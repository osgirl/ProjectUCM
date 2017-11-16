/*    */ package intradoc.gui;
/*    */ 
/*    */ import java.awt.event.TextListener;
/*    */ import java.util.Collection;
/*    */ import java.util.HashSet;
/*    */ import javax.accessibility.AccessibleContext;
/*    */ import javax.swing.JTextField;
/*    */ 
/*    */ public class CustomTextField extends JTextField
/*    */ {
/* 32 */   public Collection<TextListener> m_textListeners = new HashSet();
/*    */ 
/*    */   public CustomTextField()
/*    */   {
/*    */   }
/*    */ 
/*    */   public CustomTextField(int cols)
/*    */   {
/* 41 */     super(cols);
/*    */   }
/*    */ 
/*    */   public CustomTextField(String text)
/*    */   {
/* 46 */     super(text);
/* 47 */     getAccessibleContext().setAccessibleDescription(text);
/* 48 */     setFocusable(true);
/*    */   }
/*    */ 
/*    */   public CustomTextField(String text, int cols)
/*    */   {
/* 53 */     super(text, cols);
/* 54 */     getAccessibleContext().setAccessibleDescription(text);
/* 55 */     setFocusable(true);
/*    */   }
/*    */ 
/*    */   public void addTextListener(TextListener l)
/*    */   {
/* 60 */     this.m_textListeners.add(l);
/*    */   }
/*    */ 
/*    */   public void removeTextListener(TextListener l)
/*    */   {
/* 65 */     this.m_textListeners.remove(l);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 70 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomTextField
 * JD-Core Version:    0.5.4
 */