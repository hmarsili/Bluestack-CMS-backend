package com.tfsla.diario.model;

import org.opencms.file.CmsObject;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class TfsPublicacion {
	private int id;
	private String name;
	private String description;

	private boolean isonline;
	private String basepath;
	
	private int	activeedition;
	
	private String newsindex;
	private String imageindex;
	private String videoindex;
	
	private String newsindexoffline;
	private String imageindexoffline;
	private String videoindexoffline;
	
	public TfsPublicacion(TipoEdicion tEdicion)
	{
		setPublicationData(tEdicion);
	}
	
	public TfsPublicacion(int id)
	{
		TipoEdicionService tEService = new TipoEdicionService();
		TipoEdicion tEdicion = tEService.obtenerTipoEdicion(id);
		setPublicationData(tEdicion);
	}
	
	public TfsPublicacion(CmsObject cmsObject, String path)
	{
		TipoEdicionService tEService = new TipoEdicionService();
		try {
			TipoEdicion tEdicion = tEService.obtenerTipoEdicion(cmsObject, path);
			
			setPublicationData(tEdicion);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setPublicationData(TipoEdicion tEdicion)
	{
		id = tEdicion.getId();
		name = tEdicion.getNombre();
		description = tEdicion.getDescripcion();
		
		isonline = tEdicion.isOnline();
		basepath = tEdicion.getBaseURL();
		
		activeedition = tEdicion.getEdicionActiva();
		
		newsindex = tEdicion.getNoticiasIndex();
		imageindex = tEdicion.getImagenesIndex();
		videoindex = tEdicion.getVideosIndex();
		
		newsindexoffline = tEdicion.getNoticiasIndexOffline();
		imageindexoffline = tEdicion.getImagenesIndexOffline();
		videoindexoffline = tEdicion.getVideosIndexOffline();
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isIsonline() {
		return isonline;
	}
	public void setIsonline(boolean isonline) {
		this.isonline = isonline;
	}
	public String getBasepath() {
		return basepath;
	}
	public void setBasepath(String basepath) {
		this.basepath = basepath;
	}
	public int getActiveedition() {
		return activeedition;
	}
	public void setActiveedition(int activeedition) {
		this.activeedition = activeedition;
	}
	public String getNewsindex() {
		return newsindex;
	}
	public void setNewsindex(String newsindex) {
		this.newsindex = newsindex;
	}
	public String getImageindex() {
		return imageindex;
	}
	public void setImageindex(String imageindex) {
		this.imageindex = imageindex;
	}
	public String getVideoindex() {
		return videoindex;
	}
	public void setVideoindex(String videoindex) {
		this.videoindex = videoindex;
	}

	public String getNewsindexoffline() {
		return newsindexoffline;
	}

	public void setNewsindexoffline(String newsindexoffline) {
		this.newsindexoffline = newsindexoffline;
	}

	public String getImageindexoffline() {
		return imageindexoffline;
	}

	public void setImageindexoffline(String imageindexoffline) {
		this.imageindexoffline = imageindexoffline;
	}

	public String getVideoindexoffline() {
		return videoindexoffline;
	}

	public void setVideoindexoffline(String videoindexoffline) {
		this.videoindexoffline = videoindexoffline;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	

}
