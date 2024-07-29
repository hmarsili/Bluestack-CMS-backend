package org.opencms.util.imageCompression;

import java.util.ArrayList;

import org.opencms.util.A_CmsBashProcess;

import com.alkacon.simapi.Simapi;

public class PngquantBashProcess extends A_CmsBashProcess  implements I_OptimizationProcess{

	public static int DEFAULT_QUALITY = 80;
	
	protected int quality = DEFAULT_QUALITY;
	
	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
		
		this.commandLine = new ArrayList<String>();
		this.commandLine.add("pngquant");
		this.commandLine.add("--quality=" + (quality-10) + "-" +quality);
		this.commandLine.add("-");
	}


	public PngquantBashProcess() {
		this.commandLine = new ArrayList<String>();
		this.commandLine.add("pngquant");
		this.commandLine.add("--quality=" + (quality-10) + "-" +quality);
		this.commandLine.add("-");
	}
	

	@Override
	public boolean useProcessor(String extension) {
		return extension.equals(Simapi.TYPE_PNG);
	}
	
	
}
