/*     */ package intradoc.server.script;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptExtensionsAdaptor;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.indexer.IndexerState;
/*     */ import intradoc.search.ParsedQueryElements;
/*     */ import intradoc.search.QueryElement;
/*     */ import intradoc.search.QueryElementValue;
/*     */ import intradoc.search.UniversalSearchQueryParser;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.server.SearchUtils;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.WebRequestUtils;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.CommonSearchConfigCompanion;
/*     */ import intradoc.shared.IndexerCollectionData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.QueryElementField;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SearchServiceScriptExtensions extends ScriptExtensionsAdaptor
/*     */ {
/*     */   public SearchServiceScriptExtensions()
/*     */   {
/*  48 */     this.m_variableTable = new String[] { "IsFromPageUrl", "NoMatches", "OneMatch", "UseXmlUrl", "UseHtmlOrTextHighlightInfo", "IsFullTextIndexed", "URL", "VDKSUMMARY", "IsLocalSearchCollectionID", "IsLocalSearchCollections", "sCollectionID", "QuickSearchQuery", "SearchFullTextQueryDef", "IsZoneQuickSearch" };
/*     */ 
/*  57 */     this.m_variableDefinitionTable = new int[][] { { 0, 1 }, { 1, 1 }, { 2, 1 }, { 3, 1 }, { 4, 1 }, { 5, 1 }, { 6, 2 }, { 7, 0 }, { 8, 1 }, { 9, 1 }, { 10, 0 }, { 11, 0 }, { 12, 0 }, { 13, 1 } };
/*     */ 
/*  75 */     this.m_functionTable = new String[] { "loadCollectionInfo", "computeURLView", "loadEnterpriseSearchCollections", "loadEnterpriseSearchResults", "proxiedCgiWebUrl", "proxiedBrowserFullCgiWebUrl", "indexerSetCollectionValue", "loadSearchOperatorTables", "isZoneField", "isZoneSearchField", "isFieldInSearchResult", "constructQueryFieldValues" };
/*     */ 
/*  98 */     this.m_functionDefinitionTable = new int[][] { { 0, 1, 0, -1, -1 }, { 1, 1, 0, -1, 1 }, { 2, 1, 1, -1, -1 }, { 3, 0, 0, -1, 1 }, { 4, 1, 0, -1, 0 }, { 5, 1, 0, -1, 0 }, { 6, 2, 0, 0, 0 }, { 7, 1, 0, -1, 1 }, { 8, 1, 0, -1, 1 }, { 9, 1, 0, -1, 1 }, { 10, 1, 0, -1, 1 }, { 11, 0, -1, -1, 0 } };
/*     */   }
/*     */ 
/*     */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*     */   {
/* 119 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/* 120 */     int[] config = (int[])(int[])info.m_entry;
/*     */ 
/* 122 */     boolean bResult = false;
/* 123 */     String sResult = null;
/*     */     try
/*     */     {
/* 126 */       switch (config[0])
/*     */       {
/*     */       case 0:
/* 130 */         String pageUrl = binder.getActiveValueSearchAll("FromPageUrl");
/* 131 */         bResult = pageUrl != null;
/* 132 */         break;
/*     */       case 1:
/* 137 */         String totalRowsStr = binder.getActiveValueSearchAll("TotalRows");
/* 138 */         int totalRows = NumberUtils.parseInteger(totalRowsStr, 0);
/* 139 */         bResult = totalRows == 0;
/* 140 */         break;
/*     */       case 2:
/* 145 */         String totalRowsStr = binder.getActiveValueSearchAll("TotalRows");
/* 146 */         int totalRows = NumberUtils.parseInteger(totalRowsStr, 0);
/*     */ 
/* 148 */         bResult = totalRows == 1;
/* 149 */         break;
/*     */       case 3:
/*     */       case 4:
/* 155 */         boolean enableHighlight = SharedObjects.getEnvValueAsBoolean("EnableDocumentHighlight", false);
/*     */ 
/* 157 */         boolean alwaysHighlight = SharedObjects.getEnvValueAsBoolean("HighlightAllQueries", false);
/* 158 */         if (!alwaysHighlight)
/*     */         {
/* 160 */           String query = binder.getAllowMissing("QueryText");
/* 161 */           if ((!DataBinderUtils.getBoolean(binder, "ftx", false)) || (query == null) || (query.length() == 0))
/*     */           {
/* 164 */             enableHighlight = false;
/*     */           }
/*     */         }
/*     */ 
/* 168 */         if (enableHighlight)
/*     */         {
/* 172 */           String format = binder.getAllowMissing("dFullTextFormat");
/* 173 */           if ((format == null) || (format.length() == 0))
/*     */           {
/* 175 */             if (!SearchLoader.isOptionalField("dFullTextFormat"))
/*     */               break label858;
/* 177 */             format = binder.getAllowMissing("dWebExtension");
/* 178 */             if (format == null) break label858; if (format.length() == 0)
/*     */             {
/*     */               break label858;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 188 */           format = format.toLowerCase();
/* 189 */           CommonSearchConfig searchConfig = SearchIndexerUtils.getCommonSearchConfig(context);
/* 190 */           String highlightType = searchConfig.getEngineValue("DefaultTextHighlightFormat");
/*     */ 
/* 192 */           String multipleTypeSupport = searchConfig.getEngineValue("SupportMultipleHighlightTypes");
/*     */ 
/* 194 */           if (StringUtils.convertToBool(multipleTypeSupport, false))
/*     */           {
/* 196 */             switch (config[0])
/*     */             {
/*     */             case 3:
/* 201 */               if ((enableHighlight) && (format != null) && (format.length() > 0))
/*     */               {
/* 204 */                 int index = format.indexOf("pdf");
/*     */ 
/* 206 */                 if (index >= 0)
/*     */                 {
/* 208 */                   highlightType = "PdfHighlight";
/* 209 */                   binder.putLocal("HighlightType", highlightType);
/* 210 */                   bResult = true;
/*     */                 }
/*     */               }
/* 212 */               break;
/*     */             case 4:
/* 218 */               String nativeFormat = binder.getAllowMissing("dFormat");
/* 219 */               String pubType = binder.getAllowMissing("dPublishType");
/* 220 */               if ((format.indexOf("htm") >= 0) || ((((pubType == null) || (pubType.length() == 0) || (pubType.equals("P")))) && (((nativeFormat == null) || (nativeFormat.indexOf("form") < 0))) && (format.indexOf("hcsp") >= 0)))
/*     */               {
/* 223 */                 highlightType = "HtmlHighlight";
/*     */               }
/* 225 */               else if ((format.indexOf("txt") >= 0) || (format.indexOf("text") >= 0))
/*     */               {
/* 227 */                 highlightType = "TextHighlight";
/*     */               }
/* 229 */               if (highlightType != null)
/*     */               {
/* 231 */                 binder.putLocal("HighlightType", highlightType);
/* 232 */                 bResult = true;
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/*     */           }
/* 240 */           else if (highlightType != null)
/*     */           {
/* 242 */             binder.putLocal("HighlightType", highlightType);
/*     */ 
/* 244 */             if (config[0] == 4)
/*     */             {
/* 246 */               bResult = true; } 
/*     */           }
/* 246 */         }break;
/*     */       case 5:
/* 254 */         bResult = SearchUtils.isFullTextIndexed(binder);
/* 255 */         break;
/*     */       case 6:
/* 259 */         sResult = binder.getAllowMissing("URL");
/* 260 */         if (isConditional)
/*     */         {
/* 262 */           bVal[0] = SearchUtils.computeURLView(sResult, binder);
/*     */         }
/*     */         else
/*     */         {
/* 266 */           sResult = WebRequestUtils.encodeUrlForBrowser(sResult, binder, context);
/* 267 */           sVal[0] = sResult;
/*     */         }
/* 269 */         break;
/*     */       case 7:
/* 274 */         bResult = SearchUtils.isFullTextIndexed(binder);
/* 275 */         if (bResult)
/*     */         {
/* 277 */           sResult = binder.getAllowMissing("VDKSUMMARY"); } break;
/*     */       case 8:
/*     */       case 9:
/* 285 */         bResult = SearchUtils.isLocalCollectionID(binder);
/* 286 */         break;
/*     */       case 10:
/* 291 */         sResult = binder.getActiveAllowMissing("sCollectionID");
/* 292 */         if ((sResult == null) || (sResult.length() == 0))
/*     */         {
/* 294 */           sResult = binder.getLocal("currentCollectionID"); } break;
/*     */       case 11:
/*     */       case 12:
/* 302 */         CommonSearchConfig searchConfig = SearchIndexerUtils.getCommonSearchConfig(context);
/* 303 */         String format = SearchIndexerUtils.getSearchQueryFormat(binder, context);
/*     */ 
/* 306 */         String name = searchConfig.getCurrentEngineName();
/*     */ 
/* 308 */         Hashtable clientConfigs = (Hashtable)SharedObjects.getObject("globalObjects", "CommonSearchClientObjects");
/*     */ 
/* 310 */         Properties clientConfig = (Properties)clientConfigs.get(format + "." + name);
/* 311 */         if (clientConfig == null)
/*     */         {
/* 313 */           clientConfig = (Properties)clientConfigs.get(format);
/*     */         }
/*     */ 
/* 316 */         if (clientConfig == null)
/*     */         {
/* 318 */           name = searchConfig.getStringFromTable("SearchRepository", "srID", name, "srEngineName");
/* 319 */           clientConfig = (Properties)clientConfigs.get(name);
/*     */         }
/* 321 */         sResult = clientConfig.getProperty(info.m_key);
/* 322 */         break;
/*     */       case 13:
/* 326 */         bResult = SearchLoader.isZoneQuickSearch(context);
/* 327 */         break;
/*     */       default:
/* 330 */         return false;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 335 */       label858: Report.trace(null, null, e);
/*     */     }
/*     */ 
/* 338 */     switch (config[1])
/*     */     {
/*     */     case 0:
/* 342 */       if (isConditional)
/*     */       {
/* 344 */         bVal[0] = (((sResult != null) && (sResult.length() > 0)) ? 1 : false); break label968:
/*     */       }
/*     */ 
/* 348 */       sVal[0] = sResult;
/*     */ 
/* 350 */       break;
/*     */     case 1:
/* 354 */       if (isConditional)
/*     */       {
/* 356 */         bVal[0] = bResult; break label968:
/*     */       }
/*     */ 
/* 360 */       sVal[0] = ((bResult) ? "1" : "0");
/*     */     }
/*     */ 
/* 367 */     label968: return true;
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/* 374 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/* 375 */     int[] config = (int[])(int[])info.m_entry;
/* 376 */     String function = info.m_key;
/*     */ 
/* 378 */     int nargs = args.length - 1;
/* 379 */     int allowedParams = config[1];
/* 380 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*     */     {
/* 382 */       String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*     */ 
/* 384 */       throw new IllegalArgumentException(msg);
/*     */     }
/*     */ 
/* 390 */     String sArg1 = null;
/* 391 */     String sArg2 = null;
/* 392 */     long lArg1 = 0L;
/*     */ 
/* 394 */     if (nargs > 0)
/*     */     {
/* 396 */       if (config[2] == 0)
/*     */       {
/* 398 */         sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*     */       }
/* 400 */       else if (config[2] == 1)
/*     */       {
/* 402 */         lArg1 = ScriptUtils.getLongVal(args[0], context);
/*     */       }
/*     */     }
/*     */ 
/* 406 */     if (nargs > 1)
/*     */     {
/* 408 */       if (config[3] == 0)
/*     */       {
/* 410 */         sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*     */       }
/* 412 */       else if (config[3] != 1);
/*     */     }
/*     */ 
/* 420 */     boolean bResult = false;
/* 421 */     int iResult = 0;
/* 422 */     double dResult = 0.0D;
/*     */ 
/* 424 */     Object oResult = null;
/*     */ 
/* 426 */     switch (config[0])
/*     */     {
/*     */     case 0:
/* 429 */       String collectionName = ScriptUtils.getDisplayString(sArg1, context);
/* 430 */       SearchUtils.loadCollectionInfo(collectionName, binder);
/* 431 */       oResult = "";
/* 432 */       break;
/*     */     case 1:
/* 435 */       bResult = SearchUtils.computeURLView(sArg1, binder);
/* 436 */       break;
/*     */     case 2:
/* 440 */       if (context instanceof Service)
/*     */       {
/* 442 */         Service service = (Service)context;
/* 443 */         UserData data = (UserData)service.getCachedObject("UserData");
/* 444 */         if (data == null)
/*     */         {
/* 446 */           data = service.getUserData();
/*     */         }
/* 448 */         DataResultSet userCollections = SearchLoader.getSearchableProviderList(data, lArg1 > 0L, false);
/*     */ 
/* 450 */         if (binder != null)
/*     */         {
/*     */           FieldInfo[] infos;
/*     */           try
/*     */           {
/* 455 */             infos = ResultSetUtils.createInfoList(userCollections, new String[] { "ProviderName", "Selected", "IsLocalCollection" }, false);
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 460 */             throw new ServiceException(e);
/*     */           }
/*     */ 
/* 463 */           String providers = binder.getAllowMissing("SearchProviders");
/* 464 */           if ((providers != null) && (providers.length() > 0))
/*     */           {
/* 466 */             Vector list = StringUtils.parseArray(providers, ',', '^');
/* 467 */             int size = list.size();
/* 468 */             for (int i = 0; i < size; ++i)
/*     */             {
/* 470 */               String name = (String)list.elementAt(i);
/* 471 */               Vector row = userCollections.findRow(infos[0].m_index, name);
/* 472 */               if (row == null)
/*     */                 continue;
/* 474 */               row.setElementAt("1", infos[1].m_index);
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 480 */             for (userCollections.first(); userCollections.isRowPresent(); userCollections.next())
/*     */             {
/* 482 */               String isLocal = userCollections.getStringValue(infos[2].m_index);
/* 483 */               if (!StringUtils.convertToBool(isLocal, false))
/*     */                 continue;
/* 485 */               Vector row = userCollections.getCurrentRowValues();
/* 486 */               row.setElementAt("1", infos[1].m_index);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 491 */         binder.addResultSet("EnterpriseSearchCollections", userCollections);
/* 492 */         bResult = true;
/*     */       }
/*     */       else {
/* 495 */         return false;
/*     */       }
/*     */     case 3:
/* 500 */       if (binder != null)
/*     */       {
/* 502 */         String name = binder.getAllowMissing("ResultSetName");
/* 503 */         if (name == null)
/*     */         {
/* 505 */           return false;
/*     */         }
/*     */ 
/* 508 */         DataResultSet rset = (DataResultSet)binder.getResultSet(name);
/* 509 */         if (rset == null)
/*     */         {
/* 511 */           binder.removeResultSet("SearchResults");
/* 512 */           bResult = false;
/*     */         }
/*     */         else
/*     */         {
/* 516 */           binder.addResultSet("SearchResults", rset);
/* 517 */           bResult = true;
/*     */         }
/*     */       }
/*     */       else {
/* 521 */         return false;
/*     */       }
/*     */     case 4:
/* 526 */       boolean isAbs = false;
/* 527 */       if (binder != null)
/*     */       {
/* 529 */         isAbs = DataBinderUtils.getBoolean(binder, "isAbsoluteWeb", false);
/*     */       }
/*     */ 
/* 532 */       String pathRoot = binder.getEnvironmentValue("HTTP_CGIPATHROOT");
/* 533 */       if ((pathRoot != null) && (!isAbs))
/*     */       {
/* 535 */         oResult = pathRoot + sArg1 + "pxs";
/*     */       }
/*     */       else
/*     */       {
/* 539 */         oResult = DirectoryLocator.getExternalProxiedCgiWebUrl(isAbs, sArg1);
/*     */       }
/* 541 */       break;
/*     */     case 5:
/* 546 */       String fullPath = null;
/* 547 */       String protocol = binder.getEnvironmentValue("SERVER_PROTOCOL");
/* 548 */       int index = protocol.indexOf(47);
/* 549 */       if (index > 0)
/*     */       {
/* 551 */         fullPath = protocol.substring(0, index);
/*     */       }
/*     */       else
/*     */       {
/* 555 */         fullPath = "http";
/*     */       }
/* 557 */       fullPath = fullPath.toLowerCase();
/*     */ 
/* 559 */       boolean useSsl = SharedObjects.getEnvValueAsBoolean("UseSSL", false);
/* 560 */       if ((useSsl) && (fullPath.equals("http")))
/*     */       {
/* 562 */         fullPath = "https";
/*     */       }
/*     */ 
/* 565 */       fullPath = fullPath + "://";
/* 566 */       String serverName = binder.getEnvironmentValue("HTTP_HOST");
/* 567 */       if (serverName == null)
/*     */       {
/* 569 */         serverName = binder.getEnvironmentValue("SERVER_NAME");
/*     */       }
/* 571 */       boolean addPortNumber = serverName.indexOf(":") <= 0;
/*     */ 
/* 573 */       fullPath = fullPath + serverName;
/*     */ 
/* 575 */       String portStr = null;
/* 576 */       if (addPortNumber)
/*     */       {
/* 578 */         String serverPort = binder.getEnvironmentValue("SERVER_PORT");
/* 579 */         if ((serverPort != null) && (serverPort.length() > 0))
/*     */         {
/* 581 */           if (!serverPort.equals("80"))
/*     */           {
/* 583 */             portStr = serverPort;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 588 */           String fullHostAddress = binder.getAllowMissing("HttpServerAddress");
/* 589 */           index = fullHostAddress.indexOf(":");
/* 590 */           if (index > 0)
/*     */           {
/* 592 */             portStr = fullHostAddress.substring(index + 1);
/* 593 */             index = portStr.indexOf(47);
/* 594 */             if (index > 0)
/*     */             {
/* 596 */               portStr = portStr.substring(0, index);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/* 601 */       if (portStr != null)
/*     */       {
/* 603 */         fullPath = fullPath + ":";
/* 604 */         fullPath = fullPath + portStr;
/*     */       }
/*     */ 
/* 607 */       String cgiPathRoot = binder.getEnvironmentValue("HTTP_CGIPATHROOT");
/* 608 */       if (cgiPathRoot != null)
/*     */       {
/* 610 */         oResult = fullPath + cgiPathRoot + sArg1 + "pxs";
/*     */       }
/*     */       else
/*     */       {
/* 614 */         oResult = fullPath + DirectoryLocator.getExternalProxiedCgiWebUrl(false, sArg1);
/*     */       }
/* 616 */       break;
/*     */     case 6:
/* 620 */       ScriptExtensionUtils.checkSecurityForIdocscript(context, "admin");
/*     */ 
/* 622 */       Service service = (Service)context;
/* 623 */       UserData data = (UserData)service.getCachedObject("UserData");
/* 624 */       if (data == null)
/*     */       {
/* 626 */         data = service.getUserData();
/* 627 */         if (data == null)
/*     */         {
/* 629 */           throw new ServiceException(null, new IdcMessage("csInsufficientPrivilege", new Object[0]));
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 634 */       Object obj = context.getCachedObject("IndexerState");
/* 635 */       if ((obj != null) && (obj instanceof IndexerState))
/*     */       {
/* 637 */         IndexerState state = (IndexerState)obj;
/* 638 */         state.setCollectionValue(sArg1, sArg2);
/* 639 */         state.m_perBatchOverrides.put(sArg1, sArg2);
/*     */       }
/*     */       else
/*     */       {
/* 644 */         throw new ServiceException("Usage of indexerSetCollectionValue in improper context.");
/*     */       }
/*     */     case 7:
/* 650 */       String queryFormat = (String)args[0];
/* 651 */       String oldQueryFormat = queryFormat;
/* 652 */       CommonSearchConfig searchConfig = null;
/*     */       try
/*     */       {
/* 655 */         searchConfig = SearchIndexerUtils.getCommonSearchConfig(context);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 659 */         throw new ServiceException(e);
/*     */       }
/* 661 */       if (queryFormat.length() == 0)
/*     */       {
/* 663 */         queryFormat = SearchIndexerUtils.getSearchQueryFormat(binder, context);
/*     */       }
/*     */       else
/*     */       {
/* 667 */         queryFormat = SearchIndexerUtils.convertToProperSearchQueryFormat(queryFormat);
/*     */       }
/* 669 */       String engineName = SearchIndexerUtils.getSearchEngineName(context);
/* 670 */       Hashtable clientConfigs = (Hashtable)SharedObjects.getObject("globalObjects", "CommonSearchClientObjects");
/*     */ 
/* 672 */       Properties clientConfig = (Properties)clientConfigs.get(queryFormat + "." + engineName);
/*     */ 
/* 674 */       if (clientConfig == null)
/*     */       {
/* 676 */         clientConfig = (Properties)clientConfigs.get(queryFormat);
/*     */       }
/*     */ 
/* 679 */       if (clientConfig == null)
/*     */       {
/* 681 */         queryFormat = searchConfig.getStringFromTable("SearchRepository", "srID", queryFormat, "srEngineName");
/* 682 */         if (queryFormat != null)
/*     */         {
/* 684 */           clientConfig = (Properties)clientConfigs.get(queryFormat);
/*     */         }
/*     */         else
/*     */         {
/* 688 */           String msg = LocaleUtils.encodeMessage("csSearchWrongQueryFormat", null, oldQueryFormat);
/* 689 */           throw new ServiceException(msg);
/*     */         }
/*     */       }
/* 692 */       synchronized (this)
/*     */       {
/* 694 */         String[] rsetNames = { "SearchTextField", "SearchDateField", "SearchBooleanField", "SearchIntegerField", "SearchZoneField", "SearchQueryOpMap", "SearchQueryOpStrMap", "SearchSortFields" };
/*     */ 
/* 698 */         for (int i = 0; i < rsetNames.length; ++i)
/*     */         {
/* 700 */           DataResultSet drset = (DataResultSet)clientConfig.get(rsetNames[i]);
/* 701 */           if (drset == null)
/*     */             continue;
/* 703 */           binder.addResultSet(rsetNames[i], drset.shallowClone());
/*     */         }
/*     */ 
/* 708 */         String[] colKeys = { "SearchFullTextQueryDef", "SearchConjunction", "SearchOrConjunction", "SearchNotOperator", "DefaultSearchOperator", "DefaultNBFieldSearchOperator" };
/*     */ 
/* 711 */         for (String key : colKeys)
/*     */         {
/* 713 */           String value = (String)clientConfig.get(key);
/* 714 */           if ((value == null) || (value.length() == 0))
/*     */             continue;
/* 716 */           binder.putLocal(key, value);
/*     */         }
/*     */       }
/*     */ 
/* 720 */       break;
/*     */     case 8:
/* 724 */       bResult = SearchLoader.isZoneField(sArg1, context);
/* 725 */       break;
/*     */     case 9:
/* 729 */       bResult = SearchLoader.isZoneSearchField(sArg1, context);
/* 730 */       break;
/*     */     case 10:
/* 734 */       String name = SearchIndexerUtils.getSearchEngineName(context);
/* 735 */       IndexerCollectionData data = SearchLoader.getCurrentSearchableFieldsAllowInit(name);
/* 736 */       if ((data != null) && (data.m_fieldInfos.get(sArg1) != null))
/*     */       {
/* 738 */         bResult = true;
/*     */       }
/*     */       else
/*     */       {
/*     */         try
/*     */         {
/* 744 */           CommonSearchConfig csc = SearchIndexerUtils.getCommonSearchConfig(context);
/*     */ 
/* 746 */           String dynFields = csc.getEvaluatedEngineValue("DynamicResultFields", context);
/*     */ 
/* 748 */           if ((dynFields != null) && (dynFields.indexOf('|' + sArg1 + '|') >= 0))
/*     */           {
/* 750 */             bResult = true;
/*     */           }
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 755 */           throw new ServiceException(e);
/*     */         }
/*     */       }
/*     */     case 11:
/* 762 */       if (context instanceof Service)
/*     */       {
/*     */         try
/*     */         {
/* 766 */           Service service = (Service)context;
/* 767 */           String queryText = binder.getLocal("QueryText");
/* 768 */           String searchFormType = binder.getLocal("searchFormType");
/* 769 */           boolean isSearchFormTypeLocked = DataBinderUtils.getBoolean(binder, "isSearchFormTypeLocked", false);
/*     */ 
/* 772 */           ParsedQueryElements queryElts = new ParsedQueryElements();
/* 773 */           context.setCachedObject("ParsedQueryElements", queryElts);
/*     */ 
/* 775 */           CommonSearchConfig csc = SearchIndexerUtils.retrieveSearchConfig(service);
/* 776 */           Vector fieldNames = SearchLoader.loadFieldInfo(service, binder);
/* 777 */           csc.prepareQueryText(queryText, binder, context);
/* 778 */           CommonSearchConfigCompanion companion = csc.getCompanion();
/* 779 */           companion.fixUpAndValidateQuery(binder, context);
/*     */ 
/* 781 */           Map queryFieldValues = new HashMap();
/* 782 */           queryFieldValues.put("queryBuilder", new IdcStringBuilder("\n"));
/* 783 */           if (searchFormType.equals("standard"))
/*     */           {
/* 785 */             queryFieldValues.put("standard", new IdcStringBuilder());
/*     */           }
/*     */ 
/* 788 */           boolean isParsable = parseSearchQueryIntoQueryFieldValues(queryElts.m_searchQuery, null, queryFieldValues, true, new HashMap(), binder);
/*     */ 
/* 791 */           binder.putLocal("QueryText", queryText);
/* 792 */           if (isParsable)
/*     */           {
/* 794 */             IdcStringBuilder standardBuilder = (IdcStringBuilder)queryFieldValues.get("standard");
/* 795 */             if ((standardBuilder != null) && (searchFormType.equals("standard")))
/*     */             {
/* 797 */               binder.putLocal("QueryFieldValues", standardBuilder.toString());
/*     */             }
/*     */             else
/*     */             {
/* 801 */               IdcStringBuilder queryBuilder = (IdcStringBuilder)queryFieldValues.get("queryBuilder");
/* 802 */               if ((queryBuilder != null) && ((
/* 804 */                 (searchFormType.equals("queryBuilder")) || (!isSearchFormTypeLocked))))
/*     */               {
/* 806 */                 binder.putLocal("searchFormType", "queryBuilder");
/* 807 */                 binder.putLocal("QueryFieldValues", queryBuilder.toString());
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/* 812 */             binder.putLocal("QueryFullText", "");
/* 813 */             PluginFilters.filter("postConstructQueryFieldValues", service.getWorkspace(), binder, service);
/*     */           }
/* 815 */           else if (!isSearchFormTypeLocked)
/*     */           {
/* 817 */             binder.putLocal("searchFormType", "queryBuilder");
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 822 */           Report.trace("idocscript", "Unable to parse QueryText for query page.", e);
/*     */         }
/* 823 */       }break;
/*     */     default:
/* 828 */       return false;
/*     */     }
/*     */ 
/* 831 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);
/*     */ 
/* 835 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean parseSearchQueryIntoQueryFieldValues(QueryElement element, QueryElement parent, Map<String, IdcStringBuilder> queryFieldValues, boolean allowOr, Map args, DataBinder binder)
/*     */   {
/* 842 */     if (element.m_operator == 17)
/*     */     {
/* 844 */       queryFieldValues.remove("standard");
/*     */ 
/* 846 */       if ((!queryFieldValues.containsKey("queryBuilder")) && (!allowOr))
/*     */       {
/* 848 */         return false;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 853 */       allowOr = false;
/*     */     }
/*     */ 
/* 856 */     boolean canParse = true;
/* 857 */     IdcStringBuilder queryBuilder = (IdcStringBuilder)queryFieldValues.get("queryBuilder");
/* 858 */     IdcStringBuilder standardBuilder = (IdcStringBuilder)queryFieldValues.get("standard");
/* 859 */     if (element.m_type == 101)
/*     */     {
/* 861 */       for (int i = 0; (canParse) && (i < element.m_subElements.size()); ++i)
/*     */       {
/* 863 */         if (i > 0)
/*     */         {
/* 865 */           switch (element.m_operator)
/*     */           {
/*     */           case 17:
/* 869 */             if (queryBuilder != null)
/*     */             {
/* 871 */               if (queryBuilder.length() > 0)
/*     */               {
/* 873 */                 queryBuilder.append('\n');
/*     */               }
/* 875 */               queryBuilder.append("or"); } break;
/*     */           case 16:
/* 881 */             if (queryBuilder != null)
/*     */             {
/* 883 */               if (queryBuilder.length() > 0)
/*     */               {
/* 885 */                 queryBuilder.append('\n');
/*     */               }
/* 887 */               queryBuilder.append("and");
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 894 */         QueryElement q = (QueryElement)element.m_subElements.get(i);
/* 895 */         canParse = parseSearchQueryIntoQueryFieldValues(q, element, queryFieldValues, allowOr, args, binder);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 900 */       boolean isNot = false;
/* 901 */       String field = element.m_field.m_name;
/* 902 */       String value = element.m_value.m_originalValue;
/* 903 */       int operatorCode = element.m_operator;
/* 904 */       if ((operatorCode & 0x100) != 0)
/*     */       {
/* 906 */         isNot = true;
/* 907 */         operatorCode &= -257;
/*     */       }
/* 909 */       String operator = UniversalSearchQueryParser.OPERATORKEYS[operatorCode];
/* 910 */       if (isNot)
/*     */       {
/* 912 */         operator = "not" + operator.substring(0, 1).toUpperCase(Locale.ENGLISH) + operator.substring(1);
/*     */       }
/*     */ 
/* 915 */       if ((args.containsKey("hasField:" + field)) && (!UniversalSearchQueryParser.isRangeOperator(operatorCode)))
/*     */       {
/* 917 */         queryFieldValues.remove("standard");
/*     */       }
/*     */ 
/* 920 */       if (queryBuilder != null)
/*     */       {
/* 922 */         if (queryBuilder.length() > 1)
/*     */         {
/* 924 */           queryBuilder.append('\n');
/*     */         }
/*     */ 
/* 927 */         queryBuilder.append(field);
/* 928 */         queryBuilder.append('\n');
/* 929 */         queryBuilder.append(operator);
/* 930 */         queryBuilder.append('\n');
/* 931 */         queryBuilder.append(value);
/*     */       }
/* 933 */       if (standardBuilder != null)
/*     */       {
/* 935 */         if (standardBuilder.length() > 0)
/*     */         {
/* 937 */           standardBuilder.append('\n');
/*     */         }
/*     */ 
/* 940 */         standardBuilder.append(field);
/* 941 */         standardBuilder.append('\n');
/* 942 */         standardBuilder.append(operator);
/* 943 */         standardBuilder.append('\n');
/* 944 */         standardBuilder.append(value);
/*     */ 
/* 950 */         binder.putLocal(field + ":searchval", value);
/*     */       }
/*     */ 
/* 953 */       args.put("hasField:" + field, "1");
/*     */     }
/*     */ 
/* 956 */     return canParse;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 961 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104495 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.SearchServiceScriptExtensions
 * JD-Core Version:    0.5.4
 */