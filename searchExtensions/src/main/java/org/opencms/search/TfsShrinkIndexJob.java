package org.opencms.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;

public class TfsShrinkIndexJob  implements I_CmsScheduledJob {

	private String resultados = "";
	
	@Override
	public String launch(CmsObject cms, Map parameters) throws Exception {
		
		
		String condition = (String) parameters.get("condition");
		String joinCondition = (String) parameters.get("joincondition");
		String range = (String) parameters.get("range");
		String indexName = (String) parameters.get("index");
		String _commit = (String) parameters.get("commit");
		String _merge = (String) parameters.get("merge");
		
		resultados = "Ejecutando reduccion de indice " + indexName;
		
		
		if (joinCondition==null || joinCondition.trim().equals(""))
			joinCondition = " OR ";
		
		boolean commit=true;
		if (_commit!=null)
			commit = Boolean.parseBoolean(_commit);
		
		boolean merge=true;
		if (_merge!=null)
			merge = Boolean.parseBoolean(_merge);
		
		
		resultados += "( commit: " + commit + " - merge: " + merge + ")";
		
		String parsedRange = parseRange(range);
		if ((condition==null || condition.trim().equals("")) 
				&& 
			(range==null || range.trim().equals("")))
			return resultados + " - SIN EFECTO: ausencia de condicion y/o rango";
		
		if (condition==null ) 
			condition="";
		
		if (!parsedRange.equals("")) {
			if (!condition.trim().equals("")) {
				condition += joinCondition;
			}
			condition += parsedRange;
		}
				
		ExtendedIndexManager eindexManager = new ExtendedIndexManager();
		eindexManager.removeResourcesFromIndex(indexName, condition, commit,merge);
		
		return resultados;
	}

	protected String parseRange(String range) {
		if (range==null)
			return "";
		
		Date hasta = parseDateTime(range);
		Date desde = parseDateTime("19400101");
		
		String to = "" + hasta.getTime();
		String from = "" + desde.getTime();
		
		return getRangeQueryClause(from,to,"ultimaModificacion");
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
	
	protected String getRangeQueryClause(String from, String to, String categoryName) {
		if (from!=null || to!=null) {
			return categoryName + ":[" + from + " TO " + to + "]";
		}

		return "";
	}

}
