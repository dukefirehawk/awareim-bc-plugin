package com.neural.json;

import java.io.Serializable;

public class OptionValue implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3799711655843776225L;
	
	private String 	id;
	private String 	label;
	private String 	sortOrder;
	private Boolean isDefault;
	private String 	value;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
	public Boolean getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
}
