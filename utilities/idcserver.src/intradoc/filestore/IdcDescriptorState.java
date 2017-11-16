package intradoc.filestore;

import intradoc.common.ExecutionContext;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import java.util.Map;

public abstract interface IdcDescriptorState
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70235 $";

  public abstract void updateMetaData(Map paramMap, ExecutionContext paramExecutionContext);

  public abstract void updateToWebless(ExecutionContext paramExecutionContext)
    throws DataException;

  public abstract void clearWebless(ExecutionContext paramExecutionContext)
    throws DataException;

  public abstract void finishUpdate(DataBinder paramDataBinder, Workspace paramWorkspace)
    throws DataException;

  public abstract boolean isUpdatedMetaData(String paramString);

  public abstract boolean isSetMetaData(String paramString);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.IdcDescriptorState
 * JD-Core Version:    0.5.4
 */