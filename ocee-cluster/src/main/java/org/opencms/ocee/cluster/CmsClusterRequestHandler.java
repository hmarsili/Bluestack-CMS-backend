package org.opencms.ocee.cluster;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.opencms.db.CmsDbContext;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsRequestContext;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsCoreProvider;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsRequestHandler;
import org.opencms.main.OpenCms;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;

public class CmsClusterRequestHandler implements I_CmsRequestHandler {
   public static final String C_REQUEST_PARAM_ACTION = "action";
   public static final String C_REQUEST_PARAM_EVENT = "event";
   public static final String C_REQUEST_PARAM_EVENT_TIME = "eventtime";
   public static final String C_REQUEST_PARAM_FILENAME = "filename";
   public static final String C_REQUEST_PARAM_INSTANCE_INFO = "info";
   public static final String C_REQUEST_PARAM_IP = "IP";
   public static final String C_REQUEST_PARAM_PASSPHRASE = "passphrase";
   public static final String C_REQUEST_PARAM_UID = "UUID";
   public static final String C_REQUEST_PARAM_DELAY_EVENT = "delay";
   public static final String C_REQUEST_PARAM_WPSERVER_NAME = "wpServerName";
   public static final String C_RESPONSE_PARAM_CLUSTER_SOURCE = "isClusterEventSource";
   public static final String C_RESPONSE_PARAM_CONFIG_FILE = "configFileContent";
   public static final String C_RESPONSE_PARAM_INSTANCE_INFO = "info";
   public static final String C_RESPONSE_PARAM_LICENSE_ACTKEY = "licenseActKey";
   public static final String C_RESPONSE_PARAM_LICENSE_DIST = "licenseDist";
   public static final String C_RESPONSE_PARAM_LICENSE_LEFTTIME = "licenseLeftTime";
   public static final String C_RESPONSE_PARAM_LICENSE_NAME = "licenseName";
   public static final String C_RESPONSE_PARAM_LICENSE_TYPE = "licenseType";
   public static final String C_RESPONSE_PARAM_MISSING_PARAMETERS = "missingParameters";
   public static final String C_RESPONSE_PARAM_VALID_PASSPHRASE = "validPassphrase";
   private static final String[] C_HANDLER_NAMES = new String[]{"Cluster"};
   protected static final Log LOG = CmsLog.getLog(CmsClusterRequestHandler.class);

   public String[] getHandlerNames() {
      return C_HANDLER_NAMES;
   }

