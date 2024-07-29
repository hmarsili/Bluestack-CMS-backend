package com.tfsla.diario.analysis.service.contentRules;

import org.opencms.file.CmsObject;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;
import com.tfsla.diario.analysis.model.NewsAnalysis;

public interface I_ContentNewsRule {
	public boolean ruleViolation(CmsObject cmsObject, NewsAnalysis analysis);
	
	public BrokenRuleDescription ruleMessage();
	
}
