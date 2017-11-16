package intradoc.common;

public abstract interface ReportProgress
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 77686 $";
  public static final short ERROR = -1;
  public static final short STATUS_COUNT = 0;
  public static final short STATUS_PERCENT = 1;
  public static final short FINISHED = 2;
  public static final short TRACE = 3;
  public static final short START = 4;

  public abstract void reportProgress(int paramInt, String paramString, float paramFloat1, float paramFloat2);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ReportProgress
 * JD-Core Version:    0.5.4
 */