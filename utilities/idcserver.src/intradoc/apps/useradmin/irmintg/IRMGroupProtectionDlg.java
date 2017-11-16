/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.useradmin.irmintg.util.IRMUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IRMGroupProtectionDlg extends IRMProtectionDlg
/*     */ {
/*     */   protected DisplayChoice m_cmbNonControlSealedContent;
/*     */   protected DisplayChoice m_cmbUnsealType;
/*     */   protected JLabel m_lblNonControlSealedContent;
/*     */   protected JLabel m_lblUnsealType;
/*     */   protected String m_groupName;
/*     */ 
/*     */   public IRMGroupProtectionDlg(SystemInterface sys, String title, String groupName)
/*     */   {
/*  72 */     super(sys, title);
/*  73 */     this.m_groupName = groupName;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  82 */     super.init(3, 2);
/*     */ 
/*  84 */     this.m_lblUnsealType = new CustomLabel(LocaleResources.getString("apLabelUnsealableType", this.m_ctx), 1);
/*     */ 
/*  86 */     this.m_cmbUnsealType = new DisplayChoice();
/*  87 */     this.m_lblNonControlSealedContent = new CustomLabel(LocaleResources.getString("apLabelNCSealedContent", this.m_ctx), 1);
/*     */ 
/*  90 */     this.m_cmbNonControlSealedContent = new DisplayChoice();
/*     */ 
/*  93 */     String[][] contentOptions = IRMUtils.getContentOptions(this.m_ctx);
/*  94 */     this.m_cmbUnsealType.init(contentOptions);
/*  95 */     this.m_cmbNonControlSealedContent.init(contentOptions);
/*     */ 
/*  97 */     this.m_chkEnableProtection.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent evt)
/*     */       {
/* 101 */         IRMGroupProtectionDlg.this.enableProtectionLsnr(evt);
/*     */       }
/*     */     });
/* 105 */     this.m_componentsPanel.add(this.m_lblUnsealType);
/* 106 */     this.m_componentsPanel.add(this.m_cmbUnsealType);
/* 107 */     this.m_componentsPanel.add(this.m_lblNonControlSealedContent);
/* 108 */     this.m_componentsPanel.add(this.m_cmbNonControlSealedContent);
/*     */ 
/* 110 */     packComponents();
/* 111 */     pack();
/*     */ 
/* 113 */     setProtectionData();
/* 114 */     this.m_helper.show();
/*     */   }
/*     */ 
/*     */   protected void setProtectionData()
/*     */   {
/* 122 */     DataBinder binder = new DataBinder();
/* 123 */     Properties localData = binder.getLocalData();
/* 124 */     localData.put("isAccount", Boolean.FALSE.toString());
/* 125 */     localData.put("dGroupName", this.m_groupName);
/*     */ 
/* 127 */     super.getProtectionData(binder);
/*     */ 
/* 129 */     String isIRMEnabled = (String)localData.get("EnableIRM");
/*     */ 
/* 132 */     if (!StringUtils.convertToBool(isIRMEnabled, false))
/*     */     {
/* 134 */       setComponentsState(false);
/*     */ 
/* 136 */       DataBinder defaultConfigBinder = new DataBinder();
/*     */       try
/*     */       {
/* 139 */         AppLauncher.executeService("IRM_GET_CONFIG", defaultConfigBinder);
/*     */ 
/* 141 */         ResultSet rsDefaultConfig = defaultConfigBinder.getResultSet("rsConfig");
/*     */ 
/* 143 */         this.m_cmbUnsealType.setSelectedItem(rsDefaultConfig.getStringValueByName("UnsealTypes"));
/*     */ 
/* 145 */         this.m_cmbNonControlSealedContent.setSelectedItem(rsDefaultConfig.getStringValueByName("NonControlSealedContent"));
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 151 */         MessageBox.reportError(this.m_system, new IdcMessage(LocaleResources.getString("apUnableToGetDefaultConfig", this.m_ctx), new Object[0]));
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 158 */       setComponentsState(true);
/* 159 */       this.m_cmbUnsealType.setSelectedItem(localData.get("UnsealTypes"));
/*     */ 
/* 161 */       this.m_cmbNonControlSealedContent.setSelectedItem(localData.get("NonControlSealedContent"));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void setComponentsState(boolean isEnabled)
/*     */   {
/* 173 */     this.m_cmbUnsealType.setEnabled(isEnabled);
/* 174 */     this.m_cmbNonControlSealedContent.setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   protected void enableProtectionLsnr(ActionEvent evt)
/*     */   {
/* 184 */     CustomCheckbox enableProtectionCheckBox = (CustomCheckbox)evt.getSource();
/*     */ 
/* 186 */     boolean isEnabled = false;
/* 187 */     if (enableProtectionCheckBox.isSelected())
/*     */     {
/* 189 */       isEnabled = true;
/*     */     }
/*     */     else
/*     */     {
/* 193 */       isEnabled = false;
/*     */     }
/* 195 */     setComponentsState(isEnabled);
/*     */   }
/*     */ 
/*     */   protected void okHandler(ActionEvent evt)
/*     */   {
/* 204 */     if ((this.m_groupName == null) || (this.m_groupName.length() == 0))
/*     */     {
/* 206 */       MessageBox.reportError(this.m_system, new IdcMessage(LocaleResources.getString("apSelectGroupMsg", this.m_ctx), new Object[0]));
/*     */ 
/* 208 */       return;
/*     */     }
/*     */ 
/* 211 */     DataBinder binder = new DataBinder();
/* 212 */     Properties localData = binder.getLocalData();
/*     */ 
/* 214 */     localData.put("isAccount", Boolean.FALSE.toString());
/* 215 */     localData.put("dGroupName", this.m_groupName);
/* 216 */     localData.put("UnsealTypes", this.m_cmbUnsealType.getSelectedInternalValue());
/*     */ 
/* 218 */     localData.put("NonControlSealedContent", this.m_cmbNonControlSealedContent.getSelectedInternalValue());
/*     */ 
/* 221 */     super.okHandler(evt, binder);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 232 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92579 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMGroupProtectionDlg
 * JD-Core Version:    0.5.4
 */