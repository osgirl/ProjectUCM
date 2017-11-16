/*     */ package intradoc.apputilities.installer;
/*     */ 
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
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Locale;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class BuildPortal
/*     */   implements SectionInstaller
/*     */ {
/*     */   protected SysInstaller m_installer;
/*     */   protected PromptUser m_prompter;
/*     */   protected int m_unattemptedWork;
/*     */   protected Vector m_warnings;
/*     */   protected ReportSubProgress m_progress;
/*     */ 
/*     */   public BuildPortal()
/*     */   {
/*  64 */     this.m_unattemptedWork = 0;
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  72 */     this.m_installer = installer;
/*  73 */     this.m_prompter = this.m_installer.m_promptUser;
/*     */ 
/*  75 */     if (disposition.equals("always"))
/*     */     {
/*  77 */       boolean skipDatabase = StringUtils.convertToBool(this.m_installer.m_installerConfig.getProperty("SkipDatabase"), false);
/*     */ 
/*  80 */       if (!this.m_installer.m_isUpdate)
/*     */       {
/*  82 */         this.m_installer.prepareForLocks("data/schema/");
/*  83 */         this.m_installer.prepareForLocks("data/pages/");
/*     */       }
/*     */ 
/*  87 */       this.m_progress = new ReportSubProgress(this.m_installer, 0, 6);
/*     */ 
/*  91 */       this.m_warnings = new IdcVector();
/*  92 */       this.m_warnings.addElement("!csInstallerBuildWebPagesError");
/*  93 */       if (!skipDatabase)
/*     */       {
/*  95 */         this.m_warnings.addElement("!csInstallerUserDBPublishError");
/*     */       }
/*     */ 
/*  98 */       Workspace ws = null;
/*     */       try
/*     */       {
/* 101 */         this.m_installer.initServerConfig(this.m_installer.m_idcDir, true);
/* 102 */         IdcSystemLoader.loadComponentData();
/* 103 */         if (!skipDatabase)
/*     */         {
/* 105 */           IdcSystemLoader.initProviders(0);
/*     */         }
/*     */ 
/* 108 */         String serverLocale = installer.m_intradocConfig.getProperty("SystemLocale");
/* 109 */         if (serverLocale == null)
/*     */         {
/* 111 */           Locale locale = Locale.getDefault();
/* 112 */           DataResultSet drset = SharedObjects.getTable("LanguageLocaleMap");
/* 113 */           if ((drset != null) && (locale != null))
/*     */           {
/* 115 */             serverLocale = ResourceLoader.computeLocale(locale, drset);
/*     */           }
/*     */         }
/* 118 */         if (serverLocale == null)
/*     */         {
/* 120 */           IdcLocale locale = LocaleResources.getLocale("SystemLocale");
/* 121 */           serverLocale = locale.m_name;
/*     */         }
/* 123 */         if (serverLocale == null)
/*     */         {
/* 125 */           serverLocale = "English-US";
/*     */         }
/* 127 */         SharedObjects.putEnvironmentValue("SystemLocale", serverLocale);
/*     */ 
/* 130 */         IdcExtendedLoader extendedLoader = (IdcExtendedLoader)ComponentClassFactory.createClassInstance("IdcExtendedLoader", "intradoc.server.IdcExtendedLoader", "!csInstallerCustomInitLoaderError");
/*     */ 
/* 133 */         IdcSystemLoader.setExtendedLoader(extendedLoader);
/*     */ 
/* 135 */         ActiveState.load();
/*     */ 
/* 137 */         if ((!skipDatabase) && (!this.m_installer.getInstallBool("PublishedUsers", false)))
/*     */         {
/*     */           try
/*     */           {
/* 142 */             this.m_unattemptedWork = 2;
/* 143 */             reportProgress("!csInstallerProgressConfigServer");
/* 144 */             ws = IdcSystemLoader.loadDatabase(1);
/* 145 */             IdcSystemLoader.loadSystemUserDatabase(1);
/*     */ 
/* 147 */             reportProgress("!csInstallerProgressConfigUser");
/* 148 */             extendedLoader.extraBeforeCacheLoadInit(ws);
/* 149 */             UsersSubjectCallback usersCallback = new UsersSubjectCallback();
/* 150 */             usersCallback.setWorkspace(ws);
/* 151 */             usersCallback.cacheUsers();
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 155 */             failure(t);
/*     */           }
/*     */ 
/*     */           try
/*     */           {
/* 160 */             this.m_unattemptedWork = 1;
/*     */ 
/* 162 */             if (ws == null)
/*     */             {
/* 164 */               throw new ServiceException("!csInstallerWorkspaceNotInit");
/*     */             }
/* 166 */             Service service = new Service();
/* 167 */             service.init(ws, null, new DataBinder(), new ServiceData());
/* 168 */             UserServiceHandler handler = new UserServiceHandler(ws);
/* 169 */             handler.init(service);
/* 170 */             handler.updateCache();
/* 171 */             reportProgress("!csInstallerProgressConfigUser");
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 175 */             failure(t);
/*     */           }
/* 177 */           this.m_warnings.removeElementAt(0);
/*     */         }
/*     */         else
/*     */         {
/* 181 */           this.m_warnings.removeElementAt(0);
/* 182 */           reportProgress("!csInstallerProgressConfigServer");
/* 183 */           reportProgress("!csInstallerProgressConfigServer");
/* 184 */           reportProgress("!csInstallerProgressConfigServer");
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/* 190 */           this.m_unattemptedWork = 2;
/*     */ 
/* 192 */           reportProgress("!csInstallerProgressWebConfig");
/* 193 */           LegacyDirectoryLocator.buildWebRoots();
/* 194 */           IdcSystemLoader.loadPageBuilderConfig();
/* 195 */           ComponentLoader.initDefaults();
/* 196 */           IdcSystemLoader.loadComponentData();
/* 197 */           reportProgress("!csInstallerProgressWebConfig");
/* 198 */           IdcSystemLoader.loadIdocScriptExtensions();
/* 199 */           SubjectCallbackAdapter templatesCallback = new TemplatesSubjectCallback();
/* 200 */           templatesCallback.refresh("templates");
/* 201 */           DataLoader.cacheGlobalIncludes();
/* 202 */           DataLoader.cacheTemplateFiles();
/* 203 */           DocProfileManager.init();
/* 204 */           DocProfileManager.load();
/*     */ 
/* 207 */           SearchIndexerUtils.initSearchIndexerConfig();
/*     */ 
/* 209 */           WebPublishUtils.doPublish(ws, null, 8);
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 213 */           failure(t);
/*     */         }
/* 215 */         if (this.m_warnings.size() > 0)
/*     */         {
/* 217 */           this.m_warnings.removeElementAt(0);
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 222 */         IdcMessage msg = IdcMessageFactory.lc(t, "csUnexpectedException", new Object[] { t.getClass().getName() });
/* 223 */         installer.m_installLog.warning(LocaleUtils.encodeMessage(msg));
/* 224 */         Report.trace("install", null, t);
/*     */       }
/*     */ 
/* 227 */       for (int i = 0; i < this.m_warnings.size(); ++i)
/*     */       {
/* 229 */         installer.m_installLog.warning((String)this.m_warnings.elementAt(i));
/*     */       }
/*     */ 
/* 232 */       reportProgress("!csInstallerProgressConfigDone");
/*     */     }
/* 234 */     return 0;
/*     */   }
/*     */ 
/*     */   public void reportProgress(String msg)
/*     */   {
/* 239 */     this.m_progress.m_curProgress += 1;
/* 240 */     this.m_installer.reportProgress(1, msg, this.m_progress.m_curProgress, this.m_progress.m_maxProgress);
/*     */ 
/* 243 */     if (this.m_unattemptedWork <= 0)
/*     */       return;
/* 245 */     this.m_unattemptedWork -= 1;
/*     */   }
/*     */ 
/*     */   public void failure(Throwable t)
/*     */     throws ServiceException
/*     */   {
/* 251 */     String message = t.getMessage();
/* 252 */     String className = t.getClass().getName();
/*     */ 
/* 254 */     Report.trace("install", null, t);
/* 255 */     if (this.m_warnings.size() > 0)
/*     */     {
/* 257 */       String logMessage = LocaleUtils.appendMessage(className, (String)this.m_warnings.elementAt(0));
/*     */ 
/* 259 */       logMessage = LocaleUtils.appendMessage(message, logMessage);
/* 260 */       this.m_installer.m_installLog.warning(logMessage);
/*     */     }
/*     */     else
/*     */     {
/* 264 */       this.m_installer.m_installLog.warning(t.toString());
/*     */     }
/* 266 */     this.m_progress.m_curProgress += this.m_unattemptedWork;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 271 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99914 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.BuildPortal
 * JD-Core Version:    0.5.4
 */