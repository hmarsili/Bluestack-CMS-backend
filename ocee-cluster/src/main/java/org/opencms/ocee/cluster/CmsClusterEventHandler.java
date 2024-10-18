package org.opencms.ocee.cluster;

import bsh.EvalError;
import bsh.Interpreter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsBaseModuleAction;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.ocee.license.CmsLicenseConfiguration;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;

public class CmsClusterEventHandler implements I_CmsEventListener, I_CmsClusterEventHandler {
   public static final String C_EVENT_ALREADY_REPLICATED = "eventAlreadyReplicated";
   public static final Log LOG = CmsLog.getLog(CmsClusterEventHandler.class);
   protected boolean m_isFowardingEvent;

   public CmsClusterEventHandler() {
      OpenCms.addCmsEventListener(this);
   }

   public void cmsEvent(CmsEvent event) {
      CmsClusterManager clusterManager = CmsClusterManager.getInstance();
      if (clusterManager != null) {
         if (!clusterManager.isInitialized()) {
            clusterManager.reInitializeCluster();
         }

         Map<String, Object> eventData = new HashMap();
         if (event.getData() != null) {
            eventData.putAll(event.getData());
         }

         CmsEvent clusterEvent = null;
         if (event.getType() == 2) {
            eventData.put("delay", "true");
            clusterEvent = new CmsEvent(CmsClusterEventTypes.PUBLISH_PROJECT.getType(), eventData);
            OpenCms.fireCmsEvent(clusterEvent);
         } else if (event.getType() == CmsClusterEventTypes.GET_CONFIG_FILE.getType()) {
            this.handleClusterGetConfigFile(event);
         } else if (event.getType() == CmsClusterEventTypes.GET_INSTANCE_INFO.getType()) {
            this.handleClusterGetInstanceInfo(event);
         } else if (event.getType() == CmsClusterEventTypes.GET_LICENSE_INFO.getType()) {
            this.handleClusterGetLicenseInfo(event);
         } else if (event.getType() == 5) {
            eventData.put("delay", "true");
            clusterEvent = new CmsEvent(CmsClusterEventTypes.CLEAR_CACHES.getType(), eventData);
            OpenCms.fireCmsEvent(clusterEvent);
         } else if (event.getType() == 9) {
            eventData.put("delay", "true");
            clusterEvent = new CmsEvent(CmsClusterEventTypes.FLEX_CLEAR_CACHES.getType(), eventData);
            OpenCms.fireCmsEvent(clusterEvent);
         } else if (event.getType() == 8) {
            clusterEvent = new CmsEvent(CmsClusterEventTypes.FLEX_PURGE_JSP_REPOSITORY.getType(), eventData);
            OpenCms.fireCmsEvent(clusterEvent);
         } else if (event.getType() == 6) {
            clusterEvent = new CmsEvent(CmsClusterEventTypes.CLEAR_PRINCIPAL_CACHES.getType(), eventData);
            OpenCms.fireCmsEvent(clusterEvent);
         } else if (event.getType() == 4) {
            eventData.put("delay", "true");
            clusterEvent = new CmsEvent(CmsClusterEventTypes.FULLSTATIC_EXPORT.getType(), eventData);
            OpenCms.fireCmsEvent(clusterEvent);
         } else if (event.getType() == 32) {
            eventData.put("delay", "true");
            clusterEvent = new CmsEvent(CmsClusterEventTypes.REBUILD_SEARCHINDEX.getType(), eventData);
            OpenCms.fireCmsEvent(clusterEvent);
         } else if (CmsOceeManager.getInstance().checkCoreVersion("7.0.6") && event.getType() == 29) {
            clusterEvent = new CmsEvent(CmsClusterEventTypes.UNCACHE_USER.getType(), eventData);
            OpenCms.fireCmsEvent(clusterEvent);
         } else if (CmsOceeManager.getInstance().checkCoreVersion("7.5.1") && event.getType() == 30) {
            clusterEvent = new CmsEvent(CmsClusterEventTypes.UNCACHE_OU.getType(), eventData);
            OpenCms.fireCmsEvent(clusterEvent);
         } else if (CmsOceeManager.getInstance().checkCoreVersion("7.5.1") && event.getType() == 31) {
            clusterEvent = new CmsEvent(CmsClusterEventTypes.UNCACHE_GROUP.getType(), eventData);
            OpenCms.fireCmsEvent(clusterEvent);
         } else {
            this.handleClusterEvent(event);
         }

      }
   }

