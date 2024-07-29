package com.tfsla.diario.analysis.service.contentRules;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;
import com.tfsla.diario.analysis.model.NewsAnalysis;

public class CR_Videos extends A_ContentNewsRule implements I_ContentNewsRule {

	@Override
	public boolean ruleViolation(CmsObject cmsObject, NewsAnalysis analysis) {

		CmsMessages messages = getMessages(cmsObject);
		
		String imageGallery = messages.key("ruleViolation.video.content"); // "Considere agregar titulo y tags los videos.";
		
		if (analysis.getVideoFlashCount()>0 && (analysis.isVideoFlashWithNoTitle() || analysis.isVideoFlashWithNoKeywords())) {
			violationMsg =  new BrokenRuleDescription(imageGallery,BrokenRuleDescription.BR_WARNING, messages.key("ruleViolation.title.SEOOptimization"));
			return true;
		}
		
		if (analysis.getVideoEmbeddedCount()>0 && (analysis.isVideoEmbeddedWithNoTitle() || analysis.isVideoEmbeddedWithNoKeywords())) {
			violationMsg =  new BrokenRuleDescription(imageGallery,BrokenRuleDescription.BR_WARNING, messages.key("ruleViolation.title.SEOOptimization"));
			return true;
		}
		
		if (analysis.getVideoYouTubeCount()>0 && (analysis.isVideoYouTubeWithNoTitle() || analysis.isVideoYouTubeWithNoKeywords())) {
			violationMsg =  new BrokenRuleDescription(imageGallery,BrokenRuleDescription.BR_WARNING,messages.key("ruleViolation.title.SEOOptimization"));
			return true;
		}
		
		
		return false;
	}

}
