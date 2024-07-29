package com.tfsla.opencms.webusers.openauthorization.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.tfsla.opencms.webusers.openauthorization.common.IValueConverter;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidConversionException;
import com.tfsla.opencms.webusers.openauthorization.converters.RegexTableValueConverter;

public class RegexTableValueConverterTests {

	private IValueConverter valueConverter;
	private String tableFilePath;
	
	@Before
	public void setUp() throws Exception {
		this.valueConverter = new RegexTableValueConverter();
		this.tableFilePath = "/home/tfsuser/test-regex.txt";
	}

	@Test
	public void testConvert() throws InvalidConversionException {
		assertTrue(this.valueConverter.convert("true", this.tableFilePath, null).toString().equals("true"));
		assertTrue(this.valueConverter.convert("True", this.tableFilePath, null).toString().equals("true"));
		assertTrue(this.valueConverter.convert("buenos aires", this.tableFilePath, null).toString().equals("Buenos Aires"));
		assertTrue(this.valueConverter.convert("ciudad autónoma de buenos aires", this.tableFilePath, null).toString().equals("Buenos Aires"));
		assertTrue(this.valueConverter.convert("ciudad autonoma", this.tableFilePath, null).toString().equals("Buenos Aires"));
		assertTrue(this.valueConverter.convert("buenos aires, argentina", this.tableFilePath, null).toString().equals("Buenos Aires"));
		assertTrue(this.valueConverter.convert("CABA", this.tableFilePath, null).toString().equals("Buenos Aires"));
	}
	
	@Test
	public void testFail() throws InvalidConversionException {
		assertFalse(this.valueConverter.convert("mismatch", this.tableFilePath, null).toString().equals("mismatch"));
	}

}
