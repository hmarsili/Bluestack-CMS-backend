package com.tfsla.diario.analysis.service.contentRules;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;
import com.tfsla.diario.analysis.model.NewsAnalysis;

public class CR_imagePreview extends A_ContentNewsRule implements I_ContentNewsRule {

	@Override
	public boolean ruleViolation(CmsObject cmsObject, NewsAnalysis analysis) {
		
		CmsMessages messages = getMessages(cmsObject);
		
		String imagePreview = messages.key("ruleViolation.imagePreview.count"); // "Determine una imagen de previsualizacion para la noticia.";
		
		if (analysis.getImagePreviewCount()==0) {
			violationMsg = new BrokenRuleDescription(imagePreview,BrokenRuleDescription.BR_DANGER,   messages.key("ruleViolation.title.SEOOptimization"));
					
			return true;
		}
		
		String imagePreviewTitle = messages.key("ruleViolation.imagePreview.title"); // "Considere agregar un titulo a la imagen de previsualizacion"
		if (analysis.getImagePreviewTitleWordCount()<=3) {
			violationMsg = new BrokenRuleDescription(imagePreviewTitle
					+ " " + messages.key("ruleViolation.imagePreview.title.min.words"),BrokenRuleDescription.BR_DANGER,   messages.key("ruleViolation.title.SEOOptimization")); // "Su longitud debe ser mayor a 3 palabras."
					
			return true;
		}
		
		if (analysis.getImagePreviewTitleTextLength()<=20) {
			violationMsg = new BrokenRuleDescription(imagePreviewTitle
					+ " " + messages.key("ruleViolation.imagePreview.titlee.min.letters"),BrokenRuleDescription.BR_DANGER,   messages.key("ruleViolation.title.SEOOptimization")); // "Su longitud debe ser mayor a 20 letras."
					
			return true;
		}
		
			
		return false;
	}

}
