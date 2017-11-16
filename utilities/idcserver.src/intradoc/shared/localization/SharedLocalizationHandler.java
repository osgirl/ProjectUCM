package intradoc.shared.localization;

import intradoc.common.ExecutionContext;
import intradoc.data.DataResultSet;
import intradoc.util.LocalizationHandlerBase;

public abstract interface SharedLocalizationHandler extends LocalizationHandlerBase
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83232 $";

  public abstract DataResultSet getTimeZones(ExecutionContext paramExecutionContext);

  public abstract void prepareTimeZonesForDisplay(DataResultSet paramDataResultSet, ExecutionContext paramExecutionContext, int paramInt);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.localization.SharedLocalizationHandler
 * JD-Core Version:    0.5.4
 */