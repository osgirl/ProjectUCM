/*    */ package intradoc.gui;
/*    */ 
/*    */ import java.awt.Container;
/*    */ import java.awt.Dimension;
/*    */ import java.awt.FlowLayout;
/*    */ 
/*    */ public class CustomFlowLayout extends FlowLayout
/*    */ {
/*    */   public boolean m_doDoubleColumnLogic;
/*    */ 
/*    */   public CustomFlowLayout()
/*    */   {
/* 39 */     this.m_doDoubleColumnLogic = false;
/*    */   }
/*    */ 
/*    */   public Dimension minimumLayoutSize(Container c)
/*    */   {
/* 44 */     Dimension d = super.minimumLayoutSize(c);
/* 45 */     d.width -= getHgap();
/* 46 */     Dimension realSize = c.getSize();
/*    */ 
/* 48 */     if ((realSize.width > 0) && (this.m_doDoubleColumnLogic) && (realSize.width < d.width))
/*    */     {
/* 51 */       d.height = (7 * d.height / 4);
/*    */     }
/*    */ 
/* 54 */     return d;
/*    */   }
/*    */ 
/*    */   public Dimension preferredLayoutSize(Container c)
/*    */   {
/* 59 */     Dimension d = super.preferredLayoutSize(c);
/* 60 */     d.width -= getHgap();
/*    */ 
/* 69 */     return d;
/*    */   }
/*    */ 
/*    */   public void layoutContainer(Container c)
/*    */   {
/* 75 */     Dimension realSize = c.getSize();
/* 76 */     Dimension d = minimumLayoutSize(c);
/*    */ 
/* 78 */     if (realSize.width < d.width)
/*    */     {
/* 80 */       if (!this.m_doDoubleColumnLogic)
/*    */       {
/* 82 */         this.m_doDoubleColumnLogic = true;
/* 83 */         c.invalidate();
/* 84 */         c.getParent().validate();
/*    */       }
/*    */ 
/*    */     }
/*    */     else {
/* 89 */       this.m_doDoubleColumnLogic = false;
/*    */     }
/*    */ 
/* 92 */     super.layoutContainer(c);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 99 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomFlowLayout
 * JD-Core Version:    0.5.4
 */