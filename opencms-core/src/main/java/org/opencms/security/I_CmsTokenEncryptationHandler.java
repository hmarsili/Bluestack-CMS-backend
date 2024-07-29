package org.opencms.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public interface I_CmsTokenEncryptationHandler {

	String encryptToken(String text);

	String decryptToken(String text) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalArgumentException, InvalidKeyException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException;

	String decryptEncodedLogin(String text) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalArgumentException, InvalidKeyException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException;

}