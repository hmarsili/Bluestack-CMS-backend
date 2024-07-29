package com.tfsla.rankViews.service;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;

import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.I_statisticsDataCollector;

import org.opencms.file.types.CmsResourceTypeExternalImage;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.main.CmsException;

public class TfsRankImageDataCollector extends A_RankDataCollector implements I_statisticsDataCollector {

	List<TfsKeyValue> values;
	int tagsNumber = 0;

	public TfsKeyValue[] collect(CmsObject cms, CmsResource res, CmsUser user, String sessionId, TfsHitPage page) throws Exception {

		values = new ArrayList<TfsKeyValue>();
		tagsNumber = 0;

		
		if (res.getTypeId()==CmsResourceTypeExternalImage.getStaticTypeId()
				|| res.getTypeId()==CmsResourceTypeImage.getStaticTypeId())
		{
			
			getCategories(res, cms);
			
			page.setTipoContenido(getContentType());

		}
		
		TfsKeyValue[] keyArray = new TfsKeyValue[values.size()];
		values.toArray(keyArray);
		
		return keyArray;

	}

	public String getContentName() {
		return "Imagenes";
	}

	public String getContentType() {
		
		return "" + CmsResourceTypeExternalImage.getStaticTypeId();
	}

	private void getCategories(CmsResource res, CmsObject cms)
	{
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
			
			String categories = cms.readPropertyObject(res, "category", false).getValue();
			if (categories!=null) {
				String[] categorias = categories.split("\\|");
				
				for (String categoria : categorias) {
					String[] catParts = categoria.split("/");
					
					String catParent = "/";
					for (String tag : catParts)
						if (tag.trim().length()>0)
						{
							catParent += tag.trim() + "/";
							if (!catParent.equals("/system/") && !catParent.equals("/system/categories/") && !catParent.equals("/categories/"))
								if (!tags.contains(catParent))
									tags.add(catParent);
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

	}

}
