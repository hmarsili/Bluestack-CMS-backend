package com.tfsla.diario.analysis.service.contentRules;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;
import com.tfsla.diario.analysis.model.NewsAnalysis;

public class CR_titleLength extends A_ContentNewsRule implements I_ContentNewsRule {

	public boolean ruleViolation(CmsObject cmsObject, NewsAnalysis analysis) {
		
		CmsMessages messages = getMessages(cmsObject);
		
		String shortTitle = messages.key("ruleViolation.title.Short"); // "El titulo de la nota es corto.";
		String longTitle = messages.key("ruleViolation.title.Long"); //"El titulo de la nota es largo.";
		
		if (analysis.getTitleWordCount()<=3) {
			violationMsg =  new BrokenRuleDescription(shortTitle + " " + 
					messages.key("ruleViolation.title.min.words"),BrokenRuleDescription.BR_WARNING,messages.key("ruleViolation.title.SEOOptimization")); //"Se recomienda que contenga más de 3 palabras";
					
			return true;
		}
		
		if (analysis.getTitleTextLength()<=20) {
			violationMsg =  new BrokenRuleDescription(shortTitle + " " + 
					messages.key("ruleViolation.title.min.letters"),BrokenRuleDescription.BR_WARNING,messages.key("ruleViolation.title.SEOOptimization")); // "Se recomienda que contenga más de 20 letras";
					
			return true;
		}
		
		if (analysis.getTitleTextLength()>=120) {
			violationMsg =  new BrokenRuleDescription(longTitle + " " + 
					messages.key("ruleViolation.title.max.letters"),BrokenRuleDescription.BR_WARNING,messages.key("ruleViolation.title.SEOOptimization")); //"Se recomienda que contenga menos de 120 letras";
					
			return true;
		}
		
		return false;
	}

}
