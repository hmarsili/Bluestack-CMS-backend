package org.opencms.ocee.db.transaction;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.ocee.base.A_CmsStatisticsDialog;
import org.opencms.ocee.base.CmsStatisticalCounterCollection;

public class CmsTransactionStatisticsDialog extends A_CmsStatisticsDialog {
   public CmsTransactionStatisticsDialog(CmsJspActionElement jsp) {
      super(jsp);
   }

   public CmsTransactionStatisticsDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {
      this(new CmsJspActionElement(context, req, res));
   }

   protected void initMessages() {
      this.addMessages(Messages.get().getBundleName());
      super.initMessages();
   }

   protected CmsStatisticalCounterCollection getCounters() {
      return CmsTransactionManager.getInstance().getStatistics();
   }
}
