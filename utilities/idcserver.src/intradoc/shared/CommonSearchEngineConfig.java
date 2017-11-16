/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.StringUtils;
/*     */ 
/*     */ public class CommonSearchEngineConfig
/*     */ {
/*     */   public static final int UPDATE_CACHE_NONE = 0;
/*     */   public static final int UPDATE_CACHE_INVALIDATE = 1;
/*     */   public static final int UPDATE_CACHE_REPAIR = 2;
/*  37 */   public static String[] UPDATE_CACHE_LEVEL_STRINGS = { "none", "invalidate", "repair" };
/*     */   public String m_engineName;
/*     */   public String[][] m_escapeCharMap;
/*     */   public String m_sortFieldDefault;
/*     */   public String m_sortOrderDefault;
/*     */   public boolean m_allowCacheDiffUpdates;
/*     */   public int m_cacheDiffUpdateLevel;
/*     */   public String[] m_fullTextFields;
/*     */   public int m_numNavigationFields;
/*     */   public boolean m_invalidateQueryCacheOnOutRangeRow;
/*     */ 
/*     */   public CommonSearchEngineConfig()
/*     */   {
/*  78 */     this.m_numNavigationFields = 0;
/*     */ 
/*  83 */     this.m_invalidateQueryCacheOnOutRangeRow = false;
/*     */   }
/*     */ 
/*     */   public void setDiffUpdateLevel(String updateLevel)
/*     */   {
/*  90 */     for (int i = 0; i < UPDATE_CACHE_LEVEL_STRINGS.length; ++i)
/*     */     {
/*  92 */       if (!UPDATE_CACHE_LEVEL_STRINGS[i].equalsIgnoreCase(updateLevel))
/*     */         continue;
/*  94 */       this.m_cacheDiffUpdateLevel = i;
/*  95 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getDiffUpdateLevelAsString(int updateLevel)
/*     */   {
/* 105 */     if ((updateLevel < 0) || (updateLevel >= UPDATE_CACHE_LEVEL_STRINGS.length))
/*     */     {
/* 107 */       return "undefined";
/*     */     }
/* 109 */     return UPDATE_CACHE_LEVEL_STRINGS[updateLevel];
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 118 */     StringUtils.appendDebugProperty(appendable, "engineName", this.m_engineName, false);
/* 119 */     StringUtils.appendDebugProperty(appendable, "escapeCharMap", this.m_escapeCharMap, true);
/* 120 */     StringUtils.appendDebugProperty(appendable, "sortFieldDefault", this.m_sortFieldDefault, true);
/* 121 */     StringUtils.appendDebugProperty(appendable, "sortOrderDefault", this.m_sortOrderDefault, true);
/* 122 */     StringUtils.appendDebugProperty(appendable, "allowCacheDiffUpdates", "" + this.m_allowCacheDiffUpdates, true);
/*     */ 
/* 124 */     StringUtils.appendDebugProperty(appendable, "cacheDiffUpdateLevel", getDiffUpdateLevelAsString(this.m_cacheDiffUpdateLevel), true);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 135 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 136 */     appendDebugFormat(builder);
/* 137 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 142 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70975 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.CommonSearchEngineConfig
 * JD-Core Version:    0.5.4
 */