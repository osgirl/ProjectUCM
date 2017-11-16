/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserUtils;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.gui.SecurityEditHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditUserDlg
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_ctx;
/*  56 */   protected ComponentValidator m_cmpValidator = null;
/*  57 */   protected SecurityEditHelper m_editHelper = null;
/*     */   protected String m_action;
/*     */   protected String m_helpPage;
/*     */   protected UserData m_originalUserData;
/*  64 */   protected TabPanel m_tabPanel = null;
/*     */ 
/*  66 */   protected final String[][] PANEL_INFOS = { { "EditUserInfoPanel", "intradoc.apps.useradmin.EditUserInfoPanel", "apLabelInfo", "" }, { "EditUserRolePanel", "intradoc.apps.useradmin.EditUserRolePanel", "apLabelRoles", "" }, { "EditUserAccountPanel", "intradoc.apps.useradmin.EditUserAccountPanel", "apLabelAccountsTab", "UseAccounts,UseCollaboration" } };
/*     */ 
/*     */   public EditUserDlg(SystemInterface sys, String title, ResultSet rset, String helpPage)
/*     */   {
/*  75 */     this.m_helper = new DialogHelper(sys, title, true, true);
/*  76 */     this.m_systemInterface = sys;
/*  77 */     this.m_ctx = sys.getExecutionContext();
/*  78 */     this.m_cmpValidator = new ComponentValidator(rset);
/*     */ 
/*  80 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init(UserData data, boolean isEdit)
/*     */     throws ServiceException
/*     */   {
/*  94 */     this.m_originalUserData = data;
/*  95 */     Properties props = this.m_originalUserData.getProperties();
/*  96 */     this.m_helper.m_props = ((Properties)props.clone());
/*  97 */     this.m_originalUserData.setProperties(this.m_helper.m_props);
/*     */ 
/*  99 */     if (isEdit)
/*     */     {
/* 101 */       this.m_action = "EDIT_USER";
/*     */     }
/*     */     else
/*     */     {
/* 105 */       this.m_action = "ADD_USER";
/* 106 */       this.m_helper.m_props.put("dUserSourceFlags", "0");
/*     */     }
/*     */ 
/* 109 */     this.m_editHelper = new SecurityEditHelper(this.m_helper, this.m_systemInterface);
/* 110 */     this.m_editHelper.m_userData = UserUtils.createUserData();
/* 111 */     this.m_editHelper.m_userData.copyAttributes(data);
/*     */ 
/* 113 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 118 */         return EditUserDlg.this.onOK();
/*     */       }
/*     */     };
/* 121 */     okCallback.m_dlgHelper = this.m_helper;
/*     */ 
/* 123 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 125 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 126 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 127 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*     */ 
/* 130 */     String authType = data.getProperty("dUserAuthType");
/* 131 */     boolean isExternal = authType.equals("EXTERNAL");
/*     */ 
/* 133 */     this.m_tabPanel = new TabPanel();
/* 134 */     int numPanels = this.PANEL_INFOS.length;
/* 135 */     if (isExternal)
/*     */     {
/* 137 */       numPanels = 1;
/*     */     }
/*     */ 
/* 140 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 142 */       String flagStr = this.PANEL_INFOS[i][3];
/* 143 */       if (flagStr.length() != 0)
/*     */       {
/* 146 */         Vector flags = StringUtils.parseArray(flagStr, ',', '^');
/* 147 */         int size = flags.size();
/* 148 */         boolean isEnabled = false;
/* 149 */         for (int j = 0; j < size; ++j)
/*     */         {
/* 151 */           String flag = (String)flags.elementAt(j);
/* 152 */           isEnabled = SharedObjects.getEnvValueAsBoolean(flag, false);
/* 153 */           if (isEnabled) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 158 */         if (!isEnabled)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 165 */       EditUserBasePanel editPanel = (EditUserBasePanel)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_ctx, this.PANEL_INFOS[i][0]));
/*     */ 
/* 170 */       editPanel.setHelperInfo(this.m_systemInterface, this.m_editHelper, this.m_cmpValidator);
/* 171 */       editPanel.init(this.m_originalUserData, isEdit);
/*     */ 
/* 173 */       this.m_tabPanel.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], this.m_ctx), editPanel, editPanel, false);
/*     */     }
/*     */ 
/* 176 */     this.m_helper.addComponent(mainPanel, this.m_tabPanel);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 181 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public boolean onOK()
/*     */   {
/* 186 */     if (!this.m_tabPanel.validateAllPanes())
/*     */     {
/* 188 */       return false;
/*     */     }
/*     */ 
/* 192 */     DataBinder binder = new DataBinder(true);
/* 193 */     binder.setLocalData(this.m_helper.m_props);
/*     */ 
/* 195 */     String user = binder.getLocal("dName");
/*     */ 
/* 199 */     binder.putLocal("dRole", "guest");
/* 200 */     this.m_originalUserData.m_name = user;
/* 201 */     this.m_originalUserData.copyAttributesReference(this.m_editHelper.m_userData);
/* 202 */     String defAccount = binder.getLocal("defaultAccount");
/* 203 */     this.m_originalUserData.setDefaultAccount(defAccount);
/*     */     try
/*     */     {
/* 207 */       UserUtils.serializeAttribInfo(binder, this.m_originalUserData, true, false);
/*     */ 
/* 210 */       AppLauncher.executeService(this.m_action, binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 214 */       MessageBox.reportError(this.m_systemInterface, e);
/* 215 */       return false;
/*     */     }
/* 217 */     return true;
/*     */   }
/*     */ 
/*     */   public Properties getProps()
/*     */   {
/* 222 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   protected void reportError(IdcMessage msg)
/*     */   {
/* 227 */     MessageBox.reportError(this.m_systemInterface, msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 232 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87442 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditUserDlg
 * JD-Core Version:    0.5.4
 */