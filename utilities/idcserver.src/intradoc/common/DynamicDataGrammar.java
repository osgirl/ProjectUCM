/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.List;
/*     */ 
/*     */ public class DynamicDataGrammar
/*     */   implements IdcDebugOutput
/*     */ {
/*  28 */   public static int F_ALWAYS_APPEND = 1;
/*     */   public List<String> m_columns;
/*     */   public int[] m_columnMap;
/*     */   public GrammarElement[][] m_columnElements;
/*     */   public boolean m_replacePrepareElement;
/*     */   public String m_unparsedPrepareElement;
/*     */   public GrammarElement m_prepareElement;
/*     */ 
/*     */   public void setOption(String optionName, String optionValue)
/*     */   {
/*  68 */     if (optionName.equals("idocClauseColumns"))
/*     */     {
/*  70 */       this.m_columns = StringUtils.makeListFromSequenceSimple(optionValue);
/*     */     }
/*  72 */     else if (optionName.equals("idocReplaceClausePrepare"))
/*     */     {
/*  74 */       this.m_replacePrepareElement = (!StringUtils.convertToBool(optionValue, true));
/*     */     } else {
/*  76 */       if (!optionName.equals("idocClausePrepare"))
/*     */         return;
/*  78 */       this.m_unparsedPrepareElement = optionValue;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void mergeIfNewIsNotNull(DynamicDataGrammar newOptions, int flags)
/*     */   {
/*  87 */     boolean alwaysAppend = (flags & F_ALWAYS_APPEND) != 0;
/*  88 */     this.m_columns = DynamicDataUtils.mergeObjectListsNoDuplicates(this.m_columns, newOptions.m_columns, flags);
/*  89 */     if (newOptions.m_unparsedPrepareElement == null)
/*     */       return;
/*  91 */     boolean replace = (!alwaysAppend) && (newOptions.m_replacePrepareElement) && (!StringUtils.isConfigAllWhiteSpace(this.m_unparsedPrepareElement));
/*     */ 
/*  93 */     if (replace)
/*     */     {
/*  95 */       this.m_unparsedPrepareElement = newOptions.m_unparsedPrepareElement;
/*     */     }
/*     */     else
/*     */     {
/*  99 */       this.m_unparsedPrepareElement += newOptions.m_unparsedPrepareElement;
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 107 */     IdcStringBuilder debugBuf = new IdcStringBuilder();
/* 108 */     appendDebugFormat(debugBuf);
/* 109 */     return debugBuf.toString();
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 114 */     StringUtils.appendForDebug(appendable, this.m_columns, 0);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 119 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicDataGrammar
 * JD-Core Version:    0.5.4
 */