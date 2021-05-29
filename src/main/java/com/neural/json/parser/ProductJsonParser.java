package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.Product;

public class ProductJsonParser extends BigCommerceJsonParser<Product> {
	
	@Override
	protected Product mapJsonOject(JsonNode node) {
		Product product = new Product();
		product.setAvailability(node.get("availability").asText());
		product.setAvailabilityDescription(node.get("availability_description").asText());
		product.setBinPickingNumber(node.get("bin_picking_number").asText());
		product.setCategory(node.get("categories").asText());
		product.setCostPrice(node.get("cost_price").asText());
		product.setDateCreated(node.get("date_created").asText());
		product.setDateLastImported(node.get("date_last_imported").asText());
		product.setDateUpdated(node.get("date_modified").asText());
		product.setDepth(node.get("depth").asText());
		product.setDescription(node.get("description").asText());
		product.setFixedCostShippingPrice(node.get("fixed_cost_shipping_price").asText());
		product.setHeight(node.get("height").asText());
		product.setInventoryLevel(node.get("inventory_level").asText());
		product.setInventoryWarningLevel(node.get("inventory_warning_level").asText());
		product.setInventoryTracking(node.get("inventory_tracking").asText());
		product.setIsFeatured(node.get("is_featured").asText());
		product.setIsFreeShipping(node.get("is_free_shipping").asText());
		product.setIsPreorderOnly(node.get("is_preorder_only").asText());
		product.setIsVisible(node.get("is_visible").asText());
		product.setMetaDescription(node.get("meta_description").asText());
		product.setMetaKeywords(node.get("meta_keywords").asText());
		product.setName(node.get("name").asText());
		product.setNumberSold(node.get("total_sold").asText());
		product.setOrderQuantityMaximum(node.get("order_quantity_maximum").asText());
		product.setOrderQuantityMinimum(node.get("order_quantity_minimum").asText());
		product.setPreOrderReleaseDate(node.get("preorder_release_date").asText());
		product.setPreorderMessage(node.get("preorder_message").asText());
		product.setPrice(node.get("price").asText());
		product.setProductID(node.get("id").asText());
		product.setRatingCount(node.get("rating_count").asText());
		product.setRatingTotal(node.get("rating_total").asText());
		product.setRelatedProducts(node.get("related_products").asText());
		product.setRetailPrice(node.get("retail_price").asText());
		product.setSalePrice(node.get("sale_price").asText());
		product.setSearchKeywords(node.get("search_keywords").asText());
		product.setSKU(node.get("sku").asText());
		product.setSortOrder(node.get("sort_order").asText());
		product.setTotalSold(node.get("total_sold").asText());
		product.setType(node.get("type").asText());
		product.setUPC(node.get("upc").asText());
		product.setViewCount(node.get("view_count").asText());
		product.setWarranty(node.get("warranty").asText());
		product.setWeight(node.get("weight").asText());
		product.setWidth(node.get("width").asText());
		product.setOptionSetId(node.get("option_set_id").asText());
		
		// Added to get Option Set

		return product;
	}
	

}
