/*     */ package intradoc.apputilities.systemproperties.ibr;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ 
/*     */ public class Win32PrinterHelper
/*     */ {
/*     */   protected NativeOsUtils m_prnUtils;
/*     */   protected IdcMessage m_errMsg;
/*     */ 
/*     */   public Win32PrinterHelper()
/*     */   {
/*  37 */     this.m_errMsg = null;
/*     */     try
/*     */     {
/*  40 */       this.m_prnUtils = new NativeOsUtils();
/*     */     }
/*     */     catch (UnsatisfiedLinkError missingNative)
/*     */     {
/*  44 */       if (SystemUtils.m_verbose)
/*     */       {
/*  46 */         Report.trace(null, null, missingNative);
/*     */       }
/*  48 */       this.m_prnUtils = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public IdcMessage getErrorMsg()
/*     */   {
/*  54 */     return this.m_errMsg;
/*     */   }
/*     */ 
/*     */   public boolean isPrinterUtilsReady()
/*     */   {
/*  59 */     if (this.m_prnUtils != null)
/*     */     {
/*  61 */       return this.m_prnUtils.isPrinterInstallSupported();
/*     */     }
/*  63 */     return false;
/*     */   }
/*     */ 
/*     */   public String win8SignedMicrosoftPSDriverPath()
/*     */   {
/*  68 */     String sysRoot = this.m_prnUtils.getEnv("SystemRoot");
/*  69 */     return FileUtils.directorySlashes(new StringBuilder().append(sysRoot).append("/System32/DriverStore/FileRepository/prnms005.inf_amd64_ab654d5de9d04cb6/").toString()) + "prnms005.inf";
/*     */   }
/*     */ 
/*     */   public boolean isWin8SignedMicrosoftPSDriverAvailable()
/*     */   {
/*  74 */     String driverInfFile = win8SignedMicrosoftPSDriverPath();
/*  75 */     return FileUtils.checkPathExists(driverInfFile);
/*     */   }
/*     */ 
/*     */   public boolean installPrinterWithInf(String printerName, String portPath, String driverName, String infFile)
/*     */     throws ServiceException
/*     */   {
/*  91 */     this.m_errMsg = null;
/*  92 */     if (this.m_prnUtils != null)
/*     */     {
/*  94 */       if (isPrinterInstalled(printerName))
/*     */       {
/*  96 */         this.m_errMsg = IdcMessageFactory.lc("csPrinterAlreadyInstalled2", new Object[] { printerName });
/*  97 */         return true;
/*     */       }
/*  99 */       long rc = installFILEPort(portPath);
/* 100 */       if (rc != 0L)
/*     */       {
/* 102 */         this.m_errMsg = IdcMessageFactory.lc("csPrintPortNotCreated", new Object[] { portPath, Long.valueOf(rc) });
/* 103 */         return false;
/*     */       }
/* 105 */       runNativePrinterUtility(printerName, portPath, driverName, infFile);
/* 106 */       return true;
/*     */     }
/* 108 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean isPrinterInstalled(String printerName)
/*     */   {
/* 113 */     if (this.m_prnUtils != null)
/*     */     {
/* 115 */       return this.m_prnUtils.isPrinterInstalled(printerName);
/*     */     }
/* 117 */     return false;
/*     */   }
/*     */ 
/*     */   protected long installFILEPort(String portPath)
/*     */     throws ServiceException
/*     */   {
/* 129 */     if (this.m_prnUtils != null)
/*     */     {
/* 132 */       portPath = FileUtils.fileSlashes(portPath);
/* 133 */       String portDir = FileUtils.getDirectory(portPath);
/* 134 */       FileUtils.checkOrCreateDirectory(portDir, 3);
/*     */ 
/* 136 */       String winPortPath = FileUtils.windowsSlashes(portPath);
/*     */ 
/* 138 */       long rc = this.m_prnUtils.addPrinterFilePort(winPortPath);
/* 139 */       if (rc == 0L)
/*     */       {
/* 141 */         startStopSpooler(false);
/* 142 */         startStopSpooler(true);
/*     */       }
/* 144 */       return rc;
/*     */     }
/* 146 */     return -999L;
/*     */   }
/*     */ 
/*     */   protected void runNativePrinterUtility(String printerName, String printerPort, String driverModelName, String infFile)
/*     */     throws ServiceException
/*     */   {
/* 162 */     String[] cmd = { "rundll32", "printui.dll,PrintUIEntry", "/if", "/b", printerName, "/r", FileUtils.windowsSlashes(printerPort), "/m", driverModelName, "/f", FileUtils.windowsSlashes(infFile) };
/*     */ 
/* 171 */     runCommand(cmd);
/*     */   }
/*     */ 
/*     */   protected void startStopSpooler(boolean start)
/*     */   {
/*     */     String[] cmd;
/*     */     String[] cmd;
/* 177 */     if (start)
/*     */     {
/* 179 */       cmd = new String[] { "net.exe", "start", "spooler" };
/*     */     }
/*     */     else
/*     */     {
/* 186 */       cmd = new String[] { "net.exe", "stop", "spooler" };
/*     */     }
/*     */ 
/* 191 */     runCommand(cmd);
/* 192 */     SystemUtils.sleep(1000L);
/*     */   }
/*     */ 
/*     */   protected int runCommand(String[] cmd)
/*     */   {
/* 197 */     Report.trace(null, "Executing ==> ", null);
/* 198 */     for (int i = 0; i < cmd.length; ++i)
/*     */     {
/* 200 */       Report.trace(null, cmd[i] + " ", null);
/*     */     }
/* 202 */     int exit = 0;
/*     */     try
/*     */     {
/* 205 */       Runtime rt = Runtime.getRuntime();
/* 206 */       Process p = rt.exec(cmd);
/* 207 */       exit = p.waitFor();
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 211 */       Report.trace(null, null, ignore);
/*     */     }
/* 213 */     return exit;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 218 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103830 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.ibr.Win32PrinterHelper
 * JD-Core Version:    0.5.4
 */