package org.opencms.ocee.base;

import org.opencms.workplace.CmsDialog;

public interface I_CmsStatisticalCounter extends Comparable {
   String getGroup();

   String getName();

   float getPosition();

   long getValue();

   void incrementValue();

   void setValue(long var1);

   String toString(CmsDialog var1);
}
