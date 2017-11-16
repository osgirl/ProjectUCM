package intradoc.idcwls;

import intradoc.common.ExecutionContext;
import intradoc.data.DataBinder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public abstract interface IdcServletRequestContext
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96980 $";

  public abstract IdcServletConfig getServletConfig();

  public abstract ExecutionContext getParentExecutionContext();

  public abstract ServletActiveLocalData getActiveData();

  public abstract DataBinder getParentDataBinder();

  public abstract String getRequestHeader(String paramString);

  public abstract Map<String, String> getCopyRequestHeaders();

  public abstract Object getRequestAttribute(String paramString);

  public abstract void setRequestAttribute(String paramString, Object paramObject);

  public abstract Object getSessionAttribute(String paramString);

  public abstract void setSessionAttribute(String paramString, Object paramObject);

  public abstract Map<String, Object> getCopySessionAttributes();

  public abstract Map getLocalParameters();

  public abstract Object getLocalParameter(String paramString);

  public abstract void setLocalParameter(String paramString, Object paramObject);

  public abstract Map<String, String[]> getCopyRequestParameters();

  public abstract void setResponseHeader(String paramString1, String paramString2);

  public abstract void addResponseHeader(String paramString1, String paramString2);

  public abstract void setHttpResponseStatusCode(int paramInt);

  public abstract void setHttpResponseErrorMessage(String paramString);

  public abstract void sendStandardHttpErrorResponse()
    throws IOException;

  public abstract boolean isHttpRequest();

  public abstract boolean isBinaryPost();

  public abstract void setIsBinaryPost(boolean paramBoolean);

  public abstract boolean getSendResponseHeadersDirect();

  public abstract void setSendResponseHeadersDirect(boolean paramBoolean);

  public abstract boolean getResponseSent();

  public abstract void setResponseSent(boolean paramBoolean);

  public abstract InputStream getServletInputStream()
    throws IOException;

  public abstract OutputStream getServletOutputStream()
    throws IOException;

  public abstract InputStream getManufacturedInputStream();

  public abstract void setManufacturedInputStream(InputStream paramInputStream);

  public abstract OutputStream getManufacturedOutputStream();

  public abstract void setManufacturedOutputStream(OutputStream paramOutputStream);

  public abstract void setCurrentConfigAsOwner();

  public abstract void setRequestUri(String paramString);

  public abstract void logout();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.IdcServletRequestContext
 * JD-Core Version:    0.5.4
 */