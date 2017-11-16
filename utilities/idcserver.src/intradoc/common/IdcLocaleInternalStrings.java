/*    */ package intradoc.common;
/*    */ 
/*    */ public class IdcLocaleInternalStrings
/*    */ {
/* 34 */   public String[] m_shortMonthNames = null;
/* 35 */   public String[] m_longMonthNames = null;
/* 36 */   public String[] m_shortWeekNames = null;
/* 37 */   public String[] m_longWeekNames = null;
/*    */ 
/*    */   public IdcLocaleInternalStrings()
/*    */   {
/*    */   }
/*    */ 
/*    */   public IdcLocaleInternalStrings(String[] shortMonthNames, String[] longMonthNames, String[] shortWeekNames, String[] longWeekNames)
/*    */   {
/* 47 */     this.m_shortMonthNames = shortMonthNames;
/* 48 */     this.m_longMonthNames = longMonthNames;
/* 49 */     this.m_shortWeekNames = shortWeekNames;
/* 50 */     this.m_longWeekNames = longWeekNames;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 55 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcLocaleInternalStrings
 * JD-Core Version:    0.5.4
 */