/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.ExportQueryData;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.QueryBuilderHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditQueryDlg
/*     */   implements ComponentBinder
/*     */ {
/*  76 */   protected CollectionContext m_collectionContext = null;
/*  77 */   protected SystemInterface m_systemInterface = null;
/*  78 */   protected ExecutionContext m_cxt = null;
/*  79 */   protected DialogHelper m_helper = null;
/*     */ 
/*  81 */   protected QueryBuilderHelper m_queryHelper = null;
/*     */   protected DocumentLocalizedProfile m_docProfile;
/*  84 */   protected boolean m_isExportTables = false;
/*  85 */   protected ViewFields m_schemaFields = null;
/*     */ 
/*  87 */   protected final String EDIT_ITEMS = "aExportQuery";
/*     */ 
/*  89 */   protected String m_createCol = null;
/*  90 */   protected String m_modifiedCol = null;
/*  91 */   protected String m_curTableName = null;
/*     */ 
/*     */   public EditQueryDlg(SystemInterface sys, String title, CollectionContext context)
/*     */   {
/*  95 */     this.m_collectionContext = context;
/*  96 */     this.m_systemInterface = sys;
/*  97 */     this.m_cxt = sys.getExecutionContext();
/*  98 */     this.m_helper = new DialogHelper(sys, title, true);
/*     */ 
/* 100 */     UserData userData = AppLauncher.getUserData();
/* 101 */     this.m_docProfile = new DocumentLocalizedProfile(userData, 1, this.m_cxt);
/*     */   }
/*     */ 
/*     */   public int init(Properties props)
/*     */   {
/* 106 */     boolean hasError = false;
/* 107 */     this.m_helper.m_props = props;
/*     */ 
/* 109 */     this.m_curTableName = props.getProperty("currentTable");
/* 110 */     this.m_isExportTables = (this.m_curTableName != null);
/*     */ 
/* 112 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 118 */         EditQueryDlg.this.m_queryHelper.getFormatString();
/*     */         try
/*     */         {
/* 121 */           Properties editData = EditQueryDlg.this.m_helper.m_props;
/* 122 */           editData.put("EditItems", "aExportQuery");
/* 123 */           String str = EditQueryDlg.this.m_queryHelper.getFormatString();
/* 124 */           if (EditQueryDlg.this.m_isExportTables)
/*     */           {
/* 126 */             String tableList = EditQueryDlg.this.m_helper.m_props.getProperty("aExportTables");
/* 127 */             if (tableList != null)
/*     */             {
/* 129 */               EditQueryDlg.this.m_helper.m_props.put("aExportTables", tableList);
/*     */             }
/* 131 */             Vector tables = StringUtils.parseArray(tableList, ',', ',');
/* 132 */             String editItems = "aExportTables";
/* 133 */             int size = tables.size();
/* 134 */             for (int i = 0; i < size; ++i)
/*     */             {
/* 136 */               String table = (String)tables.elementAt(i);
/* 137 */               String itemKey = "aExportTable" + table;
/* 138 */               editItems = editItems + "," + itemKey;
/* 139 */               String value = EditQueryDlg.this.m_helper.m_props.getProperty(itemKey);
/* 140 */               if (value == null)
/*     */               {
/* 142 */                 value = "";
/*     */               }
/* 144 */               EditQueryDlg.this.m_helper.m_props.put(itemKey, value);
/*     */             }
/* 146 */             editData.put("EditItems", editItems);
/* 147 */             editData.put("aExportTable" + EditQueryDlg.this.m_curTableName, str);
/*     */           }
/*     */           else
/*     */           {
/* 151 */             editData.put("aExportQuery", str);
/*     */           }
/*     */ 
/* 154 */           SharedContext shContext = EditQueryDlg.this.m_collectionContext.getSharedContext();
/* 155 */           AppContextUtils.executeService(shContext, "EDIT_ARCHIVEDATA", editData, true);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 159 */           MessageBox.reportError(EditQueryDlg.this.m_systemInterface, exp, IdcMessageFactory.lc("apErrorEditingExportQuery", new Object[0]));
/*     */ 
/* 161 */           return false;
/*     */         }
/* 163 */         return true;
/*     */       }
/*     */     };
/* 166 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, DialogHelpTable.getHelpPage("EditExportQuery"));
/*     */ 
/* 170 */     JPanel queryClauses = new CustomPanel();
/* 171 */     JPanel queryOptions = new CustomPanel();
/*     */ 
/* 173 */     JPanel pnl = new PanePanel(false);
/*     */ 
/* 175 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 176 */     gh.m_gc.weightx = 1.0D;
/* 177 */     gh.m_gc.weighty = 1.0D;
/* 178 */     this.m_helper.addLastComponentInRow(mainPanel, pnl);
/* 179 */     pnl.setLayout(new BorderLayout());
/*     */ 
/* 181 */     pnl.add("Center", queryClauses);
/*     */ 
/* 184 */     gh.useGridBag(queryClauses);
/* 185 */     this.m_helper.addPanelTitle(queryClauses, LocaleResources.getString("apTitleQueryDefinition", this.m_cxt));
/*     */ 
/* 188 */     JPanel queryDefinitionPanel = new PanePanel(false);
/* 189 */     gh.m_gc.weighty = 1.0D;
/* 190 */     gh.m_gc.weightx = 1.0D;
/* 191 */     gh.m_gc.fill = 1;
/* 192 */     this.m_helper.addLastComponentInRow(queryClauses, queryDefinitionPanel);
/* 193 */     hasError = initQueryHelper(queryDefinitionPanel);
/*     */ 
/* 195 */     if ((!this.m_isExportTables) || (hasError))
/*     */     {
/* 197 */       pnl.add("South", queryOptions);
/*     */ 
/* 200 */       JPanel optPanel = new PanePanel(false);
/* 201 */       gh.useGridBag(optPanel);
/* 202 */       gh.m_gc.fill = 0;
/* 203 */       this.m_helper.addPanelTitle(optPanel, LocaleResources.getString("apLabelQueryOptions", this.m_cxt));
/*     */ 
/* 206 */       gh.prepareAddLastRowElement(17);
/* 207 */       this.m_helper.addExchangeComponent(optPanel, new JCheckBox(LocaleResources.getString("apExportRecentRevsOnly", this.m_cxt)), "UseExportDate");
/*     */ 
/* 210 */       this.m_helper.addExchangeComponent(optPanel, new JCheckBox(LocaleResources.getString("apAllowExportOfPublishedRevs", this.m_cxt)), "AllowExportPublished");
/*     */ 
/* 215 */       boolean isAutomated = StringUtils.convertToBool(props.getProperty("aIsAutomatedExport"), false);
/*     */ 
/* 217 */       ButtonGroup group = new ButtonGroup();
/* 218 */       gh.prepareAddRowElement();
/* 219 */       String[][] boxInfo = { { "AllRevisions", "apLabelAllSelectedRevs" }, { "LatestRevisions", "apLabelLatestRevs" }, { "NotLatestRevisions", "apLabelNotLatestRevs" }, { "MostRecentMatching", "apLabelMostRecentMatchingRev" } };
/*     */ 
/* 227 */       int len = boxInfo.length;
/* 228 */       for (int i = 0; i < len; ++i)
/*     */       {
/* 230 */         String name = boxInfo[i][0];
/* 231 */         String label = this.m_systemInterface.getString(boxInfo[i][1]);
/* 232 */         boolean isSelected = i == 0;
/*     */ 
/* 234 */         CustomCheckbox box = new CustomCheckbox(label, group, isSelected);
/* 235 */         box.setEnabled(!isAutomated);
/*     */ 
/* 237 */         if (i == len - 1)
/*     */         {
/* 239 */           gh.prepareAddLastRowElement();
/*     */         }
/* 241 */         this.m_helper.addExchangeComponent(optPanel, box, name);
/*     */       }
/*     */ 
/* 244 */       gh.useGridBag(queryOptions);
/* 245 */       this.m_helper.addComponent(queryOptions, optPanel);
/* 246 */       gh.m_gc.weightx = 1.0D;
/* 247 */       this.m_helper.addComponent(queryOptions, new PanePanel(false));
/*     */     }
/*     */ 
/* 250 */     if (hasError)
/*     */     {
/* 252 */       return 4;
/*     */     }
/*     */ 
/* 255 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected boolean initQueryHelper(JPanel pnl)
/*     */   {
/* 260 */     boolean hasError = false;
/*     */ 
/* 262 */     this.m_queryHelper = new QueryBuilderHelper();
/* 263 */     this.m_queryHelper.init(this.m_systemInterface);
/* 264 */     this.m_queryHelper.setDocumentProfile(this.m_docProfile);
/*     */ 
/* 266 */     Vector docFields = null;
/* 267 */     Hashtable displayMap = null;
/* 268 */     String queryStr = this.m_helper.m_props.getProperty("aExportQuery");
/*     */ 
/* 271 */     if (this.m_isExportTables)
/*     */     {
/* 273 */       docFields = computeDisplayFields(this.m_curTableName);
/* 274 */       displayMap = this.m_schemaFields.getDisplayMaps();
/* 275 */       queryStr = this.m_helper.m_props.getProperty("aExportTable" + this.m_curTableName);
/*     */ 
/* 277 */       if ((docFields == null) || (docFields.size() == 0))
/*     */       {
/* 279 */         IdcMessage msg = IdcMessageFactory.lc("apExportTableColumnsNotFound", new Object[] { this.m_curTableName });
/* 280 */         MessageBox.reportError(this.m_systemInterface, msg);
/* 281 */         hasError = true;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 287 */       ResultSet metaFields = SharedObjects.getTable("DocMetaDefinition");
/* 288 */       ViewFields docFieldsObj = new ViewFields(this.m_cxt);
/*     */       try
/*     */       {
/* 291 */         docFieldsObj.m_searchableOnly = false;
/* 292 */         docFieldsObj.m_isAllowPlaceHolderFields = false;
/* 293 */         docFieldsObj.addStandardDocFields();
/* 294 */         docFieldsObj.addDocDateFields(false, false);
/* 295 */         docFieldsObj.addArchiverDocFlags();
/* 296 */         docFieldsObj.addMetaFields(metaFields);
/* 297 */         docFields = docFieldsObj.m_viewFields;
/*     */ 
/* 299 */         displayMap = docFieldsObj.getDisplayMaps();
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 303 */         MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apErrorCreatingQueryDisplayFieldList", new Object[0]));
/*     */ 
/* 305 */         hasError = true;
/*     */       }
/*     */     }
/* 308 */     if (!hasError)
/*     */     {
/* 310 */       this.m_queryHelper.setFieldList(docFields, null);
/* 311 */       this.m_queryHelper.setDisplayMaps(displayMap);
/*     */ 
/* 313 */       this.m_queryHelper.setTitles(LocaleResources.getString("apTitleField", this.m_cxt), LocaleResources.getString("apTitleValue", this.m_cxt));
/*     */ 
/* 315 */       this.m_queryHelper.createStandardQueryPanel(this.m_helper, pnl, this.m_collectionContext.getSharedContext());
/*     */ 
/* 319 */       ExportQueryData queryData = new ExportQueryData();
/* 320 */       queryData.init(this.m_isExportTables);
/* 321 */       String wildcards = SharedObjects.getEnvironmentValue("DatabaseWildcards");
/* 322 */       if (wildcards != null)
/*     */       {
/* 324 */         queryData.setWildcards(wildcards);
/*     */       }
/* 326 */       this.m_queryHelper.setData(queryData, queryStr);
/*     */       try
/*     */       {
/* 330 */         this.m_queryHelper.loadData();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 334 */         MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToLoadExportQuery", new Object[0]));
/*     */ 
/* 336 */         hasError = true;
/*     */       }
/*     */     }
/* 339 */     return hasError;
/*     */   }
/*     */ 
/*     */   public Vector computeDisplayFields(String tableName)
/*     */   {
/* 344 */     DataBinder binder = new DataBinder();
/* 345 */     SharedContext shContext = this.m_collectionContext.getSharedContext();
/*     */     try
/*     */     {
/* 348 */       binder.putLocal("tableNames", tableName);
/* 349 */       shContext.executeService("GET_TABLECOLUMNLIST", binder, false);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 353 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apArchiverCannotRetrieveColumnList", new Object[0]));
/*     */     }
/*     */ 
/* 356 */     return computeTableFieldNames(tableName, binder.getResultSet("TableColumnList"));
/*     */   }
/*     */ 
/*     */   public Vector computeTableFieldNames(String name, ResultSet rset)
/*     */   {
/* 361 */     if (this.m_schemaFields == null)
/*     */     {
/* 363 */       this.m_schemaFields = new ViewFields(this.m_cxt);
/*     */     }
/*     */     else
/*     */     {
/* 368 */       this.m_schemaFields.m_viewFields = new IdcVector();
/*     */     }
/*     */ 
/* 371 */     String tableName = name;
/* 372 */     ResultSetFilter rsFilter = new ResultSetFilter(tableName)
/*     */     {
/*     */       public int checkRow(String val, int curNumRow, Vector row)
/*     */       {
/* 376 */         if (val.equalsIgnoreCase(this.val$tableName))
/*     */         {
/* 378 */           return 1;
/*     */         }
/* 380 */         return 0;
/*     */       }
/*     */     };
/* 383 */     DataResultSet drset = new DataResultSet();
/* 384 */     drset.copyFiltered(rset, "tableName", rsFilter);
/*     */     try
/*     */     {
/* 388 */       int index = ResultSetUtils.getIndexMustExist(drset, "columnName");
/* 389 */       int typeIndex = ResultSetUtils.getIndexMustExist(drset, "columnType");
/* 390 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 392 */         String colName = drset.getStringValue(index);
/* 393 */         ViewFieldDef fieldDef = this.m_schemaFields.addViewFieldDef(colName, colName);
/* 394 */         String colType = drset.getStringValue(typeIndex);
/* 395 */         fieldDef.m_type = colType;
/* 396 */         fieldDef.m_hasView = false;
/* 397 */         fieldDef.m_optionListKey = "tableView";
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 403 */       Report.trace(null, "EditQueryDlg.computeTableFieldNames: The TableColumnList table is badly defined.", e);
/*     */     }
/*     */ 
/* 407 */     return this.m_schemaFields.m_viewFields;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 418 */     String name = exchange.m_compName;
/* 419 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 424 */     return true;
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 430 */     if (updateComponent)
/*     */     {
/* 432 */       exchange.m_compValue = this.m_queryHelper.getQueryProp(name);
/*     */     }
/*     */     else
/*     */     {
/* 436 */       this.m_queryHelper.setQueryProp(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 442 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102751 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditQueryDlg
 * JD-Core Version:    0.5.4
 */