package com.neural.bc;

import java.util.Calendar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neural.BigCommerceConnector;
import com.neural.BigCommerceException;
import com.neural.datasync.utils.ParserUtil;
import com.neural.json.Coupon;
import com.neural.json.Group;

public class TestCoupon {
	 private String username = "neural-test";
	 private String storeUrl = "https://store-436my6p.mybigcommerce.com/api/v2/";
	 private String apikey = "9aee3113f5bc6c9d8c260616ee48148817322966";

	public void doTest() {
  	 
  	 	try {
	  	 	BigCommerceConnector conn = new BigCommerceConnector(storeUrl, username, apikey);
	   	 	ObjectMapper mapper = new ObjectMapper();
	   	 	
	   	 	Group g = new Group();
	   	 	Integer []ids = { 0 };
	   	 	g.setIds(ids);
	   	 	g.setEntity("categories");
	   	 	Coupon c = new Coupon();
	   	 	
	   	 	c.setCode("test_kk");
	   	 	c.setName("test_kk");
	   	 	c.setAmount("3.00");
	   	 	c.setType("per_item_discount");
	   	 	c.setAppliesTo(g);
	   	 	//c.setExpires(ParserUtil.getDateAsRFC822String(Calendar.getInstance().getTime()));
	   	 	c.setExpires(ParserUtil.getDateAsRFC822String(null));
 			String jsonInString = mapper.writeValueAsString(c);
			System.out.println("Data: " + jsonInString);
			
 			String result = conn.servicePost("coupons", jsonInString);
 			
 			System.out.println("Result: " + result);
 			
 		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 		} catch (BigCommerceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   	 	
		
	}
	
	public static void main(String[] args) {
		TestCoupon test = new TestCoupon();
		
		test.doTest();

	}

}
