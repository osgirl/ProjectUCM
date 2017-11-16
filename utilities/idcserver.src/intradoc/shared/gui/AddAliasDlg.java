/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddAliasDlg
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_helpPage;
/*     */   protected ExecutionContext m_cxt;
/*  56 */   protected UdlPanel m_aliasList = null;
/*  57 */   protected boolean m_isMultiMode = true;
/*  58 */   protected DataResultSet m_excludedSet = null;
/*     */ 
/*     */   public AddAliasDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  62 */     this.m_helper = new DialogHelper(sys, title, true);
/*  63 */     this.m_systemInterface = sys;
/*  64 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/*  65 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public boolean init(DialogCallback okCallback, Properties props, DataResultSet excludedSet)
/*     */   {
/*  70 */     this.m_helper.m_props = props;
/*  71 */     return init(okCallback, excludedSet);
/*     */   }
/*     */ 
/*     */   public boolean init(boolean isMulti)
/*     */   {
/*  76 */     this.m_isMultiMode = isMulti;
/*  77 */     return init(null, null);
/*     */   }
/*     */ 
/*     */   public boolean init(DialogCallback okCallback, DataResultSet excludedSet)
/*     */   {
/*  82 */     this.m_excludedSet = excludedSet;
/*     */ 
/*  84 */     if (okCallback == null)
/*     */     {
/*  86 */       okCallback = new DialogCallback()
/*     */       {
/*     */         public boolean handleDialogEvent(ActionEvent e)
/*     */         {
/*  91 */           Properties localData = AddAliasDlg.this.m_helper.m_props;
/*  92 */           String alias = localData.getProperty("aliases");
/*  93 */           if ((alias == null) || (alias.length() == 0))
/*     */           {
/*  96 */             MessageBox.reportError(AddAliasDlg.this.m_systemInterface, null, IdcMessageFactory.lc("apSelectAnAlias", new Object[0]));
/*  97 */             return false;
/*     */           }
/*  99 */           return true;
/*     */         }
/*     */       };
/*     */     }
/*     */ 
/* 104 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 2, true, this.m_helpPage);
/*     */ 
/* 106 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 20, 10, 20);
/* 107 */     initUI(mainPanel);
/*     */ 
/* 109 */     refreshList();
/* 110 */     return true;
/*     */   }
/*     */ 
/*     */   protected void initUI(JPanel mainPanel)
/*     */   {
/* 116 */     this.m_aliasList = new UdlPanel(LocaleResources.getString("apAliasesTitle", this.m_cxt), null, 250, 15, "Alias", true);
/*     */ 
/* 118 */     this.m_aliasList.init();
/* 119 */     this.m_aliasList.setMultipleMode(this.m_isMultiMode);
/*     */ 
/* 121 */     this.m_aliasList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelAlias", this.m_cxt), "dAlias", 100.0D));
/*     */ 
/* 124 */     this.m_aliasList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apLabelAliasDisplayName", this.m_cxt), "dAliasDisplayName", 100.0D));
/*     */ 
/* 127 */     this.m_aliasList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleDescription", this.m_cxt), "dAliasDescription", 150.0D));
/*     */ 
/* 130 */     this.m_aliasList.setVisibleColumns("dAlias,dAliasDisplayName,dAliasDescription");
/*     */ 
/* 132 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 133 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 134 */     this.m_helper.addExchangeComponent(mainPanel, this.m_aliasList, "aliases");
/* 135 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/*     */   }
/*     */ 
/*     */   protected void refreshList()
/*     */   {
/* 140 */     DataResultSet dset = SharedObjects.getTable("Alias");
/* 141 */     this.m_aliasList.refreshListEx(dset, null);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 146 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public String[] getSelected()
/*     */   {
/* 151 */     return this.m_aliasList.getSelectedObjs();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 159 */     if (updateComponent)
/*     */       return;
/* 161 */     String[] selObjs = this.m_aliasList.getSelectedObjs();
/* 162 */     Vector sel = new IdcVector();
/* 163 */     Properties props = this.m_helper.m_props;
/* 164 */     if (selObjs == null)
/*     */       return;
/* 166 */     for (int i = 0; i < selObjs.length; ++i)
/*     */     {
/* 168 */       sel.addElement(selObjs[i]);
/*     */     }
/* 170 */     props.put("aliases", StringUtils.createString(sel, '\t', '^'));
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 177 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 182 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80283 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.AddAliasDlg
 * JD-Core Version:    0.5.4
 */