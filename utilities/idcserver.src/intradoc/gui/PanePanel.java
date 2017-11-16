/*     */ package intradoc.gui;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Insets;
/*     */ import java.awt.SystemColor;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class PanePanel extends JPanel
/*     */ {
/*     */   public static final int NONE = 1;
/*     */   public static final int RAISED = 2;
/*     */   public static final int FLAT = 4;
/*     */   public static final int LOWERED = 8;
/*     */   public static final int GROOVED = 16;
/*     */   public static final int RIDGED = 32;
/*     */   public static final int ROUNDED = 64;
/*  43 */   protected boolean m_skipTop = false;
/*  44 */   protected boolean m_skipBottom = false;
/*  45 */   protected boolean m_skipLeft = false;
/*  46 */   protected boolean m_skipRight = false;
/*     */ 
/*  49 */   protected int m_style = 1;
/*     */ 
/*  52 */   protected int m_thickness = 2;
/*     */ 
/*  55 */   protected boolean m_useInsets = true;
/*     */ 
/*  57 */   protected Color m_color = Color.gray;
/*     */ 
/*     */   public PanePanel()
/*     */   {
/*  61 */     this.m_style = 1;
/*  62 */     setBackground(SystemColor.control);
/*  63 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public PanePanel(boolean useInsets)
/*     */   {
/*  70 */     this.m_useInsets = useInsets;
/*     */   }
/*     */ 
/*     */   public PanePanel(int style)
/*     */   {
/*  75 */     this.m_style = style;
/*  76 */     setBackground(SystemColor.control);
/*  77 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public PanePanel(int style, int width, int height)
/*     */   {
/*  82 */     this.m_style = 1;
/*  83 */     setSize(width, height);
/*  84 */     setBackground(SystemColor.control);
/*  85 */     setForeground(SystemColor.controlText);
/*     */   }
/*     */ 
/*     */   public Insets getInsets()
/*     */   {
/*  91 */     if (!this.m_useInsets)
/*     */     {
/*  93 */       return new Insets(0, 0, 0, 0);
/*     */     }
/*  95 */     int space = this.m_thickness + 1;
/*  96 */     int topSpace = space;
/*     */ 
/*  98 */     return new Insets(topSpace, space, space, space);
/*     */   }
/*     */ 
/*     */   public void setFont(Font f)
/*     */   {
/* 104 */     super.setFont(f);
/* 105 */     repaint();
/*     */   }
/*     */ 
/*     */   public void setStyle(int style)
/*     */   {
/* 110 */     this.m_style = style;
/* 111 */     repaint();
/*     */   }
/*     */ 
/*     */   public void setThickness(int thick)
/*     */   {
/* 116 */     this.m_thickness = thick;
/* 117 */     repaint();
/*     */   }
/*     */ 
/*     */   public void paint(Graphics g)
/*     */   {
/* 123 */     super.paint(g);
/*     */ 
/* 125 */     if (this.m_style == 1)
/*     */       return;
/* 127 */     drawFrame(g);
/*     */   }
/*     */ 
/*     */   public void drawFrame(Graphics g)
/*     */   {
/* 133 */     int offset = 0;
/* 134 */     int rounded = 0;
/* 135 */     Dimension bounds = getSize();
/*     */ 
/* 137 */     int j = 1;
/* 138 */     for (int i = 0; i < this.m_thickness; j += 2)
/*     */     {
/* 140 */       if (i != 0)
/*     */       {
/* 142 */         rounded = 0;
/*     */       }
/* 146 */       else if (isStyleSet(64))
/*     */       {
/* 148 */         rounded = 1;
/*     */       }
/*     */ 
/* 152 */       if ((isStyleSet(2)) || ((isStyleSet(32)) && (j < this.m_thickness)) || ((isStyleSet(16)) && (j > this.m_thickness)))
/*     */       {
/* 156 */         g.setColor(Color.white);
/*     */       }
/*     */       else
/*     */       {
/* 160 */         g.setColor(this.m_color);
/*     */       }
/*     */ 
/* 164 */       if (!this.m_skipTop)
/*     */       {
/* 166 */         g.fillRect(i + rounded, i + offset, bounds.width - 2 * (i + rounded), 1);
/*     */       }
/*     */ 
/* 173 */       if (!this.m_skipLeft)
/*     */       {
/* 175 */         g.fillRect(i, i + rounded + offset, 1, bounds.height - (2 * i + 1 + rounded) - offset);
/*     */       }
/*     */ 
/* 181 */       if ((isStyleSet(2)) || (isStyleSet(4)) || ((isStyleSet(32)) && (j < this.m_thickness)) || ((isStyleSet(16)) && (j > this.m_thickness)))
/*     */       {
/* 185 */         g.setColor(this.m_color);
/*     */       }
/*     */       else
/*     */       {
/* 189 */         g.setColor(Color.white);
/*     */       }
/*     */ 
/* 193 */       if (!this.m_skipBottom)
/*     */       {
/* 195 */         g.fillRect(i + rounded, bounds.height - (i + 1), bounds.width - 2 * (i + rounded), 1);
/*     */       }
/*     */ 
/* 202 */       if (!this.m_skipRight)
/*     */       {
/* 204 */         g.fillRect(bounds.width - (i + 1), i + rounded + offset, 1, bounds.height - (2 * i + 1 + rounded) - offset);
/*     */       }
/* 138 */       ++i;
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isStyleSet(int check)
/*     */   {
/* 214 */     return (this.m_style & check) != 0;
/*     */   }
/*     */ 
/*     */   public void setSkip(String which, boolean onOff)
/*     */   {
/* 219 */     if (which.equalsIgnoreCase("North"))
/*     */     {
/* 221 */       this.m_skipTop = onOff;
/*     */     }
/* 223 */     else if (which.equalsIgnoreCase("South"))
/*     */     {
/* 225 */       this.m_skipBottom = onOff;
/*     */     }
/* 227 */     else if (which.equalsIgnoreCase("East"))
/*     */     {
/* 229 */       this.m_skipRight = onOff;
/*     */     }
/* 231 */     else if (which.equalsIgnoreCase("West"))
/*     */     {
/* 233 */       this.m_skipLeft = onOff;
/*     */     } else {
/* 235 */       if (!which.equalsIgnoreCase("All"))
/*     */         return;
/* 237 */       this.m_skipTop = onOff;
/* 238 */       this.m_skipBottom = onOff;
/* 239 */       this.m_skipRight = onOff;
/* 240 */       this.m_skipLeft = onOff;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setColor(Color clr)
/*     */   {
/* 246 */     this.m_color = clr;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 251 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78496 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.PanePanel
 * JD-Core Version:    0.5.4
 */