package intradoc.common;

public abstract interface TraceParameterImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70693 $";

  public abstract void setParameter(String paramString, Object paramObject);

  public abstract Object getParameter(String paramString);

  public abstract String getStringParameter(String paramString1, String paramString2);

  public abstract int getIntegerParameter(String paramString, int paramInt);

  public abstract boolean getBooleanParameter(String paramString, boolean paramBoolean);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TraceParameterImplementor
 * JD-Core Version:    0.5.4
 */