package com.tfsla.cmsMedios.releaseManager.github.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.tfsla.cmsMedios.releaseManager.common.ConnectorConfiguration;
import com.tfsla.cmsMedios.releaseManager.github.common.GitFile;
import com.tfsla.cmsMedios.releaseManager.github.common.GitFileStatus;
import com.tfsla.cmsMedios.releaseManager.github.common.InvalidManifestException;
import com.tfsla.cmsMedios.releaseManager.github.common.Strings;
import com.tfsla.cmsMedios.releaseManager.github.common.interfaces.IGithubTagsService;
import com.tfsla.cmsMedios.releaseManager.github.service.core.GithubService;

public class GithubTagsService extends GithubService implements IGithubTagsService {

	public GithubTagsService(ConnectorConfiguration config) {
		super(config);
	}
	
	@Override
	public List<GitFile> getTagsList() throws IOException {
		this.log.info(Strings.RETRIEVING_TAGS);
		String response = connector.get("/tags");
		
		JSONArray tags = JSONArray.fromObject(response);
		this.log.info(String.format(Strings.TAGS_FOUND, tags.size()));
		
		List<GitFile> files = new ArrayList<GitFile>();
		for(int i=0; i<tags.size(); i++) {
			JSONObject o = tags.getJSONObject(i);
			GitFile gitFile = new GitFile();
			gitFile.setFileName(o.getString("name"));
			gitFile.setRawUrl(o.getString("zipball_url"));
			files.add(gitFile);
		}
		
		this.log.debug(String.format(Strings.SERVICE_RETURNS, files.toString()));
		return files;
	}
	
	@Override
	public List<GitFile> getFilesBetweenTags(String tagName1, String tagName2) throws IOException {
		
		List<GitFile> files = new ArrayList<GitFile>();
		String response = "";
	
		this.log.info(String.format(Strings.RETRIEVING_FILES_BETWEEN_TAGS, tagName1, tagName2));
		
		String serviceCall = String.format("/compare/%s:%s...%s:%s", this.config.getOwner(), tagName1, this.config.getOwner(), tagName2);
		
		
		try {
			response = connector.get(serviceCall);
		} catch(IOException e) {
			System.out.println(e.getMessage());
			this.log.info(String.format(Strings.FILES_FOUND, 0));
			return files;
		}
		
		JSONObject json = JSONObject.fromObject(response);
		JSONArray array = json.getJSONArray("files");
		this.log.info(String.format(Strings.FILES_FOUND, array.size()));
		
		for (int i=0; i<array.size(); i++) {
			JSONObject o = array.getJSONObject(i);
			if (!GithubFilesValidator.validate(o)) continue;
				
			if(o.getString("filename").indexOf("com.tfsla.diario.base/schemas/noticia.xsd")==-1 && o.getString("filename").indexOf("com.tfsla.diario.newsTags/classes/com/tfsla/diario/utils/tags_names.properties")==-1) {
				GitFile gitFile = new GitFile();
				gitFile.setFileName(o.getString("filename"));
				gitFile.setRawUrl(o.getString("raw_url"));
				gitFile.setStatus(GitFileStatus.getByString(o.getString("status")));
				
				System.out.println(o.getString("filename") + " Status: " + o.getString("status"));
				
				if (gitFile.getStatus() == GitFileStatus.RENAMED) {
					gitFile.setPreviousFileName(o.getString("previous_filename"));
				}
				files.add(gitFile);
			}
		}
		
		System.out.println("Cantidad de archivos: "+files.size());
		this.log.debug(String.format(Strings.SERVICE_RETURNS, files.toString()));
		
        HashSet<GitFile> set = new HashSet<>(files);
        files.clear();
        files.addAll(set);

		return files;
	}
	
