/*     */ package intradoc.server.jobs;
/*     */ 
/*     */ import intradoc.common.Errors;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceRequestImplementor;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class ServiceJobImplementor
/*     */   implements ScheduledJobImplementor
/*     */ {
/*     */   protected JobState m_jState;
/*     */   protected Workspace m_workspace;
/*     */   protected ExecutionContext m_cxt;
/*     */ 
/*     */   public ServiceJobImplementor()
/*     */   {
/*  46 */     this.m_jState = null;
/*  47 */     this.m_workspace = null;
/*  48 */     this.m_cxt = null;
/*     */   }
/*     */ 
/*     */   public void initProcess(JobState jState, Workspace ws, ExecutionContext cxt)
/*     */   {
/*  53 */     this.m_jState = jState;
/*  54 */     this.m_workspace = ws;
/*  55 */     this.m_cxt = cxt;
/*     */   }
/*     */ 
/*     */   public Object processJob(JobState jState, Workspace ws, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/*  71 */     initProcess(jState, ws, cxt);
/*  72 */     String preService = jState.m_data.getLocal("SjPrepareService");
/*  73 */     if (preService != null)
/*     */     {
/*  79 */       updateProgress(3, IdcMessageFactory.lc("csSjPrepareServiceExecution", new Object[0]), jState.m_data, null, this.m_workspace);
/*     */ 
/*  82 */       jState.m_exceptionData.setTolerance(false);
/*  83 */       executeService(jState.m_data);
/*  84 */       jState.m_exceptionData.resetTolerance(jState);
/*     */ 
/*  86 */       updateProgress(3, IdcMessageFactory.lc("csSjFinishedPreparing", new Object[0]), jState.m_data, null, this.m_workspace);
/*     */     }
/*     */ 
/*  90 */     IdcMessage idcMsg = null;
/*  91 */     DataResultSet drset = (DataResultSet)jState.m_data.getResultSet("JobParameters");
/*  92 */     if (drset == null)
/*     */     {
/*  94 */       DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  95 */       DataBinder.mergeHashTables(binder.getLocalData(), jState.m_data.getLocalData());
/*  96 */       addResultSetsForJob(jState, binder);
/*     */ 
/*  98 */       idcMsg = IdcMessageFactory.lc("csSjExecuteSingleton", new Object[] { Integer.valueOf(0), Integer.valueOf(1) });
/*  99 */       jState.updateCounters(0, 1);
/* 100 */       updateProgress(3, idcMsg, binder, null, ws);
/*     */ 
/* 102 */       executeService(binder);
/*     */ 
/* 104 */       idcMsg = IdcMessageFactory.lc("csSjFinishedPreparing", new Object[] { Integer.valueOf(1), Integer.valueOf(1) });
/* 105 */       jState.updateCounters(1, 1);
/* 106 */       updateProgress(3, idcMsg, jState.m_data, null, ws);
/*     */     }
/*     */     else
/*     */     {
/* 110 */       int numRows = drset.getNumRows();
/* 111 */       int count = 1;
/* 112 */       for (drset.first(); drset.isRowPresent(); ++count)
/*     */       {
/* 114 */         Map map = drset.getCurrentRowMap();
/*     */ 
/* 116 */         DataBinder binder = new DataBinder();
/* 117 */         DataBinder.mergeHashTables(binder.getLocalData(), jState.m_data.getLocalData());
/* 118 */         DataBinder.mergeHashTables(binder.getLocalData(), map);
/* 119 */         addResultSetsForJob(jState, binder);
/*     */ 
/* 121 */         executeService(binder);
/* 122 */         idcMsg = IdcMessageFactory.lc("csSjFinishedTask", new Object[] { Integer.valueOf(count), Integer.valueOf(numRows) });
/* 123 */         updateProgress(3, idcMsg, binder, null, ws);
/*     */ 
/* 112 */         drset.next();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 126 */     return jState;
/*     */   }
/*     */ 
/*     */   public void addResultSetsForJob(JobState jState, DataBinder binder)
/*     */   {
/* 131 */     for (Iterator i$ = jState.m_data.getResultSets().keySet().iterator(); i$.hasNext(); ) { Object key = i$.next();
/*     */ 
/* 133 */       String rsetName = (String)key;
/* 134 */       if (DataBinderUtils.getLocalBoolean(jState.m_data, "sjIncludeResultSet:" + rsetName, false))
/*     */       {
/* 136 */         binder.addResultSet(rsetName, jState.m_data.getResultSet(rsetName));
/*     */       } }
/*     */   }
/*     */ 
/*     */   public void executeService(DataBinder binder)
/*     */     throws ServiceException, DataException
/*     */   {
/* 143 */     Service service = null;
/* 144 */     Exception capException = null;
/*     */     try
/*     */     {
/* 147 */       Report.trace("scheduledjobs", null, "csSjExecutingService", new Object[] { binder.getLocal("jobItemID"), binder.getLocal("sjCommand") });
/*     */ 
/* 149 */       this.m_jState.reportProgress(3, null, "csSjExecutingService", new Object[] { binder.getLocal("jobItemID"), binder.getLocal("sjCommand") });
/*     */ 
/* 152 */       String cmd = binder.get("sjCommand");
/* 153 */       service = ScheduledJobUtils.initService(cmd, binder, this.m_workspace, this.m_jState, this);
/* 154 */       service.doRequestInternal();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 158 */       capException = e;
/*     */     }
/*     */     finally
/*     */     {
/* 162 */       finishExecuteCommand(capException, binder, service);
/* 163 */       if (service != null)
/*     */       {
/* 165 */         service.clear();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void finishExecuteCommand(Exception e, DataBinder binder, Service cxt)
/*     */     throws ServiceException
/*     */   {
/* 173 */     String jobItem = binder.getLocal("jobItemID");
/* 174 */     String command = binder.getLocal("sjCommand");
/* 175 */     JobExceptionData exceptionData = this.m_jState.m_exceptionData;
/*     */ 
/* 177 */     Report.trace("scheduledjobs", e, "csSjFinishExecuteCommand", new Object[] { jobItem, command });
/* 178 */     this.m_jState.reportProgress(3, e, "csSjFinishExecuteCommand", new Object[] { jobItem, command });
/*     */ 
/* 180 */     ServiceException fatalException = null;
/* 181 */     if (e != null)
/*     */     {
/* 183 */       this.m_jState.setErrorState(e, "", false);
/* 184 */       fatalException = this.m_jState.addExceptionItem(binder, e, this);
/*     */ 
/* 186 */       exceptionData.m_consecutiveErrorCount += 1;
/* 187 */       exceptionData.m_errorCount += 1;
/*     */     }
/*     */     else
/*     */     {
/* 191 */       exceptionData.m_consecutiveErrorCount = 0;
/*     */     }
/*     */ 
/* 194 */     if (cxt != null)
/*     */     {
/* 196 */       ServiceRequestImplementor srI = cxt.getRequestImplementor();
/* 197 */       if (srI.m_isSevereError)
/*     */       {
/* 199 */         exceptionData.m_severeErrorCount += 1;
/*     */       }
/*     */     }
/*     */ 
/* 203 */     Report.trace("scheduledjobs", null, "csSjFinishExecuteCmdReport", new Object[] { jobItem, command, "" + exceptionData.m_errorCount, "" + exceptionData.m_severeErrorCount, "" + exceptionData.m_consecutiveErrorCount });
/*     */ 
/* 206 */     this.m_jState.reportProgress(3, null, "csSjFinishExecuteCmdReport", new Object[] { jobItem, command, "" + exceptionData.m_errorCount, "" + exceptionData.m_severeErrorCount, "" + exceptionData.m_consecutiveErrorCount });
/*     */ 
/* 210 */     int errorCode = 0;
/* 211 */     String errMsgStub = null;
/* 212 */     if (fatalException != null)
/*     */     {
/* 214 */       errorCode = fatalException.m_errorCode;
/* 215 */       errMsgStub = "!csSjFatalError";
/*     */     }
/* 217 */     else if (!exceptionData.m_isTolerant)
/*     */     {
/* 219 */       errorCode = -32;
/* 220 */       errMsgStub = "!csSjJobIntolerant";
/*     */     }
/* 222 */     else if (exceptionData.m_severeErrorCount > exceptionData.m_maxSevereErrors)
/*     */     {
/* 224 */       errorCode = -32;
/* 225 */       errMsgStub = LocaleUtils.encodeMessage("csSjExceededMaxSevereErrors", null, "" + exceptionData.m_severeErrorCount, "" + exceptionData.m_maxSevereErrors);
/*     */     }
/* 228 */     else if (exceptionData.m_errorCount > exceptionData.m_maxErrors)
/*     */     {
/* 230 */       errorCode = -64;
/* 231 */       errMsgStub = LocaleUtils.encodeMessage("csSjExceededMaxErrors", null, "" + exceptionData.m_errorCount, "" + exceptionData.m_maxErrors);
/*     */     }
/* 234 */     else if (exceptionData.m_consecutiveErrorCount > exceptionData.m_maxConsecutiveErrors)
/*     */     {
/* 236 */       errorCode = -64;
/* 237 */       errMsgStub = LocaleUtils.encodeMessage("csSjExceededMaxConsecutiveErrors", null, "" + exceptionData.m_consecutiveErrorCount, "" + exceptionData.m_maxConsecutiveErrors);
/*     */     }
/* 240 */     else if ((e instanceof IdcException) && (!e instanceof ServiceException))
/*     */     {
/* 242 */       int code = ((IdcException)e).m_errorCode;
/* 243 */       boolean isNormal = Errors.isNormalUserOperationalErrorCode(code);
/* 244 */       if (!isNormal)
/*     */       {
/* 246 */         errorCode = ((IdcException)e).m_errorCode;
/* 247 */         errMsgStub = "!csSjOperationalError";
/*     */       }
/*     */     }
/*     */ 
/* 251 */     if (errorCode >= 0)
/*     */       return;
/* 253 */     String job = binder.getLocal("dSjName");
/* 254 */     String errMsg = LocaleUtils.encodeMessage("csSjAbortingJob", errMsgStub, job);
/* 255 */     throw new ServiceException(errorCode, errMsg, e);
/*     */   }
/*     */ 
/*     */   public void updateProgress(int type, IdcMessage idcMsg, DataBinder binder, Map args, Workspace ws)
/*     */     throws ServiceException, DataException
/*     */   {
/* 265 */     boolean isCheckAbort = true;
/* 266 */     if (args != null)
/*     */     {
/* 268 */       isCheckAbort = StringUtils.convertToBool((String)args.get("isCheckAbort"), isCheckAbort);
/*     */     }
/*     */ 
/* 272 */     Object[] params = { this.m_jState, args };
/* 273 */     this.m_cxt.setCachedObject("updateJobProgress:parameters", params);
/* 274 */     PluginFilters.filter("updateJobProgress", this.m_workspace, binder, this.m_cxt);
/*     */ 
/* 276 */     this.m_jState.updateAndCheckProgress(3, idcMsg, binder, args, ws, this.m_cxt);
/*     */   }
/*     */ 
/*     */   public void finishJob()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void buildResult()
/*     */   {
/* 293 */     Object[] params = { this.m_jState };
/* 294 */     this.m_cxt.setCachedObject("buildJobResult:parameters", params);
/*     */     try
/*     */     {
/* 297 */       PluginFilters.filter("buildJobResult", this.m_workspace, this.m_jState.m_data, this.m_cxt);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 301 */       Report.trace("scheduledjobs", e, "Error building scheduled job result.", new Object[0]);
/* 302 */       this.m_jState.reportProgress(3, e, "Error building scheduled job result.", new Object[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public DataResultSet createExceptionSet(DataBinder binder)
/*     */   {
/* 316 */     DataResultSet drset = null;
/*     */     try
/*     */     {
/* 319 */       Object[] params = { binder };
/* 320 */       this.m_cxt.setCachedObject("sortScheduledJobs:parameters", params);
/* 321 */       int ret = PluginFilters.filter("sortScheduledJobs", this.m_workspace, binder, this.m_cxt);
/* 322 */       if (ret == 1)
/*     */       {
/* 324 */         drset = (DataResultSet)this.m_cxt.getCachedObject("SjExceptionSet");
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 329 */       Report.trace("scheduledjobs", e, "csSjExceptionSetError", new Object[] { this.m_jState.m_data.getLocal("dSjName") });
/*     */ 
/* 331 */       this.m_jState.reportProgress(3, e, "csSjExceptionSetError", new Object[] { this.m_jState.m_data.getLocal("dSjName") });
/*     */     }
/*     */ 
/* 334 */     return drset;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 339 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80997 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ServiceJobImplementor
 * JD-Core Version:    0.5.4
 */