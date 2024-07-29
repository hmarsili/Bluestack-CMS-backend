package com.tfsla.diario.friendlyTags;

import java.util.List;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;

import com.tfsla.diario.model.TfsLista;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderListField;
import com.tfsla.opencms.webusers.openauthorization.data.ProviderFieldDAO;

public class TfsSocialProvidersListItemsTag extends A_TfsUsuarioValueTag {

	@Override
	public int doStartTag() throws JspException {
		saveFields();
		
		ProviderFieldDAO dao = new ProviderFieldDAO();
		try {
			dao.openConnection();
			String userId = "";
			if(this.getFilterCurrentUser()){
				CmsUser user=getCurrentUser().getUser();
				userId = user.getId().toString();
			}
			this.values = dao.getListValues(this.getEntryName(), userId);
		} catch(Exception e) {
			e.printStackTrace();
		}finally{
			dao.closeConnection();
		}
		
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	}
	
	@Override
	public int doAfterBody() throws JspException {
		if (hasMoreContent()) {
			return EVAL_BODY_AGAIN;
		}
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return SKIP_BODY;
	}
	
	@Override
	public int doEndTag() {
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		index = -1;
		return EVAL_PAGE;
	}
	
	private boolean hasMoreContent() {
		if(values == null) return false;
		
		index++;
		
		if (index<values.size())
			exposeField(values.get(index));
		else
			restoreFields();

		return (index<values.size());
	}
	
	private void exposeField(ProviderListField value) {
		TfsLista lista = new TfsLista(this.values.size(),this.index+1,this.size,this.page);
		pageContext.getRequest().setAttribute("listValues", lista);
		pageContext.getRequest().setAttribute("item", value);
	}
	
	private void restoreFields() {
		pageContext.getRequest().setAttribute("item", previousValue);
    	pageContext.getRequest().setAttribute("listValues", previousList);
	}
	
	private void saveFields() {
		previousList = (TfsLista) pageContext.getRequest().getAttribute("listValues");
		previousValue  = (String) pageContext.getRequest().getAttribute("item");
		
    	pageContext.getRequest().setAttribute("listValues",null);
    	pageContext.getRequest().setAttribute("item",null);
    }
	
	private int index = -1;
	private int size=0;
	private int page=1;
	private Boolean filterCurrentUser;
	private String entryName;
	private String previousValue;
	private TfsLista previousList;
	private List<ProviderListField> values;
	
	public ProviderListField getField() {
		return values.get(index);
	}
	
	public int getIndex() {
		return index;
	}

	public boolean isLast() {
		return (index==values.size()-1);
	}
	
	public String getEntryName() {
		return entryName;
	}

	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	public Boolean getFilterCurrentUser() {
		return filterCurrentUser == null ? false : filterCurrentUser;
	}

	public void setFilterCurrentUser(Boolean filterCurrentUser) {
		this.filterCurrentUser = filterCurrentUser;
	}

	private static final long serialVersionUID = -2109066881815252864L;
}
