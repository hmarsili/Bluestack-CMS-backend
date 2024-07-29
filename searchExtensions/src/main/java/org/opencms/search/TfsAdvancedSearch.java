package org.opencms.search;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.apache.commons.text.similarity.LongestCommonSubsequence;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.ExtendedQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldDocs;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchResult;
import org.opencms.search.CmsSearchResultList;
import org.opencms.search.Messages;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.util.CmsStringUtil;

public class TfsAdvancedSearch implements Cloneable {

	public static final Sort SORT_FECHA_EDICION_ASC = new Sort(new SortField[]{new SortField("fechaNoticia", SortField.Type.INT, false), SortField.FIELD_SCORE});
	public static final Sort SORT_FECHA_EDICION_DESC = new Sort(new SortField[]{new SortField("fechaNoticia", SortField.Type.INT, true), SortField.FIELD_SCORE});
	public static final Sort SORT_SECCION = new Sort(new SortField[]{new SortField("seccion",SortField.Type.STRING,false) , SortField.FIELD_SCORE});
	public static final Sort SORT_DEFAULT = Sort.RELEVANCE;

	public static final String[] SORT_NAMES = {"SORT_DEFAULT", "SORT_FECHA_EDICION_DESC", "SORT_FECHA_EDICION_ASC", "SORT_SECCION"};
	private static final Map<String,Sort> SORTS = new HashMap<String,Sort>();
	private static final Map<Sort,String> NAMES = new HashMap<Sort,String>();

	static {

		 SORTS.put("SORT_DEFAULT", SORT_DEFAULT);
		 SORTS.put("SORT_FECHA_EDICION_DESC", SORT_FECHA_EDICION_DESC);
		 SORTS.put("SORT_FECHA_EDICION_ASC", SORT_FECHA_EDICION_ASC);
		 SORTS.put("SORT_SECCION", SORT_SECCION);

		 NAMES.put(SORT_DEFAULT,"SORT_DEFAULT");
		 NAMES.put(SORT_FECHA_EDICION_DESC, "SORT_FECHA_EDICION_DESC");
		 NAMES.put(SORT_FECHA_EDICION_ASC, "SORT_FECHA_EDICION_ASC");
		 NAMES.put(SORT_SECCION, "SORT_SECCION");

	 }

	private static final Log LOG = CmsLog.getLog(TfsAdvancedSearch.class);

	private static IndexSearcher is;

	private transient CmsObject cms;

	
	protected CmsSearchIndex index;
	protected List<CmsSearchIndex> SecundaryIndexes = null;
	
	protected CmsSearchFieldConfiguration fieldConf=null;
	protected Analyzer languageAnalyzer;
	
	protected List<String> stopWords;
	protected Map<CharSequence, Integer> vectorNotaThis;
	protected CharSequence caracteresNotaThis;

	protected String[] fieldNames;
	
	
	protected Sort idxSort = SORT_DEFAULT;
    protected String sortOrder="SORT_DEFAULT";
	protected String query;
	protected Query extraQuery = null;
	protected int matchesPerPage;
	protected int displayPages;
	protected int pageCount;
	protected int maxResults=1000;
	

	protected List results = null;
	protected List<Double> similarities = null;
	
	protected int page=1;
	protected int searchResultCount;
	protected String nextUrl = null;
	protected String prevUrl = null;
	protected CmsResourceFilter resFilter = CmsResourceFilter.DEFAULT;
	
	protected String exactPage = "auto";
	

	public CmsResourceFilter getResFilter() {
		return resFilter;
	}

	public void setResFilter(CmsResourceFilter resFilter) {
		this.resFilter = resFilter;
	}

	protected Exception lastException = null;

	public int getSearchResultCount()
	{
		return searchResultCount;
	}

	public TfsAdvancedSearch() {
		matchesPerPage = 20;
		displayPages = 10;
		languageAnalyzer = new SpanishAnalyzer();
	}

	public void setStopWords(String words) {
		stopWords = new ArrayList<String>();
	
		for (String word : words.split(",")) {
			stopWords.add(word.toLowerCase().trim());	
		}
	}
	
	public List<String> getStopWords(){
		return stopWords;
	}
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setExtraQuery(Query query) {
		this.extraQuery = query;
	}
	
