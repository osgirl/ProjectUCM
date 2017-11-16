/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class AddColumnDlg extends DialogCallback
/*     */   implements ComponentBinder, ItemListener
/*     */ {
/*  58 */   protected SystemInterface m_systemInterface = null;
/*  59 */   protected ExecutionContext m_context = null;
/*  60 */   protected DialogHelper m_helper = null;
/*  61 */   protected String m_helpPage = null;
/*     */ 
/*  63 */   protected DataResultSet m_currentClmnSet = null;
/*  64 */   protected String m_initialColumnName = null;
/*     */   protected boolean m_isAdd;
/*     */   protected boolean m_isNewTable;
/*     */   protected DisplayChoice m_typeChoice;
/*     */   protected JTextField m_lengthBox;
/*  70 */   protected String m_cachedLength = null;
/*     */   protected int m_originalLength;
/*     */   protected JCheckBox m_primaryKeyBox;
/*  73 */   protected boolean m_cachedPrimaryKey = false;
/*     */ 
/*     */   public AddColumnDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  77 */     this.m_systemInterface = sys;
/*  78 */     this.m_context = sys.getExecutionContext();
/*  79 */     title = LocaleResources.localizeMessage(title, this.m_context);
/*  80 */     this.m_helper = new DialogHelper(sys, title, true);
/*  81 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public boolean getBoolean(String key)
/*     */   {
/*  86 */     String value = this.m_helper.m_props.getProperty(key);
/*  87 */     return StringUtils.convertToBool(value, false);
/*     */   }
/*     */ 
/*     */   public int init(Properties props, DataResultSet currentClmnSet, Map<String, String> args)
/*     */   {
/*  92 */     this.m_helper.m_props = props;
/*  93 */     this.m_isAdd = StringUtils.convertToBool((String)args.get("isNewColumn"), false);
/*  94 */     this.m_isNewTable = StringUtils.convertToBool((String)args.get("isNewTable"), false);
/*  95 */     if (this.m_isAdd)
/*     */     {
/*  97 */       this.m_currentClmnSet = currentClmnSet;
/*     */     }
/*     */ 
/* 100 */     this.m_dlgHelper = this.m_helper;
/*     */ 
/* 102 */     boolean isNewToEdit = StringUtils.convertToBool((String)args.get("isNewToEdit"), false);
/* 103 */     if ((!this.m_isAdd) && (!this.m_isNewTable))
/*     */     {
/* 105 */       String originalLength = props.getProperty("ColumnLength");
/* 106 */       if ((originalLength != null) && (originalLength.length() > 0))
/*     */       {
/* 108 */         this.m_originalLength = NumberUtils.parseInteger(originalLength, -1);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 113 */     initUI(isNewToEdit);
/*     */ 
/* 115 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI(boolean isAdd)
/*     */   {
/* 120 */     JPanel mainPanel = this.m_helper.initStandard(this, this, 1, true, this.m_helpPage);
/*     */ 
/* 123 */     String label = LocaleResources.getString("apSchColumnName", this.m_context);
/* 124 */     this.m_helper.addLabelEditPair(mainPanel, label, 40, "ColumnName");
/*     */ 
/* 129 */     this.m_typeChoice = new DisplayChoice();
/* 130 */     this.m_lengthBox = new JTextField();
/* 131 */     this.m_primaryKeyBox = new JCheckBox(this.m_systemInterface.getString("apSchEnabled"));
/*     */ 
/* 133 */     this.m_typeChoice.addItemListener(this);
/* 134 */     this.m_typeChoice.init(TableFields.SCHEMAFIELD_TYPES_OPTIONSLIST);
/* 135 */     if ((isAdd) || (this.m_isNewTable))
/*     */     {
/* 137 */       this.m_helper.addLabelFieldPair(mainPanel, this.m_systemInterface.getString("apSchColumnType"), this.m_typeChoice, "ColumnType");
/*     */     }
/*     */     else
/*     */     {
/* 143 */       this.m_helper.addLabelDisplayPair(mainPanel, this.m_systemInterface.getString("apSchColumnType"), 30, "ColumnType");
/*     */     }
/*     */ 
/* 146 */     this.m_helper.addLabelFieldPair(mainPanel, this.m_systemInterface.getString("apSchColumnLength"), this.m_lengthBox, "ColumnLength");
/*     */ 
/* 149 */     this.m_helper.addLabelFieldPair(mainPanel, this.m_systemInterface.getString("apSchPrimaryKey"), this.m_primaryKeyBox, "IsPrimaryKey");
/*     */ 
/* 153 */     String type = this.m_helper.m_props.getProperty("ColumnType");
/* 154 */     if (isVariableLengthType(type))
/*     */     {
/* 156 */       this.m_lengthBox.setEnabled(true);
/*     */     }
/*     */     else
/*     */     {
/* 160 */       this.m_helper.m_props.put("ColumnLength", "");
/* 161 */       this.m_lengthBox.setEnabled(false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 171 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 172 */     helper.exchangeComponentValue(exchange, updateComponent);
/* 173 */     if ((this.m_initialColumnName != null) || (!exchange.m_compName.equals("ColumnName")))
/*     */       return;
/* 175 */     this.m_initialColumnName = exchange.m_compValue;
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 181 */     String name = exchange.m_compName;
/* 182 */     String val = exchange.m_compValue;
/* 183 */     IdcMessage errMsg = null;
/*     */ 
/* 186 */     if (name.equals("ColumnName"))
/*     */     {
/* 188 */       if ((!this.m_isAdd) && (!this.m_isNewTable) && (this.m_initialColumnName != null) && (!val.equalsIgnoreCase(this.m_initialColumnName)))
/*     */       {
/* 192 */         errMsg = IdcMessageFactory.lc("apSchemaColumnCaseChangeOnly", new Object[] { this.m_initialColumnName });
/*     */       }
/*     */ 
/* 195 */       if (this.m_currentClmnSet != null)
/*     */       {
/* 197 */         int index = 0;
/*     */         try
/*     */         {
/* 200 */           index = ResultSetUtils.getIndexMustExist(this.m_currentClmnSet, "ColumnName");
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 204 */           Report.trace("schema", null, e);
/*     */         }
/* 206 */         Vector row = this.m_currentClmnSet.findRow(index, val);
/* 207 */         if (row != null)
/*     */         {
/* 209 */           errMsg = IdcMessageFactory.lc("apSchColumnNameNotUnique", new Object[0]);
/*     */         }
/*     */ 
/* 212 */         if (errMsg == null)
/*     */         {
/* 215 */           int result = Validation.checkDatabaseFieldName(name);
/* 216 */           switch (result)
/*     */           {
/*     */           case 0:
/* 222 */             if (name.length() > 64)
/*     */             {
/* 224 */               errMsg = IdcMessageFactory.lc("apSchColumnNameExceedsMaxLength", new Object[0]); } break;
/*     */           case -1:
/* 228 */             errMsg = IdcMessageFactory.lc("apSchSpecifyColumnName", new Object[0]);
/* 229 */             break;
/*     */           case -2:
/* 231 */             errMsg = IdcMessageFactory.lc("apSchColumnCannotContainSpaces", new Object[0]);
/* 232 */             break;
/*     */           case -3:
/* 234 */             errMsg = IdcMessageFactory.lc("apSchInvalidCharInColumnName", new Object[0]);
/* 235 */             break;
/*     */           default:
/* 237 */             errMsg = IdcMessageFactory.lc("apSchInvalidNameForColumnName", new Object[0]);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 244 */     if (errMsg != null)
/*     */     {
/* 246 */       exchange.m_errorMessage = errMsg;
/* 247 */       return false;
/*     */     }
/* 249 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent e)
/*     */   {
/* 256 */     IdcMessage errMsg = null;
/* 257 */     String clmnName = this.m_helper.m_props.getProperty("ColumnName");
/* 258 */     String clmnType = this.m_helper.m_props.getProperty("ColumnType");
/* 259 */     boolean isCreateTimestamp = getBoolean("IsCreateTimestamp");
/* 260 */     boolean isModifyTimestamp = getBoolean("IsModifyTimestamp");
/* 261 */     if ((clmnName == null) || (clmnName.length() == 0))
/*     */     {
/* 263 */       errMsg = IdcMessageFactory.lc("apSchColumnNameEmpty", new Object[0]);
/*     */     }
/*     */ 
/* 266 */     if ((((clmnName.equals("sCreateTs")) || (clmnName.equals("sLastModified")) || (isCreateTimestamp) || (isModifyTimestamp))) && 
/* 270 */       (!clmnType.equals("date")))
/*     */     {
/* 272 */       errMsg = IdcMessageFactory.lc("apSchColumnMustBeOfTypeDate", new Object[] { clmnName });
/*     */     }
/*     */ 
/* 276 */     if (isVariableLengthType(clmnType))
/*     */     {
/* 278 */       String clmnLength = this.m_helper.m_props.getProperty("ColumnLength");
/* 279 */       int len = NumberUtils.parseInteger(clmnLength, 0);
/* 280 */       if (len <= 0)
/*     */       {
/* 282 */         errMsg = IdcMessageFactory.lc("apSchColumnLengthPositive", new Object[0]);
/*     */       }
/* 288 */       else if (len < this.m_originalLength)
/*     */       {
/* 290 */         errMsg = IdcMessageFactory.lc("apSchColumnLengthShorteningError", new Object[] { "" + this.m_originalLength });
/* 291 */         this.m_lengthBox.setText("" + this.m_originalLength);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 301 */       this.m_helper.m_props.put("ColumnLength", "");
/*     */     }
/* 303 */     if (errMsg != null)
/*     */     {
/* 305 */       MessageBox.reportError(this.m_systemInterface, errMsg);
/* 306 */       return false;
/*     */     }
/*     */ 
/* 309 */     return true;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent event)
/*     */   {
/* 314 */     int stateChange = event.getStateChange();
/* 315 */     Object object = event.getSource();
/* 316 */     if (object == this.m_typeChoice)
/*     */     {
/* 318 */       switch (stateChange)
/*     */       {
/*     */       case 1:
/* 321 */         int index = this.m_typeChoice.getSelectedIndex();
/* 322 */         String[] info = TableFields.SCHEMAFIELD_TYPES_OPTIONSLIST[index];
/* 323 */         boolean allowLength = StringUtils.convertToBool(info[2], false);
/* 324 */         boolean allowPrimaryKey = StringUtils.convertToBool(info[3], false);
/* 325 */         if (allowLength)
/*     */         {
/* 327 */           if ((!this.m_lengthBox.isEnabled()) && (this.m_cachedLength != null))
/*     */           {
/* 329 */             this.m_lengthBox.setText(this.m_cachedLength);
/* 330 */             this.m_cachedLength = null;
/*     */           }
/*     */ 
/*     */         }
/* 335 */         else if (this.m_cachedLength == null)
/*     */         {
/* 337 */           this.m_cachedLength = this.m_lengthBox.getText();
/* 338 */           this.m_lengthBox.setText("");
/*     */         }
/*     */ 
/* 341 */         this.m_lengthBox.setEnabled(allowLength);
/*     */ 
/* 343 */         if (allowPrimaryKey)
/*     */         {
/* 345 */           if (!this.m_primaryKeyBox.isEnabled())
/*     */           {
/* 347 */             this.m_primaryKeyBox.setSelected(this.m_cachedPrimaryKey);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 352 */           this.m_cachedPrimaryKey = this.m_primaryKeyBox.isSelected();
/* 353 */           this.m_primaryKeyBox.setSelected(false);
/*     */         }
/* 355 */         this.m_primaryKeyBox.setEnabled(allowPrimaryKey);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 361 */       Report.trace("schema", "unknown object in itemStateChanged()", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean isVariableLengthType(String typeName)
/*     */   {
/* 367 */     if (typeName == null)
/*     */     {
/* 369 */       return true;
/*     */     }
/*     */ 
/* 372 */     String[][] rules = TableFields.SCHEMAFIELD_TYPES_OPTIONSLIST;
/* 373 */     for (int i = 0; i < rules.length; ++i)
/*     */     {
/* 375 */       if (!typeName.equalsIgnoreCase(rules[i][0]))
/*     */         continue;
/* 377 */       boolean isVariableLength = StringUtils.convertToBool(rules[i][2], false);
/*     */ 
/* 379 */       return isVariableLength;
/*     */     }
/*     */ 
/* 382 */     for (int i = 0; i < rules.length; ++i)
/*     */     {
/* 384 */       if (typeName.toLowerCase().indexOf(rules[i][0].toLowerCase()) < 0)
/*     */         continue;
/* 386 */       boolean isVariableLength = StringUtils.convertToBool(rules[i][2], false);
/*     */ 
/* 388 */       return isVariableLength;
/*     */     }
/*     */ 
/* 392 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 397 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81789 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.AddColumnDlg
 * JD-Core Version:    0.5.4
 */