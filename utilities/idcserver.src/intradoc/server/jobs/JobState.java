/*     */ package intradoc.server.jobs;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.ProgressState;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JobState
/*     */ {
/*     */   public DataBinder m_data;
/*     */   public DataBinder m_orgData;
/*     */   public DataResultSet m_exceptionSet;
/*  57 */   public boolean m_isFinished = true;
/*  58 */   public boolean m_hasWork = false;
/*  59 */   public int m_workCount = 0;
/*  60 */   public int m_totalCount = 0;
/*     */ 
/*  63 */   public boolean m_isInError = false;
/*  64 */   public boolean m_isFatalError = false;
/*  65 */   public Exception m_finalException = null;
/*  66 */   public String m_errorMsg = null;
/*  67 */   public JobExceptionData m_exceptionData = null;
/*     */   public ProgressState m_globalProgress;
/*     */   public ProgressState m_jobProgress;
/*     */   public IdcMessage m_lastProgress;
/*     */   public long m_lastReportTs;
/*     */   public long m_reportInterval;
/*     */ 
/*     */   public JobState()
/*     */   {
/*  79 */     this.m_jobProgress = new ProgressState();
/*     */   }
/*     */ 
/*     */   public JobState(ProgressState progress)
/*     */   {
/*  84 */     this.m_globalProgress = progress;
/*  85 */     this.m_jobProgress = new ProgressState();
/*     */   }
/*     */ 
/*     */   public void init(DataBinder jobData, Map jobInfo)
/*     */   {
/*  92 */     this.m_data = jobData.createShallowCopyCloneResultSets();
/*  93 */     this.m_data.copyLocalDataStateClone(jobData);
/*  94 */     DataBinder.mergeHashTables(this.m_data.getLocalData(), jobInfo);
/*     */ 
/*  97 */     this.m_orgData = jobData;
/*  98 */     DataBinder.mergeHashTables(this.m_orgData.getLocalData(), jobInfo);
/*     */ 
/* 100 */     this.m_exceptionData = new JobExceptionData();
/* 101 */     this.m_exceptionData.init(this);
/*     */ 
/* 103 */     this.m_lastReportTs = System.currentTimeMillis();
/* 104 */     computeReportInterval();
/*     */   }
/*     */ 
/*     */   public void computeReportInterval()
/*     */   {
/* 109 */     String lookupKey = "SjProgressReportInterval";
/* 110 */     String defValue = "324";
/* 111 */     String jobType = this.m_data.getLocal("dSjType");
/* 112 */     if (jobType.equals("I"))
/*     */     {
/* 114 */       lookupKey = "SjProgressReportImmediateInterval";
/* 115 */       defValue = "10018";
/*     */     }
/*     */ 
/* 118 */     String defaultInterval = ScheduledJobUtils.getConfigValue(lookupKey, this.m_data, defValue, true);
/*     */ 
/* 120 */     this.m_reportInterval = NumberUtils.parseTypedInteger(defaultInterval, 3, 18, 18);
/*     */   }
/*     */ 
/*     */   public ServiceException addExceptionItem(DataBinder binder, Exception e, ScheduledJobImplementor jobImp)
/*     */   {
/* 128 */     ServiceException se = null;
/*     */     try
/*     */     {
/* 131 */       if (this.m_exceptionSet == null)
/*     */       {
/* 133 */         createExceptionSet(jobImp);
/*     */       }
/* 135 */       binder.putLocal("sjException", e.getMessage());
/* 136 */       String jobItemID = binder.getLocal("jobItemID");
/* 137 */       if (jobItemID == null)
/*     */       {
/* 140 */         jobItemID = binder.getAllowMissing("sjCommand");
/* 141 */         if (jobItemID == null)
/*     */         {
/* 143 */           jobItemID = "prepService";
/*     */         }
/* 145 */         binder.putLocal("jobItemID", jobItemID);
/*     */       }
/* 147 */       if (SystemUtils.m_verbose)
/*     */       {
/* 149 */         String sjName = binder.getLocal("dSjName");
/* 150 */         String msg = "Exception encountered in scheduled job:dSjName=" + sjName + " jobItemID=" + jobItemID;
/*     */ 
/* 152 */         Report.debug("scheduledjobs", msg, e);
/*     */       }
/* 154 */       List row = this.m_exceptionSet.createRowAsList(binder);
/* 155 */       this.m_exceptionSet.addRowWithList(row);
/*     */     }
/*     */     catch (DataException de)
/*     */     {
/* 159 */       if (de.getCause() == null)
/*     */       {
/* 161 */         de.initCause(e);
/*     */       }
/* 163 */       String msg = LocaleUtils.encodeMessage("csSjUnableToAddExceptionTask", null);
/* 164 */       se = new ServiceException(-32, msg, de);
/* 165 */       Report.error("scheduledjobs", "JobState.addExceptionItem: Unable to add exception to failed list.", se);
/*     */     }
/*     */ 
/* 168 */     return se;
/*     */   }
/*     */ 
/*     */   public void createExceptionSet(ScheduledJobImplementor jobImp)
/*     */   {
/* 173 */     DataResultSet drset = (DataResultSet)this.m_data.getResultSet("JobParameters");
/* 174 */     if (drset != null)
/*     */     {
/* 176 */       this.m_exceptionSet = new DataResultSet();
/* 177 */       this.m_exceptionSet.copyFieldInfo(drset);
/* 178 */       FieldInfo fi = new FieldInfo();
/* 179 */       Vector fieldList = new IdcVector();
/* 180 */       fi.m_name = "sjException";
/* 181 */       fieldList.add(fi);
/* 182 */       this.m_exceptionSet.mergeFieldsWithFlags(fieldList, 0);
/*     */     }
/*     */     else
/*     */     {
/* 186 */       this.m_exceptionSet = jobImp.createExceptionSet(this.m_data);
/* 187 */       if (this.m_exceptionSet != null)
/*     */         return;
/* 189 */       this.m_exceptionSet = new DataResultSet(new String[] { "jobItemID", "sjException" });
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setErrorState(Exception e, String errMsg, boolean isFatal)
/*     */   {
/* 196 */     this.m_isInError = true;
/* 197 */     this.m_isFatalError = isFatal;
/* 198 */     if ((this.m_finalException != null) && (this.m_finalException != e))
/*     */     {
/* 200 */       Throwable t = e.getCause();
/* 201 */       if (t == null)
/*     */       {
/* 203 */         e.initCause(this.m_finalException);
/*     */       }
/* 205 */       this.m_finalException = e;
/*     */     }
/*     */     else
/*     */     {
/* 209 */       this.m_finalException = e;
/*     */     }
/* 211 */     if ((errMsg == null) || (errMsg.length() <= 0))
/*     */       return;
/* 213 */     this.m_errorMsg = LocaleUtils.encodeMessage(errMsg, this.m_errorMsg);
/*     */   }
/*     */ 
/*     */   public String getLastProgress()
/*     */   {
/* 220 */     String errMsg = null;
/* 221 */     if (this.m_isInError)
/*     */     {
/* 223 */       errMsg = LocaleUtils.encodeMessage("csSjErrorReport", null, "" + this.m_exceptionData.m_errorCount, "" + this.m_exceptionData.m_consecutiveErrorCount, "" + this.m_exceptionData.m_severeErrorCount);
/*     */     }
/*     */ 
/* 227 */     String msg = LocaleUtils.encodeMessage("csSjProgressReport", errMsg, "" + this.m_workCount, "" + this.m_totalCount);
/*     */ 
/* 229 */     return msg;
/*     */   }
/*     */ 
/*     */   public String createFinalProgressMessage()
/*     */   {
/* 234 */     String errMsg = null;
/* 235 */     if (this.m_isFatalError)
/*     */     {
/* 237 */       errMsg = LocaleUtils.encodeMessage("csSjFatalError", this.m_errorMsg);
/*     */     }
/*     */     else
/*     */     {
/* 241 */       errMsg = this.m_errorMsg;
/*     */     }
/* 243 */     if (this.m_isInError)
/*     */     {
/* 245 */       errMsg = LocaleUtils.encodeMessage("csSjErrorReport", errMsg, "" + this.m_exceptionData.m_errorCount, "" + this.m_exceptionData.m_consecutiveErrorCount, "" + this.m_exceptionData.m_severeErrorCount);
/*     */     }
/*     */ 
/* 249 */     return LocaleUtils.encodeMessage("csSjFinalProgress", errMsg, "" + this.m_workCount, "" + this.m_totalCount);
/*     */   }
/*     */ 
/*     */   public void updateCounters(int count, int totalRows)
/*     */   {
/* 255 */     this.m_workCount = count;
/* 256 */     this.m_totalCount = totalRows;
/*     */   }
/*     */ 
/*     */   public void updateAndCheckProgress(int type, IdcMessage idcMsg, DataBinder binder, Map args, Workspace ws, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 263 */     Object[] params = { this, args };
/* 264 */     cxt.setCachedObject("updateJobProgress:parameters", params);
/* 265 */     PluginFilters.filter("updateJobProgress", ws, binder, cxt);
/*     */ 
/* 267 */     reportProgress(type, null, idcMsg);
/*     */ 
/* 269 */     boolean isCheckAbort = true;
/* 270 */     if (args != null)
/*     */     {
/* 272 */       isCheckAbort = StringUtils.convertToBool((String)args.get("isCheckAbort"), isCheckAbort);
/*     */     }
/*     */ 
/* 276 */     boolean isCancelled = updateJobStatus(binder, ws, isCheckAbort);
/* 277 */     if (!isCancelled)
/*     */       return;
/* 279 */     throw new ServiceException(-65, "!csSjJobCancelled");
/*     */   }
/*     */ 
/*     */   public boolean updateJobStatus(DataBinder binder, Workspace ws, boolean isCheckAbort)
/*     */   {
/* 287 */     boolean isAbort = false;
/* 288 */     long ts = System.currentTimeMillis();
/* 289 */     long t = this.m_lastReportTs + this.m_reportInterval;
/* 290 */     if (t >= ts)
/*     */     {
/* 292 */       return isAbort;
/*     */     }
/*     */ 
/* 295 */     boolean isError = false;
/*     */     try
/*     */     {
/* 298 */       Date dte = new Date(ts);
/* 299 */       String msg = LocaleUtils.encodeMessage("csSjProgressReport", null, "" + this.m_workCount, "" + this.m_totalCount);
/*     */ 
/* 301 */       binder.putLocal("dSjProgress", msg);
/* 302 */       binder.putLocal("dSjProcressTs", LocaleUtils.formatODBC(dte));
/* 303 */       ws.execute("UscheduledJobProgress", binder);
/*     */ 
/* 306 */       if (isCheckAbort)
/*     */       {
/* 308 */         ResultSet rset = ws.createResultSet("QscheduledJobState", this.m_data);
/* 309 */         String state = null;
/* 310 */         if (!rset.isEmpty())
/*     */         {
/* 312 */           state = ResultSetUtils.getValue(rset, "dSjState");
/*     */         }
/* 314 */         if ((state == null) || (!ScheduledJobUtils.isActiveJob(state)))
/*     */         {
/* 316 */           isAbort = true;
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception state)
/*     */     {
/*     */       Map state;
/* 322 */       Report.error("scheduledjobs", e, "csSjUpdateProgressError", new Object[0]);
/* 323 */       reportProgress(-1, e, "csSjUpdateProgressError", new Object[0]);
/* 324 */       isAbort = true;
/*     */     }
/*     */     finally
/*     */     {
/*     */       Map state;
/* 328 */       if (!isError)
/*     */       {
/* 330 */         this.m_lastReportTs = ts;
/*     */       }
/*     */ 
/* 333 */       Map state = new HashMap();
/* 334 */       ws.getConnectionState(state);
/* 335 */       if (!ScriptUtils.convertObjectToBool(state.get("isInTransaction"), false))
/*     */       {
/* 337 */         ws.releaseConnection();
/*     */       }
/*     */     }
/* 340 */     return isAbort;
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, Throwable t, IdcMessage idcMsg)
/*     */   {
/* 345 */     this.m_lastProgress = idcMsg;
/* 346 */     String jobName = this.m_data.getLocal("dSjName");
/* 347 */     this.m_lastProgress.m_prior = IdcMessageFactory.lc("csSjProgress", new Object[] { jobName });
/*     */ 
/* 349 */     String msg = LocaleUtils.encodeMessage(this.m_lastProgress);
/* 350 */     this.m_jobProgress.reportProgress(type, msg, t);
/* 351 */     Report.trace("scheduledjobs", msg, t);
/* 352 */     if (this.m_globalProgress == null)
/*     */       return;
/* 354 */     if ((type == 2) || (type == 4))
/*     */     {
/* 359 */       type = 3;
/*     */     }
/* 361 */     this.m_globalProgress.reportProgress(type, msg, t);
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, Throwable t, String key, Object[] args)
/*     */   {
/* 367 */     IdcMessage idcMessage = IdcMessageFactory.lc(key, args);
/* 368 */     reportProgress(type, t, idcMessage);
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, Throwable t)
/*     */   {
/* 373 */     IdcMessage idcMsg = new IdcMessage();
/* 374 */     idcMsg.m_msgSimple = msg;
/* 375 */     reportProgress(type, t, idcMsg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 380 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74220 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.JobState
 * JD-Core Version:    0.5.4
 */