package com.tfsla.diario.ediciones.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CloneStreamsService {
	private CloneStreamsService() { }
	
	public static CloneStreamsService getInstance() {
		return new CloneStreamsService();
	}
	
	public InputStream clone(File file) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream fileStream = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		int len;
		while ((len = fileStream.read(buffer)) > -1 ) {
		    baos.write(buffer, 0, len);
		}
		baos.flush();
		fileStream.close();

		return new ByteArrayInputStream(baos.toByteArray());
	}
}
