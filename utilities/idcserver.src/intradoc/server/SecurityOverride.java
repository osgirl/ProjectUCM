/*    */ package intradoc.server;
/*    */ 
/*    */ class SecurityOverride
/*    */ {
/* 24 */   public int m_value = 1;
/* 25 */   public boolean m_isOverride = false;
/*    */ 
/*    */   public SecurityOverride(String value)
/*    */   {
/* 29 */     if (value == null)
/*    */       return;
/* 31 */     this.m_isOverride = true;
/*    */     try
/*    */     {
/* 34 */       this.m_value = Integer.parseInt(value);
/*    */     }
/*    */     catch (Throwable ignore)
/*    */     {
/* 38 */       this.m_value = 1;
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 45 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SecurityOverride
 * JD-Core Version:    0.5.4
 */