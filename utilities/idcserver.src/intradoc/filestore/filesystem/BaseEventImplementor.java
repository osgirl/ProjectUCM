/*     */ package intradoc.filestore.filesystem;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.filestore.BaseFileStore;
/*     */ import intradoc.filestore.CommonStoreImplementor;
/*     */ import intradoc.filestore.FileStoreEventImplementor;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.utils.DocumentInfoCacheUtils;
/*     */ import intradoc.shared.FilterImplementor;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class BaseEventImplementor
/*     */   implements FileStoreEventImplementor, CommonStoreImplementor
/*     */ {
/*     */   public Provider m_provider;
/*     */   public BaseFileStore m_fileStore;
/*     */ 
/*     */   public void preInit(FileStoreProvider fs, Provider provider)
/*     */   {
/*  43 */     this.m_fileStore = ((BaseFileStore)fs);
/*  44 */     this.m_provider = provider;
/*     */   }
/*     */ 
/*     */   public void init(FileStoreProvider fs, Provider provider)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void renameFileToLabel(IdcFileDescriptor descriptor)
/*     */     throws IOException
/*     */   {
/*  54 */     String storageClass = descriptor.getProperty("StorageClass");
/*  55 */     if ((!storageClass.equals("web")) && 
/*  57 */       (SystemUtils.m_verbose))
/*     */     {
/*  59 */       Report.debug("filestore", "not renaming '" + descriptor.getProperty("path") + "'", null);
/*     */     }
/*     */ 
/*  63 */     String docName = descriptor.getProperty("dDocName");
/*  64 */     String revLabel = descriptor.getProperty("dRevLabel");
/*  65 */     String extension = descriptor.getProperty("dWebExtension");
/*  66 */     String status = descriptor.getProperty("dStatus");
/*  67 */     String filePath = descriptor.getProperty("path");
/*  68 */     String webDocumentDir = FileUtils.getDirectory(filePath);
/*     */ 
/*  70 */     String docPathStart = docName;
/*  71 */     String oldNameStr = docPathStart;
/*  72 */     String newNameStr = docPathStart + "~" + revLabel;
/*     */ 
/*  74 */     if ((extension != null) && (extension.length() > 0))
/*     */     {
/*  76 */       oldNameStr = oldNameStr + "." + extension;
/*  77 */       newNameStr = newNameStr + "." + extension;
/*     */     }
/*  79 */     oldNameStr = oldNameStr.toLowerCase();
/*  80 */     newNameStr = newNameStr.toLowerCase();
/*  81 */     oldNameStr = webDocumentDir + "/" + oldNameStr;
/*  82 */     newNameStr = webDocumentDir + "/" + newNameStr;
/*     */ 
/*  84 */     File oldName = new File(oldNameStr);
/*  85 */     File newName = new File(newNameStr);
/*     */ 
/*  87 */     boolean oldNameExists = oldName.exists();
/*  88 */     if (SystemUtils.m_verbose)
/*     */     {
/*  90 */       if (oldNameExists)
/*     */       {
/*  92 */         Report.debug("filestore", "Demoting current release webviewable to path " + newNameStr, null);
/*     */       }
/*     */       else
/*     */       {
/*  97 */         Report.debug("filestore", "We are assuming path " + newNameStr + " is already demoted", null);
/*     */       }
/*     */     }
/*     */ 
/* 101 */     if (oldNameExists)
/*     */     {
/* 103 */       if (!newName.exists())
/*     */       {
/*     */         try
/*     */         {
/* 107 */           FileUtils.renameFile(oldNameStr, newNameStr);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 111 */           if (!newName.exists())
/*     */           {
/* 113 */             Report.error("filestore", e, "csIndexerRenameDisappeared4", new Object[] { oldName.getAbsolutePath(), newName.getAbsolutePath() });
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 120 */         Report.trace("filestore", "Stopped demotion to " + newNameStr + " because file is already present", null);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 128 */       if ((newName.exists()) || (status.equals("DELETED")))
/*     */         return;
/* 130 */       String msg = LocaleUtils.encodeMessage("csIndexerRenameTargetMissing", null, newName.getAbsolutePath());
/*     */ 
/* 132 */       Report.error(null, msg, null);
/* 133 */       Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void renameFileWithoutLabel(IdcFileDescriptor descriptor)
/*     */     throws DataException, ServiceException
/*     */   {
/* 142 */     String storageClass = descriptor.getProperty("StorageClass");
/* 143 */     if (!storageClass.equals("web"))
/*     */     {
/* 145 */       if (SystemUtils.m_verbose)
/*     */       {
/* 147 */         Report.debug("filestore", "not renaming '" + descriptor.getProperty("path") + "'", null);
/*     */       }
/*     */ 
/* 150 */       return;
/*     */     }
/* 152 */     Map metaData = this.m_fileStore.getKeyMetaData(descriptor);
/* 153 */     String webDocumentsDir = LegacyDirectoryLocator.computeWebPathDir(new MapParameters(metaData));
/*     */ 
/* 156 */     String docName = descriptor.getProperty("dDocName").toLowerCase();
/* 157 */     String revLabel = descriptor.getProperty("dRevLabel");
/* 158 */     if (revLabel != null)
/*     */     {
/* 160 */       revLabel = revLabel.toLowerCase();
/*     */     }
/*     */ 
/* 163 */     String webExtension = descriptor.getProperty("dWebExtension");
/* 164 */     String oldNameStr = webDocumentsDir + docName + "~" + revLabel;
/* 165 */     String fileName = docName;
/* 166 */     if ((webExtension != null) && (webExtension.length() > 0))
/*     */     {
/* 168 */       oldNameStr = oldNameStr + "." + webExtension.toLowerCase();
/* 169 */       fileName = fileName + "." + webExtension.toLowerCase();
/*     */     }
/*     */ 
/* 172 */     String newNameStr = webDocumentsDir + fileName;
/*     */ 
/* 174 */     File oldName = new File(oldNameStr);
/* 175 */     File newName = new File(newNameStr);
/*     */ 
/* 177 */     boolean oldExists = oldName.exists();
/* 178 */     boolean newExists = newName.exists();
/* 179 */     boolean fileRenamed = newExists;
/* 180 */     Exception capturedException = null;
/* 181 */     if (oldExists)
/*     */     {
/*     */       try
/*     */       {
/* 185 */         FileUtils.renameFile(oldNameStr, newNameStr);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 189 */         capturedException = e;
/* 190 */         Report.trace("filestore", e, "csIndexerRenameDisappeared4", new Object[] { oldName.getAbsolutePath(), newName.getAbsolutePath() });
/*     */       }
/*     */ 
/* 196 */       if (newName.exists())
/*     */       {
/* 198 */         fileRenamed = true;
/*     */       }
/*     */       else
/*     */       {
/* 202 */         fileRenamed = false;
/*     */       }
/*     */     }
/*     */ 
/* 206 */     int rc = -1;
/* 207 */     IdcMessage priorMsg = IdcMessageFactory.lc("syFileDoesNotExist", new Object[] { newNameStr });
/* 208 */     IdcMessage idcMsg = null;
/* 209 */     if (!fileRenamed)
/*     */     {
/* 211 */       if (oldExists)
/*     */       {
/* 213 */         if (oldName.exists())
/*     */         {
/* 215 */           idcMsg = IdcMessageFactory.lc("csIndexerRenameOldExists", new Object[0]);
/* 216 */           rc = -17;
/*     */         }
/*     */         else
/*     */         {
/* 220 */           idcMsg = IdcMessageFactory.lc("csIndexerRenameDisappeared", new Object[0]);
/* 221 */           rc = -25;
/*     */         }
/*     */       }
/*     */     }
/* 225 */     else if (!newName.exists())
/*     */     {
/* 228 */       if (oldExists)
/*     */       {
/* 230 */         idcMsg = IdcMessageFactory.lc("csIndexerRenameDisappeared2", new Object[] { oldNameStr });
/* 231 */         rc = -23;
/*     */       }
/*     */       else
/*     */       {
/* 235 */         idcMsg = IdcMessageFactory.lc("csIndexerRenameDisappeared3", new Object[] { oldNameStr });
/* 236 */         rc = -26;
/*     */       }
/*     */     }
/* 239 */     if (idcMsg != null)
/*     */     {
/* 241 */       idcMsg.m_prior = priorMsg;
/* 242 */       throw new ServiceException(capturedException, rc, idcMsg);
/*     */     }
/* 244 */     updateWebFileTimestamp(descriptor);
/*     */ 
/* 246 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 248 */     String msg = "storageclass=" + storageClass + " oldName=" + oldName + " newName=" + newName;
/* 249 */     Report.trace("filestore", "BaseEventImplementor.renameFileWithoutLabel: " + msg, null);
/*     */   }
/*     */ 
/*     */   public void updateWebFileTimestamp(IdcFileDescriptor desc)
/*     */   {
/*     */     try
/*     */     {
/* 261 */       String webExtension = desc.getProperty("dWebExtension");
/* 262 */       Map docProps = new HashMap();
/* 263 */       docProps.put("dWebExtension", webExtension);
/* 264 */       if (DocumentInfoCacheUtils.supportsWebviewableTimestampUpdate(docProps))
/*     */       {
/* 266 */         String path = this.m_fileStore.getFilesystemPath(desc, null);
/* 267 */         File newFile = new File(path);
/* 268 */         if (newFile.exists())
/*     */         {
/* 270 */           newFile.setLastModified(System.currentTimeMillis());
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 276 */       Report.trace("filestore", "Error while updating timestamp for " + desc.getProperty("dDocName") + ": " + e.getMessage(), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void notifyOfEvent(IdcFileDescriptor descriptor, Map data, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 285 */     String type = (String)data.get("EventType");
/* 286 */     if (type == null)
/*     */     {
/* 288 */       type = "(null)";
/*     */     }
/* 290 */     renameOnEvent(type, descriptor, data, cxt);
/* 291 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 293 */     String msg = LocaleUtils.encodeMessage("csFsUnknownEventType", null, type);
/* 294 */     Report.debug("filestore", LocaleResources.localizeMessage(msg, null), null);
/*     */   }
/*     */ 
/*     */   public void renameOnEvent(String type, IdcFileDescriptor descriptor, Map data, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 302 */     if (type.equals("event_released"))
/*     */     {
/* 304 */       renameFileWithoutLabel(descriptor);
/*     */     } else {
/* 306 */       if (!type.equals("event_unreleased"))
/*     */         return;
/* 308 */       renameFileToLabel(descriptor);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void registerEventFilter(FilterImplementor filter)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void unregisterEventFilter(FilterImplementor filter)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public List getEventFilters() throws ServiceException
/*     */   {
/* 324 */     return new ArrayList();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 331 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81852 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.filesystem.BaseEventImplementor
 * JD-Core Version:    0.5.4
 */