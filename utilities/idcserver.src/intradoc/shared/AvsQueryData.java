/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ 
/*     */ public class AvsQueryData extends ClausesData
/*     */ {
/*  30 */   protected static final String[][] AVS_OPERATOR_CODES = { { "matches", "apQueryFieldMatches" }, { "substring", "apQueryFieldSubstring" }, { "begin", "apQueryFieldBegins" }, { "dateTo", "apAvsQueryFieldDateTo" }, { "dateFrom", "apAvsQueryFieldDateFrom" }, { "numberLE", "apQueryFieldLessOrEqual" }, { "numberGE", "apQueryFieldGreaterOrEqual" } };
/*     */ 
/*  42 */   protected static final String[][] AVS_OP_MAP = { { "substring", "" }, { "matches", "" }, { "begin", "" }, { "dateTo", "" }, { "dateFrom", "" }, { "numberLE", "" }, { "numberGE", "" } };
/*     */ 
/*  52 */   public static final short[] AVS_COMPARISON_INDEX_STARTS = { 0, 3, -1, 5 };
/*     */ 
/*     */   public AvsQueryData()
/*     */   {
/*  57 */     this.m_operatorCodes = AVS_OPERATOR_CODES;
/*  58 */     this.m_operatorMap = AVS_OP_MAP;
/*  59 */     this.m_fieldComparisonIndexStart = AVS_COMPARISON_INDEX_STARTS;
/*  60 */     this.m_conjunction = " &\n";
/*     */   }
/*     */ 
/*     */   public void appendClause(String field, IdcStringBuilder query, String op, String value)
/*     */     throws ServiceException
/*     */   {
/*  68 */     if ((op.equalsIgnoreCase("substring")) || (op.equalsIgnoreCase("matches")) || (op.equalsIgnoreCase("begin")))
/*     */     {
/*  70 */       query.append(field);
/*  71 */       query.append(":{");
/*     */ 
/*  73 */       if (op.equalsIgnoreCase("substring"))
/*     */       {
/*  75 */         query.append("**");
/*     */       }
/*     */ 
/*  78 */       if ((value != null) && (value.length() > 0))
/*     */       {
/*  80 */         query.append(value);
/*     */       }
/*     */ 
/*  83 */       if ((op.equalsIgnoreCase("substring")) || (op.equalsIgnoreCase("begin")))
/*     */       {
/*  85 */         query.append("**");
/*     */       }
/*  87 */       query.append("}");
/*     */     }
/*     */     else
/*     */     {
/*  91 */       query.append("[");
/*  92 */       query.append(field);
/*  93 */       query.append(":");
/*  94 */       if ((op.equalsIgnoreCase("dateTo")) || (op.equalsIgnoreCase("numberLE")))
/*     */       {
/*  96 */         query.append("-");
/*     */       }
/*     */ 
/*  99 */       if ((value != null) && (value.length() > 0))
/*     */       {
/* 101 */         query.append(value);
/*     */       }
/* 103 */       if ((op.equalsIgnoreCase("dateFrom")) || (op.equalsIgnoreCase("numberGE")))
/*     */       {
/* 105 */         query.append("-");
/*     */       }
/*     */ 
/* 108 */       query.append("]");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 114 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.AvsQueryData
 * JD-Core Version:    0.5.4
 */