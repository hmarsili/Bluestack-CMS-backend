package com.tfsla.statistics;

import javax.servlet.*; 
import javax.servlet.http.*; 

import org.apache.log4j.Logger;


public class TfsStatsServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5823467009131343147L;

	private MonitorThread monitor = null;
	
	static Logger LOG = Logger.getLogger(TfsStatsServlet.class);

	public void init(ServletConfig config) throws ServletException {
		super.init(config); 
		
	    String sMinutes = getInitParameter("minutes");

	    int minutes = Integer.parseInt(sMinutes);

	    LOG.info("Monitor de Conexiones : Iniciando.");
	    HttpConnectionManager.getInstance();
	    monitor = new MonitorThread(minutes);
	}
	
	public void destroy() { 
	    LOG.info("Monitor de Conexion : Terminando.");
	    monitor.setExecute(false);
	    HttpConnectionManager.getInstance().shutdown();
		//shut down the scheduler 
	}
	
	class MonitorThread extends Thread {
		
		boolean execute = true;
		int minutes;
		
	    public MonitorThread(int minutes) {
	    	super("Monitor de Conexiones");	
	    	this.minutes = minutes;
	    }
	    
	    public void run() {
	    	while (execute) {
	    		try {
					sleep(minutes * 60 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		if (execute)
	    			HttpConnectionManager.getInstance().clean();
	    	}
	    	
	    }

		public void setExecute(boolean execute) {
			this.execute = execute;
		}
	}
}
