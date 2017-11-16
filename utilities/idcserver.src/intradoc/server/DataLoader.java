/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.LoggingUtils;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.conversion.CryptoPasswordUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.SimpleParameters;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.resource.ComponentData;
/*      */ import intradoc.resource.ResourceCacheInfo;
/*      */ import intradoc.resource.ResourceCacheState;
/*      */ import intradoc.resource.ResourceLoader;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.flexarea.FlexAreaFunctions;
/*      */ import intradoc.server.flexarea.FlexAreaOutput;
/*      */ import intradoc.server.flexarea.ScriptFlexAreaOutput;
/*      */ import intradoc.shared.ResultSetTreeSort;
/*      */ import intradoc.shared.SharedLoader;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.SharedPageMergerData;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.StringReader;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DataLoader
/*      */ {
/*   79 */   public static int[] m_syncObject = { 0 };
/*      */ 
/*   82 */   static boolean m_isInit = false;
/*      */ 
/*   85 */   public static boolean m_doResourceCacheChecking = false;
/*      */ 
/*   89 */   public static int m_cacheCheckTimeIntervalMillis = 2000;
/*      */ 
/*   94 */   public static int m_warningCacheCheckTimeThresholdMillis = 100;
/*      */ 
/*   97 */   public static IntervalData m_lastSharedCacheTime = null;
/*      */ 
/*      */   public void checkInit()
/*      */   {
/*  101 */     if (m_isInit)
/*      */       return;
/*  103 */     m_cacheCheckTimeIntervalMillis = SharedObjects.getTypedEnvironmentInt("SharedCacheCheckingTimeInterval", m_cacheCheckTimeIntervalMillis, 18, 18);
/*      */ 
/*  105 */     m_warningCacheCheckTimeThresholdMillis = SharedObjects.getTypedEnvironmentInt("SharedCacheCheckingWarningTimeInterval", m_warningCacheCheckTimeThresholdMillis, 18, 18);
/*      */ 
/*  108 */     m_isInit = true;
/*      */   }
/*      */ 
/*      */   public static void cacheSystemProperties()
/*      */     throws DataException
/*      */   {
/*      */     try
/*      */     {
/*  116 */       SystemUtils.getAppProperty("Dummy");
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  120 */       throw new DataException(e.getMessage());
/*      */     }
/*      */ 
/*  123 */     if (!SystemUtils.checkStartup())
/*      */     {
/*  125 */       throw new DataException("!csUnableToFindConfigFile");
/*      */     }
/*      */ 
/*  131 */     String binDir = SystemUtils.getBinDir();
/*  132 */     SharedObjects.addSecureEnvironmentKey("BinDir");
/*  133 */     SharedObjects.putEnvironmentValueWithoutOverwrite("BinDir", binDir, "cacheSystemProperties");
/*      */ 
/*  135 */     Properties env = SystemUtils.getAppProperties();
/*      */ 
/*  138 */     String prefix = env.getProperty("LogMessagePrefix");
/*  139 */     if (prefix == null)
/*  140 */       prefix = env.getProperty("ClusterNodeName");
/*  141 */     if ((prefix != null) && (prefix.length() > 0))
/*      */     {
/*  143 */       String curPrefix = LoggingUtils.getLogFileMsgPrefix();
/*  144 */       if ((curPrefix != null) && (curPrefix.length() > 0))
/*      */       {
/*  146 */         curPrefix = curPrefix + prefix + " " + curPrefix;
/*      */       }
/*      */       else
/*      */       {
/*  150 */         curPrefix = prefix;
/*      */       }
/*  152 */       LoggingUtils.setLogFileMsgPrefix(curPrefix);
/*      */     }
/*      */ 
/*  155 */     Map args = new HashMap();
/*  156 */     cachePropertiesImplementor(env, SystemUtils.getCfgFilePath(), args, true);
/*      */   }
/*      */ 
/*      */   public static void cachePropertiesFromFile(String path, Map args)
/*      */     throws DataException
/*      */   {
/*  167 */     cachePropertiesFromFileEx(path, args, true);
/*      */   }
/*      */ 
/*      */   public static Properties cachePropertiesFromFileWithOverwrite(String path, Map args)
/*      */     throws DataException
/*      */   {
/*  180 */     return cachePropertiesFromFileImplementor(path, args, true);
/*      */   }
/*      */ 
/*      */   public static Properties cachePropertiesFromFileWithoutOverwrite(String path, Map args)
/*      */     throws DataException
/*      */   {
/*  193 */     return cachePropertiesFromFileImplementor(path, args, false);
/*      */   }
/*      */ 
/*      */   public static void cachePropertiesFromFileEx(String path, Map args, boolean allowOverride)
/*      */     throws DataException
/*      */   {
/*  206 */     cachePropertiesFromFileImplementor(path, args, allowOverride);
/*      */   }
/*      */ 
/*      */   protected static Properties cachePropertiesFromFileImplementor(String path, Map args, boolean allowOverride)
/*      */     throws DataException
/*      */   {
/*  219 */     Properties props = new Properties();
/*      */     try
/*      */     {
/*  222 */       FileUtils.loadProperties(props, path);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  227 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadProps", e.getMessage(), path);
/*      */ 
/*  229 */       throw new DataException(msg);
/*      */     }
/*  231 */     cachePropertiesImplementor(props, path, args, allowOverride);
/*  232 */     return props;
/*      */   }
/*      */ 
/*      */   public static void cacheProperties(Properties props, Map args)
/*      */   {
/*  237 */     cachePropertiesEx(props, args, true);
/*      */   }
/*      */ 
/*      */   public static void cachePropertiesEx(Properties props, Map args, boolean allowOverride)
/*      */   {
/*  242 */     cachePropertiesImplementor(props, null, args, allowOverride);
/*      */   }
/*      */ 
/*      */   protected static void cachePropertiesImplementor(Properties props, String source, Map args, boolean allowOverwrite)
/*      */   {
/*  248 */     boolean isExtractPasswords = false;
/*  249 */     if (args != null)
/*      */     {
/*  251 */       isExtractPasswords = StringUtils.convertToBool((String)args.get("isExtractPasswords"), false);
/*      */     }
/*  253 */     for (Enumeration e = props.propertyNames(); e.hasMoreElements(); )
/*      */     {
/*  255 */       String key = (String)e.nextElement();
/*  256 */       String value = props.getProperty(key);
/*  257 */       SharedObjects.putEnvironmentValueAllowOverwrite(key, value, source, allowOverwrite);
/*      */     }
/*      */ 
/*  260 */     if (!isExtractPasswords)
/*      */       return;
/*  262 */     extractPasswordUpdate(props, source, args, allowOverwrite);
/*      */   }
/*      */ 
/*      */   public static void extractPasswordUpdate(Properties props, String source, Map args, boolean allowOverwrite)
/*      */   {
/*      */     try
/*      */     {
/*  271 */       DataResultSet configSet = SharedObjects.getTable("SecurityConfigFields");
/*  272 */       if (configSet == null)
/*      */       {
/*  274 */         configSet = CryptoPasswordUtils.createSecuritySet();
/*      */       }
/*      */ 
/*  277 */       CryptoPasswordUtils.populatePasswordSet(props, configSet, source, args, allowOverwrite);
/*      */ 
/*  279 */       SharedObjects.putTable("SecurityConfigFields", configSet);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  283 */       Report.error(null, e, "csExtractPasswordsError", new Object[] { source });
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void cacheDataBinderFromFile(String filename, ComponentData cmpData)
/*      */     throws ServiceException
/*      */   {
/*  290 */     cacheDataBinderFromFileWithFlags(filename, cmpData, 32);
/*      */   }
/*      */ 
/*      */   public static void cacheDataBinderFromFileWithFlags(String filename, ComponentData cmpData, int flags)
/*      */     throws ServiceException
/*      */   {
/*  296 */     DataBinder data = ResourceLoader.loadDataBinderFromFileWithFlags(filename, flags);
/*  297 */     if (data == null) {
/*      */       return;
/*      */     }
/*  300 */     Enumeration en = data.getResultSetList();
/*  301 */     while (en.hasMoreElements())
/*      */     {
/*  303 */       String key = (String)en.nextElement();
/*  304 */       DataResultSet rset = (DataResultSet)data.getResultSet(key);
/*  305 */       ComponentLoader.addExtraComponentColumn(key, rset, cmpData);
/*  306 */       SharedObjects.putTable(key, rset);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void cacheTemplateDataFromFile(String dir, String filename, ComponentData cmpData)
/*      */     throws ServiceException
/*      */   {
/*  314 */     DataBinder data = ResourceUtils.readDataBinder(dir, filename);
/*  315 */     cacheTemplateData(data, dir, "filename", cmpData);
/*      */   }
/*      */ 
/*      */   public static void cacheTemplateData(DataBinder data, String dir, String columnName, ComponentData cmpData)
/*      */     throws ServiceException
/*      */   {
/*  322 */     Properties props = (Properties)data.getLocalData().clone();
/*      */ 
/*  326 */     props.remove("blDateFormat");
/*  327 */     props.remove("blFieldTypes");
/*  328 */     if (props.size() > 0)
/*      */     {
/*  330 */       Report.deprecatedUsage("LocalData in template hda files is deprecated.");
/*      */     }
/*  332 */     cachePropertiesImplementor(props, "templates from " + dir, null, false);
/*      */ 
/*  334 */     Enumeration e = data.getResultSetList();
/*  335 */     while (e.hasMoreElements())
/*      */     {
/*  337 */       String key = (String)e.nextElement();
/*  338 */       DataResultSet table = (DataResultSet)data.getResultSet(key);
/*  339 */       mergeInDir(table, dir, columnName);
/*  340 */       ComponentLoader.addExtraComponentColumn(key, table, cmpData);
/*  341 */       SharedObjects.putTable(key, table);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void mergeInDir(DataResultSet rset, String dir, String columnName)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  350 */       int index = ResultSetUtils.getIndexMustExist(rset, columnName);
/*      */ 
/*  352 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*      */       {
/*  354 */         String file = rset.getStringValue(index);
/*      */ 
/*  356 */         if (file.trim().length() <= 0)
/*      */           continue;
/*  358 */         rset.setCurrentValue(index, FileUtils.getAbsolutePath(dir, file));
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  364 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void translateExternalPublishedStrings()
/*      */     throws DataException
/*      */   {
/*  371 */     DataResultSet wfcSet = SharedObjects.getTable("WebFilterConfigurationUntranslated");
/*  372 */     if (wfcSet == null)
/*      */       return;
/*  374 */     DataBinder binder = new DataBinder();
/*  375 */     String isoEnc = DataSerializeUtils.getIsoEncoding(DataSerializeUtils.getSystemEncoding());
/*      */ 
/*  377 */     if (isoEnc != null)
/*      */     {
/*  379 */       binder.putLocal("charset", isoEnc);
/*      */     }
/*  381 */     PageMerger pm = new PageMerger(binder, null);
/*      */ 
/*  383 */     DataResultSet wfcTranslated = new DataResultSet();
/*  384 */     wfcTranslated.copyFieldInfo(wfcSet);
/*  385 */     FieldInfo[] fi = ResultSetUtils.createInfoList(wfcSet, new String[] { "wfcType", "wfcValue" }, false);
/*  386 */     int tI = fi[0].m_index;
/*  387 */     int vI = fi[1].m_index;
/*  388 */     if ((tI >= 0) && (vI >= 0))
/*      */     {
/*  390 */       for (wfcSet.first(); wfcSet.isRowPresent(); wfcSet.next())
/*      */       {
/*  392 */         boolean copyRowOver = true;
/*  393 */         Vector v = (Vector)wfcSet.getCurrentRowValues().clone();
/*  394 */         String type = wfcSet.getStringValue(tI);
/*  395 */         if (type.indexOf("string") >= 0)
/*      */         {
/*  397 */           String val = wfcSet.getStringValue(vI);
/*  398 */           String translated = LocaleResources.getString(val, null);
/*      */           try
/*      */           {
/*  401 */             translated = pm.evaluateScript(translated);
/*      */           }
/*      */           catch (IOException ioe)
/*      */           {
/*      */           }
/*      */ 
/*  408 */           if (!val.equals(translated))
/*      */           {
/*  410 */             v.setElementAt(translated, vI);
/*      */           }
/*      */           else
/*      */           {
/*  414 */             copyRowOver = false;
/*      */           }
/*      */         }
/*  417 */         if (!copyRowOver)
/*      */           continue;
/*  419 */         wfcTranslated.addRow(v);
/*      */       }
/*      */     }
/*      */ 
/*  423 */     SharedObjects.putTable("WebFilterConfiguration", wfcTranslated);
/*  424 */     pm.releaseAllTemporary();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void cacheMetaData(Workspace ws)
/*      */     throws DataException
/*      */   {
/*  434 */     throw new DataException("cacheMetaData() is obsolete.");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void cacheWorkflowData(Workspace ws)
/*      */     throws DataException
/*      */   {
/*  443 */     throw new DataException("cacheMetaData() is obsolete.");
/*      */   }
/*      */ 
/*      */   public static void cacheAliases(Workspace ws) throws DataException
/*      */   {
/*  448 */     throw new DataException("cacheAliases() is obsolete.");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   protected static void cacheSubscriptionTypes()
/*      */     throws DataException
/*      */   {
/*  456 */     throw new DataException("cacheSubscriptionTypes() is obsolete.");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void cacheUsers(Workspace ws)
/*      */     throws DataException
/*      */   {
/*  464 */     throw new DataException("cacheUsers() is obsolete.");
/*      */   }
/*      */ 
/*      */   public static void cacheSecurityGroupLists(Workspace ws)
/*      */     throws DataException
/*      */   {
/*  475 */     cacheTableAndOptList("SecurityGroups", "QsecurityGroups", "dGroupName", "securityGroups", null, ws);
/*      */ 
/*  479 */     boolean areAllSpecial = SharedObjects.getEnvValueAsBoolean("AllSpecialAuthGroups", false);
/*  480 */     if (!areAllSpecial)
/*      */       return;
/*  482 */     Vector grps = SharedObjects.getOptList("securityGroups");
/*  483 */     String str = StringUtils.createString(grps, ',', '^');
/*  484 */     SharedObjects.putEnvironmentValue("SpecialAuthGroups", str);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void cacheFormats()
/*      */     throws DataException
/*      */   {
/*  493 */     throw new DataException("cacheFormats() is obsolete.");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void cacheRenditions()
/*      */     throws DataException
/*      */   {
/*  501 */     throw new DataException("cacheRenditions() is obsolete.");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void cacheCollections()
/*      */     throws DataException
/*      */   {
/*  509 */     throw new DataException("cacheCollections() is obsolete.");
/*      */   }
/*      */ 
/*      */   public static void cachePage(String name, String type, String filename)
/*      */   {
/*      */     try
/*      */     {
/*  516 */       cachePageAllowException(name, type, filename);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  520 */       Report.error("resourceloader", e, "csUnableToLoadTemplatePage", new Object[0]);
/*  521 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadTemplatePage", e.getMessage(), name);
/*      */ 
/*  523 */       ExecutionContext cxt = new ExecutionContextAdaptor();
/*  524 */       msg = LocaleResources.localizeMessage(msg, cxt);
/*  525 */       Report.trace("resourceloader", msg, e);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  529 */       Report.error("resourceloader", e, "csUnableToLoadTemplatePage", new Object[0]);
/*  530 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadTemplatePage", e.getMessage(), name);
/*      */ 
/*  532 */       ExecutionContext cxt = new ExecutionContextAdaptor();
/*  533 */       msg = LocaleResources.localizeMessage(msg, cxt);
/*  534 */       Report.trace("resourceloader", msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void cachePageAllowException(String name, String type, String filename)
/*      */     throws DataException, ServiceException
/*      */   {
/*  541 */     if (SystemUtils.m_verbose)
/*      */     {
/*  543 */       Report.debug("resourceloader", "loading " + type + " " + name + " from " + filename, null);
/*      */     }
/*      */ 
/*  547 */     String flexIncludeKey = "formtypeflexinclude:" + type;
/*  548 */     String flexInclude = SharedObjects.getEnvironmentValue(flexIncludeKey);
/*  549 */     String sourcePath = filename;
/*  550 */     long originalTimestamp = -1L;
/*  551 */     if (flexInclude != null)
/*      */     {
/*  554 */       FlexAreaOutput flexOutput = new ScriptFlexAreaOutput(flexInclude);
/*  555 */       String dir = FileUtils.getDirectory(filename);
/*  556 */       String filenameonly = FileUtils.getName(filename);
/*  557 */       sourcePath = dir + "/flexmerge_" + filenameonly;
/*  558 */       File file = FileUtilsCfgBuilder.getCfgFile(filename, null, false);
/*  559 */       originalTimestamp = file.lastModified();
/*  560 */       FlexAreaFunctions.createMergedFile(dir, filename, sourcePath, flexOutput);
/*      */     }
/*      */ 
/*  563 */     ResourceCacheInfo info = ResourceCacheState.addCacheInfo(name, type, filename);
/*  564 */     DynamicHtml dynHtml = ResourceLoader.loadPage(sourcePath, false);
/*      */ 
/*  566 */     if (dynHtml == null)
/*      */       return;
/*  568 */     if (originalTimestamp < 0L)
/*      */     {
/*  570 */       originalTimestamp = dynHtml.m_timeStamp;
/*      */     }
/*  572 */     SharedObjects.putHtmlPage(name, dynHtml);
/*  573 */     info.m_lastLoaded = originalTimestamp;
/*      */   }
/*      */ 
/*      */   public static void createDuplicateReference(String templateAlias, String originalTemplate)
/*      */     throws ServiceException
/*      */   {
/*  580 */     DynamicHtml dynHtml = SharedObjects.getHtmlPage(originalTemplate);
/*  581 */     ResourceCacheInfo cacheInfo = ResourceCacheState.getCacheInfo(originalTemplate);
/*  582 */     if ((dynHtml == null) || (cacheInfo == null))
/*      */     {
/*  584 */       String msg = LocaleUtils.encodeMessage("csUnableToCreateTemplateAlias", null, templateAlias, originalTemplate);
/*      */ 
/*  586 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  589 */     SharedObjects.putHtmlPage(templateAlias, dynHtml);
/*  590 */     ResourceCacheState.addCacheInfo(templateAlias, cacheInfo.m_type, cacheInfo.m_filePath);
/*      */   }
/*      */ 
/*      */   public static ResourceContainer loadDynamicResource(String key, String path, Object auxInfo, long startTime, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/*  596 */     ResourceContainer res = null;
/*  597 */     synchronized (m_syncObject)
/*      */     {
/*  600 */       File f = new File(path);
/*  601 */       long ts = f.lastModified();
/*  602 */       ResourceCacheInfo cacheInfo = ResourceCacheState.getTemporaryCache(key, startTime);
/*  603 */       if ((cacheInfo != null) && (cacheInfo.m_resourceObj != null) && (cacheInfo.m_resourceObj instanceof ResourceContainer) && (cacheInfo.m_lastLoaded == ts))
/*      */       {
/*  607 */         res = (ResourceContainer)cacheInfo.m_resourceObj;
/*  608 */         cacheInfo.m_associatedInfo = auxInfo;
/*  609 */         cacheInfo.m_agedTS = (startTime + ResourceCacheState.getAgeResourceFilesTimeoutInSecs() * 1000);
/*      */       }
/*      */       else
/*      */       {
/*  613 */         res = new ResourceContainer();
/*  614 */         ResourceLoader.loadResourceFileEx(res, key, path, false, auxInfo, startTime, null);
/*      */       }
/*      */     }
/*  617 */     return res;
/*      */   }
/*      */ 
/*      */   public static DynamicHtml loadDynamicPage(String key, String path, long startTime, boolean isXmlTemplate, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  623 */     ResourceCacheInfo cacheInfo = null;
/*  624 */     synchronized (m_syncObject)
/*      */     {
/*  627 */       File file = new File(path);
/*  628 */       long lastModified = file.lastModified();
/*  629 */       if (lastModified <= 0L)
/*      */       {
/*  631 */         String msg = LocaleUtils.encodeMessage("csDynamicAppFileMissing", null, path);
/*      */ 
/*  633 */         throw new ServiceException(-16, msg);
/*      */       }
/*  635 */       cacheInfo = ResourceCacheState.getTemporaryCache(key, startTime);
/*  636 */       if (cacheInfo == null)
/*      */       {
/*  638 */         cacheInfo = new ResourceCacheInfo(key, "DynamicPage", path);
/*      */       }
/*      */ 
/*  641 */       if ((cacheInfo.m_resourceObj != null) && 
/*  643 */         (cacheInfo.m_lastLoaded != lastModified))
/*      */       {
/*  645 */         cacheInfo.m_resourceObj = null;
/*      */       }
/*      */ 
/*  648 */       checkSharedCachedResources(cxt);
/*  649 */       if (cacheInfo.m_resourceObj == null)
/*      */       {
/*  651 */         cacheInfo.m_resourceObj = ResourceLoader.loadPage(path, isXmlTemplate);
/*  652 */         cacheInfo.m_lastLoaded = lastModified;
/*  653 */         cacheInfo.m_size = file.length();
/*  654 */         ResourceCacheState.addTimedTemporaryCache(key, cacheInfo, startTime);
/*      */       }
/*      */     }
/*      */ 
/*  658 */     if ((cacheInfo.m_resourceObj == null) || (!cacheInfo.m_resourceObj instanceof DynamicHtml))
/*      */     {
/*  661 */       return null;
/*      */     }
/*  663 */     return (DynamicHtml)cacheInfo.m_resourceObj; } 
/*      */   // ERROR //
/*      */   public static void checkCachedPage(String name, ExecutionContext cxt) throws ServiceException { // Byte code:
/*      */     //   0: aload_1
/*      */     //   1: ifnonnull +61 -> 62
/*      */     //   4: new 27	java/lang/StringBuilder
/*      */     //   7: dup
/*      */     //   8: invokespecial 28	java/lang/StringBuilder:<init>	()V
/*      */     //   11: ldc 195
/*      */     //   13: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   16: aload_0
/*      */     //   17: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   20: ldc 196
/*      */     //   22: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   25: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   28: astore_2
/*      */     //   29: getstatic 146	intradoc/common/SystemUtils:m_verbose	Z
/*      */     //   32: ifeq +22 -> 54
/*      */     //   35: new 11	intradoc/data/DataException
/*      */     //   38: dup
/*      */     //   39: aload_2
/*      */     //   40: invokespecial 13	intradoc/data/DataException:<init>	(Ljava/lang/String;)V
/*      */     //   43: astore_3
/*      */     //   44: ldc 138
/*      */     //   46: aconst_null
/*      */     //   47: aload_3
/*      */     //   48: invokestatic 149	intradoc/common/Report:debug	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   51: goto +10 -> 61
/*      */     //   54: ldc 138
/*      */     //   56: aload_2
/*      */     //   57: aconst_null
/*      */     //   58: invokestatic 144	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   61: return
/*      */     //   62: getstatic 197	intradoc/server/DataLoader:m_doResourceCacheChecking	Z
/*      */     //   65: ifne +40 -> 105
/*      */     //   68: getstatic 146	intradoc/common/SystemUtils:m_verbose	Z
/*      */     //   71: ifeq +33 -> 104
/*      */     //   74: ldc 138
/*      */     //   76: new 27	java/lang/StringBuilder
/*      */     //   79: dup
/*      */     //   80: invokespecial 28	java/lang/StringBuilder:<init>	()V
/*      */     //   83: ldc 195
/*      */     //   85: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   88: aload_0
/*      */     //   89: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   92: ldc 198
/*      */     //   94: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   97: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   100: aconst_null
/*      */     //   101: invokestatic 149	intradoc/common/Report:debug	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   104: return
/*      */     //   105: getstatic 174	intradoc/server/DataLoader:m_syncObject	[I
/*      */     //   108: dup
/*      */     //   109: astore_2
/*      */     //   110: monitorenter
/*      */     //   111: aload_0
/*      */     //   112: ifnull +188 -> 300
/*      */     //   115: new 27	java/lang/StringBuilder
/*      */     //   118: dup
/*      */     //   119: invokespecial 28	java/lang/StringBuilder:<init>	()V
/*      */     //   122: aload_0
/*      */     //   123: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   126: ldc 199
/*      */     //   128: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   131: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   134: astore_3
/*      */     //   135: aload_1
/*      */     //   136: aload_3
/*      */     //   137: invokeinterface 200 2 0
/*      */     //   142: checkcast 47	java/lang/String
/*      */     //   145: iconst_0
/*      */     //   146: invokestatic 48	intradoc/common/StringUtils:convertToBool	(Ljava/lang/String;Z)Z
/*      */     //   149: ifeq +42 -> 191
/*      */     //   152: getstatic 146	intradoc/common/SystemUtils:m_verbose	Z
/*      */     //   155: ifeq +33 -> 188
/*      */     //   158: ldc 138
/*      */     //   160: new 27	java/lang/StringBuilder
/*      */     //   163: dup
/*      */     //   164: invokespecial 28	java/lang/StringBuilder:<init>	()V
/*      */     //   167: ldc 201
/*      */     //   169: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   172: aload_0
/*      */     //   173: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   176: ldc 202
/*      */     //   178: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   181: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   184: aconst_null
/*      */     //   185: invokestatic 149	intradoc/common/Report:debug	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   188: aload_2
/*      */     //   189: monitorexit
/*      */     //   190: return
/*      */     //   191: aload_1
/*      */     //   192: aload_3
/*      */     //   193: ldc 203
/*      */     //   195: invokeinterface 204 3 0
/*      */     //   200: aload_0
/*      */     //   201: invokestatic 168	intradoc/resource/ResourceCacheState:getCacheInfo	(Ljava/lang/String;)Lintradoc/resource/ResourceCacheInfo;
/*      */     //   204: astore 4
/*      */     //   206: aload 4
/*      */     //   208: ifnull +92 -> 300
/*      */     //   211: new 175	java/io/File
/*      */     //   214: dup
/*      */     //   215: aload 4
/*      */     //   217: getfield 173	intradoc/resource/ResourceCacheInfo:m_filePath	Ljava/lang/String;
/*      */     //   220: invokespecial 176	java/io/File:<init>	(Ljava/lang/String;)V
/*      */     //   223: astore 5
/*      */     //   225: aload 5
/*      */     //   227: invokevirtual 160	java/io/File:lastModified	()J
/*      */     //   230: aload 4
/*      */     //   232: getfield 166	intradoc/resource/ResourceCacheInfo:m_lastLoaded	J
/*      */     //   235: lcmp
/*      */     //   236: ifeq +20 -> 256
/*      */     //   239: aload_0
/*      */     //   240: aload 4
/*      */     //   242: getfield 172	intradoc/resource/ResourceCacheInfo:m_type	Ljava/lang/String;
/*      */     //   245: aload 4
/*      */     //   247: getfield 173	intradoc/resource/ResourceCacheInfo:m_filePath	Ljava/lang/String;
/*      */     //   250: invokestatic 137	intradoc/server/DataLoader:cachePageAllowException	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   253: goto +47 -> 300
/*      */     //   256: getstatic 146	intradoc/common/SystemUtils:m_verbose	Z
/*      */     //   259: ifeq +41 -> 300
/*      */     //   262: ldc 138
/*      */     //   264: new 27	java/lang/StringBuilder
/*      */     //   267: dup
/*      */     //   268: invokespecial 28	java/lang/StringBuilder:<init>	()V
/*      */     //   271: ldc 205
/*      */     //   273: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   276: aload_0
/*      */     //   277: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   280: ldc 206
/*      */     //   282: invokevirtual 29	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   285: aload 4
/*      */     //   287: getfield 166	intradoc/resource/ResourceCacheInfo:m_lastLoaded	J
/*      */     //   290: invokevirtual 207	java/lang/StringBuilder:append	(J)Ljava/lang/StringBuilder;
/*      */     //   293: invokevirtual 31	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   296: aconst_null
/*      */     //   297: invokestatic 149	intradoc/common/Report:debug	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   300: goto +15 -> 315
/*      */     //   303: astore_3
/*      */     //   304: new 90	intradoc/common/ServiceException
/*      */     //   307: dup
/*      */     //   308: ldc 208
/*      */     //   310: aload_3
/*      */     //   311: invokespecial 209	intradoc/common/ServiceException:<init>	(Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   314: athrow
/*      */     //   315: aload_1
/*      */     //   316: invokestatic 190	intradoc/server/DataLoader:checkSharedCachedResources	(Lintradoc/common/ExecutionContext;)V
/*      */     //   319: aload_2
/*      */     //   320: monitorexit
/*      */     //   321: goto +10 -> 331
/*      */     //   324: astore 6
/*      */     //   326: aload_2
/*      */     //   327: monitorexit
/*      */     //   328: aload 6
/*      */     //   330: athrow
/*      */     //   331: return
/*      */     //
/*      */     // Exception table:
/*      */     //   from	to	target	type
/*      */     //   111	188	303	intradoc/data/DataException
/*      */     //   191	300	303	intradoc/data/DataException
/*      */     //   111	190	324	finally
/*      */     //   191	321	324	finally
/*      */     //   324	328	324	finally } 
/*  745 */   public static void checkSharedCachedResources(ExecutionContext cxt) throws ServiceException { if (cxt == null)
/*      */     {
/*  747 */       return;
/*      */     }
/*      */ 
/*  750 */     boolean forceRefresh = cxt.getCachedObject("forceRefresh") != null;
/*  751 */     if ((!m_doResourceCacheChecking) && (!forceRefresh))
/*      */     {
/*  753 */       return;
/*      */     }
/*      */ 
/*  756 */     String cxtName = "HtmlResourcesIsChecked";
/*  757 */     if (StringUtils.convertToBool((String)cxt.getCachedObject(cxtName), false))
/*      */     {
/*  759 */       if (SystemUtils.m_verbose)
/*      */       {
/*  761 */         Report.debug("resourceloader", "not checking shared resources because they've already been checked", null);
/*      */       }
/*      */ 
/*  764 */       return;
/*      */     }
/*      */ 
/*  768 */     cxt.setCachedObject(cxtName, "1");
/*      */ 
/*  771 */     if (m_lastSharedCacheTime == null)
/*      */     {
/*  773 */       m_lastSharedCacheTime = new IntervalData("DataLoader");
/*      */     }
/*  775 */     long diff = m_lastSharedCacheTime.getInterval();
/*  776 */     diff = (diff < 0L) ? -diff : diff;
/*  777 */     if (diff < m_cacheCheckTimeIntervalMillis * 1000000L)
/*      */     {
/*  779 */       if (SystemUtils.m_verbose)
/*      */       {
/*  781 */         Report.debug("resourceloader", "Skipping resource file check because it is too soon since the last one", null);
/*      */       }
/*  783 */       return;
/*      */     }
/*  785 */     m_lastSharedCacheTime.start();
/*      */ 
/*  787 */     List resList = ResourceCacheState.getResourceList();
/*      */ 
/*  791 */     boolean hasChanged = forceRefresh;
/*  792 */     Map changedFiles = new HashMap();
/*  793 */     int nresFiles = resList.size();
/*  794 */     int numDifferent = 0;
/*  795 */     boolean afterFirstLoop = false;
/*  796 */     for (int loop = 0; loop < 2; ++loop)
/*      */     {
/*  798 */       if (loop == 1)
/*      */       {
/*  800 */         if (!afterFirstLoop)
/*      */         {
/*  802 */           long checkDuration = m_lastSharedCacheTime.getInterval() / 1000000L;
/*  803 */           if (SystemUtils.m_verbose)
/*      */           {
/*  805 */             Report.debug("resourceloader", "Checked resources files and " + numDifferent + " out of " + nresFiles + " have changed and it required " + checkDuration + " milliseconds to do the check", null);
/*      */ 
/*  807 */             if (changedFiles.size() != numDifferent)
/*      */             {
/*  809 */               Report.debug("resourceloader", "The number of files being loaded is " + changedFiles.size() + " which is different from the number of files that were detected to be different", null);
/*      */             }
/*      */           }
/*      */ 
/*  813 */           if (checkDuration > m_warningCacheCheckTimeThresholdMillis)
/*      */           {
/*  815 */             Report.trace("system", "It required " + checkDuration + " millisecond(s) to check " + nresFiles + " shared " + "resource files. Consider enabling the configuration entry DisableSharedCacheChecking to improve performance.", null);
/*      */           }
/*      */ 
/*  818 */           afterFirstLoop = true;
/*      */         }
/*  820 */         if (!hasChanged) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*  825 */       for (int i = 0; i < nresFiles; ++i)
/*      */       {
/*  827 */         ResourceCacheInfo info = (ResourceCacheInfo)resList.get(i);
/*      */ 
/*  829 */         if (loop == 0)
/*      */         {
/*  831 */           File file = new File(info.m_filePath);
/*  832 */           if (file.lastModified() != info.m_lastLoaded)
/*      */           {
/*  834 */             changedFiles.put(info.m_filePath, "1");
/*  835 */             hasChanged = true;
/*  836 */             if (SystemUtils.m_verbose)
/*      */             {
/*  838 */               Report.debug("resourceloader", "Detected change in shared resource file " + info.m_filePath, null);
/*      */             }
/*  840 */             ++numDifferent;
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  845 */           if (!hasChanged)
/*      */             continue;
/*  847 */           ResourceContainer res = SharedObjects.getResources();
/*      */ 
/*  851 */           if (changedFiles.get(info.m_filePath) != null)
/*      */           {
/*  853 */             if (info.m_languageId != null)
/*      */             {
/*  855 */               ResourceLoader.loadResourceFileEx(res, info.m_filePath, info.m_filePath, true, null, 0L, info.m_languageId);
/*      */             }
/*      */             else
/*      */             {
/*  860 */               cacheResourceFile(res, info.m_filePath);
/*      */             }
/*      */ 
/*      */           }
/*      */           else {
/*  865 */             ResourceLoader.loadCachedResources(res, info);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  872 */     if (!hasChanged)
/*      */       return;
/*  874 */     if (SystemUtils.m_verbose)
/*      */     {
/*  876 */       long checkDuration = m_lastSharedCacheTime.getInterval() / 1000000L;
/*  877 */       Report.debug("resourceloader", "The shared resource load check required " + checkDuration + " milliseconds", null);
/*      */     }
/*  879 */     ResourceContainer res = SharedObjects.getResources();
/*  880 */     LocaleResources.initStrings(res);
/*      */     try
/*      */     {
/*  883 */       String stringDir = DirectoryLocator.getAppDataDirectory();
/*  884 */       stringDir = FileUtils.getAbsolutePath(stringDir, "strings");
/*  885 */       if (FileUtils.checkFile(stringDir, 0) == 0)
/*      */       {
/*  887 */         LocaleService.doStringIndexOp(new DataBinder(), "refresh");
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  892 */       throw new ServiceException(e);
/*      */     } }
/*      */ 
/*      */ 
/*      */   public static void cacheTemplateTables()
/*      */     throws ServiceException
/*      */   {
/*  901 */     Vector templatesData = ComponentLoader.m_templates;
/*  902 */     int size = templatesData.size();
/*  903 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  905 */       ComponentData data = (ComponentData)templatesData.elementAt(i);
/*  906 */       String filename = data.m_file;
/*  907 */       cacheTemplateDataFromFile(FileUtils.getDirectory(filename), FileUtils.getName(filename), data);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void cacheCustomTemplateTables()
/*      */     throws DataException
/*      */   {
/*  916 */     throw new DataException("cacheCustomTemplateTables() is obsolete.");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void cacheCustomResultsTemplates()
/*      */     throws DataException
/*      */   {
/*  924 */     throw new DataException("cacheCustomResultsTemplates() is obsolete.");
/*      */   }
/*      */ 
/*      */   public static void cacheTemplateFiles() throws ServiceException, DataException
/*      */   {
/*  929 */     IntervalData timer = new IntervalData("cacheTemplateFiles()");
/*  930 */     String templatesDir = DirectoryLocator.getTemplatesDirectory();
/*      */ 
/*  932 */     DataResultSet drset = SharedObjects.getTable("IntradocTemplates");
/*  933 */     String[] keys = { "name", "filename", "class", "formtype" };
/*  934 */     String[][] table = ResultSetUtils.createStringTable(drset, keys);
/*      */ 
/*  936 */     for (int i = 0; i < table.length; ++i)
/*      */     {
/*  938 */       String name = table[i][0];
/*  939 */       String filename = table[i][1];
/*      */ 
/*  941 */       if (filename != null)
/*      */       {
/*  943 */         String pagePath = FileUtils.getAbsolutePath(templatesDir, filename);
/*  944 */         cachePage(name, table[i][3], pagePath);
/*      */       }
/*      */ 
/*  948 */       SharedPageMergerData.addTemplateInfo(name, filename, table[i][2], table[i][3]);
/*      */     }
/*  950 */     timer.trace("startup", "Loaded " + table.length + " templates");
/*      */   }
/*      */ 
/*      */   public static void cacheGlobalIncludes() throws ServiceException, DataException
/*      */   {
/*  955 */     serializeGlobalIncludes(null, false);
/*      */   }
/*      */ 
/*      */   public static void serializeGlobalIncludes(DataBinder data, boolean isWrite) throws ServiceException, DataException
/*      */   {
/*  960 */     String globalIncDir = DirectoryLocator.getAppDataDirectory() + "pages/";
/*      */ 
/*  962 */     DataResultSet drset = SharedObjects.getTable("GlobalScriptIncludes");
/*  963 */     if (drset == null)
/*      */       return;
/*  965 */     if ((isWrite) && (data == null))
/*      */     {
/*  967 */       throw new IllegalArgumentException("!csCannotSerializeGlobalIncludesWithoutData");
/*      */     }
/*      */ 
/*  975 */     Hashtable res = new Hashtable();
/*  976 */     Vector dataToSave = new IdcVector();
/*  977 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  979 */       String name = ResultSetUtils.getValue(drset, "name");
/*  980 */       String key = name + ":globalInclude";
/*  981 */       String filePath = globalIncDir + name + ".inc";
/*  982 */       String incData = null;
/*  983 */       String errMsg = LocaleUtils.encodeMessage("csUnableToParseGlobalInclude", null, ResultSetUtils.getValue(drset, "description"));
/*      */       try
/*      */       {
/*  989 */         if (isWrite)
/*      */         {
/*  991 */           String temp = data.getLocal(key);
/*  992 */           if (temp != null)
/*      */           {
/*  994 */             incData = temp;
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  999 */           int access = FileUtils.checkFile(filePath, true, true);
/* 1000 */           if (access == 0)
/*      */           {
/* 1003 */             incData = FileUtils.loadFile(filePath, null, null);
/*      */           }
/*      */         }
/*      */ 
/* 1007 */         if (incData == null)
/*      */         {
/* 1009 */           String defIncData = ResultSetUtils.getValue(drset, "default");
/* 1010 */           String incType = ResultSetUtils.getValue(drset, "type");
/*      */ 
/* 1012 */           if ((incType != null) && (incType.equals("localizedString")))
/*      */           {
/* 1014 */             defIncData = LocaleResources.getString(defIncData, null);
/*      */           }
/*      */ 
/* 1017 */           if (defIncData != null)
/*      */           {
/* 1019 */             char[] buf = new char[defIncData.length()];
/* 1020 */             defIncData.getChars(0, buf.length, buf, 0);
/* 1021 */             incData = StringUtils.decodeXmlEscapeSequence(buf, 0, buf.length);
/*      */           }
/*      */         }
/*      */ 
/* 1025 */         if (incData != null)
/*      */         {
/* 1027 */           if ((isWrite) || (data == null))
/*      */           {
/* 1029 */             DynamicHtml dynHtml = new DynamicHtml();
/* 1030 */             StringReader strReader = new StringReader(incData);
/* 1031 */             dynHtml.loadHtml(strReader, null, false);
/* 1032 */             dynHtml.m_resourceString = incData;
/* 1033 */             res.put(name, dynHtml);
/*      */           }
/*      */           else
/*      */           {
/* 1039 */             data.putLocal(key, incData);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1044 */           String msg = LocaleUtils.encodeMessage("csGlobalIncludeResMissing", null, name);
/* 1045 */           throw new ServiceException(msg);
/*      */         }
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 1050 */         throw new ServiceException(errMsg, e);
/*      */       }
/*      */       catch (ParseSyntaxException e)
/*      */       {
/* 1054 */         throw new ServiceException(e.createMessage(errMsg, incData));
/*      */       }
/*      */ 
/* 1057 */       if (!isWrite)
/*      */         continue;
/* 1059 */       String[] item = { filePath, incData };
/* 1060 */       dataToSave.addElement(item);
/*      */     }
/*      */ 
/* 1065 */     if (isWrite)
/*      */     {
/* 1067 */       for (int i = 0; i < dataToSave.size(); ++i)
/*      */       {
/* 1069 */         String[] item = (String[])(String[])dataToSave.elementAt(i);
/*      */ 
/* 1071 */         String msg = LocaleUtils.encodeMessage("csUnableToSaveContent", null, item[0]);
/*      */ 
/* 1076 */         File incOutputFile = new File(item[0]);
/* 1077 */         FileUtils.writeFile(item[1], incOutputFile, "UTF8", 0, msg);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1082 */     if ((!isWrite) && (data != null))
/*      */       return;
/* 1084 */     SharedObjects.putObject("globalObjects", "GlobalScriptIncludes", res);
/*      */   }
/*      */ 
/*      */   public static void cacheReportFiles()
/*      */     throws ServiceException
/*      */   {
/* 1093 */     copyTables("IntradocReports", "ReportsToLoad");
/*      */ 
/* 1096 */     cacheTemplatePagesInTable("ReportsToLoad", "Report", "datasource", DirectoryLocator.getReportsDirectory());
/*      */   }
/*      */ 
/*      */   public static void putEnvironmentValueIfNew(String key, String val)
/*      */   {
/* 1102 */     String keyAlt = key + "$fromTable";
/* 1103 */     if ((SharedObjects.getEnvironmentValue(keyAlt) == null) && (SharedObjects.getEnvironmentValue(key) != null)) {
/*      */       return;
/*      */     }
/* 1106 */     SharedObjects.putEnvironmentValue(key, val);
/* 1107 */     SharedObjects.putEnvironmentValue(keyAlt, "Yes");
/*      */   }
/*      */ 
/*      */   public static void extractRows(String sourceTable, String destTable, String colName, String[] colValues)
/*      */     throws ServiceException
/*      */   {
/* 1114 */     ResultSet rset = SharedObjects.getTable(sourceTable);
/* 1115 */     FieldInfo fi = new FieldInfo();
/* 1116 */     if (!rset.getFieldInfo(colName, fi))
/*      */     {
/* 1118 */       String msg = LocaleUtils.encodeMessage("csColumnNotFound", null, colName, sourceTable);
/*      */ 
/* 1120 */       throw new ServiceException(msg);
/*      */     }
/* 1122 */     int colIndex = fi.m_index;
/*      */ 
/* 1124 */     DataResultSet drset = new DataResultSet();
/* 1125 */     drset.copyFieldInfo(rset);
/*      */ 
/* 1127 */     for (int i = 0; i < colValues.length; ++i)
/*      */     {
/* 1129 */       Vector v = drset.createEmptyRow();
/* 1130 */       v.setElementAt(colValues[i], colIndex);
/* 1131 */       drset.addRow(v);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1136 */       drset.merge(colName, rset, true);
/* 1137 */       SharedObjects.putTable(destTable, drset);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1141 */       throw new ServiceException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void copyTables(String sourceTable, String destTable)
/*      */   {
/* 1147 */     ResultSet rsSource = SharedObjects.getTable(sourceTable);
/* 1148 */     DataResultSet drsDest = new DataResultSet();
/* 1149 */     drsDest.copy(rsSource);
/* 1150 */     SharedObjects.putTable(destTable, drsDest);
/*      */   }
/*      */ 
/*      */   public static void mergeTables(String sourceTable, String destTable, String colName)
/*      */     throws ServiceException
/*      */   {
/* 1156 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1158 */       Report.debug("resourceloader", "merging " + sourceTable + " into " + destTable + " on column " + colName, null);
/*      */     }
/*      */ 
/* 1161 */     DataResultSet rsSource = SharedObjects.getTable(sourceTable);
/* 1162 */     DataResultSet drsDest = SharedObjects.getTable(destTable);
/* 1163 */     if (rsSource == null)
/*      */     {
/* 1165 */       Report.debug("resourceloader", "DataLoader.mergeTables: the source table " + sourceTable + " does not exist.", null);
/*      */ 
/* 1167 */       return;
/*      */     }
/* 1169 */     if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("resourceloader")))
/*      */     {
/* 1171 */       Report.debug("resourceloader", rsSource.toString(), null);
/* 1172 */       for (SimpleParameters params : rsSource.getSimpleParametersIterable())
/*      */       {
/* 1174 */         Report.debug("resourceloader", params.toString(), null);
/*      */       }
/*      */     }
/* 1177 */     if (drsDest == null)
/*      */     {
/* 1179 */       drsDest = new DataResultSet();
/* 1180 */       drsDest.copy(rsSource);
/* 1181 */       SharedObjects.putTable(destTable, drsDest);
/* 1182 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1187 */       DataResultSet dset = new DataResultSet();
/* 1188 */       dset.copy(drsDest);
/*      */ 
/* 1190 */       ResourceUtils.doResultSetLog(rsSource, sourceTable, destTable, colName);
/*      */ 
/* 1192 */       dset.mergeFields(rsSource);
/* 1193 */       dset.merge(colName, rsSource, false);
/* 1194 */       SharedObjects.putTable(destTable, dset);
/* 1195 */       if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("resourceloader")))
/*      */       {
/* 1197 */         for (SimpleParameters params : dset.getSimpleParametersIterable())
/*      */         {
/* 1199 */           Report.debug("resourceloader", params.toString(), null);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1205 */       String msg = LocaleUtils.encodeMessage("csUnableToMergeTables", e.getMessage(), sourceTable, destTable, colName);
/*      */ 
/* 1207 */       throw new ServiceException(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void cacheTemplatePagesInTable(String tableName, String templateClass, String typeLookupKey, String dir)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1217 */       ResultSet rset = SharedObjects.getTable(tableName);
/* 1218 */       if (rset == null)
/*      */       {
/* 1220 */         String msg = LocaleUtils.encodeMessage("csHtmlTemplateTableMissing", null, tableName);
/*      */ 
/* 1222 */         throw new ServiceException(msg);
/*      */       }
/* 1224 */       String[] keys = { "name", "filename", typeLookupKey };
/* 1225 */       String[][] table = ResultSetUtils.createStringTable(rset, keys);
/*      */ 
/* 1227 */       for (int i = 0; i < table.length; ++i)
/*      */       {
/* 1229 */         String path = FileUtils.getAbsolutePath(dir, table[i][1]);
/* 1230 */         cachePage(table[i][0], table[i][2], path);
/* 1231 */         SharedPageMergerData.addTemplateInfo(table[i][0], table[i][1], templateClass, table[i][2]);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1237 */       String msg = LocaleUtils.encodeMessage("csFailedToCacheTemplatePages", e.getMessage());
/*      */ 
/* 1239 */       throw new ServiceException(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void cacheResourceFile(ResourceContainer res, String filePath)
/*      */     throws ServiceException
/*      */   {
/* 1246 */     ResourceLoader.loadResourceFile(res, filePath);
/*      */   }
/*      */ 
/*      */   public static void cacheTableAndOptList(String resultSetName, String queryName, String optField, String optIndexKey, Parameters args, Workspace ws)
/*      */     throws DataException
/*      */   {
/* 1253 */     cacheTableAndOptListEx(resultSetName, queryName, null, false, optField, optIndexKey, args, ws);
/*      */   }
/*      */ 
/*      */   public static void cacheTableAndOptListEx(String resultSetName, String queryName, String sortField, boolean isCaseSensitive, String optField, String optIndexKey, Parameters args, Workspace ws)
/*      */     throws DataException
/*      */   {
/* 1262 */     ResultSet rset = ws.createResultSet(queryName, null);
/* 1263 */     DataResultSet drset = new DataResultSet();
/* 1264 */     drset.copy(rset);
/*      */ 
/* 1266 */     if (sortField != null)
/*      */     {
/* 1268 */       FieldInfo info = new FieldInfo();
/* 1269 */       if (!drset.getFieldInfo(sortField, info))
/*      */       {
/* 1271 */         throw new DataException(LocaleUtils.encodeMessage("apResultSetSortColumnMissing", null, resultSetName, sortField));
/*      */       }
/*      */ 
/* 1274 */       ResultSetTreeSort sorter = new ResultSetTreeSort(drset, info.m_index, false);
/* 1275 */       sorter.m_isCaseSensitive = isCaseSensitive;
/* 1276 */       sorter.sort();
/*      */     }
/*      */ 
/* 1279 */     SharedObjects.putTable(resultSetName, drset);
/* 1280 */     drset.first();
/* 1281 */     SharedLoader.cacheOptList(drset, optField, optIndexKey);
/*      */   }
/*      */ 
/*      */   public static void cacheOptList(String queryName, String optField, String optIndexKey, Parameters args, Workspace workspace)
/*      */     throws DataException
/*      */   {
/* 1288 */     ResultSet rset = workspace.createResultSet(queryName, args);
/* 1289 */     SharedLoader.cacheOptList(rset, optField, optIndexKey);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void cacheDocMetaOptList(Workspace workspace)
/*      */     throws DataException
/*      */   {
/* 1297 */     throw new DataException("cacheDocMetaOptList() is obsolete.");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadDocOptionsIntoDataBinder(DataBinder binder)
/*      */     throws DataException
/*      */   {
/* 1306 */     throw new DataException("loadDocOptionsIntoDataBinder() is obsolete.");
/*      */   }
/*      */ 
/*      */   public static String getResultTemplateFileName(String templateName)
/*      */     throws DataException
/*      */   {
/* 1312 */     String sourceFile = null;
/*      */ 
/* 1314 */     DataResultSet dset = SharedObjects.getTable("SearchResultTemplates");
/*      */ 
/* 1316 */     FieldInfo[] fieldInfoList = ResultSetUtils.createInfoList(dset, new String[] { "name", "filename" }, true);
/*      */ 
/* 1319 */     int col = fieldInfoList[0].m_index;
/* 1320 */     Vector v = dset.findRow(col, templateName);
/* 1321 */     if (v != null)
/*      */     {
/* 1323 */       sourceFile = (String)v.elementAt(fieldInfoList[1].m_index);
/*      */     }
/*      */     else
/*      */     {
/* 1328 */       v = dset.findRow(col, "StandardResults");
/*      */ 
/* 1330 */       if (v != null)
/*      */       {
/* 1332 */         sourceFile = (String)v.elementAt(fieldInfoList[1].m_index);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1337 */     return sourceFile;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadDocOptionsList(ResultSet set, DataBinder binder)
/*      */     throws DataException
/*      */   {
/* 1346 */     throw new DataException("loadDocOptionsList() is deprecated.");
/*      */   }
/*      */ 
/*      */   public static void loadTablesToDataBinder(DataBinder data, String[] tables)
/*      */   {
/* 1354 */     if (tables == null)
/*      */     {
/* 1356 */       return;
/*      */     }
/* 1358 */     for (int i = 0; i < tables.length; ++i)
/*      */     {
/* 1360 */       ResultSet rset = SharedObjects.getTable(tables[i]);
/* 1361 */       data.addResultSet(tables[i], rset);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1367 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98095 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DataLoader
 * JD-Core Version:    0.5.4
 */