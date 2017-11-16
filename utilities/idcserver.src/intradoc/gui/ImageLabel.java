/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.common.GuiUtils;
/*     */ import java.awt.Color;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Image;
/*     */ 
/*     */ public class ImageLabel extends PanePanel
/*     */ {
/*     */   private Image m_image;
/*  34 */   private int m_width = -1;
/*  35 */   private int m_height = -1;
/*  36 */   boolean m_isDone = false;
/*  37 */   boolean m_isError = false;
/*  38 */   private boolean m_isFixedSize = false;
/*     */ 
/*     */   public ImageLabel(int width, int height, boolean isFixed)
/*     */   {
/*  43 */     this.m_image = null;
/*  44 */     this.m_width = width;
/*  45 */     this.m_height = height;
/*  46 */     this.m_isFixedSize = isFixed;
/*     */   }
/*     */ 
/*     */   public ImageLabel(Image image)
/*     */   {
/*  51 */     setImage(image);
/*     */   }
/*     */ 
/*     */   public void setImage(Image image)
/*     */   {
/*  57 */     invalidate();
/*  58 */     this.m_image = image;
/*  59 */     this.m_isDone = false;
/*  60 */     this.m_isError = false;
/*  61 */     if (image != null)
/*     */     {
/*  63 */       changeSize(this.m_image.getWidth(this), this.m_image.getHeight(this));
/*     */     }
/*  65 */     repaint();
/*     */   }
/*     */ 
/*     */   public void setSize(int width, int height)
/*     */   {
/*  71 */     if (this.m_isFixedSize)
/*     */       return;
/*  73 */     super.setSize(width, height);
/*  74 */     this.m_width = width;
/*  75 */     this.m_height = height;
/*     */   }
/*     */ 
/*     */   private void changeSize(int w, int h)
/*     */   {
/*  81 */     if ((this.m_isFixedSize == true) || (w == -1) || (h == -1))
/*     */     {
/*  83 */       return;
/*     */     }
/*  85 */     if ((this.m_width == w) && (this.m_height == h))
/*     */       return;
/*  87 */     this.m_width = w;
/*  88 */     this.m_height = h;
/*  89 */     setSize(this.m_width, this.m_height);
/*  90 */     GuiUtils.packWindow(this);
/*     */   }
/*     */ 
/*     */   public Dimension getPreferredSize()
/*     */   {
/*  97 */     return getMinimumSize();
/*     */   }
/*     */ 
/*     */   public Dimension getMinimumSize()
/*     */   {
/* 103 */     return new Dimension(this.m_width, this.m_height);
/*     */   }
/*     */ 
/*     */   public void update(Graphics graphics)
/*     */   {
/* 111 */     paint(graphics);
/*     */   }
/*     */ 
/*     */   public void paint(Graphics graphics)
/*     */   {
/* 117 */     super.paint(graphics);
/* 118 */     if (this.m_image != null)
/*     */     {
/* 120 */       graphics.drawImage(this.m_image, 0, 0, this.m_width, this.m_height, this);
/*     */     }
/*     */     else
/*     */     {
/* 124 */       Color clr = graphics.getColor();
/* 125 */       graphics.setColor(getBackground());
/* 126 */       graphics.fillRect(0, 0, this.m_width, this.m_height);
/* 127 */       graphics.setColor(clr);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h)
/*     */   {
/* 134 */     boolean errorFlag = (infoflags & 0x40) != 0;
/* 135 */     boolean abortFlag = (infoflags & 0x80) != 0;
/* 136 */     boolean widthFlag = (infoflags & 0x1) != 0;
/* 137 */     boolean heightFlag = (infoflags & 0x2) != 0;
/* 138 */     boolean somebitsFlag = (infoflags & 0x8) != 0;
/* 139 */     boolean framebitsFlag = (infoflags & 0x10) != 0;
/* 140 */     boolean allbitsFlag = (infoflags & 0x20) != 0;
/* 141 */     if ((errorFlag) || (abortFlag))
/*     */     {
/* 143 */       setImage(GuiUtils.brokenIcon());
/* 144 */       this.m_isDone = true;
/* 145 */       this.m_isError = true;
/* 146 */       return false;
/*     */     }
/* 148 */     if ((widthFlag) || (heightFlag) || (framebitsFlag) || (allbitsFlag))
/*     */     {
/* 150 */       changeSize(w, h);
/*     */     }
/* 152 */     if ((framebitsFlag) || (allbitsFlag))
/*     */     {
/* 154 */       repaint();
/* 155 */       if (allbitsFlag)
/*     */       {
/* 157 */         this.m_isDone = true;
/*     */       }
/* 159 */       return false;
/*     */     }
/* 161 */     if (somebitsFlag)
/*     */     {
/* 163 */       repaint(100L);
/*     */     }
/* 165 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 170 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.ImageLabel
 * JD-Core Version:    0.5.4
 */