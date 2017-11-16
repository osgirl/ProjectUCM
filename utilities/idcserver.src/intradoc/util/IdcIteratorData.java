/*    */ package intradoc.util;
/*    */ 
/*    */ import java.util.Iterator;
/*    */ 
/*    */ public class IdcIteratorData<E>
/*    */   implements Iterator<E>
/*    */ {
/*    */   public IdcIterable<E> m_targetObject;
/*    */   public Object m_pointerObject;
/*    */   public int m_pointerOffset;
/*    */   public boolean m_removeSupported;
/*    */ 
/*    */   public IdcIteratorData(IdcIterable<E> targetObject, Object pointerObject)
/*    */   {
/* 32 */     this.m_targetObject = targetObject;
/* 33 */     this.m_pointerObject = pointerObject;
/* 34 */     if (pointerObject != null)
/*    */       return;
/* 36 */     this.m_pointerOffset = 0;
/*    */   }
/*    */ 
/*    */   public boolean hasNext()
/*    */   {
/* 42 */     return this.m_targetObject.hasNext(this);
/*    */   }
/*    */ 
/*    */   public E next()
/*    */   {
/* 47 */     return this.m_targetObject.next(this);
/*    */   }
/*    */ 
/*    */   public void remove() throws UnsupportedOperationException, IllegalStateException
/*    */   {
/* 52 */     this.m_targetObject.remove(this);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 57 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73841 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcIteratorData
 * JD-Core Version:    0.5.4
 */