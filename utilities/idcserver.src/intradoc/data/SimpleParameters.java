package intradoc.data;

public abstract interface SimpleParameters extends Parameters
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";

  public abstract String get(String paramString);

  public abstract String getSystem(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.SimpleParameters
 * JD-Core Version:    0.5.4
 */