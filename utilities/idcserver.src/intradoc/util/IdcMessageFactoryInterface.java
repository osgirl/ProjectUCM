package intradoc.util;

public abstract interface IdcMessageFactoryInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";

  public abstract IdcMessage newIdcMessage();

  public abstract IdcMessage newIdcMessage(String paramString, Object[] paramArrayOfObject);

  public abstract IdcMessage newIdcMessage(IdcMessage paramIdcMessage, String paramString, Object[] paramArrayOfObject);

  public abstract IdcMessage newIdcMessage(Throwable paramThrowable);

  public abstract IdcMessage newIdcMessage(Throwable paramThrowable, String paramString, Object[] paramArrayOfObject);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcMessageFactoryInterface
 * JD-Core Version:    0.5.4
 */