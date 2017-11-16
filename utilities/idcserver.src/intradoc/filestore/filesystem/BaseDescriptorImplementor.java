/*     */ package intradoc.filestore.filesystem;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.filestore.BaseFileHelper;
/*     */ import intradoc.filestore.BaseFileStore;
/*     */ import intradoc.filestore.BasicIdcFileDescriptor;
/*     */ import intradoc.filestore.CommonStoreImplementor;
/*     */ import intradoc.filestore.FileStoreDescriptorImplementor;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.LegacyDocumentPathBuilder;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class BaseDescriptorImplementor
/*     */   implements FileStoreDescriptorImplementor, CommonStoreImplementor
/*     */ {
/*     */   public Provider m_provider;
/*     */   public BaseFileStore m_fileStore;
/*     */   public BaseFileHelper m_fileHelper;
/*     */ 
/*     */   public void preInit(FileStoreProvider fs, Provider provider)
/*     */   {
/*  41 */     this.m_fileStore = ((BaseFileStore)fs);
/*  42 */     this.m_fileHelper = this.m_fileStore.m_fileHelper;
/*  43 */     this.m_provider = provider;
/*     */   }
/*     */ 
/*     */   public void init(FileStoreProvider fs, Provider provider)
/*     */   {
/*     */   }
/*     */ 
/*     */   public IdcFileDescriptor createDescriptor(Parameters metaData, Map args, ExecutionContext context)
/*     */     throws ServiceException, DataException
/*     */   {
/*  54 */     String rendition = metaData.get("RenditionId");
/*  55 */     BasicIdcFileDescriptor descriptor = new BasicIdcFileDescriptor();
/*  56 */     if (context != null)
/*     */     {
/*  58 */       context.setCachedObject("descriptor", descriptor);
/*     */     }
/*     */ 
/*  61 */     Properties localData = new Properties();
/*  62 */     metaData = new PropParameters(localData, metaData);
/*  63 */     localData.put("RenditionId.path", rendition);
/*     */ 
/*  65 */     String storageClass = this.m_fileStore.computeStorageClass(metaData);
/*  66 */     descriptor.put("StorageClass", storageClass);
/*  67 */     boolean isContainer = false;
/*  68 */     if (args != null)
/*     */     {
/*  70 */       isContainer = StringUtils.convertToBool((String)args.get("isContainer"), false);
/*     */ 
/*  72 */       descriptor.put("isContainer", "" + isContainer);
/*     */     }
/*     */ 
/*  75 */     String path = null;
/*  76 */     if (storageClass.equals("web"))
/*     */     {
/*  78 */       String name = metaData.get("dDocName");
/*  79 */       if (rendition.startsWith("rendition"))
/*     */       {
/*  81 */         String revisionLabel = metaData.get("dRevLabel");
/*  82 */         String renditionFlag = rendition.substring("rendition".length() + 1);
/*     */ 
/*  85 */         AdditionalRenditions renditions = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*     */ 
/*  87 */         String renditionExtension = renditions.getExtension(renditionFlag);
/*  88 */         localData.put("dWebExtension", renditionExtension);
/*     */ 
/*  91 */         descriptor.put("dWebExtension", renditionExtension);
/*     */ 
/*  93 */         String webPathDir = LegacyDirectoryLocator.computeWebPathDir(metaData);
/*  94 */         path = webPathDir;
/*  95 */         if (!isContainer)
/*     */         {
/*  97 */           path = path + LegacyDocumentPathBuilder.computeRenditionFilename(name, renditionFlag, revisionLabel, renditionExtension);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 103 */         if (metaData.get("dWebExtension") == null)
/*     */         {
/* 105 */           String dExtension = metaData.get("dExtension");
/* 106 */           if (dExtension == null)
/*     */           {
/* 108 */             Report.trace("filestore", "dWebExtension missing", null);
/*     */           }
/*     */           else
/*     */           {
/* 112 */             Report.trace("filestore", "dWebExtension missing, using " + dExtension + " instead", null);
/*     */           }
/*     */         }
/*     */ 
/* 116 */         if (isContainer)
/*     */         {
/* 118 */           path = LegacyDirectoryLocator.computeWebPathDir(metaData);
/*     */         }
/*     */         else
/*     */         {
/* 122 */           path = LegacyDirectoryLocator.computeCurrentReleaseWebPath(name, metaData);
/*     */         }
/*     */       }
/*     */     }
/* 126 */     else if (storageClass.equals("vault"))
/*     */     {
/* 128 */       if (isContainer)
/*     */       {
/* 130 */         path = LegacyDirectoryLocator.getVaultDirectory() + LegacyDocumentPathBuilder.computeRelativeVaultDir(metaData);
/*     */       }
/*     */       else
/*     */       {
/* 135 */         DataBinder binder = DataBinderUtils.createBinderFromParameters(metaData, context);
/* 136 */         PluginFilters.filter("createDescriptorFileName", null, binder, context);
/* 137 */         String filePrefix = binder.getLocal(rendition + ":prefix");
/* 138 */         String fileName = binder.getLocal(rendition + ":filename");
/* 139 */         if (fileName == null)
/*     */         {
/* 141 */           fileName = LegacyDirectoryLocator.computeVaultFileName(metaData);
/*     */         }
/* 143 */         if (filePrefix != null)
/*     */         {
/* 145 */           fileName = filePrefix + fileName;
/*     */         }
/* 147 */         path = LegacyDirectoryLocator.computeVaultPath(fileName, metaData);
/*     */ 
/* 149 */         descriptor.put("fileNamePrefix", filePrefix);
/*     */       }
/*     */     }
/* 152 */     else if (storageClass.equals("webIndex"))
/*     */     {
/* 154 */       String name = metaData.get("FileName");
/* 155 */       if ((name != null) && (name.equals("portal.htm")))
/*     */       {
/* 157 */         path = SharedObjects.getEnvironmentValue("WeblayoutDir");
/* 158 */         path = FileUtils.directorySlashes(path) + "portal.htm";
/* 159 */         descriptor.put("dSecurityGroup", "");
/* 160 */         descriptor.put("dDocAccount", "");
/*     */       }
/*     */       else
/*     */       {
/* 164 */         String securityGroup = metaData.get("dSecurityGroup");
/* 165 */         String groupDir = LegacyDirectoryLocator.getWebGroupRootDirectory(securityGroup);
/* 166 */         String relativeWebDir = LegacyDocumentPathBuilder.computeWebDirSecuritySuffix(metaData) + "pages/";
/*     */ 
/* 169 */         if (!isContainer)
/*     */         {
/* 171 */           name = name.toLowerCase();
/* 172 */           path = groupDir + relativeWebDir + name + ".htm";
/*     */         }
/*     */       }
/*     */     } else {
/* 176 */       if ((storageClass.equals("data")) || (storageClass.equals("config")))
/*     */       {
/* 179 */         String msg = LocaleUtils.encodeMessage("csFsStorageClassNotSupported", null, storageClass);
/*     */ 
/* 181 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/* 185 */       String msg = LocaleUtils.encodeMessage("csFsStorageClassNotSupported", null, storageClass);
/*     */ 
/* 187 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 190 */     descriptor.put("path", path);
/* 191 */     descriptor.put("dRenditionId", rendition);
/* 192 */     descriptor.put("RenditionId.path", rendition);
/*     */ 
/* 196 */     descriptor.put("uniqueId", path);
/*     */ 
/* 198 */     if (context != null)
/*     */     {
/* 202 */       context.setCachedObject("descriptor", "");
/*     */     }
/* 204 */     if (SystemUtils.m_verbose)
/*     */     {
/* 206 */       String msg = "storageclass=" + storageClass + " rendition=" + rendition + " path=" + path;
/*     */ 
/* 208 */       Report.trace("filestore", "BaseDescriptorImplementor.createDescriptor: " + msg, null);
/*     */     }
/* 210 */     return descriptor;
/*     */   }
/*     */ 
/*     */   public String getClientURL(IdcFileDescriptor descriptor, String baseUrl, Map args, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/* 216 */     String path = null;
/* 217 */     String storageClass = descriptor.getProperty("StorageClass");
/* 218 */     Map data = this.m_fileStore.getKeyMetaData(descriptor);
/* 219 */     MapParameters params = new MapParameters(data);
/*     */ 
/* 221 */     boolean isAbsolute = this.m_fileStore.getConfigBoolean("useAbsolute", args, false, false);
/* 222 */     if ((storageClass.equals("web")) || (storageClass.equals("vault")))
/*     */     {
/* 225 */       boolean isPureRel = this.m_fileStore.getConfigBoolean("isPureRelative", args, false, false);
/* 226 */       if (isPureRel)
/*     */       {
/* 228 */         path = LegacyDocumentPathBuilder.computeWebDirPartialPath(params);
/*     */       }
/*     */       else
/*     */       {
/* 232 */         path = LegacyDocumentPathBuilder.computeWebUrlDir(params, isAbsolute);
/*     */       }
/*     */ 
/* 235 */       boolean isContainer = this.m_fileStore.getConfigBoolean("isContainer", args, false, false);
/*     */ 
/* 237 */       if (!isContainer)
/*     */       {
/* 239 */         String fileName = descriptor.getProperty("dDocName");
/*     */ 
/* 241 */         String revLabel = descriptor.getProperty("dRevLabel");
/* 242 */         String releaseState = descriptor.getProperty("dReleaseState");
/*     */ 
/* 244 */         boolean isReleased = false;
/* 245 */         if ((releaseState == null) || ("YUI".indexOf(releaseState) >= 0))
/*     */         {
/* 247 */           isReleased = true;
/*     */         }
/* 249 */         if (!isReleased)
/*     */         {
/* 251 */           fileName = fileName + "~" + revLabel;
/*     */         }
/* 253 */         String extension = descriptor.getProperty("dWebExtension");
/* 254 */         if ((extension == null) || (extension.length() == 0))
/*     */         {
/* 256 */           extension = descriptor.getProperty("dExtension");
/*     */         }
/* 258 */         if ((extension != null) && (extension.length() > 0))
/*     */         {
/* 260 */           fileName = fileName + "." + extension;
/*     */         }
/* 262 */         path = path + fileName.toLowerCase();
/*     */       }
/*     */     }
/* 265 */     else if (storageClass.equals("webIndex"))
/*     */     {
/* 267 */       String name = params.get("FileName");
/* 268 */       if (name.equals("portal.htm"))
/*     */       {
/* 270 */         path = LegacyDirectoryLocator.getWebRoot(isAbsolute);
/* 271 */         path = path + "portal.htm";
/*     */       }
/* 273 */       String securityGroup = params.get("dSecurityGroup");
/* 274 */       String groupDir = LegacyDirectoryLocator.getWebGroupRootDirectory(securityGroup);
/* 275 */       String relativeWebDir = LegacyDocumentPathBuilder.computeWebDirSecuritySuffix(params) + "pages/";
/*     */ 
/* 278 */       name = name.toLowerCase();
/* 279 */       name = StringUtils.urlEncode(name);
/* 280 */       path = groupDir + relativeWebDir + name + ".htm";
/*     */     }
/*     */     else
/*     */     {
/* 284 */       String msg = LocaleUtils.encodeMessage("csFsStorageClassNotSupported", null, storageClass);
/*     */ 
/* 286 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 289 */     if (SystemUtils.m_verbose)
/*     */     {
/* 291 */       String msg = "storageclass=" + storageClass + " path=" + path;
/* 292 */       Report.trace("filestore", "BaseDescriptorImplementor.getClientURL: " + msg, null);
/*     */     }
/* 294 */     return path;
/*     */   }
/*     */ 
/*     */   public IdcFileDescriptor getContainer(IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException
/*     */   {
/* 301 */     BasicIdcFileDescriptor cDesc = (BasicIdcFileDescriptor)descriptor.createClone();
/* 302 */     boolean isContainer = StringUtils.convertToBool(descriptor.getProperty("isContainer"), false);
/*     */ 
/* 304 */     if (!isContainer)
/*     */     {
/* 306 */       String path = getContainerPath(cDesc, args, null);
/* 307 */       cDesc.put("path", path);
/* 308 */       cDesc.put("isContainer", "1");
/*     */     }
/* 310 */     return cDesc;
/*     */   }
/*     */ 
/*     */   public String getContainerPath(IdcFileDescriptor descriptor, Map args, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/* 317 */     if (args == null)
/*     */     {
/* 319 */       args = new HashMap();
/*     */     }
/* 321 */     args.put("doContainerPath", "1");
/* 322 */     return getFilesystemPathWithArgs(descriptor, args, context);
/*     */   }
/*     */ 
/*     */   public boolean compareDescriptors(IdcFileDescriptor desc1, IdcFileDescriptor desc2, Map args, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/* 331 */     String path1 = null;
/* 332 */     String path2 = null;
/*     */ 
/* 334 */     String cStr = (args != null) ? (String)args.get("isContainer") : null;
/* 335 */     boolean isContainer = StringUtils.convertToBool(cStr, false);
/* 336 */     if (isContainer)
/*     */     {
/* 338 */       path1 = getContainerPath(desc1, null, context);
/* 339 */       path2 = getContainerPath(desc2, null, context);
/*     */     }
/*     */     else
/*     */     {
/* 343 */       path1 = desc1.getProperty("uniqueId");
/* 344 */       path2 = desc2.getProperty("uniqueId");
/*     */     }
/* 346 */     return path1.equals(path2);
/*     */   }
/*     */ 
/*     */   public String getFilesystemPath(IdcFileDescriptor descriptor, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/* 354 */     return getFilesystemPathWithArgs(descriptor, null, context);
/*     */   }
/*     */ 
/*     */   public String getFilesystemPathWithArgs(IdcFileDescriptor descriptor, Map args, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/* 361 */     return this.m_fileHelper.getFilesystemPathWithArgs(descriptor, args, context);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 368 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98822 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.filesystem.BaseDescriptorImplementor
 * JD-Core Version:    0.5.4
 */