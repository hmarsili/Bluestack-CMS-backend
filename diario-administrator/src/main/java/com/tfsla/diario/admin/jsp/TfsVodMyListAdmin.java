package com.tfsla.diario.admin.jsp;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceManager;
import org.opencms.workplace.CmsWorkplaceSettings;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.vod.data.VodMyListDAO;
import com.tfsla.vod.model.TfsVodMyList;
import com.tfsla.vod.model.TfsVodNews;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TfsVodMyListAdmin {

	 protected static final Log LOG = CmsLog.getLog(TfsVodAdmin.class);
	 
	 private CmsFlexController m_controller;
	 private HttpSession m_session;
	private HttpServletRequest request;
	
	/**  The currently used message bundle. */
	private CmsMultiMessages m_messages;
	
	/** The current OpenCms users workplace settings. */
	private CmsWorkplaceSettings m_settings;
	
	private List<CmsResource> resources = new ArrayList<CmsResource>();
	 
	private String siteName;
	private TipoEdicion currentPublication;
	private String publication;
	private String moduleConfigName;
	private CPMConfig config;
	private String refereceDays="3";

    public String getSiteName() {
    	return this.siteName;
    }
    
    public String getPublication() {
    	return this.publication;
    }
	    
    public CmsObject getCmsObject() {
        return m_controller.getCmsObject();
    }

    public TfsVodMyListAdmin (PageContext context, HttpServletRequest req, HttpServletResponse res) throws Exception {
		m_controller = CmsFlexController.getController(req);
	    request = req;
	    m_session = req.getSession();
	    
		siteName = OpenCms.getSiteManager().getCurrentSite(getCmsObject()).getSiteRoot();
		
		currentPublication = (TipoEdicion) m_session.getAttribute("currentPublication");
	
		if (currentPublication==null) {
	    	TipoEdicionService tService = new TipoEdicionService();
	
			currentPublication = tService.obtenerEdicionOnlineRoot(siteName);
			m_session.setAttribute("currentPublication",currentPublication);
		}
		
	    m_settings = (CmsWorkplaceSettings)m_session.getAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS);
	
	    if (m_settings==null)  {
	    	
	    	if (LOG.isDebugEnabled()) {
	    		LOG.debug("Editing the content " + req.getParameter("url")  + " in resource list. Session settings not found. Starting context-based configuration.");
	    	}        	
	    	m_settings = CmsWorkplace.initWorkplaceSettings( m_controller.getCmsObject(), m_settings, true);
	    	m_session.setAttribute(CmsWorkplaceManager.SESSION_WORKPLACE_SETTINGS, m_settings);
	    	
	    	if (LOG.isDebugEnabled()) {
	    		LOG.debug("Current proyect " + m_settings.getProject() + " - Current site" + m_settings.getSite());
	    	}
	    }
	
	    // initialize messages            
	    CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(getLocale());
	    // generate a new multi messages object and add the messages from the workplace
	    m_messages = new CmsMultiMessages(getLocale());
	    m_messages.addMessages(messages);
	    
	    if (currentPublication != null)
	    	publication = "" + currentPublication.getId();
		moduleConfigName = "vod";
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		refereceDays = config.getParam(siteName, publication, moduleConfigName, "myListDays", "3");
	}
		
	public Locale getLocale() {
		return m_settings.getUserSettings().getLocale();
	}
		 
	
	/*agrega un nuevo registro a la lista*/
	public JSONObject addVodToMyList (String source, String userID) {
		VodMyListDAO myListDAO = new VodMyListDAO();
		JSONObject result = new JSONObject();
		
		
		
		TfsVodMyList element = new TfsVodMyList();
		element.setFecha(new Timestamp((new Date()).getTime()));
		element.setSource(source);
		element.setUserId(userID);
		try {
			myListDAO.insertSource(element);
			result.put("SUCCESS", "Se agrego el registro correctamente");
			
		} catch (Exception e) {
			LOG.error("Error al insertar registro en mi lista", e);
			result.put("ERROR", "Error al borrar registro");
			
		}
		return result;
	}
	
	
	
	
	/*elimina un registro de la lista*/
	public JSONObject removeVodToMyList (String source, String userID) {
		VodMyListDAO myListDAO = new VodMyListDAO();
		
		JSONObject result = new JSONObject();
		
		TfsVodMyList element = new TfsVodMyList();
		element.setSource(source);
		element.setUserId(userID);
		try {
			myListDAO.deleteSource(element);
			result.put("SUCCESS", "Se elimio el registro correctamente");
		} catch (Exception e) {
			LOG.error("Error al borrar  registro de mi lista", e);
			result.put("ERROR", "Error al borrar registro");
		}
		return result;
	}
	
	
	public JSONObject checkMyListModification (String userID) {
		if (refereceDays.equals(""))
			refereceDays = "10";
		VodMyListDAO myListDAO = new VodMyListDAO(refereceDays);
		boolean modified = false;
		try {
			modified = myListDAO.getSourceModification(userID);
		} catch (Exception e) {
			LOG.error("Error al buscar modificaciones",e);
		}
		if (modified) {
			return getMyListbyUser(userID, "fecha desc");
		} 
		JSONObject listado = new JSONObject();
		listado.put("NO-CHANGE", "No se actualiza la informacion del usuario");
		return listado;
		
	}
	
	/*entrega el listado de elementos de la tabla MyList */
	public JSONObject getMyList (Map<String, String> parameters) {
		JSONObject listado = new JSONObject();
	
		VodMyListDAO myListDAO = new VodMyListDAO();
		
		List<TfsVodMyList> lista = null;
		try {
			 lista = myListDAO.getList(parameters);
		} catch (Exception e) {
			LOG.error("Error al buscar el listado de mylist", e);
			return listado;
		}
		
	
		try {
			for (TfsVodMyList tfsVodMyList : lista) {
				CmsUser user = null;
				try {
					 user = getCmsObject().readUser(new CmsUUID(tfsVodMyList.getUserId()));
								
				} catch (Exception e) {
					LOG.error("No puede leer el usuario:", e);
				}
				if (user != null) {
					if (listado.get("users") != null ) {
						JSONArray users = listado.getJSONArray("users");
						boolean exists = false;
						for (int i = 0; i < users.size(); i++) {
							if (((JSONObject)users.get(i)).get("user").equals(user.getName())) {
								JSONObject element = new  JSONObject();
								element.put("source", tfsVodMyList.getSource());
								element.put("date", tfsVodMyList.getFecha().getTime());
								JSONArray arraySources = (JSONArray) ((JSONObject)users.get(i)).get("sources");
								arraySources.add(element);
								exists = true;
								break;
							} 	
						}
						if (!exists) {
							JSONObject userObject = new  JSONObject();
							userObject.put("user", user.getName());
							//agrego el source y la fecha
							JSONObject element = new  JSONObject();
							element.put("source", tfsVodMyList.getSource());
							element.put("date", tfsVodMyList.getFecha().getTime());
							
							JSONArray arraySources = new JSONArray();
							arraySources.add(element);
							userObject.put("sources",arraySources);
							
							users.add(userObject);
						}
					} else {
						//agrego el usuario
						JSONObject userObject = new  JSONObject();
						userObject.put("user", user.getName());
						//agrego el source y la fecha
						JSONObject element = new  JSONObject();
						element.put("source", tfsVodMyList.getSource());
						element.put("date", tfsVodMyList.getFecha().getTime());
						
						JSONArray arraySources = new JSONArray();
						arraySources.add(element);
						userObject.put("sources",arraySources);
						
						JSONArray users = new JSONArray();
						users.add(userObject);
						
						listado.put("users", users);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Error al armar el json del listado de mylist", e);
		}
		return listado;
	}
	
	
	/*entrega el listado de elementos de la tabla MyList */
	public JSONObject getMyListbyUser (String user, String order) {
		JSONObject listado = new JSONObject();
	
		VodMyListDAO myListDAO = new VodMyListDAO();
		
		List<TfsVodMyList> lista = null;
		try {
			 lista = myListDAO.getListByUserAndOrder(user, order);
		} catch (Exception e) {
			LOG.error("Error al buscar el listado de mylist", e);
		}
		
		try {
			for (TfsVodMyList tfsVodMyList : lista) {
				if (listado.has("myList")) {
					JSONArray arrayUser = listado.getJSONArray("myList");
					JSONObject element = new  JSONObject();
					element.put("source", tfsVodMyList.getSource());
					element.put("date", tfsVodMyList.getFecha().getTime());
					arrayUser.add(element);
				} else {
					JSONArray arrayUser = new JSONArray();
					JSONObject element = new  JSONObject();
					element.put("source", tfsVodMyList.getSource());
					element.put("date", tfsVodMyList.getFecha().getTime());
					arrayUser.add(element);
					listado.put("myList", arrayUser);
				}
			}
			listado.put("lastModified", (new Date()).getTime());
			
		} catch (Exception e) {
			LOG.error("Error al armar el json del listado de mylist", e);
		}
		return listado;
	}
	
	/*evalua si hay que volver a cargar el archivo con novedades*/
	public JSONObject checkNewsChanged(String order) {
		VodMyListDAO myListDAO = new VodMyListDAO();
		JSONObject listado = new JSONObject();
		
		
		try {
			if ( myListDAO.getNewsChanged()) {
				return getNews (order);
			} else {
				listado.put("NO-CHANGE", "No se encontraron novedades");
				return listado;
			}
		} catch (Exception e) {
			LOG.error("Errror al buscar novedades",e);
			listado.put("ERROR", "Error al buscar novedades");
			return listado; 
		}
		
	}
	
	
	/*entrega el listado de elementos de la tabla MyList */
	public JSONObject getNews (String order) {
		JSONObject listado = new JSONObject();
	
		VodMyListDAO myListDAO = new VodMyListDAO();
		
		List<TfsVodNews> lista = null;
		try {
			 lista = myListDAO.getNews( order);
		} catch (Exception e) {
			LOG.error("Error al buscar el listado de novedades", e);
		}
		
		try {
			for (TfsVodNews tfsVodNews : lista) {
				if (listado.get("news") != null && listado.get("news").toString().indexOf(tfsVodNews.getSourceParent())>-1) {
					JSONArray arraySource = listado.getJSONArray("news");
					JSONObject element = new  JSONObject();
					element.put("sourceParent", tfsVodNews.getSourceParent());
					element.put("source", tfsVodNews.getSource());
					element.put("date", tfsVodNews.getFecha().getTime());
					element.put("date-publication", tfsVodNews.getFechaPublicacion().getTime());
					element.put("date-availability",  tfsVodNews.getDisponibility()==null? "":  tfsVodNews.getDisponibility().getTime());
					element.put("description", tfsVodNews.getDescripcion());
					arraySource.add(element);
				} else {
					JSONArray arraySource = new JSONArray();
					JSONObject element = new  JSONObject();
					element.put("sourceParent", tfsVodNews.getSourceParent());
					element.put("source", tfsVodNews.getSource());
					element.put("date", tfsVodNews.getFecha().getTime());
					element.put("date-publication", tfsVodNews.getFechaPublicacion().getTime());
					element.put("date-availability", tfsVodNews.getDisponibility()==null? "":  tfsVodNews.getDisponibility().getTime());
					element.put("description", tfsVodNews.getDescripcion());
					arraySource.add(element);
					listado.put("news", arraySource);
				}
			}
			listado.put("lastModified", (new Date()).getTime());
		} catch (Exception e) {
			LOG.error("Error al armar el json del listado de novedades", e);
		}
		return listado;
	}
	

}

