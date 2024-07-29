package com.tfsla.diario.analysis.service.contentRules;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;
import com.tfsla.diario.analysis.model.NewsAnalysis;

public class CR_bodyEmbeddedDensity extends A_ContentNewsRule implements I_ContentNewsRule {

	@Override
	public boolean ruleViolation(CmsObject cmsObject, NewsAnalysis analysis) {
		
		CmsMessages messages = getMessages(cmsObject);
		
		String bodyEmbedded = messages.key("ruleViolation.body.embedded.density"); // "El numero de contenido embebido en el cuerpo de la nota desproposionado en relación al texto.";
		
		int embeddedCount = analysis.getEmbeddedAudio() +
				analysis.getEmbeddedAudioGalleries() +
				analysis.getEmbeddedFacebook() +
				analysis.getEmbeddedFlick() +
				analysis.getEmbeddedImage() +
				analysis.getEmbeddedImageGalleries() +
				analysis.getEmbeddedInstagram() +
				analysis.getEmbeddedPinteres() +
				analysis.getEmbeddedPoll() +
				analysis.getEmbeddedStorify() +
				analysis.getEmbeddedTwitter() +
				analysis.getEmbeddedVideo() +
				analysis.getEmbeddedVideoGalleries() +
				analysis.getEmbeddedYoutube();
		
		if (analysis.getBodyNumberParagrapths()+1< embeddedCount) {
			violationMsg = new BrokenRuleDescription(bodyEmbedded, BrokenRuleDescription.BR_DANGER, messages.key("ruleViolation.title.monetization") );
					
			return true;
		}
		
		return false;
	}

}
