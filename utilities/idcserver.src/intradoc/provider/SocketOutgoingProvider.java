/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class SocketOutgoingProvider
/*     */   implements ProviderInterface, OutgoingProvider
/*     */ {
/*     */   protected Provider m_provider;
/*     */   protected int m_port;
/*     */   protected String m_host;
/*     */ 
/*     */   public SocketOutgoingProvider()
/*     */   {
/*  33 */     this.m_provider = null;
/*     */ 
/*  36 */     this.m_host = null;
/*     */   }
/*     */ 
/*     */   public void init(Provider provider) throws DataException {
/*  40 */     this.m_provider = provider;
/*  41 */     DataBinder providerData = provider.getProviderData();
/*     */     try
/*     */     {
/*  45 */       this.m_port = Integer.parseInt(providerData.get("ServerPort").trim());
/*     */       try
/*     */       {
/*  49 */         this.m_host = providerData.get("IntradocServerHostName");
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*  53 */         this.m_host = providerData.getAllowMissing("HttpServerAddress");
/*  54 */         if (this.m_host == null)
/*     */         {
/*  58 */           throw e;
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/*  64 */       throw new DataException(e, "csUnableToInitProvider", new Object[] { provider.getName() });
/*     */     }
/*     */ 
/*  67 */     if (this.m_host != null)
/*     */     {
/*  69 */       int index = this.m_host.indexOf(":");
/*     */ 
/*  74 */       if ((index > 0) && (this.m_host.lastIndexOf(":") == index))
/*     */       {
/*  76 */         this.m_host = this.m_host.substring(0, index);
/*     */       }
/*     */     }
/*     */ 
/*  80 */     execFilterNoException("outgoingSocketProviderInit", null);
/*     */ 
/*  82 */     this.m_provider.getProviderState().put("isStarted", "1");
/*     */   }
/*     */ 
/*     */   public String getReportString(String key)
/*     */   {
/*  87 */     return null;
/*     */   }
/*     */ 
/*     */   public void startProvider() throws DataException, ServiceException
/*     */   {
/*  92 */     execFilter("outgoingSocketProviderStart", null);
/*     */ 
/*  94 */     this.m_provider.getProviderState().put("isStarted", "1");
/*     */   }
/*     */ 
/*     */   public void stopProvider()
/*     */   {
/*  99 */     execFilterNoException("outgoingSocketProviderStop", null);
/*     */ 
/* 102 */     this.m_provider.getProviderState().put("isStarted", "0");
/*     */   }
/*     */ 
/*     */   public Provider getProvider()
/*     */   {
/* 107 */     return this.m_provider;
/*     */   }
/*     */ 
/*     */   public boolean isStarted()
/*     */   {
/* 112 */     return StringUtils.convertToBool((String)this.m_provider.getProviderState().get("isStarted"), false);
/*     */   }
/*     */ 
/*     */   public ProviderConfig createProviderConfig()
/*     */     throws DataException
/*     */   {
/* 118 */     ProviderConfig pCon = (ProviderConfig)this.m_provider.createClass("ProviderConfig", "intradoc.provider.ProviderConfigImpl");
/*     */ 
/* 121 */     return pCon;
/*     */   }
/*     */ 
/*     */   public void testConnection(DataBinder binder, ExecutionContext ctxt) throws ServiceException, DataException
/*     */   {
/* 126 */     execFilterNoException("outgoingSocketProviderTestConnection", ctxt);
/*     */ 
/* 128 */     if ((ctxt != null) && (ctxt.getCachedObject("outgoingSocketProviderTestConnection:handled") != null))
/*     */       return;
/* 130 */     binder.putLocal("IdcService", "PING_SERVER");
/* 131 */     ServerRequestUtils.doAnonymousProxyRequest(this.m_provider, binder, ctxt);
/*     */   }
/*     */ 
/*     */   public void pollConnectionState(DataBinder provData, Properties provState)
/*     */   {
/* 137 */     execFilterNoException("outgoingSocketProviderPollConnection", null);
/*     */   }
/*     */ 
/*     */   public void releaseConnection()
/*     */   {
/* 142 */     execFilterNoException("outgoingSocketProviderReleaseConnection", null);
/*     */   }
/*     */ 
/*     */   public OutgoingConnection createConnection()
/*     */     throws ServiceException, DataException
/*     */   {
/* 150 */     if (!StringUtils.convertToBool((String)this.m_provider.getProviderState().get("isStarted"), false))
/*     */     {
/* 153 */       throw new ServiceException(-25, "!csConnectionNotAvailable");
/*     */     }
/*     */ 
/* 157 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 158 */     execFilter("outgoingSocketProviderPreCreateConnection", cxt);
/*     */ 
/* 160 */     SocketOutgoingConnection con = (SocketOutgoingConnection)cxt.getCachedObject("outgoingSocketProviderPreCreateConnection:connection");
/*     */ 
/* 163 */     if (con == null)
/*     */     {
/* 165 */       con = (SocketOutgoingConnection)this.m_provider.createClass("ProviderConnection", "intradoc.provider.SocketOutgoingConnection");
/*     */ 
/* 167 */       con.m_provider = this.m_provider;
/*     */     }
/*     */ 
/* 170 */     con.setProviderData(this.m_provider.getProviderData());
/* 171 */     con.init(this.m_host, this.m_port);
/* 172 */     con.connectToServer();
/*     */ 
/* 174 */     execFilter("outgoingSocketProviderPostCreateConnection", cxt);
/*     */ 
/* 176 */     return con;
/*     */   }
/*     */ 
/*     */   public ServerRequest createRequest() throws ServiceException, DataException
/*     */   {
/* 181 */     OutgoingConnection con = createConnection();
/* 182 */     if (con == null)
/*     */     {
/* 184 */       throw new ServiceException(-25, "!csUnableToCreateConnectionToServer");
/*     */     }
/*     */ 
/* 188 */     ServerRequest sr = (ServerRequest)this.m_provider.createClass("ProviderRequest", "intradoc.provider.StandardServerRequest");
/*     */ 
/* 190 */     sr.setOutgoingConnection(con);
/*     */ 
/* 192 */     return sr;
/*     */   }
/*     */ 
/*     */   protected int execFilterNoException(String name, ExecutionContext cxt)
/*     */   {
/* 197 */     int retVal = 0;
/*     */     try
/*     */     {
/* 201 */       retVal = execFilter(name, cxt);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 205 */       SystemUtils.dumpException(null, e);
/*     */     }
/*     */ 
/* 208 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected int execFilter(String name, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 214 */     int retVal = 0;
/* 215 */     DataBinder provData = this.m_provider.getProviderData();
/*     */ 
/* 217 */     if (cxt == null)
/*     */     {
/* 219 */       cxt = new ExecutionContextAdaptor();
/*     */     }
/*     */ 
/* 222 */     cxt.setCachedObject("Provider", this.m_provider);
/*     */ 
/* 224 */     retVal = PluginFilters.filter(name, null, provData, cxt);
/*     */ 
/* 226 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 231 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82333 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.SocketOutgoingProvider
 * JD-Core Version:    0.5.4
 */