   public void forwardEvent(CmsClusterServer server, int eventType, Map responseData, Map additionalParameters) {
      boolean compatible = eventType == CmsClusterEventTypes.GET_LICENSE_INFO.getType();
      compatible = compatible || eventType == CmsClusterEventTypes.GET_CONFIG_FILE.getType();
      compatible = compatible || eventType == CmsClusterEventTypes.GET_INSTANCE_INFO.getType();
      compatible = compatible || eventType == CmsClusterEventTypes.CHECK_SOURCE.getType();
      compatible = compatible || eventType == CmsClusterEventTypes.SET_WP_SERVER.getType();
      CmsClusterManager clusterManager = CmsClusterManager.getInstance();
      String eventName = "";

      try {
         eventName = CmsClusterEventTypes.valueOf(eventType).toString();
      } catch (Exception var41) {
         eventName = String.valueOf(eventType);
      }

      if (clusterManager != null && clusterManager.isEventStoreEnabled() && clusterManager.getThisServer().isEventSource() && !compatible && !server.isAccessible() && !server.isMarkedAsDead()) {
         LOG.debug("Storing event " + eventName + " for server " + server.getName());
         clusterManager.storeEventData(server, eventType, additionalParameters);
      } else if (clusterManager != null && clusterManager.getThisServer().isEventSource() && (compatible || server.isLicenseCompatible())) {
         long eventTime = System.currentTimeMillis();
         CmsMessageContainer errorMessage = null;
         String url = server.getUrl();
         I_CmsReport report = (I_CmsReport)responseData.get("report");
         PostMethod post = null;

         try {
            if (report == null) {
               report = new CmsLogReport(CmsLocaleManager.getDefaultLocale(), this.getClass());
            }

            ((I_CmsReport)report).println(Messages.get().container("RPT_CLUSTER_FORWARD_BEGIN_1", server.getName()), 2);
            this.m_isFowardingEvent = true;
            Map requestData = new HashMap();
            requestData.put("passphrase", CmsEncoder.encode(clusterManager.getConfiguration().getPassphrase(), "UTF-8"));
            requestData.put("event", "" + eventType);
            if (additionalParameters != null) {
               requestData.putAll(additionalParameters);
            }

            requestData.put("eventtime", String.valueOf(eventTime));
            String requestDataStr = new String();
            NameValuePair[] data = new NameValuePair[requestData.size()];
            Iterator i = requestData.keySet().iterator();
            int j = 0;

            while(i.hasNext()) {
               String key = (String)i.next();
               requestDataStr = requestDataStr + key + "=" + requestData.get(key).toString();
               data[j] = new NameValuePair(key, requestData.get(key).toString());
               ++j;
               if (i.hasNext()) {
                  requestDataStr = requestDataStr + ", ";
               }
            }

            if (LOG.isDebugEnabled()) {
               LOG.debug("<<<<<<<< " + server.getName());
               LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_FORWARD_EVENT_3", new Object[]{new Integer(eventType), url, requestDataStr}));
            }

            if (LOG.isInfoEnabled()) {
               LOG.info(Messages.get().getBundle().key("LOG_INFO_FORWARDING_EVENT_3", String.valueOf(eventTime), eventName, url));
            }

            post = new PostMethod(url);
            post.setRequestBody(data);
            HttpClient httpclient = new HttpClient();
            HttpClientParams clientParams = new HttpClientParams();
            clientParams.setSoTimeout(CmsClusterManager.getInstance().getConfiguration().getTimeout() * 1000);
            httpclient.setParams(clientParams);

            try {
               int result = httpclient.executeMethod(post);
               BufferedReader inputReader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));

               for(String inputLine = inputReader.readLine(); inputLine != null; inputLine = inputReader.readLine()) {
                  if (result == 200) {
                     StringTokenizer stringTokenizer = new StringTokenizer(inputLine, ":");
                     if (stringTokenizer.countTokens() >= 2) {
                        responseData.put(stringTokenizer.nextToken(), stringTokenizer.nextToken());
                     }
                  }
               }

               inputReader.close();
            } catch (Exception var42) {
               if (eventType == CmsClusterEventTypes.CHECK_SOURCE.getType() || eventType == CmsClusterEventTypes.PUBLISH_PROJECT.getType() || eventType == CmsClusterEventTypes.REBUILD_SEARCHINDEX.getType()) {
                  server.setLastCommunicationException(var42);
                  if (clusterManager.isEventStoreEnabled() && !compatible) {
                     server.setAccessible(false);
                     clusterManager.storeEventData(server, eventType, additionalParameters);
                  }
               }

               throw var42;
            } finally {
               post.releaseConnection();
            }

            clusterManager.setLastEventType(eventType);
            clusterManager.setLastEventDate(new Date());
            if (LOG.isDebugEnabled()) {
               LOG.debug(server.getName() + " >>>>>>>");
            }
         } catch (IOException var44) {
            if (eventType == CmsClusterEventTypes.CHECK_SOURCE.getType() || eventType == CmsClusterEventTypes.GET_LICENSE_INFO.getType() || eventType == CmsClusterEventTypes.GET_INSTANCE_INFO.getType() || eventType == CmsClusterEventTypes.PUBLISH_PROJECT.getType() || eventType == CmsClusterEventTypes.REBUILD_SEARCHINDEX.getType()) {
               errorMessage = Messages.get().container("ERR_FORWARD_EVENT_IO_STREAM_3", url, var44.toString(), new Long(eventTime));
            }
         } catch (Exception var45) {
            if (eventType == CmsClusterEventTypes.CHECK_SOURCE.getType() || eventType == CmsClusterEventTypes.GET_LICENSE_INFO.getType() || eventType == CmsClusterEventTypes.GET_INSTANCE_INFO.getType() || eventType == CmsClusterEventTypes.PUBLISH_PROJECT.getType() || eventType == CmsClusterEventTypes.REBUILD_SEARCHINDEX.getType()) {
               errorMessage = Messages.get().container("ERR_FORWARD_EVENT_3", url, var45.toString(), new Long(eventTime));
            }
         } finally {
            this.m_isFowardingEvent = false;
            if (errorMessage != null) {
               LOG.error(eventTime + ":  " + errorMessage.key() + "\n" + Messages.get().container("ERR_TRIED_FORWARDING_EVENT_1", eventName).key());
               if (errorMessage.key().indexOf(Messages.get().getBundle().key("ERR_STREAM_TIMEOUT_0")) < 0) {
                  server.setMessage(errorMessage);
                  ((I_CmsReport)report).println(Messages.get().container("RPT_CLUSTER_FORWARD_FAILED_2", server.getName(), errorMessage.key()));
               }
            } else {
               server.setMessage((CmsMessageContainer)null);
               server.setLastCommunicationException((Exception)null);
            }

            ((I_CmsReport)report).println(Messages.get().container("RPT_CLUSTER_FORWARD_END_1", server.getName()), 2);
         }

      }
   }

   public void handleClusterEvent(CmsEvent event) {
      CmsClusterManager clusterManager = CmsClusterManager.getInstance();
      CmsClusterServer thisServer = clusterManager.getThisServer();
      if (thisServer == null || !thisServer.isWpServer() && !thisServer.isEventSource()) {
         if (thisServer == null && LOG.isWarnEnabled()) {
            LOG.warn(Messages.get().getBundle().key("LOG_THIS_SERVER_NULL_0"));
         }

      } else if (event.getData() != null && event.getData().get("eventAlreadyReplicated") != null) {
         if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key("LOG_EVENT_SKIPPED_1", new Integer(event.getType())));
         }

      } else {
         if (event.getType() == 31 || event.getType() == 29 || event.getType() == 8 || event.getType() == 2131 || event.getType() == 2132 || event.getType() == CmsClusterEventTypes.PUBLISH_PROJECT.getType() || event.getType() == CmsClusterEventTypes.FULLSTATIC_EXPORT.getType() || event.getType() == CmsClusterEventTypes.REBUILD_SEARCHINDEX.getType() || event.getType() == CmsClusterEventTypes.FLEX_CLEAR_CACHES.getType() || event.getType() == CmsClusterEventTypes.FLEX_PURGE_JSP_REPOSITORY.getType() || event.getType() == CmsClusterEventTypes.CLEAR_CACHES.getType() || event.getType() == CmsClusterEventTypes.CLEAR_PRINCIPAL_CACHES.getType() || event.getType() == CmsClusterEventTypes.UNCACHE_USER.getType() || event.getType() == CmsClusterEventTypes.UNCACHE_OU.getType() || event.getType() == CmsClusterEventTypes.UNCACHE_GROUP.getType()) {
            Map eventData = new HashMap();
            if (event.getData() != null) {
               eventData.putAll(event.getData());
            }

            if ((clusterManager.getOtherServers() == null || clusterManager.getOtherServers().isEmpty()) && LOG.isWarnEnabled()) {
               LOG.warn(Messages.get().getBundle().key("LOG_OTHER_SERVERS_EMPTY_0"));
            }

            Iterator otherServers = clusterManager.getOtherServers().iterator();

            while(otherServers.hasNext()) {
               CmsClusterServer server = (CmsClusterServer)otherServers.next();
               this.forwardEvent(server, event.getType(), eventData, eventData);
            }

            OpenCms.fireCmsEvent(CmsClusterEventTypes.HOOK.getType(), new HashMap());
         }

      }
   }

   public boolean isFowardingEvent() {
      return this.m_isFowardingEvent;
   }

   protected void handleClusterGetConfigFile(CmsEvent event) {
      String requiredFile = null;

      try {
         String content = "";
         requiredFile = (String)event.getData().get("filename");
         if (requiredFile != null) {
            String libName;
            if (!requiredFile.equals("LIB_FOLDER")) {
               libName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(CmsSystemInfo.FOLDER_CONFIG + requiredFile);
               content = new String(CmsFileUtil.readFile(new File(libName)), OpenCms.getSystemInfo().getDefaultEncoding());
            } else {
               libName = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("lib") + File.separatorChar;
               File libFolder = new File(libName);
               List files = Arrays.asList(libFolder.list());
               Collections.sort(files);
               StringBuffer list = new StringBuffer(1024);
               Iterator it = files.iterator();

               while(it.hasNext()) {
                  String fileName = (String)it.next();
                  File file = new File(libName + fileName);
                  String date = CmsDateUtil.getDateTime(new Date(file.lastModified()), 2, CmsLocaleManager.getDefaultLocale());
                  String size = file.isDirectory() ? Messages.get().getBundle().key("GUI_CLUSTER_DIR_0") : "" + file.length();
                  list.append(CmsStringUtil.padLeft(date, 30));
                  list.append(CmsStringUtil.padLeft(size, 15));
                  list.append("  ");
                  list.append(fileName);
                  list.append("\n");
               }

               content = list.toString();
            }

            content = CmsEncoder.encode(content, "UTF-8");
         }

         event.getData().put("configFileContent", content);
      } catch (Exception var13) {
         if (LOG.isWarnEnabled()) {
            LOG.warn(Messages.get().getBundle().key("ERR_GET_CONFIG_FILE_1", new Object[]{requiredFile}));
         }

         event.getData().put("configFileContent", "");
      }

   }

   protected void handleClusterGetInstanceInfo(CmsEvent event) {
      String requiredInfo = null;
      String responseInfo = "";

      try {
         requiredInfo = (String)event.getData().get("info");
         if (requiredInfo != null) {
            requiredInfo = new String(Base64.decodeBase64(requiredInfo.getBytes("UTF-8")), "UTF-8");
            Interpreter i = new Interpreter();
            i.set("cms", this.getAdminCms());

            try {
               Object ret = i.eval(requiredInfo);
               Iterator it;
               Object obj;
               if (ret instanceof List) {
                  it = ((List)ret).iterator();

                  while(it.hasNext()) {
                     obj = it.next();
                     responseInfo = responseInfo + obj.toString();
                     if (it.hasNext()) {
                        responseInfo = responseInfo + "|-\n-|";
                     }
                  }
               } else if (ret instanceof Set) {
                  it = ((Set)ret).iterator();

                  while(it.hasNext()) {
                     obj = it.next();
                     responseInfo = responseInfo + obj.toString();
                     if (it.hasNext()) {
                        responseInfo = responseInfo + "|-\n-|";
                     }
                  }
               } else if (ret instanceof Map) {
                  it = ((Map)ret).entrySet().iterator();

                  while(it.hasNext()) {
                     Entry entry = (Entry)it.next();
                     if (entry.getValue() != null) {
                        responseInfo = responseInfo + entry.getKey().toString() + "|-:-|" + entry.getValue().toString();
                        responseInfo = responseInfo + "|-\n-|";
                     }
                  }

                  if (responseInfo.length() > 4) {
                     responseInfo = responseInfo.substring(0, responseInfo.length() - "|-\n-|".length());
                  }
               } else {
                  responseInfo = ret == null ? "null" : ret.toString();
               }
            } catch (EvalError var18) {
               if (LOG.isErrorEnabled()) {
                  LOG.error(Messages.get().getBundle().key("ERR_GET_INSTANCE_INFO_1", new Object[]{requiredInfo}), var18);
               }

               responseInfo = "__err__" + var18.getLocalizedMessage();
            }
         }
      } catch (Exception var19) {
         if (LOG.isErrorEnabled()) {
            LOG.error(Messages.get().getBundle().key("ERR_GET_INSTANCE_INFO_1", new Object[]{requiredInfo}), var19);
         }

         responseInfo = "__err__" + var19.getLocalizedMessage();
      } finally {
         try {
            responseInfo = new String(Base64.encodeBase64(responseInfo.getBytes("UTF-8")), "UTF-8");
         } catch (UnsupportedEncodingException var17) {
            if (LOG.isErrorEnabled()) {
               LOG.error(var17.getLocalizedMessage(), var17);
            }
         }

         event.getData().put("info", responseInfo);
      }

   }

   protected void handleClusterGetLicenseInfo(CmsEvent event) {
      String licenseType = "No";
      CmsLicenseConfiguration license = (CmsLicenseConfiguration)CmsCoreProvider.getInstance().getConfigurationManager().getConfiguration(CmsLicenseConfiguration.class);
      String licenseName = license == null ? null : license.getLicenseName();
      if (licenseName == null) {
         licenseName = "";
      }

      event.getData().put("licenseName", licenseName);
      String licenseActKey = license == null ? null : license.getActivationKey();
      if (licenseActKey == null) {
         licenseActKey = "";
      }

      event.getData().put("licenseActKey", licenseActKey);
      if (license != null && license.getLicenseDate() != 0L) {
         String licenseDist = license.getDistribution();
         if (licenseDist == null) {
            licenseDist = "";
         }

         event.getData().put("licenseDist", licenseDist);
         long licenseLeftTime = 0L;
         if (license.isEvaluation() != null) {
            licenseLeftTime = license.getExpirationDate() - System.currentTimeMillis();
            licenseType = "Evaluation";
         } else if ("Development".toUpperCase().equals(licenseActKey)) {
            licenseLeftTime = (long)(license.getDevelopmentTime() * 60 * 60) * 1000L - OpenCms.getSystemInfo().getRuntime();
            licenseType = "Development";
         } else {
            licenseType = "Production";
         }

         if (!license.isEnabled()) {
            licenseLeftTime = -1L;
         }

         event.getData().put("licenseLeftTime", "" + licenseLeftTime);
      } else {
         event.getData().put("licenseLeftTime", "0");
      }

      event.getData().put("licenseType", licenseType);
   }

   private CmsObject getAdminCms() {
      try {
         return OpenCms.initCmsObject(CmsBaseModuleAction.getCms());
      } catch (CmsException var2) {
         if (LOG.isErrorEnabled()) {
            LOG.error(var2.getLocalizedMessage(), var2);
         }

         return null;
      }
   }
}
