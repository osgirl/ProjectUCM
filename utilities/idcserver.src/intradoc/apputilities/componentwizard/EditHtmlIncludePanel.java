/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseOutput;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.io.CharArrayWriter;
/*     */ import java.io.StringReader;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditHtmlIncludePanel extends EditBasePanel
/*     */ {
/*  69 */   protected DataResultSet[] m_dynamicHtmlList = null;
/*  70 */   protected DataResultSet[] m_suggestedHtmlList = null;
/*  71 */   protected CWResourceContainer m_resources = null;
/*  72 */   protected JTextArea m_includeData = null;
/*  73 */   protected JCheckBox[] m_resourceTypeCbox = null;
/*  74 */   protected boolean m_enableSelectBtn = false;
/*     */ 
/*  77 */   protected JButton m_selectBtn = null;
/*  78 */   protected boolean m_isAllChoices = false;
/*     */ 
/*     */   public void initUI(int editType)
/*     */     throws ServiceException
/*     */   {
/*  88 */     String colName = "includeOrString";
/*     */ 
/*  90 */     this.m_isAllChoices = (this.m_resourceType < 0);
/*  91 */     this.m_includeData = new CustomTextArea("", 15, 60);
/*  92 */     if (editType != 2)
/*     */     {
/*  94 */       ItemListener showAllListener = new ItemListener()
/*     */       {
/*     */         public void itemStateChanged(ItemEvent e)
/*     */         {
/*     */           try
/*     */           {
/* 100 */             boolean isAll = EditHtmlIncludePanel.this.m_showAll.isSelected();
/* 101 */             EditHtmlIncludePanel.this.initSelectListData(isAll);
/* 102 */             EditHtmlIncludePanel.this.refreshSelectList(null);
/*     */           }
/*     */           catch (Exception excp)
/*     */           {
/* 106 */             Report.trace("system", null, excp);
/*     */           }
/*     */         }
/*     */       };
/* 111 */       addNewOrUseExistingPanel(this, "includeOrString", true, true, showAllListener);
/*     */       try
/*     */       {
/* 114 */         initSelectListData(false);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 118 */         Report.trace("system", null, e);
/*     */       }
/*     */ 
/* 121 */       this.m_selectBtn.setEnabled(this.m_enableSelectBtn);
/*     */     }
/*     */ 
/* 124 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 125 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(12);
/* 126 */     JScrollPane scrollPane = new JScrollPane(this.m_includeData);
/* 127 */     this.m_helper.m_exchange.addComponent("includeOrStringData", this.m_includeData, null);
/* 128 */     this.m_helper.addComponent(this, scrollPane);
/*     */ 
/* 130 */     if (editType != 2)
/*     */       return;
/* 132 */     String filename = this.m_helper.m_props.getProperty("filename");
/* 133 */     ResourceFileInfo fileInfo = this.m_component.retrieveResourceFileInfo("resource", filename, "null", false);
/*     */ 
/* 136 */     if ((fileInfo == null) || (fileInfo.m_htmlIncludes == null))
/*     */       return;
/* 138 */     String key = this.m_helper.m_props.getProperty(colName);
/* 139 */     Map list = CWizardUtils.getIncludeOrStringMapByType(fileInfo, this.m_resourceType);
/* 140 */     String data = CWizardUtils.findIncludeOrStringData(key, list, this.m_resourceType);
/* 141 */     if (data == null)
/*     */       return;
/* 143 */     this.m_helper.m_props.put("includeOrStringData", data.trim());
/*     */   }
/*     */ 
/*     */   public void initControls(int editType)
/*     */   {
/* 152 */     super.initControls(editType);
/* 153 */     if (this.m_selectBtn != null)
/*     */     {
/* 155 */       this.m_selectBtn.setEnabled(this.m_enableSelectBtn);
/*     */     }
/* 157 */     if (!this.m_isAllChoices)
/*     */       return;
/* 159 */     provideSampleEntry(true);
/*     */   }
/*     */ 
/*     */   protected int determineArrayIndex()
/*     */   {
/* 165 */     int retVal = 0;
/* 166 */     if (this.m_resourceType == 1)
/*     */     {
/* 168 */       retVal = 1;
/*     */     }
/* 170 */     else if (this.m_resourceType == 7)
/*     */     {
/* 172 */       retVal = 1;
/*     */     }
/* 174 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected void addNewOrUseExistingPanel(JPanel panel, String colName, boolean addShowAll, boolean addSortList, ItemListener showAllListener)
/*     */   {
/* 180 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/*     */ 
/* 182 */     if (this.m_isAllChoices)
/*     */     {
/* 184 */       JPanel checkboxPanel = new PanePanel();
/* 185 */       this.m_helper.makePanelGridBag(checkboxPanel, 1);
/*     */ 
/* 187 */       ButtonGroup group = new ButtonGroup();
/* 188 */       gbh.prepareAddRowElement();
/* 189 */       this.m_resourceTypeCbox = new JCheckBox[CWizardUtils.CORE_RESOURCE_TYPES.length];
/* 190 */       for (int i = 0; i < this.m_resourceTypeCbox.length; ++i)
/*     */       {
/* 192 */         boolean isLast = i == this.m_resourceTypeCbox.length - 1;
/* 193 */         boolean isDefaultSelected = i == 0;
/* 194 */         String label = LocaleResources.getString(CWizardUtils.CORE_RESOURCE_TYPE_LABELS[i], null);
/* 195 */         this.m_resourceTypeCbox[i] = new JCheckBox(label, isDefaultSelected);
/* 196 */         group.add(this.m_resourceTypeCbox[i]);
/* 197 */         if (isLast)
/*     */         {
/* 199 */           gbh.prepareAddLastRowElement();
/*     */         }
/* 201 */         this.m_helper.addExchangeComponent(checkboxPanel, this.m_resourceTypeCbox[i], CWizardUtils.CORE_RESOURCE_TYPE_KEYS[i]);
/*     */       }
/*     */ 
/* 205 */       ItemListener iListener = new ItemListener()
/*     */       {
/*     */         public void itemStateChanged(ItemEvent e)
/*     */         {
/* 209 */           int resourceIndex = EditHtmlIncludePanel.this.determineArrayIndexFromCheckBoxes();
/* 210 */           EditHtmlIncludePanel.this.m_resourceType = CWizardUtils.CORE_RESOURCE_TYPES[resourceIndex];
/*     */           try
/*     */           {
/* 213 */             EditHtmlIncludePanel.this.initSelectListData(false);
/*     */           }
/*     */           catch (ServiceException excep)
/*     */           {
/* 217 */             Report.trace("system", null, excep);
/*     */           }
/* 219 */           EditHtmlIncludePanel.this.m_selectBtn.setEnabled(EditHtmlIncludePanel.this.m_enableSelectBtn);
/* 220 */           EditHtmlIncludePanel.this.provideSampleEntry(false);
/*     */         }
/*     */       };
/* 223 */       for (int i = 0; i < this.m_resourceTypeCbox.length; ++i)
/*     */       {
/* 225 */         this.m_resourceTypeCbox[i].addItemListener(iListener);
/*     */       }
/*     */ 
/* 228 */       gbh.prepareAddLastRowElement();
/* 229 */       this.m_helper.addComponent(panel, checkboxPanel);
/*     */     }
/*     */     else
/*     */     {
/* 234 */       int arrayIndex = CWizardUtils.determineArrayIndexFromResourceType(this.m_resourceType);
/* 235 */       String checkBoxKey = CWizardUtils.CORE_RESOURCE_TYPE_KEYS[arrayIndex];
/* 236 */       this.m_helper.m_props.setProperty(checkBoxKey, "1");
/*     */     }
/*     */ 
/* 239 */     this.m_helper.addLabelFieldPairEx(panel, LocaleResources.getString("csCompWizLabelName2", null), new CustomTextField(30), colName, false);
/*     */ 
/* 242 */     gbh.prepareAddRowElement();
/* 243 */     this.m_helper.addComponent(panel, new CustomLabel());
/*     */ 
/* 245 */     gbh.prepareAddLastRowElement();
/* 246 */     this.m_selectBtn = new JButton(LocaleResources.getString("csCompWizCommandSelect", null));
/* 247 */     this.m_helper.addComponent(panel, this.m_selectBtn);
/*     */ 
/* 249 */     ActionListener listener = new ActionListener(addShowAll, addSortList, showAllListener)
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 253 */         EditHtmlIncludePanel.this.selectUseExisting(this.val$addShowAll, this.val$addSortList, this.val$showAllListener);
/*     */       }
/*     */     };
/* 256 */     this.m_selectBtn.addActionListener(listener);
/*     */   }
/*     */ 
/*     */   protected void provideSampleEntry(boolean loadIntoProps)
/*     */   {
/* 261 */     int arrayIndex = CWizardUtils.determineArrayIndexFromResourceType(this.m_resourceType);
/* 262 */     String key = CWizardUtils.CORE_RESOURCE_TYPE_KEYS[arrayIndex];
/* 263 */     Properties oldProps = null;
/* 264 */     DataBinder binder = this.m_component.m_binder;
/* 265 */     String id = "";
/* 266 */     String text = "";
/*     */     try
/*     */     {
/* 269 */       oldProps = binder.getLocalData();
/* 270 */       Properties newProps = (Properties)oldProps.clone();
/* 271 */       newProps.putAll(this.m_helper.m_props);
/* 272 */       binder.setLocalData(newProps);
/*     */ 
/* 274 */       String idInclude = key + "_sample_name";
/* 275 */       String textInclude = key + "_sample_text";
/* 276 */       id = CWizardUtils.retrieveDynamicHtml(binder, idInclude);
/* 277 */       text = CWizardUtils.retrieveDynamicHtml(binder, textInclude);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 281 */       Report.trace("system", null, e);
/*     */     }
/*     */     finally
/*     */     {
/* 285 */       if (oldProps != null)
/*     */       {
/* 287 */         binder.setLocalData(oldProps);
/*     */       }
/*     */     }
/* 290 */     Object[] obj = this.m_helper.m_exchange.findComponent("includeOrString", false);
/* 291 */     if (obj != null)
/*     */     {
/* 293 */       JTextField tfield = (JTextField)obj[1];
/* 294 */       tfield.setText(id);
/*     */     }
/* 296 */     this.m_includeData.setText(text);
/*     */ 
/* 298 */     if (!loadIntoProps)
/*     */       return;
/* 300 */     this.m_helper.m_props.setProperty("includeOrString", id);
/* 301 */     this.m_helper.m_props.setProperty("includeOrStringData", text);
/*     */   }
/*     */ 
/*     */   protected int determineArrayIndexFromCheckBoxes()
/*     */   {
/* 307 */     int retVal = 0;
/* 308 */     for (int i = 0; i < this.m_resourceTypeCbox.length; ++i)
/*     */     {
/* 310 */       if (!this.m_resourceTypeCbox[i].isSelected())
/*     */         continue;
/* 312 */       retVal = i;
/* 313 */       break;
/*     */     }
/*     */ 
/* 316 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected boolean onSelect()
/*     */   {
/*     */     try
/*     */     {
/* 324 */       if (this.m_selectList.getSelectedIndex() < 0)
/*     */       {
/* 326 */         throw new ServiceException("!csCompWizChooseItem");
/*     */       }
/* 328 */       String name = this.m_selectList.getSelectedObj();
/*     */ 
/* 330 */       String data = CWizardUtils.findIncludeOrStringData(name, this.m_resources.m_dynamicHtml, this.m_resourceType);
/* 331 */       this.m_helper.m_props.put("includeOrString", name);
/* 332 */       if (data != null)
/*     */       {
/* 334 */         this.m_helper.m_props.put("includeOrStringData", data.trim());
/*     */       }
/* 336 */       this.m_helper.loadComponentValues();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 340 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/* 341 */       return false;
/*     */     }
/*     */ 
/* 344 */     return true;
/*     */   }
/*     */ 
/*     */   protected void initSelectListData(boolean isAll)
/*     */     throws ServiceException
/*     */   {
/* 351 */     if (this.m_resourceType == 7)
/*     */     {
/* 353 */       this.m_enableSelectBtn = false;
/* 354 */       return;
/*     */     }
/*     */ 
/* 357 */     int arrayIndex = CWizardUtils.determineArrayIndexFromResourceType(this.m_resourceType);
/* 358 */     int workingResType = CWizardUtils.CORE_RESOURCE_TYPES[arrayIndex];
/* 359 */     boolean isHtmlInclude = workingResType == 0;
/* 360 */     String tablename = (isHtmlInclude) ? "htmlInclude.common" : "dataInclude.common";
/* 361 */     if (this.m_suggestedHtmlList == null)
/*     */     {
/* 363 */       this.m_suggestedHtmlList = new DataResultSet[CWizardUtils.CORE_RESOURCE_TYPES.length];
/*     */     }
/* 365 */     if (this.m_suggestedHtmlList[arrayIndex] == null)
/*     */     {
/* 367 */       DataResultSet original = SharedObjects.getTable(tablename);
/* 368 */       if (original != null)
/*     */       {
/* 370 */         DataResultSet drsetCopy = new DataResultSet();
/* 371 */         drsetCopy.copy(original);
/* 372 */         FieldInfo finfo = new FieldInfo();
/* 373 */         drsetCopy.getFieldInfo("description", finfo);
/*     */ 
/* 376 */         for (drsetCopy.first(); drsetCopy.isRowPresent(); drsetCopy.next())
/*     */         {
/* 378 */           Vector v = drsetCopy.getCurrentRowValues();
/* 379 */           String desc = (String)v.elementAt(finfo.m_index);
/* 380 */           int n = drsetCopy.getCurrentRow();
/*     */ 
/* 382 */           v.setElementAt(LocaleResources.getString(desc, null), finfo.m_index);
/* 383 */           drsetCopy.setRowValues(v, n);
/*     */         }
/* 385 */         this.m_suggestedHtmlList[arrayIndex] = drsetCopy;
/*     */       }
/*     */     }
/* 388 */     DataResultSet drset = this.m_suggestedHtmlList[arrayIndex];
/* 389 */     this.m_enableSelectBtn = (drset != null);
/*     */ 
/* 391 */     if (this.m_dynamicHtmlList == null)
/*     */     {
/* 393 */       this.m_dynamicHtmlList = new DataResultSet[CWizardUtils.CORE_RESOURCE_TYPES.length];
/*     */     }
/*     */ 
/* 396 */     if ((this.m_dynamicHtmlList[arrayIndex] == null) && (drset != null))
/*     */     {
/* 398 */       if (this.m_resources == null)
/*     */       {
/* 400 */         String file = FileUtils.getAbsolutePath(LegacyDirectoryLocator.getResourcesDirectory(), "core/idoc/std_page.idoc");
/*     */ 
/* 402 */         this.m_resources = new CWResourceContainer();
/* 403 */         DataLoader.cacheResourceFile(this.m_resources, file);
/*     */       }
/* 405 */       this.m_dynamicHtmlList[arrayIndex] = new DataResultSet(new String[] { "name", "description" });
/*     */ 
/* 407 */       Map dynHtml = (isHtmlInclude) ? this.m_resources.m_dynamicHtml : this.m_resources.m_dynamicData;
/* 408 */       if (dynHtml != null)
/*     */       {
/* 410 */         Iterator it = dynHtml.keySet().iterator();
/*     */ 
/* 413 */         while (it.hasNext())
/*     */         {
/* 415 */           String include = (String)it.next();
/* 416 */           List v = this.m_dynamicHtmlList[arrayIndex].createEmptyRowAsList();
/* 417 */           v.set(0, include);
/*     */ 
/* 419 */           String desc = "";
/*     */ 
/* 422 */           List tempRow = drset.findRow(0, include.trim());
/*     */ 
/* 424 */           if (tempRow != null)
/*     */           {
/* 426 */             desc = (String)tempRow.get(1);
/*     */           }
/* 428 */           v.set(1, desc);
/* 429 */           this.m_dynamicHtmlList[arrayIndex].addRowWithList(v);
/*     */         }
/*     */       }
/*     */     }
/* 433 */     if (isAll)
/*     */     {
/* 435 */       this.m_selectListData = this.m_dynamicHtmlList[arrayIndex];
/*     */     }
/*     */     else
/*     */     {
/* 439 */       this.m_selectListData = this.m_suggestedHtmlList[arrayIndex];
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 448 */     String name = exchange.m_compName;
/* 449 */     String val = exchange.m_compValue;
/* 450 */     IdcMessage errMsg = null;
/*     */ 
/* 452 */     if (name.equals("includeOrString"))
/*     */     {
/* 454 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizLabelHTMLIncludeName", new Object[0]), false, false);
/*     */     }
/* 456 */     else if (name.equals("includeDataOrString"))
/*     */     {
/* 458 */       ParseOutput parseOutput = new ParseOutput();
/*     */       try
/*     */       {
/* 461 */         CharArrayWriter outbuf = new CharArrayWriter();
/* 462 */         parseOutput.m_writer = outbuf;
/* 463 */         StringReader reader = new StringReader(val);
/* 464 */         if (CWParser.findScriptTagEx(reader, parseOutput, '@', true, false) == true)
/*     */         {
/* 467 */           String tag = parseOutput.waitingBufferAsString().trim();
/* 468 */           throw new ServiceException(LocaleUtils.encodeMessage("csCompWizHTMLInvalidTagDef", null, tag));
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 473 */         errMsg = IdcMessageFactory.lc(e);
/*     */       }
/*     */       finally
/*     */       {
/* 477 */         parseOutput.releaseBuffers();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 482 */       return super.validateComponentValue(exchange);
/*     */     }
/*     */ 
/* 485 */     if (errMsg != null)
/*     */     {
/* 487 */       exchange.m_errorMessage = errMsg;
/* 488 */       return false;
/*     */     }
/* 490 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 495 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82874 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.EditHtmlIncludePanel
 * JD-Core Version:    0.5.4
 */