package com.tfsla.opencmsdev.encuestas;

import java.lang.reflect.InvocationTargetException;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.ediciones.services.openCmsService;

	public abstract class AbstractScheduledEncuestaProcess extends AbstractEncuestaProcess {

	protected String getUpdateEstadoPublicacionQuery(CmsObject cms, String encuestaURL, String nuevoEstado,
			String encuestaTableName) {
		return "UPDATE " + encuestaTableName + " " + "SET " + ESTADO_PUBLICACION + " = '" + nuevoEstado
				+ "' WHERE " + URL_ENCUESTA + " = '" + encuestaURL + "' AND " + SITIO + " = '" + openCmsService.getCurrentSite(cms) + "'";
	}

	protected String updateEstadoEncuestaContent(CmsObject cms, String encuestaURL, String nuevoEstado) {
		
		try {
			CmsResource resource = cms.readResource(encuestaURL);
			CmsFile file = CmsFile.upgrade(resource, cms);
			CmsXmlContent myxmlcontent = CmsXmlContentFactory.unmarshal(cms, file);

			myxmlcontent.getValue("estado[1]", CmsLocaleManager.getDefaultLocale()).setStringValue(cms,
					nuevoEstado);

			// seteo tambien el valor de la property estadoYGrupo, ya que se usa para filtar las encuestas
			String grupo = myxmlcontent.getValue("grupo[1]", CmsLocaleManager.getDefaultLocale())
					.getStringValue(cms);
			myxmlcontent
					.getValue(Encuesta.ESTADO_GRUPO_PROPERTY + "[1]", CmsLocaleManager.getDefaultLocale())
					.setStringValue(cms, Encuesta.getEstadoYGrupoIdentifier(nuevoEstado, grupo));

			byte[] content = myxmlcontent.marshal();
			file.setContents(content);
			cms.lockResource(encuestaURL);
			cms.writeFile(file);
			cms.unlockResource(encuestaURL);

			//cms.publishResource(encuestaURL);
			
			return null;
		}
		catch (Exception e) {
			return "ERROR - Cierre/Despublicación encuesta:" + encuestaURL + " No se pudo actualizar el estado en el contenido estructurado. Causa: "+ e.getMessage();
		}
	}
	
	protected boolean isValidStructureContent(CmsObject cms, String encuestaURL) throws Exception {
		
		boolean isValid = true;
		
		CmsXmlEntityResolver resolver = new CmsXmlEntityResolver(cms);
			
		CmsFile file = cms.readFile(encuestaURL);
		CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
		
		try {
			xmlContent.validateXmlStructure(resolver);
		} catch (CmsXmlException e) {
			isValid = false;
		}
		
		return isValid;
		
	}
	
	protected boolean fixValidStructureContent(CmsObject cms, String encuestaURL) throws Exception {
		
		boolean isFixed = true;
		
		CmsFile file = cms.readFile(encuestaURL);
		CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(cms, file);
		
		try{
			xmlContent.setAutoCorrectionEnabled(true);
			xmlContent.correctXmlStructure(cms);
			file.setContents(xmlContent.marshal());
			cms.writeFile(file);
		} catch (CmsXmlException e) {
			isFixed = false;
		}
		
		return isFixed;
		
	}


}