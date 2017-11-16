/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.SimpleResultSetFilter;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class BrowseValueDlg
/*     */   implements ItemListener
/*     */ {
/*  51 */   protected SystemInterface m_systemInterface = null;
/*  52 */   protected DialogHelper m_helper = null;
/*  53 */   protected CollectionContext m_collectionContext = null;
/*  54 */   protected ExecutionContext m_cxt = null;
/*     */ 
/*  56 */   protected DisplayChoice m_batchChoice = null;
/*  57 */   protected DisplayChoice m_fieldChoice = null;
/*     */ 
/*  59 */   protected boolean m_isTable = false;
/*  60 */   protected String m_tableName = null;
/*     */ 
/*     */   public BrowseValueDlg(SystemInterface sys, String title, CollectionContext context)
/*     */   {
/*  64 */     this.m_systemInterface = sys;
/*  65 */     this.m_cxt = sys.getExecutionContext();
/*  66 */     this.m_helper = new DialogHelper(sys, title, true);
/*  67 */     this.m_collectionContext = context;
/*     */   }
/*     */ 
/*     */   public int init(String selFile, String selField, String tableName, boolean isTable)
/*     */   {
/*  72 */     this.m_isTable = isTable;
/*  73 */     this.m_tableName = tableName;
/*  74 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  79 */         String errMsg = null;
/*  80 */         String filename = BrowseValueDlg.this.getSelectedFile();
/*  81 */         if ((filename == null) || (filename.length() == 0) || (filename.equals(LocaleResources.getString("apLabelSelectBatchFile", this.m_dlgHelper.m_exchange.m_sysInterface.getExecutionContext()))))
/*     */         {
/*  85 */           errMsg = LocaleResources.getString("apSelectBatchFile", this.m_dlgHelper.m_exchange.m_sysInterface.getExecutionContext());
/*     */         }
/*     */ 
/*  89 */         if (errMsg != null)
/*     */         {
/*  91 */           BrowseValueDlg.this.m_collectionContext.reportError(errMsg);
/*  92 */           return false;
/*     */         }
/*  94 */         return true;
/*     */       }
/*     */     };
/*  97 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage("ArchiverBrowseForValues"));
/*     */ 
/* 100 */     if (!initUI(mainPanel, selFile, selField))
/*     */     {
/* 102 */       this.m_collectionContext.reportError(LocaleResources.getString("apNoBatchFilesToSelect", this.m_cxt));
/*     */ 
/* 104 */       return 0;
/*     */     }
/*     */ 
/* 107 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public boolean initUI(JPanel pnl, String selFile, String selField)
/*     */   {
/* 112 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 10, 5, 10);
/* 113 */     this.m_helper.addComponent(pnl, new CustomLabel(LocaleResources.getString("apLabelFromBatchFile", this.m_cxt)));
/*     */ 
/* 116 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 117 */     this.m_helper.addExchangeComponent(pnl, this.m_batchChoice = new DisplayChoice(), "batchFileName");
/* 118 */     this.m_batchChoice.addItemListener(this);
/*     */ 
/* 120 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 121 */     this.m_helper.addComponent(pnl, new CustomLabel(LocaleResources.getString("apLabelFromField", this.m_cxt)));
/*     */ 
/* 123 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 124 */     this.m_helper.addExchangeComponent(pnl, this.m_fieldChoice = new DisplayChoice(LocaleResources.getString("apLabelNoFields", this.m_cxt)), "batchFieldName");
/*     */     try
/*     */     {
/* 129 */       DataResultSet rset = this.m_collectionContext.getBatchFiles();
/* 130 */       if (rset != null)
/*     */       {
/* 132 */         rset = removeUnrelatedBatch(rset);
/*     */       }
/* 134 */       Vector fileList = ResultSetUtils.loadValuesFromSet(rset, "aBatchFile");
/* 135 */       if ((fileList == null) || (fileList.size() == 0))
/*     */       {
/* 138 */         return false;
/*     */       }
/*     */ 
/* 141 */       fileList.insertElementAt(LocaleResources.getString("apLabelSelectBatchFile", this.m_cxt), 0);
/*     */ 
/* 143 */       this.m_batchChoice.init(fileList);
/*     */ 
/* 145 */       if (selFile != null)
/*     */       {
/* 147 */         this.m_batchChoice.select(selFile);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 153 */       this.m_collectionContext.reportError(e, LocaleResources.getString("apConfigurationError", this.m_cxt));
/*     */ 
/* 155 */       return false;
/*     */     }
/*     */ 
/* 158 */     checkSelection(selField);
/*     */ 
/* 160 */     return true;
/*     */   }
/*     */ 
/*     */   protected DataResultSet removeUnrelatedBatch(DataResultSet drset)
/*     */   {
/* 165 */     String isTableValue = (this.m_isTable) ? "1" : "0";
/* 166 */     DataResultSet results = new DataResultSet();
/* 167 */     results.copySimpleFiltered(drset, "aIsTableBatch", isTableValue);
/* 168 */     if (this.m_isTable)
/*     */     {
/* 170 */       SimpleResultSetFilter sFilter = new SimpleResultSetFilter("*/" + this.m_tableName + "_arTable*");
/* 171 */       sFilter.m_isWildcard = true;
/* 172 */       DataResultSet tmpResults = new DataResultSet();
/* 173 */       tmpResults.copyFiltered(results, "aBatchFile", sFilter);
/* 174 */       results = tmpResults;
/*     */     }
/* 176 */     return results;
/*     */   }
/*     */ 
/*     */   public void checkSelection(String selField)
/*     */   {
/* 182 */     int selIndex = this.m_batchChoice.getSelectedIndex();
/* 183 */     if (selIndex < 0)
/*     */     {
/* 186 */       return;
/*     */     }
/*     */ 
/* 189 */     String filename = this.m_batchChoice.getSelectedInternalValue();
/* 190 */     if ((filename == null) || (filename.length() == 0) || (filename.equals(LocaleResources.getString("apLabelSelectBatchFile", this.m_cxt))))
/*     */     {
/* 193 */       this.m_fieldChoice.removeAllItems();
/* 194 */       this.m_fieldChoice.setEnabled(false);
/* 195 */       return;
/*     */     }
/*     */ 
/* 198 */     String[][] fields = getMappingFields(false, filename, !this.m_isTable);
/* 199 */     if (fields == null)
/*     */     {
/* 202 */       this.m_collectionContext.reportError(LocaleResources.getString("apNoFieldsForBatchFile", this.m_cxt, filename));
/*     */ 
/* 204 */       return;
/*     */     }
/*     */ 
/* 207 */     this.m_fieldChoice.init(fields);
/*     */ 
/* 209 */     if (selField == null)
/*     */       return;
/* 211 */     this.m_fieldChoice.select(selField);
/*     */   }
/*     */ 
/*     */   protected String[][] getMappingFields(boolean isLocal, String fileName, boolean isDocument)
/*     */   {
/* 217 */     boolean isTable = !isDocument;
/* 218 */     String[][] fields = this.m_collectionContext.getBatchFields(isLocal, fileName, isTable);
/* 219 */     if ((isTable) && (!isLocal))
/*     */     {
/* 221 */       Properties props = this.m_collectionContext.getBatchProperties(fileName);
/* 222 */       String parents = props.getProperty("parentTables");
/* 223 */       if ((parents != null) && (parents.trim().length() != 0))
/*     */       {
/* 225 */         Vector tables = StringUtils.parseArray(parents, ',', '^');
/* 226 */         String table = props.getProperty("tableName");
/* 227 */         tables.insertElementAt(table, 0);
/* 228 */         int size = tables.size();
/* 229 */         int index = -1;
/* 230 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 232 */           table = (String)tables.elementAt(i);
/* 233 */           int numField = Integer.parseInt(props.getProperty("numFields" + table));
/* 234 */           for (int j = 0; j < numField; ++j)
/*     */           {
/* 236 */             ++index;
/* 237 */             if (fields[index][0].indexOf(46) < 0)
/*     */             {
/* 239 */               fields[index][0] = (table + "." + fields[index][0]);
/*     */             }
/*     */ 
/* 242 */             if (fields[index][1].indexOf(46) >= 0)
/*     */               continue;
/* 244 */             fields[index][1] = (table + "." + fields[index][1]);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 250 */     return fields;
/*     */   }
/*     */ 
/*     */   public String getSelectedFile()
/*     */   {
/* 255 */     return this.m_helper.m_props.getProperty("batchFileName");
/*     */   }
/*     */ 
/*     */   public String getSelectedField()
/*     */   {
/* 260 */     return this.m_helper.m_props.getProperty("batchFieldName");
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 265 */     checkSelection(null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 270 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83687 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.BrowseValueDlg
 * JD-Core Version:    0.5.4
 */