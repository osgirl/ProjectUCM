/*     */ package intradoc.apputilities.batchloader;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SpiderMapListDialog
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_sysInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected UdlPanel m_mapList;
/*     */   protected JButton m_editBtn;
/*     */   protected JButton m_deleteBtn;
/*  65 */   protected ColumnInfo m_nameInfo = null;
/*     */ 
/*  68 */   protected DataBinder m_mapListBinder = null;
/*  69 */   protected DataResultSet m_mapListSet = null;
/*     */ 
/*  71 */   protected String m_mapDir = null;
/*  72 */   protected String m_mapListFileName = "mapping.hda";
/*  73 */   protected String m_mapDefaultFileName = "default.hda";
/*  74 */   protected String m_mapListTableName = "SpiderMappingList";
/*  75 */   protected String m_mapTableName = "SpiderMapping";
/*  76 */   protected String[] m_mapListFields = { "mapName", "mapDescription" };
/*  77 */   protected String[] m_mapFields = { "mapField", "mapValue" };
/*  78 */   protected int m_mapListNameIndex = -1;
/*  79 */   protected int m_mapListDescIndex = -1;
/*     */ 
/*     */   public SpiderMapListDialog(SystemInterface sys)
/*     */   {
/*  83 */     this.m_cxt = sys.getExecutionContext();
/*  84 */     this.m_helper = new DialogHelper(sys, LocaleResources.getString("csSpiderMapListDialogTitle", this.m_cxt), true);
/*     */ 
/*  86 */     this.m_sysInterface = sys;
/*  87 */     this.m_mapDir = (LegacyDirectoryLocator.getAppDataDirectory() + "search/external/mapping/");
/*  88 */     this.m_mapListBinder = new DataBinder();
/*     */   }
/*     */ 
/*     */   public void init() throws DataException, ServiceException
/*     */   {
/*  93 */     initUI();
/*  94 */     reloadMapList();
/*  95 */     this.m_mapListNameIndex = ResultSetUtils.getIndexMustExist(this.m_mapListSet, "mapName");
/*  96 */     this.m_mapListDescIndex = ResultSetUtils.getIndexMustExist(this.m_mapListSet, "mapDescription");
/*  97 */     refreshDialog(null);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/* 102 */     JPanel mapPanel = initMapPanel();
/* 103 */     initMapList(mapPanel);
/* 104 */     initButtonPanel();
/*     */   }
/*     */ 
/*     */   protected JPanel initMapPanel()
/*     */   {
/* 109 */     JPanel mapPanel = new PanePanel();
/* 110 */     mapPanel.setLayout(new GridBagLayout());
/* 111 */     this.m_helper.makePanelGridBag(this.m_helper.m_mainPanel, 1);
/* 112 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 113 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 114 */     this.m_helper.addLastComponentInRow(this.m_helper.m_mainPanel, mapPanel);
/*     */ 
/* 116 */     return mapPanel;
/*     */   }
/*     */ 
/*     */   protected void initMapList(JPanel mapPanel)
/*     */   {
/* 121 */     this.m_mapList = new UdlPanel(LocaleResources.getString("csSpiderMapListDialogTitle", this.m_cxt), null, 400, 10, this.m_mapListTableName, true);
/*     */ 
/* 125 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("csSpiderMapNameTitle", this.m_cxt), "mapName", 10.0D);
/*     */ 
/* 127 */     this.m_mapList.setColumnInfo(info);
/* 128 */     this.m_nameInfo = info;
/*     */ 
/* 130 */     info = new ColumnInfo(LocaleResources.getString("csSpiderMapDescTitle", this.m_cxt), "mapDescription", 10.0D);
/*     */ 
/* 132 */     this.m_mapList.setColumnInfo(info);
/*     */ 
/* 134 */     this.m_mapList.setVisibleColumns("mapName,mapDescription");
/* 135 */     this.m_mapList.setIDColumn("mapName");
/* 136 */     this.m_mapList.init();
/*     */ 
/* 138 */     this.m_helper.makePanelGridBag(this.m_helper.m_mainPanel, 1);
/* 139 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 30, 5, 30);
/* 140 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 141 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 142 */     this.m_helper.addLastComponentInRow(mapPanel, this.m_mapList);
/*     */   }
/*     */ 
/*     */   protected void initButtonPanel()
/*     */   {
/* 148 */     JButton addBtn = new JButton(LocaleResources.getString("csButtonAddLocale", this.m_cxt));
/* 149 */     ActionListener addListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/*     */         try
/*     */         {
/* 155 */           SpiderMapListDialog.this.addMap();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 159 */           SpiderMapListDialog.this.reportError(e.getMessage());
/*     */         }
/*     */       }
/*     */     };
/* 163 */     addBtn.addActionListener(addListener);
/*     */ 
/* 166 */     this.m_editBtn = new JButton(LocaleResources.getString("csButtonEditLocale", this.m_cxt));
/* 167 */     ActionListener editListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/*     */         try
/*     */         {
/* 173 */           SpiderMapListDialog.this.editMap(null);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 177 */           SpiderMapListDialog.this.reportError(e.getMessage());
/*     */         }
/*     */       }
/*     */     };
/* 181 */     this.m_editBtn.addActionListener(editListener);
/*     */ 
/* 184 */     this.m_deleteBtn = new JButton(LocaleResources.getString("csButtonDeleteLocale", this.m_cxt));
/* 185 */     ActionListener deleteListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/*     */         try
/*     */         {
/* 191 */           SpiderMapListDialog.this.deleteMap();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 195 */           SpiderMapListDialog.this.reportError(e.getMessage());
/*     */         }
/*     */       }
/*     */     };
/* 199 */     this.m_deleteBtn.addActionListener(deleteListener);
/*     */ 
/* 202 */     JButton closeBtn = new JButton(LocaleResources.getString("csSpiderCloseButton", this.m_cxt));
/* 203 */     ActionListener closeListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/* 207 */         SpiderMapListDialog.this.m_helper.close();
/*     */       }
/*     */     };
/* 210 */     closeBtn.addActionListener(closeListener);
/*     */ 
/* 213 */     JPanel buttonPanel = new PanePanel();
/* 214 */     buttonPanel.setLayout(new FlowLayout());
/* 215 */     buttonPanel.add(addBtn);
/* 216 */     buttonPanel.add(this.m_editBtn);
/* 217 */     buttonPanel.add(this.m_deleteBtn);
/* 218 */     buttonPanel.add(closeBtn);
/*     */ 
/* 221 */     this.m_mapList.add("South", buttonPanel);
/*     */   }
/*     */ 
/*     */   protected void addMap() throws DataException, ServiceException
/*     */   {
/* 226 */     Properties mapListProps = new Properties();
/* 227 */     SpiderAddMapDialog dlg = new SpiderAddMapDialog(this.m_sysInterface);
/* 228 */     dlg.init(mapListProps);
/* 229 */     if (dlg.prompt() != 1) {
/*     */       return;
/*     */     }
/* 232 */     String mapName = mapListProps.getProperty("mapName");
/* 233 */     String mapDescription = mapListProps.getProperty("mapDescription");
/*     */ 
/* 236 */     reloadMapList();
/* 237 */     if (this.m_mapListSet.findRow(this.m_mapListNameIndex, mapName) != null)
/*     */     {
/* 239 */       String msg = LocaleResources.getString("csSpiderMapNameExists", this.m_cxt, mapName);
/* 240 */       reportError(msg);
/* 241 */       return;
/*     */     }
/*     */ 
/* 245 */     Vector mapRow = new IdcVector();
/* 246 */     mapRow.setSize(this.m_mapListFields.length);
/* 247 */     mapRow.setElementAt(mapName, this.m_mapListNameIndex);
/* 248 */     mapRow.setElementAt(mapDescription, this.m_mapListDescIndex);
/* 249 */     this.m_mapListSet.addRow(mapRow);
/* 250 */     saveMapList();
/*     */ 
/* 252 */     refreshDialog(mapName);
/*     */ 
/* 254 */     editMap(mapListProps);
/*     */   }
/*     */ 
/*     */   protected void editMap(Properties mapListProps)
/*     */     throws DataException, ServiceException
/*     */   {
/* 261 */     if (mapListProps == null)
/*     */     {
/* 263 */       int selectedIndex = this.m_mapList.getSelectedIndex();
/* 264 */       if (selectedIndex < 0)
/*     */       {
/* 266 */         String msg = LocaleResources.getString("csSpiderSelectMapEdit", this.m_cxt);
/* 267 */         reportError(msg);
/* 268 */         return;
/*     */       }
/* 270 */       mapListProps = this.m_mapList.getDataAt(selectedIndex);
/*     */     }
/*     */ 
/* 274 */     String mapName = mapListProps.getProperty("mapName");
/* 275 */     DataBinder mapBinder = new DataBinder();
/* 276 */     ResourceUtils.serializeDataBinder(this.m_mapDir, mapName + ".hda", mapBinder, false, false);
/* 277 */     DataResultSet mapSet = (DataResultSet)mapBinder.getResultSet(this.m_mapTableName);
/* 278 */     if (mapSet == null)
/*     */     {
/* 280 */       mapSet = new DataResultSet(this.m_mapFields);
/* 281 */       mapBinder.addResultSet(this.m_mapTableName, mapSet);
/*     */     }
/*     */ 
/* 284 */     SpiderEditMapDialog dlg = new SpiderEditMapDialog(this.m_sysInterface);
/* 285 */     dlg.init(mapListProps, mapSet);
/* 286 */     if (dlg.prompt() != 1)
/*     */       return;
/* 288 */     reloadMapList();
/*     */ 
/* 291 */     mapName = mapListProps.getProperty("mapName");
/* 292 */     Vector mapListRow = this.m_mapListSet.findRow(this.m_mapListNameIndex, mapName);
/* 293 */     if (mapListRow == null)
/*     */     {
/* 295 */       reportError(LocaleResources.getString("csSpiderMapNameDeleted", this.m_cxt, mapName));
/* 296 */       return;
/*     */     }
/* 298 */     mapListRow.setElementAt(mapName, this.m_mapListNameIndex);
/* 299 */     mapListRow.setElementAt(mapListProps.getProperty("mapDescription"), this.m_mapListDescIndex);
/*     */ 
/* 302 */     saveMapList();
/*     */ 
/* 305 */     ResourceUtils.serializeDataBinder(this.m_mapDir, mapName + ".hda", mapBinder, true, false);
/*     */ 
/* 307 */     refreshDialog(mapName);
/*     */   }
/*     */ 
/*     */   public void deleteMap()
/*     */     throws DataException, ServiceException
/*     */   {
/* 313 */     int selectedIndex = this.m_mapList.getSelectedIndex();
/* 314 */     if (selectedIndex < 0)
/*     */     {
/* 316 */       String msg = LocaleResources.getString("csSpiderSelectMapDelete", this.m_cxt);
/* 317 */       reportError(msg);
/* 318 */       return;
/*     */     }
/*     */ 
/* 321 */     Properties mapListProps = this.m_mapList.getDataAt(selectedIndex);
/* 322 */     String mapName = mapListProps.getProperty("mapName");
/* 323 */     if (mapName == null)
/*     */     {
/* 325 */       return;
/*     */     }
/*     */ 
/* 328 */     IdcMessage deleteMsg = IdcMessageFactory.lc("csSpiderPromptMapDelete", new Object[] { mapName });
/* 329 */     if (MessageBox.doMessage(this.m_sysInterface, deleteMsg, 4) != 2)
/*     */       return;
/* 331 */     reloadMapList();
/*     */ 
/* 334 */     for (this.m_mapListSet.first(); this.m_mapListSet.isRowPresent(); this.m_mapListSet.next())
/*     */     {
/* 336 */       String curMapName = this.m_mapListSet.getStringValue(this.m_mapListNameIndex);
/* 337 */       if (!curMapName.equals(mapName))
/*     */         continue;
/* 339 */       this.m_mapListSet.deleteCurrentRow();
/* 340 */       break;
/*     */     }
/*     */ 
/* 344 */     saveMapList();
/*     */ 
/* 347 */     FileUtils.deleteFile(this.m_mapDir + mapName + ".hda");
/*     */ 
/* 349 */     refreshDialog(null);
/*     */   }
/*     */ 
/*     */   protected void reloadMapList()
/*     */     throws ServiceException
/*     */   {
/* 355 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_mapListFileName, this.m_mapListBinder, false, false);
/* 356 */     this.m_mapListSet = ((DataResultSet)this.m_mapListBinder.getResultSet(this.m_mapListTableName));
/* 357 */     if (this.m_mapListSet != null)
/*     */       return;
/* 359 */     this.m_mapListSet = new DataResultSet(this.m_mapListFields);
/*     */   }
/*     */ 
/*     */   protected void saveMapList()
/*     */     throws ServiceException
/*     */   {
/* 365 */     ResourceUtils.serializeDataBinder(this.m_mapDir, this.m_mapListFileName, this.m_mapListBinder, true, false);
/*     */   }
/*     */ 
/*     */   protected void refreshDialog(String mapName)
/*     */   {
/* 370 */     int selectedIndex = this.m_mapList.refreshList(this.m_mapListBinder, mapName);
/* 371 */     this.m_mapList.setSort(this.m_nameInfo, false);
/* 372 */     this.m_mapList.enableDisable(selectedIndex >= 0);
/*     */ 
/* 374 */     int numRows = this.m_mapList.getNumRows();
/* 375 */     this.m_editBtn.setEnabled(numRows > 0);
/* 376 */     this.m_deleteBtn.setEnabled(numRows > 0);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 381 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(String msg)
/*     */   {
/* 388 */     MessageBox.reportError(this.m_sysInterface.getMainWindow(), msg, LocaleResources.getString("csSpiderMapListTitle", this.m_cxt));
/*     */   }
/*     */ 
/*     */   public void reportError(IdcMessage msg)
/*     */   {
/* 394 */     MessageBox.reportError(this.m_sysInterface, this.m_sysInterface.getMainWindow(), msg, IdcMessageFactory.lc("csSpiderMapListTitle", new Object[0]));
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 401 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.batchloader.SpiderMapListDialog
 * JD-Core Version:    0.5.4
 */