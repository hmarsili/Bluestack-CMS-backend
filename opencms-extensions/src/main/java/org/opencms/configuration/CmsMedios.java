package org.opencms.configuration;


import java.io.File;
import java.net.URISyntaxException;

import org.apache.commons.digester.Digester;
import org.dom4j.Element;
import org.opencms.main.CmsLog;

public class CmsMedios  extends A_CmsXmlConfiguration {

	
	public static final String N_ROOT = "cmsMedios";
    public static final String N_SITIOS = "sites";
    public static final String N_SITIO = "site";
    
    public static final String N_PUBLICACIONES = "publications";
    public static final String N_PUBLICACION = "publication";
    public static final String N_MODULOS = "modules";
    public static final String N_MODULO = "module";
    public static final String N_PARAM = "param";
    public static final String N_PARAMGROUP = "param-group";
    
    private static final String XPATH_SITIO = N_ROOT + "/" + N_SITIOS + "/" + N_SITIO;
    private static final String XPATH_PUBLICACION = N_ROOT + "/" + N_SITIOS + "/" + N_SITIO + "/" + N_PUBLICACIONES + "/" + N_PUBLICACION;
    private static final String XPATH_MODULO = N_ROOT + "/" + N_SITIOS + "/" + N_SITIO + "/" + N_PUBLICACIONES + "/" + N_PUBLICACION + "/" + N_MODULOS + "/" + N_MODULO;
    private static final String XPATH_PARAM = N_ROOT + "/" + N_SITIOS + "/" + N_SITIO + "/" + N_PUBLICACIONES + "/" + N_PUBLICACION + "/" + N_MODULOS + "/" + N_MODULO + "/" + N_PARAM;

    private static final String XPATH_PARAMGROUP = N_ROOT + "/" + N_SITIOS + "/" + N_SITIO + "/" + N_PUBLICACIONES + "/" + N_PUBLICACION + "/" + N_MODULOS + "/" + N_MODULO + "/" + N_PARAMGROUP;
    private static final String XPATH_PARAMGROUPITEM = N_ROOT + "/" + N_SITIOS + "/" + N_SITIO + "/" + N_PUBLICACIONES + "/" + N_PUBLICACION + "/" + N_MODULOS + "/" + N_MODULO + "/" + N_PARAMGROUP + "/" + N_PARAM;

    private static final String XPATH_MODULOGENERAL = N_ROOT + "/" + N_MODULOS + "/" + N_MODULO;
    private static final String XPATH_PARAMGENERAL = N_ROOT + "/" + N_MODULOS + "/" + N_MODULO + "/" + N_PARAM;

    private static final String XPATH_PARAMGROUPGENERAL = N_ROOT + "/" + N_MODULOS + "/" + N_MODULO + "/" + N_PARAMGROUP;
    private static final String XPATH_PARAMGROUPITEMGENERAL = N_ROOT + "/" + N_MODULOS + "/" + N_MODULO + "/" + N_PARAMGROUP + "/" + N_PARAM;

    private static final String XPATH_MODULOSITIO = N_ROOT + "/" + N_SITIOS + "/" + N_SITIO + "/" + N_MODULOS + "/" + N_MODULO;
    private static final String XPATH_PARAMSITIO = N_ROOT + "/" + N_SITIOS + "/" + N_SITIO + "/" + N_MODULOS + "/" + N_MODULO + "/" + N_PARAM;

    private static final String XPATH_PARAMGROUPSITIO = N_ROOT + "/" + N_SITIOS + "/" + N_SITIO + "/" + N_MODULOS + "/" + N_MODULO + "/" + N_PARAMGROUP;
    private static final String XPATH_PARAMGROUPITEMSITIO = N_ROOT + "/" + N_SITIOS + "/" + N_SITIO + "/" + N_MODULOS + "/" + N_MODULO + "/" + N_PARAMGROUP + "/" + N_PARAM;

        
    
    public static final String N_KEY = "key";

    private CPMConfig config =null;
    
    
    public static final String CONFIGURATION_DTD_NAME = "cmsMedios.dtd";

    public static final String DEFAULT_XML_FILE_NAME = "cmsMedios.xml";
	private static final String DTD_PREFIX = "http://localhost:8180/sniffer/reciver/dtd/4.0/";


