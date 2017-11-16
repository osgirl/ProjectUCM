/*    */ package intradoc.refinery.configure;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.configpage.ConfigPageService;
/*    */ import intradoc.data.DataException;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class GenerateThumbnailServices extends ConfigPageService
/*    */ {
/*    */   public void loadOIOptions()
/*    */   {
/* 29 */     Properties configData = (Properties)getCachedObject("CurrentConfigOptions-ConfigureOIThumbnails");
/*    */ 
/* 31 */     OitFontUtilsHelper.loadConfigureFontAndRenderingOption(configData);
/*    */   }
/*    */ 
/*    */   public void saveOIOptions() throws ServiceException, DataException
/*    */   {
/* 36 */     Object[] params = (Object[])(Object[])getCachedObject("CustomConfigPostParams-ConfigureOIThumbnails");
/* 37 */     Properties props = (Properties)params[0];
/*    */ 
/* 39 */     OitFontUtilsHelper.saveConfigureFontAndRenderingOption(props, this.m_binder);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 44 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93338 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.configure.GenerateThumbnailServices
 * JD-Core Version:    0.5.4
 */