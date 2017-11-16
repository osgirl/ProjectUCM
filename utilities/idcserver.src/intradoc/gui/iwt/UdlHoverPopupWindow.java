/*    */ package intradoc.gui.iwt;
/*    */ 
/*    */ import java.awt.Color;
/*    */ import java.awt.Dimension;
/*    */ import java.awt.FontMetrics;
/*    */ import java.awt.Graphics;
/*    */ import java.awt.Insets;
/*    */ import java.awt.Rectangle;
/*    */ import javax.swing.JFrame;
/*    */ import javax.swing.JWindow;
/*    */ 
/*    */ public class UdlHoverPopupWindow extends JWindow
/*    */ {
/*    */   public String m_finalText;
/*    */ 
/*    */   public UdlHoverPopupWindow(JFrame ancestor, String finalText)
/*    */   {
/* 38 */     super(ancestor);
/* 39 */     this.m_finalText = finalText;
/*    */   }
/*    */ 
/*    */   public void paint(Graphics g)
/*    */   {
/* 45 */     Dimension d = getSize();
/*    */ 
/* 47 */     g.setColor(Color.yellow.brighter());
/* 48 */     g.fillRect(0, 0, d.width, d.height);
/*    */ 
/* 50 */     g.setColor(Color.black);
/* 51 */     g.drawRect(0, 0, d.width - 1, d.height - 1);
/* 52 */     Insets insets = getInsets();
/* 53 */     Rectangle r = new Rectangle();
/* 54 */     r.x = (6 + insets.left);
/* 55 */     r.width = (d.width - 12 - insets.left - insets.right);
/* 56 */     r.y = (2 + insets.top);
/* 57 */     r.height = (d.height - 4 - insets.top - insets.bottom);
/* 58 */     Dimension s = DrawUtils.drawText(g, this.m_finalText, r);
/* 59 */     String warning = getWarningString();
/* 60 */     if (warning != null)
/*    */     {
/* 62 */       FontMetrics f = g.getFontMetrics();
/* 63 */       int width = f.stringWidth(warning);
/* 64 */       s.width = Math.max(width, s.width);
/*    */     }
/* 66 */     if ((s.width <= r.width) && (s.height <= r.height))
/*    */       return;
/* 68 */     setSize(s.width + 12 + insets.left + insets.right, s.height + 4 + insets.top + insets.bottom);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 76 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.UdlHoverPopupWindow
 * JD-Core Version:    0.5.4
 */