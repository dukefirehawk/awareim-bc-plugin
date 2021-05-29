package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.Coupon;

public class CouponJsonParser extends BigCommerceJsonParser<Coupon> {
	
	@Override
	protected Coupon mapJsonOject(JsonNode node) {
		Coupon coupon = new Coupon();
		
		coupon.setCouponId(node.get("id").asText());
		coupon.setName(node.get("name").asText());
		coupon.setType(node.get("type").asText());
		coupon.setAmount(node.get("amount").asText());
		coupon.setMinPurchase(node.get("min_purchase").asText());
		coupon.setExpires(node.get("expires").asText());
		coupon.setEnabled(node.get("enabled").asBoolean());
		coupon.setCode(node.get("code").asText());
		coupon.setNumUses(node.get("num_uses").asText());
		coupon.setMaxUses(node.get("max_uses").asText());
		coupon.setMaxUsesPerCustomer(node.get("max_uses_per_customer").asText());

		return coupon;
	}
	

}
