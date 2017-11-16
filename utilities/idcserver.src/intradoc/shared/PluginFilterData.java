/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.common.SystemUtils;
/*    */ 
/*    */ public class PluginFilterData
/*    */ {
/*    */   public String m_filterType;
/*    */   public String m_location;
/*    */   public String m_parameter;
/*    */   public int m_order;
/*    */ 
/*    */   public PluginFilterData()
/*    */   {
/* 31 */     this.m_filterType = null;
/*    */ 
/* 36 */     this.m_location = null;
/*    */ 
/* 41 */     this.m_parameter = null;
/*    */ 
/* 46 */     this.m_order = 0;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 51 */     StringBuffer buf = new StringBuffer();
/* 52 */     buf.append(this.m_filterType);
/* 53 */     buf.append("->");
/* 54 */     buf.append(this.m_location);
/* 55 */     buf.append('(');
/* 56 */     buf.append(this.m_parameter);
/* 57 */     buf.append(')');
/* 58 */     if (SystemUtils.m_verbose)
/*    */     {
/* 60 */       buf.append(" ");
/* 61 */       buf.append(super.toString());
/*    */     }
/* 63 */     return buf.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 68 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.PluginFilterData
 * JD-Core Version:    0.5.4
 */