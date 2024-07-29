package com.tfsla.rankViews.service;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.I_statisticsDataCollector;

import org.opencms.util.CmsUUID;

public class TfsRankNewsDataCollector extends A_RankDataCollector implements I_statisticsDataCollector {

	private static final Log LOG = CmsLog.getLog(RankService.class);

	List<TfsKeyValue> values;
	int tagsNumber = 0;

	private String getParam(String siteName, String publicationName, String paramName)
	{
    	String module = "newsAuthor";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
		return config.getParam(siteName, publicationName, module, paramName, "userCreated");

	}

	public TfsKeyValue[] collect(CmsObject cms, CmsResource res, CmsUser user, String sessionId, TfsHitPage page) throws Exception {

		int tEd=0;

		values = new ArrayList<TfsKeyValue>();
		tagsNumber = 0;
		

		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();

		String proyecto = siteName.replaceFirst("/sites/", "");

		String urlNoPath = cms.getRequestContext().removeSiteRoot(res.getRootPath());
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, urlNoPath);
		if (tEdicion==null)
			tEdicion = tService.obtenerEdicionOnline(proyecto);

		if (tEdicion!=null) {
			tEd = tEdicion.getId();
		}

		try {

			if (res.getTypeId() == OpenCms.getResourceManager().getResourceType("noticia").getTypeId())
			{
				
    			CmsFile file = cms.readFile(res);

    			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);


    			getCategories(content,res, cms);
    			
    			
				page.setTipoContenido(getContentType());
				

				CmsUser cmsUser = null;
				page.setAutor(null);
				
				String _excludeAnonymousFromRankings = getParam(siteName, ""+tEd,"excludeAnonymousFromRankings");
				boolean excludeAnonymousFromRankings = _excludeAnonymousFromRankings !=null && (_excludeAnonymousFromRankings.trim().toLowerCase().equals("yes") || _excludeAnonymousFromRankings.trim().toLowerCase().equals("true"));
				String userAnonCheck = getParam(siteName, ""+tEd,"anonymousUser");

				
				I_CmsXmlContentValue contentValue = content.getValue("autor/internalUser",Locale.ENGLISH);
				if (contentValue!=null)
				{
					String autor = contentValue.getStringValue(cms);
					if (autor!=null && autor.trim().length()>0)
					{
						if (!excludeAnonymousFromRankings || (excludeAnonymousFromRankings && !autor.equals(userAnonCheck))) {
							try {
								cmsUser = cms.readUser(autor);
							}
							catch (org.opencms.db.CmsDbEntryNotFoundException e){}
							if (cmsUser!=null)
								page.setAutor(cmsUser.getId().getStringValue());
							else
							{
								try {
									cmsUser = cms.readUser(new CmsUUID(autor));
								}
								catch (org.opencms.db.CmsDbEntryNotFoundException e){}
								catch (NumberFormatException e){}
	
								if (cmsUser!=null)
									page.setAutor(cmsUser.getId().getStringValue());
							}
						}
					}
				}
				if (page.getAutor()==null) 
				{
					String indexingMode = getParam(siteName, ""+tEd,"indexingModeOnEmpty");
					//empty | anonymousUser | newsCreator
					if (indexingMode.equals("anonymousUser") && !excludeAnonymousFromRankings) {
						String userAnon = getParam(siteName, ""+tEd,"anonymousUser");
						try {
							cmsUser = cms.readUser(userAnon);
							page.setAutor(cmsUser.getId().getStringValue());
						}
						catch (org.opencms.db.CmsDbEntryNotFoundException e){}
					} 
					else if (indexingMode.equals("newsCreator")) {
						try {
							cmsUser = cms.readUser(res.getUserCreated());
						}
						catch (org.opencms.db.CmsDbEntryNotFoundException e){}

						page.setAutor(res.getUserCreated().getStringValue());
					}

				}

				if (cmsUser!=null)
					getUserGrups(cmsUser, cms);
				
				TfsKeyValue[] keyArray = new TfsKeyValue[values.size()];
				values.toArray(keyArray);
				
				return keyArray;
			}
		} catch (CmsException e) {
			throw new JspException("Error al intentar acceder a la informacion del archivo.",e);
		} catch (Exception e) {
			throw new JspException("Error al intentar acceder a la informacion de la edicion",e);
		}

		return null;
	}

	public String getContentName() {
		return "Noticias";
	}

	public String getContentType() {
		try {
			return "" + OpenCms.getResourceManager().getResourceType("noticia").getTypeId();
		} catch (CmsLoaderException e) {
			e.printStackTrace();
			return "noticia";
		}
	}

	private boolean getUserGrups(CmsUser cmsUser, CmsObject cms) {
		
		try {
			List<CmsGroup> groups = cms.getGroupsOfUser(cmsUser.getName(), true);
			
			if (groups!=null)
				for (CmsGroup group : groups) {
					tagsNumber++;
					TfsKeyValue keyValue = new TfsKeyValue();
					keyValue.setKey("tag" + tagsNumber);
					keyValue.setValue("userGroup_" + group.getName());			
					values.add(keyValue);

				}
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}
	
	private boolean getCategories(CmsXmlContent xmlContent, CmsResource res, CmsObject cms)
	{
		List<String> tags = new ArrayList<String>();
		
		int j=1;
				
		String xmlName ="Categorias[" + j + "]";
		I_CmsXmlContentValue value = xmlContent.getValue(xmlName, Locale.ENGLISH);
		while (value!=null)
		{
			String categoria = value.getStringValue(cms);
			if (categoria!=null && categoria.trim().length() > 0)
			{
				String[] categorias = categoria.split("/");
				
				String catParent = "/";
				for (String tag : categorias)
							if (tag.trim().length()>0)
							{
								catParent += tag.trim() + "/";
								if (!catParent.equals("/system/") && !catParent.equals("/system/categories/") && !catParent.equals("/categories/"))
									if (!tags.contains(catParent))
										tags.add(catParent);
							}
			}
	
			j++;
			xmlName ="Categorias[" + j + "]";
			value = xmlContent.getValue(xmlName, Locale.ENGLISH);
		}

		try {
			CmsProperty keyProp = cms.readPropertyObject(res, "Keywords", false);
			
			if (!CmsProperty.getNullProperty().equals(keyProp)) {
				String keywords = keyProp.getValue();
				if (keywords!=null) {
					String[] keyword = keywords.split(",");
					LOG.debug("keywords: " + keywords);
					for (String tag : keyword) {
						LOG.debug("tag previo: " + tag);
							tag = TfsTokenHelper.convert(tag);
							LOG.debug("tag: " + tag);
							if (tag.length()>0 && !tags.contains(tag))
								tags.add(tag);
					}
				}
			}
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String tag : tags)
		{
			tagsNumber++;
			TfsKeyValue keyValue = new TfsKeyValue();
			keyValue.setKey("tag" + tagsNumber);
			keyValue.setValue(tag);			
			values.add(keyValue);
		}
		
		
		
		return false;
	}



}
