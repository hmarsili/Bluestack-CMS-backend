package com.tfsla.opencmsdev.encuestas;

import java.util.Iterator;
import java.util.List;

public class ResultadoEncuestaBean {

	private List<RespuestaEncuestaConVotos> respuestas;

	public ResultadoEncuestaBean(List<RespuestaEncuestaConVotos> respuestas) {
		this.respuestas = respuestas;
		
			for (Iterator it = respuestas.iterator(); it.hasNext();) {
				RespuestaEncuestaConVotos respuesta = (RespuestaEncuestaConVotos) it.next();
				respuesta.setResultado(this);
			}
		
	}

	public List<RespuestaEncuestaConVotos> getRespuestas() {
		return this.respuestas;
	}

	public int getTotalVotos() {
		int cant = 0;
		for (Iterator it = this.respuestas.iterator(); it.hasNext();) {
			RespuestaEncuestaConVotos respuesta = (RespuestaEncuestaConVotos) it.next();
			cant = cant + respuesta.getCantVotos();
		}

		return cant;
	}
}