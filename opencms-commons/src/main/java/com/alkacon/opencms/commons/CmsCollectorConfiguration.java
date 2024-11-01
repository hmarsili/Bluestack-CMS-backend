package com.alkacon.opencms.commons;

import java.util.ArrayList;
import java.util.List;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.OpenCms;

public class CmsCollectorConfiguration {
   private List m_properties;
   private boolean m_recursive;
   private String m_resourceType;
   private String m_uri;

   public CmsCollectorConfiguration(String uri) {
      this.m_recursive = true;
      this.m_uri = uri;
      this.m_properties = new ArrayList();
   }

   public CmsCollectorConfiguration(String uri, String resourceType, List properties) {
      this(uri);
      this.m_resourceType = resourceType;
      this.setProperties(properties);
   }

   public CmsCollectorConfiguration(String uri, String resourceType, List properties, boolean recurse) {
      this(uri, resourceType, properties);
      this.m_recursive = recurse;
   }

   public List getProperties() {
      return this.m_properties;
   }

   public String getResourceType() {
      return this.m_resourceType;
   }

   public int getResourceTypeId() {
      try {
         return OpenCms.getResourceManager().getResourceType(this.getResourceType()).getTypeId();
      } catch (CmsLoaderException var2) {
         return -1;
      }
   }

   public String getUri() {
      return this.m_uri;
   }

   public boolean isRecursive() {
      return this.m_recursive;
   }

   public void setProperties(List properties) {
      this.m_properties.clear();
      if (properties != null) {
         this.m_properties.addAll(properties);
      }

   }

   public void setRecursive(boolean recursive) {
      this.m_recursive = recursive;
   }

   public void setResourceType(String resourceType) {
      this.m_resourceType = resourceType;
   }

   public void setUri(String uri) {
      this.m_uri = uri;
   }
}
