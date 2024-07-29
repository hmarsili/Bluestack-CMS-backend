package com.tfsla.opencmsdev.encuestas.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.beanutils.BeanUtils;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

public class XMLContentUtils {

    public static void setDatePropertyFromXML(Object bean, String propertyName,
            CmsXmlContent content, CmsObject cms)throws IllegalAccessException, InvocationTargetException {
        
        String absolutePath = content.getFile().getName();
        List locales = content.getLocales();
        
        if (locales.size() == 0) {
            locales = OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath);
        }
        
        Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(CmsLocaleManager.getLocale(""),
    OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath),locales);

        I_CmsXmlContentValue field = content.getValue(propertyName,locale);
        
        String value = field.getStringValue(cms);
        BeanUtils.setProperty(bean, propertyName, value);
    }

    public static void setBooleanPropertyFromXML(Object bean,
            String propertyName, CmsXmlContent content, CmsObject cms)
            throws IllegalAccessException, InvocationTargetException {
        I_CmsXmlContentValue field = content.getValue(propertyName,
                CmsLocaleManager.getDefaultLocale());
        String value = field.getStringValue(cms);
        BeanUtils.setProperty(bean, propertyName, Boolean.valueOf(value));
    }

    public static void setBooleanPropertyFromXML(Object bean,
            String propertyName, CmsXmlContent content, CmsObject cms, Boolean defaultValue)
            throws IllegalAccessException, InvocationTargetException {
        I_CmsXmlContentValue field = content.getValue(propertyName,
                CmsLocaleManager.getDefaultLocale());
        
        Boolean val = defaultValue;
        if (field!=null)
        	val = Boolean.valueOf(field.getStringValue(cms));
        	
        BeanUtils.setProperty(bean, propertyName, val);
    }

    public static List setListFromXML(Object bean, String elementName,
            String listName, CmsXmlContent content, CmsObject cms)
            throws IllegalAccessException, InvocationTargetException {
        List fields = content.getValues(elementName, CmsLocaleManager
                .getDefaultLocale());
        List<String> values = new ArrayList<String>();
        for (Iterator it = fields.iterator(); it.hasNext();) {
            I_CmsXmlContentValue value = (I_CmsXmlContentValue) it.next();
            String stringValue = value.getStringValue(cms);
            values.add(stringValue);
        }

        BeanUtils.setProperty(bean, listName, values);

        return values;
    }

    public static void setPropertyFromXML(Object bean, String propertyName,
            CmsXmlContent content, CmsObject cms, String defaultValue)
            throws IllegalAccessException, InvocationTargetException {
        I_CmsXmlContentValue field = content.getValue(propertyName,
                CmsLocaleManager.getDefaultLocale());
        String value = (field==null ? defaultValue : field.getStringValue(cms));
        BeanUtils.setProperty(bean, propertyName, value);
    }

    public static void setPropertyFromXML(Object bean, String propertyName,
            CmsXmlContent content, CmsObject cms)
            throws IllegalAccessException, InvocationTargetException {
        setPropertyFromXML(bean, propertyName, content, cms,null);
    }
}