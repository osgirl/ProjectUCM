/*     */ import intradoc.admin.IdcAdminManager;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.PrintStream;
/*     */ 
/*     */ public class IdcAdminNT
/*     */ {
/*  35 */   protected static IdcAdminManager m_adminManager = new IdcAdminManager();
/*     */   protected static int m_port;
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*  40 */     IdcAdminNT admin = new IdcAdminNT();
/*  41 */     boolean report = true;
/*  42 */     for (int i = 0; i < args.length; ++i)
/*     */     {
/*  44 */       if (!args[i].equals("-console"))
/*     */         continue;
/*  46 */       report = false;
/*  47 */       break;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  53 */       admin.init(report);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */       try
/*     */       {
/*  59 */         Report.fatal(null, "csFailedToInitAdminServer", t);
/*  60 */         String msg = LocaleUtils.createMessageStringFromThrowable(t);
/*  61 */         if (report)
/*     */         {
/*  63 */           admin.reportToLauncher(LocaleResources.localizeMessage(msg, null));
/*     */         }
/*     */         else
/*     */         {
/*  67 */           System.err.println(LocaleResources.localizeMessage(msg, null));
/*     */         }
/*     */       }
/*     */       catch (Throwable t2)
/*     */       {
/*  72 */         t2.printStackTrace();
/*     */       }
/*     */ 
/*  75 */       System.exit(-1);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(boolean report)
/*     */     throws DataException, ServiceException
/*     */   {
/*  84 */     m_adminManager.init();
/*  85 */     m_adminManager.serviceStart(0, 0);
/*     */ 
/*  87 */     if (report)
/*  88 */       reportToLauncher("0");
/*     */   }
/*     */ 
/*     */   protected void reportToLauncher(String report)
/*     */   {
/*  96 */     FileOutputStream fos = null;
/*  97 */     boolean success = false;
/*  98 */     int count = 0;
/*  99 */     int maxCount = 10;
/*     */ 
/* 101 */     if (report == null)
/*     */     {
/* 103 */       report = "0";
/*     */     }
/*     */ 
/* 106 */     while ((!success) && (count < maxCount))
/*     */     {
/*     */       try
/*     */       {
/* 110 */         ++count;
/* 111 */         String binDir = SystemUtils.getBinDir();
/* 112 */         fos = new FileOutputStream(binDir + "/IdcAdminNTstatus.dat");
/* 113 */         fos.write(report.getBytes());
/* 114 */         success = true;
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 118 */         SystemUtils.sleepRandom(0L, 2000L);
/*     */       }
/*     */       finally
/*     */       {
/* 122 */         FileUtils.closeFiles(fos, null);
/*     */       }
/*     */     }
/*     */ 
/* 126 */     if (success)
/*     */       return;
/* 128 */     System.err.println(LocaleResources.localizeMessage("!csUnableToReportToLauncher", null));
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 135 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     IdcAdminNT
 * JD-Core Version:    0.5.4
 */