package intradoc.util;

public abstract interface IdcIterable<E> extends Iterable<E>
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73841 $";

  public abstract IdcIteratorData<E> iterator();

  public abstract boolean hasNext(IdcIteratorData<E> paramIdcIteratorData);

  public abstract E next(IdcIteratorData<E> paramIdcIteratorData);

  public abstract void remove(IdcIteratorData<E> paramIdcIteratorData)
    throws UnsupportedOperationException, IllegalStateException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcIterable
 * JD-Core Version:    0.5.4
 */