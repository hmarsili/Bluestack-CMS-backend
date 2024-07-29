/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/CmsSearchIndex.java,v $
 * Date   : $Date: 2011/03/23 14:52:47 $
 * Version: $Revision: 1.85 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.documents.I_CmsDocumentFactory;
import org.opencms.search.documents.I_CmsTermHighlighter;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.search.indexExcludeCondition.EvalIdxConditionVisitor;
import org.opencms.search.indexExcludeCondition.idxConditionLexer;
import org.opencms.search.indexExcludeCondition.idxConditionParser;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.logging.Log;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.queries.TermsQuery;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAFDirectory;

/**
 * Implements the search within an index and the management of the index configuration.<p>
 * 
 * @author Alexander Kandzior 
 * @author Carsten Weinholz
 * 
 * @version $Revision: 1.85 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSearchIndex implements I_CmsConfigurationParameterHandler {

 
    /** Constant for additional parameter to enable optimized full index regeneration (default: false). */
    public static final String BACKUP_REINDEXING = CmsSearchIndex.class.getName() + ".useBackupReindexing";

    /** Constant for additional parameter to enable excerpt creation (default: true). */
    public static final String EXCERPT = CmsSearchIndex.class.getName() + ".createExcerpt";

    /** Constant for additional parameter for index content extraction. */
    public static final String EXTRACT_CONTENT = CmsSearchIndex.class.getName() + ".extractContent";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_AUTO_COMMIT = "lucene.AutoCommit";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_MAX_MERGE_DOCS = "lucene.MaxMergeDocs";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_MERGE_FACTOR = "lucene.MergeFactor";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_RAM_BUFFER_SIZE_MB = "lucene.RAMBufferSizeMB";

    /** Constant for additional parameter for the Lucene index setting. */
    public static final String LUCENE_USE_COMPOUND_FILE = "lucene.UseCompoundFile";

    /** Constant for additional parameter for controlling how many hits are loaded at maximum (default: 1000). */
    public static final String MAX_HITS = CmsSearchIndex.class.getName() + ".maxHits";

    /** Indicates how many hits are loaded at maximum by default. */
    public static final int MAX_HITS_DEFAULT = 5000;

    /** Constant for additional parameter to enable permission checks (default: true). */
    public static final String PERMISSIONS = CmsSearchIndex.class.getName() + ".checkPermissions";

    /** Constant for additional parameter to set the thread priority during search. */
    public static final String PRIORITY = CmsSearchIndex.class.getName() + ".priority";

    /** Automatic ("auto") index rebuild mode. */
    public static final String REBUILD_MODE_AUTO = "auto";

    /** Manual ("manual") index rebuild mode. */
    public static final String REBUILD_MODE_MANUAL = "manual";

    /** Offline ("offline") index rebuild mode. */
    public static final String REBUILD_MODE_OFFLINE = "offline";

    /** Constant for additional parameter to enable time range checks (default: true). */
    public static final String TIME_RANGE = CmsSearchIndex.class.getName() + ".checkTimeRange";

    /** Constant for additional parameter to index expired content (default: false). */
    public static final String INDEX_EXPIRED = CmsSearchIndex.class.getName() + ".indexExpiredContent";

    /** Constant for additional parameter to index expired content (default: false). */
    public static final String BUILD_HISTORICAL = CmsSearchIndex.class.getName() + ".buildHistorical";

    /** Constant for years max range span in document search. */
    public static final int MAX_YEAR_RANGE = 25;

 
    /** Constant for a field list that contains the "meta" field as well as the "content" field. */
    static final String[] DOC_META_FIELDS = new String[] {CmsSearchField.FIELD_META, CmsSearchField.FIELD_CONTENT};

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSearchIndex.class);

    /** The list of configured index sources. */
    protected List<CmsSearchIndexSource> m_sources;

    /** The configured Lucene analyzer used for this index. */
    private Analyzer m_analyzer;

    /** Indicates if backup re-indexing is used by this index. */
    private boolean m_backupReindexing;

    /** The permission check mode for this index. */
    private boolean m_checkPermissions;

    /** The time range check mode for this index. */
    private boolean m_checkTimeRange;

    /** Indicates if expired content must be indexed. */
    private boolean m_indexExpirerContent;
    
    private boolean m_indexBuildHistorical;
    
    /** The excerpt mode for this index. */
    private boolean m_createExcerpt;

    /** Map of display query filters to use. */
    private Map<String, TermsQuery> m_displayFilters;

    /** Document types of folders/channels. */
    private Map<String, List<String>> m_documenttypes;

    /** An internal enabled flag, used to disable the index if for instance the configured project does not exist. */
    private boolean m_enabled;

    /** The content extraction mode for this index. */
    private boolean m_extractContent;

    /** The search field configuration of this index. */
    private CmsSearchFieldConfiguration m_fieldConfiguration;

    /** The name of the search field configuration used by this index. */
    private String m_fieldConfigurationName;

    /** The locale of this index. */
    private Locale m_locale;

    /** The exclude condition of a resource from the index */
    private String m_excludeCondition;
    
    /** The Lucene index merge factor setting, see {@link IndexWriter#setMaxMergeDocs(int)}. */
    private Integer m_luceneMaxMergeDocs;

    /** The Lucene index merge factor setting, see {@link IndexWriter#setMergeFactor(int)}. */
    private Integer m_luceneMergeFactor;

    /** The Lucene index RAM buffer size, see {@link IndexWriter#setRAMBufferSizeMB(double)}. */
    private Double m_luceneRAMBufferSizeMB;

    /** The Lucene index setting that controls, see {@link IndexWriter#setUseCompoundFile(boolean)}.  */
    private Boolean m_luceneUseCompoundFile;

    /** Indicates how many hits are loaded at maximum. */
    private int m_maxHits;

    /** The name of this index. */
    private String m_name;

    /** The path where this index stores it's data in the "real" file system. */
    private String m_path;

    /** The thread priority for a search. */
    private int m_priority;

    /** The project of this index. */
    private String m_project;

    /** The rebuild mode for this index. */
    private String m_rebuild;

    /** The Lucene index writer to use. */
    private IndexWriter m_indexWriter;
    
    /** The Lucene index searcher to use. */
    
    private SearcherManager m_searcherManager;

    /** The configured sources for this index. */
    private List<String> m_sourceNames;

    /** Look table to quickly zero-pad days / months in date Strings. */
    public static final String[] DATES = new String[] {
        "00",
        "01",
        "02",
        "03",
        "04",
        "05",
        "06",
        "07",
        "08",
        "09",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
        "24",
        "25",
        "26",
        "27",
        "28",
        "29",
        "30",
        "31"};

    
    /**
     * Default constructor only intended to be used by the XML configuration. <p>
     * 
     * It is recommended to use the constructor <code>{@link #CmsSearchIndex(String)}</code> 
     * as it enforces the mandatory name argument. <p>
     * 
     */
    public CmsSearchIndex() {

        m_sourceNames = new ArrayList<String>();
        m_documenttypes = new HashMap<String, List<String>>();
        m_createExcerpt = true;
        m_extractContent = true;
        m_checkTimeRange = true;
        m_indexExpirerContent = false;
        m_indexBuildHistorical = false;
        m_checkPermissions = true;
        m_enabled = true;
        m_priority = -1;
        m_maxHits = MAX_HITS_DEFAULT;
    }

    /**
     * Creates a new CmsSearchIndex with the given name.<p>
     * 
     * @param name the system-wide unique name for the search index 
     * 
     * @throws CmsIllegalArgumentException if the given name is null, empty or already taken by another search index 
     * 
     */
    public CmsSearchIndex(String name)
    throws CmsIllegalArgumentException {

        this();
        setName(name);
    }

    /**
     * Adds a parameter.<p>
     * 
     * @param key the key/name of the parameter
     * @param value the value of the parameter
     */
    public void addConfigurationParameter(String key, String value) {

        if (PERMISSIONS.equals(key)) {
            m_checkPermissions = Boolean.valueOf(value).booleanValue();
        } else if (TIME_RANGE.equals(key)) {
            m_checkTimeRange = Boolean.valueOf(value).booleanValue();
        } else if (BUILD_HISTORICAL.equals(key)) {
            m_indexBuildHistorical = Boolean.valueOf(value).booleanValue();
        } else if (INDEX_EXPIRED.equals(key)) {
            m_indexExpirerContent = Boolean.valueOf(value).booleanValue();
        } else if (EXCERPT.equals(key)) {
            m_createExcerpt = Boolean.valueOf(value).booleanValue();
        } else if (EXTRACT_CONTENT.equals(key)) {
            m_extractContent = Boolean.valueOf(value).booleanValue();
        } else if (BACKUP_REINDEXING.equals(key)) {
            m_backupReindexing = Boolean.valueOf(value).booleanValue();
        } else if (MAX_HITS.equals(key)) {
            try {
                m_maxHits = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
            if (m_maxHits < (MAX_HITS_DEFAULT / 100)) {
                m_maxHits = MAX_HITS_DEFAULT;
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
        } else if (PRIORITY.equals(key)) {
            m_priority = Integer.parseInt(value);
            if (m_priority < Thread.MIN_PRIORITY) {
                m_priority = Thread.MIN_PRIORITY;
                LOG.error(Messages.get().getBundle().key(
                    Messages.LOG_SEARCH_PRIORITY_TOO_LOW_2,
                    value,
                    new Integer(Thread.MIN_PRIORITY)));

            } else if (m_priority > Thread.MAX_PRIORITY) {
                m_priority = Thread.MAX_PRIORITY;
                LOG.debug(Messages.get().getBundle().key(
                    Messages.LOG_SEARCH_PRIORITY_TOO_HIGH_2,
                    value,
                    new Integer(Thread.MAX_PRIORITY)));

            }
        } else if (LUCENE_MAX_MERGE_DOCS.equals(key)) {
            try {
                m_luceneMaxMergeDocs = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
        } else if (LUCENE_MERGE_FACTOR.equals(key)) {
            try {
                m_luceneMergeFactor = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
        } else if (LUCENE_RAM_BUFFER_SIZE_MB.equals(key)) {
            try {
                m_luceneRAMBufferSizeMB = Double.valueOf(value);
            } catch (NumberFormatException e) {
                LOG.error(Messages.get().getBundle().key(Messages.LOG_INVALID_PARAM_3, value, key, getName()));
            }
        } else if (LUCENE_USE_COMPOUND_FILE.equals(key)) {
            m_luceneUseCompoundFile = Boolean.valueOf(value);
        }
    }

    /**
     * Adds am index source to this search index.<p>
     * 
     * @param sourceName the index source name to add
     */
    public void addSourceName(String sourceName) {

        m_sourceNames.add(sourceName);
    }

    /**
     * Checks is this index has been configured correctly.<p>
     * 
     * In case the check fails, the <code>enabled</code> property
     * is set to <code>false</code>
     * 
     * @param cms a OpenCms user context to perform the checks with (should have "Administrator" permissions)
     *
     * @return <code>true</code> in case the index is correctly configured and enabled after the check
     * 
     * @see #isEnabled()
     */
    public boolean checkConfiguration(CmsObject cms) {

        if (isEnabled()) {
            // check if the project for the index exists        
            try {
                cms.readProject(getProject());
                setEnabled(true);
            } catch (CmsException e) {
                // the project does not exist, disable the index
                setEnabled(false);
                if (LOG.isErrorEnabled()) {
                    LOG.error(Messages.get().getBundle().key(
                        Messages.LOG_SEARCHINDEX_CREATE_BAD_PROJECT_2,
                        getProject(),
                        getName()));
                }
            }
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info(Messages.get().getBundle().key(Messages.LOG_SEARCHINDEX_DISABLED_1, getName()));
            }
        }

        return isEnabled();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsSearchIndex) {
            return ((CmsSearchIndex)obj).m_name.equals(m_name);
        }
        return false;
    }

    /**
     * Returns the Lucene analyzer used for this index.<p>
     *
     * @return the Lucene analyzer used for this index
     */
    public Analyzer getAnalyzer() {

        return m_analyzer;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map<String, Object> getConfiguration() {

        Map<String, Object> result = new TreeMap<String, Object>();
        if (getPriority() > 0) {
            result.put(PRIORITY, new Integer(m_priority));
        }
        if (!isCreatingExcerpt()) {
            result.put(EXCERPT, Boolean.valueOf(m_createExcerpt));
        }
        if (!isExtractingContent()) {
            result.put(EXTRACT_CONTENT, Boolean.valueOf(m_extractContent));
        }
        if (!isCheckingPermissions()) {
            result.put(PERMISSIONS, Boolean.valueOf(m_checkPermissions));
        }
        if (!isCheckingTimeRange()) {
            result.put(TIME_RANGE, Boolean.valueOf(m_checkTimeRange));
        }
        if (isIndexingExpiredContent()) {
            result.put(INDEX_EXPIRED, Boolean.valueOf(m_indexExpirerContent));
        }
        if (isBuildHistorical()) {
            result.put(BUILD_HISTORICAL, Boolean.valueOf(m_indexBuildHistorical));
        }
        if (isBackupReindexing()) {
            result.put(BACKUP_REINDEXING, Boolean.valueOf(m_backupReindexing));
        }
        if (getMaxHits() != MAX_HITS_DEFAULT) {
            result.put(MAX_HITS, String.valueOf(getMaxHits()));
        }
        // set the index writer parameter if required 
        if (m_luceneMaxMergeDocs != null) {
            result.put(LUCENE_MAX_MERGE_DOCS, m_luceneMaxMergeDocs);
        }
        if (m_luceneMergeFactor != null) {
            result.put(LUCENE_MERGE_FACTOR, m_luceneMergeFactor);
        }
        if (m_luceneRAMBufferSizeMB != null) {
            result.put(LUCENE_RAM_BUFFER_SIZE_MB, m_luceneRAMBufferSizeMB);
        }
        if (m_luceneUseCompoundFile != null) {
            result.put(LUCENE_USE_COMPOUND_FILE, m_luceneUseCompoundFile);
        }
        return result;
    }

    /**
     * Returns the Lucene document with the given root path from the index.<p>
     * 
     * @param rootPath the root path of the document to get 
     * 
     * @return the Lucene document with the given root path from the index
     */
    public Document getDocument(String rootPath) {

        Document result = null;
        IndexSearcher searcher=null;
		try {
			searcher = getSearcher();
		
            // search for an exact match on the document root path
            Term pathTerm = new Term(CmsSearchField.FIELD_PATH, rootPath);
            try {
                TopDocs hits = searcher.search(new TermQuery(pathTerm), 1);
                if (hits.scoreDocs.length > 0) {
                    result = searcher.doc(hits.scoreDocs[0].doc);
                }
            } catch (IOException e) {
                // ignore, return null and assume document was not found
            }
        
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		finally {
			if (searcher!=null)
				try {
					releaseSearcher(searcher);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
        return result;
    }

    /**
     * Returns the Lucene document id with the given root path from the index.<p>
     * 
     * @param rootPath the root path of the document to get 
     * 
     * @return the Lucene document with the given root path from the index
     */
    public Integer getDocumentId(String rootPath) {

        Integer result = null;
        IndexSearcher searcher=null;
		try {
			searcher = getSearcher();
		
            // search for an exact match on the document root path
            Term pathTerm = new Term(CmsSearchField.FIELD_PATH, rootPath);
            try {
                TopDocs hits = searcher.search(new TermQuery(pathTerm), 1);
                if (hits.scoreDocs.length > 0) {
                    result = hits.scoreDocs[0].doc;
                }
            } catch (IOException e) {
                // ignore, return null and assume document was not found
            }
        
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		finally {
			if (searcher!=null)
				try {
					releaseSearcher(searcher);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
        return result;
    }

    
    /**
     * Returns the document type factory used for the given resource in this index, or <code>null</code>  
     * in case the resource is not indexed by this index.<p>
     * 
     * A resource is indexed if the following is all true: <ol>
     * <li>The index contains at last one index source matching the root path of the given resource.
     * <li>For this matching index source, the document type factory needed by the resource is also configured.
     * </ol>
     * 
     * @param res the resource to check
     * 
     * @return he document type factory used for the given resource in this index, or <code>null</code>  
     * in case the resource is not indexed by this index
     */
    public I_CmsDocumentFactory getDocumentFactory(CmsResource res) {

        if ((res != null) && (m_sources != null)) {
            // the result can only be null or the type configured for the resource
            I_CmsDocumentFactory result = OpenCms.getSearchManager().getDocumentFactory(res);
            if (result != null) {
                // check the path of the resource if it matches with one (or more) of the configured index sources
                Iterator<CmsSearchIndexSource> i = m_sources.iterator();
                while (i.hasNext()) {
                    CmsSearchIndexSource source = i.next();
                    if (source.isIndexing(res.getRootPath(), result.getName())) {
                        // we found an index source that indexes the resource
                        return result;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the search field configuration of this index.<p>
     * 
     * @return the search field configuration of this index
     */
    public CmsSearchFieldConfiguration getFieldConfiguration() {

        return m_fieldConfiguration;
    }

    /**
     * Returns the name of the field configuration used for this index.<p>
     * 
     * @return the name of the field configuration used for this index
     */
    public String getFieldConfigurationName() {

        return m_fieldConfigurationName;
    }

    public void commitIndexChanges() {
            try {
            	
            	if (m_indexWriter==null || !m_indexWriter.isOpen()) {
        			LOG.error("The index"  + getName() + " was closed. Trying to reopen it ", m_indexWriter.getTragicException());
					try {				
						reopen();
					} catch (CmsIndexException e) { 
						 LOG.error("failed during commiting index changes in" + getName(), e );
					}
        		}
            	
            	try {
            		m_indexWriter.commit();
            	}
            	catch (AlreadyClosedException ex ) {
            		LOG.error("The index"  + getName() + " has directory of type " + m_indexWriter.getDirectory().getClass().getCanonicalName());
            		LOG.error("The index"  + getName() + " has failed on commit ", ex);
            		
            		m_indexWriter.close();
            	}
				
				if (m_indexWriter==null || !m_indexWriter.isOpen()) {
        			LOG.error("The index"  + getName() + " was closed. Trying to reopen it ", m_indexWriter.getTragicException());
					try {				
						reopen();
					} catch (CmsIndexException e) { 
						 LOG.error("failed during commiting index changes in" + getName(), e );
					}
        		}
				m_searcherManager.maybeRefresh();
            } catch (RuntimeException e) {
            	LOG.fatal("failed during commiting index changes in" + getName(), e );
            } catch (IOException e) {
			
			    if (LOG.isWarnEnabled()) {
                    LOG.warn(Messages.get().getBundle().key(
                        Messages.LOG_IO_INDEX_WRITER_COMMIT_2,
                        getName(),
                        getPath()), e);
                }
				
			}
    }
    
    public boolean isOpen() {
    	if (m_indexWriter==null)
    		return false;
    	
    	return m_indexWriter.isOpen();
    }
    
    public synchronized IndexWriter reopen() throws CmsIndexException {
		
    	LOG.info("Reopening index " + this.getName());
    	if (m_indexWriter!=null) {
	    	try {
				if (m_indexWriter.isOpen())
					m_indexWriter.close();

		    	m_searcherManager.close();


	        } catch (IOException e) {
	            // if we can't close the index we are busted!
	            throw new CmsIndexException(
	                Messages.get().container(Messages.LOG_IO_INDEX_WRITER_CLOSE_2, getPath(), getName()),
	                e);
	        }
			finally {
				m_indexWriter = null;
		   	 	m_searcherManager = null;

			}
    	}
    	
    	m_indexWriter = getIndexWriter(false);
    	searchManagerRestore();
    	return m_indexWriter;
    }
    
    /**
     * Returns a new index writer for this index.<p>
     * 
     * @param create if <code>true</code> a whole new index is created, if <code>false</code> an existing index is updated
     * 
     * @return a new instance of IndexWriter
     * @throws CmsIndexException if the index can not be opened
     */
    public IndexWriter getIndexWriter(boolean create) throws CmsIndexException {

    	if (m_indexWriter != null) {
    		
    		
    		if (!create) {
    			//Added to ensure if the index was cloded by an error.
    			if (m_indexWriter.isOpen()) {
    				// re-use existing index writer
                    return m_indexWriter;
        		}
            }
    		
    		try {
    			if (m_indexWriter.isOpen())
    				m_indexWriter.close();
                
            } catch (IOException e) {
                // if we can't close the index we are busted!
                throw new CmsIndexException(
                    Messages.get().container(Messages.LOG_IO_INDEX_WRITER_CLOSE_2, getPath(), getName()),
                    e);
            }
    		finally {
    			m_indexWriter = null;
    		}
    	
    	}
    	
    	IndexWriter indexWriter = null; 
    	FSDirectory dir = null;

        try {
            File f = new File(getPath());
            if (!f.exists()) {
                f = f.getParentFile();
                if ((f != null) && (!f.exists())) {
                    f.mkdirs();
                }
                create = true;
            }

            if (OpenCms.getSearchManager().getOpenMode()!=null && OpenCms.getSearchManager().getOpenMode().toLowerCase().trim().equals("raf")) {
            	dir = new RAFDirectory(Paths.get(getPath()));
            }
            else { 
            	dir = FSDirectory.open(Paths.get(getPath()));
            }
            IndexWriterConfig indexConfig = new IndexWriterConfig(getAnalyzer());
            
            if (m_luceneRAMBufferSizeMB != null) {
                indexConfig.setRAMBufferSizeMB(m_luceneRAMBufferSizeMB.doubleValue());
            }
            
            if (m_luceneUseCompoundFile != null) {
            	indexConfig.setUseCompoundFile(m_luceneUseCompoundFile.booleanValue());
            }
            
            MergePolicy mergePolicy = null;
            //lucene ahora usa esta merge policy:
            if (m_luceneMaxMergeDocs == null && m_luceneMergeFactor == null)
            	mergePolicy = new TieredMergePolicy();
            else {
        		LogDocMergePolicy _mergePolicy = new LogDocMergePolicy();
	            if (m_luceneMaxMergeDocs != null) {
	            	_mergePolicy.setMaxMergeDocs(m_luceneMaxMergeDocs.intValue());
	            }
	            if (m_luceneMergeFactor != null) {
	            	_mergePolicy.setMergeFactor(m_luceneMergeFactor.intValue());
	            }
	            mergePolicy = _mergePolicy;
            }
            indexConfig.setMergePolicy(mergePolicy);
            
            if (create) {
                indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            // TODO: register the modified default similarity implementation
            //indexConfig.setSimilarity(m_sim);
            indexWriter = new IndexWriter(dir, indexConfig);
            
        } catch (Exception e) {
            if (dir != null) {
                try {
					dir.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }

            if (indexWriter != null) {
                try {
                    indexWriter.close();
                } catch (IOException closeExeception) {

                    throw new CmsIndexException(
                        Messages.get().container(Messages.ERR_IO_INDEX_WRITER_OPEN_2, getPath(), getName()),
                        e);
                }
            }

            throw new CmsIndexException(
                Messages.get().container(Messages.ERR_IO_INDEX_WRITER_OPEN_2, getPath(), getName()),
                e);
        }
        
        m_indexWriter = indexWriter;
        
        return indexWriter;
    	
    }

    /**
     * Returns the language locale of this index.<p>
     * 
     * @return the language locale of this index, for example "en"
     */
    public Locale getLocale() {

        return m_locale;
    }
    
    public String getExcludeCondition() {
		return (m_excludeCondition==null ? "" : m_excludeCondition);
	}

    /**
     * Returns the language locale of the index as a String.<p>
     * 
     * @return the language locale of the index as a String
     * 
     * @see #getLocale()
     */
    public String getLocaleString() {

        return getLocale().toString();
    }

    /**
     * Indicates the number of how many hits are loaded at maximum.<p> 
     * 
     * Since Lucene 2.4, the number of maximum documents to load from the index
     * must be specified. The default of this setting is {@link CmsSearchIndex#MAX_HITS_DEFAULT} (5000).
     * This means that at maximum 5000 results are returned from the index.
     * Please note that this number may be reduced further because of OpenCms read permissions 
     * or per-user file visibility settings not controlled in the index.<p>
     * 
     * @return the number of how many hits are loaded at maximum
     * 
     * @since 7.5.1
     */
    public int getMaxHits() {

        return m_maxHits;
    }

    /**
     * Gets the name of this index.<p>
     * 
     * @return the name of the index
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the path where this index stores it's data in the "real" file system.<p>
     * 
     * @return the path where this index stores it's data in the "real" file system
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the Thread priority for this search index.<p>
     *
     * @return the Thread priority for this search index
     */
    public int getPriority() {

        return m_priority;
    }

    /**
     * Gets the project of this index.<p>
     * 
     * @return the project of the index, i.e. "online"
     */
    public String getProject() {

        return m_project;
    }

    /**
     * Get the rebuild mode of this index.<p>
     * 
     * @return the current rebuild mode
     */
    public String getRebuildMode() {

        return m_rebuild;
    }

    /**
     * Returns the Lucene index searcher used for this search index.<p>
     *
     * @return the Lucene index searcher used for this search index
     * @throws IOException 
     */

    public IndexSearcher getSearcher() throws IOException {
    	return m_searcherManager.acquire();
    }

    /**
     * Returns all configured sources names of this search index.<p>
     * 
     * @return a list with all configured sources names of this search index
     */
    public List<String> getSourceNames() {

        return m_sourceNames;
    }

    /**
     * Returns all configured index sources of this search index.<p>
     * 
     * @return all configured index sources of this search index
     */
    public List<CmsSearchIndexSource> getSources() {

        return m_sources;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_name != null ? m_name.hashCode() : 0;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        // noting to do here
    }

    /**
     * Initializes the search index.<p>
     * 
     * @throws CmsSearchException if the index source association failed
     */
    public void initialize() throws CmsSearchException {

        if (!isEnabled()) {
            // index is disabled, no initialization is required
            return;
        }

        String sourceName = null;
        CmsSearchIndexSource indexSource = null;
        List<String> searchIndexSourceDocumentTypes = null;
        List<String> resourceNames = null;
        String resourceName = null;
        m_sources = new ArrayList<CmsSearchIndexSource>();

        m_path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(
            OpenCms.getSearchManager().getDirectory() + "/" + m_name);

        for (int i = 0, n = m_sourceNames.size(); i < n; i++) {

            try {
                sourceName = m_sourceNames.get(i);
                indexSource = OpenCms.getSearchManager().getIndexSource(sourceName);
                m_sources.add(indexSource);

                resourceNames = indexSource.getResourcesNames();
                searchIndexSourceDocumentTypes = indexSource.getDocumentTypes();
                for (int j = 0, m = resourceNames.size(); j < m; j++) {

                    resourceName = resourceNames.get(j);
                    m_documenttypes.put(resourceName, searchIndexSourceDocumentTypes);
                }
            } catch (Exception e) {
                // mark this index as disabled
                setEnabled(false);
                throw new CmsSearchException(Messages.get().container(
                    Messages.ERR_INDEX_SOURCE_ASSOCIATION_1,
                    sourceName), e);
            }
        }

        // initialize the search field configuration
        if (m_fieldConfigurationName == null) {
            // if not set, use standard field configuration
            m_fieldConfigurationName = CmsSearchFieldConfiguration.STR_STANDARD;
        }
        m_fieldConfiguration = OpenCms.getSearchManager().getFieldConfiguration(m_fieldConfigurationName);
        if (m_fieldConfiguration == null) {
            // we must have a valid field configuration to continue
            throw new CmsSearchException(Messages.get().container(
                Messages.ERR_FIELD_CONFIGURATION_UNKNOWN_2,
                m_name,
                m_fieldConfigurationName));
        }

        // get the configured analyzer and apply the the field configuration analyzer wrapper
        Analyzer baseAnalyzer = OpenCms.getSearchManager().getAnalyzer(getLocale());
        setAnalyzer(m_fieldConfiguration.getAnalyzer(baseAnalyzer));

        // initialize the index searcher instance
        searchManagerInit();
    }

    /**
     * Returns <code>true</code> if backup re-indexing is done by this index.<p>
     *
     * This is an optimization method by which the old extracted content is 
     * reused in order to save performance when re-indexing.<p>
     *
     * @return  <code>true</code> if backup re-indexing is done by this index
     * 
     * @since 7.5.1
     */
    public boolean isBackupReindexing() {

        return m_backupReindexing;
    }

    /**
     * Returns <code>true</code> if permissions are checked for search results by this index.<p>
     *
     * If permission checks are not required, they can be turned off in the index search configuration parameters
     * in <code>opencms-search.xml</code>. Not checking permissions will improve performance.<p> 
     * 
     * This is can be of use in scenarios when you know that all search results are always readable,
     * which is usually true for public websites that do not have personalized accounts.<p>
     * 
     * Please note that even if a result is returned where the current user has no read permissions, 
     * the user can not actually access this document. It will only appear in the search result list,
     * but if the user clicks the link to open the document he will get an error.<p>
     * 
     *
     * @return <code>true</code> if permissions are checked for search results by this index
     */
    public boolean isCheckingPermissions() {

        return m_checkPermissions;
    }

    /**
     * Returns <code>true</code> if the document time range is checked for search results by this index.<p>
     * 
     * If time range checks are not required, they can be turned off in the index search configuration parameters
     * in <code>opencms-search.xml</code>. Not checking the time range will improve performance.<p> 
     * 
     * @return <code>true</code> if the document time range is checked for search results by this index
     */
    public boolean isCheckingTimeRange() {

        return m_checkTimeRange;
    }

    public boolean isIndexingExpiredContent() {
    	return m_indexExpirerContent;
    }
    
    public boolean isBuildHistorical() {
    	return m_indexBuildHistorical;
    }
    /**
     * Returns <code>true</code> if an excerpt is generated by this index.<p>
     *
     * If no except is required, generation can be turned off in the index search configuration parameters
     * in <code>opencms-search.xml</code>. Not generating an excerpt will improve performance.<p> 
     *
     * @return <code>true</code> if an excerpt is generated by this index
     */
    public boolean isCreatingExcerpt() {

        return m_createExcerpt;
    }

    /**
     * Returns <code>true</code> if this index is currently disabled.<p>
     * 
     * @return <code>true</code> if this index is currently disabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Returns <code>true</code> if full text is extracted by this index.<p>
     *
     * Full text content extraction can be turned off in the index search configuration parameters
     * in <code>opencms-search.xml</code>. 
     * Not extraction the full text information will highly improve performance.<p> 
     *
     * @return <code>true</code> if full text is extracted by this index
     */
    public boolean isExtractingContent() {

        return m_extractContent;
    }

    /**
     * Removes an index source from this search index.<p>
     * 
     * @param sourceName the index source name to remove
     */
    public void removeSourceName(String sourceName) {

        m_sourceNames.remove(sourceName);
    }

    /**
     * Performs a search on the index within the given fields.<p>
     * 
     * The result is returned as List with entries of type I_CmsSearchResult.<p>
     * @param cms the current user's Cms object
     * @param params the parameters to use for the search
     * @return the List of results found or an empty list
     * @throws CmsSearchException if something goes wrong
     */
    public synchronized CmsSearchResultList search(CmsObject cms, CmsSearchParameters params) throws CmsSearchException {

        long timeTotal = -System.currentTimeMillis();
        long timeLucene;
        long timeResultProcessing;

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_SEARCH_PARAMS_2, params, getName()));
        }

        // the hits found during the search
        TopDocs hits;

        // storage for the results found
        CmsSearchResultList searchResults = new CmsSearchResultList();

        int previousPriority = Thread.currentThread().getPriority();

        // get an index searcher that is certainly up to date
        IndexSearcher searcher=null;
		try {
			searcher = getSearcher();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        try {
            // copy the user OpenCms context
            CmsObject searchCms = OpenCms.initCmsObject(cms);

            if (getPriority() > 0) {
                // change thread priority in order to reduce search impact on overall system performance
                Thread.currentThread().setPriority(getPriority());
            }

            
            // change the project     
            searchCms.getRequestContext().setCurrentProject(searchCms.readProject(getProject()));

            timeLucene = -System.currentTimeMillis();
            
            // several search options are searched using filters
            BooleanQuery.Builder filter = new BooleanQuery.Builder();
            // append root path filter
            filter = appendPathFilter(searchCms, filter, params.getRoots());
            // append category filter
            filter = appendCategoryFilter(searchCms, filter, params.getCategories());
            // append resource type filter
            filter = appendResourceTypeFilter(searchCms, filter, params.getResourceTypes());
            
         // append date last modified filter
            filter = appendDateLastModifiedFilter(
                filter,
                params.getMinDateLastModified(),
                params.getMaxDateLastModified());
            
            // append date created filter
            filter = appendDateCreatedFilter(
            		filter, 
            		params.getMinDateCreated(), 
            		params.getMaxDateCreated());
            
            // append expired content inclussion
            filter = appendExpiredContentFilter(searchCms, filter, params.isExpiredContentIncluded());

            // the search query to use, will be constructed in the next lines
            Query query = null;
            // store separate fields query for excerpt highlighting
            Query fieldsQuery = null;
            
            
            if (params.getFieldQueries() != null) {
                // each field has an individual query
            	BooleanQuery.Builder mustOccur = null;
                BooleanQuery.Builder shouldOccur = null;
                for (CmsSearchParameters.CmsSearchFieldQuery fq : params.getFieldQueries()) {
                    // add one sub-query for each defined field
                	QueryParser p = new QueryParser(fq.getFieldName(), getAnalyzer());
                    if (BooleanClause.Occur.SHOULD.equals(fq.getOccur())) {
                        if (shouldOccur == null) {
                            shouldOccur = new BooleanQuery.Builder();
                        }
                        shouldOccur.add(p.parse(fq.getSearchQuery()), fq.getOccur());
                    } else {
                        if (mustOccur == null) {
                            mustOccur = new BooleanQuery.Builder();
                        }
                        mustOccur.add(p.parse(fq.getSearchQuery()), fq.getOccur());
                    }
                }
                BooleanQuery.Builder booleanFieldsQuery = new BooleanQuery.Builder();
                if (mustOccur != null) {
                    booleanFieldsQuery.add(mustOccur.build(), BooleanClause.Occur.MUST);
                }
                if (shouldOccur != null) {
                    booleanFieldsQuery.add(shouldOccur.build(), BooleanClause.Occur.MUST);
                }
                
                booleanFieldsQuery.add(filter.build(),BooleanClause.Occur.FILTER);
                
                fieldsQuery = searcher.rewrite(booleanFieldsQuery.build());
            } else if ((params.getFields() != null) && (params.getFields().size() > 0)) {
                // no individual field queries have been defined, so use one query for all fields 
                BooleanQuery.Builder booleanFieldsQuery = new BooleanQuery.Builder();
                // this is a "regular" query over one or more fields
                // add one sub-query for each of the selected fields, e.g. "content", "title" etc.
                for (int i = 0; i < params.getFields().size(); i++) {
                    QueryParser p = new QueryParser(params.getFields().get(i), getAnalyzer());
                    booleanFieldsQuery.add(p.parse(params.getQuery()), BooleanClause.Occur.SHOULD);
                }
                
                booleanFieldsQuery.add(filter.build(),BooleanClause.Occur.FILTER);
                
                fieldsQuery = searcher.rewrite(booleanFieldsQuery.build());
            } else {
                // if no fields are provided, just use the "content" field by default
                QueryParser p = new QueryParser(CmsSearchField.FIELD_CONTENT, getAnalyzer());
                
                BooleanQuery.Builder booleanFieldsQuery = new BooleanQuery.Builder();
                booleanFieldsQuery.add(p.parse(params.getQuery()), BooleanClause.Occur.SHOULD)
                		.add(filter.build(),BooleanClause.Occur.FILTER);
                
                fieldsQuery = searcher.rewrite(booleanFieldsQuery.build());
            }

            // finally add the field queries to the main query
            query = fieldsQuery;
            //query.add(fieldsQuery, BooleanClause.Occur.MUST);

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_BASE_QUERY_1, query));
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_FIELDS_QUERY_1, fieldsQuery));
            }

            // TODO: collect the categories is disabled. not compatible with lucene 6. hint: use facets
            /*
            CmsSearchCategoryCollector categoryCollector;
            if (params.isCalculateCategories()) {
                // USE THIS OPTION WITH CAUTION
                // this may slow down searched by an order of magnitude
                categoryCollector = new CmsSearchCategoryCollector(searcher);
                // perform a first search to collect the categories
                searcher.search(query, filter, categoryCollector);
                // store the result
                searchResults.setCategories(categoryCollector.getCategoryCountResult());
            }
			*/
            
            // perform the search operation
            if (params.getSort() == null) {
                hits = searcher.search(query, m_maxHits);
            } else {
                hits = searcher.search(query, m_maxHits, params.getSort());
            }

            timeLucene += System.currentTimeMillis();
            timeResultProcessing = -System.currentTimeMillis();

            Document doc;
            CmsSearchResult searchResult;

            if (hits != null) {
                int hitCount = hits.totalHits > hits.scoreDocs.length ? hits.scoreDocs.length : hits.totalHits;
                int page = params.getSearchPage();
                int start = -1, end = -1;
                if ((params.getMatchesPerPage() > 0) && (page > 0) && (hitCount > 0)) {
                    // calculate the final size of the search result
                    start = params.getMatchesPerPage() * (page - 1);
                    end = start + params.getMatchesPerPage();
                    // ensure that both i and n are inside the range of foundDocuments.size()
                    start = (start > hitCount) ? hitCount : start;
                    end = (end > hitCount) ? hitCount : end;
                } else {
                    // return all found documents in the search result
                    start = 0;
                    end = hitCount;
                }

                int visibleHitCount = hitCount;
                for (int i = 0, cnt = 0; (i < hitCount) && (cnt < end); i++) {
                    try {
                        doc = searcher.doc(hits.scoreDocs[i].doc);
                        if (isInTimeRange(doc, params) && (hasReadPermission(searchCms, doc))) {
                            // user has read permission
                            if (cnt >= start) {
                                // do not use the resource to obtain the raw content, read it from the lucene document!
                                String excerpt = null;
                                if (isCreatingExcerpt()) {
                                    I_CmsTermHighlighter highlighter = OpenCms.getSearchManager().getHighlighter();
                                    excerpt = highlighter.getExcerpt(doc, this, params, fieldsQuery, getAnalyzer());
                                }
                                searchResult = new CmsSearchResult(
                                    Math.round((hits.scoreDocs[i].score / hits.getMaxScore()) * 100f),
                                    hits.scoreDocs[i].score,
                                    doc,
                                    excerpt);
                                searchResults.add(searchResult);
                            }
                            cnt++;
                        } else {
                            visibleHitCount--;
                        }
                    } catch (Exception e) {
                        // should not happen, but if it does we want to go on with the next result nevertheless                        
                        if (LOG.isWarnEnabled()) {
                            LOG.warn(Messages.get().getBundle().key(Messages.LOG_RESULT_ITERATION_FAILED_0), e);
                        }
                    }
                }

                // save the total count of search results
                searchResults.setHitCount(visibleHitCount);
            } else {
                searchResults.setHitCount(0);
            }

            timeResultProcessing += System.currentTimeMillis();
        } catch (RuntimeException e) {
            throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1, params), e);
        } catch (Exception e) {
            throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1, params), e);
        } finally {
            // re-set thread to previous priority
            Thread.currentThread().setPriority(previousPriority);
            //release the sercher
        	try {
        		releaseSearcher(searcher);
			} catch (IOException e) {
	            throw new CmsSearchException(Messages.get().container(Messages.ERR_SEARCH_PARAMS_1, params), e);
			}
        }

        if (LOG.isDebugEnabled()) {
            timeTotal += System.currentTimeMillis();
            Object[] logParams = new Object[] {
                new Integer(hits == null ? 0 : hits.totalHits),
                new Long(timeTotal),
                new Long(timeLucene),
                new Long(timeResultProcessing)};
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_STAT_RESULTS_TIME_4, logParams));
        }

        return searchResults;
    }
    
    public void releaseSearcher(IndexSearcher searcher) throws IOException {
    	m_searcherManager.release(searcher);
		
	}

	protected BooleanQuery.Builder appendExpiredContentFilter(CmsObject cms, BooleanQuery.Builder filter, boolean isExpiredContentIncluded) {
    	
    	if (isExpiredContentIncluded) {
    		filter.add(new BooleanClause(
    				 getMultiTermQueryFilter(
    				 CmsSearchField.FIELD_TIMERANGE,
    				 Boolean.toString(true)), BooleanClause.Occur.FILTER));
    	}
    	return filter;
    }

    /**
     * Appends the a VFS path filter to the given filter clause that matches all given root paths.<p>
     *
     * In case the provided List is null or empty, the current request context site root is appended.<p>
     *
     * The original filter parameter is extended and also provided as return value.<p>
     *
     * @param cms the current OpenCms search context
     * @param filter the filter to extend
     * @param roots the VFS root paths that will compose the filter
     *
     * @return the extended filter clause
     */
    protected BooleanQuery.Builder appendPathFilter(CmsObject cms, BooleanQuery.Builder filter, List<String> roots) {

        // complete the search root
        List<Term> terms = new ArrayList<Term>();
        if ((roots != null) && (roots.size() > 0)) {
            // add the all configured search roots with will request context
            for (int i = 0; i < roots.size(); i++) {
                String searchRoot = cms.getRequestContext().addSiteRoot(roots.get(i));
                extendPathFilter(terms, searchRoot);
            }
        } else {
            // use the current site root as the search root
            extendPathFilter(terms, cms.getRequestContext().getSiteRoot());
        }

        // add the calculated path filter for the root path
        filter.add(new BooleanClause(new TermsQuery(terms), BooleanClause.Occur.FILTER));
        return filter;
    }
    
    /**
     * Appends the a category filter to the given filter clause that matches all given categories.<p>
     *
     * In case the provided List is null or empty, the original filter is left unchanged.<p>
     *
     * The original filter parameter is extended and also provided as return value.<p>
     *
     * @param cms the current OpenCms search context
     * @param filter the filter to extend
     * @param categories the categories that will compose the filter
     *
     * @return the extended filter clause
     */
    protected BooleanQuery.Builder appendCategoryFilter(CmsObject cms, BooleanQuery.Builder filter, List<String> categories) {

        if ((categories != null) && (categories.size() > 0)) {
            // add query categories (if required)

            // categories are indexed as lower-case strings
            // @see org.opencms.search.fields.CmsSearchFieldConfiguration#appendCategories
            List<String> lowerCaseCategories = new ArrayList<String>();
            for (String category : categories) {
                lowerCaseCategories.add(category.toLowerCase());
            }
            filter.add(
                new BooleanClause(
                    getMultiTermQueryFilter(CmsSearchField.FIELD_CATEGORY, lowerCaseCategories),
                    BooleanClause.Occur.FILTER));
        }

        return filter;
    }

    /**
     * Appends the a resource type filter to the given filter clause that matches all given resource types.<p>
     *
     * In case the provided List is null or empty, the original filter is left unchanged.<p>
     *
     * The original filter parameter is extended and also provided as return value.<p>
     *
     * @param cms the current OpenCms search context
     * @param filter the filter to extend
     * @param resourceTypes the resource types that will compose the filter
     *
     * @return the extended filter clause
     */
    protected BooleanQuery.Builder appendResourceTypeFilter(CmsObject cms, BooleanQuery.Builder filter, List<String> resourceTypes) {

        if ((resourceTypes != null) && (resourceTypes.size() > 0)) {
            // add query resource types (if required)
            filter.add(
                new BooleanClause(
                    getMultiTermQueryFilter(CmsSearchField.FIELD_TYPE, resourceTypes),
                    BooleanClause.Occur.FILTER));
        }

        return filter;
    }

    /**
     * Appends a date of creation filter to the given filter clause that matches the
     * given time range.<p>
     *
     * If the start time is equal to {@link Long#MIN_VALUE} and the end time is equal to {@link Long#MAX_VALUE}
     * than the original filter is left unchanged.<p>
     *
     * The original filter parameter is extended and also provided as return value.<p>
     *
     * @param filter the filter to extend
     * @param startTime start time of the range to search in
     * @param endTime end time of the range to search in
     *
     * @return the extended filter clause
     */
    protected BooleanQuery.Builder appendDateCreatedFilter(BooleanQuery.Builder filter, long startTime, long endTime) {
    	if (isCheckingTimeRange()) {
	        // create special optimized sub-filter for the date last modified search
	    	TermsQuery dateFilter = createDateRangeFilter(CmsSearchField.FIELD_DATE_CREATED_LOOKUP, startTime, endTime);
	        if (dateFilter != null) {
	            // extend main filter with the created date filter
	            filter.add(new BooleanClause(dateFilter, BooleanClause.Occur.FILTER));
	        }
    	}
        return filter;
    }

    /**
     * Appends a date of last modification filter to the given filter clause that matches the
     * given time range.<p>
     *
     * If the start time is equal to {@link Long#MIN_VALUE} and the end time is equal to {@link Long#MAX_VALUE}
     * than the original filter is left unchanged.<p>
     *
     * The original filter parameter is extended and also provided as return value.<p>
     *
     * @param filter the filter to extend
     * @param startTime start time of the range to search in
     * @param endTime end time of the range to search in
     *
     * @return the extended filter clause
     */
    protected BooleanQuery.Builder appendDateLastModifiedFilter(BooleanQuery.Builder filter, long startTime, long endTime) {

    	if (isCheckingTimeRange()) {
	        // create special optimized sub-filter for the date last modified search
	        TermsQuery dateFilter = createDateRangeFilter(CmsSearchField.FIELD_DATE_LASTMODIFIED_LOOKUP, startTime, endTime);
	        if (dateFilter != null) {
	            // extend main filter with the created date filter
	            filter.add(new BooleanClause(dateFilter, BooleanClause.Occur.MUST));
	        }
    	}

        return filter;
    }

    
    
    /**
     * Sets the Lucene analyzer used for this index.<p>
     *
     * @param analyzer the Lucene analyzer to set
     */
    public void setAnalyzer(Analyzer analyzer) {

        m_analyzer = analyzer;
    }

    /**
     * Can be used to enable / disable this index.<p>
     * 
     * @param enabled the state of the index to set
     */
    public void setEnabled(boolean enabled) {

        m_enabled = enabled;
    }

    /**
     * Sets the field configuration used for this index.<p>
     * 
     * @param fieldConfiguration the field configuration to set
     */
    public void setFieldConfiguration(CmsSearchFieldConfiguration fieldConfiguration) {

        m_fieldConfiguration = fieldConfiguration;
    }

    /**
     * Sets the name of the field configuration used for this index.<p>
     * 
     * @param fieldConfigurationName the name of the field configuration to set
     */
    public void setFieldConfigurationName(String fieldConfigurationName) {

        m_fieldConfigurationName = fieldConfigurationName;
    }

    /**
     * Sets the locale to index resources.<p>
     * 
     * @param locale the locale to index resources
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    private idxConditionParser.ParseContext parseContext=null;
    
    public boolean excludeResource(CmsObject cms, CmsResource resource) {
    	if (m_excludeCondition==null || m_excludeCondition.trim().equals(""))
    		return false;
    	
    	Object result = new EvalIdxConditionVisitor(cms,resource).visit(parseContext);
    	if (result!=null && result.getClass().equals(Boolean.class))
    		return (boolean)result;
    	
    	return false;
    }
    
    public void setExcludeCondition(String excludeCondition) {
    	m_excludeCondition = excludeCondition;
    	
    	if (m_excludeCondition!=null && !m_excludeCondition.trim().equals("")) {
    		idxConditionLexer lexer = new idxConditionLexer(CharStreams.fromString(m_excludeCondition));
    		idxConditionParser parser = new idxConditionParser(new CommonTokenStream(lexer));
    		parseContext = parser.parse();
    	}
    }
    
    /**
     * Sets the locale to index resources as a String.<p>
     * 
     * @param locale the locale to index resources
     * 
     * @see #setLocale(Locale)
     */
    public void setLocaleString(String locale) {

        setLocale(CmsLocaleManager.getLocale(locale));
    }

    /**
     * Sets the number of how many hits are loaded at maximum.<p> 
     * 
     * This must be set at least to 50, or this setting is ignored.<p>
     * 
     * @param maxHits the number of how many hits are loaded at maximum to set
     * 
     * @see #getMaxHits()
     * 
     * @since 7.5.1
     */
    public void setMaxHits(int maxHits) {

        if (m_maxHits >= (MAX_HITS_DEFAULT / 100)) {
            m_maxHits = maxHits;
        }
    }

    /**
     * Sets the logical key/name of this search index.<p>
     * 
     * @param name the logical key/name of this search index 
     * 
     * @throws CmsIllegalArgumentException if the given name is null, empty or already taken by another search index 
     */
    public void setName(String name) throws CmsIllegalArgumentException {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(name)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_SEARCHINDEX_CREATE_MISSING_NAME_0));
        } else {
            // check if already used, but only if the name was modified: 
            // this is important as unmodifiable DisplayWidgets will also invoke this...
            if (!name.equals(m_name)) {
                // don't mess with XML configuration
                if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) {
                    // not needed at startup and additionally getSearchManager may return null
                    Iterator<String> itIdxNames = OpenCms.getSearchManager().getIndexNames().iterator();
                    while (itIdxNames.hasNext()) {
                        if (itIdxNames.next().equals(name)) {
                            throw new CmsIllegalArgumentException(Messages.get().container(
                                Messages.ERR_SEARCHINDEX_CREATE_INVALID_NAME_1,
                                name));
                        }
                    }
                }
            }
        }
        m_name = name;
    }

    /**
     * Sets the name of the project used to index resources.<p>
     * 
     * A duplicate method of <code>{@link #setProjectName(String)}</code> that allows 
     * to use instances of this class as a widget object (bean convention, 
     * cp.: <code>{@link #getProject()}</code>.<p> 
     * 
     * @param projectName the name of the project used to index resources
     */
    public void setProject(String projectName) {

        setProjectName(projectName);
    }

    /**
     * Sets the name of the project used to index resources.<p>
     * 
     * @param projectName the name of the project used to index resources
     */
    public void setProjectName(String projectName) {

        m_project = projectName;
    }

    /**
     * Sets the rebuild mode of this search index.<p>
     * 
     * @param rebuildMode the rebuild mode of this search index {auto|manual}
     */
    public void setRebuildMode(String rebuildMode) {

        m_rebuild = rebuildMode;
    }

    /**
     * Shuts down the search index.<p>
     * 
     * This will close the local Lucene index searcher instance.<p>
     * 
     * @throws IOException in case the index could not be closed
     */
    public void shutDown() throws IOException {

    	 if (m_indexWriter != null) {
             try {
                 m_indexWriter.commit();
                 m_indexWriter.close();
             } catch (IOException e) {

                 LOG.error(
                     Messages.get().getBundle().key(Messages.LOG_IO_INDEX_WRITER_CLOSE_2, getPath(), getName()),
                     e);

             }

         }
    	 m_searcherManager.close();
    	 m_searcherManager = null;
    	 
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_INDEX_1, getName()));
        }
    }


    /**
     * Closes the index searcher for this index.<p
     *
     * @see #indexSearcherOpen(String)
     */
