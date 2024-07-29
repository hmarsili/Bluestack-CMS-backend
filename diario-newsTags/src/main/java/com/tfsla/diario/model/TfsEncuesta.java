package com.tfsla.diario.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tfsla.opencmsdev.encuestas.Encuesta;
import com.tfsla.opencmsdev.encuestas.ResultadoEncuestaBean;

public class TfsEncuesta {
	
	private Encuesta encuesta;
	private ResultadoEncuestaBean resultados;
	private TfsEncuestaOpcion option;
	private TfsEncuestaOpcion optionImage;
	
	public TfsEncuesta()
	{
	}
	
	public TfsEncuesta(Encuesta encuesta, ResultadoEncuestaBean resultados)
	{
		this.encuesta = encuesta;
		this.resultados = resultados;
	}
	
	public String getUrl()
	{
		return encuesta!=null ? encuesta.getEncuestaURL() : "undefined";
	}
	
	public String getGroup(){
		return encuesta!=null ? encuesta.getGrupo() : "";
	}
	public String getDescription(){
		return encuesta!=null ? encuesta.getTextoAclaratorio() : "";
	}
	public String getQuestion(){
		return encuesta!=null ? encuesta.getPregunta() : "";
	}
	
	public Date getClosedate(){
		if (encuesta==null)
			return new Date(Long.MIN_VALUE);
		Date date = new Date(Long.parseLong(encuesta.getFechaCierre()));
		return date;
	}
	public Date getCreationdate(){
		if (encuesta==null)
			return new Date(Long.MIN_VALUE);
		Date date = new Date(Long.parseLong(encuesta.getFechaCreacion()));
		return date;
	}
	public Date getPublicationdate(){
		if (encuesta==null)
			return new Date(Long.MIN_VALUE);
		Date date = new Date(Long.parseLong(encuesta.getFechaPublicacion()));
		return date;
	}
	
	public int getOptionsnumber(){
		return encuesta!=null ? encuesta.getCantRespuestasOriginal() : 0;
	}
	
	public int getTotalVotes()
	{
		return resultados != null ? resultados.getTotalVotos() : 0;
	}
	
	public TfsEncuestaOpcion getOption() {
		return option;
	}
	
	public void setOption(TfsEncuestaOpcion option)
	{
		this.option = option;
	}
	
	public TfsEncuestaOpcion getOptionImage() {
		return optionImage;
	}
	
	public void setOptionImage(TfsEncuestaOpcion optionImage)
	{
		this.optionImage = optionImage;
	}
	
	public boolean isMultiselect()
	{
		return encuesta!=null ? !encuesta.isRespuestaExcluyente() : false;
	}

	public boolean isPublished()
	{
		return encuesta!=null ? !encuesta.isNoPublicada() : false;
	}

	public boolean isOnlyregisteredusers()
	{
		return encuesta!=null ? encuesta.isUsuariosRegistrados() : true;
	}
	
	public boolean isUseCaptcha() {
		return encuesta!=null ? !encuesta.isUsarCaptcha() : false;
	}
	
	public String getStatus()
	{
		return encuesta!=null ? encuesta.getEstado() : "";
	}
	
	public String getStyle()
	{
		return encuesta!=null ? encuesta.getEstilo() : "";
	}
	
	public List<String> getCategories()
	{
		return encuesta.getcategorias();
	}
	
	public Map<String,Boolean> getHascategory()
	{
		List<String> categories = encuesta.getcategorias();
		Map<String,Boolean> categorias = new HashMap<String, Boolean>();
		
		if (categories.size()>0)
		{
			int lastElement = categories.size()-1;
			
			for (int j=0;j<=lastElement;j++){
				String categoria = categories.get(j);
				categorias.put(categoria, true);
			}
		}
			
		return categorias;
	}

	public boolean getHasImage(){
		return !encuesta.getImagenAMostrar().equals("");
	}
}
