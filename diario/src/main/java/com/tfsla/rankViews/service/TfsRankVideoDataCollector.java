package com.tfsla.rankViews.service;

import java.util.ArrayList;
import java.util.List;


import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;

import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.I_statisticsDataCollector;

import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

public class TfsRankVideoDataCollector extends A_RankDataCollector implements I_statisticsDataCollector {
	List<TfsKeyValue> values;
	int tagsNumber = 0;

	public TfsKeyValue[] collect(CmsObject cms, CmsResource res, CmsUser user, String sessionId, TfsHitPage page) throws Exception {
		values = new ArrayList<TfsKeyValue>();
		tagsNumber = 0;

		int tipoVideo = OpenCms.getResourceManager().getResourceType("video").getTypeId();
		int tipoVideoLink = OpenCms.getResourceManager().getResourceType("video-link").getTypeId();
		int tipoVideoYoutube = OpenCms.getResourceManager().getResourceType("video-youtube").getTypeId();
		int tipoVideoVimeo = OpenCms.getResourceManager().getResourceType("video-vimeo").getTypeId();
		int tipoVideoEmbedded = OpenCms.getResourceManager().getResourceType("video-embedded").getTypeId();

		if (res.getTypeId() == tipoVideoLink || res.getTypeId() == tipoVideo || res.getTypeId() == tipoVideoYoutube || res.getTypeId() == tipoVideoVimeo || res.getTypeId() == tipoVideoEmbedded) {
			getCategories(res, cms);
			page.setTipoContenido(getContentType());
		} else if (res.getTypeId() == CmsResourceTypePointer.getStaticTypeId()) {
			String link = new String(cms.readFile(res).getContents());
			if (hasVideoExtension(link)) {
				getCategories(res, cms);
				page.setTipoContenido(getContentType());
			}
		} else if (res.getTypeId() == CmsResourceTypeBinary.getStaticTypeId() && hasVideoExtension(res.getName())) {
			String link = new String(cms.readFile(res).getContents());
			if (hasVideoExtension(link)) {
				getCategories(res, cms);
				page.setTipoContenido(getContentType());
			}
		}
		
		TfsKeyValue[] keyArray = new TfsKeyValue[values.size()];
		values.toArray(keyArray);
		
		return keyArray;
	}

	public String getContentName() {
		return "Videos";
	}

	public String getContentType() {
		return "" + CmsResourceTypeBinary.getStaticTypeId() + 1000000;
	}

	private boolean hasVideoExtension(String name) {
		if (name.indexOf(".mpeg")>0)
			return true;

		if (name.indexOf(".mp4")>0)
			return true;
		
		if (name.indexOf(".mpg")>0)
			return true;
		
		if (name.indexOf(".wmv")>0)
			return true;
		
		if (name.indexOf(".avi")>0)
			return true;

		if (name.indexOf(".flv")>0)
			return true;

		return false;
	}
	
	private void getCategories(CmsResource res, CmsObject cms) {
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
						if (tag.trim().length() > 0) {
							catParent += tag.trim() + "/";
							if (!catParent.equals("/system/") && !catParent.equals("/system/categories/") && !catParent.equals("/categories/"))
								if (!tags.contains(catParent))
									tags.add(catParent);
						}

				}
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
}
