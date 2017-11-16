/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.QueryBuilderHelper;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditQueryDlg extends EditLinkBaseDlg
/*     */ {
/*     */   protected QueryBuilderHelper m_queryHelper;
/*     */   protected String m_queryDataStr;
/*     */   protected PageData m_pageData;
/*     */   protected DocumentLocalizedProfile m_docProfile;
/*     */   protected DialogCallback m_originalOkCallback;
/*  71 */   protected TabPanel m_tabs = null;
/*     */ 
/*     */   public EditQueryDlg(SystemInterface sysInterface, PageData pageData, DocumentLocalizedProfile docProfile, PageManagerContext pageServices, Vector linkInfo, boolean isNew)
/*     */   {
/*  77 */     super(sysInterface, pageServices, linkInfo, isNew, DialogHelpTable.getHelpPage("QueryLinkDef"));
/*     */ 
/*  80 */     this.m_pageData = pageData;
/*  81 */     this.m_docProfile = docProfile;
/*     */   }
/*     */ 
/*     */   public boolean initLinkFields(JPanel top)
/*     */   {
/*  87 */     setTitle(LocaleResources.getString("apTitleQueryLinkDefinition", this.m_ctx));
/*     */     try
/*     */     {
/*  90 */       initQueryInfo();
/*  91 */       initTabs(top);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  95 */       MessageBox.reportError(this.m_sysInterface, e);
/*  96 */       return false;
/*     */     }
/*     */ 
/*  99 */     return true;
/*     */   }
/*     */ 
/*     */   protected void initQueryInfo() throws ServiceException
/*     */   {
/* 104 */     this.m_queryDataStr = null;
/* 105 */     if (!this.m_isNew)
/*     */     {
/* 107 */       this.m_queryDataStr = ((String)this.m_linkInfo.elementAt(1));
/*     */     }
/*     */ 
/* 110 */     this.m_queryHelper = new QueryBuilderHelper();
/* 111 */     this.m_queryHelper.init(this.m_sysInterface);
/*     */ 
/* 114 */     this.m_queryHelper.setDocumentProfile(this.m_docProfile);
/*     */ 
/* 117 */     ResultSet metaFields = SharedObjects.getTable("DocMetaDefinition");
/* 118 */     ViewFields docFieldsObj = new ViewFields(this.m_ctx);
/*     */     try
/*     */     {
/* 121 */       docFieldsObj.addStandardDocFields();
/* 122 */       docFieldsObj.addDocDateFields(false, false);
/* 123 */       docFieldsObj.addRenditions();
/* 124 */       docFieldsObj.addFlags(ViewFields.PUBLISH_FLAG_INFO, 1);
/* 125 */       docFieldsObj.m_searchableOnly = true;
/* 126 */       docFieldsObj.addMetaFields(metaFields);
/*     */ 
/* 128 */       Vector docFields = docFieldsObj.m_viewFields;
/* 129 */       this.m_queryHelper.setDisplayMaps(docFieldsObj.m_tableFields.m_displayMaps);
/*     */ 
/* 132 */       Vector excludedFields = new IdcVector();
/* 133 */       if (StringUtils.convertToBool(this.m_pageData.m_binder.getLocal("restrictByAccount"), false))
/*     */       {
/* 136 */         excludedFields.addElement("dDocAccount");
/*     */       }
/* 138 */       if (StringUtils.convertToBool(this.m_pageData.m_binder.getLocal("restrictByGroup"), false))
/*     */       {
/* 141 */         excludedFields.addElement("dSecurityGroup");
/*     */       }
/*     */ 
/* 145 */       addSearchInfo(docFields);
/*     */ 
/* 147 */       this.m_queryHelper.setFieldList(docFields, StringUtils.convertListToArray(excludedFields));
/*     */ 
/* 152 */       String searchSortFields = SharedObjects.getEnvironmentValue("SearchSortFieldsList");
/*     */ 
/* 154 */       if (searchSortFields == null)
/*     */       {
/* 156 */         Map container = (Map)SharedObjects.getObject("globalObjects", "CommonSearchClientObjects");
/* 157 */         String engineName = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/* 158 */         Properties engineProps = (Properties)container.get(engineName);
/* 159 */         DataResultSet sortFieldsRset = (DataResultSet)engineProps.get("SearchSortFields");
/*     */ 
/* 161 */         searchSortFields = "";
/* 162 */         for (int fieldNo = 0; fieldNo < sortFieldsRset.getNumRows(); ++fieldNo)
/*     */         {
/* 164 */           Vector sortFieldRow = sortFieldsRset.getRowValues(fieldNo);
/* 165 */           String sortFieldName = (String)sortFieldRow.get(0);
/*     */ 
/* 167 */           if (searchSortFields.contains(sortFieldName + ","))
/*     */             continue;
/* 169 */           searchSortFields = searchSortFields + sortFieldName + ",";
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 174 */       Vector sortFieldDefs = new Vector();
/* 175 */       List searchSortFieldsList = StringUtils.makeListFromSequenceSimple(searchSortFields);
/* 176 */       for (int fieldNo = 0; fieldNo < searchSortFieldsList.size(); ++fieldNo)
/*     */       {
/* 178 */         String fieldName = (String)searchSortFieldsList.get(fieldNo);
/*     */ 
/* 180 */         FieldDef fieldDef = new FieldDef();
/* 181 */         fieldDef.m_name = fieldName;
/* 182 */         fieldDef.m_caption = ((String)docFieldsObj.m_fieldLabels.get(fieldName));
/*     */ 
/* 184 */         if (fieldDef.m_caption == null)
/*     */           continue;
/* 186 */         sortFieldDefs.add(fieldDef);
/*     */       }
/*     */ 
/* 189 */       this.m_queryHelper.setSortFieldList(sortFieldDefs);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 193 */       throw new ServiceException(LocaleResources.getString("apErrorCreatingFieldList", this.m_ctx));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addSearchInfo(Vector docFields)
/*     */   {
/* 200 */     DataResultSet drset = SharedObjects.getTable("SearchDesignInfo");
/* 201 */     if ((drset == null) || (drset.isEmpty()))
/*     */       return;
/*     */     try
/*     */     {
/* 205 */       int nameIndex = ResultSetUtils.getIndexMustExist(drset, "fieldName");
/* 206 */       int optIndex = ResultSetUtils.getIndexMustExist(drset, "advOptions");
/*     */ 
/* 208 */       int size = docFields.size();
/* 209 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 211 */         ViewFieldDef def = (ViewFieldDef)docFields.elementAt(i);
/* 212 */         Vector row = drset.findRow(nameIndex, def.m_name);
/* 213 */         if (row == null)
/*     */           continue;
/* 215 */         String str = (String)row.elementAt(optIndex);
/* 216 */         Vector options = StringUtils.parseArray(str, ',', '^');
/* 217 */         int count = options.size();
/* 218 */         for (int j = 0; j < count; ++j)
/*     */         {
/* 220 */           String key = (String)options.elementAt(j);
/* 221 */           ++j;
/* 222 */           String val = (String)options.elementAt(j);
/* 223 */           if (!key.equals("isZone"))
/*     */             continue;
/* 225 */           def.m_isZoneField = StringUtils.convertToBool(val, false);
/* 226 */           break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 234 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 236 */       Report.debug("applet", null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initTabs(JPanel top)
/*     */     throws ServiceException
/*     */   {
/* 244 */     String[][] tabInfo = { { "EditQueryDefPanel", "intradoc.apps.pagebuilder.EditQueryDefPanel", "apLabelQuery" }, { "EditQueryResultsPanel", "intradoc.apps.pagebuilder.EditQueryResultsPanel", "apLabelResults" } };
/*     */ 
/* 250 */     this.m_tabs = new TabPanel();
/* 251 */     for (int i = 0; i < tabInfo.length; ++i)
/*     */     {
/* 253 */       EditQueryBasePanel editPanel = (EditQueryBasePanel)ComponentClassFactory.createClassInstance(tabInfo[i][0], tabInfo[i][1], LocaleResources.getString("apUnableToLoadPanel", this.m_ctx, tabInfo[i][0]));
/*     */ 
/* 257 */       editPanel.setQueryInfo(this.m_queryHelper, this.m_queryDataStr, this.m_pageData);
/* 258 */       editPanel.init(this.m_helper, this.m_pageServices, this.m_docProfile, this.m_isNew);
/* 259 */       this.m_tabs.addPane(LocaleResources.getString(tabInfo[i][2], this.m_ctx), editPanel, editPanel, false);
/*     */     }
/*     */ 
/* 263 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 264 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 265 */     this.m_helper.addComponent(top, this.m_tabs);
/*     */   }
/*     */ 
/*     */   public boolean prompt(DialogCallback okCallback)
/*     */   {
/* 273 */     this.m_originalOkCallback = okCallback;
/*     */ 
/* 275 */     DialogCallback newCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 280 */         if (!EditQueryDlg.this.m_tabs.validateAllPanes())
/*     */         {
/* 282 */           return false;
/*     */         }
/*     */ 
/* 286 */         String linkData = EditQueryDlg.this.m_queryHelper.getFormatString();
/* 287 */         EditQueryDlg.this.m_linkInfo.setElementAt(linkData, 1);
/*     */ 
/* 289 */         return EditQueryDlg.this.m_originalOkCallback.handleDialogEvent(e);
/*     */       }
/*     */     };
/* 292 */     return super.prompt(newCallback);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 302 */     int index = determineIndex(name);
/* 303 */     if (index >= 0)
/*     */     {
/* 305 */       exchangeBoundField(index, exchange, updateComponent);
/*     */     }
/* 309 */     else if (updateComponent)
/*     */     {
/* 311 */       exchange.m_compValue = this.m_queryHelper.getQueryProp(name);
/*     */     }
/*     */     else
/*     */     {
/* 315 */       this.m_queryHelper.setQueryProp(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 322 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditQueryDlg
 * JD-Core Version:    0.5.4
 */