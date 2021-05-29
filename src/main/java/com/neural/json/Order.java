package com.neural.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Order implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3040834239096255680L;
	
	@JsonIgnore
	private String addressOne;

	@JsonIgnore
	private String addressTwo;

	@JsonProperty("base_handling_cost")
	private Double baseHandlingCost = 0.0;

	@JsonProperty("base_shipping_cost")
	private Double baseShippingCost = 0.0;

	@JsonProperty("base_wrapping_cost")
	private Double baseWrappingCost = 0.0;

	@JsonIgnore
	private String city;

	@JsonIgnore
	private String company;

	@JsonIgnore
	private String costShipping;

	@JsonIgnore
	private String costSubtotal;

	@JsonIgnore
	private String costTax;

	@JsonIgnore
	private String country;

	@JsonIgnore
	private String currencyCode;

	@JsonIgnore
	private String currencyExchangeRate;

	@JsonProperty("customer_message")
	private String customerMessage = "";

	@JsonIgnore
	private String dateCreated;

	@JsonIgnore
	private String dateShipped;

	@JsonIgnore
	private String dateUpdated;

	@JsonIgnore
	private String defaultCurrencyCode;

	@JsonProperty("discount_amount")
	private Double discountAmount = 0.0;

	@JsonIgnore
	private String douponDiscount;

	@JsonIgnore
	private String emailAddress;

	@JsonIgnore
	private String externalSource;

	@JsonIgnore
	private String firstName;

	@JsonIgnore
	private String geoipCountry;

	@JsonIgnore
	private String geoipCountryIso2;

	@JsonIgnore
	private String giftCertificateAmount;

	@JsonProperty("handling_cost_ex_tax")
	private Double handlingCostExTax = 0.0;

	@JsonProperty("handling_cost_inc_tax")
	private Double handlingCostIncTax = 0.0;

	@JsonIgnore
	private String handlingCostTax;

	@JsonIgnore
	private String ipAddress;

	@JsonIgnore
	private String itemsShipped;

	@JsonIgnore
	private String itemsTotal;

	@JsonIgnore
	private String lastName;

	@JsonIgnore
	private String orderId;

	@JsonIgnore
	private String orderIsDigital;

	@JsonIgnore
	private String orderSource;

	@JsonIgnore
	private String paymentMethod;

	@JsonIgnore
	private String paymentStatus;

	@JsonIgnore
	private String phone;

	@JsonProperty("refunded_amount")
	private Double refundedAmount = 0.0;

	@JsonIgnore
	private String saleAmount;

	@JsonProperty("shipping_cost_ex_tax")
	private Double shippingCostExTax = 0.0;

	@JsonProperty("shipping_cost_inc_tax")
	private Double shippingCostIncTax = 0.0;

	@JsonIgnore
	private String shippingCostTax;

	@JsonProperty("staff_notes")
	private String staffNotes = "";

	@JsonIgnore
	private String state;

	@JsonIgnore
	private String status;

	@JsonIgnore
	private String storeCreditAmount;

	@JsonProperty("subtotal_inc_tax")
	private Double subtotalIncTax = 0.0;

	@JsonProperty("subtotal_ex_tax")
	private Double subtotalExTax = 0.0;

	@JsonIgnore
	private String subtotalTax;

	@JsonProperty("total_ex_tax")
	private Double totalExTax = 0.0;

	@JsonProperty("total_inc_tax")
	private Double totalIncTax = 0.0;

	@JsonProperty("wrapping_cost_ex_tax")
	private Double wrappingCostExTax = 0.0;

	@JsonProperty("wrapping_cost_inc_tax")
	private Double wrappingCostIncTax = 0.0;

	@JsonIgnore
	private String wrappingCostTax;

	@JsonIgnore
	private String zip;
	
	@JsonProperty("products")
	private OrderItem[] orderItems;
	
	@JsonProperty("billing_address")
	private Address billingAddress;
	
	@JsonProperty("customer_id")
	private Integer customerId;
	
	@JsonProperty("status_id")
	private Integer statusId = 1;
	
	public String getAddressOne() {
		return addressOne;
	}
	public void setAddressOne(String addressOne) {
		this.addressOne = addressOne;
	}
	public String getAddressTwo() {
		return addressTwo;
	}
	public void setAddressTwo(String addressTwo) {
		this.addressTwo = addressTwo;
	}
	public Double getBaseHandlingCost() {
		return baseHandlingCost;
	}
	public void setBaseHandlingCost(Double baseHandlingCost) {
		this.baseHandlingCost = baseHandlingCost;
	}
	public Double getBaseShippingCost() {
		return baseShippingCost;
	}
	public void setBaseShippingCost(Double baseShippingCost) {
		this.baseShippingCost = baseShippingCost;
	}
	public Double getBaseWrappingCost() {
		return baseWrappingCost;
	}
	public void setBaseWrappingCost(Double baseWrappingCost) {
		this.baseWrappingCost = baseWrappingCost;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getCostShipping() {
		return costShipping;
	}
	public void setCostShipping(String costShipping) {
		this.costShipping = costShipping;
	}
	public String getCostSubtotal() {
		return costSubtotal;
	}
	public void setCostSubtotal(String costSubtotal) {
		this.costSubtotal = costSubtotal;
	}
	public String getCostTax() {
		return costTax;
	}
	public void setCostTax(String costTax) {
		this.costTax = costTax;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}
	public String getCurrencyExchangeRate() {
		return currencyExchangeRate;
	}
	public void setCurrencyExchangeRate(String currencyExchangeRate) {
		this.currencyExchangeRate = currencyExchangeRate;
	}
	public String getCustomerMessage() {
		return customerMessage;
	}
	public void setCustomerMessage(String customerMessage) {
		this.customerMessage = customerMessage;
	}
	public String getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getDateShipped() {
		return dateShipped;
	}
	public void setDateShipped(String dateShipped) {
		this.dateShipped = dateShipped;
	}
	public String getDateUpdated() {
		return dateUpdated;
	}
	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	public String getDefaultCurrencyCode() {
		return defaultCurrencyCode;
	}
	public void setDefaultCurrencyCode(String defaultCurrencyCode) {
		this.defaultCurrencyCode = defaultCurrencyCode;
	}
	public Double getDiscountAmount() {
		return discountAmount;
	}
	public void setDiscountAmount(Double discountAmount) {
		this.discountAmount = discountAmount;
	}
	public String getDouponDiscount() {
		return douponDiscount;
	}
	public void setDouponDiscount(String douponDiscount) {
		this.douponDiscount = douponDiscount;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getExternalSource() {
		return externalSource;
	}
	public void setExternalSource(String externalSource) {
		this.externalSource = externalSource;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getGeoipCountry() {
		return geoipCountry;
	}
	public void setGeoipCountry(String geoipCountry) {
		this.geoipCountry = geoipCountry;
	}
	public String getGeoipCountryIso2() {
		return geoipCountryIso2;
	}
	public void setGeoipCountryIso2(String geoipCountryIso2) {
		this.geoipCountryIso2 = geoipCountryIso2;
	}
	public String getGiftCertificateAmount() {
		return giftCertificateAmount;
	}
	public void setGiftCertificateAmount(String giftCertificateAmount) {
		this.giftCertificateAmount = giftCertificateAmount;
	}
	public Double getHandlingCostExTax() {
		return handlingCostExTax;
	}
	public void setHandlingCostExTax(Double handlingCostExTax) {
		this.handlingCostExTax = handlingCostExTax;
	}
	public Double getHandlingCostIncTax() {
		return handlingCostIncTax;
	}
	public void setHandlingCostIncTax(Double handlingCostIncTax) {
		this.handlingCostIncTax = handlingCostIncTax;
	}
	public String getHandlingCostTax() {
		return handlingCostTax;
	}
	public void setHandlingCostTax(String handlingCostTax) {
		this.handlingCostTax = handlingCostTax;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getItemsShipped() {
		return itemsShipped;
	}
	public void setItemsShipped(String itemsShipped) {
		this.itemsShipped = itemsShipped;
	}
	public String getItemsTotal() {
		return itemsTotal;
	}
	public void setItemsTotal(String itemsTotal) {
		this.itemsTotal = itemsTotal;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getOrderIsDigital() {
		return orderIsDigital;
	}
	public void setOrderIsDigital(String orderIsDigital) {
		this.orderIsDigital = orderIsDigital;
	}
	public String getOrderSource() {
		return orderSource;
	}
	public void setOrderSource(String orderSource) {
		this.orderSource = orderSource;
	}
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
	public String getPaymentStatus() {
		return paymentStatus;
	}
	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public Double getRefundedAmount() {
		return refundedAmount;
	}
	public void setRefundedAmount(Double refundedAmount) {
		this.refundedAmount = refundedAmount;
	}
	public String getSaleAmount() {
		return saleAmount;
	}
	public void setSaleAmount(String saleAmount) {
		this.saleAmount = saleAmount;
	}
	public Double getShippingCostExTax() {
		return shippingCostExTax;
	}
	public void setShippingCostExTax(Double shippingCostExTax) {
		this.shippingCostExTax = shippingCostExTax;
	}
	public String getShippingCostTax() {
		return shippingCostTax;
	}
	public void setShippingCostTax(String shippingCostTax) {
		this.shippingCostTax = shippingCostTax;
	}
	public String getStaffNotes() {
		return staffNotes;
	}
	public void setStaffNotes(String staffNotes) {
		this.staffNotes = staffNotes;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStoreCreditAmount() {
		return storeCreditAmount;
	}
	public void setStoreCreditAmount(String storeCreditAmount) {
		this.storeCreditAmount = storeCreditAmount;
	}
	public Double getSubtotalIncTax() {
		return subtotalIncTax;
	}
	public void setSubtotalIncTax(Double subtotalIncTax) {
		this.subtotalIncTax = subtotalIncTax;
	}
	public String getSubtotalTax() {
		return subtotalTax;
	}
	public void setSubtotalTax(String subtotalTax) {
		this.subtotalTax = subtotalTax;
	}
	public Double getTotalExTax() {
		return totalExTax;
	}
	public void setTotalExTax(Double totalExTax) {
		this.totalExTax = totalExTax;
	}
	public Double getWrappingCostExTax() {
		return wrappingCostExTax;
	}
	public void setWrappingCostExTax(Double wrappingCostExTax) {
		this.wrappingCostExTax = wrappingCostExTax;
	}
	public Double getWrappingCostIncTax() {
		return wrappingCostIncTax;
	}
	public void setWrappingCostIncTax(Double wrappingCostIncTax) {
		this.wrappingCostIncTax = wrappingCostIncTax;
	}
	public String getWrappingCostTax() {
		return wrappingCostTax;
	}
	public void setWrappingCostTax(String wrappingCostTax) {
		this.wrappingCostTax = wrappingCostTax;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	public Address getBillingAddress() {
		return billingAddress;
	}
	public void setBillingAddress(Address billingAddress) {
		this.billingAddress = billingAddress;
	}
	public OrderItem[] getOrderItems() {
		return orderItems;
	}
	public void setOrderItems(OrderItem[] orderItems) {
		this.orderItems = orderItems;
	}
	public Integer getCustomerId() {
		return customerId;
	}
	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
	}
	public Integer getStatusId() {
		return statusId;
	}
	public void setStatusId(Integer statusId) {
		this.statusId = statusId;
	}
	public Double getShippingCostIncTax() {
		return shippingCostIncTax;
	}
	public void setShippingCostIncTax(Double shippingCostIncTax) {
		this.shippingCostIncTax = shippingCostIncTax;
	}
	public Double getSubtotalExTax() {
		return subtotalExTax;
	}
	public void setSubtotalExTax(Double subtotalExTax) {
		this.subtotalExTax = subtotalExTax;
	}
	public Double getTotalIncTax() {
		return totalIncTax;
	}
	public void setTotalIncTax(Double totalIncTax) {
		this.totalIncTax = totalIncTax;
	}
	
}
