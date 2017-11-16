package intradoc.provider;

import intradoc.common.ServiceException;
import intradoc.data.DataException;

public abstract interface OutgoingProvider
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract OutgoingConnection createConnection()
    throws ServiceException, DataException;

  public abstract ServerRequest createRequest()
    throws ServiceException, DataException;

  public abstract boolean isStarted();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.OutgoingProvider
 * JD-Core Version:    0.5.4
 */