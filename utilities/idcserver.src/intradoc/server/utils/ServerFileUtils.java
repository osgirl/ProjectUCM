/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ServerFileUtils
/*     */ {
/*     */   public static void copyFileWithRetries(String from, String to)
/*     */     throws ServiceException
/*     */   {
/*  39 */     copyFileWithRetriesEx(from, to, 0);
/*     */   }
/*     */ 
/*     */   public static void copyFileWithRetriesEx(String from, String to, int flags) throws ServiceException
/*     */   {
/*  44 */     File fromFile = new File(from);
/*  45 */     if (!fromFile.exists())
/*     */     {
/*  47 */       throw new ServiceException(LocaleUtils.encodeMessage("syFileUtilsCopyNoSource", null, from, to));
/*     */     }
/*     */ 
/*  51 */     String extension = FileUtils.getExtension(from);
/*     */ 
/*  53 */     int numTries = 0;
/*  54 */     int maxTries = SharedObjects.getEnvironmentInt("MaxCopyAttempts:" + extension, -1);
/*  55 */     if (maxTries < 0)
/*     */     {
/*  57 */       maxTries = SharedObjects.getEnvironmentInt("MaxCopyAttempts", 3);
/*     */     }
/*     */ 
/*  60 */     long maxTimeout = SharedObjects.getTypedEnvironmentInt("MaxCopyRetryTimeoutInSeconds:" + extension, -1, 18, 24);
/*     */ 
/*  63 */     if (maxTimeout < 0L)
/*     */     {
/*  65 */       maxTimeout = SharedObjects.getTypedEnvironmentInt("MaxCopyRetryTimeoutInSeconds", 30000, 18, 24);
/*     */     }
/*     */ 
/*  70 */     long timeout = SharedObjects.getTypedEnvironmentInt("CopyRetryFirstTimeoutMillisPerMeg:" + extension, -1, 18, 18);
/*     */ 
/*  73 */     if (timeout < 0L)
/*     */     {
/*  75 */       timeout = SharedObjects.getTypedEnvironmentInt("CopyRetryFirstTimeoutMillisPerMeg", 10, 18, 18);
/*     */     }
/*     */ 
/*  80 */     long additionalMillisPerMeg = SharedObjects.getTypedEnvironmentInt("CopyRetryAdditionalMillisPerMeg:" + extension, -1, 18, 18);
/*     */ 
/*  83 */     if (additionalMillisPerMeg < 0L)
/*     */     {
/*  85 */       additionalMillisPerMeg = SharedObjects.getTypedEnvironmentInt("CopyRetryAdditionalMillisPerMegPerAttempt", 250, 18, 18);
/*     */     }
/*     */ 
/*  90 */     long megs = fromFile.length() / 1048576L + 1L;
/*  91 */     timeout *= megs;
/*     */ 
/*  93 */     boolean copySucceeded = false;
/*  94 */     while ((!copySucceeded) && (numTries < maxTries))
/*     */     {
/*  96 */       copySucceeded = true;
/*     */ 
/*  98 */       if (numTries > 0)
/*     */       {
/* 103 */         if (numTries > 1)
/*     */         {
/* 105 */           timeout += megs * additionalMillisPerMeg;
/*     */         }
/* 107 */         if (timeout > maxTimeout)
/*     */         {
/* 109 */           timeout = maxTimeout;
/*     */         }
/*     */ 
/* 112 */         SystemUtils.sleep(timeout);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 117 */         FileUtils.copyFileEx(from, to, flags);
/* 118 */         if (numTries > 0)
/*     */         {
/* 120 */           Report.warning(null, null, "csFileCopyFailedRetrySucceeded", new Object[] { from, to });
/*     */         }
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 125 */         copySucceeded = false;
/* 126 */         ++numTries;
/* 127 */         if (numTries == maxTries)
/*     */         {
/* 129 */           throw e;
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getTemporaryFilePath(DataBinder binder, String fileKey)
/*     */     throws DataException
/*     */   {
/* 139 */     String filePath = binder.getAllowMissing(fileKey + ":path");
/*     */ 
/* 148 */     if (binder.m_isExternalRequest)
/*     */     {
/* 150 */       if (filePath == null)
/*     */       {
/* 152 */         return null;
/*     */       }
/* 154 */       Vector tempFiles = binder.getTempFiles();
/* 155 */       if (tempFiles == null)
/*     */       {
/* 157 */         return null;
/*     */       }
/* 159 */       boolean uploaded = false;
/* 160 */       for (int i = 0; i < tempFiles.size(); ++i)
/*     */       {
/* 162 */         String path = (String)tempFiles.elementAt(i);
/* 163 */         if (!path.equalsIgnoreCase(filePath))
/*     */           continue;
/* 165 */         uploaded = true;
/* 166 */         break;
/*     */       }
/*     */ 
/* 169 */       if (!uploaded)
/*     */       {
/* 171 */         return null;
/*     */       }
/* 173 */       return filePath;
/*     */     }
/*     */ 
/* 178 */     if ((filePath == null) || (filePath.trim().length() == 0))
/*     */     {
/* 180 */       filePath = binder.getAllowMissing(fileKey);
/*     */     }
/*     */ 
/* 183 */     return filePath;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 188 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81603 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ServerFileUtils
 * JD-Core Version:    0.5.4
 */