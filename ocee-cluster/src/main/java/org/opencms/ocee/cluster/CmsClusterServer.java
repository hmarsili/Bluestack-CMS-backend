package org.opencms.ocee.cluster;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.util.NodeComparator;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ocee.license.CmsLicenseConfiguration;
import org.opencms.util.A_CmsModeIntEnumeration;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlException;
import org.opencms.xml.CmsXmlUtils;

public class CmsClusterServer implements Serializable {
   public static final CmsClusterServer.CmsRemoteExeReturnType RET_MODE_LIST;
   public static final CmsClusterServer.CmsRemoteExeReturnType RET_MODE_MAP;
   public static final CmsClusterServer.CmsRemoteExeReturnType RET_MODE_STRING;
   private static final long INVALID_CLUSTER_LICENSE_TIMEOUT = 300000L;
   private static final Log LOG;
   private static final long serialVersionUID = 4444866727546233964L;
   private boolean m_accessible;
   private Map m_configFiles = new HashMap();
   private long m_delay;
   private String m_eventSource;
   private CmsUUID m_id = new CmsUUID();
   private CmsMessageContainer m_initMessage;
   private String m_ip;
   private Exception m_lastCommunicationException;
   private CmsClusterLicenseData m_license;
   private String m_macAddress;
   private boolean m_markedAsDead;
   private CmsMessageContainer m_message;
   private String m_name;
   private String m_realName;
   private Map m_responseData;
   private String m_url;
   private boolean m_wpServer;

   public void clearConfigFiles() {
      this.m_configFiles.clear();
   }

   public Object executeCmd(CmsObject cms, String javaCode, CmsClusterServer.CmsRemoteExeReturnType retType) {
      try {
         return this.executeCmd(javaCode, retType);
      } catch (CmsClusterRemoteExeException var5) {
         LOG.error("Error while executing remote command", var5);
         return null;
      }
   }

   public String getConfigFile(String fileName) {
      return (String)this.m_configFiles.get(fileName);
   }

   public long getDelay() {
      return this.m_delay;
   }

   public String getEventSource() {
      return this.m_eventSource;
   }

   public CmsUUID getId() {
      return this.m_id;
   }

   public String getIp() {
      return this.m_ip;
   }

   public Exception getLastCommunicationException() {
      return this.m_lastCommunicationException;
   }

   public CmsClusterLicenseData getLicenseData() {
      if (this.m_license == null || this.m_license.hasTimestampOlderThan(300000L)) {
         try {
            Map<String, Object> responseData = new HashMap();
            long licenseLeftTime;
            if (OpenCms.getSystemInfo().getServerName().equals(this.getName())) {
               CmsLicenseConfiguration license = (CmsLicenseConfiguration)CmsCoreProvider.getInstance().getConfigurationManager().getConfiguration(CmsLicenseConfiguration.class);
               if (license != null && license.getLicenseDate() != 0L) {
                  String licenseType = "No";
                  licenseLeftTime = 0L;
                  if (license.isEvaluation() != null) {
                     licenseLeftTime = license.getExpirationDate() - System.currentTimeMillis();
                     licenseType = "Evaluation";
                  } else if ("Development".toUpperCase().equals(license.getActivationKey())) {
                     licenseLeftTime = (long)(license.getDevelopmentTime() * 60 * 60) * 1000L - OpenCms.getSystemInfo().getRuntime();
                     licenseType = "Development";
                  } else {
                     licenseType = "Production";
                  }

                  if (!license.isEnabled()) {
                     licenseLeftTime = -1L;
                  }

                  CmsClusterLicenseData licenseData = new CmsClusterLicenseData(licenseType);
                  licenseData.setName(license.getLicenseName() != null ? license.getLicenseName() : "");
                  licenseData.setDistribution(license.getDistribution() != null ? license.getDistribution() : "");
                  licenseData.setActivationKey(license.getActivationKey() != null ? license.getActivationKey() : "");
                  licenseData.setTimeLeft(licenseLeftTime);
                  this.m_license = licenseData;
                  return this.m_license;
               }

               this.m_license = new CmsClusterLicenseData("No");
               this.setAccessible(false);
               return this.m_license;
            }

            CmsClusterManager.getInstance().getEventHandler().forwardEvent(this, CmsClusterEventTypes.GET_LICENSE_INFO.getType(), responseData, (Map)null);
            String licenseType = (String)responseData.get("licenseType");
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(licenseType)) {
               CmsClusterLicenseData data = new CmsClusterLicenseData(licenseType);
               data.setName((String)responseData.get("licenseName"));
               data.setDistribution((String)responseData.get("licenseDist"));
               data.setActivationKey((String)responseData.get("licenseActKey"));
               licenseLeftTime = Long.parseLong((String)responseData.get("licenseLeftTime"));
               data.setTimeLeft(licenseLeftTime);
               this.m_license = data;
            } else {
               this.m_license = new CmsClusterLicenseData("No accessible");
               this.m_license.setTimestamp(System.currentTimeMillis());
               this.setAccessible(false);
            }
         } catch (Exception var7) {
            if (LOG.isErrorEnabled()) {
               LOG.error(Messages.get().getBundle().key("ERR_FORWARD_GET_LICENSE_INFO_1", new Object[]{this.getName()}), var7);
            }

            this.m_license = new CmsClusterLicenseData("No accessible");
            this.setAccessible(false);
         }
      }

