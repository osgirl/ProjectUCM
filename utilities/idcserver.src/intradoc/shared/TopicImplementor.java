package intradoc.shared;

import intradoc.common.ExecutionContext;
import intradoc.data.DataBinder;
import intradoc.data.ResultSet;

public abstract interface TopicImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init(DataBinder paramDataBinder);

  public abstract ResultSet retrieveResultSet(String paramString, DataBinder paramDataBinder, ExecutionContext paramExecutionContext);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.TopicImplementor
 * JD-Core Version:    0.5.4
 */