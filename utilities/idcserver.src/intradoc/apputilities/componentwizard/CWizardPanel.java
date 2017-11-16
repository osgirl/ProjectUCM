/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public abstract class CWizardPanel extends PanePanel
/*     */ {
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ContainerHelper m_helper;
/*     */   protected IntradocComponent m_component;
/*     */ 
/*     */   public CWizardPanel()
/*     */   {
/*  35 */     this.m_systemInterface = null;
/*  36 */     this.m_helper = null;
/*  37 */     this.m_component = null;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys, IntradocComponent comp) throws ServiceException {
/*  41 */     this.m_systemInterface = sys;
/*  42 */     this.m_component = comp;
/*  43 */     this.m_helper = new ContainerHelper();
/*  44 */     this.m_helper.attachToContainer(this, sys, null);
/*     */ 
/*  46 */     initUI();
/*     */   }
/*     */ 
/*     */   public abstract void initUI()
/*     */     throws ServiceException;
/*     */ 
/*     */   @Deprecated
/*     */   public String assignComponentInfo(IntradocComponent comp, boolean reloadAll)
/*     */   {
/*  55 */     IdcMessage msg = assignComponentInfo(comp, reloadAll, null);
/*  56 */     if (msg != null)
/*     */     {
/*  58 */       return LocaleUtils.encodeMessage(msg);
/*     */     }
/*  60 */     return null;
/*     */   }
/*     */ 
/*     */   public IdcMessage assignComponentInfo(IntradocComponent comp, boolean reloadAll, Map options)
/*     */   {
/*  65 */     IdcMessage errMsg = null;
/*  66 */     this.m_component = comp;
/*  67 */     this.m_helper.m_props = new Properties();
/*  68 */     this.m_helper.loadComponentValues();
/*     */ 
/*  70 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public UdlPanel createUdlPanel(String listTitle, int width, int height, String resultSetName, boolean setVisibleColumns, String[][] columnMap, String idColumn, boolean forceVertical)
/*     */   {
/*  76 */     return CWizardGuiUtils.createUdlPanel(listTitle, width, height, resultSetName, setVisibleColumns, columnMap, idColumn, forceVertical);
/*     */   }
/*     */ 
/*     */   protected IdcMessage checkField(String val, IdcMessage defaultMessage, boolean spaceAllowed, boolean specialCharAllowed)
/*     */   {
/*  82 */     IdcMessage errMsg = null;
/*  83 */     int retVal = Validation.checkUrlFileSegment(val);
/*     */ 
/*  85 */     if (retVal != 0)
/*     */     {
/*  87 */       if (retVal == -1)
/*     */       {
/*  89 */         errMsg = IdcMessageFactory.lc("csCompWizFieldReq", new Object[] { defaultMessage });
/*     */       }
/*  91 */       else if ((retVal == -2) && (!spaceAllowed))
/*     */       {
/*  93 */         errMsg = IdcMessageFactory.lc("csCompWizFieldHasSpaces", new Object[] { defaultMessage });
/*     */       }
/*  95 */       else if (!specialCharAllowed)
/*     */       {
/*  97 */         errMsg = IdcMessageFactory.lc("csCompWizFieldHasInvalidChars", new Object[] { defaultMessage });
/*     */       }
/*     */     }
/*     */ 
/* 101 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 106 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.CWizardPanel
 * JD-Core Version:    0.5.4
 */