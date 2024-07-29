package com.tfsla.homeCreator;

import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.tfsla.diario.ediciones.exceptions.UndefinedTipoEdicion;
import com.tfsla.diario.ediciones.model.Edicion;
import com.tfsla.diario.ediciones.model.TipoEdicion;
import com.tfsla.diario.ediciones.services.EdicionService;
import com.tfsla.diario.ediciones.services.TipoEdicionService;

/**
 * Recibe un xml con la estructura de las noticias a mostrar en una home de una publicacion (edicion impresa) y actualiza la misma.
 * @author Victor Podberezski
 *
 */
public class EdicionImpresaInlineDesigner {

    private static final Log LOG = CmsLog.getLog(EdicionImpresaInlineDesigner.class);

	private CmsObject cmsObject;
	
	private String inputEncoding = "ISO-8859-1";

	private String SITE;
	private int tipoEdicion = -1;
	private int nroEdicion = -1;
	private String homePage = "";
	private String target = "";

	private String xmlNodeName = "";
	
	private List<String> regionsNames = new ArrayList<String>(); 
	private HashMap<String, List<String>> noticiasByRegion = new HashMap<String,List<String>>(); 
	
	public EdicionImpresaInlineDesigner(CmsObject cmsObject)
	{
		this.cmsObject = cmsObject;
	}

