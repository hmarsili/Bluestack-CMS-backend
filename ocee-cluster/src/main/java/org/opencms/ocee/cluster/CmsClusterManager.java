package org.opencms.ocee.cluster;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.mail.internet.InternetAddress;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.mail.CmsSimpleMail;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSystemInfo;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.util.CmsUUID;

public final class CmsClusterManager {
   public static final String CONFIG_LIB_FOLDER = "LIB_FOLDER";
   public static final String CONNECTION_RETRY_JOB_NAME = "Cluster connection retry job";
   public static final String EXTENSION_PROPERTIES = ".properties";
   public static final String EXTENSION_XML = ".xml";
   public static final String KEY_EVENT_TYPE = "eventType";
   private static final Log LOG = CmsLog.getLog(CmsClusterManager.class);
   private Map<String, CmsClusterServer> m_allServers = new HashMap();
   private I_CmsClusterEventHandler m_clusterEventHandler;
   private CmsClusterConfiguration m_configuration;
   private CmsUUID m_id;
   private Map<CmsClusterServer, Integer> m_inAccessibleServers = new HashMap();
   private boolean m_isInitialized;
   private boolean m_isVisiting;
   private Date m_lastEventDate;
   private int m_lastEventType;
   private List<CmsClusterServer> m_otherServers;
   private Map<CmsClusterServer, Queue<Map<String, Object>>> m_serverEventQueues = new HashMap();
   private CmsClusterServer m_thisServer;
   private CmsClusterServer m_wpServer;

   public static CmsClusterManager getInstance() {
      try {
         return (CmsClusterManager)CmsOceeManager.getInstance().getClassLoader().loadObject(CmsClusterManager.class.getName());
      } catch (Exception var1) {
         return null;
      }
   }

   public static List<String> getLocalConfigFileNames() {
      List result = new ArrayList();
      String configPath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(CmsSystemInfo.FOLDER_CONFIG);
      if (configPath != null) {
         File configFolder = new File(configPath);
         File[] configFiles = configFolder.listFiles();

         for(int i = 0; i < configFiles.length; ++i) {
            File configFile = configFiles[i];
            if (configFile.isFile() && (configFile.getName().endsWith(".xml") || configFile.getName().endsWith(".properties"))) {
               result.add(configFile.getName());
            }
         }
      }

      result.add("LIB_FOLDER");
      return result;
   }

   public void addInAccessibleServer(CmsClusterServer server) {
      if (!server.isMarkedAsDead() && !this.m_inAccessibleServers.containsKey(server)) {
         this.m_inAccessibleServers.put(server, 0);
      }

   }

   public CmsClusterConfiguration getConfiguration() {
      return this.m_configuration;
   }

   public I_CmsClusterEventHandler getEventHandler() {
      return this.m_clusterEventHandler;
   }

   public CmsUUID getId() {
      return this.m_id;
   }

   public Date getLastEventDate() {
      return this.m_lastEventDate;
   }

   public int getLastEventType() {
      return this.m_lastEventType;
   }

   public String getMacAddress() throws Exception {
      Field ethAddress = CmsUUID.class.getDeclaredField("m_ethernetAddress");
      ethAddress.setAccessible(true);
      return ethAddress.get((Object)null).toString();
   }

   public List<CmsClusterServer> getOtherServers() {
      return Collections.unmodifiableList(this.m_otherServers);
   }

   public CmsClusterServer getServer(String serverName) {
      return serverName == null ? null : (CmsClusterServer)this.m_allServers.get(serverName);
   }

   public CmsClusterServer getThisServer() {
      return this.m_thisServer;
   }

   public long getTimeoutMillis() {
      return (long)(this.m_configuration.getTimeout() * 1000);
   }

   public CmsClusterServer getWpServer() {
      return this.m_wpServer;
   }

