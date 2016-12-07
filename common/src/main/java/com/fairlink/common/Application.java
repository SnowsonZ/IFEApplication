package com.fairlink.common;

import java.util.Date;

public class Application {
	private Integer id;

	private String version = "";

	private String name = "";

	private String developer = "";
	
	private String componentName = "";

    private String type = "";   // 一级目录

    private String category = "";    // 二级目录

	private String description = "";

	private String path = "";

	private String origin = "";

	private Integer isUsing;

	private Date addTime;

	public Application() {
		
	}
	
	public Application(String name, String developer, String componentName, String type,
			String category, String origin, String version) {
		super();
		this.name = name;
		this.developer = developer;
		this.type = type;
		this.category = category;
		this.origin = origin;
		this.version = version;
		this.componentName = componentName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version == null ? "" : version.trim();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name == null ? "" : name.trim();
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer == null ? "" : developer.trim();
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category == null ? "" : category.trim();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type == null ? "" : type.trim();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description == null ? "" : description.trim();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path == null ? "" : path.trim();
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin == null ? "" : origin.trim();
	}

	public void setIsUsing(Integer isUsing) {
		this.isUsing = isUsing;
	}

	public Integer getIsUsing() {
		return isUsing;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName == null ? "" : componentName.trim();
	}
}
