package com.tfsla.diario.admin.jobs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsLog;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.diario.auditActions.service.hangout.HangoutNotificationSender;
import com.tfsla.diario.auditActions.service.slack.SlackNotificationSender;
import com.tfsla.diario.auditActions.service.telegram.TelegramNotificationSender;
import com.tfsla.opencms.exceptions.ProgramException;
import com.tfsla.workflow.QueryBuilder;
import com.tfsla.workflow.ResultSetProcessor;

public class TfsWebhooksNotificationsJob implements I_CmsScheduledJob {
	
	private static final Log LOG = CmsLog.getLog(TfsWebhooksNotificationsJob.class);
	CmsMessages messages;
	
	private CmsObject m_cms;
	
	private static final String TFS_WEBHOOKS_QUEUE = "TFS_WEBHOOKS_QUEUE";
    
    private static final String DB_SITE         = "SITE";
    private static final String DB_PUBLICATION  = "PUBLICATION";
    private static final String DB_PATH         = "PATH";
    private static final String DB_PUBLISH_DATE = "PUBLISH_DATE";
    private static final String DB_TEXT         = "TEXT";

	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		
		m_cms = cms;
		
		// Con el site y la publicacion traigo los registros 
		// Registros con publish_date menor a fecha actual - los  minutos (minutesDelay) pasados por parametro
		
		int publication = 0;
		String _publication = (String)parameters.get("publication");

		if (_publication!=null)
			publication = Integer.parseInt(_publication);
		
		String site =  cms.getRequestContext().getSiteRoot();
		
		int minutesDelay = 0;
		String _minutesDelay = (String)parameters.get("minutesDelay");
	
		if(_minutesDelay!=null)
			minutesDelay = Integer.parseInt(_minutesDelay);
		
		
		HashMap <String, String> msgs =  getListMsgs(site,  publication, minutesDelay);
		
		for (Map.Entry<String, String> entry : msgs.entrySet()) {
			
		    String path = entry.getKey(); 
		    String text = entry.getValue();
		    
		    TelegramNotificationSender telegramSender = new TelegramNotificationSender(site, publication);
		    telegramSender.sendMessage(text);
		    
		    SlackNotificationSender slackSender =  new SlackNotificationSender(site, publication);
		    slackSender.sendMessage(text);
		    
		    HangoutNotificationSender hangoutSenter = new HangoutNotificationSender(site, publication); 
			hangoutSenter.sendMessage(text);
			
			deleteByPath(path);
		
		}
		
		return null;
	}
	
	public HashMap<String, String> getListMsgs(String site, int publication, int minutesDelay)throws Exception{
		
		Calendar calendar = Calendar.getInstance();
        
        Date dateNow = calendar.getTime(); 
        Timestamp dateNowTs = new Timestamp(dateNow.getTime());
        
        calendar.setTime(dateNowTs);  
        calendar.add(Calendar.MINUTE,   -minutesDelay);  
        
        Date dateDelay = calendar.getTime(); 
		
        HashMap <String, String> listMsgs = new HashMap<String, String>();
		
		QueryBuilder <HashMap<String, String>> queryBuilder = new QueryBuilder<HashMap<String, String>>(m_cms);
		
		queryBuilder.setSQLQuery("SELECT "+DB_PATH+","+DB_TEXT+" from "+TFS_WEBHOOKS_QUEUE+" WHERE "+DB_SITE+"=? AND "+DB_PUBLICATION+"=? AND "+DB_PUBLISH_DATE+"<? ");
		
		queryBuilder.addParameter(site);
		queryBuilder.addParameter(publication);
		queryBuilder.addParameter(dateDelay);
		
		ResultSetProcessor<HashMap<String, String>> proc = new ResultSetProcessor<HashMap<String, String>>() {

			private HashMap <String, String> msgs = new HashMap <String, String>();

			public void processTuple(ResultSet rs) {

				try {
					this.msgs.put(rs.getString(DB_PATH),rs.getString(DB_TEXT));
				}
				catch (SQLException e) {
					throw ProgramException.wrap(
							"error al intentar obtener la lista de noticias para notificar", e);
				}
			}

			public HashMap <String, String> getResult() {
				return this.msgs;
			}
		};
		
		listMsgs = queryBuilder.execute(proc);
		
		return listMsgs;
	}
	
	private void deleteByPath(String path) {
		QueryBuilder queryBuilder = new QueryBuilder(m_cms);
		queryBuilder.setSQLQuery("DELETE FROM "+TFS_WEBHOOKS_QUEUE+" WHERE "+DB_PATH+"=? ");
		queryBuilder.addParameter(path);
		
		queryBuilder.execute();	
	}

}
