package intradoc.provider;

import intradoc.data.DataException;

public abstract interface IncomingProvider
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract boolean isReady();

  public abstract IncomingThread getConnectionThread(IncomingConnection paramIncomingConnection)
    throws DataException;

  public abstract IncomingConnection accept()
    throws DataException;

  public abstract void close();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.IncomingProvider
 * JD-Core Version:    0.5.4
 */