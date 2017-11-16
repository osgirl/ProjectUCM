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
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import java.awt.Insets;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class BrowseFieldDlg
/*     */ {
/*  47 */   protected SystemInterface m_systemInterface = null;
/*  48 */   protected ExecutionContext m_cxt = null;
/*  49 */   protected DialogHelper m_helper = null;
/*  50 */   protected CollectionContext m_collectionContext = null;
/*     */ 
/*  53 */   protected String m_tableName = null;
/*  54 */   protected boolean m_isTable = false;
/*     */ 
/*     */   public BrowseFieldDlg(SystemInterface sys, String title, CollectionContext context)
/*     */   {
/*  58 */     this.m_systemInterface = sys;
/*  59 */     this.m_cxt = sys.getExecutionContext();
/*  60 */     this.m_helper = new DialogHelper(sys, title, true);
/*  61 */     this.m_collectionContext = context;
/*     */   }
/*     */ 
/*     */   public int init()
/*     */   {
/*  66 */     return init(null, false);
/*     */   }
/*     */ 
/*     */   public int init(String tableName, boolean isTable)
/*     */   {
/*  71 */     this.m_tableName = tableName;
/*  72 */     this.m_isTable = isTable;
/*  73 */     JPanel mainPanel = this.m_helper.initStandard(null, null, 2, true, DialogHelpTable.getHelpPage("ArchiverBrowseForFields"));
/*     */ 
/*  76 */     initUI(mainPanel);
/*  77 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel pnl)
/*     */   {
/*  82 */     ButtonGroup group = new ButtonGroup();
/*  83 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 10, 5, 10);
/*  84 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  85 */     this.m_helper.addComponent(pnl, new CustomLabel(LocaleResources.getString("apLabelRetrieveFieldsFrom", this.m_cxt)));
/*     */ 
/*  88 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 15, 2, 15);
/*  89 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  90 */     this.m_helper.addExchangeComponent(pnl, new CustomCheckbox(LocaleResources.getString("apLabelLocalSystem", this.m_cxt), group, true), "fromLocalSystem");
/*     */ 
/*  93 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/*  94 */     CustomCheckbox batchBox = new CustomCheckbox(LocaleResources.getString("apLabelBatch", this.m_cxt), group, false);
/*     */ 
/*  96 */     this.m_helper.addExchangeComponent(pnl, batchBox, "fromBatchFile");
/*  97 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*     */ 
/*  99 */     DisplayChoice batchChoice = new DisplayChoice();
/* 100 */     this.m_helper.addExchangeComponent(pnl, batchChoice, "batchFileName");
/*     */     try
/*     */     {
/* 104 */       DataResultSet rset = this.m_collectionContext.getBatchFiles();
/* 105 */       if (rset != null)
/*     */       {
/* 107 */         rset = removeUnrelatedBatch(rset);
/* 108 */         Vector fileList = ResultSetUtils.loadValuesFromSet(rset, "aBatchFile");
/* 109 */         batchChoice.init(fileList);
/* 110 */         if ((fileList == null) || (fileList.size() == 0))
/*     */         {
/* 112 */           batchChoice.addItem(LocaleResources.getString("apLabelNoBatchFiles", this.m_cxt));
/* 113 */           batchBox.setEnabled(false);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 120 */       this.m_collectionContext.reportError(e, LocaleResources.getString("apConfigurationError", this.m_cxt));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected DataResultSet removeUnrelatedBatch(DataResultSet drset)
/*     */   {
/* 127 */     String isTableValue = (this.m_isTable) ? "1" : "0";
/* 128 */     DataResultSet results = new DataResultSet();
/* 129 */     results.copySimpleFiltered(drset, "aIsTableBatch", isTableValue);
/* 130 */     if (this.m_isTable)
/*     */     {
/* 132 */       SimpleResultSetFilter sFilter = new SimpleResultSetFilter("*/" + this.m_tableName + "_arTable*");
/* 133 */       sFilter.m_isWildcard = true;
/* 134 */       DataResultSet tmpResults = new DataResultSet();
/* 135 */       tmpResults.copyFiltered(results, "aBatchFile", sFilter);
/* 136 */       results = tmpResults;
/*     */     }
/* 138 */     return results;
/*     */   }
/*     */ 
/*     */   public boolean getIsLocal()
/*     */   {
/* 144 */     return StringUtils.convertToBool(this.m_helper.m_props.getProperty("fromLocalSystem"), false);
/*     */   }
/*     */ 
/*     */   public String getSelectedFile()
/*     */   {
/* 149 */     return this.m_helper.m_props.getProperty("batchFileName");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 154 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.BrowseFieldDlg
 * JD-Core Version:    0.5.4
 */