/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.gui.iwt.IdcFileChooser;
/*     */ import java.awt.Component;
/*     */ import java.awt.Container;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.LayoutManager;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.io.File;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JComponent;
/*     */ import javax.swing.JFileChooser;
/*     */ import javax.swing.JLabel;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.JTextField;
/*     */ import javax.swing.JViewport;
/*     */ 
/*     */ public class ContainerHelper
/*     */   implements ComponentBinder
/*     */ {
/*     */   public Container m_container;
/*     */   public JPanel m_mainPanel;
/*     */   public DynamicComponentExchange m_exchange;
/*     */   public Properties m_props;
/*     */   public GridBagHelper m_gridHelper;
/*     */   public ComponentBinder m_componentBinder;
/*     */   public boolean m_keepSpaces;
/*     */   public Map<String, String> m_valueMappingRules;
/*     */   public Map<Component, Component> m_componentLabels;
/*     */ 
/*     */   public ContainerHelper()
/*     */   {
/*  91 */     this.m_exchange = new DynamicComponentExchange();
/*  92 */     this.m_gridHelper = new GridBagHelper();
/*  93 */     this.m_componentBinder = this;
/*  94 */     this.m_keepSpaces = false;
/*  95 */     this.m_valueMappingRules = null;
/*  96 */     this.m_componentLabels = new HashMap();
/*     */   }
/*     */ 
/*     */   public void attachToContainer(Container container, SystemInterface sys, Properties props)
/*     */   {
/* 101 */     this.m_container = container;
/* 102 */     this.m_exchange.m_sysInterface = sys;
/* 103 */     this.m_props = props;
/*     */   }
/*     */ 
/*     */   public void addExchangeComponent(JPanel panel, Component comp, String name)
/*     */   {
/* 109 */     addComponent(panel, comp);
/* 110 */     if ((name == null) || (name.length() <= 0))
/*     */       return;
/* 112 */     if (comp instanceof JScrollPane)
/*     */     {
/* 114 */       JScrollPane scrollpane = (JScrollPane)comp;
/* 115 */       comp = scrollpane.getViewport().getView();
/*     */     }
/* 117 */     this.m_exchange.addComponent(name, comp, null);
/*     */   }
/*     */ 
/*     */   public void addComponent(JPanel panel, Component comp)
/*     */   {
/* 124 */     LayoutManager man = panel.getLayout();
/* 125 */     if (comp instanceof JTextArea)
/*     */     {
/* 127 */       JScrollPane scrollPane = new JScrollPane(20, 30);
/*     */ 
/* 129 */       scrollPane.setViewportView(comp);
/*     */ 
/* 131 */       if (man instanceof GridBagLayout)
/*     */       {
/* 133 */         GridBagLayout gridBag = (GridBagLayout)man;
/* 134 */         gridBag.setConstraints(scrollPane, this.m_gridHelper.m_gc);
/*     */       }
/*     */ 
/* 137 */       panel.add(scrollPane);
/*     */     }
/*     */     else
/*     */     {
/* 141 */       if (man instanceof GridBagLayout)
/*     */       {
/* 143 */         GridBagLayout gridBag = (GridBagLayout)man;
/* 144 */         gridBag.setConstraints(comp, this.m_gridHelper.m_gc);
/*     */       }
/*     */ 
/* 147 */       panel.add(comp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addLastComponentInRow(JPanel panel, Component comp)
/*     */   {
/* 156 */     this.m_gridHelper.m_gc.gridwidth = 0;
/* 157 */     addComponent(panel, comp);
/* 158 */     this.m_gridHelper.m_gc.gridwidth = 1;
/*     */   }
/*     */ 
/*     */   public CustomLabel addLabelFieldPair(JPanel panel, String label, Component comp, String name)
/*     */   {
/* 165 */     return addLabelFieldPairEx(panel, label, comp, name, true);
/*     */   }
/*     */ 
/*     */   public CustomLabel addLabelFieldPairEx(JPanel panel, String label, Component comp, String name, boolean isRowEnd)
/*     */   {
/* 174 */     boolean useShortComponent = false;
/* 175 */     boolean allowVerticalResizing = false;
/* 176 */     if (comp instanceof JCheckBox)
/*     */     {
/* 178 */       useShortComponent = true;
/*     */     }
/* 180 */     else if (comp instanceof JTextArea)
/*     */     {
/* 182 */       allowVerticalResizing = true;
/*     */     }
/* 184 */     int labelStyle = 1;
/* 185 */     GridBagConstraints oldgc = (GridBagConstraints)this.m_gridHelper.m_gc.clone();
/* 186 */     GridBagConstraints gc = this.m_gridHelper.m_gc;
/* 187 */     this.m_gridHelper.prepareAddRowElement(13);
/* 188 */     gc.weightx = 0.0D;
/* 189 */     gc.fill = 0;
/*     */ 
/* 191 */     CustomLabel labelComponent = new CustomLabel(label, labelStyle);
/*     */ 
/* 194 */     if (comp.isFocusable())
/*     */     {
/* 196 */       if (comp instanceof JLabel)
/*     */       {
/* 201 */         String keyPart = comp.getAccessibleContext().getAccessibleName();
/* 202 */         String valuePart = labelComponent.getAccessibleContext().getAccessibleName();
/* 203 */         comp.getAccessibleContext().setAccessibleName(keyPart + " " + valuePart);
/*     */       }
/* 205 */       labelComponent.setFocusable(false);
/*     */     }
/*     */ 
/* 209 */     labelComponent.setLabelFor(comp);
/* 210 */     if (comp instanceof JComponent)
/*     */     {
/* 212 */       JComponent c = (JComponent)comp;
/* 213 */       c.setToolTipText(label);
/*     */     }
/*     */ 
/* 216 */     addComponent(panel, labelComponent);
/*     */ 
/* 218 */     gc.weightx = 1.0D;
/* 219 */     if (useShortComponent)
/*     */     {
/* 221 */       gc.fill = 0;
/*     */     }
/* 223 */     else if (allowVerticalResizing)
/*     */     {
/* 225 */       gc.fill = 1;
/*     */     }
/*     */     else
/*     */     {
/* 229 */       gc.fill = 2;
/*     */     }
/* 231 */     int gridWidth = (isRowEnd) ? 0 : 1;
/* 232 */     this.m_gridHelper.prepareAddRowElement(17, gridWidth);
/* 233 */     addExchangeComponent(panel, comp, name);
/*     */ 
/* 235 */     this.m_gridHelper.m_gc = oldgc;
/* 236 */     this.m_componentLabels.put(comp, labelComponent);
/* 237 */     return labelComponent;
/*     */   }
/*     */ 
/*     */   public void setEnabled(Component comp, boolean newState)
/*     */   {
/* 242 */     comp.setEnabled(newState);
/* 243 */     Component label = (Component)this.m_componentLabels.get(comp);
/* 244 */     if (label == null)
/*     */       return;
/* 246 */     label.setEnabled(newState);
/*     */   }
/*     */ 
/*     */   public void addComboComponent(JPanel panel, AggregateImplementor comp, String checkName, String fieldName)
/*     */   {
/* 253 */     int oldFill = this.m_gridHelper.m_gc.fill;
/* 254 */     this.m_gridHelper.m_gc.fill = 2;
/*     */ 
/* 256 */     this.m_gridHelper.prepareAddRowElement(13);
/*     */ 
/* 258 */     Insets oldInsets = this.m_gridHelper.m_gc.insets;
/* 259 */     if (oldInsets != null)
/*     */     {
/* 261 */       this.m_gridHelper.m_gc.insets = ((Insets)oldInsets.clone());
/* 262 */       this.m_gridHelper.m_gc.insets.right = 0;
/*     */     }
/*     */ 
/* 265 */     addExchangeComponent(panel, comp.getComponent(), checkName);
/*     */ 
/* 267 */     if (oldInsets != null)
/*     */     {
/* 269 */       this.m_gridHelper.m_gc.insets = ((Insets)oldInsets.clone());
/* 270 */       this.m_gridHelper.m_gc.insets.left = 0;
/*     */     }
/*     */ 
/* 273 */     Vector buddies = comp.getBuddies();
/* 274 */     JButton browseButton = comp.getBrowseButton();
/* 275 */     boolean hasBuddies = (buddies != null) && (buddies.size() > 0);
/* 276 */     if (hasBuddies)
/*     */     {
/* 278 */       this.m_gridHelper.prepareAddRowElement(17);
/* 279 */       this.m_gridHelper.m_gc.weightx = 1.0D;
/* 280 */       int size = buddies.size();
/* 281 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 283 */         Component buddy = (Component)buddies.elementAt(i);
/* 284 */         String name = fieldName;
/* 285 */         if (size > 1)
/*     */         {
/* 287 */           name = name + ":" + i;
/*     */         }
/*     */ 
/* 290 */         if ((browseButton == null) && (i == size - 1))
/*     */         {
/* 292 */           this.m_gridHelper.prepareAddRowElement(17, 0);
/*     */         }
/* 294 */         if (buddy instanceof JButton)
/*     */         {
/* 296 */           addComponent(panel, buddy);
/*     */         }
/*     */         else
/*     */         {
/* 300 */           addExchangeComponent(panel, buddy, name);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 305 */     if (browseButton != null)
/*     */     {
/* 308 */       this.m_gridHelper.prepareAddLastRowElement();
/* 309 */       this.m_gridHelper.m_gc.weightx = 0.0D;
/* 310 */       this.m_gridHelper.m_gc.fill = 0;
/* 311 */       addComponent(panel, browseButton);
/*     */     }
/* 313 */     else if (!hasBuddies)
/*     */     {
/* 315 */       this.m_gridHelper.m_gc.weightx = 0.0D;
/* 316 */       this.m_gridHelper.prepareAddRowElement(17, 0);
/* 317 */       addComponent(panel, new PanePanel());
/*     */     }
/* 319 */     this.m_gridHelper.m_gc.insets = oldInsets;
/* 320 */     this.m_gridHelper.m_gc.fill = oldFill;
/*     */   }
/*     */ 
/*     */   public void addPanelTitle(JPanel panel, String title)
/*     */   {
/* 328 */     this.m_gridHelper.prepareAddLastRowElement(18);
/* 329 */     addComponent(panel, new CustomLabel(title, 2));
/*     */   }
/*     */ 
/*     */   public static String gatherAccessibleText(Container container, ExecutionContext cxt)
/*     */   {
/* 344 */     IdcStringBuilder output = new IdcStringBuilder();
/*     */ 
/* 346 */     Component[] allComps = container.getComponents();
/* 347 */     for (int i = 0; i < allComps.length; ++i)
/*     */     {
/* 349 */       Component comp = container.getComponent(i);
/* 350 */       boolean isVisible = comp.isVisible();
/* 351 */       String accText = comp.getAccessibleContext().getAccessibleName();
/* 352 */       if ((isVisible) && (accText != null) && (accText.length() > 0))
/*     */       {
/* 354 */         IdcStringBuilder compText = new IdcStringBuilder(accText);
/* 355 */         compText.append(gatherAccessibleTypeInfo(comp, cxt));
/* 356 */         output.append(compText);
/*     */       }
/* 358 */       if ((!isVisible) || (!comp instanceof Container))
/*     */         continue;
/* 360 */       output.append(gatherAccessibleText((Container)comp, cxt));
/*     */     }
/*     */ 
/* 364 */     return output.toString();
/*     */   }
/*     */ 
/*     */   protected static String gatherAccessibleTypeInfo(Component comp, ExecutionContext cxt)
/*     */   {
/* 378 */     IdcStringBuilder typeInfo = new IdcStringBuilder(" ");
/*     */ 
/* 381 */     if (comp instanceof JButton)
/*     */     {
/* 383 */       if (comp.isEnabled())
/*     */       {
/* 385 */         typeInfo.append(LocaleResources.getString("apButton", cxt));
/*     */       }
/*     */       else
/*     */       {
/* 389 */         typeInfo.append(LocaleResources.getString("apDisabledButton", cxt));
/*     */       }
/*     */     }
/* 392 */     else if (comp instanceof JCheckBox)
/*     */     {
/* 394 */       if (comp.isEnabled())
/*     */       {
/* 396 */         typeInfo.append(LocaleResources.getString("apCheckbox", cxt));
/*     */       }
/*     */       else
/*     */       {
/* 400 */         typeInfo.append(LocaleResources.getString("apDisabledCheckbox", cxt));
/*     */       }
/*     */     }
/* 403 */     else if (comp instanceof JComboBox)
/*     */     {
/* 405 */       if (comp.isEnabled())
/*     */       {
/* 407 */         typeInfo.append(LocaleResources.getString("apComboBox", cxt));
/*     */       }
/*     */       else
/*     */       {
/* 411 */         typeInfo.append(LocaleResources.getString("apDisabledComboBox", cxt));
/*     */       }
/*     */     }
/*     */ 
/* 415 */     typeInfo.append(" ");
/*     */ 
/* 417 */     return typeInfo.toString();
/*     */   }
/*     */ 
/*     */   public void addLabelDisplayPair(JPanel panel, String label, int displen, String name)
/*     */   {
/* 422 */     addLabelDisplayPairEx(panel, label, displen, name, true);
/*     */   }
/*     */ 
/*     */   public void addLabelDisplayPairEx(JPanel panel, String label, int displen, String name, boolean isRowEnd)
/*     */   {
/* 428 */     CustomLabel cl = new CustomLabel();
/* 429 */     cl.setMinWidth(displen);
/* 430 */     addLabelFieldPairEx(panel, label, cl, name, isRowEnd);
/*     */   }
/*     */ 
/*     */   public Component[] addLabelEditPair(JPanel panel, String label, int neditcols, String name)
/*     */   {
/* 435 */     return addLabelEditPairEx(panel, label, neditcols, name, true);
/*     */   }
/*     */ 
/*     */   public Component[] addLabelEditPairEx(JPanel panel, String label, int neditcols, String name, boolean isRowEnd)
/*     */   {
/* 441 */     Component[] rc = new Component[2];
/* 442 */     JTextField textField = new CustomTextField(neditcols);
/* 443 */     rc[0] = addLabelFieldPairEx(panel, label, textField, name, isRowEnd);
/* 444 */     rc[1] = textField;
/* 445 */     return rc;
/*     */   }
/*     */ 
/*     */   public void makePanelGridBag(JPanel pnl, int fill)
/*     */   {
/* 450 */     this.m_gridHelper.useGridBag(pnl);
/* 451 */     this.m_gridHelper.m_gc.fill = fill;
/*     */   }
/*     */ 
/*     */   public JButton addCommandButton(JPanel panel, String label, String command, ActionListener callback)
/*     */   {
/* 460 */     JButton btn = new JButton(label);
/* 461 */     btn.setActionCommand(command);
/* 462 */     btn.addActionListener(callback);
/* 463 */     addComponent(panel, btn);
/* 464 */     return btn;
/*     */   }
/*     */ 
/*     */   public JButton addFilePathComponent(JPanel mainPanel, int textFieldSize, String title, String name)
/*     */   {
/* 470 */     return addFilePathComponentEx(mainPanel, textFieldSize, title, name, null, true);
/*     */   }
/*     */ 
/*     */   public JButton addFilePathComponentEx(JPanel mainPanel, int textFieldSize, String title, String name, String defaultFile, boolean isRowEnd)
/*     */   {
/* 476 */     JTextField bField = new CustomTextField(textFieldSize);
/* 477 */     JButton bFileBtn = new JButton(GuiText.m_browseLabel);
/*     */ 
/* 479 */     String fileDlgTitle = title;
/* 480 */     String defaultFilename = defaultFile;
/*     */ 
/* 482 */     ActionListener bListener = new ActionListener(fileDlgTitle, defaultFilename, bField)
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 486 */         JFileChooser fileDlg = new IdcFileChooser();
/* 487 */         fileDlg.setDialogTitle(this.val$fileDlgTitle);
/* 488 */         if (this.val$defaultFilename != null)
/*     */         {
/* 490 */           IdcFileFilter filter = new IdcFileFilter();
/* 491 */           filter.m_pattern = this.val$defaultFilename;
/* 492 */           fileDlg.setFileFilter(filter);
/*     */         }
/*     */ 
/* 495 */         fileDlg.showOpenDialog(null);
/*     */ 
/* 498 */         File file = fileDlg.getSelectedFile();
/* 499 */         if (file == null)
/*     */           return;
/* 501 */         this.val$bField.setText(file.getAbsolutePath());
/*     */       }
/*     */     };
/* 506 */     bFileBtn.addActionListener(bListener);
/*     */ 
/* 508 */     GridBagHelper gridBag = this.m_gridHelper;
/* 509 */     GridBagConstraints gc = gridBag.m_gc;
/* 510 */     int oldfill = gc.fill;
/* 511 */     double oldweightx = gc.weightx;
/*     */ 
/* 513 */     gridBag.prepareAddRowElement(17);
/* 514 */     gc.gridwidth = 2;
/* 515 */     gc.fill = 2;
/* 516 */     gc.weightx = 5.0D;
/* 517 */     addExchangeComponent(mainPanel, bField, name);
/*     */ 
/* 519 */     if (isRowEnd)
/*     */     {
/* 521 */       gridBag.prepareAddLastRowElement();
/*     */     }
/*     */     else
/*     */     {
/* 525 */       gridBag.prepareAddRowElement();
/*     */     }
/* 527 */     gc.fill = 0;
/* 528 */     gc.weightx = 0.5D;
/* 529 */     addComponent(mainPanel, bFileBtn);
/* 530 */     gc.weightx = oldweightx;
/* 531 */     gc.fill = oldfill;
/*     */ 
/* 533 */     return bFileBtn;
/*     */   }
/*     */ 
/*     */   public void loadComponentValues()
/*     */   {
/* 541 */     this.m_exchange.m_currentObject = this;
/* 542 */     this.m_exchange.exchange(this.m_componentBinder, true);
/*     */   }
/*     */ 
/*     */   public boolean retrieveComponentValues()
/*     */   {
/* 547 */     this.m_exchange.m_currentObject = this;
/* 548 */     return this.m_exchange.exchange(this.m_componentBinder, false);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 559 */     String name = exchange.m_compName;
/* 560 */     if (updateComponent)
/*     */     {
/* 562 */       String value = this.m_props.getProperty(name);
/* 563 */       if ((value != null) && 
/* 565 */         (isTrimmedWhitespace(exchange)))
/*     */       {
/* 567 */         value = value.trim();
/*     */       }
/*     */ 
/* 570 */       exchange.m_compValue = value;
/*     */     }
/*     */     else
/*     */     {
/* 574 */       this.m_props.put(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 580 */     String val = exchange.m_compValue;
/* 581 */     if (val != null)
/*     */     {
/* 583 */       if (isTrimmedWhitespace(exchange))
/*     */       {
/* 585 */         val = val.trim();
/*     */       }
/* 587 */       exchange.m_compValue = val;
/*     */     }
/*     */ 
/* 590 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean isTrimmedWhitespace(DynamicComponentExchange exchange)
/*     */   {
/* 595 */     boolean keepSpaces = this.m_keepSpaces;
/* 596 */     if ((keepSpaces) && (this.m_valueMappingRules != null) && (exchange.m_compName != null))
/*     */     {
/* 598 */       String rule = (String)this.m_valueMappingRules.get(exchange.m_compName);
/* 599 */       if ((rule != null) && (rule.indexOf("trim") >= 0))
/*     */       {
/* 602 */         keepSpaces = false;
/*     */       }
/*     */     }
/* 605 */     return !keepSpaces;
/*     */   }
/*     */ 
/*     */   public void handleActionPerformed(ActionEvent event, Object obj, SystemInterface si)
/*     */   {
/* 611 */     String cmd = event.getActionCommand();
/* 612 */     if (SystemUtils.m_verbose)
/*     */     {
/* 614 */       Report.debug("applet", "trying action " + cmd + " on object " + obj.getClass().getName(), null);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 620 */       ClassHelperUtils.executeMethod(obj, cmd, null, null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 626 */       MessageBox.reportError(si, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 632 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83557 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.ContainerHelper
 * JD-Core Version:    0.5.4
 */