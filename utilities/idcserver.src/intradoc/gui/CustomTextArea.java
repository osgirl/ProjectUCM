/*    */ package intradoc.gui;
/*    */ 
/*    */ import javax.swing.JTextArea;
/*    */ 
/*    */ public class CustomTextArea extends JTextArea
/*    */ {
/*    */   public CustomTextArea()
/*    */   {
/*    */   }
/*    */ 
/*    */   public CustomTextArea(int rows, int cols)
/*    */   {
/* 35 */     super(rows, cols);
/*    */   }
/*    */ 
/*    */   public CustomTextArea(String text)
/*    */   {
/* 40 */     super(text);
/*    */   }
/*    */ 
/*    */   public CustomTextArea(String text, int rows, int cols)
/*    */   {
/* 45 */     super(text, rows, cols);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 50 */     return "releaseInfo=dev,releaseRevision=$Rev: 79260 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomTextArea
 * JD-Core Version:    0.5.4
 */