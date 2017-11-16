package intradoc.refinery.convert;

import intradoc.util.IdcMessage;

public abstract interface ConverterStatusInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93322 $";

  public abstract void setCurrentStatusMsg(String paramString1, String paramString2);

  public abstract void setCurrentStatusMsg(String paramString, IdcMessage paramIdcMessage);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.convert.ConverterStatusInterface
 * JD-Core Version:    0.5.4
 */