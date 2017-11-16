/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.DataStreamWrapper;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderProtocolInterface;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.soap.SoapSerializer;
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class DataBinderProtocolImplementor
/*     */   implements DataBinderProtocolInterface
/*     */ {
/*     */   protected Workspace m_workspace;
/*     */ 
/*     */   public DataBinderProtocolImplementor()
/*     */   {
/*  31 */     this.m_workspace = null;
/*     */   }
/*     */ 
/*     */   public void init(Workspace ws, DataBinder data, ExecutionContext cxt) throws IOException, DataException, ServiceException
/*     */   {
/*  36 */     this.m_workspace = ws;
/*  37 */     cxt = getExecutionContext(cxt);
/*     */ 
/*  39 */     PluginFilters.filter("initDataBinderProtocol", ws, data, cxt);
/*     */ 
/*  41 */     if (this.m_workspace == null)
/*     */       return;
/*     */     try
/*     */     {
/*  45 */       SoapSerializer.init(ws, data, cxt);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  49 */       String errMsg = LocaleUtils.appendMessage(e.getMessage(), "!csSoapInitializationError");
/*  50 */       Report.error(null, e, "csSoapInitializationError", new Object[0]);
/*  51 */       SystemUtils.outln(LocaleResources.localizeMessage(errMsg, null));
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean parseRequest(DataBinder data, ExecutionContext cxt)
/*     */     throws IOException, DataException
/*     */   {
/*  59 */     cxt = getExecutionContext(cxt);
/*     */     try
/*     */     {
/*  63 */       int filter = PluginFilters.filter("parseDataForServiceRequest", this.m_workspace, data, cxt);
/*     */ 
/*  65 */       if (filter == 1)
/*     */       {
/*  67 */         return true;
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  72 */       throw new DataException(e.getMessage());
/*     */     }
/*     */ 
/*  75 */     boolean isSoapRequest = false;
/*  76 */     if (SoapSerializer.m_isValid)
/*     */     {
/*  78 */       isSoapRequest = SoapSerializer.parseRequest(data);
/*     */     }
/*  80 */     return isSoapRequest;
/*     */   }
/*     */ 
/*     */   public boolean continueParse(DataBinder data, ExecutionContext cxt)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/*  86 */     cxt = getExecutionContext(cxt);
/*     */ 
/*  88 */     int filter = PluginFilters.filter("continueParseDataForServiceRequest", this.m_workspace, data, cxt);
/*     */ 
/*  92 */     return filter == 1;
/*     */   }
/*     */ 
/*     */   public void postParseRequest(DataBinder data, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 101 */     cxt = getExecutionContext(cxt);
/*     */     try
/*     */     {
/* 105 */       PluginFilters.filter("postParseDataForServiceRequest", this.m_workspace, data, cxt);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 110 */       Report.trace(null, null, e);
/* 111 */       throw new DataException(e.getMessage());
/*     */     }
/*     */ 
/* 114 */     if (!SoapSerializer.m_isValid)
/*     */       return;
/* 116 */     SoapSerializer.postParseRequest(data);
/*     */   }
/*     */ 
/*     */   public void postContinueParse(DataBinder data, ExecutionContext cxt)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/* 123 */     cxt = getExecutionContext(cxt);
/*     */     try
/*     */     {
/* 127 */       PluginFilters.filter("postContinueParseDataForServiceRequest", this.m_workspace, data, cxt);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 132 */       Report.trace(null, null, e);
/* 133 */       throw new DataException(e.getMessage());
/*     */     }
/*     */ 
/* 136 */     if (!SoapSerializer.m_isValid)
/*     */       return;
/* 138 */     SoapSerializer.postParseRequest(data);
/*     */   }
/*     */ 
/*     */   public byte[] sendResponseBytes(DataBinder data, ExecutionContext cxt, String encoding)
/*     */     throws IOException
/*     */   {
/* 145 */     cxt = getExecutionContext(cxt);
/* 146 */     cxt.setCachedObject("encoding", encoding);
/*     */ 
/* 148 */     byte[] responseBytes = null;
/*     */     try
/*     */     {
/* 151 */       int filter = PluginFilters.filter("sendDataForServerResponseBytes", this.m_workspace, data, cxt);
/*     */ 
/* 153 */       if (filter == 1)
/*     */       {
/* 155 */         Object responseObj = cxt.getCachedObject("responseBytes");
/* 156 */         if ((responseObj != null) && (responseObj instanceof byte[]))
/*     */         {
/* 158 */           responseBytes = (byte[])(byte[])responseObj;
/* 159 */           return responseBytes;
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 165 */       IOException ioE = new IOException(e.getMessage());
/* 166 */       SystemUtils.setExceptionCause(ioE, e);
/* 167 */       throw ioE;
/*     */     }
/*     */ 
/* 170 */     byte[] soapResponseBytes = null;
/* 171 */     if (SoapSerializer.m_isValid)
/*     */     {
/* 173 */       soapResponseBytes = SoapSerializer.sendResponse(data, cxt, encoding);
/*     */     }
/* 175 */     return soapResponseBytes;
/*     */   }
/*     */ 
/*     */   public String sendResponse(DataBinder data, ExecutionContext cxt)
/*     */     throws IOException
/*     */   {
/* 181 */     cxt = getExecutionContext(cxt);
/*     */     try
/*     */     {
/* 185 */       int filter = PluginFilters.filter("sendDataForServerResponse", this.m_workspace, data, cxt);
/*     */ 
/* 187 */       if (filter == 1)
/*     */       {
/* 189 */         Object responseObj = cxt.getCachedObject("responseString");
/* 190 */         if ((responseObj != null) && (responseObj instanceof String))
/*     */         {
/* 192 */           String responseStr = (String)responseObj;
/* 193 */           return responseStr;
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 199 */       IOException ioE = new IOException(e.getMessage());
/* 200 */       SystemUtils.setExceptionCause(ioE, e);
/* 201 */       throw ioE;
/*     */     }
/*     */ 
/* 204 */     return null;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public boolean sendFileResponse(DataBinder data, ExecutionContext cxt, String fileName, String downloadName, String format)
/*     */     throws IOException
/*     */   {
/* 213 */     DataStreamWrapper streamWrapper = new DataStreamWrapper(fileName, downloadName, format);
/*     */ 
/* 215 */     return sendStreamResponse(data, streamWrapper, cxt);
/*     */   }
/*     */ 
/*     */   public boolean sendStreamResponse(DataBinder data, DataStreamWrapper streamWrapper, ExecutionContext cxt)
/*     */     throws IOException
/*     */   {
/* 221 */     ServiceHttpImplementor httpImplementor = (ServiceHttpImplementor)cxt.getCachedObject("HttpImplementor");
/*     */ 
/* 223 */     Service service = (Service)cxt;
/*     */ 
/* 225 */     boolean isSoapFileResponse = false;
/* 226 */     if (SoapSerializer.m_isValid)
/*     */     {
/* 228 */       isSoapFileResponse = SoapSerializer.sendStreamResponse(data, service, streamWrapper, httpImplementor);
/*     */     }
/*     */ 
/* 231 */     return isSoapFileResponse;
/*     */   }
/*     */ 
/*     */   protected ExecutionContext getExecutionContext(ExecutionContext cxt)
/*     */   {
/* 237 */     if (cxt == null)
/*     */     {
/* 239 */       cxt = new ExecutionContextAdaptor();
/*     */     }
/*     */ 
/* 242 */     return cxt;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 247 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99161 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DataBinderProtocolImplementor
 * JD-Core Version:    0.5.4
 */