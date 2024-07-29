package com.tfsla.diario.videoConverter;

import java.io.Serializable;

/**
 * Attributes controlling the encoding process (based on FFMPEG)
 */
public class EncodingAttributes implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The format name for the encoded target multimedia file. Be sure this
	 * format is supported (see {@link Encoder#getSupportedEncodingFormats()}.
	 */
	private String format = null;

	/**
	 * The start offset time (seconds). If null or not specified no start offset
	 * will be applied.
	 */
	private Float offset = null;

	/**
	 * The duration (seconds) of the re-encoded stream. If null or not specified
	 * the source stream, starting from the offset, will be completely
	 * re-encoded in the target stream.
	 */
	private Float duration = null;

	/**
	 * The attributes for the encoding of the audio stream in the target
	 * multimedia file. If null of not specified no audio stream will be
	 * encoded. It cannot be null if also the video field is null.
	 */
	private AudioAttributes audioAttributes = null;

	/**
	 * The attributes for the encoding of the video stream in the target
	 * multimedia file. If null of not specified no video stream will be
	 * encoded. It cannot be null if also the audio field is null.
	 */
	private VideoAttributes videoAttributes = null;

	/**
	 * Returns the format name for the encoded target multimedia file.
	 * 
	 * @return The format name for the encoded target multimedia file.
	 */
	String getFormat() {
		return format;
	}

	/**
	 * Sets the format name for the encoded target multimedia file. Be sure this
	 * format is supported (see {@link Encoder#getSupportedEncodingFormats()}.
	 * 
	 * @param format
	 *            The format name for the encoded target multimedia file.
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Returns the start offset time (seconds).
	 * 
	 * @return The start offset time (seconds).
	 */
	Float getOffset() {
		return offset;
	}

	/**
	 * Sets the start offset time (seconds). If null or not specified no start
	 * offset will be applied.
	 * 
	 * @param offset
	 *            The start offset time (seconds).
	 */
	public void setOffset(Float offset) {
		this.offset = offset;
	}

	/**
	 * Returns the duration (seconds) of the re-encoded stream.
	 * 
	 * @return The duration (seconds) of the re-encoded stream.
	 */
	Float getDuration() {
		return duration;
	}

	/**
	 * Sets the duration (seconds) of the re-encoded stream. If null or not
	 * specified the source stream, starting from the offset, will be completely
	 * re-encoded in the target stream.
	 * 
	 * @param duration
	 *            The duration (seconds) of the re-encoded stream.
	 */
	public void setDuration(Float duration) {
		this.duration = duration;
	}

	/**
	 * Returns the attributes for the encoding of the audio stream in the target
	 * multimedia file.
	 * 
	 * @return The attributes for the encoding of the audio stream in the target
	 *         multimedia file.
	 */
	AudioAttributes getAudioAttributes() {
		return audioAttributes;
	}

	/**
	 * Sets the attributes for the encoding of the audio stream in the target
	 * multimedia file. If null of not specified no audio stream will be
	 * encoded. It cannot be null if also the video field is null.
	 * 
	 * @param audioAttributes
	 *            The attributes for the encoding of the audio stream in the
	 *            target multimedia file.
	 */
	public void setAudioAttributes(AudioAttributes audioAttributes) {
		this.audioAttributes = audioAttributes;
	}

	/**
	 * Returns the attributes for the encoding of the video stream in the target
	 * multimedia file.
	 * 
	 * @return The attributes for the encoding of the video stream in the target
	 *         multimedia file.
	 */
	VideoAttributes getVideoAttributes() {
		return videoAttributes;
	}

	/**
	 * Sets the attributes for the encoding of the video stream in the target
	 * multimedia file. If null of not specified no video stream will be
	 * encoded. It cannot be null if also the audio field is null.
	 * 
	 * @param videoAttributes
	 *            The attributes for the encoding of the video stream in the
	 *            target multimedia file.
	 */
	public void setVideoAttributes(VideoAttributes videoAttributes) {
		this.videoAttributes = videoAttributes;
	}

	public String toString() {
		return getClass().getName() + "(format=" + format + ", offset="
				+ offset + ", duration=" + duration + ", audioAttributes="
				+ audioAttributes + ", videoAttributes=" + videoAttributes
				+ ")";
	}

}

