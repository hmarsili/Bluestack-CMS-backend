package com.tfsla.rankViews.service;

public class TfsTokenHelper {
	static public String convert(String word) {
		//word= word.replaceAll("Ã¡", "á");
		//word= word.replaceAll("Ã", "Á");
		//word= word.replaceAll("Ã©", "é");
		//word= word.replaceAll("Ã?", "É");
		//word= word.replaceAll("Ã­", "í");
		//word= word.replaceAll("Ã", "Í");
		//word= word.replaceAll("Ã³", "ó");
		//word= word.replaceAll("Ã?", "Ó");
		//word= word.replaceAll("Ãº", "ú");
		//word= word.replaceAll("Ã?", "Ú");
		//word= word.replaceAll("Ã±", "ñ");
		//word= word.replaceAll("Ã?", "Ñ");

		word= word.replaceAll("á", "a");
		word= word.replaceAll("Á", "A");
		word= word.replaceAll("é", "e");
		word= word.replaceAll("É", "E");
		word= word.replaceAll("í", "i");
		word= word.replaceAll("Í", "I");
		word= word.replaceAll("ó", "o");
		word= word.replaceAll("Ó", "O");
		word= word.replaceAll("ú", "u");
		word= word.replaceAll("Ú", "U");

		word= word.replaceAll("\\:","");
		word= word.replaceAll("\\!","");
		word= word.replaceAll("\\¡","");
		word= word.replaceAll("\\?","");
		word= word.replaceAll("\\¿","");
		word= word.replaceAll("\\\"","");
		word= word.replaceAll("\\\'","");
		word= word.replaceAll("\\,","");
		word= word.replaceAll("\\@","");


		word=word.trim();
		word=word.toLowerCase();
		
		return word;
	}
}
