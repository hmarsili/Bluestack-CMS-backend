package org.opencms.jsp;

import com.tfsla.opencms.util.PropertiesProvider;
import com.tfsla.opencmsdev.module.pages.PageConfiguration;
import com.tfsla.opencmsdev.module.pages.Project;

public class SectionPageTag extends AbstractOpenCmsTag {

    private String projectName;

    static private String defaultProjectName;

	public SectionPageTag() {
		super();
		projectName = defaultProjectName;
	}

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

	@Override
	public int doStartTag() {

        Project project = PageConfiguration.getInstance().getProjectByName(this.getProjectName());
        String pageName = PageConfiguration.getInstance().getSectionPageName(project, this.getContent());

        this.getWriter().print(pageName);
		return SKIP_BODY;
	}

    public String getElement() {
       return "seccion";
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
