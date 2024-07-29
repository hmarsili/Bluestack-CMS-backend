package com.tfsla.opencms.follow.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.opencms.follow.data.FollowDAO;
import com.tfsla.opencms.follow.model.UserFollow;

public class UserFollowService{
	private static Map<String, UserFollowService> instances = new HashMap<String, UserFollowService>();
	private int UserMaxFollow;
	
	private UserFollowService(String siteName, String publication) {
		this.loadProperties(siteName,publication);
	}
	
	public void loadProperties(String siteName, String publication) {
		
    	String module = "webusers";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration(); 		
 		UserMaxFollow = config.getIntegerParam(siteName, publication, module, "userMaxFollow", 20); 		 		
	}
	
	public static UserFollowService getInstance(String siteName, String publication) {
    	String id = siteName + "||" + publication;
    	
    	UserFollowService instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new UserFollowService(siteName, publication);

	    	instances.put(id, instance);
    	}
        return instance;
    
    }
   

    public static UserFollowService getInstance(CmsObject cms)
    {
    	
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return getInstance(siteName, publication);
    }
    
    public int getUserMaxFollow(){
		return this.UserMaxFollow;
	}
    
 // ******************************
 	// ** template methods
 	// ******************************
 	protected FollowDAO getFollowDAO() {
 		return new FollowDAO();
 	}
    
    /**
	 * Obtiene una lista de todos los followers de un usuario determinado.
	 * @param cms
	 * @param userName
	 * @param pageNumber
	 * @return List<Comment>currentUri
	 */
	public List<CmsUser> getFollowersPerUser(CmsObject cms, String userName) {
		List<UserFollow> userFollowList = new ArrayList<UserFollow>();
		List<CmsUser> userList = new ArrayList<CmsUser>();
		//String siteName = getSiteName(cms); 
		
		//CmsUser user = cms.readUser(userName);
		try {
			userFollowList = this.getFollowDAO().getFollowersPerUser(cms.readUser(userName).getId().getStringValue());
		} catch (CmsException e) {
			e.printStackTrace();
		}
		for(UserFollow uF : userFollowList){			
			try {
				String userFollowerId = uF.getSeguidor();
				CmsUUID uId = new CmsUUID(userFollowerId);
				CmsUser userFollower = cms.readUser(uId);
				userList.add(userFollower);
			} catch (CmsException e) {
				e.printStackTrace();
			}
		}
		
		return userList;
	}
	
	/**
	 * Obtiene la cantidad de usuarios que siguen a un autor.
	 * @param cms
	 * @param userName
	 * @param pageNumber
	 * @return List<Comment>
	 */
	public int getCountFollowersPerUsers(CmsObject cms, String userName) {
		//List<UserFollow> userFollowList = new ArrayList<UserFollow>();
		int countFollowers = 0;
		//List<CmsUser> userList = new ArrayList<CmsUser>();
		
		try {
			countFollowers = this.getFollowDAO().getCountFollowersPerUser(cms.readUser(userName).getId().getStringValue());
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return countFollowers;
	}
	
    /**
	 * Obtiene una lista de todos los usuarios que un autor sigue.
	 * @param cms
	 * @param userName
	 * @param pageNumber
	 * @return List<Comment>
	 */
	public List<CmsUser> getFollowingUsers(CmsObject cms, String userName) {
		List<UserFollow> userFollowList = new ArrayList<UserFollow>();
		List<CmsUser> userList = new ArrayList<CmsUser>();
		
		try {
			userFollowList = this.getFollowDAO().getFollowingByUser(cms.readUser(userName).getId().getStringValue());
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		for(UserFollow uF : userFollowList){			
			try {
				String userFollowingId = uF.getSeguido();
				CmsUUID uId = new CmsUUID(userFollowingId);
				CmsUser userFollowing = cms.readUser(uId);
				userList.add(userFollowing);
			} catch (CmsException e) {
				e.printStackTrace();
			}
		}
		
		return userList;
	}
	
	/**
	 * Obtiene la cantidad de usuarios que un autor sigue.
	 * @param cms
	 * @param userName
	 * @param pageNumber
	 * @return List<Comment>
	 */
	public int getCountFollowingUsers(CmsObject cms, String userName) {
		int countFollowers = 0;
		
		try {
			countFollowers = this.getFollowDAO().getCountFollowingPerUser(cms.readUser(userName).getId().getStringValue());
		} catch (CmsException e) {
			e.printStackTrace();
		}
		
		return countFollowers;
	}
	
	/**
	 * Agrega un Follower.
	 */
	public String addFollower(CmsObject cms, String userFollower, String userFollow) {
		int cantFollow = getCountFollowingUsers(cms, userFollower);
		List<UserFollow> followingUsers = new ArrayList<UserFollow>();
		//Checkeo que no estoy agregando un usuario repetido
		try {
			CmsUser uFollow = cms.readUser(userFollower);
			CmsUser uFollowing = cms.readUser(userFollow);
			followingUsers = this.getFollowDAO().getFollowingByUser(uFollow.getId().getStringValue());
			for(UserFollow uF : followingUsers){
				CmsUUID uId = new CmsUUID(uF.getSeguido());
				CmsUser u = cms.readUser(uId);
				if(u.getName().equals(uFollowing.getName())){
					return "error";
				}					
			}
		} catch (CmsException e) {
			return "error" + e;
		}		
		//Checkeo que no estoy agregando mas usuarios que los permitidos
		if(cantFollow <= UserMaxFollow){
			CmsUser uFollow = null;
			CmsUser uFollower = null;
			try {
				uFollow = cms.readUser(userFollow);
				uFollower = cms.readUser(userFollower);
			} catch (CmsException e) {
				return "error" + e;
			}
			
			UserFollow newFollower = new UserFollow();
			newFollower.setFecha(new Date());
			newFollower.setSeguido(uFollow.getId().getStringValue());
			newFollower.setSeguidor(uFollower.getId().getStringValue());
			
			try{
				this.getFollowDAO().addFollower(newFollower);
			}catch(Exception ex){
				return "error" + ex;
			}
			
			return "ok-"+uFollow.getName();
		}
		return "maxfollow";
	}
	
	/**
	 * elimina un Follower.
	 */
	public String deleteFollower(CmsObject cms, String userFollower, String userFollow) {	
		CmsUser uFollow = null;
		CmsUser uFollower = null;
		try {
			uFollow = cms.readUser(userFollow);
			uFollower = cms.readUser(userFollower);
		} catch (CmsException e) {
			return "error" + e;
		}
		try{
			this.getFollowDAO().deleteFollower(uFollow.getId().getStringValue(), uFollower.getId().getStringValue());
		}catch(Exception ex){
			return "error" + ex;
		}
		
		return "ok";
	}
	
	@SuppressWarnings("unused")
	private String getSiteName(CmsObject cms)
	{
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getTitle();
		siteName = siteName.replaceFirst("/sites/", "");
        siteName = siteName.replaceFirst("/site/", "");
        
		return siteName.substring(0,siteName.length() -1);
	}
}
