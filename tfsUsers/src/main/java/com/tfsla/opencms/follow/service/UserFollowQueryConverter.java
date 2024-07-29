package com.tfsla.opencms.follow.service;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;

public class UserFollowQueryConverter {
	
	public String getQuery(CmsObject cms, String user){
		
		String query = "";
		List<String> usersQuery = new ArrayList<String>();
		List<CmsUser> usersFollowing = UserFollowService.getInstance(cms).getFollowingUsers(cms, user);
		
		for(CmsUser uFollow : usersFollowing){
			usersQuery.add(uFollow.getName());
		}			
		
		query = CreateQueryByListOfUsers(usersQuery);
				
		return query;
	}
	
	public void getUsersFollowing(CmsObject cms, String user, JSONArray jsonItems) {
		
		List<CmsUser> usersFollowing = UserFollowService.getInstance(cms).getFollowingUsers(cms, user);
		
		for (CmsUser uFollow : usersFollowing) {		
			
			JSONObject jsonitem = new JSONObject();
				
			jsonitem.put("key", uFollow.getId().getStringValue());
			jsonitem.put("value", uFollow.getEmail());
			jsonItems.add(jsonitem);			
		}			
	} 

	private String CreateQueryByListOfUsers(List<String> usersQuery) {
		String query = "( internalUser :( ";
		int i = 0;
		
		for (String user : usersQuery)
		{
			i++;
			query += user;
			
			if(hasMoreItems(i, usersQuery.size())){
				query += " OR ";
			}
		}
		
		query += " ) )";		
		return query;
	}
	
	private boolean hasMoreItems(int index, int size) {
		if(index != size){
			return true;
		}
		return false;
	}
}
