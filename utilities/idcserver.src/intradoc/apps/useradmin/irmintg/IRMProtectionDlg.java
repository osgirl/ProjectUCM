/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public abstract class IRMProtectionDlg extends IRMDlg
/*     */ {
/*     */   protected CustomCheckbox m_chkEnableProtection;
/*     */   protected JLabel m_lblEnableProtection;
/*     */ 
/*     */   public IRMProtectionDlg(SystemInterface sys, String title)
/*     */   {
/*  56 */     super(sys, title);
/*     */   }
/*     */ 
/*     */   public void init(int gridRows, int gridCols)
/*     */   {
/*  69 */     super.init();
/*     */ 
/*  71 */     this.componentsPanelLayout = new GridLayout(gridRows, gridCols, 10, 10);
/*  72 */     this.m_componentsPanel.setLayout(this.componentsPanelLayout);
/*     */ 
/*  74 */     this.m_lblEnableProtection = new CustomLabel(LocaleResources.getString("apLabelEnableIRMProtection", this.m_ctx), 1);
/*     */ 
/*  78 */     this.m_chkEnableProtection = new CustomCheckbox();
/*     */ 
/*  80 */     this.m_componentsPanel.add(this.m_lblEnableProtection);
/*  81 */     this.m_componentsPanel.add(this.m_chkEnableProtection);
/*     */   }
/*     */ 
/*     */   protected void getProtectionData(DataBinder binder)
/*     */   {
/*     */     try
/*     */     {
/*  94 */       AppLauncher.executeService("IRM_GET_PROTECTION", binder);
/*     */ 
/*  96 */       String isIRMEnabled = binder.get("EnableIRM");
/*  97 */       this.m_chkEnableProtection.setSelected(Boolean.parseBoolean(isIRMEnabled));
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 102 */       MessageBox.reportError(this.m_system, new IdcMessage(LocaleResources.getString("apFailedGetMsg", this.m_ctx), new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void okHandler(ActionEvent evt, DataBinder binder)
/*     */   {
/* 116 */     String isIRMEnabled = Boolean.FALSE.toString();
/* 117 */     if (this.m_chkEnableProtection.isSelected())
/*     */     {
/* 119 */       isIRMEnabled = Boolean.TRUE.toString();
/*     */     }
/*     */ 
/* 122 */     binder.putLocal("EnableIRM", isIRMEnabled);
/*     */     try
/*     */     {
/* 125 */       AppLauncher.executeService("IRM_SET_PROTECTION", binder);
/*     */ 
/* 128 */       MessageBox.showMessage(new DialogHelper(this.m_system, LocaleResources.getString("apLabelIRMProtection", this.m_ctx), true), new IdcMessage(LocaleResources.getString("apSuccessUpdateMsg", this.m_ctx), new Object[0]), 1);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 135 */       MessageBox.reportError(this.m_system, new IdcMessage(LocaleResources.getString("apFailedUpdateMsg", this.m_ctx), new Object[0]));
/*     */     }
/*     */ 
/* 138 */     dispose();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 149 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92590 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMProtectionDlg
 * JD-Core Version:    0.5.4
 */