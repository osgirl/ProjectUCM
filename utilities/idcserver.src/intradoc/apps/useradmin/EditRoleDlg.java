/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditRoleDlg
/*     */   implements ComponentBinder, SharedContext
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_ctx;
/*     */   protected ComponentValidator m_cmpValidator;
/*     */   protected String m_action;
/*     */   protected DataBinder m_binder;
/*     */   protected String m_helpPage;
/*     */ 
/*     */   public EditRoleDlg(SystemInterface sys, String title, ResultSet rset, String helpPage)
/*     */   {
/*  60 */     this.m_helper = new DialogHelper(sys, title, true);
/*  61 */     this.m_systemInterface = sys;
/*  62 */     this.m_ctx = sys.getExecutionContext();
/*  63 */     this.m_helpPage = helpPage;
/*  64 */     this.m_cmpValidator = new ComponentValidator(rset);
/*     */   }
/*     */ 
/*     */   public void init(Properties data)
/*     */   {
/*  69 */     this.m_helper.m_props = data;
/*     */ 
/*  71 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/*  78 */           EditRoleDlg.this.m_binder = new DataBinder(true);
/*  79 */           EditRoleDlg.this.m_binder.setLocalData(this.m_dlgHelper.m_props);
/*  80 */           EditRoleDlg.this.executeService("EDIT_ROLE_DISPLAY_NAME", EditRoleDlg.this.m_binder, false);
/*  81 */           return true;
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/*  85 */           this.m_errorMessage = IdcMessageFactory.lc(exp);
/*  86 */         }return false;
/*     */       }
/*     */     };
/*  90 */     okCallback.m_dlgHelper = this.m_helper;
/*     */ 
/*  92 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 2, true, this.m_helpPage);
/*     */ 
/*  94 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelRoleName", this.m_ctx), new CustomLabel(), "dRoleName");
/*     */ 
/*  96 */     this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelRoleDisplayName", this.m_ctx), new CustomTextField(30), "dRoleDisplayName");
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 102 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public String getAlias()
/*     */   {
/* 107 */     return this.m_helper.m_props.getProperty("dAlias");
/*     */   }
/*     */ 
/*     */   public DataBinder getBinder()
/*     */   {
/* 112 */     return this.m_binder;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 125 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 126 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 131 */     String name = exchange.m_compName;
/* 132 */     String val = exchange.m_compValue;
/*     */ 
/* 134 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 136 */     IdcMessage errMsg = null;
/* 137 */     if ((name.equals("dRoleDisplayName")) && (val != null) && (val.length() > maxLength))
/*     */     {
/* 139 */       errMsg = IdcMessageFactory.lc("apRoleDisplayNameExceedsMaxLength", new Object[] { val, Integer.valueOf(maxLength) });
/*     */     }
/*     */ 
/* 142 */     if (errMsg != null)
/*     */     {
/* 144 */       exchange.m_errorMessage = errMsg;
/* 145 */       return false;
/*     */     }
/* 147 */     return true;
/*     */   }
/*     */ 
/*     */   public void executeService(String action, DataBinder data, boolean isRefreshList)
/*     */     throws ServiceException
/*     */   {
/* 156 */     AppLauncher.executeService(action, data);
/*     */   }
/*     */ 
/*     */   public UserData getUserData()
/*     */   {
/* 161 */     return AppLauncher.getUserData();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 166 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditRoleDlg
 * JD-Core Version:    0.5.4
 */