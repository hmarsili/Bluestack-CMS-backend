package org.opencms.ocee.cluster;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.digester3.Digester;
import org.dom4j.Element;
import org.opencms.configuration.A_CmsXmlConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.ocee.base.CmsOceeManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.CmsXmlErrorHandler;
import org.xml.sax.SAXException;

public class CmsClusterConfiguration extends A_CmsXmlConfiguration {
   protected static final String N_CLUSTER = "cluster";
   protected static final String N_CONNECTION_RETRIES = "connection-retries";
   protected static final String N_DELAY = "delay";
   protected static final String N_ENABLED = "enabled";
   protected static final String N_ERRORS_ENABLED = "errors-enabled";
   protected static final String N_EVENTHANDLER = "eventhandler";
   protected static final String N_EVENTSOURCE = "event-source";
   protected static final String N_EVENTSTORE = "eventstore";
   protected static final String N_IP = "ip";
   protected static final String N_MAILSETTINGS = "mailsettings";
   protected static final String N_PASSPHRASE = "passphrase";
   protected static final String N_RECIPIENTS = "recipients";
   protected static final String N_SERVER = "server";
   protected static final String N_SERVERS = "servers";
   protected static final String N_TIMEOUT = "timeout";
   protected static final String N_URL = "url";
   protected static final String N_WARNINGS_ENABLED = "warnings-enabled";
   protected static final String N_WPSERVER = "wp-server";
   private static final String C_CONFIGURATION_DTD_LOCATION = "org/opencms/ocee/cluster/";
   private static final String C_CONFIGURATION_DTD_NAME = "ocee-cluster.dtd";
   private static final String C_CONFIGURATION_DTD_URL_PREFIX = "http://www.alkacon.com/dtd/6.0/";
   private static final String C_CONFIGURATION_XML_FILE_NAME = "ocee-cluster.xml";
   private static final int MAX_CONNECTION_RETRIES = 5;
   private static final String N_MANAGERSGROUP = "managersgroup";
   protected List<CmsClusterServer> m_allServers;
   protected String m_eventHandlerClassName;
   protected String m_passphrase;
   protected int m_timeout;
   protected String m_wpServer;
   private int m_connectionRetries;
   private boolean m_isEventStoreEnabled;
   private List<String> m_mailRecipients;
   private String m_managersGroup;
   private boolean m_sendErrors;
   private boolean m_sendWarnigs;

   public CmsClusterConfiguration() {
      this.setXmlFileName("ocee-cluster.xml");
      this.m_allServers = new ArrayList();
      this.m_connectionRetries = 5;
      this.m_mailRecipients = new ArrayList();
   }

   public void addServer(CmsClusterServer server) {
      this.m_allServers.add(server);
      if (CmsLog.INIT.isInfoEnabled()) {
         CmsLog.INIT.info(Messages.get().getBundle().key("INIT_ADD_SERVER_1", new Object[]{server.toString()}));
      }

   }

   public void addXmlDigesterRules(Digester digester) {
      digester.addCallMethod("*/cluster", "initConfiguration");
      digester.addCallMethod("*/cluster/managersgroup", "setManagersGroup", 0);
      digester.addCallMethod("*/cluster/timeout", "setTimeout", 0);
      digester.addCallMethod("*/cluster/passphrase", "setPassphrase", 0);
      digester.addCallMethod("*/cluster/eventhandler", "setEventHandler", 0);
      digester.addCallMethod("*/cluster/eventstore/enabled", "setEventStoreEnabled", 0);
      digester.addCallMethod("*/cluster/eventstore/connection-retries", "setConnectionRetries", 0);
      digester.addCallMethod("*/cluster/eventstore/mailsettings/warnings-enabled", "setWarningsEnabled", 0);
      digester.addCallMethod("*/cluster/eventstore/mailsettings/errors-enabled", "setErrorsEnabled", 0);
      digester.addCallMethod("*/cluster/eventstore/mailsettings/recipients", "setMailRecipients", 0);
      digester.addCallMethod("*/cluster/wp-server", "setWpServer", 0);
      String xPathServer = "*/cluster/servers/server";
      digester.addObjectCreate(xPathServer, CmsClusterServer.class);
      digester.addCallMethod(xPathServer + "/" + "name", "setName", 0);
      digester.addCallMethod(xPathServer + "/" + "url", "setUrl", 0);
      digester.addCallMethod(xPathServer + "/" + "ip", "setIp", 0);
      digester.addCallMethod(xPathServer + "/" + "event-source", "setEventSource", 0);
      digester.addCallMethod(xPathServer + "/" + "delay", "setDelay", 0);
      digester.addSetNext(xPathServer, "addServer");
   }

