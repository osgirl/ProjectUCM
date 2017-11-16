/*      */ package intradoc.admin;
/*      */ 
/*      */ import intradoc.common.ClassHelperUtils;
/*      */ import intradoc.common.DataStreamWrapper;
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseOutput;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.SortUtils;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.SimpleParameters;
/*      */ import intradoc.filestore.config.ConfigFileLoader;
/*      */ import intradoc.filestore.config.ConfigFileUtilities;
/*      */ import intradoc.io.IdcBasicIO;
/*      */ import intradoc.process.ProcessLogger;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.ServerRequest;
/*      */ import intradoc.provider.SocketOutgoingProvider;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.Action;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.IdcServerOutput;
/*      */ import intradoc.server.IdcServiceAction;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.PluginFilterLoader;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceRequestImplementor;
/*      */ import intradoc.server.alert.AlertUtils;
/*      */ import intradoc.server.utils.CompInstallUtils;
/*      */ import intradoc.server.utils.ComponentInstaller;
/*      */ import intradoc.server.utils.ComponentListEditor;
/*      */ import intradoc.server.utils.ComponentListManager;
/*      */ import intradoc.server.utils.ComponentLocationUtils;
/*      */ import intradoc.server.utils.ComponentPreferenceData;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.shared.PluginFilterData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.RevisionSpec;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.BasicIdcMessageContainer;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcMessageContainer;
/*      */ import intradoc.util.IdcVector;
/*      */ import intradoc.zip.ZipFunctions;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileReader;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.io.StringReader;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ import java.util.regex.Matcher;
/*      */ import java.util.regex.Pattern;
/*      */ 
/*      */ public class AdminService extends Service
/*      */   implements IdcBasicIO
/*      */ {
/*      */   public static final int INPUT = 0;
/*      */   public static final int OUTPUT = 1;
/*      */   public static final int F_READ = 0;
/*      */   public static final int F_WRITE = 1;
/*   60 */   public static final String[] UNSETTABLE_VARS = { "IDC_Id", "IDC_Name", "UseSecurity", "IsJdbc", "JdbcUser", "JdbcPassword", "JdbcDriver", "JdbcPasswordEncoding", "JdbcConnectionString", "DatabasePreserveCase", "UserJdbcUser", "UserJdbcPassword", "UserJdbcDriver", "UserJdbcPasswordEncoding", "UserJdbcConnectionString", "HttpServerAddress", "IntradocServerPort", "IntradocServerHostName", "SocketHostNameSecurityFilter", "SocketHostAddressSecurityFilter", "ProxyPassword", "ProxyPasswordEncoding" };
/*      */ 
/*   69 */   public static final String[] NOT_PASSED_VARS = { "IsJdbc", "JdbcUser", "JdbcPassword", "JdbcDriver", "JdbcPasswordEncoding", "JdbcConnectionString", "UserJdbcUser", "UserJdbcPassword", "UserJdbcDriver", "UserJdbcPasswordEncoding", "UserJdbcConnectionString", "ProxyPassword", "ProxyPasswordEncoding" };
/*      */ 
/*   75 */   public static String[] NOT_PROXIED_SERVICES = { "REMOVE_SERVER" };
/*      */   protected AdminServerData m_allServers;
/*      */   protected ConfigFileUtilities m_CFU;
/*      */   protected Map m_administratedServerStatus;
/*      */   protected boolean m_useIOProcess;
/*      */ 
/*      */   public AdminService()
/*      */   {
/*   78 */     this.m_allServers = null;
/*      */ 
/*   82 */     this.m_useIOProcess = false;
/*      */   }
/*      */ 
/*      */   public void initDelegatedObjects()
/*      */     throws DataException, ServiceException
/*      */   {
/*   90 */     super.initDelegatedObjects();
/*   91 */     this.m_allServers = ((AdminServerData)SharedObjects.getTable("ServerDefinition"));
/*   92 */     if (this.m_allServers != null)
/*      */     {
/*   94 */       AdminServerUtils.setupConfigFileStoresForAllServers(this.m_allServers);
/*      */     }
/*   96 */     AdminServerUtils.initAdminDataBinder(this.m_binder, this);
/*   97 */     if ((AdminServerUtils.m_isSimplifiedServer) && (AdminServerUtils.m_directActionsInterface != null))
/*      */     {
/*   99 */       loadSimplifiedServerStatus();
/*      */     }
/*  101 */     if (this.m_binder.getLocal("testMemory") == null)
/*      */       return;
/*      */     try
/*      */     {
/*  105 */       Class c = Class.forName("TestClassLoader");
/*  106 */       Object o = c.newInstance();
/*  107 */       ClassHelperUtils.executeMethod(o, "testClassLoader", null, null);
/*  108 */       for (int i = 0; i < 50; ++i)
/*      */       {
/*      */         try
/*      */         {
/*  112 */           Thread.sleep(100L);
/*      */         }
/*      */         catch (Exception ignore)
/*      */         {
/*      */         }
/*      */ 
/*  118 */         System.gc();
/*  119 */         if (i % 10 != 0)
/*      */           continue;
/*  121 */         System.runFinalization();
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/*  127 */       Report.trace(null, "Test memory failed", ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void loadSimplifiedServerStatus()
/*      */     throws ServiceException
/*      */   {
/*  134 */     String action = "getServerStatus";
/*  135 */     executeSimplifiedServerAdminAction(action);
/*      */   }
/*      */ 
/*      */   public void executeSimplifiedServerAdminAction(String action) throws ServiceException
/*      */   {
/*  140 */     this.m_administratedServerStatus = new HashMap();
/*  141 */     Map in = new HashMap();
/*      */     try
/*      */     {
/*  144 */       AdminServerUtils.m_directActionsInterface.doAction(action, in, this.m_administratedServerStatus, this);
/*  145 */       Report.trace("servlet", new StringBuilder().append("administratedServerStatus={").append(this.m_administratedServerStatus).append("}").toString(), null);
/*  146 */       if (getBooleanValue(this.m_administratedServerStatus, "needsRestart", false))
/*      */       {
/*  148 */         updateInternalRestartRequiredState("", "true");
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  153 */       throw new ServiceException(e, "csServletAdminFailedAction", new Object[] { action });
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean getBooleanValue(Map in, String key, boolean defVal)
/*      */   {
/*  159 */     if (in == null)
/*      */     {
/*  161 */       return defVal;
/*      */     }
/*  163 */     Object val = in.get(key);
/*  164 */     return ScriptUtils.convertObjectToBool(val, defVal);
/*      */   }
/*      */ 
/*      */   public void setAdminServerData(AdminServerData data)
/*      */   {
/*  173 */     this.m_allServers = data;
/*      */   }
/*      */ 
/*      */   public void initFileStoreObjects()
/*      */     throws DataException, ServiceException
/*      */   {
/*  182 */     if (null == ConfigFileLoader.m_defaultCFU)
/*      */     {
/*  184 */       Report.trace("system", "WARNING: AdminService created without a ConfigFileStore", null);
/*      */ 
/*  186 */       super.initFileStoreObjects();
/*  187 */       return;
/*      */     }
/*  189 */     this.m_CFU = ConfigFileLoader.m_defaultCFU;
/*  190 */     if (this != this.m_CFU.m_context)
/*      */     {
/*  192 */       this.m_CFU = ConfigFileUtilities.createConfigFileUtilities(this.m_CFU.m_CFS, this);
/*      */     }
/*      */ 
/*  195 */     this.m_useIOProcess = ((!AdminServerUtils.m_isSimplifiedServer) && (EnvUtils.isFamily("unix")));
/*  196 */     String ioOverride = this.m_binder.getLocal("useIOProcess");
/*  197 */     this.m_useIOProcess = StringUtils.convertToBool(ioOverride, this.m_useIOProcess);
/*      */   }
/*      */ 
/*      */   public void doActions()
/*      */     throws ServiceException
/*      */   {
/*  207 */     String server = getServerId();
/*  208 */     String thisAdminServer = SharedObjects.getEnvironmentValue("IDC_Admin_Name");
/*  209 */     String thisClusterNodeName = SharedObjects.getEnvironmentValue("ClusterNodeName");
/*  210 */     if ((server != null) && (server.length() > 0) && (this.m_allServers != null))
/*      */     {
/*  212 */       Exception error = null;
/*      */       try
/*      */       {
/*  215 */         String adminServer = ResultSetUtils.findValue(this.m_allServers, "IDC_Id", server, "IDC_Admin_Name");
/*      */ 
/*  217 */         String clusterNodeName = ResultSetUtils.findValue(this.m_allServers, "IDC_Id", server, "ClusterNodeName");
/*      */ 
/*  220 */         boolean allowProxy = true;
/*  221 */         String idcService = this.m_binder.getLocal("IdcService");
/*  222 */         for (int i = 0; i < NOT_PROXIED_SERVICES.length; ++i)
/*      */         {
/*  224 */           if (!idcService.equals(NOT_PROXIED_SERVICES[i]))
/*      */             continue;
/*  226 */           allowProxy = false;
/*  227 */           break;
/*      */         }
/*      */ 
/*  231 */         boolean requiresRemote = requestRequiresRemoteServer(adminServer, clusterNodeName);
/*      */ 
/*  233 */         if ((requiresRemote) && (allowProxy))
/*      */         {
/*  235 */           String myId = new StringBuilder().append(thisAdminServer).append("-").append(thisClusterNodeName).toString();
/*  236 */           this.m_binder.putLocal("IDC_Admin_Name", adminServer);
/*  237 */           this.m_binder.putLocal("ClusterNodeName", clusterNodeName);
/*  238 */           String sourceId = this.m_binder.getLocal("SourceServerId");
/*  239 */           if ((sourceId != null) && (sourceId.equals(myId)))
/*      */           {
/*  242 */             String msg = LocaleUtils.encodeMessage("csAdminLoopDetected", null, sourceId);
/*      */ 
/*  244 */             throw new ServiceException(msg);
/*      */           }
/*  246 */           String targetId = this.m_binder.getLocal("TargetServerId");
/*  247 */           if ((targetId != null) && (!SharedObjects.getEnvValueAsBoolean("AdminAllowMultihopCluster", false)) && (!targetId.equals(myId)))
/*      */           {
/*  251 */             String msg = LocaleUtils.encodeMessage("csAdminMisconfiguredTarget", null, sourceId, targetId, myId);
/*      */ 
/*  254 */             throw new ServiceException(msg);
/*      */           }
/*  256 */           requestRemoteAdminService();
/*      */         }
/*      */         else
/*      */         {
/*  260 */           super.doActions();
/*  261 */           return;
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  266 */         error = e;
/*  267 */         Report.trace("system", null, e);
/*      */       }
/*  269 */       if (error != null)
/*  270 */         createServiceException(error, null);
/*      */     }
/*      */     else
/*      */     {
/*  274 */       super.doActions();
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void alterProcess()
/*      */     throws DataException, ServiceException
/*      */   {
/*  285 */     String action = this.m_binder.getLocal("Action");
/*  286 */     String user = this.m_binder.getLocal("dUser");
/*      */ 
/*  289 */     if (AdminServerUtils.m_isSimplifiedServer)
/*      */     {
/*  292 */       Report.info(null, null, "csAdminUserExecutionActionOnCompanionServer", new Object[] { user, action });
/*  293 */       if (AdminServerUtils.m_directActionsInterface != null)
/*      */       {
/*  295 */         alterSimplifiedServerState(action);
/*      */       }
/*      */       else
/*      */       {
/*  299 */         throw new ServiceException(null, "csAdminServerActionNotSupported", new Object[] { action });
/*      */       }
/*  301 */       return;
/*      */     }
/*      */ 
/*  304 */     String server = this.m_binder.getLocal("IDC_Id");
/*      */ 
/*  307 */     Report.info(null, null, "csAdminUserExecutedAction", new Object[] { user, action, server });
/*      */ 
/*  309 */     String mixedCaseOutput = alterProcess(action, server, null);
/*  310 */     String output = mixedCaseOutput.toLowerCase();
/*      */ 
/*  312 */     if (output.indexOf("success") < 0)
/*      */     {
/*  314 */       IdcMessage idcMessage = IdcMessageFactory.lc("csAdminUnableToExecAction", new Object[] { action, server });
/*  315 */       Report.info(null, null, idcMessage);
/*  316 */       createServiceException(null, LocaleUtils.encodeMessage(idcMessage));
/*      */     }
/*      */ 
/*  320 */     String status = output.substring(output.lastIndexOf(":") + 2).trim();
/*  321 */     if ((action.equalsIgnoreCase("start")) || (action.equalsIgnoreCase("stop")) || (action.equalsIgnoreCase("restart")))
/*      */     {
/*  325 */       setRestartRequired(server, "false");
/*  326 */       this.m_binder.putLocal("serverStatus", status);
/*      */     }
/*  328 */     else if (action.equalsIgnoreCase("query"))
/*      */     {
/*  330 */       this.m_binder.putLocal("serverStatus", status);
/*      */     }
/*      */ 
/*  334 */     this.m_binder.putLocal("processOutput", mixedCaseOutput);
/*      */ 
/*  336 */     Report.info(null, null, mixedCaseOutput, new Object[0]);
/*      */   }
/*      */ 
/*      */   public void alterSimplifiedServerState(String adminServerAction) throws ServiceException
/*      */   {
/*  341 */     adminServerAction = adminServerAction.toLowerCase();
/*  342 */     boolean isRestart = adminServerAction.equals("restart");
/*  343 */     boolean doingStop = (adminServerAction.equals("stop")) || (isRestart);
/*  344 */     boolean doingStart = (adminServerAction.equals("start")) || (isRestart);
/*  345 */     boolean isSuccess = true;
/*      */ 
/*  348 */     if (doingStop)
/*      */     {
/*  350 */       executeSimplifiedServerAdminAction("stopServer");
/*  351 */       isSuccess = getBooleanValue(this.m_administratedServerStatus, "isSuccess", false);
/*      */     }
/*  353 */     if ((doingStart) && (isSuccess) && 
/*  355 */       (getBooleanValue(this.m_administratedServerStatus, "isSuccess", false)))
/*      */     {
/*  357 */       executeSimplifiedServerAdminAction("startServer");
/*  358 */       isSuccess = getBooleanValue(this.m_administratedServerStatus, "isSuccess", false);
/*      */     }
/*      */ 
/*  361 */     if (!isSuccess)
/*      */     {
/*  363 */       boolean isError = getBooleanValue(this.m_administratedServerStatus, "isError", false);
/*  364 */       boolean isRunning = getBooleanValue(this.m_administratedServerStatus, "isRunning", false);
/*  365 */       boolean isStarting = getBooleanValue(this.m_administratedServerStatus, "isStarting", false);
/*  366 */       Throwable t = (Throwable)this.m_administratedServerStatus.get("exception");
/*  367 */       IdcMessage msg = null;
/*  368 */       if ((doingStop) && (!isRunning))
/*      */       {
/*  370 */         msg = IdcMessageFactory.lc("csServletAdminAlreadyStopped", new Object[0]);
/*      */       }
/*  372 */       else if ((!isError) && (doingStart) && (((isRunning) || (isStarting))))
/*      */       {
/*  374 */         msg = IdcMessageFactory.lc("csServletAdminAlreadyStarted", new Object[0]);
/*      */       }
/*  376 */       if (msg == null)
/*      */       {
/*  378 */         msg = IdcMessageFactory.lc("csServletAdminFailedAction", new Object[] { adminServerAction });
/*      */       }
/*  380 */       msg.m_throwable = t;
/*  381 */       createServiceException(msg);
/*      */     }
/*      */ 
/*  386 */     String curStatus = determineSimplifiedServerStatus();
/*  387 */     this.m_binder.putLocal("serverStatus", curStatus);
/*      */   }
/*      */ 
/*      */   protected String alterProcess(String action, String server, IdcMessageContainer statusMsg)
/*      */     throws DataException, ServiceException
/*      */   {
/*  398 */     Properties serverProps = this.m_allServers.getLocalData(server);
/*  399 */     synchronized (serverProps)
/*      */     {
/*  401 */       String[] commandArray = makeCommandArray(action, server);
/*      */ 
/*  404 */       byte[] buf = new byte[2048];
/*  405 */       int nread = 0;
/*  406 */       ProcessLogger logger = null;
/*      */       try
/*      */       {
/*  409 */         Runtime runner = Runtime.getRuntime();
/*  410 */         Process process = runner.exec(commandArray);
/*  411 */         logger = new ProcessLogger(process);
/*  412 */         IdcStringBuilder builder = new IdcStringBuilder("Problem executing command:");
/*  413 */         for (String arg : commandArray)
/*      */         {
/*  415 */           builder.append2(' ', arg);
/*      */         }
/*  417 */         logger.setHeaderMessage(builder.toString());
/*  418 */         logger.setTraceSection("admin");
/*      */ 
/*  420 */         process.getOutputStream().close();
/*  421 */         InputStream procIn = logger.getLinkedStdoutStream(0);
/*      */         try
/*      */         {
/*  424 */           while ((nread = procIn.read(buf)) > 0)
/*      */           {
/*  426 */             logger.writeToLog(new String(buf, 0, nread));
/*      */           }
/*      */         }
/*      */         finally
/*      */         {
/*  431 */           FileUtils.closeObject(procIn);
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  436 */         int rc = -1;
/*  437 */         Throwable se = e;
/*  438 */         while (se != null)
/*      */         {
/*  440 */           if (se instanceof ServiceException) {
/*      */             break;
/*      */           }
/*      */ 
/*  444 */           se = se.getCause();
/*      */         }
/*      */ 
/*  447 */         if (se != null)
/*      */         {
/*  449 */           IdcMessage m = ((ServiceException)se).getIdcMessage();
/*  450 */           while (m != null)
/*      */           {
/*  452 */             if ("syProcessErrorCode".equals(m.m_stringKey))
/*      */             {
/*  454 */               rc = NumberUtils.parseInteger(m.m_args[0].toString(), -2);
/*  455 */               break;
/*      */             }
/*  457 */             m = m.m_prior;
/*      */           }
/*      */         }
/*  460 */         String commandString = "";
/*  461 */         for (int i = 0; i < commandArray.length; ++i)
/*      */         {
/*  463 */           commandString = new StringBuilder().append(commandString).append(commandArray[i]).toString();
/*  464 */           commandString = new StringBuilder().append(commandString).append(" ").toString();
/*      */         }
/*  466 */         if (rc == 0)
/*      */         {
/*  468 */           if (statusMsg != null)
/*      */           {
/*  470 */             IdcMessage msg = new IdcMessage("csAdminExecErrors", new Object[] { commandString });
/*  471 */             msg.m_prior = new IdcMessage(e);
/*  472 */             statusMsg.setIdcMessage(msg);
/*      */           }
/*      */           else
/*      */           {
/*  476 */             throw new ServiceException(e, "csAdminExecErrors", new Object[] { commandString });
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  483 */         throw new ServiceException(e, "csAdminUnableToExec", new Object[] { commandString });
/*      */       }
/*      */ 
/*  488 */       return logger.getOutputString();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean processIsRunning(Process p)
/*      */   {
/*      */     try
/*      */     {
/*  501 */       p.exitValue();
/*  502 */       return false;
/*      */     }
/*      */     catch (IllegalThreadStateException e) {
/*      */     }
/*  506 */     return true;
/*      */   }
/*      */ 
/*      */   protected String[] makeCommandArray(String action, String server)
/*      */     throws ServiceException, DataException
/*      */   {
/*  518 */     if ((server == null) && (action.equals("read")))
/*      */     {
/*  520 */       this.m_binder.putLocal("processController", "bin/UnixProcCtrl");
/*      */     }
/*      */     else
/*      */     {
/*  524 */       if (!this.m_allServers.validateAction(server, action))
/*      */       {
/*  526 */         String msg = LocaleUtils.encodeMessage("csAdminActionNotAllowed", null, action, server);
/*      */ 
/*  528 */         createServiceException(null, msg);
/*      */       }
/*      */ 
/*  533 */       String serverName = this.m_binder.getLocal("IDC_Id");
/*  534 */       if (serverName == null)
/*      */       {
/*  536 */         throw new DataException("!csAdminIdcNameMissing");
/*      */       }
/*  538 */       Properties localProps = this.m_binder.getLocalData();
/*  539 */       Properties serverProps = this.m_allServers.getLocalData(serverName);
/*  540 */       if (serverProps == null)
/*      */       {
/*  542 */         serverProps = localProps;
/*      */       }
/*      */       else
/*      */       {
/*  546 */         DataBinder.mergeHashTables(serverProps, localProps);
/*      */       }
/*  548 */       this.m_binder.setLocalData(serverProps);
/*      */     }
/*      */ 
/*  552 */     String ctrl = this.m_binder.getLocal("processController");
/*  553 */     if (ctrl == null)
/*      */     {
/*  555 */       throw new DataException("!csAdminProcCtrlNotSpecified");
/*      */     }
/*  557 */     ctrl = FileUtils.getAbsolutePath(SharedObjects.getEnvironmentValue("AdminDir"), ctrl);
/*  558 */     if (FileUtils.checkFile(ctrl, true, false) < 0)
/*      */     {
/*  560 */       String msg = LocaleUtils.encodeMessage("csAdminUnableToExec2", null, ctrl);
/*      */ 
/*  562 */       createServiceException(null, msg);
/*      */     }
/*  564 */     this.m_binder.putLocal("processController", ctrl);
/*      */ 
/*  567 */     String command = this.m_allServers.getActionValue(action, "actionCommand");
/*  568 */     if (command == null)
/*      */     {
/*  570 */       String msg = LocaleUtils.encodeMessage("csAdminSpecifiedActionNotDefined", null, action, server);
/*      */ 
/*  572 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/*  576 */     ParseOutput parseOutput = null;
/*      */     try
/*      */     {
/*  579 */       DynamicHtml htmlCommand = new DynamicHtml();
/*  580 */       htmlCommand.loadHtmlInContext(new StringReader(command), parseOutput = new ParseOutput());
/*  581 */       PageMerger pm = new PageMerger(this.m_binder, null);
/*  582 */       command = pm.createMergedPage(htmlCommand);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  586 */       createServiceException(e, "!csAdminUnableToCreateCommandLine");
/*      */     }
/*      */     finally
/*      */     {
/*  590 */       parseOutput.releaseBuffers();
/*      */     }
/*      */ 
/*  594 */     List commandList = null;
/*      */     try
/*      */     {
/*  597 */       commandList = StringUtils.makeListFromEscapedString(command);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  601 */       createServiceException(e, "!csAdminUnableToCreateCommandLine");
/*      */     }
/*      */ 
/*  605 */     String[] commandArray = new String[commandList.size()];
/*  606 */     commandList.toArray(commandArray);
/*  607 */     Report.trace("idcadmin", new StringBuilder().append("Executing command: ").append(commandList.toString()).toString(), null);
/*  608 */     return commandArray;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadAdminOutput()
/*      */   {
/*  618 */     String output = " ";
/*  619 */     String clear = this.m_binder.getLocal("ClearOutput");
/*  620 */     if (StringUtils.convertToBool(clear, false))
/*      */     {
/*  622 */       IdcServerOutput.clearOutput();
/*      */     }
/*      */     else
/*      */     {
/*  626 */       output = IdcServerOutput.viewOutput();
/*      */     }
/*  628 */     this.m_binder.putLocal("AdminOutput", output);
/*  629 */     this.m_binder.putLocal("CleanAdminOutput", StringUtils.createErrorStringForBrowser(output));
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadServerOutput()
/*      */   {
/*  640 */     String output = " ";
/*  641 */     String clear = this.m_binder.getLocal("ClearOutput");
/*  642 */     if (StringUtils.convertToBool(clear, false))
/*      */     {
/*  644 */       IdcServerOutput.clearOutput();
/*      */     }
/*      */     else
/*      */     {
/*  648 */       output = IdcServerOutput.viewOutput();
/*      */     }
/*  650 */     this.m_binder.putLocal("ServerOutput", output);
/*      */   }
/*      */ 
/*      */   public void loadLoggedServerOutput()
/*      */     throws ServiceException
/*      */   {
/*  657 */     String dir = SharedObjects.getEnvironmentValue("EventDirectory");
/*  658 */     if (dir == null)
/*      */     {
/*  660 */       dir = SharedObjects.getEnvironmentValue("BaseLogDir");
/*  661 */       dir = new StringBuilder().append(dir).append("/trace/event").toString();
/*      */     }
/*      */ 
/*  664 */     File f = new File(dir);
/*  665 */     if (f.isDirectory())
/*      */     {
/*  667 */       File[] files = f.listFiles();
/*  668 */       ArrayList fileNames = new ArrayList();
/*  669 */       for (int i = 0; i < files.length; ++i)
/*      */       {
/*  671 */         fileNames.add(files[i].getName());
/*      */       }
/*  673 */       SortUtils.sortStringList(fileNames, false);
/*      */ 
/*  675 */       DataResultSet rset = new DataResultSet(new String[] { "product", "time", "logFile" });
/*  676 */       Pattern p = Pattern.compile("(.*)_([0-9]*|current).log");
/*  677 */       for (String fileName : fileNames)
/*      */       {
/*  679 */         Vector row = new IdcVector();
/*  680 */         Matcher m = p.matcher(fileName);
/*  681 */         if (!m.matches())
/*      */         {
/*  683 */           Report.trace(null, new StringBuilder().append("fileName ").append(fileName).append(" doesn't match filename pattern.").toString(), null);
/*      */ 
/*  685 */           row.add(fileName);
/*  686 */           row.add("");
/*      */         }
/*      */         else
/*      */         {
/*  690 */           row.add(m.group(1));
/*  691 */           row.add(m.group(2));
/*      */         }
/*  693 */         row.add(fileName);
/*  694 */         rset.addRow(row);
/*      */       }
/*  696 */       this.m_binder.addResultSetDirect("TraceLogs", rset);
/*      */     }
/*      */ 
/*  699 */     String fileName = this.m_binder.getLocal("fileName");
/*  700 */     if (fileName == null)
/*      */     {
/*  702 */       String prefix = new StringBuilder().append(SharedObjects.getEnvironmentValue("IdcProductName")).append("_").toString();
/*  703 */       String clusterNodeName = SharedObjects.getEnvironmentValue("IDC_Id");
/*  704 */       if (clusterNodeName != null)
/*      */       {
/*  706 */         prefix = new StringBuilder().append(prefix).append(clusterNodeName).append("_").toString();
/*      */       }
/*  708 */       fileName = new StringBuilder().append(prefix).append("current.log").toString();
/*  709 */       this.m_binder.putLocal("fileName", fileName);
/*      */     }
/*      */ 
/*  712 */     f = new File(dir, fileName);
/*  713 */     BufferedReader r = null;
/*      */     try
/*      */     {
/*  716 */       IdcStringBuilder builder = new IdcStringBuilder();
/*  717 */       if (f.exists())
/*      */       {
/*  719 */         r = new BufferedReader(new FileReader(f));
/*  720 */         char[] buf = new char[1024];
/*      */ 
/*  722 */         while ((len = r.read(buf)) > 0)
/*      */         {
/*      */           int len;
/*  724 */           builder.append(buf, 0, len);
/*      */         }
/*      */       }
/*  727 */       this.m_binder.putLocal("ServerOutput", builder.toString());
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  735 */       FileUtils.closeObject(r);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void requestRemoteAdminService()
/*      */     throws ServiceException, DataException
/*      */   {
/*  747 */     doRequestRemoteAdminService(this.m_binder, false);
/*      */   }
/*      */ 
/*      */   public boolean doRequestRemoteAdminService(DataBinder data, boolean suppressError)
/*      */     throws ServiceException, DataException
/*      */   {
/*  757 */     Properties connectionProps = data.getLocalData();
/*  758 */     String idcAdminName = data.getLocal("IDC_Admin_Name");
/*  759 */     String clusterNodeName = data.getLocal("ClusterNodeName");
/*  760 */     String idcAdminId = idcAdminName;
/*  761 */     if ((clusterNodeName != null) && (clusterNodeName.length() > 0)) {
/*  762 */       idcAdminId = new StringBuilder().append(idcAdminId).append("-").append(clusterNodeName).toString();
/*      */     }
/*  764 */     String sourceServerId = SharedObjects.getEnvironmentValue("IDC_Admin_Name");
/*  765 */     String sourceClusterName = SharedObjects.getEnvironmentValue("ClusterNodeName");
/*  766 */     if (sourceClusterName != null)
/*  767 */       sourceServerId = new StringBuilder().append(sourceServerId).append("-").append(sourceClusterName).toString();
/*  768 */     data.putLocal("SourceServerId", sourceServerId);
/*      */ 
/*  770 */     if ((idcAdminId != null) && (idcAdminId.trim().length() > 0))
/*      */     {
/*  772 */       String errorKey = null;
/*  773 */       String error = null;
/*  774 */       DataResultSet adminServers = (DataResultSet)this.m_allServers.m_serverData.getResultSet("AdminServers");
/*      */ 
/*  777 */       if (adminServers == null)
/*      */       {
/*  779 */         error = "!csNoAdminServerConfiguration";
/*  780 */         errorKey = error;
/*  781 */         if (!suppressError) {
/*  782 */           createServiceException(null, error);
/*      */         }
/*      */       }
/*  785 */       int idcAdminIdInd = ResultSetUtils.getIndexMustExist(adminServers, "IDC_Admin_Id");
/*  786 */       Vector v = adminServers.findRow(idcAdminIdInd, idcAdminId);
/*  787 */       if (v == null)
/*      */       {
/*  789 */         error = LocaleUtils.encodeMessage("csAdminNoAdminServerDefined", null, idcAdminId);
/*  790 */         errorKey = error;
/*  791 */         if (!suppressError)
/*  792 */           createServiceException(null, error);
/*      */       }
/*  794 */       connectionProps = adminServers.getCurrentRowProps();
/*      */ 
/*  796 */       if (error != null)
/*      */       {
/*  798 */         data.putLocal("StatusMessageKey", errorKey);
/*  799 */         data.putLocal("StatusMessage", error);
/*  800 */         data.putLocal("StatusCode", "-1");
/*  801 */         return false;
/*      */       }
/*  803 */       data.putLocal("TargetServerId", idcAdminId);
/*      */     }
/*      */ 
/*  809 */     String targetAddress = connectionProps.getProperty("SocketServerAddress");
/*  810 */     if ((targetAddress == null) || (targetAddress.length() == 0))
/*      */     {
/*  812 */       targetAddress = connectionProps.getProperty("HttpServerAddress");
/*      */     }
/*  814 */     connectionProps.put("IntradocServerHostName", targetAddress);
/*      */ 
/*  816 */     DataBinder prData = new DataBinder((Properties)SharedObjects.getSecureEnvironment().clone());
/*  817 */     prData.setLocalData(connectionProps);
/*  818 */     SocketOutgoingProvider op = createOutgoingProvider(prData);
/*      */ 
/*  820 */     String oldIdcService = data.getLocal("IdcService");
/*  821 */     String idcService = data.getLocal("RemoteIdcService");
/*  822 */     if ((idcService != null) && (idcService.trim().length() > 0)) {
/*  823 */       data.putLocal("IdcService", idcService);
/*      */     }
/*  825 */     ServerRequest sr = null;
/*  826 */     String errorMsgKey = null;
/*  827 */     String errorMsg = null;
/*  828 */     Exception error = null;
/*      */     try
/*      */     {
/*  831 */       String hostName = connectionProps.getProperty("HttpServerAddress");
/*  832 */       String remoteUser = SharedObjects.getEnvironmentValue("RemoteAdminServiceUser");
/*  833 */       if ((remoteUser == null) || (remoteUser.trim().length() == 0))
/*  834 */         remoteUser = "sysadmin";
/*  835 */       String remoteUserRoles = SharedObjects.getEnvironmentValue("RemoteAdminServiceUserRoles");
/*  836 */       if ((remoteUserRoles == null) || (remoteUserRoles.trim().length() == 0)) {
/*  837 */         remoteUserRoles = "admin,sysmanager";
/*      */       }
/*      */ 
/*  840 */       data.setEnvironmentValue("HTTP_HOST", hostName);
/*  841 */       data.setEnvironmentValue("REMOTE_USER", remoteUser);
/*  842 */       data.setEnvironmentValue("EXTERNAL_ROLES", remoteUserRoles);
/*      */ 
/*  844 */       if (SystemUtils.m_verbose)
/*      */       {
/*  846 */         Report.debug("idcadmin", new StringBuilder().append("Requesting remote service:\n").append(data.getLocalData().toString()).toString(), null);
/*      */ 
/*  848 */         Report.debug("idcadmin", new StringBuilder().append("HTTP_HOST=").append(hostName).append(", REMOTE_USER=").append(remoteUser).append(", EXTERNAL_ROLES=").append(remoteUserRoles).toString(), null);
/*      */       }
/*      */ 
/*  852 */       sr = op.createRequest();
/*  853 */       DataBinder requestBinder = data.createShallowCopy();
/*  854 */       sr.doRequest(requestBinder, data, this);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  859 */       error = e;
/*      */ 
/*  861 */       errorMsg = LocaleUtils.encodeMessage("csAdminUnableToExecRemoteService", error.getMessage(), idcService, idcAdminName);
/*      */ 
/*  863 */       errorMsgKey = errorMsg;
/*  864 */       Report.trace("idcadmin", errorMsg, error);
/*      */     }
/*      */     finally
/*      */     {
/*  868 */       if (sr != null)
/*      */       {
/*  870 */         sr.closeRequest(this);
/*      */       }
/*      */     }
/*  873 */     data.putLocal("IdcService", oldIdcService);
/*      */ 
/*  876 */     if (errorMsg == null)
/*      */     {
/*  878 */       String statusCode = data.getLocal("StatusCode");
/*  879 */       if ((statusCode != null) && (Integer.valueOf(statusCode).intValue() < 0))
/*      */       {
/*  881 */         String statusMsg = data.getLocal("StatusMessage");
/*  882 */         String statusMsgKey = data.getLocal("StatusMessageKey");
/*  883 */         errorMsg = LocaleUtils.encodeMessage("csAdminContentServerReportedError", null, idcAdminName, statusMsg);
/*      */ 
/*  885 */         errorMsgKey = LocaleUtils.encodeMessage("csAdminContentServerReportedError", null, idcAdminName, statusMsgKey);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  891 */     if (errorMsg != null)
/*      */     {
/*  893 */       if (suppressError)
/*      */       {
/*  895 */         data.putLocal("StatusMessageKey", errorMsgKey);
/*  896 */         data.putLocal("StatusMessage", errorMsg);
/*  897 */         data.putLocal("StatusCode", "-1");
/*  898 */         return false;
/*      */       }
/*  900 */       createServiceException(error, errorMsg);
/*      */     }
/*  902 */     return true;
/*      */   }
/*      */ 
/*      */   protected SocketOutgoingProvider createOutgoingProvider(DataBinder prData)
/*      */     throws ServiceException
/*      */   {
/*  908 */     SocketOutgoingProvider sop = null;
/*      */ 
/*  911 */     String hostName = prData.getLocal("SocketServerAddress");
/*  912 */     if ((hostName == null) || (hostName.trim().length() == 0))
/*  913 */       hostName = prData.getLocal("HttpServerAddress");
/*  914 */     if ((hostName == null) || (hostName.trim().length() == 0))
/*  915 */       createServiceException(null, "!csNoHostNameSuppliedForRemoteAdminServerRequest");
/*  916 */     prData.putLocal("HttpServerAddress", hostName);
/*      */ 
/*  919 */     String port = prData.getLocal("ServerPort");
/*  920 */     if (port == null)
/*      */     {
/*  922 */       port = prData.getLocal("IdcAdminServerPort");
/*  923 */       int portNum = NumberUtils.parseInteger(port, 4440);
/*  924 */       port = String.valueOf(portNum);
/*      */     }
/*  926 */     prData.putLocal("ServerPort", port);
/*      */     try
/*      */     {
/*  930 */       Provider pr = new Provider(prData);
/*  931 */       String className = prData.getAllowMissing("AdminServerSocketOutgoingProvider");
/*  932 */       if (className == null) {
/*  933 */         className = "intradoc.provider.SocketOutgoingProvider";
/*      */       }
/*  935 */       sop = (SocketOutgoingProvider)Class.forName(className).newInstance();
/*  936 */       sop.init(pr);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  940 */       String msg = "!csUnableToInitializeAdminServerOutgoingProvider";
/*  941 */       Report.error(null, msg, e);
/*  942 */       createServiceException(null, msg);
/*      */     }
/*  944 */     return sop;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void requestRemoteService()
/*      */     throws ServiceException, DataException
/*      */   {
/*  955 */     String idcName = this.m_binder.get("IDC_Id");
/*  956 */     String targetName = idcName;
/*  957 */     if ((targetName == null) || (targetName.length() == 0))
/*      */     {
/*  961 */       targetName = this.m_binder.get("IDC_Name");
/*      */     }
/*  963 */     String host = this.m_allServers.getValueMustExist(idcName, "HttpServerAddress");
/*  964 */     String port = this.m_allServers.getValueMustExist(idcName, "IntradocServerPort");
/*      */ 
/*  973 */     boolean allowSplitAddress = SharedObjects.getEnvValueAsBoolean("AllowAdminSplitSocketAddress", false);
/*      */ 
/*  975 */     if (allowSplitAddress)
/*      */     {
/*  977 */       String masterAddress = SharedObjects.getEnvironmentValue("HttpServerAddress");
/*  978 */       allowSplitAddress = (masterAddress == null) || (!masterAddress.equals(host));
/*      */     }
/*  980 */     if (!allowSplitAddress)
/*      */     {
/*  982 */       String masterDirectAddress = SharedObjects.getEnvironmentValue("IntradocServerHostName");
/*  983 */       if (masterDirectAddress == null)
/*      */       {
/*  985 */         masterDirectAddress = "localhost";
/*      */       }
/*  987 */       host = masterDirectAddress;
/*      */     }
/*      */ 
/*  990 */     int portNum = NumberUtils.parseInteger(port, 4444);
/*  991 */     port = String.valueOf(portNum);
/*      */ 
/*  993 */     DataBinder prData = new DataBinder();
/*  994 */     prData.putLocal("ServerPort", port);
/*  995 */     prData.putLocal("SocketServerAddress", host);
/*  996 */     SocketOutgoingProvider sop = createOutgoingProvider(prData);
/*      */ 
/*  998 */     String newService = this.m_currentAction.getParamAt(0);
/*  999 */     Properties originalLocalData = (Properties)this.m_binder.getLocalData().clone();
/* 1000 */     DataBinder requestBinder = this.m_binder.createShallowCopy();
/* 1001 */     requestBinder.putLocal("IdcService", newService);
/*      */ 
/* 1003 */     ServerRequest sr = null;
/*      */     try
/*      */     {
/* 1006 */       this.m_binder.setEnvironmentValue("PROXY_USER", "sysadmin");
/* 1007 */       sr = sop.createRequest();
/* 1008 */       sr.doRequest(requestBinder, this.m_binder, this);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1012 */       String msg = LocaleUtils.encodeMessage("csAdminUnableToExecService", null, newService, targetName);
/*      */ 
/* 1014 */       createServiceException(e, msg);
/*      */     }
/*      */     finally
/*      */     {
/* 1019 */       copyLocalValues(new String[] { "StatusCode", "StatusMessage" }, this.m_binder, originalLocalData);
/*      */ 
/* 1022 */       this.m_binder.mergeHashTablesInternal(this.m_binder.getLocalData(), originalLocalData, this.m_binder, false);
/*      */ 
/* 1024 */       if (sr != null)
/*      */       {
/* 1026 */         sr.closeRequest(this);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1031 */     String statusCode = this.m_binder.getLocal("StatusCode");
/* 1032 */     if ((statusCode == null) || (Integer.valueOf(statusCode).intValue() >= 0)) {
/*      */       return;
/*      */     }
/* 1035 */     String statusMsg = this.m_binder.getLocal("StatusMessage");
/* 1036 */     String msg = LocaleUtils.encodeMessage("csAdminContentServerReportedError", null, idcName, statusMsg);
/*      */ 
/* 1038 */     createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   public void copyLocalValues(String[] list, DataBinder binder, Map props)
/*      */   {
/* 1044 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/* 1046 */       String val = binder.getLocal(list[i]);
/* 1047 */       if (val == null)
/*      */         continue;
/* 1049 */       props.put(list[i], val);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean requestRequiresRemoteServer(String adminServerName, String clusterNodeName)
/*      */   {
/* 1061 */     String thisAdminServer = SharedObjects.getEnvironmentValue("IDC_Admin_Name");
/* 1062 */     String thisClusterNodeName = SharedObjects.getEnvironmentValue("ClusterNodeName");
/* 1063 */     Report.trace("idcadmin", new StringBuilder().append("requestRequiresRemoteServer() checking admin '").append(adminServerName).append("', node name '").append(clusterNodeName).append("'").toString(), null);
/*      */ 
/* 1065 */     boolean isSameAdmin = (adminServerName == null) || (adminServerName.trim().length() == 0) || (adminServerName.equals(thisAdminServer));
/*      */ 
/* 1067 */     boolean isSameNode = (clusterNodeName == null) || (clusterNodeName.trim().length() == 0) || (clusterNodeName.equals(thisClusterNodeName));
/*      */ 
/* 1069 */     return (!isSameAdmin) || (!isSameNode);
/*      */   }
/*      */ 
/*      */   public InputStream getReadStream(String file)
/*      */     throws IOException
/*      */   {
/* 1077 */     if (this.m_useIOProcess)
/*      */     {
/*      */       try
/*      */       {
/* 1081 */         this.m_binder.putLocal("fileName", file);
/* 1082 */         String[] cmd = makeCommandArray("read", getServerId());
/* 1083 */         this.m_binder.removeLocal("fileName");
/* 1084 */         return (InputStream)getProcessStream(cmd, 0);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1088 */         IOException ioe = new IOException();
/* 1089 */         ioe.initCause(e);
/* 1090 */         throw ioe;
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1094 */         IOException ioe = new IOException();
/* 1095 */         ioe.initCause(e);
/* 1096 */         throw ioe;
/*      */       }
/*      */     }
/* 1099 */     return new FileInputStream(file);
/*      */   }
/*      */ 
/*      */   public OutputStream getWriteStream(String file)
/*      */     throws IOException
/*      */   {
/* 1107 */     if (this.m_useIOProcess)
/*      */     {
/*      */       try
/*      */       {
/* 1111 */         this.m_binder.putLocal("fileName", file);
/* 1112 */         String[] cmd = makeCommandArray("write", getServerId());
/* 1113 */         this.m_binder.removeLocal("fileName");
/* 1114 */         return (OutputStream)getProcessStream(cmd, 1);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1118 */         IOException ioe = new IOException();
/* 1119 */         ioe.initCause(e);
/* 1120 */         throw ioe;
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1124 */         IOException ioe = new IOException();
/* 1125 */         ioe.initCause(e);
/* 1126 */         throw ioe;
/*      */       }
/*      */     }
/* 1129 */     return FileUtilsCfgBuilder.getCfgOutputStream(file, null);
/*      */   }
/*      */ 
/*      */   protected Object getProcessStream(String[] commandArray, int streamNum)
/*      */     throws ServiceException
/*      */   {
/* 1143 */     Object stream = null;
/*      */     try
/*      */     {
/* 1146 */       Runtime runner = Runtime.getRuntime();
/* 1147 */       Process proc = runner.exec(commandArray);
/* 1148 */       ProcessLogger logger = new ProcessLogger(proc);
/* 1149 */       IdcStringBuilder builder = new IdcStringBuilder("Problem executing command:");
/* 1150 */       for (String arg : commandArray)
/*      */       {
/* 1152 */         builder.append2(' ', arg);
/*      */       }
/* 1154 */       logger.setHeaderMessage(builder.toString());
/* 1155 */       logger.setTraceSection("admin");
/*      */ 
/* 1157 */       switch (streamNum)
/*      */       {
/*      */       case 0:
/* 1160 */         proc.getOutputStream().close();
/* 1161 */         stream = logger.getLinkedStdoutStream(0);
/* 1162 */         break;
/*      */       case 1:
/* 1164 */         proc.getInputStream().close();
/* 1165 */         stream = logger.getLinkedStdinStream(0);
/* 1166 */         break;
/*      */       default:
/* 1168 */         throw new AssertionError();
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1173 */       createServiceException(e, "!csAdminExecError");
/*      */     }
/* 1175 */     return stream;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   protected void prepareComponentListEditor(ComponentListEditor compLE, String configDir, String compDir, String encoding, boolean doWrite)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1189 */     prepareComponentListEditor(compLE);
/*      */   }
/*      */ 
/*      */   protected void prepareComponentListEditor(ComponentListEditor compLE)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1200 */     this.m_binder.putLocal("fileName", "is testing of polluting of config");
/*      */ 
/* 1202 */     Map env = this.m_binder.getLocalData();
/*      */ 
/* 1204 */     String idcDir = this.m_binder.getLocal("IntradocDir");
/* 1205 */     String configDir = this.m_binder.getLocal("ConfigDir");
/* 1206 */     String compDir = this.m_binder.getLocal("ComponentsDataDir");
/* 1207 */     String homeDir = this.m_binder.getLocal("IdcHomeDir");
/*      */ 
/* 1209 */     compLE.setIO(this);
/* 1210 */     compLE.init(idcDir, configDir, compDir, homeDir, env);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadComponentData()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1220 */     loadComponentData(false);
/*      */   }
/*      */ 
/*      */   public void loadComponentData(boolean isToggle) throws ServiceException, DataException
/*      */   {
/* 1225 */     String server = getServerId();
/* 1226 */     if (server == null)
/*      */     {
/* 1228 */       throw new DataException("!csAdminIdcNameNotSpecified");
/*      */     }
/*      */ 
/* 1232 */     this.m_allServers.mergeLocalData(server, this.m_binder);
/*      */ 
/* 1234 */     String intradocDir = this.m_allServers.getValueMustExist(server, "IntradocDir");
/* 1235 */     String configDir = AdminDirectoryLocator.getConfigDir(this.m_binder);
/* 1236 */     String compDir = new StringBuilder().append(AdminDirectoryLocator.getAppDataDirectory(this.m_binder)).append("/components/").toString();
/*      */ 
/* 1239 */     String encoding = this.m_allServers.getValue(server, "FileEncoding");
/* 1240 */     if ((encoding == null) || (encoding.length() == 0))
/*      */     {
/* 1242 */       encoding = DataSerializeUtils.getSystemEncoding();
/*      */     }
/*      */ 
/* 1246 */     ComponentListEditor compLE = new ComponentListEditor();
/*      */ 
/* 1253 */     compLE.m_isAdminServer = true;
/* 1254 */     compLE.m_isListsOnly = true;
/*      */ 
/* 1256 */     this.m_binder.putLocal("IntradocDir", intradocDir);
/* 1257 */     this.m_binder.putLocal("ConfigDir", configDir);
/* 1258 */     this.m_binder.putLocal("ComponentsDataDir", compDir);
/* 1259 */     this.m_binder.putLocal("FileEncoding", encoding);
/*      */ 
/* 1261 */     prepareComponentListEditor(compLE);
/*      */ 
/* 1263 */     if (isToggle)
/*      */     {
/* 1265 */       if (DataBinderUtils.getBoolean(this.m_binder, "isSimple", false))
/*      */       {
/* 1267 */         String enableComponentList = this.m_binder.getLocal("EnableComponentList");
/* 1268 */         String disableComponentList = this.m_binder.getLocal("DisableComponentList");
/*      */ 
/* 1270 */         compLE.enableOrDisableComponent(enableComponentList, true);
/* 1271 */         compLE.enableOrDisableComponent(disableComponentList, false);
/*      */       }
/*      */       else
/*      */       {
/* 1275 */         boolean enable = StringUtils.convertToBool(this.m_binder.getLocal("isEnable"), true);
/*      */ 
/* 1277 */         String components = getComponentList();
/* 1278 */         compLE.enableOrDisableComponent(components, enable);
/*      */       }
/*      */ 
/* 1281 */       setRestartRequired(server, "true");
/*      */     }
/* 1283 */     storeComponentResultSets(compLE, intradocDir);
/* 1284 */     compLE.closeAllStreams();
/*      */   }
/*      */ 
/*      */   public String getServerId()
/*      */   {
/* 1289 */     if (AdminServerUtils.m_isSimplifiedServer)
/*      */     {
/* 1294 */       this.m_binder.putLocal("IDC_Id", "");
/* 1295 */       return "";
/*      */     }
/* 1297 */     return this.m_binder.getAllowMissing("IDC_Id");
/*      */   }
/*      */ 
/*      */   protected String getComponentList()
/*      */   {
/* 1302 */     String components = this.m_binder.getLocal("installedComponents");
/* 1303 */     if (components == null)
/*      */     {
/* 1305 */       components = this.m_binder.getLocal("ComponentNames");
/*      */     }
/* 1307 */     if (components == null)
/*      */     {
/* 1309 */       components = this.m_binder.getLocal("EnableComponentList");
/*      */     }
/* 1311 */     if ((components == null) || (components.length() == 0))
/*      */     {
/* 1313 */       components = this.m_binder.getLocal("DisableComponentList");
/*      */     }
/* 1315 */     return components;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void toggleComponents()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1325 */     String components = getComponentList();
/* 1326 */     if ((components == null) || (components.length() <= 0))
/*      */       return;
/* 1328 */     loadComponentData(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void contentServerSelfToggleComponents()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1335 */     UserData userData = getUserData();
/* 1336 */     if (!SecurityUtils.isUserOfRole(userData, "sysmanager"))
/*      */     {
/* 1338 */       String msg = LocaleUtils.encodeMessage("csInsufficientPrivilege", null);
/* 1339 */       createServiceException(null, msg);
/*      */     }
/* 1341 */     String enableComponentList = this.m_binder.getLocal("EnableComponentList");
/* 1342 */     String disableComponentList = this.m_binder.getLocal("DisableComponentList");
/*      */ 
/* 1344 */     ComponentListEditor compLE = new ComponentListEditor();
/* 1345 */     compLE.init(false);
/* 1346 */     compLE.enableOrDisableComponent(enableComponentList, true);
/* 1347 */     compLE.enableOrDisableComponent(disableComponentList, false);
/* 1348 */     this.m_binder.putLocal("StatusCode", "0");
/*      */   }
/*      */ 
/*      */   protected void storeComponentResultSets(ComponentListEditor compLE, String intradocDir)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1359 */     DataResultSet allComps = compLE.getComponentSet();
/*      */ 
/* 1362 */     DataResultSet enabledSet = new DataResultSet();
/* 1363 */     DataResultSet disabledSet = new DataResultSet();
/* 1364 */     DataResultSet editableSet = new DataResultSet();
/* 1365 */     DataResultSet downComps = new DataResultSet();
/*      */ 
/* 1367 */     DataResultSet descriptionSet = new DataResultSet(new String[] { "description" });
/* 1368 */     this.m_binder.m_blFieldTypes.put("description", "message");
/* 1369 */     allComps.mergeWithFlags(null, descriptionSet, 16, 0);
/* 1370 */     FieldInfo info = new FieldInfo();
/* 1371 */     allComps.getFieldInfo("description", info);
/* 1372 */     int counter = 0;
/* 1373 */     for (SimpleParameters params : allComps.getSimpleParametersIterable())
/*      */     {
/* 1375 */       String compName = params.get("name");
/* 1376 */       allComps.setCurrentRow(counter++);
/* 1377 */       allComps.setCurrentValue(info.m_index, new StringBuilder().append("!csCompDesc_").append(compName).toString());
/*      */     }
/*      */ 
/* 1380 */     enabledSet.copyFieldInfo(allComps);
/* 1381 */     disabledSet.copyFieldInfo(allComps);
/* 1382 */     editableSet.copyFieldInfo(allComps);
/* 1383 */     downComps.copyFieldInfo(allComps);
/*      */ 
/* 1386 */     DataResultSet tagSet = new DataResultSet(new String[] { "tag", "tagComponents" });
/*      */ 
/* 1389 */     DataResultSet legacyComponentTags = compLE.getLegacyTaggedComponentSet();
/* 1390 */     int nameIndex = -1;
/* 1391 */     int tagIndex = -1;
/* 1392 */     if (legacyComponentTags != null)
/*      */     {
/* 1394 */       nameIndex = ResultSetUtils.getIndexMustExist(legacyComponentTags, "componentName");
/* 1395 */       tagIndex = ResultSetUtils.getIndexMustExist(legacyComponentTags, "tags");
/*      */     }
/*      */ 
/* 1398 */     boolean buildBeforeDownload = SharedObjects.getEnvValueAsBoolean("BuildComponentBeforeDownload", false);
/*      */ 
/* 1400 */     for (allComps.first(); allComps.isRowPresent(); allComps.next())
/*      */     {
/* 1402 */       Map map = allComps.getCurrentRowMap();
/* 1403 */       Vector row = allComps.getCurrentRowValues();
/*      */ 
/* 1405 */       String name = (String)map.get("name");
/* 1406 */       String status = (String)map.get("status");
/* 1407 */       String installID = (String)map.get("installID");
/* 1408 */       String tags = (String)map.get("componentTags");
/* 1409 */       boolean hasPreferenceData = StringUtils.convertToBool((String)map.get("hasPreferenceData"), false);
/*      */ 
/* 1411 */       if (status.equalsIgnoreCase("disabled"))
/*      */       {
/* 1413 */         disabledSet.addRow((Vector)row.clone());
/*      */       }
/*      */       else
/*      */       {
/* 1417 */         enabledSet.addRow((Vector)row.clone());
/*      */       }
/*      */ 
/* 1420 */       if ((installID != null) && (installID.length() > 0) && (hasPreferenceData))
/*      */       {
/* 1422 */         editableSet.addRow((Vector)row.clone());
/*      */       }
/*      */ 
/* 1425 */       boolean isLocal = ComponentLocationUtils.isLocal(map);
/* 1426 */       if (isLocal)
/*      */       {
/* 1430 */         String location = ComponentLocationUtils.determineComponentLocationWithEnv(map, 1, this.m_binder.getLocalData(), false);
/*      */ 
/* 1432 */         String dir = FileUtils.getDirectory(location);
/*      */ 
/* 1434 */         String dwnLocation = null;
/* 1435 */         if (buildBeforeDownload)
/*      */         {
/* 1437 */           dwnLocation = FileUtils.getAbsolutePath(dir, "manifest.hda");
/*      */         }
/*      */         else
/*      */         {
/* 1442 */           dwnLocation = FileUtils.getAbsolutePath(dir, new StringBuilder().append(name).append(".zip").toString());
/*      */         }
/*      */ 
/* 1445 */         if (FileUtils.checkFile(dwnLocation, true, false) >= 0)
/*      */         {
/* 1447 */           downComps.addRow((Vector)row.clone());
/*      */         }
/* 1449 */         else if (!buildBeforeDownload)
/*      */         {
/* 1452 */           dwnLocation = FileUtils.getAbsolutePath(dir, "manifest.zip");
/* 1453 */           if (FileUtils.checkFile(dwnLocation, true, false) >= 0)
/*      */           {
/* 1455 */             downComps.addRow((Vector)row.clone());
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1461 */       List tList = StringUtils.makeListFromSequenceSimple(tags);
/* 1462 */       int size = tList.size();
/*      */ 
/* 1465 */       if ((size == 0) && (legacyComponentTags != null))
/*      */       {
/* 1467 */         String sysTags = null;
/* 1468 */         Vector sysRow = legacyComponentTags.findRow(nameIndex, name);
/* 1469 */         if (sysRow != null)
/*      */         {
/* 1471 */           sysTags = legacyComponentTags.getStringValue(tagIndex);
/* 1472 */           List sList = StringUtils.makeListFromSequenceSimple(sysTags);
/* 1473 */           for (int i = 0; i < sList.size(); ++i)
/*      */           {
/* 1475 */             String tag = (String)sList.get(i);
/* 1476 */             if (tList.contains(tag))
/*      */               continue;
/* 1478 */             tList.add(tag);
/*      */           }
/*      */         }
/*      */ 
/* 1482 */         size = tList.size();
/*      */       }
/*      */ 
/* 1485 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 1487 */         String tag = (String)tList.get(i);
/* 1488 */         Vector tRow = tagSet.findRow(0, tag);
/* 1489 */         if (tRow == null)
/*      */         {
/* 1491 */           tRow = tagSet.createEmptyRow();
/* 1492 */           tRow.setElementAt(tag, 0);
/* 1493 */           tagSet.addRow(tRow);
/*      */         }
/*      */ 
/* 1497 */         String cmpStr = (String)tRow.elementAt(1);
/* 1498 */         if (cmpStr.length() > 0)
/*      */         {
/* 1500 */           cmpStr = new StringBuilder().append(cmpStr).append(",").toString();
/*      */         }
/* 1502 */         cmpStr = new StringBuilder().append(cmpStr).append(name).toString();
/* 1503 */         tRow.setElementAt(cmpStr, 1);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1508 */     String enabledName = this.m_currentAction.getParamAt(0);
/* 1509 */     String disabledName = this.m_currentAction.getParamAt(1);
/* 1510 */     String downloadableName = this.m_currentAction.getParamAt(2);
/* 1511 */     String editableName = this.m_currentAction.getParamAt(3);
/* 1512 */     String tagSetName = this.m_currentAction.getParamAt(4);
/*      */ 
/* 1514 */     this.m_binder.addResultSet(enabledName, enabledSet);
/* 1515 */     this.m_binder.addResultSet(disabledName, disabledSet);
/* 1516 */     this.m_binder.addResultSet(editableName, editableSet);
/* 1517 */     this.m_binder.addResultSet(downloadableName, downComps);
/* 1518 */     this.m_binder.addResultSet(tagSetName, tagSet);
/*      */ 
/* 1520 */     this.m_binder.addResultSet("Components", allComps);
/*      */ 
/* 1523 */     ResultSetUtils.sortResultSet(disabledSet, new String[] { "name" });
/* 1524 */     ResultSetUtils.sortResultSet(downComps, new String[] { "name" });
/* 1525 */     ResultSetUtils.sortResultSet(tagSet, new String[] { "tag" });
/* 1526 */     ResultSetUtils.sortResultSet(allComps, new String[] { "name" });
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateComponentConfig()
/*      */     throws ServiceException, IOException, DataException
/*      */   {
/* 1536 */     getOrUpdateComponentConfig(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getComponentConfig()
/*      */     throws ServiceException, IOException, DataException
/*      */   {
/* 1544 */     getOrUpdateComponentConfig(false);
/*      */   }
/*      */ 
/*      */   protected void getOrUpdateComponentConfig(boolean isUpdate)
/*      */     throws ServiceException, IOException, DataException
/*      */   {
/* 1551 */     if (!SharedObjects.getEnvValueAsBoolean("AllowUpdateComponentConfig", true))
/*      */     {
/* 1553 */       createServiceException(null, "!csAdminUpdateCompoentConfigNotAllowed");
/*      */     }
/*      */ 
/* 1556 */     String installID = this.m_binder.getLocal("installID");
/* 1557 */     if ((installID == null) || (installID.length() == 0))
/*      */     {
/* 1559 */       createServiceException(null, "!csInstallIdRequired");
/*      */     }
/*      */ 
/* 1562 */     String compName = this.m_binder.getLocal("ComponentName");
/* 1563 */     if ((compName == null) || (compName.length() == 0))
/*      */     {
/* 1565 */       createServiceException(null, "!csComponentNameRequired");
/*      */     }
/*      */ 
/* 1569 */     String compPath = ComponentLocationUtils.computeAbsoluteComponentLocation(compName);
/* 1570 */     DataBinder cmpBinder = ComponentLoader.getComponentBinder(compName);
/* 1571 */     if (cmpBinder == null)
/*      */     {
/* 1573 */       cmpBinder = ComponentLoader.getDisabledComponentBinder(compName);
/*      */     }
/* 1575 */     String useType = (cmpBinder != null) ? cmpBinder.getLocal("useType") : "local";
/* 1576 */     boolean isLocal = (useType != null) && (useType.equals("local"));
/* 1577 */     if (compPath == null)
/*      */       return;
/* 1579 */     String compDir = FileUtils.getDirectory(compPath);
/*      */ 
/* 1581 */     String compDataDir = CompInstallUtils.getInstallConfPath(installID, compName);
/*      */ 
/* 1584 */     ComponentPreferenceData prefData = new ComponentPreferenceData(compDir, compDataDir);
/* 1585 */     prefData.setCanUpdate(isLocal);
/* 1586 */     prefData.load();
/* 1587 */     prefData.addResultSetsToBinder(prefData.getPreferenceResources(), this.m_binder);
/* 1588 */     prefData.loadPreferenceStrings();
/* 1589 */     DataResultSet prefTable = prefData.getPreferenceTable();
/*      */ 
/* 1592 */     Properties settings = null;
/* 1593 */     if (DataBinderUtils.getBoolean(this.m_binder, "revertToInstall", false))
/*      */     {
/* 1595 */       settings = prefData.m_installData;
/*      */     }
/*      */     else
/*      */     {
/* 1599 */       settings = prefData.m_configData;
/*      */     }
/*      */ 
/* 1603 */     DataResultSet configRset = prefTable.shallowClone();
/* 1604 */     ResultSetUtils.addColumnsWithDefaultValues(configRset, null, new String[] { "" }, new String[] { "pCurrVal" });
/*      */ 
/* 1606 */     FieldInfo[] fi = ResultSetUtils.createInfoList(prefTable, new String[] { "pName", "pCurrVal" }, true);
/*      */ 
/* 1608 */     for (configRset.first(); configRset.isRowPresent(); configRset.next())
/*      */     {
/* 1610 */       String name = configRset.getStringValue(fi[0].m_index);
/* 1611 */       String currVal = settings.getProperty(name);
/*      */ 
/* 1614 */       if (isUpdate)
/*      */       {
/* 1616 */         currVal = this.m_binder.getLocal(name);
/* 1617 */         if (currVal != null)
/*      */         {
/* 1619 */           SharedObjects.putEnvironmentValue(name, currVal);
/* 1620 */           settings.put(name, currVal);
/*      */         }
/*      */       }
/*      */ 
/* 1624 */       if ((currVal == null) || (currVal.length() <= 0))
/*      */         continue;
/* 1626 */       configRset.setCurrentValue(fi[1].m_index, currVal);
/*      */     }
/*      */ 
/* 1630 */     if (isUpdate)
/*      */     {
/* 1632 */       prefData.save();
/* 1633 */       String doneMsg = "!csComponentCfgUpdateDone";
/* 1634 */       this.m_binder.putLocal("StatusMessageKey", doneMsg);
/* 1635 */       this.m_binder.putLocal("StatusMessage", doneMsg);
/* 1636 */       this.m_binder.putLocal("StatusCode", "0");
/*      */ 
/* 1639 */       String alertId = "csComponentUpdateNeedRestart";
/* 1640 */       boolean componentUpdateAlertExists = AlertUtils.existsAlert(alertId, 1);
/* 1641 */       if (!componentUpdateAlertExists)
/*      */       {
/* 1643 */         DataBinder binder = new DataBinder();
/* 1644 */         binder.putLocal("alertId", alertId);
/* 1645 */         binder.putLocal("alertMsg", "<$lcMessage('!csComponentUpdateNeedRestart')$>");
/* 1646 */         binder.putLocal("flags", "1");
/* 1647 */         binder.putLocal("role", "admin");
/* 1648 */         AlertUtils.setAlert(binder);
/*      */ 
/* 1650 */         String msg = LocaleUtils.encodeMessage(alertId, null);
/* 1651 */         Report.warning(null, msg, null);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1656 */       String rsetName = this.m_currentAction.getParamAt(0);
/* 1657 */       this.m_binder.addResultSet(rsetName, configRset);
/* 1658 */       addToBinder("ComponentName", compName);
/* 1659 */       addToBinder("installID", installID);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void uninstallComponent()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1670 */     if (!SharedObjects.getEnvValueAsBoolean("AllowComponentUnintall", true))
/*      */     {
/* 1672 */       createServiceException(null, "!csAdminUninstallNotAllowed");
/*      */     }
/*      */ 
/* 1675 */     ComponentListManager.init();
/* 1676 */     ComponentListEditor compLE = ComponentListManager.getEditor();
/* 1677 */     compLE.loadComponents();
/*      */ 
/* 1679 */     String compName = this.m_binder.getLocal("ComponentName");
/* 1680 */     if (compName == null)
/*      */     {
/* 1682 */       createServiceException(null, "!csComponentNameRequired");
/*      */     }
/*      */ 
/* 1685 */     DataResultSet components = compLE.getComponentSet();
/* 1686 */     int nameIndex = ResultSetUtils.getIndexMustExist(components, "name");
/* 1687 */     Vector row = components.findRow(nameIndex, compName);
/* 1688 */     if (row == null)
/*      */     {
/* 1690 */       String msg = LocaleUtils.encodeMessage("csAdminComponentDoesNotExist", null, compName);
/*      */ 
/* 1692 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1695 */     Map map = components.getCurrentRowMap();
/* 1696 */     doComponentInstallUninstall(compName, map, false);
/* 1697 */     String msg = "!csUninstallCompleted";
/* 1698 */     this.m_binder.putLocal("StatusMessageKey", msg);
/* 1699 */     this.m_binder.putLocal("StatusMessage", msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void executeManifest()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1710 */     if (!SharedObjects.getEnvValueAsBoolean("AllowComponentUpload", true))
/*      */     {
/* 1712 */       createServiceException(null, "!csAdminUploadNotAllowed");
/*      */     }
/*      */ 
/* 1715 */     String compName = this.m_binder.getLocal("ComponentName");
/* 1716 */     if (compName == null)
/*      */     {
/* 1718 */       createServiceException(null, "!csComponentNameRequired");
/*      */     }
/*      */ 
/* 1721 */     Exception error = null;
/*      */     try
/*      */     {
/* 1724 */       doComponentInstallUninstall(compName, this.m_binder.getLocalData(), true);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1728 */       error = e;
/*      */     }
/*      */ 
/* 1731 */     if (error != null)
/*      */     {
/* 1733 */       this.m_binder.putLocal("installedComponents", compName);
/* 1734 */       this.m_binder.putLocal("errorResourceInclude", "component_install_error_msg");
/* 1735 */       createServiceException(error, LocaleUtils.encodeMessage("csCannotInstallComponentToLocation", null, compName));
/*      */     }
/*      */     else
/*      */     {
/* 1741 */       String enabledStr = this.m_binder.getLocal("enabledComponents");
/* 1742 */       List enabledList = StringUtils.makeListFromSequenceSimple(enabledStr);
/* 1743 */       if (enabledList.size() > 0)
/*      */       {
/* 1747 */         ComponentListManager.init();
/* 1748 */         ComponentListEditor compLE = ComponentListManager.getEditor();
/*      */ 
/* 1750 */         String compListStr = this.m_binder.getLocal("installedComponents");
/* 1751 */         compLE.enableOrDisableComponent(compListStr, true);
/* 1752 */         this.m_binder.putLocal("didEnabled", "1");
/*      */       }
/*      */     }
/* 1755 */     if (this.m_binder.getLocal("StatusMessage") != null) {
/*      */       return;
/*      */     }
/* 1758 */     this.m_binder.putLocal("StatusMessage", "");
/* 1759 */     this.m_binder.putLocal("StatusMessageKey", "");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadComponentInstallInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1770 */     String compName = this.m_binder.getLocal("ComponentName");
/* 1771 */     String location = this.m_binder.getLocal("location");
/* 1772 */     String zipPath = parseManifestZipPath();
/*      */ 
/* 1774 */     ComponentInstaller installer = new ComponentInstaller();
/* 1775 */     DataBinder manifestData = installer.readManifestInfoFromZip(zipPath);
/* 1776 */     processComponentInstallStep(manifestData, zipPath, compName, location, false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadComponentInstallSettings() throws ServiceException, DataException
/*      */   {
/* 1782 */     String compName = this.m_binder.getLocal("ComponentName");
/* 1783 */     String location = this.m_binder.getLocal("location");
/*      */ 
/* 1785 */     if (compName == null)
/*      */     {
/* 1787 */       createServiceException(null, "!csComponentNameRequired");
/*      */     }
/* 1789 */     if (location == null)
/*      */     {
/* 1791 */       createServiceException(null, "!csComponentLocationRequired");
/*      */     }
/*      */ 
/* 1794 */     String zipPath = parseManifestZipPath();
/* 1795 */     processComponentInstallStep(null, zipPath, compName, location, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void saveComponentZip()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1805 */     if (!SharedObjects.getEnvValueAsBoolean("AllowComponentUpload", true))
/*      */     {
/* 1807 */       createServiceException(null, "!csAdminUploadNotAllowed");
/*      */     }
/*      */ 
/* 1810 */     Vector tempFiles = this.m_binder.getTempFiles();
/* 1811 */     if (tempFiles.size() == 0)
/*      */     {
/* 1813 */       createServiceException(null, "!csAdminNoManifest");
/*      */     }
/*      */ 
/* 1816 */     if (tempFiles.size() > 1)
/*      */     {
/* 1818 */       createServiceException(null, "!csAdminNoMultipleFileUpload");
/*      */     }
/*      */ 
/* 1822 */     String zipName = (String)tempFiles.elementAt(0);
/*      */ 
/* 1824 */     ComponentInstaller installer = new ComponentInstaller();
/* 1825 */     DataBinder tmpBinder = installer.readManifestInfoFromZip(zipName);
/* 1826 */     if (tmpBinder == null)
/*      */     {
/* 1828 */       createServiceException(null, "csAdminUnableToLoadManifestInfo");
/*      */     }
/*      */ 
/* 1831 */     String[] retVal = installer.retrieveComponentNameAndLocation(tmpBinder);
/* 1832 */     String location = retVal[0];
/* 1833 */     String compName = retVal[1];
/*      */ 
/* 1835 */     DataBinder cmpBinder = installer.readFileAsBinder(zipName, new StringBuilder().append("component/").append(location.toLowerCase()).toString());
/*      */ 
/* 1837 */     String[] paths = computeManifestZipPaths(cmpBinder, compName);
/*      */ 
/* 1839 */     String absPath = paths[0];
/* 1840 */     String keyedPath = paths[1];
/* 1841 */     FileUtils.checkOrCreateDirectory(FileUtils.getDirectory(absPath), 2);
/* 1842 */     FileUtils.copyFile(zipName, absPath);
/* 1843 */     addToBinder("ComponentName", compName);
/* 1844 */     addToBinder("location", location);
/* 1845 */     addToBinder("componentDir", keyedPath);
/*      */   }
/*      */ 
/*      */   protected void doComponentInstallUninstall(String compName, Map<String, String> cmpMap, boolean isInstall)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1852 */     String server = getServerId();
/* 1853 */     if (server == null)
/*      */     {
/* 1856 */       server = SharedObjects.getEnvironmentValue("IDC_Name");
/*      */     }
/* 1858 */     if (server == null)
/*      */     {
/* 1860 */       throw new DataException("!csAdminIdcNameNotSpecified");
/*      */     }
/*      */ 
/* 1864 */     ComponentInstaller installer = new ComponentInstaller();
/* 1865 */     DataBinder manifestData = null;
/* 1866 */     String fileName = null;
/*      */ 
/* 1868 */     String location = (String)cmpMap.get("location");
/* 1869 */     String compDir = null;
/* 1870 */     if (isInstall)
/*      */     {
/* 1872 */       compDir = FileUtils.getDirectory(location);
/* 1873 */       fileName = parseManifestZipPath();
/*      */     }
/*      */     else
/*      */     {
/* 1877 */       boolean isLocal = ComponentLocationUtils.isLocal(cmpMap);
/* 1878 */       if (!isLocal)
/*      */       {
/* 1880 */         createServiceException(null, "!csAdminUninstallNonlocalComponentError");
/*      */       }
/*      */ 
/* 1884 */       location = ComponentLocationUtils.determineComponentLocation(cmpMap, 1);
/*      */ 
/* 1886 */       compDir = FileUtils.getDirectory(location);
/*      */ 
/* 1889 */       fileName = FileUtils.getAbsolutePath(compDir, "manifest.hda");
/*      */ 
/* 1891 */       if (FileUtils.checkFile(fileName, true, false) < 0)
/*      */       {
/* 1893 */         String tempLoc = new StringBuilder().append(FileUtils.getDirectory(location)).append("/").append(compName).append(".zip").toString();
/* 1894 */         fileName = FileUtils.getAbsolutePath(compDir, tempLoc);
/* 1895 */         if (FileUtils.checkFile(fileName, true, false) < 0)
/*      */         {
/* 1897 */           tempLoc = new StringBuilder().append(FileUtils.getDirectory(location)).append("/manifest.zip").toString();
/* 1898 */           fileName = FileUtils.getAbsolutePath(compDir, tempLoc);
/* 1899 */           if (FileUtils.checkFile(fileName, true, false) < 0)
/*      */           {
/* 1902 */             createServiceException(null, "!csAdminUninstallNoBuildSettings");
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1908 */     boolean isZipFile = true;
/* 1909 */     DataBinder compData = null;
/* 1910 */     Map args = new HashMap();
/* 1911 */     if (fileName.endsWith(".hda"))
/*      */     {
/* 1913 */       isZipFile = false;
/* 1914 */       String dir = FileUtils.getDirectory(fileName);
/*      */ 
/* 1917 */       manifestData = new DataBinder();
/* 1918 */       ResourceUtils.serializeDataBinder(dir, "manifest.hda", manifestData, false, true);
/*      */ 
/* 1921 */       compData = new DataBinder();
/* 1922 */       ResourceUtils.serializeDataBinder(dir, new StringBuilder().append(compName).append(".hda").toString(), compData, false, true);
/*      */     }
/*      */     else
/*      */     {
/* 1926 */       manifestData = ZipFunctions.extractFileAsDataBinder(fileName, "manifest.hda");
/* 1927 */       String[] r = installer.retrieveComponentNameAndLocation(manifestData);
/* 1928 */       compData = ZipFunctions.extractFileAsDataBinder(fileName, new StringBuilder().append("component/").append(r[0]).toString());
/*      */ 
/* 1933 */       DataBinder componentDef = ZipFunctions.extractFileAsDataBinder(fileName, new StringBuilder().append("component/").append(location).toString());
/*      */ 
/* 1935 */       boolean doNotBackupZip = DataBinderUtils.getBoolean(componentDef, "disableZipFileBackup", false);
/*      */ 
/* 1938 */       if (!doNotBackupZip)
/*      */       {
/* 1940 */         args.put("Backup", "true");
/* 1941 */         args.put("Overwrite", "false");
/*      */       }
/*      */       else
/*      */       {
/* 1945 */         args.put("Backup", "false");
/* 1946 */         args.put("Overwrite", "true");
/*      */       }
/*      */ 
/* 1951 */       Properties props = componentDef.getLocalData();
/* 1952 */       if (props.containsKey("disableZipFileBackup"))
/*      */       {
/* 1954 */         this.m_binder.putLocal("disableZipFileBackup", props.getProperty("disableZipFileBackup"));
/*      */       }
/*      */     }
/*      */ 
/* 1958 */     String installID = this.m_binder.getLocal("installID");
/* 1959 */     if (!isInstall)
/*      */     {
/* 1963 */       installID = CompInstallUtils.getInstallID(compName);
/* 1964 */       executeUninstallFilter(compData, manifestData, fileName, compName, location, isZipFile);
/*      */     }
/*      */ 
/* 1967 */     String backupName = installer.getComponentBackupPath(installID, compName);
/*      */ 
/* 1970 */     String type = "Uninstall";
/* 1971 */     if (isInstall)
/*      */     {
/* 1973 */       type = "Install";
/*      */     }
/*      */ 
/* 1976 */     args.put(type, "true");
/* 1977 */     if (isZipFile)
/*      */     {
/* 1979 */       args.put("ZipName", fileName);
/*      */     }
/*      */     else
/*      */     {
/* 1983 */       args.remove("ZipName");
/*      */     }
/* 1985 */     args.put("BackupZipName", backupName);
/*      */ 
/* 1987 */     installer.executeInstaller(compData, manifestData, installID, compName, args);
/* 1988 */     if (!isInstall)
/*      */       return;
/* 1990 */     installer.doInstallExtra(this.m_binder, compName, location, fileName, installID);
/*      */ 
/* 1993 */     this.m_binder.putLocal("logDataDir", installer.getLogDataDir());
/* 1994 */     this.m_binder.putLocal("logFileName", installer.getLogFileName());
/* 1995 */     Vector compList = installer.getSucessfulComponents();
/* 1996 */     if ((compList != null) && (compList.size() > 0))
/*      */     {
/* 1998 */       this.m_binder.putLocal("installedComponents", StringUtils.createStringSimple(compList));
/*      */     }
/* 2000 */     Vector enabledList = installer.getEnabledComponents();
/* 2001 */     if ((enabledList == null) || (enabledList.size() <= 0))
/*      */       return;
/* 2003 */     this.m_binder.putLocal("enabledComponents", StringUtils.createStringSimple(enabledList));
/*      */   }
/*      */ 
/*      */   protected void executeUninstallFilter(DataBinder compData, DataBinder manifest, String fileName, String compName, String compLoc, boolean isZipFile)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2012 */     if (manifest == null)
/*      */     {
/* 2014 */       return;
/*      */     }
/*      */ 
/* 2017 */     CompInstallUtils.hideComponentDocMetaFields(this.m_workspace, compName, null);
/*      */ 
/* 2019 */     SharedObjects.putEnvironmentValue("ComponentName", compName);
/* 2020 */     String filterType = new StringBuilder().append(compName).append("ComponentUninstallFilter").toString();
/* 2021 */     if (!PluginFilters.hasFilter(filterType))
/*      */     {
/* 2023 */       DataBinder binder = null;
/* 2024 */       String idcDir = SharedObjects.getEnvironmentValue("IntradocDir");
/* 2025 */       if (isZipFile)
/*      */       {
/* 2027 */         ResultSet manifestSet = manifest.getResultSet("Manifest");
/* 2028 */         String tempLoc = ResultSetUtils.findValue(manifestSet, "entryType", "component", "location");
/*      */ 
/* 2030 */         binder = ZipFunctions.extractFileAsDataBinder(fileName, new StringBuilder().append("component/").append(tempLoc).toString());
/*      */       }
/*      */       else
/*      */       {
/* 2034 */         String tempName = FileUtils.getAbsolutePath(idcDir, compLoc);
/*      */ 
/* 2036 */         binder = new DataBinder();
/*      */ 
/* 2038 */         ResourceUtils.serializeDataBinder(FileUtils.getDirectory(tempName), FileUtils.getName(tempName), binder, false, true);
/*      */       }
/*      */ 
/* 2042 */       if (binder == null)
/*      */       {
/* 2044 */         return;
/*      */       }
/*      */ 
/* 2047 */       DataResultSet rset = (DataResultSet)binder.getResultSet("Filters");
/* 2048 */       if ((rset == null) || (rset.isEmpty()))
/*      */       {
/* 2050 */         return;
/*      */       }
/*      */ 
/* 2053 */       String[] fields = { "type", "location", "parameter", "loadOrder" };
/* 2054 */       FieldInfo[] info = ResultSetUtils.createInfoList(rset, fields, false);
/* 2055 */       Vector filterList = new IdcVector();
/*      */ 
/* 2057 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*      */       {
/* 2059 */         String type = rset.getStringValue(info[0].m_index);
/* 2060 */         if (!type.equals(filterType))
/*      */           continue;
/* 2062 */         PluginFilterData data = new PluginFilterData();
/* 2063 */         data.m_filterType = type;
/* 2064 */         data.m_location = rset.getStringValue(info[1].m_index);
/* 2065 */         if (info[2].m_index >= 0)
/*      */         {
/* 2067 */           data.m_parameter = rset.getStringValue(info[2].m_index);
/*      */         }
/* 2069 */         if (info[3].m_index >= 0)
/*      */         {
/* 2071 */           String order = rset.getStringValue(info[3].m_index);
/* 2072 */           data.m_order = PluginFilterLoader.parseOrder(order);
/*      */         }
/*      */ 
/* 2075 */         filterList.addElement(data);
/*      */       }
/*      */ 
/* 2079 */       PluginFilters.registerFilters(filterList);
/*      */     }
/* 2081 */     PluginFilters.filter(filterType, this.m_workspace, null, this);
/*      */   }
/*      */ 
/*      */   protected void processComponentInstallStep(DataBinder tmpBinder, String zipName, String compName, String location, boolean isPrefSetup)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2089 */     String hasPrefStr = null;
/* 2090 */     String hasInstStr = null;
/* 2091 */     String installID = null;
/* 2092 */     String reqFeatures = null;
/* 2093 */     String provFeatures = null;
/* 2094 */     String addComps = null;
/* 2095 */     String version = null;
/* 2096 */     String preventDowngrades = null;
/* 2097 */     String disableComps = null;
/* 2098 */     DataBinder binder = null;
/*      */ 
/* 2101 */     binder = ZipFunctions.extractFileAsDataBinder(zipName, new StringBuilder().append("component/").append(location).toString());
/* 2102 */     if (binder == null)
/*      */     {
/* 2104 */       createServiceException(null, LocaleUtils.encodeMessage("csUnableToLoadResourceDefinition", null, zipName));
/*      */     }
/*      */ 
/* 2108 */     hasPrefStr = binder.getLocal("hasPreferenceData");
/* 2109 */     hasInstStr = binder.getLocal("hasInstallStrings");
/* 2110 */     installID = binder.getLocal("installID");
/* 2111 */     reqFeatures = binder.getLocal("requiredFeatures");
/* 2112 */     provFeatures = binder.getLocal("featureExtensions");
/* 2113 */     addComps = binder.getLocal("additionalComponents");
/* 2114 */     disableComps = binder.getLocal("componentsToDisable");
/* 2115 */     version = binder.getLocal("version");
/* 2116 */     preventDowngrades = binder.getLocal("preventAdditionalComponentDowngrade");
/*      */ 
/* 2118 */     ComponentInstaller installer = new ComponentInstaller();
/* 2119 */     installer.checkVersion(compName, binder);
/*      */ 
/* 2121 */     if (isPrefSetup)
/*      */     {
/* 2124 */       ComponentPreferenceData prefData = new ComponentPreferenceData();
/* 2125 */       installer.retrievePreferenceData(prefData, zipName, compName, installID);
/*      */ 
/* 2127 */       ResourceContainer prefResources = new ResourceContainer();
/*      */ 
/* 2129 */       if (DataBinderUtils.getBoolean(this.m_binder, "hasInstallStrings", false))
/*      */       {
/* 2131 */         installer.retrievePreferenceResources(zipName, compName, prefResources);
/*      */       }
/*      */ 
/* 2135 */       prefData.addResultSetsToBinder(prefResources, this.m_binder);
/*      */ 
/* 2137 */       this.m_binder.addResultSet("PreferenceData", prefData.getPreferenceTable());
/*      */     }
/*      */     else
/*      */     {
/* 2141 */       ResultSet rset = tmpBinder.getResultSet("Manifest");
/* 2142 */       this.m_binder.addResultSet("Manifest", rset);
/*      */     }
/*      */ 
/* 2145 */     if ((installID == null) || (installID.length() == 0))
/*      */     {
/* 2147 */       if (isPrefSetup)
/*      */       {
/* 2149 */         createServiceException(null, "!csInstallIdRequired");
/*      */       }
/*      */ 
/* 2152 */       installID = compName;
/*      */     }
/* 2154 */     addToBinder("ComponentName", compName);
/* 2155 */     addToBinder("location", location);
/* 2156 */     addToBinder("hasInstallStrings", hasInstStr);
/* 2157 */     addToBinder("hasPreferenceData", hasPrefStr);
/* 2158 */     addToBinder("installID", installID);
/* 2159 */     addToBinder("requiredFeatures", reqFeatures);
/* 2160 */     addToBinder("featureExtensions", provFeatures);
/* 2161 */     addToBinder("additionalComponents", addComps);
/* 2162 */     addToBinder("preventAdditionalComponentDowngrade", preventDowngrades);
/* 2163 */     addToBinder("componentsToDisable", disableComps);
/* 2164 */     addToBinder("version", version);
/*      */   }
/*      */ 
/*      */   protected void addToBinder(String name, String val)
/*      */   {
/* 2169 */     if (val == null)
/*      */     {
/* 2171 */       val = "";
/*      */     }
/* 2173 */     this.m_binder.putLocal(name, val);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setRestartRequired()
/*      */     throws ServiceException
/*      */   {
/* 2181 */     String server = getServerId();
/* 2182 */     setRestartRequired(server, "true");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void cancelComponentInstall()
/*      */     throws ServiceException
/*      */   {
/* 2191 */     if (!SharedObjects.getEnvValueAsBoolean("AllowComponentUpload", true))
/*      */     {
/* 2193 */       createServiceException(null, "!csAdminUploadNotAllowed");
/*      */     }
/*      */ 
/* 2197 */     String zipName = parseManifestZipPath();
/* 2198 */     if (FileUtils.checkFile(zipName, true, false) >= 0)
/*      */     {
/* 2200 */       FileUtils.deleteFile(zipName);
/*      */     }
/* 2202 */     String msg = "!csAdminInstallCancelled";
/* 2203 */     this.m_binder.putLocal("StatusMessageKey", msg);
/* 2204 */     this.m_binder.putLocal("StatusMessage", msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void downloadComponent()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2218 */     setConditionVar("SuppressCacheControlHeader", true);
/*      */ 
/* 2221 */     if (!SharedObjects.getEnvValueAsBoolean("AllowComponentDownload", true))
/*      */     {
/* 2223 */       createServiceException(null, "!csAdminDownloadNotAllowed");
/*      */     }
/*      */ 
/* 2226 */     String componentName = this.m_binder.getLocal("ComponentName");
/* 2227 */     if (componentName == null)
/*      */     {
/* 2229 */       createServiceException(null, "!csComponentNameRequired");
/*      */     }
/*      */ 
/* 2233 */     ComponentListManager.init();
/* 2234 */     ComponentListEditor compLE = ComponentListManager.getEditor();
/*      */ 
/* 2236 */     DataResultSet components = compLE.getComponentSet();
/* 2237 */     int nameIndex = ResultSetUtils.getIndexMustExist(components, "name");
/* 2238 */     Vector row = components.findRow(nameIndex, componentName);
/* 2239 */     if (row == null)
/*      */     {
/* 2241 */       String msg = LocaleUtils.encodeMessage("csAdminComponentDoesNotExist", null, componentName);
/*      */ 
/* 2243 */       throw new DataException(msg);
/*      */     }
/* 2245 */     Map map = components.getCurrentRowMap();
/*      */ 
/* 2248 */     if (!ComponentLocationUtils.isLocal(map))
/*      */     {
/* 2250 */       createServiceException(null, "!csAdminComponentMustBeLocal");
/*      */     }
/*      */ 
/* 2254 */     boolean fileAvailable = false;
/* 2255 */     String compDefFile = ComponentLocationUtils.determineComponentLocation(map, 1);
/*      */ 
/* 2257 */     String compDir = FileUtils.getDirectory(compDefFile);
/* 2258 */     String location = FileUtils.getAbsolutePath(compDir, new StringBuilder().append(componentName).append(".zip").toString());
/*      */ 
/* 2261 */     boolean buildBeforeDownload = SharedObjects.getEnvValueAsBoolean("BuildComponentBeforeDownload", false);
/*      */ 
/* 2263 */     if (buildBeforeDownload)
/*      */     {
/* 2266 */       ComponentInstaller installer = new ComponentInstaller();
/* 2267 */       boolean isUpdate = false;
/* 2268 */       String buildSettings = FileUtils.getAbsolutePath(compDir, "manifest.hda");
/*      */ 
/* 2271 */       if (FileUtils.checkFile(buildSettings, true, false) < 0)
/*      */       {
/* 2273 */         throw new DataException(LocaleUtils.encodeMessage("csAdminManifestFileMissing", null, compDir));
/*      */       }
/*      */ 
/* 2278 */       if (FileUtils.checkFile(compDefFile, true, false) < 0)
/*      */       {
/* 2280 */         throw new DataException(LocaleUtils.encodeMessage("csAdminManifestFileMissing", null, compDir));
/*      */       }
/*      */ 
/* 2285 */       DataBinder manifestData = ResourceUtils.readDataBinderFromPath(buildSettings);
/*      */ 
/* 2288 */       DataBinder compData = ResourceUtils.readDataBinderFromPath(compDefFile);
/*      */ 
/* 2290 */       if (manifestData == null)
/*      */       {
/* 2292 */         throw new DataException(LocaleUtils.encodeMessage("csAdminUnableToReadManifestFile", null, compDir));
/*      */       }
/*      */ 
/* 2296 */       Map args = new HashMap();
/* 2297 */       String backupName = new StringBuilder().append(FileUtils.getDirectory(location)).append("/").append(componentName).append("_backup_").append(Long.toString(new Date().getTime())).append(".zip").toString();
/*      */ 
/* 2299 */       args.put("Build", "true");
/* 2300 */       args.put("NewZipName", location);
/* 2301 */       args.put("BackupZipName", backupName);
/*      */ 
/* 2304 */       if (FileUtils.checkFile(location, true, false) >= 0)
/*      */       {
/* 2306 */         FileUtils.renameFile(location, backupName);
/*      */       }
/*      */ 
/* 2310 */       installer.initEx(componentName, compData, manifestData, args);
/* 2311 */       installer.executeManifest();
/*      */ 
/* 2314 */       Map exceptions = installer.getExceptions();
/* 2315 */       if ((exceptions != null) && (!exceptions.isEmpty()))
/*      */       {
/* 2317 */         ServiceException se = new ServiceException("!csAdminUnableToBuildComponent");
/* 2318 */         for (Iterator i$ = exceptions.entrySet().iterator(); i$.hasNext(); ) { Object entry = i$.next();
/*      */ 
/* 2320 */           Exception ex = (Exception)((Map.Entry)entry).getValue();
/* 2321 */           se.addCause(ex); }
/*      */ 
/*      */ 
/* 2325 */         if (isUpdate)
/*      */         {
/* 2327 */           FileUtils.renameFile(backupName, location);
/*      */         }
/*      */ 
/* 2330 */         throw se;
/*      */       }
/*      */ 
/* 2333 */       fileAvailable = true;
/*      */     }
/* 2337 */     else if (FileUtils.checkFile(location, true, false) >= 0)
/*      */     {
/* 2339 */       fileAvailable = true;
/*      */     }
/*      */     else
/*      */     {
/* 2343 */       location = FileUtils.getAbsolutePath(compDir, "manifest.zip");
/* 2344 */       if (FileUtils.checkFile(location, true, false) >= 0)
/*      */       {
/* 2346 */         fileAvailable = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2351 */     String downloadName = FileUtils.getName(location);
/* 2352 */     DataStreamWrapper streamWrapper = getDownloadStream(true);
/* 2353 */     streamWrapper.setSimpleFileData(location, downloadName, "application/zip");
/* 2354 */     streamWrapper.m_clientFileName = downloadName;
/*      */ 
/* 2357 */     streamWrapper.m_useStream = fileAvailable;
/* 2358 */     streamWrapper.m_determinedExistence = true;
/* 2359 */     streamWrapper.m_streamLocationExists = fileAvailable;
/*      */ 
/* 2361 */     if (fileAvailable)
/*      */       return;
/* 2363 */     String error = LocaleUtils.encodeMessage("csAdminDownloadFileDoesNotExist", null, componentName, location);
/*      */ 
/* 2365 */     createServiceException(null, error);
/*      */   }
/*      */ 
/*      */   protected SystemPropertiesEditor prepareSysPropsEditor(SystemPropertiesEditor editor, String intradocDir, String encoding, boolean doWrite, String idcFile)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2378 */     return prepareSysPropsEditorEx(editor, intradocDir, encoding, doWrite, idcFile);
/*      */   }
/*      */ 
/*      */   protected SystemPropertiesEditor prepareSysPropsEditorEx(SystemPropertiesEditor editor, String intradocDir, String encoding, boolean doWrite, String idcFile)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2390 */     if (!this.m_useIOProcess)
/*      */     {
/* 2392 */       return editor;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2400 */       InputStream idcReader = getReadStream(idcFile);
/*      */ 
/* 2403 */       String cfgFile = FileUtils.getAbsolutePath(intradocDir, "config/config.cfg");
/* 2404 */       InputStream cfgReader = null;
/* 2405 */       if (!FileUtils.storeInDB(cfgFile))
/*      */       {
/* 2407 */         cfgReader = getReadStream(cfgFile);
/*      */       }
/*      */ 
/* 2412 */       OutputStream cfgWriter = null;
/* 2413 */       if ((doWrite) && 
/* 2415 */         (!FileUtils.storeInDB(cfgFile)))
/*      */       {
/* 2417 */         cfgWriter = getWriteStream(FileUtils.getAbsolutePath(intradocDir, "config/config.cfg"));
/*      */       }
/*      */ 
/* 2422 */       editor.setFilepaths(idcFile, cfgFile);
/* 2423 */       editor.setInputStreams(idcReader, cfgReader);
/* 2424 */       editor.setOutputStreams(null, cfgWriter);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2429 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/* 2432 */     return editor;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadSysProps()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2441 */     String server = getServerId();
/* 2442 */     if (server == null)
/*      */     {
/* 2444 */       throw new DataException("!csAdminIdcNameNotSpecified");
/*      */     }
/*      */ 
/* 2448 */     this.m_allServers.mergeLocalData(server, this.m_binder);
/* 2449 */     String intradocDir = this.m_allServers.getValueMustExist(server, "IntradocDir");
/* 2450 */     String idcFile = FileUtils.getAbsolutePath(AdminDirectoryLocator.getBinDir(this.m_binder), "intradoc.cfg");
/*      */ 
/* 2454 */     String encoding = this.m_allServers.getValue(server, "FileEncoding");
/* 2455 */     if ((encoding == null) || (encoding.length() == 0))
/*      */     {
/* 2457 */       encoding = DataSerializeUtils.getSystemEncoding();
/*      */     }
/*      */ 
/* 2462 */     SystemPropertiesEditor editor = new SystemPropertiesEditor(idcFile);
/* 2463 */     editor = prepareSysPropsEditor(editor, intradocDir, encoding, false, idcFile);
/* 2464 */     editor.loadProperties();
/* 2465 */     editor.closeAllStreams();
/* 2466 */     Properties props = this.m_binder.getLocalData();
/* 2467 */     Properties newProps = editor.getConfig();
/* 2468 */     Properties idcProps = editor.getIdc();
/*      */ 
/* 2471 */     for (int i = 0; i < NOT_PASSED_VARS.length; ++i)
/*      */     {
/* 2473 */       newProps.remove(NOT_PASSED_VARS[i]);
/* 2474 */       idcProps.remove(NOT_PASSED_VARS[i]);
/*      */     }
/*      */ 
/* 2477 */     DataBinder.mergeHashTables(props, idcProps);
/* 2478 */     DataBinder.mergeHashTables(props, newProps);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void saveSysProps()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2488 */     String server = getServerId();
/* 2489 */     if (server == null)
/*      */     {
/* 2491 */       throw new DataException("!csAdminIdcNameNotSpecified");
/*      */     }
/*      */ 
/* 2494 */     this.m_binder.removeLocal("IdcService");
/* 2495 */     this.m_binder.removeLocal("CurrentTab");
/* 2496 */     Properties cfgProps = this.m_binder.getLocalData();
/*      */ 
/* 2500 */     Properties tempProps = new Properties(cfgProps);
/* 2501 */     this.m_binder.setLocalData(tempProps);
/*      */ 
/* 2504 */     String majRev = this.m_binder.getLocal("MajorRevSeq");
/* 2505 */     String minRev = this.m_binder.getLocal("MinorRevSeq");
/*      */ 
/* 2507 */     boolean minRevPresent = (minRev != null) && (minRev.length() > 0);
/* 2508 */     boolean majRevPresent = (majRev != null) && (majRev.length() > 0);
/* 2509 */     if ((minRevPresent) && (!majRevPresent))
/*      */     {
/* 2511 */       throw new ServiceException("!csMajorRevRangeMissing");
/*      */     }
/* 2513 */     if (majRevPresent)
/*      */     {
/* 2515 */       String oldMajRev = "";
/* 2516 */       String oldMinRev = "";
/*      */       try
/*      */       {
/* 2522 */         oldMajRev = SharedObjects.getEnvironmentValue("MajorRevSeq");
/* 2523 */         SharedObjects.putEnvironmentValue("MajorRevSeq", majRev);
/* 2524 */         if (minRevPresent)
/*      */         {
/* 2526 */           oldMinRev = SharedObjects.getEnvironmentValue("MinorRevSeq");
/* 2527 */           SharedObjects.putEnvironmentValue("MinorRevSeq", minRev);
/*      */         }
/* 2529 */         RevisionSpec.initImplementor();
/*      */       }
/*      */       catch (ServiceException s)
/*      */       {
/* 2535 */         if (oldMajRev == null)
/*      */         {
/* 2537 */           SharedObjects.removeEnvironmentValue("MajorRevSeq");
/*      */         }
/*      */         else
/*      */         {
/* 2541 */           SharedObjects.putEnvironmentValue("MajorRevSeq", oldMajRev);
/*      */         }
/*      */ 
/* 2544 */         if (minRevPresent)
/*      */         {
/* 2546 */           if (oldMajRev == null)
/*      */           {
/* 2548 */             SharedObjects.removeEnvironmentValue("MinorRevSeq");
/*      */           }
/*      */           else
/*      */           {
/* 2552 */             SharedObjects.putEnvironmentValue("MinorRevSeq", oldMinRev);
/*      */           }
/*      */         }
/* 2555 */         throw s;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2560 */     String autoNum = this.m_binder.getLocal("AutoNumberPrefix");
/* 2561 */     if ((autoNum != null) && 
/* 2564 */       (autoNum.indexOf("<$") < 0))
/*      */     {
/* 2567 */       int result = Validation.checkUrlFileSegment(autoNum);
/* 2568 */       String error = null;
/* 2569 */       switch (result)
/*      */       {
/*      */       case -2:
/* 2572 */         error = "!csAutoPrefixNoSpaces";
/* 2573 */         break;
/*      */       case -3:
/* 2576 */         error = LocaleUtils.encodeMessage("csAutoPrefixIllegalChars", null, ";/\\?:@&=+\"#%<>*~|[]ıİ");
/*      */       }
/*      */ 
/* 2581 */       if (autoNum.length() > 15)
/*      */       {
/* 2583 */         error = "!csAutoPrefixTooLong";
/*      */       }
/*      */ 
/* 2586 */       if (error != null)
/*      */       {
/* 2588 */         throw new ServiceException(error);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2593 */     String ntlmSecurityType = this.m_binder.getLocal("NtlmSecurityType");
/* 2594 */     if (ntlmSecurityType != null)
/*      */     {
/* 2597 */       boolean useNtlm = ntlmSecurityType.equalsIgnoreCase("ntlm");
/* 2598 */       boolean useAdsi = ntlmSecurityType.equalsIgnoreCase("adsi");
/* 2599 */       cfgProps.put("UseNtlm", (useNtlm) ? "Yes" : "");
/* 2600 */       cfgProps.put("UseAdsi", (useAdsi) ? "Yes" : "");
/* 2601 */       cfgProps.put("NtlmSecurityEnabled", ((useNtlm) || (useAdsi)) ? "Yes" : "");
/*      */     }
/*      */ 
/* 2606 */     this.m_allServers.mergeLocalData(server, this.m_binder);
/* 2607 */     String intradocDir = this.m_allServers.getValueMustExist(server, "IntradocDir");
/* 2608 */     String idcFile = new StringBuilder().append(AdminDirectoryLocator.getBinDir(this.m_binder)).append("intradoc.cfg").toString();
/*      */ 
/* 2611 */     String encoding = this.m_allServers.getValue(server, "FileEncoding");
/* 2612 */     if ((encoding == null) || (encoding.length() == 0))
/*      */     {
/* 2614 */       encoding = DataSerializeUtils.getSystemEncoding();
/*      */     }
/*      */ 
/* 2618 */     SystemPropertiesEditor editor = new SystemPropertiesEditor(idcFile);
/* 2619 */     editor = prepareSysPropsEditor(editor, intradocDir, encoding, true, idcFile);
/* 2620 */     editor.loadProperties();
/*      */     try
/*      */     {
/* 2625 */       String extras = cfgProps.getProperty("cfgExtraVariables");
/* 2626 */       if (extras != null)
/*      */       {
/* 2628 */         Properties newExtraProps = new Properties();
/* 2629 */         FileUtils.loadProperties(newExtraProps, new ByteArrayInputStream(extras.getBytes(FileUtils.m_javaSystemEncoding)));
/*      */ 
/* 2635 */         String newLine = System.getProperty("line.separator");
/* 2636 */         String key = null;
/* 2637 */         String specialAuthGroups = newExtraProps.getProperty("SpecialAuthGroups");
/*      */ 
/* 2639 */         if (specialAuthGroups != null)
/*      */         {
/* 2641 */           String[] groups = specialAuthGroups.split(",");
/* 2642 */           StringBuilder builder = new StringBuilder();
/* 2643 */           for (int i = 0; i < groups.length; ++i)
/*      */           {
/* 2645 */             if (i != 0)
/*      */             {
/* 2647 */               builder.append(",");
/*      */             }
/* 2649 */             builder.append(groups[i].trim());
/*      */           }
/* 2651 */           newExtraProps.setProperty("SpecialAuthGroups", builder.toString());
/*      */ 
/* 2653 */           builder.setLength(0);
/*      */ 
/* 2655 */           Enumeration en = newExtraProps.keys();
/* 2656 */           while (en.hasMoreElements())
/*      */           {
/* 2658 */             key = (String)en.nextElement();
/* 2659 */             builder.append(key).append('=').append(newExtraProps.get(key)).append(newLine);
/*      */           }
/*      */ 
/* 2663 */           cfgProps.remove("cfgExtraVariables");
/* 2664 */           cfgProps.setProperty("cfgExtraVariables", builder.toString());
/*      */         }
/*      */ 
/* 2669 */         for (int i = 0; i < UNSETTABLE_VARS.length; ++i)
/*      */         {
/* 2671 */           String newValue = newExtraProps.getProperty(UNSETTABLE_VARS[i]);
/* 2672 */           String oldValue = editor.searchForValue(UNSETTABLE_VARS[i]);
/* 2673 */           if ((newValue == null) || ((oldValue != null) && (newValue.equals(oldValue))))
/*      */             continue;
/* 2675 */           String msg = LocaleUtils.encodeMessage("csAdminIllegalChange", null, UNSETTABLE_VARS[i], oldValue, newValue);
/*      */ 
/* 2677 */           createServiceException(null, msg);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 2684 */       createServiceException(null, "!csAdminUnableToParse");
/*      */     }
/*      */ 
/* 2688 */     IdcMessage info = new AdminLog().getLogInfo(cfgProps, editor);
/*      */ 
/* 2690 */     if (info != null)
/*      */     {
/* 2692 */       IdcMessage msg = IdcMessageFactory.lc("csAdminValueChangeLog", new Object[] { cfgProps.get("dUser"), cfgProps.get("IDC_Id") });
/*      */ 
/* 2694 */       msg.m_prior = info;
/* 2695 */       Report.info(null, null, msg);
/*      */     }
/*      */ 
/* 2699 */     editor.mergePropertyValues(null, cfgProps);
/* 2700 */     editor.saveConfig();
/* 2701 */     editor.closeAllStreams();
/* 2702 */     setRestartRequired(server, "true");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addServerReference()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2712 */     String idcDir = this.m_binder.getLocal("IntradocDir");
/* 2713 */     String idcFile = null;
/* 2714 */     if (idcDir != null)
/*      */     {
/* 2716 */       idcFile = new StringBuilder().append(idcDir).append("/bin/intradoc.cfg").toString();
/*      */     }
/* 2718 */     String contentServerType = this.m_binder.getLocal("contentServerType");
/* 2719 */     String nodeIdcDir = this.m_binder.getLocal("ClusterNodeIntradocDir");
/* 2720 */     if (idcDir == null)
/*      */     {
/* 2722 */       if (nodeIdcDir != null)
/*      */       {
/* 2724 */         if (contentServerType.equals("localCluster"))
/*      */         {
/* 2726 */           idcDir = nodeIdcDir;
/* 2727 */           idcFile = new StringBuilder().append(nodeIdcDir).append("/bin/intradoc.cfg").toString();
/*      */         }
/*      */         else
/*      */         {
/* 2731 */           idcDir = FileUtils.getParent(nodeIdcDir);
/* 2732 */           idcFile = new StringBuilder().append(nodeIdcDir).append("/intradoc.cfg").toString();
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 2738 */         this.m_binder.putLocal("FileEncoding", System.getProperty("file.encoding"));
/* 2739 */         return;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2745 */     String encoding = this.m_binder.getLocal("FileEncoding");
/* 2746 */     idcDir = FileUtils.directorySlashes(idcDir);
/* 2747 */     this.m_binder.putLocal("IntradocDir", idcDir);
/*      */ 
/* 2749 */     SystemPropertiesEditor editor = new SystemPropertiesEditor(idcFile);
/* 2750 */     this.m_binder.putLocal("serverActions", "read");
/* 2751 */     editor = prepareSysPropsEditorEx(editor, idcDir, encoding, false, idcFile);
/* 2752 */     editor.loadProperties();
/* 2753 */     editor.closeAllStreams();
/*      */ 
/* 2756 */     if (nodeIdcDir != null)
/*      */     {
/* 2758 */       idcDir = editor.searchForValue("IntradocDir");
/* 2759 */       this.m_binder.putLocal("IntradocDir", idcDir);
/* 2760 */       this.m_binder.putLocal("serverActions", "read");
/* 2761 */       editor = prepareSysPropsEditor(editor, idcDir, encoding, false, idcFile);
/* 2762 */       editor.loadProperties();
/* 2763 */       editor.closeAllStreams();
/*      */     }
/*      */ 
/* 2767 */     this.m_binder.putLocal("IntradocServerPort", "4444");
/* 2768 */     this.m_binder.putLocal("CgiFileName", "idcplg");
/*      */ 
/* 2770 */     String[] keys = { "IDC_Name", "IDC_Id", "ClusterNodeName", "ClusterGroup", "SocketServerAddress", "HttpServerAddress", "IntradocServerPort", "HttpRelativeWebRoot", "HttpRelativeCgiRoot", "CgiFileName", "FileEncoding", "ClusterBinDirRule", "ClusterNodeAddress" };
/*      */ 
/* 2776 */     for (int i = 0; i < keys.length; ++i)
/*      */     {
/* 2778 */       String val = editor.searchForValue(keys[i]);
/* 2779 */       if (val != null)
/* 2780 */         this.m_binder.putLocal(keys[i], val);
/*      */     }
/* 2782 */     if (this.m_binder.getLocal("IDC_Name") == null)
/*      */     {
/* 2784 */       throw new ServiceException(null, "csAdminServerConfigNoIdcName", new Object[] { editor.getCfgFile(), editor.cfgFileContents() });
/*      */     }
/*      */ 
/* 2789 */     if ((contentServerType != null) && (contentServerType.endsWith("Cluster")))
/*      */     {
/* 2792 */       String clusterNodeName = this.m_binder.getLocal("ClusterNodeName");
/* 2793 */       String clusterGroup = this.m_binder.getLocal("ClusterGroup");
/* 2794 */       if (clusterGroup == null)
/*      */       {
/* 2796 */         clusterGroup = this.m_binder.getLocal("IDC_Name");
/*      */       }
/* 2798 */       if ((clusterNodeName == null) || (clusterGroup == null) || (clusterNodeName.length() == 0) || (clusterGroup.length() == 0))
/*      */       {
/* 2801 */         createServiceException(null, "!csClusterNodeRequiresNodeNameAndGroup");
/*      */       }
/* 2803 */       this.m_binder.putLocal("IDC_Id", new StringBuilder().append(this.m_binder.getLocal("IDC_Name")).append("-").append(clusterNodeName).toString());
/*      */     }
/*      */     else
/*      */     {
/* 2807 */       this.m_binder.putLocal("IDC_Id", this.m_binder.getLocal("IDC_Name"));
/*      */     }
/*      */ 
/* 2812 */     String relWeb = editor.searchForValue("HttpRelativeWebRoot");
/* 2813 */     String relCgi = editor.searchForValue("HttpRelativeCgiRoot");
/* 2814 */     if ((relWeb != null) && (relCgi == null)) {
/* 2815 */       this.m_binder.putLocal("HttpRelativeCgiRoot", relWeb);
/*      */     }
/*      */ 
/* 2818 */     String isProxied = editor.searchForValue("IsProxiedServer");
/* 2819 */     String cgiFile = this.m_binder.get("CgiFileName");
/* 2820 */     if (StringUtils.convertToBool(isProxied, false))
/*      */     {
/* 2822 */       cgiFile = DirectoryLocator.createProxiedCgiFileName(cgiFile, relWeb);
/* 2823 */       this.m_binder.putLocal("CgiFileName", cgiFile);
/*      */     }
/*      */ 
/* 2827 */     String serverActions = "stop,start,restart,query";
/* 2828 */     if (EnvUtils.isFamily("unix"))
/*      */     {
/* 2830 */       serverActions = new StringBuilder().append(serverActions).append(",read,write").toString();
/*      */     }
/* 2832 */     this.m_binder.putLocal("serverActions", serverActions);
/* 2833 */     this.m_binder.putLocal("description", "");
/* 2834 */     this.m_binder.putLocal("directorySet", "1");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void saveServerData()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2843 */     String serverName = getServerId();
/* 2844 */     if ((serverName == null) || (serverName.trim().length() == 0))
/*      */     {
/* 2846 */       createServiceException(null, "!csAdminServerNotSpecified");
/*      */     }
/* 2848 */     String isEdit = this.m_binder.getLocal("isEdit");
/* 2849 */     Properties data = this.m_allServers.getLocalData(serverName);
/* 2850 */     if ((data != null) && (!StringUtils.convertToBool(isEdit, false)))
/*      */     {
/* 2852 */       String msg = LocaleUtils.encodeMessage("csServerNameAlreadyExists2", null, serverName);
/*      */ 
/* 2854 */       createServiceExceptionEx(null, msg, -17);
/*      */     }
/*      */ 
/* 2858 */     this.m_allServers.addOrEditServerReference(this.m_binder, this.m_CFU);
/*      */ 
/* 2861 */     getServiceData().m_htmlPage = "ROOT_IDC_ADMIN_PAGE";
/* 2862 */     this.m_binder.putLocal("IdcService", "GET_ROOT_IDC_ADMIN_PAGE");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void removeServerData()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2873 */     String serverName = getServerId();
/* 2874 */     this.m_allServers.removeServerReference(serverName, this.m_CFU);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void determineRootPage()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2883 */     String replacementServiceName = (AdminServerUtils.m_isSimplifiedServer) ? computeSimpleServerLandingPage() : "GET_ALL_SERVERS_ADMIN_PAGE";
/*      */ 
/* 2887 */     this.m_binder.putLocal("isSimple", "1");
/* 2888 */     this.m_requestImplementor.executeReplacementService(this, replacementServiceName);
/*      */   }
/*      */ 
/*      */   public String computeSimpleServerLandingPage()
/*      */   {
/* 2900 */     String landingPage = SharedObjects.getEnvironmentValue("AdminSimpleServerLandingPage");
/* 2901 */     if ((landingPage == null) || (landingPage.length() == 0))
/*      */     {
/* 2903 */       boolean allowRestart = DataBinderUtils.getBoolean(this.m_binder, "AllowInternalRestart", false);
/* 2904 */       boolean restartRequired = DataBinderUtils.getBoolean(this.m_binder, "restartRequired", false);
/* 2905 */       boolean inError = getBooleanValue(this.m_administratedServerStatus, "isError", false);
/* 2906 */       landingPage = ((AdminServerUtils.m_directActionsInterface != null) && (((inError) || (allowRestart) || (restartRequired)))) ? "GET_IDC_ADMIN_PAGE" : "GET_COMPONENT_DATA";
/*      */     }
/*      */ 
/* 2909 */     return landingPage;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadAllIdcServerData()
/*      */     throws ServiceException, DataException
/*      */   {
/* 2922 */     DataResultSet dset = new DataResultSet(new String[] { "IDC_Id", "IDC_Name", "description", "serverStatus", "restartRequired", "IDC_Admin_Name", "errorMessage", "ClusterNodeName", "ClusterGroup" });
/*      */ 
/* 2925 */     AdminServerData allServers = (AdminServerData)SharedObjects.getTable("ServerDefinition");
/* 2926 */     if (allServers == null)
/*      */     {
/* 2928 */       return;
/*      */     }
/*      */ 
/* 2932 */     DataResultSet adminDefs = (DataResultSet)allServers.m_serverData.getResultSet("AdminServers");
/*      */ 
/* 2934 */     this.m_binder.addResultSet("AdminServers", adminDefs);
/*      */ 
/* 2937 */     Properties originalProps = (Properties)this.m_binder.getLocalData().clone();
/* 2938 */     for (allServers.first(); allServers.isRowPresent(); allServers.next())
/*      */     {
/* 2941 */       Vector row = dset.createEmptyRow();
/* 2942 */       String server = ResultSetUtils.getValue(allServers, "IDC_Id");
/* 2943 */       String adminServer = ResultSetUtils.getValue(allServers, "IDC_Admin_Name");
/* 2944 */       String clusterNodeName = ResultSetUtils.getValue(allServers, "ClusterNodeName");
/*      */ 
/* 2948 */       Properties localProps = (Properties)originalProps.clone();
/* 2949 */       Properties serverProps = allServers.getLocalData(server);
/* 2950 */       DataBinder.mergeHashTables(localProps, serverProps);
/* 2951 */       this.m_binder.setLocalData(localProps);
/*      */ 
/* 2954 */       row.setElementAt(server, 0);
/* 2955 */       String desc = allServers.getValue(server, "description");
/* 2956 */       row.setElementAt(desc, 2);
/*      */ 
/* 2959 */       row.setElementAt(adminServer, 5);
/* 2960 */       boolean useRemoteServer = requestRequiresRemoteServer(adminServer, clusterNodeName);
/*      */ 
/* 2962 */       IdcMessageContainer container = new BasicIdcMessageContainer();
/*      */       String status;
/* 2963 */       if (!useRemoteServer) {
/*      */         String mixedCaseOutput;
/*      */         String output;
/*      */         String errorMessage;
/*      */         try { mixedCaseOutput = alterProcess("query", server, container);
/* 2968 */           output = mixedCaseOutput.toLowerCase();
/* 2969 */           errorMessage = LocaleUtils.encodeMessage(container.getIdcMessage());
/* 2970 */           this.m_binder.m_blFieldTypes.put("errorMessage", "message");
/* 2971 */           row.setElementAt(errorMessage, 6); }
/*      */         catch (ServiceException e)
/*      */         {
/* 2975 */           mixedCaseOutput = output = "";
/* 2976 */           errorMessage = LocaleUtils.encodeMessage(new IdcMessage(e));
/* 2977 */           this.m_binder.m_blFieldTypes.put("errorMessage", "message");
/* 2978 */           row.setElementAt(errorMessage, 6);
/*      */         }
/* 2980 */         String status = "Unknown";
/* 2981 */         if (output.indexOf("success") >= 0)
/*      */         {
/* 2983 */           status = mixedCaseOutput.substring(mixedCaseOutput.lastIndexOf(":") + 1).trim();
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 2989 */         originalProps.put("multipleAdminServers", "1");
/* 2990 */         this.m_binder.putLocal("IDC_Admin_Name", adminServer);
/* 2991 */         this.m_binder.putLocal("ClusterNodeName", clusterNodeName);
/* 2992 */         this.m_binder.putLocal("RemoteIdcService", "GET_IDC_ADMIN_PAGE");
/* 2993 */         this.m_binder.putLocal("IDC_Id", server);
/* 2994 */         if (!doRequestRemoteAdminService(this.m_binder, true))
/*      */         {
/* 2996 */           String errorMessage = this.m_binder.getLocal("StatusMessage");
/* 2997 */           row.setElementAt(errorMessage, 6);
/*      */         }
/* 2999 */         status = this.m_binder.getLocal("IdcServerStatus");
/* 3000 */         if (status == null)
/*      */         {
/* 3002 */           status = "Unknown";
/*      */         }
/*      */       }
/* 3005 */       row.setElementAt(status, 3);
/*      */ 
/* 3007 */       String doRestart = allServers.getValue(server, "restartRequired");
/* 3008 */       if (doRestart == null)
/*      */       {
/* 3010 */         doRestart = "false";
/*      */       }
/* 3012 */       row.setElementAt(doRestart, 4);
/*      */ 
/* 3014 */       dset.addRow(row);
/*      */     }
/* 3016 */     String rsetName = this.m_currentAction.getParamAt(0);
/* 3017 */     this.m_binder.addResultSet(rsetName, dset);
/* 3018 */     this.m_binder.setLocalData(originalProps);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadServerData()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3028 */     String server = getServerId();
/* 3029 */     AdminServerData allServers = (AdminServerData)SharedObjects.getTable("ServerDefinition");
/*      */ 
/* 3033 */     if (!allServers.serverExists(server))
/*      */     {
/* 3035 */       String msg = LocaleUtils.encodeMessage("csAdminCannotLoadData", null, server);
/*      */ 
/* 3037 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/* 3040 */     allServers.mergeLocalData(server, this.m_binder);
/* 3041 */     if ((server == null) || (server.length() <= 0))
/*      */       return;
/* 3043 */     String serverActions = ResultSetUtils.findValue(allServers, "IDC_Id", server, "serverActions");
/*      */ 
/* 3045 */     this.m_binder.putLocal("serverActions", serverActions);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadServerStatus()
/*      */     throws ServiceException, DataException
/*      */   {
/* 3055 */     if (AdminServerUtils.m_isSimplifiedServer)
/*      */     {
/* 3058 */       determineSimplifiedServerStatus();
/* 3059 */       return;
/*      */     }
/*      */ 
/* 3062 */     String server = getServerId();
/* 3063 */     if (server == null)
/*      */     {
/* 3065 */       throw new DataException("!csAdminIdcNameNotSpecified");
/*      */     }
/*      */ 
/* 3068 */     AdminServerData allServers = (AdminServerData)SharedObjects.getTable("ServerDefinition");
/* 3069 */     allServers.mergeLocalData(server, this.m_binder);
/*      */ 
/* 3071 */     IdcMessageContainer statusMessage = new BasicIdcMessageContainer();
/*      */ 
/* 3074 */     String errorMessage = null;
/*      */     String mixedCaseOutput;
/*      */     String output;
/*      */     try {
/* 3077 */       mixedCaseOutput = alterProcess("query", server, statusMessage);
/* 3078 */       output = mixedCaseOutput.toLowerCase();
/* 3079 */       errorMessage = LocaleUtils.encodeMessage(statusMessage.getIdcMessage());
/* 3080 */       this.m_binder.m_blFieldTypes.put("IdcServerStatusMessage", "message");
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 3084 */       mixedCaseOutput = output = "";
/* 3085 */       errorMessage = LocaleUtils.encodeMessage(new IdcMessage(e));
/* 3086 */       this.m_binder.m_blFieldTypes.put("IdcServerStatusMessage", "message");
/*      */     }
/* 3088 */     if (errorMessage != null)
/*      */     {
/* 3090 */       this.m_binder.putLocal("IdcServerStatusMessage", errorMessage);
/*      */     }
/* 3092 */     String status = "Unknown Status";
/* 3093 */     if (output.indexOf("success") >= 0)
/*      */     {
/* 3095 */       status = mixedCaseOutput.substring(mixedCaseOutput.lastIndexOf(":") + 1).trim();
/*      */     }
/* 3097 */     IdcMessage msg = statusMessage.getIdcMessage();
/* 3098 */     if (msg != null)
/*      */     {
/* 3100 */       this.m_binder.putLocal("IdcServerStatusMessage", LocaleUtils.encodeMessage(msg));
/*      */     }
/*      */ 
/* 3103 */     if (status.indexOf("Stopped") >= 0)
/*      */     {
/* 3105 */       setRestartRequired(server, "false");
/*      */     }
/*      */ 
/* 3109 */     if (!status.startsWith("cs"))
/*      */     {
/* 3111 */       DataResultSet strings = SharedObjects.getTable("ServerStatusStrings");
/* 3112 */       if (null != strings)
/*      */       {
/*      */         try
/*      */         {
/* 3116 */           String[] fieldNames = { "key", "status" };
/* 3117 */           FieldInfo[] fields = ResultSetUtils.createInfoList(strings, fieldNames, true);
/* 3118 */           List row = strings.findRow(fields[1].m_index, status);
/* 3119 */           if (null != row)
/*      */           {
/* 3121 */             status = (String)row.get(fields[0].m_index);
/*      */           }
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/* 3126 */           Report.trace("admin", null, e);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 3131 */     this.m_binder.putLocal("IdcServerStatus", status);
/*      */   }
/*      */ 
/*      */   public String determineSimplifiedServerStatus()
/*      */   {
/* 3137 */     String statusStr = "csServerStatusUnknownStatus";
/* 3138 */     boolean continueLoad = getBooleanValue(this.m_administratedServerStatus, "didAction", false);
/* 3139 */     if (continueLoad)
/*      */     {
/* 3141 */       boolean isRunning = getBooleanValue(this.m_administratedServerStatus, "isRunning", false);
/* 3142 */       if (isRunning)
/*      */       {
/* 3144 */         statusStr = "csServerStatusRunning";
/* 3145 */         continueLoad = false;
/*      */       }
/*      */     }
/* 3148 */     if (continueLoad)
/*      */     {
/* 3150 */       boolean isStarting = getBooleanValue(this.m_administratedServerStatus, "isStarting", false);
/* 3151 */       if (isStarting)
/*      */       {
/* 3153 */         continueLoad = false;
/* 3154 */         statusStr = "csServerStatusStarting";
/*      */       }
/*      */     }
/* 3157 */     if (continueLoad)
/*      */     {
/* 3159 */       boolean isError = getBooleanValue(this.m_administratedServerStatus, "isError", false);
/* 3160 */       if (isError)
/*      */       {
/* 3162 */         Throwable t = (Throwable)this.m_administratedServerStatus.get("exception");
/* 3163 */         if (t != null)
/*      */         {
/* 3165 */           String stackTrace = this.m_requestImplementor.createStackReport(t, null, this);
/* 3166 */           this.m_binder.putLocal("IdcServerErrorStack", stackTrace);
/*      */         }
/* 3168 */         statusStr = "csServerStatusError";
/* 3169 */         continueLoad = false;
/*      */       }
/*      */     }
/* 3172 */     if (continueLoad)
/*      */     {
/* 3176 */       boolean isSuccess = getBooleanValue(this.m_administratedServerStatus, "isSuccess", false);
/* 3177 */       if (isSuccess)
/*      */       {
/* 3179 */         statusStr = "csServerStatusStopped";
/* 3180 */         continueLoad = false;
/*      */       }
/*      */     }
/* 3183 */     this.m_binder.putLocal("IdcServerStatus", statusStr);
/* 3184 */     return statusStr;
/*      */   }
/*      */ 
/*      */   protected void setRestartRequired(String server, String restartRequired)
/*      */     throws ServiceException
/*      */   {
/* 3193 */     if ((AdminServerUtils.m_isSimplifiedServer) && (AdminServerUtils.m_directActionsInterface != null))
/*      */     {
/* 3196 */       executeSimplifiedServerAdminAction("setNeedsRestart");
/* 3197 */       determineSimplifiedServerStatus();
/*      */     }
/*      */     else
/*      */     {
/* 3201 */       updateInternalRestartRequiredState(server, restartRequired);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateInternalRestartRequiredState(String server, String restartRequired)
/*      */   {
/* 3207 */     AdminServerData allServers = (AdminServerData)SharedObjects.getTable("ServerDefinition");
/* 3208 */     Properties props = allServers.getLocalData(server);
/* 3209 */     props.put("restartRequired", restartRequired);
/* 3210 */     if ((server == null) || (server.length() == 0))
/*      */     {
/* 3213 */       this.m_binder.putLocal("restartRequired", restartRequired);
/*      */     }
/* 3215 */     allServers.setLocalData(server, props);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadExtraActionData()
/*      */     throws ServiceException, DataException
/*      */   {
/* 3224 */     AdminServerData allServers = (AdminServerData)SharedObjects.getTable("ServerDefinition");
/* 3225 */     DataResultSet actionData = allServers.getActionDataList();
/* 3226 */     DataResultSet extraActionData = new DataResultSet(new String[] { "actionName", "actionType", "actionCaption", "actionCommand", "actionImage" });
/*      */ 
/* 3229 */     if (actionData.getNumRows() == 0)
/*      */     {
/* 3231 */       return;
/*      */     }
/* 3233 */     String server = getServerId();
/* 3234 */     int nameIndex = ResultSetUtils.getIndexMustExist(actionData, "actionName");
/* 3235 */     for (actionData.first(); actionData.isRowPresent(); actionData.next())
/*      */     {
/* 3237 */       String actionName = actionData.getStringValue(nameIndex);
/* 3238 */       String lcaName = actionName.toLowerCase();
/* 3239 */       if ((!allServers.validateAction(server, actionName)) || (lcaName.equals("stop")) || (lcaName.equals("start")) || (lcaName.equals("restart")) || (lcaName.equals("query")) || (lcaName.equals("read")) || (lcaName.equals("write")))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 3244 */       extraActionData.addRow(actionData.getCurrentRowValues());
/*      */     }
/*      */ 
/* 3248 */     String rsetName = this.m_currentAction.getParamAt(0);
/* 3249 */     this.m_binder.addResultSet(rsetName, extraActionData);
/*      */   }
/*      */ 
/*      */   protected String[] computeManifestZipPaths(DataBinder cmpBinder, String componentName)
/*      */     throws ServiceException
/*      */   {
/* 3255 */     String[] paths = new String[2];
/* 3256 */     String dir = ComponentLocationUtils.computeDefaultComponentDir(cmpBinder.getLocalData(), 1, false, new boolean[1]);
/*      */ 
/* 3259 */     String keyedDir = ComponentLocationUtils.computeDefaultComponentDir(cmpBinder.getLocalData(), 1, true, new boolean[1]);
/*      */ 
/* 3264 */     paths[0] = new StringBuilder().append(FileUtils.directorySlashes(dir)).append(componentName).append("/manifest.zip").toString();
/*      */ 
/* 3267 */     paths[1] = new StringBuilder().append(keyedDir).append(componentName).append("/").toString();
/* 3268 */     return paths;
/*      */   }
/*      */ 
/*      */   protected String parseManifestZipPath() throws ServiceException
/*      */   {
/* 3273 */     String location = this.m_binder.getLocal("componentDir");
/* 3274 */     if ((location == null) || (location.length() == 0))
/*      */     {
/* 3276 */       createServiceException(null, "!csComponentLocationRequired");
/*      */     }
/*      */ 
/* 3279 */     String path = new StringBuilder().append(FileUtils.computePathFromSubstitutionMap(SharedObjects.getSecureEnvironment(), location)).append("manifest.zip").toString();
/*      */ 
/* 3281 */     return path;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadServersFromAdminPeer()
/*      */     throws ServiceException, DataException
/*      */   {
/* 3292 */     boolean isPush = StringUtils.convertToBool(this.m_binder.getLocal("isPush"), false);
/*      */ 
/* 3295 */     DataResultSet serverDefs = (DataResultSet)this.m_allServers.m_serverData.getResultSet("ServerDefinition");
/*      */ 
/* 3297 */     DataResultSet adminDefs = (DataResultSet)this.m_allServers.m_serverData.getResultSet("AdminServers");
/*      */ 
/* 3300 */     DataResultSet fullServerData = this.m_allServers.buildFullInfoForAllServers();
/*      */ 
/* 3303 */     DataBinder recieveBinder = this.m_binder;
/* 3304 */     if (isPush)
/*      */     {
/* 3306 */       DataBinder sendBinder = new DataBinder();
/* 3307 */       sendBinder.addResultSet("AdminServers", adminDefs);
/* 3308 */       sendBinder.addResultSet("ServerDefinition", serverDefs);
/* 3309 */       sendBinder.addResultSet("FullServerData", fullServerData);
/* 3310 */       sendBinder.putLocal("IdcService", "LOAD_SERVERS_FROM_PEER");
/* 3311 */       sendBinder.putLocal("SocketServerAddress", this.m_binder.getLocal("SocketServerAddress"));
/*      */ 
/* 3313 */       sendBinder.putLocal("IdcAdminServerPort", this.m_binder.getLocal("IdcAdminServerPort"));
/*      */ 
/* 3316 */       doRequestRemoteAdminService(sendBinder, false);
/* 3317 */       recieveBinder = sendBinder;
/*      */     }
/*      */ 
/* 3321 */     DataResultSet newServerDefs = (DataResultSet)recieveBinder.getResultSet("ServerDefinition");
/*      */ 
/* 3323 */     DataResultSet newAdminDefs = (DataResultSet)recieveBinder.getResultSet("AdminServers");
/*      */ 
/* 3325 */     DataResultSet newFullServerData = (DataResultSet)recieveBinder.getResultSet("FullServerData");
/*      */ 
/* 3328 */     mergeNewDataOnly(serverDefs, newServerDefs, "IDC_Id", true);
/* 3329 */     mergeNewDataOnly(adminDefs, newAdminDefs, "IDC_Admin_Id", true);
/* 3330 */     mergeNewDataOnly(fullServerData, newFullServerData, "IDC_Id", true);
/*      */ 
/* 3332 */     this.m_allServers.m_serverData.addResultSet("FullServerData", fullServerData);
/* 3333 */     this.m_allServers.serializeAdminServersData(this.m_allServers.m_serverData, this.m_CFU);
/*      */ 
/* 3335 */     if (isPush)
/*      */       return;
/* 3337 */     this.m_binder.addResultSet("ServerDefinition", serverDefs);
/* 3338 */     this.m_binder.addResultSet("AdminServers", adminDefs);
/* 3339 */     this.m_binder.addResultSet("FullServerData", fullServerData);
/*      */   }
/*      */ 
/*      */   protected static void mergeNewDataOnly(DataResultSet currentSet, DataResultSet newSet, String fieldName, boolean errorOnOverwrite)
/*      */     throws DataException
/*      */   {
/* 3357 */     if (currentSet == null)
/*      */     {
/* 3359 */       if (newSet != null)
/*      */       {
/* 3361 */         currentSet = new DataResultSet();
/* 3362 */         currentSet.copy(newSet);
/*      */       }
/* 3364 */       return;
/*      */     }
/*      */ 
/* 3371 */     int curInd = ResultSetUtils.getIndexMustExist(currentSet, fieldName);
/* 3372 */     int newInd = ResultSetUtils.getIndexMustExist(newSet, fieldName);
/* 3373 */     for (currentSet.first(); currentSet.isRowPresent(); currentSet.next())
/*      */     {
/* 3375 */       String curFieldValue = currentSet.getStringValue(curInd);
/*      */ 
/* 3378 */       for (newSet.first(); newSet.isRowPresent(); newSet.next())
/*      */       {
/* 3380 */         String newFieldValue = newSet.getStringValue(newInd);
/* 3381 */         if (!newFieldValue.equals(curFieldValue)) {
/*      */           continue;
/*      */         }
/* 3384 */         Properties currentProps = currentSet.getCurrentRowProps();
/* 3385 */         Properties newProps = newSet.getCurrentRowProps();
/* 3386 */         boolean overwrite = false;
/*      */ 
/* 3388 */         Enumeration e = currentProps.keys();
/*      */         while (true) { if (!e.hasMoreElements())
/*      */             break label224;
/* 3391 */           String key = (String)e.nextElement();
/* 3392 */           String currentKeyValue = currentProps.getProperty(key);
/* 3393 */           String newKeyValue = newProps.getProperty(key);
/*      */ 
/* 3395 */           if (currentKeyValue != null)
/*      */           {
/* 3397 */             if ((newKeyValue == null) || (!newKeyValue.equals(currentKeyValue))) {
/* 3398 */               overwrite = true;
/*      */             }
/*      */ 
/*      */           }
/* 3402 */           else if (newKeyValue != null) {
/* 3403 */             overwrite = true;
/*      */           }
/* 3405 */           label224: if ((overwrite) && (errorOnOverwrite))
/*      */           {
/* 3407 */             Report.warning(null, null, "csCannotMergeSetsFieldValueDoesNotMatch", new Object[] { curFieldValue, key });
/*      */           }
/*      */  }
/*      */ 
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3417 */     currentSet.merge(fieldName, newSet, false);
/*      */   }
/*      */ 
/*      */   protected void shallowCopySubservice(Service service, boolean fromScript)
/*      */   {
/* 3423 */     super.shallowCopySubservice(service, fromScript);
/* 3424 */     if (!service instanceof AdminService)
/*      */       return;
/* 3426 */     AdminService adminService = (AdminService)service;
/* 3427 */     adminService.m_allServers = this.m_allServers;
/* 3428 */     adminService.m_CFU = this.m_CFU;
/* 3429 */     adminService.m_administratedServerStatus = this.m_administratedServerStatus;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadLog()
/*      */     throws ServiceException
/*      */   {
/* 3436 */     ComponentInstaller installer = new ComponentInstaller();
/* 3437 */     installer.setLogLocation(this.m_binder.getLocal("logDataDir"), this.m_binder.getLocal("logFileName"));
/* 3438 */     installer.readLog();
/* 3439 */     this.m_binder.merge(installer.getLog());
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 3444 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101067 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.admin.AdminService
 * JD-Core Version:    0.5.4
 */