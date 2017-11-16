/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class JavaCodePanel extends CWizardPanel
/*     */ {
/*  57 */   public static final String[][] FILTERS_COL_MAP = { { "type", "!csCompWizLabelType", "30" }, { "location", "!csCompWizLabelLocation", "30" }, { "parameter", "!csCompWizLabelParam", "30" }, { "loadOrder", "!csCompWizLabelLoadOrder", "3" } };
/*     */ 
/*  62 */   public static final String[][] CLASSS_COL_MAP = { { "classname", "!csCompWizLabelClassName", "30" }, { "location", "!csCompWizLabelLocation", "30" }, { "loadOrder", "!csCompWizLabelLoadOrder", "3" } };
/*     */   protected UdlPanel m_filtersList;
/*     */   protected UdlPanel m_classAliasesList;
/*     */ 
/*     */   public JavaCodePanel()
/*     */   {
/*  66 */     this.m_filtersList = null;
/*  67 */     this.m_classAliasesList = null;
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  72 */     JPanel filterPanel = initFilterPanel();
/*  73 */     JPanel classPanel = initClassAliasesPanel();
/*     */ 
/*  75 */     this.m_helper.makePanelGridBag(this, 1);
/*  76 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  77 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*     */ 
/*  79 */     this.m_helper.addComponent(this, filterPanel);
/*  80 */     this.m_helper.addComponent(this, classPanel);
/*     */   }
/*     */ 
/*     */   public JPanel initFilterPanel()
/*     */   {
/*  85 */     String idColumn = FILTERS_COL_MAP[0][0];
/*     */ 
/*  89 */     this.m_filtersList = createUdlPanel("!csCompWizCustomFiltersTitle", 250, 8, "Fileters", false, FILTERS_COL_MAP, idColumn, false);
/*     */ 
/*  91 */     this.m_filtersList.setVisibleColumns(idColumn);
/*  92 */     this.m_filtersList.setIDColumn(idColumn);
/*  93 */     ItemListener fItemListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/*  97 */         int state = e.getStateChange();
/*  98 */         switch (state)
/*     */         {
/*     */         case 1:
/* 101 */           JavaCodePanel.this.assignListInfo(JavaCodePanel.this.m_filtersList, true);
/*     */         case 2:
/*     */         }
/*     */       }
/*     */     };
/* 108 */     this.m_filtersList.m_list.addItemListener(fItemListener);
/*     */ 
/* 110 */     JPanel infoPanel = new PanePanel();
/* 111 */     this.m_helper.makePanelGridBag(infoPanel, 1);
/*     */ 
/* 113 */     addFieldInfoComponent(infoPanel, "!csCompWizLabelSpecialization", "flocation");
/* 114 */     this.m_helper.addLabelDisplayPair(infoPanel, LocaleResources.getString("csCompWizLabelLoadOrder2", null), 40, "floadOrder");
/*     */ 
/* 116 */     addFieldInfoComponent(infoPanel, "!csCompWizLabelParam", "parameter");
/* 117 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 118 */     this.m_helper.addComponent(infoPanel, addToolBarPanel(this.m_filtersList, true));
/*     */ 
/* 121 */     JPanel wrapper = new CustomPanel();
/* 122 */     this.m_helper.makePanelGridBag(wrapper, 1);
/* 123 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 124 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 125 */     this.m_helper.addComponent(wrapper, this.m_filtersList);
/* 126 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 127 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 128 */     this.m_helper.addComponent(wrapper, infoPanel);
/* 129 */     return wrapper;
/*     */   }
/*     */ 
/*     */   public JPanel initClassAliasesPanel()
/*     */   {
/* 134 */     this.m_classAliasesList = createUdlPanel("!csCompWizLabelCustClassAlias", 250, 13, "ClassAliases", false, CLASSS_COL_MAP, CLASSS_COL_MAP[0][0], false);
/*     */ 
/* 136 */     this.m_classAliasesList.setVisibleColumns("classname");
/* 137 */     this.m_classAliasesList.setIDColumn("classname");
/*     */ 
/* 139 */     ItemListener cItemListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 143 */         int state = e.getStateChange();
/* 144 */         switch (state)
/*     */         {
/*     */         case 1:
/* 147 */           JavaCodePanel.this.assignListInfo(JavaCodePanel.this.m_classAliasesList, false);
/*     */         case 2:
/*     */         }
/*     */       }
/*     */     };
/* 154 */     this.m_classAliasesList.m_list.addItemListener(cItemListener);
/*     */ 
/* 156 */     JPanel infoPanel = new PanePanel();
/* 157 */     this.m_helper.makePanelGridBag(infoPanel, 1);
/* 158 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 159 */     addFieldInfoComponent(infoPanel, "!csCompWizLabelSpecialization", "clocation");
/* 160 */     this.m_helper.addLabelDisplayPair(infoPanel, LocaleResources.getString("csCompWizLabelLoadOrder2", null), 40, "cloadOrder");
/*     */ 
/* 162 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 163 */     this.m_helper.addComponent(infoPanel, addToolBarPanel(this.m_classAliasesList, false));
/*     */ 
/* 166 */     JPanel wrapper = new CustomPanel();
/* 167 */     this.m_helper.makePanelGridBag(wrapper, 1);
/* 168 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 169 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 170 */     this.m_helper.addComponent(wrapper, this.m_classAliasesList);
/* 171 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 172 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 173 */     this.m_helper.addComponent(wrapper, infoPanel);
/*     */ 
/* 175 */     return wrapper;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String assignComponentInfo(IntradocComponent comp, boolean reloadAll)
/*     */   {
/* 182 */     IdcMessage msg = assignComponentInfo(comp, reloadAll, null);
/* 183 */     if (msg != null)
/*     */     {
/* 185 */       return LocaleUtils.encodeMessage(msg);
/*     */     }
/* 187 */     return null;
/*     */   }
/*     */ 
/*     */   public IdcMessage assignComponentInfo(IntradocComponent comp, boolean reloadAll, Map options)
/*     */   {
/* 193 */     IdcMessage errMsg = null;
/*     */     try
/*     */     {
/* 197 */       if (reloadAll)
/*     */       {
/* 200 */         super.assignComponentInfo(comp, reloadAll, options);
/*     */ 
/* 202 */         refreshList(this.m_filtersList, null, true);
/* 203 */         assignListInfo(this.m_filtersList, true);
/* 204 */         refreshList(this.m_classAliasesList, null, false);
/* 205 */         assignListInfo(this.m_classAliasesList, true);
/*     */       }
/*     */       else
/*     */       {
/* 209 */         reloadComponentInfo();
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 214 */       errMsg = IdcMessageFactory.lc(e, "csCorrectErrors", new Object[0]);
/* 215 */       errMsg = IdcMessageFactory.lc(errMsg, "csCompWizCompLoadError", new Object[] { comp.m_name });
/*     */     }
/*     */ 
/* 218 */     return errMsg;
/*     */   }
/*     */ 
/*     */   protected void addFieldInfoComponent(JPanel infoPanel, String label, String compName)
/*     */   {
/* 223 */     JTextField tf = new CustomTextField(40);
/* 224 */     tf.setEditable(false);
/* 225 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 226 */     this.m_helper.addComponent(infoPanel, new CustomLabel(LocaleResources.localizeMessage(label, null), 1));
/*     */ 
/* 228 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 229 */     this.m_helper.addExchangeComponent(infoPanel, tf, compName);
/*     */   }
/*     */ 
/*     */   protected JPanel addToolBarPanel(UdlPanel list, boolean isFilter)
/*     */   {
/* 234 */     JPanel panel = new PanePanel();
/* 235 */     panel.setLayout(new FlowLayout());
/*     */ 
/* 263 */     JButton delBtn = list.addButton(LocaleResources.getString("csCompWizCommandRemove", null), true);
/* 264 */     panel.add(delBtn);
/*     */ 
/* 266 */     ActionListener delListener = null;
/* 267 */     if (isFilter)
/*     */     {
/* 269 */       delListener = new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 273 */           JavaCodePanel.this.delete(JavaCodePanel.this.m_filtersList, true);
/*     */         }
/*     */ 
/*     */       };
/*     */     }
/*     */     else {
/* 279 */       delListener = new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 283 */           JavaCodePanel.this.delete(JavaCodePanel.this.m_classAliasesList, false);
/*     */         }
/*     */       };
/*     */     }
/* 287 */     delBtn.addActionListener(delListener);
/*     */ 
/* 289 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void assignListInfo(UdlPanel list, boolean isFilter)
/*     */   {
/* 294 */     int index = list.getSelectedIndex();
/* 295 */     if (index < 0)
/*     */     {
/* 297 */       return;
/*     */     }
/*     */ 
/* 301 */     Properties props = new Properties(this.m_helper.m_props);
/* 302 */     Properties newProps = list.getDataAt(index);
/* 303 */     for (Enumeration e = newProps.propertyNames(); e.hasMoreElements(); )
/*     */     {
/* 305 */       String key = (String)e.nextElement();
/* 306 */       String val = newProps.getProperty(key);
/* 307 */       props.put(key, val);
/*     */     }
/*     */ 
/* 310 */     String location = props.getProperty("location");
/* 311 */     if ((location != null) && (location.length() > 0))
/*     */     {
/* 313 */       String param = "clocation";
/*     */ 
/* 315 */       if (isFilter)
/*     */       {
/* 317 */         param = "flocation";
/*     */       }
/* 319 */       props.put(param, location);
/*     */     }
/* 321 */     String order = props.getProperty("loadOrder");
/* 322 */     if (order == null)
/*     */     {
/* 324 */       order = "";
/*     */     }
/* 326 */     String param = "floadOrder";
/* 327 */     if (!isFilter)
/*     */     {
/* 329 */       param = "cloadOrder";
/*     */     }
/* 331 */     props.put(param, order);
/*     */ 
/* 333 */     this.m_helper.m_props = props;
/* 334 */     this.m_helper.loadComponentValues();
/* 335 */     list.enableDisable(true);
/*     */   }
/*     */ 
/*     */   protected void refreshList(UdlPanel list, String selObj, boolean isFilter)
/*     */   {
/* 340 */     DataResultSet drset = null;
/* 341 */     if (isFilter)
/*     */     {
/* 343 */       if (this.m_component == null)
/*     */       {
/* 345 */         drset = new DataResultSet(IntradocComponent.FILTERS_FIELD_INFO);
/*     */       }
/*     */       else
/*     */       {
/* 349 */         drset = this.m_component.getFiltersTable();
/*     */       }
/*     */ 
/*     */     }
/* 354 */     else if (this.m_component == null)
/*     */     {
/* 356 */       drset = new DataResultSet(IntradocComponent.CLASS_FIELD_INFO);
/*     */     }
/*     */     else
/*     */     {
/* 360 */       drset = this.m_component.getClassAliasesTable();
/*     */     }
/*     */ 
/* 364 */     if (drset == null)
/*     */       return;
/* 366 */     list.refreshList(drset, selObj);
/*     */ 
/* 368 */     if (selObj == null)
/*     */     {
/* 370 */       list.enableDisable(false);
/*     */     }
/*     */     else
/*     */     {
/* 374 */       list.enableDisable(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void add(UdlPanel list, boolean isFilter)
/*     */   {
/* 381 */     String title = "!csCompWizLabelAddClassAlias";
/* 382 */     String compName = "!csCompWizLabelClassAlias";
/*     */ 
/* 384 */     if (isFilter)
/*     */     {
/* 386 */       title = "!csCompWizLabelAddFilter";
/* 387 */       compName = "!csCompWizLabelFilter";
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 392 */       AddJavaCodeDlg dlg = new AddJavaCodeDlg(this.m_systemInterface, LocaleResources.localizeMessage(title, null), null, this.m_component);
/*     */ 
/* 395 */       dlg.init(isFilter);
/* 396 */       compName = dlg.getComponentName();
/*     */ 
/* 398 */       if (dlg.prompt() == 1)
/*     */       {
/* 400 */         this.m_helper.m_props = dlg.getProperties();
/*     */ 
/* 402 */         if (isFilter)
/*     */         {
/* 404 */           refreshList(this.m_filtersList, compName, isFilter);
/* 405 */           assignListInfo(this.m_filtersList, true);
/*     */         }
/*     */         else
/*     */         {
/* 409 */           refreshList(this.m_classAliasesList, compName, isFilter);
/* 410 */           assignListInfo(this.m_classAliasesList, false);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 416 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("csCompWizComponentAddError", new Object[] { compName }));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void delete(UdlPanel list, boolean isFilter)
/*     */   {
/* 423 */     int index = list.getSelectedIndex();
/* 424 */     if (index < 0)
/*     */     {
/* 426 */       IdcMessage errMsg = IdcMessageFactory.lc("csCompWizRemoveClassWarning", new Object[0]);
/* 427 */       if (isFilter)
/*     */       {
/* 429 */         errMsg = IdcMessageFactory.lc("csCompWizRemoveFilterWarning", new Object[0]);
/*     */       }
/* 431 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, errMsg);
/* 432 */       return;
/*     */     }
/*     */ 
/* 435 */     Properties props = list.getDataAt(index);
/* 436 */     String name = null;
/* 437 */     if (isFilter)
/*     */     {
/* 439 */       name = props.getProperty("type");
/*     */     }
/*     */     else
/*     */     {
/* 443 */       name = props.getProperty("classname");
/*     */     }
/*     */ 
/* 446 */     if (CWizardGuiUtils.doMessage(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizPromptRemove", new Object[] { name }), 4) != 2) {
/*     */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 452 */       this.m_component.deleteJavaCode(props, isFilter);
/* 453 */       if (isFilter)
/*     */       {
/* 455 */         refreshList(this.m_filtersList, null, true);
/* 456 */         this.m_helper.m_props.remove("flocation");
/* 457 */         this.m_helper.m_props.remove("floadOrder");
/* 458 */         this.m_helper.m_props.remove("parameter");
/*     */       }
/*     */       else
/*     */       {
/* 462 */         refreshList(this.m_classAliasesList, null, false);
/* 463 */         this.m_helper.m_props.remove("clocation");
/* 464 */         this.m_helper.m_props.remove("cloadOrder");
/*     */       }
/*     */ 
/* 467 */       this.m_helper.loadComponentValues();
/* 468 */       list.enableDisable(false);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 472 */       CWizardGuiUtils.reportError(this.m_systemInterface, exp, IdcMessageFactory.lc("csCompWizRemovedFailed", new Object[] { name }));
/*     */ 
/* 474 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void launchEditor(UdlPanel list, boolean isFilter)
/*     */   {
/*     */     try
/*     */     {
/* 483 */       int index = list.getSelectedIndex();
/* 484 */       if (index < 0)
/*     */       {
/* 487 */         CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizMustSelectResource", new Object[0]));
/*     */ 
/* 489 */         return;
/*     */       }
/*     */ 
/* 492 */       Properties props = list.getDataAt(index);
/* 493 */       String bindir = SharedObjects.getEnvironmentValue("BinDir");
/* 494 */       if ((bindir == null) || (bindir.length() == 0))
/*     */       {
/* 496 */         throw new ServiceException("!csCompWizBinDirNotFound");
/*     */       }
/*     */ 
/* 499 */       String location = props.getProperty("location");
/* 500 */       String editorPath = SharedObjects.getEnvironmentValue("JavaEditorPath");
/*     */ 
/* 502 */       CWizardUtils.launchExe(editorPath, CWizardUtils.covertPackageToFilePath(bindir, location));
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 506 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void reloadComponentInfo() {
/* 511 */     String filterSelObj = null;
/* 512 */     int index = this.m_filtersList.getSelectedIndex();
/* 513 */     if (index >= 0)
/*     */     {
/* 515 */       Properties props = this.m_filtersList.getDataAt(index);
/* 516 */       filterSelObj = props.getProperty("type");
/*     */     }
/*     */ 
/* 519 */     String classSelObj = null;
/* 520 */     index = this.m_classAliasesList.getSelectedIndex();
/* 521 */     if (index >= 0)
/*     */     {
/* 523 */       Properties props = this.m_classAliasesList.getDataAt(index);
/* 524 */       classSelObj = props.getProperty("classname");
/*     */     }
/*     */ 
/* 527 */     refreshList(this.m_filtersList, filterSelObj, true);
/* 528 */     assignListInfo(this.m_filtersList, true);
/* 529 */     refreshList(this.m_classAliasesList, classSelObj, false);
/* 530 */     assignListInfo(this.m_classAliasesList, true);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 535 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.JavaCodePanel
 * JD-Core Version:    0.5.4
 */