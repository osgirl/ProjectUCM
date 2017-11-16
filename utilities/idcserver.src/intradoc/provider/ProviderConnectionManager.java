package intradoc.provider;

import intradoc.common.DebugTracingCallback;
import intradoc.data.DataException;
import java.util.Vector;

public abstract interface ProviderConnectionManager extends DebugTracingCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 68456 $";
  public static final int ACTIVE_CONNECTION = 1;
  public static final int IDLE_CONNECTION = 16;
  public static final int ANY_TYPE = 17;

  public abstract void init(Provider paramProvider)
    throws DataException;

  public abstract WorkspaceConfigImplementor createConfig(Provider paramProvider)
    throws DataException;

  public abstract ProviderConnection getConnection(String paramString)
    throws DataException;

  public abstract ProviderConnection getConnectionEx(String paramString, int paramInt)
    throws DataException;

  public abstract void reserveAccess(ProviderConnection paramProviderConnection, boolean paramBoolean)
    throws DataException;

  public abstract void releaseAccess(ProviderConnection paramProviderConnection, boolean paramBoolean);

  public abstract void releaseConnection(String paramString);

  public abstract void setForceSync(boolean paramBoolean);

  public abstract void addConnectionToPool()
    throws DataException;

  public abstract ProviderConnection createConnection(Object paramObject)
    throws DataException;

  public abstract Vector getAllConnections();

  public abstract void cleanUp();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ProviderConnectionManager
 * JD-Core Version:    0.5.4
 */