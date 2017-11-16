/*     */ package intradoc.server.jobs.test;
/*     */ 
/*     */ import intradoc.apps.shared.StandAloneApp;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.server.PluginFilterLoader;
/*     */ import intradoc.server.jobs.ScheduledJobManager;
/*     */ import intradoc.server.jobs.ScheduledJobStorage;
/*     */ import intradoc.server.jobs.ScheduledJobsProcessor;
/*     */ import intradoc.shared.FilterImplementor;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SJobTestDriver
/*     */   implements FilterImplementor
/*     */ {
/*  33 */   protected static SJobTestDriver m_test = null;
/*     */   protected StandAloneApp m_standAlone;
/*     */   protected ExecutionContext m_context;
/*     */   protected Workspace m_workspace;
/*     */   protected ScheduledJobStorage m_jobStorage;
/*     */   protected String m_dataFile;
/*     */   protected DataBinder m_inputData;
/*     */   protected Thread m_timer;
/*     */   protected boolean m_isFinished;
/*     */ 
/*     */   public SJobTestDriver()
/*     */   {
/*  34 */     this.m_standAlone = null;
/*     */ 
/*  36 */     this.m_context = null;
/*  37 */     this.m_workspace = null;
/*  38 */     this.m_jobStorage = null;
/*     */ 
/*  40 */     this.m_dataFile = null;
/*  41 */     this.m_inputData = null;
/*     */ 
/*  43 */     this.m_timer = null;
/*  44 */     this.m_isFinished = false;
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/*  51 */     m_test = new SJobTestDriver();
/*  52 */     boolean r = m_test.init(args);
/*  53 */     if (!r)
/*     */     {
/*  55 */       return;
/*     */     }
/*     */ 
/*  59 */     SystemUtils.m_verbose = true;
/*  60 */     Report.m_verbose = true;
/*  61 */     SystemUtils.addAsActiveTrace("scheduledjobs");
/*     */ 
/*  64 */     m_test.runTest();
/*     */   }
/*     */ 
/*     */   protected boolean init(String[] args)
/*     */   {
/*  69 */     boolean r = false;
/*     */     try
/*     */     {
/*  72 */       IdcSystemConfig.loadInitialConfig();
/*  73 */       IdcSystemConfig.loadAppConfigInfo();
/*  74 */       IdcSystemConfig.initLocalization(IdcSystemConfig.F_STANDARD_SERVER);
/*  75 */       IdcSystemConfig.configLocalization();
/*     */ 
/*  77 */       IdcSystemLoader.finishInit(false);
/*     */ 
/*  79 */       SharedObjects.putEnvironmentValue("DisableSubjectMonitoringThread", "1");
/*  80 */       this.m_standAlone = new StandAloneApp();
/*  81 */       this.m_standAlone.finishLoad(false);
/*     */ 
/*  83 */       addTestFilter();
/*     */ 
/*  86 */       this.m_standAlone.doStandaloneAppInit("weblayout");
/*  87 */       this.m_standAlone.doStandaloneAppInit("archiver");
/*     */ 
/*  90 */       this.m_standAlone.setUser("sysadmin");
/*     */ 
/*  92 */       this.m_context = new ExecutionContextAdaptor();
/*  93 */       Provider provider = Providers.getProvider("SystemDatabase");
/*  94 */       this.m_workspace = ((Workspace)provider.getProvider());
/*     */ 
/*  96 */       parseArgs(args);
/*  97 */       readDataFile();
/*     */ 
/*  99 */       this.m_jobStorage = ScheduledJobManager.getStorage(this.m_workspace);
/*     */ 
/* 101 */       r = true;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 105 */       Report.debug(null, "Failed to initialize test", e);
/*     */     }
/* 107 */     return r;
/*     */   }
/*     */ 
/*     */   protected void addTestFilter()
/*     */     throws DataException, ServiceException
/*     */   {
/* 113 */     DataResultSet drset = SharedObjects.getTable("CoreFilters");
/* 114 */     drset.removeAll();
/* 115 */     List row = drset.createEmptyRowAsList();
/* 116 */     row.set(0, "updateJobProgress");
/* 117 */     row.set(1, "intradoc.server.jobs.test.SJobTestDriver");
/* 118 */     row.set(2, "testUpdateJobProgress");
/* 119 */     row.set(3, "10");
/* 120 */     drset.addRowWithList(row);
/*     */ 
/* 122 */     DataBinder filterBinder = new DataBinder();
/* 123 */     filterBinder.addResultSet("CoreFilters", drset);
/* 124 */     Vector list = PluginFilterLoader.cacheFilters(filterBinder, "CoreFilters");
/* 125 */     PluginFilters.registerFilters(list);
/*     */   }
/*     */ 
/*     */   protected void parseArgs(String[] args)
/*     */   {
/* 131 */     int size = args.length;
/* 132 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 134 */       String arg = args[i].toLowerCase();
/* 135 */       if (arg.indexOf("data") < 0)
/*     */         continue;
/* 137 */       this.m_dataFile = args[(++i)];
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void readDataFile()
/*     */     throws ServiceException
/*     */   {
/* 144 */     String workingDir = FileUtils.getWorkingDir();
/* 145 */     if (this.m_dataFile == null)
/*     */     {
/* 148 */       this.m_dataFile = "sjtest.hda";
/*     */     }
/*     */ 
/* 151 */     String path = FileUtils.getAbsolutePath(workingDir, this.m_dataFile);
/* 152 */     this.m_inputData = ResourceUtils.readDataBinderFromPath(path);
/*     */   }
/*     */ 
/*     */   public void runTest()
/*     */   {
/*     */     try
/*     */     {
/* 160 */       DataResultSet jobSet = (DataResultSet)this.m_inputData.getResultSet("TestJobs");
/* 161 */       if ((jobSet == null) || (jobSet.isEmpty()))
/*     */       {
/* 163 */         Report.debug(null, "No jobs to perform the TestJobs result set is empty.", null);
/*     */       }
/*     */       else
/*     */       {
/* 167 */         for (jobSet.first(); jobSet.isRowPresent(); jobSet.next())
/*     */         {
/* 169 */           Map taskMap = jobSet.getCurrentRowMap();
/* 170 */           addJob(taskMap);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 175 */       performJobs();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 179 */       Report.debug(null, "Error encountered while running the test.", e);
/*     */     }
/*     */     finally
/*     */     {
/* 184 */       waitForExit();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void waitForExit()
/*     */   {
/* 190 */     Runnable run = new Object()
/*     */     {
/*     */       public void run()
/*     */       {
/* 194 */         while (!SJobTestDriver.this.m_isFinished)
/*     */         {
/* 196 */           synchronized (SJobTestDriver.this.m_timer)
/*     */           {
/*     */             try
/*     */             {
/* 200 */               SJobTestDriver.this.m_timer.wait(300L);
/*     */             }
/*     */             catch (Exception ignore)
/*     */             {
/* 204 */               Report.trace("system", null, ignore);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 211 */     this.m_timer = new Thread(run, "exitTimer");
/* 212 */     this.m_timer.setDaemon(true);
/* 213 */     this.m_timer.start();
/*     */   }
/*     */ 
/*     */   public void addJob(Map taskMap) throws ServiceException, DataException
/*     */   {
/* 218 */     Properties savedProps = this.m_inputData.getLocalData();
/* 219 */     Properties props = (Properties)savedProps.clone();
/*     */     try
/*     */     {
/* 222 */       DataBinder binder = buildJobBinder(props, taskMap);
/* 223 */       this.m_jobStorage.addTask(binder, this.m_workspace, this.m_context);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 227 */       Report.trace("system", "Unable to add job.", e);
/*     */     }
/*     */     finally
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public DataBinder buildJobBinder(Properties props, Map taskMap)
/*     */   {
/* 238 */     DataBinder binder = new DataBinder();
/* 239 */     binder.setLocalData(props);
/* 240 */     DataBinder.mergeHashTables(props, taskMap);
/*     */ 
/* 242 */     String rsName = taskMap.get("sjName") + "_" + "JobParameters";
/* 243 */     DataResultSet drset = (DataResultSet)this.m_inputData.getResultSet(rsName);
/* 244 */     binder.addResultSet("JobParameters", drset);
/* 245 */     return binder;
/*     */   }
/*     */ 
/*     */   public void performJobs() throws ServiceException, DataException
/*     */   {
/* 250 */     ScheduledJobsProcessor sJobs = new ScheduledJobsProcessor();
/* 251 */     DataBinder binder = new DataBinder();
/* 252 */     binder.putLocal("dSjQueueType", "S");
/* 253 */     sJobs.processJobs(this.m_workspace, binder, this.m_context);
/*     */   }
/*     */ 
/*     */   public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 260 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 265 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79188 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.test.SJobTestDriver
 * JD-Core Version:    0.5.4
 */