package com.tfsla.opencmsdev.encuestas;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;

public class GetEncuestasProperties extends AbstractEncuestaProcess{

	private String TiempoXIP;
	private Integer CantVoto;
	private Integer CantVotosxUsuario;
	private Integer pageSizeAdminList;
	private String PathCategorias;
	private String SubPathCategorias;
	private boolean useCaptcha;
	private List<String> grupos;
	
	private GetEncuestasProperties(String siteName, String publication) {
		this.loadProperties(siteName,publication);
	}
	
    private static Map<String, GetEncuestasProperties> instances = new HashMap<String, GetEncuestasProperties>();

	public synchronized static GetEncuestasProperties getInstance(String siteName, String publication) {
		
    	String id = siteName + "||" + publication;
    	
    	GetEncuestasProperties instance = instances.get(id);
    	
    	if (instance == null) {
	    	instance = new GetEncuestasProperties(siteName, publication);

	    	instances.put(id, instance);
    	}
        return instance;

	}

	public synchronized static GetEncuestasProperties getInstance(CmsObject cms) {
		
    	String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	String publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}

    	return getInstance(siteName, publication);
    	
	}
	
	public void loadProperties(String siteName, String publication) {
		
    	String module = "polls";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

 		PathCategorias = config.getParam(siteName, publication, module, "pathCategorias", "categorias");
 		CantVoto = config.getIntegerParam(siteName, publication, module, "votosXIP",1);
 		TiempoXIP = config.getParam(siteName, publication, module, "TiempoXIP","10");
 		CantVotosxUsuario =  config.getIntegerParam(siteName, publication, module, "VotosxUsuario",1);
 		pageSizeAdminList =  config.getIntegerParam(siteName, publication, module, "pageSizeAdmin",20);
		
 		SubPathCategorias = config.getParam(siteName, publication, module, "subPathCategorias", "/");
  
 		useCaptcha = config.getBooleanParam(siteName, publication, module, "useCaptcha", false);
 		
 		grupos = config.getParamList(siteName, publication, module, "groups");

	}
	
	public int getCantVotosXIP() {
		return CantVoto;
	}

	public void setCantVotosXIP(int VotosxIP) {
		this.CantVoto = VotosxIP;
	}
	
	public String getTiempXIP() {
		return TiempoXIP;
	}

	public void setTiempoXIP(String TiempoxIP) {
		this.TiempoXIP = TiempoxIP;
	}
	
	public void setCantVotosxUsuario(int VotosxUsuario){
		this.CantVotosxUsuario = VotosxUsuario;
	}
	
	public int getCantVotosxUsuario(){
		return CantVotosxUsuario;
	}
	
	public int getPageSizeAdmin(){
		return pageSizeAdminList;
	}
	
	public String getCategoriesPath() {
		return PathCategorias;
	}

	public void setCategoriesPath(String PathCategorias) {
		this.PathCategorias = PathCategorias;
	}
	
	public String getCategoriesSubPath() {
		return SubPathCategorias;
	}

	public void setCategoriesSubPath(String SubPathCategorias) {
		this.SubPathCategorias = SubPathCategorias;
	}
	
	public boolean isUseCaptcha() {
		return useCaptcha;
	}

	public void setUseCaptcha(boolean useCaptcha) {
		this.useCaptcha = useCaptcha;
	}

	public List<String> getGrupos() {
		return grupos;
	}

	public void setGrupos(List<String> grupos) {
		this.grupos = grupos;
	}
}
