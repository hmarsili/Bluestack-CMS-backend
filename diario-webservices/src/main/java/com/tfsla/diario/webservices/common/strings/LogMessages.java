package com.tfsla.diario.webservices.common.strings;

public class LogMessages {
	public static final String PUSH_NOT_ENABLED = "The push is not enabled for the site %s";
	public static final String PUSH_JOB_STARTED = "PushNotificationsJob started - %s news today";
	public static final String PUSH_JOB_NEWS = "PushNotificationsJob - %s news to be pushed, now calling push services...";
	public static final String PUSH_JOB_ERROR = "Error on PushNotificationsJob";
	public static final String PUSH_JOB_ERROR_ON_START = "Error when starting PushNotificationsJob";
	public static final String PUSH_JOB_FINISHED_IDLE = "The push notifications job finished since is out of the running window";
	public static final String PUSH_JOB_FINISHED = "PushNotificationsJob finished - %s news to process, %s pushed, %s errors";
	public static final String PUSH_JOB_FINISHED_NO_NEWS = "PushNotificationsJob finished - no news to process";
	public static final String ERROR_REGISTERING_PUSH = "Error registering push for item %s";
	public static final String ERROR_UPDATING_JSON = "Error updating json with latest pushed element";
	public static final String PUSH_JOB_SCHEDULE_STARTED = "Push job scheduler started - %s news published, processing push schedules...";
	public static final String PUSH_QUEUE_JOB_SCHEDULE_STARTED = "Push queue job scheduler started. Adding resource %s";
	public static final String PUSH_QUEUE_JOB_SCHEDULE_FINISHED = "Push queue job scheduler finished. Resource %s added to queue in topic %s with ID %s";
	public static final String PUSH_DATED_JOB_SCHEDULE_STARTED = "Push job scheduler started. Adding resource %s";
	public static final String PUSH_DATED_JOB_SCHEDULE_FINISHED = "Push job scheduler finished. Resource %s added to queue in topic %s with ID %s";

	public static final String PUSH_JOB_SCHEDULED = "Scheduled job %s for resource '%s'";
	public static final String PUSH_JOB_SCHEDULE_FINISHED = "Push job scheduler finished - %s jobs scheduled, %s errors";
	public static final String PUSH_ITEM_ERROR = "The push failed to all the platforms";
	public static final String PUSH_ITEM_OK = "Pushed to %s";
	public static final String PUSH_ITEM_PLATFORM_ERROR = "The push failed to the following platforms: %s";
	public static final String PUSH_ITEM_AMZ_ERROR = "The push failed. Contact your administrator";
	public static final String REMOVING_ANDROID_CLIENT = "Removing Android client id '%s'";
	public static final String REMOVING_APPLE_CLIENT = "Removing Apple client token '%s'";
	public static final String PUSH_DISABLED = "Push disabled, push type: %s";
	public static final String ITEM_ALREADY_PUSHED = "Item aldready pushed, id: %s, skipping check: %s";
}