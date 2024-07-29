package com.tfsla.diario.videoConverter;


import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

public class VideoInfoExtractor {
	
	private static final Log LOG = CmsLog.getLog(VideoInfoExtractor.class);
	
	Pattern p1 = Pattern.compile("^General$",
				Pattern.CASE_INSENSITIVE);
		
		Pattern p2 = Pattern.compile("^Video$",
				Pattern.CASE_INSENSITIVE);
		
		Pattern p3 = Pattern.compile("^Audio$",
				Pattern.CASE_INSENSITIVE);
		
		Pattern p4 = Pattern.compile("^.* [Bb]it rate\\s*: ([\\d\\s]*)Kbps$",
				Pattern.CASE_INSENSITIVE);
		
		Pattern p5 = Pattern.compile(
				"^\\s*Duration\\s*: (.*)$",
				Pattern.CASE_INSENSITIVE);
		
		Pattern p6 = Pattern.compile(
				"^\\s*Format\\s*: (.*)$",
				Pattern.CASE_INSENSITIVE);
				
		Pattern p7 = Pattern.compile(
				"^\\s*Width\\s*: (.*)$",
				Pattern.CASE_INSENSITIVE);
				
		Pattern p8 = Pattern.compile(
				"^\\s*Height\\s*: (.*)$",
				Pattern.CASE_INSENSITIVE);

		Pattern p9 = Pattern.compile(
				"^\\s*Frame rate\\s*: (.*)$",
				Pattern.CASE_INSENSITIVE);
		
		Pattern p10 = Pattern.compile(
				"^\\s*Codec ID\\s*: (.*)$",
				Pattern.CASE_INSENSITIVE);
				
		Pattern p11 = Pattern.compile(
				"^\\s*Format\\s*: (.*)$",
				Pattern.CASE_INSENSITIVE);
				
				
		Pattern p12 = Pattern.compile(
				"^Channel\\(s\\)\\s*: (.*)$",
				Pattern.CASE_INSENSITIVE);

		Pattern p13 = Pattern.compile(
				"^Sampling rate\\s*: (.*)$",
				Pattern.CASE_INSENSITIVE);
	
		Pattern p14 = Pattern.compile(
				"^Display aspect ratio\\s*: (.*)$",
				Pattern.CASE_INSENSITIVE);
		
	                           
	int width=0;
	int height=0;
	
	private MediaInfoLocator locator;

	/**
	 * It builds an encoder using a {@link DefaultFFMPEGLocator} instance to
	 * locate the mediainfo executable to use.
	 */
	public VideoInfoExtractor() {
		this.locator = new DefaultMediaInfoLocator();
	}
	
	public MultimediaInfo getInfo(String url) throws InputFormatException,
	EncoderException {
		MediaInfoExecutor mediainfo = locator.createExecutor();
		mediainfo.addArgument(url);
		try {
			mediainfo.execute();
		} catch (IOException e) {
			LOG.error("Error al ejecutar mediainfo.execute() " + url , e);
			throw new EncoderException(e);
		}
		try {
			RBufferedReader reader = null;
			reader = new RBufferedReader(new InputStreamReader(mediainfo
					.getInputStream()));
			return parseMultimediaInfo(reader);
		} finally {
			mediainfo.destroy();
		}
	}
	
