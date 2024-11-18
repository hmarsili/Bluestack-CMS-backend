package com.tfsla.diario.friendlyTags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.tagext.DynamicAttributes;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;


import com.tfsla.diario.model.TfsListaUsuarios;
import com.tfsla.diario.model.TfsUsuario;
import com.tfsla.diario.usersCollector.DataUsersCollector;


public class TfsUserListTag extends BaseTag implements I_TfsUser,I_TfsCollectionListTag, DynamicAttributes{

	private static final long serialVersionUID = 6526211498343816619L;
	private static final Log LOG = CmsLog.getLog(TfsUserListTag.class);
	
	TfsUsuario usuario = null;
	TfsUsuario previousUser = null;
	TfsListaUsuarios previousListaUsuarios = null;
	public static final String param_ou = "ou";
	public static final String param_group="group";
	public static final String param_state="state";
	public static final String param_filter="filter";
	public static final String param_order="order";
	public static final String param_size="size";
	public static final String param_page="page";
	public static final String param_from="from";
	public static final String param_to="to";
	public static final String param_age="age";
	//public static final String param_userInfo="user-info";
	public static final String param_numberOfParamters = "params-count";
	
	private String ou = null;
	private String group= null;
	private String state=null;
	private String filter=null;
	private String order=null;
	private int size=0;
	private int page=1;
	private String from=null;
	private String to=null;
	private String age = null;
	//private String userinfo=null;
	
	private Map<String,Object> atributosdinamicos = new HashMap<String,Object>();
	
	public String getOu() {
		return ou;
	}
	public void setOu(String ou) {
		this.ou = ou;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getState() {
		return state;
	}

	public void setState(String state) {
		if (state==null || state.trim().length()==0)
			this.state = null;
		else
			this.state = state;
	}
	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		if (filter==null || filter.trim().length()==0)
			this.filter = null;
		else
			this.filter = filter;
	}
	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		if (order==null || order.trim().length()==0)
			this.order = null;
		else
			this.order = order;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public int getPage() {
		return page;
	}
	
