package com.tfsla.rankViews.service;

import java.rmi.RemoteException;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;


import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsKeyValue;
import com.tfsla.statistics.service.I_statisticsDataCollector;

public class CommentRankService extends RankService {

	private static final Log LOG = CmsLog.getLog(CommentRankService.class);

	public void addHitCustom(CmsResource res, String commentId,CmsObject cms, HttpSession session, int nro, int value)
	{

		I_statisticsDataCollector dCollector = new TfsRankComentarioDataCollector();

		TfsHitPage page = new TfsHitPage();

		collectInformation(cms,res,session,page);
		
		page.setURL(cms.getRequestContext().removeSiteRoot(res.getRootPath()) + "?cId="+ commentId);
		page.setTipoContenido(dCollector.getContentType());
		
		String sitePath = getSiteName(cms);
		String siteName = sitePath.replaceFirst("/sites/", "");

		TfsKeyValue key = new TfsKeyValue();
		key.setKey("cId");
		key.setValue(commentId);

		page.setValues((TfsKeyValue[]) ArrayUtils.add(page.getValues(), key)); 

		page.setSitio(siteName);

		switch (nro)
		{
		case 1:
			page.setCustom1(value);
			break;
		case 2:
			page.setCustom2(value);
			break;
		case 3:
			page.setCustom3(value);
			break;
		case 4:
			page.setCustom4(value);
			break;
		case 5:
			page.setCustom5(value);
			break;
		case 6:
			page.setCustom6(value);
			break;
		case 7:
			page.setCustom7(value);
			break;
		case 8:
			page.setCustom8(value);
			break;
		case 9:
			page.setCustom9(value);
			break;
		case 10:
			page.setCustom10(value);
			break;
		}

        try {
			this.AddCustomEvents(page);

		} catch (RemoteException e) {
			LOG.error("Error al agregar el valor custom al comentario.",e);
		}

	}

	
	public void addValoracion(CmsResource res, String commentId,CmsObject cms, HttpSession session, int valor)
	{

		I_statisticsDataCollector dCollector = new TfsRankComentarioDataCollector();

		TfsHitPage page = new TfsHitPage();

		collectInformation(cms,res,session,page);
		
		page.setURL(cms.getRequestContext().removeSiteRoot(res.getRootPath()) + "?cId="+ commentId);
		page.setTipoContenido(dCollector.getContentType());
		
		TfsKeyValue key = new TfsKeyValue();
		key.setKey("cId");
		key.setValue(commentId);

		page.setValues((TfsKeyValue[]) ArrayUtils.add(page.getValues(), key)); 
		page.setValoracion(valor);		

		String sitePath = getSiteName(cms);
		String siteName = sitePath.replaceFirst("/sites/", "");
		page.setSitio(siteName);
		
        try {
			this.addValoracion(page);

		} catch (RemoteException e) {
			LOG.error("Error al agregar la vaaloracion al comentario.",e);
		}

	}

	public void addRecomendacion(CmsResource res, String commentId,CmsObject cms, HttpSession session)
	{

		I_statisticsDataCollector dCollector = new TfsRankComentarioDataCollector();

		TfsHitPage page = new TfsHitPage();

		collectInformation(cms,res,session,page);
		
		page.setURL(cms.getRequestContext().removeSiteRoot(res.getRootPath()) + "?cId="+ commentId);
		page.setTipoContenido(dCollector.getContentType());
		
		TfsKeyValue key = new TfsKeyValue();
		key.setKey("cId");
		key.setValue(commentId);

		page.setValues((TfsKeyValue[]) ArrayUtils.add(page.getValues(), key)); 
		page.setRecomendacion(1);		

		String sitePath = getSiteName(cms);
		String siteName = sitePath.replaceFirst("/sites/", "");
		page.setSitio(siteName);
		
        try {
			this.addRecomendation(page);

		} catch (RemoteException e) {
			LOG.error("Error al agregar la recomendacion al comentario.",e);
		}

	}

}
