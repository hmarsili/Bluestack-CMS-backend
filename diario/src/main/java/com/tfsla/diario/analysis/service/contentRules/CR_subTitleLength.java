package com.tfsla.diario.analysis.service.contentRules;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;
import com.tfsla.diario.analysis.model.NewsAnalysis;

public class CR_subTitleLength extends A_ContentNewsRule implements I_ContentNewsRule {

	@Override
	public boolean ruleViolation(CmsObject cmsObject, NewsAnalysis analysis) {
CmsMessages messages = getMessages(cmsObject);
		
		String shortSubTitle = messages.key("ruleViolation.subtitle.Short"); // "El subtitulo de la nota es corto.";
		String unbalancedSubTitle = messages.key("ruleViolation.subtitle.unbalanced"); //"El subtitulo de la nota es mayor a la mitad de la extension del cuerpo de la nota.";
		
		if (analysis.getSubTitleWordCount()<=60) {
			violationMsg =  new BrokenRuleDescription(shortSubTitle + " " + 
					messages.key("ruleViolation.subtitle.min.words"),BrokenRuleDescription.BR_WARNING,   messages.key("ruleViolation.title.SEOOptimization")); //"Se recomienda que contenga más de 60 palabras";
					
			return true;
		}
		
		if (analysis.getSubTitleTextLength()<=150) {
			violationMsg =  new BrokenRuleDescription(shortSubTitle + " " + 
					messages.key("ruleViolation.subtitle.min.letters"),BrokenRuleDescription.BR_WARNING,   messages.key("ruleViolation.title.SEOOptimization")); // "Se recomienda que contenga más de 150 letras";
					
			return true;
		}
		
		if (analysis.getSubTitleTextLength()*2> analysis.getBodyTextLength()) {
			violationMsg =  new BrokenRuleDescription(unbalancedSubTitle,BrokenRuleDescription.BR_WARNING,  messages.key("ruleViolation.title.SEOOptimization"));
					
			return true;
		}
		
		return false;
	}

}
