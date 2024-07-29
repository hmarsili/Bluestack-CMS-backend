package com.tfsla.rankViews.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.I_statisticsDataCollector;

public class TfsRankRecipeDataCollector extends A_RankDataCollector implements I_statisticsDataCollector {

	List<TfsKeyValue> values;
	int tagsNumber = 0;

	public TfsKeyValue[] collect(CmsObject cms, CmsResource res, CmsUser user, String sessionId, TfsHitPage page) throws Exception {
		values = new ArrayList<TfsKeyValue>();
		tagsNumber = 0;
		int tEd=0;

		int tipoRecipe = OpenCms.getResourceManager().getResourceType("receta").getTypeId();
	
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();

		if (res.getTypeId() == tipoRecipe ) {
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

		} 
		TfsKeyValue[] keyArray = new TfsKeyValue[values.size()];
		values.toArray(keyArray);
		
		return keyArray;
	}

	public String getContentName() {
		return "Recetas";
	}

	public String getContentType() {
		try {
			return "" + OpenCms.getResourceManager().getResourceType("receta").getTypeId();
		} catch (CmsLoaderException e) {
			e.printStackTrace();
			return "receta";
		}
	}

	
	
	private void getCategories(CmsXmlContent xmlContent,CmsResource res, CmsObject cms) {
		List<String> tags = new ArrayList<String>();
		
		try {
			String keywords = cms.readPropertyObject(res, "Keywords", false).getValue();
			if (keywords!=null) {
				String[] categorias = keywords.split(",");
				
				for (String tag : categorias) {
					tag = TfsTokenHelper.convert(tag);
					if (!tags.contains(tag))
						tags.add(tag);
				}
			}
			
			int j=1;
			String xmlName ="Categorias[" + j + "]";
			I_CmsXmlContentValue value = xmlContent.getValue(xmlName, Locale.ENGLISH);
			while (value!=null) {
				String categories = value.getStringValue(cms);
				if (categories!=null && categories.trim().length() > 0) {
			
					String[] categorias = categories.split("\\|");
				
					for (String categoria : categorias) {
						String[] catParts = categoria.split("/");
					
						String catParent = "/";
						for (String tag : catParts)
							if (tag.trim().length() > 0) {
								catParent += tag.trim() + "/";
								if (!catParent.equals("/system/") && !catParent.equals("/system/categories/") && !catParent.equals("/categories/"))
									if (!tags.contains(catParent))
										tags.add(catParent);
							}
	
					}
				}
				j++;
				xmlName ="Categorias[" + j + "]";
				value = xmlContent.getValue(xmlName, Locale.ENGLISH);
			}
			
			//Ingredientes
			j=1;
			xmlName ="ingrediente[" + j + "]/nombre["+j+"]";
			value = xmlContent.getValue(xmlName, Locale.ENGLISH);
			while (value!=null) {
				String ingredientes = value.getStringValue(cms);
				if (ingredientes!=null && ingredientes.trim().length() > 0) {
			
					String[] ingrediente = ingredientes.split("\\|");
					for (String item : ingrediente) {
						tags.add("ingr_" + item);
					}
				}
				j++;
				xmlName ="ingrediente[" + j + "]/nombre[1]";
				value = xmlContent.getValue(xmlName, Locale.ENGLISH);
			}
	
			//tipoCocina
			j=1;
			xmlName ="tipoCocina[" + j + "]";
			value = xmlContent.getValue(xmlName, Locale.ENGLISH);
			if (value!=null && value.getStringValue(cms).trim().length() > 0) {
				tags.add("cuisine_" + value.getStringValue(cms).trim());
			}
			
			//tipoCoccion
			j=1;
			xmlName ="tipoCoccion[" + j + "]";
			value = xmlContent.getValue(xmlName, Locale.ENGLISH);
			if (value!=null && value.getStringValue(cms).trim().length() > 0) {
				tags.add("cooking_" + value.getStringValue(cms).trim());
			}
				
	
			
		} catch (CmsException e) {
			e.printStackTrace();
		}

		for (String tag : tags) {
			tagsNumber++;
			TfsKeyValue keyValue = new TfsKeyValue();
			keyValue.setKey("tag" + tagsNumber);
			keyValue.setValue(tag);			
			values.add(keyValue);
		}
	}
	
	private String getParam(String siteName, String publicationName, String paramName)
	{
    	String module = "recipes";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
		return config.getParam(siteName, publicationName, module, paramName, "userCreated");

	}

}

