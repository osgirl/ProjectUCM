/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddViewValueDlg
/*     */   implements ComponentBinder
/*     */ {
/*  49 */   protected SystemInterface m_systemInterface = null;
/*  50 */   protected ExecutionContext m_context = null;
/*  51 */   protected DialogHelper m_helper = null;
/*  52 */   protected String m_helpPage = null;
/*     */ 
/*  54 */   protected DataResultSet m_resultSet = null;
/*  55 */   protected ComponentValidator m_cmpValidator = null;
/*  56 */   protected String m_internalColumn = null;
/*  57 */   protected Vector m_filter = null;
/*  58 */   protected String m_wildCards = null;
/*  59 */   protected Vector m_primaryClmns = null;
/*     */ 
/*     */   public AddViewValueDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  63 */     this.m_systemInterface = sys;
/*  64 */     this.m_context = sys.getExecutionContext();
/*  65 */     this.m_helper = new DialogHelper(sys, title, true);
/*  66 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(Properties props, String internalColumn, DataResultSet drset, Vector filter, boolean isAdd, String primaryClmns)
/*     */   {
/*  72 */     this.m_helper.m_props = props;
/*  73 */     this.m_resultSet = drset;
/*  74 */     this.m_cmpValidator = new ComponentValidator(this.m_resultSet);
/*  75 */     this.m_internalColumn = internalColumn;
/*  76 */     this.m_filter = filter;
/*  77 */     this.m_primaryClmns = StringUtils.parseArray(primaryClmns, ',', '^');
/*     */ 
/*  79 */     this.m_wildCards = SharedObjects.getEnvironmentValue("DatabaseWildcards");
/*  80 */     if (this.m_wildCards == null)
/*     */     {
/*  82 */       this.m_wildCards = "%_";
/*     */     }
/*     */ 
/*  86 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  91 */         return true;
/*     */       }
/*     */     };
/*  94 */     okCallback.m_dlgHelper = this.m_helper;
/*     */ 
/*  96 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/*  99 */     JPanel pnl = initUI(isAdd);
/*     */ 
/* 101 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 102 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 103 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 104 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 105 */     this.m_helper.addComponent(mainPanel, pnl);
/*     */ 
/* 107 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected JPanel initUI(boolean isAdd)
/*     */   {
/* 114 */     JPanel pnl = new PanePanel();
/* 115 */     this.m_helper.makePanelGridBag(pnl, 1);
/* 116 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*     */ 
/* 118 */     int num = this.m_resultSet.getNumFields();
/* 119 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 121 */       FieldInfo fi = new FieldInfo();
/* 122 */       this.m_resultSet.getIndexFieldInfo(i, fi);
/* 123 */       if (fi.m_name.startsWith("Display."))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 130 */       int displayWidth = fi.m_maxLen;
/* 131 */       if (displayWidth > 50)
/*     */       {
/* 133 */         displayWidth = 50;
/*     */       }
/* 135 */       boolean isFound = false;
/* 136 */       if ((this.m_internalColumn.equalsIgnoreCase(fi.m_name)) && (!isAdd))
/*     */       {
/* 138 */         isFound = true;
/* 139 */         this.m_helper.addLabelDisplayPair(pnl, fi.m_name, displayWidth, fi.m_name);
/*     */       }
/*     */       else
/*     */       {
/* 144 */         int size = this.m_primaryClmns.size();
/* 145 */         for (int j = 0; j < size; ++j)
/*     */         {
/* 147 */           String pkClmn = (String)this.m_primaryClmns.elementAt(j);
/* 148 */           if ((!pkClmn.equalsIgnoreCase(fi.m_name)) || (isAdd))
/*     */             continue;
/* 150 */           isFound = true;
/* 151 */           this.m_helper.addLabelDisplayPair(pnl, fi.m_name, displayWidth, fi.m_name);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 156 */       if (!isFound)
/*     */       {
/* 158 */         this.m_helper.addLabelEditPair(pnl, fi.m_name, displayWidth, fi.m_name);
/*     */       }
/*     */ 
/* 166 */       if (this.m_filter == null)
/*     */         continue;
/* 168 */       int size = this.m_filter.size();
/* 169 */       for (int j = 0; j < size; ++j)
/*     */       {
/* 171 */         FilterData fd = (FilterData)this.m_filter.elementAt(j);
/* 172 */         if (!fi.m_name.equals(fd.m_fieldDef.m_name))
/*     */           continue;
/* 174 */         int vNum = fd.m_values.size();
/* 175 */         if (vNum <= 0)
/*     */           continue;
/* 177 */         String val = (String)fd.m_values.elementAt(0);
/* 178 */         this.m_helper.m_props.put(fi.m_name, val);
/*     */ 
/* 181 */         String op = (String)fd.m_operators.elementAt(0);
/* 182 */         if (!op.equals("="))
/*     */           break;
/* 184 */         Object[] obj = this.m_helper.m_exchange.findComponent(fi.m_name, false);
/* 185 */         Component exCmp = (Component)obj[1];
/* 186 */         exCmp.setEnabled(false);
/* 187 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 194 */     return pnl;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 203 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 204 */     String name = exchange.m_compName;
/*     */ 
/* 206 */     FieldInfo info = new FieldInfo();
/* 207 */     this.m_resultSet.getFieldInfo(name, info);
/* 208 */     boolean isDate = info.m_type == 5;
/*     */ 
/* 210 */     if (isDate)
/*     */     {
/* 212 */       if (updateComponent)
/*     */       {
/* 214 */         String value = this.m_helper.m_props.getProperty(name);
/* 215 */         exchange.m_compValue = LocaleResources.localizeDate(value, this.m_context);
/*     */       }
/*     */       else
/*     */       {
/* 219 */         String value = exchange.m_compValue;
/* 220 */         value = LocaleResources.internationalizeDate(value, this.m_context);
/* 221 */         this.m_helper.m_props.put(name, value);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/* 226 */       helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 232 */     String name = exchange.m_compName;
/* 233 */     String val = exchange.m_compValue;
/* 234 */     IdcMessage errMsg = null;
/*     */ 
/* 237 */     if ((name.equals(this.m_internalColumn)) && ((
/* 239 */       (val == null) || (val.length() == 0))))
/*     */     {
/* 241 */       errMsg = IdcMessageFactory.lc("apSchInternalColumnValueMissing", new Object[] { name });
/*     */     }
/*     */ 
/* 244 */     if (name.equals("dProfileTriggerValue"))
/*     */     {
/* 246 */       boolean hasWildCard = StringUtils.containsWildcards(val);
/* 247 */       if (hasWildCard)
/*     */       {
/* 249 */         errMsg = IdcMessageFactory.lc("apDpTriggerValueIsInvalid", new Object[0]);
/*     */       }
/*     */     }
/* 252 */     if (errMsg == null)
/*     */     {
/* 254 */       if (this.m_filter != null)
/*     */       {
/* 256 */         errMsg = this.m_cmpValidator.validate(name, val, this.m_filter, 30, this.m_wildCards, null);
/*     */       }
/*     */       else
/*     */       {
/* 260 */         errMsg = this.m_cmpValidator.validateType(name, val, null);
/* 261 */         if (errMsg != null)
/*     */         {
/* 263 */           int max = this.m_cmpValidator.getMaxLength(name, 50);
/* 264 */           int length = val.length();
/* 265 */           if (val.length() > max)
/*     */           {
/* 267 */             errMsg = IdcMessageFactory.lc("apSchColumnValueTooLarge", new Object[] { name, Integer.valueOf(max), Integer.valueOf(length) });
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 273 */     if (errMsg != null)
/*     */     {
/* 275 */       exchange.m_errorMessage = errMsg;
/* 276 */       return false;
/*     */     }
/* 278 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 283 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95901 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.AddViewValueDlg
 * JD-Core Version:    0.5.4
 */