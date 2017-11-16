/*    */ package intradoc.common;
/*    */ 
/*    */ public class ScriptInfo
/*    */ {
/*    */   public ScriptExtensions m_extension;
/*    */   public Object m_entry;
/*    */   public String m_key;
/*    */   public ScriptInfo m_prior;
/*    */ 
/*    */   public ScriptInfo()
/*    */   {
/*    */   }
/*    */ 
/*    */   public ScriptInfo(ScriptExtensions extension, Object entry, String key)
/*    */   {
/* 38 */     this.m_extension = extension;
/* 39 */     this.m_entry = entry;
/* 40 */     this.m_key = key;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 45 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82554 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ScriptInfo
 * JD-Core Version:    0.5.4
 */