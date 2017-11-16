/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.StringBufferOutputStream;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.PrintStream;
/*     */ import java.io.RandomAccessFile;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProgressState
/*     */   implements ReportProgress
/*     */ {
/*     */   protected long m_startTs;
/*     */   protected long m_lastTraceTs;
/*     */   protected long m_lastWriteTs;
/*     */   protected Vector m_messages;
/*     */   protected DataBinder m_binder;
/*     */   protected boolean m_isSaveToFile;
/*     */   protected String m_dir;
/*     */   protected String m_traceFilePath;
/*     */   protected String m_stateFile;
/*     */   protected int m_traceIntervalInSeconds;
/*     */   protected int m_writeInSeconds;
/*     */   protected boolean m_isTraceAll;
/*     */ 
/*     */   public ProgressState()
/*     */   {
/*  75 */     this.m_binder = new DataBinder();
/*  76 */     this.m_messages = new IdcVector();
/*     */ 
/*  79 */     this.m_binder.setFieldType("startTs", "date");
/*  80 */     this.m_binder.setFieldType("finishTs", "date");
/*  81 */     this.m_binder.setFieldType("lastTraceTs", "date");
/*  82 */     this.m_binder.setFieldType("lastWriteTs", "date");
/*     */ 
/*  84 */     this.m_isSaveToFile = false;
/*  85 */     this.m_isTraceAll = true;
/*     */   }
/*     */ 
/*     */   public void init(String name)
/*     */   {
/*  91 */     Map args = getProgressStateConfig(name);
/*  92 */     if (args == null)
/*     */     {
/*  94 */       args = new HashMap();
/*  95 */       if (SystemUtils.m_verbose)
/*     */       {
/*  97 */         Report.debug(null, "The progress state trace for " + name + " is not defined.", null);
/*     */       }
/*     */     }
/* 100 */     init(args);
/*     */   }
/*     */ 
/*     */   public void init(Map<String, String> args)
/*     */   {
/* 105 */     String prefix = (String)args.get("psPrefix");
/* 106 */     if (prefix == null)
/*     */     {
/* 108 */       prefix = "";
/*     */     }
/*     */ 
/* 111 */     this.m_traceIntervalInSeconds = NumberUtils.parseInteger(SharedObjects.getEnvironmentValue(prefix + "TraceIntervalInSeconds"), 5);
/*     */ 
/* 113 */     this.m_writeInSeconds = NumberUtils.parseInteger(SharedObjects.getEnvironmentValue(prefix + "WriteIntervalInSeconds"), 30);
/*     */ 
/* 115 */     this.m_isTraceAll = StringUtils.convertToBool(SharedObjects.getEnvironmentValue(prefix + "TraceAll"), true);
/*     */ 
/* 118 */     String dir = SharedObjects.getEnvironmentValue(prefix + "ProgressDirectory");
/* 119 */     if (dir == null)
/*     */     {
/* 121 */       dir = (String)args.get("ProgressDirectory");
/*     */     }
/* 123 */     if (dir == null)
/*     */     {
/* 126 */       this.m_isSaveToFile = false;
/* 127 */       return;
/*     */     }
/*     */ 
/* 130 */     String filename = (String)args.get("TraceFileName");
/* 131 */     if (filename == null)
/*     */     {
/* 133 */       filename = prefix.toLowerCase() + "trace.log";
/*     */     }
/* 135 */     this.m_stateFile = ((String)args.get("StateFileName"));
/* 136 */     if (this.m_stateFile == null)
/*     */     {
/* 138 */       this.m_stateFile = "state.hda";
/*     */     }
/*     */     try
/*     */     {
/* 142 */       this.m_dir = PathUtils.substitutePathVariables(dir, SharedObjects.getSecureEnvironment(), null, PathUtils.F_VARS_MUST_EXIST, null);
/*     */ 
/* 144 */       this.m_dir = FileUtils.directorySlashes(this.m_dir);
/* 145 */       this.m_traceFilePath = FileUtils.getAbsolutePath(this.m_dir, filename);
/*     */ 
/* 148 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_dir, 3, false);
/*     */ 
/* 151 */       this.m_binder.putLocal("TraceDirectory", this.m_dir);
/* 152 */       this.m_binder.putLocal("TraceFile", filename);
/* 153 */       this.m_binder.putLocal("TraceIntervalInSeconds", "" + this.m_traceIntervalInSeconds);
/* 154 */       this.m_binder.putLocal("WriteIntervalInSeconds", "" + this.m_writeInSeconds);
/*     */ 
/* 157 */       this.m_isSaveToFile = true;
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 161 */       Report.warning(null, "Unable to instantiate the progress state object.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Map getProgressStateConfig(String name)
/*     */   {
/* 167 */     Map args = null;
/*     */     try
/*     */     {
/* 171 */       DataResultSet drset = SharedObjects.requireTable("ProgressStateInfo");
/* 172 */       int index = ResultSetUtils.getIndexMustExist(drset, "psName");
/* 173 */       Vector row = drset.findRow(index, name);
/* 174 */       args = new HashMap();
/* 175 */       if (row != null)
/*     */       {
/* 177 */         args = drset.getCurrentRowMap();
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 182 */       Report.warning(null, "Unable to instantiate the progress state object for " + name + ".", e);
/*     */     }
/*     */ 
/* 185 */     return args;
/*     */   }
/*     */ 
/*     */   public void start()
/*     */   {
/* 190 */     this.m_binder.putLocal("isActive", "1");
/* 191 */     this.m_startTs = System.currentTimeMillis();
/* 192 */     this.m_messages = new IdcVector();
/* 193 */     this.m_binder.putLocal("lastErrorMsg", "");
/* 194 */     this.m_binder.putLocal("startTs", LocaleUtils.formatODBC(new Date()));
/* 195 */     this.m_binder.putLocal("lastState", "wwStarted");
/*     */ 
/* 198 */     if ((!this.m_isSaveToFile) || (this.m_traceFilePath == null))
/*     */       return;
/* 200 */     boolean isLocked = false;
/*     */     try
/*     */     {
/* 203 */       FileUtils.reserveDirectory(this.m_dir);
/* 204 */       isLocked = true;
/* 205 */       int r = FileUtils.checkFile(this.m_traceFilePath, true, false);
/* 206 */       if (r == 0)
/*     */       {
/* 208 */         FileUtils.renameFile(this.m_traceFilePath, this.m_traceFilePath + ".tmp");
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 213 */       Report.error("system", e, "csUnableToRenameProgressFileToTmp", new Object[] { this.m_traceFilePath });
/*     */     }
/*     */     finally
/*     */     {
/* 217 */       if (isLocked)
/*     */       {
/* 219 */         FileUtils.releaseDirectory(this.m_dir);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean checkReport(int type, Throwable t)
/*     */   {
/* 229 */     boolean isReport = (t != null) || (type == -1) || (type == 4) || (type == 2);
/* 230 */     long curTs = System.currentTimeMillis();
/* 231 */     if ((curTs - this.m_lastTraceTs > 5000L) || (this.m_isTraceAll))
/*     */     {
/* 234 */       this.m_lastTraceTs = curTs;
/* 235 */       this.m_binder.putLocal("lastTraceTs", LocaleUtils.formatODBC(new Date()));
/* 236 */       isReport = true;
/*     */     }
/* 238 */     return isReport;
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, Throwable t, String key, Object[] args)
/*     */   {
/* 243 */     boolean isReport = checkReport(type, t);
/* 244 */     if (!isReport)
/*     */       return;
/* 246 */     IdcMessage idcMsg = IdcMessageFactory.lc(key, args);
/* 247 */     String msg = LocaleUtils.encodeMessage(idcMsg);
/* 248 */     report(type, t, msg);
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, Throwable t)
/*     */   {
/* 254 */     boolean isReport = checkReport(type, t);
/* 255 */     if (!isReport)
/*     */       return;
/* 257 */     report(type, t, msg);
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, float amtDone, float max)
/*     */   {
/* 270 */     msg = StringUtils.createReportProgressString(type, msg, amtDone, max);
/* 271 */     reportProgress(type, msg, null);
/*     */   }
/*     */ 
/*     */   public synchronized void report(int type, Throwable t, String msg)
/*     */   {
/* 276 */     if (type == 4)
/*     */     {
/* 278 */       start();
/*     */     }
/* 280 */     String dteStr = LocaleUtils.formatODBC(new Date());
/* 281 */     if (type == 2)
/*     */     {
/* 283 */       this.m_binder.putLocal("finishTs", dteStr);
/* 284 */       this.m_binder.putLocal("isActive", "0");
/*     */     }
/*     */ 
/* 287 */     createAndAddMessage(type, msg, t);
/* 288 */     if (!this.m_isSaveToFile)
/*     */       return;
/* 290 */     long curTs = System.currentTimeMillis();
/* 291 */     if ((curTs - this.m_lastWriteTs < this.m_writeInSeconds * 1000) && (type != -1) && (type != 4) && (type != 2)) {
/*     */       return;
/*     */     }
/* 294 */     this.m_binder.putLocal("lastWriteTs", dteStr);
/* 295 */     RandomAccessFile file = null;
/*     */     try
/*     */     {
/* 298 */       FileUtils.reserveDirectory(this.m_dir);
/*     */ 
/* 301 */       ResourceUtils.serializeDataBinder(this.m_dir, this.m_stateFile, this.m_binder, true, false);
/*     */ 
/* 304 */       file = new RandomAccessFile(this.m_traceFilePath, "rw");
/* 305 */       long length = file.length();
/* 306 */       if (length == 0L)
/*     */       {
/* 308 */         file.write(FileUtils.UTF8_SIGNATURE);
/*     */       }
/*     */       else
/*     */       {
/* 312 */         file.seek(length);
/*     */       }
/* 314 */       int size = this.m_messages.size();
/* 315 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 317 */         String m = (String)this.m_messages.get(i);
/* 318 */         m = m + "\n";
/* 319 */         byte[] bytes = m.getBytes("UTF8");
/* 320 */         file.write(bytes);
/*     */       }
/* 322 */       this.m_messages.clear();
/* 323 */       this.m_lastWriteTs = curTs;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 327 */       Report.trace(null, "Failed to log start up install message: " + msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 331 */       FileUtils.closeObject(file);
/* 332 */       FileUtils.releaseDirectory(this.m_dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void createAndAddMessage(int type, String errMsg, Throwable t)
/*     */   {
/* 340 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 341 */     StringBufferOutputStream sbos = new StringBufferOutputStream(buf);
/* 342 */     buf.m_disableToStringReleaseBuffers = true;
/*     */ 
/* 345 */     Date d = new Date();
/* 346 */     String sd = LocaleUtils.debugDate(d);
/* 347 */     buf.append(sd);
/* 348 */     if (errMsg != null)
/*     */     {
/* 350 */       buf.append('\t');
/* 351 */       buf.append(errMsg);
/*     */     }
/* 353 */     if (t != null)
/*     */     {
/* 355 */       buf.append("!$");
/* 356 */       PrintStream printStream = new PrintStream(sbos);
/* 357 */       if (t instanceof StackTrace)
/*     */       {
/* 359 */         sbos.m_skipUntil = '\t';
/*     */       }
/* 361 */       t.printStackTrace(printStream);
/*     */     }
/* 363 */     String msg = buf.toString();
/* 364 */     buf.releaseBuffers();
/*     */ 
/* 366 */     this.m_messages.add(msg);
/* 367 */     if (errMsg == null)
/*     */     {
/* 369 */       this.m_binder.removeLocal("lastTraceMsg");
/*     */     }
/*     */     else
/*     */     {
/* 373 */       this.m_binder.putLocal("lastTraceMsg", errMsg);
/*     */     }
/* 375 */     this.m_binder.putLocal("lastTraceTs", LocaleUtils.formatODBC(d));
/*     */ 
/* 377 */     if (type != -1)
/*     */       return;
/* 379 */     this.m_binder.putLocal("lastErrorMsg", msg);
/*     */   }
/*     */ 
/*     */   public void setStateValue(String key, String value)
/*     */   {
/* 385 */     this.m_binder.putLocal(key, value);
/*     */   }
/*     */ 
/*     */   public void updateState(DataBinder binder)
/*     */   {
/* 390 */     this.m_binder.merge(binder);
/*     */   }
/*     */ 
/*     */   public List getMessages()
/*     */   {
/* 395 */     return (List)this.m_messages.clone();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 400 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87582 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ProgressState
 * JD-Core Version:    0.5.4
 */