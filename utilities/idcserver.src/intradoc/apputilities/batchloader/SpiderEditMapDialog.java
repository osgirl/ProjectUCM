/*     */ package intradoc.apputilities.batchloader;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
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
/*     */ public class SpiderEditMapDialog
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_sysInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected UdlPanel m_fieldList;
/*     */   protected JButton m_editBtn;
/*     */   protected JButton m_deleteBtn;
/*  71 */   protected DataBinder m_mapBinder = null;
/*  72 */   protected DataResultSet m_mapSet = null;
/*     */ 
/*  74 */   protected String m_mapDir = null;
/*  75 */   protected String m_mapListFileName = "mapping.hda";
/*  76 */   protected String m_mapDefaultFileName = "default.hda";
/*  77 */   protected String m_mapListTableName = "SpiderMappingList";
/*  78 */   protected String m_mapTableName = "SpiderMapping";
/*  79 */   protected String[] m_mapListFields = { "mapName", "mapDescription" };
/*  80 */   protected String[] m_mapFields = { "mapField", "mapValue" };
/*  81 */   protected int m_mapFieldIndex = -1;
/*  82 */   protected int m_mapValueIndex = -1;
/*     */ 
/*     */   public SpiderEditMapDialog(SystemInterface sys)
/*     */   {
/*  86 */     this.m_cxt = sys.getExecutionContext();
/*  87 */     this.m_helper = new DialogHelper(sys, LocaleResources.getString("csSpiderEditMapDialogTitle", this.m_cxt), true);
/*     */ 
/*  89 */     this.m_sysInterface = sys;
/*  90 */     this.m_mapDir = (LegacyDirectoryLocator.getAppDataDirectory() + "search/external/mapping/");
/*     */   }
/*     */ 
/*     */   public void init(Properties mapProps, DataResultSet mapSet)
/*     */     throws DataException, ServiceException
/*     */   {
/*  96 */     this.m_helper.m_props = mapProps;
/*  97 */     this.m_mapSet = mapSet;
/*     */ 
/*  99 */     initUI();
/* 100 */     refreshDialog(null);
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/* 105 */     this.m_helper.initStandard(this, null, 2, false, null);
/* 106 */     JPanel mapPanel = initMapPanel();
/* 107 */     initMapFields(mapPanel);
/* 108 */     initFieldList(mapPanel);
/* 109 */     initButtonPanel();
/*     */   }
/*     */ 
/*     */   public JPanel initMapPanel()
/*     */   {
/* 114 */     JPanel mapPanel = new PanePanel();
/* 115 */     mapPanel.setLayout(new GridBagLayout());
/* 116 */     this.m_helper.makePanelGridBag(this.m_helper.m_mainPanel, 1);
/* 117 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 118 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 119 */     this.m_helper.addLastComponentInRow(this.m_helper.m_mainPanel, mapPanel);
/*     */ 
/* 121 */     return mapPanel;
/*     */   }
/*     */ 
/*     */   public void initMapFields(JPanel mapPanel)
/*     */   {
/* 127 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 128 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 129 */     this.m_helper.addComponent(mapPanel, new CustomLabel(LocaleResources.getString("csSpiderMapNameLabel", this.m_cxt), 1));
/*     */ 
/* 132 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 133 */     this.m_helper.addExchangeComponent(mapPanel, new CustomLabel("", 0), "mapName");
/*     */ 
/* 137 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 138 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 139 */     this.m_helper.addComponent(mapPanel, new CustomLabel(LocaleResources.getString("csSpiderMapDescLabel", this.m_cxt), 1));
/*     */ 
/* 142 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 143 */     this.m_helper.addExchangeComponent(mapPanel, new CustomTextField(40), "mapDescription");
/*     */   }
/*     */ 
/*     */   public void initFieldList(JPanel mapPanel)
/*     */   {
/* 148 */     this.m_fieldList = new UdlPanel(this.m_sysInterface.getString("csSpiderMappingTitle"), null, 400, 10, this.m_mapTableName, true);
/*     */ 
/* 152 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("csSpiderMapFieldTitle", this.m_cxt), "mapField", 10.0D);
/*     */ 
/* 154 */     this.m_fieldList.setColumnInfo(info);
/*     */ 
/* 156 */     info = new ColumnInfo(LocaleResources.getString("csSpiderMapValueTitle", this.m_cxt), "mapValue", 10.0D);
/*     */ 
/* 158 */     this.m_fieldList.setColumnInfo(info);
/*     */ 
/* 160 */     this.m_fieldList.setVisibleColumns("mapField,mapValue");
/* 161 */     this.m_fieldList.setIDColumn("mapField");
/* 162 */     this.m_fieldList.init();
/*     */ 
/* 164 */     JPanel listWrapper = new CustomPanel();
/* 165 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 166 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 167 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 168 */     this.m_helper.addComponent(mapPanel, listWrapper);
/*     */ 
/* 170 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 30, 5, 30);
/* 171 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 172 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 173 */     this.m_helper.addLastComponentInRow(listWrapper, this.m_fieldList);
/*     */   }
/*     */ 
/*     */   public void initButtonPanel()
/*     */   {
/* 179 */     JButton addBtn = new JButton(LocaleResources.getString("csButtonAddLocale", this.m_cxt));
/* 180 */     ActionListener addListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/*     */         try
/*     */         {
/* 186 */           SpiderEditMapDialog.this.addOrEditField(true);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 190 */           SpiderEditMapDialog.this.reportError(e.getMessage());
/*     */         }
/*     */       }
/*     */     };
/* 194 */     addBtn.addActionListener(addListener);
/*     */ 
/* 197 */     this.m_editBtn = new JButton(LocaleResources.getString("csButtonEditLocale", this.m_cxt));
/* 198 */     ActionListener editListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/*     */         try
/*     */         {
/* 204 */           SpiderEditMapDialog.this.addOrEditField(false);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 208 */           SpiderEditMapDialog.this.reportError(e.getMessage());
/*     */         }
/*     */       }
/*     */     };
/* 212 */     this.m_editBtn.addActionListener(editListener);
/*     */ 
/* 215 */     this.m_deleteBtn = new JButton(LocaleResources.getString("csButtonDeleteLocale", this.m_cxt));
/* 216 */     ActionListener deleteListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/*     */         try
/*     */         {
/* 222 */           SpiderEditMapDialog.this.deleteField();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 226 */           SpiderEditMapDialog.this.reportError(e.getMessage());
/*     */         }
/*     */       }
/*     */     };
/* 230 */     this.m_deleteBtn.addActionListener(deleteListener);
/*     */ 
/* 233 */     JPanel buttonPanel = new PanePanel();
/* 234 */     buttonPanel.setLayout(new FlowLayout());
/* 235 */     buttonPanel.add(addBtn);
/* 236 */     buttonPanel.add(this.m_editBtn);
/* 237 */     buttonPanel.add(this.m_deleteBtn);
/*     */ 
/* 240 */     this.m_fieldList.add("South", buttonPanel);
/*     */   }
/*     */ 
/*     */   public void addOrEditField(boolean isAdd) throws DataException
/*     */   {
/* 245 */     Properties fieldProps = null;
/* 246 */     if (isAdd)
/*     */     {
/* 248 */       fieldProps = new Properties();
/*     */     }
/*     */     else
/*     */     {
/* 252 */       int selectedIndex = this.m_fieldList.getSelectedIndex();
/* 253 */       if (selectedIndex < 0)
/*     */       {
/* 255 */         String msg = LocaleResources.getString("csSpiderSelectFieldEdit", this.m_cxt);
/* 256 */         reportError(msg);
/* 257 */         return;
/*     */       }
/* 259 */       fieldProps = this.m_fieldList.getDataAt(selectedIndex);
/*     */     }
/* 261 */     SpiderEditFieldDialog dlg = new SpiderEditFieldDialog(this.m_sysInterface);
/* 262 */     dlg.init(isAdd, fieldProps, this.m_mapSet);
/* 263 */     if (dlg.prompt() != 1)
/*     */       return;
/* 265 */     String mapField = fieldProps.getProperty("mapField");
/* 266 */     String mapValue = fieldProps.getProperty("mapValue");
/*     */ 
/* 268 */     Vector fieldRow = null;
/* 269 */     if (isAdd)
/*     */     {
/* 271 */       fieldRow = new IdcVector();
/* 272 */       fieldRow.setSize(this.m_mapFields.length);
/* 273 */       this.m_mapSet.addRow(fieldRow);
/*     */     }
/*     */     else
/*     */     {
/* 277 */       fieldRow = this.m_mapSet.findRow(this.m_mapFieldIndex, mapField);
/*     */     }
/* 279 */     fieldRow.setElementAt(mapField, this.m_mapFieldIndex);
/* 280 */     fieldRow.setElementAt(mapValue, this.m_mapValueIndex);
/*     */ 
/* 282 */     refreshDialog(mapField);
/*     */   }
/*     */ 
/*     */   public void deleteField()
/*     */     throws DataException
/*     */   {
/* 288 */     int selectedIndex = this.m_fieldList.getSelectedIndex();
/* 289 */     if (selectedIndex < 0)
/*     */     {
/* 291 */       IdcMessage msg = IdcMessageFactory.lc("csSpiderSelectFieldDelete", new Object[0]);
/* 292 */       MessageBox.doMessage(this.m_sysInterface, msg, 1);
/* 293 */       return;
/*     */     }
/*     */ 
/* 296 */     Properties mapProps = this.m_fieldList.getDataAt(selectedIndex);
/* 297 */     String mapField = mapProps.getProperty("mapField");
/* 298 */     if (mapField == null)
/*     */     {
/* 300 */       return;
/*     */     }
/*     */ 
/* 303 */     IdcMessage deleteMsg = IdcMessageFactory.lc("csSpiderPromptFieldDelete", new Object[] { mapField });
/* 304 */     if (MessageBox.doMessage(this.m_sysInterface, deleteMsg, 4) != 2) {
/*     */       return;
/*     */     }
/* 307 */     for (this.m_mapSet.first(); this.m_mapSet.isRowPresent(); this.m_mapSet.next())
/*     */     {
/* 309 */       String curField = this.m_mapSet.getStringValue(this.m_mapFieldIndex);
/* 310 */       if (!curField.equals(mapField))
/*     */         continue;
/* 312 */       this.m_mapSet.deleteCurrentRow();
/*     */     }
/*     */ 
/* 315 */     refreshDialog(null);
/*     */   }
/*     */ 
/*     */   public void refreshDialog(String mapField)
/*     */     throws DataException
/*     */   {
/* 321 */     if (this.m_mapBinder == null)
/*     */     {
/* 323 */       this.m_mapBinder = new DataBinder();
/* 324 */       this.m_mapBinder.addResultSet(this.m_mapTableName, this.m_mapSet);
/*     */ 
/* 326 */       this.m_mapFieldIndex = ResultSetUtils.getIndexMustExist(this.m_mapSet, "mapField");
/* 327 */       this.m_mapValueIndex = ResultSetUtils.getIndexMustExist(this.m_mapSet, "mapValue");
/*     */     }
/*     */ 
/* 330 */     int selectedIndex = this.m_fieldList.refreshList(this.m_mapBinder, mapField);
/* 331 */     this.m_fieldList.enableDisable(selectedIndex >= 0);
/*     */ 
/* 333 */     int numRows = this.m_fieldList.getNumRows();
/* 334 */     this.m_editBtn.setEnabled(numRows > 0);
/* 335 */     this.m_deleteBtn.setEnabled(numRows > 0);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(String msg)
/*     */   {
/* 342 */     MessageBox.reportError(this.m_sysInterface.getMainWindow(), msg, LocaleResources.getString("csSpiderEditMapTitle", this.m_cxt));
/*     */   }
/*     */ 
/*     */   public void reportError(IdcMessage msg)
/*     */   {
/* 348 */     MessageBox.reportError(this.m_sysInterface, this.m_sysInterface.getMainWindow(), msg, IdcMessageFactory.lc("csSpiderEditMapTitle", new Object[0]));
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 354 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 361 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 366 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 371 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.batchloader.SpiderEditMapDialog
 * JD-Core Version:    0.5.4
 */