/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Sort
/*     */ {
/*     */   public static final int INSERTION_SORT_THRETHOLD = 8;
/*     */ 
/*     */   public static final void sortVector(Vector v, IdcComparator cmp)
/*     */   {
/*  45 */     Object[] s = v.toArray();
/*  46 */     int nElts = s.length;
/*  47 */     sort(s, 0, nElts - 1, cmp);
/*  48 */     for (int i = 0; i < nElts; ++i)
/*     */     {
/*  50 */       v.set(i, s[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static final void sortList(List list, IdcComparator cmp)
/*     */   {
/*  59 */     Object[] s = list.toArray();
/*  60 */     int nElts = s.length;
/*  61 */     sort(s, 0, nElts - 1, cmp);
/*  62 */     for (int i = 0; i < nElts; ++i)
/*     */     {
/*  64 */       list.set(i, s[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static final void sort(Object[] s, IdcComparator cmp)
/*     */   {
/*  70 */     sort(s, 0, s.length - 1, cmp);
/*     */   }
/*     */ 
/*     */   public static final void sort(Object[] s, int lo, int hi, IdcComparator cmp)
/*     */   {
/*  78 */     int len = hi - lo + 1;
/*  79 */     Object[] t = new Object[len];
/*     */ 
/*  83 */     int i = lo; for (int j = 0; i <= hi; ++j)
/*     */     {
/*  85 */       t[j] = s[i];
/*     */ 
/*  83 */       ++i;
/*     */     }
/*     */ 
/*  88 */     sort(t, 0, len - 1, s, lo, hi, cmp);
/*     */   }
/*     */ 
/*     */   protected static final void sort(Object[] s, int srcLow, int srcHi, Object[] t, int tgtLow, int tgtHi, IdcComparator cmp)
/*     */   {
/*  95 */     int len = tgtHi - tgtLow + 1;
/*  96 */     if (len <= 8)
/*     */     {
/*  99 */       Object tmp = null;
/* 100 */       for (int i = tgtLow; i <= tgtHi; ++i)
/*     */       {
/* 102 */         for (int j = i; (j > tgtLow) && (cmp.compare(t[(j - 1)], t[j]) > 0); --j)
/*     */         {
/* 104 */           tmp = t[j];
/* 105 */           t[j] = t[(j - 1)];
/* 106 */           t[(j - 1)] = tmp;
/*     */         }
/*     */       }
/* 109 */       return;
/*     */     }
/*     */ 
/* 112 */     int mid = tgtHi + tgtLow >>> 1;
/* 113 */     int smid = srcLow + srcHi >>> 1;
/*     */ 
/* 115 */     sort(t, tgtLow, mid - 1, s, srcLow, smid - 1, cmp);
/* 116 */     sort(t, mid, tgtHi, s, smid, srcHi, cmp);
/*     */ 
/* 118 */     int li = srcLow;
/* 119 */     int pi = smid;
/* 120 */     for (int i = tgtLow; i <= tgtHi; ++i)
/*     */     {
/* 122 */       if ((pi > srcHi) || ((li < smid) && (cmp.compare(s[li], s[pi]) <= 0)))
/*     */       {
/* 124 */         t[i] = s[(li++)];
/*     */       }
/*     */       else
/*     */       {
/* 128 */         t[i] = s[(pi++)];
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 135 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.Sort
 * JD-Core Version:    0.5.4
 */