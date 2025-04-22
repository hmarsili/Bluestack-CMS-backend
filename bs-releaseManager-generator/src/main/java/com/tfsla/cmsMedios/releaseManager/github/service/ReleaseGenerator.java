package com.tfsla.cmsMedios.releaseManager.github.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.tfsla.cmsMedios.releaseManager.common.GenerateReleaseRequest;
import com.tfsla.cmsMedios.releaseManager.common.ConnectorConfiguration;
import com.tfsla.cmsMedios.releaseManager.github.common.GitFile;
import com.tfsla.cmsMedios.releaseManager.github.common.GitFileStatus;
import com.tfsla.cmsMedios.releaseManager.github.common.InvalidManifestException;
import com.tfsla.cmsMedios.releaseManager.github.common.ReleaseCMS;

public class ReleaseGenerator {
	
	public static String rmBaseDir = "";
	public static List<String> attachedFilesList =  new ArrayList<String>();
	
	public static ReleaseCMS generate(GenerateReleaseRequest releaseRequest) throws IOException, InvalidManifestException {
		String tagName = releaseRequest.getTagName().replace(" ", "");
		String previousTagName = releaseRequest.getPreviousTagName();
		String protecteFiles = releaseRequest.getProtectedFiles();
		String removedJars = releaseRequest.getRemovedJars();
		String updateBannerLink = releaseRequest.getUpdateBannerLink();
		String updateBannerVFSLink = releaseRequest.getUpdateBannerVFSLink();
		String gitCoreFolder = releaseRequest.getGitCoreFolder();
		String releaseVersion = releaseRequest.getReleaseVersion();
		
		ConnectorConfiguration coreConfiguration = releaseRequest.getCoreConfiguration();
		ConnectorConfiguration vfsConfiguration = releaseRequest.getVfsConfiguration();
		
		GithubTagsService coreTagsService = new GithubTagsService(coreConfiguration);
		GithubTagsService vfsTagsService = new GithubTagsService(vfsConfiguration);
		ManifestGenerator manifestGenerator = new ManifestGenerator(tagName);
		List<GitFile> configFiles = coreTagsService.getTagFiles(gitCoreFolder, false);
		List<GitFile> attachedFiles = coreTagsService.getTagAttachedFiles(gitCoreFolder);
		List<GitFile> jars = coreTagsService.getTagJars(gitCoreFolder);
		List<GitFile> scripts = coreTagsService.getTagSQLScripts(gitCoreFolder);
		List<GitFile> xmlConfig = coreTagsService.getTagConfigFiles(gitCoreFolder);
		List<GitFile> changedFiles = vfsTagsService.getFilesBetweenTags(previousTagName, tagName);
		
		manifestGenerator.addUpdateBannerLink(updateBannerLink);
		manifestGenerator.addupdateBannerVfsLink(updateBannerVFSLink);
		manifestGenerator.addReleaseVersion(releaseVersion);
		
		String currentDir = System.getProperty("user.dir");
		
		File rmBaseDir = createRMDirectory(currentDir, tagName);
		String rmBaseDirName = rmBaseDir.getAbsolutePath();
		
		setRMBaseDir(rmBaseDirName);
		
		setAttachedFiles(coreTagsService.getTagAttachedFilesList(tagName));
		
		String[] protecteFilesArr = protecteFiles.split(",");
		for (String a : protecteFilesArr) manifestGenerator.addProtectedFile(a);
		
		String[] removedJarsArr = removedJars.split(",");
		for (String r : removedJarsArr) manifestGenerator.addRemovedJar(r);
		
		GithubFilesService coreFilesService = new GithubFilesService(coreConfiguration);
		File configDir = createRMDirectory(rmBaseDirName, "config");
		fillDirectory(configDir.getAbsolutePath(), configFiles, coreFilesService, true);
		configFiles.stream().forEach(x->manifestGenerator.addConfig(x.getFileName()));
		
		File attachedFilesDir = createRMDirectory(rmBaseDirName, "attachedFiles");
		fillDirectory(attachedFilesDir.getAbsolutePath(), attachedFiles, coreFilesService);
		attachedFiles.stream().forEach(x->manifestGenerator.addAttachedFile(x.getFileName()));
		
		File jarsDir = createRMDirectory(rmBaseDirName, "jars");
		fillDirectory(jarsDir.getAbsolutePath(), jars, coreFilesService);
		jars.stream().forEach(x->manifestGenerator.addJar(x.getFileName()));
		
		File scriptsDir = createRMDirectory(rmBaseDirName, "scripts");
		fillDirectory(scriptsDir.getAbsolutePath(), scripts, coreFilesService);
		scripts.stream().forEach(x->manifestGenerator.addScript(x.getFileName()));
		
		File cmsMediosConfigDir = createRMDirectory(rmBaseDirName, "cmsMedios");
		fillDirectory(cmsMediosConfigDir.getAbsolutePath(), xmlConfig, coreFilesService);
		xmlConfig.stream().forEach(x->manifestGenerator.addCmsMediosConfig(x.getFileName()));
		
		File filesDir = createRMDirectory(rmBaseDirName, "files");
		GithubFilesService vfsFilesService = new GithubFilesService(vfsConfiguration);
		
		List<GitFile> added = changedFiles.stream().filter(x->x.getStatus() == GitFileStatus.ADDED || x.getStatus() == GitFileStatus.RENAMED).collect(Collectors.toList());
		filesDir = createRMDirectory(rmBaseDirName, "files/added");
		fillDirectory(filesDir.getAbsolutePath(), added, vfsFilesService, true);
		added.forEach(x->manifestGenerator.addFileCreated(x.getFileName()));
		
		List<GitFile> changed = changedFiles.stream().filter(x->x.getStatus() == GitFileStatus.MODIFIED).collect(Collectors.toList());
		filesDir = createRMDirectory(rmBaseDirName, "files/modified");
		fillDirectory(filesDir.getAbsolutePath(), changed, vfsFilesService, true);
		changed.forEach(x->manifestGenerator.addFileChanged(x.getFileName()));
		
		changedFiles.stream().filter(x->x.getStatus() == GitFileStatus.DELETED || x.getStatus() == GitFileStatus.RENAMED)
			.forEach(x-> {
				if (x.getStatus() == GitFileStatus.RENAMED) { manifestGenerator.addFileRemoved(x.getPreviousFileName()); }
				else { manifestGenerator.addFileRemoved(x.getFileName()); }
			});
		
		String zipName = rmBaseDirName + ".zip";
		File f = new File(zipName);
		if(f.exists()) {
			System.out.println(String.format("Deleting %s...", zipName));
			f.delete();
		}
		System.out.println(String.format("Packing directory %s to %s...", rmBaseDirName, zipName));
		ZipUtils.pack(rmBaseDirName, zipName);
		System.out.println("Done!");
		
		ReleaseCMS release = new ReleaseCMS();
		release.setManifestPath(createManifest(manifestGenerator, rmBaseDirName));
		release.setReleasePath(rmBaseDirName + ".zip");
		release.setReleaseName(tagName);
		
		return release;
	}
	
