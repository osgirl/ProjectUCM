/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.gui.FilterData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditBatchModeDlg
/*     */   implements ComponentBinder
/*     */ {
/*  57 */   protected SystemInterface m_systemInterface = null;
/*  58 */   protected ExecutionContext m_cxt = null;
/*  59 */   protected SharedContext m_shContext = null;
/*  60 */   protected DialogHelper m_helper = null;
/*  61 */   protected String m_helpPage = null;
/*     */ 
/*  64 */   protected Properties m_filterProps = null;
/*  65 */   protected Vector m_filter = null;
/*  66 */   protected DataResultSet m_changedSet = null;
/*  67 */   protected ComponentValidator m_cmpValidator = null;
/*     */ 
/*  70 */   protected CustomTextArea m_batchText = null;
/*     */ 
/*     */   public EditBatchModeDlg(SystemInterface sys, String title, String helpPage, SharedContext shContext)
/*     */   {
/*  74 */     this.m_helper = new DialogHelper(sys, title, true, true);
/*  75 */     this.m_systemInterface = sys;
/*  76 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*  77 */     this.m_shContext = shContext;
/*  78 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(Properties props, Properties filterProps, Vector filter, DataResultSet currentValues)
/*     */   {
/*  83 */     this.m_helper.m_props = props;
/*  84 */     this.m_filterProps = filterProps;
/*  85 */     this.m_filter = filter;
/*  86 */     this.m_changedSet = new DataResultSet();
/*  87 */     this.m_changedSet.copy(currentValues);
/*  88 */     this.m_cmpValidator = new ComponentValidator(currentValues);
/*     */ 
/*  90 */     initUI();
/*     */ 
/*  92 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  97 */     DialogCallback okCallback = createOkCallback();
/*  98 */     JPanel pnl = this.m_helper.initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 101 */     String msg = this.m_systemInterface.getString("apSchDescBatchValues");
/* 102 */     CustomText descText = new CustomText(msg);
/* 103 */     this.m_helper.addLastComponentInRow(pnl, descText);
/*     */ 
/* 106 */     String filterMsg = buildFilterMessage();
/* 107 */     if ((filterMsg != null) && (filterMsg.length() > 0))
/*     */     {
/* 109 */       filterMsg = "!apSchFilterMatch!$\n" + filterMsg;
/* 110 */       filterMsg = LocaleResources.localizeMessage(filterMsg, this.m_cxt);
/* 111 */       CustomText filterDescText = new CustomText(filterMsg);
/* 112 */       this.m_helper.addLastComponentInRow(pnl, filterDescText);
/*     */     }
/*     */ 
/* 115 */     this.m_batchText = new CustomTextArea();
/* 116 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 117 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 118 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 119 */     this.m_helper.addExchangeComponent(pnl, this.m_batchText, "BatchValues");
/*     */   }
/*     */ 
/*     */   protected DialogCallback createOkCallback()
/*     */   {
/* 125 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 130 */         boolean isSuccess = false;
/*     */         try
/*     */         {
/* 133 */           DataBinder binder = EditBatchModeDlg.this.buildBinder();
/* 134 */           EditBatchModeDlg.this.m_shContext.executeService("EDIT_SCHEMA_VIEW_VALUES", binder, false);
/* 135 */           isSuccess = true;
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 139 */           MessageBox.reportError(EditBatchModeDlg.this.m_systemInterface, exp);
/*     */         }
/* 141 */         return isSuccess;
/*     */       }
/*     */     };
/* 145 */     return okCallback;
/*     */   }
/*     */ 
/*     */   protected String buildFilterMessage()
/*     */   {
/* 150 */     String msg = "";
/* 151 */     int size = this.m_filter.size();
/* 152 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 154 */       FilterData fd = (FilterData)this.m_filter.elementAt(i);
/* 155 */       String name = fd.m_fieldDef.m_name;
/* 156 */       String val = "";
/* 157 */       int num = fd.m_values.size();
/* 158 */       if (num > 0)
/*     */       {
/* 160 */         val = (String)fd.m_values.elementAt(0);
/* 161 */         if (fd.m_fieldDef.m_type.equals("date"))
/*     */         {
/* 163 */           val = LocaleResources.localizeDate(val, this.m_cxt);
/*     */         }
/*     */       }
/* 166 */       String op = "=";
/* 167 */       num = fd.m_operators.size();
/* 168 */       if (num > 0)
/*     */       {
/* 170 */         op = (String)fd.m_operators.elementAt(0);
/*     */       }
/*     */ 
/* 173 */       msg = msg + LocaleUtils.encodeMessage("apSchValueFilterStub", null, name, val, op);
/* 174 */       msg = msg + "!$\n";
/*     */     }
/* 176 */     return msg;
/*     */   }
/*     */ 
/*     */   protected DataBinder buildBinder() throws DataException
/*     */   {
/* 181 */     DataBinder binder = new DataBinder();
/* 182 */     Properties props = (Properties)this.m_helper.m_props.clone();
/* 183 */     binder.setLocalData(props);
/* 184 */     binder.putLocal("editViewValueAction", "batch");
/* 185 */     String tableName = this.m_helper.m_props.getProperty("schTableName");
/* 186 */     binder.addResultSet(tableName, this.m_changedSet);
/*     */ 
/* 188 */     return binder;
/*     */   }
/*     */ 
/*     */   public boolean retrieveComponentValues()
/*     */   {
/* 193 */     boolean result = this.m_helper.retrieveComponentValues();
/* 194 */     if (result)
/*     */     {
/* 196 */       String str = this.m_helper.m_props.getProperty("BatchValues");
/* 197 */       IdcMessage errMsg = createResultSet(str);
/*     */ 
/* 199 */       if (errMsg != null)
/*     */       {
/* 201 */         MessageBox.reportError(this.m_systemInterface, errMsg);
/* 202 */         return false;
/*     */       }
/*     */     }
/* 205 */     return result;
/*     */   }
/*     */ 
/*     */   protected String computeValueString()
/*     */   {
/* 211 */     StringBuffer buff = new StringBuffer();
/*     */ 
/* 214 */     int numFields = this.m_changedSet.getNumFields();
/* 215 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 217 */       FieldInfo fi = new FieldInfo();
/* 218 */       this.m_changedSet.getIndexFieldInfo(i, fi);
/* 219 */       if (i > 0)
/*     */       {
/* 221 */         buff.append("\t\t|\t");
/*     */       }
/* 223 */       buff.append(fi.m_name);
/*     */     }
/*     */ 
/* 227 */     buff.append("\n");
/* 228 */     int size = buff.length();
/* 229 */     for (int i = 0; i < 4 * size; ++i)
/*     */     {
/* 231 */       buff.append("-");
/*     */     }
/*     */ 
/* 234 */     for (this.m_changedSet.first(); this.m_changedSet.isRowPresent(); this.m_changedSet.next())
/*     */     {
/* 236 */       buff.append("\n");
/* 237 */       for (int i = 0; i < numFields; ++i)
/*     */       {
/* 239 */         if (i > 0)
/*     */         {
/* 241 */           buff.append("\t\t|\t");
/*     */         }
/* 243 */         buff.append(this.m_changedSet.getStringValue(i));
/*     */       }
/*     */     }
/* 246 */     return buff.toString();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 254 */     String name = exchange.m_compName;
/* 255 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 260 */     String name = exchange.m_compName;
/* 261 */     String val = exchange.m_compValue;
/*     */ 
/* 263 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 269 */     if ((name.equals("BatchValues")) && (updateComponent))
/*     */     {
/* 271 */       this.m_helper.m_props.put(name, computeValueString());
/*     */     }
/* 273 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 278 */     if (name.equals("BatchValues"))
/*     */     {
/* 280 */       IdcMessage errMsg = createResultSet(val);
/* 281 */       if (errMsg != null)
/*     */       {
/* 283 */         exchange.m_errorMessage = errMsg;
/* 284 */         return false;
/*     */       }
/*     */     }
/* 287 */     return true;
/*     */   }
/*     */ 
/*     */   protected IdcMessage createResultSet(String rsetStr)
/*     */   {
/* 292 */     IdcMessage errMsg = null;
/* 293 */     DataResultSet rset = new DataResultSet();
/* 294 */     rset.copyFieldInfo(this.m_changedSet);
/*     */ 
/* 296 */     int numFields = this.m_changedSet.getNumFields();
/* 297 */     Vector rows = StringUtils.parseArray(rsetStr, '\n', '\\');
/*     */ 
/* 299 */     String wildCards = SharedObjects.getEnvironmentValue("DatabaseWildcards");
/* 300 */     if (wildCards == null)
/*     */     {
/* 302 */       wildCards = "%_";
/*     */     }
/*     */ 
/* 305 */     String internalClmn = this.m_helper.m_props.getProperty("schInternalColumn");
/* 306 */     int internalClmnIndex = -1;
/*     */     try
/*     */     {
/* 309 */       internalClmnIndex = ResultSetUtils.getIndexMustExist(rset, internalClmn);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 313 */       errMsg = IdcMessageFactory.lc("apSchMissingInternalClmn", new Object[] { internalClmn });
/* 314 */       return errMsg;
/*     */     }
/*     */ 
/* 317 */     Hashtable valMap = new Hashtable();
/*     */ 
/* 320 */     int size = rows.size();
/* 321 */     int count = 2;
/* 322 */     for (; count < size; ++count)
/*     */     {
/* 324 */       String str = (String)rows.elementAt(count);
/* 325 */       Vector row = StringUtils.parseArrayEx(str, '|', '*', true);
/* 326 */       if (row.size() != numFields)
/*     */       {
/* 328 */         errMsg = IdcMessageFactory.lc(errMsg, "apSchValuesIncorrectNumColumns", new Object[] { "" + (count - 1) });
/* 329 */         break;
/*     */       }
/*     */ 
/* 332 */       for (int i = 0; i < numFields; ++i)
/*     */       {
/* 334 */         FieldInfo fi = new FieldInfo();
/* 335 */         this.m_changedSet.getIndexFieldInfo(i, fi);
/*     */ 
/* 337 */         String value = (String)row.elementAt(i);
/* 338 */         errMsg = this.m_cmpValidator.validate(fi.m_name, value, this.m_filter, 30, wildCards, null);
/* 339 */         if (errMsg != null)
/*     */         {
/*     */           break;
/*     */         }
/*     */ 
/* 344 */         if (i != internalClmnIndex)
/*     */           continue;
/* 346 */         if ((value == null) || (value.length() == 0))
/*     */         {
/* 348 */           errMsg = IdcMessageFactory.lc("apSchInternalColumnValueMissing", new Object[] { internalClmn });
/* 349 */           break;
/*     */         }
/*     */ 
/* 352 */         boolean isDoCheck = SharedObjects.getEnvValueAsBoolean("SchemaBatchEditCheck", true);
/* 353 */         if (!isDoCheck)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 359 */         Object obj = valMap.get(value);
/* 360 */         if (obj != null)
/*     */         {
/* 362 */           String objStr = (String)obj;
/* 363 */           errMsg = IdcMessageFactory.lc("apSchInternalColumnValueExists", new Object[] { internalClmn, value, objStr });
/*     */ 
/* 365 */           break;
/*     */         }
/* 367 */         valMap.put(value, "" + (count - 1));
/*     */       }
/*     */ 
/* 371 */       if (errMsg != null)
/*     */       {
/* 373 */         errMsg = IdcMessageFactory.lc(errMsg, "apSchValuesInvalidRow", new Object[] { "" + (count - 1) });
/* 374 */         return errMsg;
/*     */       }
/* 376 */       rset.addRow(row);
/*     */     }
/*     */ 
/* 379 */     this.m_changedSet = rset;
/* 380 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 385 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditBatchModeDlg
 * JD-Core Version:    0.5.4
 */