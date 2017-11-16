/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.LogDirInfo;
/*     */ import intradoc.common.LogWriter;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.io.RandomAccessFile;
/*     */ import java.util.Date;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class InstallLog
/*     */   implements LogWriter
/*     */ {
/*     */   protected static final int NOTICE = 0;
/*     */   protected static final int WARNING = 1;
/*     */   protected static final int ERROR = 2;
/*  44 */   protected static final String[] LOG_FILES = { "log.txt", "log.txt", "log.txt" };
/*     */ 
/*  48 */   protected static final String[] MESSAGE_LABELS = { "!csInstallerLogMsgNoticeLabel", "!csInstallerLogMsgWarningLabel", "!csInstallerLogMsgErrorLabel" };
/*     */ 
/*  55 */   protected Vector m_messages = new IdcVector();
/*  56 */   protected String m_logDirectory = null;
/*     */ 
/*  58 */   protected boolean m_hasWarnings = false;
/*  59 */   protected boolean m_hasErrors = false;
/*     */ 
/*  61 */   public String m_logInitMessage = null;
/*  62 */   protected boolean m_hasOutputMessages = false;
/*     */ 
/*  64 */   public boolean m_quiet = false;
/*     */   public PromptUser m_promptUser;
/*     */ 
/*     */   public InstallLog(PromptUser promptUser)
/*     */   {
/*  69 */     this.m_promptUser = promptUser;
/*  70 */     String msg = LocaleUtils.encodeMessage("csInstallerLogInitMsg", null, new Date());
/*     */ 
/*  72 */     setInitMessage(msg);
/*     */   }
/*     */ 
/*     */   public void setInitMessage(String initMessage)
/*     */   {
/*  77 */     this.m_logInitMessage = initMessage;
/*     */   }
/*     */ 
/*     */   public void setLogDirectory(String pathName) throws ServiceException
/*     */   {
/*  82 */     this.m_logDirectory = FileUtils.directorySlashes(pathName);
/*  83 */     FileUtils.checkOrCreateDirectory(this.m_logDirectory, 2);
/*     */ 
/*  85 */     Report.trace("install", "setting log directory to \"" + this.m_logDirectory + "\"", null);
/*     */ 
/*  87 */     Vector v = this.m_messages;
/*  88 */     this.m_messages = new IdcVector();
/*  89 */     int size = v.size();
/*  90 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  92 */       LogEntry entry = (LogEntry)v.elementAt(i);
/*  93 */       makeEntry(entry.m_severity, entry.m_message);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void notice(String message) throws ServiceException
/*     */   {
/*  99 */     makeEntry(0, message);
/*     */   }
/*     */ 
/*     */   public void warning(String message) throws ServiceException
/*     */   {
/* 104 */     makeEntry(1, message);
/* 105 */     this.m_hasWarnings = true;
/*     */   }
/*     */ 
/*     */   public void error(String message) throws ServiceException
/*     */   {
/* 110 */     makeEntry(2, message);
/* 111 */     this.m_hasErrors = true;
/*     */   }
/*     */ 
/*     */   public boolean hasWarnings()
/*     */   {
/* 116 */     return this.m_hasWarnings;
/*     */   }
/*     */ 
/*     */   public boolean hasErrors()
/*     */   {
/* 121 */     return this.m_hasErrors;
/*     */   }
/*     */ 
/*     */   protected String makeEntryString(int severity, String entry)
/*     */   {
/* 126 */     String errMsg = LocaleUtils.appendMessage(entry, MESSAGE_LABELS[severity]);
/* 127 */     errMsg = LocaleResources.localizeMessage(errMsg, null);
/* 128 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public void doMessageAppend(int messageType, String msg, LogDirInfo ldinfo, Throwable t)
/*     */   {
/* 137 */     int severity = 0;
/* 138 */     switch (messageType)
/*     */     {
/*     */     case 0:
/* 141 */       severity = 0;
/* 142 */       break;
/*     */     case 1:
/* 144 */       severity = 1;
/* 145 */       break;
/*     */     case 2:
/*     */     case 3:
/* 148 */       severity = 2;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 154 */       IdcMessage idcMsg = null;
/* 155 */       if (t != null)
/*     */       {
/* 157 */         idcMsg = IdcMessageFactory.lc(t);
/*     */       }
/* 159 */       if (msg != null)
/*     */       {
/* 161 */         idcMsg = IdcMessageFactory.lc(idcMsg, null, new Object[0]);
/* 162 */         idcMsg.m_msgEncoded = msg;
/*     */       }
/* 164 */       msg = LocaleUtils.encodeMessage(idcMsg);
/* 165 */       makeEntry(severity, msg);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 169 */       Report.trace(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void makeEntry(int severity, String entry) throws ServiceException
/*     */   {
/* 175 */     if (this.m_logDirectory == null)
/*     */     {
/* 177 */       Report.trace("install", "InstallLog queueing log message \"" + entry + "\" with severity " + MESSAGE_LABELS[severity] + ".", null);
/*     */ 
/* 179 */       this.m_messages.addElement(new LogEntry(severity, entry));
/*     */     }
/*     */     else
/*     */     {
/* 183 */       if (!this.m_hasOutputMessages)
/*     */       {
/* 185 */         this.m_hasOutputMessages = true;
/* 186 */         makeEntry(0, this.m_logInitMessage);
/*     */       }
/*     */ 
/* 189 */       RandomAccessFile file = null;
/*     */       try
/*     */       {
/* 193 */         file = new RandomAccessFile(this.m_logDirectory + LOG_FILES[severity], "rw");
/* 194 */         long length = file.length();
/* 195 */         if (length == 0L)
/*     */         {
/* 197 */           file.write(FileUtils.UTF8_SIGNATURE);
/*     */         }
/*     */         else
/*     */         {
/* 201 */           file.seek(length);
/*     */         }
/* 203 */         String errMsg = makeEntryString(severity, entry);
/* 204 */         if (SystemUtils.m_verbose)
/*     */         {
/* 206 */           SystemUtils.traceDumpException("install", "InstallLog logging message \"" + errMsg + "\".", new StackTrace(""));
/*     */         }
/*     */ 
/* 209 */         errMsg = errMsg + "\n";
/* 210 */         byte[] bytes = errMsg.getBytes("UTF8");
/* 211 */         file.write(bytes);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/*     */       }
/*     */       finally
/*     */       {
/* 219 */         FileUtils.closeObject(file);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void finalize()
/*     */     throws Throwable
/*     */   {
/* 227 */     int size = this.m_messages.size();
/* 228 */     if (size <= 0)
/*     */       return;
/* 230 */     String msg = LocaleResources.getString("csInstallerUnflushedMessagesHeader", null);
/*     */ 
/* 232 */     this.m_promptUser.outputMessage(msg);
/* 233 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 235 */       LogEntry entry = (LogEntry)this.m_messages.elementAt(i);
/* 236 */       msg = LocaleUtils.appendMessage(entry.m_message, MESSAGE_LABELS[entry.m_severity]);
/*     */ 
/* 238 */       msg = LocaleResources.localizeMessage(msg, null);
/* 239 */       this.m_promptUser.outputMessage(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 246 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.InstallLog
 * JD-Core Version:    0.5.4
 */