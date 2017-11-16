/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.StandAloneApp;
/*     */ import intradoc.apputilities.batchloader.BatchLoaderApp;
/*     */ import intradoc.apputilities.batchloader.SpiderApp;
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.LoggingUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.GuiText;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.PrintStream;
/*     */ 
/*     */ public class BatchLoader
/*     */ {
/*  33 */   public static String[][] m_appsInfo = { { "Spider", "intradoc.apputilities.batchloader.SpiderFrame", "csSpiderFrameTitle", "", "", "0" }, { "BatchLoader", "intradoc.apputilities.batchloader.BatchLoaderFrame", "csBatchLoaderTitle", "", "", "0" } };
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*  55 */     boolean doBackground = false;
/*  56 */     boolean doSpider = false;
/*  57 */     boolean localizationLoaded = false;
/*     */     try
/*     */     {
/*  62 */       AppObjectRepository.putObject("CommandLine", args);
/*     */ 
/*  65 */       doBackground = checkCommandLine(args, "Q");
/*  66 */       doSpider = checkCommandLine(args, "spider");
/*  67 */       if (doBackground)
/*     */       {
/*  69 */         doBackgroundProcess(doSpider);
/*     */       }
/*     */       else
/*     */       {
/*  73 */         String appName = null;
/*  74 */         if (doSpider)
/*     */         {
/*  76 */           appName = "Spider";
/*     */         }
/*     */         else
/*     */         {
/*  80 */           appName = "BatchLoader";
/*     */         }
/*  82 */         AppLauncher.init(appName, true, m_appsInfo);
/*  83 */         AppLauncher.launch(appName);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  88 */       IdcMessage msg = IdcMessageFactory.lc();
/*  89 */       msg.m_msgLocalized = "Unable to start the batch loader.";
/*     */ 
/*  91 */       if (localizationLoaded)
/*     */       {
/*  93 */         msg = IdcMessageFactory.lc("csUnableToStartProduct", new Object[] { "csBatchLoaderTitle" });
/*     */       }
/*     */ 
/*  96 */       if (doBackground)
/*     */       {
/*  98 */         System.err.println(LocaleResources.localizeMessage(null, msg, null).toString());
/*  99 */         Report.error(null, e, msg);
/* 100 */         System.exit(1);
/*     */       }
/*     */       else
/*     */       {
/* 104 */         Report.error(null, e, msg);
/* 105 */         msg.m_prior = LocaleUtils.createMessageListFromThrowable(e);
/* 106 */         AppLauncher.reportFatal(new AppFrameHelper(), msg);
/*     */       }
/* 108 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   static void doBackgroundProcess(boolean doSpider) throws Exception
/*     */   {
/* 114 */     StandAloneApp standAlone = new StandAloneApp();
/*     */ 
/* 117 */     IdcSystemConfig.loadInitialConfig();
/* 118 */     IdcSystemConfig.loadAppConfigInfo();
/* 119 */     IdcSystemConfig.initLocalization(IdcSystemConfig.F_STANDARD_SERVER);
/* 120 */     IdcSystemConfig.configLocalization();
/* 121 */     GuiText.localize(null);
/*     */ 
/* 124 */     LoggingUtils.setLogFileMsgPrefix(LocaleResources.getString("csBatchLoaderLogPrefix", null));
/*     */ 
/* 128 */     IdcSystemLoader.finishInit(false);
/*     */ 
/* 130 */     standAlone.setSplashFrame(null);
/* 131 */     standAlone.finishLoad(false);
/*     */ 
/* 133 */     if (doSpider)
/*     */     {
/* 135 */       SpiderApp spiderApp = new SpiderApp(standAlone, true);
/* 136 */       spiderApp.createBatchFile();
/*     */     }
/*     */     else
/*     */     {
/* 140 */       BatchLoaderApp batchApp = new BatchLoaderApp(standAlone, true);
/* 141 */       batchApp.loadBatchLoader();
/*     */     }
/*     */   }
/*     */ 
/*     */   static boolean checkCommandLine(String[] args, String command)
/*     */   {
/* 147 */     int numArgs = args.length;
/* 148 */     command = command.toUpperCase();
/* 149 */     for (int i = 0; i < numArgs; ++i)
/*     */     {
/* 151 */       String curArg = args[i].toUpperCase();
/*     */ 
/* 153 */       int index = curArg.indexOf("/" + command);
/* 154 */       if (index < 0)
/*     */       {
/* 156 */         index = curArg.indexOf("-" + command);
/*     */       }
/* 158 */       if (index >= 0)
/*     */       {
/* 160 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 164 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 169 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79188 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     BatchLoader
 * JD-Core Version:    0.5.4
 */