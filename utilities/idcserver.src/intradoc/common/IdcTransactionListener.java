package intradoc.common;

public abstract interface IdcTransactionListener
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final int F_DEFAULT = 0;
  public static final int F_ALLOW_NESTING = 1;
  public static final int F_BEGIN_FAILED = 2;
  public static final int F_COMMIT_FAILED = 4;
  public static final int F_ROLLBACK_FAILED = 8;

  public abstract void beginTransaction(int paramInt, ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract void commitTransaction(int paramInt, ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract void rollbackTransaction(int paramInt, ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract void closeTransactionListener(int paramInt, ExecutionContext paramExecutionContext)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcTransactionListener
 * JD-Core Version:    0.5.4
 */