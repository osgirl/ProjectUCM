package intradoc.data;

import intradoc.common.DataStreamWrapper;
import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import java.io.IOException;

public abstract interface DataBinderProtocolInterface
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99161 $";

  public abstract void init(Workspace paramWorkspace, DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws IOException, DataException, ServiceException;

  public abstract boolean parseRequest(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws IOException, DataException;

  public abstract boolean continueParse(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws IOException, DataException, ServiceException;

  public abstract void postParseRequest(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws DataException;

  public abstract void postContinueParse(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws IOException, DataException, ServiceException;

  public abstract byte[] sendResponseBytes(DataBinder paramDataBinder, ExecutionContext paramExecutionContext, String paramString)
    throws IOException;

  public abstract String sendResponse(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws IOException;

  @Deprecated
  public abstract boolean sendFileResponse(DataBinder paramDataBinder, ExecutionContext paramExecutionContext, String paramString1, String paramString2, String paramString3)
    throws IOException;

  public abstract boolean sendStreamResponse(DataBinder paramDataBinder, DataStreamWrapper paramDataStreamWrapper, ExecutionContext paramExecutionContext)
    throws IOException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataBinderProtocolInterface
 * JD-Core Version:    0.5.4
 */