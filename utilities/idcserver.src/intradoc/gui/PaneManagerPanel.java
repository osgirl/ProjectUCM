/*    */ package intradoc.gui;
/*    */ 
/*    */ import java.awt.CardLayout;
/*    */ import java.awt.Component;
/*    */ 
/*    */ public class PaneManagerPanel extends PanePanel
/*    */ {
/* 29 */   protected CardLayout m_layout = new CardLayout();
/*    */ 
/*    */   public PaneManagerPanel()
/*    */   {
/* 33 */     super(2);
/* 34 */     setLayout(this.m_layout);
/*    */ 
/* 36 */     setThickness(1);
/* 37 */     setSkip("North", true);
/*    */   }
/*    */ 
/*    */   public void addPane(String name, Component comp)
/*    */   {
/* 42 */     add(name, comp);
/*    */   }
/*    */ 
/*    */   public void selectPane(String paneName)
/*    */   {
/* 47 */     this.m_layout.show(this, paneName);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 52 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.PaneManagerPanel
 * JD-Core Version:    0.5.4
 */