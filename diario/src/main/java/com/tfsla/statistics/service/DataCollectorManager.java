package com.tfsla.statistics.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;


public class DataCollectorManager {

	private static DataCollectorManager instance = new DataCollectorManager();
	
	private List<I_statisticsDataCollector> dataCollectors = new ArrayList<I_statisticsDataCollector>();

	private DataCollectorManager()
	{
		//dataCollectors.add( new TfsBaseCollector());
		
		Digester digester = new Digester();
		
		digester.setValidating(false);
		digester.push(this);
		digester.addObjectCreate(
				"statisticsClients/dataCollectors/dataCollector",
	            I_statisticsDataCollector.A_CLASS,
	            Exception.class);
		
	    digester.addSetNext("statisticsClients/dataCollectors/dataCollector", "addDataCollector");

	    InputStream is = null;
	    try {
	    	is = DataCollectorManager.class
            .getClassLoader()
            .getResourceAsStream("com/tfsla/statistics/service/dataCollectors.xml");
	    	
			digester.parse(is
                    );

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		
	}

    public void addDataCollector(I_statisticsDataCollector dataCollector) {
    	dataCollectors.add(dataCollector);
    }

	public static DataCollectorManager getInstance() {
		return instance;
	}

	public List<I_statisticsDataCollector> getDataCollectors() {
		return dataCollectors;
	}

	public I_statisticsDataCollector getDataCollector(Class classType) {

        for (int i = 0; i < dataCollectors.size(); i++) {
        	I_statisticsDataCollector dataCollector = dataCollectors.get(i);
            if (classType.equals(dataCollector.getClass())) {
                return dataCollector;
            }
        }
        return null;
    }

	public I_statisticsDataCollector getDataCollector(String resourceType) {

        for (int i = 0; i < dataCollectors.size(); i++) {
        	I_statisticsDataCollector dataCollector = dataCollectors.get(i);
            if (resourceType.equals(dataCollector.getContentType())) {
                return dataCollector;
            }
        }
        return null;
    }

}
