package com.tfsla.opencmsdev.encuestas;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import com.tfsla.exceptions.ApplicationException;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class YaVotoIP_Process extends AbstractEncuestaProcess  {

	
public boolean execute(HttpServletRequest request,CmsObject cms, Encuesta encuesta) {
		
		int CantidadVotosPermitidos = GetEncuestasProperties.getInstance(cms).getCantVotosXIP();
		String TiempoVotosPorIPConfig = GetEncuestasProperties.getInstance(cms).getTiempXIP();
		
		int IdEncuesta = encuesta.getIdEncuesta();
		
		String RemoteIp  = getRemoteIP(cms.getRequestContext().getRemoteAddress());
		
		int VotosYaEmitidosPorIP = 0;
		String FechaUltimaVotacion = null;		
		
		HashMap <String, String> lastVoteforIp = getPollLastVoteForIP(cms, IdEncuesta,RemoteIp);
		
		if(lastVoteforIp.size()>0){
			VotosYaEmitidosPorIP = Integer.valueOf(lastVoteforIp.get("CANT"));
			FechaUltimaVotacion = lastVoteforIp.get("FECHA_VOTO");		
		}
		
		boolean YaVoto = false;
		
		if(!(FechaUltimaVotacion ==null)){

			  Date horaActual = new Date();
			
			  long lvotacion = Long.parseLong(FechaUltimaVotacion); 
			  long lahora = horaActual.getTime(); 
			
			  long minutos = (lahora - lvotacion)/(1000*60);
			
			  
			  if(VotosYaEmitidosPorIP >= CantidadVotosPermitidos ){
				  
				  if(minutos > Integer.valueOf(TiempoVotosPorIPConfig)){
					  YaVoto = false;
					  
					  // Pongo la cantidad de votos en 0
					  //String registraVotoSql = "UPDATE "+TABLA_ENCUESTA_VOTOS+" SET "+CANT_VOTOS_IP +"= 0,"+ FECHA_ULTIMA_VOTACION +"='"+horaActual.getTime()+"'  WHERE " + ID_ENCUESTA + " = " + IdEncuesta + " AND  "+ REMOTE_IP +"='"+ RemoteIp +"' ";
					  String registraVotoSql = "DELETE FROM "+TABLA_ENCUESTA_VOTOS+"  WHERE " + ID_ENCUESTA + " = " + IdEncuesta + " AND  "+ REMOTE_IP +"='"+ RemoteIp +"' ";
					  new QueryBuilder<String>(cms).setSQLQuery(registraVotoSql).execute();
				  }else{
					  YaVoto = true; 
				  }
				   
			  }else{
				  YaVoto = false;
			  }
			  
		}
		
		return YaVoto;
	}
	
	@Deprecated
	public boolean execute(HttpServletRequest request,CmsObject cms, String encuestaURL) {
		
		int CantidadVotosPermitidos = GetEncuestasProperties.getInstance(cms).getCantVotosXIP();
		String TiempoVotosPorIPConfig = GetEncuestasProperties.getInstance(cms).getTiempXIP();
		
		int IdEncuesta = cms.getRequestContext().currentProject().isOnlineProject()
		? ModuloEncuestas.getEncuestaIDFromOnline(cms, encuestaURL)
		: ModuloEncuestas.getEncuestaID(cms, encuestaURL);
		
		String RemoteIp  = getRemoteIP(cms.getRequestContext().getRemoteAddress());
		
		int VotosYaEmitidosPorIP = 0;
		String FechaUltimaVotacion = null;		
		
		HashMap <String, String> lastVoteforIp = getPollLastVoteForIP(cms, IdEncuesta,RemoteIp);
		
		if(lastVoteforIp.size()>0){
			VotosYaEmitidosPorIP = Integer.valueOf(lastVoteforIp.get("CANT"));
			FechaUltimaVotacion = lastVoteforIp.get("FECHA_VOTO");		
		}
		
		boolean YaVoto = false;
		
		if(!(FechaUltimaVotacion ==null)){

			  Date horaActual = new Date();
			
			  long lvotacion = Long.parseLong(FechaUltimaVotacion); 
			  long lahora = horaActual.getTime(); 
			
			  long minutos = (lahora - lvotacion)/(1000*60);
			
			  
			  if(VotosYaEmitidosPorIP >= CantidadVotosPermitidos ){
				  
				  if(minutos > Integer.valueOf(TiempoVotosPorIPConfig)){
					  YaVoto = false;
					  
					  // Pongo la cantidad de votos en 0
					  //String registraVotoSql = "UPDATE "+TABLA_ENCUESTA_VOTOS+" SET "+CANT_VOTOS_IP +"= 0,"+ FECHA_ULTIMA_VOTACION +"='"+horaActual.getTime()+"'  WHERE " + ID_ENCUESTA + " = " + IdEncuesta + " AND  "+ REMOTE_IP +"='"+ RemoteIp +"' ";
					  String registraVotoSql = "DELETE FROM "+TABLA_ENCUESTA_VOTOS+"  WHERE " + ID_ENCUESTA + " = " + IdEncuesta + " AND  "+ REMOTE_IP +"='"+ RemoteIp +"' ";
					  new QueryBuilder<String>(cms).setSQLQuery(registraVotoSql).execute();
				  }else{
					  YaVoto = true; 
				  }
				   
			  }else{
				  YaVoto = false;
			  }
			  
		}
		
		return YaVoto;
	}
	
    private HashMap<String, String> getPollLastVoteForIP(CmsObject cms, int IdEncuesta, String RemoteIp) {
    	
    	HashMap <String, String> lastVoteforIp = new HashMap<String, String>();
    	
    	QueryBuilder<HashMap<String, String>> queryBuilder = new QueryBuilder<HashMap<String, String>>(cms);
		 
		queryBuilder.setSQLQuery("SELECT " + CANT_VOTOS_IP + "," + FECHA_ULTIMA_VOTACION + " FROM "+ TABLA_ENCUESTA_VOTOS +" WHERE "
				+ ID_ENCUESTA + " =? AND  "+ REMOTE_IP +"=? ");
		
		queryBuilder.addParameter(IdEncuesta);
		queryBuilder.addParameter(RemoteIp);
		
		ResultSetProcessor<HashMap<String, String>> proc = new ResultSetProcessor<HashMap<String, String>>() {

			private HashMap <String, String> lastVoteforIp = new HashMap<String, String>();

			public void processTuple(ResultSet rs) {

				try {
					int cantVotos = new Integer(rs.getInt(CANT_VOTOS_IP));
					this.lastVoteforIp.put("CANT", Integer.toString(cantVotos ));
					this.lastVoteforIp.put("FECHA_VOTO", new String(rs.getString(FECHA_ULTIMA_VOTACION)));
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar recuperar la info del video en cola de la base", e);
				}
			}

			public HashMap <String, String> getResult() {
				return this.lastVoteforIp;
			}
		};
		
		lastVoteforIp = queryBuilder.execute(proc);
		
		return lastVoteforIp;
    	
    }
	
	// ***************************
	// ** Traigo los votos de la encuesta para la IP ingresada
	// ***************************
    @Deprecated
	public static int getEncuestaIPVotos(CmsObject cms, int IdEncuesta, String RemoteIp) {
		String getEncuestaSQL = "SELECT " + CANT_VOTOS_IP + " FROM "+ TABLA_ENCUESTA_VOTOS +" WHERE "
				+ ID_ENCUESTA + " = '" + IdEncuesta + "' AND  "+ REMOTE_IP +"='"+ RemoteIp +"' ";

		 Integer cant_votos_encuesta_por_IP = new QueryBuilder<Integer>(cms).setSQLQuery(getEncuestaSQL).execute(
				new ResultSetProcessor<Integer>() {

					private Integer CantidadVotosPorIP;

					public void processTuple(ResultSet rs) {
						try {
							this.CantidadVotosPorIP = new Integer(rs.getInt(CANT_VOTOS_IP));
						}
						catch (SQLException e) {
							throw new ApplicationException("No se pudo leer la columna " + CANT_VOTOS_IP, e);
						}
					}

					public Integer getResult() {
						return this.CantidadVotosPorIP;
					}
				});
				
				if(cant_votos_encuesta_por_IP == null ){
					cant_votos_encuesta_por_IP=-1;
				}

		return cant_votos_encuesta_por_IP;
	}
	
//	 ***************************
	// ** Traigo la fecha del ultimo voto emitido por la IP
	// ***************************
    @Deprecated
	private static String getEncuestaFechaVotacionIP(CmsObject cms, int IdEncuesta, String RemoteIp) {
		String getEncuestaSQL = "SELECT " + FECHA_ULTIMA_VOTACION + " FROM "+ TABLA_ENCUESTA_VOTOS +" WHERE "
				+ ID_ENCUESTA + " = '" + IdEncuesta + "' AND  "+ REMOTE_IP +"='"+ RemoteIp +"' ";

		String Fecha_votacion_encuesta_por_IP = new QueryBuilder<String>(cms).setSQLQuery(getEncuestaSQL).execute(
				new ResultSetProcessor<String>() {

					private String FechaVotacionPorIP;

					public void processTuple(ResultSet rs) {
						try {
							this.FechaVotacionPorIP = new String(rs.getString(FECHA_ULTIMA_VOTACION));
						}
						catch (SQLException e) {
							throw new ApplicationException("No se pudo leer la columna " + FECHA_ULTIMA_VOTACION, e);
						}
					}

					public String getResult() {
						return this.FechaVotacionPorIP;
					}
				});

		return Fecha_votacion_encuesta_por_IP;
	}
	
    @Deprecated
	public String getRemoteAddress(String remoteAddress){
		  
		String ip = "";
		
		// Expresion regular ip clase A (10.0.0.0 - 10.255.255.255)
		String regexClassA = "(10\\.(([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){2}([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5]))";

		// Expresion regular ip clase B (172.16.0.0 - 172.31.255.255)
		String regexClassB = "(172\\.(1[6-9]|2[0-9]|3[0-1])\\.([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5]))";

		// Expresion regular ip clase C (192.168.0.0 - 192.168.255.255)
		String regexClassC = "(192\\.168\\.([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.([0,1]?[0-9]{1,2}|2[0-4][0-9]|25[0-5]))";

		Pattern pA = Pattern.compile(regexClassA);
		Pattern pB = Pattern.compile(regexClassB);
		Pattern pC = Pattern.compile(regexClassC);
		
		Matcher mA, mB, mC;
		
		String[] remoteAddrArray = remoteAddress.split(",");
	    int                 size = remoteAddrArray.length;
	    
	    for (int a=0;a< size; a++){
	    	 mA = pA.matcher(remoteAddrArray[a]);
	         mB = pB.matcher(remoteAddrArray[a]);
	         mC = pC.matcher(remoteAddrArray[a]);
		
	         if (!mA.find() && !mB.find() && !mC.find()){
	 		    ip = remoteAddrArray[a];
	 		    a = size+1;
	 		}
	    }
	    
	    ip = ip.trim();
		
		return ip;
	}

	public static String getRemoteIP(String remoteAddress){
		String ip = "";
		
		String[] remoteAddrArray = remoteAddress.split(",");
		int size = remoteAddrArray.length;
		
		for(int a=0; a<size;a++){
			
			try{
				String ipStr = remoteAddrArray[a].trim();
				InetAddress address = InetAddress.getByName(ipStr);
				
				if(!address.isSiteLocalAddress())
				{
					ip = ipStr;
					a = size + 1;
				}
				
			}catch( UnknownHostException e){
				//CmsLog.getLog(this).error("Error determinando si la ip es interna en encuestas: "+e.getMessage());
			}
			
		}
		
		return ip;
	}
	
}
