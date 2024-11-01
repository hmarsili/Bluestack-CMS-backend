package com.alkacon.opencms.commons;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.logging.Log;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import java.util.Base64;

public final class CmsStringCrypter {
   private static final String ENCRYPTION = "DES";
   private static final String FORMAT = "UTF8";
   private static final Log LOG = CmsLog.getLog(CmsStringCrypter.class);
   private static final String PASSWORD_DEFAULT = "fuZe-6jK";

   private CmsStringCrypter() {
   }

   public static String decrypt(String value) {
      return decrypt(value, "fuZe-6jK");
   }

   public static String decrypt(String value, String password) {
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
         if (LOG.isWarnEnabled()) {
            LOG.warn(Messages.get().getBundle().key("LOG_WARN_INVALID_DECRYPT_STRING_1", value));
         }

         return null;
      } else {
         try {
            Key key = new SecretKeySpec(getKey(password), "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(2, key);
            
            byte[] cleartext = Base64.getDecoder().decode(value);
            byte[] ciphertext = cipher.doFinal(cleartext);
            return CmsEncoder.decode(new String(ciphertext));
         } catch (Exception var7) {
            if (LOG.isErrorEnabled()) {
               LOG.error(Messages.get().getBundle().key("LOG_ERROR_DECRPYT_0"), var7);
            }

            return null;
         }
      }
   }

   public static String encrypt(String value) {
      return encrypt(value, "fuZe-6jK");
   }

   public static String encrypt(String value, String password) {
      if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
         if (LOG.isWarnEnabled()) {
            LOG.warn(Messages.get().getBundle().key("LOG_WARN_INVALID_ENCRYPT_STRING_1", value));
         }

         return null;
      } else {
         try {
            byte[] k = getKey(password);
            Key key = new SecretKeySpec(k, "DES");
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(1, key);
            byte[] cleartext = value.getBytes("UTF8");
            byte[] ciphertext = cipher.doFinal(cleartext);
            return CmsEncoder.encode(new String(Base64.getEncoder().encode(ciphertext)));
         } catch (Exception var8) {
            if (LOG.isErrorEnabled()) {
               LOG.error(Messages.get().getBundle().key("LOG_ERROR_ENCRYPT_0"), var8);
            }

            return null;
         }
      }
   }

   private static byte[] getKey(String password) {
      try {
         MessageDigest md5 = MessageDigest.getInstance("MD5");
         md5.update(password.toString().getBytes());
         byte[] key = md5.digest();
         byte[] finalKey = new byte[8];

         for(int i = 0; i <= 7; ++i) {
            finalKey[i] = key[i];
         }

         return finalKey;
      } catch (NoSuchAlgorithmException var5) {
         if (LOG.isErrorEnabled()) {
            LOG.error(Messages.get().getBundle().key("LOG_ERROR_CREATE_KEY_0"), var5);
         }

         return null;
      }
   }
}
