/*    */ package intradoc.gui;
/*    */ 
/*    */ import intradoc.common.SystemInterface;
/*    */ import java.awt.Window;
/*    */ import java.awt.event.WindowAdapter;
/*    */ import java.awt.event.WindowEvent;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class WindowHelper extends ContainerHelper
/*    */ {
/*    */   public Window m_window;
/*    */   public boolean m_exitOnClose;
/*    */   public PromptHandler m_isCloseAllowedCallback;
/*    */ 
/*    */   public WindowHelper()
/*    */   {
/* 46 */     this.m_window = null;
/* 47 */     this.m_exitOnClose = false;
/* 48 */     this.m_mainPanel = null;
/* 49 */     this.m_isCloseAllowedCallback = null;
/*    */   }
/*    */ 
/*    */   public void attachToWindow(Window window, SystemInterface sys, Properties props)
/*    */   {
/* 54 */     attachToContainer(window, sys, props);
/* 55 */     this.m_window = window;
/*    */ 
/* 58 */     WindowAdapter winAdapt = new WindowAdapter()
/*    */     {
/*    */       public void windowClosing(WindowEvent e)
/*    */       {
/* 63 */         WindowHelper.this.doCloseEvent();
/*    */       }
/*    */ 
/*    */       public void windowClosed(WindowEvent e)
/*    */       {
/* 69 */         if (!WindowHelper.this.m_exitOnClose)
/*    */           return;
/* 71 */         System.exit(0);
/*    */       }
/*    */     };
/* 76 */     this.m_window.addWindowListener(winAdapt);
/*    */   }
/*    */ 
/*    */   public void doCloseEvent()
/*    */   {
/* 81 */     if (this.m_isCloseAllowedCallback != null)
/*    */     {
/* 83 */       int retVal = this.m_isCloseAllowedCallback.prompt();
/* 84 */       if ((retVal != 1) && (retVal != 2))
/*    */       {
/* 86 */         return;
/*    */       }
/*    */     }
/*    */ 
/* 90 */     this.m_window.setVisible(false);
/* 91 */     this.m_window.dispose();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 96 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79221 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.WindowHelper
 * JD-Core Version:    0.5.4
 */