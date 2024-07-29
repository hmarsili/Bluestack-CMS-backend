package com.tfsla.diario.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TfsHashGenerator {

	static public String  getHash(String value)
	{
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			byte[] data = value.getBytes(); 
			m.update(data,0,data.length);
			BigInteger i = new BigInteger(1,m.digest());
			return String.format("%1$032X", i);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		return value;
	}
}
