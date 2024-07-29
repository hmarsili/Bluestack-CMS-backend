package org.opencms.db;

import org.opencms.file.CMSObjectAccesor;
import org.opencms.file.CmsObject;

/**
 * Se mete en las entrañas del CMS para obtener una connection a la base de datos viola el encapsulamiento (o el
 * escondite) de la Connection.
 * 
 * Debe estar en el mismo package que CmsSecurityManager
 * 
 * @author Leo
 */
public class SecurityManagerAccesor {

	/**
	 * El CMSDBContext
	 * @param object
	 * @return
	 */
	public static CmsDbContext getCmsDbContext(CmsObject object) {
		return CMSObjectAccesor.getSecurityManager(object).m_dbContextFactory.getDbContext(object.getRequestContext());
	}
		
}
