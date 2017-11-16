/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.ItemSelectable;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddRelationDialog extends DialogCallback
/*     */   implements ActionListener, ItemListener, ComponentBinder
/*     */ {
/*  60 */   protected SystemInterface m_systemInterface = null;
/*  61 */   protected ExecutionContext m_context = null;
/*  62 */   protected DialogHelper m_helper = null;
/*     */   protected boolean m_isAdd;
/*  64 */   protected String m_helpPage = null;
/*     */   protected DataResultSet m_tablesConfig;
/*  66 */   protected Hashtable m_choiceObjects = new Hashtable();
/*     */ 
/*  68 */   protected String m_action = null;
/*  69 */   protected DataBinder m_binder = null;
/*     */ 
/*     */   public AddRelationDialog(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  73 */     this.m_systemInterface = sys;
/*  74 */     this.m_context = sys.getExecutionContext();
/*  75 */     this.m_helper = new DialogHelper(sys, title, true);
/*  76 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(Properties props, boolean isAdd)
/*     */   {
/*  81 */     this.m_helper.m_props = props;
/*  82 */     this.m_isAdd = isAdd;
/*  83 */     this.m_binder = new DataBinder();
/*     */ 
/*  85 */     if (this.m_isAdd)
/*     */     {
/*  87 */       this.m_action = "ADD_SCHEMA_RELATION";
/*     */     }
/*     */     else
/*     */     {
/*  91 */       this.m_action = "EDIT_SCHEMA_RELATION";
/*     */     }
/*     */ 
/*  95 */     this.m_dlgHelper = this.m_helper;
/*     */     try
/*     */     {
/*  99 */       loadTableConfiguration();
/* 100 */       initUI(this, this.m_binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 104 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */ 
/* 107 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void loadTableConfiguration() throws ServiceException
/*     */   {
/* 112 */     DataBinder binder = new DataBinder();
/* 113 */     AppLauncher.executeService("GET_SCHEMA_TABLES", binder);
/* 114 */     this.m_tablesConfig = SharedObjects.getTable("SchemaTableConfig");
/*     */   }
/*     */ 
/*     */   protected void initUI(DialogCallback okCallback, DataBinder binder)
/*     */   {
/* 121 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 124 */     this.m_binder.setLocalData(this.m_helper.m_props);
/* 125 */     JPanel pnl = initPanels(binder);
/*     */ 
/* 127 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 128 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 129 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 130 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 131 */     this.m_helper.addComponent(mainPanel, pnl);
/*     */   }
/*     */ 
/*     */   protected JPanel initPanels(DataBinder binder)
/*     */   {
/* 136 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 137 */     mainPanel.setLayout(new BorderLayout());
/* 138 */     JPanel panel = new PanePanel();
/*     */ 
/* 140 */     int length = 30;
/* 141 */     this.m_helper.makePanelGridBag(panel, 2);
/* 142 */     if (this.m_isAdd)
/*     */     {
/* 144 */       this.m_helper.addLabelEditPair(panel, this.m_systemInterface.localizeMessage("!apSchemaRelationName"), length, "schRelationName");
/*     */     }
/*     */     else
/*     */     {
/* 150 */       this.m_helper.addLabelDisplayPair(panel, this.m_systemInterface.localizeMessage("!apSchemaRelationName"), length, "schRelationName");
/*     */     }
/*     */ 
/* 154 */     this.m_helper.addLabelEditPair(panel, this.m_systemInterface.localizeMessage("!apSchemaRelationDescription"), length, "schRelationDescription");
/*     */ 
/* 158 */     Object[] objects = createTableAndColumnSelectionControls("schTable1Table", "schTable1Column");
/* 159 */     this.m_helper.addLabelFieldPair(panel, this.m_systemInterface.localizeMessage("!apSchemaRelationParentInfo"), (Component)objects[0], "schTable1Table");
/*     */ 
/* 162 */     this.m_helper.addLabelFieldPair(panel, "", (Component)objects[1], "schTable1Column");
/*     */ 
/* 165 */     objects = createTableAndColumnSelectionControls("schTable2Table", "schTable2Column");
/* 166 */     this.m_helper.addLabelFieldPair(panel, this.m_systemInterface.localizeMessage("!apSchemaRelationChildInfo"), (Component)objects[0], "schTable2Table");
/*     */ 
/* 169 */     this.m_helper.addLabelFieldPair(panel, "", (Component)objects[1], "schTable2Column");
/*     */ 
/* 171 */     return panel;
/*     */   }
/*     */ 
/*     */   public Object[] createTableAndColumnSelectionControls(String parentName, String childName)
/*     */   {
/* 184 */     Object[] objects = new Object[2];
/* 185 */     Object[] choiceData = new Object[3];
/*     */      tmp20_17 = new JComboBox(); JComboBox tableChoice = tmp20_17; objects[0] = tmp20_17;
/*     */      tmp33_30 = new JComboBox(); JComboBox columnChoice = tmp33_30; objects[1] = tmp33_30;
/* 190 */     choiceData[0] = columnChoice;
/* 191 */     choiceData[1] = parentName;
/* 192 */     choiceData[2] = childName;
/* 193 */     this.m_choiceObjects.put(objects[0], choiceData);
/* 194 */     DataResultSet set = this.m_tablesConfig.shallowClone();
/* 195 */     tableChoice.addItemListener(this);
/*     */     try
/*     */     {
/* 199 */       FieldInfo[] infos = ResultSetUtils.createInfoList(set, new String[] { "schTableName" }, true);
/*     */ 
/* 201 */       String selectValue = this.m_helper.m_props.getProperty(parentName);
/* 202 */       for (set.first(); set.isRowPresent(); set.next())
/*     */       {
/* 204 */         String table = set.getStringValue(infos[0].m_index);
/* 205 */         if (selectValue == null)
/*     */         {
/* 207 */           selectValue = table;
/*     */         }
/* 209 */         tableChoice.addItem(table);
/*     */       }
/*     */ 
/* 212 */       if (selectValue != null)
/*     */       {
/* 216 */         tableChoice.setSelectedItem(selectValue);
/* 217 */         ItemEvent event = new ItemEvent(tableChoice, 701, selectValue, 1);
/*     */ 
/* 220 */         itemStateChanged(event);
/*     */       }
/*     */     }
/*     */     catch (DataException ignore)
/*     */     {
/* 225 */       Report.trace("schema", null, ignore);
/*     */     }
/* 227 */     return objects;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent event)
/*     */   {
/* 237 */     ItemSelectable sourceObject = event.getItemSelectable();
/* 238 */     Object[] choiceData = (Object[])(Object[])this.m_choiceObjects.get(sourceObject);
/* 239 */     if (choiceData == null)
/*     */       return;
/* 241 */     JComboBox child = (JComboBox)choiceData[0];
/*     */ 
/* 243 */     String childField = (String)choiceData[2];
/* 244 */     Vector items = new IdcVector();
/*     */ 
/* 246 */     String firstValue = this.m_systemInterface.getString("apSelectTableEntry");
/* 247 */     items.addElement(firstValue);
/* 248 */     Object[] tableNames = sourceObject.getSelectedObjects();
/* 249 */     String childValue = this.m_helper.m_props.getProperty(childField);
/* 250 */     if (tableNames.length > 0)
/*     */     {
/* 252 */       String tableName = (String)tableNames[0];
/*     */       FieldInfo[] infos;
/*     */       try
/*     */       {
/* 257 */         infos = ResultSetUtils.createInfoList(this.m_tablesConfig, new String[] { "schTableName", "schTableDescription", "schColumnList" }, true);
/*     */       }
/*     */       catch (DataException ignore)
/*     */       {
/* 265 */         Report.trace("schema", null, ignore);
/* 266 */         return;
/*     */       }
/* 268 */       Vector row = this.m_tablesConfig.findRow(infos[0].m_index, tableName);
/* 269 */       if (row != null)
/*     */       {
/* 271 */         String list = this.m_tablesConfig.getStringValue(infos[2].m_index);
/* 272 */         if (list != null)
/*     */         {
/* 274 */           items.removeAllElements();
/* 275 */           Vector v = StringUtils.parseArray(list, ',', '^');
/* 276 */           for (int i = 0; i < v.size(); ++i)
/*     */           {
/* 278 */             String value = (String)v.elementAt(i);
/* 279 */             items.addElement(value);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 284 */     child.removeAllItems();
/* 285 */     for (int i = 0; i < items.size(); ++i)
/*     */     {
/* 287 */       child.addItem(items.elementAt(i));
/*     */     }
/* 289 */     if (childValue == null)
/*     */       return;
/* 291 */     child.setSelectedItem(childValue);
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent event)
/*     */   {
/*     */     try
/*     */     {
/* 301 */       AppLauncher.executeService(this.m_action, this.m_binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 305 */       Report.trace("schema", null, e);
/* 306 */       MessageBox.reportError(this.m_systemInterface, e);
/* 307 */       return false;
/*     */     }
/* 309 */     return true;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean isUpdate)
/*     */   {
/* 315 */     this.m_helper.exchangeComponentValue(exchange, isUpdate);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 320 */     String name = exchange.m_compName;
/* 321 */     String value = exchange.m_compValue;
/*     */ 
/* 326 */     return (!name.equals("schRelationName")) || 
/* 324 */       (value.length() != 0);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 335 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.AddRelationDialog
 * JD-Core Version:    0.5.4
 */