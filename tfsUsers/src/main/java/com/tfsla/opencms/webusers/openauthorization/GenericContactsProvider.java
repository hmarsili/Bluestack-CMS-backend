package com.tfsla.opencms.webusers.openauthorization;

import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;

import com.tfsla.opencms.webusers.openauthorization.common.SocialContact;

public abstract class GenericContactsProvider extends GenericProvider implements IContactsProvider {
	
	public List<SocialContact> getContacts() {
		if(this.contacts == null) {
			this.contacts = this.retrieveContacts();
		}
		return this.contacts;
	}
	
	public Boolean getUserInviteContacts(CmsUser user) {
		if(user == null) return false;
		
		Object additionalInfo = user.getAdditionalInfo(this.getInviteContactsDataKey());
		if(additionalInfo == null) return true;
		
		String userData = additionalInfo.toString();
		if(userData.equals("")) return true;
		
		return Boolean.parseBoolean(userData);
	}
	
	public void setUserInviteContacts(CmsUser user, CmsObject cms, Boolean invite) {
		try {
			user.setAdditionalInfo(this.getInviteContactsDataKey(), invite);
			cms.writeUser(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected abstract List<SocialContact> retrieveContacts();
	
	protected SocialContact getContact(String contactId) {
		for(SocialContact contact : this.getContacts()) {
			if(contact.getId().equals(contactId))
				return contact;
		}
		return null;
	}
	
	private String getInviteContactsDataKey() {
		return this.getProviderName().toUpperCase() + "_INVITE_CONTACTS";
	}
	
	private List<SocialContact> contacts;
}