   public void handle(HttpServletRequest req, HttpServletResponse res, String name) {
      CmsClusterManager clusterManager = CmsClusterManager.getInstance();
      if (clusterManager != null) {
         PrintWriter out = null;

         int eventType = -1;
         try {
            out = res.getWriter();
            if (!clusterManager.isInitialized()) {
               clusterManager.reInitializeCluster();
            }

            List missingRequestParameters = new ArrayList();
            String paramPassphrase = req.getParameter("passphrase");
            if (paramPassphrase != null) {
               if (paramPassphrase.equals(clusterManager.getConfiguration().getPassphrase())) {
                  out.println("validPassphrase:true");
               } else {
                  out.println("validPassphrase:false");
               }
            } else {
               missingRequestParameters.add("passphrase");
            }

            //int eventType = -1;
            String paramEventType = req.getParameter("event");
            if (paramEventType != null) {
               try {
                  eventType = Integer.parseInt(paramEventType);
               } catch (NumberFormatException var38) {
                  eventType = -1;
                  missingRequestParameters.add("event");
               }
            } else {
               missingRequestParameters.add("event");
            }

            long eventTime = 0L;
            String eventTimeString = req.getParameter("eventtime");
            if (eventTimeString != null) {
               try {
                  eventTime = Long.parseLong(eventTimeString);
               } catch (NumberFormatException var37) {
                  eventTime = 0L;
                  missingRequestParameters.add("eventtime");
               }
            } else {
               missingRequestParameters.add("eventtime");
            }

            String eventName = "";

            try {
               eventName = CmsClusterEventTypes.valueOf(eventType).toString();
            } catch (Exception var36) {
               eventName = String.valueOf(eventType);
            }

            boolean fireEventInNewThread = false;
            if (eventType != -1) {
               Map eventData = new HashMap();
               String groupName;
               if (LOG.isDebugEnabled()) {
                  StringBuffer strBuf = new StringBuffer();
                  Iterator i = req.getParameterMap().keySet().iterator();

                  while(i.hasNext()) {
                     String key = (String)i.next();
                     groupName = req.getParameter(key);
                     strBuf.append(key).append('=').append(groupName);
                     if (i.hasNext()) {
                        strBuf.append(", ");
                     }
                  }

                  try {
                     LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_RECEIVED_EVENT_2", new Object[]{CmsClusterEventTypes.valueOf(eventType).toString(), strBuf.toString()}));
                  } catch (CmsIllegalArgumentException var35) {
                     LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_RECEIVED_EVENT_2", new Object[]{(new Integer(eventType)).toString(), strBuf.toString()}));
                  }
               }

               if (LOG.isInfoEnabled()) {
                  LOG.info(Messages.get().getBundle().key("LOG_INFO_RECEIVING_EVENT_2", String.valueOf(eventTime), eventName));
               }

               CmsClusterServer thisServer = clusterManager.getThisServer();
               final long waitTime = Boolean.parseBoolean(req.getParameter("delay")) && thisServer != null ? thisServer.getDelay() : 0L;
               String groupId;
               if (eventType == CmsClusterEventTypes.CHECK_SOURCE.getType()) {
                  groupName = req.getParameter("UUID");
                  if (groupName != null) {
                     eventData.put("UUID", groupName);
                  } else {
                     missingRequestParameters.add("UUID");
                  }

                  CmsUUID myUUID = clusterManager.getId();
                  CmsUUID otherUUID = new CmsUUID(groupName);
                  if (clusterManager.isForwardingEvent() && otherUUID.equals(myUUID)) {
                     eventData.put("isClusterEventSource", "true");
                  } else {
                     eventData.put("isClusterEventSource", "false");
                  }
               } else if (eventType == CmsClusterEventTypes.PUBLISH_PROJECT.getType()) {
                  fireEventInNewThread = true;
                  eventType = 2;
                  eventData.put("projectId", CmsProject.ONLINE_PROJECT_ID);
                  groupName = req.getParameter("publishHistoryId");
                  if (groupName != null) {
                     eventData.put("publishHistoryId", groupName);
                  } else {
                     missingRequestParameters.add("publishHistoryId");
                  }
               } else if (eventType == CmsClusterEventTypes.UPDATE_EXPORTS.getType()) {
                  fireEventInNewThread = true;
                  groupName = req.getParameter("IP");
                  if (groupName != null) {
                     this.handleClusterUpdateExports(groupName);
                  } else {
                     missingRequestParameters.add("IP");
                  }
               } else if (CmsOceeManager.getInstance().checkCoreVersion("7.0.2") && eventType == CmsClusterEventTypes.REBUILD_SEARCHINDEX.getType()) {
                  fireEventInNewThread = true;
                  groupName = req.getParameter("indexNames");
                  if (groupName != null) {
                     eventData.put("indexNames", groupName);
                  }

                  this.handleClusterRebuildSearchIndex(eventData, waitTime);
                  if (LOG.isInfoEnabled()) {
                     LOG.info(Messages.get().getBundle().key("LOG_INFO_EVENT_DELEGATED_2", String.valueOf(eventTime), eventName));
                  }
               } else if (eventType == CmsClusterEventTypes.FULLSTATIC_EXPORT.getType()) {
                  fireEventInNewThread = true;
                  Boolean paramPurgeFirst = new Boolean(req.getParameter("purge"));
                  if (req.getParameter("purge") != null) {
                     this.handleClusterFullStaticExport(paramPurgeFirst, waitTime);
                  } else {
                     missingRequestParameters.add("purge");
                  }
               } else if (eventType == CmsClusterEventTypes.SET_WP_SERVER.getType()) {
                  groupName = req.getParameter("wpServerName");
                  clusterManager.setWpServer(groupName);
                  OpenCms.writeConfiguration(CmsClusterConfiguration.class);
               } else if (eventType == CmsClusterEventTypes.GET_CONFIG_FILE.getType()) {
                  groupName = req.getParameter("filename");
                  if (groupName != null) {
                     eventData.put("filename", groupName);
                  } else {
                     missingRequestParameters.add("filename");
                  }
               } else if (eventType == CmsClusterEventTypes.GET_INSTANCE_INFO.getType()) {
                  groupName = req.getParameter("info");
                  if (groupName != null) {
                     eventData.put("info", groupName);
                  } else {
                     missingRequestParameters.add("info");
                  }
               } else if (eventType == CmsClusterEventTypes.CLEAR_CACHES.getType()) {
                  fireEventInNewThread = true;
                  eventType = 5;
               } else if (eventType == CmsClusterEventTypes.FLEX_CLEAR_CACHES.getType()) {
                  eventType = 9;
                  groupName = req.getParameter("action");
                  if (groupName != null) {
                     eventData.put("action", Integer.valueOf(groupName));
                  }
               } else if (eventType == CmsClusterEventTypes.FLEX_PURGE_JSP_REPOSITORY.getType()) {
                  fireEventInNewThread = true;
                  eventType = 8;
               } else if (eventType == CmsClusterEventTypes.CLEAR_PRINCIPAL_CACHES.getType()) {
                  eventType = 6;
               } else if (eventType == CmsClusterEventTypes.UNCACHE_USER.getType()) {
                  fireEventInNewThread = true;
                  String userName = req.getParameter("userName");
                  if (userName != null) {
                     eventData.put("userName", userName);
                  }

                  groupId = req.getParameter("userId");
                  if (groupId != null) {
                     eventData.put("userId", groupId);
                  }

                  groupName = req.getParameter("groupName");
                  if (groupName != null) {
                     eventData.put("groupName", groupName);
                  }

                  groupId = req.getParameter("groupId");
                  if (groupId != null) {
                     eventData.put("groupId", groupId);
                  }

                  String userAction = req.getParameter("userAction");
                  if (userAction != null) {
                     eventData.put("userAction", userAction);
                  }

                  eventType = 29;
                  OpenCms.getMemoryMonitor().clearPrincipalsCache();
               } else if (eventType == CmsClusterEventTypes.UNCACHE_OU.getType()) {
                  fireEventInNewThread = true;
                  groupName = req.getParameter("ouName");
                  if (groupName != null) {
                     eventData.put("ouName", groupName);
                  }

                  groupId = req.getParameter("ouId");
                  if (groupId != null) {
                     eventData.put("ouId", groupId);
                  }

                  String userAction = req.getParameter("userAction");
                  if (userAction != null) {
                     eventData.put("userAction", userAction);
                  }

                  eventType = 30;
                  OpenCms.getMemoryMonitor().clearPrincipalsCache();
               } else if (eventType == CmsClusterEventTypes.UNCACHE_GROUP.getType()) {
                  fireEventInNewThread = true;
                  groupName = req.getParameter("groupName");
                  if (groupName != null) {
                     eventData.put("groupName", groupName);
                  }

                  groupId = req.getParameter("groupId");
                  if (groupId != null) {
                     eventData.put("groupId", groupId);
                  }

                  String userAction = req.getParameter("userAction");
                  if (userAction != null) {
                     eventData.put("userAction", userAction);
                  }

                  eventType = 31;
                  OpenCms.getMemoryMonitor().clearPrincipalsCache();
               }

               if (LOG.isDebugEnabled()) {
                  StringBuffer strBuf = new StringBuffer();
                  Iterator k = eventData.keySet().iterator();

                  while(k.hasNext()) {
                     String userAction = (String)k.next();
                     groupId = eventData.get(userAction).toString();
                     strBuf.append(userAction).append('=').append(groupId);
                     if (k.hasNext()) {
                        strBuf.append(", ");
                     }
                  }

                  try {
                     LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_FIRED_EVENT_2", new Object[]{CmsClusterEventTypes.valueOf(eventType).toString(), strBuf.toString()}));
                  } catch (CmsIllegalArgumentException var34) {
                     LOG.debug(Messages.get().getBundle().key("LOG_DEBUG_FIRED_EVENT_2", new Object[]{(new Integer(eventType)).toString(), strBuf.toString()}));
                  }
               }

               if (!eventData.containsKey("eventAlreadyReplicated")) {
                  eventData.put("eventAlreadyReplicated", Boolean.TRUE);
                  final int _eventType = eventType;
                  if (fireEventInNewThread) {
                     final Map data = new HashMap(eventData);
                     Thread thread = new Thread(new Runnable() {
                        public void run() {
                           if (waitTime > 0L) {
                              try {
                                 Thread.sleep(waitTime * 1000L);
                              } catch (InterruptedException var10) {
                                 if (CmsClusterRequestHandler.LOG.isErrorEnabled()) {
                                    CmsClusterRequestHandler.LOG.error(var10);
                                 }
                              }
                           }

                           CmsDbContext dbc = null;

                           try {
                              dbc = CmsCoreProvider.getInstance().getNewDbContext((CmsRequestContext)null);
                              data.put("dbContext", dbc);
                              OpenCms.fireCmsEvent(_eventType, data);
                           } catch (Exception var8) {
                              if (CmsClusterRequestHandler.LOG.isErrorEnabled()) {
                                 CmsClusterRequestHandler.LOG.error(Messages.get().getBundle().key("ERR_HANDLING_REQUEST_0"), var8);
                              }
                           } finally {
                              if (dbc != null) {
                                 dbc.clear();
                              }

                           }

                        }
                     });
                     thread.start();
                     if (LOG.isInfoEnabled()) {
                        LOG.info(Messages.get().getBundle().key("LOG_INFO_EVENT_DELEGATED_2", String.valueOf(eventTime), eventName));
                     }
                  } else {
                     CmsDbContext dbc = null;

                     try {
                        dbc = CmsCoreProvider.getInstance().getNewDbContext((CmsRequestContext)null);
                        eventData.put("dbContext", dbc);
                        OpenCms.fireCmsEvent(eventType, eventData);
                        if (LOG.isInfoEnabled()) {
                           LOG.info(Messages.get().getBundle().key("LOG_INFO_EVENT_PROCESSED_2", String.valueOf(eventTime), eventName));
                        }
                     } catch (Exception var39) {
                        if (LOG.isErrorEnabled()) {
                           LOG.error(eventTime + ": " + Messages.get().getBundle().key("ERR_HANDLING_REQUEST_0"), var39);
                        }
                     } finally {
                        if (dbc != null) {
                           dbc.clear();
                        }

                     }
                  }

                  Iterator j = eventData.keySet().iterator();

                  while(j.hasNext()) {
                     groupId = (String)j.next();
                     String userAction = eventData.get(groupId).toString();
                     out.println(groupId + ":" + userAction);
                  }
               }
            }

            out.print("missingParameters");
            out.print(":");
            if (missingRequestParameters.size() > 0) {
               Iterator l = missingRequestParameters.iterator();

               while(l.hasNext()) {
                  out.print((String)l.next());
                  if (l.hasNext()) {
                     out.print(",");
                  }
               }

               out.println();
            } else {
               out.println("none");
            }
         } catch (Exception var41) {
            if (LOG.isErrorEnabled()) {
               LOG.error(Messages.get().getBundle().key("ERR_HANDLING_REQUEST_0"), var41);
            }

            if (out != null) {
               out.println(Messages.get().getBundle().key("GUI_EXCEPTION_1", new Object[]{var41.getMessage()}));
            }
         }

         if (LOG.isErrorEnabled() && out != null && out.checkError()) {
            LOG.error("An error has occured while writing the cluster response.");
         }

      }
   }

