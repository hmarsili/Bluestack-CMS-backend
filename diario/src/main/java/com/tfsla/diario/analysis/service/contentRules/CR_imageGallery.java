package com.tfsla.diario.analysis.service.contentRules;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;
import com.tfsla.diario.analysis.model.NewsAnalysis;

public class CR_imageGallery extends A_ContentNewsRule implements I_ContentNewsRule {

	@Override
	public boolean ruleViolation(CmsObject cmsObject, NewsAnalysis analysis) {

		CmsMessages messages = getMessages(cmsObject);
		
		String imageGallery = messages.key("ruleViolation.imageGallery.content"); // "Considere agregar titulo y tags a las imagenes de fotogaleria.";
		
		if (analysis.getImageGalleriesCount()==0)
			return false;
		
		if (analysis.isImageGalleriesWithNoDescription() || analysis.isImageGalleriesWithNoKeywords()) {
			violationMsg =  new BrokenRuleDescription(imageGallery,BrokenRuleDescription.BR_DANGER,  messages.key("ruleViolation.title.SEOOptimization"));
			return true;
		}		
			
		return false;
	}

}