	private List<GitFile> listFiles(String path, GitFileStatus status) throws IOException {
		
	       String serviceCall = "/contents/"+path;
	      
	       List<GitFile> files = new ArrayList<GitFile>();
		   String response = "";
			try {
				response = connector.get(serviceCall);
			} catch(IOException e) {
				this.log.info(String.format(Strings.FILES_FOUND, 0));
				return files;
			}
			
			if(!response.startsWith("["))
				response = "["+response +"]";
			
			JSONArray array = JSONArray.fromObject(response);
			this.log.info(String.format(Strings.FILES_FOUND, array.size()));
			
			for (int i=0; i<array.size(); i++) {
				JSONObject o = array.getJSONObject(i);
				if (!GithubFilesValidator.validate(o)) continue;
				
				if(o.getString("type").equals("file")) {
					GitFile gitFile = new GitFile();
					gitFile.setFileName(o.getString("path"));
					gitFile.setRawUrl(o.getString("download_url"));
					gitFile.setStatus(status);
					files.add(gitFile);
					
					System.out.println(o.getString("path"));
					
					files.add(gitFile);
				}
				
				if(o.getString("type").equals("dir")) {
					List<GitFile> filesInDir = new ArrayList<GitFile>();
					filesInDir = listFiles(o.getString("path"), status);
					
					files.addAll(filesInDir);
				}
		}
			
		return files;
    }
	
	
	@Override
	public List<GitFile> getTagJars(String tagName) throws IOException {
		this.log.info(String.format(Strings.RETRIEVING_TAG_JARS, tagName));
		List<GitFile> files = new ArrayList<GitFile>();
		String serviceCall = "/contents/cmsMedios/releases/"+tagName+"/lib";
		String response = "";
		try {
			response = connector.get(serviceCall);
		} catch(IOException e) {
			this.log.info(String.format(Strings.JARS_FOUND, 0));
			return files;
		}
		
		JSONArray array = JSONArray.fromObject(response);
		this.log.info(String.format(Strings.JARS_FOUND, array.size()));
		for (int i=0; i<array.size(); i++) {
			JSONObject o = array.getJSONObject(i);
			if(!GithubFilesValidator.validate(o)) continue;
			
			GitFile gitFile = new GitFile();
			gitFile.setFileName(o.getString("name"));
			gitFile.setRawUrl(o.getString("download_url"));
			gitFile.setStatus(GitFileStatus.ADDED);
			files.add(gitFile);
		}
		
		this.log.debug(String.format(Strings.SERVICE_RETURNS, files.toString()));
		return files;
	}
	
	@Override
	public List<GitFile> getTagSQLScripts(String tagName) throws IOException {
		this.log.info(String.format(Strings.RETRIEVING_TAG_SCRIPTS, tagName));
		List<GitFile> files = new ArrayList<GitFile>();
		String serviceCall = "/contents/cmsMedios/releases/"+tagName+"/sql";
		String response = "";
		try {
			response = connector.get(serviceCall);
		} catch(IOException e) {
			this.log.info(String.format(Strings.SCRIPTS_FOUND, 0));
			return files;
		}
		
		JSONArray array = JSONArray.fromObject(response);
		this.log.info(String.format(Strings.SCRIPTS_FOUND, array.size()));
		for (int i=0; i<array.size(); i++) {
			JSONObject o = array.getJSONObject(i);
			GitFile gitFile = new GitFile();
			gitFile.setFileName(o.getString("name"));
			gitFile.setRawUrl(o.getString("download_url"));
			gitFile.setStatus(GitFileStatus.ADDED);
			files.add(gitFile);
		}
		
		this.log.debug(String.format(Strings.SERVICE_RETURNS, files.toString()));
		return files;
	}
	
	@Override
	public List<GitFile> getTagConfigFiles(String tagName) throws IOException {
		this.log.info(String.format(Strings.RETRIEVING_TAG_SCRIPTS, tagName));
		List<GitFile> files = new ArrayList<GitFile>();
		String serviceCall = "/contents/cmsMedios/releases/"+tagName+"/config";
		String response = "";
		try {
			response = connector.get(serviceCall);
		} catch(IOException e) {
			this.log.info(String.format(Strings.XML_CONFIG_FOUND, 0));
			return files;
		}
		
		JSONArray array = JSONArray.fromObject(response);
		this.log.info(String.format(Strings.XML_CONFIG_FOUND, array.size()));
		for (int i=0; i<array.size(); i++) {
			JSONObject o = array.getJSONObject(i);
			GitFile gitFile = new GitFile();
			gitFile.setFileName(o.getString("name"));
			gitFile.setRawUrl(o.getString("download_url"));
			gitFile.setStatus(GitFileStatus.ADDED);
			files.add(gitFile);
		}
		
		this.log.debug(String.format(Strings.SERVICE_RETURNS, files.toString()));
		return files;
	}

