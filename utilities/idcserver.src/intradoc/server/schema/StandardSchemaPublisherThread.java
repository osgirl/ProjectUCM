/*     */ package intradoc.server.schema;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.ProgressState;
/*     */ import intradoc.shared.ProgressStateUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.StringReader;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class StandardSchemaPublisherThread extends Thread
/*     */   implements SchemaPublisherThread
/*     */ {
/*     */   protected boolean m_abort;
/*     */   protected String m_notificationDir;
/*     */   protected String m_notificationFile;
/*     */   protected String m_resetTimerFile;
/*     */   protected SchemaUtils m_schemaUtils;
/*     */   protected ExecutionContext m_context;
/*     */   protected ServerSchemaManager m_manager;
/*     */   protected Hashtable m_fileTable;
/*     */   protected Vector m_fileList;
/*     */   protected long m_lastStartTime;
/*     */   protected long m_lastFinishTime;
/*     */   protected long m_earliestStartTime;
/*     */   protected long m_averageRunTime;
/*     */   protected long m_notificationTimestamp;
/*     */   protected boolean m_recheckPublishing;
/*     */   protected boolean m_isPublishing;
/*     */   protected boolean m_isWaitPublishing;
/*     */   protected boolean m_isEnabled;
/*     */   public long m_republishInterval;
/*     */   public long m_publishRequestCheckInterval;
/*     */   public long m_publishFailureSeatbeltInterval;
/*     */   public long m_publishFailureSeatbeltIntervalMax;
/*     */   public long m_publishMinimumInterval;
/*     */   public long m_publishMaximumInterval;
/*     */   public long m_win32DelayRenameInterval;
/*     */   public long m_renameRetryInitialInterval;
/*     */   public long m_renameRetryIterations;
/*  82 */   protected static int m_counter = 0;
/*     */ 
/*     */   public StandardSchemaPublisherThread()
/*     */   {
/*  33 */     this.m_abort = false;
/*     */ 
/*  36 */     this.m_notificationFile = "publish.dat";
/*  37 */     this.m_resetTimerFile = "reset.dat";
/*     */ 
/*  45 */     this.m_lastStartTime = 0L;
/*  46 */     this.m_lastFinishTime = 0L;
/*  47 */     this.m_earliestStartTime = 0L;
/*     */ 
/*  49 */     this.m_averageRunTime = -1L;
/*     */ 
/*  57 */     this.m_notificationTimestamp = 0L;
/*     */ 
/*  61 */     this.m_recheckPublishing = false;
/*     */ 
/*  64 */     this.m_isPublishing = false;
/*     */ 
/*  67 */     this.m_isWaitPublishing = false;
/*     */ 
/*  70 */     this.m_isEnabled = false;
/*     */ 
/*  72 */     this.m_republishInterval = -1L;
/*  73 */     this.m_publishRequestCheckInterval = -1L;
/*  74 */     this.m_publishFailureSeatbeltInterval = -1L;
/*  75 */     this.m_publishFailureSeatbeltIntervalMax = -1L;
/*  76 */     this.m_publishMinimumInterval = -1L;
/*  77 */     this.m_publishMaximumInterval = -1L;
/*  78 */     this.m_win32DelayRenameInterval = -1L;
/*  79 */     this.m_renameRetryInitialInterval = -1L;
/*  80 */     this.m_renameRetryIterations = -1L;
/*     */   }
/*     */ 
/*     */   public void init(String notificationDir, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/*  87 */     this.m_schemaUtils = ((SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", "!csSchemaUnableToLoadUtils"));
/*     */ 
/*  92 */     this.m_manager = ((ServerSchemaManager)context.getCachedObject("ServerSchemaManager"));
/*     */ 
/*  94 */     this.m_republishInterval = getTimeSeconds("SchemaPublishInterval", 14400);
/*     */ 
/*  96 */     this.m_publishRequestCheckInterval = getTimeSeconds("SchemaPublishCheckInterval", 120);
/*     */ 
/*  98 */     this.m_publishFailureSeatbeltInterval = getTimeSeconds("SchemaPublishRetryInterval", 3);
/*     */ 
/* 100 */     this.m_publishFailureSeatbeltIntervalMax = getTimeSeconds("SchemaPublishRetryIntervalMax", 300);
/*     */ 
/* 102 */     this.m_publishMinimumInterval = getTimeSeconds("SchemaPublishMinimumInterval", 4);
/*     */ 
/* 104 */     this.m_publishMaximumInterval = getTimeSeconds("SchemaPublishMaximumInterval", 86400);
/*     */ 
/* 107 */     this.m_notificationDir = FileUtils.directorySlashes(notificationDir);
/*     */ 
/* 109 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_notificationDir, 1, true);
/*     */ 
/* 111 */     if (m_counter > 0)
/*     */     {
/* 113 */       setName("SchemaPublisher-" + m_counter);
/*     */     }
/*     */     else
/*     */     {
/* 117 */       setName("SchemaPublisher");
/*     */     }
/*     */ 
/* 120 */     this.m_context = context;
/* 121 */     this.m_context.setCachedObject("SchemaPublisherThread", this);
/* 122 */     setDaemon(true);
/* 123 */     start();
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 129 */     m_counter += 1;
/*     */ 
/* 131 */     if (SystemUtils.m_verbose)
/*     */     {
/* 133 */       Report.debug("schemamonitor", "publishing thread started", null);
/*     */     }
/*     */ 
/* 136 */     long nextStartTime = System.currentTimeMillis() + this.m_republishInterval;
/* 137 */     DataBinder binder = null;
/* 138 */     SystemUtils.registerSynchronizationObjectToNotifyOnStop(this);
/* 139 */     while (!SystemUtils.m_isServerStopped)
/*     */     {
/* 141 */       this.m_isPublishing = false;
/* 142 */       synchronized (this)
/*     */       {
/* 144 */         if ((!this.m_recheckPublishing) || (!this.m_isEnabled))
/*     */         {
/* 146 */           this.m_isWaitPublishing = true;
/* 147 */           if (!this.m_isEnabled)
/*     */           {
/* 149 */             doWait(60000L);
/*     */           }
/*     */           else
/*     */           {
/* 153 */             doWait(this.m_publishRequestCheckInterval);
/*     */           }
/* 155 */           this.m_isWaitPublishing = false;
/*     */ 
/* 157 */           if (this.m_isEnabled);
/* 164 */           long now = System.currentTimeMillis();
/* 165 */           if (now > nextStartTime)
/*     */           {
/*     */             try
/*     */             {
/* 173 */               touchNotificationFile(0L, null);
/*     */             }
/*     */             catch (ServiceException e)
/*     */             {
/* 177 */               Report.error(null, null, e);
/*     */             }
/*     */           }
/*     */         }
/* 181 */         this.m_recheckPublishing = false;
/* 182 */         if (this.m_abort)
/*     */         {
/* 184 */           Report.trace("schemamonitor", "aborting publishing thread.", null);
/* 185 */           return;
/*     */         }
/*     */       }
/*     */ 
/* 189 */       String notificationFilePath = this.m_notificationDir + this.m_notificationFile;
/* 190 */       if (FileUtils.checkFile(notificationFilePath, true, false) != 0)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 198 */         if (FileUtils.checkFile(notificationFilePath, true, false) == 0);
/* 203 */         FileUtils.reserveLongTermLock(this.m_notificationDir, "publish", "schemamonitor", 0L, true);
/*     */ 
/* 205 */         Report.trace("schemamonitor", "got lock at " + System.currentTimeMillis(), null);
/*     */ 
/* 207 */         this.m_isPublishing = true;
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 211 */         if (SystemUtils.m_verbose)
/*     */         {
/* 213 */           Report.debug("schemamonitor", "unable to get long term lock", e);
/*     */         }
/*     */       }
/* 215 */       continue;
/*     */ 
/* 220 */       checkTimerReset();
/* 221 */       long now = System.currentTimeMillis();
/* 222 */       long waitTime = this.m_earliestStartTime - now;
/* 223 */       int counter = 0;
/* 224 */       while (waitTime > 0L)
/*     */       {
/* 226 */         if (counter % 60 == 0)
/*     */         {
/* 228 */           Report.trace("schemamonitor", "waiting " + waitTime / 1000L + " seconds before publishing.", null);
/*     */         }
/*     */ 
/* 231 */         long timeToWait = (waitTime > 10000L) ? 10000L : waitTime;
/* 232 */         doWait(timeToWait);
/* 233 */         now = System.currentTimeMillis();
/* 234 */         checkTimerReset();
/* 235 */         waitTime = this.m_earliestStartTime - now;
/* 236 */         ++counter;
/*     */       }
/* 238 */       Report.trace("schemamonitor", "starting publishing operation.", null);
/* 239 */       binder = new DataBinder();
/* 240 */       this.m_notificationTimestamp = readNotificationFile(binder);
/*     */ 
/* 242 */       boolean hasError = false;
/* 243 */       String errorKey = "csSchemaUnableToPublish";
/* 244 */       ProgressState progress = null;
/*     */       try
/*     */       {
/* 247 */         if (FileUtils.checkFile(notificationFilePath, true, false) != 0)
/*     */         {
/* 250 */           FileUtils.releaseLongTermLock(this.m_notificationDir, "publish", "schemamonitor");
/*     */ 
/* 320 */           FileUtils.releaseLongTermLock(this.m_notificationDir, "publish", "schemamonitor");
/*     */         }
/* 253 */         this.m_lastStartTime = System.currentTimeMillis();
/* 254 */         SchemaPublisher publisher = (SchemaPublisher)this.m_context.getCachedObject("SchemaPublisher");
/* 255 */         progress = publisher.getProgress();
/* 256 */         publisher.doPublishing(binder);
/*     */ 
/* 258 */         this.m_lastFinishTime = System.currentTimeMillis();
/* 259 */         nextStartTime = System.currentTimeMillis() + this.m_republishInterval;
/* 260 */         computeStartTime();
/* 261 */         long elapsed = this.m_lastFinishTime - this.m_lastStartTime;
/*     */ 
/* 263 */         ProgressStateUtils.reportProgress(progress, null, "schemamonitor", 2, "finished publishing operation in " + (elapsed + 500L) / 1000L + " sec., average is " + (this.m_averageRunTime + 500L) / 1000L + " sec. " + "Next publishing in no less than " + (this.m_earliestStartTime - this.m_lastFinishTime + 500L) / 1000L + " sec.", null);
/*     */ 
/* 271 */         this.m_publishFailureSeatbeltInterval = SharedObjects.getTypedEnvironmentInt("SchemaPublishRetryInterval", 3000, 18, 24);
/*     */ 
/* 279 */         SystemUtils.sleep(4000L);
/* 280 */         if (FileUtils.checkFile(notificationFilePath, true, false) == 0)
/*     */         {
/* 282 */           binder = new DataBinder();
/* 283 */           long newTimestamp = readNotificationFile(binder);
/* 284 */           if ((newTimestamp != 0L) && (newTimestamp == this.m_notificationTimestamp))
/*     */           {
/* 286 */             FileUtils.deleteFile(notificationFilePath);
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 292 */         String msg = null;
/* 293 */         if (t instanceof Exception)
/*     */         {
/* 295 */           msg = LocaleUtils.encodeMessage(errorKey, null);
/*     */         }
/*     */         else
/*     */         {
/* 299 */           msg = t.getMessage();
/* 300 */           if (msg == null)
/*     */           {
/* 302 */             msg = "!syNullPointerException";
/*     */           }
/* 304 */           msg = LocaleUtils.encodeMessage(errorKey, msg);
/*     */         }
/* 306 */         if (progress != null)
/*     */         {
/* 308 */           ProgressStateUtils.reportError(progress, "schemapublisher", msg, t);
/*     */         }
/*     */         else
/*     */         {
/* 312 */           Report.trace("schemapublisher", msg, t);
/*     */         }
/* 314 */         hasError = true;
/*     */       }
/*     */       finally
/*     */       {
/* 320 */         FileUtils.releaseLongTermLock(this.m_notificationDir, "publish", "schemamonitor");
/*     */       }
/* 322 */       if (hasError)
/*     */       {
/* 324 */         SystemUtils.sleep(this.m_publishFailureSeatbeltInterval);
/* 325 */         this.m_publishFailureSeatbeltInterval *= 2L;
/* 326 */         if (this.m_publishFailureSeatbeltInterval > this.m_publishFailureSeatbeltIntervalMax)
/*     */         {
/* 328 */           this.m_publishFailureSeatbeltInterval = this.m_publishFailureSeatbeltIntervalMax;
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void computeStartTime()
/*     */   {
/* 339 */     int aMinute = 60000;
/* 340 */     long elapsed = this.m_lastFinishTime - this.m_lastStartTime;
/*     */ 
/* 342 */     if (this.m_averageRunTime == -1L)
/*     */     {
/* 344 */       this.m_averageRunTime = elapsed;
/*     */     }
/* 346 */     this.m_averageRunTime = ((elapsed + this.m_averageRunTime) / 2L);
/* 347 */     this.m_earliestStartTime = this.m_lastFinishTime;
/* 348 */     if (elapsed > 60 * aMinute)
/*     */     {
/* 350 */       this.m_earliestStartTime = (this.m_lastFinishTime + 1440 * aMinute);
/*     */     }
/* 352 */     else if (elapsed > 15 * aMinute)
/*     */     {
/* 354 */       this.m_earliestStartTime = (this.m_lastFinishTime + 240 * aMinute);
/*     */     }
/* 356 */     else if (elapsed > 5 * aMinute)
/*     */     {
/* 358 */       this.m_earliestStartTime = (this.m_lastFinishTime + 60 * aMinute);
/*     */     }
/* 360 */     else if (elapsed > 2000L)
/*     */     {
/* 362 */       this.m_earliestStartTime = (this.m_lastFinishTime + elapsed * 12L);
/*     */     }
/*     */ 
/* 366 */     if (this.m_earliestStartTime - this.m_lastFinishTime < this.m_publishMinimumInterval)
/*     */     {
/* 368 */       this.m_earliestStartTime = (this.m_lastFinishTime + this.m_publishMinimumInterval);
/*     */     }
/* 370 */     if (this.m_earliestStartTime <= this.m_publishMaximumInterval + this.m_lastFinishTime)
/*     */       return;
/* 372 */     this.m_earliestStartTime = (this.m_publishMaximumInterval + this.m_lastFinishTime);
/*     */   }
/*     */ 
/*     */   public synchronized void setEnabled(boolean isEnabled)
/*     */   {
/* 383 */     Report.trace("schemamonitor", "enabling schema publishing", null);
/* 384 */     boolean wasEnabled = this.m_isEnabled;
/* 385 */     this.m_isEnabled = isEnabled;
/*     */ 
/* 387 */     if ((wasEnabled) || (!this.m_isEnabled)) {
/*     */       return;
/*     */     }
/*     */ 
/* 391 */     if (!this.m_isWaitPublishing)
/*     */     {
/* 393 */       this.m_recheckPublishing = true;
/*     */     }
/* 395 */     super.notify();
/*     */   }
/*     */ 
/*     */   public synchronized void resetTimers()
/*     */   {
/* 404 */     FileUtils.touchFile(this.m_notificationDir + this.m_resetTimerFile);
/* 405 */     this.m_earliestStartTime = 0L;
/* 406 */     this.m_publishFailureSeatbeltInterval = getTimeSeconds("SchemaPublishRetryInterval", 3);
/*     */ 
/* 408 */     if (!this.m_isWaitPublishing)
/*     */     {
/* 410 */       this.m_recheckPublishing = true;
/*     */     }
/* 412 */     super.notify();
/*     */   }
/*     */ 
/*     */   protected void checkTimerReset()
/*     */   {
/* 417 */     String resetFile = this.m_notificationDir + this.m_resetTimerFile;
/* 418 */     if (FileUtils.checkFile(resetFile, true, false) != 0)
/*     */       return;
/* 420 */     FileUtils.deleteFile(resetFile);
/* 421 */     this.m_earliestStartTime = 0L;
/* 422 */     this.m_publishFailureSeatbeltInterval = getTimeSeconds("SchemaPublishRetryInterval", 3);
/*     */   }
/*     */ 
/*     */   protected void touchNotificationFile(long time, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 430 */     if (binder == null)
/*     */     {
/* 432 */       binder = new DataBinder();
/*     */     }
/*     */     try
/*     */     {
/* 436 */       FileUtils.reserveDirectory(this.m_notificationDir);
/* 437 */       if (SystemUtils.m_verbose)
/*     */       {
/* 439 */         Report.debug("schemamonitor", "touchNotificationFile(): " + time, null);
/*     */       }
/*     */ 
/* 442 */       String publishOperation = binder.getLocal("publishOperation");
/* 443 */       if (publishOperation == null)
/*     */       {
/* 445 */         publishOperation = "full";
/*     */       }
/*     */       else
/*     */       {
/* 449 */         Report.trace("schemamonitor", "publish operation is '" + publishOperation + "'", null);
/*     */       }
/*     */ 
/* 453 */       DataBinder oldBinder = new DataBinder();
/* 454 */       if (readNotificationFileEx(oldBinder))
/*     */       {
/* 456 */         String timeString = oldBinder.getLocal("notificationTime");
/* 457 */         long currentTime = NumberUtils.parseLong(timeString, 0L);
/* 458 */         if ((currentTime > time) && (time != 0L))
/*     */         {
/* 460 */           Report.trace("schemamonitor", "touchNotificationFile(): time going backwards", null);
/*     */         }
/*     */ 
/* 463 */         String oldPublishOperation = oldBinder.getLocal("publishOperation");
/* 464 */         if ((oldPublishOperation != null) && (oldPublishOperation.equals("full")) && (!publishOperation.equals("full")))
/*     */         {
/* 468 */           Report.trace("schemamonitor", "promoted operation to full", null);
/* 469 */           publishOperation = "full";
/*     */         }
/*     */       }
/* 472 */       binder.putLocal("publishOperation", publishOperation);
/*     */ 
/* 474 */       binder.putLocal("notificationTime", "" + time);
/* 475 */       ResourceUtils.serializeDataBinderWithEncoding(this.m_notificationDir, this.m_notificationFile, binder, 1, "UTF8");
/*     */     }
/*     */     finally
/*     */     {
/* 481 */       FileUtils.releaseDirectory(this.m_notificationDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected long readNotificationFile(DataBinder binder)
/*     */   {
/* 487 */     long time = 0L;
/*     */     try
/*     */     {
/* 490 */       FileUtils.reserveDirectory(this.m_notificationDir);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 494 */       Report.trace("schemamonitory", null, e);
/* 495 */       return 0L;
/*     */     }
/*     */     try
/*     */     {
/* 499 */       readNotificationFileEx(binder);
/* 500 */       String timeString = binder.getLocal("notificationTime");
/* 501 */       time = NumberUtils.parseLong(timeString, 0L);
/*     */     }
/*     */     finally
/*     */     {
/* 505 */       FileUtils.releaseDirectory(this.m_notificationDir);
/*     */     }
/* 507 */     return time;
/*     */   }
/*     */ 
/*     */   protected boolean readNotificationFileEx(DataBinder binder)
/*     */   {
/* 512 */     if (binder == null)
/*     */     {
/* 514 */       binder = new DataBinder();
/*     */     }
/*     */ 
/* 517 */     long time = 0L;
/* 518 */     InputStream is = null;
/* 519 */     String filePath = this.m_notificationDir + this.m_notificationFile;
/* 520 */     if (FileUtils.checkFile(filePath, true, false) != 0)
/*     */     {
/* 522 */       return false;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 527 */       is = FileUtilsCfgBuilder.getCfgInputStream(filePath);
/* 528 */       String file = FileUtils.loadFile(is, "hda", null);
/* 529 */       if ((file.startsWith("<?hda")) || (file.startsWith("﻿<?hda")))
/*     */       {
/* 531 */         BufferedReader reader = new BufferedReader(new StringReader(file));
/*     */ 
/* 533 */         binder.receive(reader);
/* 534 */         String timeString = binder.getLocal("notificationTime");
/* 535 */         Report.trace("schemamonitor", "read publish.dat; notification time is '" + timeString + "'", null);
/*     */ 
/* 537 */         time = NumberUtils.parseLong(timeString, 0L);
/*     */       }
/*     */       else
/*     */       {
/* 541 */         time = NumberUtils.parseLong(file, 0L);
/*     */       }
/*     */ 
/* 544 */       if ((time == 0L) || (time == 1L))
/*     */       {
/* 546 */         File f = new File(filePath);
/* 547 */         time = f.lastModified();
/*     */       }
/* 549 */       binder.putLocal("notificationTime", "" + time);
/*     */ 
/* 551 */       String operation = binder.getLocal("publishOperation");
/* 552 */       if (operation == null)
/*     */       {
/* 554 */         binder.putLocal("publishOperation", "full");
/*     */       }
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 559 */       Report.trace("schemamonitor", "unable to read from notification file", ignore);
/*     */     }
/*     */     finally
/*     */     {
/* 564 */       FileUtils.closeObject(is);
/*     */     }
/*     */ 
/* 567 */     return true;
/*     */   }
/*     */ 
/*     */   public long getLastNotificationTime()
/*     */   {
/* 572 */     return this.m_notificationTimestamp;
/*     */   }
/*     */ 
/*     */   public void publish(long time, boolean isImmediate)
/*     */     throws ServiceException
/*     */   {
/* 578 */     publish(time, isImmediate, new DataBinder());
/*     */   }
/*     */ 
/*     */   public synchronized void publish(long time, boolean isImmediate, DataBinder settings)
/*     */     throws ServiceException
/*     */   {
/* 584 */     if ((!this.m_isEnabled) && (!isImmediate))
/*     */     {
/* 587 */       return;
/*     */     }
/* 589 */     touchNotificationFile(time, settings);
/* 590 */     if (!isImmediate)
/*     */       return;
/* 592 */     if ((!this.m_isWaitPublishing) || (!this.m_isEnabled))
/*     */     {
/* 594 */       if (this.m_isPublishing)
/*     */       {
/* 596 */         Report.trace("schemamonitor", "queueing publishing request", null);
/*     */       }
/* 598 */       this.m_recheckPublishing = true;
/*     */     }
/*     */     else
/*     */     {
/* 602 */       super.notify();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void abort()
/*     */   {
/* 609 */     this.m_abort = true;
/* 610 */     synchronized (this)
/*     */     {
/* 612 */       if (!this.m_isWaitPublishing)
/*     */       {
/* 614 */         this.m_recheckPublishing = true;
/*     */       }
/* 616 */       super.notify();
/*     */     }
/*     */   }
/*     */ 
/*     */   public long getTimeSeconds(String key, int defaultInSeconds)
/*     */   {
/* 622 */     long value = SharedObjects.getTypedEnvironmentInt(key, defaultInSeconds * 1000, 18, 24);
/*     */ 
/* 625 */     if (value == 0L)
/*     */     {
/* 628 */       value = 1L;
/*     */     }
/* 630 */     return value;
/*     */   }
/*     */ 
/*     */   protected synchronized void doWait(long time)
/*     */   {
/* 635 */     if (time == 0L)
/*     */     {
/* 637 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 641 */       SystemUtils.wait(this, time);
/*     */     }
/*     */     catch (InterruptedException ignore)
/*     */     {
/* 645 */       Report.trace("schemamonitor", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 651 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.StandardSchemaPublisherThread
 * JD-Core Version:    0.5.4
 */