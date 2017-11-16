/*     */ package intradoc.server.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceCacheInfo;
/*     */ import intradoc.resource.ResourceCacheState;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DocStateTransition;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.server.ServiceManager;
/*     */ import intradoc.shared.ProgressState;
/*     */ import intradoc.shared.ProgressStateUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WfCompanionManager
/*     */ {
/*  37 */   public static String m_stateDir = null;
/*  38 */   public static String m_saveDir = null;
/*  39 */   protected static ProgressState m_progress = null;
/*     */ 
/*     */   public static void init()
/*     */     throws ServiceException
/*     */   {
/*  51 */     String wfDir = LegacyDirectoryLocator.getWorkflowDirectory();
/*  52 */     m_stateDir = wfDir + "states/";
/*  53 */     m_saveDir = wfDir + "saved/";
/*     */ 
/*  55 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(m_stateDir, 2, true);
/*  56 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(m_saveDir, 2, true);
/*     */ 
/*  58 */     m_progress = new ProgressState();
/*  59 */     m_progress.init("WorkflowUpdate");
/*     */   }
/*     */ 
/*     */   protected static WfCompanionData getOrCreateCompanionDataFromStorage(String name, String subDir, Workspace ws, DataBinder binder, boolean isCreate)
/*     */     throws ServiceException, DataException
/*     */   {
/*  68 */     String lookupName = name.toLowerCase();
/*  69 */     if ((((subDir == null) || (subDir.length() == 0))) && 
/*  71 */       (isCreate))
/*     */     {
/*  74 */       subDir = createSubDirectory(binder);
/*  75 */       binder.putLocal("dWfDirectory", subDir);
/*  76 */       ws.execute("UworkflowDirectory", binder);
/*     */     }
/*     */ 
/*  80 */     if (subDir == null)
/*     */     {
/*  82 */       throw new DataException("!csWfMissingCompanionDir");
/*     */     }
/*     */ 
/*  85 */     String cacheKey = createCacheKey(lookupName);
/*  86 */     long curTime = System.currentTimeMillis();
/*  87 */     ResourceCacheInfo cacheInfo = ResourceCacheState.getTemporaryCache(cacheKey, curTime);
/*  88 */     WfCompanionData wfData = null;
/*  89 */     String fileName = lookupName + ".hda";
/*  90 */     String dir = m_stateDir + subDir + "/";
/*  91 */     String filePath = dir + fileName;
/*  92 */     File file = FileUtilsCfgBuilder.getCfgFile(filePath, "Workflow", false);
/*  93 */     boolean isNew = false;
/*  94 */     long lastModified = 0L;
/*  95 */     boolean updateCache = false;
/*  96 */     if ((cacheInfo != null) && (cacheInfo.m_resourceObj instanceof WfCompanionData))
/*     */     {
/*  98 */       wfData = (WfCompanionData)cacheInfo.m_resourceObj;
/*  99 */       lastModified = file.lastModified();
/* 100 */       if (wfData.m_lastLoadedTs != lastModified)
/*     */       {
/* 102 */         isNew = true;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 107 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 1, true);
/* 108 */       isNew = true;
/*     */     }
/*     */ 
/* 111 */     if (isNew)
/*     */     {
/* 113 */       boolean fileExists = file.exists();
/*     */ 
/* 115 */       if ((fileExists) || (isCreate))
/*     */       {
/* 117 */         wfData = new WfCompanionData(name, subDir);
/*     */       }
/*     */ 
/* 120 */       if (fileExists)
/*     */       {
/* 122 */         DataBinder data = readWorkflowCompanionFile(dir, fileName);
/* 123 */         wfData.m_data = data;
/* 124 */         if (lastModified <= 0L)
/*     */         {
/* 126 */           lastModified = file.lastModified();
/*     */         }
/* 128 */         wfData.m_lastLoadedTs = lastModified;
/* 129 */         updateCache = true;
/*     */       }
/* 131 */       else if (isCreate)
/*     */       {
/* 135 */         DataBinder cmpData = new DataBinder();
/* 136 */         cmpData.putLocal("fileNotFound", "1");
/* 137 */         Vector parents = new IdcVector();
/*     */ 
/* 139 */         String wfName = binder.get("dWfName");
/* 140 */         String stepName = binder.getAllowMissing("dWfStepName");
/*     */ 
/* 142 */         String stepID = null;
/* 143 */         if (stepName == null)
/*     */         {
/* 147 */           stepID = binder.get("dWfCurrentStepID");
/*     */         }
/*     */ 
/* 151 */         ResultSet rset = ws.createResultSet("QworkflowSteps", binder);
/* 152 */         int stepIndex = ResultSetUtils.getIndexMustExist(rset, "dWfStepName");
/* 153 */         int stepIDIndex = ResultSetUtils.getIndexMustExist(rset, "dWfStepID");
/* 154 */         for (; rset.isRowPresent(); rset.next())
/*     */         {
/* 156 */           String step = rset.getStringValue(stepIndex);
/* 157 */           parents.insertElementAt(step + "@" + wfName, 0);
/*     */ 
/* 159 */           if (stepName != null)
/*     */           {
/* 161 */             if (!step.equalsIgnoreCase(stepName))
/*     */               continue;
/* 163 */             break;
/*     */           }
/*     */ 
/* 168 */           String id = rset.getStringValue(stepIDIndex);
/* 169 */           if (id.equals(stepID))
/*     */           {
/*     */             break;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 176 */         cmpData.putLocal("wfParentList", StringUtils.createString(parents, '#', '^'));
/* 177 */         wfData.m_data = cmpData;
/*     */ 
/* 179 */         writeCompanionFileToStorage(lookupName, wfData, dir, fileName, file);
/* 180 */         updateCache = true;
/*     */       }
/*     */     }
/* 183 */     if (updateCache)
/*     */     {
/* 185 */       addToTemporaryCache(cacheKey, lookupName, wfData, filePath, file, curTime);
/*     */     }
/*     */ 
/* 188 */     return wfData;
/*     */   }
/*     */ 
/*     */   static String createCacheKey(String lookupName)
/*     */   {
/* 195 */     return "wfs:/" + lookupName;
/*     */   }
/*     */ 
/*     */   protected static void addToTemporaryCache(String cacheKey, String lookupName, WfCompanionData wfData, String filePath, File file, long curTime)
/*     */   {
/* 201 */     ResourceCacheInfo cacheInfo = new ResourceCacheInfo(cacheKey, "WfCompanionData", filePath);
/* 202 */     cacheInfo.m_lastLoaded = wfData.m_lastLoadedTs;
/* 203 */     cacheInfo.m_size = file.length();
/* 204 */     cacheInfo.m_resourceObj = wfData;
/* 205 */     ResourceCacheState.addTimedTemporaryCache(cacheKey, cacheInfo, curTime);
/*     */   }
/*     */ 
/*     */   public static WfCompanionData getCompanionData(String name, String subDir)
/*     */     throws DataException, ServiceException
/*     */   {
/* 211 */     return getOrCreateCompanionDataEx(name, subDir, null, null, false);
/*     */   }
/*     */ 
/*     */   public static WfCompanionData getOrCreateCompanionData(String name, String subDir, Workspace ws, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 217 */     return getOrCreateCompanionDataEx(name, subDir, ws, binder, true);
/*     */   }
/*     */ 
/*     */   public static WfCompanionData getOrCreateCompanionDataEx(String name, String subDir, Workspace ws, DataBinder binder, boolean isDoCreate)
/*     */     throws DataException, ServiceException
/*     */   {
/* 223 */     WfCompanionData wfData = getOrCreateCompanionDataFromStorage(name, subDir, ws, binder, isDoCreate);
/* 224 */     if (wfData == null)
/*     */     {
/* 226 */       return null;
/*     */     }
/*     */ 
/* 230 */     return wfData.shallowClone();
/*     */   }
/*     */ 
/*     */   protected static DataBinder readWorkflowCompanionFile(String dir, String fileName)
/*     */     throws ServiceException
/*     */   {
/* 236 */     DataBinder data = new DataBinder();
/*     */ 
/* 238 */     boolean readFile = ResourceUtils.serializeDataBinder(dir, fileName, data, false, false);
/*     */ 
/* 240 */     data.putLocal("fileNotFound", (readFile) ? "" : "1");
/*     */ 
/* 242 */     WorkflowUtils.makeCompatibleWithLatestVersion(data, "wfcompanion");
/* 243 */     return data;
/*     */   }
/*     */ 
/*     */   protected static boolean writeCompanionFileToStorage(String lookupName, WfCompanionData wfData, String dir, String fileName, File file)
/*     */     throws ServiceException
/*     */   {
/* 249 */     boolean fileNotFound = DataBinderUtils.getLocalBoolean(wfData.m_data, "fileNotFound", false);
/* 250 */     int flags = 1;
/* 251 */     if (fileNotFound)
/*     */     {
/* 253 */       flags |= 8;
/*     */     }
/* 255 */     boolean result = ResourceUtils.serializeDataBinderWithEncoding(dir, fileName, wfData.m_data, flags, null);
/*     */ 
/* 259 */     if (result)
/*     */     {
/* 261 */       wfData.m_data.putLocal("fileNotFound", "");
/* 262 */       wfData.m_lastLoadedTs = file.lastModified();
/*     */     }
/* 264 */     return result;
/*     */   }
/*     */ 
/*     */   public static void writeCompanionFile(WfCompanionData wfData) throws ServiceException
/*     */   {
/* 269 */     String lookupName = wfData.m_docName.toLowerCase();
/* 270 */     String dir = m_stateDir + wfData.m_subDir + "/";
/* 271 */     String fileName = lookupName + ".hda";
/* 272 */     File file = FileUtilsCfgBuilder.getCfgFile(dir + fileName, "", false);
/* 273 */     boolean result = writeCompanionFileToStorage(lookupName, wfData, dir, fileName, file);
/*     */ 
/* 275 */     if (!result)
/*     */       return;
/* 277 */     long curTime = System.currentTimeMillis();
/* 278 */     String cacheKey = createCacheKey(lookupName);
/* 279 */     addToTemporaryCache(cacheKey, lookupName, wfData, dir + fileName, file, curTime);
/*     */   }
/*     */ 
/*     */   public static void deleteCompanionFile(String docName, String subDir, DataBinder binder, boolean isInTransaction)
/*     */   {
/* 286 */     String lookupName = docName.toLowerCase();
/* 287 */     ResourceCacheInfo cacheInfo = ResourceCacheState.removeTemporaryCache(lookupName);
/* 288 */     WfCompanionData wfData = null;
/* 289 */     if ((cacheInfo != null) && (cacheInfo.m_resourceObj instanceof WfCompanionData))
/*     */     {
/* 291 */       wfData = (WfCompanionData)cacheInfo.m_resourceObj;
/*     */     }
/*     */ 
/* 294 */     String filepath = null;
/* 295 */     if (wfData != null)
/*     */     {
/* 297 */       filepath = m_stateDir + wfData.m_subDir + "/" + lookupName + ".hda";
/*     */     }
/*     */     else
/*     */     {
/* 301 */       filepath = m_stateDir + subDir + "/" + lookupName + ".hda";
/*     */     }
/*     */ 
/* 304 */     if (isInTransaction)
/*     */     {
/* 308 */       binder.addTempFile(filepath);
/*     */     }
/*     */     else
/*     */     {
/* 312 */       FileUtils.deleteFile(filepath);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void removeCompanionFile(String docName, String subDir, DataBinder binder, boolean isInTransaction)
/*     */   {
/* 319 */     String lookupName = docName.toLowerCase();
/* 320 */     String filePath = m_stateDir + subDir + "/" + lookupName + ".hda";
/*     */ 
/* 322 */     boolean isSave = SharedObjects.getEnvValueAsBoolean("IsSaveWfCompanionFiles", false);
/* 323 */     if (isSave)
/*     */     {
/*     */       try
/*     */       {
/* 328 */         String toDir = m_saveDir + subDir;
/* 329 */         FileUtils.checkOrCreateDirectory(toDir, 2);
/*     */ 
/* 331 */         String toPath = toDir + "/" + lookupName + ".hda";
/*     */ 
/* 333 */         FileUtils.copyFile(filePath, toPath);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 337 */         Report.error(null, e, "csWfErrorSavingCompanionFile", new Object[] { docName });
/*     */       }
/*     */     }
/*     */ 
/* 341 */     deleteCompanionFile(docName, subDir, binder, isInTransaction);
/*     */   }
/*     */ 
/*     */   public static String createSubDirectory(DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 347 */     String group = binder.get("dSecurityGroup");
/* 348 */     return group.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static void updateWorkflowItems(Workspace ws, String action)
/*     */     throws DataException, ServiceException
/*     */   {
/* 361 */     updateWorkflowItemsEx(ws, "QwfActiveDocs", action, null, null);
/*     */   }
/*     */ 
/*     */   public static void updateWorkflowItemsEx(Workspace ws, String wfDocsQuery, String wfAction, String filterKey, ResultSetFilter filter)
/*     */     throws DataException, ServiceException
/*     */   {
/* 368 */     ResultSet rset = ws.createResultSet(wfDocsQuery, null);
/* 369 */     DataResultSet drset = new DataResultSet();
/* 370 */     drset.copy(rset);
/*     */ 
/* 372 */     ws.releaseConnection();
/*     */ 
/* 375 */     String workflowDir = LegacyDirectoryLocator.getWorkflowDirectory();
/*     */     try
/*     */     {
/* 378 */       FileUtils.testFileSystem(workflowDir);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 382 */       throw new DataException("!csWfFailedWorkflowDirectoryTest", e);
/*     */     }
/*     */ 
/* 385 */     ProgressStateUtils.reportProgress(m_progress, null, "workflow", 4, null, "csWfUpdateStart", new Object[0]);
/*     */     try
/*     */     {
/* 390 */       DataBinder workBinder = new DataBinder();
/* 391 */       workBinder.addResultSet("ActiveDocs", drset);
/* 392 */       for (; drset.isRowPresent(); drset.next())
/*     */       {
/* 394 */         DataBinder binder = new DataBinder();
/* 395 */         binder.setEnvironment(new Properties(SharedObjects.getSecureEnvironment()));
/* 396 */         Service cxt = createService(binder, ws);
/*     */         try
/*     */         {
/* 399 */           cxt.beginTransaction();
/* 400 */           updateWorkflowItem(ws, wfAction, filterKey, filter, binder, cxt, workBinder);
/* 401 */           cxt.commitTransaction();
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 405 */           cxt.rollbackTransaction();
/* 406 */           ProgressStateUtils.reportError(m_progress, null, t, "csWfErrorUpdatingScript", new Object[] { binder.getAllowMissing("dDocName"), binder.getAllowMissing("dWfStepName"), binder.getAllowMissing("dWfName") });
/*     */         }
/*     */         finally
/*     */         {
/* 412 */           ws.releaseConnection();
/* 413 */           cxt.clear();
/*     */         }
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 419 */       ProgressStateUtils.reportProgress(m_progress, null, "workflow", 2, null, "csWfUpdateFinished", new Object[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void updateWorkflowItem(Workspace ws, String wfAction, String filterKey, ResultSetFilter filter, DataBinder binder, ExecutionContext cxt, DataBinder workBinder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 428 */     ResultSet rset = ws.createResultSet("QwfActiveDocInfo", workBinder);
/* 429 */     if (rset.isEmpty())
/*     */     {
/* 431 */       return;
/*     */     }
/*     */ 
/* 434 */     DataResultSet workSet = new DataResultSet();
/* 435 */     if ((filter != null) && (filterKey != null))
/*     */     {
/* 438 */       workSet.copyFiltered(rset, filterKey, filter);
/* 439 */       if (workSet.isEmpty())
/*     */       {
/* 441 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 447 */       workSet.copy(rset, 1);
/*     */     }
/*     */ 
/* 450 */     binder.putLocal("wfAction", wfAction);
/* 451 */     binder.putLocal("isFinished", "0");
/* 452 */     binder.addResultSet("WF_DOC_INFO", workSet);
/*     */ 
/* 454 */     m_progress.updateState(binder);
/*     */ 
/* 456 */     DocStateTransition.advanceDocumentState(binder, ws, false, false, false, true, cxt);
/*     */ 
/* 461 */     if (!DataBinderUtils.getLocalBoolean(binder, "isStatusChanged", false))
/*     */       return;
/* 463 */     ws.execute("UrevisionWorkflowState", binder);
/*     */   }
/*     */ 
/*     */   public static Service createService(DataBinder binder, Workspace ws)
/*     */   {
/* 470 */     Service service = null;
/*     */     try
/*     */     {
/* 473 */       ServiceData serviceData = new ServiceData();
/* 474 */       serviceData.m_subjects = new Vector();
/* 475 */       serviceData.m_errorMsg = "!csWfActivityError";
/* 476 */       service = ServiceManager.createService("WorkflowService", ws, null, binder, serviceData);
/*     */ 
/* 478 */       service.initDelegatedObjects();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 484 */       Report.error(null, e, "csWfContextError", new Object[0]);
/* 485 */       return null;
/*     */     }
/* 487 */     service.setCachedObject("IsOutOfContext", "1");
/* 488 */     return service;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 493 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WfCompanionManager
 * JD-Core Version:    0.5.4
 */