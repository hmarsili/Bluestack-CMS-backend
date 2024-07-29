package com.vpode.test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.arabidopsis.ahocorasick.AhoCorasick;
import org.arabidopsis.ahocorasick.SearchResult;

public class TestAhoCorasick {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			
		String termsList = "hello,word,puta,putas";
		
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(termsList.getBytes());
			 
	        byte byteData[] = md.digest();
	        
	        md = MessageDigest.getInstance("MD5");
			md.update(termsList.getBytes());
	        byte byteData2[] = md.digest();

	        System.out.println(Arrays.equals(byteData,byteData2));;
	        
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		List<String> terms = new ArrayList(Arrays.asList(termsList.split(",")));
				
		AhoCorasick tree = createTree(terms);

	    String information = "Putas hello world computadora world";
	    
	    information = preproc(information);
	       
	    findWords(tree, information);
	}

	private static AhoCorasick createTree(List<String> terms) {
		AhoCorasick tree = new AhoCorasick();
		for (String term : terms)
			tree.add(term.getBytes(), term);
		
				tree.prepare();
				
				return tree;
	}

	private static void findWords(AhoCorasick tree, String information) {
		int size = information.length();
	       Iterator searcher = tree.search(information.getBytes());
	       while (searcher.hasNext()) {
	           SearchResult result = (SearchResult) searcher.next();
	           System.out.println(result.getOutputs());
	           
	           boolean isFullWord = true;
	           if (result.getLastIndex()<size) {
	        	   if (Character.isLetterOrDigit(information.charAt(result.getLastIndex())))
	        		   isFullWord = false;
	           }
	           int startAt = result.getLastIndex()- ((HashSet)result.getOutputs()).iterator().next().toString().length();
	           if (startAt>0) {
	        	   if (Character.isLetterOrDigit(information.charAt(startAt-1)))
	        		   isFullWord = false;	        	   
	           }
	           
	           System.out.println("Found " + (isFullWord ? "" : " false positive" ) + " at index: " + result.getLastIndex());
	       }
	}

	private static String preproc(String information) {
		String text = information.toLowerCase();
		return text;
	}

}
