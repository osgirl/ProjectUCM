/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ 
/*     */ public class TokenDlg
/*     */   implements ActionListener, ItemListener
/*     */ {
/*     */   protected SystemInterface m_systemInterface;
/*  60 */   protected ExecutionContext m_cxt = null;
/*     */   protected WorkflowContext m_context;
/*     */   protected DialogHelper m_helper;
/*  64 */   protected DataResultSet m_tokenSet = null;
/*  65 */   protected JComboBox m_tokenChoice = null;
/*     */ 
/*  68 */   protected JButton m_editBtn = null;
/*  69 */   protected JButton m_deleteBtn = null;
/*     */ 
/*     */   public TokenDlg(SystemInterface sys, String title)
/*     */   {
/*  73 */     this.m_systemInterface = sys;
/*  74 */     this.m_cxt = sys.getExecutionContext();
/*  75 */     this.m_helper = new DialogHelper(sys, title, true);
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  80 */     CustomPanel pnl = new CustomPanel();
/*  81 */     this.m_helper.makePanelGridBag(pnl, 1);
/*  82 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(15, 15, 5, 15);
/*     */ 
/*  84 */     this.m_tokenChoice = new CustomChoice();
/*  85 */     this.m_tokenChoice.addItemListener(this);
/*     */ 
/*  87 */     JTextArea userText = new CustomTextArea(5, 30);
/*  88 */     userText.setEnabled(false);
/*  89 */     CustomLabel desc = new CustomLabel();
/*  90 */     desc.setMinWidth(30);
/*     */ 
/*  92 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelName", this.m_cxt), this.m_tokenChoice, "wfTokenName");
/*     */ 
/*  94 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 15, 5, 15);
/*  95 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelDescription", this.m_cxt), desc, "wfTokenDescription");
/*     */ 
/*  97 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 15, 15, 15);
/*  98 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  99 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelUsers", this.m_cxt), userText, "wfToken");
/*     */ 
/* 102 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 103 */     mainPanel.setLayout(new BorderLayout());
/* 104 */     mainPanel.add("Center", pnl);
/*     */ 
/* 107 */     String[][] buttonInfo = { { LocaleResources.getString("apDlgButtonAdd", this.m_cxt), "add" }, { LocaleResources.getString("apDlgButtonEdit", this.m_cxt), "edit" }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "delete" }, { LocaleResources.getString("apLabelClose", this.m_cxt), "close" } };
/*     */ 
/* 114 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 116 */       String cmd = buttonInfo[i][1];
/* 117 */       JButton btn = this.m_helper.addCommandButton(buttonInfo[i][0], this);
/* 118 */       btn.setActionCommand(cmd);
/* 119 */       if (cmd.equals("edit"))
/*     */       {
/* 121 */         this.m_editBtn = btn;
/*     */       } else {
/* 123 */         if (!cmd.equals("delete"))
/*     */           continue;
/* 125 */         this.m_deleteBtn = btn;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 130 */     loadTokenChoice();
/* 131 */     checkSelection();
/*     */   }
/*     */ 
/*     */   public void loadTokenChoice()
/*     */   {
/* 136 */     this.m_tokenSet = SharedObjects.getTable("WorkflowTokens");
/* 137 */     if (this.m_tokenSet == null)
/*     */     {
/* 139 */       this.m_context.reportError(null, IdcMessageFactory.lc("apErrorRetrievingTokenList", new Object[0]));
/* 140 */       return;
/*     */     }
/*     */ 
/* 143 */     this.m_tokenChoice.removeAllItems();
/*     */ 
/* 145 */     boolean isEmpty = this.m_tokenSet.isEmpty();
/* 146 */     if (isEmpty)
/*     */     {
/* 148 */       this.m_tokenChoice.addItem(LocaleResources.getString("apChoiceNoValues", this.m_cxt));
/*     */     }
/*     */     else
/*     */     {
/* 152 */       for (this.m_tokenSet.first(); this.m_tokenSet.isRowPresent(); this.m_tokenSet.next())
/*     */       {
/* 154 */         String name = this.m_tokenSet.getStringValue(0);
/* 155 */         this.m_tokenChoice.addItem(name);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 160 */     this.m_tokenChoice.setEnabled(!isEmpty);
/* 161 */     this.m_editBtn.setEnabled(!isEmpty);
/* 162 */     this.m_deleteBtn.setEnabled(!isEmpty);
/*     */   }
/*     */ 
/*     */   public void prompt(WorkflowStateInfo wfInfo, WorkflowContext context)
/*     */   {
/* 167 */     this.m_context = context;
/* 168 */     init();
/* 169 */     this.m_helper.show();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 174 */     boolean result = false;
/* 175 */     String name = null;
/* 176 */     String cmd = e.getActionCommand();
/* 177 */     if (cmd.equals("add"))
/*     */     {
/* 179 */       name = addOrEdit(true);
/*     */     }
/* 181 */     else if (cmd.equals("edit"))
/*     */     {
/* 183 */       name = addOrEdit(false);
/*     */     }
/* 185 */     else if (cmd.equals("delete"))
/*     */     {
/* 187 */       result = deleteToken();
/*     */     }
/* 189 */     else if (cmd.equals("close"))
/*     */     {
/* 191 */       this.m_helper.close();
/*     */     }
/*     */ 
/* 194 */     if ((name == null) && (!result))
/*     */       return;
/* 196 */     loadTokenChoice();
/* 197 */     loadTokenData(name);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 203 */     switch (e.getStateChange())
/*     */     {
/*     */     case 1:
/* 206 */       checkSelection();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 213 */     String name = (String)this.m_tokenChoice.getSelectedItem();
/* 214 */     if (name == null)
/*     */     {
/* 216 */       return;
/*     */     }
/*     */ 
/* 219 */     loadTokenData(name);
/*     */   }
/*     */ 
/*     */   protected void loadTokenData(String name)
/*     */   {
/* 224 */     DataResultSet drset = SharedObjects.getTable("WorkflowTokens");
/* 225 */     if ((drset == null) || (drset.isEmpty()))
/*     */     {
/* 228 */       this.m_helper.m_props = new Properties();
/*     */     }
/*     */     else
/*     */     {
/* 232 */       if (name != null)
/*     */       {
/* 234 */         Vector row = drset.findRow(0, name);
/* 235 */         if (row == null)
/*     */         {
/* 237 */           return;
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 243 */         drset.setCurrentRow(0);
/*     */       }
/*     */ 
/* 246 */       this.m_helper.m_props = drset.getCurrentRowProps();
/*     */     }
/*     */ 
/* 249 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected String addOrEdit(boolean isAdd)
/*     */   {
/* 254 */     String title = LocaleResources.getString("apTitleAddToken", this.m_cxt);
/*     */ 
/* 256 */     Properties props = null;
/* 257 */     if (!isAdd)
/*     */     {
/* 259 */       String name = (String)this.m_tokenChoice.getSelectedItem();
/* 260 */       Vector row = this.m_tokenSet.findRow(0, name);
/* 261 */       if (row == null)
/*     */       {
/* 264 */         return name;
/*     */       }
/*     */ 
/* 267 */       props = this.m_tokenSet.getCurrentRowProps();
/* 268 */       title = LocaleResources.getString("apTitleEditToken", this.m_cxt, name);
/*     */     }
/*     */ 
/* 271 */     EditTokenDlg dlg = new EditTokenDlg(this.m_systemInterface, title);
/* 272 */     dlg.init(props, this.m_context);
/*     */ 
/* 274 */     return dlg.getName();
/*     */   }
/*     */ 
/*     */   protected boolean deleteToken()
/*     */   {
/* 279 */     String name = this.m_helper.m_props.getProperty("wfTokenName");
/* 280 */     if ((name == null) || (name.length() == 0))
/*     */     {
/* 282 */       return false;
/*     */     }
/*     */ 
/* 285 */     IdcMessage msg = IdcMessageFactory.lc("apVerifyTokenDelete", new Object[] { name });
/* 286 */     if (MessageBox.doMessage(this.m_systemInterface, msg, 4) == 3)
/*     */     {
/* 289 */       return false;
/*     */     }
/*     */ 
/* 292 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 295 */       binder.putLocal("wfTokenName", name);
/* 296 */       SharedContext shContext = this.m_context.getSharedContext();
/* 297 */       shContext.executeService("DELETE_WORKFLOW_TOKEN", binder, false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 301 */       this.m_context.reportError(e);
/* 302 */       return false;
/*     */     }
/*     */ 
/* 305 */     this.m_tokenSet = ((DataResultSet)binder.getResultSet("WorkflowTokens"));
/* 306 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 311 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.TokenDlg
 * JD-Core Version:    0.5.4
 */