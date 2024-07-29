package com.tfsla.cdnIntegration.util;

import java.net.URI;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.opencms.main.CmsLog;

public class EdgeGridV1Signer {

	 /** Name of the EdgeGrid signing algorithm. */

    private static final String ALGORITHM_NAME = "EG1-HMAC-SHA256";

    /** Pre-compiled regex to match multiple spaces. */

    private static final Pattern PATTERN_SPACES = Pattern.compile("\\s+");

    private static final String AUTH_CLIENT_TOKEN_NAME = "client_token";

    private static final String AUTH_ACCESS_TOKEN_NAME = "access_token";

    private static final String AUTH_TIMESTAMP_NAME = "timestamp";

    private static final String AUTH_NONCE_NAME = "nonce";

    private static final String AUTH_SIGNATURE_NAME = "signature";

    /** Message digest algorithm. */

    private static final String DIGEST_ALGORITHM = "SHA-256";



    /** Message signing algorithm. */

    private static final String SIGNING_ALGORITHM = "HmacSHA256";

    private static final Log LOG = CmsLog.getLog(EdgeGridV1Signer.class);


    /**

     * Creates signer with default configuration.

     */

    public EdgeGridV1Signer() {

    }



    public String getSignature(HttpPost request, ClientCredential credential)

            throws Exception {

        return getSignature(request, credential, System.currentTimeMillis(), generateNonce());

    }



    private static String generateNonce() {

        return UUID.randomUUID().toString();

    }



    private static String getAuthorizationHeaderValue(String authData, String signature) {

        return authData + AUTH_SIGNATURE_NAME + '=' + signature;

    }



    private static String getRelativePathWithQuery(URI uri) {

        StringBuilder sb = new StringBuilder(uri.getPath());

        if (uri.getQuery() != null) {

            sb.append("?").append(uri.getQuery());

        }

        return sb.toString();

    }



    private static byte[] sign(String s, String clientSecret) throws Exception {

        return sign(s, clientSecret.getBytes("UTF-8"));

    }



    private static byte[] sign(String s, byte[] key) throws Exception {

        try {

            SecretKeySpec signingKey = new SecretKeySpec(key, SIGNING_ALGORITHM);

            Mac mac = Mac.getInstance(SIGNING_ALGORITHM);

            mac.init(signingKey);



            byte[] valueBytes = s.getBytes("UTF-8");

            return mac.doFinal(valueBytes);

        } catch (NoSuchAlgorithmException e) {

            throw new Exception("Failed to sign: your JDK does not recognize signing algorithm <" + SIGNING_ALGORITHM +">", e);

        } catch (InvalidKeyException e) {

            throw new Exception("Failed to sign: invalid key", e);

        }

    }



    private static String formatTimeStamp(long time) {

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ssZ");

        Date date = new Date(time);

        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        return format.format(date);

    }



    private static String canonicalizeUri(String uri) {

        if (StringUtils.isEmpty(uri)) {

            return "/";

        }



        if (uri.charAt(0) != '/') {

            uri = "/" + uri;

        }



        return uri;

    }



    String getSignature(HttpPost request, ClientCredential credential, long timestamp, String nonce)

            throws Exception {

        Validate.notNull(credential, "credential cannot be null");

        Validate.notNull(request, "request cannot be null");



        String timeStamp = formatTimeStamp(timestamp);

        String authData = getAuthData(credential, timeStamp, nonce);

        String signature = getSignature(request, credential, timeStamp, authData);

        LOG.debug("Signature: {}" + signature);



        return getAuthorizationHeaderValue(authData, signature);

    }



    private String getSignature(HttpPost request, ClientCredential credential, String timeStamp,

                                String authData) throws Exception {

        String signingKey = getSigningKey(timeStamp, credential.getClientSecret());

        String canonicalizedRequest = getCanonicalizedRequest(request, credential);

        LOG.debug("Canonicalized request: {}" + StringEscapeUtils.escapeJava(canonicalizedRequest));

        String dataToSign = getDataToSign(canonicalizedRequest, authData);

        LOG.debug("Data to sign: {}" + StringEscapeUtils.escapeJava(dataToSign));



        return signAndEncode(dataToSign, signingKey);

    }



    private String signAndEncode(String stringToSign, String signingKey) throws Exception {

        byte[] signatureBytes = sign(stringToSign, signingKey);

        return new String(Base64.encodeBase64(signatureBytes));

    }



    private String getSigningKey(String timeStamp, String clientSecret) throws Exception {

        byte[] signingKeyBytes = sign(timeStamp, clientSecret);

        return new String(Base64.encodeBase64(signingKeyBytes));

    }



