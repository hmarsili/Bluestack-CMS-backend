package com.tfsla.opencms.webusers.openauthorization.tests;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.sf.json.JSONObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfiguration;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderField;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderListField;
import com.tfsla.opencms.webusers.openauthorization.common.UserProfileDataManager;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConversionException;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidFormatException;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidPathException;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidPropertyException;

public class UserProfileDataManagerTests {

	private JSONObject jsonObject;
	
	@Before
	public void setUp() throws Exception {
		String jsonString = ""
				+ "{"
				+ "\"name\": \"Nombre\","
				+ "\"email\": \"em@il.com\","
				+ "\"empty\": null,"
				+ "\"birthdate\": \"2013-01-01\","
				+ "\"location\":"
				+ "	{"
				+ "		\"address\": \"Direccion 1234\","
				+ "		\"country\": \"Argentina\","
				+ "		\"state\": "
				+ "		{"
				+ "			\"name\": \"Buenos Aires\","
				+ "			\"code\": \"BA\""
				+ "		}"
				+ "	},"
				+ "\"languages\":["
				+ "		{\"id\": \"1\", \"name\": \"spanish\"},"
				+ "		{\"id\": \"2\", \"name\": \"english\"}"
				+ "	 ],"
				+ "\"compose\": {"
				+ "		\"languages\":["
				+ "			{\"id\": \"1\", \"name\": \"spanish\"},"
				+ "			{\"id\": \"2\", \"name\": \"english\"}"
				+ "	 	]"
				+ "	 }"
				+ "}";
		this.jsonObject = JSONObject.fromObject(jsonString);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUserProfileDataProperty() throws InvalidPropertyException, InvalidConversionException, InvalidPathException, InvalidFormatException {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("json");
		config.addField("name", new ProviderField() {{
			setName("name");
			setEntryName("");
			setPath("name");
			setProperty("setNickName");
			setType("string");
			setConverter("");
		}});
		config.addField("email", new ProviderField() {{
			setName("email");
			setEntryName("");
			setPath("email");
			setProperty("setEmail");
			setType("string");
			setConverter("");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
		
		assertTrue(profileData.getNickName().equals("Nombre"));
		assertTrue(profileData.getEmail().equals("em@il.com"));
	}
	
	@Test
	public void testUserProfileDataCompossedProperty() throws InvalidPropertyException, InvalidConversionException, InvalidPathException, InvalidFormatException {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("json");
		config.addField("address", new ProviderField() {{
			setName("address");
			setEntryName("USER_ADDRESS");
			setPath("location.address");
			setProperty("");
			setType("string");
			setConverter("");
		}});
		config.addField("state", new ProviderField() {{
			setName("state");
			setEntryName("USER_STATE");
			setPath("location.state.name");
			setProperty("");
			setType("string");
			setConverter("");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
		
		Object address = profileData.getAdditionalInfo("USER_ADDRESS");
		assertTrue(address.toString().equals("Direccion 1234"));
		Object state = profileData.getAdditionalInfo("USER_STATE");
		assertTrue(state.toString().equals("Buenos Aires"));
	}
	
	@Test
	public void testUserProfileDataAdditionalInfo() throws InvalidPropertyException, InvalidConversionException, InvalidPathException, InvalidFormatException {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("json");
		config.addField("name", new ProviderField() {{
			setName("name");
			setEntryName("USER_NAME");
			setPath("name");
			setProperty("");
			setType("string");
			setConverter("");
		}});
		config.addField("email", new ProviderField() {{
			setName("email");
			setEntryName("USER_EMAIL");
			setPath("email");
			setProperty("");
			setType("string");
			setConverter("");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
		
		Object profileInfoName = profileData.getAdditionalInfo("USER_NAME");
		assertTrue(profileInfoName.toString().equals("Nombre"));
		Object email = profileData.getAdditionalInfo("USER_EMAIL");
		assertTrue(email.toString().equals("em@il.com"));
	}

	@Test
	public void testUserProfileDataMixed() throws InvalidPropertyException, InvalidConversionException, InvalidPathException, InvalidFormatException {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("json");
		config.addField("name", new ProviderField() {{
			setName("name");
			setEntryName("USER_NAME");
			setPath("name");
			setProperty("setNickName");
			setType("string");
			setConverter("");
		}});
		config.addField("email", new ProviderField() {{
			setName("email");
			setEntryName("USER_EMAIL");
			setPath("email");
			setProperty("setEmail");
			setType("string");
			setConverter("");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
		
		Object profileInfoName = profileData.getAdditionalInfo("USER_NAME");
		assertTrue(profileInfoName.toString().equals("Nombre"));
		assertTrue(profileData.getNickName().equals("Nombre"));
		Object email = profileData.getAdditionalInfo("USER_EMAIL");
		assertTrue(email.toString().equals("em@il.com"));
		assertTrue(profileData.getEmail().equals("em@il.com"));
	}
	
	@Test
	public void testValueConverter() throws InvalidPropertyException, InvalidConversionException, InvalidPathException, ParseException, InvalidFormatException {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("json");
		config.addField("stateCode", new ProviderField() {{
			setName("state");
			setEntryName("USER_STATE");
			setPath("location.state.code");
			setProperty("");
			setType("string");
			setConverter("com.tfsla.opencms.webusers.openauthorization.tests.CountryCodeValueConverter");
		}});
		config.addField("birthdate", new ProviderField() {{
			setName("birthdate");
			setEntryName("USER_BIRTHDATE");
			setPath("birthdate");
			setProperty("");
			setType("date");
			setConverter("com.tfsla.opencms.webusers.openauthorization.tests.DateValueConverter");
			setConverterParameter("yyyy-MM-dd");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
		
		Object stateCode = profileData.getAdditionalInfo("USER_STATE");
		assertTrue(stateCode.toString().equals("ba"));
		Object birthdate = profileData.getAdditionalInfo("USER_BIRTHDATE");
		Date date = new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2013");
		assertTrue(date.compareTo((Date) birthdate) == 0);
	}
	
	@Test
	public void testLists() throws InvalidPropertyException, InvalidConversionException, InvalidPathException, ParseException, InvalidFormatException {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("json");
		config.addField("languages", new ProviderField() {{
			setName("languages");
			setEntryName("USER_LANGUAGES");
			setPath("languages");
			setProperty("");
			setType("list");
			setListIdField("id");
			setListValueField("name");
		}});
		config.addField("composeLanguages", new ProviderField() {{
			setName("composeLanguages");
			setEntryName("COMPOSE_USER_LANGUAGES");
			setPath("compose.languages");
			setProperty("");
			setType("list");
			setListIdField("id");
			setListValueField("name");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
		
		ArrayList<ProviderListField> languages = profileData.getList("USER_LANGUAGES");
		assertTrue(languages.size() == 2);
		for(ProviderListField language : languages) {
			assertTrue(language.getId().equals("1") || language.getId().equals("2"));
			assertTrue(language.getValue().equals("spanish") || language.getValue().equals("english"));
		}
		
		languages = profileData.getList("COMPOSE_USER_LANGUAGES");
		assertTrue(languages.size() == 2);
		for(ProviderListField language : languages) {
			assertTrue(language.getId().equals("1") || language.getId().equals("2"));
			assertTrue(language.getValue().equals("spanish") || language.getValue().equals("english"));
		}
	}
	
	@Test(expected = InvalidPropertyException.class)
	public void testInvalidPropertyName() throws InvalidPropertyException, InvalidConversionException, InvalidPathException, InvalidFormatException  {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("json");
		config.addField("name", new ProviderField() {{
			setName("name");
			setEntryName("");
			setPath("name");
			setProperty("setInvalidProperty");
			setType("string");
			setConverter("");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
	}
	
	//@Test(expected = InvalidPathException.class)
	//Will need to handle this exception to avoid the whole manager to fail
	@Test
	public void testInvalidPath() throws InvalidPathException, InvalidConversionException, InvalidPathException, InvalidPropertyException, InvalidFormatException  {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("json");
		config.addField("name", new ProviderField() {{
			setName("name");
			setEntryName("");
			setPath("invalidPath");
			setProperty("name");
			setType("string");
			setConverter("");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
	}
	
	//@Test(expected = InvalidConversionException.class)
	//Will need to handle this exception to avoid the whole manager to fail
	@Test
	public void testInvalidConverter() throws InvalidPathException, InvalidConversionException, InvalidPathException, InvalidPropertyException, InvalidFormatException  {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("json");
		config.addField("name", new ProviderField() {{
			setName("name");
			setEntryName("");
			setPath("name");
			setProperty("name");
			setType("string");
			setConverter("invalidconverter");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
	}
	
	//@Test(expected = InvalidConversionException.class)
	//Will need to handle this exception to avoid the whole manager to fail
	@Test
	public void testInvalidConversion() throws InvalidPathException, InvalidConversionException, InvalidPathException, InvalidPropertyException, InvalidFormatException  {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("json");
		config.addField("empty", new ProviderField() {{
			setName("empty");
			setEntryName("");
			setPath("empty");
			setProperty("empty");
			setType("string");
			setConverter("com.tfsla.opencms.webusers.openauthorization.tests.DateValueConverter");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
	}
	
	@Test(expected = InvalidFormatException.class)
	public void testInvalidFormat() throws InvalidPathException, InvalidConversionException, InvalidPathException, InvalidPropertyException, InvalidFormatException  {
		ProviderConfiguration config = new ProviderConfiguration();
		config.setFormat("unknown");
		config.addField("empty", new ProviderField() {{
			setName("empty");
			setEntryName("");
			setPath("empty");
			setProperty("empty");
			setType("string");
			setConverter("");
		}});
		
		UserProfileData profileData = new UserProfileData();
		profileData.setProviderResponse(this.jsonObject);
		UserProfileDataManager dataManager = UserProfileDataManager.getInstance(config, profileData);
		dataManager.updateUserProfileData();
	}
}
