/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.converter.ConverterUtils;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class DatedCacheUtils
/*     */ {
/*     */   public static void removeDatedCaches(Workspace ws)
/*     */     throws DataException, ServiceException
/*     */   {
/*  61 */     boolean isDynamicConverterEnabled = SharedObjects.getEnvValueAsBoolean("IsDynamicConverterEnabled", false);
/*     */ 
/*  63 */     boolean checkDynamicConversionCache = SharedObjects.getEnvValueAsBoolean("DoCheckDynamicConversionCache", false);
/*     */ 
/*  65 */     if ((isDynamicConverterEnabled) || (checkDynamicConversionCache))
/*     */     {
/*  67 */       ConverterUtils.deleteExpiredConversions(ws);
/*     */     }
/*     */ 
/*  70 */     PluginFilters.filter("removeCustomDatedCaches", ws, null, null);
/*     */ 
/*  72 */     Properties props = new Properties();
/*  73 */     Date dte = new Date();
/*  74 */     String str = LocaleUtils.formatODBC(dte);
/*  75 */     props.put("dLastUsedDate", str);
/*     */ 
/*  77 */     DataBinder params = new DataBinder();
/*  78 */     params.setLocalData(props);
/*  79 */     ResultSet rset = ws.createResultSet("QagedCaches", params);
/*  80 */     if (rset.isEmpty())
/*     */     {
/*  82 */       return;
/*     */     }
/*     */ 
/*  85 */     DataResultSet drset = new DataResultSet();
/*  86 */     drset.copy(rset);
/*     */ 
/*  88 */     ws.releaseConnection();
/*     */ 
/*  91 */     for (; drset.isRowPresent(); drset.next())
/*     */     {
/*  93 */       props = drset.getCurrentRowProps();
/*     */ 
/*  96 */       params.setLocalData(props);
/*  97 */       ws.execute("Dcache", params);
/*     */ 
/*  99 */       String action = props.getProperty("dCacheAction");
/* 100 */       if ((action.equals("checkin")) || (action.equals("form")) || (action.equals("simple")))
/*     */       {
/* 102 */         cleanUpCheckinDirectories(params);
/*     */       }
/*     */       else
/*     */       {
/* 106 */         PluginFilters.filter("removeDatedCaches", ws, params, null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void cleanUpCheckinDirectories(DataBinder params)
/*     */   {
/*     */     try
/*     */     {
/* 116 */       String docName = params.get("dDocName").toLowerCase();
/*     */ 
/* 118 */       String checkinDirRoot = DirectoryLocator.getTempDirectory() + "~checkin/";
/* 119 */       String checkinDir = checkinDirRoot + "~" + docName;
/*     */ 
/* 121 */       File dir = new File(checkinDir);
/* 122 */       File rootDir = new File(checkinDirRoot);
/*     */ 
/* 124 */       String cDir = dir.getCanonicalPath();
/* 125 */       String cRootDir = rootDir.getCanonicalPath();
/* 126 */       if (!cDir.startsWith(cRootDir))
/*     */       {
/* 128 */         throw new ServiceException(null, IdcMessageFactory.lc("csInvalidCachedCheckInDirectory", new Object[] { cDir }));
/*     */       }
/*     */ 
/* 131 */       FileUtils.deleteDirectory(dir, true);
/*     */ 
/* 133 */       String docType = params.get("dDocType");
/*     */ 
/* 135 */       if (docType.length() > 0)
/*     */       {
/* 137 */         ExecutionContext context = new ExecutionContextAdaptor();
/* 138 */         Provider provider = Providers.getProvider("SystemDatabase");
/* 139 */         Object workspace = provider.getProvider();
/* 140 */         context.setCachedObject("Workspace", workspace);
/*     */ 
/* 142 */         FileStoreProvider fileStore = FileStoreProviderLoader.initFileStore(context);
/* 143 */         params.putLocal("RenditionId", "webViewableFile");
/* 144 */         Map args = new HashMap();
/* 145 */         args.put("isContainer", "1");
/* 146 */         args.put("forceNoLink", "1");
/* 147 */         args.put("isLocationOnly", "1");
/* 148 */         IdcFileDescriptor d = fileStore.createDescriptor(params, args, context);
/*     */ 
/* 150 */         String webDir = FileUtils.directorySlashes(d.getProperty("path"));
/* 151 */         String suffix = "~" + docName;
/* 152 */         webDir = webDir + suffix;
/* 153 */         PathUtils.validatePathIsLegal(webDir);
/* 154 */         dir = new File(webDir);
/* 155 */         boolean validatePathFlag = SharedObjects.getEnvValueAsBoolean("ValidateDeleteDirPath", true);
/* 156 */         if (validatePathFlag)
/*     */         {
/* 158 */           String absPath = dir.getAbsolutePath();
/* 159 */           if (!absPath.endsWith(suffix))
/*     */           {
/* 161 */             String msg = absPath + " is not a valid path";
/* 162 */             throw new ServiceException(msg);
/*     */           }
/*     */         }
/* 165 */         FileUtils.deleteDirectory(dir, true);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 170 */       Report.error(null, e, "csUnableToDeleteCachedCheckinDir", new Object[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 176 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 91799 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DatedCacheUtils
 * JD-Core Version:    0.5.4
 */