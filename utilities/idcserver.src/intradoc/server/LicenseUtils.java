/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class LicenseUtils
/*     */ {
/*  33 */   public static final String[][] m_licenseKeys = { { "LicenseSerialNumber", "csProductSerialNumberLabel" }, { "LicenseFeatureCode", "csProductFeatureDescriptionCodeLabel" }, { "LicenseSignature", "csProductLicenseSignatureLabel" } };
/*     */ 
/*     */   public static void loadLicenseInfo()
/*     */   {
/*  40 */     String path = getLicenseFilePath();
/*     */     try
/*     */     {
/*  43 */       Properties props = new Properties();
/*  44 */       if (FileUtils.checkFile(path, true, false) == 0)
/*     */       {
/*  46 */         FileUtils.loadProperties(props, path);
/*     */       }
/*     */       else
/*     */       {
/*  50 */         Report.trace("startup", "not loading license configuration data because " + path + " does not exist.", null);
/*     */       }
/*     */ 
/*  53 */       for (int i = 0; i < m_licenseKeys.length; ++i)
/*     */       {
/*  55 */         String val = props.getProperty(m_licenseKeys[i][0]);
/*  56 */         if (val == null)
/*     */           continue;
/*  58 */         SharedObjects.putEnvironmentValue(m_licenseKeys[i][0], val);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  64 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadLicense", null, path);
/*     */ 
/*  66 */       Report.error(null, msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void saveLicenseInfo() throws ServiceException
/*     */   {
/*  72 */     String path = getLicenseFilePath();
/*     */     try
/*     */     {
/*  75 */       BufferedWriter w = FileUtils.openDataWriter(new File(path));
/*     */ 
/*  77 */       StringBuffer output = new StringBuffer("#License information\n");
/*  78 */       for (int i = 0; i < m_licenseKeys.length; ++i)
/*     */       {
/*  80 */         String val = SharedObjects.getEnvironmentValue(m_licenseKeys[i][0]);
/*  81 */         if (val == null)
/*     */         {
/*  83 */           String msg = LocaleUtils.encodeMessage("csUnableToSaveLicense", null, m_licenseKeys[i][1]);
/*     */ 
/*  85 */           throw new ServiceException(msg);
/*     */         }
/*  87 */         output.append(m_licenseKeys[i][0]);
/*  88 */         output.append("=");
/*  89 */         output.append(StringUtils.encodeLiteralStringEscapeSequence(val));
/*  90 */         output.append("\n");
/*     */       }
/*  92 */       String str = output.toString();
/*  93 */       w.write("#License information\n" + str);
/*  94 */       w.close();
/*  95 */       String msg = LocaleUtils.encodeMessage("csUpdatedLicenseInfo", str);
/*  96 */       Report.info(null, msg, null);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 100 */       String msg = LocaleUtils.encodeMessage("csUnableToSaveLicense2", null, path);
/*     */ 
/* 102 */       throw new ServiceException(msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getLicenseFilePath()
/*     */   {
/* 108 */     return LegacyDirectoryLocator.getConfigDirectory() + "license.cfg";
/*     */   }
/*     */ 
/*     */   public static long parseLicenseTime(String str)
/*     */   {
/* 113 */     String year = str.substring(0, 2);
/* 114 */     String month = str.substring(2, 4);
/* 115 */     String day = str.substring(4, 6);
/* 116 */     int y = Integer.parseInt(year);
/* 117 */     int m = Integer.parseInt(month);
/* 118 */     int d = Integer.parseInt(day);
/* 119 */     return getLicenseTime(y, m, d);
/*     */   }
/*     */ 
/*     */   public static long getLicenseTime(int[] date)
/*     */   {
/* 124 */     return getLicenseTime(date[0], date[1], date[2]);
/*     */   }
/*     */ 
/*     */   public static long getLicenseTime(int year, int month, int day)
/*     */   {
/* 129 */     if (year < 70)
/*     */     {
/* 131 */       year += 2000;
/*     */     }
/* 133 */     else if (year < 100)
/*     */     {
/* 135 */       year += 1900;
/*     */     }
/* 137 */     Calendar cal = Calendar.getInstance();
/* 138 */     cal.set(year, month - 1, day, 0, 0, 0);
/* 139 */     Date d = cal.getTime();
/* 140 */     return d.getTime();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 146 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.LicenseUtils
 * JD-Core Version:    0.5.4
 */