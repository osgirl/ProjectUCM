package intradoc.util;

public abstract interface HashMapHelper<K, V>
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";

  public abstract int hashCode(Object paramObject);

  public abstract boolean equals(Object paramObject1, Object paramObject2);

  public abstract K getKey(Object paramObject);

  public abstract V getVal(Object paramObject);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.HashMapHelper
 * JD-Core Version:    0.5.4
 */