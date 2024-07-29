package com.tfsla.opencms.webusers.rankings;

import java.util.ArrayList;
import java.util.List;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;

import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.TipoEdicionBaseService;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfigurationLoader;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderField;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConfigurationException;

public class RankingDimensionsManager {

	public RankingDimensionsManager(CmsObject cms) throws InvalidConfigurationException {

		this.config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
		publication = "0";
     	TipoEdicionBaseService tService = new TipoEdicionBaseService();
    	try {
			TipoEdicion tEdicion = tService.obtenerTipoEdicion(cms, cms.getRequestContext().getUri());			
			if (tEdicion!=null)
				publication = "" + tEdicion.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}

    	siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
    	
    	this.loadDimensions();
	}

	public UserDimension getDimension(String name) {
		for(UserDimension dimension : this.dimensions) {
			if(dimension.getName().equals(name)) {
				return dimension;
			}
		}
		
		return null;
	}
	
	
	public ArrayList<UserDimension> getRankingDimensions() {

		return this.dimensions;

	}

	private void loadDimensions() throws InvalidConfigurationException {
		if(this.dimensions == null) {
			
			ProviderConfigurationLoader configLoader = new ProviderConfigurationLoader();
			this.dimensions = new ArrayList<UserDimension>();
			List<ProviderField> providerFields = configLoader.getConfiguredFields(siteName, publication);
			List<String> additionalInfoFields = this.config.getParamList(siteName, publication, "webusers", "additionalInfo");
	
			this.dimensions.addAll(providerFields);
	
			for (String field : additionalInfoFields) {
				this.dimensions.add(this.getRankingDimension(field));
			}

		}
	}
	
	private UserDimension getRankingDimension(final String fieldName) {

		return new RankingDimension() {
			{
				setDescription(config.getItemGroupParam(siteName, publication,
						"webusers", fieldName, "niceName", fieldName));
				setEntryName(config.getItemGroupParam(siteName, publication,
						"webusers", fieldName, "entryname", fieldName));
				setType(config.getItemGroupParam(siteName, publication,
						"webusers", fieldName, "type", fieldName));
				setBasePath(config.getItemGroupParam(siteName, publication,
						"webusers", fieldName, "basePath", fieldName));
				setDependsOn(config.getItemGroupParam(siteName, publication,
						"webusers", fieldName, "dependsOn", fieldName));
				setName(fieldName);
			}
		};

	}

	private ArrayList<UserDimension> dimensions;
	private CPMConfig config = null;
	private String siteName;
	private String publication;

}
