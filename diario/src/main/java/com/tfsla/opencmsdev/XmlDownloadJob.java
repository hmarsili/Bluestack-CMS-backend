package com.tfsla.opencmsdev;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.scheduler.I_CmsScheduledJob;

public class XmlDownloadJob implements I_CmsScheduledJob {
	
	public void download(CmsObject object, String readPath, String writePath, StringBuffer log) {
		try {
			byte[] html = readUrl(readPath);
			write(html, object, writePath);
		} catch (Exception e) {
			String msg = "No se pudo ejecutar el download del archivo[readFrom: " + readPath + ", writeFrom: " + writePath + "]";
			CmsLog.getLog(this).error(msg, e);
			log.append(msg + " a causa de : " + e.getMessage());
		}
	}

	private void write(byte[] html, CmsObject object, String writePath) {
        try {
            CmsFile resource = getResource(object, writePath);
            resource.setContents(html);
			object.writeFile(resource);
			object.unlockResource(writePath);
			OpenCms.getPublishManager().publishResource(object,writePath);
		}
		catch (Exception e) {
            CmsLog.getLog(this).error("No se pudo guardar el archivo.", e);
		}
	}

	private CmsFile getResource(CmsObject object, String writePath) {
		CmsFile resource = null;
		try {
			resource = object.readFile(writePath);
			object.lockResource(writePath);
		}
		catch (CmsException e) {
			try {
				resource = new CmsFile(object.createResource(writePath, 1));
			}
			catch (CmsIllegalArgumentException e1) {
                CmsLog.getLog(this).error("No se pudo obtener el recurso.", e1);
			}
			catch (CmsException e2) {
                CmsLog.getLog(this).error("No se pudo obtener el recurso.", e2);
			}
		}
		return resource;
	}

	private byte[] readUrl(String url) throws MalformedURLException,
			IOException {
		
		List<Byte> list = new ArrayList<Byte>();
		
		///////
		URL direccion = new URL(url);

		// lees
		BufferedReader br = new BufferedReader(new InputStreamReader(direccion
				.openStream()));

		
		   String line;
		   while ((line = br.readLine()) != null){ 
		       for(char charr : line.toCharArray()) {
		    	   list.add(new Byte((byte)charr));
		       }
	    	   list.add(new Byte((byte)'\n'));
		   } 
		   br.close(); 
		   
		byte[] bytes = new byte[list.size()];
		int i = 0;
		for(Iterator<Byte> iter = list.iterator(); iter.hasNext();) {
			bytes[i++] = iter.next();
		}
		return bytes;
	}

	public String launch(CmsObject cms, Map parameters) throws Exception {
		StringBuffer sb = new StringBuffer();
		for(Object entryObject : parameters.entrySet() ) {
			Map.Entry entry = (Entry) entryObject;
	 		sb.append("bajando :" +entry.getValue() + " en " + entry.getKey());
	 		this.download(cms, (String)entry.getValue(), (String)entry.getKey(), sb);
		}
		sb.append("Finalizado");		
		return sb.toString();
	}

}

