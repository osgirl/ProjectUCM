/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ 
/*     */ public class SqlQueryData extends ClausesData
/*     */ {
/*  35 */   protected static final String[][] SQL_OPERATOR_CODES = { { "sqlEq", "apQueryFieldIs" }, { "sqlNeq", "apQueryFieldIsNot" }, { "begins", "apQueryFieldBegins" }, { "contains", "apQueryFieldContains" }, { "dateLess", "apQueryFieldDateLess" }, { "dateGreater", "apQueryFieldDateGreater" }, { "is", "apQueryFieldIs" }, { "numberEquals", "apQueryFieldNumberEquals" }, { "numberNotEquals", "apQueryFieldNumberNotEquals" }, { "numberLE", "apQueryFieldLessOrEqual" }, { "numberLess", "apQueryFieldLess" }, { "numberGE", "apQueryFieldGreaterOrEqual" }, { "numberGreater", "apQueryFieldGreater" } };
/*     */ 
/*  56 */   protected static final String[][] SQL_OP_MAP = { { "sqlEq", "=" }, { "sqlNeq", "<>" }, { "begins", "LIKE" }, { "contains", "LIKE" }, { "dateLess", "<" }, { "dateGreater", ">" }, { "is", "Is" }, { "numberLess", "<" }, { "numberEquals", "=" }, { "numberNotEquals", "<>" }, { "numberGreater", ">" }, { "numberLE", "<=" }, { "numberGE", ">=" } };
/*     */ 
/*  74 */   public static final short[] SQL_COMPARISON_INDEX_STARTS = { 0, 4, 6, 7 };
/*     */   protected String m_wildcards;
/*     */ 
/*     */   public SqlQueryData()
/*     */   {
/*  82 */     this.m_operatorCodes = SQL_OPERATOR_CODES;
/*  83 */     this.m_operatorMap = SQL_OP_MAP;
/*  84 */     this.m_fieldComparisonIndexStart = SQL_COMPARISON_INDEX_STARTS;
/*  85 */     this.m_wildcards = "%_";
/*     */ 
/*  87 */     this.m_legacyOpMap = new String[][] { { "sqlEq", "=" }, { "sqlNeq", "<>" } };
/*     */   }
/*     */ 
/*     */   public void setWildcards(String wildcards)
/*     */   {
/*  96 */     this.m_wildcards = wildcards;
/*     */   }
/*     */ 
/*     */   public void appendClause(String field, IdcStringBuilder query, String op, String value)
/*     */     throws ServiceException
/*     */   {
/* 103 */     boolean isEqualCheck = op.equals("sqlEq");
/* 104 */     boolean addNullClause = false;
/* 105 */     if ((isEqualCheck) || (op.equals("sqlNeq")))
/*     */     {
/* 107 */       addNullClause = (value == null) || (value.length() == 0);
/*     */     }
/*     */ 
/* 110 */     if (addNullClause)
/*     */     {
/* 112 */       query.append("(");
/*     */     }
/* 114 */     super.appendClause(field, query, op, value);
/* 115 */     if (!addNullClause)
/*     */       return;
/* 117 */     if (isEqualCheck)
/*     */     {
/* 119 */       query.append(" OR ");
/*     */     }
/*     */     else
/*     */     {
/* 123 */       query.append(" AND ");
/*     */     }
/* 125 */     query.append(field);
/* 126 */     if (isEqualCheck)
/*     */     {
/* 128 */       query.append(" IS NULL)");
/*     */     }
/*     */     else
/*     */     {
/* 132 */       query.append(" IS NOT NULL)");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void appendOpAndValue(IdcStringBuilder query, String op, String value)
/*     */     throws ServiceException
/*     */   {
/* 141 */     String dataOp = getPresentationOperatorString(op);
/* 142 */     String convVal = null;
/* 143 */     int opLen = op.length();
/* 144 */     boolean isBeginWild = false;
/* 145 */     boolean isEndWild = false;
/*     */ 
/* 147 */     if ((opLen >= 4) && (op.substring(0, 4).equals("date")))
/*     */     {
/* 149 */       convVal = value;
/*     */     }
/* 151 */     else if ((opLen >= 6) && (op.substring(0, 6).equals("number")))
/*     */     {
/* 153 */       convVal = value;
/*     */     }
/* 155 */     else if ((opLen >= 2) && (op.substring(0, 2).equals("is")))
/*     */     {
/* 157 */       boolean isTrue = StringUtils.convertToBool(value, true);
/* 158 */       convVal = "0";
/*     */ 
/* 160 */       dataOp = "=";
/*     */ 
/* 162 */       if (isTrue)
/*     */       {
/* 164 */         dataOp = "<>";
/*     */       }
/*     */     }
/* 167 */     else if (op.equals("begins"))
/*     */     {
/* 169 */       isEndWild = true;
/*     */     }
/* 171 */     else if (op.equals("contains"))
/*     */     {
/* 173 */       isBeginWild = true;
/* 174 */       isEndWild = true;
/*     */     }
/*     */ 
/* 177 */     query.append(dataOp);
/* 178 */     query.append(" ");
/* 179 */     if (convVal != null)
/*     */     {
/* 182 */       query.append(convVal);
/*     */     }
/*     */     else
/*     */     {
/* 186 */       query.append("'");
/* 187 */       if (isBeginWild)
/*     */       {
/* 189 */         query.append(this.m_wildcards.charAt(0));
/*     */       }
/*     */ 
/* 192 */       query.append(StringUtils.createQuotableString(value));
/* 193 */       if (isEndWild)
/*     */       {
/* 195 */         query.append(this.m_wildcards.charAt(0));
/*     */       }
/* 197 */       query.append("'");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 204 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SqlQueryData
 * JD-Core Version:    0.5.4
 */