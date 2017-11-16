/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.SimpleTimeZone;
/*     */ import java.util.TimeZone;
/*     */ 
/*     */ public class RollingFileOutputStream extends OutputStream
/*     */   implements IdcComparator
/*     */ {
/*     */   public static final int F_TRACE = 1;
/*     */   public static final int F_EXCEPTIONS_TO_STDERR = 2;
/*     */   public static final int F_FORCE_TIMESTAMP = 4;
/*     */   public static final int F_USE_8601_TIMESTAMP = 8;
/*     */   public static final int F_USE_ZULU_TIMESTAMP = 16;
/*     */   public static final int F_SLEEP_TO_ENFORCE_SIZE = 32;
/*     */   public String m_dir;
/*  51 */   public String m_currentTSPart = "current";
/*     */ 
/*  54 */   public String m_namePrefix = "";
/*     */ 
/*  57 */   public String m_nameSuffix = ".log";
/*     */ 
/*  60 */   public String m_header = "";
/*     */ 
/*  64 */   public long m_maxSize = 1048576L;
/*     */ 
/*  67 */   public int m_maxFiles = 10;
/*     */ 
/*  70 */   public int m_maxAge = -1;
/*     */ 
/*  75 */   public int m_maxIdle = -1;
/*     */   public int m_flags;
/*  87 */   public IdcDateFormat m_format = null;
/*     */ 
/*  90 */   public OutputStream m_currentOutput = null;
/*     */ 
/*  93 */   public String m_currentOutputName = null;
/*     */ 
/*  97 */   public String m_priorOutputName = null;
/*     */ 
/* 102 */   public String m_lastRenameOutputName = null;
/*     */ 
/* 105 */   public long m_fileLength = 0L;
/*     */ 
/* 108 */   public long m_lastWrite = 0L;
/*     */ 
/* 111 */   public long m_openTime = 0L;
/*     */ 
/* 114 */   public File m_lastDeleteFailure = null;
/*     */ 
/* 117 */   public File m_lastDeletedFile = null;
/*     */ 
/* 120 */   public OutputStream m_nullOutputStream = new NullOutputStream();
/*     */ 
/* 123 */   public PrintStream m_errorStream = System.err;
/*     */ 
/* 126 */   public boolean m_unableToCreateErrorDisplayed = false;
/*     */ 
/* 129 */   public long m_counter = 1L;
/*     */ 
/*     */   public RollingFileOutputStream(String dir, String namePrefix, String nameSuffix, int flags)
/*     */   {
/* 142 */     initEx(dir, namePrefix, nameSuffix, flags);
/*     */   }
/*     */ 
/*     */   public void init(String dir, String namePrefix, String nameSuffix, int flags)
/*     */   {
/* 147 */     initEx(dir, namePrefix, nameSuffix, flags);
/*     */   }
/*     */ 
/*     */   public synchronized void initEx(String dir, String namePrefix, String nameSuffix, int flags)
/*     */   {
/*     */     try
/*     */     {
/* 154 */       close();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 158 */       e.printStackTrace(this.m_errorStream);
/*     */     }
/* 160 */     this.m_dir = FileUtils.fileSlashes(dir);
/* 161 */     this.m_namePrefix = namePrefix;
/* 162 */     this.m_nameSuffix = nameSuffix;
/* 163 */     this.m_flags = flags;
/*     */ 
/* 165 */     initFormat();
/*     */   }
/*     */ 
/*     */   public void initFormat()
/*     */   {
/* 174 */     if ((this.m_flags & 0x8) != 0)
/*     */     {
/* 176 */       if (this.m_format != null)
/*     */         return;
/* 178 */       TimeZone tz = null;
/* 179 */       if ((this.m_flags & 0x10) != 0)
/*     */       {
/* 181 */         tz = new SimpleTimeZone(0, "UTC");
/*     */       }
/* 183 */       this.m_format = new IdcDateFormat();
/*     */       try
/*     */       {
/* 186 */         this.m_format.init("iso8601short", tz, null, null);
/*     */       }
/*     */       catch (ParseStringException e)
/*     */       {
/* 195 */         this.m_format = null;
/* 196 */         this.m_flags &= -9;
/* 197 */         throw new AssertionError(e);
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 204 */         this.m_format = null;
/* 205 */         this.m_flags &= -9;
/* 206 */         t.printStackTrace(this.m_errorStream);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 212 */       this.m_format = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void close()
/*     */     throws IOException
/*     */   {
/* 222 */     if (this.m_currentOutput == null)
/*     */       return;
/* 224 */     SystemUtils.removeStreamObjectToCloseOnStop(this.m_currentOutput);
/* 225 */     FileUtils.closeObject(this.m_currentOutput);
/* 226 */     this.m_currentOutput = null;
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/* 233 */     OutputStream out = this.m_currentOutput;
/* 234 */     if (out == null)
/*     */       return;
/* 236 */     out.flush();
/*     */   }
/*     */ 
/*     */   public synchronized void write(byte[] buf)
/*     */     throws IOException
/*     */   {
/* 243 */     OutputStream out = checkOutput(buf.length);
/* 244 */     out.write(buf);
/* 245 */     this.m_lastWrite = System.currentTimeMillis();
/* 246 */     this.m_fileLength += buf.length;
/*     */   }
/*     */ 
/*     */   public synchronized void write(byte[] buf, int start, int length)
/*     */     throws IOException
/*     */   {
/* 252 */     OutputStream out = checkOutput(length);
/* 253 */     out.write(buf, start, length);
/* 254 */     this.m_lastWrite = System.currentTimeMillis();
/* 255 */     this.m_fileLength += length;
/*     */   }
/*     */ 
/*     */   public synchronized void write(int c)
/*     */     throws IOException
/*     */   {
/* 261 */     OutputStream out = checkOutput(1);
/* 262 */     out.write(c);
/* 263 */     this.m_lastWrite = System.currentTimeMillis();
/* 264 */     this.m_fileLength += 1L;
/*     */   }
/*     */ 
/*     */   public synchronized OutputStream checkOutput(int length)
/*     */     throws IOException
/*     */   {
/* 279 */     boolean useNewFile = false;
/*     */ 
/* 283 */     long now = System.currentTimeMillis();
/* 284 */     if ((this.m_currentOutput != null) && (((this.m_fileLength + length > this.m_maxSize) || ((this.m_maxAge > 0) && (this.m_openTime + this.m_maxAge < now)) || ((this.m_maxIdle > 0) && (this.m_lastWrite > 0L) && (this.m_lastWrite + this.m_maxIdle < now)))) && (checkCurrentRename()))
/*     */     {
/* 291 */       OutputStream out = this.m_currentOutput;
/* 292 */       this.m_currentOutput = null;
/*     */       try
/*     */       {
/* 295 */         SystemUtils.removeStreamObjectToCloseOnStop(out);
/* 296 */         out.close();
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 300 */         if ((this.m_flags & 0x2) == 0)
/*     */         {
/* 302 */           throw e;
/*     */         }
/* 304 */         e.printStackTrace(this.m_errorStream);
/*     */       }
/* 306 */       handleCurrentRename();
/* 307 */       useNewFile = true;
/*     */     }
/*     */ 
/* 310 */     if (this.m_currentOutput != null)
/*     */     {
/* 312 */       return this.m_currentOutput;
/*     */     }
/*     */ 
/* 315 */     File dir = new File(this.m_dir);
/* 316 */     if (!dir.exists())
/*     */     {
/*     */       try
/*     */       {
/* 320 */         FileUtils.checkOrCreateDirectory(this.m_dir, 999);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 324 */         IOException ioe = new IOException();
/* 325 */         SystemUtils.setExceptionCause(ioe, e);
/* 326 */         if ((this.m_flags & 0x2) == 0)
/*     */         {
/* 328 */           throw ioe;
/*     */         }
/* 330 */         ioe.printStackTrace(this.m_errorStream);
/* 331 */         if (!this.m_unableToCreateErrorDisplayed)
/*     */         {
/* 333 */           this.m_errorStream.println("Unable to create directory " + this.m_dir);
/* 334 */           this.m_unableToCreateErrorDisplayed = true;
/*     */         }
/* 336 */         return this.m_nullOutputStream;
/*     */       }
/*     */     }
/*     */ 
/* 340 */     String[] dirList = dir.list();
/* 341 */     ArrayList logFileInfo = new ArrayList();
/* 342 */     ArrayList logFileNameList = new ArrayList();
/*     */ 
/* 344 */     int i = 0;
/* 345 */     for (String name : dirList)
/*     */     {
/* 347 */       if ((!name.startsWith(this.m_namePrefix)) || (!name.endsWith(this.m_nameSuffix))) {
/*     */         continue;
/*     */       }
/* 350 */       String tsPart = getTsPart(name);
/* 351 */       long timestamp = parseTimestamp(tsPart);
/* 352 */       if (timestamp == -1L)
/*     */       {
/* 354 */         if ((this.m_flags & 0x1) == 0)
/*     */           continue;
/* 356 */         this.m_errorStream.println("RollingFileOutputStream: ignoring file " + name);
/*     */       }
/*     */       else
/*     */       {
/* 361 */         long[] info = { timestamp, i };
/* 362 */         logFileInfo.add(info);
/* 363 */         logFileNameList.add(name);
/* 364 */         ++i;
/*     */       }
/*     */     }
/*     */ 
/* 368 */     Sort.sortList(logFileInfo, this);
/*     */ 
/* 370 */     while (logFileInfo.size() > this.m_maxFiles)
/*     */     {
/* 372 */       long[] fileInfo = (long[])logFileInfo.remove(0);
/* 373 */       String file = this.m_dir + "/" + (String)logFileNameList.get((int)fileInfo[1]);
/* 374 */       File f = new File(file);
/* 375 */       if (!f.exists()) {
/*     */         continue;
/*     */       }
/*     */ 
/* 379 */       if ((!f.equals(this.m_lastDeleteFailure)) && ((this.m_flags & 0x1) != 0))
/*     */       {
/* 381 */         this.m_errorStream.println("RollingFileOutputStream:  removing " + file);
/*     */       }
/* 383 */       if (!f.delete())
/*     */       {
/* 385 */         if (f.equals(this.m_lastDeleteFailure))
/*     */         {
/* 388 */           return this.m_nullOutputStream;
/*     */         }
/* 390 */         this.m_lastDeleteFailure = f;
/* 391 */         this.m_errorStream.println("Output to directory " + this.m_dir + " halted because file rotation failed.");
/*     */ 
/* 393 */         if ((this.m_flags & 0x2) == 0)
/*     */         {
/* 395 */           throw new IOException("$!AJK Unable to delete " + file);
/*     */         }
/* 397 */         return this.m_nullOutputStream;
/*     */       }
/* 399 */       if (f.equals(this.m_lastDeletedFile))
/*     */       {
/* 401 */         this.m_errorStream.println("RollingFileOutputStream: re-deleting file " + this.m_lastDeletedFile);
/*     */       }
/*     */ 
/* 404 */       this.m_lastDeletedFile = f;
/* 405 */       f = new File(file);
/* 406 */       if (f.exists())
/*     */       {
/* 408 */         this.m_errorStream.println("RollingFileOutputStream: the file " + f + " exists after deletion");
/*     */       }
/*     */ 
/* 411 */       if (this.m_lastDeleteFailure != null)
/*     */       {
/* 413 */         this.m_errorStream.println("Output to directory " + this.m_dir + " resumed because file rotation succeeded.");
/*     */       }
/*     */ 
/* 416 */       this.m_lastDeleteFailure = null;
/*     */     }
/*     */ 
/* 419 */     if ((!useNewFile) && (logFileInfo.size() > 0))
/*     */     {
/* 421 */       long[] lastFileInfo = (long[])logFileInfo.get(logFileInfo.size() - 1);
/* 422 */       String lastFileName = (String)logFileNameList.get((int)lastFileInfo[1]);
/* 423 */       File lastFile = new File(this.m_dir + "/" + lastFileName);
/* 424 */       this.m_currentOutputName = lastFileName;
/*     */ 
/* 426 */       if ((this.m_lastRenameOutputName != null) && (this.m_lastRenameOutputName.equalsIgnoreCase(lastFileName)))
/*     */       {
/* 429 */         this.m_currentOutputName = null;
/*     */       }
/* 431 */       else if ((this.m_maxIdle < 0) && ((this.m_fileLength = lastFile.length()) + length <= this.m_maxSize))
/*     */       {
/* 433 */         if ((this.m_flags & 0x1) != 0)
/*     */         {
/* 435 */           this.m_errorStream.println("RollingFileOutputStream: reopening " + this.m_dir + "/" + lastFileName);
/*     */         }
/*     */ 
/* 438 */         this.m_currentOutput = openOutputFile(lastFileName);
/* 439 */         SystemUtils.addStreamObjectToCloseOnStop(this.m_currentOutput);
/*     */       }
/*     */       else
/*     */       {
/* 443 */         this.m_currentOutputName = lastFileName;
/* 444 */         if (checkCurrentRename())
/*     */         {
/* 446 */           handleCurrentRename();
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 451 */     if (this.m_currentOutput == null)
/*     */     {
/*     */       String fileName;
/*     */       String fileName;
/* 455 */       if ((this.m_flags & 0x4) == 0)
/*     */       {
/* 457 */         fileName = this.m_namePrefix + this.m_currentTSPart + this.m_nameSuffix;
/*     */       }
/*     */       else
/*     */       {
/*     */         while (true)
/*     */         {
/* 463 */           long ts = System.currentTimeMillis();
/* 464 */           fileName = this.m_namePrefix + formatTimestamp(ts) + this.m_nameSuffix;
/* 465 */           if ((this.m_flags & 0x20) == 0) break; if (FileUtils.checkFile(this.m_dir + "/" + fileName, true, true) != 0)
/*     */           {
/*     */             break;
/*     */           }
/*     */ 
/* 471 */           this.m_errorStream.println("RollingFileOutputStream: sleeping to prevent duplicate renames in " + this.m_dir);
/*     */ 
/* 473 */           SystemUtils.sleep(1000L - ts % 1000L);
/*     */         }
/*     */       }
/* 476 */       this.m_currentOutputName = fileName;
/* 477 */       this.m_currentOutput = openOutputFile(fileName);
/* 478 */       SystemUtils.addStreamObjectToCloseOnStop(this.m_currentOutput);
/* 479 */       this.m_fileLength = 0L;
/*     */     }
/*     */ 
/* 482 */     if (this.m_currentOutput == null)
/*     */     {
/* 484 */       return this.m_nullOutputStream;
/*     */     }
/* 486 */     return this.m_currentOutput;
/*     */   }
/*     */ 
/*     */   protected boolean checkCurrentRename()
/*     */   {
/* 494 */     if ((this.m_priorOutputName != null) && ((this.m_flags & 0x4) == 0) && ((this.m_flags & 0x20) == 0))
/*     */     {
/* 497 */       if (this.m_currentOutputName != null)
/*     */       {
/* 499 */         File f = new File(this.m_dir + "/" + this.m_currentOutputName);
/* 500 */         long ts = f.lastModified();
/* 501 */         String tsPrior = getTsPart(this.m_priorOutputName);
/* 502 */         if (formatTimestamp(ts).equals(tsPrior))
/*     */         {
/* 504 */           return false;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 509 */         return false;
/*     */       }
/*     */     }
/* 512 */     return true;
/*     */   }
/*     */ 
/*     */   protected String getTsPart(String name)
/*     */   {
/* 520 */     String tsPart = null;
/* 521 */     if ((name != null) && (name.startsWith(this.m_namePrefix)) && (name.endsWith(this.m_nameSuffix)))
/*     */     {
/* 523 */       int prefixLength = this.m_namePrefix.length();
/* 524 */       int suffixLength = this.m_nameSuffix.length();
/* 525 */       tsPart = name.substring(prefixLength, name.length() - suffixLength);
/*     */ 
/* 528 */       int index = tsPart.indexOf(95);
/* 529 */       if (index > 0)
/*     */       {
/* 531 */         tsPart = tsPart.substring(0, index);
/*     */       }
/*     */     }
/* 534 */     return tsPart;
/*     */   }
/*     */ 
/*     */   protected void handleCurrentRename()
/*     */     throws IOException
/*     */   {
/* 545 */     String tmpName = this.m_currentOutputName;
/* 546 */     this.m_currentOutputName = null;
/* 547 */     if ((tmpName != null) && (tmpName.equals(this.m_namePrefix + this.m_currentTSPart + this.m_nameSuffix)))
/*     */     {
/* 552 */       File f = new File(this.m_dir + "/" + tmpName);
/* 553 */       long ts = f.lastModified();
/*     */ 
/* 557 */       String tsPart = getTsPart(this.m_priorOutputName);
/* 558 */       String tsFormat = formatTimestamp(ts);
/* 559 */       if (tsFormat.equals(tsPart))
/*     */       {
/* 561 */         tsFormat = tsFormat + "_" + this.m_counter;
/* 562 */         this.m_counter += 1L;
/*     */       }
/*     */ 
/* 565 */       String newName = this.m_namePrefix + tsFormat + this.m_nameSuffix;
/*     */       try
/*     */       {
/* 584 */         this.m_lastRenameOutputName = newName;
/* 585 */         FileUtils.renameFile(this.m_dir + "/" + tmpName, this.m_dir + "/" + newName);
/*     */ 
/* 588 */         this.m_priorOutputName = newName;
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 592 */         if ((this.m_flags & 0x1) != 0)
/*     */         {
/* 594 */           e.printStackTrace(this.m_errorStream);
/*     */         }
/* 596 */         IOException ie = new IOException();
/* 597 */         SystemUtils.setExceptionCause(ie, e);
/* 598 */         if ((this.m_flags & 0x2) == 0)
/*     */         {
/* 600 */           throw ie;
/*     */         }
/* 602 */         ie.printStackTrace(this.m_errorStream);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 607 */       this.m_priorOutputName = tmpName;
/*     */     }
/*     */   }
/*     */ 
/*     */   public long parseTimestamp(String ts)
/*     */   {
/* 617 */     if (ts.equals(this.m_currentTSPart))
/*     */     {
/* 619 */       return 9223372036854775807L;
/*     */     }
/*     */ 
/* 622 */     if (this.m_format != null)
/*     */     {
/*     */       try
/*     */       {
/* 626 */         Date d = this.m_format.parseDate(ts);
/* 627 */         return d.getTime();
/*     */       }
/*     */       catch (ParseStringException e)
/*     */       {
/* 631 */         if ((this.m_flags & 0x1) != 0)
/*     */         {
/* 633 */           e.printStackTrace(this.m_errorStream);
/*     */         }
/* 635 */         return -1L;
/*     */       }
/*     */     }
/*     */ 
/* 639 */     return NumberUtils.parseLong(ts, -1L);
/*     */   }
/*     */ 
/*     */   public String formatTimestamp(long ts)
/*     */   {
/* 649 */     if (this.m_format != null)
/*     */     {
/* 651 */       return this.m_format.format(new Date(ts));
/*     */     }
/*     */ 
/* 654 */     return "" + ts;
/*     */   }
/*     */ 
/*     */   public OutputStream openOutputFile(String name)
/*     */     throws IOException
/*     */   {
/* 664 */     File f = new File(this.m_dir + "/" + name);
/* 665 */     boolean exists = f.exists();
/* 666 */     FileOutputStream stream = null;
/*     */     try
/*     */     {
/* 669 */       stream = new FileOutputStream(f, true);
/* 670 */       if (!exists)
/*     */       {
/* 672 */         stream.write(this.m_header.getBytes());
/*     */       }
/* 674 */       this.m_openTime = System.currentTimeMillis();
/* 675 */       return stream;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 679 */       if ((this.m_flags & 0x2) == 0)
/*     */       {
/* 681 */         throw e;
/*     */       }
/* 683 */       e.printStackTrace(this.m_errorStream);
/* 684 */     }return null;
/*     */   }
/*     */ 
/*     */   public int compare(Object arg1, Object arg2)
/*     */   {
/* 695 */     long[] l1 = (long[])(long[])arg1;
/* 696 */     long[] l2 = (long[])(long[])arg2;
/*     */ 
/* 698 */     long t1 = l1[0];
/* 699 */     long t2 = l2[0];
/*     */ 
/* 701 */     if (t1 > t2)
/*     */     {
/* 703 */       return 1;
/*     */     }
/* 705 */     if (t1 == t2)
/*     */     {
/* 707 */       return 0;
/*     */     }
/* 709 */     return -1;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 715 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100810 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.RollingFileOutputStream
 * JD-Core Version:    0.5.4
 */