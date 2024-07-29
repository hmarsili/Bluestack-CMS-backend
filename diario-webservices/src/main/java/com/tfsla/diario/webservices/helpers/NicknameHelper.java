package com.tfsla.diario.webservices.helpers;

import org.opencms.main.CmsLog;

import com.tfsla.diario.webservices.data.UserInfoDAO;
import com.tfsla.webusersposts.common.INickNameHelper;

public class NicknameHelper implements INickNameHelper {
	
	/**
	 * Retrieves a valid and unique nickname from the CMS_USERDATA table
	 * @param userName the userName to check for
	 * @param nickName a suggested nickname
	 * @return a unique nickname
	 */
	public static synchronized String getValidNickname(String userName, String nickName) {
		UserInfoDAO dao = new UserInfoDAO();
		try {
			dao.openConnection();
			String maxId = dao.getNickMaxId(userName, nickName);
			if(maxId == null) return nickName;
			int newId = Integer.parseInt(maxId) + 1;
			return nickName + "_" + newId;
		} catch(Exception e) {
			e.printStackTrace();
			CmsLog.getLog(NicknameHelper.class).error("Error while trying to get a unique nickname", e);
			return nickName;
		} finally {
			dao.closeConnection();
		}
	}
	
	public String getUniqeNickname(String userName, String nickName) {
		return getValidNickname(userName, nickName);
	}
}
