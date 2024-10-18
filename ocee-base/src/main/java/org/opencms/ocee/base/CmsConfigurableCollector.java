package org.opencms.ocee.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.A_CmsResourceCollector;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

public class CmsConfigurableCollector extends A_CmsResourceCollector {
   public static final String COLLECTOR_NAME = "configurableCollector";
   public static final String NODE_FOLDER = "Folder";
   public static final String NODE_PROPERTY = "Property";
   public static final String NODE_RESCONFIG = "ResConfig";
   public static final String NODE_RESTYPE = "ResType";
   private final List o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public CmsConfigurableCollector() {
      this.setDefaultCollectorName("configurableCollector");
      this.setDefaultCollectorParam("");
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = new ArrayList();
   }

   public CmsConfigurableCollector(List collectorConfigurations) {
      this();
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.addAll(collectorConfigurations);
   }

   public List getCollectorConfigurations() {
      return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   public List getCollectorNames() {
      return Collections.singletonList("configurableCollector");
   }

   public String getCreateLink(CmsObject cms, String collectorName, String param) {
      return null;
   }

   public String getCreateParam(CmsObject cms, String collectorName, String param) {
      return null;
   }

   public List getResults(CmsObject cms, String collectorName, String param) throws CmsDataAccessException, CmsException {
      if (collectorName == null) {
         collectorName = "configurableCollector";
      }

      return this.getAllInFolder(cms, param);
   }

   public void setCollectorConfigurations(List collectorConfigurations) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.clear();
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super.addAll(collectorConfigurations);
   }

   protected void checkParams() {
      if (this.getDefaultCollectorName() == null) {
         throw new CmsIllegalArgumentException(Messages.get().container("ERR_COLLECTOR_DEFAULTS_INVALID_2", this.getDefaultCollectorName(), this.getDefaultCollectorParam()));
      }
   }

   protected List getAllInFolder(CmsObject cms, String param) throws CmsException, CmsIllegalArgumentException {
      List collectorConfigurations = this.getCollectorConfigurations();
      if (CmsStringUtil.isNotEmpty(param)) {
         try {
            collectorConfigurations = this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(cms, param);
         } catch (CmsException var14) {
            throw new CmsXmlException(Messages.get().container("ERR_COLLECTOR_CONFIG_INVALID_1", param));
         }
      }

      Set collected = new HashSet();

      for(int i = 0; i < collectorConfigurations.size(); ++i) {
         CmsCollectorConfiguration config = (CmsCollectorConfiguration)collectorConfigurations.get(i);
         CmsResourceFilter filter = CmsResourceFilter.DEFAULT.addExcludeFlags(1024);
         if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(config.getResourceType())) {
            filter = filter.addRequireType(config.getResourceTypeId());
         }

         List resources = cms.readResources(config.getUri(), filter, config.isRecursive());
         if (config.getProperties().size() <= 0) {
            collected.addAll(resources);
         } else {
            for(int k = resources.size() - 1; k > -1; --k) {
               CmsResource res = (CmsResource)resources.get(k);
               cms.readPropertyObjects(res, false);
               boolean addToResult = true;

               for(int m = config.getProperties().size() - 1; m > -1; --m) {
                  String propertyDef = (String)config.getProperties().get(m);
                  if (CmsStringUtil.isEmptyOrWhitespaceOnly(cms.readPropertyObject(res, propertyDef, false).getValue())) {
                     addToResult = false;
                     break;
                  }
               }

               if (addToResult) {
                  collected.add(res);
               }
            }
         }
      }

      List result = new ArrayList(collected);
      Collections.sort(result, CmsResource.COMPARE_ROOT_PATH);
      Collections.reverse(result);
      return result;
   }

   private List o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(CmsObject cms, String resourceName) throws CmsException {
      List result = new ArrayList();
      Locale locale = cms.getRequestContext().getLocale();
      CmsResource res = cms.readResource(resourceName);
      CmsXmlContent xml = CmsXmlContentFactory.unmarshal(cms, CmsFile.upgrade(res, cms));
      List configurations = xml.getValues("ResConfig", locale);
      int configurationSize = configurations.size();

      for(int i = 0; i < configurationSize; ++i) {
         I_CmsXmlContentValue resConfig = (I_CmsXmlContentValue)configurations.get(i);
         String resConfigPath = resConfig.getPath() + "/";
         String resType = xml.getStringValue(cms, resConfigPath + "ResType", locale);
         String folder = xml.getStringValue(cms, resConfigPath + "Folder", locale);
         List propertyValues = xml.getValues(resConfigPath + "Property", locale);
         List properties = new ArrayList(propertyValues.size());

         for(int k = propertyValues.size() - 1; k > -1; --k) {
            I_CmsXmlContentValue value = (I_CmsXmlContentValue)propertyValues.get(k);
            properties.add(value.getStringValue(cms));
         }

         result.add(new CmsCollectorConfiguration(folder, resType, properties));
      }

      return result;
   }
}
