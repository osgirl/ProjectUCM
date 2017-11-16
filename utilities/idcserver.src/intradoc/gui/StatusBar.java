/*    */ package intradoc.gui;
/*    */ 
/*    */ import java.awt.BorderLayout;
/*    */ import java.awt.Dimension;
/*    */ import java.awt.Insets;
/*    */ 
/*    */ public class StatusBar extends PanePanel
/*    */ {
/*    */   private CustomLabel m_label;
/*    */   private int m_height;
/*    */ 
/*    */   public StatusBar()
/*    */   {
/* 37 */     this.m_height = 23;
/* 38 */     this.m_style = 2;
/* 39 */     this.m_label = new CustomLabel();
/* 40 */     this.m_label.setHorizontalAlignment(2);
/* 41 */     setLayout(new BorderLayout());
/* 42 */     add("West", this.m_label);
/*    */   }
/*    */ 
/*    */   public void setHeight(int height)
/*    */   {
/* 47 */     this.m_height = height;
/*    */   }
/*    */ 
/*    */   public Dimension getPreferredSize()
/*    */   {
/* 53 */     return new Dimension(20, this.m_height);
/*    */   }
/*    */ 
/*    */   public Insets getInsets()
/*    */   {
/* 59 */     return new Insets(2, 5, 2, 5);
/*    */   }
/*    */ 
/*    */   public void setText(String text)
/*    */   {
/* 64 */     this.m_label.setText(text);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 69 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.StatusBar
 * JD-Core Version:    0.5.4
 */