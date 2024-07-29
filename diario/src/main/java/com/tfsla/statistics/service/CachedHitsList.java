package com.tfsla.statistics.service;

import com.tfsla.rankViews.service.TfsRankingService;
import com.tfsla.statistics.SoapConfig;
import com.tfsla.statistics.model.TfsHitPage;

import java.rmi.RemoteException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class CachedHitsList {

	private static final Log LOG = CmsLog.getLog(CachedHitsList.class);

	private LinkedHashMap<TfsHitPage,TfsHitPage> pages = null;

	private long hitsRecorded = 0;
	
	private static CachedHitsList instance = new CachedHitsList();
	
	private CachedHitsList()
	{
		pages = new LinkedHashMap<TfsHitPage,TfsHitPage>();
	}
	
	public static CachedHitsList getInstance() {
		return instance;
	}
	
	public void addHit (TfsHitPage page) {
		hitsRecorded++;
		TfsHitPage storedPage = pages.get(page);
		if (storedPage!=null) {
			storedPage.setCantidad(storedPage.getCantidad()+page.getCantidad());
		}
		else
			pages.put(page,page);
		
		if (pages.size()==SoapConfig.getInstance().getCachedHitMaxLimit())
		{
			String result = sendHits();
			LOG.info("Enviando noticias cacheadas " + result);
		}
		
	}
	
	public String sendHits()
	{

		LinkedHashMap<TfsHitPage,TfsHitPage> sendPages = pages;
		pages = new LinkedHashMap<TfsHitPage,TfsHitPage>();
		
		long hitsToSend = hitsRecorded;
		hitsRecorded=0;
		
		if (sendPages.size()>0) {
			
			String resuelt=" - enviando "+ hitsToSend + " hits en " + sendPages.size() + " paquetes de datos - ";
	
			try {
				TfsHitPage[] vector = sendPages.values().toArray(new TfsHitPage[sendPages.size()]);
				
				int hitsPerSend = SoapConfig.getInstance().getCachedHitMaxHitPerSend();
				int totBlocks = sendPages.size() / hitsPerSend;
				for (int j=0; j<=totBlocks;j++)
				{
				
					int size = (j<totBlocks ? hitsPerSend : sendPages.size() % hitsPerSend); 
					TfsHitPage[] vecPart = new TfsHitPage[size];
					System.arraycopy(vector, j*hitsPerSend, vecPart, 0, size);
				
					TfsRankingService rService = new TfsRankingService();
					rService.addHits(vecPart);
					
				}
				resuelt+= "OK.\n";
			} catch (RemoteException e) {
				e.printStackTrace();
				resuelt+= "ERROR: " + e.getMessage() + ".\n";
			}
			
			return resuelt;
		}
		else 
			return "No hay hits a registrar.\n";
	}
}
