/*      */ package intradoc.util;
/*      */ 
/*      */ import java.util.ConcurrentModificationException;
/*      */ import java.util.ListIterator;
/*      */ 
/*      */ class IdcVectorListIterator<E>
/*      */   implements ListIterator<E>
/*      */ {
/*      */   protected IdcVector m_vector;
/*  903 */   protected int m_index = 0;
/*  904 */   protected int m_lastIndex = -1;
/*  905 */   protected int m_generation = 0;
/*      */ 
/*      */   public IdcVectorListIterator(IdcVector v)
/*      */   {
/*  909 */     this.m_vector = v;
/*  910 */     this.m_generation = v.m_generation;
/*      */   }
/*      */ 
/*      */   public IdcVectorListIterator(IdcVector v, int index)
/*      */   {
/*  915 */     this.m_vector = v;
/*  916 */     this.m_index = index;
/*  917 */     this.m_generation = v.m_generation;
/*      */   }
/*      */ 
/*      */   public void add(E obj)
/*      */   {
/*  923 */     if (this.m_generation != this.m_vector.m_generation)
/*      */     {
/*  925 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*      */     }
/*  927 */     this.m_generation += 1;
/*  928 */     this.m_vector.add(this.m_index, obj);
/*      */   }
/*      */ 
/*      */   public boolean hasNext()
/*      */   {
/*  933 */     assert (this.m_vector.assertState());
/*  934 */     if (this.m_generation != this.m_vector.m_generation)
/*      */     {
/*  936 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*      */     }
/*  938 */     return this.m_index < this.m_vector.m_length;
/*      */   }
/*      */ 
/*      */   public boolean hasPrevious()
/*      */   {
/*  943 */     assert (this.m_vector.assertState());
/*  944 */     return this.m_index > 0;
/*      */   }
/*      */ 
/*      */   public E next()
/*      */   {
/*  949 */     assert (this.m_vector.assertState());
/*  950 */     if (this.m_generation != this.m_vector.m_generation)
/*      */     {
/*  952 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*      */     }
/*  954 */     this.m_lastIndex = this.m_index;
/*  955 */     return this.m_vector.m_array[(this.m_index++)];
/*      */   }
/*      */ 
/*      */   public int nextIndex()
/*      */   {
/*  960 */     if (this.m_generation != this.m_vector.m_generation)
/*      */     {
/*  962 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*      */     }
/*  964 */     return this.m_index;
/*      */   }
/*      */ 
/*      */   public E previous()
/*      */   {
/*  969 */     assert (this.m_vector.assertState());
/*  970 */     if (this.m_generation != this.m_vector.m_generation)
/*      */     {
/*  972 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*      */     }
/*  974 */     this.m_lastIndex = (--this.m_index);
/*  975 */     return this.m_vector.m_array[this.m_index];
/*      */   }
/*      */ 
/*      */   public int previousIndex()
/*      */   {
/*  980 */     if (this.m_generation != this.m_vector.m_generation)
/*      */     {
/*  982 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*      */     }
/*  984 */     return this.m_index - 1;
/*      */   }
/*      */ 
/*      */   public void remove()
/*      */   {
/*  989 */     if (this.m_generation != this.m_vector.m_generation)
/*      */     {
/*  991 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*      */     }
/*  993 */     if (this.m_index == 0)
/*      */     {
/*  995 */       throw new IllegalStateException("remove() called prior to calling next().");
/*      */     }
/*  997 */     int index = --this.m_index;
/*      */ 
/* 1000 */     int count = this.m_vector.m_length - index - 1;
/*      */     int i;
/* 1001 */     if (count < 16)
/*      */     {
/* 1003 */       System.arraycopy(this.m_vector.m_array, index + 1, this.m_vector.m_array, index, count);
/*      */     }
/*      */     else
/*      */     {
/* 1007 */       int stop = index + count;
/* 1008 */       for (i = index; i < stop; )
/*      */       {
/* 1010 */         this.m_vector.m_array[i] = this.m_vector.m_array[(++i)];
/*      */       }
/*      */     }
/* 1013 */     this.m_vector.m_length -= 1;
/* 1014 */     this.m_vector.m_array[this.m_vector.m_length] = null;
/* 1015 */     assert (this.m_vector.assertState());
/* 1016 */     this.m_vector.m_generation += 1;
/* 1017 */     this.m_generation += 1;
/*      */   }
/*      */ 
/*      */   public void set(E obj)
/*      */   {
/* 1023 */     if (this.m_generation != this.m_vector.m_generation)
/*      */     {
/* 1025 */       throw new ConcurrentModificationException("Concurrent modification detected");
/*      */     }
/* 1027 */     this.m_generation += 1;
/* 1028 */     this.m_vector.m_generation += 1;
/* 1029 */     this.m_vector.m_array[this.m_lastIndex] = obj;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1034 */     return "releaseInfo=dev,releaseRevision=$Rev: 75945 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcVectorListIterator
 * JD-Core Version:    0.5.4
 */