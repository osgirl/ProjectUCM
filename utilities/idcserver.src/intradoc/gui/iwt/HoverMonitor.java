/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.gui.iwt.event.IwtEvent;
/*     */ import intradoc.gui.iwt.event.IwtListener;
/*     */ import java.awt.Point;
/*     */ import java.awt.event.MouseEvent;
/*     */ 
/*     */ public class HoverMonitor
/*     */   implements Runnable
/*     */ {
/*  33 */   public static long m_lastMotionTime = 0L;
/*  34 */   public static Point m_lastMotionPoint = null;
/*  35 */   public static IwtListener m_lastHoverListener = null;
/*  36 */   public static IwtListener m_notifyOnMotion = null;
/*  37 */   public static Thread m_bgThread = null;
/*  38 */   public static HoverMonitor m_bgHoverMonitor = null;
/*  39 */   public static boolean m_ignoreNext = false;
/*  40 */   public static final boolean[] m_lockObject = new boolean[0];
/*  41 */   public static boolean m_isMoved = false;
/*     */ 
/*  43 */   public static int m_hoverDelay = 500;
/*     */   public boolean m_bgExit;
/*     */ 
/*     */   public HoverMonitor()
/*     */   {
/*  45 */     this.m_bgExit = false;
/*     */   }
/*     */ 
/*     */   public static void mouseEvent(IwtListener listener, MouseEvent event)
/*     */   {
/*  55 */     int id = event.getID();
/*  56 */     if (event.isPopupTrigger())
/*     */     {
/*  58 */       id = 505;
/*     */     }
/*     */ 
/*  61 */     switch (id)
/*     */     {
/*     */     case 504:
/*  64 */       m_ignoreNext = true;
/*  65 */       break;
/*     */     case 506:
/*  67 */       m_lastMotionTime = event.getWhen();
/*  68 */       m_lastMotionPoint = event.getPoint();
/*  69 */       m_lastHoverListener = listener;
/*  70 */       synchronized (m_lockObject)
/*     */       {
/*  72 */         if (m_bgThread != null)
/*     */         {
/*  74 */           m_bgHoverMonitor.m_bgExit = true;
/*  75 */           m_bgThread = null;
/*  76 */           m_bgHoverMonitor = null;
/*  77 */           m_isMoved = true;
/*  78 */           m_lockObject.notify();
/*     */         }
/*     */       }
/*  81 */       break;
/*     */     case 503:
/*  83 */       if (m_ignoreNext == true)
/*     */       {
/*  85 */         m_ignoreNext = false;
/*  86 */         return;
/*     */       }
/*  88 */       synchronized (m_lockObject)
/*     */       {
/*  90 */         if (m_notifyOnMotion != null)
/*     */         {
/*  92 */           IwtEvent newEvent = new IwtEvent(m_notifyOnMotion, null, 0, 2001);
/*     */ 
/*  94 */           newEvent.m_point = m_lastMotionPoint;
/*  95 */           m_notifyOnMotion.iwtEvent(newEvent);
/*  96 */           m_notifyOnMotion = null;
/*     */         }
/*  98 */         m_lastMotionTime = event.getWhen();
/*  99 */         m_lastMotionPoint = event.getPoint();
/* 100 */         m_lastHoverListener = listener;
/*     */ 
/* 102 */         if ((m_bgThread == null) || (!m_bgThread.isAlive()))
/*     */         {
/* 104 */           m_bgHoverMonitor = new HoverMonitor();
/* 105 */           m_bgThread = new Thread(m_bgHoverMonitor, "hover");
/* 106 */           m_bgThread.setDaemon(true);
/* 107 */           m_bgThread.start();
/*     */         }
/*     */         else
/*     */         {
/* 111 */           m_isMoved = true;
/* 112 */           m_lockObject.notify();
/*     */         }
/*     */       }
/* 115 */       break;
/*     */     case 505:
/* 117 */       synchronized (m_lockObject)
/*     */       {
/* 119 */         if (m_bgThread != null)
/*     */         {
/* 121 */           m_bgHoverMonitor.m_bgExit = true;
/* 122 */           m_bgThread = null;
/* 123 */           m_bgHoverMonitor = null;
/* 124 */           m_isMoved = true;
/* 125 */           m_lockObject.notify();
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     while (true)
/*     */     {
/*     */       try
/*     */       {
/* 138 */         synchronized (m_lockObject)
/*     */         {
/* 140 */           m_lockObject.wait(m_hoverDelay);
/*     */         }
/*     */       }
/*     */       catch (InterruptedException e)
/*     */       {
/* 145 */         Report.trace(null, null, e);
/*     */       }
/* 147 */       if (this.m_bgExit) {
/*     */         return;
/*     */       }
/*     */ 
/* 151 */       if (!m_isMoved)
/*     */         break;
/* 153 */       m_isMoved = false;
/*     */     }
/*     */ 
/* 158 */     synchronized (m_lockObject)
/*     */     {
/* 160 */       if (m_lastHoverListener != null)
/*     */       {
/* 162 */         IwtEvent event = new IwtEvent(m_lastHoverListener, this, 0, 2000);
/*     */ 
/* 164 */         event.m_point = m_lastMotionPoint;
/* 165 */         m_lastHoverListener.iwtEvent(event);
/* 166 */         m_notifyOnMotion = m_lastHoverListener;
/* 167 */         m_lastHoverListener = null;
/*     */       }
/* 169 */       m_bgThread = null;
/* 170 */       m_bgHoverMonitor = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 178 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.HoverMonitor
 * JD-Core Version:    0.5.4
 */