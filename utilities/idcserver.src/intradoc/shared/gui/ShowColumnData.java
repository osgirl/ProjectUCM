/*    */ package intradoc.shared.gui;
/*    */ 
/*    */ import intradoc.shared.ViewFields;
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class ShowColumnData
/*    */ {
/*    */   public ViewFields m_columnFields;
/*    */   public Vector m_persistentColumns;
/*    */   public Vector m_columns;
/*    */   public Vector m_columnLabels;
/*    */   public String m_columnStr;
/*    */   public String m_viewName;
/*    */ 
/*    */   public ShowColumnData()
/*    */   {
/* 32 */     this.m_columnFields = null;
/* 33 */     this.m_persistentColumns = new IdcVector();
/*    */ 
/* 35 */     this.m_columns = null;
/* 36 */     this.m_columnLabels = null;
/* 37 */     this.m_columnStr = null;
/*    */ 
/* 39 */     this.m_viewName = null;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 43 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.ShowColumnData
 * JD-Core Version:    0.5.4
 */