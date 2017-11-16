package intradoc.shared;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.Workspace;

public abstract interface FilterImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
  public static final int ABORT = -1;
  public static final int CONTINUE = 0;
  public static final int FINISHED = 1;

  public abstract int doFilter(Workspace paramWorkspace, DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.FilterImplementor
 * JD-Core Version:    0.5.4
 */