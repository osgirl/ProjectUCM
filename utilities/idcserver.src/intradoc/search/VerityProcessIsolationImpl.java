/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.VersionInfo;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class VerityProcessIsolationImpl extends VeritySearchImplementor
/*     */ {
/*     */   protected boolean m_isExeCall;
/*     */   protected DataBinder m_params;
/*     */   protected String m_result;
/*     */   protected String m_encoding;
/*     */   protected ExecuteProcessCommand m_executeProcess;
/*     */   protected boolean m_keepProcessAlive;
/*     */   public boolean m_isDebug;
/*     */   public boolean m_isTrace;
/*     */   public String m_pid;
/*  48 */   public static long m_count = 0L;
/*     */   protected String m_appName;
/*     */   protected String m_appSig;
/*     */   protected String m_options;
/*     */   protected String m_exePath;
/*     */   protected boolean m_useVdk4;
/*     */   protected DataResultSet m_collRSet;
/*  59 */   protected static byte[] m_lock = new byte[1];
/*     */ 
/*     */   public VerityProcessIsolationImpl()
/*     */   {
/*  35 */     this.m_isExeCall = false;
/*  36 */     this.m_params = null;
/*  37 */     this.m_result = null;
/*  38 */     this.m_encoding = null;
/*  39 */     this.m_executeProcess = null;
/*     */ 
/*  43 */     this.m_keepProcessAlive = false;
/*  44 */     this.m_isDebug = false;
/*  45 */     this.m_isTrace = false;
/*  46 */     this.m_pid = null;
/*     */ 
/*  50 */     this.m_appName = null;
/*  51 */     this.m_appSig = null;
/*  52 */     this.m_options = null;
/*     */ 
/*  54 */     this.m_exePath = null;
/*  55 */     this.m_useVdk4 = false;
/*     */   }
/*     */ 
/*     */   public void init(CommonSearchConnection searchConn)
/*     */   {
/*  64 */     this.m_data = searchConn.m_connectionData;
/*  65 */     this.m_sc = searchConn;
/*     */ 
/*  67 */     this.m_isDebug = StringUtils.convertToBool(this.m_data.getEnvironmentValue("SearchExeDebug"), false);
/*  68 */     this.m_isTrace = StringUtils.convertToBool(this.m_data.getEnvironmentValue("SearchExeTrace"), false);
/*  69 */     this.m_keepProcessAlive = (!StringUtils.convertToBool(this.m_data.getEnvironmentValue("SearchExeKillPerCall"), false));
/*     */ 
/*  71 */     this.m_encoding = SearchLoader.m_encoding;
/*  72 */     this.m_useVdk4 = useVdk4();
/*     */ 
/*  74 */     String instDir = this.m_data.getAllowMissing("VerityInstallDir");
/*  75 */     if (instDir == null)
/*     */     {
/*  77 */       String indexerPath = this.m_data.getActiveAllowMissing("IndexerPath");
/*     */ 
/*  79 */       if ((indexerPath != null) && (indexerPath.length() > 0))
/*     */       {
/*  81 */         instDir = SearchLoader.computeVerityInstallDir(indexerPath);
/*     */       }
/*     */ 
/*  84 */       if ((instDir == null) || (instDir.length() == 0))
/*     */       {
/*  86 */         CommonSearchConfig csc = SearchIndexerUtils.retrieveSearchConfig(this.m_context);
/*  87 */         String commonDir = csc.getEngineValue("VerityCommonDir");
/*     */ 
/*  89 */         DataBinder db = new DataBinder(SharedObjects.getSecureEnvironment());
/*  90 */         PageMerger pm = new PageMerger(db, this.m_context);
/*     */         try
/*     */         {
/*  94 */           DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/*  95 */           int colIndex = ResultSetUtils.getIndexMustExist(searchEngines, "seId");
/*     */ 
/*  97 */           if (searchEngines.findRow(colIndex, csc.getCurrentEngineName()) != null)
/*     */           {
/*  99 */             db.mergeResultSetRowIntoLocalData(searchEngines);
/* 100 */             String componentName = db.getLocal("idcComponentName");
/* 101 */             if ((componentName != null) && (componentName.length() > 0))
/*     */             {
/* 103 */               pm.evaluateScript("<$ComponentDir = getComponentInfo(idcComponentName,\"ComponentDir\")$>");
/*     */             }
/*     */           }
/*     */ 
/* 107 */           commonDir = pm.evaluateScript(commonDir);
/*     */ 
/* 109 */           instDir = commonDir;
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/* 113 */           Report.trace("searchquery", null, e);
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 117 */           Report.trace("searchquery", null, e);
/*     */         }
/*     */         finally
/*     */         {
/* 121 */           pm.releaseAllTemporary();
/*     */         }
/*     */       }
/*     */ 
/* 125 */       if ((instDir == null) || (instDir.length() == 0))
/*     */       {
/* 128 */         if (this.m_useVdk4)
/*     */         {
/* 130 */           instDir = this.m_sc.m_sharedDir + "search/vdk4/common";
/*     */         }
/*     */         else
/*     */         {
/* 134 */           instDir = this.m_sc.m_sharedDir + "search/vdk/common";
/*     */         }
/*     */       }
/*     */     }
/* 138 */     this.m_installDir = instDir;
/*     */ 
/* 140 */     this.m_exePath = determineExePath();
/*     */ 
/* 142 */     this.m_keepProcessAlive = (!DataBinderUtils.getBoolean(this.m_data, "CloseSearchConnections", false));
/* 143 */     if (this.m_keepProcessAlive)
/*     */     {
/* 145 */       this.m_keepProcessAlive = DataBinderUtils.getBoolean(this.m_data, "IsPersistSearchConnection", true);
/*     */     }
/*     */ 
/* 148 */     if (!this.m_keepProcessAlive)
/*     */       return;
/* 150 */     String pid = null;
/*     */ 
/* 154 */     synchronized (m_lock)
/*     */     {
/* 156 */       boolean pidUnavailable = SharedObjects.getEnvValueAsBoolean("IsProcessIdUnavailable", false);
/* 157 */       if (!pidUnavailable)
/*     */       {
/* 159 */         boolean useNativeApi = SharedObjects.getEnvValueAsBoolean("UseNativeForGetPid", false);
/* 160 */         if (!useNativeApi)
/*     */         {
/* 162 */           SharedObjects.putEnvironmentValue("ProcessPidForChildren", "childdetermines");
/*     */         }
/* 164 */         pid = SharedObjects.getEnvironmentValue("ProcessPidForChildren");
/* 165 */         if (pid == null)
/*     */         {
/*     */           try
/*     */           {
/* 169 */             Class nativeUtilsClass = Class.forName("intradoc.common.NativeOsUtils");
/* 170 */             Object nativeUtilsObject = nativeUtilsClass.newInstance();
/* 171 */             pid = (String)ClassHelperUtils.executeMethod(nativeUtilsObject, "getPid", null, null);
/*     */           }
/*     */           catch (Throwable e)
/*     */           {
/* 175 */             Report.trace("search", "Native OS Utils with method to get process ID not present. Search api executable will not remain resident.", e);
/*     */           }
/*     */ 
/* 180 */           if (pid == null)
/*     */           {
/* 182 */             SharedObjects.putEnvironmentValue("IsProcessIdUnavailable", "1");
/*     */           }
/*     */           else
/*     */           {
/* 186 */             SharedObjects.putEnvironmentValue("ProcessPidForChildren", pid);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 210 */     if (pid != null)
/*     */     {
/* 212 */       this.m_pid = pid;
/*     */     }
/*     */     else
/*     */     {
/* 216 */       this.m_keepProcessAlive = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setIsExeCall(boolean isExeCall)
/*     */   {
/* 224 */     this.m_isExeCall = isExeCall;
/*     */   }
/*     */ 
/*     */   public void setEncoding(String encoding)
/*     */   {
/* 229 */     this.m_encoding = encoding;
/*     */   }
/*     */ 
/*     */   public void open(String appName, String appSig, String instDir, String options)
/*     */     throws ServiceException, DataException
/*     */   {
/* 251 */     this.m_appName = appName;
/* 252 */     this.m_appSig = appSig;
/* 253 */     this.m_options = options;
/*     */   }
/*     */ 
/*     */   public boolean initCollection(Vector collections)
/*     */   {
/* 263 */     DataResultSet drset = new DataResultSet(new String[] { "cName", "cPath", "cIsAdded", "cErrMsg" });
/* 264 */     String dir = this.m_data.getEnvironmentValue("IntradocDir");
/* 265 */     for (int i = 0; i < collections.size(); ++i)
/*     */     {
/* 267 */       String path = (String)collections.elementAt(i);
/* 268 */       String absPath = FileUtils.getAbsolutePath(dir, path);
/*     */ 
/* 272 */       int result = FileUtils.checkFile(path, false, false);
/* 273 */       if ((result != 0) && (result != -24))
/*     */       {
/* 275 */         String msg = FileUtils.getErrorMsg(path, false, result);
/* 276 */         Report.trace("search", "unable to find collection at '" + path + "': " + msg, null);
/*     */ 
/* 279 */         break;
/*     */       }
/*     */ 
/* 282 */       Vector v = drset.createEmptyRow();
/* 283 */       v.setElementAt(path, 0);
/* 284 */       v.setElementAt(absPath, 1);
/* 285 */       drset.addRow(v);
/*     */     }
/*     */ 
/* 290 */     this.m_collRSet = drset;
/* 291 */     return true;
/*     */   }
/*     */ 
/*     */   public String doQuery(DataBinder binder)
/*     */   {
/* 299 */     binder.putLocal("function", "query");
/*     */ 
/* 301 */     return buildAndExecuteRequest(binder);
/*     */   }
/*     */ 
/*     */   public String retrieveHighlightInfo(DataBinder binder, int hlType, String hlBegin, String hlEnd)
/*     */   {
/* 309 */     binder.putLocal("function", "highlight");
/* 310 */     binder.putLocal("hlType", "" + hlType);
/* 311 */     binder.putLocal("hlBegin", hlBegin);
/* 312 */     binder.putLocal("hlEnd", hlEnd);
/*     */ 
/* 314 */     return buildAndExecuteRequest(binder);
/*     */   }
/*     */ 
/*     */   public String viewDoc(DataBinder binder, int viewType)
/*     */   {
/* 321 */     binder.putLocal("function", "view");
/* 322 */     binder.putLocal("viewType", "" + viewType);
/*     */ 
/* 324 */     return buildAndExecuteRequest(binder);
/*     */   }
/*     */ 
/*     */   public String getResult()
/*     */   {
/* 330 */     return this.m_result;
/*     */   }
/*     */ 
/*     */   public String retrieveDocInfo(String docKey, String fields, int numFields)
/*     */   {
/* 337 */     DataBinder binder = new DataBinder();
/* 338 */     binder.putLocal("function", "docinfo");
/* 339 */     binder.putLocal("dDocName", docKey);
/* 340 */     binder.putLocal("FieldNames", fields);
/* 341 */     binder.putLocal("NumFieldNames", "" + numFields);
/*     */ 
/* 343 */     return buildAndExecuteRequest(binder);
/*     */   }
/*     */ 
/*     */   public void closeSession()
/*     */   {
/* 350 */     if (this.m_executeProcess == null)
/*     */       return;
/* 352 */     if (!this.m_keepProcessAlive)
/*     */     {
/* 354 */       Report.trace("search", "Execute search API process not null on reset. This is an error.", null);
/*     */     }
/* 356 */     else if (this.m_sc.m_isBadConnection)
/*     */     {
/* 358 */       askForProcessShutdown(this.m_executeProcess);
/*     */     }
/* 360 */     this.m_executeProcess.clearExe();
/* 361 */     this.m_executeProcess = null;
/*     */   }
/*     */ 
/*     */   public void askForProcessShutdown(ExecuteProcessCommand executeProcess)
/*     */   {
/* 367 */     if (!executeProcess.isProcessActive())
/*     */     {
/*     */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 375 */       DataBinder binder = new DataBinder();
/* 376 */       binder.putLocal("function", "close");
/* 377 */       Vector results = new IdcVector();
/* 378 */       IdcCharArrayWriter writer = new IdcCharArrayWriter();
/* 379 */       binder.send(writer);
/* 380 */       String command = writer.toStringRelease();
/* 381 */       executeProcess.executeCommand(command, 100000, results, true);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 385 */       Report.trace("search", "askForShutdown " + t.getClass().toString() + " " + t.getMessage(), t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String determineExePath()
/*     */   {
/* 395 */     String searchExe = this.m_data.getEnvironmentValue("SearchApiProcessPath");
/* 396 */     String platform = EnvUtils.getOSName();
/*     */ 
/* 398 */     if (searchExe == null)
/*     */     {
/* 400 */       CommonSearchConfig csc = SearchIndexerUtils.retrieveSearchConfig(this.m_context);
/* 401 */       searchExe = csc.getEngineValue("SearchIsolationExecutablePath");
/*     */ 
/* 403 */       if ((searchExe != null) && (searchExe.length() > 0))
/*     */       {
/* 405 */         DataBinder db = new DataBinder(SharedObjects.getSecureEnvironment());
/* 406 */         PageMerger pm = new PageMerger(db, this.m_context);
/*     */         try
/*     */         {
/* 411 */           DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/* 412 */           int colIndex = ResultSetUtils.getIndexMustExist(searchEngines, "seId");
/*     */ 
/* 414 */           if (searchEngines.findRow(colIndex, csc.getCurrentEngineName()) != null)
/*     */           {
/* 416 */             db.mergeResultSetRowIntoLocalData(searchEngines);
/* 417 */             String componentName = db.getLocal("idcComponentName");
/* 418 */             if ((componentName != null) && (componentName.length() > 0))
/*     */             {
/* 420 */               pm.evaluateScript("<$ComponentDir = getComponentInfo(idcComponentName,\"ComponentDir\")$>");
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 425 */           db.putLocal("PLATFORM", "<$PLATFORM$>");
/*     */ 
/* 428 */           searchExe = pm.evaluateScript(searchExe);
/*     */ 
/* 431 */           int index = searchExe.indexOf("<$PLATFORM$>");
/* 432 */           if (index >= 0)
/*     */           {
/* 434 */             String basePath = searchExe.substring(0, index);
/* 435 */             String abstractPath = searchExe.substring(index + "<$PLATFORM$>".length() + 1);
/*     */ 
/* 437 */             Map options = new HashMap();
/* 438 */             options.put("base_directory", basePath);
/* 439 */             options.put("type_executable", "type_executable");
/* 440 */             Report.trace("searchquery", "looking for searchExe with base " + basePath + " and abstract path " + abstractPath, null);
/*     */ 
/* 442 */             Map map = EnvUtils.normalizeOSPath(abstractPath, options);
/*     */ 
/* 444 */             if (StringUtils.convertToBool((String)map.get("isSuccess"), false))
/*     */             {
/* 446 */               searchExe = (String)map.get("path");
/*     */             }
/*     */             else
/*     */             {
/* 450 */               String msg = LocaleUtils.encodeMessage("csErrorCalculatingSearchApiProcessPath", null, searchExe);
/*     */ 
/* 452 */               throw new ServiceException(msg);
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 459 */           throw new AssertionError(e);
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 464 */           throw new AssertionError(e);
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/* 469 */           throw new AssertionError(e);
/*     */         }
/*     */         finally
/*     */         {
/* 473 */           pm.releaseAllTemporary();
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 480 */     if (searchExe == null)
/*     */     {
/* 482 */       if (this.m_useVdk4)
/*     */       {
/* 484 */         searchExe = "SearchVdk4ApiProcess";
/*     */       }
/*     */       else
/*     */       {
/* 488 */         searchExe = "SearchApiProcess";
/*     */       }
/*     */     }
/* 491 */     String sharedDir = DirectoryLocator.getSharedDirectory();
/* 492 */     String osLibDir = sharedDir + "/os/" + platform + "/lib/";
/* 493 */     searchExe = FileUtils.getAbsolutePath(osLibDir, searchExe);
/*     */ 
/* 495 */     Report.trace("search", "using executable '" + searchExe + "' for searching", null);
/* 496 */     return searchExe;
/*     */   }
/*     */ 
/*     */   public String buildAndExecuteRequest(DataBinder srcBinder)
/*     */   {
/* 502 */     this.m_result = null;
/*     */ 
/* 505 */     if (this.m_collRSet.isEmpty())
/*     */     {
/* 507 */       this.m_result = "";
/* 508 */       return null;
/*     */     }
/* 510 */     DataBinder binder = new DataBinder();
/* 511 */     binder.setLocalData(srcBinder.getLocalData());
/*     */ 
/* 515 */     if (this.m_isDebug)
/*     */     {
/* 517 */       binder.putLocal("isDebug", "1");
/*     */     }
/* 519 */     if (this.m_isTrace)
/*     */     {
/* 521 */       SystemUtils.reportDeprecatedUsage("SearchExeTrace is deprecated.  Enable the search tracing section and verbose tracing.");
/*     */ 
/* 524 */       binder.putLocal("isTrace", "1");
/* 525 */       SystemUtils.addAsDefaultTrace("search");
/* 526 */       SystemUtils.m_verbose = true;
/*     */     }
/*     */ 
/* 529 */     binder.putLocal("appName", this.m_appName);
/* 530 */     binder.putLocal("appSig", this.m_appSig);
/* 531 */     binder.putLocal("instDir", this.m_installDir);
/* 532 */     binder.putLocal("options", this.m_options);
/*     */ 
/* 534 */     binder.addResultSet("Collections", this.m_collRSet);
/*     */ 
/* 536 */     encodeVdkKey(binder);
/*     */ 
/* 538 */     String exePath = this.m_exePath;
/*     */ 
/* 540 */     Vector results = new IdcVector();
/* 541 */     boolean isExecuted = false;
/* 542 */     String r1 = null;
/*     */     try
/*     */     {
/* 545 */       executeRequest(exePath, binder, results);
/* 546 */       isExecuted = true;
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 550 */       DataBinder errResults = new DataBinder();
/* 551 */       errResults.putLocal("StatusCode", "" + e.m_errorCode);
/* 552 */       errResults.putLocal("StatusMessageKey", e.getMessage());
/* 553 */       errResults.putLocal("StatusMessage", e.getMessage());
/* 554 */       IdcCharArrayWriter s = new IdcCharArrayWriter();
/*     */       try
/*     */       {
/* 557 */         errResults.send(s);
/*     */       }
/*     */       catch (IOException ignore)
/*     */       {
/* 561 */         ignore.printStackTrace();
/*     */       }
/* 563 */       r1 = s.toStringRelease();
/*     */     }
/* 565 */     if (isExecuted)
/*     */     {
/* 567 */       int totLen = 0;
/* 568 */       int numResults = results.size();
/* 569 */       for (int i = 0; i < numResults; ++i)
/*     */       {
/* 571 */         String r = (String)results.elementAt(i);
/* 572 */         totLen += r.length();
/*     */       }
/* 574 */       Report.trace("search", "Executed query (" + numResults + ", " + totLen + ").", null);
/* 575 */       int nresults = results.size();
/* 576 */       if (nresults == 0)
/*     */       {
/* 578 */         return "";
/*     */       }
/*     */ 
/* 581 */       r1 = (String)results.elementAt(0);
/*     */ 
/* 583 */       if (nresults > 1)
/*     */       {
/* 585 */         this.m_result = ((String)results.elementAt(1));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 591 */     return r1;
/*     */   }
/*     */ 
/*     */   public void executeRequest(String exePath, DataBinder params, Vector results)
/*     */     throws ServiceException
/*     */   {
/* 597 */     String query = params.getLocal("QueryText");
/* 598 */     String function = params.getLocal("function");
/* 599 */     Report.trace("search", "Executing function '" + function + "' using search executable.", null);
/* 600 */     if (query != null)
/*     */     {
/* 602 */       Report.trace("search", query, null);
/*     */     }
/* 604 */     Vector commandLineParams = new IdcVector();
/* 605 */     if ((this.m_keepProcessAlive) && (this.m_pid != null))
/*     */     {
/* 607 */       commandLineParams.addElement("/parentpid");
/* 608 */       commandLineParams.addElement(this.m_pid);
/*     */     }
/* 610 */     if ((this.m_executeProcess == null) || (!this.m_executeProcess.isRunning()) || (!this.m_executeProcess.isProcessActive()))
/*     */     {
/* 612 */       if (this.m_executeProcess != null)
/*     */       {
/* 614 */         this.m_executeProcess.clearExe();
/* 615 */         Report.trace("search", "Search api process was unexpectedly terminated, regenerating the process.", null);
/*     */       }
/* 617 */       this.m_executeProcess = new ExecuteProcessCommand();
/* 618 */       String encoding = this.m_encoding;
/* 619 */       if (encoding == null)
/*     */       {
/* 621 */         encoding = System.getProperty("file.encoding");
/*     */       }
/* 623 */       this.m_executeProcess.setEncoding(encoding);
/* 624 */       this.m_executeProcess.startProcess(exePath, commandLineParams);
/*     */     }
/* 626 */     IdcCharArrayWriter writer = new IdcCharArrayWriter();
/*     */     try
/*     */     {
/* 629 */       params.send(writer);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 634 */       throw new ServiceException(e);
/*     */     }
/* 636 */     String command = writer.toStringRelease();
/* 637 */     int searchExeTimeout = NumberUtils.parseInteger(this.m_data.getEnvironmentValue("SearchExeTimeout"), 40) * 1000;
/*     */     try
/*     */     {
/* 641 */       this.m_executeProcess.executeCommand(command, searchExeTimeout, results, !this.m_keepProcessAlive);
/*     */     }
/*     */     finally
/*     */     {
/* 645 */       if ((!this.m_keepProcessAlive) || (!this.m_executeProcess.isRunning()))
/*     */       {
/* 647 */         this.m_executeProcess.clearExe();
/* 648 */         this.m_executeProcess = null;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean useVdk4()
/*     */   {
/* 655 */     boolean usesVdk4 = false;
/* 656 */     String prodVersion = VersionInfo.getProductVersion();
/* 657 */     if ((prodVersion != null) && (prodVersion.length() > 3))
/*     */     {
/* 659 */       char ch1 = prodVersion.charAt(0);
/* 660 */       char ch2 = prodVersion.charAt(1);
/* 661 */       char ch3 = prodVersion.charAt(2);
/* 662 */       usesVdk4 = !StringUtils.convertToBool(this.m_data.getEnvironmentValue("UseVdkLegacySearch"), false);
/* 663 */       if ((ch2 == '.') && ((
/* 665 */         (ch1 == '4') || (ch2 == '5') || ((ch1 == '6') && (((ch3 == '0') || (ch3 == '1')))))))
/*     */       {
/* 667 */         usesVdk4 = false;
/*     */       }
/*     */     }
/*     */ 
/* 671 */     return usesVdk4;
/*     */   }
/*     */ 
/*     */   protected void encodeVdkKey(DataBinder binder)
/*     */   {
/* 676 */     String key = binder.getLocal("dDocName");
/* 677 */     if ((key == null) || (!SearchLoader.m_encodeVdkKeyForSearch))
/*     */       return;
/* 679 */     IdcStringBuilder buff = new IdcStringBuilder();
/* 680 */     buff.append("z");
/*     */     try
/*     */     {
/* 683 */       StringUtils.appendAsHex(buff, key);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 687 */       Report.trace("search", null, ignore);
/*     */     }
/* 689 */     key = buff.toString();
/* 690 */     binder.putLocal("docKey", key);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 698 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98038 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.VerityProcessIsolationImpl
 * JD-Core Version:    0.5.4
 */