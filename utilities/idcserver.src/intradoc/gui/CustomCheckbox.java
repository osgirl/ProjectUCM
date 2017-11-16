/*    */ package intradoc.gui;
/*    */ 
/*    */ import javax.swing.ButtonGroup;
/*    */ import javax.swing.JCheckBox;
/*    */ 
/*    */ public class CustomCheckbox extends JCheckBox
/*    */ {
/*    */   public CustomCheckbox()
/*    */   {
/*    */   }
/*    */ 
/*    */   public CustomCheckbox(String label)
/*    */   {
/* 36 */     super(label);
/* 37 */     GuiStyles.setCustomStyle(this, 1);
/*    */   }
/*    */ 
/*    */   public CustomCheckbox(String label, int style)
/*    */   {
/* 42 */     super(label);
/* 43 */     GuiStyles.setCustomStyle(this, style);
/*    */   }
/*    */ 
/*    */   public CustomCheckbox(String label, boolean state)
/*    */   {
/* 48 */     super(label, state);
/* 49 */     GuiStyles.setCustomStyle(this, 1);
/*    */   }
/*    */ 
/*    */   public CustomCheckbox(String label, ButtonGroup group)
/*    */   {
/* 54 */     super(label, false);
/* 55 */     group.add(this);
/* 56 */     GuiStyles.setCustomStyle(this, 1);
/*    */   }
/*    */ 
/*    */   public CustomCheckbox(String label, ButtonGroup group, boolean state)
/*    */   {
/* 61 */     super(label, state);
/* 62 */     group.add(this);
/* 63 */     GuiStyles.setCustomStyle(this, 1);
/*    */   }
/*    */ 
/*    */   public CustomCheckbox(String label, boolean state, ButtonGroup group)
/*    */   {
/* 68 */     super(label, state);
/* 69 */     group.add(this);
/* 70 */     GuiStyles.setCustomStyle(this, 1);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 75 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomCheckbox
 * JD-Core Version:    0.5.4
 */