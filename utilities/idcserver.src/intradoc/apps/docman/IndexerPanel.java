/*     */ package intradoc.apps.docman;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.BasePanel;
/*     */ import intradoc.apps.shared.PromptDialog;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.FocusListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IndexerPanel extends BasePanel
/*     */   implements ActionListener, Observer, FocusListener
/*     */ {
/*     */   protected JPanel m_indexerPanel;
/*     */   protected GridBagConstraints m_indexerPanelGbc;
/*     */   protected Hashtable m_cycles;
/*     */ 
/*     */   public void init(SystemInterface sys, JMenu fMenu)
/*     */     throws ServiceException
/*     */   {
/*  79 */     super.init(sys, fMenu);
/*     */ 
/*  82 */     AppLauncher.addSubjectObserver("indexerstatus", this);
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  88 */     this.m_indexerPanel = new PanePanel();
/*  89 */     setLayout(new GridLayout(1, 1));
/*  90 */     this.m_indexerPanel.setLayout(new GridBagLayout());
/*  91 */     this.m_indexerPanel.removeAll();
/*     */     DataBinder binder;
/*     */     try
/*     */     {
/*  95 */       binder = getUpdatedData();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  99 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apIndexerStatusLoadError", new Object[0]));
/* 100 */       return;
/*     */     }
/*     */ 
/* 103 */     this.m_cycles = new Hashtable();
/* 104 */     GridBagConstraints gbc = new GridBagConstraints();
/* 105 */     gbc.weightx = 1.0D;
/* 106 */     gbc.fill = 2;
/* 107 */     gbc.gridwidth = 0;
/* 108 */     gbc.anchor = 18;
/* 109 */     gbc.insets = new Insets(1, 1, 1, 1);
/* 110 */     this.m_indexerPanelGbc = ((GridBagConstraints)gbc.clone());
/* 111 */     refreshEx(binder);
/*     */ 
/* 113 */     gbc.gridheight = 0;
/* 114 */     gbc.weighty = 1.0D;
/*     */ 
/* 122 */     add(this.m_indexerPanel);
/*     */   }
/*     */ 
/*     */   protected DataBinder getUpdatedData()
/*     */     throws ServiceException
/*     */   {
/* 128 */     DataBinder binder = new DataBinder();
/* 129 */     binder.putLocal("getStatus", "1");
/* 130 */     AppLauncher.executeService("CONTROL_SEARCH_INDEX", binder, this.m_systemInterface);
/* 131 */     return binder;
/*     */   }
/*     */ 
/*     */   protected void refresh()
/*     */   {
/*     */     try
/*     */     {
/* 138 */       DataBinder binder = getUpdatedData();
/* 139 */       refreshEx(binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 143 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apIndexerStatusLoadError", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void refreshEx(DataBinder binder)
/*     */   {
/* 149 */     ResultSet rset = binder.getResultSet("INDEXER_STATUS");
/* 150 */     boolean mustLayout = false;
/*     */ 
/* 152 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/*     */       String cycle;
/*     */       try
/*     */       {
/* 157 */         cycle = binder.get("sCycleID");
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 161 */         reportError(e);
/* 162 */         break label239:
/*     */       }
/*     */ 
/* 165 */       String autoDisable = binder.getLocal("sDisableAutoUpdate");
/* 166 */       if (cycle.equals("update"))
/*     */       {
/* 168 */         if (StringUtils.convertToBool(autoDisable, false))
/*     */         {
/* 170 */           binder.putLocal("labelCaption", LocaleResources.getString("apIndexerDisabled", this.m_cxt));
/*     */         }
/*     */         else
/*     */         {
/* 174 */           binder.putLocal("sDisableAutoUpdate", "");
/* 175 */           binder.putLocal("labelCaption", LocaleResources.getString("apIndexerEnabled", this.m_cxt));
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 180 */         binder.putLocal("sDisableAutoUpdate", "");
/* 181 */         binder.putLocal("labelCaption", LocaleResources.getString("apIndexerEnabled", this.m_cxt));
/*     */       }
/*     */ 
/* 184 */       Hashtable components = (Hashtable)this.m_cycles.get(cycle);
/* 185 */       if (components == null)
/*     */       {
/* 187 */         components = new Hashtable();
/* 188 */         JPanel panel = createCyclePanel(binder, components, this, this.m_systemInterface.getExecutionContext());
/*     */ 
/* 190 */         this.m_indexerPanel.add(panel, this.m_indexerPanelGbc);
/*     */ 
/* 192 */         mustLayout = true;
/* 193 */         label239: this.m_cycles.put(cycle, components);
/*     */       }
/*     */       else
/*     */       {
/* 197 */         updateCyclePanel(binder, components, this.m_systemInterface.getExecutionContext());
/*     */       }
/*     */     }
/*     */ 
/* 201 */     if (!mustLayout)
/*     */       return;
/* 203 */     validate();
/*     */   }
/*     */ 
/*     */   public PanePanel createCyclePanel(DataBinder binder, Hashtable components, ActionListener listener, ExecutionContext cxt)
/*     */   {
/* 211 */     return updateCyclePanelEx(binder, components, listener, cxt, true);
/*     */   }
/*     */ 
/*     */   public void updateCyclePanel(DataBinder binder, Hashtable components, ExecutionContext cxt)
/*     */   {
/* 217 */     updateCyclePanelEx(binder, components, null, cxt, false);
/* 218 */     AppLauncher.addOrRefreshActiveSubjectEvent("indexstatus");
/*     */   }
/*     */ 
/*     */   public PanePanel updateCyclePanelEx(DataBinder binder, Hashtable components, ActionListener listener, ExecutionContext cxt, boolean isCreate)
/*     */   {
/* 228 */     JPanel panel = null;
/* 229 */     if (isCreate)
/*     */     {
/* 231 */       panel = new PanePanel();
/* 232 */       panel.setLayout(new GridBagLayout());
/*     */     }
/*     */ 
/* 235 */     String[] fields = { "sCycleID", "sCycleLabel", "sDescription", "statusMsg", "status", "stateMsg", "startDate", "finishDate", "activeDate", "progressMessage", "totalFullTextAdd", "totalAddIndex", "totalDeleteIndex", "totalDummyAddIndex", "labelCaption" };
/*     */ 
/* 239 */     String sectionLabel = "";
/*     */ 
/* 241 */     String notAvailable = LocaleResources.getString("apIndexerValueNotAvailable", cxt);
/* 242 */     for (int i = 0; i < fields.length; ++i)
/*     */     {
/* 244 */       String value = notAvailable;
/* 245 */       boolean isAvailable = true;
/*     */       try
/*     */       {
/* 248 */         value = binder.get(fields[i]);
/*     */       }
/*     */       catch (DataException ignore)
/*     */       {
/* 252 */         isAvailable = false;
/*     */       }
/*     */ 
/* 255 */       if ((fields[i].equals("activeDate")) && (((value == null) || (value.length() == 0))))
/*     */       {
/* 257 */         value = LocaleResources.getString("apIndexerNotActive", cxt);
/*     */       }
/*     */ 
/* 260 */       if ((fields[i].equals("progressMessage")) && (isAvailable))
/*     */       {
/* 265 */         Vector v = StringUtils.parseArray(value, ',', '\\');
/* 266 */         if (v.size() == 4)
/*     */         {
/* 268 */           int type = Integer.parseInt((String)v.elementAt(0));
/* 269 */           float amtDone = new Float((String)v.elementAt(1)).floatValue();
/* 270 */           float max = new Float((String)v.elementAt(2)).floatValue();
/* 271 */           String msg = (String)v.elementAt(3);
/*     */ 
/* 273 */           value = msg;
/* 274 */           if (max >= 0.01D)
/*     */           {
/* 276 */             if (type == 0)
/*     */             {
/* 278 */               int m = (int)(max + 0.01D);
/* 279 */               int a = (int)(amtDone + 0.01D);
/* 280 */               String aMsg = LocaleUtils.encodeMessage("apReportProgress1", null, "" + a, "" + m);
/* 281 */               value = value + " " + LocaleResources.localizeMessage(aMsg, cxt);
/*     */             }
/*     */             else
/*     */             {
/* 285 */               float perc = 100.0F * amtDone / max;
/* 286 */               String aMsg = LocaleUtils.encodeMessage("apReportProgress2", null, "" + Math.round(perc));
/*     */ 
/* 288 */               value = value + " " + LocaleResources.localizeMessage(aMsg, cxt);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/* 293 */       else if ((fields[i].equals("labelCaption")) && (!isAvailable))
/*     */       {
/* 295 */         value = "";
/*     */       }
/*     */ 
/* 299 */       if (isCreate)
/*     */       {
/*     */         Component label;
/*     */         Component label;
/* 301 */         if (fields[i].equals("sDescription"))
/*     */         {
/* 303 */           label = new CustomText(value, 75);
/*     */         }
/*     */         else
/*     */         {
/*     */           Component label;
/* 305 */           if (fields[i].equals("sCycleLabel"))
/*     */           {
/* 307 */             label = new CustomLabel(value, 2);
/*     */           }
/*     */           else
/*     */           {
/* 311 */             label = new CustomLabel(value);
/*     */           }
/*     */         }
/* 313 */         components.put(fields[i] + "_component", label);
/*     */       }
/*     */       else
/*     */       {
/* 317 */         Component label = getComponent(components, fields[i]);
/*     */ 
/* 319 */         if (label instanceof CustomLabel)
/*     */         {
/* 321 */           ((CustomLabel)label).setText(value);
/*     */         }
/*     */       }
/*     */ 
/* 325 */       components.put(fields[i] + "_value", value);
/*     */     }
/*     */ 
/* 328 */     String cycle = getValue(components, "sCycleID");
/*     */ 
/* 330 */     if (isCreate)
/*     */     {
/* 332 */       GridBagConstraints label = new GridBagConstraints();
/* 333 */       label.insets.left = 5;
/* 334 */       label.anchor = 12;
/* 335 */       GridBagConstraints value = new GridBagConstraints();
/* 336 */       value.insets.left = 5;
/* 337 */       value.insets.right = 5;
/* 338 */       value.fill = 2;
/* 339 */       value.weightx = 1.0D;
/* 340 */       GridBagConstraints value2 = (GridBagConstraints)value.clone();
/* 341 */       value.gridwidth = 0;
/*     */ 
/* 344 */       Component cycleLabel = getComponent(components, "sCycleLabel");
/* 345 */       panel.add(cycleLabel, value2);
/* 346 */       sectionLabel = cycleLabel.getAccessibleContext().getAccessibleName();
/*     */ 
/* 348 */       Component labelCaption = getComponent(components, "labelCaption");
/* 349 */       panel.add(labelCaption, value);
/*     */ 
/* 352 */       value.insets.left += 5;
/* 353 */       panel.add(getComponent(components, "progressMessage"), value);
/* 354 */       value.insets.left -= 5;
/*     */ 
/* 357 */       JPanel spanel = new PanePanel(8);
/* 358 */       spanel.setLayout(new GridBagLayout());
/*     */ 
/* 360 */       spanel.add(createCaptionLabel("apIndexerState"), label);
/* 361 */       Component comp = getComponent(components, "stateMsg");
/* 362 */       spanel.add(comp, value);
/*     */ 
/* 364 */       spanel.add(createCaptionLabel("apIndexerStatus"), label);
/* 365 */       comp = getComponent(components, "statusMsg");
/* 366 */       spanel.add(comp, value);
/*     */ 
/* 368 */       spanel.add(createCaptionLabel("apIndexerStartDate"), label);
/* 369 */       comp = getComponent(components, "startDate");
/* 370 */       spanel.add(comp, value);
/*     */ 
/* 372 */       spanel.add(createCaptionLabel("apIndexerFinishDate"), label);
/* 373 */       comp = getComponent(components, "finishDate");
/* 374 */       spanel.add(comp, value);
/*     */ 
/* 376 */       spanel.add(createCaptionLabel("apIndexerActiveDate"), label);
/* 377 */       comp = getComponent(components, "activeDate");
/* 378 */       spanel.add(comp, value);
/*     */ 
/* 380 */       value2.insets.right = 0;
/* 381 */       panel.add(spanel, value2);
/* 382 */       value2.insets.right = 5;
/*     */ 
/* 384 */       JPanel counterPanel = new PanePanel(8);
/* 385 */       createCounterSubPanel(counterPanel, components, label, value, cxt);
/* 386 */       value.insets.left = 0;
/* 387 */       panel.add(counterPanel, value);
/* 388 */       value.insets.left = 5;
/*     */ 
/* 391 */       JPanel controlPanel = new PanePanel();
/* 392 */       GridLayout layout = new GridLayout(1, 4);
/* 393 */       layout.setHgap(4);
/* 394 */       controlPanel.setLayout(layout);
/*     */ 
/* 396 */       JButton btn = createIndexerCommandBtn(cycle, "start", sectionLabel, components, listener, cxt);
/* 397 */       controlPanel.add(btn);
/* 398 */       btn = createIndexerCommandBtn(cycle, "suspend", sectionLabel, components, listener, cxt);
/* 399 */       controlPanel.add(btn);
/* 400 */       btn = createIndexerCommandBtn(cycle, "cancel", sectionLabel, components, listener, cxt);
/* 401 */       controlPanel.add(btn);
/* 402 */       btn = createIndexerCommandBtn(cycle, "configure", sectionLabel, components, listener, cxt);
/* 403 */       controlPanel.add(btn);
/*     */ 
/* 409 */       PanePanel outerPanel = new PanePanel(8);
/* 410 */       outerPanel.setLayout(new GridBagLayout());
/* 411 */       outerPanel.add(panel, value);
/* 412 */       value.fill = 0;
/* 413 */       value.anchor = 10;
/* 414 */       value.insets.top = 5;
/* 415 */       value.insets.bottom = 5;
/* 416 */       outerPanel.add(controlPanel, value);
/*     */ 
/* 418 */       panel = outerPanel;
/*     */     }
/*     */ 
/* 427 */     updateIndexerCommandBtns(cycle, components, cxt);
/* 428 */     return (PanePanel)panel;
/*     */   }
/*     */ 
/*     */   public int createCounterSubPanel(JPanel panel, Hashtable components, GridBagConstraints label, GridBagConstraints value, ExecutionContext cxtt)
/*     */   {
/* 434 */     panel.setLayout(new GridBagLayout());
/* 435 */     GridBagConstraints gbc = new GridBagConstraints();
/* 436 */     gbc.gridwidth = 0;
/*     */ 
/* 439 */     panel.add(createLabel("apIndexerCounters"), gbc);
/* 440 */     panel.add(createCaptionLabel("apIndexerTotal"), label);
/* 441 */     panel.add(getComponent(components, "totalAddIndex"), value);
/* 442 */     panel.add(createCaptionLabel("apIndexerFullText"), label);
/* 443 */     panel.add(getComponent(components, "totalFullTextAdd"), value);
/* 444 */     panel.add(createCaptionLabel("apIndexerMetaOnly"), label);
/* 445 */     panel.add(getComponent(components, "totalDummyAddIndex"), value);
/* 446 */     panel.add(createCaptionLabel("apIndexerDelete"), label);
/* 447 */     panel.add(getComponent(components, "totalDeleteIndex"), value);
/* 448 */     return 5;
/*     */   }
/*     */ 
/*     */   protected Component createLabel(String text)
/*     */   {
/* 454 */     text = this.m_systemInterface.getString(text);
/* 455 */     return new CustomLabel(text, 1);
/*     */   }
/*     */ 
/*     */   protected Component createCaptionLabel(String text)
/*     */   {
/* 461 */     text = this.m_systemInterface.localizeCaption(text);
/* 462 */     return new CustomLabel(text, 1);
/*     */   }
/*     */ 
/*     */   protected JButton createIndexerCommandBtn(String cycle, String action, String accessibleName, Hashtable info, ActionListener listener, ExecutionContext cxt)
/*     */   {
/* 468 */     JButton btn = new JButton();
/* 469 */     String btnText = "";
/*     */ 
/* 471 */     if (action.equals("start"))
/*     */     {
/* 473 */       btnText = LocaleResources.getString("apIndexerStart", cxt);
/*     */     }
/* 475 */     else if (action.equals("suspend"))
/*     */     {
/* 477 */       btnText = LocaleResources.getString("apIndexerSuspend", cxt);
/*     */     }
/* 479 */     else if (action.equals("cancel"))
/*     */     {
/* 481 */       btnText = LocaleResources.getString("apIndexerCancel", cxt);
/*     */     }
/* 483 */     else if (action.equals("configure"))
/*     */     {
/* 485 */       btnText = LocaleResources.getString("apIndexerConfigure", cxt);
/*     */     }
/*     */ 
/* 488 */     btn.setText(btnText);
/* 489 */     btn.getAccessibleContext().setAccessibleName(accessibleName + " " + btnText);
/* 490 */     btn.setActionCommand(action + " " + cycle);
/* 491 */     btn.addActionListener(listener);
/* 492 */     info.put(action + "_command", btn);
/* 493 */     return btn;
/*     */   }
/*     */ 
/*     */   protected void updateIndexerCommandBtns(String cycle, Hashtable info, ExecutionContext cxt)
/*     */   {
/* 498 */     String status = getValue(info, "status");
/* 499 */     JButton startBtn = (JButton)info.get("start_command");
/* 500 */     JButton suspendBtn = (JButton)info.get("suspend_command");
/* 501 */     JButton cancelBtn = (JButton)info.get("cancel_command");
/*     */ 
/* 503 */     boolean suspendEnabled = true;
/* 504 */     boolean cancelEnabled = true;
/*     */ 
/* 506 */     if (status.equals("idle"))
/*     */     {
/* 508 */       startBtn.setEnabled(true);
/* 509 */       startBtn.setText(LocaleResources.getString("apIndexerStart", cxt));
/* 510 */       startBtn.setActionCommand("start " + cycle);
/*     */ 
/* 512 */       suspendEnabled = false;
/* 513 */       cancelEnabled = false;
/*     */     }
/* 515 */     else if (status.equals("interrupted"))
/*     */     {
/* 517 */       startBtn.setEnabled(true);
/* 518 */       startBtn.setText(LocaleResources.getString("apIndexerRestart", cxt));
/* 519 */       startBtn.setActionCommand("restart " + cycle);
/*     */ 
/* 521 */       suspendEnabled = false;
/* 522 */       cancelEnabled = true;
/*     */     }
/* 524 */     else if (status.equals("active"))
/*     */     {
/* 526 */       startBtn.setEnabled(false);
/* 527 */       startBtn.setText(LocaleResources.getString("apIndexerStart", cxt));
/* 528 */       startBtn.setActionCommand("start " + cycle);
/*     */ 
/* 530 */       suspendEnabled = true;
/* 531 */       cancelEnabled = true;
/*     */     }
/* 533 */     else if (status.equals("suspending"))
/*     */     {
/* 535 */       startBtn.setEnabled(false);
/* 536 */       suspendEnabled = false;
/* 537 */       cancelEnabled = false;
/*     */     }
/* 539 */     else if (status.equals("cancelling"))
/*     */     {
/* 541 */       startBtn.setEnabled(false);
/* 542 */       suspendEnabled = false;
/* 543 */       cancelEnabled = false;
/*     */     }
/* 545 */     else if (status.equals("indeterminate"))
/*     */     {
/* 547 */       startBtn.setEnabled(false);
/* 548 */       suspendEnabled = false;
/* 549 */       cancelEnabled = false;
/*     */     }
/*     */ 
/* 552 */     suspendBtn.setEnabled(suspendEnabled);
/* 553 */     cancelBtn.setEnabled(cancelEnabled);
/*     */   }
/*     */ 
/*     */   protected Component getComponent(Hashtable table, String key)
/*     */   {
/* 558 */     return (Component)table.get(key + "_component");
/*     */   }
/*     */ 
/*     */   protected String getValue(Hashtable table, String key)
/*     */   {
/* 563 */     return (String)table.get(key + "_value");
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/* 569 */     JButton btn = (JButton)event.getSource();
/* 570 */     DataBinder binder = new DataBinder();
/*     */ 
/* 572 */     String command = btn.getActionCommand();
/* 573 */     int index = command.indexOf(" ");
/* 574 */     String action = command.substring(0, index);
/* 575 */     String cycleId = command.substring(index + 1);
/*     */ 
/* 577 */     if (action.equals("configure"))
/*     */     {
/* 579 */       binder.putLocal("action", "getConfiguration");
/* 580 */       binder.putLocal("cycleID", cycleId);
/*     */       try
/*     */       {
/* 584 */         AppLauncher.executeService("CONTROL_SEARCH_INDEX", binder, this.m_systemInterface);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 588 */         reportError(e);
/*     */       }
/*     */ 
/* 592 */       String helpPage = "ConfigureIndexer";
/* 593 */       if (cycleId.equals("rebuild"))
/*     */       {
/* 595 */         helpPage = "ConfigureIndexerRebuild";
/*     */       }
/*     */ 
/* 598 */       Hashtable components = (Hashtable)this.m_cycles.get(cycleId);
/* 599 */       String title = getValue(components, "sCycleLabel");
/* 600 */       String description = getValue(components, "sDescription");
/* 601 */       Properties props = binder.getLocalData();
/* 602 */       IndexerCycleDlg dlg = new IndexerCycleDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 605 */       props.put("description", description);
/*     */ 
/* 607 */       DialogCallback callback = new DialogCallback(dlg, props)
/*     */       {
/*     */         public boolean handleDialogEvent(ActionEvent ignore)
/*     */         {
/* 612 */           DataBinder data = new DataBinder();
/* 613 */           data.setLocalData(this.val$dlg.m_helper.m_props);
/* 614 */           data.putLocal("action", "setConfiguration");
/* 615 */           data.putLocal("cycleID", (String)this.val$props.get("cycleID"));
/*     */           try
/*     */           {
/* 618 */             AppLauncher.executeService("CONTROL_SEARCH_INDEX", data);
/*     */           }
/*     */           catch (ServiceException e)
/*     */           {
/* 622 */             IndexerPanel.this.reportError(e);
/*     */           }
/*     */ 
/* 625 */           return true;
/*     */         }
/*     */       };
/* 629 */       dlg.init(callback, props);
/* 630 */       dlg.prompt();
/*     */     }
/*     */     else
/*     */     {
/* 634 */       if ((action.toLowerCase().indexOf("start") >= 0) && (cycleId.equals("rebuild")))
/*     */       {
/* 636 */         PromptDialog pd = new PromptDialog(this.m_systemInterface, IdcMessageFactory.lc("apIndexerRebuild", new Object[0]), null);
/*     */ 
/* 638 */         String[] options = new String[0];
/* 639 */         IdcMessage[] captions = new IdcMessage[0];
/* 640 */         boolean[] defaultStates = new boolean[0];
/* 641 */         IdcMessage msg = IdcMessageFactory.lc("apVerifySearchIndexRebuild", new Object[0]);
/* 642 */         binder.putLocal("fastRebuild", "0");
/* 643 */         if (SharedObjects.getEnvValueAsBoolean("SupportFastRebuild", false))
/*     */         {
/* 645 */           options = new String[] { "fastRebuild" };
/* 646 */           captions = new IdcMessage[] { IdcMessageFactory.lc("apIndexerCollectionFastRebuild", new Object[0]) };
/*     */ 
/* 650 */           if (SharedObjects.getEnvValueAsBoolean("IndexCollectionSynced", true))
/*     */           {
/* 652 */             defaultStates = new boolean[] { true };
/*     */           }
/*     */           else
/*     */           {
/* 656 */             defaultStates = new boolean[] { false };
/*     */           }
/*     */         }
/* 659 */         pd.init(options, captions, defaultStates, msg);
/* 660 */         if (!pd.prompt(binder.getLocalData()))
/*     */         {
/* 662 */           return;
/*     */         }
/*     */       }
/*     */       try
/*     */       {
/* 667 */         index = action.indexOf("start");
/* 668 */         if (index >= 0)
/*     */         {
/* 670 */           binder.putLocal("PerformProcessConversion", "1");
/*     */         }
/* 672 */         binder.putLocal("cycleID", cycleId);
/* 673 */         binder.putLocal("action", action);
/* 674 */         binder.putLocal("getStatus", "1");
/*     */ 
/* 676 */         boolean useVdkLegacyRebuild = SharedObjects.getEnvValueAsBoolean("UseVdkLegacyRebuild", false);
/* 677 */         boolean useVdkLegacySearch = SharedObjects.getEnvValueAsBoolean("UseVdkLegacySearch", false);
/*     */ 
/* 679 */         if ((cycleId.equals("rebuild")) && (action.equals("start")) && ((((!useVdkLegacyRebuild) && (useVdkLegacySearch)) || ((useVdkLegacyRebuild) && (!useVdkLegacySearch)))))
/*     */         {
/* 682 */           String key = null;
/*     */ 
/* 684 */           if ((!useVdkLegacyRebuild) && (useVdkLegacySearch))
/*     */           {
/* 686 */             key = "apVdk4RebuildWarning";
/*     */           }
/* 688 */           else if ((useVdkLegacyRebuild) && (!useVdkLegacySearch))
/*     */           {
/* 690 */             key = "apVdkLegacyRebuildWarning";
/*     */           }
/*     */ 
/* 693 */           if ((key != null) && (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc(key, new Object[0]), 4) != 2))
/*     */           {
/* 696 */             return;
/*     */           }
/*     */         }
/*     */ 
/* 700 */         if (action.equals("cancel"))
/*     */         {
/* 702 */           String key = "apVerifyIndexCancel_" + cycleId;
/* 703 */           IdcMessage msg = IdcMessageFactory.lc(key, new Object[0]);
/* 704 */           if (MessageBox.doMessage(this.m_systemInterface, msg, 4) != 2)
/*     */           {
/* 707 */             return;
/*     */           }
/*     */         }
/*     */ 
/* 711 */         AppLauncher.executeService("CONTROL_SEARCH_INDEX", binder, this.m_systemInterface);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 715 */         MessageBox.reportError(this.m_systemInterface, e);
/*     */       }
/*     */ 
/* 718 */       if (binder.getResultSet("INDEXER_STATUS") == null) {
/*     */         return;
/*     */       }
/* 721 */       refreshEx(binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void update(Observable obs, Object obj)
/*     */   {
/* 731 */     DataBinder binder = (DataBinder)obj;
/* 732 */     ResultSet rset = binder.getResultSet("INDEXER_STATUS");
/* 733 */     if (rset == null)
/*     */     {
/* 735 */       refresh();
/*     */     }
/*     */     else
/*     */     {
/* 739 */       refreshEx(binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeNotify()
/*     */   {
/* 746 */     AppLauncher.removeSubjectObserver("indexerstatus", this);
/* 747 */     super.removeNotify();
/*     */   }
/*     */ 
/*     */   public void focusGained(FocusEvent event)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void focusLost(FocusEvent event)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 764 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96112 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.IndexerPanel
 * JD-Core Version:    0.5.4
 */