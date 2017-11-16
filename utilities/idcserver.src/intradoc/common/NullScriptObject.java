/*    */ package intradoc.common;
/*    */ 
/*    */ public class NullScriptObject
/*    */   implements ScriptObject
/*    */ {
/*    */   public int m_type;
/*    */ 
/*    */   NullScriptObject(int type)
/*    */   {
/* 38 */     this.m_type = type;
/*    */   }
/*    */ 
/*    */   public void appendRepresentativeString(IdcAppendable appendable)
/*    */   {
/*    */   }
/*    */ 
/*    */   public String getRepresentativeString()
/*    */   {
/* 49 */     return "";
/*    */   }
/*    */ 
/*    */   public int getType()
/*    */   {
/* 54 */     return this.m_type;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 60 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.NullScriptObject
 * JD-Core Version:    0.5.4
 */