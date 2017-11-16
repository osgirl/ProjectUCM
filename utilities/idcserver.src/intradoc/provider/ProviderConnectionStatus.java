package intradoc.provider;

public abstract interface ProviderConnectionStatus
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96600 $";

  public abstract String getId();

  public abstract String getCurrentState();

  public abstract void setCurrentState(String paramString);

  public abstract void startAction(String paramString);

  public abstract String getCurrentAction();

  public abstract void setCurrentActionID(String paramString);

  public abstract String getActionID();

  public abstract void setCurrentActionStatus(String paramString);

  public abstract String getCurrentActionStatus();

  public abstract long computeTimePending(long paramLong);

  public abstract long computeTimeActive(long paramLong);

  public abstract void endAction();

  public abstract String getStackTrace();

  public abstract void setStackTrace(String paramString);

  public abstract String getServiceName();

  public abstract void setServiceName(String paramString);

  public abstract String getSubServiceName();

  public abstract void setSubServiceName(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ProviderConnectionStatus
 * JD-Core Version:    0.5.4
 */