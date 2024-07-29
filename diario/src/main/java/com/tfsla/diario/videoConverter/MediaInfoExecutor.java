package com.tfsla.diario.videoConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

/**
 * A mediainfo process wrapper (based on mediainfo)
 */
public class MediaInfoExecutor {

	private static final Log LOG = CmsLog.getLog(MediaInfoExecutor.class);
	/**
	 * The path of the mediainfo executable.
	 */
	private String mediainfoExecutablePath;

	/**
	 * Arguments for the executable.
	 */
	private ArrayList args = new ArrayList();

	/**
	 * The process representing the mediainfo execution.
	 */
	private Process mediainfo = null;

	/**
	 * A process killer to kill the mediainfo process with a shutdown hook, useful
	 * if the jvm execution is shutted down during an ongoing encoding process.
	 */
	private ProcessKiller mediainfoKiller = null;

	/**
	 * A stream reading from the mediainfo process standard output channel.
	 */
	private InputStream inputStream = null;

	/**
	 * A stream writing in the mediainfo process standard input channel.
	 */
	private OutputStream outputStream = null;

	/**
	 * A stream reading from the mediainfo process standard error channel.
	 */
	private InputStream errorStream = null;
	
	
	/**
	 * It build the executor.
	 * 
	 * @param mediainfoExecutablePath
	 *            The path of the mediainfo executable.
	 */
	public MediaInfoExecutor(String mediainfoExecutablePath) {
		this.mediainfoExecutablePath = mediainfoExecutablePath;
	}

	/**
	 * Adds an argument to the mediainfo executable call.
	 * 
	 * @param arg
	 *            The argument.
	 */
	public void addArgument(String arg) {
		args.add(arg);
	}

	/**
	 * Executes the mediainfo process with the previous given arguments.
	 * 
	 * @throws IOException
	 *             If the process call fails.
	 */
	public void execute() throws IOException {
		int argsSize = args.size();
		String[] cmd = new String[argsSize + 1];
		cmd[0] = mediainfoExecutablePath;
		for (int i = 0; i < argsSize; i++) {
			cmd[i + 1] = (String) args.get(i);
		}
		Runtime runtime = Runtime.getRuntime();
		
		LOG.debug("executing >" + StringUtils.join(cmd, " ") );
		mediainfo = runtime.exec(cmd);
		mediainfoKiller = new ProcessKiller(mediainfo);
		runtime.addShutdownHook(mediainfoKiller);
		inputStream = mediainfo.getInputStream();
		outputStream = mediainfo.getOutputStream();
		errorStream = mediainfo.getErrorStream();
	}

	/**
	 * Returns a stream reading from the mediainfo process standard output channel.
	 * 
	 * @return A stream reading from the mediainfo process standard output channel.
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Returns a stream writing in the mediainfo process standard input channel.
	 * 
	 * @return A stream writing in the mediainfo process standard input channel.
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 * Returns a stream reading from the mediainfo process standard error channel.
	 * 
	 * @return A stream reading from the mediainfo process standard error channel.
	 */
	public InputStream getErrorStream() {
		return errorStream;
	}

	/**
	 * If there's a mediainfo execution in progress, it kills it.
	 */
	public void destroy() {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (Throwable t) {
				;
			}
			inputStream = null;
		}
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (Throwable t) {
				;
			}
			outputStream = null;
		}
		if (errorStream != null) {
			try {
				errorStream.close();
			} catch (Throwable t) {
				;
			}
			errorStream = null;
		}
		if (mediainfo != null) {
			mediainfo.destroy();
			mediainfo = null;
		}
		if (mediainfoKiller != null) {
			Runtime runtime = Runtime.getRuntime();
			runtime.removeShutdownHook(mediainfoKiller);
			mediainfoKiller = null;
		}
	}

}