	protected static String createManifest(ManifestGenerator manifestGenerator, String baseDir) throws IOException {
		String fileName = "manifest.json";
		System.out.println(String.format("Creating manifest %s...", fileName));
		if(!baseDir.endsWith("/")) {
			baseDir += "/";
		}
		
		File file = new File(baseDir + fileName);
		FileWriter output = new FileWriter(file);
		output.write(manifestGenerator.getManifest().toString());
		output.close();
		
		return baseDir + fileName;
	}
	
	protected static void fillDirectory(String directory, List<GitFile> files, GithubFilesService filesService) throws IOException {
		fillDirectory(directory, files, filesService, false);
	}
	
	protected static void fillDirectory(String directory, List<GitFile> files, GithubFilesService filesService, Boolean ignoreSlashesOnFileName) throws IOException {
		Iterator<GitFile> iter = files.iterator();
		while (iter.hasNext()) {
			GitFile file = iter.next();

			try {
				System.out.println(String.format("Downloading %s from %s...", file.getFileName(), file.getRawUrl()));
				filesService.getFile(file, directory, ignoreSlashesOnFileName);
				System.out.println("Done!");
			} catch(Exception e) {
				System.out.println("Error downloading file: " + e.getMessage());
				iter.remove();
			}
		}
		System.out.println(String.format("Placed %s files on %s", files.size(), directory));
	}
	
	protected static File createRMDirectory(String baseDir, String directoryName) {
		if(!baseDir.endsWith("/")) {
			baseDir += "/";
		}
		File theDir = new File(baseDir + directoryName);
		if (!theDir.exists()) {
		    System.out.println("creating directory: " + baseDir + directoryName);
		    boolean result = false;

		    try{
		        theDir.mkdir();
		        result = true;
		    } catch(SecurityException se){
		        System.out.println("SecurityException: " + se.getMessage());
		        throw se;
		    }
		    if(result) {    
		        System.out.println("DIR created");  
		    }
		}
		
		return theDir;
	}
	
	public static void setRMBaseDir(String rmBaseDirName) {
		rmBaseDir = rmBaseDirName;
	}
	
	public static String getRMBaseDir() {
		return rmBaseDir;
	} 
	
	public static void setAttachedFiles(List<String> attachedFiles) {
		attachedFilesList = attachedFiles;
	}
	
	public static List<String> getAttachedFiles() {
		return attachedFilesList;
	} 
}
