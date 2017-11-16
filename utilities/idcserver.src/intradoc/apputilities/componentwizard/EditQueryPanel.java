/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ 
/*     */ public class EditQueryPanel extends EditBasePanel
/*     */ {
/*  56 */   public static final String[][] PARAM_COL_MAP = { { "paramName", "!csCompWizLabelName", "20" }, { "paramType", "!csCompWizLabelType", "20" } };
/*     */ 
/*     */   public void initUI(int editType)
/*     */   {
/*     */     try
/*     */     {
/*  70 */       JPanel panel = new PanePanel();
/*  71 */       this.m_helper.makePanelGridBag(panel, 1);
/*     */ 
/*  73 */       if ((editType == 1) || (editType == 0))
/*     */       {
/*  75 */         this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  76 */         addNewOrUseExistingPanel(panel, "name", false, true, null, false, null);
/*     */       }
/*  78 */       this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  79 */       this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  80 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  81 */       this.m_helper.addComponent(panel, new CustomLabel(LocaleResources.getString("csCompWizLabelQuery", null), 1));
/*     */ 
/*  83 */       JTextArea queryStr = new CustomTextArea("", 5, 60);
/*     */ 
/*  85 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  86 */       this.m_helper.addExchangeComponent(panel, queryStr, "queryStr");
/*     */ 
/*  88 */       this.m_list = createUdlPanel("", 100, 10, "parametersData", true, PARAM_COL_MAP, PARAM_COL_MAP[0][0], false);
/*     */ 
/*  91 */       this.m_list.add("North", addUpDownButtons("!csCompWizLabelParams", "paramName"));
/*  92 */       this.m_helper.addComponent(panel, this.m_list);
/*  93 */       this.m_list.add("East", addUdlPanelCommandButtons(false));
/*     */ 
/*  95 */       this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  96 */       this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  97 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  98 */       this.m_helper.addComponent(this, panel);
/*     */ 
/* 100 */       String param = this.m_helper.m_props.getProperty("parameters");
/* 101 */       buildParametersResultSet(param);
/* 102 */       refreshList(null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 106 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean onSelect()
/*     */   {
/*     */     try
/*     */     {
/* 115 */       if (this.m_selectList.getSelectedIndex() < 0)
/*     */       {
/* 117 */         throw new ServiceException("!csCompWizChooseItem");
/*     */       }
/*     */ 
/* 120 */       DataResultSet drset = SharedObjects.getTable("IdcQueries");
/*     */ 
/* 122 */       if (drset == null)
/*     */       {
/* 124 */         throw new ServiceException("!csCompWizQueryTableNotLoaded");
/*     */       }
/*     */ 
/* 127 */       String name = this.m_selectList.getSelectedObj();
/* 128 */       Vector v = drset.findRow(0, name);
/*     */ 
/* 130 */       if ((v == null) || (v.size() == 0))
/*     */       {
/* 132 */         throw new ServiceException(LocaleUtils.encodeMessage("csCompWizUnableToFindRS", null, name));
/*     */       }
/* 134 */       this.m_helper.m_props.put("name", name);
/*     */ 
/* 136 */       this.m_helper.m_props.put("queryStr", v.elementAt(1));
/* 137 */       this.m_helper.m_props.put("parameters", v.elementAt(2));
/*     */ 
/* 139 */       buildParametersResultSet((String)v.elementAt(2));
/* 140 */       this.m_helper.loadComponentValues();
/* 141 */       refreshList(null);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 145 */       CWizardGuiUtils.reportError(this.m_systemInterface, exp, (IdcMessage)null);
/* 146 */       return false;
/*     */     }
/* 148 */     return true;
/*     */   }
/*     */ 
/*     */   protected void initSelectListData(boolean isAll)
/*     */     throws DataException, ServiceException
/*     */   {
/* 154 */     String tablename = "IdcQueries";
/* 155 */     this.m_selectListData = new DataResultSet(new String[] { "name", "description" });
/* 156 */     DataResultSet drset = SharedObjects.getTable(tablename);
/*     */ 
/* 158 */     if (drset == null)
/*     */     {
/* 160 */       throw new ServiceException(LocaleUtils.encodeMessage("csTableNotLoaded", null, tablename));
/*     */     }
/*     */ 
/* 163 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 165 */       String name = drset.getStringValue(0);
/* 166 */       Vector v = this.m_selectListData.createEmptyRow();
/* 167 */       v.setElementAt(name, 0);
/* 168 */       v.setElementAt("", 1);
/* 169 */       this.m_selectListData.addRow(v);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void buildParametersResultSet(String params) throws ServiceException
/*     */   {
/* 175 */     this.m_listData = new DataResultSet(new String[] { "paramName", "paramType" });
/*     */ 
/* 177 */     if (params == null)
/*     */       return;
/* 179 */     Vector pv = StringUtils.parseArray(params.trim(), '\n', '^');
/* 180 */     for (int i = 0; i < pv.size(); ++i)
/*     */     {
/* 182 */       Vector v = this.m_listData.createEmptyRow();
/* 183 */       String temp = (String)pv.elementAt(i);
/* 184 */       temp = temp.trim();
/* 185 */       int index = temp.indexOf(32);
/* 186 */       if (index > 0)
/*     */       {
/* 188 */         v.setElementAt(temp.substring(0, index).trim(), 0);
/* 189 */         v.setElementAt(temp.substring(index + 1, temp.length()).trim(), 1);
/*     */       }
/* 191 */       this.m_listData.addRow(v);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void add()
/*     */   {
/* 199 */     addOrEdit(null, "!csCompWizLabelAddParam");
/*     */   }
/*     */ 
/*     */   protected void edit()
/*     */   {
/* 205 */     int index = getIndex(IdcMessageFactory.lc("csCompWizSelectParamToEdit", new Object[0]));
/*     */ 
/* 207 */     if (index < 0)
/*     */     {
/* 209 */       return;
/*     */     }
/*     */ 
/* 213 */     Properties props = this.m_list.getDataAt(index);
/* 214 */     String pname = props.getProperty("paramName");
/* 215 */     addOrEdit(props, LocaleUtils.encodeMessage("csCompWizEditParam", null, pname));
/*     */   }
/*     */ 
/*     */   protected void addOrEdit(Properties props, String title)
/*     */   {
/* 220 */     this.m_isNewListItem = true;
/*     */ 
/* 222 */     if (props != null)
/*     */     {
/* 224 */       this.m_isNewListItem = false;
/*     */     }
/*     */ 
/* 227 */     this.m_dlgHelper = new DialogHelper(this.m_systemInterface, LocaleResources.localizeMessage(title, null), true);
/* 228 */     this.m_dlgHelper.m_helpPage = DialogHelpTable.getHelpPage("CW_AddEditParameter");
/* 229 */     JPanel mainPanel = this.m_dlgHelper.m_mainPanel;
/* 230 */     this.m_dlgHelper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 232 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 239 */           return EditQueryPanel.this.onOk();
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 243 */           IdcMessage msg = IdcMessageFactory.lc("csCompWizAddParamError", new Object[0]);
/* 244 */           if (!EditQueryPanel.this.m_isNewListItem)
/*     */           {
/* 246 */             String paramName = this.m_dlgHelper.m_props.getProperty("paramName");
/* 247 */             msg = IdcMessageFactory.lc("csCompWizEditParamError", new Object[] { paramName });
/*     */           }
/* 249 */           CWizardGuiUtils.reportError(EditQueryPanel.this.m_systemInterface, exp, msg);
/* 250 */         }return false;
/*     */       }
/*     */     };
/* 254 */     this.m_dlgHelper.addOK(okCallback);
/* 255 */     this.m_dlgHelper.addCancel(null);
/* 256 */     this.m_dlgHelper.addHelp(null);
/*     */ 
/* 258 */     if (this.m_isNewListItem)
/*     */     {
/* 260 */       this.m_dlgHelper.addLabelEditPair(mainPanel, LocaleResources.getString("csCompWizLabelName2", null), 30, "paramName");
/*     */     }
/*     */     else
/*     */     {
/* 265 */       this.m_dlgHelper.m_props = props;
/*     */     }
/* 267 */     DisplayChoice typeChoice = new DisplayChoice();
/* 268 */     String[] OPTIONS = { "varchar", "boolean", "int", "date" };
/* 269 */     Vector options = new IdcVector();
/*     */ 
/* 271 */     for (int i = 0; i < OPTIONS.length; ++i)
/*     */     {
/* 273 */       options.addElement(OPTIONS[i]);
/*     */     }
/* 275 */     typeChoice.init(options);
/* 276 */     this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizLabelType2", null), typeChoice, "paramType");
/*     */ 
/* 280 */     if (this.m_dlgHelper.prompt() != 1)
/*     */       return;
/* 282 */     return;
/*     */   }
/*     */ 
/*     */   protected boolean onOk()
/*     */     throws ServiceException
/*     */   {
/* 288 */     String name = this.m_dlgHelper.m_props.getProperty("paramName");
/* 289 */     String type = this.m_dlgHelper.m_props.getProperty("paramType");
/*     */ 
/* 291 */     IdcMessage errMsg = checkField(name, IdcMessageFactory.lc("csCompWizLabelParamName", new Object[0]), false, false);
/*     */ 
/* 293 */     if (errMsg != null)
/*     */     {
/* 295 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, errMsg);
/* 296 */       return false;
/*     */     }
/*     */ 
/* 299 */     Vector v = null;
/* 300 */     if (this.m_isNewListItem)
/*     */     {
/* 302 */       v = new IdcVector();
/* 303 */       v.addElement(name);
/* 304 */       v.addElement(type);
/* 305 */       this.m_listData.addRow(v);
/*     */     }
/*     */     else
/*     */     {
/* 309 */       v = this.m_listData.findRow(0, name);
/* 310 */       if (v != null)
/*     */       {
/* 312 */         v.setElementAt(type, 1);
/*     */       }
/*     */     }
/*     */ 
/* 316 */     refreshList(name);
/* 317 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean validateEntries()
/*     */   {
/* 323 */     boolean validate = StringUtils.convertToBool(this.m_helper.m_props.getProperty("validateEntries"), true);
/*     */ 
/* 325 */     if ((!super.validateEntries()) || (!validate))
/*     */     {
/* 327 */       return false;
/*     */     }
/* 329 */     this.m_helper.m_props.put("parametersData", this.m_listData);
/*     */ 
/* 331 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 339 */     String name = exchange.m_compName;
/* 340 */     String val = exchange.m_compValue;
/*     */ 
/* 342 */     IdcMessage errMsg = null;
/*     */ 
/* 344 */     if (name.equals("name"))
/*     */     {
/* 346 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizLabelQueryName", new Object[0]), false, false);
/*     */     }
/* 348 */     else if (name.equals("queryStr"))
/*     */     {
/* 350 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizLabelQueryString", new Object[0]), true, true);
/*     */     }
/*     */     else
/*     */     {
/* 354 */       return super.validateComponentValue(exchange);
/*     */     }
/*     */ 
/* 357 */     if (errMsg != null)
/*     */     {
/* 359 */       exchange.m_errorMessage = errMsg;
/* 360 */       return false;
/*     */     }
/* 362 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 367 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.EditQueryPanel
 * JD-Core Version:    0.5.4
 */