	public Query getExtraQuery() {
		return this.extraQuery;
	}
	
	public void setSecundaryIndex(String indexName) {
		if (CmsStringUtil.isNotEmpty(indexName)) {
			try {
				if (SecundaryIndexes==null) {
					SecundaryIndexes= new ArrayList<>();
				}

				CmsSearchIndex idx = OpenCms.getSearchManager().getIndex(indexName);
				if (idx == null) {
					throw new CmsException(Messages.get().container(
							Messages.ERR_INDEX_NOT_FOUND_1, indexName));
				}
				
				SecundaryIndexes.add(idx);
			} catch (Exception exc) {
				if (LOG.isDebugEnabled()) {
					LOG
							.debug(Messages.get().getBundle().key(
									Messages.LOG_INDEX_ACCESS_FAILED_1,
									indexName), exc);
				}
				lastException = exc;
			}
		}
		
			
	}
	
	public void setIndex(String indexName) {
		//resetLastResult();
		if (CmsStringUtil.isNotEmpty(indexName)) {
			try {
				index = OpenCms.getSearchManager().getIndex(indexName);
				if (index == null) {
					throw new CmsException(Messages.get().container(
							Messages.ERR_INDEX_NOT_FOUND_1, indexName));
				}
			} catch (Exception exc) {
				if (LOG.isDebugEnabled()) {
					LOG
							.debug(Messages.get().getBundle().key(
									Messages.LOG_INDEX_ACCESS_FAILED_1,
									indexName), exc);
				}
				lastException = exc;
			}
		}
	}

	public Analyzer getLanguageAnalyzer() {
		return languageAnalyzer;
	}

	public void setLanguageAnalyzer(Analyzer languageAnalyzer) {
		this.languageAnalyzer = languageAnalyzer;
	}

	public void setMatchesPerPage(int matches) {
		matchesPerPage = matches;
		//resetLastResult();
	}

	public int getMatchesPerPage() {
		return matchesPerPage;
	}

	public int getDisplayPages() {
		return displayPages;
	}

	public void setDisplayPages(int value) {
		displayPages = value;
	}

	public void init(CmsObject cms, CmsSearchFieldConfiguration fieldConf) {
		this.cms = cms;
		this.fieldConf = fieldConf;
		resetLastResult();
	}
	
	public void init(CmsObject cms) {
		this.cms = cms;
		resetLastResult();
	}

	private void resetLastResult() {
		extraQuery = null;
		results = null;
		lastException = null;
		nextUrl = null;
		prevUrl = null;
		page=1;
		idxSort = SORT_DEFAULT;
		sortOrder = "SORT_DEFAULT";
		resFilter = CmsResourceFilter.DEFAULT;
	}

	public String getNextUrl() {
		return nextUrl;
	}

	public String getPreviousUrl() {
		return prevUrl;
	}

