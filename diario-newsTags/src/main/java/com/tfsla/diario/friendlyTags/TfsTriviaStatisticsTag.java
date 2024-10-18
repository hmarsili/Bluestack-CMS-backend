package com.tfsla.diario.friendlyTags;

import java.util.List;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;

import org.apache.commons.logging.Log;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlException;

import com.tfsla.diario.utils.TfsXmlContentNameProvider;
import com.tfsla.trivias.data.TfsTriviasDAO;
import com.tfsla.trivias.model.TfsTrivia;

public class TfsTriviaStatisticsTag  extends A_TfsTriviaCollection  {
	
	private static final long serialVersionUID = -5379220139840466427L;

	protected static final Log LOG = CmsLog.getLog(TfsTriviaStatisticsTag.class);
	List<TfsTrivia> trivias = null;
	TfsTrivia trivia = null;
	private String results="10";
	
	@Override
	public int doStartTag() throws JspException {
		
		index = 0;
		
		I_TfsTrivia trivia = getCurrentTrivia();
		
		TfsTriviasDAO tDAO = new TfsTriviasDAO ();
		String triviaPath = CmsFlexController.getCmsObject(pageContext.getRequest()).getRequestContext().removeSiteRoot(trivia.getXmlDocument().getFile().getRootPath());  
		
		String type =null;
		try {
			type = trivia.getXmlDocument().getStringValue(CmsFlexController.getCmsObject(pageContext.getRequest()), 
					TfsXmlContentNameProvider.getInstance().getTagName("trivia.resultsType"),
					CmsFlexController.getCmsObject(pageContext.getRequest()).getRequestContext().getLocale());
		} catch (CmsXmlException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
		if (type != null) {
			try {
				if (type.equals("scale"))
					trivias = tDAO.getTopTenByResultType(triviaPath,type,results);
				else 
					trivias = tDAO.getCantUsersByClassification(triviaPath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (index<trivias.size()) {
			exposeTriviasResult (index);
			return EVAL_BODY_INCLUDE;
		} 
		return SKIP_BODY;
				
	}

	private void exposeTriviasResult(int index) {
		trivia = trivias.get(index);
		
		
	}

	@Override
	public int doAfterBody() throws JspException {
		
		index++;

		//if (index==trivias.size())
			//restoreTrivia(index);
		
		if (index<trivias.size()) {
			exposeTriviasResult(index);
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
	
	public String getCollectionValue(String name) throws JspTagException {
		LOG.debug("TfsTriviaStatisticTag - getCollectionValue (name:" + name + ")");

		String elementName = getElementName(name);
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("trivia.resultName")))
			return (trivia.getResultName()!=null ? trivia.getResultName() : "");
		
		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("trivia.resultPoints")))
			return (trivia.getResultPoints()!=null ? trivia.getResultPoints() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("trivia.resultDate")))
			return (trivia.getResultDate()!=null ? String.valueOf(trivia.getResultDate().getTime()) : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("trivia.resultType")))
			return (trivia.getResultType()!=null ? trivia.getResultType() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("trivia.userName")))
			return (trivia.getUser()!=null ? trivia.getUser() : "");

		if (elementName.equals(TfsXmlContentNameProvider.getInstance().getTagName("trivia.timeResolution")))
			return (trivia.getTimeResolution()!= 0 ? String.valueOf(trivia.getTimeResolution()) : ""); 
		if (elementName.contains(TfsXmlContentNameProvider.getInstance().getTagName("trivia.cantUsers")))
			return (trivia.getCantUsers()!=0 ? String.valueOf(trivia.getCantUsers()) : "");
	
		return null;
	}
	
	private String getElementName(String elementName) {
		String[] parts = elementName.split("/");
		String element = parts[parts.length-1];
		
		int end = element.indexOf("[");
		
		return element.substring(0, end);
		
	}

	public String getResults() {
		return results;
	}

	public void setResults(String results) {
		this.results = results;
	}

}
