package com.tfsla.opencms.webusers.openauthorization.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfiguration;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderConfigurationLoader;
import com.tfsla.opencms.webusers.openauthorization.common.ProviderField;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConfigurationException;

public class ProviderConfigurationLoaderTest {
	
	private ProviderConfigurationLoader loader;
	
	@Before
	public void setUp() throws Exception {
		CPMConfigDummy config = new CPMConfigDummy();
		this.loader = new ProviderConfigurationLoader();
		this.loader.setConfig(config);
	}

	@After
	public void tearDown() throws Exception {
		this.loader.setConfig(null);
	}

	@Test
	public void testGetFacebookConfiguration() {
		ProviderConfiguration configuration = null;
		try {
			configuration = this.loader.getConfiguration("webusers-facebook", "", "");
		} catch (InvalidConfigurationException e) {
			fail("Invalid configuration for webusers-facebook");
		}
		assertTrue(configuration.getFormat().equals("json"));
		assertTrue(configuration.getProviderName().equals("facebook"));
		assertTrue(configuration.getLocale().equals("es_AR"));
		assertTrue(configuration.getPriority() == 1);
		assertTrue(configuration.getFields().size() == 4);
		assertTrue(configuration.getFields().containsKey("username"));
		assertTrue(configuration.getFields().containsKey("email"));
		assertTrue(configuration.getFields().containsKey("gender"));
		assertTrue(configuration.getFields().containsKey("likes"));
		
		ProviderField providerField = null;
		providerField = configuration.getField("username");
		assertTrue(providerField.getName().equals("username"));
		assertTrue(providerField.getDescription().equals("username"));
		assertTrue(providerField.getEntryName().equals("USER_NAME"));
		assertTrue(providerField.getPath().equals("username"));
		assertTrue(providerField.getType().equals("string"));
		assertTrue(providerField.getProperty().equals("setNickName"));
		assertTrue(providerField.getForceWrite());
		assertNull(providerField.getConverter());
		assertFalse(providerField.isList());
		
		providerField = configuration.getField("email");
		assertTrue(providerField.getName().equals("email"));
		assertTrue(providerField.getDescription().equals("Email"));
		assertTrue(providerField.getEntryName().equals("USER_EMAIL"));
		assertTrue(providerField.getPath().equals("email"));
		assertTrue(providerField.getType().equals("string"));
		assertTrue(providerField.getProperty().equals("setEmail"));
		assertFalse(providerField.getForceWrite());
		assertNull(providerField.getConverter());
		assertFalse(providerField.isList());
		
		providerField = configuration.getField("gender");
		assertTrue(providerField.getName().equals("gender"));
		assertTrue(providerField.getEntryName().equals("USER_GENDER"));
		assertTrue(providerField.getPath().equals("gender"));
		assertTrue(providerField.getType().equals("string"));
		assertNull(providerField.getProperty());
		assertFalse(providerField.getForceWrite());
		assertTrue(providerField.getConverter().equals("com.tfsla.diario.webusers.services.FacebookGenderConverter"));
		assertNull(providerField.getConverterParameter());
		assertFalse(providerField.isList());
		
		providerField = configuration.getField("likes");
		assertTrue(providerField.getName().equals("likes"));
		assertTrue(providerField.getEntryName().equals("USER_LIKES"));
		assertTrue(providerField.getPath().equals("likes"));
		assertTrue(providerField.getType().equals("list"));
		assertNull(providerField.getProperty());
		assertFalse(providerField.getForceWrite());
		assertNull(providerField.getConverter());
		assertTrue(providerField.isList());
	}

