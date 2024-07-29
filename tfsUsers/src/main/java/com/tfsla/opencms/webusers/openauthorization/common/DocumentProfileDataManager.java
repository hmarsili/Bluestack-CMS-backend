package com.tfsla.opencms.webusers.openauthorization.common;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.tfsla.opencms.webusers.openauthorization.UserProfileData;
import com.tfsla.opencms.webusers.openauthorization.common.exceptions.InvalidPathException;

public class DocumentProfileDataManager extends UserProfileDataManager {
	
	protected DocumentProfileDataManager(ProviderConfiguration configuration, UserProfileData data) {
		super(configuration, data);
	}

	@Override
	protected Object getObjectValue(Object providerResponse, String path) throws InvalidPathException {
		Document dom = (Document) providerResponse;
		String ret = null;
		try {
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList nodes = (NodeList)xPath.evaluate(path,
					dom.getDocumentElement(), XPathConstants.NODESET);
			
			if(nodes.getLength() > 0) {
				Element element = (Element)(nodes.item(0));
				return element.getNodeValue();
			}
			//Element docEle = dom.getDocumentElement();
			//ret = docEle.getElementsByTagName(path).item(0).getFirstChild().getNodeValue();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return ret;
	}

	@Override
	protected ArrayList<ProviderListField> getListValue(Object objectList,
			String idField, String valueField) {
		// TODO Auto-generated method stub
		return null;
	}

}
