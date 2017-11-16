/*    */ package intradoc.resource;
/*    */ 
/*    */ public class TableMergeRule
/*    */ {
/*    */   public String m_fromTable;
/*    */   public String m_toTable;
/*    */   public String m_column;
/*    */   public int m_order;
/*    */ 
/*    */   public TableMergeRule(String from, String to, String column, int order)
/*    */   {
/* 34 */     this.m_fromTable = from;
/* 35 */     this.m_toTable = to;
/* 36 */     this.m_column = column;
/* 37 */     this.m_order = order;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 43 */     return "m_fromTable: " + this.m_fromTable + "\nm_toTable: " + this.m_toTable + "\nm_column: " + this.m_column + "\nm_order: " + this.m_order;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 49 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.TableMergeRule
 * JD-Core Version:    0.5.4
 */