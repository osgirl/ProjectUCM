package intradoc.common;

public abstract interface IdcCharSequence extends CharSequence, IdcAppender
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void getChars(int paramInt1, int paramInt2, char[] paramArrayOfChar, int paramInt3);

  public abstract int indexOf(int paramInt1, int paramInt2, CharSequence paramCharSequence, int paramInt3, int paramInt4, boolean paramBoolean);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcCharSequence
 * JD-Core Version:    0.5.4
 */