    private static CmsMedios instance;
    
    
    public CmsMedios() {
    	instance = this;
	    setXmlFileName(DEFAULT_XML_FILE_NAME);
	    if (CmsLog.INIT.isInfoEnabled()) {
	        CmsLog.INIT.info(MessagesCmsMedios.get().getBundle().key(MessagesCmsMedios.INIT_CMSMEDIOS_CONFIG_INIT_0));
	    }
	     
	}

	@Override
	public void addXmlDigesterRules(Digester digester) {
		
		config = new CPMConfig();
		
		digester.addObjectCreate(XPATH_MODULOGENERAL, CPMModuleConfig.class);
		digester.addSetNext(XPATH_MODULOGENERAL, "addModule", "org.opencms.configuration.CPMModuleConfig");
		digester.addCallMethod(XPATH_MODULOGENERAL, "setName", 1);
		digester.addCallParam(XPATH_MODULOGENERAL, 0, N_NAME);

		digester.addCallMethod(XPATH_PARAMGENERAL, "addParam", 2);
		digester.addCallParam(XPATH_PARAMGENERAL, 0, N_NAME);
		digester.addCallParam(XPATH_PARAMGENERAL, 1);
	

		digester.addCallMethod(XPATH_PARAMGROUPITEMGENERAL, "addParamItemToGroup", 2);
		digester.addCallParam(XPATH_PARAMGROUPITEMGENERAL, 0, N_NAME);
		digester.addCallParam(XPATH_PARAMGROUPITEMGENERAL, 1);

		digester.addCallMethod(XPATH_PARAMGROUPGENERAL, "creataParamGroup", 1);
		digester.addCallParam(XPATH_PARAMGROUPGENERAL, 0, N_NAME);
		
		digester.addObjectCreate(XPATH_SITIO, CPMSiteConfig.class);
		digester.addSetProperties(XPATH_SITIO,N_NAME,N_NAME);

		digester.addObjectCreate(XPATH_MODULOSITIO, CPMModuleConfig.class);
		digester.addSetProperties(XPATH_MODULOSITIO,N_NAME,N_NAME);
		
		digester.addCallMethod(XPATH_PARAMSITIO, "addParam", 2);
		digester.addCallParam(XPATH_PARAMSITIO, 0, N_NAME);
		digester.addCallParam(XPATH_PARAMSITIO, 1);

		digester.addCallMethod(XPATH_PARAMGROUPITEMSITIO, "addParamItemToGroup", 2);
		digester.addCallParam(XPATH_PARAMGROUPITEMSITIO, 0, N_NAME);
		digester.addCallParam(XPATH_PARAMGROUPITEMSITIO, 1);

		digester.addCallMethod(XPATH_PARAMGROUPSITIO, "creataParamGroup", 1);
		digester.addCallParam(XPATH_PARAMGROUPSITIO, 0, N_NAME);

		digester.addObjectCreate(XPATH_PUBLICACION, CPMPublicationConfig.class);
		digester.addSetProperties(XPATH_PUBLICACION,N_NAME,N_NAME);
		
		digester.addObjectCreate(XPATH_MODULO, CPMModuleConfig.class);
		digester.addSetProperties(XPATH_MODULO,N_NAME,N_NAME);

		digester.addCallMethod(XPATH_PARAM, "addParam", 2);
		digester.addCallParam(XPATH_PARAM, 0, N_NAME);
		digester.addCallParam(XPATH_PARAM, 1);

		digester.addCallMethod(XPATH_PARAMGROUPITEM, "addParamItemToGroup", 2);
		digester.addCallParam(XPATH_PARAMGROUPITEM, 0, N_NAME);
		digester.addCallParam(XPATH_PARAMGROUPITEM, 1);

		digester.addCallMethod(XPATH_PARAMGROUP, "creataParamGroup", 1);
		digester.addCallParam(XPATH_PARAMGROUP, 0, N_NAME);

		digester.addSetNext(XPATH_MODULO, "addModule", "org.opencms.configuration.CPMModuleConfig");
		
		digester.addSetNext(XPATH_PUBLICACION, "addPublication", "org.opencms.configuration.CPMPublicationConfig");
		
		digester.addSetNext(XPATH_MODULOSITIO, "addModule", "org.opencms.configuration.CPMModuleConfig");
		
		digester.addSetNext(XPATH_SITIO, "addSite", "org.opencms.configuration.CPMSiteConfig");

	}
	
