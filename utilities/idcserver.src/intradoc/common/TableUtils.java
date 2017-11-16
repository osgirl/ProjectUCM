/*    */ package intradoc.common;
/*    */ 
/*    */ public class TableUtils
/*    */ {
/*    */   public static int[] getIndexList(Table table, String[] colNames)
/*    */   {
/* 31 */     int[] in = new int[colNames.length];
/* 32 */     for (int i = 0; i < colNames.length; ++i)
/*    */     {
/* 34 */       in[i] = StringUtils.findStringIndex(table.m_colNames, colNames[i]);
/*    */     }
/*    */ 
/* 37 */     return in;
/*    */   }
/*    */ 
/*    */   public static String[] getColumnAsArray(Table table, String columnName)
/*    */   {
/* 48 */     int index = StringUtils.findStringIndex(table.m_colNames, columnName);
/* 49 */     String[] arr = new String[table.getNumRows()];
/* 50 */     for (int i = 0; i < table.getNumRows(); ++i)
/*    */     {
/* 52 */       arr[i] = table.getString(i, index);
/*    */     }
/*    */ 
/* 55 */     return arr;
/*    */   }
/*    */ 
/*    */   public static String findValue(Table table, String lookupKey, String lookupFilter, String resultKey)
/*    */   {
/* 71 */     int lookupIndex = StringUtils.findStringIndex(table.m_colNames, lookupKey);
/* 72 */     for (int i = 0; i < table.getNumRows(); ++i)
/*    */     {
/* 74 */       String str = table.getString(i, lookupIndex);
/* 75 */       if (!str.equals(lookupFilter))
/*    */         continue;
/* 77 */       int resultIndex = StringUtils.findStringIndex(table.m_colNames, resultKey);
/* 78 */       return table.getString(i, resultIndex);
/*    */     }
/*    */ 
/* 82 */     return null;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 87 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95485 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TableUtils
 * JD-Core Version:    0.5.4
 */