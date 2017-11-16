/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ProviderConfig;
/*     */ import intradoc.provider.ProviderInterface;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.PrintStream;
/*     */ import java.util.Date;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ServletIncomingProvider
/*     */   implements ProviderInterface
/*     */ {
/*     */   public Provider m_provider;
/*     */   public boolean m_reportOnFinalize;
/*     */   public Date m_startDate;
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/*  54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86759 $";
/*     */   }
/*     */ 
/*     */   public ProviderConfig createProviderConfig() throws DataException
/*     */   {
/*  59 */     return (ProviderConfig)this.m_provider.createClass("ProviderConfig", "intradoc.provider.ProviderConfigImpl");
/*     */   }
/*     */ 
/*     */   public Provider getProvider()
/*     */   {
/*  64 */     return this.m_provider;
/*     */   }
/*     */ 
/*     */   public String getReportString(String key)
/*     */   {
/*  69 */     String msg = null;
/*  70 */     if (key.equals("startup"))
/*     */     {
/*  72 */       msg = LocaleUtils.encodeMessage("csServletReadyForRequest", null);
/*  73 */       if (this.m_reportOnFinalize)
/*     */       {
/*  75 */         System.err.println("+++ClassLoader reporting object initialized at " + this.m_startDate);
/*     */       }
/*     */     }
/*  78 */     return msg;
/*     */   }
/*     */ 
/*     */   public void init(Provider provider) throws DataException
/*     */   {
/*  83 */     this.m_provider = provider;
/*  84 */     this.m_reportOnFinalize = SharedObjects.getEnvValueAsBoolean("ReportClassLoaderRelease", true);
/*  85 */     this.m_startDate = new Date();
/*     */   }
/*     */ 
/*     */   public void pollConnectionState(DataBinder arg0, Properties arg1)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void releaseConnection()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void startProvider()
/*     */     throws DataException, ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void stopProvider()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void testConnection(DataBinder arg0, ExecutionContext arg1)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   protected void finalize()
/*     */   {
/* 116 */     if (!this.m_reportOnFinalize) {
/*     */       return;
/*     */     }
/*     */ 
/* 120 */     System.err.println("---ClassLoader for server instance object created at " + this.m_startDate + " has been collected by the Garbage Collector ");
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.ServletIncomingProvider
 * JD-Core Version:    0.5.4
 */