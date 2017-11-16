/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Font;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ 
/*     */ public class CustomText extends PanePanel
/*     */   implements TextHandler
/*     */ {
/*     */   public static final int LEFT = 0;
/*     */   public static final int CENTER = 1;
/*     */   public static final int RIGHT = 2;
/*     */   protected String m_text;
/*     */   protected Vector m_lines;
/*     */   protected int m_numLines;
/*     */   protected int m_maxChars;
/*     */   protected int m_marginWidth;
/*     */   protected int m_marginHeight;
/*     */   protected int m_lineHeight;
/*     */   protected int m_lineAscent;
/*     */   protected int[] m_lineWidth;
/*     */   protected int m_maxWidth;
/*  49 */   protected int m_alignment = 0;
/*     */ 
/*     */   public CustomText(String text, int maxChars, int marginWidth, int marginHeight, int alignment)
/*     */   {
/*  55 */     this.m_maxChars = maxChars;
/*  56 */     this.m_text = text;
/*  57 */     newText();
/*  58 */     this.m_marginWidth = marginWidth;
/*  59 */     this.m_marginHeight = marginHeight;
/*  60 */     this.m_alignment = alignment;
/*     */ 
/*  63 */     getAccessibleContext().setAccessibleName(text);
/*  64 */     setFocusable(true);
/*     */   }
/*     */ 
/*     */   public CustomText(String text, int maxChars, int marginWidth, int marginHeight)
/*     */   {
/*  70 */     this(text, maxChars, marginWidth, marginHeight, 0);
/*     */   }
/*     */ 
/*     */   public CustomText(String text, int maxChars, int alignment)
/*     */   {
/*  75 */     this(text, maxChars, 10, 10, alignment);
/*     */   }
/*     */ 
/*     */   public CustomText(String text, int maxChars)
/*     */   {
/*  80 */     this(text, maxChars, 10, 10, 0);
/*     */   }
/*     */ 
/*     */   public CustomText(String text)
/*     */   {
/*  85 */     this(text, -1, 10, 10, 0);
/*     */   }
/*     */ 
/*     */   public CustomText()
/*     */   {
/*  90 */     this("CustomText");
/*     */   }
/*     */ 
/*     */   protected void newText()
/*     */   {
/*  95 */     this.m_lines = new IdcVector();
/*  96 */     int len = this.m_text.length();
/*  97 */     int startFrom = 0;
/*     */ 
/*  99 */     while ((startFrom != -1) && (startFrom < len))
/*     */     {
/* 103 */       int index = this.m_text.indexOf("\n", startFrom);
/*     */       String line;
/* 105 */       if (index == -1)
/*     */       {
/* 107 */         String line = this.m_text.substring(startFrom);
/* 108 */         startFrom = -1;
/*     */       }
/*     */       else
/*     */       {
/* 112 */         line = this.m_text.substring(startFrom, index);
/* 113 */         startFrom = index + 1;
/*     */       }
/*     */ 
/* 117 */       if ((this.m_maxChars == -1) || (line.length() <= this.m_maxChars))
/*     */       {
/* 119 */         this.m_lines.addElement(line);
/*     */       }
/*     */       else
/*     */       {
/* 123 */         while (line.length() > this.m_maxChars)
/*     */         {
/* 125 */           int offset = line.lastIndexOf(32, this.m_maxChars);
/* 126 */           if (offset == -1)
/*     */           {
/* 129 */             offset = line.indexOf(32);
/*     */ 
/* 131 */             if (offset == -1)
/*     */             {
/* 133 */               offset = this.m_maxChars;
/*     */             }
/*     */           }
/*     */ 
/* 137 */           this.m_lines.addElement(line.substring(0, offset));
/* 138 */           line = line.substring(offset + 1);
/*     */         }
/*     */ 
/* 141 */         this.m_lines.addElement(line);
/*     */       }
/*     */     }
/*     */ 
/* 145 */     this.m_numLines = this.m_lines.size();
/* 146 */     this.m_lineWidth = new int[this.m_numLines];
/*     */   }
/*     */ 
/*     */   protected void measure()
/*     */   {
/* 153 */     if (getFont() == null) {
/* 154 */       return;
/*     */     }
/* 156 */     FontMetrics fm = getFontMetrics(getFont());
/*     */ 
/* 159 */     if (fm == null) {
/* 160 */       return;
/*     */     }
/* 162 */     this.m_lineHeight = fm.getHeight();
/* 163 */     this.m_lineAscent = fm.getAscent();
/* 164 */     this.m_maxWidth = 0;
/*     */ 
/* 166 */     for (int i = 0; i < this.m_numLines; ++i)
/*     */     {
/* 168 */       this.m_lineWidth[i] = fm.stringWidth((String)this.m_lines.elementAt(i));
/*     */ 
/* 170 */       if (this.m_lineWidth[i] <= this.m_maxWidth)
/*     */         continue;
/* 172 */       this.m_maxWidth = this.m_lineWidth[i];
/*     */     }
/*     */ 
/* 176 */     Dimension d = getPreferredSize();
/* 177 */     setSize(d);
/*     */   }
/*     */ 
/*     */   public void setText(String text)
/*     */   {
/* 184 */     this.m_text = text;
/* 185 */     newText();
/* 186 */     measure();
/* 187 */     repaint();
/*     */ 
/* 190 */     getAccessibleContext().setAccessibleName(text);
/* 191 */     setFocusable(true);
/*     */   }
/*     */ 
/*     */   public String getText()
/*     */   {
/* 196 */     return this.m_text;
/*     */   }
/*     */ 
/*     */   public void setMaxColumns(int maxCols)
/*     */   {
/* 201 */     if (maxCols < 0) {
/* 202 */       maxCols = -1;
/*     */     }
/* 204 */     this.m_maxChars = maxCols;
/* 205 */     newText();
/* 206 */     measure();
/* 207 */     repaint();
/*     */   }
/*     */ 
/*     */   public int getMaxColumns()
/*     */   {
/* 212 */     return this.m_maxChars;
/*     */   }
/*     */ 
/*     */   public void setFont(Font f)
/*     */   {
/* 218 */     super.setFont(f);
/* 219 */     measure();
/* 220 */     repaint();
/*     */   }
/*     */ 
/*     */   public void setForeground(Color c)
/*     */   {
/* 226 */     super.setForeground(c);
/* 227 */     repaint();
/*     */   }
/*     */ 
/*     */   public void setAlignment(int a)
/*     */   {
/* 232 */     this.m_alignment = a;
/* 233 */     repaint();
/*     */   }
/*     */ 
/*     */   public void setMarginWidth(int mw)
/*     */   {
/* 238 */     this.m_marginWidth = mw;
/* 239 */     repaint();
/*     */   }
/*     */ 
/*     */   public void setMarginHeight(int mh)
/*     */   {
/* 244 */     this.m_marginHeight = mh;
/* 245 */     repaint();
/*     */   }
/*     */ 
/*     */   public int getAlignment()
/*     */   {
/* 250 */     return this.m_alignment;
/*     */   }
/*     */ 
/*     */   public int getMarginWidth()
/*     */   {
/* 255 */     return this.m_marginWidth;
/*     */   }
/*     */ 
/*     */   public int getMarginHeight()
/*     */   {
/* 260 */     return this.m_marginHeight;
/*     */   }
/*     */ 
/*     */   public void addNotify()
/*     */   {
/* 266 */     super.addNotify();
/* 267 */     measure();
/*     */   }
/*     */ 
/*     */   public Dimension getPreferredSize()
/*     */   {
/* 273 */     Dimension d = super.getPreferredSize();
/*     */ 
/* 275 */     d.width = (this.m_maxWidth + this.m_marginWidth);
/* 276 */     d.height = (this.m_numLines * this.m_lineHeight + this.m_marginHeight);
/*     */ 
/* 278 */     return d;
/*     */   }
/*     */ 
/*     */   public Dimension getMinimumSize()
/*     */   {
/* 284 */     Dimension d = super.getMinimumSize();
/*     */ 
/* 286 */     d.width = this.m_maxWidth;
/* 287 */     d.height = (this.m_numLines * this.m_lineHeight);
/*     */ 
/* 289 */     return d;
/*     */   }
/*     */ 
/*     */   public void paint(Graphics g)
/*     */   {
/* 296 */     Dimension d = getSize();
/*     */ 
/* 298 */     int mw = Math.max((d.width - this.m_maxWidth) / 2, 0);
/* 299 */     if (mw > this.m_marginWidth)
/*     */     {
/* 301 */       mw = this.m_marginWidth;
/*     */     }
/*     */ 
/* 304 */     int y = this.m_lineAscent + (d.height - this.m_numLines * this.m_lineHeight) / 2;
/*     */ 
/* 306 */     for (int i = 0; i < this.m_numLines; y += this.m_lineHeight)
/*     */     {
/*     */       int x;
/* 308 */       switch (this.m_alignment)
/*     */       {
/*     */       case 0:
/* 311 */         x = 0;
/* 312 */         break;
/*     */       case 1:
/*     */       default:
/* 315 */         x = (d.width - this.m_lineWidth[i]) / 2;
/* 316 */         break;
/*     */       case 2:
/* 318 */         x = d.width - this.m_lineWidth[i];
/*     */       }
/*     */ 
/* 326 */       g.drawString(((String)this.m_lines.elementAt(i)).trim(), x, y);
/*     */ 
/* 306 */       ++i;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 332 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84769 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.CustomText
 * JD-Core Version:    0.5.4
 */