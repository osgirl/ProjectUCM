/*    */ package intradoc.common;
/*    */ 
/*    */ public class ResourceContainerParams
/*    */ {
/*    */   public boolean m_inResource;
/*    */   public boolean m_inScriptResource;
/*    */   public int m_curResource;
/*    */   public boolean m_isEnd;
/*    */ 
/*    */   public ResourceContainerParams()
/*    */   {
/* 22 */     this.m_inResource = false;
/* 23 */     this.m_inScriptResource = false;
/* 24 */     this.m_curResource = 0;
/* 25 */     this.m_isEnd = false;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 30 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ResourceContainerParams
 * JD-Core Version:    0.5.4
 */