   protected void handleClusterFullStaticExport(Boolean purgeFirst, final long waitTime) {
      CmsClusterManager clusterManager = CmsClusterManager.getInstance();
      CmsClusterServer server = clusterManager.getThisServer();
      final boolean purge = purgeFirst;
      if (server != null) {
         Thread thread = new Thread(new Runnable() {
            public void run() {
               try {
                  if (waitTime > 0L) {
                     Thread.sleep(waitTime * 1000L);
                  }

                  OpenCms.getStaticExportManager().exportFullStaticRender(purge, new CmsLogReport(CmsLocaleManager.getDefaultLocale(), this.getClass()));
               } catch (Throwable var2) {
                  if (CmsClusterRequestHandler.LOG.isErrorEnabled()) {
                     CmsClusterRequestHandler.LOG.error(Messages.get().getBundle().key("ERR_HANDLING_WRITING_EXPORTS_1", new Object[]{var2.toString()}), var2);
                  }
               }

            }
         });
         thread.start();
      }

   }

   protected void handleClusterRebuildSearchIndex(Map eventData, final long waitTime) {
      eventData.put("eventAlreadyReplicated", Boolean.TRUE);
      final Map data = new HashMap(eventData);
      Thread thread = new Thread(new Runnable() {
         public void run() {
            CmsDbContext dbc = null;

            try {
               if (waitTime > 0L) {
                  Thread.sleep(waitTime * 1000L);
               }

               I_CmsReport report = new CmsLogReport(Locale.ENGLISH, this.getClass());
               if (!CmsOceeManager.getInstance().checkCoreVersion("7.0.2")) {
                  OpenCms.getSearchManager().rebuildAllIndexes(report);
               } else {
                  dbc = CmsCoreProvider.getInstance().getNewDbContext((CmsRequestContext)null);
                  data.put("dbContext", dbc);
                  data.put("report", report);
                  OpenCms.fireCmsEvent(32, data);
               }
            } catch (Exception var7) {
               if (CmsClusterRequestHandler.LOG.isErrorEnabled()) {
                  CmsClusterRequestHandler.LOG.error(Messages.get().getBundle().key("ERR_HANDLING_REBUILD_SEARCHINDEX_1", new Object[]{var7.toString()}), var7);
               }
            } finally {
               if (dbc != null) {
                  dbc.clear();
               }

            }

         }
      });
      thread.start();
   }

