package intradoc.server.cache;

public abstract interface IdcCacheListener
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99324 $";

  public abstract void insertEvent(IdcCacheEvent paramIdcCacheEvent);

  public abstract void updateEvent(IdcCacheEvent paramIdcCacheEvent);

  public abstract void deleteEvent(IdcCacheEvent paramIdcCacheEvent);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcCacheListener
 * JD-Core Version:    0.5.4
 */