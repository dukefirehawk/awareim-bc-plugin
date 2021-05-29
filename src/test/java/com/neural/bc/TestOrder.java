package com.neural.bc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neural.BigCommerceConnector;
import com.neural.BigCommerceException;
import com.neural.json.Address;
import com.neural.json.Order;
import com.neural.json.OrderItem;
import com.neural.json.ProductOption;


public class TestOrder {
	 private String username = "neural-test";
	 private String storeUrl = "https://store-436my6p.mybigcommerce.com/api/v2/";
	 private String apikey = "9aee3113f5bc6c9d8c260616ee48148817322966";

	public void doTest() {
		try {
	  	 	BigCommerceConnector conn = new BigCommerceConnector(storeUrl, username, apikey);
			Address addr = new Address();
			//addr.setFirstName("Miriam");
			//addr.setCompany("Acme Pte");
			//addr.setLastName("Loke");
			//addr.setStreet1("Sussex St");
			//addr.setStreet2("");
			//addr.setCity("Austin");
			//addr.setState("Texas");
			//addr.setZip("787878");
			//addr.setCountry("United States");
			//addr.setCountryIso2("US");
			//addr.setPhone("111-222-333");

			addr.setFirstName("Test3");
			addr.setCompany("Test3");
			addr.setLastName("Test3");
			addr.setStreet1("Test3");
			addr.setStreet2("");
			addr.setCity("Test");
			addr.setState("Test");
			addr.setPhone("12345");
			addr.setZip("4000");
			addr.setCountry("United States");
			addr.setCountryIso2("US");
			addr.setEmail("aaa3@gmail.com");
			
			OrderItem[] orderItem = new OrderItem[2];
			orderItem[0] = new OrderItem();
			orderItem[0].setQuantity(Double.parseDouble("1"));
			orderItem[0].setProductId("32");
			
			orderItem[1] = new OrderItem();
			orderItem[1].setQuantity(Double.parseDouble("3"));
			orderItem[1].setProductId("33");
			
			ProductOption options[] = new ProductOption[1];
			options[0] = new ProductOption();
			options[0].setId("87");
			options[0].setValue("7");
			orderItem[1].setProductOptions(options);
			
			Order order = new Order();
			order.setOrderItems(orderItem);
			order.setBillingAddress(addr);
			order.setStatusId(11);
			order.setCustomerId(1);
			
	   	 	ObjectMapper mapper = new ObjectMapper();
			String jsonInString = mapper.writeValueAsString(order);
			System.out.println("JSON: " + jsonInString);
			
 			String result = conn.servicePost("orders", jsonInString);
 			
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
		TestOrder test = new TestOrder();
		
		test.doTest();

	}

}
