package com.tfsla.diario.model;

import com.tfsla.opencmsdev.encuestas.RespuestaEncuestaConVotos;

public class TfsEncuestaOpcion {

	RespuestaEncuestaConVotos resp = null;
	String      text = "";
	String     image = "";
	String     description = "";
	
	public TfsEncuestaOpcion(RespuestaEncuestaConVotos resp, String text, String image, String description)
	{
		this.resp = resp;
		this.text = text;
		this.description = description;
	}
	
	public int getPosition()
	{
		return (resp!=null ? resp.getPosicionResultados() : 0);
	}
	
	public double getPorcentage()
	{
		return (resp!=null ? resp.getPorcentajeVotos() : 0);
	}
	
	public int getNumber()
	{
		return (resp!=null ? resp.getNroRespuesta() : 0);
	}
	
	public int getVotes()
	{
		return (resp!=null ? resp.getCantVotos() : 0);
	}
	
	public int getId()
	{
		return (resp!=null ? resp.getIdRespuesta() : 0);
	}
	
	public String getText()
	{
		return text;
	}

	public String getDescription()
	{
		return description;
	}

	public String getImage()
	{
		return image;
	}
	
	public Boolean getHasImage(){
		
		Boolean hasImage = false;
		
		if(image!= null && !image.equals(""))
			hasImage = true;
		
		return hasImage;
		
	}
}
