/*     */ package intradoc.apps.pagebuilder;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.gui.QueryBuilderHelper;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ 
/*     */ public class EditQueryResultsPanel extends EditQueryBasePanel
/*     */   implements ItemListener
/*     */ {
/*     */   protected JComboBox m_resultOptions;
/*     */   protected EditResultCustomPanel m_resultsCustomPanel;
/*     */ 
/*     */   public EditQueryResultsPanel()
/*     */   {
/*  48 */     this.m_resultOptions = null;
/*  49 */     this.m_resultsCustomPanel = null;
/*     */   }
/*     */ 
/*     */   public void init(DialogHelper helper, PageManagerContext pageContext, DocumentLocalizedProfile docProfile, boolean isNew)
/*     */     throws ServiceException
/*     */   {
/*  55 */     this.m_systemInterface = helper.m_exchange.m_sysInterface;
/*  56 */     this.m_ctx = this.m_systemInterface.getExecutionContext();
/*  57 */     this.m_pageServices = pageContext;
/*     */ 
/*  59 */     this.m_helper = new ContainerHelper();
/*  60 */     this.m_helper.attachToContainer(this, this.m_systemInterface, helper.m_props);
/*  61 */     this.m_helper.m_componentBinder = this;
/*  62 */     this.m_helper.m_gridHelper.useGridBag(this);
/*     */ 
/*  64 */     initUI();
/*     */ 
/*  66 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  72 */     boolean useAltaVista = SharedObjects.getEnvValueAsBoolean("UseAltaVista", false);
/*     */ 
/*  74 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/*  77 */     JPanel resultsAttributes = new PanePanel();
/*  78 */     gh.useGridBag(resultsAttributes);
/*  79 */     gh.m_gc.fill = 1;
/*  80 */     gh.m_gc.weightx = 1.0D;
/*     */ 
/*  82 */     this.m_helper.addLabelFieldPair(resultsAttributes, LocaleResources.getString("apLabelPageTitle", this.m_ctx), new CustomTextField(50), "ResultsTitle");
/*     */ 
/*  85 */     DisplayChoice sortOptions = new DisplayChoice();
/*  86 */     this.m_queryHelper.addSortFieldList(sortOptions);
/*  87 */     this.m_helper.addLabelFieldPair(resultsAttributes, LocaleResources.getString("apLabelSortResultsBy", this.m_ctx), sortOptions, "SortField");
/*     */ 
/*  90 */     DisplayChoice sortOrder = new DisplayChoice();
/*     */ 
/*  92 */     if (useAltaVista)
/*     */     {
/*  94 */       sortOrder.init(new String[][] { { "-", LocaleResources.getString("apLabelAscending", this.m_ctx) }, { "+", LocaleResources.getString("apLabelDescending", this.m_ctx) } });
/*     */     }
/*     */     else
/*     */     {
/*  99 */       sortOrder.init(new String[][] { { "Asc", LocaleResources.getString("apLabelAscending", this.m_ctx) }, { "Desc", LocaleResources.getString("apLabelDescending", this.m_ctx) } });
/*     */     }
/*     */ 
/* 102 */     this.m_helper.addLabelFieldPair(resultsAttributes, LocaleResources.getString("apLabelSortOrder", this.m_ctx), sortOrder, "SortOrder");
/*     */ 
/* 105 */     this.m_resultOptions = new CustomChoice();
/* 106 */     this.m_resultOptions.addItemListener(this);
/* 107 */     this.m_helper.addLabelFieldPair(resultsAttributes, LocaleResources.getString("apLabelResultTemplatePage", this.m_ctx), this.m_resultOptions, "ResultTemplate");
/*     */ 
/* 111 */     this.m_resultsCustomPanel = new EditResultCustomPanel(true, this.m_helper, this.m_resultOptions);
/*     */ 
/* 113 */     this.m_resultsCustomPanel.init();
/* 114 */     addResultList(this.m_resultOptions);
/*     */ 
/* 116 */     JPanel queryOptions = new PanePanel();
/* 117 */     gh.useGridBag(queryOptions);
/* 118 */     this.m_helper.addPanelTitle(queryOptions, LocaleResources.getString("apLabelResultPageProperties", this.m_ctx));
/*     */ 
/* 120 */     gh.m_gc.fill = 1;
/* 121 */     gh.m_gc.weightx = 1.0D;
/* 122 */     gh.prepareAddLastRowElement();
/* 123 */     this.m_helper.addComponent(queryOptions, resultsAttributes);
/* 124 */     gh.m_gc.weighty = 1.0D;
/* 125 */     this.m_helper.addComponent(queryOptions, this.m_resultsCustomPanel);
/*     */ 
/* 127 */     this.m_helper.addLastComponentInRow(this, queryOptions);
/*     */   }
/*     */ 
/*     */   protected void addResultList(JComboBox opts)
/*     */   {
/* 132 */     DataResultSet dset = this.m_resultsCustomPanel.getTemplates();
/* 133 */     for (dset.first(); dset.isRowPresent(); dset.next())
/*     */     {
/* 135 */       String name = dset.getStringValue(0);
/* 136 */       opts.addItem(name);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 146 */     JComboBox src = (JComboBox)e.getSource();
/* 147 */     if (!src.equals(this.m_resultOptions))
/*     */       return;
/* 149 */     int index = src.getSelectedIndex();
/* 150 */     this.m_resultsCustomPanel.alterTemplateTextFields(index);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 161 */     if (updateComponent)
/*     */     {
/* 163 */       String str = this.m_queryHelper.getQueryProp(name);
/* 164 */       exchange.m_compValue = str;
/*     */ 
/* 168 */       if (name.equals("Text2"))
/*     */       {
/* 172 */         boolean isCustom = this.m_resultsCustomPanel.m_useCustom.isSelected();
/* 173 */         if (!isCustom)
/*     */         {
/* 175 */           int index = this.m_resultOptions.getSelectedIndex();
/* 176 */           if ((index == -1) && (this.m_resultOptions.getItemCount() > 0))
/*     */           {
/* 178 */             index = 0;
/*     */           }
/* 180 */           this.m_resultsCustomPanel.alterTemplateTextFields(index);
/* 181 */           exchange.m_compValue = this.m_resultsCustomPanel.m_text2.getText();
/*     */         }
/*     */ 
/* 185 */         this.m_resultOptions.setEnabled(!isCustom);
/* 186 */         this.m_resultsCustomPanel.setEnableDisableEdit(isCustom);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 191 */       String str = exchange.m_compValue;
/* 192 */       this.m_queryHelper.setQueryProp(name, str);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 198 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.pagebuilder.EditQueryResultsPanel
 * JD-Core Version:    0.5.4
 */