package org.opencms.ocee.vfsdoctor;

import java.util.Iterator;
import org.apache.commons.digester3.Digester;
import org.dom4j.Element;
import org.opencms.configuration.A_CmsXmlConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.ocee.base.CmsOceeManager;

public class CmsVfsDoctorConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {
   protected static final String ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper = "pool";
   protected static final String ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null = "read-only";
   protected static final String ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper = "whitelist";
   protected static final String õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int = "maxtime";
   protected static final String Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = "command";
   protected static final String ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private = "command-interpreter";
   protected static final String øO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000classsuper = "drive";
   protected static final String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = "exe-param";
   protected static final String Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void = "forbidden-drives";
   protected static final String ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface = "initial-dir";
   protected static final String Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper = "path";
   protected static final String ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if = "plugin";
   protected static final String ÓO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000public = "plugins";
   protected static final String õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper = "restrict-paths";
   protected static final String oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do = "rfsbrowser";
   private static final String öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper = "org/opencms/ocee/vfsdoctor/";
   private static final String Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = "ocee-vfsdoctor.dtd";
   private static final String ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper = "http://www.alkacon.com/dtd/6.0/";
   private static final String Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = "ocee-vfsdoctor.xml";
   private static final String Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return = "allowedsentences";
   private static final String OO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000for = "disallowedsentences";
   private static final String ÒO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000while = "sentence";
   private static final String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = "sqlconsole";
   private static final String ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float = "vfsdoctor";

   public CmsVfsDoctorConfiguration() {
      this.setXmlFileName("ocee-vfsdoctor.xml");
   }

