/*    */ package intradoc.apputilities.installer;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ 
/*    */ public class SettingInfo
/*    */ {
/*    */   public String m_name;
/*    */   public String m_value;
/* 26 */   public ArrayList m_appliedFlags = new ArrayList();
/*    */ 
/*    */   public SettingInfo(String name)
/*    */   {
/* 30 */     this.m_name = name;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 35 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66344 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.SettingInfo
 * JD-Core Version:    0.5.4
 */