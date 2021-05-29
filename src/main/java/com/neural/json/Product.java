package com.neural.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Product implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 610794780203834697L;
	
	@JsonIgnore
	private String availability;

	@JsonIgnore
	private String availabilityDescription;

	@JsonIgnore
	private String binPickingNumber;

	@JsonIgnore
	private String category;

	@JsonIgnore
	private String costPrice;

	@JsonIgnore
	private String dateCreated;

	@JsonIgnore
	private String dateLastImported;

	@JsonIgnore
	private String dateUpdated;

	@JsonIgnore
	private String depth;

	@JsonIgnore
	private String description;

	@JsonIgnore
	private String fixedCostShippingPrice;

	@JsonIgnore
	private String height;

	@JsonIgnore
	private String inventoryLevel;

	@JsonIgnore
	private String inventoryWarningLevel;

	@JsonIgnore
	private String inventoryTracking;

	@JsonIgnore
	private String isFeatured;

	@JsonIgnore
	private String isFreeShipping;

	@JsonIgnore
	private String isPreorderOnly;
	
	@JsonIgnore
	private String isVisible;

	@JsonIgnore
	private String metaDescription;

	@JsonIgnore
	private String metaKeywords;

	@JsonIgnore
	private String name;

	@JsonIgnore
	private String numberSold;

	@JsonIgnore
	private String orderQuantityMaximum;

	@JsonIgnore
	private String orderQuantityMinimum;

	@JsonIgnore
	private String preOrderReleaseDate;

	@JsonIgnore
	private String preorderMessage;

	@JsonIgnore
	private String price;

	@JsonProperty("id")
	private String productID;

	@JsonIgnore
	private String ratingCount;

	@JsonIgnore
	private String ratingTotal;

	@JsonIgnore
	private String relatedProducts;

	@JsonIgnore
	private String retailPrice;

	@JsonIgnore
	private String salePrice;

	@JsonIgnore
	private String searchKeywords;

	@JsonIgnore
	private String sKU;

	@JsonIgnore
	private String sortOrder;

	@JsonIgnore
	private String totalSold;

	@JsonIgnore
	private String type;

	@JsonIgnore
	private String uPC;

	@JsonIgnore
	private String viewCount;

	@JsonIgnore
	private String warranty;

	@JsonIgnore
	private String weight;

	@JsonIgnore
	private String width;
	
	@JsonIgnore
	private String optionSetId;	

	public String getAvailability() {
		return availability;
	}
	public void setAvailability(String availability) {
		this.availability = availability;
	}
	public String getAvailabilityDescription() {
		return availabilityDescription;
	}
	public void setAvailabilityDescription(String availabilityDescription) {
		this.availabilityDescription = availabilityDescription;
	}
	public String getBinPickingNumber() {
		return binPickingNumber;
	}
	public void setBinPickingNumber(String binPickingNumber) {
		this.binPickingNumber = binPickingNumber;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getCostPrice() {
		return costPrice;
	}
	public void setCostPrice(String costPrice) {
		this.costPrice = costPrice;
	}
	public String getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	public String getDateLastImported() {
		return dateLastImported;
	}
	public void setDateLastImported(String dateLastImported) {
		this.dateLastImported = dateLastImported;
	}
	public String getDateUpdated() {
		return dateUpdated;
	}
	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	public String getDepth() {
		return depth;
	}
	public void setDepth(String depth) {
		this.depth = depth;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getFixedCostShippingPrice() {
		return fixedCostShippingPrice;
	}
	public void setFixedCostShippingPrice(String fixedCostShippingPrice) {
		this.fixedCostShippingPrice = fixedCostShippingPrice;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getInventoryLevel() {
		return inventoryLevel;
	}
	public void setInventoryLevel(String inventoryLevel) {
		this.inventoryLevel = inventoryLevel;
	}
	public String getInventoryWarningLevel() {
		return inventoryWarningLevel;
	}
	public void setInventoryWarningLevel(String inventoryWarningLevel) {
		this.inventoryWarningLevel = inventoryWarningLevel;
	}
	public String getInventoryTracking() {
		return inventoryTracking;
	}
	public void setInventoryTracking(String inventoryTracking) {
		this.inventoryTracking = inventoryTracking;
	}
	public String getIsFeatured() {
		return isFeatured;
	}
	public void setIsFeatured(String isFeatured) {
		this.isFeatured = isFeatured;
	}
	public String getIsFreeShipping() {
		return isFreeShipping;
	}
	public void setIsFreeShipping(String isFreeShipping) {
		this.isFreeShipping = isFreeShipping;
	}
	public String getIsPreorderOnly() {
		return isPreorderOnly;
	}
	public void setIsPreorderOnly(String isPreorderOnly) {
		this.isPreorderOnly = isPreorderOnly;
	}
	public String getIsVisible() {
		return isVisible;
	}
	public void setIsVisible(String isVisible) {
		this.isVisible = isVisible;
	}
	public String getMetaDescription() {
		return metaDescription;
	}
	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}
	public String getMetaKeywords() {
		return metaKeywords;
	}
	public void setMetaKeywords(String metaKeywords) {
		this.metaKeywords = metaKeywords;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNumberSold() {
		return numberSold;
	}
	public void setNumberSold(String numberSold) {
		this.numberSold = numberSold;
	}
	public String getOrderQuantityMaximum() {
		return orderQuantityMaximum;
	}
	public void setOrderQuantityMaximum(String orderQuantityMaximum) {
		this.orderQuantityMaximum = orderQuantityMaximum;
	}
	public String getOrderQuantityMinimum() {
		return orderQuantityMinimum;
	}
	public void setOrderQuantityMinimum(String orderQuantityMinimum) {
		this.orderQuantityMinimum = orderQuantityMinimum;
	}
	public String getPreOrderReleaseDate() {
		return preOrderReleaseDate;
	}
	public void setPreOrderReleaseDate(String preOrderReleaseDate) {
		this.preOrderReleaseDate = preOrderReleaseDate;
	}
	public String getPreorderMessage() {
		return preorderMessage;
	}
	public void setPreorderMessage(String preorderMessage) {
		this.preorderMessage = preorderMessage;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getProductID() {
		return productID;
	}
	public void setProductID(String productID) {
		this.productID = productID;
	}
	public String getRatingCount() {
		return ratingCount;
	}
	public void setRatingCount(String ratingCount) {
		this.ratingCount = ratingCount;
	}
	public String getRatingTotal() {
		return ratingTotal;
	}
	public void setRatingTotal(String ratingTotal) {
		this.ratingTotal = ratingTotal;
	}
	public String getRelatedProducts() {
		return relatedProducts;
	}
	public void setRelatedProducts(String relatedProducts) {
		this.relatedProducts = relatedProducts;
	}
	public String getRetailPrice() {
		return retailPrice;
	}
	public void setRetailPrice(String retailPrice) {
		this.retailPrice = retailPrice;
	}
	public String getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(String salePrice) {
		this.salePrice = salePrice;
	}
	public String getSearchKeywords() {
		return searchKeywords;
	}
	public void setSearchKeywords(String searchKeywords) {
		this.searchKeywords = searchKeywords;
	}
	public String getSKU() {
		return sKU;
	}
	public void setSKU(String sKU) {
		this.sKU = sKU;
	}
	public String getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
	public String getTotalSold() {
		return totalSold;
	}
	public void setTotalSold(String totalSold) {
		this.totalSold = totalSold;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUPC() {
		return uPC;
	}
	public void setUPC(String uPC) {
		this.uPC = uPC;
	}
	public String getViewCount() {
		return viewCount;
	}
	public void setViewCount(String viewCount) {
		this.viewCount = viewCount;
	}
	public String getWarranty() {
		return warranty;
	}
	public void setWarranty(String warranty) {
		this.warranty = warranty;
	}
	public String getWeight() {
		return weight;
	}
	public void setWeight(String weight) {
		this.weight = weight;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getOptionSetId() {
		return optionSetId;
	}
	public void setOptionSetId(String optionSetId) {
		this.optionSetId = optionSetId;
	}

}
