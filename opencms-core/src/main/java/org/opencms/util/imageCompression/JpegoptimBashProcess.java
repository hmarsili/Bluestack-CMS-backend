package org.opencms.util.imageCompression;

import java.util.ArrayList;

import org.opencms.util.A_CmsBashProcess;

import com.alkacon.simapi.Simapi;

public class JpegoptimBashProcess extends A_CmsBashProcess implements I_OptimizationProcess {

	public static int DEFAULT_QUALITY = 80;
	
	protected int quality = DEFAULT_QUALITY;
	
	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
		
		this.commandLine = new ArrayList<String>();
		this.commandLine.add("jpegoptim");
		this.commandLine.add("-m" +quality);
		this.commandLine.add("--all-progressive");
		this.commandLine.add("--stdout");
		this.commandLine.add("--stdin");
	}

	/*
	  
	 Starting from v1.4.0, jpegoptim supports stdin/stdout. So, now there is couple ways to do what you want:

	jpegoptim --stdout a.jpg > b.jpg
	
	or
	
	cat a.jpg | jpegoptim --stdin > b.jpg
	  
	 */
	public JpegoptimBashProcess() {
		this.commandLine = new ArrayList<String>();
		this.commandLine.add("jpegoptim");
		this.commandLine.add("-m" +quality);
		this.commandLine.add("--all-progressive");
		this.commandLine.add("--stdout");
		this.commandLine.add("--stdin");
		
	}

	@Override
	public boolean useProcessor(String extension) {
		return extension.equals(Simapi.TYPE_JPEG);
	}
	
	
	
}
