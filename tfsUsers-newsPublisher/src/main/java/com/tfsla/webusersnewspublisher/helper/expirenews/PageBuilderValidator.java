package com.tfsla.webusersnewspublisher.helper.expirenews;

import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.xml.types.I_CmsXmlContentValue;

/**
 * Validates if the page is not on the Page Builder 
 */
public class PageBuilderValidator extends CmsXmlValidator {

	public PageBuilderValidator(CmsObject cmsObject, String pageBuilderPath) throws Exception {
		super(cmsObject, pageBuilderPath);
	}

	public Boolean validate(String resourcePath) throws Exception {
		List<I_CmsXmlContentValue> containers = xmlContent.getValues(Strings.CONTAINERS_XML_PATH, locale);
		if(containers != null) {
			for(int i_containers=1; i_containers <= containers.size(); i_containers++) {
				String isHide = xmlContent.getStringValue(cmsObject, String.format(Strings.CONTAINERS_HIDE_XML_PATH_FORMAT, i_containers), locale);
				if(isHide != null && Boolean.valueOf(isHide)) continue;
				
				List<I_CmsXmlContentValue> items = xmlContent.getValues(String.format(Strings.CONTAINERS_ITEMS_XML_PATH, i_containers), locale);
				for(int i_items=1; i_items <= items.size(); i_items++) {
					String tipoItem = xmlContent.getStringValue(cmsObject, String.format(
							Strings.ITEM_TIPO_XML_PATH_FORMAT, 
							i_containers,
							i_items
						), locale
					);
					if(tipoItem == null || !tipoItem.equals(Strings.TIPO_NOTICIA)) continue;
					
					List<I_CmsXmlContentValue> params = xmlContent.getValues(String.format(Strings.ITEMS_PARAMS_XML_PATH,
							i_containers,
							i_items
						), locale
					);
					for(int i_params=1; i_params <= params.size(); i_params++) {
						String paramName = xmlContent.getStringValue(cmsObject, String.format(
								Strings.ITEMS_PARAMS_NAME_XML_PATH, 
								i_containers,
								i_items,
								i_params
							), locale
						);
						if(paramName == null || !paramName.equals(Strings.PARAM_NAME_URL)) continue;
						String paramValue = xmlContent.getStringValue(cmsObject, String.format(
								Strings.ITEMS_PARAMS_VALUE_XML_PATH, 
								i_containers,
								i_items,
								i_params
							), locale
						);
						if(paramValue != null && resourcePath.endsWith(paramValue)) return false;
					}
				}
			}
		}
		return true;
	}
	
}