   public void initConfiguration(CmsClusterConfiguration configuration) {
      I_CmsClusterEventHandler clusterEventHandler = null;

      try {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         clusterEventHandler = (I_CmsClusterEventHandler)cl.loadClass(configuration.getEventHandler()).newInstance();
      } catch (Throwable var5) {
         if (CmsLog.INIT.isErrorEnabled()) {
            CmsLog.INIT.error(Messages.get().getBundle().key("INIT_ERR_CREATING_EVENT_HANDLER_1", new Object[]{configuration.getEventHandler()}), var5);
         }
      }

      this.m_id = new CmsUUID();
      this.m_otherServers = new ArrayList();
      this.m_thisServer = null;
      this.m_isInitialized = false;
      this.m_isVisiting = false;
      this.m_clusterEventHandler = clusterEventHandler;

      CmsClusterServer server;
      for(Iterator it = configuration.getAllServers().iterator(); it.hasNext(); this.m_allServers.put(server.getName(), server)) {
         server = (CmsClusterServer)it.next();
         if (server.getName().equals(configuration.getWpServer())) {
            this.m_wpServer = server;
            server.setWpServer(true);
         } else {
            server.setWpServer(false);
         }
      }

      this.m_configuration = configuration;
   }

   public void initConnectionRetryJob(CmsObject cms) {
      if (this.getConfiguration() != null && this.getConfiguration().isEventStoreEnabled()) {
         boolean jobAlreadyExists = false;
         Iterator i$ = OpenCms.getScheduleManager().getJobs().iterator();

         while(i$.hasNext()) {
            Object inf = i$.next();
            CmsScheduledJobInfo info = (CmsScheduledJobInfo)inf;
            if ("Cluster connection retry job".equals(info.getJobName())) {
               jobAlreadyExists = true;
               break;
            }
         }

         if (!jobAlreadyExists) {
            CmsScheduledJobInfo job = new CmsScheduledJobInfo((String)null, "Cluster connection retry job", CmsClusterRetryConnectionJob.class.getName(), new CmsContextInfo(), "0 0/5 * * * ?", false, true, new TreeMap());

            try {
               OpenCms.getScheduleManager().scheduleJob(cms, job);
            } catch (CmsException var6) {
               LOG.error(var6.getLocalizedMessage(), var6);
            }
         }
      }

   }

   public boolean isConfigured() {
      return this.m_configuration != null && CmsCoreProvider.getInstance().getRequestHandler(CmsClusterRequestHandler.class.getName()) != null;
   }

   public boolean isEventStoreEnabled() {
      return this.getConfiguration().isEventStoreEnabled();
   }

   public boolean isForwardingEvent() {
      return this.m_clusterEventHandler.isFowardingEvent();
   }

   public boolean isInitialized() {
      return this.m_isInitialized;
   }

   public boolean isOtherServerAccessible() {
      Iterator itServers = this.m_otherServers.iterator();

      CmsClusterServer server;
      do {
         if (!itServers.hasNext()) {
            return false;
         }

         server = (CmsClusterServer)itServers.next();
      } while(!server.isAccessible());

      return true;
   }

   public void reInitializeCluster() {
      this.m_isInitialized = false;
      this.initializeCluster();
      this.retryForwardingEvents();
   }

