/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.UserData;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JPasswordField;
/*     */ 
/*     */ public class EditUserAuthType
/*     */ {
/*     */   protected ExecutionContext m_ctx;
/*     */   protected DialogHelper m_helper;
/*     */   protected String m_helpPage;
/*     */   protected UserData m_userData;
/*     */   protected String m_curAuthType;
/*     */   protected JCheckBox m_setPasswordEnabled;
/*     */ 
/*     */   public EditUserAuthType(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  60 */     this.m_helper = new DialogHelper(sys, title, true);
/*  61 */     this.m_ctx = sys.getExecutionContext();
/*  62 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init(UserData userData, boolean isEdit)
/*     */   {
/*  67 */     this.m_userData = userData;
/*  68 */     if (isEdit)
/*     */     {
/*  71 */       this.m_helper.m_props = ((Properties)userData.getProperties().clone());
/*     */     }
/*     */     else
/*     */     {
/*  77 */       this.m_helper.m_props = userData.getProperties();
/*     */     }
/*  79 */     initUI(isEdit);
/*     */   }
/*     */ 
/*     */   protected void initUI(boolean isEdit)
/*     */   {
/*  84 */     DialogCallback dCbck = null;
/*  85 */     ActionListener checkBoxAction = null;
/*  86 */     JPasswordField passwordBox1 = new JPasswordField(20);
/*  87 */     JPasswordField passwordBox2 = new JPasswordField(20);
/*  88 */     if (isEdit)
/*     */     {
/*  90 */       dCbck = new DialogCallback()
/*     */       {
/*     */         public boolean handleDialogEvent(ActionEvent e)
/*     */         {
/*  95 */           return EditUserAuthType.this.onOk();
/*     */         }
/*     */       };
/*  99 */       checkBoxAction = new ActionListener(passwordBox1, passwordBox2)
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 103 */           EditUserAuthType.this.m_helper.setEnabled(this.val$passwordBox1, EditUserAuthType.this.m_setPasswordEnabled.isSelected());
/* 104 */           EditUserAuthType.this.m_helper.setEnabled(this.val$passwordBox2, EditUserAuthType.this.m_setPasswordEnabled.isSelected());
/*     */         }
/*     */       };
/*     */     }
/*     */ 
/* 109 */     JPanel mainPanel = this.m_helper.initStandard(null, dCbck, 1, false, this.m_helpPage);
/* 110 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*     */ 
/* 113 */     DisplayChoice authChoices = new DisplayChoice();
/* 114 */     this.m_curAuthType = this.m_userData.getProperty("dUserAuthType");
/* 115 */     if (isEdit)
/*     */     {
/* 117 */       String[][] displayMap = TableFields.USER_AUTH_TYPES;
/*     */ 
/* 121 */       String displayAuthType = null;
/* 122 */       boolean hasGlobalUsers = SharedObjects.getEnvValueAsBoolean("HasGlobalUsers", true);
/* 123 */       int len = displayMap.length - 1;
/* 124 */       if ((!hasGlobalUsers) && (!this.m_curAuthType.equalsIgnoreCase("global")))
/*     */       {
/* 126 */         len = displayMap.length - 2;
/*     */       }
/*     */ 
/* 129 */       String[][] truncMap = new String[len][2];
/* 130 */       boolean isFound = false;
/* 131 */       boolean isGlobalFound = false;
/* 132 */       for (int i = 0; i < displayMap.length; ++i)
/*     */       {
/* 134 */         String authType = displayMap[i][0];
/* 135 */         if (authType.equalsIgnoreCase(this.m_curAuthType))
/*     */         {
/* 137 */           displayAuthType = displayMap[i][1];
/* 138 */           isFound = true;
/*     */         }
/* 142 */         else if ((!hasGlobalUsers) && (authType.equalsIgnoreCase("GLOBAL")))
/*     */         {
/* 144 */           isGlobalFound = true;
/*     */         }
/*     */         else
/*     */         {
/* 148 */           int index = i;
/* 149 */           if (isFound)
/*     */           {
/* 151 */             --index;
/*     */           }
/* 153 */           if (isGlobalFound)
/*     */           {
/* 155 */             --index;
/*     */           }
/* 157 */           truncMap[index] = displayMap[i];
/*     */         }
/*     */       }
/*     */ 
/* 161 */       authChoices.init(truncMap);
/* 162 */       this.m_helper.m_props.put("curUserAuthType", this.m_curAuthType);
/*     */ 
/* 165 */       String name = this.m_userData.getProperty("dName");
/* 166 */       this.m_helper.addComponent(mainPanel, new CustomLabel(LocaleResources.getString("apLabelChangeUserTo", this.m_ctx, displayAuthType, name), 1));
/*     */     }
/*     */     else
/*     */     {
/* 172 */       authChoices.init(TableFields.NEW_USER_AUTH_TYPES);
/*     */     }
/*     */ 
/* 175 */     authChoices.setMinWidth(150);
/* 176 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelAuthorizationType", this.m_ctx), authChoices, "dUserAuthType");
/*     */ 
/* 179 */     if ((!isEdit) || (!this.m_curAuthType.equals("EXTERNAL")))
/*     */       return;
/* 181 */     this.m_setPasswordEnabled = new JCheckBox(LocaleResources.getString("apLabelSetPassword", null));
/*     */ 
/* 183 */     this.m_setPasswordEnabled.addActionListener(checkBoxAction);
/* 184 */     this.m_helper.addLabelFieldPair(mainPanel, "", this.m_setPasswordEnabled, "setPassword");
/*     */ 
/* 186 */     passwordBox1.setEchoChar('*');
/* 187 */     passwordBox2.setEchoChar('*');
/*     */ 
/* 189 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelPassword", this.m_ctx), passwordBox1, "password1");
/*     */ 
/* 191 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelConfirmPassword", this.m_ctx), passwordBox2, "password2");
/*     */ 
/* 194 */     checkBoxAction.actionPerformed(null);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 200 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected boolean onOk()
/*     */   {
/*     */     try
/*     */     {
/* 207 */       if ((this.m_curAuthType.equals("EXTERNAL")) && (this.m_setPasswordEnabled.isSelected()))
/*     */       {
/* 209 */         String password1 = this.m_helper.m_props.getProperty("password1");
/* 210 */         String password2 = this.m_helper.m_props.getProperty("password2");
/* 211 */         if (password1.equals(password2))
/*     */         {
/* 213 */           Validation.validatePassword(password1);
/* 214 */           this.m_helper.m_props.put("dPassword", password1);
/* 215 */           this.m_helper.m_props.put("dPasswordEncoding", "");
/*     */         }
/*     */         else
/*     */         {
/* 219 */           AppLauncher.reportOperationError(this.m_helper.m_exchange.m_sysInterface, null, IdcMessageFactory.lc("apMsgPasswordMismatch", new Object[0]));
/*     */ 
/* 221 */           return false;
/*     */         }
/*     */       }
/* 224 */       this.m_helper.m_props.remove("password1");
/* 225 */       this.m_helper.m_props.remove("password2");
/* 226 */       this.m_helper.m_props.remove("setPassword");
/* 227 */       AppLauncher.executeService("CHANGE_USER_AUTH_TYPE", this.m_helper.m_props);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 231 */       AppLauncher.reportOperationError(this.m_helper.m_exchange.m_sysInterface, e, null);
/* 232 */       return false;
/*     */     }
/* 234 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 239 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80531 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditUserAuthType
 * JD-Core Version:    0.5.4
 */