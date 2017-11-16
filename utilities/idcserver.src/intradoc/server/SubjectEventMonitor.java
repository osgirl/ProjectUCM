package intradoc.server;

public abstract interface SubjectEventMonitor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract boolean checkForChange(String paramString, long paramLong);

  public abstract void handleChange(String paramString, boolean paramBoolean, long paramLong1, long paramLong2);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SubjectEventMonitor
 * JD-Core Version:    0.5.4
 */