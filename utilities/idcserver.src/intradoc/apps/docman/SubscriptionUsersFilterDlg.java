/*     */ package intradoc.apps.docman;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.FilterData;
/*     */ import intradoc.shared.gui.FilterDlg;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SubscriptionUsersFilterDlg extends FilterDlg
/*     */ {
/*  40 */   public String[][] FIELD_INFO = { { "dSubscriptionType", "apSubscriptionTypeColumnLabel", "subscriptionTypes", "text", "", "SubscriptionPanel" }, { "dSubscriptionAliasType", "apSubscriptionAliasTypeColumnLabel", "subscriptionAliasTypes", "text", "", "UserPanel" }, { "dSubscriptionAlias", "apSubscriptionUserAliasColumnLabel", "", "text", "", "UserPanel" }, { "dSubscriptionCreateDate", "apSubscriptionCreateDateLabel2", "", "date", "", "DatePanel" }, { "dSubscriptionNotifyDate", "apSubscriptionNotifyDateLabel2", "", "date", "", "DatePanel" }, { "dSubscriptionUsedDate", "apSubscriptionUsedDateLabel2", "", "date", "", "DatePanel" } };
/*     */ 
/*  51 */   public String[][] PANEL_INFO = { { "SubscriptionPanel", "apSubscriptionTypeFilterTitle" }, { "UserPanel", "apSubscriptionUserAliasFilterTitle" }, { "DatePanel", "apSubscriptionDateFilterTitle" } };
/*     */ 
/*     */   public SubscriptionUsersFilterDlg(SystemInterface sys, String title, String helpPage, SharedContext shContext)
/*     */   {
/*  61 */     super(sys, title, helpPage, shContext);
/*  62 */     this.m_isSplitFields = false;
/*     */   }
/*     */ 
/*     */   public void init(Hashtable filterData, Properties props)
/*     */   {
/*  68 */     this.m_filterData = filterData;
/*  69 */     this.m_helper.m_props = props;
/*     */ 
/*  71 */     JPanel mainPanel = this.m_helper.initStandard(this, null, 2, this.m_helpPage != null, this.m_helpPage);
/*     */ 
/*  74 */     LocaleResources.localizeDoubleArray(this.FIELD_INFO, this.m_cxt, 1);
/*  75 */     LocaleResources.localizeDoubleArray(this.PANEL_INFO, this.m_cxt, 1);
/*     */ 
/*  78 */     this.m_fields = new ViewFields(this.m_cxt);
/*  79 */     this.m_fields.addFlags(this.FIELD_INFO);
/*     */ 
/*  81 */     createFilter(mainPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel createDatePanel(JPanel mainPanel, String title)
/*     */   {
/*  86 */     JPanel pnl = createTitledPanel(mainPanel, title, false);
/*     */ 
/*  88 */     this.m_helper.addComponent(pnl, new CustomLabel(getString("apSubscriptionDateFilterFromLabel")));
/*  89 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/*  90 */     this.m_helper.addComponent(pnl, new CustomLabel(getString("apSubscriptionDateFilterToLabel")));
/*     */ 
/*  92 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected String getPanelName(FilterData data, int index)
/*     */   {
/*  98 */     return this.FIELD_INFO[index][5];
/*     */   }
/*     */ 
/*     */   protected JPanel createPanel(String name, JPanel mainPanel)
/*     */   {
/* 104 */     JPanel pnl = null;
/* 105 */     if (name.equals("DatePanel"))
/*     */     {
/* 107 */       pnl = createDatePanel(mainPanel, this.PANEL_INFO[2][1]);
/*     */     }
/*     */     else
/*     */     {
/* 111 */       pnl = createTitledPanel(mainPanel, this.PANEL_INFO[0][1], true);
/*     */     }
/* 113 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected String[][] getDisplayMap(String key)
/*     */   {
/* 119 */     String[][] display = (String[][])null;
/* 120 */     if (key.equals("subscriptionAliasTypes"))
/*     */     {
/* 122 */       display = new String[][] { { "user", getString("apLabelUser") }, { "alias", getString("apLabelAlias") } };
/*     */     }
/*     */     else
/*     */     {
/* 127 */       display = super.getDisplayMap(key);
/*     */     }
/* 129 */     return display;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 134 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.SubscriptionUsersFilterDlg
 * JD-Core Version:    0.5.4
 */