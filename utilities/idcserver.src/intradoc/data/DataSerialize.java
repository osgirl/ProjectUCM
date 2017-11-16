package intradoc.data;

import intradoc.common.ExecutionContext;
import intradoc.common.ServiceException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

public abstract interface DataSerialize
{
  public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";

  public abstract void setInvalidEnvObjects(Object[] paramArrayOfObject);

  public abstract boolean validateEnvironmentData(Object paramObject);

  public abstract void sendEx(DataBinder paramDataBinder, Writer paramWriter, boolean paramBoolean, ExecutionContext paramExecutionContext)
    throws IOException;

  public abstract byte[] sendBytes(DataBinder paramDataBinder, String paramString, boolean paramBoolean, ExecutionContext paramExecutionContext)
    throws IOException;

  public abstract void receiveEx(DataBinder paramDataBinder, BufferedReader paramBufferedReader, boolean paramBoolean, ExecutionContext paramExecutionContext)
    throws IOException;

  public abstract String decode(DataBinder paramDataBinder, String paramString, ExecutionContext paramExecutionContext);

  public abstract String encode(DataBinder paramDataBinder, String paramString, ExecutionContext paramExecutionContext);

  public abstract void parseRequest(DataBinder paramDataBinder, BufferedInputStream paramBufferedInputStream, ExecutionContext paramExecutionContext)
    throws IOException, DataException;

  public abstract void prepareParseRequest(DataBinder paramDataBinder, BufferedInputStream paramBufferedInputStream, ExecutionContext paramExecutionContext)
    throws IOException, DataException;

  public abstract void parseRequestBody(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws IOException, DataException;

  public abstract int determineContentType(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws DataException;

  public abstract void resetMultiContentFlags(DataBinder paramDataBinder, ExecutionContext paramExecutionContext);

  public abstract void continueParse(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws IOException, DataException, ServiceException;

  public abstract String readLineEx(DataBinder paramDataBinder, BufferedInputStream paramBufferedInputStream, boolean paramBoolean1, boolean paramBoolean2, ExecutionContext paramExecutionContext)
    throws IOException;

  public abstract void parseLocalParameters(DataBinder paramDataBinder, String paramString1, String paramString2, ExecutionContext paramExecutionContext);

  public abstract String detectEncoding(DataBinder paramDataBinder, BufferedInputStream paramBufferedInputStream, ExecutionContext paramExecutionContext)
    throws IOException;

  public abstract String packageEncodingHeader(DataBinder paramDataBinder, ExecutionContext paramExecutionContext)
    throws IOException;

  public abstract String determineEncoding(DataBinder paramDataBinder, ExecutionContext paramExecutionContext);

  public abstract String parseHdaEncodingEx(DataBinder paramDataBinder, String paramString);

  public abstract String parseHdaEncoding(String paramString);

  public abstract void setEncodingMap(ResultSet paramResultSet)
    throws DataException;

  public abstract String getIsoEncoding(String paramString);

  public abstract String getJavaEncoding(String paramString);

  public abstract void setSystemEncoding(String paramString);

  public abstract String getSystemEncoding();

  public abstract void setWebEncoding(String paramString);

  public abstract String getWebEncoding();

  public abstract void setMultiMode(boolean paramBoolean);

  public abstract boolean isMultiMode();

  public abstract void setUseClientEncoding(boolean paramBoolean);

  public abstract boolean useClientEncoding();

  public abstract void setDataBinderProtocol(DataBinderProtocolInterface paramDataBinderProtocolInterface);

  public abstract DataBinderProtocolInterface getDataBinderProtocol();
}

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataSerialize
 * JD-Core Version:    0.5.4
 */