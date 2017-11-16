package intradoc.server;

import intradoc.data.DataException;
import intradoc.data.ResultSet;
import intradoc.data.Workspace;

public abstract interface TableSerializationCallback
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract boolean allowModifyAndDelete(ResultSet paramResultSet1, ResultSet paramResultSet2, boolean paramBoolean);

  public abstract void postModification(Workspace paramWorkspace, String paramString1, ResultSet paramResultSet, String paramString2, long paramLong, boolean paramBoolean)
    throws DataException;

  public abstract void handleImportError(Workspace paramWorkspace, String paramString, ResultSet paramResultSet, boolean paramBoolean, Throwable paramThrowable);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.TableSerializationCallback
 * JD-Core Version:    0.5.4
 */