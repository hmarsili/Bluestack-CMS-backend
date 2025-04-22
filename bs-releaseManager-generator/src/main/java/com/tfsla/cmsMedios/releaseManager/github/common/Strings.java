package com.tfsla.cmsMedios.releaseManager.github.common;

/**
 * Contains static strings used for logging and keeping processes noticed
 */
public class Strings {
	public static final String RETRIEVING_FILE = "Retrieving file from %s";
	public static final String FILE_NAME = "File name: %s";
	public static final String FILE_CREATED = "Created temp file on %s";
	
	public static final String RETRIEVING_TAGS = "Retrieving tags";
	public static final String RETRIEVING_TAG_JARS = "Retrieving jars from tag %s";
	public static final String RETRIEVING_TAG_SCRIPTS = "Retrieving SQL scripts from tag %s";
	public static final String RETRIEVING_TAG_CONFIG_FILES = "Retrieving config. files from tag %s";
	public static final String RETRIEVING_FILES_BETWEEN_TAGS = "Retrieving files between tags %s and %s";
	public static final String RETRIEVING_TAG_ATTACHED_FILES = "Retrieving attached files from tag %s";
	public static final String ERROR_INVALID_MANIFEST = "Manifest not found for tag %s";
	
	public static final String TAGS_FOUND = "Tags found: %s";
	public static final String FILES_FOUND = "Assets found: %s";
	public static final String SCRIPTS_FOUND = "Scripts found: %s";
	public static final String XML_CONFIG_FOUND = "XML config files found: %s";
	public static final String JARS_FOUND = "Jars found: %s";
	public static final String ATTACHED_FILES_FOUND = "Attached files found: %s";
	
	public static final String SERVICE_RESPONSE = "Github call at %s service - response: %s";
	public static final String SERVICE_RETURNS = "Returning data %s";
	public static final String SERVICE_INIT = "%s initialized, repo: %s, owner: %s, token: %s, API version: %s";
	
	public static final String REQUEST_FOR_SERVICE = "Request for github API service at %s";
	public static final String REQUEST_FOR_BINARY_FILE = "Request for github binary file at %s";
	public static final String BINARY_FILE_AVAILABLE = "Opened Stream for %s, available: %s";
	public static final String CALLING_URL = "Calling github API for %s";
}
