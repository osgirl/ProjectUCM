package intradoc.common;

import intradoc.util.IdcException;

public abstract interface DebugTracingCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70600 $";

  public abstract void debugMsg(String paramString);

  public abstract void debugLockingMsg(String paramString);

  public abstract void printThreadMsg(String paramString);

  public abstract void setDebugTraceFlags()
    throws IdcException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DebugTracingCallback
 * JD-Core Version:    0.5.4
 */