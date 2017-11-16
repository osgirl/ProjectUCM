/*     */ package intradoc.refinery.configure;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.configpage.ConfigPageUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.alert.AlertUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class OitFontUtilsHelper
/*     */ {
/*  38 */   protected static String F_ALERT_ID = "OitFontPathNotSet";
/*     */ 
/*     */   public static void initOitFontPathAlertIfMissing(String url)
/*     */   {
/*  42 */     String fontdirectory = SharedObjects.getEnvironmentValue("fontdirectory");
/*  43 */     if ((fontdirectory == null) || (fontdirectory.length() == 0))
/*     */     {
/*  45 */       fontdirectory = buildSystemDefaultFontPath();
/*  46 */       SharedObjects.putEnvironmentValue("fontdirectory", fontdirectory);
/*     */ 
/*  48 */       SharedObjects.putEnvironmentValue("fontdirectoryDefault", fontdirectory);
/*     */     }
/*     */ 
/*  51 */     IdcMessage alertMsg = null;
/*     */     try
/*     */     {
/*  54 */       validateFontPath(fontdirectory, true);
/*     */     }
/*     */     catch (ServiceException fontSetupExp)
/*     */     {
/*  58 */       Report.trace("system", "error setting up fonts.", fontSetupExp);
/*  59 */       alertMsg = fontSetupExp.getIdcMessage();
/*     */     }
/*     */ 
/*  62 */     if (alertMsg == null)
/*     */       return;
/*     */     try
/*     */     {
/*  66 */       AlertUtils.setAlertSimple(F_ALERT_ID, LocaleUtils.encodeMessage(alertMsg), url, 2);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/*  70 */       Report.trace(null, null, exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String buildSystemDefaultFontPath()
/*     */   {
/*  77 */     String path = FileUtils.directorySlashes(System.getProperty("java.home"));
/*  78 */     path = path + "lib/fonts/";
/*  79 */     return path; } 
/*     */   protected static void validateFontPath(String fontPath, boolean doAlert) throws ServiceException { // Byte code:
/*     */     //   0: aconst_null
/*     */     //   1: astore_2
/*     */     //   2: aload_0
/*     */     //   3: ifnull +10 -> 13
/*     */     //   6: aload_0
/*     */     //   7: invokevirtual 4	java/lang/String:length	()I
/*     */     //   10: ifne +39 -> 49
/*     */     //   13: iload_1
/*     */     //   14: ifeq +15 -> 29
/*     */     //   17: ldc 26
/*     */     //   19: iconst_0
/*     */     //   20: anewarray 27	java/lang/Object
/*     */     //   23: invokestatic 28	intradoc/common/IdcMessageFactory:lc	(Ljava/lang/String;[Ljava/lang/Object;)Lintradoc/util/IdcMessage;
/*     */     //   26: goto +12 -> 38
/*     */     //   29: ldc 29
/*     */     //   31: iconst_0
/*     */     //   32: anewarray 27	java/lang/Object
/*     */     //   35: invokestatic 28	intradoc/common/IdcMessageFactory:lc	(Ljava/lang/String;[Ljava/lang/Object;)Lintradoc/util/IdcMessage;
/*     */     //   38: astore_2
/*     */     //   39: new 9	intradoc/common/ServiceException
/*     */     //   42: dup
/*     */     //   43: aconst_null
/*     */     //   44: aload_2
/*     */     //   45: invokespecial 30	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;Lintradoc/util/IdcMessage;)V
/*     */     //   48: athrow
/*     */     //   49: iload_1
/*     */     //   50: ifeq +15 -> 65
/*     */     //   53: ldc 31
/*     */     //   55: iconst_0
/*     */     //   56: anewarray 27	java/lang/Object
/*     */     //   59: invokestatic 28	intradoc/common/IdcMessageFactory:lc	(Ljava/lang/String;[Ljava/lang/Object;)Lintradoc/util/IdcMessage;
/*     */     //   62: goto +12 -> 74
/*     */     //   65: ldc 32
/*     */     //   67: iconst_0
/*     */     //   68: anewarray 27	java/lang/Object
/*     */     //   71: invokestatic 28	intradoc/common/IdcMessageFactory:lc	(Ljava/lang/String;[Ljava/lang/Object;)Lintradoc/util/IdcMessage;
/*     */     //   74: astore_2
/*     */     //   75: aload_0
/*     */     //   76: aload_2
/*     */     //   77: iconst_0
/*     */     //   78: invokestatic 33	intradoc/common/FileUtils:validatePath	(Ljava/lang/String;Lintradoc/util/IdcMessage;I)V
/*     */     //   81: aload_0
/*     */     //   82: ldc 34
/*     */     //   84: invokestatic 35	intradoc/common/FileUtils:getMatchingFileNames	(Ljava/lang/String;Ljava/lang/String;)[Ljava/lang/String;
/*     */     //   87: astore_3
/*     */     //   88: aload_3
/*     */     //   89: arraylength
/*     */     //   90: ifne +47 -> 137
/*     */     //   93: iload_1
/*     */     //   94: ifeq +19 -> 113
/*     */     //   97: ldc 36
/*     */     //   99: iconst_1
/*     */     //   100: anewarray 27	java/lang/Object
/*     */     //   103: dup
/*     */     //   104: iconst_0
/*     */     //   105: aload_0
/*     */     //   106: aastore
/*     */     //   107: invokestatic 28	intradoc/common/IdcMessageFactory:lc	(Ljava/lang/String;[Ljava/lang/Object;)Lintradoc/util/IdcMessage;
/*     */     //   110: goto +16 -> 126
/*     */     //   113: ldc 37
/*     */     //   115: iconst_1
/*     */     //   116: anewarray 27	java/lang/Object
/*     */     //   119: dup
/*     */     //   120: iconst_0
/*     */     //   121: aload_0
/*     */     //   122: aastore
/*     */     //   123: invokestatic 28	intradoc/common/IdcMessageFactory:lc	(Ljava/lang/String;[Ljava/lang/Object;)Lintradoc/util/IdcMessage;
/*     */     //   126: astore_2
/*     */     //   127: new 9	intradoc/common/ServiceException
/*     */     //   130: dup
/*     */     //   131: aconst_null
/*     */     //   132: aload_2
/*     */     //   133: invokespecial 30	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;Lintradoc/util/IdcMessage;)V
/*     */     //   136: athrow
/*     */     //   137: return } 
/* 105 */   public static void loadConfigureFontAndRenderingOption(Properties configData) { String manualgdFontPath = ConfigPageUtils.getBaseSetting("GDFONTPATH");
/* 106 */     if ((manualgdFontPath != null) && (manualgdFontPath.length() > 0))
/*     */     {
/* 108 */       configData.put("fontdirectory", manualgdFontPath);
/* 109 */       configData.put("fontdirectory:disable", "1");
/*     */     }
/*     */     else
/*     */     {
/* 113 */       String fontdirectory = configData.getProperty("fontdirectory");
/* 114 */       if ((fontdirectory == null) || (fontdirectory.length() == 0))
/*     */       {
/* 116 */         String systemFontPath = SharedObjects.getEnvironmentValue("fontdirectoryDefault");
/*     */ 
/* 122 */         if ((systemFontPath != null) && (systemFontPath.length() > 0))
/*     */         {
/* 124 */           SharedObjects.putEnvironmentValue("fontdirectory", systemFontPath);
/* 125 */           configData.put("fontdirectory", systemFontPath);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 130 */     String osName = EnvUtils.getOSName();
/* 131 */     boolean allowRenderingChoice = (osName.equals("linux")) || (osName.equals("linux64")) || (osName.equals("solaris")) || (osName.equals("solaris64"));
/*     */ 
/* 134 */     if (allowRenderingChoice)
/*     */       return;
/* 136 */     configData.put("preferoitrendering", "false");
/* 137 */     configData.put("preferoitrendering:disable", "1"); }
/*     */ 
/*     */ 
/*     */   public static void saveConfigureFontAndRenderingOption(Properties configData, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 144 */     boolean skipFontPathConfiguration = DataBinderUtils.getBoolean(binder, "SkipFontConfiguration", false);
/* 145 */     if (skipFontPathConfiguration)
/*     */       return;
/* 147 */     String fontPath = configData.getProperty("fontdirectory");
/* 148 */     validateFontPath(fontPath, false);
/* 149 */     boolean alertExists = AlertUtils.existsAlert(F_ALERT_ID, 2);
/* 150 */     if (!alertExists)
/*     */       return;
/* 152 */     binder.putLocal("alertId", F_ALERT_ID);
/* 153 */     AlertUtils.deleteAlert(binder);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 160 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.configure.OitFontUtilsHelper
 * JD-Core Version:    0.5.4
 */