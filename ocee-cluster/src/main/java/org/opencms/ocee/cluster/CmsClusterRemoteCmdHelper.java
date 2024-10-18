package org.opencms.ocee.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsConfigurationCopyResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleVersion;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.search.CmsSearchDocumentType;
import org.opencms.search.CmsSearchIndex;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.CmsExplorerContextMenuItem;
import org.opencms.workplace.explorer.CmsExplorerTypeAccess;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.menu.CmsMenuRuleTranslator;

public final class CmsClusterRemoteCmdHelper {
   private static final String CMD_ACTIVATE_SCHEDULED_JOBS = "activateScheduledJobs";
   private static final String CMD_CREATE_MODULE = "createModule";
   private static final String CMD_DELETE_MODULE = "deleteModule";
   private static final String CMD_DELETE_SCHEDULED_JOB = "deleteScheduledJob";
   private static final String CMD_DELETE_SEARCH_INDEX = "deleteSearchIndex";
   private static final String CMD_EXISTS_MODULE = "existsModule";
   private static final String CMD_EXISTS_SCHEDULED_JOB = "existsScheduledJob";
   private static final String CMD_GET_ANALYZERS_NAMES = "getAnalyzerNames";
   private static final String CMD_GET_DOC_TYPE_CONFIG_NAMES = "getDocumentTypeConfigs";
   private static final String CMD_GET_EXECUTION_TIME_FOR_JOB = "getExecutionTimeForJob";
   private static final String CMD_GET_FIELD_CONFIG_NAMES = "getFieldConfigurationNames";
   private static final String CMD_GET_INDEX_SOURCE_NAMES = "getIndexSourceNames";
   private static final String CMD_GET_MODULE_NAMES = "getModuleNames";
   private static final String CMD_GET_MODULES = "getModules";
   private static final String CMD_GET_PROJECT_NAMES = "getAllProjectNames";
   private static final String CMD_GET_SCHEDULED_JOB_IDS = "getScheduledJobIds";
   private static final String CMD_GET_SEARCH_INDEX_NAMES = "getSearchIndexNames";
   private static final String CMD_MAP_PUT = "Map.put";
   private static final String CMD_OVERWRITE_SCHEDULED_JOB = "overwriteScheduledJob";
   private static final String CMD_READ_ALL_MODULE_PARAMETERS = "readAllModuleParameters";
   private static final String CMD_READ_DOC_TYPE_CONFIG = "readDocumentTypeConfig";
   private static final String CMD_READ_INDEX_SOURCE = "readIndexSource";
   private static final String CMD_READ_MODULE_AUTHORINFOS = "readModuleAuthorInfos";
   private static final String CMD_READ_MODULE_DEPENDENCIES = "readModuleDependencies";
   private static final String CMD_READ_MODULE_PARAMETERS = "readModuleParameters";
   private static final String CMD_READ_MODULE_RESOURCES = "readModuleResources";
   private static final String CMD_READ_MODULE_RESOURCETYPES = "readModuleResourceTypes";
   private static final String CMD_READ_SCHEDULED_JOB = "readScheduledJob";
   private static final String CMD_READ_SEARCH_INDEX = "readSearchIndex";
   private static final String CMD_REBUILD_SEARCH_INDEXES = "rebuildSearchIndexes";
   private static final String CMD_SET_MODULE_PARAMETERS = "setModuleParameters";
   private static final String CMD_SYSTEM_CURRENT_TIME_MILLIS = "System.currentTimeMillis";
   private static final String CMD_WRITE_MODULE_CONFIGURATION = "writeModuleConfiguration";
   private static final String CMD_WRITE_SCHEDULED_JOB = "writeScheduledJob";
   private static final String CMD_WRITE_SEARCH_CONFIGURATION = "writeSearchConfiguration";
   private static final String CMD_WRITE_SEARCH_INDEX = "writeSearchIndex";
   private static final String CMD_WRITE_SEARCH_INDEX_SOURCE = "writeSearchIndexSource";
   private static final String CMD_WRITE_SYSTEM_CONFIGURATION = "writeSystemConfiguration";
   private static final String CMS_CREATE_SCHEDULED_JOB_IF_MISSING = "createScheduledJobIsMissing";
   private static final Log LOG = CmsLog.getLog(CmsClusterRemoteCmdHelper.class);
   private static Map m_cmds;
   private static final String MACRO_ACTIVATE = "${activate}";
   private static final String MACRO_ID = "${id}";
   private static final String MACRO_KEY = "${key}";
   private static final String MACRO_NAME = "${name}";
   private static final String MACRO_PROPS_MAP = "${propsMap}";
   private static final String MACRO_SET_PROPS = "${setProps}";
   private static final String MACRO_VALUE = "${value}";
   private static final String MACRO_VERSION = "${version}";

