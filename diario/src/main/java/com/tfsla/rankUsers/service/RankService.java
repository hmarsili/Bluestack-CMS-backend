package com.tfsla.rankUsers.service;

import org.apache.commons.logging.Log;
import java.rmi.RemoteException;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;

import com.tfsla.rankUsers.service.RankService;
import com.tfsla.statistics.model.TfsHitUser;
import com.tfsla.statistics.model.TfsUserStatsOptions;

import java.util.*;

public class RankService extends TfsRankingService {


	private static final Log LOG = CmsLog.getLog(RankService.class);

	public final static int COUNTER_NOTASPUBLICADAS = 1;
	public final static int COUNTER_VISITASRECIBIDAS = 2;
	public final static int COUNTER_VALORACIONESRECIBIDAS = 3;
	public final static int COUNTER_RECOMENDACIONESRECIBIDAS = 4;
	public final static int COUNTER_COMENTARIOSRECIBIDOS = 5;
	public final static int COUNTER_COMENTARIOSREALIZADOS = 6;
	public final static int COUNTER_COMENTARIOSRECHAZADOS = 7;

	public final static int COUNTER_CUSTOM1 = 8;
	public final static int COUNTER_CUSTOM2 = 9;
	public final static int COUNTER_CUSTOM3 = 10;
	public final static int COUNTER_CUSTOM4 = 11;
	public final static int COUNTER_CUSTOM5 = 12;
	public final static int COUNTER_CUSTOM6 = 13;
	public final static int COUNTER_CUSTOM7 = 14;
	public final static int COUNTER_CUSTOM8 = 15;
	public final static int COUNTER_CUSTOM9 = 16;
	public final static int COUNTER_CUSTOM10 = 17;

	
	public void addUserHit(CmsUser user,CmsObject cms, int counter, int value)
	{
		TfsHitUser hitUser = fillTfsHitUser(user, cms);

		switch (counter)
		{
		case COUNTER_NOTASPUBLICADAS:
			hitUser.setNotasPublicadas(value);
			break;
		case COUNTER_VISITASRECIBIDAS:
			hitUser.setVisitasRecibidas(value);
			break;
		case COUNTER_VALORACIONESRECIBIDAS:
			hitUser.setValoracionesRecibidas(value);
			hitUser.setCantidadValoraciones(1);
			break;
		case COUNTER_RECOMENDACIONESRECIBIDAS:
			hitUser.setRecomendacionesRecibidas(value);
			break;
		case COUNTER_COMENTARIOSRECIBIDOS:
			hitUser.setComentariosRecibidos(value);
			break;
		case COUNTER_COMENTARIOSREALIZADOS:
			hitUser.setComentariosRealizados(value);
			break;
		case COUNTER_COMENTARIOSRECHAZADOS:
			hitUser.setComentariosRechazados(value);
			break;
		case COUNTER_CUSTOM1:
			hitUser.setCustom1(value);
			break;
		case COUNTER_CUSTOM2:
			hitUser.setCustom2(value);
			break;
		case COUNTER_CUSTOM3:
			hitUser.setCustom3(value);
			break;
		case COUNTER_CUSTOM4:
			hitUser.setCustom4(value);
			break;
		case COUNTER_CUSTOM5:
			hitUser.setCustom5(value);
			break;
		case COUNTER_CUSTOM6:
			hitUser.setCustom6(value);
			break;
		case COUNTER_CUSTOM7:
			hitUser.setCustom7(value);
			break;
		case COUNTER_CUSTOM8:
			hitUser.setCustom8(value);
			break;
		case COUNTER_CUSTOM9:
			hitUser.setCustom9(value);
			break;
		case COUNTER_CUSTOM10:
			hitUser.setCustom10(value);
			break;
		}

        try {
        	this.addHit(hitUser);
		} catch (RemoteException e) {
			LOG.fatal("Error al intentar registrar la visita a una pagina del usuario", e);
			e.printStackTrace();
		}

	}

	public void addUserHitCustom(CmsUser user,CmsObject cms, int nro)
	{
		TfsHitUser hitUser = fillTfsHitUser(user, cms);
		switch (nro)
		{
		case 1:
			hitUser.setCustom1(1);
			break;
		case 2:
			hitUser.setCustom2(1);
			break;
		case 3:
			hitUser.setCustom3(1);
			break;
		case 4:
			hitUser.setCustom4(1);
			break;
		case 5:
			hitUser.setCustom5(1);
			break;
		case 6:
			hitUser.setCustom6(1);
			break;
		case 7:
			hitUser.setCustom7(1);
			break;
		case 8:
			hitUser.setCustom8(1);
			break;
		case 9:
			hitUser.setCustom9(1);
			break;
		case 10:
			hitUser.setCustom10(1);
			break;
		}
	
        try {
        	
            this.addHit(hitUser);

		} catch (RemoteException e) {
			LOG.fatal("Error al intentar registrar el contador custom " + nro + " del usuario", e);
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private TfsHitUser fillTfsHitUser(CmsUser user, CmsObject cms) {
		TfsHitUser hitUser = new TfsHitUser();
		hitUser.setUsuario(user.getId().getStringValue());
		
		String sitePath = getSiteName(cms);
		String siteName = sitePath.replaceFirst("/sites/", "");

		hitUser.setSitio(siteName);
		
		hitUser.setOu(user.getOuFqn());
		try {
			
			
			List<CmsGroup> groups = cms.getGroupsOfUser(user.getName(),true);
			
			if (groups.size()>0)
			{
				String[] grupos = new String[groups.size()];
				int j=0;
				for (CmsGroup group : groups)
				{
					grupos[j] = group.getId().getStringValue();
					j++;
				}
				hitUser.setGrupos(grupos);
			}
		} catch (CmsException e) {
			LOG.fatal("Error al intentar obtener la informacion del usuario", e);
			e.printStackTrace();
		}
		
		return hitUser;
	}

    
    public void removeUserFromStatistics(CmsUser user, CmsObject cms)
    {

    	TfsUserStatsOptions options = new TfsUserStatsOptions();
        try {
        	
        	options.setUsuario(user.getId().getStringValue());
        	options.setOu(user.getOuFqn());

        	String sitePath = getSiteName(cms);
        	String siteName = sitePath.replaceFirst("/sites/", "");

        	options.setSitio(siteName);

        	this.removeUserFromStatistics(options);
            
		} catch (RemoteException e) {
			LOG.fatal("Error al eliminar un usuario de las estadisticas ",e);
			e.printStackTrace();
		}
		
    }
    
    public void removeUserFromStatistics(String userId)
    {

    	TfsUserStatsOptions options = new TfsUserStatsOptions();
        try {
        	
        	options.setUsuario(userId);
        	
        	removeUserFromStatistics(options);
        	
		} catch (RemoteException e) {
			LOG.fatal("Error al eliminar un usuario de las estadisticas ",e);
			e.printStackTrace();
		}
		
    }

    private String getSiteName(CmsObject cms)
	{
		CmsSite site = OpenCms.getSiteManager().getCurrentSite(cms);
		String siteName = site.getSiteRoot(); 
		return siteName;
	}

}
