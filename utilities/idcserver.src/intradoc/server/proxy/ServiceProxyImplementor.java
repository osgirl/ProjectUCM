/*     */ package intradoc.server.proxy;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.OutgoingProvider;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ServerRequest;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.utils.ServerInstallUtils;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Date;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ServiceProxyImplementor
/*     */   implements ProxyImplementor
/*     */ {
/*     */   public boolean checkProxy(DataBinder binder, ExecutionContext ctxt)
/*     */   {
/*  50 */     return StringUtils.convertToBool(binder.getLocal("IsProxiedRequest"), false);
/*     */   }
/*     */ 
/*     */   public void performProxyRequest(DataBinder binder, OutputStream output, ExecutionContext ctxt)
/*     */     throws ServiceException
/*     */   {
/*  57 */     Provider provider = null;
/*  58 */     String str = null;
/*  59 */     if (ctxt != null)
/*     */     {
/*  61 */       Object pObj = ctxt.getCachedObject("TargetProvider");
/*  62 */       if ((pObj != null) && (pObj instanceof Provider))
/*     */       {
/*  64 */         provider = (Provider)pObj;
/*  65 */         str = provider.getName();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  70 */     boolean isUserViewableRequest = output != null;
/*     */ 
/*  72 */     if (provider == null)
/*     */     {
/*  74 */       str = binder.getLocal("RequestProvider");
/*  75 */       if (str == null)
/*     */       {
/*  77 */         throw new ServiceException("!csProviderRequestProviderNotSpecified");
/*     */       }
/*  79 */       provider = OutgoingProviderMonitor.getOutgoingProvider(str);
/*     */     }
/*  81 */     if (provider == null)
/*     */     {
/*  83 */       String msg = LocaleUtils.encodeMessage("csOutgoingProviderNotConfigured2", null, str);
/*     */ 
/*  85 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/*  89 */     String disablePollingStr = binder.getEnvironmentValue("DisableConnectionPolling");
/*  90 */     boolean disablePolling = StringUtils.convertToBool(disablePollingStr, false);
/*  91 */     Properties props = provider.getProviderState();
/*  92 */     if (!disablePolling)
/*     */     {
/*  94 */       boolean isBad = StringUtils.convertToBool(props.getProperty("IsBadConnection"), false);
/*  95 */       if (isBad)
/*     */       {
/*  97 */         throw new ServiceException("!csOutgoingProviderConnectionDown");
/*     */       }
/*     */     }
/*     */ 
/* 101 */     Object providerObj = provider.getProvider();
/* 102 */     if (!providerObj instanceof OutgoingProvider)
/*     */     {
/* 104 */       String msg = LocaleUtils.encodeMessage("csProviderNotOutgoing2", null, str);
/*     */ 
/* 106 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 109 */     DataBinder outBinder = null;
/*     */ 
/* 112 */     String userAgent = binder.getEnvironmentValue("HTTP_USER_AGENT");
/* 113 */     String isJava = binder.getLocal("IsJava");
/*     */ 
/* 116 */     OutgoingProvider outProvider = (OutgoingProvider)providerObj;
/* 117 */     ServerRequest sr = null;
/*     */     try
/*     */     {
/* 121 */       sr = outProvider.createRequest();
/*     */ 
/* 127 */       String command = binder.getLocal("IdcService");
/* 128 */       if (!isUserViewableRequest)
/*     */       {
/* 130 */         output = null;
/* 131 */         outBinder = new DataBinder();
/*     */ 
/* 136 */         binder.putLocal("IsJava", "1");
/* 137 */         binder.setEnvironmentValue("HTTP_USER_AGENT", "JAVA");
/*     */       }
/*     */ 
/* 140 */       if (((ServerInstallUtils.isCatalogServer()) && ("FLD_CREATE_FOLDER".equalsIgnoreCase(command))) || ("FLD_REGISTER_TENANT".equalsIgnoreCase(command)))
/*     */       {
/* 145 */         outBinder = new DataBinder();
/* 146 */         binder.putLocal("isDuplicateResponse", "1");
/*     */       }
/*     */ 
/* 149 */       if ((output == null) && (outBinder == null))
/*     */       {
/* 151 */         throw new ServiceException("!csProviderOutputStreamMissing");
/*     */       }
/*     */ 
/* 155 */       binder.removeLocal("IsProxiedRequest");
/*     */ 
/* 166 */       sr.doRequest(binder, outBinder, ctxt);
/*     */ 
/* 168 */       if ((outBinder != null) && (ServerInstallUtils.isCatalogServer()))
/*     */       {
/* 172 */         ctxt.setCachedObject("libServerName", str);
/* 173 */         ctxt.setCachedObject("outBinder", outBinder);
/*     */         try
/*     */         {
/* 176 */           PluginFilters.filter("processProxiedResponse", ((Service)ctxt).getWorkspace(), binder, ctxt);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 181 */           Report.error("frameworkfolders", e, "csFilterError", new Object[] { "sniffProxiedResponse" });
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 187 */       if (output != null)
/*     */       {
/* 189 */         streamResponse(sr, binder, output, ctxt);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 198 */       if (userAgent != null)
/*     */       {
/* 200 */         binder.setEnvironmentValue("HTTP_USER_AGENT", userAgent);
/*     */       }
/* 202 */       if (isJava != null)
/*     */       {
/* 204 */         binder.putLocal("IsJava", isJava);
/*     */       }
/*     */       else
/*     */       {
/* 208 */         binder.m_isJava = false;
/* 209 */         binder.removeLocal("IsJava");
/*     */       }
/*     */ 
/* 212 */       if (sr != null)
/*     */       {
/* 214 */         sr.closeRequest(ctxt);
/*     */       }
/*     */     }
/*     */ 
/* 218 */     if (isUserViewableRequest) {
/*     */       return;
/*     */     }
/* 221 */     int statusCode = NumberUtils.parseInteger(outBinder.getLocal("StatusCode"), 0);
/* 222 */     if (statusCode >= 0)
/*     */       return;
/* 224 */     String msg = LocaleUtils.encodeMessage("csProviderRequestFailed", outBinder.getLocal("StatusMessage"), str);
/*     */ 
/* 226 */     throw new ServiceException(statusCode, msg);
/*     */   }
/*     */ 
/*     */   public void streamResponse(ServerRequest sr, DataBinder binder, OutputStream output, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 236 */     DataBinder headers = new DataBinder();
/* 237 */     String httpResponseHeaders = sr.getResponseHeaders(ctxt, headers);
/* 238 */     StringBuffer buffer = new StringBuffer(200);
/* 239 */     String contentLen = headers.getEnvironmentValue("CONTENT_LENGTH");
/* 240 */     boolean hasContentLen = (contentLen != null) && (contentLen.length() > 0);
/* 241 */     if ((httpResponseHeaders == null) || (httpResponseHeaders.length() == 0))
/*     */     {
/* 244 */       buffer.append("HTTP/1.1 200 OK\r\n");
/* 245 */       String serverSoftware = binder.getEnvironmentValue("SERVER_SOFTWARE");
/* 246 */       if (serverSoftware != null)
/*     */       {
/* 248 */         buffer.append("Server: ");
/* 249 */         buffer.append(serverSoftware);
/* 250 */         buffer.append("\r\n");
/*     */       }
/*     */ 
/* 253 */       String tstamp = LocaleUtils.formatRFC1123Date(new Date());
/* 254 */       buffer.append("Date: ");
/* 255 */       buffer.append(tstamp);
/* 256 */       buffer.append("\r\n");
/* 257 */       if (hasContentLen)
/*     */       {
/* 259 */         buffer.append("Content-Length: ");
/* 260 */         buffer.append(contentLen);
/* 261 */         buffer.append("\r\n");
/*     */       }
/* 263 */       String contentType = headers.getEnvironmentValue("CONTENT_TYPE");
/* 264 */       if ((contentType == null) || (contentType.length() == 0))
/*     */       {
/* 266 */         contentType = "text/hda";
/*     */       }
/* 268 */       buffer.append("Content-Type: ");
/* 269 */       buffer.append(contentType);
/* 270 */       buffer.append("\r\n");
/*     */     }
/*     */     else
/*     */     {
/* 274 */       buffer.append(httpResponseHeaders);
/*     */     }
/*     */ 
/* 277 */     buffer.append("\r\n");
/* 278 */     BufferedInputStream bis = sr.getResponseBodyInputStream(ctxt);
/*     */     try
/*     */     {
/* 292 */       long totLen = 0L;
/* 293 */       byte[] b = buffer.toString().getBytes();
/* 294 */       output.write(b);
/*     */ 
/* 296 */       byte[] buf = new byte[2000];
/* 297 */       int nread = -1;
/* 298 */       while ((nread = bis.read(buf)) >= 0)
/*     */       {
/* 300 */         totLen += nread;
/* 301 */         output.write(buf, 0, nread);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 311 */       String msg = LocaleUtils.encodeMessage("csProviderUnableToPipeResponse", e.getMessage());
/*     */ 
/* 313 */       Report.trace(null, LocaleResources.localizeMessage(msg, null), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void redirectCommand(Service parentService) throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 321 */       String cmd = getRedirectCommand(parentService);
/* 322 */       if (cmd != null)
/*     */       {
/* 324 */         parentService.executeService(cmd);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 329 */       throw new ServiceException("!csProviderUnableToRedirect", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getRedirectCommand(Service parentService) throws ServiceException
/*     */   {
/* 335 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 340 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98478 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.proxy.ServiceProxyImplementor
 * JD-Core Version:    0.5.4
 */