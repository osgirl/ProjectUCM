/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.ClauseDisplay;
/*     */ import intradoc.shared.ClausesData;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditFieldMapDlg
/*     */   implements ComponentBinder
/*     */ {
/*  58 */   protected CollectionContext m_collectionContext = null;
/*  59 */   protected SystemInterface m_systemInterface = null;
/*  60 */   protected ExecutionContext m_cxt = null;
/*  61 */   protected DialogHelper m_helper = null;
/*  62 */   protected MapBuildHelper m_mapHelper = null;
/*     */ 
/*  64 */   protected String m_editItems = "aFieldMaps";
/*     */ 
/*     */   public EditFieldMapDlg(SystemInterface sys, String title, CollectionContext context)
/*     */   {
/*  68 */     this.m_collectionContext = context;
/*  69 */     this.m_systemInterface = sys;
/*  70 */     this.m_cxt = sys.getExecutionContext();
/*  71 */     this.m_helper = new DialogHelper(sys, title, true);
/*     */   }
/*     */ 
/*     */   public int init(Properties props, ClauseDisplay display)
/*     */   {
/*  77 */     this.m_helper.m_props = props;
/*  78 */     String editingTable = props.getProperty("editingTable");
/*  79 */     boolean isTableArchive = false;
/*  80 */     if ((editingTable != null) && (editingTable.length() != 0))
/*     */     {
/*  82 */       this.m_editItems = ("aFieldMapsTable" + editingTable);
/*  83 */       isTableArchive = true;
/*     */     }
/*     */ 
/*  86 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  93 */         EditFieldMapDlg.this.m_mapHelper.getFormatString();
/*     */         try
/*     */         {
/*  97 */           if (!EditFieldMapDlg.this.validateMaps())
/*     */           {
/*  99 */             return false;
/*     */           }
/*     */ 
/* 103 */           Properties editProps = EditFieldMapDlg.this.m_helper.m_props;
/* 104 */           editProps.put("EditItems", EditFieldMapDlg.this.m_editItems);
/* 105 */           String str = EditFieldMapDlg.this.m_mapHelper.getFormatString();
/* 106 */           editProps.put(EditFieldMapDlg.this.m_editItems, str);
/*     */ 
/* 108 */           SharedContext shContext = EditFieldMapDlg.this.m_collectionContext.getSharedContext();
/* 109 */           AppContextUtils.executeService(shContext, "EDIT_ARCHIVEDATA", editProps, true);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 113 */           MessageBox.reportError(EditFieldMapDlg.this.m_systemInterface, exp, IdcMessageFactory.lc("apErrorEditingExportQuery", new Object[0]));
/*     */ 
/* 115 */           return false;
/*     */         }
/* 117 */         return true;
/*     */       }
/*     */     };
/* 120 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 2, true, DialogHelpTable.getHelpPage("EditImportFieldMaps"));
/*     */ 
/* 125 */     this.m_mapHelper = new MapBuildHelper(this.m_collectionContext, editingTable, !isTableArchive);
/* 126 */     this.m_mapHelper.init(this.m_systemInterface);
/*     */     try
/*     */     {
/* 131 */       Vector fields = getFieldList();
/* 132 */       this.m_mapHelper.setFieldList(fields, null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 136 */       this.m_collectionContext.reportError(e, LocaleResources.getString("apErrorCreatingContentFieldList", this.m_cxt));
/*     */     }
/*     */ 
/* 140 */     this.m_mapHelper.setTitles(LocaleResources.getString("apLabelExportField", this.m_cxt), LocaleResources.getString("apLabelTargetField", this.m_cxt));
/*     */ 
/* 142 */     this.m_mapHelper.createStandardClausePanel(this.m_helper, mainPanel, LocaleResources.getString("apLabelFieldMaps", this.m_cxt));
/*     */ 
/* 145 */     String queryStr = props.getProperty(this.m_editItems);
/* 146 */     ClausesData data = new ClausesData();
/* 147 */     data.setClauseDisplay(display, "\n");
/* 148 */     this.m_mapHelper.setData(data, queryStr);
/*     */     try
/*     */     {
/* 152 */       this.m_mapHelper.loadData();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 156 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToLoadFieldMap", new Object[0]));
/*     */     }
/*     */ 
/* 159 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public boolean validateMaps()
/*     */   {
/* 186 */     return true;
/*     */   }
/*     */ 
/*     */   protected Vector getFieldList() throws DataException, ServiceException
/*     */   {
/* 191 */     String editingTable = this.m_helper.m_props.getProperty("editingTable");
/* 192 */     if (editingTable == null)
/*     */     {
/* 195 */       ResultSet metaFields = SharedObjects.getTable("DocMetaDefinition");
/* 196 */       ViewFields docFieldsObj = new ViewFields(this.m_cxt);
/* 197 */       int flags = 22;
/*     */ 
/* 199 */       return docFieldsObj.createDocumentFieldsListWithFlags(metaFields, flags);
/*     */     }
/*     */ 
/* 202 */     SharedContext shContext = this.m_collectionContext.getSharedContext();
/* 203 */     DataBinder binder = new DataBinder();
/* 204 */     binder.setLocalData(this.m_helper.m_props);
/* 205 */     binder.putLocal("tableNames", editingTable);
/*     */     try
/*     */     {
/* 208 */       shContext.executeService("GET_TABLECOLUMNLIST", binder, true);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 212 */       String label = "apUnableRetrieveFieldList";
/* 213 */       String msg = null;
/* 214 */       String batchFile = binder.getLocal("aBatchFile");
/* 215 */       if (batchFile != null)
/*     */       {
/* 217 */         label = "apUnableRetrieveFieldListFromLocalAndArchive";
/* 218 */         msg = LocaleUtils.encodeMessage(label, null, editingTable, batchFile);
/*     */       }
/*     */       else
/*     */       {
/* 222 */         msg = LocaleUtils.encodeMessage(label, null, editingTable);
/*     */       }
/*     */ 
/* 225 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 228 */     ResultSet rset = binder.getResultSet("TableColumnList");
/* 229 */     Vector fields = new IdcVector();
/* 230 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 232 */       FieldDef fd = new FieldDef();
/* 233 */       fd.m_name = rset.getStringValue(1);
/* 234 */       fd.m_caption = fd.m_name;
/* 235 */       fd.m_type = rset.getStringValue(2);
/* 236 */       fields.addElement(fd);
/*     */     }
/*     */ 
/* 239 */     return fields;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 251 */     String name = exchange.m_compName;
/* 252 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 257 */     return true;
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 263 */     if (updateComponent)
/*     */     {
/* 265 */       String str = this.m_mapHelper.getQueryProp(name);
/* 266 */       exchange.m_compValue = str;
/*     */     }
/*     */     else
/*     */     {
/* 270 */       String str = exchange.m_compValue;
/* 271 */       this.m_mapHelper.setQueryProp(name, str);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 277 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditFieldMapDlg
 * JD-Core Version:    0.5.4
 */