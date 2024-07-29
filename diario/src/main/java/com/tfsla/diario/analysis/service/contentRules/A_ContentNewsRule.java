package com.tfsla.diario.analysis.service.contentRules;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.main.OpenCms;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;

public class A_ContentNewsRule {

	protected BrokenRuleDescription violationMsg = null;

	
	public A_ContentNewsRule() {
		super();
	}

	public BrokenRuleDescription ruleMessage() {
		return violationMsg;
	}

	protected CmsMessages getMessages(CmsObject cmsObject) {
		CmsMultiMessages m_messages = new CmsMultiMessages(cmsObject.getRequestContext().getLocale());
		CmsMessages messages = OpenCms.getWorkplaceManager().getMessages(cmsObject.getRequestContext().getLocale());
	
		m_messages.addMessages(messages);
		
		return m_messages;
	}
}