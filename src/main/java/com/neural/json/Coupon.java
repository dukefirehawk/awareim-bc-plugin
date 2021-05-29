package com.neural.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Coupon {

	@JsonIgnore
	@JsonProperty("id")
	private String couponId;
	
	private String code;
	private String name;
	private String type;
	private String amount;
	private Boolean enabled = true;

	@JsonIgnore
	@JsonProperty("max_uses")
	private String maxUses;
	
	/* Format: Thu, 31 Jan 2013 00:00:00 +0000 */
	//JsonIgnore
	private String expires;
	
	@JsonIgnore
	@JsonProperty("num_uses")
	private String numUses;
	
	@JsonIgnore
	@JsonProperty("max_uses_per_customer")
	private String maxUsesPerCustomer;
	
	@JsonProperty("applies_to")
	private Group appliesTo;
	
	@JsonIgnore
	@JsonProperty("min_purchase")
	private String minPurchase;
	
	@JsonIgnore
	public String getCouponId() {
		return couponId;
	}
	
	@JsonProperty
	public void setCouponId(String couponId) {
		this.couponId = couponId;
	}
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@JsonIgnore
	public String getMaxUses() {
		return maxUses;
	}
	
	public void setMaxUses(String maxUses) {
		this.maxUses = maxUses;
	}

	//JsonIgnore
	public String getExpires() {
		return expires;
	}
	
	public void setExpires(String expires) {
		this.expires = expires;
	}

	@JsonIgnore	
	public String getNumUses() {
		return numUses;
	}

	@JsonProperty
	public void setNumUses(String numUses) {
		this.numUses = numUses;
	}

	@JsonIgnore
	public String getMaxUsesPerCustomer() {
		return maxUsesPerCustomer;
	}
	
	public void setMaxUsesPerCustomer(String maxUsesPerCustomer) {
		this.maxUsesPerCustomer = maxUsesPerCustomer;
	}
	
	public Group getAppliesTo() {
		return appliesTo;
	}
	public void setAppliesTo(Group appliesTo) {
		this.appliesTo = appliesTo;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	@JsonIgnore
	public String getMinPurchase() {
		return minPurchase;
	}

	public void setMinPurchase(String minPurchase) {
		this.minPurchase = minPurchase;
	}	
	
}
