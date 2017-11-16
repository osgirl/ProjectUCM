/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.ProfileUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Container;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ 
/*     */ public class ShowColumnDlg
/*     */ {
/*  60 */   protected SystemInterface m_systemInterface = null;
/*  61 */   protected ExecutionContext m_cxt = null;
/*  62 */   protected DialogHelper m_helper = null;
/*  63 */   protected String m_helpPage = null;
/*     */ 
/*  65 */   protected ShowColumnData m_columnData = null;
/*     */ 
/*  67 */   protected Hashtable m_columnInfoMap = null;
/*  68 */   protected Vector m_columnInfos = null;
/*     */ 
/*  70 */   protected boolean m_hasPreferences = false;
/*  71 */   protected SharedContext m_context = null;
/*  72 */   protected boolean m_showMemoFields = true;
/*     */ 
/*     */   public ShowColumnDlg(SystemInterface sys, String title)
/*     */   {
/*  76 */     this.m_helper = new DialogHelper(sys, title, true, true);
/*  77 */     this.m_systemInterface = sys;
/*  78 */     this.m_cxt = sys.getExecutionContext();
/*  79 */     this.m_helpPage = "Column";
/*     */ 
/*  81 */     this.m_columnInfos = new IdcVector();
/*  82 */     this.m_columnInfoMap = new Hashtable();
/*     */   }
/*     */ 
/*     */   public ShowColumnDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  87 */     this.m_helper = new DialogHelper(sys, title, true, true);
/*  88 */     this.m_systemInterface = sys;
/*  89 */     this.m_helpPage = helpPage;
/*     */ 
/*  91 */     this.m_columnInfos = new IdcVector();
/*  92 */     this.m_columnInfoMap = new Hashtable();
/*     */   }
/*     */ 
/*     */   public void init(ShowColumnData clmnData)
/*     */   {
/*  97 */     initEx(clmnData, false, null);
/*     */   }
/*     */ 
/*     */   public void initEx(ShowColumnData clmnData, boolean hasPrefs, SharedContext ctxt)
/*     */   {
/* 102 */     this.m_columnData = clmnData;
/* 103 */     this.m_hasPreferences = hasPrefs;
/* 104 */     this.m_context = ctxt;
/*     */ 
/* 106 */     Properties props = this.m_helper.m_props;
/* 107 */     this.m_helper.m_scrollPane.setPreferredSize(new Dimension(250, 500));
/*     */ 
/* 109 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 114 */         ShowColumnDlg.this.buildShowColumns();
/* 115 */         if (ShowColumnDlg.this.m_columnData.m_columns.size() == 0)
/*     */         {
/* 117 */           this.m_errorMessage = IdcMessageFactory.lc("apSelectAtLeastOneColumn", new Object[0]);
/* 118 */           return false;
/*     */         }
/* 120 */         ShowColumnDlg.this.saveColumnChoices();
/* 121 */         return true;
/*     */       }
/*     */     };
/* 125 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage(this.m_helpPage));
/*     */ 
/* 128 */     CustomPanel metaPanel = new CustomPanel();
/* 129 */     this.m_helper.makePanelGridBag(metaPanel, 2);
/* 130 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/* 131 */     this.m_helper.addComponent(mainPanel, metaPanel);
/*     */ 
/* 133 */     Vector docFieldInfos = this.m_columnData.m_columnFields.m_viewFields;
/*     */ 
/* 135 */     boolean isFirstMeta = true;
/* 136 */     int size = docFieldInfos.size();
/* 137 */     int numPersistent = this.m_columnData.m_persistentColumns.size();
/* 138 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 140 */       ViewFieldDef fdef = (ViewFieldDef)docFieldInfos.elementAt(i);
/*     */ 
/* 142 */       if ((!this.m_showMemoFields) && (fdef.m_type.equalsIgnoreCase("memo")))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 148 */       Vector persistentColumns = this.m_columnData.m_persistentColumns;
/* 149 */       if (persistentColumns != null)
/*     */       {
/* 151 */         boolean isPersistent = false;
/* 152 */         int numP = persistentColumns.size();
/* 153 */         for (int j = 0; j < numP; ++j)
/*     */         {
/* 155 */           ShowColumnInfo info = (ShowColumnInfo)persistentColumns.elementAt(j);
/* 156 */           if (!fdef.m_name.equals(info.m_name))
/*     */             continue;
/* 158 */           isPersistent = true;
/* 159 */           this.m_columnInfoMap.put(info.m_name, info);
/* 160 */           break;
/*     */         }
/*     */ 
/* 163 */         if (isPersistent) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 169 */       if ((fdef.m_isCustomMeta) && (isFirstMeta))
/*     */       {
/* 172 */         isFirstMeta = false;
/* 173 */         this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/* 174 */         metaPanel = new CustomPanel();
/* 175 */         this.m_helper.makePanelGridBag(metaPanel, 2);
/* 176 */         this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/* 177 */         this.m_helper.addComponent(mainPanel, metaPanel);
/*     */       }
/*     */ 
/* 180 */       JCheckBox checkbox = new JCheckBox(fdef.m_caption);
/* 181 */       this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/* 182 */       this.m_helper.addExchangeComponent(metaPanel, checkbox, fdef.m_name);
/* 183 */       props.put(fdef.m_caption, "0");
/*     */ 
/* 186 */       ShowColumnInfo info = new ShowColumnInfo();
/* 187 */       info.m_name = fdef.m_name;
/* 188 */       info.m_label = fdef.m_caption;
/* 189 */       info.m_order = (i + numPersistent);
/*     */ 
/* 191 */       this.m_columnInfos.addElement(info);
/* 192 */       this.m_columnInfoMap.put(info.m_name, info);
/*     */     }
/*     */ 
/* 195 */     Vector showColumns = StringUtils.parseArray(this.m_columnData.m_columnStr, ',', ',');
/* 196 */     size = showColumns.size();
/* 197 */     for (int i = 0; i < showColumns.size(); ++i)
/*     */     {
/* 199 */       props.put(showColumns.elementAt(i), "1");
/*     */     }
/*     */ 
/* 202 */     if (!this.m_hasPreferences) {
/*     */       return;
/*     */     }
/* 205 */     initUserPreferencesUI(mainPanel);
/*     */   }
/*     */ 
/*     */   protected void initUserPreferencesUI(JPanel mainPanel)
/*     */   {
/* 211 */     JCheckBox saveBox = new CustomCheckbox(LocaleResources.getString("apSaveSettings", this.m_cxt), 1);
/*     */ 
/* 215 */     Container newPane = new PanePanel();
/* 216 */     newPane.setLayout(new BorderLayout());
/*     */ 
/* 218 */     PanePanel helpPane = new PanePanel();
/* 219 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 220 */     this.m_helper.addExchangeComponent(helpPane, saveBox, "isSaveSettings");
/*     */ 
/* 222 */     newPane.add("South", helpPane);
/* 223 */     newPane.add("Center", this.m_helper.m_scrollPane);
/* 224 */     this.m_helper.m_dialog.add("Center", newPane);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 230 */     if (this.m_columnInfos.size() == 0)
/*     */     {
/* 232 */       String msg = this.m_systemInterface.getString("apNoColumnsToShow");
/* 233 */       MessageBox.doMessage(this.m_systemInterface, msg, 1);
/* 234 */       return 0;
/*     */     }
/* 236 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void setShowMemoFields(boolean showMemoFields)
/*     */   {
/* 241 */     this.m_showMemoFields = showMemoFields;
/*     */   }
/*     */ 
/*     */   public void buildShowColumns()
/*     */   {
/* 246 */     Vector columnInfos = new IdcVector();
/* 247 */     Vector persistentColumns = this.m_columnData.m_persistentColumns;
/*     */ 
/* 249 */     Properties props = this.m_helper.m_props;
/*     */ 
/* 251 */     int numColumns = this.m_columnInfos.size();
/* 252 */     int numPersistent = 0;
/* 253 */     if (persistentColumns != null)
/*     */     {
/* 255 */       numPersistent = persistentColumns.size();
/*     */     }
/* 257 */     int num = numColumns + numPersistent;
/*     */ 
/* 259 */     Vector orgColumns = this.m_columnData.m_columns;
/* 260 */     if (orgColumns == null)
/*     */     {
/* 262 */       orgColumns = new IdcVector();
/* 263 */       this.m_columnData.m_columns = orgColumns;
/*     */     }
/*     */ 
/* 266 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 268 */       ShowColumnInfo clmnInfo = null;
/* 269 */       boolean isSelected = true;
/* 270 */       if (i < numPersistent)
/*     */       {
/* 272 */         clmnInfo = (ShowColumnInfo)persistentColumns.elementAt(i);
/*     */       }
/*     */       else
/*     */       {
/* 277 */         clmnInfo = (ShowColumnInfo)this.m_columnInfos.elementAt(i - numPersistent);
/* 278 */         String value = props.getProperty(clmnInfo.m_name);
/* 279 */         isSelected = StringUtils.convertToBool(value, false);
/*     */       }
/*     */ 
/* 282 */       if (isSelected)
/*     */       {
/* 284 */         columnInfos.addElement(clmnInfo);
/*     */       }
/*     */       else
/*     */       {
/* 289 */         String name = clmnInfo.m_name;
/* 290 */         int index = -1;
/* 291 */         int numOrg = orgColumns.size();
/* 292 */         for (int j = 0; j < numOrg; ++j)
/*     */         {
/* 294 */           ShowColumnInfo info = (ShowColumnInfo)orgColumns.elementAt(j);
/* 295 */           if (!name.equals(info.m_name))
/*     */             continue;
/* 297 */           index = j;
/* 298 */           break;
/*     */         }
/*     */ 
/* 301 */         if (index < 0)
/*     */           continue;
/* 303 */         orgColumns.removeElementAt(index);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 309 */     int numInfos = columnInfos.size();
/* 310 */     for (int i = 0; i < numInfos; ++i)
/*     */     {
/* 313 */       ShowColumnInfo info = (ShowColumnInfo)columnInfos.elementAt(i);
/* 314 */       String name = info.m_name;
/* 315 */       int index = 0;
/* 316 */       int numOrg = orgColumns.size();
/* 317 */       for (int j = numOrg - 1; j >= 0; --j)
/*     */       {
/* 319 */         ShowColumnInfo orgInfo = (ShowColumnInfo)orgColumns.elementAt(j);
/* 320 */         String orgName = orgInfo.m_name;
/*     */ 
/* 324 */         ShowColumnInfo stdInfo = (ShowColumnInfo)this.m_columnInfoMap.get(orgName);
/* 325 */         if (orgName.equals(name))
/*     */         {
/* 327 */           index = -1;
/*     */ 
/* 330 */           orgInfo.m_label = stdInfo.m_label;
/* 331 */           break;
/*     */         }
/* 333 */         if (stdInfo == null)
/*     */         {
/* 337 */           index = -1;
/* 338 */           orgColumns.removeElementAt(j);
/* 339 */           break;
/*     */         }
/*     */ 
/* 343 */         if ((index != 0) || (stdInfo.m_order >= info.m_order))
/*     */           continue;
/* 345 */         index = j + 1;
/*     */       }
/*     */ 
/* 349 */       if (index < 0)
/*     */         continue;
/* 351 */       if (index > numOrg)
/*     */       {
/* 353 */         orgColumns.addElement(info);
/*     */       }
/*     */       else
/*     */       {
/* 357 */         orgColumns.insertElementAt(info, index);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 363 */     Vector columns = new IdcVector(numInfos);
/* 364 */     Vector columnLabels = new IdcVector(numInfos);
/* 365 */     for (int i = 0; i < orgColumns.size(); ++i)
/*     */     {
/* 367 */       ShowColumnInfo info = (ShowColumnInfo)orgColumns.elementAt(i);
/* 368 */       columns.addElement(info.m_name);
/* 369 */       columnLabels.addElement(info.m_label);
/*     */     }
/*     */ 
/* 372 */     this.m_columnData.m_columnStr = StringUtils.createString(columns, ',', ',');
/* 373 */     this.m_columnData.m_columnLabels = columnLabels;
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e, String msg)
/*     */   {
/* 378 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   protected void saveColumnChoices()
/*     */   {
/* 383 */     boolean isSave = StringUtils.convertToBool(this.m_helper.m_props.getProperty("isSaveSettings"), false);
/* 384 */     if (!isSave)
/*     */       return;
/* 386 */     DataBinder data = new DataBinder();
/* 387 */     String key = this.m_columnData.m_viewName + ":columns";
/* 388 */     data.putLocal(key, this.m_columnData.m_columnStr);
/*     */ 
/* 391 */     data.putLocal("isTopicSuppressError", "false");
/*     */ 
/* 393 */     ProfileUtils.addTopicEdit("appcommongui", "updateKeys", key, data, data);
/*     */     try
/*     */     {
/* 396 */       this.m_context.executeService("PING_SERVER", data, false);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 400 */       reportError(e, "!apUnableToSaveSettings");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 407 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78495 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.ShowColumnDlg
 * JD-Core Version:    0.5.4
 */