/*     */ package intradoc.filestore.config;
/*     */ 
/*     */ import intradoc.common.EventInputStream;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StreamEventHandler;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.filestore.BaseFileStore;
/*     */ import intradoc.filestore.FileStoreEventData;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.filestore.filesystem.BaseAccessImplementor;
/*     */ import intradoc.provider.Provider;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ConfigAccessImplementor extends BaseAccessImplementor
/*     */   implements StreamEventHandler
/*     */ {
/*     */   protected ConfigDescriptorImplementor m_descriptor;
/*     */   protected ConfigMetadataImplementor m_metadata;
/*     */   protected ServerData m_server;
/*     */ 
/*     */   protected void createContainerDirectories(IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/*  71 */     if (!this.m_fileStore.getConfigBoolean("doCreateContainers", args, true, false))
/*     */     {
/*  74 */       return;
/*     */     }
/*  76 */     IdcFileDescriptor container = this.m_descriptor.getContainer(descriptor, args);
/*  77 */     String createDir = container.getProperty("pathname");
/*  78 */     String intradocDir = this.m_server.getIntradocDir();
/*     */ 
/*  80 */     boolean doLockfile = this.m_fileStore.getConfigBoolean(ConfigFileStore.F_CREATE_LOCKFILE, args, true, false);
/*     */ 
/*  82 */     boolean doSubDirectoryCreate = true;
/*  83 */     if (FileUtils.isAbsolutePath(createDir))
/*     */     {
/*  85 */       if (createDir.startsWith(intradocDir))
/*     */       {
/*  87 */         createDir = createDir.substring(intradocDir.length());
/*     */       }
/*     */       else
/*     */       {
/*  91 */         doSubDirectoryCreate = false;
/*     */       }
/*     */     }
/*  94 */     if (doSubDirectoryCreate)
/*     */     {
/*  96 */       FileUtils.checkOrCreateSubDirectoryEx(intradocDir, createDir, doLockfile);
/*     */     }
/*     */     else
/*     */     {
/* 100 */       Report.trace("system", "Relative container path " + createDir + " not normalized against install directory " + intradocDir, null);
/*     */ 
/* 102 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(createDir, 3, doLockfile);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(FileStoreProvider fs, Provider provider)
/*     */   {
/* 111 */     super.init(fs, provider);
/* 112 */     this.m_fileStore = ((BaseFileStore)fs);
/* 113 */     this.m_descriptor = ((ConfigDescriptorImplementor)this.m_fileStore.getImplementor("DescriptorImplementor"));
/* 114 */     this.m_metadata = ((ConfigMetadataImplementor)this.m_fileStore.getImplementor("MetadataImplementor"));
/* 115 */     this.m_server = ((ServerData)provider.getProviderObject("ServerData"));
/*     */   }
/*     */ 
/*     */   public OutputStream getOutputStream(IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 124 */     String pathname = this.m_metadata.validateDescriptorMetaData(descriptor, false);
/* 125 */     String useWrapper = descriptor.getProperty("useWrapper");
/* 126 */     if (false == StringUtils.convertToBool(useWrapper, false))
/*     */     {
/* 128 */       createContainerDirectories(descriptor, args);
/*     */ 
/* 130 */       boolean isDoTemp = this.m_fileStore.getConfigBoolean("isDoTemp", args, true, false);
/*     */ 
/* 132 */       if (isDoTemp)
/*     */       {
/* 135 */         pathname = pathname + ".tmp";
/*     */       }
/*     */ 
/* 138 */       boolean doLock = this.m_fileStore.getConfigBoolean(ConfigFileStore.O_LOCK_CONTAINER, args, false, false);
/*     */ 
/* 140 */       if (doLock)
/*     */       {
/* 148 */         String parentPath = (String)descriptor.get("containerPath");
/* 149 */         FileUtils.reserveDirectory(parentPath);
/*     */       }
/* 151 */       OutputStream stream = FileUtilsCfgBuilder.getCfgOutputStream(pathname, null);
/*     */ 
/* 153 */       return stream;
/*     */     }
/*     */ 
/* 161 */     OutputStream out = (OutputStream)this.m_server.execServerIO(this.m_server.SERVER_ACTION_WRITE, pathname);
/* 162 */     return out;
/*     */   }
/*     */ 
/*     */   public void commitOutputStream(OutputStream stream, IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/*     */     try
/*     */     {
/* 171 */       if (stream == null) {
/*     */         boolean doUnlock;
/*     */         String parentPath;
/*     */         return;
/* 175 */       }String pathname = this.m_metadata.validateDescriptorMetaData(descriptor, false);
/* 176 */       String useWrapStr = descriptor.getProperty("useWrapper");
/* 177 */       boolean useWrapper = StringUtils.convertToBool(useWrapStr, false);
/* 178 */       if (useWrapper) {
/*     */         boolean doUnlock;
/*     */         String parentPath;
/*     */         return;
/* 182 */       }boolean isDoTemp = this.m_fileStore.getConfigBoolean("isDoTemp", args, true, false);
/*     */ 
/* 184 */       boolean isDoBackup = this.m_fileStore.getConfigBoolean("isDoBackup", args, true, false);
/*     */ 
/* 186 */       File f = FileUtilsCfgBuilder.getCfgFile(pathname, null);
/* 187 */       if (isDoBackup)
/*     */       {
/* 189 */         File old = FileUtilsCfgBuilder.getCfgFile(pathname + ".old", null);
/*     */ 
/* 191 */         if (!old.delete())
/*     */         {
/* 195 */           Report.trace("filestore", "unable to delete '" + pathname + ".old'", null);
/*     */         }
/*     */ 
/* 198 */         if (!f.renameTo(old))
/*     */         {
/* 200 */           Report.trace("filestore", "unable to rename '" + pathname + "' to '" + pathname + ".old'", null);
/*     */         }
/*     */       }
/* 203 */       if (isDoTemp)
/*     */       {
/* 205 */         File tmp = FileUtilsCfgBuilder.getCfgFile(pathname + ".tmp", null);
/* 206 */         if (!tmp.renameTo(f))
/*     */         {
/* 208 */           Report.trace("filestore", "unable to rename '" + pathname + ".tmp' to '" + pathname + "'", null);
/*     */         }
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/*     */       boolean doUnlock;
/*     */       String parentPath;
/* 215 */       boolean doUnlock = this.m_fileStore.getConfigBoolean(ConfigFileStore.O_UNLOCK_CONTAINER, args, false, false);
/*     */ 
/* 217 */       if (doUnlock)
/*     */       {
/* 220 */         String parentPath = (String)descriptor.get("containerPath");
/* 221 */         FileUtils.releaseDirectory(parentPath);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public InputStream getInputStream(IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 242 */     String pathname = this.m_metadata.validateDescriptorMetaData(descriptor, false);
/* 243 */     String useWrapper = descriptor.getProperty("useWrapper");
/* 244 */     boolean didLock = false;
/* 245 */     String parentPath = null;
/* 246 */     if ((null == useWrapper) || (false == StringUtils.convertToBool(useWrapper, false)))
/*     */     {
/* 248 */       boolean requireExist = false;
/* 249 */       if (null != args)
/*     */       {
/* 251 */         String value = (String)args.get(ConfigFileStore.O_REQUIRE_EXISTENCE);
/* 252 */         requireExist = StringUtils.convertToBool(value, false);
/*     */       }
/*     */ 
/*     */       InputStream stream;
/*     */       try
/*     */       {
/* 258 */         boolean doLock = this.m_fileStore.getConfigBoolean(ConfigFileStore.O_LOCK_CONTAINER, args, false, false);
/*     */ 
/* 260 */         if (doLock)
/*     */         {
/* 262 */           parentPath = (String)descriptor.get("containerPath");
/*     */           try
/*     */           {
/* 265 */             if (FileUtils.checkFile(parentPath, 2) == 0)
/*     */             {
/* 269 */               FileUtils.reserveDirectory(parentPath);
/* 270 */               didLock = true;
/*     */             }
/*     */             else
/*     */             {
/* 274 */               String msg = LocaleUtils.encodeMessage("syFileUtilsDirNotFound", null, parentPath);
/*     */ 
/* 276 */               throw new FileNotFoundException(msg);
/*     */             }
/*     */           }
/*     */           catch (ServiceException e)
/*     */           {
/* 281 */             if ((requireExist) && (e.m_errorCode == -16))
/*     */             {
/* 283 */               throw e;
/*     */             }
/*     */           }
/*     */         }
/* 287 */         stream = FileUtilsCfgBuilder.getCfgInputStream(pathname);
/*     */       }
/*     */       catch (FileNotFoundException e)
/*     */       {
/* 291 */         if ((didLock) && (parentPath != null))
/*     */         {
/* 293 */           FileUtils.releaseDirectory(parentPath);
/*     */         }
/* 295 */         if (requireExist)
/*     */         {
/* 297 */           throw e;
/*     */         }
/* 299 */         return null;
/*     */       }
/*     */       finally
/*     */       {
/*     */       }
/*     */ 
/* 305 */       EventInputStream eventStream = new EventInputStream(stream);
/* 306 */       FileStoreEventData data = new FileStoreEventData(descriptor, args);
/* 307 */       eventStream.addStreamEventHandler(this, data);
/* 308 */       return eventStream;
/*     */     }
/* 310 */     InputStream in = (InputStream)this.m_server.execServerIO(this.m_server.SERVER_ACTION_READ, pathname);
/* 311 */     return in;
/*     */   }
/*     */ 
/*     */   public void duplicateFile(IdcFileDescriptor source, IdcFileDescriptor target, Map args, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 323 */     String msg = LocaleUtils.encodeMessage("syNotSupported", null, "duplicateFile", "ConfigFileStore");
/*     */ 
/* 325 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public void moveFile(IdcFileDescriptor source, IdcFileDescriptor target, Map args, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 333 */     String msg = LocaleUtils.encodeMessage("syNotSupported", null, "moveFile", "ConfigFileStore");
/*     */ 
/* 335 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public void deleteFile(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 342 */     String pathname = this.m_metadata.validateDescriptorMetaData(descriptor, false);
/* 343 */     File f = FileUtilsCfgBuilder.getCfgFile(pathname, null);
/* 344 */     f.delete();
/*     */   }
/*     */ 
/*     */   public void handleStreamEvent(String event, Object stream, Object data)
/*     */     throws IOException
/*     */   {
/* 352 */     if (event != "close")
/*     */       return;
/* 354 */     this.m_fileStore.handleStreamEvent(event, stream, data);
/* 355 */     if (!stream instanceof EventInputStream)
/*     */     {
/* 357 */       return;
/*     */     }
/* 359 */     FileStoreEventData eventData = (FileStoreEventData)data;
/* 360 */     Map args = eventData.m_eventArgs;
/* 361 */     IdcFileDescriptor descriptor = eventData.m_descriptor;
/*     */ 
/* 363 */     boolean doUnlock = this.m_fileStore.getConfigBoolean(ConfigFileStore.O_UNLOCK_CONTAINER, args, false, false);
/*     */ 
/* 365 */     if (!doUnlock) {
/*     */       return;
/*     */     }
/* 368 */     String parentPath = (String)descriptor.get("containerPath");
/* 369 */     FileUtils.releaseDirectory(parentPath);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 377 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97486 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.config.ConfigAccessImplementor
 * JD-Core Version:    0.5.4
 */