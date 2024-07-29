package com.tfsla.diario.webservices.common.strings;

public class SqlQueries {
	
	public static final String GET_TOPIC_CONFIGURATION = "select NAME, DESCRIPTION, SITE, PUBLICATION, URLPARAMS, `INTERVAL`, FROMHOUR, FROMMINUTES, TOHOUR, TOMINUTES, PRIORITY from TFS_PUSH_TOPICS WHERE NAME=?";
	
	public static final String GET_TOPIC_CONFIGURATION_BY_LOCATION = "select NAME, DESCRIPTION, SITE, PUBLICATION, URLPARAMS, `INTERVAL`, FROMHOUR, FROMMINUTES, TOHOUR, TOMINUTES, PRIORITY from TFS_PUSH_TOPICS WHERE SITE=? AND PUBLICATION=? ORDER BY PRIORITY ASC";
	
	public static final String GET_USER_NAME_FROM_EMAIL = "select USER_NAME from CMS_USERS where USER_EMAIL = ? or USER_NAME = ?";
	
	public static final String GET_USER_ID_FROM_INFO = "select USER_ID from CMS_USERDATA where DATA_KEY = ? and DATA_VALUE = ?";
	
	public static final String GET_NICK_MAX_ID = "select max(cast(substring(CMS_USERDATA.DATA_VALUE, instr(CMS_USERDATA.DATA_VALUE, '_') + 1) as unsigned)) as MAX_ID from CMS_USERS inner join CMS_USERDATA on CMS_USERS.USER_ID = CMS_USERDATA.USER_ID where CMS_USERS.USER_NAME <> ? and CMS_USERDATA.DATA_KEY = 'APODO' and CMS_USERDATA.DATA_VALUE like ?";
	
	public static final String GET_USER_BY_CLIENT_ID_AND_PLATFORM = "select REGISTER_ID from TFS_PUSH_CLIENTS where CLIENT_ID = ? and PLATFORM = ?  and TOPIC_ARN = ? and SITE = ? and PUBLICATION = ?";
	
	public static final String INSERT_PUSH_ENDPOINT = "insert into TFS_PUSH_CLIENTS (CLIENT_ID, PLATFORM, CREATION_DATE, APP_ARN, TOPIC_ARN, SITE, PUBLICATION) values (?, ?, ?, ?, ?, ?, ?)";
	
	public static final String UPDATE_PUSH_ENDPOINT = "update TFS_PUSH_CLIENTS SET APP_ARN = ?, TOPIC_ARN = ? where CLIENT_ID = ? AND SITE = ? AND PUBLICATION = ? AND PLATFORM = ?";
	
	public static final String INSERT_PUSH_CLIENT = "insert into TFS_PUSH_CLIENTS (CLIENT_ID, PLATFORM, CREATION_DATE, CLIENT_INFO, USER_ID, SITE, PUBLICATION) values (?, ?, ?, ?, (select USER_ID from CMS_USERS where USER_EMAIL = ? limit 1), ?, ?)";
	
	public static final String REMOVE_TOPIC_PUSH_CLIENT = "delete from TFS_PUSH_CLIENTS where CLIENT_ID = ? and PLATFORM = ? and TOPIC_ARN = ?";
	public static final String REMOVE_PUSH_CLIENT = "delete from TFS_PUSH_CLIENTS where CLIENT_ID = ? and PLATFORM = ?";
	
	public static final String REMOVE_ENDPOINT = "delete from TFS_PUSH_CLIENTS where APP_ARN = ?";

	public static final String GET_TOPICS_FROM_CLIENTS_AND_PLATFORM = "select * from TFS_PUSH_CLIENTS where CLIENT_ID=? and PLATFORM = ? and SITE = ?";
	public static final String GET_TOPICS_FROM_CLIENTS_AND_PLATFORM_AND_PUBLICATION = "select * from TFS_PUSH_CLIENTS where CLIENT_ID=? and PLATFORM = ? and SITE = ? and PUBLICATION = ?";
	
	
	public static final String GET_CLIENTS_BY_PLATFORM_AND_PUBLICATION = "select * from TFS_PUSH_CLIENTS where PLATFORM = ? and SITE = ? and PUBLICATION = ?";
	
	public static final String GET_CLIENTS_BY_PLATFORM = "select * from TFS_PUSH_CLIENTS where PLATFORM = ? and SITE = ?";
	
	public static final String GET_CLIENTS_COUNT_BY_SITE = "select count(*) as PUSH_CLIENTS from TFS_PUSH_CLIENTS where SITE = ?";
	
	public static final String GET_CLIENTS_COUNT_BY_SITE_AND_PUBLICATION = "select count(*) as PUSH_CLIENTS from TFS_PUSH_CLIENTS where SITE = ? and PUBLICATION = ?";

	public static final String GET_CLIENTS_COUNT_BY_SITE_AND_DATE = "select count(*) as PUSH_CLIENTS from TFS_PUSH_CLIENTS where SITE = ? and CREATION_DATE between ? and ?";
	
	public static final String GET_CLIENTS_COUNT_BY_SITE_AND_PUBLICATION_AND_DATE = "select count(*) as PUSH_CLIENTS from TFS_PUSH_CLIENTS where SITE = ? and PUBLICATION = ?  and CREATION_DATE between ? and ?";

	public static final String GET_CLIENTS_COUNT_BY_TOPIC_AND_SITE_AND_DATE = "select count(*) as PUSH_CLIENTS from TFS_PUSH_CLIENTS where SITE = ? and TOPIC_ARN = ? and CREATION_DATE between ? and ?";
	
