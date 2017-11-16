/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.workflow.WfScriptStorage;
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
/*     */ public class ScriptDlg
/*     */   implements ActionListener, ItemListener
/*     */ {
/*     */   protected SystemInterface m_systemInterface;
/*  61 */   protected ExecutionContext m_cxt = null;
/*     */   protected WorkflowContext m_context;
/*     */   protected DialogHelper m_helper;
/*  65 */   protected WorkflowStateInfo m_workflowInfo = null;
/*  66 */   protected DataResultSet m_scriptSet = null;
/*  67 */   protected JComboBox m_scriptChoice = null;
/*     */ 
/*  70 */   protected JButton m_editBtn = null;
/*  71 */   protected JButton m_deleteBtn = null;
/*     */ 
/*     */   public ScriptDlg(SystemInterface sys, String title)
/*     */   {
/*  75 */     this.m_systemInterface = sys;
/*  76 */     this.m_cxt = sys.getExecutionContext();
/*  77 */     this.m_helper = new DialogHelper(sys, title, true);
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  82 */     CustomPanel pnl = new CustomPanel();
/*  83 */     this.m_helper.makePanelGridBag(pnl, 1);
/*  84 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(15, 15, 5, 15);
/*     */ 
/*  86 */     this.m_scriptChoice = new CustomChoice();
/*  87 */     this.m_scriptChoice.addItemListener(this);
/*     */ 
/*  89 */     JTextArea jumpText = new CustomTextArea(5, 30);
/*  90 */     jumpText.setEnabled(false);
/*  91 */     CustomLabel desc = new CustomLabel();
/*  92 */     desc.setMinWidth(30);
/*     */ 
/*  94 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelName", this.m_cxt), this.m_scriptChoice, "wfScriptName");
/*     */ 
/*  96 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 15, 5, 15);
/*  97 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelDescription", this.m_cxt), desc, "wfScriptDescription");
/*     */ 
/*  99 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 15, 15, 15);
/* 100 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 101 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelSummary", this.m_cxt), jumpText, "wfScriptSummary");
/*     */ 
/* 104 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 105 */     mainPanel.setLayout(new BorderLayout());
/* 106 */     mainPanel.add("Center", pnl);
/*     */ 
/* 109 */     String[][] buttonInfo = { { LocaleResources.getString("apDlgButtonAdd", this.m_cxt), "add" }, { LocaleResources.getString("apDlgButtonEdit", this.m_cxt), "edit" }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "delete" }, { LocaleResources.getString("apLabelClose", this.m_cxt), "close" } };
/*     */ 
/* 116 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 118 */       String cmd = buttonInfo[i][1];
/* 119 */       JButton btn = this.m_helper.addCommandButton(buttonInfo[i][0], this);
/* 120 */       btn.setActionCommand(cmd);
/* 121 */       if (cmd.equals("edit"))
/*     */       {
/* 123 */         this.m_editBtn = btn;
/*     */       } else {
/* 125 */         if (!cmd.equals("delete"))
/*     */           continue;
/* 127 */         this.m_deleteBtn = btn;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 132 */     loadScriptChoice();
/* 133 */     checkSelection();
/*     */   }
/*     */ 
/*     */   public void loadScriptChoice()
/*     */   {
/* 138 */     this.m_scriptSet = SharedObjects.getTable("WorkflowScripts");
/* 139 */     if (this.m_scriptSet == null)
/*     */     {
/* 141 */       this.m_context.reportError(null, IdcMessageFactory.lc("apErrorRetrievingScriptList", new Object[0]));
/* 142 */       return;
/*     */     }
/*     */ 
/* 145 */     this.m_scriptChoice.removeAllItems();
/*     */ 
/* 147 */     boolean isEmpty = this.m_scriptSet.isEmpty();
/* 148 */     if (isEmpty)
/*     */     {
/* 150 */       this.m_scriptChoice.addItem(LocaleResources.getString("apChoiceNoValues", this.m_cxt));
/*     */     }
/*     */     else
/*     */     {
/* 154 */       for (this.m_scriptSet.first(); this.m_scriptSet.isRowPresent(); this.m_scriptSet.next())
/*     */       {
/* 156 */         String name = this.m_scriptSet.getStringValue(0);
/* 157 */         this.m_scriptChoice.addItem(name);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 162 */     this.m_scriptChoice.setEnabled(!isEmpty);
/* 163 */     this.m_editBtn.setEnabled(!isEmpty);
/* 164 */     this.m_deleteBtn.setEnabled(!isEmpty);
/*     */   }
/*     */ 
/*     */   public void prompt(WorkflowStateInfo wfInfo, WorkflowContext context)
/*     */   {
/* 169 */     this.m_workflowInfo = wfInfo;
/* 170 */     this.m_context = context;
/* 171 */     init();
/* 172 */     this.m_helper.show();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 177 */     boolean result = false;
/* 178 */     String name = null;
/* 179 */     String cmd = e.getActionCommand();
/* 180 */     if (cmd.equals("add"))
/*     */     {
/* 182 */       name = addOrEdit(true);
/*     */     }
/* 184 */     else if (cmd.equals("edit"))
/*     */     {
/* 186 */       name = addOrEdit(false);
/*     */     }
/* 188 */     else if (cmd.equals("delete"))
/*     */     {
/* 190 */       result = deleteScript();
/*     */     }
/* 192 */     else if (cmd.equals("close"))
/*     */     {
/* 194 */       this.m_helper.close();
/*     */     }
/*     */ 
/* 197 */     if ((name == null) && (!result))
/*     */       return;
/* 199 */     loadScriptChoice();
/* 200 */     loadScriptData(name);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 206 */     switch (e.getStateChange())
/*     */     {
/*     */     case 1:
/* 209 */       checkSelection();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 216 */     String name = (String)this.m_scriptChoice.getSelectedItem();
/* 217 */     if (name == null)
/*     */     {
/* 219 */       return;
/*     */     }
/*     */ 
/* 222 */     loadScriptData(name);
/*     */   }
/*     */ 
/*     */   protected void loadScriptData(String name)
/*     */   {
/* 227 */     DataResultSet drset = SharedObjects.getTable("WorkflowScripts");
/* 228 */     if ((drset == null) || (drset.isEmpty()))
/*     */     {
/* 231 */       this.m_helper.m_props = new Properties();
/*     */     }
/*     */     else
/*     */     {
/* 235 */       if (name != null)
/*     */       {
/* 237 */         Vector row = drset.findRow(0, name);
/* 238 */         if (row == null)
/*     */         {
/* 240 */           return;
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 246 */         drset.setCurrentRow(0);
/*     */       }
/*     */ 
/* 249 */       this.m_helper.m_props = drset.getCurrentRowProps();
/*     */     }
/*     */ 
/* 252 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected String addOrEdit(boolean isAdd)
/*     */   {
/* 257 */     String title = LocaleResources.getString("apTitleAddScript", this.m_cxt);
/* 258 */     String helpPage = "AddWfTemplateScript";
/*     */ 
/* 260 */     WfScriptStorage wfScript = null;
/* 261 */     if (!isAdd)
/*     */     {
/* 263 */       String name = (String)this.m_scriptChoice.getSelectedItem();
/* 264 */       Vector row = this.m_scriptSet.findRow(0, name);
/* 265 */       if (row == null)
/*     */       {
/* 268 */         return name;
/*     */       }
/*     */ 
/* 271 */       Properties props = this.m_scriptSet.getCurrentRowProps();
/* 272 */       title = LocaleResources.getString("apTitleEditScript2", this.m_cxt, name);
/* 273 */       helpPage = "EditWfTemplateScript";
/*     */       try
/*     */       {
/* 277 */         DataBinder binder = new DataBinder();
/* 278 */         binder.putLocal("wfScriptName", name);
/* 279 */         SharedContext shContext = this.m_context.getSharedContext();
/* 280 */         shContext.executeService("GET_WORKFLOW_SCRIPT", binder, false);
/*     */ 
/* 283 */         DataBinder.mergeHashTables(binder.getLocalData(), props);
/*     */ 
/* 286 */         wfScript = new WfScriptStorage(name);
/* 287 */         wfScript.setScriptData(binder);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 291 */         this.m_context.reportError(e);
/* 292 */         return name;
/*     */       }
/*     */     }
/*     */ 
/* 296 */     EditScriptDlg dlg = new EditScriptDlg(this.m_systemInterface, title, true, helpPage);
/* 297 */     dlg.init(wfScript, this.m_workflowInfo, false, this.m_context);
/*     */ 
/* 299 */     return dlg.getName();
/*     */   }
/*     */ 
/*     */   protected boolean deleteScript()
/*     */   {
/* 304 */     String name = this.m_helper.m_props.getProperty("wfScriptName");
/* 305 */     if ((name == null) || (name.length() == 0))
/*     */     {
/* 307 */       return false;
/*     */     }
/*     */ 
/* 310 */     IdcMessage msg = IdcMessageFactory.lc("apVerifyWorkflowScriptDelete", new Object[] { name });
/* 311 */     if (MessageBox.doMessage(this.m_systemInterface, msg, 4) == 3)
/*     */     {
/* 314 */       return false;
/*     */     }
/*     */ 
/* 317 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 320 */       binder.putLocal("wfScriptName", name);
/* 321 */       SharedContext shContext = this.m_context.getSharedContext();
/* 322 */       shContext.executeService("DELETE_WORKFLOW_SCRIPT", binder, false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 326 */       this.m_context.reportError(e);
/* 327 */       return false;
/*     */     }
/*     */ 
/* 330 */     this.m_scriptSet = ((DataResultSet)binder.getResultSet("WorkflowScripts"));
/* 331 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 336 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.ScriptDlg
 * JD-Core Version:    0.5.4
 */