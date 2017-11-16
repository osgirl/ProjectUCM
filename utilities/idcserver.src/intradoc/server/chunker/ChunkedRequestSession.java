package intradoc.server.chunker;

import intradoc.data.DataBinder;

public abstract interface ChunkedRequestSession
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final int PROCEED = 1;
  public static final int FAILED = -1;
  public static final int EXIST = 0;

  public abstract String getSessionID();

  public abstract void setSessionID(String paramString);

  public abstract boolean openSession(DataBinder paramDataBinder);

  public abstract boolean closeSession();

  public abstract boolean isClosed();

  public abstract int verify(DataBinder paramDataBinder);

  public abstract int getTimeOut();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.chunker.ChunkedRequestSession
 * JD-Core Version:    0.5.4
 */