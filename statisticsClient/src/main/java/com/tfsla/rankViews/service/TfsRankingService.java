package com.tfsla.rankViews.service;

import java.rmi.RemoteException;

import com.tfsla.rankViews.AddComentario;
import com.tfsla.rankViews.AddCustomEvents;
import com.tfsla.rankViews.AddHit;
import com.tfsla.rankViews.AddHits;
import com.tfsla.rankViews.AddRecomendacion;
import com.tfsla.rankViews.AddValoracion;
import com.tfsla.rankViews.GetStatistics;
import com.tfsla.rankViews.PutTags;
import com.tfsla.rankViews.RemoveStatistics;
import com.tfsla.rankViews.TfsRankingViewsStub;
import com.tfsla.rankViews.model.TfsRankResults;
import com.tfsla.statistics.BlockedException;
import com.tfsla.statistics.model.TfsHitPage;
import com.tfsla.statistics.model.TfsStatisticsOptions;
import com.tfsla.statistics.service.A_TfsRankingService;

public class TfsRankingService extends A_TfsRankingService {


	protected TfsRankResults getRankingFromCache(TfsStatisticsOptions options, String cacheName) throws RemoteException
	{
		if (TfsCacheManager.getInstance().cacheExists(cacheName))
		{
			TfsRankResults result = (TfsRankResults) TfsCacheManager.getInstance().getObjectFromCache(cacheName, options);
			if (result!=null)
				return result;
			else
			{
				result = getStatsFromRemote(options);
				TfsCacheManager.getInstance().putObjectToCache(cacheName, options, result);
				return result;
			}
		}
		else {
			TfsRankResults result = getStatsFromRemote(options);
			return result;			
		}
		
	}
	
	public TfsRankResults getRankingStatistics(TfsStatisticsOptions options) throws RemoteException
	{
		if (options.isUseCachedresultsSpecified() && !options.getUseCachedresults())
			return getStatsFromRemote(options);
		else if ( 
				(options.getUrl()!=null && !options.getUrl().trim().equals("")) || 
				(options.getUrls()!=null && options.getUrls().length>0))
			return getRankingFromCache(options,"pageStats");
		else
			return getRankingFromCache(options,"stats");
		
	}

	private TfsRankResults getStatsFromRemote(TfsStatisticsOptions options) throws RemoteException
	{
		if (!isBlocked()) {
			try {
				TfsRankingViewsStub stat = new TfsRankingViewsStub();
				GetStatistics getStatistics40 = new GetStatistics();
				
				getStatistics40.setOptions(options);
				return stat.getStatistics(getStatistics40).get_return();
			} catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de páginas esta bloqueado preventivamente luego de multiples errores de conexión");

	}
	
	public void addRecomendation(TfsHitPage page) throws RemoteException
	{
		if (!isBlocked()) {
			try {
		        	
	            TfsRankingViewsStub rank = new TfsRankingViewsStub();        	
	            AddRecomendacion addRecomendacion = new AddRecomendacion();
	            addRecomendacion.setPage(page);
				rank.addRecomendacion(addRecomendacion);
	
			} catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de páginas esta bloqueado preventivamente luego de multiples errores de conexión");
        
	}

	public void addComentario(TfsHitPage page) throws RemoteException
	{
		if (!isBlocked()) {
	        try {	
	            TfsRankingViewsStub rank = new TfsRankingViewsStub();        	
	        	AddComentario addComentario = new AddComentario();
	        	addComentario.setPage(page);
				rank.addComentario(addComentario);
	
			} catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de páginas esta bloqueado preventivamente luego de multiples errores de conexión");        
	}

	public void addValoracion(TfsHitPage page) throws RemoteException
	{
		if (!isBlocked()) {
	        try {
	            TfsRankingViewsStub rank = new TfsRankingViewsStub();        	
	        	AddValoracion addValoracion = new AddValoracion();
	        	addValoracion.setPage(page);
				rank.addValoracion(addValoracion);
			} catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de páginas esta bloqueado preventivamente luego de multiples errores de conexión");        
        
	}

	public void removeResourceFromStatistics(TfsStatisticsOptions options) throws RemoteException
	{
		if (!isBlocked()) {
		
        try {
            TfsRankingViewsStub rank = new TfsRankingViewsStub();        	
            RemoveStatistics removeStatistics = new RemoveStatistics();			
        	removeStatistics.setOptions(options);
			rank.removeStatistics(removeStatistics);
			
		} catch (RemoteException e) {
			manageError();
			throw e;
		}
	}
	else throw new BlockedException("Los rankings de páginas esta bloqueado preventivamente luego de multiples errores de conexión");        
		
	}

	public void addHit(TfsHitPage page) throws RemoteException
	{
		if (!isBlocked()) {
	        try {	
	            TfsRankingViewsStub rank = new TfsRankingViewsStub();        	
	        	AddHit addHit = new AddHit();
	        	addHit.setPage(page);
				rank.addHit(addHit);
	
			} catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de páginas esta bloqueado preventivamente luego de multiples errores de conexión");        
	}
	
	public void putTags(TfsHitPage page) throws RemoteException
	{
		if (!isBlocked()) {
	        try {	
	            TfsRankingViewsStub rank = new TfsRankingViewsStub();        	
	            PutTags putTags = new PutTags();
	            putTags.setPage(page);
				rank.putTags(putTags);
	
			} catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de páginas esta bloqueado preventivamente luego de multiples errores de conexión");        
	}

	public void addHits(TfsHitPage[] page) throws RemoteException
	{
		if (!isBlocked()) {
	        try {	
	            TfsRankingViewsStub rank = new TfsRankingViewsStub();        	
	        	AddHits addHits = new AddHits();
	        	addHits.setPages(page);
				rank.addHits(addHits);
	
			} catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de páginas esta bloqueado preventivamente luego de multiples errores de conexión");        
	}

	public void AddCustomEvents(TfsHitPage page) throws RemoteException
	{
		if (!isBlocked()) {
	        try {	
	            TfsRankingViewsStub rank = new TfsRankingViewsStub();        	
	        	AddCustomEvents addCustomEvents = new AddCustomEvents();
	        	addCustomEvents.setPage(page);
				rank.addCustomEvents(addCustomEvents);
			} catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de páginas esta bloqueado preventivamente luego de multiples errores de conexión");        
	}


}
