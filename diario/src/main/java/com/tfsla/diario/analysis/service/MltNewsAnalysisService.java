package com.tfsla.diario.analysis.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.analysis.model.NewsSuggestion;
import com.tfsla.diario.analysis.model.NewsSuggestion.Term;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;
import com.tfsla.diario.terminos.data.PersonsDAO;
import com.tfsla.diario.terminos.data.TermsDAO;
import com.tfsla.diario.terminos.data.TermsTypesDAO;
import com.tfsla.diario.terminos.model.Persons;
import com.tfsla.diario.terminos.model.Terms;
import com.tfsla.diario.terminos.model.TermsTypes;

public class MltNewsAnalysisService {
	
	private static final Log LOG = CmsLog.getLog(NewsWizard.class);
	
	public NewsSuggestion suggest(CmsObject cmsObject, String text, TipoEdicion tEdicion) throws CmsException, Exception {
		
		CmsResourceFilter resourceFilter = CmsResourceFilter.DEFAULT;
		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		adSearch.init(cmsObject);

		List<CmsSearchResult> resultados = findSimilars(adSearch,tEdicion, text, resourceFilter);

		NewsSuggestion sug=generateSuggestion(cmsObject, tEdicion, resultados, resourceFilter, null);
		
		if (sug!=null)
			sug.setQuery(adSearch.getQuery());
		
		return sug;
	}
	
