/*    */ package intradoc.gui;
/*    */ 
/*    */ import java.awt.Dimension;
/*    */ import javax.swing.JProgressBar;
/*    */ 
/*    */ public class ProgressBar extends JProgressBar
/*    */ {
/*    */   public ProgressBar(int width, int height)
/*    */   {
/* 32 */     super(0, 100);
/* 33 */     Dimension size = new Dimension(width, height);
/* 34 */     setMinimumSize(size);
/* 35 */     setPreferredSize(size);
/*    */   }
/*    */ 
/*    */   public void updateProgressBar(int incPercent)
/*    */   {
/* 40 */     setValue(incPercent);
/* 41 */     repaint();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 46 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.ProgressBar
 * JD-Core Version:    0.5.4
 */