package org.opencms.security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.opencms.main.OpenCms;

public class CmsDefaultTokenEncryptationHandler implements I_CmsTokenEncryptationHandler {

	private final int CHUNK_SIZE=117;
	private final int ENCODED_CHUNK_SIZE=172;
			
	@Override
	public String encryptToken(String text) {
		String enc = "";
		
		String key = OpenCms.getCmsTwoFactorLoginConfiguration().getRsaPuKToken();
		PublicKey pubkey;
		try {
			pubkey = preparePublicKey(key);
	
			List<String> splitedText = split(text, CHUNK_SIZE);
			for (String part : splitedText) {
				enc += encryptStringWithPublicKey(part,pubkey);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return enc;
	}
	
	
	@Override
	public String decryptToken(String text) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalArgumentException, InvalidKeyException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		String dec = "";
		
		String key = OpenCms.getCmsTwoFactorLoginConfiguration().getRsaPrKToken();
		PrivateKey privkey;
		
			privkey = preparePrivateKey(key);
	
			List<String> splitedText = split(text, ENCODED_CHUNK_SIZE);
			for (String part : splitedText) {
				dec += decryptStringWithPrivateKey(part,privkey);
				
			}
			
		return dec;
	}

	
	@Override
	public String decryptEncodedLogin(String text) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalArgumentException, InvalidKeyException, NoSuchPaddingException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
		String dec = "";
		
		String key = OpenCms.getCmsTwoFactorLoginConfiguration().getRsaKeyExtChannel();
		PrivateKey privkey;
		
			privkey = preparePrivateKey(key);
	
			List<String> splitedText = split(text, ENCODED_CHUNK_SIZE);
			for (String part : splitedText) {
				dec += decryptStringWithPrivateKey(part,privkey);
				
			}
		
		return dec;
	}
	
	private String decryptStringWithPrivateKey(String s,PrivateKey pkey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, pkey);
        String dec = new String(cipher.doFinal(Base64.getDecoder().decode(s)), "UTF-8");
 
        return dec;
    }
	
	private String encryptStringWithPublicKey(String s, PublicKey pubkey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("RSA");
        
        cipher.init(Cipher.ENCRYPT_MODE, pubkey);
        String enc = Base64.getEncoder().encodeToString(cipher.doFinal(s.getBytes("UTF-8")));
        return enc;
    }
	
	private PublicKey preparePublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        
    	byte[] keyBytes = publicKey.getBytes();
    	
    	String keyString = new String(keyBytes);
        String privKeyPEM = keyString.replace("-----BEGIN PUBLIC KEY-----", "");
        privKeyPEM = privKeyPEM.replace("-----END PUBLIC KEY-----", "");
        privKeyPEM = privKeyPEM.replace("\r", "");
        privKeyPEM = privKeyPEM.replace("\n", "");
        keyBytes = Base64.getDecoder().decode(privKeyPEM);
        
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
	
	private List<String> split(String s, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < s.length(); i += chunkSize) {
            chunks.add(s.substring(i, Math.min(s.length(), i + chunkSize)));
        }
        return chunks;
    }
	
	// https://stackoverflow.com/questions/7216969/getting-rsa-private-key-from-pem-base64-encoded-private-key-file/55339208#55339208
    // https://github.com/Mastercard/client-encryption-java/blob/master/src/main/java/com/mastercard/developer/utils/EncryptionUtils.java
    // https://docs.oracle.com/javase/8/docs/api/java/security/spec/PKCS8EncodedKeySpec.html
    private PrivateKey preparePrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalArgumentException {
        
    	//byte[] keyBytes = Files.readAllBytes(new File(keyFilename).toPath());
    	
    	
    	//byte[] keyBytes = Files.readAllBytes(new File(keyFilename).toPath());
        byte[] keyBytes = privateKey.getBytes();
    	
    	String keyString = new String(keyBytes);
        String privKeyPEM = keyString.replace("-----BEGIN RSA PRIVATE KEY-----", "");
        privKeyPEM = privKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
        privKeyPEM = privKeyPEM.replace("\r", "");
        privKeyPEM = privKeyPEM.replace("\n", "");
  
        keyBytes = Base64.getDecoder().decode(privKeyPEM);
  
        // We can't use Java internal APIs to parse ASN.1 structures, so we build a PKCS#8 key Java can understand
        int pkcs1Length = keyBytes.length;
        int totalLength = pkcs1Length + 22;
        byte[] pkcs8Header = new byte[] {
                0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff), // Sequence + total length
                0x2, 0x1, 0x0, // Integer (0)
                0x30, 0xD, 0x6, 0x9, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0, // Sequence: 1.2.840.113549.1.1.1, NULL
                0x4, (byte) 0x82, (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff) // Octet string + length
        };
        keyBytes = join(pkcs8Header, keyBytes);
  
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
    
    private static byte[] join(byte[] byteArray1, byte[] byteArray2){
        byte[] bytes = new byte[byteArray1.length + byteArray2.length];
        System.arraycopy(byteArray1, 0, bytes, 0, byteArray1.length);
        System.arraycopy(byteArray2, 0, bytes, byteArray1.length, byteArray2.length);
        return bytes;
   }
}
