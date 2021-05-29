package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.Address;

public class AddressJsonParser extends BigCommerceJsonParser<Address> {

	@Override
	protected Address mapJsonOject(JsonNode node) {
		Address address = new Address();
		address.setId(node.get("id").asText());
		address.setCustomerId(node.get("customer_id").asText());
		address.setFirstName(node.get("first_name").asText());
		address.setLastName(node.get("last_name").asText());
		address.setCompany(node.get("company").asText());
		address.setStreet1(node.get("street_1").asText());
		address.setStreet2(node.get("street_2").asText());
		address.setCity(node.get("city").asText());
		address.setState(node.get("state").asText());
		address.setZip(node.get("zip").asText());
		address.setCountry(node.get("country").asText());
		address.setCountryIso2(node.get("country_iso2").asText());
		address.setPhone(node.get("phone").asText());
		address.setAddressType(node.get("address_type").asText());		
		return address;
	}

}
