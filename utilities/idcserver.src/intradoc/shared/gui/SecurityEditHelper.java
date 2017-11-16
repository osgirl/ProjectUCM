/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SecurityEditHelper
/*     */ {
/*     */   public UserData m_userData;
/*     */   public ContainerHelper m_helper;
/*     */   public SystemInterface m_systemInterface;
/*     */   public ExecutionContext m_cxt;
/*     */   public ComboChoice m_accountChoices;
/*     */ 
/*     */   public SecurityEditHelper(ContainerHelper helper, SystemInterface systemInterface)
/*     */   {
/*  67 */     this.m_userData = null;
/*  68 */     this.m_helper = helper;
/*  69 */     this.m_systemInterface = systemInterface;
/*  70 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*  71 */     this.m_accountChoices = null;
/*     */   }
/*     */ 
/*     */   public void addSecurityGroupEditField(JPanel securityPanel, String label, String groupid)
/*     */     throws ServiceException
/*     */   {
/*  78 */     Vector allowableGroups = SecurityUtils.getUserGroupsWithPrivilege(this.m_userData, 8);
/*  79 */     if ((allowableGroups == null) || (allowableGroups.size() == 0))
/*     */     {
/*  81 */       String userName = ((this.m_userData != null) && (this.m_userData.m_name != null)) ? this.m_userData.m_name : LocaleResources.getString("apIDUnknown", this.m_cxt);
/*     */ 
/*  83 */       String msg = LocaleUtils.encodeMessage("apUserNoAdminAccess", null, userName);
/*     */ 
/*  85 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/*  88 */     JComboBox groupsFld = new CustomChoice();
/*  89 */     int ngroups = allowableGroups.size();
/*  90 */     for (int i = 0; i < ngroups; ++i)
/*     */     {
/*  92 */       groupsFld.addItem(allowableGroups.elementAt(i));
/*     */     }
/*  94 */     this.m_helper.addLabelFieldPairEx(securityPanel, label, groupsFld, groupid, false);
/*     */ 
/*  96 */     this.m_helper.addLastComponentInRow(securityPanel, new PanePanel());
/*     */   }
/*     */ 
/*     */   public void addAccountEditField(JPanel securityPanel, String label, String accountid)
/*     */   {
/* 102 */     this.m_accountChoices = new ComboChoice();
/* 103 */     this.m_helper.addLabelFieldPairEx(securityPanel, label, this.m_accountChoices, accountid, false);
/*     */ 
/* 105 */     this.m_helper.addLastComponentInRow(securityPanel, new CustomLabel(""));
/*     */   }
/*     */ 
/*     */   public void refreshAccountChoiceList(boolean addSpecialAccounts, int priv)
/*     */   {
/* 111 */     Vector knownAllowableAccounts = null;
/*     */     try
/*     */     {
/* 114 */       knownAllowableAccounts = SecurityUtils.getAccessibleAccounts(this.m_userData, addSpecialAccounts, priv, this.m_cxt);
/*     */ 
/* 117 */       if ((addSpecialAccounts) && (SharedObjects.getEnvValueAsBoolean("UseCollaboration", false)) && 
/* 121 */         (SecurityUtils.isAccountAccessible(this.m_userData, "prj", priv)) && 
/* 123 */         (!knownAllowableAccounts.contains("prj")))
/*     */       {
/* 125 */         knownAllowableAccounts.addElement("prj");
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 132 */       reportError(IdcMessageFactory.lc(e, "apUnableToLoadAccounts", new Object[0]));
/*     */     }
/*     */ 
/* 136 */     if (knownAllowableAccounts == null)
/*     */     {
/* 138 */       knownAllowableAccounts = new IdcVector();
/*     */     }
/* 140 */     knownAllowableAccounts.insertElementAt("", 0);
/* 141 */     this.m_accountChoices.initChoiceList(knownAllowableAccounts);
/*     */   }
/*     */ 
/*     */   public void setAccountEditValue(String accountid, String val)
/*     */   {
/* 146 */     this.m_helper.m_props.put(accountid, val);
/* 147 */     this.m_helper.m_exchange.setComponentValue(accountid, val);
/*     */   }
/*     */ 
/*     */   protected void reportError(IdcMessage msg)
/*     */   {
/* 152 */     MessageBox.reportError(this.m_systemInterface, msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 157 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83557 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.SecurityEditHelper
 * JD-Core Version:    0.5.4
 */