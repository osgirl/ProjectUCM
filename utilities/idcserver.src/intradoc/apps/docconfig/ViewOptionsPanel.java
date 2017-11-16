/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ViewOptionsPanel extends DocConfigPanel
/*     */   implements ItemListener
/*     */ {
/*     */   protected DisplayChoice[] m_clmnChoice;
/*     */   protected DisplayChoice m_fieldChoice;
/*     */   protected DisplayChoice m_clientFieldChoice;
/*     */   protected Vector m_serverSortComponents;
/*     */   protected Vector m_clientSortComponents;
/*     */   protected JCheckBox m_clientSortedBox;
/*     */   protected JCheckBox m_serverSortedBox;
/*     */   protected JCheckBox m_databaseSortedBox;
/*     */   protected Component[] m_specificSortFieldControls;
/*     */   protected Component[] m_clientFieldControls;
/*     */ 
/*     */   public ViewOptionsPanel()
/*     */   {
/*  48 */     this.m_clmnChoice = null;
/*  49 */     this.m_fieldChoice = null;
/*  50 */     this.m_clientFieldChoice = null;
/*     */ 
/*  52 */     this.m_serverSortComponents = new IdcVector();
/*  53 */     this.m_clientSortComponents = new IdcVector();
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  64 */     super.initEx(sys, binder);
/*     */ 
/*  66 */     JPanel panel = initUI(binder);
/*  67 */     this.m_helper.makePanelGridBag(this, 1);
/*     */ 
/*  69 */     this.m_helper.addComponent(this, panel);
/*     */   }
/*     */ 
/*     */   protected JPanel initUI(DataBinder binder)
/*     */   {
/*  74 */     SystemInterface si = this.m_systemInterface;
/*  75 */     JPanel pnl = new PanePanel();
/*  76 */     this.m_helper.makePanelGridBag(pnl, 2);
/*     */ 
/*  79 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(18);
/*     */ 
/*  81 */     if (SharedObjects.getEnvValueAsBoolean("IsBeta", false))
/*     */     {
/*  83 */       JCheckBox typeBox = new CustomCheckbox(si.getString("apSchIsTypeAhead"));
/*  84 */       this.m_helper.addExchangeComponent(pnl, typeBox, "schIsTypeAhead");
/*     */     }
/*     */ 
/*  88 */     String msg = si.getString("apSchWhatIsAnEditCriteria");
/*  89 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  90 */     this.m_helper.addComponent(pnl, new CustomText(msg, 95));
/*     */ 
/*  93 */     updateOrCreateColumnChoices();
/*     */ 
/*  96 */     for (int i = 0; i < 2; ++i)
/*     */     {
/*  98 */       JPanel fieldPanel = new PanePanel();
/*  99 */       this.m_helper.makePanelGridBag(fieldPanel, 2);
/*     */ 
/* 101 */       this.m_helper.m_gridHelper.prepareAddRowElement(13);
/* 102 */       this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 103 */       this.m_helper.addExchangeComponent(fieldPanel, this.m_clmnChoice[i], "schCriteriaField" + i);
/*     */ 
/* 105 */       this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 106 */       CustomLabel sepField = new CustomLabel(si.getString("apSchCriteriaSeparator"));
/* 107 */       this.m_helper.addComponent(fieldPanel, sepField);
/*     */ 
/* 109 */       this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 110 */       this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 111 */       this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 112 */       this.m_helper.addExchangeComponent(fieldPanel, new CustomTextField(30), "schCriteriaValue" + i);
/*     */ 
/* 115 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 116 */       this.m_helper.addComponent(pnl, fieldPanel);
/*     */     }
/*     */ 
/* 120 */     initSortUI(pnl, binder);
/*     */ 
/* 122 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void initSortUI(JPanel pnl, DataBinder binder)
/*     */   {
/* 127 */     SystemInterface si = this.m_systemInterface;
/*     */ 
/* 129 */     this.m_helper.m_gridHelper.m_gc.anchor = 13;
/* 130 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 131 */     this.m_helper.m_gridHelper.m_gc.gridwidth = 1;
/*     */ 
/* 134 */     this.m_helper.m_gridHelper.m_gc.anchor = 17;
/*     */ 
/* 136 */     String msg = si.localizeCaption("apSchSortAndLoadOptions");
/* 137 */     this.m_clientSortedBox = new CustomCheckbox(si.getString("apSchIsClientSorted"));
/*     */ 
/* 139 */     this.m_helper.addLabelFieldPair(pnl, msg, this.m_clientSortedBox, "schIsClientSorted");
/*     */ 
/* 141 */     this.m_clientSortedBox.addItemListener(this);
/*     */ 
/* 143 */     this.m_serverSortedBox = new CustomCheckbox(si.getString("apSchIsServerSorted"));
/*     */ 
/* 145 */     this.m_helper.addLabelFieldPair(pnl, "", this.m_serverSortedBox, "schIsServerSorted");
/*     */ 
/* 147 */     this.m_serverSortedBox.addItemListener(this);
/*     */ 
/* 149 */     this.m_databaseSortedBox = new CustomCheckbox(si.getString("apSchIsDatabaseSorted"));
/*     */ 
/* 151 */     this.m_helper.addLabelFieldPair(pnl, "", this.m_databaseSortedBox, "schIsDatabaseSorted");
/*     */ 
/* 153 */     this.m_databaseSortedBox.addItemListener(this);
/*     */ 
/* 155 */     String[][] display = { { "ascending", "apSortAscending" }, { "descending", "apSortDescending" } };
/*     */ 
/* 160 */     LocaleResources.localizeDoubleArray(display, this.m_ctx, 1);
/*     */ 
/* 162 */     this.m_clientFieldControls = new Component[2];
/* 163 */     this.m_clientFieldControls[0] = this.m_helper.addLabelFieldPairEx(pnl, si.localizeCaption("apSchSortClientLabel"), this.m_clientFieldChoice, "schClientSortField", false);
/*     */ 
/* 166 */     this.m_clientFieldControls[1] = this.m_clientFieldChoice;
/* 167 */     this.m_clientSortComponents.addElement(this.m_clientFieldControls[0]);
/* 168 */     this.m_clientSortComponents.addElement(this.m_clientFieldControls[1]);
/*     */ 
/* 170 */     DisplayChoice clientSortChoice = new DisplayChoice();
/* 171 */     clientSortChoice.init(display);
/* 172 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 173 */     this.m_helper.addExchangeComponent(pnl, clientSortChoice, "schClientSortOrder");
/* 174 */     this.m_clientSortComponents.addElement(clientSortChoice);
/*     */ 
/* 176 */     this.m_serverSortComponents.addElement(this.m_fieldChoice);
/* 177 */     this.m_specificSortFieldControls = new Component[2];
/* 178 */     this.m_specificSortFieldControls[0] = this.m_helper.addLabelFieldPairEx(pnl, si.localizeCaption("apSchSortFieldLabel"), this.m_fieldChoice, "schSortField", false);
/*     */ 
/* 181 */     this.m_serverSortComponents.addElement(this.m_specificSortFieldControls[0]);
/* 182 */     this.m_specificSortFieldControls[1] = this.m_fieldChoice;
/*     */ 
/* 184 */     DisplayChoice sortChoice = new DisplayChoice();
/* 185 */     sortChoice.init(display);
/* 186 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 187 */     this.m_helper.addExchangeComponent(pnl, sortChoice, "schSortOrder");
/* 188 */     this.m_serverSortComponents.addElement(sortChoice);
/*     */   }
/*     */ 
/*     */   public void loadComponents()
/*     */   {
/* 195 */     super.loadComponents();
/* 196 */     itemStateChanged(null);
/*     */   }
/*     */ 
/*     */   protected void enableDisable(Vector cmpList, boolean enable)
/*     */   {
/* 201 */     int num = cmpList.size();
/* 202 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 204 */       Component cmp = (Component)cmpList.elementAt(i);
/* 205 */       cmp.setEnabled(enable);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updateOrCreateColumnChoices()
/*     */   {
/* 211 */     if (this.m_fieldChoice == null)
/*     */     {
/* 213 */       this.m_fieldChoice = new DisplayChoice();
/*     */     }
/*     */ 
/* 216 */     if (this.m_clmnChoice == null)
/*     */     {
/* 218 */       this.m_clmnChoice = new DisplayChoice[2];
/* 219 */       for (int i = 0; i < 2; ++i)
/*     */       {
/* 221 */         this.m_clmnChoice[i] = new DisplayChoice();
/*     */       }
/*     */     }
/*     */ 
/* 225 */     this.m_docContext.updateColumnList(this.m_fieldChoice, false, false);
/* 226 */     for (int i = 0; i < this.m_clmnChoice.length; ++i)
/*     */     {
/* 228 */       this.m_docContext.updateColumnList(this.m_clmnChoice[i], true, false);
/*     */     }
/*     */ 
/* 231 */     if (this.m_clientFieldChoice == null)
/*     */     {
/* 233 */       this.m_clientFieldChoice = new DisplayChoice();
/*     */     }
/*     */ 
/* 236 */     this.m_docContext.updateColumnList(this.m_clientFieldChoice, false, true);
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */   {
/* 242 */     updateOrCreateColumnChoices();
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 247 */     for (int i = 0; i < this.m_clmnChoice.length; ++i)
/*     */     {
/* 249 */       this.m_clmnChoice[i].setEnabled(true);
/*     */     }
/*     */ 
/* 253 */     enableDisable(this.m_clientSortComponents, this.m_clientSortedBox.isSelected());
/* 254 */     enableDisable(this.m_serverSortComponents, (this.m_serverSortedBox.isSelected()) || (this.m_databaseSortedBox.isSelected()));
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 266 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.ViewOptionsPanel
 * JD-Core Version:    0.5.4
 */