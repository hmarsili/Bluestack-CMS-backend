package org.opencms.search;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ExtendedQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.search.fields.CmsSearchField;

public class ExtendedIndexManager {

    private static final Log LOG = CmsLog.getLog(ExtendedIndexManager.class);

    
    public CmsSearchIndexSource createIndexSource(String name, List<String> resources, List<String> documentTypes, String indexerClassName) {
    	CmsSearchIndexSource searchIndexSource = new CmsSearchIndexSource();
    	
    	searchIndexSource.setName(name);
    	searchIndexSource.setResourcesNames(resources);
    	searchIndexSource.setDocumentTypes(documentTypes);
    	searchIndexSource.setIndexerClassName(indexerClassName);
    	OpenCms.getSearchManager().addSearchIndexSource(searchIndexSource);
    	
    	return searchIndexSource;
    }
    
    public void removeIndexes(List<String> indexNames) {
    	OpenCms.getSearchManager().removeSearchIndexes(indexNames);
    }
    
    public void removeIndexSources(String name) {
    	CmsSearchIndexSource indexsource = OpenCms.getSearchManager().getIndexSource(name);
    	
    	OpenCms.getSearchManager().removeSearchIndexSource(indexsource);
    }
    
	public CmsSearchIndex creteIndex(String name, String projectName, String rebuildMode, String fieldConfigurationName, List<String> sourceNames, Map<String,Object> configuration, Locale locale) {
		
		CmsSearchIndex searchIndex= new CmsSearchIndex();  
		searchIndex.setName(name);
		searchIndex.setProjectName(projectName);
		searchIndex.setRebuildMode(rebuildMode);
	
		for (String sourceName : sourceNames)
			searchIndex.addSourceName(sourceName);
		
		// <param name="lucene.RAMBufferSizeMB">48.0</param>
        // <param name="org.opencms.search.CmsSearchIndex.indexExpiredContent">true</param>
		 if (configuration.get(CmsSearchIndex.LUCENE_RAM_BUFFER_SIZE_MB)!=null)
			 searchIndex.addConfigurationParameter(CmsSearchIndex.LUCENE_RAM_BUFFER_SIZE_MB, "" + configuration.get(CmsSearchIndex.LUCENE_RAM_BUFFER_SIZE_MB));
		 if (configuration.get(CmsSearchIndex.INDEX_EXPIRED)!=null)
			 searchIndex.addConfigurationParameter(CmsSearchIndex.INDEX_EXPIRED, "" + configuration.get(CmsSearchIndex.INDEX_EXPIRED));
				
		searchIndex.setFieldConfigurationName(fieldConfigurationName);
		
		searchIndex.setLocale(locale);
		OpenCms.getSearchManager().addSearchIndex(searchIndex);
		
		return searchIndex;
	}
	
	public void addFolderToSource(String indexSource, String folder) throws Exception {
		if (!sourceExists(indexSource))
			throw new Exception("IndexSource '" + indexSource + "' not found");
		
		CmsSearchIndexSource source =  OpenCms.getSearchManager().getIndexSource(indexSource);
		
		if (folderExistsInSource(source, folder))
			throw new Exception("Folder '" + folder + "' already exists in source " + indexSource);
		
		source.getResourcesNames().add(folder);
	}

	public void removeFolderFromSource(String indexSource, String folder) throws Exception {
		if (!sourceExists(indexSource))
			throw new Exception("IndexSource '" + indexSource + "' not found");
		
		CmsSearchIndexSource source =  OpenCms.getSearchManager().getIndexSource(indexSource);
		
		if (!folderExistsInSource(source, folder))
			throw new Exception("Folder '" + folder + "' not exists in source " + indexSource);
		
		source.getResourcesNames().remove(folder);
	}

	
	public void updateIndexDefinition() {
		OpenCms.writeConfiguration(CmsSearchConfiguration.class);
	}
	
	protected boolean folderExistsInSource(CmsSearchIndexSource source, String folder) {
		for (String resource :source.getResourcesNames()) {
			if (resource.equals(folder))
				return true;
		}
		return false;
	}
	
	protected boolean sourceExists(String folder) {
		
		for (String source : OpenCms.getSearchManager().getSearchIndexSources().keySet()) {
			if (source.equals(folder))
				return true;
		}
		return false;
	}

