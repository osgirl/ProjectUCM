package intradoc.provider;

import intradoc.data.DataBinder;
import intradoc.data.DataException;
import java.util.Map;

public abstract interface ProviderConnection
{
  public static final int F_RAW_CONNECTION_REUSED = 1;
  public static final int F_PROVIDER_CONNECTION_IS_REUSED = 2;
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 68456 $";

  public abstract void init(ProviderConnectionManager paramProviderConnectionManager, DataBinder paramDataBinder)
    throws DataException;

  public abstract void init(ProviderConnectionManager paramProviderConnectionManager, DataBinder paramDataBinder, String paramString, Object paramObject, int paramInt, Map paramMap)
    throws DataException;

  public abstract Object getRawConnection();

  public abstract void prepareUse();

  public abstract void reset();

  public abstract void close();

  public abstract boolean isBadConnection();

  public abstract Object getConnection();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ProviderConnection
 * JD-Core Version:    0.5.4
 */