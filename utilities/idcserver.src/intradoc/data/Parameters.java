package intradoc.data;

public abstract interface Parameters
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract String get(String paramString)
    throws DataException;

  public abstract String getSystem(String paramString)
    throws DataException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.Parameters
 * JD-Core Version:    0.5.4
 */