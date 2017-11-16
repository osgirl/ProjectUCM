/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.workflow.WfScriptStorage;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class WfStepScriptPanel extends EditViewBase
/*     */   implements ActionListener
/*     */ {
/*     */   public void initUI()
/*     */   {
/*  55 */     this.m_helper.makePanelGridBag(this, 1);
/*     */ 
/*  57 */     addScriptUI(LocaleResources.getString("apLabelEntry", this.m_cxt), "wfEntryScriptSummary", "Entry");
/*  58 */     addScriptUI(LocaleResources.getString("apLabelUpdate", this.m_cxt), "wfUpdateScriptSummary", "Update");
/*  59 */     addScriptUI(LocaleResources.getString("apLabelExit", this.m_cxt), "wfExitScriptSummary", "Exit");
/*     */   }
/*     */ 
/*     */   protected void addScriptUI(String title, String fieldName, String cmdPrefix)
/*     */   {
/*  64 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 0, 0, 0);
/*  65 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  66 */     Component comp = new CustomTextArea(4, 30);
/*  67 */     this.m_helper.addLabelFieldPairEx(this, title, comp, fieldName, false);
/*  68 */     comp.setEnabled(false);
/*     */ 
/*  71 */     JPanel btnPanel = new PanePanel();
/*  72 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/*  74 */     JButton editBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/*  75 */     editBtn.setActionCommand("edit" + cmdPrefix);
/*  76 */     editBtn.addActionListener(this);
/*     */ 
/*  78 */     JButton clearBtn = new JButton(LocaleResources.getString("apTitleClear", this.m_cxt));
/*  79 */     clearBtn.setActionCommand("clear" + cmdPrefix);
/*  80 */     clearBtn.addActionListener(this);
/*     */ 
/*  82 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 5, 5, 5);
/*  83 */     this.m_helper.addLastComponentInRow(btnPanel, editBtn);
/*  84 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 5, 5, 5);
/*  85 */     this.m_helper.addLastComponentInRow(btnPanel, clearBtn);
/*     */ 
/*  87 */     this.m_helper.addLastComponentInRow(this, btnPanel);
/*     */   }
/*     */ 
/*     */   public void load()
/*     */   {
/*  94 */     createSummaries();
/*  95 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 101 */     this.m_helper.retrieveComponentValues();
/* 102 */     String stepName = this.m_workflowInfo.get("dWfStepName");
/*     */ 
/* 104 */     String cmd = e.getActionCommand();
/* 105 */     if (cmd.startsWith("edit"))
/*     */     {
/* 107 */       String type = null;
/* 108 */       String title = null;
/* 109 */       String column = null;
/* 110 */       if (cmd.equals("editEntry"))
/*     */       {
/* 112 */         title = LocaleResources.getString("apTitleEditEntryScript", this.m_cxt, stepName);
/* 113 */         type = "entry";
/* 114 */         column = "wfEntryScript";
/*     */       }
/* 116 */       else if (cmd.equals("editExit"))
/*     */       {
/* 118 */         title = LocaleResources.getString("apTitleEditExitScript", this.m_cxt, stepName);
/* 119 */         type = "exit";
/* 120 */         column = "wfExitScript";
/*     */       }
/* 122 */       else if (cmd.equals("editUpdate"))
/*     */       {
/* 124 */         title = LocaleResources.getString("apTitleEditUpdateScript", this.m_cxt, stepName);
/* 125 */         type = "update";
/* 126 */         column = "wfUpdateScript";
/*     */       }
/*     */ 
/* 129 */       Properties props = new Properties();
/*     */ 
/* 131 */       String scriptID = type;
/* 132 */       props.put("ScriptID", scriptID);
/* 133 */       props.put("ScriptColumn", column);
/*     */ 
/* 135 */       String val = this.m_helper.m_props.getProperty(column);
/* 136 */       boolean isNew = (val == null) || (val.length() == 0);
/*     */ 
/* 138 */       WfScriptChoiceDlg dlg = new WfScriptChoiceDlg(this.m_systemInterface, title, "EditStepScript");
/*     */ 
/* 140 */       if (dlg.init(props, this.m_context, isNew) == 1)
/*     */       {
/* 142 */         editScript(type, props);
/*     */       }
/*     */     } else {
/* 145 */       if (!cmd.startsWith("clear")) {
/*     */         return;
/*     */       }
/* 148 */       String script = null;
/* 149 */       String summary = null;
/* 150 */       String rsName = null;
/* 151 */       if (cmd.equals("clearEntry"))
/*     */       {
/* 153 */         script = "wfEntryScript";
/* 154 */         summary = "wfEntryScriptSummary";
/* 155 */         rsName = "entry_WorkflowScriptJumps";
/*     */       }
/* 157 */       else if (cmd.equals("clearExit"))
/*     */       {
/* 159 */         script = "wfExitScript";
/* 160 */         summary = "wfExitScriptSummary";
/* 161 */         rsName = "update_WorkflowScriptJumps";
/*     */       }
/* 163 */       else if (cmd.equals("clearUpdate"))
/*     */       {
/* 165 */         script = "wfUpdateScript";
/* 166 */         summary = "wfUpdateScriptSummary";
/* 167 */         rsName = "exit_WorkflowScriptJumps";
/*     */       }
/* 169 */       this.m_workflowInfo.setValue(script, "");
/* 170 */       this.m_helper.m_props.put(summary, "");
/* 171 */       this.m_helper.m_exchange.setComponentValue(summary, "");
/* 172 */       this.m_workflowInfo.getWorkflowData().removeResultSet(rsName);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void editScript(String type, Properties props)
/*     */   {
/* 180 */     boolean isNew = StringUtils.convertToBool(props.getProperty("isNew"), false);
/* 181 */     boolean isCopy = StringUtils.convertToBool(props.getProperty("isCopy"), false);
/*     */ 
/* 183 */     String baseScript = null;
/*     */ 
/* 185 */     boolean isFresh = false;
/* 186 */     if (isNew)
/*     */     {
/* 188 */       isFresh = true;
/*     */     }
/* 190 */     else if (isCopy)
/*     */     {
/* 192 */       baseScript = props.getProperty("wfScriptName");
/*     */     }
/* 194 */     editStepScript(type, isFresh, baseScript, props);
/*     */   }
/*     */ 
/*     */   protected void editStepScript(String type, boolean isNew, String baseScript, Properties props)
/*     */   {
/* 199 */     String title = LocaleResources.getString("apTitleEditScript", this.m_cxt);
/* 200 */     String scriptID = props.getProperty("ScriptID");
/* 201 */     WfScriptStorage wfScript = new WfScriptStorage(scriptID);
/* 202 */     String helpPage = "EditWorkflowScript";
/*     */ 
/* 204 */     if (isNew)
/*     */     {
/* 207 */       wfScript = new WfScriptStorage(scriptID);
/*     */     }
/* 209 */     else if (baseScript != null)
/*     */     {
/* 212 */       DataResultSet scriptSet = SharedObjects.getTable("WorkflowScripts");
/* 213 */       Vector row = scriptSet.findRow(0, baseScript);
/* 214 */       if (row == null)
/*     */       {
/* 216 */         this.m_context.reportError(null, IdcMessageFactory.lc("apCannotFindScriptInScriptList", new Object[0]));
/* 217 */         return;
/*     */       }
/* 219 */       Properties rowProps = scriptSet.getCurrentRowProps();
/*     */       try
/*     */       {
/* 223 */         DataBinder binder = new DataBinder();
/* 224 */         binder.putLocal("wfScriptName", baseScript);
/* 225 */         SharedContext shContext = this.m_context.getSharedContext();
/* 226 */         shContext.executeService("GET_WORKFLOW_SCRIPT", binder, false);
/*     */ 
/* 229 */         DataBinder.mergeHashTables(binder.getLocalData(), rowProps);
/*     */ 
/* 232 */         wfScript.setScriptData(binder);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 236 */         this.m_context.reportError(e);
/* 237 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 243 */       extractScriptInfo(scriptID, wfScript);
/*     */     }
/*     */ 
/* 246 */     EditScriptDlg dlg = new EditScriptDlg(this.m_systemInterface, title, false, helpPage);
/* 247 */     if (dlg.init(wfScript, this.m_workflowInfo, this.m_isNew, this.m_context) != 1) {
/*     */       return;
/*     */     }
/* 250 */     DataBinder wfData = this.m_workflowInfo.getWorkflowData();
/* 251 */     DataBinder scriptData = wfScript.getScriptData();
/*     */ 
/* 254 */     ResultSet rset = scriptData.getResultSet("WorkflowScriptJumps");
/* 255 */     wfData.addResultSet(scriptID + "_WorkflowScriptJumps", rset);
/*     */ 
/* 258 */     String[] keys = { "wfIsCustomScript", "wfCustomScript" };
/* 259 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 261 */       String key = keys[i];
/* 262 */       String val = scriptData.getLocal(key);
/* 263 */       if (val == null)
/*     */       {
/* 265 */         val = "";
/*     */       }
/*     */ 
/* 268 */       this.m_workflowInfo.setValue(scriptID + "_" + key, val);
/*     */     }
/*     */ 
/* 271 */     String column = props.getProperty("ScriptColumn");
/* 272 */     this.m_workflowInfo.setValue(column, scriptID);
/*     */ 
/* 275 */     String prefix = scriptID + "_";
/* 276 */     String summary = WorkflowScriptUtils.computeScriptString(type, prefix, wfData, true);
/* 277 */     this.m_helper.m_props.put(column + "Summary", summary);
/* 278 */     this.m_helper.m_exchange.setComponentValue(column + "Summary", summary);
/*     */   }
/*     */ 
/*     */   protected void extractScriptInfo(String type, WfScriptStorage wfScript)
/*     */   {
/* 284 */     DataBinder wfData = this.m_workflowInfo.getWorkflowData();
/* 285 */     DataBinder binder = new DataBinder();
/*     */ 
/* 287 */     ResultSet rset = wfData.getResultSet(type + "_WorkflowScriptJumps");
/* 288 */     if (rset == null)
/*     */       return;
/* 290 */     DataResultSet copyRS = new DataResultSet();
/* 291 */     copyRS.copy(rset);
/*     */ 
/* 299 */     DataResultSet tempSet = new DataResultSet(WorkflowScriptUtils.WF_JUMP_COLUMNS);
/* 300 */     copyRS.mergeFields(tempSet);
/* 301 */     binder.addResultSet("WorkflowScriptJumps", copyRS);
/*     */ 
/* 303 */     String[] keys = { "wfIsCustomScript", "wfCustomScript" };
/* 304 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 306 */       String key = type + "_" + keys[i];
/* 307 */       String val = wfData.getLocal(key);
/* 308 */       if (val == null)
/*     */       {
/* 310 */         val = "";
/*     */       }
/* 312 */       binder.putLocal(keys[i], val);
/*     */     }
/* 314 */     wfScript.setScriptData(binder);
/*     */   }
/*     */ 
/*     */   protected void createSummaries()
/*     */   {
/* 320 */     String[][] types = { { "entry", "wfEntryScriptSummary" }, { "exit", "wfExitScriptSummary" }, { "update", "wfUpdateScriptSummary" } };
/*     */ 
/* 326 */     DataBinder wfData = this.m_workflowInfo.getWorkflowData();
/* 327 */     for (int i = 0; i < types.length; ++i)
/*     */     {
/* 329 */       String prefix = types[i][0] + "_";
/* 330 */       String summary = WorkflowScriptUtils.computeScriptString(types[i][0], prefix, wfData, true);
/* 331 */       this.m_helper.m_props.put(types[i][1], summary);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 337 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WfStepScriptPanel
 * JD-Core Version:    0.5.4
 */