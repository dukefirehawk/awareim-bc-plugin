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
import com.bas.shared.ruleparser.ParseException;
import com.neural.json.Order;
import com.neural.json.parser.OrderJsonParser;
import com.neural.json.parser.ProductJsonParser;

public class BcOrderProcess implements IProcess {

	private static Log log = LogFactory.getLog(BcOrderProcess.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1954289752366681178L;
	
	private static final String PROPS_FILE = "bc_orders.props";
	
	private static final int MAX_PAGE_SUSPEND = 3;
	
	private String customQueryString = null;
	private String minOrderIdQueryString;
	private String lastDateQueryString;
	
	private boolean m_cancelled = false;
	
	private IEntity boSettings;

	private IEntity boOrder;

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

	private String lastDate;

	private String syncStatusKey;

	private String syncStatus;

	private String addressOneKey;

	private String addressTwoKey;

	private String baseHandlingCostKey;

	private String baseShippingCostKey;

	private String baseWrappingCostKey;

	private String cityKey;

	private String companyKey;

	private String costShippingKey;

	private String costSubtotalKey;

	private String costTaxKey;

	private String countryKey;

	private String currencyCodeKey;

	private String currencyExchangeRateKey;

	private String customerMessageKey;

	private String dateShippedKey;

	private String dateCreatedKey;

	private String dateUpdatedKey;

	private String defaultCurrencyCodeKey;

	private String discountAmountKey;

	private String douponDiscountKey;

	private String emailAddressKey;

	private String externalSourceKey;

	private String firstNameKey;

	private String geoipCountryKey;

	private String geoipCountryIso2Key;

	private String giftCertificateAmountKey;

	private String handlingCostExTaxKey;

	private String handlingCostIncTaxKey;

	private String handlingCostTaxKey;

	private String ipAddressKey;

	private String itemsShippedKey;

	private String itemsTotalKey;

	private String lastNameKey;

	private String orderIdKey;

	private String orderIsDigitalKey;

	private String orderSourceKey;

	private String paymentMethodKey;

	private String paymentStatusKey;

	private String phoneKey;

	private String refundedAmountKey;

	private String saleAmountKey;

	private String shippingCostExTaxKey;

	private String shippingCostTaxKey;

	private String staffNotesKey;

	private String stateKey;

	private String statusKey;

	private String storeCreditAmountKey;

	private String subtotalIncTaxKey;

	private String subtotalTaxKey;

	private String totalExTaxKey;

	private String wrappingCostExTaxKey;

	private String wrappingCostIncTaxKey;

	private String wrappingCostTaxKey;

	private String zipKey;
	
	private int addedRecordCount = 0;
	private int updatedRecordCount = 0;
	
	private String minOrderID;
	
	private int processedMaxID;

	private int syncCap = 6000;

	private String syncOptionKey;

	private String syncStartingIdKey;

	private String syncOption;

	private String syncStartingId;

	private String errorActionKey;

	private String errorMessageKey;

	private String errorStatusKey;

	private String errorCreatedDateKey;

	private String errorProcessNameKey;

	//private OrderItemSubProcess subProcess;

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
	public Object resume(IExecutionEngine engine, Object parameters)
			throws SuspendProcessException, ExecutionException,
			AccessDeniedException {
		
		inform("Resume");

		logMessageToBSV(engine, "Sync", "Resume Order Sync", "Ok");
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
			this.boOrder = null;
			if(parameters.length > 1) {
				this.boOrder = (IEntity) parameters[1];
			}
		}
		
		this.storeUrlKey = (keys.getProperty("storeUrl") != null) ? keys.getProperty("storeUrl") : "storeUrl";
		this.usernameKey = (keys.getProperty("username") != null) ? keys.getProperty("username") : "username";
		this.passwordKey = (keys.getProperty("password") != null) ? keys.getProperty("password") : "password";
		this.syncStatusKey = (keys.getProperty("sync_status") != null) ? keys.getProperty("sync_status") : "sync_status";
		this.syncOptionKey = (keys.getProperty("syncOption") != null) ? keys.getProperty("syncOption") : "sync_option";
		this.syncStartingIdKey = (keys.getProperty("sync_starting_id") != null) ? keys.getProperty("sync_starting_id") : "sync_starting_id";
		
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
						
			syncStatus = (String) boSettings.getAttributeValue(syncStatusKey);
			inform(syncStatusKey + " (syncStatus) --> " + syncStatus);
						
			syncOption = (String) boSettings.getAttributeValue(syncOptionKey);
			inform(syncOptionKey + " (syncOption) --> " + syncOption);
						
			syncStartingId = (String) boSettings.getAttributeValue(syncStartingIdKey);
			inform(syncStartingIdKey + " (syncStartingId) --> " + syncStartingId);
						
			// Error BO Name
			errorBoName = (keys.getProperty("errorBoNameKey") != null) ? keys.getProperty("errorBoNameKey") : "web_service_logs";			
			inform(" (Error BO Name) --> " + errorBoName);		

			// BO Name
			boName = (keys.getProperty("boNameKey") != null) ? keys.getProperty("boNameKey") : "e_orders";			
			inform(" (BO Name) --> " + boName);		
			// String settingsBo = (keys.getProperty("settingsBo") != null) ? keys.getProperty("settingsBo") : "settingsBo";
			
			// Address_One
			addressOneKey = (keys.getProperty("Address_OneKey") != null) ? keys.getProperty("Address_OneKey") : "Address_One";

			// Address_Two
			addressTwoKey = (keys.getProperty("Address_TwoKey") != null) ? keys.getProperty("Address_TwoKey") : "Address_Two";

			// Base_Handling_Cost
			baseHandlingCostKey = (keys.getProperty("Base_Handling_CostKey") != null) ? keys.getProperty("Base_Handling_CostKey") : "Base_Handling_Cost";

			// Base_Shipping_Cost
			baseShippingCostKey = (keys.getProperty("Base_Shipping_CostKey") != null) ? keys.getProperty("Base_Shipping_CostKey") : "Base_Shipping_Cost";

			// Base_Wrapping_Cost
			baseWrappingCostKey = (keys.getProperty("Base_Wrapping_CostKey") != null) ? keys.getProperty("Base_Wrapping_CostKey") : "Base_Wrapping_Cost";

			// City
			cityKey = (keys.getProperty("CityKey") != null) ? keys.getProperty("CityKey") : "City";

			// Company
			companyKey = (keys.getProperty("CompanyKey") != null) ? keys.getProperty("CompanyKey") : "Company";

			// cost_shipping
			costShippingKey = (keys.getProperty("cost_shippingKey") != null) ? keys.getProperty("cost_shippingKey") : "cost_shipping";

			// cost_subtotal
			costSubtotalKey = (keys.getProperty("cost_subtotalKey") != null) ? keys.getProperty("cost_subtotalKey") : "cost_subtotal";

			// cost_tax
			costTaxKey = (keys.getProperty("cost_taxKey") != null) ? keys.getProperty("cost_taxKey") : "cost_tax";

			// Country
			countryKey = (keys.getProperty("CountryKey") != null) ? keys.getProperty("CountryKey") : "Country";

			// Currency_code
			currencyCodeKey = (keys.getProperty("Currency_codeKey") != null) ? keys.getProperty("Currency_codeKey") : "Currency_code";

			// Currency_Exchange_Rate
			currencyExchangeRateKey = (keys.getProperty("Currency_Exchange_RateKey") != null) ? keys.getProperty("Currency_Exchange_RateKey") : "Currency_Exchange_Rate";

			// Customer_Message
			customerMessageKey = (keys.getProperty("Customer_MessageKey") != null) ? keys.getProperty("Customer_MessageKey") : "Customer_Message";

			// date_shipped
			dateShippedKey = (keys.getProperty("date_shippedKey") != null) ? keys.getProperty("date_shippedKey") : "date_shipped";

			// date_created
			dateCreatedKey = (keys.getProperty("date_createdKey") != null) ? keys.getProperty("date_createdKey") : "date_created";

			// date_updated
			dateUpdatedKey = (keys.getProperty("date_updatedKey") != null) ? keys.getProperty("date_updatedKey") : "date_updated";

			// Default_Currency_Code
			defaultCurrencyCodeKey = (keys.getProperty("Default_Currency_CodeKey") != null) ? keys.getProperty("Default_Currency_CodeKey") : "Default_Currency_Code";
			
			// Discount_Amount
			discountAmountKey = (keys.getProperty("Discount_AmountKey") != null) ? keys.getProperty("Discount_AmountKey") : "Discount_Amount";

			// Doupon_Discount
			douponDiscountKey = (keys.getProperty("Doupon_DiscountKey") != null) ? keys.getProperty("Doupon_DiscountKey") : "Doupon_Discount";

			// Email_Address
			emailAddressKey = (keys.getProperty("Email_AddressKey") != null) ? keys.getProperty("Email_AddressKey") : "Email_Address";

			// External_Source
			externalSourceKey = (keys.getProperty("External_SourceKey") != null) ? keys.getProperty("External_SourceKey") : "External_Source";

			// First_Name
			firstNameKey = (keys.getProperty("First_NameKey") != null) ? keys.getProperty("First_NameKey") : "First_Name";

			// Geoip_Country
			geoipCountryKey = (keys.getProperty("Geoip_CountryKey") != null) ? keys.getProperty("Geoip_CountryKey") : "Geoip_Country";

			// Geoip_Country_iso2
			geoipCountryIso2Key = (keys.getProperty("Geoip_Country_iso2Key") != null) ? keys.getProperty("Geoip_Country_iso2Key") : "Geoip_Country_iso2";

			// Gift_Certificate_Amount
			giftCertificateAmountKey = (keys.getProperty("Gift_Certificate_AmountKey") != null) ? keys.getProperty("Gift_Certificate_AmountKey") : "Gift_Certificate_Amount";

			// Handling_Cost_Ex_Tax
			handlingCostExTaxKey = (keys.getProperty("Handling_Cost_Ex_TaxKey") != null) ? keys.getProperty("Handling_Cost_Ex_TaxKey") : "Handling_Cost_Ex_Tax";

			// Handling_Cost_Inc_Tax
			handlingCostIncTaxKey = (keys.getProperty("Handling_Cost_Inc_TaxKey") != null) ? keys.getProperty("Handling_Cost_Inc_TaxKey") : "Handling_Cost_Inc_Tax";

			// Handling_Cost_Tax
			handlingCostTaxKey = (keys.getProperty("Handling_Cost_TaxKey") != null) ? keys.getProperty("Handling_Cost_TaxKey") : "Handling_Cost_Tax";

			// ip_address
			ipAddressKey = (keys.getProperty("ip_addressKey") != null) ? keys.getProperty("ip_addressKey") : "ip_address";

			// Items_Shipped
			itemsShippedKey = (keys.getProperty("Items_ShippedKey") != null) ? keys.getProperty("Items_ShippedKey") : "Items_Shipped";

			// Items_Total
			itemsTotalKey = (keys.getProperty("Items_TotalKey") != null) ? keys.getProperty("Items_TotalKey") : "Items_Total";

			// Last_Name
			lastNameKey = (keys.getProperty("Last_NameKey") != null) ? keys.getProperty("Last_NameKey") : "Last_Name";

			// order_id
			orderIdKey = (keys.getProperty("order_idKey") != null) ? keys.getProperty("order_idKey") : "order_id";

			// Order_Is_Digital
			orderIsDigitalKey = (keys.getProperty("Order_Is_DigitalKey") != null) ? keys.getProperty("Order_Is_DigitalKey") : "Order_Is_Digital";

			// Order_Source
			orderSourceKey = (keys.getProperty("Order_SourceKey") != null) ? keys.getProperty("Order_SourceKey") : "Order_Source";

			// Payment_Method
			paymentMethodKey = (keys.getProperty("Payment_MethodKey") != null) ? keys.getProperty("Payment_MethodKey") : "Payment_Method";

			// Payment_Status
			paymentStatusKey = (keys.getProperty("Payment_StatusKey") != null) ? keys.getProperty("Payment_StatusKey") : "Payment_Status";

			// Phone
			phoneKey = (keys.getProperty("PhoneKey") != null) ? keys.getProperty("PhoneKey") : "Phone";

			// Refunded_Amount
			refundedAmountKey = (keys.getProperty("Refunded_AmountKey") != null) ? keys.getProperty("Refunded_AmountKey") : "Refunded_Amount";

			// sale_amount
			saleAmountKey = (keys.getProperty("sale_amountKey") != null) ? keys.getProperty("sale_amountKey") : "sale_amount";

			// Shipping_Cost_Ex_Tax
			shippingCostExTaxKey = (keys.getProperty("Shipping_Cost_Ex_TaxKey") != null) ? keys.getProperty("Shipping_Cost_Ex_TaxKey") : "Shipping_Cost_Ex_Tax";

			// Shipping_Cost_Tax
			shippingCostTaxKey = (keys.getProperty("Shipping_Cost_TaxKey") != null) ? keys.getProperty("Shipping_Cost_TaxKey") : "Shipping_Cost_Tax";

			// Staff_Notes
			staffNotesKey = (keys.getProperty("Staff_NotesKey") != null) ? keys.getProperty("Staff_NotesKey") : "Staff_Notes";

			// State
			stateKey = (keys.getProperty("StateKey") != null) ? keys.getProperty("StateKey") : "State";

			// status
			statusKey = (keys.getProperty("statusKey") != null) ? keys.getProperty("statusKey") : "status";

			// Store_Credit_Amount
			storeCreditAmountKey = (keys.getProperty("Store_Credit_AmountKey") != null) ? keys.getProperty("Store_Credit_AmountKey") : "Store_Credit_Amount";

			// Subtotal_Inc_Tax
			subtotalIncTaxKey = (keys.getProperty("Subtotal_Inc_TaxKey") != null) ? keys.getProperty("Subtotal_Inc_TaxKey") : "Subtotal_Inc_Tax";

			// Subtotal_Tax
			subtotalTaxKey = (keys.getProperty("Subtotal_TaxKey") != null) ? keys.getProperty("Subtotal_TaxKey") : "Subtotal_Tax";

			// Total_Ex_Tax
			totalExTaxKey = (keys.getProperty("Total_Ex_TaxKey") != null) ? keys.getProperty("Total_Ex_TaxKey") : "Total_Ex_Tax";

			// Wrapping_Cost_Ex_Tax
			wrappingCostExTaxKey = (keys.getProperty("Wrapping_Cost_Ex_TaxKey") != null) ? keys.getProperty("Wrapping_Cost_Ex_TaxKey") : "Wrapping_Cost_Ex_Tax";

			// Wrapping_Cost_Inc_Tax
			wrappingCostIncTaxKey = (keys.getProperty("Wrapping_Cost_Inc_TaxKey") != null) ? keys.getProperty("Wrapping_Cost_Inc_TaxKey") : "Wrapping_Cost_Inc_Tax";

			// Wrapping_Cost_Tax
			wrappingCostTaxKey = (keys.getProperty("Wrapping_Cost_TaxKey") != null) ? keys.getProperty("Wrapping_Cost_TaxKey") : "Wrapping_Cost_Tax";

			// Zip
			zipKey = (keys.getProperty("ZipKey") != null) ? keys.getProperty("ZipKey") : "Zip";

			String cap = (keys.getProperty("sync_cap") != null) ? keys.getProperty("sync_cap") : "sync_cap";
			
			try {
				if(cap != null && !cap.isEmpty()) {
					this.syncCap = Integer.parseInt(cap);
				}
			} catch (Exception e) { }
			
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
		
		logMessageToBSV(engine, "Sync", "Starting Order Sync", "Ok");
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
					inform("Hit " + syncCap + " records cap. Order Process is stopped.");
					return;
				} else {
					//inform("Free memory: " + (freeMemory / 1024 / 1024) + "Mb");
					//inform("Max memory: " + (maxMemory / 1024 / 1024) + "Mb");
					inform("Sleep 10 sec");
					Thread.sleep(20000);
				}
			}
			
			BigCommerceConnector bcConnector = new BigCommerceConnector(storeUrl, username, password);
			OrderItemSubProcess subProcess = new OrderItemSubProcess();
			
			String urlPath = "orders";
			
	    	if(boOrder != null) {
				String orderId = (String) boOrder.getAttributeValue(orderIdKey);
	    		urlPath = urlPath + "/" + orderId;

	    		@SuppressWarnings({ "unchecked" })
				List<Order> orderList = bcConnector.serviceGET(urlPath, "", new ProductJsonParser());
	    		if(orderList.isEmpty()) {
	    			// Customer not found
	    			return;
	    		}
	    		

	    		dataSyncSingle(engine, subProcess, bcConnector, storeUrl, boOrder, orderList.get(0));

	    		updatedRecordCount++;
	    		
	    	} else {
	    		/*
	    		 * For sync everything, only take records that are later than last modified date
	    		 */
	    		
  			 	 syncOrderByID(engine, subProcess, bcConnector);
	    		    		
	    		 if(!this.m_cancelled) {
		    		 syncOrderByModifiedDate(engine, subProcess, bcConnector);
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
		} catch (ParseException e) {
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
		logMessageToBSV(engine, "Sync", "Finished Order Sync, " + message, "Ok");
						
	}
	
	private void syncOrderByID(IExecutionEngine engine, OrderItemSubProcess subProcess, 
			BigCommerceConnector bcConnector) throws InvalidParameterException, AccessDeniedException, 
					ServerTimeOutException, ExecutionException, SuspendProcessException, 
					BigCommerceException, ParseException, InvalidTypeException {
		
		inform("Sync by min order ID");
		
		if(this.page2 == 1) {
			this.minOrderID = getMinOrderID(engine);
				
			if(this.minOrderID != null && !this.minOrderID.equals("")) {				
				this.processedMaxID = Integer.parseInt(minOrderID);
			}			
			
		}
		
		String urlPath = "orders";
		if(syncStatus != null) {
			int statusId = getOrderStatusId(syncStatus);
			if(statusId > -1) {
				urlPath += "?limit=100&status_id=" + statusId;
				urlPath += "&min_id=" + minOrderID;
			}
		} else {
			urlPath += "?limit=100&min_id=" + minOrderID;
		}
		
		while(true) {
			
			if(this.m_cancelled) {
				inform("Process has been cancelled");
				//inform("Total records synced: " + processedRecordCount);
				return;
			}
			
    		String pagedUrlPath = urlPath + "&page=" + page2;
    		inform("BC URL: " + pagedUrlPath);
    		
    		@SuppressWarnings({ "unchecked" })
			List<Order> orderList = bcConnector.serviceGET(pagedUrlPath, "", new OrderJsonParser());
			if(orderList.isEmpty()) {
				// Finish
				break;
			}
			
			// Store the max customer ID
			for(Order rec: orderList) {
				if(rec.getOrderId() != null && !rec.getOrderId().equals("")) {
				
					int value = Integer.parseInt(rec.getOrderId());
				
					if(value > this.processedMaxID) {
						this.processedMaxID = value;
					}
				}			
			}

			int processed = dataSyncMultiple(engine, subProcess, bcConnector, urlPath, orderList);
			
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

	private void syncOrderByModifiedDate(IExecutionEngine engine, OrderItemSubProcess subProcess, 
			BigCommerceConnector bcConnector) throws InvalidParameterException, AccessDeniedException, 
					ServerTimeOutException, ExecutionException, SuspendProcessException, 
					BigCommerceException, ParseException, InvalidTypeException {
		
		if(this.page == 1) {
			this.lastDate = getLastSyncDate(engine);
			inform("Get last Date: " + lastDate);
		}
		
		String urlPath = "orders";
		if (syncStatus != null) {
			int statusId = getOrderStatusId(syncStatus);
			if (statusId > -1) {
				urlPath = urlPath + "?limit=100&status_id=" + statusId;
			}
		} else if (this.lastDate == null) {
			// Do nothing
			return;
		} else {
			urlPath = urlPath + "?limit=100&min_date_modified=" + this.lastDate;			
		}

		while(true) {
			
			if(this.m_cancelled) {
				inform("Process has been cancelled");
				return;
			}

    		String pagedUrlPath = urlPath + "&page=" + page;
    		inform("BC URL: " + pagedUrlPath);

    		@SuppressWarnings({ "unchecked" })
			List<Order> orderList = bcConnector.serviceGET(pagedUrlPath, "", new OrderJsonParser());
			if(orderList.isEmpty()) {
				// Finish
				return;
			}
			
			// Filter out order with ID that does not match.
			for(int i=orderList.size(); i>0; i--) {
				Order rec = orderList.get(i-1);
				if(rec.getOrderId() != null && !rec.getOrderId().equals("")) {
					
					int value = Integer.parseInt(rec.getOrderId());
				
					if(value > this.processedMaxID) {
						orderList.remove(i-1);
					}
				}			
				
			}
			
			int processed = dataSyncMultiple(engine, subProcess, bcConnector, urlPath, orderList);
			updatedRecordCount += processed;
			
			page++;
			
			/*
			 * Suspend if max page is reached
			 */
			if(page % MAX_PAGE_SUSPEND == 0) {
				inform("suspend process");
				throw new SuspendProcessException(true);
			}
			
		}
	}
	
	private void dataSyncSingle(IExecutionEngine engine, OrderItemSubProcess subProcess,
			BigCommerceConnector bcConnector, String storeUrl,IEntity boInstance, Order jsonOrder) 
				throws AccessDeniedException, ServerTimeOutException, ExecutionException, 
				       InvalidParameterException, BigCommerceException, ParseException  {
		
		try {

			inform(" Created BO --> " + boInstance.getId());

			updateOrder(keys, jsonOrder, boInstance);

			inform("Updating the orders bo ");
			engine.updateEntity(this, boInstance, null, null, null);
			inform("orders bo updated");
			
			inform("Calling order items subprocess");
			
			subProcess.syncData(this, engine, bcConnector, jsonOrder.getOrderId(), boInstance);
			inform("Done order items subprocess");

		} catch (AccessDeniedException | ServerTimeOutException | ExecutionException | InvalidParameterException  e) {
			error(" ", e);	
			throw e;
		} catch (BigCommerceException e) {
			error(" ", e);
			throw e;
		} catch (ParseException e) {
			error(" ", e);
			throw e;
		}
			
	}
	
	private int dataSyncMultiple(IExecutionEngine engine, OrderItemSubProcess subProcess,
			BigCommerceConnector bcConnector, String storeUrl, List<Order> orderList)  
					throws AccessDeniedException, ServerTimeOutException, ExecutionException, 
					       InvalidParameterException, BigCommerceException, ParseException {
		
		int counter = 0;
    	for(Order rec: orderList) {
    		
			if(this.m_cancelled) {
				inform("Process has been cancelled");
				return counter;
			}

			try {
	    		IEntity boInstance = getEntityByOrderID(engine, keys, rec.getOrderId());
	    		if(boInstance == null) {
	    			boInstance = engine.createEntity(this, boName);
	    		}
	    		
	    		dataSyncSingle(engine, subProcess, bcConnector, storeUrl, boInstance, rec);
	    		
    		} catch (AccessDeniedException | ServerTimeOutException | ExecutionException| InvalidParameterException e) {
    			error(" ", e);
    			throw e;
    		} catch (BigCommerceException e) {
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
		
	private String getMinOrderIdQuery() {
		
		// Cache the query for reuse
		if(minOrderIdQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ALL ");
	
			buff.append(boName);
			buff.append(" ORDER BY ");
			buff.append(boName);
			buff.append(".");
			
			buff.append(orderIdKey);
			buff.append(" DESC TAKE BEST 1");
			
			this.minOrderIdQueryString = buff.toString();
			
		}
		
		inform("Min Customer Query: " + minOrderIdQueryString);
		
		return minOrderIdQueryString;
	}
	
	/*
	 * Get the customer by customer id
	 */
	private String getMinOrderID(IExecutionEngine engine) throws ParseException, 
				ExecutionException, AccessDeniedException, InvalidParameterException, InvalidTypeException {
		
		try {
			
			Query customQuery = Query.createFromRuleLanguageString(getMinOrderIdQuery());
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				Object dat = data[0].getAttributeValue(orderIdKey);
				if(dat instanceof Long) {
					Long ordId = (Long)dat;
					inform("Found Min Order ID: " + ordId);
					
					return ordId.toString();
				} else {
					String typeName = dat.getClass().getName();
					throw new InvalidTypeException("Expected " + orderIdKey + " field to be Number, but it is " + typeName);
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
	
	private int getOrderStatusId(String status) {
		if("Incomplete".equalsIgnoreCase(status)) {
			return 0;
		} else if("Pending".equalsIgnoreCase(status)) {
			return 1;
		} else if("Shipped".equalsIgnoreCase(status)) {
			return 2;
		} else if("Partially Shipped".equalsIgnoreCase(status)) {
			return 3;
		} else if("Refunded".equalsIgnoreCase(status)) {
			return 4;
		} else if("Cancelled".equalsIgnoreCase(status)) {
			return 5;
		} else if("Declined".equalsIgnoreCase(status)) {
			return 6;
		} else if("Awaiting Payment".equalsIgnoreCase(status)) {
			return 7;
		} else if("Awaiting Pickup".equalsIgnoreCase(status)) {
			return 8;
		} else if("Awaiting Shipment".equalsIgnoreCase(status)) {
			return 9;
		} else if("Completed".equalsIgnoreCase(status)) {
			return 10;
		} else if("Awaiting Fulfillment".equalsIgnoreCase(status)) {
			return 11;
		} else if("Manual Verification Required".equalsIgnoreCase(status)) {
			return 12;
		} else if("Disputed".equalsIgnoreCase(status)) {
			return 13;
		} else {
			return -1;
		}

	}

	private String getOrderByIdQuery(Properties keys, String id) {
		
		// Cache the query for reuse
		if(customQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ");
	
			buff.append(boName);
			buff.append(" WHERE ");
			buff.append(boName);
			buff.append(".");
			
			buff.append(orderIdKey);
			buff.append("=");
			
			this.customQueryString = buff.toString();
			
		}
		
		//String q = this.customQueryString + "'" + id + "'";
		String q = this.customQueryString + id;
		inform("Custom Rule Query: " + q);
		
		return q;
	}

	/*
	 * Get the order by order id
	 */
	private IEntity getEntityByOrderID(IExecutionEngine engine, Properties keys, String orderId) 
				throws ParseException, ExecutionException, AccessDeniedException, InvalidParameterException {
		
		try {
			Query customQuery = Query.createFromRuleLanguageString(getOrderByIdQuery(keys, orderId));
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				inform("Found Order ID: " + data[0].getAttributeValue(orderIdKey));
				
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
	
	private String getLastSyncDate(IExecutionEngine engine) 
			throws InvalidParameterException, ParseException, ExecutionException, 
					InvalidTypeException, AccessDeniedException {
		
		try {
			Query customQuery = Query.createFromRuleLanguageString(getLastDateQuery());
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			//String namedQuery = (keys.getProperty("named_order_time_query") != null) ? keys.getProperty("named_order_time_query") : "get_order_max_modified_time";
			//inform("Named Query: " + namedQuery);
			//QueryResult result = engine.executeNamedQuery(this, namedQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				Object dat = data[0].getAttributeValue(dateUpdatedKey);
				if(dat instanceof Date) {
					Date updateDate = (Date)data[0].getAttributeValue(dateUpdatedKey);
					
					if(updateDate != null) {
						inform("Found Last Sync Date: " + updateDate);
						return getISO8601Date(updateDate);
					}
					
				} else if ( dat instanceof DateTimeHolder) {
					DateTimeHolder updateDate;
					updateDate = (DateTimeHolder)data[0].getAttributeValue(dateUpdatedKey);
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
		
	private void updateOrder(Properties keys, Order orderJsonObj, IEntity boInstance) throws InvalidParameterException {
		// Address_One
		String addressOne = orderJsonObj.getAddressOne();
		inform(addressOneKey + " (Address_One Key) --> " + addressOne);
		boInstance.setAttributeValue(addressOneKey, addressOne);

		// Address_Two
		String addressTwo = orderJsonObj.getAddressTwo();
		inform(addressTwoKey + " (Address_Two Key) --> " + addressTwo);
		boInstance.setAttributeValue(addressTwoKey, addressTwo);

		// Base_Handling_Cost
		Double baseHandlingCost = orderJsonObj.getBaseHandlingCost();
		inform(baseHandlingCostKey + " (Base_Handling_Cost Key) --> " + baseHandlingCost);
		boInstance.setAttributeValue(baseHandlingCostKey, baseHandlingCost.toString());

		// Base_Shipping_Cost
		Double baseShippingCost = orderJsonObj.getBaseShippingCost();
		inform(baseShippingCostKey + " (Base_Shipping_Cost Key) --> " + baseShippingCost);
		boInstance.setAttributeValue(baseShippingCostKey, baseShippingCost.toString());

		// Base_Wrapping_Cost
		Double baseWrappingCost = orderJsonObj.getBaseWrappingCost();
		inform(baseWrappingCostKey + " (Base_Wrapping_Cost Key) --> " + baseWrappingCost);
		boInstance.setAttributeValue(baseWrappingCostKey, baseWrappingCost.toString());

		// City
		String city = orderJsonObj.getCity();
		inform(cityKey + " (City Key) --> " + city);
		boInstance.setAttributeValue(cityKey, city);

		// Company
		String company = orderJsonObj.getCompany();
		inform(companyKey + " (Company Key) --> " + company);
		boInstance.setAttributeValue(companyKey, company);

		// cost_shipping
		String costShipping = orderJsonObj.getCostShipping();
		inform(costShippingKey + " (cost_shipping Key) --> " + costShipping);
		boInstance.setAttributeValue(costShippingKey, costShipping);

		// cost_subtotal
		String costSubtotal = orderJsonObj.getCostSubtotal();
		inform(costSubtotalKey + " (cost_subtotal Key) --> " + costSubtotal);
		boInstance.setAttributeValue(costSubtotalKey, costSubtotal);

		// cost_tax
		String costTax = orderJsonObj.getCostTax();
		inform(costTaxKey + " (cost_tax Key) --> " + costTax);
		boInstance.setAttributeValue(costTaxKey, costTax);

		// Country
		String country = orderJsonObj.getCountry();
		inform(countryKey + " (Country Key) --> " + country);
		boInstance.setAttributeValue(countryKey, country);

		// Currency_code
		String currencyCode = orderJsonObj.getCurrencyCode();
		inform(currencyCodeKey + " (Currency_code Key) --> " + currencyCode);
		boInstance.setAttributeValue(currencyCodeKey, currencyCode);

		// Currency_Exchange_Rate
		String currencyExchangeRate = orderJsonObj.getCurrencyExchangeRate();
		inform(currencyExchangeRateKey + " (Currency_Exchange_Rate Key) --> " + currencyExchangeRate);
		boInstance.setAttributeValue(currencyExchangeRateKey, currencyExchangeRate);

		// Customer_Message
		String customerMessage = orderJsonObj.getCustomerMessage();
		inform(customerMessageKey + " (Customer_Message Key) --> " + customerMessage);
		boInstance.setAttributeValue(customerMessageKey, customerMessage);

		// date_shipped
		String dateShipped = orderJsonObj.getDateShipped();
		inform(dateShippedKey + " (date_shipped Key) --> " + dateShipped);
		boInstance.setAttributeValue(dateShippedKey, dateShipped);

		SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");

		try {
			// date_created
			String dateCreated = orderJsonObj.getDateCreated();
			inform(dateCreatedKey + " (date_created Key) --> " + dateCreated);
			boInstance.setAttributeValue(dateCreatedKey, format.parse(dateCreated));
		} catch (Exception e) {
			error(" ", e);
		}

		try {
			// date_updated
			String dateUpdated = orderJsonObj.getDateUpdated();
			inform(dateUpdatedKey + " (date_updated Key) --> " + dateUpdated);
			boInstance.setAttributeValue(dateUpdatedKey, format.parse(dateUpdated));
		} catch (Exception e) {
			error(" ", e);
		}

		// Default_Currency_Code
		String defaultCurrencyCode = orderJsonObj.getDefaultCurrencyCode();
		inform(defaultCurrencyCodeKey + " (Default_Currency_Code Key) --> " + defaultCurrencyCode);
		boInstance.setAttributeValue(defaultCurrencyCodeKey, defaultCurrencyCode);

		// Discount_Amount
		Double discountAmount = orderJsonObj.getDiscountAmount();
		inform(discountAmountKey + " (Discount_Amount Key) --> " + discountAmount);
		boInstance.setAttributeValue(discountAmountKey, discountAmount.toString());

		// Doupon_Discount
		String douponDiscount = orderJsonObj.getDouponDiscount();
		inform(douponDiscountKey + " (Doupon_Discount Key) --> " + douponDiscount);
		boInstance.setAttributeValue(douponDiscountKey, douponDiscount);

		// Email_Address
		String emailAddress = orderJsonObj.getEmailAddress();
		inform(emailAddressKey + " (Email_Address Key) --> " + emailAddress);
		boInstance.setAttributeValue(emailAddressKey, emailAddress);

		// External_Source
		String externalSource = orderJsonObj.getExternalSource();
		inform(externalSourceKey + " (External_Source Key) --> " + externalSource);
		boInstance.setAttributeValue(externalSourceKey, externalSource);

		// First_Name
		String firstName = orderJsonObj.getFirstName();
		inform(firstNameKey + " (First_Name Key) --> " + firstName);
		boInstance.setAttributeValue(firstNameKey, firstName);

		// Geoip_Country
		String geoipCountry = orderJsonObj.getGeoipCountry();
		inform(geoipCountryKey + " (Geoip_Country Key) --> " + geoipCountry);
		boInstance.setAttributeValue(geoipCountryKey, geoipCountry);

		// Geoip_Country_iso2
		String geoipCountryIso2 = orderJsonObj.getGeoipCountryIso2();
		inform(geoipCountryIso2Key + " (Geoip_Country_iso2 Key) --> " + geoipCountryIso2);
		boInstance.setAttributeValue(geoipCountryIso2Key, geoipCountryIso2);

		// Gift_Certificate_Amount
		String giftCertificateAmount = orderJsonObj.getGiftCertificateAmount();
		inform(giftCertificateAmountKey + " (Gift_Certificate_Amount Key) --> " + giftCertificateAmount);
		boInstance.setAttributeValue(giftCertificateAmountKey, giftCertificateAmount);

		// Handling_Cost_Ex_Tax
		Double handlingCostExTax = orderJsonObj.getHandlingCostExTax();
		inform(handlingCostExTaxKey + " (Handling_Cost_Ex_Tax Key) --> " + handlingCostExTax);
		boInstance.setAttributeValue(handlingCostExTaxKey, handlingCostExTax.toString());

		// Handling_Cost_Inc_Tax
		Double handlingCostIncTax = orderJsonObj.getHandlingCostIncTax();
		inform(handlingCostIncTaxKey + " (Handling_Cost_Inc_Tax Key) --> " + handlingCostIncTax);
		boInstance.setAttributeValue(handlingCostIncTaxKey, handlingCostIncTax.toString());

		// Handling_Cost_Tax
		String handlingCostTax = orderJsonObj.getHandlingCostTax();
		inform(handlingCostTaxKey + " (Handling_Cost_Tax Key) --> " + handlingCostTax);
		boInstance.setAttributeValue(handlingCostTaxKey, handlingCostTax);

		// ip_address
		String ipAddress = orderJsonObj.getIpAddress();
		inform(ipAddressKey + " (ip_address Key) --> " + ipAddress);
		boInstance.setAttributeValue(ipAddressKey, ipAddress);

		// Items_Shipped
		String itemsShipped = orderJsonObj.getItemsShipped();
		inform(itemsShippedKey + " (Items_Shipped Key) --> " + itemsShipped);
		boInstance.setAttributeValue(itemsShippedKey, itemsShipped);

		// Items_Total
		String itemsTotal = orderJsonObj.getItemsTotal();
		inform(itemsTotalKey + " (Items_Total Key) --> " + itemsTotal);
		boInstance.setAttributeValue(itemsTotalKey, itemsTotal);

		// Last_Name
		String lastName = orderJsonObj.getLastName();
		inform(lastNameKey + " (Last_Name Key) --> " + lastName);
		boInstance.setAttributeValue(lastNameKey, lastName);

		// order_id
		String orderId = orderJsonObj.getOrderId();
		inform(orderIdKey + " (order_id Key) --> " + orderId);
		boInstance.setAttributeValue(orderIdKey, orderId);

		// Order_Is_Digital
		String orderIsDigital = orderJsonObj.getOrderIsDigital();
		inform(orderIsDigitalKey + " (Order_Is_Digital Key) --> " + orderIsDigital);
		boInstance.setAttributeValue(orderIsDigitalKey, orderIsDigital);

		// Order_Source
		String orderSource = orderJsonObj.getOrderSource();
		inform(orderSourceKey + " (Order_Source Key) --> " + orderSource);
		boInstance.setAttributeValue(orderSourceKey, orderSource);

		// Payment_Method
		String paymentMethod = orderJsonObj.getPaymentMethod();
		inform(paymentMethodKey + " (Payment_Method Key) --> " + paymentMethod);
		boInstance.setAttributeValue(paymentMethodKey, paymentMethod);

		// Payment_Status
		String paymentStatus = orderJsonObj.getPaymentStatus();
		inform(paymentStatusKey + " (Payment_Status Key) --> " + paymentStatus);
		boInstance.setAttributeValue(paymentStatusKey, paymentStatus);

		// Phone
		String phone = orderJsonObj.getPhone();
		inform(phoneKey + " (Phone Key) --> " + phone);
		boInstance.setAttributeValue(phoneKey, phone);

		// Refunded_Amount
		Double refundedAmount = orderJsonObj.getRefundedAmount();
		inform(refundedAmountKey + " (Refunded_Amount Key) --> " + refundedAmount);
		boInstance.setAttributeValue(refundedAmountKey, refundedAmount.toString());

		// sale_amount
		String saleAmount = orderJsonObj.getSaleAmount();
		inform(saleAmountKey + " (sale_amount Key) --> " + saleAmount);
		boInstance.setAttributeValue(saleAmountKey, saleAmount);

		// Shipping_Cost_Ex_Tax
		Double shippingCostExTax = orderJsonObj.getShippingCostExTax();
		inform(shippingCostExTaxKey + " (Shipping_Cost_Ex_Tax Key) --> " + shippingCostExTax);
		boInstance.setAttributeValue(shippingCostExTaxKey, shippingCostExTax.toString());

		// Shipping_Cost_Tax
		String shippingCostTax = orderJsonObj.getShippingCostTax();
		inform(shippingCostTaxKey + " (Shipping_Cost_Tax Key) --> " + shippingCostTax);
		boInstance.setAttributeValue(shippingCostTaxKey, shippingCostTax);

		// Staff_Notes
		String staffNotes = orderJsonObj.getStaffNotes();
		inform(staffNotesKey + " (Staff_Notes Key) --> " + staffNotes);
		boInstance.setAttributeValue(staffNotesKey, staffNotes);

		// State
		String state = orderJsonObj.getState();
		inform(stateKey + " (State Key) --> " + state);
		boInstance.setAttributeValue(stateKey, state);

		// status
		String status = orderJsonObj.getStatus();
		inform(statusKey + " (status Key) --> " + status);
		boInstance.setAttributeValue(statusKey, status);

		// Store_Credit_Amount
		String storeCreditAmount = orderJsonObj.getStoreCreditAmount();
		inform(storeCreditAmountKey + " (Store_Credit_Amount Key) --> " + storeCreditAmount);
		boInstance.setAttributeValue(storeCreditAmountKey, storeCreditAmount);

		// Subtotal_Inc_Tax
		Double subtotalIncTax = orderJsonObj.getSubtotalIncTax();
		inform(subtotalIncTaxKey + " (Subtotal_Inc_Tax Key) --> " + subtotalIncTax);
		boInstance.setAttributeValue(subtotalIncTaxKey, subtotalIncTax.toString());

		// Subtotal_Tax
		String subtotalTax = orderJsonObj.getSubtotalTax();
		inform(subtotalTaxKey + " (Subtotal_Tax Key) --> " + subtotalTax);
		boInstance.setAttributeValue(subtotalTaxKey, subtotalTax);

		// Total_Ex_Tax
		Double totalExTax = orderJsonObj.getTotalExTax();
		inform(totalExTaxKey + " (Total_Ex_Tax Key) --> " + totalExTax);
		boInstance.setAttributeValue(totalExTaxKey, totalExTax.toString());

		// Wrapping_Cost_Ex_Tax
		Double wrappingCostExTax = orderJsonObj.getWrappingCostExTax();
		inform(wrappingCostExTaxKey + " (Wrapping_Cost_Ex_Tax Key) --> " + wrappingCostExTax);
		boInstance.setAttributeValue(wrappingCostExTaxKey, wrappingCostExTax.toString());

		// Wrapping_Cost_Inc_Tax
		Double wrappingCostIncTax = orderJsonObj.getWrappingCostIncTax();
		inform(wrappingCostIncTaxKey + " (Wrapping_Cost_Inc_Tax Key) --> " + wrappingCostIncTax);
		boInstance.setAttributeValue(wrappingCostIncTaxKey, wrappingCostIncTax.toString());

		// Wrapping_Cost_Tax
		String wrappingCostTax = orderJsonObj.getWrappingCostTax();
		inform(wrappingCostTaxKey + " (Wrapping_Cost_Tax Key) --> " + wrappingCostTax);
		boInstance.setAttributeValue(wrappingCostTaxKey, wrappingCostTax);

		// Zip
		String zip = orderJsonObj.getZip();
		inform(zipKey + " (Zip Key) --> " + zip);
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
		log.info("[BSV Orders] " + message);
	}
	
	private void error(String message, Throwable t) {
		//System.out.println(message);
		if( t != null) {
			log.error("[BSV Orders] " + message, t);
		} else {
			log.error("[BSV Orders] " + message);
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
	
	private String getISO8601Date(Date date) {
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String d = parser.format(date);
		return d.replace(" ", "T");
	}
	
}
