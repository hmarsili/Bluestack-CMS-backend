package com.tfsla.diario.auditActions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;

import com.tfsla.diario.auditActions.data.TfsAuditActionDAO;
import com.tfsla.diario.auditActions.data.TfsAuditAttachmentDAO;
import com.tfsla.diario.auditActions.data.TfsAuditCommentDAO;

public class TfsAuditActionPurgeJob   implements I_CmsScheduledJob {

	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		String resultados = "Purgando eventos de dashboard anteriores a ";

		String range = (String) parameters.get("range");
		Date to = parseDateTime(range);
		
		
		resultados += " " + to.toString();
		TfsAuditAttachmentDAO aaDAO = new TfsAuditAttachmentDAO();
		TfsAuditCommentDAO acDAO = new TfsAuditCommentDAO();
		TfsAuditActionDAO aDAO = new TfsAuditActionDAO();
		
		aaDAO.purgeAttachmentEvent(to);
		acDAO.purgeCommentAuditEvent(to);
		aDAO.purgeActions(to);
		
		return resultados;
	}
	
	
	
	protected Date parseDateTime(String value) {
		if (value==null)
			return null;
		
		if (value.matches("\\d{8}"))
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		if (value.matches("\\d{8}\\s\\d{4}"))
		{
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd hhmm");
			try {
				return sdf.parse(value);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	
		if (value.matches("\\d+d"))
		{
			value = value.replace("d", "");
			int days = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.DAY_OF_YEAR, -1* days);
			return cal.getTime();
		}
	
		if (value.matches("\\d+M"))
		{
			value = value.replace("M", "");
			int month = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.MONTH, -1* month);
			return cal.getTime();
		}
	
		if (value.matches("\\d+y"))
		{
			value = value.replace("y", "");
			int year = Integer.parseInt(value);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.YEAR, -1* year);
			return cal.getTime();
		}
	
		return null;
	}


}
