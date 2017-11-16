/*     */ package intradoc.apputilities.batchloader;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
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
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SpiderCollectionListDialog
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_sysInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected UdlPanel m_collectionList;
/*     */   protected JButton m_editBtn;
/*     */   protected JButton m_deleteBtn;
/*  68 */   protected ColumnInfo m_idInfo = null;
/*     */ 
/*  71 */   protected DataBinder m_dataBinder = null;
/*  72 */   protected DataResultSet m_resultSet = null;
/*     */ 
/*  74 */   protected String m_searchDir = null;
/*  75 */   protected String m_fileName = "search_collections.hda";
/*  76 */   protected String m_tableName = "SearchCollections";
/*  77 */   protected String[] m_fields = { "sCollectionID", "sDescription", "sProfile", "sLocation", "sFlag", "sUrlScript", "sPhysicalFileRoot", "sRelativeWebRoot", "sProperties" };
/*     */ 
/*  80 */   protected String[] m_externalFields = { "sPhysicalFileRoot", "sRelativeWebRoot", "sProperties" };
/*     */ 
/*  82 */   protected FieldInfo[] m_fieldInfo = null;
/*     */ 
/*     */   public SpiderCollectionListDialog(SystemInterface sys)
/*     */   {
/*  86 */     this.m_cxt = sys.getExecutionContext();
/*  87 */     this.m_helper = new DialogHelper(sys, LocaleResources.getString("csSpiderCollectionListDialogTitle", this.m_cxt), true);
/*     */ 
/*  89 */     this.m_sysInterface = sys;
/*  90 */     this.m_searchDir = (LegacyDirectoryLocator.getAppDataDirectory() + "search/");
/*  91 */     this.m_dataBinder = new DataBinder();
/*     */   }
/*     */ 
/*     */   public void init() throws DataException, ServiceException
/*     */   {
/*  96 */     initUI();
/*  97 */     reloadCollectionList();
/*  98 */     refreshDialog(null);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/* 103 */     JPanel collectionPanel = initCollectionPanel();
/* 104 */     initCollectionList(collectionPanel);
/* 105 */     initButtonPanel();
/*     */   }
/*     */ 
/*     */   protected JPanel initCollectionPanel()
/*     */   {
/* 110 */     JPanel collectionPanel = new PanePanel();
/* 111 */     collectionPanel.setLayout(new GridBagLayout());
/* 112 */     this.m_helper.makePanelGridBag(this.m_helper.m_mainPanel, 1);
/* 113 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 114 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 115 */     this.m_helper.addLastComponentInRow(this.m_helper.m_mainPanel, collectionPanel);
/*     */ 
/* 117 */     return collectionPanel;
/*     */   }
/*     */ 
/*     */   protected void initCollectionList(JPanel collectionPanel)
/*     */   {
/* 122 */     this.m_collectionList = new UdlPanel(LocaleResources.getString("csSpiderCollectionListDialogTitle", this.m_cxt), null, 400, 10, this.m_tableName, true);
/*     */ 
/* 126 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("csSpiderCollectionIDTitle", this.m_cxt), "sCollectionID", 25.0D);
/*     */ 
/* 128 */     this.m_collectionList.setColumnInfo(info);
/* 129 */     this.m_idInfo = info;
/*     */ 
/* 131 */     info = new ColumnInfo(LocaleResources.getString("csSpiderMapDescTitle", this.m_cxt), "sDescription", 50.0D);
/*     */ 
/* 133 */     this.m_collectionList.setColumnInfo(info);
/*     */ 
/* 135 */     info = new ColumnInfo(LocaleResources.getString("csCompWizLabelStatus", this.m_cxt), "sFlag", 25.0D);
/*     */ 
/* 137 */     this.m_collectionList.setColumnInfo(info);
/*     */ 
/* 139 */     this.m_collectionList.setVisibleColumns("sCollectionID,sDescription,sFlag");
/* 140 */     this.m_collectionList.setIDColumn("sCollectionID");
/* 141 */     this.m_collectionList.init();
/*     */ 
/* 143 */     this.m_helper.makePanelGridBag(this.m_helper.m_mainPanel, 1);
/* 144 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 30, 5, 30);
/* 145 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 146 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 147 */     this.m_helper.addLastComponentInRow(collectionPanel, this.m_collectionList);
/*     */   }
/*     */ 
/*     */   protected void initButtonPanel()
/*     */   {
/* 153 */     JButton addBtn = new JButton(LocaleResources.getString("csButtonAddLocale", this.m_cxt));
/* 154 */     ActionListener addListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/*     */         try
/*     */         {
/* 160 */           SpiderCollectionListDialog.this.addOrEditCollection(true);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 164 */           SpiderCollectionListDialog.this.reportError(e.getMessage());
/*     */         }
/*     */       }
/*     */     };
/* 168 */     addBtn.addActionListener(addListener);
/*     */ 
/* 171 */     this.m_editBtn = new JButton(LocaleResources.getString("csButtonEditLocale", this.m_cxt));
/* 172 */     ActionListener editListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/*     */         try
/*     */         {
/* 178 */           SpiderCollectionListDialog.this.addOrEditCollection(false);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 182 */           SpiderCollectionListDialog.this.reportError(e.getMessage());
/*     */         }
/*     */       }
/*     */     };
/* 186 */     this.m_editBtn.addActionListener(editListener);
/*     */ 
/* 189 */     this.m_deleteBtn = new JButton(LocaleResources.getString("csButtonDeleteLocale", this.m_cxt));
/* 190 */     ActionListener deleteListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/*     */         try
/*     */         {
/* 196 */           SpiderCollectionListDialog.this.deleteCollection();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 200 */           SpiderCollectionListDialog.this.reportError(e.getMessage());
/*     */         }
/*     */       }
/*     */     };
/* 204 */     this.m_deleteBtn.addActionListener(deleteListener);
/*     */ 
/* 207 */     JButton closeBtn = new JButton(LocaleResources.getString("csSpiderCloseButton", this.m_cxt));
/* 208 */     ActionListener closeListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/* 212 */         SpiderCollectionListDialog.this.m_helper.close();
/*     */       }
/*     */     };
/* 215 */     closeBtn.addActionListener(closeListener);
/*     */ 
/* 218 */     JPanel buttonPanel = new PanePanel();
/* 219 */     buttonPanel.setLayout(new FlowLayout());
/* 220 */     buttonPanel.add(addBtn);
/* 221 */     buttonPanel.add(this.m_editBtn);
/* 222 */     buttonPanel.add(this.m_deleteBtn);
/* 223 */     buttonPanel.add(closeBtn);
/*     */ 
/* 226 */     this.m_collectionList.add("South", buttonPanel);
/*     */   }
/*     */ 
/*     */   protected void addOrEditCollection(boolean isAdd) throws DataException, ServiceException
/*     */   {
/* 231 */     Properties props = null;
/* 232 */     if (isAdd)
/*     */     {
/* 234 */       props = new Properties();
/*     */     }
/*     */     else
/*     */     {
/* 238 */       int selectedIndex = this.m_collectionList.getSelectedIndex();
/* 239 */       if (selectedIndex < 0)
/*     */       {
/* 241 */         String msg = LocaleResources.getString("csSpiderSelectCollectionEdit", this.m_cxt);
/* 242 */         reportError(msg);
/* 243 */         return;
/*     */       }
/* 245 */       props = this.m_collectionList.getDataAt(selectedIndex);
/*     */     }
/*     */ 
/* 248 */     SpiderEditCollectionDialog dlg = new SpiderEditCollectionDialog(this.m_sysInterface);
/* 249 */     dlg.init(props, true);
/* 250 */     if (dlg.prompt() != 1) {
/*     */       return;
/*     */     }
/* 253 */     boolean verityLocaleExists = false;
/* 254 */     FieldInfo verityLocaleInfo = new FieldInfo();
/* 255 */     verityLocaleExists = this.m_resultSet.getFieldInfo("sVerityLocale", verityLocaleInfo);
/*     */ 
/* 257 */     String collectionID = props.getProperty("sCollectionID");
/*     */ 
/* 259 */     reloadCollectionList();
/* 260 */     Vector row = this.m_resultSet.findRow(this.m_fieldInfo[0].m_index, collectionID);
/* 261 */     if (isAdd)
/*     */     {
/* 263 */       if (row != null)
/*     */       {
/* 265 */         String msg = LocaleResources.getString("csSpiderCollectionNameExists", this.m_cxt, collectionID);
/*     */ 
/* 267 */         reportError(msg);
/* 268 */         return;
/*     */       }
/* 270 */       row = new IdcVector();
/* 271 */       if (verityLocaleExists)
/*     */       {
/* 273 */         row.setSize(this.m_fields.length + 1);
/*     */       }
/*     */       else
/*     */       {
/* 277 */         row.setSize(this.m_fields.length);
/*     */       }
/*     */ 
/*     */     }
/* 282 */     else if (row == null)
/*     */     {
/* 284 */       String msg = LocaleResources.getString("csSpiderCollectionDeleted", this.m_cxt, collectionID);
/*     */ 
/* 286 */       reportError(msg);
/* 287 */       return;
/*     */     }
/*     */ 
/* 291 */     row.setElementAt(collectionID, this.m_fieldInfo[0].m_index);
/* 292 */     row.setElementAt(props.getProperty("sDescription"), this.m_fieldInfo[1].m_index);
/* 293 */     row.setElementAt("external", this.m_fieldInfo[2].m_index);
/* 294 */     row.setElementAt(props.getProperty("sLocation"), this.m_fieldInfo[3].m_index);
/* 295 */     row.setElementAt(props.getProperty("sFlag"), this.m_fieldInfo[4].m_index);
/* 296 */     row.setElementAt("<$URL$>", this.m_fieldInfo[5].m_index);
/* 297 */     row.setElementAt(props.getProperty("sPhysicalFileRoot"), this.m_fieldInfo[6].m_index);
/* 298 */     row.setElementAt(props.getProperty("sRelativeWebRoot"), this.m_fieldInfo[7].m_index);
/* 299 */     row.setElementAt(props.getProperty("sProperties"), this.m_fieldInfo[8].m_index);
/*     */ 
/* 301 */     if (verityLocaleExists)
/*     */     {
/* 303 */       row.setElementAt("englishx", verityLocaleInfo.m_index);
/*     */     }
/*     */ 
/* 306 */     if (isAdd)
/*     */     {
/* 308 */       this.m_resultSet.addRow(row);
/*     */     }
/*     */ 
/* 311 */     saveCollectionList();
/* 312 */     refreshDialog(collectionID);
/*     */   }
/*     */ 
/*     */   public void deleteCollection()
/*     */     throws DataException, ServiceException
/*     */   {
/* 318 */     int selectedIndex = this.m_collectionList.getSelectedIndex();
/* 319 */     if (selectedIndex < 0)
/*     */     {
/* 321 */       String msg = LocaleResources.getString("csSpiderSelectCollectionDelete", this.m_cxt);
/* 322 */       reportError(msg);
/* 323 */       return;
/*     */     }
/*     */ 
/* 326 */     Properties props = this.m_collectionList.getDataAt(selectedIndex);
/* 327 */     String collectionID = props.getProperty("sCollectionID");
/* 328 */     if (collectionID == null)
/*     */     {
/* 330 */       return;
/*     */     }
/*     */ 
/* 333 */     IdcMessage deleteMsg = IdcMessageFactory.lc("csSpiderPromptCollectionDelete", new Object[] { collectionID });
/* 334 */     if (MessageBox.doMessage(this.m_sysInterface, deleteMsg, 4) != 2)
/*     */       return;
/* 336 */     reloadCollectionList();
/*     */ 
/* 339 */     for (this.m_resultSet.first(); this.m_resultSet.isRowPresent(); this.m_resultSet.next())
/*     */     {
/* 341 */       String curCollection = this.m_resultSet.getStringValue(this.m_fieldInfo[0].m_index);
/* 342 */       if (!curCollection.equals(collectionID))
/*     */         continue;
/* 344 */       this.m_resultSet.deleteCurrentRow();
/* 345 */       break;
/*     */     }
/*     */ 
/* 349 */     saveCollectionList();
/*     */ 
/* 352 */     deleteFromActiveIndex(collectionID);
/*     */ 
/* 354 */     refreshDialog(null);
/*     */   }
/*     */ 
/*     */   protected void reloadCollectionList()
/*     */     throws DataException, ServiceException
/*     */   {
/* 360 */     ResourceUtils.serializeDataBinder(this.m_searchDir, this.m_fileName, this.m_dataBinder, false, false);
/*     */ 
/* 362 */     DataResultSet drset = (DataResultSet)this.m_dataBinder.getResultSet(this.m_tableName);
/* 363 */     if (drset == null)
/*     */     {
/* 365 */       drset = new DataResultSet(this.m_fields);
/*     */     }
/*     */     else
/*     */     {
/* 370 */       DataResultSet mergeSet = new DataResultSet(this.m_externalFields);
/* 371 */       drset.mergeFields(mergeSet);
/*     */ 
/* 373 */       this.m_resultSet = new DataResultSet();
/* 374 */       this.m_resultSet.copySimpleFiltered(drset, "sProfile", "external");
/*     */ 
/* 376 */       this.m_dataBinder.addResultSet(this.m_tableName, this.m_resultSet);
/*     */     }
/* 378 */     this.m_fieldInfo = ResultSetUtils.createInfoList(drset, this.m_fields, true);
/*     */   }
/*     */ 
/*     */   protected void saveCollectionList() throws ServiceException
/*     */   {
/* 383 */     File searchFile = FileUtilsCfgBuilder.getCfgFile(this.m_searchDir, null, true);
/* 384 */     if (!searchFile.exists())
/*     */     {
/* 386 */       FileUtils.checkOrCreateDirectory(this.m_searchDir, 2);
/*     */     }
/*     */ 
/* 389 */     ResourceUtils.serializeDataBinder(this.m_searchDir, this.m_fileName, this.m_dataBinder, true, false);
/*     */   }
/*     */ 
/*     */   protected void refreshDialog(String collectionID)
/*     */   {
/* 394 */     int selectedIndex = this.m_collectionList.refreshList(this.m_dataBinder, collectionID);
/* 395 */     this.m_collectionList.setSort(this.m_idInfo, false);
/* 396 */     this.m_collectionList.enableDisable(selectedIndex >= 0);
/*     */ 
/* 398 */     int numRows = this.m_collectionList.getNumRows();
/* 399 */     this.m_editBtn.setEnabled(numRows > 0);
/* 400 */     this.m_deleteBtn.setEnabled(numRows > 0);
/*     */   }
/*     */ 
/*     */   protected void deleteFromActiveIndex(String collectionID)
/*     */     throws DataException, ServiceException
/*     */   {
/* 406 */     String searchDir = LegacyDirectoryLocator.getSearchDirectory();
/*     */     try
/*     */     {
/* 410 */       FileUtils.reserveDirectory(searchDir);
/*     */ 
/* 412 */       DataBinder data = new DataBinder();
/* 413 */       ResourceUtils.serializeDataBinder(searchDir, "activeindex.hda", data, false, false);
/*     */ 
/* 415 */       DataResultSet drset = (DataResultSet)data.getResultSet("SearchCollections");
/* 416 */       if (drset != null)
/*     */       {
/* 419 */         int index = ResultSetUtils.getIndexMustExist(drset, "sCollectionID");
/* 420 */         if (drset.findRow(index, collectionID) != null)
/*     */         {
/* 422 */           drset.deleteCurrentRow();
/*     */         }
/*     */ 
/* 426 */         ResourceUtils.serializeDataBinder(searchDir, "activeindex.hda", data, true, false);
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 433 */       FileUtils.releaseDirectory(searchDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 439 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(String msg)
/*     */   {
/* 446 */     MessageBox.reportError(this.m_sysInterface.getMainWindow(), msg, LocaleResources.getString("csSpiderCollectionListDialogTitle", this.m_cxt));
/*     */   }
/*     */ 
/*     */   public void reportError(IdcMessage msg)
/*     */   {
/* 452 */     MessageBox.reportError(this.m_sysInterface, this.m_sysInterface.getMainWindow(), msg, IdcMessageFactory.lc("csSpiderCollectionListDialogTitle", new Object[0]));
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 459 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.batchloader.SpiderCollectionListDialog
 * JD-Core Version:    0.5.4
 */