/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/collectors/A_CmsResourceCollector.java,v $
 * Date   : $Date: 2011/03/23 14:50:53 $
 * Version: $Revision: 1.19 $
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

package org.opencms.file.collectors;

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.PrintfFormat;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides some helpful base implementations for resource collector classes.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.19 $
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsResourceCollector implements I_CmsResourceCollector {

    /** The template file separator string for creating a new resource in direct edit mode,
     *  can be used to append an explicit template file name in {@link #getCreateParam(CmsObject, String, String)}. */
    public static final String SEPARATOR_TEMPLATEFILE = "::";

    /** The "number" macro. */
    private static final String MACRO_NUMBER = "number";

    /** Format for file create parameter. */
    private static final PrintfFormat NUMBER_FORMAT = new PrintfFormat("%0.4d");

    /** The collector order of this collector. */
    protected int m_order;

    /** The name of the configured default collector. */
    private String m_defaultCollectorName;

    /** The default collector parameters. */
    private String m_defaultCollectorParam;

    /** The hash code of this collector. */
    private int m_hashcode;

    /**
     * Constructor to initialize some default values.<p>
     */
    public A_CmsResourceCollector() {

        m_hashcode = getClass().getName().hashCode();
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof I_CmsResourceCollector) {
            return getOrder() - ((I_CmsResourceCollector)obj).getOrder();
        }
        return 0;
    }

    /**
     * Two collectors are considered to be equal if they are sharing the same
     * implementation class.<p> 
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof I_CmsResourceCollector) {
            return getClass().getName().equals(obj.getClass().getName());
        }
        return false;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject)
     */
    public String getCreateLink(CmsObject cms) throws CmsException, CmsDataAccessException {

        checkParams();
        return getCreateLink(cms, getDefaultCollectorName(), getDefaultCollectorParam());
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject)
     */
    public String getCreateParam(CmsObject cms) throws CmsDataAccessException {

        checkParams();
        return getCreateParam(cms, getDefaultCollectorName(), getDefaultCollectorParam());
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getDefaultCollectorName()
     */
    public String getDefaultCollectorName() {

        return m_defaultCollectorName;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getDefaultCollectorParam()
     */
    public String getDefaultCollectorParam() {

        return m_defaultCollectorParam;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getOrder()
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject)
     */
    public List getResults(CmsObject cms) throws CmsDataAccessException, CmsException {

        checkParams();
        return getResults(cms, getDefaultCollectorName(), getDefaultCollectorParam());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_hashcode;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setDefaultCollectorName(java.lang.String)
     */
    public void setDefaultCollectorName(String collectorName) {

        m_defaultCollectorName = collectorName;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setDefaultCollectorParam(java.lang.String)
     */
    public void setDefaultCollectorParam(String param) {

        m_defaultCollectorParam = param;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setOrder(int)
     */
    public void setOrder(int order) {

        m_order = order;
    }

    /**
     * Checks if the required parameters have been set.<p>
     * 
     * @see #setDefaultCollectorName(String)
     * @see #setDefaultCollectorParam(String)
     */
    protected void checkParams() {

        if ((m_defaultCollectorName == null) || (m_defaultCollectorParam == null)) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_COLLECTOR_DEFAULTS_INVALID_2,
                m_defaultCollectorName,
                m_defaultCollectorParam));
        }
    }

    /**
     * Returns the link to create a new XML content item in the folder pointed to by the parameter.<p>
     * 
     * @param cms the current CmsObject
     * @param data the collector data to use
     * 
     * @return the link to create a new XML content item in the folder
     * 
     * @throws CmsException if something goes wrong
     * 
     * @since 7.0.2
     */
    protected String getCreateInFolder(CmsObject cms, CmsCollectorData data) throws CmsException {

        String foldername = CmsResource.getFolderPath(data.getFileName());

        // must check ALL resources in folder because name doesn't care for type
        List resources = cms.readResources(foldername, CmsResourceFilter.ALL, false);

        // now create a list of all resources that just contains the file names
        List result = new ArrayList(resources.size());
        for (int i = 0; i < resources.size(); i++) {
            CmsResource resource = (CmsResource)resources.get(i);
            result.add(resource.getRootPath());
        }

        String fileName = cms.getRequestContext().addSiteRoot(data.getFileName());
        String checkFileName, checkTempFileName, number;
        CmsMacroResolver resolver = CmsMacroResolver.newInstance();

        int j = 0;
        do {
            number = NUMBER_FORMAT.sprintf(++j);
            resolver.addMacro(MACRO_NUMBER, number);
            // resolve macros in file name
            checkFileName = resolver.resolveMacros(fileName);
            // get name of the resolved temp file
            checkTempFileName = CmsWorkplace.getTemporaryFileName(checkFileName);
        } while (result.contains(checkFileName) || result.contains(checkTempFileName));

        return cms.getRequestContext().removeSiteRoot(checkFileName);
    }

    /**
     * Returns the link to create a new XML content item in the folder pointed to by the parameter.<p>
     * 
     * @param cms the current CmsObject
     * @param param the folder name to use
     * 
     * @return the link to create a new XML content item in the folder
     * 
     * @throws CmsException if something goes wrong
     */
    protected String getCreateInFolder(CmsObject cms, String param) throws CmsException {

        return getCreateInFolder(cms, new CmsCollectorData(param));
    }

    /**
     * Shrinks a List to fit a maximum size.<p>
     * 
     * @param result a List
     * @param maxSize the maximum size of the List
     * 
     * @return the shrinked list
     */
    protected List shrinkToFit(List result, int maxSize) {

        if ((maxSize > 0) && (result.size() > maxSize)) {
            // cut off all items > count
            result = result.subList(0, maxSize);
        }

        return result;
    }
}