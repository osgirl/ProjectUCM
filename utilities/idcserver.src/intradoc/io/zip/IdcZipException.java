/*    */ package intradoc.io.zip;
/*    */ 
/*    */ import intradoc.util.IdcException;
/*    */ import intradoc.util.IdcMessage;
/*    */ 
/*    */ public class IdcZipException extends IdcException
/*    */ {
/*    */   public IdcZipException(Throwable cause)
/*    */   {
/* 31 */     super(cause, null);
/*    */   }
/*    */ 
/*    */   public IdcZipException(Throwable cause, IdcMessage message)
/*    */   {
/* 36 */     super(cause, message);
/*    */   }
/*    */ 
/*    */   public IdcZipException(Throwable cause, String key, Object[] args)
/*    */   {
/* 41 */     super(cause, new IdcMessage(key, args));
/*    */   }
/*    */ 
/*    */   public IdcZipException(IdcMessage message)
/*    */   {
/* 46 */     super(null, message);
/*    */   }
/*    */ 
/*    */   public IdcZipException(String key, Object[] args)
/*    */   {
/* 51 */     super(null, new IdcMessage(key, args));
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 57 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipException
 * JD-Core Version:    0.5.4
 */