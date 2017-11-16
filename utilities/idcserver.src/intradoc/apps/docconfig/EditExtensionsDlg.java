/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditExtensionsDlg
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_action;
/*  62 */   protected DataBinder m_binder = null;
/*     */   protected String m_helpPage;
/*  64 */   protected ComponentValidator m_cmpValidator = null;
/*  65 */   protected ExecutionContext m_ctx = null;
/*     */   protected Properties m_itemProps;
/*     */ 
/*     */   public EditExtensionsDlg(SystemInterface sys, String title, ResultSet rset, String helpPage)
/*     */   {
/*  70 */     this.m_helper = new DialogHelper(sys, title, true);
/*  71 */     this.m_ctx = sys.getExecutionContext();
/*     */ 
/*  73 */     this.m_systemInterface = sys;
/*  74 */     this.m_helpPage = helpPage;
/*  75 */     this.m_cmpValidator = new ComponentValidator(rset);
/*     */   }
/*     */ 
/*     */   public void init(Properties data, DataResultSet formats, int formatIndex)
/*     */   {
/*  80 */     this.m_itemProps = data;
/*     */     Component extensionTxt;
/*     */     Component extensionTxt;
/*  82 */     if (data != null)
/*     */     {
/*  84 */       extensionTxt = new CustomLabel(data.getProperty("dExtension"));
/*     */     }
/*     */     else
/*     */     {
/*  88 */       extensionTxt = new CustomTextField(20);
/*     */     }
/*     */ 
/*  92 */     JComboBox formatChoice = new CustomChoice();
/*  93 */     int size = formats.getNumRows();
/*  94 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  96 */       Vector row = formats.getRowValues(i);
/*  97 */       String str = (String)row.elementAt(formatIndex);
/*  98 */       formatChoice.addItem(str);
/*     */     }
/*     */ 
/* 101 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 108 */           Properties localData = this.m_dlgHelper.m_props;
/* 109 */           EditExtensionsDlg.this.m_binder = new DataBinder();
/* 110 */           EditExtensionsDlg.this.m_binder.setLocalData(localData);
/* 111 */           if (EditExtensionsDlg.this.m_itemProps != null)
/*     */           {
/* 113 */             String isEnabledStr = EditExtensionsDlg.this.m_itemProps.getProperty("dIsEnabled");
/* 114 */             if ((isEnabledStr == null) || (isEnabledStr.length() == 0))
/*     */             {
/* 116 */               isEnabledStr = "0";
/*     */             }
/*     */             else
/*     */             {
/* 123 */               boolean isEnabled = StringUtils.convertToBool(isEnabledStr, false);
/* 124 */               isEnabledStr = (isEnabled) ? "1" : "0";
/*     */             }
/* 126 */             EditExtensionsDlg.this.m_binder.putLocal("dIsEnabled", isEnabledStr);
/*     */           }
/*     */           else
/*     */           {
/* 130 */             EditExtensionsDlg.this.m_binder.putLocal("dIsEnabled", "1");
/*     */           }
/* 132 */           AppLauncher.executeService(EditExtensionsDlg.this.m_action, EditExtensionsDlg.this.m_binder);
/* 133 */           return true;
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 137 */           MessageBox.reportError(EditExtensionsDlg.this.m_systemInterface, exp);
/*     */         }
/* 139 */         return false;
/*     */       }
/*     */     };
/* 142 */     okCallback.m_dlgHelper = this.m_helper;
/* 143 */     JPanel wrapper = this.m_helper.initStandard(this, okCallback, 2, true, this.m_helpPage);
/*     */ 
/* 145 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 20, 10, 20);
/*     */ 
/* 147 */     SystemInterface sys = this.m_systemInterface;
/* 148 */     JPanel mainPanel = new PanePanel();
/* 149 */     this.m_helper.addComponent(wrapper, mainPanel);
/* 150 */     this.m_helper.makePanelGridBag(mainPanel, 2);
/*     */ 
/* 152 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 0, 0, 0);
/* 153 */     this.m_helper.addLabelFieldPair(mainPanel, sys.getString("apLabelExtension"), extensionTxt, "dExtension");
/*     */ 
/* 156 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 0, 0, 0);
/* 157 */     this.m_helper.addLabelFieldPair(mainPanel, sys.getString("apLabelMapToFormat"), formatChoice, "dFormat");
/*     */ 
/* 160 */     String overrideStatus = null;
/*     */ 
/* 163 */     boolean isAdd = true;
/* 164 */     if (data != null)
/*     */     {
/* 166 */       formatChoice.setSelectedItem(data.getProperty("dFormat"));
/* 167 */       this.m_helper.retrieveComponentValues();
/* 168 */       overrideStatus = data.getProperty("overrideStatus");
/* 169 */       isAdd = StringUtils.convertToBool(data.getProperty("isAdd"), false);
/*     */     }
/*     */ 
/* 174 */     if ((overrideStatus == null) || (overrideStatus.length() == 0) || (isAdd))
/*     */     {
/* 176 */       this.m_action = "ADD_DOCEXTENSION";
/*     */     }
/*     */     else
/*     */     {
/* 180 */       this.m_action = "EDIT_DOCEXTENSION";
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 186 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 196 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 197 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 202 */     String name = exchange.m_compName;
/* 203 */     String val = exchange.m_compValue;
/*     */ 
/* 205 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 207 */     if (name.equals("dExtension"))
/*     */     {
/* 209 */       int valResult = Validation.checkUrlFileSegment(val);
/* 210 */       IdcMessage errMsg = null;
/* 211 */       switch (valResult)
/*     */       {
/*     */       case 0:
/* 214 */         break;
/*     */       case -1:
/* 216 */         errMsg = IdcMessageFactory.lc("apSpecifyFileExtensionName", new Object[0]);
/* 217 */         break;
/*     */       case -2:
/* 219 */         errMsg = IdcMessageFactory.lc("apNoSpacesInFileExtensionName", new Object[0]);
/*     */       }
/*     */ 
/* 222 */       if (val.length() > maxLength)
/*     */       {
/* 224 */         errMsg = IdcMessageFactory.lc("apFileExtensionNameExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/* 226 */       if (errMsg != null)
/*     */       {
/* 228 */         exchange.m_errorMessage = errMsg;
/* 229 */         return false;
/*     */       }
/*     */     }
/* 232 */     return true;
/*     */   }
/*     */ 
/*     */   public String getExtension()
/*     */   {
/* 237 */     return this.m_helper.m_props.getProperty("dExtension");
/*     */   }
/*     */ 
/*     */   public DataBinder getBinder()
/*     */   {
/* 242 */     return this.m_binder;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 247 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 88236 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditExtensionsDlg
 * JD-Core Version:    0.5.4
 */