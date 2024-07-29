package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.relations.TfsLink;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.I_CmsMacroResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Resolves link macros for jsp pages.<p>
 * 
 * The only supported macro is the 'link' macro.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.5.4 
 */
public class TfsJspLinkMacroResolver implements I_CmsMacroResolver {

    /** Identifier for the link macro separator. */
    public static final char KEY_SEPARATOR = ':';

    /** Identifier for the link macro name. */
    public static final String MACRO_LINK = "link:";

    /** Identifier for the link macro name. */
    public static final String MACRO_LINK_STRONG = "link.strong:";

    /** Identifier for the link macro name. */
    public static final String MACRO_LINK_WEAK = "link.weak:";

    /** Identifier for link commands. */
    public static final String[] VALUE_NAME_ARRAY = {MACRO_LINK, MACRO_LINK_WEAK, MACRO_LINK_STRONG};

    /** The link commands wrapped in a List. */
    public static final List VALUE_NAMES = Collections.unmodifiableList(Arrays.asList(VALUE_NAME_ARRAY));

    /** The cms context. */
    private CmsObject m_cms;

    /** 
     * If <code>true</code> the macros get really resolved to valid vfs paths, 
     * otherwise only the path/id in the macros are updated. 
     */
    private boolean m_forRfs;

    /** The jsp root path. */
    private String m_jspRootPath;

    /** The list of links. */
    private List m_links = new ArrayList();

    /**
     * Default constructor.<p>
     * 
     * @param cms the cms context
     * @param jspRootPath the (optional) jsp root path, needed for saving from the editor to resolve relative links 
     * @param forRfs Only if <code>true</code> the macros get really resolved to valid vfs paths
     */
    public TfsJspLinkMacroResolver(CmsObject cms, String jspRootPath, boolean forRfs) {

        m_cms = cms;
        m_forRfs = forRfs;
        m_jspRootPath = jspRootPath;
    }

    /**
     * Returns the links.<p>
     *
     * @return the links
     */
    public List getLinks() {

        return m_links;
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#getMacroValue(java.lang.String)
     */
    public String getMacroValue(String macro) {

        String path = null;
        String id = null;

        // validate macro command
        Iterator it = VALUE_NAMES.iterator();
        while (it.hasNext()) {
            String cmd = it.next().toString();
            if (macro.startsWith(cmd)) {
                // a macro was found
                path = macro.substring(cmd.length());
                macro = cmd;
                break;
            }
        }
        if (path == null) {
            // this is an unknown macro, ignore it
            return null;
        }

        // we do have a valid link macro now, parse path and id
        int pos = path.indexOf(KEY_SEPARATOR);
        if ((pos > -1) && (path.length() > pos + 1)) {
            id = path.substring(pos + 1);
        }
        if (pos > -1) {
            path = path.substring(0, pos);
        }

        // check the id
        CmsUUID uuid = null;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(id)) {
            if (path != null) {
                // try to use the path as an id (in case there is only an id)
                id = path;
            } else {
                id = null;
            }
        }
        if (id != null) {
            try {
                uuid = new CmsUUID(id);
            } catch (Exception e) {
                // ignore
            }
        }

        // rewrite the path
        if ((path == null) || (path.trim().length() == 0)) {
            path = null;
        } else {
            boolean isAbsolute = (path.charAt(0) == '/');
            path = CmsFileUtil.normalizePath(path, '/');
            if (!isAbsolute) {
                path = path.substring(1);
            }
            if (isAbsolute && !path.startsWith(m_cms.getRequestContext().getSiteRoot())) {
                // add the site root if needed
                path = m_cms.getRequestContext().addSiteRoot(path);
            } else if (m_jspRootPath != null) {
                // get the site aware absolute path
                path = CmsLinkManager.getAbsoluteUri(path, CmsResource.getParentFolder(m_jspRootPath));
            }
        }

        // check the relation type
        CmsRelationType type = CmsRelationType.JSP_WEAK;
        if (macro == MACRO_LINK_STRONG) {
            type = CmsRelationType.JSP_STRONG;
        }

        // get the link object
        TfsLink link = new TfsLink("link0", type, uuid, path, true);
        link.checkConsistency(m_cms); // update id/path
        m_links.add(link);

        if (m_forRfs) {
            // return the current correct link path 
            return m_cms.getRequestContext().removeSiteRoot(link.getTarget());
        } else {
            // rewrite the macro with the right absolute path and id 
            StringBuffer newMacro = new StringBuffer(128);
            newMacro.append(I_CmsMacroResolver.MACRO_DELIMITER);
            newMacro.append(I_CmsMacroResolver.MACRO_START);
            newMacro.append(macro);
            newMacro.append(link.getVfsUri());
            if ((link.getStructureId() != null) && !link.getStructureId().isNullUUID()) {
                newMacro.append(KEY_SEPARATOR).append(link.getStructureId());
            }
            newMacro.append(I_CmsMacroResolver.MACRO_END);
            return newMacro.toString();
        }
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#isKeepEmptyMacros()
     */
    public boolean isKeepEmptyMacros() {

        return true;
    }

    /**
     * Resolves the JSP link management macros in the given input.<p>
     * 
     * Calls <code>{@link #resolveMacros(String)}</code> once for each macro in the input.
     * This means "nested" macros are not supported in this implementation, which is fine since
     * it can't happen in JSP link management anyway.<p> 
     * 
     * @see org.opencms.util.I_CmsMacroResolver#resolveMacros(java.lang.String)
     */
    public String resolveMacros(String input) {

        // clear the list of links
        m_links.clear();

        // parse the input string
        String result;
        if (input != null) {
            // resolve the macros
            result = CmsMacroResolver.resolveMacros(input, this);
        } else {
            // nothing to resolve
            result = null;
        }
        // return the result
        return result;
    }
}