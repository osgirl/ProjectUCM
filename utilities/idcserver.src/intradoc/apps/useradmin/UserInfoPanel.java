/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.BasePanel;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.MetaFieldUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class UserInfoPanel extends BasePanel
/*     */   implements Observer, ActionListener
/*     */ {
/*     */   public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97558 $";
/*     */   protected UdlPanel m_metaList;
/*     */   protected JButton m_databaseUpdate;
/*     */   protected JButton m_up;
/*     */   protected JButton m_down;
/*     */   protected Vector m_addFields;
/*     */   protected Vector m_changeFields;
/*     */   protected Vector m_deleteFields;
/*     */ 
/*     */   public void init(SystemInterface sys)
/*     */     throws ServiceException
/*     */   {
/*  91 */     super.init(sys);
/*     */ 
/*  94 */     refreshList(null);
/*     */ 
/*  96 */     AppLauncher.addSubjectObserver("metadata", this);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/* 102 */     JPanel mainPanel = new PanePanel();
/* 103 */     JPanel centerPanel = new PanePanel();
/*     */ 
/* 105 */     mainPanel.setLayout(new GridBagLayout());
/*     */ 
/* 108 */     initList();
/*     */ 
/* 111 */     DisplayStringCallback dispCallback = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 117 */         if (name.equals("umdName"))
/*     */         {
/* 119 */           value = UserInfoPanel.this.formatDisplayName(value);
/*     */         }
/* 121 */         else if (name.equals("isCustom"))
/*     */         {
/* 123 */           String newVal = StringUtils.getPresentationString(TableFields.YESNO_OPTIONLIST, value);
/* 124 */           if (newVal == null)
/*     */           {
/* 126 */             newVal = LocaleResources.getString("apLabelNo", null);
/*     */           }
/* 128 */           value = newVal;
/*     */         }
/* 130 */         else if (name.equals("umdType"))
/*     */         {
/* 132 */           String newVal = StringUtils.getPresentationString(TableFields.METAFIELD_TYPES_OPTIONSLIST, value);
/* 133 */           if (newVal == null)
/*     */           {
/* 135 */             newVal = "Text";
/* 136 */             Report.trace(null, "Unrecognized user field type " + value + " -- defaulting to <Text>", null);
/*     */           }
/* 138 */           value = newVal;
/*     */         }
/* 140 */         else if (name.equals("umdIsOptionList"))
/*     */         {
/* 142 */           value = (StringUtils.convertToBool(value, false)) ? LocaleResources.getString("apTrue", UserInfoPanel.this.m_cxt) : LocaleResources.getString("apFalse", UserInfoPanel.this.m_cxt);
/*     */         }
/* 146 */         else if (name.equals("umdCaption"))
/*     */         {
/* 148 */           value = LocaleResources.getString(value, UserInfoPanel.this.m_cxt);
/*     */         }
/* 150 */         return value;
/*     */       }
/*     */     };
/* 153 */     this.m_metaList.setDisplayCallback("umdName", dispCallback);
/* 154 */     this.m_metaList.setDisplayCallback("umdType", dispCallback);
/* 155 */     this.m_metaList.setDisplayCallback("umdIsOptionList", dispCallback);
/* 156 */     this.m_metaList.setDisplayCallback("isCustom", dispCallback);
/* 157 */     this.m_metaList.setDisplayCallback("umdCaption", dispCallback);
/*     */ 
/* 160 */     JPanel headerPanel = new PanePanel();
/* 161 */     JPanel moveButtonPanel = new PanePanel();
/*     */ 
/* 163 */     this.m_up = new JButton(LocaleResources.getString("apLabelUp", this.m_cxt));
/* 164 */     this.m_down = new JButton(LocaleResources.getString("apLabelDown", this.m_cxt));
/* 165 */     this.m_up.setActionCommand("up");
/* 166 */     this.m_up.addActionListener(this);
/* 167 */     this.m_down.setActionCommand("down");
/* 168 */     this.m_down.addActionListener(this);
/* 169 */     this.m_metaList.addControlComponent(this.m_up);
/* 170 */     this.m_metaList.addControlComponent(this.m_down);
/*     */ 
/* 172 */     this.m_helper.makePanelGridBag(moveButtonPanel, 0);
/* 173 */     this.m_helper.makePanelGridBag(headerPanel, 2);
/* 174 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.8D;
/* 175 */     this.m_helper.addComponent(moveButtonPanel, new CustomLabel(LocaleResources.getString("apLabelFieldInfo", this.m_cxt), 2));
/*     */ 
/* 178 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 179 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 2, 0, 2);
/* 180 */     this.m_helper.addComponent(moveButtonPanel, this.m_up);
/* 181 */     this.m_helper.addComponent(moveButtonPanel, this.m_down);
/* 182 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.2D;
/* 183 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 0, 0, 0);
/* 184 */     this.m_helper.addComponent(headerPanel, moveButtonPanel);
/*     */ 
/* 186 */     this.m_metaList.add("North", headerPanel);
/*     */ 
/* 189 */     this.m_helper.makePanelGridBag(centerPanel, 1);
/* 190 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 0, 0, 0);
/* 191 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 192 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 193 */     this.m_helper.addLastComponentInRow(centerPanel, this.m_metaList);
/*     */ 
/* 195 */     this.m_helper.m_gridHelper.m_gc.gridwidth = -1;
/* 196 */     this.m_helper.addComponent(mainPanel, centerPanel);
/*     */ 
/* 198 */     this.m_helper.makePanelGridBag(this, 1);
/* 199 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 200 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 201 */     this.m_helper.addLastComponentInRow(this, new CustomText(""));
/*     */ 
/* 203 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 204 */     this.m_helper.addLastComponentInRow(this, mainPanel);
/*     */ 
/* 206 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 207 */     this.m_helper.addLastComponentInRow(this, new CustomText(""));
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent evt)
/*     */   {
/* 214 */     Object src = evt.getSource();
/* 215 */     if (src == this.m_metaList.m_list)
/*     */     {
/* 217 */       Properties data = null;
/*     */ 
/* 220 */       int index = this.m_metaList.getSelectedIndex();
/* 221 */       if (index >= 0)
/*     */       {
/* 223 */         data = this.m_metaList.getDataAt(index);
/* 224 */         addOrEditMeta(false, data);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 229 */       String cmd = evt.getActionCommand();
/* 230 */       actionByCommand(cmd);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void actionByCommand(String cmd)
/*     */   {
/* 236 */     Properties data = null;
/* 237 */     String fieldName = null;
/*     */ 
/* 240 */     int index = this.m_metaList.getSelectedIndex();
/* 241 */     if (index >= 0)
/*     */     {
/* 243 */       data = this.m_metaList.getDataAt(index);
/* 244 */       fieldName = data.getProperty("umdName");
/*     */     }
/*     */ 
/* 248 */     if (cmd.equals("add"))
/*     */     {
/* 250 */       addOrEditMeta(true, null);
/*     */     }
/* 252 */     else if (cmd.equals("edit"))
/*     */     {
/* 254 */       if (index < 0)
/*     */       {
/* 256 */         reportError(IdcMessageFactory.lc("apSelectUserMeta", new Object[0]));
/* 257 */         return;
/*     */       }
/* 259 */       addOrEditMeta(false, data);
/*     */     }
/* 261 */     else if (cmd.equals("delete"))
/*     */     {
/* 263 */       if (index < 0)
/*     */       {
/* 265 */         reportError(IdcMessageFactory.lc("apSelectUserMeta", new Object[0]));
/* 266 */         return;
/*     */       }
/* 268 */       if (fieldName.startsWith("d"))
/*     */       {
/* 270 */         reportError(IdcMessageFactory.lc("apUnableToDeleteSystemField", new Object[0]));
/* 271 */         return;
/*     */       }
/* 273 */       deleteMeta(data);
/*     */     }
/* 275 */     else if (cmd.equals("up"))
/*     */     {
/* 277 */       if (index < 0)
/*     */       {
/* 279 */         reportError(IdcMessageFactory.lc("apSelectUserMeta", new Object[0]));
/* 280 */         return;
/*     */       }
/* 282 */       if (index == 0)
/*     */       {
/* 284 */         return;
/*     */       }
/*     */ 
/* 287 */       moveUp(index);
/*     */     }
/* 289 */     else if (cmd.equals("down"))
/*     */     {
/* 291 */       int size = this.m_metaList.getNumRows();
/*     */ 
/* 293 */       if (index < 0)
/*     */       {
/* 295 */         reportError(IdcMessageFactory.lc("apSelectUserMeta", new Object[0]));
/* 296 */         return;
/*     */       }
/* 298 */       if (index == size - 1)
/*     */       {
/* 300 */         return;
/*     */       }
/*     */ 
/* 303 */       moveDown(index);
/*     */     } else {
/* 305 */       if (!cmd.equals("updateDB"))
/*     */         return;
/* 307 */       updateDatabase();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void enableOrDisable()
/*     */   {
/*     */   }
/*     */ 
/*     */   protected JPanel initList()
/*     */   {
/* 319 */     this.m_metaList = new UdlPanel("", null, 275, 20, "UserMetaDefinition", true);
/* 320 */     this.m_metaList.init();
/* 321 */     this.m_metaList.useDefaultListener();
/* 322 */     this.m_metaList.m_list.addActionListener(this);
/*     */ 
/* 325 */     String columns = "umdName,isCustom,umdType,umdCaption,umdIsOptionList";
/*     */ 
/* 328 */     this.m_metaList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apUserInfoTitleName", this.m_cxt), "umdName", 10.0D));
/*     */ 
/* 330 */     this.m_metaList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apUserInfoTitleIsCustom", this.m_cxt), "isCustom", 6.0D));
/*     */ 
/* 332 */     this.m_metaList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apUserInfoTitleType", this.m_cxt), "umdType", 5.0D));
/*     */ 
/* 334 */     this.m_metaList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apUserInfoTitleCaption", this.m_cxt), "umdCaption", 6.0D));
/*     */ 
/* 336 */     this.m_metaList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apUserInfoTitleIsOptionList", this.m_cxt), "umdIsOptionList", 6.0D));
/*     */ 
/* 338 */     this.m_metaList.setVisibleColumns(columns);
/* 339 */     this.m_metaList.setIDColumn("umdName");
/* 340 */     this.m_metaList.addItemListener(new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 344 */         int sel = UserInfoPanel.this.m_metaList.getSelectedIndex();
/* 345 */         boolean isSelected = sel >= 0;
/*     */ 
/* 347 */         int nlinks = UserInfoPanel.this.m_metaList.getNumRows();
/* 348 */         UserInfoPanel.this.m_up.setEnabled((isSelected) && (sel > 0));
/* 349 */         UserInfoPanel.this.m_down.setEnabled((isSelected) && (sel < nlinks - 1));
/*     */       }
/*     */     });
/* 355 */     String[][] buttonInfo = { { LocaleResources.getString("apDlgButtonAdd", this.m_cxt), "add", "0" }, { LocaleResources.getString("apDlgButtonEdit", this.m_cxt), "edit", "1" }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "delete", "1" } };
/*     */ 
/* 362 */     JPanel btnActionsPanel = new PanePanel();
/* 363 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 365 */       JButton btn = new JButton(buttonInfo[i][0]);
/* 366 */       btn.setActionCommand(buttonInfo[i][1]);
/* 367 */       btn.addActionListener(this);
/* 368 */       btnActionsPanel.add(btn);
/*     */ 
/* 370 */       boolean isListControlled = StringUtils.convertToBool(buttonInfo[i][2], false);
/* 371 */       if (!isListControlled)
/*     */         continue;
/* 373 */       this.m_metaList.addControlComponent(btn);
/*     */     }
/*     */ 
/* 377 */     btnActionsPanel.add(new CustomText(""));
/*     */ 
/* 380 */     this.m_databaseUpdate = new JButton(LocaleResources.getString("apUpdateDatabaseDesign", this.m_cxt));
/* 381 */     this.m_databaseUpdate.setActionCommand("updateDB");
/* 382 */     this.m_databaseUpdate.addActionListener(this);
/* 383 */     btnActionsPanel.add(this.m_databaseUpdate);
/*     */ 
/* 386 */     this.m_metaList.add("South", btnActionsPanel);
/*     */ 
/* 388 */     return this.m_metaList;
/*     */   }
/*     */ 
/*     */   protected void refreshList(String selectedObj) throws ServiceException
/*     */   {
/* 393 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 396 */       AppLauncher.executeService("GET_USER_METADEFS", binder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 400 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 403 */     DataResultSet metaFields = (DataResultSet)binder.getResultSet("Users");
/* 404 */     DataResultSet drset = SharedObjects.getTable("UserMetaDefinition");
/*     */     try
/*     */     {
/* 410 */       FieldInfo fi = new FieldInfo();
/* 411 */       boolean isCustomFieldExists = drset.getFieldInfo("isCustom", fi);
/*     */ 
/* 414 */       if (!isCustomFieldExists)
/*     */       {
/* 416 */         Vector newFields = new IdcVector();
/*     */ 
/* 418 */         fi.m_name = "isCustom";
/* 419 */         newFields.addElement(fi);
/* 420 */         drset.mergeFieldsWithFlags(newFields, 2);
/*     */       }
/*     */ 
/* 423 */       int nameIndex = ResultSetUtils.getIndexMustExist(drset, "umdName");
/* 424 */       int customIndex = fi.m_index;
/* 425 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 427 */         String field = drset.getStringValue(nameIndex);
/* 428 */         if (isCustomField(field))
/*     */         {
/* 430 */           drset.setCurrentValue(customIndex, "1");
/*     */         }
/*     */         else
/*     */         {
/* 434 */           drset.setCurrentValue(customIndex, "0");
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 440 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 443 */     this.m_metaList.refreshList(drset, selectedObj);
/*     */ 
/* 446 */     String[] rFields = { "dName", "dPasswordEncoding", "dPassword", "dUserAuthType", "dUserOrgPath", "dUserSourceOrgPath", "dUserSourceFlags", "dUserArriveDate", "dUserChangeDate" };
/*     */ 
/* 448 */     metaFields.removeFields(rFields);
/*     */ 
/* 451 */     this.m_addFields = new IdcVector();
/* 452 */     this.m_changeFields = new IdcVector();
/* 453 */     this.m_deleteFields = new IdcVector();
/*     */ 
/* 455 */     boolean changesNeeded = MetaFieldUtils.createDiffList(metaFields, drset, this.m_addFields, this.m_changeFields, this.m_deleteFields);
/*     */ 
/* 457 */     this.m_databaseUpdate.setEnabled(changesNeeded);
/*     */   }
/*     */ 
/*     */   protected void addOrEditMeta(boolean isAdd, Properties data)
/*     */   {
/*     */     String helpPageName;
/*     */     String title;
/*     */     String helpPageName;
/* 465 */     if (!isAdd)
/*     */     {
/* 467 */       String fieldName = formatDisplayName(data.getProperty("umdName"));
/* 468 */       String title = LocaleResources.getString("apEditCustomInfoField", this.m_cxt, fieldName);
/* 469 */       helpPageName = "EditUserMetaFields";
/*     */     }
/*     */     else
/*     */     {
/* 473 */       data = promptNewFieldName();
/* 474 */       if (data == null)
/*     */       {
/* 476 */         return;
/*     */       }
/*     */ 
/* 479 */       String fieldName = formatDisplayName(data.getProperty("FieldName"));
/* 480 */       title = LocaleResources.getString("apAddCustomInfoField", this.m_cxt, fieldName);
/* 481 */       helpPageName = "AddUserMetaFields";
/*     */     }
/*     */ 
/* 485 */     EditUserMetafieldDlg dlg = new EditUserMetafieldDlg(this.m_systemInterface, title, this.m_metaList.getResultSet(), isAdd, DialogHelpTable.getHelpPage(helpPageName));
/*     */     try
/*     */     {
/* 490 */       dlg.init(data);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 494 */       reportError(e);
/* 495 */       return;
/*     */     }
/*     */ 
/* 498 */     if (dlg.prompt() != 1)
/*     */       return;
/*     */     try
/*     */     {
/* 502 */       refreshList(data.getProperty("umdName"));
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 506 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteMeta(Properties data)
/*     */   {
/* 513 */     String fieldName = formatDisplayName(data.getProperty("umdName"));
/*     */ 
/* 515 */     if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifyMetaDelete", new Object[] { fieldName }), 4) != 2) {
/*     */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 521 */       DataBinder binder = new DataBinder();
/* 522 */       binder.setLocalData(data);
/* 523 */       binder.putLocal("action", "DELETE");
/*     */ 
/* 525 */       AppLauncher.executeService("UPDATE_USER_META", binder);
/* 526 */       refreshList(null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 530 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void moveUp(int index)
/*     */   {
/* 537 */     DataBinder binder = new DataBinder();
/* 538 */     binder.putLocal("action", "MOVE_UP");
/* 539 */     binder.putLocal("row", "" + index);
/*     */     try
/*     */     {
/* 543 */       AppLauncher.executeService("UPDATE_USER_META", binder);
/* 544 */       refreshList(this.m_metaList.getDataAt(index).getProperty("umdName"));
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 548 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void moveDown(int index)
/*     */   {
/* 554 */     DataBinder binder = new DataBinder();
/* 555 */     binder.putLocal("action", "MOVE_DOWN");
/* 556 */     binder.putLocal("row", "" + index);
/*     */     try
/*     */     {
/* 560 */       AppLauncher.executeService("UPDATE_USER_META", binder);
/* 561 */       refreshList(this.m_metaList.getDataAt(index).getProperty("umdName"));
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 565 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean checkInDeletedList(String name) {
/* 570 */     Object[] dellist = this.m_deleteFields.toArray();
/*     */ 
/* 572 */     for (int i = 0; i < dellist.length; ++i)
/*     */     {
/* 574 */       FieldInfo fi = (FieldInfo)dellist[i];
/* 575 */       if (fi.m_name.equalsIgnoreCase(name))
/*     */       {
/* 577 */         return true;
/*     */       }
/*     */     }
/* 580 */     return false;
/*     */   }
/*     */ 
/*     */   protected Properties promptNewFieldName()
/*     */   {
/* 585 */     ComponentValidator cmpValidator = new ComponentValidator(this.m_metaList.getResultSet());
/* 586 */     int maxLength = cmpValidator.getMaxLength("umdName", 30) - 1;
/*     */ 
/* 588 */     DialogCallback okCallback = new DialogCallback(maxLength)
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 594 */         formatNameField();
/* 595 */         Properties promptData = this.m_dlgHelper.m_props;
/* 596 */         String name = promptData.getProperty("FieldName");
/* 597 */         int val = Validation.checkDatabaseFieldName(name);
/* 598 */         switch (val)
/*     */         {
/*     */         case 0:
/* 602 */           if ((UserInfoPanel.this.m_metaList.findRowPrimaryField(name) < 0) && (!UserInfoPanel.this.checkInDeletedList(name)))
/*     */           {
/* 605 */             if (name.length() > this.val$maxLength)
/*     */             {
/* 607 */               this.m_errorMessage = IdcMessageFactory.lc("apInfoFieldNameExceedsMaxLength", new Object[] { Integer.valueOf(this.val$maxLength) });
/* 608 */               return false;
/*     */             }
/* 610 */             promptData.put("umdName", name);
/* 611 */             return true;
/*     */           }
/* 613 */           this.m_errorMessage = IdcMessageFactory.lc("apInfoFieldNameConflict", new Object[0]);
/* 614 */           break;
/*     */         case -1:
/* 616 */           this.m_errorMessage = IdcMessageFactory.lc("apSpecifyInfoFieldName", new Object[0]);
/* 617 */           break;
/*     */         case -2:
/* 619 */           this.m_errorMessage = IdcMessageFactory.lc("apNameCannotContainSpaces", new Object[0]);
/* 620 */           break;
/*     */         case -3:
/* 622 */           this.m_errorMessage = IdcMessageFactory.lc("apInvalidCharInFieldName", new Object[0]);
/* 623 */           break;
/*     */         default:
/* 625 */           this.m_errorMessage = IdcMessageFactory.lc("apInvalidNameForInfoField", new Object[0]);
/*     */         }
/*     */ 
/* 628 */         return false;
/*     */       }
/*     */ 
/*     */       protected void formatNameField()
/*     */       {
/* 633 */         String name = this.m_dlgHelper.m_props.getProperty("FieldName");
/* 634 */         if (name.equals(""))
/*     */         {
/* 636 */           return;
/*     */         }
/*     */ 
/* 640 */         name = name.substring(0, 1).toUpperCase() + name.substring(1);
/* 641 */         this.m_dlgHelper.m_props.put("FieldName", "u" + name);
/*     */       }
/*     */     };
/* 646 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apTitleAddCustomInfoField", this.m_cxt), true);
/*     */ 
/* 648 */     JPanel mainPanel = helper.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage("AddCustomUserInfo"));
/*     */ 
/* 650 */     helper.addLabelFieldPair(mainPanel, this.m_systemInterface.localizeCaption("apLabelFieldName"), new CustomTextField(20), "FieldName");
/*     */ 
/* 654 */     if (helper.prompt() == 1)
/*     */     {
/* 657 */       Properties props = helper.m_props;
/* 658 */       props.put("umdType", "Text");
/* 659 */       props.put("umdIsEnabled", "1");
/* 660 */       props.put("umdCaption", props.getProperty("FieldName").substring(1));
/*     */ 
/* 662 */       return props;
/*     */     }
/* 664 */     return null;
/*     */   }
/*     */ 
/*     */   protected void updateDatabase()
/*     */   {
/*     */     try
/*     */     {
/* 674 */       Vector fieldsToDelete = new IdcVector();
/* 675 */       if (!promptDesignChanged(fieldsToDelete))
/*     */       {
/* 677 */         return;
/*     */       }
/*     */ 
/* 681 */       Vector delList = new IdcVector();
/* 682 */       for (int i = 0; i < fieldsToDelete.size(); ++i)
/*     */       {
/* 684 */         FieldInfo fi = (FieldInfo)fieldsToDelete.elementAt(i);
/* 685 */         delList.addElement(fi.m_name);
/*     */       }
/* 687 */       String delStr = StringUtils.createString(delList, ',', ',');
/* 688 */       Properties props = new Properties();
/* 689 */       props.put("UserMetaFieldsToDelete", delStr);
/* 690 */       AppLauncher.executeService("UPDATE_USER_META_TABLE", props);
/*     */ 
/* 693 */       refreshList(null);
/*     */     }
/*     */     catch (ServiceException excep)
/*     */     {
/* 697 */       reportError(excep);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean promptDesignChanged(Vector delFields)
/*     */   {
/* 704 */     JCheckBox[] delBoxes = null;
/*     */ 
/* 706 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apUpdateDatabaseDesign", this.m_cxt), true);
/*     */ 
/* 708 */     JPanel mainPanel = helper.initStandard(null, null, 2, true, DialogHelpTable.getHelpPage("UpdatingDocInfoFields"));
/*     */ 
/* 711 */     if (this.m_addFields.size() > 0)
/*     */     {
/* 713 */       JPanel subPanel = createFieldListSubPanel(mainPanel, LocaleResources.getString("apInfoFieldsToBeAdded", this.m_cxt));
/*     */ 
/* 715 */       addFieldList(subPanel, this.m_addFields, false);
/*     */     }
/* 717 */     if (this.m_changeFields.size() > 0)
/*     */     {
/* 719 */       JPanel subPanel = createFieldListSubPanel(mainPanel, LocaleResources.getString("apInfoFieldsToBeModified", this.m_cxt));
/*     */ 
/* 721 */       addFieldList(subPanel, this.m_changeFields, false);
/*     */     }
/* 723 */     if (this.m_deleteFields.size() > 0)
/*     */     {
/* 725 */       JPanel subPanel = createFieldListSubPanel(mainPanel, LocaleResources.getString("apInfoFieldsToBeDeleted", this.m_cxt));
/*     */ 
/* 727 */       delBoxes = addFieldList(subPanel, this.m_deleteFields, true);
/*     */     }
/*     */ 
/* 731 */     if (helper.prompt() == 1)
/*     */     {
/* 733 */       if (delBoxes != null)
/*     */       {
/* 735 */         for (int i = 0; i < delBoxes.length; ++i)
/*     */         {
/* 737 */           if (!delBoxes[i].isSelected())
/*     */             continue;
/* 739 */           delFields.addElement(this.m_deleteFields.elementAt(i));
/*     */         }
/*     */       }
/*     */ 
/* 743 */       return true;
/*     */     }
/*     */ 
/* 746 */     return false;
/*     */   }
/*     */ 
/*     */   protected JPanel createFieldListSubPanel(JPanel panel, String title)
/*     */   {
/* 752 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 753 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 754 */     JPanel fieldList = new PanePanel();
/* 755 */     this.m_helper.addComponent(panel, fieldList);
/*     */ 
/* 757 */     this.m_helper.makePanelGridBag(fieldList, 0);
/* 758 */     this.m_helper.addPanelTitle(fieldList, title);
/* 759 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(10);
/* 760 */     return fieldList;
/*     */   }
/*     */ 
/*     */   protected JCheckBox[] addFieldList(JPanel panel, Vector v, boolean isSelectable)
/*     */   {
/* 767 */     int n = v.size();
/* 768 */     JCheckBox[] boxes = null;
/* 769 */     if (isSelectable)
/*     */     {
/* 771 */       boxes = new JCheckBox[n];
/*     */     }
/* 773 */     for (int i = 0; i < n; ++i)
/*     */     {
/* 775 */       FieldInfo fi = (FieldInfo)v.elementAt(i);
/* 776 */       Component comp = null;
/* 777 */       String name = fi.m_name;
/* 778 */       if (isSelectable)
/*     */       {
/* 780 */         JCheckBox box = new JCheckBox(name);
/* 781 */         box.setBackground(getBackground());
/* 782 */         box.setSelected(true);
/* 783 */         boxes[i] = box;
/* 784 */         comp = box;
/*     */       }
/*     */       else
/*     */       {
/* 788 */         comp = new CustomLabel(name);
/*     */       }
/* 790 */       this.m_helper.addComponent(panel, comp);
/*     */     }
/* 792 */     return boxes;
/*     */   }
/*     */ 
/*     */   protected String formatDisplayName(String value)
/*     */   {
/* 797 */     char ch = value.charAt(0);
/* 798 */     if ((ch == 'u') || (ch == 'd'))
/*     */     {
/* 800 */       value = value.substring(1);
/*     */     }
/* 802 */     return value;
/*     */   }
/*     */ 
/*     */   protected boolean isCustomField(String value)
/*     */   {
/* 807 */     char ch = value.charAt(0);
/*     */ 
/* 810 */     return ch == 'u';
/*     */   }
/*     */ 
/*     */   public boolean canExit()
/*     */   {
/* 818 */     if (((this.m_changeFields != null) && (this.m_changeFields.size() > 0)) || ((this.m_addFields != null) && (this.m_addFields.size() > 0)) || ((this.m_deleteFields != null) && (this.m_deleteFields.size() > 0)))
/*     */     {
/* 826 */       return MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apDatabaseDesignIsInvalid", new Object[0]), 4) == 2;
/*     */     }
/*     */ 
/* 831 */     return true;
/*     */   }
/*     */ 
/*     */   public void update(Observable obs, Object arg)
/*     */   {
/* 839 */     String selectedObj = this.m_metaList.getSelectedObj();
/*     */     try
/*     */     {
/* 843 */       refreshList(selectedObj);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 847 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeNotify()
/*     */   {
/* 854 */     AppLauncher.removeSubjectObserver("metadata", this);
/* 855 */     super.removeNotify();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 860 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97558 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.UserInfoPanel
 * JD-Core Version:    0.5.4
 */