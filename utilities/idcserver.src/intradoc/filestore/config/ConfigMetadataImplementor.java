/*     */ package intradoc.filestore.config;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.PosixStructStat;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.filestore.filesystem.BaseMetadataImplementor;
/*     */ import intradoc.provider.Provider;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ConfigMetadataImplementor extends BaseMetadataImplementor
/*     */ {
/*     */   protected ServerData m_server;
/*     */   protected NativeOsUtils m_nativeOsUtils;
/*     */ 
/*     */   public void init(FileStoreProvider fs, Provider provider)
/*     */   {
/*  62 */     super.init(fs, provider);
/*  63 */     if (EnvUtils.m_useNativeOSUtils)
/*     */     {
/*     */       try
/*     */       {
/*  67 */         this.m_nativeOsUtils = new NativeOsUtils();
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/*  71 */         Report.trace(null, "unable to instantiate NativeOsUtils", t);
/*     */       }
/*     */     }
/*     */ 
/*  75 */     this.m_server = ((ServerData)provider.getProviderObject("ServerData"));
/*     */   }
/*     */ 
/*     */   protected String validateDescriptorMetaData(IdcFileDescriptor descriptor, boolean onlyMinimal)
/*     */     throws DataException
/*     */   {
/*  94 */     if (null == descriptor)
/*     */     {
/*  96 */       String msg = LocaleUtils.encodeMessage("syMissingArgument", null, "descriptor");
/*     */ 
/*  98 */       throw new DataException(msg);
/*     */     }
/* 100 */     String serverPath = descriptor.getProperty("IntradocDir");
/* 101 */     String filename = descriptor.getProperty("filename");
/* 102 */     if ((null == serverPath) || (null == filename))
/*     */     {
/* 104 */       String msg = LocaleUtils.encodeMessage("syMissingArgument2", null, (null == serverPath) ? "IntradocDir" : "filename", "descriptor");
/*     */ 
/* 108 */       throw new DataException(msg);
/*     */     }
/* 110 */     if (onlyMinimal)
/*     */     {
/* 112 */       return null;
/*     */     }
/* 114 */     String pathname = descriptor.getProperty("pathname");
/* 115 */     if (null == pathname)
/*     */     {
/* 117 */       String msg = LocaleUtils.encodeMessage("syMissingArgument2", null, "pathname", "descriptor");
/*     */ 
/* 119 */       throw new DataException(msg);
/*     */     }
/* 121 */     return pathname;
/*     */   }
/*     */ 
/*     */   public Map getKeyMetaData(IdcFileDescriptor descriptor)
/*     */     throws DataException
/*     */   {
/* 130 */     validateDescriptorMetaData(descriptor, true);
/* 131 */     String serverPath = descriptor.getProperty("IntradocDir");
/* 132 */     String filename = descriptor.getProperty("filename");
/* 133 */     HashMap metadata = new HashMap(2);
/* 134 */     metadata.put("IntradocDir", serverPath);
/* 135 */     metadata.put("filename", filename);
/* 136 */     return metadata;
/*     */   }
/*     */ 
/*     */   public Map getStorageData(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 153 */     HashMap data = new HashMap();
/* 154 */     String pathname = validateDescriptorMetaData(descriptor, false);
/* 155 */     data.put("path", pathname);
/* 156 */     boolean fileExists = false; boolean fileIsContainer = false;
/* 157 */     boolean fileIsReadable = false; boolean fileIsWriteable = false;
/* 158 */     long fileLength = 0L; long fileLastModified = 0L;
/* 159 */     String useLegacyStatStr = descriptor.getProperty("useLegacyStat");
/* 160 */     boolean useLegacyStat = StringUtils.convertToBool(useLegacyStatStr, false);
/* 161 */     if ((useLegacyStat) || (this.m_nativeOsUtils == null) || (!this.m_nativeOsUtils.isStatSupported()))
/*     */     {
/* 165 */       File f = new File(pathname);
/* 166 */       fileExists = f.exists();
/* 167 */       if (fileExists)
/*     */       {
/* 169 */         fileLength = f.length();
/* 170 */         fileIsContainer = f.isDirectory();
/* 171 */         fileLastModified = f.lastModified();
/* 172 */         fileIsReadable = f.canRead();
/* 173 */         fileIsWriteable = f.canWrite();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 178 */       PosixStructStat stat = new PosixStructStat();
/* 179 */       int error = this.m_nativeOsUtils.lstat(pathname, stat);
/* 180 */       if ((0 != error) && (SystemUtils.m_verbose))
/*     */       {
/* 182 */         long errorCode = this.m_nativeOsUtils.getErrorCode();
/* 183 */         String errorMsg = this.m_nativeOsUtils.getErrorMessage(errorCode);
/* 184 */         Report.debug("filestore", "Unable to lstat '" + pathname + "': " + errorMsg, null);
/*     */       }
/*     */ 
/* 187 */       int type = stat.st_mode & NativeOsUtils.S_IFMT;
/* 188 */       fileExists = (NativeOsUtils.S_IFREG == type) || (NativeOsUtils.S_IFDIR == type);
/* 189 */       if (fileExists)
/*     */       {
/* 191 */         fileIsContainer = NativeOsUtils.S_IFDIR == type;
/*     */ 
/* 193 */         fileLastModified = stat.st_size;
/*     */       }
/*     */     }
/* 196 */     data.put("fileExists", (fileExists) ? "1" : "0");
/* 197 */     if (fileExists)
/*     */     {
/* 199 */       data.put("fileSize", "" + fileLength);
/* 200 */       data.put("isContainer", (fileIsContainer) ? "1" : "0");
/* 201 */       data.put("lastModified", "" + fileLastModified);
/* 202 */       data.put("canRead", (fileIsReadable) ? "1" : "0");
/* 203 */       data.put("canWrite", (fileIsWriteable) ? "1" : "0");
/*     */     }
/* 205 */     return data;
/*     */   }
/*     */ 
/*     */   public void updateCacheData(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 217 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 77451 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.config.ConfigMetadataImplementor
 * JD-Core Version:    0.5.4
 */