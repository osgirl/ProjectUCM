/*     */ package intradoc.filestore.filesystem;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.filestore.BaseFileHelper;
/*     */ import intradoc.filestore.BaseFileStore;
/*     */ import intradoc.filestore.BasicIdcFileDescriptor;
/*     */ import intradoc.filestore.CommonStoreImplementor;
/*     */ import intradoc.filestore.FileStoreMetadataImplementor;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.provider.Provider;
/*     */ import java.io.IOException;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class BaseMetadataImplementor
/*     */   implements FileStoreMetadataImplementor, CommonStoreImplementor
/*     */ {
/*     */   public BaseFileStore m_fileStore;
/*     */   public Provider m_provider;
/*     */   public BaseFileHelper m_fileHelper;
/*     */ 
/*     */   public void preInit(FileStoreProvider fs, Provider provider)
/*     */   {
/*  39 */     this.m_fileStore = ((BaseFileStore)fs);
/*  40 */     this.m_provider = provider;
/*  41 */     this.m_fileHelper = this.m_fileStore.m_fileHelper;
/*     */   }
/*     */ 
/*     */   public void init(FileStoreProvider fs, Provider provider)
/*     */   {
/*     */   }
/*     */ 
/*     */   public Map getStorageData(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/*  52 */     Map data = this.m_fileStore.getKeyMetaData(descriptor);
/*  53 */     copyMetaData(descriptor, data, new String[] { "fileSize", "uniqueId", "path", "fileExists", "lastModified", "RenditionId", "RenditionId.path" });
/*     */ 
/*  57 */     return data;
/*     */   }
/*     */ 
/*     */   public Map getKeyMetaData(IdcFileDescriptor descriptor) throws DataException, ServiceException
/*     */   {
/*  62 */     Map metaData = (Map)this.m_provider.getProviderObject("MetaData");
/*  63 */     String key = this.m_fileStore.computeStorageKey(descriptor);
/*  64 */     String[] metaDataList = (String[])(String[])metaData.get(key);
/*     */ 
/*  66 */     Properties metaProps = new Properties();
/*  67 */     copyMetaData(descriptor, metaProps, metaDataList);
/*     */ 
/*  69 */     metaDataList = (String[])(String[])metaData.get(key + "_ext");
/*  70 */     if (metaDataList != null)
/*     */     {
/*  72 */       copyMetaData(descriptor, metaProps, metaDataList);
/*     */     }
/*     */ 
/*  75 */     metaDataList = (String[])(String[])metaData.get("optionalFields");
/*  76 */     if (metaDataList != null)
/*     */     {
/*  78 */       copyMetaData(descriptor, metaProps, metaDataList);
/*     */     }
/*  80 */     return metaProps;
/*     */   }
/*     */ 
/*     */   public void copyMetaData(IdcFileDescriptor source, Map target, String[] list)
/*     */   {
/*  85 */     BasicIdcFileDescriptor bDesc = (BasicIdcFileDescriptor)source;
/*  86 */     for (int i = 0; i < list.length; ++i)
/*     */     {
/*  88 */       String key = list[i];
/*  89 */       String value = bDesc.getCacheProperty(key);
/*  90 */       if (value == null)
/*     */       {
/*  92 */         value = bDesc.getProperty(key);
/*     */       }
/*  94 */       if (value == null)
/*     */         continue;
/*  96 */       target.put(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void appendMetaData(Parameters source, IdcFileDescriptor target, Map args, String[] list)
/*     */     throws DataException
/*     */   {
/* 105 */     BasicIdcFileDescriptor bTarget = null;
/* 106 */     if (!target instanceof BasicIdcFileDescriptor)
/*     */       return;
/* 108 */     bTarget = (BasicIdcFileDescriptor)target;
/* 109 */     boolean isUnmanaged = StringUtils.convertToBool(bTarget.getProperty("isUnmanaged"), false);
/*     */ 
/* 111 */     boolean isOptional = this.m_fileStore.getConfigBoolean("isOptionalField", args, false, false);
/* 112 */     boolean isLocationOnly = this.m_fileStore.getConfigBoolean("isLocationOnly", args, false, false);
/*     */ 
/* 114 */     for (int i = 0; i < list.length; ++i)
/*     */     {
/* 116 */       String key = list[i];
/* 117 */       String targetValue = target.getProperty(key);
/* 118 */       if (targetValue != null) {
/*     */         continue;
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 124 */         String value = source.get(key);
/* 125 */         if (value != null)
/*     */         {
/* 127 */           bTarget.put(key, value);
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 132 */         if (SystemUtils.m_verbose)
/*     */         {
/* 134 */           String msg = "";
/* 135 */           if (isUnmanaged)
/*     */           {
/* 137 */             msg = " unmanaged ";
/*     */           }
/* 139 */           else if (isOptional)
/*     */           {
/* 141 */             msg = " optional ";
/*     */           }
/* 143 */           if (SystemUtils.m_verbose)
/*     */           {
/* 145 */             Report.debug("filestore", "BaseFileStore.appendMetaData: value is missing for the " + msg + " key " + key, null);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 150 */         if ((!isUnmanaged) && (!isOptional) && (!isLocationOnly))
/*     */         {
/* 152 */           throw e;
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateCacheData(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*     */   {
/* 161 */     this.m_fileHelper.updateCacheData(descriptor, args);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 166 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95567 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.filesystem.BaseMetadataImplementor
 * JD-Core Version:    0.5.4
 */