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
import com.bas.shared.ruleparser.ParseException;
import com.neural.json.Coupon;
import com.neural.json.parser.CouponJsonParser;

public class BcSyncCouponProcess implements IProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = -454790101100678727L;

	private static Log log = LogFactory.getLog(BcSyncCouponProcess.class);
	
	private static final String 	PROPS_FILE = "bc_get_coupon.props";
	private static final int 		MAX_PAGE_SUSPEND = 3;
	private static final int		SLEEP_TIME = 2000;
	private static final String 	SYNC_ALL = "all";
	private static final int 		READ_LIMIT = 100;	

	private Properties keys;
	
	private String password;
	private String username;
	private String storeUrl;
	private String syncOption;
	
	private String errorBoName;
	private String boName;

	private IEntity boSettings;
	private IEntity boCoupon;
	
	private boolean m_cancelled = false;
	private int page = 1;
	private String minID = "1";

	private String couponIdKey;

	private String nameKey;

	private String typeKey;

	private String amountKey;

	private String minPurchaseKey;

	private String expiresKey;

	private String codeKey;

	private String numUsesKey;

	private String maxUsesKey;

	private String maxUsesPerCustomerKey;

	private String enabledKey;

	private String appliesToKey;

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

		inform("Starting BC Coupon Sync");
		
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
		
		logMessageToBSV(engine, "Sync", "Starting Coupon Sync", "Ok");
		execute (engine);

		return null;
	}
	
	private void execute(IExecutionEngine engine)
			throws SuspendProcessException, AccessDeniedException, ExecutionException {	

		try {

		  	BigCommerceConnector bcConnector = new BigCommerceConnector(this.storeUrl, this.username, this.password);
			String urlPath = "coupons?limit=" + READ_LIMIT;
			
    		if(this.page == 1) {
    			if(SYNC_ALL.equals(this.syncOption)) {
    				this.minID = "1";
    			} else {
    				this.minID = getMinCouponID(engine);
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
				List<Coupon> couponList = bcConnector.serviceGET(pagedUrlPath, "", new CouponJsonParser());
				if(couponList.isEmpty()) {
					// Finish
					logMessageToBSV(engine, "Sync", "Finished Coupon Sync", "Ok");
					break;
				}
    			dataSyncMultiple(engine, urlPath, couponList);
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
			String urlPath, List<Coupon> couponList) 
					throws AccessDeniedException, ServerTimeOutException, 
						ExecutionException, InvalidParameterException, ParseException {
		
		int counter = 0;
    	for(Coupon rec: couponList) {
    		
			if(this.m_cancelled) {
				inform("Process has been cancelled");
				return counter;
			}

			try {
	    		IEntity boInstance = getEntityByCouponID(engine, rec.getCouponId());
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

	private void dataSyncSingle(IExecutionEngine engine, IEntity boInstance, Coupon rec) 
		throws ServerTimeOutException, ExecutionException, 
					AccessDeniedException, InvalidParameterException  {
		try {

			inform(" Created BO --> " + boInstance.getId());

			updateCoupon(rec, boInstance);

			inform("Updating the category bo ");
			engine.updateEntity(this, boInstance, null, null, null);
			inform("category bo updated");
			
		} catch (InvalidParameterException e) {
			error(" ", e);
			throw e;
		}
	}

	private void updateCoupon(Coupon rec, IEntity boInstance) throws InvalidParameterException {
		
		boInstance.setAttributeValue(this.amountKey, rec.getAmount());
		boInstance.setAttributeValue(this.codeKey, rec.getCode());
		boInstance.setAttributeValue(this.couponIdKey, rec.getCouponId());
		boInstance.setAttributeValue(this.enabledKey, rec.getEnabled());
		boInstance.setAttributeValue(this.expiresKey, rec.getExpires());
		boInstance.setAttributeValue(this.maxUsesKey, rec.getMaxUses());
		boInstance.setAttributeValue(this.maxUsesPerCustomerKey, rec.getMaxUsesPerCustomer());
		boInstance.setAttributeValue(this.nameKey, rec.getName());
		boInstance.setAttributeValue(this.numUsesKey, rec.getNumUses());
		boInstance.setAttributeValue(this.typeKey, rec.getType());
		boInstance.setAttributeValue(this.minPurchaseKey, rec.getMinPurchase());
		
	}

	private IEntity getEntityByCouponID(IExecutionEngine engine, String couponId) 
			throws ParseException, ExecutionException, AccessDeniedException, InvalidParameterException {
		try {
			Query customQuery = Query.createFromRuleLanguageString(getCouponByIdQuery(couponId));
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				inform("Found Coupon ID: " + data[0].getAttributeValue(this.couponIdKey));
				
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

	private String getCouponByIdQuery(String couponId) {
		// Cache the query for reuse
		if(customQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ");
	
			buff.append(this.boName);
			buff.append(" WHERE ");
			buff.append(this.boName);
			buff.append(".");
			
			buff.append(this.couponIdKey);
			buff.append("=");
			
			this.customQueryString = buff.toString();
			
		}
		
		//String q = this.customQueryString + "'" + id + "'";
		String q = this.customQueryString + couponId;
		inform("Custom Rule Query: " + q);
		
		return q;
	}

	private String getMinCouponID(IExecutionEngine engine) 
			throws InvalidTypeException, ParseException, ExecutionException, AccessDeniedException, InvalidParameterException {
		try {
			
			Query customQuery = Query.createFromRuleLanguageString(getMinIDQuery());
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				Object dat = data[0].getAttributeValue(this.couponIdKey);
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
					throw new InvalidTypeException("Expected " + this.couponIdKey + " field to be Number, but it is " + typeName);
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
			
			buff.append(this.couponIdKey);
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
		inform(storeUrlKey + " (storeUrl) --> " + storeUrl);
		
		this.username = (String) boSettings.getAttributeValue(usernameKey);
		inform(usernameKey + " (username) --> " + username);
		
		this.password = (String) boSettings.getAttributeValue(passwordKey);
		inform(passwordKey + " (password) --> " + password);
					
		this.syncOption = (String) boSettings.getAttributeValue(syncOptionKey);
		inform(" (sync_option) --> " + syncOption);
					
		// Error BO Name
		this.errorBoName = (keys.getProperty("errorBoNameKey") != null) ? keys.getProperty("errorBoNameKey") : "web_service_logs";			
		inform(" (Error BO Name) --> " + errorBoName);		

		// BO Name
		this.boName = (keys.getProperty("boNameKey") != null) ? keys.getProperty("boNameKey") : "e_get_coupon";			
		inform(" (BO Name) --> " + boName);
		
		// String settingsBo = (keys.getProperty("settingsBo") != null) ? keys.getProperty("settingsBo") : "settingsBo";
		// Coupon Id
		this.couponIdKey = (keys.getProperty("couponIdKey") != null) ? keys.getProperty("couponIdKey") : "coupon_id";

		this.nameKey = (keys.getProperty("nameKey") != null) ? keys.getProperty("nameKey") : "name";

		this.typeKey = (keys.getProperty("typeKey") != null) ? keys.getProperty("typeKey") : "type";
		
		this.amountKey = (keys.getProperty("amountKey") != null) ? keys.getProperty("amountKey") : "amount";

		this.minPurchaseKey = (keys.getProperty("minPurchaseKey") != null) ? keys.getProperty("minPurchaseKey") : "min_purchase";

		this.expiresKey = (keys.getProperty("expiresKey") != null) ? keys.getProperty("expiresKey") : "expires";
		
		this.codeKey = (keys.getProperty("codeKey") != null) ? keys.getProperty("codeKey") : "code";
		
		this.numUsesKey = (keys.getProperty("numUsesKey") != null) ? keys.getProperty("numUsesKey") : "num_uses";
		
		this.maxUsesKey = (keys.getProperty("maxUsesKey") != null) ? keys.getProperty("maxUsesKey") : "max_uses";
	
		this.maxUsesPerCustomerKey = (keys.getProperty("maxUsesPerCustomerKey") != null) ? keys.getProperty("maxUsesPerCustomerKey") : "max_uses_per_customer";

		this.minPurchaseKey = (keys.getProperty("minPurchaseKey") != null) ? keys.getProperty("minPurchaseKey") : "min_purchase";

		this.enabledKey = (keys.getProperty("enabledKey") != null) ? keys.getProperty("enabledKey") : "enabled";

		// Log error to BO
		this.errorActionKey = getAttibuteByKey("errorActionKey", "action");
		this.errorCreatedDateKey = getAttibuteByKey("errorCreatedDateKey", "created_date");
		this.errorMessageKey = getAttibuteByKey("errorMessageKey", "error_message");
		this.errorProcessNameKey = getAttibuteByKey("errorProcessNameKey", "process_name");
		this.errorStatusKey = getAttibuteByKey("errorStatusKey", "status");
	}
	
	/**
	 * Logs the message into the console
	 * 
	 * @param message
	 *            The message to be logged into console
	 */
	private void inform(String message) {
		//System.out.println(message);
		log.info("[BSV Sync Coupon] " + message);
	}
	
	private void error(String message, Throwable t) {
		//System.out.println(message);
		if( t != null) {
			log.error("[BSV Sync Coupon] " + message, t);
		} else {
			log.error("[BSV Sync Coupon] " + message);
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
	
	private String getAttibuteByKey(String key, String defaultValue) {
		return (this.keys.getProperty(key) != null) ? this.keys.getProperty(key) : defaultValue;
	}
	

}
