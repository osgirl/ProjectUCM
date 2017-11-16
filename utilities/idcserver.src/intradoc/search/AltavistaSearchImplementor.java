/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class AltavistaSearchImplementor extends CommonSearchAdaptor
/*     */ {
/*     */   protected CommonSearchConnection m_sc;
/*     */   protected AvsSearch m_connection;
/*     */   protected String m_result;
/*     */   protected Vector m_collections;
/*     */   protected DataBinder m_binder;
/*     */   public final String[] AVS_SEARCH_PARAMS;
/*     */   public final String[] AVS_OPS;
/*     */ 
/*     */   public AltavistaSearchImplementor()
/*     */   {
/*  38 */     this.AVS_SEARCH_PARAMS = new String[] { "MaxNumericVal", "AvsLicenseKey", "AvsSearchTimeout", "AvsPositionBoost", "AvsRankToBool", "AvsOptionIndexCJKCharsAsWords", "AvsNoLogging", "AvsRankLatest", "AvsSearchSince", "AvsIgnoredThresh", "AvsCharsBeforeWildcard", "AvsUnlimitedWildWords", "AvsIndexFormat", "AvsCacheThreshold", "AvsInstallDir", "AvsCharacterEncoding", "EnableThesaurus", "EnableSpellCheck", "EnableStemmingSearch", "EnableCJKCompoundSearch" };
/*     */ 
/*  45 */     this.AVS_OPS = new String[] { "and", "or", "not", "near", "within", "before", "after", "atleast", "ge", "lt" };
/*     */   }
/*     */ 
/*     */   public void init(CommonSearchConnection sc)
/*     */   {
/*  50 */     super.init(sc);
/*  51 */     this.m_sc = sc;
/*  52 */     this.m_connection = new AvsSearch();
/*  53 */     this.m_binder = this.m_sc.m_connectionData;
/*     */   }
/*     */ 
/*     */   public boolean prepareUse(ExecutionContext ctxt)
/*     */   {
/*  59 */     super.prepareUse(ctxt);
/*     */     try
/*     */     {
/*  62 */       this.m_connection.init(binderToString(this.m_sc.m_connectionData, "!csAvsConnectionInitError"), SearchLoader.m_encoding);
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/*  66 */       Report.trace("search", null, e);
/*  67 */       return false;
/*     */     }
/*  69 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean initCollection(Vector collections)
/*     */   {
/*  74 */     if (this.m_connection == null)
/*     */     {
/*  76 */       Report.trace("search", "!csConnectionNotOpen", null);
/*     */     }
/*     */ 
/*  79 */     this.m_collections = collections;
/*  80 */     for (int i = 0; i < collections.size(); ++i)
/*     */     {
/*  82 */       String dir = this.m_sc.m_connectionData.getEnvironmentValue("IntradocDir");
/*  83 */       String path = (String)collections.elementAt(i);
/*  84 */       String absPath = FileUtils.getAbsolutePath(dir, path);
/*     */ 
/*  86 */       int result = FileUtils.checkFile(absPath, false, false);
/*  87 */       if ((result == 0) || (result == -24))
/*     */         continue;
/*  89 */       String msg = FileUtils.getErrorMsg(path, false, result);
/*  90 */       msg = LocaleUtils.encodeMessage("csSearchCollectionPathError", msg);
/*  91 */       Report.trace("search", msg, null);
/*  92 */       return false;
/*     */     }
/*     */ 
/*  95 */     return true;
/*     */   }
/*     */ 
/*     */   public String doQuery(DataBinder binder)
/*     */   {
/* 101 */     String encoding = SearchLoader.m_encoding;
/*     */ 
/* 103 */     String sortSpec = binder.getLocal("SortSpec");
/* 104 */     String query = binder.getLocal("QueryText");
/* 105 */     if ((query == null) || (query.length() == 0))
/*     */     {
/* 107 */       query = "**";
/* 108 */       binder.putLocal("QueryText", query);
/*     */     }
/*     */ 
/* 112 */     if ((sortSpec != null) && (((sortSpec.equalsIgnoreCase("Score")) || (sortSpec.equalsIgnoreCase("+Score")) || (sortSpec.length() == 0))))
/*     */     {
/* 115 */       sortSpec = "";
/* 116 */       binder.removeLocal("AvsRankToBool");
/*     */     }
/*     */     else
/*     */     {
/* 121 */       binder.putLocal("AvsRankToBool", "2");
/*     */     }
/*     */ 
/* 125 */     String indexPath = (String)this.m_collections.elementAt(0);
/* 126 */     indexPath.trim();
/* 127 */     if (indexPath.endsWith("/"))
/*     */     {
/* 129 */       indexPath = indexPath.substring(0, indexPath.length() - 1);
/*     */     }
/*     */ 
/* 132 */     boolean enableDebug = false;
/* 133 */     String debugLevel = this.m_sc.m_connectionData.getEnvironmentValue("SearchDebugLevel");
/* 134 */     if ((debugLevel != null) && (!debugLevel.equalsIgnoreCase("none")))
/*     */     {
/* 136 */       enableDebug = true;
/*     */     }
/* 138 */     DataResultSet drset = SharedObjects.getTable("IndexerFieldsMap");
/* 139 */     if (drset != null)
/*     */     {
/* 141 */       binder.addResultSet("IndexerFieldsMap", drset);
/*     */     }
/* 143 */     String binderStr = null;
/*     */     try
/*     */     {
/* 146 */       binderStr = binderToString(binder, "!csAvsIOStreamError");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 150 */       return e.getMessage();
/*     */     }
/*     */ 
/* 154 */     this.m_result = this.m_connection.doQuery(binderStr, indexPath, sortSpec, query, encoding, enableDebug);
/* 155 */     return null;
/*     */   }
/*     */ 
/*     */   public String getResult()
/*     */   {
/* 163 */     return this.m_result;
/*     */   }
/*     */ 
/*     */   public void closeSession()
/*     */   {
/* 169 */     if (this.m_connection == null)
/*     */       return;
/* 171 */     this.m_connection.close();
/*     */   }
/*     */ 
/*     */   protected String expandQuery(String orgQuery, String langId)
/*     */     throws ServiceException
/*     */   {
/* 180 */     String expandQuery = "";
/* 181 */     String tempQuery = orgQuery;
/* 182 */     String temp = "";
/* 183 */     String tempSubQuery = "";
/* 184 */     Vector words = new IdcVector();
/*     */ 
/* 186 */     boolean enableCjkCompoundSearch = StringUtils.convertToBool(this.m_binder.getEnvironmentValue("EnableCJKCompoundSearch"), false);
/* 187 */     boolean enableStemmingSearch = StringUtils.convertToBool(this.m_binder.getEnvironmentValue("EnableStemmingSearch"), false);
/* 188 */     boolean enableThesaurus = StringUtils.convertToBool(this.m_binder.getEnvironmentValue("EnableThesaurus"), false);
/* 189 */     boolean enableSpellCheck = StringUtils.convertToBool(this.m_binder.getEnvironmentValue("EnableSpellCheck"), false);
/* 190 */     boolean isIndexCJKCharsAsWord = StringUtils.convertToBool(this.m_binder.getEnvironmentValue("AvsOptionIndexCJKCharsAsWords"), false);
/*     */ 
/* 192 */     boolean isCjk = (langId.startsWith("ko")) || (langId.startsWith("ja")) || (langId.startsWith("zh"));
/* 193 */     boolean allowExpand = ((isCjk) && (enableCjkCompoundSearch)) || ((!isCjk) && (((enableStemmingSearch) || (enableThesaurus) || (enableSpellCheck)))) || ((isCjk) && (isIndexCJKCharsAsWord));
/*     */ 
/* 197 */     if (!allowExpand)
/*     */     {
/* 199 */       return orgQuery;
/*     */     }
/*     */ 
/* 203 */     if (isIndexCJKCharsAsWord)
/*     */     {
/* 205 */       temp = convertCJKQuery(orgQuery);
/* 206 */       if ((temp == null) || (temp.length() == 0))
/*     */       {
/* 208 */         expandQuery = orgQuery;
/*     */       }
/*     */       else
/*     */       {
/* 212 */         expandQuery = temp;
/*     */       }
/*     */ 
/* 215 */       return expandQuery;
/*     */     }
/*     */ 
/* 218 */     boolean isLiteral = false;
/* 219 */     boolean isLetter = false;
/* 220 */     int letterStart = 0;
/* 221 */     int start = 0;
/* 222 */     int queryLen = tempQuery.length();
/*     */ 
/* 224 */     for (int i = 0; i < queryLen; ++i)
/*     */     {
/* 226 */       char ch = tempQuery.charAt(i);
/*     */ 
/* 228 */       switch (ch)
/*     */       {
/*     */       case '{':
/*     */       case '}':
/* 232 */         if ((((ch != '{') || (isLiteral))) && (((ch != '}') || (!isLiteral))))
/*     */           continue;
/* 234 */         if (ch == '{')
/*     */         {
/* 236 */           isLiteral = true;
/*     */         }
/*     */         else
/*     */         {
/* 240 */           isLiteral = false;
/*     */         }
/*     */ 
/* 243 */         if (i >= start)
/*     */         {
/* 245 */           expandQuery = expandQuery + tempQuery.substring(start, i + 1);
/*     */         }
/* 247 */         start = i + 1; break;
/*     */       case '"':
/*     */       case '[':
/* 252 */         if (isLiteral)
/*     */           continue;
/* 254 */         char endCh = ']';
/* 255 */         if (ch == '"')
/*     */         {
/* 257 */           endCh = '"';
/*     */         }
/* 259 */         int index = tempQuery.indexOf(endCh, i + 1);
/* 260 */         if (index > 0)
/*     */         {
/* 262 */           if (i >= start)
/*     */           {
/* 264 */             expandQuery = expandQuery + tempQuery.substring(start, index + 1);
/*     */           }
/* 266 */           i = index;
/* 267 */           start = i + 1;
/*     */         }
/*     */         else
/*     */         {
/* 271 */           i = tempQuery.length();
/* 272 */           expandQuery = orgQuery;
/* 273 */           continue;
/*     */         }
/* 275 */         break;
/*     */       case '#':
/*     */       case '@':
/* 279 */         if ((isLiteral) || (isCjk) || ((!enableStemmingSearch) && (!enableThesaurus))) {
/*     */           continue;
/*     */         }
/* 282 */         if (i > start)
/*     */         {
/* 285 */           expandQuery = expandQuery + tempQuery.substring(start, i);
/* 286 */           start = i;
/*     */         }
/* 288 */         ++start;
/*     */ 
/* 291 */         for (i = start; i < queryLen; ++i)
/*     */         {
/* 293 */           char tempCh = tempQuery.charAt(i);
/* 294 */           if (!Character.isLetter(tempCh)) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 299 */         String val = tempQuery.substring(start, i).trim();
/*     */ 
/* 302 */         if ((val != null) && (val.length() > 0))
/*     */         {
/* 304 */           if ((ch == '@') && (enableStemmingSearch))
/*     */           {
/* 307 */             words = getStemWords(val, langId);
/*     */           }
/* 309 */           else if ((ch == '#') && (enableThesaurus))
/*     */           {
/* 311 */             words = getThesaurus(val, langId);
/*     */           }
/*     */         }
/*     */ 
/* 315 */         if (words.size() > 0)
/*     */         {
/* 317 */           tempSubQuery = formatWordsToQuery(words, val, true, false);
/* 318 */           if ((tempSubQuery != null) && (tempSubQuery.length() > 0))
/*     */           {
/* 320 */             expandQuery = expandQuery + tempSubQuery;
/*     */           }
/*     */ 
/* 323 */           start = i;
/*     */         }
/*     */         else
/*     */         {
/* 327 */           int end = i + 1;
/*     */ 
/* 330 */           if (end >= queryLen)
/*     */           {
/* 332 */             end = i;
/*     */           }
/* 334 */           expandQuery = expandQuery + tempQuery.substring(start, end);
/* 335 */           start = i + 1;
/*     */         }
/* 337 */         break;
/*     */       default:
/* 340 */         if ((isLiteral) || ((!enableSpellCheck) && (!enableCjkCompoundSearch)))
/*     */           continue;
/* 342 */         boolean tempIsLetter = Character.isLetter(ch);
/* 343 */         if (tempIsLetter)
/*     */         {
/* 345 */           if (isLetter)
/*     */             continue;
/* 347 */           if (i >= start)
/*     */           {
/* 349 */             expandQuery = expandQuery + tempQuery.substring(start, i);
/* 350 */             start = i;
/*     */           }
/* 352 */           isLetter = true;
/* 353 */           letterStart = i;
/*     */         }
/*     */         else
/*     */         {
/* 358 */           if ((isLetter) && (((ch == ' ') || (ch == '(') || (ch == '!') || (ch == '&') || (ch == '!') || (ch == ')'))))
/*     */           {
/* 361 */             tempSubQuery = retriveSubQuery(tempQuery, langId, letterStart, i, isCjk, enableCjkCompoundSearch, enableSpellCheck);
/*     */ 
/* 363 */             if ((tempSubQuery != null) && (tempSubQuery.length() > 0))
/*     */             {
/* 365 */               expandQuery = expandQuery + tempSubQuery;
/* 366 */               start = i + 1;
/*     */             }
/*     */             else
/*     */             {
/* 370 */               i = tempQuery.length();
/* 371 */               expandQuery = orgQuery;
/*     */             }
/*     */           }
/* 374 */           else if ((isLetter) && 
/* 376 */             (i > start))
/*     */           {
/* 378 */             expandQuery = expandQuery + tempQuery.substring(start, i + 1);
/* 379 */             start = i + 1;
/*     */           }
/*     */ 
/* 382 */           isLetter = false;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 390 */     if (isLiteral)
/*     */     {
/* 392 */       expandQuery = orgQuery;
/*     */     }
/* 394 */     else if (isLetter)
/*     */     {
/* 396 */       tempSubQuery = retriveSubQuery(tempQuery, langId, letterStart, tempQuery.length(), isCjk, enableCjkCompoundSearch, enableSpellCheck);
/*     */ 
/* 398 */       if ((tempSubQuery != null) && (tempSubQuery.length() > 0))
/*     */       {
/* 400 */         expandQuery = expandQuery + tempSubQuery;
/*     */       }
/*     */       else
/*     */       {
/* 404 */         expandQuery = orgQuery;
/*     */       }
/*     */     }
/* 407 */     else if (start < queryLen)
/*     */     {
/* 409 */       expandQuery = expandQuery + tempQuery.substring(start, queryLen);
/*     */     }
/*     */ 
/* 412 */     return expandQuery;
/*     */   }
/*     */ 
/*     */   protected String retriveSubQuery(String tempQuery, String langId, int letterStart, int i, boolean isCjk, boolean enableCjkCompoundSearch, boolean enableSpellCheck)
/*     */   {
/* 418 */     Vector words = new IdcVector();
/* 419 */     String subQuery = "";
/* 420 */     int queryLen = tempQuery.length();
/* 421 */     int end = i + 1;
/*     */ 
/* 423 */     if (end > queryLen)
/*     */     {
/* 425 */       end = i;
/*     */     }
/*     */ 
/* 428 */     String temp = tempQuery.substring(letterStart, i);
/* 429 */     if ((temp != null) && (temp.length() > 0))
/*     */     {
/* 432 */       boolean isOp = false;
/* 433 */       for (int j = 0; j < this.AVS_OPS.length; ++j)
/*     */       {
/* 435 */         if (temp.trim().equalsIgnoreCase(this.AVS_OPS[j]) != true)
/*     */           continue;
/* 437 */         isOp = true;
/* 438 */         break;
/*     */       }
/*     */ 
/* 444 */       if (!isOp)
/*     */       {
/* 446 */         if ((isCjk) && (enableCjkCompoundSearch))
/*     */         {
/* 448 */           words = getCJKCompoundWords(temp.trim(), langId);
/*     */         }
/* 450 */         else if (enableSpellCheck)
/*     */         {
/* 452 */           words = getCorrectSpelling(temp.trim(), langId);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 457 */     if (words.size() > 0)
/*     */     {
/* 459 */       String tempSubQuery = formatWordsToQuery(words, temp, true, isCjk);
/* 460 */       if ((tempSubQuery != null) && (tempSubQuery.length() > 0))
/*     */       {
/* 462 */         subQuery = subQuery + tempSubQuery;
/*     */       }
/*     */ 
/* 465 */       if (i < queryLen)
/*     */       {
/* 467 */         subQuery = subQuery + tempQuery.charAt(i);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 472 */       subQuery = subQuery + tempQuery.substring(letterStart, end);
/*     */     }
/*     */ 
/* 475 */     return subQuery;
/*     */   }
/*     */ 
/*     */   protected String formatWordsToQuery(Vector words, String word, boolean isInculdeWord, boolean isCjk) {
/* 479 */     String query = "";
/* 480 */     int vSize = words.size();
/* 481 */     if (vSize > 0)
/*     */     {
/* 483 */       query = query + "(";
/*     */ 
/* 485 */       if ((isInculdeWord) && (((!isCjk) || (vSize > 1))))
/*     */       {
/* 487 */         query = query + word + " | ";
/*     */       }
/*     */ 
/* 490 */       for (int k = 0; k < vSize; ++k)
/*     */       {
/* 492 */         String temp = (String)words.elementAt(k);
/*     */ 
/* 494 */         if ((temp == null) || (temp.length() <= 0))
/*     */           continue;
/* 496 */         query = query + temp;
/* 497 */         if (k < vSize - 1)
/*     */         {
/* 499 */           if (isCjk)
/*     */           {
/* 501 */             query = query + " & ";
/*     */           }
/*     */           else
/*     */           {
/* 505 */             query = query + " | ";
/*     */           }
/*     */ 
/*     */         }
/*     */         else {
/* 510 */           query = query + ")";
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 516 */     return query;
/*     */   }
/*     */ 
/*     */   protected String binderToString(DataBinder data, String errMsg) throws DataException {
/* 520 */     IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*     */     try
/*     */     {
/* 524 */       data.send(sw);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 528 */       throw new DataException(errMsg, e);
/*     */     }
/*     */ 
/* 531 */     return sw.toStringRelease();
/*     */   }
/*     */ 
/*     */   public Vector getStemWords(String word, String lang) {
/* 535 */     return parseWords(this.m_connection.getStemWords(word, lang, SearchLoader.m_encoding));
/*     */   }
/*     */ 
/*     */   public Vector getThesaurus(String word, String lang)
/*     */   {
/* 540 */     return parseWords(this.m_connection.getThesaurus(word, lang, SearchLoader.m_encoding));
/*     */   }
/*     */ 
/*     */   public Vector getCorrectSpelling(String word, String lang)
/*     */   {
/* 545 */     return parseWords(this.m_connection.getCorrectSpelling(word, lang, SearchLoader.m_encoding));
/*     */   }
/*     */ 
/*     */   public Vector getCJKCompoundWords(String phrase, String lang)
/*     */   {
/* 550 */     return parseWords(this.m_connection.getCJKCompoundWords(phrase, lang, SearchLoader.m_encoding));
/*     */   }
/*     */ 
/*     */   public String convertCJKQuery(String query)
/*     */   {
/* 555 */     return this.m_connection.convertCJKQuery(query, SearchLoader.m_encoding);
/*     */   }
/*     */ 
/*     */   protected Vector parseWords(String words)
/*     */   {
/* 560 */     Vector v = new IdcVector();
/*     */ 
/* 562 */     if (words != null)
/*     */     {
/* 564 */       v = StringUtils.parseArray(words, ',', ',');
/*     */     }
/*     */ 
/* 567 */     return v;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 573 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84156 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.AltavistaSearchImplementor
 * JD-Core Version:    0.5.4
 */