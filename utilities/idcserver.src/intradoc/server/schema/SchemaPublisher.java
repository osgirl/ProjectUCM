package intradoc.server.schema;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import intradoc.shared.ProgressState;

public abstract interface SchemaPublisher
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73871 $";
  public static final String OP_FULL = "full";
  public static final String OP_BASE = "base";

  public abstract void init(Workspace paramWorkspace, String paramString, ExecutionContext paramExecutionContext)
    throws ServiceException;

  public abstract void doPublishing(DataBinder paramDataBinder)
    throws ServiceException, DataException;

  public abstract String getPublishDirectory();

  public abstract ProgressState getProgress();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.SchemaPublisher
 * JD-Core Version:    0.5.4
 */