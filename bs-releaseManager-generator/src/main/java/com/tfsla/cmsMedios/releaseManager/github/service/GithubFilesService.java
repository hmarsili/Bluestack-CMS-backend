package com.tfsla.cmsMedios.releaseManager.github.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.tfsla.cmsMedios.releaseManager.common.ConnectorConfiguration;
import com.tfsla.cmsMedios.releaseManager.github.common.GitFile;
import com.tfsla.cmsMedios.releaseManager.github.common.Strings;
import com.tfsla.cmsMedios.releaseManager.github.common.interfaces.IGithubFilesService;
import com.tfsla.cmsMedios.releaseManager.github.service.core.GithubService;

public class GithubFilesService extends GithubService implements IGithubFilesService {

	public GithubFilesService(ConnectorConfiguration config) {
		super(config);
	}

	@Override
	public File getFile(GitFile gitFile) throws IOException {
		return getFile(gitFile.getRawUrl(), null, this.getFileName(gitFile.getFileName()));		
	}
	
	@Override
	public File getFile(GitFile gitFile, String path) throws IOException {
		return getFile(gitFile.getRawUrl(), path, this.getFileName(gitFile.getFileName()));
	}
	
	@Override
	public File getFile(GitFile gitFile, String path, Boolean ignoreSlashesOnFileName) throws IOException {
		if(ignoreSlashesOnFileName) {
			return getFile(gitFile.getRawUrl(), path, gitFile.getFileName());
		}
		return getFile(gitFile.getRawUrl(), path, this.getFileName(gitFile.getFileName()));
	}
	
	@Override
	public File getFile(String urlString) throws IOException {
		return getFile(urlString, null);
	}
	
	@Override
	public File getFile(String urlString, String path) throws IOException {
		String fileName = this.getFileName(urlString);
		return this.getFile(urlString, path, fileName);
	}
	
	@Override
	public File getFile(String urlString, String path, String fileName) throws IOException {
		this.log.info(String.format(Strings.RETRIEVING_FILE, urlString));
		File file = null;
		URL url = new URL(urlString);
		InputStream inputStream = connector.getBinary(url);
		if(fileName == null || fileName.equals("")) {
			fileName = this.getFileName(urlString);
		}
		if(fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf("?"));
		}
		if(fileName.contains("&")) {
			fileName = fileName.substring(0, fileName.indexOf("&"));
		}
		this.log.info(String.format(Strings.FILE_NAME, fileName));
		if(path != null && !path.equals("")) {
			if(!path.endsWith("/")) {
				path += "/";
			}
			file = new File(path + fileName);
		} else {
			file = File.createTempFile(fileName, "");
		}
		
		file.getParentFile().mkdirs();
		//file.mkdir();
		
		OutputStream output = new FileOutputStream(file);
		
		int n = -1;
		byte[] buffer = new byte[4096];
		while((n = inputStream.read(buffer)) != -1) {
			output.write(buffer, 0, n);
		}

		output.close();
		inputStream.close();
		
		this.log.info(String.format(Strings.FILE_CREATED, file.getAbsolutePath()));
		file.deleteOnExit();
		
		return file;
	}
	
	private String getFileName(String fileName) {
		if(fileName.contains("/")) {
			return fileName.substring(fileName.lastIndexOf('/') + 1);
		}
		return fileName;
	}
}
