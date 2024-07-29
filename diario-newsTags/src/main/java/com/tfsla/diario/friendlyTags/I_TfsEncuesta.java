package com.tfsla.diario.friendlyTags;

import com.tfsla.opencmsdev.encuestas.Encuesta;
import com.tfsla.opencmsdev.encuestas.ResultadoEncuestaBean;

public interface I_TfsEncuesta {

	public Encuesta getEncuesta();
	public ResultadoEncuestaBean getResultadosEncuesta();
	public String getEncuestaUrl();
}
