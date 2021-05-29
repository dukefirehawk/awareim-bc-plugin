package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.OptionSet;
import com.neural.json.OptionValue;

public class OptionSetJsonParser extends BigCommerceJsonParser<OptionSet> {

	@Override
	protected OptionSet mapJsonOject(JsonNode node) {
		OptionSet rec = new OptionSet();
		rec.setId(node.get("id").asText());
		rec.setOptionId(node.get("option_id").asText());
		rec.setOptionSetId(node.get("option_set_id").asText());
		
		JsonNode valueNode = node.get("values");
		if(valueNode.isArray()){
			
			OptionValue []optVal =  new OptionValue[valueNode.size()];
			for(int i=0; i<valueNode.size(); i++) {
				JsonNode jn = valueNode.get(i);
				optVal[i] = new OptionValue();
				optVal[i].setId(jn.get("option_value_id").asText());
				optVal[i].setIsDefault(jn.get("is_default").asBoolean());
				optVal[i].setLabel(jn.get("label").asText());
				optVal[i].setSortOrder(jn.get("sort_order").asText());
				optVal[i].setValue(jn.get("value").asText());
				
			}
			if(optVal.length > 0){
				rec.setOptionValues(optVal);
			}
		}
		
		return rec;
	}

}
