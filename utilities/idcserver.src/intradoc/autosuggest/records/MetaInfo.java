/*     */ package intradoc.autosuggest.records;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestConstants;
/*     */ import intradoc.autosuggest.AutoSuggestContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import java.io.Serializable;
/*     */ 
/*     */ public class MetaInfo
/*     */   implements Serializable
/*     */ {
/*     */   private static final long serialVersionUID = 6106746177588540118L;
/*     */   public transient AutoSuggestContext m_context;
/*     */   public String m_indexedDate;
/*     */   public String m_activeIndex;
/*     */ 
/*     */   public MetaInfo(AutoSuggestContext context)
/*     */   {
/*  41 */     this.m_context = context;
/*  42 */     this.m_activeIndex = AutoSuggestConstants.AUTO_SUGGEST_PRIMARY_INDEX;
/*     */   }
/*     */ 
/*     */   public void init(String indexedDate, String activeIndex)
/*     */   {
/*  51 */     this.m_indexedDate = indexedDate;
/*  52 */     this.m_activeIndex = activeIndex;
/*     */   }
/*     */ 
/*     */   public String getIndexedDate()
/*     */   {
/*  59 */     return this.m_indexedDate;
/*     */   }
/*     */ 
/*     */   public void setIndexedDate(String indexedDate) {
/*  63 */     this.m_indexedDate = indexedDate;
/*     */   }
/*     */ 
/*     */   public String getActiveIndex()
/*     */   {
/*  71 */     return this.m_activeIndex;
/*     */   }
/*     */ 
/*     */   public void setActiveIndex(String activeIndex) {
/*  75 */     this.m_activeIndex = activeIndex;
/*     */   }
/*     */ 
/*     */   public void switchActiveIndex()
/*     */   {
/*  82 */     String switchedActiveIndex = getSwitchedActiveIndex();
/*  83 */     this.m_activeIndex = switchedActiveIndex;
/*     */   }
/*     */ 
/*     */   public String getSwitchedActiveIndex()
/*     */   {
/*  91 */     String switchedActiveIndex = AutoSuggestConstants.AUTO_SUGGEST_PRIMARY_INDEX;
/*  92 */     if (this.m_activeIndex.equalsIgnoreCase(AutoSuggestConstants.AUTO_SUGGEST_PRIMARY_INDEX))
/*     */     {
/*  94 */       switchedActiveIndex = AutoSuggestConstants.AUTO_SUGGEST_SECONDARY_INDEX;
/*     */     }
/*  96 */     else if (this.m_activeIndex.equalsIgnoreCase(AutoSuggestConstants.AUTO_SUGGEST_SECONDARY_INDEX))
/*     */     {
/*  98 */       switchedActiveIndex = AutoSuggestConstants.AUTO_SUGGEST_PRIMARY_INDEX;
/*     */     }
/* 100 */     return switchedActiveIndex;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 105 */     IdcStringBuilder metaInfoBuilder = new IdcStringBuilder();
/* 106 */     metaInfoBuilder.append(" Indexed Date : " + this.m_indexedDate);
/* 107 */     metaInfoBuilder.append(" Active Index : " + this.m_activeIndex);
/* 108 */     return metaInfoBuilder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 113 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98808 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.records.MetaInfo
 * JD-Core Version:    0.5.4
 */