/*      */ package intradoc.search;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IdcTimeZone;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.server.SearchLoader;
/*      */ import intradoc.shared.CommonSearchConfig;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import java.util.Date;
/*      */ import java.util.List;
/*      */ import java.util.Locale;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ 
/*      */ public class UniversalSearchQueryParser
/*      */ {
/*      */   public static final int EQUALS = 0;
/*      */   public static final int HASASSUBSTRING = 1;
/*      */   public static final int BEGINSWITH = 2;
/*      */   public static final int ENDSWITH = 3;
/*      */   public static final int HASASWORD = 4;
/*      */   public static final int FULLTEXT = 5;
/*      */   public static final int DATEGREATER = 6;
/*      */   public static final int DATEGE = 7;
/*      */   public static final int DATEEQUALS = 8;
/*      */   public static final int DATELE = 9;
/*      */   public static final int DATELESS = 10;
/*      */   public static final int NUMBERGREATER = 11;
/*      */   public static final int NUMBERGE = 12;
/*      */   public static final int NUMBEREQUALS = 13;
/*      */   public static final int NUMBERLE = 14;
/*      */   public static final int NUMBERLESS = 15;
/*      */   public static final int AND = 16;
/*      */   public static final int OR = 17;
/*      */   public static final int NOT = 18;
/*      */   public static final int OPERATOR_FILTER = 255;
/*      */   public static final int ZONEHASASWORD = 19;
/*      */   public static final int ZONEHASASWORDPREFIX = 20;
/*      */   public static final int NOT_FLAG = 256;
/*      */   public static final int CLAUSE = 100;
/*      */   public static final int COMPOUND = 101;
/*      */   public static final int QSCH = 200;
/*      */   public static final int ISCH = 201;
/*      */   public static final int FTX = 202;
/*      */   public static final int USCH = 203;
/*      */   public static final int NONE = -1;
/*      */   public static final int NUMBERDATEOPDIFF = 5;
/*      */   public static final long META_TEXT_OPS = 1572895L;
/*      */   public static final long META_DATE_OPS = 960L;
/*      */   public static final long META_NUMBER_OPS = 64512L;
/*      */   public static final long CONJUNCTIONS = 458752L;
/*      */   public static final long ALL_OPS = -266338305L;
/*   80 */   public static final String[] OPERATORKEYS = { "equals", "hasAsSubstring", "beginsWith", "endsWith", "hasAsWord", "fullText", "dateGreater", "dateGE", "dateEquals", "dateLE", "dateLess", "numberGreater", "numberGE", "numberEquals", "numberLE", "numberLess", "and", "or", "not", "zoneHasAsWord", "zoneHasAsWordPrefix" };
/*      */   public static final String SUBSTR_TO_OTHER_OPR_CONV_KEY_UNIV = "SubstringOperatorReplacement";
/*      */   public static final String SUBSTR_TO_OTHER_OPR_CONV_KEY_BINDER = "substringOperatorReplacement";
/*      */   protected CommonSearchConfig m_config;
/*      */   protected String m_queryDefLabel;
/*      */   protected char[][] m_wildCards;
/*      */   protected QueryParserCallback m_parserCallback;
/*      */   protected SearchQueryValidator m_queryValidator;
/*      */   protected long m_monitoredOperators;
/*      */   protected IdcDateFormat m_dateFormat;
/*      */   protected TimeZone m_timeZone;
/*      */   protected boolean m_allowBackSlashEscape;
/*      */   protected String m_illegalChars;
/*      */   protected boolean m_allowParseInternetSearch;
/*      */   protected boolean m_limitWildCardToEquals;
/*      */   protected boolean m_disableFieldSpecificInternetSearch;
/*      */   protected boolean m_escapeReservedStringsInSearch;
/*      */   protected boolean m_validateSearchQuery;
/*      */   protected int m_substrToOtherOprConvFlg;
/*  114 */   protected static Map m_callbacks = new ConcurrentHashMap();
/*      */ 
/*      */   public UniversalSearchQueryParser()
/*      */   {
/*   89 */     this.m_config = null;
/*   90 */     this.m_queryDefLabel = null;
/*   91 */     this.m_wildCards = ((char[][])null);
/*   92 */     this.m_parserCallback = null;
/*   93 */     this.m_queryValidator = null;
/*   94 */     this.m_monitoredOperators = 0L;
/*      */ 
/*   96 */     this.m_dateFormat = null;
/*   97 */     this.m_timeZone = null;
/*      */ 
/*   99 */     this.m_allowBackSlashEscape = false;
/*  100 */     this.m_illegalChars = "";
/*      */ 
/*  102 */     this.m_allowParseInternetSearch = false;
/*      */ 
/*  104 */     this.m_limitWildCardToEquals = false;
/*      */ 
/*  106 */     this.m_disableFieldSpecificInternetSearch = false;
/*      */ 
/*  108 */     this.m_escapeReservedStringsInSearch = false;
/*      */ 
/*  110 */     this.m_validateSearchQuery = false;
/*      */ 
/*  112 */     this.m_substrToOtherOprConvFlg = -1;
/*      */   }
/*      */ 
/*      */   public void init(String queryDefLabel, String wildCards, CommonSearchConfig config, QueryParserCallback callback)
/*      */     throws ServiceException
/*      */   {
/*  119 */     init(queryDefLabel, wildCards, config, callback, null);
/*      */   }
/*      */ 
/*      */   public void init(String queryDefLabel, String wildCards, CommonSearchConfig config, QueryParserCallback callback, SearchQueryValidator queryValidator)
/*      */     throws ServiceException
/*      */   {
/*  140 */     this.m_queryDefLabel = queryDefLabel;
/*  141 */     this.m_config = config;
/*      */ 
/*  143 */     if (wildCards != null)
/*      */     {
/*  145 */       Vector wildCardsList = StringUtils.parseArray(wildCards, ',', ',');
/*  146 */       int size = wildCardsList.size();
/*  147 */       if (size != 2)
/*      */       {
/*  149 */         String msg = LocaleUtils.encodeMessage("csSearchQueryParserUnableToParseWildCard", null, wildCards);
/*      */ 
/*  151 */         throw new ServiceException(msg);
/*      */       }
/*  153 */       this.m_wildCards = new char[size][];
/*  154 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  156 */         String tmp = (String)wildCardsList.elementAt(i);
/*  157 */         this.m_wildCards[i] = tmp.toCharArray();
/*      */       }
/*      */     }
/*  160 */     this.m_parserCallback = callback;
/*  161 */     this.m_queryValidator = queryValidator;
/*  162 */     this.m_dateFormat = LocaleResources.m_odbcFormat;
/*  163 */     this.m_timeZone = LocaleResources.getSystemTimeZone();
/*      */ 
/*  165 */     this.m_illegalChars = config.getEngineValue(config.getCurrentEngineName(), "UniversalSearchIllegalChars");
/*      */ 
/*  167 */     String allowEscape = config.getEngineValue(config.getCurrentEngineName(), "UniversalSearchAllowBackSlashEscape");
/*  168 */     this.m_allowBackSlashEscape = StringUtils.convertToBool(allowEscape, true);
/*      */ 
/*  170 */     this.m_allowParseInternetSearch = SharedObjects.getEnvValueAsBoolean("DoMetaInternetSearch", false);
/*      */ 
/*  172 */     String limitWildCardToEquals = config.getEngineValue("LimitWildCardToEqualsOp");
/*  173 */     this.m_limitWildCardToEquals = StringUtils.convertToBool(limitWildCardToEquals, false);
/*      */ 
/*  175 */     String tmp = config.getEngineValue("DisableFieldSpecificInternetSearchChange");
/*  176 */     this.m_disableFieldSpecificInternetSearch = StringUtils.convertToBool(tmp, false);
/*      */   }
/*      */ 
/*      */   public String parse(DataBinder binder, ExecutionContext context) throws ServiceException
/*      */   {
/*  181 */     String query = binder.getLocal("QueryText");
/*      */ 
/*  183 */     ParsedQueryElements queryElts = SearchQueryUtils.lookupSearchParsingObject(context);
/*      */ 
/*  185 */     setSubstrToOtherOprConvFlag(query, binder);
/*      */ 
/*  187 */     if (Report.m_verbose)
/*      */     {
/*  189 */       Report.trace("searchqueryparse", "Into parse of UniversalSearchQuery for: " + query, null);
/*      */     }
/*      */ 
/*  193 */     return parseQuery(query, binder, queryElts, context);
/*      */   }
/*      */ 
/*      */   public void setSubstrToOtherOprConvFlag(String query, DataBinder binder)
/*      */   {
/*  205 */     if (Report.m_verbose)
/*      */     {
/*  207 */       Report.trace("searchquery", "Setting substr to other operator conv flag: '" + query + "'", null);
/*      */     }
/*      */ 
/*  210 */     String substrToOtherOprConvFlg = null;
/*      */ 
/*  214 */     substrToOtherOprConvFlg = binder.getLocal("substringOperatorReplacement");
/*      */ 
/*  216 */     if ((substrToOtherOprConvFlg == null) || (substrToOtherOprConvFlg.equals("")))
/*      */     {
/*  220 */       substrToOtherOprConvFlg = binder.getLocal("SubstringOperatorReplacement");
/*      */     }
/*      */ 
/*  223 */     if ((substrToOtherOprConvFlg == null) || (substrToOtherOprConvFlg.equals("")))
/*      */     {
/*  225 */       String searchEngineName = this.m_config.getCurrentEngineName();
/*  226 */       substrToOtherOprConvFlg = this.m_config.getEngineValue(searchEngineName, "SubstringOperatorReplacement");
/*      */     }
/*      */ 
/*  229 */     if ((substrToOtherOprConvFlg == null) || (substrToOtherOprConvFlg.equals("")))
/*      */       return;
/*  231 */     if (substrToOtherOprConvFlg.equalsIgnoreCase("contains"))
/*      */     {
/*  233 */       this.m_substrToOtherOprConvFlg = 4;
/*      */     }
/*  235 */     if (!substrToOtherOprConvFlg.equalsIgnoreCase("matches"))
/*      */       return;
/*  237 */     this.m_substrToOtherOprConvFlg = 0;
/*      */   }
/*      */ 
/*      */   public String parseQuery(String query, DataBinder binder, ParsedQueryElements queryElts, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*  247 */     if ((query == null) || (query.length() == 0))
/*      */     {
/*  249 */       if (queryElts != null)
/*      */       {
/*      */         try
/*      */         {
/*  253 */           SearchQueryUtils.processQueryElements(queryElts, binder, context);
/*  254 */           queryElts.m_isCompacted = true;
/*  255 */           queryElts.m_isFinishedParsing = true;
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  259 */           Report.trace("system", null, e);
/*      */         }
/*      */       }
/*  262 */       return query;
/*      */     }
/*  264 */     IdcStringBuilder builder = new IdcStringBuilder();
/*  265 */     parseQueryEx(builder, query.toCharArray(), binder, queryElts, context);
/*  266 */     return builder.toString();
/*      */   }
/*      */ 
/*      */   public void parseQueryEx(IdcStringBuilder appendable, char[] queryArr, DataBinder binder, ParsedQueryElements queryElts, ExecutionContext context) throws ServiceException
/*      */   {
/*  271 */     if (queryArr == null)
/*      */     {
/*  273 */       return;
/*      */     }
/*      */ 
/*  276 */     if (Report.m_verbose)
/*      */     {
/*  278 */       Report.trace("searchqueryparse", "Into parseQueryEx of UniversalSearchQueryParser for queryArr : " + new String(queryArr), null);
/*      */     }
/*      */ 
/*  282 */     if (Report.m_verbose)
/*      */     {
/*  284 */       Report.trace("searchquery", "Parsing universal query: '" + new String(queryArr) + "'", null);
/*      */     }
/*  286 */     boolean allowParseInternetSearch = DataBinderUtils.getBoolean(binder, "DoMetaInternetSearch", this.m_allowParseInternetSearch);
/*  287 */     allowParseInternetSearch = DataBinderUtils.getBoolean(binder, "doMetaInternetSearch", allowParseInternetSearch);
/*      */ 
/*  289 */     boolean allowMatchUseInternetSearch = DataBinderUtils.getBoolean(binder, "AllowMatchUseInternetSearch", false);
/*  290 */     allowMatchUseInternetSearch = DataBinderUtils.getBoolean(binder, "allowMatchUseInternetSearch", allowMatchUseInternetSearch);
/*      */ 
/*  292 */     boolean escapeReservedWords = SharedObjects.getEnvValueAsBoolean("EscapeReservedStringsInSearch", this.m_escapeReservedStringsInSearch);
/*  293 */     escapeReservedWords = DataBinderUtils.getBoolean(binder, "EscapeReservedStringsInSearch", escapeReservedWords);
/*  294 */     escapeReservedWords = DataBinderUtils.getBoolean(binder, "escapeReservedStringsInSearch", escapeReservedWords);
/*      */ 
/*  296 */     binder.putLocal("escapeReservedStringsInSearch", Boolean.toString(escapeReservedWords));
/*      */ 
/*  298 */     boolean validateSearchQuery = SharedObjects.getEnvValueAsBoolean("ValidateSearchQuery", this.m_validateSearchQuery);
/*  299 */     validateSearchQuery = DataBinderUtils.getBoolean(binder, "ValidateSearchQuery", validateSearchQuery);
/*  300 */     validateSearchQuery = DataBinderUtils.getBoolean(binder, "validateSearchQuery", validateSearchQuery);
/*  301 */     binder.putLocal("validateSearchQuery", Boolean.toString(validateSearchQuery));
/*      */ 
/*  304 */     boolean isOpenBackQuote = false;
/*  305 */     boolean isNewFrag = true;
/*  306 */     boolean isOpFound = false;
/*  307 */     boolean isPrefixValue = false;
/*  308 */     boolean hasWildCard = false;
/*      */ 
/*  310 */     int lastIndex = 0;
/*      */ 
/*  312 */     int followingCharCopyCount = 0;
/*  313 */     int skipCharCount = 0;
/*  314 */     int initialSkipCharCount = 0;
/*  315 */     int openParens = 0;
/*  316 */     char[] valueBuf = new char[4096];
/*  317 */     int valueLen = 0;
/*  318 */     int operator = -1;
/*  319 */     int conjunction = -1;
/*  320 */     int fieldStartIndex = -1;
/*  321 */     int fieldLen = 0;
/*  322 */     Date[] parsedDate = new Date[1];
/*      */ 
/*  324 */     String lastOperatorKey = null;
/*  325 */     String lastConjunctionKey = null;
/*      */ 
/*  327 */     boolean isError = false;
/*  328 */     String errorMsg = null;
/*  329 */     String priorMsg = null;
/*  330 */     String newFragPostParsedElement = null;
/*  331 */     List qElts = null;
/*      */ 
/*  333 */     boolean isNumberField = false;
/*  334 */     boolean isDateField = false;
/*  335 */     if (queryElts != null)
/*      */     {
/*  337 */       qElts = queryElts.m_rawParsedElements;
/*      */     }
/*      */ 
/*  340 */     int i = 0;
/*  341 */     for (i = 0; i < queryArr.length; ++i)
/*      */     {
/*  343 */       char[] valueChars = valueBuf;
/*      */       label2376: int j;
/*  344 */       switch (queryArr[i])
/*      */       {
/*      */       case '(':
/*      */       case ')':
/*  348 */         if (queryArr[i] == '(')
/*      */         {
/*  350 */           ++openParens;
/*      */         }
/*      */         else
/*      */         {
/*  354 */           if (openParens < 1)
/*      */           {
/*  356 */             isError = true;
/*  357 */             errorMsg = "csSearchQueryParserMismatchParens";
/*      */           }
/*  359 */           --openParens;
/*      */         }
/*      */ 
/*  362 */         if (qElts != null)
/*      */         {
/*  364 */           newFragPostParsedElement = (queryArr[i] == '(') ? "(" : ")";
/*      */         }
/*      */ 
/*  367 */         isNewFrag = true;
/*  368 */         followingCharCopyCount = 1;
/*  369 */         skipCharCount = 1;
/*  370 */         break;
/*      */       case '<':
/*  374 */         int remainLen = queryArr.length - i;
/*  375 */         remainLen = (remainLen > 11) ? 11 : remainLen;
/*      */         int j;
/*  376 */         switch (remainLen)
/*      */         {
/*      */         case 11:
/*  379 */           j = i;
/*  380 */           if ((queryArr[(j + 10)] == '>') && (((queryArr[(++j)] == 's') || (queryArr[j] == 'S'))) && (((queryArr[(++j)] == 'u') || (queryArr[j] == 'U'))) && (((queryArr[(++j)] == 'b') || (queryArr[j] == 'B'))) && (((queryArr[(++j)] == 's') || (queryArr[j] == 'S'))) && (((queryArr[(++j)] == 't') || (queryArr[j] == 'T'))) && (((queryArr[(++j)] == 'r') || (queryArr[j] == 'R'))) && (((queryArr[(++j)] == 'i') || (queryArr[j] == 'I'))) && (((queryArr[(++j)] == 'n') || (queryArr[j] == 'N'))) && (((queryArr[(++j)] == 'g') || (queryArr[j] == 'G'))))
/*      */           {
/*  393 */             operator = 1;
/*      */ 
/*  395 */             if (this.m_substrToOtherOprConvFlg != -1)
/*      */             {
/*  397 */               operator = this.m_substrToOtherOprConvFlg;
/*      */             }
/*      */ 
/*  400 */             skipCharCount = 11;
/*  401 */             isNewFrag = false;
/*  402 */             isOpFound = true;
/*  403 */           }break;
/*      */         case 10:
/*  406 */           j = i;
/*  407 */           if ((queryArr[(j + 9)] == '>') && (((queryArr[(++j)] == 'c') || (queryArr[j] == 'C'))) && (((queryArr[(++j)] == 'o') || (queryArr[j] == 'O'))) && (((queryArr[(++j)] == 'n') || (queryArr[j] == 'N'))) && (((queryArr[(++j)] == 't') || (queryArr[j] == 'T'))) && (((queryArr[(++j)] == 'a') || (queryArr[j] == 'A'))) && (((queryArr[(++j)] == 'i') || (queryArr[j] == 'I'))) && (((queryArr[(++j)] == 'n') || (queryArr[j] == 'N'))) && (((queryArr[(++j)] == 's') || (queryArr[j] == 'S'))))
/*      */           {
/*  424 */             operator = 4;
/*  425 */             skipCharCount = 10;
/*  426 */             isNewFrag = false;
/*  427 */             isOpFound = true;
/*  428 */           }break;
/*      */         case 9:
/*  431 */           j = i;
/*  432 */           if ((queryArr[(j + 8)] == '>') && (((queryArr[(++j)] == 'm') || (queryArr[j] == 'M'))) && (((queryArr[(++j)] == 'a') || (queryArr[j] == 'A'))) && (((queryArr[(++j)] == 't') || (queryArr[j] == 'T'))) && (((queryArr[(++j)] == 'c') || (queryArr[j] == 'C'))) && (((queryArr[(++j)] == 'h') || (queryArr[j] == 'H'))) && (((queryArr[(++j)] == 'e') || (queryArr[j] == 'E'))) && (((queryArr[(++j)] == 's') || (queryArr[j] == 'S'))))
/*      */           {
/*  443 */             operator = 0;
/*  444 */             skipCharCount = 9;
/*  445 */             isNewFrag = false;
/*  446 */             isOpFound = true;
/*  447 */           }break;
/*      */         case 8:
/*  451 */           j = i;
/*  452 */           if ((queryArr[(j + 7)] == '>') && (((queryArr[(++j)] == 's') || (queryArr[j] == 'S'))) && (((queryArr[(++j)] == 't') || (queryArr[j] == 'T'))) && (((queryArr[(++j)] == 'a') || (queryArr[j] == 'A'))) && (((queryArr[(++j)] == 'r') || (queryArr[j] == 'R'))) && (((queryArr[(++j)] == 't') || (queryArr[j] == 'T'))) && (((queryArr[(++j)] == 's') || (queryArr[j] == 'S'))))
/*      */           {
/*  462 */             operator = 2;
/*  463 */             skipCharCount = 8;
/*  464 */             isNewFrag = false;
/*  465 */             isOpFound = true;
/*  466 */           }break;
/*      */         case 6:
/*      */         case 7:
/*  471 */           j = i;
/*  472 */           if ((queryArr[(j + 5)] == '>') && (((queryArr[(++j)] == 'e') || (queryArr[j] == 'E'))) && (((queryArr[(++j)] == 'n') || (queryArr[j] == 'N'))) && (((queryArr[(++j)] == 'd') || (queryArr[j] == 'D'))) && (((queryArr[(++j)] == 's') || (queryArr[j] == 'S'))))
/*      */           {
/*  480 */             operator = 3;
/*  481 */             skipCharCount = 6;
/*  482 */             isNewFrag = false;
/*  483 */             isOpFound = true;
/*      */           }
/*      */           else
/*      */           {
/*  487 */             j = i;
/*      */ 
/*  489 */             if ((queryArr[(j + 5)] == '>') && (((queryArr[(++j)] == 'q') || (queryArr[j] == 'q') || (queryArr[j] == 'u') || (queryArr[j] == 'U') || (queryArr[j] == 'i') || (queryArr[j] == 'I'))) && (((queryArr[(++j)] == 's') || (queryArr[j] == 's'))) && (((queryArr[(++j)] == 'c') || (queryArr[j] == 'c'))) && (((queryArr[(++j)] == 'h') || (queryArr[j] == 'h'))))
/*      */             {
/*  497 */               int k = j + 2;
/*  498 */               boolean isInternetSearch = false;
/*  499 */               for (; k < queryArr.length; ++k)
/*      */               {
/*  501 */                 if ((queryArr[k] != '<') || (queryArr[(k + 1)] != '/') || (queryArr[(k + 2)] != queryArr[(j - 3)]) || (queryArr[(k + 3)] != queryArr[(j - 2)]) || (queryArr[(k + 4)] != queryArr[(j - 1)]) || (queryArr[(k + 5)] != queryArr[j]) || (queryArr[(k + 6)] != '>'))
/*      */                 {
/*      */                   continue;
/*      */                 }
/*      */ 
/*  508 */                 isInternetSearch = true;
/*  509 */                 break;
/*      */               }
/*      */ 
/*  512 */               if (!isInternetSearch)
/*      */                 break label2376;
/*  514 */               conjunction = -1;
/*  515 */               operator = 200;
/*  516 */               if ((queryArr[(j - 3)] == 'u') || (queryArr[(j - 3)] == 'U'))
/*      */               {
/*  518 */                 operator = 203;
/*      */               }
/*  520 */               else if ((queryArr[(j - 3)] == 'i') || (queryArr[(j - 3)] == 'I'))
/*      */               {
/*  522 */                 operator = 201;
/*      */               }
/*  524 */               skipCharCount = k - i + 7;
/*  525 */               followingCharCopyCount = 0;
/*  526 */               initialSkipCharCount = 0;
/*  527 */               isNewFrag = true;
/*  528 */               isOpFound = true;
/*  529 */               valueChars = new char[k - j - 2];
/*  530 */               System.arraycopy(queryArr, j + 2, valueChars, 0, k - j - 2); } 
/*  530 */           }break;
/*      */         case 5:
/*  537 */           j = i;
/*  538 */           if ((queryArr[(j + 4)] == '>') && (((queryArr[(++j)] == 'a') || (queryArr[j] == 'A'))) && (((queryArr[(++j)] == 'n') || (queryArr[j] == 'N'))) && (((queryArr[(++j)] == 'd') || (queryArr[j] == 'D'))))
/*      */           {
/*  545 */             conjunction = 16;
/*  546 */             skipCharCount = 5;
/*  547 */             isNewFrag = true;
/*  548 */             isOpFound = true;
/*      */           }
/*      */           else {
/*  551 */             j = i;
/*  552 */             if ((queryArr[(j + 4)] == '>') && (((queryArr[(++j)] == 'n') || (queryArr[j] == 'N'))) && (((queryArr[(++j)] == 'o') || (queryArr[j] == 'O'))) && (((queryArr[(++j)] == 't') || (queryArr[j] == 'T'))))
/*      */             {
/*  558 */               conjunction = 18;
/*  559 */               skipCharCount = 5;
/*  560 */               isNewFrag = true;
/*  561 */               isOpFound = true;
/*      */             }
/*      */             else
/*      */             {
/*  565 */               j = i;
/*  566 */               if ((queryArr[(j + 4)] == '>') && (((queryArr[(++j)] == 'f') || (queryArr[j] == 'F'))) && (((queryArr[(++j)] == 't') || (queryArr[j] == 'T'))) && (((queryArr[(++j)] == 'x') || (queryArr[j] == 'X'))))
/*      */               {
/*  576 */                 int k = j;
/*  577 */                 boolean isInternetSearch = false;
/*  578 */                 for (; k < queryArr.length; ++k)
/*      */                 {
/*  580 */                   if ((queryArr[k] != '<') || (queryArr[(k + 1)] != '/') || (queryArr[(k + 2)] != queryArr[(j - 2)]) || (queryArr[(k + 3)] != queryArr[(j - 1)]) || (queryArr[(k + 4)] != queryArr[j]) || (queryArr[(k + 5)] != '>'))
/*      */                   {
/*      */                     continue;
/*      */                   }
/*      */ 
/*  586 */                   isInternetSearch = true;
/*  587 */                   break;
/*      */                 }
/*      */ 
/*  590 */                 if (!isInternetSearch)
/*      */                   break label2376;
/*  592 */                 conjunction = -1;
/*  593 */                 operator = 202;
/*  594 */                 skipCharCount = k - i + 6;
/*  595 */                 followingCharCopyCount = 0;
/*  596 */                 initialSkipCharCount = 0;
/*  597 */                 isNewFrag = true;
/*  598 */                 isOpFound = true;
/*  599 */                 valueChars = new char[k - j - 2];
/*  600 */                 System.arraycopy(queryArr, j + 2, valueChars, 0, k - j - 2); } 
/*      */             }
/*  600 */           }break;
/*      */         case 4:
/*  606 */           j = i;
/*  607 */           if ((queryArr[(j + 3)] == '>') && (((queryArr[(++j)] == 'o') || (queryArr[j] == 'O'))) && (((queryArr[(++j)] == 'r') || (queryArr[j] == 'R'))))
/*      */           {
/*  613 */             conjunction = 17;
/*  614 */             skipCharCount = 4;
/*  615 */             isNewFrag = true;
/*  616 */             isOpFound = true;
/*      */           }
/*      */           else {
/*  619 */             j = i;
/*  620 */             if ((queryArr[(j + 3)] == '>') && (((queryArr[(++j)] == 'i') || (queryArr[j] == 'I'))) && (((queryArr[(++j)] == 'n') || (queryArr[j] == 'N'))))
/*      */             {
/*  626 */               operator = 19;
/*  627 */               if (isPrefixValue)
/*      */               {
/*  629 */                 operator = 20;
/*      */               }
/*  631 */               skipCharCount = 4;
/*  632 */               isNewFrag = false;
/*  633 */               isOpFound = true;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  640 */         if (isOpFound)
/*      */         {
/*  642 */           isOpFound = false;
/*  643 */         }break;
/*      */       case '=':
/*      */       case '>':
/*  648 */         if (operator != -1)
/*      */         {
/*  650 */           isError = true;
/*  651 */           errorMsg = "csSearchQueryParserOperatorExists";
/*      */         }
/*  654 */         else if ((i == 0) || (i == queryArr.length - 1))
/*      */         {
/*  657 */           isError = true;
/*  658 */           errorMsg = "csSearchQueryParserInvalidSyntaxConstruction";
/*      */         }
/*      */         else {
/*  661 */           switch (queryArr[i])
/*      */           {
/*      */           case '>':
/*  664 */             if (queryArr[(i + 1)] == '=')
/*      */             {
/*  666 */               operator = 12;
/*  667 */               skipCharCount = 2;
/*      */             }
/*      */             else
/*      */             {
/*  671 */               operator = 11;
/*  672 */               skipCharCount = 1;
/*      */             }
/*  674 */             break;
/*      */           case '<':
/*  676 */             if (queryArr[(i + 1)] == '=')
/*      */             {
/*  678 */               operator = 14;
/*  679 */               skipCharCount = 2;
/*      */             }
/*      */             else
/*      */             {
/*  683 */               operator = 15;
/*  684 */               skipCharCount = 1;
/*      */             }
/*  686 */             break;
/*      */           case '=':
/*  688 */             operator = 13;
/*  689 */             skipCharCount = 1;
/*      */           }
/*      */ 
/*  692 */           isNewFrag = false;
/*  693 */         }break;
/*      */       case '`':
/*  697 */         if (valueLen != 0)
/*      */         {
/*  700 */           errorMsg = "csSearhcQueryParserParsingError";
/*  701 */           isError = true;
/*      */         }
/*      */         else
/*      */         {
/*  705 */           boolean escapeChar = false;
/*  706 */           isOpenBackQuote = true;
/*  707 */           j = 0;
/*  708 */           boolean isWildCardChar = false;
/*  709 */           boolean isLastCharWildCard = false;
/*  710 */           for (j = i + 1; j < queryArr.length; ++j)
/*      */           {
/*  712 */             char tmpChar = queryArr[j];
/*  713 */             char[] replaceChars = null;
/*  714 */             char replaceChar = tmpChar;
/*  715 */             if (!escapeChar)
/*      */             {
/*  717 */               if (isWildCardChar)
/*      */               {
/*  719 */                 isLastCharWildCard = true;
/*      */               }
/*  721 */               if ((this.m_illegalChars != null) && (this.m_illegalChars.indexOf(tmpChar) >= 0))
/*      */               {
/*  724 */                 isError = true;
/*  725 */                 errorMsg = "csSearchQueryParserIllegalCharFound"; break label2823:
/*      */               }
/*  727 */               if (tmpChar == '\\')
/*      */               {
/*  729 */                 if (this.m_allowBackSlashEscape)
/*      */                 {
/*  731 */                   escapeChar = true;
/*  732 */                   if ((operator != 0) || (queryArr.length <= j + 2) || (queryArr[(j + 1)] != '\\') || (queryArr[(j + 2)] != '*'))
/*      */                   {
/*      */                     continue;
/*      */                   }
/*      */ 
/*  739 */                   escapeChar = false; continue;
/*      */                 }
/*      */ 
/*  744 */                 isError = true;
/*  745 */                 errorMsg = "csSearchQueryParserIllegalCharFound"; break label2823:
/*      */               }
/*      */ 
/*  748 */               if ((tmpChar == '*') && (this.m_wildCards != null) && (((!this.m_limitWildCardToEquals) || (operator == 0) || (operator == -1))))
/*      */               {
/*  751 */                 replaceChars = this.m_wildCards[0];
/*  752 */                 isWildCardChar = true;
/*  753 */                 hasWildCard = true; break label2823:
/*      */               }
/*  755 */               if ((tmpChar == '?') && (this.m_wildCards != null) && (((!this.m_limitWildCardToEquals) || (operator == 0) || (operator == -1))))
/*      */               {
/*  757 */                 replaceChars = this.m_wildCards[1];
/*  758 */                 isWildCardChar = true;
/*  759 */                 hasWildCard = true; break label2823:
/*      */               }
/*      */ 
/*  762 */               if (tmpChar != '`') {
/*      */                 break label2823;
/*      */               }
/*  765 */               isOpenBackQuote = false;
/*  766 */               if (isLastCharWildCard)
/*      */               {
/*  768 */                 isPrefixValue = true;
/*      */               }
/*  770 */               ++j;
/*  771 */               break;
/*      */             }
/*      */ 
/*  777 */             escapeChar = false;
/*      */ 
/*  780 */             if (replaceChars != null)
/*      */             {
/*  782 */               for (int k = 0; k < replaceChars.length; ++k)
/*      */               {
/*  784 */                 valueChars[(valueLen++)] = replaceChars[k];
/*      */               }
/*      */ 
/*      */             }
/*      */             else {
/*  789 */               valueChars[(valueLen++)] = replaceChar;
/*      */             }
/*      */           }
/*  792 */           skipCharCount = j - i;
/*  793 */           if ((isOpenBackQuote) && (!isError))
/*      */           {
/*  795 */             isError = true;
/*  796 */             errorMsg = "csSearchQueryParserMismatchBackQuotes";
/*      */           }
/*  798 */           if (!isError)
/*      */           {
/*  800 */             isNewFrag = false; } 
/*  800 */         }break;
/*      */       default:
/*  805 */         if ((Character.isLetterOrDigit(queryArr[i])) || (queryArr[i] == '_'))
/*      */         {
/*  807 */           if (fieldLen == 0)
/*      */           {
/*  809 */             label2823: fieldStartIndex = i;
/*      */           }
/*      */           else
/*      */           {
/*  814 */             isError = true;
/*      */           }
/*  816 */           int endIndex = queryArr.length;
/*  817 */           for (j = i; j < queryArr.length; ++j)
/*      */           {
/*  819 */             if (Character.isLetterOrDigit(queryArr[j])) continue; if (queryArr[j] == '_') {
/*      */               continue;
/*      */             }
/*      */ 
/*  823 */             endIndex = j;
/*  824 */             break;
/*      */           }
/*  826 */           fieldLen = endIndex - i;
/*  827 */           skipCharCount = fieldLen;
/*      */         }
/*  829 */         else if (!Validation.isSpace(queryArr[i]))
/*      */         {
/*  832 */           isError = true;
/*      */         }
/*      */         else
/*      */         {
/*  836 */           skipCharCount = 1;
/*      */         }
/*  838 */         isNewFrag = false;
/*      */       }
/*      */ 
/*  841 */       if ((!isError) && (((isNewFrag) || (i + skipCharCount == queryArr.length))))
/*      */       {
/*  843 */         if (hasWildCard)
/*      */         {
/*  845 */           binder.putLocal("wildCardTranslationOccurred", "1");
/*      */         }
/*  847 */         if ((operator == 200) || (operator == 201) || (operator == 202) || (operator == 203))
/*      */         {
/*      */           try
/*      */           {
/*  851 */             switch (operator)
/*      */             {
/*      */             case 201:
/*  854 */               InternetSearchQueryUtils.parseISearch(appendable, valueChars, this.m_queryDefLabel, binder, queryElts, context, this.m_config);
/*      */ 
/*  856 */               break;
/*      */             case 200:
/*  858 */               InternetSearchQueryUtils.parseQuickSearch(appendable, valueChars, this.m_queryDefLabel, binder, queryElts, context, this.m_config);
/*      */ 
/*  860 */               break;
/*      */             case 203:
/*  862 */               DataBinder tmpBinder = new DataBinder();
/*  863 */               Properties prop = new Properties(binder.getLocalData());
/*  864 */               tmpBinder.setLocalData(prop);
/*  865 */               tmpBinder.putLocal("doMetaInternetSearch", "1");
/*  866 */               tmpBinder.putLocal("skipQueryElementProcessing", "true");
/*  867 */               parseQueryEx(appendable, valueChars, tmpBinder, queryElts, context);
/*  868 */               break;
/*      */             case 202:
/*  870 */               if ((this.m_parserCallback == null) || (!this.m_parserCallback.isQueryContainsNativeSyntax(valueChars)))
/*      */               {
/*  872 */                 InternetSearchQueryUtils.parseInternetSearch(appendable, "dDocFullText", "fullText", new String(valueChars), this.m_queryDefLabel, binder, queryElts, context, this.m_config);
/*      */               }
/*      */               else
/*      */               {
/*  877 */                 if (queryElts != null)
/*      */                 {
/*  879 */                   QueryElement elt = new QueryElement(new String(valueChars));
/*  880 */                   queryElts.m_rawParsedElements.add(elt);
/*      */                 }
/*  882 */                 appendable.append(valueChars, 0, valueChars.length);
/*      */               }
/*      */             }
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/*  888 */             throw new ServiceException(e);
/*      */           }
/*      */         }
/*  891 */         else if ((operator == -1) || (fieldLen == 0))
/*      */         {
/*  894 */           appendable.append(queryArr, lastIndex, i - lastIndex);
/*      */         }
/*  896 */         else if ((operator != -1) && (fieldLen != 0))
/*      */         {
/*  898 */           String queryOperator = OPERATORKEYS[operator];
/*  899 */           String fieldName = new String(queryArr, fieldStartIndex, fieldLen);
/*  900 */           isNumberField = false;
/*  901 */           isDateField = false;
/*  902 */           if ((DataBinderUtils.isDecimalField(fieldName, binder)) || (DataBinderUtils.isIntField(fieldName, binder)))
/*      */           {
/*  904 */             isNumberField = true;
/*      */           }
/*  906 */           if (DataBinderUtils.isDateField(fieldName, binder))
/*      */           {
/*  908 */             isDateField = true;
/*  909 */             if (operator == 0)
/*      */             {
/*  911 */               operator = 13;
/*      */             }
/*  913 */             int diff = operator - 5;
/*  914 */             if ((diff < 0) || (diff >= OPERATORKEYS.length))
/*      */             {
/*  916 */               String opName = SearchQueryUtils.convertToString(operator);
/*  917 */               priorMsg = LocaleUtils.encodeMessage("csSearchQueryInvalidDateOperator", null, opName, fieldName);
/*  918 */               isError = true;
/*  919 */               break;
/*      */             }
/*  921 */             operator -= 5;
/*  922 */             queryOperator = OPERATORKEYS[operator];
/*      */           }
/*  924 */           String value = new String(valueChars, 0, valueLen);
/*      */ 
/*  926 */           if (valueLen == 0)
/*      */           {
/*  928 */             value = this.m_config.getStringFromTableWithInheritance("SearchQueryDefinition", "SearchEngineName", null, "EmptyValue");
/*      */           }
/*      */ 
/*  935 */           boolean validQuery = true;
/*      */ 
/*  937 */           if ((validateSearchQuery == true) && (this.m_queryValidator != null))
/*      */           {
/*  939 */             validQuery = this.m_queryValidator.validateSearchQuery(this.m_config, context, binder, new String[] { queryOperator, fieldName, value });
/*      */           }
/*      */ 
/*  942 */           if (!validQuery)
/*      */           {
/*  946 */             String validationErrorMsg = binder.getLocal("validationErrorMsg");
/*  947 */             String invalidQueryMsg = LocaleUtils.encodeMessage("csSearchQueryParserInvalidClause", validationErrorMsg);
/*      */ 
/*  949 */             ServiceException exception = new ServiceException(invalidQueryMsg);
/*  950 */             Report.warning("searchquery", invalidQueryMsg, exception);
/*      */ 
/*  952 */             throw exception;
/*      */           }
/*      */ 
/*  957 */           int callbackReturn = 1;
/*      */           try
/*      */           {
/*  960 */             callbackReturn = InternetSearchQueryUtils.processQueryParserCallback(this.m_parserCallback, this.m_config, appendable, binder, context, queryOperator, fieldName, value, lastOperatorKey, lastConjunctionKey);
/*      */ 
/*  963 */             if (callbackReturn == 1)
/*      */             {
/*  965 */               Object[] filterObjs = (Object[])(Object[])context.getCachedObject("queryParserCallbackFilter");
/*  966 */               queryOperator = (String)filterObjs[2];
/*  967 */               fieldName = (String)filterObjs[3];
/*  968 */               value = (String)filterObjs[4];
/*      */             }
/*      */           }
/*      */           catch (DataException exception)
/*      */           {
/*  973 */             throw new ServiceException(exception);
/*      */           }
/*      */ 
/*  976 */           boolean createdQueryElement = false;
/*  977 */           String originalValue = value;
/*      */ 
/*  979 */           boolean isSearchEngineDatabase = this.m_config.getCurrentEngineName().startsWith("DATABASE");
/*      */ 
/*  981 */           boolean databaseSearchIsNull = SharedObjects.getEnvValueAsBoolean("DatabaseSearchNullValues", true);
/*  982 */           if ((isSearchEngineDatabase) && (databaseSearchIsNull) && (value.length() == 0))
/*      */           {
/*  986 */             if ((binder.getLocal("IsSqlServer") != "1") && ((
/*  988 */               (queryOperator == OPERATORKEYS[0]) || (queryOperator == OPERATORKEYS[8]) || (queryOperator == OPERATORKEYS[13]))))
/*      */             {
/*  990 */               queryOperator = "isNull";
/*      */             }
/*      */ 
/*      */           }
/*  996 */           else if (isDateField)
/*      */           {
/*  998 */             value = convertDateValue(fieldName, value, parsedDate, binder, context);
/*      */           }
/* 1000 */           else if (isNumberField)
/*      */           {
/* 1002 */             value = convertNumericValue(fieldName, value, binder, context);
/*      */           }
/*      */ 
/* 1006 */           if (callbackReturn == 1)
/*      */           {
/* 1008 */             appendable.append(' ');
/*      */             try
/*      */             {
/* 1011 */               if ((allowParseInternetSearch) && ((((operator <= 5) && (operator >= 0)) || ((((operator == 19) || (operator == 20))) && (((allowMatchUseInternetSearch) || (operator != 0))) && (isInternetSearchAllowedForField(fieldName))))))
/*      */               {
/* 1015 */                 InternetSearchQueryUtils.parseInternetSearch(appendable, fieldName, queryOperator, value, this.m_queryDefLabel, binder, queryElts, context, this.m_config);
/*      */ 
/* 1017 */                 createdQueryElement = true;
/*      */               }
/*      */               else
/*      */               {
/* 1021 */                 this.m_config.appendClauseElement(appendable, queryOperator, this.m_queryDefLabel, fieldName, value);
/*      */               }
/*      */ 
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/* 1027 */               Report.trace("searchquery", null, e);
/* 1028 */               isError = true;
/* 1029 */               priorMsg = e.getMessage();
/*      */             }
/* 1031 */             appendable.append(' ');
/*      */           }
/* 1033 */           if ((!createdQueryElement) && 
/* 1035 */             (qElts != null))
/*      */           {
/* 1037 */             Object convertedVal = null;
/* 1038 */             if (parsedDate[0] != null)
/*      */             {
/* 1040 */               convertedVal = parsedDate[0];
/*      */             }
/* 1042 */             QueryElement qe = SearchQueryUtils.createQueryElement(fieldName, operator, originalValue, convertedVal, binder, this.m_config, context);
/*      */ 
/* 1044 */             qElts.add(qe);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1049 */         if (conjunction != -1)
/*      */         {
/* 1051 */           int callbackReturn = 1;
/* 1052 */           if ((this.m_parserCallback != null) && ((this.m_monitoredOperators & 1 << conjunction) != 0L))
/*      */           {
/* 1054 */             callbackReturn = this.m_parserCallback.doCallback(appendable, binder, context, new String[] { OPERATORKEYS[conjunction], "", "", lastOperatorKey, lastConjunctionKey });
/*      */           }
/*      */ 
/* 1057 */           if (callbackReturn == 1)
/*      */           {
/* 1059 */             appendable.append(' ');
/*      */             try
/*      */             {
/* 1062 */               this.m_config.appendClauseElement(appendable, OPERATORKEYS[conjunction], this.m_queryDefLabel, "", "");
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/* 1067 */               Report.trace("searchquery", null, e);
/* 1068 */               isError = true;
/* 1069 */               priorMsg = e.getMessage();
/*      */             }
/* 1071 */             appendable.append(' ');
/*      */           }
/* 1073 */           if (qElts != null)
/*      */           {
/* 1075 */             qElts.add(new Integer(conjunction));
/*      */           }
/*      */         }
/*      */ 
/* 1079 */         if (followingCharCopyCount > 0)
/*      */         {
/* 1081 */           appendable.append(queryArr, i + initialSkipCharCount, followingCharCopyCount);
/* 1082 */           followingCharCopyCount = 0;
/* 1083 */           initialSkipCharCount = 0;
/*      */         }
/*      */ 
/* 1088 */         if ((operator != -1) && (operator < OPERATORKEYS.length))
/*      */         {
/* 1090 */           lastOperatorKey = OPERATORKEYS[operator];
/*      */         }
/*      */         else
/*      */         {
/* 1094 */           lastOperatorKey = null;
/*      */         }
/* 1096 */         if (conjunction != -1)
/*      */         {
/* 1098 */           lastConjunctionKey = OPERATORKEYS[conjunction];
/*      */         }
/*      */         else
/*      */         {
/* 1102 */           lastConjunctionKey = null;
/*      */         }
/* 1104 */         if ((qElts != null) && (newFragPostParsedElement != null))
/*      */         {
/* 1106 */           qElts.add(newFragPostParsedElement);
/* 1107 */           newFragPostParsedElement = null;
/*      */         }
/*      */ 
/* 1110 */         operator = -1;
/* 1111 */         valueLen = 0;
/* 1112 */         fieldLen = 0;
/* 1113 */         conjunction = -1;
/* 1114 */         isPrefixValue = false;
/* 1115 */         if (hasWildCard)
/*      */         {
/* 1117 */           binder.removeLocal("wildCardTranslationOccurred");
/* 1118 */           hasWildCard = false;
/*      */         }
/*      */       }
/*      */ 
/* 1122 */       lastIndex = i;
/* 1123 */       if (skipCharCount != 0)
/*      */       {
/* 1125 */         lastIndex += skipCharCount;
/* 1126 */         i = i + skipCharCount - 1;
/* 1127 */         skipCharCount = 0;
/*      */       }
/*      */ 
/* 1130 */       if (isError) {
/*      */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1136 */     if (isError)
/*      */     {
/* 1138 */       if (errorMsg == null)
/*      */       {
/* 1140 */         errorMsg = "csSearchQueryParserParsingError";
/*      */       }
/* 1142 */       errorMsg = LocaleUtils.encodeMessage(errorMsg, priorMsg, new String(queryArr), "" + i);
/*      */     }
/* 1144 */     if ((openParens > 0) && (!isError))
/*      */     {
/* 1147 */       isError = true;
/* 1148 */       errorMsg = LocaleUtils.encodeMessage("csSearchQueryParserMissingCloseParens", null, new String(queryArr));
/*      */     }
/*      */ 
/* 1151 */     if ((!isError) && (queryElts != null) && (!DataBinderUtils.getBoolean(binder, "skipQueryElementProcessing", false)))
/*      */     {
/* 1153 */       if ((!queryElts.m_isCompacted) && (!queryElts.m_isError))
/*      */       {
/*      */         try
/*      */         {
/* 1157 */           SearchQueryUtils.processQueryElements(queryElts, binder, context);
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/* 1161 */           throw new ServiceException(e);
/*      */         }
/* 1163 */         queryElts.m_isCompacted = true;
/* 1164 */         queryElts.m_isFinishedParsing = true;
/*      */       }
/* 1166 */       if (queryElts.m_isError)
/*      */       {
/* 1168 */         errorMsg = SearchQueryUtils.createErrorReport(queryElts);
/* 1169 */         isError = true;
/*      */       }
/*      */     }
/*      */ 
/* 1173 */     if (isError)
/*      */     {
/* 1176 */       throw new ServiceException(-64, errorMsg);
/*      */     }
/* 1178 */     if (!Report.m_verbose)
/*      */       return;
/* 1180 */     Report.trace("searchquery", "Converted native query: '" + appendable.toString() + "'", null);
/*      */   }
/*      */ 
/*      */   public String convertDateValue(String fieldName, String value, Date[] parsedVal, DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/* 1188 */     Date d = null;
/* 1189 */     if (DataBinderUtils.isDateField(fieldName, binder))
/*      */     {
/* 1191 */       d = LocaleResources.parseDateDataEntry(value, cxt, "searchQueryParser");
/* 1192 */       value = this.m_dateFormat.format(d, this.m_timeZone, 0);
/*      */     }
/* 1194 */     if ((parsedVal != null) && (parsedVal.length > 0))
/*      */     {
/* 1196 */       parsedVal[0] = d;
/*      */     }
/* 1198 */     return value;
/*      */   }
/*      */ 
/*      */   public String convertNumericValue(String fieldName, String value, DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/* 1218 */     String formattedValue = value;
/*      */     try
/*      */     {
/* 1222 */       Locale userLocale = LocaleUtils.constructJavaLocaleFromContext(cxt);
/* 1223 */       formattedValue = NumberUtils.formatNumber(value, userLocale, Locale.US);
/*      */ 
/* 1227 */       Double.parseDouble(formattedValue);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1231 */       String errorMsg = LocaleUtils.encodeMessage("csNumberInvalidCharacters", null, value);
/* 1232 */       throw new ServiceException(e, errorMsg, new Object[0]);
/*      */     }
/*      */ 
/* 1235 */     boolean disableFormatting = SharedObjects.getEnvValueAsBoolean("DisableNumericFieldFormat", false);
/* 1236 */     if (disableFormatting == true)
/*      */     {
/* 1238 */       return value;
/*      */     }
/*      */ 
/* 1241 */     return formattedValue;
/*      */   }
/*      */ 
/*      */   public void setDateFormat(IdcDateFormat format)
/*      */   {
/* 1246 */     this.m_dateFormat = format;
/*      */   }
/*      */ 
/*      */   public IdcDateFormat getDateFormat()
/*      */   {
/* 1251 */     return this.m_dateFormat;
/*      */   }
/*      */ 
/*      */   public void setTZ(TimeZone zone)
/*      */   {
/* 1256 */     this.m_timeZone = zone;
/*      */   }
/*      */ 
/*      */   public TimeZone getTZ()
/*      */   {
/* 1261 */     return this.m_timeZone;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void setTimeZone(IdcTimeZone zone)
/*      */   {
/* 1268 */     this.m_timeZone = zone;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public IdcTimeZone getTimezone()
/*      */   {
/* 1275 */     return IdcTimeZone.wrap(this.m_timeZone);
/*      */   }
/*      */ 
/*      */   public static boolean isRangeOperator(int operatorCode)
/*      */   {
/* 1282 */     return (operatorCode >= 6) && (operatorCode <= 15) && (operatorCode != 8) && (operatorCode != 13);
/*      */   }
/*      */ 
/*      */   public void setQueryParserCallback(QueryParserCallback callback)
/*      */   {
/* 1289 */     this.m_parserCallback = callback;
/*      */   }
/*      */ 
/*      */   public boolean isInternetSearchAllowedForField(String field)
/*      */   {
/* 1294 */     return (this.m_disableFieldSpecificInternetSearch) || (!SearchLoader.isSecurityFieldInEngine(field, this.m_config.getCurrentEngineName()));
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1300 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104481 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.UniversalSearchQueryParser
 * JD-Core Version:    0.5.4
 */