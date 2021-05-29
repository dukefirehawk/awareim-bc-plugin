package com.neural;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import com.bas.shared.data.EntityIdAndName;
import com.bas.shared.data.QueryResult;
import com.bas.shared.domain.configuration.elements.Query;
import com.bas.shared.domain.operation.IEntity;
import com.neural.json.Option;
import com.neural.json.OptionValue;
import com.neural.json.Product;
import com.neural.json.Sku;
import com.neural.json.SkuOption;
import com.neural.json.parser.ProductJsonParser;

public class BcProductProcess implements IProcess {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1954289752366681178L;
	
	//private static final Log log = LogFactory.getLog(BcProductProcess.class);
	private static Log log = LogFactory.getLog(BcProductProcess.class);
	
	private static final String 	PROPS_FILE = "bc_products.props";
	private static final String 	SYNC_ALL = "all";
	private static final int 		MAX_PAGE_SUSPEND = 3;
	
	private String customQueryString;
	private String lastDateQueryString;
	
	private boolean m_cancelled = false;
	
	private IEntity boSettings;

	private IEntity boProduct;

	private Properties keys;

	private String storeUrl;

	private String username;

	private String password;

	private String errorBoName;

	private String boName;

	private int page = 1;

	private String lastDate;

	private String availabilityKey;

	private String dateCreatedKey;

	private String dateUpdatedKey;

	private String dateLastImportedKey;

	private String depthKey;

	private String descriptionKey;

	private String fixedCostShippingPriceKey;

	private String heightKey;

	private String inventoryLevelKey;

	private String inventoryWarningLevelKey;

	private String inventoryTrackingKey;

	private String isFeaturedKey;

	private String isFreeShippingKey;

	private String isPreorderOnlyKey;

	private String isVisibleKey;

	private String metaDescriptionKey;

	private String metaKeywordsKey;

	private String nameKey;

	private String numberSoldKey;

	private String orderQuantityMaximumKey;

	private String orderQuantityMinimumKey;

	private String preOrderReleaseDateKey;

	private String preorderMessageKey;

	private String priceKey;

	private String productIDKey;

	private String ratingCountKey;

	private String ratingTotalKey;

	private String relatedProductsKey;

	private String retailPriceKey;

	private String salePriceKey;

	private String searchKeywordsKey;

	private String sKUKey;

	private String sortOrderKey;

	private String totalSoldKey;

	private String typeKey;

	private String uPCKey;

	private String viewCountKey;

	private String warrantyKey;

	private String weightKey;

	private String widthKey;

	private String availabilityDescriptionKey;

	private String binPickingNumberKey;

	private String categoryKey;

	private String costPriceKey;

	private HashMap<String, Option> optionMap;

	private HashMap<String, OptionValue> optionValueMap;

	private String inventoryQueryString;

	private String inventoryBoKey;

	private String inventoryCostPriceKey;

	private String inventoryDescriptionKey;

	private String inventorySkuKey;

	private String inventoryProductRefKey;

	//private String inventoryOptionsRefKey;

	private String inventorySkuLevelKey;

	private String inventorySkuWarningLevelKey;

	private String optionBoKey;

	private String optionLabelKey;

	private String optionNameKey;

	private String optionValueIdKey;

	private String optionInventoryRefKey;

	private String inventoryIdKey;

	private String optionPrimaryIdKey;

	private String optionOptionIdKey;

	private Object optionQueryString;

	private String inventoryUpcKey;

	private String inventoryBinPickingNumberKey;

	private String syncOption;

	private String errorActionKey;

	private String errorCreatedDateKey;

	private String errorMessageKey;

	private String errorProcessNameKey;

	private String errorStatusKey;

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
	public Object resume(IExecutionEngine engine, Object parameters)
			throws SuspendProcessException, ExecutionException,AccessDeniedException {
		
		inform("Resume");
		logMessageToBSV(engine, "Sync", "Resume Product Sync", "Ok");
		execute(engine);
		
		return null;
	}
	

