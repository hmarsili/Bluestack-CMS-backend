package com.tfsla.rankViews.collector;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import com.tfsla.statistics.model.TfsStatisticsOptions;

public class MasComentadosCollectors  extends A_ServerStatisticsCollector {

	public List getCollectorNames() {
		List<String> nombres = new ArrayList<String>();
		nombres.add("CollectorMasComentados");
		return nombres;

	}

	public List getResults(CmsObject cms, String collectorName, String param) throws CmsDataAccessException, CmsException {
		parseParam(param, cms);
		TfsStatisticsOptions options = getOptions(TfsStatisticsOptions.RANK_COMENTARIOS);
		options.setShowComentarios(true);
		return getResources(options,cms);
	}

}
