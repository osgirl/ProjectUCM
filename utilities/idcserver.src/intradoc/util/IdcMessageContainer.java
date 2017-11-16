package intradoc.util;

public abstract interface IdcMessageContainer
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70600 $";
  public static final String IS_WRAPPER = "isWrapper";
  public static final String IS_WRAPPED = "isWrapped";

  public abstract IdcMessage getIdcMessage();

  public abstract void setIdcMessage(IdcMessage paramIdcMessage);

  public abstract IdcMessage appendAssociatedMessages(IdcMessage paramIdcMessage);

  public abstract Object getContainerAttribute(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcMessageContainer
 * JD-Core Version:    0.5.4
 */