	/**
	 * Toma un xml con formato:
	 * 
	 * 
	 * 	<tipoEdicion>
	 * 		IdPublicacion
	 * 	</tipoEdicion>
	 *  <edicion>
	 *  	NroEdicion
	 *  </edicion>
	 *  <target>
	 *  	titulos, index o nombre de seccion
	 *  </target>
	 *  <regiones>
	 *  	<region name="nombre de seccion o zona">
	 *  		<noticia>
	 *  			<path>path en el vfs de la noticia</path>
	 *  		</noticia>
	 *  		<noticia>
	 *  		</noticia>
	 *  		...
	 *  	</region>
	 *  	<region>
	 *      ...
	 *      </region>
	 *  </regiones>
	 *  
	 *  y actualiza el contenido estructurado de la home o titulos de la edicion de la publicacion definida por el xml
	 * @param xml (String)
	 * @throws DOMException
	 * @throws CmsException
	 */
	public void order(String xml) throws DOMException, CmsException
	{
	    try {
			InputStream is = new ByteArrayInputStream(xml.getBytes(inputEncoding));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			DocumentBuilder db = dbf.newDocumentBuilder();
	        Document dom = db.parse(is);

	        Element docEle = dom.getDocumentElement();  

	        //Obtengo el tipo de edicion.
	        NodeList node = docEle.getElementsByTagName("tipoEdicion");
	        if (node != null && node.getLength() == 1) {  
	        	Element eTipoEdicion = (Element) node.item(0);
	        	tipoEdicion = Integer.parseInt(eTipoEdicion.getFirstChild().getNodeValue());
	        }
	        
	        //Obtengo la edicion.
	        node = docEle.getElementsByTagName("edicion");
	        if (node != null && node.getLength() == 1) {  
	        	Element eTipoEdicion = (Element) node.item(0);
	        	nroEdicion = Integer.parseInt(eTipoEdicion.getFirstChild().getNodeValue());
	        }
	        
	        //Obtengo la home a modificar.
	        node = docEle.getElementsByTagName("target");
	        if (node != null && node.getLength() == 1) {  
	        	Element eTipoEdicion = (Element) node.item(0);
	        	target = eTipoEdicion.getFirstChild().getNodeValue();
	        	xmlNodeName = (target.toLowerCase().equals("titulos") ? "seccion" : "zona");
	        	homePage =  target + ".xml";
	        }
	       
	        NodeList regions = docEle.getElementsByTagName("region");
	        if (regions != null) {
        		for (int i = 0; i < regions.getLength(); i++) {
        			Element region = (Element) regions.item(i);
        			
        			String name = region.getAttribute("name");
        			List<String> noticias = new ArrayList<String>(); 
        			
        			regionsNames.add(name);
        			noticiasByRegion.put(name, noticias);
        			
        			NodeList nNoticias = region.getElementsByTagName("noticia");
        	        if (nNoticias != null) {  
        	        	for (int j = 0; j < nNoticias.getLength(); j++) {  
        	        		Element noticia = (Element) nNoticias.item(j);  
        	        		Element ePath = (Element) noticia.getElementsByTagName("path").item(0);
        	        		
        	        		String path = ePath.getFirstChild().getNodeValue();
        	        		noticias.add(path);

        	        	}  

        	        }  
        			
        		}
	        }
	        
	        EdicionService eService = new EdicionService();
	        TipoEdicionService tService = new TipoEdicionService();
	        
	        TipoEdicion tEdicion = tService.obtenerTipoEdicion(tipoEdicion);
	        Edicion edicion = eService.obtenerEdicion(tipoEdicion, nroEdicion);
	        
	        edicion.setTipoEdicion(tEdicion);
	        
	        SITE = "/sites/" + tEdicion.getProyecto();
	        
	        homePage = edicion.getbaseURL() + EdicionService.HOME_DIRECTORY + homePage;
	        
	        configurarHome();
	        
	    } catch (ParserConfigurationException e) {
			e.printStackTrace();
	    } 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UndefinedTipoEdicion e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void configurarHome()
	{

		String currentSiteRoot = cmsObject.getRequestContext().getSiteRoot();

		try {
	
			cmsObject.getRequestContext().setSiteRoot(SITE + "/");


			homePage = homePage.replace(SITE, "");
		
		
			lockTheFile(homePage);
	
			CmsFile contentFile = cmsObject.readFile(homePage);
			CmsXmlContent content = CmsXmlContentFactory.unmarshal(cmsObject, contentFile);
	
			content.setAutoCorrectionEnabled(true); 
			content.correctXmlStructure(cmsObject);
	
			for (String nombre : regionsNames) {
			
				//Busco la region...
				int nroRegion = findKeyInContent(content,xmlNodeName + "[{nro}]/nombre[1]",nombre);
	
	
				//Si no esta la agrego.
				if (nroRegion==-1)
				{
					content.addValue(cmsObject, xmlNodeName, Locale.ENGLISH, 0);
					nroRegion=1;
					I_CmsXmlContentValue value = content.getValue(xmlNodeName + "[" + nroRegion + "]/nombre[1]", Locale.ENGLISH);
					value.setStringValue(cmsObject, nombre);
		
				}
	
				// Quito todas las noticias que estaban antes
				while (content.getValue(xmlNodeName + "[" + nroRegion + "]/noticia[1]", Locale.ENGLISH)!=null)
					content.removeValue(xmlNodeName + "[" + nroRegion + "]/noticia", Locale.ENGLISH, 0);
				
				
				//Agrego las que vienen ahora
				int nroNot = 1;
				for (String fileName : noticiasByRegion.get(nombre)) {
					//Agrego la noticia.
				    content.addValue(cmsObject, xmlNodeName + "[" + nroRegion + "]/noticia[" + nroNot + "]", Locale.ENGLISH, (nroNot-1));
				    I_CmsXmlContentValue value = content.getValue(xmlNodeName + "[" + nroRegion + "]/noticia[" + nroNot + "]", Locale.ENGLISH);
				    value.setStringValue(cmsObject, fileName);

				     
					nroNot++;
				}
			}
			
			contentFile.setContents(content.marshal());
			cmsObject.writeFile(contentFile);
	
	
			cmsObject.unlockResource(homePage);
	

		}
		catch (CmsException e)
		{
			LOG.error("Error al intentar agregar la  noticia.",e);
		} 
		finally {
			cmsObject.getRequestContext().setSiteRoot(currentSiteRoot);	
		}
	}

	protected int findKeyInContent(CmsXmlContent content, String key, String value)
	{
		int nro=1;
		
		String xmlName = key.replace("{nro}", "" + nro); 
		String seccion = content.getStringValue(cmsObject, xmlName, Locale.ENGLISH);
		while (seccion!=null && !seccion.equals(value))
		{
			nro++;
			xmlName = key.replace("{nro}", "" + nro);
			seccion = content.getStringValue(cmsObject, xmlName, Locale.ENGLISH);
		}
	
		if (seccion==null)
			nro=-1;
		
		return nro;
	}

	private void lockTheFile(String file) throws CmsException
	{
		if (cmsObject.getLock(file).isUnlocked())
			cmsObject.lockResource(file);
        else
        {
        	try {
        		cmsObject.unlockResource(file);
        		cmsObject.lockResource(file);
        	}
        	catch (Exception e)
        	{
        		cmsObject.changeLock(file);	            		
        	}
        }

	}

	public String getInputEncoding() {
		return inputEncoding;
	}

	public void setInputEncoding(String inputEncoding) {
		this.inputEncoding = inputEncoding;
	}

}
