/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.CommonSearchConfigCompanion;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class CommonSearchConfigCompanionAdaptor
/*     */   implements CommonSearchConfigCompanion, QueryParserCallback, SearchQueryValidator
/*     */ {
/*     */   protected CommonSearchConfig m_config;
/*     */   protected UniversalSearchQueryParser m_parser;
/*     */   protected String m_wildCards;
/*     */   protected char[][] m_textExtras;
/*     */   public static final int TEXT_AND = 0;
/*     */   public static final int TEXT_OR = 1;
/*     */   public static final int TEXT_ANDNOT = 2;
/*     */   public static final int TEXT_NOT = 3;
/*     */   public static final int TEXT_ANY = 4;
/*     */   public static final int TEXT_ANYONE = 5;
/*     */   protected String m_queryDefinitionLabel;
/*     */   protected QueryParserCallback m_parserCallback;
/*     */   protected SearchQueryValidator m_queryValidator;
/*     */   protected Map m_monitoredFields;
/*     */   protected long m_monitoredOperators;
/*     */   protected boolean m_isInited;
/*     */   protected String m_callbackFilterName;
/*     */   protected boolean m_escapeReservedStringsInSearch;
/*     */ 
/*     */   public CommonSearchConfigCompanionAdaptor()
/*     */   {
/*  46 */     this.m_config = null;
/*  47 */     this.m_parser = null;
/*  48 */     this.m_wildCards = "%,_";
/*  49 */     this.m_textExtras = new char[][] { { 'A', 'N', 'D' }, { 'O', 'R' }, { 'A', 'N', 'D', ' ', 'N', 'O', 'T' }, { 'N', 'O', 'T' }, { '*' }, { '*' } };
/*     */ 
/*  65 */     this.m_queryDefinitionLabel = null;
/*  66 */     this.m_parserCallback = null;
/*  67 */     this.m_queryValidator = null;
/*     */ 
/*  69 */     this.m_monitoredFields = new ConcurrentHashMap();
/*  70 */     this.m_monitoredOperators = 0L;
/*  71 */     this.m_isInited = false;
/*     */ 
/*  73 */     this.m_callbackFilterName = "universalQueryParserCallbackFilter";
/*  74 */     this.m_escapeReservedStringsInSearch = false;
/*     */   }
/*     */ 
/*     */   public void init(CommonSearchConfig config)
/*     */     throws ServiceException
/*     */   {
/*  81 */     this.m_config = config;
/*  82 */     this.m_parser = new UniversalSearchQueryParser();
/*  83 */     this.m_parserCallback = this;
/*  84 */     this.m_queryValidator = this;
/*  85 */     if (this.m_isInited)
/*     */       return;
/*  87 */     this.m_monitoredOperators = initCallbackMonitor(this.m_monitoredFields, this.m_monitoredOperators);
/*  88 */     this.m_isInited = true;
/*     */   }
/*     */ 
/*     */   public long initCallbackMonitor(Map monitoredFields, long monitoredOperators)
/*     */     throws ServiceException
/*     */   {
/*  94 */     synchronized (monitoredFields)
/*     */     {
/*     */       try
/*     */       {
/*  98 */         DataResultSet drset = SharedObjects.getTable("UniversalSearchCallbackMonitors");
/*  99 */         String engineName = this.m_config.getCurrentEngineName();
/* 100 */         int lastIndex = 0;
/*     */         do
/*     */         {
/* 103 */           String[][] monitors = ResultSetUtils.createFilteredStringTableEx(drset, new String[] { "uscmEngineName", "uscmMonitorType", "uscmMonitorValue" }, engineName, true, false);
/*     */ 
/* 106 */           for (int i = 0; (monitors != null) && (i < monitors.length); ++i)
/*     */           {
/* 108 */             if (monitors[i][0].equalsIgnoreCase("fieldname"))
/*     */             {
/* 110 */               List fields = StringUtils.makeListFromSequenceSimple(monitors[i][1]);
/* 111 */               for (String field : fields)
/*     */               {
/* 113 */                 monitoredFields.put(field.toLowerCase(), "1");
/*     */               }
/*     */             } else {
/* 116 */               if (!monitors[i][0].equalsIgnoreCase("operator"))
/*     */                 continue;
/* 118 */               List operators = StringUtils.makeListFromSequenceSimple(monitors[i][1]);
/* 119 */               for (String operatorStr : operators)
/*     */               {
/* 121 */                 int index = StringUtils.findStringIndex(UniversalSearchQueryParser.OPERATORKEYS, operatorStr);
/* 122 */                 monitoredOperators |= 1 << index;
/*     */               }
/*     */             }
/*     */           }
/* 126 */           lastIndex = engineName.lastIndexOf(46);
/* 127 */           if (lastIndex <= 0)
/*     */             continue;
/* 129 */           engineName = engineName.substring(0, lastIndex);
/*     */         }
/*     */ 
/* 132 */         while (lastIndex > 0);
/* 133 */         this.m_isInited = true;
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 137 */         throw new ServiceException(e);
/*     */       }
/*     */     }
/* 140 */     return monitoredOperators;
/*     */   }
/*     */ 
/*     */   public int prepareQueryText(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 149 */     if (this.m_queryDefinitionLabel == null)
/*     */     {
/* 151 */       this.m_queryDefinitionLabel = SearchIndexerUtils.getSearchEngineName(ctxt);
/*     */     }
/* 153 */     ctxt.setCachedObject("SearchQueryTextExtras", this.m_textExtras);
/* 154 */     if (this.m_parser != null)
/*     */     {
/* 156 */       this.m_parser.init(this.m_queryDefinitionLabel, this.m_wildCards, this.m_config, this.m_parserCallback, this.m_queryValidator);
/*     */     }
/*     */ 
/* 160 */     int nextIndex = 0;
/* 161 */     String query = binder.getLocal("QueryText");
/* 162 */     char[] queryChars = query.toCharArray();
/* 163 */     int[] taggedQuery = null;
/* 164 */     while ((nextIndex < queryChars.length) && ((taggedQuery = InternetSearchQueryUtils.findNextTaggedQuery(queryChars, nextIndex, 2, true)) != null))
/*     */     {
/* 167 */       query = new String(queryChars, taggedQuery[1], taggedQuery[2] - taggedQuery[1] + 1).trim();
/*     */ 
/* 169 */       if (query.startsWith("site:"))
/*     */       {
/* 171 */         int size = query.length();
/* 172 */         int endIndex = size;
/* 173 */         for (int i = 5; i < size; ++i)
/*     */         {
/* 175 */           if (!Character.isWhitespace(query.charAt(i)))
/*     */             continue;
/* 177 */           endIndex = i;
/* 178 */           break;
/*     */         }
/*     */ 
/* 181 */         if (endIndex > 5)
/*     */         {
/* 183 */           String site = query.substring(5, endIndex);
/* 184 */           InternetSearchQueryUtils.setSearchProvider(site, binder);
/*     */         }
/*     */       }
/* 187 */       nextIndex = taggedQuery[4] + 1;
/*     */     }
/* 189 */     return 0;
/*     */   }
/*     */ 
/*     */   public int fixUpAndValidateQuery(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 197 */     String queryText = binder.getLocal("QueryText");
/* 198 */     String queryFormat = SearchIndexerUtils.getSearchQueryFormat(binder, ctxt);
/* 199 */     if ((queryFormat != null) && (queryFormat.equalsIgnoreCase("Universal")))
/*     */     {
/* 201 */       this.m_parser.setDateFormat(LocaleResources.m_searchFormat);
/* 202 */       ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/* 203 */       cxt.setParentContext(ctxt);
/* 204 */       cxt.setCachedObject("UserDateFormat", binder.m_blDateFormat);
/* 205 */       queryText = this.m_parser.parse(binder, cxt);
/*     */     }
/*     */     else
/*     */     {
/* 209 */       queryText = prepareFullTextQuery(queryText, binder, ctxt);
/*     */     }
/* 211 */     if (queryText != null)
/*     */     {
/* 213 */       binder.putLocal("TranslatedQueryText", queryText);
/* 214 */       binder.putLocal("QueryText", queryText);
/*     */     }
/* 216 */     return 0;
/*     */   }
/*     */ 
/*     */   public String prepareFullTextQuery(String query, DataBinder binder, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/* 226 */     int nextIndex = 0;
/*     */ 
/* 228 */     char[] queryChars = query.toCharArray();
/* 229 */     int[] fullTextIndex = null;
/* 230 */     IdcStringBuilder buf = new IdcStringBuilder(query.length());
/* 231 */     while ((nextIndex < queryChars.length) && ((fullTextIndex = InternetSearchQueryUtils.findNextFullTextQuery(queryChars, nextIndex)) != null))
/*     */     {
/* 233 */       buf.append(queryChars, nextIndex, fullTextIndex[2] - nextIndex);
/*     */ 
/* 235 */       char[] fullTextChars = new char[fullTextIndex[1] - fullTextIndex[0] + 1];
/*     */ 
/* 237 */       System.arraycopy(queryChars, fullTextIndex[0], fullTextChars, 0, fullTextChars.length);
/*     */ 
/* 239 */       translateFullTextQuery(buf, fullTextChars, binder, null, context);
/*     */ 
/* 241 */       nextIndex = fullTextIndex[3] + 1;
/*     */     }
/* 243 */     if (nextIndex < queryChars.length)
/*     */     {
/* 245 */       buf.append(queryChars, nextIndex, queryChars.length - nextIndex);
/*     */     }
/* 247 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public void translateFullTextQuery(IdcAppendable appendable, char[] queryChars, DataBinder binder, ParsedQueryElements parsedElts, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/* 267 */     appendable.append(queryChars, 0, queryChars.length);
/*     */   }
/*     */ 
/*     */   public String translateTaggedQuery(String query, String queryDefLabel, DataBinder binder, ExecutionContext context)
/*     */     throws ServiceException, DataException
/*     */   {
/* 277 */     int nextIndex = 0;
/* 278 */     char[] queryChars = query.toCharArray();
/* 279 */     int[] taggedQuery = null;
/* 280 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 281 */     while ((nextIndex < queryChars.length) && ((taggedQuery = InternetSearchQueryUtils.findNextTaggedQuery(queryChars, nextIndex, 15, true)) != null))
/*     */     {
/* 284 */       builder.append(queryChars, nextIndex, taggedQuery[3] - nextIndex);
/* 285 */       char[] taggedQueryChars = new char[taggedQuery[2] - taggedQuery[1] + 1];
/*     */ 
/* 287 */       System.arraycopy(queryChars, taggedQuery[1], taggedQueryChars, 0, taggedQueryChars.length);
/*     */ 
/* 289 */       switch (InternetSearchQueryUtils.TAGS[taggedQuery[0]])
/*     */       {
/*     */       case 1:
/* 292 */         translateFullTextQuery(builder, taggedQueryChars, binder, null, context);
/* 293 */         break;
/*     */       case 2:
/* 295 */         InternetSearchQueryUtils.parseQuickSearch(builder, taggedQueryChars, queryDefLabel, binder, null, context, this.m_config);
/*     */ 
/* 297 */         break;
/*     */       case 4:
/* 299 */         InternetSearchQueryUtils.parseISearch(builder, taggedQueryChars, queryDefLabel, binder, null, context, this.m_config);
/*     */ 
/* 301 */         break;
/*     */       case 8:
/* 303 */         DataBinder tmpBinder = new DataBinder();
/* 304 */         Properties prop = new Properties(binder.getLocalData());
/* 305 */         tmpBinder.putLocal("doMetaInternetSearch", "1");
/* 306 */         tmpBinder.setLocalData(prop);
/*     */ 
/* 308 */         this.m_parser.parseQueryEx(builder, taggedQueryChars, tmpBinder, null, context);
/* 309 */         break;
/*     */       case 3:
/*     */       case 5:
/*     */       case 6:
/*     */       case 7:
/*     */       default:
/* 311 */         builder.append(taggedQueryChars);
/*     */       }
/*     */ 
/* 314 */       nextIndex = taggedQuery[4] + 1;
/*     */     }
/* 316 */     if (nextIndex < queryChars.length)
/*     */     {
/* 318 */       builder.append(queryChars, nextIndex, queryChars.length - nextIndex);
/*     */     }
/* 320 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public int prepareQuery(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 330 */     return 0;
/*     */   }
/*     */ 
/*     */   public int appendQueryTextFilters(DataBinder binder, String filterStr)
/*     */     throws DataException
/*     */   {
/* 339 */     return 0;
/*     */   }
/*     */ 
/*     */   public int appendQueryTextFilter(IdcAppendable buffer, String filter)
/*     */     throws DataException
/*     */   {
/* 348 */     return 0;
/*     */   }
/*     */ 
/*     */   public int doCallback(IdcStringBuilder builder, DataBinder binder, ExecutionContext context, String[] args)
/*     */     throws ServiceException
/*     */   {
/* 355 */     IdcStringBuilder segment = new IdcStringBuilder();
/* 356 */     Object[] callBackObject = { segment, builder, args[0], args[1], args[2], args[3] };
/* 357 */     context.setCachedObject("queryParserCallbackFilter", callBackObject);
/*     */ 
/* 359 */     IdcStringBuilder actionBuilder = new IdcStringBuilder();
/* 360 */     actionBuilder.append(this.m_config.getCurrentEngineName());
/* 361 */     actionBuilder.append('|');
/* 362 */     actionBuilder.append("operator:");
/* 363 */     actionBuilder.append(args[0]);
/*     */ 
/* 365 */     boolean escapeReservedStrings = DataBinderUtils.getBoolean(binder, "escapeReservedStringsInSearch", false);
/* 366 */     String supportReservedExpProcessingString = this.m_config.getEngineValue(this.m_queryDefinitionLabel, "SupportReservedExpProcessing");
/* 367 */     boolean supportReservedExpProcessing = StringUtils.convertToBool(supportReservedExpProcessingString, false);
/*     */ 
/* 369 */     if ((supportReservedExpProcessing == true) && (escapeReservedStrings == true))
/*     */     {
/* 371 */       String postActionValue = args[2];
/*     */       try
/*     */       {
/* 375 */         postActionValue = QueryValueModifierUtils.processReservedSequences(this.m_config, context, this, binder, new String[] { args[0], args[1], args[2] });
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 380 */         throw new ServiceException(e);
/*     */       }
/*     */ 
/* 383 */       if ((postActionValue == null) || (postActionValue.length() == 0))
/*     */       {
/* 385 */         postActionValue = args[2];
/*     */       }
/*     */ 
/* 388 */       callBackObject = new Object[] { segment, builder, args[0], args[1], postActionValue, args[3] };
/* 389 */       context.setCachedObject("queryParserCallbackFilter", callBackObject);
/*     */     }
/*     */ 
/* 392 */     if (isFieldMonitored(args[1]))
/*     */     {
/* 394 */       actionBuilder.append('|');
/* 395 */       actionBuilder.append("fieldName:");
/* 396 */       actionBuilder.append(args[0]);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 405 */       int result = PluginFilters.filterWithAction(this.m_callbackFilterName, actionBuilder.toString(), null, binder, context);
/* 406 */       if (result == 1)
/*     */       {
/* 408 */         builder.append(segment);
/* 409 */         result = 0;
/*     */       }
/*     */       else
/*     */       {
/* 413 */         result = 1;
/*     */       }
/* 415 */       return result;
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 419 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public long getMonitoredOperators()
/*     */   {
/* 425 */     return 0L;
/*     */   }
/*     */ 
/*     */   public boolean isOperatorMonitored(long operator)
/*     */   {
/* 430 */     return (this.m_monitoredOperators & 1 << (int)operator) != 0L;
/*     */   }
/*     */ 
/*     */   public boolean isFieldMonitored(String fieldName)
/*     */   {
/* 435 */     return (fieldName != null) && (this.m_monitoredFields.get(fieldName.toLowerCase()) != null);
/*     */   }
/*     */ 
/*     */   public boolean isQueryContainsNativeSyntax(char[] values)
/*     */     throws ServiceException
/*     */   {
/* 446 */     return false;
/*     */   }
/*     */ 
/*     */   public String getCacheKey(String secGroupCacheContext, String accounts, String request, DataBinder binder, int flags, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 454 */     return request + "/" + secGroupCacheContext + "/" + accounts + "/" + this.m_config.getCurrentEngineName();
/*     */   }
/*     */ 
/*     */   public boolean validateSearchQuery(CommonSearchConfig csConfig, ExecutionContext context, DataBinder binder, String[] args) throws ServiceException
/*     */   {
/* 459 */     boolean isValid = true;
/*     */ 
/* 462 */     String engineName = this.m_queryDefinitionLabel;
/* 463 */     Report.trace("searchquery", "Validating query for fieldName '" + args[1] + "' and operator " + args[0], null);
/*     */ 
/* 465 */     String fieldName = args[1].toLowerCase();
/* 466 */     String databaseQueryKeys = this.m_config.getEngineValue(engineName, "DatabaseInternalQueryKeys");
/* 467 */     List databaseQueryKeyList = StringUtils.makeListFromSequenceSimple(databaseQueryKeys);
/*     */ 
/* 469 */     String lowerCaseFieldName = fieldName.toLowerCase();
/* 470 */     boolean isDatabaseQueryKey = databaseQueryKeyList.contains(lowerCaseFieldName);
/*     */ 
/* 472 */     if (isDatabaseQueryKey == true)
/*     */     {
/* 474 */       String validationErrorMsg = LocaleUtils.encodeMessage("csSearchDBInternalClause", null, fieldName);
/* 475 */       binder.putLocal("validationErrorMsg", validationErrorMsg);
/* 476 */       isValid = false;
/*     */     }
/*     */ 
/* 479 */     return isValid;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 484 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86114 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.CommonSearchConfigCompanionAdaptor
 * JD-Core Version:    0.5.4
 */