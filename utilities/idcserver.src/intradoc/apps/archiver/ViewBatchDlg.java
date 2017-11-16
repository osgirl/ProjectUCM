/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ViewBatchDlg
/*     */   implements ActionListener
/*     */ {
/*  56 */   protected DialogHelper m_helper = null;
/*  57 */   protected CollectionContext m_collectionContext = null;
/*  58 */   protected ExecutionContext m_cxt = null;
/*     */ 
/*  60 */   protected UdlPanel m_batchList = null;
/*     */ 
/*  63 */   protected Vector m_filterInfo = null;
/*  64 */   protected SystemInterface m_sys = null;
/*     */ 
/*     */   public ViewBatchDlg(SystemInterface sys, String title, CollectionContext context)
/*     */   {
/*  68 */     this.m_helper = new DialogHelper(sys, title, true);
/*  69 */     this.m_cxt = sys.getExecutionContext();
/*  70 */     this.m_collectionContext = context;
/*  71 */     this.m_sys = sys;
/*     */   }
/*     */ 
/*     */   public int init()
/*     */   {
/*  76 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/*  77 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/*  79 */     initUI(mainPanel);
/*     */ 
/*  81 */     refreshList(null);
/*     */ 
/*  83 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel pnl)
/*     */   {
/*  88 */     this.m_batchList = new UdlPanel(LocaleResources.getString("apLabelCurrentBatchFiles", this.m_cxt), null, 450, 20, "BatchFiles", true);
/*     */ 
/*  90 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("apLabelBatchFile", this.m_cxt), "aBatchFile", 50.0D);
/*  91 */     this.m_batchList.setColumnInfo(info);
/*  92 */     info = new ColumnInfo(LocaleResources.getString("apLabelInstanceName", this.m_cxt), "IDC_Name", 25.0D);
/*  93 */     this.m_batchList.setColumnInfo(info);
/*  94 */     info = new ColumnInfo(LocaleResources.getString("apLabelNumber", this.m_cxt), "aNumDocuments", 13.0D);
/*  95 */     this.m_batchList.setColumnInfo(info);
/*  96 */     info = new ColumnInfo(LocaleResources.getString("apLabelState", this.m_cxt), "aState", 12.0D);
/*     */ 
/*  98 */     this.m_batchList.setColumnInfo(info);
/*  99 */     this.m_batchList.init();
/* 100 */     this.m_batchList.useDefaultListener();
/* 101 */     this.m_batchList.setVisibleColumns("aBatchFile,IDC_Name,aNumDocuments,aState");
/* 102 */     UserDrawList list = this.m_batchList.m_list;
/* 103 */     list.setFlags(14);
/*     */ 
/* 106 */     ActionListener aListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 110 */         ViewBatchDlg.this.editBatchFile();
/*     */       }
/*     */     };
/* 113 */     this.m_batchList.m_list.addActionListener(aListener);
/*     */ 
/* 115 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 116 */     this.m_helper.m_gridHelper.m_gc.weighty = 10.0D;
/* 117 */     this.m_helper.addComponent(pnl, this.m_batchList);
/*     */ 
/* 120 */     String[][] BUTTON_INFO = { { "apDlgButtonImport", "import", "true" }, { "apDlgButtonEdit", "edit", "true" }, { "apLabelDelete", "delete", "true" }, { "apLabelClose", "close", "false" } };
/*     */ 
/* 129 */     JPanel toolbar = this.m_helper.m_toolbar;
/*     */ 
/* 131 */     for (int i = 0; i < BUTTON_INFO.length; ++i)
/*     */     {
/* 133 */       boolean controlled = StringUtils.convertToBool(BUTTON_INFO[i][2], false);
/*     */ 
/* 135 */       JButton btn = this.m_batchList.addButton(LocaleResources.getString(BUTTON_INFO[i][0], this.m_cxt), controlled);
/* 136 */       btn.setActionCommand(BUTTON_INFO[i][1]);
/* 137 */       btn.addActionListener(this);
/* 138 */       btn.setEnabled(!controlled);
/*     */ 
/* 140 */       toolbar.add(btn);
/*     */     }
/*     */ 
/* 143 */     this.m_helper.addHelpInfo(DialogHelpTable.getHelpPage("ViewExportBatchFiles"));
/*     */   }
/*     */ 
/*     */   public void refreshList(String selObj)
/*     */   {
/* 148 */     DataResultSet rset = this.m_collectionContext.getBatchFiles();
/* 149 */     this.m_batchList.refreshList(rset, selObj);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 155 */     String cmd = e.getActionCommand();
/* 156 */     if (cmd.equals("close"))
/*     */     {
/* 158 */       this.m_helper.close();
/*     */     }
/* 160 */     else if (cmd.equals("delete"))
/*     */     {
/* 162 */       deleteBatchFile();
/*     */     }
/* 164 */     else if (cmd.equals("edit"))
/*     */     {
/* 166 */       editBatchFile();
/*     */     } else {
/* 168 */       if (!cmd.equals("import"))
/*     */         return;
/* 170 */       importBatchFile();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteBatchFile()
/*     */   {
/* 176 */     int index = this.m_batchList.getSelectedIndex();
/* 177 */     if (index < 0)
/*     */     {
/* 179 */       return;
/*     */     }
/*     */ 
/* 182 */     Properties props = this.m_batchList.getDataAt(index);
/* 183 */     String filename = props.getProperty("aBatchFile");
/*     */ 
/* 185 */     IdcMessage msg = IdcMessageFactory.lc("apVerifyBatchFileDelete", new Object[] { filename });
/* 186 */     if (MessageBox.doMessage(this.m_helper.m_exchange.m_sysInterface, msg, 4) == 3)
/*     */     {
/* 189 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 194 */       SharedContext shContext = this.m_collectionContext.getSharedContext();
/* 195 */       AppContextUtils.executeService(shContext, "DELETE_BATCH_FILE", props, false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 199 */       this.m_collectionContext.reportError(e);
/*     */     }
/*     */ 
/* 202 */     refreshList(filename);
/*     */   }
/*     */ 
/*     */   protected void editBatchFile()
/*     */   {
/* 207 */     String batchFile = this.m_batchList.getSelectedObj();
/* 208 */     if (batchFile == null)
/*     */     {
/* 210 */       return;
/*     */     }
/* 212 */     DataResultSet rset = this.m_collectionContext.getBatchFiles();
/* 213 */     boolean isTableView = true;
/*     */     try
/*     */     {
/* 216 */       String isDocViewStr = ResultSetUtils.findValue(rset, "aBatchFile", batchFile, "aIsTableBatch");
/* 217 */       isTableView = StringUtils.convertToBool(isDocViewStr, false);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/*     */     }
/*     */ 
/* 224 */     EditBatchDlg dlg = null;
/* 225 */     if (!isTableView)
/*     */     {
/* 227 */       dlg = new EditBatchDlg(this.m_helper.m_exchange.m_sysInterface, this.m_sys.getString("apLabelViewExportedContentItems"), this.m_collectionContext);
/*     */     }
/*     */     else
/*     */     {
/* 233 */       dlg = new EditTableBatchDlg(this.m_helper.m_exchange.m_sysInterface, this.m_sys.getString("apLabelViewExportedContentItems"), this.m_collectionContext);
/*     */     }
/*     */ 
/* 237 */     dlg.init(batchFile, this.m_filterInfo);
/*     */ 
/* 240 */     this.m_filterInfo = dlg.getFilterData();
/*     */ 
/* 242 */     refreshList(batchFile);
/*     */   }
/*     */ 
/*     */   protected void importBatchFile()
/*     */   {
/* 247 */     String batchFile = this.m_batchList.getSelectedObj();
/* 248 */     if (batchFile == null)
/*     */     {
/* 250 */       return;
/*     */     }
/*     */ 
/* 253 */     DataResultSet rset = this.m_collectionContext.getBatchFiles();
/* 254 */     DataResultSet batchRSet = new DataResultSet();
/* 255 */     batchRSet.copyFieldInfo(rset);
/* 256 */     int index = this.m_batchList.getSelectedIndex();
/* 257 */     Vector row = rset.getRowValues(index);
/* 258 */     batchRSet.addRow(row);
/*     */     try
/*     */     {
/* 262 */       DataBinder binder = new DataBinder();
/* 263 */       binder.addResultSet("BatchFile", batchRSet);
/* 264 */       SharedContext cxt = this.m_collectionContext.getSharedContext();
/* 265 */       AppContextUtils.executeService(cxt, "IMPORT_BATCHFILE", binder);
/* 266 */       MessageBox.doMessage(this.m_sys, IdcMessageFactory.lc("apImportBatchFileFinished", new Object[0]), 1);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 271 */       Report.trace("applet", null, e);
/* 272 */       MessageBox.reportError(this.m_sys, e, IdcMessageFactory.lc("apUnableToImportFile", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 278 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ViewBatchDlg
 * JD-Core Version:    0.5.4
 */