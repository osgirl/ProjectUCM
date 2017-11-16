/*    */ package intradoc.common;
/*    */ 
/*    */ public class SessionUtils
/*    */ {
/*    */   public static void setSessionAttribute(Object servletrequestContext, String name, Object val)
/*    */     throws ServiceException
/*    */   {
/*    */     try
/*    */     {
/* 26 */       ClassHelperUtils.executeMethod(servletrequestContext, "setSessionAttribute", new Object[] { new String(name), val }, new Class[] { String.class, Object.class });
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 30 */       throw new ServiceException("csSetSessionException", e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object getSessionAttribute(Object servletrequestContext, String name) throws ServiceException
/*    */   {
/*    */     try
/*    */     {
/* 38 */       return ClassHelperUtils.executeMethod(servletrequestContext, "getSessionAttribute", new Object[] { new String(name) }, new Class[] { String.class });
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 42 */       throw new ServiceException("csGetSessionException", e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static String getSessionAttributeAsString(Object servletrequestContext, String name) throws ServiceException
/*    */   {
/* 48 */     String value = (String)getSessionAttribute(servletrequestContext, name);
/* 49 */     if ((value != null) && (value.trim().length() >= 1))
/*    */     {
/* 51 */       return value;
/*    */     }
/* 53 */     return null;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 58 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96989 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.SessionUtils
 * JD-Core Version:    0.5.4
 */