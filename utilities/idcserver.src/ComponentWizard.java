/*    */ import intradoc.apputilities.componentwizard.CWizardFrame;
/*    */ import intradoc.apputilities.componentwizard.CWizardUtils;
/*    */ import intradoc.common.IdcMessageFactory;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.gui.GuiText;
/*    */ import intradoc.server.IdcSystemConfig;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import intradoc.util.IdcMessage;
/*    */ 
/*    */ public class ComponentWizard
/*    */ {
/*    */   public static void main(String[] args)
/*    */   {
/* 35 */     CWizardFrame cwFrame = new CWizardFrame();
/* 36 */     boolean localizationLoaded = false;
/*    */     try
/*    */     {
/* 41 */       IdcSystemConfig.loadInitialConfig();
/* 42 */       IdcSystemConfig.loadAppConfigInfo();
/* 43 */       IdcSystemConfig.initLocalization(IdcSystemConfig.F_UTILITY_APP);
/* 44 */       IdcSystemConfig.configLocalization();
/* 45 */       GuiText.localize(null);
/*    */ 
/* 47 */       localizationLoaded = true;
/*    */ 
/* 49 */       intradoc.data.DataSerializeUtils.m_useOverrideEncodingHeaderVersion = SharedObjects.getEnvValueAsBoolean("ComponentWizardUseOverrideEncodingHeaderVersion", false);
/* 50 */       intradoc.data.DataSerializeUtils.m_overrideEncodingHeaderVersionString = SharedObjects.getEnvironmentValue("ComponentWizardOverrideEncodingHeaderVersion");
/*    */ 
/* 52 */       computeIsLightWeight();
/*    */ 
/* 54 */       IdcMessage title = IdcMessageFactory.lc("csCompWizTitle", new Object[0]);
/* 55 */       cwFrame.init(title, true);
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 59 */       if (!localizationLoaded)
/*    */       {
/* 61 */         IdcMessage msg = IdcMessageFactory.lc();
/* 62 */         msg.m_msgLocalized = "Error loading system localization strings.";
/* 63 */         cwFrame.reportError(e, msg);
/* 64 */         System.exit(1);
/*    */       }
/* 66 */       Report.trace(null, null, e);
/* 67 */       cwFrame.reportError(e);
/* 68 */       System.exit(1);
/*    */     }
/*    */   }
/*    */ 
/*    */   protected static void computeIsLightWeight()
/*    */   {
/* 74 */     CWizardUtils.m_isLightWeightCW = SharedObjects.getEnvValueAsBoolean("IsLightWeightComponentWizard", false);
/*    */ 
/* 76 */     CWizardUtils.m_isRefineryCW = SharedObjects.getEnvValueAsBoolean("IsRefineryComponentWizard", false);
/*    */ 
/* 81 */     String jdbcDriver = SharedObjects.getEnvironmentValue("JdbcDriver");
/* 82 */     if ((CWizardUtils.m_isRefineryCW != true) && (jdbcDriver != null) && (jdbcDriver.length() != 0)) {
/*    */       return;
/*    */     }
/*    */ 
/* 86 */     CWizardUtils.m_isLightWeightCW = true;
/* 87 */     SharedObjects.putEnvironmentValue("IsLightWeightComponentWizard", "1");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 93 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80969 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     ComponentWizard
 * JD-Core Version:    0.5.4
 */