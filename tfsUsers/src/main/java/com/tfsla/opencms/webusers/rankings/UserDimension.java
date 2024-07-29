package com.tfsla.opencms.webusers.rankings;

import java.util.Arrays;
import java.util.List;

public abstract class UserDimension implements Comparable<UserDimension> {

	public UserDimension() {
		this.setTable("CMS_USERDATA");
	}
	
	public String getType() {
		if (type != null)
			return type.toLowerCase();
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
		if (description == null || description.equals("")) {
			this.description = this.name;
		} else {
			this.description = description;
		}
	}

	public String getEntryName() {
		return entryName;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
		if(entryName != null && UserDimension.CMS_USER_FIELDS.contains(entryName)) {
			this.setTable("CMS_USERS");
		}
	}
	
	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public Boolean isList() {
		if (this.getType() != null && !this.getType().equals("")) {
			return this.getType().toLowerCase().equals("list");
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof UserDimension))
			return false;
		UserDimension field = (UserDimension) obj;

		return field.getName().equals(this.getName());
	}
	
	@Override
	public int compareTo(UserDimension arg0) {
		return this.getDescription().compareTo(arg0.getDescription());
	}

	private String type;
	private String name;
	private String description;
	private String entryName;
	private String table;
	
	private static final List<String> CMS_USER_FIELDS = Arrays.asList(
		"USER_NAME", 
		"USER_EMAIL", 
		"USER_LASTNAME",
		"USER_FIRSTNAME"
	);

}
