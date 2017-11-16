/*     */ package intradoc.server.publish;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.config.ConfigFileUtilities;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.ResultSetTreeSort;
/*     */ import java.io.IOException;
/*     */ import java.util.List;
/*     */ 
/*     */ public class WebPublishUtils
/*     */ {
/*     */   public static final String DYNAMIC_PUBLISHED_TABLE = "LastWebfilesPublished";
/*     */   public static final String FILTERED_STATIC_PUBLISHED_TABLE = "FilteredStaticWebResources";
/*     */   protected static final String PUBLISHED_CLASS_BUNDLE_MAP = "PublishedClassBundleMap";
/*  51 */   protected static String PUBLISH_AT_STARTUP_FILENAME = "$DataDir/publish/startup.hda";
/*     */ 
/*     */   protected static DataBinder loadPublishAtStartupBinder(ConfigFileUtilities cfu)
/*     */     throws DataException, ServiceException
/*     */   {
/*  64 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/*  67 */       cfu.readDataBinderFromName(PUBLISH_AT_STARTUP_FILENAME, binder, null);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  71 */       if (SystemUtils.m_verbose)
/*     */       {
/*  73 */         Report.debug("publish", null, e);
/*     */       }
/*     */     }
/*  76 */     return binder;
/*     */   }
/*     */ 
/*     */   protected static void savePublishAtStartupBinder(ConfigFileUtilities cfu, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/*  92 */       cfu.writeDataBinderToName(PUBLISH_AT_STARTUP_FILENAME, binder, null);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  96 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void setPublishEverythingAtStartup(ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 113 */     ConfigFileUtilities cfu = ConfigFileUtilities.getOrCreateConfigFileUtilitiesForExecutionContext(cxt);
/* 114 */     DataBinder binder = loadPublishAtStartupBinder(cfu);
/* 115 */     binder.putLocal("PublishEverything", "true");
/* 116 */     savePublishAtStartupBinder(cfu, binder);
/*     */   }
/*     */ 
/*     */   public static void addPublishStaticClassAtStartup(ExecutionContext cxt, String className)
/*     */     throws DataException, ServiceException
/*     */   {
/* 132 */     ConfigFileUtilities cfu = ConfigFileUtilities.getOrCreateConfigFileUtilitiesForExecutionContext(cxt);
/* 133 */     DataBinder binder = loadPublishAtStartupBinder(cfu);
/* 134 */     String[] fieldNames = { "class" };
/* 135 */     DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, "PublishClasses", true, false);
/* 136 */     if (null == drset)
/*     */     {
/* 138 */       drset = new DataResultSet(fieldNames);
/* 139 */       binder.addResultSet("PublishClasses", drset);
/*     */     }
/* 141 */     FieldInfo[] fields = ResultSetUtils.createInfoList(drset, fieldNames, true);
/* 142 */     int index = fields[0].m_index;
/* 143 */     List row = drset.createEmptyRowAsList();
/* 144 */     row.set(index, className);
/* 145 */     drset.addRowWithList(row);
/* 146 */     savePublishAtStartupBinder(cfu, binder);
/*     */   }
/*     */ 
/*     */   public static void addPublishStaticClassesAtStartup(ExecutionContext cxt, List classNames)
/*     */     throws DataException, ServiceException
/*     */   {
/* 162 */     ConfigFileUtilities cfu = ConfigFileUtilities.getOrCreateConfigFileUtilitiesForExecutionContext(cxt);
/* 163 */     DataBinder binder = loadPublishAtStartupBinder(cfu);
/* 164 */     String[] fieldNames = { "class" };
/* 165 */     DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, "PublishClasses", true, false);
/* 166 */     if (null == drset)
/*     */     {
/* 168 */       drset = new DataResultSet(fieldNames);
/* 169 */       binder.addResultSet("PublishClasses", drset);
/*     */     }
/* 171 */     FieldInfo[] fields = ResultSetUtils.createInfoList(drset, fieldNames, true);
/* 172 */     int index = fields[0].m_index;
/* 173 */     int numClasses = classNames.size();
/* 174 */     for (int i = 0; i < numClasses; ++i)
/*     */     {
/* 176 */       List row = drset.createEmptyRowAsList();
/* 177 */       String className = (String)classNames.get(i);
/* 178 */       row.set(index, className);
/* 179 */       drset.addRowWithList(row);
/*     */     }
/* 181 */     savePublishAtStartupBinder(cfu, binder);
/*     */   }
/*     */ 
/*     */   public static WebPublisher doPublish(Workspace ws, ExecutionContext cxt, int flags)
/*     */     throws DataException, ServiceException
/*     */   {
/* 211 */     WebPublisher publisher = new WebPublisher();
/* 212 */     publisher.initializeState(ws, cxt, flags);
/* 213 */     publisher.prepareForPublishing();
/* 214 */     publisher.startPublish();
/* 215 */     return (publisher.m_thread == null) ? null : publisher;
/*     */   }
/*     */ 
/*     */   public static void sortFileSet(DataResultSet drset, int index)
/*     */   {
/* 222 */     ResultSetTreeSort resultSetSort = new ResultSetTreeSort(drset, index, false);
/* 223 */     resultSetSort.determineFieldType("int");
/* 224 */     resultSetSort.determineIsAscending("asc");
/* 225 */     resultSetSort.sort();
/*     */   }
/*     */ 
/*     */   protected static boolean checkForPublish(String publishScript, DataBinder binder, PageMerger pageMerger)
/*     */     throws ServiceException
/*     */   {
/* 231 */     boolean isPublish = false;
/*     */     try
/*     */     {
/* 234 */       pageMerger.evaluateScript(publishScript);
/* 235 */       isPublish = StringUtils.convertToBool(binder.getLocal("doPublish"), false);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 239 */       String msg = LocaleUtils.encodeMessage("csUnableToEvalScript", null, publishScript);
/*     */ 
/* 241 */       throw new ServiceException(msg, e);
/*     */     }
/* 243 */     return isPublish;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 250 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72253 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.WebPublishUtils
 * JD-Core Version:    0.5.4
 */