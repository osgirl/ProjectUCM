/*    */ package intradoc.gui;
/*    */ 
/*    */ import java.awt.Insets;
/*    */ 
/*    */ public class CustomPanel extends PanePanel
/*    */ {
/* 28 */   protected Insets m_insets = new Insets(5, 5, 5, 5);
/*    */ 
/*    */   public CustomPanel()
/*    */   {
/* 32 */     this.m_style = 8;
/*    */   }
/*    */ 
/*    */   public Insets getInsets()
/*    */   {
/* 38 */     return this.m_insets;
/*    */   }
/*    */ 
/*    */   public void setInsets(int top, int left, int bottom, int right)
/*    */   {
/* 43 */     this.m_insets.top = top;
/* 44 */     this.m_insets.left = left;
/* 45 */     this.m_insets.bottom = bottom;
/* 46 */     this.m_insets.right = right;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 51 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomPanel
 * JD-Core Version:    0.5.4
 */