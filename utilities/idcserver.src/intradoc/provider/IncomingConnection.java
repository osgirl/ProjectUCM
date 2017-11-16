package intradoc.provider;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract interface IncomingConnection
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract InputStream getInputStream()
    throws IOException;

  public abstract OutputStream getOutputStream()
    throws IOException;

  public abstract void setProviderData(DataBinder paramDataBinder);

  public abstract DataBinder getProviderData();

  public abstract void prepareUse(DataBinder paramDataBinder);

  public abstract void checkRequestAllowed(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract void close();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.IncomingConnection
 * JD-Core Version:    0.5.4
 */