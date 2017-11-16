/*    */ package intradoc.tools.common.eclipse;
/*    */ 
/*    */ import intradoc.util.GenericTracingCallback;
/*    */ import java.io.File;
/*    */ import java.io.FileReader;
/*    */ import java.io.IOException;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class WorkspaceEditor
/*    */ {
/*    */   public GenericTracingCallback m_trace;
/*    */   public File m_workspaceDir;
/*    */ 
/*    */   public void setWorkspaceDir(File workspaceDir)
/*    */     throws IOException
/*    */   {
/* 47 */     if (workspaceDir == null)
/*    */     {
/* 49 */       this.m_workspaceDir = null;
/* 50 */       return;
/*    */     }
/* 52 */     File versionFile = new File(workspaceDir, ".metadata/version.ini");
/* 53 */     if (!versionFile.isFile())
/*    */     {
/* 55 */       throw new IOException("no such file: " + versionFile.getPath());
/*    */     }
/* 57 */     FileReader fr = new FileReader(versionFile);
/*    */     try
/*    */     {
/* 60 */       Properties props = new Properties();
/* 61 */       props.load(fr);
/* 62 */       String runtime = props.getProperty("org.eclipse.core.runtime");
/* 63 */       if (runtime == null)
/*    */       {
/* 65 */         throw new IOException("missing org.eclipse.core.runtime");
/*    */       }
/* 67 */       if (!runtime.equals("1"))
/*    */       {
/* 69 */         throw new IOException("unknown value for org.eclipse.core.runtime: " + runtime);
/*    */       }
/*    */     }
/*    */     finally
/*    */     {
/* 74 */       fr.close();
/*    */     }
/* 76 */     this.m_workspaceDir = workspaceDir;
/*    */   }
/*    */ 
/*    */   public File computeLaunchConfigurationFile(String launchName)
/*    */   {
/* 87 */     File workspaceDir = this.m_workspaceDir;
/* 88 */     if (workspaceDir == null)
/*    */     {
/* 90 */       return null;
/*    */     }
/* 92 */     String pathname = ".metadata/.plugins/org.eclipse.debug.core/.launches/" + launchName + ".launch";
/* 93 */     return new File(workspaceDir, pathname);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 99 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97481 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.eclipse.WorkspaceEditor
 * JD-Core Version:    0.5.4
 */