/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceManager;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class GenerateSQLInstaller
/*     */   implements SectionInstaller
/*     */ {
/*     */   protected SysInstaller m_installer;
/*     */   protected PromptUser m_prompter;
/*     */   protected int m_unattemptedWork;
/*     */   protected Vector m_warnings;
/*     */ 
/*     */   public GenerateSQLInstaller()
/*     */   {
/*  37 */     this.m_unattemptedWork = 0;
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  44 */     this.m_installer = installer;
/*  45 */     this.m_prompter = this.m_installer.m_promptUser;
/*     */ 
/*  47 */     if (disposition.equals("always"))
/*     */     {
/*  51 */       this.m_warnings = new IdcVector();
/*     */ 
/*  53 */       Workspace ws = null;
/*     */       try
/*     */       {
/*  56 */         this.m_installer.initServerConfig(this.m_installer.m_idcDir, true);
/*     */ 
/*  58 */         IdcSystemLoader.initComponentData();
/*  59 */         IdcSystemLoader.loadComponentDataEx(false);
/*     */ 
/*  61 */         ServiceManager sm = new ServiceManager();
/*  62 */         sm.init(new DataBinder(), ws);
/*  63 */         sm.loadServiceScripts();
/*     */         try
/*     */         {
/*  68 */           this.m_unattemptedWork = 2;
/*  69 */           DataBinder binder = new DataBinder();
/*     */ 
/*  71 */           String command = "GENERATE_SQL_INSTALL";
/*  72 */           Service service = ServiceManager.getInitializedService(command, binder, ws);
/*     */ 
/*  75 */           service.createHandlersForService();
/*  76 */           service.initHandlers();
/*  77 */           service.executeActions();
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/*  81 */           failure(t);
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/*  86 */         installer.m_installLog.warning(LocaleUtils.encodeMessage("csUnexpectedException", t.getMessage()));
/*     */ 
/*  89 */         SystemUtils.dumpException("install", t);
/*     */       }
/*     */ 
/*  92 */       for (int i = 0; i < this.m_warnings.size(); ++i)
/*     */       {
/*  94 */         installer.m_installLog.warning((String)this.m_warnings.elementAt(i));
/*     */       }
/*     */     }
/*     */ 
/*  98 */     return 0;
/*     */   }
/*     */ 
/*     */   public void failure(Throwable t) throws ServiceException
/*     */   {
/* 103 */     String message = t.getMessage();
/* 104 */     String className = t.getClass().getName();
/*     */ 
/* 106 */     SystemUtils.dumpException("install", t);
/* 107 */     if (this.m_warnings.size() > 0)
/*     */     {
/* 109 */       String logMessage = LocaleUtils.appendMessage(className, (String)this.m_warnings.elementAt(0));
/*     */ 
/* 111 */       logMessage = LocaleUtils.appendMessage(message, logMessage);
/* 112 */       this.m_installer.m_installLog.warning(logMessage);
/*     */     }
/*     */     else
/*     */     {
/* 116 */       this.m_installer.m_installLog.warning(t.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 122 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78801 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.GenerateSQLInstaller
 * JD-Core Version:    0.5.4
 */