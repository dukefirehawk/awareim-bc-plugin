package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.Option;

public class OptionJsonParser extends BigCommerceJsonParser<Option> {

	@Override
	protected Option mapJsonOject(JsonNode node) {
		
		Option opt = new Option();
		opt.setId(node.get("id").asText());
		opt.setDisplayName(node.get("display_name").asText());
		opt.setIsRequired(node.get("is_required").asBoolean());
		opt.setOptionId(node.get("option_id").asText());
		opt.setSortOrder(node.get("sort_order").asText());
		
		return opt;
	}

}
