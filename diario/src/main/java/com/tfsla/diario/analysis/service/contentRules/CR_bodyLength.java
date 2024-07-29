package com.tfsla.diario.analysis.service.contentRules;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;
import com.tfsla.diario.analysis.model.NewsAnalysis;

public class CR_bodyLength extends A_ContentNewsRule implements I_ContentNewsRule{

	@Override
	public boolean ruleViolation(CmsObject cmsObject, NewsAnalysis analysis) {
		
		CmsMessages messages = getMessages(cmsObject);
		
		String shortBody = messages.key("ruleViolation.body.Short"); // "El cuerpo de la nota es corto.";
		
		if (analysis.getBodyNumberParagrapths()<=3) {
			violationMsg = new BrokenRuleDescription(shortBody + " " + 
					messages.key("ruleViolation.body.min.paragraphs"), BrokenRuleDescription.BR_DANGER, messages.key("ruleViolation.title.monetization") );
			 //"Se recomienda que contenga más de 3 parrafos";
					
			return true;
		}

		if (analysis.getBodyTextLength()<=1200) {
			violationMsg = new BrokenRuleDescription(shortBody + " " + 
					messages.key("ruleViolation.body.min.letters"), BrokenRuleDescription.BR_DANGER, messages.key("ruleViolation.title.monetization") ); //"Se recomienda que contenga más de 1200 caracteres";
					
			return true;
		}

		if (analysis.getBodyWordCount()<=200) {
			violationMsg = new BrokenRuleDescription(shortBody + " " + 
					messages.key("ruleViolation.body.min.words"), BrokenRuleDescription.BR_DANGER, messages.key("ruleViolation.title.monetization") ); //"Se recomienda que contenga más de 200 palabras";
					
			return true;
		}

		return false;
	}

}
