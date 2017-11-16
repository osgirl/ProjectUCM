package intradoc.server.jobs;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import intradoc.data.DataResultSet;
import intradoc.data.Workspace;
import intradoc.util.IdcMessage;
import java.util.Map;

public abstract interface ScheduledJobImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74220 $";

  public abstract Object processJob(JobState paramJobState, Workspace paramWorkspace, ExecutionContext paramExecutionContext)
    throws ServiceException, DataException;

  public abstract void updateProgress(int paramInt, IdcMessage paramIdcMessage, DataBinder paramDataBinder, Map paramMap, Workspace paramWorkspace)
    throws ServiceException, DataException;

  public abstract void finishJob();

  public abstract void buildResult();

  public abstract DataResultSet createExceptionSet(DataBinder paramDataBinder);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ScheduledJobImplementor
 * JD-Core Version:    0.5.4
 */