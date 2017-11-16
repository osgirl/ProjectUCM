/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.schema.SchemaResultSet;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SchemaTablePanel extends DocConfigPanel
/*     */   implements ActionListener
/*     */ {
/*  58 */   protected UdlPanel m_tableList = null;
/*  59 */   public boolean m_verticalButtons = true;
/*  60 */   public boolean m_noButtons = false;
/*  61 */   public String m_resultSetName = "SchemaTableConfig";
/*     */ 
/*  63 */   protected Vector m_actionListeners = new IdcVector();
/*     */ 
/*     */   public SchemaTablePanel()
/*     */   {
/*  67 */     this.m_subject = "schema";
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  74 */     super.initEx(sys, binder);
/*     */ 
/*  76 */     initUI();
/*     */ 
/*  79 */     refreshData(null);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  84 */     initListPanel();
/*  85 */     this.m_helper.makePanelGridBag(this, 1);
/*     */ 
/*  88 */     JPanel tablePanel = new PanePanel();
/*  89 */     this.m_helper.makePanelGridBag(tablePanel, 1);
/*  90 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  91 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  92 */     if (this.m_verticalButtons)
/*     */     {
/*  94 */       this.m_helper.addComponent(tablePanel, this.m_tableList);
/*     */     }
/*     */     else
/*     */     {
/*  98 */       this.m_helper.addLastComponentInRow(tablePanel, this.m_tableList);
/*     */     }
/* 100 */     this.m_helper.addLastComponentInRow(this, tablePanel);
/*     */ 
/* 102 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 103 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 104 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 105 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 5, 5);
/*     */ 
/* 109 */     if (!this.m_noButtons)
/*     */     {
/* 111 */       JPanel btnPanel = initButtonPanel();
/* 112 */       this.m_helper.addLastComponentInRow(tablePanel, btnPanel);
/*     */     }
/*     */ 
/* 115 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 116 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 117 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 118 */     add(tablePanel, this.m_helper.m_gridHelper.m_gc);
/*     */   }
/*     */ 
/*     */   public void addActionListener(ActionListener listener)
/*     */   {
/* 123 */     this.m_actionListeners.addElement(listener);
/*     */   }
/*     */ 
/*     */   protected void initListPanel()
/*     */   {
/* 128 */     this.m_tableList = new UdlPanel(LocaleResources.getString("apLabelTables", this.m_ctx), null, 500, 10, this.m_resultSetName, true);
/*     */ 
/* 130 */     this.m_tableList.m_list.addActionListener(this);
/*     */ 
/* 133 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("apTitleTableName", this.m_ctx), "schTableName", 100.0D);
/*     */ 
/* 135 */     this.m_tableList.setColumnInfo(info);
/*     */ 
/* 138 */     this.m_tableList.setVisibleColumns("schTableName");
/* 139 */     this.m_tableList.setIDColumn("schTableName");
/* 140 */     this.m_tableList.init();
/* 141 */     this.m_tableList.useDefaultListener();
/*     */   }
/*     */ 
/*     */   protected JPanel initButtonPanel()
/*     */   {
/* 147 */     String[][] btnInfo = { { "create", "apDlgButtonCreateTable", "0", "apSchAddTableDlg" }, { "add", "apDlgButtonAddTable", "0", "apDlgButtonAddTable" }, { "edit", "apDlgButtonEditTable", "1", "apDlgButtonEditTable" }, { "delete", "apDlgButtonDeleteTable", "1", "apDlgButtonDeleteTable" } };
/*     */ 
/* 155 */     JPanel btnPanel = new PanePanel();
/* 156 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/* 157 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 159 */       String cmd = btnInfo[i][0];
/* 160 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/*     */ 
/* 162 */       JButton btn = this.m_tableList.addButton(LocaleResources.getString(btnInfo[i][1], this.m_ctx), isControlled);
/* 163 */       btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_ctx));
/* 164 */       btn.setActionCommand(cmd);
/* 165 */       btn.addActionListener(this);
/* 166 */       if (this.m_verticalButtons)
/*     */       {
/* 168 */         this.m_helper.addLastComponentInRow(btnPanel, btn);
/*     */       }
/*     */       else
/*     */       {
/* 172 */         this.m_helper.addComponent(btnPanel, btn);
/*     */       }
/*     */     }
/*     */ 
/* 176 */     return btnPanel;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/*     */     try
/*     */     {
/* 186 */       String cmd = event.getActionCommand();
/* 187 */       if (cmd.equals("create"))
/*     */       {
/* 189 */         addOrEdit(true, null);
/*     */       }
/* 191 */       else if (cmd.equals("add"))
/*     */       {
/* 193 */         addExistingTable();
/*     */       }
/* 195 */       else if (cmd.equals(""))
/*     */       {
/* 198 */         String tableName = this.m_tableList.getSelectedObj();
/* 199 */         if (this.m_actionListeners.size() > 0)
/*     */         {
/* 201 */           ActionEvent newEvent = new ActionEvent(this, 1001, tableName, event.getModifiers());
/*     */ 
/* 204 */           for (int i = 0; i < this.m_actionListeners.size(); ++i)
/*     */           {
/* 206 */             ActionListener listener = (ActionListener)this.m_actionListeners.elementAt(i);
/*     */ 
/* 208 */             listener.actionPerformed(newEvent);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 213 */           addOrEdit(false, tableName);
/*     */         }
/*     */       }
/* 216 */       else if (cmd.equals("edit"))
/*     */       {
/* 218 */         String tableName = this.m_tableList.getSelectedObj();
/* 219 */         addOrEdit(false, tableName);
/*     */       }
/* 221 */       else if (cmd.equals("delete"))
/*     */       {
/* 223 */         delete();
/*     */       }
/* 225 */       else if (cmd.equals("editValues"))
/*     */       {
/* 227 */         editValues();
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 232 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addOrEdit(boolean isAdd, String tableName)
/*     */   {
/* 238 */     Properties props = new IdcProperties();
/* 239 */     String title = LocaleResources.getString("apSchAddTableDlg", this.m_ctx, tableName);
/*     */ 
/* 241 */     if (!isAdd)
/*     */     {
/* 243 */       title = LocaleResources.getString("apSchEditTableDlg", this.m_ctx, tableName);
/*     */     }
/*     */ 
/* 246 */     if (tableName == null)
/*     */     {
/* 248 */       tableName = "";
/*     */     }
/* 250 */     props.put("schTableName", tableName);
/*     */ 
/* 252 */     AddTableDlg dlg = new AddTableDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("AddOrEditSchemaTable"));
/*     */ 
/* 254 */     int result = dlg.init(props, isAdd);
/* 255 */     if (result != 1)
/*     */       return;
/* 257 */     tableName = props.getProperty("schTableName");
/* 258 */     refreshData(tableName);
/*     */   }
/*     */ 
/*     */   protected String addExistingTable()
/*     */     throws ServiceException
/*     */   {
/* 264 */     String helpPage = DialogHelpTable.getHelpPage("AddTable");
/* 265 */     SelectTableDialog dlg = new SelectTableDialog(this.m_systemInterface, this, this.m_systemInterface.localizeMessage("!apSchSelectTablePanelTitle"), helpPage);
/*     */ 
/* 267 */     Properties props = new IdcProperties();
/* 268 */     dlg.init(props);
/*     */ 
/* 270 */     String tableName = props.getProperty("schTableName");
/* 271 */     ResultSet rset = this.m_tableList.getResultSet();
/* 272 */     this.m_tableList.refreshList(rset, tableName);
/* 273 */     return tableName;
/*     */   }
/*     */ 
/*     */   protected void delete() throws ServiceException
/*     */   {
/* 278 */     String tableName = this.m_tableList.getSelectedObj();
/* 279 */     IdcMessage msg = IdcMessageFactory.lc("apSchDeleteTable", new Object[] { tableName });
/*     */ 
/* 286 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 2);
/*     */ 
/* 288 */     if (result != 1)
/*     */       return;
/* 290 */     DataBinder binder = new DataBinder();
/* 291 */     binder.putLocal("schTableName", tableName);
/* 292 */     if (result == 2)
/*     */     {
/* 297 */       binder.putLocal("DropTable", "1");
/*     */     }
/* 299 */     AppLauncher.executeService("DELETE_SCHEMA_TABLE", binder);
/*     */ 
/* 302 */     refreshData(tableName);
/*     */   }
/*     */ 
/*     */   public void editValues()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void refreshData(String selName)
/*     */   {
/* 313 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 316 */       if (this.m_resultSetName.equals("UnknownTableList"))
/*     */       {
/* 318 */         binder.putLocal("LoadDatabaseTables", "1");
/*     */       }
/*     */ 
/* 321 */       SchemaResultSet rset = (SchemaResultSet)SharedObjects.getTable("SchemaTableConfig");
/* 322 */       if (rset != null)
/*     */       {
/* 324 */         binder.addResultSet("SchemaTableConfig", SharedObjects.getTable("SchemaTableConfig"));
/*     */       }
/*     */ 
/* 327 */       executeService("GET_SCHEMA_TABLES", binder, false);
/* 328 */       this.m_tableList.refreshList(binder, selName);
/* 329 */       rset = (SchemaResultSet)SharedObjects.getTable("SchemaTableConfig");
/* 330 */       if (rset != null)
/*     */       {
/* 332 */         binder.addResultSet("SchemaTableConfig", SharedObjects.getTable("SchemaTableConfig"));
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 338 */       Report.trace("schema", null, e);
/* 339 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public IdcMessage retrievePanelValuesAndValidate()
/*     */   {
/* 346 */     IdcMessage errMsg = null;
/* 347 */     String schName = this.m_tableList.getSelectedObj();
/* 348 */     if (schName == null)
/*     */     {
/* 350 */       errMsg = IdcMessageFactory.lc("apSchSelectTable", new Object[0]);
/* 351 */       return errMsg;
/*     */     }
/* 353 */     this.m_helper.m_props.put("schTableName", schName);
/* 354 */     return null;
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */     throws ServiceException
/*     */   {
/* 360 */     String tableName = this.m_tableList.getSelectedObj();
/* 361 */     refreshData(tableName);
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 372 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.SchemaTablePanel
 * JD-Core Version:    0.5.4
 */