   public void retryForwardingEvents() {
      if (this.getConfiguration() != null && this.getConfiguration().isEventStoreEnabled() && this.getThisServer() != null && (this.getThisServer().isEventSource() || this.getThisServer().isWpServer())) {
         synchronized(this.m_inAccessibleServers) {
            if (this.m_inAccessibleServers.isEmpty()) {
               LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_NO_SERVERS_MARKED_AS_INACCESSIBLE_0"));
            } else {
               Iterator it = this.m_inAccessibleServers.entrySet().iterator();

               while(true) {
                  while(it.hasNext()) {
                     Entry<CmsClusterServer, Integer> entry = (Entry)it.next();
                     CmsClusterServer server = (CmsClusterServer)entry.getKey();
                     server.reset();
                     Map eventData;
                     Integer eventType;
                     String eventName;
                     if (server.getLicenseData().isAccessible()) {
                        server.setAccessible(true);
                        Queue<Map<String, Object>> eventQueue = (Queue)this.m_serverEventQueues.get(server);
                        if (eventQueue == null) {
                           LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_INACCESSIBLE_SERVER_HAS_NO_EVENTS_QUEUED_1", server.getName()));
                        } else {
                           LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_RESENDING_EVENTS_FOR_SERVER_1", server.getName()));
                           String eventNames = "";

                           while(eventQueue.size() > 0 && server.isAccessible()) {
                              eventData = (Map)eventQueue.poll();
                              eventType = (Integer)eventData.get("eventType");
                              eventName = "";

                              try {
                                 eventName = CmsClusterEventTypes.valueOf(eventType).toString();
                              } catch (Exception var13) {
                                 eventName = String.valueOf(eventType);
                              }

                              eventNames = eventNames + eventName + ", ";
                              this.getEventHandler().forwardEvent(server, eventType, eventData, eventData);
                           }

                           if (server.isAccessible()) {
                              this.m_serverEventQueues.remove(server);
                              this.sendMail(server, eventNames, false);
                           }
                        }

                        it.remove();
                     } else {
                        LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_SERVER_STILL_NOT_AVAILABLE_1", server.getName()));
                        if (this.getConfiguration().getConnectionRetries() > (Integer)entry.getValue()) {
                           entry.setValue((Integer)entry.getValue() + 1);
                        } else {
                           String eventNames = "";
                           Queue<Map<String, Object>> eventQueue = (Queue)this.m_serverEventQueues.get(server);
                           if (eventQueue != null) {
                              for(; eventQueue.size() > 0; eventNames = eventNames + eventName + ", ") {
                                 eventData = (Map)eventQueue.poll();
                                 eventType = (Integer)eventData.get("eventType");
                                 eventName = "";

                                 try {
                                    eventName = CmsClusterEventTypes.valueOf(eventType).toString();
                                 } catch (Exception var12) {
                                    eventName = String.valueOf(eventType);
                                 }
                              }
                           }

                           this.removeQueuedEventData(server);
                           server.setMarkedAsDead(true);
                           this.sendMail(server, eventNames, true);
                           it.remove();
                        }
                     }
                  }

                  return;
               }
            }
         }
      }

   }

   public void setLastEventDate(Date date) {
      this.m_lastEventDate = date;
   }

   public void setLastEventType(int eventType) {
      this.m_lastEventType = eventType;
   }

   public void setWpServer(String wpServer) {
      Iterator it = this.m_allServers.values().iterator();

      while(it.hasNext()) {
         CmsClusterServer server = (CmsClusterServer)it.next();
         server.setWpServer(server.getName().equals(wpServer));
      }

      this.m_wpServer = this.getServer(wpServer);
      this.m_configuration.setWpServer(this.m_wpServer.getName());
   }

   public void storeEventData(CmsClusterServer server, int eventType, Map<String, Object> eventData) {
      if (this.getConfiguration().isEventStoreEnabled() && !server.isMarkedAsDead()) {
         synchronized(this.m_inAccessibleServers) {
            Map<String, Object> dataStore = new HashMap();
            dataStore.putAll(eventData);
            dataStore.put("eventType", eventType);
            dataStore.remove("report");
            Queue<Map<String, Object>> eventQueue = (Queue)this.m_serverEventQueues.get(server);
            if (eventQueue == null) {
               eventQueue = new LinkedList();
               this.m_serverEventQueues.put(server, eventQueue);
            }

            ((Queue)eventQueue).offer(dataStore);
            if (!this.m_inAccessibleServers.containsKey(server)) {
               this.m_inAccessibleServers.put(server, 0);
            }
         }
      }

   }

   protected void initializeCluster() {
      if (!this.m_isVisiting && !this.m_isInitialized && this.m_otherServers != null) {
         if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key("LOG_INIT_CLUSTER_START_0"));
         }

