package com.neural.json.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.neural.json.Sku;
import com.neural.json.SkuOption;

public class SkuJsonParser extends BigCommerceJsonParser<Sku> {
	
	@Override
	protected Sku mapJsonOject(JsonNode node) {
		Sku rec = new Sku();
		rec.setId(node.get("id").asText());
		rec.setBinPickingNumber(node.get("bin_picking_number").asText());
		rec.setCostPrice(node.get("cost_price").asText());
		rec.setInventoryLevel(node.get("inventory_level").asText());
		rec.setInventoryWarningLevel(node.get("inventory_warning_level").asText());
		rec.setProductId(node.get("product_id").asText());
		rec.setSku(node.get("sku").asText());
		rec.setUpc(node.get("upc").asText());
		
		JsonNode optionNode = node.get("options");
		if(optionNode.isArray()){

			SkuOption []skuOptions = new SkuOption[optionNode.size()];
			for(int i=0; i<optionNode.size(); i++) {
				JsonNode jn = optionNode.get(i);
				skuOptions[i] = new SkuOption();
				skuOptions[i].setProductOptionId(jn.get("product_option_id").asText());
				skuOptions[i].setOptionValueId(jn.get("option_value_id").asText());
			}
			rec.setOptions(skuOptions);
		}
		return rec;
	}
	

}
