package intradoc.server.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public abstract interface IdcCacheRegion
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99324 $";

  public abstract String getRegionName();

  public abstract void put(String paramString, Serializable paramSerializable);

  public abstract void putAll(Map<String, Serializable> paramMap);

  public abstract Serializable get(String paramString);

  public abstract void remove(String paramString);

  public abstract void clear();

  public abstract boolean isPersistentCache();

  public abstract boolean isAutoExpiryCache();

  public abstract String getAutoExpiryTime();

  public abstract void loadAllFromPersistentStore();

  public abstract boolean isPersistenceSupported();

  public abstract boolean isEmpty();

  public abstract Set keyset();

  public abstract int size();

  public abstract IdcCacheRegion getCacheRegion(String paramString);

  public abstract void addListener(IdcCacheListener paramIdcCacheListener);

  public abstract void removeListener(IdcCacheListener paramIdcCacheListener);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcCacheRegion
 * JD-Core Version:    0.5.4
 */