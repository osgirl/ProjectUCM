/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.taskmanager.TaskInfo;
/*     */ import intradoc.taskmanager.TaskWork;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IndexerTaskWorkImpl
/*     */   implements TaskWork
/*     */ {
/*     */   Properties m_props;
/*     */   IndexerInfo m_ii;
/*     */   String m_filePath;
/*     */   boolean m_clearFileIfUnsuccessful;
/*     */   protected ConversionValidationUtils m_validator;
/*     */ 
/*     */   public void init(IndexerInfo ii, Properties props, String filePath, boolean clearFileIfUnsuccessful)
/*     */   {
/*  38 */     this.m_ii = ii;
/*  39 */     this.m_props = props;
/*  40 */     this.m_filePath = filePath;
/*  41 */     this.m_clearFileIfUnsuccessful = clearFileIfUnsuccessful;
/*  42 */     this.m_validator = new ConversionValidationUtils();
/*     */   }
/*     */ 
/*     */   public void cleanUp()
/*     */   {
/*  49 */     if (this.m_filePath == null)
/*     */       return;
/*     */     try
/*     */     {
/*  53 */       File deleteme = new File(this.m_filePath);
/*  54 */       deleteme.delete();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public void finishWork(TaskInfo info)
/*     */   {
/*  70 */     File file = null;
/*  71 */     boolean doClearFile = this.m_clearFileIfUnsuccessful;
/*  72 */     boolean hasExtractedFile = false;
/*  73 */     if (this.m_filePath != null)
/*     */     {
/*  75 */       file = new File(this.m_filePath);
/*     */     }
/*     */ 
/*  78 */     if ((file != null) && (file.exists()))
/*     */     {
/*  80 */       long size = file.length();
/*     */ 
/*  82 */       if (size > 0L)
/*     */       {
/*  84 */         this.m_props.put("DOC_FN", this.m_filePath);
/*     */         try
/*     */         {
/*  91 */           this.m_validator.validate(this.m_props);
/*  92 */           this.m_filePath = this.m_props.getProperty("DOC_FN");
/*  93 */           file = new File(this.m_filePath);
/*  94 */           size = file.length();
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/*  98 */           Report.trace("indexer", "Error validating extracted text", e);
/*     */         }
/*     */ 
/* 101 */         String ext = FileUtils.getExtension(this.m_filePath);
/* 102 */         this.m_props.put("dFullTextFormat", ext);
/* 103 */         if (this.m_ii != null)
/*     */         {
/* 105 */           this.m_ii.m_size = size;
/* 106 */           this.m_ii.m_indexWebFile = true;
/*     */         }
/* 108 */         hasExtractedFile = true;
/*     */       }
/*     */       else
/*     */       {
/* 112 */         Report.trace("indexer", "Extracted file contains zero bytes.", null);
/* 113 */         doClearFile = true;
/*     */       }
/*     */     }
/*     */ 
/* 117 */     if ((doClearFile) && (!hasExtractedFile))
/*     */     {
/* 119 */       this.m_props.put("DOC_FN", "");
/* 120 */       this.m_props.put("dFullTextFormat", "");
/* 121 */       String inputFilePath = this.m_props.getProperty("tifInputFilePath");
/* 122 */       if (this.m_ii != null)
/*     */       {
/* 124 */         this.m_ii.m_size = 0L;
/* 125 */         this.m_ii.m_indexWebFile = false;
/* 126 */         this.m_ii.m_indexError = LocaleUtils.encodeMessage("csTextConversionFailed", null, inputFilePath);
/*     */       }
/*     */     }
/*     */     else {
/* 130 */       if (hasExtractedFile) {
/*     */         return;
/*     */       }
/* 133 */       SystemUtils.trace("indexer", "TextExport was not successful.  Attempting normal indexing.");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String[] startWork()
/*     */   {
/* 145 */     String textExportPath = SharedObjects.getEnvironmentValue("TextExportExecutablePath");
/* 146 */     if (textExportPath == null)
/*     */     {
/*     */       try
/*     */       {
/* 150 */         textExportPath = TextConversionHandler.determineTextExportPath();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 154 */         Report.trace("taskmanager", "Error in startWork for TextExport task", e);
/*     */       }
/*     */     }
/*     */ 
/* 158 */     String[] cmdLine = { textExportPath, "startbatch" };
/* 159 */     return cmdLine;
/*     */   }
/*     */ 
/*     */   public TaskInfo stopCommand(TaskInfo ti)
/*     */   {
/* 169 */     Vector v = new Vector();
/* 170 */     v.add("stopbatch");
/* 171 */     TaskInfo stop_ti = new TaskInfo(ti.m_name, v, null);
/* 172 */     stop_ti.m_cmdLineOptions = v;
/* 173 */     stop_ti.m_eods = ti.m_eods;
/* 174 */     stop_ti.m_timeout = ti.m_timeout;
/* 175 */     stop_ti.m_environment = ti.m_environment;
/* 176 */     stop_ti.m_needProcPersistance = ti.m_needProcPersistance;
/* 177 */     return stop_ti;
/*     */   }
/*     */ 
/*     */   public boolean isSuccessful(String output)
/*     */   {
/* 182 */     boolean isSuccess = false;
/*     */ 
/* 184 */     if ((output.startsWith("0")) || (output.indexOf("success") != -1))
/*     */     {
/* 186 */       isSuccess = true;
/*     */     }
/*     */     else
/*     */     {
/* 190 */       Report.trace("taskmanager", "Task failed with output: " + output, null);
/*     */     }
/* 192 */     return isSuccess;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 196 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 91394 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerTaskWorkImpl
 * JD-Core Version:    0.5.4
 */