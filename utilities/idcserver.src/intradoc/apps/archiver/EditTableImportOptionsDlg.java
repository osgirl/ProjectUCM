/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.ExportQueryData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditTableImportOptionsDlg extends DialogCallback
/*     */   implements ComponentBinder, ActionListener, ItemListener
/*     */ {
/*     */   protected SystemInterface m_sysInterface;
/*     */   protected CollectionContext m_context;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected DialogHelper m_helper;
/*     */   protected boolean m_isAdd;
/*  63 */   protected String m_currentTable = null;
/*  64 */   protected String m_tableListPrefix = "aImportTable";
/*  65 */   protected String[] m_valueKeys = null;
/*     */ 
/*  68 */   protected JCheckBox m_allowDeleteParentRows = null;
/*  69 */   protected JCheckBox m_removeWhenNoChild = null;
/*  70 */   protected JCheckBox m_syncChildCheckBox = null;
/*  71 */   protected JCheckBox m_allowDeleteRows = null;
/*  72 */   protected JCheckBox m_useParentTS = null;
/*     */ 
/*     */   public EditTableImportOptionsDlg(SystemInterface sys, String title, CollectionContext cxt, ExecutionContext execCxt)
/*     */   {
/*  77 */     this.m_sysInterface = sys;
/*  78 */     this.m_context = cxt;
/*  79 */     this.m_cxt = execCxt;
/*  80 */     this.m_helper = new DialogHelper(sys, title, true);
/*     */   }
/*     */ 
/*     */   public void init(String table, Properties props, boolean isAdd)
/*     */   {
/*  85 */     this.m_helper.m_props = props;
/*  86 */     this.m_currentTable = table;
/*  87 */     this.m_isAdd = isAdd;
/*     */ 
/*  89 */     JPanel main = this.m_helper.m_mainPanel;
/*  90 */     JButton button = new JButton(this.m_sysInterface.getString("apLabelOK"));
/*  91 */     this.m_helper.addCommandButtonEx(button, this);
/*  92 */     if (!isAdd)
/*     */     {
/*  94 */       button = new JButton(this.m_sysInterface.getString("apLabelRemove"));
/*  95 */       button.setActionCommand("Remove");
/*  96 */       this.m_helper.addCommandButtonEx(button, this);
/*     */     }
/*  98 */     this.m_helper.addCancel(null);
/*  99 */     this.m_helper.m_componentBinder = this;
/*     */ 
/* 101 */     JPanel tablePanel = initDescriptionPanel();
/* 102 */     JPanel optionPanel = initOptionUI();
/*     */ 
/* 104 */     main.setLayout(new BorderLayout());
/* 105 */     main.add("North", tablePanel);
/* 106 */     main.add("South", optionPanel);
/*     */ 
/* 109 */     String[][] keys = ImportTableList.KEYS;
/* 110 */     this.m_valueKeys = new String[keys.length];
/* 111 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 113 */       this.m_valueKeys[i] = keys[i][0];
/*     */     }
/*     */ 
/* 116 */     parseOptions();
/* 117 */     enableDisableOptions();
/*     */   }
/*     */ 
/*     */   protected void parseOptions()
/*     */   {
/* 122 */     Properties props = this.m_helper.m_props;
/* 123 */     String options = props.getProperty(this.m_tableListPrefix + this.m_currentTable);
/*     */ 
/* 125 */     if ((options == null) || (options.length() == 0))
/*     */     {
/* 127 */       return;
/*     */     }
/* 129 */     ExportQueryData queryData = new ExportQueryData();
/* 130 */     queryData.parse(options);
/* 131 */     for (int i = 0; i < this.m_valueKeys.length; ++i)
/*     */     {
/* 133 */       String value = queryData.getQueryProp(this.m_valueKeys[i]);
/* 134 */       if (value == null)
/*     */         continue;
/* 136 */       props.put(this.m_valueKeys[i], value);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void enableDisableOptions()
/*     */   {
/* 143 */     String deleteStr = this.m_helper.m_props.getProperty("aIsReplicateDeletedRows");
/* 144 */     boolean allowDelete = StringUtils.convertToBool(deleteStr, false);
/* 145 */     this.m_syncChildCheckBox.setEnabled(allowDelete);
/* 146 */     this.m_removeWhenNoChild.setEnabled(allowDelete);
/* 147 */     this.m_allowDeleteParentRows.setEnabled(allowDelete);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 152 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public JPanel initDescriptionPanel()
/*     */   {
/* 157 */     JPanel panel = new PanePanel();
/* 158 */     this.m_helper.makePanelGridBag(panel, 1);
/* 159 */     StringBuffer msgBuf = new StringBuffer();
/* 160 */     if ((this.m_currentTable == null) || (this.m_currentTable.length() == 0))
/*     */     {
/* 162 */       msgBuf.setLength(0);
/* 163 */       msgBuf.append(LocaleResources.getString("apArchiverImportOptionsMsg", this.m_cxt, this.m_helper.m_props.getProperty("aArchiveName")));
/*     */     }
/*     */     else
/*     */     {
/* 168 */       msgBuf.append(LocaleResources.getString("apArchiverTableImportOptionsMsg", this.m_cxt, this.m_currentTable));
/*     */     }
/* 170 */     CustomText text = new CustomText(msgBuf)
/*     */     {
/*     */       public Dimension getPreferredSize()
/*     */       {
/* 175 */         Dimension d = super.getPreferredSize();
/*     */ 
/* 177 */         d.width = (this.m_maxWidth + this.m_marginWidth);
/* 178 */         d.height = ((this.val$msgBuf.length() / 85 + 1) * this.m_lineHeight);
/*     */ 
/* 180 */         return d;
/*     */       }
/*     */     };
/* 183 */     text.setMaxColumns(85);
/* 184 */     text.setText(msgBuf.toString());
/* 185 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 186 */     this.m_helper.addComponent(panel, text);
/* 187 */     return panel;
/*     */   }
/*     */ 
/*     */   public JPanel initOptionUI()
/*     */   {
/* 192 */     JPanel panel = new CustomPanel();
/* 193 */     this.m_helper.makePanelGridBag(panel, 2);
/* 194 */     this.m_helper.addExchangeComponent(panel, getCheckbox("apArchiverLabelCreateField"), "aIsCreateNewField");
/* 195 */     this.m_helper.addLabelDisplayPairEx(panel, " ", 20, "", false);
/* 196 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 197 */     JCheckBox sIDCheckBox = getCheckbox("apArchiverLabelUseSourceID", "aUseSourceID");
/* 198 */     this.m_helper.addExchangeComponent(panel, sIDCheckBox, "aUseSourceID");
/*     */ 
/* 200 */     this.m_allowDeleteRows = getCheckbox("apArchiverLabelRpcdDeleted", "aIsReplicateDeletedRows");
/* 201 */     this.m_allowDeleteRows.addItemListener(this);
/* 202 */     this.m_helper.addExchangeComponent(panel, this.m_allowDeleteRows, "aIsReplicateDeletedRows");
/*     */ 
/* 205 */     this.m_syncChildCheckBox = getCheckbox("apArchiverLabelRemoveChildrenOnImport", "aRemoveExistingChildren");
/* 206 */     this.m_helper.addExchangeComponent(panel, this.m_syncChildCheckBox, "aRemoveExistingChildren");
/*     */ 
/* 208 */     this.m_allowDeleteParentRows = getCheckbox("apArchiverLabelAllowDeleteParentRows", "aAllowDeleteParentRows");
/*     */ 
/* 210 */     this.m_helper.addExchangeComponent(panel, this.m_allowDeleteParentRows, "aAllowDeleteParentRows");
/* 211 */     this.m_removeWhenNoChild = getCheckbox("apArchiverLabelRemoveWhenNoChild", "aDeleteParentOnlyWhenNoChild");
/*     */ 
/* 213 */     this.m_helper.addExchangeComponent(panel, this.m_removeWhenNoChild, "aDeleteParentOnlyWhenNoChild");
/*     */ 
/* 215 */     return panel;
/*     */   }
/*     */ 
/*     */   protected JCheckBox getCheckbox(String label)
/*     */   {
/* 220 */     return getCheckbox(label, null);
/*     */   }
/*     */ 
/*     */   protected JCheckBox getCheckbox(String label, String name)
/*     */   {
/* 225 */     label = LocaleResources.getString(label, this.m_cxt);
/* 226 */     JCheckBox box = new CustomCheckbox(label);
/* 227 */     if ((name != null) && (name.length() > 0))
/*     */     {
/* 229 */       box.setName(name);
/*     */     }
/* 231 */     return box;
/*     */   }
/*     */ 
/*     */   public String createQueryString()
/*     */   {
/* 236 */     String query = this.m_helper.m_props.getProperty(this.m_tableListPrefix + this.m_currentTable);
/* 237 */     ExportQueryData queryData = new ExportQueryData();
/* 238 */     queryData.parse(query);
/* 239 */     queryData.addExportQueryOptions(this.m_helper.m_props, this.m_valueKeys);
/* 240 */     return queryData.formatString();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent actionEvent)
/*     */   {
/* 245 */     String command = actionEvent.getActionCommand();
/*     */ 
/* 248 */     if (command.equals("Remove"))
/*     */     {
/* 250 */       this.m_helper.m_props.put("isDelete", "true");
/* 251 */       IdcMessage msg = IdcMessageFactory.lc("apArchiveTableRemoveImportOptions", new Object[0]);
/* 252 */       int returnCode = MessageBox.doMessage(this.m_sysInterface, msg, 2);
/*     */ 
/* 254 */       if (returnCode == 1)
/*     */       {
/* 256 */         this.m_helper.m_result = 1;
/* 257 */         this.m_helper.close();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 262 */       this.m_helper.m_result = 1;
/* 263 */       if (!this.m_helper.retrieveComponentValues())
/*     */         return;
/* 265 */       this.m_helper.close();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 272 */     if (!this.m_helper.retrieveComponentValues())
/*     */       return;
/* 274 */     enableDisableOptions();
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent e)
/*     */   {
/* 281 */     return true;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 291 */     String name = exchange.m_compName;
/* 292 */     if (updateComponent)
/*     */     {
/* 294 */       String value = this.m_helper.m_props.getProperty(name);
/* 295 */       if (value != null)
/*     */       {
/* 297 */         exchange.m_compValue = value;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 302 */       this.m_helper.m_props.put(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 311 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 316 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditTableImportOptionsDlg
 * JD-Core Version:    0.5.4
 */