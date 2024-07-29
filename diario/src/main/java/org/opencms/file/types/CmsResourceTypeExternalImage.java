/*
 * File   : $Source: /usr/local/cvs/opencms/src/org/opencms/file/types/CmsResourceTypeImage.java,v $
 * Date   : $Date: 2010-01-18 10:03:26 $
 * Version: $Revision: 1.21 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2010 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.loader.CmsExternalImageScaler;
import org.opencms.loader.CmsExternalImageLoader;
import org.opencms.loader.CmsPointerLoader;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Resource type descriptor for the type "external-image".<p>
 *
 * @author Victor Podberezski 
 * 
 * @version $Revision: 1.21 $ 
 * 
 * @since 6.0.0 
 */
public class CmsResourceTypeExternalImage extends A_CmsResourceType {

    /**
     * A data container for image size and scale operations.<p>
     */
    protected static class CmsImageAdjuster {

        /** The image byte content. */
        private byte[] m_content;

        /** The (optional) image scaler that contains the image downscale settings. */
        private CmsExternalImageScaler m_imageDownScaler;

        /** The image properties. */
        private List m_properties;

        /** The image root path. */
        private String m_rootPath;

        /**
         * Creates a new image data container.<p>
         * 
         * @param content the image byte content
         * @param rootPath the image root path
         * @param properties the image properties
         * @param downScaler the (optional) image scaler that contains the image downscale settings
         */
        public CmsImageAdjuster(byte[] content, String rootPath, List properties, CmsExternalImageScaler downScaler) {

            m_content = content;
            m_rootPath = rootPath;
            m_properties = properties;
            m_imageDownScaler = downScaler;
        }

        /**
         * Calculates the image size and adjusts the image dimensions (if required) accoring to the configured 
         * image downscale settings.<p>
         * 
         * The image dimensions are always calculated from the given image. The internal list of properties is updated 
         * with a value for <code>{@link CmsPropertyDefinition#PROPERTY_IMAGE_SIZE}</code> that 
         * contains the calculated image dimensions.<p> 
         */
        public void adjust() {

            CmsExternalImageScaler scaler = new CmsExternalImageScaler(getContent(), getRootPath());
            if (!scaler.isValid()) {
            	// error calculating image dimensions - this image can't be scaled or resized
                return;
            }

            // check if the image is to big and needs to be rescaled
            if (scaler.isDownScaleRequired(m_imageDownScaler)) {
                // image is to big, perform rescale operation                    
                CmsExternalImageScaler downScaler = scaler.getDownScaler(m_imageDownScaler);
                // perform the rescale using the adjusted size
                m_content = downScaler.scaleImage(m_content, m_rootPath);
                // image size has been changed, adjust the scaler for later setting of properties
                scaler.setHeight(downScaler.getHeight());
                scaler.setWidth(downScaler.getWidth());
            }

            CmsProperty p = new CmsProperty(CmsPropertyDefinition.PROPERTY_IMAGE_SIZE, null, scaler.toString());
            // create the new property list if required (don't modify the original List)
            
            List result = new ArrayList();
            if ((m_properties != null) && (m_properties.size() > 0)) {
                result.addAll(m_properties);
                result.remove(p);
            }
            // add the updated property
            result.add(p);
            // store the changed properties
            m_properties = result;
        }

        /**
         * Returns the image content.<p>
         *
         * @return the image content
         */
        public byte[] getContent() {

            return m_content;
        }

        /**
         * Returns the image properties.<p>
         *
         * @return the image properties
         */
        public List getProperties() {

            return m_properties;
        }

        /**
         * Returns the image VFS root path.<p>
         *
         * @return the image VFS root path
         */
        public String getRootPath() {

            return m_rootPath;
        }
    }

    /** The log object for this class. */
    public static final Log LOG = CmsLog.getLog(CmsResourceTypeExternalImage.class);

    /** 
     * The value for the {@link CmsPropertyDefinition#PROPERTY_IMAGE_SIZE} property if resources in 
     * a folder should never be downscaled.<p>
     */
    public static final String PROPERTY_VALUE_UNLIMITED = "unlimited";

    /** The image scaler for the image downscale operation (if configured). */
    private static CmsExternalImageScaler m_downScaler;

    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The static resource loader id of this resource type. */
    private static int m_staticLoaderId;

    /** The static type id of this resource type. */
    private static int m_staticTypeId;

    /** The type id of this resource type. */
    private static final int RESOURCE_TYPE_ID = 2004;

    /** The name of this resource type. */
    private static final String RESOURCE_TYPE_NAME = "external-image";

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public CmsResourceTypeExternalImage() {

        super();
        m_typeId = RESOURCE_TYPE_ID;
        m_typeName = RESOURCE_TYPE_NAME;
    }


    /**
     * Returns the static type id of this (default) resource type.<p>
     * 
     * @return the static type id of this (default) resource type
     */
    public static int getStaticTypeId() {

        return m_staticTypeId;
    }

    /**
     * Returns the static type name of this (default) resource type.<p>
     * 
     * @return the static type name of this (default) resource type
     */
    public static String getStaticTypeName() {

        return RESOURCE_TYPE_NAME;
    }

    /**
     * @see org.opencms.file.types.I_CmsResourceType#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return m_staticLoaderId;
    }

    /**
     * @see org.opencms.file.types.A_CmsResourceType#initConfiguration(java.lang.String, java.lang.String, String)
     */
    @Override
    public void initConfiguration(String name, String id, String className) throws CmsConfigurationException {

        if ((OpenCms.getRunLevel() > OpenCms.RUNLEVEL_2_INITIALIZING) && m_staticFrozen) {
            // configuration already frozen
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_CONFIG_FROZEN_3,
                this.getClass().getName(),
                getStaticTypeName(),
                new Integer(getStaticTypeId())));
        }

        if (!RESOURCE_TYPE_NAME.equals(name)) {
            // default resource type MUST have default name
            throw new CmsConfigurationException(Messages.get().container(
                Messages.ERR_INVALID_RESTYPE_CONFIG_NAME_3,
                this.getClass().getName(),
                RESOURCE_TYPE_NAME,
                name));
        }

        // freeze the configuration
        //m_staticFrozen = true;
        m_staticFrozen = false;

        super.initConfiguration(RESOURCE_TYPE_NAME, id, className);
        // set static members with values from the configuration        
        m_staticTypeId = m_typeId;

        if (CmsExternalImageLoader.isEnabled()) {
            // the image loader is enabled, image operations are supported
            m_staticLoaderId = CmsExternalImageLoader.RESOURCE_LOADER_ID_IMAGE_LOADER;
            // set the maximum size scaler
            String downScaleParams = CmsExternalImageLoader.getDownScaleParams();
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(downScaleParams)) {
                m_downScaler = new CmsExternalImageScaler(downScaleParams);
                if (!m_downScaler.isValid()) {
                    // ignore invalid parameters
                    m_downScaler = null;
                }
            }
        } else {
            // no image operations are supported, use dump loader
            m_staticLoaderId = CmsPointerLoader.RESOURCE_LOADER_ID;
            // disable maximum image size operation
            m_downScaler = null;
        }
    }

 

}