/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportSubProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.server.ActiveState;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.DocProfileManager;
/*     */ import intradoc.server.IdcExtendedLoader;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.server.UserServiceHandler;
/*     */ import intradoc.server.publish.WebPublishUtils;
/*     */ import intradoc.server.subject.TemplatesSubjectCallback;
/*     */ import intradoc.server.subject.UsersSubjectCallback;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Locale;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class PublishWeblayout
/*     */   implements SectionInstaller
/*     */ {
/*     */   protected SysInstaller m_installer;
/*     */   protected PromptUser m_prompter;
/*     */   protected int m_unattemptedWork;
/*     */   protected Vector m_warnings;
/*     */   protected ReportSubProgress m_progress;
/*     */   protected boolean m_isProxied;
/*     */   protected boolean m_isRefinery;
/*     */ 
/*     */   public PublishWeblayout()
/*     */   {
/*  66 */     this.m_unattemptedWork = 0;
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  76 */     this.m_installer = installer;
/*  77 */     this.m_prompter = this.m_installer.m_promptUser;
/*  78 */     this.m_isProxied = this.m_installer.getInstallBool("IsProxiedServer", false);
/*  79 */     this.m_isRefinery = this.m_installer.getInstallBool("IsRefinery", false);
/*     */ 
/*  81 */     if (disposition.equals("always"))
/*     */     {
/*  83 */       boolean skipDatabase = StringUtils.convertToBool(this.m_installer.m_installerConfig.getProperty("SkipDatabase"), false);
/*     */ 
/*  86 */       if (!this.m_installer.m_isUpdate)
/*     */       {
/*  88 */         this.m_installer.prepareForLocks("data/schema/");
/*  89 */         this.m_installer.prepareForLocks("data/pages/");
/*     */       }
/*     */ 
/*  93 */       this.m_progress = new ReportSubProgress(this.m_installer, 0, 6);
/*     */ 
/*  97 */       this.m_warnings = new IdcVector();
/*  98 */       this.m_warnings.addElement("!csInstallerBuildWebPagesError");
/*  99 */       if (!skipDatabase)
/*     */       {
/* 101 */         this.m_warnings.addElement("!csInstallerUserDBPublishError");
/*     */       }
/* 103 */       this.m_warnings.addElement("!csUnableToPublishWeblayout");
/*     */ 
/* 105 */       Workspace ws = null;
/* 106 */       IdcExtendedLoader extendedLoader = null;
/*     */       try
/*     */       {
/* 109 */         boolean runningFromServer = this.m_installer.getInstallBool("RunningFromServer", false);
/* 110 */         if (!runningFromServer)
/*     */         {
/* 112 */           this.m_installer.initServerConfig(this.m_installer.m_idcDir, true);
/* 113 */           IdcSystemLoader.loadComponentData();
/* 114 */           SharedLoader.configureResultSetJoin();
/* 115 */           if (!skipDatabase)
/*     */           {
/* 117 */             IdcSystemLoader.initProviders(0);
/*     */           }
/*     */         }
/*     */ 
/* 121 */         String serverLocale = installer.m_intradocConfig.getProperty("SystemLocale");
/* 122 */         if (serverLocale == null)
/*     */         {
/* 124 */           Locale locale = Locale.getDefault();
/* 125 */           DataResultSet drset = SharedObjects.getTable("LanguageLocaleMap");
/* 126 */           if ((drset != null) && (locale != null))
/*     */           {
/* 128 */             serverLocale = ResourceLoader.computeLocale(locale, drset);
/*     */           }
/*     */         }
/* 131 */         if (serverLocale == null)
/*     */         {
/* 133 */           IdcLocale locale = LocaleResources.getLocale("SystemLocale");
/* 134 */           serverLocale = locale.m_name;
/*     */         }
/* 136 */         if (serverLocale == null)
/*     */         {
/* 138 */           serverLocale = "English-US";
/*     */         }
/*     */ 
/* 141 */         if (!runningFromServer)
/*     */         {
/* 143 */           SharedObjects.putEnvironmentValue("SystemLocale", serverLocale);
/*     */ 
/* 146 */           extendedLoader = (IdcExtendedLoader)ComponentClassFactory.createClassInstance("IdcExtendedLoader", "intradoc.server.IdcExtendedLoader", "!csInstallerCustomInitLoaderError");
/*     */ 
/* 150 */           IdcSystemLoader.setExtendedLoader(extendedLoader);
/*     */ 
/* 152 */           ActiveState.load();
/*     */         }
/*     */ 
/* 155 */         if ((!skipDatabase) && (!this.m_installer.getInstallBool("PublishedUsers", false)))
/*     */         {
/*     */           try
/*     */           {
/* 160 */             if (!runningFromServer)
/*     */             {
/* 162 */               this.m_unattemptedWork = 2;
/* 163 */               reportProgress("!csInstallerProgressConfigServer");
/* 164 */               ws = IdcSystemLoader.loadDatabase(1);
/* 165 */               IdcSystemLoader.loadSystemUserDatabase(1);
/*     */ 
/* 167 */               reportProgress("!csInstallerProgressConfigUser");
/* 168 */               IdcSystemLoader.loadSystemVariables();
/* 169 */               extendedLoader.extraBeforeCacheLoadInit(ws);
/*     */             }
/*     */             else
/*     */             {
/* 173 */               ws = (Workspace)this.m_installer.m_context.getCachedObject("Workspace");
/* 174 */               if (ws == null)
/*     */               {
/* 176 */                 ExecutionContext parentContext = (ExecutionContext)this.m_installer.m_context.getCachedObject("ParentContext");
/*     */ 
/* 178 */                 if (parentContext != null)
/*     */                 {
/* 180 */                   if (parentContext instanceof Service)
/*     */                   {
/* 182 */                     ws = ((Service)parentContext).getWorkspace();
/*     */                   }
/*     */                   else
/*     */                   {
/* 186 */                     ws = (Workspace)parentContext.getCachedObject("Workspace");
/*     */                   }
/*     */                 }
/*     */               }
/* 190 */               this.m_unattemptedWork = 0;
/*     */             }
/*     */ 
/* 193 */             UsersSubjectCallback usersCallback = new UsersSubjectCallback();
/* 194 */             usersCallback.setWorkspace(ws);
/* 195 */             usersCallback.cacheUsers();
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 199 */             failure(t);
/*     */           }
/*     */ 
/*     */           try
/*     */           {
/* 204 */             this.m_unattemptedWork = 1;
/*     */ 
/* 206 */             if (ws == null)
/*     */             {
/* 208 */               throw new ServiceException("!csInstallerWorkspaceNotInit");
/*     */             }
/* 210 */             Service service = new Service();
/* 211 */             service.init(ws, null, new DataBinder(), new ServiceData());
/* 212 */             UserServiceHandler handler = new UserServiceHandler(ws);
/* 213 */             handler.init(service);
/* 214 */             handler.updateCache();
/* 215 */             reportProgress("!csInstallerProgressConfigUser");
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 219 */             failure(t);
/*     */           }
/* 221 */           this.m_warnings.removeElementAt(0);
/*     */         }
/*     */         else
/*     */         {
/* 225 */           this.m_warnings.removeElementAt(0);
/* 226 */           reportProgress("!csInstallerProgressConfigServer");
/* 227 */           reportProgress("!csInstallerProgressConfigServer");
/* 228 */           reportProgress("!csInstallerProgressConfigServer");
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/* 235 */           reportProgress("!csInstallerProgressWebConfig");
/* 236 */           if (runningFromServer)
/*     */           {
/* 238 */             this.m_unattemptedWork = 0;
/*     */           }
/*     */           else
/*     */           {
/* 242 */             this.m_unattemptedWork = 1;
/* 243 */             LegacyDirectoryLocator.buildWebRoots();
/* 244 */             IdcSystemLoader.loadPageBuilderConfig();
/* 245 */             ComponentLoader.initDefaults();
/* 246 */             IdcSystemLoader.loadComponentDataEx(true);
/* 247 */             reportProgress("!csInstallerProgressWebConfig");
/* 248 */             IdcSystemLoader.loadIdocScriptExtensions();
/* 249 */             SubjectCallbackAdapter templatesCallback = new TemplatesSubjectCallback();
/* 250 */             templatesCallback.refresh("templates");
/* 251 */             DataLoader.cacheGlobalIncludes();
/* 252 */             DataLoader.cacheTemplateFiles();
/* 253 */             DocProfileManager.init();
/* 254 */             DocProfileManager.load();
/*     */           }
/*     */ 
/* 257 */           if ((!runningFromServer) && 
/* 259 */             (!this.m_isRefinery))
/*     */           {
/* 262 */             SearchIndexerUtils.initSearchIndexerConfig();
/*     */           }
/*     */ 
/* 266 */           WebPublishUtils.doPublish(ws, null, 16);
/* 267 */           this.m_warnings.removeElementAt(0);
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 271 */           failure(t);
/*     */         }
/* 273 */         if (this.m_warnings.size() > 0)
/*     */         {
/* 275 */           this.m_warnings.removeElementAt(0);
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 280 */         IdcMessage msg = IdcMessageFactory.lc(t, "csUnexpectedException", new Object[] { t.getClass().getName() });
/* 281 */         installer.m_installLog.warning(LocaleUtils.encodeMessage(msg));
/* 282 */         Report.trace("install", null, t);
/*     */       }
/*     */ 
/* 285 */       for (int i = 0; i < this.m_warnings.size(); ++i)
/*     */       {
/* 287 */         installer.m_installLog.warning((String)this.m_warnings.elementAt(i));
/*     */       }
/*     */ 
/* 290 */       reportProgress("!csInstallerProgressConfigDone");
/*     */     }
/* 292 */     return 0;
/*     */   }
/*     */ 
/*     */   public void reportProgress(String msg)
/*     */   {
/* 297 */     this.m_progress.m_curProgress += 1;
/* 298 */     this.m_installer.reportProgress(1, msg, this.m_progress.m_curProgress, this.m_progress.m_maxProgress);
/*     */ 
/* 301 */     if (this.m_unattemptedWork <= 0)
/*     */       return;
/* 303 */     this.m_unattemptedWork -= 1;
/*     */   }
/*     */ 
/*     */   public void failure(Throwable t)
/*     */     throws ServiceException
/*     */   {
/* 309 */     String message = t.getMessage();
/* 310 */     String className = t.getClass().getName();
/*     */ 
/* 312 */     Report.trace("install", null, t);
/* 313 */     if (this.m_warnings.size() > 0)
/*     */     {
/* 315 */       String logMessage = LocaleUtils.appendMessage(className, (String)this.m_warnings.elementAt(0));
/*     */ 
/* 317 */       logMessage = LocaleUtils.appendMessage(message, logMessage);
/* 318 */       this.m_installer.m_installLog.warning(logMessage);
/*     */     }
/*     */     else
/*     */     {
/* 322 */       this.m_installer.m_installLog.warning(t.toString());
/*     */     }
/* 324 */     this.m_progress.m_curProgress += this.m_unattemptedWork;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 329 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99914 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.PublishWeblayout
 * JD-Core Version:    0.5.4
 */