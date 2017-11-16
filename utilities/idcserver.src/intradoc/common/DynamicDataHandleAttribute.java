package intradoc.common;

public abstract interface DynamicDataHandleAttribute
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void handleTag(String paramString);

  public abstract void handleValue(String paramString1, String paramString2);

  public abstract void handleError(String paramString1, String paramString2, int paramInt1, String paramString3, int paramInt2, int paramInt3, char[] paramArrayOfChar);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicDataHandleAttribute
 * JD-Core Version:    0.5.4
 */