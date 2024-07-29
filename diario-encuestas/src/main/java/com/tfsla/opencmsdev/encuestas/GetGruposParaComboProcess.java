package com.tfsla.opencmsdev.encuestas;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;
import org.opencms.widgets.CmsSelectWidgetOption;

public class GetGruposParaComboProcess extends AbstractEncuestaProcess {


	public List<CmsSelectWidgetOption> execute(CmsObject cms) {
		
		List<CmsSelectWidgetOption> widgetOptions = new ArrayList<CmsSelectWidgetOption>();
		List<String> stringOptions = GetEncuestasProperties.getInstance(cms).getGrupos();
		for (String option : stringOptions) {
			widgetOptions.add(new CmsSelectWidgetOption(option));
		}
		return widgetOptions;
	}
}