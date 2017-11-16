package intradoc.util;

public abstract interface BasicFormatter
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";

  public abstract IdcAppendableBase format(IdcAppendableBase paramIdcAppendableBase, Object paramObject1, Object paramObject2, int paramInt)
    throws IdcException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.BasicFormatter
 * JD-Core Version:    0.5.4
 */