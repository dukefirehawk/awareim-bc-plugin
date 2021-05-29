package com.neural;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
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
import com.bas.shared.domain.operation.IEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neural.json.Address;
import com.neural.json.Order;
import com.neural.json.OrderItem;
import com.neural.json.ProductOption;

public class BcPlaceOrderProcess implements IProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1415681164118059629L;
	
	private static Log log = LogFactory.getLog(BcPlaceOrderProcess.class);
	
	private static final String 	PROPS_FILE = "bc_place_orders.props";
	
	private static final int		DEFAULT_STATUS_ID = 1;		// Pending
	private static final Integer	DEFAULT_CUSTOMER_ID = 0; 	// Guest

	private IEntity boSettings;

	private IEntity boOrder;

	private Properties keys;

	private String storeUrl;

	private String username;

	private String password;

	private String errorBoName;

	private String boName;

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

	private String productsKey;

	private String quantityKey;

	private String productIdKey;

	private String countryIso2Key;

	private String inventoryRefKey;

	private String inventoryOptionRefKey;

	private String inventoryOptionIdKey;

	private String inventoryOptionValueIdKey;

	private String customerRefKey;

	private String customerIdKey;

	private String errorActionKey;

	private String errorMessageKey;

	private String errorProcessNameKey;

	private String errorStatusKey;

	private String errorCreatedDateKey;
	
	@Override
	public boolean cancel() {
		// TODO Auto-generated method stub
		return false;
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
	
	private void init() throws InvalidParameterException {
		
		String storeUrlKey = (keys.getProperty("storeUrl") != null) ? keys.getProperty("storeUrl") : "storeUrl";
		String usernameKey = (keys.getProperty("username") != null) ? keys.getProperty("username") : "username";
		String passwordKey = (keys.getProperty("password") != null) ? keys.getProperty("password") : "password";
		
		this.storeUrl = (String) boSettings.getAttributeValue(storeUrlKey);
		if(!storeUrl.endsWith("/")) {
			storeUrl += "/";
		}
		inform(storeUrlKey + " (storeUrl) --> " + storeUrl);
		
		this.username = (String) boSettings.getAttributeValue(usernameKey);
		inform(usernameKey + " (username) --> " + username);
		
		this.password = (String) boSettings.getAttributeValue(passwordKey);
		inform(passwordKey + " (password) --> " + password);
					
		// Error BO Name
		this.errorBoName = (keys.getProperty("errorBoNameKey") != null) ? keys.getProperty("errorBoNameKey") : "web_service_logs";			
		inform(" (Error BO Name) --> " + errorBoName);		

		// BO Name
		this.boName = (keys.getProperty("boNameKey") != null) ? keys.getProperty("boNameKey") : "e_orders";			
		inform(" (BO Name) --> " + boName);		
		// String settingsBo = (keys.getProperty("settingsBo") != null) ? keys.getProperty("settingsBo") : "settingsBo";
		
		// Address_One
		this.addressOneKey = (keys.getProperty("Address_OneKey") != null) ? keys.getProperty("Address_OneKey") : "Address_One";

		// Address_Two
		this.addressTwoKey = (keys.getProperty("Address_TwoKey") != null) ? keys.getProperty("Address_TwoKey") : "Address_Two";

		// Base_Handling_Cost
		this.baseHandlingCostKey = (keys.getProperty("Base_Handling_CostKey") != null) ? keys.getProperty("Base_Handling_CostKey") : "Base_Handling_Cost";

		// Base_Shipping_Cost
		this.baseShippingCostKey = (keys.getProperty("Base_Shipping_CostKey") != null) ? keys.getProperty("Base_Shipping_CostKey") : "Base_Shipping_Cost";

		// Base_Wrapping_Cost
		this.baseWrappingCostKey = (keys.getProperty("Base_Wrapping_CostKey") != null) ? keys.getProperty("Base_Wrapping_CostKey") : "Base_Wrapping_Cost";

		// City
		this.cityKey = (keys.getProperty("CityKey") != null) ? keys.getProperty("CityKey") : "City";

		// Company
		this.companyKey = (keys.getProperty("CompanyKey") != null) ? keys.getProperty("CompanyKey") : "Company";

		// cost_shipping
		this.costShippingKey = (keys.getProperty("cost_shippingKey") != null) ? keys.getProperty("cost_shippingKey") : "cost_shipping";

		// cost_subtotal
		this.costSubtotalKey = (keys.getProperty("cost_subtotalKey") != null) ? keys.getProperty("cost_subtotalKey") : "cost_subtotal";

		// cost_tax
		this.costTaxKey = (keys.getProperty("cost_taxKey") != null) ? keys.getProperty("cost_taxKey") : "cost_tax";

		// Country
		this.countryKey = (keys.getProperty("CountryKey") != null) ? keys.getProperty("CountryKey") : "Country";

		// Currency_code
		this.currencyCodeKey = (keys.getProperty("Currency_codeKey") != null) ? keys.getProperty("Currency_codeKey") : "Currency_code";

		// Currency_Exchange_Rate
		this.currencyExchangeRateKey = (keys.getProperty("Currency_Exchange_RateKey") != null) ? keys.getProperty("Currency_Exchange_RateKey") : "Currency_Exchange_Rate";

		// Customer_Message
		this.customerMessageKey = (keys.getProperty("Customer_MessageKey") != null) ? keys.getProperty("Customer_MessageKey") : "Customer_Message";

		// date_shipped
		this.dateShippedKey = (keys.getProperty("date_shippedKey") != null) ? keys.getProperty("date_shippedKey") : "date_shipped";

		// date_created
		this.dateCreatedKey = (keys.getProperty("date_createdKey") != null) ? keys.getProperty("date_createdKey") : "date_created";

		// date_updated
		this.dateUpdatedKey = (keys.getProperty("date_updatedKey") != null) ? keys.getProperty("date_updatedKey") : "date_updated";

		// Default_Currency_Code
		this.defaultCurrencyCodeKey = (keys.getProperty("Default_Currency_CodeKey") != null) ? keys.getProperty("Default_Currency_CodeKey") : "Default_Currency_Code";
		
		// Discount_Amount
		this.discountAmountKey = (keys.getProperty("Discount_AmountKey") != null) ? keys.getProperty("Discount_AmountKey") : "Discount_Amount";

		// Doupon_Discount
		this.douponDiscountKey = (keys.getProperty("Doupon_DiscountKey") != null) ? keys.getProperty("Doupon_DiscountKey") : "Doupon_Discount";

		// Email_Address
		this.emailAddressKey = (keys.getProperty("Email_AddressKey") != null) ? keys.getProperty("Email_AddressKey") : "Email_Address";

		// External_Source
		this.externalSourceKey = (keys.getProperty("External_SourceKey") != null) ? keys.getProperty("External_SourceKey") : "External_Source";

		// First_Name
		this.firstNameKey = (keys.getProperty("First_NameKey") != null) ? keys.getProperty("First_NameKey") : "First_Name";

		// Geoip_Country
		this.geoipCountryKey = (keys.getProperty("Geoip_CountryKey") != null) ? keys.getProperty("Geoip_CountryKey") : "Geoip_Country";

		// Geoip_Country_iso2
		this.geoipCountryIso2Key = (keys.getProperty("Geoip_Country_iso2Key") != null) ? keys.getProperty("Geoip_Country_iso2Key") : "Geoip_Country_iso2";

		// Gift_Certificate_Amount
		this.giftCertificateAmountKey = (keys.getProperty("Gift_Certificate_AmountKey") != null) ? keys.getProperty("Gift_Certificate_AmountKey") : "Gift_Certificate_Amount";

		// Handling_Cost_Ex_Tax
		this.handlingCostExTaxKey = (keys.getProperty("Handling_Cost_Ex_TaxKey") != null) ? keys.getProperty("Handling_Cost_Ex_TaxKey") : "Handling_Cost_Ex_Tax";

		// Handling_Cost_Inc_Tax
		this.handlingCostIncTaxKey = (keys.getProperty("Handling_Cost_Inc_TaxKey") != null) ? keys.getProperty("Handling_Cost_Inc_TaxKey") : "Handling_Cost_Inc_Tax";

		// Handling_Cost_Tax
		this.handlingCostTaxKey = (keys.getProperty("Handling_Cost_TaxKey") != null) ? keys.getProperty("Handling_Cost_TaxKey") : "Handling_Cost_Tax";

		// ip_address
		this.ipAddressKey = (keys.getProperty("ip_addressKey") != null) ? keys.getProperty("ip_addressKey") : "ip_address";

		// Items_Shipped
		this.itemsShippedKey = (keys.getProperty("Items_ShippedKey") != null) ? keys.getProperty("Items_ShippedKey") : "Items_Shipped";

		// Items_Total
		this.itemsTotalKey = (keys.getProperty("Items_TotalKey") != null) ? keys.getProperty("Items_TotalKey") : "Items_Total";

		// Last_Name
		this.lastNameKey = (keys.getProperty("Last_NameKey") != null) ? keys.getProperty("Last_NameKey") : "Last_Name";

		// order_id
		this.orderIdKey = (keys.getProperty("order_idKey") != null) ? keys.getProperty("order_idKey") : "order_id";

		// Order_Is_Digital
		this.orderIsDigitalKey = (keys.getProperty("Order_Is_DigitalKey") != null) ? keys.getProperty("Order_Is_DigitalKey") : "Order_Is_Digital";

		// Order_Source
		this.orderSourceKey = (keys.getProperty("Order_SourceKey") != null) ? keys.getProperty("Order_SourceKey") : "Order_Source";

		// Payment_Method
		this.paymentMethodKey = (keys.getProperty("Payment_MethodKey") != null) ? keys.getProperty("Payment_MethodKey") : "Payment_Method";

		// Payment_Status
		this.paymentStatusKey = (keys.getProperty("Payment_StatusKey") != null) ? keys.getProperty("Payment_StatusKey") : "Payment_Status";

		// Phone
		this.phoneKey = (keys.getProperty("PhoneKey") != null) ? keys.getProperty("PhoneKey") : "Phone";

		// Refunded_Amount
		this.refundedAmountKey = (keys.getProperty("Refunded_AmountKey") != null) ? keys.getProperty("Refunded_AmountKey") : "Refunded_Amount";

		// sale_amount
		this.saleAmountKey = (keys.getProperty("sale_amountKey") != null) ? keys.getProperty("sale_amountKey") : "sale_amount";

		// Shipping_Cost_Ex_Tax
		this.shippingCostExTaxKey = (keys.getProperty("Shipping_Cost_Ex_TaxKey") != null) ? keys.getProperty("Shipping_Cost_Ex_TaxKey") : "Shipping_Cost_Ex_Tax";

		// Shipping_Cost_Tax
		this.shippingCostTaxKey = (keys.getProperty("Shipping_Cost_TaxKey") != null) ? keys.getProperty("Shipping_Cost_TaxKey") : "Shipping_Cost_Tax";

		// Staff_Notes
		this.staffNotesKey = (keys.getProperty("Staff_NotesKey") != null) ? keys.getProperty("Staff_NotesKey") : "Staff_Notes";

		// State
		this.stateKey = (keys.getProperty("StateKey") != null) ? keys.getProperty("StateKey") : "State";

		// status
		this.statusKey = (keys.getProperty("statusKey") != null) ? keys.getProperty("statusKey") : "status";

		// Store_Credit_Amount
		this.storeCreditAmountKey = (keys.getProperty("Store_Credit_AmountKey") != null) ? keys.getProperty("Store_Credit_AmountKey") : "Store_Credit_Amount";

		// Subtotal_Inc_Tax
		this.subtotalIncTaxKey = (keys.getProperty("Subtotal_Inc_TaxKey") != null) ? keys.getProperty("Subtotal_Inc_TaxKey") : "Subtotal_Inc_Tax";

		// Subtotal_Tax
		this.subtotalTaxKey = (keys.getProperty("Subtotal_TaxKey") != null) ? keys.getProperty("Subtotal_TaxKey") : "Subtotal_Tax";

		// Total_Ex_Tax
		this.totalExTaxKey = (keys.getProperty("Total_Ex_TaxKey") != null) ? keys.getProperty("Total_Ex_TaxKey") : "Total_Ex_Tax";

		// Wrapping_Cost_Ex_Tax
		this.wrappingCostExTaxKey = (keys.getProperty("Wrapping_Cost_Ex_TaxKey") != null) ? keys.getProperty("Wrapping_Cost_Ex_TaxKey") : "Wrapping_Cost_Ex_Tax";

		// Wrapping_Cost_Inc_Tax
		this.wrappingCostIncTaxKey = (keys.getProperty("Wrapping_Cost_Inc_TaxKey") != null) ? keys.getProperty("Wrapping_Cost_Inc_TaxKey") : "Wrapping_Cost_Inc_Tax";

		// Wrapping_Cost_Tax
		this.wrappingCostTaxKey = (keys.getProperty("Wrapping_Cost_TaxKey") != null) ? keys.getProperty("Wrapping_Cost_TaxKey") : "Wrapping_Cost_Tax";

		// Zip
		this.zipKey = (keys.getProperty("ZipKey") != null) ? keys.getProperty("ZipKey") : "Zip";

		// Quantity
		this.quantityKey = (keys.getProperty("quantityKey") != null) ? keys.getProperty("quantityKey") : "quantity";

		// Country ISO
		this.countryIso2Key = (keys.getProperty("countryIso2Key") != null) ? keys.getProperty("countryIso2Key") : "Country_iso2";

		// Product
		this.productsKey = (keys.getProperty("productsKey") != null) ? keys.getProperty("productsKey") : "products";

		// Product Id
		this.productIdKey = (keys.getProperty("productIdKey") != null) ? keys.getProperty("productIdKey") : "Product_ID";

		// Inventory 
		this.inventoryRefKey = getAttibuteByKey("inventoryRefKey", "inventory");

		// Inventory Option 
		this.inventoryOptionRefKey = getAttibuteByKey("inventoryOptionRefKey", "inventory_options");

		// Inventory Option Id
		this.inventoryOptionIdKey = getAttibuteByKey("inventoryOptionIdKey", "option_id");

		// Inventory Option Value Id
		this.inventoryOptionValueIdKey = getAttibuteByKey("inventoryOptionValueIdKey", "option_value_id");

		// Customer Reference
		this.customerRefKey = getAttibuteByKey("customerRefKey", "customer");

		// Customer Id
		this.customerIdKey = getAttibuteByKey("customerIdKey", "customer_id");
		
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
	

	
	@Override
	public Object execute(IExecutionEngine engine, Object[] parameters)
			throws SuspendProcessException, ExecutionException, AccessDeniedException {
		inform("Starting BIGCommerce Place Order");
		
		if(parameters == null || parameters.length < 2) {
			error("This process is expecting 2 parameters", null);
			return null;
		}
		this.boSettings = (IEntity) parameters[0];
		this.boOrder = (IEntity) parameters[1];
		
		loadProps();
		try {
			init();
			logMessageToBSV(engine, "Sync", "Start Sending Order", "Ok");
			
		  	BigCommerceConnector conn = new BigCommerceConnector(this.storeUrl, this.username, this.password);
			
		  	Address billingAddress = new Address();
		  	
			billingAddress.setFirstName(getBOData(boOrder, this.firstNameKey));
			billingAddress.setCompany(getBOData(boOrder, this.companyKey));
			billingAddress.setLastName(getBOData(boOrder, this.lastNameKey));
			billingAddress.setStreet1(getBOData(boOrder, this.addressOneKey));
			billingAddress.setStreet2(getBOData(boOrder, this.addressTwoKey));
			billingAddress.setCity(getBOData(boOrder, this.cityKey));
			billingAddress.setState(getBOData(boOrder, this.stateKey));
			billingAddress.setZip(getBOData(boOrder, this.zipKey));
			billingAddress.setCountry(getBOData(boOrder, this.countryKey));
			billingAddress.setCountryIso2(getBOData(boOrder, this.countryIso2Key));
			billingAddress.setPhone(getBOData(boOrder, this.phoneKey));
			billingAddress.setEmail(getBOData(boOrder, this.emailAddressKey));
			
			Order order = new Order();
			order.setStatusId(DEFAULT_STATUS_ID);
			
			order.setCustomerId(getCustomer(engine));
			
			order.setBillingAddress(billingAddress);
			
			OrderItem[] orderItems = getOrderItem(engine);
			if(orderItems != null) {
				order.setOrderItems(orderItems);
			}
			
	   	 	ObjectMapper mapper = new ObjectMapper();
			String jsonInString = mapper.writeValueAsString(order);
			inform("JSON: " + jsonInString);
			
 			String result = conn.servicePost("orders", jsonInString);
			inform("Result: " + result);
			
			logMessageToBSV(engine, "Sync", "Finished Sending Order", "Ok");
		} catch (InvalidParameterException e) {
			error(" ", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		} catch (JsonProcessingException e) {
			error(" ", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		} catch (BigCommerceException e) {
			error(" ", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		} catch (Exception e) {
			error(" ", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		}
		
		return null;
	}
	
	private Integer getCustomer(IExecutionEngine engine) 
			throws InvalidParameterException, ExecutionException, AccessDeniedException {
		
		IEntity[] customerArray = engine.getAllReferences(this, this.boOrder, this.customerRefKey);
		if(customerArray != null && customerArray.length > 0) {
			IEntity customer = customerArray[0];
			
			String dat = getBOData(customer, this.customerIdKey);
			if(dat != null && !"".equals(dat)) {
				return Integer.valueOf(dat);
			}
		}
		
		return DEFAULT_CUSTOMER_ID;
	}

	private ProductOption[] getProductOption(IExecutionEngine engine) 
			throws ExecutionException, AccessDeniedException, InvalidParameterException {
		
		IEntity[] inventoryBoArray = engine.getAllReferences(this, this.boOrder, this.inventoryRefKey);
		
		if(inventoryBoArray != null && inventoryBoArray.length > 0) {
			
			IEntity inventoryBo = inventoryBoArray[0];
			IEntity[] optionBoArray = engine.getAllReferences(this, inventoryBo, this.inventoryOptionRefKey);
			if(optionBoArray != null && optionBoArray.length > 0) {
			
				ProductOption[] options = new ProductOption[optionBoArray.length];
				for(int i=0; i<options.length; i++) {
					
					options[i] = new ProductOption();
			
					options[i].setId(getBOData(optionBoArray[i], this.inventoryOptionIdKey));
					options[i].setValue(getBOData(optionBoArray[i], this.inventoryOptionValueIdKey));
				}
				return options;
			}
		}
		
		return null;
	}
	
	private OrderItem[] getOrderItem(IExecutionEngine engine) 
			throws ExecutionException, AccessDeniedException, InvalidParameterException {
		IEntity[] productRefs = engine.getAllReferences(this, this.boOrder, this.productsKey);
		
		if(productRefs != null && productRefs.length > 0) {
			IEntity boProduct = productRefs[0];
			
			OrderItem[] orderItems = new OrderItem[1];
			orderItems[0] = new OrderItem(); 
			orderItems[0].setQuantity(getBONumberData(this.boOrder, this.quantityKey));
			orderItems[0].setProductId(getBOData(boProduct, this.productIdKey));
			
			ProductOption[] options = getProductOption(engine);
			if(options != null) {
				orderItems[0].setProductOptions(options);
			}
			
			return orderItems;
		}
		
		return null;
	}
	
	private Double getBONumberData(IEntity bo, String key) throws InvalidParameterException {
		Object obj = bo.getAttributeValue(key);
		if(obj != null) {
			if(obj instanceof Long) {
				Long v = (Long)obj;
				return v.doubleValue();
			} else {
				return (Double)obj;
				
			}
		}
		
		return 0.0;
	}

	private String getBOData(IEntity bo, String key) throws InvalidParameterException {
		Object obj = bo.getAttributeValue(key);
		if(obj != null) {
			if(obj instanceof Long) {
				Long v = (Long)obj;
				return v.toString();
			} else if(obj instanceof Integer) {
				Integer v = (Integer)obj;
				return v.toString();
			} else {
				return (String)obj;
			}
		}
		
		return "";
	}

	@Override
	public Object resume(IExecutionEngine arg0, Object arg1)
			throws SuspendProcessException, ExecutionException,
			AccessDeniedException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Logs the message into the console
	 * 
	 * @param message
	 *            The message to be logged into console
	 */
	private void inform(String message) {
		//System.out.println(message);
		log.info("[BSV Upload Order] " + message);
	}
	
	private void error(String message, Throwable t) {
		//System.out.println(message);
		if( t != null) {
			log.error("[BSV Upload Order] " + message, t);
		} else {
			log.error("[BSV Upload Order] " + message);
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
