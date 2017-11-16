/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.gui.iwt.event.IwtEvent;
/*     */ import intradoc.gui.iwt.event.IwtListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ 
/*     */ public class DoubleBufferMonitor
/*     */   implements Runnable
/*     */ {
/*  32 */   public static long m_updateDelay = 100L;
/*  33 */   public static long m_updateTimeout = 15000L;
/*     */ 
/*  35 */   protected static Hashtable m_updateList = new Hashtable();
/*  36 */   protected static Thread m_bgThread = null;
/*  37 */   protected static boolean[] m_lockObject = new boolean[0];
/*     */ 
/*     */   public static void requestPaint(IwtListener listener)
/*     */   {
/*  46 */     Long now = new Long(System.currentTimeMillis());
/*  47 */     synchronized (m_lockObject)
/*     */     {
/*  49 */       m_updateList.put(listener, now);
/*     */ 
/*  51 */       if (m_bgThread == null)
/*     */       {
/*  53 */         m_bgThread = new Thread(new DoubleBufferMonitor(), "Double Buffer Thread");
/*     */ 
/*  55 */         m_bgThread.setDaemon(true);
/*  56 */         m_bgThread.start();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void sync()
/*     */   {
/*  66 */     m_bgThread.interrupt();
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*     */     try
/*     */     {
/*  73 */       long now = System.currentTimeMillis();
/*  74 */       long lastUpdate = now;
/*     */       while (true)
/*     */       {
/*  77 */         SystemUtils.sleep(m_updateDelay);
/*     */         Enumeration list;
/*  79 */         synchronized (m_lockObject)
/*     */         {
/*  81 */           if ((m_updateList.size() == 0) && (now - lastUpdate > m_updateTimeout))
/*     */           {
/*  84 */             m_bgThread = null;
/*  85 */             break label153:
/*     */           }
/*  87 */           list = m_updateList.keys();
/*  88 */           m_updateList = new Hashtable();
/*     */         }
/*     */ 
/*  91 */         while (list.hasMoreElements())
/*     */         {
/*  94 */           IwtListener object = (IwtListener)list.nextElement();
/*  95 */           IwtEvent event = new IwtEvent(object, null, 1, 2002);
/*     */           try
/*     */           {
/*  99 */             object.iwtEvent(event);
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 103 */             Report.trace("applet", "DoubleBufferMonitor:", e);
/* 104 */             label153: requestPaint(object);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 111 */       m_bgThread = null;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 117 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.DoubleBufferMonitor
 * JD-Core Version:    0.5.4
 */