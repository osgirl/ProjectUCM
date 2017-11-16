package intradoc.shared;

import intradoc.common.ExecutionContext;
import intradoc.common.IdcAppendable;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;

public abstract interface CommonSearchConfigCompanion
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final int ABORT = -1;
  public static final int CONTINUE = 0;
  public static final int FINISHED = 1;

  public abstract void init(CommonSearchConfig paramCommonSearchConfig)
    throws ServiceException;

  public abstract int fixUpAndValidateQuery(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract int prepareQuery(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract int prepareQueryText(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract int appendQueryTextFilters(DataBinder paramDataBinder, String paramString)
    throws DataException;

  public abstract int appendQueryTextFilter(IdcAppendable paramIdcAppendable, String paramString)
    throws DataException;

  public abstract String prepareFullTextQuery(String paramString, DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract String getCacheKey(String paramString1, String paramString2, String paramString3, DataBinder paramDataBinder, int paramInt, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.CommonSearchConfigCompanion
 * JD-Core Version:    0.5.4
 */