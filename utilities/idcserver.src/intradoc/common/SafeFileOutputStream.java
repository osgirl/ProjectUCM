/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintStream;
/*     */ import java.nio.channels.FileChannel;
/*     */ 
/*     */ public class SafeFileOutputStream extends FileOutputStream
/*     */ {
/*     */   public static final int F_PURGE_BACKUP = 1;
/*     */   public static final int F_USE_JAVA_SEMANTICS = 2;
/*     */   public static final int F_DISABLE_RENAME_SEMANTICS = 4;
/*     */   public static final int F_USE_COUNTER_TEMP = 8;
/*     */   public static final int F_VERBOSE = 16;
/*     */   public String m_file;
/*     */   public String m_backupFile;
/*     */   public String m_tmpFile;
/*     */   public int m_flags;
/*     */   public PrintStream m_report;
/*     */   protected NativeOsUtils m_utils;
/*     */   protected IOException m_lastException;
/*     */   protected boolean m_isClosed;
/*     */   protected boolean m_isAbort;
/*     */   public static boolean m_nativeOsUtilsReported;
/*  52 */   public static int m_counter = 0;
/*     */   public static boolean m_disableNativeApi;
/*     */ 
/*     */   public SafeFileOutputStream(String file, int flags)
/*     */     throws IOException
/*     */   {
/*  57 */     this(file, flags, new String[1], System.err);
/*     */   }
/*     */ 
/*     */   public SafeFileOutputStream(String file, int flags, PrintStream reporter) throws IOException
/*     */   {
/*  62 */     this(file, flags, new String[1], reporter);
/*     */   }
/*     */ 
/*     */   public SafeFileOutputStream(String file, int flags, String[] tmpFile, PrintStream reporter)
/*     */     throws IOException
/*     */   {
/*  69 */     super(tmpFile[0] =  = computeTempFile(file, flags, reporter));
/*  70 */     this.m_tmpFile = tmpFile[0];
/*     */ 
/*  72 */     this.m_file = file;
/*  73 */     this.m_backupFile = (file + ".bak");
/*  74 */     this.m_flags = flags;
/*  75 */     this.m_report = reporter;
/*     */     try
/*     */     {
/*  79 */       if (((flags & 0x2) == 0) && (EnvUtils.m_useNativeOSUtils))
/*     */       {
/*  81 */         this.m_utils = new NativeOsUtils();
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  86 */       if ((m_nativeOsUtilsReported) || (!EnvUtils.isFamily("unix")))
/*     */         return;
/*  88 */       m_nativeOsUtilsReported = true;
/*  89 */       IdcMessage msg = IdcMessageFactory.lc("syNativeOsUtilsNotLoaded", new Object[0]);
/*  90 */       msg.m_prior = IdcMessageFactory.lc("syNativeFileOperationsDisabled", new Object[0]);
/*  91 */       Report.warning("system", t, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public FileChannel getChannel()
/*     */   {
/* 100 */     throw new AssertionError("SafeFileOutputStream can't have channel references.");
/*     */   }
/*     */ 
/*     */   protected static String computeTempFile(String file, int flags, PrintStream reporter)
/*     */   {
/* 105 */     String tmpFile = file + ".tmp";
/*     */ 
/* 109 */     return tmpFile;
/*     */   }
/*     */ 
/*     */   protected static synchronized int getCounter(PrintStream reporter)
/*     */   {
/* 114 */     int counter = m_counter % 1000;
/* 115 */     m_counter += 1;
/* 116 */     reporter.println("using counter value " + counter);
/* 117 */     return counter;
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */     throws IOException
/*     */   {
/* 123 */     if (this.m_isAbort)
/*     */     {
/* 125 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 129 */       super.write(b);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 133 */       this.m_lastException = e;
/* 134 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(byte[] buf)
/*     */     throws IOException
/*     */   {
/* 141 */     if (this.m_isAbort)
/*     */     {
/* 143 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 147 */       super.write(buf);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 151 */       this.m_lastException = e;
/* 152 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(byte[] buf, int start, int len)
/*     */     throws IOException
/*     */   {
/* 159 */     if (this.m_isAbort)
/*     */     {
/* 161 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 165 */       super.write(buf, start, len);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 169 */       this.m_lastException = e;
/* 170 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void abort()
/*     */   {
/* 176 */     this.m_isAbort = true;
/*     */   }
/*     */ 
/*     */   public void abortAndClose()
/*     */   {
/* 181 */     this.m_isAbort = true;
/*     */     try
/*     */     {
/* 184 */       super.close();
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 188 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 190 */       Report.debug(null, "IOException on close when aborting write to " + this.m_file, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void report(String text)
/*     */   {
/* 197 */     if ((this.m_flags & 0x10) == 0)
/*     */       return;
/* 199 */     this.m_report.println(Thread.currentThread().getName() + ": " + text);
/* 200 */     this.m_report.flush();
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 207 */     super.flush();
/* 208 */     super.close();
/* 209 */     if (this.m_isClosed)
/*     */     {
/* 211 */       return;
/*     */     }
/* 213 */     report("closing " + this.m_tmpFile);
/* 214 */     this.m_isClosed = true;
/* 215 */     if (this.m_isAbort)
/*     */     {
/* 219 */       return;
/*     */     }
/*     */ 
/* 222 */     if (this.m_lastException != null)
/*     */     {
/* 224 */       String msg = LocaleUtils.encodeMessage("sySafeWriteCloseFailed2", null, this.m_tmpFile, this.m_file);
/*     */ 
/* 226 */       IOException ioe = new IOException(msg);
/* 227 */       SystemUtils.setExceptionCause(ioe, this.m_lastException);
/* 228 */       throw ioe;
/*     */     }
/*     */ 
/* 231 */     boolean useJavaSemantics = m_disableNativeApi;
/* 232 */     if (((this.m_flags & 0x2) == 0) && (this.m_utils != null) && (this.m_utils.isLinkSupported()))
/*     */     {
/* 235 */       report("using native file update semantics");
/*     */ 
/* 238 */       if (FileUtils.checkFile(this.m_file, 1) == 0)
/*     */       {
/* 240 */         report("deleting " + this.m_backupFile);
/* 241 */         FileUtils.deleteFile(this.m_backupFile);
/* 242 */         report("linking " + this.m_file + " to " + this.m_backupFile);
/* 243 */         int rc = this.m_utils.link(this.m_file, this.m_backupFile);
/* 244 */         if (rc != 0)
/*     */         {
/* 246 */           IdcMessage msg = IdcMessageFactory.lc();
/* 247 */           msg.m_msgSimple = this.m_utils.getErrorMessage(rc);
/* 248 */           IdcMessage msg2 = IdcMessageFactory.lc("syHardLinkFailed", new Object[] { this.m_file, this.m_backupFile });
/*     */ 
/* 250 */           msg2.m_prior = msg;
/* 251 */           ServiceException se = new ServiceException(null, msg2);
/* 252 */           se.m_isWrapped = true;
/* 253 */           IOException ioe = new IOException();
/* 254 */           ioe.initCause(se);
/* 255 */           if (EnvUtils.m_allowFailedHardLinkTransactions)
/*     */           {
/* 258 */             useJavaSemantics = true;
/* 259 */             Report.trace("system", "Failed to hard link to back up file, will use non transactional approach", se);
/*     */           }
/*     */           else
/*     */           {
/* 263 */             throw ioe;
/*     */           }
/*     */         }
/*     */       }
/* 267 */       if (!useJavaSemantics)
/*     */       {
/* 269 */         if (((this.m_flags & 0x4) == 0) && (this.m_utils.isRenameSupported()))
/*     */         {
/* 272 */           report("renaming " + this.m_tmpFile + " to " + this.m_file);
/* 273 */           int rc = this.m_utils.rename(this.m_tmpFile, this.m_file);
/* 274 */           if (rc != 0)
/*     */           {
/* 276 */             IdcMessage msg = IdcMessageFactory.lc("sySafeWriteCloseFailed", new Object[] { this.m_tmpFile, this.m_file });
/* 277 */             msg.m_msgSimple = ("Unable to rename " + this.m_tmpFile + " to " + this.m_file + ".  ");
/* 278 */             IdcMessage basemsg = IdcMessageFactory.lc();
/* 279 */             basemsg.m_msgSimple = this.m_utils.getErrorMessage(rc);
/* 280 */             msg.m_prior = basemsg;
/* 281 */             ServiceException se = new ServiceException(null, msg);
/* 282 */             se.m_isWrapped = true;
/* 283 */             IOException ioe = new IOException();
/* 284 */             ioe.initCause(se);
/* 285 */             throw ioe;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 290 */           report("deleting " + this.m_file);
/* 291 */           FileUtils.deleteFile(this.m_file);
/* 292 */           report("linking " + this.m_tmpFile + " to " + this.m_file);
/* 293 */           int rc = this.m_utils.link(this.m_tmpFile, this.m_file);
/* 294 */           if (rc == 0)
/*     */           {
/* 296 */             report("deleting " + this.m_tmpFile);
/* 297 */             FileUtils.deleteFile(this.m_tmpFile);
/*     */           }
/*     */           else
/*     */           {
/* 301 */             IdcMessage msg = IdcMessageFactory.lc("syHardLinkFailed", new Object[] { this.m_tmpFile, this.m_file });
/* 302 */             IdcMessage basemsg = IdcMessageFactory.lc();
/* 303 */             basemsg.m_msgSimple = this.m_utils.getErrorMessage(rc);
/* 304 */             msg.m_prior = basemsg;
/* 305 */             ServiceException se = new ServiceException(null, msg);
/* 306 */             se.m_isWrapped = true;
/* 307 */             IOException ioe = new IOException();
/* 308 */             ioe.initCause(se);
/* 309 */             throw ioe;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 316 */       useJavaSemantics = true;
/*     */     }
/* 318 */     if (useJavaSemantics)
/*     */     {
/* 320 */       report("using Java file update semantics");
/* 321 */       ServiceException se1 = null;
/*     */       try
/*     */       {
/* 324 */         FileUtils.renameFile(this.m_file, this.m_backupFile);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 328 */         se1 = e;
/*     */       }
/*     */       try
/*     */       {
/* 332 */         report("renaming " + this.m_tmpFile + " to " + this.m_file);
/* 333 */         FileUtils.renameFile(this.m_tmpFile, this.m_file);
/* 334 */         report("renamed " + this.m_tmpFile + " to " + this.m_file);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 338 */         ServiceException se = new ServiceException(e, "sySafeWriteCloseFailed", new Object[] { this.m_tmpFile, this.m_file });
/*     */ 
/* 340 */         if (se1 != null)
/*     */         {
/* 342 */           se.addCause(se1);
/*     */         }
/* 344 */         IOException ioe = new IOException();
/* 345 */         se.m_isWrapped = true;
/* 346 */         ioe.initCause(se);
/* 347 */         throw ioe;
/*     */       }
/*     */     }
/*     */ 
/* 351 */     if ((this.m_flags & 0x1) == 0)
/*     */       return;
/* 353 */     report("deleting " + this.m_backupFile);
/* 354 */     FileUtils.deleteFile(this.m_backupFile);
/*     */   }
/*     */ 
/*     */   public void finalize()
/*     */     throws IOException
/*     */   {
/* 361 */     if (!this.m_isClosed)
/*     */     {
/* 363 */       abortAndClose();
/*     */     }
/* 365 */     super.finalize();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 370 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94851 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.SafeFileOutputStream
 * JD-Core Version:    0.5.4
 */