   public Element generateXml(Element parent) {
      Element clusterElement = parent.addElement("cluster");
      if (this.m_managersGroup != null) {
         clusterElement.addElement("managersgroup").addText(this.m_managersGroup.toString());
      }

      clusterElement.addElement("timeout").addText(Integer.toString(this.m_timeout));
      clusterElement.addElement("passphrase").addText(this.m_passphrase);
      clusterElement.addElement("eventhandler").addText(this.m_eventHandlerClassName);
      Element eventStoreElement = clusterElement.addElement("eventstore");
      eventStoreElement.addElement("enabled").addText(String.valueOf(this.m_isEventStoreEnabled));
      eventStoreElement.addElement("connection-retries").addText("" + this.m_connectionRetries);
      Element mailSettings = eventStoreElement.addElement("mailsettings");
      mailSettings.addElement("warnings-enabled").addText(String.valueOf(this.m_sendWarnigs));
      mailSettings.addElement("errors-enabled").addText(String.valueOf(this.m_sendErrors));
      String recipients = "";
      if (!this.m_mailRecipients.isEmpty()) {
         String recipient;
         for(Iterator i$ = this.m_mailRecipients.iterator(); i$.hasNext(); recipients = recipients + recipient + ",") {
            recipient = (String)i$.next();
         }

         recipients = recipients.substring(0, recipients.length() - 1);
      }

      mailSettings.addElement("recipients").addText(recipients);
      Element serversElement = clusterElement.addElement("servers");
      int i = 0;

      for(int n = this.m_allServers.size(); i < n; ++i) {
         CmsClusterServer server = (CmsClusterServer)this.m_allServers.get(i);
         Element serverElement = serversElement.addElement("server");
         serverElement.addElement("name").addText(server.getName());
         serverElement.addElement("url").addText(server.getUrl());
         serverElement.addElement("ip").addText(server.getIp());
         if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(server.getEventSource())) {
            serverElement.addElement("event-source").addText(server.getEventSource());
         }

         serverElement.addElement("delay").addText(String.valueOf(server.getDelay()));
      }

