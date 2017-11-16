/*     */ package intradoc.server.project;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceCacheInfo;
/*     */ import intradoc.resource.ResourceCacheState;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProblemReportUtils
/*     */ {
/*     */   public static String m_directory;
/* 194 */   public static Object[][] STATE_LISTS = { { "prStates", { "OPEN", "FIXED", "CLOSED", "DEFERRED", "ENHANCEMENT" } }, { "prSeverities", { "MINOR", "SERIOUS", "STOPPER" } } };
/*     */ 
/*     */   public static void init()
/*     */   {
/*  43 */     m_directory = LegacyDirectoryLocator.getProjectDirectory();
/*     */   }
/*     */ 
/*     */   public static String buildDirectory(Parameters params, boolean isCheckOrCreate)
/*     */     throws DataException, ServiceException
/*     */   {
/*  49 */     String projectID = params.get("dProjectID");
/*  50 */     String dir = m_directory + projectID.toLowerCase() + "/reports/";
/*     */ 
/*  52 */     if (isCheckOrCreate)
/*     */     {
/*  54 */       FileUtils.checkOrCreateDirectory(dir, 2);
/*     */     }
/*     */ 
/*  57 */     return dir;
/*     */   }
/*     */ 
/*     */   public static String buildFilename(Parameters params) throws DataException
/*     */   {
/*  62 */     String prID = params.get("dPrID");
/*  63 */     String docName = params.get("dDocName");
/*     */ 
/*  65 */     String filename = docName.toLowerCase() + "_" + prID + ".hda";
/*  66 */     return filename;
/*     */   }
/*     */ 
/*     */   public static void createProblemReport(Parameters locParams, DataBinder prData)
/*     */     throws DataException, ServiceException
/*     */   {
/*  72 */     String dir = buildDirectory(locParams, true);
/*  73 */     String filename = buildFilename(locParams);
/*     */ 
/*  75 */     ResourceUtils.serializeDataBinder(dir, filename, prData, true, false);
/*     */ 
/*  77 */     File f = FileUtilsCfgBuilder.getCfgFile(dir + filename, "Project", false);
/*  78 */     long ts = f.lastModified();
/*  79 */     long curTime = System.currentTimeMillis();
/*  80 */     addTemporaryResourceCache(curTime, filename, prData, ts);
/*     */   }
/*     */ 
/*     */   public static DataBinder readProblemReport(Parameters params)
/*     */     throws ServiceException, DataException
/*     */   {
/*  86 */     return readProblemReportEx(params, true);
/*     */   }
/*     */ 
/*     */   public static DataBinder readProblemReportEx(Parameters locParams, boolean asClone)
/*     */     throws ServiceException, DataException
/*     */   {
/*  92 */     String dir = buildDirectory(locParams, true);
/*  93 */     String filename = buildFilename(locParams);
/*     */ 
/*  98 */     File prFile = FileUtilsCfgBuilder.getCfgFile(dir + filename, "Project", false);
/*  99 */     if (!prFile.exists())
/*     */     {
/* 102 */       String msg = LocaleUtils.encodeMessage("csProblemReportNoLongerExists", null, filename);
/*     */ 
/* 104 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 107 */     long ts = prFile.lastModified();
/*     */ 
/* 109 */     DataBinder prData = null;
/* 110 */     long curTime = System.currentTimeMillis();
/* 111 */     ResourceCacheInfo cacheInfo = ResourceCacheState.getTemporaryCache(filename, curTime);
/* 112 */     if ((cacheInfo != null) && 
/* 114 */       (ts != cacheInfo.m_lastLoaded))
/*     */     {
/* 116 */       prData = (DataBinder)cacheInfo.m_resourceObj;
/*     */     }
/*     */ 
/* 120 */     if (prData == null)
/*     */     {
/*     */       try
/*     */       {
/* 124 */         prData = ResourceUtils.readDataBinder(dir, filename);
/* 125 */         addTemporaryResourceCache(curTime, filename, prData, ts);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 129 */         String msg = LocaleUtils.encodeMessage("csProblemReportNoLongerExists", null, filename);
/*     */ 
/* 131 */         throw new ServiceException(msg, e);
/*     */       }
/*     */     }
/*     */ 
/* 135 */     return (asClone) ? cloneData(prData) : prData;
/*     */   }
/*     */ 
/*     */   public static void addTemporaryResourceCache(long curTime, String key, DataBinder binder, long ts)
/*     */   {
/* 140 */     ResourceCacheInfo cacheInfo = new ResourceCacheInfo(key);
/* 141 */     cacheInfo.m_resourceObj = binder;
/* 142 */     cacheInfo.m_lastLoaded = ts;
/* 143 */     cacheInfo.m_size = 10000L;
/* 144 */     ResourceCacheState.addTimedTemporaryCache(key, cacheInfo, curTime);
/*     */   }
/*     */ 
/*     */   public static DataBinder cloneData(DataBinder data)
/*     */   {
/* 150 */     DataBinder clone = new DataBinder();
/* 151 */     DataBinder.mergeHashTables(clone.getLocalData(), data.getLocalData());
/*     */ 
/* 153 */     Enumeration en = data.getResultSetList();
/* 154 */     while (en.hasMoreElements())
/*     */     {
/* 156 */       String name = (String)en.nextElement();
/* 157 */       DataResultSet drset = (DataResultSet)data.getResultSet(name);
/* 158 */       ResultSet clonedSet = drset.shallowClone();
/*     */ 
/* 160 */       clone.addResultSet(name, clonedSet);
/*     */     }
/* 162 */     return clone;
/*     */   }
/*     */ 
/*     */   public static void deleteProblemReport(Parameters params)
/*     */     throws DataException, ServiceException
/*     */   {
/* 168 */     String dir = buildDirectory(params, false);
/* 169 */     String filename = buildFilename(params);
/*     */ 
/* 171 */     File file = FileUtilsCfgBuilder.getCfgFile(dir + filename, "Project", false);
/* 172 */     if (file.exists())
/*     */     {
/*     */       try
/*     */       {
/* 176 */         file.delete();
/*     */       }
/*     */       catch (Throwable ignore)
/*     */       {
/* 180 */         if (SystemUtils.m_verbose)
/*     */         {
/* 182 */           Report.debug("system", null, ignore);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 188 */     ResourceCacheState.removeTemporaryCache(filename);
/*     */   }
/*     */ 
/*     */   public static void loadStateLists(DataBinder binder, Workspace ws, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 203 */     PluginFilters.filter("loadStateLists", ws, binder, ctxt);
/*     */ 
/* 205 */     for (int i = 0; i < STATE_LISTS.length; ++i)
/*     */     {
/* 207 */       String name = (String)STATE_LISTS[i][0];
/* 208 */       Vector v = SharedObjects.getOptList(name);
/* 209 */       if (v == null)
/*     */       {
/* 211 */         String[] strs = (String[])(String[])STATE_LISTS[i][1];
/* 212 */         v = new IdcVector();
/* 213 */         for (int j = 0; j < strs.length; ++j)
/*     */         {
/* 215 */           v.addElement(strs[j]);
/*     */         }
/* 217 */         SharedObjects.putOptList((String)STATE_LISTS[i][0], v);
/*     */       }
/* 219 */       binder.addOptionList(name, v);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 225 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.project.ProblemReportUtils
 * JD-Core Version:    0.5.4
 */