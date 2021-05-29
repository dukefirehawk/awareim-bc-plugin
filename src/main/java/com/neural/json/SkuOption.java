package com.neural.json;

import java.io.Serializable;

public class SkuOption implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8191922295911295624L;
	
	private String id;
	
	private String displayName;
	private String label;
	private String productOptionId;
    private String optionValueId;
    
	public String getProductOptionId() {
		return productOptionId;
	}
	public void setProductOptionId(String productOptionId) {
		this.productOptionId = productOptionId;
	}
	public String getOptionValueId() {
		return optionValueId;
	}
	public void setOptionValueId(String optionValueId) {
		this.optionValueId = optionValueId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

}
