package intradoc.search;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.shared.CommonSearchConfig;

public abstract interface SearchQueryValidator
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86114 $";

  public abstract boolean validateSearchQuery(CommonSearchConfig paramCommonSearchConfig, ExecutionContext paramExecutionContext, DataBinder paramDataBinder, String[] paramArrayOfString)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchQueryValidator
 * JD-Core Version:    0.5.4
 */