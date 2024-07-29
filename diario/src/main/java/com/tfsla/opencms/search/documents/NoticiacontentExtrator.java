package com.tfsla.opencms.search.documents;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedNumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.data.EdicionesDAO;
import com.tfsla.diario.ediciones.data.TipoEdicionesDAO;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;


public class NoticiacontentExtrator implements I_TFSContentExtractor {

	final static Log LOG = CmsLog.getLog(NoticiacontentExtrator.class);

	static public final String KEY_SEPARATOR = ".";
	static public final String VALUE_SEPARATOR = ".";
	
	
	Collection<String> camposZonasYSecciones;
	Map<String,List<String>> camposZonasYSecciones_Valores;
	
	Collection<String> camposPrioridades;
	Map<String,List<Integer>> camposPrioridades_Valores;
	
	boolean enPortada;
	
	public void extractContent(CmsObject cms, CmsFile file, CmsResource resource, Locale locale, StringBuffer content, HashMap items, List<Field> customFields) 
	{
		
		String rootPath = resource.getRootPath();
		String site = rootPath.replaceAll("/sites/", "");
		site = site.substring(0,site.indexOf("/"));
		String pathNoticia = rootPath.replaceAll("/sites/", "").replace(site + "/", "");
		int tEd = 0;
		int ed = 0;
		String fecha = "";
		TipoEdicion tEdicion = null;

		try {
			TipoEdicionesDAO tDAO = new TipoEdicionesDAO();
			String tipoEdicion = pathNoticia.substring(0,pathNoticia.indexOf("/"));
			// fijarse si el diario es el online.
			if (tipoEdicion.equals("contenidos"))
				tEdicion = tDAO.getTipoEdicionOnline(site);
			else
				tEdicion = tDAO.getTipoEdicion(site,tipoEdicion);

			if (tEdicion==null)
				tEdicion = tDAO.getTipoEdicionOnlineRoot(site);

			tEd = tEdicion.getId();
			if (!tEdicion.isOnline()) {
				String edStr = pathNoticia.replace(tipoEdicion + "/", "");
				edStr = edStr.substring(edStr.indexOf("/") +1);
				edStr = edStr.substring(edStr.indexOf("/") +1);
				edStr = edStr.substring(0, edStr.indexOf("/"));
				edStr = edStr.replace("edicion_", "");
				EdicionesDAO eDAO = new EdicionesDAO();
				Edicion edicion = eDAO.getEdicion(tEdicion.getId(), Integer.parseInt(edStr));
				ed = edicion.getNumero();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd");
				fecha = sdf.format(edicion.getFecha()).replaceAll(" ", "");
			}
			else
			{
				fecha = rootPath.substring(rootPath.indexOf("contenidos/") + 11, rootPath.indexOf("contenidos/") + 21);
				fecha = fecha.replace("/", "");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String fileName = rootPath.substring(rootPath.lastIndexOf("/")+1);

		items.put("temporal[1]", "" + fileName.contains("~"));
		items.put("fechaNoticia[1]", fecha);
		items.put("edicion[1]", ""+ed);
		items.put("tipoEdicion[1]", ""+tEd);
		items.put("sitio[1]", site);


		CmsUser user=null;

		try {
			items.put("newscreator[1]", cms.readUser(resource.getUserCreated()).getName());
		} catch (CmsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		String indexingMode = getParam(siteName, ""+tEd,"indexingModeOnEmpty");
		//empty | anonymousUser | newsCreator
		try {
			if (items.get("autor[1]/internalUser[1]")==null || items.get("autor[1]/internalUser[1]").toString().trim().equals("")) {
				if (indexingMode.equals("anonymousUser")) {
					user = cms.readUser(getParam(siteName, ""+tEd,"anonymousUser"));
					items.put("autor[1]/internalUser[1]",user.getName());
					items.put("autor[1]/nickName[1]",user.getAdditionalInfo("APODO")==null?"":user.getAdditionalInfo("APODO").toString());
					items.put("autor[1]/fullName[1]",user.getFirstname() + " " + user.getLastname());
				} 
				else if (indexingMode.equals("newsCreator")) {
					user = cms.readUser(resource.getUserCreated());
					items.put("autor[1]/internalUser[1]",user.getName());
					items.put("autor[1]/nickName[1]",user.getAdditionalInfo("APODO")==null?"":user.getAdditionalInfo("APODO").toString());
					items.put("autor[1]/fullName[1]",user.getFirstname() + " " + user.getLastname());
				}
	
			}
			else {
				user = cms.readUser(items.get("autor[1]/internalUser[1]").toString());
				items.put("autor[1]/nickName[1]",user.getAdditionalInfo("APODO")==null?"":user.getAdditionalInfo("APODO").toString());
				items.put("autor[1]/fullName[1]",user.getFirstname() + " " + user.getLastname());
			}

			if (user!=null)
			{
				String grupos = "";
				List<CmsGroup> groups = cms.getGroupsOfUser(user.getName(), true);
				if (groups!=null)
					for (CmsGroup group : groups)
					{
						grupos += " " + group.getName();
					}
				items.put("usergroups[1]", grupos);
			}

		} catch (CmsException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int autorNumber = 2;
		while (items.get("autor[" + autorNumber + "]/internalUser[1]")!=null && items.get("autor[" + autorNumber + "]/internalUser[1]").toString().trim().equals(""))
		{
			try {
				user = cms.readUser(items.get("autor[" + autorNumber + "]/internalUser[1]").toString());
				items.put("autor[" + autorNumber + "]/nickName[1]",user.getAdditionalInfo("APODO")==null?"":user.getAdditionalInfo("APODO").toString());
				items.put("autor[" + autorNumber + "]/fullName[1]",user.getFirstname() + " " + user.getLastname());
	
				if (user!=null)
				{
					String grupos = "";
					List<CmsGroup> groups = cms.getGroupsOfUser(user.getName(), true);
					if (groups!=null)
						for (CmsGroup group : groups)
						{
							grupos += " " + group.getName();
						}
					items.put("usergroups[" + autorNumber + "]", grupos);
				}
			} catch (CmsException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			autorNumber++;
		}
				
		String absolutePath = cms.getSitePath(file);
		A_CmsXmlDocument xmlContent;
		try {
			xmlContent = CmsXmlContentFactory.unmarshal(cms, file);

			List<Locale> locales = xmlContent.getLocales();
			if (locales.size() == 0) {
				locales = OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath);
			}

		    
			Locale contentLocale = OpenCms.getLocaleManager().getBestMatchingLocale(
					locale,
					OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath),
					locales);

			/* Comienzo de agregado de campos por notas en portadas (alto transito) */
			String seccion = xmlContent.getStringValue(cms, "seccion", contentLocale);
			String zonahome = xmlContent.getStringValue(cms, "zonahome", contentLocale);
			String prioridadhome = xmlContent.getStringValue(cms, "prioridadhome", contentLocale);
			String zonaseccion = xmlContent.getStringValue(cms, "zonaseccion", contentLocale);
			String prioridadseccion = xmlContent.getStringValue(cms, "prioridadseccion", contentLocale);

			enPortada=false;
			
			camposZonasYSecciones = new HashSet<String>();
			camposZonasYSecciones_Valores = new HashMap<String,List<String>>();
			
			camposPrioridades = new HashSet<String>();
			camposPrioridades_Valores = new HashMap<String,List<Integer>>();
			
			getHighTrafficInformation(
					tEdicion.getNombre(), zonahome, prioridadhome, seccion,
					zonaseccion, prioridadseccion);

			List<I_CmsXmlContentValue> values = xmlContent.getValues("publicaciones", contentLocale);
			for (I_CmsXmlContentValue value : values) {
				String preffix = "publicaciones["  + (value.getIndex()+1) + "]/";

				String publicacion = xmlContent.getStringValue(cms, preffix + "publicacion", contentLocale);
				zonahome = xmlContent.getStringValue(cms, preffix + "zonahome", contentLocale);
				prioridadhome = xmlContent.getStringValue(cms, preffix + "prioridadhome", contentLocale);
				seccion = xmlContent.getStringValue(cms, preffix + "seccion", contentLocale);
				zonaseccion = xmlContent.getStringValue(cms, preffix + "zonaseccion", contentLocale);
				prioridadseccion = xmlContent.getStringValue(cms, preffix + "prioridadseccion", contentLocale);

				getHighTrafficInformation(
						publicacion, zonahome, prioridadhome, seccion,
						zonaseccion, prioridadseccion);
			}
			
			LOG.debug("Indexando hightraffic para noticia " + file.getRootPath());
			for (String campo : camposPrioridades) {
				for (Integer prio : camposPrioridades_Valores.get(campo)) {
					LOG.debug(campo + ": " + prio);
					customFields.add(new SortedNumericDocValuesField(campo, prio));
					//customFields.add(new IntPoint(campo, prio));
					//Esto lo estoy por probar...
					customFields.add(new StoredField(campo, prio)); 
				}
			}
			for (String campo : camposZonasYSecciones) {
				for (String zonaSeccion :  camposZonasYSecciones_Valores.get(campo)) {
					LOG.debug(campo + ": " + zonaSeccion);
					customFields.add(new StringField(campo,zonaSeccion, Field.Store.YES));
					customFields.add(new SortedSetDocValuesField(
							campo, 
			        		new BytesRef(
			        				zonaSeccion
			        				)
			        		)
					);
				}
			}
			
			/* Fin de agregado de campos por notas en portadas (alto transito) */

			values = xmlContent.getValues("Categorias", contentLocale);
			int idx=1;
			for (I_CmsXmlContentValue value : values) {
				String categoria = xmlContent.getStringValue(cms, "Categorias[" + idx + "]", contentLocale);
				
				categoria = categoria.replaceAll("/", " ");
   	    	 	categoria = categoria.replaceAll("[-_]", "");
				items.put("categ["+ idx + "]", categoria);
				idx++;
			}
			
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		items.put("highTraffic[1]", Boolean.toString(enPortada));
		//content.append(field.trim());
		//content.append('\n');

	}


	private String getParam(String siteName, String publicationName, String paramName)
	{
    	String module = "newsAuthor";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
		return config.getParam(siteName, publicationName, module, paramName, "userCreated");

	}
	
	private void getHighTrafficInformation(
			String publicacion, String zonahome, String prioridadhome,
			String seccion, String zonaseccion, String prioridadseccion) {
		
		boolean zonaHomeLoaded = zonahome!=null && !zonahome.equals("no_mostrar") && !zonahome.equals("");
		boolean zonaSeccionLoaded =zonaseccion!=null && !zonaseccion.equals("no_mostrar") && !zonaseccion.equals("");
		
		if (zonaHomeLoaded || zonaSeccionLoaded) {
			
			//Para guardar las diferentes prioridades de una noticia en una publicacion			
			// ej: pub_diario
			String pub = "pub" + VALUE_SEPARATOR + escapeValue(publicacion);
			
			if (zonaHomeLoaded) {
				
				enPortada=true;
				
				//ej: pub_diario -> zonahome
				addValorString(camposZonasYSecciones, camposZonasYSecciones_Valores, pub, "zonahome");				
				
				String pub_prio = pub + VALUE_SEPARATOR + "prio";
				//ej: pub_diario_prio
				addValorInteger(camposPrioridades, camposPrioridades_Valores, pub_prio, Integer.parseInt(prioridadhome));
				
				String pub_zonaHome = "pub" + VALUE_SEPARATOR + escapeValue(publicacion) + KEY_SEPARATOR + "zonahome";
				//ej: pub_diario_zonahome -> destacado
				addValorString(camposZonasYSecciones, camposZonasYSecciones_Valores, pub_zonaHome, zonahome);
				
				String pub_zonaHome_prio = pub_zonaHome + VALUE_SEPARATOR + "prio";
				// ej: pub_diario_zonahome_prio -> 3
				addValorInteger(camposPrioridades, camposPrioridades_Valores, pub_zonaHome_prio, Integer.parseInt(prioridadhome));
				
				String pub_zonahome_val = "pub" + VALUE_SEPARATOR + escapeValue(publicacion) + KEY_SEPARATOR + "zonahome" + KEY_SEPARATOR + escapeValue(zonahome) + VALUE_SEPARATOR + "prio";
				// ej: pub_diario_zonahome_destacado_prio -> 3
				addValorInteger(camposPrioridades, camposPrioridades_Valores, pub_zonahome_val, Integer.parseInt(prioridadhome));
				
			}

			if (zonaSeccionLoaded && !seccion.equals("")) {
				
				enPortada=true;
				
				//ej: pub_diario -> zonaseccion
				addValorString(camposZonasYSecciones, camposZonasYSecciones_Valores, pub, "zonaseccion");
				
				String pub_prio = pub + VALUE_SEPARATOR + "prio";
				//ej: pub_diario_prio -> 5
				addValorInteger(camposPrioridades, camposPrioridades_Valores, pub_prio, Integer.parseInt(prioridadseccion));
				
				String pub_sec = pub + VALUE_SEPARATOR + "seccion";
				//ej: pub_diario_seccion -> espectaculos
				addValorString(camposZonasYSecciones, camposZonasYSecciones_Valores, pub_sec, seccion);
		
				String pub_zona = pub + KEY_SEPARATOR + "zonaseccion";
				//ej: pub_diario_zonaseccion -> urgente
				addValorString(camposZonasYSecciones, camposZonasYSecciones_Valores, pub_zona, zonaseccion);		
				
				String pub_seccion_zona = pub_sec + KEY_SEPARATOR + escapeValue(seccion);
				//ej: pub_diario_seccion_espectaculos -> urgente
				addValorString(camposZonasYSecciones, camposZonasYSecciones_Valores, pub_seccion_zona, zonaseccion);
				
				String pub_seccion_zona_prio = pub_seccion_zona + KEY_SEPARATOR + "prio";
				//ej: pub_diario_seccion_espectaculos_prio -> 5
				addValorInteger(camposPrioridades, camposPrioridades_Valores,  pub_seccion_zona_prio,Integer.parseInt(prioridadseccion));
				
				String pub_zonaSeccion_prio = pub + KEY_SEPARATOR + "zonaseccion"  + KEY_SEPARATOR + "prio" ;
				// ej: pub_diario_zonaseccion_prio -> 5
				addValorInteger(camposPrioridades, camposPrioridades_Valores,  pub_zonaSeccion_prio,Integer.parseInt(prioridadseccion));
				
				String pub_zonaseccion_prio = pub + KEY_SEPARATOR + "zonaseccion" + KEY_SEPARATOR + escapeValue(zonaseccion) + KEY_SEPARATOR + "prio";
				//ej: pub_diario_zonaseccion_urgente_prio -> 5
				addValorInteger(camposPrioridades, camposPrioridades_Valores, pub_zonaseccion_prio,Integer.parseInt(prioridadseccion));
				
				String pub_seccion_zona_val_prio = pub_seccion_zona + KEY_SEPARATOR + "zonaseccion" + KEY_SEPARATOR + escapeValue(zonaseccion) + KEY_SEPARATOR + "prio";
				//ej: pub_diario_seccion_espectaculos_zonaseccion_urgente_prio -> 5
				addValorInteger(camposPrioridades, camposPrioridades_Valores,  pub_seccion_zona_val_prio,Integer.parseInt(prioridadseccion));
			}
		}
	}


	private void addValorInteger(Collection<String> listaCampos, Map<String, List<Integer>> camposValores, String campo, Integer valor) {
		listaCampos.add(campo);
		
		List<Integer> valores = camposValores.get(campo);
		if (valores==null) {
			valores = new ArrayList<Integer>();
		}
		valores.add(valor);
		camposValores.put(campo, valores);
	}

	private void addValorString(Collection<String> listaCampos, Map<String, List<String>> camposValores, String campo, String valor) {
		listaCampos.add(campo);
		
		List<String> valores = camposValores.get(campo);
		if (valores==null) {
			valores = new ArrayList<String>();
		}
		valores.add(escapeValue(valor));
		camposValores.put(campo, valores);
	}


	public static String escapeValue(String value) {
		String escapedValue = value.replaceAll("\\.", ".dot.");
		escapedValue = escapedValue.replaceAll("_", ".slash.");
		escapedValue = escapedValue.replaceAll("-", ".minus.");
		escapedValue = escapedValue.toLowerCase();
		return escapedValue;
		
		
	}
	
	public static String unescapeValue(String value) {
		String escapedValue = value.replaceAll("\\.dot\\.",".");
		escapedValue = escapedValue.replaceAll("\\.slash\\.","_");
		escapedValue = escapedValue.replaceAll("\\.minus\\.","-");
		escapedValue = escapedValue.toLowerCase();
		return escapedValue;
		
		
	}
	
	public boolean isconfiguredExtractor(CmsResource resource) {
		int tipo = resource.getTypeId();
		try {
			return (tipo == OpenCms.getResourceManager().getResourceType("noticia").getTypeId());
		} catch (CmsLoaderException e) {
			return false;
		}
	}

}