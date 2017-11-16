/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.VersionInfo;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.net.InetAddress;
/*     */ import java.net.InetSocketAddress;
/*     */ import java.net.ServerSocket;
/*     */ import java.net.Socket;
/*     */ import java.net.UnknownHostException;
/*     */ import java.util.Properties;
/*     */ import javax.net.ServerSocketFactory;
/*     */ 
/*     */ public class SocketIncomingProvider
/*     */   implements ProviderInterface, IncomingProvider
/*     */ {
/*  36 */   public static final String[] ADDRESS_CONFIG_VARS = { "ClusterNodeAddress", "ServerBindAddress", "IdcServerBindAddress" };
/*     */   protected Provider m_provider;
/*     */   protected boolean m_isInitialized;
/*     */   protected boolean m_isActive;
/*     */   protected boolean m_isReady;
/*     */   protected int m_serverPort;
/*     */   protected int m_backlog;
/*     */   protected boolean m_allowReuse;
/*     */   protected InetAddress m_serverAddress;
/*     */   protected String m_serverAddressString;
/*     */   protected DataBinder m_serverData;
/*     */   public ServerSocketFactory m_serverSocketFactory;
/*     */   protected ServerSocket m_serverSocket;
/*     */ 
/*     */   public SocketIncomingProvider()
/*     */   {
/*  40 */     this.m_provider = null;
/*     */ 
/*  42 */     this.m_isInitialized = false;
/*  43 */     this.m_isActive = false;
/*  44 */     this.m_isReady = false;
/*     */ 
/*  46 */     this.m_serverPort = 4444;
/*  47 */     this.m_backlog = 0;
/*  48 */     this.m_allowReuse = true;
/*  49 */     this.m_serverAddress = null;
/*  50 */     this.m_serverAddressString = null;
/*     */ 
/*  54 */     this.m_serverData = null;
/*     */ 
/*  56 */     this.m_serverSocketFactory = null;
/*  57 */     this.m_serverSocket = null;
/*     */   }
/*     */ 
/*     */   public void init(Provider provider) throws DataException {
/*  61 */     if (this.m_isInitialized)
/*     */     {
/*  63 */       throw new DataException("!csServerIncomingProviderAlreadyInitialized");
/*     */     }
/*     */ 
/*  66 */     this.m_provider = provider;
/*     */ 
/*  73 */     DataBinder curData = this.m_provider.getProviderData();
/*  74 */     DataBinder temp = new DataBinder();
/*  75 */     temp.merge(curData);
/*  76 */     this.m_serverData = temp;
/*     */ 
/*  78 */     String str = this.m_serverData.get("ServerPort").trim();
/*  79 */     this.m_serverPort = Integer.parseInt(str);
/*  80 */     str = this.m_serverData.getAllowMissing("ServerQueueDepth");
/*  81 */     if (str != null)
/*     */     {
/*  83 */       this.m_backlog = NumberUtils.parseInteger(str, -1);
/*  84 */       if (this.m_backlog < 1)
/*     */       {
/*  86 */         Report.warning(null, null, "csProviderIllegalServerQueueDepth", new Object[] { str });
/*     */       }
/*     */     }
/*     */ 
/*  90 */     str = this.m_serverData.getAllowMissing("ServerSocketAllowReuse");
/*  91 */     this.m_allowReuse = StringUtils.convertToBool(str, this.m_allowReuse);
/*     */ 
/*  93 */     if (this.m_serverAddress == null)
/*     */     {
/*  95 */       int len = ADDRESS_CONFIG_VARS.length;
/*     */ 
/*  97 */       for (int i = 0; (i < len) && (this.m_serverAddress == null); ++i)
/*     */       {
/*  99 */         loadSharedVariable(ADDRESS_CONFIG_VARS[i]);
/* 100 */         str = this.m_serverData.getAllowMissing(ADDRESS_CONFIG_VARS[i]);
/* 101 */         if (str == null)
/*     */           continue;
/*     */         try
/*     */         {
/* 105 */           this.m_serverAddress = InetAddress.getByName(str);
/* 106 */           this.m_serverAddressString = str;
/*     */         }
/*     */         catch (UnknownHostException e)
/*     */         {
/* 112 */           throw new DataException(LocaleUtils.encodeMessage("csUnableToParseAddress", null, str));
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 118 */     execFilterNoException("incomingSocketProviderInit", null);
/*     */   }
/*     */ 
/*     */   public void loadSharedConfig(ExecutionContext ctxt)
/*     */   {
/* 155 */     loadSharedVariable("TraceSocketRequests");
/* 156 */     loadSharedVariable("FlushInputStream");
/* 157 */     loadSharedVariable("FlushOutputStream");
/* 158 */     loadSharedVariable("SocketHostNameSecurityFilter");
/* 159 */     loadSharedVariable("SocketHostAddressSecurityFilter");
/*     */ 
/* 161 */     ProxyConnectionUtils.loadIncomingProviderProxyConfig(this, this.m_serverData, ctxt);
/*     */   }
/*     */ 
/*     */   public void loadSharedVariable(String key)
/*     */   {
/* 166 */     ProviderConfigUtils.loadSharedVariable(this.m_serverData, key);
/*     */   }
/*     */ 
/*     */   public void loadSharedTable(String key)
/*     */   {
/* 171 */     ProviderConfigUtils.loadSharedTable(this.m_serverData, key);
/*     */   }
/*     */ 
/*     */   protected String getProcessIdString()
/*     */   {
/* 176 */     String pidString = "?";
/*     */     try
/*     */     {
/* 180 */       NativeOsUtils utils = new NativeOsUtils();
/* 181 */       int pid = utils.getPid();
/* 182 */       pidString = "" + pid;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 186 */       Report.trace(null, null, t);
/*     */     }
/*     */ 
/* 189 */     return pidString;
/*     */   }
/*     */ 
/*     */   public String getReportString(String key)
/*     */   {
/* 194 */     String msg = null;
/* 195 */     if (key.equals("startup"))
/*     */     {
/* 197 */       String pidString = getProcessIdString();
/* 198 */       msg = LocaleUtils.encodeMessage("csServerWaitingForConnectionMessage", null, "" + this.m_serverPort, pidString);
/*     */ 
/* 201 */       String lookupKey = this.m_serverData.getAllowMissing("ProviderReportStringKey");
/* 202 */       if ((lookupKey == null) || (lookupKey.length() == 0))
/*     */       {
/* 204 */         lookupKey = "csServerWaitingForConnectionLogMessage";
/*     */       }
/* 206 */       Report.info("socketprotocol", null, lookupKey, new Object[] { VersionInfo.getProductVersion(), "" + this.m_serverPort, pidString });
/*     */     }
/*     */ 
/* 209 */     return msg;
/*     */   }
/*     */ 
/*     */   public ProviderConfig createProviderConfig() throws DataException
/*     */   {
/* 214 */     return (ProviderConfig)this.m_provider.createClass("ProviderConfig", "intradoc.provider.ProviderConfigImpl");
/*     */   }
/*     */ 
/*     */   public void startProvider() throws DataException, ServiceException
/*     */   {
/* 219 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 220 */     execFilter("incomingSocketProviderPreStart", cxt);
/*     */ 
/* 222 */     if (cxt.getCachedObject("incomingSocketProviderPreStart:handled") != null)
/*     */     {
/* 224 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 231 */       if (this.m_serverSocket == null)
/*     */       {
/* 233 */         this.m_serverSocket = createServerSocket(this.m_serverPort, this.m_backlog, this.m_serverAddress);
/*     */ 
/* 235 */         this.m_serverPort = this.m_serverSocket.getLocalPort();
/*     */ 
/* 237 */         this.m_provider.addProviderObject("ServerSocket", this.m_serverSocket);
/*     */       }
/* 239 */       this.m_isInitialized = true;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */       String msg;
/*     */       String msg;
/* 244 */       if (this.m_serverAddress == null)
/*     */       {
/* 246 */         msg = LocaleUtils.encodeMessage("csCouldNotListenOnPort", null, "" + this.m_serverPort);
/*     */       }
/*     */       else
/*     */       {
/* 251 */         msg = LocaleUtils.encodeMessage("csCouldNotListen", null, this.m_serverAddressString, "" + this.m_serverPort);
/*     */       }
/*     */ 
/* 254 */       throw new DataException(msg, e);
/*     */     }
/*     */ 
/* 262 */     loadSharedConfig(null);
/*     */ 
/* 264 */     Properties provState = this.m_provider.getProviderState();
/* 265 */     boolean isOnDemand = StringUtils.convertToBool(provState.getProperty("IsOnDemand"), false);
/*     */ 
/* 267 */     if (isOnDemand)
/*     */     {
/* 269 */       return;
/*     */     }
/*     */ 
/* 272 */     this.m_isActive = true;
/* 273 */     Runnable run = new Runnable()
/*     */     {
/*     */       public void run()
/*     */       {
/* 277 */         while (SocketIncomingProvider.this.m_isActive) {
/*     */           IncomingConnection connection;
/*     */           do while (true) { SocketIncomingProvider.this.m_isReady = true;
/* 280 */               connection = null;
/*     */               try
/*     */               {
/* 283 */                 connection = SocketIncomingProvider.this.accept();
/*     */               }
/*     */               catch (Exception e)
/*     */               {
/* 287 */                 String msg = LocaleUtils.encodeMessage("csIncomingAcceptFailed", null, "" + SocketIncomingProvider.this.m_serverPort);
/*     */ 
/* 289 */                 Report.error(null, msg, e);
/*     */               } }
/*     */ 
/*     */ 
/* 293 */           while (connection == null);
/*     */           try
/*     */           {
/* 300 */             Thread thread = SocketIncomingProvider.this.getConnectionThread(connection);
/* 301 */             thread.setDaemon(true);
/* 302 */             SystemUtils.startClientThread(thread);
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 306 */             e.printStackTrace();
/* 307 */             Report.fatal(null, "!csUnableToEstablishConnection", e);
/* 308 */             break label119:
/*     */           }
/*     */         }
/*     */ 
/* 312 */         label119: SocketIncomingProvider.this.close();
/* 313 */         Report.info(null, null, "csShuttingDownServerConnections", new Object[0]);
/*     */       }
/*     */     };
/* 317 */     Thread listener = new Thread(run, "tcp/" + this.m_serverPort + " listener");
/*     */ 
/* 319 */     listener.start();
/*     */ 
/* 321 */     execFilter("incomingSocketProviderPostStart", null);
/*     */   }
/*     */ 
/*     */   public void stopProvider()
/*     */   {
/* 326 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 327 */     execFilterNoException("incomingSocketProviderStop", cxt);
/* 328 */     if (cxt.getCachedObject("incomingSocketProviderStop:handled") != null)
/*     */       return;
/* 330 */     close();
/*     */   }
/*     */ 
/*     */   public Provider getProvider()
/*     */   {
/* 336 */     return this.m_provider;
/*     */   }
/*     */ 
/*     */   public void testConnection(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 342 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 343 */     execFilterNoException("incomingSocketProviderTestConnection", cxt);
/* 344 */     if ((cxt.getCachedObject("incomingSocketProviderTestConnection:handled") != null) || (
/* 346 */       (this.m_isInitialized) && (this.m_serverSocket != null)))
/*     */       return;
/* 348 */     throw new DataException("!csServerSocketNotInitialized");
/*     */   }
/*     */ 
/*     */   public void pollConnectionState(DataBinder provData, Properties provState)
/*     */   {
/* 355 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 356 */     execFilterNoException("incomingSocketProviderPollConnection", cxt);
/* 357 */     if (cxt.getCachedObject("incomingSocketProviderPollConnection:handled") != null)
/*     */       return;
/* 359 */     String str = "!csProviderStateDown";
/* 360 */     if ((this.m_isInitialized) && (this.m_serverSocket != null))
/*     */     {
/* 362 */       str = "!csProviderStateGood";
/*     */     }
/* 364 */     provState.put("ConnectionState", str);
/*     */   }
/*     */ 
/*     */   public boolean isReady()
/*     */   {
/* 373 */     return this.m_isReady;
/*     */   }
/*     */ 
/*     */   public IncomingThread getConnectionThread(IncomingConnection con) throws DataException
/*     */   {
/* 378 */     IncomingThread it = (IncomingThread)this.m_provider.createClass("IncomingThread", "intradoc.server.IdcServerThread");
/*     */ 
/* 380 */     it.init(this.m_provider, con);
/*     */ 
/* 382 */     return it;
/*     */   }
/*     */ 
/*     */   public IncomingConnection accept() throws DataException
/*     */   {
/* 387 */     SocketIncomingConnection connection = null;
/*     */     try
/*     */     {
/* 390 */       Socket clientSocket = this.m_serverSocket.accept();
/*     */ 
/* 395 */       int timeOut = SharedObjects.getTypedEnvironmentInt("ClientSocketTimeOut", 7200000, 18, 18);
/* 396 */       clientSocket.setSoTimeout(timeOut);
/*     */ 
/* 398 */       connection = (SocketIncomingConnection)this.m_provider.createClass("ProviderConnection", "intradoc.provider.SocketIncomingConnection");
/*     */ 
/* 400 */       connection.setProviderData(this.m_serverData);
/* 401 */       connection.init(clientSocket);
/*     */ 
/* 403 */       ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/* 404 */       cxt.setCachedObject("incomingSocketProviderAccept:connection", connection);
/* 405 */       execFilter("incomingSocketProviderAccept", cxt);
/*     */ 
/* 407 */       if (cxt.getCachedObject("incomingSocketProviderAccept:handled") != null)
/*     */       {
/* 409 */         Object localObject1 = null;
/*     */         Properties provState;
/*     */         return localObject1;
/*     */       }
/* 412 */       InputStream is = (InputStream)cxt.getCachedObject("inputStream");
/* 413 */       if (is != null)
/*     */       {
/* 415 */         connection.setInputStream(is);
/*     */       }
/* 417 */       OutputStream os = (OutputStream)cxt.getCachedObject("outputStream");
/* 418 */       if (os != null)
/*     */       {
/* 420 */         connection.setOutputStream(os);
/*     */       }
/* 422 */       Socket socket = (Socket)cxt.getCachedObject("socket");
/* 423 */       if (socket != null)
/*     */       {
/* 425 */         connection.m_socket = socket;
/*     */       }
/*     */ 
/* 428 */       Properties provState = this.m_provider.getProviderState();
/* 429 */       provState.put("LastActivityTs", String.valueOf(System.currentTimeMillis()));
/*     */     }
/*     */     catch (IOException provState)
/*     */     {
/*     */       Properties provState;
/* 434 */       if (this.m_isActive)
/*     */       {
/* 436 */         Report.error(null, e, "csUnableToEstablishConnection", new Object[] { "" + this.m_serverPort });
/*     */       }
/*     */     }
/*     */     catch (ServiceException provState)
/*     */     {
/*     */       Properties provState;
/* 441 */       SystemUtils.dumpException(null, e);
/*     */     }
/*     */     finally
/*     */     {
/*     */       Properties provState;
/* 448 */       if (connection == null)
/*     */       {
/* 450 */         this.m_isActive = false;
/* 451 */         this.m_isReady = false;
/* 452 */         this.m_isInitialized = false;
/*     */ 
/* 454 */         Properties provState = this.m_provider.getProviderState();
/* 455 */         provState.put("ConnectionState", "!csProviderStateDown");
/*     */       }
/*     */     }
/*     */ 
/* 459 */     return connection;
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/* 464 */     if (!this.m_isActive)
/*     */     {
/* 466 */       return;
/*     */     }
/*     */ 
/* 469 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 470 */     execFilterNoException("incomingSocketProviderClose", cxt);
/* 471 */     if (cxt.getCachedObject("incomingSocketProviderClose:handled") != null)
/*     */       return;
/*     */     try
/*     */     {
/* 475 */       this.m_isActive = false;
/*     */ 
/* 477 */       if (this.m_serverSocket != null)
/*     */       {
/* 479 */         this.m_serverSocket.close();
/*     */       }
/* 481 */       this.m_isInitialized = false;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 485 */       SystemUtils.err(e, "!csCouldNotCloseServerSocket");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void releaseConnection()
/*     */   {
/* 492 */     execFilterNoException("incomingSocketProviderReleaseConnection", null);
/*     */   }
/*     */ 
/*     */   public void finalize()
/*     */   {
/* 498 */     close();
/*     */   }
/*     */ 
/*     */   public int getServerPort()
/*     */   {
/* 506 */     return this.m_serverPort;
/*     */   }
/*     */ 
/*     */   public ServerSocket createServerSocket(int serverPort, int backlog, InetAddress serverAddress)
/*     */     throws IOException
/*     */   {
/* 514 */     if (this.m_serverSocketFactory == null)
/*     */     {
/* 516 */       this.m_serverSocketFactory = createServerSocketFactory();
/*     */     }
/*     */ 
/* 519 */     ServerSocket serverSocket = this.m_serverSocketFactory.createServerSocket();
/* 520 */     this.m_provider.addProviderObject("ServerSocket", serverSocket);
/*     */ 
/* 533 */     if (!EnvUtils.isFamily("windows"))
/*     */     {
/* 535 */       serverSocket.setReuseAddress(this.m_allowReuse);
/*     */     }
/*     */ 
/* 538 */     InetSocketAddress isa = null;
/* 539 */     if (serverAddress == null)
/*     */     {
/* 541 */       isa = new InetSocketAddress(serverPort);
/*     */     }
/*     */     else
/*     */     {
/* 545 */       isa = new InetSocketAddress(serverAddress, serverPort);
/*     */     }
/*     */ 
/* 548 */     serverSocket.bind(isa, backlog);
/* 549 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 550 */     execFilterNoException("incomingSocketProviderCreateSocket", cxt);
/*     */ 
/* 552 */     Object tmp = cxt.getCachedObject("incomingSocketProviderCreateSocket:serverSocket");
/* 553 */     if ((tmp != null) && 
/* 555 */       (tmp instanceof ServerSocket))
/*     */     {
/* 557 */       serverSocket = (ServerSocket)tmp;
/*     */     }
/*     */ 
/* 561 */     return serverSocket;
/*     */   }
/*     */ 
/*     */   public ServerSocketFactory createServerSocketFactory()
/*     */   {
/* 566 */     ServerSocketFactory sf = ServerSocketFactory.getDefault();
/*     */ 
/* 568 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 569 */     execFilterNoException("incomingSocketProviderCreateSocketFactory", cxt);
/*     */ 
/* 571 */     Object obj = cxt.getCachedObject("incomingSocketProviderCreateSocketFactory:serverSocketFactory");
/*     */ 
/* 573 */     if (obj != null)
/*     */     {
/* 575 */       sf = (ServerSocketFactory)obj;
/*     */     }
/*     */ 
/* 578 */     return sf;
/*     */   }
/*     */ 
/*     */   protected int execFilterNoException(String name, ExecutionContext cxt)
/*     */   {
/* 583 */     int retVal = 0;
/*     */     try
/*     */     {
/* 587 */       retVal = execFilter(name, cxt);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 591 */       SystemUtils.dumpException(null, e);
/*     */     }
/*     */ 
/* 594 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected int execFilter(String name, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 600 */     int retVal = 0;
/* 601 */     DataBinder provData = this.m_provider.getProviderData();
/*     */ 
/* 603 */     if (cxt == null)
/*     */     {
/* 605 */       cxt = new ExecutionContextAdaptor();
/*     */     }
/*     */ 
/* 608 */     cxt.setCachedObject("Provider", this.m_provider);
/*     */ 
/* 610 */     retVal = PluginFilters.filter(name, null, provData, cxt);
/*     */ 
/* 612 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 617 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.SocketIncomingProvider
 * JD-Core Version:    0.5.4
 */