package com.neural.json.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;

public abstract class BigCommerceJsonParser<T> implements IBigCommerceParser<T> {
	
	public List<T> parse(Reader reader) throws Exception {
		
		List<T> objList = new ArrayList<>();
		
		JsonFactory f = new MappingJsonFactory();
		
		//JsonParser jp = f.createParser(data);
		JsonParser jp = f.createParser(reader);
		
		/* 
		 * Assume JSON data: [{a, b, c }]
		 */
		JsonToken current = jp.nextToken();
		if (current != JsonToken.START_ARRAY) {
			//System.out.println("Error: root should be object: quiting.");
			JsonNode node = jp.readValueAsTree();
			if(node != null) {
				objList.add(mapJsonOject(node));
			}
			
			return objList;
		}
		while ( (current = jp.nextToken()) != JsonToken.END_ARRAY) {
			JsonNode node = jp.readValueAsTree();
			// And now we have random access to everything in the
			// object
			
			if(node != null) {
				/*
				Iterator<String> il = node.fieldNames();
				while(il.hasNext()) {
					String key = il.next();
					int size = node.get(key).asText().length();
					if( size > 1000) {
						System.out.println("Field Name: " + key + ", " + size);
					}
				}
				*/
			
				objList.add(mapJsonOject(node));
			}
		}
		
		return objList;
	}
	
	protected abstract T mapJsonOject(JsonNode node); 

}
