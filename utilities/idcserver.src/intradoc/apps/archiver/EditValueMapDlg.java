/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
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
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditValueMapDlg
/*     */   implements ComponentBinder
/*     */ {
/*  61 */   protected CollectionContext m_collectionContext = null;
/*  62 */   protected SystemInterface m_systemInterface = null;
/*  63 */   protected ExecutionContext m_cxt = null;
/*  64 */   protected DialogHelper m_helper = null;
/*  65 */   protected ValueMapHelper m_mapHelper = null;
/*     */ 
/*  67 */   protected String m_editItems = "aValueMaps";
/*     */ 
/*     */   public EditValueMapDlg(SystemInterface sys, String title, CollectionContext context)
/*     */   {
/*  71 */     this.m_collectionContext = context;
/*  72 */     this.m_systemInterface = sys;
/*  73 */     this.m_cxt = sys.getExecutionContext();
/*  74 */     this.m_helper = new DialogHelper(sys, title, true);
/*     */   }
/*     */ 
/*     */   public int init(Properties props, ClauseDisplay display)
/*     */   {
/*  79 */     this.m_helper.m_props = props;
/*     */ 
/*  81 */     String editingTable = props.getProperty("editingTable");
/*  82 */     if (editingTable != null)
/*     */     {
/*  84 */       int index = editingTable.indexOf(44);
/*  85 */       if (index >= 0)
/*     */       {
/*  87 */         editingTable = editingTable.substring(0, index);
/*     */       }
/*  89 */       this.m_editItems = ("aValueMapsTable" + editingTable);
/*     */     }
/*     */ 
/*  92 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 101 */           if (!EditValueMapDlg.this.validateMaps())
/*     */           {
/* 103 */             return false;
/*     */           }
/*     */ 
/* 106 */           Properties editProps = EditValueMapDlg.this.m_helper.m_props;
/* 107 */           editProps.put("EditItems", EditValueMapDlg.this.m_editItems);
/* 108 */           String str = EditValueMapDlg.this.m_mapHelper.getFormatString();
/* 109 */           editProps.put(EditValueMapDlg.this.m_editItems, str);
/*     */ 
/* 111 */           SharedContext shContext = EditValueMapDlg.this.m_collectionContext.getSharedContext();
/* 112 */           AppContextUtils.executeService(shContext, "EDIT_ARCHIVEDATA", editProps, true);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 116 */           MessageBox.reportError(EditValueMapDlg.this.m_systemInterface, exp, IdcMessageFactory.lc("apErrorEditingValueMaps", new Object[0]));
/*     */ 
/* 118 */           return false;
/*     */         }
/* 120 */         return true;
/*     */       }
/*     */     };
/* 123 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 2, true, DialogHelpTable.getHelpPage("EditImportValueMaps"));
/*     */ 
/* 128 */     boolean isTable = StringUtils.convertToBool(props.getProperty("isTableArchive"), false);
/* 129 */     String tableName = props.getProperty("aTableName");
/* 130 */     this.m_mapHelper = new ValueMapHelper(this.m_collectionContext, tableName, isTable);
/* 131 */     this.m_mapHelper.init(this.m_systemInterface);
/*     */     try
/*     */     {
/* 135 */       Vector fields = getFieldList();
/* 136 */       this.m_mapHelper.setFieldList(fields, null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 140 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apErrorCreatingDisplayForValueMaps", new Object[0]));
/*     */     }
/*     */ 
/* 144 */     this.m_mapHelper.setTitles(LocaleResources.getString("apTitleField", this.m_cxt), LocaleResources.getString("apLabelInputValue", this.m_cxt), LocaleResources.getString("apLabelOutputValue", this.m_cxt));
/*     */ 
/* 147 */     this.m_mapHelper.createStandardClausePanel(this.m_helper, mainPanel, LocaleResources.getString("apLabelValueMaps", this.m_cxt));
/*     */ 
/* 150 */     String queryStr = props.getProperty(this.m_editItems);
/* 151 */     ClausesData data = new ClausesData();
/* 152 */     data.setClauseDisplay(display, "\n");
/* 153 */     this.m_mapHelper.setData(data, queryStr);
/*     */     try
/*     */     {
/* 157 */       this.m_mapHelper.loadData();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 161 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToLoadValueMapData", new Object[0]));
/*     */     }
/*     */ 
/* 165 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public boolean validateMaps()
/*     */   {
/* 171 */     ClausesData mapsData = this.m_mapHelper.getClauseData();
/* 172 */     Vector valueMaps = mapsData.m_clauses;
/*     */ 
/* 174 */     Hashtable mapper = new Hashtable();
/* 175 */     int size = valueMaps.size();
/* 176 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 178 */       Vector values = (Vector)valueMaps.elementAt(i);
/* 179 */       String value = (String)values.elementAt(1);
/* 180 */       String field = (String)values.elementAt(2);
/*     */ 
/* 182 */       Vector mappedValues = (Vector)mapper.get(field);
/* 183 */       if (mappedValues != null)
/*     */       {
/* 185 */         int numValues = mappedValues.size();
/* 186 */         for (int j = 0; j < numValues; ++j)
/*     */         {
/* 188 */           String str = (String)mappedValues.elementAt(j);
/* 189 */           if (!str.equals(value))
/*     */             continue;
/* 191 */           this.m_collectionContext.reportError(LocaleResources.getString("apCannotMapFieldTwice", this.m_cxt, field));
/*     */ 
/* 193 */           return false;
/*     */         }
/*     */ 
/* 196 */         mappedValues.addElement(value);
/*     */       }
/*     */       else
/*     */       {
/* 200 */         mappedValues = new IdcVector();
/* 201 */         mapper.put(field, mappedValues);
/*     */       }
/* 203 */       mappedValues.addElement(value);
/*     */     }
/* 205 */     return true;
/*     */   }
/*     */ 
/*     */   protected Vector getFieldList()
/*     */     throws DataException, ServiceException
/*     */   {
/* 211 */     String editingTable = this.m_helper.m_props.getProperty("editingTable");
/* 212 */     if (editingTable == null)
/*     */     {
/* 215 */       ResultSet metaFields = SharedObjects.getTable("DocMetaDefinition");
/* 216 */       ViewFields docFieldsObj = new ViewFields(this.m_cxt);
/* 217 */       docFieldsObj.m_enabledOnly = false;
/* 218 */       int flags = 22;
/*     */ 
/* 220 */       return docFieldsObj.createDocumentFieldsListWithFlags(metaFields, flags);
/*     */     }
/*     */ 
/* 223 */     SharedContext shContext = this.m_collectionContext.getSharedContext();
/* 224 */     DataBinder binder = new DataBinder();
/* 225 */     binder.setLocalData(this.m_helper.m_props);
/* 226 */     binder.putLocal("tableNames", editingTable);
/*     */     try
/*     */     {
/* 230 */       shContext.executeService("GET_TABLECOLUMNLIST", binder, true);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 234 */       String label = "apUnableRetrieveFieldList";
/* 235 */       String msg = null;
/* 236 */       String batchFile = binder.getLocal("aBatchFile");
/* 237 */       if (batchFile != null)
/*     */       {
/* 239 */         label = "apUnableRetrieveFieldListFromLocalAndArchive";
/* 240 */         msg = LocaleUtils.encodeMessage(label, null, editingTable, batchFile);
/*     */       }
/*     */       else
/*     */       {
/* 244 */         msg = LocaleUtils.encodeMessage(label, null, editingTable);
/*     */       }
/* 246 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 250 */     ResultSet rset = binder.getResultSet("TableColumnList");
/* 251 */     Vector fields = new IdcVector();
/* 252 */     Vector tables = ResultSetUtils.loadValuesFromSet(rset, "tableName");
/* 253 */     boolean usePrefix = false;
/* 254 */     if (tables.size() > 1)
/*     */     {
/* 256 */       usePrefix = true;
/*     */     }
/* 258 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 260 */       FieldDef fd = new FieldDef();
/* 261 */       String prefix = (usePrefix) ? rset.getStringValue(0) + "." : "";
/* 262 */       fd.m_name = (prefix + rset.getStringValue(1));
/* 263 */       fd.m_caption = fd.m_name;
/* 264 */       fd.m_type = rset.getStringValue(2);
/* 265 */       fields.addElement(fd);
/*     */     }
/*     */ 
/* 268 */     return fields;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 279 */     String name = exchange.m_compName;
/* 280 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 285 */     return true;
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 291 */     if (updateComponent)
/*     */     {
/* 293 */       String str = this.m_mapHelper.getQueryProp(name);
/* 294 */       exchange.m_compValue = str;
/*     */     }
/*     */     else
/*     */     {
/* 298 */       String str = exchange.m_compValue;
/* 299 */       this.m_mapHelper.setQueryProp(name, str);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 305 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditValueMapDlg
 * JD-Core Version:    0.5.4
 */