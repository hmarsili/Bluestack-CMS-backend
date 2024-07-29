package com.tfsla.diario.model;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.opencmsdev.module.TfsConstants;

public class TfsNotaEnPortada {
	private String publication;
	private String section;
	private String sectionzone;
	private String homezone;
	private int homepriority;
	private int sectionpriority;
	private TipoEdicion tipoedicion;
	
	private boolean dontshowonhome;
	private boolean dontshowonsection;
	
	public TfsNotaEnPortada(
			TipoEdicion publication,
			String section,
			String sectionzone,
			String homezone,
			int homepriority,
			int sectionpriority ) {
		
		this.publication = publication.getNombre();
		this.tipoedicion = publication;
		this.section = section;
		this.sectionzone = sectionzone;
		this.homezone = homezone;
		this.homepriority = homepriority;
		this.sectionpriority = sectionpriority;
		
		dontshowonhome = homezone.equals(TfsConstants.NO_MOSTRAR_VALUE);
		dontshowonsection = sectionzone.equals(TfsConstants.NO_MOSTRAR_VALUE);
			
	}
	
	public String getPublication() {
		return publication;
	}
	public void setPublication(String publication) {
		this.publication = publication;
	}
	public String getSection() {
		return section;
	}
	public void setSection(String section) {
		this.section = section;
	}
	public String getSectionzone() {
		return sectionzone;
	}
	public void setSectionzone(String sectionzone) {
		this.sectionzone = sectionzone;
	}
	public String getHomezone() {
		return homezone;
	}
	public void setHomezone(String homezone) {
		this.homezone = homezone;
	}
	public int getHomepriority() {
		return homepriority;
	}
	public void setHomepriority(int homepriority) {
		this.homepriority = homepriority;
	}
	public int getSectionpriority() {
		return sectionpriority;
	}
	public void setSectionpriority(int sectionpriority) {
		this.sectionpriority = sectionpriority;
	}
	public boolean isDontshowonhome() {
		return dontshowonhome;
	}
	public void setDontshowonhome(boolean dontshowonhome) {
		this.dontshowonhome = dontshowonhome;
	}
	public boolean isDontshowonsection() {
		return dontshowonsection;
	}
	public void setDontshowonsection(boolean dontshowonsection) {
		this.dontshowonsection = dontshowonsection;
	}

	public TipoEdicion getTipoedicion() {
		return tipoedicion;
	}

	public void setTipoedicion(TipoEdicion tipoedicion) {
		this.tipoedicion = tipoedicion;
	}
	
}
