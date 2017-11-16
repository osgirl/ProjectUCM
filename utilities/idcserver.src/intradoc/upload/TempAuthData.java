/*    */ package intradoc.upload;
/*    */ 
/*    */ import intradoc.shared.UserData;
/*    */ 
/*    */ public class TempAuthData
/*    */ {
/*    */   public UserData m_userData;
/*    */   public String m_externalRoles;
/*    */   public String m_externalAccounts;
/*    */   public String m_lookupCode;
/*    */   public long m_timeStamp;
/*    */ 
/*    */   public TempAuthData(UserData userData, String requestID, String authCode, String externalRoles, String externalAccounts)
/*    */   {
/* 37 */     this.m_userData = userData;
/* 38 */     this.m_lookupCode = (requestID + "." + authCode);
/* 39 */     this.m_timeStamp = System.currentTimeMillis();
/* 40 */     this.m_externalRoles = externalRoles;
/* 41 */     this.m_externalAccounts = externalAccounts;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 46 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.upload.TempAuthData
 * JD-Core Version:    0.5.4
 */