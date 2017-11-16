/*    */ package intradoc.tools.common;
/*    */ 
/*    */ import java.util.Comparator;
/*    */ 
/*    */ public class ReverseComparator<T extends Comparable>
/*    */   implements Comparator<T>
/*    */ {
/*    */   public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98160 $";
/*    */   public Comparator<T> m_comparator;
/*    */ 
/*    */   public ReverseComparator()
/*    */   {
/*    */   }
/*    */ 
/*    */   public ReverseComparator(Comparator<T> comparator)
/*    */   {
/* 35 */     this.m_comparator = comparator;
/*    */   }
/*    */ 
/*    */   public int compare(T o1, T o2)
/*    */   {
/* 40 */     Comparator comparator = this.m_comparator;
/* 41 */     if (comparator == null)
/*    */     {
/* 43 */       return -o1.compareTo(o2);
/*    */     }
/* 45 */     return -comparator.compare(o1, o2);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 51 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98160 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.ReverseComparator
 * JD-Core Version:    0.5.4
 */