/*    */ package intradoc.common;
/*    */ 
/*    */ import java.util.Comparator;
/*    */ 
/*    */ public class ComparatorAdapter
/*    */   implements Comparator
/*    */ {
/*    */   public int m_sortStrength;
/*    */ 
/*    */   public ComparatorAdapter()
/*    */   {
/* 24 */     this.m_sortStrength = 2;
/*    */   }
/*    */ 
/*    */   public int compare(Object obj1, Object obj2) {
/* 28 */     int comparison = 0;
/*    */ 
/* 30 */     if (this.m_sortStrength == 1)
/*    */     {
/* 32 */       comparison = ((String)obj1).compareToIgnoreCase((String)obj2);
/*    */     }
/*    */     else
/*    */     {
/* 36 */       comparison = ((String)obj1).compareTo((String)obj2);
/*    */     }
/*    */ 
/* 39 */     return comparison;
/*    */   }
/*    */ 
/*    */   public synchronized void setStrength(int newStrength)
/*    */   {
/* 50 */     this.m_sortStrength = newStrength;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 55 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72401 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ComparatorAdapter
 * JD-Core Version:    0.5.4
 */