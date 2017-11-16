/*    */ package intradoc.gui;
/*    */ 
/*    */ import java.awt.Scrollbar;
/*    */ 
/*    */ public class CustomScrollbar extends Scrollbar
/*    */ {
/*    */   protected boolean m_isJDK_1_2;
/*    */ 
/*    */   public CustomScrollbar()
/*    */   {
/* 31 */     checkBroken();
/*    */   }
/*    */ 
/*    */   public CustomScrollbar(int orientation)
/*    */   {
/* 36 */     super(orientation);
/* 37 */     checkBroken();
/*    */   }
/*    */ 
/*    */   public CustomScrollbar(int orientation, int value, int visible, int min, int max)
/*    */   {
/* 42 */     super(orientation);
/* 43 */     checkBroken();
/* 44 */     setValues(value, visible, min, max);
/*    */   }
/*    */ 
/*    */   protected void checkBroken()
/*    */   {
/* 49 */     this.m_isJDK_1_2 = true;
/*    */   }
/*    */ 
/*    */   public int getMaximum()
/*    */   {
/* 55 */     int max = super.getMaximum();
/* 56 */     if (!this.m_isJDK_1_2)
/*    */     {
/* 58 */       max -= super.getVisibleAmount();
/*    */     }
/* 60 */     return max;
/*    */   }
/*    */ 
/*    */   public void setMaximum(int max)
/*    */   {
/* 66 */     if (!this.m_isJDK_1_2)
/*    */     {
/* 68 */       max += super.getVisibleAmount();
/*    */     }
/* 70 */     super.setMaximum(max);
/*    */   }
/*    */ 
/*    */   public void setValues(int value, int visible, int min, int max)
/*    */   {
/* 76 */     if (!this.m_isJDK_1_2)
/*    */     {
/* 78 */       max += visible;
/*    */     }
/* 80 */     super.setValues(value, visible, min, max);
/*    */   }
/*    */ 
/*    */   public void setVisibleAmount(int visible)
/*    */   {
/* 86 */     int max = getMaximum();
/* 87 */     super.setValues(super.getValue(), visible, super.getMinimum(), max + visible);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 92 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomScrollbar
 * JD-Core Version:    0.5.4
 */