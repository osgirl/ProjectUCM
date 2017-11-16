/*    */ package intradoc.tools.build;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.util.IdcException;
/*    */ import java.io.File;
/*    */ 
/*    */ public class PrebuiltComponent extends Component
/*    */ {
/*    */   public void init(BuildManager manager, File moduleDir, File componentBinderFile)
/*    */     throws IdcException
/*    */   {
/* 33 */     super.init(manager, moduleDir, null);
/* 34 */     DataBinder config = this.m_buildConfig;
/* 35 */     String componentZipFilename = config.getLocal("ComponentZip");
/* 36 */     if (componentZipFilename == null)
/*    */     {
/* 38 */       componentZipFilename = this.m_moduleName + ".zip";
/*    */     }
/* 40 */     this.m_componentBinder = new DataBinder();
/* 41 */     this.m_componentZipFile = new File(this.m_moduleDir, componentZipFilename);
/* 42 */     this.m_componentBinderFile = null;
/*    */   }
/*    */ 
/*    */   public boolean buildAndPackage(File targetDir)
/*    */     throws IdcException
/*    */   {
/* 51 */     super.buildAndPackage(targetDir);
/*    */ 
/* 53 */     BuildEnvironment env = this.m_manager.m_env;
/* 54 */     File componentZipFile = this.m_componentZipFile;
/* 55 */     File targetFile = new File(env.m_componentZipsDir, componentZipFile.getName());
/* 56 */     BuildUtils.copyOutdatedFile(componentZipFile, targetFile, env.m_trace);
/* 57 */     return true;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 63 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98926 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.PrebuiltComponent
 * JD-Core Version:    0.5.4
 */