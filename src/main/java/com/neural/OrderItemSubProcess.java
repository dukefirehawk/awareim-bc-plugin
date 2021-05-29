package com.neural;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.dataobjects.InvalidParameterException;

import com.bas.basserver.executionengine.ExecutionException;
import com.bas.basserver.executionengine.IExecutionEngine;
import com.bas.basserver.executionengine.IProcess;
import com.bas.basserver.executionengine.ServerTimeOutException;
import com.bas.connectionserver.server.AccessDeniedException;
import com.bas.shared.data.EntityIdAndName;
import com.bas.shared.data.QueryResult;
import com.bas.shared.domain.configuration.elements.Query;
import com.bas.shared.domain.operation.IEntity;
import com.bas.shared.ruleparser.ParseException;
import com.neural.json.OrderItem;
import com.neural.json.parser.OrderItemJsonParser;

public class OrderItemSubProcess {

	private static Log log = LogFactory.getLog(OrderItemSubProcess.class);
	
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

	private String productIdKey;

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
			
			//loadProps();
			
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
				
			boName = (keys.getProperty("boNameKey") != null) ? keys.getProperty("boNameKey") : "e_order_items";			
			
			// Error BO Name
			errorBoName = (keys.getProperty("errorBoNameKey") != null) ? keys.getProperty("errorBoNameKey") : "web_service_logs";			
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

