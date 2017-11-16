/*    */ package intradoc.apps.docconfig;
/*    */ 
/*    */ import java.util.Properties;
/*    */ 
/*    */ class MetaFieldGui
/*    */ {
/*    */   public static String getDispFieldName(Properties props)
/*    */   {
/* 35 */     return createDisplayName((String)props.get("dName"));
/*    */   }
/*    */ 
/*    */   public static String createDisplayName(String dbFieldName)
/*    */   {
/* 40 */     char ch = dbFieldName.charAt(0);
/* 41 */     if ((ch == 'x') || (ch == 'd'))
/*    */     {
/* 43 */       return dbFieldName.substring(1);
/*    */     }
/* 45 */     return dbFieldName;
/*    */   }
/*    */ 
/*    */   public static String getDbFieldName(Properties props)
/*    */   {
/* 50 */     return createDatabaseName(props.getProperty("FieldName"));
/*    */   }
/*    */ 
/*    */   public static String createDatabaseName(String dispFieldName)
/*    */   {
/* 55 */     return "x" + dispFieldName;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 60 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.MetaFieldGui
 * JD-Core Version:    0.5.4
 */