package intradoc.common;

public abstract interface ReportDelegator extends ReportHandler
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";

  public abstract void deprecatedUsage(String paramString);

  public abstract ReportHandler getDefaultReportHandler();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ReportDelegator
 * JD-Core Version:    0.5.4
 */