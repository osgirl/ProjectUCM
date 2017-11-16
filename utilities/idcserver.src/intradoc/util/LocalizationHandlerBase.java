package intradoc.util;

public abstract interface LocalizationHandlerBase
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83241 $";

  public abstract void verifyPrerequisites()
    throws IdcException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.LocalizationHandlerBase
 * JD-Core Version:    0.5.4
 */