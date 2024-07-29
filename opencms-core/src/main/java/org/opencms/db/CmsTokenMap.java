package org.opencms.db;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CmsTokenMap<K,J> extends LinkedHashMap<K, J> {

	private static final long serialVersionUID = -4367915039035704338L;
	
	private long expiryTime = 100000L;
    private long currentOldest = 0L;
    private long maxEntries = 1024;
    LinkedHashMap<K, Long> expires = new LinkedHashMap<K, Long>(); 

    @Override
    public J get(Object key) {
        long currentTime = new Date().getTime();
        //System.out.println("... buscando: " + key);
        if ((currentOldest > 0L) && (currentOldest + expiryTime) > currentTime) {
        	//System.out.println("currentOldest:" + currentOldest + " + " + expiryTime + " < " + currentTime);
        	//System.out.println("diff:" + ((currentOldest + expiryTime) - currentTime));
        	
        	//System.out.println("even the oldest key has not expired.");
            return super.get(key);
        }

        synchronized(this) { 
	        Iterator<java.util.Map.Entry<K, J>> iter = this.entrySet().iterator();
	        while (iter.hasNext()) {
	            Map.Entry<K, J> entry = iter.next(); 
	            if (entry!=null) {
		            Long entryTime = expires.get(entry.getKey());
		            if (entryTime==null) entryTime = 0L;
		            
		            //System.out.println("entryTime:" + entryTime + " + " + expiryTime + " < " + currentTime);
		        	if (currentTime >= entryTime + expiryTime) {
		                iter.remove();
		                expires.remove(entry.getKey());
		            } else {
		                // since this is a linked hash map, order is preserved.
		                // All the elements after the current entry came later.
		                // So no need to check the remaining elements if the current is not expired.
		                currentOldest = entryTime;
		                break;
		            }
	        	}
	        }
        }

        return super.get(key);
    }
    
    @Override
    public J remove(Object key) {
    	//System.out.println(this.size() + "| borrando " + key);
    	expires.remove(key);
    	return super.remove(key);
    }
    
    @Override
    public J put(K key, J value) {
    	//System.out.println(this.size() + ">" + expires.size() + " | put " + key + " at "+ value);
    	expires.put(key,Long.valueOf(new Date().getTime()));
    	return super.put(key,value);
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, J> eldest) {
    	try {
	    	synchronized(this) { 
		    	boolean mustRemoveEldest = (size() > maxEntries);
		    	//System.out.println("mustRemoveEldest " + mustRemoveEldest + " > " + eldest.getKey() + " > " + expires.get(eldest.getKey()));
		    	if (eldest!=null) {
		    		currentOldest = expires.get(eldest.getKey());
		    		if (mustRemoveEldest) expires.remove(eldest.getKey());
		    	}
		        return mustRemoveEldest;
	    	}
    	}
    	catch (NullPointerException ex) {
    		ex.printStackTrace();
    		return false;
    	}
        
    }
    
    @Override
	public void clear() {
		expires.clear();
		super.clear();
	}
    
    public CmsTokenMap(long maxCapacity, long expiryTime) {
    	super();
    	maxEntries = maxCapacity;
    	this.expiryTime = expiryTime;
	}

	
	public static void main(String[] args) {
		CmsTokenMap<String,String> tokens = new CmsTokenMap<>(10L, 4000L); 
		tokens.put("1","Token 1");
		tokens.put("2","Token 2");
		tokens.put("3","Token 3");
		tokens.put("4","Token 4");
		tokens.put("1","Token 1");
		tokens.put("5","Token 5");
		tokens.put("6","Token 6");
		tokens.put("7","Token 7");
		tokens.put("8","Token 8");
		tokens.put("9","Token 9");
		tokens.put("1","Token 1");
		tokens.put("10","Token 10");
		tokens.put("1","Token 1");
		tokens.put("11","Token 11");
		tokens.put("12","Token 12");
		
		
		System.out.println("... buscando: " + 1);
		String str = tokens.get("1");
		if (str!=null)
			System.out.println(str);
		else
			System.out.println("No esta");
		
		
		System.out.println("... Esperando: 2 segundos");
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("... buscando: " + 5);
		str = tokens.get("5");
		if (str!=null)
			System.out.println(str);
		else
			System.out.println("5 - No esta");
		
		System.out.println("... buscando: " + 2);
		str = tokens.get("2");
		if (str!=null)
			System.out.println(str);
		else
			System.out.println("2- No esta");
		
		try {
			TimeUnit.SECONDS.sleep(3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("... buscando: " + 6);
		str = tokens.get("6");
		if (str!=null)
			System.out.println(str);
		else
			System.out.println("6 - No esta");
		
		tokens.put("1","Token 1");
		System.out.println("... buscando: " + 1);
		str = tokens.get("1");
		if (str!=null)
			System.out.println(str);
		else
			System.out.println("1- No esta");
		
    }

	
}
