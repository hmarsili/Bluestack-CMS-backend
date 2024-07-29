package com.tfsla.diario.ediciones.jobs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;


public class PublicacionDiferidaJob implements I_CmsScheduledJob {

	public String launch(CmsObject cms, Map parameters) throws Exception {
		String resultados = "";

		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		resultados = "Lanzando publicación diferida a las " + sdf.format(now) + "\n";
		EdicionService eService = new EdicionService();
		TipoEdicionService tService = new TipoEdicionService();

		List<Edicion> Ediciones = eService.obtenerEdicionesAPublicar(now);
		String auxMsg = "";
		for (Iterator it = Ediciones.iterator();it.hasNext();)
		{
			Edicion edicion = (Edicion) it.next();

			TipoEdicion tEdicion = tService.obtenerTipoEdicion(edicion.getTipo());
			auxMsg += "Publicando " + tEdicion.getDescripcion() + " edicion " + edicion.getNumero() + "\n";
			eService.publicarEdicion(edicion.getTipo(), edicion.getNumero(), cms);

			edicion.setPublicacion(null);
			eService.establecerFechaPublicacionEdicion(edicion);

			tService.establecerEdicionActiva(edicion.getTipo(), edicion.getNumero());
			auxMsg += "Estableciendo como activa a " + tEdicion.getDescripcion() + " edicion " + edicion.getNumero() + "\n";
		}

		resultados += auxMsg;
		return resultados;
	}

}
