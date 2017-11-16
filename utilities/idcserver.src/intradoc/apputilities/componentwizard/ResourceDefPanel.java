/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.CardLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ResourceDefPanel extends CWizardPanel
/*     */ {
/*  58 */   public static final String[][] RES_DEF_COL_MAP = { { "displayTypeName", "!csCompWizLabelType", "10" }, { "filename", "!csCompWizLabelFileName", "30" } };
/*     */ 
/*  61 */   public static final String[][] RESOURCE_NAME_MAP = { { "resource", "csCompWizResourceTypeName" }, { "query", "csCompWizQueryTypeName" }, { "service", "csCompWizServiceTypeName" }, { "template", "csCompWizTemplateTypeName" }, { "environment", "csCompWizEnvironmenTypeName" } };
/*     */   protected UdlPanel m_resDefList;
/*     */   protected JPanel m_flipPanel;
/*     */   protected BaseResViewPanel m_curView;
/*     */   protected JButton m_addBtn;
/*     */   protected JButton m_delBtn;
/*     */   protected JButton m_launchBtn;
/*     */   protected JButton m_reloadBtn;
/*     */   protected Hashtable m_flipComponents;
/*     */   protected DataResultSet m_listData;
/*     */   protected int m_idCount;
/*     */ 
/*     */   public ResourceDefPanel()
/*     */   {
/*  72 */     this.m_resDefList = null;
/*     */ 
/*  76 */     this.m_addBtn = null;
/*  77 */     this.m_delBtn = null;
/*  78 */     this.m_launchBtn = null;
/*  79 */     this.m_reloadBtn = null;
/*     */ 
/*  83 */     this.m_listData = null;
/*  84 */     this.m_idCount = 0;
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  89 */     JPanel listPanel = initUdlPanel();
/*  90 */     JPanel infoPanel = initInfoPanel();
/*     */ 
/*  92 */     this.m_helper.makePanelGridBag(this, 1);
/*  93 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  94 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  95 */     this.m_helper.addComponent(this, listPanel);
/*  96 */     this.m_helper.addComponent(this, infoPanel);
/*     */ 
/* 100 */     LocaleResources.localizeStaticDoubleArray(RESOURCE_NAME_MAP, null, 1);
/*     */   }
/*     */ 
/*     */   public JPanel initUdlPanel()
/*     */   {
/* 105 */     this.m_resDefList = createUdlPanel("!csCompWizLabelCustResourceDef", 250, 15, "", true, RES_DEF_COL_MAP, "id", false);
/*     */ 
/* 107 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 111 */         ResourceDefPanel.this.checkSelection();
/*     */       }
/*     */     };
/* 114 */     this.m_resDefList.m_list.addItemListener(iListener);
/* 115 */     this.m_resDefList.add("South", addToolBarPanel());
/*     */ 
/* 117 */     this.m_resDefList.enableDisable(false);
/* 118 */     this.m_addBtn.setEnabled(false);
/*     */ 
/* 121 */     JPanel wrapper = new CustomPanel();
/* 122 */     this.m_helper.makePanelGridBag(wrapper, 1);
/* 123 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 124 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 125 */     this.m_helper.addComponent(wrapper, this.m_resDefList);
/*     */ 
/* 127 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel addToolBarPanel()
/*     */   {
/* 133 */     JPanel panel = new PanePanel();
/*     */ 
/* 135 */     this.m_addBtn = this.m_resDefList.addButton(LocaleResources.getString("csCompWizCommandAdd", null), false);
/* 136 */     panel.add(this.m_addBtn);
/*     */ 
/* 138 */     ActionListener addListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 142 */         ResourceDefPanel.this.addResource();
/*     */       }
/*     */     };
/* 145 */     this.m_addBtn.addActionListener(addListener);
/*     */ 
/* 147 */     this.m_delBtn = this.m_resDefList.addButton(LocaleResources.getString("csCompWizCommandRemove", null), true);
/* 148 */     panel.add(this.m_delBtn);
/*     */ 
/* 150 */     ActionListener deleteListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 154 */         ResourceDefPanel.this.removeResource();
/*     */       }
/*     */     };
/* 157 */     this.m_delBtn.addActionListener(deleteListener);
/*     */ 
/* 159 */     this.m_launchBtn = this.m_resDefList.addButton(LocaleResources.getString("csCompWizLanchEditor", null), true);
/* 160 */     panel.add(this.m_launchBtn);
/*     */ 
/* 162 */     ActionListener launchListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 166 */         ResourceDefPanel.this.launchEditor();
/*     */       }
/*     */     };
/* 169 */     this.m_launchBtn.addActionListener(launchListener);
/* 170 */     this.m_resDefList.m_list.addActionListener(launchListener);
/*     */ 
/* 172 */     this.m_reloadBtn = this.m_resDefList.addButton(LocaleResources.getString("csCompWizCommandReload", null), true);
/* 173 */     panel.add(this.m_reloadBtn);
/*     */ 
/* 175 */     ActionListener reloadListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 179 */         IdcMessage msg = null;
/*     */         try
/*     */         {
/* 182 */           ResourceDefPanel.this.reloadComponentInfo();
/* 183 */           msg = CWizardUtils.formatErrorMessage(ResourceDefPanel.this.m_component, null);
/* 184 */           if (msg != null)
/*     */           {
/* 186 */             throw new ServiceException(null, msg);
/*     */           }
/*     */         }
/*     */         catch (Exception excp)
/*     */         {
/* 191 */           CWizardGuiUtils.reportError(ResourceDefPanel.this.m_systemInterface, excp, (IdcMessage)null);
/*     */         }
/*     */       }
/*     */     };
/* 195 */     this.m_reloadBtn.addActionListener(reloadListener);
/* 196 */     return panel;
/*     */   }
/*     */ 
/*     */   protected JPanel initInfoPanel()
/*     */   {
/* 201 */     this.m_flipPanel = new CustomPanel();
/* 202 */     this.m_flipComponents = new Hashtable();
/* 203 */     CardLayout flipLayout = new CardLayout();
/* 204 */     this.m_flipPanel.setLayout(flipLayout);
/*     */ 
/* 206 */     BaseResViewPanel baseResViewPanel = new BaseResViewPanel();
/* 207 */     this.m_curView = baseResViewPanel;
/* 208 */     addFlipComponent("Empty", baseResViewPanel, -1);
/* 209 */     createFlipComponents();
/* 210 */     flipLayout.show(this.m_flipPanel, "Empty");
/*     */ 
/* 212 */     return this.m_flipPanel;
/*     */   }
/*     */ 
/*     */   protected void createFlipComponents()
/*     */   {
/* 217 */     addFlipComponent("htmlIncludes", new ResourceBasePanel(), 0);
/* 218 */     addFlipComponent("dataIncludes", new ResourceBasePanel(), 1);
/* 219 */     addFlipComponent("resourceTables", new ResourceBasePanel(), 2);
/* 220 */     addFlipComponent("resStrings", new ResourceBasePanel(), 7);
/* 221 */     addFlipComponent("includesStrings", new ResourcePanel(), -1);
/* 222 */     addFlipComponent("tablesStrings", new ResourcePanel(), -1);
/* 223 */     addFlipComponent("tablesIncludes", new ResourcePanel(), -1);
/* 224 */     addFlipComponent("htmlDataIncludes", new ResourcePanel(), -1);
/* 225 */     addFlipComponent("tablesIncludesStrings", new ResourcePanel(), -1);
/* 226 */     addFlipComponent("query", new ResourceBasePanel(), 3);
/* 227 */     addFlipComponent("service", new ResourceBasePanel(), 4);
/* 228 */     addFlipComponent("template", new ResourceBasePanel(), 5);
/* 229 */     addFlipComponent("environment", new ResourceBasePanel(), 6);
/*     */   }
/*     */ 
/*     */   protected void addFlipComponent(String compId, BaseResViewPanel comp, int resourceType)
/*     */   {
/* 234 */     comp.init(this.m_helper);
/* 235 */     if (resourceType < 0)
/*     */     {
/* 237 */       comp.setResourcePanelType(compId);
/*     */     }
/* 239 */     comp.initUI(false, resourceType);
/* 240 */     this.m_flipPanel.add(compId, comp);
/* 241 */     this.m_flipComponents.put(compId, comp);
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 246 */     checkSelectionEx(false);
/*     */   }
/*     */ 
/*     */   protected void checkSelectionEx(boolean refreshAll)
/*     */   {
/* 253 */     String errMsg = null;
/* 254 */     CardLayout panelHandler = (CardLayout)this.m_flipPanel.getLayout();
/* 255 */     int index = this.m_resDefList.getSelectedIndex();
/* 256 */     if (index < 0)
/*     */     {
/* 258 */       this.m_curView = ((BaseResViewPanel)this.m_flipComponents.get("Empty"));
/* 259 */       this.m_curView.setProperties(new Properties());
/* 260 */       panelHandler.show(this.m_flipPanel, "Empty");
/* 261 */       return;
/*     */     }
/*     */ 
/* 264 */     Properties props = this.m_resDefList.getDataAt(index);
/* 265 */     String type = props.getProperty("type").toLowerCase();
/* 266 */     String filename = props.getProperty("filename");
/* 267 */     String tables = props.getProperty("tables");
/*     */ 
/* 269 */     ResourceFileInfo curFileInfo = this.m_curView.getResourceFileInfo();
/*     */ 
/* 271 */     boolean selChanged = (curFileInfo == null) || (!type.equals(curFileInfo.m_type)) || (!filename.equals(FileUtils.getName(curFileInfo.m_filename)));
/*     */ 
/* 275 */     if ((!selChanged) && (tables != null) && (tables.length() > 0))
/*     */     {
/* 277 */       selChanged = tables.equals(curFileInfo.m_tables);
/*     */     }
/*     */ 
/* 280 */     if ((!selChanged) && (!refreshAll))
/*     */     {
/* 282 */       this.m_curView.assignResourceInfo(props, curFileInfo, this.m_component);
/* 283 */       return;
/*     */     }
/*     */ 
/* 286 */     ResourceFileInfo newFileInfo = this.m_component.retrieveResourceFileInfo(props);
/* 287 */     if (newFileInfo == null)
/*     */     {
/* 289 */       errMsg = "!csCompWizResourceFileInfoError";
/* 290 */       type = "Empty";
/*     */     }
/* 292 */     else if ((newFileInfo.m_errMsg != null) && (newFileInfo.m_errMsg.length() > 0))
/*     */     {
/* 294 */       errMsg = newFileInfo.m_errMsg;
/* 295 */       type = "Empty";
/*     */     }
/* 299 */     else if (isResourceDefTypeValid(type))
/*     */     {
/* 301 */       if (type.equals("resource"))
/*     */       {
/* 303 */         type = determineResourceType(newFileInfo);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 309 */       errMsg = LocaleUtils.encodeMessage("csCompWizResourceTypeInvalid", null, type);
/* 310 */       type = "Empty";
/*     */     }
/*     */ 
/* 314 */     BaseResViewPanel baseView = (BaseResViewPanel)this.m_flipComponents.get(type);
/* 315 */     panelHandler.show(this.m_flipPanel, type);
/* 316 */     this.m_curView = baseView;
/* 317 */     this.m_curView.assignResourceInfo(props, newFileInfo, this.m_component);
/* 318 */     if (errMsg == null)
/*     */       return;
/* 320 */     this.m_helper.m_props.put("HelpMessage", LocaleResources.localizeMessage(errMsg, null));
/* 321 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected boolean isResourceDefTypeValid(String type)
/*     */   {
/* 329 */     return StringUtils.findString(IntradocComponent.RES_DEF, type, 2, 0) != null;
/*     */   }
/*     */ 
/*     */   protected String determineResourceType(ResourceFileInfo fileInfo)
/*     */   {
/* 336 */     String type = null;
/* 337 */     if (fileInfo != null)
/*     */     {
/* 339 */       boolean isTableEmpty = fileInfo.m_tables.isEmpty();
/* 340 */       boolean isIncludesEmpty = (fileInfo.m_htmlIncludes.isEmpty()) && (fileInfo.m_dataIncludes.isEmpty());
/* 341 */       boolean isStrEmpty = fileInfo.m_strings.isEmpty();
/*     */ 
/* 343 */       if ((!isTableEmpty) && (!isIncludesEmpty) && (!isStrEmpty))
/*     */       {
/* 345 */         type = "tablesIncludesStrings";
/*     */       }
/* 347 */       else if ((!isTableEmpty) && (!isIncludesEmpty))
/*     */       {
/* 349 */         type = "tablesIncludes";
/*     */       }
/* 351 */       else if ((!isTableEmpty) && (!isStrEmpty))
/*     */       {
/* 353 */         type = "tablesStrings";
/*     */       }
/* 355 */       else if ((!isIncludesEmpty) && (!isStrEmpty))
/*     */       {
/* 357 */         type = "includesStrings";
/*     */       }
/* 359 */       else if (!isTableEmpty)
/*     */       {
/* 361 */         type = "resourceTables";
/*     */       }
/* 363 */       else if (!isStrEmpty)
/*     */       {
/* 365 */         type = "resStrings";
/*     */       }
/*     */       else
/*     */       {
/* 369 */         type = "htmlDataIncludes";
/*     */       }
/*     */     }
/*     */ 
/* 373 */     return type;
/*     */   }
/*     */ 
/*     */   protected void addResource() {
/* 377 */     String title = "!csCompWizLabelAddResource";
/* 378 */     if ((this.m_component != null) && 
/* 380 */       (CWizardUtils.isReadOnly(this.m_component.m_absCompDir + this.m_component.m_filename)))
/*     */     {
/* 382 */       title = title + LocaleUtils.encodeMessage("csCompWizTitleReadOnly", null, this.m_component.m_filename);
/*     */     }
/*     */ 
/* 386 */     AddResourceDlg dlg = new AddResourceDlg(this.m_systemInterface, LocaleResources.localizeMessage(title, null), null, this.m_component);
/*     */ 
/* 390 */     int index = this.m_resDefList.getSelectedIndex();
/* 391 */     Properties props = this.m_resDefList.getDataAt(index);
/* 392 */     if (props != null)
/*     */     {
/* 394 */       String type = props.getProperty("type").toLowerCase();
/* 395 */       String filename = props.getProperty("filename");
/*     */ 
/* 397 */       if ((type != null) && (type.length() > 0))
/*     */       {
/* 399 */         if (type.equals("resource"))
/*     */         {
/* 401 */           type = "htmlIncludeOrString";
/* 402 */           if ((filename != null) && (filename.length() > 0))
/*     */           {
/* 404 */             if (filename.endsWith(".hda"))
/*     */             {
/* 406 */               type = "dynResTable";
/*     */             }
/*     */             else
/*     */             {
/* 410 */               ResourceFileInfo finfo = this.m_component.retrieveResourceFileInfo(props);
/* 411 */               if ((finfo != null) && (finfo.m_tables != null) && (!finfo.m_tables.isEmpty()) && (((finfo.m_htmlIncludes == null) || (finfo.m_htmlIncludes.isEmpty()))) && (((finfo.m_dataIncludes == null) || (finfo.m_dataIncludes.isEmpty()))) && (((finfo.m_strings == null) || (finfo.m_strings.isEmpty()))))
/*     */               {
/* 417 */                 type = "staticResTable";
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 423 */         dlg.setResourceType(type);
/*     */       }
/*     */     }
/*     */ 
/* 427 */     dlg.init();
/*     */ 
/* 429 */     if (dlg.prompt() != 1)
/*     */       return;
/* 431 */     String compName = dlg.getComponentName();
/* 432 */     props = dlg.getProperties();
/* 433 */     String type = props.getProperty("type");
/* 434 */     String filename = props.getProperty("filename");
/* 435 */     String tables = props.getProperty("tables");
/* 436 */     String loadOrder = props.getProperty("loadOrder");
/* 437 */     String displayTypeName = CWizardUtils.findDisplayName(RESOURCE_NAME_MAP, type);
/*     */     try
/*     */     {
/* 440 */       if ((type == null) || (type.length() == 0))
/*     */       {
/* 442 */         throw new ServiceException("!csCompWizTypeUndefined");
/*     */       }
/*     */ 
/* 445 */       if ((filename == null) || (type.length() == 0))
/*     */       {
/* 447 */         throw new ServiceException("!csCompWizFilenameUndefined");
/*     */       }
/*     */ 
/* 450 */       if (tables == null)
/*     */       {
/* 452 */         tables = "";
/*     */       }
/*     */ 
/* 455 */       if (loadOrder == null)
/*     */       {
/* 457 */         loadOrder = "1";
/*     */       }
/* 459 */       boolean isAppend = StringUtils.convertToBool(props.getProperty("isAppend"), false);
/*     */ 
/* 461 */       boolean isUnique = StringUtils.convertToBool(props.getProperty("isUnique"), false);
/*     */ 
/* 464 */       DataResultSet drset = null;
/* 465 */       String id = null;
/*     */ 
/* 469 */       if ((isAppend) && (!isUnique))
/*     */       {
/* 472 */         drset = findRow(type, filename);
/* 473 */         if (drset != null)
/*     */         {
/* 475 */           id = drset.getStringValue(4);
/*     */ 
/* 479 */           drset.setCurrentValue(2, tables);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 484 */         FieldInfo[] fi = ResultSetUtils.createInfoList(this.m_listData, new String[] { "type", "filename", "tables", "loadOrder", "id", "displayTypeName" }, true);
/*     */ 
/* 486 */         id = Integer.toString(this.m_idCount++);
/* 487 */         Vector v = this.m_listData.createEmptyRow();
/* 488 */         v.setElementAt(type, fi[0].m_index);
/* 489 */         v.setElementAt(filename, fi[1].m_index);
/* 490 */         v.setElementAt(tables, fi[2].m_index);
/* 491 */         v.setElementAt(loadOrder, fi[3].m_index);
/* 492 */         v.setElementAt(id, fi[4].m_index);
/* 493 */         v.setElementAt(displayTypeName, fi[5].m_index);
/* 494 */         this.m_listData.addRow(v);
/*     */       }
/* 496 */       refreshList(id);
/* 497 */       checkSelection();
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 501 */       IdcMessage msg = IdcMessageFactory.lc("csCompWizResourceAddError", new Object[] { compName });
/* 502 */       CWizardGuiUtils.reportError(this.m_systemInterface, exp, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void removeResource()
/*     */   {
/* 509 */     int index = this.m_resDefList.getSelectedIndex();
/* 510 */     if (index < 0)
/*     */     {
/* 512 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizResourceRemoveError", new Object[0]));
/*     */ 
/* 514 */       return;
/*     */     }
/*     */ 
/* 517 */     if (CWizardUtils.isReadOnly(this.m_component.m_absCompDir + this.m_component.m_filename))
/*     */     {
/* 519 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizComponentFileReadOnly", new Object[] { this.m_component.m_filename, this.m_component.m_name }));
/*     */ 
/* 522 */       return;
/*     */     }
/* 524 */     Properties props = this.m_resDefList.getDataAt(index);
/* 525 */     String id = props.getProperty("id");
/* 526 */     String type = props.getProperty("type");
/* 527 */     String filename = props.getProperty("filename");
/*     */ 
/* 529 */     if (CWizardGuiUtils.doMessage(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizRemoveResourcePrompt", new Object[] { type, this.m_component.m_name, filename }), 4) != 2)
/*     */     {
/*     */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 536 */       FieldInfo[] fi = ResultSetUtils.createInfoList(this.m_listData, new String[] { "id" }, true);
/* 537 */       this.m_component.removeResourceFileInfo(props);
/* 538 */       Vector v = this.m_listData.findRow(fi[0].m_index, id);
/* 539 */       if (v != null)
/*     */       {
/* 541 */         this.m_listData.deleteCurrentRow();
/*     */       }
/* 543 */       refreshList(null);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 547 */       IdcMessage msg = IdcMessageFactory.lc("csCompWizResourceRemoveError2", new Object[] { type });
/* 548 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void launchEditor()
/*     */   {
/* 555 */     int index = this.m_resDefList.getSelectedIndex();
/* 556 */     if (index < 0)
/*     */     {
/* 559 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizReqSelectResource", new Object[0]));
/*     */ 
/* 561 */       return;
/*     */     }
/*     */ 
/* 564 */     Properties props = this.m_resDefList.getDataAt(index);
/*     */ 
/* 566 */     String filename = props.getProperty("filename");
/* 567 */     filename = FileUtils.getAbsolutePath(this.m_component.m_absCompDir, filename);
/* 568 */     CWizardGuiUtils.launchEditor(this.m_systemInterface, filename);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String assignComponentInfo(IntradocComponent comp, boolean reloadAll)
/*     */   {
/* 575 */     IdcMessage msg = assignComponentInfo(comp, reloadAll, null);
/* 576 */     return LocaleUtils.encodeMessage(msg);
/*     */   }
/*     */ 
/*     */   public IdcMessage assignComponentInfo(IntradocComponent comp, boolean reloadAll, Map options)
/*     */   {
/* 582 */     IdcMessage errMsg = null;
/* 583 */     boolean isError = false;
/*     */     try
/*     */     {
/* 588 */       if (reloadAll)
/*     */       {
/* 591 */         super.assignComponentInfo(comp, reloadAll, options);
/* 592 */         DataResultSet drset = null;
/* 593 */         if (comp != null)
/*     */         {
/* 595 */           isError = comp.cacheFileInfo();
/* 596 */           drset = this.m_component.getResourceDefTable();
/*     */         }
/*     */ 
/* 599 */         this.m_listData = new DataResultSet(new String[] { "type", "filename", "tables", "loadOrder" });
/*     */ 
/* 601 */         if (drset != null)
/*     */         {
/* 603 */           this.m_listData.copy(drset);
/*     */ 
/* 606 */           Vector infos = ResultSetUtils.createFieldInfo(new String[] { "type", "id", "displayTypeName" }, 30);
/* 607 */           this.m_listData.mergeFieldsWithFlags(infos, 2);
/* 608 */           FieldInfo[] fi = new FieldInfo[3];
/* 609 */           infos.copyInto(fi);
/*     */ 
/* 611 */           for (this.m_listData.first(); this.m_listData.isRowPresent(); this.m_listData.next())
/*     */           {
/* 613 */             Vector v = this.m_listData.getCurrentRowValues();
/* 614 */             v.setElementAt(Integer.toString(this.m_idCount++), fi[1].m_index);
/* 615 */             v.setElementAt(CWizardUtils.findDisplayName(RESOURCE_NAME_MAP, (String)v.elementAt(fi[0].m_index)), fi[2].m_index);
/*     */           }
/*     */         }
/*     */ 
/* 619 */         refreshList(null);
/*     */       }
/*     */       else
/*     */       {
/* 623 */         isError = reloadComponentInfo();
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 628 */       return IdcMessageFactory.lc("csCompWizCompLoadError", new Object[] { comp.m_name });
/*     */     }
/*     */ 
/* 632 */     if (isError)
/*     */     {
/* 634 */       errMsg = CWizardUtils.formatErrorMessage(comp, null);
/*     */     }
/* 636 */     return errMsg;
/*     */   }
/*     */ 
/*     */   protected void refreshList(String selObj)
/*     */   {
/* 641 */     if (this.m_listData == null)
/*     */       return;
/* 643 */     this.m_resDefList.refreshList(this.m_listData, selObj);
/* 644 */     this.m_addBtn.setEnabled(true);
/*     */ 
/* 646 */     if (selObj == null)
/*     */     {
/* 648 */       checkSelection();
/*     */     }
/*     */ 
/* 651 */     int index = this.m_resDefList.getSelectedIndex();
/* 652 */     boolean isEnabled = index >= 0;
/* 653 */     this.m_resDefList.enableDisable(isEnabled);
/*     */   }
/*     */ 
/*     */   protected boolean reloadComponentInfo()
/*     */   {
/* 660 */     int index = this.m_resDefList.getSelectedIndex();
/* 661 */     String obj = this.m_resDefList.getSelectedObj();
/*     */ 
/* 663 */     boolean isError = false;
/* 664 */     if (this.m_component != null)
/*     */     {
/* 666 */       isError = this.m_component.reloadFileInfo();
/* 667 */       refreshList(obj);
/*     */ 
/* 669 */       if (index >= 0)
/*     */       {
/* 671 */         this.m_resDefList.enableDisable(true);
/*     */       }
/* 673 */       checkSelectionEx(true);
/*     */     }
/* 675 */     return isError;
/*     */   }
/*     */ 
/*     */   protected DataResultSet findRow(String type, String filename)
/*     */   {
/* 680 */     DataResultSet drset = this.m_listData.shallowClone();
/*     */     try
/*     */     {
/* 683 */       if (findRowTwoKeyMatch(drset, "type", "filename", type, filename))
/*     */       {
/* 685 */         return drset;
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 690 */       ignore.printStackTrace();
/*     */     }
/*     */ 
/* 693 */     return null;
/*     */   }
/*     */ 
/*     */   protected boolean findRowTwoKeyMatch(DataResultSet drset, String key1, String key2, String val1, String val2)
/*     */     throws DataException
/*     */   {
/* 699 */     int key1Index = ResultSetUtils.getIndexMustExist(drset, key1);
/* 700 */     int key2Index = ResultSetUtils.getIndexMustExist(drset, key2);
/* 701 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 703 */       String rsetval1 = drset.getStringValue(key1Index);
/* 704 */       String rsetval2 = drset.getStringValue(key2Index);
/* 705 */       if ((val1.equals(rsetval1)) && (val2.equals(rsetval2)))
/*     */       {
/* 707 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 711 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 716 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81512 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.ResourceDefPanel
 * JD-Core Version:    0.5.4
 */