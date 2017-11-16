/*    */ package intradoc.idcwls;
/*    */ 
/*    */ import intradoc.common.filter.ParsedTriggerCallback;
/*    */ import java.io.IOException;
/*    */ import java.io.OutputStream;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class ServletOutStreamCallback
/*    */   implements ParsedTriggerCallback
/*    */ {
/*    */   public IdcServletRequestContext m_idcRequest;
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 30 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ 
/*    */   public ServletOutStreamCallback(IdcServletRequestContext request)
/*    */   {
/* 40 */     this.m_idcRequest = request;
/*    */   }
/*    */ 
/*    */   public int foundTrigger(byte[] buf, int start, int len, Map in, Map out, Object obj)
/*    */     throws IOException
/*    */   {
/* 50 */     IdcServletRequestUtils.handleOutputStreamCallback(buf, start, len, this.m_idcRequest);
/* 51 */     int retCode = 2;
/* 52 */     ServletActiveLocalData servletData = this.m_idcRequest.getActiveData();
/* 53 */     if ((this.m_idcRequest.getResponseSent()) || (servletData.m_isPromptLogin) || (servletData.m_isError))
/*    */     {
/* 55 */       retCode |= 8;
/*    */     }
/* 57 */     else if (this.m_idcRequest.getSendResponseHeadersDirect())
/*    */     {
/* 59 */       retCode |= 1;
/*    */     }
/* 61 */     return retCode;
/*    */   }
/*    */ 
/*    */   public OutputStream getOutputStream(Map in, Map out, Object obj)
/*    */     throws IOException
/*    */   {
/* 70 */     return this.m_idcRequest.getServletOutputStream();
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.ServletOutStreamCallback
 * JD-Core Version:    0.5.4
 */