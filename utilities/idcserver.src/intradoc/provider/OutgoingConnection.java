package intradoc.provider;

import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public abstract interface OutgoingConnection
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void connectToServer()
    throws ServiceException;

  public abstract void closeServerConnection();

  public abstract void setProviderData(DataBinder paramDataBinder);

  public abstract DataBinder getProviderData();

  public abstract OutputStream startRequest(Properties paramProperties)
    throws ServiceException;

  public abstract InputStream getInputStream();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.OutgoingConnection
 * JD-Core Version:    0.5.4
 */