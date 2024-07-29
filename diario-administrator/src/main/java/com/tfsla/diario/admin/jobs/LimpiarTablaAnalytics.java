package com.tfsla.diario.admin.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.tfsla.diario.analytics.data.NewsAnalyticsDataDAO;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.OpenCmsBaseService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.friendlyTags.TfsNoticiasListTag;
import com.tfsla.diario.newsCollector.LuceneNewsCollector;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

/**
 * 
 * PENDIENTE VALIDAR QUE TOMEMOS LA ULTIMA HORA HABILITADFA COMO FECHA COMPLETA Y  POR DIA COMPLETO!!!!
 * 
 * clase que se usa para limpiar las noticias de la tabla analytica
 * recibe como parámetro
 * - site : sitio de las noticias a limpiar
 * - publication: publicacion de las noticias a limpiar
 * - hoursEnable: horas permitidas para la compactacion de datos.
 * 
 */

public class LimpiarTablaAnalytics implements I_CmsScheduledJob {

	private String RESULTLOG = "";
	private int PUBLICATION = 0;
	private String SITE = "";

	public String launch(CmsObject cms, Map parameters) throws Exception {

		PUBLICATION = (Integer) parameters.get("publication");
		SITE = (String) parameters.get("site");
		int dateEnable = (Integer)parameters.get("hoursEnable");
		
		Calendar lastDateEnable = Calendar.getInstance();
		lastDateEnable.setTimeZone(TimeZone.getTimeZone("GMT-0"));
		lastDateEnable.add(Calendar.HOUR, -dateEnable);
		lastDateEnable.set(Calendar.MILLISECOND, 0);
		lastDateEnable.set(Calendar.SECOND, 0);
		lastDateEnable.set(Calendar.MINUTE, 0);

		NewsAnalyticsDataDAO analyticsDAO = new NewsAnalyticsDataDAO();
		analyticsDAO.clearData(SITE, PUBLICATION, dateEnable);
		
		RESULTLOG = "Se eliminaron las noticias de la tabla TFS_NEWS_ANALYTICS con fecha de actualizacion menor a  " + dateEnable;
		
		return RESULTLOG;
		
	}

}	