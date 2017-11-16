/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.DebugTracingCallback;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcAppendableStringBuffer;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcNumberFormat;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseStringException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.common.TimeZoneFormat;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class QueryUtils
/*     */ {
/*     */   public static int convertInfoStringToType(String typeStr)
/*     */   {
/*  33 */     int quoteIndex = typeStr.indexOf(58);
/*  34 */     if ((quoteIndex >= 0) && (quoteIndex <= typeStr.length() - 1))
/*     */     {
/*  36 */       typeStr = typeStr.substring(quoteIndex + 1);
/*     */     }
/*  38 */     typeStr = typeStr.toLowerCase();
/*     */ 
/*  40 */     int type = 6;
/*  41 */     if (typeStr.startsWith("int"))
/*     */     {
/*  43 */       type = 3;
/*     */     }
/*  45 */     else if (typeStr.startsWith("boolean"))
/*     */     {
/*  47 */       type = 1;
/*     */     }
/*  49 */     else if ((typeStr.startsWith("date")) || (typeStr.startsWith("datetime")))
/*     */     {
/*  51 */       type = 5;
/*     */     }
/*  53 */     else if (typeStr.startsWith("blob"))
/*     */     {
/*  55 */       type = 9;
/*     */     }
/*  57 */     else if (typeStr.startsWith("clob"))
/*     */     {
/*  59 */       type = 10;
/*     */     }
/*  61 */     else if (typeStr.startsWith("column"))
/*     */     {
/*  63 */       type = -101;
/*     */     }
/*  65 */     else if (typeStr.startsWith("table"))
/*     */     {
/*  67 */       type = -102;
/*     */     }
/*  69 */     else if (typeStr.startsWith("resultset"))
/*     */     {
/*  71 */       type = -201;
/*     */     }
/*  73 */     else if (typeStr.startsWith("decimal"))
/*     */     {
/*  75 */       type = 11;
/*     */     }
/*     */ 
/*  78 */     return type;
/*     */   }
/*     */ 
/*     */   public static boolean hasListAsValue(String typeStr)
/*     */   {
/*  83 */     boolean hasListValue = false;
/*  84 */     if (typeStr != null)
/*     */     {
/*  86 */       int commaIndex = typeStr.indexOf(46);
/*  87 */       if ((commaIndex > 0) && (commaIndex < typeStr.length()))
/*     */       {
/*  89 */         String op = typeStr.substring(commaIndex + 1);
/*  90 */         if (op.equalsIgnoreCase("in"))
/*     */         {
/*  92 */           hasListValue = true;
/*     */         }
/*     */       }
/*     */     }
/*  96 */     return hasListValue;
/*     */   }
/*     */ 
/*     */   public static String convertInfoTypeToString(int type)
/*     */   {
/* 101 */     String typeStr = "varchar";
/* 102 */     switch (type)
/*     */     {
/*     */     case 1:
/* 105 */       typeStr = "boolean";
/* 106 */       break;
/*     */     case 3:
/* 109 */       typeStr = "int";
/* 110 */       break;
/*     */     case 5:
/* 113 */       typeStr = "date";
/* 114 */       break;
/*     */     case 9:
/* 117 */       typeStr = "blob";
/* 118 */       break;
/*     */     case 10:
/* 121 */       typeStr = "clob";
/*     */     case 2:
/*     */     case 4:
/*     */     case 6:
/*     */     case 7:
/*     */     case 8:
/*     */     }
/* 124 */     return typeStr;
/*     */   }
/*     */ 
/*     */   public static void addColumnMapRow(DataResultSet drset, String key)
/*     */   {
/* 130 */     Vector v = new IdcVector();
/* 131 */     v.addElement(key.toUpperCase());
/* 132 */     v.addElement(key);
/* 133 */     drset.addRow(v);
/*     */   }
/*     */ 
/*     */   public static void addQueryDef(DataResultSet qlist, String name, String queryStr, String parameters)
/*     */   {
/* 139 */     Vector v = new IdcVector();
/* 140 */     v.setSize(3);
/* 141 */     v.setElementAt(name, 0);
/* 142 */     v.setElementAt(queryStr, 1);
/* 143 */     v.setElementAt(parameters, 2);
/*     */ 
/* 145 */     qlist.addRow(v);
/*     */   }
/*     */ 
/*     */   public static void addQueryTable(Workspace workspace, ResourceContainer res, String tableName, boolean mustExist, String cmptName)
/*     */     throws DataException
/*     */   {
/* 151 */     Table tble = res.getTable(tableName);
/* 152 */     if (tble == null)
/*     */     {
/* 154 */       if (!mustExist)
/*     */       {
/* 156 */         return;
/*     */       }
/* 158 */       throw new DataException(LocaleUtils.encodeMessage("syUnableToLoadComponentQueries", null, tableName, cmptName) + "!syTableDoesNotExist");
/*     */     }
/*     */ 
/* 163 */     DataResultSet rset = new DataResultSet();
/* 164 */     rset.init(tble);
/* 165 */     workspace.addQueryDefs(rset);
/*     */   }
/*     */ 
/*     */   public static boolean appendSubclause(StringBuffer buf, boolean inClause, String keyword, String sep, String columnText)
/*     */   {
/* 174 */     if ((keyword != null) && (keyword.length() > 0) && (columnText != null) && (columnText.length() > 0))
/*     */     {
/* 177 */       if (inClause)
/*     */       {
/* 179 */         buf.append(sep);
/*     */       }
/*     */       else
/*     */       {
/* 183 */         buf.append(" ");
/* 184 */         buf.append(keyword);
/* 185 */         buf.append(" ");
/*     */       }
/* 187 */       inClause = true;
/* 188 */       buf.append(columnText);
/*     */     }
/* 190 */     return inClause;
/*     */   }
/*     */ 
/*     */   public static void appendAscendingOrDescending(StringBuffer buf, String sortField, String sortOrder)
/*     */   {
/* 196 */     if ((sortField == null) || (sortField.length() == 0))
/*     */     {
/* 198 */       return;
/*     */     }
/*     */ 
/* 201 */     if ((sortOrder == null) || (sortOrder.equals("ascending")))
/*     */     {
/* 203 */       sortOrder = "ASC";
/*     */     }
/*     */     else
/*     */     {
/* 207 */       sortOrder = "DESC";
/*     */     }
/* 209 */     buf.append(" ");
/* 210 */     buf.append(sortOrder);
/*     */   }
/*     */ 
/*     */   public static String createSubclause(FieldInfo info, String val, String op, String parentTable)
/*     */     throws DataException
/*     */   {
/* 216 */     IdcStringBuilder sql = new IdcStringBuilder();
/* 217 */     if ((parentTable != null) && (parentTable.length() > 0))
/*     */     {
/* 219 */       sql.append(parentTable);
/* 220 */       sql.append(".");
/*     */     }
/* 222 */     sql.append(info.m_name);
/* 223 */     sql.append(op);
/* 224 */     appendParam(sql, info.m_type, val, null);
/*     */ 
/* 226 */     return sql.toString();
/*     */   }
/*     */ 
/*     */   public static boolean createAndAppendSubclause(StringBuffer sql, FieldInfo info, String val, String op, String keyword, String sep, boolean inClause)
/*     */     throws DataException
/*     */   {
/* 232 */     if (inClause)
/*     */     {
/* 234 */       sql.append(sep);
/*     */     }
/*     */     else
/*     */     {
/* 238 */       sql.append(keyword);
/*     */     }
/*     */ 
/* 241 */     sql.append(info.m_name);
/* 242 */     sql.append(op);
/* 243 */     appendParam(sql, info.m_type, val);
/*     */ 
/* 245 */     inClause = true;
/* 246 */     return inClause;
/*     */   }
/*     */ 
/*     */   public static boolean createInsertAndUpdateClauses(StringBuffer[] sqlStubs, Vector fields, DataBinder workBinder, boolean inClause)
/*     */     throws DataException
/*     */   {
/* 255 */     int size = fields.size();
/* 256 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 258 */       FieldInfo info = (FieldInfo)fields.elementAt(i);
/* 259 */       String val = workBinder.get(info.m_name);
/*     */ 
/* 261 */       createUpdateClause(sqlStubs[0], info, val, ", ", inClause);
/* 262 */       inClause = createInsertClauses(sqlStubs[1], sqlStubs[2], info, val, inClause);
/*     */     }
/* 264 */     return inClause;
/*     */   }
/*     */ 
/*     */   public static boolean createInsertClauses(StringBuffer nameSql, StringBuffer valueSql, FieldInfo info, String val, boolean inClause)
/*     */     throws DataException
/*     */   {
/* 270 */     String sep = ", ";
/* 271 */     if (inClause)
/*     */     {
/* 273 */       nameSql.append(sep);
/* 274 */       valueSql.append(sep);
/*     */     }
/* 276 */     nameSql.append(info.m_name);
/* 277 */     appendParam(valueSql, info.m_type, val);
/* 278 */     inClause = true;
/*     */ 
/* 280 */     return inClause;
/*     */   }
/*     */ 
/*     */   protected static boolean createUpdateClause(StringBuffer sql, FieldInfo info, String val, String sep, boolean inClause)
/*     */     throws DataException
/*     */   {
/* 286 */     if (inClause)
/*     */     {
/* 288 */       sql.append(sep);
/*     */     }
/* 290 */     sql.append(info.m_name);
/* 291 */     sql.append("=");
/* 292 */     appendParam(sql, info.m_type, val);
/* 293 */     inClause = true;
/* 294 */     return inClause;
/*     */   }
/*     */ 
/*     */   public static void appendParam(StringBuffer buffer, int type, String value)
/*     */     throws DataException
/*     */   {
/* 300 */     IdcAppendable appendable = new IdcAppendableStringBuffer(buffer);
/* 301 */     appendParam(appendable, type, value, null);
/*     */   }
/*     */ 
/*     */   public static void appendParam(IdcAppendable buffer, int type, String value)
/*     */     throws DataException
/*     */   {
/* 307 */     appendParam(buffer, type, value, null);
/*     */   }
/*     */ 
/*     */   public static void appendParam(IdcAppendable buffer, int type, String value, DebugTracingCallback callback) throws DataException
/*     */   {
/*     */     try
/*     */     {
/* 314 */       appendParam(buffer, type, value, callback, null);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 318 */       Report.trace("Cannot add parameters to query", null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void appendParam(IdcAppendable buffer, int type, String value, DebugTracingCallback callback, Parameters args)
/*     */     throws DataException
/*     */   {
/* 325 */     String str = null;
/* 326 */     switch (type)
/*     */     {
/*     */     case 2:
/*     */     case 6:
/* 331 */       buffer = appendValueWithQuotes(buffer, value);
/*     */ 
/* 333 */       return;
/*     */     case 5:
/* 337 */       if (value != null)
/*     */       {
/* 339 */         value = value.trim();
/*     */       }
/* 341 */       if ((value == null) || (value.length() == 0) || (value.equals("null")))
/*     */       {
/* 343 */         str = "null";
/*     */       }
/*     */       else {
/* 346 */         boolean dateOkay = (value.startsWith("{ts '")) && (value.endsWith("'}"));
/* 347 */         if (dateOkay)
/*     */         {
/* 350 */           int start = "{ts '".length();
/* 351 */           int end = value.length() - "'}".length();
/* 352 */           for (int i = start; i < end; ++i)
/*     */           {
/* 354 */             char ch = value.charAt(i);
/* 355 */             if ((Character.isDigit(ch)) || (ch == '/') || (ch == '-') || (ch == ':') || (ch == '.') || (ch == ' '))
/*     */               continue;
/* 357 */             String msg = LocaleUtils.encodeMessage("csInvalidlyFormedJdbcDateForamt", null, value);
/* 358 */             if (callback != null)
/*     */             {
/* 360 */               callback.debugMsg(msg);
/*     */             }
/* 362 */             throw new DataException(msg);
/*     */           }
/*     */ 
/* 365 */           str = value;
/*     */         }
/*     */         else
/*     */         {
/* 369 */           if (callback != null)
/*     */           {
/* 371 */             callback.debugMsg("Non-JDBC date value '" + value + "'.");
/*     */           }
/* 373 */           Date dte = null;
/*     */           try
/*     */           {
/* 376 */             ExecutionContextAdaptor cxt = null;
/*     */ 
/* 378 */             if (args != null)
/*     */             {
/*     */               boolean useUserDateFormat;
/*     */               try
/*     */               {
/* 387 */                 useUserDateFormat = StringUtils.convertToBool(args.get("useUserDateFormatForDatabaseQuery"), false);
/*     */               }
/*     */               catch (DataException de)
/*     */               {
/* 392 */                 useUserDateFormat = false;
/*     */               }
/*     */               try
/*     */               {
/* 396 */                 String fmtString = args.get("UserDateFormat");
/* 397 */                 if ((useUserDateFormat) && (fmtString != null))
/*     */                 {
/* 400 */                   cxt = new ExecutionContextAdaptor();
/*     */ 
/* 402 */                   IdcNumberFormat format = new IdcNumberFormat();
/* 403 */                   format.setParseIntegerOnly(true);
/*     */ 
/* 405 */                   TimeZone tz = null;
/* 406 */                   String tZone = null;
/* 407 */                   if (args.get("UserTimeZone") != null)
/*     */                   {
/* 409 */                     tZone = args.get("UserTimeZone");
/* 410 */                     tz = LocaleResources.getTimeZone(tZone, cxt);
/*     */                   }
/*     */                   else
/*     */                   {
/* 414 */                     tz = LocaleResources.getSystemTimeZone();
/*     */                   }
/*     */ 
/* 417 */                   IdcDateFormat idcfmt = new IdcDateFormat();
/* 418 */                   TimeZoneFormat tZoneFormat = new TimeZoneFormat();
/*     */                   try
/*     */                   {
/* 422 */                     idcfmt.init(fmtString, tz, tZoneFormat, format);
/* 423 */                     idcfmt.setPattern(fmtString);
/* 424 */                     cxt.setCachedObject("UserDateFormat", idcfmt);
/*     */                   }
/*     */                   catch (ParseStringException e)
/*     */                   {
/* 428 */                     String msg = "Unable to parse " + fmtString + " (" + e.getMessage() + ")";
/* 429 */                     callback.debugMsg(msg);
/*     */                   }
/*     */                 }
/*     */ 
/*     */               }
/*     */               catch (DataException e)
/*     */               {
/* 436 */                 if (Report.m_verbose)
/*     */                 {
/* 438 */                   String msg = "Unable to get UserDateFormat for " + value;
/* 439 */                   callback.debugMsg(msg);
/*     */                 }
/*     */               }
/*     */             }
/*     */ 
/* 444 */             dte = LocaleResources.parseDate(value, cxt);
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 449 */             if (callback != null)
/*     */             {
/* 451 */               String msg = LocaleUtils.encodeMessage("csDateParseError", e.getMessage(), value);
/*     */ 
/* 453 */               callback.debugMsg("* * Invalid date format: " + value + "Error: " + e.getMessage());
/*     */ 
/* 455 */               callback.debugMsg(msg);
/*     */             }
/* 457 */             throw new DataException(e, "csDateParseError", new Object[] { value });
/*     */           }
/*     */ 
/* 461 */           str = LocaleUtils.formatODBC(dte);
/*     */         }
/*     */       }
/* 464 */       break;
/*     */     case 3:
/*     */     case 11:
/* 468 */       if ((value == null) || (value.length() == 0) || (value.equals("null")))
/*     */       {
/* 470 */         str = "null";
/*     */       }
/*     */       else
/*     */       {
/* 477 */         for (int i = 0; i < value.length(); ++i)
/*     */         {
/* 479 */           char ch = value.charAt(i);
/* 480 */           if ((Character.isDigit(ch)) || (ch == '-') || (ch == '+') || (ch == ',') || (ch == '.'))
/*     */             continue;
/* 482 */           String msg = LocaleUtils.encodeMessage("csNumberInvalidCharacters", null, value);
/* 483 */           if (callback != null)
/*     */           {
/* 485 */             callback.debugMsg(msg);
/*     */           }
/* 487 */           throw new DataException(msg);
/*     */         }
/*     */ 
/* 491 */         str = value;
/* 492 */       }break;
/*     */     case -102:
/*     */     case -101:
/* 497 */       if ((value != null) && (!validateDBUnenclosedValue(value)))
/*     */       {
/* 499 */         String msg = LocaleUtils.encodeMessage("csColumnOrTableInvalidCharacters", null, value);
/* 500 */         if (callback != null)
/*     */         {
/* 502 */           callback.debugMsg(msg);
/*     */         }
/* 504 */         throw new DataException(msg);
/*     */       }
/* 506 */       str = value;
/* 507 */       break;
/*     */     default:
/* 512 */       int index = -1;
/* 513 */       int endIndex = -1;
/* 514 */       if (value != null)
/*     */       {
/* 516 */         String tmpValue = value.toLowerCase();
/* 517 */         String[] keywords = { "exec", "begin", "insert", "delete", "insert", "update", "create", "drop", "from", "where", "alter" };
/*     */ 
/* 519 */         for (int i = 0; i < keywords.length; ++i)
/*     */         {
/* 521 */           index = tmpValue.indexOf(keywords[i]);
/* 522 */           if (index < 0)
/*     */             continue;
/* 524 */           endIndex = index + keywords[i].length();
/* 525 */           if (((index != 0) && (Character.isLetterOrDigit(tmpValue.charAt(index - 1)))) || (
/* 527 */             (endIndex != tmpValue.length() - 1) && (Character.isLetterOrDigit(tmpValue.charAt(endIndex))))) {
/*     */             continue;
/*     */           }
/* 530 */           String msg = LocaleUtils.encodeMessage("csQueryValueInvalidWord", null, value, keywords[i]);
/*     */ 
/* 533 */           if (callback != null)
/*     */           {
/* 535 */             callback.debugMsg(msg);
/*     */           }
/* 537 */           throw new DataException(msg);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 544 */       str = value;
/*     */     }
/*     */ 
/* 547 */     buffer.append(str);
/*     */   }
/*     */ 
/*     */   public static IdcAppendable appendValueWithQuotes(IdcAppendable buffer, String value)
/*     */   {
/* 637 */     String str = null;
/*     */ 
/* 639 */     buffer.append('\'');
/* 640 */     if (value != null)
/*     */     {
/* 642 */       str = StringUtils.createQuotableString(value);
/*     */     }
/*     */     else
/*     */     {
/* 646 */       str = "";
/*     */     }
/* 648 */     buffer.append(str);
/* 649 */     buffer.append('\'');
/*     */ 
/* 651 */     return buffer;
/*     */   }
/*     */ 
/*     */   public static String enclosingQueryWithSafeParenthesis(String query)
/*     */   {
/* 656 */     if ((query == null) || (query.length() == 0))
/*     */     {
/* 658 */       return "";
/*     */     }
/* 660 */     int numParentNeeded = computeNumSafeSecurityParenthesis(query);
/* 661 */     IdcStringBuilder buf = new IdcStringBuilder(query.length() + 40);
/* 662 */     for (int i = 0; i < numParentNeeded; ++i)
/*     */     {
/* 664 */       buf.append('(');
/*     */     }
/* 666 */     buf.append(query);
/* 667 */     for (int i = 0; i < numParentNeeded; ++i)
/*     */     {
/* 669 */       buf.append(')');
/*     */     }
/* 671 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public static int computeNumSafeSecurityParenthesis(String query)
/*     */   {
/* 696 */     char[] qChars = query.toCharArray();
/* 697 */     int nParensNeeded = 1;
/* 698 */     boolean insideUnsafe = false;
/* 699 */     int deductibleUnsafeParens = 0;
/* 700 */     int deductibleSafeParens = 0;
/* 701 */     boolean isPrevUnsafe = false;
/* 702 */     for (int i = 0; i < qChars.length; ++i)
/*     */     {
/* 704 */       char ch = qChars[i];
/* 705 */       boolean isSafe = true;
/* 706 */       if (ch == '(')
/*     */       {
/* 708 */         if (!isPrevUnsafe)
/*     */         {
/* 710 */           if (!insideUnsafe)
/*     */           {
/* 712 */             ++deductibleSafeParens;
/*     */           }
/*     */           else
/*     */           {
/* 716 */             ++deductibleUnsafeParens;
/*     */           }
/*     */         }
/*     */       }
/* 720 */       else if (ch == ')')
/*     */       {
/* 722 */         if (deductibleUnsafeParens > 0)
/*     */         {
/* 724 */           --deductibleUnsafeParens;
/*     */         }
/* 726 */         else if (deductibleSafeParens > 0)
/*     */         {
/* 728 */           --deductibleSafeParens;
/*     */         }
/*     */         else
/*     */         {
/* 732 */           ++nParensNeeded;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 737 */         isSafe = isSafeChar(ch);
/* 738 */         if (!isSafe)
/*     */         {
/* 740 */           insideUnsafe = true;
/* 741 */           deductibleUnsafeParens = 0;
/*     */         }
/*     */       }
/* 744 */       isPrevUnsafe = !isSafe;
/*     */     }
/*     */ 
/* 747 */     return nParensNeeded;
/*     */   }
/*     */ 
/*     */   protected static boolean isSafeChar(char ch)
/*     */   {
/* 752 */     return (ch < '\036') || (ch > '') || (Character.isLetterOrDigit(ch)) || (ch == ' ') || (ch == '<') || (ch == '>') || (ch == '.');
/*     */   }
/*     */ 
/*     */   public static boolean validateQuerySortClause(String sortClause)
/*     */   {
/* 757 */     boolean isValidated = true;
/* 758 */     sortClause = sortClause.trim().toLowerCase();
/*     */ 
/* 760 */     if ((sortClause.length() != 0) && (!sortClause.startsWith("order")))
/*     */     {
/* 762 */       isValidated = false;
/*     */     }
/* 764 */     if (isValidated)
/*     */     {
/* 766 */       isValidated = validateDBUnenclosedValue(sortClause);
/*     */     }
/* 768 */     return isValidated;
/*     */   }
/*     */ 
/*     */   public static boolean validateDBUnenclosedValue(String value)
/*     */   {
/* 773 */     boolean isValidated = true;
/* 774 */     int num = computeNumSafeSecurityParenthesis(value);
/*     */ 
/* 777 */     if (num > 1)
/*     */     {
/* 779 */       isValidated = false;
/*     */     }
/* 781 */     char[] chars = value.toCharArray();
/* 782 */     for (int i = 0; i < chars.length; ++i)
/*     */     {
/* 784 */       if (isSafeDBUneclosedChar(chars[i]))
/*     */         continue;
/* 786 */       isValidated = false;
/* 787 */       break;
/*     */     }
/*     */ 
/* 790 */     return isValidated;
/*     */   }
/*     */ 
/*     */   public static boolean isSafeDBUneclosedChar(char ch)
/*     */   {
/* 795 */     return (Character.isLetterOrDigit(ch)) || (Character.isWhitespace(ch)) || (ch == ',') || (ch == '.') || (ch == '_') || (ch == '*') || (ch == '$') || (ch == '(') || (ch == ')');
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 801 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89402 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.QueryUtils
 * JD-Core Version:    0.5.4
 */