/*     */ package intradoc.shared.workflow;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.shared.ClausesData;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JumpClausesData extends ClausesData
/*     */ {
/*  31 */   protected Properties m_jumpProps = null;
/*  32 */   protected String m_orConjunction = null;
/*     */ 
/*  36 */   protected boolean m_hasCustom = false;
/*     */ 
/*  39 */   protected static final String[][] SCRIPT_OPERATOR_CODES = { { "matches", "apQueryFieldMatches" }, { "contains", "apQueryFieldContains" }, { "begins", "apQueryFieldBegins" }, { "dateLess", "apQueryFieldDateLess" }, { "dateGreater", "apQueryFieldDateGreater" }, { "numberEquals", "apQueryFieldNumberEquals" }, { "numberNotEquals", "apQueryFieldNumberNotEquals" }, { "numberLE", "apQueryFieldLessOrEqual" }, { "numberLess", "apQueryFieldLess" }, { "numberGE", "apQueryFieldGreaterOrEqual" }, { "numberGreater", "apQueryFieldGreater" } };
/*     */ 
/*  55 */   protected static final String[][] SCRIPT_OP_MAP = { { "matches", "like" }, { "contains", "like" }, { "begins", "like" }, { "dateLess", "<" }, { "dateGreater", ">" }, { "numberGreater", ">" }, { "numberLess", "<" }, { "numberEquals", "==" }, { "numberNotEquals", "!=" }, { "numberGE", ">=" }, { "numberLE", "<=" } };
/*     */ 
/*  71 */   public static final short[] SCRIPT_COMPARISON_INDEX_STARTS = { 0, 3, -1, 5 };
/*     */   protected String m_wildcards;
/*     */ 
/*     */   public JumpClausesData()
/*     */   {
/*  78 */     init(false);
/*     */   }
/*     */ 
/*     */   public JumpClausesData(boolean hasCustom)
/*     */   {
/*  83 */     init(hasCustom);
/*     */   }
/*     */ 
/*     */   protected void init(boolean hasCustom)
/*     */   {
/*  88 */     this.m_dispStr = "No Script Parameters Defined";
/*  89 */     this.m_operatorCodes = SCRIPT_OPERATOR_CODES;
/*  90 */     this.m_operatorMap = SCRIPT_OP_MAP;
/*  91 */     this.m_fieldComparisonIndexStart = SCRIPT_COMPARISON_INDEX_STARTS;
/*  92 */     this.m_wildcards = "*?";
/*  93 */     this.m_conjunction = " and ";
/*  94 */     this.m_orConjunction = " or ";
/*  95 */     this.m_jumpProps = new Properties();
/*  96 */     this.m_hasCustom = hasCustom;
/*     */   }
/*     */ 
/*     */   public void parseJumpScript(Properties jumpProps, Properties scriptProps)
/*     */   {
/* 102 */     this.m_jumpProps = jumpProps;
/* 103 */     String jumpClauses = jumpProps.getProperty("wfJumpClauses");
/* 104 */     parse(jumpClauses);
/* 105 */     this.m_isCustom = StringUtils.convertToBool(scriptProps.getProperty("wfIsCustomScript"), false);
/*     */   }
/*     */ 
/*     */   public Properties formatJumpScript()
/*     */   {
/* 112 */     String jumpClauses = formatString();
/* 113 */     this.m_jumpProps.put("wfJumpClauses", jumpClauses);
/*     */ 
/* 115 */     return this.m_jumpProps;
/*     */   }
/*     */ 
/*     */   public void setNameValueFormatAt(Vector list, int index, String name, String value)
/*     */   {
/* 121 */     if ((!this.m_hasCustom) && (((name.equals("CustomQuery")) || (name.equals("IsCustom")))))
/*     */     {
/* 123 */       int len = list.size();
/* 124 */       list.setSize(len - 1);
/* 125 */       return;
/*     */     }
/* 127 */     super.setNameValueFormatAt(list, index, name, value);
/*     */   }
/*     */ 
/*     */   public void appendClause(String field, IdcStringBuilder query, String op, String value)
/*     */     throws ServiceException
/*     */   {
/* 134 */     String dataOp = StringUtils.getPresentationString(this.m_operatorMap, op);
/* 135 */     String convVal = null;
/* 136 */     int opLen = op.length();
/*     */ 
/* 138 */     boolean isDate = false;
/* 139 */     boolean isBeginWild = false;
/* 140 */     boolean isEndWild = false;
/*     */ 
/* 142 */     if ((opLen >= 4) && (op.substring(0, 4).equals("date")))
/*     */     {
/* 144 */       isDate = true;
/*     */ 
/* 147 */       if (value.length() > 0)
/*     */       {
/* 149 */         char ch = value.charAt(0);
/* 150 */         boolean isDigit = Character.isDigit(ch);
/* 151 */         boolean useQuotes = (isDigit) || (value.startsWith("{ts '"));
/* 152 */         convVal = "parseDate(";
/* 153 */         if (useQuotes)
/*     */         {
/* 155 */           convVal = convVal + "\"";
/*     */         }
/* 157 */         convVal = convVal + value;
/*     */ 
/* 159 */         if (useQuotes)
/*     */         {
/* 161 */           convVal = convVal + "\"";
/*     */         }
/* 163 */         convVal = convVal + ")";
/*     */       }
/*     */     }
/* 166 */     else if ((opLen >= 6) && (op.substring(0, 6).equals("number")))
/*     */     {
/* 168 */       convVal = value;
/*     */     }
/* 170 */     else if (op.equals("begins"))
/*     */     {
/* 172 */       isEndWild = true;
/*     */     }
/* 174 */     else if (op.equals("contains"))
/*     */     {
/* 176 */       isBeginWild = true;
/* 177 */       isEndWild = true;
/*     */     }
/*     */ 
/* 181 */     if (isDate)
/*     */     {
/* 183 */       query.append("parseDate(");
/*     */     }
/*     */ 
/* 187 */     if ((field.equals("entryCount")) || (field.equals("lastEntryTs")))
/*     */     {
/* 189 */       field = "wfCurrentGet(\"" + field + "\")";
/*     */     }
/* 191 */     else if ((field.equals("wfAction")) && (value.equals("UPDATE")))
/*     */     {
/* 194 */       value = "META_UPDATE|TIMED_UPDATE";
/*     */     }
/*     */ 
/* 197 */     query.append(field);
/*     */ 
/* 199 */     if (isDate)
/*     */     {
/* 201 */       query.append(")");
/*     */     }
/*     */ 
/* 205 */     query.append(" ");
/* 206 */     query.append(dataOp);
/* 207 */     query.append(" ");
/* 208 */     if (convVal != null)
/*     */     {
/* 210 */       query.append(convVal);
/*     */     }
/*     */     else
/*     */     {
/* 214 */       query.append("\"");
/* 215 */       if (isBeginWild)
/*     */       {
/* 217 */         query.append(this.m_wildcards.charAt(0));
/*     */       }
/* 219 */       value = StringUtils.encodeLiteralStringEscapeSequence(value);
/* 220 */       query.append(value);
/* 221 */       if (isEndWild)
/*     */       {
/* 223 */         query.append(this.m_wildcards.charAt(0));
/*     */       }
/* 225 */       query.append("\"");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getQueryProp(String key)
/*     */   {
/* 232 */     if ((key.equals("CurrentIndex")) || (key.equals("CustomQuery")) || (key.equals("IsCustom")))
/*     */     {
/* 234 */       return super.getQueryProp(key);
/*     */     }
/* 236 */     return this.m_jumpProps.getProperty(key);
/*     */   }
/*     */ 
/*     */   public void setQueryProp(String key, String val)
/*     */   {
/* 242 */     if (val == null)
/*     */     {
/* 244 */       val = "";
/*     */     }
/* 246 */     if ((key.equals("CurrentIndex")) || (key.equals("CustomQuery")) || (key.equals("IsCustom")))
/*     */     {
/* 248 */       super.setQueryProp(key, val);
/*     */     }
/*     */     else
/*     */     {
/* 252 */       this.m_jumpProps.put(key, val);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Properties getJumpProperties()
/*     */   {
/* 258 */     return this.m_jumpProps;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 263 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.workflow.JumpClausesData
 * JD-Core Version:    0.5.4
 */