/*    */ package intradoc.common;
/*    */ 
/*    */ public class IdcBreakpoints
/*    */ {
/*    */   public String m_fileName;
/*    */   public boolean[] m_lines;
/*    */   public boolean m_isDirty;
/*    */   public int[] m_newLineNumbers;
/*    */ 
/*    */   public IdcBreakpoints()
/*    */   {
/* 24 */     this.m_fileName = null;
/* 25 */     this.m_lines = null;
/*    */ 
/* 27 */     this.m_isDirty = false;
/* 28 */     this.m_newLineNumbers = null;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 32 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcBreakpoints
 * JD-Core Version:    0.5.4
 */