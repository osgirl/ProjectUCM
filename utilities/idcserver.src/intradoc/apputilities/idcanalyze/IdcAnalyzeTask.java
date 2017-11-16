package intradoc.apputilities.idcanalyze;

import intradoc.common.ServiceException;
import intradoc.data.DataException;
import intradoc.data.Workspace;
import java.util.Properties;

public abstract interface IdcAnalyzeTask
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void init(IdcAnalyzeApp paramIdcAnalyzeApp, Properties paramProperties, Workspace paramWorkspace)
    throws ServiceException, DataException;

  public abstract boolean doTask()
    throws DataException, ServiceException;

  public abstract int getErrorCount();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcanalyze.IdcAnalyzeTask
 * JD-Core Version:    0.5.4
 */