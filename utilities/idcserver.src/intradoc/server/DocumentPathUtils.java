/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.DocumentPathBuilder;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class DocumentPathUtils extends LegacyDocumentPathUtils
/*     */ {
/*     */   public static String getAuthPathPrefix()
/*     */   {
/*  39 */     String authPathPrefix = SharedObjects.getEnvironmentValue("AuthPathPrefix");
/*  40 */     if (authPathPrefix == null)
/*     */     {
/*  42 */       authPathPrefix = "/groups/";
/*     */     }
/*  44 */     return authPathPrefix;
/*     */   }
/*     */ 
/*     */   public static boolean parseDocInfoFromPath(String path, Properties props)
/*     */   {
/*  54 */     return LegacyDocumentPathUtils.parseDocInfoFromPath(path, props, null);
/*     */   }
/*     */ 
/*     */   public static boolean parseDocInfoFromInternalPath(String path, Properties props)
/*     */   {
/*  64 */     return LegacyDocumentPathUtils.parseDocInfoFromInternalPath(path, props, null);
/*     */   }
/*     */ 
/*     */   public static String computeAppControlledSubpath(String path)
/*     */   {
/*  72 */     String intradocRelativeUrlRoot = DocumentPathBuilder.getRelativeWebRoot();
/*  73 */     String authPathPrefix = getAuthPathPrefix();
/*  74 */     String localSecurityPrefix = null;
/*  75 */     int l = intradocRelativeUrlRoot.length();
/*  76 */     if (l > 1)
/*     */     {
/*  78 */       localSecurityPrefix = intradocRelativeUrlRoot.substring(0, l - 1) + authPathPrefix;
/*     */     }
/*     */     else
/*     */     {
/*  83 */       localSecurityPrefix = authPathPrefix;
/*     */     }
/*  85 */     String filePath = path.toLowerCase();
/*  86 */     filePath = FileUtils.fileSlashes(path);
/*     */ 
/*  91 */     int offset = filePath.indexOf(localSecurityPrefix);
/*  92 */     if (offset < 0)
/*     */     {
/*  94 */       return null;
/*     */     }
/*  96 */     filePath = filePath.substring(offset);
/*  97 */     return filePath;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getWebFileName(DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 153 */     return LegacyDocumentPathUtils.getWebFileName(binder);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getWebFileNameEx(DataBinder binder, boolean isActive)
/*     */     throws DataException
/*     */   {
/* 169 */     return LegacyDocumentPathUtils.getWebFileNameEx(binder, isActive);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 174 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocumentPathUtils
 * JD-Core Version:    0.5.4
 */