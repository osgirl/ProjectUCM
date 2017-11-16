/*    */ package intradoc.server.utils;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ 
/*    */ public class ComponentListManager
/*    */ {
/*    */   protected static boolean m_isInitialized;
/*    */   protected static ComponentListEditor m_editor;
/*    */ 
/*    */   public static void reset()
/*    */   {
/* 30 */     m_isInitialized = false;
/* 31 */     m_editor = null;
/*    */   }
/*    */ 
/*    */   public static void init() throws DataException, ServiceException
/*    */   {
/* 36 */     if (m_isInitialized)
/*    */       return;
/* 38 */     m_editor = new ComponentListEditor();
/* 39 */     m_editor.init(true);
/*    */ 
/* 41 */     m_isInitialized = true;
/*    */   }
/*    */ 
/*    */   public static void init(String intradocDir, String configDir, String compDir, String homeDir)
/*    */     throws DataException, ServiceException
/*    */   {
/* 48 */     if (m_isInitialized)
/*    */       return;
/* 50 */     m_editor = new ComponentListEditor();
/* 51 */     m_editor.init(intradocDir, configDir, compDir, homeDir, null);
/*    */ 
/* 54 */     m_isInitialized = true;
/*    */   }
/*    */ 
/*    */   public static void updateLegacyInfo()
/*    */   {
/* 60 */     m_editor.updateLegacyInfo();
/*    */   }
/*    */ 
/*    */   public static ComponentFeatures getFeatures(int flags) throws DataException, ServiceException
/*    */   {
/* 65 */     ComponentFeatures f = new ComponentFeatures();
/* 66 */     f.init();
/* 67 */     m_editor.configureFeatures(f, flags);
/* 68 */     return f;
/*    */   }
/*    */ 
/*    */   public static ComponentListEditor getEditor()
/*    */   {
/* 73 */     return m_editor;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 78 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92740 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ComponentListManager
 * JD-Core Version:    0.5.4
 */