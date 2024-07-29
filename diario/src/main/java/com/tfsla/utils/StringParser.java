package com.tfsla.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StringParser implements Iterable<String> {
	
	private String string;
	private List<TokenParser> tokenParsers;
	private List<String> out;
	

	// *********************************
	// Factory Methods
	// *********************************
	
	public static StringParser newInstance(String string, String[] tokens, boolean withToken) {
		List<TokenParser> tokenParsers = new ArrayList<TokenParser>();
		for(String token: tokens) {
			tokenParsers.add(new TokenParserImpl(token, withToken));
		}
		return new StringParser(string, tokenParsers);
	}
	
	public static StringParser newInstance(String string, String[] tokens) {
		return newInstance(string, tokens, false);
	}
	
	public static StringParser newInstance(String string, String token, boolean withToken) {
		return newInstance(string, new String[]{token}, withToken);
	}
	
	public static StringParser newInstance(String string, String token) {
		return newInstance(string, token, false);
	}
	
	
	// *********************************
	// *********************************
	
	private StringParser(String string, List<TokenParser> tokenParsers) {
		super();
		this.tokenParsers = tokenParsers;
		this.string =string;
	}
	
	public synchronized Iterator<String> iterator() {
		return this.getOut().iterator();
	}

	public int size() {
		return this.getOut().size();
	}
	
	public List<String> getOut() {
		if(this.out == null) {
			this.out = this.parse();
		}
		return this.out;
	}
	
	//Cada caracter lo va guardando en la cadena actual
	//manda a cada TokenParser el nuevo caracter, y si alguno le dice que está en condiciones
	//de parsearlo, entonces le dice a ese tokenParser que parsee la cadena actual
	//Una vez parseada la cadena actual, inicializa de nuevo la cadena actual, y le avisa a todos los
	//TokenParser que ya se puede empezar de nuevo

	private List<String> parse() {
		List<String> parsed = new ArrayList<String>();
		StringBuffer actual = new StringBuffer();
		
		for(char character : this.string.toCharArray()) {
			actual.append(character);
			for (TokenParser tokenParser : this.tokenParsers) {
				if(tokenParser.addCharacter(character)) {
					parsed.addAll(tokenParser.parse(actual.toString()));
					this.resetTokenParser();
					actual = new StringBuffer();
					break;
				}
			}
		}
		this.addLast(actual, parsed);
		return parsed;
	}
	
	/**
	 * Agrega el ultimo elemento, porque de esto nos e encargan los TokenParser 
	 * @param actual
	 * @param parsed
	 */
	private void addLast(StringBuffer actual, List<String> parsed) {
		if(actual.length() > 0) {
			parsed.add(actual.toString());
		}
	}

	private void resetTokenParser() {
		for (TokenParser tokenParser : this.tokenParsers) {
			tokenParser.newToken();
		}
	}


}
