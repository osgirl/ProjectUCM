/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ 
/*     */ public class MainFrameListener
/*     */   implements ActionListener
/*     */ {
/*     */   protected MainFrame m_frame;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_appHelpKey;
/*     */ 
/*     */   public MainFrameListener()
/*     */   {
/*  36 */     this.m_frame = null;
/*  37 */     this.m_systemInterface = null;
/*  38 */     this.m_appHelpKey = "CS_Admin_Top";
/*     */   }
/*     */ 
/*     */   public void init(MainFrame frame) {
/*  42 */     this.m_frame = frame;
/*  43 */     if (this.m_frame == null)
/*     */       return;
/*  45 */     this.m_systemInterface = this.m_frame.m_appHelper;
/*     */   }
/*     */ 
/*     */   public String getAppHelpKey()
/*     */   {
/*  51 */     return this.m_appHelpKey;
/*     */   }
/*     */ 
/*     */   public void setAppHelpKey(String appHelpKey)
/*     */   {
/*  56 */     this.m_appHelpKey = appHelpKey;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/*  61 */     String action = event.getActionCommand();
/*     */ 
/*  63 */     if (action == null)
/*     */     {
/*  65 */       Report.trace("applet", "MainFrameListener: null command", null);
/*     */     }
/*  67 */     else if (this.m_frame == null)
/*     */     {
/*  69 */       Report.trace("applet", "MainFrameListener: invoked with null frame.", null);
/*     */     }
/*  71 */     else if (action.startsWith("launch-"))
/*     */     {
/*     */       try
/*     */       {
/*  75 */         String app = action.substring("launch-".length());
/*  76 */         AppLauncher.launch(app);
/*     */       }
/*     */       catch (ServiceException exp)
/*     */       {
/*  80 */         IdcMessage error = IdcMessageFactory.lc("apUnableToLaunch", new Object[0]);
/*  81 */         this.m_frame.reportError(exp, error);
/*     */       }
/*     */     }
/*  84 */     else if (action.equals("contents"))
/*     */     {
/*  86 */       String adminTopPage = DialogHelpTable.getHelpPage("CS_Admin_Top");
/*  87 */       this.m_frame.displayHelp(adminTopPage);
/*     */     }
/*  89 */     else if (action.equals("about"))
/*     */     {
/*  91 */       this.m_frame.displayAboutInfo();
/*     */     }
/*  93 */     else if (action.equals("tracing"))
/*     */     {
/*     */       try
/*     */       {
/*  97 */         this.m_frame.displayTracingConfig();
/*     */       }
/*     */       catch (ServiceException exp)
/*     */       {
/* 101 */         IdcMessage error = IdcMessageFactory.lc("apUnableToLaunch", new Object[0]);
/* 102 */         this.m_frame.reportError(exp, error);
/*     */       }
/*     */     }
/* 105 */     else if (action.equals("publishSchema"))
/*     */     {
/*     */       try
/*     */       {
/* 109 */         DataBinder binder = new DataBinder();
/* 110 */         binder.putLocal("UserPublishingRequest", "1");
/* 111 */         AppLauncher.executeService("PUBLISH_SCHEMA", binder);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 115 */         AppLauncher.reportOperationError(this.m_systemInterface, e, null);
/*     */       }
/*     */     } else {
/* 118 */       if (!action.equals("exit"))
/*     */         return;
/* 120 */       this.m_frame.m_appHelper.doCloseEvent();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 126 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.MainFrameListener
 * JD-Core Version:    0.5.4
 */