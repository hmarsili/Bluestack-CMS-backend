package com.tfsla.opencms.search.documents;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.document.Field;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.A_CmsXmlDocument;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

public class RecipeContentExtractor implements I_TFSContentExtractor {

	public boolean isconfiguredExtractor(CmsResource resource) {
		int tipo = resource.getTypeId();
		try {
			return (tipo == OpenCms.getResourceManager().getResourceType("receta").getTypeId());
		} catch (CmsLoaderException e) {
			return false;
		}
	}

	public void extractContent(CmsObject cms, CmsFile file,
			CmsResource resource, Locale locale, StringBuffer content,
			HashMap items, List<Field> customFieds) {
		
		String rootPath = resource.getRootPath();
		String fileName = rootPath.substring(rootPath.lastIndexOf("/")+1);
		int tEd = 0;
		
		
		items.put("temporal[1]", "" + fileName.contains("~"));
		
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
		
			List<I_CmsXmlContentValue> values = xmlContent.getValues("Categorias", contentLocale);
			int idx=1;
			for (I_CmsXmlContentValue value : values) {
				String categoria = xmlContent.getStringValue(cms, "Categorias[" + idx + "]", contentLocale);
				
				categoria = categoria.replaceAll("/", " ");
		    	 	categoria = categoria.replaceAll("[-_]", "");
				items.put("categ["+ idx + "]", categoria);
				idx++;
			}
			
			String tiempoCoccion = xmlContent.getStringValue(cms,"tiempoCoccion", contentLocale);
			if (!tiempoCoccion.equals("")) {
				long result = getTransformationTime(tiempoCoccion);
				if (result != 0)
					items.put("tiempoCocc[1]",String.valueOf(result));
			}
			String tiempoPreparacion = xmlContent.getStringValue(cms,"tiempoPreparacion", contentLocale);
			if (!tiempoPreparacion.equals("")) {
				long result = getTransformationTime(tiempoPreparacion);
				if (result != 0)
					items.put("tiempoPrep[1]",String.valueOf(result));
			}
			
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

			
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getParam(String siteName, String publicationName, String paramName)
	{
    	String module = "recipe";
 		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
 		
		return config.getParam(siteName, publicationName, module, paramName, "userCreated");

	}
	//entrega el long del tiempo 
	private  long  getTransformationTime (String value) {
		String[] splitContent = value.split("-");
    	Integer quantity =0;
    	String unity = "";
    	if (splitContent.length == 2) {
    		try {
    			quantity= Integer.valueOf(splitContent[0]);
    		} catch (NumberFormatException ex) {
    			// toma el valor en 0 si no puede parsear los tiempos
    		}
    		unity = splitContent[1];
    	}
		if (unity.equals("segundos"))
			return 1000*quantity;
		else if (unity.equals("minutos"))
			return 60*1000*quantity;
		else if (unity.equals("horas"))
			return 60*60*1000*quantity;
		return 0;
	}
}

