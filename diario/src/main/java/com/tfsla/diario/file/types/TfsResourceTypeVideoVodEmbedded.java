package com.tfsla.diario.file.types;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.A_CmsResourceType;
import org.opencms.file.types.Messages;
import org.opencms.loader.CmsDumpLoader;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsIndexException;

import com.tfsla.diario.search.videoDataExtractor.I_TfsVideoDataExtractor;
import com.tfsla.diario.search.videoDataExtractor.I_TfsVideoDataExtractorProcess;
import com.tfsla.diario.search.videoDataExtractor.TfsEmbeddedIDExtractor;


public class TfsResourceTypeVideoVodEmbedded extends A_CmsResourceType implements I_TfsVideoDataExtractor {


    /** Indicates that the static configuration of the resource type has been frozen. */
    private static boolean m_staticFrozen;

    /** The static type id of this resource type. */
    private static int m_staticTypeId;

    /** The type id of this resource type. */
    private static final int RESOURCE_TYPE_ID = 2007;

    /** The name of this resource type. */
    private static final String RESOURCE_TYPE_NAME = "videoVod-embedded";
    
    private static final I_TfsVideoDataExtractorProcess extractor = new TfsEmbeddedIDExtractor();

	@Override
	public int getLoaderId() {
		
		return CmsDumpLoader.RESOURCE_LOADER_ID;
	}

    /**
     * Default constructor, used to initialize member variables.<p>
     */
    public TfsResourceTypeVideoVodEmbedded() {

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
        m_staticFrozen = false;
        m_frozen = false;
        
        super.initConfiguration(RESOURCE_TYPE_NAME, id, className);
        // set static members with values from the configuration        
        m_staticTypeId = m_typeId;
    }

	public String getUid(CmsObject cms, CmsResource resource)
			throws CmsIndexException, CmsException {
		return extractor.getUid(cms, resource);
	}
	
	public String getCategories(CmsObject cms, CmsResource resource)
			throws CmsIndexException, CmsException {
		return extractor.getCategories(cms, resource);
	}

}

