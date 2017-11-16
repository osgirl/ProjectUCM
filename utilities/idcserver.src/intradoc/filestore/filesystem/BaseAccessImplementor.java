/*     */ package intradoc.filestore.filesystem;
/*     */ 
/*     */ import intradoc.common.BufferPool;
/*     */ import intradoc.common.DataStreamWrapper;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.filestore.BaseFileHelper;
/*     */ import intradoc.filestore.BaseFileStore;
/*     */ import intradoc.filestore.BasicIdcFileDescriptor;
/*     */ import intradoc.filestore.CommonStoreImplementor;
/*     */ import intradoc.filestore.FileStoreAccessImplementor;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.provider.Provider;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Map;
/*     */ import java.util.Random;
/*     */ 
/*     */ public class BaseAccessImplementor
/*     */   implements CommonStoreImplementor, FileStoreAccessImplementor
/*     */ {
/*     */   public Provider m_provider;
/*     */   public BaseFileStore m_fileStore;
/*     */   public BaseFileHelper m_fileHelper;
/*     */   public BufferPool m_bufferPool;
/*     */   public Random m_random;
/*     */ 
/*     */   public BaseAccessImplementor()
/*     */   {
/*  37 */     this.m_random = new Random();
/*     */   }
/*     */ 
/*     */   public void preInit(FileStoreProvider fs, Provider provider) {
/*  41 */     this.m_fileStore = ((BaseFileStore)fs);
/*  42 */     this.m_provider = provider;
/*     */ 
/*  44 */     this.m_fileHelper = this.m_fileStore.m_fileHelper;
/*     */   }
/*     */ 
/*     */   public void init(FileStoreProvider fs, Provider provider)
/*     */   {
/*  49 */     this.m_bufferPool = BufferPool.getBufferPool("FileStore");
/*     */   }
/*     */ 
/*     */   public OutputStream getOutputStream(IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/*  56 */     return this.m_fileHelper.getOutputStream(descriptor, args);
/*     */   }
/*     */ 
/*     */   public void commitOutputStream(OutputStream stream, IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/*  62 */     boolean isDoTemp = this.m_fileStore.getConfigBoolean("isDoTemp", args, false, false);
/*  63 */     boolean isDoBackup = this.m_fileStore.getConfigBoolean("isDoBackup", args, false, false);
/*  64 */     String path = descriptor.getProperty("path");
/*  65 */     if (isDoBackup)
/*     */     {
/*  69 */       BasicIdcFileDescriptor target = (BasicIdcFileDescriptor)descriptor.createClone();
/*  70 */       target.put("path", path + ".old");
/*  71 */       File file = FileUtilsCfgBuilder.getCfgFile(path, null);
/*  72 */       if (file.exists())
/*     */       {
/*  74 */         moveFile(descriptor, target, args, null);
/*     */       }
/*     */     }
/*  77 */     if (!isDoTemp) {
/*     */       return;
/*     */     }
/*  80 */     BasicIdcFileDescriptor tempDesc = (BasicIdcFileDescriptor)descriptor.createClone();
/*  81 */     tempDesc.put("path", path + ".tmp");
/*  82 */     moveFile(tempDesc, descriptor, args, null);
/*     */   }
/*     */ 
/*     */   public InputStream getInputStream(IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/*  89 */     return this.m_fileHelper.getInputStream(descriptor, args);
/*     */   }
/*     */ 
/*     */   public void fillInputWrapper(DataStreamWrapper wrapper, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/*  95 */     if (wrapper.m_inStreamActive)
/*     */     {
/*  97 */       return;
/*     */     }
/*     */ 
/* 100 */     Map args = wrapper.m_streamArgs;
/* 101 */     boolean isFilePathOnly = false;
/* 102 */     if (args != null)
/*     */     {
/* 104 */       isFilePathOnly = StringUtils.convertToBool((String)args.get("isFilePathOnly"), false);
/*     */     }
/* 106 */     BasicIdcFileDescriptor desc = (BasicIdcFileDescriptor)wrapper.m_descriptor;
/* 107 */     wrapper.m_streamId = desc.getProperty("path");
/* 108 */     wrapper.m_filePath = wrapper.m_streamId;
/* 109 */     wrapper.m_isSimpleFileStream = false;
/*     */ 
/* 111 */     long len = NumberUtils.parseLong(desc.getCacheProperty("fileSize"), -1L);
/* 112 */     if (!isFilePathOnly)
/*     */     {
/* 114 */       InputStream iStream = getInputStream(desc, args);
/* 115 */       wrapper.initWithInputStream(iStream, len);
/*     */     }
/*     */     else
/*     */     {
/* 119 */       wrapper.m_streamLength = len;
/* 120 */       wrapper.m_inStreamActive = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void storeFromInputStream(IdcFileDescriptor descriptor, InputStream inputStream, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 130 */     this.m_fileHelper.storeFromInputStream(descriptor, inputStream, args, cxt);
/*     */   }
/*     */ 
/*     */   public void storeFromLocalFile(IdcFileDescriptor descriptor, File localFile, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 136 */     this.m_fileHelper.storeFromLocalFile(descriptor, localFile, args, cxt);
/*     */   }
/*     */ 
/*     */   public void storeFromStreamWrapper(IdcFileDescriptor descriptor, DataStreamWrapper streamWrapper, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 143 */     if (streamWrapper.m_inStreamActive)
/*     */     {
/* 145 */       storeFromInputStream(descriptor, streamWrapper.m_inStream, args, cxt);
/*     */     }
/*     */     else
/*     */     {
/* 149 */       storeFromLocalFile(descriptor, FileUtilsCfgBuilder.getCfgFile(streamWrapper.m_filePath, null), args, cxt);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void copyToOutputStream(IdcFileDescriptor descriptor, OutputStream outputStream, Map args)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 156 */     InputStream inputStream = null;
/*     */     try
/*     */     {
/* 159 */       inputStream = getInputStream(descriptor, args);
/* 160 */       byte[] buf = (byte[])(byte[])this.m_bufferPool.getBuffer(16384, 0);
/* 161 */       long start = System.currentTimeMillis();
/* 162 */       long totalBytes = 0L;
/*     */ 
/* 164 */       int opCount = 0;
/* 165 */       while ((count = inputStream.read(buf)) > 0)
/*     */       {
/*     */         int count;
/* 167 */         totalBytes += count;
/* 168 */         outputStream.write(buf, 0, count);
/* 169 */         ++opCount;
/*     */       }
/* 171 */       this.m_bufferPool.releaseBuffer(buf);
/* 172 */       long duration = System.currentTimeMillis() - start;
/* 173 */       Report.trace("filestore", "copied " + totalBytes + " in " + duration + "ms.  " + totalBytes / (duration / 1000.0D) / 1000.0D + " KBps", null);
/*     */     }
/*     */     finally
/*     */     {
/* 178 */       FileUtils.closeObjectsEx(inputStream, outputStream);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void copyToLocalFile(IdcFileDescriptor descriptor, File localPath, Map args)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 185 */     OutputStream os = FileUtilsCfgBuilder.getCfgOutputStream(localPath);
/* 186 */     copyToOutputStream(descriptor, os, args);
/*     */   }
/*     */ 
/*     */   public void moveFile(IdcFileDescriptor source, IdcFileDescriptor target, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 192 */     this.m_fileHelper.moveFile(source, target, args, cxt);
/*     */   }
/*     */ 
/*     */   public void duplicateFile(IdcFileDescriptor source, IdcFileDescriptor target, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 198 */     this.m_fileHelper.duplicateFile(source, target, args, cxt);
/*     */   }
/*     */ 
/*     */   public void deleteFile(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 204 */     this.m_fileHelper.deleteFile(descriptor, args, cxt);
/*     */   }
/*     */ 
/*     */   public void forceToFilesystemPath(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 211 */     boolean notFileStoreCreate = false;
/* 212 */     if (args != null)
/*     */     {
/* 214 */       notFileStoreCreate = StringUtils.convertToBool((String)args.get("isNotFileStoreCreate"), false);
/*     */     }
/*     */ 
/* 218 */     if (!notFileStoreCreate) {
/*     */       return;
/*     */     }
/*     */ 
/* 222 */     this.m_fileHelper.prepareForFileCreation(descriptor, args, cxt);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 230 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99381 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.filesystem.BaseAccessImplementor
 * JD-Core Version:    0.5.4
 */