/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.IdcServerManager;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.PrintStream;
/*     */ 
/*     */ public class IdcServerNT
/*     */ {
/*  36 */   protected static IdcServerManager m_serverManager = new IdcServerManager();
/*     */   protected static int m_port;
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*  41 */     IdcServerNT server = new IdcServerNT();
/*  42 */     boolean report = true;
/*  43 */     for (int i = 0; i < args.length; ++i)
/*     */     {
/*  45 */       if (!args[i].equals("-console"))
/*     */         continue;
/*  47 */       report = false;
/*  48 */       break;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  54 */       server.init(report);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */       try
/*     */       {
/*  60 */         Report.fatal(null, "!csFailedToInitServer", t);
/*  61 */         String msg = LocaleUtils.createMessageStringFromThrowable(t);
/*  62 */         if (report)
/*     */         {
/*  64 */           server.reportToLauncher(LocaleResources.localizeMessage(msg, null));
/*     */         }
/*     */         else
/*     */         {
/*  68 */           System.err.println(LocaleResources.localizeMessage(msg, null));
/*  69 */           t.printStackTrace(System.err);
/*     */         }
/*     */       }
/*     */       catch (Throwable t2)
/*     */       {
/*  74 */         t2.printStackTrace();
/*     */       }
/*  76 */       System.exit(-1);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(boolean report)
/*     */     throws DataException, ServiceException
/*     */   {
/*  85 */     EnvUtils.setRunAsService(true);
/*  86 */     m_serverManager.init();
/*  87 */     m_serverManager.serviceStart(0, 0);
/*     */ 
/*  89 */     if (report)
/*  90 */       reportToLauncher("0");
/*     */   }
/*     */ 
/*     */   protected void reportToLauncher(String report)
/*     */   {
/*  98 */     FileOutputStream fos = null;
/*  99 */     boolean success = false;
/* 100 */     int count = 0;
/* 101 */     int maxCount = 10;
/*     */ 
/* 103 */     if (report == null)
/*     */     {
/* 105 */       report = "0";
/*     */     }
/*     */ 
/* 108 */     while ((!success) && (count < maxCount))
/*     */     {
/*     */       try
/*     */       {
/* 112 */         ++count;
/* 113 */         String binDir = SystemUtils.getBinDir();
/* 114 */         fos = new FileOutputStream(binDir + "/IdcServerNTstatus.dat");
/* 115 */         fos.write(report.getBytes());
/* 116 */         success = true;
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 120 */         SystemUtils.sleepRandom(0L, 2000L);
/*     */       }
/*     */     }
/*     */ 
/* 124 */     if (!success)
/*     */     {
/* 126 */       System.err.println(LocaleResources.localizeMessage("!csUnableToReportToLauncher", null));
/*     */     }
/*     */ 
/* 129 */     FileUtils.closeFiles(fos, null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 134 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     IdcServerNT
 * JD-Core Version:    0.5.4
 */