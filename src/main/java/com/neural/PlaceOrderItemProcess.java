package com.neural;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.dataobjects.InvalidParameterException;

import com.bas.basserver.executionengine.ExecutionException;
import com.bas.basserver.executionengine.IExecutionEngine;
import com.bas.basserver.executionengine.IProcess;
import com.bas.basserver.executionengine.ServerTimeOutException;
import com.bas.connectionserver.server.AccessDeniedException;
import com.bas.shared.domain.operation.IEntity;
import com.bas.shared.ruleparser.ParseException;

@Deprecated
public class PlaceOrderItemProcess {

	private static Log log = LogFactory.getLog(PlaceOrderItemProcess.class);
	
	private static final String PROPS_FILE = "bc_order_items.props";

	private String customQueryString = null;
	
	private Properties keys = new Properties();

	private String categoriesKey;

	private String descriptionKey;

	private String HTML_DescriptionKey;

	private String item_idKey;

	private String nameKey;

	private String price_ex_taxKey;

	private String price_inc_taxKey;

	private String quantityKey;

	private String skuKey;

	private String upcKey;

	private String weightKey;

	private String orderKey;

	private String boName;

	private String errorBoName;

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

	private void init() {
		if(keys.isEmpty()) {
			
			loadProps();
							
			boName = (keys.getProperty("boNameKey") != null) ? keys.getProperty("boNameKey") : "e_order_items";			
			
			// Error BO Name
			errorBoName = (keys.getProperty("errorBoNameKey") != null) ? keys.getProperty("errorBoNameKey") : "web_data_errors";			
			inform(" (Error BO Name) --> " + errorBoName);
			
			// categories
			categoriesKey = (keys.getProperty("categoriesKey") != null) ? keys.getProperty("categoriesKey") : "categories";

			// description
			descriptionKey = (keys.getProperty("descriptionKey") != null) ? keys.getProperty("descriptionKey") : "description";

			// HTML_Description
			HTML_DescriptionKey = (keys.getProperty("HTML_DescriptionKey") != null) ? keys.getProperty("HTML_DescriptionKey") : "HTML_Description";

			// item_id
			item_idKey = (keys.getProperty("item_idKey") != null) ? keys.getProperty("item_idKey") : "item_id";

			// name
			nameKey = (keys.getProperty("nameKey") != null) ? keys.getProperty("nameKey") : "name";

			// price_ex_tax
			price_ex_taxKey = (keys.getProperty("price_ex_taxKey") != null) ? keys.getProperty("price_ex_taxKey") : "price_ex_tax";

			// price_inc_tax
			price_inc_taxKey = (keys.getProperty("price_inc_taxKey") != null) ? keys.getProperty("price_inc_taxKey") : "price_inc_tax";

			// quantity
			quantityKey = (keys.getProperty("quantityKey") != null) ? keys.getProperty("quantityKey") : "quantity";

			// sku
			skuKey = (keys.getProperty("skuKey") != null) ? keys.getProperty("skuKey") : "sku";

			// upc
			upcKey = (keys.getProperty("upcKey") != null) ? keys.getProperty("upcKey") : "upc";

			// weight
			weightKey = (keys.getProperty("weightKey") != null) ? keys.getProperty("weightKey") : "weight";

			// Order
			orderKey = (keys.getProperty("orderKey") != null) ? keys.getProperty("orderKey") : "e_order";
		}
	}

	public void syncData(IProcess process, IExecutionEngine engine, 
			BigCommerceConnector bcConnector, String orderId, IEntity orderInstance) 
					throws BigCommerceException, InvalidParameterException, ServerTimeOutException, 
							ExecutionException, ParseException, AccessDeniedException {

		init();
		
		// Add order item to BC
	
	}
	
	/**
	 * Logs the message into the console
	 * 
	 * @param message
	 *            The message to be logged into console
	 */
	public static void inform(String message) {
		//System.out.println(message);
		log.info("[BSV Order Items] " + message);
	}

	public static void error(String message, Throwable t) {
		//System.out.println(message);
		if( t != null) {
			log.error("[BSV Order Items] " + message, t);
		} else {
			log.error("[BSV Order Items] " + message);	
		}
	}
	
	
}
