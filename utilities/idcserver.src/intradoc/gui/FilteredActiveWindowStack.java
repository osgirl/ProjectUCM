/*    */ package intradoc.gui;
/*    */ 
/*    */ import java.awt.Component;
/*    */ 
/*    */ public class FilteredActiveWindowStack
/*    */ {
/*    */   public Component m_activeWindow;
/*    */   public Component m_topParent;
/*    */   public Component m_immediateWindowParent;
/*    */   public boolean m_hasChild;
/*    */   public FilteredActiveWindowStack m_previousStackElement;
/*    */ 
/*    */   public FilteredActiveWindowStack(Component activeWindow, Component topParent, Component immediateWindowParent)
/*    */   {
/* 37 */     this.m_activeWindow = activeWindow;
/* 38 */     this.m_topParent = topParent;
/* 39 */     this.m_immediateWindowParent = immediateWindowParent;
/* 40 */     this.m_hasChild = false;
/*    */ 
/* 42 */     this.m_previousStackElement = null;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 48 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.FilteredActiveWindowStack
 * JD-Core Version:    0.5.4
 */