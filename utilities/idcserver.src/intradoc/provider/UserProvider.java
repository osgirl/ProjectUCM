package intradoc.provider;

import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.shared.UserData;
import java.util.Properties;

public abstract interface UserProvider
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract boolean checkSynchronization(Properties paramProperties);

  public abstract void checkCredentials(UserData paramUserData, DataBinder paramDataBinder, boolean paramBoolean)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.UserProvider
 * JD-Core Version:    0.5.4
 */