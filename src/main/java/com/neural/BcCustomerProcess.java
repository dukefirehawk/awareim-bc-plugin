package com.neural;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.dataobjects.InvalidParameterException;
import org.openadaptor.util.DateTimeHolder;

import com.bas.basserver.executionengine.ExecutionException;
import com.bas.basserver.executionengine.IExecutionEngine;
import com.bas.basserver.executionengine.IProcess;
import com.bas.basserver.executionengine.ServerTimeOutException;
import com.bas.basserver.executionengine.SuspendProcessException;
import com.bas.connectionserver.server.AccessDeniedException;
import com.bas.shared.data.QueryResult;
import com.bas.shared.domain.configuration.elements.Query;
import com.bas.shared.domain.operation.IEntity;
import com.neural.json.Address;
import com.neural.json.Customer;
import com.neural.json.parser.AddressJsonParser;
import com.neural.json.parser.CustomerJsonParser;

public class BcCustomerProcess implements IProcess {
	
	
	//private static final Log log = LogFactory.getLog(BcCustomerProcess.class);
	private static Log log = LogFactory.getLog(BcCustomerProcess.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1954289752366681178L;
	
	private static final String PROPS_FILE = "bc_customers.props";
	
	private static final int MAX_PAGE_SUSPEND = 3;
	
	private String customQueryString = null;
	private String minCustomerIdQueryString = null;
	private String lastDateQueryString = null;
	
	private boolean m_cancelled = false;
	
	private IEntity boSettings;

	private IEntity boCustomer;

	private Properties keys;

	private String storeUrlKey;

	private String usernameKey;

	private String passwordKey;

	private String storeUrl;

	private String username;

	private String password;

	private String errorBoName;

	private String boName;

	private int page = 1;
	private int page2 = 1;

	private Object lastDate;

	private String addressOneKey;

	private String addressTwoKey;

	private String cityKey;

	private String countryCodeKey;

	private String addressTypeKey;

	private String countryKey;

	private String zipKey;

	private String stateKey;

	private String customerGroupIdKey;

	private String customerIdKey;

	private String companyKey;

	private String dateUpdatedKey;

	private String dateCreatedKey;

	private String emailAddressKey;

	private String firstNameKey;

	private String lastNameKey;

	private String notesKey;

	private String phoneKey;

	private String registrationIpAddressKey;

	private String storeCreditKey;
	
	private int syncCap = 6000;
	
	private int addedRecordCount = 0;
	private int updatedRecordCount = 0;
	
	private String minCustomerID;
	
	private int processedMaxID;

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
	
	@Override
	public Object execute(IExecutionEngine engine, Object[] parameters)
			throws SuspendProcessException, ExecutionException,
			AccessDeniedException {
		
		inform("Starting BIGCommerce Data Sync v1.0");
		
		//loadProps();
		
		this.keys = new Properties();
		File file = new File(PROPS_FILE);
		if (!file.exists()) {
			inform(PROPS_FILE + " file does not exist; defaulting the BO Names and attributes");
		} else {
			inform(PROPS_FILE + " file exists; reading the keys for BO Names and attributes");
			try(
					DataInputStream dis = new DataInputStream(new FileInputStream(PROPS_FILE));
					BufferedReader br = new BufferedReader(new InputStreamReader(dis));		
				) {			
				String eachLine = null;
				String[] temp = null;
				while((eachLine=br.readLine())!=null) {
					temp = eachLine.split("=");
					if(temp.length>1) {
						keys.put(temp[0].trim(), temp[1].trim());
					}
				}
			} catch(Exception e) {
				error(" ", e);
			}
		}
		
		if(parameters == null || parameters.length == 0) {
			this.boSettings = getInputParameter(engine);
		} else {
			this.boSettings = (IEntity) parameters[0];
	
			/*
			 * Accept 2nd parameter as the e_customers 
			 */
			this.boCustomer = null;
			if(parameters.length > 1) {
				boCustomer = (IEntity) parameters[1];
			}
		}
		
		this.storeUrlKey = (keys.getProperty("storeUrl") != null) ? keys.getProperty("storeUrl") : "storeUrl";
		this.usernameKey = (keys.getProperty("username") != null) ? keys.getProperty("username") : "username";
		this.passwordKey = (keys.getProperty("password") != null) ? keys.getProperty("password") : "password";
		
		try {
			storeUrl = (String) boSettings.getAttributeValue(storeUrlKey);
			if(!storeUrl.endsWith("/")) {
				storeUrl += "/";
			}
			inform(storeUrlKey + " (storeUrl) --> " + storeUrl);
			
			username = (String) boSettings.getAttributeValue(usernameKey);
			inform(usernameKey + " (username) --> " + username);
			
			password = (String) boSettings.getAttributeValue(passwordKey);
			inform(passwordKey + " (password) --> " + password);
						
			// Error BO Name
			errorBoName = (keys.getProperty("errorBoNameKey") != null) ? keys.getProperty("errorBoNameKey") : "web_service_logs";			
			inform(" (Error BO Name) --> " + errorBoName);		

			// BO Name
			boName = (keys.getProperty("boNameKey") != null) ? keys.getProperty("boNameKey") : "e_customers";			
			inform(" (BO Name) --> " + boName);		
			// String settingsBo = (keys.getProperty("settingsBo") != null) ? keys.getProperty("settingsBo") : "settingsBo";
			
			// Fields
			addressOneKey = (keys.getProperty("address_oneKey") != null) ? keys.getProperty("address_oneKey") : "address_one";
			
			// street_2
			addressTwoKey = (keys.getProperty("address_twoKey") != null) ? keys.getProperty("address_twoKey") : "address_two";

			// city
			cityKey = (keys.getProperty("cityKey") != null) ? keys.getProperty("cityKey") : "city";

			// state
			stateKey = (keys.getProperty("stateKey") != null) ? keys.getProperty("stateKey") : "state";

			// zip
			zipKey = (keys.getProperty("zipKey") != null) ? keys.getProperty("zipKey") : "zip";

			// country
			countryKey = (keys.getProperty("countryKey") != null) ? keys.getProperty("countryKey") : "country";

			// address_type
			addressTypeKey = (keys.getProperty("address_typeKey") != null) ? keys.getProperty("address_typeKey") : "address_type";
			
			// country code
			countryCodeKey = (keys.getProperty("country_codeKey") != null) ? keys.getProperty("country_codeKey") : "country_code";

			// address_one
			addressOneKey = (keys.getProperty("address_oneKey") != null) ? keys.getProperty("address_oneKey") : "address_one";

			// address_two
			addressTwoKey = (keys.getProperty("address_twoKey") != null) ? keys.getProperty("address_twoKey") : "address_two";

			// address_type
			addressTypeKey = (keys.getProperty("address_typeKey") != null) ? keys.getProperty("address_typeKey") : "address_type";

			// city
			cityKey = (keys.getProperty("cityKey") != null) ? keys.getProperty("cityKey") : "city";

			// company
			companyKey = (keys.getProperty("companyKey") != null) ? keys.getProperty("companyKey") : "company";

			// country
			countryKey = (keys.getProperty("countryKey") != null) ? keys.getProperty("countryKey") : "country";

			// customer_id
			customerIdKey = (keys.getProperty("customer_idKey") != null) ? keys.getProperty("customer_idKey") : "customer_id";

			// customer_group_id
			customerGroupIdKey = (keys.getProperty("customer_group_idKey") != null) ? keys.getProperty("customer_group_idKey") : "customer_group_id";

			dateUpdatedKey = (keys.getProperty("date_updatedKey") != null) ? keys.getProperty("date_updatedKey") : "date_updated";
		
			dateCreatedKey = (keys.getProperty("date_createdKey") != null) ? keys.getProperty("date_createdKey") : "date_created";

			// email_address
			emailAddressKey = (keys.getProperty("email_addressKey") != null) ? keys.getProperty("email_addressKey") : "email_address";

			// first_name
			firstNameKey = (keys.getProperty("first_nameKey") != null) ? keys.getProperty("first_nameKey") : "first_name";

			// last_name
			lastNameKey = (keys.getProperty("last_nameKey") != null) ? keys.getProperty("last_nameKey") : "last_name";

			// notes
			notesKey = (keys.getProperty("notesKey") != null) ? keys.getProperty("notesKey") : "notes";

			// phone
			phoneKey = (keys.getProperty("phoneKey") != null) ? keys.getProperty("phoneKey") : "phone";

			// registration_ip_address
			registrationIpAddressKey = (keys.getProperty("registration_ip_addressKey") != null) ? keys.getProperty("registration_ip_addressKey") : "registration_ip_address";

			// store_credit
			storeCreditKey = (keys.getProperty("store_creditKey") != null) ? keys.getProperty("store_creditKey") : "store_credit";

			String cap = (keys.getProperty("sync_cap") != null) ? keys.getProperty("sync_cap") : "sync_cap";
			try {
				if(cap != null && !cap.isEmpty()) {
					this.syncCap = Integer.parseInt(cap);
				}
			} catch (Exception e) {	 }

			// Log error to BO
			this.errorActionKey = getAttibuteByKey("errorActionKey", "action");
			this.errorCreatedDateKey = getAttibuteByKey("errorCreatedDateKey", "created_date");
			this.errorMessageKey = getAttibuteByKey("errorMessageKey", "error_message");
			this.errorProcessNameKey = getAttibuteByKey("errorProcessNameKey", "process_name");
			this.errorStatusKey = getAttibuteByKey("errorStatusKey", "status");
			
		} catch (InvalidParameterException e) {
			error(" ", e);
			return null;
		}
		
		// This flag controls the suspend cycle
		
		this.page = 1;
		this.page2 = 1;
		this.lastDate = null;
		
		logMessageToBSV(engine, "Sync", "Starting Customer Sync", "Ok");
		execute (engine);

		return null;
	}
	
	private String getAttibuteByKey(String key, String defaultValue) {
		return (this.keys.getProperty(key) != null) ? this.keys.getProperty(key) : defaultValue;
	}
	
	@Override
	public Object resume(IExecutionEngine engine, Object parameters)
			throws SuspendProcessException, ExecutionException,
			AccessDeniedException {

		inform("Resume");

		logMessageToBSV(engine, "Sync", "Resume Customer Sync", "Ok");
		execute (engine);
		
		return null;
	}

	private void execute(IExecutionEngine engine)
				throws SuspendProcessException, AccessDeniedException, ExecutionException {	
		try {
			
			int totalPage = page + page2 - 1;
			if(totalPage % MAX_PAGE_SUSPEND == 0) {
				//Runtime instance = Runtime.getRuntime();
				//long maxMemory = instance.maxMemory();
				//long usedMemory = instance.totalMemory() - instance.freeMemory();
				//long freeMemory = maxMemory - usedMemory;
				
				//double memoryThreshold = (double)freeMemory/maxMemory * 100;
				if(addedRecordCount > syncCap) {
					inform("Hit " + syncCap + " records cap. Customer Process is stopped.");
					return;
				} else {
					//inform("Free memory: " + (freeMemory / 1024 / 1024) + "Mb");
					//inform("Max memory: " + (maxMemory / 1024 / 1024) + "Mb");
					inform("Sleep 10 sec");
					Thread.sleep(10000);
				}
			}
			//IExecutionContext myContext = engine.getExecutionContext(this);

			BigCommerceConnector bcConnector = new BigCommerceConnector(this.storeUrl, this.username, this.password);
			
			String urlPath = "customers";
			
			// Business Object customer passed in
	    	if(boCustomer != null) {
				String customerId = (String) boCustomer.getAttributeValue(customerIdKey);
	    		urlPath = urlPath + "/" + customerId;

	    		@SuppressWarnings({ "unchecked" })
				List<Customer> customerList = bcConnector.serviceGET(urlPath, "", new CustomerJsonParser());
	    		if(customerList.isEmpty()) {
	    			// Customer not found
	    			return;
	    		}
	    		
	    		dataSyncSingle(engine, bcConnector, storeUrl, boCustomer, customerList.get(0));
	    		
	    		updatedRecordCount++;

	    	} else {
	    		/*
	    		 * For sync everything, only take records that are later than last modified date
	    		 * Only check the last modified on first call.
	    		 * 
	    		 */
   			 	 syncCustomerByID(engine, bcConnector);
	    		 
	    		 if(!this.m_cancelled) {
		    		 syncCustomerByModifiedDate(engine, bcConnector);
	    		 }
	    		 
	    	}
		} catch (InvalidParameterException | ServerTimeOutException e) {
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
		}
		
		inform("Total new records added: " + addedRecordCount);
		inform("Total records updated: " + updatedRecordCount);
		
		String message = "Total new records added: " + addedRecordCount +
				", Total records updated:" + updatedRecordCount;
		logMessageToBSV(engine, "Sync", "Finished Customer Sync, " + message, "Ok");
				
	}

	private void syncCustomerByID(IExecutionEngine engine, BigCommerceConnector bcConnector) 
			throws InvalidParameterException, AccessDeniedException, 
					ServerTimeOutException, ExecutionException, SuspendProcessException, 
					BigCommerceException, com.bas.shared.ruleparser.ParseException, InvalidTypeException {
		
		inform("Sync by min customer ID");
		
		if(this.page2 == 1) {
			this.minCustomerID = getMinCustomerID(engine);
				
			if(this.minCustomerID != null && !this.minCustomerID.equals("")) {				
				this.processedMaxID = Integer.parseInt(minCustomerID);
			}			
			
		}
		String urlPath = "customers?limit=100&min_id=" + minCustomerID;

		while(true) {
			
			if(this.m_cancelled) {
				inform("Process has been cancelled");
				//inform("Total records synced: " + processedRecordCount);
				return;
			}
			
    		String pagedUrlPath = urlPath + "&page=" + page2;
    		inform("BC URL: " + pagedUrlPath);
    		
    		@SuppressWarnings({ "unchecked" })
			List<Customer> customerList = bcConnector.serviceGET(pagedUrlPath, "", new CustomerJsonParser());
			if(customerList.isEmpty()) {
				// Finish
				break;
			}
			
			// Store the max customer ID
			for(Customer rec: customerList) {
				if(rec.getCustomerId() != null && !rec.getCustomerId().equals("")) {
				
					int value = Integer.parseInt(rec.getCustomerId());
				
					if(value > this.processedMaxID) {
						this.processedMaxID = value;
					}
				}			
			}

			int processed = dataSyncMultiple(engine, bcConnector, urlPath, customerList);
			
			addedRecordCount += processed;
			
			page2++;
			
			/*
			 * Suspend if max page is reached
			 */
			if(page2 % MAX_PAGE_SUSPEND == 0) {
				inform("suspend");
				throw new SuspendProcessException(true);
			}
		}
		
	}
	
	private void syncCustomerByModifiedDate(IExecutionEngine engine, BigCommerceConnector bcConnector) 
			throws InvalidParameterException, AccessDeniedException, 
					ServerTimeOutException, ExecutionException, SuspendProcessException, BigCommerceException, 
					com.bas.shared.ruleparser.ParseException, InvalidTypeException {
		
		inform("Sync by modified date");
		
		if(this.page == 1) {
			lastDate = getLastSyncDate(engine);
			inform("Get last Date: " + lastDate);
		}
		
		String urlPath = "customers";
		if(lastDate == null) {
			// Do nothing
			return;
		} else {
			urlPath = urlPath + "?limit=100&min_date_modified=" + lastDate;
		}

		while(true) {
			
			if(this.m_cancelled) {
				inform("Process has been cancelled");
				return;
			}
			
    		String pagedUrlPath = urlPath + "&page=" + page;
    		inform("BC URL: " + pagedUrlPath);
    		
    		@SuppressWarnings({ "unchecked" })
			List<Customer> customerList = bcConnector.serviceGET(pagedUrlPath, "", new CustomerJsonParser());
			if(customerList.isEmpty()) {
				// Finish
				break;
			}
			
			// Filter out customer with ID that is outside the range.
			for(int i=customerList.size(); i>0; i--) {
				Customer rec = customerList.get(i-1);
				if(rec.getCustomerId() != null && !rec.getCustomerId().equals("")) {
					
					int value = Integer.parseInt(rec.getCustomerId());
				
					if(value > this.processedMaxID) {
						customerList.remove(i-1);
					}
				}			
				
			}
			int processed = dataSyncMultiple(engine, bcConnector, urlPath, customerList);
			updatedRecordCount += processed;
			
			page++;
			
			/*
			 * Suspend if max page is reached
			 */
			if(page % MAX_PAGE_SUSPEND == 0) {
				inform("suspend");
				throw new SuspendProcessException(true);
			}
		}
		
	}
	
	private void dataSyncSingle(IExecutionEngine engine,
			BigCommerceConnector bcConnector, String storeUrl, 
			IEntity boInstance, Customer jsonCustomer) 
					throws AccessDeniedException, ServerTimeOutException, ExecutionException, 
					       InvalidParameterException, BigCommerceException  {
		
		try {

			inform(" Created BO --> " + boInstance.getId());

			updateCustomer(jsonCustomer, boInstance);

			String addrPath= "customers/" + jsonCustomer.getCustomerId() + "/addresses";
			//System.out.println("addrPath: " + addrPath);

			inform("Updating customers bo ");
			engine.updateEntity(this, boInstance, null, null, null);
			inform("customers bo updated");

			@SuppressWarnings("unchecked")
			List<Address> addressList = bcConnector.serviceGET(addrPath,"", new AddressJsonParser());
			if (!addressList.isEmpty()) {
				updateCustomerAddress(addressList.get(0), boInstance);
			}
			
		} catch (AccessDeniedException | ServerTimeOutException | ExecutionException | InvalidParameterException e) {
			error(" ", e);
			throw e;
		} catch (BigCommerceException e) {
			error(" ", e);
			throw e;
		}
			
	}
	
	private int dataSyncMultiple(IExecutionEngine engine,
			BigCommerceConnector bcConnector, String storeUrl, List<Customer> customerList) 
			throws AccessDeniedException, ServerTimeOutException, ExecutionException, 
			       InvalidParameterException, BigCommerceException, com.bas.shared.ruleparser.ParseException  {
		
		int counter = 0;
    	for(Customer rec: customerList) {
    		
			if(this.m_cancelled) {
				inform("Process has been cancelled");
				return counter;
			}
    		
    		try {
	    		IEntity boInstance = getEntityByCustomerID(engine, rec.getCustomerId());
	    		if(boInstance == null) {
	    			boInstance = engine.createEntity(this, boName);
	    		}
	    		
	    		dataSyncSingle(engine, bcConnector, storeUrl, boInstance, rec);
	    		
    		} catch (AccessDeniedException | ServerTimeOutException | ExecutionException| InvalidParameterException e) {
    			error(" ", e);
    			throw e;
    		} catch (BigCommerceException e) {
    			error(" ", e);
    			throw e;
			} catch (com.bas.shared.ruleparser.ParseException e) {
    			error(" ", e);
    			throw e;
			}
			
    		counter++;
    	 }
		
    	return counter;
	}
	
	private String getCustomerByIdQuery(String id) {
		
		// Cache the query for reuse
		if(customQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ");
	
			buff.append(boName);
			buff.append(" WHERE ");
			buff.append(boName);
			buff.append(".");
			
			buff.append(customerIdKey);
			buff.append("=");
			
			this.customQueryString = buff.toString();
			
		}
		
		//String q = this.customQueryString + "'" + id + "'";
		String q = this.customQueryString + id ;
		inform("Custom Rule Query: " + q);
		
		return q;
	}
	
	/*
	 * Get the customer by customer id
	 */
	private IEntity getEntityByCustomerID(IExecutionEngine engine, String customerID) 
			throws com.bas.shared.ruleparser.ParseException, 
				ExecutionException, AccessDeniedException {
		
		try {
			Query customQuery = Query.createFromRuleLanguageString(getCustomerByIdQuery(customerID));
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				inform("Found Customer ID: " + customerID);
				
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
		}
		
		return null;
	}
	
	private String getMinCustomerIdQuery() {
		
		// Cache the query for reuse
		if(minCustomerIdQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ALL ");
	
			buff.append(boName);
			buff.append(" ORDER BY ");
			buff.append(boName);
			buff.append(".");
			
			buff.append(customerIdKey);
			buff.append(" DESC TAKE BEST 1");
			
			this.minCustomerIdQueryString = buff.toString();
			
		}
		
		inform("Min Customer Query: " + minCustomerIdQueryString);
		
		return minCustomerIdQueryString;
	}
	
	/*
	 * Get the customer by customer id
	 */
	private String getMinCustomerID(IExecutionEngine engine) 
			throws com.bas.shared.ruleparser.ParseException, ExecutionException, 
					AccessDeniedException, InvalidParameterException, InvalidTypeException {
		
		try {
			
			Query customQuery = Query.createFromRuleLanguageString(getMinCustomerIdQuery());
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				Object dat = data[0].getAttributeValue(customerIdKey);
				if(dat instanceof Long) {
					Long custId = (Long)dat;
					inform("Found Min Customer ID: " + custId);
				
					return custId.toString();
				} else {
					String typeName = dat.getClass().getName();
					throw new InvalidTypeException("Expected " + customerIdKey + " field to be Number, but It is " + typeName);
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
	
	private String getLastDateQuery() {

		// Cache the query for reuse
		if(lastDateQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ALL ");
	
			buff.append(boName);
			buff.append(" ORDER BY ");
			buff.append(boName);
			buff.append(".");
			
			buff.append(dateUpdatedKey);
			buff.append(" DESC TAKE BEST 1");
			
			this.lastDateQueryString = buff.toString();
			
		}
		
		inform("Last Date Query: " + lastDateQueryString);
		
		return lastDateQueryString;
	}
	
	private IEntity getInputParameter(IExecutionEngine engine) 
			throws ExecutionException, AccessDeniedException {
		
		try {
			String namedQuery = (keys.getProperty("inputParametersNameQuery") != null) ? keys.getProperty("inputParametersNameQuery") : "get_input_parameters";
			inform("Input Parameters Named Query: " + namedQuery);
			QueryResult result = engine.executeNamedQuery(this, namedQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				
				return data[0];
			}
		} catch (ExecutionException e) {
			error(" ", e);
			throw e;
		} catch (AccessDeniedException e) {
			error(" ", e);
			throw e;
		}
		
		return null;
	
	}
	

	private String getLastSyncDate(IExecutionEngine engine) 
			throws InvalidParameterException, com.bas.shared.ruleparser.ParseException, 
					ExecutionException, AccessDeniedException, InvalidTypeException {
		
		try {
			Query customQuery = Query.createFromRuleLanguageString(getLastDateQuery());
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			//String namedQuery = (keys.getProperty("named_customer_time_query") != null) ? keys.getProperty("named_customer_time_query") : "get_customer_max_modified_time";
			//inform("Named Query: " + namedQuery);
			//QueryResult result = engine.executeNamedQuery(this, namedQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				Object dat = data[0].getAttributeValue(dateUpdatedKey);
				if(dat instanceof Date) {
					Date updateDate = (Date)dat;
					
					if(updateDate != null) {
						inform("Found Last Sync Date: " + updateDate);
						return getISO8601Date(updateDate);
					}
					
				} else if ( dat instanceof DateTimeHolder) {
					DateTimeHolder updateDate = (DateTimeHolder)dat;
					if(updateDate != null) {
						Calendar cal = Calendar.getInstance();
						cal.set(updateDate.getTrueYear(), updateDate.getMonth(), 
								updateDate.getDate(), updateDate.getHours(), 
								updateDate.getMinutes(), updateDate.getSeconds());
						cal.add(Calendar.SECOND, 1);
						
						Date d = cal.getTime();
						
						inform("Found Last Sync Date: " + d);
						return getISO8601Date(d);
					}
				} else {
					String typeName = dat.getClass().getName();
					throw new InvalidTypeException("Expected " + dateUpdatedKey + " field to be Date/Timestamp, but it is " + typeName);
				}
			}
		} catch (InvalidParameterException e) {
			error(" ", e);
			throw e;
		} catch (com.bas.shared.ruleparser.ParseException e) {
			error(" ", e);
			throw e;
		} catch (ExecutionException e) {
			error(" ", e);
			throw e;
		} catch (AccessDeniedException e) {
			error(" ", e);
			throw e;
		}
		
		return null;
	
	}
	
	private void updateCustomerAddress(Address addressJsonObj, IEntity boInstance) 
			throws InvalidParameterException {
		// street_1
		String addressOne = addressJsonObj.getStreet1();
		inform(addressOneKey + " (address_one Key) --> " + addressOne);
		boInstance.setAttributeValue(addressOneKey, addressOne);

		// street_2
		String addressTwo = addressJsonObj.getStreet2();
		inform(addressTwoKey + " (addressTwo Key) --> " + addressTwo);
		boInstance.setAttributeValue(addressTwoKey, addressTwo);

		// city
		String city = addressJsonObj.getCity();
		inform(cityKey + " (city Key) --> " + city);
		boInstance.setAttributeValue(cityKey, city);

		// state
		String state = addressJsonObj.getState();
		inform(stateKey + " (state Key) --> " + state);
		boInstance.setAttributeValue(stateKey, state);

		// zip
		String zip = addressJsonObj.getZip();
		inform(zipKey + " (zip Key) --> " + zip);
		boInstance.setAttributeValue(zipKey, zip);

		// country
		String country = addressJsonObj.getCountry();
		inform(countryKey + " (country Key) --> " + country);
		boInstance.setAttributeValue(countryKey, country);

		// address_type
		String addressType = addressJsonObj.getAddressType();
		inform(addressTypeKey + " (address_type Key) --> " + addressType);
		boInstance.setAttributeValue(addressTypeKey, addressType);
		
		// country code
		String countryCode = addressJsonObj.getCountryIso2();
		inform(countryCodeKey + " (country_code Key) --> " + countryCode);
		boInstance.setAttributeValue(countryCodeKey, countryCode);
	}
	
	private void updateCustomer(Customer customerJsonObj, IEntity boInstance) 
			throws InvalidParameterException {
		// address_one
		String addressOne = customerJsonObj.getAddressOne();
		inform(addressOneKey + " (address_one Key) --> " + addressOne);
		boInstance.setAttributeValue(addressOneKey, addressOne);

		// address_two
		String addressTwo = customerJsonObj.getAddressTwo();
		inform(addressTwoKey + " (address_two Key) --> " + addressTwo);
		boInstance.setAttributeValue(addressTwoKey, addressTwo);

		// address_type
		String addressType = customerJsonObj.getAddressType();
		inform(addressTypeKey + " (address_type Key) --> " + addressType);
		boInstance.setAttributeValue(addressTypeKey, addressType);

		// city
		String city = customerJsonObj.getCity();
		inform(cityKey + " (city Key) --> " + city);
		boInstance.setAttributeValue(cityKey, city);

		// company
		String company = customerJsonObj.getCompany();
		inform(companyKey + " (company Key) --> " + company);
		boInstance.setAttributeValue(companyKey, company);

		// country
		String country = customerJsonObj.getCountry();
		inform(countryKey + " (country Key) --> " + country);
		boInstance.setAttributeValue(countryKey, country);

		// country_code
		String countryCode = customerJsonObj.getCountryCode();
		inform(countryCodeKey + " (country_code Key) --> " + countryCode);
		boInstance.setAttributeValue(countryCodeKey, countryCode);

		// customer_id
		String customerId = customerJsonObj.getCustomerId();
		inform(customerIdKey + " (customer_id Key) --> " + customerId);
		boInstance.setAttributeValue(customerIdKey, customerId);

		// customer_group_id
		String customerGroupId = customerJsonObj.getCustomerGroupId();
		inform(customerGroupIdKey + " (customer_group_id Key) --> " + customerGroupId);
		boInstance.setAttributeValue(customerGroupIdKey, customerGroupId);

		SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
		
		try {
			// date_created
			String dateCreated = customerJsonObj.getDateCreated();
			inform(dateCreatedKey + " (date_created Key) --> " + dateCreated);
			boInstance.setAttributeValue(dateCreatedKey, format.parse(dateCreated));

		} catch (Exception e) {
			error(" ", e);
		}

		try {
			// date_updated
			String dateUpdated = customerJsonObj.getDateUpdated();
			inform(dateUpdatedKey + " (date_updated Key) --> " + dateUpdated);
			boInstance.setAttributeValue(dateUpdatedKey, format.parse(dateUpdated));
		} catch (Exception e) {
			error(" ", e);
		}

		// email_address
		String emailAddress = customerJsonObj.getEmailAddress();
		inform(emailAddressKey + " (email_address Key) --> " + emailAddress);
		boInstance.setAttributeValue(emailAddressKey, emailAddress);

		// first_name
		String firstName = customerJsonObj.getFirstName();
		inform(firstNameKey + " (first_name Key) --> " + firstName);
		boInstance.setAttributeValue(firstNameKey, firstName);

		// last_name
		String lastName = customerJsonObj.getLastName();
		inform(lastNameKey + " (last_name Key) --> " + lastName);
		boInstance.setAttributeValue(lastNameKey, lastName);

		// notes
		String notes = customerJsonObj.getNotes();
		inform(notesKey + " (notes Key) --> " + notes);
		boInstance.setAttributeValue(notesKey, notes);

		// phone
		String phone = customerJsonObj.getPhone();
		inform(phoneKey + " (phone Key) --> " + phone);
		boInstance.setAttributeValue(phoneKey, phone);

		// registration_ip_address
		String registrationIpAddress = customerJsonObj.getRegistrationIpAddress();
		inform(registrationIpAddressKey + " (registration_ip_address Key) --> " + registrationIpAddress);
		boInstance.setAttributeValue(registrationIpAddressKey, registrationIpAddress);

		// state
		String state = customerJsonObj.getState();
		inform(stateKey + " (state Key) --> " + state);
		boInstance.setAttributeValue(stateKey, state);

		// store_credit
		String storeCredit = customerJsonObj.getStoreCredit();
		inform(storeCreditKey + " (store_credit Key) --> " + storeCredit);
		//Object oldStoreCredit = boInstance.getAttributeValue(storeCreditKey);
		boInstance.setAttributeValue(storeCreditKey, storeCredit);

		// zip
		String zip = customerJsonObj.getZip();
		inform(zipKey + " (zip Key) --> " + zip);
		boInstance.setAttributeValue(zipKey, zip);

	}
	
	/**
	 * Logs the message into the console
	 * 
	 * @param message
	 *            The message to be logged into console
	 */
	private void inform(String message) {
		//System.out.println(message);
		
		log.info("[BSV Customers] " + message);
	}
	
	private void error(String message, Throwable t) {
		//System.out.println(message);
		if( t != null) {
			log.error("[BSV Customers] ", t);
		} else {
			log.error("[BSV Customers] ");			
		}
	}

	private String getISO8601Date(Date date) {
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String d = parser.format(date);
		return d.replace(" ", "T");
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

	/*
	public static void main(String args[]) {
		String d = "Tue, 25 Aug 2015 02:21:57 +0000";
		
		SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");
		
		try {
			Date dat = format.parse(d);
		    System.out.println("Date: " + dat);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String storeUrl = "https://store-r5adzn.mybigcommerce.com/api/v2";
		String username = "thomas_hii";
		String password = "ddee9404ad8c2f5698dae9270879fd7d3abb78ac";
	     BigCommerceConnector conn = new BigCommerceConnector(storeUrl, username, password);
   	 
		try {
		     @SuppressWarnings({ "unchecked" })
		     List<Customer> customerList = conn.serviceGET("/customers", "", new CustomerJsonParser());
		     for(Customer rec: customerList) {
		    	 System.out.println("Data: " + rec.getCustomerId());
				 String addrPath = "/customers/" + rec.getCustomerId() + "/" + "addresses";
				 
				 @SuppressWarnings("unchecked")
				 List<Address> addressList = conn.serviceGET( addrPath, "", new AddressJsonParser());
				 if(!addressList.isEmpty()) {
					 System.out.println("Address: " + addressList.get(0).getId());
				 }

		     }
		} catch (BigCommerceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
*/
}
