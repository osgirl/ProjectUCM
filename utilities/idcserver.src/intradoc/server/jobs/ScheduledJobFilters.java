/*     */ package intradoc.server.jobs;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.common.TableUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.FilterImplementor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ 
/*     */ public class ScheduledJobFilters
/*     */   implements FilterImplementor
/*     */ {
/*     */   public Workspace m_ws;
/*     */   public DataBinder m_binder;
/*     */   public ExecutionContext m_cxt;
/*     */ 
/*     */   public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  45 */     this.m_ws = ws;
/*  46 */     this.m_binder = binder;
/*  47 */     this.m_cxt = cxt;
/*     */ 
/*  49 */     String parameter = (String)cxt.getCachedObject("filterParameter");
/*  50 */     if (parameter == null)
/*     */     {
/*  52 */       return 0;
/*     */     }
/*     */ 
/*  55 */     int returnCode = 0;
/*  56 */     if (parameter.equals("createUpdateSystemJobs"))
/*     */     {
/*  58 */       boolean isServerMode = SharedObjects.getEnvValueAsBoolean("IsServerMode", true);
/*  59 */       if ((isServerMode) && (this.m_ws != null))
/*     */       {
/*  61 */         returnCode = createUpdateSystemJobs();
/*     */       }
/*     */     }
/*     */ 
/*  65 */     return returnCode;
/*     */   }
/*     */ 
/*     */   public int createUpdateSystemJobs() throws DataException, ServiceException
/*     */   {
/*  70 */     ScheduledJobStorage storage = ScheduledJobManager.getStorage(this.m_ws);
/*     */ 
/*  72 */     Table jobs = ResourceContainerUtils.getDynamicTableResource("SystemScheduledJobs");
/*  73 */     if ((jobs == null) || (jobs.m_rows == null))
/*     */     {
/*  75 */       return 0;
/*     */     }
/*     */ 
/*  78 */     int[] fields = TableUtils.getIndexList(jobs, new String[] { "dSjName", "paramTable", "version" });
/*  79 */     for (int i = 0; i < jobs.getNumRows(); ++i)
/*     */     {
/*  81 */       boolean isUpdate = false;
/*  82 */       String jobName = jobs.getString(i, fields[0]);
/*  83 */       String tableName = jobs.getString(i, fields[1]);
/*  84 */       String version = jobs.getString(i, fields[2]);
/*  85 */       Table jobParams = ResourceContainerUtils.getDynamicTableResource(tableName);
/*  86 */       String currentVersion = null;
/*     */ 
/*  88 */       DataBinder jobBinder = new DataBinder();
/*  89 */       jobBinder.putLocal("dSjName", jobName);
/*     */ 
/*  92 */       ResultSet rset = this.m_ws.createResultSet("QscheduledJob", jobBinder);
/*  93 */       if (rset.first())
/*     */       {
/*  95 */         isUpdate = true;
/*  96 */         jobBinder.mergeResultSetRowIntoLocalData(rset);
/*     */ 
/*  99 */         String id = ScheduledJobUtils.getIdFromBinder(jobBinder);
/* 100 */         String dir = storage.getActiveDir();
/* 101 */         ResourceUtils.serializeDataBinder(dir, id + ".hda", jobBinder, false, false);
/*     */ 
/* 104 */         currentVersion = jobBinder.getLocal("sjDesignVersion");
/* 105 */         if (currentVersion == null)
/*     */         {
/* 107 */           currentVersion = "0";
/*     */         }
/* 109 */         if (!SystemUtils.isOlderVersion(currentVersion, version)) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 115 */       DataBinderUtils.mergeKeyValueVersionTableIntoBinder(jobParams, jobBinder, currentVersion);
/*     */ 
/* 117 */       if (jobBinder.getLocal("dSjCategory") == null)
/*     */       {
/* 119 */         jobBinder.putLocal("dSjCategory", "system");
/*     */       }
/* 121 */       if (jobBinder.getLocal("dSjInitUser") == null)
/*     */       {
/* 123 */         jobBinder.putLocal("dSjInitUser", "sysadmin");
/*     */       }
/* 125 */       if (jobBinder.getLocal("dSjDescription") == null)
/*     */       {
/* 127 */         jobBinder.putLocal("dSjDescription", "csSystemJobDesc_" + jobName);
/*     */       }
/*     */ 
/* 130 */       jobBinder.putLocal("sjDesignVersion", version);
/* 131 */       if (!isUpdate)
/*     */       {
/* 133 */         storage.addTask(jobBinder, this.m_ws, new ExecutionContextAdaptor());
/*     */       }
/*     */       else
/*     */       {
/* 137 */         storage.updateTask(jobBinder, null, this.m_ws, new ExecutionContextAdaptor());
/*     */       }
/*     */     }
/*     */ 
/* 141 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 146 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82634 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ScheduledJobFilters
 * JD-Core Version:    0.5.4
 */