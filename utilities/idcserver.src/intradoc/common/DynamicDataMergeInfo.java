/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class DynamicDataMergeInfo
/*     */   implements IdcDebugOutput
/*     */ {
/*     */   public String m_varName;
/*     */   public boolean m_useDynamicData;
/*     */   public String[] m_tempFieldList;
/*     */   public String[][] m_tempRows;
/*     */   public boolean[] m_mutatedRows;
/*     */   public SortOptions m_sortOptions;
/*     */   public List<DynamicHtml> m_dynamicHtmlToMerge;
/*     */   public List<DynamicHtml> m_otherDynamicHtmlToMerge;
/*     */   public boolean m_mergePriorDataDone;
/*     */   public boolean m_mergeCopiedToData;
/*     */   public boolean m_mergeOtherDataDone;
/*     */   public List<String> m_indexedColumns;
/*     */   public List<String> m_includeColumns;
/*     */   public List<String[]> m_defaultValues;
/*     */   public List<DynamicDataDerivedColumn> m_derivedColumns;
/*     */   public DynamicDataGrammar m_grammarData;
/*     */   public List<String> m_mergeOtherData;
/*     */   public String m_countColumn;
/*     */   public Set<String> m_collapseMultiValueColumns;
/*     */   public boolean m_mergeAppendFormatSet;
/*     */   public boolean m_usingDefaultMergeAppendFormat;
/*     */   public char m_mergeAppendSep;
/*     */   public char m_mergeAppendEq;
/*     */   public boolean m_tableMergingDone;
/*     */   public Table m_activeTable;
/*     */ 
/*     */   public DynamicDataMergeInfo()
/*     */   {
/*  33 */     this.m_useDynamicData = false;
/*     */ 
/* 129 */     this.m_mergeAppendFormatSet = false;
/* 130 */     this.m_usingDefaultMergeAppendFormat = true;
/* 131 */     this.m_mergeAppendSep = ':';
/* 132 */     this.m_mergeAppendEq = '=';
/*     */   }
/*     */ 
/*     */   public void clearDynamicData()
/*     */   {
/* 152 */     this.m_activeTable = null;
/* 153 */     this.m_useDynamicData = false;
/* 154 */     this.m_tempFieldList = null;
/* 155 */     this.m_tempRows = ((String[][])null);
/* 156 */     this.m_mutatedRows = null;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 162 */     IdcStringBuilder strBuilder = new IdcStringBuilder();
/* 163 */     appendDebugFormat(strBuilder);
/* 164 */     return strBuilder.toString();
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 169 */     appendable.append(this.m_varName);
/* 170 */     appendable.append("(useDynamicData=" + this.m_useDynamicData);
/* 171 */     appendable.append(",tableMergingDone=" + this.m_tableMergingDone);
/* 172 */     appendable.append(")");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 178 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 77560 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicDataMergeInfo
 * JD-Core Version:    0.5.4
 */