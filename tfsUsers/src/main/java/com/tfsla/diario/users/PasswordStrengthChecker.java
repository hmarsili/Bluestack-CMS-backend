package com.tfsla.diario.users;

import org.opencms.configuration.CPMConfig;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsUser;

import java.util.*;

public class PasswordStrengthChecker {
	
	protected final String moduleConf = "admin-security";
	protected CPMConfig config;

	public PasswordStrengthChecker(boolean useCmsConfiguration){
		if (useCmsConfiguration)
			config = CmsMedios.getInstance().getCmsParaMediosConfiguration();
	}

	public List<String> checkPasswordStrength(CmsUser user, String newPassword) {
		List<String> problems = new ArrayList<>();
		
		lengthChecker(newPassword, problems);
		typeCharacterChecker(newPassword, problems);
		
		List<String> words = new ArrayList<>();
		if (user.getName()!=null)
			words.add(user.getName());
		if (user.getFirstname()!=null)
			words.add(user.getFirstname());
		if (user.getLastname()!=null)
			words.add(user.getLastname());
		if (user.getEmail()!=null)
			words.add(user.getEmail());
		if (user.getZipcode()!=null)
			words.add(user.getZipcode());
		
		personalInformationChecker(newPassword, words.toArray(new String[0]), problems);
		
		return problems;
	}
	
	public PasswordStrengthChecker(){
		config = CmsMedios.getInstance().getCmsParaMediosConfiguration();	
	}
	
	protected void lengthChecker(String password, List<String> problems) {
		
		int min = config.getIntegerParam("", "", moduleConf, "passMinLength",6);
		int max = config.getIntegerParam("", "", moduleConf, "passMaxLength",64);
		
		lengthChecker(password, problems, min, max);
		
	}
	
	protected void lengthChecker(String password, List<String> problems, int min, int max) {
		
		
		if (password.length()>max)
			problems.add("MAX_LENGTH_VIOLATION");
		
		if (password.length()<min)
			problems.add("MIN_LENGTH_VIOLATION");
		
	}
	
	protected void typeCharacterChecker(String password, List<String> problems) {
		boolean mustContainNumbers = config.getBooleanParam("", "", moduleConf, "mustContainNumbers",true);
		boolean mustContainSpecialChars = config.getBooleanParam("", "", moduleConf, "mustContainSpecialChars",true);
		boolean mustContainLowerCaseChars = config.getBooleanParam("", "", moduleConf, "mustContainLowerCaseChars",true);
		boolean mustContainUpperCaseChars = config.getBooleanParam("", "", moduleConf, "mustContainUpperCaseChars",true);
		
		typeCharacterChecker(password, 
				problems, 
				mustContainNumbers,
				mustContainSpecialChars,
				mustContainLowerCaseChars,
				mustContainUpperCaseChars
			);
	}
	
	protected void typeCharacterChecker(String password, 
		List<String> problems, 
		boolean mustContainNumbers,
		boolean mustContainSpecialChars,
		boolean mustContainLowerCaseChars,
		boolean mustContainUpperCaseChars
	) {
		
		if (mustContainNumbers) {
			if (!password.matches("(?=.*[0-9]).*")) {
				problems.add("NO_NUMBER_VIOLATION");
			}
		}
		
		if (mustContainSpecialChars) {
			if (!password.matches(".*[^A-Za-z0-9].*")) {
				problems.add("NO_SPECIAL_CHARS_VIOLATION");
			}
		}
	
		if (mustContainLowerCaseChars) {
			if (!password.matches("(?=.*[a-z]).*")) {
				problems.add("NO_LOWER_CASE_VIOLATION");
			}
		}
	
		if (mustContainUpperCaseChars) {
			if (!password.matches("(?=.*[A-Z]).*")) {
				problems.add("NO_UPPER_CASE_VIOLATION");
			}
		}
	}

	protected String maxSubStringInPassword(String password, String value, int minLength) {
		int maxLen = 0;
		int maxStartAt = 0;
		
		int idx = 0;
		int posValue = 0;
		int findLen = 0;
		int posFind = 0;
		while (idx < password.length()) {
			if (posValue <  value.length() && password.charAt(idx)== value.charAt(posValue)) {
				if (posValue==0) {
					posFind = idx;
				}
				posValue++;
				findLen++; 
			}
			else {
				if (findLen>0) {
					idx = posFind;
					if (findLen > maxLen) {
						maxStartAt = posFind;
						maxLen = findLen;
					}
				}
				findLen = 0;
				posValue=0;
			}
			idx++;
		}
		if (findLen>0) {
			idx = posFind;
			if (findLen > maxLen) {
				maxStartAt = posFind;
				maxLen = findLen;
			}
			findLen = 0;
		}
		
		if (maxLen>=minLength) {
			return password.substring(maxStartAt,maxStartAt + maxLen);
		}
			
		return null;
	}
	
	protected void personalInformationChecker(String password, String[] words, List<String> problems ) {
		int minLength = config.getIntegerParam("", "", moduleConf, "passMinCoincidence",4);
		personalInformationChecker(password, words, minLength, problems );
	}
	
	protected void personalInformationChecker(String password, String[] words, int minLength, List<String> problems ) {
		boolean hasPersonalInfo = false;
		
		for (String word : words) {
			if (maxSubStringInPassword(password.toLowerCase(), word.toLowerCase(), minLength)!=null) {
				 hasPersonalInfo = true;
			}
		}
		
		if (hasPersonalInfo)
			problems.add("PERSONAL_INFORMATION_VIOLATION");
	}
	
	public static void main(String[] args) {
		boolean useCmsConfiguration = false;
		
		
		 
		boolean mustContainNumbers = true;
		boolean mustContainSpecialChars = true;
		boolean mustContainLowerCaseChars = true;
		boolean mustContainUpperCaseChars = true;
		int min = 8;
		int max = 64;
		
		String password = "Victor1!";
		String[] words = {"Victor Podberezski","vpode", "vdpode@gmail.com"};
		 
		List<String> problems = new ArrayList<>();
		 
		 PasswordStrengthChecker passChecker = new PasswordStrengthChecker(useCmsConfiguration);
		 
		 passChecker.lengthChecker(password, problems,  min, max);
		 
		 passChecker.typeCharacterChecker(password, 
					problems, 
					mustContainNumbers,
					mustContainSpecialChars,
					mustContainLowerCaseChars,
					mustContainUpperCaseChars
				);
		 
		 passChecker.personalInformationChecker(password, words, 4, problems );
		 
		 
		 for (String problem : problems) {
			 System.out.println(problem);
		 }
		 
		 for (String word : words) {
			 
			 System.out.println( passChecker.maxSubStringInPassword(password.toLowerCase(), word.toLowerCase(),  4));
		 }
		
	}
}
