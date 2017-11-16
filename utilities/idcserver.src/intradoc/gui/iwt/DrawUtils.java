/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.FontMetrics;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Polygon;
/*     */ import java.awt.Rectangle;
/*     */ import java.awt.Shape;
/*     */ import java.awt.SystemColor;
/*     */ 
/*     */ public class DrawUtils
/*     */ {
/*     */   public static String drawTruncatedString(Graphics g, String text, int x, int y, int w, String suffixText)
/*     */   {
/*  52 */     FontMetrics f = g.getFontMetrics();
/*  53 */     return drawTruncatedStringEx(g, f, text, x, y, w, suffixText);
/*     */   }
/*     */ 
/*     */   public static String drawTruncatedStringEx(Graphics g, FontMetrics f, String text, int x, int y, int w, String suffixText)
/*     */   {
/*  74 */     StringBuffer s = new StringBuffer(text);
/*  75 */     String rc = "";
/*  76 */     while (s.length() > 0)
/*     */     {
/*  78 */       rc = new String(s);
/*  79 */       if (!rc.equals(text))
/*     */       {
/*  81 */         rc = rc + suffixText;
/*     */       }
/*  83 */       if (f.stringWidth(rc) <= w)
/*     */         break;
/*  85 */       s.setLength(s.length() - 1);
/*  86 */       rc = "";
/*     */     }
/*     */ 
/*  94 */     if (g != null)
/*     */     {
/*  96 */       g.drawString(rc, x, y);
/*     */     }
/*  98 */     return rc;
/*     */   }
/*     */ 
/*     */   public static void drawAlignedString(Graphics g, String text, Rectangle r, int alignment)
/*     */   {
/* 113 */     FontMetrics f = g.getFontMetrics();
/* 114 */     int h = f.getMaxAscent();
/* 115 */     int width = f.stringWidth(text);
/*     */     int x;
/*     */     int y;
/* 118 */     switch (alignment)
/*     */     {
/*     */     case 10:
/* 121 */       x = r.x + (r.width - width) / 2;
/* 122 */       y = r.y + (r.height - h) / 2;
/* 123 */       break;
/*     */     case 11:
/* 125 */       x = r.x + (r.width - width) / 2;
/* 126 */       y = r.y + h;
/* 127 */       break;
/*     */     case 12:
/* 129 */       x = r.x + r.width - width;
/* 130 */       y = r.y + h;
/* 131 */       break;
/*     */     case 13:
/* 133 */       x = r.x + r.width - width;
/* 134 */       y = r.y + (r.height - h) / 2;
/* 135 */       break;
/*     */     case 14:
/* 137 */       x = r.x + r.width - width;
/* 138 */       y = r.y + r.height - h;
/* 139 */       break;
/*     */     case 15:
/* 141 */       x = r.x + (r.width - width) / 2;
/* 142 */       y = r.y + r.height - h;
/* 143 */       break;
/*     */     case 16:
/* 145 */       x = r.x;
/* 146 */       y = r.x + r.height - h;
/* 147 */       break;
/*     */     case 17:
/* 149 */       x = r.x;
/* 150 */       y = r.y + (r.height - h) / 2;
/* 151 */       break;
/*     */     case 18:
/*     */     default:
/* 154 */       x = r.x;
/* 155 */       y = r.y + h;
/*     */     }
/*     */ 
/* 158 */     g.drawString(text, x, y);
/*     */   }
/*     */ 
/*     */   public static Dimension drawText(Graphics g, String text, Rectangle bounds)
/*     */   {
/* 172 */     FontMetrics f = g.getFontMetrics();
/* 173 */     int h = f.getMaxAscent();
/* 174 */     int l = f.getHeight();
/* 175 */     int y = bounds.y + h;
/* 176 */     Dimension d = new Dimension(0, 0);
/*     */ 
/* 178 */     while (text.length() > 0)
/*     */     {
/* 180 */       int i = text.indexOf("\n");
/*     */       String line;
/* 182 */       if (i == -1)
/*     */       {
/* 184 */         String line = text;
/* 185 */         text = "";
/*     */       }
/*     */       else
/*     */       {
/* 189 */         line = text.substring(0, i);
/* 190 */         text = text.substring(i + 1);
/*     */       }
/* 192 */       int width = f.stringWidth(line);
/* 193 */       d.width = ((width > d.width) ? width : d.width);
/* 194 */       if (y <= bounds.y + bounds.height)
/*     */       {
/* 196 */         g.drawString(line, bounds.x, y);
/*     */       }
/* 198 */       y += l;
/*     */     }
/* 200 */     d.height = (y - bounds.y + f.getMaxDescent() - l);
/* 201 */     return d;
/*     */   }
/*     */ 
/*     */   public static void paintButton(Graphics g, Rectangle l, boolean isDown)
/*     */   {
/* 217 */     Color lightColor = SystemColor.controlLtHighlight;
/* 218 */     Color darkColor = SystemColor.controlDkShadow;
/*     */ 
/* 220 */     Color oldColor = g.getColor();
/* 221 */     Shape clip = g.getClip();
/* 222 */     g.setClip(l);
/*     */ 
/* 224 */     g.setColor((isDown) ? lightColor : darkColor);
/* 225 */     g.drawRect(l.x, l.y + l.height - 2, l.width - 1, 1);
/* 226 */     g.drawRect(l.x + l.width - 2, l.y, 1, l.height - 1);
/* 227 */     g.setColor((isDown) ? darkColor : lightColor);
/* 228 */     g.drawRect(l.x, l.y - 1, l.width - 2, 1);
/* 229 */     g.drawRect(l.x - 1, l.y, 1, l.height - 2);
/*     */ 
/* 231 */     g.setColor(oldColor);
/* 232 */     g.setClip(clip);
/*     */   }
/*     */ 
/*     */   public static void drawDottedLine(Graphics g, int x1, int y1, int x2, int y2)
/*     */   {
/* 245 */     int dx = x2 - x1;
/* 246 */     int dy = y2 - y1;
/*     */     do
/*     */     {
/* 249 */       boolean draw = (x1 + y1 & 0x1) == 0;
/* 250 */       if (draw)
/*     */       {
/* 252 */         g.drawLine(x1, y1, x1, y1);
/*     */       }
/*     */       int iy;
/*     */       int iy;
/*     */       int ix;
/* 256 */       if (Math.abs(dx) > Math.abs(dy))
/*     */       {
/* 258 */         int ix = (x1 != x2) ? dx / Math.abs(dx) : 0;
/* 259 */         iy = (y1 != y2) ? Math.round(dy / dx) : 0;
/*     */       }
/*     */       else
/*     */       {
/* 263 */         iy = (y1 != y2) ? dy / Math.abs(dy) : 0;
/* 264 */         ix = (x1 != x2) ? Math.round(dx / dy) : 0;
/*     */       }
/* 266 */       x1 += ix;
/* 267 */       y1 += iy;
/*     */     }
/* 269 */     while ((x1 != x2) || (y1 != y2));
/*     */   }
/*     */ 
/*     */   public static int drawCheckbox(Graphics g, int x, int y, boolean isChecked)
/*     */   {
/* 277 */     FontMetrics f = g.getFontMetrics();
/* 278 */     int l = f.getHeight();
/* 279 */     double ratio1 = 0.6666666666666666D;
/* 280 */     double ratio2 = 0.1666666666666667D;
/* 281 */     int s = (int)(ratio1 * l);
/* 282 */     x += (int)(ratio2 * l);
/* 283 */     y += (int)(ratio2 * l);
/*     */ 
/* 285 */     g.drawRect(x, y, s, s);
/* 286 */     if (isChecked)
/*     */     {
/* 288 */       if (s < 6)
/*     */       {
/* 290 */         g.fillRect(x, y, s, s);
/*     */       }
/*     */       else
/*     */       {
/* 294 */         Polygon p = new Polygon();
/* 295 */         p.addPoint(x + s * 1 / 5, y + s * 3 / 5);
/* 296 */         p.addPoint(x + s * 2 / 5, y + s * 4 / 5 + 1);
/* 297 */         p.addPoint(x + s * 4 / 5 + 1, y + s * 2 / 5);
/* 298 */         p.addPoint(x + s * 4 / 5 + 1, y + s * 1 / 5 - 1);
/* 299 */         p.addPoint(x + s * 2 / 5, y + s * 3 / 5);
/* 300 */         p.addPoint(x + s * 1 / 5, y + s * 2 / 5 - 1);
/* 301 */         g.fillPolygon(p);
/*     */       }
/*     */     }
/*     */ 
/* 305 */     return l;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 310 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.DrawUtils
 * JD-Core Version:    0.5.4
 */