	@Override
	public List<GitFile> getTagFiles(String tagName, Boolean requireManifest) throws IOException, InvalidManifestException {
		this.log.info(String.format(Strings.RETRIEVING_TAG_CONFIG_FILES, tagName));
		List<GitFile> files = new ArrayList<GitFile>();
		String serviceCall = "/contents/cmsMedios/releases/"+tagName;
		String response = connector.get(serviceCall);
		
		JSONArray array = JSONArray.fromObject(response);
		Boolean containsManifest = false;
		this.log.info(String.format(Strings.FILES_FOUND, array.size()));
		for (int i=0; i<array.size(); i++) {
			JSONObject o = array.getJSONObject(i);
			if (o.getString("type") == null || o.getString("type").equals("dir") || o.getString("name").toLowerCase().endsWith(".zip")) {
				continue;
			}
			
			String fileName = o.getString("name");
			if(fileName.toLowerCase().contains("readme")) {
				fileName = "readme.txt";
			}
			if(fileName.toLowerCase().contains("manifest.json")) {
				containsManifest = true;
			}
			GitFile gitFile = new GitFile();
			gitFile.setFileName(fileName);
			gitFile.setRawUrl(o.getString("download_url"));
			gitFile.setStatus(GitFileStatus.ADDED);
			files.add(gitFile);
		}
		
		if (!containsManifest && requireManifest) {
			this.log.error(String.format(Strings.ERROR_INVALID_MANIFEST, tagName));
			throw new InvalidManifestException();
		}
		
		this.log.debug(String.format(Strings.SERVICE_RETURNS, files.toString()));
		return files;
	}
	
	@Override
	public List<GitFile> getTagFiles(String tagName) throws IOException, InvalidManifestException {
		return this.getTagFiles(tagName, true);
	}
	
	@Override
	public List<GitFile> getTagAttachedFiles(String tagName) throws IOException {
		
		this.log.info(String.format(Strings.RETRIEVING_TAG_ATTACHED_FILES, tagName));
		
		List<GitFile> files = new ArrayList<GitFile>();
		String serviceCall = "/contents/cmsMedios/releases/"+tagName+"/attachedFiles";
		String response = "";
		try {
			response = connector.get(serviceCall);
		} catch(IOException e) {
			this.log.info(String.format(Strings.ATTACHED_FILES_FOUND, 0));
			return files;
		}
		
		JSONArray array = JSONArray.fromObject(response);
		this.log.info(String.format(Strings.ATTACHED_FILES_FOUND, array.size()));
		for (int i=0; i<array.size(); i++) {
			JSONObject o = array.getJSONObject(i);
			if(!GithubFilesValidator.validate(o)) continue;
			
			GitFile gitFile = new GitFile();
			gitFile.setFileName(o.getString("name"));
			gitFile.setRawUrl(o.getString("download_url"));
			gitFile.setStatus(GitFileStatus.ADDED);
			files.add(gitFile);
		}
		
		this.log.debug(String.format(Strings.SERVICE_RETURNS, files.toString()));
		return files;
	}
	
	public List<String> getTagAttachedFilesList(String tagName) throws IOException {
		
		List<String> files = new ArrayList<String>();
		String tagVersion = tagName.split("_")[0];
		String tagRM = tagName.split("_")[1].toLowerCase();
		String serviceCall = String.format("/contents/cmsMedios/releases/%s/%s/attachedFiles", tagVersion, tagRM);
		String response = "";
		try {
			response = connector.get(serviceCall);
		} catch(IOException e) {
			return files;
		}
		
		JSONArray array = JSONArray.fromObject(response);
		for (int i=0; i<array.size(); i++) {
			JSONObject o = array.getJSONObject(i);
			if(!GithubFilesValidator.validate(o)) continue;
			
			files.add(o.getString("name"));
		}
		
		return files;
	}
}