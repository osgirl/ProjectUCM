/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.data.DataResultSet;
/*    */ 
/*    */ public class LogInfo extends DataResultSet
/*    */ {
/* 29 */   public static String m_tableName = "LogInfo";
/*    */ 
/* 31 */   public static String[] COLUMNS = { "name", "indexPage", "prefix", "title" };
/*    */ 
/*    */   public DataResultSet shallowClone()
/*    */   {
/* 42 */     DataResultSet rset = new LogInfo();
/* 43 */     initShallow(rset);
/*    */ 
/* 45 */     return rset;
/*    */   }
/*    */ 
/*    */   public LogInfo()
/*    */   {
/* 50 */     super(COLUMNS);
/*    */   }
/*    */ 
/*    */   public void init()
/*    */   {
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 60 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71698 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.LogInfo
 * JD-Core Version:    0.5.4
 */