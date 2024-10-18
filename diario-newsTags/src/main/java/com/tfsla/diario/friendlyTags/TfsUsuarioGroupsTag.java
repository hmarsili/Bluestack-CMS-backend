package com.tfsla.diario.friendlyTags;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.file.CmsGroup;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.model.TfsUsuario;


public class TfsUsuarioGroupsTag extends A_TfsUsuarioValueTag  implements I_TfsCollectionListTag{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2858816904359122058L;
	private static final Log LOG = CmsLog.getLog(TfsUsuarioGroupsTag.class);
	private int idx = -1;
	private List<CmsGroup> grupos=null;
	@Override
	    public int doStartTag() throws JspException {
		idx = -1;
	    	//try {
	    		//TfsUsuario usuario =(TfsUsuario) pageContext.getAttribute("ntuser");//
	    		CmsUser user=getCurrentUser().getUser();
	    		 CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    		if (user!=null){
	    			
					try {
						grupos =cms.getGroupsOfUser(user.getName(),true, true);
					} catch (CmsException e) {
						// TODO Auto-generated catch block
						LOG.error("ERROR tfsUsuarioGroupsTag"+ e.getStackTrace());
					}
	    			//for(CmsGroup gr: grupos){
	    			//pageContext.getOut().print(gr.getName());
	    			//}
	    		}
			//} catch (IOException e1) {
				//throw new JspException(e1);
			//	LOG.error("ERROR tfsUsuarioGroupsTag"+ e1.getStackTrace());
			//}
			
	    	 return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );
	    }
	private boolean hasMoreContent()
	{
		
		if (grupos==null)
			return false;
		
		idx++;
		for (; idx<grupos.size() && grupos.get(idx).getName().isEmpty() ;idx++);
		
		if (idx==grupos.size())
			return false;
		
		
		addGroupToContext();
		//respuestasMostradas++;
		return true;
	}
	private void addGroupToContext()
	{
		TfsUsuario tfsusuario = (TfsUsuario)pageContext.getRequest().getAttribute("ntuser");
		//RespuestaEncuestaConVotos resp = encuesta.getResultadosEncuesta().getRespuestas().get(idx);		
		String gruponombre = new String( grupos.get(idx).getName());
		
		//tfsusuario. Encuesta.setOption(option);
	}
	@Override
	public int doAfterBody() throws JspException {

		if (hasMoreContent()) {
			//addOptionToContext();
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
		return EVAL_PAGE;
	}

	public int getIndex() {
		return idx;
	}

	public boolean isLast() {
		return (grupos.size()==idx+1);
	}

	public String getValue()
	{
		return grupos.get(idx).getName();
	}

	
	

	
	public String getCollectionPathName() throws JspTagException {
		// TODO Auto-generated method stub
		return "";
	}

	
	public String getCollectionValue(String name) throws JspTagException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public String getCollectionIndexValue(String name, int index)
			throws JspTagException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public int getCollectionIndexSize(String name, boolean isCollectionPart)
			throws JspTagException {
		// TODO Auto-generated method stub
		return 0;
	}

}
