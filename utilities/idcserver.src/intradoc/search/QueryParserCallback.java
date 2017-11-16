package intradoc.search;

import intradoc.common.ExecutionContext;
import intradoc.common.IdcStringBuilder;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;

public abstract interface QueryParserCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final int HANDLED = 0;
  public static final int SKIPPED = 1;
  public static final int OPERATOR = 0;
  public static final int FIELD_NAME = 1;
  public static final int VALUE = 2;
  public static final int LAST_OPERATOR = 3;
  public static final int LAST_CONJUNCTION = 4;

  public abstract int doCallback(IdcStringBuilder paramIdcStringBuilder, DataBinder paramDataBinder, ExecutionContext paramExecutionContext, String[] paramArrayOfString)
    throws ServiceException;

  public abstract boolean isQueryContainsNativeSyntax(char[] paramArrayOfChar)
    throws ServiceException;

  public abstract long getMonitoredOperators();

  public abstract boolean isFieldMonitored(String paramString);

  public abstract boolean isOperatorMonitored(long paramLong);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.QueryParserCallback
 * JD-Core Version:    0.5.4
 */