         this.m_isInitialized = true;
         List<CmsClusterServer> otherServers = new ArrayList();
         this.m_thisServer = null;
         this.m_isVisiting = true;
         Set<String> macAddresses = new HashSet();
         Map<String, Boolean> isDevByMacAddress = new HashMap();
         Set<String> duplicateMacAddresses = new HashSet();
         Set<String> serverNames = new HashSet();
         Map<String, CmsClusterServer> allServers = new HashMap();
         Map<String, CmsClusterServer> tempAllServers = new HashMap(this.m_allServers);
         String localServerName = OpenCms.getSystemInfo().getServerName();
         if (tempAllServers.containsKey(localServerName)) {
            this.m_thisServer = (CmsClusterServer)this.m_allServers.get(localServerName);
            tempAllServers.remove(localServerName);
            this.m_thisServer.reset();
            this.m_thisServer.setAccessible(true);
            if (this.m_thisServer.isWpServer() && !this.m_thisServer.isEventSource()) {
               this.m_thisServer.setEventSource(Boolean.TRUE.toString());
            }

            macAddresses.add(this.m_thisServer.getMacAddress());
            CmsClusterLicenseData thisLicenseData = this.m_thisServer.getLicenseData();
            isDevByMacAddress.put(this.m_thisServer.getMacAddress(), this.isDevelopmentActivationCode(thisLicenseData.getActivationKey()));
            serverNames.add(this.m_thisServer.getName());
            allServers.put(this.m_thisServer.getName(), this.m_thisServer);
         } else if (LOG.isErrorEnabled()) {
            LOG.error(Messages.get().getBundle().key("ERR_THIS_SERVER_NOT_CONFIGURED_1", localServerName));
         }

         Iterator it;
         label155:
         for(int phase = 0; !tempAllServers.isEmpty(); ++phase) {
            it = tempAllServers.entrySet().iterator();

            while(true) {
               CmsClusterServer server;
               do {
                  do {
                     if (!it.hasNext()) {
                        continue label155;
                     }

                     Entry<String, CmsClusterServer> entry = (Entry)it.next();
                     server = (CmsClusterServer)entry.getValue();
                     server.reset();
                  } while(phase == 0 && !server.isWpServer());
               } while(phase == 1 && !server.getName().equals(server.getRealName()));

               Map<String, Object> responseData = new HashMap();
               Map<String, Object> additionalParameters = new HashMap();
               additionalParameters.put("UUID", this.getId());
               this.m_clusterEventHandler.forwardEvent(server, CmsClusterEventTypes.CHECK_SOURCE.getType(), responseData, additionalParameters);
               String isClusterEventSource = (String)responseData.get("isClusterEventSource");
               boolean startingUp = false;
               if (isClusterEventSource == null) {
                  isClusterEventSource = String.valueOf(OpenCms.getSystemInfo().getServerName().equals(server.getName()));
                  startingUp = true;
               }

               if (!startingUp && !server.getName().equals(server.getRealName())) {
                  if (LOG.isWarnEnabled()) {
                     LOG.warn(Messages.get().getBundle().key("LOG_WARN_SERVER_NAME_2", server.getName(), server.getRealName()));
                  }

                  server.setName(server.getRealName());
               }

               server.setInitMessage((CmsMessageContainer)null);
               CmsClusterLicenseData licenseData = server.getLicenseData();
               String activationCode = "";
               boolean isDev = false;
               if (licenseData != null) {
                  activationCode = licenseData.getActivationKey();
                  isDev = this.isDevelopmentActivationCode(activationCode);
               }

               String macAddress = server.getMacAddress();
               if (isDevByMacAddress.containsKey(macAddress)) {
                  if (!(Boolean)isDevByMacAddress.get(macAddress) || !isDev) {
                     duplicateMacAddresses.add(macAddress);
                  }
               } else {
                  isDevByMacAddress.put(macAddress, isDev);
               }

               if (!startingUp && serverNames.contains(server.getName())) {
                  server.setInitMessage(Messages.get().container("ERR_SERVER_EQUAL_NAME_1", server.getName()));
                  isClusterEventSource = null;
               } else if (!startingUp && duplicateMacAddresses.contains(server.getMacAddress())) {
                  server.setInitMessage(Messages.get().container("ERR_SERVER_MAC_ADDRESS_2", server.getName(), server.getMacAddress()));
                  isClusterEventSource = null;
               } else {
                  server.setAccessible(!startingUp);
                  if (LOG.isInfoEnabled()) {
                     LOG.info(Messages.get().getBundle().key("LOG_INIT_SERVER_ACCESSIBLE_1", new Object[]{server.getName()}));
                  }

                  if (!Boolean.valueOf(isClusterEventSource)) {
                     if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key("LOG_SETTING_OTHER_SERVER_1", server.getName()));
                     }

                     otherServers.add(server);
                  } else {
                     if (LOG.isDebugEnabled()) {
                        LOG.debug(Messages.get().getBundle().key("LOG_SETTING_THIS_SERVER_1", server.getName()));
                     }

                     this.m_thisServer = server;
                  }
               }

