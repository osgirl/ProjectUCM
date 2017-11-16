/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.MessageMaker;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.net.InetAddress;
/*     */ import java.net.Socket;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class SocketIncomingConnection
/*     */   implements IncomingConnection
/*     */ {
/*     */   public Socket m_socket;
/*     */   public InetAddress m_inetAddress;
/*     */   public InputStream m_inStream;
/*     */   public OutputStream m_outStream;
/*     */   public DataBinder m_providerData;
/*     */   public Provider m_provider;
/*     */   public boolean m_isProxied;
/*     */   public boolean m_flushInputStream;
/*     */   public boolean m_flushOutputStream;
/*     */   public boolean m_neverGetHostName;
/*     */ 
/*     */   public SocketIncomingConnection()
/*     */   {
/*  34 */     this.m_socket = null;
/*  35 */     this.m_inetAddress = null;
/*  36 */     this.m_inStream = null;
/*  37 */     this.m_outStream = null;
/*     */ 
/*  39 */     this.m_providerData = null;
/*  40 */     this.m_provider = null;
/*     */ 
/*  42 */     this.m_isProxied = false;
/*  43 */     this.m_flushInputStream = false;
/*  44 */     this.m_flushOutputStream = false;
/*  45 */     this.m_neverGetHostName = false;
/*     */   }
/*     */ 
/*     */   public void printTraceMessage(String msg) {
/*  49 */     Report.trace("socketrequests", msg, null);
/*     */   }
/*     */ 
/*     */   public void init(Socket socket) throws DataException
/*     */   {
/*  54 */     this.m_socket = socket;
/*  55 */     if (this.m_providerData == null)
/*     */     {
/*  57 */       return;
/*     */     }
/*  59 */     this.m_flushInputStream = StringUtils.convertToBool(this.m_providerData.getLocal("FlushInputStream"), this.m_flushInputStream);
/*     */ 
/*  61 */     this.m_flushOutputStream = StringUtils.convertToBool(this.m_providerData.getLocal("FlushOutputStream"), this.m_flushOutputStream);
/*     */ 
/*  63 */     printTraceMessage("Initializing connection.");
/*     */   }
/*     */ 
/*     */   public InputStream getInputStream()
/*     */     throws IOException
/*     */   {
/*  69 */     printTraceMessage("Getting input stream.");
/*  70 */     if (this.m_inStream == null)
/*     */     {
/*  72 */       this.m_inStream = this.m_socket.getInputStream();
/*     */     }
/*  74 */     return this.m_inStream;
/*     */   }
/*     */ 
/*     */   public void setInputStream(InputStream inputStream)
/*     */   {
/*  79 */     this.m_inStream = inputStream;
/*     */   }
/*     */ 
/*     */   public OutputStream getOutputStream() throws IOException
/*     */   {
/*  84 */     printTraceMessage("Getting output stream.");
/*  85 */     if (this.m_outStream == null)
/*     */     {
/*  87 */       this.m_outStream = this.m_socket.getOutputStream();
/*     */     }
/*  89 */     return this.m_outStream;
/*     */   }
/*     */ 
/*     */   public void setOutputStream(OutputStream outputStream)
/*     */   {
/*  94 */     this.m_outStream = outputStream;
/*     */   }
/*     */ 
/*     */   public void prepareUse(DataBinder binder)
/*     */   {
/* 100 */     if (binder == null)
/*     */       return;
/* 102 */     this.m_inetAddress = this.m_socket.getInetAddress();
/* 103 */     String hostAddress = this.m_inetAddress.getHostAddress();
/* 104 */     String remotePort = Integer.toString(this.m_socket.getPort());
/* 105 */     String localPort = Integer.toString(this.m_socket.getLocalPort());
/* 106 */     if (hostAddress == null)
/*     */     {
/* 108 */       hostAddress = "";
/*     */     }
/*     */ 
/* 111 */     binder.setEnvironmentValue("RemoteClientHostAddress", hostAddress);
/* 112 */     binder.setEnvironmentValue("RemoteClientRemotePort", remotePort);
/* 113 */     binder.setEnvironmentValue("RemoteClientPort", localPort);
/* 114 */     binder.setEnvironmentValue("IsSocketConnection", "1");
/* 115 */     if (SystemUtils.m_verbose)
/*     */     {
/* 117 */       String traceMsg = "RemoteClientHostAddress=" + hostAddress + " RemoveClientRemotePort=" + remotePort + " RemoteClientPort=" + localPort;
/*     */ 
/* 119 */       Report.debug("socketprotocol", traceMsg, null);
/*     */     }
/*     */ 
/* 122 */     String noReverseLookupStr = binder.getEnvironmentValue("NoReverseLookupForHost");
/* 123 */     this.m_neverGetHostName = StringUtils.convertToBool(noReverseLookupStr, false);
/* 124 */     if (!this.m_neverGetHostName)
/*     */     {
/* 126 */       String alwaysReverseLookupStr = binder.getEnvironmentValue("AlwaysReverseLookupForHost");
/* 127 */       boolean alwaysReverseLookup = StringUtils.convertToBool(alwaysReverseLookupStr, false);
/* 128 */       if (alwaysReverseLookup)
/*     */       {
/* 130 */         String hostName = this.m_inetAddress.getHostName();
/* 131 */         if ((hostName == null) || (hostName.length() == 0))
/*     */         {
/* 133 */           hostName = "<unknown>";
/*     */         }
/* 135 */         binder.setEnvironmentValue("RemoteClientHostName", hostName);
/* 136 */         if (SystemUtils.m_verbose)
/*     */         {
/* 138 */           String traceMsg = "AlwaysReverseLookupForHost mandated calculation of RemoteClientHostName=" + hostName;
/* 139 */           Report.debug("socketprotocol", traceMsg, null);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 144 */     this.m_isProxied = StringUtils.convertToBool(binder.getEnvironmentValue("IsProxied"), false);
/*     */   }
/*     */ 
/*     */   public void checkRequestAllowed(DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 151 */     String clientHostName = binder.getEnvironmentValue("RemoteClientHostName");
/* 152 */     String clientHostAddress = binder.getEnvironmentValue("RemoteClientHostAddress");
/* 153 */     Map proxyParams = null;
/* 154 */     if (cxt != null)
/*     */     {
/* 156 */       Object obj = cxt.getCachedObject("ProxyAuthParams");
/* 157 */       if ((obj != null) && (obj instanceof Map))
/*     */       {
/* 159 */         proxyParams = (Map)obj;
/*     */       }
/* 161 */       if (proxyParams == null)
/*     */       {
/* 163 */         proxyParams = getAndCheckConnectionProperties(binder, clientHostName, clientHostAddress, cxt);
/* 164 */         if (proxyParams != null)
/*     */         {
/* 166 */           cxt.setCachedObject("ProxyAuthParams", proxyParams);
/*     */         }
/*     */       }
/*     */     }
/* 170 */     if (proxyParams == null)
/*     */       return;
/* 172 */     if (proxyParams.get("pcName") == null)
/*     */     {
/* 176 */       Object o = cxt.getCachedObject("ConnectionUserName");
/* 177 */       boolean isAnonymous = (o != null) && (o.toString().equals("anonymous"));
/* 178 */       String t = this.m_providerData.getLocal("SocketHostNameSecurityFilter");
/* 179 */       if (t != null)
/*     */       {
/* 181 */         proxyParams.put("pcHostNameFilter", t);
/*     */       }
/*     */ 
/* 184 */       t = null;
/* 185 */       String localhost4 = "127.0.0.1";
/* 186 */       String localhost6 = "0:0:0:0:0:0:0:1";
/* 187 */       if (isAnonymous)
/*     */       {
/* 189 */         t = this.m_providerData.getLocal("SocketHostAnonymousAddressSecurityFilter");
/*     */       }
/* 191 */       if ((t == null) || (t.length() == 0))
/*     */       {
/* 193 */         t = this.m_providerData.getLocal("SocketHostAddressSecurityFilter");
/*     */       }
/* 195 */       if (t == null)
/*     */       {
/* 197 */         t = localhost4 + "|" + localhost6;
/*     */       }
/* 201 */       else if (t.equals(localhost4))
/*     */       {
/* 203 */         t = localhost4 + "|" + localhost6;
/*     */       }
/*     */       else
/*     */       {
/* 207 */         int index = t.indexOf(localhost4 + "|");
/* 208 */         if (index >= 0)
/*     */         {
/* 210 */           t = t.substring(0, index) + "|" + localhost6 + "|" + t.substring(index);
/*     */         }
/* 213 */         else if ((index = t.indexOf("|" + localhost4)) > 0)
/*     */         {
/* 215 */           t = t.substring(0, index + 1) + localhost6 + t.substring(index);
/*     */         }
/*     */       }
/*     */ 
/* 219 */       proxyParams.put("pcHostAddressFilter", t);
/*     */     }
/*     */ 
/* 223 */     String msg = LocaleUtils.encodeMessage("csIncomingHostNotAllowed", null, clientHostName);
/*     */ 
/* 225 */     if ((!this.m_neverGetHostName) && (((clientHostName == null) || (clientHostName.length() == 0))))
/*     */     {
/* 228 */       String curHostNameFilter = (String)proxyParams.get("pcHostNameFilter");
/* 229 */       if ((curHostNameFilter != null) && (curHostNameFilter.length() > 0))
/*     */       {
/* 231 */         clientHostName = this.m_inetAddress.getHostName();
/* 232 */         if (clientHostName == null)
/*     */         {
/* 234 */           clientHostName = "";
/*     */         }
/* 236 */         binder.setEnvironmentValue("RemoteClientHostName", clientHostName);
/*     */       }
/*     */     }
/* 239 */     checkEnvironmentValueFilter(binder, cxt, proxyParams, clientHostName, "pcHostNameFilter", msg);
/*     */ 
/* 242 */     msg = LocaleUtils.encodeMessage("csIncomingAddressNotAllowed", null, clientHostAddress);
/*     */ 
/* 244 */     checkEnvironmentValueFilter(binder, cxt, proxyParams, clientHostAddress, "pcHostAddressFilter", msg);
/*     */   }
/*     */ 
/*     */   protected Map getAndCheckConnectionProperties(DataBinder binder, String clientHostName, String clientHostAddress, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 252 */     Map proxyParams = new HashMap();
/*     */     try
/*     */     {
/* 255 */       ProxyConnectionUtils.incomingProxyAuth(proxyParams, binder, clientHostName, clientHostAddress, this, cxt);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 259 */       throw new ServiceException("!csUnableToParseConnectionInfo", e);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 263 */       throw new ServiceException("!csUnableToAuthorizeConnection", e);
/*     */     }
/* 265 */     return proxyParams;
/*     */   }
/*     */ 
/*     */   protected void checkEnvironmentValueFilter(DataBinder binder, ExecutionContext cxt, Map props, String envValue, String filterId, String errMsg)
/*     */     throws ServiceException
/*     */   {
/* 271 */     if (this.m_providerData == null)
/*     */     {
/* 274 */       throw new ServiceException("!csSecurityContextNotProvided");
/*     */     }
/*     */ 
/* 277 */     String filter = (String)props.get(filterId);
/* 278 */     if ((filter == null) || (filter.length() == 0) || (envValue == null))
/*     */     {
/* 280 */       if (SystemUtils.m_verbose)
/*     */       {
/* 282 */         if (envValue == null)
/*     */         {
/* 284 */           Report.debug("socketprotocol", "Skipped validation of " + filterId + " because value was null.", null);
/*     */         }
/*     */         else
/*     */         {
/* 288 */           Report.debug("socketprotocol", "Skipped validation of " + filterId + " because no validation value was supplied.", null);
/*     */         }
/*     */       }
/* 291 */       return;
/*     */     }
/*     */ 
/* 294 */     filter = filter.toLowerCase();
/* 295 */     envValue = envValue.toLowerCase();
/* 296 */     if (!StringUtils.match(envValue, filter, true))
/*     */     {
/* 298 */       Report.trace("socketprotocol", "Failed validation of filter " + filterId + " with value " + filter + " against value " + envValue, null);
/* 299 */       errMsg = MessageMaker.merge(errMsg, binder);
/* 300 */       throw new ServiceException(-20, errMsg);
/*     */     }
/* 302 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 304 */     Report.debug("socketprotocol", "Validated filter " + filterId + " with value " + filter + " against value " + envValue, null);
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/* 310 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 311 */     execFilterNoException("incomingSocketConnectionClose", cxt);
/* 312 */     if (cxt.getCachedObject("incomingSocketConnectionClose:handled") != null)
/*     */     {
/* 314 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 319 */       if ((this.m_inStream != null) && (this.m_flushInputStream))
/*     */       {
/* 321 */         printTraceMessage("Flushing input stream.");
/*     */ 
/* 323 */         byte[] tempBuf = new byte[256];
/* 324 */         while (this.m_inStream.available() > 0)
/*     */         {
/* 326 */           this.m_inStream.read(tempBuf);
/*     */         }
/*     */       }
/* 329 */       if ((this.m_outStream != null) && (this.m_flushOutputStream))
/*     */       {
/* 331 */         printTraceMessage("Flushing output stream.");
/* 332 */         this.m_outStream.flush();
/*     */       }
/*     */ 
/* 335 */       if (this.m_outStream != null)
/*     */       {
/* 337 */         this.m_outStream.close();
/*     */       }
/* 339 */       if (this.m_inStream != null)
/*     */       {
/* 341 */         this.m_inStream.close();
/*     */       }
/* 343 */       if (this.m_socket != null)
/*     */       {
/* 345 */         this.m_socket.close();
/*     */       }
/* 347 */       printTraceMessage("Connection closed.");
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 351 */       Report.trace(null, "Error closing socket.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setProviderData(DataBinder providerData)
/*     */   {
/* 358 */     this.m_providerData = providerData;
/* 359 */     String provName = this.m_providerData.getAllowMissing("ProviderName");
/* 360 */     if (provName != null)
/*     */     {
/* 362 */       this.m_provider = Providers.getProvider(provName);
/*     */     }
/* 364 */     if (this.m_provider != null)
/*     */       return;
/* 366 */     SystemUtils.trace("socketrequests", "No provider associated with this socket connection");
/*     */   }
/*     */ 
/*     */   public DataBinder getProviderData()
/*     */   {
/* 371 */     return this.m_providerData;
/*     */   }
/*     */ 
/*     */   public Provider getProvider()
/*     */   {
/* 376 */     return this.m_provider;
/*     */   }
/*     */ 
/*     */   protected int execFilterNoException(String name, ExecutionContext cxt)
/*     */   {
/* 381 */     int retVal = 0;
/*     */     try
/*     */     {
/* 385 */       retVal = execFilter(name, cxt);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 389 */       SystemUtils.dumpException(null, e);
/*     */     }
/*     */ 
/* 392 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected int execFilter(String name, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 398 */     int retVal = 0;
/* 399 */     DataBinder provData = this.m_provider.getProviderData();
/*     */ 
/* 401 */     if (cxt == null)
/*     */     {
/* 403 */       cxt = new ExecutionContextAdaptor();
/*     */     }
/*     */ 
/* 406 */     cxt.setCachedObject("Provider", this.m_provider);
/*     */ 
/* 408 */     retVal = PluginFilters.filter(name, null, provData, cxt);
/*     */ 
/* 410 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 415 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.SocketIncomingConnection
 * JD-Core Version:    0.5.4
 */