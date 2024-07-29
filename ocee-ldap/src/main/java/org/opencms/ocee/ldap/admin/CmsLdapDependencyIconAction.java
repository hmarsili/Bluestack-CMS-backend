package org.opencms.ocee.ldap.admin;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.ldap.CmsLdapManager;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.tools.accounts.CmsDependencyIconAction;
import org.opencms.workplace.tools.accounts.CmsDependencyIconActionType;

public class CmsLdapDependencyIconAction
  extends CmsDependencyIconAction
{
  public CmsLdapDependencyIconAction(String id, CmsDependencyIconActionType type, CmsObject cms)
  {
    super(id + type.getId(), type, cms);
  }

  public String getIconPath()
  {
    try
    {
      if (getType() == CmsDependencyIconActionType.USER) {
        getCms().getRequestContext().setAttribute("SKIP_SYNC", Boolean.TRUE);
        CmsUser user = getCms().readUser(new CmsUUID(getItem().getId()));
        if (CmsLdapManager.hasLdapFlag(user.getFlags())) {
          return "tools/ocee-ldap/buttons/user.png";
        }
        return "tools/accounts/buttons/user.png"; }
      CmsGroup group;
      if (getType() == CmsDependencyIconActionType.GROUP) {
        group = getCms().readGroup(new CmsUUID(getItem().getId()));
        if (CmsLdapManager.hasLdapFlag(group.getFlags())) {
          return "tools/ocee-ldap/buttons/group.png";
        }
        return "tools/accounts/buttons/group.png";
      }
      
      return super.getIconPath();
    } catch (Exception e) {
      //String str;
      return super.getIconPath();
    } finally {
      if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
        getCms().getRequestContext().removeAttribute("SKIP_SYNC");
      } else {
        getCms().getRequestContext().setAttribute("SKIP_SYNC", Boolean.FALSE);
      }
    }
  }
}