      return this.m_license;
   }

   public String getMacAddress() {
      if (this.m_macAddress == null) {
         String info = "org.opencms.ocee.cluster.CmsClusterManager.getInstance().getMacAddress()";

         try {
            if (OpenCms.getSystemInfo().getServerName().equals(this.getName())) {
               this.m_macAddress = CmsClusterManager.getInstance().getMacAddress();
            } else {
               this.m_macAddress = (String)this.executeCmd(info, RET_MODE_STRING);
            }
         } catch (Exception var3) {
         }

         if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.m_macAddress)) {
            this.setAccessible(false);
         }
      }

      return this.m_macAddress;
   }

   public String getMessage(Locale locale) {
      return this.m_message == null ? "" : this.m_message.key(locale);
   }

   public String getName() {
      return this.m_name;
   }

   public String getRealName() {
      if (this.m_realName == null) {
         String info = "org.opencms.main.OpenCms.getSystemInfo().getServerName()";

         try {
            this.m_realName = (String)this.executeCmd(info, RET_MODE_STRING);
         } catch (CmsClusterRemoteExeException var3) {
         }

         if (CmsStringUtil.isEmptyOrWhitespaceOnly(this.m_realName)) {
            this.setAccessible(false);
         }
      }

      if (this.m_realName == null) {
         this.m_realName = this.m_name;
      }

      return this.m_realName;
   }

   public String getStatus(Locale locale) {
      String status = null;
      String msg = this.getMessage(locale);
      if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(msg)) {
         status = msg;
      } else if (!this.isAccessible()) {
         List<String> messages = new ArrayList();
         messages.add(Messages.get().getBundle(locale).key("GUI_CLUSTER_SERVER_STATUS_NO_ACCESS_0"));
         if (this.m_initMessage != null) {
            messages.add(this.m_initMessage.key(locale));
         }

         status = this.listAsString(messages, " - ");
      } else if (!this.isLicenseCompatible()) {
         status = Messages.get().getBundle(locale).key("GUI_CLUSTER_SERVER_STATUS_COMPATIBLE_LICENSE_0");
      }

      return status;
   }

   public String getUrl() {
      return this.m_url;
   }

   public boolean isAccessible() {
      return this.m_accessible;
   }

   public boolean isEventSource() {
      return Boolean.valueOf(this.getEventSource());
   }

   public boolean isIdenticalConfigFile(String filename) {
      String content = (String)this.m_configFiles.get(filename);
      return Boolean.parseBoolean(content);
   }

   public boolean isLicenseCompatible() {
      CmsClusterLicenseData licenseData = this.getLicenseData();
      CmsClusterLicenseData wpLicenseData = CmsClusterManager.getInstance().getWpServer().getLicenseData();
      boolean result = licenseData.validateAgainst(wpLicenseData);
      if (!result) {
         LOG.warn("Incompatible license for server " + this.m_url + " (" + this.m_name + "), accessible=" + licenseData.isAccessible());
         LOG.warn("license for WP: " + wpLicenseData.validationDebugString());
         LOG.warn("license for this server (" + this.m_name + "): " + licenseData.validationDebugString());
      }

      return result;
   }

   public boolean isMarkedAsDead() {
      return this.m_markedAsDead;
   }

   public boolean isMissingConfigFile(String filename) {
      Object content = this.m_configFiles.get(filename);
      return Boolean.FALSE.toString().equals(content);
   }

   public boolean isWpServer() {
      return this.m_wpServer;
   }

   public void reset() {
      this.m_configFiles.clear();
      this.m_license = null;
      this.m_macAddress = null;
      this.m_realName = null;
      this.m_markedAsDead = false;
      this.m_lastCommunicationException = null;
      this.m_message = null;
      this.m_accessible = false;
   }

   public void retrieveAllConfigFiles() {
      if (this.m_configFiles.isEmpty()) {
         Iterator itFileNames = CmsClusterManager.getLocalConfigFileNames().iterator();

         String fileName;
         Document localDoc;
         while(itFileNames.hasNext()) {
            fileName = (String)itFileNames.next();

            try {
               Map additionalParameters = new HashMap();
               Map responseData = new HashMap();
               additionalParameters.put("filename", fileName);
               CmsClusterManager.getInstance().getEventHandler().forwardEvent(this, CmsClusterEventTypes.GET_CONFIG_FILE.getType(), responseData, additionalParameters);
               if (responseData.containsKey("configFileContent")) {
                  String content = CmsEncoder.decode((String)responseData.get("configFileContent"), "UTF-8");
                  if (fileName.endsWith(".xml")) {
                     localDoc = CmsXmlUtils.unmarshalHelper(content, new CmsXmlEntityResolver((CmsObject)null));
                     content = CmsXmlUtils.marshal(localDoc, OpenCms.getSystemInfo().getDefaultEncoding());
                  }

                  this.m_configFiles.put(fileName, content);
               }
            } catch (Exception var8) {
               if (LOG.isErrorEnabled()) {
                  LOG.error(Messages.get().getBundle().key("ERR_FORWARD_GET_CONFIG_FILE_2", new Object[]{fileName, this.getName()}), var8);
               }
            }
         }

         if (CmsClusterManager.getInstance().getThisServer().getName().equals(this.getName())) {
            return;
         }

         itFileNames = CmsClusterManager.getLocalConfigFileNames().iterator();

         while(itFileNames.hasNext()) {
            fileName = (String)itFileNames.next();
            String content = (String)this.m_configFiles.get(fileName);
            if (content == null) {
               this.m_configFiles.put(fileName, Boolean.FALSE.toString());
            } else {
               String localContent = CmsClusterManager.getInstance().getThisServer().getConfigFile(fileName);
               if (content.equals(localContent)) {
                  this.m_configFiles.put(fileName, Boolean.TRUE.toString());
               } else if (fileName.endsWith(".xml")) {
                  try {
                     Document remoteDoc = CmsXmlUtils.unmarshalHelper(content, new CmsXmlEntityResolver((CmsObject)null));
                     localDoc = CmsXmlUtils.unmarshalHelper(localContent, new CmsXmlEntityResolver((CmsObject)null));
                     if ((new NodeComparator()).compare(localDoc, remoteDoc) == 0) {
                        this.m_configFiles.put(fileName, Boolean.TRUE.toString());
                     }
                  } catch (CmsXmlException var7) {
                  }
               }
            }
         }
      }

   }

   public void setAccessible(boolean accessible) {
      this.m_accessible = accessible;
   }

   public void setDelay(long delay) {
      this.m_delay = delay;
   }

   public void setDelay(String delay) {
      this.m_delay = 0L;

      try {
         this.m_delay = Long.valueOf(delay);
      } catch (Exception var3) {
         if (LOG.isErrorEnabled()) {
            LOG.error("Unable to parse value for server delay: " + delay, var3);
         }
      }

   }

   public void setEventSource(String eventSource) {
      this.m_eventSource = eventSource;
   }

   public void setInitMessage(CmsMessageContainer message) {
      this.m_initMessage = message;
   }

   public void setIp(String ip) {
      this.m_ip = ip;
   }

   public void setLastCommunicationException(Exception exception) {
      this.m_lastCommunicationException = exception;
   }

   public void setMarkedAsDead(boolean markedAsDead) {
      this.m_markedAsDead = markedAsDead;
   }

   public void setMessage(CmsMessageContainer message) {
      this.m_message = message;
   }

   public void setName(String name) {
      this.m_name = name;
   }

   public void setUrl(String url) {
      this.m_url = url;
   }

   public void setWpServer(boolean active) {
      this.m_wpServer = active;
   }

   public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append("[");
      buf.append("id=").append(this.m_id);
      buf.append(", ");
      buf.append("name=").append(this.m_name);
      buf.append(", ");
      buf.append("url=").append(this.m_url);
      buf.append(", ");
      buf.append("ip=").append(this.m_ip);
      buf.append("]");
      return buf.toString();
   }

   private Object executeCmd(String javaCode, CmsClusterServer.CmsRemoteExeReturnType retType) throws CmsClusterRemoteExeException {
      Map responseData = new HashMap();
      HashMap additionalParameters = new HashMap();

      try {
         additionalParameters.put("info", new String(Base64.encodeBase64(javaCode.getBytes("UTF-8")), "UTF-8"));
      } catch (UnsupportedEncodingException var7) {
         if (LOG.isErrorEnabled()) {
            LOG.error(var7.getLocalizedMessage(), var7);
         }

         throw new CmsClusterRemoteExeException(var7.getLocalizedMessage());
      }

      CmsClusterManager.getInstance().getEventHandler().forwardEvent(this, CmsClusterEventTypes.GET_INSTANCE_INFO.getType(), responseData, additionalParameters);
      String ret = (String)responseData.get("info");
      if (ret != null) {
         try {
            ret = new String(Base64.decodeBase64(ret.getBytes("UTF-8")), "UTF-8");
         } catch (UnsupportedEncodingException var8) {
            if (LOG.isErrorEnabled()) {
               LOG.error(var8.getLocalizedMessage(), var8);
            }

            throw new CmsClusterRemoteExeException(var8.getLocalizedMessage());
         }

         if (ret.startsWith("__err__")) {
            if (LOG.isErrorEnabled()) {
               LOG.error(Messages.get().getBundle().key("ERR_FORWARD_GET_INSTANCE_INFO_2", new Object[]{this.getName(), javaCode}));
               LOG.error(ret.substring("__err__".length()));
            }

            throw new CmsClusterRemoteExeException(ret.substring("__err__".length()));
         }
      } else if (retType == RET_MODE_LIST || retType == RET_MODE_MAP) {
         ret = "";
      }

      if (retType == RET_MODE_LIST) {
         return CmsStringUtil.splitAsList(ret, "|-\n-|", true);
      } else {
         return retType == RET_MODE_MAP ? CmsStringUtil.splitAsMap(ret, "|-\n-|", "|-:-|") : ret;
      }
   }

   private String listAsString(List list, String separator) {
      StringBuffer string = new StringBuffer(128);
      Iterator it = list.iterator();

      while(it.hasNext()) {
         string.append(it.next());
         if (it.hasNext()) {
            string.append(separator);
         }
      }

      return string.toString();
   }

   static {
      RET_MODE_LIST = CmsClusterServer.CmsRemoteExeReturnType.MODE_LIST;
      RET_MODE_MAP = CmsClusterServer.CmsRemoteExeReturnType.MODE_MAP;
      RET_MODE_STRING = CmsClusterServer.CmsRemoteExeReturnType.MODE_STRING;
      LOG = CmsLog.getLog(CmsClusterServer.class);
   }

   public static final class CmsRemoteExeReturnType extends A_CmsModeIntEnumeration {
      protected static final CmsClusterServer.CmsRemoteExeReturnType MODE_LIST = new CmsClusterServer.CmsRemoteExeReturnType(2);
      protected static final CmsClusterServer.CmsRemoteExeReturnType MODE_MAP = new CmsClusterServer.CmsRemoteExeReturnType(3);
      protected static final CmsClusterServer.CmsRemoteExeReturnType MODE_STRING = new CmsClusterServer.CmsRemoteExeReturnType(1);
      private static final long serialVersionUID = -1838389068962420583L;

      private CmsRemoteExeReturnType(int mode) {
         super(mode);
      }
   }
}
