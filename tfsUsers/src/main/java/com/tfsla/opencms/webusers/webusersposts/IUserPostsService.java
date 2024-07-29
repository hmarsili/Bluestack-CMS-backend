package com.tfsla.opencms.webusers.webusersposts;

import org.opencms.file.CmsUser;

public interface IUserPostsService {
	/**
	 * Updates the user posts to PENDING, since the posts were awaiting
	 * for the user to verify his account
	 * @param user posts owner
	 * @return true if some posts were updated
	 */
	Boolean processUserActivePosts(CmsUser user);
}
