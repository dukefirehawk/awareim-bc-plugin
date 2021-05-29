package com.neural.json;

import java.io.Serializable;

public class Sku implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7496232751054838891L;

	private String id;
    private String productId;
    private String sku;
    private String costPrice;
    private String upc;
    private String inventoryLevel;
    private String inventoryWarningLevel;
    private String binPickingNumber;
    private String description;
    private SkuOption[] options = new SkuOption[0];
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public String getCostPrice() {
		return costPrice;
	}
	public void setCostPrice(String costPrice) {
		this.costPrice = costPrice;
	}
	public String getUpc() {
		return upc;
	}
	public void setUpc(String upc) {
		this.upc = upc;
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
	public String getBinPickingNumber() {
		return binPickingNumber;
	}
	public void setBinPickingNumber(String binPickingNumber) {
		this.binPickingNumber = binPickingNumber;
	}
	public SkuOption[] getOptions() {
		return options;
	}
	public void setOptions(SkuOption[] options) {
		this.options = options;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
