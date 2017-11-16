/*    */ package intradoc.shared;
/*    */ 
/*    */ public class QueryElementField
/*    */ {
/*    */   public String m_name;
/*    */   public int m_type;
/*    */ 
/*    */   public QueryElementField(String name, int type)
/*    */   {
/* 40 */     this.m_name = name;
/* 41 */     this.m_type = type;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 51 */     return this.m_name;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 57 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.QueryElementField
 * JD-Core Version:    0.5.4
 */