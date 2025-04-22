package com.tfsla.cmsMedios.releaseManager.github.common.interfaces;

import java.io.IOException;
import java.util.List;

import com.tfsla.cmsMedios.releaseManager.github.common.GitFile;
import com.tfsla.cmsMedios.releaseManager.github.common.InvalidManifestException;

/**
 * Provides services for retrieving Github tags assets and information 
 */
public interface IGithubTagsService {
	
	/**
	 * Retrieves a list of tags for a repository
	 * @return A List of GitFile instances with the tags for the repository 
	 * @throws IOException
	 * @see GitFile
	 */
	public List<GitFile> getTagsList() throws IOException;
	
	/**
	 * Retrieves a list of assets changed between two tags
	 * @param tagName1 First tag to get assets from
	 * @param tagName2 Tag to get assets to
	 * @return A List of GitFile instances with the assets changed between two tags
	 * @throws IOException
	 */
	public List<GitFile> getFilesBetweenTags(String tagName1, String tagName2) throws IOException;
	
	/**
	 * Retrieves a list of GitFile instances of jars found for a tag 
	 * @param tagName Tag name to retrieve jars for
	 * @return a list of GitFile instances for the jars found for the tag required
	 * @throws IOException
	 */
	public List<GitFile> getTagJars(String tagName) throws IOException;
	
	/**
	 * Retrieves the SQL scripts necessary for deploying a release
	 * @param tagName Tag name to retrieve SQL scripts for
	 * @return A list of GitFile instances for the SQL scripts found for the tag required
	 * @throws IOException
	 */
	public List<GitFile> getTagSQLScripts(String tagName) throws IOException;
	
	/**
	 * Retrieves XML files containing modules upgrades and general changes for cmsMedios.xml config file
	 * @param tagName Tag name to retrieve config files for
	 * @return A list of GitFile instances for the XML config files found for the tag required
	 * @throws IOException
	 */
	public List<GitFile> getTagConfigFiles(String tagName) throws IOException;
	
	/**
	 * Retrieves the config. files necessary for deploying a release
	 * @param tagName Tag name to retrieve SQL scripts for
	 * @return A list of GitFile instances for the config. files found for the tag required
	 * @throws IOException
	 * @throws InvalidManifestException If the tag does not have a manifest.json in its root folder
	 */
	public List<GitFile> getTagFiles(String tagName) throws IOException, InvalidManifestException;
	
	/**
	 * Retrieves the config. files necessary for deploying a release
	 * @param tagName Tag name to retrieve SQL scripts for
	 * @param requireManifest Indicates either if manifest.json is required or not
	 * @return A list of GitFile instances for the config. files found for the tag required
	 * @throws IOException
	 * @throws InvalidManifestException If the tag does not have a manifest.json in its root folder
	 */
	public List<GitFile> getTagFiles(String tagName, Boolean requireManifest) throws IOException, InvalidManifestException;
	
	/**
	 * Retrieves a list of GitFile instances of attached files found for a tag 
	 * @param tagName Tag name to retrieve files for
	 * @return a list of GitFile instances for the attached files found for the tag required
	 * @throws IOException
	 */
	public List<GitFile> getTagAttachedFiles(String tagName) throws IOException;
}