      clusterElement.addElement("wp-server").addText(this.m_wpServer);
      return clusterElement;
   }

   public List<CmsClusterServer> getAllServers() {
      return this.m_allServers;
   }

   public int getConnectionRetries() {
      return this.m_connectionRetries;
   }

   public String getDtdFilename() {
      return "ocee-cluster.dtd";
   }

   public String getDtdSystemLocation() {
      return "org/opencms/ocee/cluster/";
   }

   public String getDtdUrlPrefix() {
      return "http://www.alkacon.com/dtd/6.0/";
   }

   public String getEventHandler() {
      return this.m_eventHandlerClassName;
   }

   public List<String> getMailRecipients() {
      return this.m_mailRecipients;
   }

   public String getManagersGroup() {
      return this.m_managersGroup;
   }

   public String getPassphrase() {
      return this.m_passphrase;
   }

   public int getTimeout() {
      return this.m_timeout;
   }

   public String getWpServer() {
      return this.m_wpServer;
   }

   public String getXmlFileName() {
      return "ocee-cluster.xml";
   }

   public void initConfiguration() {
      String thisFilename = this.getClass().getName().replace('.', '/');
      URL thisUrl = this.getClass().getClassLoader().getResource(thisFilename + ".class");

      try {
         URL baseUrl = new URL(thisUrl, ".");
         CmsClusterRemoteCmdConfiguration cmdConf = new CmsClusterRemoteCmdConfiguration();
         this.loadXmlConfiguration(baseUrl, cmdConf);
         CmsClusterRemoteCmdHelper.setCmds(cmdConf.getCmds());
      } catch (Exception var5) {
      }

      CmsClusterManager manager = CmsClusterManager.getInstance();
      if (manager != null) {
         manager.initConfiguration(this);
      }

      if (CmsLog.INIT.isInfoEnabled()) {
         CmsLog.INIT.info(Messages.get().getBundle().key("INIT_IMPORTED_CLUSTER_CONFIG_0"));
      }

   }

   public boolean isEventStoreEnabled() {
      return this.m_isEventStoreEnabled;
   }

   public boolean isSendErrors() {
      return this.m_sendErrors;
   }

   public boolean isSendWarnigs() {
      return this.m_sendWarnigs;
   }

   public void setConnectionRetries(String retries) {
      try {
         this.m_connectionRetries = Integer.parseInt(retries);
      } catch (Exception var3) {
         this.m_connectionRetries = 3;
      }

   }

   public void setErrorsEnabled(String enabled) {
      this.m_sendErrors = Boolean.parseBoolean(enabled);
   }

   public void setEventHandler(String eventHandlerClassName) {
      this.m_eventHandlerClassName = eventHandlerClassName;
   }

   public void setEventStoreEnabled(String enabled) {
      this.m_isEventStoreEnabled = Boolean.parseBoolean(enabled);
   }

   public void setMailRecipients(String recipients) {
      this.m_mailRecipients.clear();
      if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(recipients)) {
         String[] recs = recipients.split(",");

         for(int i = 0; i < recs.length; ++i) {
            this.m_mailRecipients.add(recs[i].trim());
         }
      }

   }

   public void setManagersGroup(String managersGroup) {
      this.m_managersGroup = managersGroup;
   }

   public void setPassphrase(String passphrase) {
      this.m_passphrase = passphrase;
   }

   public void setTimeout(String timeout) {
      byte defaultTimeout = 15;

      try {
         this.m_timeout = Integer.parseInt(timeout);
      } catch (NumberFormatException var4) {
         if (CmsLog.INIT.isErrorEnabled()) {
            CmsLog.INIT.error(Messages.get().getBundle().key("INIT_ERR_SETTIMEOUT_2", new Object[]{String.valueOf(timeout), String.valueOf(defaultTimeout)}), var4);
         }

         this.m_timeout = defaultTimeout;
      }

   }

   public void setWarningsEnabled(String enabled) {
      this.m_sendWarnigs = Boolean.parseBoolean(enabled);
   }

   public void setWpServer(String wpServer) {
      this.m_wpServer = wpServer;
   }

   protected void initMembers() {
      CmsOceeManager.getInstance().checkOceeVersion();
   }

   private void cacheDtdSystemId(I_CmsXmlConfiguration configuration) {
      if (configuration.getDtdSystemLocation() != null) {
         try {
            String file = CmsFileUtil.readFile(configuration.getDtdSystemLocation() + configuration.getDtdFilename(), "UTF-8");
            CmsXmlEntityResolver.cacheSystemId(configuration.getDtdUrlPrefix() + configuration.getDtdFilename(), file.getBytes("UTF-8"));
         } catch (IOException var3) {
         }
      }

   }

   private void loadXmlConfiguration(URL url, I_CmsXmlConfiguration configuration) throws IOException, SAXException {
      this.cacheDtdSystemId(configuration);
      URL fileUrl = new URL(url, configuration.getXmlFileName());
      Digester digester = new Digester();
      digester.setValidating(true);
      digester.setEntityResolver(new CmsXmlEntityResolver((CmsObject)null));
      digester.setRuleNamespaceURI((String)null);
      digester.setErrorHandler(new CmsXmlErrorHandler());
      digester.push(configuration);
      configuration.addXmlDigesterRules(digester);
      digester.parse(fileUrl.openStream());
   }
}
