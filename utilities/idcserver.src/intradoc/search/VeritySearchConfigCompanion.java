/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.server.SearchUtils;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class VeritySearchConfigCompanion extends CommonSearchConfigCompanionAdaptor
/*     */ {
/*     */   public void init(CommonSearchConfig config)
/*     */     throws ServiceException
/*     */   {
/*  35 */     super.init(config);
/*  36 */     this.m_wildCards = null;
/*     */     try
/*     */     {
/*  39 */       String and = config.parseElement("and", null, null);
/*  40 */       String or = config.parseElement("or", null, null);
/*  41 */       String not = config.parseElement("not", null, null);
/*  42 */       this.m_textExtras[0] = and.toString().toCharArray();
/*  43 */       this.m_textExtras[1] = or.toString().toCharArray();
/*  44 */       this.m_textExtras[3] = not.toString().toCharArray();
/*  45 */       IdcStringBuilder andnot = new IdcStringBuilder();
/*  46 */       andnot.append(and).append(' ').append(not);
/*  47 */       this.m_textExtras[2] = andnot.toCharArray();
/*  48 */       andnot.releaseBuffers();
/*  49 */       this.m_textExtras[5] = { '?' };
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  54 */       String msg = LocaleUtils.encodeMessage("csUnableToSetTextConjunction", null, e.getMessage());
/*     */ 
/*  56 */       throw new ServiceException(msg, e);
/*     */     }
/*     */ 
/*  59 */     this.m_callbackFilterName = "verityQueryParserCallbackFilter";
/*     */   }
/*     */ 
/*     */   public int fixUpAndValidateQuery(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  65 */     String queryText = binder.getLocal("QueryText");
/*  66 */     String queryFormat = SearchIndexerUtils.getSearchQueryFormat(binder, ctxt);
/*  67 */     if ((queryFormat != null) && (queryFormat.equalsIgnoreCase("Universal")) && (StringUtils.convertToBool(this.m_config.getEngineValue("UniversalSearchParseForVerity"), true)))
/*     */     {
/*  70 */       this.m_parser.setDateFormat(LocaleResources.m_searchFormat);
/*  71 */       IdcDateFormat format = (IdcDateFormat)ctxt.getCachedObject("UserDateFormat");
/*  72 */       ctxt.setCachedObject("UserDateFormat", binder.m_blDateFormat);
/*  73 */       queryText = this.m_parser.parse(binder, ctxt);
/*  74 */       if (format != null)
/*     */       {
/*  76 */         ctxt.setCachedObject("UserDateFomrat", format);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*  81 */       queryText = translateTaggedQuery(queryText, this.m_queryDefinitionLabel, binder, ctxt);
/*  82 */       queryText = SearchUtils.fixVerityDateFields(queryText, LocaleResources.m_searchFormat, binder.m_blDateFormat, binder.getFieldTypes());
/*     */ 
/*  85 */       binder.putLocal("TranslatedQueryText", queryText);
/*     */ 
/*  88 */       boolean insideLiteral = false;
/*  89 */       char literalChar = '-';
/*  90 */       int first = 0;
/*  91 */       int next = 0;
/*  92 */       int length = queryText.length();
/*  93 */       boolean isFirst = false;
/*  94 */       int maxlen = SharedObjects.getEnvironmentInt("VerityMaxQueryLength", 10000);
/*     */ 
/*  96 */       char[] temp = queryText.toCharArray();
/*     */ 
/*  99 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 101 */         char tch = temp[i];
/*     */ 
/* 103 */         if (insideLiteral)
/*     */         {
/* 105 */           if (tch != literalChar)
/*     */             continue;
/* 107 */           insideLiteral = false;
/*     */         }
/*     */         else
/*     */         {
/* 112 */           if (tch == '>')
/*     */           {
/* 114 */             if (!isFirst)
/*     */             {
/* 116 */               isFirst = true;
/* 117 */               first = i;
/*     */             }
/*     */             else
/*     */             {
/* 121 */               next = i;
/* 122 */               if (next - first > maxlen)
/*     */               {
/* 124 */                 String errMsg = LocaleUtils.encodeMessage("csSearchQueryTooLong", null, queryText);
/*     */ 
/* 126 */                 throw new DataException(errMsg);
/*     */               }
/* 128 */               first = next;
/* 129 */               next = 0;
/*     */             }
/*     */           }
/*     */ 
/* 133 */           if (tch != '`')
/*     */             continue;
/* 135 */           insideLiteral = true;
/* 136 */           literalChar = tch;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 141 */       if (insideLiteral)
/*     */       {
/* 143 */         String msg = LocaleUtils.encodeMessage("csSearchUnmatchedLiteral", null, "" + literalChar);
/*     */ 
/* 145 */         throw new DataException(msg);
/*     */       }
/*     */ 
/* 148 */       if (length - first > maxlen)
/*     */       {
/* 150 */         String errMsg = LocaleUtils.encodeMessage("csSearchQueryTooLong", null, queryText);
/*     */ 
/* 152 */         throw new DataException(errMsg);
/*     */       }
/*     */     }
/* 155 */     binder.putLocal("QueryText", queryText);
/*     */ 
/* 157 */     return 1;
/*     */   }
/*     */ 
/*     */   public int prepareQueryText(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 164 */     String searchLocale = SearchLoader.m_locale;
/*     */ 
/* 166 */     if ((searchLocale != null) && (searchLocale.equalsIgnoreCase("uni")))
/*     */     {
/* 168 */       String langId = binder.getLocal("LangID");
/* 169 */       if ((langId == null) || (langId.length() == 0))
/*     */       {
/* 171 */         boolean defaultLangId = SharedObjects.getEnvValueAsBoolean("DefaultSearchLangID", false);
/* 172 */         if ((ctxt != null) && (defaultLangId))
/*     */         {
/* 174 */           IdcLocale locale = (IdcLocale)ctxt.getCachedObject("UserLocale");
/* 175 */           if (locale != null)
/*     */           {
/* 177 */             String idcLangId = locale.m_name;
/* 178 */             DataResultSet vcConfig = SharedObjects.getTable("SearchLocaleConfig");
/* 179 */             if (vcConfig == null)
/*     */             {
/* 181 */               throw new DataException(LocaleUtils.encodeMessage("csIndexerSearchLocaleConfigMissing", null, LegacyDirectoryLocator.getResourcesDirectory()));
/*     */             }
/*     */ 
/* 187 */             langId = ResultSetUtils.findValue(vcConfig, "lcLocaleId", idcLangId, "lcSearchLangId");
/* 188 */             binder.putLocal("LangID", langId);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 193 */       if ((langId == null) || (langId.length() == 0) || (langId.equalsIgnoreCase("none")))
/*     */       {
/* 195 */         binder.putLocal("UseFullTextValueEnclosure", "true");
/*     */       }
/*     */     }
/*     */ 
/* 199 */     return super.prepareQueryText(binder, ctxt);
/*     */   }
/*     */ 
/*     */   public int prepareQuery(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 205 */     String queryText = binder.getLocal("QueryText");
/* 206 */     String searchLocale = SearchLoader.m_locale;
/*     */ 
/* 208 */     if ((searchLocale != null) && (searchLocale.equalsIgnoreCase("uni")) && (queryText != null) && (queryText.length() > 0))
/*     */     {
/* 210 */       String langId = binder.getLocal("LangID");
/*     */ 
/* 212 */       if ((langId != null) && (langId.length() > 0) && (!langId.equalsIgnoreCase("none")))
/*     */       {
/* 214 */         queryText = "<lang/" + langId + "> " + queryText;
/* 215 */         binder.putLocal("QueryText", queryText);
/*     */       }
/*     */     }
/*     */ 
/* 219 */     return 0;
/*     */   }
/*     */ 
/*     */   public int appendQueryTextFilters(DataBinder binder, String filterStr)
/*     */     throws DataException
/*     */   {
/* 225 */     byte[] b = { 1 };
/* 226 */     String sp = new String(b);
/* 227 */     Vector filters = new IdcVector();
/* 228 */     if ((filterStr != null) && (filterStr.length() != 0))
/*     */     {
/* 230 */       filters = StringUtils.parseArray(filterStr, ',', ',');
/*     */     }
/*     */ 
/* 233 */     String queryText = binder.getLocal("QueryText");
/* 234 */     if (queryText == null)
/*     */     {
/* 237 */       return 1;
/*     */     }
/*     */ 
/* 240 */     IdcStringBuilder buffer = new IdcStringBuilder();
/*     */ 
/* 244 */     for (int i = 0; i < queryText.length(); ++i)
/*     */     {
/* 246 */       if (queryText.charAt(i) <= '\007')
/*     */         continue;
/* 248 */       buffer.append(queryText.charAt(i));
/*     */     }
/*     */ 
/* 252 */     int len = filters.size();
/* 253 */     boolean appendSeparator = true;
/* 254 */     CommonSearchConfig searchConfig = (CommonSearchConfig)SharedObjects.getObject("globalObjects", "CommonSearchConfig");
/*     */ 
/* 256 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 258 */       String key = (String)filters.elementAt(i);
/* 259 */       String value = binder.getLocal(key);
/* 260 */       if ((value == null) || (value.length() <= 0))
/*     */         continue;
/* 262 */       if (buffer.length() > 0)
/*     */       {
/* 264 */         buffer.insert(0, '(');
/* 265 */         buffer.append(')');
/* 266 */         if (appendSeparator)
/*     */         {
/* 268 */           buffer.append(sp);
/* 269 */           appendSeparator = false;
/*     */         }
/*     */         else
/*     */         {
/* 273 */           buffer.append(' ');
/* 274 */           searchConfig.appendClauseElement(buffer, "AND", null, null, null);
/* 275 */           buffer.append(' ');
/*     */         }
/*     */       }
/*     */ 
/* 279 */       if (appendSeparator)
/*     */       {
/* 281 */         buffer.append(sp);
/* 282 */         appendSeparator = false;
/*     */       }
/* 284 */       buffer.append('(');
/* 285 */       buffer.append(value);
/* 286 */       buffer.append(')');
/*     */     }
/*     */ 
/* 289 */     binder.putLocal("QueryText", buffer.toString());
/* 290 */     return 1;
/*     */   }
/*     */ 
/*     */   public void translateFullTextQuery(IdcAppendable appendable, char[] queryChars, DataBinder binder, ParsedQueryElements parsedElts, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 299 */       InternetSearchQueryUtils.parseInternetSearch(appendable, "", "fulltext", new String(queryChars), this.m_queryDefinitionLabel, binder, parsedElts, context, this.m_config);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 304 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isQueryContainsNativeSyntax(char[] valueChars)
/*     */   {
/* 311 */     boolean contains = false;
/* 312 */     boolean isOpenQuote = false;
/* 313 */     for (int i = 0; (i < valueChars.length) && (!contains); ++i)
/*     */     {
/* 315 */       switch (valueChars[i])
/*     */       {
/*     */       case '\\':
/* 319 */         ++i;
/* 320 */         break;
/*     */       case '"':
/* 323 */         isOpenQuote = !isOpenQuote;
/* 324 */         break;
/*     */       case '<':
/*     */       case '=':
/*     */       case '>':
/*     */       case '`':
/* 329 */         if (isOpenQuote)
/*     */           continue;
/* 331 */         contains = true;
/*     */       }
/*     */     }
/*     */ 
/* 335 */     return contains;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 340 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.VeritySearchConfigCompanion
 * JD-Core Version:    0.5.4
 */