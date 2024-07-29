package com.tfsla.genericImport.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.opencms.db.CmsDbEntryAlreadyExistsException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContentValueSequence;
import org.opencms.xml.types.I_CmsXmlContentValue;

import com.tfsla.genericImport.exception.DataTransformartionException;
import com.tfsla.genericImport.model.A_ImportService;

public class ImportUsersService extends A_ImportService {

	public ImportUsersService(CmsObject cms, String importDefinitionPath) throws CmsException {
		super(cms, importDefinitionPath);
		
		this.contentType = "Usuarios";
		this.importName = importDefinitionContent.getStringValue(cms, "NombreImportacion", contentLocale);
		this.cvsSplitBy = importDefinitionContent.getStringValue(cms, "Separador", contentLocale);
	}

	@Override
	public void run() {
		writeToLog("// Inicio de proceso de importacion");
		BufferedReader br = null;
		String line = "";
		int counter = 0;
		int cantidad = Integer.MAX_VALUE;
		int offset = 0;
		int errores = 0;
		int linesToImport = 0;
		String userName = "";
		String ou = "";
		
		try {
			if(this.cantidad != null && !this.cantidad.equals(""))
				cantidad = Integer.parseInt(this.cantidad);
			if(this.offset != null && !this.offset.equals(""))
				offset = Integer.parseInt(this.offset);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			DecimalFormat formatter = new DecimalFormat("#0.00");
			linesToImport = this.getLinesToImport(cantidad, offset);
			br = new BufferedReader(new FileReader(fileName));
			
			while ((line = br.readLine()) != null) {
				if(counter < offset) {
					counter ++;
					continue;
				}
				if(counter - offset >= cantidad) {
					break;
				}
				
				try {
					writeToLog("Procesando linea " + counter);
					
					String[] row = line.split(this.cvsSplitBy);
					userName = this.getFieldValue("", "NombreUsuario", row);
					this.assertField("NombreUsuario", userName);
					String password = this.getFieldValue("", "Password", row);
					this.assertField("Password", password);
					String email = this.getFieldValue("", "Email", row);
					this.assertField("Email", email);
					String descripcion = this.getFieldValue("", "Descripcion", row);
					ou = this.getFieldValue("", "OU", row);
					if(ou != null && !ou.equals("") && !ou.endsWith("/")) {
						ou += "/";
					}
					String activo = this.getFieldValue("", "Activo", row);
					Boolean enabled = true;
					if(activo != null && !activo.equals("")) {
						enabled = Boolean.parseBoolean(activo);
					}
					
					String nombre = this.getFieldValue("", "Nombre", row);
					String apellido = this.getFieldValue("", "Apellido", row);
					String pais = this.getFieldValue("", "Pais", row);
					String localidad = this.getFieldValue("", "Ciudad", row);
					String domicilio = this.getFieldValue("", "Direccion", row);
					String cp = this.getFieldValue("", "CP", row);
					
					CmsUser newUser = cms.createUser(ou + userName, password, descripcion, new HashMap<Object, Object>());
					if(nombre != null && !nombre.equals(""))
						newUser.setFirstname(nombre);
					if(apellido != null && !apellido.equals(""))
						newUser.setLastname(apellido);
					if(email != null && !email.equals(""))
						newUser.setEmail(email);
					if(pais != null && !pais.equals(""))
						newUser.setCountry(pais);
					if(localidad != null && !localidad.equals(""))
						newUser.setCity(localidad);
					if(domicilio != null && !domicilio.equals(""))
						newUser.setAddress(domicilio);
					if(cp != null && !cp.equals(""))
						newUser.setZipcode(cp);
					newUser.setEnabled(enabled);
					
					//SET USER ADDITIONAL INFO
					if(importDefinitionContent.hasValue("InformacionAdicional", contentLocale)) {
						CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence("InformacionAdicional", contentLocale);
						int elementCount = elementSequence.getElementCount();
						for (int j = 0; j < elementCount; j++) {
							String path = String.format("InformacionAdicional[%s]", j+1);
							String valor = this.getFieldValue("", path, row);
							String columna = importDefinitionContent.getStringValue(cms, path + "/Campo", contentLocale);
							newUser.setAdditionalInfo(columna, valor);
						}
					}
					
					//SET USER GROUPS
					if(importDefinitionContent.hasValue("Grupos", contentLocale)) {
						CmsXmlContentValueSequence elementSequence = importDefinitionContent.getValueSequence("Grupos", contentLocale);
						int elementCount = elementSequence.getElementCount();
						for (int j = 0; j < elementCount; j++) {
							String path = String.format("Grupos[%s]", j+1);
							String valor = this.getFieldValue("", path, row);
							if(valor != null && !valor.equals("")) {
								cms.addUserToGroup(ou + userName, valor);
							}
						}
					}
					
					cms.writeUser(newUser);
					writeToLog(String.format("Linea %s: usuario '%s' creado", counter, ou + userName));
				} catch(CmsDbEntryAlreadyExistsException ex) {
					writeToLog(String.format("Error al procesar la linea %s: el usuario '%s%s' ya existe", counter, ou, userName));
					errores++;
				} catch(Exception ex) {
					writeToLog("Error al procesar la linea " + counter + ": " + ex.getMessage());
					errores++;
				}
				counter++;
				Double por = (double)(counter - offset) / linesToImport * 100;
				writeToLog("// Avance de proceso " + formatter.format(por) + "%");
			}
		} catch (Exception e) {
			writeToLog("Error en la importacion: " + e.getMessage());
			writeToLog(e.getStackTrace().toString());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			writeToLog(linesToImport + " registros procesados");
			if(errores > 0) writeToLog(errores + " errores encontrados durante el proceso");
			writeToLog("// Proceso de importacion finalizado");
			closeLog();
		}
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	private String getFieldValue(String path, String propertyName, String[] row) throws DataTransformartionException {
		String ret = "";
		
		if(!importDefinitionContent.hasValue(path + propertyName, contentLocale)) return ret;
		
		I_CmsXmlContentValue valor = importDefinitionContent.getValue(propertyName + "/Valor", contentLocale);
		if(valor != null && valor.getStringValue(cms) != null && !valor.getStringValue(cms).equals("")) {
			ret = valor.getStringValue(cms);
		}
		Object[] param = new Object[1];
		param[0] = ret;
		
		I_CmsXmlContentValue columna = importDefinitionContent.getValue(propertyName + "/Columna", contentLocale);
		if(columna != null && columna.getStringValue(cms) != null && !columna.getStringValue(cms).equals("")) {
			String[] str = columna.getStringValue(cms).split(",");
			param = new Object[str.length];
			for (int i = 0; i < str.length; i++) 
			    param[i] = row[Integer.valueOf(str[i])];
		}
		
		try {
			Object result = this.transform(path + propertyName + "/", param);
			if(result == null) return "";
			return result.toString();
		} catch(Exception ex) {
			writeToLog(String.format("Error convirtiendo el campo %s: %s", propertyName, ex.getMessage()));
		}
		return ret;
	}
	
	private int getLinesToImport(int cantidad, int offset) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		int lines = 0;
		int i = 0;
		while (reader.readLine() != null) {
			i++;
			if(i >= offset && i <= cantidad)
				lines++;
		}
		reader.close();
		
		return lines;
	}
	
	private void assertField(String fieldName, String value) throws Exception {
		if(value == null || value.trim().equals("")) {
			throw new Exception(String.format("El campo '%s' se encuentra en blanco para este registro", fieldName));
		}
	}
	
	private String cvsSplitBy;
	private String fileName;
}
