package intradoc.provider;

import intradoc.data.DataException;

public abstract interface ProviderConnectionListener
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void prepareConnection(String paramString, int paramInt, ProviderConnectionManager paramProviderConnectionManager)
    throws DataException;

  public abstract void releaseConnection(String paramString, ProviderConnectionManager paramProviderConnectionManager);

  public abstract void beginTran(ProviderConnectionManager paramProviderConnectionManager)
    throws DataException;

  public abstract void commitTran(ProviderConnectionManager paramProviderConnectionManager);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ProviderConnectionListener
 * JD-Core Version:    0.5.4
 */