package intradoc.provider;

import intradoc.common.ExecutionContext;
import intradoc.common.ReportProgress;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import java.io.BufferedInputStream;
import java.util.Properties;

public abstract interface ServerRequest
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void setOutgoingConnection(OutgoingConnection paramOutgoingConnection);

  public abstract void setRequestProperties(Properties paramProperties);

  public abstract void setReportProgress(ReportProgress paramReportProgress);

  public abstract void doRequest(DataBinder paramDataBinder1, DataBinder paramDataBinder2, ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract String getResponseHeaders(ExecutionContext paramExecutionContext, DataBinder paramDataBinder)
    throws DataException, ServiceException;

  public abstract BufferedInputStream getResponseBodyInputStream(ExecutionContext paramExecutionContext)
    throws DataException, ServiceException;

  public abstract void closeRequest(ExecutionContext paramExecutionContext);
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ServerRequest
 * JD-Core Version:    0.5.4
 */