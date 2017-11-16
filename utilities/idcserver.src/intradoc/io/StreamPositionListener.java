package intradoc.io;

public abstract interface StreamPositionListener
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92976 $";

  public abstract void updatePosition(long paramLong, Object paramObject);

  public abstract void finish(long paramLong, Object paramObject);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.StreamPositionListener
 * JD-Core Version:    0.5.4
 */