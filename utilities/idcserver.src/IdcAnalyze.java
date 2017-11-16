/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apputilities.idcanalyze.IdcAnalyzeApp;
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ 
/*     */ public class IdcAnalyze
/*     */ {
/*  36 */   public static String[][] m_appsInfo = { { "IdcAnalyze", "intradoc.apputilities.idcanalyze.IdcAnalyzeFrame", "csIDCAnalyzeFrameTitle", "", "", "0" } };
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*  50 */     boolean useGUI = false;
/*     */     try
/*     */     {
/*  54 */       AppObjectRepository.putObject("CommandLine", args);
/*     */ 
/*  56 */       useGUI = checkCommandLine(args, "GUI");
/*  57 */       if (!useGUI)
/*     */       {
/*  59 */         useGUI = checkCommandLine(args, "G");
/*     */       }
/*  61 */       if (!useGUI)
/*     */       {
/*  63 */         doBackgroundProcess(args);
/*     */       }
/*     */       else
/*     */       {
/*  67 */         AppLauncher.init("IdcAnalyze", true, m_appsInfo);
/*  68 */         AppLauncher.launch("IdcAnalyze");
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  73 */       SystemUtils.handleFatalException(e, IdcMessageFactory.lc("csIDCAnalyzeInitError", new Object[0]), -1);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void doBackgroundProcess(String[] args)
/*     */     throws DataException, ServiceException
/*     */   {
/*  83 */     IdcSystemConfig.loadInitialConfig();
/*  84 */     IdcSystemConfig.loadAppConfigInfo();
/*  85 */     IdcSystemConfig.initLocalization(IdcSystemConfig.F_STANDARD_SERVER);
/*  86 */     IdcSystemConfig.configLocalization();
/*     */ 
/*  88 */     IdcSystemLoader.finishInit(false);
/*     */ 
/*  92 */     IdcAnalyzeApp analyzeApp = new IdcAnalyzeApp(true);
/*  93 */     analyzeApp.analyze();
/*     */   }
/*     */ 
/*     */   static boolean checkCommandLine(String[] args, String command)
/*     */   {
/*  98 */     int numArgs = args.length;
/*  99 */     command = command.toUpperCase();
/* 100 */     for (int i = 0; i < numArgs; ++i)
/*     */     {
/* 102 */       String curArg = args[i].toUpperCase();
/*     */ 
/* 104 */       int index = curArg.indexOf("/" + command);
/* 105 */       if (index < 0)
/*     */       {
/* 107 */         index = curArg.indexOf("-" + command);
/*     */       }
/* 109 */       if (index >= 0)
/*     */       {
/* 111 */         return true;
/*     */       }
/*     */     }
/* 114 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 119 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79188 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     IdcAnalyze
 * JD-Core Version:    0.5.4
 */