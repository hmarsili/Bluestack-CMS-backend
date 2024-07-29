package com.tfsla.diario.ediciones.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.opencms.configuration.CmsMedios;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.IOException;


import com.google.cloud.translate.Detection;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentialsUtils;

public class GoogleCloudTranslatorService {

	private static final Log LOG = CmsLog.getLog(GoogleCloudTranslatorService.class);
	private static Map<String, GoogleCloudTranslatorService> instances = new HashMap<String, GoogleCloudTranslatorService>();


	protected CmsObject cmsObject = null;
	protected String siteName;
	protected String publication;


	public static GoogleCloudTranslatorService getInstance(CmsObject cms) {
		String siteName = OpenCms.getSiteManager().getCurrentSite(cms).getSiteRoot();
		String publication = "0";
		try {
			publication = String.valueOf(PublicationService.getPublicationId(cms));
		} catch (Exception e) {
			LOG.error(e);
		}

		String id = siteName + "||" + publication;

		GoogleCloudTranslatorService instance = instances.get(id);

		if (instance == null) {
			instance = new GoogleCloudTranslatorService(cms,siteName, publication);

			instances.put(id, instance);
		}

		instance.cmsObject = cms;


		return instance;
	}

	public GoogleCloudTranslatorService() {}

	public GoogleCloudTranslatorService(CmsObject cmsObject, String siteName, String publication) {
		this.siteName = siteName;
		this.publication = publication;
	}

	protected String getModuleName() {
		return "googleCloud";
	}

	public boolean isTranslatorEnabled() {
		return CmsMedios.getInstance().getCmsParaMediosConfiguration().getBooleanParam(siteName, publication, getModuleName(), "translatorEnabled", false);
	}

