package com.tfsla.opencmsdev.encuestas;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsObject;

public class GetGruposProcess {
	
	public List execute(CmsObject cms) {
		List grupos = new ArrayList();
		List<String> stringOptions = GetEncuestasProperties.getInstance(cms).getGrupos();
		for (String option : stringOptions) {
			grupos.add(option);
		}
		return grupos;
	}

}