	@Override
	public Object execute(IExecutionEngine engine, Object[] parameters)
			throws SuspendProcessException, ExecutionException,
			AccessDeniedException {
		
		inform("Starting BIGCommerce Product Sync v1.0");
		
		if(parameters == null || parameters.length == 0) {
			inform("No bo received from parameter");
			this.boSettings = getInputParameter(engine);
		} else {
			inform("Received bo as parameter");
			this.boSettings = (IEntity) parameters[0];
	
			/*
			 * Accept 2nd parameter as the e_customers 
			 */
			this.boProduct = null;
			if(parameters.length > 1) {
				this.boProduct = (IEntity) parameters[1];
			} 
		}
		
		try {
			loadProps();
			
			String storeUrlKey = (keys.getProperty("storeUrl") != null) ? keys.getProperty("storeUrl") : "storeUrl";
			String usernameKey = (keys.getProperty("username") != null) ? keys.getProperty("username") : "username";
			String passwordKey = (keys.getProperty("password") != null) ? keys.getProperty("password") : "password";
			String syncOptionKey = getAttibuteByKey("syncOption", "sync_option");
			
			this.storeUrl = (String) boSettings.getAttributeValue(storeUrlKey);
			if(!this.storeUrl.endsWith("/")) {
				this.storeUrl += "/";
			}
			inform(storeUrlKey + " (storeUrl) --> " + this.storeUrl);
			
			this.username = (String) boSettings.getAttributeValue(usernameKey);
			inform(usernameKey + " (username) --> " + this.username);
			
			this.password = (String) boSettings.getAttributeValue(passwordKey);
			inform(passwordKey + " (password) --> " + this.password);
						
			this.syncOption = (String) boSettings.getAttributeValue(syncOptionKey);
			inform(syncOptionKey + " (syncOption) --> " + this.syncOption);

			// Error BO Name
			this.errorBoName = (keys.getProperty("errorBoNameKey") != null) ? keys.getProperty("errorBoNameKey") : "web_service_logs";			
			inform(" (Error BO Name) --> " + errorBoName);		

			// BO Name
			boName = (keys.getProperty("boNameKey") != null) ? keys.getProperty("boNameKey") : "e_products";			
			inform(" (BO Name) --> " + boName);		
			// String settingsBo = (keys.getProperty("settingsBo") != null) ? keys.getProperty("settingsBo") : "settingsBo";
			
			// Availability
			availabilityKey = (keys.getProperty("AvailabilityKey") != null) ? keys.getProperty("AvailabilityKey") : "Availability";

			// Availability_Description
			availabilityDescriptionKey = (keys.getProperty("Availability_DescriptionKey") != null) ? keys.getProperty("Availability_DescriptionKey") : "Availability_Description";

			// Bin_Picking_Number
			binPickingNumberKey = (keys.getProperty("Bin_Picking_NumberKey") != null) ? keys.getProperty("Bin_Picking_NumberKey") : "Bin_Picking_Number";

			// Category
			categoryKey = (keys.getProperty("CategoryKey") != null) ? keys.getProperty("CategoryKey") : "Category";

			// Cost_Price
			costPriceKey = (keys.getProperty("Cost_PriceKey") != null) ? keys.getProperty("Cost_PriceKey") : "Cost_Price";

			// date_created
			dateCreatedKey = (keys.getProperty("date_createdKey") != null) ? keys.getProperty("date_createdKey") : "date_created";

			// date_updated
			dateUpdatedKey = (keys.getProperty("date_updatedKey") != null) ? keys.getProperty("date_updatedKey") : "date_updated";

			// Date_Last_imported
			dateLastImportedKey = (keys.getProperty("Date_Last_importedKey") != null) ? keys.getProperty("Date_Last_importedKey") : "Date_Last_imported";

			// Depth
			depthKey = (keys.getProperty("DepthKey") != null) ? keys.getProperty("DepthKey") : "Depth";

			// Description
			descriptionKey = (keys.getProperty("DescriptionKey") != null) ? keys.getProperty("DescriptionKey") : "Description";

			// Fixed_Cost_Shipping_Price
			fixedCostShippingPriceKey = (keys.getProperty("Fixed_Cost_Shipping_PriceKey") != null) ? keys.getProperty("Fixed_Cost_Shipping_PriceKey") : "Fixed_Cost_Shipping_Price";

			// Height
			heightKey = (keys.getProperty("HeightKey") != null) ? keys.getProperty("HeightKey") : "Height";

			// Inventory_Level
			inventoryLevelKey = (keys.getProperty("Inventory_LevelKey") != null) ? keys.getProperty("Inventory_LevelKey") : "Inventory_Level";

			// Inventory_Warning_Level
			inventoryWarningLevelKey = (keys.getProperty("Inventory_Warning_LevelKey") != null) ? keys.getProperty("Inventory_Warning_LevelKey") : "Inventory_Warning_Level";

			// Inventory_Tracking
			inventoryTrackingKey = (keys.getProperty("Inventory_TrackingKey") != null) ? keys.getProperty("Inventory_TrackingKey") : "Inventory_Tracking";

			// Is_Featured
			isFeaturedKey = (keys.getProperty("Is_FeaturedKey") != null) ? keys.getProperty("Is_FeaturedKey") : "Is_Featured";

			// Is_Free_Shipping
			isFreeShippingKey = (keys.getProperty("Is_Free_ShippingKey") != null) ? keys.getProperty("Is_Free_ShippingKey") : "Is_Free_Shipping";

			// Is_Preorder_Only
			isPreorderOnlyKey = (keys.getProperty("Is_Preorder_OnlyKey") != null) ? keys.getProperty("Is_Preorder_OnlyKey") : "Is_Preorder_Only";

			// Is_Visible
			isVisibleKey = (keys.getProperty("Is_VisibleKey") != null) ? keys.getProperty("Is_VisibleKey") : "Is_Visible";

			// Meta_Description
			metaDescriptionKey = (keys.getProperty("Meta_DescriptionKey") != null) ? keys.getProperty("Meta_DescriptionKey") : "Meta_Description";

			// Meta_Keywords
			metaKeywordsKey = (keys.getProperty("Meta_KeywordsKey") != null) ? keys.getProperty("Meta_KeywordsKey") : "Meta_Keywords";

			// Name
			nameKey = (keys.getProperty("NameKey") != null) ? keys.getProperty("NameKey") : "Name";

			// Number_Sold
			numberSoldKey = (keys.getProperty("Number_SoldKey") != null) ? keys.getProperty("Number_SoldKey") : "Number_Sold";

			// Order_Quantity_Maximum
			orderQuantityMaximumKey = (keys.getProperty("Order_Quantity_MaximumKey") != null) ? keys.getProperty("Order_Quantity_MaximumKey") : "Order_Quantity_Maximum";

			// Order_Quantity_Minimum
			orderQuantityMinimumKey = (keys.getProperty("Order_Quantity_MinimumKey") != null) ? keys.getProperty("Order_Quantity_MinimumKey") : "Order_Quantity_Minimum";

			// Pre_Order_Release_Date
			preOrderReleaseDateKey = (keys.getProperty("Pre_Order_Release_DateKey") != null) ? keys.getProperty("Pre_Order_Release_DateKey") : "Pre_Order_Release_Date";

			// Preorder_Message
			preorderMessageKey = (keys.getProperty("Preorder_MessageKey") != null) ? keys.getProperty("Preorder_MessageKey") : "Preorder_Message";

			// Price
			priceKey = (keys.getProperty("PriceKey") != null) ? keys.getProperty("PriceKey") : "Price";

			// Product_ID
			productIDKey = (keys.getProperty("Product_IDKey") != null) ? keys.getProperty("Product_IDKey") : "Product_ID";

			// Rating_Count
			ratingCountKey = (keys.getProperty("Rating_CountKey") != null) ? keys.getProperty("Rating_CountKey") : "Rating_Count";

			// Rating_Total
			ratingTotalKey = (keys.getProperty("Rating_TotalKey") != null) ? keys.getProperty("Rating_TotalKey") : "Rating_Total";

			// Related_Products
			relatedProductsKey = (keys.getProperty("Related_ProductsKey") != null) ? keys.getProperty("Related_ProductsKey") : "Related_Products";

			// Retail_Price
			retailPriceKey = (keys.getProperty("Retail_PriceKey") != null) ? keys.getProperty("Retail_PriceKey") : "Retail_Price";

			// Sale_Price
			salePriceKey = (keys.getProperty("Sale_PriceKey") != null) ? keys.getProperty("Sale_PriceKey") : "Sale_Price";

			// Search_Keywords
			searchKeywordsKey = (keys.getProperty("Search_KeywordsKey") != null) ? keys.getProperty("Search_KeywordsKey") : "Search_Keywords";

			// SKU
			sKUKey = (keys.getProperty("SKUKey") != null) ? keys.getProperty("SKUKey") : "SKU";

			// Sort_Order
			sortOrderKey = (keys.getProperty("Sort_OrderKey") != null) ? keys.getProperty("Sort_OrderKey") : "Sort_Order";

			// Total_Sold
			totalSoldKey = (keys.getProperty("Total_SoldKey") != null) ? keys.getProperty("Total_SoldKey") : "Total_Sold";

			// Type
			typeKey = (keys.getProperty("TypeKey") != null) ? keys.getProperty("TypeKey") : "Type";

			// UPC
			uPCKey = (keys.getProperty("UPCKey") != null) ? keys.getProperty("UPCKey") : "UPC";

			// View_Count
			viewCountKey = (keys.getProperty("View_CountKey") != null) ? keys.getProperty("View_CountKey") : "View_Count";

			// Warranty
			warrantyKey = (keys.getProperty("WarrantyKey") != null) ? keys.getProperty("WarrantyKey") : "Warranty";

			// Weight
			weightKey = (keys.getProperty("WeightKey") != null) ? keys.getProperty("WeightKey") : "Weight";

			// Width
			widthKey = (keys.getProperty("WidthKey") != null) ? keys.getProperty("WidthKey") : "Width";

			// Inventory
			this.inventoryBoKey = getAttibuteByKey("inventoryBoKey", "e_inventory");
			
			this.inventoryIdKey = getAttibuteByKey("inventoryIdKey", "inventory_id");
			this.inventoryCostPriceKey = getAttibuteByKey("inventoryCostPriceKey", "cost_price");
			this.inventoryDescriptionKey = getAttibuteByKey("inventoryDescriptionKey", "description");
			this.inventorySkuKey = getAttibuteByKey("inventorySkuKey", "sku");
			this.inventoryProductRefKey = getAttibuteByKey("inventoryProductRefKey", "product");
			//this.inventoryOptionsRefKey = getAttibuteByKey("inventoryOptionsRefKey", "inventory_options");
			this.inventorySkuLevelKey = getAttibuteByKey("inventorySkuLevelKey", "inventory_level");
			this.inventorySkuWarningLevelKey = getAttibuteByKey("inventorySkuWarningLevelKey", "inventory_warning_level");
			this.inventoryUpcKey = getAttibuteByKey("inventoryUpcKey", "upc");
			this.inventoryBinPickingNumberKey = getAttibuteByKey("inventoryBinPickingNumberKey", "bin_picking_number");
			
			// Inventory Option
			this.optionBoKey = getAttibuteByKey("optionBoKey", "e_inventory_options");
			this.optionPrimaryIdKey = getAttibuteByKey("optionPrimaryIdKey", "inventory_option_id");
			this.optionLabelKey = getAttibuteByKey("optionLabelKey", "label");
			this.optionNameKey = getAttibuteByKey("optionNameKey", "name");
			this.optionOptionIdKey = getAttibuteByKey("optionOptionIdKey", "option_id");
			this.optionValueIdKey = getAttibuteByKey("optionValueIdKey", "option_value_id");
			this.optionInventoryRefKey = getAttibuteByKey("optionInventoryRefKey", "inventory");

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
		
		this.optionMap = new HashMap<String, Option>();
		this.optionValueMap = new HashMap<String, OptionValue>();
		
		this.page = 1;
		this.lastDate = null;
		
		logMessageToBSV(engine, "Sync", "Starting Product Sync", "Ok");
		execute (engine);

		return null;
	}
	
	private String getAttibuteByKey(String key, String defaultValue) {
		return (this.keys.getProperty(key) != null) ? this.keys.getProperty(key) : defaultValue;
	}
	
	private void execute (IExecutionEngine engine)
			throws SuspendProcessException, AccessDeniedException, ExecutionException {	

		try {

			Thread.sleep(500);
			
			BigCommerceConnector bcConnector = new BigCommerceConnector(storeUrl, username, password);
 			
			String urlPath = "products";
			
			// Business Object product passed in
	    	if(boProduct != null) {
				String productId = (String) boProduct.getAttributeValue(productIDKey);
	    		urlPath = urlPath + "/" + productId;

	    		@SuppressWarnings({ "unchecked" })
				List<Product> productList = bcConnector.serviceGET(urlPath, "", new ProductJsonParser());
	    		if(productList.isEmpty()) {
	    			// Customer not found
	    			return;
	    		}
	    		
	    		dataSyncSingle(engine, keys, bcConnector, storeUrl, boProduct, productList.get(0));

	    	} else {
	    		/*
	    		 * For sync everything, only take records that are later than last modified date.
	    		 * Only check the last modified on first call.
	    		 */
	    		
	    		if(this.page == 1) {
	    			if(!SYNC_ALL.equals(this.syncOption)) {
	    				this.lastDate = getLastSyncDate(engine);
	    			}
	    		}
	    		
	    		if(this.lastDate != null) {
	    			urlPath = urlPath + "?limit=30&min_date_modified=" + this.lastDate;
	    		} else {
	    			urlPath = urlPath + "?limit=30";
	    		}
	    		inform("UrlPath: " + urlPath);

	    		while(true) {
	    			
	    			if(this.m_cancelled) {
	    				inform("Process has been cancelled");
						logMessageToBSV(engine, "Sync", "Cancelled Product Sync", "Failed");
	    				return;
	    			}

		    		String pagedUrlPath = urlPath + "&page=" + page;

		    		@SuppressWarnings({ "unchecked" })
					List<Product> productList = bcConnector.serviceGET(pagedUrlPath, "", new ProductJsonParser());
					if(productList.isEmpty()) {
						// Finish
						break;
					}
	    			dataSyncMultiple(engine, keys, bcConnector, urlPath, productList);
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
		
		logMessageToBSV(engine, "Sync", "Finished Product Sync", "Ok");
	}

	private void updateInventory( IEntity boInstance, Sku jsonObj ) throws InvalidParameterException {
		boInstance.setAttributeValue(this.inventoryCostPriceKey, jsonObj.getCostPrice());
		boInstance.setAttributeValue(this.inventorySkuLevelKey, jsonObj.getInventoryLevel());
		boInstance.setAttributeValue(this.inventorySkuKey, jsonObj.getSku());
		boInstance.setAttributeValue(this.inventorySkuWarningLevelKey, jsonObj.getInventoryWarningLevel());
		boInstance.setAttributeValue(this.inventoryBinPickingNumberKey, jsonObj.getBinPickingNumber());
		boInstance.setAttributeValue(this.inventoryUpcKey, jsonObj.getUpc());
		boInstance.setAttributeValue(this.inventoryDescriptionKey, jsonObj.getDescription());
		boInstance.setAttributeValue(this.inventoryIdKey, jsonObj.getId());
	}
	
	private void updateInventoryOption(IEntity boInstance, SkuOption jsonObj) throws InvalidParameterException {
		boInstance.setAttributeValue(this.optionLabelKey, jsonObj.getLabel());
		boInstance.setAttributeValue(this.optionNameKey, jsonObj.getDisplayName());
		boInstance.setAttributeValue(this.optionOptionIdKey, jsonObj.getProductOptionId());
		boInstance.setAttributeValue(this.optionValueIdKey, jsonObj.getOptionValueId());
		boInstance.setAttributeValue(this.optionPrimaryIdKey, jsonObj.getId());
		
	}
		
	private List<Sku> getSkuList(BigCommerceConnector conn, String productId) 
			throws BigCommerceException {
		
		List<Sku> skuList = conn.getProductSku(productId);
		if(!skuList.isEmpty()) {
			for(Sku sku: skuList) {
				//inform("Sku: " + sku.getSku());
				//inform("Inventory Level: " + sku.getInventoryLevel());
								
				SkuOption skuOpt[] = sku.getOptions();
				if(skuOpt.length > 0) {
					StringBuilder desc = new StringBuilder();
					for(int j=0; j<skuOpt.length; j++) {
						String optionId = skuOpt[j].getProductOptionId();
						String optionValueId = skuOpt[j].getOptionValueId();
						
						//inform("Sku (Product Option Id): " + optionId);
						//inform("Sku (Option Value Id): " + optionValueId);
						
						// Create a unique key for SKU option
						String id = "S" + sku.getId() + "_O" + optionId + "_V" + optionValueId;
						skuOpt[j].setId(id);
						
						Option opt = optionMap.get(optionId);
						if(opt == null) {
							List<Option> optionList = conn.getProductOptionById(productId, optionId);
							opt = optionList.get(0);
							optionMap.put(optionId, opt);
						}
						
						// Generate a unique lookup key
						String key = "O" + opt.getOptionId() + "_V" + optionValueId;
						OptionValue optValue = optionValueMap.get(key);
						if(optValue == null) {
							List<OptionValue> optValueList = conn.getOptionValueById(opt.getOptionId(), optionValueId);
							optValue = optValueList.get(0);
							optionValueMap.put(key, optValue);
						}

						//inform("Product Option Id : " + opt.getId());
						//inform("Option Id : " + opt.getOptionId());
						//inform(" ");
						skuOpt[j].setDisplayName(opt.getDisplayName());
						skuOpt[j].setLabel(optValue.getLabel());
						desc.append(opt.getDisplayName() + ":" + optValue.getLabel() + " ");
						
					}
					
					//inform("Options : " + desc);
					
					// Combine the all the options into a description
					sku.setDescription(desc.toString());
				}
			}
			
		}
		
		return skuList;
	}
	
	private void dataSyncSingle(IExecutionEngine engine, Properties keys, 
			BigCommerceConnector bcConnector, String storeUrl, IEntity boInstance, Product jsonProduct) 
				throws AccessDeniedException, ServerTimeOutException, ExecutionException, 
						InvalidParameterException, BigCommerceException, com.bas.shared.ruleparser.ParseException  {
		
		try {

			inform(" Created BO --> " + boInstance.getId());

			updateProduct(keys, jsonProduct, boInstance);

			inform("Updating products bo ");
			engine.updateEntity(this, boInstance, null, null, null);
			inform("products bo updated");
			
			// Product Entity Reference
	    	EntityIdAndName[] productIdList = new EntityIdAndName[1];
	    	productIdList[0] = new EntityIdAndName(boInstance.getId(), boInstance.getName());

			List<Sku> skuList = getSkuList(bcConnector, jsonProduct.getProductID());
			for(Sku rec: skuList) {
				IEntity inventoryBo = getInventoryByID(engine, rec.getId());
	    		if(inventoryBo == null) {
	    			inventoryBo = engine.createEntity(this, this.inventoryBoKey);
	    		}
    			updateInventory(inventoryBo, rec);
    	
    			inform("Updating inventory bo ");
    			engine.updateEntity(this, inventoryBo, null, null, null);
    	    	engine.addReferences(this, inventoryBo.getName(), inventoryBo.getId(), 
    	    			this.inventoryProductRefKey, productIdList);
    			inform("Inventory bo updated");
    			
    			// Inventory Entity Reference
    	    	EntityIdAndName[] inventoryIdList = new EntityIdAndName[1];
    	    	inventoryIdList[0] = new EntityIdAndName(inventoryBo.getId(), inventoryBo.getName());

    	    	if(rec.getOptions().length > 0) {
    				SkuOption skuOptions[] = rec.getOptions();
    				
    				for(int i=0; i<skuOptions.length; i++) {
    					SkuOption opt = skuOptions[i];
    					IEntity inventoryOptionBo = getInventoryOptionByID(engine, opt.getId());
    					if(inventoryOptionBo == null) {
    						inventoryOptionBo = engine.createEntity(this, this.optionBoKey);
    					}
    					updateInventoryOption(inventoryOptionBo, opt);

    					inform("Updating inventory option bo ");
    	    			engine.updateEntity(this, inventoryOptionBo, null, null, null);
    	    	    	engine.addReferences(this, inventoryOptionBo.getName(), inventoryOptionBo.getId(), 
    	    	    			this.optionInventoryRefKey, inventoryIdList);
    	    			inform("Inventory option bo updated");
    				}
    				
    			}
    		
			}
			
		} catch (AccessDeniedException | ServerTimeOutException | ExecutionException | InvalidParameterException e) {
			error(" ", e);
			throw e;
		} catch (BigCommerceException e) {
			error(" ", e);
			throw e;
		} catch (com.bas.shared.ruleparser.ParseException e) {
			error(" ", e);
			throw e;
		}
			
	}
	
	private void dataSyncMultiple(IExecutionEngine engine, Properties keys, 
			BigCommerceConnector bcConnector, String storeUrl, List<Product> productList)  
				throws AccessDeniedException, ServerTimeOutException, ExecutionException, InvalidParameterException, 
						com.bas.shared.ruleparser.ParseException, BigCommerceException  {
		
    	for(Product rec: productList) {
    		
			if(this.m_cancelled) {
				inform("Process has been cancelled");
				return;
			}
			
    		try {
	    		IEntity boInstance = getEntityByProductID(engine, rec.getProductID());
	    		if(boInstance == null) {
	    			boInstance = engine.createEntity(this, boName);
	    		}
	    		
	    		dataSyncSingle(engine, keys, bcConnector, storeUrl, boInstance, rec);
	    		
    		} catch (AccessDeniedException | ServerTimeOutException | ExecutionException| InvalidParameterException e) {
    			error(" ", e);
    			throw e;
    		} catch (com.bas.shared.ruleparser.ParseException e) {
    			error(" ", e);
    			throw e;
			} catch (BigCommerceException e) {
    			error(" ", e);
    			throw e;
			}
			
    	 }
		
	}

	private String getInventoryOptionByIdQuery(String id) {
		
		// Cache the query for reuse
		if(optionQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ");
	
			buff.append(this.optionBoKey);
			buff.append(" WHERE ");
			buff.append(this.optionBoKey);
			buff.append(".");
			
			buff.append(this.optionPrimaryIdKey);
			buff.append("=");
			
			this.optionQueryString = buff.toString();
			
		}
		
		String q = this.optionQueryString + "'" + id + "'";
		inform("Option Rule Query: " + q);
		
		return q;
	}
	
	private IEntity getInventoryOptionByID(IExecutionEngine engine, String optionId) 
			throws com.bas.shared.ruleparser.ParseException, ExecutionException, AccessDeniedException, 
					InvalidParameterException {
	
		try {
			Query customQuery = Query.createFromRuleLanguageString(getInventoryOptionByIdQuery(optionId));
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
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
	
	private String getInventoryByIdQuery(String id) {
		
		// Cache the query for reuse
		if(inventoryQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ");
	
			buff.append(this.inventoryBoKey);
			buff.append(" WHERE ");
			buff.append(this.inventoryBoKey);
			buff.append(".");
			
			buff.append(this.inventoryIdKey);
			buff.append("=");
			
			this.inventoryQueryString = buff.toString();
			
		}
		
		String q = this.inventoryQueryString + "'" + id + "'";
		inform("Inventory Rule Query: " + q);
		
		return q;
	}
	
	private IEntity getInventoryByID(IExecutionEngine engine, String inventoryId) 
			throws com.bas.shared.ruleparser.ParseException, ExecutionException, AccessDeniedException, 
					InvalidParameterException {
	
		try {
			Query customQuery = Query.createFromRuleLanguageString(getInventoryByIdQuery(inventoryId));
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
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
	
	private String getProductByIdQuery(String id) {
		
		// Cache the query for reuse
		if(customQueryString == null) {
			StringBuilder buff = new StringBuilder();
			buff.append("FIND ");
	
			buff.append(boName);
			buff.append(" WHERE ");
			buff.append(boName);
			buff.append(".");
			
			buff.append(productIDKey);
			buff.append("=");
			
			this.customQueryString = buff.toString();
			
		}
		
		String q = this.customQueryString + "'" + id + "'";
		inform("Custom Rule Query: " + q);
		
		return q;
	}

	/*
	 * Get the product by product id
	 */
	private IEntity getEntityByProductID(IExecutionEngine engine, String productID) 
			throws com.bas.shared.ruleparser.ParseException, ExecutionException, AccessDeniedException, 
					InvalidParameterException {
		
		try {
			Query customQuery = Query.createFromRuleLanguageString(getProductByIdQuery(productID));
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			IEntity []data =  result.getEntities();
			if(data != null && data.length > 0) {
				String prodId = (String)data[0].getAttributeValue(productIDKey);
				inform("Found Product ID: " + prodId);
				
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
			throws com.bas.shared.ruleparser.ParseException, InvalidTypeException, ExecutionException, 
				AccessDeniedException, InvalidParameterException {
		
		try {
			Query customQuery = Query.createFromRuleLanguageString(getLastDateQuery());
			QueryResult result = engine.executeQuery(this, customQuery, null, null);
			
			//String namedQuery = (keys.getProperty("named_product_time_query") != null) ? keys.getProperty("named_product_time_query") : "get_product_max_modified_time";
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
				} else if(dat instanceof DateTimeHolder) {
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
					throw new InvalidTypeException("Invalid Type");
				}
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
	
	private void updateProduct(Properties keys, Product productJsonObj, IEntity boInstance) throws InvalidParameterException {
		
		// Availability
		String availability = productJsonObj.getAvailability();
		inform(availabilityKey + " (Availability Key) --> " + availability);
		boInstance.setAttributeValue(availabilityKey, availability);

		// Availability_Description
		String availabilityDescription = productJsonObj.getAvailabilityDescription();
		inform(availabilityDescriptionKey + " (Availability_Description Key) --> " + availabilityDescription);
		boInstance.setAttributeValue(availabilityDescriptionKey, availabilityDescription);

		// Bin_Picking_Number
		String binPickingNumber = productJsonObj.getBinPickingNumber();
		inform(binPickingNumberKey + " (Bin_Picking_Number Key) --> " + binPickingNumber);
		boInstance.setAttributeValue(binPickingNumberKey, binPickingNumber);

		// Category
		String category = productJsonObj.getCategory();
		inform(categoryKey + " (Category Key) --> " + category);
		boInstance.setAttributeValue(categoryKey, category);

		// Cost_Price
		String costPrice = productJsonObj.getCostPrice();
		inform(costPriceKey + " (Cost_Price Key) --> " + costPrice);
		boInstance.setAttributeValue(costPriceKey, costPrice);

		SimpleDateFormat format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss");

		try {
			// date_created
			String dateCreated = productJsonObj.getDateCreated();
			inform(dateCreatedKey + " (date_created Key) --> " + dateCreated);
			boInstance.setAttributeValue(dateCreatedKey, format.parse(dateCreated));
		} catch (ParseException e) {
			error("Missing date_created", e);
		}

		try {
			// date_updated
			String dateUpdated = productJsonObj.getDateUpdated();
			inform(dateUpdatedKey + " (date_updated Key) --> " + dateUpdated);
			boInstance.setAttributeValue(dateUpdatedKey, format.parse(dateUpdated));
		} catch (ParseException e) {
			error("Missing date_updated", e);
		}

		// Date_Last_imported
		String dateLastImported = productJsonObj.getDateLastImported();
		inform(dateLastImportedKey + " (Date_Last_imported Key) --> " + dateLastImported);
		boInstance.setAttributeValue(dateLastImportedKey, dateLastImported);

		// Depth
		String depth = productJsonObj.getDepth();
		inform(depthKey + " (Depth Key) --> " + depth);
		boInstance.setAttributeValue(depthKey, depth);

		// Description
		String description = productJsonObj.getDescription();
		inform(descriptionKey + " (Description Key) --> " + description);
		boInstance.setAttributeValue(descriptionKey, description);

		// Fixed_Cost_Shipping_Price
		String fixedCostShippingPrice = productJsonObj.getFixedCostShippingPrice();
		inform(fixedCostShippingPriceKey + " (Fixed_Cost_Shipping_Price Key) --> " + fixedCostShippingPrice);
		boInstance.setAttributeValue(fixedCostShippingPriceKey, fixedCostShippingPrice);

		// Height
		String height = productJsonObj.getHeight();
		inform(heightKey + " (Height Key) --> " + height);
		boInstance.setAttributeValue(heightKey, height);

		// Inventory_Level
		String inventoryLevel = productJsonObj.getInventoryLevel();
		inform(inventoryLevelKey + " (Inventory_Level Key) --> " + inventoryLevel);
		boInstance.setAttributeValue(inventoryLevelKey, inventoryLevel);

		// Inventory_Warning_Level
		String inventoryWarningLevel = productJsonObj.getInventoryWarningLevel();
		inform(inventoryWarningLevelKey + " (Inventory_Warning_Level Key) --> " + inventoryWarningLevel);
		boInstance.setAttributeValue(inventoryWarningLevelKey, inventoryWarningLevel);

		// Inventory_Tracking
		String inventoryTracking = productJsonObj.getInventoryTracking();
		inform(inventoryTrackingKey + " (Inventory_Tracking Key) --> " + inventoryTracking);
		boInstance.setAttributeValue(inventoryTrackingKey, inventoryTracking);

		// Is_Featured
		String isFeatured = productJsonObj.getIsFeatured();
		inform(isFeaturedKey + " (Is_Featured Key) --> " + isFeatured);
		boInstance.setAttributeValue(isFeaturedKey, isFeatured);

		// Is_Free_Shipping
		String isFreeShipping = productJsonObj.getIsFreeShipping();
		inform(isFreeShippingKey + " (Is_Free_Shipping Key) --> " + isFreeShipping);
		boInstance.setAttributeValue(isFreeShippingKey, isFreeShipping);

		// Is_Preorder_Only
		String isPreorderOnly = productJsonObj.getIsPreorderOnly();
		inform(isPreorderOnlyKey + " (Is_Preorder_Only Key) --> " + isPreorderOnly);
		boInstance.setAttributeValue(isPreorderOnlyKey, isPreorderOnly);

		// Is_Visible
		String isVisible = productJsonObj.getIsVisible();
		inform(isVisibleKey + " (Is_Visible Key) --> " + isVisible);
		boInstance.setAttributeValue(isVisibleKey, isVisible);

		// Meta_Description
		String metaDescription = productJsonObj.getMetaDescription();
		inform(metaDescriptionKey + " (Meta_Description Key) --> " + metaDescription);
		boInstance.setAttributeValue(metaDescriptionKey, metaDescription);

		// Meta_Keywords
		String metaKeywords = productJsonObj.getMetaKeywords();
		inform(metaKeywordsKey + " (Meta_Keywords Key) --> " + metaKeywords);
		boInstance.setAttributeValue(metaKeywordsKey, metaKeywords);

		// Name
		String name = productJsonObj.getName();
		inform(nameKey + " (Name Key) --> " + name);
		boInstance.setAttributeValue(nameKey, name);

		// Number_Sold
		String numberSold = productJsonObj.getNumberSold();
		inform(numberSoldKey + " (Number_Sold Key) --> " + numberSold);
		boInstance.setAttributeValue(numberSoldKey, numberSold);

		// Order_Quantity_Maximum
		String orderQuantityMaximum = productJsonObj.getOrderQuantityMaximum();
		inform(orderQuantityMaximumKey + " (Order_Quantity_Maximum Key) --> " + orderQuantityMaximum);
		boInstance.setAttributeValue(orderQuantityMaximumKey, orderQuantityMaximum);

		// Order_Quantity_Minimum
		String orderQuantityMinimum = productJsonObj.getOrderQuantityMinimum();
		inform(orderQuantityMinimumKey + " (Order_Quantity_Minimum Key) --> " + orderQuantityMinimum);
		boInstance.setAttributeValue(orderQuantityMinimumKey, orderQuantityMinimum);

		// Pre_Order_Release_Date
		String preOrderReleaseDate = productJsonObj.getPreOrderReleaseDate();
		inform(preOrderReleaseDateKey + " (Pre_Order_Release_Date Key) --> " + preOrderReleaseDate);
		boInstance.setAttributeValue(preOrderReleaseDateKey, preOrderReleaseDate);

		// Preorder_Message
		String preorderMessage = productJsonObj.getPreorderMessage();
		inform(preorderMessageKey + " (Preorder_Message Key) --> " + preorderMessage);
		boInstance.setAttributeValue(preorderMessageKey, preorderMessage);

		// Price
		String price = productJsonObj.getPrice();
		inform(priceKey + " (Price Key) --> " + price);
		boInstance.setAttributeValue(priceKey, price);

		// Product_ID
		String productID = productJsonObj.getProductID();
		inform(productIDKey + " (Product_ID Key) --> " + productID);
		boInstance.setAttributeValue(productIDKey, productID);

		// Rating_Count
		String ratingCount = productJsonObj.getRatingCount();
		inform(ratingCountKey + " (Rating_Count Key) --> " + ratingCount);
		boInstance.setAttributeValue(ratingCountKey, ratingCount);

		// Rating_Total
		String ratingTotal = productJsonObj.getRatingTotal();
		inform(ratingTotalKey + " (Rating_Total Key) --> " + ratingTotal);
		boInstance.setAttributeValue(ratingTotalKey, ratingTotal);

		// Related_Products
		String relatedProducts = productJsonObj.getRelatedProducts();
		inform(relatedProductsKey + " (Related_Products Key) --> " + relatedProducts);
		boInstance.setAttributeValue(relatedProductsKey, relatedProducts);

		// Retail_Price
		String retailPrice = productJsonObj.getRetailPrice();
		inform(retailPriceKey + " (Retail_Price Key) --> " + retailPrice);
		boInstance.setAttributeValue(retailPriceKey, retailPrice);

		// Sale_Price
		String salePrice = productJsonObj.getSalePrice();
		inform(salePriceKey + " (Sale_Price Key) --> " + salePrice);
		boInstance.setAttributeValue(salePriceKey, salePrice);

		// Search_Keywords
		String searchKeywords = productJsonObj.getSearchKeywords();
		inform(searchKeywordsKey + " (Search_Keywords Key) --> " + searchKeywords);
		boInstance.setAttributeValue(searchKeywordsKey, searchKeywords);

		// SKU
		String sKU = productJsonObj.getSKU();
		inform(sKUKey + " (SKU Key) --> " + sKU);
		boInstance.setAttributeValue(sKUKey, sKU);

		// Sort_Order
		String sortOrder = productJsonObj.getSortOrder();
		inform(sortOrderKey + " (Sort_Order Key) --> " + sortOrder);
		boInstance.setAttributeValue(sortOrderKey, sortOrder);

		// Total_Sold
		String totalSold = productJsonObj.getTotalSold();
		inform(totalSoldKey + " (Total_Sold Key) --> " + totalSold);
		boInstance.setAttributeValue(totalSoldKey, totalSold);

		// Type
		String type = productJsonObj.getType();
		inform(typeKey + " (Type Key) --> " + type);
		boInstance.setAttributeValue(typeKey, type);

		// UPC
		String uPC = productJsonObj.getUPC();
		inform(uPCKey + " (UPC Key) --> " + uPC);
		boInstance.setAttributeValue(uPCKey, uPC);

		// View_Count
		String viewCount = productJsonObj.getViewCount();
		inform(viewCountKey + " (View_Count Key) --> " + viewCount);
		boInstance.setAttributeValue(viewCountKey, viewCount);

		// Warranty
		String warranty = productJsonObj.getWarranty();
		inform(warrantyKey + " (Warranty Key) --> " + warranty);
		boInstance.setAttributeValue(warrantyKey, warranty);

		// Weight
		String weight = productJsonObj.getWeight();
		inform(weightKey + " (Weight Key) --> " + weight);
		boInstance.setAttributeValue(weightKey, weight);

		// Width
		String width = productJsonObj.getWidth();
		inform(widthKey + " (Width Key) --> " + width);
		boInstance.setAttributeValue(widthKey, width);

	}
	
	/**
	 * Logs the message into the console
	 * 
	 * @param message
	 *            The message to be logged into console
	 */
	private void inform(String message) {
		//System.out.println(message);
		log.info("[BSV Products] " + message);
	}
	
	private void error(String message, Throwable t) {
		//System.out.println(message);
		if( t != null) {
			log.error("[BSV Products] ", t);
		} else {
			log.error("[BSV Products] ");			
		}
	}

	private String getISO8601Date(Date date) {
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String d = parser.format(date);
		return d.replace(" ", "T");
	}
	
	private void logMessageToBSV(IExecutionEngine engine, String action, String message, String status) {
		

		try {
			IEntity errorBo = engine.createEntity(this, this.errorBoName);
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
		String storeUrl = "https://store-r5adzn.mybigcommerce.com/api/v2";
		String username = "thomas_hii";
		String password = "ddee9404ad8c2f5698dae9270879fd7d3abb78ac";
	     BigCommerceConnector conn = new BigCommerceConnector(storeUrl, username, password);
	     
	     SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     
	     String d = parser.format(Calendar.getInstance().getTime());
	     
	     System.out.println("Time: " + d.replace(" ", "T"));
   	 
	     try {
		     @SuppressWarnings({ "unchecked" })
			 //List<Product> productList = conn.serviceGET("/products?min_date_modified=" + d, "", new ProductJsonParser());
			 List<Product> productList = conn.serviceGET("/products", "", new ProductJsonParser());
		     for(Product rec: productList) {
		    	 System.out.println("Data: " + rec.getName());
		     }
	     
	     } catch (Exception e) {
	    	 
	     }
		
	}
	*/

}
