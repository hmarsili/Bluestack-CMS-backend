package com.tfsla.diario.videoConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The default ffmpeg executable locator, which exports on disk the ffmpeg
 * executable bundled with the library distributions. It should work both for
 * windows and many linux distributions. If it doesn't, try compiling your own
 * ffmpeg executable and plug it in JAVE with a custom {@link FFMPEGLocator}.
 */
public class DefaultFFMPEGLocator extends FFMPEGLocator {

	/**
	 * Trace the version of the bundled ffmpeg executable. It's a counter: every
	 * time the bundled ffmpeg change it is incremented by 1.
	 */
	private static final int myEXEversion = 1;

	/**
	 * The ffmpeg executable file path.
	 */
	private String path;

	/**
	 * It builds the default FFMPEGLocator, exporting the ffmpeg executable on a
	 * temp file.
	 */
	public DefaultFFMPEGLocator() {
		// Windows?
		boolean isWindows;
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("windows") != -1) {
			isWindows = true;
		} else {
			isWindows = false;
		}
		// Temp dir?
		File temp = new File(System.getProperty("java.io.tmpdir"), "cmsMediosConverter-"
				+ myEXEversion);
		if (!temp.exists()) {
			temp.mkdirs();
			temp.deleteOnExit();
		}
		// ffmpeg executable export on disk.
		String suffix = isWindows ? ".exe" : "";
		File exe = new File(temp, "ffmpeg" + suffix);
	//	if (!exe.exists()) {
	//		copyFile("ffmpeg" + suffix, exe);
	//	}
		// pthreadGC2.dll
		if (isWindows) {
			File dll = new File(temp, "pthreadGC2.dll");
			if (!dll.exists()) {
				copyFile("pthreadGC2.dll", dll);
			}
		}
		// Need a chmod?
		if (!isWindows) {
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec(new String[] { "/bin/chmod", "775",
						exe.getAbsolutePath() });
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Ok.
		this.path = exe.getAbsolutePath();
	}

	protected String getFFMPEGExecutablePath() {
		return path;
	}

	/**
	 * Copies a file bundled in the package to the supplied destination.
	 * 
	 * @param path
	 *            The name of the bundled file.
	 * @param dest
	 *            The destination.
	 * @throws RuntimeException
	 *             If aun unexpected error occurs.
	 */
	private void copyFile(String path, File dest) throws RuntimeException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = getClass().getResourceAsStream(path);
			output = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int l;
			while ((l = input.read(buffer)) != -1) {
				output.write(buffer, 0, l);
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot write file "
					+ dest.getAbsolutePath());
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (Throwable t) {
					;
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (Throwable t) {
					;
				}
			}
		}
	}

}
