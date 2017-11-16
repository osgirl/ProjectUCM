/*     */ package intradoc.taskmanager.tasks;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.taskmanager.TaskInfo;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.IOException;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.Vector;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class DocumentTextExtractor
/*     */ {
/*     */   protected boolean m_init;
/*     */   protected String m_tifFallbackFormat;
/*     */   protected String m_tifJavaOutputCharacterSet;
/*     */   protected String m_tifExportExePath;
/*     */   protected String[] m_tifEnvironmentValues;
/*     */   protected int m_timeout;
/*     */   protected boolean m_extractSubobjects;
/*     */   public boolean m_singleThreadExtraction;
/*     */   public boolean m_outputFileViaBinder;
/*     */ 
/*     */   public DocumentTextExtractor()
/*     */   {
/*  36 */     this.m_init = false;
/*  37 */     this.m_tifFallbackFormat = null;
/*  38 */     this.m_tifJavaOutputCharacterSet = null;
/*  39 */     this.m_tifExportExePath = null;
/*  40 */     this.m_tifEnvironmentValues = null;
/*  41 */     this.m_timeout = 15000;
/*  42 */     this.m_extractSubobjects = false;
/*  43 */     this.m_singleThreadExtraction = false;
/*  44 */     this.m_outputFileViaBinder = false;
/*     */   }
/*     */ 
/*     */   public void init(String fallbackFormat, String outputCharset, String exePath, String[] envValues) {
/*  48 */     this.m_tifFallbackFormat = fallbackFormat;
/*  49 */     this.m_tifJavaOutputCharacterSet = outputCharset;
/*  50 */     this.m_tifExportExePath = exePath;
/*  51 */     this.m_tifEnvironmentValues = envValues;
/*  52 */     this.m_timeout = (SharedObjects.getEnvironmentInt("TextExtractorTimeoutInSec", 15) * 1000);
/*  53 */     this.m_extractSubobjects = SharedObjects.getEnvValueAsBoolean("TextExtractorProcessSubobjects", true);
/*  54 */     this.m_singleThreadExtraction = checkTextExportCapability();
/*  55 */     this.m_init = true;
/*     */   }
/*     */ 
/*     */   public TaskInfo doTextExport(String inputFilePath, String outputFilePath, String manifestDir, String format)
/*     */     throws ServiceException
/*     */   {
/*  62 */     return doTextExport(inputFilePath, outputFilePath, manifestDir, format, null);
/*     */   }
/*     */ 
/*     */   public TaskInfo doTextExport(String inputFilePath, String outputFilePath, String manifestDir, String format, String forcedHandler)
/*     */     throws ServiceException
/*     */   {
/*  78 */     TaskInfo taskInfo = null;
/*  79 */     Vector cmdLine = new IdcVector();
/*     */ 
/*  82 */     if (this.m_singleThreadExtraction)
/*     */     {
/*  84 */       String manifestFileName = DataBinder.getNextFileCounter() + ".hda";
/*  85 */       String manifestFilePath = manifestDir + manifestFileName;
/*     */ 
/*  87 */       DataBinder data = new DataBinder();
/*  88 */       data.putLocal("InputFilePath", inputFilePath);
/*  89 */       data.putLocal("FallbackFormat", this.m_tifFallbackFormat);
/*  90 */       data.putLocal("OutputCharacterSet", this.m_tifJavaOutputCharacterSet);
/*  91 */       data.putLocal("OutputFilePath", outputFilePath);
/*  92 */       ResourceUtils.serializeDataBinder(manifestDir, manifestFileName, data, true, false);
/*     */ 
/*  95 */       cmdLine.addElement(this.m_tifExportExePath);
/*  96 */       cmdLine.addElement("-c");
/*  97 */       cmdLine.addElement(manifestFilePath);
/*  98 */       if (!this.m_outputFileViaBinder)
/*     */       {
/* 102 */         cmdLine.addElement("-f");
/* 103 */         cmdLine.addElement(outputFilePath);
/*     */       }
/* 105 */       if (this.m_extractSubobjects)
/*     */       {
/* 107 */         cmdLine.addElement("-r");
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 112 */       String cmd = this.m_tifJavaOutputCharacterSet + "," + inputFilePath + "," + this.m_tifFallbackFormat + "," + outputFilePath;
/* 113 */       if (this.m_extractSubobjects)
/*     */       {
/* 115 */         cmd = cmd + "," + this.m_extractSubobjects;
/*     */       }
/*     */ 
/* 118 */       cmdLine.addElement(cmd);
/*     */     }
/*     */ 
/* 121 */     taskInfo = prepareTask(cmdLine, outputFilePath);
/*     */ 
/* 123 */     return taskInfo;
/*     */   }
/*     */ 
/*     */   protected boolean checkTextExportCapability()
/*     */   {
/* 128 */     boolean oldVersion = false;
/* 129 */     String override = SharedObjects.getEnvironmentValue("TextExtractorOverrideMode");
/* 130 */     if (override != null)
/*     */     {
/* 133 */       if (override.compareTo("batch") != 0)
/*     */       {
/* 135 */         oldVersion = true;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 140 */       Runtime run = Runtime.getRuntime();
/*     */ 
/* 143 */       String[] cmdLine = { this.m_tifExportExePath, "-v" };
/*     */       try
/*     */       {
/* 147 */         Process textExport = run.exec(cmdLine);
/* 148 */         byte[] buf = new byte[500];
/* 149 */         int len = 0;
/*     */ 
/* 151 */         if (EnvUtils.isAppServerType("websphere"))
/*     */         {
/*     */           try
/*     */           {
/* 155 */             Object inputStreamObject = textExport.getInputStream();
/* 156 */             String classString = "java.lang.ProcessInputStream";
/* 157 */             if (ClassHelperUtils.isInstanceOf(classString, inputStreamObject))
/*     */             {
/* 159 */               Method readMethod = inputStreamObject.getClass().getDeclaredMethod("read", new Class[] { buf.getClass() });
/* 160 */               readMethod.setAccessible(true);
/* 161 */               len = ((Integer)readMethod.invoke(inputStreamObject, new Object[] { buf })).intValue();
/*     */             }
/*     */           }
/*     */           catch (Exception se)
/*     */           {
/* 166 */             Report.trace(null, "Unable to create class java.lang.ProcessInputStream. Text extraction will fail", se);
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 172 */           BufferedInputStream is = (BufferedInputStream)textExport.getInputStream();
/* 173 */           len = is.read(buf);
/*     */         }
/*     */ 
/* 176 */         if (len > 0)
/*     */         {
/* 178 */           String version = "";
/* 179 */           String output = new String(buf, 0, len);
/* 180 */           Pattern pattern = Pattern.compile("[0-9]+.[0-9]+.[0-9]+.[0-9]+.");
/* 181 */           Matcher matcher = pattern.matcher(output);
/* 182 */           boolean found = matcher.find();
/* 183 */           if (found)
/*     */           {
/* 186 */             version = matcher.group().trim();
/* 187 */             Report.trace("indexer", "found version  " + version + " in output " + output, null);
/* 188 */             int result = SystemUtils.compareVersions(version, "8.3.2.5346");
/* 189 */             if (result < 0)
/*     */             {
/* 191 */               Report.trace(null, "defaulting to single thread text extraction" + output, null);
/* 192 */               oldVersion = true;
/*     */             }
/*     */ 
/* 195 */             result = SystemUtils.compareVersions(version, "8.3.5.0");
/* 196 */             if (result < 0)
/*     */             {
/* 198 */               Report.trace(null, "passing outputfilepath to textexport via command line", null);
/* 199 */               this.m_outputFileViaBinder = false;
/*     */             }
/*     */             else
/*     */             {
/* 203 */               Report.trace(null, "passing outputfilepath to textexport via databinder", null);
/* 204 */               this.m_outputFileViaBinder = true;
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 211 */             oldVersion = true;
/* 212 */             Report.trace(null, "defaulting to single thread text extraction - found NO version in output " + output, null);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 219 */         e.printStackTrace();
/*     */       }
/*     */     }
/*     */ 
/* 223 */     return oldVersion;
/*     */   }
/*     */ 
/*     */   protected TaskInfo prepareTask(Vector cmdLine, String outputFilePath)
/*     */   {
/* 228 */     TaskInfo taskInfo = new TaskInfo("TextExport", cmdLine, outputFilePath);
/*     */ 
/* 231 */     taskInfo.m_traceSubject = "tasks";
/* 232 */     taskInfo.m_timeout = this.m_timeout;
/* 233 */     taskInfo.m_environment = this.m_tifEnvironmentValues;
/*     */ 
/* 235 */     taskInfo.m_needProcPersistance = (!this.m_singleThreadExtraction);
/*     */ 
/* 237 */     byte[] eodMarker = { Byte.parseByte("46") };
/* 238 */     taskInfo.m_eods[2] = eodMarker;
/* 239 */     taskInfo.m_eods[1] = eodMarker;
/* 240 */     taskInfo.m_eods[0] = eodMarker;
/*     */ 
/* 242 */     return taskInfo;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 247 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94144 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.taskmanager.tasks.DocumentTextExtractor
 * JD-Core Version:    0.5.4
 */