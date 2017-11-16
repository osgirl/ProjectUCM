/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.apputilities.installer.InstallLog;
/*     */ import intradoc.apputilities.installer.StartInstaller;
/*     */ import intradoc.apputilities.installer.SysInstaller;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.shared.InstallInterface;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ServerInstallUtils
/*     */   implements ReportProgress
/*     */ {
/*     */   public ExecutionContext m_context;
/*     */   public Properties m_installerConfig;
/*     */   public StartInstaller m_starter;
/*     */   public DataBinder m_installerDefinition;
/*     */   public String m_installScratchDirectory;
/*     */   public ReportProgress m_reportProgress;
/*     */   public InstallLog m_log;
/*     */   public InstallInterface m_installer;
/*     */   public ServerInstallUtilsPromptUser m_prompter;
/*     */ 
/*     */   public ServerInstallUtils(ExecutionContext context)
/*     */   {
/*  58 */     this.m_context = context;
/*     */   }
/*     */ 
/*     */   public void constructInstaller(String intradocDir)
/*     */     throws DataException, ServiceException
/*     */   {
/*  64 */     Map args = new HashMap();
/*  65 */     args.put("IntradocDir", intradocDir);
/*  66 */     constructInstallerWithArgs(args);
/*     */   }
/*     */ 
/*     */   public void constructInstallerWithArgs(Map<String, String> args)
/*     */     throws DataException, ServiceException
/*     */   {
/*  72 */     String intradocDir = (String)args.get("IntradocDir");
/*  73 */     if (this.m_installerConfig == null)
/*     */     {
/*  75 */       this.m_installerConfig = new IdcProperties(SharedObjects.getSecureEnvironment());
/*  76 */       this.m_installerConfig.put("RunningFromInstalledServer", "true");
/*  77 */       this.m_installerConfig.put("RunningFromServer", "true");
/*     */ 
/*  80 */       this.m_installerConfig.put("SourceDirectory", intradocDir);
/*  81 */       this.m_installerConfig.put("IntradocDir", intradocDir);
/*     */ 
/*  83 */       String hasCfg = (String)args.get("NoCfgActivity");
/*  84 */       if (hasCfg != null)
/*     */       {
/*  86 */         this.m_installerConfig.put("NoCfgActivity", hasCfg);
/*     */       }
/*     */     }
/*     */ 
/*  90 */     if (this.m_starter == null)
/*     */     {
/*  92 */       this.m_starter = new StartInstaller();
/*     */     }
/*     */ 
/*  95 */     if (this.m_installerDefinition == null)
/*     */     {
/*     */       try
/*     */       {
/*  99 */         this.m_installerDefinition = this.m_starter.readInstallerDefinition(this.m_installerConfig);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 104 */         throw new ServiceException(e);
/*     */       }
/*     */     }
/*     */ 
/* 108 */     if (this.m_installScratchDirectory == null)
/*     */     {
/* 110 */       this.m_installScratchDirectory = FileUtils.directorySlashes(intradocDir + "/install");
/*     */     }
/*     */ 
/* 113 */     if (this.m_reportProgress == null)
/*     */     {
/* 115 */       this.m_reportProgress = this;
/*     */     }
/*     */ 
/* 120 */     this.m_prompter = new ServerInstallUtilsPromptUser();
/*     */ 
/* 122 */     if (this.m_log == null)
/*     */     {
/* 124 */       this.m_log = new ServerInstallUtilsInstallLog(this.m_prompter);
/* 125 */       this.m_log.setLogDirectory(this.m_installScratchDirectory);
/*     */     }
/*     */ 
/* 128 */     SysInstaller installer = new SysInstaller();
/* 129 */     this.m_installer = installer;
/* 130 */     installer.init(this.m_installerDefinition, this.m_installerConfig, new IdcProperties(), this.m_log, this.m_reportProgress, this.m_prompter);
/*     */ 
/* 132 */     installer.m_intradocConfig = SharedObjects.getSecureEnvironment();
/* 133 */     String IDCName = (String)args.get("IDC_Name");
/* 134 */     if (IDCName != null)
/*     */     {
/* 136 */       installer.m_intradocConfig.put("IDC_Name", IDCName);
/*     */     }
/* 138 */     installer.m_context.setCachedObject("ParentContext", this.m_context);
/*     */   }
/*     */ 
/*     */   public void doInstall() throws ServiceException
/*     */   {
/* 143 */     IdcVector results = new IdcVector();
/* 144 */     int rc = this.m_installer.doInstall(results);
/* 145 */     if (rc != 0)
/*     */     {
/* 147 */       ServiceException se = new ServiceException("!csInstallerFailedToExecuteConfigurationChange");
/* 148 */       int size = results.size();
/* 149 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 151 */         Object msgObj = results.get(i);
/* 152 */         Throwable t = null;
/* 153 */         if (msgObj instanceof Throwable)
/*     */         {
/* 155 */           t = (Throwable)msgObj;
/*     */         }
/* 157 */         else if (msgObj instanceof String)
/*     */         {
/* 159 */           t = new ServiceException((String)msgObj);
/*     */         }
/* 161 */         if (t == null)
/*     */         {
/* 163 */           t = new ServiceException(null, "syNullPointerException", new Object[0]);
/*     */         }
/* 165 */         Report.trace("system", null, t);
/* 166 */         se.addCause(t);
/*     */       }
/* 168 */       throw se;
/*     */     }
/*     */ 
/* 171 */     IdcMessage rootMessage = IdcMessageFactory.lc("csInstallerFailedToExecuteConfigurationChange", new Object[0]);
/* 172 */     IdcMessage currentMessage = rootMessage;
/* 173 */     for (String msg : this.m_prompter.m_messages)
/*     */     {
/* 175 */       IdcMessage tmpMsg = IdcMessageFactory.lc();
/* 176 */       tmpMsg.m_msgLocalized = msg;
/* 177 */       currentMessage.m_prior = tmpMsg;
/* 178 */       currentMessage = tmpMsg;
/*     */     }
/* 180 */     if (rootMessage.m_prior == null)
/*     */       return;
/* 182 */     throw new ServiceException(null, rootMessage);
/*     */   }
/*     */ 
/*     */   public static boolean isLibraryServer()
/*     */   {
/* 191 */     return SharedObjects.getEnvValueAsBoolean("IsLibraryServer", false);
/*     */   }
/*     */ 
/*     */   public static boolean isCatalogServer()
/*     */   {
/* 196 */     return SharedObjects.getEnvValueAsBoolean("IsCatalogServer", false);
/*     */   }
/*     */ 
/*     */   public static boolean isUserServer()
/*     */   {
/* 201 */     return SharedObjects.getEnvValueAsBoolean("IsUserServer", false);
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, float progress, float total)
/*     */   {
/* 207 */     Report.trace("install", msg, null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 212 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97679 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ServerInstallUtils
 * JD-Core Version:    0.5.4
 */