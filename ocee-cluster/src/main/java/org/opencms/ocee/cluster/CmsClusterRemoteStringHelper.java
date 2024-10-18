package org.opencms.ocee.cluster;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsLog;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleVersion;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.fields.CmsSearchField;
import org.opencms.search.fields.CmsSearchFieldConfiguration;
import org.opencms.util.CmsStringUtil;

public final class CmsClusterRemoteStringHelper {
   public static final String DOC_TYPE_CONFIG_PROP_LIST;
   public static final String MODULE_PROP_LIST;
   public static final String SCHEDULED_JOB_PROP_LIST;
   public static final String SEARCH_INDEX_PROP_LIST;
   public static final String WRITE_SEARCH_INDEX_PROP_LIST;
   public static final String SEARCH_INDEX_SOURCE_PROP_LIST;
   private static final Log LOG = CmsLog.getLog(CmsClusterRemoteStringHelper.class);
   private static PropertyUtilsBean m_propUtils = new PropertyUtilsBean();

   private CmsClusterRemoteStringHelper() {
   }

   public static CmsSearchDocumentType createDocumentTypeConfig(String name) {
      CmsSearchDocumentType docType = new CmsSearchDocumentType();
      docType.setName(name);
      return docType;
   }

   public static CmsSearchIndexSource createIndexSource(String name, int typeSize, int resNameSize) {
      CmsSearchIndexSource idxSource = new CmsSearchIndexSource();

      int i;
      for(i = 0; i < typeSize; ++i) {
         idxSource.addDocumentType(new String());
      }

      for(i = 0; i < resNameSize; ++i) {
         idxSource.addResourceName(new String());
      }

      try {
         Field nameField = CmsSearchIndexSource.class.getDeclaredField("m_name");
         nameField.setAccessible(true);
         nameField.set(idxSource, name);
      } catch (Exception var5) {
         if (LOG.isErrorEnabled()) {
            LOG.error(var5.getLocalizedMessage(), var5);
         }
      }

      return idxSource;
   }

