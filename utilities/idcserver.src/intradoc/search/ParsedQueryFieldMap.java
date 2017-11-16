/*    */ package intradoc.search;
/*    */ 
/*    */ public class ParsedQueryFieldMap
/*    */ {
/*    */   public int[] m_map;
/*    */   public CacheDataDesign m_cacheDataDesign;
/*    */ 
/*    */   public ParsedQueryFieldMap(int nFieldNames, CacheDataDesign cacheDataDesign)
/*    */   {
/* 42 */     this.m_map = new int[nFieldNames];
/* 43 */     this.m_cacheDataDesign = cacheDataDesign;
/* 44 */     for (int i = 0; i < this.m_map.length; ++i)
/*    */     {
/* 46 */       this.m_map[i] = -1;
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 53 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.ParsedQueryFieldMap
 * JD-Core Version:    0.5.4
 */