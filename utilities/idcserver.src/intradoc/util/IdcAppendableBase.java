package intradoc.util;

public abstract interface IdcAppendableBase extends Appendable
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";

  public abstract IdcAppendableBase append(char paramChar);

  public abstract IdcAppendableBase append(char[] paramArrayOfChar, int paramInt1, int paramInt2);

  public abstract IdcAppendableBase append(CharSequence paramCharSequence);

  public abstract IdcAppendableBase append(CharSequence paramCharSequence, int paramInt1, int paramInt2);

  public abstract IdcAppendableBase append(IdcAppenderBase paramIdcAppenderBase);

  public abstract IdcAppendableBase appendObject(Object paramObject);

  public abstract boolean truncate(int paramInt);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcAppendableBase
 * JD-Core Version:    0.5.4
 */