/*    */ package intradoc.server;
/*    */ 
/*    */ public class UserStorageImplementorData
/*    */ {
/*    */   public boolean isNewUser;
/*    */   public boolean needExternalData;
/*    */   public boolean cachedPasswordIsGood;
/*    */   public boolean queriedDbUserInfo;
/*    */   public boolean queriedDbUserAttributes;
/*    */   public boolean queriedUserProvider;
/*    */   public boolean retrievedUserAttributes;
/*    */ 
/*    */   public UserStorageImplementorData()
/*    */   {
/* 24 */     this.isNewUser = false;
/* 25 */     this.needExternalData = false;
/* 26 */     this.cachedPasswordIsGood = false;
/* 27 */     this.queriedDbUserInfo = false;
/* 28 */     this.queriedDbUserAttributes = false;
/* 29 */     this.queriedUserProvider = false;
/* 30 */     this.retrievedUserAttributes = false;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 35 */     StringBuffer sb = new StringBuffer();
/* 36 */     sb.append("isNewUser: ");
/* 37 */     sb.append(this.isNewUser);
/* 38 */     sb.append("\nneedExternalData: ");
/* 39 */     sb.append(this.needExternalData);
/* 40 */     sb.append("\ncachedPasswordIsGood: ");
/* 41 */     sb.append(this.cachedPasswordIsGood);
/* 42 */     sb.append("\nqueriedDbUserInfo: ");
/* 43 */     sb.append(this.queriedDbUserInfo);
/* 44 */     sb.append("\nqueriedDbUserAttributes: ");
/* 45 */     sb.append(this.queriedDbUserAttributes);
/* 46 */     sb.append("\nqueriedUserProvider: ");
/* 47 */     sb.append(this.queriedUserProvider);
/*    */ 
/* 49 */     return sb.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserStorageImplementorData
 * JD-Core Version:    0.5.4
 */