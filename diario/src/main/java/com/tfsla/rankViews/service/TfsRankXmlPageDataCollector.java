package com.tfsla.rankViews.service;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;

import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.DataCollectorManager;
import com.tfsla.statistics.service.I_statisticsDataCollector;

import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import java.util.*;

import javax.servlet.jsp.JspException;

public class TfsRankXmlPageDataCollector extends A_RankDataCollector implements I_statisticsDataCollector {

	List<TfsKeyValue> values;
	int tagsNumber = 0;

	public TfsKeyValue[] collect(CmsObject cms, CmsResource res, CmsUser user, String sessionId, TfsHitPage page) throws Exception {
		
		values = new ArrayList<TfsKeyValue>();
		tagsNumber = 0;
		
		boolean isNotImplementedXmlContent = false;
		if (CmsResourceTypeXmlContent.isXmlContent(res)) {
			isNotImplementedXmlContent = true;
			DataCollectorManager dataCollectorManager = DataCollectorManager.getInstance();
			for (Iterator it = dataCollectorManager.getDataCollectors().iterator();it.hasNext();)
			{
				I_statisticsDataCollector dCollector = (I_statisticsDataCollector) it.next(); 
				if (dCollector.getContentType().equals("" + res.getTypeId()))
				{
					isNotImplementedXmlContent = false;
					break;
				}
			}
		}
		
		if ( CmsResourceTypeXmlPage.isXmlPage(res) || isNotImplementedXmlContent)
		{
			getCategories(res, cms);
						
			page.setTipoContenido(getContentType());

		}
		
		TfsKeyValue[] keyArray = new TfsKeyValue[values.size()];
		values.toArray(keyArray);
		
		return keyArray;
	}

	public String getContentName() {
		return "Páginas Generales";
	}

	public String getContentType() {
		return "6";
	}

	private void getCategories(CmsResource res, CmsObject cms)
	{
		List<String> tags = new ArrayList<String>();
		
		try {
			String keywords = cms.readPropertyObject(res, "Keywords", false).getValue();
			if (keywords!=null) {
				String[] categorias = keywords.split(" ");
				
				for (String tag : categorias)
						if (!tags.contains(tag))
							tags.add(tag);
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

	}
	
	public String getValue(CmsObject cms, String uid, String key) throws Exception {
		CmsProperty titleProperty;
		try {
			titleProperty = cms.readPropertyObject(uid, key, false);
		} catch (CmsException e) {
			throw new JspException("Error al intentar acceder a la informacion del archivo.",e);
		}
		return titleProperty.getValue();

	}
}
