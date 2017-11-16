/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import java.io.File;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class DocProfileUtils
/*     */ {
/* 138 */   protected static String[] m_envReservedStrings = { "dUser", "monitoredSubjects", "refreshSubjects", "changedSubjects", "watchedMonikers", "refreshMonikers", "changedMonikers", "refreshSubMonikers", "forceLogin", "Auth", "monitoredTopics", "refreshTopics", "changedTopics", "subjectNotifyChanged", "monikerNotifyChanged", "topicNotifyChanged", "CurrentArchiverStatus", "GetCurrentArchiverStatus", "GetCurrentIndexingStatus", "NoHttpHeaders", "StatusCode", "StatusMessageKey", "StatusMessage", "ErrorStackTrace", "IsNew", "ClientEncoding", "IsJava", "IdcService" };
/*     */ 
/* 148 */   protected static String[] m_envReservedTables = { "UpdatedUserTopics", "EnterpriseSearch" };
/*     */ 
/*     */   public static String getDocumentDir()
/*     */   {
/*  34 */     return LegacyDirectoryLocator.getAppDataDirectory() + "profiles/document/";
/*     */   }
/*     */ 
/*     */   public static long writeListingFile(String dir, String name, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  43 */     return writeFile(dir, name, binder);
/*     */   }
/*     */ 
/*     */   public static DataBinder readListingFile(String dir, String name, boolean isLock)
/*     */     throws ServiceException
/*     */   {
/*  49 */     return readFile(dir, name, null);
/*     */   }
/*     */ 
/*     */   public static long writeDefinitionFile(String dir, String name, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/*  55 */     long result = writeFile(dir, name, binder);
/*  56 */     return result;
/*     */   }
/*     */ 
/*     */   public static DataBinder readDefinitionFile(String dir, String name, boolean isLock)
/*     */     throws ServiceException
/*     */   {
/*  62 */     String lockDir = null;
/*  63 */     if (isLock)
/*     */     {
/*  65 */       lockDir = dir;
/*     */     }
/*  67 */     return readFile(dir, name, lockDir);
/*     */   }
/*     */ 
/*     */   public static DataBinder readFile(String dir, String filename, String lockDir)
/*     */     throws ServiceException
/*     */   {
/*  73 */     DataBinder binder = new DataBinder(true);
/*  74 */     boolean exists = false;
/*  75 */     if (lockDir != null)
/*     */     {
/*  77 */       FileUtils.reserveDirectory(lockDir);
/*     */     }
/*     */     try
/*     */     {
/*  81 */       filename = filename + ".hda";
/*  82 */       exists = ResourceUtils.serializeDataBinder(dir, filename, binder, false, false);
/*     */     }
/*     */     finally
/*     */     {
/*  86 */       if (lockDir != null)
/*     */       {
/*  88 */         FileUtils.releaseDirectory(lockDir);
/*     */       }
/*  90 */       binder.putLocal("defFileExists", "" + exists);
/*     */     }
/*  92 */     return binder;
/*     */   }
/*     */ 
/*     */   public static long writeFile(String dir, String filename, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  99 */     filename = filename + ".hda";
/* 100 */     if (binder != null)
/*     */     {
/* 102 */       removeServerCruft(binder);
/* 103 */       ResourceUtils.serializeDataBinder(dir, filename, binder, true, false);
/*     */     }
/*     */ 
/* 106 */     File f = new File(dir, filename);
/* 107 */     return f.lastModified();
/*     */   }
/*     */ 
/*     */   public static long checkFile(String dir, String name)
/*     */   {
/*     */     try
/*     */     {
/* 114 */       name = name + ".hda";
/* 115 */       File f = new File(dir, name);
/* 116 */       return f.lastModified();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 120 */       Report.trace(null, "DocProfileUtils.checkFile: file=" + dir + "/" + name, e);
/* 121 */     }return -2L;
/*     */   }
/*     */ 
/*     */   public static void deleteDefinitionFile(String dir, String name, DataBinder data)
/*     */     throws ServiceException
/*     */   {
/* 129 */     String filename = dir + name + ".hda";
/*     */ 
/* 132 */     data.addTempFile(filename);
/*     */   }
/*     */ 
/*     */   public static void removeServerCruft(DataBinder binder)
/*     */   {
/* 152 */     String[] keys = m_envReservedStrings;
/* 153 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 155 */       binder.removeLocal(keys[i]);
/*     */     }
/* 157 */     Properties props = binder.getLocalData();
/* 158 */     Enumeration en = props.keys();
/* 159 */     while (en.hasMoreElements())
/*     */     {
/* 161 */       String key = (String)en.nextElement();
/* 162 */       if (key.startsWith("_"))
/*     */       {
/* 164 */         binder.removeLocal(key);
/*     */       }
/*     */     }
/*     */ 
/* 168 */     String[] resultSets = m_envReservedTables;
/* 169 */     for (int i = 0; i < resultSets.length; ++i)
/*     */     {
/* 171 */       binder.removeResultSet(resultSets[i]);
/*     */     }
/*     */ 
/* 174 */     en = binder.m_optionLists.keys();
/* 175 */     while (en.hasMoreElements())
/*     */     {
/* 177 */       Object key = en.nextElement();
/* 178 */       binder.m_optionLists.remove(key);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 184 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98846 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocProfileUtils
 * JD-Core Version:    0.5.4
 */