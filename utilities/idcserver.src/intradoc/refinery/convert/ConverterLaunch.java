/*     */ package intradoc.refinery.convert;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.taskmanager.TaskInfo;
/*     */ import intradoc.taskmanager.TaskInfo.STATUS;
/*     */ import intradoc.taskmanager.TaskMonitor;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.StringReader;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ConverterLaunch
/*     */ {
/*     */   protected ConverterStatusInterface m_statusInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected String m_traceSection;
/*     */   protected boolean m_convertFailed;
/*     */   protected boolean m_doneConverting;
/*     */   protected boolean m_useCompanion;
/*     */   protected boolean m_companionIsDone;
/*     */   protected String m_jobId;
/*     */   protected String m_taskId;
/*  71 */   protected String m_exePath = null;
/*  72 */   protected String[] m_envp = null;
/*  73 */   protected String m_workingDir = null;
/*     */ 
/*  75 */   protected String m_paramStr = null;
/*     */   protected int m_exitValue;
/*  79 */   protected boolean m_isTimeout = false;
/*     */ 
/*  82 */   protected String m_stdIn = null;
/*  83 */   protected String m_stdErr = "";
/*     */ 
/*  86 */   protected long m_timeAllowed = 0L;
/*     */ 
/*  89 */   protected boolean m_useMonitorFile = false;
/*     */ 
/*  91 */   protected String m_monitorFile = null;
/*     */ 
/*  93 */   protected long m_monitorFileLastModified = 0L;
/*     */ 
/*  95 */   protected int m_maxNumDeadCycle = 0;
/*     */ 
/*  98 */   protected String m_msgFile = null;
/*  99 */   protected String m_msgFileName = null;
/* 100 */   protected String m_msgFilePath = null;
/*     */ 
/* 102 */   protected long m_msgFileLastModified = 0L;
/*     */ 
/* 104 */   protected String m_msg = null;
/*     */ 
/*     */   public ConverterLaunch()
/*     */   {
/* 109 */     reset();
/*     */   }
/*     */ 
/*     */   public ConverterLaunch(ExecutionContext cxt)
/*     */   {
/* 117 */     init(cxt);
/*     */   }
/*     */ 
/*     */   public ConverterLaunch(ExecutionContext cxt, ExsimpleHelper exsimpleHelper)
/*     */   {
/* 128 */     init(cxt);
/* 129 */     setExeInfo(exsimpleHelper.m_exportExePath);
/* 130 */     setEnvironment(exsimpleHelper.getCurrentProcessEnv(), exsimpleHelper.m_exportDir);
/* 131 */     setUseCompanion(false);
/*     */   }
/*     */ 
/*     */   public void init(ExecutionContext cxt)
/*     */   {
/* 141 */     this.m_cxt = cxt;
/* 142 */     this.m_statusInterface = null;
/*     */ 
/* 144 */     Object o = cxt.getCachedObject("AgentData");
/* 145 */     if ((o != null) && (o instanceof ConverterStatusInterface))
/*     */     {
/* 147 */       this.m_statusInterface = ((ConverterStatusInterface)o);
/*     */     }
/*     */ 
/* 150 */     this.m_jobId = ((String)cxt.getCachedObject("jobId"));
/* 151 */     this.m_traceSection = loadAndReportVarFromContext(cxt, "ConverterLaunchTraceSection", "refinery");
/*     */ 
/* 153 */     reset();
/*     */   }
/*     */ 
/*     */   public static String loadAndReportVarFromContext(ExecutionContext cxt, String key, String defVal)
/*     */   {
/* 158 */     String val = (String)cxt.getCachedObject(key);
/* 159 */     if ((val == null) || (val.length() == 0))
/*     */     {
/* 161 */       val = defVal;
/* 162 */       if ((SystemUtils.m_verbose) || (SystemUtils.m_isDevelopmentEnvironment))
/*     */       {
/* 164 */         Report.debug(null, "the required context key: " + key + "; was not passed; using default: " + defVal, new StackTrace());
/*     */       }
/*     */     }
/* 167 */     return val;
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/* 176 */     this.m_convertFailed = true;
/* 177 */     this.m_useCompanion = true;
/* 178 */     this.m_companionIsDone = false;
/* 179 */     this.m_isTimeout = false;
/*     */   }
/*     */ 
/*     */   public void setExeInfo(String exePath, String paramStr)
/*     */   {
/* 191 */     setExeInfo(exePath);
/* 192 */     setParameterInfo(paramStr);
/*     */   }
/*     */ 
/*     */   public void setExeInfo(String exePath)
/*     */   {
/* 203 */     this.m_exePath = exePath;
/* 204 */     this.m_taskId = loadAndReportVarFromContext(this.m_cxt, "ConvertLaunchMasterTaskId", "CmdLineConversion");
/* 205 */     if (!EnvUtils.isFamily("windows"))
/*     */       return;
/* 207 */     this.m_exePath = ("\"" + exePath + "\"");
/*     */   }
/*     */ 
/*     */   public void setTimeOuts(long timeout)
/*     */   {
/* 213 */     setTimeOuts(0L, timeout, 0L, null);
/*     */   }
/*     */ 
/*     */   public void setTimeOuts(long minTimeOut, long maxTimeOut, long factor, String infile)
/*     */   {
/* 218 */     if ((infile != null) && (factor > 0L))
/*     */     {
/* 220 */       this.m_timeAllowed = computeTimeAllowed(infile, factor);
/*     */ 
/* 222 */       if (this.m_timeAllowed < minTimeOut)
/*     */       {
/* 224 */         this.m_timeAllowed = minTimeOut;
/*     */       }
/* 226 */       else if (this.m_timeAllowed > maxTimeOut)
/*     */       {
/* 228 */         this.m_timeAllowed = maxTimeOut;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 233 */       this.m_timeAllowed = maxTimeOut;
/*     */     }
/* 235 */     Report.trace(this.m_traceSection, "min time = " + minTimeOut + "\nmax time = " + maxTimeOut + "\ntime factor = " + factor + "\n(adjusted) time allowed " + this.m_timeAllowed, null);
/*     */   }
/*     */ 
/*     */   public void setParameterInfo(String paramStr)
/*     */   {
/* 247 */     this.m_paramStr = paramStr;
/*     */   }
/*     */ 
/*     */   public void setDefaultMsg(String encodedMsg, String msgFile)
/*     */   {
/* 261 */     if (!encodedMsg.startsWith("!"))
/*     */     {
/* 263 */       encodedMsg = LocaleUtils.encodeMessage(encodedMsg, null);
/*     */     }
/* 265 */     this.m_msg = encodedMsg;
/* 266 */     computeMsgPaths(msgFile);
/*     */   }
/*     */ 
/*     */   public void setDefaultMsg(IdcMessage msg, String msgFile)
/*     */   {
/* 278 */     this.m_msg = LocaleUtils.encodeMessage(msg);
/* 279 */     computeMsgPaths(msgFile);
/*     */   }
/*     */ 
/*     */   protected void computeMsgPaths(String msgFile)
/*     */   {
/* 284 */     if ((msgFile == null) || (msgFile.length() <= 0))
/*     */       return;
/* 286 */     this.m_msgFile = msgFile;
/* 287 */     this.m_msgFileName = FileUtils.getName(msgFile);
/* 288 */     this.m_msgFilePath = FileUtils.getDirectory(msgFile);
/* 289 */     FileUtils.deleteFile(msgFile);
/*     */   }
/*     */ 
/*     */   public void setFileToMonitor(String monitorPath, int numCycles)
/*     */   {
/* 302 */     this.m_useCompanion = true;
/* 303 */     this.m_useMonitorFile = true;
/* 304 */     this.m_monitorFile = monitorPath;
/* 305 */     this.m_maxNumDeadCycle = numCycles;
/*     */   }
/*     */ 
/*     */   public void setEnvironment(String[] envVars, String workingDir)
/*     */   {
/* 318 */     if (workingDir != null)
/*     */     {
/* 320 */       workingDir = EnvUtils.convertPathToOSConventions(workingDir);
/*     */     }
/* 322 */     boolean editEnv = true;
/* 323 */     if ((envVars == null) && (workingDir == null))
/*     */     {
/* 325 */       editEnv = false;
/*     */     }
/*     */ 
/* 328 */     if (editEnv)
/*     */     {
/* 330 */       IdcVector envv = new IdcVector();
/* 331 */       boolean osPathEnvSet = false;
/* 332 */       String osPathEnvName = EnvUtils.getLibraryPathEnvironmentVariableName();
/* 333 */       if (envVars != null)
/*     */       {
/* 335 */         for (int i = 0; i < envVars.length; ++i)
/*     */         {
/* 337 */           String env = envVars[i];
/* 338 */           if ((env.startsWith(osPathEnvName)) && (workingDir != null))
/*     */           {
/* 340 */             String sep = EnvUtils.getPathSeparator();
/* 341 */             env = env + sep + workingDir;
/* 342 */             osPathEnvSet = true;
/*     */           }
/* 344 */           if (env == null)
/*     */             continue;
/* 346 */           envv.addElement(env);
/*     */         }
/*     */       }
/*     */ 
/* 350 */       if (workingDir != null)
/*     */       {
/* 352 */         if (!osPathEnvSet)
/*     */         {
/* 354 */           envv.addElement(osPathEnvName + "=" + workingDir);
/*     */         }
/* 356 */         if (!EnvUtils.isFamily("windows"))
/*     */         {
/* 358 */           envv.addElement("PATH=" + workingDir);
/*     */         }
/*     */       }
/* 361 */       this.m_envp = StringUtils.convertListToArray(envv);
/*     */     }
/*     */ 
/* 364 */     this.m_workingDir = workingDir;
/*     */ 
/* 366 */     IdcStringBuilder traceMsg = new IdcStringBuilder("working directory: " + this.m_workingDir);
/* 367 */     if (this.m_envp != null)
/*     */     {
/* 369 */       int numEnv = this.m_envp.length;
/* 370 */       traceMsg.append("\nThere are " + numEnv + " environment vars set for this process");
/* 371 */       if (SystemUtils.m_verbose)
/*     */       {
/* 373 */         for (int i = 0; i < numEnv; ++i)
/*     */         {
/* 375 */           traceMsg.append("\n\t" + this.m_envp[i]);
/*     */         }
/*     */       }
/*     */     }
/* 379 */     Report.trace(this.m_traceSection, traceMsg.toString(), null);
/*     */   }
/*     */ 
/*     */   public void setUseCompanion(boolean useCompanion)
/*     */   {
/* 389 */     this.m_useCompanion = useCompanion;
/* 390 */     if (this.m_useCompanion)
/*     */       return;
/* 392 */     this.m_companionIsDone = true;
/* 393 */     this.m_useMonitorFile = false;
/* 394 */     this.m_msgFile = null;
/*     */   }
/*     */ 
/*     */   public String getStdErrText()
/*     */   {
/* 403 */     return this.m_stdErr;
/*     */   }
/*     */ 
/*     */   public String getStdInText()
/*     */   {
/* 411 */     return this.m_stdIn;
/*     */   }
/*     */ 
/*     */   public int getExitValue()
/*     */   {
/* 420 */     return this.m_exitValue;
/*     */   }
/*     */ 
/*     */   public boolean didProcessTimeOut()
/*     */   {
/* 430 */     return this.m_isTimeout;
/*     */   }
/*     */ 
/*     */   public boolean execute()
/*     */     throws ServiceException
/*     */   {
/* 439 */     if (!SystemUtils.m_isServerStopped)
/*     */     {
/* 441 */       Vector cmdLine = createCommandLine();
/*     */       try
/*     */       {
/* 445 */         TaskInfo taskInfo = new TaskInfo(this.m_taskId, cmdLine, null);
/* 446 */         taskInfo.m_traceSubject = this.m_traceSection;
/* 447 */         taskInfo.m_idleTimeout = 10;
/* 448 */         taskInfo.m_timeout = this.m_timeAllowed;
/* 449 */         taskInfo.m_environment = this.m_envp;
/* 450 */         taskInfo.m_workingDir = this.m_workingDir;
/*     */ 
/* 453 */         TaskMonitor.addToQueue(taskInfo);
/*     */ 
/* 455 */         if (this.m_useCompanion)
/*     */         {
/* 457 */           String name = FileUtils.getName(this.m_exePath);
/* 458 */           String companionId = "TaskCompanion_" + this.m_jobId + "_" + name;
/* 459 */           startCompanionThread(this.m_taskId, companionId);
/*     */         }
/*     */ 
/* 462 */         synchronized (taskInfo)
/*     */         {
/* 464 */           while ((!taskInfo.m_isFinished) && (!SystemUtils.m_isServerStopped))
/*     */           {
/*     */             try
/*     */             {
/* 468 */               taskInfo.wait(10000L);
/*     */             }
/*     */             catch (InterruptedException ignore)
/*     */             {
/* 473 */               if (SystemUtils.m_verbose)
/*     */               {
/* 475 */                 Report.debug("refinery", null, ignore);
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 481 */         if (SystemUtils.m_isServerStopped)
/*     */         {
/* 483 */           Report.trace(null, "IBR is shutting down....terminating subprocesses", null);
/* 484 */           TaskMonitor.stop(this.m_taskId);
/* 485 */           this.m_stdIn = taskInfo.m_output;
/* 486 */           this.m_isTimeout = true;
/* 487 */           ??? = 1;
/*     */           return ???;
/*     */         }
/* 490 */         this.m_stdIn = taskInfo.m_output;
/* 491 */         this.m_stdErr = taskInfo.m_errMsg;
/* 492 */         this.m_exitValue = taskInfo.m_exitValue;
/* 493 */         this.m_companionIsDone = true;
/*     */ 
/* 496 */         if (taskInfo.m_status == TaskInfo.STATUS.SUCCESS)
/*     */         {
/* 498 */           this.m_convertFailed = false;
/*     */         }
/* 500 */         else if (taskInfo.m_status == TaskInfo.STATUS.TIMEOUT)
/*     */         {
/* 502 */           this.m_isTimeout = true;
/* 503 */           terminateChildProc();
/*     */         }
/* 505 */         else if (taskInfo.m_status == TaskInfo.STATUS.FAILURE)
/*     */         {
/* 507 */           terminateChildProc();
/* 508 */           this.m_convertFailed = true;
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*     */       }
/*     */       finally
/*     */       {
/* 518 */         if (SystemUtils.m_isServerStopped)
/*     */         {
/* 521 */           terminateChildProc();
/*     */         }
/*     */       }
/*     */     }
/* 525 */     return this.m_convertFailed;
/*     */   }
/*     */ 
/*     */   protected void startCompanionThread(String taskId, String companionId)
/*     */   {
/* 530 */     String id = taskId;
/* 531 */     Thread taskCompanionThread = new Thread(new Runnable(id)
/*     */     {
/*     */       public void run()
/*     */       {
/* 535 */         int monitorDormantCnt = 0;
/*     */         try
/*     */         {
/* 538 */           while ((!ConverterLaunch.this.m_companionIsDone) && (!SystemUtils.m_isServerStopped))
/*     */           {
/* 540 */             if (ConverterLaunch.this.m_useMonitorFile)
/*     */             {
/* 542 */               boolean active = ConverterLaunch.this.isMonitorActive();
/* 543 */               if (!active)
/*     */               {
/* 545 */                 ++monitorDormantCnt;
/* 546 */                 if (monitorDormantCnt < ConverterLaunch.this.m_maxNumDeadCycle)
/*     */                   break label98;
/* 548 */                 Report.trace("refinery", "heartbeat stopped", null);
/* 549 */                 TaskMonitor.stop(this.val$id);
/* 550 */                 ConverterLaunch.this.m_convertFailed = true;
/* 551 */                 ConverterLaunch.this.m_companionIsDone = true;
/* 552 */                 ConverterLaunch.this.m_doneConverting = true;
/* 553 */                 break;
/*     */               }
/*     */ 
/* 558 */               monitorDormantCnt = 0;
/*     */             }
/*     */ 
/* 561 */             label98: ConverterLaunch.this.updateStatusMessage();
/* 562 */             for (int i = 1; i <= 30; ++i)
/*     */             {
/* 564 */               SystemUtils.sleep(1000L);
/* 565 */               if (ConverterLaunch.this.m_companionIsDone) break; if (SystemUtils.m_isServerStopped) {
/*     */                 break;
/*     */               }
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (Exception ignore)
/*     */         {
/* 574 */           Report.debug(null, null, ignore);
/*     */         }
/*     */       }
/*     */     });
/* 578 */     taskCompanionThread.setDaemon(true);
/* 579 */     taskCompanionThread.setName(companionId);
/* 580 */     taskCompanionThread.start();
/*     */   }
/*     */ 
/*     */   protected Vector createCommandLine()
/*     */   {
/* 588 */     IdcVector cmdLine = new IdcVector();
/* 589 */     if (this.m_exePath != null)
/*     */     {
/* 591 */       cmdLine.addElement(this.m_exePath);
/*     */     }
/* 593 */     if ((this.m_paramStr != null) && (this.m_paramStr.length() != 0))
/*     */     {
/* 595 */       Vector param = StringUtils.parseArray(this.m_paramStr, ' ', ' ');
/* 596 */       int i = 0;
/* 597 */       int sz = param.size();
/*     */ 
/* 599 */       while (i < sz)
/*     */       {
/* 601 */         String item = (String)param.elementAt(i);
/* 602 */         if ((item != null) && (item.length() > 0))
/*     */         {
/* 604 */           int tok = item.indexOf("\"");
/* 605 */           if (tok == 0)
/*     */           {
/* 607 */             boolean done = false;
/* 608 */             String str = null;
/* 609 */             while (!done)
/*     */             {
/* 611 */               String temp = (String)param.elementAt(i);
/* 612 */               if (str == null)
/*     */               {
/* 614 */                 str = temp;
/*     */               }
/*     */               else
/*     */               {
/* 618 */                 str = str + " " + temp;
/*     */               }
/* 620 */               if (temp.endsWith("\""))
/*     */               {
/* 622 */                 done = true;
/*     */               }
/* 624 */               ++i;
/*     */             }
/*     */ 
/* 630 */             item = str.substring(1, str.length() - 1);
/* 631 */             --i;
/*     */           }
/* 633 */           item = fixUncPath(item);
/* 634 */           if (SystemUtils.m_verbose)
/*     */           {
/* 636 */             Report.trace("refinery", "appending to launch cmd: '" + item + "'", null);
/*     */           }
/*     */ 
/* 639 */           cmdLine.addElement(item);
/*     */         }
/*     */         else
/*     */         {
/* 643 */           Report.trace("refinery", "Skipping the " + i + " element of parameter string, parameter is empty string.", null);
/*     */         }
/* 645 */         ++i;
/*     */       }
/*     */     }
/* 648 */     return cmdLine;
/*     */   }
/*     */ 
/*     */   public static String fixUncPath(String path)
/*     */   {
/* 653 */     if (path.startsWith("//"))
/*     */     {
/* 655 */       path = "\\\\" + path.substring(2);
/*     */     }
/* 657 */     return path;
/*     */   }
/*     */ 
/*     */   protected void terminateChildProc()
/*     */   {
/* 662 */     if (this.m_stdIn == null)
/*     */     {
/* 664 */       Report.trace("refinery", "child process STDOUT was null; unable to terminate sub processes.", null);
/* 665 */       return;
/*     */     }
/* 667 */     BufferedReader br = new BufferedReader(new StringReader(this.m_stdIn));
/* 668 */     DataBinder data = new DataBinder();
/*     */ 
/* 670 */     Report.trace("refinery", "terminating processes by force", null);
/*     */     try
/*     */     {
/* 674 */       data.receive(br);
/* 675 */       String before = data.getLocal("Before");
/* 676 */       if ((before != null) && (before.length() > 0))
/*     */       {
/* 678 */         before = before.trim();
/*     */       }
/* 680 */       String after = data.getLocal("After");
/* 681 */       if ((after != null) && (after.length() > 0))
/*     */       {
/* 683 */         after = after.trim();
/*     */       }
/* 685 */       Vector prePID = StringUtils.parseArray(before, ' ', ' ');
/* 686 */       Vector postPID = StringUtils.parseArray(after, ' ', ' ');
/* 687 */       Vector diffPID = new Vector();
/*     */ 
/* 689 */       int postSize = postPID.size();
/* 690 */       int preSize = prePID.size();
/* 691 */       for (int i = 0; i < postSize; ++i)
/*     */       {
/* 693 */         String post = (String)postPID.elementAt(i);
/* 694 */         boolean isFound = false;
/* 695 */         for (int j = 0; j < preSize; ++j)
/*     */         {
/* 697 */           String pre = (String)prePID.elementAt(j);
/* 698 */           if (!pre.equals(post))
/*     */             continue;
/* 700 */           isFound = true;
/* 701 */           break;
/*     */         }
/*     */ 
/* 704 */         if (isFound)
/*     */           continue;
/* 706 */         diffPID.addElement(post);
/*     */       }
/*     */ 
/* 709 */       if (diffPID.size() > 0)
/*     */       {
/* 711 */         NativeOsUtils utils = new NativeOsUtils();
/* 712 */         IdcStringBuilder traceMsg = new IdcStringBuilder();
/* 713 */         for (int k = 0; k < diffPID.size(); ++k)
/*     */         {
/* 715 */           String pid = (String)diffPID.elementAt(k);
/* 716 */           if ((pid == null) || (pid.length() <= 0))
/*     */             continue;
/* 718 */           traceMsg.append("Terminating pid (" + pid + ")\n");
/* 719 */           utils.kill(Integer.parseInt(pid), NativeOsUtils.SIGKILL);
/*     */         }
/*     */ 
/* 722 */         Report.trace("refinery", traceMsg.toString(), null);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 729 */       Report.debug(null, null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected long computeTimeAllowed(String inFile, long factor)
/*     */   {
/* 735 */     long fileSize = 0L;
/*     */ 
/* 737 */     if (inFile.indexOf(";") > 0)
/*     */     {
/* 739 */       Vector v = StringUtils.parseArray(inFile, ';', ' ');
/* 740 */       for (int i = 0; i == v.size(); ++i)
/*     */       {
/* 742 */         File processingFile = new File((String)v.elementAt(i));
/* 743 */         long temp = processingFile.length();
/* 744 */         fileSize += temp;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 750 */       File processingFile = new File(inFile);
/* 751 */       fileSize = processingFile.length();
/*     */     }
/*     */ 
/* 754 */     return fileSize * factor;
/*     */   }
/*     */ 
/*     */   protected boolean isMonitorActive()
/*     */   {
/* 760 */     File statusFile = new File(this.m_monitorFile);
/* 761 */     long modified = statusFile.lastModified();
/* 762 */     if (modified != this.m_monitorFileLastModified)
/*     */     {
/* 764 */       Report.trace("refinery", "heartbeat alive", null);
/* 765 */       this.m_monitorFileLastModified = modified;
/* 766 */       return true;
/*     */     }
/* 768 */     Report.trace("refinery", "heartbeat dead", null);
/* 769 */     return false;
/*     */   }
/*     */ 
/*     */   protected void updateStatusMessage()
/*     */   {
/* 774 */     if (this.m_msgFile != null)
/*     */     {
/*     */       try
/*     */       {
/* 778 */         File msg = new File(this.m_msgFile);
/* 779 */         long modified = msg.lastModified();
/* 780 */         if (modified != this.m_msgFileLastModified)
/*     */         {
/* 782 */           DataBinder status = ResourceUtils.readDataBinder(this.m_msgFilePath, this.m_msgFileName);
/* 783 */           this.m_msg = status.getLocal("StatusMsg");
/*     */         }
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 788 */         Report.debug(null, null, ignore);
/*     */       }
/*     */     }
/* 791 */     if ((this.m_statusInterface == null) || (this.m_msg == null))
/*     */       return;
/* 793 */     this.m_statusInterface.setCurrentStatusMsg(this.m_jobId, this.m_msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 799 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99079 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.convert.ConverterLaunch
 * JD-Core Version:    0.5.4
 */