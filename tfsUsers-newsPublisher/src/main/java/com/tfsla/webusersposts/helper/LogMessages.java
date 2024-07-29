package com.tfsla.webusersposts.helper;

public class LogMessages {
	
	public static final String JOB_STARTED = "UserPostsPublisherJob - job started, site: %s - publication: %s";
	public static final String JOB_INFO = "UserPostsPublisherJob - %s posts to process, folder %s";
	public static final String JOB_EXECUTED = "UserPostsPublisherJob - Publicacion de posts ejecutado en %s, sin posts para procesar";
	public static final String JOB_SUMMARY = "UserPostsPublisherJob - job finished: %s posts approved - %s posts to moderate";
	public static final String JOB_FINISHED = "UserPostsPublisherJob - Publicacion de posts ejecutado en %s, %s posts procesados - %s aprobados - %s para moderar";
	public static final String JOB_ERROR = "UserPostsPublisherJob - error during the process: ";
	public static final String JOB_ERROR_CLEAN_HISTORY = "UserPostsPublisherJob - error cleaning post abuse reports";
	public static final String JOB_CLEAN_HISTORY = "UserPostsPublisherJob - cleaning abuse reports for resource %s, site %s, publication %s";
	public static final String JOB_EXECUTION_ERROR = "Publicacion de posts ejecutado en %s, error en la ejecucion: %s";
	public static final String POST_TO_PUBLISH = "UserPostsPublisherJob - post %s to be published: %s";
	public static final String POST_REJECTED = "UserPostsPublisherJob - post %s to moderate";
	public static final String POST_ERROR = "UserPostsPublisherJob - error processing post %s: ";
	public static final String ANONYMOUS_POSTS_NOT_ALLOWED = "Anonymous posts not allowed for this site";
	
}
