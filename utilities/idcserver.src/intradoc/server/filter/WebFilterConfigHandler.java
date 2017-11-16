/*    */ package intradoc.server.filter;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.StringUtils;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.IdcServiceAction;
/*    */ import intradoc.server.ServiceHandler;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class WebFilterConfigHandler extends ServiceHandler
/*    */ {
/*    */   @IdcServiceAction
/*    */   public void loadFilterConfig()
/*    */     throws ServiceException
/*    */   {
/* 33 */     DataBinder tmpBinder = WebFilterConfigUtils.readFilterConfigFromFile();
/* 34 */     this.m_binder.merge(tmpBinder);
/*    */   }
/*    */ 
/*    */   @IdcServiceAction
/*    */   public void updateFilterConfig() throws ServiceException, DataException
/*    */   {
/* 40 */     DataBinder cfgBinder = WebFilterConfigUtils.readFilterConfigFromFile();
/*    */ 
/* 42 */     String formValuesStr = this.m_binder.getLocal("ExtraFields");
/* 43 */     if (formValuesStr == null)
/*    */     {
/* 45 */       return;
/*    */     }
/*    */ 
/* 48 */     Vector formValues = StringUtils.parseArray(formValuesStr, ',', '^');
/* 49 */     for (int i = 0; i < formValues.size(); ++i)
/*    */     {
/* 51 */       String key = (String)formValues.elementAt(i);
/* 52 */       if (key.length() == 0)
/*    */       {
/*    */         continue;
/*    */       }
/*    */ 
/* 57 */       String value = this.m_binder.getAllowMissing(key);
/* 58 */       if (value != null)
/*    */       {
/* 60 */         cfgBinder.putLocal(key, value);
/*    */       }
/*    */       else
/*    */       {
/* 64 */         String defVal = StringUtils.findString(WebFilterConfigUtils.DEFAULT_CONFIG_VALS, key, 0, 1);
/* 65 */         if (defVal == null)
/*    */           continue;
/* 67 */         cfgBinder.putLocal(key, defVal);
/*    */       }
/*    */ 
/*    */     }
/*    */ 
/* 72 */     WebFilterConfigUtils.updateServerEnvironment(cfgBinder);
/* 73 */     WebFilterConfigUtils.writeFilterConfigToFile(cfgBinder);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 78 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.filter.WebFilterConfigHandler
 * JD-Core Version:    0.5.4
 */