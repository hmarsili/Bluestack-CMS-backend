package org.opencms.ocee.license;

import org.apache.commons.digester3.Digester;
import org.dom4j.Element;
import org.opencms.configuration.A_CmsXmlConfiguration;
import org.opencms.ocee.base.CmsOceeManager;

public class CmsLicenseConfiguration extends A_CmsXmlConfiguration {
   protected static final String oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do = "key";
   protected static final String Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = "license";
   private static final String OO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000for = "org/opencms/ocee/license/";
   private static final String õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int = "ocee-license.dtd";
   private static final String ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float = "http://www.alkacon.com/dtd/6.0/";
   private static final String Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = "ocee-license.xml";
   private String activationKey;
   private int developmentTime;
   private String distribution;
   private boolean enabled = true;
   private Boolean evaluation;
   private long expirationDate;
   private long licenseDate;
   private String licenseKey;
   private String licenseName;

   public CmsLicenseConfiguration() {
      this.setXmlFileName("ocee-license.xml");
   }

   public void addXmlDigesterRules(Digester digester) {
      digester.addCallMethod("*/license", "initConfiguration");
      digester.addCallMethod("*/name", "setLicenseName", 0);
      digester.addCallMethod("*/key", "setLicenseKey", 0);
   }

   public Element generateXml(Element parent) {
      Element licenseElement = parent.addElement("license");
      licenseElement.addElement("name").addText(this.getLicenseName());
      licenseElement.addElement("key").addText(this.getLicenseKey());
      return licenseElement;
   }

   public String getActivationKey() {
      return this.activationKey;
   }

   public int getDevelopmentTime() {
      return this.developmentTime;
   }

   public String getDistribution() {
      return this.distribution;
   }

   public String getDtdFilename() {
      return "ocee-license.dtd";
   }

   public String getDtdSystemLocation() {
      return "org/opencms/ocee/license/";
   }

   public String getDtdUrlPrefix() {
      return "http://www.alkacon.com/dtd/6.0/";
   }

   public long getExpirationDate() {
      return this.expirationDate;
   }

   public long getLicenseDate() {
      return this.licenseDate;
   }

   public String getLicenseKey() {
      return this.licenseKey;
   }

   public String getLicenseName() {
      return this.licenseName;
   }

   public String getXmlFileName() {
      return "ocee-license.xml";
   }

   public void initConfiguration() {
      CmsLicenseManager.getInstance().initialize(this);
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public Boolean isEvaluation() {
      return this.evaluation;
   }

   public void setActivationKey(String activationKey) {
      this.activationKey = activationKey;
   }

   public void setDevelopmentTime(int developmentTime) {
      this.developmentTime = developmentTime;
   }

   public void setDistribution(String distribution) {
      this.distribution = distribution;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public void setEvaluation(Boolean evaluation) {
      this.evaluation = evaluation;
   }

   public void setExpirationDate(long expirationDate) {
      this.expirationDate = expirationDate;
   }

   public void setLicenseDate(long licenseDate) {
      this.licenseDate = licenseDate;
   }

   public void setLicenseKey(String licenseKey) {
      this.licenseKey = licenseKey;
   }

   public void setLicenseName(String licenseName) {
      this.licenseName = licenseName;
   }

   protected void initMembers() {
      CmsOceeManager.getInstance().checkOceeVersion();
   }
}
