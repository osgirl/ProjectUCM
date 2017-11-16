/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.ClassHelper;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.CryptoCommonUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.GuiUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.GuiText;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.provider.ServerRequestUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.server.IndexerMonitor;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.ServiceManager;
/*     */ import intradoc.server.archive.ArchiverMonitor;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.LocaleLoader;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.Users;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.CharArrayReader;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JFrame;
/*     */ 
/*     */ public class StandAloneApp
/*     */ {
/*  83 */   protected Workspace m_workspace = null;
/*  84 */   protected String m_user = "anonymous";
/*     */   protected SplashFrame m_splashFrame;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected boolean m_isHeavyClient;
/*     */   protected Provider m_provider;
/*  90 */   public static final Class m_frameClass = JFrame.class;
/*     */ 
/*     */   public StandAloneApp()
/*     */   {
/*  94 */     this.m_cxt = new ExecutionContextAdaptor();
/*     */   }
/*     */ 
/*     */   public void init(SplashFrame splashFrame) throws DataException, ServiceException
/*     */   {
/*  99 */     init(null, splashFrame);
/*     */   }
/*     */ 
/*     */   public void init(String appName, SplashFrame splashFrame) throws DataException, ServiceException
/*     */   {
/* 104 */     this.m_splashFrame = splashFrame;
/* 105 */     boolean reportProgress = false;
/* 106 */     if (this.m_splashFrame != null)
/*     */     {
/* 110 */       reportProgress = true;
/* 111 */       this.m_splashFrame.m_windowHelper.m_exitOnClose = true;
/*     */     }
/* 113 */     initSystem(reportProgress);
/*     */ 
/* 115 */     if (splashFrame != null)
/*     */     {
/* 118 */       GuiText.localize(this.m_cxt);
/*     */ 
/* 120 */       if (this.m_isHeavyClient)
/*     */       {
/* 123 */         this.m_cxt.setCachedObject("UserLocale", LocaleResources.getSystemLocale());
/*     */       }
/*     */       else
/*     */       {
/* 128 */         loginUser(appName);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 134 */     if (this.m_splashFrame != null)
/*     */     {
/* 136 */       AppLauncher.m_dateFormat = LocaleResources.getSystemDateFormat();
/*     */     }
/* 138 */     LocaleLoader.doStaticLocalization(this.m_cxt);
/*     */   }
/*     */ 
/*     */   public void initSystem(boolean doProgressReport) throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 145 */       IntervalData interval = new IntervalData("initSystem");
/* 146 */       IdcSystemConfig.loadInitialConfig();
/* 147 */       IdcSystemConfig.loadAppConfigInfo();
/* 148 */       String midPointStartPlace = null;
/* 149 */       this.m_isHeavyClient = SharedObjects.getEnvValueAsBoolean("IsClient", false);
/*     */ 
/* 151 */       if (this.m_isHeavyClient)
/*     */       {
/* 154 */         IdcSystemConfig.initLocalization(IdcSystemConfig.F_STANDARD_SERVER);
/* 155 */         IdcSystemConfig.configLocalization();
/* 156 */         IdcSystemLoader.initComponentData();
/* 157 */         IdcSystemLoader.loadProviderConfiguration();
/* 158 */         IdcSystemLoader.loadProviders();
/*     */ 
/* 160 */         Provider p = null;
/* 161 */         String myProvider = SharedObjects.getEnvironmentValue("ServerProvider");
/* 162 */         if (myProvider == null)
/*     */         {
/* 165 */           DataBinder secInfo = new DataBinder();
/* 166 */           String dir = LegacyDirectoryLocator.getUserPublishCacheDir();
/* 167 */           String file = "SecurityInfo.hda";
/* 168 */           ResourceUtils.serializeDataBinderWithEncoding(dir, file, secInfo, 0, null);
/*     */           try
/*     */           {
/* 172 */             DataBinder providerData = new DataBinder();
/* 173 */             providerData.putLocal("pName", "<auto>");
/* 174 */             providerData.putLocal("ServerPort", secInfo.get("IntradocServerPort"));
/* 175 */             providerData.putLocal("ProviderClass", "intradoc.provider.SocketOutgoingProvider");
/* 176 */             providerData.putLocal("ProviderConnection", "intradoc.provider.SocketOutgoingConnection");
/* 177 */             providerData.putLocal("ProviderType", "outgoing");
/*     */ 
/* 179 */             String idcName = secInfo.getLocal("IDC_Name");
/* 180 */             DataBinder proxyInfo = new DataBinder();
/* 181 */             ResourceUtils.serializeDataBinderWithEncoding(dir, "ProxyAuthorization.hda", proxyInfo, 0, null);
/*     */ 
/* 183 */             String authInfo = proxyInfo.getLocal(idcName + ":IdcProxyAuth");
/* 184 */             if (authInfo != null)
/*     */             {
/* 186 */               DataBinder passInfo = new DataBinder();
/* 187 */               char[] chars = new char[authInfo.length()];
/* 188 */               authInfo.getChars(0, chars.length, chars, 0);
/* 189 */               passInfo.receive(new BufferedReader(new CharArrayReader(chars)));
/* 190 */               String pass = passInfo.get("password");
/* 191 */               if (pass != null)
/*     */               {
/* 193 */                 DataBinder authInfoBinder = new DataBinder();
/*     */ 
/* 196 */                 pass = CryptoCommonUtils.sha1UuencodeHash(pass, null);
/* 197 */                 authInfoBinder.putLocal("password", pass);
/* 198 */                 authInfoBinder.putLocal("encoding", "");
/*     */ 
/* 200 */                 IdcCharArrayWriter writer = new IdcCharArrayWriter();
/* 201 */                 authInfoBinder.sendEx(writer, false);
/* 202 */                 providerData.putLocal("IdcProxyAuth", writer.toString());
/* 203 */                 FileUtils.discard(writer);
/*     */               }
/*     */             }
/*     */ 
/* 207 */             String hostName = secInfo.getLocal("IntradocServerHostName");
/* 208 */             if (hostName == null)
/*     */             {
/* 210 */               hostName = "localhost";
/*     */             }
/* 212 */             providerData.putLocal("IntradocServerHostName", hostName);
/*     */ 
/* 214 */             p = new Provider(providerData);
/* 215 */             p.init();
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 219 */             throw new ServiceException(e, "csServerProviderConfFailed", new Object[0]);
/*     */           }
/*     */         }
/*     */ 
/* 223 */         if (p == null)
/*     */         {
/* 225 */           p = Providers.getProvider(myProvider);
/* 226 */           if (p == null)
/*     */           {
/* 228 */             throw new ServiceException(null, "csOutgoingProviderNotConfigured2", new Object[] { myProvider });
/*     */           }
/*     */         }
/*     */ 
/* 232 */         this.m_provider = p;
/*     */ 
/* 236 */         Class myClassObject = super.getClass();
/* 237 */         Method terminateMethod = myClassObject.getMethod("terminateAtUserRequest", new Class[0]);
/*     */         try
/*     */         {
/* 246 */           Object callbackHandler = ComponentClassFactory.createClassInstance("IdcGUILoginCallbackHandler", "idc.gui.IdcGUILoginCallbackHandler", null);
/*     */ 
/* 249 */           String title = LocaleResources.getString("csLogin", this.m_cxt);
/* 250 */           ClassHelperUtils.executeMethod(callbackHandler, "init", new Object[] { this.m_splashFrame, title }, new Class[] { m_frameClass, ClassHelper.m_stringClass });
/* 251 */           ClassHelperUtils.executeMethod(callbackHandler, "addCancelCallback", new Object[] { this, terminateMethod, new Object[0] }, new Class[] { ClassHelper.m_objectClass, terminateMethod.getClass(), ClassHelper.m_objectArrayClass });
/*     */ 
/* 254 */           p.addProviderObject("AuthCallbackHandler", callbackHandler);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 258 */           Report.trace("startup", "unable to create login handler", e);
/*     */         }
/*     */ 
/* 261 */         IdcSystemLoader.registerProviders();
/* 262 */         interval.traceAndRestart("startup", "loadInitialConfig() -> registerProviders()");
/* 263 */         midPointStartPlace = "loadComponentDataEx()";
/* 264 */         IdcSystemLoader.loadComponentDataEx(true);
/* 265 */         IdcSystemLoader.configureAppObjects();
/*     */ 
/* 267 */         p.startProvider(true);
/* 268 */         String startupMessage = p.getReportString("startup");
/* 269 */         if (startupMessage != null)
/*     */         {
/* 271 */           startupMessage = LocaleResources.localizeMessage(startupMessage, null);
/*     */ 
/* 273 */           SystemUtils.outln(startupMessage);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 278 */         IdcSystemLoader.init(false);
/*     */ 
/* 280 */         interval.traceAndRestart("startup", "loadInitialConfig() -> init()");
/* 281 */         midPointStartPlace = "finishLoad()";
/* 282 */         finishLoad(doProgressReport);
/*     */       }
/* 284 */       interval.traceAndRestart("startup", midPointStartPlace + "->finish");
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 288 */       Report.trace("applet", null, e);
/* 289 */       if (e instanceof DataException)
/*     */       {
/* 291 */         throw ((DataException)e);
/*     */       }
/*     */ 
/* 297 */       throw new ServiceException(e);
/*     */     }
/*     */     finally
/*     */     {
/* 301 */       if (this.m_workspace != null)
/*     */       {
/* 303 */         Providers.releaseConnections();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void finishLoad(boolean doProgressReport)
/*     */     throws DataException, ServiceException
/*     */   {
/* 311 */     if (doProgressReport)
/*     */     {
/*     */       try
/*     */       {
/* 317 */         if (System.getProperties().getProperty("os.name").indexOf("Windows") >= 0)
/*     */         {
/* 319 */           this.m_splashFrame.setIconImage(GuiUtils.getAppImage("app-icon.gif"));
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 324 */         if (SystemUtils.m_verbose)
/*     */         {
/* 326 */           Report.debug("applet", null, e);
/*     */         }
/*     */       }
/*     */ 
/* 330 */       this.m_splashFrame.setVisible(true);
/* 331 */       this.m_splashFrame.reportProgress(1, LocaleResources.getString("csDatabaseConnect", this.m_cxt), 10.0F, 100.0F);
/*     */     }
/*     */ 
/* 335 */     this.m_workspace = IdcSystemLoader.loadDatabase(4);
/* 336 */     IdcSystemLoader.loadSystemUserDatabase(2);
/*     */ 
/* 339 */     if (doProgressReport)
/*     */     {
/* 341 */       this.m_splashFrame.reportProgress(1, LocaleResources.getString("csLoadCaches", this.m_cxt), 50.0F, 100.0F);
/*     */     }
/*     */ 
/* 345 */     IdcSystemLoader.loadCaches(this.m_workspace);
/* 346 */     IdcSystemLoader.validateStandardDirectories();
/*     */ 
/* 349 */     if (doProgressReport)
/*     */     {
/* 351 */       this.m_splashFrame.reportProgress(1, LocaleResources.getString("csLoadConfig", this.m_cxt), 70.0F, 100.0F);
/*     */     }
/*     */ 
/* 356 */     if (doProgressReport)
/*     */     {
/* 358 */       this.m_splashFrame.reportProgress(1, LocaleResources.getString("csCacheMonitor", this.m_cxt), 80.0F, 100.0F);
/*     */     }
/*     */ 
/* 362 */     IdcSystemLoader.startCacheMonitoring(this.m_workspace);
/*     */ 
/* 367 */     IdcSystemLoader.loadServiceData();
/*     */ 
/* 369 */     startProviders();
/*     */ 
/* 371 */     if (!doProgressReport)
/*     */       return;
/* 373 */     this.m_splashFrame.reportProgress(1, LocaleResources.getString("csInitFinished", this.m_cxt), 100.0F, 100.0F);
/*     */   }
/*     */ 
/*     */   public void startProviders()
/*     */     throws DataException, ServiceException
/*     */   {
/* 381 */     IdcSystemLoader.registerProviders();
/* 382 */     IdcSystemLoader.startProviders(true);
/*     */   }
/*     */ 
/*     */   public void doStandaloneAppInit(String appName)
/*     */     throws ServiceException
/*     */   {
/* 390 */     if ((appName.equalsIgnoreCase("archiver")) || (appName.equalsIgnoreCase("repoman")) || (appName.equalsIgnoreCase("configman")))
/*     */     {
/* 393 */       IdcSystemLoader.initReplicationData();
/* 394 */       IdcSystemLoader.initArchiver(this.m_workspace, false);
/*     */     }
/*     */ 
/* 397 */     if ((appName.equalsIgnoreCase("workflow")) || (appName.equalsIgnoreCase("repoman")))
/*     */     {
/* 399 */       IdcSystemLoader.initWorkflow(this.m_workspace, false);
/*     */     }
/*     */ 
/* 402 */     if (appName.equalsIgnoreCase("weblayout"))
/*     */     {
/* 404 */       IdcSystemLoader.loadPageBuilderConfig();
/*     */     }
/* 406 */     else if ((appName.equalsIgnoreCase("repoman")) && (!this.m_isHeavyClient))
/*     */     {
/* 408 */       IdcSystemLoader.initSearchIndexer(this.m_workspace, false);
/*     */     }
/* 410 */     else if ((appName.equalsIgnoreCase("configman")) && (!this.m_isHeavyClient))
/*     */     {
/* 412 */       IdcSystemLoader.loadPageBuilderConfig();
/* 413 */       IdcSystemLoader.initSearchIndexer(this.m_workspace, false);
/* 414 */       IndexerMonitor.init(LegacyDirectoryLocator.getSearchDirectory(), this.m_workspace, null);
/*     */     }
/*     */ 
/* 419 */     IdcSystemLoader.checkCache();
/*     */   }
/*     */ 
/*     */   public void executeService(String action, DataBinder binder) throws ServiceException
/*     */   {
/* 424 */     ServiceManager smg = null;
/*     */     try
/*     */     {
/* 427 */       if (this.m_isHeavyClient)
/*     */       {
/* 429 */         binder.putLocal("IdcService", action);
/* 430 */         DataBinder outBinder = new DataBinder();
/* 431 */         ServerRequestUtils.doAdminProxyRequest(this.m_provider, binder, outBinder, this.m_cxt);
/*     */ 
/* 433 */         binder.merge(outBinder);
/* 434 */         DataSerializeUtils.setSystemEncoding(outBinder.m_javaEncoding);
/* 435 */         String statusCodeString = binder.getLocal("StatusCode");
/* 436 */         int statusCode = NumberUtils.parseInteger(statusCodeString, 0);
/* 437 */         if (statusCode != 0)
/*     */         {
/* 439 */           IdcMessage msg = IdcMessageFactory.lc();
/* 440 */           msg.m_msgLocalized = binder.getLocal("StatusMessage");
/* 441 */           ServiceException e = new ServiceException(null, statusCode, msg);
/* 442 */           throw e;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 447 */         binder.putLocal("IdcService", action);
/* 448 */         binder.setEnvironmentValue("REMOTE_USER", this.m_user);
/* 449 */         smg = new ServiceManager();
/* 450 */         smg.init(binder, this.m_workspace);
/* 451 */         smg.processCommand();
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 465 */       if (smg != null)
/*     */       {
/* 467 */         cleanUpServiceManager(smg);
/*     */       }
/* 469 */       if (this.m_workspace != null)
/*     */       {
/* 471 */         Providers.releaseConnections();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cleanUpServiceManager(ServiceManager smg)
/*     */   {
/* 478 */     if (smg == null)
/*     */       return;
/*     */     try
/*     */     {
/* 482 */       smg.cleanup();
/* 483 */       smg.clear();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 487 */       Report.trace("system", null, t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateUserAccess(UserData userData, String appName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 497 */     DataBinder binder = new DataBinder();
/* 498 */     binder.putLocal("dUserName", userData.m_name);
/* 499 */     ResultSet rset = WorkspaceUtils.getWorkspace("user").createResultSet("QuserSecurityAttributes", binder);
/*     */ 
/* 501 */     String[][] attribInfo = ResultSetUtils.createStringTable(rset, new String[] { "dAttributeType", "dAttributeName", "dAttributePrivilege" });
/*     */ 
/* 503 */     userData.setAttributes(attribInfo);
/*     */ 
/* 505 */     if (SecurityUtils.isUserOfRole(userData, "admin"))
/*     */     {
/* 507 */       return true;
/*     */     }
/* 509 */     if (appName != null)
/*     */     {
/* 514 */       int rights = SecurityAccessListUtils.getRightsForApp(appName);
/* 515 */       if (rights > 0)
/*     */       {
/* 517 */         int priv = SecurityUtils.determineGroupPrivilege(userData, "#AppsGroup");
/* 518 */         return (priv & rights) != 0;
/*     */       }
/*     */     }
/* 521 */     return false;
/*     */   }
/*     */ 
/*     */   public void setupUserAccess(UserData userData) throws ServiceException
/*     */   {
/* 526 */     this.m_user = userData.m_name;
/* 527 */     String localeName = userData.getProperty("dUserLocale");
/* 528 */     IdcLocale locale = null;
/* 529 */     if ((localeName == null) || (localeName.length() == 0))
/*     */     {
/* 531 */       localeName = "SystemLocale";
/*     */     }
/* 533 */     locale = LocaleResources.getLocale(localeName);
/* 534 */     if (locale == null)
/*     */     {
/* 536 */       IdcMessage msg = IdcMessageFactory.lc("csLocaleNotFoundUsingSystem", new Object[] { localeName });
/* 537 */       AppLauncher.reportError(new AppFrameHelper(), msg);
/* 538 */       locale = LocaleResources.getLocale("SystemLocale");
/*     */     }
/* 540 */     this.m_cxt.setCachedObject("UserLocale", locale);
/*     */ 
/* 543 */     LegacyDirectoryLocator.computeAndSetSystemHelpRoot(this.m_cxt);
/*     */   }
/*     */ 
/*     */   public void loginUser(String appName) throws ServiceException
/*     */   {
/* 548 */     boolean useSecurity = SharedObjects.getEnvValueAsBoolean("UseSecurity", true);
/*     */ 
/* 550 */     if (useSecurity)
/*     */     {
/* 552 */       String user = System.getProperty("user.name");
/* 553 */       if ((user != null) && (SharedObjects.getEnvValueAsBoolean("EnableTrustedSignon", false)))
/*     */       {
/* 556 */         Users users = (Users)SharedObjects.getTable("Users");
/* 557 */         UserData userData = users.getLocalUserData(user);
/* 558 */         if (userData != null)
/*     */         {
/*     */           try
/*     */           {
/* 562 */             if (validateUserAccess(userData, appName))
/*     */             {
/* 564 */               setupUserAccess(userData);
/* 565 */               return;
/*     */             }
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 570 */             Report.trace("applet", null, e);
/*     */           }
/*     */         }
/*     */       }
/* 574 */       String title = LocaleResources.getString("csLogin", this.m_cxt);
/* 575 */       LoginDlg loginDlg = new LoginDlg(this.m_splashFrame, title);
/* 576 */       String aName = appName;
/* 577 */       DialogCallback validateUser = new DialogCallback(loginDlg, aName)
/*     */       {
/*     */         public boolean handleDialogEvent(ActionEvent e)
/*     */         {
/*     */           try
/*     */           {
/* 584 */             UserData userData = this.val$loginDlg.getUserData();
/* 585 */             boolean bool = StandAloneApp.this.validateUserAccess(userData, this.val$aName);
/*     */ 
/* 593 */             return bool;
/*     */           }
/*     */           catch (Exception except)
/*     */           {
/* 589 */             this.m_errorMessage = LocaleUtils.createMessageListFromThrowable(except);
/*     */           }
/*     */           finally
/*     */           {
/* 593 */             Providers.releaseConnections();
/*     */           }
/* 595 */           return false;
/*     */         }
/*     */       };
/* 599 */       loginDlg.init(validateUser);
/* 600 */       if (loginDlg.prompt() == 0)
/*     */       {
/* 602 */         terminateAtUserRequest();
/* 603 */         return;
/*     */       }
/*     */ 
/* 606 */       this.m_splashFrame.setVisible(false);
/* 607 */       UserData userData = loginDlg.getUserData();
/* 608 */       setupUserAccess(userData);
/*     */     }
/*     */     else
/*     */     {
/* 612 */       this.m_user = "anonymous";
/*     */     }
/*     */   }
/*     */ 
/*     */   public void terminateAtUserRequest()
/*     */   {
/* 618 */     System.exit(0);
/*     */   }
/*     */ 
/*     */   public String getUser()
/*     */   {
/* 623 */     return this.m_user;
/*     */   }
/*     */ 
/*     */   public ExecutionContext getUserContext()
/*     */   {
/* 628 */     return this.m_cxt;
/*     */   }
/*     */ 
/*     */   public void setUser(String user)
/*     */   {
/* 633 */     this.m_user = user;
/*     */   }
/*     */ 
/*     */   public void setReportProgressCallback(String type, ReportProgress rp)
/*     */   {
/* 638 */     if (type.equals("indexer"))
/*     */     {
/* 640 */       IndexerMonitor.setReportProgressCallback(rp);
/*     */     } else {
/* 642 */       if (!type.equals("archiver"))
/*     */         return;
/* 644 */       ArchiverMonitor.setReportProgressCallback(rp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setSplashFrame(SplashFrame sf)
/*     */   {
/* 650 */     this.m_splashFrame = sf;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 655 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99914 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.StandAloneApp
 * JD-Core Version:    0.5.4
 */