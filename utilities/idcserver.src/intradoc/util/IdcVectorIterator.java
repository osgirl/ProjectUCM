/*     */ package intradoc.util;
/*     */ 
/*     */ import java.util.ConcurrentModificationException;
/*     */ import java.util.Iterator;
/*     */ 
/*     */ class IdcVectorIterator<E>
/*     */   implements Iterator<E>
/*     */ {
/*     */   protected IdcVector m_vector;
/* 830 */   protected int m_index = 0;
/* 831 */   protected int m_generation = 0;
/*     */ 
/*     */   public IdcVectorIterator(IdcVector v)
/*     */   {
/* 835 */     this.m_vector = v;
/* 836 */     this.m_generation = v.m_generation;
/*     */   }
/*     */ 
/*     */   public boolean hasNext()
/*     */   {
/* 841 */     assert (this.m_vector.assertState());
/* 842 */     if (this.m_generation != this.m_vector.m_generation)
/*     */     {
/* 844 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*     */     }
/* 846 */     return this.m_index < this.m_vector.m_length;
/*     */   }
/*     */ 
/*     */   public E next()
/*     */   {
/* 851 */     assert (this.m_vector.assertState());
/* 852 */     if (this.m_generation != this.m_vector.m_generation)
/*     */     {
/* 854 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*     */     }
/* 856 */     return this.m_vector.m_array[(this.m_index++)];
/*     */   }
/*     */ 
/*     */   public void remove()
/*     */   {
/* 861 */     assert (this.m_vector.assertState());
/* 862 */     if (this.m_generation != this.m_vector.m_generation)
/*     */     {
/* 864 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*     */     }
/* 866 */     if (this.m_index == 0)
/*     */     {
/* 868 */       throw new IllegalStateException("remove() called prior to calling next().");
/*     */     }
/* 870 */     int index = --this.m_index;
/*     */ 
/* 873 */     int count = this.m_vector.m_length - index - 1;
/*     */     int i;
/* 874 */     if (count < 16)
/*     */     {
/* 876 */       System.arraycopy(this.m_vector.m_array, index + 1, this.m_vector.m_array, index, count);
/*     */     }
/*     */     else
/*     */     {
/* 880 */       int stop = index + count;
/* 881 */       for (i = index; i < stop; )
/*     */       {
/* 883 */         this.m_vector.m_array[i] = this.m_vector.m_array[(++i)];
/*     */       }
/*     */     }
/* 886 */     this.m_vector.m_length -= 1;
/* 887 */     this.m_vector.m_array[this.m_vector.m_length] = null;
/* 888 */     assert (this.m_vector.assertState());
/* 889 */     this.m_generation += 1;
/* 890 */     this.m_vector.m_generation += 1;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 896 */     return "releaseInfo=dev,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcVectorIterator
 * JD-Core Version:    0.5.4
 */