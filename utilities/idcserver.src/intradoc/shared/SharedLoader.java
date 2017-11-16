/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.Browser;
/*     */ import intradoc.common.BufferPool;
/*     */ import intradoc.common.DefaultReportHandler;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcStringBuilderFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportDelegator;
/*     */ import intradoc.common.ReportHandler;
/*     */ import intradoc.common.ReportTracingCallback;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.TraceImplementor;
/*     */ import intradoc.common.TracerReportUtils;
/*     */ import intradoc.common.VersionInfo;
/*     */ import intradoc.conversion.CryptoPasswordUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.util.IdcMessageUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import intradoc.util.SimpleTracingCallback;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SharedLoader
/*     */ {
/*     */   public static final int F_NO_WARNINGS = 0;
/*     */   public static final int F_ISSUE_WARNINGS = 1;
/*  71 */   public static String[] m_tracingColumns = { "itsNode", "itsSections", "itsFlags" };
/*     */   public static final int m_defaultTraceColumnCount = 3;
/*  81 */   public static boolean m_allowGlobalUtilPointers = false;
/*     */ 
/*     */   public static void cacheOptList(ResultSet rset, String optField, String optIndexKey)
/*     */   {
/*  87 */     cacheOptListEx(rset, optField, optIndexKey, false);
/*     */   }
/*     */ 
/*     */   public static void cacheOptListEx(ResultSet rset, String optField, String optIndexKey, boolean isUnique)
/*     */   {
/*  93 */     FieldInfo info = new FieldInfo();
/*  94 */     if (!rset.getFieldInfo(optField, info))
/*     */     {
/*  96 */       return;
/*     */     }
/*     */ 
/*  99 */     Vector v = new IdcVector();
/* 100 */     Hashtable options = new Hashtable();
/* 101 */     for (; rset.isRowPresent(); rset.next())
/*     */     {
/* 103 */       String value = rset.getStringValue(info.m_index);
/* 104 */       if ((isUnique) && (options.get(value) != null)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 108 */       v.addElement(value);
/* 109 */       if (!isUnique)
/*     */         continue;
/* 111 */       options.put(value, value);
/*     */     }
/*     */ 
/* 115 */     SharedObjects.putOptList(optIndexKey, v);
/*     */   }
/*     */ 
/*     */   public static String getDocGifSubDirectory()
/*     */   {
/* 122 */     if (Browser.hasAppletContext())
/*     */     {
/* 124 */       String relativeWebRoot = LegacyDocumentPathBuilder.getRelativeWebRoot();
/* 125 */       return relativeWebRoot + "images/docgifs/";
/*     */     }
/* 127 */     return "docgifs/";
/*     */   }
/*     */ 
/*     */   public static void addEnvVariableListToTable(DataBinder binder, String[] list)
/*     */     throws DataException
/*     */   {
/* 135 */     if ((list == null) || (list.length == 0))
/*     */     {
/* 137 */       return;
/*     */     }
/*     */ 
/* 140 */     for (int i = 0; i < list.length; ++i)
/*     */     {
/* 143 */       String varName = list[i];
/* 144 */       if (varName.length() <= 0)
/*     */         continue;
/* 146 */       String varValue = SharedObjects.getEnvironmentValue(varName);
/* 147 */       addEnvVariableToTable(binder, varName, varValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addEnvVariableToTable(DataBinder binder, String varName, String varValue)
/*     */     throws DataException
/*     */   {
/* 156 */     DataResultSet drset = (DataResultSet)binder.getResultSet("ServerEnvHolder");
/* 157 */     if (drset == null)
/*     */     {
/* 159 */       drset = new DataResultSet(new String[] { "name", "value" });
/* 160 */       binder.addResultSet("ServerEnvHolder", drset);
/*     */     }
/*     */ 
/* 166 */     Vector v = drset.findRow(0, varName);
/*     */ 
/* 168 */     if (v == null)
/*     */     {
/* 170 */       v = drset.createEmptyRow();
/* 171 */       drset.addRow(v);
/*     */     }
/*     */ 
/* 174 */     v.setElementAt(varName, 0);
/* 175 */     if (varValue == null)
/*     */     {
/* 177 */       varValue = "";
/*     */     }
/* 179 */     v.setElementAt(varValue, 1);
/*     */   }
/*     */ 
/*     */   public static void loadEnvVariableListFromTable(DataBinder binder)
/*     */   {
/* 187 */     DataResultSet drset = (DataResultSet)binder.getResultSet("ServerEnvHolder");
/* 188 */     if (drset == null)
/*     */     {
/* 190 */       return;
/*     */     }
/*     */ 
/* 193 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 195 */       String varName = drset.getStringValue(0);
/* 196 */       String varValue = drset.getStringValue(1);
/* 197 */       SharedObjects.putEnvironmentValue(varName, varValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void loadInitialConfig()
/*     */   {
/* 210 */     m_allowGlobalUtilPointers = (SharedObjects.getEnvValueAsBoolean("AllowGlobalUtilPointers", true)) && (!EnvUtils.isHostedInAppServer());
/*     */ 
/* 213 */     int extraTraces = TracerReportUtils.m_traceSectionTypes.length;
/* 214 */     String[] tracingColumns = new String[extraTraces + 3];
/* 215 */     System.arraycopy(m_tracingColumns, 0, tracingColumns, 0, 3);
/* 216 */     for (int i = 0; i < extraTraces; ++i)
/*     */     {
/* 218 */       tracingColumns[(i + 3)] = ("its" + TracerReportUtils.m_traceSectionTypes[i]);
/*     */     }
/* 220 */     m_tracingColumns = tracingColumns;
/*     */ 
/* 222 */     configureTracing(null);
/* 223 */     Properties appProps = SystemUtils.getAppProperties();
/* 224 */     Properties safeProps = SharedObjects.getSafeEnvironment();
/* 225 */     Properties envProps = SharedObjects.getSecureEnvironment();
/* 226 */     appProps.putAll(safeProps);
/* 227 */     appProps.putAll(envProps);
/* 228 */     SystemUtils.calculateIsDevelopmentBuild();
/* 229 */     if (SystemUtils.m_isDevelopmentEnvironment)
/*     */     {
/* 231 */       Report.trace("system", "Server is configured to run in development mode", null);
/*     */     }
/*     */ 
/* 237 */     if (SharedObjects.getEnvValueAsBoolean("NoNewLockFiles", false))
/*     */     {
/* 239 */       intradoc.common.FileUtilsLockDirectory.m_noNewFiles = true;
/*     */     }
/*     */ 
/* 242 */     configureResultSetJoin();
/* 243 */     configureBufferPoolUsage();
/* 244 */     configureIdcMessageUtils();
/* 245 */     configureEncodingChecking();
/* 246 */     configureLocalization();
/* 247 */     configureDeprecatedSettings();
/* 248 */     initFeatures();
/* 249 */     configureCrypto();
/*     */   }
/*     */ 
/*     */   public static void configureTracing(DataBinder binder)
/*     */   {
/* 254 */     SystemUtils.addAsDefaultTrace("system");
/* 255 */     SystemUtils.addAsDefaultTrace("idocscript");
/* 256 */     Vector allTraces = StringUtils.parseArrayEx("all", ',', '^', true);
/* 257 */     for (int i = 0; i < TracerReportUtils.m_traceSectionTypes.length; ++i)
/*     */     {
/* 259 */       SystemUtils.setActiveTraces(allTraces, TracerReportUtils.m_traceSectionTypes[i]);
/*     */     }
/* 261 */     boolean consoleTraceDefault = Browser.hasAppletContext();
/* 262 */     if (SystemUtils.m_isDevelopmentEnvironment)
/*     */     {
/* 264 */       SystemUtils.addAsDefaultTrace("deprecation");
/* 265 */       if (null == SharedObjects.getEnvironmentValue("IsPageDebug"))
/*     */       {
/* 267 */         SharedObjects.putEnvironmentValue("IsPageDebug", "1");
/*     */       }
/* 269 */       consoleTraceDefault = true;
/*     */     }
/* 274 */     else if (null == SharedObjects.getEnvironmentValue("DisableErrorPageStackTrace"))
/*     */     {
/* 276 */       SharedObjects.putEnvironmentValue("DisableErrorPageStackTrace", "1");
/*     */     }
/*     */ 
/* 279 */     TracerReportUtils.m_idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 280 */     TracerReportUtils.m_environment = SharedObjects.getSafeEnvironment();
/*     */ 
/* 283 */     consoleTraceDefault = SharedObjects.getEnvValueAsBoolean("TraceToConsole", consoleTraceDefault);
/*     */ 
/* 285 */     TracerReportUtils.setDefaultTraceToConsole(consoleTraceDefault);
/*     */ 
/* 287 */     DataResultSet tmpTracing = null;
/* 288 */     String counterString = null;
/* 289 */     if (binder != null)
/*     */     {
/* 291 */       tmpTracing = (DataResultSet)binder.getResultSet("Tracing");
/* 292 */       counterString = binder.getLocal("tracingCounter");
/*     */     }
/* 294 */     DataBinder currentTracing = (DataBinder)SharedObjects.getObject("", "TracingConfiguration");
/*     */ 
/* 296 */     if ((currentTracing != null) && (currentTracing.getLocal("forceUpdate") == null) && (counterString != null) && (counterString.length() > 0))
/*     */     {
/* 300 */       String oldCounterString = currentTracing.getLocal("tracingCounter");
/* 301 */       if (oldCounterString != null)
/*     */       {
/* 303 */         int newCounter = NumberUtils.parseInteger(counterString, 0);
/* 304 */         int oldCounter = NumberUtils.parseInteger(oldCounterString, -1);
/* 305 */         if (newCounter == oldCounter)
/*     */         {
/* 307 */           if (SystemUtils.m_verbose)
/*     */           {
/* 309 */             Report.debug(null, "skipping tracing config, version " + newCounter, null);
/*     */           }
/*     */ 
/* 312 */           return;
/*     */         }
/*     */       }
/*     */     }
/* 316 */     if (binder != null)
/*     */     {
/* 318 */       SharedObjects.putObject("", "TracingConfiguration", binder);
/*     */     }
/*     */ 
/* 321 */     String nodeName = SharedObjects.getEnvironmentValue("ClusterNodeName");
/* 322 */     if (nodeName == null)
/*     */     {
/* 324 */       nodeName = "";
/*     */     }
/*     */ 
/* 328 */     DataResultSet tracing = new DataResultSet(m_tracingColumns);
/*     */     try
/*     */     {
/* 331 */       if (tmpTracing != null)
/*     */       {
/* 333 */         tracing.merge(null, tmpTracing, false);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 338 */       Report.trace(null, "unable to configure tracing", e);
/* 339 */       return;
/*     */     }
/* 341 */     Vector row = null;
/* 342 */     String sectionList = SharedObjects.getEnvironmentValue("TraceSectionsList");
/* 343 */     if ((sectionList != null) && (TracerReportUtils.m_tracingHasRuntimeEditingUserInterface))
/*     */     {
/* 345 */       SystemUtils.reportDeprecatedUsage("using TraceSectionsList from config/config.cfg rather than tracing configuration in data/config/tracing.hda");
/*     */     }
/*     */ 
/* 349 */     String[] traceList = new String[TracerReportUtils.m_traceSectionTypes.length];
/* 350 */     List flags = new ArrayList();
/* 351 */     row = tracing.findRow(0, nodeName);
/* 352 */     if (row == null)
/*     */     {
/* 354 */       row = tracing.findRow(0, "");
/*     */     }
/* 356 */     if (row != null)
/*     */     {
/* 358 */       if ((sectionList == null) && (row.size() > 1))
/*     */       {
/* 360 */         sectionList = (String)row.elementAt(1);
/*     */       }
/*     */ 
/* 363 */       if (row.size() > 2) {
/* 364 */         StringUtils.appendListFromSequenceSimple(flags, (String)row.elementAt(2));
/*     */       }
/*     */ 
/* 368 */       for (int i = 0; i < TracerReportUtils.m_traceSectionTypes.length; ++i)
/*     */       {
/* 370 */         if (row.size() > i + 3) {
/* 371 */           traceList[i] = ((String)row.elementAt(i + 3));
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 377 */     boolean verbose = TracerReportUtils.getBooleanFlag(flags, "traceIsVerbose", false);
/* 378 */     boolean withoutTimestamp = TracerReportUtils.getBooleanFlag(flags, "traceWithoutTimestamp", false);
/*     */ 
/* 381 */     if ((sectionList != null) || (verbose) || (withoutTimestamp))
/*     */     {
/* 383 */       Report.trace("system", "Configuring tracing verbose: " + verbose + "; sections: " + sectionList, null);
/*     */     }
/*     */ 
/* 386 */     configureTracingEx(verbose, sectionList, withoutTimestamp);
/* 387 */     for (int i = 0; i < TracerReportUtils.m_traceSectionTypes.length; ++i)
/*     */     {
/* 389 */       Vector traces = StringUtils.parseArrayEx(traceList[i], ',', '^', true);
/* 390 */       if ((traceList[i] == null) || (traceList[i].length() < 0))
/*     */         continue;
/* 392 */       SystemUtils.setActiveTraces(traces, TracerReportUtils.m_traceSectionTypes[i]);
/*     */     }
/*     */ 
/* 395 */     DataResultSet nodeSectionSet = new DataResultSet(new String[] { m_tracingColumns[1] });
/*     */ 
/* 401 */     Vector nodeSectionList = SystemUtils.getActiveTraces();
/* 402 */     for (int i = 0; i < nodeSectionList.size(); ++i)
/*     */     {
/* 404 */       Vector v = new IdcVector();
/* 405 */       v.addElement(nodeSectionList.elementAt(i));
/* 406 */       nodeSectionSet.addRow(v);
/*     */     }
/* 408 */     SharedObjects.putTable("NodeTracingSections", nodeSectionSet);
/*     */ 
/* 410 */     DefaultReportHandler handler = getDefaultReportHandler();
/* 411 */     if (handler != null)
/*     */     {
/* 413 */       handler.m_manditoryTraceThreshold = TracerReportUtils.getIntegerFlag(flags, "ReportManditoryTraceThreshold", handler.m_manditoryTraceThreshold);
/*     */ 
/* 417 */       handler.m_autoSectionThreshold = TracerReportUtils.getIntegerFlag(flags, "ReportAutoSectionThreshold", handler.m_autoSectionThreshold);
/*     */ 
/* 421 */       handler.m_autoSectionName = TracerReportUtils.getStringFlag(flags, "ReportAutoSectionName", handler.m_autoSectionName);
/*     */ 
/* 424 */       String dataDir = SharedObjects.getEnvironmentValue("DataDir");
/* 425 */       if ((dataDir == null) || (dataDir.length() == 0))
/*     */       {
/* 427 */         dataDir = SharedObjects.getEnvironmentValue("IntradocDir") + "/data";
/* 428 */         dataDir = FileUtils.directorySlashes(dataDir);
/*     */       }
/*     */ 
/* 431 */       String logBaseDir = SharedObjects.getEnvironmentValue("BaseLogDir");
/* 432 */       String traceDir = SharedObjects.getEnvironmentValue("TraceDirectory");
/* 433 */       if (traceDir == null)
/*     */       {
/* 435 */         traceDir = logBaseDir;
/* 436 */         traceDir = traceDir + "trace";
/* 437 */         SharedObjects.putEnvironmentValue("TraceDirectory", traceDir);
/*     */       }
/*     */ 
/* 440 */       String eventDir = SharedObjects.getEnvironmentValue("EventDirectory");
/* 441 */       if (eventDir == null)
/*     */       {
/* 443 */         eventDir = logBaseDir;
/* 444 */         eventDir = eventDir + "trace/event";
/* 445 */         SharedObjects.putEnvironmentValue("EventDirectory", eventDir);
/*     */       }
/*     */ 
/* 451 */       if (!SharedObjects.getEnvValueAsBoolean("DisableEventFileTrigger", false))
/*     */       {
/* 453 */         String trigger = null;
/* 454 */         String alwaysAddThreadDump = null;
/* 455 */         if (binder != null)
/*     */         {
/* 457 */           trigger = binder.getAllowMissing("EventFileTrigger");
/* 458 */           alwaysAddThreadDump = binder.getAllowMissing("EventFileTriggerAddThreadDump");
/*     */         }
/* 460 */         if ((trigger == null) && (SharedObjects.getEnvironmentValue("EventFileTrigger") == null))
/*     */         {
/* 463 */           trigger = "NoEventTrap";
/*     */         }
/*     */ 
/* 466 */         if (trigger != null)
/*     */         {
/* 468 */           SharedObjects.putEnvironmentValue("EventFileTrigger", trigger);
/*     */         }
/* 470 */         if (alwaysAddThreadDump != null)
/*     */         {
/* 472 */           SharedObjects.putEnvironmentValue("EventFileTriggerAddThreadDump", alwaysAddThreadDump);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 481 */       if (SharedObjects.getEnvironmentValue("EventFileIdleLimit") == null)
/*     */       {
/* 483 */         SharedObjects.putEnvironmentValue("EventFileIdleLimit", "5000");
/*     */       }
/*     */ 
/* 489 */       if (SharedObjects.getEnvironmentValue("EventFileCountLimit") == null)
/*     */       {
/* 491 */         SharedObjects.putEnvironmentValue("EventFileCountLimit", "100");
/*     */       }
/*     */ 
/* 494 */       Properties env = SharedObjects.getSafeEnvironment();
/* 495 */       IdcProperties props = new IdcProperties(env);
/* 496 */       if (binder != null)
/*     */       {
/* 498 */         Properties localData = binder.getLocalData();
/* 499 */         props.setMap(localData);
/*     */       }
/* 501 */       for (int i = 0; i < handler.m_tracers.length; ++i)
/*     */       {
/* 503 */         TraceImplementor impl = handler.m_tracers[i];
/* 504 */         if (impl == null)
/*     */           continue;
/* 506 */         impl.configureTrace(flags, props);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 511 */     configureIdcMessageUtils();
/*     */   }
/*     */ 
/*     */   public static boolean allowGlobalUtilPointers()
/*     */   {
/* 516 */     return m_allowGlobalUtilPointers;
/*     */   }
/*     */ 
/*     */   public static void configureResultSetJoin()
/*     */   {
/* 521 */     intradoc.data.ResultSetJoin.m_tables = SharedObjects.m_tables;
/*     */   }
/*     */ 
/*     */   public static DefaultReportHandler getDefaultReportHandler()
/*     */   {
/* 526 */     ReportHandler handler = Report.getDelegator().getDefaultReportHandler();
/* 527 */     if (handler instanceof DefaultReportHandler)
/*     */     {
/* 529 */       return (DefaultReportHandler)handler;
/*     */     }
/* 531 */     return null;
/*     */   }
/*     */ 
/*     */   public static void configureTracingEx(boolean isVerboseTrace, String list, boolean isWithoutTimestamp)
/*     */   {
/* 537 */     Vector traceList = StringUtils.parseArrayEx(list, ',', '^', true);
/* 538 */     Report.m_verbose = isVerboseTrace;
/* 539 */     SystemUtils.m_verbose = isVerboseTrace;
/* 540 */     if ((list != null) && (list.length() > 0))
/*     */     {
/* 542 */       SystemUtils.setActiveTraces(traceList);
/*     */     }
/* 544 */     DefaultReportHandler handler = getDefaultReportHandler();
/* 545 */     if (handler != null)
/*     */     {
/* 547 */       handler.setTraceParameter("traceWithoutTimestamp", "" + isWithoutTimestamp);
/*     */     }
/*     */ 
/* 550 */     if (!SystemUtils.isActiveTrace("localization"))
/*     */       return;
/* 552 */     LocaleResources.m_insufficentArgumentsError = "(InsufficentArgument)";
/* 553 */     LocaleResources.m_invalidFlagError = "(InvalidFlag)";
/*     */   }
/*     */ 
/*     */   public static void configureComponentTracing()
/*     */   {
/* 562 */     DataResultSet drset = SharedObjects.getTable("IdcTracingSections");
/* 563 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 565 */       Properties props = drset.getCurrentRowProps();
/* 566 */       String key = props.getProperty("itsSection");
/* 567 */       String flag = props.getProperty("itsDefaultEnabled");
/* 568 */       if (!StringUtils.convertToBool(flag, false))
/*     */         continue;
/* 570 */       SystemUtils.addAsDefaultTrace(key);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void initIdcName(Properties props, int flags)
/*     */   {
/* 583 */     boolean issueWarnings = (flags & 0x1) != 0;
/* 584 */     String idcName = props.getProperty("IDC_Name");
/* 585 */     if ((idcName == null) || (idcName.length() == 0))
/*     */     {
/* 587 */       idcName = "";
/* 588 */       if (issueWarnings)
/*     */       {
/* 590 */         Report.warning(null, null, "csIdcNameMissing", new Object[0]);
/* 591 */         Report.trace(null, null, LocaleResources.localizeMessage("!csIdcNameMissing", null), new Object[0]);
/*     */       }
/*     */     }
/* 594 */     props.put("IDC_Name", idcName);
/*     */ 
/* 596 */     String instanceMenuLabel = props.getProperty("InstanceMenuLabel");
/* 597 */     if (instanceMenuLabel == null)
/*     */     {
/* 599 */       instanceMenuLabel = idcName;
/* 600 */       props.put("InstanceMenuLabel", instanceMenuLabel);
/*     */     }
/*     */ 
/* 603 */     String instanceDescription = props.getProperty("InstanceDescription");
/* 604 */     if (instanceDescription != null)
/*     */       return;
/* 606 */     instanceDescription = "Instance " + instanceMenuLabel;
/* 607 */     props.put("InstanceDescription", instanceDescription);
/*     */   }
/*     */ 
/*     */   public static void configureLocalization()
/*     */   {
/* 613 */     if (!SharedObjects.getEnvValueAsBoolean("DisableLocalization", false))
/*     */       return;
/* 615 */     LocaleResources.m_disableLocalization = true;
/*     */   }
/*     */ 
/*     */   public static void configureDeprecatedSettings()
/*     */   {
/* 621 */     String[][] depInfo = { { "MaxSavedSearchResults", "SavedQueries:mru" } };
/*     */ 
/* 626 */     Properties publicEnv = SharedObjects.getSafeEnvironment();
/* 627 */     Properties privateEnv = SharedObjects.getSecureEnvironment();
/* 628 */     for (String[] depRule : depInfo)
/*     */     {
/* 630 */       String val = publicEnv.getProperty(depRule[0]);
/* 631 */       if (val == null)
/*     */       {
/* 633 */         val = privateEnv.getProperty(depRule[0]);
/*     */       }
/* 635 */       if (val == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 639 */       Report.deprecatedUsage("replacing deprecated environment variable " + depRule[0] + " with new variable " + depRule[1]);
/*     */ 
/* 641 */       SharedObjects.putEnvironmentValue(depRule[1], val);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void initFeatures()
/*     */   {
/* 647 */     Features.init();
/*     */   }
/*     */ 
/*     */   public static void configureCrypto()
/*     */   {
/* 652 */     CryptoPasswordUtils.setEnvironment(SharedObjects.getSecureEnvironment());
/*     */   }
/*     */ 
/*     */   public static void configureBufferPoolUsage()
/*     */   {
/* 657 */     BufferPool.m_largeBufferSize = SharedObjects.getTypedEnvironmentInt("LargeBufferSize", BufferPool.m_largeBufferSize, 5, 0);
/*     */ 
/* 660 */     BufferPool.m_mediumBufferSize = SharedObjects.getTypedEnvironmentInt("MediumBufferSize", BufferPool.m_mediumBufferSize, 5, 0);
/*     */ 
/* 663 */     BufferPool.m_smallBufferSize = SharedObjects.getTypedEnvironmentInt("SmallBufferSize", BufferPool.m_smallBufferSize, 5, 0);
/*     */ 
/* 674 */     boolean stackTracesForLeak = SystemUtils.isActiveTrace("bufferleak");
/* 675 */     if (SystemUtils.m_isDevelopmentEnvironment)
/*     */     {
/* 677 */       stackTracesForLeak = true;
/*     */     }
/* 679 */     BufferPool pool = BufferPool.getBufferPool();
/* 680 */     Map options = pool.getOptions();
/* 681 */     options.put("stackTracesForLeak", Boolean.valueOf(stackTracesForLeak));
/* 682 */     pool.setOptions(options);
/*     */ 
/* 684 */     String version = VersionInfo.getProductVersionInfo();
/* 685 */     if ((SystemUtils.m_isDevelopmentEnvironment) || (!SystemUtils.isOlderVersion(version, "7.3.1")))
/*     */     {
/* 688 */       BufferPool.m_failOnOverallocation = true;
/* 689 */       BufferPool.m_defaultTracingCallback = new ReportTracingCallback();
/*     */     }
/* 691 */     if (SharedObjects.getEnvValueAsBoolean("UseBufferPools", true))
/*     */     {
/* 693 */       intradoc.common.ParseOutput.m_defaultBufferPool = BufferPool.getBufferPool("ParseOutput");
/*     */ 
/* 695 */       intradoc.common.IdcStringBuilder.m_defaultBufferPool = BufferPool.getBufferPool("IdcStringBuilder");
/*     */ 
/* 697 */       intradoc.common.IdcCharArrayWriter.m_defaultBufferPool = BufferPool.getBufferPool("IdcCharArrayWriter");
/*     */ 
/* 699 */       FileUtils.m_defaultBufferPool = BufferPool.getBufferPool("FileUtils");
/*     */     }
/*     */     else
/*     */     {
/* 703 */       Report.trace(null, "running without buffer pools", null);
/*     */     }
/*     */ 
/* 706 */     String useMapped = SharedObjects.getEnvironmentValue("UseMappedIO");
/* 707 */     FileUtils.m_useMappedIO = SharedObjects.getEnvValueAsBoolean("UseMappedIO", true);
/*     */ 
/* 711 */     boolean useMappedDiffDefault = (useMapped == null) ? false : (!EnvUtils.getOSFamily().equals("windows")) ? true : FileUtils.m_useMappedIO;
/*     */ 
/* 714 */     FileUtils.m_useMappedDiff = SharedObjects.getEnvValueAsBoolean("UseMappedDiff", useMappedDiffDefault);
/*     */   }
/*     */ 
/*     */   public static void configureIdcMessageUtils()
/*     */   {
/* 722 */     boolean localizationAssertionsDefault = false;
/* 723 */     if ((SystemUtils.isActiveTrace("localization")) && (SystemUtils.m_verbose))
/*     */     {
/* 725 */       localizationAssertionsDefault = true;
/*     */     }
/* 727 */     if (SystemUtils.m_isDevelopmentEnvironment)
/*     */     {
/* 729 */       localizationAssertionsDefault = true;
/*     */     }
/* 731 */     if (SharedObjects.getEnvValueAsBoolean("EnableLocalizationAssertions", localizationAssertionsDefault))
/*     */     {
/* 734 */       IdcMessageUtils.m_flags |= 1;
/*     */     }
/* 736 */     if (allowGlobalUtilPointers())
/*     */     {
/* 738 */       IdcMessageUtils.init(LocaleUtils.m_utcOdbcDateFormat, new IdcStringBuilderFactory(), new ReportTracingCallback("localization"));
/*     */     }
/*     */     else
/*     */     {
/* 745 */       SimpleTracingCallback callback = new SimpleTracingCallback();
/* 746 */       if (!SystemUtils.m_isDevelopmentEnvironment)
/*     */       {
/* 750 */         callback.m_output = null;
/*     */       }
/*     */ 
/* 755 */       IdcMessageUtils.init(null, null, callback);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void configureEncodingChecking()
/*     */   {
/* 761 */     boolean failReplacementCharacters = SharedObjects.getEnvValueAsBoolean("FailOnReplacementCharacter", false);
/*     */ 
/* 763 */     SystemUtils.setFailOnReplacementCharacterDefault(failReplacementCharacters);
/*     */ 
/* 765 */     boolean failOnEncodeFailure = SharedObjects.getEnvValueAsBoolean("FailOnEncodingFailure", false);
/*     */ 
/* 767 */     SystemUtils.setFailOnEncodingFailure(failOnEncodeFailure);
/*     */ 
/* 769 */     boolean writeUnicodeSignature = SharedObjects.getEnvValueAsBoolean("WriteUnicodeSignature", false);
/*     */ 
/* 771 */     SystemUtils.setWriteUnicodeSignature(writeUnicodeSignature);
/*     */ 
/* 773 */     boolean writeUTF8Signature = SharedObjects.getEnvValueAsBoolean("WriteUTF8Signature", true);
/*     */ 
/* 775 */     SystemUtils.setWriteUTF8Signature(writeUTF8Signature);
/*     */   }
/*     */ 
/*     */   public static void postInitialize()
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 789 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105081 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SharedLoader
 * JD-Core Version:    0.5.4
 */