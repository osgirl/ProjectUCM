/*     */ package intradoc.apps.docman;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.gui.iwt.IdcList;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.ShowColumnData;
/*     */ import intradoc.shared.gui.ShowColumnDlg;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.ListSelectionModel;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ import javax.swing.text.JTextComponent;
/*     */ 
/*     */ public class AddEditSubscriptionType
/*     */   implements ActionListener, ComponentBinder
/*     */ {
/*  74 */   public static final String[][] CAPTIONS = { { "scpType", "apSubscriptionTypeCaption" }, { "scpDescription", "apSubscriptionDescriptionCaption" }, { "scpEnabled", "apSubscriptionEnabledCaption" }, { "scpFields", "apSubscriptionFieldsCaption" } };
/*     */   protected Hashtable m_captionMap;
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected String m_helpPage;
/*     */   protected Component m_type;
/*     */   protected JTextComponent m_description;
/*     */   protected IdcList m_fieldList;
/*     */   protected JButton m_fieldBtn;
/*     */   protected JCheckBox m_enabledBox;
/*  95 */   protected String m_fields = "";
/*     */ 
/*     */   public AddEditSubscriptionType(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  99 */     this.m_helper = new DialogHelper(sys, title, true);
/* 100 */     this.m_systemInterface = sys;
/* 101 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init(DialogCallback okCallback, Properties props)
/*     */   {
/* 106 */     String type = props.getProperty("scpType");
/* 107 */     boolean isBasicSubscription = (type != null) && (type.equalsIgnoreCase("Basic"));
/* 108 */     boolean isFolderSubscription = (type != null) && (type.equalsIgnoreCase("Folder"));
/* 109 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*     */ 
/* 111 */     this.m_captionMap = new Hashtable();
/* 112 */     for (int i = 0; i < CAPTIONS.length; ++i)
/*     */     {
/* 114 */       this.m_captionMap.put(CAPTIONS[i][0], LocaleResources.getString(CAPTIONS[i][1], this.m_cxt));
/*     */     }
/*     */ 
/* 118 */     boolean isNew = StringUtils.convertToBool((String)props.get("IsNew"), false);
/* 119 */     this.m_helper.m_props = props;
/*     */ 
/* 121 */     if (isNew)
/*     */     {
/* 123 */       this.m_type = new CustomTextField(20);
/*     */     }
/*     */     else
/*     */     {
/* 127 */       this.m_type = new CustomLabel();
/*     */     }
/* 129 */     this.m_description = new CustomTextField(20);
/* 130 */     this.m_fieldList = new IdcList(new DefaultListModel());
/* 131 */     ListSelectionListener listener = new ListSelectionListener()
/*     */     {
/*     */       public void valueChanged(ListSelectionEvent e)
/*     */       {
/* 135 */         int index = AddEditSubscriptionType.this.m_fieldList.getSelectedIndex();
/* 136 */         if (index < 0)
/*     */           return;
/* 138 */         AddEditSubscriptionType.this.m_fieldList.getSelectionModel().removeSelectionInterval(index, index);
/*     */       }
/*     */     };
/* 142 */     this.m_fieldList.getSelectionModel().addListSelectionListener(listener);
/* 143 */     this.m_fieldBtn = new JButton(LocaleResources.getString("apSubscriptionLabelFields", this.m_cxt));
/*     */ 
/* 145 */     this.m_fieldBtn.addActionListener(this);
/* 146 */     this.m_fieldBtn.setEnabled((!isBasicSubscription) && (!isFolderSubscription));
/* 147 */     this.m_enabledBox = new JCheckBox(LocaleResources.getString("apSubscriptionLabelCheckboxEnabled", this.m_cxt));
/*     */ 
/* 150 */     JPanel panel = new PanePanel();
/* 151 */     this.m_helper.makePanelGridBag(panel, 2);
/* 152 */     GridBagConstraints gbc = this.m_helper.m_gridHelper.m_gc;
/* 153 */     gbc.insets = new Insets(2, 5, 2, 5);
/*     */ 
/* 155 */     this.m_helper.addLabelFieldPair(panel, getCaption("scpType"), this.m_type, "scpType");
/* 156 */     this.m_helper.addLabelFieldPair(panel, getCaption("scpDescription"), this.m_description, "scpDescription");
/*     */ 
/* 158 */     this.m_helper.addLabelFieldPair(panel, getCaption("scpEnabled"), this.m_enabledBox, "scpEnabled");
/*     */ 
/* 162 */     JPanel btnPanel = new PanePanel();
/* 163 */     this.m_helper.makePanelGridBag(btnPanel, 0);
/* 164 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 165 */     this.m_helper.addLastComponentInRow(btnPanel, this.m_fieldBtn);
/*     */ 
/* 168 */     JPanel subPanel = new CustomPanel();
/* 169 */     this.m_helper.makePanelGridBag(subPanel, 1);
/* 170 */     JScrollPane scrollPane = new JScrollPane(this.m_fieldList);
/*     */ 
/* 172 */     this.m_helper.addComponent(subPanel, new CustomLabel(getCaption("scpFields"), 1));
/*     */ 
/* 174 */     gbc.insets = new Insets(2, 5, 15, 5);
/* 175 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 176 */     gbc.weightx = (gbc.weighty = 1.0D);
/* 177 */     this.m_helper.addExchangeComponent(subPanel, scrollPane, "SCP_FIELDS_HOLDER");
/* 178 */     this.m_helper.m_props.put("SCP_FIELDS_HOLDER", "0");
/*     */ 
/* 180 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 181 */     gbc.weightx = (gbc.weighty = 0.0D);
/* 182 */     this.m_helper.addComponent(subPanel, btnPanel);
/*     */ 
/* 184 */     if (!isNew)
/*     */     {
/* 187 */       this.m_fields = ((String)props.get("scpFields"));
/* 188 */       this.m_helper.m_props.put("scpFields", this.m_fields);
/* 189 */       ViewFields fields = getDocFields();
/* 190 */       readFieldList(fields);
/*     */     }
/*     */ 
/* 193 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 196 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 197 */     this.m_helper.addComponent(mainPanel, panel);
/*     */ 
/* 199 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 200 */     gbc.weightx = 1.0D;
/* 201 */     gbc.weighty = 1.0D;
/* 202 */     gbc.fill = 1;
/* 203 */     this.m_helper.addComponent(mainPanel, subPanel);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 208 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public Properties getData()
/*     */   {
/* 213 */     return this.m_helper.m_props;
/*     */   }
/*     */ 
/*     */   public String getCaption(String field)
/*     */   {
/* 218 */     String caption = (String)this.m_captionMap.get(field);
/* 219 */     if (caption == null)
/*     */     {
/* 221 */       return field;
/*     */     }
/*     */ 
/* 224 */     return caption;
/*     */   }
/*     */ 
/*     */   protected void readFieldList(ViewFields docFields)
/*     */   {
/* 229 */     DefaultListModel model = (DefaultListModel)this.m_fieldList.getModel();
/* 230 */     model.removeAllElements();
/*     */ 
/* 232 */     Vector fieldDefs = docFields.m_viewFields;
/* 233 */     int numDFields = fieldDefs.size();
/*     */ 
/* 235 */     Vector fields = StringUtils.parseArray(this.m_fields, ',', ',');
/* 236 */     int num = fields.size();
/*     */ 
/* 238 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 240 */       String name = (String)fields.elementAt(i);
/* 241 */       for (int j = 0; j < numDFields; ++j)
/*     */       {
/* 243 */         ViewFieldDef fDef = (ViewFieldDef)fieldDefs.elementAt(j);
/* 244 */         if (!name.equals(fDef.m_name))
/*     */           continue;
/* 246 */         model.addElement(fDef.m_caption);
/* 247 */         break;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 259 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 260 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 265 */     String name = exchange.m_compName;
/* 266 */     String val = exchange.m_compValue;
/* 267 */     IdcMessage errMsg = null;
/*     */ 
/* 269 */     if (name.equals("scpDescription"))
/*     */     {
/* 273 */       return true;
/*     */     }
/*     */ 
/* 276 */     if (name.equals("SCP_FIELDS_HOLDER"))
/*     */     {
/* 278 */       if (this.m_fields.length() == 0)
/*     */       {
/* 280 */         errMsg = IdcMessageFactory.lc("apSubscriptionMustHaveFields", new Object[0]);
/*     */       }
/*     */     }
/* 283 */     else if ((val == null) || (val.length() == 0))
/*     */     {
/* 285 */       errMsg = IdcMessageFactory.lc("apSubscriptionFieldMissing_" + name, new Object[0]);
/*     */     }
/* 290 */     else if (name.equals("scpType"))
/*     */     {
/* 294 */       errMsg = Validation.checkUrlFileSegmentForDB(val, "apSubscriptionTypeErrorStub", 30, null);
/*     */     }
/*     */ 
/* 299 */     if (errMsg != null)
/*     */     {
/* 301 */       exchange.m_errorMessage = errMsg;
/* 302 */       return false;
/*     */     }
/* 304 */     return true;
/*     */   }
/*     */ 
/*     */   protected ViewFields getDocFields()
/*     */   {
/* 311 */     ViewFields fields = new ViewFields(this.m_cxt);
/* 312 */     fields.addStandardDocFields();
/*     */ 
/* 314 */     DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/*     */     try
/*     */     {
/* 317 */       fields.addMetaFields(drset);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 322 */       Report.trace(null, LocaleResources.getString("apUnableToLoadDocMetaDefinition", this.m_systemInterface.getExecutionContext()), e);
/*     */     }
/*     */ 
/* 325 */     fields.addField("fParentGUID", LocaleResources.getString("apSubscriptionFolderID", this.m_cxt));
/* 326 */     return fields;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/* 332 */     ViewFields fields = getDocFields();
/* 333 */     ShowColumnDlg dlg = new ShowColumnDlg(this.m_systemInterface, LocaleResources.getString("apSubscriptionFieldsTitle", this.m_systemInterface.getExecutionContext()), "AddSubscriptionFields");
/*     */ 
/* 336 */     ShowColumnData clmnData = new ShowColumnData();
/* 337 */     clmnData.m_columnStr = this.m_fields;
/* 338 */     clmnData.m_columnFields = fields;
/*     */ 
/* 340 */     dlg.init(clmnData);
/* 341 */     if (dlg.prompt() != 1)
/*     */       return;
/* 343 */     this.m_fields = clmnData.m_columnStr;
/* 344 */     this.m_helper.m_props.put("scpFields", this.m_fields);
/* 345 */     readFieldList(fields);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 351 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98371 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.AddEditSubscriptionType
 * JD-Core Version:    0.5.4
 */