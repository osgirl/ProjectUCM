/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.gui.iwt.IdcList;
/*     */ import intradoc.shared.ClausesData;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.ListSelectionModel;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ 
/*     */ public class ClauseBuilderHelper
/*     */   implements ComponentBinder, ItemListener, ActionListener, ListSelectionListener
/*     */ {
/*     */   protected static final short TEXT_OP = 0;
/*     */   protected static final short DATE_OP = 1;
/*  70 */   protected ContainerHelper m_guiHelper = null;
/*     */ 
/*  73 */   protected SystemInterface m_sysInterface = null;
/*     */ 
/*  75 */   protected ExecutionContext m_cxt = null;
/*     */ 
/*  78 */   protected ClausesData m_clauseData = null;
/*     */ 
/*  81 */   protected Vector m_fieldDefs = null;
/*     */ 
/*  84 */   protected String[][] m_nameCaptions = (String[][])null;
/*     */ 
/*  88 */   protected String[] m_excludedFields = null;
/*     */ 
/*  93 */   protected DocumentLocalizedProfile m_docProfile = null;
/*     */ 
/*  96 */   protected Hashtable m_displayMaps = null;
/*     */ 
/*  99 */   protected FixedSizeList m_clauseList = null;
/*     */ 
/* 102 */   protected JButton m_addBtn = null;
/* 103 */   protected JButton m_deleteBtn = null;
/* 104 */   protected JButton m_updateBtn = null;
/*     */ 
/* 108 */   protected JPanel m_operPanel = null;
/* 109 */   protected JPanel m_clauseValPanel = null;
/*     */ 
/* 112 */   protected JPanel m_clauseEditPanel = null;
/*     */ 
/* 115 */   protected Vector m_curClause = null;
/* 116 */   protected int m_numSegments = 3;
/*     */ 
/* 119 */   protected String m_curOptionListKey = null;
/*     */ 
/* 122 */   protected String m_fieldTitle = null;
/* 123 */   protected String m_valueTitle = null;
/*     */ 
/* 126 */   protected Vector m_sortFieldDefs = null;
/*     */ 
/*     */   public void init(SystemInterface sysInterface)
/*     */   {
/* 135 */     this.m_sysInterface = sysInterface;
/* 136 */     this.m_cxt = this.m_sysInterface.getExecutionContext();
/*     */ 
/* 138 */     this.m_curClause = null;
/* 139 */     this.m_curOptionListKey = null;
/* 140 */     this.m_docProfile = null;
/* 141 */     this.m_displayMaps = null;
/*     */ 
/* 143 */     this.m_fieldTitle = LocaleResources.getString("apLabelField", this.m_cxt);
/* 144 */     this.m_valueTitle = LocaleResources.getString("apLabelValue", this.m_cxt);
/*     */   }
/*     */ 
/*     */   public void setTitles(String fieldTitle, String valueTitle)
/*     */   {
/* 149 */     this.m_fieldTitle = fieldTitle;
/* 150 */     this.m_valueTitle = valueTitle;
/*     */   }
/*     */ 
/*     */   public void setNumSegments(int num)
/*     */   {
/* 155 */     this.m_numSegments = num;
/*     */   }
/*     */ 
/*     */   public void setFieldList(Vector fieldDefs, String[] excludedFields)
/*     */   {
/* 160 */     this.m_fieldDefs = fieldDefs;
/* 161 */     this.m_excludedFields = excludedFields;
/*     */ 
/* 163 */     this.m_nameCaptions = new String[this.m_fieldDefs.size()][2];
/* 164 */     for (int i = 0; i < this.m_nameCaptions.length; ++i)
/*     */     {
/* 166 */       FieldDef fieldDef = (FieldDef)this.m_fieldDefs.elementAt(i);
/* 167 */       this.m_nameCaptions[i][0] = fieldDef.m_name;
/* 168 */       this.m_nameCaptions[i][1] = fieldDef.m_caption;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setSortFieldList(Vector sortFieldDefs)
/*     */   {
/* 174 */     this.m_sortFieldDefs = sortFieldDefs;
/*     */   }
/*     */ 
/*     */   public void setDocumentProfile(DocumentLocalizedProfile docProfile)
/*     */   {
/* 179 */     this.m_docProfile = docProfile;
/*     */   }
/*     */ 
/*     */   public void setDisplayMaps(Hashtable displayMaps)
/*     */   {
/* 184 */     this.m_displayMaps = displayMaps;
/*     */   }
/*     */ 
/*     */   public void setDisplayMap(String field, String[][] optListMap)
/*     */   {
/* 189 */     this.m_displayMaps.put(field, optListMap);
/*     */   }
/*     */ 
/*     */   public void setFieldOptionListKey(String field, String key, String optType)
/*     */   {
/* 194 */     int size = this.m_fieldDefs.size();
/* 195 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 197 */       FieldDef fieldDef = (FieldDef)this.m_fieldDefs.elementAt(i);
/* 198 */       if (!fieldDef.m_name.equals(field))
/*     */         continue;
/* 200 */       fieldDef.m_optionListKey = key;
/*     */ 
/* 202 */       if (optType == null)
/*     */         return;
/* 204 */       fieldDef.m_optionListType = optType;
/* 205 */       fieldDef.m_isOptionList = true; return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void createEditButtonsPanel(JPanel editButtonsPanel, ContainerHelper guiHelper, GridBagHelper gh)
/*     */   {
/* 215 */     this.m_addBtn = new JButton(LocaleResources.getString("apButtonAdd", this.m_cxt));
/* 216 */     this.m_updateBtn = new JButton(LocaleResources.getString("apButtonUpdate", this.m_cxt));
/*     */ 
/* 218 */     editButtonsPanel.setLayout(new GridBagLayout());
/* 219 */     gh.m_gc.fill = 1;
/* 220 */     guiHelper.addComponent(editButtonsPanel, this.m_addBtn);
/* 221 */     guiHelper.addLastComponentInRow(editButtonsPanel, this.m_updateBtn);
/*     */   }
/*     */ 
/*     */   public JPanel createStandardClausePanel(ContainerHelper guiHelper, JPanel queryDefinitionPanel, String title)
/*     */   {
/* 227 */     GridBagHelper gh = guiHelper.m_gridHelper;
/*     */ 
/* 229 */     JPanel queryEditArea = new CustomPanel();
/*     */ 
/* 231 */     guiHelper.makePanelGridBag(queryDefinitionPanel, 1);
/* 232 */     gh.m_gc.weightx = 1.0D;
/* 233 */     gh.m_gc.weighty = 1.0D;
/* 234 */     guiHelper.addLastComponentInRow(queryDefinitionPanel, queryEditArea);
/*     */ 
/* 237 */     gh.useGridBag(queryEditArea);
/*     */ 
/* 239 */     JPanel clauseEditPanel = new PanePanel();
/*     */ 
/* 241 */     JPanel editButtonsPanel = new PanePanel();
/* 242 */     createEditButtonsPanel(editButtonsPanel, guiHelper, gh);
/*     */ 
/* 244 */     JPanel selectedItemButtons = new PanePanel();
/* 245 */     selectedItemButtons.setLayout(new GridBagLayout());
/* 246 */     gh.m_gc.fill = 1;
/* 247 */     this.m_deleteBtn = new JButton(LocaleResources.getString("apButtonDelete", this.m_cxt));
/* 248 */     guiHelper.addLastComponentInRow(selectedItemButtons, this.m_deleteBtn);
/*     */ 
/* 250 */     gh.m_gc.insets = new Insets(0, 5, 0, 5);
/* 251 */     gh.m_gc.anchor = 18;
/*     */ 
/* 253 */     FixedSizeList clauseList = new FixedSizeList(4);
/* 254 */     clauseList.m_list.setFixedCellWidth(400);
/*     */ 
/* 256 */     gh.m_gc.fill = 2;
/* 257 */     gh.m_gc.weightx = 1.0D;
/* 258 */     guiHelper.addLastComponentInRow(queryEditArea, clauseEditPanel);
/* 259 */     gh.m_gc.anchor = 17;
/* 260 */     gh.m_gc.weightx = 0.1D;
/* 261 */     guiHelper.addLastComponentInRow(queryEditArea, editButtonsPanel);
/* 262 */     guiHelper.addLastComponentInRow(queryEditArea, new CustomLabel(title));
/* 263 */     gh.m_gc.fill = 1;
/* 264 */     gh.m_gc.weightx = 1.0D;
/* 265 */     gh.m_gc.weighty = 10.0D;
/* 266 */     guiHelper.addComponent(queryEditArea, clauseList);
/* 267 */     gh.m_gc.weightx = 0.1D;
/* 268 */     guiHelper.addLastComponentInRow(queryEditArea, selectedItemButtons);
/* 269 */     gh.m_gc.weighty = 0.0D;
/*     */ 
/* 274 */     registerButtons();
/* 275 */     gh.m_gc.fill = 0;
/* 276 */     gh.m_gc.anchor = 10;
/* 277 */     registerComponents(guiHelper, clauseList, clauseEditPanel);
/*     */ 
/* 279 */     return queryEditArea;
/*     */   }
/*     */ 
/*     */   public void registerButtons()
/*     */   {
/* 284 */     this.m_addBtn.addActionListener(this);
/* 285 */     this.m_addBtn.setActionCommand("add");
/*     */ 
/* 287 */     this.m_deleteBtn.addActionListener(this);
/* 288 */     this.m_deleteBtn.setActionCommand("delete");
/*     */ 
/* 290 */     this.m_updateBtn.addActionListener(this);
/* 291 */     this.m_updateBtn.setActionCommand("update");
/*     */   }
/*     */ 
/*     */   public void registerComponents(ContainerHelper guiHelper, FixedSizeList clauseList, JPanel clauseEditPanel)
/*     */   {
/* 298 */     this.m_guiHelper = new ContainerHelper();
/* 299 */     this.m_guiHelper.m_gridHelper = guiHelper.m_gridHelper;
/* 300 */     this.m_guiHelper.m_props = guiHelper.m_props;
/* 301 */     this.m_guiHelper.m_exchange.m_sysInterface = this.m_sysInterface;
/*     */ 
/* 303 */     this.m_clauseList = clauseList;
/* 304 */     this.m_clauseEditPanel = clauseEditPanel;
/*     */ 
/* 306 */     this.m_clauseList.m_list.getSelectionModel().addListSelectionListener(this);
/*     */ 
/* 308 */     this.m_clauseEditPanel.setLayout(new GridBagLayout());
/* 309 */     guiHelper.addLastComponentInRow(this.m_clauseEditPanel, this.m_clauseValPanel = new PanePanel(false));
/* 310 */     this.m_clauseValPanel.setLayout(new GridBagLayout());
/*     */   }
/*     */ 
/*     */   public void setData(ClausesData clauseData, String data)
/*     */   {
/* 315 */     this.m_clauseData = clauseData;
/* 316 */     if (data != null)
/*     */     {
/* 318 */       this.m_clauseData.parse(data);
/*     */     }
/*     */ 
/* 321 */     setPanelData();
/*     */ 
/* 324 */     String indexStr = this.m_clauseData.getQueryProp("CurrentIndex");
/* 325 */     if (indexStr == null)
/*     */       return;
/* 327 */     int index = Integer.parseInt(indexStr);
/* 328 */     if ((index < 0) || (index >= this.m_clauseData.m_clauses.size()))
/*     */       return;
/* 330 */     this.m_curClause = ((Vector)this.m_clauseData.m_clauses.elementAt(index));
/*     */   }
/*     */ 
/*     */   protected void setPanelData()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void loadData()
/*     */     throws ServiceException
/*     */   {
/* 342 */     refreshClauseList();
/* 343 */     exchangeQueryInfo(true);
/*     */   }
/*     */ 
/*     */   public String[][] getCaptionsMap()
/*     */   {
/* 348 */     return this.m_nameCaptions;
/*     */   }
/*     */ 
/*     */   public void saveCurrentSelection()
/*     */   {
/* 354 */     int index = this.m_clauseList.getSelectedIndex();
/* 355 */     this.m_clauseData.setQueryProp("CurrentIndex", Integer.toString(index));
/*     */   }
/*     */ 
/*     */   public String getFormatString()
/*     */   {
/* 360 */     saveCurrentSelection();
/* 361 */     return this.m_clauseData.formatString();
/*     */   }
/*     */ 
/*     */   public void markQueryPropForUrl(String key)
/*     */   {
/* 366 */     this.m_clauseData.markQueryPropForUrl(key);
/*     */   }
/*     */ 
/*     */   public String getQueryProp(String key)
/*     */   {
/* 371 */     return this.m_clauseData.getQueryProp(key);
/*     */   }
/*     */ 
/*     */   public void setQueryProp(String key, String value)
/*     */   {
/* 376 */     this.m_clauseData.setQueryProp(key, value);
/*     */   }
/*     */ 
/*     */   public ClausesData getClauseData()
/*     */   {
/* 381 */     return this.m_clauseData;
/*     */   }
/*     */ 
/*     */   public void addFieldList(DisplayChoice cf)
/*     */   {
/* 389 */     addFieldListEx(cf, this.m_fieldDefs, this.m_excludedFields);
/*     */   }
/*     */ 
/*     */   public void addSortFieldList(DisplayChoice cf)
/*     */   {
/* 394 */     addFieldListEx(cf, this.m_sortFieldDefs, null);
/*     */   }
/*     */ 
/*     */   public void addFieldListEx(DisplayChoice cf, Vector fieldDefs, String[] excludedFields)
/*     */   {
/* 399 */     Vector fields = new IdcVector();
/* 400 */     int size = fieldDefs.size();
/* 401 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 403 */       FieldDef fieldDef = (FieldDef)fieldDefs.elementAt(i);
/*     */ 
/* 406 */       boolean isExcluded = false;
/* 407 */       if (excludedFields != null)
/*     */       {
/* 409 */         for (int j = 0; j < excludedFields.length; ++j)
/*     */         {
/* 411 */           if (!excludedFields[j].equals(fieldDef.m_name))
/*     */             continue;
/* 413 */           isExcluded = true;
/* 414 */           break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 419 */       if (isExcluded)
/*     */         continue;
/* 421 */       String[] map = new String[2];
/* 422 */       map[0] = fieldDef.m_name;
/* 423 */       map[1] = fieldDef.m_caption;
/*     */ 
/* 425 */       fields.addElement(map);
/*     */     }
/*     */ 
/* 429 */     int num = fields.size();
/* 430 */     String[][] display = new String[num][2];
/* 431 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 433 */       display[i] = ((String[])(String[])fields.elementAt(i));
/*     */     }
/* 435 */     cf.init(display);
/*     */   }
/*     */ 
/*     */   protected ComboChoice getOptChoiceList(String optKey)
/*     */   {
/* 440 */     if (optKey == null)
/*     */     {
/* 442 */       return null;
/*     */     }
/*     */ 
/* 445 */     Vector optList = null;
/* 446 */     if (this.m_docProfile != null)
/*     */     {
/* 449 */       optList = this.m_docProfile.getOptionList(optKey, false);
/*     */     }
/* 451 */     if (optList == null)
/*     */     {
/* 454 */       optList = SharedObjects.getOptList(optKey);
/*     */     }
/* 456 */     if (optList == null)
/*     */     {
/* 458 */       return null;
/*     */     }
/* 460 */     int nopts = optList.size();
/* 461 */     ComboChoice values = new ComboChoice();
/* 462 */     for (int i = 0; i < nopts; ++i)
/*     */     {
/* 464 */       values.addItem((String)optList.elementAt(i));
/*     */     }
/* 466 */     return values;
/*     */   }
/*     */ 
/*     */   public void valueChanged(ListSelectionEvent e)
/*     */   {
/* 474 */     enableDisable(true);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 482 */     enableDisable(true);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 490 */     String cmd = e.getActionCommand();
/* 491 */     if (cmd.equals("add"))
/*     */     {
/* 493 */       Vector oldClause = this.m_curClause;
/* 494 */       this.m_curClause = new IdcVector();
/* 495 */       this.m_curClause.setSize(this.m_numSegments);
/* 496 */       if (exchangeQueryInfo(false) == true)
/*     */       {
/* 498 */         Vector clauses = this.m_clauseData.m_clauses;
/*     */ 
/* 500 */         clauses.addElement(this.m_curClause);
/*     */         try
/*     */         {
/* 503 */           refreshClauseList();
/*     */         }
/*     */         catch (ServiceException ignore)
/*     */         {
/* 507 */           if (SystemUtils.m_verbose)
/*     */           {
/* 509 */             Report.debug("system", null, ignore);
/*     */           }
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 515 */         this.m_curClause = oldClause;
/*     */       }
/*     */     }
/* 518 */     else if (cmd.equals("delete"))
/*     */     {
/* 520 */       Vector clauses = this.m_clauseData.m_clauses;
/*     */ 
/* 522 */       clauses.removeElement(this.m_curClause);
/* 523 */       this.m_curClause = null;
/*     */       try
/*     */       {
/* 526 */         refreshClauseList();
/*     */       }
/*     */       catch (ServiceException ignore)
/*     */       {
/* 530 */         if (SystemUtils.m_verbose)
/*     */         {
/* 532 */           Report.debug("system", null, ignore);
/*     */         }
/*     */       }
/*     */     } else {
/* 536 */       if ((!cmd.equals("update")) || 
/* 538 */         (exchangeQueryInfo(false) != true))
/*     */         return;
/*     */       try
/*     */       {
/* 542 */         refreshClauseList();
/*     */       }
/*     */       catch (ServiceException ignore)
/*     */       {
/* 546 */         if (!SystemUtils.m_verbose)
/*     */           return;
/* 548 */         Report.debug("system", null, ignore);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean loadSelection)
/*     */   {
/* 558 */     boolean isSelected = false;
/* 559 */     boolean isCustom = this.m_clauseData.m_isCustom;
/* 560 */     int sel = -1;
/* 561 */     if (!isCustom)
/*     */     {
/* 563 */       sel = this.m_clauseList.getSelectedIndex();
/* 564 */       isSelected = sel >= 0;
/*     */     }
/*     */ 
/* 567 */     this.m_addBtn.setEnabled(!isCustom);
/* 568 */     this.m_clauseList.setEnabled(!isCustom);
/*     */ 
/* 570 */     this.m_deleteBtn.setEnabled(isSelected);
/* 571 */     this.m_updateBtn.setEnabled(isSelected);
/*     */ 
/* 573 */     if ((loadSelection != true) || (isCustom))
/*     */       return;
/* 575 */     this.m_curClause = null;
/* 576 */     if (isSelected)
/*     */     {
/* 578 */       this.m_curClause = ((Vector)this.m_clauseData.m_clauses.elementAt(sel));
/*     */     }
/* 580 */     exchangeQueryInfo(true);
/*     */   }
/*     */ 
/*     */   public boolean exchangeQueryInfo(boolean updateComponents)
/*     */   {
/* 588 */     return this.m_guiHelper.m_exchange.exchange(this, updateComponents);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 596 */     String name = exchange.m_compName;
/* 597 */     if (updateComponent)
/*     */     {
/* 599 */       exchange.m_compValue = getQueryProp(name);
/*     */     }
/*     */     else
/*     */     {
/* 603 */       setQueryProp(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 609 */     String val = exchange.m_compValue;
/*     */ 
/* 611 */     if (val != null)
/*     */     {
/* 613 */       val = val.trim();
/* 614 */       if (val.length() == 0)
/*     */       {
/* 616 */         val = null;
/*     */       }
/*     */     }
/* 619 */     return true;
/*     */   }
/*     */ 
/*     */   protected void refreshClauseList()
/*     */     throws ServiceException
/*     */   {
/* 627 */     this.m_clauseList.removeAllItems();
/*     */ 
/* 630 */     Vector clauses = this.m_clauseData.m_clauses;
/* 631 */     int nclauses = clauses.size();
/*     */ 
/* 633 */     int curIndex = -1;
/*     */ 
/* 636 */     for (int i = 0; i < nclauses; ++i)
/*     */     {
/* 638 */       StringBuffer dispStr = new StringBuffer();
/* 639 */       Vector elts = (Vector)clauses.elementAt(i);
/* 640 */       if (this.m_curClause == elts)
/*     */       {
/* 642 */         curIndex = i;
/*     */       }
/* 644 */       int size = elts.size();
/* 645 */       for (int j = 0; j < size; ++j)
/*     */       {
/* 647 */         String str = (String)elts.elementAt(j);
/* 648 */         dispStr.append(str);
/* 649 */         if (j >= size - 1)
/*     */           continue;
/* 651 */         dispStr.append(' ');
/*     */       }
/*     */ 
/* 654 */       this.m_clauseList.add(dispStr.toString());
/*     */     }
/*     */ 
/* 657 */     if (curIndex >= 0)
/*     */     {
/* 659 */       this.m_clauseList.select(curIndex);
/*     */     }
/*     */     else
/*     */     {
/* 663 */       this.m_curClause = null;
/*     */     }
/*     */ 
/* 667 */     this.m_clauseData.m_dispStr = "Standard Query";
/*     */ 
/* 670 */     enableDisable(false);
/*     */   }
/*     */ 
/*     */   protected boolean checkSelectedClauseDocField()
/*     */   {
/* 676 */     return true;
/*     */   }
/*     */ 
/*     */   public void reportError(Exception e, String msg)
/*     */   {
/* 682 */     MessageBox.reportError(this.m_sysInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 687 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80447 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.ClauseBuilderHelper
 * JD-Core Version:    0.5.4
 */