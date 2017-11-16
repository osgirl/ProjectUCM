/*     */ package intradoc.filestore;
/*     */ 
/*     */ import intradoc.common.DataStreamWrapper;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class FileStoreUtils
/*     */ {
/*     */   public static boolean isStoredOnFileSystem(IdcFileDescriptor descriptor, FileStoreProvider fileStore)
/*     */   {
/*  40 */     boolean result = fileStore.hasFeature(1);
/*  41 */     if (!result)
/*     */     {
/*  43 */       Object[] args = { descriptor, fileStore };
/*  44 */       ExecutionContext cxt = new ExecutionContextAdaptor();
/*  45 */       cxt.setCachedObject("isStoredOnFileSystem:parameters", args);
/*     */       try
/*     */       {
/*  48 */         if (PluginFilters.filter("isStoredOnFileSystem", null, null, cxt) == 1)
/*     */         {
/*  51 */           String oResult = (String)cxt.getReturnValue();
/*  52 */           if (oResult != null)
/*     */           {
/*  54 */             result = StringUtils.convertToBool(oResult, false);
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*  60 */         Report.trace("filestore", "Unable to compute if file is stored on the file system.", e);
/*     */       }
/*     */       finally
/*     */       {
/*  65 */         clearPageMerger(cxt);
/*     */       }
/*     */     }
/*  68 */     return result;
/*     */   }
/*     */ 
/*     */   public static void forceDownloadStreamToFilePath(DataStreamWrapper streamWrapper, FileStoreProvider fileStore, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/*  78 */       if ((!streamWrapper.m_isSimpleFileStream) && (streamWrapper.m_descriptor != null) && (streamWrapper.m_descriptor instanceof IdcFileDescriptor))
/*     */       {
/*  81 */         IdcFileDescriptor descriptor = (IdcFileDescriptor)streamWrapper.m_descriptor;
/*  82 */         fileStore.forceToFilesystemPath(descriptor, null, cxt);
/*  83 */         streamWrapper.m_filePath = fileStore.getFilesystemPath(descriptor, cxt);
/*     */       }
/*  85 */       streamWrapper.m_isSimpleFileStream = true;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  89 */       throw new ServiceException(-25, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void updateMetaData(String field, String val, ExecutionContext cxt)
/*     */   {
/*  95 */     IdcDescriptorState states = null;
/*  96 */     if (!cxt instanceof Service)
/*     */       return;
/*  98 */     states = (IdcDescriptorState)cxt.getCachedObject("DescriptorStates");
/*  99 */     if (states == null)
/*     */       return;
/* 101 */     Map map = new HashMap();
/* 102 */     map.put(field, val);
/* 103 */     states.updateMetaData(map, cxt);
/*     */   }
/*     */ 
/*     */   public static boolean isUpdatedMetaData(String field, ExecutionContext cxt)
/*     */   {
/* 110 */     IdcDescriptorState states = null;
/* 111 */     if (cxt instanceof Service)
/*     */     {
/* 113 */       states = (IdcDescriptorState)cxt.getCachedObject("DescriptorStates");
/* 114 */       if (states != null)
/*     */       {
/* 116 */         return states.isUpdatedMetaData(field);
/*     */       }
/*     */     }
/* 119 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean isSetMetaData(String field, ExecutionContext cxt)
/*     */   {
/* 124 */     IdcDescriptorState states = null;
/* 125 */     if (cxt instanceof Service)
/*     */     {
/* 127 */       states = (IdcDescriptorState)cxt.getCachedObject("DescriptorStates");
/* 128 */       if (states != null)
/*     */       {
/* 130 */         return states.isSetMetaData(field);
/*     */       }
/*     */     }
/* 133 */     return false;
/*     */   }
/*     */ 
/*     */   public static void addActionToCommitLog(String action, String path, Map args, String providerName, ExecutionContext cxt)
/*     */   {
/* 139 */     ArrayList transList = (ArrayList)cxt.getCachedObject("FileStore:" + providerName + ":commitLog");
/*     */ 
/* 141 */     if (transList == null)
/*     */       return;
/* 143 */     Report.trace("filestore", "adding " + action + " operation to commit log path=" + path, null);
/*     */ 
/* 145 */     List l = new ArrayList();
/* 146 */     l.add(action);
/* 147 */     l.add(path);
/* 148 */     l.add(null);
/* 149 */     l.add(args);
/* 150 */     transList.add(l);
/*     */   }
/*     */ 
/*     */   public static void addActionToCommitLog(String action, IdcFileDescriptor desc, Map args, String providerName, ExecutionContext cxt)
/*     */   {
/* 157 */     ArrayList commitLog = (ArrayList)cxt.getCachedObject("FileStore:" + providerName + ":commitLog");
/*     */ 
/* 159 */     if (commitLog == null)
/*     */       return;
/* 161 */     Report.trace("filestore", "adding " + action + " operation to commit log", null);
/* 162 */     List l = new ArrayList();
/* 163 */     l.add(action);
/* 164 */     l.add(desc);
/* 165 */     l.add(null);
/* 166 */     l.add(args);
/* 167 */     commitLog.add(l);
/*     */   }
/*     */ 
/*     */   public static void addActionToRollbackLog(String action, String path, String altPath, Map args, String providerName, ExecutionContext cxt)
/*     */   {
/* 174 */     ArrayList rollbackLog = (ArrayList)cxt.getCachedObject("FileStore:" + providerName + ":rollbackLog");
/*     */ 
/* 176 */     if (rollbackLog == null)
/*     */       return;
/* 178 */     Report.trace("filestore", "adding " + action + " operation to rollback log with " + "path=" + path, null);
/*     */ 
/* 180 */     List l = new ArrayList();
/* 181 */     l.add(action);
/* 182 */     l.add(path);
/* 183 */     l.add(altPath);
/* 184 */     l.add(args);
/* 185 */     rollbackLog.add(l);
/*     */   }
/*     */ 
/*     */   public static void addActionToRollbackLog(String action, IdcFileDescriptor target, IdcFileDescriptor source, Map args, String providerName, ExecutionContext cxt)
/*     */   {
/* 192 */     ArrayList rollbackLog = (ArrayList)cxt.getCachedObject("FileStore:" + providerName + ":rollbackLog");
/*     */ 
/* 194 */     if (rollbackLog == null)
/*     */       return;
/* 196 */     Report.trace("filestore", "adding " + action + " operation to rollback log.", null);
/* 197 */     List l = new ArrayList();
/* 198 */     l.add(action);
/* 199 */     l.add(target);
/* 200 */     l.add(source);
/* 201 */     l.add(args);
/* 202 */     rollbackLog.add(l);
/*     */   }
/*     */ 
/*     */   public static PageMerger getPageMerger(ExecutionContext context)
/*     */   {
/* 208 */     PageMerger merger = null;
/* 209 */     DataBinder binder = null;
/* 210 */     String logMsg = null;
/* 211 */     if (context == null)
/*     */     {
/* 213 */       context = new ExecutionContextAdaptor();
/*     */     }
/*     */     else
/*     */     {
/* 217 */       String str = "FileStoreProviderPageMerger";
/* 218 */       merger = (PageMerger)context.getCachedObject("FileStoreProviderPageMerger");
/* 219 */       if (merger != null)
/*     */       {
/* 221 */         logMsg = "FileSystemUtils.getPageMerger: " + str + " Merger = " + merger;
/*     */       }
/*     */     }
/* 224 */     if (merger == null)
/*     */     {
/* 226 */       Parameters params = (Parameters)context.getCachedObject("FileParameters");
/* 227 */       if (params != null)
/*     */       {
/* 231 */         binder = DataBinderUtils.createBinderFromParameters(params, context);
/* 232 */         if (binder.m_blDateFormat != null)
/*     */         {
/* 234 */           context.setCachedObject("UserDateFormat", binder.m_blDateFormat);
/*     */         }
/*     */ 
/* 237 */         logMsg = "FileSystemUtils.getPageMerger: createBinderFromParameters";
/*     */       }
/*     */       else
/*     */       {
/* 241 */         binder = (DataBinder)context.getCachedObject("DataBinder");
/* 242 */         if (binder == null)
/*     */         {
/* 244 */           binder = new DataBinder();
/*     */         }
/*     */       }
/*     */     }
/* 248 */     if ((SystemUtils.m_verbose) && (logMsg != null))
/*     */     {
/* 250 */       Report.debug("filestore", logMsg, null);
/*     */     }
/* 252 */     if (merger == null)
/*     */     {
/*     */       try
/*     */       {
/* 257 */         merger = new PageMerger();
/* 258 */         merger.initImplementProtectContext(binder, context);
/* 259 */         context.setCachedObject("FileStoreProviderPageMerger", merger);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 263 */         Report.trace("filestore", "Unable to create new page merger for filestore.", e);
/*     */       }
/*     */     }
/*     */ 
/* 267 */     return merger;
/*     */   }
/*     */ 
/*     */   public static void clearPageMerger(ExecutionContext context)
/*     */   {
/* 272 */     PageMerger merger = (PageMerger)context.getCachedObject("FileStoreProviderPageMerger");
/*     */ 
/* 274 */     if (merger == null)
/*     */       return;
/* 276 */     merger.releaseAllTemporary();
/* 277 */     context.setCachedObject("FileStoreProviderPageMerger", null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 283 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70600 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FileStoreUtils
 * JD-Core Version:    0.5.4
 */