/*    */ package intradoc.util;
/*    */ 
/*    */ public class IdcPool<T>
/*    */ {
/*    */   private IdcVector<T> m_pool;
/*    */ 
/*    */   public IdcPool()
/*    */   {
/* 31 */     this.m_pool = new IdcVector();
/*    */   }
/*    */ 
/*    */   public synchronized T get()
/*    */   {
/* 39 */     if (this.m_pool.m_length > 0)
/*    */     {
/* 41 */       return this.m_pool.remove(0);
/*    */     }
/* 43 */     return null;
/*    */   }
/*    */ 
/*    */   public synchronized void put(T obj)
/*    */   {
/* 51 */     this.m_pool.addElement(obj);
/*    */   }
/*    */ 
/*    */   public synchronized void clear()
/*    */   {
/* 59 */     this.m_pool.removeAllElements();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 65 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71094 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcPool
 * JD-Core Version:    0.5.4
 */