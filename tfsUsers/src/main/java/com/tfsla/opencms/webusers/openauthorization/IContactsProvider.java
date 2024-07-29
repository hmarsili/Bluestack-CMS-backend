package com.tfsla.opencms.webusers.openauthorization;

import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;

import com.tfsla.opencms.webusers.openauthorization.common.SocialContact;

/*
 * Interfaz para componentes de obtención de datos de contactos de usuario
 */
public interface IContactsProvider extends IOpenProvider {
	
	void inviteContact(CmsUser user, String contactId);
	
	List<SocialContact> getContacts();
	
	void setUserInviteContacts(CmsUser user, CmsObject cms, Boolean invite);
	
	Boolean getUserInviteContacts(CmsUser user);
}