	private MultimediaInfo parseMultimediaInfo(
			RBufferedReader reader)  throws InputFormatException,
	EncoderException {
		
		MultimediaInfo info = null;
		VideoInfo vInfo = null;
		AudioInfo aInfo = null;
		
		width=0;
		height=0;
		int step = 0;
		try {
			while (true) {
				String line = reader.readLine();
				
				LOG.debug(" parseMultimediaInfo >" + line);
				if (line == null) {
					break;
				}
				
				if (step == 0) {
					Matcher m = p1.matcher(line);
					if (m.matches()) {
						info = new MultimediaInfo();
						step++;
					}
				}
				else if (step==1) {
					Matcher m = p2.matcher(line);
					if (m.matches()) {
						vInfo = new VideoInfo();
						step++;
					}
					
					try {
						setGeneralBitRate(info, line);
					} catch (Exception e) {LOG.error("Error al setear el BitRate: " + e.getMessage());}	
					
					try {
						setGeneralDuration(info, line);
					} catch (Exception e) {LOG.error("Error al setear la duración: " + e.getMessage());}
					
					try {
						setFormat(info, line);
					} catch (Exception e){LOG.error("Error al setear el formato: " + e.getMessage());}
					
				}
				else if (step==2) {
					Matcher m = p3.matcher(line);
					if (m.matches()) {
						step++;
						aInfo = new AudioInfo();
					}
					
					try {
						setVideoBitRate(vInfo, line);
					} catch (Exception e) {LOG.error("Error al setear videoBitRate: " + e.getMessage());}
					
					try {
						setVideoSize(vInfo, line);
					} catch (Exception e){LOG.error("Error al setear videoSize: " + e.getMessage());}
					
					try {
						setVideoFrameRate(vInfo, line);
					} catch (Exception e) {LOG.error("Error al setear videoFrameRate: " + e.getMessage());}
					
					try {
						setVideoDecoder(vInfo, line);
					} catch (Exception e){LOG.error("Error al setear videoDecoder: " + e.getMessage());}
					
					try {
						setVideoAspectRatio(vInfo, line);
					} catch (Exception e){LOG.error("Error al setear videoAspectRatio: " + e.getMessage());}

				}
				else if (step==3) {
					
					try {
						setAudioBitRate(aInfo, line);
					} catch (Exception e){LOG.error("Error al setear audioBitRate: " + e.getMessage());}
					
					try {
						setAudioFormat(aInfo, line);
					} catch (Exception e){LOG.error("Error al setear audioFormat: " + e.getMessage());}
					
					try {
						setAudioChannels(aInfo, line);
					} catch (Exception e){LOG.error("Error al setear audioChannels: " + e.getMessage());}
					
					try {
						setAudioSamplingRate(aInfo, line);
					} catch (Exception e){LOG.error("Error al setear audioSamplingRate: " + e.getMessage());}
				}
			}
			
		} catch (IOException e) {
			throw new EncoderException(e);
		}
		if (info == null) {
			throw new InputFormatException();
		}
		
		info.setVideo(vInfo);
		info.setAudio(aInfo);
		
		return info;
	}


	private void setAudioSamplingRate(AudioInfo aInfo, String line)throws Exception {
		//Format
		Matcher mbf = p13.matcher(line);
		if (mbf.matches()) {
			String sSamplingRate = mbf.group(1);
			sSamplingRate = sSamplingRate.toLowerCase();
			sSamplingRate = sSamplingRate.replaceAll("khz","");
			sSamplingRate = sSamplingRate.replaceAll(" ","");
			
			if(sSamplingRate.indexOf("/")>-1){
			   sSamplingRate  = sSamplingRate.split("/")[0];
			}
			
			int samplingRate = new Double(Double.parseDouble(sSamplingRate)).intValue();			
			aInfo.setSamplingRate(samplingRate);
		}
	}
	
	private void setAudioChannels(AudioInfo aInfo, String line)throws Exception {
		//Format
		Matcher mbf = p12.matcher(line);
		if (mbf.matches()) {
			String sChannels = mbf.group(1);
			
			sChannels = sChannels.replaceAll("channels","");
			sChannels = sChannels.replaceAll("channel","");
			sChannels = sChannels.replaceAll(" ","");

			if(sChannels.indexOf("/")>-1){
				sChannels  = sChannels.split("/")[0];
			}
			
			int channels = Integer.parseInt(sChannels);			
			aInfo.setChannels(channels);
		}
	}


	private void setAudioFormat(AudioInfo aInfo, String line)throws Exception {
		//Format
		Matcher mbf = p11.matcher(line);
		if (mbf.matches()) {
			String format = mbf.group(1);
			aInfo.setDecoder(format);
		}
	}
	
	private void setAudioBitRate(AudioInfo aInfo, String line)throws Exception {
		//Bit Rate
		Matcher mbr = p4.matcher(line);
		if (mbr.matches()) {
			String bitrate = mbr.group(1);
			bitrate = bitrate.replace(" ", "");
			bitrate = bitrate.replace("Kbps", " kb/s");
			aInfo.setBitRate(Integer.parseInt(bitrate));
		}
	}
	
