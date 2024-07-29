package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.tfsla.diario.model.TfsEncuesta;
import com.tfsla.opencmsdev.encuestas.Encuesta;
import com.tfsla.opencmsdev.encuestas.ModuloEncuestas;
import com.tfsla.opencmsdev.encuestas.ResultadoEncuestaBean;

public class TfsEncuestaTag  extends BaseTag implements I_TfsEncuesta {

	private String url = null;
	private Encuesta encuesta = null;
	private TfsEncuesta previousEncuesta = null;
	private ResultadoEncuestaBean resultados = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8598854108401740669L;
	/** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(TfsEncuestaTag.class);

	public int doStartTag() throws JspException {

		init();
		
		saveEncuesta();
		
		try {
			encuesta = Encuesta.getEncuestaFromURL(m_cms, url);
			resultados = ModuloEncuestas.getResultado(m_cms, url);
			 

		} catch (Exception e) {
			LOG.error("Failed to fecth the poll " + url,e);
			return SKIP_BODY;
		}
	
		
		exposeEncuesta(encuesta,resultados);
		
		return EVAL_BODY_INCLUDE;
	}
	
	@Override
	public int doEndTag() throws JspException {
		saveEncuesta();
		
		if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
			release();
		}
		return super.doEndTag();
	}

    protected void exposeEncuesta(Encuesta poll, ResultadoEncuestaBean results)
    {
    	TfsEncuesta encuesta = new TfsEncuesta(poll,results);
		pageContext.getRequest().setAttribute("poll", encuesta);
    }
    
    protected void restoreEncuesta()
    {
    	pageContext.getRequest().setAttribute("poll", previousEncuesta);
    }

	protected void saveEncuesta()
    {
		previousEncuesta = (TfsEncuesta) pageContext.getRequest().getAttribute("poll");
    	pageContext.getRequest().setAttribute("poll",null);
    }

	public Encuesta getEncuesta() {
		return encuesta;
	}

	public ResultadoEncuestaBean getResultadosEncuesta() {
		return resultados;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getEncuestaUrl() {
		return url;
	}

}
