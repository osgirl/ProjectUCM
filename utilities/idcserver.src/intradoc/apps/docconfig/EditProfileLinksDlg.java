/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocProfileScriptUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditProfileLinksDlg
/*     */   implements ActionListener, ItemListener
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_helpPage;
/*  59 */   protected ExecutionContext m_context = null;
/*  60 */   protected SharedContext m_shContext = null;
/*     */   protected DataBinder m_profileData;
/*     */   protected DataBinder m_workingData;
/*  66 */   protected Hashtable m_controlledMap = null;
/*  67 */   protected final String[][] UI_INFO_MAP = { { "checkinLink", "editCheckin", "apDpHasCheckinLinkScriptLabel", "dpHasCheckinLinkScript", "dpCheckinLinkScriptSummary", "DpEditCheckinLinkScript", "apDpEditCheckinLinkScriptTitle" }, { "searchLink", "editSearch", "apDpHasSearchLinkScriptLabel", "dpHasSearchLinkScript", "dpSearchLinkScriptSummary", "DpEditSearchLinkScript", "apDpEditSearchLinkScriptTitle" } };
/*     */ 
/*     */   public EditProfileLinksDlg(SystemInterface sys, String title, SharedContext shContext, String helpPage)
/*     */   {
/*  80 */     this.m_helper = new DialogHelper(sys, title, true);
/*  81 */     this.m_context = sys.getExecutionContext();
/*  82 */     this.m_shContext = shContext;
/*  83 */     this.m_systemInterface = sys;
/*     */ 
/*  85 */     this.m_helpPage = helpPage;
/*  86 */     this.m_controlledMap = new Hashtable();
/*     */   }
/*     */ 
/*     */   public int init(DataBinder profileData)
/*     */   {
/*  91 */     this.m_profileData = profileData;
/*  92 */     this.m_workingData = new DataBinder();
/*     */ 
/*  94 */     Properties props = (Properties)this.m_profileData.getLocalData().clone();
/*  95 */     this.m_workingData.setLocalData(props);
/*  96 */     this.m_helper.m_props = props;
/*     */ 
/*  98 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 103 */         EditProfileLinksDlg.this.m_profileData.merge(EditProfileLinksDlg.this.m_workingData);
/* 104 */         return true;
/*     */       }
/*     */     };
/* 107 */     okCallback.m_dlgHelper = this.m_helper;
/* 108 */     JPanel mainPanel = this.m_helper.initStandard(null, okCallback, 2, true, this.m_helpPage);
/*     */ 
/* 111 */     initUI(mainPanel);
/* 112 */     loadComponents();
/* 113 */     enableDisable();
/*     */ 
/* 115 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI(JPanel mainPanel)
/*     */   {
/* 120 */     JPanel pnl = createPanel();
/*     */ 
/* 122 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 123 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 124 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 125 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 126 */     this.m_helper.addComponent(mainPanel, pnl);
/*     */   }
/*     */ 
/*     */   protected JPanel createPanel()
/*     */   {
/* 131 */     JPanel panel = new PanePanel();
/* 132 */     this.m_helper.makePanelGridBag(panel, 1);
/*     */ 
/* 135 */     for (int i = 0; i < this.UI_INFO_MAP.length; ++i)
/*     */     {
/* 137 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 138 */       this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 139 */       CustomCheckbox box = new CustomCheckbox(this.m_systemInterface.getString(this.UI_INFO_MAP[i][2]));
/* 140 */       this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 141 */       this.m_helper.addExchangeComponent(panel, box, this.UI_INFO_MAP[i][3]);
/* 142 */       box.addItemListener(this);
/*     */ 
/* 145 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 146 */       this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 147 */       this.m_helper.addComponent(panel, new CustomLabel());
/*     */ 
/* 150 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 25, 2, 5);
/* 151 */       this.m_helper.m_gridHelper.prepareAddRowElement();
/* 152 */       this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 153 */       this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 154 */       this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 155 */       CustomTextArea area = new CustomTextArea(5, 40);
/* 156 */       this.m_helper.addExchangeComponent(panel, area, this.UI_INFO_MAP[i][4]);
/* 157 */       area.setEditable(false);
/*     */ 
/* 159 */       this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 0, 2, 0);
/* 160 */       JButton btn = new JButton(this.m_systemInterface.getString("apDlgButtonEdit"));
/* 161 */       btn.setActionCommand(this.UI_INFO_MAP[i][1]);
/* 162 */       btn.addActionListener(this);
/* 163 */       this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 164 */       this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 165 */       this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 166 */       this.m_helper.addLastComponentInRow(panel, btn);
/*     */ 
/* 169 */       Component[] cntrls = new Component[2];
/* 170 */       cntrls[0] = box;
/* 171 */       cntrls[1] = btn;
/*     */ 
/* 173 */       this.m_controlledMap.put(this.UI_INFO_MAP[i][3], cntrls);
/*     */     }
/* 175 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void loadComponents()
/*     */   {
/* 181 */     String[] tables = { "CheckinLinkClauses", "SearchLinkClauses" };
/* 182 */     for (int i = 0; i < tables.length; ++i)
/*     */     {
/* 184 */       ResultSet rset = this.m_profileData.getResultSet(tables[i]);
/* 185 */       if (rset == null)
/*     */         continue;
/* 187 */       this.m_workingData.addResultSet(tables[i], rset);
/*     */     }
/*     */ 
/* 190 */     createSummaries();
/* 191 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 196 */     for (Enumeration en = this.m_controlledMap.keys(); en.hasMoreElements(); )
/*     */     {
/* 198 */       String key = (String)en.nextElement();
/* 199 */       boolean isEnabled = StringUtils.convertToBool(this.m_helper.m_props.getProperty(key), false);
/*     */ 
/* 201 */       Component[] objs = (Component[])(Component[])this.m_controlledMap.get(key);
/* 202 */       objs[1].setEnabled(isEnabled);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void createSummaries()
/*     */   {
/* 208 */     String[][] types = { { "checkinLink", "dpCheckinLinkScriptSummary" }, { "searchLink", "dpSearchLinkScriptSummary" } };
/*     */ 
/* 214 */     for (int i = 0; i < types.length; ++i)
/*     */     {
/* 216 */       String summary = DocProfileScriptUtils.computeScriptString("", this.m_workingData, types[i][0], true);
/* 217 */       this.m_helper.m_props.put(types[i][1], summary);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 226 */     Object src = e.getSource();
/* 227 */     if (!src instanceof JCheckBox) {
/*     */       return;
/*     */     }
/* 230 */     for (Enumeration en = this.m_controlledMap.elements(); en.hasMoreElements(); )
/*     */     {
/* 232 */       Component[] objs = (Component[])(Component[])en.nextElement();
/* 233 */       if (objs[0] == src)
/*     */       {
/* 235 */         JCheckBox box = (JCheckBox)objs[0];
/* 236 */         boolean state = box.isSelected();
/* 237 */         objs[1].setEnabled(state);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 248 */     String cmd = e.getActionCommand();
/*     */ 
/* 251 */     String[] info = null;
/* 252 */     int len = this.UI_INFO_MAP.length;
/* 253 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 255 */       if (!cmd.equals(this.UI_INFO_MAP[i][1]))
/*     */         continue;
/* 257 */       info = this.UI_INFO_MAP[i];
/* 258 */       break;
/*     */     }
/*     */ 
/* 262 */     String type = info[0];
/* 263 */     Properties configProps = DocProfileScriptUtils.loadConfiguration(type);
/* 264 */     DataBinder scriptData = loadEditConfiguration(configProps);
/*     */ 
/* 266 */     String helpPage = DialogHelpTable.getHelpPage(info[5]);
/* 267 */     EditProfileScriptDlg dlg = new EditProfileScriptDlg(this.m_systemInterface, this.m_shContext, this.m_systemInterface.getString(info[6]), helpPage);
/*     */ 
/* 270 */     int result = dlg.init(scriptData, configProps, null);
/* 271 */     if (result == 1)
/*     */     {
/* 273 */       this.m_workingData.merge(scriptData);
/*     */     }
/*     */ 
/* 276 */     String summary = DocProfileScriptUtils.computeScriptString("", this.m_workingData, info[0], true);
/* 277 */     this.m_helper.m_exchange.setComponentValue(info[4], summary);
/*     */   }
/*     */ 
/*     */   protected DataBinder loadEditConfiguration(Properties configProps)
/*     */   {
/* 282 */     Properties props = (Properties)this.m_helper.m_props.clone();
/* 283 */     String tableName = configProps.getProperty("TableName");
/*     */ 
/* 285 */     ResultSet rset = this.m_workingData.getResultSet(tableName);
/* 286 */     if (rset == null)
/*     */     {
/* 288 */       rset = this.m_profileData.getResultSet(tableName);
/*     */     }
/*     */ 
/* 291 */     if (rset == null)
/*     */     {
/* 293 */       rset = new DataResultSet(DocProfileScriptUtils.DP_LINK_COLUMNS);
/*     */     }
/*     */     else
/*     */     {
/* 297 */       DataResultSet copySet = new DataResultSet();
/* 298 */       copySet.copy(rset);
/* 299 */       rset = copySet;
/*     */     }
/*     */ 
/* 303 */     DataBinder scriptData = new DataBinder();
/* 304 */     scriptData.setLocalData(props);
/* 305 */     scriptData.addResultSet(tableName, rset);
/*     */ 
/* 307 */     return scriptData;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 312 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditProfileLinksDlg
 * JD-Core Version:    0.5.4
 */