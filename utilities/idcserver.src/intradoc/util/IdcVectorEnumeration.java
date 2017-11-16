/*      */ package intradoc.util;
/*      */ 
/*      */ import java.util.Enumeration;
/*      */ 
/*      */ class IdcVectorEnumeration<E>
/*      */   implements Enumeration<E>
/*      */ {
/*      */   protected IdcVector m_vector;
/*      */   protected int m_index;
/*      */ 
/*      */   public IdcVectorEnumeration(IdcVector v)
/*      */   {
/* 1045 */     this.m_index = 0;
/* 1046 */     this.m_vector = v;
/*      */   }
/*      */ 
/*      */   public boolean hasMoreElements()
/*      */   {
/* 1051 */     return this.m_index < this.m_vector.m_length;
/*      */   }
/*      */ 
/*      */   public E nextElement()
/*      */   {
/* 1056 */     return this.m_vector.m_array[(this.m_index++)];
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1061 */     return "releaseInfo=dev,releaseRevision=$Rev: 75945 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcVectorEnumeration
 * JD-Core Version:    0.5.4
 */