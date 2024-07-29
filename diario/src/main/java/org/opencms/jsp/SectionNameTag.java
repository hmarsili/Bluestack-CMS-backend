package org.opencms.jsp;

import org.apache.commons.lang.StringUtils;
import org.opencms.file.CmsProperty;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsException;
import com.tfsla.exceptions.ApplicationException;
import com.tfsla.opencms.util.PropertiesProvider;
import com.tfsla.opencmsdev.module.pages.PageConfiguration;
import com.tfsla.opencmsdev.module.pages.Project;
import com.tfsla.utils.CmsResourceUtils;

public class SectionNameTag extends AbstractOpenCmsTag {

    private String capitalize;
    private String uppercase;
    private String projectName;

    static private String defaultProjectName;

	static {
		loadProperties();
	}

	static void loadProperties()
	{
		PropertiesProvider properties = new PropertiesProvider(SectionPageTag.class, "tags.properties");
		defaultProjectName = properties.get("default_project_name");
	}

	static void reload()
	{
		loadProperties();
	}

    public SectionNameTag() {
		super();
		projectName = defaultProjectName;
	}

	@Override
	public int doStartTag() {
        Project project = PageConfiguration.getInstance().getProjectByName(this.getProjectName());
		String descripcionSeccion = PageConfiguration.getInstance().getSectionDescription(project, this.getContent());

        if ("true".equals(this.getCapitalize())) {
            descripcionSeccion = StringUtils.capitalize(descripcionSeccion);
        }

        if ("true".equals(this.getUppercase())) {
            descripcionSeccion = StringUtils.upperCase(descripcionSeccion);
        }

        this.getWriter().print(descripcionSeccion);
		return SKIP_BODY;
	}

	@Override
	protected String getContent() {
        CmsProperty property;
		try {
			property = CmsFlexController.getCmsObject(this.getPageContext().getRequest())
							.readPropertyObject(CmsResourceUtils.getLink(this.getAncestor().getXmlDocument().getFile())
									, this.getElement(), false);
		}
		catch (CmsException e) {
			throw new ApplicationException(e);
		}
        return (property == null || property.isNullProperty()) ? "" : property.getValue();
	}


    @Override
    public String getElement() {
        return "seccion";
    }

    public String getCapitalize() {
        return this.capitalize;
    }

    public void setCapitalize(String capitalize) {
        this.capitalize = capitalize;
    }

    public String getUppercase() {
        return uppercase;
    }

    public void setUppercase(String uppercase) {
        this.uppercase = uppercase;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

}
