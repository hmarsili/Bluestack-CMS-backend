package com.tfsla.webusersposts.strings;

public class SqlQueries {
	
	public static final String INSERT_POST = "insert into TFS_USER_POSTS " +
			"( FILE_CONTENT, USER_ID, TITLE, DATE_CREATED, DATE_UPDATED, PUBLICATION, SITE, SOCIAL_NETWORKS, RESOURCE_ID, STATUS, URL )" +
			" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public static final String UPDATE_POST = "update TFS_USER_POSTS " +
			"set FILE_CONTENT = ?, USER_ID = ?, TITLE = ?, DATE_UPDATED = ?, PUBLICATION = ?, SITE = ?, SOCIAL_NETWORKS = ?, STATUS = ? " +
			" where RESOURCE_ID = ?";
	
	public static final String UPDATE_POST_STATUS = "update TFS_USER_POSTS " +
			"set STATUS = ?, DATE_UPDATED = ?, URL = ?, INFO = ? where RESOURCE_ID = ?";
	
	public static final String UPDATE_PENDING_USER_POSTS_STATUS = "update TFS_USER_POSTS " +
			"set STATUS = ? where STATUS = ? and USER_ID = ?";
	
	public static final String GET_USER_POSTS = "select * from TFS_USER_POSTS " +
			"where USER_ID = ? order by DATE_CREATED desc";
	
	public static final String GET_USER_POSTS_FILTERED = "select * from TFS_USER_POSTS " +
			"where USER_ID = ? %s order by DATE_CREATED desc limit ?,?";
	
	public static final String GET_POST = "select * from TFS_USER_POSTS " + 
			"where RESOURCE_ID = ?";
	
	public static final String GET_POST_BY_URL_AND_PUBLICATION = "select * from TFS_USER_POSTS " + 
			"where URL = ? and PUBLICATION = ?";
	
	public static final String GET_POSTS = "select * from TFS_USER_POSTS " + 
			"where STATUS = ? and PUBLICATION = ? and SITE = ? limit ?";
	
	public static final String CLEAN_OLD_DRAFTS = "delete from TFS_USER_POSTS " + 
			"where STATUS = 0 and DATE_CREATED < ?";
	
	public static final String DELETE_DRAFT = "delete from TFS_USER_POSTS " +
			"where RESOURCE_ID = ?";
	
	public static final String AND_FILTER = " and ";
	
	public static final String AND_FILTER_FORMAT = AND_FILTER + "%s %s ? ";
	
	public static final String IN_OP = " in ";
	
	public static final String EQ_OP = " = ";
	
	public static final String LIKE_OP = " like ";
}
