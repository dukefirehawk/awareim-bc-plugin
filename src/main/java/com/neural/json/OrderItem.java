package com.neural.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8680930150564844659L;

	@JsonIgnore
	private String categories;

	@JsonIgnore
	private String dateCreated;

	@JsonIgnore
	private String dateUpdated;

	@JsonIgnore
	private String description;

	@JsonIgnore
	private String hTMLDescription;

	@JsonProperty("product_id")
	private String productId;
	
	@JsonIgnore
	private String itemId;

	@JsonIgnore
	private String name;

	@JsonIgnore
	private String priceExTax;

	@JsonIgnore
	private String priceIncTax;

	private Double quantity;

	@JsonIgnore
	private String sku;

	@JsonIgnore
	private String upc;

	@JsonIgnore
	private String weight;

	@JsonIgnore
	private String orderId;
	
	@JsonProperty("product_options")
	private ProductOption[] productOptions = new ProductOption[0];

	public String getCategories() {
		return categories;
	}
	public void setCategories(String categories) {
		this.categories = categories;
	}
	public String getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getDateUpdated() {
		return dateUpdated;
	}
	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@JsonIgnore
	public String getHTMLDescription() {
		return hTMLDescription;
	}
	public void setHTMLDescription(String hTMLDescription) {
		this.hTMLDescription = hTMLDescription;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPriceExTax() {
		return priceExTax;
	}
	public void setPriceExTax(String priceExTax) {
		this.priceExTax = priceExTax;
	}
	public String getPriceIncTax() {
		return priceIncTax;
	}
	public void setPriceIncTax(String priceIncTax) {
		this.priceIncTax = priceIncTax;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public Double getQuantity() {
		return quantity;
	}
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public String getUpc() {
		return upc;
	}
	public void setUpc(String upc) {
		this.upc = upc;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
		
	}
	public ProductOption[] getProductOptions() {
		return productOptions;
	}
	public void setProductOptions(ProductOption[] productOptions) {
		this.productOptions = productOptions;
	}

}
