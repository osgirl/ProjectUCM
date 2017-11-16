/*    */ import intradoc.apputilities.systemproperties.SystemPropertiesFrame;
/*    */ import intradoc.common.IdcMessageFactory;
/*    */ import intradoc.common.SystemUtils;
/*    */ import intradoc.gui.GuiText;
/*    */ import intradoc.server.utils.IdcUtilityLoader;
/*    */ import intradoc.shared.SharedLoader;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import intradoc.util.IdcMessage;
/*    */ 
/*    */ public class SystemProperties
/*    */ {
/*    */   public static void main(String[] args)
/*    */   {
/* 45 */     boolean localizationLoaded = false;
/*    */ 
/* 47 */     SystemPropertiesFrame spd = null;
/*    */     try
/*    */     {
/* 51 */       IdcUtilityLoader.initLocalizationEx(1);
/* 52 */       SharedLoader.loadInitialConfig();
/* 53 */       GuiText.localize(null);
/*    */ 
/* 55 */       boolean useSimpleSysProp = SharedObjects.getEnvValueAsBoolean("UseSimpleSystemProperties", false);
/*    */ 
/* 57 */       String additionalTab = SharedObjects.getEnvironmentValue("AdditionalPanelForSysProperties");
/*    */ 
/* 59 */       IdcMessage spdTitle = IdcMessageFactory.lc("csSysPropsFrameTitle", new Object[0]);
/* 60 */       localizationLoaded = true;
/* 61 */       spd = new SystemPropertiesFrame();
/* 62 */       spd.initEx(spdTitle, true, useSimpleSysProp, additionalTab);
/*    */     }
/*    */     catch (Throwable t)
/*    */     {
/*    */       IdcMessage msg;
/*    */       IdcMessage msg;
/* 67 */       if (localizationLoaded)
/*    */       {
/* 69 */         msg = IdcMessageFactory.lc("csAppStartError", new Object[] { "SystemPropertiesEditor" });
/*    */       }
/*    */       else
/*    */       {
/* 73 */         msg = IdcMessageFactory.lc();
/* 74 */         msg.m_msgLocalized = "Unable to start application.";
/*    */       }
/*    */ 
/* 78 */       SystemUtils.handleFatalException(t, msg, -1);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 84 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     SystemProperties
 * JD-Core Version:    0.5.4
 */