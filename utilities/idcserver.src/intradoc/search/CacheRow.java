/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.StringUtils;
/*     */ 
/*     */ public class CacheRow
/*     */ {
/*     */   public String[] m_row;
/*     */   public Object[] m_resultRowCachedConversions;
/*     */   public String[] m_capturedPerQueryValues;
/*     */   public CacheDataDesign m_dataDesign;
/*     */   public long m_generationalCounter;
/*     */ 
/*     */   public CacheRow(String[] row, String[] perQueryValues, CacheDataDesign dataDesign, long generationalCounter)
/*     */   {
/*  65 */     this.m_row = row;
/*  66 */     this.m_capturedPerQueryValues = perQueryValues;
/*  67 */     this.m_dataDesign = dataDesign;
/*  68 */     this.m_generationalCounter = generationalCounter;
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/*  77 */     if ((this.m_row == null) || (this.m_dataDesign == null))
/*     */     {
/*  79 */       appendable.append("null");
/*     */     }
/*     */     else
/*     */     {
/*  83 */       int titleIndex = this.m_dataDesign.m_titleRevKeyIndex;
/*  84 */       int idIndex = this.m_dataDesign.m_specificRevKeyIndex;
/*  85 */       int primaryKeyIndex = this.m_dataDesign.m_primaryKeyIndex;
/*  86 */       if ((primaryKeyIndex >= 0) && (primaryKeyIndex < this.m_row.length))
/*     */       {
/*  88 */         appendable.append(this.m_row[primaryKeyIndex]);
/*     */       }
/*     */       else
/*     */       {
/*  92 */         appendable.append("primaryKeyIndex=");
/*  93 */         appendable.append("" + primaryKeyIndex);
/*     */       }
/*  95 */       boolean isAfterFirst = false;
/*  96 */       if ((idIndex >= 0) && (idIndex < this.m_row.length))
/*     */       {
/*  98 */         if (!isAfterFirst)
/*     */         {
/* 100 */           appendable.append(" (");
/*     */         }
/* 102 */         StringUtils.appendDebugProperty(appendable, "id", this.m_row[idIndex], isAfterFirst);
/* 103 */         isAfterFirst = true;
/*     */       }
/* 105 */       if ((titleIndex >= 0) && (titleIndex < this.m_row.length))
/*     */       {
/* 107 */         if (!isAfterFirst)
/*     */         {
/* 109 */           appendable.append("(");
/*     */         }
/* 111 */         StringUtils.appendDebugProperty(appendable, "title", this.m_row[titleIndex], isAfterFirst);
/* 112 */         isAfterFirst = true;
/*     */       }
/* 114 */       if (!isAfterFirst)
/*     */         return;
/* 116 */       appendable.append(")");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 128 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 129 */     appendDebugFormat(builder);
/* 130 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 136 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79572 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.CacheRow
 * JD-Core Version:    0.5.4
 */