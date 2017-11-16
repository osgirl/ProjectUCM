/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.LockReadWriteMutex;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.io.PrintStream;
/*      */ import java.lang.reflect.InvocationTargetException;
/*      */ import java.text.DateFormat;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Random;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SystemUtils
/*      */ {
/*      */   public static final int MSG_INFO_TYPE = 0;
/*      */   public static final int MSG_WARNING_TYPE = 1;
/*      */   public static final int MSG_ERROR_TYPE = 2;
/*      */   public static final int MSG_FATAL_TYPE = 3;
/*      */   protected static String m_binDir;
/*      */   protected static String m_cfgFile;
/*   51 */   protected static String m_propertyFile = new String();
/*   52 */   protected static String m_propertyFileDes = new String();
/*   53 */   protected static Properties m_appProperties = new Properties();
/*      */   protected static boolean m_appPropertiesLoaded;
/*      */   protected static Properties m_sysPropertiesClone;
/*      */   protected static boolean m_isInitialized;
/*      */   protected static Random m_waitRandom;
/*      */   public static boolean m_isDevelopmentBuild;
/*      */   public static boolean m_isDevelopmentEnvironment;
/*      */   public static boolean m_alignDevelopmentBuildWithEnvironment;
/*      */   public static final int F_SHORT_ID = 0;
/*      */   public static final int F_FULL_REPORT_ID = 1;
/*      */   public static int m_numThreads;
/*      */   public static long m_totalThreads;
/*   90 */   protected static Boolean m_lockObj = new Boolean(true);
/*   91 */   protected static int m_threadCounter = 1;
/*      */ 
/*   96 */   public static ThreadLocal<String> m_shortThreadID = new ThreadLocal();
/*      */   public static volatile boolean m_isServerStopped;
/*  105 */   public static boolean[] m_isServerStoppedPtr = { false };
/*      */ 
/*  110 */   protected static List m_objectsToNotifyOnStop = new ArrayList();
/*      */ 
/*  116 */   public static List m_objectsToCloseOnStop = new ArrayList();
/*      */ 
/*  124 */   public static LockReadWriteMutex m_serverStopMutex = new LockReadWriteMutex("serverstop");
/*      */   public static boolean m_verbose;
/*      */   public static boolean m_reportAllDeprecatedTraceCalls;
/*      */   public static boolean m_reportedTraceDeprecation;
/*      */   public static ThreadManagerInterface m_systemClientThreadScheduler;
/*      */   public static long m_sharedServerStartupTime;
/*      */   public static OutputStream m_captureOutStream;
/*  155 */   public static PrintStream m_out = System.out;
/*      */ 
/*  160 */   public static PrintStream m_err = System.err;
/*      */ 
/*  164 */   public static String[] m_tracingFlags = { "traceIsVerbose", "traceWithoutTimestamp", "traceToStdErr", "traceDumpVerboseException", "alwaysIncludeStack", "ReportManditoryTraceThreshold", "ReportAutoSectionThreshold", "ReportAutoSectionName" };
/*      */ 
/*  173 */   public static boolean[] m_tracingFlagDefaults = { false, false, true, false, false, false, false, false };
/*      */ 
/*  189 */   public static DateFormat m_traceLogFormat = new SimpleDateFormat("MM.dd HH:mm:ss.SSS");
/*      */   public static boolean m_failOnReplacementCharacterDefault;
/*      */   public static boolean m_failOnEncodingFailure;
/*  196 */   public static boolean m_writeUTF8Signature = true;
/*      */   public static boolean m_writeUnicodeSignature;
/*  199 */   protected static String[] m_sysPropertiesToClone = { "java.version", "java.vendor", "java.vendor.url", "java.home", "java.vm.specification.version", "java.vm.specification.vendor", "java.vm.specification.name", "java.vm.version", "java.vm.vendor", "java.vm.name", "java.specification.version", "java.specification.vendor", "java.specification.name", "java.class.version", "java.class.path", "java.library.path", "java.io.tmpdir", "java.compiler", "java.ext.dirs", "os.name", "os.arch", "os.version", "file.separator", "path.separator", "line.separator", "user.name", "user.home", "user.dir" };
/*      */ 
/*      */   public static Properties getSystemPropertiesClone()
/*      */   {
/*  214 */     if (null != m_sysPropertiesClone)
/*      */     {
/*  216 */       return m_sysPropertiesClone;
/*      */     }
/*      */     try
/*      */     {
/*  220 */       m_sysPropertiesClone = (Properties)System.getProperties().clone();
/*  221 */       return m_sysPropertiesClone;
/*      */     }
/*      */     catch (SecurityException i)
/*      */     {
/*  225 */       m_sysPropertiesClone = new Properties();
/*      */ 
/*  227 */       int i = 0; if (i >= m_sysPropertiesToClone.length)
/*      */         break label85;
/*      */       try
/*      */       {
/*  231 */         String value = System.getProperty(m_sysPropertiesToClone[i]);
/*  232 */         if (null != value)
/*      */         {
/*  234 */           m_sysPropertiesClone.put(m_sysPropertiesToClone[i], value);
/*      */         }
/*      */       }
/*      */       catch (SecurityException e)
/*      */       {
/*      */       }
/*  227 */       ++i;
/*      */     }
/*      */ 
/*  242 */     label85: return m_sysPropertiesClone;
/*      */   }
/*      */ 
/*      */   public static String getClonedSystemProperty(String key)
/*      */   {
/*  247 */     Properties props = getSystemPropertiesClone();
/*  248 */     return props.getProperty(key);
/*      */   }
/*      */ 
/*      */   public static void setClonedSystemProperty(String key, String val)
/*      */   {
/*  253 */     Properties props = getSystemPropertiesClone();
/*  254 */     props.put(key, val);
/*      */   }
/*      */ 
/*      */   public static Map getReadOnlyEnvironment()
/*      */   {
/*  259 */     return (Map)AppObjectRepository.getObject("environment");
/*      */   }
/*      */ 
/*      */   public static boolean checkStartup()
/*      */   {
/*  264 */     return m_isInitialized;
/*      */   }
/*      */ 
/*      */   public static int getNextThreadCount()
/*      */   {
/*  272 */     int retVal = 0;
/*  273 */     synchronized (m_lockObj)
/*      */     {
/*  275 */       retVal = m_threadCounter++;
/*  276 */       if (m_threadCounter >= 10000000)
/*      */       {
/*  278 */         m_threadCounter = 10000;
/*      */       }
/*      */     }
/*  281 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static int alterCount(int inc)
/*      */   {
/*  289 */     synchronized (m_lockObj)
/*      */     {
/*  291 */       if (inc > 0)
/*      */       {
/*  293 */         m_numThreads += 1;
/*  294 */         m_totalThreads += 1L;
/*      */       }
/*  296 */       else if (inc < 0)
/*      */       {
/*  298 */         m_numThreads -= 1;
/*      */       }
/*  300 */       return m_numThreads;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void assignReportingThreadIdToCurrentThread(int flags)
/*      */   {
/*  310 */     int nextCount = alterCount(1);
/*  311 */     int threadCounter = getNextThreadCount();
/*  312 */     String id = "IdcServer-" + threadCounter;
/*      */ 
/*  315 */     m_shortThreadID.set(id);
/*  316 */     if (!isActiveTrace("threads"))
/*      */       return;
/*  318 */     String longThreadID = Thread.currentThread().getName();
/*  319 */     Report.trace("threads", "Assigned short thread ID to thread " + longThreadID + " with " + nextCount + " request threads active", null);
/*      */   }
/*      */ 
/*      */   public static void releaseReportingThreadIdForCurrentThread()
/*      */   {
/*  326 */     if ((Report.m_verbose) && (isActiveTrace("threads")))
/*      */     {
/*  328 */       String longThreadID = Thread.currentThread().getName();
/*  329 */       Report.debug("threads", "Short thread ID is being released for thread " + longThreadID, null);
/*      */     }
/*  331 */     alterCount(-1);
/*  332 */     m_shortThreadID.remove();
/*      */   }
/*      */ 
/*      */   public static String getCurrentReportingThreadID(int flags)
/*      */   {
/*  337 */     String reportID = (String)m_shortThreadID.get();
/*  338 */     if (reportID == null)
/*      */     {
/*  340 */       reportID = Thread.currentThread().getName();
/*      */     }
/*  344 */     else if ((flags & 0x1) != 0)
/*      */     {
/*  346 */       reportID = reportID + "(" + Thread.currentThread().getName() + ")";
/*      */     }
/*      */ 
/*  349 */     return reportID;
/*      */   }
/*      */ 
/*      */   public static int getThreadCount()
/*      */   {
/*  359 */     return m_numThreads;
/*      */   }
/*      */ 
/*      */   public static long getTotalAccumulatedThreadUseCount()
/*      */   {
/*  367 */     return m_totalThreads;
/*      */   }
/*      */ 
/*      */   public static void registerSynchronizationObjectToNotifyOnStop(Object o)
/*      */   {
/*  377 */     synchronized (m_objectsToNotifyOnStop)
/*      */     {
/*  379 */       for (Iterator i$ = m_objectsToNotifyOnStop.iterator(); i$.hasNext(); ) { Object prevO = i$.next();
/*      */ 
/*  381 */         if (prevO == o)
/*      */         {
/*  383 */           Report.trace(null, "Registering object to notify on stop more than once", new StackTrace());
/*  384 */           return;
/*      */         } }
/*      */ 
/*  387 */       m_objectsToNotifyOnStop.add(o);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void unregisterSynchronizedObjectToNotifyOnStop(Object o)
/*      */   {
/*  396 */     Report.deprecatedUsage("unregisterSynchronizedObjectToNotifyOnStop()");
/*  397 */     unregisterSynchronizationObjectToNotifyOnStop(o);
/*      */   }
/*      */ 
/*      */   public static void unregisterSynchronizationObjectToNotifyOnStop(Object o)
/*      */   {
/*  402 */     synchronized (m_objectsToNotifyOnStop)
/*      */     {
/*  404 */       m_objectsToNotifyOnStop.remove(o);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addStreamObjectToCloseOnStop(Object o)
/*      */   {
/*  410 */     m_objectsToCloseOnStop.add(o);
/*      */   }
/*      */ 
/*      */   public static void removeStreamObjectToCloseOnStop(Object o)
/*      */   {
/*  415 */     m_objectsToCloseOnStop.remove(o);
/*      */   }
/*      */ 
/*      */   public static void markServerAsStopped()
/*      */   {
/*  424 */     m_isServerStopped = true;
/*  425 */     synchronized (m_isServerStoppedPtr)
/*      */     {
/*  427 */       m_isServerStoppedPtr[0] = true;
/*  428 */       m_isServerStoppedPtr.notifyAll();
/*      */     }
/*  430 */     synchronized (m_objectsToNotifyOnStop)
/*      */     {
/*  432 */       for (Iterator i$ = m_objectsToNotifyOnStop.iterator(); i$.hasNext(); ) { Object o = i$.next();
/*      */         try
/*      */         {
/*  437 */           synchronized (o)
/*      */           {
/*  439 */             o.notifyAll();
/*  440 */             if (o instanceof Thread)
/*      */             {
/*  442 */               ((Thread)o).interrupt();
/*      */             }
/*      */           }
/*      */         }
/*      */         catch (Exception ignore)
/*      */         {
/*  448 */           Report.trace(null, null, ignore);
/*      */         } }
/*      */ 
/*  451 */       m_objectsToNotifyOnStop.clear();
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void cleanUpAfterStop()
/*      */   {
/*  460 */     for (Iterator i$ = m_objectsToCloseOnStop.iterator(); i$.hasNext(); ) { Object o = i$.next();
/*      */ 
/*  462 */       FileUtils.closeObject(o); }
/*      */ 
/*  464 */     m_objectsToCloseOnStop.clear();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String getProductVersion()
/*      */   {
/*  471 */     reportDeprecatedUsage("use VersionInfo.getProductVersion()");
/*  472 */     return VersionInfo.getProductVersion();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String getProductVersionInfo()
/*      */   {
/*  479 */     reportDeprecatedUsage("use VersionInfo.getProductVersionInfo()");
/*  480 */     return VersionInfo.getProductVersionInfo();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String getProductBuildInfo()
/*      */   {
/*  487 */     reportDeprecatedUsage("use VersionInfo.getProductBuildInfo()");
/*  488 */     return VersionInfo.getProductBuildInfo();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String getProductCopyright()
/*      */   {
/*  495 */     reportDeprecatedUsage("use VersionInfo.getProductCopyright()");
/*  496 */     return VersionInfo.getProductCopyright();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String getProductThirdParty()
/*      */   {
/*  503 */     reportDeprecatedUsage("use VersionInfo.getProductThirdParty()");
/*  504 */     return VersionInfo.getProductThirdParty();
/*      */   }
/*      */ 
/*      */   public static boolean isDevelopmentBuild()
/*      */   {
/*  512 */     return m_isDevelopmentBuild;
/*      */   }
/*      */ 
/*      */   public static boolean isDevelopmentEnvironment()
/*      */   {
/*  524 */     return m_isDevelopmentEnvironment;
/*      */   }
/*      */ 
/*      */   public static boolean calculateIsDevelopmentBuild()
/*      */   {
/*  534 */     String disableDevChecks = m_appProperties.getProperty("DisableDevChecks");
/*  535 */     if (StringUtils.convertToBool(disableDevChecks, false))
/*      */     {
/*  537 */       return false;
/*      */     }
/*  539 */     m_isDevelopmentBuild = VersionInfo.getProductBuildInfo().indexOf("dev") >= 0;
/*  540 */     String isDevEnvString = m_appProperties.getProperty("IsDevelopmentEnvironment");
/*  541 */     m_isDevelopmentEnvironment = StringUtils.convertToBool(isDevEnvString, m_isDevelopmentEnvironment);
/*      */ 
/*  543 */     if (m_alignDevelopmentBuildWithEnvironment)
/*      */     {
/*  545 */       if (EnvUtils.m_isHostedInAppServer)
/*      */       {
/*  547 */         m_isDevelopmentEnvironment = !EnvUtils.m_isAppServerInProductionMode;
/*      */       }
/*      */       else
/*      */       {
/*  551 */         m_isDevelopmentEnvironment = m_isDevelopmentBuild;
/*      */       }
/*      */     }
/*  554 */     return m_isDevelopmentBuild;
/*      */   }
/*      */ 
/*      */   public static boolean isOlderVersion(String info1, String info2)
/*      */   {
/*  568 */     int rc = compareVersions(info1, info2);
/*  569 */     return rc < 0;
/*      */   }
/*      */ 
/*      */   public static int compareVersions(String info1, String info2)
/*      */   {
/*  581 */     if ((info1 == null) || (info1.length() == 0))
/*      */     {
/*  583 */       if ((info2 == null) || (info2.length() == 0))
/*      */       {
/*  585 */         return 0;
/*      */       }
/*      */ 
/*  588 */       return -1;
/*      */     }
/*      */ 
/*  591 */     if ((info2 == null) || (info2.length() == 0))
/*      */     {
/*  593 */       return 1;
/*      */     }
/*      */ 
/*  596 */     int[] theVersion = parseVersion(info1);
/*  597 */     int[] myVersion = parseVersion(info2);
/*  598 */     for (int i = 0; i < myVersion.length; ++i)
/*      */     {
/*  600 */       if ((theVersion[i] == -1) || (myVersion[i] == -1)) return 0;
/*  601 */       if (theVersion[i] < myVersion[i]) return -1;
/*  602 */       if (theVersion[i] > myVersion[i]) return 1;
/*      */     }
/*      */ 
/*  605 */     return 0;
/*      */   }
/*      */ 
/*      */   public static int[] parseVersion(String info)
/*      */   {
/*  613 */     int[] version = { -1, -1, -1, -1 };
/*  614 */     int index = 0;
/*      */ 
/*  616 */     if (info == null)
/*      */     {
/*  618 */       return version;
/*      */     }
/*  620 */     info = info.replace('_', '.');
/*  621 */     info = info.replace('-', '.');
/*      */     int i;
/*      */     do {
/*  624 */       if (index == version.length) {
/*      */         break;
/*      */       }
/*      */ 
/*  628 */       String value = null;
/*  629 */       i = info.indexOf(".");
/*  630 */       if (i == -1)
/*      */       {
/*  632 */         value = info;
/*  633 */         info = "";
/*      */       }
/*      */       else
/*      */       {
/*  637 */         value = info.substring(0, i);
/*  638 */         info = info.substring(i + 1);
/*      */       }
/*  640 */       version[(index++)] = NumberUtils.parseInteger(value, 0);
/*      */     }
/*  642 */     while (i >= 0);
/*      */ 
/*  644 */     return version;
/*      */   }
/*      */ 
/*      */   public static void setPropertiesFile(String fileName, String fileDes)
/*      */   {
/*  653 */     m_propertyFile = fileName;
/*  654 */     m_propertyFileDes = fileDes;
/*      */   }
/*      */ 
/*      */   public static Properties getAppProperties()
/*      */   {
/*  659 */     return m_appProperties;
/*      */   }
/*      */ 
/*      */   public static String getAppProperty(String name)
/*      */     throws IOException
/*      */   {
/*  665 */     if (!m_isInitialized)
/*      */     {
/*  667 */       loadProperties();
/*      */     }
/*  669 */     return m_appProperties.getProperty(name);
/*      */   }
/*      */ 
/*      */   public static void setAppProperty(String name, String value)
/*      */   {
/*  675 */     m_appProperties.put(name, value);
/*      */   }
/*      */ 
/*      */   public static String getBinDir()
/*      */   {
/*  680 */     locateBinDir();
/*  681 */     return m_binDir;
/*      */   }
/*      */ 
/*      */   public static void setBinDir(String binDir)
/*      */   {
/*  686 */     m_binDir = binDir;
/*      */   }
/*      */ 
/*      */   public static String getCfgFileName()
/*      */   {
/*  691 */     locateBinDir();
/*  692 */     return m_cfgFile;
/*      */   }
/*      */ 
/*      */   public static String getCfgFilePath()
/*      */   {
/*  697 */     locateBinDir();
/*  698 */     return m_propertyFile;
/*      */   }
/*      */ 
/*      */   public static void setAppProperties(Properties props)
/*      */   {
/*  708 */     m_appProperties.putAll(props);
/*  709 */     m_appPropertiesLoaded = true;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String getLogFileMsgPrefix()
/*      */   {
/*  716 */     reportDeprecatedUsage("Use LoggingUtils");
/*  717 */     return LoggingUtils.getLogFileMsgPrefix();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void setLogFileMsgPrefix(String prefix)
/*      */   {
/*  724 */     reportDeprecatedUsage("Use LoggingUtils");
/*  725 */     LoggingUtils.setLogFileMsgPrefix(prefix);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static Log getLogger()
/*      */   {
/*  732 */     reportDeprecatedUsage("Use LoggingUtils");
/*  733 */     return LoggingUtils.getLogger();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void setLogger(Log logger)
/*      */   {
/*  740 */     reportDeprecatedUsage("Use LoggingUtils");
/*  741 */     LoggingUtils.setLogger(logger);
/*      */   }
/*      */ 
/*      */   protected static void locateBinDir()
/*      */   {
/*  746 */     if (m_binDir == null)
/*      */     {
/*  748 */       m_cfgFile = System.getenv("IDC_CONFIG_FILE");
/*  749 */       m_binDir = System.getenv("BIN_DIR");
/*      */ 
/*  751 */       if ((m_binDir == null) || (m_binDir.length() == 0))
/*      */       {
/*  753 */         m_binDir = System.getProperty("idc.bin.dir");
/*      */       }
/*  755 */       if ((m_binDir == null) || (m_binDir.length() == 0))
/*      */       {
/*  757 */         m_binDir = System.getProperty("user.dir");
/*      */       }
/*      */     }
/*  760 */     if (m_cfgFile == null)
/*      */     {
/*  762 */       m_cfgFile = System.getProperty("idc.config.file");
/*      */     }
/*  764 */     if (m_cfgFile == null)
/*      */     {
/*  766 */       m_cfgFile = "intradoc.cfg";
/*  767 */       m_propertyFile = null;
/*      */     }
/*      */ 
/*  770 */     m_binDir = FileUtils.fileSlashes(m_binDir);
/*      */ 
/*  772 */     if (m_propertyFile != null)
/*      */       return;
/*  774 */     m_propertyFile = FileUtils.fileSlashes(m_binDir + "/" + m_cfgFile);
/*      */   }
/*      */ 
/*      */   protected static void loadProperties()
/*      */     throws IOException
/*      */   {
/*  780 */     if (m_isInitialized == true)
/*      */     {
/*  782 */       return;
/*      */     }
/*  784 */     locateBinDir();
/*  785 */     if (!m_appPropertiesLoaded)
/*      */     {
/*  788 */       if (m_propertyFileDes.length() == 0)
/*      */       {
/*  790 */         m_propertyFileDes = "!csPropertyFileDesc";
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/*  795 */         FileUtils.loadProperties(m_appProperties, m_propertyFile);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  799 */         throw new IOException(LocaleUtils.encodeMessage("csUnableToLoadProps", e.getMessage(), m_propertyFile));
/*      */       }
/*  801 */       m_appPropertiesLoaded = true;
/*      */     }
/*      */ 
/*  805 */     String impl = m_appProperties.getProperty("TraceImplementorClass");
/*  806 */     if (impl != null)
/*      */     {
/*  808 */       TracerReportUtils.updateDefaultTracer(impl);
/*      */     }
/*      */ 
/*  812 */     Properties env = (Properties)AppObjectRepository.getObject("environment");
/*  813 */     env.put("IdcProductVersion", VersionInfo.getProductVersion());
/*  814 */     env.put("IdcProductVersionInfo", VersionInfo.getProductVersionInfo());
/*  815 */     env.put("IdcProductBuildInfo", VersionInfo.getProductBuildInfo());
/*      */ 
/*  817 */     calculateIsDevelopmentBuild();
/*      */ 
/*  821 */     String reportAllDeprecatedTraceCalls = m_appProperties.getProperty("ReportAllDeprecatedTraceCalls");
/*      */ 
/*  823 */     m_reportAllDeprecatedTraceCalls = StringUtils.convertToBool(reportAllDeprecatedTraceCalls, m_reportAllDeprecatedTraceCalls);
/*      */ 
/*  826 */     m_isInitialized = true;
/*      */   }
/*      */ 
/*      */   public static void reset()
/*      */   {
/*  832 */     m_binDir = null;
/*  833 */     m_cfgFile = null;
/*  834 */     m_propertyFile = new String();
/*  835 */     m_propertyFileDes = new String();
/*  836 */     m_appProperties = new Properties();
/*      */ 
/*  838 */     m_isInitialized = false;
/*  839 */     LoggingUtils.setLogger(new Log());
/*      */ 
/*  842 */     m_verbose = false;
/*      */ 
/*  845 */     m_failOnReplacementCharacterDefault = false;
/*  846 */     m_failOnEncodingFailure = false;
/*  847 */     m_writeUTF8Signature = true;
/*  848 */     m_writeUnicodeSignature = false;
/*      */ 
/*  850 */     m_traceLogFormat = new SimpleDateFormat("MM.dd HH:mm:ss.SSS");
/*      */   }
/*      */ 
/*      */   protected static int typeToLevel(int type)
/*      */   {
/*  860 */     switch (type)
/*      */     {
/*      */     case 0:
/*  863 */       return 5000;
/*      */     case 1:
/*  865 */       return 4000;
/*      */     case 2:
/*  867 */       return 3000;
/*      */     case 3:
/*  869 */       return 1000;
/*      */     }
/*  871 */     throw new AssertionError("Unknown message type " + type);
/*      */   }
/*      */ 
/*      */   public static void reportError(int type, String msg)
/*      */   {
/*  878 */     reportTraceDeprecation("obsolete method SystemUtils.reportError() called");
/*      */ 
/*  880 */     int level = typeToLevel(type);
/*  881 */     Report.message(null, null, level, msg, null, -1, -1, null, null);
/*      */   }
/*      */ 
/*      */   public static void reportErrorEx(Throwable t, int type, String msg, String app)
/*      */   {
/*  887 */     reportTraceDeprecation("obsolete method SystemUtils.reportErrorEx() called");
/*      */ 
/*  889 */     int level = typeToLevel(type);
/*  890 */     Report.message(app, null, level, msg, null, -1, -1, t, null);
/*      */   }
/*      */ 
/*      */   public static void info(String msg)
/*      */   {
/*  896 */     reportTraceDeprecation("obsolete method SystemUtils.info() called");
/*      */ 
/*  898 */     Report.info(null, msg, null);
/*      */   }
/*      */ 
/*      */   public static void infoEx(String msg, String app)
/*      */   {
/*  904 */     reportTraceDeprecation("obsolete method SystemUtils.infoEx() called");
/*      */ 
/*  906 */     Report.appInfo(app, null, msg, null);
/*      */   }
/*      */ 
/*      */   public static void warning(Exception e, String msg)
/*      */   {
/*  912 */     reportTraceDeprecation("obsolete method SystemUtils.warning() called");
/*      */ 
/*  914 */     Report.warning(null, msg, e);
/*      */   }
/*      */ 
/*      */   public static void warningEx(Exception e, String msg, String app)
/*      */   {
/*  920 */     reportTraceDeprecation("obsolete method SystemUtils.warningEx() called");
/*      */ 
/*  922 */     Report.appWarning(app, null, msg, e);
/*      */   }
/*      */ 
/*      */   public static void warn(Throwable t, String msg)
/*      */   {
/*  928 */     reportTraceDeprecation("obsolete method SystemUtils.warn() called");
/*      */ 
/*  930 */     Report.warning(null, msg, t);
/*      */   }
/*      */ 
/*      */   public static void warnEx(Throwable t, String msg, String app)
/*      */   {
/*  936 */     reportTraceDeprecation("obsolete method SystemUtils.warnEx() called");
/*      */ 
/*  938 */     Report.appWarning(app, null, msg, t);
/*      */   }
/*      */ 
/*      */   public static void error(Exception e, String msg)
/*      */   {
/*  944 */     reportTraceDeprecation("obsolete method SystemUtils.error() called.");
/*      */ 
/*  946 */     Report.error(null, msg, e);
/*      */   }
/*      */ 
/*      */   public static void errorEx(Exception e, String msg, String app)
/*      */   {
/*  952 */     reportTraceDeprecation("obsolete method SystemUtils.errorEx() called.");
/*      */ 
/*  954 */     Report.appError(app, null, msg, e);
/*      */   }
/*      */ 
/*      */   public static void err(Throwable t, String msg)
/*      */   {
/*  960 */     reportTraceDeprecation("obsolete method SystemUtils.err() called.");
/*      */ 
/*  962 */     Report.error(null, msg, t);
/*      */   }
/*      */ 
/*      */   public static void errEx(Throwable t, String msg, String app)
/*      */   {
/*  968 */     reportTraceDeprecation("obsolete method SystemUtils.errEx() called.");
/*      */ 
/*  970 */     Report.appError(app, null, msg, t);
/*      */   }
/*      */ 
/*      */   public static void fatal(Exception e, String msg)
/*      */   {
/*  976 */     reportTraceDeprecation("obsolete method SystemUtils.fatal() called.");
/*      */ 
/*  978 */     Report.fatal(null, msg, e);
/*      */   }
/*      */ 
/*      */   public static void fatal(Throwable t, String msg)
/*      */   {
/*  984 */     reportTraceDeprecation("obsolete method SystemUtils.fatal() called.");
/*      */ 
/*  986 */     Report.fatal(null, msg, t);
/*      */   }
/*      */ 
/*      */   public static void fatalEx(Throwable t, String msg, String app)
/*      */   {
/*  992 */     reportTraceDeprecation("obsolete method SystemUtils.fatalEx() called.");
/*      */ 
/*  994 */     Report.appFatal(app, null, msg, t);
/*      */   }
/*      */ 
/*      */   public static void handleFatalException(Throwable t, IdcMessage baseMessage, int rc)
/*      */   {
/* 1002 */     if (baseMessage == null)
/*      */     {
/* 1004 */       baseMessage = IdcMessageFactory.lc("csFailedToInitServer", new Object[0]);
/*      */     }
/*      */     try
/*      */     {
/* 1008 */       Report.fatal(null, t, baseMessage);
/* 1009 */       String msgText = LocaleResources.localizeMessage(null, baseMessage, null).toString();
/* 1010 */       errln(msgText);
/* 1011 */       msgText = LocaleResources.localizeMessage(null, IdcMessageFactory.lc(t), null).toString();
/* 1012 */       errln(msgText);
/*      */     }
/*      */     catch (Throwable t2)
/*      */     {
/* 1016 */       t2.printStackTrace();
/*      */     }
/* 1018 */     System.exit(rc);
/*      */   }
/*      */ 
/*      */   public static boolean getFailOnReplacementCharacterDefault()
/*      */   {
/* 1026 */     return m_failOnReplacementCharacterDefault;
/*      */   }
/*      */ 
/*      */   public static void setFailOnReplacementCharacterDefault(boolean newValue)
/*      */   {
/* 1031 */     m_failOnReplacementCharacterDefault = newValue;
/*      */   }
/*      */ 
/*      */   public static boolean getFailOnEncodingFailure()
/*      */   {
/* 1036 */     return m_failOnEncodingFailure;
/*      */   }
/*      */ 
/*      */   public static void setFailOnEncodingFailure(boolean newValue)
/*      */   {
/* 1041 */     m_failOnEncodingFailure = newValue;
/*      */   }
/*      */ 
/*      */   public static void setWriteUnicodeSignature(boolean newValue)
/*      */   {
/* 1055 */     m_writeUnicodeSignature = newValue;
/*      */   }
/*      */ 
/*      */   public static boolean getWriteUnicodeSignature()
/*      */   {
/* 1060 */     return m_writeUnicodeSignature;
/*      */   }
/*      */ 
/*      */   public static void setWriteUTF8Signature(boolean newValue)
/*      */   {
/* 1073 */     m_writeUTF8Signature = newValue;
/*      */   }
/*      */ 
/*      */   public static boolean getWriteUTF8Signature()
/*      */   {
/* 1078 */     return m_writeUTF8Signature;
/*      */   }
/*      */ 
/*      */   public static boolean isActiveTrace(String section)
/*      */   {
/* 1097 */     if (section == null)
/*      */     {
/* 1099 */       return true;
/*      */     }
/* 1101 */     TraceSection[] data = getSectionData(section);
/* 1102 */     return data.length > 0;
/*      */   }
/*      */ 
/*      */   public static TraceSection[] getSectionData(String section)
/*      */   {
/* 1113 */     if (section == null)
/*      */     {
/* 1115 */       return null;
/*      */     }
/* 1117 */     ReportHandler h = Report.getDelegator().getDefaultReportHandler();
/* 1118 */     if (h instanceof DefaultReportHandler)
/*      */     {
/* 1120 */       return ((DefaultReportHandler)h).getSectionData(section);
/*      */     }
/* 1122 */     return null;
/*      */   }
/*      */ 
/*      */   public static void addAsActiveTrace(String pattern)
/*      */   {
/* 1131 */     ReportHandler h = Report.getDelegator().getDefaultReportHandler();
/* 1132 */     if (!h instanceof DefaultReportHandler)
/*      */       return;
/* 1134 */     ((DefaultReportHandler)h).addAsActivePattern(pattern);
/*      */   }
/*      */ 
/*      */   public static void addAsDefaultTrace(String pattern)
/*      */   {
/* 1146 */     ReportHandler h = Report.getDelegator().getDefaultReportHandler();
/* 1147 */     if (!h instanceof DefaultReportHandler)
/*      */       return;
/* 1149 */     ((DefaultReportHandler)h).addAsDefaultPattern(pattern);
/*      */   }
/*      */ 
/*      */   public static void removeAsActiveTrace(String pattern)
/*      */   {
/* 1158 */     ReportHandler h = Report.getDelegator().getDefaultReportHandler();
/* 1159 */     if (!h instanceof DefaultReportHandler)
/*      */       return;
/* 1161 */     ((DefaultReportHandler)h).removeAsActivePattern(pattern);
/*      */   }
/*      */ 
/*      */   public static void setActiveTraces(Vector patternList)
/*      */   {
/* 1170 */     ReportHandler h = Report.getDelegator().getDefaultReportHandler();
/* 1171 */     if (!h instanceof DefaultReportHandler)
/*      */       return;
/* 1173 */     ((DefaultReportHandler)h).setActivePatterns(patternList);
/*      */   }
/*      */ 
/*      */   public static Vector getActiveTraces()
/*      */   {
/* 1182 */     ReportHandler h = Report.getDelegator().getDefaultReportHandler();
/* 1183 */     if (h instanceof DefaultReportHandler)
/*      */     {
/* 1185 */       return ((DefaultReportHandler)h).getActivePatterns();
/*      */     }
/* 1187 */     return null;
/*      */   }
/*      */ 
/*      */   public static void setActiveTraces(Vector patternList, String traceType)
/*      */   {
/* 1195 */     ReportHandler h = Report.getDelegator().getDefaultReportHandler();
/* 1196 */     if (!h instanceof DefaultReportHandler)
/*      */       return;
/* 1198 */     ((DefaultReportHandler)h).setActivePatterns(patternList, traceType);
/*      */   }
/*      */ 
/*      */   public static Vector getActiveTraces(String traceType)
/*      */   {
/* 1207 */     ReportHandler h = Report.getDelegator().getDefaultReportHandler();
/* 1208 */     if (h instanceof DefaultReportHandler)
/*      */     {
/* 1210 */       return ((DefaultReportHandler)h).getActivePatterns(traceType);
/*      */     }
/* 1212 */     return null;
/*      */   }
/*      */ 
/*      */   public static void reportDeprecatedUsage(String msg)
/*      */   {
/* 1221 */     Report.deprecatedUsage(msg);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String formatTraceLogDate(Date d)
/*      */   {
/* 1232 */     reportTraceDeprecation("SystemUtils.formatTraceLogDate() is deprecated.");
/* 1233 */     return m_traceLogFormat.format(d);
/*      */   }
/*      */ 
/*      */   public static void trace(String section, String message)
/*      */   {
/* 1241 */     reportTraceDeprecation("Use Report.trace() instead of SystemUtils.trace().");
/*      */ 
/* 1243 */     Report.trace(section, message, null);
/*      */   }
/*      */ 
/*      */   public static void traceWithDate(String section, String message, Date d)
/*      */   {
/* 1252 */     Report.message(null, section, 6000, message, null, -1, -1, null, d);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void traceDirectToOutput(String section, String message)
/*      */   {
/* 1265 */     Report.trace(section, message, null);
/*      */   }
/*      */ 
/*      */   public static void traceBytes(String section, byte[] b, int start, int len)
/*      */   {
/* 1274 */     Report.message(null, section, 6000, null, b, start, len, null, null);
/*      */   }
/*      */ 
/*      */   public static void dumpException(String section, Throwable t)
/*      */   {
/* 1284 */     Report.trace(section, null, t);
/*      */   }
/*      */ 
/*      */   public static void traceDumpException(String section, String message, Throwable t)
/*      */   {
/* 1294 */     Report.trace(section, message, t);
/*      */   }
/*      */ 
/*      */   public static void reportTraceDeprecation(String msg)
/*      */   {
/* 1302 */     if ((m_reportedTraceDeprecation) && (!m_reportAllDeprecatedTraceCalls))
/*      */       return;
/* 1304 */     Report.deprecatedUsage(msg);
/* 1305 */     m_reportedTraceDeprecation = true;
/*      */   }
/*      */ 
/*      */   public static void outln(String message)
/*      */   {
/* 1316 */     m_out.println(message);
/*      */   }
/*      */ 
/*      */   public static void out(String message)
/*      */   {
/* 1324 */     m_out.print(message);
/*      */   }
/*      */ 
/*      */   public static void errln(String message)
/*      */   {
/* 1329 */     m_err.println(message);
/*      */   }
/*      */ 
/*      */   public static void err(String message)
/*      */   {
/* 1334 */     m_err.print(message);
/*      */   }
/*      */ 
/*      */   public static boolean sleep(long millis)
/*      */   {
/*      */     try
/*      */     {
/* 1348 */       if (millis > 5000L)
/*      */       {
/* 1352 */         synchronized (m_isServerStoppedPtr)
/*      */         {
/* 1354 */           if (m_isServerStopped);
/* 1360 */           m_isServerStoppedPtr.wait(millis);
/*      */         }
/*      */ 
/*      */       }
/*      */       else {
/* 1365 */         Thread.sleep(millis);
/*      */       }
/*      */     }
/*      */     catch (InterruptedException ignore)
/*      */     {
/* 1370 */       if ((!m_isServerStopped) && (m_verbose))
/*      */       {
/* 1372 */         Report.trace(null, null, ignore);
/*      */       }
/* 1374 */       return false;
/*      */     }
/* 1376 */     return true;
/*      */   }
/*      */ 
/*      */   public static void wait(Object o, long millis)
/*      */     throws InterruptedException
/*      */   {
/* 1388 */     if ((m_isServerStopped) && (millis > 1000L))
/*      */     {
/* 1391 */       millis = 1000L;
/*      */     }
/* 1393 */     o.wait(millis);
/*      */   }
/*      */ 
/*      */   public static boolean sleepRandom(long minMillis, long maxMillis)
/*      */   {
/* 1408 */     if (m_waitRandom == null)
/*      */     {
/* 1410 */       m_waitRandom = new Random(System.currentTimeMillis() * System.currentTimeMillis());
/*      */     }
/*      */ 
/* 1413 */     if (minMillis > maxMillis)
/*      */     {
/* 1415 */       long tmp = maxMillis;
/* 1416 */       maxMillis = minMillis;
/* 1417 */       minMillis = tmp;
/*      */     }
/* 1419 */     long range = maxMillis - minMillis + 1L;
/* 1420 */     int rnd = m_waitRandom.nextInt();
/* 1421 */     if (rnd < 0)
/*      */     {
/* 1423 */       rnd *= -1;
/*      */     }
/* 1425 */     return sleep(minMillis + rnd % range);
/*      */   }
/*      */ 
/*      */   public static void setExceptionCause(Throwable newException, Throwable causedByException)
/*      */   {
/* 1430 */     if ((newException == null) || (causedByException == null))
/*      */     {
/* 1432 */       return;
/*      */     }
/*      */     try
/*      */     {
/* 1436 */       ClassHelperUtils.executeMethod(newException, "initCause", new Object[] { causedByException }, new Class[] { ClassHelper.m_throwableClass });
/*      */     }
/*      */     catch (Throwable ignore)
/*      */     {
/* 1440 */       if (!m_verbose)
/*      */         return;
/* 1442 */       Report.debug(null, "setExceptionCause threw: ", ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void setExceptionOrigin(Throwable newException, Throwable origin)
/*      */   {
/* 1449 */     Throwable t = newException;
/*      */     try
/*      */     {
/*      */       while (true)
/*      */       {
/* 1454 */         Throwable cause = (Throwable)ClassHelperUtils.executeMethod(t, "getCause", new Object[0], new Class[0]);
/*      */ 
/* 1456 */         if (cause == null)
/*      */           break;
/* 1458 */         t = cause;
/*      */       }
/*      */ 
/* 1461 */       setExceptionCause(t, origin);
/* 1462 */       return;
/*      */     }
/*      */     catch (Throwable ignore)
/*      */     {
/* 1467 */       if (!m_verbose)
/*      */         return;
/* 1469 */       Report.debug(null, "setExceptionOrigin threw: ", ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String getProcessIdString()
/*      */   {
/* 1476 */     String pidString = "?";
/*      */     try
/*      */     {
/* 1480 */       NativeOsUtils utils = new NativeOsUtils();
/* 1481 */       int pid = utils.getPid();
/* 1482 */       pidString = "" + pid;
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1486 */       Report.trace(null, null, t);
/*      */     }
/*      */ 
/* 1489 */     return pidString;
/*      */   }
/*      */ 
/*      */   public static Object getIdcVersionInfo(String className, Object arg)
/*      */     throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
/*      */   {
/* 1501 */     Object info = null;
/*      */ 
/* 1504 */     if ((className != null) && (!className.startsWith("java")) && (!className.startsWith("sun.")))
/*      */     {
/* 1506 */       Class c = Class.forName(className);
/* 1507 */       boolean methodExists = ClassHelperUtils.checkMethodExistence(c, "idcVersionInfo", new Class[] { ClassHelper.m_objectClass });
/* 1508 */       if (methodExists)
/*      */       {
/* 1510 */         if (arg == null)
/*      */         {
/* 1512 */           arg = "";
/*      */         }
/* 1514 */         info = ClassHelperUtils.executeStaticMethod(c, "idcVersionInfo", new Object[] { arg }, new Class[] { ClassHelper.m_objectClass });
/*      */       }
/*      */     }
/* 1517 */     return info;
/*      */   }
/*      */ 
/*      */   public static void startClientThread(Thread t)
/*      */     throws ServiceException
/*      */   {
/* 1528 */     if (m_systemClientThreadScheduler != null)
/*      */     {
/* 1530 */       m_systemClientThreadScheduler.schedule(t, null);
/*      */     }
/*      */     else
/*      */     {
/* 1534 */       t.start();
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Properties getIdcVersionInfoAsProps(String className, Object arg)
/*      */     throws ServiceException
/*      */   {
/* 1548 */     Object info = null;
/*      */     try
/*      */     {
/* 1551 */       info = getIdcVersionInfo(className, arg);
/*      */     }
/*      */     catch (ClassNotFoundException e)
/*      */     {
/* 1555 */       throw new ServiceException(e);
/*      */     }
/*      */     catch (IllegalAccessException e)
/*      */     {
/* 1559 */       throw new ServiceException(e);
/*      */     }
/*      */     catch (InvocationTargetException e)
/*      */     {
/* 1563 */       throw new ServiceException(e);
/*      */     }
/*      */     catch (NoSuchMethodException e)
/*      */     {
/* 1567 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/* 1570 */     Properties props = convertIdcVersionInfoIntoProps(info);
/* 1571 */     return props;
/*      */   }
/*      */ 
/*      */   public static Properties convertIdcVersionInfoIntoProps(Object info)
/*      */   {
/* 1582 */     if ((null == info) || (info instanceof Properties))
/*      */     {
/* 1584 */       return (Properties)info;
/*      */     }
/* 1586 */     if (info instanceof String)
/*      */     {
/* 1588 */       Properties props = new Properties();
/* 1589 */       StringUtils.parsePropertiesEx(props, (String)info, ',', ',', '=');
/* 1590 */       return props;
/*      */     }
/* 1592 */     return null;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1597 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99307 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.SystemUtils
 * JD-Core Version:    0.5.4
 */