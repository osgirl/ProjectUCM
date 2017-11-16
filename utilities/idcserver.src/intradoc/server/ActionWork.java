package intradoc.server;

import intradoc.common.ServiceException;
import intradoc.data.DataException;

public abstract interface ActionWork
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69463 $";

  public abstract boolean checkProxy()
    throws ServiceException;

  public abstract void preActions()
    throws ServiceException;

  public abstract void doActions()
    throws ServiceException;

  public abstract void doCode(String paramString)
    throws ServiceException, DataException;

  public abstract void postActions()
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ActionWork
 * JD-Core Version:    0.5.4
 */