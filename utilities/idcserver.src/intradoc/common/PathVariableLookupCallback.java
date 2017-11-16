package intradoc.common;

public abstract interface PathVariableLookupCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69731 $";

  public abstract void prepareScript(PathScriptConstructInfo paramPathScriptConstructInfo, int paramInt)
    throws ServiceException;

  public abstract CharSequence executeScript(PathScriptConstructInfo paramPathScriptConstructInfo, int paramInt)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.PathVariableLookupCallback
 * JD-Core Version:    0.5.4
 */