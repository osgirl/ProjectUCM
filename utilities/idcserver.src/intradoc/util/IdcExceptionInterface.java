package intradoc.util;

import java.util.List;

public abstract interface IdcExceptionInterface extends IdcAppendableFactory
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78440 $";

  public abstract void init(Throwable paramThrowable, int paramInt, IdcMessage paramIdcMessage, String paramString);

  public abstract Throwable addCause(Throwable paramThrowable);

  public abstract List<Throwable> getCauses();

  public abstract void setIdcMessage(IdcMessage paramIdcMessage);

  public abstract IdcMessage getIdcMessage();

  public abstract void setContainerAttribute(String paramString, Object paramObject);

  public abstract Object getContainerAttribute(String paramString);

  public abstract void wrapIn(Throwable paramThrowable);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcExceptionInterface
 * JD-Core Version:    0.5.4
 */