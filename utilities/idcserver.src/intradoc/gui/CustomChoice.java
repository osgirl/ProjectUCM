/*    */ package intradoc.gui;
/*    */ 
/*    */ import java.awt.Dimension;
/*    */ import javax.swing.JComboBox;
/*    */ 
/*    */ public class CustomChoice extends JComboBox
/*    */ {
/*    */   public int m_minWidth;
/*    */ 
/*    */   public CustomChoice()
/*    */   {
/* 35 */     this.m_minWidth = 0;
/*    */   }
/*    */ 
/*    */   public void setMinWidth(int minWidth)
/*    */   {
/* 40 */     this.m_minWidth = minWidth;
/*    */   }
/*    */ 
/*    */   public Dimension getPreferredSize()
/*    */   {
/* 46 */     Dimension d = super.getPreferredSize();
/* 47 */     if ((this.m_minWidth > 0) && (d.width < this.m_minWidth))
/*    */     {
/* 49 */       d.width = this.m_minWidth;
/*    */     }
/*    */ 
/* 52 */     return d;
/*    */   }
/*    */ 
/*    */   public Dimension getMinimumSize()
/*    */   {
/* 58 */     Dimension d = super.getMinimumSize();
/* 59 */     if (this.m_minWidth > 0)
/*    */     {
/* 61 */       d.width = this.m_minWidth;
/*    */     }
/*    */ 
/* 64 */     return d;
/*    */   }
/*    */ 
/*    */   public void add(String str)
/*    */   {
/* 69 */     addItem(str);
/*    */   }
/*    */ 
/*    */   public void select(String str)
/*    */   {
/* 74 */     setSelectedItem(str);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 79 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78690 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomChoice
 * JD-Core Version:    0.5.4
 */