package com.tfsla.diario.admin.brain.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.TfsAdvancedSearch;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.diario.analysis.model.NewsSuggestion;
import com.tfsla.diario.analysis.model.NewsSuggestion.Term;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

public class NewsWizard {
	
	public NewsSuggestion suggest(CmsObject cmsObject, String text, TipoEdicion tEdicion) throws CmsException, Exception {
		
		CmsResourceFilter resourceFilter = CmsResourceFilter.DEFAULT;
		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		adSearch.init(cmsObject);

		List<CmsSearchResult> resultados = findSimilars(adSearch,tEdicion, text, resourceFilter);

		NewsSuggestion sug=generateSuggestion(cmsObject, resultados, resourceFilter, null);
		
		if (sug!=null)
			sug.setQuery(adSearch.getQuery());
		
		return sug;
	}
	
	public NewsSuggestion suggest(CmsObject cmsObject, String newsPath) throws Exception {
				
		CmsResourceFilter resourceFilter = CmsResourceFilter.DEFAULT;
		
		TfsAdvancedSearch adSearch = new TfsAdvancedSearch();
		adSearch.init(cmsObject);
		List<CmsSearchResult> resultados = findSimilars(cmsObject, adSearch,newsPath, resourceFilter);
	
		NewsSuggestion sug=generateSuggestion(cmsObject, resultados, resourceFilter, newsPath);
		if (sug!=null)
			sug.setQuery(adSearch.getQuery());
						
		return sug;
	}

	private NewsSuggestion generateSuggestion(CmsObject cmsObject, List<CmsSearchResult> resultados, CmsResourceFilter resourceFilter, String originalNews) {
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
						if (tag.trim().length()>0)
							tagsCounter.addTerm(tag.trim(),resultado.getLuceneScore());
					}
					
					String personas = noticiaContent.getStringValue(cmsObject, "personas", locale);
					String[] personasList = personas.split(",");
					for (String persona : personasList) {
						if (persona.trim().length()>0)
							personasCounter.addTerm(persona.trim(),resultado.getLuceneScore());
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
			
			sug.setSections(seccionesCounter.getSortedTerms());
			sug.setCategories(categoriasCounter.getSortedTerms());
			sug.setTags(tagsCounter.getSortedTerms());
			sug.setPersonas(personasCounter.getSortedTerms());
			
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
		
		public List<Term> getSortedTerms() {
			List<Term> sortedTerms = new ArrayList<Term>();
			
			for (String key : terms.keySet())
				sortedTerms.add(
						new NewsSuggestion() .new Term(key,
								(float)terms.get(key) / (float)globalWeight)
			);
			
			sortedTerms.sort(
					new Comparator<Term>() {

					    public int compare(Term term1, Term term2) {
					      //descending order
					      return -1*Float.compare(term1.getWeight(), term2.getWeight());
					    }
					}
			);
			
			return sortedTerms;
		}
	}
}
