/*    */ package intradoc.shared;
/*    */ 
/*    */ public class QueryOperatorUtils
/*    */ {
/* 24 */   public static final String[][] ALIASMAP = { { "matches", "equals" }, { "substring", "hasAsSubstring" }, { "contain", "hasAsWord" }, { "begin", "beginsWith" }, { "end", "endsWith" }, { "dateTo", "dateLE" }, { "dateFrom", "dateGE" }, { "contains", "hasAsWord" }, { "starts", "beginsWith" }, { "ends", "endsWith" }, { "before", "dateLE" }, { "after", "dateGE" }, { "greaterequal", "numberGE" }, { "lessequal", "numberLE" } };
/*    */ 
/*    */   public static String findOperatorFromAlias(String alias, String[][] additionalMap)
/*    */   {
/* 47 */     if ((alias == null) || (alias.length() == 0))
/*    */     {
/* 49 */       return alias;
/*    */     }
/* 51 */     int size = 0;
/* 52 */     if (additionalMap != null)
/*    */     {
/* 54 */       size = additionalMap.length;
/*    */     }
/* 56 */     for (int i = 0; i < size; ++i)
/*    */     {
/* 58 */       if (alias.equalsIgnoreCase(additionalMap[i][0]))
/*    */       {
/* 60 */         return additionalMap[i][1];
/*    */       }
/*    */     }
/* 63 */     for (int i = 0; i < ALIASMAP.length; ++i)
/*    */     {
/* 65 */       if (alias.equalsIgnoreCase(ALIASMAP[i][0]))
/*    */       {
/* 67 */         return ALIASMAP[i][1];
/*    */       }
/*    */     }
/* 70 */     return alias;
/*    */   }
/*    */ 
/*    */   public static String findOperatorFromAlias(String alias)
/*    */   {
/* 75 */     return findOperatorFromAlias(alias, (String[][])null);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 80 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.QueryOperatorUtils
 * JD-Core Version:    0.5.4
 */