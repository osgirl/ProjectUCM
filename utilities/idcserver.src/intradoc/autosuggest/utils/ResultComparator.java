/*    */ package intradoc.autosuggest.utils;
/*    */ 
/*    */ import java.util.Comparator;
/*    */ 
/*    */ public class ResultComparator
/*    */   implements Comparator<ResultTermInfo>
/*    */ {
/*    */   public int compare(ResultTermInfo info1, ResultTermInfo info2)
/*    */   {
/* 37 */     if (info1.m_score > info2.m_score)
/*    */     {
/* 39 */       return -1;
/*    */     }
/* 41 */     if (info1.m_score < info2.m_score)
/*    */     {
/* 43 */       return 1;
/*    */     }
/*    */ 
/* 46 */     if (info1.m_proximityIndex > info2.m_proximityIndex)
/*    */     {
/* 48 */       return -1;
/*    */     }
/* 50 */     if (info1.m_proximityIndex < info2.m_proximityIndex)
/*    */     {
/* 52 */       return 1;
/*    */     }
/* 54 */     return 0;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 60 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98770 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.ResultComparator
 * JD-Core Version:    0.5.4
 */