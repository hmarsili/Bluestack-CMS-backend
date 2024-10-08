/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/extractors/CmsExtractionResult.java,v $
 * Date   : $Date: 2011/03/23 14:51:17 $
 * Version: $Revision: 1.14 $
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

package org.opencms.search.extractors;

import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Field;
import org.apache.lucene.util.BytesRef;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The result of a document text extraction.<p>
 * 
 * This data structure contains the extracted text as well as (optional) 
 * meta information extracted from the document.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExtractionResult implements I_CmsExtractionResult, Serializable {

    /** UID rerquired for safe serialization. */
    private static final long serialVersionUID = 1465447302192195154L;

    /** The extracted individual content items. */
    private Map<String, String> m_contentItems;

    /** Custom fields to index */
    private List<Field> m_customFields;
    
    /** The serialized version of this object. */
    private byte[] m_serializedVersion;

    /**
     * Creates a new extration result without meta information and without additional fields.<p>
     * 
     * @param content the extracted content
     */
    public CmsExtractionResult(String content) {

        this(content, null);
        m_contentItems.put(ITEM_RAW, content);
    }

    /**
     * Creates a new extraction result.<p>
     * 
     * @param content the extracted content
     * @param contentItems the individual extracted content items
     */
    public CmsExtractionResult(String content, Map<String, String> contentItems) {

        if (contentItems != null) {
            m_contentItems = contentItems;
        } else {
            m_contentItems = new HashMap<String, String>();
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
            m_contentItems.put(ITEM_CONTENT, content);
        }
        
        m_customFields = new ArrayList<Field>();
    }
    
    public CmsExtractionResult(String content, Map<String, String> contentItems, List<Field> customFields) {

        if (contentItems != null) {
            m_contentItems = contentItems;
        } else {
            m_contentItems = new HashMap<String, String>();
        }
        
        if (customFields!= null) {
        	m_customFields = customFields;
        }
        else {
        	m_customFields = new ArrayList<Field>();
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(content)) {
            m_contentItems.put(ITEM_CONTENT, content);
        }
    }

    /**
     * Creates an extraction result from a serialized byte array.<p> 
     * 
     * @param bytes the serialized version of the extraction result
     * 
     * @return extraction result created from the serialized byte array  
     */
    public static final CmsExtractionResult fromBytes(BytesRef bytes) {

        CmsExtractionResult result = null;
        if (bytes != null) {
            Object obj = null;
            // create an object out of the byte array
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(CompressionTools.decompress(bytes.bytes));
                ObjectInputStream oin = new ObjectInputStream(in);
                obj = oin.readObject();
                oin.close();
            } catch (Exception e) {
                // ignore, null is not an instance of CmsExtractionResult
            }
            if (obj instanceof CmsExtractionResult) {
                result = (CmsExtractionResult)obj;
                result.m_serializedVersion = bytes.bytes;
            }
        }
        return result;
    }

    /**
     * Creates an extraction result from a serialized byte array.<p> 
     * 
     * @param bytes the serialized version of the extraction result
     * 
     * @return extraction result created from the serialized byte array  
     */
    public static final CmsExtractionResult fromBytes(byte[] bytes) {

        CmsExtractionResult result = null;
        if (bytes != null) {
            Object obj = null;
            // create an object out of the byte array
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                ObjectInputStream oin = new ObjectInputStream(in);
                obj = oin.readObject();
                oin.close();
            } catch (Exception e) {
                // ignore, null is not an instance of CmsExtractionResult
            }
            if (obj instanceof CmsExtractionResult) {
                result = (CmsExtractionResult)obj;
                result.m_serializedVersion = bytes;
            }
        }
        return result;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getBytes()
     */
    public byte[] getBytes() {

        // check if we have a cached version of the serialized object available
        if (m_serializedVersion != null) {
            return m_serializedVersion;
        }
        try {
            // serialize this object and return
            ByteArrayOutputStream out = new ByteArrayOutputStream(512);
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(this);
            oout.close();
            m_serializedVersion = out.toByteArray();
        } catch (Exception e) {
            // ignore, serialized version will be null
        }
        return m_serializedVersion;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getContent()
     */
    public String getContent() {

        return m_contentItems.get(ITEM_CONTENT);
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#getContentItems()
     */
    public Map<String, String> getContentItems() {

        return m_contentItems;
    }
    
    public List<Field> getCustomFields() {
    	return m_customFields;
    }

    /**
     * @see org.opencms.search.extractors.I_CmsExtractionResult#release()
     */
    public void release() {

        if (!m_contentItems.isEmpty()) {
            m_contentItems.clear();
        }
        m_contentItems = null;
        m_serializedVersion = null;
    }
}