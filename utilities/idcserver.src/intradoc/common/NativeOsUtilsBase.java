/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import java.io.File;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public abstract class NativeOsUtilsBase
/*     */ {
/*  39 */   public static long WAIT_OBJECT_0 = -17L;
/*  40 */   public static long WAIT_TIMEOUT = -17L;
/*  41 */   public static long WAIT_ABANDONED = -17L;
/*  42 */   public static long WAIT_FAILED = -17L;
/*  43 */   public static long INFINITE = -17L;
/*     */   public static final int F_DEFAULT = 0;
/*     */   public static final int F_NO_ECHO = 1;
/*     */   public static final int F_NO_CREATE_CONSOLE = 2;
/*     */   public static final int F_NO_READ = 4;
/*     */   public static final int F_REQUIRE_CONSOLE = 8;
/*     */   public static final int F_FOR_READ = 4096;
/*     */   public static final int F_FOR_WRITE = 8192;
/*     */ 
/*     */   @Deprecated
/*  73 */   public long WAIT_0 = 0L;
/*     */ 
/*     */   @Deprecated
/*  76 */   public long TIMEOUT = 258L;
/*     */ 
/*     */   @Deprecated
/*  79 */   public long ABANDONED_WAIT_0 = 128L;
/*     */   public static long ERROR_SUCCESS;
/*     */   public static long ERROR_FILE_NOT_FOUND;
/*     */   public static long ERROR_PATH_NOT_FOUND;
/*     */   public static long ERROR_ENVVAR_NOT_FOUND;
/*     */   public static int RLIMIT_CORE;
/*     */   public static int RLIMIT_CPU;
/*     */   public static int RLIMIT_DATA;
/*     */   public static int RLIMIT_FSIZE;
/*     */   public static int RLIMIT_MEMLOCK;
/*     */   public static int RLIMIT_NOFILE;
/*     */   public static int RLIMIT_NPROC;
/*     */   public static int RLIMIT_RSS;
/*     */   public static int RLIMIT_STACK;
/*     */   public static int RLIMIT_SBSIZE;
/*     */   public static int O_RDONLY;
/*     */   public static int O_WRONLY;
/*     */   public static int O_RDWR;
/*     */   public static int O_NONBLOCK;
/*     */   public static int O_APPEND;
/*     */   public static int O_CREAT;
/*     */   public static int O_TRUNC;
/*     */   public static int O_EXCL;
/*     */   public static int O_FSYNC;
/*     */   public static int S_IFMT;
/*     */   public static int S_IFIFO;
/*     */   public static int S_IFCHR;
/*     */   public static int S_IFDIR;
/*     */   public static int S_IFBLK;
/*     */   public static int S_IFREG;
/*     */   public static int S_IFLNK;
/*     */   public static int S_IFSOCK;
/*     */   public static int S_IFWHT;
/*     */   public static int S_ISUID;
/*     */   public static int S_ISGID;
/*     */   public static int S_ISVTX;
/*     */   public static int S_ISTXT;
/*     */   public static int S_IRWXU;
/*     */   public static int S_IRUSR;
/*     */   public static int S_IWUSR;
/*     */   public static int S_IXUSR;
/*     */   public static int S_IRWXG;
/*     */   public static int S_IRGRP;
/*     */   public static int S_IWGRP;
/*     */   public static int S_IXGRP;
/*     */   public static int S_IRWXO;
/*     */   public static int S_IROTH;
/*     */   public static int S_IWOTH;
/*     */   public static int S_IXOTH;
/*     */   public static int EPERM;
/*     */   public static int ENOENT;
/*     */   public static int ESRCH;
/*     */   public static int EINTR;
/*     */   public static int EIO;
/*     */   public static int ENXIO;
/*     */   public static int E2BIG;
/*     */   public static int ENOEXEC;
/*     */   public static int EBADF;
/*     */   public static int ECHILD;
/*     */   public static int EDEADLK;
/*     */   public static int ENOMEM;
/*     */   public static int EACCES;
/*     */   public static int EFAULT;
/*     */   public static int EBUSY;
/*     */   public static int EEXIST;
/*     */   public static int EXDEV;
/*     */   public static int ENODEV;
/*     */   public static int ENOTDIR;
/*     */   public static int EISDIR;
/*     */   public static int EINVAL;
/*     */   public static int ENFILE;
/*     */   public static int EMFILE;
/*     */   public static int ENOTTY;
/*     */   public static int EFBIG;
/*     */   public static int ENOSPC;
/*     */   public static int ESPIPE;
/*     */   public static int EROFS;
/*     */   public static int EPIPE;
/*     */   public static int EDOM;
/*     */   public static int ERANGE;
/*     */   public static int EAGAIN;
/*     */   public static int ENAMETOOLONG;
/*     */   public static int ENOLCK;
/*     */   public static int ENOSYS;
/* 176 */   public static int SIGHUP = -1;
/* 177 */   public static int SIGINT = -1;
/* 178 */   public static int SIGQUIT = -1;
/* 179 */   public static int SIGILL = -1;
/* 180 */   public static int SIGABRT = -1;
/* 181 */   public static int SIGFPE = -1;
/* 182 */   public static int SIGBUS = -1;
/* 183 */   public static int SIGSEGV = -1;
/* 184 */   public static int SIGSYS = -1;
/* 185 */   public static int SIGPIPE = -1;
/* 186 */   public static int SIGALRM = -1;
/* 187 */   public static int SIGTERM = -1;
/* 188 */   public static int SIGINFO = -1;
/* 189 */   public static int SIGUSR1 = -1;
/* 190 */   public static int SIGUSR2 = -1;
/*     */ 
/* 192 */   public static int SIGKILL = -1;
/*     */   protected static final String m_nativeVersion = "7.2.1.1";
/*     */   public static boolean m_loadFinished;
/*     */   public static boolean m_checkFinished;
/*     */   public static String m_IdcHomeDir;
/*     */   public GenericTracingCallback m_callback;
/*     */   public Map m_args;
/*     */ 
/*     */   public static native String getNativeVersion();
/*     */ 
/*     */   public static native String getNativeBuildInfo();
/*     */ 
/*     */   public static native int initNativeOsConstants();
/*     */ 
/*     */   public native boolean isWin32();
/*     */ 
/*     */   public native boolean isSemaphoreSupported();
/*     */ 
/*     */   public native long createSemaphore(String paramString);
/*     */ 
/*     */   public native long openSemaphore(String paramString);
/*     */ 
/*     */   public native long waitSemaphore(long paramLong1, long paramLong2);
/*     */ 
/*     */   public native long releaseSemaphore(long paramLong);
/*     */ 
/*     */   public native long closeSemaphore(long paramLong);
/*     */ 
/*     */   public native int getConsoleCP();
/*     */ 
/*     */   public native int getConsoleOutputCP();
/*     */ 
/*     */   public native void setConsoleCP(int paramInt);
/*     */ 
/*     */   public native void setConsoleOutputCP();
/*     */ 
/*     */   public native int writeConsole(String paramString, int paramInt);
/*     */ 
/*     */   public native String readConsole(int paramInt);
/*     */ 
/*     */   public native boolean isPrinterInstallSupported();
/*     */ 
/*     */   public native long addPrinterFilePort(String paramString);
/*     */ 
/*     */   public native boolean isPrinterInstalled(String paramString);
/*     */ 
/*     */   public native boolean isWindowsRegistrySupported();
/*     */ 
/*     */   public native String getRegistryValue(String paramString);
/*     */ 
/*     */   public native long setRegistryValue(String paramString1, String paramString2, String paramString3);
/*     */ 
/*     */   public native long clearRegistryEntry(String paramString);
/*     */ 
/*     */   public native boolean isMutexSupported();
/*     */ 
/*     */   public native long createMutex(String paramString);
/*     */ 
/*     */   public native long waitMutex(long paramLong1, long paramLong2);
/*     */ 
/*     */   public native boolean releaseMutex(long paramLong);
/*     */ 
/*     */   public native boolean closeMutex(long paramLong);
/*     */ 
/*     */   public native int getPid();
/*     */ 
/*     */   public native boolean isPosixUserSupported();
/*     */ 
/*     */   public native int getUid();
/*     */ 
/*     */   public native int getGid();
/*     */ 
/*     */   public native boolean isKillSupported();
/*     */ 
/*     */   public native int kill(int paramInt1, int paramInt2);
/*     */ 
/*     */   public native boolean isLinkSupported();
/*     */ 
/*     */   public native int link(String paramString1, String paramString2);
/*     */ 
/*     */   public native boolean isUnlinkSupported();
/*     */ 
/*     */   public native int unlink(String paramString);
/*     */ 
/*     */   public native int statfs(String paramString, StructStatFS paramStructStatFS);
/*     */ 
/*     */   public native boolean isStatSupported();
/*     */ 
/*     */   public native int stat(String paramString, PosixStructStat paramPosixStructStat);
/*     */ 
/*     */   public native boolean isRenameSupported();
/*     */ 
/*     */   public native int rename(String paramString1, String paramString2);
/*     */ 
/*     */   public native boolean isMoveAfterRebootSupported();
/*     */ 
/*     */   public native int moveAfterReboot(String paramString1, String paramString2);
/*     */ 
/*     */   public native boolean isMoveAcrossVolumesSupported();
/*     */ 
/*     */   public native int moveAcrossVolumes(String paramString1, String paramString2);
/*     */ 
/*     */   public native boolean isFcntlSupported();
/*     */ 
/*     */   public native int open(String paramString, int paramInt1, int paramInt2);
/*     */ 
/*     */   public native int close(int paramInt);
/*     */ 
/*     */   public native int dup(int paramInt);
/*     */ 
/*     */   public native int dup2(int paramInt1, int paramInt2);
/*     */ 
/*     */   public native boolean isPosixFilesystemSupported();
/*     */ 
/*     */   public native int chmod(String paramString, int paramInt);
/*     */ 
/*     */   public native int updateAccessTime(String paramString, long paramLong);
/*     */ 
/*     */   public native int updateModificationTime(String paramString, long paramLong);
/*     */ 
/*     */   public native int umask(int paramInt);
/*     */ 
/*     */   public native int lstat(String paramString, PosixStructStat paramPosixStructStat);
/*     */ 
/*     */   public native int fstat(int paramInt, PosixStructStat paramPosixStructStat);
/*     */ 
/*     */   public native int symlink(String paramString1, String paramString2);
/*     */ 
/*     */   public native String readlink(String paramString);
/*     */ 
/*     */   public native String getFileOwner(String paramString);
/*     */ 
/*     */   public native boolean isScreenSizeSupported();
/*     */ 
/*     */   public native int getScreenSize(int[] paramArrayOfInt);
/*     */ 
/*     */   public native String getEnv(String paramString);
/*     */ 
/*     */   public native int setEnv(String paramString1, String paramString2);
/*     */ 
/*     */   public native boolean isRlimitSupported();
/*     */ 
/*     */   public native int getRlimit(int paramInt, long[] paramArrayOfLong);
/*     */ 
/*     */   public native int setRlimit(int paramInt, long[] paramArrayOfLong);
/*     */ 
/*     */   public native long getErrorCode();
/*     */ 
/*     */   public native String getErrorMessage(long paramLong);
/*     */ 
/*     */   public native String getOSName();
/*     */ 
/*     */   public native String getOSFamily();
/*     */ 
/*     */   public native long createMailSlot(String paramString);
/*     */ 
/*     */   public native boolean mailSlotExists(String paramString);
/*     */ 
/*     */   public native boolean writeToMailSlot(String paramString1, String paramString2);
/*     */ 
/*     */   public native boolean clearMailSlot(long paramLong);
/*     */ 
/*     */   public native boolean isMailWaiting(long paramLong);
/*     */ 
/*     */   public native boolean closeMailSlot(long paramLong);
/*     */ 
/*     */   public native String getPrivateProfileString(String paramString1, String paramString2, String paramString3);
/*     */ 
/*     */   public native boolean writePrivateProfileString(String paramString1, String paramString2, String paramString3, String paramString4);
/*     */ 
/*     */   public native String getComputerName();
/*     */ 
/*     */   public native void enableDiagnosticMethods();
/*     */ 
/*     */   public native long allocateMemory(long paramLong);
/*     */ 
/*     */   public native void freeMemory(long paramLong);
/*     */ 
/*     */   public native void crash(int paramInt);
/*     */ 
/*     */   public native long fillMemory();
/*     */ 
/* 353 */   public NativeOsUtilsBase(Map args) { this.m_args = args;
/* 354 */     if (this.m_args == null)
/*     */     {
/* 356 */       this.m_args = new HashMap();
/*     */     }
/* 358 */     if (!m_loadFinished)
/*     */     {
/* 360 */       doLoad();
/* 361 */       m_loadFinished = true;
/*     */     }
/*     */ 
/* 364 */     checkVersion(); }
/*     */ 
/*     */ 
/*     */   public void doLoad()
/*     */   {
/* 369 */     String libPath = (String)this.m_args.get("libpath");
/* 370 */     String libName = (String)this.m_args.get("libname");
/* 371 */     if (libName == null)
/*     */     {
/* 373 */       libName = "JniNativeOsUtils";
/*     */     }
/*     */     try
/*     */     {
/* 377 */       if (libPath != null)
/*     */       {
/* 379 */         System.load(libPath);
/*     */       }
/*     */       else
/*     */       {
/* 383 */         String os = OsUtils.getOSName();
/* 384 */         String filename = "";
/* 385 */         String nativeOSUtilsLibDir = (String)this.m_args.get("NativeOSUtilsLibDir");
/* 386 */         if (nativeOSUtilsLibDir != null)
/*     */         {
/* 388 */           if (!nativeOSUtilsLibDir.endsWith("/"))
/*     */           {
/* 390 */             nativeOSUtilsLibDir = nativeOSUtilsLibDir + '/';
/*     */           }
/*     */ 
/*     */         }
/* 395 */         else if (m_IdcHomeDir != null)
/*     */         {
/* 397 */           nativeOSUtilsLibDir = m_IdcHomeDir + "components/NativeOsUtils/lib/";
/*     */         }
/*     */ 
/* 400 */         if ((os != null) && (nativeOSUtilsLibDir != null))
/*     */         {
/* 402 */           if ((os.equals("hpux64")) || (os.equals("hpux")))
/*     */           {
/* 404 */             filename = "/libJniNativeOsUtils.sl";
/*     */           }
/* 406 */           else if (os.startsWith("win"))
/*     */           {
/* 408 */             filename = "/JniNativeOsUtils.dll";
/*     */           }
/*     */           else
/*     */           {
/* 412 */             filename = "/libJniNativeOsUtils.so";
/*     */           }
/*     */ 
/* 415 */           libPath = nativeOSUtilsLibDir + os + '/' + "7.2.1.1" + filename;
/* 416 */           File file = new File(libPath);
/*     */ 
/* 418 */           if ((file.exists()) && (file.canRead()) && (file.isFile()))
/*     */           {
/* 420 */             System.load(libPath);
/*     */           }
/*     */           else
/*     */           {
/* 425 */             libPath = nativeOSUtilsLibDir + os + filename;
/* 426 */             file = new File(libPath);
/* 427 */             if ((file.exists()) && (file.canRead()) && (file.isFile()))
/*     */             {
/* 429 */               System.load(libPath);
/*     */             }
/*     */             else
/*     */             {
/* 434 */               System.loadLibrary(libName);
/*     */             }
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 440 */           System.loadLibrary(libName);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */       try
/*     */       {
/* 448 */         getNativeVersion();
/*     */       }
/*     */       catch (Throwable t2)
/*     */       {
/* 452 */         AssertionError ae = new AssertionError("!syNativeOsUtilsNotLoaded");
/*     */         try
/*     */         {
/* 456 */           ae.initCause(t2);
/*     */ 
/* 458 */           report("system", t);
/*     */         }
/*     */         catch (Throwable t3)
/*     */         {
/* 462 */           t3.printStackTrace();
/*     */         }
/* 464 */         throw ae;
/*     */       }
/*     */     }
/* 467 */     initNativeOsConstants();
/*     */   }
/*     */ 
/*     */   public void checkVersion()
/*     */   {
/* 472 */     if (m_checkFinished)
/*     */     {
/* 474 */       return;
/*     */     }
/* 476 */     String version = getNativeVersion();
/* 477 */     if (!version.equals("7.2.1.1"))
/*     */     {
/* 479 */       report(null, "NativeOsUtils version mismatch. The native code version is \"" + version + "\" and the Java version is \"" + "7.2.1.1" + "\".");
/*     */     }
/*     */ 
/* 483 */     m_checkFinished = true;
/*     */   }
/*     */ 
/*     */   public abstract void report(String paramString, Object paramObject);
/*     */ 
/*     */   public static String getJavaNativeVersion()
/*     */   {
/* 490 */     return "7.2.1.1";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 495 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98970 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.NativeOsUtilsBase
 * JD-Core Version:    0.5.4
 */