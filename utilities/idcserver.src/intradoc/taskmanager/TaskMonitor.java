/*     */ package intradoc.taskmanager;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.HashSet;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TaskMonitor
/*     */ {
/*  31 */   public static String m_defaultQueueName = "DefaultTasks";
/*  32 */   protected static boolean m_isInitialized = false;
/*  33 */   protected static Map<String, Vector> m_queues = new Hashtable();
/*  34 */   protected static Map<String, Set> m_launchers = new Hashtable();
/*     */ 
/*  38 */   protected static int m_counter = 0;
/*     */ 
/*  40 */   protected static boolean m_isBusy = false;
/*     */ 
/*  42 */   protected static String m_lockObject = "TaskMonitorLock";
/*     */ 
/*     */   public static TaskLauncher startProcessMonitor(String name, TaskInfo tinfo, Vector procQueue, String unique)
/*     */   {
/*  46 */     String uniqueStr = unique;
/*  47 */     Vector queue = procQueue;
/*  48 */     if (!m_isInitialized)
/*     */     {
/*  50 */       SystemUtils.registerSynchronizationObjectToNotifyOnStop(m_lockObject);
/*  51 */       m_isInitialized = true;
/*     */     }
/*  53 */     TaskLauncher launcher = new TaskLauncher(name, tinfo.m_needProcPersistance);
/*  54 */     int timeout = 1000 * tinfo.m_idleTimeout;
/*  55 */     Runnable run = new Object(launcher, queue, timeout, uniqueStr)
/*     */     {
/*     */       // ERROR //
/*     */       public void run()
/*     */       {
/*     */         // Byte code:
/*     */         //   0: ldc 6
/*     */         //   2: new 7	java/lang/StringBuilder
/*     */         //   5: dup
/*     */         //   6: invokespecial 8	java/lang/StringBuilder:<init>	()V
/*     */         //   9: ldc 9
/*     */         //   11: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   14: aload_0
/*     */         //   15: invokevirtual 11	java/lang/Object:toString	()Ljava/lang/String;
/*     */         //   18: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   21: ldc 12
/*     */         //   23: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   26: invokevirtual 13	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */         //   29: aconst_null
/*     */         //   30: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   33: iconst_0
/*     */         //   34: istore_1
/*     */         //   35: aload_0
/*     */         //   36: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   39: getfield 15	intradoc/taskmanager/TaskLauncher:m_abort	Z
/*     */         //   42: ifne +785 -> 827
/*     */         //   45: aload_0
/*     */         //   46: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   49: getfield 16	intradoc/taskmanager/TaskLauncher:m_failed	Z
/*     */         //   52: ifne +775 -> 827
/*     */         //   55: getstatic 17	intradoc/common/SystemUtils:m_isServerStopped	Z
/*     */         //   58: ifne +769 -> 827
/*     */         //   61: aconst_null
/*     */         //   62: astore_2
/*     */         //   63: aload_0
/*     */         //   64: getfield 2	intradoc/taskmanager/TaskMonitor$1:val$queue	Ljava/util/Vector;
/*     */         //   67: dup
/*     */         //   68: astore_3
/*     */         //   69: monitorenter
/*     */         //   70: aload_0
/*     */         //   71: getfield 2	intradoc/taskmanager/TaskMonitor$1:val$queue	Ljava/util/Vector;
/*     */         //   74: invokevirtual 18	java/util/Vector:size	()I
/*     */         //   77: ifle +66 -> 143
/*     */         //   80: aload_0
/*     */         //   81: getfield 2	intradoc/taskmanager/TaskMonitor$1:val$queue	Ljava/util/Vector;
/*     */         //   84: iconst_0
/*     */         //   85: invokevirtual 19	java/util/Vector:elementAt	(I)Ljava/lang/Object;
/*     */         //   88: checkcast 20	intradoc/taskmanager/TaskInfo
/*     */         //   91: astore_2
/*     */         //   92: aload_0
/*     */         //   93: getfield 2	intradoc/taskmanager/TaskMonitor$1:val$queue	Ljava/util/Vector;
/*     */         //   96: iconst_0
/*     */         //   97: invokevirtual 21	java/util/Vector:removeElementAt	(I)V
/*     */         //   100: ldc 6
/*     */         //   102: new 7	java/lang/StringBuilder
/*     */         //   105: dup
/*     */         //   106: invokespecial 8	java/lang/StringBuilder:<init>	()V
/*     */         //   109: ldc 9
/*     */         //   111: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   114: aload_0
/*     */         //   115: invokevirtual 11	java/lang/Object:toString	()Ljava/lang/String;
/*     */         //   118: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   121: ldc 22
/*     */         //   123: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   126: aload_2
/*     */         //   127: invokevirtual 23	intradoc/taskmanager/TaskInfo:toString	()Ljava/lang/String;
/*     */         //   130: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   133: invokevirtual 13	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */         //   136: aconst_null
/*     */         //   137: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   140: goto +124 -> 264
/*     */         //   143: invokestatic 24	java/lang/System:currentTimeMillis	()J
/*     */         //   146: lstore 4
/*     */         //   148: aload_0
/*     */         //   149: getfield 2	intradoc/taskmanager/TaskMonitor$1:val$queue	Ljava/util/Vector;
/*     */         //   152: aload_0
/*     */         //   153: getfield 3	intradoc/taskmanager/TaskMonitor$1:val$timeout	I
/*     */         //   156: i2l
/*     */         //   157: invokestatic 25	intradoc/common/SystemUtils:wait	(Ljava/lang/Object;J)V
/*     */         //   160: aload_0
/*     */         //   161: getfield 2	intradoc/taskmanager/TaskMonitor$1:val$queue	Ljava/util/Vector;
/*     */         //   164: invokevirtual 18	java/util/Vector:size	()I
/*     */         //   167: ifne +82 -> 249
/*     */         //   170: invokestatic 24	java/lang/System:currentTimeMillis	()J
/*     */         //   173: lstore 6
/*     */         //   175: lload 6
/*     */         //   177: lload 4
/*     */         //   179: aload_0
/*     */         //   180: getfield 3	intradoc/taskmanager/TaskMonitor$1:val$timeout	I
/*     */         //   183: i2l
/*     */         //   184: ladd
/*     */         //   185: lcmp
/*     */         //   186: iflt +58 -> 244
/*     */         //   189: ldc 6
/*     */         //   191: new 7	java/lang/StringBuilder
/*     */         //   194: dup
/*     */         //   195: invokespecial 8	java/lang/StringBuilder:<init>	()V
/*     */         //   198: ldc 26
/*     */         //   200: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   203: aload_0
/*     */         //   204: invokevirtual 11	java/lang/Object:toString	()Ljava/lang/String;
/*     */         //   207: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   210: ldc 27
/*     */         //   212: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   215: aload_0
/*     */         //   216: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   219: getfield 28	intradoc/taskmanager/TaskLauncher:m_name	Ljava/lang/String;
/*     */         //   222: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   225: ldc 29
/*     */         //   227: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   230: invokevirtual 13	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */         //   233: aconst_null
/*     */         //   234: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   237: iconst_1
/*     */         //   238: istore_1
/*     */         //   239: aload_3
/*     */         //   240: monitorexit
/*     */         //   241: goto +586 -> 827
/*     */         //   244: aload_3
/*     */         //   245: monitorexit
/*     */         //   246: goto -211 -> 35
/*     */         //   249: aload_3
/*     */         //   250: monitorexit
/*     */         //   251: goto -216 -> 35
/*     */         //   254: astore 4
/*     */         //   256: ldc 6
/*     */         //   258: aconst_null
/*     */         //   259: aload 4
/*     */         //   261: invokestatic 31	intradoc/common/Report:info	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   264: aload_3
/*     */         //   265: monitorexit
/*     */         //   266: goto +10 -> 276
/*     */         //   269: astore 8
/*     */         //   271: aload_3
/*     */         //   272: monitorexit
/*     */         //   273: aload 8
/*     */         //   275: athrow
/*     */         //   276: aload_0
/*     */         //   277: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   280: aload_2
/*     */         //   281: getfield 32	intradoc/taskmanager/TaskInfo:m_traceSubject	Ljava/lang/String;
/*     */         //   284: invokevirtual 33	intradoc/taskmanager/TaskLauncher:setTraceSubject	(Ljava/lang/String;)V
/*     */         //   287: aload_0
/*     */         //   288: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   291: getfield 34	intradoc/taskmanager/TaskLauncher:m_needPersistance	Z
/*     */         //   294: ifeq +221 -> 515
/*     */         //   297: ldc 6
/*     */         //   299: ldc 35
/*     */         //   301: aconst_null
/*     */         //   302: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   305: aload_2
/*     */         //   306: getfield 36	intradoc/taskmanager/TaskInfo:m_worker	Lintradoc/taskmanager/TaskWork;
/*     */         //   309: invokeinterface 37 1 0
/*     */         //   314: astore_3
/*     */         //   315: aload_0
/*     */         //   316: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   319: aload_3
/*     */         //   320: aload_2
/*     */         //   321: aload_0
/*     */         //   322: getfield 4	intradoc/taskmanager/TaskMonitor$1:val$uniqueStr	Ljava/lang/String;
/*     */         //   325: invokevirtual 38	intradoc/taskmanager/TaskLauncher:startExe	([Ljava/lang/String;Lintradoc/taskmanager/TaskInfo;Ljava/lang/String;)V
/*     */         //   328: aload_2
/*     */         //   329: getfield 39	intradoc/taskmanager/TaskInfo:m_cmdLine	Ljava/util/Vector;
/*     */         //   332: iconst_0
/*     */         //   333: invokevirtual 40	java/util/Vector:get	(I)Ljava/lang/Object;
/*     */         //   336: checkcast 41	java/lang/String
/*     */         //   339: astore 4
/*     */         //   341: new 42	java/util/Vector
/*     */         //   344: dup
/*     */         //   345: invokespecial 43	java/util/Vector:<init>	()V
/*     */         //   348: astore 5
/*     */         //   350: aload 5
/*     */         //   352: aload 4
/*     */         //   354: invokevirtual 44	java/util/Vector:add	(Ljava/lang/Object;)Z
/*     */         //   357: pop
/*     */         //   358: aload_2
/*     */         //   359: aload 5
/*     */         //   361: putfield 45	intradoc/taskmanager/TaskInfo:m_cmdLineOptions	Ljava/util/Vector;
/*     */         //   364: aload_0
/*     */         //   365: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   368: aload_2
/*     */         //   369: invokevirtual 46	intradoc/taskmanager/TaskLauncher:launchExe	(Lintradoc/taskmanager/TaskInfo;)V
/*     */         //   372: goto +140 -> 512
/*     */         //   375: astore 4
/*     */         //   377: ldc 6
/*     */         //   379: ldc 48
/*     */         //   381: aload 4
/*     */         //   383: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   386: aload_2
/*     */         //   387: dup
/*     */         //   388: getfield 49	intradoc/taskmanager/TaskInfo:m_attemptCount	I
/*     */         //   391: iconst_1
/*     */         //   392: iadd
/*     */         //   393: putfield 49	intradoc/taskmanager/TaskInfo:m_attemptCount	I
/*     */         //   396: aload_2
/*     */         //   397: getfield 49	intradoc/taskmanager/TaskInfo:m_attemptCount	I
/*     */         //   400: new 7	java/lang/StringBuilder
/*     */         //   403: dup
/*     */         //   404: invokespecial 8	java/lang/StringBuilder:<init>	()V
/*     */         //   407: aload_2
/*     */         //   408: getfield 50	intradoc/taskmanager/TaskInfo:m_name	Ljava/lang/String;
/*     */         //   411: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   414: ldc 51
/*     */         //   416: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   419: invokevirtual 13	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */         //   422: iconst_5
/*     */         //   423: invokestatic 52	intradoc/shared/SharedObjects:getEnvironmentInt	(Ljava/lang/String;I)I
/*     */         //   426: if_icmplt +74 -> 500
/*     */         //   429: aload_2
/*     */         //   430: aload 4
/*     */         //   432: invokevirtual 53	java/lang/Exception:getMessage	()Ljava/lang/String;
/*     */         //   435: putfield 54	intradoc/taskmanager/TaskInfo:m_errMsg	Ljava/lang/String;
/*     */         //   438: aload_2
/*     */         //   439: aload 4
/*     */         //   441: putfield 55	intradoc/taskmanager/TaskInfo:m_error	Ljava/lang/Throwable;
/*     */         //   444: aload_2
/*     */         //   445: iconst_1
/*     */         //   446: putfield 56	intradoc/taskmanager/TaskInfo:m_hasError	Z
/*     */         //   449: aload_2
/*     */         //   450: getstatic 57	intradoc/taskmanager/TaskInfo$STATUS:FAILURE	Lintradoc/taskmanager/TaskInfo$STATUS;
/*     */         //   453: putfield 58	intradoc/taskmanager/TaskInfo:m_status	Lintradoc/taskmanager/TaskInfo$STATUS;
/*     */         //   456: aload_0
/*     */         //   457: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   460: invokestatic 59	intradoc/taskmanager/TaskMonitor:markLauncherFailed	(Lintradoc/taskmanager/TaskLauncher;)V
/*     */         //   463: ldc 6
/*     */         //   465: new 7	java/lang/StringBuilder
/*     */         //   468: dup
/*     */         //   469: invokespecial 8	java/lang/StringBuilder:<init>	()V
/*     */         //   472: ldc 60
/*     */         //   474: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   477: aload_2
/*     */         //   478: getfield 49	intradoc/taskmanager/TaskInfo:m_attemptCount	I
/*     */         //   481: invokevirtual 61	java/lang/StringBuilder:append	(I)Ljava/lang/StringBuilder;
/*     */         //   484: ldc 62
/*     */         //   486: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   489: invokevirtual 13	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */         //   492: aload 4
/*     */         //   494: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   497: goto +15 -> 512
/*     */         //   500: ldc 6
/*     */         //   502: ldc 63
/*     */         //   504: aconst_null
/*     */         //   505: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   508: aload_2
/*     */         //   509: invokestatic 64	intradoc/taskmanager/TaskMonitor:addToQueue	(Lintradoc/taskmanager/TaskInfo;)V
/*     */         //   512: goto +45 -> 557
/*     */         //   515: ldc 6
/*     */         //   517: ldc 65
/*     */         //   519: aconst_null
/*     */         //   520: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   523: aload_0
/*     */         //   524: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   527: aload_2
/*     */         //   528: getfield 39	intradoc/taskmanager/TaskInfo:m_cmdLine	Ljava/util/Vector;
/*     */         //   531: iconst_1
/*     */         //   532: invokevirtual 66	intradoc/taskmanager/TaskLauncher:createCommandLine	(Ljava/util/Vector;Z)[Ljava/lang/String;
/*     */         //   535: astore_3
/*     */         //   536: aload_0
/*     */         //   537: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   540: aload_3
/*     */         //   541: aload_2
/*     */         //   542: aload_0
/*     */         //   543: getfield 4	intradoc/taskmanager/TaskMonitor$1:val$uniqueStr	Ljava/lang/String;
/*     */         //   546: invokevirtual 38	intradoc/taskmanager/TaskLauncher:startExe	([Ljava/lang/String;Lintradoc/taskmanager/TaskInfo;Ljava/lang/String;)V
/*     */         //   549: aload_0
/*     */         //   550: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   553: aload_2
/*     */         //   554: invokevirtual 46	intradoc/taskmanager/TaskLauncher:launchExe	(Lintradoc/taskmanager/TaskInfo;)V
/*     */         //   557: ldc 6
/*     */         //   559: new 7	java/lang/StringBuilder
/*     */         //   562: dup
/*     */         //   563: invokespecial 8	java/lang/StringBuilder:<init>	()V
/*     */         //   566: ldc 67
/*     */         //   568: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   571: aload_2
/*     */         //   572: invokevirtual 68	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
/*     */         //   575: invokevirtual 13	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */         //   578: aconst_null
/*     */         //   579: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   582: aload_2
/*     */         //   583: dup
/*     */         //   584: astore_3
/*     */         //   585: monitorenter
/*     */         //   586: aload_2
/*     */         //   587: iconst_1
/*     */         //   588: putfield 69	intradoc/taskmanager/TaskInfo:m_isFinished	Z
/*     */         //   591: aload_2
/*     */         //   592: invokevirtual 70	java/lang/Object:notify	()V
/*     */         //   595: aload_3
/*     */         //   596: monitorexit
/*     */         //   597: goto +10 -> 607
/*     */         //   600: astore 9
/*     */         //   602: aload_3
/*     */         //   603: monitorexit
/*     */         //   604: aload 9
/*     */         //   606: athrow
/*     */         //   607: aload_0
/*     */         //   608: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   611: getfield 34	intradoc/taskmanager/TaskLauncher:m_needPersistance	Z
/*     */         //   614: ifne +210 -> 824
/*     */         //   617: aload_0
/*     */         //   618: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   621: iconst_0
/*     */         //   622: invokevirtual 71	intradoc/taskmanager/TaskLauncher:clearExe	(Z)V
/*     */         //   625: goto +199 -> 824
/*     */         //   628: astore_3
/*     */         //   629: ldc 6
/*     */         //   631: ldc 72
/*     */         //   633: aload_3
/*     */         //   634: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   637: aload_2
/*     */         //   638: aload_3
/*     */         //   639: invokevirtual 73	java/lang/Throwable:getMessage	()Ljava/lang/String;
/*     */         //   642: putfield 54	intradoc/taskmanager/TaskInfo:m_errMsg	Ljava/lang/String;
/*     */         //   645: aload_2
/*     */         //   646: aload_3
/*     */         //   647: putfield 55	intradoc/taskmanager/TaskInfo:m_error	Ljava/lang/Throwable;
/*     */         //   650: aload_2
/*     */         //   651: iconst_1
/*     */         //   652: putfield 56	intradoc/taskmanager/TaskInfo:m_hasError	Z
/*     */         //   655: aload_2
/*     */         //   656: getstatic 57	intradoc/taskmanager/TaskInfo$STATUS:FAILURE	Lintradoc/taskmanager/TaskInfo$STATUS;
/*     */         //   659: putfield 58	intradoc/taskmanager/TaskInfo:m_status	Lintradoc/taskmanager/TaskInfo$STATUS;
/*     */         //   662: aload_0
/*     */         //   663: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   666: invokestatic 59	intradoc/taskmanager/TaskMonitor:markLauncherFailed	(Lintradoc/taskmanager/TaskLauncher;)V
/*     */         //   669: ldc 6
/*     */         //   671: ldc 74
/*     */         //   673: aload_3
/*     */         //   674: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   677: ldc 6
/*     */         //   679: new 7	java/lang/StringBuilder
/*     */         //   682: dup
/*     */         //   683: invokespecial 8	java/lang/StringBuilder:<init>	()V
/*     */         //   686: ldc 67
/*     */         //   688: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   691: aload_2
/*     */         //   692: invokevirtual 68	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
/*     */         //   695: invokevirtual 13	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */         //   698: aconst_null
/*     */         //   699: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   702: aload_2
/*     */         //   703: dup
/*     */         //   704: astore_3
/*     */         //   705: monitorenter
/*     */         //   706: aload_2
/*     */         //   707: iconst_1
/*     */         //   708: putfield 69	intradoc/taskmanager/TaskInfo:m_isFinished	Z
/*     */         //   711: aload_2
/*     */         //   712: invokevirtual 70	java/lang/Object:notify	()V
/*     */         //   715: aload_3
/*     */         //   716: monitorexit
/*     */         //   717: goto +10 -> 727
/*     */         //   720: astore 10
/*     */         //   722: aload_3
/*     */         //   723: monitorexit
/*     */         //   724: aload 10
/*     */         //   726: athrow
/*     */         //   727: aload_0
/*     */         //   728: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   731: getfield 34	intradoc/taskmanager/TaskLauncher:m_needPersistance	Z
/*     */         //   734: ifne +90 -> 824
/*     */         //   737: aload_0
/*     */         //   738: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   741: iconst_0
/*     */         //   742: invokevirtual 71	intradoc/taskmanager/TaskLauncher:clearExe	(Z)V
/*     */         //   745: goto +79 -> 824
/*     */         //   748: astore 11
/*     */         //   750: ldc 6
/*     */         //   752: new 7	java/lang/StringBuilder
/*     */         //   755: dup
/*     */         //   756: invokespecial 8	java/lang/StringBuilder:<init>	()V
/*     */         //   759: ldc 67
/*     */         //   761: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   764: aload_2
/*     */         //   765: invokevirtual 68	java/lang/StringBuilder:append	(Ljava/lang/Object;)Ljava/lang/StringBuilder;
/*     */         //   768: invokevirtual 13	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */         //   771: aconst_null
/*     */         //   772: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   775: aload_2
/*     */         //   776: dup
/*     */         //   777: astore 12
/*     */         //   779: monitorenter
/*     */         //   780: aload_2
/*     */         //   781: iconst_1
/*     */         //   782: putfield 69	intradoc/taskmanager/TaskInfo:m_isFinished	Z
/*     */         //   785: aload_2
/*     */         //   786: invokevirtual 70	java/lang/Object:notify	()V
/*     */         //   789: aload 12
/*     */         //   791: monitorexit
/*     */         //   792: goto +11 -> 803
/*     */         //   795: astore 13
/*     */         //   797: aload 12
/*     */         //   799: monitorexit
/*     */         //   800: aload 13
/*     */         //   802: athrow
/*     */         //   803: aload_0
/*     */         //   804: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   807: getfield 34	intradoc/taskmanager/TaskLauncher:m_needPersistance	Z
/*     */         //   810: ifne +11 -> 821
/*     */         //   813: aload_0
/*     */         //   814: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   817: iconst_0
/*     */         //   818: invokevirtual 71	intradoc/taskmanager/TaskLauncher:clearExe	(Z)V
/*     */         //   821: aload 11
/*     */         //   823: athrow
/*     */         //   824: goto -789 -> 35
/*     */         //   827: iload_1
/*     */         //   828: ifeq +13 -> 841
/*     */         //   831: aload_0
/*     */         //   832: getfield 1	intradoc/taskmanager/TaskMonitor$1:val$launcher	Lintradoc/taskmanager/TaskLauncher;
/*     */         //   835: getfield 28	intradoc/taskmanager/TaskLauncher:m_name	Ljava/lang/String;
/*     */         //   838: invokestatic 75	intradoc/taskmanager/TaskMonitor:stop	(Ljava/lang/String;)V
/*     */         //   841: ldc 6
/*     */         //   843: new 7	java/lang/StringBuilder
/*     */         //   846: dup
/*     */         //   847: invokespecial 8	java/lang/StringBuilder:<init>	()V
/*     */         //   850: ldc 76
/*     */         //   852: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   855: aload_0
/*     */         //   856: invokevirtual 11	java/lang/Object:toString	()Ljava/lang/String;
/*     */         //   859: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   862: ldc 77
/*     */         //   864: invokevirtual 10	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*     */         //   867: invokevirtual 13	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*     */         //   870: aconst_null
/*     */         //   871: invokestatic 14	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*     */         //   874: return
/*     */         //
/*     */         // Exception table:
/*     */         //   from	to	target	type
/*     */         //   143	239	254	java/lang/Throwable
/*     */         //   70	241	269	finally
/*     */         //   244	246	269	finally
/*     */         //   249	251	269	finally
/*     */         //   254	266	269	finally
/*     */         //   269	273	269	finally
/*     */         //   315	372	375	java/lang/Exception
/*     */         //   586	597	600	finally
/*     */         //   600	604	600	finally
/*     */         //   276	557	628	java/lang/Throwable
/*     */         //   706	717	720	finally
/*     */         //   720	724	720	finally
/*     */         //   276	557	748	finally
/*     */         //   628	677	748	finally
/*     */         //   748	750	748	finally
/*     */         //   780	792	795	finally
/*     */         //   795	800	795	finally
/*     */       }
/*     */     };
/* 193 */     Thread thread = new Thread(run, name + unique);
/* 194 */     thread.setDaemon(true);
/* 195 */     thread.start();
/*     */ 
/* 197 */     return launcher;
/*     */   }
/*     */ 
/*     */   public static Vector getOrCreateQueue(TaskInfo info, boolean isCreateQueue)
/*     */   {
/* 202 */     String key = m_defaultQueueName;
/* 203 */     if ((info.m_name != null) && (info.m_name.length() > 0))
/*     */     {
/* 205 */       key = info.m_name;
/*     */     }
/*     */ 
/* 208 */     Vector queue = (Vector)m_queues.get(key);
/* 209 */     if ((isCreateQueue) && (queue == null))
/*     */     {
/* 211 */       queue = new IdcVector();
/*     */ 
/* 213 */       m_queues.put(key, queue);
/* 214 */       SystemUtils.registerSynchronizationObjectToNotifyOnStop(queue);
/*     */     }
/*     */ 
/* 217 */     checkLauncherInited(key, info, queue);
/* 218 */     return queue;
/*     */   }
/*     */ 
/*     */   protected static void checkLauncherInited(String key, TaskInfo info, Vector queue)
/*     */   {
/* 223 */     int curNumLaunchers = 0;
/* 224 */     Set launcherSet = (Set)m_launchers.get(key);
/* 225 */     if (launcherSet != null)
/*     */     {
/* 227 */       curNumLaunchers = launcherSet.size();
/*     */     }
/*     */     else
/*     */     {
/* 231 */       launcherSet = new HashSet();
/*     */     }
/*     */ 
/* 235 */     int neededNumLaunchers = 1;
/* 236 */     neededNumLaunchers = SharedObjects.getEnvironmentInt(key + "NumLaunchers", 1);
/* 237 */     if (neededNumLaunchers < 1)
/*     */     {
/* 240 */       neededNumLaunchers = 1;
/*     */     }
/*     */ 
/* 243 */     int numLaunchersToCreate = neededNumLaunchers - curNumLaunchers;
/* 244 */     if (numLaunchersToCreate > 0)
/*     */     {
/* 246 */       Report.trace("taskmanager", "Creating " + numLaunchersToCreate + " new launchers for task: " + key, null);
/*     */     }
/*     */ 
/* 249 */     for (int i = 0; i < numLaunchersToCreate; ++i)
/*     */     {
/* 251 */       TaskLauncher launcher = startProcessMonitor(key, info, queue, "_" + m_counter++);
/* 252 */       launcherSet.add(launcher);
/*     */     }
/* 254 */     m_launchers.put(key, launcherSet);
/*     */   }
/*     */ 
/*     */   public static void addToQueue(TaskInfo info)
/*     */   {
/* 259 */     Report.trace("taskmanager", "adding to queue taskinfo:" + info.toString(), null);
/* 260 */     synchronized (m_lockObject)
/*     */     {
/* 262 */       info.m_taskOrigin = new Exception("task origin stack");
/*     */ 
/* 264 */       Vector queue = getOrCreateQueue(info, true);
/* 265 */       synchronized (queue)
/*     */       {
/* 267 */         queue.addElement(info);
/* 268 */         queue.notify();
/*     */       }
/*     */     }
/* 271 */     Report.trace("taskmanager", "releasing m_lockObject in addToQueue()", null);
/*     */   }
/*     */ 
/*     */   public static void markLauncherFailed(TaskLauncher launcher)
/*     */   {
/* 276 */     synchronized (m_lockObject)
/*     */     {
/* 278 */       String name = launcher.getName();
/* 279 */       Set launcherSet = (Set)m_launchers.get(name);
/* 280 */       if (launcherSet != null)
/*     */       {
/* 282 */         Iterator iter = launcherSet.iterator();
/* 283 */         while (iter.hasNext())
/*     */         {
/* 285 */           TaskLauncher tempLauncher = (TaskLauncher)iter.next();
/* 286 */           if (tempLauncher == launcher)
/*     */           {
/* 288 */             Report.trace("taskmanager", "Removing launcher for task: " + launcher.m_name + " that has been marked as terminated", null);
/* 289 */             launcherSet.remove(tempLauncher);
/* 290 */             if (!launcherSet.isEmpty())
/*     */               break;
/* 292 */             m_launchers.remove(name); break;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int getNumInQueue()
/*     */   {
/* 303 */     return getNumInQueue(null);
/*     */   }
/*     */ 
/*     */   public static int getNumInQueue(String name)
/*     */   {
/* 308 */     int num = 0;
/* 309 */     if (name == null)
/*     */     {
/* 311 */       name = m_defaultQueueName;
/*     */     }
/* 313 */     Vector queue = (Vector)m_queues.get(name);
/* 314 */     if (queue != null)
/*     */     {
/* 316 */       num = queue.size();
/*     */     }
/* 318 */     return num;
/*     */   }
/*     */ 
/*     */   public static boolean isBusy()
/*     */   {
/* 323 */     return isBusy(null);
/*     */   }
/*     */ 
/*     */   public static boolean isBusy(String name)
/*     */   {
/* 328 */     boolean isBusy = false;
/*     */ 
/* 330 */     Set launcherSet = getLauncher(name);
/* 331 */     if ((((launcherSet != null) ? 1 : 0) & ((launcherSet.size() > 0) ? 1 : 0)) != 0)
/*     */     {
/* 335 */       isBusy = true;
/* 336 */       Iterator iter = launcherSet.iterator();
/* 337 */       while (iter.hasNext())
/*     */       {
/* 339 */         TaskLauncher tempLauncher = (TaskLauncher)iter.next();
/* 340 */         if (!tempLauncher.isBusy())
/*     */         {
/* 342 */           isBusy = false;
/* 343 */           break;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 348 */     return isBusy;
/*     */   }
/*     */ 
/*     */   public static Set getLauncher(String name)
/*     */   {
/* 353 */     if (name == null)
/*     */     {
/* 355 */       name = m_defaultQueueName;
/*     */     }
/* 357 */     return (Set)m_launchers.get(name);
/*     */   }
/*     */ 
/*     */   public static void stop()
/*     */   {
/* 362 */     stop(null);
/*     */   }
/*     */ 
/*     */   public static void stop(String name)
/*     */   {
/* 368 */     synchronized (m_lockObject)
/*     */     {
/* 370 */       Set launcherSet = getLauncher(name);
/* 371 */       if (launcherSet != null)
/*     */       {
/* 373 */         Iterator iter = launcherSet.iterator();
/* 374 */         while (iter.hasNext())
/*     */         {
/* 376 */           TaskLauncher tempLauncher = (TaskLauncher)iter.next();
/* 377 */           tempLauncher.abort();
/*     */         }
/*     */       }
/*     */ 
/* 381 */       Vector queue = (Vector)m_queues.remove(name);
/* 382 */       if (queue != null)
/*     */       {
/* 384 */         synchronized (queue)
/*     */         {
/* 386 */           queue.notifyAll();
/*     */         }
/* 388 */         SystemUtils.unregisterSynchronizationObjectToNotifyOnStop(queue);
/*     */       }
/* 390 */       m_launchers.remove(name);
/*     */     }
/* 392 */     SystemUtils.unregisterSynchronizationObjectToNotifyOnStop(m_lockObject);
/*     */   }
/*     */ 
/*     */   public static void clearQueue()
/*     */   {
/* 397 */     stop();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 402 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102103 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.taskmanager.TaskMonitor
 * JD-Core Version:    0.5.4
 */