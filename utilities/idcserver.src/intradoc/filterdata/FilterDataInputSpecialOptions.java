/*     */ package intradoc.filterdata;
/*     */ 
/*     */ public class FilterDataInputSpecialOptions
/*     */ {
/*     */   public static final String ZERO_WIDTH_WHITESPACE = "&#8203;";
/*  33 */   public boolean m_doWordBreak = false;
/*     */ 
/*  38 */   public int m_maxWordBreak = 120;
/*     */ 
/*  44 */   public String m_lineBreakEntity = " ";
/*     */ 
/*  49 */   public boolean m_escapePotentiallyUnsafeCharacters = false;
/*     */ 
/*  54 */   public char[] m_potentiallyUnsafeCharacters = { '"' };
/*     */   public boolean m_encodeAdditionalUnsafe;
/*     */   public boolean m_encodeLineFeeds;
/*     */   public boolean m_removeTags;
/*     */   public boolean m_processFragments;
/*     */ 
/*     */   public FilterDataInputSpecialOptions shallowClone()
/*     */   {
/*  95 */     FilterDataInputSpecialOptions clone = new FilterDataInputSpecialOptions();
/*  96 */     clone.m_doWordBreak = this.m_doWordBreak;
/*  97 */     clone.m_maxWordBreak = this.m_maxWordBreak;
/*  98 */     clone.m_escapePotentiallyUnsafeCharacters = this.m_escapePotentiallyUnsafeCharacters;
/*  99 */     clone.m_potentiallyUnsafeCharacters = this.m_potentiallyUnsafeCharacters;
/* 100 */     clone.m_encodeAdditionalUnsafe = this.m_encodeAdditionalUnsafe;
/* 101 */     clone.m_removeTags = this.m_removeTags;
/* 102 */     clone.m_encodeLineFeeds = this.m_encodeLineFeeds;
/* 103 */     clone.m_processFragments = this.m_processFragments;
/* 104 */     return clone;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 110 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filterdata.FilterDataInputSpecialOptions
 * JD-Core Version:    0.5.4
 */