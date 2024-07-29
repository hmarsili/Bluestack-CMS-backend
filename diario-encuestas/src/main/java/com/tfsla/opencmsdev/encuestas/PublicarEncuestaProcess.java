package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsLogReport;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.services.openCmsService;
import com.tfsla.utils.CmsResourceUtils;
import com.tfsla.utils.DateUtils;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class PublicarEncuestaProcess extends AbstractEncuestaProcess {

	public void execute(CmsJspActionElement jsp, String encuestaURL) {
		// *******************************************************
		// ** obtenemos las encuestas y respuestas del offline...
		// *******************************************************
		try {
			CmsResourceUtils.forceLockResource(jsp.getCmsObject(),encuestaURL);
			
			Encuesta encuesta = Encuesta.getEncuestaFromURL(jsp.getCmsObject(), encuestaURL);
			
			// update del contenido encuesta con los datos de publicacion
			Locale locale = java.util.Locale.ENGLISH;
			
			CmsFile readFile = jsp.getCmsObject().readFile(encuestaURL);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(jsp.getCmsObject(), readFile);
					      content.setAutoCorrectionEnabled(true); 
			              content.correctXmlStructure(jsp.getCmsObject());
			              content.getValue("fechaPublicacion", locale).setStringValue(jsp.getCmsObject(), DateUtils.today());
			              readFile.setContents(content.marshal());
			              jsp.getCmsObject().writeFile(readFile);
			              
			int encuestaID = ModuloEncuestas.getEncuestaID(jsp.getCmsObject(), encuestaURL);
			
			//TODO: por que saco las respuestas de la base y no de la encuesta misma??
			List respuestas = this.getRespuestas(jsp, encuestaID);

			//publicamos el contenido, que ahora tiene los datos de publicacion
			
			List<CmsResource> publishList = new ArrayList<CmsResource>();
	    	CmsResource res = jsp.getCmsObject().readResource(encuestaURL);    	
	    	publishList.add(res);
	    	
			List<CmsRelation> relations = jsp.getCmsObject().getRelationsForResource(res, CmsRelationFilter.ALL);
	    	
			for (CmsRelation rel : relations){
				String relation = "";
		      	
		      	String rel1 = jsp.getCmsObject().getRequestContext().removeSiteRoot(rel.getTargetPath());
   				String rel2 = jsp.getCmsObject().getRequestContext().removeSiteRoot(rel.getSourcePath());

   				if (rel1.equals(encuestaURL))
   					relation = rel2;
   				else
   					relation = rel1;
   				
   			//	if (!jsp.getCmsObject().getLock(relation).isUnlocked()){
			//	     if(!jsp.getCmsObject().getLock(relation).isOwnedBy(jsp.getCmsObject().getRequestContext().currentUser())){
			//	    	 jsp.getCmsObject().changeLock(relation);
			//	    }
			//	}else{
			//		jsp.getCmsObject().lockResource(relation);
			//	}
   				
   				if (!jsp.getCmsObject().getLock(relation).isUnlocked())
   				 com.tfsla.utils.CmsResourceUtils.unlockResource(jsp.getCmsObject(),relation,false);
				
				CmsResource  resourceRelation = jsp.getCmsObject().readResource(relation);
   				
				if(resourceRelation.getRootPath().indexOf("~")< 0 )
   				     publishList.add(resourceRelation);
			}
			
			OpenCms.getPublishManager().publishProject(jsp.getCmsObject(), new CmsLogReport(Locale.getDefault(), this.getClass()), OpenCms.getPublishManager().getPublishList(jsp.getCmsObject(),publishList, false));


			// **************************************************************************************
			// ** Y las insertamos en el online, reemplazando lo que hubiera, salvo la cant de votos.
			// **************************************************************************************
			boolean existsInOnline = existsInOnline(jsp, encuesta);
			
			if (existsInOnline) {
				new UpdateEncuestaProcess().execute(jsp.getCmsObject(), encuesta);
			}
			else {
				// es un insert
			    new InsertEncuestaProcess().executeInOnline(jsp.getCmsObject(), encuesta, respuestas);
			}
			
			// Para corregir encuestas anteriores rotas agregamos esta validacion, revisamos si las respuestas existen en el online y si no las insertamos.
			int encuestaOnlineID = ModuloEncuestas.getEncuestaIDFromOnline(jsp.getCmsObject(), encuestaURL);
			List respuestasOnline = this.getRespuestasOnline(jsp, encuestaOnlineID);
			
			if(respuestasOnline==null || respuestasOnline.size()==0 ){
				for (Iterator iter = respuestas.iterator(); iter.hasNext();) {
					RespuestaEncuestaConVotos respuesta = (RespuestaEncuestaConVotos) iter.next();
					String insertRespuestaInOnline = "INSERT INTO " + TFS_RESPUESTA_ENCUESTA_ONLINE + " (" + ID_ENCUESTA
						+ ", " + NRO_RESPUESTA + ") " + "VALUES (" + encuestaOnlineID + ", '" + respuesta.getNroRespuesta()
						+ "')";

					new QueryBuilder<String>(jsp.getCmsObject()).setSQLQuery(insertRespuestaInOnline).execute();
				}
			}
			
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean existsInOnline(CmsJspActionElement jsp, Encuesta encuestaDAO) {
		String existsEncuestaInOnlineQuery = "SELECT " + URL_ENCUESTA + " FROM " + TFS_ENCUESTA_ONLINE + " WHERE "
			+ URL_ENCUESTA + " = '" + encuestaDAO.getEncuestaURL() + "' AND " + SITIO + " = '" + openCmsService.getCurrentSite(jsp.getCmsObject()) + "'";

		ResultSetProcessor processor = new EncuestasResultSetProcessor() {

			private Boolean exists = false;

			@Override
			protected void doProcessTuple(ResultSet rs) throws Exception {
				this.exists = Boolean.TRUE;
			}

			public Boolean getResult() {
				return this.exists;
			}
		};

		Boolean existsInOnline = (Boolean) getQueryRunner(jsp.getCmsObject(), existsEncuestaInOnlineQuery).execute(
			processor);
		return existsInOnline.booleanValue();
	}

	/**
	 * Obtiene las respuestas de las tablas del offline.
	 * 
	 * @param jsp
	 * @param encuestaID
	 * @return una lista de respuestas
	 */
	private List getRespuestas(CmsJspActionElement jsp, int encuestaID) {
		String selectRespuestasQuery = "SELECT * FROM " + TFS_RESPUESTA_ENCUESTA + " WHERE " + ID_ENCUESTA + " = "
			+ encuestaID;

		ResultSetProcessor processorRespuestas = new EncuestasResultSetProcessor() {

			private List respuestas = new ArrayList();

			@Override
			protected void doProcessTuple(ResultSet rs) throws Exception {
				// no me interesa la cantidad de votos ni el id que pueda tener en el offline, entonces le
				// paso cero. hacker.
				this.respuestas.add(new RespuestaEncuestaConVotos(0, rs.getInt(NRO_RESPUESTA), 0));
			}

			public List getResult() {
				return this.respuestas;
			}

		};

		return (List) getQueryRunner(jsp.getCmsObject(), selectRespuestasQuery).execute(processorRespuestas);
	}
	
	private List getRespuestasOnline(CmsJspActionElement jsp, int encuestaID) {
		String selectRespuestasQuery = "SELECT * FROM " + TFS_RESPUESTA_ENCUESTA_ONLINE + " WHERE " + ID_ENCUESTA + " = "
			+ encuestaID;

		ResultSetProcessor processorRespuestas = new EncuestasResultSetProcessor() {

			private List respuestas = new ArrayList();

			@Override
			protected void doProcessTuple(ResultSet rs) throws Exception {
				// no me interesa la cantidad de votos ni el id que pueda tener en el offline, entonces le
				// paso cero. hacker.
				this.respuestas.add(new RespuestaEncuestaConVotos(0, rs.getInt(NRO_RESPUESTA), 0));
			}

			public List getResult() {
				return this.respuestas;
			}

		};

		return (List) getQueryRunner(jsp.getCmsObject(), selectRespuestasQuery).execute(processorRespuestas);
	}
	
}