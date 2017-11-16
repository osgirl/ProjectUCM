/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.CharConversionException;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ import java.lang.reflect.InvocationTargetException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class FileUtilsLockThread
/*     */   implements Runnable
/*     */ {
/*     */   public Hashtable m_locks;
/*     */   public Vector m_locksList;
/*     */   public boolean[] m_lockObject;
/*     */   public int m_touchMonitorInterval;
/*     */   public int m_randomCount;
/*     */   public boolean m_useRename;
/*     */   public boolean m_isInit;
/*     */   public boolean m_extraTrace;
/*     */ 
/*     */   public FileUtilsLockThread()
/*     */   {
/*  31 */     this.m_locks = new Hashtable();
/*  32 */     this.m_locksList = new IdcVector();
/*     */ 
/*  34 */     this.m_lockObject = new boolean[] { true };
/*     */ 
/*  36 */     this.m_touchMonitorInterval = 30000;
/*  37 */     this.m_randomCount = 0;
/*     */ 
/*  39 */     this.m_useRename = false;
/*     */ 
/*  41 */     this.m_isInit = false;
/*  42 */     this.m_extraTrace = false;
/*     */   }
/*     */ 
/*     */   public void checkInit() {
/*  46 */     synchronized (this.m_lockObject)
/*     */     {
/*  48 */       if (!this.m_isInit)
/*     */       {
/*  52 */         if (!this.m_useRename)
/*     */         {
/*  54 */           this.m_useRename = (!ClassHelperUtils.checkMethodExistence(ClassHelper.m_fileClass, "createNewFile", null));
/*     */         }
/*     */ 
/*  57 */         this.m_isInit = true;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean usesAtomicCreateFileMethod()
/*     */   {
/*  67 */     checkInit();
/*  68 */     return !this.m_useRename;
/*     */   }
/*     */ 
/*     */   public boolean atomicCreateFile(File file)
/*     */     throws IOException
/*     */   {
/*  78 */     boolean lockedIt = false;
/*     */     try
/*     */     {
/*  81 */       Object o = ClassHelperUtils.executeMethod(file, "createNewFile", null, null);
/*  82 */       if ((o != null) && (o instanceof Boolean))
/*     */       {
/*  84 */         Boolean r = (Boolean)o;
/*  85 */         lockedIt = r.booleanValue();
/*     */       }
/*     */     }
/*     */     catch (InvocationTargetException e)
/*     */     {
/*  90 */       Throwable inner = e.getTargetException();
/*  91 */       if (inner instanceof IOException)
/*     */       {
/*  93 */         throw ((IOException)inner);
/*     */       }
/*     */ 
/*  96 */       Report.trace("filelonglock", null, e);
/*  97 */       throw new IOException(inner.getMessage());
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 101 */       Report.trace("filelonglock", null, e);
/* 102 */       throw new IOException(e.getMessage());
/*     */     }
/* 104 */     return lockedIt;
/*     */   }
/*     */ 
/*     */   public String quickDirFix(String dir)
/*     */   {
/* 109 */     int dirLen = dir.length();
/* 110 */     if (dirLen == 0)
/*     */     {
/* 112 */       return "./";
/*     */     }
/* 114 */     char ch = dir.charAt(dirLen - 1);
/* 115 */     if ((ch != '\\') && (ch != '/'))
/*     */     {
/* 117 */       dir = new StringBuilder().append(dir).append("/").toString();
/*     */     }
/* 119 */     return dir;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 124 */     long lastTime = System.currentTimeMillis();
/* 125 */     boolean checkStatusAll = false;
/* 126 */     while (!SystemUtils.m_isServerStopped)
/*     */     {
/*     */       try
/*     */       {
/* 133 */         Vector dirsToScanList = new IdcVector();
/* 134 */         Hashtable dirsToScan = new Hashtable();
/*     */ 
/* 139 */         Vector clonedLockList = (Vector)this.m_locksList.clone();
/* 140 */         int nlocks = clonedLockList.size();
/* 141 */         for (int i = 0; i < nlocks; ++i)
/*     */         {
/* 143 */           FileLockData data = (FileLockData)clonedLockList.elementAt(i);
/* 144 */           if ((data.m_state == 1) && (data.m_isLocked))
/*     */           {
/* 146 */             synchronized (data.m_lockObj)
/*     */             {
/* 149 */               if ((data.m_state == 1) && (data.m_isLocked))
/*     */               {
/* 151 */                 if (SystemUtils.m_verbose)
/*     */                 {
/* 153 */                   Report.debug("filelonglock", new StringBuilder().append("Background touching file ").append(data.m_fullPath).append(" for ").append(data.m_agent).append(".").toString(), null);
/*     */                 }
/* 155 */                 updateLockFile(data);
/* 156 */                 data.m_isProcessed = true;
/*     */               }
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 162 */             if (dirsToScan.get(data.m_dir) == null)
/*     */             {
/* 164 */               dirsToScan.put(data.m_dir, "1");
/* 165 */               dirsToScanList.addElement(data.m_dir);
/*     */             }
/* 167 */             data.m_isProcessed = false;
/* 168 */             if (data.m_state == 1)
/*     */             {
/* 171 */               Report.trace("system", new StringBuilder().append("Unlocked file locked by this process ").append(data.m_fullPath).append(" ").append(data.m_agent).append(".").toString(), null);
/*     */             }
/*     */           }
/* 174 */           if ((!SystemUtils.m_verbose) || (data.m_waitingAgents.size() <= 0))
/*     */             continue;
/* 176 */           reportWaitingAgents(data, checkStatusAll);
/*     */         }
/*     */ 
/* 180 */         long curTime = System.currentTimeMillis();
/* 181 */         long diff = curTime - lastTime;
/* 182 */         lastTime = curTime;
/*     */ 
/* 190 */         long reduceInterval = diff + this.m_touchMonitorInterval / 2;
/* 191 */         if (diff > this.m_touchMonitorInterval / 3)
/*     */         {
/* 193 */           Thread thread = Thread.currentThread();
/* 194 */           String errMsg = new StringBuilder().append("The background thread ").append(thread.getName()).append(" is taking too long (").append(diff).append(" millis).").toString();
/* 195 */           if (nlocks != 0)
/*     */           {
/* 197 */             errMsg = new StringBuilder().append(errMsg).append(" The following processes are currently being watched:").toString();
/*     */           }
/* 199 */           for (int i = 0; i < nlocks; ++i)
/*     */           {
/* 201 */             FileLockData data = (FileLockData)this.m_locksList.elementAt(i);
/* 202 */             errMsg = new StringBuilder().append(errMsg).append("\n----").append(data.m_fullPath).append("   ").append(data.m_agent).toString();
/*     */           }
/* 204 */           Report.trace("system", errMsg, null);
/*     */         }
/* 206 */         int sleepTime = 200;
/* 207 */         if ((reduceInterval < this.m_touchMonitorInterval) && (reduceInterval >= 0L))
/*     */         {
/* 209 */           sleepTime = this.m_touchMonitorInterval - (int)reduceInterval;
/*     */         }
/* 211 */         SystemUtils.sleep(sleepTime);
/* 212 */         lastTime += sleepTime;
/* 213 */         if ((SystemUtils.m_verbose) && (this.m_extraTrace))
/*     */         {
/* 215 */           Report.debug("filelonglock", new StringBuilder().append("File long lock thread slept for ").append(sleepTime).append(" milliseconds.").toString(), null);
/*     */         }
/*     */ 
/* 219 */         Hashtable badDirs = new Hashtable();
/* 220 */         if (checkStatusAll)
/*     */         {
/* 223 */           int ndirs = dirsToScanList.size();
/* 224 */           for (int i = 0; i < ndirs; ++i)
/*     */           {
/* 226 */             String dir = (String)dirsToScanList.elementAt(i);
/* 227 */             String[] list = FileUtils.getMatchingFileNames(dir, "*.flck");
/* 228 */             if ((list == null) || (list.length == 0))
/*     */             {
/* 230 */               if (FileUtils.checkFile(dir, false, true) == 0)
/*     */                 continue;
/* 232 */               Report.trace("filelonglock", new StringBuilder().append("Could not access directory ").append(dir).toString(), null);
/* 233 */               badDirs.put(dir, "1");
/*     */             }
/*     */             else
/*     */             {
/* 238 */               processLockFiles(list, dir);
/*     */             }
/*     */           }
/*     */ 
/* 242 */           expireUnmaintainedLocks(badDirs);
/* 243 */           checkStatusAll = false;
/*     */         }
/*     */         else
/*     */         {
/* 247 */           checkStatusAll = true;
/*     */         }
/*     */       }
/*     */       catch (Throwable ignore)
/*     */       {
/* 252 */         Report.trace("filelonglock", null, ignore);
/*     */ 
/* 258 */         SystemUtils.sleepRandom(150L, 250L);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void reportWaitingAgents(FileLockData data, boolean isFullScan)
/*     */   {
/* 266 */     if (data.m_lockObj == null)
/*     */       return;
/* 268 */     synchronized (data.m_lockObj)
/*     */     {
/* 270 */       StringBuffer agentsBuf = new StringBuffer();
/* 271 */       for (int j = 0; j < data.m_waitingAgents.size(); ++j)
/*     */       {
/* 273 */         if (j > 0)
/*     */         {
/* 275 */           agentsBuf.append(",");
/*     */         }
/* 277 */         Object[] o = (Object[])(Object[])data.m_waitingAgents.elementAt(j);
/* 278 */         agentsBuf.append(o[0].toString());
/* 279 */         agentsBuf.append("(");
/* 280 */         agentsBuf.append(o[1].toString());
/* 281 */         agentsBuf.append(")");
/*     */       }
/* 283 */       String activityString = (isFullScan) ? " scanning for missing or unmodified files since last scan" : " not scanning for lock state change";
/* 284 */       Report.trace("filelonglock", new StringBuilder().append("Agents ").append(agentsBuf.toString()).append(" waiting for ").append(data.m_id).append(" locked by ").append(data.m_agent).append(activityString).toString(), null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void processLockFiles(String[] list, String dir)
/*     */     throws CharConversionException
/*     */   {
/* 292 */     for (int j = 0; j < list.length; ++j)
/*     */     {
/* 294 */       String fileName = list[j].toLowerCase();
/* 295 */       String path = new StringBuilder().append(dir).append(fileName).toString();
/* 296 */       FileLockData curLock = (FileLockData)this.m_locks.get(path);
/* 297 */       File file = FileUtilsCfgBuilder.getCfgFile(path, "Lock", false);
/* 298 */       boolean isNew = false;
/* 299 */       if (curLock == null)
/*     */       {
/* 301 */         curLock = createNewAutoDetectedLock(file, dir, fileName, path);
/* 302 */         isNew = true;
/*     */       }
/*     */ 
/* 305 */       boolean processOwns = curLock.m_state == 1;
/* 306 */       boolean addToActive = true;
/* 307 */       if (!isNew)
/*     */       {
/* 309 */         long newLastModified = file.lastModified();
/* 310 */         if (!processOwns)
/*     */         {
/* 312 */           if ((newLastModified != curLock.m_lastModified) && (newLastModified > 0L) && (!curLock.m_isTempFile))
/*     */           {
/* 314 */             curLock.m_state = 3;
/*     */           }
/* 318 */           else if (curLock.m_state == 4)
/*     */           {
/* 321 */             addToActive = false;
/* 322 */             Report.trace("filelonglock", new StringBuilder().append("++FileUtils lock ").append(curLock.m_id).append(" deleted (not maintained) for agent ").append(curLock.m_agent).append(".").toString(), null);
/*     */ 
/* 324 */             if (newLastModified > 0L)
/*     */             {
/* 326 */               file.delete();
/*     */             }
/* 328 */             curLock.m_state = 0;
/* 329 */             curLock.m_isLocked = false;
/*     */           }
/*     */           else
/*     */           {
/* 333 */             curLock.m_state = 4;
/*     */           }
/*     */         }
/*     */ 
/* 337 */         curLock.m_lastModified = newLastModified;
/* 338 */         if ((curLock.m_isLocked) && (curLock.m_state != 4))
/*     */         {
/* 340 */           curLock.m_lastTimestamp = System.currentTimeMillis();
/*     */         }
/*     */       }
/* 343 */       if (!addToActive)
/*     */         continue;
/* 345 */       if ((isNew) || (!curLock.m_isLocked))
/*     */       {
/* 347 */         curLock.m_isLocked = true;
/* 348 */         if (SystemUtils.m_verbose)
/*     */         {
/* 350 */           Report.debug("filelonglock", new StringBuilder().append("Lock ").append(curLock.m_id).append(" now active for agent ").append(curLock.m_agent).append(".").toString(), null);
/*     */         }
/*     */       }
/*     */ 
/* 354 */       synchronized (this.m_lockObject)
/*     */       {
/* 356 */         editLockList(path, curLock);
/*     */       }
/*     */ 
/* 359 */       curLock.m_isProcessed = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void expireUnmaintainedLocks(Hashtable badDirs)
/*     */   {
/* 369 */     synchronized (this.m_lockObject)
/*     */     {
/* 371 */       int index = 0;
/* 372 */       while (index < this.m_locksList.size())
/*     */       {
/* 374 */         boolean goToNext = true;
/* 375 */         FileLockData data = (FileLockData)this.m_locksList.elementAt(index);
/* 376 */         if ((badDirs.get(data.m_dir) == null) && 
/* 380 */           (!data.m_isProcessed))
/*     */         {
/* 383 */           synchronized (data.m_lockObj)
/*     */           {
/* 385 */             if (data.m_waitingAgents.size() == 0)
/*     */             {
/* 387 */               this.m_locksList.removeElementAt(index);
/* 388 */               goToNext = false;
/* 389 */               this.m_locks.remove(data.m_fullPath);
/*     */             }
/* 391 */             data.m_lockObj.notify();
/* 392 */             Report.trace("filelonglock", new StringBuilder().append("Lock ").append(data.m_id).append(" not maintained for ").append(data.m_agent).append(".").toString(), null);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 397 */         if (goToNext)
/*     */         {
/* 399 */           ++index;
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected FileLockData createNewAutoDetectedLock(File file, String dir, String fileName, String path) throws CharConversionException
/*     */   {
/* 407 */     String name = FileUtils.getName(fileName);
/* 408 */     String lockId = StringUtils.decodeHttpHeaderStyle(name);
/* 409 */     boolean isTemp = fileName.indexOf("~tmp") > 0;
/* 410 */     return createNewDetectedLock(file, dir, lockId, null, path, isTemp, true);
/*     */   }
/*     */ 
/*     */   protected FileLockData createNewDetectedLock(File file, String dir, String lockId, String agent, String path, boolean isTemp, boolean isBackground)
/*     */   {
/* 416 */     boolean isCreate = agent != null;
/* 417 */     if (agent == null)
/*     */     {
/* 419 */       if (!isTemp)
/*     */       {
/* 421 */         agent = readFileContents(file);
/*     */       }
/*     */       else
/*     */       {
/* 425 */         agent = "<Temp File>";
/*     */       }
/*     */     }
/* 428 */     FileLockData data = new FileLockData(dir, path, lockId, agent);
/* 429 */     data.m_isTempFile = isTemp;
/* 430 */     data.m_lastModified = file.lastModified();
/* 431 */     data.m_state = 2;
/* 432 */     data.m_isProcessed = true;
/* 433 */     data.m_isLocked = true;
/* 434 */     if (SystemUtils.m_verbose)
/*     */     {
/* 436 */       Report.debug("filelonglock", new StringBuilder().append("").append((isCreate) ? "created" : "detected").append(" new lock ").append(path).append(" by ").append(agent).append(" isAuto=").append(isBackground).toString(), null);
/*     */     }
/* 438 */     return data;
/*     */   }
/*     */ 
/*     */   protected void updateLockFile(FileLockData data)
/*     */   {
/* 443 */     for (int i = 0; i < 3; ++i)
/*     */     {
/*     */       try
/*     */       {
/* 447 */         File file = FileUtilsCfgBuilder.getCfgFile(data.m_fullPath, "Lock", false);
/* 448 */         if (SystemUtils.m_verbose)
/*     */         {
/* 450 */           String curAgent = readFileContents(file);
/* 451 */           if ((curAgent != null) && (data.m_agent != null) && (!curAgent.equals(data.m_agent)))
/*     */           {
/* 453 */             Report.debug("filelonglock", new StringBuilder().append("Lock file is changing agent from ").append(curAgent).append(" to ").append(data.m_agent).append(".").toString(), null);
/*     */           }
/*     */         }
/* 456 */         editLockFile(file, data.m_agent);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 461 */         if (i == 2)
/*     */         {
/* 463 */           Report.trace("filelonglock", null, e);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void editLockFile(File file, String agent) throws IOException
/*     */   {
/* 471 */     Writer w = FileUtilsCfgBuilder.getCfgWriter(file);
/*     */     try
/*     */     {
/* 474 */       if (agent == null)
/*     */       {
/* 476 */         w.write("<Unknown agent>");
/*     */       }
/*     */       else
/*     */       {
/* 480 */         w.write(agent);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 485 */       FileUtils.closeObject(w);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String readFileContents(File file)
/*     */   {
/* 492 */     String result = null;
/* 493 */     Reader r = null;
/*     */     try
/*     */     {
/* 496 */       r = FileUtilsCfgBuilder.getCfgReader(file);
/* 497 */       StringBuffer buf = new StringBuffer();
/* 498 */       char[] t = new char[256];
/* 499 */       int amount = 0;
/* 500 */       while ((amount = r.read(t)) > 0)
/*     */       {
/* 502 */         buf.append(t, 0, amount);
/*     */       }
/* 504 */       String str1 = buf.toString();
/*     */ 
/* 512 */       return str1;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 508 */       Report.trace("filelonglock", new StringBuilder().append("Failed to read lock file ").append(file.toString()).toString(), e);
/*     */     }
/*     */     finally
/*     */     {
/* 512 */       FileUtils.closeObject(r);
/*     */     }
/* 514 */     return result;
/*     */   }
/*     */ 
/*     */   protected boolean editLockList(String path, FileLockData data)
/*     */   {
/* 521 */     boolean foundIt = false;
/* 522 */     for (int i = 0; i < this.m_locksList.size(); ++i)
/*     */     {
/* 524 */       FileLockData d = (FileLockData)this.m_locksList.elementAt(i);
/* 525 */       if (!d.m_fullPath.equals(path))
/*     */         continue;
/* 527 */       foundIt = true;
/* 528 */       if (data == null)
/*     */       {
/* 530 */         this.m_locksList.removeElementAt(i); break;
/*     */       }
/*     */ 
/* 536 */       data.m_lockObj = d.m_lockObj;
/* 537 */       data.m_waitingAgents = d.m_waitingAgents;
/*     */ 
/* 540 */       this.m_locksList.setElementAt(data, i);
/*     */ 
/* 542 */       break;
/*     */     }
/*     */ 
/* 545 */     if ((foundIt) && (data == null))
/*     */     {
/* 547 */       this.m_locks.remove(path);
/*     */     }
/*     */     else
/*     */     {
/* 551 */       if (!foundIt)
/*     */       {
/* 555 */         data.m_lockObj = new boolean[] { false };
/* 556 */         this.m_locksList.addElement(data);
/*     */       }
/* 558 */       this.m_locks.put(path, data);
/*     */     }
/* 560 */     return foundIt;
/*     */   }
/*     */ 
/*     */   public boolean checkLockExists(String dir, String lockName)
/*     */   {
/* 568 */     checkInit();
/* 569 */     dir = quickDirFix(dir);
/* 570 */     String lockNameFileRoot = StringUtils.urlEncodeEx(lockName, false);
/* 571 */     String filePath = new StringBuilder().append(dir).append(lockNameFileRoot).append(".flck").toString();
/* 572 */     File filePathFile = FileUtilsCfgBuilder.getCfgFile(filePath, "Lock", false);
/* 573 */     return filePathFile.exists();
/*     */   }
/*     */ 
/*     */   public boolean createLock(String dir, String lockName, String agent, long timeout, boolean waitForever)
/*     */     throws IOException, ServiceException
/*     */   {
/* 582 */     checkInit();
/* 583 */     dir = quickDirFix(dir);
/*     */ 
/* 585 */     long expireTime = 0L;
/* 586 */     long curTime = System.currentTimeMillis();
/* 587 */     if (!waitForever)
/*     */     {
/* 589 */       expireTime = curTime + timeout;
/*     */     }
/* 591 */     String lockNameFileRoot = StringUtils.urlEncodeEx(lockName, false);
/* 592 */     String randomFilePath = null;
/* 593 */     File randomFile = null;
/*     */ 
/* 595 */     if (this.m_useRename)
/*     */     {
/* 597 */       long random = curTime + 60000 * this.m_randomCount++;
/* 598 */       if (this.m_randomCount >= 60000)
/*     */       {
/* 600 */         this.m_randomCount -= 60000;
/*     */       }
/*     */ 
/* 603 */       String randomName = new StringBuilder().append(lockNameFileRoot).append("~tmp").append(random).append(".flck").toString();
/* 604 */       randomFilePath = new StringBuilder().append(dir).append(randomName.toLowerCase()).toString();
/* 605 */       randomFile = FileUtilsCfgBuilder.getCfgFile(randomFilePath, "Lock", false);
/*     */     }
/* 607 */     String fileName = new StringBuilder().append(lockNameFileRoot).append(".flck").toString();
/* 608 */     String filePath = new StringBuilder().append(dir).append(fileName.toLowerCase()).toString();
/*     */ 
/* 610 */     File file = FileUtilsCfgBuilder.getCfgFile(filePath, "Lock", false);
/* 611 */     int nexceptions = 0;
/* 612 */     int ntimesShouldHaveLock = 0;
/*     */ 
/* 614 */     boolean shouldAllowLock = false;
/* 615 */     boolean lockedIt = false;
/* 616 */     boolean isFirstAttempt = true;
/* 617 */     while ((!lockedIt) && (((waitForever) || (isFirstAttempt) || (curTime < expireTime))))
/*     */     {
/*     */       try
/*     */       {
/* 622 */         if ((this.m_useRename) || (FileUtils.storeInDB(dir)))
/*     */         {
/* 624 */           editLockFile(randomFile, agent);
/* 625 */           if ((randomFile.renameTo(file)) && 
/* 627 */             (!randomFile.exists()))
/*     */           {
/* 629 */             lockedIt = true;
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 635 */           lockedIt = atomicCreateFile(file);
/* 636 */           if (lockedIt)
/*     */           {
/* 638 */             if (SystemUtils.m_verbose)
/*     */             {
/* 640 */               Report.debug("filelonglock", new StringBuilder().append("Successfully created lock file for ").append(lockName).append(" by ").append(agent).toString(), null);
/*     */             }
/* 642 */             editLockFile(file, agent);
/*     */           }
/*     */         }
/*     */ 
/* 646 */         if (lockedIt)
/*     */         {
/* 648 */           FileLockData data = createNewDetectedLock(file, dir, lockName, agent, filePath, false, false);
/* 649 */           FileLockData cmpData = (FileLockData)this.m_locks.get(filePath);
/* 650 */           if ((cmpData != null) && (cmpData.m_state == 1))
/*     */           {
/* 652 */             Report.trace("filelonglock", new StringBuilder().append("Lock ").append(data.m_id).append(" has already had a lock placed on it for this process by ").append(data.m_agent).append(".").toString(), null);
/*     */           }
/*     */ 
/* 656 */           synchronized (this.m_lockObject)
/*     */           {
/* 658 */             data.m_state = 1;
/* 659 */             editLockList(filePath, data);
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 665 */         Report.trace("filelonglock", null, e);
/* 666 */         if ((nexceptions++ > 2) || (SystemUtils.m_isServerStopped))
/*     */         {
/* 668 */           throw e;
/*     */         }
/*     */       }
/* 671 */       if (lockedIt)
/*     */         continue;
/* 673 */       if (randomFile != null)
/*     */       {
/* 675 */         randomFile.delete();
/*     */       }
/*     */ 
/* 678 */       long attemptTimeout = 60000L;
/* 679 */       if (!waitForever)
/*     */       {
/* 681 */         attemptTimeout = expireTime - curTime;
/*     */       }
/* 683 */       if (shouldAllowLock)
/*     */       {
/* 688 */         if (ntimesShouldHaveLock++ > 5)
/*     */         {
/* 690 */           String msg = LocaleUtils.encodeMessage("syLongTermLockAttemptsFailed", lockName, agent);
/* 691 */           throw new IOException(msg);
/*     */         }
/* 693 */         Report.trace("filelonglock", new StringBuilder().append("Lock attempt blocked for ").append(lockName).append(" by ").append(agent).append(" count ").append(ntimesShouldHaveLock).append(".").toString(), null);
/*     */ 
/* 695 */         int timeToWaitForNextTry = ntimesShouldHaveLock * 200;
/* 696 */         if ((waitForever) || (attemptTimeout > timeToWaitForNextTry))
/*     */         {
/* 698 */           SystemUtils.sleep(timeToWaitForNextTry);
/* 699 */           curTime = System.currentTimeMillis();
/* 700 */           if (!waitForever)
/*     */           {
/* 702 */             attemptTimeout = expireTime - curTime;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 707 */       if (attemptTimeout <= 0L)
/*     */       {
/* 709 */         shouldAllowLock = false;
/*     */       }
/*     */       else
/*     */       {
/* 713 */         shouldAllowLock = waitForLockRelease(file, filePath, dir, lockName, agent, attemptTimeout);
/*     */       }
/* 715 */       curTime = System.currentTimeMillis();
/* 716 */       isFirstAttempt = false;
/*     */     }
/*     */ 
/* 719 */     return lockedIt;
/*     */   }
/*     */ 
/*     */   protected boolean waitForLockRelease(File file, String filePath, String dir, String lockName, String agent, long timeout)
/*     */     throws ServiceException
/*     */   {
/* 726 */     FileLockData data = null;
/* 727 */     synchronized (this.m_lockObject)
/*     */     {
/* 729 */       data = (FileLockData)this.m_locks.get(filePath);
/* 730 */       if (data == null)
/*     */       {
/* 732 */         data = createNewDetectedLock(file, dir, lockName, null, filePath, false, false);
/* 733 */         data.m_lockObj = new boolean[] { false };
/* 734 */         this.m_locksList.addElement(data);
/* 735 */         this.m_locks.put(filePath, data);
/*     */       }
/* 737 */       synchronized (data.m_lockObj)
/*     */       {
/* 739 */         editAgentList(data.m_waitingAgents, agent, false);
/*     */       }
/*     */     }
/* 742 */     synchronized (data.m_lockObj)
/*     */     {
/*     */       try
/*     */       {
/* 746 */         data.m_lockObj.wait(timeout);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 750 */         if (SystemUtils.m_isServerStopped)
/*     */         {
/* 752 */           throw new ServiceException(e);
/*     */         }
/* 754 */         e.printStackTrace();
/*     */       }
/*     */     }
/* 757 */     synchronized (this.m_lockObject)
/*     */     {
/* 759 */       editAgentList(data.m_waitingAgents, agent, true);
/* 760 */       synchronized (data.m_lockObj)
/*     */       {
/* 762 */         if ((data.m_waitingAgents.size() == 0) && (!data.m_isLocked))
/*     */         {
/* 764 */           editLockList(data.m_fullPath, null);
/*     */         }
/*     */       }
/*     */     }
/* 768 */     return !data.m_isLocked;
/*     */   }
/*     */ 
/*     */   protected void editAgentList(Vector v, String agent, boolean isDelete)
/*     */   {
/* 773 */     if (agent == null)
/*     */     {
/* 775 */       agent = "<Unknown Agent>";
/*     */     }
/* 777 */     boolean foundIt = false;
/* 778 */     for (int i = 0; i < v.size(); ++i)
/*     */     {
/* 780 */       Object[] o = (Object[])(Object[])v.elementAt(i);
/* 781 */       String a = (String)o[0];
/* 782 */       if (!a.equalsIgnoreCase(agent))
/*     */         continue;
/* 784 */       Integer countInt = (Integer)o[1];
/* 785 */       int count = countInt.intValue();
/* 786 */       if ((isDelete) && (count <= 1))
/*     */       {
/* 788 */         v.removeElementAt(i);
/*     */       }
/*     */       else
/*     */       {
/* 792 */         count = (isDelete) ? count - 1 : count + 1;
/* 793 */         o[1] = new Integer(count);
/*     */       }
/* 795 */       foundIt = true;
/* 796 */       break;
/*     */     }
/*     */ 
/* 799 */     if ((foundIt) || (isDelete))
/*     */       return;
/* 801 */     Object[] o = { agent, new Integer(1) };
/* 802 */     v.addElement(o);
/*     */   }
/*     */ 
/*     */   public void releaseLock(String dir, String lockId, String agent)
/*     */   {
/* 808 */     dir = quickDirFix(dir);
/* 809 */     String lockNameFileRoot = StringUtils.urlEncodeEx(lockId, false);
/* 810 */     String fileName = new StringBuilder().append(lockNameFileRoot).append(".flck").toString();
/* 811 */     String filePath = new StringBuilder().append(dir).append(fileName.toLowerCase()).toString();
/* 812 */     File file = FileUtilsCfgBuilder.getCfgFile(filePath, "Lock", false);
/*     */ 
/* 814 */     FileLockData data = null;
/* 815 */     synchronized (this.m_lockObject)
/*     */     {
/* 817 */       data = (FileLockData)this.m_locks.get(filePath);
/*     */ 
/* 819 */       if (data != null)
/*     */       {
/* 821 */         synchronized (data.m_lockObj)
/*     */         {
/* 823 */           if (data.m_waitingAgents.size() == 0)
/*     */           {
/* 826 */             editLockList(filePath, null);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 832 */     if (data != null)
/*     */     {
/* 836 */       synchronized (data.m_lockObj)
/*     */       {
/* 838 */         if (data.m_state != 1)
/*     */         {
/* 840 */           Report.trace("filelonglock", new StringBuilder().append("Lock ").append(data.m_id).append(" has not had a lock placed on it for this process by ").append(data.m_agent).toString(), null);
/*     */         }
/*     */ 
/* 843 */         if ((agent != null) && (data.m_agent != null) && (!agent.equals(data.m_agent)))
/*     */         {
/* 845 */           Report.trace("filelonglock", new StringBuilder().append("Lock ").append(data.m_id).append(" had lock placed by agent ").append(data.m_agent).append(" but released by ").append(data.m_agent).toString(), null);
/*     */         }
/*     */ 
/* 850 */         data.m_state = 0;
/* 851 */         data.m_isLocked = false;
/*     */ 
/* 854 */         file.delete();
/* 855 */         if (SystemUtils.m_verbose)
/*     */         {
/* 857 */           Report.debug("filelonglock", new StringBuilder().append("Lock ").append(data.m_id).append(" has been deleted by direct release by ").append(data.m_agent).toString(), null);
/*     */         }
/* 859 */         data.m_lockObj.notify();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 864 */       Report.trace("filelonglock", new StringBuilder().append("***Lock ").append(lockId).append(" has no lock object and is being released by ").append(agent).append(".").toString(), null);
/*     */ 
/* 866 */       file.delete();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 872 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97029 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FileUtilsLockThread
 * JD-Core Version:    0.5.4
 */