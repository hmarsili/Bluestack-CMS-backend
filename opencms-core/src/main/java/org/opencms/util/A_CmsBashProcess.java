package org.opencms.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.opencms.main.CmsLog;

abstract public class A_CmsBashProcess {
	
	protected static final Log LOG = CmsLog.getLog(A_CmsBashProcess.class);
	
	protected List<String> commandLine;
	
	private byte[] result;
	public byte[] getResult() {
		return result;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	private String errorMsg;
	
	public A_CmsBashProcess() {
	
	}
	
	
	public int execute(byte[] image) throws IOException {
		
		ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
		
		final Process process = processBuilder.start();
		
		OutputStream stdin = process.getOutputStream ();
		final InputStream stderr = process.getErrorStream ();
		final InputStream stdout = process.getInputStream ();
		
		
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ByteArrayOutputStream errorBos = new ByteArrayOutputStream();
		
		Thread errorT = new Thread() {
            public void run() {
    			byte[] buf = new byte[1024];
    			int len;
        		try {
        			while ((len = stdout.read(buf)) != -1) {
        				// process byte buffer
        				bos.write(buf, 0, len);
        				bos.flush();
        			}
        		} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        };
        
        errorT.start();
        
        Thread outputT = new Thread() {
            public void run() {
    			byte[] buf = new byte[1024];
    			int len;
        		try {
        			while ((len = stderr.read(buf)) != -1) {
        				errorBos.write(buf, 0, len);
        				errorBos.flush();
        			}
        			
        		} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
            }
        };
        
        outputT.start();
        
        stdin.write(image);
        stdin.flush();
        
        stdin.close();

        int res=0;
		try {
			res = process.waitFor();
		
			errorT.join();
			outputT.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
		stdin.close();
		stderr.close();
		stdout.close();
		
		
		result = bos.toByteArray();
		if (result.length==0 || result.length>image.length)
			result = image;
		
		errorMsg = errorBos.toString();
		
		return res;
	}
	
	
	}

