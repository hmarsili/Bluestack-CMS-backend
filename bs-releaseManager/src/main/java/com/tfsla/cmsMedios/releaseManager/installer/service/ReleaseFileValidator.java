package com.tfsla.cmsMedios.releaseManager.installer.service;

import java.io.File;

import com.tfsla.cmsMedios.releaseManager.installer.common.exceptions.ReleaseValidationException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ReleaseFileValidator {
	
	public static synchronized void validate(String releaseDir, JSONObject manifest) throws ReleaseValidationException {
		if(!releaseDir.endsWith("/")) {
			releaseDir += "/";
		}
		
		if(!manifest.containsKey("rm")) {
			throw new ReleaseValidationException("The release name is not specified in the manifest");
		}
		
		//Validate config files
		validateElements(manifest, "config", releaseDir);
		
		//Validate admin files (mostly JSPs)
		if(manifest.containsKey("files")) {
			JSONObject files = manifest.getJSONObject("files");
			validateElements(files, "added", releaseDir + "files/");
			validateElements(files, "modified", releaseDir + "files/");
		}
		
		//Validate MySQL scripts
		validateElements(manifest, "scripts", releaseDir);
		
		//Validate JARs files
		validateElements(manifest, "jars", releaseDir, "jars/");
	}
	
	private static void validateElements(JSONObject manifest, String elements, String releaseDir) throws ReleaseValidationException {
		validateElements(manifest, elements, releaseDir, null);
	}
	
	private static void validateElements(JSONObject manifest, String elements, String releaseDir, String dir) throws ReleaseValidationException {
		if(manifest.containsKey(elements)) {
			if(dir == null || dir.equals("")) {
				dir = elements + "/";
			}
			JSONArray files = manifest.getJSONArray(elements);
			for(Object file : files) {
				String fileDir = releaseDir + dir + file.toString();
				File f = new File(fileDir);
				if(!f.exists()) {
					throw new ReleaseValidationException("Cannot find file " + fileDir);
				}
			}
		}
	}
}
