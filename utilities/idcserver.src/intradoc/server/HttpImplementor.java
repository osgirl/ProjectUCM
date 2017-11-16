package intradoc.server;

import intradoc.common.DataStreamWrapper;
import intradoc.common.ExecutionContext;
import intradoc.common.IdcLocale;
import intradoc.common.ServiceException;
import intradoc.data.DataBinder;
import intradoc.data.DataException;
import java.io.InputStream;
import java.util.TimeZone;

public abstract interface HttpImplementor
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81739 $";

  public abstract void init(Service paramService);

  public abstract void initLocale(boolean paramBoolean)
    throws DataException, ServiceException;

  public abstract void checkProcessRawData()
    throws DataException, ServiceException;

  public abstract void checkForceLogin()
    throws ServiceException;

  public abstract void checkForRevalidateLogin()
    throws ServiceException;

  public abstract void checkServerTooBusy()
    throws ServiceException;

  public abstract String createHttpResponseHeader();

  public abstract String getHttpSendResponseHeaderEncoding();

  public abstract int getMSIEVersion();

  public abstract String getBrowserVersionNumber();

  @Deprecated
  public abstract boolean doesClientAcceptGzip();

  public abstract boolean doesClientAcceptGzipEx();

  public abstract boolean useGzipCompression(ExecutionContext paramExecutionContext);

  public abstract boolean doesClientAllowApplets();

  public abstract boolean doesClientAllowSignedApplets();

  public abstract boolean isClientOS(String paramString);

  public abstract boolean isIntranetAuth();

  public abstract String getBrowserAuthType();

  public abstract String getLoginState();

  public abstract void setLoginState(String paramString);

  public abstract IdcLocale getLocale();

  public abstract TimeZone getTimeZone(IdcLocale paramIdcLocale);

  public abstract String getRedirectUrl();

  public abstract void setRedirectUrl(String paramString);

  public abstract void setPromptForLogin(boolean paramBoolean);

  public abstract boolean getPromptForLogin();

  public abstract void setServerTooBusy(boolean paramBoolean);

  public abstract boolean getServerTooBusy();

  public abstract void setUpdateLocale(boolean paramBoolean);

  public abstract boolean getUpdateLocale();

  public abstract void setGzipCompressed(boolean paramBoolean);

  public abstract boolean getGzipCompressed();

  @Deprecated
  public abstract void sendFileResponse(DataBinder paramDataBinder, String paramString1, String paramString2, String paramString3)
    throws ServiceException;

  @Deprecated
  public abstract void sendInputStreamResponse(DataBinder paramDataBinder, InputStream paramInputStream, long paramLong, String paramString1, String paramString2)
    throws ServiceException;

  public abstract void sendStreamResponse(DataBinder paramDataBinder, DataStreamWrapper paramDataStreamWrapper)
    throws ServiceException;

  public abstract void sendMultiPartResponse(DataBinder paramDataBinder)
    throws ServiceException;
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.HttpImplementor
 * JD-Core Version:    0.5.4
 */