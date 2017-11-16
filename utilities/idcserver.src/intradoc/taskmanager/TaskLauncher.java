/*     */ package intradoc.taskmanager;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TaskLauncher
/*     */ {
/*  33 */   protected String m_name = null;
/*  34 */   protected Process m_process = null;
/*  35 */   protected TaskWork m_worker = null;
/*  36 */   protected String m_traceSubject = null;
/*     */ 
/*  38 */   protected boolean m_failed = false;
/*  39 */   protected int m_failedCount = 0;
/*  40 */   protected boolean m_abort = false;
/*  41 */   protected boolean m_stopping = false;
/*     */ 
/*  43 */   protected boolean m_needPersistance = false;
/*  44 */   protected String[] m_cmdLine = null;
/*     */ 
/*  46 */   protected TaskProcessStreamMonitor m_output = null;
/*  47 */   protected TaskProcessStreamMonitor m_error = null;
/*  48 */   protected Object m_lock = new Object();
/*  49 */   protected TaskInfo m_stopCommand = null;
/*     */ 
/*     */   public TaskLauncher(String name)
/*     */   {
/*  53 */     this.m_name = name;
/*  54 */     SystemUtils.registerSynchronizationObjectToNotifyOnStop(this.m_lock);
/*     */   }
/*     */ 
/*     */   public TaskLauncher(String name, boolean needPersistance)
/*     */   {
/*  59 */     this.m_name = name;
/*  60 */     this.m_needPersistance = needPersistance;
/*  61 */     SystemUtils.registerSynchronizationObjectToNotifyOnStop(this.m_lock);
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/*  66 */     this.m_failed = false;
/*  67 */     this.m_abort = false;
/*     */   }
/*     */ 
/*     */   public String[] createCommandLine(Vector cmdLine, boolean isReportCommand)
/*     */   {
/*  73 */     int size = cmdLine.size();
/*  74 */     String[] cmdBuff = new String[size];
/*  75 */     String reportLine = null;
/*  76 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  78 */       cmdBuff[i] = ((String)cmdLine.elementAt(i));
/*  79 */       if (!isReportCommand)
/*     */         continue;
/*  81 */       if (reportLine != null)
/*     */       {
/*  83 */         reportLine = reportLine + " " + cmdBuff[i];
/*     */       }
/*     */       else
/*     */       {
/*  87 */         reportLine = cmdBuff[i];
/*     */       }
/*     */     }
/*     */ 
/*  91 */     if (reportLine != null)
/*     */     {
/*  93 */       reportMessage("!$" + reportLine);
/*     */     }
/*     */ 
/*  96 */     return cmdBuff;
/*     */   }
/*     */ 
/*     */   public void startExe(String[] cmdLine, TaskInfo ti, String unique) throws ServiceException
/*     */   {
/* 101 */     String[] environment = ti.m_environment;
/* 102 */     String workingDir = ti.m_workingDir;
/*     */ 
/* 104 */     if ((this.m_needPersistance) && (this.m_process != null))
/*     */     {
/*     */       try
/*     */       {
/* 111 */         this.m_process.exitValue();
/* 112 */         this.m_failed = true;
/* 113 */         TaskMonitor.stop(this.m_name);
/* 114 */         this.m_failedCount += 1;
/* 115 */         if (this.m_failedCount > 10)
/*     */         {
/* 119 */           String msg = LocaleUtils.encodeMessage("csUnableToStartProcess", null, this.m_name);
/* 120 */           throw new ServiceException(msg, null);
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (IllegalThreadStateException e)
/*     */       {
/* 127 */         return;
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 133 */       if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("taskmanager")))
/*     */       {
/* 135 */         IdcStringBuilder msg = new IdcStringBuilder("starting executable - command line: ");
/* 136 */         for (String part : cmdLine)
/*     */         {
/* 138 */           msg.append("[");
/* 139 */           msg.append(part);
/* 140 */           msg.append("]");
/*     */         }
/* 142 */         Report.trace("taskmanager", msg.toString(), null);
/* 143 */         msg = new IdcStringBuilder("starting executable - environment: ");
/* 144 */         if (environment != null)
/*     */         {
/* 146 */           for (String part : environment)
/*     */           {
/* 148 */             msg.append("[");
/* 149 */             msg.append(part);
/* 150 */             msg.append("]");
/*     */           }
/*     */ 
/*     */         }
/*     */         else {
/* 155 */           msg.append("null");
/*     */         }
/* 157 */         Report.trace("taskmanager", msg.toString(), null);
/*     */       }
/*     */ 
/* 161 */       if ((this.m_needPersistance) && 
/* 163 */         (this.m_stopCommand == null) && (ti.m_worker != null))
/*     */       {
/* 165 */         this.m_stopCommand = ti.m_worker.stopCommand(ti);
/*     */       }
/*     */ 
/* 169 */       if ((environment == null) && (workingDir == null))
/*     */       {
/* 171 */         Runtime run = Runtime.getRuntime();
/* 172 */         this.m_process = run.exec(cmdLine);
/*     */       }
/*     */       else
/*     */       {
/* 176 */         ProcessBuilder processBuilder = createProcess(cmdLine, environment, workingDir);
/* 177 */         this.m_process = processBuilder.start();
/*     */       }
/*     */ 
/* 180 */       this.m_output = new TaskProcessStreamMonitor();
/* 181 */       this.m_output.init(this, this.m_process, false, this.m_needPersistance, this.m_lock);
/* 182 */       Thread outputBg = new Thread(this.m_output, "TaskLauncher_" + this.m_name + "_stdout_" + unique);
/* 183 */       outputBg.setDaemon(true);
/* 184 */       outputBg.start();
/* 185 */       SystemUtils.registerSynchronizationObjectToNotifyOnStop(this.m_output);
/* 186 */       this.m_error = new TaskProcessStreamMonitor();
/* 187 */       this.m_error.init(this, this.m_process, true, this.m_needPersistance, this.m_lock);
/* 188 */       Thread errorBg = new Thread(this.m_error, "TaskLauncher_" + this.m_name + "_stderr_" + unique);
/* 189 */       errorBg.setDaemon(true);
/* 190 */       errorBg.start();
/* 191 */       SystemUtils.registerSynchronizationObjectToNotifyOnStop(this.m_error);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 195 */       throw new ServiceException(e, "csUnableToStartProcess", new Object[] { this.m_name });
/*     */     }
/*     */   }
/*     */ 
/*     */   protected ProcessBuilder createProcess(String[] cmdLine, String[] environment, String workingDir)
/*     */   {
/* 201 */     ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
/* 202 */     Map processEnv = processBuilder.environment();
/* 203 */     if (workingDir != null)
/*     */     {
/* 205 */       processBuilder.directory(new File(workingDir));
/*     */     }
/* 207 */     if (environment != null)
/*     */     {
/* 209 */       String sep = EnvUtils.getPathSeparator();
/* 210 */       String osPathEnvName = findPathKeyInEnv(processEnv);
/* 211 */       for (int e = 0; e < environment.length; ++e)
/*     */       {
/* 213 */         String envStr = environment[e];
/* 214 */         if (envStr.length() == 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 218 */         int tok = envStr.indexOf(61);
/* 219 */         if (tok < 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 223 */         String envKey = envStr.substring(0, tok);
/* 224 */         String envVal = envStr.substring(tok + 1);
/* 225 */         boolean appendEnv = false;
/* 226 */         if ((envKey.equalsIgnoreCase(osPathEnvName)) || (envKey.equalsIgnoreCase("PATH")))
/*     */         {
/* 228 */           appendEnv = true;
/* 229 */           if (EnvUtils.isFamily("windows"))
/*     */           {
/* 232 */             envKey = osPathEnvName;
/*     */           }
/*     */         }
/*     */ 
/* 236 */         if (appendEnv)
/*     */         {
/* 238 */           String path = (String)processEnv.get(envKey);
/* 239 */           envVal = EnvUtils.convertPathToOSConventions(envVal);
/* 240 */           path = envVal + sep + path;
/* 241 */           processEnv.put(envKey, path);
/* 242 */           if (SystemUtils.m_verbose)
/*     */           {
/* 244 */             Report.trace("taskmanager", "prepending '" + envVal + "' to " + envKey, null);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 249 */           if ((SystemUtils.m_verbose) && (processEnv.containsKey(envKey)))
/*     */           {
/* 251 */             Report.trace("taskmanager", "Replacing processs environment key: " + envKey + " with " + envVal, null);
/*     */           }
/* 253 */           processEnv.put(envKey, envVal);
/*     */         }
/*     */       }
/*     */     }
/* 257 */     if (SystemUtils.m_verbose)
/*     */     {
/* 259 */       IdcStringBuilder rpt = new IdcStringBuilder();
/* 260 */       rpt.append("Process Info: working dir: " + processBuilder.directory() + "\nEnvironment:\n");
/* 261 */       for (Iterator it = processEnv.entrySet().iterator(); it.hasNext(); )
/*     */       {
/* 263 */         Map.Entry entry = (Map.Entry)it.next();
/* 264 */         Object key = entry.getKey();
/* 265 */         String value = (String)entry.getValue();
/* 266 */         if (value.indexOf(":\\") > 0)
/*     */         {
/* 269 */           value = EnvUtils.convertPathToOSConventions(value);
/*     */         }
/* 271 */         rpt.append("     " + key + " = " + value + "\n");
/*     */       }
/* 273 */       Report.trace("taskmanager", rpt.toString(), null);
/*     */     }
/* 275 */     return processBuilder;
/*     */   }
/*     */ 
/*     */   protected String findPathKeyInEnv(Map<String, String> processEnv)
/*     */   {
/* 283 */     String pathEnvName = EnvUtils.getLibraryPathEnvironmentVariableName();
/* 284 */     boolean keyFound = false;
/* 285 */     if (EnvUtils.isFamily("windows"))
/*     */     {
/* 287 */       String[] possibleKeys = { pathEnvName, "PATH", "Path", "path" };
/* 288 */       for (int i = 0; i < possibleKeys.length; ++i)
/*     */       {
/* 290 */         String key = possibleKeys[i];
/* 291 */         if (!processEnv.containsKey(key))
/*     */           continue;
/* 293 */         keyFound = true;
/* 294 */         pathEnvName = key;
/* 295 */         break;
/*     */       }
/*     */ 
/* 298 */       if (!keyFound)
/*     */       {
/* 300 */         Report.trace("taskmanager", "no 'PATH' key was found in process environment, using: " + pathEnvName, null);
/*     */       }
/*     */     }
/* 303 */     return pathEnvName;
/*     */   }
/*     */ 
/*     */   public void launchExe(TaskInfo info)
/*     */     throws ServiceException
/*     */   {
/* 309 */     if (info.m_needProcPersistance != this.m_needPersistance)
/*     */     {
/* 311 */       Report.trace("taskmanager", "Inconsistency for task launcher: " + this.m_name + " launcher.m_needPersistance: " + this.m_needPersistance + " while taskinfo.m_needProcPersistence: " + info.m_needProcPersistance, null);
/*     */     }
/*     */ 
/* 316 */     this.m_output.setCommandOptions(info.m_cmdLineOptions, info.m_eods);
/* 317 */     this.m_output.markNewWork();
/* 318 */     this.m_error.setCommandOptions(null, info.m_eods);
/* 319 */     this.m_error.markNewWork();
/*     */ 
/* 321 */     long startTime = System.currentTimeMillis();
/* 322 */     boolean isTimedOut = false;
/*     */ 
/* 324 */     boolean isDone = false;
/*     */ 
/* 326 */     ServiceException se = null;
/* 327 */     while (!isDone)
/*     */     {
/* 330 */       long left = info.m_timeout - (System.currentTimeMillis() - startTime);
/* 331 */       if (left > 500L)
/*     */       {
/* 333 */         left = 500L;
/*     */       }
/*     */ 
/* 336 */       if (left > 0L)
/*     */       {
/* 338 */         synchronized (this.m_lock)
/*     */         {
/* 340 */           if (!isDone)
/*     */           {
/*     */             try
/*     */             {
/* 344 */               this.m_lock.wait(left);
/* 345 */               isDone = this.m_output.isFinished();
/* 346 */               if ((!isDone) && (this.m_needPersistance))
/*     */               {
/* 348 */                 isDone = this.m_error.isFinished();
/* 349 */                 if (isDone)
/*     */                 {
/* 353 */                   this.m_lock.wait(5L);
/*     */                 }
/*     */               }
/*     */             }
/*     */             catch (Throwable ignore)
/*     */             {
/* 359 */               Report.trace(null, ignore, "csTaskThreadInterrupted", new Object[] { info.m_name });
/*     */             }
/*     */           }
/* 362 */           if (this.m_output.hasOutput())
/*     */           {
/* 364 */             info.m_output = this.m_output.getOutput();
/*     */           }
/* 366 */           if (this.m_error.hasOutput())
/*     */           {
/* 368 */             info.m_errMsg = this.m_error.getOutput();
/* 369 */             info.m_hasError = true;
/*     */           }
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 375 */         if ((this.m_needPersistance != true) || (this.m_stopping))
/*     */         {
/* 377 */           this.m_failed = true;
/* 378 */           isDone = true;
/*     */         }
/* 380 */         isTimedOut = true;
/*     */       }
/*     */ 
/* 383 */       if ((this.m_error.isFinished()) && (this.m_needPersistance) && (!this.m_abort))
/*     */       {
/* 385 */         String errStream = this.m_error.getOutput();
/* 386 */         String msg = LocaleUtils.encodeMessage("csErrorReturnedByProcess", errStream, this.m_name);
/*     */ 
/* 388 */         reportMessage(msg);
/* 389 */         se = new ServiceException(msg);
/* 390 */         info.m_error = se;
/* 391 */         info.m_errMsg = errStream;
/* 392 */         info.m_hasError = true;
/* 393 */         break;
/*     */       }
/*     */ 
/* 396 */       ServiceException error = this.m_output.getError();
/* 397 */       if ((error != null) || (isTimedOut))
/*     */       {
/* 399 */         IdcMessage msg = null;
/* 400 */         if (error != null)
/*     */         {
/* 402 */           msg = IdcMessageFactory.lc(error, "csErrorWaitingForProcess", new Object[] { this.m_name });
/* 403 */           info.m_error = error;
/*     */         }
/*     */         else
/*     */         {
/* 407 */           msg = IdcMessageFactory.lc("csErrorWaitingForProcess", new Object[] { this.m_name });
/*     */         }
/*     */ 
/* 410 */         String text = LocaleResources.localizeMessage(null, msg, null).toString();
/* 411 */         reportMessage(text);
/* 412 */         info.m_errMsg = LocaleUtils.encodeMessage(msg);
/* 413 */         info.m_hasError = true;
/* 414 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 418 */     if (this.m_output.getProcessId() != 0)
/*     */     {
/* 420 */       info.m_processId = this.m_output.getProcessId();
/*     */     }
/*     */ 
/* 423 */     if (this.m_failed)
/*     */     {
/* 425 */       Report.trace("taskmanager", "TaskLauncher - task failed:" + info, null);
/*     */ 
/* 427 */       this.m_output.abort();
/* 428 */       this.m_error.abort();
/*     */ 
/* 430 */       cleanUp(info);
/*     */ 
/* 432 */       String errMsg = null;
/* 433 */       if (isTimedOut)
/*     */       {
/* 435 */         Report.trace("taskmanager", "TaskLauncher - task timed out", null);
/* 436 */         errMsg = LocaleUtils.encodeMessage("csProcessTimedOut", null, this.m_name);
/*     */       }
/*     */       else
/*     */       {
/* 440 */         Report.trace("taskmanager", "TaskLauncher - aborted", null);
/* 441 */         errMsg = LocaleUtils.encodeMessage("csProcessAbort", null, this.m_name);
/*     */       }
/* 443 */       reportError(errMsg, info);
/* 444 */       se = new ServiceException(errMsg);
/* 445 */       info.m_error = se;
/* 446 */       info.m_errMsg = errMsg;
/* 447 */       if (isTimedOut)
/*     */       {
/* 449 */         info.m_status = TaskInfo.STATUS.TIMEOUT;
/*     */       }
/*     */       else
/*     */       {
/* 453 */         info.m_status = TaskInfo.STATUS.FAILURE;
/*     */       }
/* 455 */       info.m_hasError = true;
/*     */ 
/* 457 */       TaskMonitor.markLauncherFailed(this);
/*     */     }
/* 466 */     else if (info.m_worker != null)
/*     */     {
/* 470 */       if (!info.m_worker.isSuccessful(this.m_output.getOutput()))
/*     */       {
/* 472 */         Report.trace("taskmanager", "TaskLauncher - task failed based on output", null);
/* 473 */         info.m_status = TaskInfo.STATUS.FAILURE;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 478 */       info.m_status = TaskInfo.STATUS.SUCCESS;
/* 479 */       if (!info.m_needProcPersistance)
/*     */       {
/* 481 */         info.m_exitValue = waitForExitCode();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 486 */     if (!this.m_output.isFinished())
/*     */     {
/* 488 */       if (SystemUtils.m_verbose)
/*     */       {
/* 490 */         Report.debug("taskmanager", "Output stream is not finished reading at the end of processing.", null);
/*     */       }
/* 492 */       synchronized (this.m_output)
/*     */       {
/* 494 */         if (!this.m_output.hasNewTask())
/*     */         {
/* 496 */           if (SystemUtils.m_verbose)
/*     */           {
/* 498 */             Report.debug("taskmanager", "Output stream set finished reading.", null);
/*     */           }
/*     */ 
/* 501 */           this.m_output.setFinished(true);
/*     */ 
/* 504 */           info.m_output = this.m_output.getOutput();
/* 505 */           this.m_output.notify();
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 510 */     if (!this.m_error.isFinished())
/*     */     {
/* 512 */       synchronized (this.m_error)
/*     */       {
/* 514 */         this.m_error.setFinished(true);
/* 515 */         this.m_error.notify();
/*     */       }
/*     */     }
/*     */ 
/* 519 */     clearExe(this.m_failed);
/* 520 */     finishWork(info);
/*     */   }
/*     */ 
/*     */   protected int waitForExitCode()
/*     */   {
/* 525 */     int maxWaitForFinish = 50;
/* 526 */     int code = -1;
/* 527 */     int loopCnt = 0;
/* 528 */     boolean hasExited = false;
/* 529 */     while ((!hasExited) && (loopCnt <= 50))
/*     */     {
/*     */       try
/*     */       {
/* 533 */         if (this.m_process == null)
/*     */         {
/* 535 */           hasExited = true;
/*     */         }
/*     */         else
/*     */         {
/* 539 */           code = this.m_process.exitValue();
/* 540 */           hasExited = true;
/*     */         }
/*     */       }
/*     */       catch (IllegalThreadStateException badState)
/*     */       {
/* 545 */         ++loopCnt;
/* 546 */         SystemUtils.sleep(1L);
/*     */       }
/*     */     }
/* 549 */     return code;
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/* 554 */     return this.m_name;
/*     */   }
/*     */ 
/*     */   public String getTraceSubject()
/*     */   {
/* 559 */     return this.m_traceSubject;
/*     */   }
/*     */ 
/*     */   public void setTraceSubject(String subject)
/*     */   {
/* 564 */     this.m_traceSubject = subject;
/*     */   }
/*     */ 
/*     */   public void reportMessage(String msg)
/*     */   {
/* 569 */     Report.trace(this.m_traceSubject, LocaleResources.localizeMessage(msg, null), null);
/*     */   }
/*     */ 
/*     */   public void reportError(String msg, TaskInfo info)
/*     */   {
/* 574 */     msg = LocaleResources.localizeMessage(msg, null);
/* 575 */     if (SystemUtils.m_verbose)
/*     */     {
/* 577 */       Report.debug(null, msg, info.m_error);
/*     */     }
/*     */     else
/*     */     {
/* 581 */       Report.trace(null, msg, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void clearExe(boolean forceClear)
/*     */   {
/* 587 */     if ((forceClear) || (!this.m_needPersistance))
/*     */     {
/* 590 */       if (this.m_process != null)
/*     */       {
/* 592 */         this.m_process.destroy();
/* 593 */         this.m_process = null;
/*     */       }
/*     */ 
/* 596 */       if (this.m_output != null)
/*     */       {
/* 598 */         SystemUtils.unregisterSynchronizationObjectToNotifyOnStop(this.m_output);
/* 599 */         this.m_output.abort();
/*     */       }
/* 601 */       if (this.m_error != null)
/*     */       {
/* 603 */         SystemUtils.unregisterSynchronizationObjectToNotifyOnStop(this.m_error);
/* 604 */         this.m_error.abort();
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 611 */       if (this.m_output != null)
/*     */       {
/* 613 */         this.m_output.setFinished(false);
/*     */       }
/* 615 */       if (this.m_error != null)
/*     */       {
/* 617 */         this.m_error.setFinished(false);
/*     */       }
/*     */     }
/*     */ 
/* 621 */     SystemUtils.unregisterSynchronizationObjectToNotifyOnStop(this.m_lock);
/*     */   }
/*     */ 
/*     */   public void checkForAbort() throws ServiceException
/*     */   {
/* 626 */     if (!this.m_abort)
/*     */       return;
/* 628 */     String msg = LocaleUtils.encodeMessage("csAbortingProcess", null, this.m_name);
/* 629 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public void finishWork(TaskInfo info)
/*     */   {
/* 635 */     if (info.m_worker == null)
/*     */       return;
/* 637 */     info.m_worker.finishWork(info);
/*     */   }
/*     */ 
/*     */   public void cleanUp(TaskInfo info)
/*     */   {
/* 643 */     if (info.m_worker == null)
/*     */       return;
/* 645 */     info.m_worker.cleanUp();
/*     */   }
/*     */ 
/*     */   public void abort()
/*     */   {
/* 654 */     this.m_abort = true;
/* 655 */     clearExe(true);
/*     */   }
/*     */ 
/*     */   public boolean isFailed()
/*     */   {
/* 660 */     return this.m_failed;
/*     */   }
/*     */ 
/*     */   public boolean isBusy()
/*     */   {
/* 665 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 670 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101204 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.taskmanager.TaskLauncher
 * JD-Core Version:    0.5.4
 */