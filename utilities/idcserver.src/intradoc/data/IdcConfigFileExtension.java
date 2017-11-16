/*    */ package intradoc.data;
/*    */ 
/*    */ import intradoc.common.ConfigFileParameters;
/*    */ import intradoc.common.FileUtils;
/*    */ 
/*    */ public class IdcConfigFileExtension extends IdcConfigFile
/*    */ {
/*    */   public IdcConfigFileExtension(String path, String feature, Workspace workspace, String prefix)
/*    */   {
/* 27 */     super(path);
/*    */ 
/* 29 */     this.m_workspace = workspace;
/* 30 */     this.m_prefix = prefix;
/*    */ 
/* 32 */     if (path.endsWith("/"))
/*    */     {
/* 35 */       path = FileUtils.directorySlashes(path);
/* 36 */       this.m_fileName = "";
/* 37 */       this.m_relativeDir = path.replace(this.m_prefix, "");
/* 38 */       this.m_isDir = Boolean.valueOf(true);
/*    */     }
/*    */     else
/*    */     {
/* 42 */       path = FileUtils.fileSlashes(path);
/* 43 */       int index = path.lastIndexOf("/");
/* 44 */       this.m_fileName = path.substring(index + 1);
/* 45 */       String dir = path.substring(0, index + 1);
/* 46 */       this.m_relativeDir = dir.replace(this.m_prefix, "");
/*    */     }
/* 48 */     this.m_feature = feature;
/* 49 */     this.m_relativeRoot = ConfigFileParameters.getRoot(this.m_relativeDir);
/* 50 */     this.m_fileID = (this.m_relativeDir + this.m_fileName);
/*    */   }
/*    */ 
/*    */   public IdcConfigFileExtension(String path, String feature, boolean isDir, Workspace workspace, String prefix)
/*    */   {
/* 55 */     super(path);
/*    */ 
/* 57 */     this.m_workspace = workspace;
/* 58 */     this.m_prefix = prefix;
/*    */ 
/* 60 */     if (isDir)
/*    */     {
/* 63 */       path = FileUtils.directorySlashes(path);
/* 64 */       this.m_fileName = "";
/* 65 */       this.m_relativeDir = path.replace(this.m_prefix, "");
/* 66 */       this.m_isDir = Boolean.valueOf(true);
/*    */     }
/*    */     else
/*    */     {
/* 70 */       path = FileUtils.fileSlashes(path);
/* 71 */       int index = path.lastIndexOf("/");
/* 72 */       this.m_fileName = path.substring(index + 1);
/* 73 */       String dir = path.substring(0, index + 1);
/* 74 */       this.m_relativeDir = dir.replace(this.m_prefix, "");
/*    */     }
/*    */ 
/* 78 */     if ((feature == null) || (feature.length() == 0))
/*    */     {
/* 80 */       feature = ConfigFileParameters.getFeature(this.m_relativeDir);
/*    */     }
/* 82 */     this.m_feature = feature;
/* 83 */     this.m_relativeRoot = ConfigFileParameters.getRoot(this.m_relativeDir);
/* 84 */     this.m_fileID = (this.m_relativeDir + this.m_fileName);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 89 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99056 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcConfigFileExtension
 * JD-Core Version:    0.5.4
 */