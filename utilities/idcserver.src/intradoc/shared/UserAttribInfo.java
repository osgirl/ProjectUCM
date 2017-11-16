/*    */ package intradoc.shared;
/*    */ 
/*    */ public class UserAttribInfo
/*    */ {
/*    */   public String m_attribType;
/*    */   public String m_attribName;
/*    */   public int m_attribPrivilege;
/*    */ 
/*    */   public UserAttribInfo()
/*    */   {
/* 42 */     this.m_attribType = "role";
/* 43 */     this.m_attribName = "guest";
/* 44 */     this.m_attribPrivilege = 15;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 50 */     return this.m_attribType + ":" + this.m_attribName + "(" + this.m_attribPrivilege + ")";
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 55 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.UserAttribInfo
 * JD-Core Version:    0.5.4
 */