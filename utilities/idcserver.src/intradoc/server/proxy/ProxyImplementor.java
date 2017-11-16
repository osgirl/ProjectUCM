package intradoc.server.proxy;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.server.Service;
import java.io.OutputStream;

public abstract interface ProxyImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract boolean checkProxy(DataBinder paramDataBinder, ExecutionContext paramExecutionContext);

  public abstract void performProxyRequest(DataBinder paramDataBinder, OutputStream paramOutputStream, ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract void redirectCommand(Service paramService)
    throws ServiceException;

  public abstract String getRedirectCommand(Service paramService)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.proxy.ProxyImplementor
 * JD-Core Version:    0.5.4
 */