	public static final String GET_CLIENTS_COUNT_BY_TOPIC_AND_SITE_AND_PUBLICATION_AND_DATE = "select count(*) as PUSH_CLIENTS from TFS_PUSH_CLIENTS where SITE = ? and PUBLICATION = ? and TOPIC_ARN = ? and CREATION_DATE between ? and ?";

	
	public static final String GET_ALL_NEWS_FROM_PUSH_TABLE_BY_PUBLICATION = "select * from TFS_PUSH_MOBILE where SITE = ? and STATUS = ? and PUBLICATION = ? order by PRIORITY desc, DATE_CREATED asc";
	
	
	
	public static final String GET_ALL_NEWS_FROM_PUSH_TABLE = "select * from TFS_PUSH_MOBILE where SITE = ? and STATUS = ? order by PRIORITY desc, DATE_CREATED asc";
	
	public static final String GET_PUSHED_NEWS_FROM_PUSH_TABLE_BY_PUBLICATION = "select * from TFS_PUSH_MOBILE where SITE = ? and PUBLICATION = ? and STATUS <> 0 order by DATE_PUSHED desc limit 10";
	
	public static final String GET_PUSHED_NEWS_FROM_PUSH_TABLE = "select * from TFS_PUSH_MOBILE where SITE = ? and STATUS <> 0 order by DATE_PUSHED desc limit 10";

	@Deprecated
	public static final String GET_NEWS_FROM_PUSH_TABLE = "select * from TFS_PUSH_MOBILE where SITE = ? and PUBLICATION = ? and STATUS = ? and PUSH_MODE = ? order by PRIORITY desc, DATE_CREATED asc";
	
	@Deprecated
	public static final String GET_NEWS_FROM_PUSH_TABLE_LIMITED = "select * from TFS_PUSH_MOBILE where SITE = ? and PUBLICATION = ? and STATUS = ? and PUSH_MODE = ? order by PRIORITY desc, DATE_CREATED asc limit ?";

	public static final String GET_NEWS_FROM_TOPIC_BY_ID = "select * from TFS_PUSH_MOBILE where ID = ?";

	public static final String GET_NEWS_FROM_TOPIC = "select * from TFS_PUSH_MOBILE where TOPIC = ? and STATUS = ? and PUSH_MODE = ? order by PRIORITY desc, DATE_CREATED asc";	
	public static final String GET_NEWS_FROM_TOPIC_LIMITED = "select * from TFS_PUSH_MOBILE where TOPIC = ? and STATUS = ? and PUSH_MODE = ? order by PRIORITY desc, DATE_CREATED asc limit ?";

	
	public static final String INSERT_PUSHED_NEW = "insert into TFS_PUSH_MOBILE (STRUCTURE_ID, SITE, PUBLICATION, PUSH_MODE, DATE_CREATED, USER_NAME, TITLE, PRIORITY) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
	public static final String INSERT_PUSHED_NEW_WITH_SCHEDULE = "insert into TFS_PUSH_MOBILE (STRUCTURE_ID, SITE, PUBLICATION, PUSH_MODE, DATE_CREATED, USER_NAME, TITLE, PUSH_SCHEDULE, JOB_NAME, PRIORITY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";

	public static final String INSERT_IN_TOPIC_PUSHED_NEW = "insert into TFS_PUSH_MOBILE (STRUCTURE_ID, TOPIC, SITE, PUBLICATION, PUSH_MODE, DATE_CREATED, USER_NAME, TITLE, SUBTITLE, URL, IMAGE, PRIORITY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";
	public static final String INSERT_IN_TOPIC_PUSHED_NEW_WITH_SCHEDULE = "insert into TFS_PUSH_MOBILE (STRUCTURE_ID, TOPIC, SITE, PUBLICATION, PUSH_MODE, DATE_CREATED, USER_NAME, TITLE, SUBTITLE, URL, IMAGE, PUSH_SCHEDULE, JOB_NAME, PRIORITY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)";

	public static final String COPY_PUSHED_NEW = "insert into TFS_PUSH_MOBILE (STRUCTURE_ID, SITE, PUBLICATION, PUSH_MODE, DATE_CREATED, USER_NAME, PRIORITY) select STRUCTURE_ID, SITE, PUBLICATION, 'en_cola', NOW(), ?, 0 from TFS_PUSH_MOBILE where REGISTER_ID = ?";
	
	public static final String SET_PUSHED_NOTIFICATION = "update TFS_PUSH_MOBILE set STATUS = ?, DATE_PUSHED = ?, INFO = ? where REGISTER_ID = ?";
	
	public static final String SET_PUSHED_NEW = "update TFS_PUSH_MOBILE set STATUS = ?, DATE_PUSHED = ?, INFO = ? where STRUCTURE_ID = ?";
	
	public static final String GET_PUSH = "select * from TFS_PUSH_MOBILE where REGISTER_ID = ?";
	
	public static final String DELETE_PUSH = "delete from TFS_PUSH_MOBILE where REGISTER_ID = ?";
	
	public static final String GET_NEW_ID = "select REGISTER_ID from TFS_PUSH_MOBILE where STRUCTURE_ID = ? and SITE = ? and PUBLICATION = ?";
	
	public static final String GET_PUSH_ID = "select REGISTER_ID from TFS_PUSH_MOBILE where STRUCTURE_ID = ?";
	
	public static final String UPDATE_PUSH_PRIORITY = "update TFS_PUSH_MOBILE set PRIORITY = ? where REGISTER_ID = ?";

	public static final String GET_ALL_NEWS_FROM_PUSH_TABLE_BY_TOPIC = "select * from TFS_PUSH_MOBILE where TOPIC = ? and STATUS = ? order by PRIORITY desc, DATE_CREATED asc";
}
