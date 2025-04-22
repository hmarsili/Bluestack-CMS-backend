package com.tfsla.cmsMedios.releaseManager.github.service;

import net.sf.json.JSONObject;

public class GithubFilesValidator {
	
	public static Boolean validate(JSONObject githubFile) {
		try {
			for(String validation : validations) {
				String fileName = "";
				if(githubFile.containsKey("filename")) {
					fileName = githubFile.getString("filename");
				} else {
					fileName = githubFile.getString("name");
				}
				if(fileName.matches(validation)) return false;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	private static final String[] validations = {""};
	//private static final String[] validations = {"^.*noticia\\.xsd.*$"};
}
