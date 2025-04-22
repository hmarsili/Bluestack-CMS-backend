package com.tfsla.cmsMedios.releaseManager.github.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
	
	public static void pack(String sourceDirPath, final String zipFilePath) throws IOException {
	    final Path p = Files.createFile(Paths.get(zipFilePath));
	    try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
	        Path pp = Paths.get(sourceDirPath);
	        Files.walk(pp)
	          .filter(path -> !path.toString().equals(zipFilePath) && !Files.isDirectory(path))
	          .forEach(path -> {
	        	  ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
	        	  try {
	                  zs.putNextEntry(zipEntry);
	                  zs.write(Files.readAllBytes(path));
	                  zs.closeEntry();
	        	  } catch (Exception e) {
	        		  System.err.println(e);
	        	  }
	          });
	    }
	}
	
	public static void unpack(final String zipFile, final String outputFolder) throws IOException {
		byte[] buffer = new byte[1024];
		ZipInputStream zis = null;
		try {
			File folder = new File(outputFolder);
	    	if(!folder.exists()){
	    		folder.mkdir();
	    	}
	    	zis = new ZipInputStream(new FileInputStream(zipFile));
        	ZipEntry ze = zis.getNextEntry();
        	while(ze != null) {
        		String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                	fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
        	}
		} catch(Exception e) {
			System.err.println(e);
			throw e;
		} finally {
			if(zis != null) {
				zis.close();
			}
		}
	}
}
