/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/fields/CmsSearchFieldConfiguration.java,v $
 * Date   : $Date: 2011/03/23 14:53:14 $
 * Version: $Revision: 1.15 $
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.search.fields;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.extractors.I_CmsExtractionResult;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;

/**
 * Describes a configuration of fields that are used in building a search index.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.15 $ 
 * 
 * @since 7.0.0 
 */
public class CmsSearchFieldConfiguration implements Comparable<CmsSearchFieldConfiguration> {

    /**
     * The default for the standard search configuration.<p>
     * 
     * This defines the default that is used in case no "standard" field configuration 
     * is defined in <code>opencms-search.xml</code>.<p>
     */
    public static final CmsSearchFieldConfiguration DEFAULT_STANDARD = createStandardConfiguration();

    /** Value for "high" search priority. */
    public static final String SEARCH_PRIORITY_HIGH_VALUE = "high";

    /** Value for "low" search priority. */
    public static final String SEARCH_PRIORITY_LOW_VALUE = "low";

    /** Value for "maximum" search priority. */
    public static final String SEARCH_PRIORITY_MAX_VALUE = "max";

    /** Value for "normal" search priority. */
    public static final String SEARCH_PRIORITY_NORMAL_VALUE = "normal";

    /** The name for the standard field configuration. */
    public static final String STR_STANDARD = "standard";

    /** The description for the standard field configuration. */
    public static final String STR_STANDARD_DESCRIPTION = "The standard OpenCms 7.0 search index field configuration.";

    /** The VFS prefix for document keys. */
    public static final String VFS_DOCUMENT_KEY_PREFIX = "VFS";

    /** The description of the configuration. */
    private String m_description;

    /** Contains all names of the fields that are used in the excerpt. */
    private List<String> m_excerptFieldNames;

    /** Map to lookup the configured {@link CmsSearchField} instances by name. */
    private Map<String, CmsSearchField> m_fieldLookup;

    /** The list of configured {@link CmsSearchField} names. */
    private List<String> m_fieldNames;

    /** The list of configured {@link CmsSearchField} instances. */
    private List<CmsSearchField> m_fields;

    /** The name of the configuration. */
    private String m_name;

    /**
     * Creates a new, empty field configuration.<p>
     */
    public CmsSearchFieldConfiguration() {

        m_fields = new ArrayList<CmsSearchField>();
    }

    /**
     * Creates a space separated list of all parent folders of the given root path.<p>
     * 
     * @param rootPath the root path to get the parent folder list for
     * 
     * @return a space separated list of all parent folders of the given root path
     */
    public static String getParentFolderTokens(String rootPath) {

        if (CmsStringUtil.isEmpty(rootPath)) {
            return "/";
        }
        StringBuffer result = new StringBuffer(128);
        String folderName = CmsResource.getFolderPath(rootPath);
        for (int i = 0; i < folderName.length(); i++) {
            char c = folderName.charAt(i);
            if (c == '/') {
                if (result.length() > 0) {
                    result.append(' ');
                }
                result.append(folderName.substring(0, i + 1));
            }
        }
        return result.toString();
    }

