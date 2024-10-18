package org.opencms.ocee.base;

import com.alkacon.diff.DiffConfiguration;
import com.alkacon.diff.HtmlDiffConfiguration;
import java.util.Locale;

public class CmsHtmlDiffConfiguration extends HtmlDiffConfiguration {
   public CmsHtmlDiffConfiguration(int lines, Locale locale) {
      super(new DiffConfiguration(lines, Messages.get().getBundleName(), "GUI_DIFF_SKIP_LINES_1", locale));
      this.setDivStyleNames("df-unc", "df-add", "df-rem", "df-skp");
      this.setSpanStyleNames("df-unc", "df-add", "df-rem");
   }
}
