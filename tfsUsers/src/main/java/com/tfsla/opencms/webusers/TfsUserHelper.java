package com.tfsla.opencms.webusers;

import org.opencms.file.CmsUser;
//import java.util.*;

/**
 * Clase que permite manipular los usuarios de opencms de forma mas sencilla con los atributos de los usuarios del diario.
 * @author vpode
 *
 */
public class TfsUserHelper {

	public static String SEXO_MASCULINO = "m";
	public static String SEXO_FEMININO = "f";
	public static final String ERNESTO = "QWEasd21w212321PwwwTQWAooTE";	
	
	private CmsUser user;

	/**
	 * Constructor del helper.
	 * @param user
	 */
	public TfsUserHelper(CmsUser user) {
		this.user = user;
	}

	public String getFechaNacimiento() {
		return (String)user.getAdditionalInfo(RegistrationModule.USER_BIRTHDATE);
	}

	public void setFechaNacimiento(String fechaNacimiento) {
		user.setAdditionalInfo(RegistrationModule.USER_BIRTHDATE,fechaNacimiento);
	}

	public String getEmail() {
		return user.getEmail();
	}

	public String getFirstname() {
		return user.getFirstname();
	}

	public String getLastname() {
		return user.getLastname();
	}

	public String getName() {
		return user.getName();
	}

	public String getPassword() {
		return user.getPassword();
	}

	public void setEmail(String email) {
		user.setEmail(email);
	}

	public void setFirstname(String firstname) {
		user.setFirstname(firstname);
	}

	public void setLastname(String lastname) {
		user.setLastname(lastname);
	}

	public void setName(String name) {
		user.setName(name);
	}

	public void setPassword(String value) {
		user.setPassword(value);
	}

	public String getCelular() {
		return (String)user.getAdditionalInfo(RegistrationModule.USER_CELLPHONE);
	}

	public void setCelular(String celular) {
		user.setAdditionalInfo(RegistrationModule.USER_CELLPHONE,celular);;
	}

	public String getLocalidad() {
		return user.getCity();
	}

	public void setLocalidad(String localidad) {
		user.setCity(localidad);
	}

	public String getPais() {
		return user.getCountry();
	}

	public void setPais(String pais) {
		user.setCountry(pais);
	}

	public String getPcode() {
		return user.getZipcode();
	}

	public void setPcode(String pcode) {
		user.setZipcode(pcode);
	}

	public String getProvincia() {
		return (String)user.getAdditionalInfo(RegistrationModule.USER_STATE);
	}

	public void setProvincia(String provincia) {
		user.setAdditionalInfo(RegistrationModule.USER_STATE,provincia);;
	}

	public String getSexo() {
		return (String)user.getAdditionalInfo(RegistrationModule.USER_GENDER);
	}

	public void setSexo(String sexo) {
		user.setAdditionalInfo(RegistrationModule.USER_GENDER,sexo);
	}

	public String getTelefono() {
		return (String)user.getAdditionalInfo(RegistrationModule.USER_TELEPHONE);
	}

	public void setTelefono(String telefono) {
		user.setAdditionalInfo(RegistrationModule.USER_TELEPHONE,telefono);
	}

	public String getDni() {
		return (String)user.getAdditionalInfo(RegistrationModule.USER_DNI);
	}

	public void setDni(String dni) {
		user.setAdditionalInfo(RegistrationModule.USER_DNI,dni);
	}

	public String getDomicilio() {
		return user.getAddress();
	}

	public void setDomicilio(String domicilio) {
		user.setAddress(domicilio);
	}
	
	public String getProviderKey(String providerName) throws Exception {
		String providerKey = (String)user.getAdditionalInfo(RegistrationModule.USER_OPENAUTHORIZATION_PROVIDER_KEY.replace("{0}", providerName.toUpperCase()));
				
		if(providerKey == null)
			return "";
		else
			return Encrypter.decrypt(providerKey);
	}
	
	public boolean hasSetNativePassword() {
		String userSetNativePassword = (String)user.getAdditionalInfo(RegistrationModule.USER_SET_NATIVE_PASSWORD);
		
		if(userSetNativePassword == null || userSetNativePassword == "")
			return false;
		else
			return userSetNativePassword.equals("true");
	}	
	
	public boolean hasProviderAssociated(String providerName) {
		if(providerName == null || providerName.equals("")) return false;
		
		if(providerName.startsWith("web")) {
			providerName = providerName.split("-")[1];
		}
		
		String providerKey = (String)user.getAdditionalInfo(RegistrationModule.USER_OPENAUTHORIZATION_PROVIDER_KEY.replace("{0}", providerName.toUpperCase()));
		return providerKey != null && !providerKey.equals("");
	}
	
	public boolean canPostToProvider(String providerName) {
		
		String accessToken = (String)user.getAdditionalInfo(RegistrationModule.USER_OPENAUTHORIZATION_ACCESS_TOKEN.replace("{0}", providerName.toUpperCase()));
		
		if(accessToken == null || accessToken == "")
			return false;
		else
			return true;
	}	

	public String getValorAdicional(String key)
	{
		String value = (String)user.getAdditionalInfo(key);
		
		return value;
	}

	public void setValorAdicional(String key, String value)
	{
		user.setAdditionalInfo(key,value);
	}

	/**
	 * Setter de CmsUser a utilizar
	 * @return CmsUser
	 */
	public CmsUser getUser() {
		return user;
	}

	/**
	 * Getter de CmsUser a utilizar
	 */
	public void setUser(CmsUser user) {
		this.user = user;
	}
}