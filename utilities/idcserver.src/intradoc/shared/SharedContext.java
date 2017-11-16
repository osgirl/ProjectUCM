package intradoc.shared;

import intradoc.common.ServiceException;
import intradoc.data.DataBinder;

public abstract interface SharedContext
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void executeService(String paramString, DataBinder paramDataBinder, boolean paramBoolean)
    throws ServiceException;

  public abstract UserData getUserData();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SharedContext
 * JD-Core Version:    0.5.4
 */