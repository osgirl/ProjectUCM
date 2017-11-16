/*     */ package intradoc.server.jobs;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class ScheduledJobStorage
/*     */ {
/*     */   protected String m_jobsDir;
/*     */   protected boolean m_isInitialized;
/*     */   protected FieldInfo[] m_sjColumns;
/*     */   protected Map<String, String> m_requiredFields;
/*     */   protected Map<String, String> m_defaultValues;
/*     */   protected List<String> m_cruftKeys;
/*     */ 
/*     */   public ScheduledJobStorage()
/*     */   {
/*  31 */     this.m_jobsDir = null;
/*  32 */     this.m_isInitialized = false;
/*  33 */     this.m_sjColumns = null;
/*  34 */     this.m_requiredFields = null;
/*  35 */     this.m_defaultValues = null;
/*  36 */     this.m_cruftKeys = null;
/*     */   }
/*     */ 
/*     */   public void init(Workspace ws) throws ServiceException, DataException {
/*  40 */     if (this.m_isInitialized)
/*     */     {
/*  42 */       return;
/*     */     }
/*     */ 
/*  46 */     String dataDir = DirectoryLocator.getAppDataDirectory();
/*  47 */     this.m_jobsDir = (dataDir + "sjobs/");
/*     */ 
/*  50 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_jobsDir + "active/", 2, true);
/*  51 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_jobsDir + "history/", 2, true);
/*     */ 
/*  53 */     initColumnInfo(ws);
/*  54 */     initCruftKeys();
/*  55 */     this.m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public void initColumnInfo(Workspace ws) throws DataException
/*     */   {
/*  60 */     this.m_sjColumns = ws.getColumnList("ScheduledJobs");
/*  61 */     this.m_requiredFields = new HashMap();
/*  62 */     this.m_defaultValues = new HashMap();
/*     */ 
/*  64 */     DataResultSet columnInfoSet = SharedObjects.getTable("SystemTable.ScheduledJobs");
/*  65 */     FieldInfo[] fis = ResultSetUtils.createInfoList(columnInfoSet, new String[] { "dsdColumnName", "dsdIsRequired", "dsdDefaultValue" }, false);
/*     */ 
/*  67 */     for (columnInfoSet.first(); columnInfoSet.isRowPresent(); columnInfoSet.next())
/*     */     {
/*  69 */       String name = columnInfoSet.getStringValue(fis[0].m_index);
/*  70 */       String isRequiredStr = columnInfoSet.getStringValue(fis[1].m_index);
/*  71 */       boolean isRequired = StringUtils.convertToBool(isRequiredStr, false);
/*  72 */       if (isRequired)
/*     */       {
/*  74 */         this.m_requiredFields.put(name, "1");
/*     */       }
/*     */       else
/*     */       {
/*  78 */         String val = columnInfoSet.getStringValue(fis[2].m_index);
/*  79 */         this.m_defaultValues.put(name, val);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initCruftKeys()
/*     */   {
/*  87 */     this.m_cruftKeys = new ArrayList();
/*  88 */     this.m_cruftKeys.addAll(Arrays.asList(new String[] { "dUser", "monitoredSubjects", "refreshSubjects", "changedSubjects", "watchedMonikers", "refreshMonikers", "changedMonikers", "monitoredTopics", "refreshTopics", "changedTopics", "subjectNotifyChanged", "monikerNotifyChanged", "topicNotifyChanged", "CurrentArchiverStatus", "GetCurrentArchiverStatus", "GetCurrentIndexingStatus", "NoHttpHeaders", "StatusCode", "StatusMessageKey", "StatusMessage", "ErrorStackTrace", "IsNew", "ClientEncoding", "IsJava", "IdcService", "forceLogin" }));
/*     */   }
/*     */ 
/*     */   public void addTask(DataBinder data, Workspace ws, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 100 */     checkOrAddTask(data, ws, cxt, 1);
/*     */   }
/*     */ 
/*     */   public void checkOrAddTask(DataBinder data, Workspace ws, ExecutionContext cxt, int flags)
/*     */     throws ServiceException, DataException
/*     */   {
/* 107 */     ScheduledJobUtils.checkOrCreateID("dSjName", 15, data, ws, flags);
/* 108 */     if (DataBinderUtils.getLocalBoolean(data, "sjNameExists", false))
/*     */     {
/* 111 */       return;
/*     */     }
/*     */ 
/* 115 */     validateTaskInfo(data, ws, cxt);
/*     */ 
/* 118 */     ws.execute("IscheduledJob", data);
/*     */ 
/* 121 */     data.putLocal("dSjLastProcessedStatus", "I");
/* 122 */     data.putLocal("dSjMessage", "!csSjJobCreated");
/* 123 */     ScheduledJobUtils.addJobHistoryEvent(data, ws, cxt);
/*     */ 
/* 126 */     createOrUpdateJobDefinition(data, cxt, false);
/*     */   }
/*     */ 
/*     */   public void updateTask(DataBinder data, JobState jState, Workspace ws, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 134 */     validateTaskInfo(data, ws, cxt);
/*     */ 
/* 136 */     if ((jState != null) && (jState.m_isFinished))
/*     */     {
/* 138 */       ws.execute("UscheduledJobDone", data);
/*     */     }
/*     */     else
/*     */     {
/* 142 */       ws.execute("UscheduledJob", data);
/*     */     }
/*     */ 
/* 146 */     createOrUpdateJobDefinition(data, cxt, true);
/*     */   }
/*     */ 
/*     */   public void validateTaskInfo(DataBinder data, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 153 */     int size = this.m_sjColumns.length;
/* 154 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 156 */       FieldInfo info = this.m_sjColumns[i];
/* 157 */       String name = info.m_name;
/* 158 */       String val = null;
/* 159 */       Object reqObj = this.m_requiredFields.get(name);
/* 160 */       if (reqObj != null)
/*     */       {
/* 162 */         val = data.get(name);
/*     */       }
/*     */       else
/*     */       {
/* 166 */         val = data.getAllowMissing(name);
/*     */       }
/* 168 */       if ((val == null) || (val.length() == 0))
/*     */       {
/* 170 */         val = (String)this.m_defaultValues.get(name);
/* 171 */         if (val != null)
/*     */         {
/* 173 */           data.putLocal(name, val);
/*     */         }
/*     */       }
/* 176 */       if ((val == null) || (val.length() <= 0)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 180 */       String errMsg = null;
/* 181 */       if (info.m_type == 3)
/*     */       {
/* 183 */         long v = NumberUtils.parseLong(val, -1L);
/* 184 */         if (v < 0L)
/*     */         {
/* 186 */           errMsg = LocaleUtils.encodeMessage("csSjFieldInvalidInteger", null, name, val);
/*     */         }
/*     */ 
/*     */       }
/* 190 */       else if (info.m_type == 6)
/*     */       {
/* 192 */         if (val.length() > info.m_maxLen)
/*     */         {
/* 194 */           errMsg = LocaleUtils.encodeMessage("csSjFieldExceedsMaxLength", null, name, val, "" + info.m_maxLen);
/*     */         }
/* 197 */         else if (name.equals("dSjInterval"))
/*     */         {
/*     */           try
/*     */           {
/* 203 */             NumberUtils.parseTypedInteger(val, 1, 20, 20);
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 208 */             errMsg = LocaleUtils.encodeMessage("csSjIntervalError", null, name, val);
/*     */           }
/*     */ 
/*     */         }
/* 212 */         else if (name.equals("dSjState"))
/*     */         {
/* 214 */           validateOption(val, "dSjState", "SjStateTable");
/*     */         }
/* 216 */         else if (name.equals("dSjType"))
/*     */         {
/* 218 */           validateOption(val, "dSjType", "SjTypeTable");
/*     */         }
/* 220 */         else if (name.equals("dSjQueueType"))
/*     */         {
/* 222 */           validateOption(val, "dSjQueueType", "SjQueueTypeTable");
/*     */         }
/*     */       }
/* 225 */       else if (info.m_type == 5)
/*     */       {
/*     */         try
/*     */         {
/* 229 */           LocaleResources.parseDate(val, cxt);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 233 */           errMsg = LocaleUtils.encodeMessage("csSjFieldInvalidDate", e.getMessage(), name, val);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 238 */       if (errMsg == null)
/*     */         continue;
/* 240 */       throw new ServiceException(errMsg);
/*     */     }
/*     */ 
/* 245 */     if (PluginFilters.filter("validateTaskInfo", ws, data, null) == -1)
/*     */     {
/* 247 */       return;
/*     */     }
/*     */ 
/* 251 */     Date dte = new Date();
/* 252 */     String dteStr = LocaleUtils.formatODBC(dte);
/* 253 */     data.putLocal("dSjCreateTs", dteStr);
/* 254 */     data.putLocal("dSjUpdateTs", dteStr);
/*     */   }
/*     */ 
/*     */   protected void validateOption(String val, String clmn, String rsName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 260 */     ResultSet rset = SharedObjects.getTable(rsName);
/* 261 */     String v = ResultSetUtils.findValueIgnoreCase(rset, "sjValue", val, "sjValue");
/* 262 */     if (v != null)
/*     */       return;
/* 264 */     String errMsg = LocaleUtils.encodeMessage("csSjInvalidValue", null, clmn, val);
/* 265 */     throw new ServiceException(errMsg);
/*     */   }
/*     */ 
/*     */   public void createOrUpdateJobDefinition(DataBinder data, ExecutionContext cxt, boolean isUpdate)
/*     */     throws ServiceException
/*     */   {
/* 282 */     String name = data.getLocal("dSjName");
/* 283 */     Properties oldMap = data.getLocalData();
/*     */     try
/*     */     {
/* 286 */       Set set = oldMap.keySet();
/* 287 */       Properties props = new Properties();
/* 288 */       for (Iterator iter = set.iterator(); iter.hasNext(); )
/*     */       {
/* 290 */         String key = (String)iter.next();
/* 291 */         if ((!key.startsWith("dSj")) && (!this.m_cruftKeys.contains(key)))
/*     */         {
/* 293 */           props.put(key, oldMap.get(key));
/*     */         }
/*     */       }
/*     */ 
/* 297 */       String filename = getFileName(name);
/* 298 */       String dir = getActiveDir();
/*     */ 
/* 301 */       data.setLocalData(props);
/*     */ 
/* 303 */       data.putLocal("dSjName", name);
/* 304 */       boolean success = ResourceUtils.serializeDataBinder(dir, filename, data, true, isUpdate);
/*     */ 
/* 306 */       if (!success)
/*     */       {
/* 308 */         String errMsg = LocaleUtils.encodeMessage("csSjErrorWritingJobDef", null, filename, dir);
/*     */ 
/* 310 */         Report.error("system", errMsg, null);
/*     */       }
/*     */       else
/*     */       {
/* 314 */         SubjectManager.notifyChanged("scheduledjobs");
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 319 */       data.setLocalData(oldMap);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void deleteJob(JobState jState, boolean isInternal, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 326 */     DataBinder data = jState.m_data;
/* 327 */     ws.execute("DscheduledJob", data);
/*     */ 
/* 329 */     String sjName = data.get("dSjName");
/* 330 */     Service service = null;
/* 331 */     if (cxt instanceof Service)
/*     */     {
/* 333 */       service = (Service)cxt;
/*     */ 
/* 335 */       Object[] filterParams = { jState, new Boolean(isInternal) };
/* 336 */       service.setCachedObject("scheduledJobDelete:parameters", filterParams);
/* 337 */       service.executeFilter("scheduledJobDelete");
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 342 */       if (!isInternal)
/*     */       {
/* 344 */         String msg = LocaleUtils.encodeMessage("csSjDeletedJobMessage", null, sjName);
/*     */ 
/* 348 */         data.putLocal("dSjMessage", msg);
/* 349 */         data.putLocal("dSjLastProcessedStatus", "D");
/* 350 */         ScheduledJobUtils.addJobHistoryEvent(data, ws, service);
/*     */ 
/* 352 */         saveDefinitionInHistory(jState);
/*     */       }
/*     */ 
/* 356 */       String filename = getFileName(sjName);
/* 357 */       String dir = getActiveDir();
/* 358 */       if (service != null)
/*     */       {
/* 360 */         DataBinder binder = service.getBinder();
/* 361 */         binder.addTempFile(dir + filename);
/*     */       }
/*     */       else
/*     */       {
/* 365 */         FileUtils.deleteFile(dir + filename);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 370 */       String msg = LocaleUtils.encodeMessage("csSjDeletedJobSaveError", null, sjName);
/* 371 */       Report.error("scheduledjobs", msg, e);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 375 */       String msg = LocaleUtils.encodeMessage("csSjDeletedJobDbError", null, sjName);
/* 376 */       Report.error("scheduledjobs", msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void saveDefinitionInHistory(JobState jState)
/*     */     throws ServiceException, DataException
/*     */   {
/* 384 */     boolean isSave = SharedObjects.getEnvValueAsBoolean("SjSaveToHistory", false);
/* 385 */     if (!isSave)
/*     */       return;
/* 387 */     DataBinder data = jState.m_data;
/* 388 */     String guid = data.getLocal("dSjHistoryGUID");
/* 389 */     if (guid != null)
/*     */     {
/* 391 */       guid = guid.toLowerCase() + ".hda";
/*     */     }
/*     */     else
/*     */     {
/* 395 */       String sjName = data.get("dSjName");
/* 396 */       guid = getFileName(sjName);
/*     */     }
/*     */ 
/* 399 */     String historyDir = getHistoryDir();
/* 400 */     if (jState.m_exceptionSet != null)
/*     */     {
/* 402 */       data.addResultSet("JobExceptions", jState.m_exceptionSet);
/*     */     }
/*     */ 
/* 405 */     ResourceUtils.serializeDataBinder(historyDir, guid, data, true, false);
/*     */   }
/*     */ 
/*     */   public String getActiveDir()
/*     */   {
/* 411 */     return this.m_jobsDir + "active/";
/*     */   }
/*     */ 
/*     */   public String getHistoryDir()
/*     */   {
/* 416 */     return this.m_jobsDir + "history/";
/*     */   }
/*     */ 
/*     */   public String getFileName(String name)
/*     */   {
/* 421 */     return name.toLowerCase() + ".hda";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 426 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98846 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ScheduledJobStorage
 * JD-Core Version:    0.5.4
 */