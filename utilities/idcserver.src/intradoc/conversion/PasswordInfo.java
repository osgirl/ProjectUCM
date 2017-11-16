/*    */ package intradoc.conversion;
/*    */ 
/*    */ import java.util.Map;
/*    */ 
/*    */ public class PasswordInfo
/*    */ {
/* 24 */   public String m_password = null;
/* 25 */   public String m_algorithm = "Intradoc";
/*    */ 
/* 27 */   public String m_category = null;
/* 28 */   public String m_field = null;
/* 29 */   public String m_scope = null;
/* 30 */   public String m_key = null;
/* 31 */   public String m_extraEncoding = null;
/* 32 */   public long m_ts = -1L;
/*    */ 
/*    */   public PasswordInfo(String password, String algorithm, String scope, Map<String, String> catMap)
/*    */   {
/* 36 */     this.m_password = password;
/* 37 */     this.m_algorithm = algorithm;
/* 38 */     this.m_scope = scope;
/*    */ 
/* 40 */     if (catMap == null)
/*    */       return;
/* 42 */     this.m_category = ((String)catMap.get("scCategory"));
/* 43 */     this.m_field = ((String)catMap.get("scCategoryField"));
/* 44 */     this.m_extraEncoding = ((String)catMap.get("scExtraEncoding"));
/*    */   }
/*    */ 
/*    */   public PasswordInfo(String category, String algorithm, String password)
/*    */   {
/* 50 */     this.m_category = category;
/* 51 */     this.m_algorithm = algorithm;
/* 52 */     this.m_password = password;
/*    */   }
/*    */ 
/*    */   public void setPassword(String password)
/*    */   {
/* 57 */     this.m_password = password;
/*    */   }
/*    */ 
/*    */   public boolean isUnencrypted()
/*    */   {
/* 62 */     return (this.m_algorithm == null) || (this.m_algorithm.length() == 0) || (this.m_algorithm.equals("ClearText"));
/*    */   }
/*    */ 
/*    */   public boolean hasExtraEncoding()
/*    */   {
/* 67 */     return (this.m_extraEncoding != null) && (this.m_extraEncoding.length() > 0);
/*    */   }
/*    */ 
/*    */   public void update(Map<String, String> map)
/*    */   {
/* 72 */     this.m_password = ((String)map.get("scPassword"));
/* 73 */     this.m_algorithm = ((String)map.get("scPasswordEncoding"));
/*    */ 
/* 75 */     this.m_field = ((String)map.get("scPasswordField"));
/* 76 */     this.m_scope = ((String)map.get("scPasswordScope"));
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 82 */     String str = super.toString() + ": " + "m_password=" + this.m_password + ", " + "m_algorithm=" + this.m_algorithm + ", " + "m_category=" + this.m_category + ", " + "m_field=" + this.m_field + ", " + "m_scope=" + this.m_scope + ", " + "m_key=" + this.m_key + ", " + "m_extraEncoding=" + this.m_extraEncoding + ", " + "m_ts=" + this.m_ts + ".";
/*    */ 
/* 91 */     return str;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 96 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66344 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.conversion.PasswordInfo
 * JD-Core Version:    0.5.4
 */