/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.common.filter.PurgerInterface;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcReleasable;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedOutputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.ByteArrayOutputStream;
/*      */ import java.io.CharArrayWriter;
/*      */ import java.io.Closeable;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.OutputStream;
/*      */ import java.io.OutputStreamWriter;
/*      */ import java.io.PrintWriter;
/*      */ import java.io.Reader;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.io.Writer;
/*      */ import java.lang.reflect.Constructor;
/*      */ import java.lang.reflect.Field;
/*      */ import java.lang.reflect.InvocationTargetException;
/*      */ import java.net.DatagramSocket;
/*      */ import java.net.InetAddress;
/*      */ import java.net.ServerSocket;
/*      */ import java.net.Socket;
/*      */ import java.nio.MappedByteBuffer;
/*      */ import java.nio.channels.FileChannel;
/*      */ import java.nio.channels.FileChannel.MapMode;
/*      */ import java.util.Arrays;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ import java.util.zip.GZIPInputStream;
/*      */ import java.util.zip.GZIPOutputStream;
/*      */ import java.util.zip.ZipFile;
/*      */ 
/*      */ public class FileUtils
/*      */ {
/*      */   public static final long INVALID_FILE_MARKER = -2L;
/*   45 */   public static ConfigFileDescriptorFactory m_cfgDescriptorFactory = new ConfigFileDescriptorFactorySimpleImplementor();
/*      */ 
/*   47 */   public static String m_javaSystemEncoding = "utf8";
/*   48 */   public static String m_isoSystemEncoding = null;
/*   49 */   public static boolean m_useMappedIO = true;
/*   50 */   public static boolean m_useMappedDiff = true;
/*   51 */   public static BufferPool m_defaultBufferPool = null;
/*      */ 
/*   59 */   public static boolean m_neverLock = false;
/*   60 */   public static boolean m_directoryLockInit = false;
/*   61 */   public static FileUtilsLockDirectory m_lockDirectory = new FileUtilsLockDirectory();
/*   62 */   public static boolean m_validateRenames = true;
/*   63 */   public static int m_lockTimeout = 120;
/*   64 */   public static int m_minLockTimeout = 5;
/*      */ 
/*   66 */   public static int m_reserveDirectoryTestTime = 30;
/*      */ 
/*   68 */   public static int m_reserveDirectoryTestThreads = 10;
/*      */ 
/*   71 */   public static boolean m_bgLockThreadStarted = false;
/*   72 */   public static FileUtilsLockThread m_longTermLockImpl = new FileUtilsLockThread();
/*   73 */   public static int m_touchMonitorInterval = 30000;
/*      */   public static final int F_USE_DEFAULTS = 0;
/*      */   public static final int F_NO_SIGNATURE = 1;
/*      */   public static final int F_GZIP = 2;
/*      */   public static final int F_FAIL_IF_DEST_EXISTS = 4;
/*      */   public static final int F_COPY_DELETE_FOR_RENAME = 8;
/*      */   public static final int F_COPY_CREATE_DIRS = 16;
/*      */   public static final int F_SAFE_UPDATE = 16;
/*      */   public static final int F_SAFE_VERBOSE = 80;
/*      */   public static final int F_UNBUFFERED = 32;
/*      */   public static final int F_NO_LOCK = 16;
/*      */   public static final int F_USE_LOCK = 32;
/*  102 */   public static final byte[] UTF8_SIGNATURE = { -17, -69, -65 };
/*  103 */   public static final byte[] UNICODE_BIG_SIGNATURE = { -2, -1 };
/*  104 */   public static final byte[] UNICODE_LITTLE_SIGNATURE = { -1, -2 };
/*      */   public static final int F_DELETE_TARGET = 1;
/*      */ 
/*      */   @Deprecated
/*      */   public static final int F_FORCE_COPY = 1;
/*      */   public static final int F_REPLACE_FILES = 2;
/*      */   public static final int F_SKIP_EXISTING = 4;
/*      */   public static final int F_USE_FILE_LINKS = 8;
/*      */   public static final int F_KEEP_GOING = 16;
/*      */   public static final int F_IGNORE_ERROR = 48;
/*      */   public static final int F_PLAIN = 1;
/*      */   public static final int F_REQUIRE_TRAILING_SLASH = 2;
/*      */   public static final int F_PRESERVE_LEADING_TWO = 4;
/*      */   public static final int F_PRESERVE_COLON_SLASH_COMBO = 8;
/*      */   public static final int F_PRESERVE_BACKSLASHES = 16;
/*      */   public static final int F_DOUBLE_ENCODE_BACKSLASHES = 32;
/*      */   public static final int F_TREAT_AS_DIRECTORY = 64;
/*      */   public static final int F_FILE = 13;
/*      */   public static final int F_DIR = 78;
/*      */   public static final int F_SHOULD_BE_DIRECTORY = 0;
/*      */   public static final int F_SHOULD_BE_FILE = 1;
/*      */   public static final int F_SHOULD_BE_WRITABLE = 2;
/*      */   public static final int F_PREPARE_FOR_LOCKS = 1;
/*      */   public static final int F_CREATE_AS_PRIVATE = 2;
/*      */   public static final int F_TEST_WRITE_SPACE = 256;
/*      */   public static final int F_TEST_8DOT3 = 512;
/*      */   public static final int F_TEST_RESERVE_DIRECTORY = 1024;
/*      */   public static final int F_TEST_WITH_SYNC = 2048;
/*      */   public static final int F_TEST_VERBOSE = 4096;
/*  200 */   protected static NativeOsUtils m_utils = null;
/*      */   protected static Class s_classSafeFileOutputStream;
/*      */   protected static Map<String, Integer> s_flagsForSafeFileOutputStream;
/*      */ 
/*      */   public static void checkDirectoryLockInit()
/*      */   {
/*  204 */     if (m_directoryLockInit)
/*      */       return;
/*  206 */     synchronized (m_lockDirectory)
/*      */     {
/*  208 */       if (!m_directoryLockInit)
/*      */       {
/*  210 */         updateLockDirectoryConfiguration();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void updateLockDirectoryConfiguration()
/*      */   {
/*  218 */     m_lockDirectory.m_validateRenames = m_validateRenames;
/*  219 */     m_lockDirectory.m_lockTimeout = m_lockTimeout;
/*  220 */     m_lockDirectory.m_minLockTimeout = m_minLockTimeout;
/*  221 */     m_longTermLockImpl.m_touchMonitorInterval = m_touchMonitorInterval;
/*  222 */     m_directoryLockInit = true;
/*      */   }
/*      */ 
/*      */   public static void reserveDirectoryEx(String dir, boolean promoteToLongLock, boolean specifyTimeout, int timeout, String agent, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/*  228 */     checkDirectoryLockInit();
/*  229 */     dir = directorySlashes(dir);
/*  230 */     if (agent == null)
/*      */     {
/*  232 */       agent = "<no-agent>";
/*      */     }
/*  234 */     FileDirLockData lockData = createDirLockingData(dir, agent);
/*  235 */     m_lockDirectory.reserveDirectoryImplement(lockData, promoteToLongLock, specifyTimeout, timeout, cxt);
/*      */   }
/*      */ 
/*      */   public static void reserveDirectory(String dir) throws ServiceException
/*      */   {
/*  240 */     reserveDirectory(dir, false);
/*      */   }
/*      */ 
/*      */   public static void reserveDirectory(String dir, boolean reserveAnyway)
/*      */     throws ServiceException
/*      */   {
/*  250 */     if ((storeInDB(dir)) && (!reserveAnyway))
/*      */     {
/*  252 */       return;
/*      */     }
/*      */ 
/*  256 */     m_lockDirectory.m_validateRenames = m_validateRenames;
/*  257 */     m_lockDirectory.m_lockTimeout = m_lockTimeout;
/*  258 */     m_lockDirectory.m_minLockTimeout = m_minLockTimeout;
/*      */ 
/*  261 */     dir = directorySlashes(dir);
/*  262 */     String agent = "<no-agent>";
/*  263 */     FileDirLockData lockData = createDirLockingData(dir, agent);
/*  264 */     m_lockDirectory.reserveDirectoryImplement(lockData, false, false, 0L, null);
/*      */   }
/*      */ 
/*      */   public static boolean isDirectoryLockedByThisThread(String dir, ExecutionContext cxt)
/*      */   {
/*  269 */     dir = directorySlashes(dir);
/*  270 */     if (storeInDB(dir))
/*      */     {
/*  272 */       return true;
/*      */     }
/*  274 */     FileDirLockData lockData = retrieveDirLockingData(dir);
/*  275 */     return (lockData != null) && (lockData.m_lockingThread == Thread.currentThread());
/*      */   }
/*      */ 
/*      */   public static boolean checkForAndPromoteToLongTermLock(String dir, ExecutionContext cxt) throws ServiceException
/*      */   {
/*  280 */     dir = directorySlashes(dir);
/*  281 */     FileDirLockData lockData = retrieveDirLockingData(dir);
/*  282 */     return m_lockDirectory.checkForAndPromoteToLongTermLock(lockData, cxt);
/*      */   }
/*      */ 
/*      */   public static void promoteExistingLockToLongTermLock(String dir, ExecutionContext cxt) throws ServiceException
/*      */   {
/*  287 */     promoteExistingLockToLongTermLock(dir, cxt, false);
/*      */   }
/*      */ 
/*      */   public static void promoteExistingLockToLongTermLock(String dir, ExecutionContext cxt, boolean promoteAnyway)
/*      */     throws ServiceException
/*      */   {
/*  297 */     if ((storeInDB(dir)) && (!promoteAnyway))
/*      */     {
/*  299 */       return;
/*      */     }
/*  301 */     dir = directorySlashes(dir);
/*  302 */     FileDirLockData lockData = retrieveDirLockingData(dir);
/*  303 */     m_lockDirectory.promoteExistingLockToLongTermLock(lockData, cxt);
/*      */   }
/*      */ 
/*      */   public static void releaseDirectoryEx(String dir, ExecutionContext cxt)
/*      */   {
/*  308 */     checkDirectoryLockInit();
/*  309 */     m_lockDirectory.releaseDirectoryImplement(dir, cxt);
/*      */   }
/*      */ 
/*      */   public static void releaseDirectory(String dir)
/*      */   {
/*  314 */     releaseDirectory(dir, false);
/*      */   }
/*      */ 
/*      */   public static void releaseDirectory(String dir, boolean releaseAnyway)
/*      */   {
/*  323 */     if ((storeInDB(dir)) && (!releaseAnyway))
/*      */     {
/*  325 */       return;
/*      */     }
/*  327 */     checkDirectoryLockInit();
/*  328 */     m_lockDirectory.releaseDirectoryImplement(dir, null);
/*      */   }
/*      */ 
/*      */   public static FileDirLockData createDirLockingData(String dir, String agent)
/*      */   {
/*  333 */     return m_lockDirectory.createLockingData(dir, agent);
/*      */   }
/*      */ 
/*      */   public static FileDirLockData retrieveDirLockingData(String dir)
/*      */   {
/*  342 */     return m_lockDirectory.retrieveLockingData(dir);
/*      */   }
/*      */ 
/*      */   public static void checkLongTermLockInit()
/*      */   {
/*  347 */     if (m_bgLockThreadStarted)
/*      */       return;
/*  349 */     synchronized (m_longTermLockImpl.m_lockObject)
/*      */     {
/*  351 */       if (!m_bgLockThreadStarted)
/*      */       {
/*  353 */         m_longTermLockImpl.m_touchMonitorInterval = m_touchMonitorInterval;
/*  354 */         Thread t = new Thread(m_longTermLockImpl, "FileUtilsLockThread");
/*  355 */         t.setDaemon(true);
/*  356 */         t.start();
/*  357 */         m_bgLockThreadStarted = true;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static boolean checkLongTermLock(String dir, String lockName)
/*      */   {
/*  365 */     checkLongTermLockInit();
/*  366 */     return m_longTermLockImpl.checkLockExists(dir, lockName);
/*      */   }
/*      */ 
/*      */   public static boolean reserveLongTermLock(String dir, String lockName, String agent, long timeout, boolean waitForEver)
/*      */     throws ServiceException
/*      */   {
/*  372 */     return reserveLongTermLock(dir, lockName, agent, timeout, waitForEver, false);
/*      */   }
/*      */ 
/*      */   public static boolean reserveLongTermLock(String dir, String lockName, String agent, long timeout, boolean waitForEver, boolean reserveAnyway)
/*      */     throws ServiceException
/*      */   {
/*  383 */     if ((storeInDB(dir)) && (!reserveAnyway))
/*      */     {
/*  385 */       return true;
/*      */     }
/*  387 */     checkLongTermLockInit();
/*  388 */     boolean retVal = false;
/*      */     try
/*      */     {
/*  391 */       retVal = m_longTermLockImpl.createLock(dir, lockName, agent, timeout, waitForEver);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  395 */       String msg = LocaleUtils.encodeMessage("syErrorCreatingLongTermLock", dir, lockName);
/*  396 */       throw new ServiceException(msg, e);
/*      */     }
/*  398 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static void releaseLongTermLock(String dir, String lockName, String agent)
/*      */   {
/*  404 */     releaseLongTermLock(dir, lockName, agent, false);
/*      */   }
/*      */ 
/*      */   public static void releaseLongTermLock(String dir, String lockName, String agent, boolean releaseAnyway)
/*      */   {
/*  414 */     if ((storeInDB(dir)) && (!releaseAnyway))
/*      */     {
/*  416 */       return;
/*      */     }
/*  418 */     m_longTermLockImpl.releaseLock(dir, lockName, agent);
/*      */   }
/*      */ 
/*      */   public static boolean usesAtomicCreateFileMethod()
/*      */   {
/*  426 */     return m_longTermLockImpl.usesAtomicCreateFileMethod();
/*      */   }
/*      */ 
/*      */   public static boolean atomicCreateFile(File file)
/*      */     throws IOException
/*      */   {
/*  436 */     return m_longTermLockImpl.atomicCreateFile(file);
/*      */   }
/*      */ 
/*      */   public static String[] getMatchingFileNames(String dir, String wildCard)
/*      */   {
/*  453 */     if ((dir == null) || (dir.length() == 0))
/*      */     {
/*  455 */       dir = ".";
/*      */     }
/*      */ 
/*  458 */     File dirFile = FileUtilsCfgBuilder.getCfgFile(dir, null, true);
/*  459 */     String[] list = new String[0];
/*  460 */     if (!dirFile.isDirectory())
/*      */     {
/*  462 */       return list;
/*      */     }
/*      */ 
/*  465 */     WildcardFilter filter = new WildcardFilter(wildCard);
/*  466 */     list = dirFile.list(filter);
/*  467 */     return list;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void checkOrCreateDirectoryWithLock(String dir, int numParents, boolean hasLock)
/*      */     throws ServiceException
/*      */   {
/*  477 */     checkOrCreateDirectoryPrepareForLocks(dir, numParents, hasLock);
/*      */   }
/*      */ 
/*      */   public static void checkOrCreateDirectoryPrepareForLocks(String dir, int numParents, boolean hasLock)
/*      */     throws ServiceException
/*      */   {
/*  490 */     checkOrCreateDirectory(dir, numParents, (hasLock) ? 1 : 0);
/*      */   }
/*      */ 
/*      */   public static void checkOrCreateDirectory(String dir, int numParents, int flags)
/*      */     throws ServiceException
/*      */   {
/*  506 */     if ((dir == null) || (dir.length() == 0) || (dir.equals(".")) || (dir.equals("..")))
/*      */     {
/*  508 */       return;
/*      */     }
/*  510 */     if (storeInDB(dir))
/*      */     {
/*  512 */       return;
/*      */     }
/*      */ 
/*  515 */     int dirLen = dir.length();
/*      */ 
/*  517 */     while ((dirLen > 0) && (dir.charAt(dirLen - 1) == '/'))
/*      */     {
/*  519 */       --dirLen;
/*      */     }
/*  521 */     if (dirLen == 0)
/*      */     {
/*  523 */       return;
/*      */     }
/*      */ 
/*  526 */     dir = dir.substring(0, dirLen);
/*      */ 
/*  528 */     char ch = dir.charAt(dirLen - 1);
/*  529 */     if ((ch == '.') || (ch == ':'))
/*      */     {
/*  531 */       return;
/*      */     }
/*      */ 
/*  534 */     File df = new File(dir);
/*  535 */     if (df.exists())
/*      */     {
/*  537 */       if (!df.isDirectory())
/*      */       {
/*  539 */         validateDirectory(dir, "!syFileUtilsUnableToCreateDir");
/*      */       }
/*  541 */       if ((flags & 0x1) != 0)
/*      */       {
/*  543 */         createLockIfNeeded(dir);
/*      */       }
/*  545 */       return;
/*      */     }
/*  547 */     if (numParents > 0)
/*      */     {
/*  550 */       int parentIndex = dir.lastIndexOf(47);
/*  551 */       if (parentIndex > 0)
/*      */       {
/*  553 */         String parent = dir.substring(0, parentIndex);
/*      */ 
/*  555 */         checkOrCreateDirectory(parent, numParents - 1, flags & 0xFFFFFFFD);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  560 */     if (((flags & 0x2) != 0) && 
/*  562 */       (EnvUtils.m_useNativeOSUtils) && (!loadNativeOsUtils()))
/*      */     {
/*      */       try
/*      */       {
/*  566 */         new NativeOsUtils();
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/*  570 */         throw new ServiceException(t, "syFileUtilsUnableToCreateSpecifiedDir", new Object[] { dir });
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  575 */     if (!df.mkdir())
/*      */     {
/*  577 */       if (df.exists())
/*      */       {
/*  582 */         if (SystemUtils.m_verbose)
/*      */         {
/*  584 */           Exception tmp = new Exception("trace exception");
/*  585 */           Report.trace("fileaccesss", "Warning: Could not create directory " + dir + " with number of parents " + numParents + ". Someone else must have come along and created the directory " + "during the call of this method.", tmp);
/*      */         }
/*      */ 
/*  591 */         return;
/*      */       }
/*  593 */       throw new ServiceException(LocaleUtils.encodeMessage("syFileUtilsUnableToCreateSpecifiedDir", null, dir));
/*      */     }
/*      */ 
/*  597 */     if ((m_utils != null) && ((flags & 0x2) != 0))
/*      */     {
/*  599 */       int rc = 0;
/*  600 */       if (EnvUtils.isFamily("windows"))
/*      */       {
/*  603 */         rc = m_utils.chmod(dir, 448);
/*      */       }
/*      */       else
/*      */       {
/*  607 */         rc = m_utils.chmod(dir, 448);
/*      */       }
/*  609 */       if (rc != 0)
/*      */       {
/*  612 */         df.delete();
/*  613 */         IdcMessage msg = IdcMessageFactory.lc("syFileUtilsUnableToCreateSpecifiedDir", new Object[] { dir });
/*  614 */         msg.m_prior = IdcMessageFactory.lc();
/*  615 */         msg.m_prior.m_msgSimple = m_utils.getErrorMessage(rc);
/*  616 */         throw new ServiceException(null, msg);
/*      */       }
/*      */     }
/*      */ 
/*  620 */     if ((flags & 0x1) == 0)
/*      */       return;
/*  622 */     createLockIfNeeded(dir);
/*      */   }
/*      */ 
/*      */   public static boolean createLockIfNeeded(String dir)
/*      */   {
/*  633 */     if (m_neverLock)
/*      */     {
/*  635 */       return false;
/*      */     }
/*      */ 
/*  641 */     File temp1 = FileUtilsCfgBuilder.getCfgFile(dir + "/lockwait.dat", "Lock", false);
/*  642 */     if (temp1.exists())
/*      */     {
/*  644 */       return false;
/*      */     }
/*  646 */     Writer out = null;
/*      */     try
/*      */     {
/*  649 */       out = FileUtilsCfgBuilder.getCfgWriter(temp1);
/*  650 */       String msg = "File for locking";
/*  651 */       out.write(msg);
/*  652 */       int i = 1;
/*      */ 
/*  660 */       return i;
/*      */     }
/*      */     catch (Throwable e)
/*      */     {
/*  656 */       e.printStackTrace();
/*      */     }
/*      */     finally
/*      */     {
/*  660 */       closeObject(out);
/*      */     }
/*  662 */     return false;
/*      */   }
/*      */ 
/*      */   public static void checkOrCreateDirectory(String dir, int numParents)
/*      */     throws ServiceException
/*      */   {
/*  668 */     checkOrCreateDirectoryEx(dir, numParents, false);
/*      */   }
/*      */ 
/*      */   public static void checkOrCreateDirectoryEx(String dir, int numParents, boolean hasLock)
/*      */     throws ServiceException
/*      */   {
/*  674 */     checkOrCreateDirectoryPrepareForLocks(dir, numParents, hasLock);
/*      */   }
/*      */ 
/*      */   public static void checkOrCreateSubDirectory(String rootDir, String subDir)
/*      */     throws ServiceException
/*      */   {
/*  681 */     checkOrCreateSubDirectoryEx(rootDir, subDir, false);
/*      */   }
/*      */ 
/*      */   public static void checkOrCreateSubDirectoryEx(String rootDir, String subDir, boolean hasLock)
/*      */     throws ServiceException
/*      */   {
/*  693 */     int n = subDir.lastIndexOf(47);
/*  694 */     if (n <= 0)
/*      */     {
/*  697 */       return;
/*      */     }
/*      */ 
/*  700 */     int count = 0;
/*  701 */     for (int i = 0; i < n; ++i)
/*      */     {
/*  703 */       if (subDir.charAt(i) != '/')
/*      */         continue;
/*  705 */       ++count;
/*      */     }
/*      */ 
/*  708 */     checkOrCreateDirectoryEx(rootDir + subDir.substring(0, n), count, hasLock);
/*      */   }
/*      */ 
/*      */   public static void testFileSystem(String dir)
/*      */     throws ServiceException
/*      */   {
/*  722 */     testFileSystem(dir, 256);
/*      */   }
/*      */ 
/*      */   public static void testFileSystem(String fsdir, int flags)
/*      */     throws ServiceException
/*      */   {
/*  733 */     if (storeInDB(fsdir))
/*      */     {
/*  735 */       return;
/*      */     }
/*      */ 
/*  739 */     validateDirectory(fsdir, IdcMessageFactory.lc("syFileUtilsDirNotFound", new Object[] { fsdir }), 2);
/*      */ 
/*  741 */     ServerSocket s = null;
/*  742 */     String commonDir = getAbsolutePath(fsdir, "fstest");
/*  743 */     checkOrCreateDirectory(commonDir, 0);
/*  744 */     String privateDir = null;
/*      */     long unique;
/*      */     try
/*      */     {
/*      */       try
/*      */       {
/*  752 */         s = new ServerSocket(0);
/*  753 */         InetAddress addr = InetAddress.getLocalHost();
/*  754 */         unique = s.getLocalPort();
/*  755 */         String addrStr = addr.toString().replace('/', '.').replace(':', '_') + "." + unique;
/*  756 */         privateDir = getAbsolutePath(commonDir, addrStr);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  760 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*  763 */       checkOrCreateDirectory(privateDir, 1);
/*      */ 
/*  765 */       if ((flags & 0x100) != 0)
/*      */       {
/*  767 */         String test = "This is a test file.  This is a test file. This is a test file\n This is a test file.  This is a test file.  This is a test file.\n";
/*      */ 
/*  769 */         BufferedWriter bw = null;
/*  770 */         File file = null;
/*      */         try
/*      */         {
/*  773 */           file = new File(privateDir + "/test-write.txt");
/*  774 */           bw = openDataWriter(file);
/*      */ 
/*  776 */           for (int i = 0; i < 1000; ++i)
/*      */           {
/*  778 */             bw.write(test);
/*      */           }
/*  780 */           bw.close();
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/*      */         }
/*      */         finally
/*      */         {
/*  788 */           closeObject(bw);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       String file8dot3;
/*  919 */       if ((privateDir != null) && 
/*  925 */         ((flags & 0x400) == 0)) {
/*      */         try {
/*  927 */           deleteDirectory(new File(commonDir), true);
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/*  932 */           t.printStackTrace();
/*      */         }
/*      */       }
/*  935 */       closeObject(s);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static int testReserveDir(String dir, PrintWriter stat, int openFlags, String threadName, int counter)
/*      */     throws ServiceException
/*      */   {
/*  942 */     if (stat == null)
/*      */     {
/*  944 */       stat = new PrintWriter(new CharArrayWriter());
/*      */     }
/*  946 */     OutputStream out = null;
/*      */     try
/*      */     {
/*  949 */       stat.println(threadName + ": reserving");
/*  950 */       reserveDirectory(dir);
/*  951 */       stat.println(threadName + ": reserved");
/*  952 */       String file = getAbsolutePath(dir, "test.txt");
/*  953 */       stat.println(threadName + ": opening");
/*  954 */       out = openOutputStream(file, openFlags);
/*  955 */       stat.println(threadName + ": opened");
/*  956 */       Writer w = new OutputStreamWriter(out);
/*  957 */       ++counter;
/*  958 */       w.write("counter=" + counter + "\n");
/*  959 */       w.close();
/*  960 */       out = null;
/*  961 */       Properties props = new Properties();
/*      */ 
/*  964 */       for (int i = 0; i < 3; ++i)
/*      */       {
/*  966 */         stat.println(threadName + ": reading");
/*  967 */         InputStream fis = FileUtilsCfgBuilder.getCfgInputStream(file);
/*  968 */         props.load(fis);
/*  969 */         fis.close();
/*  970 */         String counterStr = props.getProperty("counter");
/*  971 */         int counterVal = NumberUtils.parseInteger(counterStr, 0);
/*  972 */         if (counterVal == counter) {
/*      */           continue;
/*      */         }
/*  975 */         throw new ServiceException("!$reserveDirectory() lock blowthough detected, expected" + counter + " but read " + counterVal);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  988 */       abort(out);
/*  989 */       stat.println(threadName + ": releasing " + dir);
/*  990 */       releaseDirectory(dir);
/*  991 */       stat.println(threadName + ": released");
/*      */     }
/*  993 */     return counter;
/*      */   }
/*      */ 
/*      */   public static OutputStream openOutputStream(String path, int flags)
/*      */     throws IOException
/*      */   {
/* 1001 */     if (storeInDB(path))
/*      */     {
/* 1003 */       return FileUtilsCfgBuilder.getCfgOutputStream(path, null);
/*      */     }
/*      */     OutputStream out;
/*      */     OutputStream out;
/* 1008 */     if ((flags & 0x10) != 0)
/*      */     {
/* 1010 */       out = createSafeFileOutputStream(path, (flags & 0x50 & 0xFFFFFFEF) != 0);
/*      */     }
/*      */     else
/*      */     {
/* 1014 */       out = new FileOutputStream(path);
/*      */     }
/* 1016 */     if ((flags & 0x20) == 0)
/*      */     {
/* 1018 */       out = new BufferedOutputStream(out);
/*      */     }
/* 1020 */     return out;
/*      */   }
/*      */ 
/*      */   public static void abort(OutputStream out)
/*      */   {
/* 1025 */     if (out == null)
/*      */     {
/* 1027 */       return;
/*      */     }
/* 1029 */     if (!out instanceof SafeFileOutputStream)
/*      */       return;
/* 1031 */     ((SafeFileOutputStream)out).abort();
/*      */   }
/*      */ 
/*      */   public static void abortAndClose(OutputStream out)
/*      */   {
/* 1037 */     if (out == null)
/*      */     {
/* 1039 */       return;
/*      */     }
/* 1041 */     if (out instanceof SafeFileOutputStream)
/*      */     {
/* 1043 */       ((SafeFileOutputStream)out).abortAndClose();
/*      */     }
/*      */     else
/*      */     {
/* 1047 */       closeObject(out);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static Class getSafeFileOutputStreamClass()
/*      */   {
/* 1056 */     Class cl = s_classSafeFileOutputStream;
/* 1057 */     if (cl == null)
/*      */     {
/* 1059 */       String[] flagNames = { "F_PURGE_BACKUP", "F_VERBOSE" };
/*      */       try
/*      */       {
/* 1062 */         cl = Class.forName("intradoc.common.SafeFileOutputStream");
/* 1063 */         Map flagsMap = new HashMap(flagNames.length);
/* 1064 */         for (String flagName : flagNames)
/*      */         {
/* 1066 */           Field field = cl.getField(flagName);
/* 1067 */           int value = field.getInt(null);
/* 1068 */           flagsMap.put(flagName, Integer.valueOf(value));
/*      */         }
/* 1070 */         s_flagsForSafeFileOutputStream = flagsMap;
/* 1071 */         s_classSafeFileOutputStream = cl;
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 1075 */         throw new RuntimeException(t);
/*      */       }
/*      */     }
/* 1078 */     return cl;
/*      */   }
/*      */ 
/*      */   protected static OutputStream createSafeFileOutputStream(String path, boolean isVerbose)
/*      */     throws IOException
/*      */   {
/*      */     try
/*      */     {
/* 1093 */       Class cl = getSafeFileOutputStreamClass();
/* 1094 */       Map flagsMap = s_flagsForSafeFileOutputStream;
/* 1095 */       int f = ((Integer)flagsMap.get("F_PURGE_BACKUP")).intValue();
/* 1096 */       if (isVerbose)
/*      */       {
/* 1098 */         f |= ((Integer)flagsMap.get("F_VERBOSE")).intValue();
/*      */       }
/* 1100 */       Constructor c = cl.getConstructor(new Class[] { String.class, Integer.TYPE });
/* 1101 */       return (OutputStream)c.newInstance(new Object[] { path, Integer.valueOf(f) });
/*      */     }
/*      */     catch (InvocationTargetException ite)
/*      */     {
/* 1105 */       Throwable t = ite.getCause();
/* 1106 */       if (t instanceof IOException)
/*      */       {
/* 1108 */         throw ((IOException)t);
/*      */       }
/* 1110 */       throw new IOException(t);
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1114 */       throw new IOException(t);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static BufferedWriter openDataWriter(String dir, String filename) throws IOException
/*      */   {
/* 1120 */     String path = getAbsolutePath(dir, filename);
/* 1121 */     OutputStream out = FileUtilsCfgBuilder.getCfgOutputStream(path, null);
/* 1122 */     BufferedWriter writer = openDataWriterEx(out, m_javaSystemEncoding, 0);
/* 1123 */     return writer;
/*      */   }
/*      */ 
/*      */   public static BufferedWriter openDataWriter(File file) throws IOException
/*      */   {
/* 1128 */     OutputStream out = FileUtilsCfgBuilder.getCfgOutputStream(file);
/* 1129 */     BufferedWriter writer = openDataWriterEx(out, m_javaSystemEncoding, 0);
/* 1130 */     return writer;
/*      */   }
/*      */ 
/*      */   public static BufferedWriter openDataWriter(String path) throws IOException
/*      */   {
/* 1135 */     OutputStream out = FileUtilsCfgBuilder.getCfgOutputStream(path, null);
/* 1136 */     BufferedWriter writer = openDataWriterEx(out, m_javaSystemEncoding, 0);
/* 1137 */     return writer;
/*      */   }
/*      */ 
/*      */   public static BufferedWriter openDataWriter(String path, String encoding, int flags)
/*      */     throws IOException
/*      */   {
/*      */     OutputStream out;
/*      */     OutputStream out;
/* 1144 */     if (storeInDB(path))
/*      */     {
/* 1146 */       out = FileUtilsCfgBuilder.getCfgOutputStream(path, null);
/*      */     }
/*      */     else
/*      */     {
/*      */       OutputStream out;
/* 1150 */       if (((flags & 0x10) != 0) && (checkFile(path, 1) == 0))
/*      */       {
/* 1153 */         out = createSafeFileOutputStream(path, (flags & 0x50 & 0xFFFFFFEF) != 0);
/*      */       }
/*      */       else
/*      */       {
/* 1157 */         out = new FileOutputStream(path);
/*      */       }
/* 1159 */       if (encoding == null)
/*      */       {
/* 1161 */         encoding = m_javaSystemEncoding;
/*      */       }
/*      */     }
/* 1164 */     BufferedWriter writer = openDataWriterEx(out, encoding, flags & 0xFFFFFFDF);
/* 1165 */     return writer;
/*      */   }
/*      */ 
/*      */   public static BufferedWriter openDataWriter(String dir, String filename, String encoding)
/*      */     throws IOException, UnsupportedEncodingException
/*      */   {
/* 1171 */     String path = getAbsolutePath(dir, filename);
/* 1172 */     OutputStream out = FileUtilsCfgBuilder.getCfgOutputStream(path, null);
/* 1173 */     BufferedWriter writer = openDataWriterEx(out, encoding, 0);
/* 1174 */     return writer;
/*      */   }
/*      */ 
/*      */   public static BufferedWriter openDataWriter(File file, String encoding)
/*      */     throws IOException, UnsupportedEncodingException
/*      */   {
/* 1181 */     OutputStream out = FileUtilsCfgBuilder.getCfgOutputStream(file);
/* 1182 */     BufferedWriter writer = openDataWriterEx(out, encoding, 0);
/* 1183 */     return writer;
/*      */   }
/*      */ 
/*      */   public static BufferedWriter openDataWriter(OutputStream out, String encoding, int flags)
/*      */     throws IOException, UnsupportedEncodingException
/*      */   {
/* 1190 */     return openDataWriterEx(out, encoding, flags);
/*      */   }
/*      */ 
/*      */   public static BufferedWriter openDataWriterEx(OutputStream out, String encoding, int flags)
/*      */     throws IOException, UnsupportedEncodingException
/*      */   {
/* 1197 */     if ((flags & 0x2) != 0)
/*      */     {
/* 1199 */       out = new GZIPOutputStream(out);
/*      */     }
/*      */ 
/* 1202 */     if (encoding == null)
/*      */     {
/* 1204 */       encoding = m_javaSystemEncoding;
/*      */     }
/*      */ 
/* 1207 */     if ((flags & 0x1) == 0)
/*      */     {
/* 1209 */       writeFileEncodingSignatureHeader(encoding, out);
/*      */     }
/*      */     BufferedWriter writer;
/*      */     BufferedWriter writer;
/* 1211 */     if (SystemUtils.getFailOnEncodingFailure())
/*      */     {
/* 1213 */       writer = new BufferedWriter(new EncodingCheckWriter(new OutputStreamWriter(out, encoding), encoding, SystemUtils.getFailOnEncodingFailure()));
/*      */     }
/*      */     else
/*      */     {
/*      */       BufferedWriter writer;
/* 1217 */       if (encoding != null)
/*      */       {
/* 1219 */         writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
/*      */       }
/*      */       else
/*      */       {
/* 1223 */         writer = new BufferedWriter(new OutputStreamWriter(out));
/*      */       }
/*      */     }
/* 1226 */     return writer;
/*      */   }
/*      */ 
/*      */   public static void writeFileEncodingSignatureHeader(String encoding, OutputStream out) throws IOException
/*      */   {
/* 1231 */     if ((encoding != null) && (SystemUtils.getWriteUTF8Signature()) && (((encoding.equalsIgnoreCase("UTF8")) || (encoding.equalsIgnoreCase("UTF-8")))))
/*      */     {
/* 1234 */       out.write(UTF8_SIGNATURE);
/*      */     }
/* 1236 */     if ((encoding == null) || (!SystemUtils.getWriteUnicodeSignature()))
/*      */       return;
/* 1238 */     encoding = encoding.toLowerCase();
/* 1239 */     if ((encoding.startsWith("unicode")) || (encoding.startsWith("unicodebig")))
/*      */     {
/* 1242 */       out.write(UNICODE_BIG_SIGNATURE);
/*      */     } else {
/* 1244 */       if (!encoding.startsWith("unicodelittle"))
/*      */         return;
/* 1246 */       out.write(UNICODE_LITTLE_SIGNATURE);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static BufferedReader openDataReader(String dir, String filename)
/*      */     throws IOException
/*      */   {
/* 1253 */     String path = getAbsolutePath(dir, filename);
/* 1254 */     InputStream fis = FileUtilsCfgBuilder.getCfgInputStream(path);
/* 1255 */     int flags = getDefaultEncodingFlags();
/* 1256 */     BufferedReader reader = openDataReaderEx(fis, m_javaSystemEncoding, flags);
/* 1257 */     return reader;
/*      */   }
/*      */ 
/*      */   public static BufferedReader openDataReader(File file) throws IOException
/*      */   {
/* 1262 */     InputStream fis = FileUtilsCfgBuilder.getCfgInputStream(file.getAbsolutePath());
/* 1263 */     int flags = getDefaultEncodingFlags();
/* 1264 */     BufferedReader reader = openDataReaderEx(fis, m_javaSystemEncoding, flags);
/* 1265 */     return reader;
/*      */   }
/*      */ 
/*      */   public static BufferedReader openDataReader(String path) throws IOException
/*      */   {
/* 1270 */     InputStream fis = FileUtilsCfgBuilder.getCfgInputStream(path);
/* 1271 */     int flags = getDefaultEncodingFlags();
/* 1272 */     BufferedReader reader = openDataReaderEx(fis, m_javaSystemEncoding, flags);
/* 1273 */     return reader;
/*      */   }
/*      */ 
/*      */   public static BufferedReader openDataReader(String dir, String filename, String encoding) throws IOException
/*      */   {
/* 1278 */     String path = getAbsolutePath(dir, filename);
/* 1279 */     InputStream fis = FileUtilsCfgBuilder.getCfgInputStream(path);
/* 1280 */     int flags = getDefaultEncodingFlags();
/* 1281 */     BufferedReader reader = openDataReaderEx(fis, encoding, flags);
/* 1282 */     return reader;
/*      */   }
/*      */ 
/*      */   public static BufferedReader openDataReader(File file, String encoding) throws IOException
/*      */   {
/* 1287 */     InputStream fis = FileUtilsCfgBuilder.getCfgInputStream(file.getAbsoluteFile());
/* 1288 */     int flags = getDefaultEncodingFlags();
/* 1289 */     BufferedReader reader = openDataReaderEx(fis, encoding, flags);
/* 1290 */     return reader;
/*      */   }
/*      */ 
/*      */   public static BufferedReader openDataReader(BufferedInputStream bstream, String encoding)
/*      */     throws IOException
/*      */   {
/* 1296 */     int flags = getDefaultEncodingFlags();
/*      */ 
/* 1298 */     BufferedReader r = openDataReaderEx(bstream, encoding, flags);
/* 1299 */     return r;
/*      */   }
/*      */ 
/*      */   protected static int getDefaultEncodingFlags()
/*      */   {
/* 1304 */     int flags = 0;
/* 1305 */     if (SystemUtils.isActiveTrace("encoding"))
/*      */     {
/* 1307 */       flags |= 1;
/*      */     }
/* 1309 */     if (SystemUtils.getFailOnReplacementCharacterDefault())
/*      */     {
/* 1311 */       flags |= 2;
/*      */     }
/* 1313 */     return flags;
/*      */   }
/*      */ 
/*      */   public static BufferedReader openDataReaderEx(InputStream inStream, String encoding, int flags)
/*      */     throws IOException
/*      */   {
/* 1320 */     if ((flags & 0x2) != 0)
/*      */     {
/* 1322 */       inStream = new GZIPInputStream(inStream);
/*      */     }
/*      */     InputStreamReader streamReader;
/*      */     InputStreamReader streamReader;
/* 1324 */     if ((encoding == null) || (encoding.length() == 0))
/*      */     {
/* 1326 */       streamReader = new InputStreamReader(inStream);
/*      */     }
/*      */     else
/*      */     {
/* 1330 */       streamReader = new InputStreamReader(inStream, encoding);
/*      */     }
/*      */     BufferedReader reader;
/*      */     BufferedReader reader;
/* 1334 */     if (EncodingCheckReader.areFlagsInteresting(flags))
/*      */     {
/* 1336 */       reader = new BufferedReader(new EncodingCheckReader(streamReader, encoding, flags));
/*      */     }
/*      */     else
/*      */     {
/* 1341 */       reader = new BufferedReader(streamReader);
/*      */     }
/*      */ 
/* 1345 */     if ((encoding != null) && (encoding.toLowerCase().startsWith("utf")))
/*      */     {
/* 1347 */       reader.mark(1);
/* 1348 */       char ch = (char)reader.read();
/* 1349 */       if (ch != 65279)
/*      */       {
/* 1351 */         reader.reset();
/*      */       }
/*      */     }
/* 1354 */     return reader;
/*      */   }
/*      */ 
/*      */   public static int checkFile(String path, int flags)
/*      */   {
/* 1371 */     return checkFile(path, (flags & 0x1) != 0, (flags & 0x2) != 0);
/*      */   }
/*      */ 
/*      */   public static int checkFile(String path, boolean shouldBeFile, boolean needWriteAccess)
/*      */   {
/* 1390 */     File file = FileUtilsCfgBuilder.getCfgFile(path, null, !shouldBeFile);
/* 1391 */     return checkFile(file, shouldBeFile, needWriteAccess);
/*      */   }
/*      */ 
/*      */   public static int checkFile(File file, boolean shouldBeFile, boolean needWriteAccess)
/*      */   {
/* 1396 */     if (!file.exists())
/*      */     {
/* 1398 */       return -16;
/*      */     }
/* 1400 */     if (!file.canRead())
/*      */     {
/* 1402 */       return -18;
/*      */     }
/* 1404 */     if ((needWriteAccess == true) && (!file.canWrite()))
/*      */     {
/* 1406 */       return -19;
/*      */     }
/* 1408 */     if (shouldBeFile != file.isFile())
/*      */     {
/* 1410 */       return -24;
/*      */     }
/* 1412 */     return 0;
/*      */   }
/*      */ 
/*      */   public static boolean checkPathExists(String path)
/*      */   {
/* 1422 */     File file = new File(path);
/* 1423 */     return file.exists();
/*      */   }
/*      */ 
/*      */   public static IdcMessage getErrorMsg(String path, int flags, int errorCode)
/*      */   {
/* 1432 */     boolean shouldBeFile = (flags & 0x1) != 0;
/* 1433 */     String msg = getErrorMsg(path, shouldBeFile, errorCode);
/* 1434 */     IdcMessage idcmsg = null;
/* 1435 */     if (msg != null)
/*      */     {
/* 1437 */       idcmsg = IdcMessageFactory.lc();
/* 1438 */       idcmsg.m_msgEncoded = msg;
/*      */     }
/* 1440 */     return idcmsg;
/*      */   }
/*      */ 
/*      */   public static String getErrorMsg(String path, boolean shouldBeFile, int errorCode)
/*      */   {
/* 1447 */     String msg = null;
/*      */ 
/* 1449 */     switch (errorCode)
/*      */     {
/*      */     case 0:
/* 1452 */       if (shouldBeFile)
/*      */       {
/* 1454 */         msg = LocaleUtils.encodeMessage("syFileUtilsFileAvail", null, path); break label215:
/*      */       }
/*      */ 
/* 1458 */       msg = LocaleUtils.encodeMessage("syFileUtilsDirAvail", null, path);
/*      */ 
/* 1461 */       break;
/*      */     case -16:
/* 1463 */       if (shouldBeFile)
/*      */       {
/* 1465 */         msg = LocaleUtils.encodeMessage("syFileUtilsFileNotFound", null, path); break label215:
/*      */       }
/*      */ 
/* 1469 */       msg = LocaleUtils.encodeMessage("syFileUtilsDirNotFound", null, path);
/*      */ 
/* 1472 */       break;
/*      */     case -18:
/* 1474 */       if (shouldBeFile)
/*      */       {
/* 1476 */         msg = LocaleUtils.encodeMessage("syFileUtilsFileNoAccess", null, path); break label215:
/*      */       }
/*      */ 
/* 1480 */       msg = LocaleUtils.encodeMessage("syFileUtilsDirNoAccess", null, path);
/*      */ 
/* 1483 */       break;
/*      */     case -19:
/* 1485 */       if (shouldBeFile)
/*      */       {
/* 1487 */         msg = LocaleUtils.encodeMessage("syFileUtilsFileReadOnly", null, path); break label215:
/*      */       }
/*      */ 
/* 1491 */       msg = LocaleUtils.encodeMessage("syFileUtilsDirReadOnly", null, path);
/*      */ 
/* 1494 */       break;
/*      */     case -24:
/* 1496 */       if (shouldBeFile)
/*      */       {
/* 1498 */         msg = LocaleUtils.encodeMessage("syFileUtilsFileInvalidPath", null, path); break label215:
/*      */       }
/*      */ 
/* 1502 */       msg = LocaleUtils.encodeMessage("syFileUtilsDirInvalidPath", null, path);
/*      */ 
/* 1505 */       break;
/*      */     default:
/* 1507 */       if (shouldBeFile)
/*      */       {
/* 1509 */         msg = LocaleUtils.encodeMessage("syFileUtilsFileSystemError", null, path); break label215:
/*      */       }
/*      */ 
/* 1513 */       msg = LocaleUtils.encodeMessage("syFileUtilsDirSystemError", null, path);
/*      */     }
/*      */ 
/* 1518 */     label215: return msg;
/*      */   }
/*      */ 
/*      */   public static void validateDirectory(String dir, String errMsg) throws ServiceException
/*      */   {
/* 1523 */     validatePath(dir, errMsg, false, true);
/*      */   }
/*      */ 
/*      */   public static void validateDirectory(String dir, IdcMessage errMsg, int flags)
/*      */     throws ServiceException
/*      */   {
/* 1529 */     validatePath(dir, errMsg, flags);
/*      */   }
/*      */ 
/*      */   public static void validateFile(String path, String errMsg) throws ServiceException
/*      */   {
/* 1534 */     validatePath(path, errMsg, true, true);
/*      */   }
/*      */ 
/*      */   public static void validatePath(String path, IdcMessage errMsg, int flags)
/*      */     throws ServiceException
/*      */   {
/* 1540 */     int retVal = -16;
/* 1541 */     if (path != null)
/*      */     {
/* 1543 */       retVal = checkFile(path, flags);
/* 1544 */       if (retVal >= 0)
/*      */       {
/* 1546 */         if (flags > 255)
/*      */         {
/*      */           try
/*      */           {
/* 1550 */             testFileSystem(path, flags);
/*      */           }
/*      */           catch (ServiceException e)
/*      */           {
/* 1554 */             throw new ServiceException(e, errMsg);
/*      */           }
/*      */         }
/* 1557 */         return;
/*      */       }
/*      */     }
/*      */ 
/* 1561 */     if (errMsg == null)
/*      */     {
/* 1563 */       errMsg = getErrorMsg(path, flags, retVal);
/*      */     }
/*      */     else
/*      */     {
/* 1567 */       errMsg.m_prior = getErrorMsg(path, flags, retVal);
/*      */     }
/* 1569 */     throw new ServiceException(null, retVal, errMsg);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void validatePath(String path, String errMsg, boolean shouldBeFile, boolean needWriteAccess)
/*      */     throws ServiceException
/*      */   {
/* 1577 */     if ((!shouldBeFile) && (storeInDB(path)))
/*      */     {
/* 1579 */       return;
/*      */     }
/*      */ 
/* 1582 */     int retVal = -16;
/* 1583 */     if (path != null)
/*      */     {
/* 1585 */       retVal = checkFile(path, shouldBeFile, needWriteAccess);
/* 1586 */       if (retVal >= 0) {
/* 1587 */         return;
/*      */       }
/*      */     }
/* 1590 */     errMsg = LocaleUtils.appendMessage(errMsg, getErrorMsg(path, shouldBeFile, retVal));
/* 1591 */     throw new ServiceException(retVal, errMsg);
/*      */   }
/*      */ 
/*      */   public static void deleteFile(String path, boolean deleteDir)
/*      */     throws ServiceException
/*      */   {
/* 1603 */     File f = FileUtilsCfgBuilder.getCfgFile(path, null, false);
/* 1604 */     if ((f.isDirectory()) && (deleteDir))
/*      */     {
/* 1606 */       deleteDirectory(f, true);
/*      */     }
/*      */     else
/*      */     {
/* 1610 */       deleteFile(path);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void deleteFile(String path)
/*      */   {
/* 1616 */     if (path == null)
/*      */     {
/* 1618 */       return;
/*      */     }
/*      */ 
/* 1621 */     File f = FileUtilsCfgBuilder.getCfgFile(path, null, false);
/* 1622 */     if (!f.exists())
/*      */       return;
/* 1624 */     if (Report.m_verbose)
/*      */     {
/* 1626 */       trace("FileUtils.deleteFile() deleting: " + path);
/*      */     }
/* 1628 */     f.delete();
/*      */   }
/*      */ 
/*      */   public static void deleteDirectory(File dir, boolean deleteSelf)
/*      */     throws ServiceException
/*      */   {
/* 1635 */     deleteDirectoryWithPurge(dir, deleteSelf, null);
/*      */   }
/*      */ 
/*      */   public static void deleteDirectoryWithPurge(File dir, boolean deleteSelf, PurgerInterface purger)
/*      */     throws ServiceException
/*      */   {
/* 1641 */     dir = FileUtilsCfgBuilder.getCfgFile(dir.getAbsolutePath(), null, true);
/* 1642 */     String[] files = dir.list();
/* 1643 */     if (files == null)
/*      */     {
/* 1645 */       return;
/*      */     }
/*      */ 
/* 1648 */     for (int i = 0; i < files.length; ++i)
/*      */     {
/* 1650 */       String path = directorySlashes(dir.getAbsolutePath());
/* 1651 */       path = path + files[i];
/* 1652 */       File dFile = FileUtilsCfgBuilder.getCfgFile(path, null);
/* 1653 */       if (dFile.isDirectory() == true)
/*      */       {
/* 1655 */         deleteDirectoryWithPurge(dFile, true, purger);
/*      */       }
/*      */       else
/*      */       {
/* 1659 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1661 */           trace("FileUtils.deleteDirectory() deleting: " + path);
/*      */         }
/* 1663 */         if ((!storeInDB(path)) && (purger != null))
/*      */         {
/* 1665 */           purger.doPreDelete(dFile, null, null);
/*      */         }
/* 1667 */         dFile.delete();
/*      */       }
/*      */     }
/* 1670 */     if (deleteSelf != true)
/*      */       return;
/* 1672 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1674 */       trace("FileUtils.deleteDirectory() deleting: " + dir.getAbsolutePath());
/*      */     }
/* 1676 */     dir.delete();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static CharSequence fixDirectorySlashes(CharSequence dir, boolean isDir)
/*      */   {
/* 1684 */     return fixDirectorySlashes(dir, (isDir) ? 78 : 13);
/*      */   }
/*      */ 
/*      */   public static CharSequence fixDirectorySlashes(CharSequence dir, int flags)
/*      */   {
/* 1689 */     if (flags == 0)
/*      */     {
/* 1691 */       throw new AssertionError("!$Illegal flags passed to fixDirectorySlashes.");
/*      */     }
/* 1693 */     if (dir == null)
/*      */     {
/* 1695 */       return null;
/*      */     }
/*      */ 
/* 1698 */     int index = 0;
/* 1699 */     int length = dir.length();
/* 1700 */     IdcStringBuilder buff = null;
/*      */ 
/* 1702 */     if (((flags & 0x4) != 0) && 
/* 1706 */       (length >= 2))
/*      */     {
/* 1709 */       char c0 = dir.charAt(0);
/* 1710 */       char c1 = dir.charAt(1);
/* 1711 */       if ((c0 == c1) && ((('\\' == c0) || ('/' == c1))))
/*      */       {
/* 1713 */         index = 2;
/*      */       }
/* 1715 */       if (((flags & 0x20) != 0) && (((c0 == '\\') || (c1 == '\\'))))
/*      */       {
/* 1718 */         buff = new IdcStringBuilder(dir.length() + 32);
/* 1719 */         if (c0 == '\\')
/*      */         {
/* 1721 */           buff.append("\\\\");
/*      */         }
/*      */         else
/*      */         {
/* 1725 */           buff.append(c0);
/*      */         }
/* 1727 */         if (c1 == '\\')
/*      */         {
/* 1729 */           buff.append("\\\\");
/*      */         }
/*      */         else
/*      */         {
/* 1733 */           buff.append(c1);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1739 */     boolean mustAppend = false;
/* 1740 */     for (int i = index; i < length; )
/*      */     {
/* 1742 */       char ch = dir.charAt(i);
/* 1743 */       int startIndex = i;
/* 1744 */       switch (ch)
/*      */       {
/*      */       case '/':
/*      */       case '\\':
/* 1748 */         if (ch == '\\')
/*      */         {
/* 1750 */           mustAppend = true;
/*      */         }
/*      */         while (true)
/*      */         {
/* 1754 */           ++i;
/* 1755 */           if (i >= length) {
/*      */             break;
/*      */           }
/*      */ 
/* 1759 */           ch = dir.charAt(i);
/* 1760 */           if ((ch != '\\') && (ch != '/')) {
/*      */             break;
/*      */           }
/*      */ 
/* 1764 */           mustAppend = true;
/*      */         }
/* 1766 */         if ((mustAppend) && (buff == null))
/*      */         {
/* 1768 */           buff = new IdcStringBuilder(length + 4);
/* 1769 */           buff.append(dir, 0, startIndex);
/*      */         }
/* 1771 */         if (buff != null)
/*      */         {
/* 1773 */           buff.append('/'); } break;
/*      */       case ':':
/* 1777 */         if (((flags & 0x8) != 0) && 
/* 1780 */           (length >= i + 3) && (i > 1))
/*      */         {
/* 1783 */           char c1 = dir.charAt(i + 1);
/* 1784 */           char c2 = dir.charAt(i + 2);
/* 1785 */           if ((c1 == c2) && ((('\\' == c1) || ('/' == c2))))
/*      */           {
/* 1787 */             if (buff == null)
/*      */             {
/* 1789 */               buff = new IdcStringBuilder(length + 4);
/* 1790 */               buff.append(dir, 0, startIndex);
/*      */             }
/* 1792 */             buff.append("://");
/* 1793 */             i += 3;
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1798 */           if (buff != null)
/*      */           {
/* 1800 */             buff.append(ch);
/*      */           }
/* 1802 */           ++i;
/* 1803 */         }break;
/*      */       default:
/* 1806 */         if (buff != null)
/*      */         {
/* 1808 */           buff.append(ch);
/*      */         }
/* 1810 */         ++i;
/*      */       }
/*      */     }
/*      */ 
/* 1814 */     if (((flags & 0x2) != 0) && 
/* 1817 */       (length > 0))
/*      */     {
/* 1819 */       char ch = dir.charAt(length - 1);
/* 1820 */       if ((ch != '/') && (ch != '\\'))
/*      */       {
/* 1822 */         if (buff == null)
/*      */         {
/* 1824 */           buff = new IdcStringBuilder(length + 1);
/* 1825 */           buff.append(dir);
/*      */         }
/* 1827 */         buff.append('/');
/*      */       }
/*      */     }
/*      */ 
/* 1831 */     if (buff == null)
/*      */     {
/* 1833 */       return dir;
/*      */     }
/* 1835 */     return buff;
/*      */   }
/*      */ 
/*      */   public static String directorySlashesEx(String dir, boolean isDir)
/*      */   {
/* 1849 */     if (dir == null)
/*      */     {
/* 1851 */       return null;
/*      */     }
/* 1853 */     CharSequence seq = fixDirectorySlashes(dir, (isDir) ? 78 : 13);
/* 1854 */     if (seq instanceof String)
/*      */     {
/* 1856 */       return (String)seq;
/*      */     }
/* 1858 */     return seq.toString();
/*      */   }
/*      */ 
/*      */   public static String directorySlashes(String dir)
/*      */   {
/* 1872 */     return directorySlashesEx(dir, true);
/*      */   }
/*      */ 
/*      */   public static String fileSlashes(String filePath)
/*      */   {
/* 1884 */     return directorySlashesEx(filePath, false);
/*      */   }
/*      */ 
/*      */   public static void renameFile(String from, String to)
/*      */     throws ServiceException
/*      */   {
/* 1896 */     renameFileEx(from, to, 0);
/*      */   }
/*      */ 
/*      */   public static void renameFileEx(String from, String to, int flags)
/*      */     throws ServiceException
/*      */   {
/* 1913 */     File fromFile = FileUtilsCfgBuilder.getCfgFile(from, null, false);
/* 1914 */     File toFile = FileUtilsCfgBuilder.getCfgFile(to, null, false);
/*      */ 
/* 1916 */     if (from.equals(to))
/*      */     {
/* 1918 */       return;
/*      */     }
/*      */ 
/* 1921 */     IdcMessage errMsg = IdcMessageFactory.lc("syFileUtilsUnableToRenameFile", new Object[] { from, to });
/* 1922 */     if (!fromFile.exists())
/*      */     {
/* 1924 */       throw new ServiceException(null, IdcMessageFactory.lc(errMsg, "syFileUtilsFileDoesNotExist", new Object[0]));
/*      */     }
/*      */ 
/* 1927 */     if (toFile.exists())
/*      */     {
/* 1929 */       if ((flags & 0x4) == 4)
/*      */       {
/* 1931 */         throw new ServiceException(LocaleUtils.encodeMessage("syFileExists", null, to));
/*      */       }
/* 1933 */       if ((EnvUtils.getOSFamily().equals("windows")) && 
/* 1936 */         (!toFile.delete()))
/*      */       {
/* 1946 */         System.gc();
/* 1947 */         toFile.delete();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1954 */       Boolean fromDB = new Boolean(storeInDB(from));
/* 1955 */       Boolean toDB = new Boolean(storeInDB(to));
/*      */ 
/* 1957 */       if ((!fromDB.equals(toDB)) && ((flags & 0x8) == 8))
/*      */       {
/* 1959 */         copyFile(from, to);
/* 1960 */         deleteFile(from);
/*      */       }
/*      */       else
/*      */       {
/* 1964 */         int retryCount = 0;
/* 1965 */         if ((EnvUtils.isFamily("windows")) && ((flags & 0x4) == 0))
/*      */         {
/* 1971 */           retryCount = 50;
/*      */         }
/*      */ 
/* 1974 */         fromFile.renameTo(toFile);
/* 1975 */         for (int i = 0; i < retryCount; ++i)
/*      */         {
/* 1977 */           if (!fromFile.exists()) {
/*      */             break;
/*      */           }
/*      */ 
/* 1981 */           System.gc();
/* 1982 */           synchronized (m_lockDirectory)
/*      */           {
/* 1984 */             SystemUtils.sleep(10 * i);
/* 1985 */             toFile.delete();
/* 1986 */             fromFile.renameTo(toFile);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1993 */       IdcMessage idcMsg = IdcMessageFactory.lc(t, "syGeneralError", new Object[0]);
/* 1994 */       idcMsg.m_prior = errMsg;
/* 1995 */       throw new ServiceException(null, idcMsg);
/*      */     }
/*      */ 
/* 1998 */     if (fromFile.exists())
/*      */     {
/* 2000 */       boolean throwError = true;
/* 2001 */       if (EnvUtils.isFamily("windows"))
/*      */       {
/* 2005 */         int[] delays = { 25, 100, 1000 };
/* 2006 */         for (int delay : delays)
/*      */         {
/* 2008 */           SystemUtils.sleep(delay);
/* 2009 */           throwError = fromFile.exists();
/* 2010 */           if (!throwError) {
/*      */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 2016 */       if (throwError)
/*      */       {
/* 2018 */         throw new ServiceException(null, IdcMessageFactory.lc(errMsg, "syFileUtilsTargetFileNoAccess", new Object[0]));
/*      */       }
/*      */     }
/*      */ 
/* 2022 */     trace("Renamed from " + from + " to " + to);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void renameFileEx(File fromFile, File toFile, boolean forceRename)
/*      */     throws ServiceException
/*      */   {
/* 2030 */     SystemUtils.reportDeprecatedUsage("use FileUtils.renameFile()");
/* 2031 */     renameFileEx(fromFile.getAbsolutePath(), toFile.getAbsolutePath(), (forceRename) ? 0 : 4);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void copyFile(File from, File to, boolean forceCopy)
/*      */     throws ServiceException
/*      */   {
/* 2038 */     SystemUtils.reportDeprecatedUsage("use FileUtils.copyFile(File, File)");
/* 2039 */     int flags = 0;
/* 2040 */     if (!forceCopy)
/*      */     {
/* 2042 */       flags |= 4;
/*      */     }
/* 2044 */     copyFileEx(from.getAbsolutePath(), to.getAbsolutePath(), flags);
/*      */   }
/*      */ 
/*      */   public static void copyFile(String from, String to) throws ServiceException
/*      */   {
/* 2049 */     copyFileEx(from, to, 0); } 
/*      */   public static void copyFileEx(String from, String to, int flags) throws ServiceException { // Byte code:
/*      */     //   0: aload_0
/*      */     //   1: aconst_null
/*      */     //   2: iconst_0
/*      */     //   3: invokestatic 53	intradoc/common/FileUtilsCfgBuilder:getCfgFile	(Ljava/lang/String;Ljava/lang/String;Z)Ljava/io/File;
/*      */     //   6: astore_3
/*      */     //   7: aload_1
/*      */     //   8: aconst_null
/*      */     //   9: iconst_0
/*      */     //   10: invokestatic 53	intradoc/common/FileUtilsCfgBuilder:getCfgFile	(Ljava/lang/String;Ljava/lang/String;Z)Ljava/io/File;
/*      */     //   13: astore 4
/*      */     //   15: aload_0
/*      */     //   16: aload_1
/*      */     //   17: invokevirtual 61	java/lang/String:equals	(Ljava/lang/Object;)Z
/*      */     //   20: ifeq +4 -> 24
/*      */     //   23: return
/*      */     //   24: aload 4
/*      */     //   26: invokevirtual 67	java/io/File:exists	()Z
/*      */     //   29: ifeq +34 -> 63
/*      */     //   32: iload_2
/*      */     //   33: iconst_4
/*      */     //   34: iand
/*      */     //   35: iconst_4
/*      */     //   36: if_icmpne +21 -> 57
/*      */     //   39: new 45	intradoc/common/ServiceException
/*      */     //   42: dup
/*      */     //   43: bipush 239
/*      */     //   45: ldc_w 328
/*      */     //   48: aconst_null
/*      */     //   49: aload_1
/*      */     //   50: invokestatic 44	intradoc/common/LocaleUtils:encodeMessage	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;
/*      */     //   53: invokespecial 298	intradoc/common/ServiceException:<init>	(ILjava/lang/String;)V
/*      */     //   56: athrow
/*      */     //   57: aload 4
/*      */     //   59: invokevirtual 101	java/io/File:delete	()Z
/*      */     //   62: pop
/*      */     //   63: aload_3
/*      */     //   64: invokevirtual 67	java/io/File:exists	()Z
/*      */     //   67: ifne +22 -> 89
/*      */     //   70: new 45	intradoc/common/ServiceException
/*      */     //   73: dup
/*      */     //   74: bipush 240
/*      */     //   76: ldc_w 346
/*      */     //   79: aconst_null
/*      */     //   80: aload_0
/*      */     //   81: aload_1
/*      */     //   82: invokestatic 347	intradoc/common/LocaleUtils:encodeMessage	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/String;
/*      */     //   85: invokespecial 298	intradoc/common/ServiceException:<init>	(ILjava/lang/String;)V
/*      */     //   88: athrow
/*      */     //   89: aconst_null
/*      */     //   90: astore 5
/*      */     //   92: aconst_null
/*      */     //   93: astore 6
/*      */     //   95: aconst_null
/*      */     //   96: astore 7
/*      */     //   98: aload_1
/*      */     //   99: invokestatic 20	intradoc/common/FileUtils:storeInDB	(Ljava/lang/String;)Z
/*      */     //   102: ifne +52 -> 154
/*      */     //   105: iload_2
/*      */     //   106: bipush 16
/*      */     //   108: iand
/*      */     //   109: ifeq +45 -> 154
/*      */     //   112: aload_1
/*      */     //   113: invokestatic 348	intradoc/common/FileUtils:getParent	(Ljava/lang/String;)Ljava/lang/String;
/*      */     //   116: astore 8
/*      */     //   118: aload 8
/*      */     //   120: iconst_0
/*      */     //   121: invokestatic 123	intradoc/common/FileUtils:checkOrCreateDirectory	(Ljava/lang/String;I)V
/*      */     //   124: goto +30 -> 154
/*      */     //   127: astore 9
/*      */     //   129: new 45	intradoc/common/ServiceException
/*      */     //   132: dup
/*      */     //   133: aload 9
/*      */     //   135: ldc_w 349
/*      */     //   138: iconst_2
/*      */     //   139: anewarray 78	java/lang/Object
/*      */     //   142: dup
/*      */     //   143: iconst_0
/*      */     //   144: aload_0
/*      */     //   145: aastore
/*      */     //   146: dup
/*      */     //   147: iconst_1
/*      */     //   148: aload_1
/*      */     //   149: aastore
/*      */     //   150: invokespecial 79	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;Ljava/lang/String;[Ljava/lang/Object;)V
/*      */     //   153: athrow
/*      */     //   154: aload_1
/*      */     //   155: aconst_null
/*      */     //   156: invokestatic 191	intradoc/common/FileUtilsCfgBuilder:getCfgOutputStream	(Ljava/lang/String;Ljava/lang/String;)Ljava/io/OutputStream;
/*      */     //   159: astore 5
/*      */     //   161: aload_0
/*      */     //   162: invokestatic 178	intradoc/common/FileUtilsCfgBuilder:getCfgInputStream	(Ljava/lang/String;)Ljava/io/InputStream;
/*      */     //   165: astore 6
/*      */     //   167: getstatic 350	intradoc/common/FileUtils:m_defaultBufferPool	Lintradoc/common/BufferPool;
/*      */     //   170: ifnull +9 -> 179
/*      */     //   173: ldc_w 351
/*      */     //   176: goto +6 -> 182
/*      */     //   179: sipush 16384
/*      */     //   182: istore 8
/*      */     //   184: iload 8
/*      */     //   186: iconst_0
/*      */     //   187: invokestatic 352	intradoc/common/FileUtils:createBufferForStreaming	(II)Ljava/lang/Object;
/*      */     //   190: checkcast 353	[B
/*      */     //   193: checkcast 353	[B
/*      */     //   196: astore 7
/*      */     //   198: aload 6
/*      */     //   200: aload 7
/*      */     //   202: invokevirtual 354	java/io/InputStream:read	([B)I
/*      */     //   205: dup
/*      */     //   206: istore 9
/*      */     //   208: iconst_m1
/*      */     //   209: if_icmpeq +16 -> 225
/*      */     //   212: aload 5
/*      */     //   214: aload 7
/*      */     //   216: iconst_0
/*      */     //   217: iload 9
/*      */     //   219: invokevirtual 355	java/io/OutputStream:write	([BII)V
/*      */     //   222: goto -24 -> 198
/*      */     //   225: aload 7
/*      */     //   227: invokestatic 356	intradoc/common/FileUtils:releaseBufferForStreaming	(Ljava/lang/Object;)V
/*      */     //   230: aload 5
/*      */     //   232: aload 6
/*      */     //   234: invokestatic 357	intradoc/common/FileUtils:closeFiles	(Ljava/io/OutputStream;Ljava/io/InputStream;)V
/*      */     //   237: goto +47 -> 284
/*      */     //   240: astore 8
/*      */     //   242: new 45	intradoc/common/ServiceException
/*      */     //   245: dup
/*      */     //   246: aload 8
/*      */     //   248: ldc_w 349
/*      */     //   251: iconst_2
/*      */     //   252: anewarray 78	java/lang/Object
/*      */     //   255: dup
/*      */     //   256: iconst_0
/*      */     //   257: aload_0
/*      */     //   258: aastore
/*      */     //   259: dup
/*      */     //   260: iconst_1
/*      */     //   261: aload_1
/*      */     //   262: aastore
/*      */     //   263: invokespecial 79	intradoc/common/ServiceException:<init>	(Ljava/lang/Throwable;Ljava/lang/String;[Ljava/lang/Object;)V
/*      */     //   266: athrow
/*      */     //   267: astore 10
/*      */     //   269: aload 7
/*      */     //   271: invokestatic 356	intradoc/common/FileUtils:releaseBufferForStreaming	(Ljava/lang/Object;)V
/*      */     //   274: aload 5
/*      */     //   276: aload 6
/*      */     //   278: invokestatic 357	intradoc/common/FileUtils:closeFiles	(Ljava/io/OutputStream;Ljava/io/InputStream;)V
/*      */     //   281: aload 10
/*      */     //   283: athrow
/*      */     //   284: return
/*      */     //
/*      */     // Exception table:
/*      */     //   from	to	target	type
/*      */     //   118	124	127	intradoc/common/ServiceException
/*      */     //   98	225	240	java/io/IOException
/*      */     //   98	225	267	finally
/*      */     //   240	269	267	finally } 
/* 2125 */   @Deprecated
/*      */   public static void copyDirectory(File fromDir, File toDir, int numParents, boolean deleteTarget) throws ServiceException { int flags = (deleteTarget) ? 1 : 0;
/* 2126 */     flags |= 48;
/* 2127 */     copyDirectoryWithFlags(fromDir, toDir, numParents, null, flags); }
/*      */ 
/*      */ 
/*      */   public static void copyDirectoryWithFlags(File fromDir, File toDir, int numParents, String exclusionFilter, int flags)
/*      */     throws ServiceException
/*      */   {
/* 2133 */     fromDir = FileUtilsCfgBuilder.getCfgFile(fromDir.getPath(), null, true);
/* 2134 */     toDir = FileUtilsCfgBuilder.getCfgFile(toDir.getPath(), null, true);
/*      */ 
/* 2136 */     ServiceException se = copyDirectoryWithFlags(null, fromDir, toDir, numParents, exclusionFilter, flags);
/*      */ 
/* 2139 */     if (se == null)
/*      */       return;
/* 2141 */     if ((flags & 0x30) != 0)
/*      */     {
/* 2143 */       Report.trace(null, null, se);
/*      */     }
/*      */     else
/*      */     {
/* 2147 */       throw se;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static ServiceException copyDirectoryWithFlags(ServiceException se, File fromDir, File toDir, int numParents, String exclusionFilter, int flags)
/*      */     throws ServiceException
/*      */   {
/* 2165 */     boolean forceCopy = (flags & 0x1) != 0;
/* 2166 */     boolean allowReplaceFiles = (flags & 0x2) != 0;
/* 2167 */     if (fromDir.equals(toDir))
/*      */     {
/* 2169 */       return se;
/*      */     }
/*      */ 
/* 2172 */     String[] files = fromDir.list();
/* 2173 */     if (files == null)
/*      */     {
/* 2175 */       return se;
/*      */     }
/*      */ 
/* 2178 */     if (toDir.exists())
/*      */     {
/* 2180 */       if (forceCopy)
/*      */       {
/* 2182 */         deleteDirectory(toDir, false);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 2187 */       String path = directorySlashes(toDir.getAbsolutePath());
/* 2188 */       checkOrCreateDirectory(path, numParents);
/*      */     }
/*      */ 
/* 2191 */     for (int i = 0; i < files.length; ++i)
/*      */     {
/* 2193 */       String oldPath = directorySlashes(fromDir.getAbsolutePath());
/* 2194 */       oldPath = oldPath + files[i];
/* 2195 */       File oldFile = FileUtilsCfgBuilder.getCfgFile(oldPath, null);
/* 2196 */       boolean oldFileIsDir = oldFile.isDirectory();
/* 2197 */       if (exclusionFilter != null)
/*      */       {
/* 2199 */         if (oldFileIsDir)
/*      */         {
/* 2201 */           oldPath = oldPath + "/";
/*      */         }
/* 2203 */         if (StringUtils.match(oldPath, exclusionFilter, true)) {
/*      */           continue;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2209 */       String newPath = directorySlashes(toDir.getAbsolutePath());
/* 2210 */       newPath = newPath + files[i];
/* 2211 */       File newFile = FileUtilsCfgBuilder.getCfgFile(newPath, null);
/*      */ 
/* 2213 */       if (oldFileIsDir)
/*      */       {
/* 2215 */         checkOrCreateDirectory(newPath, numParents);
/* 2216 */         se = copyDirectoryWithFlags(se, oldFile, newFile, numParents, exclusionFilter, flags);
/*      */       }
/*      */       else
/*      */       {
/*      */         try
/*      */         {
/* 2222 */           String of = oldFile.getAbsolutePath();
/* 2223 */           String nf = newFile.getAbsolutePath();
/* 2224 */           int nfStatus = checkFile(nf, 1);
/* 2225 */           if (((flags & 0x4) == 0) || (nfStatus != 0))
/*      */           {
/* 2227 */             boolean didCopy = false;
/* 2228 */             if (((flags & 0x8) != 0) && (loadNativeOsUtils()) && (m_utils.isLinkSupported()))
/*      */             {
/* 2231 */               int rc = 0;
/* 2232 */               if (nfStatus == 0)
/*      */               {
/* 2235 */                 newFile.delete();
/*      */               }
/*      */ 
/* 2240 */               if ((rc = m_utils.link(of, nf)) != 0)
/*      */               {
/* 2242 */                 Report.debug(null, "unable to link " + of + " to " + nf + ", falling back to copy: " + m_utils.getErrorMessage(rc), null);
/*      */               }
/*      */               else
/*      */               {
/* 2248 */                 didCopy = true;
/*      */               }
/*      */             }
/* 2251 */             if (!didCopy)
/*      */             {
/* 2253 */               int copyFlags = (allowReplaceFiles) ? 0 : 4;
/* 2254 */               copyFileEx(of, nf, copyFlags);
/*      */             }
/*      */           }
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/* 2260 */           if (se == null)
/*      */           {
/* 2262 */             se = new ServiceException(e);
/*      */           }
/*      */           else
/*      */           {
/* 2266 */             se.addCause(e);
/*      */           }
/* 2268 */           if ((flags & 0x10) == 0)
/*      */           {
/* 2270 */             throw se;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2276 */     return se;
/*      */   }
/*      */ 
/*      */   public static void closeFiles(OutputStream fos, InputStream fis)
/*      */   {
/* 2281 */     closeObjects(fos, fis);
/*      */   }
/*      */ 
/*      */   public static void close(Object o) throws IOException
/*      */   {
/* 2286 */     closeObjectEx(o);
/*      */   }
/*      */ 
/*      */   public static void discard(Object o)
/*      */   {
/* 2291 */     closeObject(o);
/*      */   }
/*      */ 
/*      */   public static void closeObjectEx(Object o) throws IOException
/*      */   {
/* 2296 */     if (o == null)
/*      */     {
/* 2298 */       return;
/*      */     }
/*      */ 
/* 2301 */     boolean throwException = false;
/* 2302 */     boolean unknownObject = false;
/*      */     try
/*      */     {
/* 2305 */       if (o instanceof Closeable)
/*      */       {
/* 2307 */         ((Closeable)o).close();
/*      */       }
/* 2309 */       else if (o instanceof Socket)
/*      */       {
/* 2311 */         ((Socket)o).close();
/*      */       }
/* 2313 */       else if (o instanceof DatagramSocket)
/*      */       {
/* 2315 */         ((DatagramSocket)o).close();
/*      */       }
/* 2317 */       else if (o instanceof Socket)
/*      */       {
/* 2319 */         ((Socket)o).close();
/*      */       }
/* 2321 */       else if (o instanceof ServerSocket)
/*      */       {
/* 2323 */         ((ServerSocket)o).close();
/*      */       }
/* 2325 */       else if (o instanceof ZipFile)
/*      */       {
/* 2327 */         ((ZipFile)o).close();
/*      */       }
/* 2329 */       else if (o instanceof InputStream)
/*      */       {
/* 2331 */         ((InputStream)o).close();
/*      */       }
/* 2333 */       else if (o instanceof OutputStream)
/*      */       {
/* 2335 */         ((OutputStream)o).close();
/*      */       }
/* 2337 */       else if (o instanceof Reader)
/*      */       {
/* 2339 */         ((Reader)o).close();
/*      */       }
/* 2341 */       else if (o instanceof Writer)
/*      */       {
/* 2343 */         ((Writer)o).close();
/*      */       }
/*      */       else
/*      */       {
/* 2347 */         unknownObject = true;
/*      */       }
/*      */ 
/* 2350 */       if (o instanceof IdcReleasable)
/*      */       {
/* 2352 */         ((IdcReleasable)o).release();
/* 2353 */         unknownObject = false;
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2358 */       if (throwException)
/*      */       {
/* 2360 */         throw e;
/*      */       }
/* 2362 */       Report.trace("system", null, e);
/*      */     }
/*      */ 
/* 2365 */     if (!unknownObject)
/*      */       return;
/* 2367 */     String msg = LocaleUtils.encodeMessage("csFileUtilsCloseObjectUnknownObject", null, o.getClass().getName());
/*      */ 
/* 2369 */     IOException e = new IOException(msg);
/* 2370 */     Report.trace("system", null, e);
/* 2371 */     throw e;
/*      */   }
/*      */ 
/*      */   public static void closeObject(Object o)
/*      */   {
/*      */     try
/*      */     {
/* 2379 */       closeObjectEx(o);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2383 */       Report.trace("system", null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void closeObjectsEx(Object o1, Object o2)
/*      */     throws IOException
/*      */   {
/* 2397 */     IOException exception = null;
/*      */     try
/*      */     {
/* 2400 */       closeObjectEx(o2);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2404 */       exception = e;
/*      */     }
/*      */ 
/* 2407 */     closeObjectEx(o1);
/* 2408 */     if (exception == null)
/*      */       return;
/* 2410 */     throw exception;
/*      */   }
/*      */ 
/*      */   public static void closeObjects(Object o1, Object o2)
/*      */   {
/* 2416 */     closeObject(o1);
/* 2417 */     closeObject(o2);
/*      */   }
/*      */ 
/*      */   public static void closeObjects(Object o1, Object o2, Object o3)
/*      */   {
/* 2422 */     closeObject(o1);
/* 2423 */     closeObject(o2);
/* 2424 */     closeObject(o3);
/*      */   }
/*      */ 
/*      */   public static void closeReader(Reader r)
/*      */   {
/* 2429 */     closeObject(r);
/*      */   }
/*      */ 
/*      */   public static long touchFile(String fileName)
/*      */   {
/*      */     try
/*      */     {
/* 2436 */       File file = FileUtilsCfgBuilder.getCfgFile(fileName, null, false);
/* 2437 */       Writer bw = FileUtilsCfgBuilder.getCfgWriter(file);
/* 2438 */       bw.write("1");
/* 2439 */       bw.close();
/* 2440 */       return file.lastModified();
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2445 */       Report.trace("fileaccess", e, new IdcMessage("syFileUtilsTouchError", new Object[] { fileName }));
/* 2446 */     }return 0L;
/*      */   }
/*      */ 
/*      */   public static void writeFile(String data, File file, String encoding, int flags, String errorMessage)
/*      */     throws ServiceException
/*      */   {
/* 2477 */     IdcMessage msg = IdcMessageFactory.lc();
/* 2478 */     msg.m_msgEncoded = errorMessage;
/* 2479 */     writeFile(data, file, encoding, flags, msg);
/*      */   }
/*      */ 
/*      */   public static void writeFile(String data, File file, String encoding, int flags, IdcMessage errorMessage)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 2488 */       writeFileRaw(data, file, encoding, flags);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2492 */       throw new ServiceException(e, errorMessage);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void writeFileRaw(String data, File file, String encoding, int flags)
/*      */     throws IOException
/*      */   {
/* 2522 */     OutputStream out = FileUtilsCfgBuilder.getCfgOutputStream(file);
/* 2523 */     BufferedWriter writer = openDataWriterEx(out, encoding, flags);
/*      */     try
/*      */     {
/* 2526 */       writer.write(data);
/*      */     }
/*      */     finally
/*      */     {
/* 2530 */       closeObject(writer);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static boolean setLastModified(String fileName, long ts)
/*      */     throws ServiceException
/*      */   {
/* 2537 */     File file = new File(fileName);
/*      */     try
/*      */     {
/* 2540 */       Object[] args = { new Long(ts) };
/* 2541 */       ClassHelperUtils.executeMethod(file, "setLastModified", args, new Class[] { Long.TYPE });
/*      */     }
/*      */     catch (Throwable ignore)
/*      */     {
/* 2545 */       if (SystemUtils.m_verbose)
/*      */       {
/* 2547 */         Report.debug("system", null, ignore);
/*      */       }
/*      */     }
/*      */ 
/* 2551 */     if (loadNativeOsUtils());
/* 2556 */     return false;
/*      */   }
/*      */ 
/*      */   public static boolean loadNativeOsUtils()
/*      */   {
/* 2561 */     if (m_utils != null)
/*      */     {
/* 2563 */       return true;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2568 */       m_utils = new NativeOsUtils();
/*      */     }
/*      */     catch (Throwable ignore)
/*      */     {
/* 2572 */       if (SystemUtils.m_verbose)
/*      */       {
/* 2574 */         Report.debug(null, null, ignore);
/*      */       }
/*      */     }
/*      */ 
/* 2578 */     return m_utils != null;
/*      */   }
/*      */ 
/*      */   public static boolean doesPathContainRelativeSegments(String path)
/*      */   {
/* 2587 */     int length = path.length();
/* 2588 */     int index = -1;
/* 2589 */     while ((index = path.indexOf(46, index + 1)) >= 0)
/*      */     {
/* 2592 */       if (index > 0)
/*      */       {
/* 2594 */         char ch = path.charAt(index - 1);
/* 2595 */         if ((ch != '/') && (ch != '\\')) {
/*      */           continue;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2601 */       if ((index + 1 < length) && (path.charAt(index + 1) == '.'))
/*      */       {
/* 2603 */         ++index;
/*      */       }
/*      */ 
/* 2606 */       if (index + 1 < length)
/*      */       {
/* 2608 */         char ch = path.charAt(index + 1);
/* 2609 */         if ((ch != '/') && (ch != '\\'))
/*      */         {
/*      */           break label101;
/*      */         }
/*      */ 
/* 2615 */         label101: return true;
/*      */       }
/*      */     }
/* 2617 */     return false;
/*      */   }
/*      */ 
/*      */   public static String getAbsolutePath(String path)
/*      */   {
/* 2622 */     return getAbsolutePath(null, path);
/*      */   }
/*      */ 
/*      */   public static String getAbsolutePath(String dir, String path)
/*      */   {
/* 2627 */     if ((path == null) || (path.length() == 0))
/*      */     {
/* 2629 */       return null;
/*      */     }
/*      */ 
/* 2632 */     path = fileSlashes(path);
/* 2633 */     if ((dir == null) || (dir.length() == 0))
/*      */     {
/* 2635 */       dir = getWorkingDir();
/*      */     }
/*      */     else
/*      */     {
/* 2639 */       dir = directorySlashes(dir);
/*      */     }
/*      */ 
/* 2642 */     String serverDir = "";
/* 2643 */     if ((dir.length() > 1) && (Character.isLetter(dir.charAt(0))) && (dir.charAt(1) == ':'))
/*      */     {
/* 2646 */       serverDir = dir.substring(0, 3);
/*      */     }
/* 2648 */     else if (dir.startsWith("//"))
/*      */     {
/* 2650 */       int index = dir.indexOf(47, 2);
/* 2651 */       if (index > 0)
/*      */       {
/* 2653 */         serverDir = dir.substring(0, index);
/*      */       }
/*      */       else
/*      */       {
/* 2657 */         serverDir = dir;
/*      */       }
/*      */     }
/*      */ 
/* 2661 */     if (isAbsolutePath(path))
/*      */     {
/* 2663 */       return path;
/*      */     }
/* 2665 */     if (path.charAt(0) == '/')
/*      */     {
/* 2667 */       return serverDir + path;
/*      */     }
/*      */ 
/* 2671 */     if (dir.endsWith("/"))
/*      */     {
/* 2673 */       return dir + path;
/*      */     }
/* 2675 */     return dir + "/" + path;
/*      */   }
/*      */ 
/*      */   public static boolean isAbsolutePath(String path)
/*      */   {
/* 2687 */     if (path == null)
/*      */     {
/* 2689 */       return false;
/*      */     }
/* 2691 */     return ((path.length() > 1) && (Character.isLetter(path.charAt(0))) && (path.charAt(1) == ':')) || (path.startsWith("/")) || (path.startsWith("\\\\"));
/*      */   }
/*      */ 
/*      */   public static String getDirectory(String filename)
/*      */   {
/* 2698 */     String tempName = filename;
/* 2699 */     if ((filename.endsWith("/")) || (filename.endsWith("\\")))
/*      */     {
/* 2701 */       tempName = filename.substring(0, filename.length() - 1);
/* 2702 */       return tempName;
/*      */     }
/*      */ 
/* 2705 */     if (storeInDB(filename))
/*      */     {
/* 2707 */       return FileUtilsCfgBuilder.getCfgDirectory(filename);
/*      */     }
/*      */ 
/* 2710 */     File file = new File(tempName);
/* 2711 */     if (file.isDirectory())
/*      */     {
/* 2713 */       return tempName;
/*      */     }
/*      */ 
/* 2716 */     int index = tempName.lastIndexOf(47);
/* 2717 */     if (index < 0)
/*      */     {
/* 2721 */       return file.getParent();
/*      */     }
/*      */ 
/* 2724 */     return tempName.substring(0, index);
/*      */   }
/*      */ 
/*      */   public static String getParent(String filename)
/*      */   {
/* 2730 */     String tempName = filename;
/* 2731 */     if ((filename.endsWith("/")) || (filename.endsWith("\\")))
/*      */     {
/* 2733 */       tempName = filename.substring(0, filename.length() - 1);
/*      */     }
/*      */ 
/* 2736 */     if (storeInDB(filename))
/*      */     {
/* 2738 */       return FileUtilsCfgBuilder.getCfgParent(filename);
/*      */     }
/*      */ 
/* 2741 */     String parent = null;
/* 2742 */     File file = new File(tempName);
/* 2743 */     if (file.isDirectory())
/*      */     {
/* 2745 */       if (!tempName.startsWith("\\\\"))
/*      */       {
/* 2747 */         parent = file.getParent();
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/* 2752 */       parent = getDirectory(tempName);
/*      */     }
/*      */ 
/* 2755 */     if (parent == null)
/*      */     {
/* 2757 */       int index = tempName.lastIndexOf(47);
/* 2758 */       if (index > 0)
/*      */       {
/* 2760 */         parent = tempName.substring(0, index);
/* 2761 */         if (tempName.equals(parent + '/'))
/*      */         {
/* 2763 */           index = parent.lastIndexOf(47);
/* 2764 */           if (index > 0)
/*      */           {
/* 2766 */             parent = tempName.substring(0, index);
/*      */           }
/*      */           else
/*      */           {
/* 2770 */             parent = null;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2776 */     return parent;
/*      */   }
/*      */ 
/*      */   public static String getName(String path)
/*      */   {
/* 2788 */     int index = path.lastIndexOf(47);
/* 2789 */     if (index < 0)
/*      */     {
/* 2793 */       File file = FileUtilsCfgBuilder.getCfgFile(path, null);
/* 2794 */       return file.getName();
/*      */     }
/*      */ 
/* 2797 */     return path.substring(index + 1);
/*      */   }
/*      */ 
/*      */   public static String getRootName(String filename)
/*      */   {
/* 2802 */     int startIndex = filename.lastIndexOf(47);
/* 2803 */     if (startIndex < 0)
/*      */     {
/* 2807 */       File file = new File(filename);
/* 2808 */       filename = file.getName();
/* 2809 */       startIndex = 0;
/*      */     }
/*      */     else
/*      */     {
/* 2813 */       ++startIndex;
/*      */     }
/* 2815 */     int endIndex = filename.lastIndexOf(46);
/* 2816 */     if (endIndex < 0)
/*      */     {
/* 2818 */       endIndex = filename.length();
/*      */     }
/* 2820 */     return filename.substring(startIndex, endIndex);
/*      */   }
/*      */ 
/*      */   public static String getWorkingDir()
/*      */   {
/* 2825 */     return directorySlashes(System.getProperty("user.dir"));
/*      */   }
/*      */ 
/*      */   public static String getExtension(String path)
/*      */   {
/* 2830 */     return getExtension(path, false);
/*      */   }
/*      */ 
/*      */   public static String getExtension(String path, boolean withDot)
/*      */   {
/* 2835 */     String ext = "";
/* 2836 */     int index = path.lastIndexOf(46);
/* 2837 */     if (index >= 0)
/*      */     {
/* 2839 */       ext = path.substring(index + ((withDot) ? 0 : 1));
/*      */     }
/*      */ 
/* 2842 */     return ext;
/*      */   }
/*      */ 
/*      */   public static Object createBufferForStreaming(int size, int type)
/*      */   {
/* 2854 */     Object o = null;
/* 2855 */     if (size <= 0)
/*      */     {
/* 2857 */       if (m_defaultBufferPool != null)
/*      */       {
/* 2860 */         size = 16384;
/*      */       }
/*      */       else
/*      */       {
/* 2864 */         size = 1024;
/*      */       }
/*      */     }
/* 2867 */     if (m_defaultBufferPool != null)
/*      */     {
/* 2869 */       o = m_defaultBufferPool.getBuffer(size, type);
/*      */     }
/* 2873 */     else if (type == 0)
/*      */     {
/* 2875 */       o = new byte[size];
/*      */     }
/*      */     else
/*      */     {
/* 2879 */       o = new char[size];
/*      */     }
/*      */ 
/* 2882 */     return o;
/*      */   }
/*      */ 
/*      */   public static void releaseBufferForStreaming(Object buf)
/*      */   {
/* 2887 */     if ((m_defaultBufferPool == null) || (buf == null))
/*      */       return;
/* 2889 */     m_defaultBufferPool.releaseBuffer(buf);
/*      */   }
/*      */ 
/*      */   public static String loadFile(String path, String type, String[] encoding)
/*      */     throws IOException
/*      */   {
/* 2895 */     InputStream is = null;
/*      */     try
/*      */     {
/* 2898 */       is = FileUtilsCfgBuilder.getCfgInputStream(path);
/* 2899 */       String str = loadFile(is, type, encoding);
/*      */ 
/* 2903 */       return str; } finally { closeObject(is); }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static String loadFile(InputStream is, String type, String[] encoding)
/*      */     throws IOException
/*      */   {
/* 2910 */     if (encoding == null)
/*      */     {
/* 2912 */       encoding = new String[] { null };
/*      */     }
/* 2914 */     byte[] bytes = (byte[])(byte[])createBufferForStreaming(0, 0);
/* 2915 */     int nread = 0;
/* 2916 */     ByteArrayOutputStream output = new ByteArrayOutputStream();
/* 2917 */     while ((nread = is.read(bytes)) > 0)
/*      */     {
/* 2919 */       output.write(bytes, 0, nread);
/*      */     }
/* 2921 */     releaseBufferForStreaming(bytes);
/* 2922 */     byte[] byteArray = output.toByteArray();
/* 2923 */     if (byteArray.length == 0)
/*      */     {
/* 2925 */       return "";
/*      */     }
/*      */ 
/* 2929 */     String temp = checkForUnicodeEncoding(byteArray, 0, byteArray.length);
/* 2930 */     if (temp != null)
/*      */     {
/* 2932 */       encoding[0] = temp;
/*      */     }
/* 2934 */     else if (type != null)
/*      */     {
/* 2936 */       int lengthToRead = byteArray.length;
/* 2937 */       if (lengthToRead > 16384)
/*      */       {
/* 2939 */         lengthToRead = 16384;
/*      */       }
/*      */ 
/* 2941 */       char[] charArray = (char[])(char[])createBufferForStreaming(lengthToRead + 1, 1);
/*      */       int textLen;
/*      */       String textString;
/*      */       try
/*      */       {
/* 2947 */         for (int i = 0; i < lengthToRead; ++i)
/*      */         {
/* 2949 */           charArray[i] = (char)(byteArray[i] % 255);
/* 2950 */           if (charArray[i] == '\n') {
/*      */             break;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 2956 */         textLen = i;
/* 2957 */         textString = new String(charArray, 0, i);
/*      */       }
/*      */       finally
/*      */       {
/* 2961 */         releaseBufferForStreaming(charArray);
/*      */       }
/*      */ 
/* 2964 */       charArray = null;
/* 2965 */       int startIndex = textString.indexOf("<?" + type);
/* 2966 */       int endIndex = textString.indexOf("?>");
/* 2967 */       if ((startIndex >= 0) && (endIndex > startIndex))
/*      */       {
/* 2969 */         String charsetKey = "jcharset=";
/* 2970 */         int startCharsetIndex = textString.indexOf(charsetKey, startIndex);
/* 2971 */         if (startCharsetIndex >= 0)
/*      */         {
/* 2974 */           startCharsetIndex += charsetKey.length();
/* 2975 */           int endCharsetIndex = textString.indexOf(32, startCharsetIndex);
/* 2976 */           if (endCharsetIndex < 0)
/*      */           {
/* 2978 */             endCharsetIndex = textString.indexOf(63, startCharsetIndex);
/*      */           }
/* 2980 */           if (endCharsetIndex > 0)
/*      */           {
/* 2982 */             if (startCharsetIndex + 1 < textLen)
/*      */             {
/* 2984 */               char ch = textString.charAt(startCharsetIndex);
/* 2985 */               if ((ch == '"') || (ch == '\''))
/*      */               {
/* 2987 */                 ++startCharsetIndex;
/*      */               }
/*      */             }
/* 2990 */             char ch = textString.charAt(endCharsetIndex - 1);
/* 2991 */             if ((ch == '"') || (ch == '\''))
/*      */             {
/* 2993 */               --endCharsetIndex;
/*      */             }
/* 2995 */             if (endCharsetIndex > startCharsetIndex)
/*      */             {
/* 2997 */               encoding[0] = textString.substring(startCharsetIndex, endCharsetIndex).trim();
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 3004 */     if (encoding[0] == null)
/*      */     {
/* 3006 */       String fileText = new String(byteArray, m_javaSystemEncoding);
/* 3007 */       return fileText;
/*      */     }
/* 3009 */     encoding[0] = LocaleResources.getEncodingFromAlias(encoding[0]);
/* 3010 */     if ((encoding[0] == null) || (encoding[0].length() == 0))
/*      */     {
/* 3012 */       encoding[0] = "iso-8859-1";
/*      */     }
/* 3014 */     String fileText = new String(byteArray, encoding[0]);
/* 3015 */     return fileText;
/*      */   }
/*      */ 
/*      */   public static String checkForASCIIEncoding(byte[] buf, int start, int len)
/*      */   {
/* 3025 */     while (start < len)
/*      */     {
/* 3027 */       byte b = buf[(start++)];
/*      */ 
/* 3030 */       if (b <= 0)
/*      */       {
/* 3032 */         return null;
/*      */       }
/*      */     }
/* 3035 */     return "ASCII";
/*      */   }
/*      */ 
/*      */   public static String checkForUnicodeEncoding(byte[] buf, int start, int len)
/*      */   {
/* 3041 */     if (buf.length < 4)
/*      */     {
/* 3043 */       byte[] tmpBuf = new byte[4];
/* 3044 */       for (int i = 0; i < buf.length; ++i)
/*      */       {
/* 3046 */         tmpBuf[i] = buf[i];
/*      */       }
/* 3048 */       buf = tmpBuf;
/*      */     }
/* 3050 */     if ((buf.length >= start + len) && (len >= 4))
/*      */     {
/* 3052 */       int startFourBytes = buf[start] & 0xFF;
/* 3053 */       startFourBytes <<= 8;
/* 3054 */       startFourBytes |= buf[(start + 1)] & 0xFF;
/* 3055 */       startFourBytes <<= 8;
/* 3056 */       startFourBytes |= buf[(start + 2)] & 0xFF;
/* 3057 */       startFourBytes <<= 8;
/* 3058 */       startFourBytes |= buf[(start + 3)] & 0xFF;
/*      */     }
/*      */     else
/*      */     {
/* 3062 */       return null;
/*      */     }
/*      */     int startFourBytes;
/* 3065 */     if ((startFourBytes & 0xFEFF0000) == -16842752)
/*      */     {
/* 3067 */       return "UnicodeBig";
/*      */     }
/* 3069 */     if ((startFourBytes & 0xFFFE0000) == -131072)
/*      */     {
/* 3071 */       return "UnicodeLittle";
/*      */     }
/* 3073 */     if ((startFourBytes & 0xEFBBBF00) == -272908544)
/*      */     {
/* 3075 */       return "UTF8";
/*      */     }
/* 3077 */     if (((startFourBytes & 0xFF00FF) == 0) && ((startFourBytes & 0xFF00FF00) != 0))
/*      */     {
/* 3080 */       return "UnicodeLittle";
/*      */     }
/* 3082 */     if (((startFourBytes & 0xFF00FF00) == 0) && ((startFourBytes & 0xFF00FF) != 0))
/*      */     {
/* 3085 */       return "UnicodeBig";
/*      */     }
/* 3087 */     return null;
/*      */   }
/*      */ 
/*      */   public static String loadProperties(Properties props, InputStream is)
/*      */     throws IOException
/*      */   {
/* 3093 */     return loadPropertiesEx(props, is, null);
/*      */   }
/*      */ 
/*      */   public static String loadPropertiesEx(Properties props, InputStream is, String encoding)
/*      */     throws IOException
/*      */   {
/* 3099 */     String[] enc = { encoding };
/* 3100 */     String textString = loadFile(is, "cfg", enc);
/* 3101 */     Vector lines = StringUtils.parseArray(textString, '\n', '\n');
/*      */ 
/* 3105 */     int num = lines.size();
/* 3106 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 3108 */       String line = ((String)lines.elementAt(i)).trim();
/* 3109 */       int eqIndx = line.indexOf("=");
/* 3110 */       int colonIndex = line.indexOf(": ");
/*      */ 
/* 3113 */       if (i == 0)
/*      */       {
/* 3115 */         int cfgIndex = line.indexOf("<?cfg");
/* 3116 */         if ((cfgIndex >= 0) && (cfgIndex < eqIndx))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 3123 */       if ((eqIndx <= 0) && (colonIndex <= 0))
/*      */         continue;
/*      */       String value;
/*      */       String key;
/*      */       String value;
/* 3125 */       if (eqIndx > 0)
/*      */       {
/* 3127 */         String key = line.substring(0, eqIndx);
/* 3128 */         value = line.substring(eqIndx + 1, line.length());
/*      */       }
/*      */       else
/*      */       {
/* 3132 */         key = line.substring(0, colonIndex);
/* 3133 */         value = line.substring(colonIndex + 2);
/*      */       }
/*      */ 
/* 3136 */       props.put(key, StringUtils.decodeLiteralStringEscapeSequence(value));
/*      */     }
/*      */ 
/* 3139 */     return enc[0];
/*      */   }
/*      */ 
/*      */   public static void loadProperties(Properties props, String filePath) throws IOException
/*      */   {
/* 3144 */     InputStream fis = null;
/*      */     try
/*      */     {
/* 3147 */       fis = FileUtilsCfgBuilder.getCfgInputStream(filePath);
/* 3148 */       loadProperties(props, fis);
/*      */     }
/*      */     finally
/*      */     {
/* 3152 */       if (fis != null)
/*      */       {
/* 3154 */         fis.close();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void redirectOutput(String filename)
/*      */   {
/* 3161 */     NativeOsUtils utils = new NativeOsUtils();
/* 3162 */     int fd = utils.open(filename, NativeOsUtils.O_APPEND | NativeOsUtils.O_CREAT | NativeOsUtils.O_RDWR, 420);
/*      */ 
/* 3164 */     if (fd < 0)
/*      */     {
/* 3166 */       Report.trace(null, "Unable to open \"" + filename + "\": " + utils.getErrorMessage(utils.getErrorCode()), null);
/*      */     }
/*      */     else
/*      */     {
/* 3171 */       Report.trace(null, "Redirecting stdout and stderr to " + filename + ".", null);
/*      */ 
/* 3174 */       int rc = utils.dup2(fd, 1);
/* 3175 */       if (rc < 0)
/*      */       {
/* 3177 */         Report.trace(null, "Unable to redirect stdout: " + utils.getErrorMessage(utils.getErrorCode()), null);
/*      */       }
/*      */ 
/* 3180 */       rc = utils.dup2(fd, 2);
/* 3181 */       if (rc < 0)
/*      */       {
/* 3183 */         Report.trace(null, "Unable to redirect stderr: " + utils.getErrorMessage(utils.getErrorCode()), null);
/*      */       }
/*      */ 
/* 3186 */       utils.close(fd);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static boolean filesAreDifferent(String file1, String file2)
/*      */     throws IOException, ServiceException
/*      */   {
/* 3193 */     FileInputStream input1 = null;
/* 3194 */     FileInputStream input2 = null;
/*      */     try
/*      */     {
/* 3198 */       input1 = new FileInputStream(file1);
/*      */     }
/*      */     catch (FileNotFoundException ignore)
/*      */     {
/* 3202 */       if (SystemUtils.m_verbose)
/*      */       {
/* 3204 */         Report.debug("system", null, ignore);
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 3210 */       input2 = new FileInputStream(file2);
/*      */     }
/*      */     catch (FileNotFoundException ignore)
/*      */     {
/* 3214 */       if (SystemUtils.m_verbose)
/*      */       {
/* 3216 */         Report.debug("system", null, ignore);
/*      */       }
/*      */     }
/*      */ 
/* 3220 */     if ((input1 == null) && (input2 == null))
/*      */     {
/* 3222 */       String msg = LocaleUtils.encodeMessage("csFileUtilsDiffBothMissing", null, file1, file2);
/*      */ 
/* 3225 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/* 3228 */     boolean result = false;
/* 3229 */     byte[] buf1 = null;
/* 3230 */     byte[] buf2 = null;
/* 3231 */     FileChannel channel1 = null;
/* 3232 */     FileChannel channel2 = null;
/*      */     try
/*      */     {
/* 3235 */       if ((input1 == null) || (input2 == null))
/*      */       {
/* 3237 */         int i = 1;
/*      */         return i;
/*      */       }
/* 3239 */       if ((m_useMappedDiff) && (input1.available() < 2147483648L) && (input2.available() < 2147483648L))
/*      */       {
/* 3244 */         channel1 = input1.getChannel();
/* 3245 */         channel2 = input2.getChannel();
/* 3246 */         long length1 = channel1.size();
/* 3247 */         long length2 = channel2.size();
/* 3248 */         if (length1 != length2)
/*      */         {
/* 3250 */           int j = 1;
/*      */           return j;
/*      */         }
/* 3252 */         if (length1 > 16384L)
/*      */         {
/* 3254 */           if (SystemUtils.m_verbose)
/*      */           {
/* 3256 */             trace("Doing mapped file comparision of (file: " + file1 + " length: " + length1 + ") to (file: " + file2 + " length: " + length2 + ")");
/*      */           }
/*      */ 
/* 3260 */           MappedByteBuffer map1 = channel1.map(FileChannel.MapMode.READ_ONLY, 0L, length1);
/*      */ 
/* 3262 */           MappedByteBuffer map2 = channel2.map(FileChannel.MapMode.READ_ONLY, 0L, length2);
/*      */ 
/* 3264 */           int rc = map1.compareTo(map2);
/*      */ 
/* 3266 */           map1 = null;
/* 3267 */           map2 = null;
/*      */ 
/* 3274 */           System.gc();
/* 3275 */           int k = (rc != 0) ? 1 : 0;
/*      */           return k;
/*      */         }
/*      */       }
/* 3279 */       buf1 = (byte[])(byte[])createBufferForStreaming(0, 0);
/* 3280 */       buf2 = (byte[])(byte[])createBufferForStreaming(0, 0);
/*      */ 
/* 3284 */       while ((count1 = input1.read(buf1)) > 0)
/*      */       {
/*      */         int count1;
/* 3286 */         int count2 = 0;
/* 3287 */         int tmpCount = 0;
/* 3288 */         while ((tmpCount >= 0) && (count2 < count1))
/*      */         {
/* 3290 */           tmpCount = input2.read(buf2, count2, buf2.length - count2);
/* 3291 */           if (tmpCount == -1)
/*      */           {
/* 3293 */             result = true;
/* 3294 */             break;
/*      */           }
/*      */ 
/* 3297 */           count2 += tmpCount;
/*      */         }
/* 3299 */         if (result) {
/*      */           break;
/*      */         }
/*      */ 
/* 3303 */         if (count1 != buf1.length)
/*      */         {
/* 3305 */           Arrays.fill(buf1, count1, buf1.length - 1, 0);
/* 3306 */           Arrays.fill(buf2, count1, buf1.length - 1, 0);
/*      */         }
/* 3308 */         if (!Arrays.equals(buf1, buf2))
/*      */         {
/* 3310 */           result = true;
/* 3311 */           break;
/*      */         }
/* 3313 */         if (buf1.length < 16384)
/*      */         {
/* 3315 */           buf1 = new byte[16384];
/* 3316 */           buf2 = new byte[16384];
/*      */         }
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 3322 */       releaseBufferForStreaming(buf1);
/* 3323 */       releaseBufferForStreaming(buf2);
/* 3324 */       closeObjects(channel1, channel2);
/* 3325 */       closeObjects(input1, input2);
/*      */     }
/* 3327 */     return result;
/*      */   }
/*      */ 
/*      */   public static String windowsSlashes(String path)
/*      */   {
/* 3339 */     path = fileSlashes(path);
/* 3340 */     if (path.endsWith("/"))
/*      */     {
/* 3342 */       path = path.substring(0, path.length() - 1);
/*      */     }
/* 3344 */     path = removeParentDirReferences(path);
/* 3345 */     path = path.replace('/', '\\');
/* 3346 */     return path;
/*      */   }
/*      */ 
/*      */   public static String removeParentDirReferences(String path)
/*      */   {
/* 3354 */     while ((index = path.indexOf("/../")) >= 0)
/*      */     {
/*      */       int index;
/* 3356 */       int priorIndex = path.substring(0, index).lastIndexOf("/");
/* 3357 */       if (priorIndex >= 0)
/*      */       {
/* 3359 */         path = path.substring(0, priorIndex) + path.substring(index + 3);
/*      */       }
/*      */       else
/*      */       {
/* 3363 */         path = path.substring(index + 3);
/*      */       }
/*      */     }
/* 3366 */     return path;
/*      */   }
/*      */ 
/*      */   public static String makeSafeDirectoryForNtfs(String dir)
/*      */   {
/* 3374 */     IdcStringBuilder buf = null;
/*      */ 
/* 3377 */     int startIndex = 0;
/* 3378 */     while (startIndex < dir.length())
/*      */     {
/* 3380 */       if (dir.charAt(startIndex) != '.') {
/*      */         break;
/*      */       }
/*      */ 
/* 3384 */       ++startIndex;
/*      */     }
/* 3386 */     if (startIndex > 0)
/*      */     {
/* 3388 */       buf = new IdcStringBuilder();
/* 3389 */       for (int i = 0; i < startIndex; ++i)
/*      */       {
/* 3391 */         buf.append("%2e");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3396 */     int endIndex = dir.length() - 1;
/* 3397 */     if (endIndex >= startIndex)
/*      */     {
/* 3399 */       while (endIndex >= startIndex)
/*      */       {
/* 3401 */         if (dir.charAt(endIndex) != '.') {
/*      */           break;
/*      */         }
/*      */ 
/* 3405 */         --endIndex;
/*      */       }
/*      */ 
/* 3409 */       boolean hasEnd = endIndex < dir.length() - 1;
/* 3410 */       if ((buf != null) || (hasEnd))
/*      */       {
/* 3412 */         if (buf == null)
/*      */         {
/* 3414 */           buf = new IdcStringBuilder();
/*      */         }
/* 3416 */         buf.append(dir.substring(startIndex, endIndex + 1));
/* 3417 */         if (hasEnd)
/*      */         {
/* 3419 */           for (int i = endIndex + 1; i < dir.length(); ++i)
/*      */           {
/* 3421 */             buf.append("%2e");
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3428 */     String retVal = dir;
/* 3429 */     if (buf != null)
/*      */     {
/* 3431 */       retVal = buf.toString();
/*      */     }
/*      */ 
/* 3436 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static void appendToWriter(IdcAppender appender, Writer w)
/*      */     throws IOException
/*      */   {
/* 3442 */     IdcAppendableWriter writer = new IdcAppendableWriter(w);
/* 3443 */     appender.appendTo(writer);
/* 3444 */     writer.checkForException();
/*      */   }
/*      */ 
/*      */   public static String computePathFromSubstitutionMap(Map keyMap, String pathname)
/*      */     throws ServiceException
/*      */   {
/* 3457 */     return computePathFromSubstitutionMapWithFlags(keyMap, pathname, 13);
/*      */   }
/*      */ 
/*      */   public static String computePathFromSubstitutionMapWithFlags(Map keyMap, String pathname, int flags)
/*      */     throws ServiceException
/*      */   {
/* 3481 */     IdcStringBuilder precomputed = new IdcStringBuilder(pathname);
/*      */ 
/* 3483 */     int dollarIndex = precomputed.indexOf(0, '$');
/*      */ 
/* 3485 */     if (dollarIndex > 0)
/*      */     {
/* 3487 */       String msg = LocaleUtils.encodeMessage("syFileUtilsDirKeySubstituteNotPrefix", null, pathname);
/* 3488 */       throw new ServiceException(msg);
/*      */     }
/*      */     IdcStringBuilder computed;
/*      */     IdcStringBuilder computed;
/* 3490 */     if (dollarIndex < 0)
/*      */     {
/* 3492 */       computed = precomputed;
/*      */     }
/*      */     else
/*      */     {
/* 3497 */       if (precomputed.indexOf(dollarIndex + 1, '$') >= 0)
/*      */       {
/* 3499 */         String msg = LocaleUtils.encodeMessage("syFileUtilsDirKeySubstituteNotPrefix", null, pathname);
/* 3500 */         throw new ServiceException(msg);
/*      */       }
/* 3502 */       computed = new IdcStringBuilder(precomputed.m_length);
/* 3503 */       computed.m_disableToStringReleaseBuffers = true;
/* 3504 */       int slashIndex = precomputed.indexOf(dollarIndex, '/');
/* 3505 */       if (slashIndex < 1)
/*      */       {
/* 3507 */         slashIndex = precomputed.m_length;
/*      */       }
/* 3509 */       String key = (String)precomputed.subSequence(dollarIndex + 1, slashIndex);
/*      */       String value;
/*      */       String value;
/* 3511 */       if (keyMap instanceof Properties)
/*      */       {
/* 3513 */         value = ((Properties)keyMap).getProperty(key);
/*      */       }
/*      */       else
/*      */       {
/* 3517 */         value = (String)keyMap.get(key);
/*      */       }
/* 3519 */       if (null == value)
/*      */       {
/* 3522 */         throw new ServiceException(null, -27, "syMissingArgument2", new Object[] { key, "dirKeys" });
/*      */       }
/*      */ 
/* 3525 */       computed.append(value);
/* 3526 */       if (slashIndex < precomputed.m_length)
/*      */       {
/* 3529 */         computed.append(precomputed, slashIndex, precomputed.m_length - slashIndex);
/*      */       }
/*      */     }
/*      */ 
/* 3533 */     String result = fixDirectorySlashes(computed, flags).toString();
/* 3534 */     precomputed.releaseBuffers();
/* 3535 */     computed.releaseBuffers();
/* 3536 */     return result;
/*      */   }
/*      */ 
/*      */   public static void computeAndCaptureOsDirValues(String prefix, Map in, Map substitutionMap, List<String> capturedValuesList, int flags)
/*      */     throws ServiceException
/*      */   {
/* 3551 */     Set keys = in.keySet();
/* 3552 */     for (Iterator i$ = keys.iterator(); i$.hasNext(); ) { Object key = i$.next();
/*      */ 
/* 3554 */       if (key instanceof String)
/*      */       {
/* 3556 */         String stringKey = (String)key;
/* 3557 */         if (!stringKey.startsWith(prefix)) {
/*      */           continue;
/*      */         }
/*      */ 
/* 3561 */         Object objVal = in.get(stringKey);
/* 3562 */         if (!objVal instanceof String) {
/*      */           continue;
/*      */         }
/*      */ 
/* 3566 */         String value = (String)objVal;
/*      */         try
/*      */         {
/* 3569 */           value = computePathFromSubstitutionMapWithFlags(substitutionMap, value, flags);
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/* 3573 */           if (e.m_errorCode == -27)
/*      */           {
/* 3576 */             Report.trace("fileaccess", null, e);
/*      */           }
/*      */ 
/* 3579 */           throw e;
/*      */         }
/* 3581 */         if (capturedValuesList.contains(value)) {
/*      */           continue;
/*      */         }
/*      */ 
/* 3585 */         boolean isDir = (flags & 0x40) != 0;
/* 3586 */         if (checkFile(value, !isDir, false) == 0)
/*      */         {
/* 3588 */           capturedValuesList.add(value);
/*      */         }
/*      */         else
/*      */         {
/* 3592 */           Report.trace("fileaccess", "not adding non-existent directory " + value, null);
/*      */         }
/*      */       } }
/*      */ 
/*      */   }
/*      */ 
/*      */   public static void addPathSubstitutionMappings(Map keyMap, String[][] keyPathPairs)
/*      */     throws ServiceException
/*      */   {
/* 3619 */     int numPairs = keyPathPairs.length;
/*      */ 
/* 3621 */     for (int i = 0; i < numPairs; ++i)
/*      */     {
/* 3623 */       String key = keyPathPairs[i][0];
/* 3624 */       String path = keyPathPairs[i][1];
/* 3625 */       String pathSubst = computePathFromSubstitutionMap(keyMap, path);
/* 3626 */       keyMap.put(key, pathSubst);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void copyReaderToWriter(Reader reader, Writer writer)
/*      */     throws IOException
/*      */   {
/* 3638 */     char[] buf = null;
/*      */     try
/*      */     {
/* 3641 */       buf = (char[])(char[])createBufferForStreaming(0, 1);
/*      */ 
/* 3645 */       while ((count = reader.read(buf)) > 0)
/*      */       {
/*      */         int count;
/* 3647 */         writer.write(buf, 0, count);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 3652 */       releaseBufferForStreaming(buf);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static boolean storeInDB(String path)
/*      */   {
/* 3661 */     return m_cfgDescriptorFactory.storeInDB(path);
/*      */   }
/*      */ 
/*      */   public static void trace(String msg)
/*      */   {
/* 3666 */     Report.trace("fileaccess", msg, null);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 3671 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103663 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FileUtils
 * JD-Core Version:    0.5.4
 */