	public NewsSuggestion suggest(CmsObject cmsObject, TipoEdicion tEdicion, String newsPath) throws Exception {
				
		CmsResourceFilter resourceFilter = CmsResourceFilter.DEFAULT;
		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		adSearch.init(cmsObject);
		List<CmsSearchResult> resultados = findSimilars(cmsObject, adSearch,newsPath, resourceFilter);
	
		NewsSuggestion sug=generateSuggestion(cmsObject, tEdicion, resultados, resourceFilter, newsPath);
		if (sug!=null)
			sug.setQuery(adSearch.getQuery());
						
		return sug;
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
	
	Set<String> approvedTags = new HashSet<String>();
	private boolean lookifTagApproved(TipoEdicion tEdicion, CmsObject cmsObject, String tag) {
		
		if (approvedTags.contains(tag))
			return true;
		
		long typeId = getTermTypeId(tEdicion,cmsObject);
		TermsDAO termDAO = new TermsDAO();
		try {
			List<Terms> terms = termDAO.getTerminosAprobados(typeId,tag); 
			for (Terms term : terms) {
				if (term.getName().toLowerCase().equals(tag.toLowerCase())) {
					approvedTags.add(tag);
					return true;
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return false;
	}

	Set<String> approvedPeople = new HashSet<String>();
	private boolean lookifPersonApproved(CmsObject cmsObject, String person) {
		
		if (approvedPeople.contains(person))
			return true;
		
		PersonsDAO pDAO = new PersonsDAO();
		
		List<Persons> people;
		try {
			people = pDAO.getPersonas(person);
			for (Persons per : people) {
				if (per.getApproved()==1 && per.getName().toLowerCase().equals(person.toLowerCase())) {
					approvedPeople.add(person);
					return true;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	private NewsSuggestion generateSuggestion(CmsObject cmsObject, TipoEdicion tEdicion, List<CmsSearchResult> resultados, CmsResourceFilter resourceFilter, String originalNews) {
		NewsSuggestion sug=new NewsSuggestion();
		
		TermCounter seccionesCounter = new TermCounter();
		TermCounter categoriasCounter = new TermCounter();
		TermCounter tagsCounter = new TermCounter();
		TermCounter personasCounter = new TermCounter();
		
		float totalWeight = 0f;
		if (resultados!=null) {
			for (CmsSearchResult resultado : resultados) {
				String path = cmsObject.getRequestContext().removeSiteRoot(resultado.getPath());
				try {
					CmsFile noticia = cmsObject.readFile(path,resourceFilter);
					if (originalNews!=null && cmsObject.getSitePath(noticia).equals(originalNews))
						continue;
					
					//Agrego la noticia encontrada
					sug.addSimilarNews(noticia);
					totalWeight+=resultado.getLuceneScore();
					
					//procedo a obtener los diferentes campos a analizar.
					I_CmsXmlDocument noticiaContent = CmsXmlContentFactory.unmarshal(cmsObject, noticia);

					List locales = noticiaContent.getLocales();
					if (locales.size() == 0) {
						locales = OpenCms.getLocaleManager().getDefaultLocales(cmsObject,cmsObject.getSitePath(noticia ));
					}
					Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
					OpenCms.getLocaleManager().getDefaultLocales(cmsObject, cmsObject.getSitePath(noticia)),locales);

					String seccion = noticiaContent.getStringValue(cmsObject, "seccion", locale);
					seccionesCounter.addTerm(seccion,resultado.getLuceneScore());
					
					String tags = noticiaContent.getStringValue(cmsObject, "claves", locale);
					String[] tagsList = tags.split(",");
					for (String tag : tagsList) {
						if (tag.trim().length()>0) {
							tagsCounter.addTerm(tag.trim(),resultado.getLuceneScore());
							lookifTagApproved(tEdicion, cmsObject,tag.trim());
						}
					}
					
					String personas = noticiaContent.getStringValue(cmsObject, "personas", locale);
					String[] personasList = personas.split(",");
					for (String persona : personasList) {
						if (persona.trim().length()>0) {
							personasCounter.addTerm(persona.trim(),resultado.getLuceneScore());
							lookifPersonApproved(cmsObject,persona.trim());
						}
					}
					
					List<I_CmsXmlContentValue>  categorias = noticiaContent.getValues("Categorias", locale);
					int idx=1;
					for (I_CmsXmlContentValue categoria : categorias) {
						String categoryName = noticiaContent.getStringValue(cmsObject, "Categorias[" + idx + "]", locale);
						
						categoriasCounter.addTerm(categoryName,resultado.getLuceneScore());
						
						idx++;
					}
					
							
				} catch (CmsException e) {
				}
			}
		
			seccionesCounter.setGlobalWeigth(totalWeight);
			categoriasCounter.setGlobalWeigth(totalWeight);
			tagsCounter.setGlobalWeigth(totalWeight);
			personasCounter.setGlobalWeigth(totalWeight);
			
			Set<String> empty = new HashSet<String>();
			sug.setSections(seccionesCounter.getSortedTerms(empty));
			sug.setTags(tagsCounter.getSortedTerms(approvedTags));
			sug.setPersonas(personasCounter.getSortedTerms(approvedPeople));
			
		}

		return sug;
	}
	
	private List<CmsSearchResult> findSimilars(CmsObject cmsObject, TfsAdvancedSearch adSearch, String newsPath, CmsResourceFilter resourceFilter)
			throws CmsException, Exception {
		CmsResource resource = cmsObject.readResource(newsPath);
				
		TipoEdicionService tEService = new TipoEdicionService();
		TipoEdicion tEdicion = tEService.obtenerTipoEdicion(cmsObject,resource.getRootPath());
		
		String serarchIndexName = tEdicion.getNoticiasIndexOffline();
		
		
		adSearch.setIndex(serarchIndexName);
		adSearch.setMatchesPerPage(50);
		adSearch.setResFilter(resourceFilter);
		
		String[] fieldNames = {"cuerpo","copete","titulo"};
		List<CmsSearchResult> resultados = adSearch.moreLikeThis(resource,fieldNames);
		
		return resultados;
	}

	private List<CmsSearchResult> findSimilars(TfsAdvancedSearch adSearch, TipoEdicion tEdicion, String text, CmsResourceFilter resourceFilter)
			throws CmsException, Exception {
		
		String serarchIndexName = tEdicion.getNoticiasIndexOffline();
		
		
		adSearch.setIndex(serarchIndexName);
		adSearch.setMatchesPerPage(50);
		adSearch.setResFilter(resourceFilter);
		
		String[] fieldNames = {"cuerpo","copete","titulo"};
		List<CmsSearchResult> resultados = adSearch.moreLikeThis(text,fieldNames);
		
		return resultados;
	}

	
	private class TermCounter {
		LinkedHashMap<String,Float> terms;
		int totalCount=0;
		float globalWeight=0;
		
		TermCounter() {
			terms = new LinkedHashMap<String,Float>();	
		}
		
		public void addTerm(String term, float weight) {
			Float termWeigth = terms.get(term);
			if (termWeigth==null)
				termWeigth=0f;
			
			termWeigth+=weight;
			
			terms.put(term, termWeigth);
			totalCount++;
		}
		
		public void setGlobalWeigth(float weight) {
			this.globalWeight = weight;
		}
		
		public List<Term> getSortedTerms(Set<String> approved) {
			List<Term> sortedTerms = new ArrayList<Term>();
			
			for (String key : terms.keySet()) {
				//LOG.error("El termino " + key + " se agrega con peso " + (float)terms.get(key) / (float)globalWeight);
				sortedTerms.add(
						new NewsSuggestion() .new Term(key,
								(float)terms.get(key) / (float)globalWeight, approved.contains(key) )
						);
			}
			
			sortedTerms.sort(
					new Comparator<Term>() {

					    public int compare(Term term1, Term term2) {
					      //descending order
					      return -1*Float.compare((term1.isApproved() ? 0 : 1) + term1.getWeight(), (term2.isApproved() ? 0 : 1) +term2.getWeight());
					    }
					}
			);
			
			return sortedTerms;
		}
	}
}
