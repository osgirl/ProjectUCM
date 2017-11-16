package intradoc.shared;

public abstract interface LRUManagerContainer
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract int getSize(Object paramObject);

  public abstract void discard(Object paramObject);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.LRUManagerContainer
 * JD-Core Version:    0.5.4
 */