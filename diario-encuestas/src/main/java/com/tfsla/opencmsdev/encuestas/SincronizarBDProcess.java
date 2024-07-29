package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsSelectWidgetOption;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.exceptions.ApplicationException;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class SincronizarBDProcess extends AbstractEncuestaProcess {

	// ***************************
	// ** Main
	// ***************************
	public void execute(CmsObject cms) {
		try {
			this.deleteInconsistentOfflineContents(cms);
			this.deleteInconsistentOfflineTableData(cms);
			this.deleteInconsistentOnlineTableData(cms);
		}
		catch (Exception e) {
			throw new ApplicationException("No se pudo sincronizar la base y los contenidos", e);
		}
	}

	// ***************************
	// ** Delete methods
	// ***************************
	/**
	 * Borra de la tabla online del modulo aquellos registros que no se encuentren en la tabla offline.
	 * 
	 * @param cms
	 */
	private void deleteInconsistentOnlineTableData(CmsObject cms) {
		
		List urlsOnline = this.getAllEncuestas(cms, true);
		for (Iterator iter = urlsOnline.iterator(); iter.hasNext();) {
			String url = (String) iter.next();
			if (!this.estaEnBD(cms, url, false)) {
				// si esta en la tabla online, pero no esta en la offline, es una inconsistencia
				this.deleteEncuesta(cms, url, true);
			}
		}
	}

	/**
	 * Borra de la tabla offline del modulo aquellos registros que no correspondan a encuestas validas como
	 * contenidos offline.
	 * 
	 * @param cms
	 */
	private void deleteInconsistentOfflineTableData(CmsObject cms) {

		List<String> urls = getAllEncuestas(cms, false);

		for (Iterator iter = urls.iterator(); iter.hasNext();) {
			String url = (String) iter.next();
			if (!cms.existsResource(url)) {
				// la encuesta esta en las tablas del modulo, pero no esta
				// en la carpeta de contenidos,
				// lo cual es una inconsistencia, asi que borramos la
				// encuesta de las tablas.
				deleteEncuesta(cms, url, false);
			}
		}
	}

	/**
	 * Borra de la carpeta encuestas los contenidos que no esten en las tablas del modulo.
	 * 
	 * @param cms
	 * @throws Exception
	 */
	private void deleteInconsistentOfflineContents(CmsObject cms) throws Exception {
		
		TipoEdicionService tDAO = new TipoEdicionService();
		
		String proyecto = openCmsService.getCurrentSite(cms);

		try {
			List<TipoEdicion> tEdiciones = tDAO.obtenerTipoEdiciones(proyecto);

			for (TipoEdicion tEdicion : tEdiciones) {
				String folder = ModuloEncuestas.getEncuestaPath(cms,tEdicion.getId());
				
				if (cms.existsResource(folder)){

				List<CmsFile> encuestasFiles = cms.getResourcesInFolder(folder, CmsResourceFilter.DEFAULT);

				for (CmsFile file : encuestasFiles) {

					//String encuestaURL = ModuloEncuestas.getCompletePath(file.getName());
					
					String encuestaVFS = folder+"/"+file.getName();

					if (!estaEnBD(cms, encuestaVFS, false)) {
						// la encuesta esta en la carpeta /encuestas como contenido,
						// pero no esta en la base
						// lo cual es una inconsistencia, entonces borrarla de los
						// contenidos
						CmsResourceState fileState = file.getState();
						
						CmsResourceUtils.forceLockResource(cms,encuestaVFS);
						cms.deleteResource(encuestaVFS, CmsFile.DELETE_REMOVE_SIBLINGS);
						CmsResourceUtils.unlockResource(cms, encuestaVFS, false);
						
						if(!fileState.equals(CmsResourceState.STATE_NEW))
						   OpenCms.getPublishManager().publishResource(cms, encuestaVFS);
					}
				}
				}

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}

	}

	// ***************************
	// ** Private helpers
	// ***************************
	private void deleteEncuesta(CmsObject cms, String url, boolean onlineProject) {
		int encuestaID = onlineProject ? ModuloEncuestas.getEncuestaIDFromOnline(cms, url) : ModuloEncuestas
			.getEncuestaID(cms, url);

		String respuestaTableName = this.getRespuestaTableName(onlineProject);

		// borrar las respuestas de la encuesta
		String deleteRespuestasEncuestaSQL = "DELETE FROM " + respuestaTableName + " WHERE " + ID_ENCUESTA + " = "
			+ encuestaID;
		new QueryBuilder<List<String>>(cms).setSQLQuery(deleteRespuestasEncuestaSQL).execute();

		String encuestaTableName = this.getEncuestaTableName(onlineProject);
		// borrar la encuesta
		String deleteEncuestaSQL = "DELETE FROM " + encuestaTableName + " WHERE " + ID_ENCUESTA + " = " + encuestaID;
		new QueryBuilder<List<String>>(cms).setSQLQuery(deleteEncuestaSQL).execute();
	}

	private String getRespuestaTableName(boolean onlineProject) {
		return onlineProject ? TFS_RESPUESTA_ENCUESTA_ONLINE : TFS_RESPUESTA_ENCUESTA;
	}

	private String getEncuestaTableName(boolean onlineProject) {
		return onlineProject ? TFS_ENCUESTA_ONLINE : TFS_ENCUESTA;
	}

	private boolean estaEnBD(CmsObject cms, String encuestaURL, boolean onlineProject) {
		String getEncuestaSQL = "SELECT * FROM " + this.getEncuestaTableName(onlineProject) + " WHERE " + URL_ENCUESTA
			+ " = '" + encuestaURL + "' AND " + SITIO + "= '" + openCmsService.getCurrentSite(cms) + "'";

		ResultSetProcessor<Boolean> processor = new ResultSetProcessor<Boolean>() {

			private Boolean hayResultados = Boolean.FALSE;

			public void processTuple(ResultSet rs) {
				hayResultados = Boolean.TRUE;
			}

			public Boolean getResult() {
				return this.hayResultados;
			}

		};
		new QueryBuilder<Boolean>(cms).setSQLQuery(getEncuestaSQL).execute(processor);

		return processor.getResult().booleanValue();
	}

	private List<String> getAllEncuestas(CmsObject cms, boolean onlineProject) {
		String selectEncuestasActivaSQL = "SELECT " + URL_ENCUESTA + " FROM "
			+ this.getEncuestaTableName(onlineProject) + " WHERE " + SITIO + " = '" + openCmsService.getCurrentSite(cms) + "'";

		List<String> results = new QueryBuilder<List<String>>(cms).setSQLQuery(selectEncuestasActivaSQL).execute(
			new EncuestasURLsResultSetProcessor());

		return results;
	}
}