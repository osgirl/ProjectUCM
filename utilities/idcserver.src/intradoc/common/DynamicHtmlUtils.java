/*    */ package intradoc.common;
/*    */ 
/*    */ public class DynamicHtmlUtils
/*    */ {
/*    */   public static void setBackRedirectHtmlResourceSuppressError(String include, DynamicHtml dynHtml, DynamicHtmlMerger merger)
/*    */   {
/* 31 */     if (dynHtml == null)
/*    */     {
/* 33 */       return;
/*    */     }
/*    */     try
/*    */     {
/* 37 */       merger.setBackRedirectHtmlResource(include, dynHtml, null);
/*    */     }
/*    */     catch (ParseSyntaxException e)
/*    */     {
/* 41 */       Report.trace("system", null, e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static void pushStackMessage(String key, String arg, DynamicHtmlMerger merger)
/*    */   {
/* 53 */     String msg = LocaleUtils.encodeMessage(key, null, arg);
/* 54 */     merger.pushStackMessage(msg);
/*    */   }
/*    */ 
/*    */   public static void pushStackMessage(String key, String arg1, String arg2, DynamicHtmlMerger merger)
/*    */   {
/* 66 */     String msg = LocaleUtils.encodeMessage(key, null, arg1, arg2);
/* 67 */     merger.pushStackMessage(msg);
/*    */   }
/*    */ 
/*    */   public static void pushStackMessage(String key, Object[] params, DynamicHtmlMerger merger)
/*    */   {
/* 78 */     String msg = LocaleUtils.encodeMessage(key, null, params);
/* 79 */     merger.pushStackMessage(msg);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 85 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicHtmlUtils
 * JD-Core Version:    0.5.4
 */