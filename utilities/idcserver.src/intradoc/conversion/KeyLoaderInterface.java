package intradoc.conversion;

import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import java.util.Map;

public abstract interface KeyLoaderInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78765 $";

  public abstract void init(Map paramMap)
    throws ServiceException;

  public abstract boolean readKeys(DataBinder paramDataBinder)
    throws ServiceException;

  public abstract boolean canUpdateKeys();

  public abstract boolean writeKeys(DataBinder paramDataBinder)
    throws ServiceException;

  public abstract void load(SecurityObjects paramSecurityObjects)
    throws DataException, ServiceException;

  public abstract void storeKey(Map paramMap, SecurityObjects paramSecurityObjects)
    throws DataException, ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.conversion.KeyLoaderInterface
 * JD-Core Version:    0.5.4
 */