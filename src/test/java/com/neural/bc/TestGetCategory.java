package com.neural.bc;

import java.util.List;

import com.neural.BigCommerceConnector;
import com.neural.BigCommerceException;
import com.neural.json.Category;
import com.neural.json.parser.CategoryJsonParser;

public class TestGetCategory {
	 private String username = "neural-test";
	 private String storeUrl = "https://store-436my6p.mybigcommerce.com/api/v2/";
	 private String apikey = "9aee3113f5bc6c9d8c260616ee48148817322966";

	public void doTest() {
  	 
  	 	try {
  	    	String path = "categories";
  	    	String data = "";

	  	 	BigCommerceConnector conn = new BigCommerceConnector(storeUrl, username, apikey);
	  	 	@SuppressWarnings("unchecked")
			List<Category> recList = conn.serviceGET(path, data, new CategoryJsonParser());

			if(!recList.isEmpty()) {
	        	 System.out.println("Category: " + recList.size());
	        	 for(Category rec: recList) {
	        		 System.out.println("Category: " + rec.getName());
	        	 }
	    	 }
 			
 		} catch (BigCommerceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	 	
		
	}
	
	public static void main(String[] args) {
		TestGetCategory test = new TestGetCategory();
		
		test.doTest();

	}

}
