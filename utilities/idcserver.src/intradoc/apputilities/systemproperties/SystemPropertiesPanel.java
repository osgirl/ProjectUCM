/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SystemPropertiesPanel extends PanePanel
/*     */   implements ComponentBinder, PromptHandler
/*     */ {
/*     */   protected ContainerHelper m_helper;
/*     */   protected SystemInterface m_sysInterface;
/*     */   protected Properties m_cfgProperties;
/*     */   protected boolean m_isRefinery;
/*     */ 
/*     */   public SystemPropertiesPanel()
/*     */   {
/*  51 */     this.m_cfgProperties = null;
/*     */   }
/*     */ 
/*     */   public void init(Properties props, SystemInterface sys, boolean isRefinery)
/*     */     throws ServiceException
/*     */   {
/*  59 */     this.m_helper = new ContainerHelper();
/*  60 */     this.m_helper.attachToContainer(this, sys, props);
/*  61 */     this.m_helper.m_componentBinder = this;
/*  62 */     this.m_sysInterface = sys;
/*  63 */     this.m_isRefinery = isRefinery;
/*     */ 
/*  66 */     initUI();
/*     */ 
/*  69 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public void init(Properties props, SystemInterface sys, Properties cfgProps, boolean isRefinery)
/*     */     throws ServiceException
/*     */   {
/*  75 */     this.m_cfgProperties = cfgProps;
/*  76 */     init(props, sys, isRefinery);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   protected void reportError(IdcMessage msg)
/*     */   {
/*  89 */     MessageBox.reportError(this.m_sysInterface, msg);
/*     */   }
/*     */ 
/*     */   protected JPanel addNewSubPanel(JPanel mainPanel, int anchor)
/*     */   {
/*  98 */     CustomPanel panel = new CustomPanel();
/*  99 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*     */ 
/* 101 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 102 */     this.m_helper.addComponent(mainPanel, panel);
/* 103 */     this.m_helper.makePanelGridBag(panel, anchor);
/*     */ 
/* 105 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void checkboxDescript(JPanel panel, String text)
/*     */   {
/* 110 */     GridBagConstraints gbc = this.m_helper.m_gridHelper.m_gc;
/* 111 */     int gbcLeft = gbc.insets.left;
/* 112 */     int anchor = gbc.anchor;
/* 113 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 114 */     gbc.insets.left = 20;
/* 115 */     gbc.anchor = 11;
/* 116 */     this.m_helper.addComponent(panel, new CustomText(LocaleResources.localizeMessage(text, null), 100));
/* 117 */     gbc.insets.left = gbcLeft;
/* 118 */     gbc.anchor = anchor;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 127 */     this.m_helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 132 */     return true;
/*     */   }
/*     */ 
/*     */   public String validateField(String prefix, String val)
/*     */   {
/* 137 */     int valResult = Validation.checkUrlFileSegment(val);
/* 138 */     String errMsg = null;
/* 139 */     switch (valResult)
/*     */     {
/*     */     case 0:
/* 142 */       break;
/*     */     case -1:
/* 144 */       errMsg = LocaleUtils.encodeMessage("csSysPropsPanelValidateFieldEmpty", null, prefix);
/* 145 */       break;
/*     */     case -2:
/* 147 */       errMsg = LocaleUtils.encodeMessage("csSysPropsPanelValidateFieldSpaces", null, prefix);
/* 148 */       break;
/*     */     default:
/* 150 */       errMsg = LocaleUtils.encodeMessage("csSysPropsPanelValidateFieldIllegalChars", null, prefix);
/*     */     }
/*     */ 
/* 154 */     return errMsg;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String validatePath(String path, boolean shouldBeFile, boolean needWriteAccess)
/*     */   {
/* 161 */     int flags = ((shouldBeFile) ? 1 : 0) | ((needWriteAccess) ? 2 : 0);
/*     */ 
/* 163 */     IdcMessage msg = validatePath(path, flags);
/* 164 */     return LocaleUtils.encodeMessage(msg);
/*     */   }
/*     */ 
/*     */   public IdcMessage validatePath(String path, int flags)
/*     */   {
/* 169 */     boolean shouldBeFile = (flags & 0x1) != 0;
/* 170 */     boolean needWriteAccess = (flags & 0x2) != 0;
/* 171 */     IdcMessage errMsg = null;
/*     */ 
/* 173 */     int retVal = FileUtils.checkFile(path, shouldBeFile, needWriteAccess);
/*     */ 
/* 175 */     if (retVal != 0)
/*     */     {
/* 177 */       errMsg = FileUtils.getErrorMsg(path, flags, retVal);
/*     */     }
/*     */ 
/* 180 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public void saveChanges()
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 197 */     if (this.m_helper.retrieveComponentValues())
/*     */     {
/* 199 */       return 1;
/*     */     }
/*     */ 
/* 202 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 207 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.SystemPropertiesPanel
 * JD-Core Version:    0.5.4
 */