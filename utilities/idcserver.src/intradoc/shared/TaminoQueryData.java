/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ 
/*     */ public class TaminoQueryData extends ClausesData
/*     */ {
/*  30 */   protected static final String[][] TAMINO_OPERATOR_CODES = { { "matches", "apQueryFieldMatches" }, { "substring", "apQueryFieldSubstring" }, { "contain", "apQueryFieldContains" }, { "begin", "apQueryFieldBegins" }, { "dateLess", "apQueryFieldDateLess" }, { "dateGreater", "apQueryFieldDateGreater" }, { "numberEquals", "apQueryFieldNumberEquals" }, { "numberLE", "apQueryFieldLessOrEqual" }, { "numberLess", "apQueryFieldLess" }, { "numberGE", "apQueryFieldGreaterOrEqual" }, { "numberGreater", "apQueryFieldGreater" } };
/*     */ 
/*  46 */   protected static final String[][] TAMINO_OP_MAP = { { "matches", "=" }, { "substring", "~=" }, { "contain", "~=" }, { "begin", "~=" }, { "dateLess", "<" }, { "dateGreater", ">" }, { "numberGreater", ">" }, { "numberLess", "<" }, { "numberEquals", "=" }, { "numberGE", ">=" }, { "numberLE", "<=" } };
/*     */ 
/*  62 */   public static final short[] TAMINO_COMPARISON_INDEX_STARTS = { 0, 4, -1, 6 };
/*     */ 
/*     */   public TaminoQueryData()
/*     */   {
/*  67 */     this.m_operatorCodes = TAMINO_OPERATOR_CODES;
/*  68 */     this.m_operatorMap = TAMINO_OP_MAP;
/*  69 */     this.m_fieldComparisonIndexStart = TAMINO_COMPARISON_INDEX_STARTS;
/*  70 */     this.m_conjunction = " and\n";
/*     */   }
/*     */ 
/*     */   public String prepareQueryValue(String dataOp, String value)
/*     */   {
/*  75 */     int length = TAMINO_OP_MAP.length;
/*     */ 
/*  77 */     for (int i = 0; i < length; ++i)
/*     */     {
/*  79 */       if (TAMINO_OP_MAP[i][0].equals(dataOp)) {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  85 */     if (i >= TAMINO_COMPARISON_INDEX_STARTS[3])
/*     */     {
/*  87 */       return value;
/*     */     }
/*     */ 
/*  90 */     int vlen = value.length();
/*  91 */     IdcStringBuilder buf = new IdcStringBuilder(vlen + 2);
/*  92 */     buf.append('\'');
/*  93 */     if (dataOp.equalsIgnoreCase("substring"))
/*     */     {
/*  95 */       buf.append('*');
/*     */     }
/*  97 */     if ((dataOp.equalsIgnoreCase("dateless")) || (dataOp.equalsIgnoreCase("dategreater")))
/*     */     {
/* 100 */       buf.append("{tdate ");
/*     */     }
/* 102 */     for (i = 0; i < vlen; ++i)
/*     */     {
/* 104 */       char ch = value.charAt(i);
/* 105 */       if (ch == '\'')
/*     */       {
/* 107 */         buf.append("\\'");
/*     */       }
/*     */       else
/*     */       {
/* 111 */         buf.append(ch);
/*     */       }
/*     */     }
/* 114 */     if ((dataOp.equalsIgnoreCase("substring")) || (dataOp.equalsIgnoreCase("begin")))
/*     */     {
/* 117 */       buf.append('*');
/*     */     }
/* 119 */     else if ((dataOp.equalsIgnoreCase("dateless")) || (dataOp.equalsIgnoreCase("dategreater")))
/*     */     {
/* 122 */       buf.append('}');
/*     */     }
/* 124 */     buf.append('\'');
/* 125 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public void appendOpAndValue(IdcStringBuilder query, String op, String value)
/*     */     throws ServiceException
/*     */   {
/* 132 */     String dataOp = StringUtils.getPresentationString(this.m_operatorMap, op);
/* 133 */     query.append(dataOp);
/* 134 */     query.append(" ");
/* 135 */     value = prepareQueryValue(op, value);
/* 136 */     query.append(value);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 141 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.TaminoQueryData
 * JD-Core Version:    0.5.4
 */