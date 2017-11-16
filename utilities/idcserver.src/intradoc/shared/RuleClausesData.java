/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class RuleClausesData extends ClausesData
/*     */ {
/*  29 */   protected Properties m_ruleProps = null;
/*  30 */   protected String m_customKey = null;
/*  31 */   protected String m_orConjunction = null;
/*     */ 
/*  35 */   protected boolean m_hasCustom = false;
/*     */ 
/*  38 */   protected static final String[][] SCRIPT_OPERATOR_CODES = { { "matches", "apQueryFieldMatches" }, { "contains", "apQueryFieldContains" }, { "begins", "apQueryFieldBegins" }, { "dateLess", "apQueryFieldDateLess" }, { "dateGreater", "apQueryFieldDateGreater" }, { "is", "apQueryFieldIs" }, { "numberEquals", "apQueryFieldNumberEquals" }, { "numberNotEquals", "apQueryFieldNumberNotEquals" }, { "numberLE", "apQueryFieldLessOrEqual" }, { "numberLess", "apQueryFieldLess" }, { "numberGE", "apQueryFieldGreaterOrEqual" }, { "numberGreater", "apQueryFieldGreater" } };
/*     */ 
/*  55 */   protected static final String[][] SCRIPT_OP_MAP = { { "matches", "like" }, { "contains", "like" }, { "begins", "like" }, { "dateLess", "<" }, { "dateGreater", ">" }, { "is", "like" }, { "numberGreater", ">" }, { "numberLess", "<" }, { "numberEquals", "==" }, { "numberNotEquals", "!=" }, { "numberGE", ">=" }, { "numberLE", "<=" } };
/*     */ 
/*  72 */   public static final short[] SCRIPT_COMPARISON_INDEX_STARTS = { 0, 3, 5, 6 };
/*     */   protected String m_wildcards;
/*     */ 
/*     */   public RuleClausesData()
/*     */   {
/*  79 */     init(false);
/*     */   }
/*     */ 
/*     */   public RuleClausesData(boolean hasCustom, String customKey)
/*     */   {
/*  84 */     init(hasCustom);
/*     */ 
/*  87 */     this.m_customKey = customKey;
/*     */   }
/*     */ 
/*     */   public void init(boolean hasCustom)
/*     */   {
/*  92 */     this.m_dispStr = "No Rule Parameters Defined";
/*  93 */     this.m_operatorCodes = SCRIPT_OPERATOR_CODES;
/*  94 */     this.m_operatorMap = SCRIPT_OP_MAP;
/*  95 */     this.m_fieldComparisonIndexStart = SCRIPT_COMPARISON_INDEX_STARTS;
/*  96 */     this.m_wildcards = "*?";
/*  97 */     this.m_conjunction = " and ";
/*  98 */     this.m_orConjunction = " or ";
/*  99 */     this.m_hasCustom = hasCustom;
/* 100 */     this.m_ruleProps = new Properties();
/*     */   }
/*     */ 
/*     */   public void parseRuleScript(Properties ruleProps, Properties scriptProps)
/*     */   {
/* 106 */     this.m_ruleProps = ruleProps;
/* 107 */     String ruleClauses = ruleProps.getProperty("dpRuleClauses");
/* 108 */     parse(ruleClauses);
/* 109 */     this.m_isCustom = StringUtils.convertToBool(scriptProps.getProperty(this.m_customKey), false);
/*     */   }
/*     */ 
/*     */   public Properties formatRuleScript()
/*     */   {
/* 116 */     String ruleClauses = formatString();
/* 117 */     this.m_ruleProps.put("dpRuleClauses", ruleClauses);
/*     */ 
/* 119 */     return this.m_ruleProps;
/*     */   }
/*     */ 
/*     */   public void setNameValueFormatAt(Vector list, int index, String name, String value)
/*     */   {
/* 125 */     if ((!this.m_hasCustom) && (((name.equals("CustomQuery")) || (name.equals("IsCustom")))))
/*     */     {
/* 127 */       int len = list.size();
/* 128 */       list.setSize(len - 1);
/* 129 */       return;
/*     */     }
/* 131 */     super.setNameValueFormatAt(list, index, name, value);
/*     */   }
/*     */ 
/*     */   public void appendClause(String field, IdcStringBuilder query, String op, String value)
/*     */     throws ServiceException
/*     */   {
/* 138 */     String dataOp = StringUtils.getPresentationString(this.m_operatorMap, op);
/* 139 */     String convVal = null;
/* 140 */     int opLen = op.length();
/*     */ 
/* 142 */     boolean isDate = false;
/* 143 */     boolean isBeginWild = false;
/* 144 */     boolean isEndWild = false;
/*     */ 
/* 146 */     if ((opLen >= 4) && (op.substring(0, 4).equals("date")))
/*     */     {
/* 148 */       isDate = true;
/*     */ 
/* 151 */       if (value.length() > 0)
/*     */       {
/* 153 */         char ch = value.charAt(0);
/* 154 */         boolean isDigit = Character.isDigit(ch);
/* 155 */         boolean useQuotes = (isDigit) || (value.startsWith("{ts '"));
/* 156 */         convVal = "parseDate(";
/* 157 */         if (useQuotes)
/*     */         {
/* 159 */           convVal = convVal + "\"";
/*     */         }
/* 161 */         convVal = convVal + value;
/*     */ 
/* 163 */         if (useQuotes)
/*     */         {
/* 165 */           convVal = convVal + "\"";
/*     */         }
/* 167 */         convVal = convVal + ")";
/*     */       }
/*     */     }
/* 170 */     else if ((opLen >= 6) && (op.substring(0, 6).equals("number")))
/*     */     {
/* 172 */       convVal = value;
/*     */     }
/* 174 */     else if (op.equals("begins"))
/*     */     {
/* 176 */       isEndWild = true;
/*     */     }
/* 178 */     else if (op.equals("contains"))
/*     */     {
/* 180 */       isBeginWild = true;
/* 181 */       isEndWild = true;
/*     */     }
/*     */ 
/* 184 */     boolean isAppendValue = false;
/* 185 */     if (field.equals("role"))
/*     */     {
/* 187 */       isAppendValue = true;
/* 188 */       query.append("userHasRole(");
/*     */     }
/*     */     else
/*     */     {
/* 193 */       if (isDate)
/*     */       {
/* 195 */         query.append("parseDate(");
/*     */       }
/*     */ 
/* 198 */       query.append(field);
/*     */ 
/* 200 */       if (isDate)
/*     */       {
/* 202 */         query.append(")");
/*     */       }
/*     */ 
/* 206 */       query.append(" ");
/* 207 */       query.append(dataOp);
/* 208 */       query.append(" ");
/* 209 */       if (convVal != null)
/*     */       {
/* 211 */         query.append(convVal);
/*     */       }
/*     */       else
/*     */       {
/* 215 */         isAppendValue = true;
/*     */       }
/*     */     }
/*     */ 
/* 219 */     if (isAppendValue)
/*     */     {
/* 221 */       query.append("\"");
/* 222 */       if (isBeginWild)
/*     */       {
/* 224 */         query.append(this.m_wildcards.charAt(0));
/*     */       }
/* 226 */       value = StringUtils.encodeLiteralStringEscapeSequence(value);
/* 227 */       query.append(value);
/* 228 */       if (isEndWild)
/*     */       {
/* 230 */         query.append(this.m_wildcards.charAt(0));
/*     */       }
/* 232 */       query.append("\"");
/*     */     }
/* 234 */     if (!field.equals("role"))
/*     */       return;
/* 236 */     query.append(")");
/*     */   }
/*     */ 
/*     */   public String getQueryProp(String key)
/*     */   {
/* 243 */     if ((key.equals("CurrentIndex")) || (key.equals("CustomQuery")) || (key.equals("IsCustom")))
/*     */     {
/* 245 */       return super.getQueryProp(key);
/*     */     }
/* 247 */     return this.m_ruleProps.getProperty(key);
/*     */   }
/*     */ 
/*     */   public void setQueryProp(String key, String val)
/*     */   {
/* 253 */     if (val == null)
/*     */     {
/* 255 */       val = "";
/*     */     }
/* 257 */     if ((key.equals("CurrentIndex")) || (key.equals("CustomQuery")) || (key.equals("IsCustom")))
/*     */     {
/* 259 */       super.setQueryProp(key, val);
/*     */     }
/*     */     else
/*     */     {
/* 263 */       this.m_ruleProps.put(key, val);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Properties getRuleProps()
/*     */   {
/* 269 */     return this.m_ruleProps;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 274 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.RuleClausesData
 * JD-Core Version:    0.5.4
 */