/*    */ package intradoc.shared.localization;
/*    */ 
/*    */ import intradoc.common.CommonLocalizationHandlerFactory;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ 
/*    */ public class SharedLocalizationHandlerFactory
/*    */ {
/*    */   protected static boolean m_isInitialized;
/*    */   protected static Class m_handlerClass;
/*    */ 
/*    */   public static void init()
/*    */   {
/* 31 */     if (m_isInitialized)
/*    */       return;
/*    */     try
/*    */     {
/* 35 */       m_handlerClass = CommonLocalizationHandlerFactory.loadLocalizationClass("SharedLocalizationHandler");
/*    */     }
/*    */     catch (ServiceException e)
/*    */     {
/* 40 */       Report.error("localization", null, e);
/*    */     }
/* 42 */     if (m_handlerClass == null)
/*    */     {
/* 44 */       m_handlerClass = DefaultSharedLocalizationHandler.class;
/*    */     }
/* 46 */     m_isInitialized = true;
/*    */   }
/*    */ 
/*    */   public static SharedLocalizationHandler createInstance()
/*    */   {
/* 52 */     if (!m_isInitialized)
/*    */     {
/* 54 */       init();
/*    */     }
/*    */ 
/* 57 */     SharedLocalizationHandler slh = null;
/*    */     try
/*    */     {
/* 60 */       slh = (SharedLocalizationHandler)m_handlerClass.newInstance();
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 64 */       slh = new DefaultSharedLocalizationHandler();
/*    */     }
/*    */ 
/* 67 */     return slh;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 72 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83232 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.localization.SharedLocalizationHandlerFactory
 * JD-Core Version:    0.5.4
 */