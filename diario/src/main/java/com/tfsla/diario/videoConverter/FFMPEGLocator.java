package com.tfsla.diario.videoConverter;

/**
 * Abstract class whose derived concrete instances are used by {@link Encoder}
 * to locate the ffmpeg executable path  
 * @see Encoder
 */
public abstract class FFMPEGLocator {

	/**
	 * This method should return the path of a ffmpeg executable suitable for
	 * the current machine.
	 * 
	 * @return The path of the ffmpeg executable.
	 * 
	 */
	protected abstract String getFFMPEGExecutablePath();

	/**
	 * It returns a brand new {@link FFMPEGExecutor}, ready to be used in a
	 * ffmpeg call.
	 * 
	 * @return A newly instanced {@link FFMPEGExecutor}, using this locator to
	 *         call the ffmpeg executable.
	 */
	public FFMPEGExecutor createExecutor() {
		return new FFMPEGExecutor(getFFMPEGConfigPath);
	}
	
	public String getFFMPEGConfigPath = "/usr/bin/ffmpeg";


}
