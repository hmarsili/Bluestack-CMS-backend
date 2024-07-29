/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspNavElement.java,v $
 * Date   : $Date: 2011/03/23 14:51:34 $
 * Version: $Revision: 1.23 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.workplace.CmsWorkplace;

import java.util.Map;

/**
 * Bean to collect navigation information from a resource in the OpenCms VFS.<p>
 *
 * Each navigation element contains a number of information about a VFS resource,
 * obtained either from the resources properties or attributes.
 * You can use this information to generate a HTML navigation for 
 * files in the VFS in your template.<p>
 *
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.23 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.jsp.CmsJspNavBuilder
 */
public class CmsJspNavElement implements Comparable {

    private String m_fileName;
    private Boolean m_hasNav;
    private int m_navTreeLevel = Integer.MIN_VALUE;
    private float m_position;
    private Map m_properties;
    private String m_resource;
    private String m_text;

    /**
     * Empty constructor required for every JavaBean, does nothing.<p>
     * 
     * Call one of the init methods after you have created an instance 
     * of the bean. Instead of using the constructor you should use 
     * the static factory methods provided by this class to create
     * navigation beans that are properly initialized with current 
     * OpenCms context.<p>
     * 
     * @see CmsJspNavBuilder#getNavigationForResource()
     * @see CmsJspNavBuilder#getNavigationForFolder()
     * @see CmsJspNavBuilder#getNavigationTreeForFolder(int, int)
     */
    public CmsJspNavElement() {

        // empty
    }

    /**
     * Create a new instance of the bean and calls the init method 
     * with the provided parameters.<p>
     * 
     * @param resource will be passed to <code>init</code>
     * @param properties will be passed to <code>init</code>
     * 
     * @see #init(String, Map)
     */
    public CmsJspNavElement(String resource, Map properties) {

        init(resource, properties, -1);
    }

    /**
     * Create a new instance of the bean and calls the init method 
     * with the provided parameters.<p>
     * 
     * @param resource will be passed to <code>init</code>
     * @param properties will be passed to <code>init</code>
     * @param navTreeLevel will be passed to <code>init</code>
     * 
     * @see #init(String, Map, int)
     */
    public CmsJspNavElement(String resource, Map properties, int navTreeLevel) {

        init(resource, properties, navTreeLevel);
    }

    /**
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsJspNavElement) {
            float pos = ((CmsJspNavElement)obj).getNavPosition();
            // please note: can't just subtract and cast to int here because of float precision loss
            if (m_position == pos) {
                return 0;
            }
            return (m_position < pos) ? -1 : 1;
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsJspNavElement) {
            return ((CmsJspNavElement)obj).m_resource.equals(m_resource);
        }
        return false;
    }

    /**
     * Returns the value of the property PROPERTY_DESCRIPTION of this navigation element,
     * or <code>null</code> if this property is not set.<p> 
     * 
     * @return the value of the property PROPERTY_DESCRIPTION of this navigation element
     *          or <code>null</code> if this property is not set
     */
    public String getDescription() {

        return (String)m_properties.get(CmsPropertyDefinition.PROPERTY_DESCRIPTION);
    }

    /**
     * Returns the filename of the navigation element, i.e.
     * the name of the navigation resource without any path information.<p>
     * 
     * @return the filename of the navigation element, i.e.
     *          the name of the navigation resource without any path information
     */
    public String getFileName() {

        if (m_fileName == null) {
            // use "lazy initializing"
            if (!m_resource.endsWith("/")) {
                m_fileName = m_resource.substring(m_resource.lastIndexOf("/") + 1, m_resource.length());
            } else {
                m_fileName = m_resource.substring(
                    m_resource.substring(0, m_resource.length() - 1).lastIndexOf("/") + 1,
                    m_resource.length());
            }
        }
        return m_fileName;
    }

    /**
     * Returns the value of the property <code>{@link CmsPropertyDefinition#PROPERTY_NAVINFO}</code> of this 
     * navigation element, or <code>null</code> if this property is not set.<p> 
     * 
     * @return the value of the property or <code>null</code> if this property is not set
     */
    public String getInfo() {

        return (String)m_properties.get(CmsPropertyDefinition.PROPERTY_NAVINFO);
    }

    /**
     * Returns the value of the property <code>{@link CmsPropertyDefinition#PROPERTY_LOCALE}</code> of this 
     * navigation element, or <code>null</code> if this property is not set.<p> 
     * 
     * @return the value of the property or <code>null</code> if this property is not set
     */
    public String getLocale() {

        return (String)m_properties.get(CmsPropertyDefinition.PROPERTY_LOCALE);
    }

    /**
     * Returns the value of the property <code>{@link CmsPropertyDefinition#PROPERTY_NAVIMAGE}</code> of this 
     * navigation element, or <code>null</code> if this property is not set.<p> 
     * 
     * @return the value of the property or <code>null</code> if this property is not set
     */
    public String getNavImage() {

        return (String)m_properties.get(CmsPropertyDefinition.PROPERTY_NAVIMAGE);
    }

    /**
     * Returns the value of the property C_PROPERTY_NAVPOS converted to a <code>float</code>,
     * or a value of <code>Float.MAX_VALUE</code> if the navigation position property is not 
     * set (or not a valid number) for this resource.<p>
     * 
     * @return float the value of the property C_PROPERTY_NAVPOS converted to a <code>float</code>,
     *          or a value of <code>Float.MAX_VALUE</code> if the navigation position property is not 
     *          set (or not a valid number) for this resource
     */
    public float getNavPosition() {

        return m_position;
    }

