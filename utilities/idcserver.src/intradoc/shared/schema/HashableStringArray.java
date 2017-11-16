/*    */ package intradoc.shared.schema;
/*    */ 
/*    */ import intradoc.common.IdcHashableStringArray;
/*    */ 
/*    */ public class HashableStringArray extends IdcHashableStringArray
/*    */ {
/*    */   public HashableStringArray(String[] array)
/*    */   {
/* 31 */     super(array);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 36 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73821 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.HashableStringArray
 * JD-Core Version:    0.5.4
 */