   private CmsClusterRemoteCmdHelper() {
   }

   public static void activateScheduleJobs(CmsObject cms, CmsClusterServer server, List ids, boolean activate) {
      StringBuffer cmd = new StringBuffer();
      Iterator itIds = ids.iterator();

      while(itIds.hasNext()) {
         String id = (String)itIds.next();
         Map substitutions = new HashMap();
         substitutions.put("${id}", id);
         substitutions.put("${activate}", Boolean.toString(activate));
         cmd.append(getCmd("activateScheduledJobs", substitutions));
      }

      cmd.append(getCmd("writeSystemConfiguration", (Map)null));
      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static void createIndexSource(CmsObject cms, CmsClusterServer server, CmsSearchIndexSource indexSource) {
      StringBuffer cmd = new StringBuffer();
      Map substitutions = new HashMap();
      substitutions.put("${propsMap}", generateMapToString(CmsClusterRemoteStringHelper.objToMap(indexSource, CmsClusterRemoteStringHelper.SEARCH_INDEX_SOURCE_PROP_LIST)));
      substitutions.put("${name}", indexSource.getName());
      cmd.append(getCmd("writeSearchIndexSource", substitutions));
      cmd.append(getCmd("writeSearchConfiguration", (Map)null));
      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static void createModules(CmsObject cms, CmsClusterServer server, List modules) {
      StringBuffer cmd = new StringBuffer();
      Iterator itModules = modules.iterator();

      while(itModules.hasNext()) {
         CmsModule module = (CmsModule)itModules.next();
         Map substitutions = new HashMap();
         substitutions.put("${propsMap}", generateMapToString(CmsClusterRemoteStringHelper.objToMap(module, CmsClusterRemoteStringHelper.MODULE_PROP_LIST)));
         substitutions.put("${setProps}", setModuleResourceTypes(module));
         substitutions.put("${version}", module.getVersion().getVersion());
         cmd.append(getCmd("createModule", substitutions));
      }

      cmd.append(getCmd("writeModuleConfiguration", (Map)null));
      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static void createScheduleJobIfMissing(CmsObject cms, CmsClusterServer server, CmsScheduledJobInfo job) {
      StringBuffer cmd = new StringBuffer();
      Map substitutions = new HashMap();
      substitutions.put("${name}", job.getJobName());
      substitutions.put("${propsMap}", generateMapToString(CmsClusterRemoteStringHelper.objToMap(job, CmsClusterRemoteStringHelper.SCHEDULED_JOB_PROP_LIST)));
      cmd.append(getCmd("overwriteScheduledJob", substitutions));
      cmd.append(getCmd("createScheduledJobIsMissing", substitutions));
      cmd.append(getCmd("writeSystemConfiguration", (Map)null));
      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static void deleteModules(CmsObject cms, CmsClusterServer server, List moduleNames) {
      StringBuffer cmd = new StringBuffer();
      Iterator itMdoules = moduleNames.iterator();

      while(itMdoules.hasNext()) {
         String module = (String)itMdoules.next();
         cmd.append(getCmd("deleteModule", "${name}", module));
      }

      cmd.append(getCmd("writeModuleConfiguration", (Map)null));
      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static void deleteScheduleJobs(CmsObject cms, CmsClusterServer server, List ids) {
      StringBuffer cmd = new StringBuffer();
      Iterator itIds = ids.iterator();

      while(itIds.hasNext()) {
         String id = (String)itIds.next();
         cmd.append(getCmd("deleteScheduledJob", "${id}", id));
      }

      cmd.append(getCmd("writeSystemConfiguration", (Map)null));
      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static void deleteSearchIndex(CmsObject cms, CmsClusterServer server, String indexName) {
      StringBuffer cmd = new StringBuffer();
      Map substitutions = new HashMap();
      substitutions.put("${name}", indexName);
      cmd.append(getCmd("deleteSearchIndex", substitutions));
      cmd.append(getCmd("writeSearchConfiguration", (Map)null));
      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static boolean existsModule(CmsObject cms, CmsClusterServer server, String moduleName) {
      String cmd = getCmd("existsModule", "${name}", moduleName);
      String ret = (String)server.executeCmd(cms, cmd, CmsClusterServer.RET_MODE_STRING);
      return Boolean.valueOf(ret);
   }

   public static boolean existsScheduleJob(CmsObject cms, CmsClusterServer server, String jobId) {
      String cmd = getCmd("existsScheduledJob", "${id}", jobId);
      String ret = (String)server.executeCmd(cms, cmd, CmsClusterServer.RET_MODE_STRING);
      return Boolean.valueOf(ret);
   }

   public static List getAllProjects(CmsObject cms, CmsClusterServer server) {
      List list = (List)server.executeCmd(cms, getCmd("getAllProjectNames", (Map)null), CmsClusterServer.RET_MODE_LIST);
      return list;
   }

   public static List getAnalyzers(CmsObject cms, CmsClusterServer server) {
      List list = (List)server.executeCmd(cms, getCmd("getAnalyzerNames", (Map)null), CmsClusterServer.RET_MODE_LIST);
      return list;
   }

   public static CmsSearchDocumentType getDocumentTypeConfig(CmsObject cms, CmsClusterServer server, String name) {
      Map data = (Map)server.executeCmd(cms, getCmd("readDocumentTypeConfig", "${name}", name), CmsClusterServer.RET_MODE_MAP);
      CmsSearchDocumentType docType = CmsClusterRemoteStringHelper.createDocumentTypeConfig((String)data.get("name"));
      data.remove("name");
      CmsClusterRemoteStringHelper.mapToObject(data, docType);
      return docType;
   }

   public static List getDocumentTypeConfigs(CmsObject cms, CmsClusterServer server) {
      List list = (List)server.executeCmd(cms, getCmd("getDocumentTypeConfigs", (Map)null), CmsClusterServer.RET_MODE_LIST);
      List docTypes = new ArrayList();
      Iterator it = list.iterator();

      while(it.hasNext()) {
         String name = (String)it.next();
         docTypes.add(getDocumentTypeConfig(cms, server, name));
      }

      return docTypes;
   }

   public static List getExecutionTimesForJob(CmsObject cms, CmsClusterServer server, String jobId) {
      List list = (List)server.executeCmd(cms, getCmd("getExecutionTimeForJob", "${id}", jobId), CmsClusterServer.RET_MODE_LIST);

      for(int i = 0; i < list.size(); ++i) {
         list.set(i, Long.valueOf((String)list.get(i)));
      }

      return list;
   }

   public static List getFieldConfigurations(CmsObject cms, CmsClusterServer server) {
      List list = (List)server.executeCmd(cms, getCmd("getFieldConfigurationNames", (Map)null), CmsClusterServer.RET_MODE_LIST);
      return list;
   }

   public static CmsSearchIndexSource getIndexSource(CmsObject cms, CmsClusterServer server, String name) {
      Map data = (Map)server.executeCmd(cms, getCmd("readIndexSource", "${name}", name), CmsClusterServer.RET_MODE_MAP);
      if (data == null) {
         return null;
      } else {
         int typeSize = Integer.valueOf((String)data.remove("documentTypes.size"));
         int resNamesSize = Integer.valueOf((String)data.remove("resourceNames.size"));
         CmsSearchIndexSource idxSource = CmsClusterRemoteStringHelper.createIndexSource((String)data.get("name"), typeSize, resNamesSize);
         data.remove("name");
         CmsClusterRemoteStringHelper.mapToObject(data, idxSource);
         return idxSource;
      }
   }

   public static List getIndexSources(CmsObject cms, CmsClusterServer server) {
      List list = (List)server.executeCmd(cms, getCmd("getIndexSourceNames", (Map)null), CmsClusterServer.RET_MODE_LIST);
      List indexSrcList = new ArrayList();
      Iterator it = list.iterator();

      while(it.hasNext()) {
         String name = (String)it.next();
         indexSrcList.add(getIndexSource(cms, server, name));
      }

      return indexSrcList;
   }

   public static Map getModuleAuthorInfos(CmsObject cms, CmsClusterServer server) {
      return (Map)server.executeCmd(cms, getCmd("readModuleAuthorInfos", (Map)null), CmsClusterServer.RET_MODE_MAP);
   }

   public static Map getModuleDependencies(CmsObject cms, CmsClusterServer server) {
      return (Map)server.executeCmd(cms, getCmd("readModuleDependencies", (Map)null), CmsClusterServer.RET_MODE_MAP);
   }

   public static Map getModuleParameters(CmsObject cms, CmsClusterServer server, String moduleName) {
      return moduleName != null ? (Map)server.executeCmd(cms, getCmd("readModuleParameters", "${name}", moduleName), CmsClusterServer.RET_MODE_MAP) : (Map)server.executeCmd(cms, getCmd("readAllModuleParameters", (Map)null), CmsClusterServer.RET_MODE_MAP);
   }

   public static Map getModuleResources(CmsObject cms, CmsClusterServer server) {
      return (Map)server.executeCmd(cms, getCmd("readModuleResources", (Map)null), CmsClusterServer.RET_MODE_MAP);
   }

   public static Map getModuleResourceTypes(CmsObject cms, CmsClusterServer server) {
      return (Map)server.executeCmd(cms, getCmd("readModuleResourceTypes", (Map)null), CmsClusterServer.RET_MODE_MAP);
   }

   public static List getModules(CmsObject cms, CmsClusterServer server) {
      List list = (List)server.executeCmd(cms, getCmd("getModuleNames", (Map)null), CmsClusterServer.RET_MODE_LIST);
      List modules = new ArrayList();
      Map data = (Map)server.executeCmd(cms, getCmd("getModules", (Map)null), CmsClusterServer.RET_MODE_MAP);
      Iterator it = list.iterator();

      while(it.hasNext()) {
         String name = (String)it.next();
         CmsModule module = new CmsModule(name, (String)data.get(name + ".niceName"), (String)data.get(name + ".group"), (String)null, (String)null, new CmsModuleVersion((String)data.get(name + ".version")), (String)null, (String)null, 0L, (String)null, 0L, (List)null, (List)null, (List)null, (Map)null);
         modules.add(module);
      }

      return modules;
   }

   public static CmsScheduledJobInfo getScheduledJob(CmsObject cms, CmsClusterServer server, String id) {
      Map data = (Map)server.executeCmd(cms, getCmd("readScheduledJob", "${id}", id), CmsClusterServer.RET_MODE_MAP);
      CmsScheduledJobInfo job = CmsClusterRemoteStringHelper.createScheduledJob((String)data.get("id"));
      data.remove("id");
      CmsClusterRemoteStringHelper.mapToObject(data, job);
      return job;
   }

   public static List getScheduledJobs(CmsObject cms, CmsClusterServer server) {
      List list = (List)server.executeCmd(cms, getCmd("getScheduledJobIds", (Map)null), CmsClusterServer.RET_MODE_LIST);
      List jobs = new ArrayList();
      Iterator it = list.iterator();

      while(it.hasNext()) {
         String id = (String)it.next();
         jobs.add(getScheduledJob(cms, server, id));
      }

      return jobs;
   }

   public static CmsSearchIndex getSearchIndex(CmsObject cms, CmsClusterServer server, String name) {
      Map data = (Map)server.executeCmd(cms, getCmd("readSearchIndex", "${name}", name), CmsClusterServer.RET_MODE_MAP);
      int fieldSize = Integer.valueOf((String)data.remove("fieldConfiguration.fields.size"));
      int sourceNamesSize = Integer.valueOf((String)data.remove("sourceNames.size"));
      CmsSearchIndex index = CmsClusterRemoteStringHelper.createSearchIndex((String)data.get("name"), fieldSize, sourceNamesSize);
      index.setLocaleString((String)data.get("locale"));
      data.remove("name");
      data.remove("locale");
      CmsClusterRemoteStringHelper.mapToObject(data, index);
      int i = 0;

      for(int n = index.getSourceNames().size(); i < n; ++i) {
         String sourceName = (String)index.getSourceNames().get(i);
         CmsSearchIndexSource indexSource = getIndexSource(cms, server, sourceName);
         if (indexSource != null) {
            index.getSources().add(indexSource);
         }
      }

      return index;
   }

   public static List getSearchIndexes(CmsObject cms, CmsClusterServer server) {
      List list = (List)server.executeCmd(cms, getCmd("getSearchIndexNames", (Map)null), CmsClusterServer.RET_MODE_LIST);
      List indexList = new ArrayList();
      Iterator it = list.iterator();

      while(it.hasNext()) {
         String name = (String)it.next();
         indexList.add(getSearchIndex(cms, server, name));
      }

      return indexList;
   }

   public static long getSystemMillis(CmsObject cms, CmsClusterServer server) {
      String time = (String)server.executeCmd(cms, getCmd("System.currentTimeMillis", (Map)null), CmsClusterServer.RET_MODE_STRING);

      try {
         return Long.valueOf(time);
      } catch (NumberFormatException var4) {
         if (LOG.isErrorEnabled()) {
            LOG.error(var4.getLocalizedMessage(), var4);
         }

         return 0L;
      }
   }

   public static void overwriteScheduleJobs(CmsObject cms, CmsClusterServer server, CmsScheduledJobInfo job) {
      StringBuffer cmd = new StringBuffer();
      Map substitutions = new HashMap();
      substitutions.put("${name}", job.getJobName());
      substitutions.put("${propsMap}", generateMapToString(CmsClusterRemoteStringHelper.objToMap(job, CmsClusterRemoteStringHelper.SCHEDULED_JOB_PROP_LIST)));
      cmd.append(getCmd("overwriteScheduledJob", substitutions));
      cmd.append(getCmd("writeSystemConfiguration", (Map)null));
      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static void rebuildSearchIndexes(CmsObject cms, CmsClusterServer server, List indexNames) {
      StringBuffer cmd = new StringBuffer();
      Iterator iter = indexNames.iterator();

      while(iter.hasNext()) {
         String name = (String)iter.next();
         Map substitutions = new HashMap();
         substitutions.put("${name}", name);
         cmd.append(getCmd("rebuildSearchIndexes", substitutions));
      }

      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static void setModuleParameters(CmsObject cms, CmsClusterServer server, String moduleName, SortedMap parameters) {
      CmsModule module = new CmsModule();
      module.setParameters(parameters);
      Map substitutions = new HashMap();
      substitutions.put("${name}", moduleName);
      substitutions.put("${propsMap}", generateMapToString(CmsClusterRemoteStringHelper.objToMap(module, "parameters")));
      server.executeCmd(cms, getCmd("setModuleParameters", substitutions), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static void writeScheduleJobs(CmsObject cms, CmsClusterServer server, List jobs) {
      StringBuffer cmd = new StringBuffer();
      Iterator itJobs = jobs.iterator();

      while(itJobs.hasNext()) {
         CmsScheduledJobInfo job = (CmsScheduledJobInfo)itJobs.next();
         Map substitutions = new HashMap();
         substitutions.put("${propsMap}", generateMapToString(CmsClusterRemoteStringHelper.objToMap(job, CmsClusterRemoteStringHelper.SCHEDULED_JOB_PROP_LIST)));
         String id = "" + job.getId();
         if (job.getId() != null) {
            id = "\"" + id + "\"";
         }

         substitutions.put("${id}", id);
         cmd.append(getCmd("writeScheduledJob", substitutions));
      }

      cmd.append(getCmd("writeSystemConfiguration", (Map)null));
      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   public static void writeSearchIndexes(CmsObject cms, CmsClusterServer server, List indexes) {
      Iterator itIndexes = indexes.iterator();

      while(itIndexes.hasNext()) {
         CmsSearchIndex index = (CmsSearchIndex)itIndexes.next();
         Iterator iterSources = index.getSources().iterator();

         while(iterSources.hasNext()) {
            createIndexSource(cms, server, (CmsSearchIndexSource)iterSources.next());
         }
      }

      StringBuffer cmd = new StringBuffer();
      itIndexes = indexes.iterator();

      while(itIndexes.hasNext()) {
         CmsSearchIndex index = (CmsSearchIndex)itIndexes.next();
         StringBuffer propBuffer = new StringBuffer();
         propBuffer.append("indexInfo.setLocaleString(\"");
         propBuffer.append(index.getLocale());
         propBuffer.append("\");\n");
         if (index.getFieldConfiguration() != null) {
            propBuffer.append("indexInfo.getFieldConfiguration().setName(\"");
            propBuffer.append(index.getFieldConfiguration().getName());
            propBuffer.append("\");\n");
         }

         if (index.getSourceNames() != null) {
            propBuffer.append("indexInfo.getSourceNames().clear();\n");
            Iterator iter = index.getSourceNames().iterator();

            while(iter.hasNext()) {
               propBuffer.append("indexInfo.addSourceName(\"");
               propBuffer.append((String)iter.next());
               propBuffer.append("\");\n");
            }
         }

         Map substitutions = new HashMap();
         substitutions.put("${propsMap}", generateMapToString(CmsClusterRemoteStringHelper.objToMap(index, CmsClusterRemoteStringHelper.WRITE_SEARCH_INDEX_PROP_LIST)));
         substitutions.put("${setProps}", propBuffer.toString());
         substitutions.put("${name}", index.getName());
         int fieldSize = index.getFieldConfiguration() != null ? index.getFieldConfiguration().getFields().size() : 0;
         substitutions.put("${fields.size}", String.valueOf(fieldSize));
         cmd.append(getCmd("writeSearchIndex", substitutions));
      }

      cmd.append(getCmd("writeSearchConfiguration", (Map)null));
      server.executeCmd(cms, cmd.toString(), (CmsClusterServer.CmsRemoteExeReturnType)null);
   }

   protected static void setCmds(Map cmds) {
      m_cmds = cmds;
   }

   private static String generateMapToString(Map propsMap) {
      StringBuffer props = new StringBuffer(512);
      Iterator it = propsMap.entrySet().iterator();

      while(it.hasNext()) {
         Entry entry = (Entry)it.next();
         props.append(getCmdMapPut("propsMap", "\"" + entry.getKey().toString() + "\"", "\"" + entry.getValue().toString() + "\""));
         props.append("\n");
      }

      return props.toString();
   }

   private static String getCmd(String cmdName, Map macros) {
      String cmd = (String)m_cmds.get(cmdName);
      return macros != null && !macros.isEmpty() ? CmsStringUtil.substitute(cmd, macros) : cmd;
   }

   private static String getCmd(String cmdName, String macro, String value) {
      String cmd = (String)m_cmds.get(cmdName);
      return CmsStringUtil.isEmptyOrWhitespaceOnly(macro) ? cmd : CmsStringUtil.substitute(cmd, macro, value);
   }

   private static String getCmdMapPut(String name, String key, String value) {
      Map substitutions = new HashMap();
      substitutions.put("${name}", name);
      substitutions.put("${key}", key);
      substitutions.put("${value}", value);
      return getCmd("Map.put", substitutions);
   }

   private static String setExplorerMenuItem(CmsMenuRuleTranslator menuRuleTranslator, CmsExplorerContextMenuItem item) {
      StringBuffer cmd = new StringBuffer();
      cmd.append("item = new org.opencms.workplace.explorer.CmsExplorerContextMenuItem();\n");
      cmd.append("item.setType(\"").append(item.getType()).append("\");\n");
      if ("entry".equals(item.getType())) {
         cmd.append("item.setKey(\"").append(item.getKey()).append("\");\n");
         if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(item.getRule())) {
            cmd.append("item.setRule(\"").append(item.getRule()).append("\");\n");
         } else {
            String legacyRules = item.getRules();
            if (CmsStringUtil.isNotEmpty(legacyRules) && menuRuleTranslator.hasMenuRule(legacyRules)) {
               cmd.append("item.setRule(\"").append(menuRuleTranslator.getMenuRuleName(legacyRules)).append("\");\n");
            } else {
               cmd.append("item.setRules(\"").append(legacyRules).append("\");\n");
            }
         }

         if (item.getTarget() != null) {
            cmd.append("item.setTarget(\"").append(item.getTarget()).append("\");\n");
         }

         cmd.append("item.setUri(\"").append(item.getUri()).append("\");\n");
         if (item.isParentItem()) {
            Iterator i = item.getSubItems().iterator();

            while(i.hasNext()) {
               CmsExplorerContextMenuItem subItem = (CmsExplorerContextMenuItem)i.next();
               cmd.append(setExplorerMenuItem(menuRuleTranslator, subItem));
            }
         }
      }

      cmd.append("settings.addContextMenuEntry(item);\n");
      return cmd.toString();
   }

   private static String setModuleResourceTypes(CmsModule module) {
      StringBuffer cmd = new StringBuffer();
      cmd.append("ClassLoader classLoader = Thread.currentThread().getContextClassLoader();\n");
      cmd.append("List types = new ArrayList();\n");
      Iterator itResTypes = module.getResourceTypes().iterator();

      Iterator itExpTypes;
      
      Iterator itProps;
      while(itResTypes.hasNext()) {
         I_CmsResourceType type = (I_CmsResourceType)itResTypes.next();
         cmd.append("type = classLoader.loadClass(\"").append(type.getClass().getName()).append("\").newInstance();\n");
         if (type.getConfiguration() != null) {
            itExpTypes = type.getConfiguration().entrySet().iterator();

            while(itExpTypes.hasNext()) {
               Entry entry = (Entry)itExpTypes.next();
               cmd.append("type.addConfigurationParameter(\"").append(entry.getKey()).append("\", \"").append(entry.getValue()).append("\");\n");
            }
         }

         itExpTypes = type.getConfiguredCopyResources().iterator();

         while(itExpTypes.hasNext()) {
            CmsConfigurationCopyResource res = (CmsConfigurationCopyResource)itExpTypes.next();
            cmd.append("type.addCopyResource(\"").append(res.getSource()).append("\", \"").append(res.getTarget()).append("\", \"").append(res.getTypeString()).append("\");\n");
         }

         for(itProps = type.getConfiguredDefaultProperties().iterator(); itProps.hasNext(); cmd.append("));\n")) {
            CmsProperty prop = (CmsProperty)itProps.next();
            cmd.append("type.addDefaultProperty(new org.opencms.file.CmsProperty(\"");
            cmd.append(prop.getName()).append("\", ");
            if (prop.getStructureValue() != null) {
               cmd.append("\"").append(prop.getStructureValue()).append("\"");
            } else {
               cmd.append("null");
            }

            cmd.append(", ");
            if (prop.getResourceValue() != null) {
               cmd.append("\"").append(prop.getResourceValue()).append("\"");
            } else {
               cmd.append("null");
            }
         }

         itProps = type.getConfiguredMappings().iterator();

         while(itProps.hasNext()) {
        	String prop = (String)itProps.next();
            cmd.append("type.addMappingType(\"").append(prop).append("\");\n");
         }

         cmd.append("type.setAdditionalModuleResourceType(true);\n");
         cmd.append("type.initConfiguration(\"").append(type.getTypeName()).append("\", \"").append(type.getTypeId()).append("\", \"").append(type.getClassName()).append("\");\n");
         cmd.append("types.add(type);\n");
      }

      cmd.append("module.setResourceTypes(types);\n");
      CmsExplorerTypeAccess defaultAccess = null;
      if (OpenCms.getWorkplaceManager() != null) {
         defaultAccess = OpenCms.getWorkplaceManager().getDefaultAccess();
      }

      itExpTypes = module.getExplorerTypes().iterator();

      while(itExpTypes.hasNext()) {
         CmsExplorerTypeSettings type = (CmsExplorerTypeSettings)itExpTypes.next();
         cmd.append("settings = new ").append(CmsExplorerTypeSettings.class.getName()).append("();\n");
         cmd.append("settings.setTypeAttributes(\"").append(type.getName()).append("\", \"").append(type.getKey()).append("\", \"").append(type.getIcon()).append("\", \"").append(type.getReference()).append("\");\n");
         if (type.getNewResourceHandlerClassName() != null) {
            cmd.append("settings.setNewResourceHandlerClassName(\"").append(type.getNewResourceHandlerClassName()).append("\");\n");
         }

         cmd.append("settings.setNewResourceUri(\"").append(type.getNewResourceUri()).append("\");\n");
         cmd.append("settings.setNewResourceOrder(\"").append(type.getNewResourceOrder()).append("\");\n");
         if (CmsStringUtil.isNotEmpty(type.getNewResourcePage())) {
            cmd.append("settings.setNewResourcePage(\"").append(type.getNewResourcePage()).append("\");\n");
         }

         cmd.append("settings.setAutoSetNavigation(\"").append(type.isAutoSetNavigation()).append("\");\n");
         cmd.append("settings.setAutoSetTitle(\"").append(type.isAutoSetTitle()).append("\");\n");
         cmd.append("settings.setInfo(\"").append(type.getInfo()).append("\");\n");
         if (type.getDescriptionImage() != null) {
            cmd.append("settings.setDescriptionImage(\"").append(type.getDescriptionImage()).append("\");\n");
         }

         if (type.getTitleKey() != null) {
            cmd.append("settings.setTitleKey(\"").append(type.getTitleKey()).append("\");\n");
         }

         if (type.getAccess() != defaultAccess && !type.getAccess().isEmpty()) {
            cmd.append("access = new ").append(CmsExplorerTypeAccess.class.getName()).append("();\n");
            itProps = type.getAccess().getAccessEntries().entrySet().iterator();

            while(itProps.hasNext()) {
               Entry entry = (Entry)itProps.next();
               cmd.append("access.addAccessEntry(\"").append(entry.getKey()).append("\", \"").append(entry.getValue()).append("\");\n");
            }

            cmd.append("settings.setAccess(access);\n");
         }

         if (type.hasEditOptions()) {
            cmd.append("settings.setPropertyDefaults(\"").append(type.isPropertiesEnabled()).append("\", \"").append(type.isShowNavigation()).append("\");\n");
            itProps = type.getProperties().iterator();

            while(itProps.hasNext()) {
               String prop = (String)itProps.next();
               cmd.append("settings.addProperty(\"").append(prop).append("\");\n");
            }

            CmsMenuRuleTranslator menuRuleTranslator = new CmsMenuRuleTranslator();
            Iterator itMenus = type.getContextMenuEntries().iterator();

            while(itMenus.hasNext()) {
               CmsExplorerContextMenuItem item = (CmsExplorerContextMenuItem)itMenus.next();
               cmd.append(setExplorerMenuItem(menuRuleTranslator, item));
            }
         }

         cmd.append("settings.setAddititionalModuleExplorerType(true);\n");
         cmd.append("module.getExplorerTypes().add(settings);\n");
      }

      return cmd.toString();
   }
}
