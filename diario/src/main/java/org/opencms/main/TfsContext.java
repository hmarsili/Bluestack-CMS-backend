package org.opencms.main;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.opencms.db.CmsDefaultUsers;
import org.opencms.file.CmsObject;
import org.opencms.flex.CmsFlexController;
//import org.opencms.main.OpenCmsCore;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.ThreadAccesor;
//import org.opencms.site.CmsSiteManager;
import org.opencms.workplace.CmsWorkplaceAction;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.opencmsdev.module.pages.PageConfiguration;
import com.tfsla.opencmsdev.module.pages.Project;
import com.tfsla.utils.TfsAdminUserProvider;

public class TfsContext {
	

	//private ThreadLocal<CmsObject> threadLocal = new ThreadLocal<CmsObject>();
	private ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>();
	private ThreadLocal<HttpServletResponse> response = new ThreadLocal<HttpServletResponse>();
	private ThreadLocal<CmsObject> threadCmsObject = new ThreadLocal<CmsObject>();
	
	/** parametro configurado en el web.xml para que usen los colectors */
	private Integer timeBefore;
	private boolean purge;

	private static TfsContext instance;

	public static synchronized TfsContext getInstance() {
		if (TfsContext.instance == null) {
			TfsContext.instance = new TfsContext();
		}
		return TfsContext.instance;
	}

	private TfsContext() {
		super();
	}

	public CmsObject getCmsObject() {
		HttpServletRequest req = this.getRequest();
		CmsObject object = null;
		
		if(req != null) { 
			object = (CmsObject) req.getAttribute("tfsCmsObject");
		}
		if (object == null) {
			object = searchCmsObject();
			if(req != null) {
				req.setAttribute("tfsCmsObject", object);
			}
		}
		return object;
	}


	public HttpServletRequest getRequest() {
		return this.request.get();
	}

	public void setRequest(HttpServletRequest request) {
		this.request.set(request);
	}

	public HttpServletResponse getResponse() {
		return this.response.get();
	}

	public void setResponse(HttpServletResponse response) {
		this.response.set(response);
	}

	// **********************************************
	// ** Metodos para buscar el cmsObject en los threads que no vienen del servlet directamente
	// **********************************************

	private CmsObject searchCmsObject() {
		CmsObject cmsObject;
		if(this.getRequest() != null && this.getResponse()!= null) {
			cmsObject = CmsFlexController.getCmsObject(this.getRequest());
			if (cmsObject != null) {
				return cmsObject;
			}
			else {
				return this.hackOpencmsCore(this.getRequest(), this.getResponse());
			}
		}
		else {
		cmsObject = this.threadCmsObject.get();
		if(cmsObject != null) {
			return cmsObject;
		}
		Thread thread = Thread.currentThread();
		if (thread instanceof A_CmsReportThread) {
			return ThreadAccesor.getCmsObject((A_CmsReportThread) thread);
		}
		else {
			cmsObject = searchCmsObjectInFields(thread);
			if (cmsObject != null) {
				return cmsObject;
			}
			else {
				return this.searchCmsObjectInMethods(thread);
			}
		}
		}

	}

	private CmsObject hackOpencmsCore(HttpServletRequest request, HttpServletResponse response) {
			OpenCmsCore core = OpenCmsCore.getInstance();
			try {
				
/*				CmsObject cmsObject = OpenCms.initCmsObject(new
						CmsDefaultUsers().getUserGuest());

				cmsObject.loginUser(TfsAdminUserProvider.getInstance().getUserName(), TfsAdminUserProvider.getInstance().getPassword());

				return OpenCms.initCmsObject(cmsObject);
*/				
				
				Method initCms = OpenCmsCore.class.getDeclaredMethod("initCmsObject", new Class[] {
						HttpServletRequest.class, HttpServletResponse.class });
				initCms.setAccessible(true);
				return  (CmsObject) initCms.invoke(core, new Object[] { request, response });
				
				
			}
			catch (Exception e) {
				
				CmsObject cmsObject;
				try {
					CmsObject _cmsObject =CmsWorkplaceAction.getInstance().getCmsAdminObject();
					cmsObject = OpenCms.initCmsObject(_cmsObject);
					
					return OpenCms.initCmsObject(cmsObject);
					
				} catch (CmsException e1) {
					throw new ApplicationException("no se pudo conseguir el opencmsObject", e);
				}


			}
	}

	private CmsObject searchCmsObjectInMethods(Object thread) {
		Method[] methods = thread.getClass().getDeclaredMethods();
		for (Method method : methods) {
			String current_class = method.getDeclaringClass().getName();
			if(current_class==null){}
			
			if (method.getDeclaringClass().isAssignableFrom(CmsObject.class)
					&& method.getParameterTypes().length == 0) {
				return getCmsObject(thread, method);
			}
		}
		return null;
	}

	private CmsObject searchCmsObjectInFields(Thread thread) {
		Field[] fields = thread.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getDeclaringClass().isAssignableFrom(CmsObject.class)) {
				return getCmsObject(thread, field);
			}
		}
		return null;
	}

	private CmsObject getCmsObject(Object thread, Method method) {
		method.setAccessible(true);
		try {
			return (CmsObject) method.invoke(thread);
		}
		catch (IllegalArgumentException e) {
			throw new ApplicationException("", e);
		}
		catch (IllegalAccessException e) {
			throw new ApplicationException("", e);
		}
		catch (InvocationTargetException e) {
			throw new ApplicationException("", e);
		}
	}

	private CmsObject getCmsObject(Object thread, Field field) {
		field.setAccessible(true);
		try {
			CmsObject object = (CmsObject) field.get(thread);
			return object;
		}
		catch (IllegalArgumentException e) {
			throw new ApplicationException("no se pudo obtener el cmsObject", e);
		}
		catch (IllegalAccessException e) {
			throw new ApplicationException("no se pudo obtener el cmsObject", e);
		}
	}

	public void putObjectInSession(String key, Object value) {
		this.getSession().setAttribute(key, value);
	}

	private HttpSession getSession() {
		return this.getRequest().getSession();
	}

	public Object getObjectInSession(String key) {
		return this.getSession().getAttribute(key);
	}

	public Project getProject() {
		String name = getProjectName();
		Project project = PageConfiguration.getInstance().getProjectByName(name);
		ApplicationException.assertNotNull(project, "No existe el projecto con el nombre: " + name
				+ ", aseg�rese de elegir el sitio correcto en el combo de sitios");
		return project;
	}

	private String getProjectName() {
		String name = OpenCms.getSiteManager().getCurrentSite(this.getCmsObject()).getTitle();
		name = name.replace("/sites/", "");
		name = name.replace("/", "");
		return name;
	}

	public void setTimeBefore(Integer timeBefore) {
		this.timeBefore = timeBefore;
	}

	public Integer getTimeBefore() {
		return this.timeBefore;
	}

	public void setPurge(boolean purge) {
		this.purge = purge;
	}

	public boolean isPurge() {
		return this.purge;
	}

	/**
	 * Un thread que usa este metodo debe usar el remove despues
	 * @param cms
	 */
	public void setCmsObject(CmsObject cms) {
		this.threadCmsObject.set(cms);
	}

	public void removeCmsObject() {
		this.threadCmsObject.set(null);
	}
	
}