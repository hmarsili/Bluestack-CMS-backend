package org.opencms.search;
 
import org.opencms.util.CmsUUID;
 
/**
* An index resource is a wrapper class that contains the data of a
* Cms resource specified by a Lucene search result document.<p>
* 
*/

public abstract class A_CmsIndexResource {
 
     /** Concrete data object. */
     protected Object   m_data;
 
     /** Id of the object. */
     protected CmsUUID m_id;
 
     /** Mimetype of the object, <code>null</code> if the object is a <code>CmsMasterDataSet</code>. */
     protected String   m_mimeType;
 
     /** Name of the object. */
     protected String   m_name;
 
     /** Path to access the object. */
     protected String   m_path;
 
     /** Type of the object. */
     protected int m_type;
 
     /**
      * Returns the wrapped data object.<p>
      * 
      * The concrete type of the data object is either <code>CmsResource</code>
      * or <code>CmsMasterDataSet</code>
      * 
      * @return the wrapped data object
      */
     public Object   getData() {
 
         return m_data;
     }
 
     /**
      * Returns the document key for the search manager.<p> 
      * 
      * @param withMimeType true if the mime type should be included in the key
      * @return the document key for the search manager
      */
     public abstract String   getDocumentKey(boolean withMimeType);
 
     /**
      * Returns the id of the wrapped object.<p>
      * 
      * @return the id
      */
     public CmsUUID getId() {
 
         return m_id;
     }
 
     /**
      * Returns the mimetype of the wrapped object.<p>
      * 
      * @return the mimetype of the wrapped object or <code>null</code>
      */
     public String   getMimetype() {
 
         return m_mimeType;
     }
 
     /**
      * Returns the name of the wrapped object.<p>
      * 
      * @return the name of the wrapped object
      */
     public String   getName() {
 
        return m_name;
     }
 
     /**
      * Returns the access path of the wrapped object.<p>
      * 
      * @return the access path of the wrapped object
      */
     public String   getRootPath() {
 
         return m_path;
     }
 
     /**
      * Returns the type of the wrapped object.<p>
      * 
      * The type is either the type of the wrapped <code>CmsResource</code> 
      * or the SubId of the <code>CmsMasterDataSet</code>.
      * 
      * @return the type of the wrapped object
      */
     public int getType() {
 
         return m_type;
     }
}