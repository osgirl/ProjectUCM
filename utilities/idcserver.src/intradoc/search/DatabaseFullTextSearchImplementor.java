/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.CallableResults;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DatabaseFullTextSearchImplementor extends CommonSearchAdaptor
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
/*     */   protected boolean m_enableDBSnippet;
/*     */ 
/*     */   public DatabaseFullTextSearchImplementor()
/*     */   {
/*  36 */     this.m_tagSet = "HTML_NAVIGATE";
/*  37 */     this.m_maxDocSize = 1000000;
/*  38 */     this.m_startTag = "<A Name=SCS%CURNUM Style='background-color: yellow; color= black'><STRONG><B>";
/*  39 */     this.m_endTag = "</B></STRONG></A>";
/*  40 */     this.m_prevTag = "<A HREF='#SCS%PREVNUM'>&lt;&lt;</A>";
/*  41 */     this.m_nextTag = "<A HREF='#SCS%NEXTNUM'>&gt;&gt;</A>";
/*     */ 
/*  43 */     this.m_enableDBSnippet = false;
/*     */   }
/*     */ 
/*     */   public void init(CommonSearchConnection sc)
/*     */   {
/*  48 */     this.m_csConn = sc;
/*     */   }
/*     */ 
/*     */   public boolean prepareUse(ExecutionContext ctxt)
/*     */   {
/*  54 */     super.prepareUse(ctxt);
/*  55 */     Workspace ws = (Workspace)ctxt.getCachedObject("Workspace");
/*  56 */     if (ws == null)
/*     */     {
/*  58 */       if (ctxt instanceof Service)
/*     */       {
/*  60 */         ws = ((Service)ctxt).getWorkspace();
/*     */       }
/*     */       else
/*     */       {
/*  64 */         return false;
/*     */       }
/*     */     }
/*  67 */     this.m_workspace = ws;
/*     */ 
/*  70 */     this.m_tagSet = this.m_csConn.m_queryConfig.getEngineValue("OracleTextHighlightType");
/*  71 */     String maxSize = this.m_csConn.m_queryConfig.getEngineValue("OracleTextHighlightMaxDocSize");
/*  72 */     if (maxSize != null)
/*     */     {
/*  74 */       this.m_maxDocSize = NumberUtils.parseInteger(maxSize, 5000000);
/*     */     }
/*     */ 
/*  77 */     this.m_startTag = this.m_csConn.m_queryConfig.getEngineValue("OracleHighlightStartTag");
/*  78 */     this.m_endTag = this.m_csConn.m_queryConfig.getEngineValue("OracleHighlightEndTag");
/*  79 */     this.m_prevTag = this.m_csConn.m_queryConfig.getEngineValue("OracleHighlightPrevTag");
/*  80 */     this.m_nextTag = this.m_csConn.m_queryConfig.getEngineValue("OracleHighlightNextTag");
/*  81 */     String snippet = this.m_csConn.m_queryConfig.getEngineValue("EnableSearchSnippet");
/*  82 */     this.m_enableDBSnippet = StringUtils.convertToBool(snippet, true);
/*  83 */     return true;
/*     */   }
/*     */ 
/*     */   public String doQuery(DataBinder binder)
/*     */   {
/*  89 */     DataBinder origBinder = binder;
/*  90 */     String query = binder.getLocal("QueryAssembly");
/*  91 */     int startRow = Integer.parseInt(binder.getLocal("StartRow"));
/*  92 */     int resultCount = Integer.parseInt(binder.getLocal("ResultCount"));
/*     */ 
/*  94 */     String doubleQuery = binder.getAllowMissing("DisableTotalItemsSearchQuery");
/*  95 */     boolean disableDoubleQuery = StringUtils.convertToBool(doubleQuery, false);
/*  96 */     String querySelection = binder.getLocal("QuerySelection");
/*  97 */     String sortSpec = binder.getLocal("SortSpec");
/*  98 */     int index = 0;
/*     */ 
/* 100 */     if (!disableDoubleQuery)
/*     */     {
/* 102 */       if ((querySelection == null) || (querySelection.length() == 0))
/*     */       {
/* 104 */         String queryTmp = query.toLowerCase();
/* 105 */         while ((index = queryTmp.indexOf("from", index)) >= 0)
/*     */         {
/* 107 */           if ((index < queryTmp.length() - 5) && (Validation.isSpace(queryTmp.charAt(index - 1))) && (Validation.isSpace(queryTmp.charAt(index + 4))))
/*     */           {
/* 111 */             --index;
/* 112 */             break;
/*     */           }
/* 114 */           ++index;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 119 */         index = query.indexOf(querySelection) + querySelection.length();
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 124 */       resultCount += 1;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 129 */       if ((sortSpec != null) && (sortSpec.trim().length() > 0) && (!QueryUtils.validateQuerySortClause(sortSpec)))
/*     */       {
/* 132 */         String msg = LocaleUtils.encodeMessage("csSearchInvalidOrderByClause", null, sortSpec);
/* 133 */         throw new DataException(msg);
/*     */       }
/* 135 */       ResultSet rset = this.m_workspace.createResultSetSQL(query);
/* 136 */       rset.setDateFormat(LocaleResources.m_iso8601Format);
/* 137 */       DataResultSet drset = new DataResultSet();
/* 138 */       drset.copyFieldInfo(rset);
/* 139 */       binder = new DataBinder();
/*     */ 
/* 141 */       int skippedRow = rset.skip(startRow - 1);
/* 142 */       if ((startRow == 0) || (skippedRow == startRow - 1))
/*     */       {
/* 144 */         drset.copy(rset, resultCount);
/*     */       }
/* 146 */       if ((!disableDoubleQuery) && (index > 0))
/*     */       {
/* 148 */         String countQuery = "SELECT COUNT(*) " + query.substring(index);
/*     */ 
/* 150 */         if ((sortSpec != null) && (sortSpec.length() != 0))
/*     */         {
/* 152 */           index = countQuery.length() - sortSpec.length();
/*     */         }
/*     */         else
/*     */         {
/* 156 */           String queryTmp = countQuery.toLowerCase();
/* 157 */           while ((index = queryTmp.indexOf("order", index)) >= 0)
/*     */           {
/* 160 */             if ((index < queryTmp.length() - 6) && (Validation.isSpace(queryTmp.charAt(index - 1))) && (Validation.isSpace(queryTmp.charAt(index + 5))))
/*     */             {
/* 164 */               String tmp = queryTmp.substring(index + 6).trim();
/* 165 */               if ((tmp.startsWith("by")) && (Validation.isSpace(tmp.charAt(3))) && (tmp.indexOf('\'') < 0) && (tmp.indexOf('"') < 0))
/*     */               {
/* 169 */                 --index;
/* 170 */                 break;
/*     */               }
/*     */             }
/*     */ 
/* 174 */             ++index;
/*     */           }
/*     */         }
/* 177 */         if (index > 0)
/*     */         {
/* 179 */           countQuery = countQuery.substring(0, index);
/*     */         }
/* 181 */         rset = this.m_workspace.createResultSetSQL(countQuery);
/* 182 */         if (rset.isRowPresent())
/*     */         {
/* 184 */           String totalRows = rset.getStringValue(0);
/* 185 */           binder.putLocal("TotalRows", totalRows);
/*     */         }
/*     */       }
/* 188 */       else if (drset.isCopyAborted())
/*     */       {
/* 190 */         drset.last();
/* 191 */         drset.deleteCurrentRow();
/* 192 */         binder.putLocal("HasMoreRows", "1");
/*     */       }
/*     */ 
/* 195 */       if (this.m_enableDBSnippet)
/*     */       {
/* 197 */         updateResultWithSnippet(drset, origBinder);
/*     */       }
/* 199 */       binder.addResultSet("SearchResults", drset);
/* 200 */       this.m_resultBinder = binder;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 204 */       return CommonSearchConnection.createErrorMsg(e, "csSearchUnableToRetrieveSearchResult");
/*     */     }
/* 206 */     return null;
/*     */   }
/*     */ 
/*     */   public void updateResultWithSnippet(DataResultSet drset, DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 212 */     String type = this.m_workspace.getProperty("DatabaseType");
/* 213 */     String version = this.m_workspace.getProperty("DatabaseVersion");
/* 214 */     if ((!type.equalsIgnoreCase("oracle")) || (version.compareTo("10.2") < 0))
/*     */     {
/* 216 */       return;
/*     */     }
/*     */ 
/* 219 */     FieldInfo fids = new FieldInfo();
/* 220 */     fids.m_name = "srfDocSnippet";
/* 221 */     Vector fieldInfo = new IdcVector();
/* 222 */     fieldInfo.add(fids);
/* 223 */     drset.mergeFieldsWithFlags(fieldInfo, 2);
/*     */ 
/* 225 */     String textQuery = getTextQuery(binder);
/* 226 */     if ((textQuery == null) || (textQuery.length() == 0))
/*     */     {
/* 228 */       return;
/*     */     }
/* 230 */     binder.putLocal("textQuery", textQuery);
/* 231 */     int snippetBatch = DataBinderUtils.getInteger(binder, "SnippetQueryBatchSize", 100);
/*     */ 
/* 233 */     int curBatchCount = 0;
/* 234 */     FieldInfo fi = new FieldInfo();
/* 235 */     drset.getFieldInfo("dID", fi);
/* 236 */     IdcStringBuilder builder = null;
/*     */ 
/* 239 */     String activeIndex = binder.get("ActiveIndex");
/* 240 */     activeIndex = "FT_" + activeIndex.toUpperCase();
/* 241 */     binder.putLocal("ftIndexName", activeIndex);
/*     */ 
/* 243 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 245 */       String id = drset.getStringValue(fi.m_index);
/* 246 */       if (builder == null)
/*     */       {
/* 248 */         builder = new IdcStringBuilder();
/*     */       }
/*     */       else
/*     */       {
/* 252 */         builder.append(',');
/*     */       }
/* 254 */       builder.append(id);
/* 255 */       ++curBatchCount;
/* 256 */       if (curBatchCount < snippetBatch)
/*     */         continue;
/* 258 */       binder.putLocal("dIDs", builder.toString());
/* 259 */       builder = null;
/* 260 */       prepareSnippet(curBatchCount, drset.getCurrentRow(), fids, drset, binder);
/* 261 */       curBatchCount = 0;
/*     */     }
/*     */ 
/* 265 */     if ((builder == null) || (builder.length() <= 0))
/*     */       return;
/* 267 */     binder.putLocal("dIDs", builder.toString());
/* 268 */     prepareSnippet(curBatchCount, drset.getCurrentRow() - 1, fids, drset, binder);
/*     */   }
/*     */ 
/*     */   protected void prepareSnippet(int curBatchCount, int curIndex, FieldInfo fi, DataResultSet drset, DataBinder binder)
/*     */   {
/*     */     try
/*     */     {
/* 277 */       CallableResults result = this.m_workspace.executeCallable("CtextSnippet", binder);
/* 278 */       ResultSet rset = (ResultSet)result.getObject("result");
/*     */ 
/* 280 */       int iterateIndex = 0;
/* 281 */       while (rset.isRowPresent())
/*     */       {
/* 283 */         String snippet = rset.getStringValue(0);
/* 284 */         drset.setCurrentRow(curIndex - curBatchCount + iterateIndex + 1);
/* 285 */         drset.setCurrentValue(fi.m_index, snippet);
/* 286 */         ++iterateIndex;
/* 287 */         rset.next();
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 292 */       Report.trace("search", "Unable to populate snippet:", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getResult()
/*     */   {
/* 299 */     if (this.m_result == null)
/*     */     {
/* 301 */       this.m_result = getString(this.m_resultBinder);
/*     */     }
/* 303 */     return this.m_result;
/*     */   }
/*     */ 
/*     */   public DataBinder getResultAsBinder()
/*     */   {
/* 309 */     return this.m_resultBinder;
/*     */   }
/*     */ 
/*     */   public String getString(DataBinder binder)
/*     */   {
/* 314 */     String result = null;
/*     */     try
/*     */     {
/* 317 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/* 318 */       binder.send(sw);
/* 319 */       result = sw.toStringRelease();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 324 */       Report.trace("search", null, e);
/*     */     }
/* 326 */     return result;
/*     */   }
/*     */ 
/*     */   public String retrieveHighlightInfo(DataBinder binder, int hlType, String hlBegin, String hlEnd)
/*     */   {
/* 334 */     String type = this.m_workspace.getProperty("DatabaseType");
/* 335 */     if (!type.equalsIgnoreCase("oracle"))
/*     */     {
/* 337 */       return super.retrieveHighlightInfo(binder, hlType, hlBegin, hlEnd);
/*     */     }
/*     */     try
/*     */     {
/* 341 */       String textQuery = getTextQuery(binder);
/* 342 */       String activeIndex = binder.get("ActiveIndex");
/* 343 */       activeIndex = "FT_" + activeIndex.toUpperCase();
/* 344 */       binder.putLocal("textQuery", textQuery);
/* 345 */       binder.putLocal("tagSet", this.m_tagSet);
/* 346 */       binder.putLocal("ftIndexName", activeIndex);
/* 347 */       binder.putLocal("hlBegin", this.m_startTag);
/* 348 */       binder.putLocal("hlEnd", this.m_endTag);
/* 349 */       binder.putLocal("prevTag", this.m_prevTag);
/* 350 */       binder.putLocal("nextTag", this.m_nextTag);
/* 351 */       CallableResults rset = this.m_workspace.executeCallable("CtextHighlight", binder);
/* 352 */       setResult(rset);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 356 */       return CommonSearchConnection.createErrorMsg(e, "csSearchRetrieveHighlightInfoError");
/*     */     }
/*     */ 
/* 359 */     return null;
/*     */   }
/*     */ 
/*     */   public void setResult(CallableResults rset) throws DataException, IOException
/*     */   {
/* 364 */     Reader reader = rset.getReader("highlightClob");
/* 365 */     BufferedReader breader = new BufferedReader(reader);
/* 366 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 367 */     char[] cbuf = new char[65526];
/* 368 */     int len = cbuf.length;
/* 369 */     int total = 0;
/* 370 */     while ((len = breader.read(cbuf, 0, len)) != -1)
/*     */     {
/* 372 */       buf.append(cbuf, 0, len);
/* 373 */       total += len;
/* 374 */       if (total > this.m_maxDocSize) {
/*     */         break;
/*     */       }
/*     */ 
/* 378 */       if (total + len > this.m_maxDocSize)
/*     */       {
/* 380 */         len = this.m_maxDocSize - total;
/*     */       }
/*     */ 
/* 384 */       len = cbuf.length;
/*     */     }
/*     */ 
/* 388 */     this.m_result = buf.toString();
/*     */   }
/*     */ 
/*     */   public String getTextQuery(DataBinder binder) throws DataException
/*     */   {
/* 393 */     String queryText = binder.get("QueryText");
/*     */ 
/* 395 */     int index = 0;
/* 396 */     StringBuffer buf = new StringBuffer();
/* 397 */     while ((index = queryText.indexOf("CONTAINS(", index)) >= 0)
/*     */     {
/* 399 */       int start = queryText.indexOf('\'', index);
/* 400 */       if (start == -1) {
/*     */         break;
/*     */       }
/*     */ 
/* 404 */       int end = queryText.indexOf("> 0", start);
/* 405 */       end = queryText.lastIndexOf('\'', end);
/* 406 */       String tmp = queryText.substring(start + 1, end);
/* 407 */       if (buf.length() != 0)
/*     */       {
/* 409 */         buf.append(" and ");
/*     */       }
/*     */ 
/* 412 */       buf.append(tmp);
/*     */ 
/* 414 */       index = end;
/*     */     }
/* 416 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 421 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86052 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.DatabaseFullTextSearchImplementor
 * JD-Core Version:    0.5.4
 */