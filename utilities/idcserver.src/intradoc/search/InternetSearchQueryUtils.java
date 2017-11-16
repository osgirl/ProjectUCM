/*      */ package intradoc.search;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.shared.CommonSearchConfig;
/*      */ import intradoc.shared.CommonSearchConfigCompanion;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class InternetSearchQueryUtils
/*      */ {
/*      */   public static final int FULL_TEXT_QUERY_BEGIN = 0;
/*      */   public static final int FULL_TEXT_QUERY_END = 1;
/*      */   public static final int FULL_TEXT_QUERY_OPEN_TAG_BEGIN = 2;
/*      */   public static final int FULL_TEXT_QUERY_CLOSE_TAG_END = 3;
/*      */   public static final int TAG_TYPE = 0;
/*      */   public static final int QUERY_BEGIN = 1;
/*      */   public static final int QUERY_END = 2;
/*      */   public static final int OPEN_TAG_BEGIN = 3;
/*      */   public static final int CLOSE_TAG_END = 4;
/*      */   public static final int FTX = 1;
/*      */   public static final int QSCH = 2;
/*      */   public static final int ISCH = 4;
/*      */   public static final int USCH = 8;
/*   54 */   public static final int[] TAGS = { 1, 2, 4, 8 };
/*   55 */   public static final char[][][] TAG_IDS = { { { '<', 'F', 'T', 'X', '>' }, { '<', '/', 'F', 'T', 'X', '>' }, { '<', 'f', 't', 'x', '>' }, { '<', '/', 'f', 't', 'x', '>' } }, { { '<', 'Q', 'S', 'C', 'H', '>' }, { '<', '/', 'Q', 'S', 'C', 'H', '>' }, { '<', 'q', 's', 'c', 'h', '>' }, { '<', '/', 'q', 's', 'c', 'h', '>' } }, { { '<', 'I', 'S', 'C', 'H', '>' }, { '<', '/', 'I', 'S', 'C', 'H', '>' }, { '<', 'i', 's', 'c', 'h', '>' }, { '<', '/', 'i', 's', 'c', 'h', '>' } }, { { '<', 'U', 'S', 'C', 'H', '>' }, { '<', '/', 'U', 'S', 'C', 'H', '>' }, { '<', 'u', 's', 'c', 'h', '>' }, { '<', '/', 'u', 's', 'c', 'h', '>' } } };
/*      */   public static final char ISCH_FIELD_SEP_OR = '|';
/*      */   public static final char ISCH_FIELD_SEP_AND = '&';
/*   85 */   protected static HashMap m_warnableOperators = null;
/*      */ 
/*      */   public static int[] findNextFullTextQuery(char[] queryChars, int lastIndex) throws ServiceException
/*      */   {
/*   89 */     int[] tmp = findNextTaggedQuery(queryChars, lastIndex, 1, true);
/*   90 */     int[] returnCode = null;
/*   91 */     if (tmp != null)
/*      */     {
/*   93 */       returnCode = new int[4];
/*   94 */       returnCode[0] = tmp[1];
/*   95 */       returnCode[1] = tmp[2];
/*   96 */       returnCode[2] = tmp[3];
/*   97 */       returnCode[3] = tmp[4];
/*      */     }
/*   99 */     return returnCode;
/*      */   }
/*      */ 
/*      */   public static int[] findNextTaggedQuery(char[] queryChars, int lastIndex, int searchedTags, boolean allowScan) throws ServiceException
/*      */   {
/*  104 */     int[] returnCode = null;
/*  105 */     boolean foundIt = false;
/*  106 */     int tagType = -1;
/*  107 */     int end = queryChars.length - 5;
/*  108 */     if ((!allowScan) && (end > lastIndex))
/*      */     {
/*  110 */       end = lastIndex + 1;
/*      */     }
/*  112 */     for (int i = lastIndex; i < end; ++i)
/*      */     {
/*  114 */       int j = -1;
/*  115 */       if (queryChars[i] == '<')
/*      */       {
/*  117 */         for (int k = 0; k < TAGS.length; ++k)
/*      */         {
/*  119 */           if (((searchedTags & TAGS[k]) == 0) || (i + TAG_IDS[k][0].length + TAG_IDS[k][1].length >= queryChars.length))
/*      */             continue;
/*  121 */           boolean checked = false;
/*  122 */           int index = 0;
/*  123 */           while (index < TAG_IDS[k][0].length)
/*      */           {
/*  125 */             if ((queryChars[(i + index)] != TAG_IDS[k][0][index]) && (queryChars[(i + index)] != TAG_IDS[k][2][index]))
/*      */             {
/*  127 */               checked = true;
/*  128 */               break;
/*      */             }
/*  130 */             ++index;
/*      */           }
/*  132 */           if (!checked)
/*      */           {
/*  134 */             for (j = i + TAG_IDS[k][0].length; j <= queryChars.length - TAG_IDS[k][1].length; ++j)
/*      */             {
/*  136 */               index = 0;
/*  137 */               boolean match = true;
/*  138 */               while (index < TAG_IDS[k][0].length)
/*      */               {
/*  140 */                 if ((queryChars[(j + index)] != TAG_IDS[k][1][index]) && (queryChars[(j + index)] != TAG_IDS[k][3][index]))
/*      */                 {
/*  142 */                   match = false;
/*  143 */                   break;
/*      */                 }
/*  145 */                 ++index;
/*      */               }
/*  147 */               if (!match)
/*      */                 continue;
/*  149 */               tagType = k;
/*  150 */               foundIt = true;
/*  151 */               break;
/*      */             }
/*      */           }
/*      */ 
/*  155 */           if (foundIt) {
/*      */             break;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  162 */       if (!foundIt)
/*      */         continue;
/*  164 */       returnCode = new int[5];
/*  165 */       returnCode[0] = tagType;
/*  166 */       returnCode[1] = (i + TAG_IDS[tagType][0].length);
/*  167 */       returnCode[2] = (j - 1);
/*  168 */       returnCode[3] = i;
/*  169 */       returnCode[4] = (j + TAG_IDS[tagType][2].length);
/*  170 */       break;
/*      */     }
/*      */ 
/*  173 */     return returnCode;
/*      */   }
/*      */ 
/*      */   public static void parseQuickSearch(IdcAppendable appendable, char[] quickSearch, String queryDefLabel, DataBinder binder, ParsedQueryElements parsedElts, ExecutionContext cxt, CommonSearchConfig cfg)
/*      */     throws DataException, ServiceException
/*      */   {
/*  179 */     Object[] specialItems = extractSpecialItemsInQuickSearch(quickSearch);
/*      */ 
/*  181 */     String engineName = cfg.getCurrentEngineName();
/*  182 */     String explicitClauseOperator = cfg.getEngineValue(engineName, "QuickSearchExplicitClauseOperator");
/*      */ 
/*  184 */     String quickSearchFields = cfg.getEngineValue(engineName, "QuickSearchFields");
/*  185 */     String quickSearchOperator = cfg.getEngineValue(engineName, "QuickSearchOperators");
/*  186 */     for (int i = 0; i < specialItems.length; ++i)
/*      */     {
/*  188 */       if (specialItems[i] instanceof String)
/*      */       {
/*  190 */         if (parsedElts != null)
/*      */         {
/*  192 */           parsedElts.m_rawParsedElements.add("(");
/*      */         }
/*  194 */         appendable.append(" (");
/*  195 */         parseInternetSearch(appendable, quickSearchFields, quickSearchOperator, (String)specialItems[i], queryDefLabel, binder, parsedElts, cxt, cfg);
/*      */ 
/*  197 */         if (parsedElts != null)
/*      */         {
/*  199 */           parsedElts.m_rawParsedElements.add(")");
/*      */         }
/*  201 */         appendable.append(')');
/*      */       }
/*      */       else
/*      */       {
/*  205 */         String[] item = (String[])(String[])specialItems[i];
/*  206 */         String operator = item[2];
/*  207 */         if (operator == null)
/*      */         {
/*  209 */           operator = explicitClauseOperator;
/*      */         }
/*  211 */         if (parsedElts != null)
/*      */         {
/*  213 */           int operatorCode = SearchQueryUtils.convertToOperatorConstant(operator);
/*  214 */           QueryElement qEl = SearchQueryUtils.createQueryElement(item[0], operatorCode, item[1], null, binder, cfg, cxt);
/*  215 */           parsedElts.m_rawParsedElements.add(qEl);
/*      */         }
/*  217 */         appendable.append(' ');
/*  218 */         cfg.appendClauseElement(appendable, operator, cfg.getCurrentEngineName(), item[0], item[1]);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void setSearchProvider(String sites, DataBinder binder)
/*      */   {
/*  225 */     if (sites == null)
/*      */     {
/*  227 */       return;
/*      */     }
/*      */ 
/*  230 */     String providers = convertToProviders(sites);
/*  231 */     if ((providers == null) || (providers.length() == 0))
/*      */       return;
/*  233 */     binder.putLocal("SearchProviders", providers);
/*      */   }
/*      */ 
/*      */   protected static String convertToProviders(String tmpList)
/*      */   {
/*  239 */     Vector alias = StringUtils.parseArray(tmpList, ',', ',');
/*      */ 
/*  241 */     int size = alias.size();
/*  242 */     HashMap existingProvider = new HashMap();
/*  243 */     StringBuffer buf = new StringBuffer();
/*  244 */     boolean isFirst = true;
/*  245 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  247 */       String name = (String)alias.elementAt(i);
/*  248 */       String tmp = findProperProviderName(name);
/*  249 */       if (tmp != null)
/*      */       {
/*  251 */         name = tmp;
/*      */       }
/*  253 */       if (existingProvider.get(name) != null)
/*      */         continue;
/*  255 */       if (!isFirst)
/*      */       {
/*  257 */         buf.append(',');
/*      */       }
/*  259 */       buf.append(name);
/*      */     }
/*      */ 
/*  262 */     return buf.toString();
/*      */   }
/*      */ 
/*      */   protected static String findProperProviderName(String name)
/*      */   {
/*  267 */     HashMap siteMap = (HashMap)SharedObjects.getObject("globalObjects", "QuickSearchSiteMap");
/*  268 */     String properName = (String)siteMap.get(name);
/*  269 */     if (properName == null)
/*      */     {
/*  271 */       properName = name;
/*      */     }
/*  273 */     return properName;
/*      */   }
/*      */ 
/*      */   protected static Object[] extractSpecialItemsInQuickSearch(char[] quickSearch)
/*      */   {
/*  278 */     ArrayList alist = new ArrayList();
/*  279 */     int lastWSIndex = -1;
/*  280 */     int size = quickSearch.length;
/*  281 */     int lastCheckPoint = 0;
/*  282 */     boolean isPrevSite = false;
/*  283 */     boolean containsWord = false;
/*  284 */     boolean allowAddIQuery = false;
/*  285 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  287 */       char c = quickSearch[i];
/*  288 */       if (Character.isWhitespace(c))
/*      */       {
/*  290 */         lastWSIndex = i;
/*  291 */         if (containsWord)
/*      */         {
/*  293 */           allowAddIQuery = true;
/*  294 */           containsWord = false;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  299 */         containsWord = true;
/*      */       }
/*  301 */       int curIndex = i;
/*  302 */       if (c != ':')
/*      */         continue;
/*  304 */       String field = new String(quickSearch, lastWSIndex + 1, i - lastWSIndex - 1);
/*  305 */       if (!isLegitSpecialPrefix(field))
/*      */         continue;
/*  307 */       while ((i < size) && (!Character.isWhitespace(quickSearch[i])) && (quickSearch[i] != ','))
/*      */       {
/*  309 */         ++i;
/*      */       }
/*  311 */       if (!isPrevSite)
/*      */       {
/*  313 */         appendConjunctionOrIQuery(alist, quickSearch, lastWSIndex, lastCheckPoint, allowAddIQuery, true, true);
/*      */       }
/*      */       else
/*      */       {
/*  317 */         isPrevSite = false;
/*      */       }
/*      */ 
/*  320 */       if (quickSearch[(lastWSIndex + 1)] == '-')
/*      */       {
/*  322 */         alist.add(new String[] { null, null, "not" });
/*  323 */         field = field.substring(1);
/*      */       }
/*      */ 
/*  326 */       if (i > curIndex + 1)
/*      */       {
/*  328 */         String value = new String(quickSearch, curIndex + 1, i - curIndex - 1);
/*  329 */         String[] item = { field, value, null };
/*  330 */         if (field.equalsIgnoreCase("site"))
/*      */         {
/*  333 */           isPrevSite = true;
/*      */         }
/*      */         else
/*      */         {
/*  337 */           alist.add(item);
/*      */         }
/*      */       }
/*  340 */       lastWSIndex = i;
/*  341 */       lastCheckPoint = i;
/*      */ 
/*  343 */       allowAddIQuery = false;
/*  344 */       containsWord = false;
/*      */     }
/*      */ 
/*  348 */     if (lastCheckPoint < size)
/*      */     {
/*  350 */       boolean allowPreceding = false;
/*  351 */       if ((!isPrevSite) && (lastCheckPoint != 0))
/*      */       {
/*  353 */         allowPreceding = true;
/*      */       }
/*  355 */       appendConjunctionOrIQuery(alist, quickSearch, size - 1, lastCheckPoint, true, allowPreceding, false);
/*      */     }
/*      */ 
/*  359 */     return alist.toArray();
/*      */   }
/*      */ 
/*      */   protected static void appendConjunctionOrIQuery(ArrayList alist, char[] quickSearch, int lastWSIndex, int lastCheckPoint, boolean allowAddIQuery, boolean allowAddPrecedingConj, boolean allowAddTrailingConj)
/*      */   {
/*  366 */     String[] AND = { null, null, "and" };
/*  367 */     String[] OR = { null, null, "or" };
/*  368 */     if ((lastWSIndex >= lastCheckPoint) && (allowAddIQuery))
/*      */     {
/*  370 */       String[] conj = null;
/*  371 */       String iQuery = null;
/*  372 */       if (allowAddPrecedingConj)
/*      */       {
/*  374 */         conj = AND;
/*  375 */         if ((lastCheckPoint > 0) && (quickSearch[lastCheckPoint] == ','))
/*      */         {
/*  377 */           conj = OR;
/*      */         }
/*  379 */         int[] result = startsWithWord(quickSearch, lastCheckPoint, lastWSIndex, new char[][] { "and".toCharArray(), "or".toCharArray() });
/*  380 */         if (result[0] == 0)
/*      */         {
/*  382 */           conj = AND;
/*      */         }
/*  384 */         else if (result[0] == 1)
/*      */         {
/*  386 */           conj = OR;
/*      */         }
/*  388 */         if (result[1] > lastWSIndex)
/*      */         {
/*  390 */           allowAddTrailingConj = false;
/*  391 */           conj = null;
/*      */         }
/*      */         else
/*      */         {
/*  395 */           iQuery = new String(quickSearch, result[1], lastWSIndex + 1 - result[1]);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  400 */         iQuery = new String(quickSearch, lastCheckPoint, lastWSIndex + 1 - lastCheckPoint);
/*      */       }
/*  402 */       if (conj != null)
/*      */       {
/*  404 */         alist.add(conj);
/*      */       }
/*  406 */       if (iQuery != null)
/*      */       {
/*  409 */         alist.add(iQuery);
/*      */       }
/*      */     }
/*  412 */     if (!allowAddTrailingConj)
/*      */       return;
/*  414 */     for (int j = lastWSIndex; j >= 0; --j)
/*      */     {
/*  416 */       if (Character.isWhitespace(quickSearch[j])) {
/*      */         continue;
/*      */       }
/*      */ 
/*  420 */       if (quickSearch[j] == ',')
/*      */       {
/*  423 */         if (alist.size() > 0)
/*      */         {
/*  425 */           Object obj = alist.get(alist.size() - 1);
/*  426 */           if (obj instanceof String)
/*      */           {
/*  428 */             String tmp = new String(quickSearch, lastCheckPoint, j - lastCheckPoint);
/*      */ 
/*  430 */             alist.set(alist.size() - 1, tmp);
/*      */           }
/*      */         }
/*  433 */         alist.add(OR); return;
/*      */       }
/*      */ 
/*  437 */       alist.add(AND);
/*      */ 
/*  439 */       return;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static int[] startsWithWord(char[] quickSearch, int begin, int end, char[][] word)
/*      */   {
/*  455 */     int last = end;
/*  456 */     boolean match = false;
/*  457 */     int[] result = { -1, end + 1 };
/*  458 */     for (int i = begin; i <= last; ++i)
/*      */     {
/*  460 */       if (Character.isWhitespace(quickSearch[i])) {
/*      */         continue;
/*      */       }
/*      */ 
/*  464 */       result[1] = i;
/*  465 */       for (int j = 0; j < word.length; ++j)
/*      */       {
/*  467 */         match = true;
/*  468 */         for (int k = 0; k < word[j].length; ++k)
/*      */         {
/*  470 */           if (Character.toLowerCase(quickSearch[(i + k)]) == word[j][k])
/*      */             continue;
/*  472 */           match = false;
/*  473 */           break;
/*      */         }
/*      */ 
/*  476 */         if (!match)
/*      */           continue;
/*  478 */         result[0] = j;
/*  479 */         break;
/*      */       }
/*      */ 
/*  482 */       if ((match) && (i < last) && (Character.isWhitespace(quickSearch[(i + 1)])))
/*      */       {
/*  485 */         result[0] = -1;
/*  486 */         match = false; break;
/*      */       }
/*  488 */       if (!match) {
/*      */         break;
/*      */       }
/*  491 */       result[1] = (end + 1);
/*  492 */       for (int j = i + word[result[0]].length; j <= end; ++j)
/*      */       {
/*  494 */         if (Character.isWhitespace(quickSearch[j]))
/*      */           continue;
/*  496 */         result[1] = j;
/*  497 */         break;
/*      */       }
/*  492 */       break;
/*      */     }
/*      */ 
/*  503 */     return result;
/*      */   }
/*      */ 
/*      */   protected static boolean isLegitSpecialPrefix(String field)
/*      */   {
/*  509 */     return true;
/*      */   }
/*      */ 
/*      */   public static void parseISearch(IdcAppendable appendable, char[] iSearch, String queryDefLabel, DataBinder binder, ParsedQueryElements parsedElts, ExecutionContext cxt, CommonSearchConfig cfg)
/*      */     throws DataException, ServiceException
/*      */   {
/*  515 */     int[] specIndex = new int[4];
/*  516 */     char[] specChars = { ';', ';', '`', '`' };
/*  517 */     int index = 0;
/*  518 */     int i = 0;
/*  519 */     for (; i < iSearch.length; ++i)
/*      */     {
/*  521 */       if (iSearch[i] != specChars[index])
/*      */         continue;
/*  523 */       specIndex[index] = i;
/*  524 */       ++index;
/*      */ 
/*  526 */       if (index >= specChars.length)
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  533 */     if (index != specChars.length)
/*      */     {
/*  536 */       String msg = LocaleUtils.encodeMessage("csSearchUnableParseSearchQuery", null, new String(iSearch));
/*  537 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  540 */     String fields = new String(iSearch, 0, specIndex[0]).trim();
/*  541 */     String operator = new String(iSearch, specIndex[0] + 1, specIndex[1] - specIndex[0] - 1).trim();
/*  542 */     String values = new String(iSearch, specIndex[2] + 1, specIndex[3] - specIndex[2] - 1).trim();
/*      */ 
/*  544 */     parseInternetSearch(appendable, fields, operator, values, queryDefLabel, binder, parsedElts, cxt, cfg);
/*      */   }
/*      */ 
/*      */   public static void parseInternetSearch(IdcAppendable appendable, String fields, String ops, String values, String queryDefLabel, DataBinder binder, ParsedQueryElements parsedElts, ExecutionContext cxt, CommonSearchConfig cfg)
/*      */     throws ServiceException, DataException
/*      */   {
/*  551 */     if ((values == null) || (values.length() == 0))
/*      */     {
/*  553 */       return;
/*      */     }
/*      */ 
/*  556 */     Vector opList = StringUtils.parseArray(ops, ',', ',');
/*  557 */     InternetSearchQuery query = translateInternetSearchQuery(values.toCharArray(), false, binder, parsedElts, cxt, cfg);
/*      */ 
/*  559 */     boolean escapeReservedStrings = DataBinderUtils.getBoolean(binder, "escapeReservedStringsInSearch", false);
/*  560 */     String supportReservedExpProcessingString = cfg.getEngineValue(queryDefLabel, "SupportReservedExpProcessing");
/*  561 */     boolean supportReservedExpProcessing = StringUtils.convertToBool(supportReservedExpProcessingString, false);
/*  562 */     List reservedOperators = null;
/*      */ 
/*  564 */     if ((supportReservedExpProcessing == true) && (escapeReservedStrings == true))
/*      */     {
/*  566 */       reservedOperators = QueryValueModifierUtils.getReservedOperatorList(queryDefLabel, opList, cfg);
/*      */     }
/*      */ 
/*  569 */     if (query.isEmpty())
/*      */       return;
/*  571 */     if (opList.contains("fullText"))
/*      */     {
/*  573 */       binder.putLocal("hasFullTextTerm", "1");
/*      */     }
/*  575 */     char[] fieldChars = fields.toCharArray();
/*      */ 
/*  577 */     String op = null;
/*  578 */     int lastOpIndex = 0;
/*      */ 
/*  580 */     int lastIndex = 0;
/*  581 */     for (int i = 0; i < fieldChars.length; ++i)
/*      */     {
/*  583 */       int opCode = -1;
/*  584 */       if (fieldChars[i] == '|')
/*      */       {
/*  586 */         opCode = 17;
/*      */       } else {
/*  588 */         if (fieldChars[i] != '&')
/*      */           continue;
/*  590 */         opCode = 16;
/*      */       }
/*      */ 
/*  596 */       String field = fields.substring(lastIndex, i);
/*  597 */       if (lastOpIndex < opList.size())
/*      */       {
/*  599 */         op = (String)opList.elementAt(lastOpIndex);
/*  600 */         ++lastOpIndex;
/*      */       }
/*      */ 
/*  603 */       boolean useprocessedValue = false;
/*  604 */       if ((reservedOperators != null) && (reservedOperators.contains(op)))
/*      */       {
/*  606 */         Report.trace("searchquery", "Processing callback on query value for operator '" + op + "'", null);
/*  607 */         setProcessedSegmentsForOperator(appendable, query, op, fields, cfg, binder, cxt, queryDefLabel);
/*  608 */         useprocessedValue = true;
/*      */       }
/*      */ 
/*  611 */       addQuerySegment(appendable, field, op, values, query, queryDefLabel, cfg, binder, cxt, useprocessedValue);
/*      */ 
/*  613 */       String operator = (opCode == 17) ? "or" : "and";
/*      */ 
/*  616 */       if (query.m_parsedElements != null)
/*      */       {
/*  618 */         query.m_parsedElements.m_rawParsedElements.add(new Integer(opCode));
/*      */       }
/*  620 */       cfg.appendClauseElement(appendable, operator, queryDefLabel, null, null);
/*  621 */       appendable.append(" ");
/*      */ 
/*  623 */       lastIndex = i + 1;
/*      */     }
/*      */ 
/*  627 */     if (lastOpIndex < opList.size())
/*      */     {
/*  629 */       op = (String)opList.elementAt(lastOpIndex);
/*  630 */       ++lastOpIndex;
/*      */     }
/*  632 */     String field = fields.substring(lastIndex);
/*      */ 
/*  634 */     boolean useprocessedValue = false;
/*  635 */     if ((reservedOperators != null) && (reservedOperators.contains(op)))
/*      */     {
/*  637 */       Report.trace("searchquery", "Processing callback on query value for operator '" + op + "'", null);
/*  638 */       setProcessedSegmentsForOperator(appendable, query, op, fields, cfg, binder, cxt, queryDefLabel);
/*  639 */       useprocessedValue = true;
/*      */     }
/*      */     else
/*      */     {
/*  643 */       useprocessedValue = false;
/*      */     }
/*      */ 
/*  646 */     addQuerySegment(appendable, field, op, values, query, queryDefLabel, cfg, binder, cxt, useprocessedValue);
/*      */   }
/*      */ 
/*      */   public static void setProcessedSegmentsForOperator(IdcAppendable appendable, InternetSearchQuery query, String operator, String fields, CommonSearchConfig cfg, DataBinder binder, ExecutionContext cxt, String queryDefLabel)
/*      */     throws ServiceException, DataException
/*      */   {
/*  672 */     binder.putLocal("processAsSingleQuerySegment", "true");
/*      */ 
/*  674 */     if (query.m_segments != null)
/*      */     {
/*  676 */       for (int elNo = 0; elNo < query.m_segments.size(); ++elNo)
/*      */       {
/*  678 */         InternetSearchQueryField queryField = (InternetSearchQueryField)query.m_segments.get(elNo);
/*  679 */         if ((queryField == null) || ((queryField.m_type != 20) && (queryField.m_type != 21))) {
/*      */           continue;
/*      */         }
/*  682 */         char[] fieldValue = queryField.getValue();
/*  683 */         String fieldValueString = new String(fieldValue);
/*      */ 
/*  685 */         int callbackReturn = processQueryParserCallback(null, cfg, (IdcStringBuilder)appendable, binder, cxt, operator, fields, fieldValueString, null, null);
/*      */ 
/*  688 */         if (callbackReturn != 1)
/*      */           continue;
/*  690 */         Object[] callbackResult = (Object[])(Object[])cxt.getCachedObject("queryParserCallbackFilter");
/*  691 */         String processedValue = (String)callbackResult[4];
/*      */ 
/*  693 */         queryField.setProcessedValue(operator, processedValue);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  699 */     binder.removeLocal("processAsSingleQuerySegment");
/*      */   }
/*      */ 
/*      */   public static int processQueryParserCallback(QueryParserCallback parserCallback, CommonSearchConfig cfg, IdcStringBuilder appendable, DataBinder binder, ExecutionContext cxt, String operator, String fieldName, String value, String lastOperatorKey, String lastConjunctionKey)
/*      */     throws ServiceException, DataException
/*      */   {
/*  724 */     if (parserCallback == null)
/*      */     {
/*  726 */       CommonSearchConfigCompanion commonSearchCfgComp = cfg.getCompanion();
/*  727 */       parserCallback = (QueryParserCallback)commonSearchCfgComp;
/*      */     }
/*      */ 
/*  730 */     if (parserCallback == null)
/*      */     {
/*  732 */       return 1;
/*      */     }
/*      */ 
/*  735 */     int callbackReturn = 1;
/*  736 */     IdcStringBuilder segment = new IdcStringBuilder();
/*  737 */     Object[] callBackObject = { segment, appendable, operator, fieldName, value };
/*  738 */     cxt.setCachedObject("queryParserCallbackFilter", callBackObject);
/*      */ 
/*  740 */     if (Report.m_verbose)
/*      */     {
/*  742 */       Report.trace("searchquery", "Processing callback on query value '" + value + "'", null);
/*      */     }
/*      */ 
/*  745 */     callbackReturn = parserCallback.doCallback(appendable, binder, cxt, new String[] { operator, fieldName, value, lastOperatorKey, lastConjunctionKey });
/*  746 */     if (callbackReturn == 1)
/*      */     {
/*  748 */       callBackObject = (Object[])(Object[])cxt.getCachedObject("queryParserCallbackFilter");
/*      */     }
/*      */ 
/*  751 */     return callbackReturn;
/*      */   }
/*      */ 
/*      */   protected static void addQuerySegment(IdcAppendable appendable, String field, String op, String values, InternetSearchQuery query, String queryDefLabel, CommonSearchConfig cfg, DataBinder binder, ExecutionContext cxt, boolean useProcessedValue)
/*      */     throws DataException, ServiceException
/*      */   {
/*  758 */     appendable.append('(');
/*  759 */     if (query.m_parsedElements != null)
/*      */     {
/*  761 */       query.m_parsedElements.m_rawParsedElements.add("(");
/*      */     }
/*  763 */     if ((query.containsAnd()) && (isEmptyResultWarnableOperator(op, cfg) == true))
/*      */     {
/*  765 */       String curWarningValue = '[' + values + ']';
/*  766 */       String warningValue = binder.getLocal("EmptyResultWarningValues");
/*  767 */       if (warningValue == null)
/*      */       {
/*  769 */         warningValue = curWarningValue;
/*  770 */         binder.putLocal("EmptyResultWarningExample", "\"" + values + "\"");
/*      */       }
/*  773 */       else if (warningValue.indexOf(curWarningValue) < 0)
/*      */       {
/*  775 */         warningValue = warningValue + " or " + curWarningValue;
/*      */       }
/*  777 */       binder.putLocal("EmptyResultWarningValues", warningValue);
/*      */     }
/*  779 */     if (op.equalsIgnoreCase("fulltext"))
/*      */     {
/*  781 */       if (query.m_parsedElements != null)
/*      */       {
/*  783 */         QueryElement qElt = SearchQueryUtils.createQueryElement("FullTextSearch", 5, values, null, binder, cfg, cxt);
/*      */ 
/*  785 */         query.m_parsedElements.m_rawParsedElements.add(qElt);
/*      */       }
/*  787 */       query.appendFullTextQuery(appendable, queryDefLabel, binder, cxt, useProcessedValue);
/*      */     }
/*      */     else
/*      */     {
/*  791 */       query.appendQuery(appendable, field, op, queryDefLabel, useProcessedValue);
/*      */     }
/*  793 */     appendable.append(") ");
/*  794 */     if (query.m_parsedElements == null)
/*      */       return;
/*  796 */     query.m_parsedElements.m_rawParsedElements.add(")");
/*      */   }
/*      */ 
/*      */   public static boolean isEmptyResultWarnableOperator(String op, CommonSearchConfig cfg)
/*      */   {
/*  802 */     if ((op == null) || (op.length() == 0))
/*      */     {
/*  804 */       return false;
/*      */     }
/*  806 */     if (m_warnableOperators == null)
/*      */     {
/*  808 */       initializeWarnableOperators(cfg);
/*      */     }
/*  810 */     return m_warnableOperators.get(op.toLowerCase()) != null;
/*      */   }
/*      */ 
/*      */   public static synchronized void initializeWarnableOperators(CommonSearchConfig cfg)
/*      */   {
/*  815 */     m_warnableOperators = new HashMap();
/*  816 */     String skippedOps = cfg.getEngineValue("InternetSearchWarnableOperators");
/*  817 */     Vector ops = StringUtils.parseArray(skippedOps, ',', ',');
/*  818 */     int size = ops.size();
/*  819 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  821 */       String op = (String)ops.elementAt(i);
/*  822 */       m_warnableOperators.put(op.toLowerCase(), "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static InternetSearchQuery translateInternetSearchQuery(char[] queryChars, boolean isFullTextOnly, DataBinder binder, ParsedQueryElements parsedElts, ExecutionContext context, CommonSearchConfig csc)
/*      */     throws ServiceException
/*      */   {
/*  829 */     InternetSearchQuery query = new InternetSearchQuery();
/*  830 */     query.init(csc, parsedElts, queryChars, binder, context);
/*  831 */     boolean isError = false;
/*  832 */     String msg = "";
/*      */ 
/*  834 */     boolean allowConjunction = false;
/*  835 */     boolean isOpenQuote = false;
/*  836 */     int numOpenParen = 0;
/*  837 */     boolean canBeginWord = true;
/*  838 */     boolean isValue = false;
/*  839 */     boolean hasPrevTerm = false;
/*      */ 
/*  841 */     int skipCount = 0;
/*      */ 
/*  843 */     for (int i = 0; i < queryChars.length; ++i)
/*      */     {
/*  845 */       boolean isChangeAllowConjunction = false;
/*  846 */       isValue = false;
/*  847 */       if (allowConjunction)
/*      */       {
/*  849 */         hasPrevTerm = false;
/*      */       }
/*      */ 
/*  852 */       boolean isNotOp = false;
/*      */       int j;
/*  853 */       switch (queryChars[i])
/*      */       {
/*  860 */       case '\t':
/*      */       case '\n':
/*      */       case '\r':
/*      */       case ' ':
/*      */       case '　':
/*  860 */         if (!isOpenQuote) if (allowConjunction)
/*      */           {
/*  864 */             skipCount = 0;
/*  865 */             boolean addAnd = true;
/*  866 */             for (int j = i + 1; j < queryChars.length; ++j)
/*      */             {
/*  868 */               if ((queryChars[j] == ' ') || (queryChars[j] == '\t') || (queryChars[j] == '\n') || (queryChars[j] == '\r') || (queryChars[j] == '　'))
/*      */               {
/*  872 */                 ++skipCount;
/*      */               }
/*      */               else {
/*  875 */                 if ((queryChars[j] != ',') && (queryChars[j] != ')') && ((((queryChars[j] != 'O') && (queryChars[j] != 'o')) || (j >= queryChars.length - 2) || ((queryChars[(j + 1)] != 'R') && (queryChars[(j + 1)] != 'r')) || ((j <= queryChars.length - 3) && (Character.isLetterOrDigit(queryChars[(j + 2)]))))))
/*      */                 {
/*      */                   break;
/*      */                 }
/*      */ 
/*  881 */                 addAnd = false; break;
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*  886 */             if (skipCount + i >= queryChars.length - 1)
/*      */             {
/*  888 */               skipCount = 0;
/*  889 */               addAnd = false;
/*      */             }
/*      */ 
/*  892 */             if (addAnd)
/*      */             {
/*  894 */               query.insertSegment(0, null);
/*      */ 
/*  896 */               allowConjunction = false;
/*      */             }
/*      */           } break;
/*      */       case ',':
/*  902 */         if (!isOpenQuote)
/*      */         {
/*  906 */           if (!allowConjunction)
/*      */           {
/*  908 */             skipCount = 0;
/*      */           }
/*      */           else
/*      */           {
/*  912 */             for (int j = i + 1; (j < queryChars.length) && ((
/*  914 */               (queryChars[j] == ' ') || (queryChars[j] == '\t') || (queryChars[j] == '\n') || (queryChars[j] == '\r') || (queryChars[j] == ',') || (queryChars[j] == '　'))); ++j)
/*      */             {
/*  918 */               ++skipCount;
/*      */             }
/*      */ 
/*  923 */             query.insertSegment(1, null);
/*  924 */             allowConjunction = false;
/*      */           }
/*      */         }
/*  926 */         break;
/*      */       case '"':
/*  931 */         j = i;
/*  932 */         boolean isSkipNext = true;
/*      */         do
/*      */         {
/*  935 */           if (isSkipNext)
/*      */           {
/*  937 */             isSkipNext = false;
/*      */           }
/*  940 */           else if ((j < queryChars.length - 1) && (queryChars[j] == '"') && (queryChars[(j + 1)] == '"'))
/*      */           {
/*  942 */             isSkipNext = true;
/*      */           }
/*      */           else {
/*  945 */             if (queryChars[j] == '"')
/*      */               break;
/*      */           }
/*      */         }
/*  949 */         while (++j < queryChars.length);
/*      */ 
/*  951 */         skipCount = j - i;
/*  952 */         query.insertSegment(10, "(");
/*  953 */         query.insertSegment(21, queryChars, i + 1, skipCount - 1);
/*  954 */         query.insertSegment(11, ")");
/*  955 */         isChangeAllowConjunction = true;
/*  956 */         break;
/*      */       case '(':
/*  958 */         if (!isOpenQuote)
/*      */         {
/*  960 */           ++numOpenParen;
/*  961 */           if (allowConjunction)
/*      */           {
/*  963 */             query.insertSegment(0, null);
/*  964 */             allowConjunction = false;
/*      */           }
/*  966 */           query.insertSegment(10, "("); } break;
/*      */       case ')':
/*  971 */         if (!isOpenQuote)
/*      */         {
/*  973 */           if (!allowConjunction)
/*      */           {
/*  976 */             isError = true;
/*  977 */             msg = LocaleResources.getString("csSearchQueryParserErrorOnCloseParenthsis", context, "" + i, new String(queryChars));
/*      */           }
/*  979 */           --numOpenParen;
/*  980 */           allowConjunction = true;
/*  981 */           query.insertSegment(11, ")"); } break;
/*      */       case '-':
/*  985 */         if ((!isOpenQuote) && ((((i < queryChars.length - 1) && (((queryChars[(i + 1)] == '"') || (Character.isLetterOrDigit(queryChars[(i + 1)])))) && (((i == 0) || (!Character.isLetterOrDigit(queryChars[(i - 1)]))))) || (queryChars[(i + 1)] == '('))))
/*      */         {
/*  989 */           isNotOp = true;
/*  990 */           skipCount = 0;
/*      */         }
/*      */       case 'N':
/*      */       case 'n':
/*  998 */         if ((canBeginWord) && (!isOpenQuote) && (!isNotOp) && ((
/* 1000 */           ((queryChars.length > i + 3) && (!Character.isLetter(queryChars[(i + 3)]))) || ((queryChars.length == i + 3) && (((queryChars[(i + 1)] == 'O') || (queryChars[(i + 1)] == 'o'))) && (((queryChars[(i + 2)] == 'T') || (queryChars[(i + 2)] == 't')))))))
/*      */         {
/* 1007 */           isNotOp = true;
/* 1008 */           skipCount = 2;
/*      */         }
/*      */ 
/* 1012 */         if (isNotOp)
/*      */         {
/* 1014 */           query.insertSegment(2, null);
/* 1015 */           allowConjunction = false;
/*      */ 
/* 1017 */           if ((!isFullTextOnly) || (!hasPrevTerm))
/*      */             break label1282;
/* 1019 */           query.insertSegment(3, null);
/* 1020 */           hasPrevTerm = false; } break;
/*      */       case 'A':
/*      */       case 'a':
/* 1026 */         if ((canBeginWord) && (!isOpenQuote) && ((
/* 1028 */           ((queryChars.length > i + 3) && (!Character.isLetter(queryChars[(i + 3)]))) || ((queryChars.length == i + 3) && (((queryChars[(i + 1)] == 'N') || (queryChars[(i + 1)] == 'n'))) && (((queryChars[(i + 2)] == 'D') || (queryChars[(i + 2)] == 'd')))))))
/*      */         {
/* 1035 */           skipCount = 3;
/* 1036 */           if (allowConjunction)
/*      */           {
/* 1039 */             query.insertSegment(0, null);
/*      */           }
/* 1041 */           allowConjunction = false;
/*      */         }
/*      */         else
/*      */         {
/* 1046 */           isValue = true;
/*      */         }
/*      */       case 'O':
/*      */       case 'o':
/* 1049 */         if ((canBeginWord) && (!isOpenQuote) && (!isValue) && ((
/* 1051 */           ((queryChars.length > i + 2) && (!Character.isLetter(queryChars[(i + 2)]))) || ((queryChars.length == i + 2) && (((queryChars[(i + 1)] == 'R') || (queryChars[(i + 1)] == 'r')))))))
/*      */         {
/* 1056 */           skipCount = 1;
/*      */ 
/* 1058 */           if (allowConjunction)
/*      */           {
/* 1060 */             query.insertSegment(1, null);
/*      */           }
/* 1062 */           allowConjunction = false;
/*      */         }
/* 1067 */         else if (!isValue)
/*      */         {
/* 1069 */           isValue = true;
/*      */         }
/*      */       default:
/* 1073 */         j = i;
/*      */         do
/*      */         {
/* 1076 */           if (((numOpenParen > 0) && (queryChars[j] == ')')) || (Character.isWhitespace(queryChars[j]))) break; if (queryChars[j] == ',') {
/*      */             break;
/*      */           }
/*      */         }
/*      */ 
/* 1081 */         while (++j < queryChars.length);
/* 1082 */         String value = new String(queryChars, i, j - i);
/* 1083 */         query.insertSegment(20, value);
/* 1084 */         skipCount = j - i - 1;
/* 1085 */         isChangeAllowConjunction = true;
/*      */       }
/*      */ 
/* 1090 */       if ((!isError) && (numOpenParen < 0))
/*      */       {
/* 1092 */         label1282: isError = true;
/* 1093 */         msg = LocaleResources.getString("csSearchQueryTranslationMismatchParenExtraClose", context, new String(queryChars), new Integer(i));
/* 1094 */         break;
/*      */       }
/*      */ 
/* 1097 */       if ((!allowConjunction) && (isChangeAllowConjunction) && (queryChars[i] != ')'))
/*      */       {
/* 1100 */         allowConjunction = true;
/*      */       }
/*      */ 
/* 1103 */       if (skipCount > 0)
/*      */       {
/* 1105 */         i += skipCount;
/* 1106 */         skipCount = 0;
/*      */       }
/*      */ 
/* 1109 */       if (i >= queryChars.length)
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/* 1114 */       canBeginWord = (!Character.isLetterOrDigit(queryChars[i])) && (!isOpenQuote);
/*      */     }
/*      */ 
/* 1117 */     if ((!isError) && (numOpenParen > 0))
/*      */     {
/* 1119 */       isError = true;
/* 1120 */       msg = LocaleResources.getString("csSearchQueryTranslationMismatchParenExtraOpen", context, new String(queryChars));
/*      */     }
/* 1122 */     if (isError)
/*      */     {
/* 1124 */       throw new ServiceException(msg);
/*      */     }
/* 1126 */     return query;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1131 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95248 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.InternetSearchQueryUtils
 * JD-Core Version:    0.5.4
 */