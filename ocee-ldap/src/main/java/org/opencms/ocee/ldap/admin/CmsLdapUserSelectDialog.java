package org.opencms.ocee.ldap.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.i18n.I_CmsMessageBundle;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;


public class CmsLdapUserSelectDialog
  extends CmsWidgetDialog
{
  public static final String DIALOG_TYPE = "UserSelect";
  public static final String[] PAGES = { "page1" };
  


  public static final String PARAM_USERNAME = "username";
  


  private String userName;
  



  public CmsLdapUserSelectDialog(CmsJspActionElement jsp)
  {
    super(jsp);
  }
  







  public CmsLdapUserSelectDialog(PageContext context, HttpServletRequest req, HttpServletResponse res)
  {
    this(new CmsJspActionElement(context, req, res));
  }
  


  public void actionCommit()
    throws IOException, ServletException
  {
    List errors = new ArrayList();
    try
    {
      Map params = new HashMap();
      getCms().getRequestContext().setAttribute("SKIP_SYNC", Boolean.TRUE);
      CmsUser user = getCms().readUser(getUsername());
      params.put("userid", user.getId());
      params.put("oufqn", user.getOuFqn());
      
      getToolManager().jspForwardTool(this, "/ocee-ldap/orgunit/users/edit", params);
    } catch (CmsException e) {
      errors.add(e);
      
      setCommitErrors(errors);
    } finally {
      if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
        getCms().getRequestContext().removeAttribute("SKIP_SYNC");
      } else {
        getCms().getRequestContext().setAttribute("SKIP_SYNC", Boolean.FALSE);
      }
    }
  }
  





  public String getUsername()
  {
    return userName;
  }
  




  public void setUsername(String username)
  {
    try
    {
      getCms().getRequestContext().setAttribute("SKIP_SYNC", Boolean.TRUE);
      getCms().readUser(username).getId();
    } catch (CmsException e) {
      throw new CmsIllegalArgumentException(e.getMessageContainer());
    } finally {
      if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
        getCms().getRequestContext().removeAttribute("SKIP_SYNC");
      } else {
        getCms().getRequestContext().setAttribute("SKIP_SYNC", Boolean.FALSE);
      }
    }
    userName = username;
  }
  

  protected String createDialogHtml(String dialog)
  {
    StringBuffer result = new StringBuffer(1024);
    

    result.append(createWidgetTableStart());
    

    result.append(createWidgetErrorHeader());
    
    if (dialog.equals(PAGES[0])) {
      result.append(dialogBlockStart(key("GUI_LDAP_USER_SELECT_BLOCK_0")));
      result.append(createWidgetTableStart());
      result.append(createDialogRowsHtml(0, 0));
      result.append(createWidgetTableEnd());
      result.append(dialogBlockEnd());
    }
   
    result.append(createWidgetTableEnd());
    
    return result.toString();
  }
  
  protected void defineWidgets()
  {
    addWidget(new CmsWidgetDialogParameter(this, "username", PAGES[0], new CmsInputWidget()));
  }
  



  protected String[] getPageArray()
  {
    return PAGES;
  }
  
  protected void initMessages()
  {
    addMessages(Messages.get().getBundleName());
    
    super.initMessages();
  }
  
  protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request)
  {
    setParamDialogtype("UserSelect");
    
    super.initWorkplaceRequestValues(settings, request);
  }
}