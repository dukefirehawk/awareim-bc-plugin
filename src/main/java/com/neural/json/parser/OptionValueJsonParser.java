package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.OptionValue;

public class OptionValueJsonParser extends BigCommerceJsonParser<OptionValue> {

	@Override
	protected OptionValue mapJsonOject(JsonNode node) {
		OptionValue opt = new OptionValue();
		opt.setId(node.get("id").asText());
		opt.setIsDefault(node.get("is_default").asBoolean());
		opt.setLabel(node.get("label").asText());
		opt.setSortOrder(node.get("sort_order").asText());
		opt.setValue(node.get("value").asText());
		
		return opt;
	}

}
