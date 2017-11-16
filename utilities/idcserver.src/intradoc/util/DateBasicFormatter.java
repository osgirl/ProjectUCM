/*    */ package intradoc.util;
/*    */ 
/*    */ import java.util.Date;
/*    */ 
/*    */ public class DateBasicFormatter
/*    */   implements BasicFormatter
/*    */ {
/*    */   public static final String TS_BEGIN = "{ts ";
/*    */   public static final String TS_END = "}";
/*    */ 
/*    */   public IdcAppendableBase format(IdcAppendableBase a, Object data, Object context, int flags)
/*    */   {
/* 30 */     Date d = (Date)data;
/* 31 */     a.append("{ts " + d.getTime());
/* 32 */     a.append("}");
/* 33 */     return a;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 38 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.DateBasicFormatter
 * JD-Core Version:    0.5.4
 */