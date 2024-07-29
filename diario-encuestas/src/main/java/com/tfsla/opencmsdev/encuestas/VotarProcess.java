package com.tfsla.opencmsdev.encuestas;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opencms.file.CmsObject;
import com.tfsla.workflow.QueryBuilder;

public class VotarProcess extends ConfigurableTableProcess {

	public void execute(CmsObject cms, Encuesta encuesta, List<String> respuestas, String Username) {
		int encuestaID = encuesta.getIdEncuesta();

		for (Iterator iter = respuestas.iterator(); iter.hasNext();) {
			int nroRespuesta = Integer.valueOf((String) iter.next()).intValue();

			String updateVotosSQL = "UPDATE " + getRespuestaEncuestaTableName(cms) + " " + "SET " + CANT_VOTOS + " = ("
				+ CANT_VOTOS + " + 1) " + "WHERE " + ID_ENCUESTA + " = " + encuestaID + " AND " + NRO_RESPUESTA
				+ " = " + nroRespuesta;

			new QueryBuilder<String>(cms).setSQLQuery(updateVotosSQL).execute();
		}
		
		String registraVotoSql = "";
		
		if(Username!=null){
			
			// Registro la IP que hizo la votacion
			//String RemoteIp  = getRemoteAddress(cms.getRequestContext().getRemoteAddress());
			String RemoteIp  = com.tfsla.opencmsdev.encuestas.YaVotoIP_Process.getRemoteIP(cms.getRequestContext().getRemoteAddress());
			int VotosYaEmitidosPorUsuario = YaVotoUsuario_Process.getEncuestaVotosxUsuario(cms, encuestaID, Username);
			
			int NuevoVoto = VotosYaEmitidosPorUsuario +1;
			
			long horaActual = new Date().getTime();
			
			if(VotosYaEmitidosPorUsuario>=0 ){
				registraVotoSql = "UPDATE "+TABLA_ENCUESTA_VOTOS+" SET "+CANT_VOTOS_IP +"= '"+NuevoVoto +"'  WHERE " + ID_ENCUESTA + " = " + encuestaID + " AND  "+ VOTO_USUARIO +"='"+ Username +"' ";
			}else{
				registraVotoSql = "INSERT INTO "+TABLA_ENCUESTA_VOTOS+"("+ID_ENCUESTA+", "+REMOTE_IP+", "+FECHA_ULTIMA_VOTACION+", "+CANT_VOTOS_IP+","+VOTO_USUARIO+") VALUES('"+encuestaID+"','"+RemoteIp+"','"+horaActual+"','1','"+Username+"')";
			}
			
		}else{
			// Registro la IP que hizo la votacion
			//String RemoteIp  = getRemoteAddress(cms.getRequestContext().getRemoteAddress());
			String RemoteIp  = com.tfsla.opencmsdev.encuestas.YaVotoIP_Process.getRemoteIP(cms.getRequestContext().getRemoteAddress());
			
			int VotosYaEmitidosPorIP = YaVotoIP_Process.getEncuestaIPVotos(cms, encuestaID ,RemoteIp);
			
			int NuevoVoto = VotosYaEmitidosPorIP +1;
			
			long horaActual = new Date().getTime();
			
			if(VotosYaEmitidosPorIP>=0){
				registraVotoSql = "UPDATE "+TABLA_ENCUESTA_VOTOS+" SET "+CANT_VOTOS_IP +"= '"+NuevoVoto +"'  WHERE " + ID_ENCUESTA + " = " + encuestaID + " AND  "+ REMOTE_IP +"='"+ RemoteIp +"' ";
			}else{
				registraVotoSql = "INSERT INTO "+TABLA_ENCUESTA_VOTOS+"("+ID_ENCUESTA+", "+REMOTE_IP+", "+FECHA_ULTIMA_VOTACION+", "+CANT_VOTOS_IP+") VALUES('"+encuestaID+"','"+RemoteIp+"','"+horaActual+"','1')";
			}
		}
		
		
		new QueryBuilder<String>(cms).setSQLQuery(registraVotoSql).execute();
	}
	
