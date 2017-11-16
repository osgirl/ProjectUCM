/*    */ package intradoc.autosuggest.utils;
/*    */ 
/*    */ import intradoc.autosuggest.records.OccurrenceInfo;
/*    */ import java.util.Comparator;
/*    */ 
/*    */ public class OccurrenceComparator
/*    */   implements Comparator
/*    */ {
/*    */   public int compare(OccurrenceInfo info1, OccurrenceInfo info2)
/*    */   {
/* 33 */     if (info1.m_position < info2.m_position)
/*    */     {
/* 35 */       return -1;
/*    */     }
/* 37 */     if (info1.m_position > info2.m_position)
/*    */     {
/* 39 */       return 1;
/*    */     }
/* 41 */     if (info1.m_frequency > info2.m_frequency)
/*    */     {
/* 43 */       return -1;
/*    */     }
/* 45 */     if (info1.m_frequency < info2.m_frequency)
/*    */     {
/* 47 */       return 1;
/*    */     }
/* 49 */     return 0;
/*    */   }
/*    */ 
/*    */   public int compare(Object object1, Object object2)
/*    */   {
/* 54 */     return compare((OccurrenceInfo)object1, (OccurrenceInfo)object1);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 58 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98407 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.OccurrenceComparator
 * JD-Core Version:    0.5.4
 */