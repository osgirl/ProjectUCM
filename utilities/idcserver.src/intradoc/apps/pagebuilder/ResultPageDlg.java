/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.ResultData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ 
/*     */ public class ResultPageDlg
/*     */   implements ActionListener, ItemListener
/*     */ {
/*     */   public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80412 $";
/*     */   public SystemInterface m_sysInterface;
/*  66 */   protected DataResultSet m_resultData = null;
/*  67 */   protected ResultData m_currentResult = null;
/*     */   protected CustomLabel m_text1;
/*     */   protected JTextArea m_text2;
/*     */   protected CustomLabel m_desc;
/*     */   protected JButton m_addBtn;
/*     */   protected JButton m_editBtn;
/*     */   protected JButton m_deleteBtn;
/*     */   protected JButton m_closeBtn;
/*     */   protected JComboBox m_resultChoice;
/*     */   public DialogHelper m_helper;
/*  84 */   protected ExecutionContext m_ctx = null;
/*     */ 
/*     */   public ResultPageDlg(SystemInterface sys, String title)
/*     */   {
/*  88 */     this.m_currentResult = new ResultData();
/*  89 */     this.m_sysInterface = sys;
/*  90 */     this.m_helper = new DialogHelper(sys, title, true);
/*  91 */     this.m_ctx = sys.getExecutionContext();
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  96 */     CustomPanel pnl = new CustomPanel();
/*  97 */     this.m_helper.makePanelGridBag(pnl, 1);
/*  98 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(15, 15, 5, 15);
/*     */ 
/* 100 */     this.m_resultChoice = new CustomChoice();
/* 101 */     this.m_resultChoice.addItemListener(this);
/*     */ 
/* 103 */     this.m_text1 = new CustomLabel();
/* 104 */     this.m_text1.setMinWidth(30);
/* 105 */     this.m_text2 = new CustomTextArea(5, 30);
/* 106 */     this.m_text2.setEnabled(false);
/* 107 */     this.m_desc = new CustomLabel();
/* 108 */     this.m_desc.setMinWidth(30);
/* 109 */     this.m_desc.setUseLocale(true, this.m_ctx);
/*     */ 
/* 111 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelName", this.m_ctx), this.m_resultChoice, "name");
/*     */ 
/* 113 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 15, 5, 15);
/* 114 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelDescription", this.m_ctx), this.m_desc, "description");
/*     */ 
/* 116 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelText1", this.m_ctx), this.m_text1, "Text1");
/*     */ 
/* 118 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 15, 15, 15);
/* 119 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 120 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelText2", this.m_ctx), this.m_text2, "Text2");
/*     */ 
/* 123 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 124 */     mainPanel.setLayout(new BorderLayout());
/* 125 */     mainPanel.add("Center", pnl);
/*     */ 
/* 128 */     this.m_addBtn = this.m_helper.addCommandButton(LocaleResources.getString("apDlgButtonAdd", this.m_ctx), this);
/*     */ 
/* 130 */     this.m_editBtn = this.m_helper.addCommandButton(LocaleResources.getString("apDlgButtonEdit", this.m_ctx), this);
/*     */ 
/* 132 */     this.m_deleteBtn = this.m_helper.addCommandButton(LocaleResources.getString("apLabelDelete", this.m_ctx), this);
/*     */ 
/* 134 */     this.m_closeBtn = this.m_helper.addCommandButton(LocaleResources.getString("apLabelClose", this.m_ctx), this);
/*     */ 
/* 138 */     loadResultData(null);
/* 139 */     if (this.m_resultChoice.getItemCount() <= 0)
/*     */       return;
/* 141 */     setSelection(0);
/*     */   }
/*     */ 
/*     */   public void loadResultData(String sel)
/*     */   {
/* 147 */     this.m_resultData = SharedObjects.getTable("CurrentVerityTemplates");
/* 148 */     if (this.m_resultData == null)
/*     */     {
/* 150 */       MessageBox.reportError(this.m_sysInterface, IdcMessageFactory.lc("apErrorRetrievingTemplateList", new Object[0]));
/* 151 */       return;
/*     */     }
/*     */ 
/* 154 */     this.m_resultChoice.removeAllItems();
/* 155 */     int selIndex = -1;
/* 156 */     int count = 0;
/* 157 */     for (this.m_resultData.first(); this.m_resultData.isRowPresent(); ++count)
/*     */     {
/* 159 */       Vector data = this.m_resultData.getCurrentRowValues();
/* 160 */       String name = (String)data.elementAt(0);
/* 161 */       this.m_resultChoice.addItem(name);
/* 162 */       if ((sel != null) && (sel.equals(name)))
/*     */       {
/* 164 */         selIndex = count;
/*     */       }
/* 157 */       this.m_resultData.next();
/*     */     }
/*     */ 
/* 167 */     setSelection(selIndex);
/*     */   }
/*     */ 
/*     */   public void prompt()
/*     */   {
/* 172 */     init();
/* 173 */     this.m_helper.show();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 178 */     boolean result = false;
/* 179 */     String name = null;
/* 180 */     Object src = e.getSource();
/* 181 */     if (src == this.m_addBtn)
/*     */     {
/* 183 */       name = addOrEdit(true);
/*     */     }
/* 185 */     else if (src == this.m_editBtn)
/*     */     {
/* 187 */       name = addOrEdit(false);
/*     */     }
/* 189 */     else if (src == this.m_deleteBtn)
/*     */     {
/* 191 */       result = deletePage();
/*     */     }
/* 193 */     else if (src == this.m_closeBtn)
/*     */     {
/* 195 */       this.m_helper.close();
/*     */     }
/*     */ 
/* 198 */     if ((name == null) && (!result))
/*     */       return;
/* 200 */     loadResultData(name);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 206 */     checkSelection();
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 211 */     int index = this.m_resultChoice.getSelectedIndex();
/* 212 */     boolean isSelected = index >= 0;
/* 213 */     this.m_editBtn.setEnabled(isSelected);
/* 214 */     this.m_deleteBtn.setEnabled(isSelected);
/*     */ 
/* 216 */     if (!isSelected)
/*     */       return;
/* 218 */     setSelection(index);
/*     */   }
/*     */ 
/*     */   protected void setSelection(int index)
/*     */   {
/* 224 */     if (index < 0)
/*     */     {
/* 226 */       return;
/*     */     }
/*     */ 
/* 229 */     if (this.m_resultChoice.getSelectedIndex() != index)
/*     */     {
/* 231 */       this.m_resultChoice.setSelectedIndex(index);
/*     */     }
/*     */ 
/* 234 */     this.m_resultData.setCurrentRow(index);
/* 235 */     Properties p = this.m_resultData.getCurrentRowProps();
/* 236 */     this.m_currentResult.setValues(p);
/* 237 */     this.m_text1.setText(this.m_currentResult.get("Text1"));
/* 238 */     this.m_text2.setText(this.m_currentResult.get("Text2"));
/* 239 */     this.m_desc.setText(this.m_currentResult.get("description"));
/*     */   }
/*     */ 
/*     */   protected String addOrEdit(boolean isAdd)
/*     */   {
/* 244 */     String title = LocaleResources.getString("apTitleAddResultPage", this.m_ctx);
/* 245 */     ResultData data = null;
/* 246 */     String helpPageName = "AddResultPage";
/*     */ 
/* 248 */     if (!isAdd)
/*     */     {
/* 250 */       title = LocaleResources.getString("apTitleEditResultPage", this.m_ctx, this.m_currentResult.get("name"));
/*     */ 
/* 252 */       data = this.m_currentResult;
/* 253 */       helpPageName = "EditResultPage";
/*     */     }
/* 255 */     EditResultPage dlg = new EditResultPage(this.m_sysInterface, title, DialogHelpTable.getHelpPage(helpPageName));
/*     */ 
/* 257 */     dlg.init(data);
/*     */ 
/* 259 */     dlg.prompt();
/*     */ 
/* 261 */     ResultData newData = dlg.getData();
/* 262 */     return newData.get("name");
/*     */   }
/*     */ 
/*     */   protected boolean deletePage()
/*     */   {
/* 267 */     String name = this.m_currentResult.get("name");
/*     */ 
/* 269 */     if (name.equalsIgnoreCase("StandardResults"))
/*     */     {
/* 271 */       MessageBox.reportError(this.m_sysInterface, IdcMessageFactory.lc("apCannotDeleteResultTemplate", new Object[] { name }));
/*     */ 
/* 273 */       return false;
/*     */     }
/*     */ 
/* 276 */     IdcMessage msg = IdcMessageFactory.lc("apVerifyPageDelete", new Object[] { name });
/* 277 */     if (MessageBox.doMessage(this.m_sysInterface, msg, 2) == 0)
/*     */     {
/* 280 */       return false;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 285 */       DataResultSet updateList = new DataResultSet();
/* 286 */       DataResultSet drset = SharedObjects.getTable("CurrentVerityTemplates");
/* 287 */       updateList.copyFieldInfo(drset);
/* 288 */       Properties values = this.m_currentResult.getValues();
/* 289 */       PropParameters params = new PropParameters(values);
/* 290 */       Vector v = updateList.createRow(params);
/* 291 */       updateList.addRow(v);
/*     */ 
/* 293 */       DataBinder binder = new DataBinder();
/* 294 */       binder.addResultSet("ResultPageUpdates", updateList);
/* 295 */       AppLauncher.executeService("DELETE_RESULT_TEMPLATE", binder);
/* 296 */       this.m_resultChoice.setSelectedIndex(0);
/* 297 */       checkSelection();
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 301 */       MessageBox.reportError(this.m_sysInterface, exp);
/* 302 */       return false;
/*     */     }
/* 304 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 309 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80412 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.ResultPageDlg
 * JD-Core Version:    0.5.4
 */