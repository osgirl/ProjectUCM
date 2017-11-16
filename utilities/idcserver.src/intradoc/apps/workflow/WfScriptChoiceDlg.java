/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class WfScriptChoiceDlg
/*     */   implements ItemListener
/*     */ {
/*  47 */   protected SystemInterface m_systemInterface = null;
/*  48 */   protected ExecutionContext m_cxt = null;
/*  49 */   protected DialogHelper m_helper = null;
/*  50 */   protected String m_helpPage = null;
/*     */ 
/*  52 */   protected WorkflowContext m_context = null;
/*  53 */   protected JCheckBox m_scriptBox = null;
/*  54 */   protected JComboBox m_scriptChoice = null;
/*     */ 
/*  56 */   protected String[][] m_checkboxInfo = { { "apTitleEditCurrent", "isEdit", "1" }, { "apTitleCreateNew", "isNew", "0" }, { "apTitleUseScriptTemplate", "isCopy", "0" } };
/*     */ 
/*     */   public WfScriptChoiceDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  65 */     this.m_systemInterface = sys;
/*  66 */     this.m_cxt = sys.getExecutionContext();
/*  67 */     this.m_helper = new DialogHelper(sys, title, true);
/*  68 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(Properties props, WorkflowContext context, boolean isNew)
/*     */   {
/*  73 */     this.m_helper.m_props = props;
/*  74 */     this.m_context = context;
/*     */ 
/*  76 */     JPanel mainPanel = this.m_helper.initStandard(null, null, 1, true, DialogHelpTable.getHelpPage(this.m_helpPage));
/*     */ 
/*  79 */     if (!initUI(mainPanel, isNew))
/*     */     {
/*  81 */       this.m_helper.m_props.put("isNew", "1");
/*  82 */       return 1;
/*     */     }
/*  84 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public boolean initUI(JPanel mainPanel, boolean isNew)
/*     */   {
/*  92 */     this.m_scriptChoice = createChoiceList();
/*  93 */     if ((isNew) && (this.m_scriptChoice == null))
/*     */     {
/*  96 */       return false;
/*     */     }
/*     */ 
/*  99 */     this.m_helper.addPanelTitle(mainPanel, LocaleResources.getString("apLabelSelectEditChoice", this.m_cxt));
/*     */ 
/* 102 */     ButtonGroup cbGroup = new ButtonGroup();
/* 103 */     int len = this.m_checkboxInfo.length;
/* 104 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 106 */       if ((i == 0) && (isNew))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 111 */       boolean isSelected = StringUtils.convertToBool(this.m_checkboxInfo[i][2], false);
/* 112 */       if ((isNew) && (i == 1))
/*     */       {
/* 115 */         isSelected = true;
/*     */       }
/*     */ 
/* 118 */       if (i == 2)
/*     */       {
/* 121 */         if (this.m_scriptChoice == null)
/*     */           continue;
/* 123 */         JCheckBox cb = new CustomCheckbox(LocaleResources.getString(this.m_checkboxInfo[i][0], this.m_cxt), cbGroup, isSelected);
/*     */ 
/* 126 */         this.m_scriptBox = cb;
/* 127 */         cb.addItemListener(this);
/*     */ 
/* 129 */         this.m_helper.m_gridHelper.prepareAddRowElement();
/* 130 */         this.m_helper.addExchangeComponent(mainPanel, cb, this.m_checkboxInfo[i][1]);
/* 131 */         this.m_scriptChoice.setEnabled(false);
/* 132 */         this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 133 */         this.m_helper.addExchangeComponent(mainPanel, this.m_scriptChoice, "wfScriptName");
/*     */       }
/*     */       else
/*     */       {
/* 138 */         JCheckBox cb = new CustomCheckbox(LocaleResources.getString(this.m_checkboxInfo[i][0], this.m_cxt), cbGroup, isSelected);
/*     */ 
/* 141 */         cb.addItemListener(this);
/*     */ 
/* 143 */         this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 144 */         this.m_helper.addExchangeComponent(mainPanel, cb, this.m_checkboxInfo[i][1]);
/*     */       }
/*     */     }
/*     */ 
/* 148 */     return true;
/*     */   }
/*     */ 
/*     */   protected JComboBox createChoiceList()
/*     */   {
/* 153 */     DataResultSet scriptSet = SharedObjects.getTable("WorkflowScripts");
/* 154 */     if ((scriptSet == null) || (scriptSet.isEmpty()))
/*     */     {
/* 156 */       return null;
/*     */     }
/*     */ 
/* 159 */     JComboBox scriptChoice = new JComboBox();
/* 160 */     for (scriptSet.first(); scriptSet.isRowPresent(); scriptSet.next())
/*     */     {
/* 162 */       String name = scriptSet.getStringValue(0);
/* 163 */       scriptChoice.addItem(name);
/*     */     }
/* 165 */     return scriptChoice;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 173 */     if (this.m_scriptBox == null)
/*     */       return;
/* 175 */     boolean state = this.m_scriptBox.isSelected();
/* 176 */     this.m_scriptChoice.setEnabled(state);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 182 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WfScriptChoiceDlg
 * JD-Core Version:    0.5.4
 */