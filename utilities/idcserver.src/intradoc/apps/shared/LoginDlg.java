/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.Users;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.KeyAdapter;
/*     */ import java.awt.event.KeyEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JPasswordField;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class LoginDlg
/*     */ {
/*     */   protected UserData m_userData;
/*     */   protected DialogHelper m_helper;
/*     */   protected JTextField m_userNameField;
/*     */   protected JPasswordField m_passwordField;
/*     */   protected DialogCallback m_parentCallback;
/*  59 */   protected ExecutionContext m_cxt = new ExecutionContextAdaptor();
/*     */ 
/*     */   public LoginDlg(JFrame parent, String title)
/*     */   {
/*  63 */     JDialog dlg = new JDialog(parent, title, true);
/*  64 */     this.m_helper = new DialogHelper();
/*  65 */     this.m_helper.attachToDialog(dlg, null, new Properties());
/*  66 */     this.m_helper.setModalityType("APPLICATION_MODAL");
/*     */   }
/*     */ 
/*     */   public void init(DialogCallback parentCallback)
/*     */   {
/*  71 */     this.m_userNameField = new JTextField(20);
/*  72 */     this.m_passwordField = new JPasswordField(20);
/*  73 */     this.m_passwordField.setEchoChar('*');
/*     */ 
/*  75 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  80 */         return LoginDlg.this.onOK();
/*     */       }
/*     */     };
/*  83 */     okCallback.m_dlgHelper = this.m_helper;
/*  84 */     this.m_parentCallback = parentCallback;
/*     */ 
/*  86 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 2, false, DialogHelpTable.getHelpPage("Login"));
/*     */ 
/*  89 */     String userPrompt = LocaleResources.getString("csUserNamePrompt", this.m_cxt);
/*  90 */     String passPrompt = LocaleResources.getString("csPasswordPrompt", this.m_cxt);
/*  91 */     this.m_helper.addLabelFieldPair(mainPanel, userPrompt, this.m_userNameField, "dName");
/*  92 */     this.m_helper.addLabelFieldPair(mainPanel, passPrompt, this.m_passwordField, "dPassword");
/*     */ 
/*  95 */     KeyAdapter kAdapter = new KeyAdapter()
/*     */     {
/*     */       public void keyPressed(KeyEvent e)
/*     */       {
/* 100 */         if (e.getKeyCode() != 10)
/*     */           return;
/* 102 */         if (!LoginDlg.this.m_helper.retrieveComponentValues())
/*     */         {
/* 104 */           return;
/*     */         }
/*     */ 
/* 107 */         if (!LoginDlg.this.onOK())
/*     */         {
/* 109 */           return;
/*     */         }
/*     */ 
/* 112 */         LoginDlg.this.m_helper.m_result = 1;
/* 113 */         LoginDlg.this.m_helper.close();
/*     */       }
/*     */     };
/* 118 */     this.m_userNameField.addKeyListener(kAdapter);
/* 119 */     this.m_passwordField.addKeyListener(kAdapter);
/*     */ 
/* 121 */     PromptHandler ph = new PromptHandler()
/*     */     {
/*     */       public int prompt()
/*     */       {
/* 125 */         System.exit(0);
/* 126 */         return 4;
/*     */       }
/*     */     };
/* 129 */     this.m_helper.m_isCloseAllowedCallback = ph;
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 134 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public UserData getUserData()
/*     */   {
/* 139 */     return this.m_userData;
/*     */   }
/*     */ 
/*     */   public boolean onOK()
/*     */   {
/* 145 */     Properties props = this.m_helper.m_props;
/* 146 */     String user = props.getProperty("dName");
/* 147 */     IdcMessage msg = null;
/*     */ 
/* 149 */     if (user == null)
/*     */     {
/* 151 */       user = "";
/*     */     }
/*     */ 
/* 154 */     String password = props.getProperty("dPassword");
/*     */ 
/* 156 */     Users users = (Users)SharedObjects.getTable("Users");
/* 157 */     this.m_userData = users.getLocalUserData(user);
/*     */ 
/* 159 */     boolean isGood = false;
/* 160 */     if ((this.m_userData != null) && (users.checkLocalUserPassword(user, password)))
/*     */     {
/* 164 */       isGood = this.m_parentCallback.handleDialogEvent(null);
/* 165 */       msg = this.m_parentCallback.m_errorMessage;
/*     */     }
/* 167 */     if ((msg == null) && (!isGood))
/*     */     {
/* 169 */       msg = IdcMessageFactory.lc("csLoginFailed", new Object[0]);
/*     */     }
/*     */ 
/* 172 */     if (msg != null)
/*     */     {
/* 174 */       MessageBox.reportError(new AppFrameHelper(), this.m_helper.m_dialog, msg, IdcMessageFactory.lc("apTitleContentServerMessage", new Object[0]));
/* 175 */       this.m_passwordField.setText("");
/* 176 */       return false;
/*     */     }
/*     */ 
/* 179 */     return this.m_userData != null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 184 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86005 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.LoginDlg
 * JD-Core Version:    0.5.4
 */