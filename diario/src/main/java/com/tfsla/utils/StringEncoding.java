package com.tfsla.utils;

import java.io.UnsupportedEncodingException;

public class StringEncoding {
	
	
	public static String fixEncoding(String latin1) throws UnsupportedEncodingException{
		 
		try {
			byte[] bytes = latin1.getBytes("ISO-8859-1");
			String fixed = new String(bytes, "UTF-8");
			   
			if (!validUTF8(bytes) || fixed.length()==latin1.length()){
			    return latin1;   
			}
			    
			return fixed;
		   
		} catch (UnsupportedEncodingException e) {
		   throw new IllegalStateException("No Latin1 or UTF-8: " + e.getMessage());
		}

	}

	public static boolean validUTF8(byte[] input){
		  
		int i = 0;
		
		if (input.length >= 3 && (input[0] & 0xFF) == 0xEF
		    && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
		   i = 3;
		}

		int end;
		
		for (int j = input.length; i < j; ++i){
		   int octet = input[i];
		   if ((octet & 0x80) == 0) {
		    continue; 
		   }

		   if ((octet & 0xE0) == 0xC0) {
		    end = i + 1;
		   } else if ((octet & 0xF0) == 0xE0) {
		    end = i + 2;
		   } else if ((octet & 0xF8) == 0xF0) {
		    end = i + 3;
		   } else {
		    return false;
		   }

		   while (i < end) {
			    i++;
			    
			    if(input.length >=i) {
			    	return true;
			    }else {
				    octet = input[i];
				    
				    if ((octet & 0xC0) != 0x80) {
				     // Not a valid trailing byte
				     return false;
				    }
			    }
			}
		   
		  }
		
		  return true;
	}

}
