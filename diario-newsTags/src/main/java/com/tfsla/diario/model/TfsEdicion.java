package com.tfsla.diario.model;

import java.util.Date;

import org.opencms.file.CmsObject;

import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.services.EdicionService;

public class TfsEdicion {

	private int number=0;
	private int publicationid=0;
	private Date date = new Date();
	private String title="";
	private String firstpage="";
	private String logo="";

	public static TfsEdicion EMPTY = new TfsEdicion();
	
	private TfsEdicion(){}
	
	public TfsEdicion(int tipoEdicion, int nroEdicion)
	{
		EdicionService eService = new EdicionService();
		
		try {
			Edicion edicion = eService.obtenerEdicion(tipoEdicion,nroEdicion);
			setEditionData(edicion);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public TfsEdicion(CmsObject cmsObject, String path)
	{
		EdicionService eService = new EdicionService();
		
		try {
			Edicion edicion = eService.obtenerEdicionImpresa(cmsObject, path);
			setEditionData(edicion);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public TfsEdicion(Edicion edicion)
	{
		setEditionData(edicion);
	}
	
	private void setEditionData(Edicion edicion)
	{
		number = edicion.getNumero();
		publicationid = edicion.getTipo();
		date = edicion.getFecha();
		title = edicion.getTituloTapa();
		firstpage = edicion.getPortada();
		logo = edicion.getLogo();
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFirstpage() {
		return firstpage;
	}

	public void setFirstpage(String firstpage) {
		this.firstpage = firstpage;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public int getPublicationid() {
		return publicationid;
	}

	public void setPublicationid(int publicationid) {
		this.publicationid = publicationid;
	}
}
