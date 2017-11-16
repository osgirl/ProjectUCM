/*     */ package intradoc.preview;
/*     */ 
/*     */ import intradoc.common.DataStreamWrapper;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.taskmanager.TaskInfo;
/*     */ import intradoc.taskmanager.TaskInfo.STATUS;
/*     */ import intradoc.taskmanager.TaskMonitor;
/*     */ import intradoc.taskmanager.TaskWork;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class PreviewTaskService extends Service
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void checkPreviewServer()
/*     */     throws DataException, ServiceException
/*     */   {
/*  50 */     int numAllowed = SharedObjects.getEnvironmentInt("MaxPreviewQueue", 3);
/*  51 */     int num = TaskMonitor.getNumInQueue("Preview");
/*     */ 
/*  53 */     if (num <= numAllowed - 1)
/*     */       return;
/*  55 */     createServiceException(null, "!csPreviewServerBusy");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void performPreviewTask()
/*     */     throws DataException, ServiceException
/*     */   {
/*  62 */     String previewPath = SharedObjects.getEnvironmentValue("PreviewPath");
/*  63 */     if (previewPath == null)
/*     */     {
/*  65 */       createServiceException(null, "!csPreviewerMisconfigured!csPreviewPathNotDefined");
/*     */     }
/*  67 */     int result = FileUtils.checkFile(previewPath, true, false);
/*  68 */     if (result != 0)
/*     */     {
/*  70 */       String errMsg = FileUtils.getErrorMsg(previewPath, true, result);
/*  71 */       String msg = LocaleUtils.encodeMessage("csPreviewPathNotCorrect", errMsg);
/*  72 */       msg = LocaleUtils.encodeMessage("csPreviewPathNotDefined", msg);
/*  73 */       createServiceException(null, msg);
/*     */     }
/*  75 */     String zipFile = this.m_binder.get("ZipFile:path");
/*     */ 
/*  78 */     this.m_binder = new DataBinder();
/*     */ 
/*  80 */     boolean isDebugTrace = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("PreviewDebugTrace"), false);
/*     */ 
/*  82 */     if (!isDebugTrace)
/*     */     {
/*  84 */       this.m_binder.addTempFile(zipFile);
/*     */     }
/*     */ 
/*  87 */     String dir = DataBinder.getTemporaryDirectory();
/*  88 */     DataStreamWrapper streamWrapper = getDownloadStream(true);
/*  89 */     String resultFileName = DataBinder.getNextFileCounter() + ".zip";
/*  90 */     String resultFilePath = dir + resultFileName;
/*  91 */     String resultFileFormat = "Application/zip";
/*  92 */     streamWrapper.setSimpleFileData(resultFilePath, resultFileName, resultFileFormat);
/*     */ 
/*  98 */     Vector cmdLine = new IdcVector();
/*  99 */     cmdLine.addElement(previewPath);
/* 100 */     cmdLine.addElement("ZipFile=\"" + zipFile + "\"");
/* 101 */     cmdLine.addElement("ResultZipFile=\"" + resultFilePath + "\"");
/*     */ 
/* 104 */     TaskInfo info = new TaskInfo("Preview", cmdLine, resultFilePath);
/* 105 */     info.m_data = this.m_binder;
/* 106 */     info.m_timeout = SharedObjects.getEnvironmentInt("PreviewTimeout", 2);
/* 107 */     createWorker(info);
/*     */ 
/* 109 */     TaskMonitor.addToQueue(info);
/*     */ 
/* 111 */     synchronized (info)
/*     */     {
/* 113 */       if (!info.m_isFinished)
/*     */       {
/*     */         try
/*     */         {
/* 117 */           info.wait();
/*     */         }
/*     */         catch (Throwable ignore)
/*     */         {
/* 121 */           Report.trace(null, null, ignore);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 127 */     if ((info.m_status == TaskInfo.STATUS.FAILURE) || (info.m_status == TaskInfo.STATUS.TIMEOUT))
/*     */     {
/* 129 */       createServiceException(null, info.m_errMsg);
/*     */     }
/*     */ 
/* 132 */     PreviewWorker worker = (PreviewWorker)info.m_worker;
/* 133 */     if (worker.m_statusCode < 0L)
/*     */     {
/* 135 */       String msg = LocaleUtils.encodeMessage("csPreviewFailedToCreatePacket", worker.m_statusMessage);
/*     */ 
/* 137 */       createServiceException(null, msg);
/*     */     }
/*     */ 
/* 140 */     streamWrapper.m_useStream = true;
/*     */ 
/* 142 */     if (isDebugTrace)
/*     */       return;
/* 144 */     this.m_binder.addTempFile(resultFilePath);
/*     */   }
/*     */ 
/*     */   protected void createWorker(TaskInfo info)
/*     */   {
/* 150 */     info.m_worker = new PreviewWorker();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 155 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92435 $";
/*     */   }
/*     */   static class PreviewWorker implements TaskWork { public Properties m_props;
/*     */     public long m_statusCode;
/*     */     public String m_statusMessage;
/*     */ 
/* 161 */     PreviewWorker() { this.m_props = null;
/* 162 */       this.m_statusCode = 0L;
/* 163 */       this.m_statusMessage = null; }
/*     */ 
/*     */ 
/*     */     public void finishWork(TaskInfo info)
/*     */     {
/* 168 */       String str = info.m_output;
/* 169 */       this.m_props = new Properties();
/* 170 */       StringTokenizer tokens = new StringTokenizer(str, "\n");
/* 171 */       while (tokens.hasMoreTokens())
/*     */       {
/* 173 */         String token = tokens.nextToken();
/* 174 */         token = token.trim();
/* 175 */         if (token.length() == 0)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 180 */         int index = token.indexOf("=");
/* 181 */         if (index < 0)
/*     */         {
/* 183 */           this.m_props.put(token, "");
/*     */         }
/*     */         else
/*     */         {
/* 187 */           String key = token.substring(0, index);
/* 188 */           String value = token.substring(index + 1);
/* 189 */           this.m_props.put(key, value);
/*     */         }
/*     */       }
/*     */ 
/* 193 */       this.m_statusMessage = this.m_props.getProperty("result");
/* 194 */       String hr = this.m_props.getProperty("hr");
/*     */       try
/*     */       {
/* 197 */         this.m_statusCode = Integer.parseInt(hr);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 202 */         this.m_statusCode = -1L;
/*     */       }
/*     */     }
/*     */ 
/*     */     public TaskInfo stopCommand(TaskInfo ti)
/*     */     {
/* 213 */       return null;
/*     */     }
/*     */ 
/*     */     public boolean isSuccessful(String output)
/*     */     {
/* 218 */       return true;
/*     */     }
/*     */ 
/*     */     public String[] startWork()
/*     */     {
/* 223 */       return null;
/*     */     }
/*     */ 
/*     */     public void cleanUp()
/*     */     {
/*     */     } }
/*     */ 
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.preview.PreviewTaskService
 * JD-Core Version:    0.5.4
 */