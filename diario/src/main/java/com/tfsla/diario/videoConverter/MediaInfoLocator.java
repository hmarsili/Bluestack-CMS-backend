package com.tfsla.diario.videoConverter;

/**
 * Abstract class whose derived concrete instances are used by {@link Encoder}
 * to locate the MediaInfo executable path  
 * @see Encoder
 */
public abstract class MediaInfoLocator {

	/**
	 * This method should return the path of a MediaInfo executable suitable for
	 * the current machine.
	 * 
	 * @return The path of the MediaInfo executable.
	 * 
	 */
	protected abstract String getMediaInfoExecutablePath();

	/**
	 * It returns a brand new {@link MediaInfoExecutor}, ready to be used in a
	 * MediaInfo call.
	 * 
	 * @return A newly instanced {@link MediaInfoExecutor}, using this locator to
	 *         call the MediaInfo executable.
	 */
	public MediaInfoExecutor createExecutor() {
		return new MediaInfoExecutor(getMediaInfoConfigPath);
	}
	
	public String getMediaInfoConfigPath = "/usr/bin/mediainfo";


}
