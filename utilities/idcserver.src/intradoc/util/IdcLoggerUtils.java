/*    */ package intradoc.util;
/*    */ 
/*    */ import java.util.MissingResourceException;
/*    */ 
/*    */ public class IdcLoggerUtils
/*    */ {
/* 27 */   public static boolean m_internalDebug = false;
/*    */   public static String m_wrappedLogResourceName;
/* 37 */   public static volatile int m_bundleCacheCount = 0;
/*    */ 
/* 42 */   public static int m_validStringThreshold = 10;
/*    */ 
/*    */   public static Object[][] computeContents(IdcWrapperBundle wrappingBundle)
/*    */   {
/* 52 */     if (m_wrappedLogResourceName == null)
/*    */     {
/* 54 */       return (Object[][])null;
/*    */     }
/* 56 */     ClassLoader cl = Thread.currentThread().getContextClassLoader();
/* 57 */     if (cl == null)
/*    */     {
/* 59 */       return (Object[][])null;
/*    */     }
/*    */     try
/*    */     {
/* 63 */       Class logResourceClass = cl.loadClass(m_wrappedLogResourceName);
/* 64 */       IdcBundleContents bundleContents = (IdcBundleContents)logResourceClass.newInstance();
/* 65 */       return bundleContents.getResourceContents();
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 69 */       MissingResourceException missingException = new MissingResourceException("Could not load wrapped log resource name", m_wrappedLogResourceName, m_wrappedLogResourceName);
/*    */ 
/* 72 */       missingException.initCause(e);
/* 73 */       throw missingException;
/*    */     }
/*    */   }
/*    */ 
/*    */   public static void clearStringCache()
/*    */   {
/* 79 */     m_bundleCacheCount += 1;
/*    */   }
/*    */ 
/*    */   public static boolean checkUpToDate(IdcWrapperBundle wrappingBundle)
/*    */   {
/* 89 */     return wrappingBundle.m_cacheCounter == m_bundleCacheCount;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 96 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78304 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcLoggerUtils
 * JD-Core Version:    0.5.4
 */