/*    */ package intradoc.data;
/*    */ 
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class ParameterPacket
/*    */ {
/* 26 */   public String m_name = null;
/* 27 */   public Object m_primaryObject = null;
/* 28 */   public Map m_infoMap = null;
/*    */ 
/*    */   public ParameterPacket()
/*    */   {
/* 32 */     this.m_infoMap = new HashMap();
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 38 */     IdcStringBuilder str = new IdcStringBuilder();
/* 39 */     if (this.m_name != null)
/*    */     {
/* 41 */       str.append(this.m_name);
/* 42 */       str.append(": ");
/*    */     }
/* 44 */     str.append(this.m_primaryObject.toString());
/* 45 */     return str.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 50 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ParameterPacket
 * JD-Core Version:    0.5.4
 */