	@Test
	public void testGetTwitterConfiguration() {
		ProviderConfiguration configuration = null;
		try {
			configuration = this.loader.getConfiguration("webusers-twitter", "", "");
		} catch (InvalidConfigurationException e) {
			fail("Invalid configuration for webusers-twitter");
		}
		assertTrue(configuration.getFormat().equals("json"));
		assertTrue(configuration.getProviderName().equals("twitter"));
		assertTrue(configuration.getPriority() == 2);
		assertTrue(configuration.getFields().size() == 2);
		assertTrue(configuration.getFields().containsKey("name"));
		assertTrue(configuration.getFields().containsKey("location"));
		
		ProviderField providerField = null;
		providerField = configuration.getField("name");
		assertTrue(providerField.getName().equals("name"));
		assertTrue(providerField.getEntryName().equals("USER_NAME"));
		assertTrue(providerField.getPath().equals("name"));
		assertTrue(providerField.getType().equals("string"));
		assertNull(providerField.getProperty());
		assertNull(providerField.getConverter());
		assertFalse(providerField.getForceWrite());
		
		providerField = configuration.getField("location");
		assertTrue(providerField.getName().equals("location"));
		assertTrue(providerField.getEntryName().equals("USER_LOCATION"));
		assertTrue(providerField.getPath().equals("location"));
		assertTrue(providerField.getType().equals("string"));
		assertNull(providerField.getProperty());
		assertTrue(providerField.getConverter().equals("com.tfsla.diario.webusers.services.TwitterLocationConverter"));
		assertNull(providerField.getConverterParameter());
		assertFalse(providerField.getForceWrite());
	}
	
	@Test
	public void testGetGooglePlusConfiguration() {
		ProviderConfiguration configuration = null;
		try {
			configuration = this.loader.getConfiguration("webusers-googlePlus", "", "");
		} catch (InvalidConfigurationException e) {
			fail("Invalid configuration for webusers-googlePlus");
		}
		assertTrue(configuration.getFormat().equals("json"));
		assertTrue(configuration.getPriority() == 3);
		assertTrue(configuration.getProviderName().equals("googlePlus"));
		assertTrue(configuration.getFields().size() == 2);
		assertTrue(configuration.getFields().containsKey("name"));
		assertTrue(configuration.getFields().containsKey("gender"));
		
		ProviderField providerField = null;
		providerField = configuration.getField("name");
		assertTrue(providerField.getName().equals("name"));
		assertTrue(providerField.getEntryName().equals("USER_NAME"));
		assertTrue(providerField.getPath().equals("name.formatted"));
		assertTrue(providerField.getType().equals("string"));
		assertNull(providerField.getProperty());
		assertNull(providerField.getConverter());
		assertFalse(providerField.getForceWrite());
		
		providerField = configuration.getField("gender");
		assertTrue(providerField.getName().equals("gender"));
		assertTrue(providerField.getEntryName().equals("USER_GENDER"));
		assertTrue(providerField.getPath().equals("gender"));
		assertTrue(providerField.getType().equals("string"));
		assertNull(providerField.getProperty());
		assertTrue(providerField.getConverter().equals("com.tfsla.diario.webusers.services.GooglePlusGenderConverter"));
		assertTrue(providerField.getConverterParameter().equals("parameter"));
		assertFalse(providerField.getForceWrite());
	}
	
	@Test(expected = InvalidConfigurationException.class)
	public void testInvalidConfiguration() throws InvalidConfigurationException {
		try {
			this.loader.getConfiguration("webusers-invalidConfiguration", "", "");
		} catch(InvalidConfigurationException e) {
			assertTrue(e.getModuleName().equals("webusers-invalidConfiguration"));
			throw e;
		}
	}
	
	@Test
	public void testGetConfiguredFields() {
		try {
			List<ProviderField> fields = this.loader.getConfiguredFields("", "");
			assertTrue(fields.size() == 5);
		} catch (InvalidConfigurationException e) {
			fail("Invalid configuration for webusers");
		}
	}
	
	@Test
	public void testGetConfiguredFacebookFields() {
		try {
			List<ProviderField> fields = this.loader.getConfiguredFields("", "", "webusers-facebook");
			assertTrue(fields.size() == 3);
		} catch (InvalidConfigurationException e) {
			fail("Invalid configuration for webusers-facebook fields");
		}
	}
	
	@Test
	public void testGetConfiguredLists() {
		try {
			List<ProviderField> fields = this.loader.getConfiguredLists("", "");
			assertTrue(fields.size() == 1);
		} catch (InvalidConfigurationException e) {
			fail("Invalid configuration for webusers");
		}
	}
}