    /**
     * Returns the value of the property PROPERTY_NAVTEXT of this navigation element,
     * or a warning message if this property is not set 
     * (this method will never return <code>null</code>).<p> 
     * 
     * @return the value of the property PROPERTY_NAVTEXT of this navigation element,
     *          or a warning message if this property is not set 
     *          (this method will never return <code>null</code>)
     */
    public String getNavText() {

        if (m_text == null) {
            // use "lazy initializing"
            m_text = (String)m_properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            if (m_text == null) {
                m_text = CmsMessages.formatUnknownKey(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            }
        }
        return m_text;
    }

    /**
     * Returns the navigation tree level of this resource.<p>
     * 
     * @return the navigation tree level of this resource
     */
    public int getNavTreeLevel() {

        if (m_navTreeLevel < 0) {
            // use "lazy initializing"
            m_navTreeLevel = CmsResource.getPathLevel(m_resource);
        }
        return m_navTreeLevel;
    }

    /**
     * Returns the name of the parent folder of the resource of this navigation element.<p>
     * 
     * @return the name of the parent folder of the resource of this navigation element
     */
    public String getParentFolderName() {

        return CmsResource.getParentFolder(m_resource);
    }

    /**
     * Returns the original map of all file properties of the resource that
     * the navigation element belongs to.<p>
     * 
     * Please note that the original reference is returned, so be careful when making 
     * changes to the map.<p>
     * 
     * @return the original map of all file properties of the resource that
     *          the navigation element belongs to
     */
    public Map getProperties() {

        return m_properties;
    }

    /**
     * Returns the value of the selected property from this navigation element.<p> 
     * 
     * The navigation element contains a hash of all file properties of the resource that
     * the navigation element belongs to.<p>
     * 
     * @param key the property name to look up
     * 
     * @return the value of the selected property
     */
    public String getProperty(String key) {

        return (String)m_properties.get(key);
    }

    /**
     * Returns the resource name this navigation element was initialized with.<p>
     * 
     * @return the resource name this navigation element was initialized with
     */
    public String getResourceName() {

        return m_resource;
    }

    /**
     * Returns the value of the property PROPERTY_TITLE of this navigation element,
     * or <code>null</code> if this property is not set.<p> 
     * 
     * @return the value of the property PROPERTY_TITLE of this navigation element
     *          or <code>null</code> if this property is not set
     */
    public String getTitle() {

        return (String)m_properties.get(CmsPropertyDefinition.PROPERTY_TITLE);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return super.hashCode();
    }

    /**
     * Same as calling {@link #init(String, Map, int) 
     * init(String, Hashtable, -1)}.<p>
     * 
     * @param resource the name of the resource to extract the navigation 
     *     information from
     * @param properties the properties of the resource read from the vfs
     */
    public void init(String resource, Map properties) {

        init(resource, properties, -1);
    }

    /**
     * Initialized the member variables of this bean with the values 
     * provided.<p>
     * 
     * A resource will be in the navigation if at least one of the two properties 
     * <code>I_CmsConstants.PROPERTY_NAVTEXT</code> or 
     * <code>I_CmsConstants.PROPERTY_NAVPOS</code> is set. Otherwise
     * it will be ignored.<p>
     * 
     * This bean does provides static methods to create a new instance 
     * from the context of a current CmsObject. Call these static methods
     * in order to get a properly initialized bean.<p>
     * 
     * @param resource the name of the resource to extract the navigation 
     *     information from
     * @param properties the properties of the resource read from the vfs
     * @param navTreeLevel tree level of this resource, for building 
     *     navigation trees
     * 
     * @see CmsJspNavBuilder#getNavigationForResource()
     */
    public void init(String resource, Map properties, int navTreeLevel) {

        m_resource = resource;
        m_properties = properties;
        m_navTreeLevel = navTreeLevel;
        // init the position value
        m_position = Float.MAX_VALUE;
        try {
            m_position = Float.parseFloat((String)m_properties.get(CmsPropertyDefinition.PROPERTY_NAVPOS));
        } catch (Exception e) {
            // m_position will have Float.MAX_VALUE, so navigation element will 
            // appear last in navigation
        }
    }

    /**
     * Returns <code>true</code> if this navigation element describes a folder, 
     * <code>false</code> otherwise.<p>
     * 
     * @return <code>true</code> if this navigation element describes a folder, 
     *          <code>false</code> otherwise.<p>
     */
    public boolean isFolderLink() {

        return m_resource.endsWith("/");
    }

    /**
     * Returns <code>true</code> if this navigation element is in the navigation, 
     * <code>false</code> otherwise.<p>
     * 
     * A resource is considered to be in the navigation, if <ol>
     * <li>it has the property PROPERTY_NAVTEXT set
     * <li><em>or</em> it has the property PROPERTY_NAVPOS set 
     * <li><em>and</em> it is not a temporary file as defined by {@link CmsWorkplace#isTemporaryFileName(String)}.</ol> 
     * 
     * @return <code>true</code> if this navigation element is in the navigation, <code>false</code> otherwise
     */
    public boolean isInNavigation() {

        if (m_hasNav == null) {
            // use "lazy initializing"
            Object o1 = m_properties.get(CmsPropertyDefinition.PROPERTY_NAVTEXT);
            Object o2 = m_properties.get(CmsPropertyDefinition.PROPERTY_NAVPOS);
            m_hasNav = Boolean.valueOf(((o1 != null) || (o2 != null)) && !CmsWorkplace.isTemporaryFileName(m_resource));
        }
        return m_hasNav.booleanValue();
    }

    /**
     * Sets the value that will be returned by the {@link #getNavPosition()}
     * method of this class.<p>
     * 
     * @param value the value to set
     */
    public void setNavPosition(float value) {

        m_position = value;
    }
}