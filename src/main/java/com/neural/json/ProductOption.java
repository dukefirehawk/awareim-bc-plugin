package com.neural.json;

import java.io.Serializable;

public class ProductOption implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6710002616415032514L;
	
	private String id;
	private String value;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
