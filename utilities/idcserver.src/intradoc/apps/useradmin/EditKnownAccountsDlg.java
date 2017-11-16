/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditKnownAccountsDlg
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   public SystemInterface m_systemInterface;
/*     */   public ExecutionContext m_ctx;
/*     */   protected String m_helpPage;
/*     */   public UdlPanel m_accountsList;
/*     */   protected JPanel m_editButtonsPanel;
/*     */ 
/*     */   public EditKnownAccountsDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  69 */     this.m_helper = new DialogHelper(sys, title, true);
/*  70 */     this.m_systemInterface = sys;
/*  71 */     this.m_ctx = sys.getExecutionContext();
/*  72 */     this.m_helpPage = DialogHelpTable.getHelpPage(helpPage);
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  78 */     this.m_helper.m_ok = this.m_helper.addCommandButton(LocaleResources.getString("apLabelClose", this.m_ctx), this.m_helper);
/*     */ 
/*  82 */     this.m_helper.addHelp(null);
/*  83 */     this.m_helper.m_helpPage = this.m_helpPage;
/*  84 */     if (this.m_helpPage == null)
/*     */     {
/*  86 */       this.m_helper.m_help.setEnabled(false);
/*     */     }
/*     */ 
/*  90 */     this.m_helper.m_componentBinder = this;
/*     */ 
/*  93 */     this.m_helper.makePanelGridBag(this.m_helper.m_mainPanel, 1);
/*  94 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/*     */ 
/*  97 */     this.m_helper.addPanelTitle(mainPanel, LocaleResources.getString("apTitlePredefinedAccounts", this.m_ctx));
/*     */ 
/* 100 */     this.m_accountsList = new UdlPanel(null, null, 200, 20, "DocumentAccounts", false);
/* 101 */     this.m_accountsList.setVisibleColumns("dDocAccount");
/*     */ 
/* 103 */     this.m_accountsList.init();
/* 104 */     this.m_accountsList.useDefaultListener();
/* 105 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 106 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 107 */     this.m_helper.addComponent(mainPanel, this.m_accountsList);
/* 108 */     this.m_editButtonsPanel = new PanePanel();
/* 109 */     this.m_editButtonsPanel.setLayout(new GridLayout(0, 1, 5, 5));
/* 110 */     ActionListener al = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 114 */         EditKnownAccountsDlg.this.handleEditAccount(e);
/*     */       }
/*     */     };
/* 118 */     this.m_helper.addCommandButton(this.m_editButtonsPanel, LocaleResources.getString("apDlgButtonAdd", this.m_ctx), "add", al);
/*     */ 
/* 120 */     JButton delBtn = this.m_helper.addCommandButton(this.m_editButtonsPanel, LocaleResources.getString("apLabelDelete", this.m_ctx), "delete", al);
/*     */ 
/* 123 */     this.m_accountsList.addControlComponent(delBtn);
/* 124 */     JPanel editBtnsWrapper = new PanePanel();
/* 125 */     editBtnsWrapper.setLayout(new BorderLayout());
/* 126 */     editBtnsWrapper.add("North", this.m_editButtonsPanel);
/* 127 */     this.m_helper.addLastComponentInRow(mainPanel, editBtnsWrapper);
/*     */ 
/* 129 */     refreshList();
/*     */ 
/* 131 */     prompt();
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 136 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void refreshList()
/*     */   {
/* 141 */     DataResultSet drset = new DataResultSet(new String[] { "dDocAccount" });
/* 142 */     String selectedAccount = this.m_accountsList.getSelectedObj();
/*     */ 
/* 145 */     Vector v = SharedObjects.getOptList("docAccounts");
/* 146 */     if (v != null)
/*     */     {
/* 148 */       int size = v.size();
/* 149 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 151 */         Vector row = drset.createEmptyRow();
/* 152 */         row.setElementAt(v.elementAt(i), 0);
/* 153 */         drset.addRow(row);
/*     */       }
/*     */     }
/*     */ 
/* 157 */     this.m_accountsList.refreshList(drset, selectedAccount);
/*     */   }
/*     */ 
/*     */   protected void handleEditAccount(ActionEvent evt)
/*     */   {
/* 162 */     String cmd = evt.getActionCommand();
/* 163 */     if (cmd.equals("add"))
/*     */     {
/* 165 */       DialogHelper addAccountDlg = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apLabelAddNewPredefinedAccount", this.m_ctx), true);
/*     */ 
/* 167 */       DialogCallback okCallback = new DialogCallback()
/*     */       {
/*     */         public boolean handleDialogEvent(ActionEvent e)
/*     */         {
/*     */           try
/*     */           {
/* 174 */             AppLauncher.executeService("ADD_DOC_ACCOUNT", this.m_dlgHelper.m_props);
/* 175 */             EditKnownAccountsDlg.this.refreshList();
/* 176 */             return true;
/*     */           }
/*     */           catch (Exception exp)
/*     */           {
/* 180 */             this.m_errorMessage = IdcMessageFactory.lc(exp);
/* 181 */           }return false;
/*     */         }
/*     */       };
/* 186 */       JPanel mainPanel = addAccountDlg.initStandard(this, okCallback, 2, false, null);
/*     */ 
/* 188 */       addAccountDlg.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelPredefinedAccount", this.m_ctx), new CustomTextField(20), "dDocAccount");
/*     */ 
/* 190 */       addAccountDlg.prompt();
/*     */     } else {
/* 192 */       if (!cmd.equals("delete"))
/*     */         return;
/* 194 */       String selItem = this.m_accountsList.getSelectedObj();
/* 195 */       if (selItem == null)
/*     */         return;
/*     */       try
/*     */       {
/* 199 */         this.m_helper.m_props.put("dDocAccount", selItem);
/* 200 */         AppLauncher.executeService("DELETE_DOC_ACCOUNT", this.m_helper.m_props);
/* 201 */         refreshList();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 205 */         MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToDeleteAccount", new Object[0]));
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 217 */     if ((!updateComponent) && (exchange.m_compName.equals("dDocAccount")))
/*     */     {
/* 219 */       exchange.m_compValue = FileUtils.fileSlashes(exchange.m_compValue);
/*     */     }
/* 221 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 222 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 227 */     String name = exchange.m_compName;
/* 228 */     String val = exchange.m_compValue;
/*     */ 
/* 230 */     String[] errMsg = { "" };
/* 231 */     boolean success = true;
/* 232 */     if (name.equals("dDocAccount"))
/*     */     {
/* 234 */       success = UserUtils.isValidAccountName(val, errMsg, 1, this.m_ctx);
/*     */     }
/*     */ 
/* 237 */     if (!success)
/*     */     {
/* 239 */       IdcMessage msg = IdcMessageFactory.lc();
/* 240 */       msg.m_msgEncoded = errMsg[0];
/* 241 */       exchange.m_errorMessage = msg;
/* 242 */       return false;
/*     */     }
/* 244 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 249 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92636 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditKnownAccountsDlg
 * JD-Core Version:    0.5.4
 */