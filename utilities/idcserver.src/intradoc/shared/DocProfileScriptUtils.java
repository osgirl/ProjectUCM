/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Enumeration;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocProfileScriptUtils
/*     */ {
/*  34 */   public static final String[] DP_LINK_COLUMNS = { "dpRuleConditionName", "dpRuleClauses" };
/*     */ 
/*  40 */   public static final String[] DP_RULE_COLUMNS = { "dpRuleConditionName", "dpRuleClauses", "dpRuleValue" };
/*     */ 
/*  45 */   public static final String[] DP_RULE_ACTIVATION_COLUMNS = { "dpRuleConditionName", "dpRuleClauses", "dpEvent", "dpAction", "dpFlag" };
/*     */ 
/*  50 */   public static final String[] DP_RULE_RESTRICTEDLIST_COLUMNS = { "dpRuleListValue" };
/*     */   public static final String m_fieldRuleTable = "RuleFields";
/*  57 */   public static final String[] DP_FIELD_RULE_COLUMNS = { "dpRuleFieldName", "dpRuleFieldType", "dpRuleFieldPosition" };
/*     */ 
/*  66 */   public static final String[] DP_DOCRULE_COLUMNS = { "dpRuleName", "dpRulePriority" };
/*     */ 
/*  71 */   public static final String[] RULE_TABLE_SUFFIXES = { "RuleFields", "RuleClauses", "RestrictedList" };
/*  72 */   public static final String[] PROFILE_TABLE_SUFFIXES = { "ProfileRules", "LinkClauses" };
/*     */ 
/*     */   public static Properties loadConfiguration(String type)
/*     */   {
/*  76 */     Properties props = new Properties();
/*  77 */     props.put("ScriptType", type);
/*     */ 
/*  79 */     type = type.toLowerCase();
/*  80 */     if (type.indexOf("default") >= 0)
/*     */     {
/*  82 */       props.put("TableName", "DefaultRuleClauses");
/*  83 */       props.put("ValueKey", "dprDefaultValue");
/*  84 */       props.put("CustomKey", "dprDefaultCustomScript");
/*  85 */       props.put("IsCustomKey", "dprDefaultIsCustom");
/*     */     }
/*  87 */     else if (type.indexOf("derived") >= 0)
/*     */     {
/*  89 */       props.put("TableName", "DerivedRuleClauses");
/*  90 */       props.put("ValueKey", "dprDerivedValue");
/*  91 */       props.put("CustomKey", "dprDerivedCustomScript");
/*  92 */       props.put("IsCustomKey", "dprDerivedIsCustom");
/*     */     }
/*  94 */     else if (type.indexOf("activation") >= 0)
/*     */     {
/*  96 */       props.put("TableName", "ActivationRuleClauses");
/*  97 */       props.put("CustomKey", "dprActivationCustomScript");
/*  98 */       props.put("IsCustomKey", "dprActivationIsCustom");
/*     */     }
/* 100 */     else if (type.indexOf("restricted") >= 0)
/*     */     {
/* 102 */       props.put("TableName", "RestrictedList");
/*     */     }
/* 104 */     else if (type.indexOf("checkinlink") >= 0)
/*     */     {
/* 106 */       props.put("TableName", "CheckinLinkClauses");
/* 107 */       props.put("IsLinkKey", "isLinkActive");
/* 108 */       props.put("CustomKey", "dpCheckinLinkCustomScript");
/* 109 */       props.put("IsCustomKey", "dpCheckinLinkScriptIsCustom");
/*     */     }
/* 111 */     else if (type.indexOf("searchlink") >= 0)
/*     */     {
/* 113 */       props.put("TableName", "SearchLinkClauses");
/* 114 */       props.put("IsLinkKey", "isLinkActive");
/* 115 */       props.put("CustomKey", "dpSearchLinkCustomScript");
/* 116 */       props.put("IsCustomKey", "dpSearchLinkScriptIsCustom");
/*     */     }
/* 118 */     return props;
/*     */   }
/*     */ 
/*     */   public static String computeScriptString(String prefix, DataBinder binder, String type, boolean isSummary)
/*     */   {
/* 124 */     Properties scriptProps = loadConfiguration(type);
/* 125 */     String isCustomKey = scriptProps.getProperty("IsCustomKey");
/* 126 */     String customKey = scriptProps.getProperty("CustomKey");
/* 127 */     String tableName = scriptProps.getProperty("TableName");
/* 128 */     String valueKey = scriptProps.getProperty("ValueKey");
/*     */ 
/* 130 */     String scriptStr = null;
/* 131 */     boolean isCustom = StringUtils.convertToBool(binder.getLocal(prefix + isCustomKey), false);
/*     */ 
/* 133 */     if (isCustom)
/*     */     {
/* 135 */       scriptStr = binder.getLocal(prefix + customKey);
/*     */     }
/*     */     else
/*     */     {
/* 139 */       DataResultSet rset = (DataResultSet)binder.getResultSet(prefix + tableName);
/*     */ 
/* 141 */       if (rset != null)
/*     */       {
/* 144 */         Properties localData = binder.getLocalData();
/* 145 */         int pfxLength = prefix.length();
/* 146 */         for (Enumeration en = localData.keys(); en.hasMoreElements(); )
/*     */         {
/* 148 */           String key = (String)en.nextElement();
/* 149 */           if (key.startsWith(prefix))
/*     */           {
/* 151 */             scriptProps.put(key.substring(pfxLength), localData.getProperty(key));
/*     */           }
/*     */         }
/*     */ 
/* 155 */         scriptStr = formatString(rset, scriptProps, isCustomKey, valueKey);
/*     */       }
/*     */       else
/*     */       {
/* 159 */         scriptStr = "";
/*     */       }
/*     */     }
/* 162 */     return scriptStr;
/*     */   }
/*     */ 
/*     */   public static String formatString(DataResultSet clauseSet, Properties scriptProps, String isCustomKey, String valueKey)
/*     */   {
/* 168 */     boolean isLink = false;
/* 169 */     String type = scriptProps.getProperty("ScriptType").toLowerCase();
/* 170 */     if (type.indexOf("activation") >= 0)
/*     */     {
/* 172 */       return formatActivationString(clauseSet, scriptProps, isCustomKey);
/*     */     }
/* 174 */     if (type.indexOf("link") >= 0)
/*     */     {
/* 176 */       isLink = true;
/* 177 */       valueKey = "isLinkActive";
/*     */     }
/*     */ 
/* 180 */     StringBuffer buff = new StringBuffer();
/*     */     try
/*     */     {
/* 183 */       int numRows = clauseSet.getNumRows();
/* 184 */       int count = 0;
/* 185 */       for (clauseSet.first(); clauseSet.isRowPresent(); ++count)
/*     */       {
/* 187 */         Properties props = clauseSet.getCurrentRowProps();
/* 188 */         RuleClausesData clauses = new RuleClausesData(false, isCustomKey);
/* 189 */         clauses.parseRuleScript(props, scriptProps);
/*     */ 
/* 191 */         String str = clauses.createQueryString().trim();
/* 192 */         boolean isInIfStatement = true;
/* 193 */         if (count == 0)
/*     */         {
/* 195 */           if (str.length() > 0)
/*     */           {
/* 197 */             buff.append("<$if ");
/*     */           }
/*     */           else
/*     */           {
/* 201 */             isInIfStatement = false;
/*     */           }
/*     */         }
/* 204 */         else if (str.trim().length() == 0)
/*     */         {
/* 206 */           buff.append("<$else");
/*     */         }
/*     */         else
/*     */         {
/* 210 */           buff.append("<$elseif ");
/*     */         }
/* 212 */         buff.append(str);
/*     */ 
/* 214 */         if (isInIfStatement)
/*     */         {
/* 216 */           buff.append("$>\n");
/*     */         }
/*     */ 
/* 219 */         if (isLink)
/*     */         {
/* 221 */           buff.append("\t<$");
/* 222 */           buff.append(valueKey);
/* 223 */           buff.append("=1$>\n");
/*     */         }
/*     */         else
/*     */         {
/* 227 */           addValueClause("dpRuleValue", clauses, valueKey, buff);
/*     */         }
/*     */ 
/* 230 */         if ((isInIfStatement) && (count == numRows - 1))
/*     */         {
/* 232 */           buff.append("<$endif$>\n");
/*     */         }
/* 185 */         clauseSet.next();
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 238 */       IdcMessage message = IdcMessageFactory.lc(e);
/* 239 */       String msgText = LocaleResources.localizeMessage(null, message, null).toString();
/* 240 */       Report.trace("docprofile", "Unable to format doc profile script for " + valueKey + ". Error: " + msgText, e);
/*     */     }
/*     */ 
/* 243 */     return buff.toString();
/*     */   }
/*     */ 
/*     */   protected static void addValueClause(String field, RuleClausesData clauses, String valueKey, StringBuffer buff)
/*     */   {
/* 249 */     String val = clauses.getQueryProp(field);
/* 250 */     if ((val == null) || (val.length() == 0))
/*     */     {
/* 252 */       return;
/*     */     }
/*     */ 
/* 255 */     boolean isValQuoted = true;
/* 256 */     if (val.charAt(0) == '@')
/*     */     {
/* 258 */       val = val.substring(1);
/* 259 */       isValQuoted = false;
/* 260 */       if (val.length() == 0)
/*     */       {
/* 262 */         return;
/*     */       }
/*     */     }
/* 265 */     buff.append("\t<$");
/* 266 */     buff.append(valueKey);
/* 267 */     buff.append("=");
/* 268 */     if (isValQuoted)
/*     */     {
/* 270 */       buff.append("\"");
/*     */     }
/* 272 */     buff.append(val);
/* 273 */     if (isValQuoted)
/*     */     {
/* 275 */       buff.append("\"");
/*     */     }
/* 277 */     buff.append("$>\n");
/*     */   }
/*     */ 
/*     */   public static String formatActivationString(DataResultSet clauseSet, Properties scriptProps, String isCustomKey)
/*     */   {
/* 283 */     StringBuffer buff = new StringBuffer();
/*     */     try
/*     */     {
/* 286 */       int count = 0;
/* 287 */       for (clauseSet.first(); clauseSet.isRowPresent(); ++count)
/*     */       {
/* 289 */         Properties props = clauseSet.getCurrentRowProps();
/* 290 */         RuleClausesData clauses = new RuleClausesData(true, isCustomKey);
/* 291 */         clauses.parseRuleScript(props, scriptProps);
/*     */ 
/* 293 */         boolean inStatement = false;
/* 294 */         String str = clauses.createQueryString().trim();
/* 295 */         if (str.length() > 0)
/*     */         {
/* 297 */           if (buff.length() > 0)
/*     */           {
/* 299 */             buff.append(" or\n");
/*     */           }
/* 301 */           buff.append("(");
/* 302 */           buff.append(str);
/* 303 */           inStatement = true;
/*     */         }
/*     */ 
/* 306 */         int len = DP_RULE_ACTIVATION_COLUMNS.length;
/* 307 */         for (int i = 0; i < len; ++i)
/*     */         {
/* 309 */           String field = DP_RULE_ACTIVATION_COLUMNS[i];
/* 310 */           boolean isBooleanField = false;
/* 311 */           if (field.equals("dpRuleConditionName")) continue; if (field.equals("dpRuleClauses"))
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 316 */           String val = clauses.getQueryProp(field);
/* 317 */           if (val == null) continue; if (val.length() == 0)
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 322 */           if (field.equals("dpFlag"))
/*     */           {
/* 324 */             isBooleanField = true;
/*     */           }
/*     */ 
/* 327 */           boolean inLoopStatement = false;
/* 328 */           Vector v = StringUtils.parseArray(val, ',', '^');
/* 329 */           int size = v.size();
/* 330 */           for (int j = 0; j < size; ++j)
/*     */           {
/* 332 */             val = (String)v.elementAt(j);
/* 333 */             if (val == null) continue; if (val.trim().length() == 0) {
/*     */               continue;
/*     */             }
/*     */ 
/* 337 */             if (!inLoopStatement)
/*     */             {
/* 339 */               if (inStatement)
/*     */               {
/* 341 */                 buff.append(" and\n");
/*     */               }
/*     */               else
/*     */               {
/* 345 */                 if (buff.length() > 0)
/*     */                 {
/* 347 */                   buff.append(" or\n");
/*     */                 }
/* 349 */                 buff.append(" (");
/*     */               }
/* 351 */               buff.append(" (");
/* 352 */               inLoopStatement = true;
/* 353 */               inStatement = true;
/*     */             }
/*     */             else
/*     */             {
/* 357 */               buff.append(" or ");
/*     */             }
/* 359 */             if (isBooleanField)
/*     */             {
/* 361 */               Vector bVals = StringUtils.parseArray(val, ':', '*');
/* 362 */               boolean isTrue = StringUtils.convertToBool((String)bVals.elementAt(1), false);
/* 363 */               if (!isTrue)
/*     */               {
/* 365 */                 buff.append("not ");
/*     */               }
/* 367 */               buff.append(bVals.elementAt(0));
/*     */             }
/*     */             else
/*     */             {
/* 371 */               buff.append(field);
/* 372 */               buff.append(" like \"");
/* 373 */               buff.append(val);
/* 374 */               buff.append("\"");
/*     */             }
/*     */           }
/*     */ 
/* 378 */           if (!inLoopStatement)
/*     */             continue;
/* 380 */           buff.append(")");
/*     */         }
/*     */ 
/* 383 */         if (inStatement)
/*     */         {
/* 385 */           buff.append(")");
/*     */         }
/* 287 */         clauseSet.next();
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 391 */       IdcMessage msg = IdcMessageFactory.lc(e);
/* 392 */       String msgText = LocaleResources.localizeMessage(null, msg, null).toString();
/* 393 */       Report.trace("docprofile", "Unable to format doc profile activation script. Error: " + msgText, e);
/*     */     }
/*     */ 
/* 396 */     return buff.toString();
/*     */   }
/*     */ 
/*     */   public static List<String> getTableSuffixes(String type)
/*     */   {
/* 401 */     List list = new ArrayList();
/* 402 */     if (type.equals("rule"))
/*     */     {
/* 404 */       list = StringUtils.convertToList(RULE_TABLE_SUFFIXES);
/*     */     }
/* 406 */     else if (type.equals("profile"))
/*     */     {
/* 408 */       list = StringUtils.convertToList(PROFILE_TABLE_SUFFIXES);
/*     */     }
/* 410 */     return list;
/*     */   }
/*     */ 
/*     */   public static String[][] createDisplayIncludeOptions(String pattern, ExecutionContext cxt)
/*     */   {
/* 415 */     List rows = new IdcVector();
/* 416 */     DataResultSet rset = SharedObjects.getTable("DpDisplayIncludes");
/* 417 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 419 */       Map map = rset.getCurrentRowMap();
/* 420 */       String type = (String)map.get("dpIncludeType");
/* 421 */       if (!StringUtils.matchEx(type, pattern, true, false)) {
/*     */         continue;
/*     */       }
/* 424 */       String desc = (String)map.get("dpIncludeDesc");
/* 425 */       String name = (String)map.get("dpIncludeName");
/* 426 */       IdcMessage tmp = IdcMessageFactory.lc("apDpIncludeDesc", new Object[] { desc, name });
/*     */ 
/* 428 */       String[] row = new String[2];
/* 429 */       row[0] = name;
/* 430 */       row[1] = LocaleResources.localizeMessage(null, tmp, cxt).toString();
/* 431 */       rows.add(row);
/*     */     }
/*     */ 
/* 435 */     int nrows = rows.size();
/* 436 */     String[][] table = new String[nrows][];
/* 437 */     rows.toArray(table);
/* 438 */     return table;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 443 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80607 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.DocProfileScriptUtils
 * JD-Core Version:    0.5.4
 */