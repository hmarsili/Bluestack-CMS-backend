package com.tfsla.statistics;

import java.rmi.RemoteException;

public class BlockedException extends RemoteException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4208527046524359900L;

	public BlockedException(String msg)
	{
		super(msg);
	}
}
