package com.tfsla.diario.friendlyTags;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;

import org.opencms.main.CmsLog;

import com.tfsla.diario.model.TfsPersona;
import com.tfsla.diario.terminos.data.PersonsDAO;
import com.tfsla.diario.terminos.model.Persons;

public class TfsPersonTag extends BaseTag implements I_TfsPerson  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6002528017252015868L;
	/**
	 * 
	 */
	protected static final Log LOG = CmsLog.getLog(TfsPersonTag.class);
	private Persons person = null;
	private String personid="";
	
	private TfsPersona previousPersona = null;
	private PersonsDAO personadao=new PersonsDAO();
	
	public TfsPersonTag()
	{
		person=null;
		
	}

    @Override
	public int doStartTag() throws JspException {
    	
    	super.init();
    	
	    savePersona();
	    
	    if (previousPersona!=null){ 
    		personid= Long.toString( previousPersona.getId_person());
	    }

    	if (personid==null || personid.trim().equals(""))
    	{
    		
    		personid="0";
    		
    		
    	} 
			try {
				person =  personadao.getPersonaById(new Long(personid));//m_cms.readUser(personname);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOG.error("Error al obtener parona con id "+personid+" "+ e.getMessage());
			}
    	
    	exposePersona(person);
    	
		return EVAL_BODY_INCLUDE; //SKIP_BODY;		

    }

	public String getPersonid() {
		return personid;
	}

	public void setPersonid(String personid) {
		this.personid = personid;
	}

	public Persons getPerson() {
		
		return person;
		
	}
    
	
    protected void exposePersona(Persons person)
    {
    	TfsPersona tfsPersona = new TfsPersona(person,m_cms);
		pageContext.getRequest().setAttribute("persona", tfsPersona);

    }
    
    protected void restorePersona()
    {
    	pageContext.getRequest().setAttribute("persona", previousPersona );
    }

	protected void savePersona()
    {
		previousPersona = (TfsPersona) pageContext.getRequest().getAttribute("persona");
    	pageContext.getRequest().setAttribute("persona",null);
    }

	@Override
	public int doEndTag() throws JspException {
		restorePersona();
		
		return super.doEndTag();
	}

	

}
