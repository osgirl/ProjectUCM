/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.net.Socket;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import javax.net.SocketFactory;
/*     */ 
/*     */ public class SocketOutgoingConnection
/*     */   implements OutgoingConnection, ProviderConnection
/*     */ {
/*  36 */   protected int m_port = 0;
/*  37 */   protected String m_host = null;
/*  38 */   protected int m_timeoutInSec = 0;
/*     */ 
/*  40 */   protected Provider m_provider = null;
/*     */ 
/*  42 */   public Map m_conObjMap = new HashMap();
/*     */ 
/*     */   public SocketOutgoingConnection()
/*     */   {
/*     */   }
/*     */ 
/*     */   public SocketOutgoingConnection(OutputStream outStream, InputStream inStream)
/*     */   {
/*  51 */     this.m_conObjMap.put("inputStream", inStream);
/*  52 */     this.m_conObjMap.put("outputStream", outStream);
/*     */   }
/*     */ 
/*     */   public void init(ProviderConnectionManager man, DataBinder data) throws DataException
/*     */   {
/*  57 */     init(man, data, null, null, 0, null);
/*     */   }
/*     */ 
/*     */   public void init(ProviderConnectionManager man, DataBinder data, String defaultClass, Object rawConnection, int flags, Map params)
/*     */     throws DataException
/*     */   {
/*  63 */     setProviderData(data);
/*     */   }
/*     */ 
/*     */   public Object getRawConnection()
/*     */   {
/*  68 */     return this.m_conObjMap;
/*     */   }
/*     */ 
/*     */   public void init(String host, int port)
/*     */   {
/*  73 */     this.m_conObjMap.put("provider", this.m_provider);
/*  74 */     this.m_conObjMap.put("connection", this);
/*     */ 
/*  76 */     DataBinder providerData = this.m_provider.getProviderData();
/*     */ 
/*  78 */     this.m_host = host;
/*  79 */     this.m_port = port;
/*     */ 
/*  81 */     this.m_timeoutInSec = SharedObjects.getTypedEnvironmentInt("SocketTimeoutInSec", 300, 24, 24);
/*     */ 
/*  84 */     if (providerData != null)
/*     */     {
/*  86 */       String val = providerData.getLocal("ProviderTimeoutInSec");
/*  87 */       this.m_timeoutInSec = NumberUtils.parseTypedInteger(val, 300, 24, 24);
/*     */     }
/*     */ 
/*  92 */     execFilterNoException("outgoingSocketConnectionInit", null);
/*     */   }
/*     */ 
/*     */   public void connectToServer() throws ServiceException
/*     */   {
/*  97 */     Socket socket = null;
/*  98 */     boolean inError = true;
/*     */     try
/*     */     {
/* 103 */       if (this.m_host == null)
/*     */       {
/* 105 */         throw new ServiceException(-3, "!csUnableToConnect!csHostNotDefined");
/*     */       }
/*     */ 
/* 109 */       socket = createSocket(this.m_host, this.m_port);
/* 110 */       InputStream inStream = socket.getInputStream();
/* 111 */       OutputStream outStream = socket.getOutputStream();
/* 112 */       boolean flushStream = SharedObjects.getEnvValueAsBoolean("FlushInputStreamOnCloseOutgoingConnection", false);
/*     */ 
/* 114 */       this.m_conObjMap.put("socket", socket);
/* 115 */       this.m_conObjMap.put("connection", this);
/* 116 */       this.m_conObjMap.put("inputStream", inStream);
/* 117 */       this.m_conObjMap.put("outputStream", outStream);
/* 118 */       this.m_conObjMap.put("flushInputStreamOnClose", new Boolean(flushStream));
/*     */ 
/* 121 */       execFilter("outgoingSocketConnectToServer", null);
/*     */ 
/* 123 */       inError = false;
/* 124 */       this.m_conObjMap.put("connectTs", new StringBuilder().append("").append(System.currentTimeMillis()).toString());
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/* 128 */       if (socket != null)
/*     */       {
/*     */         try
/*     */         {
/* 132 */           socket.close();
/*     */         }
/*     */         catch (IOException ignore)
/*     */         {
/* 136 */           Report.trace(null, null, ignore);
/*     */         }
/*     */       }
/* 139 */       if ((e instanceof Exception) && 
/* 141 */         (this.m_provider != null));
/*     */       ServiceException se;
/*     */       String msg;
/* 155 */       throw new ServiceException(-3, msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 159 */       this.m_conObjMap.put("inError", new StringBuilder().append("").append(inError).toString());
/* 160 */       this.m_conObjMap.put("isConnected", new StringBuilder().append("").append(!inError).toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void closeServerConnection()
/*     */   {
/* 166 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 167 */     execFilterNoException("closeServerConnection", cxt);
/*     */ 
/* 170 */     if (cxt.getCachedObject("closeServerConnection:handled") != null)
/*     */       return;
/* 172 */     closeStreams();
/* 173 */     this.m_conObjMap.remove("socket");
/* 174 */     this.m_conObjMap.put("isConnected", "false");
/*     */   }
/*     */ 
/*     */   protected void closeStreams()
/*     */   {
/* 180 */     Socket sock = (Socket)this.m_conObjMap.get("socket");
/* 181 */     InputStream is = (InputStream)this.m_conObjMap.get("inputStream");
/* 182 */     OutputStream os = (OutputStream)this.m_conObjMap.get("outputStream");
/* 183 */     boolean flushInputStream = ((Boolean)this.m_conObjMap.get("flushInputStreamOnClose")).booleanValue();
/*     */     try
/*     */     {
/* 187 */       if ((flushInputStream) && (is != null))
/*     */       {
/* 190 */         byte[] tempBuf = new byte[256];
/* 191 */         while (is.available() > 0)
/*     */         {
/* 193 */           is.read(tempBuf);
/*     */         }
/*     */       }
/* 196 */       if (os != null)
/*     */       {
/* 198 */         os.flush();
/*     */       }
/*     */ 
/* 201 */       if (is != null)
/*     */       {
/* 203 */         is.close();
/*     */       }
/* 205 */       if (os != null)
/*     */       {
/* 207 */         os.close();
/*     */       }
/* 209 */       if (sock != null)
/*     */       {
/* 211 */         sock.close();
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 216 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 218 */       Report.debug(null, null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public OutputStream startRequest(Properties props)
/*     */     throws ServiceException
/*     */   {
/* 228 */     OutputStream os = (OutputStream)this.m_conObjMap.get("outputStream");
/*     */     try
/*     */     {
/* 232 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 233 */       for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*     */       {
/* 235 */         String key = (String)en.nextElement();
/* 236 */         String val = props.getProperty(key);
/* 237 */         if (key.equals("REQUEST_URL"))
/*     */         {
/* 239 */           String encodeUrl = StringUtils.urlEncode(val);
/* 240 */           val = "IdcService=GET_DYNAMIC_URL&fileUrl=";
/* 241 */           val = new StringBuilder().append(val).append(encodeUrl).toString();
/* 242 */           key = "QUERY_STRING";
/*     */         }
/* 244 */         sw.write(new StringBuilder().append(key).append("=").append(props.getProperty(key)).append("\n").toString());
/*     */       }
/* 246 */       sw.write("$$$$\n");
/*     */ 
/* 250 */       String str = sw.toStringRelease();
/* 251 */       os.write(str.getBytes());
/* 252 */       os.flush();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 256 */       throw new ServiceException(-4, "", e);
/*     */     }
/*     */ 
/* 259 */     return os;
/*     */   }
/*     */ 
/*     */   public void setProviderData(DataBinder providerData)
/*     */   {
/* 265 */     String provName = providerData.getAllowMissing("ProviderName");
/* 266 */     if (provName != null)
/*     */     {
/* 268 */       this.m_provider = Providers.getProvider(provName);
/* 269 */       this.m_host = providerData.getAllowMissing("IntradocServerHostName");
/* 270 */       this.m_port = Integer.parseInt(providerData.getAllowMissing("IntradocServerPort"));
/*     */     }
/* 272 */     if (this.m_provider != null)
/*     */       return;
/* 274 */     Report.trace("socketrequests", "No provider associated with this socket connection", null);
/*     */   }
/*     */ 
/*     */   public DataBinder getProviderData()
/*     */   {
/* 280 */     return this.m_provider.getProviderData();
/*     */   }
/*     */ 
/*     */   public Provider getProvider()
/*     */   {
/* 285 */     return this.m_provider;
/*     */   }
/*     */ 
/*     */   public InputStream getInputStream()
/*     */   {
/* 293 */     return (InputStream)this.m_conObjMap.get("inputStream");
/*     */   }
/*     */ 
/*     */   public Socket createSocket(String host, int port) throws ServiceException
/*     */   {
/* 298 */     SocketFactory sf = (SocketFactory)this.m_conObjMap.get("socketFactory");
/* 299 */     if (sf == null)
/*     */     {
/* 301 */       sf = createSocketFactory();
/*     */     }
/*     */ 
/* 304 */     if (sf == null)
/*     */     {
/* 307 */       throw new ServiceException("csSpUnableToCreateSocketFactory");
/*     */     }
/*     */ 
/* 310 */     Socket s = null;
/* 311 */     boolean inError = true;
/*     */     try
/*     */     {
/* 315 */       if (host == null)
/*     */       {
/* 317 */         throw new ServiceException(-3, "!csUnableToConnect!csHostNotDefined");
/*     */       }
/*     */ 
/* 320 */       s = sf.createSocket(host, port);
/* 321 */       s.setSoTimeout(this.m_timeoutInSec * 1000);
/* 322 */       inError = false;
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/* 326 */       Report.trace("keepalive", null, e);
/* 327 */       if (s != null)
/*     */       {
/*     */         try
/*     */         {
/* 331 */           s.close();
/*     */         }
/*     */         catch (IOException ignore)
/*     */         {
/* 335 */           Report.trace(null, null, ignore);
/*     */         }
/*     */       }
/* 338 */       if (e instanceof ServiceException)
/*     */       {
/* 340 */         ServiceException se = (ServiceException)e;
/* 341 */         throw se;
/*     */       }
/*     */       String msg;
/* 345 */       throw new ServiceException(-3, msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 349 */       this.m_conObjMap.put("inError", new StringBuilder().append("").append(inError).toString());
/*     */     }
/*     */ 
/* 352 */     return s;
/*     */   }
/*     */ 
/*     */   public SocketFactory createSocketFactory()
/*     */   {
/* 357 */     SocketFactory sf = SocketFactory.getDefault();
/*     */ 
/* 359 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 360 */     execFilterNoException("socketOutgoingConnectionCreateFactory", cxt);
/*     */ 
/* 362 */     Object tmp = cxt.getCachedObject("socketOutgoingConnectionCreateFactory:socketFactory");
/* 363 */     if (tmp != null)
/*     */     {
/* 365 */       sf = (SocketFactory)tmp;
/*     */     }
/*     */ 
/* 368 */     this.m_conObjMap.put("socketFactory", sf);
/*     */ 
/* 370 */     return sf;
/*     */   }
/*     */ 
/*     */   public boolean isBadConnection()
/*     */   {
/* 375 */     return StringUtils.convertToBool((String)this.m_conObjMap.get("inError"), false);
/*     */   }
/*     */ 
/*     */   public void prepareUse()
/*     */   {
/* 382 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 383 */     execFilterNoException("socketOutgoingConnectionPrepare", cxt);
/*     */ 
/* 385 */     if (cxt.getCachedObject("socketOutgoingConnectionPrepare:handled") != null)
/*     */       return;
/* 387 */     reset();
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 393 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 394 */     execFilterNoException("socketOutgoingConnectionReset", cxt);
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/* 400 */     closeServerConnection();
/*     */   }
/*     */ 
/*     */   public Object getConnection()
/*     */   {
/* 405 */     reset();
/* 406 */     return this;
/*     */   }
/*     */ 
/*     */   protected int execFilterNoException(String name, ExecutionContext cxt)
/*     */   {
/* 411 */     int retVal = 0;
/*     */     try
/*     */     {
/* 415 */       retVal = execFilter(name, cxt);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 419 */       SystemUtils.dumpException(null, e);
/*     */     }
/*     */ 
/* 422 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected int execFilter(String name, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 428 */     if (cxt == null)
/*     */     {
/* 430 */       cxt = new ExecutionContextAdaptor();
/*     */     }
/*     */ 
/* 433 */     cxt.setCachedObject("connectionObjects", this.m_conObjMap);
/* 434 */     cxt.setCachedObject("Provider", this.m_provider);
/*     */ 
/* 436 */     int retVal = 0;
/* 437 */     DataBinder provData = this.m_provider.getProviderData();
/*     */ 
/* 439 */     retVal = PluginFilters.filter(name, null, provData, cxt);
/*     */ 
/* 441 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 446 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100633 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.SocketOutgoingConnection
 * JD-Core Version:    0.5.4
 */