	@Deprecated
	public void execute(CmsObject cms, String encuestaURL, List<String> respuestas, String Username) {
		int encuestaID = this.isOnlineProject(cms)
		? ModuloEncuestas.getEncuestaIDFromOnline(cms, encuestaURL)
		: ModuloEncuestas.getEncuestaID(cms, encuestaURL);

		for (Iterator iter = respuestas.iterator(); iter.hasNext();) {
			int nroRespuesta = Integer.valueOf((String) iter.next()).intValue();

			String updateVotosSQL = "UPDATE " + getRespuestaEncuestaTableName(cms) + " " + "SET " + CANT_VOTOS + " = ("
				+ CANT_VOTOS + " + 1) " + "WHERE " + ID_ENCUESTA + " = " + encuestaID + " AND " + NRO_RESPUESTA
				+ " = " + nroRespuesta;

			new QueryBuilder<String>(cms).setSQLQuery(updateVotosSQL).execute();
		}
		
		String registraVotoSql = "";
		
		if(Username!=null){
			
			// Registro la IP que hizo la votacion
			//String RemoteIp  = getRemoteAddress(cms.getRequestContext().getRemoteAddress());
			String RemoteIp  = com.tfsla.opencmsdev.encuestas.YaVotoIP_Process.getRemoteIP(cms.getRequestContext().getRemoteAddress());
			int VotosYaEmitidosPorUsuario = YaVotoUsuario_Process.getEncuestaVotosxUsuario(cms, encuestaID, Username);
			
			int NuevoVoto = VotosYaEmitidosPorUsuario +1;
			
			long horaActual = new Date().getTime();
			
			if(VotosYaEmitidosPorUsuario>=0 ){
				registraVotoSql = "UPDATE "+TABLA_ENCUESTA_VOTOS+" SET "+CANT_VOTOS_IP +"= '"+NuevoVoto +"'  WHERE " + ID_ENCUESTA + " = " + encuestaID + " AND  "+ VOTO_USUARIO +"='"+ Username +"' ";
			}else{
				registraVotoSql = "INSERT INTO "+TABLA_ENCUESTA_VOTOS+"("+ID_ENCUESTA+", "+REMOTE_IP+", "+FECHA_ULTIMA_VOTACION+", "+CANT_VOTOS_IP+","+VOTO_USUARIO+") VALUES('"+encuestaID+"','"+RemoteIp+"','"+horaActual+"','1','"+Username+"')";
			}
			
		}else{
			// Registro la IP que hizo la votacion
			//String RemoteIp  = getRemoteAddress(cms.getRequestContext().getRemoteAddress());
			String RemoteIp  = com.tfsla.opencmsdev.encuestas.YaVotoIP_Process.getRemoteIP(cms.getRequestContext().getRemoteAddress());
			
			int VotosYaEmitidosPorIP = YaVotoIP_Process.getEncuestaIPVotos(cms, encuestaID ,RemoteIp);
			
			int NuevoVoto = VotosYaEmitidosPorIP +1;
			
			long horaActual = new Date().getTime();
			
			if(VotosYaEmitidosPorIP>=0){
				registraVotoSql = "UPDATE "+TABLA_ENCUESTA_VOTOS+" SET "+CANT_VOTOS_IP +"= '"+NuevoVoto +"'  WHERE " + ID_ENCUESTA + " = " + encuestaID + " AND  "+ REMOTE_IP +"='"+ RemoteIp +"' ";
			}else{
				registraVotoSql = "INSERT INTO "+TABLA_ENCUESTA_VOTOS+"("+ID_ENCUESTA+", "+REMOTE_IP+", "+FECHA_ULTIMA_VOTACION+", "+CANT_VOTOS_IP+") VALUES('"+encuestaID+"','"+RemoteIp+"','"+horaActual+"','1')";
			}
		}
		
		
		new QueryBuilder<String>(cms).setSQLQuery(registraVotoSql).execute();
	}
	
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

}