               if (!startingUp) {
                  macAddresses.add(server.getMacAddress());
                  serverNames.add(server.getName());
               }

               allServers.put(server.getName(), server);
               it.remove();
               if (isClusterEventSource == null) {
                  if (LOG.isInfoEnabled()) {
                     LOG.info(Messages.get().getBundle().key("LOG_INIT_SERVER_NOT_ACCESSIBLE_1", new Object[]{server.getName()}));
                  }

                  server.setAccessible(false);
                  otherServers.add(server);
               }
            }
         }

         if (this.m_thisServer == null && LOG.isWarnEnabled()) {
            LOG.warn(Messages.get().getBundle().key("LOG_THIS_SERVER_NULL_0"));
         }

         this.m_allServers = allServers;
         it = otherServers.iterator();

         while(it.hasNext()) {
            CmsClusterServer server = (CmsClusterServer)it.next();
            server.clearConfigFiles();
         }

         this.m_otherServers = otherServers;
         this.m_isVisiting = false;
         if (LOG.isInfoEnabled()) {
            LOG.info(Messages.get().getBundle().key("LOG_INIT_CLUSTER_END_0"));
         }

      }
   }

   private String getStackTrace(Throwable aThrowable) {
      Writer result = new StringWriter();
      PrintWriter printWriter = new PrintWriter(result);
      aThrowable.printStackTrace(printWriter);
      return result.toString();
   }

   private boolean isDevelopmentActivationCode(String code) {
      return code.startsWith("Development".toUpperCase());
   }

   private void removeQueuedEventData(CmsClusterServer server) {
      this.m_serverEventQueues.remove(server);
   }

   private void sendMail(CmsClusterServer server, String eventNames, boolean isError) {
      if ((isError && this.getConfiguration().isSendErrors() || !isError && this.getConfiguration().isSendWarnigs()) && !this.getConfiguration().getMailRecipients().isEmpty()) {
         String trace = "";
         if (server.getLastCommunicationException() != null) {
            trace = this.getStackTrace(server.getLastCommunicationException());
            server.setLastCommunicationException((Exception)null);
         }

         String subjectKey;
         String bodyKey;
         if (isError) {
            subjectKey = "GUI_OCEE_CLUSTER_ERROR_MAIL_SUBJECT_1";
            bodyKey = "GUI_OCEE_CLUSTER_ERROR_MAIL_BODY_3";
         } else {
            subjectKey = "GUI_OCEE_CLUSTER_WARNING_MAIL_SUBJECT_1";
            bodyKey = "GUI_OCEE_CLUSTER_WARNING_MAIL_BODY_3";
         }

         this.sendMail(Messages.get().getBundle().key(subjectKey, OpenCms.getSystemInfo().getServerName()), Messages.get().getBundle().key(bodyKey, server.getName(), eventNames, trace));
      }

   }

   private void sendMail(String subject, String body) {
      if (this.getConfiguration().isSendWarnigs()) {
         List<InternetAddress> addresses = new ArrayList();
         Iterator i$ = this.getConfiguration().getMailRecipients().iterator();

         while(i$.hasNext()) {
            String recipient = (String)i$.next();

            try {
               InternetAddress address = new InternetAddress(recipient);
               addresses.add(address);
            } catch (Exception var8) {
               LOG.error(var8.getLocalizedMessage(), var8);
            }
         }

         if (!addresses.isEmpty()) {
            try {
               CmsSimpleMail theMail = new CmsSimpleMail();
               theMail.setCharset(OpenCms.getSystemInfo().getDefaultEncoding());
               theMail.setFrom(OpenCms.getSystemInfo().getMailSettings().getMailFromDefault(), OpenCms.getSystemInfo().getMailSettings().getMailFromDefault());
               theMail.setTo(addresses);
               theMail.setSubject(subject);
               theMail.setMsg(body);
               theMail.send();
            } catch (Exception var7) {
               LOG.error(var7.getLocalizedMessage(), var7);
            }
         }
      }

   }
}
