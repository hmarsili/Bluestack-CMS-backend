package com.tfsla.rankViews.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsMessages;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;

import com.tfsla.diario.comentarios.model.Comment;
import com.tfsla.diario.comentarios.services.CommentsModule;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.I_statisticsDataCollector;

public class TfsRankComentarioDataCollector implements I_statisticsDataCollector {

	List<TfsKeyValue> values;
	int tagsNumber = 0;

	public TfsKeyValue[] collect(CmsObject cms, CmsResource res, CmsUser user, String sessionId, TfsHitPage page) throws Exception {
		values = new ArrayList<TfsKeyValue>();
		tagsNumber = 0;
		
		try {

			if (res.getTypeId() == OpenCms.getResourceManager().getResourceType("comentario").getTypeId())
			{
				String cId="";
				for (TfsKeyValue value : page.getValues())
					if (value.getKey().equals("cId"))
						cId = value.getValue();
				
				Comment comment = CommentsModule.getInstance(cms).getComment(cms, cId);
				CmsUser cmsUser = cms.readUser(comment.getUser());
				
				page.setAutor(cmsUser.getId().getStringValue());

				CmsResource resNoticia = cms.readResource(comment.getNoticiaURL());
				
				values = new ArrayList<TfsKeyValue>();
				
				int tEd=0;
				int ed=0;
				String seccion = "";

				String sitePath = getSiteName(cms);
				String siteName = sitePath.replaceFirst("/sites/", "");

				seccion = cms.readPropertyObject(resNoticia, "seccion", false).getValue();

				Date fechaLastModified = new Date(resNoticia.getDateLastModified());
				Date fechaCreated = new Date(resNoticia.getDateCreated());
				
				String ultimaMod = cms.readPropertyObject(resNoticia, "ultimaModificacion", false).getValue();
				
				Date ultimaModificacion = fechaLastModified;
				if (ultimaMod!=null)
				{
					ultimaModificacion = new Date(Long.parseLong(ultimaMod));
				}

				TipoEdicionService tService = new TipoEdicionService();
				
				String urlNoPath = cms.getRequestContext().removeSiteRoot(resNoticia.getRootPath());
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

				Calendar fechaCreacion = new GregorianCalendar();
				fechaCreacion.setTime(fechaCreated);
				value = new TfsKeyValue();
				value.setKey("fechaCreacion");
				value.setValue("" + fechaCreacion.getTime().getTime());
				values.add(value);

				Calendar fechaModificacion = new GregorianCalendar();
				fechaCreacion.setTime(fechaLastModified);
				value = new TfsKeyValue();
				value.setKey("fechaModificacion");
				value.setValue("" + fechaModificacion.getTime().getTime());
				values.add(value);

				Calendar fechaUltimaModificacion = new GregorianCalendar();
				fechaCreacion.setTime(ultimaModificacion);
				value = new TfsKeyValue();
				value.setKey("fechaUltimaModificacion");
				value.setValue("" + fechaUltimaModificacion.getTime().getTime());
				values.add(value);

				
			}
		} catch (CmsException e) {
			throw new JspException("Error al intentar acceder a la informacion del archivo.",e);
		} catch (Exception e) {
			throw new JspException("Error al intentar acceder a la informacion de la edicion",e);
		}

		TfsKeyValue[] keyArray = new TfsKeyValue[values.size()];
		values.toArray(keyArray);
		
		return keyArray;

	}

	public String getContentName() {
		return "Comentarios";
	}

	public String getContentType() {
		try {
			return "" + OpenCms.getResourceManager().getResourceType("comentario").getTypeId();
		} catch (CmsLoaderException e) {
			e.printStackTrace();
			return "comentario";
		}
	}
	
	private String getSiteName(CmsObject cms)
	{
		CmsSite site = OpenCms.getSiteManager().getCurrentSite(cms);
		String siteName = site.getSiteRoot(); 
		return siteName;
	}
	
	protected Map<String, List<String>> parseParams(String url) throws UnsupportedEncodingException {
		Map<String, List<String>> params = new HashMap<String, List<String>>(); 
		String[] urlParts = url.split("\\?"); 
		if (urlParts.length > 1) {
			String query = urlParts[1];     
			for (String param : query.split("&")) 
			{         
				String[] pair = param.split("=");
				String key = URLDecoder.decode(pair[0], "UTF-8");
				String value = URLDecoder.decode(pair[1], "UTF-8");
				List<String> values = params.get(key);
				if (values == null) {
					values = new ArrayList<String>();
					params.put(key, values);
				}
				values.add(value);
			} 
		}
		return params;
	}

	public String getValue(CmsObject cms, String uid, String key)
			throws Exception {

		Map<String, List<String>> params = parseParams(uid);

		List<String> cIds = params.get("cId");	
		
		if (cIds!=null)
		{
			String commentId = cIds.get(0);
			Comment comment = CommentsModule.getInstance(cms).getComment(cms, commentId);

			if (key.equals("Title"))
				return comment.getTituloNoticia(cms);
			
			if (key.equals("seccion"))
				return comment.getSeccionNoticia(cms);

			if (key.equals("ultimaModificacion"))
				return comment.getDateAsString();

			if (key.equals("url"))
				return comment.getNoticiaURL();	
			
			if (key.equals("id"))
				return "" + comment.getId();
			
			if (key.equals("text"))
				return comment.getText();

			
		}
		return "";

	}

	public String getDateValue(CmsMessages msg, CmsObject cms, String uid,
			String key) throws Exception {

		Map<String, List<String>> params = parseParams(uid);

		List<String> cIds = params.get("cId");	
		
		if (cIds!=null)
		{
			String commentId = cIds.get(0);
			Comment comment = CommentsModule.getInstance(cms).getComment(cms, commentId);

		if (key.equals("ultimaModificacion"))
			return comment.getDateAsString();
		}
		return "";
		
	}
		
}

