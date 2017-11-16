/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.server.utils.SystemPropertiesEditor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ComponentConfigDlg extends CWizardBaseDlg
/*     */ {
/*  48 */   protected Properties m_idcProperties = null;
/*  49 */   protected SystemPropertiesEditor m_propLoader = null;
/*  50 */   protected final String m_epName = "HTMLEditorPath";
/*     */ 
/*     */   public ComponentConfigDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  55 */     super(sys, title, helpPage);
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  61 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/*  68 */           ComponentConfigDlg.this.m_propLoader.mergePropertyValuesEx(ComponentConfigDlg.this.m_idcProperties, null, true);
/*  69 */           ComponentConfigDlg.this.m_propLoader.saveIdc();
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/*  73 */           CWizardGuiUtils.reportError(ComponentConfigDlg.this.m_systemInterface, exp, (IdcMessage)null);
/*  74 */           return false;
/*     */         }
/*     */ 
/*  77 */         return true;
/*     */       }
/*     */ 
/*     */     };
/*     */     try
/*     */     {
/*  83 */       JPanel mainPanel = getDialogHelper().initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/*  86 */       initUI(mainPanel);
/*     */ 
/*  88 */       this.m_propLoader = new SystemPropertiesEditor();
/*  89 */       this.m_propLoader.addKeys("HTMLEditorPath", null);
/*  90 */       this.m_propLoader.initIdc();
/*  91 */       this.m_idcProperties = this.m_propLoader.getIdcProperties();
/*     */ 
/*  93 */       String htmlEditorPath = this.m_idcProperties.getProperty("HTMLEditorPath");
/*  94 */       if ((htmlEditorPath != null) && (htmlEditorPath.length() > 0))
/*     */       {
/*  96 */         this.m_helper.m_props.put("HTMLEditorPath", htmlEditorPath);
/*     */       }
/*     */       else
/*     */       {
/* 101 */         String tempPath = SharedObjects.getEnvironmentValue("HTMLEditorPath");
/* 102 */         if ((tempPath != null) && (tempPath.length() > 0))
/*     */         {
/* 104 */           this.m_helper.m_props.put("HTMLEditorPath", tempPath);
/* 105 */           this.m_idcProperties.put("HTMLEditorPath", tempPath);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 111 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel mainPanel)
/*     */   {
/* 118 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 119 */     this.m_helper.addComponent(mainPanel, new CustomLabel(LocaleResources.getString("csCompWizLabelHTMLEditorPath", null), 1));
/*     */ 
/* 121 */     this.m_helper.addFilePathComponent(mainPanel, 40, LocaleResources.getString("csCompWizLabelHTMLEditorPath", null), "HTMLEditorPath");
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 132 */     String name = exchange.m_compName;
/* 133 */     String val = exchange.m_compValue;
/*     */ 
/* 135 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 141 */     IdcMessage errMsg = null;
/* 142 */     boolean isEp = name.equals("HTMLEditorPath");
/* 143 */     if (isEp)
/*     */     {
/*     */       try
/*     */       {
/* 147 */         IdcMessage tempErrMsg = IdcMessageFactory.lc("csCompWizHTMLEditorPathEmpty", new Object[0]);
/* 148 */         if ((val == null) || (val.length() == 0))
/*     */         {
/* 150 */           throw new ServiceException(null, tempErrMsg);
/*     */         }
/*     */ 
/* 154 */         int index = val.indexOf("/");
/* 155 */         if (index < 0)
/*     */         {
/* 157 */           index = val.indexOf("\\");
/*     */         }
/* 159 */         if (index > 0)
/*     */         {
/* 162 */           tempErrMsg = IdcMessageFactory.lc("csCompWizHTMLEditorPathError", new Object[0]);
/*     */ 
/* 164 */           FileUtils.validatePath(val, tempErrMsg, 1);
/*     */         }
/*     */ 
/* 167 */         this.m_idcProperties.put(name, val);
/* 168 */         SharedObjects.putEnvironmentValue(name, val);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 172 */         errMsg = LocaleUtils.createMessageListFromThrowable(e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 177 */     if (errMsg != null)
/*     */     {
/* 179 */       exchange.m_errorMessage = errMsg;
/* 180 */       return false;
/*     */     }
/* 182 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 187 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.ComponentConfigDlg
 * JD-Core Version:    0.5.4
 */