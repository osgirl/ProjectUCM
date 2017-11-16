/*    */ package intradoc.resource;
/*    */ 
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.ResourceContainer;
/*    */ import intradoc.common.ResourceTrace;
/*    */ import intradoc.common.ServiceException;
/*    */ 
/*    */ public class ComponentDataUtils
/*    */ {
/*    */   public static ResourceContainer getOrLoadQueryResources(ComponentData cData)
/*    */     throws ServiceException
/*    */   {
/* 26 */     if (cData == null)
/*    */     {
/* 28 */       return null;
/*    */     }
/*    */ 
/* 31 */     if (cData.m_rc != null)
/*    */     {
/* 33 */       return cData.m_rc;
/*    */     }
/*    */ 
/* 36 */     String str = "!csComponentLoadSystemQuery";
/* 37 */     if (!cData.m_componentName.equalsIgnoreCase("default"))
/*    */     {
/* 39 */       str = LocaleUtils.encodeMessage("csComponentLoadName", null, cData.m_componentName);
/*    */     }
/*    */ 
/* 42 */     ResourceTrace.msg(str);
/*    */ 
/* 44 */     ResourceContainer rc = new ResourceContainer();
/* 45 */     ResourceLoader.loadResourceFile(rc, cData.m_file);
/*    */ 
/* 47 */     cData.m_rc = rc;
/* 48 */     return rc;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.ComponentDataUtils
 * JD-Core Version:    0.5.4
 */