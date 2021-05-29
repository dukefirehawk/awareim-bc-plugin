package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.Customer;

public class CustomerJsonParser extends BigCommerceJsonParser<Customer> {
	
	@Override
	protected Customer mapJsonOject(JsonNode node) {
		Customer customer = new Customer();
		customer.setCustomerId(node.get("id").asText());
		customer.setCompany(node.get("company").asText());
		customer.setCustomerGroupId(node.get("customer_group_id").asText());
		customer.setDateCreated(node.get("date_created").asText());
		customer.setDateUpdated(node.get("date_modified").asText());
		customer.setEmailAddress(node.get("email").asText());
		customer.setFirstName(node.get("first_name").asText());
		customer.setLastName(node.get("last_name").asText());
		customer.setNotes(node.get("notes").asText());
		customer.setPhone(node.get("phone").asText());
		customer.setRegistrationIpAddress(node.get("registration_ip_address").asText());
		customer.setStoreCredit(node.get("store_credit").asText());
		//customer.setAddressOne(node.get("address_one").asText());
		//customer.setAddressTwo(node.get("address_two").asText());
		//customer.setAddressType(node.get("address_type").asText());
		//customer.setCity(node.get("city").asText());
		//customer.setState(node.get("state").asText());
		//customer.setCountry(node.get("country").asText());
		//customer.setCountryCode(node.get("country_code").asText());
		//customer.setZip(node.get("zip").asText());
		
		return customer;
	}
}
