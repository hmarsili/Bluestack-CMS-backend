package com.tfsla.diario.videoConverter.jsp.amazon;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.tfsla.diario.videoConverter.DefaultFFMPEGLocator;
import com.tfsla.diario.videoConverter.FFMPEGExecutor;
import com.tfsla.diario.videoConverter.FFMPEGLocator;

public class AudioCodecHelper {

	public static synchronized Boolean videoHasAudio(String videoUrl) throws Exception {
		FFMPEGLocator locator = new DefaultFFMPEGLocator();
		FFMPEGExecutor ffmpeg = locator.createExecutor();
		ffmpeg.addArgument("-i");
		ffmpeg.addArgument(videoUrl);
		String res = "";
		BufferedReader reader = null;
		try {
			ffmpeg.execute();
			reader = new BufferedReader(new InputStreamReader(ffmpeg.getErrorStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				res += line;
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			ffmpeg.destroy();
			if(reader != null) reader.close();
		}
		return res.contains("Audio:");
	}
}