/*    protected synchronized void indexSearcherClose() {
        indexSearcherClose(m_indexSearcher);
    }
*/


    /**

     * Closes the given Lucene index searcher.<p>

     *

     * @param searcher the searcher to close

     */
/*
    protected synchronized void indexSearcherClose(IndexSearcher searcher) {

        // in case there is an index searcher available close it
        if ((searcher != null) && (searcher.getIndexReader() != null)) {
            try {
                searcher.getIndexReader().close();
            } catch (Exception e) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_CLOSE_1, getName()), e);
            }

        }

    }
    */
    
    /**
     * Returns the name (<code>{@link #getName()}</code>) of this search index.<p>
     *  
     * @return the name (<code>{@link #getName()}</code>) of this search index
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return getName();
    }

    /**
     * Creates a backup of this index for optimized re-indexing of the whole content.<p>
     * 
     * @return the path to the backup folder, or <code>null</code> in case no backup was created
     */
    protected String createIndexBackup() {

        if (!isBackupReindexing()) {
            // if no backup is generated we don't need to do anything
            return null;
        }

        // check if the target directory already exists
        File file = new File(getPath());
        if (!file.exists()) {
            // index does not exist yet, so we can't backup it
            return null;
        }
        String backupPath = getPath() + "_backup";

        FSDirectory oldDir = null;
        FSDirectory newDir = null;
        try {

            // open file directory for Lucene
            oldDir = FSDirectory.open(file.toPath());
            newDir = FSDirectory.open(Paths.get(backupPath));

            for (String fileName : oldDir.listAll()) {
                newDir.copyFrom(oldDir, fileName, fileName, IOContext.DEFAULT);
            }

        } catch (Exception e) {
            // TODO: logging etc. 
            backupPath = null;
        } finally {
            if (oldDir != null) {
                try {
					oldDir.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            if (newDir != null) {
                try {
					newDir.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }

        return backupPath;
    }

    /**
     * Extends the given path query with another term for the given search root element.<p>
     * 
     * @param pathFilter the path filter to extend
     * @param searchRoot the search root to add to the path query
     */
    protected void extendPathFilter(List<Term> terms, String searchRoot) {

        if (!CmsResource.isFolder(searchRoot)) {
            searchRoot += "/";
        }
        terms.add(new Term(CmsSearchField.FIELD_PARENT_FOLDERS, searchRoot));
    }

    /**
     * Returns a cached Lucene term query filter for the given field and terms.<p>
     * 
     * @param field the field to use
     * @param terms the term to use
     * 
     * @return a cached Lucene term query filter for the given field and terms
     */
    protected TermsQuery getMultiTermQueryFilter(String field, List<String> terms) {

        return getMultiTermQueryFilter(field, null, terms);
    }

    /**
     * Returns a cached Lucene term query filter for the given field and terms.<p>
     * 
     * @param field the field to use
     * @param terms the term to use
     * 
     * @return a cached Lucene term query filter for the given field and terms
     */
    protected TermsQuery getMultiTermQueryFilter(String field, String terms) {

        return getMultiTermQueryFilter(field, terms, null);
    }

    /**
     * Returns a cached Lucene term query filter for the given field and terms.<p>
     *
     * @param field the field to use
     * @param termsStr the terms to use as a String separated by a space ' ' char
     * @param termsList the list of terms to use
     *
     * @return a cached Lucene term query filter for the given field and terms
     */
    protected TermsQuery getMultiTermQueryFilter(String field, String termsStr, List<String> termsList) {

        if (termsStr == null) {
            StringBuffer buf = new StringBuffer(64);
            for (int i = 0; i < termsList.size(); i++) {
                if (i > 0) {
                    buf.append(' ');
                }
                buf.append(termsList.get(i));
            }
            termsStr = buf.toString();
        }
        TermsQuery result = m_displayFilters.get(
            (new StringBuffer(64)).append(field).append('|').append(termsStr).toString());
        if (result == null) {
            List<Term> terms = new ArrayList<Term>();
            if (termsList == null) {
                termsList = CmsStringUtil.splitAsList(termsStr, ' ');
            }
            for (int i = 0; i < termsList.size(); i++) {
                terms.add(new Term(field, termsList.get(i)));
            }

            result = new TermsQuery(terms);
            
            m_displayFilters.put(field + termsStr, result);
        }
        return result;
    }
    /**
     * Returns a cached Lucene term query filter for the given field and term.<p>
     * 
     * @param field the field to use
     * @param term the term to use
     * 
     * @return a cached Lucene term query filter for the given field and term
     */
    protected TermsQuery getTermQueryFilter(String field, String term) {
        return getMultiTermQueryFilter(field, term, Collections.singletonList(term));
    }

    /**
     * Creates an optimized date range filter for the date of last modification or creation.<p>
     *
     * If the start date is equal to {@link Long#MIN_VALUE} and the end date is equal to {@link Long#MAX_VALUE}
     * than <code>null</code> is returned.<p>
     *
     * @param fieldName the name of the field to search
     * @param startTime start time of the range to search in
     * @param endTime end time of the range to search in
     *
     * @return an optimized date range filter for the date of last modification or creation
     */
    protected TermsQuery createDateRangeFilter(String fieldName, long startTime, long endTime) {

    	TermsQuery filter = null;
        if ((startTime != Long.MIN_VALUE) || (endTime != Long.MAX_VALUE)) {
            // a date range has been set for this document search
            if (startTime == Long.MIN_VALUE) {
                // default start will always be "yyyy1231" in order to reduce term size
                Calendar cal = Calendar.getInstance(OpenCms.getLocaleManager().getTimeZone());
                cal.setTimeInMillis(endTime);
                cal.set(cal.get(Calendar.YEAR) - MAX_YEAR_RANGE, 11, 31, 0, 0, 0);
                startTime = cal.getTimeInMillis();
            } else if (endTime == Long.MAX_VALUE) {
                // default end will always be "yyyy0101" in order to reduce term size
                Calendar cal = Calendar.getInstance(OpenCms.getLocaleManager().getTimeZone());
                cal.setTimeInMillis(startTime);
                cal.set(cal.get(Calendar.YEAR) + MAX_YEAR_RANGE, 0, 1, 0, 0, 0);
                endTime = cal.getTimeInMillis();
            }

            // get the list of all possible date range options
            List<String> dateRange = getDateRangeSpan(startTime, endTime);
            List<Term> terms = new ArrayList<Term>();
            for (String range : dateRange) {
                terms.add(new Term(fieldName, range));
            }
            // create the filter for the date
            filter = new TermsQuery(terms);
        }
        return filter;
    }
    
    /**
     * Generates a list of date terms for the optimized date range search with "daily" granularity level.<p>
     *
     * How this works:<ul>
     * <li>For each document, terms are added for the year, the month and the day the document
     * was modified or created) in. So for example if a document is modified at February 02, 2009,
     * then the following terms are stored for this document:
     * "20090202", "200902" and "2009".</li>
     * <li>In case a date range search is done, then all possible matches for the
     * provided rage are created as search terms and matched with the document terms.</li>
     * <li>Consider the following use case: You want to find out if a resource has been changed
     * in the time between November 29, 2007 and March 01, 2009.
     * One term to match is simply "2008" because if a document
     * was modified in 2008, then it is clearly in the date range.
     * Other terms are "200712", "200901" and "200902", because all documents
     * modified in these months are also a certain matches.
     * Finally we need to add terms for "20071129", "20071130" and "20090301" to match the days in the
     * starting and final month.</li>
     * </ul>
     *
     * @param startDate start date of the range to search in
     * @param endDate end date of the range to search in
     *
     * @return a list of date terms for the optimized date range search
     */
    public static List<String> getDateRangeSpan(long startDate, long endDate) {

        if (startDate > endDate) {
            // switch so that the end is always before the start
            long temp = endDate;
            endDate = startDate;
            startDate = temp;
        }

        List<String> result = new ArrayList<String>(100);

        // initialize calendars from the time value
        Calendar calStart = Calendar.getInstance(OpenCms.getLocaleManager().getTimeZone());
        Calendar calEnd = Calendar.getInstance(calStart.getTimeZone());
        calStart.setTimeInMillis(startDate);
        calEnd.setTimeInMillis(endDate);

        // get the required info to build the date range from the calendars
        int startDay = calStart.get(Calendar.DAY_OF_MONTH);
        int endDay = calEnd.get(Calendar.DAY_OF_MONTH);
        int maxDayInStartMonth = calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
        int startMonth = calStart.get(Calendar.MONTH) + 1;
        int endMonth = calEnd.get(Calendar.MONTH) + 1;
        int startYear = calStart.get(Calendar.YEAR);
        int endYear = calEnd.get(Calendar.YEAR);

        // first add all full years in the date range
        result.addAll(getYearSpan(startYear + 1, endYear - 1));

        if (startYear != endYear) {
            // different year, different month
            result.addAll(getMonthSpan(startMonth + 1, 12, startYear));
            result.addAll(getMonthSpan(1, endMonth - 1, endYear));
            result.addAll(getDaySpan(startDay, maxDayInStartMonth, startMonth, startYear));
            result.addAll(getDaySpan(1, endDay, endMonth, endYear));
        } else {
            if (startMonth != endMonth) {
                // same year, different month
                result.addAll(getMonthSpan(startMonth + 1, endMonth - 1, startYear));
                result.addAll(getDaySpan(startDay, maxDayInStartMonth, startMonth, startYear));
                result.addAll(getDaySpan(1, endDay, endMonth, endYear));
            } else {
                // same year, same month
                result.addAll(getDaySpan(startDay, endDay, endMonth, endYear));
            }
        }

        // sort the result, makes the range better readable in the debugger
        Collections.sort(result);
        return result;
    }
    
    /**
     * Calculate a span of days in the given year and month for the optimized date range search.<p>
     *
     * The result will contain dates formatted like "yyyyMMDD", for example "20080131".<p>
     *
     * @param startDay the start day
     * @param endDay the end day
     * @param month the month
     * @param year the year
     *
     * @return a span of days in the given year and month for the optimized date range search
     */
    private static List<String> getDaySpan(int startDay, int endDay, int month, int year) {

        List<String> result = new ArrayList<String>();
        String yearMonthStr = String.valueOf(year) + DATES[month];
        for (int i = startDay; i <= endDay; i++) {
            String dateStr = yearMonthStr + DATES[i];
            result.add(dateStr);
        }
        return result;
    }

    /**
     * Calculate a span of months in the given year for the optimized date range search.<p>
     *
     * The result will contain dates formatted like "yyyyMM", for example "200801".<p>
     *
     * @param startMonth the start month
     * @param endMonth the end month
     * @param year the year
     *
     * @return a span of months in the given year for the optimized date range search
     */
    private static List<String> getMonthSpan(int startMonth, int endMonth, int year) {

        List<String> result = new ArrayList<String>();
        String yearStr = String.valueOf(year);
        for (int i = startMonth; i <= endMonth; i++) {
            String dateStr = yearStr + DATES[i];
            result.add(dateStr);
        }
        return result;
    }

    /**
     * Calculate a span of years for the optimized date range search.<p>
     *
     * The result will contain dates formatted like "yyyy", for example "2008".<p>
     *
     * @param startYear the start year
     * @param endYear the end year
     *
     * @return a span of years for the optimized date range search
     */
    private static List<String> getYearSpan(int startYear, int endYear) {

        List<String> result = new ArrayList<String>();
        for (int i = startYear; i <= endYear; i++) {
            String dateStr = String.valueOf(i);
            result.add(dateStr);
        }
        return result;
    }

    /**
     * Checks if the OpenCms resource referenced by the result document can be read 
     * be the user of the given OpenCms context.<p>
     * 
     * @param cms the OpenCms user context to use for permission testing
     * @param doc the search result document to check
     * @return <code>true</code> if the user has read permissions to the resource
     */
    protected boolean hasReadPermission(CmsObject cms, Document doc) {

        if (!isCheckingPermissions()) {
            // no permission check is performed at all
            return true;
        }

        IndexableField typeField = doc.getField(CmsSearchField.FIELD_TYPE);
        IndexableField pathField = doc.getField(CmsSearchField.FIELD_PATH);
        if ((typeField == null) || (pathField == null)) {
            // permission check needs only to be performed for VFS documents that contain both fields
            return true;
        }

        String type = typeField.stringValue();
        if (!CmsSearchFieldConfiguration.VFS_DOCUMENT_KEY_PREFIX.equals(type)
            && !OpenCms.getResourceManager().hasResourceType(type)) {
            // this is not a known VFS resource type (also not the generic "VFS" type of OpenCms before 7.0)
            return true;
        }

        // check if the resource exits in the VFS, 
        // this will implicitly check read permission and if the resource was deleted
        String contextPath = cms.getRequestContext().removeSiteRoot(pathField.stringValue());
        return cms.existsResource(contextPath);
    }

    
    /**
     * Checks if the document is in the time range specified in the search parameters.<p>
     *
     * The creation date and/or the last modification date are checked.<p>
     *
     * @param doc the document to check the dates against the given time range
     * @param params the search parameters where the time ranges are specified
     *
     * @return true if document is in time range or not time range set otherwise false
     */
    protected boolean isInTimeRange(Document doc, CmsSearchParameters params) {

        if (!isCheckingTimeRange()) {
            // time range check disabled
            return true;
        }
        
        try {
            // check the creation date of the document against the given time range
            Date dateCreated = DateTools.stringToDate(doc.getField(CmsSearchField.FIELD_DATE_CREATED).stringValue());
            if (dateCreated.getTime() < params.getMinDateCreated()) {
                return false;
            }
            if (dateCreated.getTime() > params.getMaxDateCreated()) {
                return false;
            }

            // check the last modification date of the document against the given time range
            Date dateLastModified = DateTools.stringToDate(
                doc.getField(CmsSearchField.FIELD_DATE_LASTMODIFIED).stringValue());
            if (dateLastModified.getTime() < params.getMinDateLastModified()) {
                return false;
            }

            if (dateLastModified.getTime() > params.getMaxDateLastModified()) {
                return false;
            }

        } catch (ParseException ex) {
            // date could not be parsed -> doc is in time range
        }

        return true;
    }

    /**
     * Initializes the Lucene index searcher for this index.<p>
     * 
     * Use {@link #getSearcher()} in order to obtain the searcher that has been opened.<p>
     * 
     * In case there is an index searcher still open, it is closed first.<p>
     * 
     * For performance reasons, one instance of the Lucene index searcher should be kept 
     * for all searches. However, if the index is updated or changed 
     * this searcher instance needs to be re-initialized.<p>
     * 
     * @param path the path to the index directory
     */
    protected synchronized void searchManagerInit() {

        try {
        	if (m_searcherManager==null){
        		m_searcherManager = new SearcherManager(getIndexWriter(false), new SearcherFactory());
        	}
        	
            //TODO: m_indexSearcher.setSimilarity(m_sim);
            m_displayFilters = new HashMap<String, TermsQuery>();
            
            
        } catch (IOException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_1, getName()), e);
            
        } catch (CmsIndexException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_1, getName()), e);
		}
    }

    public synchronized void searchManagerRestore() {

        try {
        	if (m_searcherManager!=null){
	    		m_searcherManager.close();
	    	}
        	m_searcherManager = new SearcherManager(getIndexWriter(false), new SearcherFactory());
        	
            //TODO: m_indexSearcher.setSimilarity(m_sim);
            m_displayFilters = new HashMap<String, TermsQuery>();
            
            
        } catch (IOException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_1, getName()), e);
            
        } catch (CmsIndexException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_1, getName()), e);
		}
    }

    public void putSearcherBackup(String backup) {
    	try {
	    	if (m_searcherManager!=null){
	    		m_searcherManager.close();
	    	}

	    	FSDirectory dir;
	    	
	    	if (OpenCms.getSearchManager().getOpenMode()!=null && OpenCms.getSearchManager().getOpenMode().toLowerCase().trim().equals("raf"))
            	dir = RAFDirectory.open(Paths.get(getPath()));
            else 
                dir = FSDirectory.open(Paths.get(getPath()));
	    	
			m_searcherManager = new SearcherManager(dir, new SearcherFactory());
    	} catch (IOException e) {
            LOG.error(Messages.get().getBundle().key(Messages.ERR_INDEX_SEARCHER_1, getName()), e);
    	}
    }
    
    /**
     * Removes the given backup folder of this index.<p>
     * 
     * @param path the backup folder to remove
     * 
     * @see #isBackupReindexing()
     */
    protected void removeIndexBackup(String path) {

    	if (!isBackupReindexing()) {
            // if no backup is generated we don't need to do anything
            return;
        }
    	
        // check if the target directory already exists
        File file = new File(path);

        if (!file.exists()) {
            // index does not exist yet
            return;
        }

        try {
        	FSDirectory dir;
	    	
	    	if (OpenCms.getSearchManager().getOpenMode()!=null && OpenCms.getSearchManager().getOpenMode().toLowerCase().trim().equals("raf"))
            	dir = RAFDirectory.open(Paths.get(getPath()));
            else 
                dir = FSDirectory.open(Paths.get(getPath()));
            dir.close();
            CmsFileUtil.purgeDirectory(file);

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

}