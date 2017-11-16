/*     */ package intradoc.common;
/*     */ 
/*     */ public class IdcPosition
/*     */ {
/*  25 */   private int m_field = 0;
/*  26 */   private int m_endIndex = 0;
/*  27 */   private int m_beginIndex = 0;
/*     */ 
/*  30 */   private int m_index = 0;
/*  31 */   private int m_errorIndex = -1;
/*     */ 
/*     */   public IdcPosition(int value, boolean isField)
/*     */   {
/*  43 */     if (isField)
/*     */     {
/*  45 */       this.m_field = value;
/*     */     }
/*     */     else
/*     */     {
/*  49 */       this.m_index = value;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int getField()
/*     */   {
/*  57 */     return this.m_field;
/*     */   }
/*     */ 
/*     */   public int getBeginIndex()
/*     */   {
/*  64 */     return this.m_beginIndex;
/*     */   }
/*     */ 
/*     */   public int getEndIndex()
/*     */   {
/*  72 */     return this.m_endIndex;
/*     */   }
/*     */ 
/*     */   public void setBeginIndex(int bi)
/*     */   {
/*  79 */     this.m_beginIndex = bi;
/*     */   }
/*     */ 
/*     */   public void setEndIndex(int ei)
/*     */   {
/*  86 */     this.m_endIndex = ei;
/*     */   }
/*     */ 
/*     */   public int getIndex()
/*     */   {
/*  95 */     return this.m_index;
/*     */   }
/*     */ 
/*     */   public void setIndex(int index)
/*     */   {
/* 102 */     this.m_index = index;
/*     */   }
/*     */ 
/*     */   public int getErrorIndex()
/*     */   {
/* 112 */     return this.m_errorIndex;
/*     */   }
/*     */ 
/*     */   public void setErrorIndex(int ei)
/*     */   {
/* 122 */     this.m_errorIndex = ei;
/*     */   }
/*     */ 
/*     */   public boolean equals(Object obj)
/*     */   {
/* 131 */     if (obj == null) return false;
/* 132 */     if (!obj instanceof IdcPosition) return false;
/*     */ 
/* 134 */     IdcPosition other = (IdcPosition)obj;
/* 135 */     return (this.m_beginIndex == other.m_beginIndex) && (this.m_endIndex == other.m_endIndex) && (this.m_field == other.m_field) && (this.m_index == other.m_index) && (this.m_errorIndex == other.m_errorIndex);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 148 */     return super.getClass().getName() + "[field=" + this.m_field + ",beginIndex=" + this.m_beginIndex + ",endIndex=" + this.m_endIndex + ",index=" + this.m_index + ",errorIndex=" + this.m_errorIndex + ']';
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 158 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcPosition
 * JD-Core Version:    0.5.4
 */