package intradoc.provider;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import java.util.Properties;

public abstract interface ProviderInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init(Provider paramProvider)
    throws DataException;

  public abstract void startProvider()
    throws DataException, ServiceException;

  public abstract void stopProvider();

  public abstract Provider getProvider();

  public abstract String getReportString(String paramString);

  public abstract ProviderConfig createProviderConfig()
    throws DataException;

  public abstract void testConnection(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract void pollConnectionState(DataBinder paramDataBinder, Properties paramProperties);

  public abstract void releaseConnection();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ProviderInterface
 * JD-Core Version:    0.5.4
 */