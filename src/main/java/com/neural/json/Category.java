package com.neural.json;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Category implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2378366356878564855L;

	@JsonIgnore
	private String categoryId;

	@JsonIgnore
	private String parentId;

	@JsonIgnore
	private String name;

	@JsonIgnore
	private String description;

	@JsonIgnore
	private String sortOrder;

	@JsonIgnore
	private String pageTitle;

	@JsonIgnore
	private String metaKeywords;

	@JsonIgnore
	private String metaDescription;

	@JsonIgnore
	private String layoutFile;

	@JsonIgnore
	private String imageFile;

	@JsonIgnore
	private String isVisible;

	@JsonIgnore
	private String searchKeywords;

	@JsonIgnore
	private String url;
	
	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}


	public String getMetaKeywords() {
		return metaKeywords;
	}

	public void setMetaKeywords(String metaKeywords) {
		this.metaKeywords = metaKeywords;
	}

	public String getMetaDescription() {
		return metaDescription;
	}

	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}

	public String getLayoutFile() {
		return layoutFile;
	}

	public void setLayoutFile(String layoutFile) {
		this.layoutFile = layoutFile;
	}

	public String getImageFile() {
		return imageFile;
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}

	public String getIsVisible() {
		return isVisible;
	}

	public void setIsVisible(String isVisible) {
		this.isVisible = isVisible;
	}

	public String getSearchKeywords() {
		return searchKeywords;
	}

	public void setSearchKeywords(String searchKeywords) {
		this.searchKeywords = searchKeywords;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
