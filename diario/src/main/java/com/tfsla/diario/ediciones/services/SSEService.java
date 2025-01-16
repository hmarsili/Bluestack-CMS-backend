package com.tfsla.diario.ediciones.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import org.opencms.util.CmsUUID;

import com.tfsla.diario.ediciones.model.ServerEvent;

import java.util.List;

public class SSEService {

	
	public static final String ALLUSERS = "@all";
	private static final int retry = 1000;
	private static final int UserTtl = 10*60*1000;
	private static final int EventTtl = 2*60*1000;
	
	private static SSEService instance = new SSEService();
	
	public static SSEService getInstance() {
		return instance;
	}
	
	private SSEService() {
		userEvents = new HashMap<String,PriorityQueue<ServerEvent>>();
		//eventsQueue = new PriorityQueue<ServerEvent>(100,new EventTimestampComparator());
		lastInteractions = new HashMap<String,Long>();
		
		
		new Thread(new Runnable() {
		     @Override
		     public void run() {
		    	 try {
		    		 synchronized (this) {
		    			 wait(30*1000);
			    		
		    			 purgeInactiveUsers();
						
		    		 }
				} catch (InterruptedException e) {
					System.out.println(e);
				}
		     }
		}).start();
	}
	
	private Map<String,PriorityQueue<ServerEvent>> userEvents;
	//private PriorityQueue<ServerEvent> eventsQueue;
	private Map<String,Long> lastInteractions; 
	
	public synchronized void subscribe(String userName) {
		long lastInteration = new Date().getTime(); 
		PriorityQueue<ServerEvent> userList = userEvents.get(userName);
		if (userList==null) {
			userList = new PriorityQueue<ServerEvent>(100,new EventTimestampComparator());
			userEvents.put(userName, userList);
		}
		lastInteractions.put(userName, lastInteration);
	}
	
	public synchronized void broadcast(String eventName, String data) {
		
		Iterator<String> users = userEvents.keySet().iterator();
		while (users.hasNext()) {
			String userName = users.next();
			ServerEvent event = new ServerEvent.Builder().setUser(userName).setEvent(eventName).setData(data).setId(new CmsUUID().getStringValue()).setRetry(retry).build();
			PriorityQueue<ServerEvent> userList = userEvents.get(userName);
			if (userList!=null)
				userList.add(event);
		}
		
		
	}
	
	public synchronized void addEvent(String eventName, String data, String userName) {
		
		PriorityQueue<ServerEvent> userList = userEvents.get(userName);
		if (userList==null)
			return;
		
		ServerEvent event = new ServerEvent.Builder().setUser(userName).setEvent(eventName).setData(data).setId(new CmsUUID().getStringValue()).setRetry(retry).build();
		
		userList.add(event);
		//eventsQueue.add(event);
	}
	
	public synchronized List<ServerEvent> getEvents(String userName, int maxEvents) {
		long now = new Date().getTime();
		List<ServerEvent> events = new ArrayList<ServerEvent>();
		int nro=0;
		while (nro<maxEvents && userEvents.get(userName).size()>0) {
			ServerEvent event = userEvents.get(userName).poll();
			if ((now -event.getTimestamp())<= EventTtl) {
				events.add(event);
				nro++;
			}
		}
		return events;
	}
	
	public synchronized void updateLastInteracion(String userName) {
		long lastInteration = new Date().getTime(); 
		PriorityQueue<ServerEvent> userList = userEvents.get(userName);
		if (userList==null) {
			userList = new PriorityQueue<ServerEvent>(100,new EventTimestampComparator());
			userEvents.put(userName, userList);
		}
		lastInteractions.put(userName, lastInteration);
	}
	
	public synchronized void putBackEvents(String userName, List<ServerEvent> events) {
		if (events!=null)
			userEvents.get(userName).addAll(events);
	}
	
	private class EventTimestampComparator implements Comparator<ServerEvent> {
	    @Override
	    public int compare(ServerEvent x, ServerEvent y) {
	    	
	    	return (int)(x.getTimestamp() - y.getTimestamp());
	    }
	}
	
	
	public synchronized void purgeInactiveUsers() {
		long now = new Date().getTime();
		
		Iterator<String> users = lastInteractions.keySet().iterator();
		while (users.hasNext()) {
			String user = users.next();
			
			long lastInteraction = lastInteractions.get(user);
			if ((now - lastInteraction) > UserTtl) {
				userEvents.remove(user);
				users.remove();
			}
		}
		
	}
	
	/*
	public synchronized void purgeOldEvents(long timeElapsed) {
		
		long now = new Date().getTime();
		
		ServerEvent nextEvent = eventsQueue.peek();
		
		boolean proceed = nextEvent!=null && ((now-nextEvent.getTimestamp())>timeElapsed);
		while (proceed) {
			nextEvent = eventsQueue.poll();
			userEvents.get(nextEvent.getUser()).remove(nextEvent);
			
			nextEvent = eventsQueue.peek();
			proceed = nextEvent!=null && ((now-nextEvent.getTimestamp())>timeElapsed);
		}
		
	}
	*/
	
}