    private String getDataToSign(String canonicalizedRequest, String authData) {

        return canonicalizedRequest + authData;

    }



    private String getAuthData(ClientCredential credential, String timeStamp, String nonce) {

        StringBuilder sb = new StringBuilder();

        sb.append(ALGORITHM_NAME);

        sb.append(' ');

        sb.append(AUTH_CLIENT_TOKEN_NAME);

        sb.append('=');

        sb.append(credential.getClientToken());

        sb.append(';');



        sb.append(AUTH_ACCESS_TOKEN_NAME);

        sb.append('=');

        sb.append(credential.getAccessToken());

        sb.append(';');



        sb.append(AUTH_TIMESTAMP_NAME);

        sb.append('=');

        sb.append(timeStamp);

        sb.append(';');



        sb.append(AUTH_NONCE_NAME);

        sb.append('=');

        sb.append(nonce);

        sb.append(';');

        return sb.toString();

    }





    private String getCanonicalizedRequest(HttpPost request, ClientCredential credential) throws Exception {

        StringBuilder sb = new StringBuilder();

        sb.append(request.getMethod().toUpperCase());

        sb.append('\t');



        // all OPEN APIs use HTTPS, not HTTP

        String scheme = "https";

        sb.append(scheme);

        sb.append('\t');



        String host = credential.getHost();

        sb.append(host.toLowerCase());

        sb.append('\t');



        String relativePath = getRelativePathWithQuery(request.getURI());

        String relativeUrl = canonicalizeUri(relativePath);

        sb.append(relativeUrl);

        sb.append('\t');



        String canonicalizedHeaders = canonicalizeHeaders(request.getAllHeaders(), credential);

        sb.append(canonicalizedHeaders);

        sb.append('\t');



        sb.append(getContentHash(request.getMethod(), IOUtils.toString( request.getEntity().getContent()).getBytes(), credential.getMaxBodySize()));

        sb.append('\t');



        return sb.toString();

    }





    private byte[] getHash(byte[] requestBody, int offset, int len) throws Exception {

        try {

            MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);

            md.update(requestBody, offset, len);

            return md.digest();

        } catch (NoSuchAlgorithmException e) {

            throw new Exception("Failed to get request hash: your JDK does not recognize algorithm <" + DIGEST_ALGORITHM +">", e);

        }

    }



    private String canonicalizeHeaders(Header[] headers2, ClientCredential credential) {

        List<String> headers = new ArrayList<String>();

        // NOTE: Headers are expected to be in order. ClientCredential#headersToSign is a TreeSet.

        for (String headerName : credential.getHeadersToSign()) {

        	String headerValue = "";
        	for (Header headerRequest : headers2) {
        		if (headerRequest.getName().equals(headerName)) {
        			headerValue = headerRequest.getValue();
        		}
        	}
            

            if (StringUtils.isBlank(headerValue)) {

                continue;

            }

            headers.add(headerName.toLowerCase() + ":" + canonicalizeHeaderValue(headerValue));

        }

        return StringUtils.join(headers.iterator(), "\t");

    }



    private String canonicalizeHeaderValue(String headerValue) {

        headerValue = headerValue.trim();

        if (StringUtils.isNotBlank(headerValue)) {

            Matcher matcher = PATTERN_SPACES.matcher(headerValue);

            headerValue = matcher.replaceAll(" ");

        }

        return headerValue;

    }



    private String getContentHash(String requestMethod, byte[] requestBody, int maxBodySize)

            throws Exception {

        // only do hash for POSTs for this version

        if (!"POST".equals(requestMethod)) {

            return "";

        }



        if (requestBody == null || requestBody.length == 0) {

            return "";

        }



        int lengthToHash = requestBody.length;

        if (lengthToHash > maxBodySize) {

            LOG.info("Content length '" + lengthToHash + "' exceeds signing length of '" + maxBodySize + "'. Less than the entire message will be signed.");

            lengthToHash = maxBodySize;

        } else {

            if (LOG.isTraceEnabled()) {

                LOG.trace("Content (Base64): " + new String(Base64.decodeBase64(requestBody)));

            }

        }



        byte[] digestBytes = getHash(requestBody, 0, lengthToHash);

        LOG.debug("Content hash (Base64): " + new String(Base64.encodeBase64(digestBytes)));

        // (mgawinec) I removed support for non-retryable content, that used to reset the content for downstream handlers

        return new String(Base64.encodeBase64(digestBytes));

    }
}
