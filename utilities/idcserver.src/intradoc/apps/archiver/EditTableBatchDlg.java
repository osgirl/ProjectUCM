/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.BaseView;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class EditTableBatchDlg extends EditBatchDlg
/*     */ {
/*     */   protected static final int NUM_DISPLAYED_FIELDS = 4;
/*  35 */   protected boolean m_hasDuplicateFieldName = true;
/*  36 */   protected DataResultSet m_drset = null;
/*  37 */   protected DataResultSet m_mappedRset = null;
/*     */ 
/*     */   public EditTableBatchDlg(SystemInterface sys, String title, CollectionContext context)
/*     */   {
/*  41 */     super(sys, title, context);
/*  42 */     m_deleteService = "DELETE_BATCH_FILE_TABLES";
/*     */   }
/*     */ 
/*     */   public boolean init(String batchFileName, Vector filterData)
/*     */   {
/*  48 */     int startIndex = batchFileName.indexOf(47) + 1;
/*  49 */     int endIndex = batchFileName.lastIndexOf(46);
/*  50 */     if (endIndex < startIndex)
/*     */     {
/*  52 */       endIndex = batchFileName.length();
/*     */     }
/*  54 */     String nameExtra = batchFileName.substring(startIndex, endIndex);
/*  55 */     this.m_viewName = ("ArchiveBatchViewTable" + nameExtra);
/*  56 */     return super.init(batchFileName, filterData);
/*     */   }
/*     */ 
/*     */   protected ViewData initView()
/*     */   {
/*  62 */     this.m_docView = new BaseView(this.m_helper, this, null);
/*  63 */     return new ViewData(3, "", "ExportResults");
/*     */   }
/*     */ 
/*     */   protected String getColumnList()
/*     */   {
/*  69 */     DataResultSet drset = getMetaData();
/*     */ 
/*  71 */     StringBuffer list = new StringBuffer();
/*  72 */     int size = drset.getNumRows();
/*  73 */     for (int i = 0; (i < size) && (i < 4); drset.next())
/*     */     {
/*  75 */       String name = drset.getStringValue(0);
/*  76 */       if (i != 0)
/*     */       {
/*  78 */         list.append(',');
/*     */       }
/*     */ 
/*  81 */       list.append(name);
/*     */ 
/*  73 */       ++i;
/*     */     }
/*     */ 
/*  85 */     return list.toString();
/*     */   }
/*     */ 
/*     */   protected String[] getPersistentColumns()
/*     */   {
/*  91 */     return null;
/*     */   }
/*     */ 
/*     */   protected void additionalInit()
/*     */   {
/*     */   }
/*     */ 
/*     */   protected ViewFields createFilterFields()
/*     */   {
/* 105 */     ViewFields filterFields = createStandardFields(true, false);
/*     */ 
/* 107 */     return filterFields;
/*     */   }
/*     */ 
/*     */   protected ViewFields createShowColumnsFields()
/*     */   {
/* 113 */     return createStandardFields(false, true);
/*     */   }
/*     */ 
/*     */   protected ViewFields createStandardFields(boolean isFilter, boolean hasIDField)
/*     */   {
/* 119 */     ViewFields viewFields = new ViewFields(this.m_cxt);
/* 120 */     DataResultSet drset = getMetaData();
/* 121 */     String key = null;
/* 122 */     if (hasIDField)
/*     */     {
/* 124 */       key = getIdFields();
/* 125 */       if ((key != null) && (key.length() != 0))
/*     */       {
/* 127 */         FieldInfo fi = new FieldInfo();
/* 128 */         drset.getFieldInfo(key, fi);
/* 129 */         addViewField(viewFields, fi);
/*     */       }
/*     */     }
/*     */ 
/* 133 */     int size = drset.getNumRows();
/* 134 */     boolean isKeyFound = false;
/* 135 */     Properties props = new Properties();
/* 136 */     for (int i = 0; i < size; drset.next())
/*     */     {
/* 138 */       String name = drset.getStringValue(0);
/* 139 */       if ((key != null) && (name.equalsIgnoreCase(key)) && (!isKeyFound))
/*     */       {
/* 141 */         isKeyFound = true;
/*     */       }
/*     */       else {
/* 144 */         int counter = 1;
/* 145 */         String tmpName = name;
/* 146 */         while (props.get(tmpName) != null)
/*     */         {
/* 148 */           tmpName = name + "~" + counter;
/* 149 */           ++counter;
/* 150 */           if (this.m_hasDuplicateFieldName)
/*     */             continue;
/* 152 */           this.m_hasDuplicateFieldName = true;
/*     */         }
/*     */ 
/* 155 */         name = tmpName;
/* 156 */         props.put(name, "");
/*     */ 
/* 158 */         FieldInfo fi = new FieldInfo();
/* 159 */         fi.m_name = name;
/* 160 */         addViewField(viewFields, fi);
/*     */       }
/* 136 */       ++i;
/*     */     }
/*     */ 
/* 162 */     return viewFields;
/*     */   }
/*     */ 
/*     */   protected void addViewField(ViewFields viewFields, FieldInfo fi)
/*     */   {
/* 167 */     viewFields.addViewFieldDefFromInfo(fi);
/*     */   }
/*     */ 
/*     */   protected String getIdFields()
/*     */   {
/* 173 */     Properties prop = this.m_collectionContext.getBatchProperties(this.m_batchName);
/* 174 */     String idField = prop.getProperty("aTablePrimaryKeyFields");
/* 175 */     return idField;
/*     */   }
/*     */ 
/*     */   public DataResultSet getMetaData()
/*     */   {
/* 181 */     return this.m_collectionContext.getBatchMetaSet(false, this.m_batchName, true);
/*     */   }
/*     */ 
/*     */   protected void getRowsFromServer(int startRow)
/*     */     throws ServiceException, DataException
/*     */   {
/* 188 */     Properties props = this.m_binder.getLocalData();
/* 189 */     this.m_binder = new DataBinder();
/* 190 */     this.m_binder.setLocalData(props);
/* 191 */     this.m_binder.putLocal("SuppressResultLocalization", "true");
/*     */ 
/* 193 */     this.m_binder.putLocal("StartRow", String.valueOf(startRow));
/* 194 */     SharedContext shContext = this.m_collectionContext.getSharedContext();
/* 195 */     shContext.executeService("GET_BATCH_FILE_DOCUMENTS", this.m_binder, false);
/*     */ 
/* 197 */     DataResultSet rset = (DataResultSet)this.m_binder.getResultSet("ExportResults");
/* 198 */     if (rset == null)
/*     */     {
/* 200 */       throw new ServiceException(LocaleResources.getString("apContentInfoForBatchNotFound", this.m_cxt));
/*     */     }
/* 202 */     this.m_drset = rset;
/* 203 */     if (this.m_hasDuplicateFieldName)
/*     */     {
/* 205 */       rset = updateFieldNames(rset);
/* 206 */       this.m_mappedRset = rset;
/*     */     }
/* 208 */     setDocSet(rset);
/*     */   }
/*     */ 
/*     */   protected DataResultSet updateFieldNames(DataResultSet rset)
/*     */   {
/* 213 */     DataResultSet drset = new DataResultSet();
/* 214 */     int fieldSize = rset.getNumFields();
/* 215 */     Vector fis = new IdcVector();
/* 216 */     Properties props = new Properties();
/* 217 */     for (int i = 0; i < fieldSize; ++i)
/*     */     {
/* 219 */       FieldInfo fi = new FieldInfo();
/* 220 */       rset.getIndexFieldInfo(i, fi);
/*     */ 
/* 222 */       int counter = 1;
/* 223 */       String tmpName = fi.m_name;
/* 224 */       while (props.get(tmpName) != null)
/*     */       {
/* 226 */         tmpName = fi.m_name + "~" + counter;
/* 227 */         ++counter;
/*     */       }
/* 229 */       fi.m_name = tmpName;
/* 230 */       props.put(tmpName, "1");
/* 231 */       fis.addElement(fi);
/*     */     }
/* 233 */     drset.mergeFieldsWithFlags(fis, 2);
/* 234 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 236 */       Vector row = rset.getCurrentRowValues();
/* 237 */       drset.addRow(row);
/*     */     }
/* 239 */     return drset;
/*     */   }
/*     */ 
/*     */   protected void importSelected()
/*     */   {
/* 245 */     int index = this.m_docView.getSelectedIndex();
/* 246 */     if (index < 0)
/*     */     {
/* 248 */       return;
/*     */     }
/*     */ 
/* 251 */     DataBinder binder = new DataBinder();
/*     */ 
/* 253 */     String batchFile = this.m_binder.getLocal("aBatchFile");
/* 254 */     binder.putLocal("aBatchFile", batchFile);
/*     */ 
/* 257 */     Properties props = this.m_docView.getDataAt(index);
/*     */ 
/* 259 */     DataResultSet drset = new DataResultSet();
/* 260 */     drset.copyFieldInfo(this.m_drset);
/* 261 */     String[] removableFields = { "counter", "editStatus" };
/* 262 */     drset.removeFields(removableFields);
/*     */ 
/* 264 */     DataResultSet tmpDrset = this.m_drset;
/* 265 */     if (this.m_mappedRset != null)
/*     */     {
/* 267 */       tmpDrset = this.m_mappedRset;
/*     */     }
/* 269 */     Vector row = new IdcVector();
/* 270 */     int size = tmpDrset.getNumFields();
/* 271 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 273 */       FieldInfo fi = new FieldInfo();
/* 274 */       tmpDrset.getIndexFieldInfo(i, fi);
/*     */ 
/* 276 */       boolean skip = false;
/* 277 */       for (int j = 0; j < removableFields.length; ++j)
/*     */       {
/* 279 */         if (!fi.m_name.equals(removableFields[j]))
/*     */           continue;
/* 281 */         skip = true;
/* 282 */         break;
/*     */       }
/*     */ 
/* 285 */       if (skip) {
/*     */         continue;
/*     */       }
/*     */ 
/* 289 */       String value = props.getProperty(fi.m_name);
/* 290 */       row.addElement(value);
/*     */     }
/*     */ 
/* 293 */     drset.addRow(row);
/* 294 */     binder.addResultSet("ExportResults", drset);
/*     */ 
/* 296 */     boolean isFailed = false;
/*     */     try
/*     */     {
/* 299 */       SharedContext shContext = this.m_collectionContext.getSharedContext();
/* 300 */       shContext.executeService("IMPORT_TABLE_ENTRY", binder, false);
/*     */     }
/*     */     catch (Exception statusMsg)
/*     */     {
/*     */       String statusMsg;
/* 304 */       isFailed = true;
/* 305 */       this.m_collectionContext.reportError(e, LocaleResources.getString("apUnableToImportFile", this.m_cxt));
/*     */     }
/*     */     finally
/*     */     {
/*     */       String statusMsg;
/* 310 */       if (!isFailed)
/*     */       {
/* 312 */         String statusMsg = binder.getLocal("StatusMessage");
/* 313 */         if (statusMsg == null)
/*     */         {
/* 315 */           statusMsg = LocaleResources.getString("apSuccessfullyImportedRow", this.m_cxt);
/*     */         }
/*     */ 
/* 318 */         this.m_collectionContext.reportError(statusMsg);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 325 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditTableBatchDlg
 * JD-Core Version:    0.5.4
 */