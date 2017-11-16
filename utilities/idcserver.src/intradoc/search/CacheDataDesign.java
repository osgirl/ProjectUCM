/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.shared.CommonSearchEngineConfig;
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ 
/*     */ public class CacheDataDesign
/*     */ {
/*     */   public String m_primaryKey;
/*     */   int m_primaryKeyIndex;
/*     */   public String m_specificRevKey;
/*     */   int m_specificRevKeyIndex;
/*     */   public String m_titleRevKey;
/*     */   public int m_titleRevKeyIndex;
/*     */   public CommonSearchEngineConfig m_searchEngineConfig;
/*     */   public FieldInfo[] m_perQueryFields;
/*     */   public int m_numNavigationFields;
/*     */   public boolean m_allowClearingOfOriginalPerQueryMetadata;
/*     */   public DataResultSet m_templateResultSet;
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/*  96 */     appendable.append("primaryKey=").append(this.m_primaryKey);
/*  97 */     appendable.append(" primaryKeyIndex=").append("" + this.m_primaryKeyIndex);
/*  98 */     appendable.append(" specificRevKey=").append(this.m_specificRevKey);
/*  99 */     appendable.append(" specificRevKeyIndex=").append("" + this.m_specificRevKeyIndex);
/* 100 */     appendable.append(" titleRevKey=").append(this.m_titleRevKey);
/* 101 */     appendable.append(" titleRevKeyIndex=").append("" + this.m_titleRevKeyIndex);
/* 102 */     if (this.m_templateResultSet == null)
/*     */       return;
/* 104 */     appendable.append("\n");
/* 105 */     String drSetStr = this.m_templateResultSet.toString();
/* 106 */     appendable.append(drSetStr);
/*     */   }
/*     */ 
/*     */   public boolean equals(CacheDataDesign dataDesign)
/*     */   {
/* 112 */     return this == dataDesign;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 122 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 123 */     appendDebugFormat(builder);
/* 124 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 129 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74354 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.CacheDataDesign
 * JD-Core Version:    0.5.4
 */