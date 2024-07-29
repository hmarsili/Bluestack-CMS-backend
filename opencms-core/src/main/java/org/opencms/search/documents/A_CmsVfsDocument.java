/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/documents/A_CmsVfsDocument.java,v $
 * Date   : $Date: 2011/03/23 14:52:04 $
 * Version: $Revision: 1.26 $
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

package org.opencms.search.documents;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.extractors.CmsExtractionResult;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.search.fields.CmsSearchField;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

/**
 * Base document factory class for a VFS <code>{@link org.opencms.file.CmsResource}</code>, 
 * just requires a specialized implementation of 
 * <code>{@link I_CmsDocumentFactory#extractContent(CmsObject, CmsResource, CmsSearchIndex)}</code>
 * for text extraction from the binary document content.<p>
 * 
 * @author Carsten Weinholz 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.26 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsVfsDocument implements I_CmsDocumentFactory {

    /** 
     * Value for "high" search priority.
     * 
     * @deprecated use {@link org.opencms.search.fields.CmsSearchFieldConfiguration#SEARCH_PRIORITY_HIGH_VALUE} instead
     */
    public static final String SEARCH_PRIORITY_HIGH_VALUE = "high";

    /** 
     * Value for "low" search priority. 
     * 
     * @deprecated use {@link org.opencms.search.fields.CmsSearchFieldConfiguration#SEARCH_PRIORITY_LOW_VALUE} instead
     */
    public static final String SEARCH_PRIORITY_LOW_VALUE = "low";

    /**
     * Value for "maximum" search priority. 
     * 
     * @deprecated use {@link org.opencms.search.fields.CmsSearchFieldConfiguration#SEARCH_PRIORITY_MAX_VALUE} instead
     */
    public static final String SEARCH_PRIORITY_MAX_VALUE = "max";

    /** 
     * Value for "normal" search priority.
     *  
     * @deprecated use {@link org.opencms.search.fields.CmsSearchFieldConfiguration#SEARCH_PRIORITY_NORMAL_VALUE} instead
     */
    public static final String SEARCH_PRIORITY_NORMAL_VALUE = "normal";

    /** 
     * The VFS prefix for document keys.
     *  
     * @deprecated use {@link org.opencms.search.fields.CmsSearchFieldConfiguration#VFS_DOCUMENT_KEY_PREFIX} instead
     */
    public static final String VFS_DOCUMENT_KEY_PREFIX = "VFS";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsVfsDocument.class);

    /**
     * Name of the documenttype.
     */
    protected String m_name;

    /** The cache used for storing extracted documents. */
    private CmsExtractionResultCache m_cache;

    /**
     * Creates a new instance of this lucene document factory.<p>
     * 
     * @param name name of the documenttype
     */
    public A_CmsVfsDocument(String name) {

        m_name = name;
    }

    /**
     * Creates a document factory lookup key for the given resource type name / MIME type configuration.<p>
     * 
     * If the given <code>mimeType</code> is <code>null</code>, this indicates that the key should 
     * match all VFS resource of the given resource type regardless of the MIME type.<p>
     * 
     * @param type the resource type name to use
     * @param mimeType the MIME type to use
     * 
     * @return a document factory lookup key for the given resource id / MIME type configuration
     */
    public static String getDocumentKey(String type, String mimeType) {

        StringBuffer result = new StringBuffer(16);
        result.append(A_CmsVfsDocument.VFS_DOCUMENT_KEY_PREFIX);
        result.append('_');
        result.append(type);
        if (mimeType != null) {
            result.append(':');
            result.append(mimeType);
        }
        return result.toString();
    }

    /**
     * Generates a new lucene document instance from contents of the given resource for the provided index.<p>
     * 
     * @see org.opencms.search.documents.I_CmsDocumentFactory#createDocument(CmsObject, CmsResource, CmsSearchIndex)
     * @see org.opencms.search.fields.CmsSearchFieldConfiguration#createDocument(CmsObject, CmsResource, CmsSearchIndex, I_CmsExtractionResult)
     */
    public Document createDocument(CmsObject cms, CmsResource resource, CmsSearchIndex index) throws CmsException {

    	
        // extract the content from the resource
        I_CmsExtractionResult content = null;

        if (index.isExtractingContent()) {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("createDocument " + resource.getRootPath() + ": Starting to extract content ... ");
        	}

            // do full text content extraction only if required

            // check if caching is enabled for this document type
            CmsExtractionResultCache cache = getCache();
            String cacheName = null;
            if ((cache != null) && (resource.getSiblingCount() > 1)) {
                // hard drive based caching only makes sense for resources that have siblings, 
                // because the index will also store the content as a blob
                cacheName = cache.getCacheName(resource, isLocaleDependend() ? index.getLocale() : null);
                content = cache.getCacheObject(cacheName);
            }

            if (content!=null)
            	if (LOG.isDebugEnabled()) {
            		LOG.debug("createDocument " + resource.getRootPath() + ": content found in cache ... ");
            	}
            if (content == null) {
 
               	if (LOG.isDebugEnabled()) {
            		LOG.debug("createDocument " + resource.getRootPath() + ": content NOT found in cache ... ");
            	}

            	// extraction result has not been found in the cache
                // compare "date of last modification of content" from Lucene index and OpenCms VFS
                // if this is identical, then the data from the Lucene index can be re-used 
                Document oldDoc = index.getDocument(resource.getRootPath());
                // first check if the document is already in the index
                if (oldDoc != null) {
                   	if (LOG.isDebugEnabled()) {
                   		LOG.debug("createDocument " + resource.getRootPath() + ": content is already in the index ... ");                	}
                 	
                    // first obtain content date from Lucene index
                    IndexableField fieldContentDate = oldDoc.getField(CmsSearchField.FIELD_DATE_CONTENT);
                    long contentDateIndex = 0;
                    if (fieldContentDate != null) {
                    	      
                        String contentDate = fieldContentDate.stringValue();
                        try {
                            contentDateIndex = DateTools.stringToTime(contentDate);
                        } catch (ParseException e) {
                            // ignore
                        }
                        // now compare the date with the date stored in the resource
                        if (contentDateIndex == resource.getDateContent()) {
                            // date of content is identical, re-use existing content
                        	IndexableField fieldContentBlob = oldDoc.getField(CmsSearchField.FIELD_CONTENT_BLOB);
                            if (fieldContentBlob != null) {
                                // extract stored content blob from Lucene index
                                BytesRef oldContent = fieldContentBlob.binaryValue();
                                content = CmsExtractionResult.fromBytes(oldContent);
                                
                              	if (LOG.isDebugEnabled()) {
                              		LOG.debug("createDocument " + resource.getRootPath() + ": content not changed ... ");
                            	}

                            }
                        }
                    }
                }
            }

            if (content == null) {
                // extraction result has not been attached to the resource
                try {
                  	if (LOG.isDebugEnabled()) {
                  		LOG.debug("createDocument " + resource.getRootPath() + ": extracting content from resource ");
                	}

                	content = extractContent(cms, resource, index);
                    if ((cache != null) && (resource.getSiblingCount() > 1)) {
                        // save extracted content to the cache
                        cache.saveCacheObject(cacheName, content);
                    }
                } catch (Exception e) {
                    // text extraction failed for document - continue indexing meta information only
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_TEXT_EXTRACTION_1, resource.getRootPath()), e);
                }
            }
        }

      	if (LOG.isDebugEnabled()) {
      		LOG.debug("createDocument " + resource.getRootPath() + ": formating content from resource ");
    	}

        // create the Lucene document according to the index field configuration
        return index.getFieldConfiguration().createDocument(cms, resource, index, content);
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getCache()
     */
    public CmsExtractionResultCache getCache() {

        return m_cache;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getDocumentKeys(java.util.List, java.util.List)
     */
    public List<String> getDocumentKeys(List<String> resourceTypes, List<String> mimeTypes) throws CmsException {

        List<String> keys = new ArrayList<String>();

        if (resourceTypes.contains("*")) {
            List<String> allTypes = new ArrayList<String>();
            for (Iterator<I_CmsResourceType> i = OpenCms.getResourceManager().getResourceTypes().iterator(); i.hasNext();) {
                I_CmsResourceType resourceType = i.next();
                allTypes.add(resourceType.getTypeName());
            }
            resourceTypes = allTypes;
        }

        try {
            for (Iterator<String> i = resourceTypes.iterator(); i.hasNext();) {

                String typeName = OpenCms.getResourceManager().getResourceType(i.next()).getTypeName();
                for (Iterator<String> j = mimeTypes.iterator(); j.hasNext();) {
                    keys.add(getDocumentKey(typeName, j.next()));
                }
                if (mimeTypes.isEmpty()) {
                    keys.add(getDocumentKey(typeName, null));
                }
            }
        } catch (Exception exc) {
            throw new CmsException(Messages.get().container(Messages.ERR_CREATE_DOC_KEY_0), exc);
        }

        return keys;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.search.documents.I_CmsDocumentFactory#setCache(org.opencms.search.documents.CmsExtractionResultCache)
     */
    public void setCache(CmsExtractionResultCache cache) {

        m_cache = cache;
    }

    /**
     * Upgrades the given resource to a {@link CmsFile} with content.<p>
     * 
     * @param cms the current users OpenCms context
     * @param resource the resource to upgrade
     * 
     * @return the given resource upgraded to a {@link CmsFile} with content
     * 
     * @throws CmsException if the resource could not be read 
     * @throws CmsIndexException if the resource has no content
     */
    protected CmsFile readFile(CmsObject cms, CmsResource resource) throws CmsException, CmsIndexException {

        CmsFile file = cms.readFile(resource);
        if (file.getLength() <= 0) {
            throw new CmsIndexException(Messages.get().container(Messages.ERR_NO_CONTENT_1, resource.getRootPath()));
        }
        return file;
    }
}