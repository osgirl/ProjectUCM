/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.util.Properties;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JCheckBox;
/*     */ 
/*     */ public class SelectOptionsPanel extends DocConfigPanel
/*     */ {
/*     */   public void initEx(SystemInterface sys, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  43 */     super.initEx(sys, binder);
/*     */ 
/*  45 */     initUI();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  50 */     this.m_helper.makePanelGridBag(this, 1);
/*     */ 
/*  52 */     initOptions();
/*     */   }
/*     */ 
/*     */   protected void initOptions()
/*     */   {
/*  58 */     boolean isChildView = StringUtils.convertToBool(this.m_helper.m_props.getProperty("isChildView"), false);
/*     */ 
/*  60 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*     */ 
/*  62 */     String msg = LocaleResources.getString("apSchViewWizardDescription", this.m_ctx);
/*  63 */     CustomText ct = new CustomText(msg, 100);
/*  64 */     this.m_helper.addComponent(this, ct);
/*     */ 
/*  66 */     Insets insets = this.m_helper.m_gridHelper.m_gc.insets;
/*  67 */     insets.left += 25;
/*     */ 
/*  69 */     ButtonGroup grp = new ButtonGroup();
/*  70 */     String label = LocaleResources.getString("apSchUseTable", this.m_ctx);
/*  71 */     JCheckBox box = new CustomCheckbox(label, grp, true);
/*  72 */     this.m_helper.addExchangeComponent(this, box, "IsTableView");
/*  73 */     box.setEnabled(!isChildView);
/*     */ 
/*  75 */     label = LocaleResources.getString("apSchUseOptionList", this.m_ctx);
/*  76 */     box = new CustomCheckbox(label, grp, false);
/*  77 */     this.m_helper.addExchangeComponent(this, box, "IsOptionListView");
/*  78 */     box.setEnabled(!isChildView);
/*     */   }
/*     */ 
/*     */   public IdcMessage retrievePanelValuesAndValidate()
/*     */   {
/*  84 */     this.m_helper.retrieveComponentValues();
/*     */ 
/*  87 */     String viewType = "table";
/*  88 */     boolean isOptionList = StringUtils.convertToBool(this.m_helper.m_props.getProperty("IsOptionListView"), false);
/*  89 */     if (isOptionList)
/*     */     {
/*  91 */       viewType = "optionList";
/*     */     }
/*  93 */     this.m_helper.m_props.put("schViewType", viewType);
/*  94 */     return null;
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/* 100 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 105 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80220 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.SelectOptionsPanel
 * JD-Core Version:    0.5.4
 */