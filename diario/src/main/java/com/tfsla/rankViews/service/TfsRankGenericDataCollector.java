package com.tfsla.rankViews.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Date;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;

import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.I_statisticsDataCollector;

public class TfsRankGenericDataCollector extends A_RankDataCollector implements I_statisticsDataCollector {

	List<TfsKeyValue> values;
	
	public TfsKeyValue[] collect(CmsObject cms, CmsResource res, CmsUser user, String sessionId, TfsHitPage page) throws Exception {
		
		values = new ArrayList<TfsKeyValue>();
		
		int tEd=0;
		int ed=0;
		String seccion = "";

		String sitePath = getSiteName(cms);
		String siteName = sitePath.replaceFirst("/sites/", "");

		seccion = cms.readPropertyObject(res, "seccion", false).getValue();

		long fechaLastModified = res.getDateLastModified();
		long fechaCreated = res.getDateCreated();
		
		String ultimaMod = cms.readPropertyObject(res, "ultimaModificacion", false).getValue();
		
		long ultimaModificacion = fechaLastModified;
		if (ultimaMod!=null)
			ultimaModificacion = Long.parseLong(ultimaMod);

		TipoEdicionService tService = new TipoEdicionService();
		
		String urlNoPath = cms.getRequestContext().removeSiteRoot(res.getRootPath());
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, urlNoPath);
		if (tEdicion==null)
			tEdicion = tService.obtenerEdicionOnline(siteName);

		if (tEdicion!=null) {
			tEd = tEdicion.getId();
			if (!tEdicion.isOnline()) {
				EdicionService eService = new EdicionService();
				Edicion edicion = eService.obtenerEdicionImpresa(cms, urlNoPath);
				if (edicion!=null)
					ed = edicion.getNumero();
			}
		}
		
	
		TfsKeyValue value = null;
		
		value = new TfsKeyValue();
		value.setKey("tipoEdicion");
		value.setValue("" + tEd);
		values.add(value);
		
		value = new TfsKeyValue();
		value.setKey("edicion");
		value.setValue("" + ed);
		values.add(value);
		
		if (seccion==null)
			seccion = "";
		
		value = new TfsKeyValue();
		value.setKey("seccion");
		value.setValue(seccion);
		values.add(value);

		value = new TfsKeyValue();
		value.setKey("fechaCreacion");
		value.setValue("" + fechaCreated);
		values.add(value);

		value = new TfsKeyValue();
		value.setKey("fechaModificacion");
		value.setValue("" + fechaLastModified);
		values.add(value);

		value = new TfsKeyValue();
		value.setKey("fechaUltimaModificacion");
		value.setValue("" + ultimaModificacion);
		values.add(value);

		/*
		if (user!=null && !user.isGuestUser())
		{
			value = new TfsKeyValue();
			value.setKey("user");
			value.setValue(user.getName());
			values.add(value);

		}
		else
		{
			value = new TfsKeyValue();
			value.setKey("user");
			value.setValue("Anon_" + sessionId);
			values.add(value);

		}
		*/

		TfsKeyValue[] keyArray = new TfsKeyValue[values.size()];
		values.toArray(keyArray);
		
		return keyArray;

	}

	private String getSiteName(CmsObject cms)
	{
		CmsSite site = OpenCms.getSiteManager().getCurrentSite(cms);
		String siteName = site.getSiteRoot(); 
		return siteName;
	}

	public String getContentName() {
		return "Todas";
	}

	public String getContentType() {
		return "";
	}

}
