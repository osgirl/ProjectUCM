/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class HeapSort
/*     */ {
/*     */   public static final void sortVector(Vector v, IdcComparator cmp)
/*     */   {
/*  36 */     int nElts = v.size();
/*  37 */     Object[] s = new Object[nElts];
/*  38 */     for (int i = 0; i < nElts; ++i)
/*     */     {
/*  40 */       s[i] = v.elementAt(i);
/*     */     }
/*  42 */     sort(s, cmp);
/*  43 */     for (i = 0; i < nElts; ++i)
/*     */     {
/*  45 */       v.setElementAt(s[i], i);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static final void sort(Object[] array, IdcComparator cmp)
/*     */   {
/*  54 */     sort(array, 0, array.length - 1, cmp);
/*     */   }
/*     */ 
/*     */   public static final void sort(Object[] array, int lo, int hi, IdcComparator cmp)
/*     */   {
/*  63 */     int len = hi - lo + 1;
/*  64 */     if (len <= 0)
/*     */     {
/*  66 */       return;
/*     */     }
/*     */ 
/*  69 */     for (int i = (len - 1) / 2; i >= 0; --i)
/*     */     {
/*  71 */       updateHeap(array, i, len - 1, lo, cmp);
/*     */     }
/*     */ 
/*  74 */     int i = len - 1; for (int j = len + lo - 1; i >= 1; --j)
/*     */     {
/*  77 */       Object tmp = array[lo];
/*  78 */       array[lo] = array[j];
/*  79 */       array[j] = tmp;
/*  80 */       updateHeap(array, 0, i - 1, lo, cmp);
/*     */ 
/*  74 */       --i;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static final void updateHeap(Object[] array, int topIndex, int len, int off, IdcComparator cmp)
/*     */   {
/*  87 */     int curIndex = (topIndex << 1) + off;
/*  88 */     int topIndexWithOff = topIndex + off;
/*  89 */     len += off;
/*  90 */     while (curIndex <= len)
/*     */     {
/*     */       int maxIndex;
/*     */       int maxIndex;
/*  92 */       if ((curIndex == len) || (cmp.compare(array[curIndex], array[(curIndex + 1)]) > 0))
/*     */       {
/*  94 */         maxIndex = curIndex;
/*     */       }
/*     */       else
/*     */       {
/*  98 */         maxIndex = curIndex + 1;
/*     */       }
/*     */ 
/* 101 */       if (cmp.compare(array[topIndexWithOff], array[maxIndex]) >= 0)
/*     */         return;
/* 103 */       Object temp = array[topIndexWithOff];
/* 104 */       array[topIndexWithOff] = array[maxIndex];
/* 105 */       array[maxIndex] = temp;
/* 106 */       topIndexWithOff = maxIndex;
/* 107 */       topIndex = maxIndex - off;
/*     */ 
/* 113 */       curIndex = (topIndex << 1) + off;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 119 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.HeapSort
 * JD-Core Version:    0.5.4
 */