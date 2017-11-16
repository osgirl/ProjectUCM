/*    */ package intradoc.server.utils;
/*    */ 
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.Workspace;
/*    */ import java.util.Random;
/*    */ 
/*    */ public class DeletesTableUtils
/*    */ {
/* 28 */   protected static Random m_randomGen = new Random();
/*    */ 
/*    */   public static boolean insertEntry(Workspace ws, String table, String primaryKeys, String pkColumns)
/*    */     throws DataException
/*    */   {
/* 33 */     return insertEntry(ws, table, primaryKeys, pkColumns, null, null, null);
/*    */   }
/*    */ 
/*    */   public static boolean insertEntry(Workspace ws, String table, String primaryKeys, String pkColumns, String pkTypes)
/*    */     throws DataException
/*    */   {
/* 39 */     return insertEntry(ws, table, primaryKeys, pkColumns, pkTypes, null, null);
/*    */   }
/*    */ 
/*    */   public static boolean insertEntry(Workspace ws, String table, String primaryKeys, String pkColumns, String pkTypes, String sourceID, String dateStr)
/*    */     throws DataException
/*    */   {
/* 45 */     return TableModHistoryUtils.insertEntry(ws, table, primaryKeys, pkColumns, pkTypes, sourceID, dateStr, true);
/*    */   }
/*    */ 
/*    */   protected static synchronized String getNextKey()
/*    */   {
/* 54 */     long time = System.currentTimeMillis();
/*    */ 
/* 56 */     time >>= 3;
/* 57 */     time &= 1099511627775L;
/*    */ 
/* 64 */     Random newRandom = new Random();
/* 65 */     long halfMaxLong = 4611686018427387903L;
/* 66 */     long t1 = newRandom.nextLong();
/* 67 */     t1 %= halfMaxLong;
/* 68 */     long t2 = m_randomGen.nextLong();
/* 69 */     t2 %= halfMaxLong;
/* 70 */     long tot = t1 + t2;
/* 71 */     m_randomGen = new Random(tot);
/* 72 */     long keyLong = m_randomGen.nextLong();
/*    */ 
/* 74 */     keyLong &= 16777215L;
/* 75 */     String key = Long.toHexString(time).toUpperCase() + Long.toHexString(keyLong);
/* 76 */     return key;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 82 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.DeletesTableUtils
 * JD-Core Version:    0.5.4
 */