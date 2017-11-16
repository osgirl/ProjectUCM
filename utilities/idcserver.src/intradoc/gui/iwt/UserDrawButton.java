/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.Rectangle;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UserDrawButton extends PanePanel
/*     */   implements Runnable
/*     */ {
/*  43 */   protected boolean m_isDown = false;
/*  44 */   protected Vector m_actionListeners = new IdcVector();
/*  45 */   protected String m_command = null;
/*  46 */   protected boolean m_isRepeatable = false;
/*  47 */   protected Thread m_bgThread = null;
/*     */   protected Dimension m_minSize;
/*     */ 
/*     */   public UserDrawButton()
/*     */   {
/*  54 */     enableEvents(16L);
/*  55 */     this.m_minSize = new Dimension(15, 15);
/*     */   }
/*     */ 
/*     */   public UserDrawButton(String command)
/*     */   {
/*  61 */     enableEvents(16L);
/*  62 */     this.m_command = command;
/*     */ 
/*  64 */     this.m_minSize = new Dimension(15, 15);
/*     */   }
/*     */ 
/*     */   public UserDrawButton(String command, Dimension minSize)
/*     */   {
/*  70 */     enableEvents(16L);
/*  71 */     this.m_command = command;
/*     */ 
/*  73 */     this.m_minSize = minSize;
/*     */   }
/*     */ 
/*     */   public boolean setRepeatable(boolean r)
/*     */   {
/*  78 */     boolean rc = this.m_isRepeatable;
/*  79 */     this.m_isRepeatable = r;
/*  80 */     return rc;
/*     */   }
/*     */ 
/*     */   public void paint(Graphics g)
/*     */   {
/*  86 */     Dimension d = getSize();
/*  87 */     Rectangle bounds = new Rectangle(0, 0, d.width, d.height);
/*  88 */     DrawUtils.paintButton(g, bounds, this.m_isDown);
/*     */   }
/*     */ 
/*     */   public void addActionListener(ActionListener l)
/*     */   {
/*  93 */     this.m_actionListeners.addElement(l);
/*     */   }
/*     */ 
/*     */   public Dimension getMinimumSize()
/*     */   {
/*  99 */     return this.m_minSize;
/*     */   }
/*     */ 
/*     */   public Dimension getPreferredSize()
/*     */   {
/* 105 */     return this.m_minSize;
/*     */   }
/*     */ 
/*     */   public void processMouseEvent(MouseEvent event)
/*     */   {
/* 111 */     super.processMouseEvent(event);
/* 112 */     boolean repaint = false;
/* 113 */     switch (event.getID())
/*     */     {
/*     */     case 501:
/* 116 */       if (!isEnabled())
/*     */       {
/* 118 */         return;
/*     */       }
/* 120 */       this.m_isDown = true;
/* 121 */       repaint = true;
/* 122 */       if (this.m_isRepeatable)
/*     */       {
/* 124 */         this.m_bgThread = new Thread(this);
/* 125 */         this.m_bgThread.setDaemon(true);
/* 126 */         this.m_bgThread.start(); } break;
/*     */     case 502:
/* 130 */       this.m_isDown = false;
/* 131 */       repaint = true;
/* 132 */       if (this.m_bgThread != null)
/*     */       {
/* 134 */         this.m_bgThread.interrupt();
/* 135 */         this.m_bgThread = null;
/*     */       }
/*     */       else
/*     */       {
/* 139 */         createActionEvent();
/*     */       }
/*     */     }
/*     */ 
/* 143 */     if (!repaint)
/*     */       return;
/* 145 */     repaint();
/*     */   }
/*     */ 
/*     */   protected void createActionEvent()
/*     */   {
/* 152 */     int i = this.m_actionListeners.size();
/* 153 */     ActionEvent actionEvent = new ActionEvent(this, 1001, this.m_command, 0);
/*     */ 
/* 155 */     while (i-- > 0)
/*     */     {
/* 157 */       ActionListener l = (ActionListener)this.m_actionListeners.elementAt(i);
/* 158 */       l.actionPerformed(actionEvent);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 164 */     createActionEvent();
/* 165 */     SystemUtils.sleep(1000L);
/* 166 */     while ((this.m_isDown) && (Thread.currentThread() == this.m_bgThread))
/*     */     {
/* 168 */       createActionEvent();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 174 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.UserDrawButton
 * JD-Core Version:    0.5.4
 */