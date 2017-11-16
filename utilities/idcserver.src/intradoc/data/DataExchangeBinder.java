package intradoc.data;

public abstract interface DataExchangeBinder
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract boolean prepareNextRow(DataExchange paramDataExchange, boolean paramBoolean);

  public abstract boolean exchange(DataExchange paramDataExchange, int paramInt, boolean paramBoolean)
    throws DataException;

  public abstract void finalizeObject(DataExchange paramDataExchange, boolean paramBoolean)
    throws DataException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataExchangeBinder
 * JD-Core Version:    0.5.4
 */