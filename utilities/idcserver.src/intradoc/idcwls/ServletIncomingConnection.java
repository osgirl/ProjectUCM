/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.provider.IncomingConnection;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class ServletIncomingConnection
/*     */   implements IncomingConnection
/*     */ {
/*     */   public DataBinder m_providerData;
/*     */   public Provider m_provider;
/*     */   public String m_remoteIP;
/*     */   DirectRequestData m_directData;
/*     */   public IdcServletRequestContext m_request;
/*     */ 
/*     */   public ServletIncomingConnection()
/*     */   {
/*  35 */     this.m_providerData = null;
/*  36 */     this.m_provider = null;
/*  37 */     this.m_remoteIP = null;
/*     */   }
/*     */ 
/*     */   public void init(IdcServletRequestContext request, DirectRequestData directData)
/*     */   {
/*  51 */     this.m_request = request;
/*  52 */     this.m_directData = directData;
/*     */   }
/*     */ 
/*     */   public void checkRequestAllowed(DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/*     */   }
/*     */ 
/*     */   public InputStream getInputStream()
/*     */     throws IOException
/*     */   {
/*  76 */     if ((this.m_directData != null) && (this.m_directData.m_isDirect))
/*     */     {
/*  79 */       return this.m_directData.m_in;
/*     */     }
/*  81 */     return this.m_request.getManufacturedInputStream();
/*     */   }
/*     */ 
/*     */   public OutputStream getOutputStream()
/*     */     throws IOException
/*     */   {
/*  89 */     if ((this.m_directData != null) && (this.m_directData.m_isDirect))
/*     */     {
/*  92 */       return this.m_directData.m_out;
/*     */     }
/*  94 */     return this.m_request.getManufacturedOutputStream();
/*     */   }
/*     */ 
/*     */   public DataBinder getProviderData()
/*     */   {
/* 102 */     return this.m_providerData;
/*     */   }
/*     */ 
/*     */   public void prepareUse(DataBinder binder)
/*     */   {
/* 112 */     if (this.m_request == null)
/*     */       return;
/* 114 */     String hostAddress = (String)this.m_request.getRequestAttribute("remoteip");
/* 115 */     if (hostAddress == null)
/*     */       return;
/* 117 */     binder.setEnvironmentValue("RemoteClientHostAddress", hostAddress);
/*     */   }
/*     */ 
/*     */   public void setProviderData(DataBinder providerData)
/*     */   {
/* 127 */     this.m_providerData = providerData;
/* 128 */     String provName = this.m_providerData.getAllowMissing("ProviderName");
/* 129 */     if (provName != null)
/*     */     {
/* 131 */       this.m_provider = Providers.getProvider(provName);
/*     */     }
/* 133 */     if (this.m_provider != null)
/*     */       return;
/* 135 */     SystemUtils.trace("servlet", "No provider associated with this servlet connection");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 141 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75161 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.ServletIncomingConnection
 * JD-Core Version:    0.5.4
 */