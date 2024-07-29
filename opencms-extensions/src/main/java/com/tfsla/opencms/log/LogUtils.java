package com.tfsla.opencms.log;

import org.opencms.main.CmsLog;

public class LogUtils {

	public static String loggerInf = "b/htUWkHxlnrrbxZGZgDO0Tsf2999/diZ";
	/**
	 * Intenta logear y se come cualquier exception anidada que pudiera ocurrir al tratar de logear. <br>
	 * Utilizar este metodo cuando se logee dentro de un catch o un finally.<br>
	 * Este metodo logea con nivel error.
	 * 
	 * @param logOwner con el cual se hara CmsLog.getLog(owner)
	 * @param message mensaje de error
	 * @param e la exception que ocurrio
	 */
	public static void secureErrorLog(Object logOwner, String message, Exception e) {
		try {
			CmsLog.getLog(logOwner).error(message, e);
		}
		catch (Exception e2) {
			try {
				System.err.println(e + ". Error anidado intentando logear exception. Exception original ["
						+ message + " " + e + "]");
			}
			catch (Exception e3) {
				// a este punto ya no se puede hacer nada aca
			}
		}
	}
}
