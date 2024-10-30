package com.tfsla.diario.newsletters.service;

import java.security.InvalidParameterException;

import org.apache.commons.codec.binary.Base64;

import com.tfsla.diario.newsletters.common.NewsletterUnsubscribeRequest;

public class NewsletterUnsubscribeTokenManager {
	
	public static final String TOKEN_SEPPARATOR = "::";
	public static final String UNSUBSCRIBE_TOKEN_MACRO = "[[UNSUBSCRIBE_TOKEN]]";
	
	public String getUnsubscribeToken(String email, int newsletterID) {
		String formattedToken = String.format("%s" + TOKEN_SEPPARATOR + "%s", email, newsletterID);
		byte[] encoded = Base64.encodeBase64(formattedToken.getBytes());
		return new String(encoded);
	}
	
	public NewsletterUnsubscribeRequest decodeToken(String token) {
		String decoded = new String(Base64.decodeBase64(token.getBytes()));
		if (!decoded.contains(TOKEN_SEPPARATOR)) {
			throw new InvalidParameterException("Invalid token provided");
		}
		
		String[] params = decoded.split(TOKEN_SEPPARATOR);
		NewsletterUnsubscribeRequest ret = new NewsletterUnsubscribeRequest(); 
		ret.setEmail(params[0]);
		ret.setNewsletterID(Integer.parseInt(params[1]));
		return ret;
	}
}
