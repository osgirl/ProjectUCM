package intradoc.common;

public abstract interface ExecutionContext
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract Object getControllingObject();

  public abstract Object getCachedObject(String paramString);

  public abstract void setCachedObject(String paramString, Object paramObject);

  public abstract Object getReturnValue();

  public abstract void setReturnValue(Object paramObject);

  public abstract Object getLocaleResource(int paramInt);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ExecutionContext
 * JD-Core Version:    0.5.4
 */