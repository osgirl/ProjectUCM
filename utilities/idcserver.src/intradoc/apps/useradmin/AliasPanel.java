/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.BasePanel;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.AliasData;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AliasPanel extends BasePanel
/*     */   implements Observer, ActionListener
/*     */ {
/*     */   protected UdlPanel m_aliasList;
/*     */ 
/*     */   public AliasPanel()
/*     */   {
/*  55 */     this.m_aliasList = null;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys) throws ServiceException
/*     */   {
/*  60 */     super.init(sys);
/*     */ 
/*  63 */     DataBinder binder = new DataBinder();
/*  64 */     AppLauncher.executeService("GET_ALIASES", binder);
/*     */ 
/*  66 */     refreshList(null);
/*  67 */     AppLauncher.addSubjectObserver("aliases", this);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/*  73 */     initList();
/*     */ 
/*  75 */     String[][] buttonInfo = { { LocaleResources.getString("apDlgButtonAdd", this.m_cxt), "add", "0" }, { LocaleResources.getString("apDlgButtonEdit", this.m_cxt), "edit", "1" }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "delete", "1" } };
/*     */ 
/*  81 */     JPanel btnActionsPanel = new PanePanel();
/*  82 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/*  84 */       JButton btn = new JButton(buttonInfo[i][0]);
/*  85 */       btn.setActionCommand(buttonInfo[i][1]);
/*  86 */       btn.addActionListener(this);
/*  87 */       btnActionsPanel.add(btn);
/*     */ 
/*  89 */       boolean isListControlled = StringUtils.convertToBool(buttonInfo[i][2], false);
/*  90 */       if (!isListControlled)
/*     */         continue;
/*  92 */       this.m_aliasList.addControlComponent(btn);
/*     */     }
/*     */ 
/*  96 */     this.m_helper.makePanelGridBag(this, 1);
/*  97 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  98 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*     */ 
/* 100 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 0, 0, 0);
/* 101 */     this.m_helper.addLastComponentInRow(this, this.m_aliasList);
/*     */ 
/* 103 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 0, 0, 0);
/* 104 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 105 */     this.m_helper.addComponent(this, btnActionsPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel initList()
/*     */   {
/* 111 */     this.m_aliasList = new UdlPanel(LocaleResources.getString("apLabelAliases", this.m_cxt), null, 500, 20, "Aliases", true);
/*     */ 
/* 113 */     this.m_aliasList.init();
/* 114 */     this.m_aliasList.useDefaultListener();
/* 115 */     this.m_aliasList.m_list.addActionListener(this);
/*     */ 
/* 118 */     this.m_aliasList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleName", this.m_cxt), "dAlias", 11.0D));
/*     */ 
/* 120 */     this.m_aliasList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleDisplayName", this.m_cxt), "dAliasDisplayName", 33.0D));
/*     */ 
/* 122 */     this.m_aliasList.setColumnInfo(new ColumnInfo(LocaleResources.getString("apTitleDescription", this.m_cxt), "dAliasDescription", 33.0D));
/*     */ 
/* 124 */     this.m_aliasList.setVisibleColumns("dAlias,dAliasDisplayName,dAliasDescription");
/* 125 */     this.m_aliasList.setIDColumn("dAlias");
/*     */ 
/* 127 */     return this.m_aliasList;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 132 */     Object src = e.getSource();
/* 133 */     if (src == this.m_aliasList.m_list)
/*     */     {
/* 135 */       addOrEditAlias(false);
/* 136 */       return;
/*     */     }
/*     */ 
/* 139 */     String cmd = e.getActionCommand();
/* 140 */     if (cmd.equals("add"))
/*     */     {
/* 142 */       addOrEditAlias(true);
/*     */     }
/* 144 */     else if (cmd.equals("edit"))
/*     */     {
/* 146 */       addOrEditAlias(false);
/*     */     } else {
/* 148 */       if (!cmd.equals("delete"))
/*     */         return;
/* 150 */       deleteAlias();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addOrEditAlias(boolean isAdd)
/*     */   {
/* 156 */     Properties props = null;
/* 157 */     String title = LocaleResources.getString("apTitleAddNewAlias", this.m_cxt);
/* 158 */     String helpPageName = "AddNewAlias";
/*     */ 
/* 160 */     if (!isAdd)
/*     */     {
/* 162 */       int index = this.m_aliasList.getSelectedIndex();
/* 163 */       if (index < 0)
/*     */       {
/* 165 */         reportError(null, IdcMessageFactory.lc("apSelectAliasToEdit", new Object[0]));
/* 166 */         return;
/*     */       }
/* 168 */       props = this.m_aliasList.getDataAt(index);
/* 169 */       title = LocaleResources.getString("apTitleEditAlias", this.m_cxt, props.getProperty("dAlias"));
/* 170 */       helpPageName = "EditAlias";
/*     */     }
/*     */ 
/* 173 */     EditAliasDlg dlg = new EditAliasDlg(this.m_systemInterface, title, this.m_aliasList.getResultSet(), DialogHelpTable.getHelpPage(helpPageName));
/*     */ 
/* 175 */     dlg.init(props);
/* 176 */     if (dlg.prompt() != 1)
/*     */       return;
/*     */     try
/*     */     {
/* 180 */       refreshList(dlg.getAlias());
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 184 */       reportError(exp);
/* 185 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteAlias()
/*     */   {
/* 192 */     int index = this.m_aliasList.getSelectedIndex();
/* 193 */     if (index < 0)
/*     */     {
/* 195 */       reportError(null, IdcMessageFactory.lc("apSelectAliasToDelete", new Object[0]));
/* 196 */       return;
/*     */     }
/*     */ 
/* 199 */     Properties props = this.m_aliasList.getDataAt(index);
/* 200 */     String name = props.getProperty("dAlias");
/*     */ 
/* 202 */     if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifyAliasDelete", new Object[] { name }), 4) != 2)
/*     */     {
/*     */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 209 */       DataBinder binder = new DataBinder();
/* 210 */       binder.setLocalData(props);
/* 211 */       AppLauncher.executeService("DELETE_ALIAS", binder);
/* 212 */       refreshList(null);
/* 213 */       this.m_aliasList.enableDisable(false);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 217 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void refreshList(String selectedObj)
/*     */   {
/* 224 */     AliasData aliases = (AliasData)SharedObjects.getTable("Alias");
/* 225 */     this.m_aliasList.refreshList(aliases, selectedObj);
/*     */   }
/*     */ 
/*     */   public void update(Observable obs, Object arg)
/*     */   {
/* 233 */     String selectedObj = this.m_aliasList.getSelectedObj();
/* 234 */     refreshList(selectedObj);
/*     */   }
/*     */ 
/*     */   public void removeNotify()
/*     */   {
/* 240 */     AppLauncher.removeSubjectObserver("aliases", this);
/* 241 */     super.removeNotify();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 246 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.AliasPanel
 * JD-Core Version:    0.5.4
 */