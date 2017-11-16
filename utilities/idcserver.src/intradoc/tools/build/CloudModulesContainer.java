/*    */ package intradoc.tools.build;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.util.IdcException;
/*    */ import java.io.File;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class CloudModulesContainer extends ModulesContainer
/*    */ {
/*    */   public void init(BuildManager manager, File modulesDir, DataBinder buildConfig, boolean isRequired)
/*    */     throws IdcException
/*    */   {
/* 31 */     super.init(manager, modulesDir, buildConfig, isRequired);
/*    */ 
/* 34 */     String labelSeries = this.m_properties.getProperty("UCMLabelSeries");
/* 35 */     this.m_isIncluded = labelSeries.equals("UCM_MAIN_GENERIC");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 41 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99449 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.CloudModulesContainer
 * JD-Core Version:    0.5.4
 */