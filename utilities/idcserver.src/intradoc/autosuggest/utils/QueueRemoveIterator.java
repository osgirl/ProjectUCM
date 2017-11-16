/*    */ package intradoc.autosuggest.utils;
/*    */ 
/*    */ import java.util.Iterator;
/*    */ import java.util.Queue;
/*    */ 
/*    */ public class QueueRemoveIterator<E>
/*    */   implements Iterator<E>
/*    */ {
/*    */   public Queue<E> m_queue;
/*    */ 
/*    */   public QueueRemoveIterator(Queue<E> queue)
/*    */   {
/* 30 */     this.m_queue = queue;
/*    */   }
/*    */ 
/*    */   public boolean hasNext()
/*    */   {
/* 37 */     return !this.m_queue.isEmpty();
/*    */   }
/*    */ 
/*    */   public E next()
/*    */   {
/* 45 */     return this.m_queue.poll();
/*    */   }
/*    */ 
/*    */   public void remove() {
/* 49 */     throw new UnsupportedOperationException();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 53 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98707 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.utils.QueueRemoveIterator
 * JD-Core Version:    0.5.4
 */