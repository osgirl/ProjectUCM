/*      */ package intradoc.idcwls;
/*      */ 
/*      */ import intradoc.admin.AdminServerUtils;
/*      */ import intradoc.admin.IdcAdminManager;
/*      */ import intradoc.common.ClassHelper;
/*      */ import intradoc.common.ClassHelperUtils;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.PathUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StackTrace;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.ThreadTraceImplementor;
/*      */ import intradoc.common.TraceUtils;
/*      */ import intradoc.common.TracerReportUtils;
/*      */ import intradoc.common.filter.InputStreamExtension;
/*      */ import intradoc.common.filter.OutputStreamTriggerWrapper;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.loader.IdcClassLoader;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.provider.ProxyConnectionUtils;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.IdcInstallDefaults;
/*      */ import intradoc.server.IdcInstallInfo;
/*      */ import intradoc.server.IdcManagerBase;
/*      */ import intradoc.server.IdcServerThread;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceManager;
/*      */ import intradoc.server.UserStorageUtils;
/*      */ import intradoc.server.utils.MessageLoggerUtils;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.soap.SoapUtils;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.ByteArrayOutputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.io.PrintStream;
/*      */ import java.io.Writer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.ResourceBundle;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class IdcServletRequestUtils
/*      */ {
/*  103 */   public static String[][] m_extraVariablesToSend = { { "server-software", "SERVER_SOFTWARE" }, { "IdcSessionKey", "IDCSESSIONKEY" }, { "IdcCookiePath", "IDCCOOKIEPATH" } };
/*      */ 
/*  110 */   public static String[][] m_authorizationValues = { { "roles", "EXTERNAL_ROLES", "external-roles" }, { "accounts", "EXTERNAL_ACCOUNTS", "external-accounts" }, { "extendedInfo", "EXTERNAL_EXTENDEDUSERINFO", "external-extendeduserinfo" } };
/*      */ 
/*  117 */   public static String[] m_envParamsToCopy = { "UCM_ORACLE_HOME", "BootstrapIntradocCfgPath", "IdcHomeDir" };
/*      */ 
/*  119 */   public static final byte[] HEADER_END_BYTES = "\r\n\r\n".getBytes();
/*  120 */   public static final byte[] UPLOAD_BINARY_END = "$$\n".getBytes();
/*      */ 
/*  122 */   public static String[] m_headersToNotCopyOver = { "internetuser", "authorization", "content-type", "content-length" };
/*      */   public static final String SERVLET_INCOMING_PROVIDER_NAME = "ServletIncomingProvider";
/*      */   public static final String RESULT_SET_KEY = "__IDC_RESULT_SETS";
/*      */   public static final String OPTION_LIST_KEY = "__IDC_OPTION_LISTS";
/*      */   public static final String STREAMS_KEY = "__IDC_STREAMS";
/*      */   public static final int F_PORTAL_PAGE = 1;
/*      */   public static final int F_LOGOUT_PAGE = 2;
/*  140 */   public static boolean m_forceEnabledTracing = false;
/*      */ 
/*  145 */   public static boolean m_developmentBuildIsComputed = false;
/*      */   public static IdcManagerBase m_contentServerInstance;
/*  155 */   public static long[] m_lastReportError = { 0L };
/*      */ 
/*  165 */   public static ServletIncomingProvider m_testMemoryCleanup = null;
/*      */ 
/*  170 */   public static boolean m_extraDebugCode = false;
/*      */ 
/*      */   public static String searchForValue(IdcServletRequestContext request, String key)
/*      */   {
/*  180 */     Object val = request.getLocalParameter(key);
/*  181 */     if (val == null)
/*      */     {
/*  183 */       val = request.getRequestAttribute(key);
/*      */     }
/*  185 */     if (val == null)
/*      */     {
/*  187 */       IdcServletConfig config = request.getServletConfig();
/*  188 */       val = config.getAttribute(key);
/*      */     }
/*  190 */     String response = null;
/*  191 */     if (val != null)
/*      */     {
/*  193 */       response = ScriptUtils.getDisplayString(val, null);
/*      */     }
/*  195 */     return response;
/*      */   }
/*      */ 
/*      */   public static boolean searchForBooleanValue(IdcServletRequestContext request, String key, boolean defVal)
/*      */   {
/*  203 */     Object val = request.getLocalParameter(key);
/*  204 */     if (val == null)
/*      */     {
/*  206 */       val = request.getRequestAttribute(key);
/*      */     }
/*  208 */     if (val == null)
/*      */     {
/*  210 */       IdcServletConfig config = request.getServletConfig();
/*  211 */       val = config.getAttribute(key);
/*      */     }
/*  213 */     boolean response = defVal;
/*  214 */     if (val != null)
/*      */     {
/*  216 */       response = ScriptUtils.convertObjectToBool(val, defVal);
/*      */     }
/*  218 */     return response;
/*      */   }
/*      */ 
/*      */   public static void setBooleanValue(IdcServletRequestContext request, String key, boolean val)
/*      */   {
/*  226 */     request.setLocalParameter(key, (val) ? "1" : "");
/*      */   }
/*      */ 
/*      */   public static boolean getConfigBooleanValue(IdcServletConfig config, String key, boolean defVal)
/*      */   {
/*  234 */     Object val = config.getAttribute(key);
/*  235 */     boolean response = defVal;
/*  236 */     if (val != null)
/*      */     {
/*  238 */       response = ScriptUtils.convertObjectToBool(val, defVal);
/*      */     }
/*  240 */     return response;
/*      */   }
/*      */ 
/*      */   public static final void initializeServer(IdcServletConfig idcServletConfig)
/*      */     throws DataException, ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  254 */       synchronized (Providers.m_registeredProviders)
/*      */       {
/*  258 */         if (Providers.getProvider("ServletIncomingProvider") != null)
/*      */         {
/*  470 */           HashMap propsCopy = new HashMap();
/*  471 */           Map safeEnvProps = SharedObjects.getSafeEnvironment();
/*  472 */           if (safeEnvProps != null)
/*      */           {
/*  474 */             propsCopy.putAll(safeEnvProps);
/*      */           }
/*  476 */           Map secureEnvProps = SharedObjects.getSecureEnvironment();
/*  477 */           if (secureEnvProps != null)
/*      */           {
/*  479 */             propsCopy.putAll(secureEnvProps);
/*      */           }
/*  481 */           idcServletConfig.setAttribute("SharedObjectsEnv", propsCopy);
/*  482 */           return;
/*      */         }
/*  262 */         String serverType = (String)idcServletConfig.getAttribute("IdcServerType");
/*      */ 
/*  267 */         String binDir = (String)idcServletConfig.getAttribute("IdcBinDir");
/*  268 */         EnvUtils.setHostedInAppServer(true);
/*  269 */         if (serverType != null)
/*      */         {
/*  271 */           EnvUtils.setServletApplicationType(serverType);
/*      */         }
/*      */ 
/*  274 */         String appServerType = (String)idcServletConfig.getAttribute("AppServerType");
/*  275 */         EnvUtils.setAppServerType(appServerType);
/*      */ 
/*  277 */         String prodName = (String)idcServletConfig.getAttribute("IdcProductName");
/*  278 */         EnvUtils.setProductName(prodName);
/*      */ 
/*  280 */         Map mbeanProps = (Map)idcServletConfig.getAttribute("DefaultMBeanProps");
/*      */ 
/*  282 */         String prodMode = null;
/*  283 */         if (mbeanProps != null)
/*      */         {
/*  285 */           prodMode = (String)mbeanProps.get("ProductionModeEnabled");
/*      */         }
/*  287 */         if (prodMode != null)
/*      */         {
/*  289 */           EnvUtils.setIsAppServerInProductionMode(StringUtils.convertToBool(prodMode, false));
/*      */         }
/*      */ 
/*  294 */         AdminServerUtils.m_directActionsInterface = new ServletDirectAdminActions();
/*  295 */         AdminServerUtils.m_administratedServerProperties = (Map)idcServletConfig.getAttribute("AdministratedServerProperties");
/*      */ 
/*  298 */         IdcClassLoader idcClassLoader = null;
/*  299 */         if (EnvUtils.class.getClassLoader() instanceof IdcClassLoader)
/*      */         {
/*  301 */           idcClassLoader = (IdcClassLoader)EnvUtils.class.getClassLoader();
/*      */ 
/*  304 */           idcClassLoader.setUseParentForClass("intradoc.common.NativeOsUtilsBase", true);
/*  305 */           idcClassLoader.setUseParentForClass("intradoc.common.PosixStructStat", true);
/*  306 */           idcClassLoader.setUseParentForClass("intradoc.common.StructStatFS", true);
/*      */         }
/*  308 */         SystemUtils.setBinDir(binDir);
/*      */ 
/*  312 */         Properties sysProps = SystemUtils.getSystemPropertiesClone();
/*  313 */         for (String key : m_envParamsToCopy)
/*      */         {
/*  315 */           Object o = idcServletConfig.getAttribute(key);
/*  316 */           if (o == null)
/*      */             continue;
/*  318 */           String val = o.toString();
/*  319 */           sysProps.put(key, val);
/*      */         }
/*      */ 
/*  322 */         Map extraProps = (Map)idcServletConfig.getAttribute("IdcExtraSystemProperties");
/*  323 */         if (extraProps != null)
/*      */         {
/*  325 */           sysProps.putAll(extraProps);
/*      */         }
/*  327 */         if (sysProps.get("OhsHelpContextRoot") == null)
/*      */         {
/*  329 */           sysProps.put("OhsHelpContextRoot", "/_ocsh/help");
/*      */         }
/*  331 */         boolean useIntegratedLogging = true;
/*  332 */         Map stateProps = (Map)idcServletConfig.getAttribute("ServerProps");
/*  333 */         boolean doPartialInit = false;
/*  334 */         if (stateProps != null)
/*      */         {
/*  336 */           m_extraDebugCode = ScriptUtils.convertObjectToBool(stateProps.get("UseIdcServletRequestUtilsExtraDebug"), m_extraDebugCode);
/*      */ 
/*  338 */           if (m_extraDebugCode)
/*      */           {
/*  341 */             System.err.println("--UseIdcServletRequestUtilsExtraDebug activated");
/*      */           }
/*      */ 
/*  346 */           for (String key : stateProps.keySet())
/*      */           {
/*  348 */             String val = (String)stateProps.get(key);
/*      */             String prefix;
/*      */             int index;
/*  349 */             if (key.startsWith("JAVA_OPTIONS"))
/*      */             {
/*  351 */               prefix = "${DEFINE_PREFIX}";
/*  352 */               for (index = val.indexOf(prefix); index >= 0; )
/*      */               {
/*  355 */                 String property = val.substring(index + prefix.length());
/*  356 */                 int equalIndex = property.indexOf("=");
/*  357 */                 if (equalIndex > 0)
/*      */                 {
/*  359 */                   String name = property.substring(0, equalIndex);
/*  360 */                   String value = property.substring(equalIndex + 1);
/*  361 */                   sysProps.put(name, value);
/*  362 */                   if (m_extraDebugCode)
/*      */                   {
/*  364 */                     System.err.println(new StringBuilder().append("Setting JAVA_OPTION property, key=").append(name).append(", val=").append(value).toString());
/*      */                   }
/*      */                 }
/*  353 */                 index = val.indexOf(prefix, index + 1);
/*      */               }
/*      */ 
/*      */             }
/*      */             else
/*      */             {
/*  371 */               sysProps.put(key, val);
/*  372 */               if (m_extraDebugCode)
/*      */               {
/*  374 */                 System.err.println(new StringBuilder().append("Setting property, key=").append(key).append(", val=").append(val).toString());
/*      */               }
/*      */             }
/*      */           }
/*      */ 
/*  379 */           Object val = stateProps.get("UseIntegratedLogging");
/*  380 */           useIntegratedLogging = ScriptUtils.convertObjectToBool(val, useIntegratedLogging);
/*      */ 
/*  382 */           doPartialInit = ScriptUtils.convertObjectToBool(stateProps.get("DoPartialInitialization"), false);
/*      */         }
/*      */ 
/*  387 */         DataBinder providerData = new DataBinder(SharedObjects.getSecureEnvironment());
/*  388 */         providerData.putLocal("IsSystemProvider", "1");
/*  389 */         providerData.putLocal("ProviderName", "ServletIncomingProvider");
/*  390 */         providerData.putLocal("ProviderDescription", "System Servlet Integration");
/*      */ 
/*  392 */         providerData.putLocal("ProviderClass", "intradoc.idcwls.ServletIncomingProvider");
/*  393 */         providerData.putLocal("ProviderType", "incoming");
/*      */ 
/*  395 */         Providers.addProviderData("ServletIncomingProvider", providerData);
/*  396 */         if ((!serverType.equalsIgnoreCase("admin")) && (doPartialInit))
/*      */         {
/*  398 */           m_testMemoryCleanup = new ServletIncomingProvider();
/*  399 */           m_testMemoryCleanup.m_reportOnFinalize = true;
/*      */ 
/*  403 */           ClassHelper idStore = ClassHelperUtils.createClassHelper("oracle.security.jps.util.JpsIdentityStoreUtil");
/*      */ 
/*  406 */           System.err.println("testing->getIdmFactory()");
/*  407 */           idStore.invoke("getIdmFactory");
/*      */ 
/*  409 */           SharedObjects.init();
/*  410 */           SharedObjects.putEnvironmentValue("HttpRelativeWebRoot", "/cs/");
/*  411 */           SharedObjects.putEnvironmentValue("WeblayoutDir", "C:/StellentTrunk/weblayout/");
/*      */ 
/*  414 */           System.err.println("Skipping all other initialization");
/*      */ 
/*  470 */           HashMap propsCopy = new HashMap();
/*  471 */           Map safeEnvProps = SharedObjects.getSafeEnvironment();
/*  472 */           if (safeEnvProps != null)
/*      */           {
/*  474 */             propsCopy.putAll(safeEnvProps);
/*      */           }
/*  476 */           Map secureEnvProps = SharedObjects.getSecureEnvironment();
/*  477 */           if (secureEnvProps != null)
/*      */           {
/*  479 */             propsCopy.putAll(secureEnvProps);
/*      */           }
/*  481 */           idcServletConfig.setAttribute("SharedObjectsEnv", propsCopy);
/*  482 */           return;
/*      */         }
/*  418 */         if (prodName == null)
/*      */         {
/*  420 */           prodName = "idccs";
/*      */         }
/*      */ 
/*  423 */         System.err.println(new StringBuilder().append("***Starting IDC ").append(serverType).append(" on BinDir ").append(binDir).toString());
/*  424 */         fixupAndSetInstallDefaults(idcServletConfig);
/*  425 */         fixupAndSetClasspath(prodName, sysProps, idcClassLoader, idcServletConfig);
/*      */         try
/*      */         {
/*  430 */           if (useIntegratedLogging)
/*      */           {
/*  432 */             MessageLoggerUtils.m_loggerName = new StringBuilder().append(MessageLoggerUtils.m_loggerNamePrefix).append(".").append(prodName).toString();
/*  433 */             Map map = TracerReportUtils.createTracerSettingsMap("intradoc.idcwls.ServletLogger");
/*  434 */             TracerReportUtils.addTraceImplementor("idcservlet", map, null);
/*  435 */             boolean integratedLoggingGoesToConsole = getConfigBooleanValue(idcServletConfig, "servlet-logs-to-console", false);
/*      */ 
/*  437 */             if (integratedLoggingGoesToConsole)
/*      */             {
/*  439 */               TracerReportUtils.setDefaultTraceToConsole(false);
/*      */             }
/*      */ 
/*  442 */             intradoc.util.IdcLoggerUtils.m_wrappedLogResourceName = "intradoc.idcwls.IdcResourceBundle";
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  453 */           TracerReportUtils.setDefaultTraceToConsole(true);
/*  454 */           Report.error(null, e, "csServletFilterLogInitError", new Object[0]);
/*      */         }
/*      */ 
/*  457 */         initializeServer(serverType, prodName, idcServletConfig);
/*  458 */         prepareServletIntegration(serverType, prodName, idcServletConfig);
/*  459 */         if (IdcServletStaticEnv.m_forceSystemConfigPage)
/*      */         {
/*  461 */           Report.trace("system", new StringBuilder().append("Idc ").append(serverType).append(" environment is not yet fully configured, waiting for additional configuration").toString(), null);
/*      */         }
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*      */       HashMap propsCopy;
/*      */       Map safeEnvProps;
/*      */       Map secureEnvProps;
/*  470 */       HashMap propsCopy = new HashMap();
/*  471 */       Map safeEnvProps = SharedObjects.getSafeEnvironment();
/*  472 */       if (safeEnvProps != null)
/*      */       {
/*  474 */         propsCopy.putAll(safeEnvProps);
/*      */       }
/*  476 */       Map secureEnvProps = SharedObjects.getSecureEnvironment();
/*  477 */       if (secureEnvProps != null)
/*      */       {
/*  479 */         propsCopy.putAll(secureEnvProps);
/*      */       }
/*  481 */       idcServletConfig.setAttribute("SharedObjectsEnv", propsCopy);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void fixupAndSetClasspath(String productName, Map systemProps, IdcClassLoader classLoader, IdcServletConfig idcServletConfig)
/*      */     throws ServiceException
/*      */   {
/*  488 */     Map serverProps = (Map)idcServletConfig.getAttribute("ServerProps");
/*  489 */     ArrayList classKeyList = new ArrayList();
/*  490 */     ArrayList accumulatedErrors = new ArrayList();
/*      */ 
/*  492 */     if ((classLoader == null) || (serverProps == null))
/*      */     {
/*  494 */       return;
/*      */     }
/*      */ 
/*  497 */     classKeyList.add(new StringBuilder().append("IdcServerServletClassPath_").append(productName).toString());
/*      */ 
/*  501 */     for (String key : serverProps.keySet())
/*      */     {
/*  503 */       if (key.startsWith("JAVA_CLASSPATH"))
/*      */       {
/*  505 */         classKeyList.add(key);
/*      */       }
/*      */     }
/*      */ 
/*  509 */     for (String key : classKeyList)
/*      */     {
/*  511 */       String pathElem = (String)serverProps.get(key);
/*  512 */       if (pathElem == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  516 */       String computedClassPath = null;
/*      */       try
/*      */       {
/*  519 */         computedClassPath = PathUtils.substitutePathVariables(pathElem, systemProps, null, PathUtils.F_VARS_MUST_EXIST, null);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  526 */         ServiceException newe = new ServiceException(e, "csServletUnableToEvaluatePathForKey", new Object[] { key });
/*      */ 
/*  528 */         accumulatedErrors.add(newe);
/*  529 */       }continue;
/*      */ 
/*  532 */       computedClassPath = computedClassPath.replace(';', ':');
/*  533 */       while (computedClassPath != null)
/*      */       {
/*  536 */         int index = computedClassPath.indexOf(58, 2);
/*      */         String path;
/*  537 */         if (index >= 0)
/*      */         {
/*  539 */           String path = computedClassPath.substring(0, index);
/*  540 */           computedClassPath = computedClassPath.substring(index + 1);
/*      */         }
/*      */         else
/*      */         {
/*  544 */           path = computedClassPath;
/*  545 */           computedClassPath = null;
/*      */         }
/*  547 */         if (!FileUtils.checkPathExists(path))
/*      */         {
/*  549 */           if (!m_extraDebugCode)
/*      */             continue;
/*  551 */           System.err.println(new StringBuilder().append("Could not find bootup classpath ").append(path).toString());
/*      */         }
/*      */ 
/*  555 */         if (m_extraDebugCode)
/*      */         {
/*  557 */           System.err.println(new StringBuilder().append("Added bootup classpath ").append(path).toString());
/*      */         }
/*      */         try
/*      */         {
/*  561 */           classLoader.addClassPathElement(path, 50);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  565 */           ServiceException newe = new ServiceException(e, "csServletUnableToAddClasspath", new Object[] { path });
/*      */ 
/*  567 */           accumulatedErrors.add(newe);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  573 */     for (ServiceException msg : accumulatedErrors)
/*      */     {
/*  575 */       Report.error("servlet", msg, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void initializeServer(String serverType, String productName, IdcServletConfig idcServletConfig)
/*      */     throws DataException, ServiceException
/*      */   {
/*  582 */     IdcManagerBase managerBase = null;
/*  583 */     if (serverType.equalsIgnoreCase("admin"))
/*      */     {
/*  585 */       managerBase = new IdcAdminManager();
/*      */     }
/*      */     else
/*      */     {
/*  590 */       String servletManagerClassName = (String)idcServletConfig.getAttribute("IdcManagerClass");
/*      */       String managerClassName;
/*      */       String managerClassName;
/*  591 */       if ((servletManagerClassName != null) && (servletManagerClassName.length() > 0))
/*      */       {
/*  593 */         managerClassName = servletManagerClassName;
/*      */       }
/*      */       else
/*      */       {
/*  598 */         managerClassName = AdminServerUtils.determineDefaultStartupClass(productName);
/*      */       }
/*      */       try
/*      */       {
/*  602 */         Class managerClass = Class.forName(managerClassName);
/*  603 */         managerBase = (IdcManagerBase)managerClass.newInstance();
/*      */       }
/*      */       catch (ClassNotFoundException e)
/*      */       {
/*  607 */         throw new ServiceException(e);
/*      */       }
/*      */       catch (IllegalAccessException e)
/*      */       {
/*  611 */         throw new ServiceException(e);
/*      */       }
/*      */       catch (InstantiationException e)
/*      */       {
/*  615 */         throw new ServiceException(e);
/*      */       }
/*  617 */       if (m_testMemoryCleanup != null)
/*      */       {
/*  619 */         intradoc.server.IdcSystemConfig.m_isTestStartup = true;
/*      */       }
/*  621 */       IdcInstallDefaults.m_allowFullNewInstall = true;
/*      */     }
/*      */ 
/*  624 */     if (m_testMemoryCleanup == null)
/*      */     {
/*  626 */       managerBase.init();
/*  627 */       prepareWorkManager(managerBase, idcServletConfig);
/*  628 */       IdcServletStaticEnv.init(idcServletConfig, serverType);
/*  629 */       IdcServletAuthUtils.init();
/*      */ 
/*  632 */       managerBase.serviceStart(0, 0);
/*      */     }
/*  634 */     m_contentServerInstance = managerBase;
/*      */   }
/*      */ 
/*      */   public static void prepareWorkManager(IdcManagerBase managerBase, IdcServletConfig idcServletConfig)
/*      */   {
/*      */     try
/*      */     {
/*  641 */       String workManagerKey = WorkManagerUtils.getDefaultWorkManagerKey(idcServletConfig);
/*  642 */       Object workManager = WorkManagerUtils.getWorkManager(workManagerKey, idcServletConfig);
/*  643 */       AppServerThreadManager threadManager = new AppServerThreadManager(workManager);
/*  644 */       SystemUtils.m_systemClientThreadScheduler = threadManager;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  648 */       Report.warning("servlet", e, "csServletCouldNotPrepareWorkManager", new Object[0]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void stopServer(IdcServletConfig idcServletConfig) throws DataException, ServiceException
/*      */   {
/*  654 */     if (m_contentServerInstance == null)
/*      */       return;
/*  656 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/*  657 */     cxt.setCachedObject("IdcServletConfig", idcServletConfig);
/*      */ 
/*  662 */     SystemUtils.markServerAsStopped();
/*      */ 
/*  664 */     String type = "servletStopServer";
/*      */     try
/*      */     {
/*  667 */       DataBinder binder = new DataBinder(SharedObjects.getSafeEnvironment());
/*  668 */       PluginFilters.filter(type, IdcServletStaticEnv.m_systemWorkspace, binder, cxt);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  672 */       String msg = LocaleUtils.encodeMessage("csServletFilterException", null, type);
/*  673 */       Report.error("servlet", msg, e);
/*      */     }
/*      */ 
/*  676 */     m_contentServerInstance.stopAndClearAllProviders();
/*  677 */     m_contentServerInstance = null;
/*  678 */     ClassHelperUtils.clearMethodCache();
/*  679 */     Report.trace("system", new StringBuilder().append("Server ").append(IdcServletStaticEnv.m_servletAppID).append(" has been stopped by request from containing application server").toString(), null);
/*      */ 
/*  681 */     ResourceBundle.clearCache(SharedObjects.class.getClassLoader());
/*      */   }
/*      */ 
/*      */   public static void fixupAndSetInstallDefaults(IdcServletConfig idcServletConfig)
/*      */   {
/*  687 */     Map installDefaults = (Map)idcServletConfig.getAttribute("DefaultInstallParameters");
/*  688 */     Map mbeanProps = (Map)idcServletConfig.getAttribute("DefaultMBeanProps");
/*  689 */     if ((installDefaults == null) || (mbeanProps == null))
/*      */     {
/*  691 */       return;
/*      */     }
/*      */ 
/*  694 */     String httpServerAddress = (String)installDefaults.get("HttpServerAddress");
/*  695 */     if (httpServerAddress == null)
/*      */     {
/*  698 */       installDefaults.put("WebServer", "javaAppServer");
/*      */ 
/*  701 */       String port = (String)mbeanProps.get("ServerPort");
/*  702 */       boolean useSSL = StringUtils.convertToBool((String)mbeanProps.get("UseSSL"), false);
/*  703 */       boolean appendPort = (port != null) && (port.length() > 0) && (!port.equals("0")) && (((!useSSL) || (!port.equals("443")))) && (((useSSL) || (!port.equals("80"))));
/*      */ 
/*  705 */       String serverHostName = (String)mbeanProps.get("ServerHostName");
/*  706 */       int colonIndex = -1;
/*  707 */       if (serverHostName == null)
/*      */       {
/*  709 */         serverHostName = "localhost";
/*      */       }
/*  711 */       else if ((colonIndex = serverHostName.indexOf(":")) > 0)
/*      */       {
/*  713 */         if (serverHostName.indexOf(":", colonIndex + 1) < 0)
/*      */         {
/*  715 */           appendPort = false;
/*      */         }
/*  720 */         else if (!serverHostName.startsWith("["))
/*      */         {
/*  722 */           serverHostName = new StringBuilder().append("[").append(serverHostName).append("]").toString();
/*      */         }
/*      */       }
/*      */ 
/*  726 */       String httpAddress = serverHostName;
/*  727 */       if (appendPort)
/*      */       {
/*  729 */         httpAddress = new StringBuilder().append(httpAddress).append(":").append(port).toString();
/*      */       }
/*  731 */       installDefaults.put("HttpServerAddress", httpAddress);
/*      */ 
/*  734 */       IdcStringBuilder out = new IdcStringBuilder(httpAddress.length());
/*  735 */       int index = 0;
/*  736 */       while (index < httpAddress.length())
/*      */       {
/*  738 */         char ch = httpAddress.charAt(index);
/*  739 */         if (Character.isLetterOrDigit(ch))
/*      */         {
/*  741 */           out.append(ch);
/*      */         }
/*  743 */         ++index;
/*      */       }
/*  745 */       String instanceName = out.toString();
/*  746 */       if (instanceName.length() > 30)
/*      */       {
/*  748 */         String prefix = instanceName.substring(0, 10);
/*  749 */         String end = instanceName.substring(instanceName.length() - 10);
/*  750 */         long hashCode = instanceName.hashCode();
/*  751 */         int middleCode = (int)(hashCode % 1000000L);
/*  752 */         if (middleCode < 0)
/*      */         {
/*  754 */           middleCode = -middleCode;
/*      */         }
/*  756 */         instanceName = new StringBuilder().append(prefix).append(middleCode).append(end).toString();
/*      */       }
/*  758 */       if (instanceName.equals("localhost"))
/*      */       {
/*  760 */         instanceName = "change_me";
/*      */       }
/*  762 */       installDefaults.put("IDC_Name", instanceName);
/*  763 */       String serverName = (String)mbeanProps.get("ServerRuntimeName");
/*  764 */       if (serverName != null)
/*      */       {
/*  768 */         installDefaults.put("IDC_Id", serverName);
/*      */       }
/*      */ 
/*  772 */       String[] keysToTransfer = IdcInstallInfo.CAPTURED_APP_SERVER_PROPERTIES;
/*  773 */       for (String key : keysToTransfer)
/*      */       {
/*  775 */         String val = (String)mbeanProps.get(key);
/*  776 */         if ((val != null) && (((key.endsWith("Dir")) || (key.endsWith("Home")))))
/*      */         {
/*  779 */           val = FileUtils.directorySlashes(val);
/*      */         }
/*  781 */         if (val == null)
/*      */           continue;
/*  783 */         installDefaults.put(key, val);
/*      */       }
/*      */ 
/*  787 */       if (m_developmentBuildIsComputed)
/*      */       {
/*  792 */         installDefaults.put("DisableErrorPageStackTrace", "true");
/*      */       }
/*      */ 
/*  796 */       IdcInstallDefaults.m_defaults = installDefaults;
/*  797 */       IdcInstallDefaults.m_checkForNewInstall = true;
/*      */     }
/*      */ 
/*  800 */     Map extraConfig = (Map)idcServletConfig.getAttribute("ExtraInstallConfigValues");
/*  801 */     if (extraConfig == null)
/*      */       return;
/*  803 */     if (IdcInstallDefaults.m_extraConfigFileValues == null)
/*      */     {
/*  805 */       IdcInstallDefaults.m_extraConfigFileValues = extraConfig;
/*      */     }
/*      */     else
/*      */     {
/*  809 */       IdcInstallDefaults.m_extraConfigFileValues.putAll(extraConfig);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void prepareServletIntegration(String serverType, String prodName, IdcServletConfig idcServletConfig)
/*      */   {
/*  823 */     if (!serverType.equals("admin"))
/*      */       return;
/*  825 */     DataBinder binder = new DataBinder(SharedObjects.getSafeEnvironment());
/*  826 */     String isoEnc = DataSerializeUtils.getIsoEncoding(DataSerializeUtils.getSystemEncoding());
/*      */ 
/*  828 */     if (isoEnc != null)
/*      */     {
/*  830 */       binder.putLocal("charset", isoEnc);
/*      */     }
/*  832 */     PageMerger pm = new PageMerger(binder, null);
/*      */ 
/*  836 */     String standardMsg = getTranslatedProcessedString("csWebFilterStandardMsg", binder, pm);
/*  837 */     boolean doIt = standardMsg != null;
/*  838 */     int headersEnd = -1;
/*  839 */     if (doIt)
/*      */     {
/*  841 */       headersEnd = standardMsg.indexOf("\r\n\r\n");
/*  842 */       doIt = headersEnd > 0;
/*      */     }
/*  844 */     String standardPageContent = null;
/*  845 */     String unavailableStatusMsg = null;
/*  846 */     String messageTitle = null;
/*  847 */     if (doIt)
/*      */     {
/*  849 */       standardPageContent = standardMsg.substring(headersEnd + 4);
/*  850 */       unavailableStatusMsg = getTranslatedProcessedString("csWebFilterServerUnavailableUserMsg", binder, pm);
/*  851 */       messageTitle = getTranslatedProcessedString("csWebFilterIdcMessageTitle", binder, pm);
/*  852 */       doIt = (unavailableStatusMsg != null) && (messageTitle != null);
/*      */     }
/*      */ 
/*  855 */     if (!doIt) {
/*      */       return;
/*      */     }
/*  858 */     unavailableStatusMsg = substitutePercentStringParams(unavailableStatusMsg, null);
/*      */ 
/*  861 */     String adminUrlMsg = LocaleResources.getString("csWebFilterAdminUrlMsg", null);
/*  862 */     String cgiPath = DirectoryLocator.getCgiWebUrl(false);
/*  863 */     String[] msgArgs = { unavailableStatusMsg, cgiPath };
/*  864 */     adminUrlMsg = substitutePercentStringParams(adminUrlMsg, msgArgs);
/*  865 */     String[] pageArgs = { messageTitle, adminUrlMsg, adminUrlMsg };
/*  866 */     String pageContent = substitutePercentStringParams(standardPageContent, pageArgs);
/*  867 */     idcServletConfig.setAttribute("ServerUnavailableMessage", pageContent);
/*      */ 
/*  869 */     String charset = SharedObjects.getEnvironmentValue("PageCharset");
/*  870 */     if ((charset == null) || (charset.length() == 0))
/*      */     {
/*  872 */       charset = FileUtils.m_javaSystemEncoding;
/*      */     }
/*  874 */     if ((charset == null) || (charset.length() <= 0))
/*      */       return;
/*  876 */     idcServletConfig.setAttribute("PageCharset", charset);
/*      */   }
/*      */ 
/*      */   public static String getTranslatedProcessedString(String key, DataBinder binder, PageMerger pm)
/*      */   {
/*  884 */     String val = LocaleResources.getString(key, null);
/*  885 */     if ((val == null) || (val.equals(key)))
/*      */     {
/*  887 */       return null;
/*      */     }
/*      */     try
/*      */     {
/*  891 */       val = pm.evaluateScript(val);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  895 */       Report.trace("servlet", new StringBuilder().append("Failed to process key ").append(key).toString(), e);
/*  896 */       val = null;
/*      */     }
/*  898 */     return val;
/*      */   }
/*      */ 
/*      */   public static String substitutePercentStringParams(String val, String[] args)
/*      */   {
/*  908 */     IdcStringBuilder result = new IdcStringBuilder(val.length() + 100);
/*  909 */     int index = val.indexOf("%s");
/*  910 */     int prevIndex = 0;
/*  911 */     int argCount = 0;
/*  912 */     while (index >= 0)
/*      */     {
/*  914 */       if (index > prevIndex)
/*      */       {
/*  916 */         result.append(val, prevIndex, index - prevIndex);
/*      */       }
/*  918 */       prevIndex = index + 2;
/*  919 */       if ((args != null) && (argCount < args.length))
/*      */       {
/*  921 */         String arg = args[argCount];
/*  922 */         if (arg == null)
/*      */         {
/*  924 */           arg = "";
/*      */         }
/*  926 */         result.append(arg);
/*      */       }
/*  928 */       ++argCount;
/*  929 */       index = val.indexOf("%s", index + 1);
/*      */     }
/*  931 */     if (prevIndex < val.length())
/*      */     {
/*  933 */       result.append(val, prevIndex, val.length() - prevIndex);
/*      */     }
/*  935 */     return result.toString();
/*      */   }
/*      */ 
/*      */   public static void initializeRequest(IdcServletRequestContext request)
/*      */     throws IOException
/*      */   {
/*  950 */     ServletActiveLocalData activeData = request.getActiveData();
/*  951 */     ByteArrayOutputStream outHeadersStream = new ByteArrayOutputStream();
/*  952 */     Writer w = FileUtils.openDataWriterEx(outHeadersStream, null, 1);
/*  953 */     boolean isBinary = false;
/*  954 */     byte[] tempBuf = null;
/*  955 */     byte[] earlyBody = null;
/*  956 */     InputStream bodyIn = null;
/*      */ 
/*  958 */     if (m_forceEnabledTracing)
/*      */     {
/*  960 */       Report.m_verbose = true;
/*  961 */       SystemUtils.m_verbose = true;
/*  962 */       Vector v = new Vector();
/*  963 */       v.add("system");
/*  964 */       v.add("servlet");
/*  965 */       v.add("services");
/*  966 */       SystemUtils.setActiveTraces(v);
/*      */     }
/*      */ 
/*  970 */     String method = (activeData.m_showLoginForm) ? "GET" : activeData.m_method;
/*  971 */     appendHeaderNameValue(w, "REQUEST_METHOD", method);
/*      */ 
/*  974 */     if (activeData.m_uriPath != null)
/*      */     {
/*  976 */       appendHeaderNameValue(w, "URI_PATH", activeData.m_uriPath);
/*      */     }
/*      */ 
/*  980 */     appendHeaderNameValue(w, "IsServletRequest", "1");
/*      */ 
/*  983 */     appendHeaderNameValue(w, "IDC_REQUEST_AGENT", "webserver");
/*      */ 
/*  988 */     appendHeaderNameValue(w, "IdcAuthChallengeType", "http");
/*      */ 
/*  991 */     String isHttps = (String)request.getRequestAttribute("ishttps");
/*  992 */     if (isHttps != null)
/*      */     {
/*  994 */       appendHeaderNameValue(w, "IS_HTTPS", isHttps);
/*      */     }
/*      */ 
/*  999 */     appendHeaderNameValue(w, "SERVER_PROTOCOL_TYPE", "NONE");
/*      */ 
/* 1002 */     appendHeaderNameValue(w, "SERVER_PROTOCOL", "HTTP");
/*      */ 
/* 1005 */     if (activeData.m_showLoginForm)
/*      */     {
/* 1011 */       Object authTargetUrl = request.getSessionAttribute("success_url");
/* 1012 */       if (authTargetUrl != null)
/*      */       {
/* 1014 */         String url = authTargetUrl.toString();
/* 1015 */         int index = url.indexOf(63);
/* 1016 */         if (index >= 0)
/*      */         {
/* 1018 */           url = url.substring(0, index);
/*      */         }
/* 1020 */         appendHeaderNameValue(w, "AUTH_TARGET_URL", url);
/*      */       }
/* 1022 */       Object exceptionObj = request.getLocalParameter("authException");
/* 1023 */       if (exceptionObj != null)
/*      */       {
/* 1026 */         ExecutionContext parentContext = request.getParentExecutionContext();
/* 1027 */         parentContext.setCachedObject("PriorAuthenticationException", exceptionObj);
/* 1028 */         parentContext.setCachedObject("PriorAuthenticationFailed", Boolean.TRUE);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1034 */     String serverPort = (String)request.getRequestAttribute("serverport");
/* 1035 */     if (serverPort != null)
/*      */     {
/* 1037 */       appendHeaderNameValue(w, "SERVER_PORT", serverPort);
/*      */     }
/*      */ 
/* 1041 */     String remoteServer = (String)request.getRequestAttribute("remoteip");
/* 1042 */     if (remoteServer != null)
/*      */     {
/* 1044 */       appendHeaderNameValue(w, "REMOTE_ADDR", remoteServer);
/*      */     }
/*      */ 
/* 1047 */     String serverName = (String)request.getRequestAttribute("servername");
/* 1048 */     if (serverName != null)
/*      */     {
/* 1050 */       appendHeaderNameValue(w, "SERVER_NAME", serverName);
/*      */     }
/*      */ 
/* 1056 */     String contentType = request.getRequestHeader("content-type");
/* 1057 */     contentType = (activeData.m_showLoginForm) ? null : contentType;
/* 1058 */     if (contentType != null)
/*      */     {
/* 1060 */       String contentTypeLwr = contentType.toLowerCase();
/* 1061 */       if ((contentTypeLwr.contains("multipart")) || (contentTypeLwr.contains("unknown")))
/*      */       {
/* 1063 */         request.setIsBinaryPost(true);
/* 1064 */         isBinary = true;
/*      */       }
/* 1066 */       appendHeaderNameValue(w, "CONTENT_TYPE", contentType);
/*      */     }
/* 1068 */     if (activeData.m_query != null)
/*      */     {
/* 1070 */       appendHeaderNameValue(w, "QUERY_STRING", activeData.m_query);
/*      */     }
/* 1072 */     if (activeData.m_authUser != null)
/*      */     {
/* 1074 */       appendHeaderNameValue(w, "REMOTE_USER", activeData.m_authUser);
/*      */     }
/*      */ 
/* 1077 */     if (activeData.m_isSessionLogin)
/*      */     {
/* 1079 */       appendHeaderNameValue(w, "IsCookieLoggedIn", "1");
/*      */     }
/*      */ 
/* 1082 */     String contentLenStr = request.getRequestHeader("content-length");
/* 1083 */     if (contentLenStr != null)
/*      */     {
/* 1085 */       activeData.m_contentLength = NumberUtils.parseLong(contentLenStr, 0L);
/*      */     }
/*      */ 
/* 1089 */     if ((activeData.m_method.equalsIgnoreCase("POST")) && (!isBinary))
/*      */     {
/* 1092 */       InputStream testStream = request.getServletInputStream();
/* 1093 */       tempBuf = (byte[])(byte[])FileUtils.createBufferForStreaming(0, 0);
/* 1094 */       int numBytesRead = testStream.read(tempBuf);
/* 1095 */       if (numBytesRead > 0)
/*      */       {
/* 1098 */         earlyBody = new byte[numBytesRead];
/* 1099 */         System.arraycopy(tempBuf, 0, earlyBody, 0, earlyBody.length);
/* 1100 */         bodyIn = testStream;
/*      */       }
/* 1104 */       else if (activeData.m_contentLength > 0L)
/*      */       {
/* 1108 */         Map requestParameters = request.getCopyRequestParameters();
/* 1109 */         IdcStringBuilder paramsBuf = new IdcStringBuilder(2000);
/* 1110 */         Set s = requestParameters.keySet();
/* 1111 */         boolean appendedOne = false;
/* 1112 */         for (String key : s)
/*      */         {
/* 1114 */           String[] vals = (String[])requestParameters.get(key);
/* 1115 */           if (vals != null)
/*      */           {
/* 1117 */             for (String val : vals)
/*      */             {
/* 1119 */               if (appendedOne)
/*      */               {
/* 1121 */                 paramsBuf.append('&');
/*      */               }
/* 1123 */               paramsBuf.append(key).append('=');
/* 1124 */               if ((val != null) && (val.length() > 0))
/*      */               {
/* 1126 */                 String encodedVal = StringUtils.encodeUrlStyle(val, '%', true);
/* 1127 */                 paramsBuf.append(encodedVal);
/*      */               }
/* 1129 */               appendedOne = true;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1136 */         earlyBody = paramsBuf.toString().getBytes(FileUtils.m_javaSystemEncoding);
/* 1137 */         activeData.m_contentLength = earlyBody.length;
/*      */       }
/*      */ 
/* 1140 */       FileUtils.releaseBufferForStreaming(tempBuf);
/*      */     }
/*      */     else
/*      */     {
/* 1145 */       bodyIn = request.getServletInputStream();
/*      */     }
/* 1147 */     if (activeData.m_contentLength > 0L)
/*      */     {
/* 1149 */       appendHeaderNameValue(w, "CONTENT_LENGTH", new StringBuilder().append("").append(activeData.m_contentLength).toString());
/*      */     }
/*      */ 
/* 1152 */     if (activeData.m_additionalHeaders != null)
/*      */     {
/* 1154 */       for (String[] headerRow : activeData.m_additionalHeaders)
/*      */       {
/* 1156 */         appendHeaderNameValue(w, headerRow[0], headerRow[1]);
/*      */       }
/*      */     }
/*      */ 
/* 1160 */     for (int i = 0; i < m_extraVariablesToSend.length; ++i)
/*      */     {
/* 1162 */       String key = m_extraVariablesToSend[i][0];
/* 1163 */       String val = searchForValue(request, key);
/* 1164 */       if (val == null)
/*      */       {
/* 1166 */         val = request.getRequestHeader(key);
/*      */       }
/* 1168 */       if (val == null)
/*      */         continue;
/* 1170 */       appendHeaderNameValue(w, m_extraVariablesToSend[i][1], val);
/*      */     }
/*      */ 
/* 1174 */     if (request.isHttpRequest())
/*      */     {
/* 1176 */       Map headers = request.getCopyRequestHeaders();
/* 1177 */       IdcStringBuilder strBuilder = null;
/* 1178 */       if (activeData.m_serverRequestHeaders == null)
/*      */       {
/* 1180 */         strBuilder = new IdcStringBuilder();
/* 1181 */         activeData.m_serverRequestHeaders = strBuilder;
/*      */       }
/*      */       else
/*      */       {
/* 1185 */         strBuilder = activeData.m_serverRequestHeaders;
/*      */       }
/* 1187 */       strBuilder.m_disableToStringReleaseBuffers = true;
/* 1188 */       executeServerFilter(request, "handleRequest");
/* 1189 */       for (String header : headers.keySet())
/*      */       {
/* 1194 */         String headerVal = (String)headers.get(header);
/* 1195 */         if ((headerVal != null) && (headerVal.length() > 0) && (StringUtils.findConfigValueCaseInsensitiveInList(header, m_headersToNotCopyOver) < 0) && (!header.startsWith("external-")))
/*      */         {
/* 1201 */           boolean isAppHeader = header.startsWith("idc");
/*      */ 
/* 1210 */           int start = 0;
/* 1211 */           if (!isAppHeader)
/*      */           {
/* 1213 */             start = 5;
/* 1214 */             strBuilder.append("HTTP_");
/*      */           }
/* 1216 */           strBuilder.append(header);
/* 1217 */           for (int i = start; i < strBuilder.m_length; ++i)
/*      */           {
/* 1219 */             char ch = strBuilder.m_charArray[i];
/* 1220 */             if (ch == '-')
/*      */             {
/* 1222 */               if (isAppHeader)
/*      */                 continue;
/* 1224 */               strBuilder.m_charArray[i] = '_';
/*      */             }
/*      */             else {
/* 1227 */               if ((ch < 'a') || (ch > 'z'))
/*      */                 continue;
/* 1229 */               strBuilder.m_charArray[i] = (char)(ch - 'a' + 65);
/*      */             }
/*      */           }
/* 1232 */           strBuilder.append('=');
/* 1233 */           strBuilder.append(headerVal);
/* 1234 */           strBuilder.append("\r\n");
/* 1235 */           w.append(strBuilder);
/*      */ 
/* 1238 */           strBuilder.setLength(0);
/*      */         }
/*      */       }
/* 1241 */       strBuilder.releaseBuffers();
/*      */     }
/* 1243 */     w.append("$$$$\n");
/* 1244 */     w.flush();
/*      */ 
/* 1249 */     byte[] headerInBytes = outHeadersStream.toByteArray();
/* 1250 */     if ((Report.m_verbose) && (SystemUtils.isActiveTrace("servlet")))
/*      */     {
/* 1252 */       String sessionKeysToDump = "weblogic.formauth.targeturl,javax.servlet.error.exception";
/* 1253 */       String temp = SharedObjects.getEnvironmentValue("HttpSessionKeysToDump");
/* 1254 */       if (temp != null)
/*      */       {
/* 1256 */         sessionKeysToDump = temp;
/*      */       }
/* 1258 */       List keys = StringUtils.makeListFromSequenceSimple(sessionKeysToDump);
/* 1259 */       List outSessionAttributes = new ArrayList();
/* 1260 */       for (String key : keys)
/*      */       {
/* 1262 */         Object sessionObj = request.getSessionAttribute(key);
/* 1263 */         if (sessionObj == null)
/*      */         {
/* 1265 */           sessionObj = "<not-found>";
/*      */         }
/* 1267 */         Object[] keyPair = { key, sessionObj };
/* 1268 */         outSessionAttributes.add(keyPair);
/*      */       }
/* 1270 */       IdcStringBuilder strBuilder = new IdcStringBuilder();
/* 1271 */       StringUtils.appendForDebug(strBuilder, outSessionAttributes, 0);
/*      */ 
/* 1273 */       Report.debug("servlet", "Dump of HttpSession attributes", null);
/* 1274 */       Report.debug("servlet", new StringBuilder().append("").append(strBuilder).toString(), null);
/* 1275 */       String s = new String(headerInBytes);
/* 1276 */       Report.debug("servlet", "Headers being sent to server", null);
/* 1277 */       Report.debug("servlet", s, null);
/*      */     }
/*      */ 
/* 1282 */     String buffersizeStr = searchForValue(request, "buffersize");
/* 1283 */     int maxHeadersSize = NumberUtils.parseInteger(buffersizeStr, 500000);
/* 1284 */     request.setLocalParameter("buffersize", new StringBuilder().append("").append(maxHeadersSize).toString());
/*      */ 
/* 1287 */     InputStream headerIn = new ByteArrayInputStream(headerInBytes);
/* 1288 */     InputStream earlyBodyIn = null;
/* 1289 */     if (earlyBody != null)
/*      */     {
/* 1291 */       earlyBodyIn = new ByteArrayInputStream(earlyBody);
/*      */     }
/*      */ 
/* 1294 */     InputStream tailIn = null;
/*      */ 
/* 1296 */     if (request.isBinaryPost())
/*      */     {
/* 1298 */       tailIn = new ByteArrayInputStream(UPLOAD_BINARY_END);
/*      */     }
/* 1300 */     InputStream[] inStreamsArray = { headerIn, earlyBodyIn, bodyIn, tailIn };
/* 1301 */     InputStreamExtension inStreams = new InputStreamExtension(inStreamsArray);
/* 1302 */     ServletOutStreamCallback outStreamCallback = new ServletOutStreamCallback(request);
/* 1303 */     OutputStreamTriggerWrapper outStream = new OutputStreamTriggerWrapper(HEADER_END_BYTES, outStreamCallback, request.getLocalParameters());
/*      */ 
/* 1305 */     request.setManufacturedInputStream(inStreams);
/* 1306 */     request.setManufacturedOutputStream(outStream);
/*      */   }
/*      */ 
/*      */   public static void doRequest(IdcServletRequestContext request, DirectRequestData directData)
/*      */     throws IOException
/*      */   {
/* 1322 */     Provider servletProvider = Providers.getProvider("ServletIncomingProvider");
/* 1323 */     if (servletProvider == null)
/*      */     {
/* 1325 */       throw new IOException("!csServletProviderNotCreated");
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1331 */       if (ThreadTraceImplementor.m_isInitialized)
/*      */       {
/* 1333 */         ThreadTraceImplementor.resetTracingBuilder();
/*      */       }
/*      */ 
/* 1338 */       ServletIncomingConnection conn = new ServletIncomingConnection();
/* 1339 */       conn.init(request, directData);
/* 1340 */       DataBinder providerData = servletProvider.getProviderData();
/* 1341 */       conn.setProviderData(providerData);
/* 1342 */       String threadName = Thread.currentThread().getName();
/* 1343 */       ExecutionContext parentContext = null;
/* 1344 */       if (request != null)
/*      */       {
/* 1346 */         parentContext = request.getParentExecutionContext();
/*      */       }
/* 1348 */       IdcServerThread execObject = new IdcServerThread(threadName, parentContext);
/* 1349 */       if ((directData != null) && (directData.m_isDirect))
/*      */       {
/* 1351 */         execObject.setUpfrontBinder(directData.m_binder, directData.m_in == null, directData.m_tracingSection);
/*      */       }
/*      */ 
/* 1354 */       execObject.init(servletProvider, conn);
/* 1355 */       execObject.processRequest();
/*      */ 
/* 1358 */       if (request != null)
/*      */       {
/* 1360 */         DataBinder requestBinder = execObject.getResponseDataBinder();
/* 1361 */         if (requestBinder != null)
/*      */         {
/* 1363 */           request.setLocalParameter("RequestDataBinder", requestBinder);
/*      */ 
/* 1366 */           Properties props = requestBinder.getLocalData();
/* 1367 */           request.setLocalParameter("RequestDataBinderLocalProperties", props);
/*      */         }
/*      */ 
/* 1370 */         UserData requestUserData = execObject.getCapturedUserData();
/* 1371 */         if (requestUserData != null)
/*      */         {
/* 1373 */           request.setLocalParameter("RequestUserData", requestUserData);
/* 1374 */           Properties userProps = requestUserData.getProperties();
/* 1375 */           if (userProps != null)
/*      */           {
/* 1377 */             request.setLocalParameter("RequestUserDataProperties", userProps);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1384 */       IOException ioE = new IOException("!csServletErrorExecutingRequest");
/* 1385 */       ioE.initCause(e);
/* 1386 */       throw ioE;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void processFilterEvent(IdcServletRequestContext request)
/*      */     throws IOException
/*      */   {
/* 1397 */     if (!TraceUtils.m_haveSetUniversalParent)
/*      */     {
/* 1399 */       Throwable t = new StackTrace("Captured parent trace");
/* 1400 */       Report.trace("servlet", "Setting universal parent trace", t);
/* 1401 */       TraceUtils.setUniversalStackTraceParent(t);
/*      */     }
/* 1403 */     ServletActiveLocalData activeData = request.getActiveData();
/* 1404 */     if (IdcServletStaticEnv.m_lowerCaseWebRoot == null)
/*      */     {
/* 1406 */       Report.trace("servlet", "Filter event received on an unitialized server", null);
/* 1407 */       return;
/*      */     }
/*      */ 
/* 1410 */     if (activeData.m_uri == null)
/*      */     {
/* 1412 */       Report.trace("servlet", "Filter event received without a uri path", null);
/* 1413 */       return;
/*      */     }
/* 1415 */     String originalUri = (String)request.getRequestAttribute("ORIGINALURI");
/* 1416 */     if (originalUri == null)
/*      */     {
/* 1418 */       originalUri = activeData.m_uri;
/* 1419 */       request.setRequestAttribute("ORIGINALURI", originalUri);
/*      */     }
/* 1421 */     fixupLowerCaseUri(request, activeData);
/* 1422 */     if (executeServerFilter(request, "preProcessRequest") == -1)
/*      */     {
/* 1424 */       return;
/*      */     }
/* 1426 */     if (request.getResponseSent())
/*      */     {
/* 1428 */       return;
/*      */     }
/*      */ 
/* 1432 */     fixupLowerCaseUri(request, activeData);
/*      */ 
/* 1437 */     boolean doRedirectToHome = false;
/* 1438 */     boolean doRedirectToFullHomePage = false;
/* 1439 */     int decodedUrlLen = activeData.m_lowerCaseDecodedUri.length();
/* 1440 */     int idcWebRootLen = IdcServletStaticEnv.m_lowerCaseWebRoot.length();
/* 1441 */     if (decodedUrlLen <= idcWebRootLen)
/*      */     {
/* 1443 */       if (decodedUrlLen == idcWebRootLen - 1)
/*      */       {
/* 1446 */         doRedirectToHome = true;
/*      */         ServletActiveLocalData tmp182_181 = activeData; tmp182_181.m_lowerCaseDecodedUri = new StringBuilder().append(tmp182_181.m_lowerCaseDecodedUri).append("/").toString();
/*      */       }
/*      */ 
/* 1453 */       if (activeData.m_authUser != null)
/*      */       {
/* 1455 */         doRedirectToHome = true;
/* 1456 */         doRedirectToFullHomePage = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1463 */     if ((activeData.m_lowerCaseDecodedUri.equals(IdcServletStaticEnv.m_cgiPathRoot)) && 
/* 1465 */       (((activeData.m_query == null) || (activeData.m_query.length() == 0))) && 
/* 1467 */       (activeData.m_method.equalsIgnoreCase("GET")))
/*      */     {
/* 1469 */       doRedirectToHome = true;
/* 1470 */       activeData.m_lowerCaseDecodedUri = IdcServletStaticEnv.m_lowerCaseWebRoot;
/*      */     }
/*      */ 
/* 1476 */     if ((!activeData.m_delegateToContentServer) && (!activeData.m_lowerCaseDecodedUri.startsWith(IdcServletStaticEnv.m_lowerCaseWebRoot)) && (!activeData.m_showLoginForm))
/*      */     {
/* 1479 */       if (Report.m_verbose)
/*      */       {
/* 1481 */         Report.trace("servlet", new StringBuilder().append("processFilterEvent skipping URL ").append(activeData.m_uri).append(", it does not start with web relative root ").append(IdcServletStaticEnv.m_lowerCaseWebRoot).toString(), null);
/*      */       }
/*      */ 
/* 1485 */       return;
/*      */     }
/*      */ 
/* 1489 */     request.setRequestAttribute("OwningRelativeWebRoot", IdcServletStaticEnv.m_relativeWebRoot);
/*      */ 
/* 1502 */     checkLoadAdditonalRequestHeaders(request, activeData);
/*      */ 
/* 1504 */     if ((activeData.m_isPromptLogin) || (activeData.m_isError))
/*      */     {
/* 1506 */       checkSendImmediateResponse(request);
/* 1507 */       return;
/*      */     }
/*      */ 
/* 1510 */     if (!activeData.m_delegateToContentServer)
/*      */     {
/* 1513 */       int staticPageTarget = -1;
/* 1514 */       if (activeData.m_showLoginForm)
/*      */       {
/* 1517 */         Object authTargetUrl = request.getLocalParameter("authTargetUrl");
/* 1518 */         Object exceptionObj = request.getLocalParameter("authException");
/* 1519 */         Report.trace("servlet", new StringBuilder().append("Showing login form with authTargetUrl=").append(authTargetUrl).append(", authException=").append(exceptionObj).toString(), null);
/*      */ 
/* 1521 */         if ((exceptionObj != null) && (exceptionObj instanceof Exception) && (Report.m_verbose))
/*      */         {
/* 1523 */           Exception exception = (Exception)exceptionObj;
/* 1524 */           Report.trace("servlet", "Authentication exception that caused redisplay of login form", exception);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1529 */         String portalName = "portal.htm";
/* 1530 */         String logoutName = "logout.htm";
/* 1531 */         int index = IdcServletStaticEnv.m_lowerCaseWebRoot.length();
/* 1532 */         if ((doRedirectToHome) || (index >= activeData.m_lowerCaseDecodedUri.length()))
/*      */         {
/* 1534 */           staticPageTarget = 1;
/*      */         }
/* 1536 */         if (staticPageTarget < 0)
/*      */         {
/* 1538 */           char ch = activeData.m_lowerCaseDecodedUri.charAt(index);
/* 1539 */           if (ch == '/')
/*      */           {
/* 1541 */             ++index;
/*      */           }
/* 1543 */           if (activeData.m_lowerCaseDecodedUri.regionMatches(index, portalName, 0, portalName.length()))
/*      */           {
/* 1545 */             staticPageTarget = 1;
/*      */           }
/* 1547 */           if (activeData.m_lowerCaseDecodedUri.regionMatches(index, logoutName, 0, logoutName.length()))
/*      */           {
/* 1549 */             staticPageTarget = 2;
/*      */           }
/*      */         }
/*      */       }
/* 1553 */       if (staticPageTarget == 2)
/*      */       {
/* 1563 */         request.setLocalParameter("doLogout", "1");
/*      */       }
/* 1565 */       else if ((staticPageTarget > 0) || (activeData.m_showLoginForm))
/*      */       {
/* 1567 */         if (activeData.m_showLoginForm)
/*      */         {
/* 1569 */           activeData.m_query = "IdcService=GET_LOGIN_FORM&ActAsAnonymous=1";
/*      */         }
/* 1573 */         else if (doRedirectToHome)
/*      */         {
/* 1575 */           activeData.m_query = "IdcService=PING_SERVER&ActAsAnonymous=1";
/* 1576 */           request.setLocalParameter("servletdoredirect", "1");
/* 1577 */           if (doRedirectToFullHomePage)
/*      */           {
/* 1579 */             request.setLocalParameter("servletredirecturl", new StringBuilder().append(IdcServletStaticEnv.m_cgiPathRoot).append("?IdcService=GET_DOC_PAGE&Action=GetTemplatePage&Page=HOME_PAGE").toString());
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1585 */           activeData.m_query = "IdcService=GET_STATIC_HOME_PAGE&ActAsAnonymous=1";
/*      */         }
/*      */ 
/* 1589 */         activeData.m_delegateToContentServer = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1594 */     if ((IdcServletStaticEnv.m_forceSystemConfigPage) && (!activeData.m_showLoginForm) && (!doRedirectToHome))
/*      */     {
/* 1597 */       String relativePath = activeData.m_lowerCaseDecodedUri.substring(IdcServletStaticEnv.m_lowerCaseWebRoot.length());
/*      */ 
/* 1599 */       int resourcesDirIndex = relativePath.indexOf("resources");
/* 1600 */       int imagesDirIndex = relativePath.indexOf("images");
/* 1601 */       int idcplgIndex = relativePath.indexOf("idcplg");
/* 1602 */       boolean forceSystemConfig = true;
/* 1603 */       if (((resourcesDirIndex >= 0) && (resourcesDirIndex <= 1)) || ((imagesDirIndex >= 0) && (imagesDirIndex <= 1)))
/*      */       {
/* 1606 */         forceSystemConfig = false;
/*      */       }
/* 1608 */       if ((forceSystemConfig) && (idcplgIndex >= 0) && (idcplgIndex <= 1) && ((
/* 1610 */         (activeData.m_query == null) || (activeData.m_query.indexOf("Page=HOME_PAGE") < 0))))
/*      */       {
/* 1612 */         forceSystemConfig = false;
/*      */       }
/*      */ 
/* 1615 */       if ((forceSystemConfig) && (request.getLocalParameter("doLogout") != null))
/*      */       {
/* 1618 */         forceSystemConfig = false;
/*      */       }
/* 1620 */       if (forceSystemConfig)
/*      */       {
/* 1622 */         activeData.m_query = "IdcService=GO_TO_SYSTEM_CONFIGURATION_PAGE";
/* 1623 */         activeData.m_delegateToContentServer = true;
/*      */       }
/*      */     }
/*      */ 
/* 1627 */     String user = activeData.m_authUser;
/* 1628 */     if (user == null)
/*      */     {
/* 1630 */       user = "<anonymous>";
/*      */     }
/* 1632 */     Report.trace("servlet", new StringBuilder().append("processFilterEvent enter: user = ").append(user).append(" uri = ").append(activeData.m_uri).append(" query = ").append(activeData.m_query).toString(), null);
/*      */ 
/* 1634 */     if ((!activeData.m_delegateToContentServer) && 
/* 1636 */       (activeData.m_lowerCaseDecodedUri.startsWith(IdcServletStaticEnv.m_lowerCaseCgiPathRoot)))
/*      */     {
/* 1638 */       activeData.m_delegateToContentServer = true;
/*      */ 
/* 1641 */       if ((originalUri.indexOf("idcplg") > 0) && (originalUri.indexOf("/groups/") < 0))
/*      */       {
/* 1643 */         activeData.m_uriPath = originalUri;
/*      */       }
/*      */     }
/*      */ 
/* 1647 */     if (!activeData.m_delegateToContentServer)
/*      */     {
/* 1649 */       String fileName = FileUtils.getName(activeData.m_lowerCaseDecodedUri);
/* 1650 */       String ext = FileUtils.getExtension(fileName);
/* 1651 */       if (StringUtils.matchEx(ext, IdcServletStaticEnv.m_internalAppExtensionWildcardTest, true, false))
/*      */       {
/* 1653 */         activeData.m_isProxiedPath = true;
/*      */       }
/* 1655 */       else if (StringUtils.matchEx(ext, IdcServletStaticEnv.m_executableExtensionWildcardTest, true, false))
/*      */       {
/* 1657 */         activeData.m_contentExecutedByApplicationServer = true;
/*      */       }
/*      */     }
/*      */ 
/* 1661 */     if ((!activeData.m_isProxiedPath) && (!activeData.m_delegateToContentServer))
/*      */     {
/* 1663 */       String uri = activeData.m_lowerCaseDecodedUri;
/* 1664 */       int grpIndex = uri.indexOf(IdcServletStaticEnv.m_lowerCaseAuthPrefix);
/* 1665 */       if ((grpIndex >= 0) && (!activeData.m_method.equalsIgnoreCase("OPTIONS")))
/*      */       {
/* 1668 */         int uriLength = uri.length();
/* 1669 */         int charIndex = grpIndex + IdcServletStaticEnv.m_lowerCaseAuthPrefix.length();
/* 1670 */         boolean started = false;
/* 1671 */         int startIndex = -1;
/* 1672 */         String group = null;
/* 1673 */         while (charIndex < uriLength)
/*      */         {
/* 1675 */           char ch = uri.charAt(charIndex);
/* 1676 */           if ((ch == '/') || (ch == '\\'))
/*      */           {
/* 1678 */             if (!started)
/*      */               break label1206;
/* 1680 */             group = uri.substring(startIndex, charIndex);
/* 1681 */             break;
/*      */           }
/*      */ 
/* 1684 */           if (!started)
/*      */           {
/* 1686 */             started = true;
/* 1687 */             startIndex = charIndex;
/*      */           }
/* 1689 */           label1206: ++charIndex;
/*      */         }
/* 1691 */         if (group != null)
/*      */         {
/*      */           try
/*      */           {
/* 1695 */             checkSecuredUri(request, activeData, uri, group, charIndex);
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/* 1699 */             activeData.m_isError = true;
/* 1700 */             activeData.m_errorException = e;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1705 */     if ((activeData.m_isPromptLogin) || (activeData.m_isError))
/*      */     {
/* 1707 */       checkSendImmediateResponse(request);
/* 1708 */       return;
/*      */     }
/* 1710 */     if (executeServerFilter(request, "prepareHandleRequest") == -1)
/*      */     {
/* 1712 */       return;
/*      */     }
/* 1714 */     if ((activeData.m_isProxiedPath) && (!activeData.m_delegateToContentServer))
/*      */     {
/* 1716 */       String newQuery = "IdcService=GET_DYNAMIC_URL&fileUrl=";
/* 1717 */       String url = decodeUri(request, activeData, activeData.m_uri);
/* 1718 */       String query = activeData.m_query;
/* 1719 */       if ((query != null) && (query.length() > 0))
/*      */       {
/* 1721 */         url = new StringBuilder().append(url).append("?").append(query).toString();
/*      */       }
/* 1723 */       String encodedUrl = StringUtils.encodeUrlStyle(url, '%', true);
/* 1724 */       newQuery = new StringBuilder().append(newQuery).append(encodedUrl).toString();
/* 1725 */       activeData.m_query = newQuery;
/* 1726 */       activeData.m_queryUpdated = true;
/* 1727 */       activeData.m_delegateToContentServer = true;
/*      */     }
/* 1729 */     if (activeData.m_delegateToContentServer)
/*      */     {
/* 1731 */       initializeRequest(request);
/* 1732 */       doRequest(request, null);
/* 1733 */       if ((!activeData.m_isPromptLogin) && (!activeData.m_isError))
/*      */       {
/* 1735 */         request.setResponseSent(true);
/*      */       }
/*      */     }
/*      */ 
/* 1739 */     if (request.getResponseSent())
/*      */       return;
/* 1741 */     if ((activeData.m_isPromptLogin) || (activeData.m_isError))
/*      */     {
/* 1744 */       request.setLocalParameter("servletpath", "error.htm");
/* 1745 */       checkSendImmediateResponse(request);
/* 1746 */       return;
/*      */     }
/* 1748 */     if (executeServerFilter(request, "afterApproveUrl") == -1)
/*      */     {
/* 1750 */       return;
/*      */     }
/*      */ 
/* 1753 */     fixupLowerCaseUri(request, activeData);
/*      */ 
/* 1757 */     String decodedUrl = activeData.m_decodedUri;
/*      */ 
/* 1760 */     if ((!activeData.m_lowerCaseDecodedUri.startsWith(IdcServletStaticEnv.m_lowerCaseWebRoot)) || (decodedUrl.length() < IdcServletStaticEnv.m_lowerCaseWebRoot.length()))
/*      */     {
/* 1764 */       activeData.m_errorException = new ServiceException(null, "csServletInvalidStaticUrl", new Object[] { decodedUrl });
/* 1765 */       sendImmediateResponse(request, activeData);
/* 1766 */       return;
/*      */     }
/*      */ 
/* 1771 */     String servletPath = decodedUrl.substring(IdcServletStaticEnv.m_lowerCaseWebRoot.length() - 1);
/*      */ 
/* 1773 */     if (m_extraDebugCode)
/*      */     {
/* 1776 */       if (servletPath.indexOf("/groups/") >= 0)
/*      */       {
/* 1778 */         String weblayoutDir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/* 1779 */         String filePath = new StringBuilder().append(weblayoutDir).append(servletPath).toString();
/* 1780 */         File testFile = new File(filePath);
/* 1781 */         boolean testFileExists = testFile.exists();
/* 1782 */         Report.trace("filestore", new StringBuilder().append("File path ").append(filePath).append((testFileExists) ? " exists" : " does not exist").append(" on the file system").toString(), null);
/*      */       }
/*      */ 
/* 1787 */       String query = activeData.m_query;
/* 1788 */       int index = -1;
/* 1789 */       if ((query != null) && ((index = query.indexOf("delay=")) >= 0))
/*      */       {
/* 1791 */         int endIndex = query.indexOf(38, index + 1);
/* 1792 */         if (endIndex < 0)
/*      */         {
/* 1794 */           endIndex = query.length();
/*      */         }
/* 1796 */         String param = query.substring(index + "delay=".length(), endIndex);
/* 1797 */         int sleepTime = NumberUtils.parseInteger(param, 0);
/* 1798 */         if (sleepTime > 0)
/*      */         {
/* 1800 */           Report.trace("filestore", new StringBuilder().append("Sleeping for ").append(sleepTime).append(" milliseconds").append(" before delivering servlet path = ").append(servletPath).toString(), null);
/* 1801 */           SystemUtils.sleep(sleepTime);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1806 */     request.setLocalParameter("servletpath", servletPath);
/*      */   }
/*      */ 
/*      */   public static String decodeUri(IdcServletRequestContext request, ServletActiveLocalData activeData, String uri)
/*      */     throws IOException
/*      */   {
/* 1822 */     return StringUtils.decodeUrlEncodedString(uri, FileUtils.m_javaSystemEncoding);
/*      */   }
/*      */ 
/*      */   public static void fixupLowerCaseUri(IdcServletRequestContext request, ServletActiveLocalData activeData)
/*      */     throws IOException
/*      */   {
/* 1835 */     if (activeData.m_haveDecodedUris)
/*      */       return;
/* 1837 */     if (activeData.m_uri != null)
/*      */     {
/* 1839 */       activeData.m_decodedUri = decodeUri(request, activeData, activeData.m_uri);
/* 1840 */       activeData.m_lowerCaseDecodedUri = activeData.m_decodedUri.toLowerCase();
/*      */     }
/* 1842 */     activeData.m_haveDecodedUris = true;
/*      */   }
/*      */ 
/*      */   public static void checkLoadAdditonalRequestHeaders(IdcServletRequestContext request, ServletActiveLocalData activeData)
/*      */     throws IOException
/*      */   {
/* 1857 */     boolean hasInternalAuthData = false;
/* 1858 */     boolean hasAuthData = false;
/* 1859 */     String idcProxyAuth = request.getRequestHeader("idcproxyauth");
/* 1860 */     if (idcProxyAuth != null)
/*      */     {
/* 1862 */       DataBinder binder = request.getParentDataBinder();
/* 1863 */       ExecutionContext cxt = request.getParentExecutionContext();
/* 1864 */       binder.setEnvironmentValue("IDCPROXYAUTH", idcProxyAuth);
/* 1865 */       String clientHostAddress = (String)request.getRequestAttribute("remoteip");
/* 1866 */       Map proxyParams = new HashMap();
/* 1867 */       boolean isAuthenticated = false;
/*      */       try
/*      */       {
/* 1874 */         isAuthenticated = ProxyConnectionUtils.incomingProxyAuth(proxyParams, binder, null, clientHostAddress, null, cxt);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1878 */         activeData.m_isError = true;
/* 1879 */         activeData.m_statusCode = 403;
/* 1880 */         activeData.m_errorException = e;
/*      */       }
/*      */ 
/* 1883 */       if (isAuthenticated)
/*      */       {
/* 1887 */         String proxyRemoteUser = request.getRequestHeader("idcproxy-remote-user");
/* 1888 */         if (proxyRemoteUser == null)
/*      */         {
/* 1890 */           proxyRemoteUser = request.getRequestHeader("idcproxy-internetuser");
/*      */         }
/* 1892 */         if (proxyRemoteUser != null)
/*      */         {
/* 1899 */           request.setLocalParameter("auth-user", proxyRemoteUser);
/*      */         }
/* 1901 */         hasAuthData = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1906 */     Object o = request.getLocalParameter("IdcAuthorization");
/* 1907 */     Map authValues = null;
/* 1908 */     if ((o != null) && (o instanceof Map) && (!activeData.m_isError))
/*      */     {
/* 1910 */       authValues = (Map)o;
/* 1911 */       String appId = (String)authValues.get("authServerID");
/* 1912 */       boolean fromOurSelf = (appId != null) && (appId.equals(IdcServletStaticEnv.m_servletAppID));
/* 1913 */       if (!fromOurSelf)
/*      */       {
/* 1915 */         Object existingRoles = authValues.get("roles");
/* 1916 */         if ((existingRoles == null) || (existingRoles.toString().length() == 0))
/*      */         {
/* 1918 */           processUnmappedGroups(request, activeData, authValues);
/*      */         }
/* 1920 */         hasInternalAuthData = true;
/* 1921 */         hasAuthData = true;
/*      */       }
/*      */     }
/*      */ 
/* 1925 */     if (!hasAuthData)
/*      */       return;
/* 1927 */     if (activeData.m_additionalHeaders == null)
/*      */     {
/* 1929 */       activeData.m_additionalHeaders = new ArrayList();
/*      */     }
/*      */ 
/* 1932 */     int lookupIndexForKeyName = (hasInternalAuthData) ? 0 : 2;
/* 1933 */     for (int i = 0; i < m_authorizationValues.length; ++i)
/*      */     {
/* 1935 */       String getKey = m_authorizationValues[i][lookupIndexForKeyName];
/* 1936 */       String setKey = m_authorizationValues[i][1];
/* 1937 */       Object val = (hasInternalAuthData) ? authValues.get(getKey) : request.getRequestHeader(getKey);
/*      */ 
/* 1939 */       if ((val == null) || (!val instanceof String))
/*      */         continue;
/* 1941 */       if (hasInternalAuthData)
/*      */       {
/* 1943 */         val = StringUtils.encodeLiteralStringEscapeSequence((String)val);
/*      */       }
/* 1945 */       String[] headerRow = { setKey, (String)val };
/* 1946 */       activeData.m_additionalHeaders.add(headerRow);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void processUnmappedGroups(IdcServletRequestContext request, ServletActiveLocalData activeData, Map authValues)
/*      */     throws IOException
/*      */   {
/* 1966 */     if (executeServerFilter(request, "computeMappedUserCredentials") != 0)
/*      */     {
/* 1968 */       return;
/*      */     }
/* 1970 */     Object unmappedObj = authValues.get("unmappedgroups");
/* 1971 */     List unmappedGroups = null;
/*      */ 
/* 1973 */     boolean useSubjectAuth = SharedObjects.getEnvValueAsBoolean("UseSubjectGroupData", false);
/* 1974 */     if ((unmappedObj != null) && (unmappedObj instanceof List))
/*      */     {
/* 1976 */       unmappedGroups = (List)unmappedObj;
/*      */     }
/* 1978 */     if (unmappedGroups == null)
/*      */       return;
/* 1980 */     List roles = new ArrayList();
/* 1981 */     List accounts = new ArrayList();
/* 1982 */     for (String group : unmappedGroups)
/*      */     {
/* 1985 */       if ((useSubjectAuth) && (group.startsWith("@")))
/*      */       {
/* 1987 */         String acct = group.substring(1);
/* 1988 */         accounts.add(acct);
/*      */       }
/* 1991 */       else if ((group.equalsIgnoreCase("Administrators")) || (group.equalsIgnoreCase("sysmanager")))
/*      */       {
/* 1993 */         roles.add("admin");
/* 1994 */         roles.add("sysmanager");
/* 1995 */         roles.add("refineryadmin");
/* 1996 */         roles.add("rmaadmin");
/* 1997 */         roles.add("pcmadmin");
/* 1998 */         roles.add("ermadmin");
/* 1999 */         accounts.add("#all");
/*      */       }
/* 2001 */       else if (useSubjectAuth)
/*      */       {
/* 2003 */         roles.add(group);
/*      */       }
/*      */     }
/* 2006 */     String rolesEncoded = StringUtils.createStringSimple(roles);
/* 2007 */     String accountsEncoded = StringUtils.createStringSimple(accounts);
/* 2008 */     authValues.put("roles", rolesEncoded);
/* 2009 */     authValues.put("accounts", accountsEncoded);
/*      */   }
/*      */ 
/*      */   public static void checkSecuredUri(IdcServletRequestContext request, ServletActiveLocalData activeData, String uri, String group, int charIndex)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2025 */     boolean isAuthorized = false;
/* 2026 */     boolean isAccessDenied = false;
/* 2027 */     String user = activeData.m_authUser;
/* 2028 */     if ((activeData.m_contentExecutedByApplicationServer) && (!IdcServletStaticEnv.isAppExecutableGroup(group)))
/*      */     {
/* 2031 */       activeData.m_statusCode = 403;
/* 2032 */       int errorCode = -24;
/* 2033 */       activeData.m_isError = true;
/* 2034 */       ServiceException se = new ServiceException(null, errorCode, "csIllegalExecutionOfDynamicContent", new Object[] { group });
/*      */ 
/* 2036 */       activeData.m_errorException = se;
/*      */     }
/*      */ 
/* 2039 */     boolean isSpecialGroup = IdcServletStaticEnv.isSpecialGroup(group);
/* 2040 */     if ((((isSpecialGroup) || (SharedObjects.getEnvValueAsBoolean("CheckWebFileExists", false)))) && (!uri.regionMatches(charIndex, "/logs/", 0, 5)))
/*      */     {
/* 2045 */       DataBinder authBinder = new DataBinder();
/* 2046 */       String serviceName = "CHECK_USER_CREDENTIALS";
/* 2047 */       if (user != null)
/*      */       {
/* 2049 */         authBinder.putLocal("userName", activeData.m_authUser);
/*      */       }
/* 2051 */       authBinder.putLocal("checkPath", "1");
/* 2052 */       authBinder.putLocal("fileUrl", uri);
/* 2053 */       if (!isSpecialGroup)
/*      */       {
/* 2056 */         authBinder.putLocal("isStandardAuth", "1");
/*      */       }
/*      */ 
/* 2065 */       authBinder.putLocal("allowAlterCredentials", "1");
/*      */ 
/* 2067 */       if (activeData.m_additionalHeaders != null)
/*      */       {
/* 2069 */         for (String[] headerRow : activeData.m_additionalHeaders)
/*      */         {
/* 2071 */           String key = headerRow[0];
/* 2072 */           if (key.equals("EXTERNAL_ROLES"))
/*      */           {
/* 2074 */             authBinder.putLocal("userRoles", headerRow[1]);
/*      */           }
/* 2076 */           else if (key.equals("EXTERNAL_ACCOUNTS"))
/*      */           {
/* 2078 */             authBinder.putLocal("userAccounts", headerRow[1]);
/*      */           }
/*      */         }
/*      */       }
/* 2082 */       executeService(request, serviceName, authBinder, activeData);
/* 2083 */       String StatusCode = authBinder.getLocal("StatusCode");
/* 2084 */       String StatusMessage = authBinder.getLocal("StatusMessage");
/* 2085 */       if ((NumberUtils.parseInteger(StatusCode, 0) < 0) && (StatusMessage.equals("!csUnableToCheckUserCreds!csSystemNeedsLogin3")))
/*      */       {
/* 2088 */         activeData.m_statusCode = 403;
/*      */       }
/* 2090 */       activeData.m_validateUrlUserData = ((UserData)request.getLocalParameter("RecentlyExecutedServiceUserData"));
/*      */ 
/* 2097 */       if (DataBinderUtils.getLocalBoolean(authBinder, "isAccessDenied", false))
/*      */       {
/* 2100 */         isAccessDenied = true;
/*      */       }
/* 2102 */       else if (DataBinderUtils.getLocalBoolean(authBinder, "isAuthorized", false))
/*      */       {
/* 2105 */         isAuthorized = true;
/*      */       }
/*      */     }
/* 2108 */     if ((!isAuthorized) && (!isAccessDenied))
/*      */     {
/* 2111 */       int uriLength = uri.length();
/* 2112 */       boolean foundSlash = false;
/* 2113 */       boolean startAccountSegment = false;
/* 2114 */       int startAccountIndex = -1;
/* 2115 */       IdcStringBuilder accountBuffer = new IdcStringBuilder(50);
/* 2116 */       boolean appendedToBuffer = false;
/* 2117 */       while (charIndex < uriLength)
/*      */       {
/* 2119 */         char ch = uri.charAt(charIndex);
/* 2120 */         if ((ch == '@') && (!startAccountSegment))
/*      */         {
/* 2122 */           if (foundSlash)
/*      */           {
/* 2124 */             startAccountSegment = true;
/* 2125 */             startAccountIndex = charIndex + 1;
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 2130 */           foundSlash = (ch == '/') || (ch == '\\');
/* 2131 */           if ((foundSlash) && (startAccountSegment))
/*      */           {
/* 2133 */             if (appendedToBuffer)
/*      */             {
/* 2135 */               accountBuffer.append('/');
/*      */             }
/* 2137 */             accountBuffer.append(uri, startAccountIndex, charIndex - startAccountIndex);
/* 2138 */             appendedToBuffer = true;
/* 2139 */             startAccountSegment = false;
/*      */           }
/*      */         }
/* 2142 */         ++charIndex;
/*      */       }
/* 2144 */       String account = accountBuffer.toString();
/* 2145 */       UserData userData = activeData.m_validateUrlUserData;
/* 2146 */       if (userData == null)
/*      */       {
/* 2148 */         userData = getUserDataForUser(user, IdcServletStaticEnv.m_systemWorkspace, request, activeData);
/*      */ 
/* 2150 */         activeData.m_validateUrlUserData = userData;
/*      */       }
/* 2152 */       int readPrivilege = 1;
/* 2153 */       int priv = SecurityUtils.determineGroupPrivilege(userData, group);
/* 2154 */       boolean canRead = (priv & readPrivilege) != 0;
/* 2155 */       if ((canRead) && (SecurityUtils.m_useAccounts))
/*      */       {
/* 2157 */         canRead = SecurityUtils.isAccountAccessible(userData, account, readPrivilege);
/*      */       }
/* 2159 */       isAuthorized = canRead;
/*      */     }
/*      */ 
/* 2162 */     if (!isAuthorized)
/*      */     {
/* 2164 */       boolean isAnonymous = !IdcServletStaticEnv.isRealUser(user);
/* 2165 */       if (isAnonymous)
/*      */       {
/* 2167 */         activeData.m_isPromptLogin = true;
/*      */       }
/*      */       else
/*      */       {
/* 2171 */         activeData.m_statusCode = 403;
/* 2172 */         int errorCode = -18;
/* 2173 */         activeData.m_isError = true;
/* 2174 */         ServiceException se = new ServiceException(null, errorCode, "csUserInsufficientAccess", new Object[] { user });
/*      */ 
/* 2176 */         activeData.m_errorException = se;
/*      */       }
/*      */     }
/*      */ 
/* 2180 */     if ((!IdcServletStaticEnv.isProxiedFileGroup(group)) || (activeData.m_lowerCaseDecodedUri.contains("/logs/"))) {
/*      */       return;
/*      */     }
/*      */ 
/* 2184 */     activeData.m_isProxiedPath = true;
/*      */   }
/*      */ 
/*      */   public static UserData getUserDataForUser(String user, Workspace ws, IdcServletRequestContext request, ServletActiveLocalData activeData)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2191 */     ExecutionContext cxt = request.getParentExecutionContext();
/* 2192 */     DataBinder binder = request.getParentDataBinder();
/* 2193 */     if (activeData.m_additionalHeaders != null)
/*      */     {
/* 2195 */       for (String[] headerRow : activeData.m_additionalHeaders)
/*      */       {
/* 2197 */         binder.setEnvironmentValue(headerRow[0], headerRow[1]);
/*      */       }
/*      */     }
/* 2200 */     int flags = 0;
/* 2201 */     if (!IdcServletStaticEnv.isRealUser(user))
/*      */     {
/* 2203 */       flags |= 1;
/*      */     }
/* 2205 */     UserData userData = UserStorageUtils.loadUserData(user, binder, binder.getLocalData(), ws, cxt, flags);
/*      */ 
/* 2209 */     PluginFilters.filter("alterUserCredentials", ws, binder, cxt);
/*      */ 
/* 2211 */     return userData;
/*      */   }
/*      */ 
/*      */   public static void processAuthEvent(IdcServletRequestContext request)
/*      */     throws IOException
/*      */   {
/* 2222 */     if (executeServerFilter(request, "computeCredentials") == -1)
/*      */     {
/* 2224 */       return;
/*      */     }
/*      */ 
/* 2227 */     ServletActiveLocalData activeData = request.getActiveData();
/* 2228 */     if (activeData.m_isFinishedAuth)
/*      */     {
/* 2230 */       return;
/*      */     }
/*      */ 
/* 2233 */     String password = (String)request.getLocalParameter("password");
/* 2234 */     String username = (String)request.getLocalParameter("userName");
/*      */ 
/* 2236 */     DataBinder authBinder = new DataBinder();
/* 2237 */     String serviceName = "CHECK_USER_CREDENTIALS";
/* 2238 */     authBinder.putLocal("dUser", username);
/* 2239 */     authBinder.putLocal("userName", username);
/* 2240 */     authBinder.putLocal("userPassword", password);
/* 2241 */     authBinder.putLocal("authenticateUser", "1");
/* 2242 */     authBinder.putLocal("getUserInfo", "1");
/*      */ 
/* 2244 */     executeService(request, serviceName, authBinder, activeData);
/*      */ 
/* 2246 */     boolean isauthenticated = false;
/* 2247 */     if (DataBinderUtils.getLocalBoolean(authBinder, "isPromptLogin", false))
/*      */     {
/* 2249 */       activeData.m_isPromptLogin = true;
/*      */     }
/* 2251 */     else if (!activeData.m_isError)
/*      */     {
/* 2254 */       for (int i = 0; i < m_authorizationValues.length; ++i)
/*      */       {
/* 2256 */         String key = m_authorizationValues[i][0];
/* 2257 */         String val = authBinder.getLocal(key);
/* 2258 */         if (val == null)
/*      */           continue;
/* 2260 */         request.setLocalParameter(key, val);
/* 2261 */         isauthenticated = true;
/*      */       }
/*      */ 
/* 2264 */       request.setLocalParameter("authServerID", IdcServletStaticEnv.m_servletAppID);
/*      */     }
/* 2266 */     setBooleanValue(request, "isauthenticated", isauthenticated);
/* 2267 */     activeData.m_isFinishedAuth = true;
/* 2268 */     checkSendImmediateResponse(request);
/*      */   }
/*      */ 
/*      */   public static void executeService(IdcServletRequestContext request, String serviceName, DataBinder binder, ServletActiveLocalData activeData)
/*      */   {
/* 2281 */     Workspace ws = IdcServletStaticEnv.m_systemWorkspace;
/* 2282 */     Service service = null;
/*      */     try
/*      */     {
/* 2285 */       binder.putLocal("IdcService", serviceName);
/* 2286 */       service = ServiceManager.getInitializedService(serviceName, binder, ws);
/* 2287 */       ExecutionContext parentCxt = request.getParentExecutionContext();
/* 2288 */       service.setParentContext(parentCxt);
/* 2289 */       service.doRequestInternal();
/*      */     }
/*      */     catch (Exception userData)
/*      */     {
/*      */       UserData userData;
/*      */       String isLocalFileCreated;
/* 2295 */       activeData.m_isError = true;
/* 2296 */       activeData.m_errorException = e;
/*      */     }
/*      */     finally
/*      */     {
/*      */       UserData userData;
/*      */       String isLocalFileCreated;
/* 2300 */       if (service != null)
/*      */       {
/* 2310 */         UserData userData = (UserData)service.getCachedObject("TargetUserData");
/* 2311 */         if (userData == null)
/*      */         {
/* 2313 */           userData = service.getUserData();
/*      */         }
/* 2315 */         if (userData != null)
/*      */         {
/* 2317 */           request.setLocalParameter("RecentlyExecutedServiceUserData", userData);
/*      */         }
/*      */ 
/* 2323 */         String isLocalFileCreated = (String)service.getCachedObject("isJdbcStoredFileCopied");
/* 2324 */         if (isLocalFileCreated != null)
/*      */         {
/* 2329 */           if (request.getRequestAttribute("isJdbcStoredFileCopied") != null)
/*      */           {
/* 2331 */             request.setRequestAttribute("isJdbcStoredFileCopied", null);
/*      */           }
/*      */           else
/*      */           {
/* 2335 */             request.setRequestAttribute("isJdbcStoredFileCopied", isLocalFileCreated);
/*      */           }
/*      */         }
/* 2338 */         service.clear();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void executeRequest(Map<String, String> env, Map params, InputStream inStream, OutputStream outStream, Map args, Map results)
/*      */     throws IOException
/*      */   {
/* 2361 */     DataBinder binder = new DataBinder(SharedObjects.getSafeEnvironment());
/* 2362 */     Set envKeys = env.keySet();
/* 2363 */     for (String key : envKeys)
/*      */     {
/* 2365 */       String val = (String)env.get(key);
/* 2366 */       if (val != null)
/*      */       {
/* 2368 */         binder.setEnvironmentValue(key, val);
/*      */       }
/*      */     }
/* 2371 */     Set keys = params.keySet();
/* 2372 */     for (Iterator i$ = keys.iterator(); i$.hasNext(); ) { Object key = i$.next();
/*      */ 
/* 2374 */       String keyStr = key.toString();
/* 2375 */       Object val = params.get(key);
/* 2376 */       if ((val != null) && (val instanceof String))
/*      */       {
/* 2378 */         binder.putLocal(keyStr, (String)val);
/*      */       } }
/*      */ 
/* 2381 */     convertWebServicesDataToBinder(params, binder);
/*      */ 
/* 2383 */     DirectRequestData directData = new DirectRequestData(binder, args, inStream, outStream, "servlet");
/* 2384 */     doRequest(null, directData);
/* 2385 */     if (outStream != null)
/*      */       return;
/* 2387 */     boolean isResponseParamIncluded = SharedObjects.getEnvValueAsBoolean("IsGenericSoapServiceResponseParamaterIncluded", false);
/*      */ 
/* 2389 */     if (isResponseParamIncluded)
/*      */     {
/* 2391 */       IdcCharArrayWriter w = new IdcCharArrayWriter();
/* 2392 */       binder.send(w);
/* 2393 */       String response = w.toStringRelease();
/* 2394 */       results.put("response", response);
/*      */     }
/*      */ 
/* 2397 */     convertBinderToWebServicesData(binder, results);
/*      */   }
/*      */ 
/*      */   public static void convertWebServicesDataToBinder(Map map, DataBinder binder)
/*      */     throws IOException
/*      */   {
/*      */     Map resultSets;
/* 2422 */     if (map.containsKey("__IDC_RESULT_SETS"))
/*      */     {
/* 2424 */       resultSets = (Map)map.get("__IDC_RESULT_SETS");
/*      */ 
/* 2426 */       for (String rsetName : resultSets.keySet())
/*      */       {
/* 2428 */         String[][] rset = (String[][])(String[][])resultSets.get(rsetName);
/* 2429 */         DataResultSet drset = new DataResultSet(rset[0]);
/*      */ 
/* 2431 */         for (int a = 1; a < rset.length; ++a)
/*      */         {
/* 2433 */           drset.addRowWithList(Arrays.asList(rset[a]));
/*      */         }
/*      */ 
/* 2436 */         binder.addResultSet(rsetName, drset);
/*      */       }
/*      */     }
/*      */     Map optLists;
/* 2439 */     if (map.containsKey("__IDC_OPTION_LISTS"))
/*      */     {
/* 2441 */       optLists = (Map)map.get("__IDC_OPTION_LISTS");
/*      */ 
/* 2443 */       for (String optListName : optLists.keySet())
/*      */       {
/* 2445 */         List list = (List)optLists.get(optListName);
/* 2446 */         binder.addOptionList(optListName, new IdcVector(list));
/*      */       }
/*      */     }
/* 2449 */     if (!map.containsKey("__IDC_STREAMS"))
/*      */       return;
/* 2451 */     Map files = (Map)map.get("__IDC_STREAMS");
/*      */ 
/* 2453 */     for (String filePath : files.keySet())
/*      */     {
/* 2455 */       Object[] data = (Object[])(Object[])files.get(filePath);
/* 2456 */       String fileType = (String)data[1];
/* 2457 */       binder.putLocal(fileType, filePath);
/* 2458 */       String tmpFilePath = SoapUtils.getTempFile(binder, fileType, filePath);
/*      */ 
/* 2460 */       InputStream is = (InputStream)data[0];
/* 2461 */       OutputStream out = new FileOutputStream(tmpFilePath);
/* 2462 */       byte[] buf = null;
/*      */       try
/*      */       {
/* 2465 */         buf = (byte[])(byte[])FileUtils.createBufferForStreaming(1024, 0);
/*      */ 
/* 2467 */         while ((len = is.read(buf)) > 0)
/*      */         {
/*      */           int len;
/* 2469 */           out.write(buf, 0, len);
/*      */         }
/*      */       }
/*      */       finally
/*      */       {
/* 2474 */         FileUtils.closeObjects(is, out);
/* 2475 */         FileUtils.releaseBufferForStreaming(buf);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void convertBinderToWebServicesData(DataBinder binder, Map map)
/*      */     throws IOException
/*      */   {
/* 2493 */     map.putAll(binder.getLocalData());
/*      */ 
/* 2496 */     Map rsets = binder.getResultSets();
/* 2497 */     if (rsets.size() > 0)
/*      */     {
/* 2499 */       Map mapRsets = new HashMap();
/* 2500 */       for (String rsetName : rsets.keySet())
/*      */       {
/* 2503 */         ResultSet rrset = (ResultSet)rsets.get(rsetName);
/* 2504 */         List rowList = new ArrayList();
/* 2505 */         for (rrset.first(); rrset.isRowPresent(); rrset.next())
/*      */         {
/* 2507 */           String[] fieldValues = new String[rrset.getNumFields()];
/* 2508 */           for (int i = 0; i < rrset.getNumFields(); ++i)
/*      */           {
/* 2510 */             fieldValues[i] = rrset.getStringValue(i);
/*      */           }
/* 2512 */           rowList.add(fieldValues);
/*      */         }
/* 2514 */         String[][] rset = new String[rowList.size() + 1][rrset.getNumFields()];
/* 2515 */         rset[0] = ResultSetUtils.getFieldListAsStringArray(rrset);
/* 2516 */         for (int i = 0; i < rowList.size(); ++i)
/*      */         {
/* 2518 */           rset[(i + 1)] = ((String[])(String[])rowList.get(i));
/*      */         }
/*      */ 
/* 2522 */         mapRsets.put(rsetName, rset);
/*      */       }
/* 2524 */       map.put("__IDC_RESULT_SETS", mapRsets);
/*      */     }
/*      */ 
/* 2528 */     Enumeration optListKeys = binder.getOptionLists();
/* 2529 */     Map optLists = new HashMap();
/* 2530 */     while (optListKeys.hasMoreElements())
/*      */     {
/* 2532 */       String key = (String)optListKeys.nextElement();
/* 2533 */       Vector v = binder.getOptionList(key);
/* 2534 */       String[] optList = (String[])(String[])v.toArray(new String[0]);
/*      */ 
/* 2536 */       optLists.put(key, optList);
/* 2537 */       if (!map.containsKey("__IDC_OPTION_LISTS"))
/*      */       {
/* 2539 */         map.put("__IDC_OPTION_LISTS", optLists);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2544 */     String filePath = binder.getLocal("FilePath");
/* 2545 */     if ((filePath == null) || (filePath.length() <= 0))
/*      */       return;
/* 2547 */     Map files = new HashMap();
/* 2548 */     InputStream is = new FileInputStream(filePath);
/* 2549 */     Object[] data = new Object[2];
/* 2550 */     data[0] = is;
/* 2551 */     data[1] = "";
/* 2552 */     files.put(filePath, data);
/* 2553 */     map.put("__IDC_STREAMS", files);
/*      */   }
/*      */ 
/*      */   public static void loadActiveData(IdcServletRequestContext request)
/*      */   {
/* 2559 */     ServletActiveLocalData activeData = request.getActiveData();
/* 2560 */     activeData.m_method = searchForValue(request, "method");
/* 2561 */     if (activeData.m_contextRoot == null)
/*      */     {
/* 2563 */       activeData.m_contextRoot = searchForValue(request, "context-root");
/*      */     }
/* 2565 */     activeData.m_query = searchForValue(request, "query");
/* 2566 */     activeData.m_authUser = searchForValue(request, "auth-user");
/* 2567 */     activeData.m_isPromptLogin = searchForBooleanValue(request, "ispromptlogin", false);
/*      */ 
/* 2569 */     activeData.m_isFinishedAuth = searchForBooleanValue(request, "isfinishedauth", false);
/*      */ 
/* 2571 */     activeData.m_showLoginForm = searchForBooleanValue(request, "authShowLoginForm", false);
/* 2572 */     activeData.m_isError = searchForBooleanValue(request, "iserror", false);
/*      */ 
/* 2574 */     activeData.m_delegateToContentServer = searchForBooleanValue(request, "send-to-content-server", false);
/*      */ 
/* 2576 */     activeData.m_isSessionLogin = searchForBooleanValue(request, "issessionlogin", false);
/*      */ 
/* 2579 */     if (activeData.m_uri == null)
/*      */     {
/* 2581 */       activeData.m_uri = searchForValue(request, "uri");
/* 2582 */       if (activeData.m_uri != null)
/*      */       {
/* 2584 */         activeData.m_decodedUri = null;
/* 2585 */         activeData.m_lowerCaseDecodedUri = null;
/* 2586 */         activeData.m_haveDecodedUris = false;
/*      */       }
/*      */     }
/* 2589 */     if (!Report.m_verbose)
/*      */       return;
/* 2591 */     Report.trace("servlet", new StringBuilder().append("Loading activeData=").append(activeData).toString(), null);
/*      */   }
/*      */ 
/*      */   public static void propagateUpdates(IdcServletRequestContext request)
/*      */   {
/* 2601 */     ServletActiveLocalData activeData = request.getActiveData();
/* 2602 */     if (activeData.m_authUserUpdated)
/*      */     {
/* 2604 */       request.setLocalParameter("auth-user", activeData.m_authUser);
/* 2605 */       activeData.m_authUserUpdated = false;
/*      */     }
/* 2607 */     if (activeData.m_queryUpdated)
/*      */     {
/* 2609 */       request.setLocalParameter("query", activeData.m_query);
/* 2610 */       activeData.m_queryUpdated = false;
/*      */     }
/* 2612 */     setBooleanValue(request, "send-to-content-server", activeData.m_delegateToContentServer);
/* 2613 */     setBooleanValue(request, "ispromptlogin", activeData.m_isPromptLogin);
/* 2614 */     setBooleanValue(request, "isfinishedauth", activeData.m_isFinishedAuth);
/* 2615 */     setBooleanValue(request, "iserror", activeData.m_isError);
/* 2616 */     setBooleanValue(request, "issessionlogin", activeData.m_isSessionLogin);
/*      */   }
/*      */ 
/*      */   public static void handleOutputStreamCallback(byte[] headerBytes, int start, int len, IdcServletRequestContext request)
/*      */     throws IOException
/*      */   {
/* 2630 */     ServletActiveLocalData activeData = request.getActiveData();
/* 2631 */     activeData.m_inContentServerResponse = true;
/*      */ 
/* 2633 */     String headerEncoding = FileUtils.m_javaSystemEncoding;
/* 2634 */     request.setRequestAttribute("responseencoding", headerEncoding);
/* 2635 */     String responseHeaders = new String(headerBytes, start, len, headerEncoding);
/* 2636 */     if (!request.isHttpRequest())
/*      */     {
/* 2638 */       return;
/*      */     }
/* 2640 */     int curOffset = findNextNonWhitespaceIndex(responseHeaders, 0, responseHeaders.length());
/* 2641 */     if (curOffset < 0)
/*      */     {
/* 2643 */       return;
/*      */     }
/* 2645 */     int protocolStart = curOffset;
/* 2646 */     int firstLineIndex = responseHeaders.indexOf("\r\n", curOffset);
/*      */ 
/* 2648 */     int httpProtocolEndIndex = responseHeaders.indexOf(32, curOffset);
/* 2649 */     if ((httpProtocolEndIndex < 0) || (httpProtocolEndIndex >= firstLineIndex))
/*      */     {
/* 2651 */       return;
/*      */     }
/*      */ 
/* 2655 */     curOffset = findNextNonWhitespaceIndex(responseHeaders, httpProtocolEndIndex, firstLineIndex);
/*      */ 
/* 2657 */     if (curOffset < 0)
/*      */     {
/* 2659 */       return;
/*      */     }
/* 2661 */     int statusIndex = responseHeaders.indexOf(32, curOffset);
/*      */ 
/* 2664 */     if ((curOffset < protocolStart + 6) || ((responseHeaders.charAt(protocolStart) != 'h') && (responseHeaders.charAt(protocolStart) != 'H')))
/*      */     {
/* 2667 */       Report.trace("servlet", "Badly formed servlet header response form content server", null);
/*      */ 
/* 2669 */       return;
/*      */     }
/*      */ 
/* 2672 */     String statusCodeStr = responseHeaders.substring(curOffset, statusIndex);
/* 2673 */     activeData.m_statusCode = (int)NumberUtils.parseLong(statusCodeStr, 0L);
/* 2674 */     activeData.m_responseHeadersOffset = (firstLineIndex + 2);
/* 2675 */     if (activeData.m_statusCode == 0)
/*      */     {
/* 2677 */       Report.trace("servlet", "Missing status code in header response form content server", null);
/*      */ 
/* 2679 */       return;
/*      */     }
/* 2681 */     if (executeServerFilter(request, "contentServerResponse") == -1)
/*      */     {
/* 2683 */       return;
/*      */     }
/*      */ 
/* 2686 */     activeData.m_isPromptLogin = (activeData.m_statusCode == 401);
/* 2687 */     checkSendImmediateResponse(request);
/* 2688 */     if (request.getResponseSent())
/*      */     {
/* 2690 */       return;
/*      */     }
/* 2692 */     if (activeData.m_isPromptLogin)
/*      */     {
/* 2697 */       activeData.m_statusCode = 401;
/* 2698 */       String realm = searchForValue(request, "IdcBasicAuthRealm");
/* 2699 */       if ((realm != null) && (realm.length() > 0))
/*      */       {
/* 2701 */         request.setLocalParameter("IdcBasicAuthRealm", realm);
/*      */       }
/* 2703 */       return;
/*      */     }
/*      */ 
/* 2707 */     request.setHttpResponseStatusCode(activeData.m_statusCode);
/*      */ 
/* 2710 */     curOffset = activeData.m_responseHeadersOffset;
/* 2711 */     while ((curOffset > 0) && (curOffset < responseHeaders.length()))
/*      */     {
/* 2713 */       int nextColonIndex = responseHeaders.indexOf(58, curOffset);
/* 2714 */       if (nextColonIndex < 0) {
/*      */         return;
/*      */       }
/*      */ 
/* 2718 */       String key = responseHeaders.substring(curOffset, nextColonIndex).trim();
/* 2719 */       String lwrKey = key.toLowerCase();
/* 2720 */       int endValueIndex = responseHeaders.indexOf("\r\n", nextColonIndex);
/* 2721 */       if (endValueIndex < 0) {
/*      */         return;
/*      */       }
/*      */ 
/* 2725 */       String value = responseHeaders.substring(nextColonIndex + 1, endValueIndex).trim();
/* 2726 */       if ((!lwrKey.equals("server")) && (!lwrKey.equals("connection")))
/*      */       {
/* 2728 */         if (lwrKey.contains("cookie"))
/*      */         {
/* 2730 */           request.addResponseHeader(key, value);
/*      */         }
/*      */         else
/*      */         {
/* 2734 */           request.setResponseHeader(key, value);
/*      */         }
/*      */       }
/* 2737 */       curOffset = endValueIndex + 2;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void checkSendImmediateResponse(IdcServletRequestContext request) throws IOException
/*      */   {
/* 2743 */     ServletActiveLocalData activeData = request.getActiveData();
/* 2744 */     boolean allowImmediateResponse = (activeData.m_isPromptLogin) || (activeData.m_isError);
/*      */ 
/* 2746 */     if (!allowImmediateResponse)
/*      */       return;
/* 2748 */     if (executeServerFilter(request, "immediateResponsePage") != 0)
/*      */     {
/* 2750 */       return;
/*      */     }
/* 2752 */     if ((request.getResponseSent()) || ((activeData.m_isPromptLogin) && (!activeData.m_showLoginForm)))
/*      */     {
/* 2754 */       return;
/*      */     }
/* 2756 */     sendImmediateResponse(request, activeData);
/*      */   }
/*      */ 
/*      */   public static void sendImmediateResponse(IdcServletRequestContext request, ServletActiveLocalData activeData)
/*      */     throws IOException
/*      */   {
/* 2763 */     if (activeData.m_errorException != null)
/*      */     {
/* 2765 */       boolean reportedError = false;
/* 2766 */       synchronized (m_lastReportError)
/*      */       {
/* 2768 */         long lastReport = m_lastReportError[0];
/* 2769 */         long currentTimeMillis = System.currentTimeMillis();
/* 2770 */         if (currentTimeMillis - lastReport > 3600000L)
/*      */         {
/* 2772 */           reportedError = true;
/* 2773 */           Report.error("servlet", "!csServletExceptionInIntegrationLogic", activeData.m_errorException);
/*      */         }
/*      */       }
/* 2776 */       if (!reportedError)
/*      */       {
/* 2778 */         Report.trace("servlet", "Exception that created servlet error response", activeData.m_errorException);
/*      */       }
/*      */     }
/*      */ 
/* 2782 */     if (activeData.m_statusCode < 300)
/*      */     {
/* 2784 */       activeData.m_statusCode = 503;
/*      */     }
/* 2786 */     request.setHttpResponseStatusCode(activeData.m_statusCode);
/* 2787 */     request.sendStandardHttpErrorResponse();
/* 2788 */     request.setResponseSent(true);
/*      */   }
/*      */ 
/*      */   public static void appendHeaderNameValue(Writer w, String key, String val) throws IOException
/*      */   {
/* 2793 */     w.append(key);
/* 2794 */     w.append('=');
/* 2795 */     w.append(val);
/* 2796 */     w.append("\r\n");
/*      */   }
/*      */ 
/*      */   public static int findNextNonWhitespaceIndex(String str, int start, int end)
/*      */   {
/* 2801 */     int wsIndex = -1;
/* 2802 */     for (int i = start; i < end; ++i)
/*      */     {
/* 2804 */       char ch = str.charAt(i);
/* 2805 */       if (ch == ' ')
/*      */         continue;
/* 2807 */       wsIndex = i;
/* 2808 */       break;
/*      */     }
/*      */ 
/* 2811 */     return wsIndex;
/*      */   }
/*      */ 
/*      */   public static int executeServerFilter(IdcServletRequestContext request, String type) throws IOException
/*      */   {
/* 2816 */     ExecutionContext cxt = request.getParentExecutionContext();
/* 2817 */     DataBinder binder = request.getParentDataBinder();
/* 2818 */     int retVal = 0;
/*      */     try
/*      */     {
/* 2821 */       retVal = PluginFilters.filter(type, IdcServletStaticEnv.m_systemWorkspace, binder, cxt);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2825 */       String msg = LocaleUtils.encodeMessage("csServletFilterException", null, type);
/* 2826 */       IOException ioE = new IOException(msg);
/* 2827 */       ioE.initCause(e);
/* 2828 */       throw ioE;
/*      */     }
/* 2830 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2836 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104051 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.IdcServletRequestUtils
 * JD-Core Version:    0.5.4
 */