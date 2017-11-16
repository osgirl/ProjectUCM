/*    */ package intradoc.idcwls;
/*    */ 
/*    */ import intradoc.common.ClassHelperUtils;
/*    */ import java.io.IOException;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class IdcServletConfigWrapper
/*    */   implements IdcServletConfig
/*    */ {
/*    */   public Object m_wrappedObject;
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 35 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93999 $";
/*    */   }
/*    */ 
/*    */   public IdcServletConfigWrapper(Object wrappedObject)
/*    */   {
/* 42 */     this.m_wrappedObject = wrappedObject;
/*    */   }
/*    */ 
/*    */   public Object getAttribute(String key)
/*    */   {
/* 50 */     if (this.m_wrappedObject != null)
/*    */     {
/* 52 */       return ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getAttribute", new Object[] { key });
/*    */     }
/*    */ 
/* 55 */     return null;
/*    */   }
/*    */ 
/*    */   public void setAttribute(String key, Object obj)
/*    */   {
/* 63 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setAttribute", new Object[] { key, obj });
/*    */   }
/*    */ 
/*    */   public void executeAction(String action, Map in, Map out)
/*    */     throws IOException
/*    */   {
/*    */     try
/*    */     {
/* 73 */       ClassHelperUtils.executeMethodWithArgs(this.m_wrappedObject, "executeAction", new Object[] { action, in, out });
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 78 */       throw ClassHelperUtils.convertToIOException(e, "executeAction");
/*    */     }
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.IdcServletConfigWrapper
 * JD-Core Version:    0.5.4
 */