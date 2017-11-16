package intradoc.shared;

public abstract interface MonikerInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract String getMonikerName();

  public abstract String getMonikerLocation();

  public abstract String getMoniker();

  public abstract String getSubMoniker(String paramString);

  public abstract boolean isProxied();

  public abstract String getProxiedServer();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.MonikerInterface
 * JD-Core Version:    0.5.4
 */