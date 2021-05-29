package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.Category;

public class CategoryJsonParser extends BigCommerceJsonParser<Category> {
	
	@Override
	protected Category mapJsonOject(JsonNode node) {
		Category cat = new Category();
		
		cat.setCategoryId(node.get("id").asText());
		cat.setDescription(node.get("description").asText());
		cat.setImageFile(node.get("image_file").asText());
		cat.setIsVisible(node.get("is_visible").asText());
		cat.setLayoutFile(node.get("layout_file").asText());
		cat.setMetaDescription(node.get("meta_description").asText());
		cat.setMetaKeywords(node.get("meta_keywords").asText());
		cat.setName(node.get("name").asText());
		cat.setPageTitle(node.get("page_title").asText());
		cat.setParentId(node.get("parent_id").asText());
		cat.setSearchKeywords(node.get("search_keywords").asText());
		cat.setSortOrder(node.get("sort_order").asText());
		cat.setUrl(node.get("url").asText());
		
		return cat;
	}
	

}
