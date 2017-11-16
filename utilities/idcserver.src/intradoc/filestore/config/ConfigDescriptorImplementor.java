/*     */ package intradoc.filestore.config;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.filestore.BaseFileStore;
/*     */ import intradoc.filestore.BasicIdcFileDescriptor;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.filestore.filesystem.BaseDescriptorImplementor;
/*     */ import intradoc.provider.Provider;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ConfigDescriptorImplementor extends BaseDescriptorImplementor
/*     */ {
/*     */   protected ConfigMetadataImplementor m_metadata;
/*     */   protected ServerData m_server;
/*     */   protected String[] DEFAULT_DESCRIPTOR_METADATA;
/*     */ 
/*     */   public ConfigDescriptorImplementor()
/*     */   {
/*  45 */     this.DEFAULT_DESCRIPTOR_METADATA = new String[] { "useWrapper", "useLegacyStat", "ExecutionContext", "isNew" };
/*     */   }
/*     */ 
/*     */   public void init(FileStoreProvider fs, Provider provider)
/*     */   {
/*  57 */     super.init(fs, provider);
/*  58 */     BaseFileStore bfs = (BaseFileStore)fs;
/*  59 */     this.m_metadata = ((ConfigMetadataImplementor)bfs.getImplementor("MetadataImplementor"));
/*  60 */     this.m_server = ((ServerData)provider.getProviderObject("ServerData"));
/*     */   }
/*     */ 
/*     */   public IdcFileDescriptor createDescriptor(Parameters metadata, Map args, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     String filename;
/*  82 */     if ((null == metadata) || (null == (filename = metadata.get("filename"))))
/*     */     {
/*  84 */       String msg = LocaleUtils.encodeMessage("syMissingArgument2", null, "filename", "metadata");
/*     */ 
/*  86 */       throw new DataException(msg);
/*     */     }
/*     */     String filename;
/*  89 */     BasicIdcFileDescriptor fd = new BasicIdcFileDescriptor();
/*     */ 
/*  91 */     fd.put("IntradocDir", this.m_server.getIntradocDir());
/*  92 */     fd.put("filename", filename);
/*  93 */     String pathname = this.m_server.computeActualPathname(filename);
/*  94 */     fd.put("pathname", pathname);
/*  95 */     String containerPath = FileUtils.getParent(pathname);
/*     */ 
/*  98 */     containerPath = FileUtils.directorySlashes(containerPath);
/*  99 */     fd.put("containerPath", containerPath);
/* 100 */     fd.put("ExecutionContext", context);
/*     */ 
/* 103 */     for (int i = 0; i < this.DEFAULT_DESCRIPTOR_METADATA.length; ++i)
/*     */     {
/* 105 */       String key = this.DEFAULT_DESCRIPTOR_METADATA[i];
/* 106 */       String value = null;
/* 107 */       if (null != args)
/*     */       {
/* 109 */         value = (String)args.get(key);
/*     */       }
/* 111 */       if ((null == value) || (value.length() < 1))
/*     */       {
/* 113 */         value = metadata.get(key);
/* 114 */         if ((null == value) || (value.length() < 1))
/*     */         {
/* 116 */           value = (String)this.m_fileStore.m_defaultArgs.get(key);
/*     */         }
/*     */       }
/* 119 */       if ((null == value) || (value.length() <= 0))
/*     */         continue;
/* 121 */       fd.put(key, value);
/*     */     }
/*     */ 
/* 125 */     return fd;
/*     */   }
/*     */ 
/*     */   public String getClientURL(IdcFileDescriptor descriptor, String baseURL, Map args, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/* 132 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean compareDescriptors(IdcFileDescriptor desc1, IdcFileDescriptor desc2, Map args, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/* 142 */     if ((null == desc1) || (null == desc2))
/*     */     {
/* 144 */       String msg = LocaleUtils.encodeMessage("syMissingArgument", null, "file parameter");
/* 145 */       throw new ServiceException(msg);
/*     */     }
/* 147 */     String s1 = desc1.getProperty("IntradocDir");
/* 148 */     String s2 = desc2.getProperty("IntradocDir");
/* 149 */     if ((null == s1) || (null == s2))
/*     */     {
/* 151 */       String msg = LocaleUtils.encodeMessage("syMissingArgument2", null, "IntradocDir", "file parameters");
/*     */ 
/* 153 */       throw new DataException(msg);
/*     */     }
/* 155 */     if (!s1.equals(s2))
/*     */     {
/* 157 */       return false;
/*     */     }
/* 159 */     s1 = desc1.getProperty("filename");
/* 160 */     s2 = desc2.getProperty("filename");
/* 161 */     if ((null == s1) || (null == s2))
/*     */     {
/* 163 */       String msg = LocaleUtils.encodeMessage("syMissingArgument2", null, "filename", "file parameters");
/*     */ 
/* 165 */       throw new DataException(msg);
/*     */     }
/* 167 */     boolean cmp = s1.equals(s2);
/* 168 */     return cmp;
/*     */   }
/*     */ 
/*     */   public String getFilesystemPath(IdcFileDescriptor descriptor, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/* 176 */     if (null == descriptor)
/*     */     {
/* 178 */       String msg = LocaleUtils.encodeMessage("syMissingArgument", null, "descriptor");
/*     */ 
/* 180 */       throw new DataException(msg);
/*     */     }
/* 182 */     String path = descriptor.getProperty("pathname");
/* 183 */     return path;
/*     */   }
/*     */ 
/*     */   public IdcFileDescriptor getContainer(IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException
/*     */   {
/* 191 */     this.m_metadata.validateDescriptorMetaData(descriptor, true);
/* 192 */     String parentname = descriptor.getProperty("containerPath");
/* 193 */     Properties metadata = new Properties();
/* 194 */     metadata.setProperty("filename", parentname);
/* 195 */     PropParameters params = new PropParameters(metadata);
/*     */ 
/* 198 */     ExecutionContext context = (ExecutionContext)descriptor.get("ExecutionContext");
/* 199 */     return createDescriptor(params, args, context);
/*     */   }
/*     */ 
/*     */   public String getContainerPath(IdcFileDescriptor descriptor, Map args, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/* 206 */     this.m_metadata.validateDescriptorMetaData(descriptor, true);
/* 207 */     String parentname = descriptor.getProperty("containerPath");
/* 208 */     return parentname;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 214 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.config.ConfigDescriptorImplementor
 * JD-Core Version:    0.5.4
 */