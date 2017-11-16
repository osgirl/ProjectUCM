/*     */ package intradoc.common;
/*     */ 
/*     */ import java.applet.AppletContext;
/*     */ import java.awt.Component;
/*     */ import java.awt.Cursor;
/*     */ import java.awt.Image;
/*     */ import java.awt.Toolkit;
/*     */ import java.awt.Window;
/*     */ import java.awt.image.ImageProducer;
/*     */ import java.awt.image.MemoryImageSource;
/*     */ import java.net.URL;
/*     */ import javax.swing.JFrame;
/*     */ 
/*     */ public class GuiUtils
/*     */ {
/*  54 */   private static AppletContext m_appletContext = null;
/*  55 */   private static URL m_baseURL = null;
/*  56 */   private static String m_gifRootDir = null;
/*     */ 
/* 119 */   private static int m_brokenWidth = 16;
/* 120 */   private static int m_brokenHeight = 19;
/*     */ 
/* 122 */   private static int[] m_brokenPixels = { 65280, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, 65280, 65280, 65280, 65280, 65280, 65280, 65280, -16777216, -1, -1, -1, -1, -1, -1, -1, -16777216, -16777216, 65280, 65280, 65280, 65280, 65280, 65280, -16777216, -1, -1, -1, -1, -1, -1, -1, -16777216, -1, -16777216, 65280, 65280, 65280, 65280, 65280, -16777216, -1, -1, -1, -1, -1, -1, -1, -16777216, -1, -1, -16777216, 65280, 65280, 65280, 65280, -16777216, -1, -1, -1, -1, -1, -1, -1, -16777216, -1, -1, -1, -16777216, 65280, 65280, 65280, -16777216, -1, -1, -1, -1, -1, -1, -1, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, 65280, 65280, -16777216, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -65536, -1, -1, -1, -1, -1, -65536, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -65536, -65536, -1, -1, -1, -65536, -65536, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -1, -65536, -65536, -1, -65536, -65536, -1, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -1, -1, -65536, -65536, -65536, -1, -1, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -1, -1, -65536, -65536, -65536, -1, -1, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -1, -65536, -65536, -1, -65536, -65536, -1, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -65536, -65536, -1, -1, -1, -65536, -65536, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -65536, -1, -1, -1, -1, -1, -65536, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16777216, 65280, 65280, -16777216, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16777216, 65280, 65280, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, 65280 };
/*     */ 
/* 220 */   private static ImageProducer m_brokenSource = null;
/*     */ 
/*     */   public static void packWindow(Component comp)
/*     */   {
/*  41 */     while (comp != null)
/*     */     {
/*  43 */       if (comp instanceof Window)
/*     */       {
/*  45 */         ((Window)comp).pack();
/*  46 */         return;
/*     */       }
/*  48 */       comp = comp.getParent();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void setGifAppletContext(AppletContext appletContext, URL baseURL)
/*     */   {
/*  60 */     m_appletContext = appletContext;
/*  61 */     m_baseURL = baseURL;
/*     */   }
/*     */ 
/*     */   public static void setGifDirectoryRoot(String gifDir)
/*     */   {
/*  66 */     m_gifRootDir = gifDir;
/*     */   }
/*     */ 
/*     */   public static Image getAppImage(String file)
/*     */   {
/*  71 */     Image image = null;
/*     */     try
/*     */     {
/*  74 */       if (m_appletContext != null)
/*     */       {
/*  76 */         URL url = new URL(m_baseURL, file);
/*  77 */         image = Toolkit.getDefaultToolkit().getImage(url);
/*     */       }
/*     */       else
/*     */       {
/*  81 */         String fullPath = m_gifRootDir + file;
/*  82 */         image = Toolkit.getDefaultToolkit().getImage(fullPath);
/*     */       }
/*     */ 
/*  86 */       if (image != null)
/*     */       {
/*  88 */         WaitImageLoad waitLoad = new WaitImageLoad();
/*  89 */         if (image.getHeight(waitLoad) < 0)
/*     */         {
/*  91 */           waitLoad.waitReady();
/*     */         }
/*  93 */         if (waitLoad.m_isError)
/*     */         {
/*  95 */           image = brokenIcon();
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 101 */       if (SystemUtils.m_verbose)
/*     */       {
/* 103 */         Report.debug(null, null, ignore);
/*     */       }
/*     */     }
/* 106 */     if (image == null)
/*     */     {
/* 108 */       image = brokenIcon();
/*     */     }
/* 110 */     return image;
/*     */   }
/*     */ 
/*     */   public static Image brokenIcon()
/*     */   {
/* 224 */     if (m_brokenSource == null) {
/* 225 */       m_brokenSource = new MemoryImageSource(m_brokenHeight, m_brokenWidth, m_brokenPixels, 0, m_brokenWidth);
/*     */     }
/* 227 */     return Toolkit.getDefaultToolkit().createImage(m_brokenSource);
/*     */   }
/*     */ 
/*     */   public static Cursor setBusy(SystemInterface sysInterface)
/*     */   {
/* 238 */     Cursor busyCursor = Cursor.getPredefinedCursor(3);
/* 239 */     return setCursor(sysInterface, busyCursor);
/*     */   }
/*     */ 
/*     */   public static Cursor setCursor(SystemInterface sysInterface, Cursor cursor)
/*     */   {
/* 244 */     JFrame frame = sysInterface.getMainWindow();
/* 245 */     Cursor retVal = frame.getCursor();
/* 246 */     frame.setCursor(cursor);
/* 247 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 252 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.GuiUtils
 * JD-Core Version:    0.5.4
 */