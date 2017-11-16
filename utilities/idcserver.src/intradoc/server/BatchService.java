/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.serialize.DataBinderLocalizer;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.WriterToIdcAppendable;
/*     */ import intradoc.zip.ZipFunctions;
/*     */ import java.io.IOException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import java.util.zip.ZipEntry;
/*     */ import java.util.zip.ZipFile;
/*     */ 
/*     */ public class BatchService extends Service
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void executeCommands()
/*     */     throws ServiceException, DataException
/*     */   {
/*  51 */     String batchFile = this.m_binder.get("batchFile:path");
/*  52 */     if ((batchFile == null) || (batchFile.length() == 0))
/*     */     {
/*  54 */       throw new DataException("!csBatchFileMissing");
/*     */     }
/*  56 */     Vector tempFiles = this.m_binder.getTempFiles();
/*  57 */     String securityGroup = SharedObjects.getEnvironmentValue("LocalFileAccessSecurityGroup");
/*     */ 
/*  59 */     if (securityGroup == null)
/*     */     {
/*  61 */       securityGroup = "Secure";
/*     */     }
/*  63 */     int priv = SecurityUtils.determineGroupPrivilege(this.m_userData, securityGroup);
/*  64 */     if (((priv & 0x8) == 0) && (tempFiles.indexOf(batchFile) == -1))
/*     */     {
/*  67 */       throw new DataException("!csBatchFileMissing");
/*     */     }
/*     */ 
/*  71 */     ZipFile zipFile = null;
/*     */     try
/*     */     {
/*  74 */       Hashtable zipEntries = new Hashtable();
/*     */       try
/*     */       {
/*  77 */         zipFile = ZipFunctions.readZipFile(batchFile, zipEntries);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*  81 */         createServiceException(e, LocaleUtils.encodeMessage("csUnableToReadZip", null, batchFile));
/*     */       }
/*     */ 
/*  86 */       ZipEntry manEntry = (ZipEntry)zipEntries.get("manifest.hda");
/*  87 */       DataBinder manData = ZipFunctions.extractFileAsDataBinder(zipFile, manEntry, "manifest.hda");
/*     */ 
/*  91 */       DataResultSet cmdSet = (DataResultSet)manData.getResultSet("CommandFiles");
/*  92 */       if (cmdSet == null)
/*     */       {
/*  94 */         throw new DataException("!csCommandFilesMissing");
/*     */       }
/*     */ 
/*  98 */       DataResultSet rset = new DataResultSet(new String[] { "name", "location", "result" });
/*  99 */       rset.merge(null, cmdSet, false);
/* 100 */       this.m_binder.addResultSet("Commands", rset);
/*     */ 
/* 102 */       FieldInfo[] finfos = ResultSetUtils.createInfoList(cmdSet, new String[] { "name", "location" }, true);
/* 103 */       int nameIndex = finfos[0].m_index;
/* 104 */       int locIndex = finfos[1].m_index;
/*     */ 
/* 106 */       boolean isAbort = false;
/* 107 */       int count = 0;
/* 108 */       for (cmdSet.first(); cmdSet.isRowPresent(); ++count)
/*     */       {
/* 110 */         String location = cmdSet.getStringValue(locIndex);
/* 111 */         IdcMessage errMsg = null;
/* 112 */         DataBinder cmdBinder = null;
/* 113 */         location = location.toLowerCase();
/* 114 */         ZipEntry cmdEntry = (ZipEntry)zipEntries.get(location.toLowerCase());
/* 115 */         if (cmdEntry == null)
/*     */         {
/* 117 */           isAbort = true;
/* 118 */           errMsg = IdcMessageFactory.lc("csCommandFileMissingInZip", new Object[] { location, batchFile });
/* 119 */           rset.setCurrentValue(2, LocaleUtils.encodeMessage(errMsg));
/*     */         }
/*     */         else {
/* 122 */           cmdBinder = ZipFunctions.extractFileAsDataBinder(zipFile, cmdEntry, location);
/*     */           try
/*     */           {
/* 127 */             prepareCommand(cmdBinder, zipFile, zipEntries);
/* 128 */             executeCommand(cmdBinder);
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 134 */             errMsg = IdcMessageFactory.lc(e);
/* 135 */             isAbort = true;
/*     */           }
/*     */ 
/* 141 */           String str = makeResultString(cmdBinder, LocaleUtils.encodeMessage(errMsg));
/* 142 */           rset.setCurrentValue(2, str);
/* 143 */           if (isAbort)
/*     */           {
/* 145 */             this.m_binder.putLocal("isAborted", "1");
/* 146 */             this.m_binder.putLocal("abortedCommandName", cmdSet.getStringValue(nameIndex));
/* 147 */             this.m_binder.putLocal("abortedCommandNum", String.valueOf(++count));
/* 148 */             break;
/*     */           }
/*     */         }
/* 108 */         cmdSet.next(); rset.next();
/*     */       }
/*     */ 
/* 152 */       this.m_binder.putLocal("commandsExecuted", String.valueOf(count));
/*     */     }
/*     */     finally
/*     */     {
/* 156 */       FileUtils.closeObject(zipFile);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void prepareCommand(DataBinder cmdBinder, ZipFile zipFile, Hashtable entries)
/*     */     throws ServiceException
/*     */   {
/* 168 */     Properties env = this.m_binder.getEnvironment();
/* 169 */     cmdBinder.setEnvironment(env);
/*     */ 
/* 172 */     Properties props = cmdBinder.getLocalData();
/* 173 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*     */     {
/* 175 */       String key = (String)en.nextElement();
/* 176 */       if (key.endsWith(":path"))
/*     */       {
/* 178 */         String value = props.getProperty(key);
/* 179 */         String fileName = computeAndSetFilePath(cmdBinder, key, value);
/* 180 */         ZipEntry entry = (ZipEntry)entries.get(value);
/* 181 */         ZipFunctions.extractFileFromZip(zipFile, entry, value, fileName);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String computeAndSetFilePath(DataBinder cmdBinder, String key, String path)
/*     */   {
/* 188 */     String tempDir = DataBinder.getTemporaryDirectory();
/* 189 */     long count = DataBinder.getNextFileCounter();
/*     */ 
/* 191 */     path = FileUtils.fileSlashes(path);
/*     */ 
/* 193 */     int index = path.lastIndexOf(46);
/* 194 */     if (index < 0)
/*     */     {
/* 196 */       index = path.length();
/*     */     }
/*     */ 
/* 199 */     String name = tempDir + Long.toString(count) + path.substring(index);
/* 200 */     cmdBinder.putLocal(key, name);
/* 201 */     cmdBinder.addTempFile(name);
/*     */ 
/* 203 */     return name;
/*     */   }
/*     */ 
/*     */   protected void executeCommand(DataBinder binder) throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 210 */       String cmd = binder.getLocal("IdcService");
/* 211 */       this.m_requestImplementor.executeServiceTopLevelSimple(binder, cmd, this.m_userData);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 215 */       if (binder.getLocal("StatusCode") != null)
/*     */         return;
/* 217 */       binder.putLocal("StatusCode", String.valueOf(e.m_errorCode));
/* 218 */       binder.putLocal("StatusMessageKey", e.getMessage());
/* 219 */       binder.putLocal("StatusMessage", e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String makeResultString(DataBinder binder, String errMsg)
/*     */   {
/* 231 */     if (binder == null)
/*     */     {
/* 233 */       binder = new DataBinder();
/*     */     }
/*     */ 
/* 236 */     if ((errMsg != null) && (errMsg.length() > 0))
/*     */     {
/* 238 */       binder.putLocal("StatusMessageKey", errMsg);
/* 239 */       binder.putLocal("StatusMessage", errMsg);
/* 240 */       binder.putLocal("StatusCode", "-1");
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 245 */       DataBinderLocalizer localizer = new DataBinderLocalizer(binder, this);
/* 246 */       localizer.localizeBinder(3);
/*     */ 
/* 248 */       IdcStringBuilder builder = new IdcStringBuilder();
/* 249 */       binder.sendEx(new WriterToIdcAppendable(builder), false);
/*     */ 
/* 251 */       return builder.toString();
/*     */     }
/*     */     catch (IOException e) {
/*     */     }
/* 255 */     return "!csUnableToParseResultBinder";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 261 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98038 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.BatchService
 * JD-Core Version:    0.5.4
 */