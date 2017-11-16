/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocRulePanel extends DocConfigPanel
/*     */   implements ActionListener
/*     */ {
/*  54 */   protected UdlPanel m_rulesList = null;
/*     */ 
/*     */   public DocRulePanel()
/*     */   {
/*  58 */     this.m_subject = "docprofiles";
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys)
/*     */     throws ServiceException
/*     */   {
/*  64 */     super.init(sys);
/*     */ 
/*  66 */     initUI();
/*     */ 
/*  69 */     refreshView();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  74 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*  75 */     JPanel pnl = new PanePanel();
/*  76 */     gh.useGridBag(pnl);
/*     */ 
/*  78 */     this.m_rulesList = createList();
/*     */ 
/*  81 */     gh.m_gc.fill = 0;
/*  82 */     gh.m_gc.weighty = 0.0D;
/*  83 */     addButtons(pnl);
/*     */ 
/*  86 */     gh.useGridBag(this);
/*  87 */     gh.m_gc.weightx = 1.0D;
/*  88 */     gh.m_gc.weighty = 1.0D;
/*  89 */     gh.m_gc.fill = 1;
/*  90 */     this.m_helper.addLastComponentInRow(this, this.m_rulesList);
/*  91 */     gh.m_gc.weightx = 0.0D;
/*  92 */     this.m_helper.addLastComponentInRow(this, pnl);
/*     */   }
/*     */ 
/*     */   protected UdlPanel createList()
/*     */   {
/*  97 */     String columns = "dpRuleName,dpRuleDescription";
/*  98 */     UdlPanel list = new UdlPanel(this.m_systemInterface.getString("apDpRuleListLabel"), null, 500, 20, "DocumentRules", true);
/*     */ 
/* 102 */     ColumnInfo info = new ColumnInfo(this.m_systemInterface.getString("apDpRuleNameColumn"), "dpRuleName", 8.0D);
/* 103 */     list.setColumnInfo(info);
/* 104 */     info = new ColumnInfo(this.m_systemInterface.getString("apDpRuleDescriptionColumn"), "dpRuleDescription", 14.0D);
/*     */ 
/* 106 */     list.setColumnInfo(info);
/*     */ 
/* 108 */     list.setVisibleColumns(columns);
/* 109 */     list.setIDColumn("dpRuleName");
/* 110 */     list.useDefaultListener();
/* 111 */     list.m_list.addActionListener(this);
/*     */ 
/* 113 */     DisplayStringCallbackAdaptor dspCallback = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 119 */         String displayStr = DocRulePanel.this.m_systemInterface.getString(value);
/* 120 */         if (displayStr == null)
/*     */         {
/* 122 */           displayStr = value;
/*     */         }
/* 124 */         return displayStr;
/*     */       }
/*     */     };
/* 127 */     list.setDisplayCallback("dpRuleDescription", dspCallback);
/*     */ 
/* 129 */     list.init();
/* 130 */     return list;
/*     */   }
/*     */ 
/*     */   protected void addButtons(JPanel pnl)
/*     */   {
/* 136 */     String[][] btnInfo = { { "add", "apDpDlgButtonAddRule", "0", "apDpAddRuleTitle" }, { "edit", "apDpDlgButtonEditRule", "1", "apReadableButtonEditRule" }, { "delete", "apDpDlgButtonDeleteRule", "1", "apReadableButtonDeleteRule" } };
/*     */ 
/* 143 */     JPanel btnPanel = new PanePanel();
/* 144 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 146 */       String cmd = btnInfo[i][0];
/* 147 */       if (cmd.equals("space"))
/*     */       {
/* 150 */         btnPanel.add(new PanePanel());
/*     */       }
/*     */       else {
/* 153 */         boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/* 154 */         JButton btn = this.m_rulesList.addButton(LocaleResources.getString(btnInfo[i][1], this.m_ctx), isControlled);
/*     */ 
/* 156 */         btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_ctx));
/* 157 */         btn.setActionCommand(cmd);
/* 158 */         btn.addActionListener(this);
/* 159 */         btnPanel.add(btn);
/*     */       }
/*     */     }
/* 162 */     this.m_helper.addComponent(pnl, btnPanel);
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */     throws ServiceException
/*     */   {
/* 168 */     String selObj = this.m_rulesList.getSelectedObj();
/* 169 */     refreshData(selObj);
/*     */   }
/*     */ 
/*     */   public void refreshData(String selName) throws ServiceException
/*     */   {
/* 174 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 177 */       executeService("GET_DOCRULES", binder, false);
/* 178 */       this.m_rulesList.refreshList(binder, selName);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 182 */       Report.trace("profiles", null, e);
/* 183 */       reportError(e);
/*     */     }
/*     */     finally
/*     */     {
/* 187 */       checkSelection();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 193 */     int index = this.m_rulesList.getSelectedIndex();
/* 194 */     boolean isSelected = index >= 0;
/* 195 */     this.m_rulesList.enableDisable(isSelected);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 203 */     Object obj = e.getSource();
/* 204 */     if (obj instanceof UserDrawList)
/*     */     {
/* 207 */       addOrEditRule(false);
/*     */     }
/*     */     else
/*     */     {
/* 212 */       String cmd = e.getActionCommand();
/* 213 */       if (cmd.equals("add"))
/*     */       {
/* 215 */         addOrEditRule(true);
/*     */       }
/* 217 */       else if (cmd.equals("edit"))
/*     */       {
/* 219 */         addOrEditRule(false);
/*     */       } else {
/* 221 */         if (!cmd.equals("delete"))
/*     */           return;
/* 223 */         deleteRule();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addOrEditRule(boolean isAdd)
/*     */   {
/* 230 */     Properties props = null;
/* 231 */     String title = null;
/* 232 */     String helpPageName = null;
/*     */ 
/* 234 */     if (isAdd)
/*     */     {
/* 236 */       props = new Properties();
/* 237 */       title = "apDpTitleAddNewDocumentRule";
/* 238 */       helpPageName = "DpAddRule";
/*     */     }
/*     */     else
/*     */     {
/* 242 */       int index = this.m_rulesList.getSelectedIndex();
/* 243 */       if (index < 0)
/*     */       {
/* 245 */         reportError(null, IdcMessageFactory.lc("apDpSelectRuleToEdit", new Object[0]));
/* 246 */         return;
/*     */       }
/* 248 */       props = this.m_rulesList.getDataAt(index);
/* 249 */       title = "apDpTitleEditDocumentRule";
/* 250 */       helpPageName = "DpEditRule";
/*     */     }
/*     */ 
/* 253 */     title = LocaleUtils.encodeMessage(title, null, props.getProperty("dpRuleName"));
/* 254 */     title = LocaleResources.localizeMessage(title, this.m_ctx);
/* 255 */     EditRuleDlg dlg = new EditRuleDlg(this.m_systemInterface, title, this, DialogHelpTable.getHelpPage(helpPageName));
/*     */ 
/* 257 */     if (dlg.prompt(props, isAdd) != 1)
/*     */       return;
/*     */     try
/*     */     {
/* 261 */       String name = props.getProperty("dpRuleName");
/* 262 */       refreshData(name);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 266 */       reportError(exp);
/* 267 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteRule()
/*     */   {
/* 274 */     int index = this.m_rulesList.getSelectedIndex();
/* 275 */     if (index < 0)
/*     */     {
/* 278 */       reportError(null, IdcMessageFactory.lc("apDpSelectRuleToDelete", new Object[0]));
/* 279 */       return;
/*     */     }
/*     */ 
/* 282 */     Properties props = this.m_rulesList.getDataAt(index);
/* 283 */     String name = props.getProperty("dpRuleName");
/*     */ 
/* 285 */     if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apDpVerifyRuleToDelete", new Object[] { name }), 4) != 2) {
/*     */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 291 */       DataBinder binder = new DataBinder();
/* 292 */       binder.setLocalData(props);
/* 293 */       executeService("DELETE_DOCRULE", binder, false);
/* 294 */       refreshData(null);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 298 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 311 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.DocRulePanel
 * JD-Core Version:    0.5.4
 */