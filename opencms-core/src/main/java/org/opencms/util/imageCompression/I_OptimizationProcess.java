package org.opencms.util.imageCompression;

import java.io.IOException;

public interface I_OptimizationProcess {
	public boolean useProcessor(String extension);
	public int execute(byte[] in) throws IOException;
	public byte[] getResult();
	public String getErrorMsg();
}
