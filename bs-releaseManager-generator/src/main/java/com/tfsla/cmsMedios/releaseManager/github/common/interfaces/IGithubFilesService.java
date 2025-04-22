package com.tfsla.cmsMedios.releaseManager.github.common.interfaces;

import java.io.File;
import java.io.IOException;

import com.tfsla.cmsMedios.releaseManager.github.common.GitFile;

/**
 * Provides services for managing Github files raw contents
 */
public interface IGithubFilesService {
	/**
	 * Retrieves a temporary File with the contents of the Github asset
	 * @param gitFile A GitFile with a raw URL to be retrieved
	 * @return File instance, a temporary File with Github asset contents
	 * @throws IOException
	 */
	public File getFile(GitFile gitFile) throws IOException;
	/**
	 * Retrieves a temporary File with the contents of the Github asset
	 * @param url Github raw URL for the file
	 * @return File instance, a temporary File with Github asset contents
	 * @throws IOException
	 */
	public File getFile(String url) throws IOException;
	
	/**
	 * Retrieves a temporary File with the contents of the Github asset
	 * @param gitFile A GitFile with a raw URL to be retrieved
	 * @param path A path to place the file into
	 * @return File instance, a temporary File with Github asset contents
	 * @throws IOException
	 */
	public File getFile(GitFile gitFile, String path) throws IOException;
	
	/**
	 * Retrieves a temporary File with the contents of the Github asset
	 * @param gitFile A GitFile with a raw URL to be retrieved
	 * @param path A path to place the file into
	 * @param ignoreSlashesOnFileName If true, it will ignore slashes on file name for processing
	 * @return File instance, a temporary File with Github asset contents
	 * @throws IOException
	 */
	public File getFile(GitFile gitFile, String path, Boolean ignoreSlashesOnFileName) throws IOException;
	
	/**
	 * Retrieves a temporary File with the contents of the Github asset
	 * @param url Github raw URL for the file
	 * @param path A path to place the file into
	 * @return File instance, a temporary File with Github asset contents
	 * @throws IOException
	 */
	public File getFile(String url, String path) throws IOException;
	
	/**
	 * Retrieves a temporary File with the contents of the Github asset
	 * @param url Github raw URL for the file
	 * @param path A path to place the file into
	 * @param fileName A name for the downloaded File
	 * @return File instance, a temporary File with Github asset contents
	 * @throws IOException
	 */
	public File getFile(String urlString, String path, String fileName) throws IOException;
}
