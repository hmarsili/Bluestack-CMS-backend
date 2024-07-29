package com.tfsla.opencmsdev.encuestas;


public class RespuestaEncuestaConVotos {

	private int nroRespuesta;

	private int cantVotos;

	
	private ResultadoEncuestaBean resultadoEncuesta;

	private int idRespuesta;

	private int posicionResultados = -1;
	
	public RespuestaEncuestaConVotos(int id_respuesta, int nroRespuesta, int cantVotos) {
		this.nroRespuesta = nroRespuesta;
		this.cantVotos = cantVotos;
		this.idRespuesta = id_respuesta;
	}

	public int getCantVotos() {
		return this.cantVotos;
	}

	public double getPorcentajeVotos() {
		double porcentajeVotos = 0;
		if (this.resultadoEncuesta.getTotalVotos() != 0) {
			double cociente = (double) this.getCantVotos() / (double) this.resultadoEncuesta.getTotalVotos();
			porcentajeVotos = cociente * 100;
		}

		return porcentajeVotos;
	}

	public int getPosicionResultados() {
		if (posicionResultados!=-1)
			return posicionResultados;
		
		posicionResultados=1;
		for (RespuestaEncuestaConVotos resp: this.resultadoEncuesta.getRespuestas())
		{
			if (idRespuesta!=resp.idRespuesta)
				if (resp.cantVotos>cantVotos)
					posicionResultados++;
		}
		
		return posicionResultados;
	}
	
	public void setCantVotos(int cantVotos) {
		this.cantVotos = cantVotos;
	}

	/** publicado para persistencia de offline/online * */
	public int getNroRespuesta() {
		return this.nroRespuesta;
	}

	public void setResultado(ResultadoEncuestaBean resultado) {
		this.resultadoEncuesta = resultado;
	}

	public int getIdRespuesta() {
		return this.idRespuesta;
	}
}