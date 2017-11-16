package intradoc.data;

public abstract interface ResultSetCopier
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66344 $";

  public abstract ResultSet copy(ResultSet paramResultSet);

  public abstract boolean isCopyAborted();

  public abstract void setRowsToSkip(int paramInt);

  public abstract void setNumRows(int paramInt);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ResultSetCopier
 * JD-Core Version:    0.5.4
 */