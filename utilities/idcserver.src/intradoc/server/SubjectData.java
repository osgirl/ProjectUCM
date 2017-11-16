/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class SubjectData
/*    */ {
/*    */   public String m_name;
/*    */   public long m_counter;
/*    */   public long m_marker;
/*    */   public boolean m_externalChanged;
/*    */   public boolean m_internalChanged;
/*    */   public Vector m_callbacks;
/*    */   public Vector m_monitors;
/*    */ 
/*    */   public SubjectData(String name)
/*    */   {
/* 40 */     this.m_name = name;
/* 41 */     this.m_counter = System.currentTimeMillis();
/* 42 */     this.m_marker = -2L;
/* 43 */     this.m_externalChanged = false;
/* 44 */     this.m_internalChanged = false;
/* 45 */     this.m_callbacks = new IdcVector();
/* 46 */     this.m_monitors = new IdcVector();
/*    */   }
/*    */ 
/*    */   public void addCallback(SubjectCallback callback)
/*    */   {
/* 51 */     this.m_callbacks.addElement(callback);
/*    */   }
/*    */ 
/*    */   public void addMonitor(SubjectEventMonitor monitor)
/*    */   {
/* 56 */     this.m_monitors.addElement(monitor);
/*    */   }
/*    */ 
/*    */   public void copyShallow(SubjectData data)
/*    */   {
/* 61 */     this.m_counter = data.m_counter;
/* 62 */     this.m_callbacks = ((Vector)data.m_callbacks.clone());
/* 63 */     this.m_monitors = ((Vector)data.m_monitors.clone());
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 68 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SubjectData
 * JD-Core Version:    0.5.4
 */