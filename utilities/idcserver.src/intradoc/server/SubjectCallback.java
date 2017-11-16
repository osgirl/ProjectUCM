package intradoc.server;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;

public abstract interface SubjectCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void refresh(String paramString)
    throws DataException, ServiceException;

  public abstract void loadBinder(String paramString, DataBinder paramDataBinder, ExecutionContext paramExecutionContext);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SubjectCallback
 * JD-Core Version:    0.5.4
 */