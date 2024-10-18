package org.opencms.ocee.cluster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.digester3.Digester;
import org.dom4j.Element;
import org.opencms.configuration.A_CmsXmlConfiguration;
import org.opencms.ocee.base.CmsOceeManager;

public class CmsClusterRemoteCmdConfiguration extends A_CmsXmlConfiguration {
   private static final String C_CONFIGURATION_DTD_LOCATION = "org/opencms/ocee/cluster/";
   private static final String C_CONFIGURATION_DTD_NAME = "remote-commands.dtd";
   private static final String C_CONFIGURATION_DTD_URL_PREFIX = "http://www.alkacon.com/dtd/6.0/";
   private static final String N_REMOTECOMMAND = "remote-command";
   private static final String N_REMOTECOMMANDS = "remote-commands";
   private Map m_commands = new HashMap();

   public CmsClusterRemoteCmdConfiguration() {
      this.setXmlFileName("remote-commands.xml");
   }

   public void addCommand(String name, String cmd) {
      this.m_commands.put(name, cmd);
   }

   public void addXmlDigesterRules(Digester digester) {
      digester.addCallMethod("*/remote-commands/remote-command", "addCommand", 2);
      digester.addCallParam("*/remote-commands/remote-command", 0, "name");
      digester.addCallParam("*/remote-commands/remote-command", 1);
   }

   public Element generateXml(Element parent) {
      Element remoteCmdsElement = parent.addElement("remote-commands");
      Iterator it = this.m_commands.entrySet().iterator();

      while(it.hasNext()) {
         Entry entry = (Entry)it.next();
         Element cmdElement = remoteCmdsElement.addElement("remote-command");
         cmdElement.addAttribute("name", (String)entry.getKey());
         cmdElement.addCDATA((String)entry.getValue());
      }

      return remoteCmdsElement;
   }

   public Map getCmds() {
      return this.m_commands;
   }

   public String getDtdFilename() {
      return "remote-commands.dtd";
   }

   public String getDtdSystemLocation() {
      return "org/opencms/ocee/cluster/";
   }

   public String getDtdUrlPrefix() {
      return "http://www.alkacon.com/dtd/6.0/";
   }

   protected void initMembers() {
      CmsOceeManager.getInstance().checkOceeVersion();
   }
}
