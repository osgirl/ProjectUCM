/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderHelper;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class LegacyDocumentPathUtils
/*     */ {
/*     */   public static boolean parseDocInfoFromPath(String path, Properties props, Service service)
/*     */   {
/*  40 */     return parseDocInfoFromInternalPath(path, props, service);
/*     */   }
/*     */ 
/*     */   public static boolean parseDocInfoFromInternalPath(String path, Properties props, ExecutionContext cxt)
/*     */   {
/*     */     try
/*     */     {
/*  48 */       FileStoreProviderHelper fileHelper = null;
/*  49 */       if (cxt instanceof Service)
/*     */       {
/*  51 */         Service service = (Service)cxt;
/*  52 */         fileHelper = service.m_fileUtils;
/*     */       }
/*     */       else
/*     */       {
/*  56 */         ExecutionContextAdaptor context = new ExecutionContextAdaptor();
/*  57 */         context.setParentContext(cxt);
/*  58 */         FileStoreProvider fileStore = FileStoreProviderLoader.initFileStore(context);
/*  59 */         fileHelper = FileStoreProviderHelper.getFileStoreProviderUtils(fileStore, context);
/*     */ 
/*  61 */         cxt = context;
/*     */       }
/*  63 */       return fileHelper.parseDocInfoFromInternalPath(path, props, cxt);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  67 */       Report.trace(null, "parseDocInfoFromInternalPath: Error parsing " + path, e);
/*     */     }
/*  69 */     return false;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getWebFileName(DataBinder binder)
/*     */     throws DataException
/*     */   {
/*  83 */     return getWebFileNameEx(binder, false);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getWebFileNameEx(DataBinder binder, boolean isActive)
/*     */     throws DataException
/*     */   {
/*  98 */     String curStatus = null;
/*  99 */     if (isActive)
/*     */     {
/* 101 */       curStatus = binder.getActiveAllowMissing("dStatus");
/*     */     }
/*     */     else
/*     */     {
/* 105 */       curStatus = binder.getAllowMissing("dStatus");
/*     */     }
/*     */ 
/* 108 */     boolean isUrl = (curStatus == null) || (!curStatus.equals("GENWWW"));
/*     */ 
/* 110 */     String fileName = null;
/* 111 */     if (isUrl)
/*     */     {
/* 113 */       String docName = binder.get("dDocName", isActive);
/* 114 */       String extension = binder.get("dWebExtension", isActive);
/* 115 */       String revLabel = binder.get("dRevLabel", isActive);
/* 116 */       String relState = null;
/* 117 */       if (isActive)
/*     */       {
/* 119 */         relState = binder.getActiveAllowMissing("dReleaseState");
/*     */       }
/*     */       else
/*     */       {
/* 123 */         relState = binder.getAllowMissing("dReleaseState");
/*     */       }
/*     */ 
/* 126 */       fileName = docName;
/* 127 */       if ((relState != null) && (!relState.equals("Y")) && (!relState.equals("U")) && (!relState.equals("I")))
/*     */       {
/* 129 */         fileName = fileName + "~" + revLabel;
/*     */       }
/* 131 */       if ((extension != null) && (extension.length() > 0))
/*     */       {
/* 133 */         fileName = fileName + "." + extension;
/*     */       }
/*     */ 
/* 136 */       fileName = fileName.toLowerCase();
/*     */     }
/*     */ 
/* 139 */     return fileName;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 144 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78996 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.LegacyDocumentPathUtils
 * JD-Core Version:    0.5.4
 */