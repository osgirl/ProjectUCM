/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.useradmin.EditKnownAccountsDlg;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IRMEditKnownAccountsDlg extends EditKnownAccountsDlg
/*     */ {
/*     */   protected JButton m_irmProtectionBtn;
/*     */ 
/*     */   public IRMEditKnownAccountsDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  54 */     super(sys, title, helpPage);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/*  64 */     addIRMProtectionBtn(this.m_editButtonsPanel);
/*  65 */     return super.prompt();
/*     */   }
/*     */ 
/*     */   public void addIRMProtectionBtn(JPanel editButtonsPanel)
/*     */   {
/*  75 */     this.m_irmProtectionBtn = new JButton(LocaleResources.getString("apLabelIRMProtection", this.m_ctx));
/*     */ 
/*  77 */     editButtonsPanel.add(this.m_irmProtectionBtn);
/*     */ 
/*  79 */     ActionListener irmProtectionListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/*  83 */         IRMAccountProtectionDlg irmAccountProtDlg = new IRMAccountProtectionDlg(IRMEditKnownAccountsDlg.this.m_systemInterface, LocaleResources.getString("apLabelIRMProtection", IRMEditKnownAccountsDlg.this.m_ctx), IRMEditKnownAccountsDlg.this.m_accountsList.getSelectedObj());
/*     */ 
/*  87 */         irmAccountProtDlg.init();
/*     */       }
/*     */     };
/*  91 */     this.m_irmProtectionBtn.addActionListener(irmProtectionListener);
/*  92 */     this.m_accountsList.addControlComponent(this.m_irmProtectionBtn);
/*     */   }
/*     */ 
/*     */   protected void handleEditAccount(ActionEvent evt)
/*     */   {
/* 104 */     String selItem = this.m_accountsList.getSelectedObj();
/*     */ 
/* 106 */     super.handleEditAccount(evt);
/*     */ 
/* 108 */     String cmd = evt.getActionCommand();
/* 109 */     if (!cmd.equals("delete"))
/*     */       return;
/* 111 */     removeAccountProtection(selItem);
/*     */   }
/*     */ 
/*     */   public void removeAccountProtection(String accountName)
/*     */   {
/* 124 */     DataBinder binder = new DataBinder();
/* 125 */     Properties localData = binder.getLocalData();
/* 126 */     localData.put("isAccount", Boolean.TRUE.toString());
/* 127 */     localData.put("EnableIRM", Boolean.FALSE.toString());
/* 128 */     localData.put("dDocAccount", accountName);
/*     */     try
/*     */     {
/* 132 */       AppLauncher.executeService("IRM_SET_PROTECTION", binder);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 137 */       MessageBox.reportError(this.m_systemInterface, exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 149 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92590 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMEditKnownAccountsDlg
 * JD-Core Version:    0.5.4
 */