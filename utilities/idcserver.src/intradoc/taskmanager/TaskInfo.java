/*    */ package intradoc.taskmanager;
/*    */ 
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class TaskInfo
/*    */ {
/*    */   public String m_name;
/*    */   public Vector m_cmdLine;
/*    */   public Vector m_cmdLineOptions;
/* 37 */   public String m_resultPath = null;
/* 38 */   public String m_workingDir = null;
/* 39 */   public String[] m_environment = null;
/* 40 */   public String m_traceSubject = "taskmanager";
/* 41 */   public int m_processId = 0;
/*    */   public int m_idleTimeout;
/*    */   public int m_attemptCount;
/* 44 */   public long m_startTime = 0L;
/*    */ 
/* 46 */   public DataBinder m_data = null;
/* 47 */   public TaskWork m_worker = null;
/* 48 */   public Exception m_taskOrigin = null;
/*    */ 
/* 51 */   public volatile boolean m_isFinished = false;
/* 52 */   public volatile boolean m_hasError = false;
/* 53 */   public volatile String m_errMsg = null;
/* 54 */   public volatile Throwable m_error = null;
/* 55 */   public volatile String m_output = null;
/* 56 */   public volatile STATUS m_status = STATUS.NOTSTARTED;
/* 57 */   public volatile int m_exitValue = -1;
/*    */ 
/* 61 */   public volatile long m_timeout = 5000L;
/*    */ 
/* 63 */   public boolean m_needProcPersistance = false;
/*    */   public static final int OUTPUT = 0;
/*    */   public static final int INPUT = 1;
/*    */   public static final int ERROR = 2;
/* 69 */   public byte[][] m_eods = new byte[3][];
/*    */ 
/*    */   public TaskInfo(String name, Vector cmdLine, String resultPath)
/*    */   {
/* 73 */     this.m_name = name;
/* 74 */     this.m_cmdLine = cmdLine;
/* 75 */     this.m_resultPath = resultPath;
/* 76 */     this.m_attemptCount = 0;
/*    */ 
/* 79 */     this.m_idleTimeout = SharedObjects.getEnvironmentInt(this.m_name + "IdleTimeout", 60);
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 85 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 86 */     buf.append("TaskInfo: name<" + this.m_name + ">\n");
/* 87 */     buf.append("m_cmdLine<" + this.m_cmdLine + ">\n");
/* 88 */     buf.append("m_resultPath<" + this.m_resultPath + ">\n");
/* 89 */     buf.append("m_attemptCount<" + this.m_attemptCount + ">\n");
/* 90 */     buf.append("m_idleTimeout<" + this.m_idleTimeout + ">");
/*    */ 
/* 92 */     return buf.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 97 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97439 $";
/*    */   }
/*    */ 
/*    */   public static enum STATUS
/*    */   {
/* 32 */     NOTSTARTED, EXECUTING, SUCCESS, TIMEOUT, FAILURE;
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.taskmanager.TaskInfo
 * JD-Core Version:    0.5.4
 */