	public void setPage(int page) {
			this.page = page;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		if (age==null || age.trim().length()==0)
			this.age = null;
		else
			this.age = age;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	/*public String getUserinfo() {
		return userinfo;
	}
	public void setUserinfo(String userinfo) {
		this.userinfo = userinfo;
	}*/

	List<CmsUser> users = null;
	
	int index=-1;

	public void setDynamicAttribute(String arg0, String clave, Object valor)throws JspException {
		try {
			if (validar(clave,valor)){
			atributosdinamicos.put(clave, valor);
			
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
			
		}
		
	}
	
	private boolean validar(String clave, Object valor){
		boolean rdo= false;
		String siteName = OpenCms.getSiteManager().getCurrentSite(CmsFlexController.getCmsObject(pageContext.getRequest())).getSiteRoot();
    
    	TipoEdicionService tService = new TipoEdicionService();

    	TipoEdicion currentPublication=null;
    	try {
			currentPublication = tService.obtenerEdicionOnlineRoot(siteName.substring(siteName.lastIndexOf("/")+1));
		} catch (Exception e) {
					LOG.error("al intentar traer la publicacion actual "+e.getCause());
		}
       	String publication = "" + (currentPublication!=null?currentPublication.getId():"");
    	String moduleConfigName = "webusers";
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		List<String>listaPermitidos=config.getParamList(siteName, publication, moduleConfigName, "additionalInfo");
		if ((clave!=null)&&(valor!=null)){
			if (listaPermitidos.contains(clave)){
				rdo=true;
			}
		}
		return rdo;
	}
	
	@Override
	public int doStartTag() throws JspException {
		
		//determinar si el typepedata se trata de datos si se trata de ranking de usuarios
		if (order==null){
			findUsers("opencms");
		}
		else{
			if (order.toLowerCase().contains("most-")){
				findUsers("stats");
			}else{
				findUsers("opencms");	
			}
		}
		
		return (hasMoreContent() ? EVAL_BODY_INCLUDE : SKIP_BODY );		
	}

	protected void findUsers(String typedata) {
		users = null;
		index=-1;
		Map<String,Object> parameters = createParameterMap();
	    CmsObject cms = CmsFlexController.getCmsObject(pageContext.getRequest());
	    saveUser();
	    if (!typedata.equals("stats")){
	   
		//if(typedata=="opencms"){
			DataUsersCollector collector= new DataUsersCollector();
			users = collector.collectUsers(parameters, cms, true);
		//}
		//if (typedata=="stats"){
		//	RankingUsersCollector collector=new RankingUsersCollector();
		//	users=collector.collectUsers(parameters, cms);
		//}
	    }
	}
	
	protected Map<String,Object> createParameterMap() {
		Map<String,Object> parameters = new HashMap<String,Object>();
		
		parameters.put(param_ou, ou);
		parameters.put(param_group,group);
		parameters.put(param_state,state);
		parameters.put(param_filter,filter);
		parameters.put(param_order,order);
		parameters.put(param_size,size);
		parameters.put(param_page, page);
		parameters.put(param_age,age);
		parameters.put(param_to,to);
		parameters.put(param_from,from);
		//meto los valores adicionales
		int cuenta=0;
		for(String key : atributosdinamicos.keySet()){
			parameters.put("param_userinfo_"+key, atributosdinamicos.get(key));
			cuenta++;
		}
		int paramsWithValues = 
			(ou!=null ? 1 : 0) +
			(group!=null ? 1 : 0) +
			(state!=null ? 1 : 0) +
			(filter!=null ? 1 : 0) +
			(order!=null ? 1 : 0) +
			1  + //size
			1  + //page
			(age!=null ? 1 : 0) +
			(to!=null ? 1 : 0)+
			(from!=null ? 1 : 0);
		parameters.put(param_numberOfParamters,(Integer)paramsWithValues+cuenta);
		return parameters;
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
		return EVAL_PAGE;
	}
	
	protected boolean hasMoreContent() throws JspException {
		if (users==null)
			return false;
		
		index++;
		
		if (index<users.size())	{	
			exposeUser(users.get(index));
		}
		else
			restoreUser();

		return (index<users.size());
	}
	
	protected void exposeUser(CmsUser user) {
	 	TfsUsuario usuario = new TfsUsuario(user,CmsFlexController.getCmsObject(pageContext.getRequest()));
		TfsListaUsuarios lista = new TfsListaUsuarios(this.users.size(),this.index+1,this.size,this.page);
		pageContext.getRequest().setAttribute("userslist", lista);
		pageContext.getRequest().setAttribute("ntuser", usuario);
	}
	    
	protected void restoreUser() {
    	pageContext.getRequest().setAttribute("ntuser", previousUser );
    	pageContext.getRequest().setAttribute("userslist", previousListaUsuarios);
    }

	protected void saveUser() {
       	previousListaUsuarios = (TfsListaUsuarios) pageContext.getRequest().getAttribute("userslist");
    	previousUser  = (TfsUsuario) pageContext.getRequest().getAttribute("ntuser");
    	pageContext.getRequest().setAttribute("userslist",null);
    	pageContext.getRequest().setAttribute("ntuser",null);
    }
		
	public int getIndex() {
		return index;
	}

	public boolean isLast() {
		return (index==users.size()-1);
	}
	
	public CmsUser getUser() {
		return users.get(index);
	}
	
	public String getCollectionPathName() throws JspTagException {
		return null;
	}
	
	public String getCollectionValue(String name) throws JspTagException {
		return null;
	}
	
	public String getCollectionIndexValue(String name, int index) throws JspTagException {
		return null;
	}
	
	public int getCollectionIndexSize(String name, boolean isCollectionPart) throws JspTagException {
		return 0;
	}
}
