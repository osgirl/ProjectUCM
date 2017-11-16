/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.List;
/*     */ 
/*     */ public class DynamicDataDerivedColumn
/*     */   implements IdcDebugOutput
/*     */ {
/*     */   public String m_name;
/*     */   public List<String> m_sourceColumns;
/*     */   public String m_separator;
/*     */   public boolean m_computedSourceColumnIndices;
/*     */   public int[] m_sourceColumnToIndexMap;
/*     */ 
/*     */   public void setOption(String optionName, String optionValue)
/*     */   {
/*  59 */     if (!optionName.equals("derivedColumns"))
/*     */       return;
/*  61 */     List keyAndColumns = StringUtils.makeListFromSequence(optionValue, ':', '*', 32);
/*     */ 
/*  63 */     if (keyAndColumns.size() > 0)
/*     */     {
/*  65 */       this.m_name = ((String)keyAndColumns.get(0));
/*     */     }
/*  67 */     if (keyAndColumns.size() <= 1)
/*     */       return;
/*  69 */     String colsStr = (String)keyAndColumns.get(1);
/*  70 */     this.m_sourceColumns = StringUtils.makeListFromSequence(colsStr, '+', '~', 32);
/*     */   }
/*     */ 
/*     */   public void mergeIfNewIsNotNull(DynamicDataDerivedColumn newOptions, int flags)
/*     */   {
/*  81 */     this.m_sourceColumns = DynamicDataUtils.mergeObjectListsNoDuplicates(this.m_sourceColumns, newOptions.m_sourceColumns, flags);
/*     */ 
/*  83 */     if (newOptions.m_separator == null)
/*     */       return;
/*  85 */     this.m_separator = newOptions.m_separator;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/*  92 */     IdcStringBuilder builder = new IdcStringBuilder();
/*  93 */     appendDebugFormat(builder);
/*  94 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/*  99 */     StringUtils.appendForDebug(appendable, this.m_name, 0);
/* 100 */     appendable.append(" (");
/* 101 */     StringUtils.appendForDebug(appendable, this.m_sourceColumns, 0);
/* 102 */     appendable.append(" )");
/*     */   }
/*     */ 
/*     */   public DynamicDataDerivedColumn shallowClone()
/*     */   {
/* 107 */     DynamicDataDerivedColumn col = new DynamicDataDerivedColumn();
/* 108 */     col.m_name = this.m_name;
/* 109 */     col.m_sourceColumns = this.m_sourceColumns;
/* 110 */     col.m_separator = this.m_separator;
/* 111 */     return col;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 116 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 77588 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicDataDerivedColumn
 * JD-Core Version:    0.5.4
 */