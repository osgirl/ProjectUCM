/*    */ package intradoc.apps.pagebuilder;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ 
/*    */ public class PageData
/*    */ {
/*    */   public String m_pageId;
/*    */   public String m_typeId;
/*    */   public DataBinder m_binder;
/*    */   public PageData m_parent;
/*    */   public boolean m_isComplete;
/*    */   public boolean m_isLocked;
/*    */   public String m_accessLevel;
/*    */   public String m_lastChanged;
/*    */ 
/*    */   public PageData()
/*    */   {
/* 58 */     this.m_pageId = null;
/* 59 */     this.m_typeId = null;
/* 60 */     this.m_binder = new DataBinder();
/* 61 */     this.m_parent = null;
/* 62 */     this.m_isComplete = false;
/* 63 */     this.m_isLocked = false;
/* 64 */     this.m_accessLevel = "0";
/* 65 */     this.m_lastChanged = "0";
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 72 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.PageData
 * JD-Core Version:    0.5.4
 */