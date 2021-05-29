package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.Order;

public class OrderJsonParser extends BigCommerceJsonParser<Order> {
	
	@Override
	protected Order mapJsonOject(JsonNode node) {
		Order order = new Order();
		order.setBaseHandlingCost(node.get("base_handling_cost").asDouble());
		order.setBaseShippingCost(node.get("base_shipping_cost").asDouble());
		order.setBaseWrappingCost(node.get("base_wrapping_cost").asDouble());
		order.setCurrencyCode(node.get("currency_code").asText());
		order.setCurrencyExchangeRate(node.get("currency_exchange_rate").asText());
		order.setCustomerMessage(node.get("customer_message").asText());
		order.setDateCreated(node.get("date_created").asText());
		order.setDateShipped(node.get("date_shipped").asText());
		order.setDateUpdated(node.get("date_modified").asText());
		order.setDefaultCurrencyCode(node.get("default_currency_code").asText());
		order.setDiscountAmount(node.get("discount_amount").asDouble());
		order.setDouponDiscount(node.get("coupon_discount").asText());
		order.setExternalSource(node.get("external_source").asText());
		order.setGeoipCountry(node.get("geoip_country").asText());
		order.setGeoipCountryIso2(node.get("geoip_country_iso2").asText());
		order.setGiftCertificateAmount(node.get("gift_certificate_amount").asText());
		order.setHandlingCostExTax(node.get("handling_cost_ex_tax").asDouble());
		order.setHandlingCostIncTax(node.get("handling_cost_inc_tax").asDouble());
		order.setHandlingCostTax(node.get("handling_cost_tax").asText());
		order.setIpAddress(node.get("ip_address").asText());
		order.setItemsShipped(node.get("items_shipped").asText());
		order.setItemsTotal(node.get("items_total").asText());
		order.setOrderId(node.get("id").asText());
		order.setOrderIsDigital(node.get("order_is_digital").asText());
		order.setOrderSource(node.get("order_source").asText());
		order.setPaymentMethod(node.get("payment_method").asText());
		order.setPaymentStatus(node.get("payment_status").asText());
		order.setRefundedAmount(node.get("refunded_amount").asDouble());
		order.setShippingCostExTax(node.get("shipping_cost_ex_tax").asDouble());
		order.setShippingCostTax(node.get("shipping_cost_tax").asText());
		order.setStaffNotes(node.get("staff_notes").asText());
		order.setStatus(node.get("status").asText());
		order.setStoreCreditAmount(node.get("store_credit_amount").asText());
		order.setSubtotalIncTax(node.get("subtotal_inc_tax").asDouble());
		order.setSubtotalTax(node.get("subtotal_tax").asText());
		order.setTotalExTax(node.get("total_ex_tax").asDouble());
		order.setWrappingCostExTax(node.get("wrapping_cost_ex_tax").asDouble());
		order.setWrappingCostIncTax(node.get("wrapping_cost_inc_tax").asDouble());
		order.setWrappingCostTax(node.get("wrapping_cost_tax").asText());
		order.setCostShipping(node.get("shipping_cost_inc_tax").asText());
		order.setCostSubtotal(node.get("subtotal_inc_tax").asText());
		order.setCostTax(node.get("total_tax").asText());
		order.setSaleAmount(node.get("total_inc_tax").asText());
		
		JsonNode n = node.get("billing_address");
		//System.out.println("Node : " + n.findValue("first_name").textValue());
		order.setAddressOne(n.findValue("street_1").textValue());
		order.setAddressTwo(n.findValue("street_2").textValue());
		order.setCity(n.findValue("city").textValue());
		order.setCompany(n.findValue("company").textValue());
		order.setCountry(n.findValue("country").textValue());
		order.setFirstName(n.findValue("first_name").textValue());
		order.setLastName(n.findValue("last_name").textValue());
		order.setState(n.findValue("state").textValue());		
		order.setZip(n.findValue("zip").textValue());
		order.setEmailAddress(n.findValue("email").textValue());
		order.setPhone(n.findValue("phone").textValue());
		
		return order;
	}
}
