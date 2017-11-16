/*    */ package intradoc.common;
/*    */ 
/*    */ import javax.security.auth.login.Configuration;
/*    */ 
/*    */ public class JAASUtils
/*    */ {
/* 26 */   protected static boolean m_jaasConfigurationOverridden = false;
/* 27 */   protected static Configuration m_jaasConfigurationObject = null;
/*    */ 
/*    */   public static void setJAASConfiguration(Configuration jaasConfiguration)
/*    */   {
/* 32 */     m_jaasConfigurationObject = jaasConfiguration;
/* 33 */     Configuration.setConfiguration(m_jaasConfigurationObject);
/* 34 */     m_jaasConfigurationOverridden = true;
/*    */   }
/*    */ 
/*    */   public static Configuration getJAASConfiguration()
/*    */   {
/* 39 */     if (!m_jaasConfigurationOverridden)
/*    */     {
/* 43 */       Configuration c = new IdcJAASConfiguration();
/* 44 */       if (SystemUtils.m_verbose)
/*    */       {
/* 46 */         SystemUtils.trace("jaas", "overriding JAAS Configuration");
/*    */       }
/* 48 */       setJAASConfiguration(c);
/*    */     }
/*    */ 
/* 51 */     return m_jaasConfigurationObject;
/*    */   }
/*    */ 
/*    */   public static IdcJAASConfiguration getIdcJAASConfiguration()
/*    */     throws ServiceException
/*    */   {
/* 57 */     Configuration c = getJAASConfiguration();
/* 58 */     if (c instanceof IdcJAASConfiguration)
/*    */     {
/* 60 */       return (IdcJAASConfiguration)c;
/*    */     }
/* 62 */     throw new ServiceException("!$AJK: wrong type for JAAS Configuration object.");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 68 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.JAASUtils
 * JD-Core Version:    0.5.4
 */