   public static Object createInstance(String className) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
      return Class.forName(className, true, Thread.currentThread().getContextClassLoader()).newInstance();
   }

   public static CmsModule createModule(String version) {
      CmsModule module = new CmsModule();
      module.setDependencies(new ArrayList());
      module.setExplorerTypes(new ArrayList());
      module.setResources(new ArrayList());
      module.setExportPoints(new ArrayList());
      module.setParameters(new TreeMap());
      module.setResourceTypes(new ArrayList());
      if (version != null) {
         try {
            Field versionField = CmsModule.class.getDeclaredField("m_version");
            versionField.setAccessible(true);
            versionField.set(module, new CmsModuleVersion(version));
         } catch (Exception var3) {
            if (LOG.isErrorEnabled()) {
               LOG.error(var3.getLocalizedMessage(), var3);
            }
         }
      }

      return module;
   }

   public static CmsScheduledJobInfo createScheduledJob(String id) {
      CmsScheduledJobInfo job = new CmsScheduledJobInfo();
      job.setContextInfo(new CmsContextInfo());
      job.setParameters(new TreeMap());
      if (id == null) {
         return job;
      } else {
         try {
            Field idField = CmsScheduledJobInfo.class.getDeclaredField("m_id");
            idField.setAccessible(true);
            idField.set(job, id);
         } catch (Exception var3) {
            if (LOG.isErrorEnabled()) {
               LOG.error(var3.getLocalizedMessage(), var3);
            }
         }

         return job;
      }
   }

   public static CmsSearchIndex createSearchIndex(String name, int fieldSize, int sourceNamesSize) {
      CmsSearchIndex index = new CmsSearchIndex();
      int i;
      if (fieldSize > 0) {
         index.setFieldConfiguration(new CmsSearchFieldConfiguration());

         for(i = 0; i < fieldSize; ++i) {
            index.getFieldConfiguration().addField(new CmsSearchField());
         }
      }

      for(i = 0; i < sourceNamesSize; ++i) {
         index.addSourceName(new String());
      }

      try {
         Field nameField = CmsSearchIndex.class.getDeclaredField("m_name");
         nameField.setAccessible(true);
         nameField.set(index, name);
         Field sourcesField = CmsSearchIndex.class.getDeclaredField("m_sources");
         sourcesField.setAccessible(true);
         sourcesField.set(index, new ArrayList());
      } catch (Exception var6) {
         if (LOG.isErrorEnabled()) {
            LOG.error(var6.getLocalizedMessage(), var6);
         }
      }

      if (index.getFieldConfigurationName() == null) {
         index.setFieldConfigurationName("standard");
      }

      return index;
   }

   public static void mapToObject(Map props, Object obj) {
      Map _props = new TreeMap(props);
      Iterator it = _props.entrySet().iterator();

      while(true) {
         Entry entry;
         String prop;
         do {
            if (!it.hasNext()) {
               return;
            }

            entry = (Entry)it.next();
            prop = (String)entry.getKey();
         } while(prop.startsWith("__class["));

         try {
            Class<?> type = m_propUtils.getPropertyType(obj, prop);
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (type == null) {
               m_propUtils.setProperty(obj, prop, entry.getValue());
            } else if (type.equals(Integer.TYPE)) {
               m_propUtils.setProperty(obj, prop, Integer.valueOf(entry.getValue().toString()));
            } else if (type.equals(Boolean.TYPE)) {
               m_propUtils.setProperty(obj, prop, Boolean.valueOf(entry.getValue().toString()));
            } else if (type.equals(Long.TYPE)) {
               m_propUtils.setProperty(obj, prop, Long.valueOf(entry.getValue().toString()));
            } else if (type.equals(Float.TYPE)) {
               m_propUtils.setProperty(obj, prop, Float.valueOf(entry.getValue().toString()));
            } else if (type.equals(Double.TYPE)) {
               m_propUtils.setProperty(obj, prop, Double.valueOf(entry.getValue().toString()));
            } else {
               String clazz;
               int pos;
               String className;
               if (Map.class.isAssignableFrom(type) && entry.getValue().toString().endsWith("]__") && entry.getValue().toString().startsWith("__class:")) {
                  if (m_propUtils.isWriteable(obj, prop)) {
                     clazz = entry.getValue().toString();
                     pos = clazz.indexOf(91);
                     List keys = CmsStringUtil.splitAsList(clazz.substring(pos + 2, clazz.length() - 4), ',', true);
                     clazz = clazz.substring("__class:".length(), pos);
                     Map<String, Object> map = (Map)classLoader.loadClass(clazz).newInstance();
                     m_propUtils.setProperty(obj, prop, map);
                     Iterator itKeys = keys.iterator();

                     while(itKeys.hasNext()) {
                        className = (String)itKeys.next();
                        className = (String)_props.get("__class[" + prop + "(" + className + ")]");
                        map.put(className, classLoader.loadClass(className).newInstance());
                     }
                  }
               } else if (List.class.isAssignableFrom(type) && entry.getValue().toString().endsWith("]__") && entry.getValue().toString().startsWith("__class:")) {
                  if (m_propUtils.isWriteable(obj, prop)) {
                     clazz = entry.getValue().toString();
                     pos = clazz.indexOf(91);
                     int size = Integer.parseInt(clazz.substring(pos + 1, clazz.length() - 3));
                     clazz = clazz.substring("__class:".length(), pos);
                     List<Object> list = (List)classLoader.loadClass(clazz).newInstance();
                     m_propUtils.setProperty(obj, prop, list);

                     for(int i = 0; i < size; ++i) {
                        className = (String)_props.get("__class[" + prop + "[" + i + "]]");
                        list.add(classLoader.loadClass(className).newInstance());
                     }
                  }
               } else {
                  m_propUtils.setProperty(obj, prop, entry.getValue());
               }
            }
         } catch (Exception var14) {
            if (LOG.isErrorEnabled()) {
               LOG.error(var14.getLocalizedMessage(), var14);
            }
         }
      }
   }

   public static Map objToMap(Object obj, String propList) {
      return objToMap(obj, propList, "");
   }

   public static Map objToMap(Object obj, String propList, String prefix) {
      Map map = new HashMap();
      Iterator it = CmsStringUtil.splitAsList(propList, ',', true).iterator();

      label85:
      while(it.hasNext()) {
         String prop = (String)it.next();
         int pos = prop.indexOf("(");
         String subprops = "";
         if (pos > -1) {
            subprops = prop.substring(pos + 1, prop.length() - 1).replace('-', ',');
            prop = prop.substring(0, pos);
         }

         try {
            String key = prefix + prop;
            Object propVal = m_propUtils.getProperty(obj, prop);
            if (propVal instanceof Map) {
               map.put(key, "__class:" + TreeMap.class.getName() + "[" + ((Map)propVal).keySet().toString() + "]__");
               Iterator itMap = ((Map)propVal).entrySet().iterator();

               while(true) {
                  while(true) {
                     if (!itMap.hasNext()) {
                        continue label85;
                     }

                     Entry entry = (Entry)itMap.next();
                     String entryKey = key + "(" + entry.getKey().toString() + ")";
                     if (entry.getValue() != null) {
                        map.put("__class[" + entryKey + "]", entry.getValue().getClass().getName());
                     }

                     if (pos > -1 && entry.getValue() != null) {
                        map.putAll(objToMap(entry.getValue(), subprops, entryKey + "."));
                     } else {
                        map.put(entryKey, escapeJavaString(entry.getValue()));
                     }
                  }
               }
            } else if (!(propVal instanceof List)) {
               if (propVal != null) {
                  String value = "";

                  try {
                     value = value + propVal;
                  } catch (Exception var14) {
                     value = value + var14.getLocalizedMessage();
                  }

                  map.put(key, escapeJavaString(value));
               }
            } else {
               List list = (List)propVal;
               map.put(key, "__class:" + ArrayList.class.getName() + "[" + list.size() + "]__");

               for(int i = 0; i < list.size(); ++i) {
                  Object listObj = list.get(i);
                  String entryKey = key + "[" + i + "]";
                  if (listObj != null) {
                     map.put("__class[" + entryKey + "]", listObj.getClass().getName());
                  }

                  if (pos > -1 && listObj != null) {
                     map.putAll(objToMap(listObj, subprops, entryKey + "."));
                  } else {
                     map.put(entryKey, escapeJavaString(listObj));
                  }
               }
            }
         } catch (NestedNullException var15) {
         } catch (Exception var16) {
            if (LOG.isErrorEnabled()) {
               LOG.error(var16.getLocalizedMessage(), var16);
            }
         }
      }

      return map;
   }

   private static String escapeJavaString(Object string) {
      if (string == null) {
         return null;
      } else {
         String str = string.toString();
         Map<String, String> substitutions = new HashMap();
         substitutions.put("\n", "\\n");
         substitutions.put("\b", "\\b");
         substitutions.put("\t", "\\t");
         substitutions.put("\f", "\\f");
         substitutions.put("\r", "\\r");
         substitutions.put("\"", "\\\"");
         substitutions.put("\\", "\\\\");
         return CmsStringUtil.substitute(str, substitutions);
      }
   }

   static {
      StringBuffer propList = new StringBuffer();
      propList.append("jobName,className,cronExpression,reuseInstance,active,parameters,");
      propList.append("contextInfo.userName,contextInfo.projectName,contextInfo.requestedUri,");
      propList.append("contextInfo.localeName,contextInfo.encoding,contextInfo.remoteAddr,");
      propList.append("contextInfo.siteRoot");
      SCHEDULED_JOB_PROP_LIST = propList.toString();
      propList = new StringBuffer();
      propList.append("name,fieldConfiguration.name,rebuildMode,project,locale");
      SEARCH_INDEX_PROP_LIST = propList.toString();
      propList = new StringBuffer();
      propList.append("rebuildMode,project");
      WRITE_SEARCH_INDEX_PROP_LIST = propList.toString();
      propList = new StringBuffer();
      propList.append("name,className");
      DOC_TYPE_CONFIG_PROP_LIST = propList.toString();
      propList = new StringBuffer();
      propList.append("name,indexerClassName,documentTypes,resourcesNames");
      SEARCH_INDEX_SOURCE_PROP_LIST = propList.toString();
      propList = new StringBuffer();
      propList.append("name,niceName,group,actionClass,");
      propList.append("description,authorName,authorEmail,");
      propList.append("dateCreated,userInstalled,dateInstalled,");
      propList.append("version.version,parameters,resources,");
      propList.append("dependencies(name-version.version),");
      propList.append("exportPoints(uri-configuredDestination),");
      MODULE_PROP_LIST = propList.toString();
   }
}
