/*     */ package intradoc.search;
/*     */ 
/*     */ import C;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.SearchOperatorParsedElements;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class QueryValueModifierUtils
/*     */ {
/*     */   public static boolean isOperatorReserved(String engineName, String operator, CommonSearchConfig csConfig)
/*     */   {
/*  47 */     boolean isReserved = false;
/*     */ 
/*  49 */     List reservedOperators = csConfig.getReservedProcessOperators(engineName);
/*  50 */     isReserved = isReservedEx(operator, reservedOperators, false);
/*     */ 
/*  52 */     return isReserved;
/*     */   }
/*     */ 
/*     */   public static List getReservedOperatorList(String engineName, Vector operators, CommonSearchConfig csConfig)
/*     */   {
/*  57 */     boolean isReserved = false;
/*  58 */     List reservedOperators = new ArrayList();
/*     */ 
/*  60 */     for (int opNo = 0; opNo < operators.size(); ++opNo)
/*     */     {
/*  62 */       String operator = (String)operators.get(opNo);
/*  63 */       isReserved = isOperatorReserved(engineName, operator, csConfig);
/*     */ 
/*  65 */       if (isReserved != true)
/*     */         continue;
/*  67 */       reservedOperators.add(operator);
/*     */     }
/*     */ 
/*  71 */     return reservedOperators;
/*     */   }
/*     */ 
/*     */   public static boolean isWordReserved(String engineName, String word, CommonSearchConfig csConfig)
/*     */   {
/*  82 */     boolean isReserved = false;
/*     */ 
/*  84 */     List reservedWords = csConfig.getReservedWords(engineName);
/*  85 */     isReserved = isReservedEx(word, reservedWords, true);
/*     */ 
/*  87 */     return isReserved;
/*     */   }
/*     */ 
/*     */   public static boolean isCharReserved(String engineName, char character, CommonSearchConfig csConfig)
/*     */   {
/*  99 */     boolean isReserved = false;
/*     */ 
/* 101 */     List reservedChars = csConfig.getReservedChars(engineName);
/*     */ 
/* 103 */     String expression = Character.toString(character);
/* 104 */     isReserved = isReservedEx(expression, reservedChars, false);
/*     */ 
/* 106 */     return isReserved;
/*     */   }
/*     */ 
/*     */   public static boolean isReservedEx(String expression, List reservedList, boolean ignoreCase)
/*     */   {
/* 111 */     boolean isReserved = false;
/*     */ 
/* 113 */     if (reservedList != null)
/*     */     {
/* 115 */       if (ignoreCase == true)
/*     */       {
/* 117 */         expression = expression.toLowerCase();
/*     */       }
/* 119 */       isReserved = reservedList.contains(expression);
/*     */     }
/*     */ 
/* 122 */     return isReserved;
/*     */   }
/*     */ 
/*     */   public static String processReservedSequences(CommonSearchConfig csConfig, ExecutionContext context, CommonSearchConfigCompanionAdaptor companion, DataBinder binder, String[] args)
/*     */     throws ServiceException, DataException
/*     */   {
/* 138 */     String engineName = csConfig.getCurrentEngineName();
/* 139 */     if (companion != null)
/*     */     {
/* 141 */       engineName = companion.m_queryDefinitionLabel;
/*     */     }
/*     */ 
/* 144 */     String operator = args[0];
/* 145 */     String fieldName = args[1];
/* 146 */     String value = args[2];
/*     */ 
/* 148 */     if ((value == null) || (value.length() == 0))
/*     */     {
/* 150 */       return value;
/*     */     }
/*     */ 
/* 153 */     boolean isOperatorReserved = isOperatorReserved(engineName, operator, csConfig);
/*     */ 
/* 155 */     if (!isOperatorReserved)
/*     */     {
/* 157 */       return value;
/*     */     }
/*     */ 
/* 160 */     SearchOperatorParsedElements el = csConfig.retrieveParsedElements(operator, engineName, fieldName);
/* 161 */     List decorators = csConfig.retrieveDecoratorList(el);
/* 162 */     boolean isReservedProcSupported = csConfig.isReservedProcSupportedWithDecorators(engineName, decorators);
/*     */ 
/* 164 */     if (!isReservedProcSupported)
/*     */     {
/* 166 */       return value;
/*     */     }
/*     */ 
/* 169 */     if (Report.m_verbose)
/*     */     {
/* 171 */       Report.trace("searchquery", "Processing reserved sequences from the query value '" + value + "' for operator " + operator, null);
/*     */     }
/*     */ 
/* 176 */     IdcStringBuilder processedValueBuilder = new IdcStringBuilder();
/*     */ 
/* 178 */     InternetSearchQuery query = processReservedActionsEx(csConfig, engineName, value, operator, binder, companion, context, args);
/*     */ 
/* 180 */     if (query != null)
/*     */     {
/* 182 */       appendProcessedSequences(query, processedValueBuilder, value, operator, binder, context);
/*     */     }
/*     */ 
/* 185 */     return processedValueBuilder.toString();
/*     */   }
/*     */ 
/*     */   public static InternetSearchQuery processReservedActionsEx(CommonSearchConfig csConfig, String engineName, String value, String operator, DataBinder binder, CommonSearchConfigCompanionAdaptor companion, ExecutionContext context, String[] args)
/*     */     throws ServiceException
/*     */   {
/* 217 */     String queryValueModifierClassName = csConfig.getQueryValueModifierClassName(engineName);
/*     */ 
/* 219 */     String escapeCharactersAction = CommonQueryValueModifier.m_queryActionsTable[0][CommonQueryValueModifier.KEY_INDEX];
/* 220 */     String escapeWordsAction = CommonQueryValueModifier.m_queryActionsTable[1][CommonQueryValueModifier.KEY_INDEX];
/*     */ 
/* 222 */     IdcStringBuilder actions = new IdcStringBuilder();
/*     */ 
/* 224 */     actions.append(escapeWordsAction);
/* 225 */     actions.append(",");
/* 226 */     actions.append(escapeCharactersAction);
/*     */ 
/* 228 */     String actionsString = actions.toString();
/*     */ 
/* 230 */     ParsedQueryElements parsedElts = SearchQueryUtils.lookupSearchParsingObject(context);
/* 231 */     InternetSearchQuery query = new InternetSearchQuery();
/* 232 */     query.init(csConfig, parsedElts, value.toCharArray(), binder, context);
/*     */ 
/* 234 */     IdcStringBuilder internalProcessValueBuilder = new IdcStringBuilder();
/* 235 */     boolean processAsSingleQuerySegment = DataBinderUtils.getBoolean(binder, "processAsSingleQuerySegment", false);
/*     */ 
/* 237 */     if (processAsSingleQuerySegment == true)
/*     */     {
/* 239 */       invokeProcessActions(queryValueModifierClassName, internalProcessValueBuilder, actionsString, value, csConfig, context, companion, binder, args);
/* 240 */       query.insertSegment(21, value);
/* 241 */       ((InternetSearchQueryField)query.m_segments.get(0)).setProcessedValue(operator, internalProcessValueBuilder.toString());
/*     */     }
/*     */     else
/*     */     {
/* 248 */       query = InternetSearchQueryUtils.translateInternetSearchQuery(value.toCharArray(), false, binder, parsedElts, context, csConfig);
/*     */ 
/* 251 */       if (query.m_segments != null)
/*     */       {
/* 253 */         for (int elNo = 0; elNo < query.m_segments.size(); ++elNo)
/*     */         {
/* 255 */           InternetSearchQueryField queryField = (InternetSearchQueryField)query.m_segments.get(elNo);
/*     */ 
/* 257 */           if ((queryField == null) || ((queryField.m_type != 20) && (queryField.m_type != 21)))
/*     */             continue;
/* 259 */           char[] fieldValue = queryField.getValue();
/* 260 */           String fieldValueString = new String(fieldValue);
/* 261 */           String actionsToProcess = actionsString;
/*     */ 
/* 263 */           if ((csConfig.isPrefixOperator(engineName, operator)) && (elNo == query.m_segments.size() - 1))
/*     */           {
/* 265 */             actionsToProcess = escapeCharactersAction;
/*     */           }
/*     */ 
/* 268 */           if ((csConfig.isSuffixOperator(engineName, operator)) && (elNo == 0))
/*     */           {
/* 270 */             actionsToProcess = escapeCharactersAction;
/*     */           }
/*     */ 
/* 273 */           String processedValue = invokeProcessActions(queryValueModifierClassName, internalProcessValueBuilder, actionsToProcess, fieldValueString, csConfig, context, companion, binder, args);
/* 274 */           queryField.setProcessedValue(operator, processedValue);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 280 */     return query;
/*     */   }
/*     */ 
/*     */   public static void appendProcessedSequences(InternetSearchQuery query, IdcAppendable processedValueBuilder, String engineName, String operator, DataBinder binder, ExecutionContext context)
/*     */     throws DataException
/*     */   {
/* 296 */     char[][] textExtras = (char[][])(char[][])context.getCachedObject("SearchQueryTextExtras");
/* 297 */     char[][] originalTextExtras = (char[][])null;
/*     */ 
/* 299 */     if (textExtras == null)
/*     */     {
/* 301 */       textExtras = new char[][] { { ' ' }, { ',' }, { ' ', '-' }, { '-' }, { '*' }, { '*' } };
/*     */     }
/*     */     else
/*     */     {
/* 311 */       originalTextExtras = (char[][])textExtras.clone();
/* 312 */       textExtras[0] = " ".toCharArray();
/* 313 */       textExtras[1] = ",".toCharArray();
/*     */     }
/*     */ 
/* 316 */     context.setCachedObject("SearchQueryTextExtras", textExtras);
/* 317 */     query.appendFullTextQuery(processedValueBuilder, engineName, binder, context, operator, true, true, true);
/*     */ 
/* 319 */     context.setCachedObject("SearchQueryTextExtras", originalTextExtras);
/*     */   }
/*     */ 
/*     */   public static String invokeProcessActions(IdcStringBuilder processedValueBuilder, String actionsString, String value, CommonSearchConfig csConfig, ExecutionContext context, CommonSearchConfigCompanionAdaptor companion, DataBinder binder, String[] args)
/*     */     throws ServiceException
/*     */   {
/* 337 */     String processedValue = value;
/* 338 */     processedValue = invokeProcessActions(null, processedValueBuilder, actionsString, value, csConfig, context, companion, binder, args);
/* 339 */     return processedValue;
/*     */   }
/*     */ 
/*     */   public static String invokeProcessActions(String className, IdcAppendable processedValueBuilder, String actionsString, String value, CommonSearchConfig csConfig, ExecutionContext context, CommonSearchConfigCompanionAdaptor companion, DataBinder binder, String[] args)
/*     */     throws ServiceException
/*     */   {
/* 359 */     String processedValue = value;
/*     */ 
/* 361 */     if ((className == null) || (className.length() == 0))
/*     */     {
/* 363 */       className = "intradoc.search.CommonQueryValueModifier";
/*     */     }
/*     */ 
/* 366 */     if (Report.m_verbose)
/*     */     {
/* 368 */       Report.trace("searchquery", "Invoking process actions on '" + className + "'for value " + value, null);
/*     */     }
/*     */ 
/* 371 */     Class processActionClass = ClassHelperUtils.createClass(className);
/* 372 */     processedValue = (String)ClassHelperUtils.executeStaticMethodConvertToStandardExceptions(processActionClass, "processActions", new Object[] { processedValueBuilder, actionsString, value, csConfig, context, companion, binder, args });
/*     */ 
/* 376 */     return processedValue;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 381 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83264 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.QueryValueModifierUtils
 * JD-Core Version:    0.5.4
 */