package com.tfsla.diario.analysis.service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;

import com.tfsla.diario.analysis.model.BrokenRuleDescription;
import com.tfsla.diario.analysis.model.NewsAnalysis;
import com.tfsla.diario.analysis.service.contentRules.CR_Videos;
import com.tfsla.diario.analysis.service.contentRules.CR_bodyEmbeddedDensity;
import com.tfsla.diario.analysis.service.contentRules.CR_bodyLength;
import com.tfsla.diario.analysis.service.contentRules.CR_imageGallery;
import com.tfsla.diario.analysis.service.contentRules.CR_imagePreview;
import com.tfsla.diario.analysis.service.contentRules.CR_subTitleLength;
import com.tfsla.diario.analysis.service.contentRules.CR_tagsPeopleCount;
import com.tfsla.diario.analysis.service.contentRules.CR_titleLength;
import com.tfsla.diario.analysis.service.contentRules.I_ContentNewsRule;

public class NewsAnalysisService {
	
	private CmsObject cmsObject;
	
	private static List<I_ContentNewsRule> contentRules = null;
	
	static {
		contentRules = new ArrayList<I_ContentNewsRule>();
		contentRules.add(new CR_titleLength());
		contentRules.add(new CR_subTitleLength());
		contentRules.add(new CR_bodyLength());
		contentRules.add(new CR_bodyEmbeddedDensity());
		contentRules.add(new CR_tagsPeopleCount());
		contentRules.add(new CR_imagePreview());
		contentRules.add(new CR_imageGallery());
		contentRules.add(new CR_Videos());
	}
	
	public NewsAnalysisService(CmsObject cmsObject) {
		this.cmsObject = cmsObject;
		
	}
	
	public List<BrokenRuleDescription> suggestNewsEnhancements(String newsPath) {
		NewsAnalysis analysis = analyzeNews(newsPath);
		return suggestNewsEnhancements(analysis);
	}
	
	public List<BrokenRuleDescription> suggestNewsEnhancements(NewsAnalysis analysis) {
		List<BrokenRuleDescription> suggestions = new ArrayList<>();
		
		for (I_ContentNewsRule rule : contentRules) {
			if (rule.ruleViolation(cmsObject, analysis)) {
				suggestions.add(rule.ruleMessage());
			}
		}
		
		return suggestions;
	}
	
	private String normalizeText(String text) {
		String string = Normalizer.normalize(text, Normalizer.Form.NFD);
		string = string.replaceAll("[^\\p{ASCII}]", "");
		string = string.toLowerCase();
		return string;
	}
	
