/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddJumpDlg
/*     */   implements ComponentBinder
/*     */ {
/*  57 */   protected SystemInterface m_systemInterface = null;
/*  58 */   protected ExecutionContext m_cxt = null;
/*  59 */   protected DialogHelper m_helper = null;
/*  60 */   protected String m_helpPage = null;
/*     */ 
/*  62 */   protected TabPanel m_tabs = null;
/*     */ 
/*  65 */   protected final String[][] PANEL_INFOS = { { "JumpSideEffectsPanel", "intradoc.apps.workflow.JumpSideEffectsPanel", "apTitleSideEffects" }, { "JumpMessagePanel", "intradoc.apps.workflow.JumpMessagePanel", "apTitleMessage" } };
/*     */ 
/*     */   public AddJumpDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  73 */     this.m_systemInterface = sys;
/*  74 */     this.m_cxt = sys.getExecutionContext();
/*  75 */     this.m_helper = new DialogHelper(sys, title, true);
/*  76 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public int init(Properties props, DataResultSet jumpSet, boolean isNew)
/*     */   {
/*  81 */     if (isNew)
/*     */     {
/*  83 */       this.m_helper.m_props = props;
/*     */     }
/*     */     else
/*     */     {
/*  87 */       this.m_helper.m_props = ((Properties)props.clone());
/*     */     }
/*     */ 
/*  90 */     DataResultSet jSet = jumpSet;
/*  91 */     boolean isNewJump = isNew;
/*  92 */     DialogCallback okCallback = new DialogCallback(isNewJump, jSet)
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*  97 */         if (!AddJumpDlg.this.m_tabs.validateAllPanes())
/*     */         {
/*  99 */           return false;
/*     */         }
/*     */ 
/* 102 */         if (this.val$isNewJump)
/*     */         {
/* 105 */           String name = AddJumpDlg.this.m_helper.m_props.getProperty("wfJumpName");
/* 106 */           Vector row = this.val$jSet.findRow(0, name);
/* 107 */           if (row != null)
/*     */           {
/* 109 */             this.m_errorMessage = IdcMessageFactory.lc(IdcMessageFactory.lc("apJumpNameNotUnique", new Object[0]), "apUnableToAddJump", new Object[0]);
/* 110 */             return false;
/*     */           }
/*     */         }
/* 113 */         return true;
/*     */       }
/*     */     };
/* 117 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, DialogHelpTable.getHelpPage(this.m_helpPage));
/*     */ 
/* 120 */     if (initUI(mainPanel, isNew))
/*     */     {
/* 122 */       return this.m_helper.prompt();
/*     */     }
/* 124 */     return 0;
/*     */   }
/*     */ 
/*     */   public boolean initUI(JPanel mainPanel, boolean isNew)
/*     */   {
/* 129 */     Component nameField = null;
/* 130 */     if (isNew)
/*     */     {
/* 132 */       nameField = new CustomTextField(20);
/*     */     }
/*     */     else
/*     */     {
/* 136 */       CustomLabel nameLabel = new CustomLabel();
/* 137 */       nameLabel.setMinWidth(20);
/* 138 */       nameField = nameLabel;
/*     */     }
/* 140 */     this.m_helper.addLabelFieldPairEx(mainPanel, LocaleResources.getString("apLabelJumpName", this.m_cxt), nameField, "wfJumpName", false);
/*     */ 
/* 144 */     CustomLabel filler = new CustomLabel();
/* 145 */     filler.setMinWidth(20);
/* 146 */     this.m_helper.addLastComponentInRow(mainPanel, filler);
/* 147 */     this.m_helper.m_gridHelper.addEmptyRow(mainPanel);
/*     */ 
/* 150 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 151 */     JPanel top = new PanePanel();
/* 152 */     gh.prepareAddLastRowElement();
/* 153 */     gh.m_gc.weighty = 1.0D;
/* 154 */     this.m_helper.addComponent(mainPanel, top);
/*     */ 
/* 156 */     boolean result = true;
/*     */     try
/*     */     {
/* 159 */       initTabs(top);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 163 */       result = false;
/*     */     }
/* 165 */     return result;
/*     */   }
/*     */ 
/*     */   protected void initTabs(JPanel top) throws ServiceException
/*     */   {
/* 170 */     this.m_tabs = new TabPanel();
/* 171 */     for (int i = 0; i < this.PANEL_INFOS.length; ++i)
/*     */     {
/* 173 */       JumpBasePanel editPanel = (JumpBasePanel)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_cxt, this.PANEL_INFOS[i][0]));
/*     */ 
/* 178 */       editPanel.init(this.m_helper);
/* 179 */       this.m_tabs.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], this.m_cxt), editPanel, editPanel, false);
/*     */     }
/*     */ 
/* 183 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 184 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 185 */     this.m_helper.addComponent(top, this.m_tabs);
/*     */   }
/*     */ 
/*     */   public Properties getProperties()
/*     */   {
/* 192 */     Properties props = this.m_helper.m_props;
/* 193 */     boolean hasReturn = StringUtils.convertToBool(props.getProperty("HasReturnStep"), false);
/*     */ 
/* 195 */     if (!hasReturn)
/*     */     {
/* 197 */       props.put("wfJumpReturnStep", "");
/*     */     }
/* 199 */     return props;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 207 */     String name = exchange.m_compName;
/* 208 */     exchangeField(name, exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 213 */     String name = exchange.m_compName;
/* 214 */     String val = exchange.m_compValue;
/*     */ 
/* 216 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 222 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 227 */     IdcMessage errMsg = null;
/* 228 */     if (name.equals("wfJumpName"))
/*     */     {
/* 230 */       errMsg = Validation.checkFormFieldForDB(val, "apTitleJumpName", 0, null);
/*     */     }
/* 232 */     if (errMsg != null)
/*     */     {
/* 234 */       exchange.m_errorMessage = errMsg;
/* 235 */       return false;
/*     */     }
/* 237 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 242 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.AddJumpDlg
 * JD-Core Version:    0.5.4
 */