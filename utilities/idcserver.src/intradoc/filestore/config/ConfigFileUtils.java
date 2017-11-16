/*     */ package intradoc.filestore.config;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ConfigFileUtils
/*     */ {
/*  46 */   private static char F_READABLE = 'r';
/*  47 */   private static char F_WRITEABLE = 'w';
/*  48 */   private static char F_CONTAINER = 'd';
/*     */ 
/*     */   public static int validateStorageData(Map storageData, String flags, String errorMsg)
/*     */     throws ServiceException
/*     */   {
/*  71 */     int error = 0;
/*  72 */     String msg = null;
/*  73 */     String pathname = (String)storageData.get("path");
/*  74 */     if (null == pathname)
/*     */     {
/*  76 */       throw new AssertionError("A storageData object is missing path information.");
/*     */     }
/*  78 */     boolean isContainer = -1 != flags.indexOf(F_CONTAINER);
/*     */ 
/*  80 */     String value = (String)storageData.get("fileExists");
/*  81 */     boolean bool = StringUtils.convertToBool(value, false);
/*  82 */     if (!bool)
/*     */     {
/*  84 */       error = -16;
/*  85 */       msg = (isContainer) ? "syFileUtilsDirNotFound" : "syFileUtilsFileNotFound";
/*     */     }
/*  87 */     if ((null == msg) && (-1 != flags.indexOf(F_READABLE)))
/*     */     {
/*  89 */       value = (String)storageData.get("canRead");
/*  90 */       bool = StringUtils.convertToBool(value, false);
/*  91 */       if (!bool)
/*     */       {
/*  93 */         error = -18;
/*  94 */         msg = (isContainer) ? "syFileUtilsDirNoAccess" : "syFileUtilsFileNoAccess";
/*     */       }
/*     */     }
/*  97 */     if ((null == msg) && (-1 != flags.indexOf(F_WRITEABLE)))
/*     */     {
/*  99 */       value = (String)storageData.get("canWrite");
/* 100 */       bool = StringUtils.convertToBool(value, false);
/* 101 */       if (!bool)
/*     */       {
/* 103 */         error = -19;
/* 104 */         msg = (isContainer) ? "syFileUtilsDirReadOnly" : "syFileUtilsFileReadOnly";
/*     */       }
/*     */     }
/* 107 */     if (null == msg)
/*     */     {
/* 109 */       value = (String)storageData.get("isContainer");
/* 110 */       bool = StringUtils.convertToBool(value, !isContainer);
/* 111 */       if (bool != isContainer)
/*     */       {
/* 113 */         error = -24;
/* 114 */         msg = (isContainer) ? "syFileUtilsDirInvalidPath" : "syFileUtilsFileInvalidPath";
/*     */       }
/*     */     }
/* 117 */     if (null == msg)
/*     */     {
/* 119 */       return error;
/*     */     }
/* 121 */     msg = LocaleUtils.encodeMessage(msg, null, pathname);
/* 122 */     if (null != errorMsg)
/*     */     {
/* 124 */       msg = LocaleUtils.appendMessage(msg, errorMsg);
/*     */     }
/* 126 */     throw new ServiceException(error, msg);
/*     */   }
/*     */ 
/*     */   public static int validateDescriptor(ConfigFileStore cfs, IdcFileDescriptor desc, String flags, String errorMsg)
/*     */     throws ServiceException
/*     */   {
/*     */     Map storageData;
/*     */     try
/*     */     {
/* 153 */       storageData = cfs.getStorageData(desc, null, null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 157 */       throw new ServiceException(errorMsg, e);
/*     */     }
/* 159 */     return validateStorageData(storageData, flags, errorMsg);
/*     */   }
/*     */ 
/*     */   public static void loadPropertiesFromFile(ConfigFileStore cfs, IdcFileDescriptor desc, Properties props)
/*     */     throws ServiceException, IOException
/*     */   {
/* 166 */     InputStream in = null;
/*     */     try
/*     */     {
/* 169 */       Map args = new HashMap();
/* 170 */       args.put(ConfigFileStore.O_REQUIRE_EXISTENCE, "1");
/* 171 */       in = cfs.getInputStream(desc, args);
/* 172 */       if (null != in)
/*     */       {
/* 174 */         FileUtils.loadPropertiesEx(props, in, null);
/*     */       }
/*     */     }
/*     */     catch (FileNotFoundException e)
/*     */     {
/* 179 */       if (SystemUtils.m_verbose)
/*     */       {
/* 181 */         Report.debug("filestore", null, e);
/*     */       }
/*     */ 
/* 198 */       return;
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 192 */       throw new ServiceException(e);
/*     */     }
/*     */     finally
/*     */     {
/* 196 */       if (null != in)
/*     */       {
/* 198 */         in.close();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 206 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71699 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.config.ConfigFileUtils
 * JD-Core Version:    0.5.4
 */