   protected void handleClusterUpdateExports(String serverIp) {
      CmsClusterManager clusterManager = CmsClusterManager.getInstance();
      CmsClusterServer server = clusterManager.getThisServer();
      if (server != null) {
         if (serverIp.equalsIgnoreCase(server.getIp())) {
            Thread thread = new Thread(new Runnable() {
               public void run() {
                  CmsDbContext dbc = null;

                  try {
                     dbc = CmsCoreProvider.getInstance().getNewDbContext((CmsRequestContext)null);
                     OpenCms.fireCmsEvent(19, Collections.singletonMap("dbContext", dbc));
                     OpenCms.fireCmsEvent(8, Collections.EMPTY_MAP);
                     OpenCms.fireCmsEvent(5, Collections.EMPTY_MAP);
                  } catch (Exception var7) {
                     if (CmsClusterRequestHandler.LOG.isErrorEnabled()) {
                        CmsClusterRequestHandler.LOG.error(Messages.get().getBundle().key("ERR_HANDLING_WRITING_EXPORTS_1", new Object[]{var7.toString()}));
                     }
                  } finally {
                     if (dbc != null) {
                        dbc.clear();
                     }

                  }

               }
            });
            thread.start();
         } else if (LOG.isErrorEnabled()) {
            LOG.error(Messages.get().getBundle().key("ERR_WRITING_EXPORTS_0"));
         }
      }

   }
}