	public String getClientId() {
		return (client_id!=null ? client_id : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "clientId", ""));
	}

	public String getClientEmail() {
		return (client_email!=null ? client_email : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "clientEmail", ""));
	}

	public String getPrivateKey() {
		return (private_key!=null ? private_key :
			org.apache.commons.lang.StringEscapeUtils.unescapeJava(
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "privateKey", "")
			)
			);
	}

	public String getPrivateKeyId() {
		return (private_key_id!=null ? private_key_id : 
			
					CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "privateKeyId", "")
				);
		
	}

	public String getLanguage() {
		return (language!=null ? language : 
			CmsMedios.getInstance().getCmsParaMediosConfiguration().getParam(siteName, publication, getModuleName(), "language", ""));
	}

	private String client_id = null;
	private String client_email = null;
	private String private_key = null;
	private String private_key_id = null;
	private String language = null;
	
	private Translate translate = null;
	public Translate createTranslateService() throws IOException {
		
		//return TranslateOptions.newBuilder().setApiKey("AIzaSyCCzZlXl0hAma-N4PoYSVCp29B206AJvR4").build().getService();
		if (translate!=null)
			return translate;
				
		GoogleCredentials credentials = new ServiceAccountCredentialsUtils()
				.setClientId(getClientId())
				.setClientEmail(getClientEmail())
				.setPrivateKeyId(getPrivateKeyId())
				.setPrivateKey(getPrivateKey())
				.getServiceAccountCredentials();
				
		
		translate = TranslateOptions.newBuilder()
				.setCredentials(
						credentials
				)
				.build().getService();
				
		return translate;
	}

	public String detectLanguage(String text) throws IOException {
		Translate translate = createTranslateService();

		Detection detection = translate.detect(text);
		return detection.getLanguage();
	}

	public String defaultTranslate(String text) throws IOException {

		Translate translate = createTranslateService();
		String detectedLanguage = detectLanguage(text);
		Translation translation = translate.translate(
				text,
				TranslateOption.sourceLanguage(detectedLanguage),
				TranslateOption.targetLanguage(getLanguage()));

		return translation.getTranslatedText();

	}

	public String defaultTranslate(String text, String sourceLanguage) throws IOException {

		Translate translate = createTranslateService();
		Translation translation = translate.translate(
				text,
				TranslateOption.sourceLanguage(sourceLanguage),
				TranslateOption.targetLanguage(getLanguage()));

		return translation.getTranslatedText();

	}

	public String translate(String text, String sourceLanguage) throws IOException {

		Translate translate = createTranslateService();
		String detectedLanguage = detectLanguage(text);
		Translation translation = translate.translate(
				text,
				TranslateOption.sourceLanguage(sourceLanguage),
				TranslateOption.targetLanguage(detectedLanguage));

		return translation.getTranslatedText();

	}

	public String translate(String text, String sourceLanguage, String targetLanguage) throws IOException {

		Translate translate = createTranslateService();
		
		Translation translation = translate.translate(
				text,
				TranslateOption.sourceLanguage(sourceLanguage),
				TranslateOption.targetLanguage(targetLanguage));

		return translation.getTranslatedText();

	}

	public static void main(String [] args)
	{
		Date startDate = new Date();
		GoogleCloudTranslatorService translatorService = new GoogleCloudTranslatorService();
		
		translatorService.client_id = "111795177617689448869";
		translatorService.client_email = "translator@peppy-flame-171519.iam.gserviceaccount.com";
		translatorService.private_key = "-----BEGIN PRIVATE KEY-----\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC0KngM3Dv0AOT1\nHOZ9X+G++8VRcKEBYuKlOkuIjPD5SsjTaJPa02SlZDAZH0ktQaPeOCq5+0FC30l3\n5DheKXSpoijxUqOfJtLA+yVLK7xwuRuoM8JF+2AwKWBChT1Uu/4klxm7Dk1ZbDlH\ntVqReujC2Tw8KZQF8az/dyjncrjqo92sYEVNTUjfa9CPl4KIi0tnZGwmFRHfz777\nyVdldVBmvXQ35gAq487yp/UFK9KpfWNCliwXZim0EXR8Npj1DqdJeoPs5daSEXj7\n1v1p1SQ+NQPVbLBx57H2OkflKxbM7b4wVMMTEo7iISC5fGUUHpAu0JFdZ5l9OXS1\nGa8scrq1AgMBAAECggEACFL+APob+wYfjgOr0pcfgwd28nBVwA6lkiTDYKfCaoI+\nDSaDzlR+DfY3ErITW3fw7k+CCd3GAiT27AcrY45eyYeohJ3g2XWdvzTbYEHBDyQ+\nVuMtUrZul0sSG4C0eLnG4EHq+m2KvVr2uo1yhc47+uS2l8EECdJEv/jtduQiDxU3\nLbTOKsFqyIt989+fP7vRVUUoVhiiEYQS1GaPX6tMqVqvHVxz+gX0vEctFmM4uu6x\niThNIviKkj5TMffsjqpmaJOqaMm+/XMiwIDW1rmrFZE9TDPKnm8952Lexej1xb0g\nU43dw0J2tTlqKIStd4K5qW6TY+06QH89CTyiBoyH8QKBgQDvqkX+lLr17HxeOiHl\nK5O7G78XRXxNYAWOoILfy1ERnVdtxfl3tvjM6MVTyAAC+cI0EHudWzuFJa2f+IXl\nKSYOKwNuxdHKwb9ptV/j6fP6+5IlDOcHKwkdznU7Mrq4ivEdLIt9mt1mcpoGOKRa\nQG1nBHHigSiqJw09Vct8B6UwVwKBgQDAcgpkhIcIuOopre294fMTwR7wgHj3C0ab\n21Os2Hpz00LQq8ntBSu0aD5/oJGtWTTjc44DzklJWeWRh9KlCTTvcM4UcByE8vka\nIJ+8iaTQC9L8wlfE0bPwl/qjHq2a5sWrcNT/CJcQHQCPr/LtulABP1EWIYUXbxJi\nnmdJiFlV0wKBgQCt+MdIbwRsQhdhewdhyMk7WH/VT75UmHBQQVnfREJ76+AsMjrN\n2QcwD4Q2Ngc0IcMEcjaZWkGRfHFVn1zqpX0XacfbIEQSz6O4A04xLoHJ3dYPiRcn\nT+kVNupIZ6G9FjbCe7RiIAm5NVVzUKiVTxFbZ5GX3zP6l9B3hpDoXtBdjwKBgGiO\nzVS1qIQ1q4v/orbTB3WJLJ0wwAhBSRiu/nRfnZBGjPvBClHLyGYZaTOAcwXC/PhD\nNTqeHrVKsW7zY6AlM2yQVndGPkiBlpP6e688/Z3HftezY2pdIS2r9RIhPeN2VJmE\nEUWLTkAD8eUfjJZa7tLuqsiiZ6RGxa9tLuMFwLvfAoGAGLonvdQ9i13lA175OsYP\n6dEnXHcP2jNvRqTH2rMHUYE1yQbsRVawgKVwxihDSvCx01CJ1IruuBxTY7RdWf0n\n50U0n4eKfc8b0Vn2mFVpNKg+sFZF18fQQtddehhNxdEywB9rV5d+Zj/8OmYp8ecF\njuze6ZHIpPx5/N0J1rJ4MVc=\n-----END PRIVATE KEY-----\n";
		translatorService.private_key_id = "65003b6c83895541b58ffccb624e2e65e54f4d2a";
		translatorService.language = "es";

		try {
			System.out.println("idioma detectado: " + translatorService.detectLanguage("hola mundo!"));
		
			System.out.println("idioma detectado: " + translatorService.detectLanguage("hello world!"));

			System.out.println("texto traducido: " + translatorService.translate("Hello world","en","es"));
			System.out.println("texto traducido: " + translatorService.translate("Hello world again!","en","es"));

			System.out.println("texto traducido: " + translatorService.defaultTranslate("living room, lighthouse, people, person, human"));
			System.out.println("texto traducido: " + translatorService.defaultTranslate("car, plane, jungle, animals","en"));
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date endDate = new Date();
		System.out.println("Segundos de ejecucion: " + ((float)(endDate.getTime() - startDate.getTime())/1000.0));
		
	}
}
