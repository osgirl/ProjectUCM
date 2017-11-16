/*    */ package intradoc.tools.build;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.util.IdcException;
/*    */ import java.io.File;
/*    */ 
/*    */ public class ComplianceModulesContainer extends ModulesContainer
/*    */ {
/*    */   public void init(BuildManager manager, File modulesDir, DataBinder buildConfig, boolean isRequired)
/*    */     throws IdcException
/*    */   {
/* 31 */     super.init(manager, modulesDir, buildConfig, isRequired);
/* 32 */     manager.m_hasComplianceSources = this.m_isIncluded;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 37 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99576 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.ComplianceModulesContainer
 * JD-Core Version:    0.5.4
 */