/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.taskmanager.TaskInfo;
/*     */ import intradoc.taskmanager.TaskMonitor;
/*     */ import intradoc.taskmanager.tasks.DocumentTextExtractor;
/*     */ import java.io.File;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TextConversionHandler
/*     */   implements IndexerConversionHandler
/*     */ {
/*  35 */   public static boolean m_isTifInitialized = false;
/*     */ 
/*  40 */   public static Properties m_tifExtensionMap = null;
/*  41 */   public static String m_tifFallbackFormat = null;
/*  42 */   public static String m_tifJavaOutputCharacterSet = null;
/*  43 */   public static String m_tifExportExePath = null;
/*  44 */   public static String[] m_tifEnvironmentValues = null;
/*     */   protected IndexerConfig m_config;
/*     */   protected boolean m_useLegacyConfiguration;
/*     */   protected String m_manifestDir;
/*     */   protected DocumentTextExtractor m_extractor;
/*     */   protected String m_outputDir;
/*     */   protected IndexerWorkObject m_data;
/*     */   protected boolean m_clearFileIfUnsuccessful;
/*     */   protected boolean m_initSuccess;
/*     */ 
/*     */   public TextConversionHandler()
/*     */   {
/*  46 */     this.m_config = null;
/*     */ 
/*  48 */     this.m_useLegacyConfiguration = false;
/*  49 */     this.m_manifestDir = null;
/*  50 */     this.m_extractor = null;
/*  51 */     this.m_outputDir = null;
/*  52 */     this.m_data = null;
/*  53 */     this.m_clearFileIfUnsuccessful = true;
/*  54 */     this.m_initSuccess = false;
/*     */   }
/*     */ 
/*     */   public void init(IndexerWorkObject data, IndexerExecution exec) throws DataException, ServiceException
/*     */   {
/*  59 */     this.m_config = exec.m_config;
/*  60 */     this.m_data = data;
/*     */ 
/*  62 */     this.m_clearFileIfUnsuccessful = this.m_config.getBoolean("ClearFileForUnsuccessfulTextExtraction", true);
/*  63 */     if (!m_isTifInitialized)
/*     */     {
/*  65 */       setTifFallbackFormat();
/*  66 */       setTifOutputCharacterSet();
/*  67 */       setTifTextExportExePath();
/*     */ 
/*  69 */       m_isTifInitialized = true;
/*     */     }
/*  71 */     if (this.m_extractor == null)
/*     */     {
/*  73 */       this.m_extractor = new DocumentTextExtractor();
/*  74 */       this.m_extractor.init(m_tifFallbackFormat, m_tifJavaOutputCharacterSet, m_tifExportExePath, m_tifEnvironmentValues);
/*     */     }
/*  76 */     if (this.m_manifestDir == null)
/*     */     {
/*  78 */       this.m_manifestDir = this.m_config.getValue("TextExtractionManifestDir");
/*  79 */       if (this.m_manifestDir == null)
/*     */       {
/*  81 */         this.m_manifestDir = (LegacyDirectoryLocator.getVaultTempDirectory() + "textexport/");
/*     */ 
/*  83 */         FileUtils.checkOrCreateDirectory(this.m_manifestDir, 2);
/*     */       }
/*     */     }
/*  86 */     this.m_outputDir = (LegacyDirectoryLocator.getSystemBaseDirectory("binary") + "search/" + exec.m_activeCollectionId + "/bulkload/~export/");
/*  87 */     FileUtils.checkOrCreateDirectory(this.m_outputDir, 3);
/*  88 */     this.m_initSuccess = true;
/*     */   }
/*     */ 
/*     */   public boolean isLegacyMode()
/*     */   {
/*  98 */     boolean retval = true;
/*  99 */     if (this.m_extractor != null)
/*     */     {
/* 101 */       retval = this.m_extractor.m_singleThreadExtraction;
/*     */     }
/* 103 */     return retval;
/*     */   }
/*     */ 
/*     */   public void convertDocument(Properties props, IndexerInfo ii, ExecutionContext ctxt)
/*     */     throws ServiceException
/*     */   {
/* 111 */     if ((ii.m_isDelete) || (ii.m_isUpdate)) {
/*     */       return;
/*     */     }
/* 114 */     boolean isRename = StringUtils.convertToBool(props.getProperty("indexerUseMap"), false);
/*     */ 
/* 116 */     String inputFilePath = props.getProperty("DOC_FN");
/* 117 */     inputFilePath = getAsciiFilePath(inputFilePath, props);
/*     */ 
/* 119 */     String outputFilePath = ConversionValidationUtils.getOutputFilePath(ii, props, isRename, this.m_outputDir);
/*     */ 
/* 121 */     String format = props.getProperty("webFormat");
/*     */ 
/* 123 */     if (SystemUtils.m_verbose)
/*     */     {
/* 125 */       Report.debug("indexer", "InputFilePath is: <" + inputFilePath + ">\nOutputFilePath is: <" + outputFilePath + ">", null);
/*     */     }
/*     */ 
/* 130 */     props.put("tifInputFilePath", inputFilePath);
/* 131 */     props.put("tifOutputFilePath", outputFilePath);
/* 132 */     TaskInfo ti = null;
/* 133 */     if (!this.m_initSuccess)
/*     */       return;
/* 135 */     if (isRename)
/*     */     {
/* 137 */       copyFileWithNewExtension(inputFilePath, outputFilePath);
/*     */     }
/*     */     else
/*     */     {
/* 141 */       if (SystemUtils.m_verbose)
/*     */       {
/* 143 */         Report.debug("indexer", "Sending document to conversion", null);
/*     */       }
/* 145 */       ti = this.m_extractor.doTextExport(inputFilePath, outputFilePath, this.m_manifestDir, format);
/*     */ 
/* 147 */       recordTaskInfo(ti);
/*     */ 
/* 150 */       setTifFileValues(ii, props, outputFilePath, ti);
/*     */ 
/* 152 */       TaskMonitor.addToQueue(ti);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void recordTaskInfo(TaskInfo info)
/*     */   {
/* 160 */     List taskInfoList = (List)this.m_data.getCachedObject("tiList");
/* 161 */     if (taskInfoList == null)
/*     */     {
/* 163 */       taskInfoList = new ArrayList();
/*     */     }
/*     */ 
/* 166 */     synchronized (taskInfoList)
/*     */     {
/* 168 */       taskInfoList.add(info);
/*     */     }
/*     */ 
/* 171 */     this.m_data.setCachedObject("tiList", taskInfoList);
/*     */   }
/*     */ 
/*     */   public void setTifFallbackFormat()
/*     */     throws DataException
/*     */   {
/* 178 */     String encoding = this.m_config.getValue("DefaultFilterInputFormat");
/* 179 */     if (encoding == null)
/*     */     {
/* 181 */       encoding = DataSerializeUtils.getSystemEncoding();
/*     */     }
/*     */ 
/* 185 */     m_tifFallbackFormat = getTifTextExportFormat(encoding, "TextIndexerFilter_FallbackFormats");
/*     */   }
/*     */ 
/*     */   public void setTifOutputCharacterSet()
/*     */     throws DataException
/*     */   {
/* 192 */     String encoding = this.m_config.getValue("DefaultFilterOutputFormat");
/* 193 */     if (encoding == null)
/*     */     {
/* 195 */       encoding = DataSerializeUtils.getSystemEncoding();
/*     */     }
/*     */ 
/* 199 */     m_tifJavaOutputCharacterSet = getTifTextExportFormat(encoding, "TextIndexerFilter_OutputCharacterSet");
/*     */   }
/*     */ 
/*     */   public String getTifTextExportFormat(String encoding, String rsetName) throws DataException
/*     */   {
/* 204 */     DataResultSet drset = this.m_config.getTable(rsetName);
/* 205 */     if (drset == null)
/*     */     {
/* 207 */       String msg = LocaleUtils.encodeMessage("csIndexerUnableToFindIndexerResourceTable", null, rsetName);
/* 208 */       throw new DataException(msg);
/*     */     }
/* 210 */     int encodingIndex = ResultSetUtils.getIndexMustExist(drset, "fileencoding");
/* 211 */     int descIndex = ResultSetUtils.getIndexMustExist(drset, "description");
/*     */ 
/* 213 */     String format = null;
/* 214 */     Vector row = drset.findRow(encodingIndex, encoding);
/* 215 */     if (row == null)
/*     */     {
/* 218 */       format = encoding;
/*     */     }
/*     */     else
/*     */     {
/* 222 */       format = drset.getStringValue(descIndex);
/*     */     }
/*     */ 
/* 225 */     return format;
/*     */   }
/*     */ 
/*     */   public static String determineTextExportPath()
/*     */     throws ServiceException
/*     */   {
/* 231 */     Map pathArgs = new HashMap();
/* 232 */     pathArgs = LegacyDirectoryLocator.getOitMap(pathArgs, "lib/contentaccess/textexport", "type_executable");
/* 233 */     String exportExePath = (String)pathArgs.get("path");
/* 234 */     if (exportExePath == null)
/*     */     {
/* 236 */       List attemptedPaths = (List)pathArgs.get("attemptedPaths");
/* 237 */       String msg = LocaleUtils.encodeMessage("csTextExportExecutableMissing", null, StringUtils.createStringSimple(attemptedPaths));
/*     */ 
/* 239 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 242 */     return exportExePath;
/*     */   }
/*     */ 
/*     */   public void setTifTextExportExePath() throws ServiceException
/*     */   {
/* 247 */     if ((m_tifExportExePath != null) && (m_tifEnvironmentValues != null))
/*     */     {
/* 249 */       return;
/*     */     }
/*     */ 
/* 253 */     m_tifExportExePath = this.m_config.getValue("TextExportExecutablePath");
/* 254 */     String exeDir = null;
/*     */ 
/* 256 */     if (m_tifExportExePath == null)
/*     */     {
/* 258 */       m_tifExportExePath = determineTextExportPath();
/*     */     }
/* 260 */     exeDir = FileUtils.getDirectory(m_tifExportExePath);
/*     */ 
/* 263 */     String osFamily = EnvUtils.getOSFamily();
/* 264 */     if (!osFamily.equals("windows"))
/*     */     {
/* 266 */       Map pathArgs = new HashMap();
/* 267 */       pathArgs = LegacyDirectoryLocator.getOitMap(pathArgs, "lib/contentaccess/textexport", "type_executable");
/* 268 */       String envPath = "PATH=" + exeDir;
/* 269 */       String envLibPath = (String)pathArgs.get("environment_settings");
/*     */ 
/* 271 */       m_tifEnvironmentValues = new String[2];
/* 272 */       m_tifEnvironmentValues[0] = envPath;
/* 273 */       m_tifEnvironmentValues[1] = envLibPath;
/*     */     }
/*     */ 
/* 276 */     Report.trace("indexer", "Setting m_tifExportExePath to: " + m_tifExportExePath, null);
/*     */   }
/*     */ 
/*     */   public String getAsciiFilePath(String filePath, Properties props)
/*     */   {
/*     */     try
/*     */     {
/* 284 */       byte[] b = StringUtils.getBytes(filePath, "UTF8");
/* 285 */       if (b.length != filePath.length())
/*     */       {
/* 288 */         String inputDir = LegacyDirectoryLocator.getSystemBaseDirectory("binary") + "search/bulkload/~vault/";
/* 289 */         FileUtils.checkOrCreateDirectory(inputDir, 3);
/* 290 */         String newFilePath = inputDir + props.getProperty("dID") + FileUtils.getExtension(filePath);
/*     */ 
/* 292 */         FileUtils.copyFile(filePath, newFilePath);
/*     */ 
/* 294 */         filePath = newFilePath;
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 300 */       if (SystemUtils.m_verbose)
/*     */       {
/* 302 */         Report.debug("indexer", null, e);
/*     */       }
/*     */     }
/*     */ 
/* 306 */     return filePath;
/*     */   }
/*     */ 
/*     */   public String getTifOutputFilePath(IndexerInfo ii, Properties props, boolean isRename)
/*     */     throws ServiceException
/*     */   {
/* 313 */     String outputDir = this.m_outputDir;
/*     */ 
/* 316 */     String fileName = ii.m_indexKey;
/*     */ 
/* 318 */     boolean default7Bit = EnvUtils.isFamily("windows");
/* 319 */     boolean use7BitOutputFilePath = this.m_config.getBoolean("Use7BitOutputFilePath", default7Bit);
/* 320 */     if (use7BitOutputFilePath)
/*     */     {
/* 322 */       IdcStringBuilder tempBuf = new IdcStringBuilder();
/*     */       try
/*     */       {
/* 325 */         StringUtils.appendAsHex(tempBuf, fileName);
/* 326 */         fileName = tempBuf.toString();
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 331 */         Report.trace("indexer", null, ignore);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 336 */     String extension = null;
/* 337 */     if (isRename)
/*     */     {
/* 339 */       extension = props.getProperty("indexerMapExtension");
/*     */     }
/*     */     else
/*     */     {
/* 343 */       extension = "txt";
/*     */     }
/*     */ 
/* 347 */     String outputFilePath = outputDir + fileName + "." + extension;
/*     */ 
/* 349 */     File outputFile = new File(outputFilePath);
/* 350 */     if (outputFile.exists())
/*     */     {
/* 352 */       outputFile.delete();
/*     */     }
/*     */ 
/* 355 */     return outputFilePath;
/*     */   }
/*     */ 
/*     */   public void copyFileWithNewExtension(String inputFilePath, String outputFilePath)
/*     */     throws ServiceException
/*     */   {
/* 361 */     FileUtils.copyFile(inputFilePath, outputFilePath);
/*     */   }
/*     */ 
/*     */   public void setTifFileValues(IndexerInfo ii, Properties props, String filePath, TaskInfo ti)
/*     */   {
/* 366 */     if (ti == null) {
/*     */       return;
/*     */     }
/* 369 */     IndexerTaskWorkImpl tw = new IndexerTaskWorkImpl();
/* 370 */     tw.init(ii, props, filePath, this.m_clearFileIfUnsuccessful);
/* 371 */     ti.m_worker = tw;
/*     */   }
/*     */ 
/*     */   public void convertDocuments(Vector indexerInfoList, Hashtable docProperties, ExecutionContext ctxt)
/*     */   {
/* 377 */     Report.trace(null, "convertDocuments in TextConversionHandler is not implemented.", null);
/*     */   }
/*     */ 
/*     */   public void cleanUp() throws ServiceException
/*     */   {
/* 382 */     if (!this.m_initSuccess)
/*     */     {
/* 385 */       return;
/*     */     }
/* 387 */     if ((this.m_data.m_debugLevel != null) && (!this.m_data.m_debugLevel.equalsIgnoreCase("none")))
/*     */       return;
/* 389 */     FileUtils.deleteDirectory(new File(this.m_outputDir), false);
/*     */   }
/*     */ 
/*     */   public void finish()
/*     */   {
/* 398 */     if (!this.m_initSuccess)
/*     */       return;
/* 400 */     List taskInfoList = (List)this.m_data.getCachedObject("tiList");
/* 401 */     int guardTimeout = SharedObjects.getEnvironmentInt("IndexerTextExtractionGuardTimeout", 15);
/* 402 */     if ((taskInfoList == null) || (taskInfoList.size() <= 0))
/*     */       return;
/* 404 */     Report.trace("indexer", "TextConversionHandler.finish() waiting for " + taskInfoList.size() + " items to finish processing", null);
/* 405 */     synchronized (taskInfoList)
/*     */     {
/* 407 */       Iterator iter = taskInfoList.iterator();
/* 408 */       while (iter.hasNext())
/*     */       {
/* 410 */         TaskInfo ti = (TaskInfo)iter.next();
/* 411 */         ti.m_startTime = System.currentTimeMillis();
/*     */ 
/* 413 */         synchronized (ti)
/*     */         {
/* 415 */           int waitCount = 0;
/* 416 */           while ((!ti.m_isFinished) && (!SystemUtils.m_isServerStopped))
/*     */           {
/*     */             try
/*     */             {
/* 420 */               ti.wait(5000L);
/* 421 */               ++waitCount;
/* 422 */               if (SystemUtils.m_verbose)
/*     */               {
/* 424 */                 Report.trace("indexer", "TextConversionHandler.finish() wait count<" + waitCount + "> for taskinfo" + ti, null);
/*     */               }
/*     */             }
/*     */             catch (Throwable ignore)
/*     */             {
/*     */             }
/*     */ 
/* 431 */             long currentTime = System.currentTimeMillis();
/* 432 */             if (((waitCount * 5 >= guardTimeout) && (guardTimeout > 0)) || ((currentTime - ti.m_startTime) / 1000L > guardTimeout))
/*     */             {
/* 434 */               if (SystemUtils.m_verbose)
/*     */               {
/* 436 */                 Report.trace("indexer", "TextConversionHandler.finish() giving up waiting for " + ti, null);
/*     */               }
/* 438 */               ti.m_isFinished = true;
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 444 */       this.m_data.setCachedObject("tiList", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean IsFormatSupported(Properties prop)
/*     */   {
/* 455 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 460 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104409 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.TextConversionHandler
 * JD-Core Version:    0.5.4
 */