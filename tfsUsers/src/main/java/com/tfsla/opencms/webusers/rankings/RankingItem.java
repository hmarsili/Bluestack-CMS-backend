package com.tfsla.opencms.webusers.rankings;

import java.util.ArrayList;

public class RankingItem {
	
	private String name;
	private Integer count;
	private ArrayList<RankingItem> items;
	
	public RankingItem() {
		this.items = new ArrayList<RankingItem>();
		this.count = 0;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public ArrayList<RankingItem> getItems() {
		return items;
	}
	public void setItems(ArrayList<RankingItem> items) {
		this.items = items;
	}
	public RankingItem getItem(String itemName) {
		for(RankingItem item : this.getItems()) {
			if(item.getName().equals(itemName))
				return item;
		}
		return null;
	}
	
}
