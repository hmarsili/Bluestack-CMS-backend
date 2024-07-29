package com.tfsla.rankViews.collector;

import java.util.ArrayList;
import java.util.List;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import com.tfsla.statistics.model.TfsStatisticsOptions;

public class MasValoradosCollectors extends A_ServerStatisticsCollector {

	private int rankMode = TfsStatisticsOptions.RANK_VALORACIONES_PROMEDIO;
	
	public List getCollectorNames() {
		List<String> nombres = new ArrayList<String>();
		nombres.add("CollectorMasValorados");
		return nombres;
	}

	public List getResults(CmsObject cms, String collectorName, String param) throws CmsDataAccessException, CmsException {
		parseParam(param, cms);
		parseExtraParam(param,cms);
		
		TfsStatisticsOptions options = getOptions(rankMode);
		options.setShowValoracion(true);
		return getResources(options,cms);
	}

	public void parseExtraParam(String params, CmsObject cms)
	{
		String[] values = params.split("\\|");
		int i;
		for (i=2; i<values.length;i++)
		{
			String[] param = values[i].split(":");
			if (param[0].equalsIgnoreCase("modoRanking"))
			{
				if (param[1].equalsIgnoreCase("positivos"))
					rankMode = TfsStatisticsOptions.RANK_VALORACIONES_POSITIVO;
				else if (param[1].equalsIgnoreCase("negativos"))
					rankMode = TfsStatisticsOptions.RANK_VALORACIONES_NEGATIVO;
				else if (param[1].equalsIgnoreCase("totales"))
					rankMode = TfsStatisticsOptions.RANK_VALORACIONES_CANTIDAD;
				else if (param[1].equalsIgnoreCase("promedio"))
					rankMode = TfsStatisticsOptions.RANK_VALORACIONES_PROMEDIO;		
			}
		}
	}
}
