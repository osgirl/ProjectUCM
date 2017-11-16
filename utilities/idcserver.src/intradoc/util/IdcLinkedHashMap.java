/*    */ package intradoc.util;
/*    */ 
/*    */ import java.util.LinkedHashMap;
/*    */ import java.util.Map;
/*    */ import java.util.Map.Entry;
/*    */ 
/*    */ public class IdcLinkedHashMap<K, V> extends LinkedHashMap<K, V>
/*    */ {
/*    */   private int m_maxSize;
/*    */ 
/*    */   public IdcLinkedHashMap(int maxSize)
/*    */   {
/* 41 */     super(16, 0.75F, true);
/* 42 */     this.m_maxSize = maxSize;
/*    */   }
/*    */ 
/*    */   public IdcLinkedHashMap(int maxSize, Map<? extends K, ? extends V> m)
/*    */   {
/* 58 */     super(m.size(), 0.75F, true);
/* 59 */     this.m_maxSize = maxSize;
/* 60 */     for (Map.Entry entry : m.entrySet())
/*    */     {
/* 62 */       put(entry.getKey(), entry.getValue());
/*    */     }
/*    */   }
/*    */ 
/*    */   protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
/*    */   {
/* 75 */     return size() > this.m_maxSize;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 82 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102438 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcLinkedHashMap
 * JD-Core Version:    0.5.4
 */