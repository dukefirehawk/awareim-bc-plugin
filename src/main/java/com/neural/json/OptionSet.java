package com.neural.json;

import java.io.Serializable;

public class OptionSet implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 3167380034510240398L;
	
	private String id;
    private String optionSetId;
	private String optionId;
	
	private OptionValue[] optionValues;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOptionSetId() {
		return optionSetId;
	}

	public void setOptionSetId(String optionSetId) {
		this.optionSetId = optionSetId;
	}

	public String getOptionId() {
		return optionId;
	}

	public void setOptionId(String optionId) {
		this.optionId = optionId;
	}

	public OptionValue[] getOptionValues() {
		return optionValues;
	}

	public void setOptionValues(OptionValue[] optionValues) {
		this.optionValues = optionValues;
	}

	public void addOptionValue(int idx, OptionValue optionValue) {
		if(idx < 0 || idx >= this.optionValues.length) {
			return;
		}
		this.optionValues[idx] = optionValue;
	}

}