			// Product Id
			productIdKey = (keys.getProperty("productIdKey") != null) ? keys.getProperty("productIdKey") : "product_id";
		}
	}

	public void syncData(IProcess process, IExecutionEngine engine, 
			BigCommerceConnector bcConnector, String orderId, IEntity orderInstance) 
					throws BigCommerceException, InvalidParameterException, ServerTimeOutException, 
							ExecutionException, ParseException, AccessDeniedException {

		init();
		
    	EntityIdAndName[] orderIdList = new EntityIdAndName[1];
    	orderIdList[0] = new EntityIdAndName(orderInstance.getId(), orderInstance.getName());
    	
		/*
		 * For sync everything, only take records that are later than last modified date
		 */
		String urlPath = "orders/" + orderId + "/products?limit=50";
		
		int page = 1;
		while(true) {
    		String pagedUrlPath = urlPath + "&page=" + page;

    		@SuppressWarnings({ "unchecked" })
			List<OrderItem> orderItemList = bcConnector.serviceGET(pagedUrlPath, "", new OrderItemJsonParser());
			if(orderItemList.isEmpty()) {
				// Finish
				return;
			}
			
			dataSyncMultiple(process, engine, bcConnector, urlPath, orderItemList, orderIdList);
			page++;
		}
		
	}
	
	private void dataSyncSingle(IProcess process, IExecutionEngine engine, 
			BigCommerceConnector bcConnector, String storeUrl, IEntity boInstance, 
			OrderItem jsonOrderItem, EntityIdAndName[] orderIdList) 
				throws AccessDeniedException, ServerTimeOutException, ExecutionException, InvalidParameterException  {
		
		try {

			inform(" Created BO --> " + boInstance.getId());

			updateOrderItem(keys, jsonOrderItem, boInstance);

			inform("Updating order item bo ");
			engine.updateEntity(process, boInstance, null, null, null);
			
	    	engine.addReferences(process, boInstance.getName(), boInstance.getId(), 
	    			this.orderKey, orderIdList);
			inform("order item bo updated");

		} catch (AccessDeniedException | ServerTimeOutException | ExecutionException | InvalidParameterException e) {
			error(" ", e);			
			throw e;
		}
			
	}
	
	private void dataSyncMultiple(IProcess process, IExecutionEngine engine, 
			BigCommerceConnector bcConnector, String storeUrl, 
			List<OrderItem> orderItemList, EntityIdAndName[] orderIdList) 
					throws ServerTimeOutException, InvalidParameterException,
							ExecutionException,ParseException,AccessDeniedException	{
		
    	for(OrderItem rec: orderItemList) {
    		
    		try {
	    		IEntity boInstance = getEntityByItemID(process, engine, rec.getItemId());
	    		if(boInstance == null) {
	    			boInstance = engine.createEntity(process, boName);
	    		}
	    		
	    		dataSyncSingle(process, engine, bcConnector, storeUrl, boInstance, rec, orderIdList);
	    		
    		} catch (ExecutionException | ParseException | AccessDeniedException e) {
    			error(" ", e);
    			throw e;
    		} catch (ServerTimeOutException e) {
    			error(" ", e);
    			throw e;
			} catch (InvalidParameterException e) {
				error(" ", e);
				throw e;
			}
    	 }
	}

	private String getItemByIdQuery(Properties keys, String id) {
		
		// Cache the query for reuse
		if(customQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ");
	
			buff.append(boName);
			buff.append(" WHERE ");
			buff.append(boName);
			buff.append(".");
			
			buff.append(item_idKey);
			buff.append("=");
			
			this.customQueryString = buff.toString();
			
		}
		
		String q = this.customQueryString + id ;
		inform("Custom Rule Query: " + q);
		
		return q;
	}

	/*
	 * Get the order by order id
	 */
	private IEntity getEntityByItemID(IProcess process, IExecutionEngine engine,String itemId) 
			throws ParseException, ExecutionException, AccessDeniedException {
		
		try {
			Query customQuery = Query.createFromRuleLanguageString(getItemByIdQuery(keys, itemId));
			QueryResult result = engine.executeQuery(process, customQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				
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
	
	private void updateOrderItem(Properties keys, OrderItem orderItemJsonObj, IEntity boInstance) throws InvalidParameterException {
		// categories
		String categories = orderItemJsonObj.getCategories();
		inform(categoriesKey + " (categories Key) --> " + categories);
		boInstance.setAttributeValue(categoriesKey, categories);

		// description
		String description = orderItemJsonObj.getDescription();
		inform(descriptionKey + " (description Key) --> " + description);
		boInstance.setAttributeValue(descriptionKey, description);

		// HTML_Description
		String HTML_Description = orderItemJsonObj.getHTMLDescription();
		inform(HTML_DescriptionKey + " (HTML_Description Key) --> " + HTML_Description);
		boInstance.setAttributeValue(HTML_DescriptionKey, HTML_Description);

		// item_id
		String item_id = orderItemJsonObj.getItemId();
		inform(item_idKey + " (item_id Key) --> " + item_id);
		boInstance.setAttributeValue(item_idKey, item_id);

		// name
		String name = orderItemJsonObj.getName();
		inform(nameKey + " (name Key) --> " + name);
		boInstance.setAttributeValue(nameKey, name);

		// price_ex_tax
		String price_ex_tax = orderItemJsonObj.getPriceExTax();
		inform(price_ex_taxKey + " (price_ex_tax Key) --> " + price_ex_tax);
		boInstance.setAttributeValue(price_ex_taxKey, price_ex_tax);

		// price_inc_tax
		String price_inc_tax = orderItemJsonObj.getPriceIncTax();
		inform(price_inc_taxKey + " (price_inc_tax Key) --> " + price_inc_tax);
		boInstance.setAttributeValue(price_inc_taxKey, price_inc_tax);

		// quantity
		Double quantity = orderItemJsonObj.getQuantity();
		inform(quantityKey + " (quantity Key) --> " + quantity);
		boInstance.setAttributeValue(quantityKey, quantity.toString());

		// sku
		String sku = orderItemJsonObj.getSku();
		inform(skuKey + " (sku Key) --> " + sku);
		boInstance.setAttributeValue(skuKey, sku);

		// upc
		String upc = orderItemJsonObj.getUpc();
		inform(upcKey + " (upc Key) --> " + upc);
		boInstance.setAttributeValue(upcKey, upc);

		// weight
		String weight = orderItemJsonObj.getWeight();
		inform(weightKey + " (weight Key) --> " + weight);
		boInstance.setAttributeValue(weightKey, weight);

		// Product Id
		String productId = orderItemJsonObj.getProductId();
		inform(productIdKey + " (productId Key) --> " + productId);
		boInstance.setAttributeValue(productIdKey, productId);

	}
	
	/**
	 * Logs the message into the console
	 * 
	 * @param message
	 *            The message to be logged into console
	 */
	private void inform(String message) {
		//System.out.println(message);
		log.info("[BSV Order Items] " + message);
	}

	private void error(String message, Throwable t) {
		//System.out.println(message);
		if( t != null) {
			log.error("[BSV Order Items] ", t);
		} else {
			log.error("[BSV Order Items] ");			
		}
	}
	
	private String getAttibuteByKey(String key, String defaultValue) {
		return (this.keys.getProperty(key) != null) ? this.keys.getProperty(key) : defaultValue;
	}
	
	
}
