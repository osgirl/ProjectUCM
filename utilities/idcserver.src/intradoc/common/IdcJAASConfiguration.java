/*    */ package intradoc.common;
/*    */ 
/*    */ import java.util.HashMap;
/*    */ import javax.security.auth.login.AppConfigurationEntry;
/*    */ import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
/*    */ import javax.security.auth.login.Configuration;
/*    */ 
/*    */ public class IdcJAASConfiguration extends Configuration
/*    */ {
/*    */   protected HashMap m_configuration;
/*    */ 
/*    */   public IdcJAASConfiguration()
/*    */   {
/* 27 */     this.m_configuration = new HashMap();
/*    */   }
/*    */ 
/*    */   public AppConfigurationEntry[] getAppConfigurationEntry(String name)
/*    */   {
/* 32 */     String configEntryKey = Thread.currentThread().getName() + ":" + name;
/* 33 */     HashMap configuration = (HashMap)this.m_configuration.get(configEntryKey);
/* 34 */     if (configuration == null)
/*    */     {
/* 36 */       SystemUtils.trace("gssapi", "returning null for a request for " + name + " from thread " + Thread.currentThread().getName());
/*    */ 
/* 38 */       return null;
/*    */     }
/*    */ 
/* 41 */     AppConfigurationEntry entry = new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, configuration);
/*    */ 
/* 45 */     SystemUtils.trace("gssapi", "returning for a request for " + name + " from thread " + Thread.currentThread().getName());
/*    */ 
/* 47 */     return new AppConfigurationEntry[] { entry };
/*    */   }
/*    */ 
/*    */   public void setProperty(String configEntry, String property, String value)
/*    */   {
/* 53 */     String configEntryKey = Thread.currentThread().getName() + ":" + configEntry;
/* 54 */     HashMap theEntry = (HashMap)this.m_configuration.get(configEntryKey);
/* 55 */     if (theEntry == null)
/*    */     {
/* 57 */       theEntry = new HashMap();
/* 58 */       this.m_configuration.put(configEntryKey, theEntry);
/*    */     }
/*    */ 
/* 61 */     if (value == null)
/*    */     {
/* 63 */       theEntry.remove(property);
/*    */     }
/*    */     else
/*    */     {
/* 67 */       theEntry.put(property, value);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void reset(String configEntry)
/*    */   {
/* 73 */     String configEntryKey = Thread.currentThread().getName() + ":" + configEntry;
/* 74 */     this.m_configuration.remove(configEntryKey);
/*    */   }
/*    */ 
/*    */   public void refresh()
/*    */   {
/* 80 */     SystemUtils.trace("gssapi", "refreshing Configuration");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 85 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcJAASConfiguration
 * JD-Core Version:    0.5.4
 */