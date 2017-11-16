/*    */ package intradoc.gui;
/*    */ 
/*    */ import javax.swing.JPasswordField;
/*    */ 
/*    */ public class CustomPasswordField extends JPasswordField
/*    */ {
/*    */   public CustomPasswordField()
/*    */   {
/*    */   }
/*    */ 
/*    */   public CustomPasswordField(int cols)
/*    */   {
/* 31 */     super(cols);
/*    */   }
/*    */ 
/*    */   public CustomPasswordField(String text)
/*    */   {
/* 36 */     super(text);
/*    */   }
/*    */ 
/*    */   public CustomPasswordField(String text, int cols)
/*    */   {
/* 41 */     super(text, cols);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 46 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomPasswordField
 * JD-Core Version:    0.5.4
 */