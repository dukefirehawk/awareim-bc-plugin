package com.neural.bc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.neural.BigCommerceConnector;
import com.neural.BigCommerceException;
import com.neural.json.Option;
import com.neural.json.OptionValue;
import com.neural.json.Product;
import com.neural.json.Sku;
import com.neural.json.SkuOption;
import com.neural.json.parser.ProductJsonParser;

public class TestGetProduct {
	 private String username = "neural-test";
	 private String storeUrl = "https://store-436my6p.mybigcommerce.com/api/v2/";
	 private String apikey = "9aee3113f5bc6c9d8c260616ee48148817322966";

	public void doTest() {
  	 
  	 	try {
  	    	String path = "products";
  	    	String data = "";

  	    	BigCommerceConnector conn = new BigCommerceConnector(storeUrl, username, apikey);
	  	 	@SuppressWarnings("unchecked")
			List<Product> recList = conn.serviceGET(path, data, new ProductJsonParser());

			if (!recList.isEmpty()) {
				System.out.println("Product: " + recList.size());
				
				Map<String, Option> optionMap = new HashMap<String, Option>();
				Map<String, OptionValue> optionValueMap = new HashMap<String, OptionValue>();
				for (Product rec : recList) {
					System.out.println("Product Id: " + rec.getProductID());
					/*
					List<Option> optionList = conn.getProductOption(rec.getProductID());
					System.out.println("Product: " + rec.getName());
					System.out.println("Product Id: " + rec.getProductID());
					System.out.println("Option Set Id: " + rec.getOptionSetId());
					if (!optionList.isEmpty()) {
						for (Option opt : optionList) {
							System.out.println("Option Id: " + opt.getId());
							System.out.println("Display Name: " + opt.getDisplayName());
						}

						List<OptionSet> optionSetList = conn.getProductOptionSet(rec.getOptionSetId());
						for (OptionSet optSet : optionSetList) {
							System.out.println("Option Id: " + optSet.getOptionId());
						}

					}
					*/
					List<Sku> skuList = conn.getProductSku(rec.getProductID());
					if(!skuList.isEmpty()) {
						for(Sku sku: skuList) {
							System.out.println("Sku: " + sku.getSku());
							System.out.println("Inventory Level: " + sku.getInventoryLevel());
							SkuOption skuOpt[] = sku.getOptions();
							if(skuOpt.length > 0) {
								StringBuilder desc = new StringBuilder();
								for(int j=0; j<skuOpt.length; j++) {
									System.out.println("Sku (Product Option Id): " + skuOpt[j].getProductOptionId());
									System.out.println("Sku (Option Value Id): " + skuOpt[j].getOptionValueId());

									String optionId = skuOpt[j].getProductOptionId();
									String optionValueId = skuOpt[j].getOptionValueId();
									Option opt = optionMap.get(optionId);
									if(opt == null) {
										List<Option> optionList = conn.getProductOptionById(rec.getProductID(), optionId);
										opt = optionList.get(0);
										optionMap.put(optionId, opt);
									}
									String key = opt.getOptionId() + "_" + optionValueId;
									OptionValue optValue = optionValueMap.get(key);
									if(optValue == null) {
										List<OptionValue> optValueList = conn.getOptionValueById(opt.getOptionId(), optionValueId);
										optValue = optValueList.get(0);
										optionValueMap.put(key, optValue);
									}

									System.out.println("Product Option Id : " + opt.getId());
									System.out.println("Option Id : " + opt.getOptionId());
									System.out.println(" ");
									desc.append(opt.getDisplayName() + ":" + optValue.getLabel() + " ");
									
								}
								System.out.println("Options : " + desc);
								System.out.println(" ");
							}
						}
						
					}
					

					System.out.println("");
				}
			}
 			
 		} catch (BigCommerceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	 	
		
	}
	
	public static void main(String[] args) {
		TestGetProduct test = new TestGetProduct();
		
		test.doTest();

	}

}
