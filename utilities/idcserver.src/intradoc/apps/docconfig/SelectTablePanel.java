/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SelectTablePanel extends DocConfigPanel
/*     */   implements ActionListener
/*     */ {
/*     */   protected String m_viewType;
/*     */   protected UdlPanel m_tableList;
/*     */   protected UdlPanel m_optionList;
/*     */ 
/*     */   public SelectTablePanel()
/*     */   {
/*  57 */     this.m_viewType = null;
/*  58 */     this.m_tableList = null;
/*  59 */     this.m_optionList = null;
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder) throws ServiceException
/*     */   {
/*  64 */     super.initEx(sys, binder);
/*     */ 
/*  66 */     initUI();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  71 */     initListPanel();
/*  72 */     JPanel btnPanel = initButtonPanel();
/*     */ 
/*  75 */     JPanel tablePanel = new PanePanel();
/*  76 */     this.m_helper.makePanelGridBag(tablePanel, 1);
/*  77 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/*  78 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  79 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  80 */     this.m_helper.addComponent(tablePanel, this.m_tableList);
/*     */ 
/*  82 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/*  83 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/*  84 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/*  85 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 5, 0, 5);
/*  86 */     this.m_helper.addLastComponentInRow(tablePanel, btnPanel);
/*     */ 
/*  88 */     add("selectTable", tablePanel);
/*     */ 
/*  91 */     JPanel optionListPanel = new PanePanel();
/*  92 */     this.m_helper.makePanelGridBag(optionListPanel, 1);
/*     */ 
/*  94 */     initOptionListPanel();
/*     */ 
/*  96 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*  97 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/*  98 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  99 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 100 */     this.m_helper.addComponent(optionListPanel, this.m_optionList);
/*     */ 
/* 102 */     add("selectOptionList", optionListPanel);
/*     */   }
/*     */ 
/*     */   protected void initListPanel()
/*     */   {
/* 107 */     this.m_tableList = new UdlPanel(LocaleResources.getString("apLabelTables", this.m_ctx), null, 250, 10, "Tables", true);
/*     */ 
/* 111 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("apTitleTableName", this.m_ctx), "schTableName", 100.0D);
/*     */ 
/* 113 */     this.m_tableList.setColumnInfo(info);
/*     */ 
/* 116 */     this.m_tableList.setVisibleColumns("schTableName");
/* 117 */     this.m_tableList.setIDColumn("schTableName");
/* 118 */     this.m_tableList.init();
/* 119 */     this.m_tableList.useDefaultListener();
/*     */   }
/*     */ 
/*     */   protected JPanel initButtonPanel()
/*     */   {
/* 125 */     String[][] btnInfo = { { "add", "apDlgButtonAddTable", "0", "apDlgButtonAddTable" }, { "edit", "apDlgButtonEditTable", "1", "apDlgButtonEditTable" }, { "delete", "apDlgButtonDeleteTable", "1", "apDlgButtonDeleteTable" } };
/*     */ 
/* 132 */     JPanel btnPanel = new PanePanel();
/* 133 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/* 134 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 136 */       String cmd = btnInfo[i][0];
/* 137 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/*     */ 
/* 139 */       JButton btn = this.m_tableList.addButton(LocaleResources.getString(btnInfo[i][1], this.m_ctx), isControlled);
/* 140 */       btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_ctx));
/* 141 */       btn.setActionCommand(cmd);
/* 142 */       btn.addActionListener(this);
/* 143 */       this.m_helper.addLastComponentInRow(btnPanel, btn);
/*     */     }
/*     */ 
/* 146 */     return btnPanel;
/*     */   }
/*     */ 
/*     */   protected void initOptionListPanel()
/*     */   {
/* 151 */     this.m_optionList = new UdlPanel(LocaleResources.getString("apLabelOptions", this.m_ctx), null, 250, 10, "OptionList", true);
/*     */ 
/* 155 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("apTitleOptionName", this.m_ctx), "dOptionListKey", 100.0D);
/*     */ 
/* 157 */     this.m_optionList.setColumnInfo(info);
/*     */ 
/* 160 */     this.m_optionList.setVisibleColumns("dOptionListKey");
/* 161 */     this.m_optionList.setIDColumn("dOptionListKey");
/* 162 */     this.m_optionList.init();
/*     */   }
/*     */ 
/*     */   public IdcMessage retrievePanelValuesAndValidate()
/*     */   {
/* 169 */     IdcMessage errMsg = null;
/* 170 */     if (this.m_viewType.equals("table"))
/*     */     {
/* 172 */       String schName = this.m_tableList.getSelectedObj();
/* 173 */       if (schName == null)
/*     */       {
/* 175 */         errMsg = IdcMessageFactory.lc("apSchSelectTable", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 179 */         this.m_helper.m_props.put("schTableName", schName);
/* 180 */         this.m_helper.m_props.remove("schOptionList");
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 185 */       String schName = this.m_optionList.getSelectedObj();
/* 186 */       if (schName == null)
/*     */       {
/* 188 */         errMsg = IdcMessageFactory.lc("apSchSelectOptionList", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 192 */         this.m_helper.m_props.put("schOptionList", schName);
/* 193 */         this.m_helper.m_props.remove("schTableName");
/*     */       }
/*     */     }
/* 196 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 204 */     String cmd = e.getActionCommand();
/* 205 */     if (cmd.equals("add"))
/*     */     {
/* 207 */       addOrEditTable(true);
/*     */     }
/* 209 */     else if (cmd.equals("edit"))
/*     */     {
/* 211 */       addOrEditTable(false);
/*     */     } else {
/* 213 */       if (!cmd.equals("delete"))
/*     */         return;
/* 215 */       deleteTable();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addOrEditTable(boolean isAdd)
/*     */   {
/* 221 */     Properties props = new Properties();
/* 222 */     String title = "apSchAddTableDlg";
/* 223 */     String tableName = "sch";
/* 224 */     if (!isAdd)
/*     */     {
/* 226 */       tableName = this.m_tableList.getSelectedObj();
/* 227 */       title = "apSchEditTableDlg";
/*     */     }
/* 229 */     props.put("schTableName", tableName);
/* 230 */     title = LocaleUtils.encodeMessage(title, null, tableName);
/* 231 */     title = LocaleResources.localizeMessage(title, this.m_ctx);
/*     */ 
/* 233 */     AddTableDlg dlg = new AddTableDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("AddOrEditSchemaTable"));
/*     */ 
/* 235 */     int result = dlg.init(props, isAdd);
/* 236 */     if (result != 1)
/*     */       return;
/*     */     try
/*     */     {
/* 240 */       loadPanelInformation();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 244 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteTable()
/*     */   {
/* 251 */     String tableName = this.m_tableList.getSelectedObj();
/* 252 */     IdcMessage msg = IdcMessageFactory.lc("apSchDeleteTable", new Object[] { tableName });
/*     */ 
/* 254 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 4);
/* 255 */     if (result != 2)
/*     */       return;
/*     */     try
/*     */     {
/* 259 */       DataBinder binder = new DataBinder();
/* 260 */       binder.putLocal("schTableName", tableName);
/* 261 */       AppLauncher.executeService("DELETE_SCHEMA_TABLE", binder);
/*     */ 
/* 264 */       loadPanelInformation();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 268 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 272 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Properties promptNewTableName()
/*     */   {
/* 279 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 285 */         Properties props = this.m_dlgHelper.m_props;
/* 286 */         String name = props.getProperty("schTableName");
/* 287 */         int val = Validation.checkDatabaseFieldName(name);
/* 288 */         switch (val)
/*     */         {
/*     */         case 0:
/* 291 */           if (name.length() > 64)
/*     */           {
/* 293 */             this.m_errorMessage = IdcMessageFactory.lc("apSchTableNameExceedsMaxLength", new Object[] { Integer.valueOf(64) }); } break;
/*     */         case -1:
/* 297 */           this.m_errorMessage = IdcMessageFactory.lc("apSchSpecifyTableName", new Object[0]);
/* 298 */           break;
/*     */         case -2:
/* 300 */           this.m_errorMessage = IdcMessageFactory.lc("apSchNameCannotContainSpaces", new Object[0]);
/* 301 */           break;
/*     */         case -3:
/* 303 */           this.m_errorMessage = IdcMessageFactory.lc("apSchInvalidCharInTableName", new Object[0]);
/* 304 */           break;
/*     */         default:
/* 306 */           this.m_errorMessage = IdcMessageFactory.lc("apSchInvalidNameForTableName", new Object[0]);
/*     */         }
/*     */ 
/* 311 */         return this.m_errorMessage == null;
/*     */       }
/*     */     };
/* 318 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apSchAddTableDlg", this.m_ctx), true);
/*     */ 
/* 320 */     JPanel mainPanel = helper.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage("AddTable"));
/*     */ 
/* 323 */     String desc = LocaleResources.getString("apSchAddTableDescription", this.m_ctx);
/*     */ 
/* 325 */     helper.m_gridHelper.prepareAddLastRowElement();
/* 326 */     helper.addComponent(mainPanel, new CustomText(desc, 50, 17));
/* 327 */     helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelTableName", this.m_ctx), new CustomTextField(20), "schTableName");
/*     */ 
/* 330 */     helper.m_props.put("schTableName", "sch");
/*     */ 
/* 333 */     if (helper.prompt() == 1)
/*     */     {
/* 335 */       return helper.m_props;
/*     */     }
/* 337 */     return null;
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/* 344 */     this.m_viewType = this.m_helper.m_props.getProperty("schViewType");
/* 345 */     if (this.m_viewType.equals("table"))
/*     */     {
/*     */       try
/*     */       {
/* 349 */         DataBinder binder = new DataBinder();
/* 350 */         AppLauncher.executeService("GET_SCHEMA_TABLES", binder);
/*     */ 
/* 353 */         this.m_tableList.refreshList(binder, null);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 357 */         throw new DataException(null, e);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */       try
/*     */       {
/* 364 */         DataBinder binder = new DataBinder();
/* 365 */         AppLauncher.executeService("GET_OPTION_LIST", binder);
/*     */ 
/* 368 */         this.m_optionList.refreshList(binder, null);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 372 */         throw new DataException(null, e);
/*     */       }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 379 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.SelectTablePanel
 * JD-Core Version:    0.5.4
 */