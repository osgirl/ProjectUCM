package intradoc.common;

public abstract interface LogWriter
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void doMessageAppend(int paramInt, String paramString, LogDirInfo paramLogDirInfo, Throwable paramThrowable);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.LogWriter
 * JD-Core Version:    0.5.4
 */