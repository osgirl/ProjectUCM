/*     */ package intradoc.filestore;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.filesystem.WebLocationParser;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.utils.FileRevisionSelectionUtils;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class FileStoreProviderHelper
/*     */ {
/*     */   public FileStoreProvider m_fileStore;
/*     */   public ExecutionContext m_context;
/*     */ 
/*     */   public FileStoreProviderHelper()
/*     */   {
/*  34 */     this.m_fileStore = null;
/*  35 */     this.m_context = null;
/*     */   }
/*     */ 
/*     */   public static FileStoreProviderHelper getFileStoreProviderUtils(FileStoreProvider provider, ExecutionContext context) throws ServiceException
/*     */   {
/*  40 */     FileStoreProviderHelper utils = (FileStoreProviderHelper)ComponentClassFactory.createClassInstance("FileStoreProviderHelper", "intradoc.filestore.FileStoreProviderHelper", null);
/*     */ 
/*  43 */     utils.init(provider, context);
/*  44 */     return utils;
/*     */   }
/*     */ 
/*     */   public void init(FileStoreProvider provider, ExecutionContext context)
/*     */   {
/*  49 */     this.m_fileStore = provider;
/*  50 */     this.m_context = context;
/*     */   }
/*     */ 
/*     */   public boolean isLegacyFileStore()
/*     */   {
/*  61 */     return ((BaseFileStore)this.m_fileStore).getConfigBoolean("IsAllowConfigSystemProvider", null, false, true);
/*     */   }
/*     */ 
/*     */   public Object getCapability(String capability, IdcFileDescriptor descriptor1, IdcFileDescriptor descriptor2)
/*     */     throws DataException
/*     */   {
/*     */     Map caps;
/*     */     Map caps;
/*  69 */     if (descriptor2 != null)
/*     */     {
/*  71 */       caps = this.m_fileStore.getCapabilities(descriptor1, descriptor2);
/*     */     }
/*     */     else
/*     */     {
/*  75 */       caps = this.m_fileStore.getCapabilities(descriptor1);
/*     */     }
/*  77 */     Object result = caps.get(capability);
/*  78 */     return result;
/*     */   }
/*     */ 
/*     */   public IdcFileDescriptor createDescriptorForRendition(Parameters data, String rendition)
/*     */     throws DataException, ServiceException
/*     */   {
/*  93 */     Properties myProps = new Properties();
/*  94 */     Parameters params = new MapParameters(myProps, data);
/*  95 */     myProps.put("RenditionId", rendition);
/*  96 */     IdcFileDescriptor descr = this.m_fileStore.createDescriptor(params, null, this.m_context);
/*  97 */     return descr;
/*     */   }
/*     */ 
/*     */   public String computeRenditionPath(DataBinder params, String rendition, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 103 */     params.putLocal("RenditionId", rendition);
/* 104 */     IdcFileDescriptor d = this.m_fileStore.createDescriptor(params, null, this.m_context);
/*     */ 
/* 108 */     boolean isPathOnly = DataBinderUtils.getBoolean(params, "isProvisionalPathOnly", false);
/* 109 */     String path = null;
/* 110 */     if (isPathOnly)
/*     */     {
/* 112 */       path = d.getProperty("path");
/*     */     }
/*     */     else
/*     */     {
/* 116 */       path = this.m_fileStore.getFilesystemPathWithArgs(d, null, cxt);
/*     */     }
/* 118 */     return path;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public boolean fileExists(IdcFileDescriptor descriptor)
/*     */     throws DataException, ServiceException
/*     */   {
/* 134 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 135 */     return fileExists(descriptor, cxt);
/*     */   }
/*     */ 
/*     */   public boolean fileExists(IdcFileDescriptor descriptor, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     Map storageData;
/*     */     try
/*     */     {
/* 144 */       storageData = this.m_fileStore.getStorageData(descriptor, null, cxt);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 148 */       throw new ServiceException(e);
/*     */     }
/* 150 */     String exists = (String)storageData.get("fileExists");
/* 151 */     return StringUtils.convertToBool(exists, false);
/*     */   }
/*     */ 
/*     */   public void deleteFile(IdcFileDescriptor descriptor, Map map, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 159 */       this.m_fileStore.deleteFile(descriptor, map, cxt);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 163 */       throw new DataException(e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean parseDocInfoFromInternalPath(String relativePath, Map localData, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 179 */     boolean result = false;
/* 180 */     Object obj = this.m_fileStore.getImplementor("WebLocationParser");
/* 181 */     if (obj instanceof WebLocationParser)
/*     */     {
/* 184 */       int qIndex = relativePath.indexOf(63);
/* 185 */       if (qIndex >= 0)
/*     */       {
/* 187 */         String queryStr = relativePath.substring(qIndex + 1);
/* 188 */         relativePath = relativePath.substring(0, qIndex);
/* 189 */         DataBinder parsedQuery = new DataBinder();
/* 190 */         DataSerializeUtils.parseLocalParameters(parsedQuery, queryStr, "&", null);
/* 191 */         String idcService = parsedQuery.getLocal("IdcService");
/* 192 */         if (idcService != null)
/*     */         {
/* 194 */           String dId = parsedQuery.getAllowMissing("dID");
/* 195 */           if ((dId != null) && (dId.length() > 0))
/*     */           {
/* 197 */             Workspace ws = (Workspace)cxt.getCachedObject("Workspace");
/* 198 */             ResultSet rset = ws.createResultSet("QdocID", parsedQuery);
/* 199 */             DataResultSet drset = new DataResultSet();
/* 200 */             drset.copy(rset);
/* 201 */             if (drset.isRowPresent())
/*     */             {
/* 203 */               Map mp = drset.getCurrentRowMap();
/* 204 */               localData.putAll(mp);
/* 205 */               result = true;
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/* 210 */       if (!result)
/*     */       {
/* 212 */         WebLocationParser parser = (WebLocationParser)obj;
/* 213 */         result = parser.parseDocInfoFromPath(relativePath, localData, cxt);
/*     */       }
/*     */     }
/* 216 */     return result;
/*     */   }
/*     */ 
/*     */   public DataResultSet createFileReference(Properties props, DataBinder binder, Workspace ws, ExecutionContext cxt, boolean isForce)
/*     */     throws DataException, ServiceException
/*     */   {
/* 223 */     DataResultSet drset = null;
/* 224 */     MapParameters params = new MapParameters(props);
/* 225 */     String pathTail = props.getProperty("pathTail");
/* 226 */     String docName = props.getProperty("dDocName");
/* 227 */     String conversionPathSuffix = props.getProperty("conversionPathSuffix");
/* 228 */     boolean isContainerOnlyComputation = (conversionPathSuffix != null) && (conversionPathSuffix.length() > 0);
/*     */ 
/* 230 */     boolean isPathOnlyComputation = (isContainerOnlyComputation) || ((pathTail != null) && (pathTail.length() > 0));
/*     */ 
/* 232 */     Service service = null;
/* 233 */     if (cxt instanceof Service)
/*     */     {
/* 235 */       service = (Service)cxt;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 241 */       DataBinder wrapperBinder = binder.createShallowCopy();
/* 242 */       wrapperBinder.setLocalData(props);
/*     */ 
/* 245 */       if ((service != null) && (service.isConditionVarTrue("UseExistingDocInfo")))
/*     */       {
/* 247 */         DataResultSet curDocInfo = (DataResultSet)binder.getResultSet("DOC_INFO");
/* 248 */         if ((curDocInfo != null) && (curDocInfo.isRowPresent()) && (docName != null))
/*     */         {
/* 250 */           String curDocName = curDocInfo.getStringValueByName("dDocName");
/* 251 */           if (curDocName.equalsIgnoreCase(docName))
/*     */           {
/* 253 */             drset = curDocInfo;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 258 */       if (drset == null)
/*     */       {
/* 260 */         drset = FileRevisionSelectionUtils.loadAdditionalDocInfo("DOC_INFO", wrapperBinder, cxt, ws);
/*     */       }
/* 262 */       if ((drset != null) && (drset.isRowPresent()))
/*     */       {
/* 265 */         params.m_map = drset.getCurrentRowMap();
/* 266 */         String rendition = "webViewableFile";
/* 267 */         String renFlag = props.getProperty("renFlag");
/* 268 */         if (renFlag != null)
/*     */         {
/* 270 */           rendition = "rendition:" + renFlag;
/*     */         }
/* 272 */         params.m_map.put("RenditionId", rendition);
/* 273 */         Map args = new HashMap();
/* 274 */         if (isContainerOnlyComputation)
/*     */         {
/* 276 */           args.put("isContainer", "1");
/* 277 */           args.put("forceNoLink", "1");
/*     */         }
/*     */ 
/* 280 */         IdcFileDescriptor descriptor = this.m_fileStore.createDescriptor(params, args, cxt);
/*     */ 
/* 282 */         cxt.setCachedObject("ComputedDescriptorRef", descriptor);
/* 283 */         if (isForce)
/*     */         {
/* 286 */           args.put("isNew", "1");
/* 287 */           this.m_fileStore.getFilesystemPathWithArgs(descriptor, args, cxt);
/*     */         }
/* 290 */         else if (service != null)
/*     */         {
/* 292 */           if (isPathOnlyComputation)
/*     */           {
/* 294 */             String path = descriptor.getProperty("path");
/* 295 */             if (path == null)
/*     */             {
/* 297 */               return null;
/*     */             }
/* 299 */             IdcStringBuilder fullPath = new IdcStringBuilder(path);
/* 300 */             if (!path.endsWith("/"))
/*     */             {
/* 302 */               fullPath.append("/");
/*     */             }
/* 304 */             if (isContainerOnlyComputation)
/*     */             {
/* 306 */               fullPath.append(conversionPathSuffix);
/*     */             }
/*     */             else
/*     */             {
/* 310 */               fullPath.append(pathTail);
/*     */             }
/* 312 */             String fullPathStr = fullPath.toString();
/* 313 */             service.setFile(fullPathStr);
/* 314 */             props.setProperty("computedFilePath", fullPathStr);
/*     */           }
/*     */           else
/*     */           {
/* 318 */             service.setDescriptor(descriptor);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 327 */       Report.trace("system", "Unable to retrieve file information for " + props, e);
/*     */     }
/*     */ 
/* 330 */     return drset;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 335 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81937 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FileStoreProviderHelper
 * JD-Core Version:    0.5.4
 */