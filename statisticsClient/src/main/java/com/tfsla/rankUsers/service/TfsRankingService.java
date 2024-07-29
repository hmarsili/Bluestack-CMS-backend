package com.tfsla.rankUsers.service;

import java.rmi.RemoteException;

import com.tfsla.rankUsers.AddHit;
import com.tfsla.rankUsers.GetStatistics;
import com.tfsla.rankUsers.RemoveStatistics;
import com.tfsla.rankUsers.TfsRankingUsersStub;
import com.tfsla.rankUsers.model.TfsUserRankResults;
import com.tfsla.statistics.BlockedException;
import com.tfsla.statistics.model.TfsHitUser;
import com.tfsla.statistics.model.TfsUserStatsOptions;
import com.tfsla.statistics.service.A_TfsRankingService;

public class TfsRankingService extends A_TfsRankingService {

    public void addHit(TfsHitUser hitUser) throws RemoteException
    {

    	if (!isBlocked()) {
	        try {
			    TfsRankingUsersStub rank = new TfsRankingUsersStub();
			    
			    AddHit addHit = new AddHit();
			    addHit.setHit(hitUser);
				rank.addHit(addHit);
				
	        } catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de usuarios están bloqueados preventivamente luego de multiples errores de conexión");	
		
    }

    public TfsUserRankResults getStatistics(TfsUserStatsOptions options) throws RemoteException
    {
 		if (!isBlocked()) {
	        try {
	        	
	            TfsRankingUsersStub rank = new TfsRankingUsersStub();
	            GetStatistics getStatistics = new GetStatistics();
	            getStatistics.setOptions(options);
	            return  rank.getStatistics(getStatistics).get_return();
	        	
			} catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de usuarios están bloqueados preventivamente luego de multiples errores de conexión");
		
    }
    
    
    public void removeUserFromStatistics(TfsUserStatsOptions options) throws RemoteException
    {

    	if (!isBlocked()) {
	        try {
	        	
	        	
	            TfsRankingUsersStub rank = new TfsRankingUsersStub();
	            RemoveStatistics removeStatistics32 = new RemoveStatistics();
	            removeStatistics32.setOptions(options);
	        	rank.removeStatistics(removeStatistics32);
	        	
	        } catch (RemoteException e) {
				manageError();
				throw e;
			}
		}
		else throw new BlockedException("Los rankings de usuarios están bloqueados preventivamente luego de multiples errores de conexión");
	
		
    }

}
