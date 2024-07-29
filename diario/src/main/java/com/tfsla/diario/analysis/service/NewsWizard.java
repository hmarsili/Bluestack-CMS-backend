package com.tfsla.diario.analysis.service;

import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.es.SpanishLightStemmer;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.amazonaws.services.comprehend.model.DetectSentimentResult;
import com.amazonaws.services.comprehend.model.Entity;
import com.amazonaws.services.comprehend.model.TextSizeLimitExceededException;
import com.tfsla.diario.analysis.model.NewsSuggestion;
import com.tfsla.diario.analysis.model.NewsSuggestion.Term;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.AmzComprehendService;
import com.tfsla.diario.ediciones.services.AmzComprehendService.DocEntity;
import com.tfsla.diario.terminos.data.PersonsDAO;
import com.tfsla.diario.terminos.data.TermsDAO;
import com.tfsla.diario.terminos.data.TermsTypesDAO;
import com.tfsla.diario.terminos.model.TermsTypes;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class NewsWizard {
	
	private static final Log LOG = CmsLog.getLog(NewsWizard.class);
	
	private CmsObject cmsObject;
	
	private boolean usingComprehend;
	private AmzComprehendService comprehend;
	private MltNewsAnalysisService mlt;
	
	private final float weight =0.3f;
	
	public NewsWizard(CmsObject cmsObject) {
		this.cmsObject = cmsObject;

		comprehend = AmzComprehendService.getInstance(cmsObject);
		usingComprehend = comprehend.isAmzComprehendEnabled();
		mlt = new MltNewsAnalysisService();
	}
	
	private float CompositeTermInContent(String term, String text, float maxWeight) {
		float extraWeight=0;	
		String parts[] = term.split(" ");
		int paText = 0;
		int totalParts = parts.length;
		for (String part : parts) {
			if (part.length()<4) {
				totalParts--;
				
			}
			else if (text.indexOf(part) >=0)
				paText++;
		}
		if (totalParts>0)
			extraWeight += maxWeight*(paText/(float)totalParts);
		
		return extraWeight;
	}
	
	private String stemm(String text) {
		SpanishLightStemmer stemmer = new SpanishLightStemmer();
		String textStemmed = text.substring(0, stemmer.stem(text.toCharArray(), text.length()));
		return textStemmed.toLowerCase();
	}
	
	private String normalizeText(String text) {
		String string = Normalizer.normalize(text, Normalizer.Form.NFD);
		string = string.replaceAll("[^\\p{ASCII}]", "");
		string = string.toLowerCase();
		return string;
	}

	private long termTypeId = -1;
	private long getTermTypeId(TipoEdicion tEdicion, CmsObject cmsObject) {
		if (termTypeId!=-1)
			return termTypeId;
		
		CPMConfig config = CmsMedios.getInstance().getCmsParaMediosConfiguration();

		String siteName = OpenCms.getSiteManager().getCurrentSite(cmsObject).getSiteRoot();
		String publication = ""  + tEdicion.getId();
		String type = config.getParam(siteName, publication, "terms","termsType","tags");
		
		
		TermsTypesDAO ttDAO = new TermsTypesDAO ();
		TermsTypes oTermTypes = null;
		Long typeId = new Long(1);
		try {
			oTermTypes = ttDAO.getTermType(type);
			typeId = oTermTypes.getId_termType();
		} catch (Exception e) {
			
		}
		
		termTypeId = typeId;
		
		return typeId;
	}
	
	
	private DocEntity peopleInAmz(String term, List<DocEntity> docEntities) {
		
		List<String> sinonimos=null;
		PersonsDAO pDAO = new PersonsDAO();
		try {
			sinonimos = pDAO.getsynonyms(term);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (DocEntity docEnt : docEntities) {
			if (stemm(docEnt.getName()).equals(stemm(term)))
				return docEnt;
				
			if (sinonimos!=null)
				for (String sinonimo : sinonimos) {
					if (stemm(docEnt.getName()).equals(stemm(sinonimo))) {
						return docEnt;
				}
			
			
			}
			
		}
		return null;
		
	}
	
	private DocEntity tagInAmz(String term, List<DocEntity> docEntities) {
		
		List<String> sinonimos=null;
		TermsDAO tDAO = new TermsDAO();
		try {
			sinonimos = tDAO.getsynonyms(termTypeId,term);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (DocEntity docEnt : docEntities) {
			if (stemm(docEnt.getName()).equals(stemm(term)))
				return docEnt;
				
			if (sinonimos!=null)
				for (String sinonimo : sinonimos) {
					if (stemm(docEnt.getName()).equals(stemm(sinonimo))) {
						return docEnt;
				}
			
			
			}
			
		}
		return null;
		
	}
	
	private boolean isTag(String type) {
		return type.equals("ORGANIZATION") 
				|| type.equals("EVENT") 
				|| type.equals("OTHER") 
				|| type.equals("TITLE");
	}
	
	private boolean isPersona(String type) {
		return type.equals("PERSON");
	}

	public NewsSuggestion analizeText(TipoEdicion tEdicion, String text) throws Exception {
		NewsSuggestion mltSuggestion = mlt.suggest(cmsObject,text,tEdicion);
		
		getTermTypeId(tEdicion,cmsObject);
		
		List<Entity> amzEntities=null;
		if (!usingComprehend) 
			return mltSuggestion;
		
		String limitedText = text;
		//Agregado para que no se pase el tamaño del texto al limite de comprehend
		try {
			if (limitedText.getBytes("UTF8").length>=(5000)) {
				limitedText =  new String(Arrays.copyOfRange(limitedText.getBytes("UTF8"), 0, 5000));
				limitedText = limitedText.substring(0,limitedText.lastIndexOf(" "));
			}
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			LOG.error("Error obteniendo el texto de la noticia ",e1);
		}
		
		//if (limitedText.length()>=(5000-115)) {
		//	limitedText = limitedText.substring(0, (5000-115));
		//	limitedText = limitedText.substring(0,limitedText.lastIndexOf(" "));
		//}
		try {
			amzEntities = comprehend.dectecEntities(limitedText);
		}
		catch (TextSizeLimitExceededException ex) {
			LOG.error("El siguiente texto paso el limit de amazon (size " + limitedText.length() + " ): " + limitedText,ex );
			
			return mltSuggestion;
		}
		
		String normalizedText = normalizeText(limitedText);
		List<DocEntity> docEntities = comprehend.processEntities(amzEntities);
		
		NewsSuggestion enrichedSugg = process(mltSuggestion, normalizedText, docEntities);

		return enrichedSugg;
		
	}
		
	public NewsSuggestion analizeNews(String newsPath) throws Exception {
		
		TipoEdicionService tService = new TipoEdicionService();
		TipoEdicion tEdicion = tService.obtenerTipoEdicion(cmsObject, newsPath);
		getTermTypeId(tEdicion,cmsObject);
		
		NewsSuggestion mltSuggestion = mlt.suggest(cmsObject, tEdicion, newsPath);
		
		List<Entity> amzEntities=null;
		if (!usingComprehend) 
			return mltSuggestion;
		
		CmsFile file = cmsObject.readFile(newsPath);
		String extractedText = comprehend.setResource(file, new String[]{"titulo","copete","cuerpo","noticiaLista[x]/titulo","noticiaLista[x]/cuerpo"});
		
		
		String normalizedText = normalizeText(extractedText);
		
		amzEntities = comprehend.dectecEntities();
		List<DocEntity> docEntities = comprehend.processEntities(amzEntities);
		
		for (DocEntity ent : docEntities) {
			LOG.debug("" + ent);
		}
		
		NewsSuggestion enrichedSugg = process(mltSuggestion, normalizedText, docEntities);
		
		return enrichedSugg;
	}

	private NewsSuggestion process(NewsSuggestion mltSuggestion, String normalizedText, List<DocEntity> docEntities) {
		NewsSuggestion enrichedSugg = new NewsSuggestion();
		enrichedSugg.setQuery(mltSuggestion.getQuery());
		enrichedSugg.setSections(mltSuggestion.getSections());
		enrichedSugg.setCategories(mltSuggestion.getCategories());		
		mltSuggestion.getSimilarNews().forEach(s -> enrichedSugg.addSimilarNews(s));
		
		enrichTags(mltSuggestion, normalizedText, docEntities, enrichedSugg);
		enrichPeople(mltSuggestion, normalizedText, docEntities, enrichedSugg);

		comprehend.getPlaces(docEntities).forEach(p -> enrichedSugg.addLugar(p.getName(), p.getScore()));
		
		DetectSentimentResult sentiment = comprehend.dectedSentiment();
		enrichedSugg.setSentiment(sentiment.getSentiment());
		return enrichedSugg;
	}

	private void addOrReplace(NewsSuggestion enrichedSugg,List<Term> terms, String term, float score, boolean isApproved) {
		String norm = normalizeText(term);
		Term replaceTerm = null;
		boolean foundTerm= false;
		for (Term inTerm : terms) {
			if (norm.equals(normalizeText(inTerm.getTerm()))) {
				foundTerm = true;
				if (inTerm.getWeight()<score)
					replaceTerm = inTerm;
			}
		}
		if (!foundTerm)
			terms.add(enrichedSugg.new Term(term,score,isApproved));
		else if (replaceTerm != null){
			terms.add(enrichedSugg.new Term(term,score,isApproved));
			terms.remove(replaceTerm);
		}
	}
	
	

	float approvedScore = 0.20f;
	float maxLuceneScore = 0.40f;
	float maxComprehendScore = 0.40f;
	
	private void enrichPeople(NewsSuggestion mltSuggestion, String extractedText, List<DocEntity> docEntities,
			NewsSuggestion enrichedSugg) {
		//Primero tomo las personas que estan en ambas estimaciones y las "premio" en el score.
		// Tambien me fijo aquellos que pueden estar en forma parcial en el texto
		for (NewsSuggestion.Term term : mltSuggestion.getPersonas()) {
			DocEntity dEnt = peopleInAmz(term.getTerm(),docEntities);
			if (dEnt!=null) {
				if (isPersona(dEnt.getType()))
					docEntities.remove(dEnt);
				
				LOG.debug("La persona " + term.getTerm() + " se encuentra sugerido por amz (" + dEnt.getScore() + " y en lucene (" + term.getWeight() + ")");
				float score = (
						term.isApproved() ? approvedScore : 0) +
						term.getWeight() * maxLuceneScore +
						dEnt.getScore() * maxComprehendScore;
				
				LOG.debug("Score: " + score + ". esta aprobado" + term.isApproved());
						//weight + (1-weight)*Math.max(dEnt.getScore(), term.getWeight());
				
				//float score = weight + (1-weight)*Math.max(dEnt.getScore(), term.getWeight());
				
				addOrReplace(enrichedSugg,enrichedSugg.getPersonas(), 
						term.getTerm(), 
						score, term.isApproved());
				
				//enrichedSugg.addPersona(term.getTerm(),score);
			}
			else {
				
				LOG.debug("La persona " + term.getTerm() + " no se encuentra sugerido por amz y sin en lucene (" + term.getWeight() + ")");
				float score = (
						term.isApproved() ? approvedScore : 0) +
						term.getWeight() * maxLuceneScore +
						CompositeTermInContent(normalizeText(term.getTerm()), extractedText, maxComprehendScore);

				LOG.debug("Score: " + score + ". esta aprobado" + term.isApproved());

				addOrReplace(enrichedSugg,enrichedSugg.getPersonas(), 
						term.getTerm(), 
						score, term.isApproved());
				
				//enrichedSugg.addPersona(term.getTerm(), (1-weight)*term.getWeight() + CompositeTermInContent(normalizeText(term.getTerm()), extractedText, weight));
				
			}
		}
		
		//Ahora tomo las personas que estan sugeridas por amz pero no por lucene (son "nuevos").
		List<DocEntity> tagEntities = comprehend.getPeople(docEntities);
		for (DocEntity tag : tagEntities) {
			LOG.debug("persona " + tag.getName() + " - exists: " + tag.isExists() + "hasCandidates: " + tag.isHasCandidates() );

			if (tag.isExists()) { //Existe en la base de personas pero no fue sugerido por lucene.
				
				float score = (
						approvedScore) + //existe la persona aprobado, pero no lo sugerio lucene. 
						0 * maxLuceneScore +
						tag.getScore() * maxComprehendScore;
				
				LOG.debug("Score: " + score + ".");
				
				addOrReplace(enrichedSugg,enrichedSugg.getPersonas(), 
						tag.getName(), 
						score, true);
				
				//enrichedSugg.addPersona(tag.getName(), (1-weight)*tag.getScore() + weight);
			}
			else if (tag.getSynonymous()!=null) {
				float score = (
						approvedScore * 2f / 3f) + //existe la persona aprobado como sinonimo, pero no lo sugerio lucene. 
						0 * maxLuceneScore +
						tag.getScore() * maxComprehendScore;
				
				LOG.debug("Score como sinonimo: " + score + ".");
				addOrReplace(enrichedSugg,enrichedSugg.getPersonas(), 
						tag.getSynonymous(), 
						score, true);

			}
			else if (tag.isHasCandidates()) { // No existe en la base de tags pero hay otro con un nombre similar.
				for  (DocEntity.EntityCandidate candidate :tag.getCandidates()) {
					
					
					float score = 
							0 + //Lo agrego como posible candidato. 
							0 * maxLuceneScore +
							tag.getScore() * maxComprehendScore;
					LOG.debug("Score con condidato : " + candidate + ". score:" + score);
				
					addOrReplace(enrichedSugg,enrichedSugg.getPersonas(), 
							tag.getName(), 
							score, false);
					
					score = (
							approvedScore * candidate.getScore()) + //hay un candidato de tag aprobado
							0 * maxLuceneScore +
							tag.getScore() * maxComprehendScore;

					LOG.debug("Score con condidato : " + candidate + ". score:" + score);

					addOrReplace(enrichedSugg,enrichedSugg.getPersonas(), 
							candidate.getName(), 
							score, true);
					
					//enrichedSugg.addPersona(candidate.getName(), candidate.getScore()*tag.getScore());
					//enrichedSugg.addPersona(tag.getName(), 1.05f*tag.getScore());
				}
			}
			else { //La persona no esta no esta en la basa de tags ni tiene candidatos
				//enrichedSugg.addPersona(tag.getName(), tag.getScore());
				
				float score = 
						0 + //Lo agrego como posible candidato. 
						0 * maxLuceneScore +
						tag.getScore() * maxComprehendScore;
				
				LOG.debug("Score solo en aws :" + score);

			
				addOrReplace(enrichedSugg,enrichedSugg.getPersonas(), 
						tag.getName(), 
						score, false);
			}
		}
		
		enrichedSugg.getPersonas().sort((t1,t2) -> ((Float)t2.getWeight()).compareTo((float)t1.getWeight()));

	}
	
	
	
	private void enrichTags(NewsSuggestion mltSuggestion, String extractedText, List<DocEntity> docEntities,
			NewsSuggestion enrichedSugg) {
		
		//Score: 
		// (Si es un tags aprobado o sinonimo de uno: 30%, sino cero) + 
		// Si esta en lucene 35% * scorelucene + 
		// Si esta sugerido por comprehend : 35% * scorecomprehend
		
		//Primero tomo los tags que estan en ambas estimaciones y las "premio" en el score.
		// Tambien me fijo aquellos que pueden estar en forma parcial en el texto
		for (NewsSuggestion.Term term : mltSuggestion.getTags()) {
			DocEntity dEnt = tagInAmz(term.getTerm(),docEntities);
			if (dEnt!=null) {
				if (isTag(dEnt.getType()))
					docEntities.remove(dEnt);
				
				LOG.debug("El tag " + term.getTerm() + " se encuentra sugerido por amz (" + dEnt.getScore() + " y en lucene (" + term.getWeight() + ")");
				
				float score = (
						term.isApproved() ? approvedScore : 0) +
						term.getWeight() * maxLuceneScore +
						dEnt.getScore() * maxComprehendScore;
						
						//weight + (1-weight)*Math.max(dEnt.getScore(), term.getWeight());
				
				addOrReplace(enrichedSugg,enrichedSugg.getTags(), 
						term.getTerm(), 
						score, term.isApproved());

				//enrichedSugg.addTags(term.getTerm(),score);
			}
			else {
				
				float score = (
						term.isApproved() ? approvedScore : 0) +
						term.getWeight() * maxLuceneScore +
						CompositeTermInContent(normalizeText(term.getTerm()), extractedText, maxComprehendScore);
				
				addOrReplace(enrichedSugg,enrichedSugg.getTags(), 
						term.getTerm(), 
						score, term.isApproved());
				
				//enrichedSugg.addTags(term.getTerm(), (1-weight)*term.getWeight() + CompositeTermInContent(normalizeText(term.getTerm()), extractedText, weight));
				
			}
		}
		
		//Ahora tomo los tags que estan sugeridos por amz pero no por lucene (son "nuevos" o son sinonimos).
		List<DocEntity> tagEntities = comprehend.getTags(docEntities);
		for (DocEntity tag : tagEntities) {
			if (tag.isExists()) { //Existe en la base de tags como aprobado. pero no fue sugerido por lucene.
				
				float score = (
						approvedScore) + //existe el tag aprobado, pero no lo sugerio lucene. 
						0 * maxLuceneScore +
						tag.getScore() * maxComprehendScore;
				
				
				addOrReplace(enrichedSugg,enrichedSugg.getTags(), 
						tag.getName(), 
						score, true);
				
				//enrichedSugg.addPersona(tag.getName(), (1-weight)*tag.getScore() + weight);
			}
			else if (tag.getSynonymous()!=null) {
				float score = (
						approvedScore * 2f / 3f) + //existe el  aprobado como sinonimo, pero no lo sugerio lucene. 
						0 * maxLuceneScore +
						tag.getScore() * maxComprehendScore;
				
				
				addOrReplace(enrichedSugg,enrichedSugg.getTags(), 
						tag.getSynonymous(), 
						score, true);

			}
			else if (tag.isHasCandidates()) { // No existe en la base de tags pero hay otro con un nombre similar.
				for  (DocEntity.EntityCandidate candidate :tag.getCandidates()) {
					float score = 
							0 + //Lo agrego como posible candidato. 
							0 * maxLuceneScore +
							tag.getScore() * maxComprehendScore;
					
				
					addOrReplace(enrichedSugg,enrichedSugg.getTags(), 
							tag.getName(), 
							score, false);
					
					score = (
							approvedScore * candidate.getScore()) + //hay un candidato de tag aprobado
							0 * maxLuceneScore +
							tag.getScore() * maxComprehendScore;
					
					addOrReplace(enrichedSugg,enrichedSugg.getTags(), 
							candidate.getName(), 
							score, true);
				}
			}
			else { //El tag no esta no esta en la base de tags ni tiene candidatos
				float score = 
						0 + //Lo agrego como posible candidato. 
						0 * maxLuceneScore +
						tag.getScore() * maxComprehendScore;
				
			
				addOrReplace(enrichedSugg,enrichedSugg.getTags(), 
						tag.getName(), 
						score, false);
			
				//enrichedSugg.addTags(tag.getName(), tag.getScore());
			}
		}
		
		enrichedSugg.getTags().sort((t1,t2) -> ((Float)t2.getWeight()).compareTo((float)t1.getWeight()));
		
	}
	
}
