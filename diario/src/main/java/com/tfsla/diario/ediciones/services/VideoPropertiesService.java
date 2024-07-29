package com.tfsla.diario.ediciones.services;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import org.opencms.file.CmsProperty;

import com.tfsla.diario.videoConverter.Encoder;
import com.tfsla.diario.videoConverter.EncoderException;
import com.tfsla.diario.videoConverter.InputFormatException;
import com.tfsla.diario.videoConverter.MultimediaInfo;
import com.tfsla.diario.videoConverter.VideoInfo;
import com.tfsla.diario.videoConverter.VideoSize;

/**
 * Manages videos properties and maps them to CmsProperty objects
 */
public class VideoPropertiesService {
	
	/**
	 * Appends common video properties to a collection of CmsProperty objects
	 * @param properties a Lists of CmsProperty objects where the properties will be added to
	 * @param videoPath the full path to the video in the real file system
	 * @throws InputFormatException
	 * @throws EncoderException
	 */
	@SuppressWarnings("unchecked")
	public void setVideoProperties(List properties, String videoPath) throws InputFormatException, EncoderException {
		File uploadedFile = new File(videoPath);
		Encoder encoder = new Encoder();
	    MultimediaInfo infoVideo = new MultimediaInfo();
    	infoVideo = encoder.getInfo(uploadedFile);
    	
    	long durationMills = infoVideo.getDuration();
		int seconds = (int)((durationMills / 1000)%60);  
		int minutes = (int)((durationMills / 1000)/60);  
		int hours = (int)((durationMills / (1000*60*60))%24);  
		
		DecimalFormat formateador = new DecimalFormat("00");
		String duration = formateador.format(hours) + ":" + formateador.format(minutes) + ":" + formateador.format(seconds);
    	
		VideoInfo infoVideoMedia = new VideoInfo();
		infoVideoMedia = infoVideo.getVideo();
		VideoSize videoSize = infoVideoMedia.getSize(); 
		String bitrate = infoVideo.getBitrate();
		
		CmsProperty prop = new CmsProperty();
		prop = new CmsProperty();
		prop.setName("video-duration");
		prop.setAutoCreatePropertyDefinition(true);
		prop.setStructureValue(duration);
		properties.add(prop);
		 
		prop = new CmsProperty();
		prop.setName("video-size");
		prop.setAutoCreatePropertyDefinition(true);
		prop.setStructureValue(videoSize.getWidth() + "x" + videoSize.getHeight());
		properties.add(prop);
		 
		prop = new CmsProperty();
		prop.setName("video-bitrate");
		prop.setAutoCreatePropertyDefinition(true);
		prop.setStructureValue(bitrate);
		properties.add(prop);
	}
	
}
