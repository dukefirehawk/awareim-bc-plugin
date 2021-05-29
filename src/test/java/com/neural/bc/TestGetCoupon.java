package com.neural.bc;

import java.util.List;

import com.neural.BigCommerceConnector;
import com.neural.BigCommerceException;
import com.neural.json.Coupon;
import com.neural.json.parser.CouponJsonParser;

public class TestGetCoupon {
	 private String username = "neural-test";
	 private String storeUrl = "https://store-436my6p.mybigcommerce.com/api/v2/";
	 private String apikey = "9aee3113f5bc6c9d8c260616ee48148817322966";

	public void doTest() {
  	 
  	 	try {
  	    	String path = "coupons";
  	    	String data = "";

  	    	BigCommerceConnector conn = new BigCommerceConnector(storeUrl, username, apikey);
	  	 	@SuppressWarnings("unchecked")
			List<Coupon> recList = conn.serviceGET(path, data, new CouponJsonParser());

			if(!recList.isEmpty()) {
	        	 System.out.println("Coupon: " + recList.size());
	        	 for(Coupon rec: recList) {
	        		 System.out.println("Coupon: " + rec.getName());
	        	 }
	    	 }
 			
 		} catch (BigCommerceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	 	
		
	}
	
	public static void main(String[] args) {
		TestGetCoupon test = new TestGetCoupon();
		
		test.doTest();

	}

}
