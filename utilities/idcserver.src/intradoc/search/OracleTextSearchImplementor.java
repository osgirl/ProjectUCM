/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.CallableResults;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.indexer.OracleTextUtils;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ import java.io.StringReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Comparator;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.TreeSet;
/*     */ import java.util.Vector;
/*     */ import org.xml.sax.InputSource;
/*     */ import org.xml.sax.XMLReader;
/*     */ import org.xml.sax.helpers.XMLReaderFactory;
/*     */ 
/*     */ public class OracleTextSearchImplementor extends CommonSearchAdaptor
/*     */ {
/*     */   protected Workspace m_workspace;
/*     */   protected String m_result;
/*     */   protected DataBinder m_resultBinder;
/*     */   protected CommonSearchConnection m_csConn;
/*     */   protected String m_tagSet;
/*     */   protected int m_maxDocSize;
/*     */   protected String m_startTag;
/*     */   protected String m_endTag;
/*     */   protected String m_prevTag;
/*     */   protected String m_nextTag;
/*     */   protected int m_numDrillDownFields;
/*     */   protected char m_drillDownFieldSep;
/*     */   protected List<String> m_drillDownFields;
/*     */   protected boolean m_allowConfigChangeEachTime;
/*     */   protected String m_defaultColumns;
/*     */   protected String m_collectionName;
/*     */   protected boolean m_releaseConnectionNeeded;
/*     */   protected int m_flags;
/*     */   protected boolean m_disableSAXParsing;
/*     */   protected final int DEBUG = 1;
/*     */   protected final int SNIPPET = 2;
/*     */ 
/*     */   public OracleTextSearchImplementor()
/*     */   {
/*  39 */     this.m_tagSet = "HTML_NAVIGATE";
/*  40 */     this.m_maxDocSize = 1000000;
/*  41 */     this.m_startTag = "<A Name=SCS%CURNUM Style='background-color: yellow; color= black'><STRONG><B>";
/*  42 */     this.m_endTag = "</B></STRONG></A>";
/*  43 */     this.m_prevTag = "<A HREF='#SCS%PREVNUM'>&lt;&lt;</A>";
/*  44 */     this.m_nextTag = "<A HREF='#SCS%NEXTNUM'>&gt;&gt;</A>";
/*     */ 
/*  46 */     this.m_numDrillDownFields = 2;
/*  47 */     this.m_drillDownFieldSep = ',';
/*  48 */     this.m_drillDownFields = null;
/*     */ 
/*  50 */     this.m_allowConfigChangeEachTime = true;
/*     */ 
/*  55 */     this.m_releaseConnectionNeeded = false;
/*     */ 
/*  57 */     this.m_flags = 0;
/*     */ 
/*  59 */     this.m_disableSAXParsing = false;
/*     */ 
/*  61 */     this.DEBUG = 1;
/*  62 */     this.SNIPPET = 2;
/*     */   }
/*     */ 
/*     */   public void init(CommonSearchConnection sc)
/*     */   {
/*  67 */     this.m_csConn = sc;
/*     */   }
/*     */ 
/*     */   public boolean prepareUse(ExecutionContext ctxt)
/*     */   {
/*  73 */     if (ctxt == null)
/*     */     {
/*  75 */       return false;
/*     */     }
/*  77 */     super.prepareUse(ctxt);
/*     */ 
/*  79 */     String providerName = this.m_csConn.m_queryConfig.getEngineValue("IndexerDatabaseProviderName");
/*  80 */     Workspace ws = null;
/*  81 */     if ((providerName != null) && (providerName.length() != 0) && (!providerName.equalsIgnoreCase("systemdatabase")))
/*     */     {
/*  83 */       Provider prov = Providers.getProvider(providerName);
/*  84 */       if (prov != null) if (prov.checkState("IsStarted", false))
/*     */         {
/*  91 */           ws = (Workspace)prov.getProvider();
/*  92 */           this.m_releaseConnectionNeeded = true;
/*     */         }
/*     */     }
/*     */     else
/*     */     {
/*  97 */       ws = (Workspace)ctxt.getCachedObject("Workspace");
/*  98 */       if ((ws == null) && 
/* 100 */         (ctxt instanceof Service))
/*     */       {
/* 102 */         ws = ((Service)ctxt).getWorkspace();
/*     */       }
/*     */     }
/*     */ 
/* 106 */     if (ws == null)
/*     */     {
/* 108 */       return false;
/*     */     }
/*     */ 
/* 111 */     this.m_workspace = ws;
/* 112 */     OracleTextUtils.initColumnMappings(ws);
/*     */ 
/* 114 */     if (this.m_allowConfigChangeEachTime)
/*     */     {
/* 116 */       this.m_tagSet = this.m_csConn.m_queryConfig.getEngineValue("OracleTextHighlightType");
/* 117 */       String maxSize = this.m_csConn.m_queryConfig.getEngineValue("OracleTextHighlightMaxDocSize");
/* 118 */       if (maxSize != null)
/*     */       {
/* 120 */         this.m_maxDocSize = NumberUtils.parseInteger(maxSize, 5000000);
/*     */       }
/*     */ 
/* 123 */       this.m_startTag = this.m_csConn.m_queryConfig.getEngineValue("OracleHighlightStartTag");
/* 124 */       this.m_endTag = this.m_csConn.m_queryConfig.getEngineValue("OracleHighlightEndTag");
/* 125 */       this.m_prevTag = this.m_csConn.m_queryConfig.getEngineValue("OracleHighlightPrevTag");
/* 126 */       this.m_nextTag = this.m_csConn.m_queryConfig.getEngineValue("OracleHighlightNextTag");
/* 127 */       String snippet = this.m_csConn.m_queryConfig.getEngineValue("OracleTextDisableSearchSnippet");
/* 128 */       boolean enableDBSnippet = !StringUtils.convertToBool(snippet, false);
/* 129 */       this.m_flags = ((enableDBSnippet) ? 2 : 0);
/*     */ 
/* 133 */       initDrillDownFields();
/*     */ 
/* 135 */       this.m_disableSAXParsing = StringUtils.convertToBool(this.m_csConn.m_queryConfig.getEngineValue("DisableDrillDownParsingWithSAX"), false);
/* 136 */       String allowChangeEachTime = this.m_csConn.m_queryConfig.getEngineValue("AllowConfigChangeEachTime");
/* 137 */       this.m_allowConfigChangeEachTime = StringUtils.convertToBool(allowChangeEachTime, false);
/*     */     }
/*     */ 
/* 140 */     return true;
/*     */   }
/*     */ 
/*     */   public void initDrillDownFields()
/*     */   {
/* 148 */     String drillDownFields = SharedObjects.getEnvironmentValue("DrillDownFields");
/*     */ 
/* 150 */     if (drillDownFields == null)
/*     */     {
/* 152 */       drillDownFields = this.m_csConn.m_queryConfig.getEngineValue("DrillDownFields");
/*     */     }
/* 154 */     if (drillDownFields == null)
/*     */     {
/* 156 */       drillDownFields = "dDocType,dSecurityGroup,dDocAccount";
/*     */     }
/* 158 */     this.m_drillDownFields = StringUtils.makeListFromSequence(drillDownFields, ',', '^', 32);
/* 159 */     this.m_numDrillDownFields = this.m_drillDownFields.size();
/*     */   }
/*     */ 
/*     */   public String doQuery(DataBinder binder)
/*     */   {
/* 165 */     int flag = this.m_flags;
/* 166 */     if (SystemUtils.isActiveTrace("search"))
/*     */     {
/* 168 */       flag |= 1;
/*     */     }
/* 170 */     binder.putLocal("procFlags", "" + flag);
/* 171 */     String querySelection = binder.getLocal("QuerySelection");
/* 172 */     String queryText = binder.getLocal("WhereClause");
/*     */ 
/* 174 */     String parsedQueryTextNoSecurity = binder.getLocal("ParsedQueryTextNoSecurity");
/* 175 */     if ((queryText == null) || (queryText.trim().length() == 0))
/*     */     {
/* 177 */       queryText = "idccontenttrue";
/*     */     }
/* 179 */     binder.putLocal("queryText", queryText);
/*     */ 
/* 181 */     if ((parsedQueryTextNoSecurity == null) || (parsedQueryTextNoSecurity.trim().length() == 0))
/*     */     {
/* 183 */       parsedQueryTextNoSecurity = "idccontenttrue";
/*     */     }
/* 185 */     binder.putLocal("parsedQueryTextNoSecurity", parsedQueryTextNoSecurity);
/* 186 */     binder.putLocal("resultDescriptor", querySelection);
/*     */ 
/* 188 */     String queryCollection = binder.getLocal("QueryCollection");
/*     */ 
/* 190 */     String tableName = "IdcText2";
/* 191 */     String indexName = "FT_IdcText2";
/* 192 */     if ((queryCollection == null) || (queryCollection.equalsIgnoreCase("ots1")))
/*     */     {
/* 194 */       tableName = "IdcText1";
/* 195 */       indexName = "FT_IdcText1";
/*     */     }
/* 197 */     binder.putLocal("tableName", tableName);
/* 198 */     binder.putLocal("indexName", indexName);
/*     */ 
/* 200 */     DataBinder rBinder = new DataBinder();
/* 201 */     CallableResults results = null;
/* 202 */     boolean isError = false;
/*     */     try
/*     */     {
/* 205 */       if (((queryCollection != null) && (((this.m_collectionName == null) || (!this.m_collectionName.equalsIgnoreCase(queryCollection))))) || (this.m_collectionName == null))
/*     */       {
/* 207 */         String[] columnArr = WorkspaceUtils.getColumnList(tableName, this.m_workspace, new String[] { "otsContent", "otsMeta", "otsCounter" });
/* 208 */         this.m_defaultColumns = StringUtils.createString(StringUtils.convertToList(columnArr), ',', '^');
/* 209 */         this.m_collectionName = queryCollection;
/*     */       }
/* 211 */       binder.putLocal("returnFields", this.m_defaultColumns);
/*     */ 
/* 213 */       boolean disableSnippetForSystemClause = SharedObjects.getEnvValueAsBoolean("DisableSnippetForSystemClause", false);
/* 214 */       if (disableSnippetForSystemClause)
/*     */       {
/* 217 */         results = this.m_workspace.executeCallable("CotsSearchQueryFullTextSnippet", binder);
/*     */       }
/*     */       else
/*     */       {
/* 221 */         results = this.m_workspace.executeCallable("CotsSearchQuery", binder);
/*     */       }
/*     */ 
/* 224 */       ResultSet rset = (ResultSet)results.getObject("metaResult");
/* 225 */       ResultSet base = (ResultSet)results.getObject("baseResult");
/* 226 */       rset.setDateFormat(LocaleResources.m_iso8601Format);
/* 227 */       base.setDateFormat(LocaleResources.m_iso8601Format);
/* 228 */       int count = results.getInteger("count");
/* 229 */       DataResultSet drset = assembleResult(base, rset);
/* 230 */       if (count < drset.getNumRows())
/*     */       {
/* 232 */         Report.trace("search", "The number of rows reported by result set interface (" + count + ") was less than actual rows returned (" + drset.getNumRows() + ")-- fixing", null);
/*     */ 
/* 235 */         count = drset.getNumRows();
/*     */       }
/*     */ 
/* 238 */       rBinder.putLocal("TotalRows", "" + count);
/* 239 */       rBinder.addResultSet("SearchResults", drset);
/*     */ 
/* 241 */       String rsi = results.getString("result");
/* 242 */       int beginIndex = rsi.indexOf("<groups ");
/* 243 */       if (beginIndex >= 0)
/*     */       {
/* 245 */         int endIndex = rsi.lastIndexOf("</groups>");
/* 246 */         if (endIndex > beginIndex)
/*     */         {
/* 248 */           String drillDown = rsi.substring(beginIndex, endIndex + 9);
/* 249 */           if ((drillDown != null) && (drillDown.trim().length() > 0))
/*     */           {
/* 251 */             if (this.m_disableSAXParsing)
/*     */             {
/* 253 */               processDrillDownInfoManual(drillDown, rBinder);
/*     */             }
/*     */             else
/*     */             {
/* 257 */               processDrillDownInfoEx(drillDown, rBinder);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 263 */       if (Report.m_verbose)
/*     */       {
/* 265 */         String trace = results.getString("trace");
/* 266 */         Report.debug("search", "ResultSetInterface results: " + rsi, null);
/* 267 */         Report.debug("search", "Trace: " + trace, null);
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 272 */       isError = true;
/* 273 */       Report.debug("search", null, t);
/* 274 */       rBinder.putLocal("isSearchError", "1");
/*     */ 
/* 276 */       String msg = t.getMessage();
/* 277 */       if ((msg != null) && (msg.contains("PLS-00201")))
/*     */       {
/* 279 */         msg = LocaleUtils.encodeMessage("csOracleTextProcedureMayNotDefined", msg);
/*     */       }
/* 281 */       rBinder.putLocal("StatusMessageKey", msg);
/* 282 */       rBinder.putLocal("StatusMessage", msg);
/* 283 */       rBinder.putLocal("StatusCode", "-32");
/*     */     }
/*     */     finally
/*     */     {
/* 287 */       if (results != null)
/*     */       {
/* 289 */         results.close();
/*     */       }
/* 291 */       if (this.m_releaseConnectionNeeded)
/*     */       {
/* 293 */         this.m_workspace.releaseConnection();
/*     */       }
/*     */     }
/*     */ 
/* 297 */     this.m_resultBinder = rBinder;
/*     */ 
/* 299 */     if (isError)
/*     */     {
/* 301 */       return getResult();
/*     */     }
/* 303 */     return null;
/*     */   }
/*     */ 
/*     */   protected void processDrillDownInfoManual(String drillDown, DataBinder binder)
/*     */     throws Exception
/*     */   {
/* 309 */     Report.trace("search", "Start parsing drill down fields...", null);
/* 310 */     List navRSets = new ArrayList();
/*     */ 
/* 315 */     initDrillDownFields();
/*     */ 
/* 317 */     for (int i = 0; i < this.m_numDrillDownFields; ++i)
/*     */     {
/* 319 */       DataResultSet drset = new DataResultSet(new String[] { "drillDownOptionValue", "drillDownModifier", "count", "fieldName" });
/* 320 */       navRSets.add(drset);
/*     */     }
/*     */ 
/* 323 */     int beginIndex = 0;
/* 324 */     int numGroups = 0;
/* 325 */     int totalCount = 0;
/* 326 */     while (beginIndex >= 0)
/*     */     {
/* 328 */       beginIndex = drillDown.indexOf("value=\"", beginIndex);
/* 329 */       if (beginIndex < 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 333 */       beginIndex += 7;
/*     */ 
/* 335 */       int endIndex = drillDown.indexOf("\"", beginIndex);
/* 336 */       if (endIndex < 0)
/*     */       {
/* 338 */         beginIndex = endIndex;
/*     */       }
/*     */ 
/* 341 */       String value = drillDown.substring(beginIndex, endIndex);
/* 342 */       beginIndex = drillDown.indexOf("<count>", endIndex);
/* 343 */       if (beginIndex < 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 347 */       beginIndex += 7;
/* 348 */       endIndex = drillDown.indexOf(60, beginIndex);
/* 349 */       if (endIndex < 0)
/*     */       {
/* 351 */         beginIndex = endIndex;
/*     */       }
/*     */ 
/* 355 */       String countStr = drillDown.substring(beginIndex, endIndex);
/*     */ 
/* 357 */       int count = NumberUtils.parseInteger(countStr, 0);
/*     */ 
/* 359 */       totalCount += count;
/* 360 */       List keyList = StringUtils.makeListFromSequence(value, this.m_drillDownFieldSep, '^', 0);
/* 361 */       if (keyList.size() == this.m_numDrillDownFields)
/*     */       {
/* 363 */         for (int i = 0; i < this.m_numDrillDownFields; ++i)
/*     */         {
/* 365 */           DataResultSet drset = (DataResultSet)navRSets.get(i);
/* 366 */           String key = (String)keyList.get(i);
/* 367 */           if ((key != null) && (key.equalsIgnoreCase("idcnull")))
/*     */           {
/* 369 */             key = "";
/*     */           }
/* 371 */           key = OracleTextUtils.decodeValue(key, true);
/* 372 */           Vector row = drset.findRow(0, key);
/* 373 */           if (row == null)
/*     */           {
/* 375 */             row = new Vector();
/* 376 */             row.add(key);
/* 377 */             row.add(key);
/* 378 */             row.add("" + count);
/* 379 */             row.add("" + (String)this.m_drillDownFields.get(i));
/* 380 */             drset.addRow(row);
/*     */           }
/*     */           else
/*     */           {
/* 384 */             int tmpCount = count + NumberUtils.parseInteger((String)row.elementAt(2), 0);
/* 385 */             row.set(2, "" + tmpCount);
/*     */           }
/*     */         }
/*     */       }
/* 389 */       ++numGroups;
/*     */     }
/*     */ 
/* 392 */     DataResultSet fields = new DataResultSet(new String[] { "drillDownFieldName", "drillDownDisplayValue", "categoryCount", "totalCount" });
/* 393 */     for (int i = 0; i < this.m_numDrillDownFields; ++i)
/*     */     {
/* 395 */       DataResultSet drset = (DataResultSet)navRSets.get(i);
/* 396 */       String fieldName = (String)this.m_drillDownFields.get(i);
/* 397 */       String fieldCaption = null;
/* 398 */       ResultSet metaDefs = SharedObjects.getTable("DocMetaDefinition");
/*     */       try
/*     */       {
/* 401 */         fieldCaption = ResultSetUtils.findValue(metaDefs, "dName", fieldName, "dCaption");
/* 402 */         ResultSetUtils.sortResultSet(drset, new String[] { "drillDownOptionValue" });
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 406 */         Report.trace("search", null, e);
/*     */       }
/* 408 */       binder.addResultSetDirect("SearchResultNavigation" + fieldName, drset);
/*     */ 
/* 410 */       Vector row = new Vector();
/* 411 */       row.add(fieldName);
/* 412 */       if (fieldCaption != null)
/*     */       {
/* 414 */         row.add(fieldCaption);
/*     */       }
/*     */       else
/*     */       {
/* 418 */         row.add("ww" + fieldName);
/*     */       }
/* 420 */       row.add("" + drset.getNumRows());
/* 421 */       row.add("" + totalCount);
/* 422 */       fields.addRow(row);
/*     */     }
/*     */ 
/* 425 */     binder.addResultSet("SearchResultNavigation", fields);
/*     */ 
/* 427 */     Report.trace("search", "Completed parsing " + numGroups + " groups.", null);
/*     */   }
/*     */ 
/*     */   protected void processDrillDownInfoEx(String drillDown, DataBinder binder) throws Exception
/*     */   {
/* 432 */     Report.trace("search", "Start parsing drill down fields with SAX...", null);
/*     */ 
/* 437 */     initDrillDownFields();
/*     */ 
/* 439 */     DrillDownContentHandler handler = new DrillDownContentHandler();
/* 440 */     handler.init(binder, this.m_drillDownFields, this.m_drillDownFieldSep);
/* 441 */     XMLReader adapter = XMLReaderFactory.createXMLReader();
/* 442 */     adapter.setContentHandler(handler);
/* 443 */     InputSource source = new InputSource();
/* 444 */     source.setCharacterStream(new StringReader(drillDown));
/*     */ 
/* 446 */     adapter.parse(source);
/*     */ 
/* 448 */     Report.trace("search", "Completed parsing " + handler.m_numGroups + " groups.", null);
/*     */   }
/*     */ 
/*     */   protected DataResultSet assembleResult(ResultSet base, ResultSet meta)
/*     */     throws Exception
/*     */   {
/* 461 */     DataResultSet baseDrset = new DataResultSet();
/* 462 */     baseDrset.copyFieldInfo(base);
/* 463 */     baseDrset.copy(base);
/*     */ 
/* 465 */     HashMap indexMap = buildIndexMap(baseDrset);
/*     */ 
/* 470 */     FieldInfo[] extraFields = findExtraFields(meta, base);
/*     */ 
/* 472 */     ArrayList rows = new ArrayList(baseDrset.getNumRows());
/* 473 */     int numRows = baseDrset.getNumRows();
/* 474 */     for (int i = 0; i < numRows; ++i)
/*     */     {
/* 476 */       rows.add(null);
/*     */     }
/*     */ 
/* 479 */     FieldInfo fi = new FieldInfo();
/* 480 */     meta.getFieldInfo("dID", fi);
/* 481 */     if (fi.m_index < 0)
/*     */     {
/* 484 */       throw new ServiceException("csSearchResultsContainsNodID");
/*     */     }
/*     */ 
/* 487 */     String did = null;
/* 488 */     for (; meta.isRowPresent(); meta.next())
/*     */     {
/* 490 */       did = meta.getStringValue(fi.m_index);
/* 491 */       Integer index = (Integer)indexMap.remove(did);
/* 492 */       if (index == null)
/*     */       {
/* 495 */         Report.trace("search", "metaResult and baseResult mismatch. Entry with id of '" + did + "' is not found in baseResult.", null);
/*     */       }
/*     */       else
/*     */       {
/* 500 */         Vector row = new Vector();
/* 501 */         int size = meta.getNumFields();
/* 502 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 504 */           String value = meta.getStringValue(i);
/* 505 */           if (value.equals("idcnull"))
/*     */           {
/* 507 */             value = "";
/*     */           }
/* 509 */           row.addElement(value);
/*     */         }
/*     */ 
/* 512 */         for (int i = 0; i < extraFields.length; ++i)
/*     */         {
/* 514 */           baseDrset.setCurrentRow(index.intValue());
/* 515 */           String value = baseDrset.getStringValue(extraFields[i].m_index);
/* 516 */           if (value.equals("idcnull"))
/*     */           {
/* 518 */             value = "";
/*     */           }
/*     */ 
/* 521 */           if (extraFields[i].m_name.equalsIgnoreCase("srfDocSnippet"))
/*     */           {
/* 523 */             value = postProcessSnippet(value);
/*     */           }
/*     */ 
/* 526 */           row.addElement(value);
/*     */         }
/* 528 */         rows.set(index.intValue(), row);
/*     */       }
/*     */     }
/*     */ 
/* 532 */     removeAdditionalRows(rows, indexMap);
/*     */ 
/* 534 */     IdcDateFormat format = meta.getDateFormat();
/*     */ 
/* 536 */     DataResultSet drset = new DataResultSet();
/* 537 */     drset.copyFieldInfo(meta);
/* 538 */     drset.setDateFormat(format);
/* 539 */     drset.mergeFieldsWithFlags(Arrays.asList(extraFields), 2);
/* 540 */     for (Vector row : rows)
/*     */     {
/* 542 */       if (row != null)
/*     */       {
/* 544 */         drset.addRow(row);
/*     */       }
/*     */     }
/*     */ 
/* 548 */     return drset;
/*     */   }
/*     */ 
/*     */   protected String postProcessSnippet(String value)
/*     */   {
/* 553 */     String processedValue = value;
/*     */ 
/* 558 */     processedValue = processedValue.replace("idcnull", "");
/* 559 */     processedValue = processedValue.replace("idccontenttrue", "");
/*     */ 
/* 561 */     boolean disablePostProcessSnippet = SharedObjects.getEnvValueAsBoolean("DisablePostProcessSnippet", false);
/*     */ 
/* 563 */     if (!disablePostProcessSnippet)
/*     */     {
/* 565 */       List snippetTokenList = parseSnippetIntoTokens(processedValue);
/*     */ 
/* 567 */       for (int tokenNo = 0; tokenNo < snippetTokenList.size(); ++tokenNo)
/*     */       {
/* 569 */         IdcStringBuilder snippetToken = (IdcStringBuilder)snippetTokenList.get(tokenNo);
/*     */ 
/* 571 */         if ((snippetToken == null) || (snippetToken.length() <= 0))
/*     */           continue;
/* 573 */         String snippetTokenString = snippetToken.toString();
/*     */ 
/* 577 */         if ((!snippetTokenString.startsWith("z")) && (!snippetTokenString.startsWith("Z")))
/*     */           continue;
/* 579 */         boolean isHexValue = NumberUtils.parseHexValue(snippetTokenString.toCharArray(), 1, snippetTokenString.length(), new long[1]);
/* 580 */         if (isHexValue != true)
/*     */           continue;
/* 582 */         processedValue = processedValue.replace(snippetTokenString, "");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 589 */     return processedValue;
/*     */   }
/*     */ 
/*     */   protected List parseSnippetIntoTokens(String snippet)
/*     */   {
/* 594 */     char[] snippetCharArray = snippet.toCharArray();
/* 595 */     int tokenNumber = -1;
/*     */ 
/* 597 */     List snippetTokens = new ArrayList();
/* 598 */     boolean isNewToken = true;
/* 599 */     IdcStringBuilder token = new IdcStringBuilder();
/*     */ 
/* 601 */     for (int charNo = 0; charNo < snippet.length(); ++charNo)
/*     */     {
/* 603 */       char snippetCharacter = snippetCharArray[charNo];
/* 604 */       switch (snippetCharacter)
/*     */       {
/*     */       case '\n':
/*     */       case ' ':
/* 609 */         isNewToken = true;
/* 610 */         break;
/*     */       default:
/* 614 */         if (isNewToken == true)
/*     */         {
/* 616 */           snippetTokens.add(new IdcStringBuilder());
/* 617 */           tokenNumber += 1;
/* 618 */           token = (IdcStringBuilder)snippetTokens.get(tokenNumber);
/* 619 */           isNewToken = false;
/*     */         }
/*     */ 
/* 622 */         token.append(snippetCharacter);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 627 */     return snippetTokens;
/*     */   }
/*     */ 
/*     */   protected void removeAdditionalRows(ArrayList<Vector> rows, HashMap indexMap)
/*     */   {
/* 632 */     if ((indexMap == null) || (indexMap.size() == 0))
/*     */     {
/* 634 */       return;
/*     */     }
/*     */ 
/* 637 */     Comparator c = new Comparator()
/*     */     {
/*     */       public int compare(Object o1, Object o2) {
/* 640 */         return ((Integer)o2).intValue() - ((Integer)o1).intValue();
/*     */       }
/*     */     };
/* 643 */     TreeSet tset = new TreeSet(c);
/* 644 */     tset.addAll(indexMap.values());
/*     */ 
/* 646 */     for (Integer index : tset)
/*     */     {
/* 648 */       Vector row = (Vector)rows.remove(index.intValue());
/* 649 */       if ((row != null) && (SystemUtils.m_verbose))
/*     */       {
/* 651 */         Report.debug("search", "Removing row:" + index, null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected FieldInfo[] findExtraFields(ResultSet destRset, ResultSet srcRset)
/*     */   {
/* 658 */     int numFields = srcRset.getNumFields();
/* 659 */     ArrayList infos = new ArrayList();
/*     */ 
/* 661 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 663 */       FieldInfo fi = new FieldInfo();
/* 664 */       srcRset.getIndexFieldInfo(i, fi);
/* 665 */       FieldInfo dfi = new FieldInfo();
/* 666 */       if (destRset.getFieldInfo(fi.m_name, dfi))
/*     */         continue;
/* 668 */       infos.add(fi);
/*     */     }
/*     */ 
/* 672 */     FieldInfo[] finfos = new FieldInfo[infos.size()];
/* 673 */     infos.toArray(finfos);
/* 674 */     return finfos;
/*     */   }
/*     */ 
/*     */   public HashMap buildIndexMap(DataResultSet drset)
/*     */   {
/* 679 */     FieldInfo fi = new FieldInfo();
/* 680 */     drset.getFieldInfo("dID", fi);
/*     */ 
/* 682 */     if (fi.m_index == -1);
/* 688 */     HashMap indexMap = new HashMap();
/* 689 */     int counter = 0;
/* 690 */     String tmp = null;
/* 691 */     for (drset.first(); drset.isRowPresent(); ++counter)
/*     */     {
/* 693 */       tmp = drset.getStringValue(fi.m_index);
/* 694 */       indexMap.put(tmp, new Integer(counter));
/*     */ 
/* 691 */       drset.next();
/*     */     }
/*     */ 
/* 697 */     return indexMap;
/*     */   }
/*     */ 
/*     */   public String getResult()
/*     */   {
/* 703 */     if (this.m_result == null)
/*     */     {
/* 705 */       this.m_result = getString(this.m_resultBinder);
/*     */     }
/* 707 */     return this.m_result;
/*     */   }
/*     */ 
/*     */   public DataBinder getResultAsBinder()
/*     */   {
/* 713 */     return this.m_resultBinder;
/*     */   }
/*     */ 
/*     */   public String getString(DataBinder binder)
/*     */   {
/* 718 */     String result = null;
/*     */     try
/*     */     {
/* 721 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 722 */       binder.send(sw);
/* 723 */       result = sw.toStringRelease();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 728 */       SystemUtils.dumpException("search", e);
/*     */     }
/* 730 */     return result;
/*     */   }
/*     */ 
/*     */   public String retrieveHighlightInfo(DataBinder binder, int hlType, String hlBegin, String hlEnd)
/*     */   {
/* 738 */     String type = this.m_workspace.getProperty("DatabaseType");
/* 739 */     if (!type.equalsIgnoreCase("oracle"))
/*     */     {
/* 741 */       return super.retrieveHighlightInfo(binder, hlType, hlBegin, hlEnd);
/*     */     }
/*     */     try
/*     */     {
/* 745 */       String textQuery = binder.get("QueryText");
/* 746 */       String activeIndex = binder.get("ActiveIndex");
/*     */ 
/* 748 */       if ((activeIndex == null) || (activeIndex.equalsIgnoreCase("ots1")))
/*     */       {
/* 750 */         activeIndex = "FT_IdcText1";
/*     */       }
/*     */       else
/*     */       {
/* 754 */         activeIndex = "FT_IdcText2";
/*     */       }
/* 756 */       binder.putLocal("textQuery", textQuery);
/* 757 */       binder.putLocal("tagSet", this.m_tagSet);
/* 758 */       binder.putLocal("ftIndexName", activeIndex);
/* 759 */       binder.putLocal("hlBegin", this.m_startTag);
/* 760 */       binder.putLocal("hlEnd", this.m_endTag);
/* 761 */       binder.putLocal("prevTag", this.m_prevTag);
/* 762 */       binder.putLocal("nextTag", this.m_nextTag);
/* 763 */       CallableResults rset = this.m_workspace.executeCallable("CoracleTextHighlight", binder);
/* 764 */       setResult(rset);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 768 */       return CommonSearchConnection.createErrorMsg(e, "csSearchRetrieveHighlightInfoError");
/*     */     }
/*     */ 
/* 771 */     return null;
/*     */   }
/*     */ 
/*     */   public void setResult(CallableResults rset) throws DataException, IOException
/*     */   {
/* 776 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 777 */     Reader reader = rset.getReader("highlightClob");
/* 778 */     BufferedReader breader = null;
/*     */     try
/*     */     {
/* 782 */       breader = new BufferedReader(reader);
/* 783 */       char[] cbuf = new char[65526];
/* 784 */       int len = cbuf.length;
/* 785 */       int total = 0;
/* 786 */       while ((len = breader.read(cbuf, 0, len)) != -1)
/*     */       {
/* 788 */         buf.append(cbuf, 0, len);
/* 789 */         total += len;
/* 790 */         if (total > this.m_maxDocSize) {
/*     */           break;
/*     */         }
/*     */ 
/* 794 */         if (total + len > this.m_maxDocSize)
/*     */         {
/* 796 */           len = this.m_maxDocSize - total;
/*     */         }
/*     */ 
/* 800 */         len = cbuf.length;
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 806 */       FileUtils.closeObject(breader);
/*     */     }
/*     */ 
/* 809 */     this.m_result = buf.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 814 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103947 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.OracleTextSearchImplementor
 * JD-Core Version:    0.5.4
 */