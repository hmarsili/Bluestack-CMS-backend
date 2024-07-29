package com.tfsla.diario.webservices.common.strings;

/**
 * Contains static exception messages strings
 */
public final class ExceptionMessages {
	
	public static final String ERROR_NO_CONSTRUCTOR = "There is no constructor suitable for the service requested with the parameters provided";
	public static final String ERROR_UNABLE_GET_INSTANCE = "Unable to get an instance of the service requested";
	public static final String MISSING_VIDEO_TYPE = "You must specify the video-type parameter";
	public static final String ERROR_UPLOADING_GENERIC = "Error while uploading files";
	public static final String ERROR_UPLOADING_AT_INDEX = "Error while uploading file at index %s";
	public static final String ERROR_UPLOADING_FILE = "Error while uploading the file, you dont have permissions to manage directories in the file system";
	public static final String UNRECOGNIZED_FILE_EXTENSION = "Unrecognized file extension";
	public static final String FILE_EXTENSION_NOT_ALLOWED_FORMAT = "The file extension '%s' is not allowed";
	public static final String MISSING_OR_EMPTY_PARAMETER = "The parameter %s cannot be empty";
	public static final String CLIENT_ALREADY_REGISTERED = "The client is already registered";
	public static final String ERROR_REGISTERING_CLIENT = "An error occured while trying to register the client";
	public static final String ERROR_REGISTERING_CLIENT_PUBLICATION = "An error occured while trying to register client publication";
	public static final String ERROR_REMOVING_CLIENT = "An error occured while trying to remove the client %s";
	public static final String ERROR_REGISTERING_ENDPOINT = "An error occured while trying to register the endpoint";
	public static final String ERROR_REMOVING_ENDPOINT = "An error occured while trying to remove the endpoint %s";
	public static final String ERROR_RETRIEVING_GENERATED_KEYS = "An error occured while trying to retrieve the generated keys";
	public static final String ERROR_COPYING_PUSHED_NEW = "An error occured while trying to copy a pushed new";
	public static final String ERROR_ADDING_PUSHED_NEW = "An error occured while trying to register a pushed new";
	public static final String ERROR_SETTING_PUSHED_NEW = "An error occured while trying to flag a new as pushed";
	public static final String ERROR_REMOVING_PUSH = "An error occured while trying to remove the push with id %s";
	public static final String ERROR_RETRIEVING_NEWS_TO_PUSH = "An error occured while trying to retrieve the news to push";
	public static final String ERROR_RETRIEVING_PUSH_ITEM = "An error occured while trying to retrieve the item to push with id %s";
	public static final String ERROR_PUSHING_TO_ANDROID = "Error while pushing message to Android - %s";
	public static final String ERROR_PUSHING_TO_ANDROID_MULTICAST = "Error while multicasting to Android devices - %s";
	public static final String ERROR_PUSHING_ITEM = "Error while pushing item %s to %s";
	public static final String ERROR_CONNECTING_PUSH_SERVICE = "Error while trying to connect to the %s Push service";
	public static final String ERROR_CHECKING_NEW = "Error when checking for a pushed new";
	public static final String ERROR_SCHEDULING_PUSH_JOB = "Error when scheduling push job for resoruce '%s'";
	public static final String ERROR_UNSCHEDULING_PUSH_JOB = "Error when scheduling push job for push id %s";
	public static final String ERROR_PUSH_JOB = "Error when scheduling push job";
	public static final String ERROR_UPDATING_PRIORITY = "Error when updating priority";
	public static final String ERROR_UPDATING_PUSH_PRIORITY = "Error when updating priority for push item %s";
	public static final String ERROR_SCHEDULED_DATE = "The date provided to schedule the push must be greater than current date";
	public static final String ERROR_MISSING_TOKEN = "User token or user ID missing or empty (parameter name: 'token')";
	public static final String ERROR_MISSING_SITE = "Site missing or empty (parameter name: 'site')";
	public static final String ERROR_MISSING_PUBLICATION = "Publication missing or empty (parameter name: 'publication')";
	public static final String NO_CLIENTS_FOR_PLATFORM = "There are no clients registered for the plaform %s";
	public static final String MISSING_SCHEDULE_DATE = "The XML element 'schedulePush' was expected, but it was null or empty";
	public static final String ERROR_RETRIEVING_PUSH_SCHEDULE = "An error occured while trying to retrieve the push schedule";
	public static final String ERROR_ANONYMOUS_POSTS_NOT_ALLOWED = "Anonymous posts not allowed for site %s, publication %s";
	public static final String ERROR_PROJECT_SWITCH_NOT_ALLOWED = "Switching projects not allowed for site %s, publication %s";
	public static final String ERROR_INVALID_PROJECT = "The project %s seems to be invalid";
	public static final String INSUFFICIENT_PRIVILEGES = "The token has insufficient privileges for this operation";
	public static final String ERROR_ACCESS_TOKEN = "The token does not have access for the service requested";
	public static final String ERROR_USER_NOT_CREATOR = "The user %s associated with the token is not the content creator";
	public static final String ERROR_PROFILE_NOT_FROM_USER = "The user %s associated with the token is not the profile owner";
	public static final String ERROR_EXPIRATION_DATES = "The release date cannot be greater than the expiration date";
	public static final String ERROR_INVALID_USER_FOR_EMAIL = "Cannot find a user for the email provided";
	public static final String ERROR_SERVICE_CALL = "Error on web service call";
	public static final String ERROR_RECORD_NOT_FOUND_FORMAT = "Cannot find %s with %s '%s'";
	
}
