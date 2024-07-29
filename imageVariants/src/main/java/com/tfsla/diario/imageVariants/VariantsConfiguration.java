package com.tfsla.diario.imageVariants;


import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VariantsConfiguration {

	private String moduleName = "imageVariants";
	private String siteName;
	private String publication;
	private CPMConfig config;
	
	public VariantsConfiguration(String siteName, String publication ) {
		this.siteName = siteName;
		this.publication = publication;
		
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	}
	
	public List<String> getVariant(String resourceType) {
		
		List<String> variants = config.getListItempGroupParam(siteName, publication, moduleName,resourceType,"variants");
		
		return variants;
	}
	
	public List<String> getFormats(String resourceType) {
		
		List<String> formats = config.getListItempGroupParam(siteName, publication, moduleName,resourceType,"formats");
		
		
		return formats;
	}
	
	public String getPubId() {
		return config.getParam(siteName, publication, moduleName,"pubId");
	}
	
	public String getPrefixToRemove() {
		return config.getParam(siteName, publication, moduleName,"removePrefix");
	}
	
	public String getEndPointGenerateVariants() {
		return config.getParam(siteName, publication, moduleName,"generate-endpoint");
	}
	
	public String getEndPointDeleteVariants() {
		return config.getParam(siteName, publication, moduleName,"delete-endpoint");
	}
	
	public boolean isModuleEnabled() {
		return config.getBooleanParam(siteName, publication, moduleName,"enabled");
	}
	
	public Date getDateStart() {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		
		String startDate = config.getParam(siteName, publication, moduleName,"startDate");
		try {
			return formatter.parse(startDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
