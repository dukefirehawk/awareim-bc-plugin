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
import com.neural.datasync.utils.ParserUtil;
import com.neural.json.Coupon;
import com.neural.json.Group;

public class BcCouponProcess implements IProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = 889166520317869482L;

	private static Log log = LogFactory.getLog(BcCouponProcess.class);
	
	private static final String PROPS_FILE = "bc_coupon.props";

	private Properties keys;

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

	private String storeUrlKey;

	private String usernameKey;

	private String passwordKey;

	private String storeUrl;

	private IEntity boSettings;

	private IEntity boCoupon;

	private String errorBoName;

	private String password;

	private String username;

	private String enabledKey;

	private String appliesToKey;

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

	@Override
	public Object execute(IExecutionEngine engine, Object[] parameters)
			throws SuspendProcessException, ExecutionException,
			AccessDeniedException {
		inform("Starting BIGCommerce Place Coupon");
		
		if(parameters == null || parameters.length < 2) {
			error("This process is expecting 2 parameters", null);
			return null;
		}
		this.boSettings = (IEntity) parameters[0];
		this.boCoupon = (IEntity) parameters[1];
		
		loadProps();
		try {
			init();
			
			logMessageToBSV(engine, "Sync", "Starting Coupon Sync", "Ok");
			
		  	BigCommerceConnector conn = new BigCommerceConnector(this.storeUrl, this.username, this.password);
		   	ObjectMapper mapper = new ObjectMapper();

	   	 	Group g = new Group();
	   	 	Integer []ids = { 0 };
	   	 	g.setIds(ids);
	   	 	g.setEntity((String)boCoupon.getAttributeValue(this.appliesToKey));
	   	 	Coupon c = new Coupon();
	   	 	c.setCode((String)boCoupon.getAttributeValue(this.codeKey));
	   	 	c.setName((String)boCoupon.getAttributeValue(this.nameKey));
	   	 	c.setAmount((String)boCoupon.getAttributeValue(this.amountKey));
	   	 	c.setType((String)boCoupon.getAttributeValue(this.typeKey));
	   	 	c.setAppliesTo(g);
	   	 	
	   	 	Object dat = boCoupon.getAttributeValue(this.expiresKey);
			c.setExpires(ParserUtil.getDateAsRFC822String(ParserUtil.getDateFromBO(dat)));
		   	
			String jsonInString = mapper.writeValueAsString(c);
			inform("Json Data: " + jsonInString);
			
			conn.servicePost("coupons", jsonInString);
			
			logMessageToBSV(engine, "Sync", "Finished Coupon Sync", "Ok");
			
		} catch (InvalidParameterException e) {
			error("Invalid Parameter", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		} catch (JsonProcessingException e) {
			error("JSON Error", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		} catch (BigCommerceException e) {
			error("BC Error", e);
			logMessageToBSV(engine, "Sync", e.getMessage(), "Fail");
		}
		
		return null;
	}

	@Override
	public Object resume(IExecutionEngine arg0, Object arg1)
			throws SuspendProcessException, ExecutionException,
			AccessDeniedException {
		// TODO Auto-generated method stub
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
	
	private void init() throws InvalidParameterException {
		
		this.storeUrlKey = (keys.getProperty("storeUrl") != null) ? keys.getProperty("storeUrl") : "storeUrl";
		this.usernameKey = (keys.getProperty("username") != null) ? keys.getProperty("username") : "username";
		this.passwordKey = (keys.getProperty("password") != null) ? keys.getProperty("password") : "password";
		
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
		String boName = (keys.getProperty("boNameKey") != null) ? keys.getProperty("boNameKey") : "e_coupon";			
		inform(" (BO Name) --> " + boName);
		
		// String settingsBo = (keys.getProperty("settingsBo") != null) ? keys.getProperty("settingsBo") : "settingsBo";
		// Coupon Id
		couponIdKey = (keys.getProperty("couponIdKey") != null) ? keys.getProperty("couponIdKey") : "coupon_id";

		nameKey = (keys.getProperty("nameKey") != null) ? keys.getProperty("nameKey") : "name";

		typeKey = (keys.getProperty("typeKey") != null) ? keys.getProperty("typeKey") : "type";
		
		amountKey = (keys.getProperty("amountKey") != null) ? keys.getProperty("amountKey") : "amount";

		minPurchaseKey = (keys.getProperty("minPurchaseKey") != null) ? keys.getProperty("minPurchaseKey") : "min_purchase";

		expiresKey = (keys.getProperty("expiresKey") != null) ? keys.getProperty("expiresKey") : "expires";
		
		codeKey = (keys.getProperty("codeKey") != null) ? keys.getProperty("codeKey") : "code";
		
		numUsesKey = (keys.getProperty("numUsesKey") != null) ? keys.getProperty("numUsesKey") : "num_uses";
		
		maxUsesKey = (keys.getProperty("maxUsesKey") != null) ? keys.getProperty("maxUsesKey") : "max_uses";
	
		maxUsesPerCustomerKey = (keys.getProperty("maxUsesPerCustomerKey") != null) ? keys.getProperty("maxUsesPerCustomerKey") : "max_uses_per_customer";

		appliesToKey = (keys.getProperty("appliesToKey") != null) ? keys.getProperty("appliesToKey") : "applies_to";

		enabledKey = (keys.getProperty("enabledKey") != null) ? keys.getProperty("enabledKey") : "enabled";

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
		log.info("[BSV Upload Coupon] " + message);
	}
	
	private void error(String message, Throwable t) {
		//System.out.println(message);
		if( t != null) {
			log.error("[BSV Upload Coupon] " + message, t);
		} else {
			log.error("[BSV Upload Coupon] " + message);
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
