/*     */ package intradoc.refinery.convert;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Writer;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ExsimpleDriver
/*     */ {
/*     */   protected ExecutionContext m_cxt;
/*  46 */   protected DataResultSet m_optionsTable = null;
/*  47 */   protected IdcProperties m_props = null;
/*  48 */   protected IdcProperties m_cleanResourceProps = null;
/*     */   protected String m_exportResourcePath;
/*  50 */   protected String m_exportStdErr = "";
/*  51 */   protected String m_exportStdOut = "";
/*     */   protected String m_exportId;
/*     */   protected String m_workingDir;
/*  54 */   protected IdcStringBuilder m_cfgBuf = null;
/*     */   protected String m_traceSection;
/*     */   protected String m_jobId;
/*     */ 
/*     */   public ExsimpleDriver()
/*     */   {
/*     */   }
/*     */ 
/*     */   public ExsimpleDriver(ExecutionContext cxt)
/*     */   {
/*  67 */     this.m_cxt = cxt;
/*  68 */     this.m_traceSection = ConverterLaunch.loadAndReportVarFromContext(cxt, "ConverterLaunchTraceSection", "refinery");
/*     */   }
/*     */ 
/*     */   public void init(String jobId)
/*     */   {
/*  73 */     this.m_optionsTable = SharedObjects.getTable("ix_OIExportOptions");
/*  74 */     this.m_props = new IdcProperties();
/*  75 */     this.m_jobId = jobId;
/*  76 */     this.m_workingDir = (DataBinder.getTemporaryDirectory() + "thumbnail/" + this.m_jobId + "/");
/*  77 */     this.m_exportResourcePath = (this.m_workingDir + "ix.cfg");
/*     */   }
/*     */ 
/*     */   public String getWorkingDir()
/*     */   {
/*  82 */     return this.m_workingDir;
/*     */   }
/*     */ 
/*     */   public void setExportId(String id)
/*     */   {
/*  87 */     this.m_exportId = id;
/*  88 */     this.m_exportResourcePath = (this.m_workingDir + this.m_exportId + ".cfg");
/*  89 */     this.m_optionsTable = SharedObjects.getTable(id + "_OIExportOptions");
/*  90 */     if (this.m_optionsTable != null)
/*     */       return;
/*  92 */     Report.trace(null, "Using OI resources in non thread-safe manner", null);
/*     */   }
/*     */ 
/*     */   public void writeIXResourceForConversion(String height, String width, String pageSource, String format)
/*     */     throws ServiceException, IOException
/*     */   {
/*  99 */     FileUtils.checkOrCreateDirectory(this.m_workingDir, 3);
/* 100 */     setExportId("ix");
/* 101 */     format = format.toUpperCase();
/* 102 */     if (format.equals("JPG"))
/*     */     {
/* 104 */       format = "JPEG";
/*     */     }
/* 106 */     format = "FI_" + format;
/*     */ 
/* 108 */     loadResource();
/*     */ 
/* 110 */     this.m_props.put("outputid", format);
/*     */ 
/* 112 */     if ((pageSource != null) && (!pageSource.equals("0")))
/*     */     {
/* 114 */       this.m_props.put("exportstartpage", pageSource);
/* 115 */       this.m_props.put("exportendpage", pageSource);
/* 116 */       this.m_props.put("whattoexport", "range");
/*     */     }
/*     */ 
/* 119 */     this.m_props.put("graphicheightlimit", height);
/* 120 */     this.m_props.put("graphicwidthlimit", width);
/*     */ 
/* 122 */     writeResource();
/*     */   }
/*     */ 
/*     */   public String getProcessStdErr()
/*     */   {
/* 127 */     return this.m_exportStdErr;
/*     */   }
/*     */ 
/*     */   public String getProcessStdOut()
/*     */   {
/* 132 */     return this.m_exportStdOut;
/*     */   }
/*     */ 
/*     */   public int executeEngine(String srcFile, String convertFile, long timeout)
/*     */     throws ServiceException
/*     */   {
/* 138 */     return executeEngine(srcFile, convertFile, null, 0L, timeout, 0L);
/*     */   }
/*     */ 
/*     */   public int executeEngine(String srcFile, String convertFile, String msg, long min, long max, long factor)
/*     */     throws ServiceException
/*     */   {
/* 144 */     ExsimpleHelper helper = getHelper();
/* 145 */     if (helper != null)
/*     */     {
/* 147 */       srcFile = FileUtils.fileSlashes(srcFile);
/* 148 */       convertFile = FileUtils.fileSlashes(convertFile);
/* 149 */       this.m_exportResourcePath = FileUtils.fileSlashes(this.m_exportResourcePath);
/* 150 */       String engineParams = "\"" + srcFile + "\" \"" + convertFile + "\" \"" + this.m_exportResourcePath + "\"";
/* 151 */       if (helper.m_doDumpCfg)
/*     */       {
/* 153 */         engineParams = engineParams + " \"-p\"";
/*     */       }
/* 155 */       Report.trace(this.m_traceSection, "Launching exsimple with: " + engineParams, null);
/*     */ 
/* 157 */       ConverterLaunch launch = new ConverterLaunch(this.m_cxt, helper);
/* 158 */       launch.setParameterInfo(engineParams);
/* 159 */       launch.setTimeOuts(min, max, factor, srcFile);
/*     */ 
/* 161 */       launch.execute();
/* 162 */       this.m_exportStdErr = launch.getStdErrText();
/* 163 */       if (this.m_exportStdErr == null)
/*     */       {
/* 165 */         this.m_exportStdErr = "";
/*     */       }
/* 167 */       this.m_exportStdOut = launch.getStdInText();
/* 168 */       if (this.m_exportStdOut == null)
/*     */       {
/* 170 */         this.m_exportStdOut = "";
/*     */       }
/*     */ 
/* 174 */       int procRetVal = launch.getExitValue();
/* 175 */       String F_ERR_TOKEN = "failed: ";
/* 176 */       int failedIndx = this.m_exportStdErr.indexOf("failed: ");
/* 177 */       if ((procRetVal != 0) && (failedIndx > 0))
/*     */       {
/* 179 */         String errMsg = this.m_exportStdErr.substring(failedIndx + "failed: ".length());
/* 180 */         this.m_exportStdErr = errMsg.trim();
/*     */       }
/* 182 */       File output = new File(convertFile);
/* 183 */       boolean validOutput = (output.exists()) && (output.length() > 0L);
/* 184 */       Report.trace(this.m_traceSection, "exsimple return value: " + procRetVal + "; out file: " + convertFile + "; isValid: " + validOutput, null);
/*     */ 
/* 187 */       if ((procRetVal == 0) && (!validOutput))
/*     */       {
/* 191 */         procRetVal = -1;
/*     */       }
/* 193 */       else if ((procRetVal < 0) && (validOutput))
/*     */       {
/* 196 */         procRetVal = 0;
/*     */       }
/* 198 */       return procRetVal;
/*     */     }
/* 200 */     return -1;
/*     */   }
/*     */ 
/*     */   protected ExsimpleHelper getHelper() throws ServiceException
/*     */   {
/* 205 */     ExsimpleHelper helper = (ExsimpleHelper)SharedObjects.getObject("ibrObjects", "ExsimpleHelper");
/* 206 */     if (helper == null)
/*     */     {
/* 208 */       helper = new ExsimpleHelper();
/* 209 */       boolean isSetup = helper.init(this.m_exportId);
/* 210 */       if (isSetup)
/*     */       {
/* 212 */         SharedObjects.putObject("ibrObjects", "ExsimpleHelper", helper);
/*     */       }
/*     */       else
/*     */       {
/* 216 */         this.m_exportStdErr = LocaleUtils.encodeMessage("csFileNotFoundErr", null, "exsimple");
/* 217 */         helper = null;
/*     */       }
/*     */     }
/*     */ 
/* 221 */     return helper;
/*     */   }
/*     */ 
/*     */   protected void buildPropsResource() throws IOException
/*     */   {
/* 226 */     if (this.m_cleanResourceProps == null)
/*     */     {
/* 228 */       this.m_cleanResourceProps = new IdcProperties();
/*     */     }
/*     */ 
/* 231 */     this.m_cleanResourceProps.put("tempdir", this.m_workingDir);
/*     */ 
/* 246 */     for (Enumeration e = this.m_props.keys(); e.hasMoreElements(); )
/*     */     {
/* 248 */       String prefixedKey = (String)e.nextElement();
/* 249 */       String cleanKey = prefixedKey;
/* 250 */       int indx = prefixedKey.indexOf(95);
/* 251 */       if (indx > 0)
/*     */       {
/* 253 */         cleanKey = prefixedKey.substring(indx + 1);
/*     */       }
/* 255 */       String value = SharedObjects.getEnvironmentValue(prefixedKey);
/* 256 */       if ((value == null) || (value.length() == 0))
/*     */       {
/* 259 */         value = this.m_props.getProperty(prefixedKey);
/*     */       }
/*     */ 
/* 262 */       String overrideVal = SharedObjects.getEnvironmentValue(cleanKey);
/* 263 */       boolean didOverride = false;
/* 264 */       if (overrideVal != null)
/*     */       {
/* 266 */         didOverride = true;
/* 267 */         value = overrideVal;
/*     */       }
/*     */ 
/* 270 */       if (value != null)
/*     */       {
/* 272 */         value = value.trim();
/*     */       }
/* 274 */       if ((value != null) && (value.length() > 0))
/*     */       {
/* 276 */         this.m_cleanResourceProps.put(cleanKey, value);
/* 277 */         if (didOverride)
/*     */         {
/* 280 */           this.m_props.put(prefixedKey, overrideVal);
/* 281 */           if (SystemUtils.m_verbose)
/*     */           {
/* 283 */             Report.trace(this.m_traceSection, "export parameter '" + prefixedKey + "' was overridden with value: " + overrideVal, null);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/* 288 */       else if (SystemUtils.m_verbose)
/*     */       {
/* 290 */         Report.trace(this.m_traceSection, "export parameter '" + prefixedKey + "' was skipped, no value set", null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void writeResource() throws IOException
/*     */   {
/* 297 */     if (this.m_cfgBuf == null)
/*     */     {
/* 299 */       this.m_cfgBuf = new IdcStringBuilder();
/* 300 */       buildPropsResource();
/*     */     }
/*     */ 
/* 303 */     for (Enumeration e = this.m_cleanResourceProps.keys(); e.hasMoreElements(); )
/*     */     {
/* 305 */       String key = (String)e.nextElement();
/* 306 */       String value = this.m_cleanResourceProps.getProperty(key);
/* 307 */       if ((value != null) && (value.length() > 0))
/*     */       {
/* 309 */         String line = key + "\t" + value;
/* 310 */         this.m_cfgBuf.append(line);
/* 311 */         this.m_cfgBuf.append("\n");
/*     */       }
/*     */     }
/*     */ 
/* 315 */     if (SystemUtils.m_verbose)
/*     */     {
/* 317 */       Report.trace(this.m_traceSection, "contents of " + this.m_exportResourcePath + "\n" + this.m_cfgBuf.toStringNoRelease(), null);
/*     */     }
/*     */ 
/* 320 */     FileUtils.deleteFile(this.m_exportResourcePath);
/* 321 */     OutputStream out = new FileOutputStream(this.m_exportResourcePath);
/* 322 */     Writer writer = FileUtils.openDataWriterEx(out, "UTF8", 1);
/*     */     try
/*     */     {
/* 325 */       writer.write(this.m_cfgBuf.toString());
/*     */     }
/*     */     finally
/*     */     {
/* 329 */       FileUtils.closeObject(writer);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void loadResource() throws IOException
/*     */   {
/* 335 */     this.m_props = new IdcProperties();
/* 336 */     if (this.m_optionsTable == null)
/*     */       return;
/* 338 */     for (this.m_optionsTable.first(); this.m_optionsTable.isRowPresent(); this.m_optionsTable.next())
/*     */     {
/* 340 */       Map row = this.m_optionsTable.getCurrentRowMap();
/* 341 */       String key = (String)row.get("oikey");
/* 342 */       String def = (String)row.get("oidefault");
/* 343 */       this.m_props.put(key, def);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 350 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101713 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.convert.ExsimpleDriver
 * JD-Core Version:    0.5.4
 */