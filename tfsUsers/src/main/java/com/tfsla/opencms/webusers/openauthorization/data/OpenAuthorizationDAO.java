package com.tfsla.opencms.webusers.openauthorization.data;

import com.tfsla.data.baseDAO;

public class OpenAuthorizationDAO extends baseDAO {
	public boolean openConnection() {
		if (!connectionIsOpen()) {
			try {
				OpenConnection();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	public void closeConnection() {
		if (connectionIsOpenLocaly()) {
			try {
				super.closeConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
