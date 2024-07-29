package com.tfsla.opencmsdev.encuestas;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.relations.CmsCategory;
import org.opencms.relations.CmsCategoryService;
import org.opencms.widgets.CmsSelectWidgetOption;

public class GetCategoriesParaComboProcess extends AbstractEncuestaProcess {


	@SuppressWarnings("unchecked")
	public List<CmsSelectWidgetOption> execute(CmsObject cms) {
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();
		
		String PATH_CATEGORIA = GetEncuestasProperties.getInstance(cms).getCategoriesPath();
		String SUB_PATH_CATEGORIA = GetEncuestasProperties.getInstance(cms).getCategoriesSubPath();

		CmsCategoryService categoryService= new CmsCategoryService();
		try {
			List<CmsCategory> categorias = categoryService.readCategories(cms, SUB_PATH_CATEGORIA, true, "/");
			
			for(CmsCategory c: categorias)
			{		
				String option = (String) c.getPath();
				String cOption = option.replaceFirst(""+PATH_CATEGORIA+"", "");
				widgetOptions.add(new CmsSelectWidgetOption(cOption));
			}
			
		} catch (CmsException e) {	
			e.printStackTrace();
		}
		
		
		return widgetOptions;
	}
}