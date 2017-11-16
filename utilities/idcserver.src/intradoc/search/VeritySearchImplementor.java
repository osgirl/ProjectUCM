/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.LoggingUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class VeritySearchImplementor extends CommonSearchAdaptor
/*     */ {
/*     */   protected CommonSearchConnection m_sc;
/*     */   protected VeritySearch m_connection;
/*     */   protected Vector m_collections;
/*     */   protected Vector m_attachedColls;
/*     */   protected String m_installDir;
/*     */   protected DataBinder m_data;
/*     */   protected String[] m_appKeys;
/*     */ 
/*     */   public VeritySearchImplementor()
/*     */   {
/*  35 */     this.m_collections = null;
/*  36 */     this.m_attachedColls = null;
/*     */ 
/*  40 */     this.m_appKeys = new String[] { "VdkAppName", "VdkAppSignature" };
/*     */   }
/*     */ 
/*     */   public void init(CommonSearchConnection sc)
/*     */   {
/*  45 */     super.init(sc);
/*  46 */     this.m_sc = sc;
/*  47 */     this.m_data = sc.m_connectionData;
/*  48 */     String libName = null;
/*     */ 
/*  50 */     CommonSearchConfig csc = SearchIndexerUtils.retrieveSearchConfig(this.m_context);
/*  51 */     libName = csc.getEngineValue("SearchLibraryName");
/*     */ 
/*  53 */     SearchConfig.setLibName(libName);
/*  54 */     this.m_connection = new VeritySearch();
/*     */ 
/*  56 */     String instDir = this.m_data.getAllowMissing("VerityInstallDir");
/*  57 */     if (instDir == null)
/*     */     {
/*  59 */       String indexerPath = this.m_data.getActiveAllowMissing("IndexerPath");
/*  60 */       if ((indexerPath != null) && (indexerPath.length() > 0))
/*     */       {
/*  62 */         instDir = SearchLoader.computeVerityInstallDir(indexerPath);
/*     */       }
/*     */ 
/*  65 */       if ((instDir == null) || (instDir.length() == 0))
/*     */       {
/*  67 */         String commonDir = csc.getEngineValue("VerityCommonDir");
/*     */ 
/*  69 */         DataBinder db = new DataBinder(SharedObjects.getSecureEnvironment());
/*  70 */         PageMerger pm = new PageMerger(db, this.m_context);
/*     */ 
/*  72 */         DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/*  73 */         int colIndex = 0;
/*     */         try
/*     */         {
/*  76 */           ResultSetUtils.getIndexMustExist(searchEngines, "seId");
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/*  80 */           Report.trace("searchquery", null, e);
/*     */         }
/*     */ 
/*  83 */         if (searchEngines.findRow(colIndex, csc.getCurrentEngineName()) != null)
/*     */         {
/*  85 */           db.mergeResultSetRowIntoLocalData(searchEngines);
/*  86 */           String componentName = db.getLocal("idcComponentName");
/*  87 */           if ((componentName != null) && (componentName.length() > 0))
/*     */           {
/*     */             try
/*     */             {
/*  91 */               pm.evaluateScript("<$ComponentDir = getComponentInfo(idcComponentName,\"ComponentDir\")$>");
/*     */             }
/*     */             catch (Exception e)
/*     */             {
/*  95 */               Report.trace("indexer", null, e);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/* 102 */           commonDir = pm.evaluateScript(commonDir);
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/* 106 */           Report.trace("searchquery", null, e);
/*     */         }
/* 108 */         pm.releaseAllTemporary();
/*     */ 
/* 110 */         instDir = commonDir;
/*     */       }
/*     */     }
/*     */ 
/* 114 */     instDir = EnvUtils.convertPathToOSConventions(instDir);
/* 115 */     this.m_installDir = instDir;
/*     */   }
/*     */ 
/*     */   protected void open(String appName, String appSig, String instDir, String options)
/*     */     throws ServiceException, DataException
/*     */   {
/* 122 */     long openStartTime = 0L;
/* 123 */     boolean traceOpen = (SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("searchquery"));
/* 124 */     if (traceOpen)
/*     */     {
/* 126 */       openStartTime = System.currentTimeMillis();
/*     */     }
/* 128 */     String err = this.m_connection.openSession(appName, appSig, instDir, options);
/*     */ 
/* 130 */     if (traceOpen)
/*     */     {
/* 132 */       long curTime = System.currentTimeMillis();
/* 133 */       long diff = curTime - openStartTime;
/* 134 */       String msg = "Opened session in " + diff + "(msecs) instDir=" + instDir + ",options={" + options + "} (" + appName + "--" + appSig + ")";
/*     */ 
/* 136 */       Report.trace("searchquery", msg, null);
/*     */     }
/*     */ 
/* 139 */     if (err == null)
/*     */       return;
/* 141 */     String msg = LocaleUtils.encodeMessage("csVerityAPISessionError", null, options);
/*     */ 
/* 143 */     parseErrorCode(err, msg);
/*     */   }
/*     */ 
/*     */   public boolean initCollection(Vector collections)
/*     */   {
/* 152 */     boolean traceOpen = (SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("searchquery"));
/*     */ 
/* 154 */     Vector oldAttachedColls = this.m_attachedColls;
/* 155 */     this.m_collections = collections;
/* 156 */     this.m_attachedColls = new IdcVector();
/*     */ 
/* 158 */     for (int i = 0; i < collections.size(); ++i)
/*     */     {
/* 160 */       String dir = this.m_data.getEnvironmentValue("IntradocDir");
/* 161 */       String path = (String)collections.elementAt(i);
/* 162 */       String absPath = FileUtils.getAbsolutePath(dir, path);
/*     */       try
/*     */       {
/* 167 */         boolean alreadyAttached = false;
/* 168 */         if (oldAttachedColls != null)
/*     */         {
/* 170 */           for (int j = 0; j < oldAttachedColls.size(); ++j)
/*     */           {
/* 172 */             String oldPath = (String)oldAttachedColls.elementAt(j);
/* 173 */             if (!oldPath.equals(absPath))
/*     */               continue;
/* 175 */             alreadyAttached = true;
/* 176 */             oldAttachedColls.removeElementAt(j);
/* 177 */             break;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 182 */         if (!alreadyAttached)
/*     */         {
/* 186 */           int result = FileUtils.checkFile(path, false, false);
/* 187 */           if ((result != 0) && (result != -24))
/*     */           {
/* 189 */             String msg = FileUtils.getErrorMsg(path, false, result);
/* 190 */             msg = LocaleUtils.encodeMessage("csSearchCollectionPathError", msg);
/* 191 */             throw new ServiceException(msg);
/*     */           }
/*     */         }
/*     */ 
/* 195 */         String err = null;
/* 196 */         if (!alreadyAttached)
/*     */         {
/* 198 */           long openStartTime = 0L;
/* 199 */           if (traceOpen)
/*     */           {
/* 201 */             openStartTime = System.currentTimeMillis();
/*     */           }
/* 203 */           err = this.m_connection.attachCollection(absPath);
/* 204 */           if (traceOpen)
/*     */           {
/* 206 */             long curTime = System.currentTimeMillis();
/* 207 */             long diff = curTime - openStartTime;
/* 208 */             String msg = "Attached collection in " + diff + "(msecs) absPath=" + absPath;
/* 209 */             Report.trace("searchquery", msg, null);
/*     */           }
/*     */         }
/*     */ 
/* 213 */         if (err == null)
/*     */         {
/* 215 */           this.m_attachedColls.addElement(absPath);
/*     */         }
/*     */         else
/*     */         {
/* 219 */           String msg = LocaleUtils.encodeMessage("csSearchCollectionUnableToAttach", null, absPath);
/*     */ 
/* 221 */           parseErrorCode(err, msg);
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 226 */         String msg = e.getMessage();
/* 227 */         Report.error(null, msg, e);
/* 228 */         Report.trace(null, LocaleResources.localizeMessage(msg, null), e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 233 */     if (oldAttachedColls != null)
/*     */     {
/* 235 */       for (int i = 0; i < oldAttachedColls.size(); ++i)
/*     */       {
/* 237 */         String oldPath = (String)oldAttachedColls.elementAt(i);
/* 238 */         detachCollection(oldPath);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 243 */     return false;
/*     */   }
/*     */ 
/*     */   public String doQuery(DataBinder binder)
/*     */   {
/* 249 */     String query = binder.getLocal("QueryText");
/* 250 */     String fields = binder.getLocal("FieldNames");
/* 251 */     String sort = binder.getLocal("SortSpec");
/*     */     int numFields;
/*     */     int resultCount;
/*     */     int startRow;
/*     */     try
/*     */     {
/* 257 */       numFields = Integer.parseInt(binder.getLocal("NumFieldNames"));
/* 258 */       resultCount = Integer.parseInt(binder.getLocal("ResultCount"));
/* 259 */       startRow = Integer.parseInt(binder.getLocal("StartRow"));
/*     */     }
/*     */     catch (NumberFormatException e)
/*     */     {
/* 263 */       return createErrorMsg(e);
/*     */     }
/* 265 */     return this.m_connection.doQuery(query, fields, numFields, resultCount, startRow, sort);
/*     */   }
/*     */ 
/*     */   public String retrieveHighlightInfo(DataBinder binder, int hlType, String hlBegin, String hlEnd)
/*     */   {
/* 272 */     String docKey = binder.getLocal("dDocName");
/* 273 */     String query = binder.getLocal("QueryText");
/* 274 */     String sort = binder.getLocal("SortSpec");
/* 275 */     docKey = encodeVdkKey(docKey);
/* 276 */     return this.m_connection.retrieveHighlightInfo(docKey, query, sort, hlType, hlBegin, hlEnd);
/*     */   }
/*     */ 
/*     */   public String viewDoc(DataBinder binder, int viewType)
/*     */   {
/* 282 */     String docKey = binder.getLocal("dDocName");
/* 283 */     String query = binder.getLocal("QueryText");
/* 284 */     String sort = binder.getLocal("SortSpec");
/* 285 */     docKey = encodeVdkKey(docKey);
/* 286 */     return this.m_connection.viewDoc(docKey, query, sort, viewType);
/*     */   }
/*     */ 
/*     */   public String getResult()
/*     */   {
/* 292 */     return this.m_connection.getResult();
/*     */   }
/*     */ 
/*     */   public String retrieveDocInfo(String docKey, String fields, int numFields)
/*     */   {
/* 298 */     docKey = encodeVdkKey(docKey);
/* 299 */     return this.m_connection.retrieveDocInfo(docKey, fields, numFields);
/*     */   }
/*     */ 
/*     */   protected String encodeVdkKey(String docKey)
/*     */   {
/* 304 */     String key = docKey;
/* 305 */     if (SearchLoader.m_encodeVdkKeyForSearch)
/*     */     {
/* 307 */       IdcStringBuilder buff = new IdcStringBuilder();
/* 308 */       buff.append("z");
/*     */       try
/*     */       {
/* 311 */         StringUtils.appendAsHex(buff, docKey.toLowerCase());
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 315 */         Report.trace("search", null, ignore);
/*     */       }
/* 317 */       key = buff.toString();
/*     */     }
/*     */ 
/* 320 */     return key;
/*     */   }
/*     */ 
/*     */   public void closeSession()
/*     */   {
/* 327 */     detachCollections();
/* 328 */     this.m_connection.closeSession();
/*     */   }
/*     */ 
/*     */   public boolean prepareUse(ExecutionContext ctxt)
/*     */   {
/* 335 */     super.prepareUse(ctxt);
/*     */ 
/* 338 */     StringBuffer options = new StringBuffer();
/* 339 */     String[] sessionOpt = { "CharMap", "MaxDocSize", "TopicSet", "MemoFieldSize" };
/*     */ 
/* 342 */     String locale = this.m_data.getEnvironmentValue("Locale");
/* 343 */     if ((locale == null) || (locale.length() == 0))
/*     */     {
/* 345 */       locale = SearchLoader.m_locale;
/*     */     }
/*     */ 
/* 348 */     if ((locale != null) && (locale.length() > 0))
/*     */     {
/* 350 */       options.append("Locale=" + locale + ",");
/*     */     }
/*     */ 
/* 354 */     String encoding = SearchLoader.m_encoding;
/* 355 */     if ((encoding != null) && (encoding.length() > 0))
/*     */     {
/* 357 */       options.append("VerityEncoding=" + encoding + ",");
/*     */     }
/*     */ 
/* 360 */     for (int i = 0; i < sessionOpt.length; ++i)
/*     */     {
/* 362 */       String val = this.m_sc.m_connectionData.getEnvironmentValue(sessionOpt[i]);
/* 363 */       if (val == null)
/*     */         continue;
/* 365 */       options.append(sessionOpt[i] + "=" + val + ",");
/*     */     }
/*     */ 
/* 371 */     String val = "100000";
/* 372 */     int mr = NumberUtils.parseInteger(this.m_sc.m_connectionData.getEnvironmentValue("MaxHitCount"), 100000);
/*     */ 
/* 374 */     if (mr > 0)
/*     */     {
/* 376 */       val = String.valueOf(mr);
/*     */     }
/* 378 */     options.append("MaxResults=" + val + ",");
/*     */ 
/* 381 */     options.append("DateInputFormat=YMD,");
/*     */ 
/* 384 */     options.append("DateOutputFormat=${yyyy}-${mm}-${dd} ${hh24}:${mi}:${ss}");
/*     */     try
/*     */     {
/* 387 */       CommonSearchConfig csc = SearchIndexerUtils.retrieveSearchConfig(this.m_context);
/*     */ 
/* 389 */       String appName = csc.getEngineValue("VdkAppName");
/* 390 */       String appSig = csc.getEngineValue("VdkAppSignature");
/*     */ 
/* 392 */       open(appName, appSig, this.m_installDir, options.toString());
/* 393 */       return true;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 397 */       LoggingUtils.error(e, null, null);
/* 398 */       Report.trace("search", null, e);
/*     */     }
/* 400 */     return false;
/*     */   }
/*     */ 
/*     */   protected void detachCollections()
/*     */   {
/* 405 */     if (this.m_attachedColls == null)
/*     */     {
/* 407 */       return;
/*     */     }
/* 409 */     Vector curColls = this.m_attachedColls;
/* 410 */     this.m_attachedColls = null;
/*     */ 
/* 412 */     for (int i = 0; i < curColls.size(); ++i)
/*     */     {
/* 414 */       String collPath = (String)curColls.elementAt(i);
/* 415 */       detachCollection(collPath);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void detachCollection(String collPath)
/*     */   {
/* 421 */     boolean traceOpen = (SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("searchquery"));
/* 422 */     long openStartTime = 0L;
/*     */ 
/* 424 */     if (traceOpen)
/*     */     {
/* 426 */       openStartTime = System.currentTimeMillis();
/*     */     }
/*     */ 
/* 429 */     String err = this.m_connection.detachCollection(collPath);
/* 430 */     if (traceOpen)
/*     */     {
/* 432 */       long curTime = System.currentTimeMillis();
/* 433 */       long diff = curTime - openStartTime;
/* 434 */       String msg = "Detached collection in " + diff + "(msecs) absPath=" + collPath;
/* 435 */       Report.trace("searchquery", msg, null);
/*     */     }
/*     */ 
/* 438 */     if (err == null)
/*     */       return;
/* 440 */     BufferedReader bReader = new BufferedReader(new StringReader(err));
/*     */ 
/* 442 */     DataBinder binder = new DataBinder(true);
/* 443 */     String errorMessage = null;
/* 444 */     String vdkErrorCode = null;
/*     */     try
/*     */     {
/* 448 */       binder.receive(bReader);
/* 449 */       errorMessage = binder.getLocal("StatusMessage");
/* 450 */       vdkErrorCode = binder.getLocal("VdkErrorCode");
/*     */ 
/* 452 */       String msg = LocaleUtils.encodeMessage("csSearchCollectionDetachError", errorMessage, collPath);
/*     */ 
/* 454 */       Report.error(null, msg, null);
/* 455 */       msg = LocaleUtils.encodeMessage("csSearchError", null, errorMessage, vdkErrorCode);
/*     */ 
/* 457 */       Report.trace(null, LocaleResources.localizeMessage(msg, new ExecutionContextAdaptor()), null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 463 */       String msg = LocaleUtils.encodeMessage("csSearchCollectionDetachError", e.getMessage(), collPath);
/*     */ 
/* 465 */       Report.error(null, msg, e);
/* 466 */       Report.trace(null, LocaleResources.localizeMessage(msg, new ExecutionContextAdaptor()), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void parseErrorCode(String errorStr, String msg)
/*     */     throws ServiceException, DataException
/*     */   {
/* 474 */     BufferedReader bReader = new BufferedReader(new StringReader(errorStr));
/*     */ 
/* 476 */     DataBinder binder = new DataBinder(true);
/*     */     try
/*     */     {
/* 480 */       binder.receive(bReader);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 484 */       String errMsg = LocaleUtils.encodeMessage("csSearchErrorParseError", null, errorStr);
/*     */ 
/* 486 */       msg = LocaleUtils.appendMessage(errMsg, msg);
/* 487 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 492 */     String curStatusCode = binder.getAllowMissing("StatusCode");
/* 493 */     if (curStatusCode == null)
/*     */     {
/* 495 */       String errMsg = LocaleUtils.encodeMessage("csVerityInternalError", null, errorStr);
/*     */ 
/* 497 */       msg = LocaleUtils.appendMessage(errMsg, msg);
/* 498 */       throw new DataException(msg);
/*     */     }
/* 500 */     int errorCode = Integer.parseInt(curStatusCode);
/*     */ 
/* 502 */     if (errorCode == 0)
/*     */       return;
/* 504 */     String errorMessage = binder.getLocal("StatusMessage");
/*     */ 
/* 506 */     msg = LocaleUtils.appendMessage(errorMessage, msg);
/* 507 */     throw new ServiceException(errorCode, msg);
/*     */   }
/*     */ 
/*     */   protected String createErrorMsg(Exception e)
/*     */   {
/* 513 */     DataBinder binder = new DataBinder();
/* 514 */     binder.putLocal("StatusCode", "-1");
/* 515 */     binder.putLocal("StatusMessageKey", e.getMessage());
/* 516 */     binder.putLocal("StatusMessage", e.getMessage());
/*     */ 
/* 518 */     IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*     */     try
/*     */     {
/* 521 */       binder.send(sw);
/* 522 */       return sw.toStringRelease();
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 527 */       Report.trace("search", null, ignore);
/* 528 */     }return "";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 534 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98038 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.VeritySearchImplementor
 * JD-Core Version:    0.5.4
 */