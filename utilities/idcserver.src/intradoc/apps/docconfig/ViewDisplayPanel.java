/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ViewDisplayPanel extends DocConfigPanel
/*     */   implements ActionListener
/*     */ {
/*     */   protected UdlPanel m_localeList;
/*     */   protected DataBinder m_viewData;
/*     */ 
/*     */   public ViewDisplayPanel()
/*     */   {
/*  52 */     this.m_localeList = null;
/*  53 */     this.m_viewData = null;
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder) throws ServiceException
/*     */   {
/*  58 */     super.initEx(sys, binder);
/*  59 */     this.m_viewData = binder;
/*     */ 
/*  61 */     JPanel panel = initUI();
/*  62 */     this.m_helper.makePanelGridBag(this, 1);
/*     */ 
/*  64 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  65 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  66 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*  67 */     this.m_helper.addComponent(this, panel);
/*     */ 
/*  69 */     this.m_localeList.refreshList(binder, null);
/*     */   }
/*     */ 
/*     */   protected JPanel initUI()
/*     */   {
/*  74 */     JPanel pnl = new PanePanel();
/*  75 */     this.m_helper.makePanelGridBag(pnl, 2);
/*     */ 
/*  78 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/*  79 */     String msg = LocaleResources.getString("apSchDisplayDescription", this.m_ctx);
/*  80 */     this.m_helper.addLastComponentInRow(pnl, new CustomText(msg, 100));
/*     */ 
/*  82 */     this.m_localeList = new UdlPanel(null, null, 200, 10, "ViewLocales", true);
/*     */ 
/*  85 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("apTitleLocale", this.m_ctx), "schLocale", 7.0D);
/*  86 */     this.m_localeList.setColumnInfo(info);
/*  87 */     info = new ColumnInfo(LocaleResources.getString("apTitleDisplayRule", this.m_ctx), "schDisplayRule", 10.0D);
/*  88 */     this.m_localeList.setColumnInfo(info);
/*     */ 
/*  90 */     String clmns = "schLocale,schDisplayRule";
/*  91 */     this.m_localeList.setVisibleColumns(clmns);
/*  92 */     this.m_localeList.setIDColumn("schLocale");
/*  93 */     this.m_localeList.init();
/*  94 */     this.m_localeList.useDefaultListener();
/*     */ 
/*  96 */     JPanel buttonPnl = new PanePanel();
/*  97 */     this.m_helper.makePanelGridBag(buttonPnl, 2);
/*     */ 
/*  99 */     String[][] btnInfo = { { "edit", "apSchEditButton", "1", "apSchViewDisplayRuleTitle" }, { "delete", "apSchDeleteButton", "1", "apReadableButtonDeleteRule" }, { "reset", "apSchResetButton", "1", "apReadableButtonResetRule" }, { "resetAll", "apSchResetAllButton", "0", "apReadableButtonResetAllRules" } };
/*     */ 
/* 106 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 108 */       String cmd = btnInfo[i][0];
/* 109 */       String label = LocaleResources.getString(btnInfo[i][1], this.m_ctx);
/* 110 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/*     */ 
/* 112 */       JButton btn = this.m_localeList.addButton(label, isControlled);
/* 113 */       btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_ctx));
/* 114 */       btn.setActionCommand(cmd);
/* 115 */       btn.addActionListener(this);
/*     */ 
/* 117 */       this.m_helper.addLastComponentInRow(buttonPnl, btn);
/*     */     }
/*     */ 
/* 121 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 122 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 123 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 124 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 125 */     this.m_helper.addComponent(pnl, this.m_localeList);
/*     */ 
/* 128 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 129 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 130 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 131 */     this.m_helper.addLastComponentInRow(pnl, buttonPnl);
/*     */ 
/* 133 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected Vector createDisplayExpressionList(DataBinder binder)
/*     */   {
/* 139 */     String str = binder.getLocal("schViewColumns");
/* 140 */     Vector v = StringUtils.parseArray(str, ',', '^');
/* 141 */     for (int i = 0; i < v.size(); ++i)
/*     */     {
/* 143 */       String column = (String)v.elementAt(i);
/* 144 */       v.setElementAt("<$" + column + "$>", i);
/*     */     }
/* 146 */     return v;
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 157 */     String cmd = e.getActionCommand();
/* 158 */     String selObj = this.m_localeList.getSelectedObj();
/* 159 */     DataResultSet drset = (DataResultSet)this.m_localeList.getResultSet();
/* 160 */     if (cmd.equals("resetAll"))
/*     */     {
/* 162 */       resetAll(drset);
/*     */     }
/*     */     else
/*     */     {
/* 166 */       int selIndex = this.m_localeList.getSelectedIndex();
/* 167 */       if (selIndex < 0)
/*     */       {
/* 170 */         return;
/*     */       }
/* 172 */       Properties props = this.m_localeList.getDataAt(selIndex);
/* 173 */       if (cmd.equals("edit"))
/*     */       {
/* 175 */         edit(props, drset);
/*     */       }
/* 177 */       else if (cmd.equals("delete"))
/*     */       {
/* 179 */         delete(props, drset);
/* 180 */         selObj = null;
/*     */       }
/* 182 */       else if (cmd.equals("reset"))
/*     */       {
/* 184 */         reset(props, drset);
/*     */       }
/*     */     }
/* 187 */     this.m_localeList.reloadList(selObj);
/*     */   }
/*     */ 
/*     */   protected void edit(Properties props, DataResultSet drset)
/*     */   {
/* 192 */     AddViewDisplayRuleDlg dlg = new AddViewDisplayRuleDlg(this.m_systemInterface, LocaleResources.getString("apSchViewDisplayRuleTitle", this.m_ctx), DialogHelpTable.getHelpPage("AddViewDisplayRule"));
/*     */ 
/* 196 */     Vector clmnList = createDisplayExpressionList(this.m_viewData);
/* 197 */     int result = dlg.init(props, clmnList);
/* 198 */     if (result != 1)
/*     */       return;
/*     */     try
/*     */     {
/* 202 */       int idIndex = ResultSetUtils.getIndexMustExist(drset, "schLocale");
/* 203 */       int ruleIndex = ResultSetUtils.getIndexMustExist(drset, "schDisplayRule");
/*     */ 
/* 206 */       Vector row = drset.findRow(idIndex, props.getProperty("schLocale"));
/* 207 */       row.setElementAt(props.getProperty("schDisplayRule"), ruleIndex);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 211 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void delete(Properties props, DataResultSet drset)
/*     */   {
/* 218 */     String locale = props.getProperty("schLocale");
/* 219 */     IdcMessage msg = IdcMessageFactory.lc("apSchDeleteRule", new Object[] { locale });
/* 220 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 4);
/* 221 */     if (result != 2)
/*     */       return;
/*     */     try
/*     */     {
/* 225 */       int index = ResultSetUtils.getIndexMustExist(drset, "schLocale");
/* 226 */       Vector row = drset.findRow(index, locale);
/* 227 */       if (row != null)
/*     */       {
/* 229 */         drset.deleteCurrentRow();
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 234 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void reset(Properties props, DataResultSet drset)
/*     */   {
/*     */     try
/*     */     {
/* 243 */       int index = ResultSetUtils.getIndexMustExist(drset, "schLocale");
/* 244 */       String locale = props.getProperty("schLocale");
/* 245 */       Vector row = drset.findRow(index, locale);
/* 246 */       if (row != null)
/*     */       {
/* 248 */         int ruleIndex = ResultSetUtils.getIndexMustExist(drset, "schDisplayRule");
/* 249 */         row.setElementAt("", ruleIndex);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 254 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void resetAll(DataResultSet drset)
/*     */   {
/*     */     try
/*     */     {
/* 262 */       int ruleIndex = ResultSetUtils.getIndexMustExist(drset, "schDisplayRule");
/* 263 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 265 */         Vector row = drset.getCurrentRowValues();
/* 266 */         row.setElementAt("", ruleIndex);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 271 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 283 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.ViewDisplayPanel
 * JD-Core Version:    0.5.4
 */