    /**
     * Creates the default standard search configuration.<p>
     * 
     * This defines the default that is used in case no "standard" field configuration 
     * is defined in <code>opencms-search.xml</code>.<p>
     * 
     * @return the default standard search configuration
     */
    private static CmsSearchFieldConfiguration createStandardConfiguration() {

        CmsSearchFieldConfiguration result = new CmsSearchFieldConfiguration();
        result.setName(STR_STANDARD);
        result.setDescription(STR_STANDARD_DESCRIPTION);

        CmsSearchField field;
        // content mapping, store as compressed value
        field = new CmsSearchField(
            CmsSearchField.FIELD_CONTENT,
            "%(key.field.content)",
            true,
            true,
            true,
            true,
            true,
            null,
            CmsSearchField.BOOST_DEFAULT,
            null);
        field.addMapping(new CmsSearchFieldMapping(CmsSearchFieldMappingType.CONTENT, null));
        result.addField(field);

        // title mapping as a keyword
        field = new CmsSearchField(
            CmsSearchField.FIELD_TITLE,
            CmsSearchField.IGNORE_DISPLAY_NAME,
            true,
            true,
            false,
            false,
            0.0f,
            null);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_TITLE));
        result.addField(field);

        // title mapping as indexed field
        field = new CmsSearchField(CmsSearchField.FIELD_TITLE_UNSTORED, "%(key.field.title)", false, true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_TITLE));
        result.addField(field);

        // mapping of "Keywords" property to search field with the same name
        field = new CmsSearchField(CmsSearchField.FIELD_KEYWORDS, "%(key.field.keywords)", true, true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_KEYWORDS));
        result.addField(field);

        // mapping of "Description" property to search field with the same name
        field = new CmsSearchField(CmsSearchField.FIELD_DESCRIPTION, "%(key.field.description)", true, true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION));
        result.addField(field);

        // "meta" field is a combination of "Title", "Keywords" and "Description" properties
        field = new CmsSearchField(CmsSearchField.FIELD_META, "%(key.field.meta)", false, true);
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_TITLE));
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_KEYWORDS));
        field.addMapping(new CmsSearchFieldMapping(
            CmsSearchFieldMappingType.PROPERTY,
            CmsPropertyDefinition.PROPERTY_DESCRIPTION));
        result.addField(field);

        return result;
    }

    /**
     * Adds a field to this search field configuration.<p>
     * 
     * @param field the field to add
     */
    public void addField(CmsSearchField field) {

        if (field != null) {
            m_fields.add(field);
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsSearchFieldConfiguration obj) {

        return m_name.compareTo((obj).m_name);
    }

    /**
     * Creates the Lucene Document with this field configuration for the provided VFS resource, search index and content.<p>
     * 
     * This triggers the indexing process for the given VFS resource according to the configuration 
     * of the provided index.<p>
     * 
     * The provided index resource contains the basic contents to index.
     * The provided search index contains the configuration what to index, such as the locale and 
     * possible special field mappings.<p>
     * 
     * @param cms the OpenCms user context used to access the OpenCms VFS
     * @param resource the resource to create the Lucene document from
     * @param index the search index to create the Document for
     * @param content the plain text content extracted from the document
     * 
     * @return the Lucene Document for the given VFS resource and the given search index
     * 
     * @throws CmsException if something goes wrong
     */
    public Document createDocument(
        CmsObject cms,
        CmsResource resource,
        CmsSearchIndex index,
        I_CmsExtractionResult content) throws CmsException {

        // create the Lucene document according to the index field configuration
        Document document = new Document();

        // store the extraction result in the index
        if (content != null) {
            byte[] data = content.getBytes();
            if (data != null) {
            	//Lo deberia almacenar comprimido... (el parametro era Field.Store.COMPRESS)
            	FieldType fieldType = new FieldType();
            	fieldType.setIndexOptions(IndexOptions.NONE);
            	fieldType.setTokenized(false);
            	fieldType.setStored(true);
            	
                Field field = new Field(CmsSearchField.FIELD_CONTENT_BLOB, CompressionTools.compress(data), fieldType);
                document.add(field);
            }
        }

        // read all properties of the resource
        List<CmsProperty> propertiesSearched = cms.readPropertyObjects(resource, true);
        List<CmsProperty> properties = cms.readPropertyObjects(resource, false);

        Iterator<CmsSearchField> fieldConfigs = getFields().iterator();
        while (fieldConfigs.hasNext()) {
            // check all field configurations 
            CmsSearchField fieldConfig = fieldConfigs.next();
            // generate the content for the field mappings
            StringBuffer text = new StringBuffer();
            Iterator<CmsSearchFieldMapping> mappings = fieldConfig.getMappings().iterator();
            while (mappings.hasNext()) {
                // walk through all mappings and check if content for this is available
                CmsSearchFieldMapping mapping = mappings.next();
                String mapResult = mapping.getStringValue(cms, resource, content, properties, propertiesSearched);
                if (mapResult != null) {
                    // content is available for the mapping
                    // append the result of the mapping to the main result
                    if (text.length() > 0) {
                        // this is a multiple mapped field, append a line break
                        text.append('\n');
                    }
                    text.append(mapResult);
                }
            }
            if (text.length() > 0) {
                // content is available for this field
                Field[] fields = fieldConfig.createFields(text.toString());
                for (Field field : fields)
                	document.add(field);
            }
        }

        // now add the special OpenCms default search fields
        String value;
        Field field;
        FieldType fieldType = new FieldType();
        
        // add the category of the file (this is searched so the value can also be attached on a folder)
        value = CmsProperty.get(CmsPropertyDefinition.PROPERTY_SEARCH_CATEGORY, propertiesSearched).getValue();

        fieldType.setIndexOptions(IndexOptions.DOCS);
		fieldType.setTokenized(false);
		fieldType.setStored(true);
        
        if (CmsStringUtil.isNotEmpty(value)) {
            // all categories are internally stored lower case
            value = value.trim().toLowerCase();
            if (value.length() > 0) {
                field = new Field(CmsSearchField.FIELD_CATEGORY, value, fieldType);
                field.setBoost(0);
                document.add(field);
            }
        } else {
            // synthetic "unknown" category if no category property defined for resource
            field = new Field(
                CmsSearchField.FIELD_CATEGORY,
                "unknown",
                fieldType);
            document.add(field);
        }

        //Agrego si tiene los fields personalizados por tipo de contenido.
        for (Field contentField : content.getCustomFields()){
        	document.add(contentField);
        }
        
        // add all parent folders of the current document
        String parentFolders = getParentFolderTokens(resource.getRootPath());
        
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		fieldType.setTokenized(true);
		fieldType.setStored(false);
		
        field = new Field(CmsSearchField.FIELD_PARENT_FOLDERS, parentFolders, fieldType);
        // set boost of 0 to parent folder field, since parent folder path should have no effect on search result score 
        field.setBoost(0);
        document.add(field);

        // root path is stored again in "plain" format, but not for indexing since I_CmsDocumentFactory.DOC_ROOT is used for that
        // must be indexed as a keyword ONLY to be able to use this when deleting a resource from the index
        
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS);
		fieldType.setTokenized(false);
		fieldType.setStored(true);
		
        document.add(new Field(
            CmsSearchField.FIELD_PATH,
            resource.getRootPath(),fieldType));
        
        //add the tokenized name of the document
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
		fieldType.setTokenized(true);
		fieldType.setStored(true);
		
        document.add(new Field(
            CmsSearchField.FIELD_NAME,
            resource.getRootPath(),fieldType));

        //add name tokenized por search
        document.add(new Field(
                CmsSearchField.FIELD_NAME,
                resource.getName() + " " + resource.getName().replaceAll("\\.", " ").replaceAll("_", " ").replaceAll("-", " "),
                fieldType));

        // add date of creation, content and last modification
        
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS);
		fieldType.setTokenized(false);
		fieldType.setStored(true);
		
        field = new Field(CmsSearchField.FIELD_DATE_CREATED, DateTools.dateToString(
            new Date(resource.getDateCreated()),
            DateTools.Resolution.MILLISECOND), fieldType);
        field.setBoost(0);
        document.add(field);
        

        document.add(new SortedDocValuesField(
        		CmsSearchField.FIELD_DATE_CREATED, 
        		new BytesRef(
        				DateTools.dateToString(
        			            new Date(resource.getDateCreated()),
        			            DateTools.Resolution.MILLISECOND)
        				)));
        
        
        field = new Field(CmsSearchField.FIELD_DATE_CREATED_LOOKUP, DateTools.dateToString(
                new Date(resource.getDateCreated()),
                DateTools.Resolution.DAY), fieldType);
            field.setBoost(0);
            document.add(field);
        
        
        // add date of last modification
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS);
		fieldType.setTokenized(false);
		fieldType.setStored(true);
		
        field = new Field(
            CmsSearchField.FIELD_DATE_LASTMODIFIED,
            DateTools.dateToString(new Date(resource.getDateLastModified()), DateTools.Resolution.MILLISECOND),
            fieldType);
        field.setBoost(0);
        document.add(field);


        document.add(new SortedDocValuesField(
        		CmsSearchField.FIELD_DATE_LASTMODIFIED, 
        		new BytesRef(
        				DateTools.dateToString(
        			            new Date(resource.getDateLastModified()),
        			            DateTools.Resolution.MILLISECOND)
        				)));

        field = new Field(CmsSearchField.FIELD_DATE_LASTMODIFIED_LOOKUP, DateTools.dateToString(
                new Date(resource.getDateLastModified()),
                DateTools.Resolution.DAY), fieldType);
            field.setBoost(0);
            document.add(field);
            
        // add date of content
        
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS);
		fieldType.setTokenized(false);
		fieldType.setStored(true);
		
        field = new Field(CmsSearchField.FIELD_DATE_CONTENT, DateTools.dateToString(
            new Date(resource.getDateContent()),
            DateTools.Resolution.MILLISECOND), fieldType);
        field.setBoost(0);
        document.add(field);

        
        // *************
        
        // add expires date
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS);
		fieldType.setTokenized(false);
		fieldType.setStored(true);
		
        field = new Field(
            CmsSearchField.FIELD_DATE_EXPIRES,
            DateTools.dateToString(new Date(resource.getDateExpired()), DateTools.Resolution.MILLISECOND),
            fieldType);
        field.setBoost(0);
        document.add(field);


        document.add(new SortedDocValuesField(
        		CmsSearchField.FIELD_DATE_EXPIRES, 
        		new BytesRef(
        				DateTools.dateToString(
        			            new Date(resource.getDateExpired()),
        			            DateTools.Resolution.MILLISECOND)
        				)));

        field = new Field(CmsSearchField.FIELD_DATE_EXPIRES_LOOKUP, DateTools.dateToString(
                new Date(resource.getDateExpired()),
                DateTools.Resolution.SECOND), fieldType);
            field.setBoost(0);
            document.add(field);


            // add expires date
            fieldType = new FieldType();
            fieldType.setIndexOptions(IndexOptions.DOCS);
    		fieldType.setTokenized(false);
    		fieldType.setStored(true);
    		
            field = new Field(
                CmsSearchField.FIELD_DATE_RELEASED,
                DateTools.dateToString(new Date(resource.getDateReleased()), DateTools.Resolution.MILLISECOND),
                fieldType);
            field.setBoost(0);
            document.add(field);

            document.add(new SortedDocValuesField(
            		CmsSearchField.FIELD_DATE_RELEASED, 
            		new BytesRef(
            				DateTools.dateToString(
            			            new Date(resource.getDateReleased()),
            			            DateTools.Resolution.MILLISECOND)
            				)));

            field = new Field(CmsSearchField.FIELD_DATE_RELEASED_LOOKUP, DateTools.dateToString(
                    new Date(resource.getDateReleased()),
                    DateTools.Resolution.SECOND), fieldType);
                field.setBoost(0);
                document.add(field);

        
        // *************
        
        
        // add timeRangeDisponibility of content
        
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS);
		fieldType.setTokenized(false);
		fieldType.setStored(true);
		
        field = new Field(CmsSearchField.FIELD_TIMERANGE, Boolean.toString(isResourceInTimeWindow(resource,cms)), fieldType);
        field.setBoost(0);
        document.add(field);

        // special field for VFS documents - add a marker so that the document can be identified as VFS resource
        fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS);
		fieldType.setTokenized(false);
		fieldType.setStored(true);
		
        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
        String typeName = VFS_DOCUMENT_KEY_PREFIX;
        if (type != null) {
            typeName = type.getTypeName();
        }
        document.add(new Field(CmsSearchField.FIELD_TYPE, typeName, fieldType));

        // set individual document boost factor for the search
        float boost = CmsSearchField.BOOST_DEFAULT;
        // note that the priority property IS searched, so you can easily flag whole folders as "high" or "low"
        value = CmsProperty.get(CmsPropertyDefinition.PROPERTY_SEARCH_PRIORITY, propertiesSearched).getValue();
        if (value != null) {
            value = value.trim().toLowerCase();
            if (value.equals(SEARCH_PRIORITY_MAX_VALUE)) {
                boost = 2.0f;
            } else if (value.equals(SEARCH_PRIORITY_HIGH_VALUE)) {
                boost = 1.5f;
            } else if (value.equals(SEARCH_PRIORITY_LOW_VALUE)) {
                boost = 0.5f;
            }
        }
        
        //TODO: Ahora no hay mas boost global! se utiliza unicamente boost por campo.
        // Tampoco en elasticsearch. algunos sugieren multiplicar este campo por el de cada campo en el campo... otros lo desaconsejan
        /*
        if (boost != CmsSearchField.BOOST_DEFAULT) {
            // set individual document boost factor if required
            document.setBoost(boost);
        }
         */
        
        return document;
    }

    protected boolean isResourceInTimeWindow(CmsResource resource, CmsObject cms) {

        return cms.existsResource(
           cms.getRequestContext().removeSiteRoot(resource.getRootPath()),
            CmsResourceFilter.DEFAULT);
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsSearchFieldConfiguration) {
            return ((CmsSearchFieldConfiguration)obj).m_name.equals(m_name);
        }
        return false;
    }

    /**
     * Returns an analyzer that wraps the given base analyzer with the analyzers of this individual field configuration.<p>
     * 
     * @param analyzer the base analyzer to wrap
     * 
     * @return an analyzer that wraps the given base analyzer with the analyzers of this individual field configuration
     */
    public Analyzer getAnalyzer(Analyzer analyzer) {

    	Map<String,Analyzer> analyzerPerField = new HashMap<>();

        // first make map the default hard coded fields
        getAnalyzerForDefaultFields(analyzerPerField);

        Iterator<CmsSearchField> i = m_fields.iterator();
        while (i.hasNext()) {
            // check all fields for individual analyzer configuration
            CmsSearchField field = i.next();
            Analyzer fieldAnalyzer = field.getAnalyzer();
            if (fieldAnalyzer != null) {
                // this field has an individual analyzer configured
                analyzerPerField.put(field.getName(), fieldAnalyzer);
            }
        }

        if (analyzerPerField.size()==0) {
            // at least one field has an individual analyzer configured
            return analyzer;
        }
        
        PerFieldAnalyzerWrapper result = new PerFieldAnalyzerWrapper(analyzer,analyzerPerField);
        // no fields have individual analyzer configured
        return result;
    }

    /**
     * Returns the description of this field configuration.<p>
     * 
     * @return the description of this field configuration
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns a list of all field names (Strings) that are used in generating the search excerpt.<p>
     * 
     * @return a list of all field names (Strings) that are used in generating the search excerpt
     */
    public List<String> getExcerptFieldNames() {

        if (m_excerptFieldNames == null) {
            // lazy initialize the field names
            m_excerptFieldNames = new ArrayList<String>();
            Iterator<CmsSearchField> i = m_fields.iterator();
            while (i.hasNext()) {
                CmsSearchField field = i.next();
                if (field.isInExcerptAndStored()) {
                    m_excerptFieldNames.add(field.getName());
                }
            }
        }

        // create a copy of the list to prevent changes in other classes
        return new ArrayList<String>(m_excerptFieldNames);
    }

    /**
     * Returns the configured {@link CmsSearchField} instance with the given name.<p>
     * 
     * @param name the search field name to look up
     * 
     * @return the configured {@link CmsSearchField} instance with the given name
     */
    public CmsSearchField getField(String name) {

        if (m_fieldLookup == null) {
            // lazy initialize the field names
            m_fieldLookup = new HashMap<String, CmsSearchField>();
            Iterator<CmsSearchField> i = m_fields.iterator();
            while (i.hasNext()) {
                CmsSearchField field = i.next();
                m_fieldLookup.put(field.getName(), field);
            }
        }
        return m_fieldLookup.get(name);
    }

    /**
     * Returns the list of configured field names (Strings).<p>
     * 
     * @return the list of configured field names (Strings)
     */
    public List<String> getFieldNames() {

        if (m_fieldNames == null) {
            // lazy initialize the field names
            m_fieldNames = new ArrayList<String>();
            Iterator<CmsSearchField> i = m_fields.iterator();
            while (i.hasNext()) {
                m_fieldNames.add((i.next()).getName());
            }
        }

        // create a copy of the list to prevent changes in other classes
        return new ArrayList<String>(m_fieldNames);
    }

    /**
     * Returns the list of configured {@link CmsSearchField} instances.<p>
     * 
     * @return the list of configured {@link CmsSearchField} instances
     */
    public List<CmsSearchField> getFields() {

        return m_fields;
    }

    /**
     * Returns the name of this field configuration.<p>
     *
     * @return the name of this field configuration
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_name.hashCode();
    }

    /**
     * Sets the description of this field configuration.<p>
     * 
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the name of this field configuration.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }

    /**
     * Returns an analyzer that wraps the given base analyzer with special analyzers required for the hard coded 
     * document fields.<p>
     * 
     * @param analyzer the base analyzer to wrap
     * 
     * @return an analyzer that wraps the given base analyzer for the hard coded document fields
     * 
     * @see #getAnalyzer(Analyzer)
     */
    protected void getAnalyzerForDefaultFields(Map<String,Analyzer> analyzerPerField) {
        analyzerPerField.put(CmsSearchField.FIELD_PARENT_FOLDERS, new WhitespaceAnalyzer());
    }
}