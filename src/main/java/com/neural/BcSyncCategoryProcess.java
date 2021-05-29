package com.neural;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.dataobjects.InvalidParameterException;

import com.bas.basserver.executionengine.ExecutionException;
import com.bas.basserver.executionengine.IExecutionEngine;
import com.bas.basserver.executionengine.IProcess;
import com.bas.basserver.executionengine.ServerTimeOutException;
import com.bas.basserver.executionengine.SuspendProcessException;
import com.bas.connectionserver.server.AccessDeniedException;
import com.bas.shared.data.QueryResult;
import com.bas.shared.domain.configuration.elements.Query;
import com.bas.shared.domain.operation.IEntity;
import com.bas.shared.domain.operation.IObject;
import com.bas.shared.ruleparser.ParseException;
import com.neural.json.Category;
import com.neural.json.parser.CategoryJsonParser;

public class BcSyncCategoryProcess implements IProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = -454790101100678727L;

	private static Log log = LogFactory.getLog(BcSyncCategoryProcess.class);
	
	private static final String 	PROPS_FILE = "bc_category.props";
	private static final int 		MAX_PAGE_SUSPEND = 3;
	private static final int		SLEEP_TIME = 2000;
	private static final String 	SYNC_ALL = "all";
	private static final int 		READ_LIMIT = 100;	

	private Properties keys;

	private String username;
	private String password;
	private String storeUrl;
	private String syncOption;

	private String errorBoName;
	private String boName;

	private IObject boSettings;
	private IEntity boCategory;

	private boolean m_cancelled = false;
	private int page = 1;
	private String minID = "1";

	private String categoryIdKey;

	private String parentIdKey;

	private String nameKey;

	private String descriptionKey;

	private String sortOrderKey;

	private String pageTitleKey;

	private String metaKeywordsKey;

	private String metaDescriptionKey;

	private String layoutFileKey;

	private String imageFileKey;

	private String isVisibleKey;

	private String searchKeywordsKey;

	private String urlKey;

	private String minIdQueryString;

	private Object customQueryString;

	private String errorActionKey;

	private String errorMessageKey;

	private String errorProcessNameKey;

	private String errorStatusKey;

	private String errorCreatedDateKey;

	@Override
	public boolean cancel() {
		/* 
		 * set m_cancelled flag. This flag will be checked by execute
		 * and resume methods
		*/
		synchronized (this) {
			m_cancelled = true;
		}
		return true;
	}

	@Override
	public Object execute(IExecutionEngine engine, Object[] parameters)
			throws SuspendProcessException, ExecutionException, AccessDeniedException {

		inform("Starting BC Category Sync");
		
		if(parameters == null || parameters.length < 1) {
			error("This process is expecting a parameter", null);
			return null;
		}
		
		this.boSettings = (IEntity) parameters[0];		
		
		try {
			loadProps();
			initConfig();
			
		} catch (InvalidParameterException e) {
			error("Failed to find/load configuration file", e);
			return null;
		} catch (Exception e) {
			error("Failed to find/load configuration file", e);
			return null;			
		}
		
		this.page = 1;
		
		logMessageToBSV(engine, "Sync", "Starting Category Sync", "Ok");
		execute (engine);

		return null;
	}
	
	
	private void execute(IExecutionEngine engine)
			throws SuspendProcessException, AccessDeniedException, ExecutionException {	

		try {

		  	BigCommerceConnector bcConnector = new BigCommerceConnector(this.storeUrl, this.username, this.password);
			String urlPath = "categories?limit=" + READ_LIMIT;
			
    		if(this.page == 1) {
    			if(SYNC_ALL.equals(this.syncOption)) {
    				this.minID = "1";
    			} else {
    				this.minID = getMinCategoryID(engine);
     			}
    		} else {
				Thread.sleep(SLEEP_TIME);
    		}
    		
			urlPath += "&min_id=" + this.minID;
    		
    		inform("UrlPath: " + urlPath);

    		while(true) {
    			
    			if(this.m_cancelled) {
    				inform("Process has been cancelled");
    				return;
    			}

	    		String pagedUrlPath = urlPath + "&page=" + page;

	    		@SuppressWarnings({ "unchecked" })
				List<Category> categoryList = bcConnector.serviceGET(pagedUrlPath, "", new CategoryJsonParser());
				if(categoryList.isEmpty()) {
					// Finish
					break;
				}
    			dataSyncMultiple(engine, urlPath, categoryList);
    			page++;
    			
    			/*
    			 * Suspend if max page is reached
    			 */
    			if(page % MAX_PAGE_SUSPEND == 0) {
    				inform("suspend process");
    				throw new SuspendProcessException(true);
    			}
    			
    		}
			
		} catch (InvalidParameterException e) {
			error(" ", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		} catch (InterruptedException e) {
			error(" ", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		} catch (BigCommerceException e) {
			error(" ", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		} catch (com.bas.shared.ruleparser.ParseException e) {
			error(" ", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		} catch (InvalidTypeException e) {
			error(" ", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		} catch (ServerTimeOutException e) {
			error(" ", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		}
		
	}
	
	private int dataSyncMultiple(IExecutionEngine engine,
			String urlPath, List<Category> categoryList) 
					throws AccessDeniedException, ServerTimeOutException, 
						ExecutionException, InvalidParameterException, ParseException {
		
		int counter = 0;
    	for(Category rec: categoryList) {
    		
			if(this.m_cancelled) {
				inform("Process has been cancelled");
				return counter;
			}

			try {
	    		IEntity boInstance = getEntityByCategoryID(engine, rec.getCategoryId());
	    		if(boInstance == null) {
	    			boInstance = engine.createEntity(this, boName);
	    		}
	    		
	    		dataSyncSingle(engine, boInstance, rec);
	    		
    		} catch (AccessDeniedException e) {
    			error(" ", e);
    			throw e;
    		} catch (ServerTimeOutException e) {
    			error(" ", e);
    			throw e;
    		} catch (ExecutionException e) {
    			error(" ", e);
    			throw e;
    		} catch (InvalidParameterException e) {
    			error(" ", e);
    			throw e;
			} catch (ParseException e) {
    			error(" ", e);
    			throw e;
			}
			
			counter++;
			
    	 }
		
    	return counter;
		
	}

	private void dataSyncSingle(IExecutionEngine engine, IEntity boInstance, Category rec) 
		throws ServerTimeOutException, ExecutionException, 
					AccessDeniedException, InvalidParameterException  {
		try {

			inform(" Created BO --> " + boInstance.getId());

			updateCategory(rec, boInstance);

			inform("Updating the category bo ");
			engine.updateEntity(this, boInstance, null, null, null);
			inform("category bo updated");
			
		} catch (InvalidParameterException e) {
			error(" ", e);
			throw e;
		}
	}

	private void updateCategory(Category rec, IEntity boInstance) throws InvalidParameterException {
		
		boInstance.setAttributeValue(this.categoryIdKey, rec.getCategoryId());
		boInstance.setAttributeValue(this.descriptionKey, rec.getDescription());
		boInstance.setAttributeValue(this.imageFileKey, rec.getImageFile());
		boInstance.setAttributeValue(this.isVisibleKey, rec.getIsVisible());
		boInstance.setAttributeValue(this.layoutFileKey, rec.getLayoutFile());
		boInstance.setAttributeValue(this.metaDescriptionKey, rec.getMetaDescription());
		boInstance.setAttributeValue(this.metaKeywordsKey, rec.getMetaKeywords());
		boInstance.setAttributeValue(this.nameKey, rec.getName());
		boInstance.setAttributeValue(this.pageTitleKey, rec.getPageTitle());
		boInstance.setAttributeValue(this.parentIdKey, rec.getParentId());
		boInstance.setAttributeValue(this.searchKeywordsKey, rec.getSearchKeywords());
		boInstance.setAttributeValue(this.sortOrderKey, rec.getSortOrder());
		boInstance.setAttributeValue(this.urlKey, rec.getUrl());
		
	}

	private IEntity getEntityByCategoryID(IExecutionEngine engine, String categoryId) 
			throws ParseException, ExecutionException, AccessDeniedException, InvalidParameterException {
		try {
			Query customQuery = Query.createFromRuleLanguageString(getCategoryByIdQuery(categoryId));
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				inform("Found Category ID: " + data[0].getAttributeValue(this.categoryIdKey));
				
				return data[0];
			}
		} catch (com.bas.shared.ruleparser.ParseException e) {
			error(" ", e);
			throw e;
		} catch (ExecutionException e) {
			error(" ", e);
			throw e;
		} catch (AccessDeniedException e) {
			error(" ", e);
			throw e;
		} catch (InvalidParameterException e) {
			error(" ", e);
			throw e;
		}
		
		return null;
	}

	private String getCategoryByIdQuery(String categoryId) {
		// Cache the query for reuse
		if(customQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ");
	
			buff.append(this.boName);
			buff.append(" WHERE ");
			buff.append(boName);
			buff.append(".");
			
			buff.append(this.categoryIdKey);
			buff.append("=");
			
			this.customQueryString = buff.toString();
			
		}
		
		//String q = this.customQueryString + "'" + id + "'";
		String q = this.customQueryString + categoryId;
		inform("Custom Rule Query: " + q);
		
		return q;
	}

	private String getMinCategoryID(IExecutionEngine engine) 
			throws InvalidTypeException, ParseException, ExecutionException, AccessDeniedException, InvalidParameterException {
		try {
			
			Query customQuery = Query.createFromRuleLanguageString(getMinIDQuery());
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				Object dat = data[0].getAttributeValue(this.categoryIdKey);
				if(dat instanceof Long) {
					Long ordId = (Long)dat;
					inform("Found Min ID: " + ordId);
					
					return ordId.toString();
				} else if(dat instanceof Double) {
					Double ordId = (Double)dat;
					inform("Found Min ID: " + ordId);
						
					return ordId.toString();
				} else {
					String typeName = dat.getClass().getName();
					throw new InvalidTypeException("Expected " + this.categoryIdKey + " field to be Number, but it is " + typeName);
				}
			} else {
				return "1";
			}
		} catch (com.bas.shared.ruleparser.ParseException e) {
			error(" ", e);
			throw e;
		} catch (ExecutionException e) {
			error(" ", e);
			throw e;
		} catch (AccessDeniedException e) {
			error(" ", e);
			throw e;
		} catch (InvalidParameterException e) {
			error(" ", e);
			throw e;
		}
	}

	private String getMinIDQuery() {
		// Cache the query for reuse
		if(minIdQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ALL ");
	
			buff.append(this.boName);
			buff.append(" ORDER BY ");
			buff.append(this.boName);
			buff.append(".");
			
			buff.append(this.categoryIdKey);
			buff.append(" DESC TAKE BEST 1");
			
			this.minIdQueryString = buff.toString();
			
		}
		
		inform("Min Query: " + minIdQueryString);
		
		return minIdQueryString;
	}

	@Override
	public Object resume(IExecutionEngine engine, Object parameters)
			throws SuspendProcessException, ExecutionException, AccessDeniedException {
		inform("Resume");

		execute(engine);
		
		return null;
	}

	private void loadProps() {
		try(
			InputStream in = new FileInputStream(PROPS_FILE);
				
		) {
			this.keys = new Properties();
			keys.load(in);
		} catch(Exception ex) {
			error("Failed to load properties file", ex);
		}
		
	}
	
	private void initConfig() throws InvalidParameterException {
		
		String storeUrlKey = (keys.getProperty("storeUrl") != null) ? keys.getProperty("storeUrl") : "storeUrl";
		String usernameKey = (keys.getProperty("username") != null) ? keys.getProperty("username") : "username";
		String passwordKey = (keys.getProperty("password") != null) ? keys.getProperty("password") : "password";
		String syncOptionKey = (keys.getProperty("syncOption") != null) ? keys.getProperty("syncOption") : "sync_option";
		
		this.storeUrl = (String) boSettings.getAttributeValue(storeUrlKey);
		if(!storeUrl.endsWith("/")) {
			storeUrl += "/";
		}
		inform(" (storeUrl) --> " + storeUrl);
		
		this.username = (String) boSettings.getAttributeValue(usernameKey);
		inform(" (username) --> " + username);
		
		this.password = (String) boSettings.getAttributeValue(passwordKey);
		inform(" (password) --> " + password);
					
		this.syncOption = (String) boSettings.getAttributeValue(syncOptionKey);
		inform(" (sync_option) --> " + syncOption);
					
		// Error BO Name
		this.errorBoName = (keys.getProperty("errorBoNameKey") != null) ? keys.getProperty("errorBoNameKey") : "web_service_logs";			
		inform(" (Error BO Name) --> " + errorBoName);		

		// BO Name
		this.boName = (keys.getProperty("boNameKey") != null) ? keys.getProperty("boNameKey") : "e_coupon";			
		inform(" (BO Name) --> " + boName);
		
		// String settingsBo = (keys.getProperty("settingsBo") != null) ? keys.getProperty("settingsBo") : "settingsBo";
		// Coupon Id
		this.categoryIdKey = getAttibuteByKey("categoryIdKey","category_id");
		this.parentIdKey = getAttibuteByKey("parentIdKey","parent_id");
		this.nameKey = getAttibuteByKey("nameKey","name");
		this.descriptionKey = getAttibuteByKey("descriptionKey","description");
		this.sortOrderKey = getAttibuteByKey("sortOrderKey","sort_order");
		this.pageTitleKey = getAttibuteByKey("pageTitleKey","page_title");
		this.metaKeywordsKey = getAttibuteByKey("metaKeywordsKey","meta_keywords");
		this.metaDescriptionKey = getAttibuteByKey("metaDescriptionKey","meta_description");
		this.layoutFileKey = getAttibuteByKey("layoutFileKey","layout_file");

		this.imageFileKey = getAttibuteByKey("imageFileKey","image_file");
		this.isVisibleKey = getAttibuteByKey("isVisibleKey","is_visible");
		this.searchKeywordsKey = getAttibuteByKey("searchKeywordsKey","search_keywords");
		this.layoutFileKey = getAttibuteByKey("layoutFileKey","layout_file");
		this.urlKey = getAttibuteByKey("urlKey","url");

		// Log error to BO
		this.errorActionKey = getAttibuteByKey("errorActionKey", "action");
		this.errorCreatedDateKey = getAttibuteByKey("errorCreatedDateKey", "created_date");
		this.errorMessageKey = getAttibuteByKey("errorMessageKey", "error_message");
		this.errorProcessNameKey = getAttibuteByKey("errorProcessNameKey", "process_name");
		this.errorStatusKey = getAttibuteByKey("errorStatusKey", "status");
	}

	private String getAttibuteByKey(String key, String defaultValue) {
		return (this.keys.getProperty(key) != null) ? this.keys.getProperty(key) : defaultValue;
	}
	
	
	/**
	 * Logs the message into the console
	 * 
	 * @param message
	 *            The message to be logged into console
	 */
	private void inform(String message) {
		//System.out.println(message);
		log.info("[BSV Sync Category] " + message);
	}
	
	private void error(String message, Throwable t) {
		//System.out.println(message);
		if( t != null) {
			log.error("[BSV Sync Category] " + message, t);
		} else {
			log.error("[BSV Sync Category] " + message);
		}
	}

	private void logMessageToBSV(IExecutionEngine engine, String action, String message, String status) {
		
		IEntity errorBo;
		try {
			errorBo = engine.createEntity(this, this.errorBoName);
			errorBo.setAttributeValue(this.errorActionKey, action);
			errorBo.setAttributeValue(this.errorMessageKey, message);
			errorBo.setAttributeValue(this.errorProcessNameKey, this.getClass().getName());
			errorBo.setAttributeValue(this.errorStatusKey, status);
			errorBo.setAttributeValue(this.errorCreatedDateKey, Calendar.getInstance().getTime());

			engine.updateEntity(this, errorBo, null, null, null);
			
		} catch (ServerTimeOutException e) {
			error("", e);
		} catch (AccessDeniedException e) {
			error("", e);
		} catch (ExecutionException e) {
			error("", e);
		} catch (InvalidParameterException e) {
			error("", e);
		} catch (Exception e) {
			error("", e);
		}

	}

}
