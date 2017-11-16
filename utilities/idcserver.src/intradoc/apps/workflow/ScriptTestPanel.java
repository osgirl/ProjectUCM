/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.shared.gui.ViewDlg;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class ScriptTestPanel extends ScriptPanelBase
/*     */ {
/*     */   protected JTextField m_idValue;
/*     */ 
/*     */   public ScriptTestPanel()
/*     */   {
/*  65 */     this.m_idValue = null;
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  72 */     Properties props = new Properties();
/*  73 */     this.m_helper.m_props = props;
/*  74 */     if (this.m_isTemplate)
/*     */     {
/*  76 */       props.put("IsTemplateScript", "1");
/*     */     }
/*     */     else
/*     */     {
/*  80 */       props.put("IsTemplateScript", "0");
/*  81 */       props.put("IsNewStep", "" + this.m_isNewStep);
/*  82 */       String stepName = this.m_workflowInfo.get("dWfStepName");
/*  83 */       if ((stepName == null) || (stepName.length() == 0))
/*     */       {
/*  86 */         stepName = "";
/*     */       }
/*  88 */       props.put("dWfStepName", stepName);
/*     */ 
/*  92 */       String wfName = this.m_workflowInfo.get("dWfName");
/*  93 */       if (wfName == null)
/*     */       {
/*  95 */         wfName = "";
/*     */       }
/*  97 */       props.put("dWfName", wfName);
/*     */     }
/*     */ 
/* 100 */     Date dte = new Date();
/* 101 */     String str = LocaleUtils.formatODBC(dte);
/*     */ 
/* 103 */     String inputDefaults = "lastEntryTs=" + str + "\nentryCount=1";
/* 104 */     props.put("ScriptInput", inputDefaults);
/*     */ 
/* 107 */     JPanel idPanel = new PanePanel();
/* 108 */     this.m_helper.makePanelGridBag(idPanel, 2);
/*     */ 
/* 110 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 111 */     gh.m_gc.insets = new Insets(0, 5, 0, 5);
/* 112 */     gh.m_gc.anchor = 18;
/* 113 */     gh.m_gc.weightx = 1.0D;
/*     */ 
/* 115 */     this.m_idValue = new CustomTextField(30);
/* 116 */     this.m_helper.addLabelFieldPairEx(idPanel, LocaleResources.getString("apLabelContentId", this.m_cxt), this.m_idValue, "dDocName", false);
/*     */ 
/* 118 */     JButton selButton = new JButton(LocaleResources.getString("apDlgButtonSelect", this.m_cxt));
/* 119 */     selButton.setActionCommand("select");
/* 120 */     selButton.addActionListener(this);
/*     */ 
/* 122 */     gh.m_gc.weightx = 0.1D;
/* 123 */     gh.m_gc.insets = new Insets(0, 20, 0, 5);
/* 124 */     this.m_helper.addLastComponentInRow(idPanel, selButton);
/*     */ 
/* 126 */     JPanel inputPanel = new CustomPanel();
/* 127 */     this.m_helper.makePanelGridBag(inputPanel, 1);
/* 128 */     gh.m_gc.insets = new Insets(0, 5, 0, 5);
/* 129 */     gh.m_gc.weightx = 1.0D;
/* 130 */     this.m_helper.addLastComponentInRow(inputPanel, new CustomLabel(LocaleResources.getString("apTitleInputData", this.m_cxt), 1));
/*     */ 
/* 132 */     gh.prepareAddLastRowElement();
/* 133 */     gh.m_gc.weighty = 1.0D;
/* 134 */     this.m_helper.addExchangeComponent(inputPanel, new CustomTextArea(5, 20), "ScriptInput");
/*     */ 
/* 137 */     String[][] buttonInfo = { { LocaleResources.getString("apDlgButtonSelectWorkflow", this.m_cxt), "loadWorkflow" }, { LocaleResources.getString("apTitleLoadItemsWorkflowState", this.m_cxt), "loadState" }, { LocaleResources.getString("apTitleTestScript", this.m_cxt), "test" } };
/*     */ 
/* 144 */     gh.prepareAddRowElement();
/* 145 */     gh.m_gc.weighty = 0.0D;
/* 146 */     this.m_helper.addComponent(inputPanel, new PanePanel());
/*     */ 
/* 148 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 150 */       if ((i == 0) && (!this.m_isTemplate)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 154 */       JButton btn = new JButton(buttonInfo[i][0]);
/* 155 */       btn.setActionCommand(buttonInfo[i][1]);
/* 156 */       btn.addActionListener(this);
/* 157 */       this.m_helper.addComponent(inputPanel, btn);
/*     */     }
/*     */ 
/* 160 */     gh.prepareAddLastRowElement();
/* 161 */     this.m_helper.addComponent(inputPanel, new PanePanel());
/*     */ 
/* 163 */     JPanel resultPanel = new CustomPanel();
/* 164 */     this.m_helper.makePanelGridBag(resultPanel, 1);
/* 165 */     gh.m_gc.weightx = 1.0D;
/* 166 */     gh.m_gc.weighty = 0.0D;
/* 167 */     this.m_helper.addLastComponentInRow(resultPanel, new CustomLabel(LocaleResources.getString("apLabelResults", this.m_cxt), 1));
/*     */ 
/* 169 */     gh.m_gc.weighty = 1.0D;
/* 170 */     this.m_helper.addExchangeComponent(resultPanel, new CustomTextArea(10, 20), "ScriptResults");
/*     */ 
/* 172 */     gh.m_gc.insets = new Insets(0, 0, 0, 0);
/* 173 */     gh.m_gc.weightx = 0.1D;
/* 174 */     gh.m_gc.weighty = 0.0D;
/* 175 */     this.m_helper.addLastComponentInRow(this, idPanel);
/* 176 */     gh.m_gc.fill = 1;
/* 177 */     gh.m_gc.weightx = 1.0D;
/* 178 */     gh.m_gc.weighty = 1.0D;
/* 179 */     this.m_helper.addLastComponentInRow(this, inputPanel);
/* 180 */     this.m_helper.addLastComponentInRow(this, resultPanel);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 189 */     String cmd = e.getActionCommand();
/* 190 */     if (cmd.equals("select"))
/*     */     {
/* 192 */       ViewData viewData = null;
/* 193 */       String key = null;
/* 194 */       String title = null;
/* 195 */       String helpPage = null;
/*     */ 
/* 197 */       viewData = new ViewData(1);
/* 198 */       viewData.m_viewName = "DocSelectView";
/* 199 */       key = "dDocName";
/* 200 */       title = LocaleResources.getString("apContentItemView", this.m_cxt);
/* 201 */       helpPage = "SelectDocument";
/* 202 */       viewData.m_isViewOnly = false;
/* 203 */       ViewDlg viewDlg = new ViewDlg(null, this.m_systemInterface, title, this.m_context.getSharedContext(), DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 206 */       viewDlg.init(viewData, null);
/* 207 */       if (viewDlg.prompt() == 1)
/*     */       {
/* 209 */         Vector v = viewDlg.computeSelectedValues(key, false);
/* 210 */         if (v.size() > 0)
/*     */         {
/* 212 */           String value = (String)v.elementAt(0);
/* 213 */           this.m_idValue.setText(value);
/*     */         }
/*     */       }
/*     */     }
/* 217 */     else if (cmd.equals("loadWorkflow"))
/*     */     {
/* 219 */       loadWorkflowInfo();
/*     */     }
/* 221 */     else if (cmd.equals("loadState"))
/*     */     {
/* 223 */       loadWorkflowState();
/*     */     } else {
/* 225 */       if (!cmd.equals("test"))
/*     */         return;
/* 227 */       doTest();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void loadWorkflowInfo()
/*     */   {
/* 233 */     this.m_helper.retrieveComponentValues();
/*     */ 
/* 235 */     SelectStepDlg dlg = new SelectStepDlg(this.m_systemInterface, LocaleResources.getString("apTitleSelectWorkflowStep", this.m_cxt), "SelectWorkflowTestStep");
/*     */ 
/* 237 */     if (dlg.init(null, this.m_context) != 1)
/*     */       return;
/* 239 */     Properties props = dlg.getProperties();
/* 240 */     String wfStep = props.getProperty("wfJumpTargetStep");
/*     */ 
/* 243 */     Properties newProps = new Properties();
/* 244 */     newProps.put("WorkflowStepForTest", wfStep);
/* 245 */     updateInput(newProps);
/*     */ 
/* 247 */     this.m_helper.m_props.put("ScriptResults", "");
/* 248 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected void loadWorkflowState()
/*     */   {
/* 254 */     this.m_helper.retrieveComponentValues();
/*     */ 
/* 257 */     Properties props = this.m_helper.m_props;
/* 258 */     String docName = props.getProperty("dDocName");
/* 259 */     if ((docName == null) || (docName.length() == 0))
/*     */     {
/* 261 */       this.m_context.reportError(null, IdcMessageFactory.lc("apSelectContentItem", new Object[0]));
/* 262 */       return;
/*     */     }
/*     */ 
/* 265 */     DataBinder binder = executeCommand("GET_WF_COMPANION_INFO", props);
/* 266 */     if (binder != null)
/*     */     {
/* 269 */       String dataStr = binder.getLocal("WfCompanionData");
/* 270 */       if ((dataStr != null) && (dataStr.length() > 0))
/*     */       {
/* 272 */         DataBinder cmpData = new DataBinder();
/* 273 */         BufferedReader br = new BufferedReader(new StringReader(dataStr));
/*     */         try
/*     */         {
/* 276 */           cmpData.receive(br);
/* 277 */           updateInput(cmpData.getLocalData());
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/* 281 */           this.m_context.reportError(e, IdcMessageFactory.lc("apErrorParsingCompanionData", new Object[0]));
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 289 */       String[] keys = { "entryCount", "lastEntryTs", "WorkflowStepForTest" };
/* 290 */       clearInput(keys);
/*     */     }
/*     */ 
/* 294 */     props.put("ScriptResults", "");
/* 295 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected void doTest()
/*     */   {
/* 300 */     this.m_helper.retrieveComponentValues();
/* 301 */     DataBinder.mergeHashTables(this.m_helper.m_props, this.m_scriptData.getLocalData());
/*     */ 
/* 303 */     Properties props = this.m_helper.m_props;
/* 304 */     String docName = props.getProperty("dDocName");
/* 305 */     if ((docName == null) || (docName.length() == 0))
/*     */     {
/* 307 */       this.m_context.reportError(null, IdcMessageFactory.lc("apSelectContentItem", new Object[0]));
/* 308 */       return;
/*     */     }
/* 310 */     DataBinder binder = executeCommand("TEST_WORKFLOW_SCRIPT", props);
/* 311 */     if (binder == null)
/*     */       return;
/* 313 */     DataBinder.mergeHashTables(props, binder.getLocalData());
/*     */ 
/* 316 */     String str = binder.getLocal("ScriptResults");
/* 317 */     createResultsPresentation(str);
/* 318 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected void createResultsPresentation(String str)
/*     */   {
/* 326 */     DataBinder rResults = new DataBinder();
/* 327 */     BufferedReader br = new BufferedReader(new StringReader(str));
/*     */     try
/*     */     {
/* 330 */       rResults.receive(br);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 334 */       return;
/*     */     }
/*     */ 
/* 337 */     StringBuffer buff = new StringBuffer();
/* 338 */     Properties props = rResults.getLocalData();
/* 339 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*     */     {
/* 341 */       String key = (String)en.nextElement();
/* 342 */       String value = props.getProperty(key);
/*     */ 
/* 344 */       if (key.equals("StatusCode")) continue; if (key.startsWith("bl"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 349 */       if (buff.length() > 0)
/*     */       {
/* 351 */         buff.append("\n");
/*     */       }
/* 353 */       if (!key.equals("StatusMessage"))
/*     */       {
/* 355 */         buff.append(key);
/* 356 */         buff.append("=");
/*     */       }
/* 358 */       buff.append(value);
/*     */     }
/*     */ 
/* 361 */     ResultSet rset = rResults.getResultSet("MailQueue");
/* 362 */     if ((rset != null) && (rset.isRowPresent()))
/*     */     {
/* 364 */       buff.append("\n\n");
/* 365 */       buff.append(LocaleResources.getString("apLabelMailSentTo", this.m_cxt));
/* 366 */       buff.append('\n');
/* 367 */       for (; rset.isRowPresent(); rset.next())
/*     */       {
/* 369 */         buff.append(rset.getStringValue(0));
/* 370 */         buff.append('\n');
/*     */       }
/*     */     }
/*     */ 
/* 374 */     this.m_helper.m_props.put("ScriptResults", buff.toString());
/*     */   }
/*     */ 
/*     */   protected DataBinder executeCommand(String cmd, Properties props)
/*     */   {
/* 382 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 385 */       binder.setLocalData(props);
/* 386 */       if ((this.m_workflowInfo != null) && (this.m_workflowInfo.m_wfData != null))
/*     */       {
/* 388 */         Properties wfProps = this.m_workflowInfo.m_wfData.getLocalData();
/* 389 */         DataBinder.mergeHashTables(props, wfProps);
/*     */       }
/* 391 */       binder.merge(this.m_scriptData);
/*     */ 
/* 394 */       binder.putLocal("TestWfScript", "1");
/*     */ 
/* 396 */       SharedContext shContext = this.m_context.getSharedContext();
/* 397 */       shContext.executeService(cmd, binder, false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 401 */       this.m_context.reportError(e);
/* 402 */       return null;
/*     */     }
/*     */ 
/* 405 */     return binder;
/*     */   }
/*     */ 
/*     */   protected void updateInput(Properties props)
/*     */   {
/* 410 */     String str = this.m_helper.m_props.getProperty("ScriptInput");
/*     */ 
/* 412 */     Properties inputProps = new Properties();
/* 413 */     Vector orderedInput = WorkflowScriptUtils.parseScriptInput(str, inputProps);
/*     */ 
/* 415 */     DataBinder.mergeHashTables(inputProps, props);
/*     */ 
/* 417 */     Vector mergedInput = new IdcVector();
/*     */ 
/* 420 */     int size = orderedInput.size();
/* 421 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 423 */       String name = (String)orderedInput.elementAt(i);
/* 424 */       String value = inputProps.getProperty(name);
/* 425 */       mergedInput.addElement(name + "=" + value);
/* 426 */       inputProps.remove(name);
/*     */     }
/*     */ 
/* 430 */     for (Enumeration en = inputProps.keys(); en.hasMoreElements(); )
/*     */     {
/* 432 */       String name = (String)en.nextElement();
/* 433 */       if (name.startsWith("bl"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 438 */       String value = inputProps.getProperty(name);
/* 439 */       mergedInput.addElement(name + "=" + value);
/*     */     }
/*     */ 
/* 442 */     str = StringUtils.createString(mergedInput, '\n', '^');
/* 443 */     this.m_helper.m_props.put("ScriptInput", str);
/*     */   }
/*     */ 
/*     */   protected void clearInput(String[] exceptKeys)
/*     */   {
/* 448 */     String str = this.m_helper.m_props.getProperty("ScriptInput");
/*     */ 
/* 450 */     Properties inputProps = new Properties();
/* 451 */     Vector orderedInput = WorkflowScriptUtils.parseScriptInput(str, inputProps);
/*     */ 
/* 453 */     Vector clearedInput = new IdcVector();
/*     */ 
/* 455 */     int len = exceptKeys.length;
/* 456 */     int size = orderedInput.size();
/* 457 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 459 */       String name = (String)orderedInput.elementAt(i);
/* 460 */       for (int j = 0; j < len; ++j)
/*     */       {
/* 462 */         String key = exceptKeys[j];
/* 463 */         if (!key.equals(name))
/*     */           continue;
/* 465 */         clearedInput.addElement(key + "=" + inputProps.getProperty(key));
/* 466 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 472 */     str = StringUtils.createString(clearedInput, '\n', '^');
/* 473 */     this.m_helper.m_props.put("ScriptInput", str);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 483 */     ContainerHelper helper = (ContainerHelper)exchange.m_currentObject;
/* 484 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 489 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.ScriptTestPanel
 * JD-Core Version:    0.5.4
 */