	public void addSite(CPMSiteConfig site)
	{
		config.addSite(site);
	}
	
	public void addModule(CPMModuleConfig module)
	{
		config.addModule(module);
	}
	
	public CPMConfig getCmsParaMediosConfiguration()
	{
		return config;
	}
	
	public void addSiteConfig(CPMSiteConfig site)
	{
		config.addSite(site);
	}
	
	
	@Override
	public Element generateXml(Element parent) {
        Element cmsMediosElement = parent;
		
        //cargo las configuraciones de sitios y publicaciones
        CPMSiteConfig[] sites = config.getSites();
        if (sites.length>0) {
        	Element sitiosElement = cmsMediosElement.addElement(N_SITIOS);
        	for ( CPMSiteConfig site : sites) {
        		Element sitioElement = sitiosElement.addElement(N_SITE);
        		sitioElement.addAttribute(N_NAME, site.getName());
        		
        		CPMPublicationConfig[] publications = site.getPublications();
        		if (publications.length>0) {
        			Element publicationsElement = sitioElement.addElement(N_PUBLICACIONES);
        			for ( CPMPublicationConfig publication : publications) {
        				Element publicacionElement = publicationsElement.addElement(N_PUBLICACION);
        				publicacionElement.addAttribute(N_NAME, publication.getName());
        				
        				//agrego los modulos de una publicacion
        				CPMModuleConfig[] modules = publication.getModules();
        				if (modules.length>0) {
        					Element modulesElement = publicacionElement.addElement(N_MODULOS);
        					for (CPMModuleConfig module : modules ) {
        						generateModule(modulesElement, module);
        					}
        				}
        			}
        		}
        		
        		//agrego los modulos de un sitio
        		CPMModuleConfig[] modules = site.getModules();
        		if (modules.length>0) {
        			Element modulesElement = sitioElement.addElement(N_MODULOS);
					for (CPMModuleConfig module : modules ) {
						generateModule(modulesElement, module);
					}
        		}
        		
        	}
        }
        
      //agrego los modulos generales
		CPMModuleConfig[] modules = config.getModules();
		if (modules.length>0) {
			Element modulesElement = cmsMediosElement.addElement(N_MODULOS);
			for (CPMModuleConfig module : modules ) {
				generateModule(modulesElement, module);
			}
		}
        return cmsMediosElement;
	}

	private void generateModule(Element modulesElement, CPMModuleConfig module) {
		Element moduleElement = modulesElement.addElement(N_MODULO);
		moduleElement.addAttribute(N_NAME, module.getName());
		
		String[] params = module.getParamsNames();
		
		for (String param : params){
			Element paramElement = moduleElement.addElement(N_PARAM);
			paramElement.addAttribute(N_NAME, param);
			paramElement.addText(module.getParam(param));
			
		}
		
		String[] paramsGroups = module.getParamsGroupNames();
		for (String paramGroup : paramsGroups){
			Element paramGroupElement = moduleElement.addElement(N_PARAMGROUP);
			paramGroupElement.addAttribute(N_NAME, paramGroup);
			
			String[] params_group = module.getParamsNameFromGroup(paramGroup);
			for (String param : params_group){
				Element paramElement = paramGroupElement.addElement(N_PARAM);
				paramElement.addAttribute(N_NAME, param);
				paramElement.addText(module.getParamItemGroup(paramGroup,param));
				
			}
		}
	}

	@Override
	public String getDtdFilename() {
		 return CONFIGURATION_DTD_NAME;
	}

	@Override
	protected void initMembers() {
	}
	
	public static CmsMedios getInstance() {
		return instance;
	}

	@Override
	public String getDtdUrlPrefix() {
		try {
			return new File(CmsMedios.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent().replace("WEB-INF/lib", "WEB-INF/config") + "/";
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return DTD_PREFIX;
	}
	
	@Override
	public String getRootElementName() {
		return N_ROOT;
	}

}
