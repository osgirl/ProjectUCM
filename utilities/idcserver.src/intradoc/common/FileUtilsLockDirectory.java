/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.Writer;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Random;
/*     */ 
/*     */ public class FileUtilsLockDirectory
/*     */ {
/*     */   public static final int MAX_LOCK_SUFFIC_COUNTER = 1000000;
/*     */   public static final String PROMOTED_LOCK_NAME = "promotedlock";
/*  43 */   public static int m_lockFileCounter = -1;
/*     */ 
/*  49 */   public static boolean m_noNewFiles = false;
/*     */ 
/*  54 */   public static Map m_dirLocks = new Hashtable();
/*     */   public boolean m_validateRenames;
/*     */   public int m_lockTimeout;
/*     */   public int m_minLockTimeout;
/*     */   public Random m_random;
/*     */   public ExecutionContext m_defaultContext;
/*     */ 
/*     */   public FileUtilsLockDirectory()
/*     */   {
/*  59 */     this.m_validateRenames = false;
/*     */ 
/*  65 */     this.m_lockTimeout = 60;
/*     */ 
/*  71 */     this.m_minLockTimeout = 5;
/*     */ 
/*  76 */     this.m_random = new Random();
/*     */ 
/*  81 */     this.m_defaultContext = new ExecutionContextAdaptor();
/*     */   }
/*     */ 
/*     */   public boolean reserveDirectoryImplement(FileDirLockData lockData, boolean promoteToLongLock, boolean specifyTimeout, long timeout, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/*  90 */     if (SystemUtils.m_verbose)
/*     */     {
/*  92 */       String dir = "lockDataDirIsNull";
/*  93 */       if (lockData.m_dir != null)
/*     */       {
/*  95 */         dir = lockData.m_dir;
/*     */       }
/*  97 */       if (FileUtils.m_neverLock)
/*     */       {
/*  99 */         Report.debug("filelock", "m_neverLock set, not locking " + dir, null);
/*     */       }
/*     */       else
/*     */       {
/* 103 */         Report.debug("filelock", "Reserving " + dir, null);
/*     */       }
/*     */     }
/* 106 */     if (FileUtils.m_neverLock)
/*     */     {
/* 108 */       return true;
/*     */     }
/*     */ 
/* 111 */     FileAttemptingLockParameters lockParameters = createLockParameters(lockData, promoteToLongLock, specifyTimeout, timeout, cxt);
/*     */ 
/* 113 */     initLockingParameters(lockParameters);
/*     */ 
/* 115 */     boolean locked = waitForShortTermDirectoryAccess(lockParameters);
/* 116 */     while ((!lockParameters.m_timedOut) && (((promoteToLongLock) || ((lockParameters.m_lockIsExternalPromoted) && (!locked)))))
/*     */     {
/* 118 */       if ((SystemUtils.m_verbose) && (!lockData.m_isPromoted))
/*     */       {
/* 120 */         logLockingTrace(lockParameters, "Short term lock is being promoted to long lock (lockIsExternal=" + lockParameters.m_lockIsExternalPromoted + ")");
/*     */       }
/*     */ 
/* 123 */       boolean longLocked = lockData.m_isPromoted;
/*     */ 
/* 125 */       if (!longLocked)
/*     */       {
/* 127 */         longLocked = waitForLongTermAccess(lockParameters);
/*     */       }
/* 129 */       if ((!longLocked) || (locked))
/*     */       {
/* 132 */         lockData.m_isPromoted = true;
/*     */ 
/* 135 */         break;
/*     */       }
/*     */ 
/* 148 */       logLockingTrace(lockParameters, "Releasing promoted long term lock back to short term lock");
/* 149 */       FileUtils.releaseLongTermLock(lockData.m_dir, "promotedlock", lockData.m_agent);
/*     */ 
/* 152 */       lockParameters.m_lockIsExternalPromoted = false;
/* 153 */       lockParameters.m_lockData.m_isPromoted = false;
/*     */ 
/* 156 */       lockParameters.m_afterLongLock = true;
/*     */ 
/* 161 */       locked = waitForShortTermDirectoryAccess(lockParameters);
/* 162 */       if (!locked)
/*     */       {
/* 164 */         logLockingTrace(lockParameters, "Unable to do short term lock after getting long term lock for " + lockData.m_dir);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 169 */     cleanUpAfterLock(lockParameters, locked);
/*     */ 
/* 172 */     lockData.m_startTime = System.currentTimeMillis();
/*     */ 
/* 176 */     if (!lockParameters.m_timedOut)
/*     */     {
/* 178 */       if (!locked)
/*     */       {
/* 180 */         Report.trace("filelock", "Blowing through a lock on directory " + lockData.m_dir + ", but that is better than being artificially blocked out forever because of a misbehaving or interrupted application.", null);
/*     */       }
/* 182 */       locked = true;
/*     */     }
/* 184 */     if (locked)
/*     */     {
/* 186 */       if (SystemUtils.m_verbose)
/*     */       {
/* 188 */         logLockingTrace(lockParameters, "Locked directory " + lockData.m_dir);
/*     */       }
/* 190 */       lockData.m_lockingThread = Thread.currentThread();
/* 191 */       putLockData(lockParameters, lockData);
/*     */     }
/* 193 */     return locked;
/*     */   }
/*     */ 
/*     */   public boolean checkForAndPromoteToLongTermLock(FileDirLockData lockData, ExecutionContext cxt) throws ServiceException
/*     */   {
/* 198 */     if (!lockData.m_isActive)
/*     */     {
/* 200 */       Report.trace("filelock", "Cannot check to promote to long term lock on " + lockData.m_dir + " when lock does not exist", null);
/* 201 */       return false;
/*     */     }
/* 203 */     long curTime = System.currentTimeMillis();
/* 204 */     long elapsedTime = curTime - lockData.m_startTime;
/* 205 */     boolean hasPromoted = false;
/* 206 */     if (elapsedTime > this.m_minLockTimeout * 500)
/*     */     {
/* 208 */       if (SystemUtils.m_verbose)
/*     */       {
/* 210 */         Report.debug("filelock", "Short term lock on " + lockData.m_dir + " has duration sufficient to justify promotion to long lock", null);
/*     */       }
/*     */ 
/* 213 */       promoteExistingLockToLongTermLock(lockData, cxt);
/* 214 */       hasPromoted = true;
/*     */     }
/* 216 */     return hasPromoted;
/*     */   }
/*     */ 
/*     */   public void promoteExistingLockToLongTermLock(FileDirLockData lockData, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 225 */     FileAttemptingLockParameters lockParameters = new FileAttemptingLockParameters(lockData);
/* 226 */     lockParameters.m_cxt = ((cxt != null) ? cxt : this.m_defaultContext);
/* 227 */     lockParameters.m_promoteToLongLock = true;
/*     */     try
/*     */     {
/* 230 */       if (!lockData.m_isActive)
/*     */       {
/* 232 */         String msg = LocaleUtils.encodeMessage("csFileUtilsCannotPromoteNonExistentLock", null, lockData.m_dir, lockData.m_agent);
/* 233 */         throw new ServiceException(msg);
/*     */       }
/* 235 */       if (!lockData.m_isPromoted)
/*     */       {
/* 237 */         if (SystemUtils.m_verbose)
/*     */         {
/* 239 */           logLockingTrace(lockParameters, "Direct call to promote lock " + lockData.m_dir);
/*     */         }
/* 241 */         waitForLongTermAccess(lockParameters);
/* 242 */         lockData.m_isPromoted = true;
/*     */       }
/*     */       else
/*     */       {
/* 246 */         logLockingTrace(lockParameters, "Promoting to long term lock a lock that has already been promoted, call is ignored " + lockData.m_dir);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 253 */         putLockData(lockParameters, lockData);
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 257 */         Report.trace("system", null, t);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public FileAttemptingLockParameters createLockParameters(FileDirLockData lockData, boolean promoteToLongLock, boolean specifyTimeout, long timeout, ExecutionContext cxt)
/*     */   {
/* 265 */     String dir = lockData.m_dir;
/* 266 */     File temp1 = FileUtilsCfgBuilder.getCfgFile(dir + "lockwait.dat", "Lock", false);
/* 267 */     String lockOnSuffix = calculateLockingFileSuffix(lockData);
/* 268 */     File temp2 = FileUtilsCfgBuilder.getCfgFile(dir + "lockon" + lockOnSuffix + ".dat", "Lock", false);
/* 269 */     FileAttemptingLockParameters lockParameters = new FileAttemptingLockParameters(temp1, temp2, lockData);
/* 270 */     lockParameters.m_promoteToLongLock = promoteToLongLock;
/* 271 */     lockParameters.m_specifyTimeout = specifyTimeout;
/* 272 */     lockParameters.m_timeout = timeout;
/* 273 */     lockParameters.m_cxt = ((cxt != null) ? cxt : this.m_defaultContext);
/* 274 */     return lockParameters;
/*     */   }
/*     */ 
/*     */   public void initLockingParameters(FileAttemptingLockParameters lockParameters)
/*     */   {
/* 279 */     String dir = lockParameters.m_lockData.m_dir;
/* 280 */     lockParameters.m_reserveLockFile = FileUtilsCfgBuilder.getCfgFile(dir + "lockreserve.dat", "Lock", false);
/* 281 */     boolean hasLockStarted = checkHasBeenStartedLock(lockParameters.m_lockData);
/* 282 */     lockParameters.m_lockStarted = hasLockStarted;
/* 283 */     lockParameters.m_waitInterval = computeBaseWaitInterval(lockParameters);
/* 284 */     lockParameters.m_totalTimeAllowed = computeTotalWaitTime(lockParameters);
/* 285 */     lockParameters.m_totalTimeWaited = 0;
/* 286 */     lockParameters.m_timedOut = false;
/* 287 */     lockParameters.m_afterLongLock = false;
/* 288 */     lockParameters.m_lockIsExternalPromoted = false;
/* 289 */     long startWaitingTime = System.currentTimeMillis();
/* 290 */     lockParameters.m_lockData.m_startWaitForLockTime = startWaitingTime;
/* 291 */     if (lockParameters.m_specifyTimeout)
/*     */     {
/* 293 */       lockParameters.m_expireTime = (startWaitingTime + lockParameters.m_timeout);
/*     */     }
/*     */ 
/* 297 */     lockParameters.m_lockIsExternalReserve = lockParameters.m_reserveLockFile.exists();
/*     */   }
/*     */ 
/*     */   public File createLongTermLockFilePath(FileAttemptingLockParameters lockParameters)
/*     */   {
/* 302 */     return FileUtilsCfgBuilder.getCfgFile(lockParameters.m_lockData.m_dir + "longtermlock.tmp", "Lock", false);
/*     */   }
/*     */ 
/*     */   public String calculateLockingFileSuffix(FileDirLockData lockData)
/*     */   {
/* 307 */     if (lockData == null)
/*     */     {
/* 309 */       return "";
/*     */     }
/* 311 */     return "" + lockData.m_numGlobalCount;
/*     */   }
/*     */ 
/*     */   public boolean checkHasBeenStartedLock(FileDirLockData lockData)
/*     */   {
/* 316 */     return (lockData != null) && (lockData.m_numDirLocks > 0L);
/*     */   }
/*     */ 
/*     */   public boolean checkHasBeenPromotedToLongLock(FileAttemptingLockParameters lockParameters)
/*     */   {
/* 321 */     boolean hasBeenPromoted = FileUtils.checkLongTermLock(lockParameters.m_lockData.m_dir, "promotedlock");
/* 322 */     return hasBeenPromoted;
/*     */   }
/*     */ 
/*     */   public boolean checkTimeout(FileAttemptingLockParameters lockParameters)
/*     */   {
/* 327 */     long curTime = System.currentTimeMillis();
/* 328 */     lockParameters.m_timeRemaining = (lockParameters.m_expireTime - curTime);
/* 329 */     return lockParameters.m_timeRemaining <= 0L;
/*     */   }
/*     */ 
/*     */   public int computeBaseWaitInterval(FileAttemptingLockParameters lockParameters)
/*     */   {
/* 335 */     return this.m_lockTimeout;
/*     */   }
/*     */ 
/*     */   public int computeNextSleepInterval(int loopIndex, FileAttemptingLockParameters lockParameters)
/*     */   {
/* 340 */     int mult = loopIndex + 1;
/* 341 */     if (lockParameters.m_isLockOurReserve)
/*     */     {
/* 344 */       if (mult >= 4)
/*     */       {
/* 346 */         mult -= 2;
/*     */       }
/* 348 */       else if (mult >= 2)
/*     */       {
/* 350 */         mult = 2;
/*     */       }
/*     */     }
/* 353 */     else if ((lockParameters.m_lockIsExternalReserve) && (!lockParameters.m_afterLongLock))
/*     */     {
/* 357 */       int adjMult = this.m_random.nextInt() % 5;
/* 358 */       if (adjMult < 0)
/*     */       {
/* 360 */         adjMult = -adjMult;
/*     */       }
/*     */ 
/* 363 */       mult += adjMult + 1;
/*     */     }
/* 365 */     if (mult > 2)
/*     */     {
/* 368 */       mult *= (mult + 1) / 2;
/*     */     }
/*     */ 
/* 372 */     if (mult > 100)
/*     */     {
/* 374 */       mult = 100;
/*     */     }
/* 376 */     int sleepInterval = mult * lockParameters.m_waitInterval;
/* 377 */     if ((sleepInterval > lockParameters.m_timeRemaining) && (lockParameters.m_specifyTimeout))
/*     */     {
/* 379 */       if (lockParameters.m_timeRemaining > 0L)
/*     */       {
/* 381 */         sleepInterval = (int)lockParameters.m_timeRemaining;
/*     */       }
/*     */       else
/*     */       {
/* 385 */         sleepInterval = 0;
/*     */       }
/*     */     }
/* 388 */     if (SystemUtils.m_verbose)
/*     */     {
/* 390 */       logLockingTrace(lockParameters, "Next sleep interval is " + sleepInterval);
/*     */     }
/* 392 */     return sleepInterval;
/*     */   }
/*     */ 
/*     */   public int computeTotalWaitTime(FileAttemptingLockParameters lockParameters)
/*     */   {
/* 397 */     int baseTimeout = (lockParameters.m_lockStarted) ? this.m_lockTimeout : this.m_minLockTimeout;
/*     */ 
/* 400 */     return 1000 * baseTimeout;
/*     */   }
/*     */ 
/*     */   protected boolean waitForShortTermDirectoryAccess(FileAttemptingLockParameters params)
/*     */     throws ServiceException
/*     */   {
/* 416 */     boolean locked = false;
/* 417 */     boolean isTimedOut = false;
/*     */ 
/* 420 */     boolean afterRemovingLongLock = params.m_afterLongLock;
/* 421 */     if (afterRemovingLongLock)
/*     */     {
/* 424 */       params.m_isLockOurReserve = false;
/*     */     }
/* 426 */     boolean lockReserveExists = params.m_lockIsExternalReserve;
/* 427 */     boolean lockStarted = params.m_lockStarted;
/* 428 */     boolean specifyTimeout = params.m_specifyTimeout;
/* 429 */     int totalTimeWaited = params.m_totalTimeWaited;
/* 430 */     for (int i = 0; (i < 5) && (lockReserveExists) && (lockStarted) && (!isTimedOut) && (!afterRemovingLongLock); ++i)
/*     */     {
/*     */       try
/*     */       {
/* 434 */         if (SystemUtils.m_verbose)
/*     */         {
/* 436 */           Report.debug("filelock", "Waiting " + i, null);
/*     */         }
/* 438 */         int nextSleepInterval = computeNextSleepInterval(i, params);
/* 439 */         SystemUtils.sleep(nextSleepInterval);
/* 440 */         totalTimeWaited += nextSleepInterval;
/*     */ 
/* 442 */         if (i == 4) {
/*     */           break label175;
/*     */         }
/*     */ 
/* 446 */         lockReserveExists = params.m_reserveLockFile.exists();
/* 447 */         if (specifyTimeout)
/*     */         {
/* 449 */           isTimedOut = checkTimeout(params);
/*     */         }
/*     */       }
/*     */       catch (Throwable ignore)
/*     */       {
/* 454 */         Report.trace("system", null, ignore);
/*     */       }
/*     */     }
/* 457 */     label175: params.m_lockIsExternalReserve = lockReserveExists;
/*     */ 
/* 459 */     i = 0;
/* 460 */     File temp1 = params.m_beforeRenameFile;
/* 461 */     File temp2 = params.m_afterRenameFile;
/* 462 */     String dir = params.m_lockData.m_dir;
/* 463 */     boolean hasBeenPromoted = false;
/* 464 */     while ((!isTimedOut) && (!hasBeenPromoted))
/*     */     {
/* 466 */       if (temp1.renameTo(temp2))
/*     */       {
/* 480 */         if (this.m_validateRenames)
/*     */         {
/* 482 */           if (temp2.exists())
/*     */           {
/* 484 */             SystemUtils.sleep(1L);
/* 485 */             if (temp2.exists())
/*     */             {
/* 487 */               locked = true;
/* 488 */               break;
/*     */             }
/* 490 */             logLockingTrace(params, "Renamed file vanished.");
/*     */           }
/*     */           else
/*     */           {
/* 494 */             logLockingTrace(params, "Renamed succeeded but target file was not present.");
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 501 */           locked = true;
/* 502 */           break;
/*     */         }
/*     */       }
/*     */ 
/* 506 */       if (totalTimeWaited > params.m_totalTimeAllowed) {
/*     */         break;
/*     */       }
/*     */ 
/* 510 */       if (i == 0)
/*     */       {
/* 512 */         File fDir = FileUtilsCfgBuilder.getCfgFile(dir, null, true);
/* 513 */         if (!fDir.exists())
/*     */         {
/* 515 */           String msg = FileUtils.getErrorMsg(dir, false, -16);
/* 516 */           throw new ServiceException(-16, msg);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 521 */       if ((i >= 3) && (i % 3 == 0))
/*     */       {
/* 523 */         hasBeenPromoted = checkHasBeenPromotedToLongLock(params);
/* 524 */         if (hasBeenPromoted)
/*     */         {
/* 526 */           if (!SystemUtils.m_verbose)
/*     */             break;
/* 528 */           logLockingTrace(params, "Aborting short term lock attempt because long lock by other agent was detected, will do temporary long lock"); break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 535 */       if (SystemUtils.m_verbose)
/*     */       {
/* 537 */         Report.debug("filelock", "Lock bounce on loop " + i, null);
/*     */       }
/* 539 */       int nextSleepInterval = computeNextSleepInterval(i, params);
/* 540 */       boolean success = SystemUtils.sleep(nextSleepInterval);
/* 541 */       totalTimeWaited += nextSleepInterval;
/* 542 */       if (specifyTimeout)
/*     */       {
/* 544 */         isTimedOut = checkTimeout(params);
/*     */       }
/*     */ 
/* 548 */       boolean reserveLoop = false;
/* 549 */       if ((((!params.m_isLockOurReserve) || (i % 3 == 0))) && ((
/* 551 */         (params.m_lockData.m_numDirLocks == 0L) || (i > 3))))
/*     */       {
/* 553 */         reserveLoop = true;
/*     */       }
/*     */ 
/* 556 */       if ((success) && (reserveLoop) && (!isTimedOut))
/*     */       {
/*     */         try
/*     */         {
/* 560 */           if (params.m_reserveLockFile.exists())
/*     */           {
/* 562 */             if (!params.m_isLockOurReserve)
/*     */             {
/* 564 */               String msg = "contention to reserve attempt " + i + " on " + dir;
/*     */ 
/* 566 */               if (SystemUtils.m_verbose)
/*     */               {
/* 568 */                 Report.debug("filelock", null, new ServiceException(msg));
/*     */               }
/*     */               else
/*     */               {
/* 573 */                 Report.trace("filelock", msg, null);
/*     */               }
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 579 */             IdcStringBuilder msg = new IdcStringBuilder("creating reserve lock file in ");
/*     */ 
/* 581 */             msg.m_disableToStringReleaseBuffers = true;
/* 582 */             msg.append(dir).append(" on loop ");
/*     */ 
/* 584 */             msg.append(i);
/* 585 */             if (SystemUtils.m_verbose)
/*     */             {
/* 587 */               Report.debug("filelock", null, new StackTrace(msg.toString()));
/*     */             }
/*     */             else
/*     */             {
/* 592 */               Report.trace("filelock", msg.toString(), null);
/*     */             }
/* 594 */             if (m_noNewFiles)
/*     */             {
/* 596 */               throw new AssertionError(msg.toString());
/*     */             }
/* 598 */             Writer out = FileUtilsCfgBuilder.getCfgWriter(params.m_reserveLockFile);
/* 599 */             msg.setLength(0);
/* 600 */             msg.append("File for reserving for thread \"");
/* 601 */             msg.append(params.m_lockData.m_agent);
/* 602 */             msg.append("\"\n");
/*     */             try
/*     */             {
/* 605 */               msg.writeTo(out);
/*     */             }
/*     */             finally
/*     */             {
/* 609 */               FileUtils.closeObject(out);
/*     */             }
/* 611 */             msg.releaseBuffers();
/* 612 */             params.m_isLockOurReserve = true;
/*     */           }
/* 614 */           nextSleepInterval = computeNextSleepInterval(i, params);
/* 615 */           SystemUtils.sleep(nextSleepInterval);
/* 616 */           totalTimeWaited += nextSleepInterval;
/* 617 */           if (specifyTimeout)
/*     */           {
/* 619 */             isTimedOut = checkTimeout(params);
/*     */           }
/*     */         }
/*     */         catch (Exception ignore)
/*     */         {
/* 624 */           Report.trace("filelock", null, ignore);
/*     */         }
/*     */       }
/* 627 */       ++i;
/*     */     }
/* 629 */     if ((!hasBeenPromoted) && (!isTimedOut) && (((i > 3) || (!locked))))
/*     */     {
/* 632 */       hasBeenPromoted = checkHasBeenPromotedToLongLock(params);
/* 633 */       if ((hasBeenPromoted) && 
/* 635 */         (SystemUtils.m_verbose))
/*     */       {
/* 637 */         if (locked)
/*     */         {
/* 639 */           logLockingTrace(params, "Lock was successful, but external lock long is causing us to promote to a long lock");
/*     */         }
/*     */         else
/*     */         {
/* 643 */           logLockingTrace(params, "After timing out on short term lock, external long lock detected so we promote to long term lock");
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 649 */     params.m_lockIsExternalPromoted = hasBeenPromoted;
/* 650 */     params.m_timedOut = isTimedOut;
/* 651 */     params.m_totalTimeWaited = totalTimeWaited;
/* 652 */     return locked;
/*     */   }
/*     */ 
/*     */   public boolean waitForLongTermAccess(FileAttemptingLockParameters params) throws ServiceException
/*     */   {
/* 657 */     boolean locked = FileUtils.reserveLongTermLock(params.m_lockData.m_dir, "promotedlock", params.m_lockData.m_agent, params.m_timeRemaining, !params.m_specifyTimeout);
/*     */ 
/* 660 */     if (params.m_specifyTimeout)
/*     */     {
/* 662 */       params.m_timedOut = checkTimeout(params);
/*     */     }
/* 664 */     if (!locked)
/*     */     {
/* 667 */       params.m_timedOut = true;
/* 668 */       logLockingTrace(params, "Promotion to long term lock failed, assuming it timed out");
/*     */     }
/* 670 */     return locked;
/*     */   }
/*     */ 
/*     */   public void cleanUpAfterLock(FileAttemptingLockParameters params, boolean locked)
/*     */   {
/* 679 */     if ((params.m_lockIsExternalReserve) || (params.m_isLockOurReserve))
/*     */     {
/* 681 */       Report.trace("filelock", "Removing lock request on " + params.m_reserveLockFile, null);
/* 682 */       params.m_reserveLockFile.delete();
/*     */     }
/*     */ 
/* 685 */     if ((!locked) && (!params.m_timedOut))
/*     */     {
/* 688 */       Writer out = null;
/*     */       try
/*     */       {
/* 691 */         String dir = params.m_lockData.m_dir;
/* 692 */         boolean hasTemp2Files = false;
/* 693 */         String[] names = FileUtils.getMatchingFileNames(dir, "lockon*.dat");
/* 694 */         if ((names != null) && (names.length > 0))
/*     */         {
/* 696 */           hasTemp2Files = true;
/* 697 */           for (int j = 0; j < names.length; ++j)
/*     */           {
/* 699 */             FileUtils.deleteFile(dir + names[j]);
/*     */           }
/*     */         }
/* 702 */         params.m_beforeRenameFile.delete();
/* 703 */         out = FileUtilsCfgBuilder.getCfgWriter(params.m_afterRenameFile);
/* 704 */         IdcStringBuilder msg = new IdcStringBuilder();
/* 705 */         msg.append("Lock file for thread \"");
/* 706 */         msg.append(params.m_lockData.m_agent);
/* 707 */         msg.append("\"\n");
/* 708 */         msg.writeTo(out);
/* 709 */         out.close();
/*     */ 
/* 711 */         if (!hasTemp2Files)
/*     */         {
/* 713 */           logLockingTrace(params, "Creating lock file in directory that had none");
/* 714 */           if (m_noNewFiles)
/*     */           {
/* 716 */             throw new AssertionError("!$Creating lock file in " + dir);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 721 */           logLockingTrace(params, "Lock blowthrough");
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Throwable e)
/*     */       {
/* 727 */         Report.trace("filelock", null, e);
/*     */       }
/*     */       finally
/*     */       {
/* 731 */         FileUtils.closeObject(out);
/*     */       }
/*     */     }
/* 734 */     if ((!params.m_timedOut) || (!locked) || (!params.m_lockData.m_isPromoted))
/*     */       return;
/* 736 */     FileUtils.releaseLongTermLock(params.m_lockData.m_dir, "promotedlock", params.m_lockData.m_agent);
/*     */   }
/*     */ 
/*     */   public void releaseDirectoryImplement(String dir, ExecutionContext cxt)
/*     */   {
/* 742 */     dir = FileUtils.directorySlashes(dir);
/* 743 */     FileDirLockData lockData = retrieveAndClearLockData(dir);
/* 744 */     if ((lockData == null) || (!lockData.m_isActive))
/*     */     {
/* 746 */       Report.trace("filelock", dir + "--Already cleared or never created locking data.", null);
/* 747 */       return;
/*     */     }
/*     */ 
/* 750 */     boolean isLockPromoted = isLockPromoted(lockData);
/* 751 */     FileAttemptingLockParameters lockParameters = createLockParameters(lockData, isLockPromoted, false, 0L, cxt);
/*     */     try
/*     */     {
/* 754 */       if (!lockParameters.m_afterRenameFile.exists())
/*     */       {
/* 756 */         logLockingTrace(lockParameters, "Lock file missing before lock release.");
/*     */       }
/* 758 */       lockParameters.m_afterRenameFile.renameTo(lockParameters.m_beforeRenameFile);
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/* 762 */       Report.trace("system", null, e);
/*     */     }
/* 764 */     if (isLockPromoted)
/*     */     {
/* 766 */       logLockingTrace(lockParameters, "Releasing promoted long term lock");
/* 767 */       FileUtils.releaseLongTermLock(lockData.m_dir, "promotedlock", lockData.m_agent);
/*     */     }
/* 769 */     long curTime = System.currentTimeMillis();
/* 770 */     long diff = curTime - lockData.m_startTime;
/* 771 */     if (diff > 6000L)
/*     */     {
/* 773 */       if (isLockPromoted)
/*     */       {
/* 775 */         logLockingTrace(lockParameters, "Lock was promoted and held for a long time (" + diff + ").");
/*     */       }
/*     */       else
/*     */       {
/* 779 */         logLockingTrace(lockParameters, "Lock held too long for a short term directory lock (" + diff + ").");
/*     */       }
/*     */     }
/* 782 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 784 */     logLockingTrace(lockParameters, "Released");
/*     */   }
/*     */ 
/*     */   public FileDirLockData createLockingData(String dir, String agent)
/*     */   {
/* 790 */     FileDirLockData lockData = null;
/* 791 */     synchronized (m_dirLocks)
/*     */     {
/* 793 */       FileDirLockData curLockData = (FileDirLockData)m_dirLocks.get(dir);
/* 794 */       if (m_lockFileCounter < 0)
/*     */       {
/* 796 */         m_lockFileCounter = new Random().nextInt();
/* 797 */         if (m_lockFileCounter < 0)
/*     */         {
/* 799 */           m_lockFileCounter = -m_lockFileCounter;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 804 */         m_lockFileCounter += 1;
/*     */       }
/* 806 */       m_lockFileCounter %= 1000000;
/* 807 */       long numDirLock = (curLockData != null) ? curLockData.m_numDirLocks : 0L;
/* 808 */       lockData = new FileDirLockData(dir, agent, m_lockFileCounter, numDirLock);
/* 809 */       lockData.m_isActive = true;
/* 810 */       if (numDirLock == 0L)
/*     */       {
/* 812 */         FileDirLockData emptyLock = new FileDirLockData();
/* 813 */         m_dirLocks.put(dir, emptyLock);
/*     */       }
/*     */     }
/* 816 */     return lockData;
/*     */   }
/*     */ 
/*     */   protected FileDirLockData retrieveAndClearLockData(String dir)
/*     */   {
/* 821 */     FileDirLockData lockData = null;
/* 822 */     synchronized (m_dirLocks)
/*     */     {
/* 824 */       lockData = (FileDirLockData)m_dirLocks.get(dir);
/* 825 */       if (lockData == null)
/*     */       {
/* 827 */         lockData = new FileDirLockData();
/*     */       }
/*     */ 
/* 830 */       FileDirLockData emptyLock = new FileDirLockData();
/* 831 */       emptyLock.m_numDirLocks = lockData.m_numDirLocks;
/* 832 */       if (lockData.m_isActive)
/*     */       {
/* 834 */         emptyLock.m_numDirLocks += 1L;
/*     */       }
/* 836 */       m_dirLocks.put(dir, emptyLock);
/*     */     }
/* 838 */     return lockData;
/*     */   }
/*     */ 
/*     */   public FileDirLockData retrieveLockingData(String dir)
/*     */   {
/* 846 */     FileDirLockData lockData = null;
/* 847 */     synchronized (m_dirLocks)
/*     */     {
/* 849 */       lockData = (FileDirLockData)m_dirLocks.get(dir);
/* 850 */       if (lockData == null)
/*     */       {
/* 852 */         lockData = new FileDirLockData();
/*     */       }
/*     */     }
/* 855 */     return lockData;
/*     */   }
/*     */ 
/*     */   protected void putLockData(FileAttemptingLockParameters params, FileDirLockData lockData)
/*     */   {
/* 860 */     synchronized (m_dirLocks)
/*     */     {
/* 862 */       String dir = lockData.m_dir;
/* 863 */       FileDirLockData curLockData = (FileDirLockData)m_dirLocks.get(dir);
/*     */ 
/* 866 */       if (lockData != curLockData)
/*     */       {
/* 868 */         m_dirLocks.put(dir, lockData);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean isLockPromoted(FileDirLockData lockData)
/*     */   {
/* 875 */     return (lockData != null) && (lockData.m_isPromoted);
/*     */   }
/*     */ 
/*     */   public void logLockingTrace(FileAttemptingLockParameters params, String msg)
/*     */   {
/* 880 */     FileDirLockData lockData = params.m_lockData;
/* 881 */     if (lockData == null)
/*     */     {
/* 883 */       Report.trace("filelock", "<no active lock>--" + msg, null);
/*     */     }
/*     */     else
/*     */     {
/* 887 */       Report.trace("filelock", lockData.m_dir + "--" + lockData.m_agent + "--" + msg, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean hasCurrentProcessLockedDirectoryBefore(String dir)
/*     */   {
/* 893 */     FileDirLockData lockData = (FileDirLockData)m_dirLocks.get(dir);
/* 894 */     return lockData != null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 899 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97029 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FileUtilsLockDirectory
 * JD-Core Version:    0.5.4
 */