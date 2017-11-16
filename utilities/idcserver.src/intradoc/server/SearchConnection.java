package intradoc.server;

import intradoc.data.DataBinder;
import intradoc.provider.ProviderConnection;

public abstract interface SearchConnection extends ProviderConnection
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86054 $";

  public abstract String doQuery(DataBinder paramDataBinder);

  public abstract String getResult();

  public abstract DataBinder getResultAsBinder();

  public abstract String retrieveDocInfo(String paramString1, String paramString2, int paramInt);

  public abstract void closeSession();

  public abstract Object getConnection();

  public abstract void setId(String paramString);

  public abstract String getId();

  public abstract void setIsBadConnection(boolean paramBoolean);

  public abstract long getTimeStampLastOpen();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SearchConnection
 * JD-Core Version:    0.5.4
 */