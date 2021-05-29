package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.OrderItem;

public class OrderItemJsonParser extends BigCommerceJsonParser<OrderItem> {

	@Override
	protected OrderItem mapJsonOject(JsonNode node) {
		OrderItem orderItem = new OrderItem();
		orderItem.setItemId(node.get("id").asText());
		orderItem.setName(node.get("name").asText());
		orderItem.setSku(node.get("sku").asText());
		orderItem.setPriceExTax(node.get("price_ex_tax").asText());
		orderItem.setPriceIncTax(node.get("price_inc_tax").asText());
		orderItem.setQuantity(node.get("quantity").asDouble());
		orderItem.setWeight(node.get("weight").asText());
		orderItem.setOrderId(node.get("order_id").asText());
		orderItem.setProductId(node.get("product_id").asText());

		/*
		orderItem.setUpc(node.get("upc").asText());
		orderItem.setCategories(node.get("categories").asText());
		orderItem.setDateCreated(node.get("date_created").asText());
		orderItem.setDateUpdated(node.get("date_updated").asText());
		orderItem.setDescription(node.get("description").asText());
		orderItem.setHTMLDescription(node.get("HTML_Description").asText());
		*/
		
		return orderItem;
	}

}
