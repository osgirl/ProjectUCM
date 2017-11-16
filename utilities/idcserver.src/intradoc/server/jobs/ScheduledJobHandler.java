/*     */ package intradoc.server.jobs;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SortUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHandler;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class ScheduledJobHandler extends ServiceHandler
/*     */ {
/*     */   public void init(Service service)
/*     */     throws ServiceException, DataException
/*     */   {
/*  32 */     super.init(service);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateAfterProcessing() throws DataException, ServiceException
/*     */   {
/*  38 */     JobState jState = (JobState)this.m_service.getCachedObject("ScheduledJobState");
/*  39 */     if (jState == null)
/*     */     {
/*  41 */       throw new DataException("!csSjMissingJobState");
/*     */     }
/*     */ 
/*  44 */     updateEntry(jState);
/*     */   }
/*     */ 
/*     */   public void updateEntry(JobState jState)
/*     */     throws DataException, ServiceException
/*     */   {
/*  70 */     DataBinder binder = jState.m_data;
/*  71 */     binder.putLocal("dSjExceptionJob", "");
/*  72 */     binder.putLocal("dSjState", "I");
/*     */ 
/*  74 */     String type = binder.getLocal("dSjType");
/*  75 */     boolean isRepeat = type.equals("R");
/*     */ 
/*  77 */     ScheduledJobStorage jStorage = ScheduledJobManager.getStorage(this.m_workspace);
/*  78 */     ScheduledJobImplementor jobImp = (ScheduledJobImplementor)this.m_service.getCachedObject("ScheduledJobImplementor");
/*     */ 
/*  80 */     jobImp.buildResult();
/*     */ 
/*  82 */     boolean isUpdate = determineUpdate(binder);
/*  83 */     if (jState.m_isFatalError)
/*     */     {
/*  85 */       isUpdate = true;
/*     */ 
/*  87 */       String msg = null;
/*  88 */       if (jState.m_finalException instanceof NullPointerException)
/*     */       {
/*  90 */         msg = "null";
/*     */       }
/*     */       else
/*     */       {
/*  94 */         msg = jState.m_finalException.getMessage();
/*     */       }
/*  96 */       binder.putLocal("dSjProgress", msg);
/*  97 */       binder.putLocal("dSjState", "P");
/*  98 */       binder.putLocal("sjException", msg);
/*     */     }
/* 100 */     else if (isUpdate)
/*     */     {
/* 103 */       if (!isRepeat)
/*     */       {
/* 105 */         this.m_binder.putLocal("dSjState", "P");
/*     */       }
/* 107 */       DataResultSet jobSet = (DataResultSet)binder.getResultSet("JobParameters");
/* 108 */       DataResultSet exceptionSet = jState.m_exceptionSet;
/* 109 */       if ((jobSet != null) && (exceptionSet != null))
/*     */       {
/* 111 */         jobSet.mergeFields(exceptionSet);
/* 112 */         jobSet.merge("jobItemID", exceptionSet, false);
/*     */       }
/* 114 */       if (exceptionSet != null)
/*     */       {
/* 116 */         binder.addResultSet("JobExceptions", exceptionSet);
/*     */       }
/*     */     }
/* 119 */     else if (jState.m_isInError)
/*     */     {
/* 122 */       createExceptionJob(jState, jStorage);
/*     */     }
/*     */ 
/* 126 */     updateJobHistory(jState, binder, jStorage);
/*     */ 
/* 128 */     if ((isUpdate) || (isRepeat))
/*     */     {
/* 130 */       jStorage.updateTask(binder, jState, this.m_workspace, this.m_service);
/*     */     }
/*     */     else
/*     */     {
/* 136 */       jStorage.deleteJob(jState, true, this.m_workspace, this.m_service);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateJobHistory(JobState jState, DataBinder binder, ScheduledJobStorage jStorage)
/*     */   {
/*     */     try
/*     */     {
/* 147 */       String msg = jState.createFinalProgressMessage();
/* 148 */       binder.putLocal("dSjMessage", msg);
/*     */ 
/* 150 */       ScheduledJobUtils.addJobHistoryEvent(binder, this.m_workspace, this.m_service);
/* 151 */       this.m_service.executeFilter("updateJobHistory");
/*     */ 
/* 154 */       jStorage.saveDefinitionInHistory(jState);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 158 */       Report.error("scheduledjobs", e, "csSjUnableToUpdateHistory", new Object[] { jState.m_data.getLocal("dSjName") });
/*     */ 
/* 160 */       jState.reportProgress(-1, e, "csSjUnableToUpdateHistory", new Object[] { jState.m_data.getLocal("dSjName") });
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean determineUpdate(DataBinder binder)
/*     */   {
/* 167 */     boolean isUpdate = false;
/* 168 */     if (!isUpdate)
/*     */     {
/* 170 */       isUpdate = ScheduledJobUtils.getConfigBoolean("SjUpdateOnProcess", binder, false, true);
/*     */     }
/* 172 */     return isUpdate;
/*     */   }
/*     */ 
/*     */   public void createExceptionJob(JobState jState, ScheduledJobStorage jStorage)
/*     */     throws DataException, ServiceException
/*     */   {
/* 180 */     DataBinder expBinder = new DataBinder();
/* 181 */     DataBinder.mergeHashTables(expBinder.getLocalData(), jState.m_data.getLocalData());
/* 182 */     String name = expBinder.getLocal("dSjName");
/* 183 */     expBinder.removeLocal("dSjName");
/*     */ 
/* 186 */     expBinder.putLocal("dSjExceptionParent", name);
/* 187 */     expBinder.putLocal("dSjState", "E");
/* 188 */     expBinder.putLocal("dSjType", "O");
/* 189 */     expBinder.addResultSet("JobParameters", jState.m_exceptionSet);
/* 190 */     if (jState.m_finalException != null)
/*     */     {
/* 192 */       expBinder.putLocal("dSjProgress", jState.m_finalException.getMessage());
/*     */     }
/*     */ 
/* 195 */     jStorage.addTask(expBinder, this.m_workspace, this.m_service);
/*     */ 
/* 198 */     name = expBinder.getLocal("dSjName");
/* 199 */     jState.m_data.putLocal("dSjExceptionJob", name);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void retrieveJobInfo() throws DataException, ServiceException
/*     */   {
/* 205 */     ScheduledJobStorage jStorage = ScheduledJobManager.getStorage(this.m_workspace);
/*     */ 
/* 207 */     String id = ScheduledJobUtils.getIdFromBinder(this.m_binder);
/* 208 */     String dir = jStorage.getActiveDir();
/* 209 */     DataBinder binder = ResourceUtils.readDataBinder(dir, id + ".hda");
/* 210 */     this.m_binder.merge(binder);
/*     */ 
/* 216 */     String startToken = this.m_binder.get("dSjStartToken");
/* 217 */     if ((startToken != null) && (startToken.length() > 0) && (!startToken.startsWith("@")))
/*     */     {
/* 219 */       Date date = this.m_binder.parseDate("dSjCreateTs", startToken);
/* 220 */       this.m_binder.putLocalDate("dSjStartToken", date);
/*     */     }
/*     */ 
/* 223 */     this.m_binder.setFieldType("dSjProgress", "message");
/* 224 */     this.m_binder.setFieldType("sjException", "message");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void retrieveJobHistoryInfo() throws DataException, ServiceException
/*     */   {
/* 230 */     String id = null;
/*     */     try
/*     */     {
/* 233 */       ScheduledJobStorage jStorage = ScheduledJobManager.getStorage(this.m_workspace);
/*     */ 
/* 235 */       id = ScheduledJobUtils.getHistoryIdFromBinder(this.m_binder);
/* 236 */       String dir = jStorage.getHistoryDir();
/*     */ 
/* 238 */       DataBinder data = new DataBinder();
/* 239 */       boolean defExists = ResourceUtils.serializeDataBinder(dir, id + ".hda", data, false, false);
/*     */ 
/* 242 */       if (defExists)
/*     */       {
/* 244 */         this.m_binder.mergeResultSets(data);
/*     */       }
/*     */ 
/* 247 */       this.m_binder.setFieldType("dSjProgress", "message");
/* 248 */       this.m_binder.setFieldType("sjException", "message");
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 253 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 255 */       String name = this.m_binder.getLocal("dSjName");
/* 256 */       Report.debug("scheduledjobs", "The history file retrieval for " + id + " and name " + name + " is in error.", t);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void changeJobState()
/*     */     throws DataException, ServiceException
/*     */   {
/* 265 */     String oldState = this.m_binder.get("dSjState");
/* 266 */     String newState = this.m_binder.get("sjState").trim();
/* 267 */     String jobName = this.m_binder.getLocal("dSjName");
/* 268 */     String state = null;
/* 269 */     if (newState.equals("resubmit"))
/*     */     {
/* 271 */       if (ScheduledJobUtils.isActiveJob(oldState))
/*     */       {
/* 273 */         String errMsg = LocaleUtils.encodeMessage("csSjResubmitStateError", null, jobName);
/* 274 */         this.m_service.createServiceException(null, errMsg);
/*     */       }
/* 276 */       state = "I";
/*     */     }
/* 278 */     else if (newState.equals("cancel"))
/*     */     {
/* 280 */       if (!ScheduledJobUtils.isActiveJob(oldState))
/*     */       {
/* 282 */         String errMsg = LocaleUtils.encodeMessage("csSjCancelStateError", null, jobName);
/* 283 */         this.m_service.createServiceException(null, errMsg);
/*     */       }
/* 285 */       state = "C";
/*     */     }
/* 287 */     else if (newState.equals("delete"))
/*     */     {
/* 289 */       deleteJob();
/*     */     }
/*     */     else
/*     */     {
/* 293 */       String errMsg = LocaleUtils.encodeMessage("csSjUnknownState", null, jobName, newState);
/* 294 */       throw new DataException(errMsg);
/*     */     }
/*     */ 
/* 297 */     if (state == null)
/*     */       return;
/* 299 */     this.m_binder.putLocal("dSjState", state);
/* 300 */     this.m_workspace.execute("UscheduledJobState", this.m_binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteJob()
/*     */     throws DataException, ServiceException
/*     */   {
/* 307 */     JobState jState = new JobState();
/* 308 */     jState.m_data = this.m_binder;
/*     */ 
/* 310 */     ScheduledJobStorage jStorage = ScheduledJobManager.getStorage(this.m_workspace);
/* 311 */     jStorage.deleteJob(jState, false, this.m_workspace, this.m_service);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateScheduledJob()
/*     */     throws DataException, ServiceException
/*     */   {
/* 318 */     ScheduledJobStorage jStorage = ScheduledJobManager.getStorage(this.m_workspace);
/*     */ 
/* 320 */     String id = ScheduledJobUtils.getIdFromBinder(this.m_binder);
/* 321 */     String dir = jStorage.getActiveDir();
/* 322 */     DataBinder binder = ResourceUtils.readDataBinder(dir, id + ".hda");
/*     */ 
/* 325 */     updateJobParameters(binder);
/*     */ 
/* 328 */     DataResultSet jobInfo = (DataResultSet)this.m_binder.getResultSet("ScheduledJob");
/* 329 */     Map map = jobInfo.getCurrentRowMap();
/* 330 */     DataBinder.mergeHashTables(binder.getLocalData(), map);
/*     */ 
/* 333 */     Map localData = binder.getLocalData();
/* 334 */     Set set = localData.keySet();
/* 335 */     for (String key : set)
/*     */     {
/* 337 */       String val = this.m_binder.getAllowMissing(key);
/* 338 */       if (val != null)
/*     */       {
/* 340 */         binder.putLocal(key, val);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 345 */     Object[] params = { binder, jStorage };
/* 346 */     this.m_service.setCachedObject("updateScheduledJob:parameters", params);
/* 347 */     this.m_service.executeFilter("updateScheduledJob");
/*     */ 
/* 350 */     jStorage.updateTask(binder, null, this.m_workspace, this.m_service);
/*     */   }
/*     */ 
/*     */   public void updateJobParameters(DataBinder binder) throws ServiceException
/*     */   {
/* 355 */     DataResultSet drset = (DataResultSet)binder.getResultSet("JobParameters");
/* 356 */     if (drset == null)
/*     */     {
/* 358 */       return;
/*     */     }
/*     */ 
/* 361 */     String changedStr = this.m_binder.getLocal("sjItemsChanged");
/* 362 */     List changed = StringUtils.makeListFromSequenceSimple(changedStr);
/*     */ 
/* 364 */     int numFields = drset.getNumFields();
/* 365 */     for (int i = 0; i < changed.size(); ++i)
/*     */     {
/* 367 */       int rowIndex = NumberUtils.parseInteger((String)changed.get(i), -1);
/* 368 */       if (rowIndex < 0)
/*     */       {
/* 370 */         String errMsg = LocaleUtils.encodeMessage("csSjJobParametersUpdateError", null, "" + rowIndex);
/*     */ 
/* 372 */         this.m_service.createServiceException(null, errMsg);
/*     */       }
/*     */ 
/* 375 */       List row = drset.getRowValues(rowIndex);
/* 376 */       for (int j = 0; j < numFields; ++j)
/*     */       {
/* 378 */         String clmn = drset.getFieldName(j);
/* 379 */         String val = this.m_binder.getLocal(clmn + "_" + rowIndex);
/* 380 */         row.set(j, val);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 385 */     String deletedStr = this.m_binder.getLocal("sjItemsDeleted");
/* 386 */     List deleted = StringUtils.makeListFromSequenceSimple(deletedStr);
/* 387 */     SortUtils.sortIntegerList(deleted, false);
/* 388 */     for (int i = 0; i < deleted.size(); ++i)
/*     */     {
/* 390 */       int rowIndex = NumberUtils.parseInteger((String)deleted.get(i), -1);
/* 391 */       if (rowIndex < 0)
/*     */       {
/* 393 */         String errMsg = LocaleUtils.encodeMessage("csSjJobParametersDeleteError", null, "" + rowIndex);
/*     */ 
/* 395 */         this.m_service.createServiceException(null, errMsg);
/*     */       }
/* 397 */       drset.deleteRow(rowIndex);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 403 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87893 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ScheduledJobHandler
 * JD-Core Version:    0.5.4
 */