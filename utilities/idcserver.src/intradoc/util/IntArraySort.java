/*     */ package intradoc.util;
/*     */ 
/*     */ public class IntArraySort
/*     */ {
/*     */   public int[] m_data;
/*     */   public int m_objSize;
/*     */   public int m_keyOffset;
/*     */ 
/*     */   public IntArraySort()
/*     */   {
/*     */   }
/*     */ 
/*     */   public IntArraySort(int[] data, int objSize, int keyOffset)
/*     */   {
/*  33 */     init(data, objSize, keyOffset);
/*     */   }
/*     */ 
/*     */   public void init(int[] data, int objSize, int keyOffset)
/*     */   {
/*  38 */     this.m_data = data;
/*  39 */     this.m_objSize = objSize;
/*  40 */     this.m_keyOffset = keyOffset;
/*     */   }
/*     */ 
/*     */   public void swap(int elem1, int elem2)
/*     */   {
/*  48 */     for (int k = 0; k < this.m_objSize; ++k)
/*     */     {
/*  50 */       int scratch = this.m_data[(this.m_objSize * elem1 + k)];
/*  51 */       this.m_data[(this.m_objSize * elem1 + k)] = this.m_data[(this.m_objSize * elem2 + k)];
/*  52 */       this.m_data[(this.m_objSize * elem2 + k)] = scratch;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void sort(int[] data, int objSize, int keyOffset)
/*     */   {
/*  58 */     this.m_data = data;
/*  59 */     this.m_objSize = objSize;
/*  60 */     this.m_keyOffset = keyOffset;
/*  61 */     sort(0, this.m_data.length / this.m_objSize - 1);
/*     */   }
/*     */ 
/*     */   public void sort()
/*     */   {
/*  66 */     sort(0, this.m_data.length / this.m_objSize - 1);
/*     */   }
/*     */ 
/*     */   public void sort(int startElement, int endElement)
/*     */   {
/*  71 */     if (endElement - startElement < 5)
/*     */     {
/*  74 */       for (int i = startElement + 1; i <= endElement; ++i)
/*     */       {
/*  76 */         int v1 = this.m_data[(this.m_objSize * i + this.m_keyOffset)];
/*  77 */         for (int j = i - 1; j >= startElement; --j)
/*     */         {
/*  79 */           int v2 = this.m_data[(this.m_objSize * j + this.m_keyOffset)];
/*  80 */           if (v2 <= v1) {
/*     */             break;
/*     */           }
/*     */ 
/*  84 */           swap(j + 1, j);
/*     */         }
/*     */       }
/*  87 */       return;
/*     */     }
/*     */ 
/*  90 */     int midElement = (endElement - startElement) / 2 + startElement + 1;
/*  91 */     int l = this.m_data[(this.m_objSize * startElement + this.m_keyOffset)];
/*  92 */     int m = this.m_data[(this.m_objSize * midElement + this.m_keyOffset)];
/*  93 */     int h = this.m_data[(this.m_objSize * endElement + this.m_keyOffset)];
/*     */ 
/*  95 */     if (m < l)
/*     */     {
/*  97 */       swap(startElement, midElement);
/*  98 */       l = this.m_data[(this.m_objSize * startElement + this.m_keyOffset)];
/*  99 */       m = this.m_data[(this.m_objSize * midElement + this.m_keyOffset)];
/*     */     }
/* 101 */     if (h < m)
/*     */     {
/* 103 */       swap(midElement, endElement);
/* 104 */       m = this.m_data[(this.m_objSize * midElement + this.m_keyOffset)];
/* 105 */       h = this.m_data[(this.m_objSize * endElement + this.m_keyOffset)];
/*     */ 
/* 107 */       if (m < l)
/*     */       {
/* 109 */         swap(startElement, midElement);
/* 110 */         l = this.m_data[(this.m_objSize * startElement + this.m_keyOffset)];
/* 111 */         m = this.m_data[(this.m_objSize * midElement + this.m_keyOffset)];
/*     */       }
/*     */     }
/*     */ 
/* 115 */     int pivot = this.m_data[(this.m_objSize * midElement + this.m_keyOffset)];
/* 116 */     swap(midElement, endElement - 1);
/*     */ 
/* 119 */     int i = startElement;
/* 120 */     int j = endElement - 1;
/* 121 */     while (i < j)
/*     */     {
/* 123 */       if (this.m_data[(this.m_objSize * ++i + this.m_keyOffset)] >= pivot);
/* 124 */       while (this.m_data[(this.m_objSize * --j + this.m_keyOffset)] > pivot);
/* 125 */       if (i >= j)
/*     */         break;
/* 127 */       swap(i, j);
/*     */     }
/*     */ 
/* 135 */     swap(i, endElement - 1);
/*     */ 
/* 137 */     if (i < j)
/*     */     {
/* 139 */       if (startElement < i)
/*     */       {
/* 141 */         sort(startElement, i - 1);
/*     */       }
/* 143 */       if (j >= endElement)
/*     */         return;
/* 145 */       sort(i, endElement);
/*     */     }
/*     */     else
/*     */     {
/* 150 */       if (startElement < j)
/*     */       {
/* 152 */         sort(startElement, i - 1);
/*     */       }
/* 154 */       if (i >= endElement)
/*     */         return;
/* 156 */       sort(i, endElement);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 163 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IntArraySort
 * JD-Core Version:    0.5.4
 */