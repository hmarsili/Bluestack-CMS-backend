package com.tfsla.diario.analysis.service.contentRules;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;
import com.tfsla.diario.analysis.model.NewsAnalysis;

public class CR_tagsPeopleCount extends A_ContentNewsRule implements I_ContentNewsRule {

	@Override
	public boolean ruleViolation(CmsObject cmsObject, NewsAnalysis analysis) {
		
		CmsMessages messages = getMessages(cmsObject);
		
		String tagsPeopleCount = messages.key("ruleViolation.tagspeople.count"); // "Considere agregar palabras clave y/o personas en la noticia.";
		
		if (analysis.getKeyWordsLength()==0 || analysis.getPeopleLength()==0) {
			violationMsg =  new BrokenRuleDescription(tagsPeopleCount,BrokenRuleDescription.BR_DANGER,   messages.key("ruleViolation.title.SEOOptimization"));
					
			return true;
		}
		return false;
	}

}
