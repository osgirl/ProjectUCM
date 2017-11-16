/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.text.JTextComponent;
/*     */ 
/*     */ public class PatchEditorDlg extends CWizardBaseDlg
/*     */   implements ActionListener, ItemListener
/*     */ {
/*  65 */   protected final String[][] PATCH_COMMAND_LIST = { { "csCompWizCommandAdd", "add", "0" }, { "csCompWizCommandEdit", "edit", "1" }, { "csCompWizCommandRemove", "remove", "1" } };
/*     */   public static final String PATCH_TABLE_NAME = "PatchInfoTable";
/*  74 */   protected UdlPanel m_list = null;
/*  75 */   protected DataResultSet m_listData = null;
/*  76 */   protected DialogHelper m_dlgHelper = null;
/*     */ 
/*  78 */   protected String[][] m_patchFields = (String[][])null;
/*  79 */   protected Hashtable m_fieldTables = null;
/*  80 */   protected Vector m_fieldVector = null;
/*     */ 
/*     */   public PatchEditorDlg(SystemInterface sys, String title, String helpPage, IntradocComponent component)
/*     */   {
/*  85 */     super(sys, title, helpPage);
/*  86 */     this.m_component = component;
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/*  92 */     if ((this.m_patchFields == null) || (this.m_listData == null))
/*     */     {
/*  94 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizErrorPatchTablesCorrupt", new Object[0]));
/*     */ 
/*  96 */       return -1;
/*     */     }
/*  98 */     return super.prompt();
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 104 */     this.m_fieldTables = new Hashtable();
/*     */     try
/*     */     {
/* 107 */       DataResultSet drset = null;
/*     */ 
/* 109 */       drset = SharedObjects.getTable("PatchSettingsFieldsTable");
/* 110 */       if (drset != null)
/*     */       {
/* 112 */         this.m_patchFields = ResultSetUtils.createStringTable(drset, null);
/* 113 */         this.m_fieldVector = ResultSetUtils.loadValuesFromSet(drset, "name");
/*     */ 
/* 115 */         String[][] choiceTable = ResultSetUtils.createFilteredStringTable(drset, new String[] { "type", "name", "options" }, "choice");
/*     */ 
/* 120 */         if (choiceTable != null)
/*     */         {
/* 122 */           int len = choiceTable.length;
/* 123 */           for (int i = 0; i < len; ++i)
/*     */           {
/* 125 */             String val = choiceTable[i][0];
/* 126 */             String tableName = choiceTable[i][1];
/* 127 */             DataResultSet choiceSet = SharedObjects.getTable(tableName);
/* 128 */             if (drset == null)
/*     */               continue;
/* 130 */             String[][] fieldTable = ResultSetUtils.createStringTable(choiceSet, null);
/*     */ 
/* 132 */             LocaleResources.localizeDoubleArray(fieldTable, null, 1);
/* 133 */             this.m_fieldTables.put(val, fieldTable);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException d)
/*     */     {
/* 142 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizErrorPatchTablesCorrupt", new Object[0]));
/*     */ 
/* 144 */       return;
/*     */     }
/* 146 */     if (this.m_patchFields == null)
/*     */     {
/* 148 */       return;
/*     */     }
/*     */ 
/* 151 */     initUI();
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/* 157 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 162 */         boolean retVal = PatchEditorDlg.this.savePatchTable();
/* 163 */         return retVal;
/*     */       }
/*     */     };
/* 167 */     DataBinder binder = new DataBinder();
/* 168 */     mergeComponentInfo(binder, false);
/* 169 */     DataBinder.mergeHashTables(this.m_helper.m_props, binder.getLocalData());
/*     */ 
/* 171 */     JPanel mainPanel = getDialogHelper().initStandard(this, okCallback, 1, false, null);
/*     */ 
/* 174 */     JPanel panel = new PanePanel();
/* 175 */     this.m_helper.makePanelGridBag(panel, 2);
/*     */ 
/* 177 */     this.m_list = createUdlPanel("", 600, 15, "PatchInfoTable", true, this.m_patchFields, this.m_patchFields[0][0], false);
/*     */ 
/* 180 */     DataResultSet drset = SharedObjects.getTable("ServerVersionInfo");
/* 181 */     String[][] infoTable = (String[][])null;
/*     */     try
/*     */     {
/* 184 */       infoTable = ResultSetUtils.createStringTable(drset, null);
/*     */     }
/*     */     catch (DataException d)
/*     */     {
/* 188 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizErrorPatchTablesCorrupt", new Object[0]));
/*     */ 
/* 190 */       return;
/*     */     }
/* 192 */     ComboChoice choice = new ComboChoice();
/* 193 */     choice.setName("RemoveAfterVersion");
/* 194 */     choice.initChoiceList(infoTable);
/*     */ 
/* 196 */     this.m_helper.addLabelFieldPair(panel, LocaleResources.getString("csCompWizLabelRemoveAfterBuild", null), choice, "RemoveAfterVersion");
/*     */ 
/* 200 */     this.m_helper.addLabelEditPair(panel, LocaleResources.getString("csCompWizLabelDirectoryFilter", null), 40, "DirectoryFilter");
/*     */ 
/* 203 */     JPanel buttonPanel = new PanePanel();
/* 204 */     this.m_helper.makePanelGridBag(buttonPanel, 2);
/* 205 */     addCommandButtons(buttonPanel);
/* 206 */     this.m_list.add("East", buttonPanel);
/*     */ 
/* 208 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/*     */ 
/* 210 */     gbh.prepareAddLastRowElement();
/* 211 */     gbh.m_gc.weighty = 0.0D;
/* 212 */     gbh.m_gc.fill = 1;
/* 213 */     this.m_helper.addComponent(mainPanel, panel);
/* 214 */     gbh.m_gc.weighty = 0.0D;
/*     */ 
/* 216 */     gbh.m_gc.weighty = 1.0D;
/* 217 */     gbh.m_gc.fill = 1;
/* 218 */     this.m_helper.addComponent(mainPanel, this.m_list);
/* 219 */     gbh.m_gc.weighty = 0.0D;
/*     */ 
/* 221 */     this.m_helper.loadComponentValues();
/* 222 */     this.m_listData = ((DataResultSet)binder.getResultSet("PatchInfoTable"));
/* 223 */     String[] fieldList = StringUtils.convertListToArray(this.m_fieldVector);
/* 224 */     if (this.m_listData == null)
/*     */     {
/* 227 */       this.m_listData = new DataResultSet(fieldList);
/*     */     }
/*     */     else
/*     */     {
/* 231 */       DataResultSet tmpSet = new DataResultSet(fieldList);
/* 232 */       this.m_listData.mergeFields(tmpSet);
/*     */ 
/* 234 */       if (this.m_listData.getNumFields() > fieldList.length)
/*     */       {
/* 236 */         Vector delFields = new IdcVector();
/*     */         try
/*     */         {
/* 241 */           FieldInfo[] finfo = ResultSetUtils.createInfoList(this.m_listData, null, false);
/*     */ 
/* 244 */           for (int i = 0; i < finfo.length; ++i)
/*     */           {
/* 246 */             if (StringUtils.findStringIndex(fieldList, finfo[i].m_name) >= 0) {
/*     */               continue;
/*     */             }
/* 249 */             delFields.addElement(finfo[i].m_name);
/*     */           }
/*     */ 
/* 253 */           if (delFields.size() > 0)
/*     */           {
/* 255 */             this.m_listData.removeFields(StringUtils.convertListToArray(delFields));
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (DataException d)
/*     */         {
/* 261 */           this.m_listData = null;
/* 262 */           return;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 268 */     this.m_list.refreshList(this.m_listData, null);
/*     */   }
/*     */ 
/*     */   protected void addOrEdit(boolean isEdit)
/*     */   {
/* 273 */     this.m_dlgHelper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("csCompWizCommandAdd2", null), true);
/*     */ 
/* 276 */     String origVal = null;
/* 277 */     if (isEdit)
/*     */     {
/* 279 */       int index = this.m_list.getSelectedIndex();
/* 280 */       Properties props = new Properties();
/* 281 */       if (index >= 0)
/*     */       {
/* 283 */         this.m_listData.setCurrentRow(index);
/* 284 */         props = this.m_listData.getCurrentRowProps();
/* 285 */         origVal = (String)props.get(this.m_patchFields[0][0]);
/*     */       }
/* 287 */       DataBinder.mergeHashTables(this.m_dlgHelper.m_props, props);
/*     */     }
/*     */ 
/* 290 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 295 */         return PatchEditorDlg.this.onOk();
/*     */       }
/*     */     };
/* 299 */     JPanel mainPanel = this.m_dlgHelper.initStandard(this, okCallback, 1, false, null);
/*     */ 
/* 301 */     this.m_dlgHelper.makePanelGridBag(mainPanel, 1);
/* 302 */     GridBagHelper gbh = this.m_dlgHelper.m_gridHelper;
/*     */ 
/* 304 */     int size = this.m_patchFields.length;
/* 305 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 307 */       String id = this.m_patchFields[i][0];
/* 308 */       String type = this.m_patchFields[i][3];
/* 309 */       if (type.equalsIgnoreCase("text"))
/*     */       {
/* 311 */         int length = NumberUtils.parseInteger(this.m_patchFields[i][4], 20);
/* 312 */         gbh.m_gc.weighty = 0.0D;
/* 313 */         this.m_dlgHelper.addLabelEditPair(mainPanel, LocaleResources.localizeMessage(this.m_patchFields[i][1], null), length, id);
/*     */       }
/* 317 */       else if (type.equalsIgnoreCase("longtext"))
/*     */       {
/* 319 */         gbh.m_gc.weighty = 1.0D;
/*     */ 
/* 321 */         JTextComponent customText = new CustomTextArea(10, 40);
/* 322 */         this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.localizeMessage(this.m_patchFields[i][1], null), customText, id);
/*     */ 
/* 326 */         gbh.m_gc.weighty = 0.0D;
/*     */       } else {
/* 328 */         if (!type.equalsIgnoreCase("choice"))
/*     */           continue;
/* 330 */         gbh.m_gc.weighty = 0.0D;
/*     */ 
/* 332 */         String[][] fieldTable = (String[][])(String[][])this.m_fieldTables.get(id);
/* 333 */         if (fieldTable == null)
/*     */         {
/* 335 */           CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizErrorPatchTablesCorrupt", new Object[0]));
/*     */         }
/*     */         else
/*     */         {
/* 340 */           DisplayChoice choice = new DisplayChoice();
/* 341 */           choice.init(fieldTable);
/*     */ 
/* 343 */           DisplayStringCallbackAdaptor dspCallback = new DisplayStringCallbackAdaptor()
/*     */           {
/*     */             public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */             {
/* 350 */               String[][] table = (String[][])(String[][])PatchEditorDlg.this.m_fieldTables.get(name);
/*     */ 
/* 353 */               String displayStr = null;
/* 354 */               if (table != null)
/*     */               {
/* 356 */                 displayStr = StringUtils.getPresentationString(table, value);
/*     */               }
/*     */ 
/* 359 */               if (displayStr == null)
/*     */               {
/* 361 */                 displayStr = value;
/*     */               }
/* 363 */               return displayStr;
/*     */             }
/*     */           };
/* 366 */           this.m_list.setDisplayCallback(id, dspCallback);
/*     */ 
/* 368 */           this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.localizeMessage(this.m_patchFields[i][1], null), choice, id);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 374 */     FieldInfo[] fi = null;
/*     */     try
/*     */     {
/* 377 */       String[] fieldList = StringUtils.convertListToArray(this.m_fieldVector);
/* 378 */       fi = ResultSetUtils.createInfoList(this.m_listData, fieldList, true);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 382 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/* 383 */       return;
/*     */     }
/*     */ 
/* 386 */     if (this.m_dlgHelper.prompt() != 1)
/*     */       return;
/* 388 */     Vector row = this.m_listData.createEmptyRow();
/*     */ 
/* 390 */     int len = this.m_patchFields.length;
/* 391 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 393 */       String key = this.m_patchFields[i][0];
/* 394 */       String type = this.m_patchFields[i][3];
/* 395 */       String val = null;
/* 396 */       if ((type.equalsIgnoreCase("text")) || (type.equalsIgnoreCase("longtext")))
/*     */       {
/* 399 */         val = this.m_dlgHelper.m_props.getProperty(key);
/*     */       }
/* 401 */       else if (type.equalsIgnoreCase("choice"))
/*     */       {
/* 403 */         Object[] comp = this.m_dlgHelper.m_exchange.findComponent(key, false);
/* 404 */         if ((comp != null) && (comp[1] instanceof DisplayChoice))
/*     */         {
/* 406 */           DisplayChoice choice = (DisplayChoice)comp[1];
/* 407 */           val = choice.getSelectedInternalValue();
/*     */         }
/*     */       }
/*     */ 
/* 411 */       if (i == 0)
/*     */       {
/* 413 */         Vector v = this.m_listData.findRow(fi[0].m_index, val);
/* 414 */         if ((v != null) && (!isEdit))
/*     */         {
/* 416 */           CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizPromptEntryExistsWithID", new Object[] { val }));
/*     */ 
/* 418 */           return;
/*     */         }
/*     */       }
/*     */ 
/* 422 */       if (val == null)
/*     */       {
/* 424 */         val = "";
/*     */       }
/*     */ 
/* 427 */       row.setElementAt(val, fi[i].m_index);
/*     */     }
/*     */ 
/* 430 */     Vector oldRow = null;
/* 431 */     if (isEdit)
/*     */     {
/* 433 */       oldRow = this.m_listData.findRow(fi[0].m_index, origVal);
/*     */     }
/*     */ 
/* 436 */     if (oldRow != null)
/*     */     {
/* 438 */       this.m_listData.setRowValues(row, this.m_listData.getCurrentRow());
/*     */     }
/*     */     else
/*     */     {
/* 442 */       this.m_listData.addRow(row);
/*     */     }
/*     */ 
/* 445 */     this.m_list.refreshList(this.m_listData, null);
/*     */   }
/*     */ 
/*     */   protected boolean onOk()
/*     */   {
/* 451 */     boolean retVal = true;
/*     */ 
/* 453 */     Properties props = this.m_dlgHelper.m_props;
/* 454 */     String id = props.getProperty("id");
/* 455 */     String type = props.getProperty("type");
/*     */ 
/* 457 */     if ((id == null) || (id.length() == 0))
/*     */     {
/* 459 */       retVal = false;
/* 460 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizErrorPatchIDNeeded", new Object[0]));
/*     */     }
/*     */ 
/* 463 */     if ((type == null) || (type.length() == 0))
/*     */     {
/* 465 */       retVal = false;
/* 466 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizErrorPatchTypeNeeded", new Object[0]));
/*     */     }
/*     */ 
/* 469 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected void remove()
/*     */   {
/* 474 */     int index = this.m_list.getSelectedIndex();
/* 475 */     if (index < 0)
/*     */     {
/* 477 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizSelectItemToRemove", new Object[0]));
/*     */ 
/* 479 */       return;
/*     */     }
/*     */ 
/* 482 */     this.m_listData.deleteRow(index);
/* 483 */     this.m_list.refreshList(this.m_listData, null);
/*     */   }
/*     */ 
/*     */   protected boolean savePatchTable()
/*     */   {
/* 488 */     Properties props = this.m_helper.m_props;
/* 489 */     String version = (String)props.get("RemoveAfterVersion");
/* 490 */     String filter = (String)props.get("DirectoryFilter");
/*     */ 
/* 492 */     DataBinder binder = new DataBinder();
/* 493 */     binder.addResultSet("PatchInfoTable", this.m_listData);
/* 494 */     binder.putLocal("RemoveAfterVersion", version);
/* 495 */     binder.putLocal("DirectoryFilter", filter);
/* 496 */     mergeComponentInfo(binder, true);
/*     */ 
/* 498 */     return true;
/*     */   }
/*     */ 
/*     */   protected void addCommandButtons(JPanel panel)
/*     */   {
/* 503 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 5, 5);
/* 504 */     for (int i = 0; i < this.PATCH_COMMAND_LIST.length; ++i)
/*     */     {
/* 506 */       boolean isControlled = StringUtils.convertToBool(this.PATCH_COMMAND_LIST[i][2], false);
/*     */ 
/* 508 */       JButton button = this.m_list.addButton(LocaleResources.getString(this.PATCH_COMMAND_LIST[i][0], null), isControlled);
/*     */ 
/* 511 */       this.m_helper.addLastComponentInRow(panel, button);
/*     */ 
/* 513 */       button.setActionCommand(this.PATCH_COMMAND_LIST[i][1]);
/* 514 */       button.addActionListener(this);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 520 */     String cmdStr = e.getActionCommand();
/*     */ 
/* 522 */     if (cmdStr.equals("remove"))
/*     */     {
/* 524 */       remove();
/*     */     }
/* 526 */     else if (cmdStr.equals("add"))
/*     */     {
/* 528 */       addOrEdit(false);
/*     */     } else {
/* 530 */       if (!cmdStr.equals("edit"))
/*     */         return;
/* 532 */       addOrEdit(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void mergeComponentInfo(DataBinder binder, boolean updateParent)
/*     */   {
/* 538 */     if ((this.m_component == null) || (binder == null))
/*     */     {
/* 540 */       return;
/*     */     }
/*     */ 
/* 543 */     if (updateParent)
/*     */     {
/* 545 */       this.m_component.m_binder.merge(binder);
/*     */       try
/*     */       {
/* 548 */         this.m_component.updateResDefFile();
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 552 */         CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizErrorUnableToUpdateResource", new Object[0]));
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 558 */       binder.merge(this.m_component.m_binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 566 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 567 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 573 */     return super.validateComponentValue(exchange);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent event)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 583 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.PatchEditorDlg
 * JD-Core Version:    0.5.4
 */