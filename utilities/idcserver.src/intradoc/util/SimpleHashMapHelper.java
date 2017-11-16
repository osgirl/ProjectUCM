/*    */ package intradoc.util;
/*    */ 
/*    */ public class SimpleHashMapHelper<K, V>
/*    */   implements HashMapHelper<K, V>
/*    */ {
/*    */   public int hashCode(Object obj)
/*    */   {
/* 24 */     return obj.hashCode();
/*    */   }
/*    */ 
/*    */   public boolean equals(Object o1, Object o2)
/*    */   {
/* 29 */     return o1.equals(o2);
/*    */   }
/*    */ 
/*    */   public K getKey(Object obj)
/*    */   {
/* 34 */     return obj;
/*    */   }
/*    */ 
/*    */   public V getVal(Object obj)
/*    */   {
/* 39 */     return obj;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 44 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.SimpleHashMapHelper
 * JD-Core Version:    0.5.4
 */