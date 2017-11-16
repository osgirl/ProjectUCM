/*    */ package intradoc.data;
/*    */ 
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class FieldInfoUtils
/*    */ {
/*    */   public static void setFieldOption(FieldInfo fi, String key, String value)
/*    */   {
/* 27 */     if ((key == null) || (value == null))
/*    */     {
/* 29 */       return;
/*    */     }
/*    */ 
/* 32 */     if (fi.m_additionalOptions == null)
/*    */     {
/* 34 */       fi.m_additionalOptions = new HashMap();
/*    */     }
/* 36 */     fi.m_additionalOptions.put(key, value);
/*    */   }
/*    */ 
/*    */   public static String getFieldOption(FieldInfo fi, String key)
/*    */   {
/* 46 */     String value = null;
/* 47 */     if (fi.m_additionalOptions != null)
/*    */     {
/* 49 */       value = (String)fi.m_additionalOptions.get(key);
/*    */     }
/* 51 */     return value;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 56 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.FieldInfoUtils
 * JD-Core Version:    0.5.4
 */