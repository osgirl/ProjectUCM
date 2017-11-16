/*      */ package intradoc.search;
/*      */ 
/*      */ import intradoc.common.DynamicHtmlMerger;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DatabaseTypes;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.SearchIndexerUtils;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.shared.ActiveIndexState;
/*      */ import intradoc.shared.CommonSearchConfig;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import java.io.IOException;
/*      */ import java.util.Date;
/*      */ import java.util.Iterator;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ 
/*      */ public class DBSearchConfigCompanion extends CommonSearchConfigCompanionAdaptor
/*      */ {
/*      */   protected final char[][][] FTSEARCHCONJUNCTION;
/*      */   protected final char[][][] DATABASE_TEXT_EXTRA;
/*      */   protected final int SQLSERVER = 0;
/*      */   protected final int ORACLE = 1;
/*      */   protected final int DB2 = 2;
/*      */   protected String m_suffix;
/*      */   protected int m_type;
/*      */   protected static final int METADATA = 0;
/*      */   protected static final int FULLTEXT = 1;
/*  106 */   protected static Map[] m_monitoredFieldsArray = { new ConcurrentHashMap(), new ConcurrentHashMap() };
/*      */ 
/*  108 */   protected static long[] m_monitoredOperatorsArray = { 0L, 0L };
/*      */ 
/*  110 */   protected static boolean[] m_initedArray = { false, false };
/*      */   protected int m_engineIndex;
/*      */ 
/*      */   public DBSearchConfigCompanion()
/*      */   {
/*   33 */     this.FTSEARCHCONJUNCTION = new char[][][] { { { ' ', 'A', 'N', 'D', ' ' }, { ' ', 'O', 'R', ' ' }, { ' ', 'A', 'N', 'D', ' ', 'N', 'O', 'T', ' ' }, { ' ', '*', ' ', 'A', 'N', 'D', ' ', 'N', 'O', 'T', ' ' }, { '*' }, { '*' } }, { { ' ', 'A', 'N', 'D', ' ' }, { ' ', 'O', 'R', ' ' }, { ' ', 'N', 'O', 'T', ' ' }, { ' ', '%', ' ', 'N', 'O', 'T', ' ' }, { '%' }, { '_' } }, { { ' ', '&', ' ' }, { ' ', '|', ' ' }, { ' ', 'N', 'O', 'T', ' ' }, { ' ', 'N', 'O', 'T', ' ' }, { '%' }, { '_' } } };
/*      */ 
/*   65 */     this.DATABASE_TEXT_EXTRA = new char[][][] { { { 'A', 'N', 'D' }, { ' ', 'O', 'R', ' ' }, { 'A', 'N', 'D', ' ', 'N', 'O', 'T' }, { '*', ' ', 'A', 'N', 'D', ' ', 'N', 'O', 'T' }, { '*' }, { '*' } }, { { 'A', 'N', 'D' }, { 'O', 'R' }, { 'N', 'O', 'T' }, { '%', ' ', 'N', 'O', 'T' }, { '%' }, { '_' } }, { { '&' }, { '|' }, { 'N', 'O', 'T' }, { 'N', 'O', 'T' }, { '%' }, { '_' } } };
/*      */ 
/*   96 */     this.SQLSERVER = 0;
/*   97 */     this.ORACLE = 1;
/*   98 */     this.DB2 = 2;
/*      */ 
/*  100 */     this.m_suffix = null;
/*  101 */     this.m_type = 0;
/*      */ 
/*  112 */     this.m_engineIndex = 0;
/*      */   }
/*      */ 
/*      */   public void init(CommonSearchConfig cfg) throws ServiceException {
/*  116 */     if (!this.m_isInited)
/*      */     {
/*  119 */       this.m_isInited = true;
/*      */     }
/*  121 */     super.init(cfg);
/*      */ 
/*  123 */     this.m_callbackFilterName = "databaseMetaDataQueryParserCallbackFilter";
/*  124 */     String engineName = cfg.getCurrentEngineName();
/*  125 */     if ((engineName.toLowerCase().startsWith("database.fulltext")) || (engineName.equalsIgnoreCase("DatabaseFullText")))
/*      */     {
/*  128 */       this.m_engineIndex = 1;
/*  129 */       this.m_callbackFilterName = "databaseFullTextQueryParserCallbackFilter";
/*      */     }
/*      */ 
/*  133 */     if (m_initedArray[this.m_engineIndex] != 0)
/*      */       return;
/*  135 */     m_monitoredOperatorsArray[this.m_engineIndex] = initCallbackMonitor(m_monitoredFieldsArray[this.m_engineIndex], m_monitoredOperatorsArray[this.m_engineIndex]);
/*      */ 
/*  138 */     m_initedArray[this.m_engineIndex] = true;
/*      */   }
/*      */ 
/*      */   public int prepareQuery(DataBinder binder, ExecutionContext ctxt)
/*      */     throws DataException
/*      */   {
/*  145 */     String activeIndex = ActiveIndexState.getActiveProperty("ActiveIndex");
/*  146 */     binder.putLocal("ActiveIndex", activeIndex);
/*      */ 
/*  148 */     String hasCreateDateInSearchCollection = ActiveIndexState.getActiveProperty("SearchCollectionContainsCreateDate");
/*  149 */     if (hasCreateDateInSearchCollection != null)
/*      */     {
/*  151 */       binder.putLocal("HasCreateDateInSearchCollection", hasCreateDateInSearchCollection);
/*      */     }
/*  153 */     return 0;
/*      */   }
/*      */ 
/*      */   public int prepareQueryText(DataBinder binder, ExecutionContext ctxt)
/*      */     throws DataException
/*      */   {
/*      */     try
/*      */     {
/*  162 */       this.m_type = getDBType(ctxt);
/*  163 */       addDatabaseTypeToBinder(binder);
/*      */ 
/*  165 */       this.m_suffix = getDBName(ctxt).toUpperCase();
/*  166 */       this.m_textExtras = this.DATABASE_TEXT_EXTRA[this.m_type];
/*  167 */       ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/*  168 */       cxt.setParentContext(ctxt);
/*      */ 
/*  170 */       String engine = SearchIndexerUtils.getSearchEngineName(cxt);
/*  171 */       this.m_config.preParseQueryWithSuffix(engine, this.m_suffix, cxt);
/*  172 */       this.m_queryDefinitionLabel = (engine + "." + this.m_suffix);
/*  173 */       binder.putLocal("queryDefinitionLabel", this.m_queryDefinitionLabel);
/*      */ 
/*  175 */       super.prepareQueryText(binder, ctxt);
/*  176 */       return 0;
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  180 */       throw new DataException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addDatabaseTypeToBinder(DataBinder binder)
/*      */   {
/*  186 */     if (this.m_type == 1)
/*      */     {
/*  188 */       binder.putLocal("IsOracle", "1");
/*      */     }
/*  190 */     else if (this.m_type == 2)
/*      */     {
/*  192 */       binder.putLocal("IsDB2", "1");
/*      */     }
/*      */     else
/*      */     {
/*  196 */       binder.putLocal("IsSqlServer", "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   public int fixUpAndValidateQuery(DataBinder binder, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  203 */     String query = binder.getLocal("QueryText");
/*      */ 
/*  205 */     IdcDateFormat fmt = LocaleResources.m_dbFormat;
/*  206 */     ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/*  207 */     cxt.setParentContext(ctxt);
/*  208 */     cxt.setCachedObject("UserDateFormat", binder.m_blDateFormat);
/*      */ 
/*  211 */     String queryFormat = SearchIndexerUtils.getSearchQueryFormat(binder, null);
/*  212 */     if ((query != null) && (((queryFormat == null) || (!SharedObjects.getEnvValueAsBoolean("AllowNativeQueryFormat", false)) || (queryFormat.equalsIgnoreCase("Universal")))))
/*      */     {
/*  214 */       query = this.m_parser.parse(binder, cxt);
/*  215 */       if (query != null)
/*      */       {
/*  217 */         binder.putLocal("QueryText", query);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  222 */       query = translateTaggedQuery(query, this.m_queryDefinitionLabel, binder, ctxt);
/*      */ 
/*  224 */       if ((query != null) && (query.toLowerCase().contains("select")))
/*      */       {
/*  226 */         String lquery = query.toLowerCase();
/*  227 */         int nOpenQuotes = 0;
/*  228 */         int lastPos = 0;
/*  229 */         int pos = lquery.indexOf("select", lastPos);
/*  230 */         while (pos >= 0)
/*      */         {
/*  232 */           for (int i = lastPos; i < pos; ++i)
/*      */           {
/*  234 */             if (lquery.charAt(i) != '\'')
/*      */               continue;
/*  236 */             ++nOpenQuotes;
/*      */           }
/*      */ 
/*  240 */           if (nOpenQuotes % 2 == 0)
/*      */           {
/*  242 */             String errorMsg = LocaleUtils.encodeMessage("csQueryInvalidWords", null, query);
/*  243 */             throw new ServiceException(null, errorMsg, new Object[0]);
/*      */           }
/*      */ 
/*  246 */           lastPos = pos + 6;
/*  247 */           pos = lquery.indexOf("select", lastPos);
/*      */         }
/*      */       }
/*  250 */       binder.putLocal("TranslatedQueryText", query);
/*      */ 
/*  252 */       String allowSearchCacheInFullTextQuery = this.m_config.getEngineValue("AllowSearchCacheInFullTextQuery");
/*  253 */       String containsFullText = binder.getLocal("ftx");
/*  254 */       if ((!StringUtils.convertToBool(allowSearchCacheInFullTextQuery, false)) && (StringUtils.convertToBool(containsFullText, false)) && (!this.m_config.getCurrentEngineName().equalsIgnoreCase("DATABASE")) && (!this.m_config.getCurrentEngineName().equalsIgnoreCase("DATABASE.METADATA")))
/*      */       {
/*  260 */         binder.putLocal("UseSearchCache", "false");
/*      */       }
/*      */ 
/*  263 */       Map types = binder.getFieldTypes();
/*  264 */       Iterator it = types.keySet().iterator();
/*  265 */       while (it.hasNext())
/*      */       {
/*  267 */         String key = (String)it.next();
/*  268 */         String type = (String)types.get(key);
/*  269 */         if (type.equalsIgnoreCase("date"))
/*      */         {
/*  271 */           int i = query.indexOf(key);
/*  272 */           while (i >= 0)
/*      */           {
/*  274 */             int index = i;
/*  275 */             i = index + key.length();
/*  276 */             String pre = query.substring(0, index);
/*  277 */             String tmp = query.substring(index + key.length()).trim();
/*      */ 
/*  279 */             index = 0;
/*  280 */             while ((index < tmp.length()) && (((tmp.charAt(index) == '>') || (tmp.charAt(index) == '<') || (tmp.charAt(index) == '='))))
/*      */             {
/*  283 */               ++index;
/*      */             }
/*  285 */             if ((index <= 0) || (index == tmp.length()))
/*      */             {
/*  287 */               i = query.indexOf(key, i);
/*      */             }
/*      */ 
/*  290 */             String op = tmp.substring(0, index);
/*  291 */             tmp = tmp.substring(index).trim();
/*      */ 
/*  293 */             boolean hasQuote = false;
/*  294 */             if (!tmp.startsWith("'"))
/*      */             {
/*  298 */               index = 0;
/*  299 */               boolean isEnd = false;
/*  300 */               boolean isDateField = true;
/*  301 */               while (index < tmp.length())
/*      */               {
/*  303 */                 isEnd = false;
/*  304 */                 isDateField = true;
/*  305 */                 switch (tmp.charAt(index))
/*      */                 {
/*      */                 case 'A':
/*      */                 case 'a':
/*  309 */                   if ((index > 0) && (index < tmp.length() - 2) && (Validation.isSpace(tmp.charAt(index - 1))) && (((tmp.charAt(index + 1) == 'N') || (tmp.charAt(index + 1) == 'n'))) && (((tmp.charAt(index + 2) == 'D') || (tmp.charAt(index + 2) == 'd'))))
/*      */                   {
/*  314 */                     isEnd = true; } break;
/*      */                 case 'O':
/*      */                 case 'o':
/*  319 */                   if ((index > 0) && (index < tmp.length() - 1) && (Validation.isSpace(tmp.charAt(index - 1))) && (((tmp.charAt(index + 1) == 'R') || (tmp.charAt(index + 1) == 'r'))))
/*      */                   {
/*  323 */                     isEnd = true; } break;
/*      */                 case ')':
/*  328 */                   isEnd = true;
/*  329 */                   break;
/*      */                 case '<':
/*      */                 case '=':
/*      */                 case '>':
/*  334 */                   isDateField = false;
/*      */                 case 'I':
/*      */                 case 'i':
/*  337 */                   if ((index > 0) && (index < tmp.length() - 1) && (Validation.isSpace(tmp.charAt(index - 1))) && (((tmp.charAt(index + 1) == 'N') || (tmp.charAt(index + 1) == 'n'))))
/*      */                   {
/*  342 */                     isDateField = false; } break;
/*      */                 case 'L':
/*      */                 case 'l':
/*  348 */                   if ((index > 0) && (index < tmp.length() - 3) && (Validation.isSpace(tmp.charAt(index - 1))) && (((tmp.charAt(index + 1) == 'I') || (tmp.charAt(index + 1) == 'i'))) && (((tmp.charAt(index + 2) == 'K') || (tmp.charAt(index + 2) == 'k'))) && (((tmp.charAt(index + 3) == 'E') || (tmp.charAt(index + 3) == 'e'))))
/*      */                   {
/*  355 */                     isDateField = false; } break;
/*      */                 case 'E':
/*      */                 case 'e':
/*  361 */                   if ((index > 0) && (index < tmp.length() - 4) && (Validation.isSpace(tmp.charAt(index - 1))) && (((tmp.charAt(index + 1) == 'X') || (tmp.charAt(index + 1) == 'x'))) && (((tmp.charAt(index + 2) == 'I') || (tmp.charAt(index + 2) == 'i'))) && (((tmp.charAt(index + 3) == 'S') || (tmp.charAt(index + 3) == 's'))) && (((tmp.charAt(index + 4) == 'T') || (tmp.charAt(index + 4) == 't'))))
/*      */                   {
/*  369 */                     isDateField = false; } break;
/*      */                 case 'B':
/*      */                 case 'b':
/*  374 */                   if ((index > 0) && (index < tmp.length() - 4) && (Validation.isSpace(tmp.charAt(index - 1))) && (((tmp.charAt(index + 1) == 'E') || (tmp.charAt(index + 1) == 'e'))) && (((tmp.charAt(index + 2) == 'T') || (tmp.charAt(index + 2) == 't'))) && (((tmp.charAt(index + 3) == 'W') || (tmp.charAt(index + 3) == 'w'))) && (((tmp.charAt(index + 4) == 'E') || (tmp.charAt(index + 4) == 'e'))) && (((tmp.charAt(index + 5) == 'E') || (tmp.charAt(index + 5) == 'e'))) && (((tmp.charAt(index + 6) == 'N') || (tmp.charAt(index + 6) == 'n'))))
/*      */                   {
/*  382 */                     isDateField = false;
/*      */                   }
/*      */ 
/*      */                 }
/*      */ 
/*  387 */                 if (!isDateField) {
/*      */                   break;
/*      */                 }
/*      */ 
/*  391 */                 if (isEnd) {
/*      */                   break;
/*      */                 }
/*      */ 
/*  395 */                 ++index;
/*      */               }
/*  397 */               if (!isDateField)
/*      */               {
/*  399 */                 i = query.indexOf(key, i);
/*      */               }
/*      */ 
/*      */             }
/*      */             else
/*      */             {
/*  405 */               tmp = tmp.substring(1);
/*  406 */               index = tmp.indexOf("'");
/*  407 */               if (index < 0)
/*      */               {
/*  409 */                 i = query.indexOf(key, i);
/*      */               }
/*      */ 
/*  412 */               hasQuote = true;
/*      */             }
/*  414 */             String value = tmp.substring(0, index);
/*  415 */             String post = "";
/*  416 */             if (hasQuote)
/*      */             {
/*  419 */               ++index;
/*      */             }
/*  421 */             if (index < tmp.length())
/*      */             {
/*  423 */               post = tmp.substring(index);
/*      */             }
/*      */ 
/*      */             try
/*      */             {
/*  428 */               value = value.trim();
/*  429 */               if ((!value.startsWith("{ts '")) || (!value.endsWith("'}")))
/*      */               {
/*  431 */                 Date d = LocaleResources.parseDateDataEntry(value, cxt, "fixUpAndValidateQuery");
/*  432 */                 value = fmt.format(d);
/*      */               }
/*      */             }
/*      */             catch (ServiceException e)
/*      */             {
/*  437 */               throw new DataException(e.getMessage());
/*      */             }
/*  439 */             query = pre + key + " " + op + " " + value + " ";
/*  440 */             i = query.length();
/*  441 */             query = query + post;
/*  442 */             i = query.indexOf(key, i);
/*      */           }
/*      */         }
/*      */       }
/*  446 */       binder.putLocal("QueryText", query);
/*      */     }
/*  448 */     return 1;
/*      */   }
/*      */ 
/*      */   public void translateFullTextQuery(IdcAppendable appendable, char[] queryChars, DataBinder binder, ParsedQueryElements parsedElts, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*  455 */     int db = getDBType(context);
/*      */ 
/*  457 */     char wordPrefix = '"';
/*  458 */     char wordSuffix = '"';
/*      */ 
/*  460 */     boolean isError = false;
/*  461 */     String msg = "";
/*      */ 
/*  463 */     boolean couldWriteBuffer = false;
/*  464 */     boolean allowConjunction = false;
/*  465 */     boolean isChangeAllowConjunction = false;
/*  466 */     boolean hasPrevTerm = false;
/*  467 */     boolean isOpenQuote = false;
/*  468 */     int numOpenParen = 0;
/*  469 */     boolean noConversion = false;
/*  470 */     boolean canBeginWord = true;
/*  471 */     boolean useWordEnclose = false;
/*  472 */     boolean isFullyDefinedFullTextTerm = StringUtils.convertToBool(binder.getAllowMissing("IsFullyDefinedFullTextTerm"), false);
/*      */ 
/*  475 */     int lastIndex = 0;
/*  476 */     int skipCount = 0;
/*  477 */     char[] replaceChars = null;
/*  478 */     char[] delayedReplaceChars = null;
/*      */ 
/*  480 */     IdcStringBuilder buf = new IdcStringBuilder(queryChars.length);
/*      */ 
/*  482 */     int i = 0;
/*  483 */     for (; (i < queryChars.length) && (!isFullyDefinedFullTextTerm) && (!noConversion); ++i)
/*      */     {
/*  485 */       if (allowConjunction)
/*      */       {
/*  487 */         hasPrevTerm = false;
/*      */       }
/*      */ 
/*  490 */       isChangeAllowConjunction = true;
/*  491 */       useWordEnclose = false;
/*      */ 
/*  493 */       boolean isNotOp = false;
/*  494 */       switch (queryChars[i])
/*      */       {
/*      */       case '<':
/*      */       case '=':
/*      */       case '>':
/*  503 */         if (!isOpenQuote)
/*      */         {
/*  506 */           noConversion = true; } break;
/*      */       case '\t':
/*      */       case '\n':
/*      */       case '\r':
/*      */       case ' ':
/*  513 */         if (!isOpenQuote) if (allowConjunction)
/*      */           {
/*  517 */             skipCount = 0;
/*  518 */             boolean addAnd = true;
/*  519 */             for (int j = i; j < queryChars.length; ++j)
/*      */             {
/*  521 */               if ((queryChars[j] == ' ') || (queryChars[j] == '\t') || (queryChars[j] == '\n') || (queryChars[j] == '\r'))
/*      */               {
/*  524 */                 ++skipCount;
/*      */               }
/*      */               else {
/*  527 */                 if ((queryChars[j] != ',') && (queryChars[j] != ')') && (queryChars[j] != '-') && ((((queryChars[j] != 'O') && (queryChars[j] != 'o')) || ((((j >= queryChars.length - 1) || ((queryChars[(j + 1)] != 'R') && (queryChars[(j + 1)] != 'r')) || ((j <= queryChars.length - 2) && (Character.isLetterOrDigit(queryChars[(j + 2)]))))) && (((j >= queryChars.length - 2) || ((queryChars[j] != 'N') && (queryChars[j] != 'n')) || ((queryChars[(j + 1)] != 'O') && (queryChars[(j + 1)] != 'o')) || ((queryChars[(j + 2)] != 'T') && (queryChars[(j + 2)] != 't')) || ((j <= queryChars.length - 3) && (Character.isLetterOrDigit(queryChars[(j + 3)])))))))))
/*      */                 {
/*      */                   break;
/*      */                 }
/*      */ 
/*  537 */                 addAnd = false;
/*  538 */                 if (queryChars[j] != '-')
/*      */                   break;
/*  540 */                 hasPrevTerm = true;
/*  541 */                 allowConjunction = false; break;
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*  547 */             if (skipCount + i == queryChars.length)
/*      */             {
/*  549 */               skipCount = 0;
/*  550 */               addAnd = false;
/*      */             }
/*      */ 
/*  553 */             if (addAnd)
/*      */             {
/*  555 */               replaceChars = this.FTSEARCHCONJUNCTION[db][0];
/*  556 */               allowConjunction = false;
/*      */             }
/*      */ 
/*  559 */             couldWriteBuffer = true;
/*      */           } break;
/*      */       case ',':
/*  562 */         if (!isOpenQuote)
/*      */         {
/*  566 */           if (!allowConjunction)
/*      */           {
/*  568 */             skipCount = 1;
/*  569 */             couldWriteBuffer = true;
/*      */           }
/*      */           else
/*      */           {
/*  573 */             for (int j = i; (j < queryChars.length) && ((
/*  575 */               (queryChars[j] == ' ') || (queryChars[j] == '\t') || (queryChars[j] == '\n') || (queryChars[j] == '\r') || (queryChars[j] == ','))); ++j)
/*      */             {
/*  579 */               ++skipCount;
/*      */             }
/*      */ 
/*  584 */             replaceChars = this.FTSEARCHCONJUNCTION[db][1];
/*  585 */             allowConjunction = false;
/*  586 */             couldWriteBuffer = true;
/*      */           }
/*  587 */         }break;
/*      */       case '-':
/*  589 */         if ((!isOpenQuote) && (i < queryChars.length - 1) && (Character.isLetterOrDigit(queryChars[(i + 1)])) && (((i == 0) || (!Character.isLetterOrDigit(queryChars[(i - 1)])))))
/*      */         {
/*  592 */           isNotOp = true;
/*  593 */           skipCount = 1;
/*      */         }
/*      */       case 'N':
/*      */       case 'n':
/*  601 */         if ((canBeginWord) && (!isOpenQuote) && (!isNotOp) && ((
/*  603 */           ((queryChars.length > i + 3) && (!Character.isLetter(queryChars[(i + 3)]))) || ((queryChars.length == i + 3) && (((queryChars[(i + 1)] == 'O') || (queryChars[(i + 1)] == 'o'))) && (((queryChars[(i + 2)] == 'T') || (queryChars[(i + 2)] == 't')))))))
/*      */         {
/*  610 */           isNotOp = true;
/*  611 */           skipCount = 3;
/*      */         }
/*      */ 
/*  615 */         if (isNotOp)
/*      */         {
/*  617 */           couldWriteBuffer = true;
/*  618 */           delayedReplaceChars = this.FTSEARCHCONJUNCTION[db][3];
/*  619 */           if (hasPrevTerm)
/*      */           {
/*  621 */             delayedReplaceChars = this.FTSEARCHCONJUNCTION[db][2];
/*  622 */             hasPrevTerm = false;
/*      */           }
/*  624 */           allowConjunction = false;
/*  625 */           isChangeAllowConjunction = false; } break;
/*      */       case '*':
/*  629 */         if ((db == 1) || (db == 2))
/*      */         {
/*  631 */           skipCount = 1;
/*  632 */           couldWriteBuffer = true;
/*  633 */           replaceChars = this.FTSEARCHCONJUNCTION[db][4];
/*      */         }
/*  635 */         else if ((db == 0) && (!isOpenQuote))
/*      */         {
/*  637 */           useWordEnclose = true;
/*  638 */           skipCount = 1;
/*  639 */           replaceChars = this.FTSEARCHCONJUNCTION[db][4];
/*  640 */           couldWriteBuffer = true; } break;
/*      */       case '?':
/*  644 */         skipCount = 1;
/*  645 */         couldWriteBuffer = true;
/*  646 */         replaceChars = this.FTSEARCHCONJUNCTION[db][5];
/*  647 */         if ((db == 0) && (!isOpenQuote))
/*      */         {
/*  649 */           useWordEnclose = true; } break;
/*      */       case 'A':
/*      */       case 'a':
/*  654 */         if ((canBeginWord) && (!isOpenQuote) && ((
/*  656 */           ((queryChars.length > i + 3) && (!Character.isLetter(queryChars[(i + 3)]))) || ((queryChars.length == i + 3) && (((queryChars[(i + 1)] == 'N') || (queryChars[(i + 1)] == 'n'))) && (((queryChars[(i + 2)] == 'D') || (queryChars[(i + 2)] == 'd')))))))
/*      */         {
/*  663 */           skipCount = 3;
/*  664 */           couldWriteBuffer = true;
/*  665 */           if (allowConjunction)
/*      */           {
/*  668 */             couldWriteBuffer = true;
/*  669 */             replaceChars = this.FTSEARCHCONJUNCTION[db][0];
/*      */           }
/*  671 */           allowConjunction = false;
/*  672 */           isChangeAllowConjunction = false; } break;
/*      */       case 'O':
/*      */       case 'o':
/*  678 */         if ((canBeginWord) && (!isOpenQuote) && ((
/*  680 */           ((queryChars.length > i + 2) && (!Character.isLetter(queryChars[(i + 2)]))) || ((queryChars.length == i + 2) && (((queryChars[(i + 1)] == 'R') || (queryChars[(i + 1)] == 'r')))))))
/*      */         {
/*  685 */           skipCount = 2;
/*  686 */           couldWriteBuffer = true;
/*      */ 
/*  688 */           if (allowConjunction)
/*      */           {
/*  691 */             couldWriteBuffer = true;
/*  692 */             replaceChars = this.FTSEARCHCONJUNCTION[db][1];
/*      */           }
/*  694 */           allowConjunction = false;
/*  695 */           isChangeAllowConjunction = false; } break;
/*      */       case 'I':
/*      */       case 'i':
/*  702 */         if ((canBeginWord) && (!isOpenQuote) && ((
/*  704 */           ((queryChars.length > i + 2) && (!Character.isLetter(queryChars[(i + 2)]))) || ((queryChars.length == i + 2) && (((queryChars[(i + 1)] == 'n') || (queryChars[(i + 1)] == 'n')))))))
/*      */         {
/*  709 */           for (int j = i + 2; j < queryChars.length; ++j)
/*      */           {
/*  711 */             if (Character.isWhitespace(queryChars[j])) {
/*      */               continue;
/*      */             }
/*      */ 
/*  715 */             if (queryChars[j] != '(')
/*      */               break;
/*  717 */             noConversion = true; break;
/*      */           }
/*      */         }
/*  709 */         break;
/*      */       case '&':
/*      */       case '|':
/*  726 */         if ((db == 2) && (!isOpenQuote))
/*      */         {
/*  728 */           noConversion = true; } break;
/*      */       case 'L':
/*      */       case 'l':
/*  733 */         if ((canBeginWord) && (!isOpenQuote) && ((
/*  735 */           ((queryChars.length > i + 4) && (!Character.isLetter(queryChars[(i + 4)]))) || ((queryChars.length == i + 4) && (((queryChars[(i + 1)] == 'I') || (queryChars[(i + 1)] == 'i'))) && (((queryChars[(i + 2)] == 'K') || (queryChars[(i + 2)] == 'k'))) && (((queryChars[(i + 3)] == 'E') || (queryChars[(i + 3)] == 'e')))))))
/*      */         {
/*  742 */           for (int j = i + 4; j < queryChars.length; ++j)
/*      */           {
/*  744 */             if (Character.isWhitespace(queryChars[j])) {
/*      */               continue;
/*      */             }
/*      */ 
/*  748 */             if (queryChars[j] != '\'')
/*      */               break;
/*  750 */             noConversion = true; break;
/*      */           }
/*      */         }
/*  742 */         break;
/*      */       case '"':
/*  758 */         if (!isOpenQuote)
/*      */         {
/*  761 */           isOpenQuote = true;
/*      */         }
/*      */         else
/*      */         {
/*  765 */           isOpenQuote = false;
/*  766 */           allowConjunction = true;
/*      */         }
/*  768 */         if (db == 1)
/*      */         {
/*  771 */           skipCount = 1;
/*      */         }
/*  773 */         couldWriteBuffer = true;
/*  774 */         break;
/*      */       case '(':
/*  776 */         if (!isOpenQuote)
/*      */         {
/*  778 */           ++numOpenParen;
/*  779 */           if (allowConjunction)
/*      */           {
/*  781 */             replaceChars = this.FTSEARCHCONJUNCTION[db][0];
/*  782 */             allowConjunction = false;
/*  783 */             couldWriteBuffer = true; } 
/*  783 */         }break;
/*      */       case ')':
/*  788 */         if (!isOpenQuote)
/*      */         {
/*  790 */           --numOpenParen;
/*  791 */           allowConjunction = true; } case '\013':
/*      */       case '\f':
/*      */       case '\016':
/*      */       case '\017':
/*      */       case '\020':
/*      */       case '\021':
/*      */       case '\022':
/*      */       case '\023':
/*      */       case '\024':
/*      */       case '\025':
/*      */       case '\026':
/*      */       case '\027':
/*      */       case '\030':
/*      */       case '\031':
/*      */       case '\032':
/*      */       case '\033':
/*      */       case '\034':
/*      */       case '\035':
/*      */       case '\036':
/*      */       case '\037':
/*      */       case '!':
/*      */       case '#':
/*      */       case '$':
/*      */       case '%':
/*      */       case '\'':
/*      */       case '+':
/*      */       case '.':
/*      */       case '/':
/*      */       case '0':
/*      */       case '1':
/*      */       case '2':
/*      */       case '3':
/*      */       case '4':
/*      */       case '5':
/*      */       case '6':
/*      */       case '7':
/*      */       case '8':
/*      */       case '9':
/*      */       case ':':
/*      */       case ';':
/*      */       case '@':
/*      */       case 'B':
/*      */       case 'C':
/*      */       case 'D':
/*      */       case 'E':
/*      */       case 'F':
/*      */       case 'G':
/*      */       case 'H':
/*      */       case 'J':
/*      */       case 'K':
/*      */       case 'M':
/*      */       case 'P':
/*      */       case 'Q':
/*      */       case 'R':
/*      */       case 'S':
/*      */       case 'T':
/*      */       case 'U':
/*      */       case 'V':
/*      */       case 'W':
/*      */       case 'X':
/*      */       case 'Y':
/*      */       case 'Z':
/*      */       case '[':
/*      */       case '\\':
/*      */       case ']':
/*      */       case '^':
/*      */       case '_':
/*      */       case '`':
/*      */       case 'b':
/*      */       case 'c':
/*      */       case 'd':
/*      */       case 'e':
/*      */       case 'f':
/*      */       case 'g':
/*      */       case 'h':
/*      */       case 'j':
/*      */       case 'k':
/*      */       case 'm':
/*      */       case 'p':
/*      */       case 'q':
/*      */       case 'r':
/*      */       case 's':
/*      */       case 't':
/*      */       case 'u':
/*      */       case 'v':
/*      */       case 'w':
/*      */       case 'x':
/*      */       case 'y':
/*      */       case 'z':
/*      */       case '{': } if (numOpenParen < 0)
/*      */       {
/*  799 */         isError = true;
/*  800 */         msg = LocaleResources.getString("csSearchQueryTranslationMismatchParenExtraClose", context, new String(queryChars), new Integer(i));
/*  801 */         break;
/*      */       }
/*      */ 
/*  804 */       boolean isNotInWord = canBeginWord;
/*  805 */       canBeginWord = (!Character.isLetterOrDigit(queryChars[i])) && (!isOpenQuote);
/*      */ 
/*  807 */       if ((!allowConjunction) && (isChangeAllowConjunction) && (Character.isLetterOrDigit(queryChars[i])))
/*      */       {
/*  809 */         allowConjunction = true;
/*      */       }
/*  811 */       if ((db == 2) && 
/*  813 */         (isNotInWord != canBeginWord))
/*      */       {
/*  815 */         couldWriteBuffer = true;
/*  816 */         if ((canBeginWord) && (couldWriteBuffer))
/*      */         {
/*  818 */           useWordEnclose = true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  823 */       if (!couldWriteBuffer)
/*      */         continue;
/*  825 */       if ((replaceChars != null) && (delayedReplaceChars != null))
/*      */       {
/*  827 */         buf.append(delayedReplaceChars);
/*  828 */         delayedReplaceChars = null;
/*      */       }
/*  830 */       if (useWordEnclose)
/*      */       {
/*  832 */         buf.append(wordPrefix);
/*      */       }
/*  834 */       buf.append(queryChars, lastIndex, i - lastIndex);
/*      */ 
/*  836 */       if (replaceChars != null)
/*      */       {
/*  838 */         buf.append(replaceChars);
/*  839 */         replaceChars = null;
/*      */       }
/*  841 */       if (useWordEnclose)
/*      */       {
/*  843 */         buf.append(wordSuffix);
/*      */       }
/*  845 */       lastIndex = i + skipCount;
/*  846 */       if (skipCount > 0)
/*      */       {
/*  848 */         i += skipCount - 1;
/*  849 */         skipCount = 0;
/*      */       }
/*  851 */       couldWriteBuffer = false;
/*      */     }
/*      */ 
/*  855 */     if ((numOpenParen > 0) && (!noConversion))
/*      */     {
/*  857 */       isError = true;
/*  858 */       msg = LocaleResources.getString("csSearchQueryTranslationMismatchParenExtraOpen", context, new String(queryChars));
/*      */     }
/*  860 */     if (isError)
/*      */     {
/*  862 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  865 */     if (lastIndex < queryChars.length)
/*      */     {
/*  868 */       if (delayedReplaceChars != null)
/*      */       {
/*  870 */         for (int i = lastIndex; i < queryChars.length; ++i)
/*      */         {
/*  872 */           if (Character.isSpaceChar(queryChars[i]))
/*      */             continue;
/*  874 */           buf.append(delayedReplaceChars);
/*  875 */           break;
/*      */         }
/*      */       }
/*      */ 
/*  879 */       if ((db == 2) && (!canBeginWord))
/*      */       {
/*  881 */         useWordEnclose = true;
/*      */       }
/*  883 */       if (useWordEnclose)
/*      */       {
/*  885 */         buf.append(wordPrefix);
/*      */       }
/*  887 */       buf.append(queryChars, lastIndex, queryChars.length - lastIndex);
/*  888 */       if (useWordEnclose)
/*      */       {
/*  890 */         buf.append(wordSuffix);
/*      */       }
/*      */     }
/*      */ 
/*  894 */     if ((((!noConversion) || (isFullyDefinedFullTextTerm))) && (buf.length() != 0))
/*      */     {
/*      */       try
/*      */       {
/*  898 */         String query = this.m_config.parseElement("fullText", null, buf.toString());
/*  899 */         DynamicHtmlMerger dhtml = null;
/*  900 */         if (context == null)
/*      */         {
/*  902 */           context = new ExecutionContextAdaptor();
/*      */         }
/*      */         else
/*      */         {
/*  906 */           dhtml = (DynamicHtmlMerger)context.getCachedObject("PageMerger");
/*      */         }
/*  908 */         if (dhtml == null)
/*      */         {
/*  910 */           dhtml = new PageMerger(binder, context);
/*      */         }
/*      */         try
/*      */         {
/*  914 */           query = dhtml.evaluateScript(query);
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  918 */           Report.trace("search", null, e);
/*      */         }
/*  920 */         appendable.append(query);
/*  921 */         queryChars = query.toCharArray();
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  926 */         Report.trace("search", null, e);
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/*  931 */       appendable.append(queryChars, 0, queryChars.length);
/*      */     }
/*  933 */     buf.releaseBuffers();
/*  934 */     if ((!noConversion) || (isFullyDefinedFullTextTerm))
/*      */     {
/*  936 */       binder.putLocal("containsFullTextQuery", "true");
/*  937 */       binder.putLocal("ftx", "1");
/*      */     }
/*      */     else
/*      */     {
/*  941 */       binder.putLocal("containsFullTextQuery", "false");
/*  942 */       binder.putLocal("ftx", "0");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected int getDBType(ExecutionContext ctxt) throws ServiceException
/*      */   {
/*  948 */     Workspace ws = (Workspace)ctxt.getCachedObject("WorkSpace");
/*  949 */     if ((ws == null) && (ctxt instanceof Service))
/*      */     {
/*  951 */       ws = ((Service)ctxt).getWorkspace();
/*  952 */       if (ws == null)
/*      */       {
/*  954 */         throw new ServiceException("!csDBSearchWSNotAvailable");
/*      */       }
/*      */     }
/*  957 */     if (ws == null)
/*      */     {
/*  960 */       Report.trace("search", "Unable to find workspace in ExecutionContext, Unable to determine database, default to SQL Server", null);
/*      */ 
/*  962 */       return 0;
/*      */     }
/*      */ 
/*  966 */     if (WorkspaceUtils.isDatabaseType(ws, DatabaseTypes.ORACLE))
/*      */     {
/*  968 */       return 1;
/*      */     }
/*  970 */     if (WorkspaceUtils.isDatabaseType(ws, DatabaseTypes.DB2))
/*      */     {
/*  972 */       return 2;
/*      */     }
/*  974 */     return 0;
/*      */   }
/*      */ 
/*      */   protected String getDBName(ExecutionContext ctxt) throws ServiceException
/*      */   {
/*  979 */     Workspace ws = getWorkspace(ctxt);
/*  980 */     if (ws != null)
/*      */     {
/*  982 */       return ws.getProperty("DatabaseType");
/*      */     }
/*      */ 
/*  989 */     return "MSSQLServer";
/*      */   }
/*      */ 
/*      */   protected Workspace getWorkspace(ExecutionContext ctxt) throws ServiceException
/*      */   {
/*  994 */     Workspace ws = (Workspace)ctxt.getCachedObject("WorkSpace");
/*  995 */     if ((ws == null) && (ctxt instanceof Service))
/*      */     {
/*  997 */       ws = ((Service)ctxt).getWorkspace();
/*  998 */       if (ws == null)
/*      */       {
/* 1000 */         throw new ServiceException("!csDBSearchWSNotAvailable");
/*      */       }
/*      */     }
/* 1003 */     if (ws == null)
/*      */     {
/* 1006 */       Report.trace("search", "Unable to find workspace in ExecutionContext, Unable to determine database, default to SQL Server", null);
/*      */     }
/*      */ 
/* 1009 */     return ws;
/*      */   }
/*      */ 
/*      */   public boolean isQueryContainsNativeSyntax(char[] valueChars)
/*      */   {
/* 1015 */     boolean enableQueryNativeSyntax = SharedObjects.getEnvValueAsBoolean("EnableQueryNativeSyntax", false);
/*      */ 
/* 1017 */     if (!enableQueryNativeSyntax)
/*      */     {
/* 1019 */       return false;
/*      */     }
/*      */ 
/* 1022 */     boolean contains = false;
/* 1023 */     boolean isOpenQuote = false;
/* 1024 */     for (int i = 0; (i < valueChars.length) && (!contains); ++i)
/*      */     {
/* 1026 */       switch (valueChars[i])
/*      */       {
/*      */       case '"':
/* 1029 */         isOpenQuote = !isOpenQuote;
/* 1030 */         break;
/*      */       case '<':
/*      */       case '=':
/*      */       case '>':
/* 1038 */         if (isOpenQuote) {
/*      */           continue;
/*      */         }
/* 1041 */         contains = true; break;
/*      */       case 'I':
/*      */       case 'i':
/* 1046 */         if ((isOpenQuote) || ((i != 0) && (Character.isLetterOrDigit(valueChars[(i - 1)]))) || (
/* 1048 */           (((valueChars.length <= i + 2) || (Character.isLetterOrDigit(valueChars[(i + 2)])))) && (((valueChars.length != i + 2) || ((valueChars[(i + 1)] != 'n') && (valueChars[(i + 1)] != 'n'))))))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1053 */         for (int j = i + 2; j < valueChars.length; ++j)
/*      */         {
/* 1055 */           if (Character.isWhitespace(valueChars[j])) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1059 */           if (valueChars[j] != '(')
/*      */             break;
/* 1061 */           contains = true; break;
/*      */         }
/* 1053 */         break;
/*      */       case 'L':
/*      */       case 'l':
/* 1070 */         if ((isOpenQuote) || ((i != 0) && (Character.isLetterOrDigit(valueChars[(i - 1)]))) || (
/* 1072 */           (((valueChars.length <= i + 4) || (Character.isLetter(valueChars[(i + 4)])))) && (((valueChars.length != i + 4) || ((valueChars[(i + 1)] != 'I') && (valueChars[(i + 1)] != 'i')) || ((valueChars[(i + 2)] != 'K') && (valueChars[(i + 2)] != 'k')) || ((valueChars[(i + 3)] != 'E') && (valueChars[(i + 3)] != 'e'))))))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1079 */         for (int j = i + 4; j < valueChars.length; ++j)
/*      */         {
/* 1081 */           if (Character.isWhitespace(valueChars[j])) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1085 */           if (valueChars[j] != '\'')
/*      */             break;
/* 1087 */           contains = true; break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1096 */     return contains;
/*      */   }
/*      */ 
/*      */   public int appendQueryTextFilters(DataBinder binder, String filterStr)
/*      */     throws DataException
/*      */   {
/* 1103 */     addDatabaseTypeToBinder(binder);
/* 1104 */     return 0;
/*      */   }
/*      */ 
/*      */   public boolean isOperatorMonitored(long operator)
/*      */   {
/* 1110 */     return (m_monitoredOperatorsArray[this.m_engineIndex] & 1 << (int)operator) != 0L;
/*      */   }
/*      */ 
/*      */   public boolean isFieldMonitored(String fieldName)
/*      */   {
/* 1116 */     return (fieldName != null) && (m_monitoredFieldsArray[this.m_engineIndex].get(fieldName.toLowerCase()) != null);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1121 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96509 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.DBSearchConfigCompanion
 * JD-Core Version:    0.5.4
 */