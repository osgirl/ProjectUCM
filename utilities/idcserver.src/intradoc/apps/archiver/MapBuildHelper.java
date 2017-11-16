/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.ClausesData;
/*     */ import intradoc.shared.gui.ClauseBuilderHelper;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class MapBuildHelper extends ClauseBuilderHelper
/*     */ {
/*  50 */   protected JTextField m_mappedField = null;
/*  51 */   protected DisplayChoice m_mappedToFields = null;
/*  52 */   protected JButton m_browseFieldBtn = null;
/*  53 */   protected DisplayChoice m_fieldChoice = null;
/*     */ 
/*  55 */   protected CollectionContext m_context = null;
/*  56 */   protected boolean m_isDocument = true;
/*  57 */   protected String m_tableName = null;
/*     */ 
/*     */   public MapBuildHelper(CollectionContext context)
/*     */   {
/*  61 */     this(context, null, true);
/*     */   }
/*     */ 
/*     */   public MapBuildHelper(CollectionContext context, String table, boolean isDocument)
/*     */   {
/*  66 */     this.m_numSegments = 2;
/*  67 */     this.m_context = context;
/*  68 */     this.m_isDocument = isDocument;
/*  69 */     if (isDocument)
/*     */       return;
/*  71 */     this.m_tableName = table;
/*     */   }
/*     */ 
/*     */   public void setPanelData()
/*     */   {
/*  78 */     GridBagHelper gh = this.m_guiHelper.m_gridHelper;
/*  79 */     gh.reset();
/*  80 */     DynamicComponentExchange exchange = this.m_guiHelper.m_exchange;
/*  81 */     this.m_clauseEditPanel.removeAll();
/*     */ 
/*  83 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, new CustomLabel(this.m_fieldTitle));
/*  84 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, new CustomLabel("        "));
/*  85 */     this.m_guiHelper.addLastComponentInRow(this.m_clauseEditPanel, new CustomLabel(this.m_valueTitle));
/*     */ 
/*  87 */     gh.m_gc.fill = 2;
/*  88 */     gh.m_gc.weightx = 1.0D;
/*  89 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, this.m_mappedField = new JTextField(20));
/*  90 */     exchange.removeComponent("MapField");
/*  91 */     exchange.addComponent("MapField", this.m_mappedField, null);
/*     */ 
/*  93 */     CustomLabel lbl = new CustomLabel(LocaleResources.getString("apLabelMapsTo", this.m_cxt));
/*  94 */     lbl.setAlignment(0);
/*  95 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, lbl);
/*     */ 
/*  97 */     this.m_guiHelper.addLastComponentInRow(this.m_clauseEditPanel, this.m_mappedToFields = new DisplayChoice());
/*  98 */     exchange.removeComponent("MapValue");
/*  99 */     exchange.addComponent("MapValue", this.m_mappedToFields, null);
/* 100 */     this.m_mappedToFields.addItemListener(this);
/*     */ 
/* 102 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, this.m_fieldChoice = new DisplayChoice());
/* 103 */     this.m_fieldChoice.addItemListener(this);
/* 104 */     initFieldChoice();
/*     */ 
/* 106 */     this.m_guiHelper.addComponent(this.m_clauseEditPanel, this.m_browseFieldBtn = new JButton(LocaleResources.getString("apDlgButtonBrowseForFields", this.m_cxt)));
/*     */ 
/* 108 */     this.m_browseFieldBtn.addActionListener(this);
/* 109 */     gh.addEmptyRowElement(this.m_clauseEditPanel);
/*     */ 
/* 111 */     this.m_guiHelper.addLastComponentInRow(this.m_clauseEditPanel, this.m_clauseValPanel = new PanePanel());
/* 112 */     this.m_clauseValPanel.setLayout(new GridBagLayout());
/*     */ 
/* 114 */     addFieldList(this.m_mappedToFields);
/* 115 */     this.m_clauseEditPanel.validate();
/*     */   }
/*     */ 
/*     */   protected void initFieldChoice()
/*     */   {
/* 120 */     this.m_fieldChoice.addItem(LocaleResources.getString("apChoiceNoValues", this.m_cxt));
/* 121 */     this.m_fieldChoice.setEnabled(false);
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 130 */     Object target = e.getSource();
/*     */ 
/* 132 */     if (target == this.m_clauseList)
/*     */     {
/* 134 */       enableDisable(true);
/*     */     } else {
/* 136 */       if (target != this.m_fieldChoice)
/*     */         return;
/* 138 */       updateMappedField();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void updateMappedField()
/*     */   {
/* 144 */     String fieldName = this.m_fieldChoice.getSelectedInternalValue();
/* 145 */     if (fieldName == null)
/*     */       return;
/* 147 */     this.m_mappedField.setText(fieldName);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 157 */     Object target = e.getSource();
/* 158 */     if (target == this.m_browseFieldBtn)
/*     */     {
/* 160 */       BrowseFieldDlg dlg = new BrowseFieldDlg(this.m_sysInterface, LocaleResources.getString("apLabelBrowseForFields", this.m_cxt), this.m_context);
/*     */ 
/* 162 */       if (dlg.init(this.m_tableName, !this.m_isDocument) == 1)
/*     */       {
/* 164 */         boolean isLocal = dlg.getIsLocal();
/* 165 */         String fileName = dlg.getSelectedFile();
/* 166 */         if ((!this.m_isDocument) && (isLocal))
/*     */         {
/* 168 */           fileName = this.m_tableName;
/*     */         }
/* 170 */         String[][] fields = getMappingFields(isLocal, fileName, this.m_isDocument);
/* 171 */         if (fields == null)
/*     */         {
/* 174 */           this.m_context.reportError(LocaleResources.getString("apUnableToRetrieveFieldInfo", this.m_cxt));
/*     */ 
/* 176 */           initFieldChoice();
/* 177 */           return;
/*     */         }
/* 179 */         this.m_fieldChoice.init(fields);
/* 180 */         updateMappedField();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 185 */       super.actionPerformed(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String[][] getMappingFields(boolean isLocal, String fileName, boolean isDocument)
/*     */   {
/* 191 */     boolean isTable = !isDocument;
/* 192 */     String[][] fields = this.m_context.getBatchFields(isLocal, fileName, isTable);
/* 193 */     if ((isTable) && (!isLocal))
/*     */     {
/* 195 */       Properties props = this.m_context.getBatchProperties(fileName);
/* 196 */       String parents = props.getProperty("parentTables");
/* 197 */       if ((parents != null) && (parents.trim().length() != 0))
/*     */       {
/* 199 */         Vector tables = StringUtils.parseArray(parents, ',', '^');
/* 200 */         String table = props.getProperty("tableName");
/* 201 */         tables.insertElementAt(table, 0);
/* 202 */         int size = tables.size();
/* 203 */         int index = -1;
/* 204 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 206 */           table = (String)tables.elementAt(i);
/* 207 */           int numField = Integer.parseInt(props.getProperty("numFields" + table));
/* 208 */           for (int j = 0; j < numField; ++j)
/*     */           {
/* 210 */             ++index;
/* 211 */             if (fields[index][0].indexOf(46) < 0)
/*     */             {
/* 213 */               fields[index][0] = (table + "." + fields[index][0]);
/*     */             }
/*     */ 
/* 216 */             if (fields[index][1].indexOf(46) >= 0)
/*     */               continue;
/* 218 */             fields[index][1] = (table + "." + fields[index][1]);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 224 */     return fields;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 233 */     String name = exchange.m_compName;
/* 234 */     int index = -1;
/* 235 */     if (name.equals("MapField"))
/*     */     {
/* 237 */       index = 0;
/*     */     }
/* 239 */     else if (name.equals("MapValue"))
/*     */     {
/* 241 */       index = 1;
/*     */     }
/*     */ 
/* 244 */     String str = "";
/* 245 */     if (index >= 0)
/*     */     {
/* 247 */       if (updateComponent)
/*     */       {
/* 249 */         exchange.m_compValue = "";
/*     */       }
/*     */ 
/* 252 */       if (this.m_curClause == null)
/*     */         return;
/* 254 */       if (updateComponent)
/*     */       {
/* 256 */         str = (String)this.m_curClause.elementAt(index);
/* 257 */         exchange.m_compValue = str;
/*     */       }
/*     */       else
/*     */       {
/* 261 */         str = exchange.m_compValue;
/* 262 */         this.m_curClause.setElementAt(str, index);
/*     */       }
/*     */ 
/*     */     }
/* 268 */     else if (updateComponent)
/*     */     {
/* 270 */       exchange.m_compValue = getQueryProp(name);
/*     */     }
/*     */     else
/*     */     {
/* 274 */       setQueryProp(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 282 */     String name = exchange.m_compName;
/* 283 */     String val = exchange.m_compValue;
/*     */ 
/* 285 */     if (val != null)
/*     */     {
/* 287 */       val = val.trim();
/* 288 */       if (val.length() == 0)
/*     */       {
/* 290 */         val = null;
/*     */       }
/*     */     }
/*     */ 
/* 294 */     if ((((name.equals("MapValue")) || (name.equals("MapField")))) && 
/* 296 */       (val == null))
/*     */     {
/* 298 */       exchange.m_errorMessage = IdcMessageFactory.lc("apSpecifyMapping", new Object[0]);
/* 299 */       return false;
/*     */     }
/*     */ 
/* 302 */     return true;
/*     */   }
/*     */ 
/*     */   protected void refreshClauseList()
/*     */     throws ServiceException
/*     */   {
/* 311 */     this.m_clauseList.removeAllItems();
/*     */ 
/* 314 */     Vector clauses = this.m_clauseData.m_clauses;
/* 315 */     int nclauses = clauses.size();
/*     */ 
/* 317 */     int curIndex = -1;
/*     */ 
/* 320 */     for (int i = 0; i < nclauses; ++i)
/*     */     {
/* 322 */       IdcStringBuilder dispStr = new IdcStringBuilder();
/* 323 */       Vector elts = (Vector)clauses.elementAt(i);
/* 324 */       if (this.m_curClause == elts)
/*     */       {
/* 326 */         curIndex = i;
/*     */       }
/* 328 */       this.m_clauseData.createClauseString(elts, dispStr);
/* 329 */       this.m_clauseList.add(dispStr.toString());
/*     */     }
/*     */ 
/* 332 */     if (curIndex >= 0)
/*     */     {
/* 334 */       this.m_clauseList.select(curIndex);
/*     */     }
/*     */ 
/* 338 */     this.m_clauseData.m_dispStr = LocaleResources.getString("apLabelFieldMaps", this.m_cxt);
/*     */ 
/* 341 */     enableDisable(false);
/*     */   }
/*     */ 
/*     */   protected boolean checkSelectedClauseDocField()
/*     */   {
/* 348 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 353 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.MapBuildHelper
 * JD-Core Version:    0.5.4
 */