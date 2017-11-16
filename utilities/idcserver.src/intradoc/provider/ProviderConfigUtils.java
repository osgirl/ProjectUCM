/*    */ package intradoc.provider;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.shared.SharedObjects;
/*    */ 
/*    */ public class ProviderConfigUtils
/*    */ {
/*    */   public static void loadSharedVariable(DataBinder serverData, String key)
/*    */   {
/* 31 */     if (serverData.getLocal(key) != null)
/*    */       return;
/* 33 */     String val = SharedObjects.getEnvironmentValue(key);
/* 34 */     if (val == null)
/*    */       return;
/* 36 */     serverData.putLocal(key, val);
/*    */   }
/*    */ 
/*    */   public static void loadSharedTable(DataBinder serverData, String key)
/*    */   {
/* 43 */     loadSharedTableReplaceKey(serverData, key, key);
/*    */   }
/*    */ 
/*    */   public static void loadSharedTableReplaceKey(DataBinder serverData, String key, String newKey)
/*    */   {
/* 51 */     if (serverData.getResultSet(key) != null)
/*    */       return;
/* 53 */     DataResultSet drset = SharedObjects.getTable(key);
/* 54 */     if (drset == null)
/*    */       return;
/* 56 */     serverData.addResultSet(newKey, drset);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 63 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ProviderConfigUtils
 * JD-Core Version:    0.5.4
 */