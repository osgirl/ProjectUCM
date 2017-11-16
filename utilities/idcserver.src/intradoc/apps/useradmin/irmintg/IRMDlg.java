/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public abstract class IRMDlg extends JDialog
/*     */ {
/*     */   protected JButton m_btnCancel;
/*     */   protected JButton m_btnOk;
/*     */   protected JPanel m_componentsPanel;
/*     */   protected GridLayout componentsPanelLayout;
/*     */   protected JPanel mainPanel;
/*     */   protected SystemInterface m_system;
/*     */   protected ExecutionContext m_ctx;
/*     */   protected DialogHelper m_helper;
/*     */ 
/*     */   public IRMDlg(SystemInterface sys, String title)
/*     */   {
/*  87 */     super(sys.getMainWindow(), title, true);
/*     */ 
/*  89 */     this.m_system = sys;
/*  90 */     this.m_ctx = sys.getExecutionContext();
/*  91 */     this.m_helper = new DialogHelper();
/*  92 */     this.m_helper.attachToDialog(this, sys, null);
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 102 */     this.m_componentsPanel = new PanePanel();
/* 103 */     setDefaultCloseOperation(2);
/*     */ 
/* 105 */     ActionListener okListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent evt)
/*     */       {
/* 109 */         IRMDlg.this.okHandler(evt);
/*     */       }
/*     */     };
/* 113 */     ActionListener cancelListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent evt)
/*     */       {
/* 117 */         IRMDlg.this.cancelHandler(evt);
/*     */       }
/*     */     };
/* 121 */     this.m_btnOk = this.m_helper.addCommandButton(LocaleResources.getString("apButtonOk", this.m_ctx), okListener);
/*     */ 
/* 123 */     this.m_btnCancel = this.m_helper.addCommandButton(LocaleResources.getString("apButtonCancel", this.m_ctx), cancelListener);
/*     */   }
/*     */ 
/*     */   protected void packComponents()
/*     */   {
/* 132 */     this.mainPanel = this.m_helper.m_mainPanel;
/* 133 */     GridLayout layout = new GridLayout(1, 0, 10, 20);
/* 134 */     this.mainPanel.setLayout(layout);
/* 135 */     this.mainPanel.add(this.m_componentsPanel);
/* 136 */     pack();
/*     */   }
/*     */ 
/*     */   protected void okHandler(ActionEvent evt)
/*     */   {
/* 146 */     dispose();
/*     */   }
/*     */ 
/*     */   protected void cancelHandler(ActionEvent evt)
/*     */   {
/* 156 */     dispose();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 167 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92579 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMDlg
 * JD-Core Version:    0.5.4
 */