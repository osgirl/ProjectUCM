/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class TableView extends BaseView
/*     */ {
/*  55 */   protected DataResultSet m_tableInfos = null;
/*     */ 
/*     */   public TableView(ContainerHelper helper, RefreshView refresher, DocumentLocalizedProfile docProfile)
/*     */   {
/*  59 */     super(helper, refresher, docProfile);
/*     */ 
/*  64 */     this.m_customMetaInSeparatePanel = false;
/*     */   }
/*     */ 
/*     */   public void init(ViewData viewData)
/*     */   {
/*     */     try
/*     */     {
/*  72 */       DataResultSet rset = (DataResultSet)getTableColumns(viewData.m_tableName);
/*  73 */       init(rset);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  77 */       MessageBox.doMessage(this.m_systemInterface, e.getMessage(), 1);
/*     */ 
/*  79 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected ResultSet getTableColumns(String table) throws ServiceException
/*     */   {
/*  85 */     Properties props = new Properties();
/*  86 */     props.put("tableNames", table);
/*     */ 
/*  88 */     DataBinder binder = new DataBinder();
/*  89 */     binder.setLocalData(props);
/*     */ 
/*  91 */     SharedContext shContext = this.m_refresher.getSharedContext();
/*  92 */     shContext.executeService("GET_TABLECOLUMNLIST", binder, false);
/*     */ 
/*  94 */     DataResultSet drset = (DataResultSet)binder.getResultSet("TableColumnList");
/*  95 */     return drset;
/*     */   }
/*     */ 
/*     */   public void init(DataResultSet drset)
/*     */   {
/* 100 */     this.m_tableInfos = drset;
/*     */     try
/*     */     {
/* 103 */       createViewFields();
/* 104 */       this.m_columnData.m_columnStr = createDefaultVisibleFields();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 108 */       MessageBox.doMessage(this.m_systemInterface, e.getMessage(), 1);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void configureShowColumns(boolean forceRefresh)
/*     */   {
/* 116 */     ViewFields columnFields = this.m_columnData.m_columnFields;
/* 117 */     if ((columnFields != null) && (!forceRefresh))
/*     */       return;
/* 119 */     this.m_columnData.m_columnFields = this.m_filterFields;
/*     */   }
/*     */ 
/*     */   protected void addViewField(ViewFields viewFields, FieldInfo fi)
/*     */   {
/* 125 */     viewFields.addViewFieldDefFromInfo(fi);
/*     */   }
/*     */ 
/*     */   protected void addDisplayMaps()
/*     */   {
/* 131 */     if (this.m_displayCallback == null)
/*     */     {
/* 133 */       this.m_displayColumns = new String[] { "dUserAuthType" };
/*     */ 
/* 135 */       this.m_displayCallback = new DisplayStringCallbackAdaptor()
/*     */       {
/*     */         public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */         {
/* 141 */           String[][] displayMap = (String[][])null;
/* 142 */           if (name.equals("dUserAuthType"))
/*     */           {
/* 144 */             displayMap = TableFields.USER_AUTH_TYPES;
/*     */           }
/* 146 */           if (displayMap == null)
/*     */           {
/* 148 */             return value;
/*     */           }
/*     */ 
/* 151 */           String displayStr = StringUtils.getPresentationString(displayMap, value);
/* 152 */           if (displayStr == null)
/*     */           {
/* 154 */             displayStr = "";
/*     */           }
/* 156 */           return displayStr;
/*     */         }
/*     */       };
/*     */     }
/* 160 */     super.addDisplayMaps();
/*     */   }
/*     */ 
/*     */   public void refreshView(String[] selectedObjs)
/*     */     throws ServiceException
/*     */   {
/* 166 */     if (this.m_refresher == null)
/*     */     {
/* 168 */       throw new ServiceException("!apNoRefreshViewError");
/*     */     }
/*     */ 
/* 171 */     Vector filter = buildFilter();
/* 172 */     DataResultSet defSet = null;
/* 173 */     if (this.m_isFilterChanged)
/*     */     {
/* 175 */       defSet = this.m_filterDefaults;
/*     */     }
/* 177 */     DataBinder binder = this.m_refresher.refresh(this.m_viewData.m_tableName, filter, defSet);
/*     */ 
/* 179 */     prepareForView(binder);
/*     */ 
/* 181 */     this.m_list.refreshListEx(binder, selectedObjs);
/* 182 */     this.m_refresher.checkSelection();
/*     */ 
/* 184 */     updateStatusMessage(binder);
/*     */   }
/*     */ 
/*     */   public void prepareForView(DataBinder binder)
/*     */   {
/* 190 */     DataResultSet drset = (DataResultSet)binder.getResultSet(this.m_viewData.m_tableName);
/* 191 */     Hashtable map = new Hashtable();
/* 192 */     int len = drset.getNumFields();
/*     */ 
/* 194 */     Vector fis = new IdcVector();
/* 195 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 197 */       String name = drset.getFieldName(i);
/* 198 */       int num = 1;
/* 199 */       String tmpName = name;
/* 200 */       while (map.get(name) != null)
/*     */       {
/* 202 */         name = tmpName + num;
/* 203 */         ++num;
/*     */       }
/* 205 */       map.put(name, "1");
/* 206 */       FieldInfo fi = new FieldInfo();
/* 207 */       drset.getIndexFieldInfo(i, fi);
/* 208 */       fi.m_name = name;
/* 209 */       fis.addElement(fi);
/*     */     }
/*     */ 
/* 212 */     DataResultSet newDrset = new DataResultSet();
/* 213 */     newDrset.mergeFieldsWithFlags(fis, 0);
/*     */ 
/* 215 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 217 */       Vector row = drset.getCurrentRowValues();
/* 218 */       newDrset.addRow(row);
/*     */     }
/*     */ 
/* 221 */     binder.addResultSet(this.m_viewData.m_tableName, newDrset);
/*     */   }
/*     */ 
/*     */   protected void createViewFields() throws DataException
/*     */   {
/* 226 */     ViewFields fields = new ViewFields(this.m_cxt);
/*     */ 
/* 228 */     int colIndex = ResultSetUtils.getIndexMustExist(this.m_tableInfos, "columnName");
/* 229 */     int typeIndex = ResultSetUtils.getIndexMustExist(this.m_tableInfos, "columnType");
/*     */ 
/* 231 */     Properties prop = new Properties();
/*     */ 
/* 233 */     for (this.m_tableInfos.first(); this.m_tableInfos.isRowPresent(); this.m_tableInfos.next())
/*     */     {
/* 235 */       String colName = this.m_tableInfos.getStringValue(colIndex);
/* 236 */       String tmpName = colName;
/* 237 */       int num = 1;
/*     */ 
/* 239 */       while (prop.getProperty(colName) != null)
/*     */       {
/* 241 */         colName = tmpName + num;
/* 242 */         ++num;
/*     */       }
/* 244 */       prop.put(colName, "1");
/*     */ 
/* 246 */       ViewFieldDef vfd = fields.addViewFieldDef(colName, colName);
/* 247 */       vfd.m_type = this.m_tableInfos.getStringValue(typeIndex);
/*     */     }
/*     */ 
/* 250 */     setFilterFields(fields);
/* 251 */     setShowColumnFields(fields);
/*     */   }
/*     */ 
/*     */   protected String createDefaultVisibleFields()
/*     */     throws DataException
/*     */   {
/* 258 */     String[] keys = { "isPrimaryKey", "columnName" };
/* 259 */     String[][] fieldTable = ResultSetUtils.createFilteredStringTable(this.m_tableInfos, keys, "1");
/*     */ 
/* 261 */     String[] fields = new String[fieldTable.length];
/* 262 */     StringBuffer fieldStr = new StringBuffer();
/*     */ 
/* 264 */     for (int i = 0; i < fieldTable.length; ++i)
/*     */     {
/* 266 */       fields[i] = fieldTable[i][0];
/* 267 */       if (fieldStr.length() > 0)
/*     */       {
/* 269 */         fieldStr.append(',');
/*     */       }
/* 271 */       fieldStr.append(fields[i]);
/*     */     }
/*     */ 
/* 274 */     int remainingSize = 4 - fields.length;
/* 275 */     int colIndex = ResultSetUtils.getIndexMustExist(this.m_tableInfos, "columnName");
/* 276 */     for (this.m_tableInfos.first(); (this.m_tableInfos.isRowPresent()) && (remainingSize > 0); )
/*     */     {
/* 279 */       String name = this.m_tableInfos.getStringValue(colIndex);
/* 280 */       if (StringUtils.findStringIndexEx(fields, name, true) == -1)
/*     */       {
/* 284 */         if (fieldStr.length() > 0)
/*     */         {
/* 286 */           fieldStr.append(',');
/*     */         }
/* 288 */         fieldStr.append(name);
/* 289 */         --remainingSize;
/*     */       }
/* 277 */       this.m_tableInfos.next();
/*     */     }
/*     */ 
/* 291 */     return fieldStr.toString();
/*     */   }
/*     */ 
/*     */   protected void createShowColumns()
/*     */   {
/* 298 */     JButton showClmns = new JButton(LocaleResources.getString("apShowColumnsButtonLabel", this.m_cxt));
/*     */ 
/* 300 */     ActionListener showListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 304 */         ShowColumnDlg dlg = new ShowColumnDlg(TableView.this.m_systemInterface, LocaleResources.getString("apShowColumnsTitle", TableView.this.m_cxt));
/*     */ 
/* 308 */         TableView.this.configureShowColumns(!TableView.this.m_isFixedShowColumns);
/* 309 */         UserDrawList list = TableView.this.m_list.m_list;
/* 310 */         int numColumns = list.getColumnCount();
/* 311 */         Vector columns = new IdcVector();
/* 312 */         for (int i = 0; i < numColumns; ++i)
/*     */         {
/* 314 */           ColumnInfo info = list.getColumnInfo(i);
/*     */ 
/* 316 */           ShowColumnInfo showInfo = new ShowColumnInfo();
/* 317 */           showInfo.m_name = info.m_fieldId;
/* 318 */           showInfo.m_label = info.m_labelText;
/* 319 */           showInfo.m_order = i;
/*     */ 
/* 321 */           columns.addElement(showInfo);
/*     */         }
/* 323 */         TableView.this.m_columnData.m_columns = columns;
/*     */ 
/* 327 */         dlg.initEx(TableView.this.m_columnData, true, TableView.this.m_refresher.getSharedContext());
/* 328 */         if (dlg.prompt() != 1)
/*     */           return;
/* 330 */         TableView.this.m_list.setVisibleColumnsEx(TableView.this.m_columnData.m_columnStr, TableView.this.m_columnData.m_columnLabels);
/*     */ 
/* 333 */         String sel = TableView.this.m_list.getSelectedObj();
/* 334 */         TableView.this.m_list.reloadList(sel);
/*     */       }
/*     */     };
/* 338 */     showClmns.addActionListener(showListener);
/*     */ 
/* 340 */     JPanel btnPanel = this.m_list.getButtonPanel();
/* 341 */     btnPanel.add(showClmns);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 346 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.TableView
 * JD-Core Version:    0.5.4
 */