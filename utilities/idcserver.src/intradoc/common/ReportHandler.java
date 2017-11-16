package intradoc.common;

import intradoc.util.IdcMessage;
import java.util.Date;
import java.util.Map;

public abstract interface ReportHandler
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init(Map paramMap);

  public abstract void message(String paramString1, String paramString2, int paramInt1, IdcMessage paramIdcMessage, byte[] paramArrayOfByte, int paramInt2, int paramInt3, Throwable paramThrowable, Date paramDate);

  public abstract boolean isActiveSection(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ReportHandler
 * JD-Core Version:    0.5.4
 */