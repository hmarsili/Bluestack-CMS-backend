package com.tfsla.utils;

import java.util.ArrayList;
import java.util.List;

public class TokenParserImpl implements TokenParser {

	/** El token que quiero detectar*/
	private String token;
	/** el token.toCharArray*/
	private char[] asArray;
	/** si tengo que agregar el token en los resultados*/
	private boolean addToken;

	/** indica la cantidad de caracteres que está macheando actualmente */
	private int matchCount = 0;
		
		
	public TokenParserImpl(String token, boolean addToken) {
		super();
		this.token = token;
		this.addToken = addToken;
		this.asArray = token.toCharArray();
	}

	public boolean addCharacter(char character) {
		if(isMatch(character)) {
			this.matchCount++;
			return this.matchCount == this.asArray.length;
		} 
		else {
			this.matchCount = 0;
			return false;
		}
	}

	private boolean isMatch(char character) {
		return this.asArray[this.matchCount] == character;
	}

	public List<String> parse(String string) {
		List<String> list = new ArrayList<String>();
		String element = string.substring(0, string.length() - this.asArray.length); 
		if(!element.equals("")) {
			if (this.addToken) {
			    element += this.token;
            }
            list.add(element);
		}
		return list;
	}

	public void newToken() {
		this.matchCount = 0;
	}

}
