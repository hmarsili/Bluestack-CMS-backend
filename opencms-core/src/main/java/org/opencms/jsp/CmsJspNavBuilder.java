/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspNavBuilder.java,v $
 * Date   : $Date: 2011/03/23 14:51:33 $
 * Version: $Revision: 1.29 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Bean to provide a convenient way to build navigation structures based on the
 * <code>{@link org.opencms.jsp.CmsJspNavElement}</code>.<p>
 *
 * Use this together with the <code>{@link org.opencms.jsp.CmsJspActionElement}</code>
 * to obtain navigation information based on the current users permissions.
 * For example, use <code>{@link #getNavigationForFolder(String)}</code> and pass the 
 * value of the current OpenCms user context uri obtained 
 * from <code>{@link org.opencms.file.CmsRequestContext#getUri()}</code> as argument to obtain a list
 * of all items in the navigation of the current folder. Then use a simple scriptlet to 
 * iterate over these items and create a HTML navigation.<p>
 *
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.29 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.jsp.CmsJspNavElement
 */
public class CmsJspNavBuilder {

    // Member variables
    private CmsObject m_cms;
    private String m_requestUri;
    private String m_requestUriFolder;

    /**
     * Empty constructor, so that this bean can be initialized from a JSP.<p> 
     * 
     * @see java.lang.Object#Object()
     */
    public CmsJspNavBuilder() {

        // empty
    }

    /**
     * Default constructor.<p>
     * 
     * @param cms context provider for the current request
     */
    public CmsJspNavBuilder(CmsObject cms) {

        init(cms);
    }

    /**
     * Returns the full name (including vfs path) of the default file for this nav element 
     * or <code>null</code> if the nav element is not a folder.<p>
     * 
     * The default file of a folder is determined by the value of the property 
     * <code>default-file</code> or the systemwide property setting.
     * 
     * @param cms the cms object
     * @param folder full name of the folder
     * 
     * @return the name of the default file
     */
    public static String getDefaultFile(CmsObject cms, String folder) {

        if (folder.endsWith("/")) {
            List defaultFolders = new ArrayList();
            try {
                CmsProperty p = cms.readPropertyObject(folder, CmsPropertyDefinition.PROPERTY_DEFAULT_FILE, false);
                if (!p.isNullProperty()) {
                    defaultFolders.add(p.getValue());
                }
            } catch (CmsException exc) {
                // noop
            }

            defaultFolders.addAll(OpenCms.getDefaultFiles());

            for (Iterator i = defaultFolders.iterator(); i.hasNext();) {
                String defaultName = (String)i.next();
                if (cms.existsResource(folder + defaultName)) {
                    return folder + defaultName;
                }
            }

            return folder;
        }

        return null;
    }

    /**
     * Collect all navigation elements from the files in the given folder,
     * navigation elements are of class CmsJspNavElement.<p>
     *
     * @param cms context provider for the current request
     * @param folder the selected folder
     * @return a sorted (ascending to nav position) ArrayList of navigation elements
     */
    public static List getNavigationForFolder(CmsObject cms, String folder) {

        folder = CmsResource.getFolderPath(folder);
        List result = new ArrayList();

        List resources;
        try {
            resources = cms.getResourcesInFolder(folder, CmsResourceFilter.DEFAULT);
        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        }

        for (int i = 0; i < resources.size(); i++) {
            CmsResource r = (CmsResource)resources.get(i);
            CmsJspNavElement element = getNavigationForResource(cms, cms.getSitePath(r));
            if ((element != null) && element.isInNavigation()) {
                result.add(element);
            }
        }
        Collections.sort(result);
        return result;
    }

    /** 
     * Build a navigation for the folder that is either minus levels up 
     * from the given folder, or that is plus levels down from the 
     * root folder towards the given folder.<p> 
     * 
     * If level is set to zero the root folder is used by convention.<p>
     * 
     * @param cms context provider for the current request
     * @param folder the selected folder
     * @param level if negative, walk this many levels up, if positive, walk this many 
     * levels down from root folder 
     * @return a sorted (ascending to nav position) ArrayList of navigation elements
     */
    public static List getNavigationForFolder(CmsObject cms, String folder, int level) {

        folder = CmsResource.getFolderPath(folder);
        // If level is one just use root folder
        if (level == 0) {
            return getNavigationForFolder(cms, "/");
        }
        String navfolder = CmsResource.getPathPart(folder, level);
        // If navfolder found use it to build navigation
        if (navfolder != null) {
            return getNavigationForFolder(cms, navfolder);
        }
        // Nothing found, return empty list
        return Collections.EMPTY_LIST;
    }

    /**
     * Returns a CmsJspNavElement for the named resource.<p>
     * 
     * @param cms context provider for the current request
     * @param resource the resource name to get the nav information for, 
     * must be a full path name, e.g. "/docs/index.html".
     * @return a CmsJspNavElement for the given resource
     */
    public static CmsJspNavElement getNavigationForResource(CmsObject cms, String resource) {

        List properties;
        try {
            properties = cms.readPropertyObjects(resource, false);
        } catch (Exception e) {
            return null;
        }
        int level = CmsResource.getPathLevel(resource);
        if (resource.endsWith("/")) {
            level--;
        }
        return new CmsJspNavElement(resource, CmsProperty.toMap(properties), level);
    }

    /**
     * Builds a tree navigation for the folders between the provided start and end level.<p>
     * 
     * A tree navigation includes all nav elements that are required to display a tree structure.
     * However, the data structure is a simple list.
     * Each of the nav elements in the list has the {@link CmsJspNavElement#getNavTreeLevel()} set
     * to the level it belongs to. Use this information to distinguish between the nav levels.<p>
     * 
     * @param cms context provider for the current request
     * @param folder the selected folder
     * @param startlevel the start level
     * @param endlevel the end level
     * @return a sorted list of nav elements with the nav tree level property set 
     */
    public static List getNavigationTreeForFolder(CmsObject cms, String folder, int startlevel, int endlevel) {

        folder = CmsResource.getFolderPath(folder);
        // Make sure start and end level make sense
        if (endlevel < startlevel) {
            return Collections.EMPTY_LIST;
        }
        int currentlevel = CmsResource.getPathLevel(folder);
        if (currentlevel < endlevel) {
            endlevel = currentlevel;
        }
        if (startlevel == endlevel) {
            return getNavigationForFolder(cms, CmsResource.getPathPart(folder, startlevel), startlevel);
        }

        ArrayList result = new ArrayList();
        float parentcount = 0;

        for (int i = startlevel; i <= endlevel; i++) {
            String currentfolder = CmsResource.getPathPart(folder, i);
            List entries = getNavigationForFolder(cms, currentfolder);
            // Check for parent folder
            if (parentcount > 0) {
                for (int it = 0; it < entries.size(); it++) {
                    CmsJspNavElement e = (CmsJspNavElement)entries.get(it);
                    e.setNavPosition(e.getNavPosition() + parentcount);
                }
            }
            // Add new entries to result
            result.addAll(entries);
            Collections.sort(result);
            // Finally spread the values of the nav items so that there is enough room for further items.
            float pos = 0;
            int count = 0;
            String nextfolder = CmsResource.getPathPart(folder, i + 1);
            parentcount = 0;
            for (int it = 0; it < result.size(); it++) {
                pos = 10000 * (++count);
                CmsJspNavElement e = (CmsJspNavElement)result.get(it);
                e.setNavPosition(pos);
                if (e.getResourceName().startsWith(nextfolder)) {
                    parentcount = pos;
                }
            }
            if (parentcount == 0) {
                parentcount = pos;
            }
        }
        return result;
    }

    /**
     * This method builds a complete navigation tree with entries of all branches 
     * from the specified folder.<p>
     * 
     * For an unlimited depth of the navigation (i.e. no endLevel), set the endLevel to
     * a value &lt; 0.<p>
     * 
     * 
     * @param cms the current CmsJspActionElement.
     * @param folder the root folder of the navigation tree.
     * @param endLevel the end level of the navigation.
     * @return ArrayList of CmsJspNavElement, in depth first order.
     */
    public static List getSiteNavigation(CmsObject cms, String folder, int endLevel) {

        // check if a specific end level was given, if not, build the complete navigation
        boolean noLimit = false;
        if (endLevel < 0) {
            noLimit = true;
        }
        ArrayList list = new ArrayList();
        // get the navigation for this folder
        List curnav = getNavigationForFolder(cms, folder);
        // loop through all nav entrys
        for (int i = 0; i < curnav.size(); i++) {
            CmsJspNavElement ne = (CmsJspNavElement)curnav.get(i);
            // add the naventry to the result list
            list.add(ne);
            // check if naventry is a folder and below the max level -> if so, get the navigation from this folder as well
            if (ne.isFolderLink() && (noLimit || (ne.getNavTreeLevel() < endLevel))) {
                List subnav = getSiteNavigation(cms, ne.getResourceName(), endLevel);
                // copy the result of the subfolder to the result list
                list.addAll(subnav);
            }
        }
        return list;
    }

    /**
     * Build a "bread crump" path navigation to the current folder.<p>
     * 
     * @return ArrayList sorted list of navigation elements
     * @see #getNavigationBreadCrumb(String, int, int, boolean) 
     */
    public List getNavigationBreadCrumb() {

        return getNavigationBreadCrumb(m_requestUriFolder, 0, -1, true);
    }

    /**
     * Build a "bread crump" path navigation to the current folder.<p>
     * 
     * @param startlevel the start level, if negative, go down |n| steps from selected folder
     * @param currentFolder include the selected folder in navigation or not
     * @return ArrayList sorted list of navigation elements
     * @see #getNavigationBreadCrumb(String, int, int, boolean) 
     */
    public List getNavigationBreadCrumb(int startlevel, boolean currentFolder) {

        return getNavigationBreadCrumb(m_requestUriFolder, startlevel, -1, currentFolder);
    }

    /**
     * Build a "bread crump" path navigation to the current folder.<p>
     * 
     * @param startlevel the start level, if negative, go down |n| steps from selected folder
     * @param endlevel the end level, if -1, build navigation to selected folder
     * @return ArrayList sorted list of navigation elements
     * @see #getNavigationBreadCrumb(String, int, int, boolean) 
     */
    public List getNavigationBreadCrumb(int startlevel, int endlevel) {

        return getNavigationBreadCrumb(m_requestUriFolder, startlevel, endlevel, true);
    }

    /** 
     * Build a "bread crump" path navigation to the given folder.<p>
     * 
     * The startlevel marks the point where the navigation starts from, if negative, 
     * the count of steps to go down from the given folder.
     * The endlevel is the maximum level of the navigation path, set it to -1 to build the
     * complete navigation to the given folder.
     * You can include the given folder in the navigation by setting currentFolder to true,
     * otherwise false.<p> 
     * 
     * @param folder the selected folder
     * @param startlevel the start level, if negative, go down |n| steps from selected folder
     * @param endlevel the end level, if -1, build navigation to selected folder
     * @param currentFolder include the selected folder in navigation or not
     * @return ArrayList sorted list of navigation elements
     */
    public List getNavigationBreadCrumb(String folder, int startlevel, int endlevel, boolean currentFolder) {

        ArrayList result = new ArrayList();

        int level = CmsResource.getPathLevel(folder);
        // decrease folder level if current folder is not displayed
        if (!currentFolder) {
            level -= 1;
        }
        // check current level and change endlevel if it is higher or -1
        if ((level < endlevel) || (endlevel == -1)) {
            endlevel = level;
        }

        // if startlevel is negative, display only |startlevel| links
        if (startlevel < 0) {
            startlevel = endlevel + startlevel + 1;
            if (startlevel < 0) {
                startlevel = 0;
            }
        }

        // create the list of navigation elements     
        for (int i = startlevel; i <= endlevel; i++) {
            String navFolder = CmsResource.getPathPart(folder, i);
            CmsJspNavElement e = getNavigationForResource(navFolder);
            // add element to list
            result.add(e);
        }

        return result;
    }

    /**
     * Collect all navigation elements from the files of the folder of the current request URI,
     * navigation elements are of class CmsJspNavElement.<p>
     *
     * @return a sorted (ascending to nav position) ArrayList of navigation elements.
     */
    public List getNavigationForFolder() {

        return getNavigationForFolder(m_cms, m_requestUriFolder);
    }

    /** 
     * Build a navigation for the folder that is either minus levels up 
     * from of the folder of the current request URI, or that is plus levels down from the 
     * root folder towards the current request URI.<p> 
     * 
     * If level is set to zero the root folder is used by convention.<p>
     * 
     * @param level if negative, walk this many levels up, if positive, walk this many 
     * levels down from root folder 
     * @return a sorted (ascending to nav position) ArrayList of navigation elements
     */
    public List getNavigationForFolder(int level) {

        return getNavigationForFolder(m_cms, m_requestUriFolder, level);
    }

    /**
     * Collect all navigation elements from the files in the given folder,
     * navigation elements are of class CmsJspNavElement.<p>
     *
     * @param folder the selected folder
     * @return A sorted (ascending to nav position) ArrayList of navigation elements.
     */
    public List getNavigationForFolder(String folder) {

        return getNavigationForFolder(m_cms, folder);
    }

    /** 
     * Build a navigation for the folder that is either minus levels up 
     * from the given folder, or that is plus levels down from the 
     * root folder towards the given folder.<p> 
     * 
     * If level is set to zero the root folder is used by convention.<p>
     * 
     * @param folder the selected folder
     * @param level if negative, walk this many levels up, if positive, walk this many 
     * levels down from root folder 
     * @return a sorted (ascending to nav position) ArrayList of navigation elements
     */
    public List getNavigationForFolder(String folder, int level) {

        return getNavigationForFolder(m_cms, folder, level);
    }

    /**
     * Returns a CmsJspNavElement for the resource of the current request URI.<p>
     *  
     * @return CmsJspNavElement a CmsJspNavElement for the resource of the current request URI
     */
    public CmsJspNavElement getNavigationForResource() {

        return getNavigationForResource(m_cms, m_requestUri);
    }

    /**
     * Returns a CmsJspNavElement for the named resource.<p>
     * 
     * @param resource the resource name to get the nav information for, 
     * must be a full path name, e.g. "/docs/index.html".
     * @return CmsJspNavElement a CmsJspNavElement for the given resource
     */
    public CmsJspNavElement getNavigationForResource(String resource) {

        return getNavigationForResource(m_cms, resource);
    }

    /**
     * Builds a tree navigation for the folders between the provided start and end level.<p>
     * 
     * @param startlevel the start level
     * @param endlevel the end level
     * @return a sorted list of nav elements with the nav tree level property set 
     * @see #getNavigationTreeForFolder(CmsObject, String, int, int)
     */
    public List getNavigationTreeForFolder(int startlevel, int endlevel) {

        return getNavigationTreeForFolder(m_cms, m_requestUriFolder, startlevel, endlevel);
    }

    /**
     * Builds a tree navigation for the folders between the provided start and end level.<p>
     * 
     * @param folder the selected folder
     * @param startlevel the start level
     * @param endlevel the end level
     * @return a sorted list of nav elements with the nav tree level property set 
     * @see #getNavigationTreeForFolder(CmsObject, String, int, int) 
     */
    public List getNavigationTreeForFolder(String folder, int startlevel, int endlevel) {

        return getNavigationTreeForFolder(m_cms, folder, startlevel, endlevel);
    }

    /**
     * This method builds a complete site navigation tree with entries of all branches.<p>
     *
     * @see #getSiteNavigation(CmsObject, String, int)
     * 
     * @return ArrayList of CmsJspNavElement, in depth first order.
     */
    public List getSiteNavigation() {

        return getSiteNavigation(m_cms, "/", -1);
    }

    /**
     * This method builds a complete navigation tree with entries of all branches 
     * from the specified folder.<p>
     * 
     * @see #getSiteNavigation(CmsObject, String, int)
     * 
     * @param folder folder the root folder of the navigation tree.
     * @param endLevel the end level of the navigation.
     * @return ArrayList of CmsJspNavElement, in depth first order.
     */
    public List getSiteNavigation(String folder, int endLevel) {

        return getSiteNavigation(m_cms, folder, endLevel);
    }

    /**
     * Initiliazes this bean.<p>
     * 
     * @param cms context provider for the current request
     */
    public void init(CmsObject cms) {

        m_cms = cms;
        m_requestUri = m_cms.getRequestContext().getUri();
        m_requestUriFolder = CmsResource.getFolderPath(m_requestUri);
    }
}
