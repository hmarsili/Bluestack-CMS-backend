package com.tfsla.diario.webservices.core;

import java.util.Hashtable;

import com.tfsla.diario.webservices.*;
import com.tfsla.diario.webservices.PushNotificationServices.*;
import com.tfsla.diario.webservices.common.interfaces.*;

@SuppressWarnings("rawtypes")
public class TfsWebServiceDependenciesContainer implements IDependenciesContainer {

	@Override
	public Hashtable<Class, Class> getDependencies() {
		if(_dependencies == null) {
			_dependencies = new Hashtable<Class, Class>();
			_dependencies.put(IAuthorizationService.class, AuthorizationService.class);
			_dependencies.put(INewsListService.class, NewsListService.class);
			_dependencies.put(IUsersListService.class, UsersListService.class);
			_dependencies.put(IUsersAddService.class, UsersAddService.class);
			_dependencies.put(INewsAddService.class, NewsAddService.class);
			_dependencies.put(INewsEditService.class, NewsEditService.class);
			_dependencies.put(INewsPublishService.class, NewsPublishService.class);
			_dependencies.put(IPostsAddService.class, PostsAddService.class);
			_dependencies.put(IImagesAddWebService.class, ImagesAddWebService.class);
			_dependencies.put(IVideosAddWebService.class, VideosAddWebService.class);
			_dependencies.put(IFacebookLoginService.class, FacebookLoginService.class);
			_dependencies.put(IRegisterPushClientService.class, RegisterPushClientService.class);
			_dependencies.put(IUnsubscribePushClientService.class, UnsubscribePushClientService.class);
			_dependencies.put(IPostsListService.class, PostsListService.class);
			_dependencies.put(ISwitchProjectService.class, SwitchProjectService.class);
			_dependencies.put(ISwitchSiteService.class, SwitchSiteService.class);
			_dependencies.put(IForgotPasswordService.class, ForgotPasswordService.class);
			_dependencies.put(IRegisterWebUserService.class, RegisterWebUsersService.class);
			_dependencies.put(ITermGetService.class, TermGetService.class);
			_dependencies.put(IPersonGetService.class, PersonGetService.class);
			_dependencies.put(IUsersEditService.class, UsersEditService.class);
			_dependencies.put(IExternalProviderLoginService.class, ExternalProviderLoginService.class);
			_dependencies.put(IUpdateProfileService.class, UpdateProfileService.class);
			
		}
		return _dependencies;
	}
	
	private static Hashtable<Class, Class> _dependencies;
}