	public void removeResourcesFromIndex(String indexName, String condition, boolean commit, boolean tryMerge) {
		CmsSearchIndex index = OpenCms.getSearchManager().getIndex(indexName);
		
		
		Analyzer languageAnalyzer = new SpanishAnalyzer();
    	ExtendedQueryParser qp = new ExtendedQueryParser("content",languageAnalyzer,index.getFieldConfiguration());
    	
    	Query query;
		try {
			query = qp.parse(condition.replaceAll("/", "\\\\/"));

			LOG.info("borrando del indice " + indexName + " aquellos registros que cumplan la condicion: " + query);
	    	IndexWriter writer = index.getIndexWriter(false);

	    	IndexReader reader = index.getSearcher().getIndexReader();    	
	    	int totdocs_before = reader.numDocs();
	    	int deldocs_before = reader.numDeletedDocs();
	    	
	    	writer.deleteDocuments(query);
	    	
	    	reader = index.getSearcher().getIndexReader();    	
	    	int totdocs_after = reader.numDocs();
	    	int deldocs_after = reader.numDeletedDocs();

	    	LOG.debug("Previo commit y merge: Documentos " + totdocs_after + "(" + deldocs_after + ")");
	    	if (commit)
	    		writer.commit();
	    	
	    	if (tryMerge)
	    		writer.maybeMerge();

	    	index.searchManagerRestore();
	    	
	    	reader = index.getSearcher().getIndexReader();    	
	    	int totdocs_final = reader.numDocs();
	    	int deldocs_final = reader.numDeletedDocs();

	    	LOG.info("Borrado de documentos del indice " + indexName + " Inicial: Documentos " + totdocs_before + "(" + deldocs_before + ") - Posterior: Documentos " + totdocs_final + "(" + deldocs_final + ")");
		} catch (ParseException e) {
			LOG.error("unable to parse condition: " + condition + " while shrinking index " + indexName,e);
		} catch (CmsIndexException e) {
			LOG.error("unable get index " + indexName + " while trying to shrink it " ,e);
		} catch (IOException e) {
			LOG.error("Error while shrinking index " + indexName,e);
		}
    	
    	
    
	}
	
	
	public void copyResourcesFromIndexes(CmsObject cmsObject, String sourceIndexName, String destinationIndexName, String condition, boolean commit, boolean tryMerge) {
		CmsSearchIndex sourceIndex = OpenCms.getSearchManager().getIndex(sourceIndexName);
		CmsSearchIndex destinationIndex = OpenCms.getSearchManager().getIndex(destinationIndexName);
		
		
		Analyzer languageAnalyzer = new SpanishAnalyzer();
    	ExtendedQueryParser qp = new ExtendedQueryParser("content",languageAnalyzer,sourceIndex.getFieldConfiguration());
    	
    	Query query;
    	IndexSearcher is=null;
		try {
			query = qp.parse(condition.replaceAll("/", "\\\\/"));

			LOG.info("obteniendo del indice " + sourceIndex + " aquellos registros que cumplan la condicion: " + query);
	    	IndexWriter destWriter = destinationIndex.getIndexWriter(false);
	    		    	
	    	is = sourceIndex.getSearcher();
	    	TopDocs hits = is.search(query,
                    Integer.MAX_VALUE);
            
	    	I_CmsReport report = new CmsLogReport(Locale.ENGLISH, getClass());
	    	CmsIndexingThreadManager threadManager = new CmsIndexingThreadManager(
	    			OpenCms.getSearchManager().getTimeout(),
                    OpenCms.getSearchManager().getMaxModificationsBeforeCommit()
            );
	    	
	    	for (ScoreDoc score : hits.scoreDocs) {
	    		Document doc = is.doc(score.doc);
	    		
	    		CmsResource resource=null;
				try {
					resource = cmsObject.readResource(cmsObject.getRequestContext().removeSiteRoot(doc.getField(CmsSearchField.FIELD_PATH).stringValue()), CmsResourceFilter.IGNORE_EXPIRATION);
					
					
					try {	
						if (report != null) {
			                report.print(org.opencms.report.Messages.get().container(
			                    org.opencms.report.Messages.RPT_SUCCESSION_1,
			                    String.valueOf(threadManager.getCounter() + 1)), I_CmsReport.FORMAT_NOTE);
			                report.print(
			                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_FILE_BEGIN_0),
			                    I_CmsReport.FORMAT_NOTE);
			                report.print(org.opencms.report.Messages.get().container(
			                    org.opencms.report.Messages.RPT_ARGUMENT_1,
			                    report.removeSiteRoot(resource.getRootPath())));
			                report.print(
			                    org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_DOTS_0),
			                    I_CmsReport.FORMAT_DEFAULT);
			            }
						
						CmsObject cloneCms = OpenCms.initCmsObject(cmsObject);
						cloneCms.getRequestContext().setSiteRoot("/");
						threadManager.createIndexingThread(cloneCms, destWriter, resource, destinationIndex, report);
			    		
					} catch (Exception e) {
						if (report != null) {
			                report.println(
			                    Messages.get().container(Messages.RPT_SEARCH_INDEXING_FAILED_0),
			                    I_CmsReport.FORMAT_WARNING);
			            }
			            if (LOG.isWarnEnabled()) {
			                LOG.warn(Messages.get().getBundle().key(
			                    Messages.ERR_INDEX_RESOURCE_FAILED_2,
			                    resource.getRootPath(),
			                    destinationIndex.getName()), e);
			            }
					}
		                
		    		//destWriter.addDocument(doc);

				} catch (CmsException e) {
					LOG.error("Cannot read resource " + doc.getField(CmsSearchField.FIELD_PATH).stringValue(),e);
				}
	    	}				
	    	
	    	while (threadManager.isRunning()) {
                try {
                	LOG.debug("threadManager.isRunning()");
                    wait(1000);
                } catch (InterruptedException e) {
                	LOG.error("Interrupted during incremental index update", e);
                    // just continue with the loop after interruption
                }
            }
	    	
	    	
	    	if (commit)
	    		destWriter.commit();
	    	
	    	if (tryMerge)
	    		destWriter.maybeMerge();

	    	destinationIndex.searchManagerRestore();
	    	
	    	LOG.info("Copiando de documentos del indice " + sourceIndexName + " a " + destinationIndexName + " > Documentos " + hits.totalHits + ".");
		} catch (ParseException e) {
			LOG.error("unable to parse condition: " + condition + " while coping from index " + sourceIndexName  + " to index " + destinationIndexName ,e);
		} catch (CmsIndexException e) {
			LOG.error("unable get index " + sourceIndexName + " or " + destinationIndexName + " while trying to shrink it " ,e);
		} catch (IOException e) {
			LOG.error("Error while copyng from index " + sourceIndexName + " to index " + destinationIndexName,e);
		}
		finally {
			if (is!=null)
				try {
					sourceIndex.releaseSearcher(is);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
    	
    	
    
	}
	
}
