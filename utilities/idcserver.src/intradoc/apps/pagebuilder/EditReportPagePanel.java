/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.MetaFieldUtils;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.SqlQueryData;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.gui.QueryBuilderHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Date;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditReportPagePanel extends EditViewBase
/*     */ {
/*     */   protected TableFields m_tableFields;
/*     */   protected QueryBuilderHelper m_queryHelper;
/*     */   protected JPanel m_templatesPanel;
/*     */   protected boolean m_isHistorical;
/*     */   protected JButton m_editReport;
/*     */ 
/*     */   public EditReportPagePanel(boolean isHistorical)
/*     */   {
/*  79 */     this.m_isHistorical = isHistorical;
/*  80 */     this.m_editReport = null;
/*     */   }
/*     */ 
/*     */   public void initDisplay()
/*     */   {
/*  87 */     String[] docTypeChoices = MetaFieldUtils.getMetaFieldTypes();
/*  88 */     Vector dtc = StringUtils.convertToVector(docTypeChoices);
/*  89 */     SharedObjects.putOptList("metaFieldTypes", dtc);
/*     */ 
/*  92 */     createStandardHeader();
/*     */ 
/*  95 */     JPanel reportPanel = createStandardPageEdit();
/*  96 */     reportPanel.setLayout(new BorderLayout());
/*     */ 
/*  98 */     JPanel reportFieldsPanel = new PanePanel();
/*  99 */     GridBagHelper gh = this.m_containerHelper.m_gridHelper;
/* 100 */     gh.useGridBag(reportFieldsPanel);
/* 101 */     gh.m_gc.weighty = 0.2D;
/* 102 */     String reportPanelTitle = (this.m_isHistorical) ? LocaleResources.getString("apTitleHistoricalReportSpecification", this.m_ctx) : LocaleResources.getString("apTitleActiveReportSpecification", this.m_ctx);
/*     */ 
/* 104 */     this.m_containerHelper.addPanelTitle(reportFieldsPanel, reportPanelTitle);
/* 105 */     gh.m_gc.insets = new Insets(5, 5, 5, 5);
/* 106 */     gh.m_gc.weighty = 0.0D;
/* 107 */     this.m_containerHelper.addLabelDisplayPair(reportFieldsPanel, LocaleResources.getString("apLabelDataSource", this.m_ctx), 300, "dataSource");
/*     */ 
/* 109 */     this.m_containerHelper.addLabelDisplayPair(reportFieldsPanel, LocaleResources.getString("apLabelReportTemplate", this.m_ctx), 300, "TemplatePage");
/*     */ 
/* 111 */     if (this.m_isHistorical)
/*     */     {
/* 113 */       this.m_containerHelper.addLabelDisplayPair(reportFieldsPanel, LocaleResources.getString("apLabelReportCreationDate", this.m_ctx), 100, "ReportCreationDate");
/*     */ 
/* 115 */       this.m_containerHelper.addLabelDisplayPair(reportFieldsPanel, LocaleResources.getString("apLabelRowsPerPage", this.m_ctx), 100, "NumRowsPerPage");
/*     */     }
/*     */ 
/* 119 */     gh.m_gc.weighty = 1.0D;
/* 120 */     gh.addEmptyRow(reportFieldsPanel);
/*     */ 
/* 122 */     JPanel reportButtonsPanel = new PanePanel();
/* 123 */     gh.useGridBag(reportButtonsPanel);
/* 124 */     String editButtonStr = (this.m_isHistorical) ? LocaleResources.getString("apDlgButtonCreateReportData", this.m_ctx) : LocaleResources.getString("apDlgButtonEditReportQuery", this.m_ctx);
/*     */ 
/* 127 */     this.m_editReport = new JButton(editButtonStr);
/* 128 */     reportButtonsPanel.add(this.m_editReport);
/*     */ 
/* 130 */     this.m_editReport.addActionListener(new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 134 */         EditReportPagePanel.this.editReport();
/*     */       }
/*     */     });
/* 138 */     reportPanel.add("Center", reportFieldsPanel);
/* 139 */     reportPanel.add("South", reportButtonsPanel);
/*     */ 
/* 142 */     this.m_tableFields = new TableFields();
/* 143 */     this.m_tableFields.init();
/*     */   }
/*     */ 
/*     */   protected boolean editReport()
/*     */   {
/* 149 */     DataResultSet dmd = SharedObjects.getTable("DocMetaDefinition");
/* 150 */     dmd.first();
/* 151 */     SharedLoader.cacheOptList(dmd, "dCaption", "metaDefNames");
/* 152 */     dmd.first();
/*     */ 
/* 154 */     String editTitle = (this.m_isHistorical) ? LocaleResources.getString("apTitleCreateHistoricalReport", this.m_ctx) : LocaleResources.getString("apTitleEditActiveReportQuery", this.m_ctx);
/*     */ 
/* 158 */     DialogHelper helper = new DialogHelper(this.m_containerHelper.m_exchange.m_sysInterface, editTitle, true);
/*     */ 
/* 162 */     this.m_queryHelper = new QueryBuilderHelper();
/* 163 */     this.m_queryHelper.init(this.m_containerHelper.m_exchange.m_sysInterface);
/* 164 */     this.m_queryHelper.setDisplayMaps(this.m_tableFields.getDisplayMaps());
/*     */ 
/* 167 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 173 */         return EditReportPagePanel.this.saveQuery(IdcMessageFactory.lc("apErrorSavingQuery", new Object[0]));
/*     */       }
/*     */     };
/* 178 */     String helpPageName = (this.m_isHistorical) ? "CreateHistoricalReport" : "EditActiveReportQuery";
/*     */ 
/* 181 */     JPanel mainPanel = helper.initStandard(this, okCallback, 2, true, DialogHelpTable.getHelpPage(helpPageName));
/*     */ 
/* 183 */     JPanel headerPanel = new CustomPanel();
/* 184 */     JPanel queryPanel = new CustomPanel();
/* 185 */     GridBagHelper gh = helper.m_gridHelper;
/* 186 */     gh.m_gc.fill = 2;
/* 187 */     gh.m_gc.weightx = 1.0D;
/* 188 */     helper.addLastComponentInRow(mainPanel, headerPanel);
/* 189 */     gh.m_gc.fill = 1;
/* 190 */     gh.m_gc.weighty = 1.0D;
/* 191 */     helper.addLastComponentInRow(mainPanel, queryPanel);
/*     */ 
/* 194 */     DisplayChoice dataSourceChoices = new DisplayChoice();
/* 195 */     String[][] dataSources = TableFields.m_dataSources;
/*     */ 
/* 197 */     dataSourceChoices.init(dataSources);
/*     */ 
/* 199 */     ItemListener dataSourceListener = new ItemListener(helper)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 203 */         DisplayChoice c = (DisplayChoice)e.getSource();
/* 204 */         if (e.getStateChange() != 1)
/*     */           return;
/* 206 */         String selItem = c.getSelectedInternalValue();
/*     */         try
/*     */         {
/* 209 */           EditReportPagePanel.this.handleDataSourceChange(this.val$helper, selItem);
/*     */         }
/*     */         catch (Exception excep)
/*     */         {
/* 213 */           EditReportPagePanel.this.reportError(excep, IdcMessageFactory.lc("apUnableToSelectOption", new Object[0]));
/*     */         }
/*     */       }
/*     */     };
/* 219 */     dataSourceChoices.addItemListener(dataSourceListener);
/*     */ 
/* 221 */     this.m_templatesPanel = new PanePanel();
/* 222 */     this.m_templatesPanel.setLayout(new GridBagLayout());
/*     */ 
/* 224 */     gh.useGridBag(headerPanel);
/* 225 */     gh.m_gc.fill = 2;
/* 226 */     gh.m_gc.weightx = 1.0D;
/* 227 */     helper.addLabelFieldPair(headerPanel, LocaleResources.getString("apTitleDataSource", this.m_ctx), dataSourceChoices, "dataSource");
/*     */ 
/* 229 */     helper.addLabelFieldPair(headerPanel, LocaleResources.getString("apTitleReportTemplate", this.m_ctx), this.m_templatesPanel, "TemplatePage");
/*     */ 
/* 232 */     if (this.m_isHistorical)
/*     */     {
/* 234 */       String numRowsStr = getLocalVar("NumRowsPerPage");
/* 235 */       if (numRowsStr == null)
/*     */       {
/* 237 */         setLocalVar("NumRowsPerPage", "100");
/*     */       }
/* 239 */       helper.addLabelFieldPair(headerPanel, LocaleResources.getString("apLabelRowsPerPage", this.m_ctx), new CustomTextField(10), "NumRowsPerPage");
/*     */     }
/*     */ 
/* 243 */     gh.useGridBag(queryPanel);
/* 244 */     helper.addPanelTitle(queryPanel, LocaleResources.getString("apLabelQueryDefinition", this.m_ctx));
/* 245 */     gh.m_gc.fill = 1;
/* 246 */     gh.m_gc.weightx = 1.0D;
/* 247 */     gh.m_gc.weighty = 1.0D;
/* 248 */     JPanel queryDefinitionPanel = new PanePanel();
/* 249 */     helper.addComponent(queryPanel, queryDefinitionPanel);
/*     */ 
/* 251 */     this.m_queryHelper.createStandardQueryPanel(helper, queryDefinitionPanel, this.m_pageContext.getSharedContext());
/*     */ 
/* 253 */     String curDataSource = getLocalVar("dataSource");
/* 254 */     if (curDataSource == null)
/*     */     {
/* 256 */       curDataSource = dataSources[0][0];
/*     */     }
/*     */     try
/*     */     {
/* 260 */       handleDataSourceChange(helper, curDataSource);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 264 */       reportError(e, IdcMessageFactory.lc("apUnableToLoadDataSourceInfo", new Object[0]));
/*     */     }
/*     */ 
/* 267 */     return helper.prompt() == 1;
/*     */   }
/*     */ 
/*     */   protected boolean saveQuery(IdcMessage errMsg)
/*     */   {
/*     */     try
/*     */     {
/* 274 */       setLocalVar("QueryData", this.m_queryHelper.getFormatString());
/* 275 */       setLocalBool("IsActiveQuery", !this.m_isHistorical);
/* 276 */       setLocalBool("IsSavedQuery", this.m_isHistorical);
/*     */ 
/* 278 */       String curDate = LocaleResources.localizeDate(new Date(), this.m_ctx);
/* 279 */       setLocalVar("ReportCreationDate", curDate);
/* 280 */       if (this.m_isHistorical)
/*     */       {
/* 282 */         setLocalVar("CreateHistoricalReport", "1");
/*     */       }
/* 284 */       this.m_pageContext.saveData(this.m_data.m_pageId, this.m_data);
/* 285 */       updateView(null);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 289 */       reportError(e, errMsg);
/* 290 */       int i = 0;
/*     */ 
/* 294 */       return i; } finally { removeLocalVar("CreateHistoricalReport"); }
/*     */ 
/*     */ 
/* 297 */     return true;
/*     */   }
/*     */ 
/*     */   protected void handleDataSourceChange(DialogHelper helper, String selItem)
/*     */     throws DataException, ServiceException
/*     */   {
/* 303 */     String curDataSource = getLocalVar("dataSource");
/* 304 */     String queryDataStr = null;
/* 305 */     SqlQueryData queryData = new SqlQueryData();
/* 306 */     String wildcards = SharedObjects.getEnvironmentValue("DatabaseWildcards");
/* 307 */     if (wildcards != null)
/*     */     {
/* 309 */       queryData.setWildcards(wildcards);
/*     */     }
/* 311 */     if ((curDataSource != null) && (curDataSource.equals(selItem)))
/*     */     {
/* 313 */       queryDataStr = getLocalVar("QueryData");
/*     */     }
/*     */ 
/* 317 */     Vector v = this.m_tableFields.createTableFieldsList(selItem);
/* 318 */     this.m_queryHelper.setFieldList(v, null);
/* 319 */     this.m_queryHelper.setData(queryData, queryDataStr);
/*     */ 
/* 322 */     this.m_templatesPanel.removeAll();
/* 323 */     JComboBox templatesChoice = new CustomChoice();
/* 324 */     String[] keys = { "datasource", "name" };
/* 325 */     DataResultSet drset = SharedObjects.getTable("ReportsToLoad");
/* 326 */     String[][] templateOptions = ResultSetUtils.createFilteredStringTable(drset, keys, selItem);
/*     */ 
/* 328 */     if (templateOptions.length == 0)
/*     */     {
/* 330 */       throw new DataException(LocaleResources.getString("apNoReportDataSourcesDefined", this.m_ctx));
/*     */     }
/*     */ 
/* 333 */     for (int i = 0; i < templateOptions.length; ++i)
/*     */     {
/* 335 */       templatesChoice.addItem(templateOptions[i][0]);
/*     */     }
/*     */ 
/* 338 */     helper.m_gridHelper.m_gc.fill = 2;
/* 339 */     helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 340 */     helper.addComponent(this.m_templatesPanel, templatesChoice);
/* 341 */     if (!helper.m_exchange.replaceComponent("TemplatePage", templatesChoice))
/*     */     {
/* 343 */       helper.m_exchange.addComponent("TemplatePage", templatesChoice, null);
/*     */     }
/*     */ 
/* 346 */     this.m_templatesPanel.validate();
/*     */ 
/* 349 */     this.m_queryHelper.loadData();
/*     */   }
/*     */ 
/*     */   public void determinePrivileges(UserData curUser)
/*     */   {
/* 358 */     this.m_isEditAllowed = SecurityUtils.isUserOfRole(curUser, "admin");
/*     */   }
/*     */ 
/*     */   public void updatePrivilegeState()
/*     */   {
/* 364 */     if (this.m_editReport != null)
/*     */     {
/* 366 */       this.m_editReport.setVisible(this.m_isEditAllowed);
/*     */     }
/*     */ 
/* 369 */     super.updatePrivilegeState();
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 379 */     String name = exchange.m_compName;
/* 380 */     String val = exchange.m_compValue;
/* 381 */     if (name.equals("NumRowsPerPage"))
/*     */     {
/* 383 */       int result = Validation.checkInteger(val);
/* 384 */       IdcMessage errMsg = null;
/* 385 */       switch (result)
/*     */       {
/*     */       case 0:
/* 389 */         int v = Integer.parseInt(val);
/* 390 */         if (v <= 0)
/*     */         {
/* 392 */           errMsg = IdcMessageFactory.lc("apNonpositiveNumRows", new Object[0]); } break;
/*     */       case -1:
/* 397 */         errMsg = IdcMessageFactory.lc("apSpecifyNumRows", new Object[0]);
/* 398 */         break;
/*     */       default:
/* 400 */         errMsg = IdcMessageFactory.lc("apInvalidNumRows", new Object[0]);
/*     */       }
/*     */ 
/* 403 */       if (errMsg != null)
/*     */       {
/* 405 */         exchange.m_errorMessage = errMsg;
/* 406 */         return false;
/*     */       }
/*     */     }
/* 409 */     return super.validateComponentValue(exchange);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 414 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditReportPagePanel
 * JD-Core Version:    0.5.4
 */