   public void addDrive(String drive) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager != null) {
         manager.getRfsBrowser().registerDrive(drive);
      }

   }

   public void addPath(String path) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager != null) {
         manager.getRfsBrowser().registerPath(path);
      }

   }

   public void addXmlDigesterRules(Digester digester) {
      digester.addCallMethod("*/vfsdoctor", "initConfiguration");
      digester.addCallMethod("*/vfsdoctor", "setPool", 1);
      digester.addCallParam("*/vfsdoctor", 0, "pool");
      digester.addCallMethod("*/plugins/plugin", "registerPlugin", 1);
      digester.addCallParam("*/plugins/plugin", 0, "class");
      digester.addCallMethod("*/sqlconsole/allowedsentences/sentence", "allowedSentence", 1);
      digester.addCallParam("*/sqlconsole/allowedsentences/sentence", 0, "name");
      digester.addCallMethod("*/sqlconsole/disallowedsentences/sentence", "disallowedSentence", 1);
      digester.addCallParam("*/sqlconsole/disallowedsentences/sentence", 0, "name");
      digester.addCallMethod("*/rfsbrowser", "setReadOnly", 1);
      digester.addCallParam("*/rfsbrowser", 0, "read-only");
      digester.addCallMethod("*/rfsbrowser/initial-dir", "setInitialDir", 0);
      digester.addCallMethod("*/rfsbrowser/restrict-paths", "setWhitelist", 1);
      digester.addCallParam("*/rfsbrowser/restrict-paths", 0, "whitelist");
      digester.addCallMethod("*/rfsbrowser/restrict-paths/path", "addPath", 0);
      digester.addCallMethod("*/rfsbrowser/command-interpreter", "setCommandInterpreter", 4);
      digester.addCallParam("*/rfsbrowser/command-interpreter/command", 0);
      digester.addCallParam("*/rfsbrowser/command-interpreter/exe-param", 1);
      digester.addCallParam("*/rfsbrowser/command-interpreter", 2, "enabled");
      digester.addCallParam("*/rfsbrowser/command-interpreter", 3, "maxtime");
      digester.addCallMethod("*/rfsbrowser/forbidden-drives/drive", "addDrive", 0);
   }

   public void allowedSentence(String name) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager != null) {
         manager.getSqlConsole().registerAllowedSentence(name);
      }

   }

   public void disallowedSentence(String name) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager != null) {
         manager.getSqlConsole().registerDisallowedSentence(name);
      }

   }

   public Element generateXml(Element parent) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager == null) {
         return parent;
      } else {
         Element vfsdoctorElement = parent.addElement("vfsdoctor").addAttribute("pool", manager.getPoolName());
         Element pluginsElement = vfsdoctorElement.addElement("plugins");
         Iterator it = manager.getPluginManager().getPlugins().iterator();

         Element disallowedElement;
         while(it.hasNext()) {
            I_CmsVfsDoctorPlugin plugin = (I_CmsVfsDoctorPlugin)it.next();
            disallowedElement = pluginsElement.addElement("plugin");
            disallowedElement.addAttribute("class", plugin.getClass().getName());
         }

         Element sqlconsoleElement = vfsdoctorElement.addElement("sqlconsole");
         Element allowedElement = sqlconsoleElement.addElement("allowedsentences");
         it = manager.getSqlConsole().getAllowedSentences().iterator();

         Element restPaths;
         while(it.hasNext()) {
            String sentence = (String)it.next();
            restPaths = allowedElement.addElement("sentence");
            restPaths.addAttribute("name", sentence);
         }

         disallowedElement = sqlconsoleElement.addElement("disallowedsentences");
         it = manager.getSqlConsole().getDisallowedSentences().iterator();

         Element cmd;
         while(it.hasNext()) {
            String sentence = (String)it.next();
            cmd = disallowedElement.addElement("sentence");
            cmd.addAttribute("name", sentence);
         }

         Element rfsBrowserElement = vfsdoctorElement.addElement("rfsbrowser");
         rfsBrowserElement.addAttribute("read-only", "" + manager.getRfsBrowser().isReadOnly());
         rfsBrowserElement.addElement("initial-dir").addText(manager.getRfsBrowser().getInitialDir());
         restPaths = rfsBrowserElement.addElement("restrict-paths");
         restPaths.addAttribute("whitelist", "" + manager.getRfsBrowser().isRestrictWhitelist());
         it = manager.getRfsBrowser().getRestrictedPaths().iterator();

         while(it.hasNext()) {
            String path = (String)it.next();
            restPaths.addElement("path").addText(path);
         }

         cmd = rfsBrowserElement.addElement("command-interpreter");
         cmd.addAttribute("enabled", "" + manager.getRfsBrowser().isCmdEnabled());
         cmd.addAttribute("maxtime", "" + manager.getRfsBrowser().getMaxTime() / 1000L);
         cmd.addElement("command").addText(manager.getRfsBrowser().getCommand());
         cmd.addElement("exe-param").addText(manager.getRfsBrowser().getExeParam());
         Element forbiddenDrives = rfsBrowserElement.addElement("forbidden-drives");
         it = manager.getRfsBrowser().getForbiddenDrives().iterator();

         while(it.hasNext()) {
            String drive = (String)it.next();
            forbiddenDrives.addElement("drive").addText(drive);
         }

         return vfsdoctorElement;
      }
   }

   public String getDtdFilename() {
      return "ocee-vfsdoctor.dtd";
   }

   public String getDtdSystemLocation() {
      return "org/opencms/ocee/vfsdoctor/";
   }

   public String getDtdUrlPrefix() {
      return "http://www.alkacon.com/dtd/6.0/";
   }

   public void initConfiguration() {
      try {
         CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
         if (manager != null) {
            manager.initialize();
         }

         if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key("INIT_VFSDOCTOR_CONFIGURED_0"));
         }

      } catch (CmsException var2) {
         throw new CmsRuntimeException(var2.getMessageContainer(), var2);
      }
   }

   public void registerPlugin(String className) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager != null) {
         manager.getPluginManager().registerPlugin(className);
      }

   }

   public void setCommandInterpreter(String command, String exeParam, String enabled, String maxTime) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager != null) {
         manager.getRfsBrowser().setCommandInterpreter(command, exeParam, Boolean.valueOf(enabled), Long.valueOf(maxTime));
      }

   }

   public void setInitialDir(String path) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager != null) {
         manager.getRfsBrowser().setInitialDir(path);
      }

   }

   public void setPool(String pool) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager != null) {
         manager.setPoolName(pool);
      }

   }

   public void setReadOnly(String flag) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager != null) {
         manager.getRfsBrowser().setReadOnly(Boolean.valueOf(flag));
      }

   }

   public void setWhitelist(String flag) {
      CmsVfsDoctorManager manager = CmsVfsDoctorManager.getInstance();
      if (manager != null) {
         manager.getRfsBrowser().setRestrictWhitelist(Boolean.valueOf(flag));
      }

   }

   protected void initMembers() {
      CmsOceeManager.getInstance().checkOceeVersion();
   }
}
