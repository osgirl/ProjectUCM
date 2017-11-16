/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.data.FieldInfo;
/*     */ 
/*     */ public class SearchListItemComparator
/*     */   implements IdcComparator
/*     */ {
/*     */   public static final int SORT_ASCENDING = 0;
/*     */   public static final int SORT_DESCENDING = 1;
/*     */   public boolean m_useLong;
/*     */   public FieldInfo m_searchInfo;
/*     */   public CacheDataDesign m_dataDesign;
/*     */   public boolean m_isDescending;
/*     */   public ExecutionContext m_cxt;
/*     */ 
/*     */   public SearchListItemComparator(CacheDataDesign dataDesign, FieldInfo searchInfo, int flags, ExecutionContext cxt)
/*     */   {
/*  67 */     this.m_useLong = ((searchInfo.m_type == 5) || (searchInfo.m_type == 3));
/*  68 */     this.m_dataDesign = dataDesign;
/*  69 */     this.m_searchInfo = searchInfo;
/*  70 */     this.m_isDescending = ((flags & 0x1) != 0);
/*  71 */     this.m_cxt = cxt;
/*     */   }
/*     */ 
/*     */   public int compare(Object obj1, Object obj2)
/*     */   {
/*  83 */     int cmp = 0;
/*  84 */     if (this.m_useLong)
/*     */     {
/*  86 */       long l1 = ((SearchListItem)obj1).m_sortValueParsed;
/*  87 */       long l2 = ((SearchListItem)obj2).m_sortValueParsed;
/*  88 */       if (l1 > l2)
/*     */       {
/*  90 */         cmp = 1;
/*     */       }
/*  92 */       else if (l1 < l2)
/*     */       {
/*  94 */         cmp = -1;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*  99 */       String s1 = ((SearchListItem)obj1).m_sortValuePrepared;
/* 100 */       String s2 = ((SearchListItem)obj2).m_sortValuePrepared;
/* 101 */       cmp = SearchCacheUtils.comparePreparedStrings(this.m_dataDesign, this.m_searchInfo, s1, s2, this.m_cxt);
/*     */     }
/* 103 */     if (this.m_isDescending)
/*     */     {
/* 105 */       cmp = -cmp;
/*     */     }
/* 107 */     return cmp;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 112 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchListItemComparator
 * JD-Core Version:    0.5.4
 */