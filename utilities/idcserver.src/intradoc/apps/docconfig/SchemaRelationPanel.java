/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.schema.SchemaData;
/*     */ import intradoc.shared.schema.SchemaRelationConfig;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SchemaRelationPanel extends DocConfigPanel
/*     */   implements ActionListener
/*     */ {
/*  58 */   protected UdlPanel m_list = null;
/*     */   protected SchemaRelationConfig m_schemaRelationConfig;
/*  60 */   public boolean m_verticalButtons = false;
/*     */ 
/*     */   public SchemaRelationPanel()
/*     */   {
/*  64 */     this.m_subject = "schema";
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  71 */     super.initEx(sys, binder);
/*     */ 
/*  73 */     initUI();
/*     */ 
/*  76 */     refreshData(null);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  81 */     initListPanel();
/*  82 */     this.m_helper.makePanelGridBag(this, 1);
/*     */ 
/*  85 */     JPanel panel = new PanePanel();
/*  86 */     this.m_helper.makePanelGridBag(panel, 1);
/*  87 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  88 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  89 */     if (this.m_verticalButtons)
/*     */     {
/*  91 */       this.m_helper.addComponent(panel, this.m_list);
/*     */     }
/*     */     else
/*     */     {
/*  95 */       this.m_helper.addLastComponentInRow(panel, this.m_list);
/*     */     }
/*  97 */     this.m_helper.addLastComponentInRow(this, panel);
/*     */ 
/*  99 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 100 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 101 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 102 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 5, 5);
/*     */ 
/* 106 */     JPanel btnPanel = initButtonPanel();
/* 107 */     this.m_helper.addLastComponentInRow(panel, btnPanel);
/*     */ 
/* 109 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 110 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 111 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 112 */     add(panel, this.m_helper.m_gridHelper.m_gc);
/*     */   }
/*     */ 
/*     */   protected void initListPanel()
/*     */   {
/* 117 */     this.m_list = new UdlPanel(this.m_systemInterface.localizeMessage("!apSchemaRelationsPanelTitle"), null, 500, 10, "SchemaRelationConfig", true);
/*     */ 
/* 120 */     this.m_list.m_list.addActionListener(this);
/*     */ 
/* 123 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("apTitleRelationName", this.m_ctx), "schRelationName", 30.0D);
/*     */ 
/* 125 */     this.m_list.setColumnInfo(info);
/* 126 */     info = new ColumnInfo(this.m_systemInterface.localizeMessage("!apSchemaTable1Title"), "schTable1Table", 15.0D);
/*     */ 
/* 128 */     this.m_list.setColumnInfo(info);
/* 129 */     info = new ColumnInfo(this.m_systemInterface.localizeMessage("!apSchemaTable1ColumnTitle"), "schTable1Column", 15.0D);
/*     */ 
/* 131 */     this.m_list.setColumnInfo(info);
/* 132 */     info = new ColumnInfo(this.m_systemInterface.localizeMessage("!apSchemaTable2Title"), "schTable2Table", 15.0D);
/*     */ 
/* 134 */     this.m_list.setColumnInfo(info);
/* 135 */     info = new ColumnInfo(this.m_systemInterface.localizeMessage("!apSchemaTable2ColumnTitle"), "schTable2Column", 15.0D);
/*     */ 
/* 137 */     this.m_list.setColumnInfo(info);
/*     */ 
/* 140 */     this.m_list.setVisibleColumns("schRelationName,schTable1Table,schTable1Column,schTable2Table,schTable2Column");
/*     */ 
/* 142 */     this.m_list.setIDColumn("schRelationName");
/* 143 */     this.m_list.init();
/* 144 */     this.m_list.useDefaultListener();
/*     */   }
/*     */ 
/*     */   protected JPanel initButtonPanel()
/*     */   {
/* 150 */     String[][] btnInfo = { { "add", "apDlgButtonAddRelation", "0", "apSchemaAddRelationDialogTitle" }, { "edit", "apDlgButtonEditRelation", "1", "apSchemaEditRelationDialogTitle" }, { "delete", "apDlgButtonDeleteRelation", "1", "apReadableButtonDeleteRelationship" } };
/*     */ 
/* 157 */     JPanel btnPanel = new PanePanel();
/* 158 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/* 159 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 161 */       String cmd = btnInfo[i][0];
/* 162 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/*     */ 
/* 164 */       JButton btn = this.m_list.addButton(LocaleResources.getString(btnInfo[i][1], this.m_ctx), isControlled);
/* 165 */       btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_ctx));
/* 166 */       btn.setActionCommand(cmd);
/* 167 */       btn.addActionListener(this);
/* 168 */       if (this.m_verticalButtons)
/*     */       {
/* 170 */         this.m_helper.addLastComponentInRow(btnPanel, btn);
/*     */       }
/*     */       else
/*     */       {
/* 174 */         this.m_helper.addComponent(btnPanel, btn);
/*     */       }
/*     */     }
/*     */ 
/* 178 */     return btnPanel;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 186 */     String cmd = e.getActionCommand();
/* 187 */     if (cmd.equals("add"))
/*     */     {
/* 189 */       addOrEdit(true);
/*     */     }
/* 191 */     else if ((cmd.equals("edit")) || (cmd.equals("")))
/*     */     {
/* 193 */       addOrEdit(false);
/*     */     } else {
/* 195 */       if (!cmd.equals("delete"))
/*     */         return;
/* 197 */       delete();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addOrEdit(boolean isAdd)
/*     */   {
/* 203 */     DataBinder binder = new DataBinder();
/* 204 */     Properties props = binder.getLocalData();
/* 205 */     String title = "apSchemaAddRelationDialogTitle";
/* 206 */     String name = "";
/* 207 */     if (!isAdd)
/*     */     {
/* 209 */       name = this.m_list.getSelectedObj();
/* 210 */       if (name == null)
/*     */       {
/* 212 */         if (SystemUtils.m_verbose)
/*     */         {
/* 214 */           Report.debug("schema", "skipping because nothing is selected", null);
/*     */         }
/* 216 */         return;
/*     */       }
/* 218 */       title = "apSchemaEditRelationDialogTitle";
/* 219 */       SchemaData data = this.m_schemaRelationConfig.getData(name);
/* 220 */       if (data == null)
/*     */       {
/* 222 */         if (SystemUtils.m_verbose)
/*     */         {
/* 224 */           Report.debug("schema", "skipping because the selected item " + name + " has no data.", null);
/*     */         }
/*     */ 
/* 227 */         return;
/*     */       }
/* 229 */       data.populateBinder(binder);
/*     */     }
/* 231 */     props.put("schRelationName", name);
/* 232 */     title = LocaleUtils.encodeMessage(title, null, name);
/* 233 */     title = LocaleResources.localizeMessage(title, this.m_ctx);
/*     */ 
/* 235 */     AddRelationDialog dlg = new AddRelationDialog(this.m_systemInterface, title, DialogHelpTable.getHelpPage("AddOrEditSchemaRelation"));
/*     */ 
/* 237 */     int result = dlg.init(props, isAdd);
/* 238 */     if (result != 1)
/*     */       return;
/* 240 */     name = props.getProperty("schRelationName");
/* 241 */     refreshData(name);
/*     */   }
/*     */ 
/*     */   protected void delete()
/*     */   {
/* 247 */     String name = this.m_list.getSelectedObj();
/* 248 */     if (name == null)
/*     */     {
/* 250 */       return;
/*     */     }
/* 252 */     SchemaData data = this.m_schemaRelationConfig.getData(name);
/* 253 */     String table1 = data.get("schTable1Table");
/* 254 */     String table2 = data.get("schTable2Table");
/* 255 */     IdcMessage msg = IdcMessageFactory.lc("apSchDeleteRelation", new Object[] { name, table1, table2 });
/*     */ 
/* 257 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 4);
/*     */ 
/* 259 */     if (result != 2)
/*     */       return;
/*     */     try
/*     */     {
/* 263 */       DataBinder binder = new DataBinder();
/* 264 */       binder.putLocal("schRelationName", name);
/* 265 */       AppLauncher.executeService("DELETE_SCHEMA_RELATION", binder);
/*     */ 
/* 268 */       refreshData(name);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 272 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void editValues()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void refreshData(String selName)
/*     */   {
/* 284 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 287 */       this.m_schemaRelationConfig = ((SchemaRelationConfig)SharedObjects.getTable("SchemaRelationConfig"));
/* 288 */       if (!AppLauncher.getIsStandAlone())
/*     */       {
/* 290 */         if (this.m_schemaRelationConfig != null)
/*     */         {
/* 293 */           binder.addResultSet("SchemaRelationConfig", this.m_schemaRelationConfig);
/*     */         }
/* 295 */         AppLauncher.executeService("GET_SCHEMA_RELATIONS", binder);
/* 296 */         this.m_schemaRelationConfig = ((SchemaRelationConfig)SharedObjects.getTable("SchemaRelationConfig"));
/*     */       }
/*     */ 
/* 299 */       refreshList(selName);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 303 */       Report.trace("schema", null, e);
/* 304 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void refreshList(String selectedItemName)
/*     */   {
/* 310 */     this.m_list.refreshList(this.m_schemaRelationConfig, selectedItemName);
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */     throws ServiceException
/*     */   {
/* 317 */     String selectedName = this.m_list.getSelectedObj();
/* 318 */     refreshData(selectedName);
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 329 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.SchemaRelationPanel
 * JD-Core Version:    0.5.4
 */