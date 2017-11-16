/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.ResultData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditResultPage
/*     */   implements ComponentBinder
/*     */ {
/*  60 */   public SystemInterface m_sysInterface = null;
/*  61 */   protected DialogHelper m_helper = null;
/*  62 */   protected ResultData m_data = null;
/*  63 */   protected boolean m_isNew = false;
/*  64 */   protected String m_helpPage = null;
/*     */ 
/*  66 */   protected EditResultCustomPanel m_resultsPanel = null;
/*     */ 
/*  71 */   protected ExecutionContext m_ctx = null;
/*     */ 
/*     */   public EditResultPage(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  75 */     this.m_ctx = sys.getExecutionContext();
/*  76 */     this.m_sysInterface = sys;
/*  77 */     this.m_helper = new DialogHelper(sys, title, true);
/*  78 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init(ResultData data)
/*     */   {
/*  84 */     Component nameField = null;
/*  85 */     if (data == null)
/*     */     {
/*  87 */       this.m_data = new ResultData();
/*  88 */       this.m_data.setValues(null);
/*  89 */       nameField = new CustomTextField(30);
/*  90 */       this.m_isNew = true;
/*     */     }
/*     */     else
/*     */     {
/*  94 */       this.m_data = data;
/*  95 */       nameField = new CustomLabel();
/*     */     }
/*     */ 
/*  98 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 105 */           DataResultSet updateList = new DataResultSet();
/* 106 */           DataResultSet drset = SharedObjects.getTable("CurrentVerityTemplates");
/* 107 */           updateList.copyFieldInfo(drset);
/* 108 */           Properties values = EditResultPage.this.m_data.getValues();
/* 109 */           PropParameters params = new PropParameters(values);
/* 110 */           Vector v = updateList.createRow(params);
/* 111 */           updateList.addRow(v);
/*     */ 
/* 113 */           DataBinder binder = new DataBinder();
/* 114 */           binder.addResultSet("ResultPageUpdates", updateList);
/* 115 */           AppLauncher.executeService("UPDATE_RESULT_TEMPLATE", binder);
/*     */         }
/*     */         catch (DataException exp)
/*     */         {
/* 119 */           this.m_errorMessage = LocaleUtils.createMessageListFromThrowable(exp);
/* 120 */           return false;
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 124 */           this.m_errorMessage = LocaleUtils.createMessageListFromThrowable(exp);
/* 125 */           return false;
/*     */         }
/* 127 */         return true;
/*     */       }
/*     */     };
/* 131 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 134 */     JPanel editPanel = new CustomPanel();
/* 135 */     this.m_helper.makePanelGridBag(editPanel, 1);
/* 136 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(15, 15, 5, 15);
/* 137 */     this.m_helper.addLabelFieldPair(editPanel, LocaleResources.getString("apLabelName", this.m_ctx), nameField, "name");
/*     */ 
/* 139 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 15, 15, 15);
/* 140 */     this.m_helper.addLabelFieldPair(editPanel, LocaleResources.getString("apLabelDescription", this.m_ctx), new CustomTextField(50), "description");
/*     */ 
/* 143 */     this.m_resultsPanel = new EditResultCustomPanel(false, this.m_helper);
/* 144 */     this.m_resultsPanel.init(data);
/*     */ 
/* 147 */     mainPanel.setLayout(new BorderLayout());
/* 148 */     mainPanel.add("North", editPanel);
/* 149 */     mainPanel.add("Center", this.m_resultsPanel);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 154 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public ResultData getData()
/*     */   {
/* 159 */     return this.m_data;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 167 */     String name = exchange.m_compName;
/* 168 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 173 */     String name = exchange.m_compName;
/* 174 */     String val = exchange.m_compValue;
/* 175 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 184 */     if (updateComponent)
/*     */     {
/* 186 */       exchange.m_compValue = this.m_data.get(name);
/*     */     }
/*     */     else
/*     */     {
/* 190 */       this.m_data.put(name, exchange.m_compValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 197 */     if (name.equals("name"))
/*     */     {
/* 199 */       int valResult = Validation.checkUrlFileSegment(val);
/* 200 */       IdcMessage errMsg = null;
/* 201 */       switch (valResult)
/*     */       {
/*     */       case 0:
/* 204 */         break;
/*     */       case -1:
/* 206 */         errMsg = IdcMessageFactory.lc("apSpecifyResultTemplateName", new Object[0]);
/* 207 */         break;
/*     */       case -2:
/* 209 */         errMsg = IdcMessageFactory.lc("apSpacesInResultTemplateName", new Object[0]);
/* 210 */         break;
/*     */       default:
/* 212 */         errMsg = IdcMessageFactory.lc("apIllegalCharsInTemplateName", new Object[0]);
/*     */       }
/*     */ 
/* 215 */       if ((errMsg == null) && (this.m_isNew) && (getResultTemplate(val) != null))
/*     */       {
/* 217 */         errMsg = IdcMessageFactory.lc("apResultTemplateAlreadyExists", new Object[0]);
/*     */       }
/* 219 */       if (errMsg != null)
/*     */       {
/* 221 */         exchange.m_errorMessage = errMsg;
/* 222 */         return false;
/*     */       }
/*     */     }
/* 225 */     return true;
/*     */   }
/*     */ 
/*     */   public Vector getResultTemplate(String name)
/*     */   {
/* 230 */     String aName = name;
/* 231 */     ResultSetFilter filter = new ResultSetFilter(aName)
/*     */     {
/*     */       public int checkRow(String val, int curNumRows, Vector row)
/*     */       {
/* 235 */         if (val.equalsIgnoreCase(this.val$aName))
/*     */         {
/* 237 */           return 1;
/*     */         }
/* 239 */         return 0;
/*     */       }
/*     */     };
/* 243 */     DataResultSet dset = SharedObjects.getTable("CurrentVerityTemplates");
/* 244 */     DataResultSet aset = new DataResultSet();
/* 245 */     aset.copyFiltered(dset, "name", filter);
/* 246 */     if (aset.isRowPresent())
/*     */     {
/* 248 */       return aset.getCurrentRowValues();
/*     */     }
/* 250 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 255 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditResultPage
 * JD-Core Version:    0.5.4
 */