	public List getSearchResult() {
		if (cms != null && results == null && index != null
				&& CmsStringUtil.isNotEmpty(query)) {

			try {
				search(null);
				 if (results.size() > 0 && matchesPerPage>0) {
				// results = result;

				 searchResultCount = ((CmsSearchResultList)results).getHitCount();

				 pageCount = searchResultCount / matchesPerPage;
				 if ((searchResultCount % matchesPerPage) != 0) { pageCount++; }

				String url = cms.getRequestContext().getUri()
						+ getSearchParameters() + "&searchPage=";

				  if (page > 1) {
					  prevUrl = url + (page - 1); }

				  if (page < pageCount) {
					  nextUrl = url + (page + 1);
				  }

				 }
				 else {
				 results = Collections.EMPTY_LIST;

				 searchResultCount = 0;
				 pageCount = 0;
				 prevUrl = null;
				 nextUrl = null;

				 }
			} catch (Exception exc) {
				
				LOG.error("Error al realizar busqueda. index: " +index.getName() + ". query: " + query ,exc);
				exc.printStackTrace();
				if (LOG.isDebugEnabled()) {
					LOG.debug(Messages.get().getBundle().key(
							Messages.LOG_SEARCHING_FAILED_0), exc);
				}
				results = null;
				searchResultCount = 0;
				pageCount = 0;
				lastException = exc;
			}
		}
		return results;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	public String getSearchParameters() {
		StringBuffer params = new StringBuffer(256);
		params.append("?action=search&query=");
		params.append(CmsEncoder.encodeParameter(CmsEncoder.escape(getQuery(), CmsEncoder.ENCODING_US_ASCII)));
		params.append("&matchesPerPage=");
		params.append(getMatchesPerPage());
		params.append("&displayPages=");
		params.append(getDisplayPages());
		if (sortOrder!="SORT_DEFAULT")
		{
			params.append("&sort=");
			params.append(sortOrder);
		}
		params.append("&index=");
		params.append(CmsEncoder.encodeParameter(index.getName()));

		return params.toString();
	}

	public int getSearchPage() {
        return page;
    }

	public Map getPageLinks() {
	        Map<Integer, String> links = new TreeMap<Integer, String>();
	        if (pageCount <= 1) {
	            return links;
	        }
	        int startIndex;
	        int endIndex;
	        String link = cms.getRequestContext().getUri() + getSearchParameters() + "&searchPage=";
	        if (getDisplayPages() < 1) {
	            startIndex = 1;
	            endIndex = pageCount;
	        } else  {
	            int currentPage = getSearchPage();
	            int countBeforeCurrent = getDisplayPages() / 2;
	            int countAfterCurrent;
	            if ((currentPage - countBeforeCurrent) < 1) {
	                countBeforeCurrent = currentPage - 1;
	            }
	            countAfterCurrent = getDisplayPages() - countBeforeCurrent - 1;
	            startIndex = currentPage - countBeforeCurrent;
	            endIndex = currentPage + countAfterCurrent;
	            if (endIndex > pageCount) {
	                int delta = endIndex - pageCount;
	                startIndex -= delta;
	                if (startIndex < 1) {
	                    startIndex = 1;
	                }
	                endIndex = pageCount;
	            }
	        }
	        for (int i = startIndex; i <= endIndex; i++) {
	            links.put(new Integer(i), (link + i));
	        }
	        return links;
	    }


	////is.getIndexReader().getTermVector(topN, text)
	public List moreLikeThis(String text, String[] fieldNames) {
		return moreLikeThis(text, fieldNames, false);
	}
	
	
	public List moreLikeThis(String text, String[] fieldNames, boolean calculateSimilarity) {

		int topN = 50;

		IndexSearcher is=null;
		Query query =null;
		try {
			is = index.getSearcher();

			MoreLikeThis mlt = new MoreLikeThis(is.getIndexReader());
			if (fieldNames!=null)
				mlt.setFieldNames(fieldNames);
			mlt.setMaxQueryTerms(topN);
			mlt.setBoost(true);
			mlt.setAnalyzer(languageAnalyzer);
			
			if(languageAnalyzer instanceof StopwordAnalyzerBase){
				mlt.setStopWords(((StopwordAnalyzerBase) languageAnalyzer).getStopwordSet());
			}
			
			query = mlt.like(fieldNames[0], new StringReader(
			        text));
			
			similarities = new ArrayList<>();
			if (calculateSimilarity) {
				
				
				this.fieldNames = fieldNames;
				
				String comparableText = text;
				
				String cuerpoNoticiaProc = preprocessString(comparableText);
				
				vectorNotaThis = Arrays.stream(cuerpoNoticiaProc.split(" ")).collect(Collectors.toMap(
		                character -> character, character -> 1, Integer::sum));
				
		        caracteresNotaThis = comparableText.replaceAll(" ", "");

			}
			search(query, calculateSimilarity);
			
			
			
			
			if (results.size()>0) {
				searchResultCount = ((CmsSearchResultList)results).getHitCount();
			}
			else
			{
				results = Collections.EMPTY_LIST;
				searchResultCount = 0;
			}
		} catch (Exception e) {
			LOG.error("Error al realizar moreLikeThis para el recurso el text pasado" + ". index: " +index.getName() + ". query: " + query ,e);
			e.printStackTrace();
			if (LOG.isDebugEnabled()) {
				LOG.debug(Messages.get().getBundle().key(
						Messages.LOG_SEARCHING_FAILED_0), e);
			}
			results = null;
			searchResultCount = 0;
			pageCount = 0;
			lastException = e;			// TODO Auto-generated catch block
		
		} finally {
	
	        //results = searchResults;
	
	        try {
				index.releaseSearcher(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
	        
	    }
		
		return results;

	}
	
	public List<Double> getSimilarity(){
		return similarities;
	}
	
	public List moreLikeThis(CmsResource res, String[] fieldNames) {
		return moreLikeThis(res, fieldNames, false);
	}
	
	public List moreLikeThis(CmsResource res, String[] fieldNames, boolean calculateSimilarity) {
		
		int topN = 50;
		
		IndexSearcher is=null;
		Query query =null;
		try {
			is = index.getSearcher();
			
			MoreLikeThis mlt = new MoreLikeThis(is.getIndexReader());
			
			if (fieldNames!=null)
				mlt.setFieldNames(fieldNames);
			mlt.setMaxQueryTerms(topN);
			mlt.setBoost(true);
			mlt.setAnalyzer(languageAnalyzer);
			
			if(languageAnalyzer instanceof StopwordAnalyzerBase){
				mlt.setStopWords(((StopwordAnalyzerBase) languageAnalyzer).getStopwordSet());
			}
			
			Integer docId = index.getDocumentId(res.getRootPath());
			
			similarities = new ArrayList<>();
			if (calculateSimilarity) {
				this.fieldNames = fieldNames;
				
				String comparableText = extractComparableContent(index.getDocument(res.getRootPath()), fieldNames);
				
				String cuerpoNoticiaProc = preprocessString(comparableText);
				
				vectorNotaThis = Arrays.stream(cuerpoNoticiaProc.split(" ")).collect(Collectors.toMap(
		                character -> character, character -> 1, Integer::sum));
				
		        caracteresNotaThis = comparableText.replaceAll(" ", "");
				
				
			}
			
			if (docId!=null) {
				query = mlt.like(docId);
				
				search(query, calculateSimilarity);
				
				if (results.size()>0) {
					searchResultCount = ((CmsSearchResultList)results).getHitCount();
					
				}
				else
				{
					results = Collections.EMPTY_LIST;
					searchResultCount = 0;
				}
			}
			else {
				results = Collections.EMPTY_LIST;
				searchResultCount = 0;				
			}
		} catch (Exception e) {
			LOG.error("Error al realizar moreLikeThis para el recurso " + res.getRootPath() + ". index: " +index.getName() + ". query: " + query ,e);
			e.printStackTrace();
			if (LOG.isDebugEnabled()) {
				LOG.debug(Messages.get().getBundle().key(
						Messages.LOG_SEARCHING_FAILED_0), e);
			}
			results = null;
			searchResultCount = 0;
			pageCount = 0;
			lastException = e;			// TODO Auto-generated catch block
		
		} finally {
	
	        //results = searchResults;
	
	        try {
				index.releaseSearcher(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
	        
	    }
		
		
		return results;		
	}
	
	private String extractComparableContent(Document doc, String[] fieldNames) {
		String text="";
		
		if (fieldNames!=null) {
			for (String fieldName : fieldNames) {
				text += doc.getField(fieldName).stringValue() + " ";
			}
		}
		return text;
	}
	
	private String preprocessString(String text) {
		
		//System.out.println(text);
		String nuevoTexto = StringUtils.stripAccents(text.toLowerCase());
		nuevoTexto = nuevoTexto.replaceAll("[^\\w\\s]"," ");
		
		
		for (String word : stopWords) {
			nuevoTexto = nuevoTexto.replaceAll("^" + word.toLowerCase().trim() +" ", "");
			nuevoTexto = nuevoTexto.replaceAll("[^\\w]" + word.toLowerCase().trim() +" ", " ");
		}
		nuevoTexto = nuevoTexto.replaceAll("^\\s+", "");
		nuevoTexto = nuevoTexto.replaceAll("\\s\\s+", " ");
		
	
		return nuevoTexto;
	}
	
	private void search(Query execQuery) throws Exception
	{
		 search(execQuery, false );
	}

	private void search(Query execQuery, boolean calculateSimilarity ) throws Exception
	{


		CmsRequestContext context = cms.getRequestContext();
		CmsProject currentProject = context.currentProject();
        //IndexSearcher searcher = null;

//        Hits hits;
        CmsSearchResultList searchResults = new CmsSearchResultList();
        int previousPriority = Thread.currentThread().getPriority();
        	
        IndexSearcher[] is = new IndexSearcher[1+(SecundaryIndexes!=null ? SecundaryIndexes.size() : 0)];
        TopFieldDocs[] hitsArray = new TopFieldDocs[1+(SecundaryIndexes!=null ? SecundaryIndexes.size() : 0)];
        TopFieldDocs hits = null;
        try {

            context.setCurrentProject(cms.readProject(index.getProject()));

            
            
            //String indexPath = index.getPath();
            
            
            Query query =null;
            
            if (execQuery!=null) {
            	query = execQuery;
            	this.query = query.toString();
            } else {
            	ExtendedQueryParser qp = new ExtendedQueryParser("content",languageAnalyzer,fieldConf);

            	query = qp.parse(this.query.replaceAll("/", "\\\\/"));
            	
            	if (extraQuery!=null) {
	            	BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
	        		
	        		booleanQuery.add(query, BooleanClause.Occur.MUST);
	        		booleanQuery.add(extraQuery, BooleanClause.Occur.MUST);
	        		
	        		query = booleanQuery.build();
            	}
            }
            
            LOG.debug(query + " - " + idxSort);
            is[0] = index.getSearcher();
            hitsArray[0] = is[0].search(query,
                    maxResults,
                    idxSort,
                    true,
                    true);

            
            
            if (SecundaryIndexes!=null) {
            	int pos=1;
            	for (CmsSearchIndex secIndex : SecundaryIndexes) {
            		is[pos] = secIndex.getSearcher();
            		
            		hitsArray[pos] = is[pos].search(query,
                            maxResults,
                            idxSort,
                            true,
                            true);
            		pos ++;
            	}
            	
            	hits = TopFieldDocs.merge(idxSort, maxResults, hitsArray);
            }
            else {
            	hits = hitsArray[0];
            }
            
            LOG.debug("resultados encontrados:" + hits.totalHits);
            int hitCount = Math.min(hits.totalHits, maxResults);


            Document doc;
            CmsSearchResult searchResult;
            String excerpt = null;
  
            if (hits != null) {
                int start = -1;
                int end = -1;
                if (matchesPerPage > 0 && page > 0 && hitCount > 0) {
                    start = matchesPerPage * (page - 1);
                    end = start + matchesPerPage;
                    start = (start > hitCount) ? hitCount : start;
                    end = (end > hitCount) ? hitCount : end;
                } else  {
                    start = 0;
//                    end = hitCount;
                    end = hitCount;
                }
                int visibleHitCount = hitCount;
                
                LOG.debug("exactPage:" + exactPage);
                if (exactPage.toLowerCase().equals("true") || exactPage.toLowerCase().equals("auto") && page<10) {
	                for (int i = 0, cnt = 0; i < hitCount && cnt < end; i++) {
	                    try {
	                    	doc = is[(SecundaryIndexes!=null ? hits.scoreDocs[i].shardIndex : 0)].doc(hits.scoreDocs[i].doc);
	                    	
		                    	if (hasReadPermission(cms, doc)) {
		                    		if (cnt >= start) {                            	
		                            	
		                            	
		                        	  excerpt = "<b>" + ( doc.getField("seccion")!=null ? doc.getField("seccion").stringValue() : "") + "<b> - "+ ( doc.getField("titulo")!=null ? doc.getField("titulo").stringValue()  : "") ;
		                        	  searchResult = new CmsSearchResult(
		                        			  Math.round((hits.scoreDocs[i].score / hits.getMaxScore()) * 100f), 
		                        			  hits.scoreDocs[i].score,
		                        			  doc, 
		                        			  excerpt);
		                              searchResults.add(searchResult);
		                              
		                              //Aca agregar el calculo de similaridad
		                              
		                              if (calculateSimilarity) {
		                            	  this.similarities.add(calculateSimilarity(doc));
	
		                              }
		                              
		                            }
		                            cnt++;
		                        } else  {
		                            visibleHitCount--;
		                        }
	                    	
	                    } catch (Exception e) {
	                        if (LOG.isWarnEnabled()) {
	                            LOG.warn(Messages.get().getBundle().key(Messages.LOG_RESULT_ITERATION_FAILED_0), e);
	                        }
	                    }
	                }
                }
                else {
                	LOG.debug("mostrando resultados desde " + start + " hasta " + end + " sobre un total de " + hits.scoreDocs.length );
                	for (int i = start; i < hitCount && i < end; i++) {
	                    try {
	                    	doc = is[(SecundaryIndexes!=null ? hits.scoreDocs[i].shardIndex : 0)].doc(hits.scoreDocs[i].doc);	                    	
	                    	if (hasReadPermission(cms, doc)) {
	                    			
	                        	  excerpt = "<b>" + ( doc.getField("seccion")!=null ? doc.getField("seccion").stringValue() : "") + "<b> - "+ ( doc.getField("titulo")!=null ? doc.getField("titulo").stringValue()  : "") ;
	                        	  searchResult = new CmsSearchResult(
	                        			  Math.round((hits.scoreDocs[i].score / hits.getMaxScore()) * 100f), 
	                        			  hits.scoreDocs[i].score,
	                        			  doc, 
	                        			  excerpt);
	                              searchResults.add(searchResult);
	                            
	                        } else  {
	                            visibleHitCount--;
	                        }
	                    	
	                    } catch (Exception e) {
	                        if (LOG.isWarnEnabled()) {
	                            LOG.warn(Messages.get().getBundle().key(Messages.LOG_RESULT_ITERATION_FAILED_0), e);
	                        }
	                    }
	                }
                }
                searchResults.setHitCount(visibleHitCount);
            } else  {
                searchResults.setHitCount(0);
            }
        } catch (Exception exc) {
            throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1), exc);
        } finally {

            // re-set thread to previous priority
            Thread.currentThread().setPriority(previousPriority);
            results = searchResults;

            index.releaseSearcher(is[0]);
            if (SecundaryIndexes!=null) {
            	int pos=1;
            	for (CmsSearchIndex secIndex : SecundaryIndexes) {
            		secIndex.releaseSearcher(is[pos]);
            		pos ++;
            	}
            	
            	//hits = TopFieldDocs.merge(idxSort, maxResults, hitsArray);
            }

            // switch back to the original project
            context.setCurrentProject(currentProject);
        }

	}
	
	private Double calculateSimilarity(Document doc) {
		CosineSimilarity documentsSimilarity = new CosineSimilarity();
		LongestCommonSubsequence longComSub = new LongestCommonSubsequence();
	        
		String comparableText = extractComparableContent(doc, fieldNames);
			
		String cuerpoNoticiaProc = preprocessString(comparableText);
			
			
			//vectorNotaThis;
			//caracteresNotaThis;
		Map<CharSequence, Integer> vectorNotaCandidate = Arrays.stream(cuerpoNoticiaProc.split(" ")).collect(Collectors.toMap(
	                character -> character, character -> 1, Integer::sum));
			
		CharSequence caracteresNotaCandidate = comparableText.replaceAll(" ", "");
		

		Double docABCosSimilarity = documentsSimilarity.cosineSimilarity(vectorNotaThis, vectorNotaCandidate);
		
		int longitudcCompartida = longComSub.apply(caracteresNotaThis, caracteresNotaCandidate);

		Double porcSim = (double)longitudcCompartida / (double)Math.max(caracteresNotaCandidate.length(), caracteresNotaThis.length());

	
		return (porcSim * 0.40 + docABCosSimilarity * 0.60);
	
	}

	protected boolean hasReadPermission(CmsObject cms, Document doc) {
        IndexableField typeField = doc.getField(CmsSearchField.FIELD_TYPE);
        IndexableField pathField = doc.getField(CmsSearchField.FIELD_PATH);
        
        if ((typeField == null) || (pathField == null)) {
            return true;
        }
        String rootPath = cms.getRequestContext().removeSiteRoot(pathField.stringValue());
        
        return cms.existsResource(rootPath,resFilter);
    }

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public Exception getLastException()
	{
		return lastException;
	}

	public void setSortOrder(String sortOrder) {
		idxSort = SORTS.get(sortOrder);
		this.sortOrder = sortOrder;
	}

	public void setSortOrder(Sort sort)
	{
		idxSort = sort;
		sortOrder = NAMES.get(sort);
	}

	public String getSortOrder()
	{
		return sortOrder;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public String getExactPage() {
		return exactPage;
	}

	public void setExactPage(String exactPage) {
		if (exactPage!=null)
			this.exactPage = exactPage;
	}

}

