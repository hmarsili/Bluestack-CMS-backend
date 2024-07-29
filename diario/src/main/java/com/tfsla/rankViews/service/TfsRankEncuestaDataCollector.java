package com.tfsla.rankViews.service;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.jsp.JspException;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.I_statisticsDataCollector;
import org.opencms.util.CmsUUID;

public class TfsRankEncuestaDataCollector extends A_RankDataCollector implements I_statisticsDataCollector {

	List<TfsKeyValue> values;
	int tagsNumber = 0;
	
	public TfsKeyValue[] collect(CmsObject cms, CmsResource res, CmsUser user, String sessionId, TfsHitPage page) throws Exception {
	
		values = new ArrayList<TfsKeyValue>();
		tagsNumber = 0;
		

		try {

			if (res.getTypeId() == OpenCms.getResourceManager().getResourceType("encuesta").getTypeId())
			{
				
    			CmsFile file = cms.readFile(res);

    			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);


    			getCategories(content,res, cms);
    			
				page.setTipoContenido(getContentType());
				

				page.setAutor(null);
				I_CmsXmlContentValue contentValue = content.getValue("usuarioPublicador",Locale.ENGLISH);
				if (contentValue!=null)
				{
					String autor = contentValue.getStringValue(cms);
					if (autor!=null && autor.trim().length()>0)
					{
						CmsUser cmsUser = cms.readUser(autor);
					
						if (cmsUser!=null)
							page.setAutor(cmsUser.getId().getStringValue());
						else
						{
							cmsUser = cms.readUser(new CmsUUID(autor));
							if (cmsUser!=null)
								page.setAutor(cmsUser.getId().getStringValue());
						}
					}
				}
				//if (page.getAutor()==null) 
				//	page.setAutor(res.getUserCreated().getStringValue());

				
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
		return "Encuestas";
	}

	public String getContentType() {
		try {
			return "" + OpenCms.getResourceManager().getResourceType("encuesta").getTypeId();
		} catch (CmsLoaderException e) {
			e.printStackTrace();
			return "encuesta";
		}
	}

	private boolean getCategories(CmsXmlContent xmlContent, CmsResource res, CmsObject cms)
	{
		List<String> tags = new ArrayList<String>();
		
		int j=1;
				
		String xmlName ="grupo[" + j + "]";
		I_CmsXmlContentValue value = xmlContent.getValue(xmlName, Locale.ENGLISH);
		while (value!=null)
		{
			String categoria = value.getStringValue(cms);
			if (categoria!=null && categoria.trim().length() > 0)
				if (!tags.contains(categoria))
						tags.add(categoria);
	
			j++;
			xmlName ="grupo[" + j + "]";
			value = xmlContent.getValue(xmlName, Locale.ENGLISH);
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