	public List<BrokenRuleDescription> analyzeNewsHonor(String newsPath)  {
		String model = "/com/tfsla/diario/analysis/sp-title-clickbait-maxent.bin";
		
		List<BrokenRuleDescription> rules = new ArrayList<>();
		
		I_CmsXmlDocument fileContent = getFileContent(newsPath);
		
		Locale locale = cmsObject.getRequestContext().getLocale();

		try {
			String  titulo = fileContent.getStringValue(cmsObject, "titulo", locale);
		
			
			NlpDocumentCategorizer clickbaitTester = new NlpDocumentCategorizer();
			clickbaitTester.loadTrainedMode(model);
			NlpDocumentCategorizer.Result result = clickbaitTester.categorize(titulo);
			
			if (result.getBestCategory().equals("clickbait") && result.getProba() > 0.51) {
				rules.add(new BrokenRuleDescription("El titulo aparenta ser un clickbait (%" +  new DecimalFormat("###.###").format(100*result.getProba()) + ")", BrokenRuleDescription.BR_WARNING,"Clickbait"));
			}
			
			/*
			titulo = normalizeText(titulo);
		
			if (titulo.contains("no lo vas a poder creer") ||
				titulo.contains("mira lo que le paso") ||
				titulo.contains("te va a hacer") ||
				titulo.contains("adivina") ||
				titulo.contains("la verdadera razon") ||
				titulo.contains("el mal momento de") ||
				titulo.contains("no vas a poder parar")
					) 
				
				rules.add(new BrokenRuleDescription("El titulo aparenta una forma utilizada como carnada",BrokenRuleDescription.BR_WARNING,"Clickbait"));
				
				*/
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return rules;
	}
	
	public NewsAnalysis analyzeNews(String newsPath) {
		NewsAnalysis analysis = new NewsAnalysis();
		
		I_CmsXmlDocument fileContent = getFileContent(newsPath);

		Locale locale = cmsObject.getRequestContext().getLocale();
		String cuerpo,titulo,claves,personas,copete; 
		try {
			cuerpo = fileContent.getStringValue(cmsObject, "cuerpo", locale);
			processBody(analysis, cuerpo);
			
			titulo = fileContent.getStringValue(cmsObject, "titulo", locale);
			processTitle(analysis, titulo);
			
			claves = fileContent.getStringValue(cmsObject, "claves", locale);
			processKeyWords(analysis,claves);
			
			personas = fileContent.getStringValue(cmsObject, "personas", locale);
			processPeople(analysis,personas);
			
			copete = fileContent.getStringValue(cmsObject, "copete", locale);
			processSubTitle(analysis,copete);
			
			processImagePreview(analysis, fileContent);
			processImageGallery(analysis, fileContent);
			
			processVideoYouTube(analysis, fileContent);
			processVideoEmbedded(analysis, fileContent);
			processVideoFlash(analysis, fileContent);
			
		} catch (CmsXmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return analysis;
	}

	
	private void processVideoFlash(NewsAnalysis analysis, I_CmsXmlDocument fileContent) {
		Locale locale = cmsObject.getRequestContext().getLocale();
		
		int videoFlashCount = 0;
		boolean hasVideoFlashWithNoImage = false;
		boolean hasVideoFlashWithNoTitle = false;
		boolean hasVideoFlashWithNoKeywords = false;
		for (int j=1; j<=fileContent.getValues("videoFlash", locale).size();j++) {
			try {
				String id = fileContent.getStringValue(cmsObject, "videoFlash[" + j + "]" + "/video[1]", locale);
				if (id.trim().length()>0){
					videoFlashCount++;
					
						String descripcion = fileContent.getStringValue(cmsObject, "videoFlash[" + j + "]" + "/titulo[1]", locale);
						if (descripcion.trim().length()==0)
							hasVideoFlashWithNoTitle = true;
						
						String keywords = fileContent.getStringValue(cmsObject, "videoFlash[" + j + "]" + "/keywords[1]", locale);
						if (keywords.length()==0)
							hasVideoFlashWithNoKeywords = true;
	
						String imagen = fileContent.getStringValue(cmsObject, "videoFlash[" + j + "]" + "/imagen[1]", locale);
						if (imagen.length()==0)
							hasVideoFlashWithNoImage = true;
				}

			} catch (CmsXmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		analysis.setVideoFlashCount(videoFlashCount);
		analysis.setVideoFlashWithNoTitle(hasVideoFlashWithNoTitle);
		analysis.setVideoFlashWithNoKeywords(hasVideoFlashWithNoKeywords);
		analysis.setVideoFlashWithNoImage(hasVideoFlashWithNoImage);

	}
	
	private void processVideoEmbedded(NewsAnalysis analysis, I_CmsXmlDocument fileContent) {
		Locale locale = cmsObject.getRequestContext().getLocale();
		
		int videoEmbeddedCount = 0;
		boolean hasVideoEmbeddedWithNoImage = false;
		boolean hasVideoEmbeddedWithNoTitle = false;
		boolean hasVideoEmbeddedWithNoKeywords = false;
		for (int j=1; j<=fileContent.getValues("videoEmbedded", locale).size();j++) {
			try {
				
				String codigo = fileContent.getStringValue(cmsObject, "videoEmbedded[" + j + "]" + "/codigo[1]", locale);
				
				if (codigo.trim().length()>0){
					videoEmbeddedCount++;
					
					String descripcion = fileContent.getStringValue(cmsObject, "videoEmbedded[" + j + "]" + "/titulo[1]", locale);
					if (descripcion.trim().length()==0)
						hasVideoEmbeddedWithNoTitle = true;
					
					String keywords = fileContent.getStringValue(cmsObject, "videoEmbedded[" + j + "]" + "/keywords[1]", locale);
					if (keywords.length()==0)
						hasVideoEmbeddedWithNoKeywords = true;

					String imagen = fileContent.getStringValue(cmsObject, "videoEmbedded[" + j + "]" + "/imagen[1]", locale);
					if (imagen.length()==0)
						hasVideoEmbeddedWithNoImage = true;
	
				
				}
			} catch (CmsXmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		analysis.setVideoEmbeddedCount(videoEmbeddedCount);
		analysis.setVideoEmbeddedWithNoTitle(hasVideoEmbeddedWithNoTitle);
		analysis.setVideoEmbeddedWithNoKeywords(hasVideoEmbeddedWithNoKeywords);
		analysis.setVideoEmbeddedWithNoImage(hasVideoEmbeddedWithNoImage);

	}

	
	private void processVideoYouTube(NewsAnalysis analysis, I_CmsXmlDocument fileContent) {
		Locale locale = cmsObject.getRequestContext().getLocale();

		int videoYouTubeCount = 0;
		boolean hasVideoYouTubeWithNoImage = false;
		boolean hasVideoYouTubeWithNoTitle = false;
		boolean hasVideoYouTubeWithNoKeywords = false;
		for (int j=1; j<=fileContent.getValues("videoYouTube", locale).size();j++) {
			try { 
				String id = fileContent.getStringValue(cmsObject, "videoYouTube[" + j + "]" + "/youtubeid[1]", locale);
				if (id.trim().length()>0){
					videoYouTubeCount++;
					
					String descripcion = fileContent.getStringValue(cmsObject, "videoYouTube[" + j + "]" + "/titulo[1]", locale);
					if (descripcion.trim().length()==0)
						hasVideoYouTubeWithNoTitle = true;
					
					String keywords = fileContent.getStringValue(cmsObject, "videoYouTube[" + j + "]" + "/keywords[1]", locale);
					if (keywords.length()==0)
						hasVideoYouTubeWithNoKeywords = true;

					String imagen = fileContent.getStringValue(cmsObject, "videoYouTube[" + j + "]" + "/imagen[1]", locale);
					if (imagen.length()==0)
						hasVideoYouTubeWithNoImage = true;

											
				}
			} catch (CmsXmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
		analysis.setVideoYouTubeCount(videoYouTubeCount);
		analysis.setVideoYouTubeWithNoTitle(hasVideoYouTubeWithNoTitle);
		analysis.setVideoYouTubeWithNoKeywords(hasVideoYouTubeWithNoKeywords);
		analysis.setVideoYouTubeWithNoImage(hasVideoYouTubeWithNoImage);

	}
	
	private void processImageGallery(NewsAnalysis analysis, I_CmsXmlDocument fileContent) {
		Locale locale = cmsObject.getRequestContext().getLocale();
		
		int imageCount = 0;
		boolean hasImageWithNoDescription = false;
		boolean hasImageWithNoKeywords = false;
		for (int j=1; j<=fileContent.getValues("imagenesFotogaleria", locale).size();j++) {
			try {
				String imagen  = fileContent.getStringValue(cmsObject, "imagenesFotogaleria[" + j + "]" + "/imagen[1]", locale);
				if (imagen.trim().length()>0){
					imageCount++;
				
				
					String descripcion = fileContent.getStringValue(cmsObject, "imagenesFotogaleria[" + j + "]" + "/descripcion[1]", locale);
					if (descripcion.trim().length()==0)
						hasImageWithNoDescription = true;
					
					String keywords = fileContent.getStringValue(cmsObject, "imagenesFotogaleria[" + j + "]" + "/keywords[1]", locale);
					
					if (keywords.length()==0)
						hasImageWithNoKeywords = true;
								
				}
				
			} catch (CmsXmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
		analysis.setImageGalleriesCount(imageCount);
		analysis.setImageGalleriesWithNoDescription(hasImageWithNoDescription);
		analysis.setImageGalleriesWithNoKeywords(hasImageWithNoKeywords);
		
		
		
	}
	
	private void processImagePreview(NewsAnalysis analysis, I_CmsXmlDocument fileContent) throws CmsXmlException {
		Locale locale = cmsObject.getRequestContext().getLocale();
		String imagenesPrev = fileContent.getStringValue(cmsObject, "imagenPrevisualizacion[1]/imagen[1]", locale);
		
		if (imagenesPrev.trim().length()>0){
			analysis.setImagePreviewCount(1);
			
			String descripcion = fileContent.getStringValue(cmsObject, "imagenPrevisualizacion[1]/descripcion[1]", locale);
			
			analysis.setImagePreviewTitleTextLength(descripcion.length());
			if (descripcion.length()>0)
				analysis.setImagePreviewTitleWordCount(descripcion.replaceAll("\t", " ").replaceAll(" +", " ").split(" ").length);
			
			String keywords = fileContent.getStringValue(cmsObject, "imagenPrevisualizacion[1]/keywords[1]", locale);
			
			if (keywords.length()==0)
				analysis.setImagePreviewKeyWordsLength(0);
			else	
				analysis.setImagePreviewKeyWordsLength(keywords.trim().split(", ").length);
		}
		else {
			analysis.setImagePreviewCount(0);
		}
	}

	private void processPeople(NewsAnalysis analysis, String people) {
		people = people.trim();
		if (people.length()==0)
			analysis.setPeopleLength(0);
		else	
			analysis.setPeopleLength(people.trim().split(", ").length);
	}
	
	private void processKeyWords(NewsAnalysis analysis, String keywords) {
		keywords = keywords.trim();
		if (keywords.length()==0)
			analysis.setKeyWordsLength(0);
		else	
			analysis.setKeyWordsLength(keywords.trim().split(", ").length);
	}
	
	private void processTitle(NewsAnalysis analysis, String titulo) {
		analysis.setTitleTextLength(titulo.length());
		if (titulo.length()>0)
			analysis.setTitleWordCount(titulo.replaceAll("\t", " ").replaceAll(" +", " ").split(" ").length);
	}
	
	private void processSubTitle(NewsAnalysis analysis, String subtitle) {
		Document body = Jsoup.parse(subtitle);
		analyzePlainSubtitle(analysis, body);
		analyzeSubtitleParagraphs(analysis, body);
		
	}
	
	private void analyzePlainSubtitle(NewsAnalysis analysis, Document subtitle) {
		String plainBody = subtitle.text();
		analysis.setSubTitleTextLength(plainBody.length());
		analysis.setSubTitleWordCount(plainBody.replaceAll("\t", " ").replaceAll(" +", " ").split(" ").length);
		
	}
	
	private void analyzeSubtitleParagraphs(NewsAnalysis analysis, Document body) {
		Elements ps = body.select("body > p");
		int paragraphs = 0;
		int totalWords =0;
		int totalChars =0;
		int maxWords = 0;
		int maxLength = 0;
		for (Element p : ps) {
			String text = p.text().trim();
			text = text.replaceAll("\t", " ").replaceAll(" +", " ");
			
			
			if (maxLength<text.length())
				maxLength = text.length();
				
			
			if (text.length()>0)
			{
				paragraphs++;
				totalChars += text.length();
				int words = text.split(" ").length;
				if (maxWords<words)
					maxWords=words;
				
				totalWords+=words;
			}
		}
		analysis.setSubTitleNumberParagrapths(paragraphs);
		analysis.setSubTitleLargestParagrapthLength(maxLength);
		analysis.setSubTitleLargestParagrapthWords(maxWords);
		if (paragraphs>0) {
			analysis.setSubTitleMeanParagrapthsLength(totalChars/paragraphs);
			analysis.setSubTitleMeanParagrapthsWordsCount(totalWords/paragraphs);
			
		}
	}
	
	private void processBody(NewsAnalysis analysis, String cuerpo) {
			Document body = Jsoup.parse(cuerpo);
			
			analyzePlainBody(analysis, body);
			analyzeParagraphs(analysis, body);
			
			analysis.setEmbeddedFacebook(body.select("div.ckeditor-fb").size() + body.select("div.ckeditor-ifb").size());
			analysis.setEmbeddedFlick(body.select("div.ck-flickr").size());
			analysis.setEmbeddedInstagram(body.select("div.ck-instagram").size());
			analysis.setEmbeddedPinteres(body.select("div.ck-pinterest").size());
			analysis.setEmbeddedStorify(body.select("div.ck-storify").size());
			analysis.setEmbeddedTwitter(body.select("div.ck-twitter").size());
			analysis.setEmbeddedVine(body.select("div.ck-vine").size());
			analysis.setEmbeddedImage(body.select("figure.image").size());
			analysis.setEmbeddedImageGalleries(body.select("div.ck-image-gallery").size());
			analysis.setEmbeddedImageComparator(body.select("div.ckeditor-comparationimg").size());
			
			analysis.setEmbeddedVideo(body.select("div.ck-video-player").size());
			analysis.setEmbeddedVideoGalleries(body.select("div.ck-video-gallery").size());
			analysis.setEmbeddedAudio(body.select("div.ck-audio-player").size());
			analysis.setEmbeddedAudioGalleries(body.select("div.ck-audio-gallery").size());
			analysis.setEmbeddedYoutube(body.select("div.ck-youtube").size());
			analysis.setEmbeddedPoll(body.select("div.ckeditor-poll").size());
			
	}

	private void analyzePlainBody(NewsAnalysis analysis, Document body) {
		String plainBody = body.text();
		analysis.setBodyTextLength(plainBody.length());
		analysis.setBodyWordCount(plainBody.replaceAll("\t", " ").replaceAll(" +", " ").split(" ").length);
	}

	private void analyzeParagraphs(NewsAnalysis analysis, Document body) {
		Elements ps = body.select("body > p");
		int paragraphs = 0;
		int totalWords =0;
		int totalChars =0;
		int maxWords = 0;
		int maxLength = 0;
		for (Element p : ps) {
			String text = p.text().trim();
			text = text.replaceAll("\t", " ").replaceAll(" +", " ");
			
			
			if (maxLength<text.length())
				maxLength = text.length();
				
			
			if (text.length()>0)
			{
				paragraphs++;
				totalChars += text.length();
				int words = text.split(" ").length;
				if (maxWords<words)
					maxWords=words;
				
				totalWords+=words;
			}
		}
		analysis.setBodyNumberParagrapths(paragraphs);
		analysis.setBodyLargestParagrapthLength(maxLength);
		analysis.setBodyLargestParagrapthWords(maxWords);
		if (paragraphs>0) {
			analysis.setBodyMeanParagrapthsLength(totalChars/paragraphs);
			analysis.setBodyMeanParagrapthsWordsCount(totalWords/paragraphs);
			
		}
	}
	
	private I_CmsXmlDocument getFileContent(String newsPath) {
		I_CmsXmlDocument fileContent = null;
		try {
			CmsFile file = cmsObject.readFile(newsPath);
			fileContent = CmsXmlContentFactory.unmarshal(cmsObject, file);
		} catch (CmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileContent;
	}
}
