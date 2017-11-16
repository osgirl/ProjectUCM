package intradoc.server;

import intradoc.common.ServiceException;

public abstract interface SubjectManagerListener
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final int MONITOR_START = 0;
  public static final int MONITOR_END = 1;
  public static final int MONITOR_INIT = 2;

  public abstract void handleManagerEvent(int paramInt)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SubjectManagerListener
 * JD-Core Version:    0.5.4
 */