	private void setVideoAspectRatio(VideoInfo vInfo, String line)throws Exception {
		Matcher mc = p14.matcher(line);
		if (mc.matches()) {
			String aspectRatio = mc.group(1);
			vInfo.setAspectRatio(aspectRatio);	
		}
	}
	
	private void setVideoDecoder(VideoInfo vInfo, String line)throws Exception {
		Matcher mc = p10.matcher(line);
		if (mc.matches()) {
			String codec = mc.group(1);
			vInfo.setDecoder(codec);	
		}
	}

	private void setVideoFrameRate(VideoInfo vInfo, String line)throws Exception {
		Matcher mfr = p9.matcher(line);
		if (mfr.matches()) {
			String frameRate = mfr.group(1);
			frameRate = frameRate.toLowerCase();
			frameRate = frameRate.replace(" ", "");
			frameRate = frameRate.replace("fps", "");
			
			if (frameRate.contains("(")) {
				frameRate = frameRate.substring(0, frameRate.indexOf("("));
			}
			
			vInfo.setFrameRate(Float.parseFloat(frameRate));
		}
	}

	private void setVideoSize(VideoInfo vInfo, String line) throws Exception{
		//Width
		Matcher mbw = p7.matcher(line);
		if (mbw.matches()) {
			String sWidth = mbw.group(1);
			sWidth = sWidth.replace(" ", "");
			sWidth = sWidth.replace("pixels", "");
			
			width = Integer.parseInt(sWidth);
			
			if (width!=0 && height!=0)
				vInfo.setSize(new VideoSize(width,
				height));
		}
							
		//Height
		Matcher mbh = p8.matcher(line);
		if (mbh.matches()) {
			String sHeight = mbh.group(1);
			sHeight = sHeight.replace(" ", "");
			sHeight = sHeight.replace("pixels", "");
			
			height = Integer.parseInt(sHeight);
			
			if (width!=0 && height!=0)
				vInfo.setSize(new VideoSize(width,
				height));
		}
	}

	private void setVideoBitRate(VideoInfo vInfo, String line)throws Exception {
		//Bit Rate
		Matcher mbr = p4.matcher(line);
		if (mbr.matches()) {
			String bitrate = mbr.group(1);
			bitrate = bitrate.replace(" ", "");
			bitrate = bitrate.replace("Kbps", " kb/s");
			vInfo.setBitRate(Integer.parseInt(bitrate));
		}
	}

	private void setFormat(MultimediaInfo info, String line)throws Exception {
		//Format
		Matcher mbf = p4.matcher(line);
		if (mbf.matches()) {
			String format = mbf.group(1);
			info.setFormat(format);
		}
	}

	private void setGeneralDuration(MultimediaInfo info, String line)throws Exception {
		// Duration
		Matcher mdur = p5.matcher(line);
		if (mdur.matches()) {
			long hours = 0;
			long minutes = 0;
			long seconds = 0;
			long millisec = 0;
			
			String duration =  mdur.group(1);
			
			if(duration.indexOf("ms")>-1)
				duration = duration.replace("ms","ml");

			if(duration.indexOf("h")>-1){
				String[] partDur = duration.split("h");
				hours = Integer.parseInt(partDur[0].trim());
				duration = partDur[1];
			}

			if(duration.indexOf("mn")>-1){
				String[] partDur = duration.split("mn");
				minutes = Integer.parseInt(partDur[0].trim());
				duration = partDur[1];
			}

			if(duration.indexOf("s")>-1){
				String[] partDur = duration.split("s");
				seconds = Integer.parseInt(partDur[0].trim());
				duration = partDur[1];
			}
			
			if(duration.indexOf("ml")>-1){
				String[] partDur = duration.split("ml");
				millisec = Integer.parseInt(partDur[0].trim());
			}
		
			long durationMs = (millisec) + (seconds * 1000L)
					+ (minutes * 60L * 1000L)
					+ (hours * 60L * 60L * 1000L);
			info.setDuration(durationMs);
		}
	}

	private void setGeneralBitRate(MultimediaInfo info, String line)throws Exception {
		//General Bit Rate
		Matcher mbr = p4.matcher(line);
		if (mbr.matches()) {
			String bitrate = mbr.group(1);
			bitrate = bitrate.replace(" ", "");
			bitrate = bitrate.replace("Kbps", " kb/s");
			info.setBitrate(bitrate);
		}
	}
}
