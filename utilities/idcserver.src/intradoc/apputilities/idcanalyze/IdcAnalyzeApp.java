/*      */ package intradoc.apputilities.idcanalyze;
/*      */ 
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.IdcSystemLoader;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.FileWriter;
/*      */ import java.io.IOException;
/*      */ import java.io.PrintStream;
/*      */ import java.io.Writer;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JButton;
/*      */ 
/*      */ public class IdcAnalyzeApp
/*      */ {
/*      */   public static final int F_APPEND_NEWLINE = 1;
/*   46 */   public static final String[] ARGS = { "!csIDCAnalyzeArgs15", "!csIDCAnalyzeArgs1", "!csIDCAnalyzeArgs2", "!csIDCAnalyzeArgs3", "!csIDCAnalyzeArgs4", "!csIDCAnalyzeArgs5", "!csIDCAnalyzeArgs9", "!csIDCAnalyzeArgs14", "!csIDCAnalyzeArgs16", "!csIDCAnalyzeArgs17", "!csIDCAnalyzeArgs10", "!csIDCAnalyzeArgs11", "!csIDCAnalyzeArgs12", "!csIDCAnalyzeArgs13" };
/*      */ 
/*   64 */   String[][] DEFAULT_ENV = { { "IdcAnalyzeLogDir", "logs/" }, { "IdcAnalyzeLogName", "IdcAnalyze.log" }, { "IdcCommandFile", "IdcCommand.txt" }, { "TempDir", "temp/" }, { "BulkLoadFile", "BulkLoad.txt" }, { "ExtraFilesLog", "ExtraFiles.txt" } };
/*      */   protected IdcAnalyzeFrame m_frame;
/*      */   protected Workspace m_workspace;
/*      */   protected Workspace m_userWorkspace;
/*      */   protected boolean m_initialized;
/*      */   protected boolean m_isBackground;
/*      */   protected SystemPropertiesEditor m_propLoader;
/*      */   protected Properties m_idcProperties;
/*      */   protected Properties m_cfgProperties;
/*   83 */   protected IdcAnalyzeApp m_app = this;
/*      */   protected boolean m_analyzerStarted;
/*      */   protected boolean m_isExiting;
/*      */   protected boolean m_cancelAnalysis;
/*      */   protected int m_taskProgress;
/*   90 */   protected int m_overallProgress = 0;
/*      */ 
/*   92 */   protected int m_totalErrors = 0;
/*      */   public DataResultSet m_defaultTaskList;
/*      */   public DataResultSet m_taskOptionList;
/*      */   public Vector m_selectedTasks;
/*      */   protected Hashtable m_logFiles;
/*      */ 
/*      */   public IdcAnalyzeApp(boolean isBackground)
/*      */   {
/*  102 */     this.m_initialized = false;
/*  103 */     this.m_isBackground = isBackground;
/*  104 */     this.m_propLoader = new SystemPropertiesEditor();
/*      */   }
/*      */ 
/*      */   public void init() throws ServiceException
/*      */   {
/*  109 */     this.m_idcProperties = new Properties();
/*      */     try
/*      */     {
/*  113 */       SharedObjects.putEnvironmentValue("EnableSchemaPublish", "false");
/*  114 */       this.m_workspace = IdcSystemLoader.loadDatabase(2);
/*      */ 
/*  116 */       this.m_userWorkspace = IdcSystemLoader.loadSystemUserDatabase(2);
/*      */ 
/*  118 */       if (this.m_isBackground)
/*      */       {
/*  122 */         IdcSystemLoader.loadCaches(this.m_workspace);
/*  123 */         IdcSystemLoader.registerProviders();
/*  124 */         IdcSystemLoader.startProviders(true);
/*      */       }
/*      */     }
/*      */     catch (DataException d)
/*      */     {
/*  129 */       throw new ServiceException(d);
/*      */     }
/*      */ 
/*  132 */     String[] args = (String[])(String[])AppObjectRepository.getObject("CommandLine");
/*      */ 
/*  135 */     readIntradocCfgFile();
/*      */ 
/*  138 */     for (int i = 0; i < this.DEFAULT_ENV.length; ++i)
/*      */     {
/*  140 */       String val = (String)this.m_idcProperties.get(this.DEFAULT_ENV[i][0]);
/*  141 */       if (val == null)
/*      */       {
/*  143 */         val = this.DEFAULT_ENV[i][1];
/*      */       }
/*  145 */       this.m_idcProperties.put(this.DEFAULT_ENV[i][0], val);
/*      */     }
/*  147 */     readArgs(args);
/*      */ 
/*  149 */     this.m_defaultTaskList = new DataResultSet();
/*  150 */     this.m_taskOptionList = new DataResultSet();
/*  151 */     this.m_selectedTasks = new IdcVector();
/*  152 */     this.m_logFiles = new Hashtable();
/*  153 */     loadTasks();
/*      */     try
/*      */     {
/*  157 */       Vector list = Providers.getProvidersOfType("FileStore");
/*      */ 
/*  160 */       for (int i = 0; i < list.size(); ++i)
/*      */       {
/*  162 */         Provider p = (Provider)list.elementAt(i);
/*  163 */         p.startProvider(true);
/*      */       }
/*      */     }
/*      */     catch (DataException d)
/*      */     {
/*  168 */       throw new ServiceException(d);
/*      */     }
/*      */ 
/*  171 */     this.m_initialized = true;
/*      */   }
/*      */ 
/*      */   public void analyze()
/*      */     throws DataException, ServiceException
/*      */   {
/*  181 */     if (!this.m_initialized)
/*      */     {
/*  183 */       init();
/*      */     }
/*      */ 
/*  186 */     if (!checkLogDir())
/*      */     {
/*  188 */       return;
/*      */     }
/*      */ 
/*  191 */     if (!this.m_isBackground)
/*      */     {
/*  193 */       this.m_frame.resetOutputFrame("");
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  198 */       determineRange();
/*      */     }
/*      */     catch (DataException d)
/*      */     {
/*  202 */       error(LocaleUtils.encodeMessage("csIDCAnalyzeUnableToDetermineRange", d.getMessage()));
/*      */ 
/*  204 */       if (!this.m_isBackground)
/*      */       {
/*  206 */         cancelAnalysis();
/*      */       }
/*  208 */       return;
/*      */     }
/*      */ 
/*  211 */     recordState();
/*      */ 
/*  213 */     createTaskList();
/*      */ 
/*  216 */     String startID = (String)this.m_idcProperties.get("StartID");
/*  217 */     if (startID != null)
/*      */     {
/*  219 */       String endID = (String)this.m_idcProperties.get("EndID");
/*  220 */       if (endID != null)
/*      */       {
/*  222 */         log(LocaleUtils.encodeMessage("csIDCAnalyzeReportRangeStartEnd", null, startID, endID));
/*      */       }
/*      */       else
/*      */       {
/*  228 */         log(LocaleUtils.encodeMessage("csIDCAnalyzeReportRangeStart", null, startID));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  234 */     Runnable bg = new Runnable()
/*      */     {
/*      */       public void run()
/*      */       {
/*  238 */         IdcAnalyzeApp.this.m_totalErrors = 0;
/*  239 */         String error = null;
/*      */         try
/*      */         {
/*  242 */           for (int i = 0; i < IdcAnalyzeApp.this.m_selectedTasks.size(); ++i)
/*      */           {
/*  244 */             if (IdcAnalyzeApp.this.m_cancelAnalysis) {
/*      */               break;
/*      */             }
/*      */ 
/*  248 */             String key = (String)IdcAnalyzeApp.this.m_selectedTasks.elementAt(i);
/*  249 */             Vector row = IdcAnalyzeApp.this.m_defaultTaskList.findRow(0, key);
/*  250 */             if (row == null)
/*      */             {
/*      */               continue;
/*      */             }
/*      */ 
/*  255 */             String name = (String)row.elementAt(0);
/*  256 */             String className = (String)row.elementAt(2);
/*  257 */             error = (String)row.elementAt(4);
/*      */ 
/*  259 */             if (name == null) continue; if (className == null)
/*      */             {
/*      */               continue;
/*      */             }
/*      */ 
/*  266 */             IdcAnalyzeTask task = (IdcAnalyzeTask)ComponentClassFactory.createClassInstance(name, className, "");
/*      */ 
/*  269 */             IdcAnalyzeApp.this.m_taskProgress = 0;
/*      */ 
/*  272 */             task.init(IdcAnalyzeApp.this.m_app, IdcAnalyzeApp.this.m_idcProperties, IdcAnalyzeApp.this.m_workspace);
/*      */ 
/*  275 */             task.doTask();
/*      */ 
/*  277 */             IdcAnalyzeApp.this.m_totalErrors += task.getErrorCount();
/*      */           }
/*      */ 
/*  282 */           boolean errorFileCreated = IdcAnalyzeApp.this.writeFixupFile();
/*      */ 
/*  284 */           if (!IdcAnalyzeApp.this.m_isBackground)
/*      */           {
/*  286 */             IdcAnalyzeApp.this.m_frame.reportOverallProgress(1, null, 80.0F, 80.0F);
/*      */           }
/*  288 */           IdcAnalyzeApp.this.displayDone(errorFileCreated);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  292 */           if (error == null)
/*      */           {
/*  294 */             error = "csIDCAnalyzeError";
/*      */           }
/*  296 */           IdcAnalyzeApp.this.error(LocaleUtils.encodeMessage(error, e.getMessage()));
/*  297 */           IdcAnalyzeApp.this.m_analyzerStarted = false;
/*  298 */           IdcAnalyzeApp.this.m_isExiting = true;
/*      */         }
/*      */         finally
/*      */         {
/*  302 */           IdcAnalyzeApp.this.closeLogs();
/*  303 */           IdcAnalyzeApp.this.m_workspace.releaseConnection();
/*  304 */           if (!IdcAnalyzeApp.this.m_isBackground)
/*      */           {
/*  306 */             IdcAnalyzeApp.this.m_frame.m_loadBtn.setEnabled(true);
/*  307 */             IdcAnalyzeApp.this.m_frame.m_cancelBtn.setEnabled(false);
/*  308 */             IdcAnalyzeApp.this.reset();
/*      */           }
/*      */           else
/*      */           {
/*  312 */             System.exit(0);
/*      */           }
/*      */         }
/*      */       }
/*      */     };
/*  319 */     this.m_analyzerStarted = true;
/*  320 */     this.m_isExiting = false;
/*  321 */     if (!this.m_isBackground)
/*      */     {
/*  323 */       this.m_frame.m_loadBtn.setEnabled(false);
/*  324 */       this.m_frame.m_cancelBtn.setEnabled(true);
/*      */     }
/*      */ 
/*  327 */     Thread bgThread = new Thread(bg);
/*  328 */     bgThread.start();
/*      */   }
/*      */ 
/*      */   public void cancelAnalysis()
/*      */   {
/*  333 */     this.m_cancelAnalysis = true;
/*  334 */     this.m_frame.m_cancelBtn.setEnabled(false);
/*      */   }
/*      */ 
/*      */   protected void recordState()
/*      */   {
/*  339 */     String endID = (String)this.m_idcProperties.get("EndID");
/*  340 */     if (endID == null)
/*      */     {
/*  342 */       return;
/*      */     }
/*  344 */     int endNum = NumberUtils.parseInteger(endID, -1);
/*  345 */     if (endNum < 0)
/*      */     {
/*  347 */       return;
/*      */     }
/*      */ 
/*  350 */     DataBinder binder = new DataBinder();
/*  351 */     binder.putLocal("IdcAnalyzeResumeID", "" + (endNum + 1));
/*      */ 
/*  353 */     String logDir = (String)this.m_idcProperties.get("IdcAnalyzeLogDir");
/*  354 */     String logFile = "state.hda";
/*      */ 
/*  358 */     DataResultSet taskSet = SharedObjects.getTable("IdcAnalyzeTasks");
/*  359 */     for (taskSet.first(); taskSet.isRowPresent(); taskSet.next())
/*      */     {
/*  361 */       String id = ResultSetUtils.getValue(taskSet, "id");
/*  362 */       String val = (String)this.m_idcProperties.get(id);
/*  363 */       if (val != null)
/*      */       {
/*  365 */         binder.putLocal(id, val);
/*      */       }
/*  367 */       String optListStr = ResultSetUtils.getValue(taskSet, "options");
/*  368 */       Vector v = StringUtils.parseArray(optListStr, ',', '^');
/*  369 */       for (int i = 0; i < v.size(); ++i)
/*      */       {
/*  371 */         id = (String)v.elementAt(i);
/*  372 */         val = (String)this.m_idcProperties.get(id);
/*  373 */         if (val == null)
/*      */           continue;
/*  375 */         binder.putLocal(id, val);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  382 */       ResourceUtils.serializeDataBinder(logDir, logFile, binder, true, false);
/*      */     }
/*      */     catch (ServiceException s)
/*      */     {
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void createTaskList()
/*      */   {
/*  392 */     for (this.m_defaultTaskList.first(); this.m_defaultTaskList.isRowPresent(); this.m_defaultTaskList.next())
/*      */     {
/*  394 */       Properties props = this.m_defaultTaskList.getCurrentRowProps();
/*  395 */       String id = (String)props.get("id");
/*  396 */       if (!StringUtils.convertToBool((String)this.m_idcProperties.get(id), false))
/*      */         continue;
/*  398 */       this.m_selectedTasks.addElement(id);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void loadTasks()
/*      */     throws ServiceException
/*      */   {
/*  407 */     ResultSet tasks = SharedObjects.getTable("IdcAnalyzeTasks");
/*  408 */     if (tasks != null)
/*      */     {
/*  410 */       this.m_defaultTaskList.copy(tasks);
/*      */     }
/*      */ 
/*  416 */     ResultSet options = SharedObjects.getTable("IdcAnalyzeTaskOptions");
/*  417 */     if (options == null)
/*      */       return;
/*  419 */     this.m_taskOptionList.copy(options);
/*      */   }
/*      */ 
/*      */   protected void displayDone(boolean errorFileCreated)
/*      */   {
/*  429 */     IdcMessage msg = null;
/*  430 */     if (errorFileCreated)
/*      */     {
/*  432 */       String logDir = (String)this.m_idcProperties.get("IdcAnalyzeLogDir");
/*  433 */       msg = IdcMessageFactory.lc(msg, "csIDCAnalyzeErrorFileCreated", new Object[] { logDir + computeCleanupFile() });
/*      */ 
/*  435 */       msg = IdcMessageFactory.lc(msg, "csLinefeed", new Object[0]);
/*      */     }
/*  437 */     msg = IdcMessageFactory.lc(msg, "csIDCAnalyzeFinished", new Object[] { Integer.valueOf(this.m_totalErrors) });
/*  438 */     msg = IdcMessageFactory.lc(msg, "csLinefeed", new Object[0]);
/*      */ 
/*  440 */     this.m_analyzerStarted = false;
/*  441 */     this.m_isExiting = true;
/*      */ 
/*  443 */     Report.info(null, null, msg);
/*      */ 
/*  445 */     if (this.m_isBackground)
/*      */     {
/*  447 */       log(msg);
/*      */     }
/*      */     else
/*      */     {
/*  451 */       if (MessageBox.doMessage(this.m_frame.m_appHelper, msg, 1) == 1)
/*      */       {
/*  453 */         this.m_frame.reportProgress(1, null, 0.0F, 100.0F);
/*  454 */         this.m_frame.reportOverallProgress(1, null, 0.0F, 100.0F);
/*      */       }
/*  456 */       this.m_frame.changeCheckboxState(true);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void readArgs(String[] args)
/*      */   {
/*  466 */     boolean doAll = false;
/*  467 */     Properties provisionalProps = null;
/*  468 */     boolean taskSpecified = false;
/*      */ 
/*  470 */     if ((args.length == 0) || ((args.length == 1) && (args[0].equalsIgnoreCase("-console"))))
/*      */     {
/*  473 */       System.err.println(LocaleResources.getString("syTooFewArguments", null));
/*  474 */       usage();
/*  475 */       System.exit(0);
/*      */     }
/*      */ 
/*  478 */     for (int i = 0; i < args.length; ++i)
/*      */     {
/*  480 */       String arg = args[i];
/*  481 */       if ((arg.equalsIgnoreCase("-help")) || (arg.equalsIgnoreCase("-h")))
/*      */       {
/*  483 */         usage();
/*  484 */         System.exit(0);
/*      */       }
/*  486 */       else if ((arg.equalsIgnoreCase("-cleandatabase")) || (arg.equalsIgnoreCase("-clean")))
/*      */       {
/*  489 */         this.m_idcProperties.put("CleanDatabase", "1");
/*      */ 
/*  491 */         this.m_idcProperties.put("DatabaseCheck", "1");
/*      */       }
/*  493 */       else if ((arg.equalsIgnoreCase("-deletefiles")) || (arg.equalsIgnoreCase("-delete")))
/*      */       {
/*  496 */         this.m_idcProperties.put("DeleteExtra", "1");
/*      */ 
/*  498 */         this.m_idcProperties.put("FileSystemCheck", "1");
/*      */       }
/*  500 */       else if ((arg.equalsIgnoreCase("-safedeletefiles")) || (arg.equalsIgnoreCase("-safedelete")))
/*      */       {
/*  503 */         this.m_idcProperties.put("SafeDeleteExtra", "1");
/*      */ 
/*  505 */         this.m_idcProperties.put("FileSystemCheck", "1");
/*      */ 
/*  507 */         this.m_idcProperties.put("CheckExtra", "1");
/*      */       }
/*  509 */       else if (arg.equalsIgnoreCase("-report"))
/*      */       {
/*  511 */         taskSpecified = true;
/*  512 */         this.m_idcProperties.put("IdcAnalyzeReport", "1");
/*      */       }
/*  514 */       else if (arg.equalsIgnoreCase("-index"))
/*      */       {
/*  516 */         taskSpecified = true;
/*  517 */         this.m_idcProperties.put("IndexCheck", "1");
/*      */       }
/*  519 */       else if (arg.equalsIgnoreCase("-cleanindex"))
/*      */       {
/*  521 */         this.m_idcProperties.put("IndexCheck", "1");
/*  522 */         this.m_idcProperties.put("CleanIndex", "1");
/*      */       }
/*  524 */       else if (arg.equalsIgnoreCase("-filesystem"))
/*      */       {
/*  526 */         taskSpecified = true;
/*  527 */         this.m_idcProperties.put("FileSystemCheck", "1");
/*      */       }
/*  529 */       else if (arg.equalsIgnoreCase("-extra"))
/*      */       {
/*  531 */         this.m_idcProperties.put("CheckExtra", "1");
/*      */ 
/*  533 */         this.m_idcProperties.put("FileSystemCheck", "1");
/*      */       }
/*  535 */       else if ((arg.equalsIgnoreCase("-database")) || (arg.equalsIgnoreCase("-dbcheck")))
/*      */       {
/*  538 */         taskSpecified = true;
/*  539 */         this.m_idcProperties.put("DatabaseCheck", "1");
/*  540 */         this.m_idcProperties.put("CheckRevClassID", "1");
/*      */       }
/*  542 */       else if ((arg.equalsIgnoreCase("-norevclassid")) || (arg.equalsIgnoreCase("-norevclass")))
/*      */       {
/*  545 */         this.m_idcProperties.put("CheckRevClassID", "0");
/*      */       }
/*  547 */       else if (arg.equalsIgnoreCase("-verbose"))
/*      */       {
/*  550 */         SystemUtils.addAsActiveTrace("analyzer");
/*      */       }
/*  552 */       else if (arg.equalsIgnoreCase("-doall"))
/*      */       {
/*  554 */         taskSpecified = true;
/*  555 */         doAll = true;
/*      */       }
/*  557 */       else if (arg.equalsIgnoreCase("-resume"))
/*      */       {
/*  559 */         DataBinder data = new DataBinder();
/*  560 */         String logDir = (String)this.m_idcProperties.get("IdcAnalyzeLogDir");
/*  561 */         String logFile = "state.hda";
/*      */ 
/*  563 */         String resumeID = null;
/*      */         try
/*      */         {
/*  566 */           ResourceUtils.serializeDataBinder(logDir, logFile, data, false, false);
/*  567 */           resumeID = data.getLocal("IdcAnalyzeResumeID");
/*      */         }
/*      */         catch (ServiceException s)
/*      */         {
/*      */         }
/*      */ 
/*  577 */         provisionalProps = data.getLocalData();
/*      */ 
/*  579 */         if (resumeID != null)
/*      */         {
/*  581 */           this.m_idcProperties.put("IdcAnalyzeResumeID", resumeID);
/*      */         }
/*      */       }
/*  584 */       else if (arg.equalsIgnoreCase("-limit"))
/*      */       {
/*  586 */         ++i;
/*      */ 
/*  588 */         if (i >= args.length)
/*      */         {
/*  590 */           System.err.println(LocaleResources.getString("syTooFewArguments", null));
/*  591 */           usage();
/*  592 */           System.exit(0);
/*      */         }
/*      */ 
/*  595 */         String range = args[i];
/*  596 */         this.m_idcProperties.put("IdcAnalyzeLimit", range);
/*      */       } else {
/*  598 */         if (!arg.equalsIgnoreCase("-range"))
/*      */           continue;
/*  600 */         ++i;
/*      */ 
/*  602 */         if (i >= args.length)
/*      */         {
/*  604 */           System.err.println(LocaleResources.getString("syTooFewArguments", null));
/*  605 */           usage();
/*  606 */           System.exit(0);
/*      */         }
/*      */ 
/*  611 */         String range = args[i];
/*  612 */         this.m_idcProperties.put("IdcAnalyzeRange", range);
/*      */       }
/*      */     }
/*  615 */     if (doAll)
/*      */     {
/*  617 */       this.m_idcProperties.put("IdcAnalyzeReport", "1");
/*  618 */       this.m_idcProperties.put("DatabaseCheck", "1");
/*  619 */       this.m_idcProperties.put("CheckRevClassID", "1");
/*  620 */       this.m_idcProperties.put("IndexCheck", "1");
/*  621 */       this.m_idcProperties.put("FileSystemCheck", "1");
/*      */     } else {
/*  623 */       if ((taskSpecified) || (provisionalProps == null)) {
/*      */         return;
/*      */       }
/*      */ 
/*  627 */       DataBinder.mergeHashTables(this.m_idcProperties, provisionalProps);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setProgress(int progress)
/*      */   {
/*  633 */     this.m_taskProgress = progress;
/*  634 */     if (this.m_isBackground)
/*      */       return;
/*  636 */     this.m_frame.reportProgress(1, null, this.m_taskProgress, 80.0F);
/*      */   }
/*      */ 
/*      */   public void incProgress()
/*      */   {
/*  642 */     incProgress(1);
/*      */   }
/*      */ 
/*      */   public void incProgress(int ticks)
/*      */   {
/*  647 */     for (int i = 0; i < ticks; ++i)
/*      */     {
/*  649 */       if (this.m_isBackground)
/*      */       {
/*  651 */         System.out.print(".");
/*      */       }
/*      */       else
/*      */       {
/*  655 */         this.m_taskProgress += 1;
/*  656 */         this.m_frame.reportProgress(1, null, this.m_taskProgress, 80.0F);
/*      */ 
/*  658 */         if (this.m_taskProgress % this.m_selectedTasks.size() == 0)
/*      */         {
/*  660 */           this.m_overallProgress += 1;
/*      */         }
/*  662 */         this.m_frame.reportOverallProgress(1, null, this.m_overallProgress, 80.0F);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateProgressMessage(String msg)
/*      */   {
/*  669 */     if (this.m_isBackground)
/*      */     {
/*  671 */       System.out.print(LocaleResources.getString(msg, null));
/*      */     }
/*      */     else
/*      */     {
/*  675 */       this.m_frame.reportProgress(1, msg, this.m_taskProgress, 80.0F);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void message(String str)
/*      */   {
/*  683 */     messageEx(str, true);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void messageStraight(String str)
/*      */   {
/*  690 */     messageEx(str, false);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void messageEx(String str, boolean doLineFeed)
/*      */   {
/*  697 */     if (this.m_isBackground)
/*      */     {
/*  699 */       System.out.print(LocaleResources.localizeMessage(str, null));
/*  700 */       if (!doLineFeed)
/*      */         return;
/*  702 */       System.out.print(LocaleResources.localizeMessage("!csLinefeed", null));
/*      */     }
/*      */     else
/*      */     {
/*  707 */       this.m_frame.appendToOutputFrame(LocaleResources.localizeMessage(str, null));
/*  708 */       if (!doLineFeed)
/*      */         return;
/*  710 */       this.m_frame.appendToOutputFrame(LocaleResources.localizeMessage("!csLinefeed", null));
/*      */     }
/*      */   }
/*      */ 
/*      */   public void message(IdcMessage msg)
/*      */   {
/*  717 */     message(msg, 1);
/*      */   }
/*      */ 
/*      */   public void messageStraight(IdcMessage msg)
/*      */   {
/*  722 */     message(msg, 0);
/*      */   }
/*      */ 
/*      */   public void message(IdcMessage msg, int flags)
/*      */   {
/*  727 */     boolean doLineFeed = (flags & 0x1) != 0;
/*  728 */     if (this.m_isBackground)
/*      */     {
/*  730 */       System.out.print(LocaleResources.localizeMessage(null, msg, null));
/*  731 */       if (!doLineFeed)
/*      */         return;
/*  733 */       System.out.print(LocaleResources.getString("csLinefeed", null));
/*      */     }
/*      */     else
/*      */     {
/*  738 */       this.m_frame.appendToOutputFrame(LocaleResources.localizeMessage(null, msg, null).toString());
/*  739 */       if (!doLineFeed)
/*      */         return;
/*  741 */       this.m_frame.appendToOutputFrame(LocaleResources.getString("csLinefeed", null));
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void error(String str)
/*      */   {
/*  751 */     if ((str == null) || (str.length() == 0))
/*      */     {
/*  753 */       return;
/*      */     }
/*  755 */     IdcMessage msg = IdcMessageFactory.lc();
/*  756 */     msg.m_msgEncoded = str;
/*  757 */     error(msg);
/*      */   }
/*      */ 
/*      */   public void error(Throwable t, IdcMessage msg)
/*      */   {
/*  762 */     msg.m_prior = IdcMessageFactory.lc(t);
/*  763 */     error(msg);
/*      */   }
/*      */ 
/*      */   public void error(IdcMessage msg)
/*      */   {
/*  768 */     if (this.m_isBackground)
/*      */     {
/*  770 */       System.err.println(LocaleResources.localizeMessage(null, msg, null).toString());
/*      */     }
/*      */     else
/*      */     {
/*  775 */       reportError(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void reset()
/*      */   {
/*  781 */     this.m_selectedTasks.removeAllElements();
/*  782 */     this.m_overallProgress = 0;
/*  783 */     this.m_cancelAnalysis = false;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void reportError(String msg)
/*      */   {
/*  790 */     if ((msg == null) || (msg.length() == 0))
/*      */     {
/*  792 */       return;
/*      */     }
/*  794 */     IdcMessage idcmsg = IdcMessageFactory.lc();
/*  795 */     idcmsg.m_msgEncoded = msg;
/*  796 */     reportError(idcmsg);
/*      */   }
/*      */ 
/*      */   public void reportError(IdcMessage msg)
/*      */   {
/*  801 */     MessageBox.reportError(this.m_frame.m_appHelper, this.m_frame, msg, IdcMessageFactory.lc("csIDCAnalyzeMessage", new Object[0]));
/*      */   }
/*      */ 
/*      */   protected static void usage()
/*      */   {
/*  807 */     System.err.println(LocaleResources.getString("csIDCAnalyzeUsage", null));
/*  808 */     System.err.println("");
/*  809 */     for (int j = 0; j < ARGS.length; ++j)
/*      */     {
/*  811 */       System.err.println(LocaleResources.localizeMessage(ARGS[j], null));
/*      */     }
/*  813 */     System.exit(0);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void logStraight(String msg)
/*      */   {
/*  820 */     String val = (String)this.m_idcProperties.get("IdcAnalyzeLogName");
/*  821 */     logStraight(val, msg);
/*  822 */     messageStraight(msg);
/*      */   }
/*      */ 
/*      */   public void logStraight(IdcMessage msg)
/*      */   {
/*  827 */     String val = (String)this.m_idcProperties.get("IdcAnalyzeLogName");
/*  828 */     logStraight(val, msg);
/*  829 */     messageStraight(msg);
/*      */   }
/*      */ 
/*      */   public void writePlain(String file, String text)
/*      */   {
/*  834 */     IdcMessage msg = IdcMessageFactory.lc();
/*  835 */     msg.m_msgLocalized = text;
/*  836 */     log(file, msg);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void log(String msg)
/*      */   {
/*  843 */     String val = (String)this.m_idcProperties.get("IdcAnalyzeLogName");
/*  844 */     log(val, msg);
/*  845 */     message(msg);
/*      */   }
/*      */ 
/*      */   public void log(IdcMessage msg)
/*      */   {
/*  850 */     String val = (String)this.m_idcProperties.get("IdcAnalyzeLogName");
/*  851 */     log(val, msg);
/*  852 */     message(msg);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void logStraight(String logFile, String msg)
/*      */   {
/*  859 */     logEx(logFile, msg, false);
/*      */   }
/*      */ 
/*      */   public void logStraight(String logFile, IdcMessage msg)
/*      */   {
/*  864 */     logEx(logFile, msg, false);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void log(String logFile, String msg)
/*      */   {
/*  871 */     logEx(logFile, msg, true);
/*      */   }
/*      */ 
/*      */   public void log(String logFile, IdcMessage msg)
/*      */   {
/*  876 */     logEx(logFile, msg, true);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void logEx(String logFile, String msg, boolean endLine)
/*      */   {
/*  883 */     IdcMessage msgObject = IdcMessageFactory.lc();
/*  884 */     msgObject.m_msgEncoded = msg;
/*  885 */     logEx(logFile, msgObject, endLine);
/*      */   }
/*      */ 
/*      */   public void logEx(String logFile, IdcMessage msg, boolean endLine)
/*      */   {
/*  890 */     Writer w = getLogWriter(logFile);
/*      */     try
/*      */     {
/*  893 */       if (w == null)
/*      */       {
/*  895 */         String logDir = (String)this.m_idcProperties.get("IdcAnalyzeLogDir");
/*  896 */         w = new BufferedWriter(new FileWriter(logDir + "/" + logFile, false));
/*  897 */         putLogWriter(logFile, w);
/*      */       }
/*  899 */       w.write(LocaleResources.localizeMessage(null, msg, null).toString());
/*  900 */       if (endLine)
/*      */       {
/*  902 */         w.write(LocaleResources.localizeMessage("!csLinefeed", null));
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  907 */       IdcMessage idcmsg = IdcMessageFactory.lc(e, "csIDCAnalyzeLogWriteError", new Object[] { logFile });
/*  908 */       String text = LocaleResources.localizeMessage(null, idcmsg, null).toString();
/*  909 */       SystemUtils.errln(text);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void cleanLogDir()
/*      */   {
/*  915 */     String logDir = (String)this.m_idcProperties.get("IdcAnalyzeLogDir");
/*  916 */     String[] logFiles = FileUtils.getMatchingFileNames(logDir, "*");
/*  917 */     for (int i = 0; i < logFiles.length; ++i)
/*      */     {
/*  919 */       FileUtils.deleteFile(logFiles[i]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void putLogWriter(String logName, Writer w)
/*      */   {
/*  925 */     this.m_logFiles.put(logName, w);
/*      */   }
/*      */ 
/*      */   public Writer getLogWriter(String logName)
/*      */   {
/*  930 */     return (Writer)this.m_logFiles.get(logName);
/*      */   }
/*      */ 
/*      */   public void debug(String msg)
/*      */   {
/*  935 */     Report.trace("analyzer", msg, null);
/*      */   }
/*      */ 
/*      */   public boolean isDebug()
/*      */   {
/*  940 */     return true;
/*      */   }
/*      */ 
/*      */   public String computeCleanupFile()
/*      */   {
/*  945 */     String cleanupFileName = "cleanup.bat";
/*      */ 
/*  947 */     if (EnvUtils.isFamily("unix"))
/*      */     {
/*  949 */       cleanupFileName = "cleanup.sh";
/*      */     }
/*      */ 
/*  952 */     return cleanupFileName;
/*      */   }
/*      */ 
/*      */   public boolean writeFixupFile()
/*      */   {
/*  957 */     boolean fileCreated = true;
/*  958 */     String logDir = (String)this.m_idcProperties.get("IdcAnalyzeLogDir");
/*      */     try
/*      */     {
/*  961 */       String cleanupFileName = computeCleanupFile();
/*  962 */       String cleanupFileHeader = "";
/*  963 */       if (EnvUtils.isFamily("unix"))
/*      */       {
/*  965 */         cleanupFileHeader = "#!/bin/sh";
/*      */       }
/*      */ 
/*  968 */       String logName = (String)this.m_idcProperties.get("IdcCommandFile");
/*  969 */       FileUtils.validateFile(logDir + logName, "");
/*      */ 
/*  974 */       String binDir = SystemUtils.getBinDir();
/*  975 */       log(cleanupFileName, cleanupFileHeader);
/*  976 */       log(cleanupFileName, binDir + "/IdcCommand -f " + FileUtils.getAbsolutePath(binDir, new StringBuilder().append(logDir).append(logName).toString()) + " -u sysadmin -c standalone");
/*      */     }
/*      */     catch (ServiceException ignore)
/*      */     {
/*  982 */       fileCreated = false;
/*      */     }
/*  984 */     return fileCreated;
/*      */   }
/*      */ 
/*      */   public boolean closeLogs()
/*      */   {
/*  989 */     boolean allSuccessful = true;
/*      */ 
/*  991 */     for (Enumeration e = this.m_logFiles.keys(); e.hasMoreElements(); )
/*      */     {
/*  993 */       String key = (String)e.nextElement();
/*  994 */       Writer w = (Writer)this.m_logFiles.remove(key);
/*  995 */       if (w != null)
/*      */       {
/*      */         try
/*      */         {
/*  999 */           w.close();
/*      */         }
/*      */         catch (Exception ignore)
/*      */         {
/* 1003 */           allSuccessful = false;
/*      */         }
/*      */       }
/*      */     }
/* 1007 */     return allSuccessful;
/*      */   }
/*      */ 
/*      */   public boolean checkLogDir()
/*      */   {
/* 1012 */     boolean isLogDirOK = true;
/*      */ 
/* 1015 */     String logDir = (String)this.m_idcProperties.get("IdcAnalyzeLogDir");
/* 1016 */     logDir = FileUtils.directorySlashes(logDir);
/* 1017 */     if (logDir != null)
/*      */     {
/* 1019 */       String defPath = SystemUtils.getBinDir();
/* 1020 */       logDir = FileUtils.getAbsolutePath(defPath, logDir);
/*      */ 
/* 1022 */       this.m_idcProperties.put("IdcAnalyzeLogDir", logDir);
/*      */     }
/* 1024 */     File logDirFile = new File(logDir);
/* 1025 */     if (logDirFile.exists())
/*      */     {
/* 1027 */       if (!logDirFile.isDirectory())
/*      */       {
/* 1030 */         System.err.println(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csIDCAnalyzeEmptyLogDir", null, logDir), null));
/*      */ 
/* 1032 */         isLogDirOK = false;
/*      */       }
/*      */       else
/*      */       {
/* 1036 */         String savedLogDir = logDir;
/* 1037 */         if (savedLogDir.charAt(savedLogDir.length() - 1) == '/')
/*      */         {
/* 1039 */           savedLogDir = savedLogDir.substring(0, savedLogDir.length() - 1);
/*      */         }
/* 1041 */         savedLogDir = savedLogDir + "-" + System.currentTimeMillis();
/*      */         try
/*      */         {
/* 1044 */           FileUtils.renameFile(logDir, savedLogDir);
/*      */         }
/*      */         catch (ServiceException s)
/*      */         {
/* 1048 */           IdcMessage idcMsg = IdcMessageFactory.lc(s, "csIDCAnalyzeCannotMoveLogDirError", new Object[0]);
/* 1049 */           String text = LocaleResources.localizeMessage(null, idcMsg, null).toString();
/* 1050 */           SystemUtils.errln(text);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1057 */       FileUtils.checkOrCreateDirectory(logDir, 5);
/*      */     }
/*      */     catch (ServiceException s)
/*      */     {
/* 1061 */       IdcMessage idcMsg = IdcMessageFactory.lc(s, "csIDCAnalyzeCreateLogDirError", new Object[0]);
/* 1062 */       String text = LocaleResources.localizeMessage(null, idcMsg, null).toString();
/* 1063 */       SystemUtils.errln(text);
/*      */     }
/* 1065 */     return isLogDirOK;
/*      */   }
/*      */ 
/*      */   public void determineRange() throws DataException
/*      */   {
/* 1070 */     String startRange = null;
/* 1071 */     String endRange = null;
/* 1072 */     String origStartRange = null;
/* 1073 */     String origEndRange = null;
/*      */ 
/* 1076 */     String resumeID = (String)this.m_idcProperties.get("IdcAnalyzeResumeID");
/* 1077 */     String limit = (String)this.m_idcProperties.get("IdcAnalyzeLimit");
/*      */ 
/* 1079 */     String range = (String)this.m_idcProperties.get("IdcAnalyzeRange");
/*      */ 
/* 1081 */     if (resumeID != null)
/*      */     {
/* 1083 */       origStartRange = startRange = resumeID;
/*      */     }
/* 1085 */     else if (range != null)
/*      */     {
/* 1087 */       int index = range.indexOf(58);
/* 1088 */       if (index >= 0)
/*      */       {
/* 1090 */         origStartRange = startRange = range.substring(0, index);
/* 1091 */         origEndRange = endRange = range.substring(index + 1);
/*      */       }
/*      */       else
/*      */       {
/* 1095 */         origStartRange = startRange = range;
/*      */       }
/*      */     }
/* 1098 */     else if (limit != null)
/*      */     {
/* 1100 */       origStartRange = startRange = "0";
/*      */     }
/*      */ 
/* 1103 */     if (origStartRange == null)
/*      */     {
/* 1105 */       return;
/*      */     }
/*      */ 
/* 1108 */     if (startRange.indexOf(123) >= 0)
/*      */     {
/* 1110 */       Properties dateProps = new Properties();
/* 1111 */       String startDate = startRange.substring(1, startRange.length() - 1);
/* 1112 */       String endDate = null;
/* 1113 */       if ((endRange != null) && (endRange.length() > 0))
/*      */       {
/* 1115 */         endDate = endRange.substring(1, endRange.length() - 1);
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/* 1120 */         Date d = LocaleResources.parseDate(startDate, null);
/* 1121 */         String date = LocaleUtils.formatODBC(d);
/* 1122 */         dateProps.put("startDate", date);
/*      */ 
/* 1124 */         if (endDate != null)
/*      */         {
/* 1126 */           d = LocaleResources.parseDate(endDate, null);
/* 1127 */           date = LocaleUtils.formatODBC(d);
/* 1128 */           dateProps.put("endDate", date);
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*      */       }
/*      */ 
/* 1137 */       PropParameters params = new PropParameters(dateProps);
/*      */       try
/*      */       {
/* 1140 */         dateProps.put("orderBy", "dID ASC");
/* 1141 */         ResultSet rset = this.m_workspace.createResultSet("QIDCAnalyzeMapDateToDid", params);
/*      */ 
/* 1143 */         startRange = rset.getStringValue(0);
/*      */ 
/* 1146 */         if (endDate != null)
/*      */         {
/* 1148 */           dateProps.put("orderBy", "dID DESC");
/* 1149 */           rset = this.m_workspace.createResultSet("QIDCAnalyzeMapDateToDidWithBound", params);
/*      */ 
/* 1151 */           endRange = rset.getStringValue(0);
/*      */         }
/*      */       }
/*      */       catch (DataException de)
/*      */       {
/* 1156 */         System.err.println(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csIDCAnalyzeUnableToDetermineRange", de.getMessage()), null));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1172 */     if ((startRange != null) && (startRange.length() > 0))
/*      */     {
/* 1174 */       if (NumberUtils.parseInteger(startRange, -1) < 0)
/*      */       {
/* 1176 */         throw new DataException(LocaleUtils.encodeMessage("csIDCAnalyzeRangeValueNotValid", null, origStartRange));
/*      */       }
/*      */ 
/* 1180 */       this.m_idcProperties.put("StartID", startRange);
/*      */     }
/*      */ 
/* 1183 */     if (limit != null)
/*      */     {
/* 1185 */       int limitNum = NumberUtils.parseInteger(limit, -1);
/* 1186 */       int startNum = NumberUtils.parseInteger(startRange, 0);
/* 1187 */       if (limitNum > 0)
/*      */       {
/* 1189 */         int endNum = startNum + limitNum - 1;
/* 1190 */         this.m_idcProperties.put("EndID", "" + endNum);
/*      */       }
/*      */     } else {
/* 1193 */       if ((endRange == null) || (endRange.length() <= 0))
/*      */         return;
/* 1195 */       if (NumberUtils.parseInteger(endRange, -1) < 0)
/*      */       {
/* 1197 */         throw new DataException(LocaleUtils.encodeMessage("csIDCAnalyzeRangeValueNotValid", null, origEndRange));
/*      */       }
/*      */ 
/* 1200 */       this.m_idcProperties.put("EndID", endRange);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void readIntradocCfgFile()
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1210 */       this.m_propLoader.initIdc();
/* 1211 */       this.m_idcProperties = this.m_propLoader.getIdcProperties();
/* 1212 */       this.m_propLoader.initConfig();
/* 1213 */       this.m_cfgProperties = this.m_propLoader.getConfig();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1217 */       throw new ServiceException(LocaleUtils.encodeMessage("csBatchLoaderFileReadError", e.getMessage()));
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1225 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105359 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcanalyze.IdcAnalyzeApp
 * JD-Core Version:    0.5.4
 */